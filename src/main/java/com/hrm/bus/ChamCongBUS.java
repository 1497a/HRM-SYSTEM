package com.hrm.bus;

import com.hrm.model.*;
import com.hrm.dao.ChamCongDAO;
import com.hrm.model.CauHinhPhuCap;
import com.hrm.util.HRMConstants;
import com.hrm.util.PermissionCodes;
import com.hrm.util.SessionContext;
import com.hrm.util.ValidationUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Service cho Attendance (Ca làm, Chấm công, Đăng ký làm thêm).
 * Singleton pattern. Sử dụng ChamCongDAO (JDBC).
 */
public class ChamCongBUS {

    private static ChamCongBUS instance;
    private final ChamCongDAO repository;
    private static final String ACTION_ATTENDANCE_VIEW = PermissionCodes.ATTENDANCE_VIEW;
    private static final String ACTION_ATTENDANCE_MANAGE = PermissionCodes.ATTENDANCE_MANAGE;
    private static final String ACTION_ALLOWANCE_MANAGE  = PermissionCodes.ALLOWANCE_MANAGE;
    private static final String ACTION_ATTENDANCE_CHECKIN = PermissionCodes.ATTENDANCE_CHECKIN;
    private static final String ACTION_OVERTIME_REQUEST = PermissionCodes.OVERTIME_REQUEST;
    private static final String ACTION_OVERTIME_APPROVE = PermissionCodes.OVERTIME_APPROVE;
    /** Số giờ OT tối đa trong một tháng */
    private static final double OT_TONG_TOI_DA_THANG = 40.0;
    /** Trễ quá 15 phút → đi muộn */
    private static final int PHUT_DI_MUON = 15;
    private ChamCongBUS() {
        this.repository = ChamCongDAO.getInstance();
    }

    public static synchronized ChamCongBUS getInstance() {
        if (instance == null) {
            instance = new ChamCongBUS();
        }
        return instance;
    }

    // =====================================================================
    // CA LÀM
    // =====================================================================
    public List<CaLam> getAllCaLam() {
        return repository.findAllCaLam();
    }

    public List<CaLam> getActiveCaLam() {
        return repository.findActiveCaLam();
    }

    /**
     * Lưu ca làm (thêm mới hoặc cập nhật).
     * Validate: maCaLam không rỗng, giờ hợp lệ.
     */
    public KetQua<CaLam> saveCaLam(CaLam caLam) {
        KetQua<Void> permission = validateAttendanceManagePermission();
        if (!permission.isSuccess()) return KetQua.error(permission.getMessage());
        if (caLam.getId() == null || caLam.getId().trim().isEmpty()) {
            return KetQua.error("Mã ca làm không được để trống.");
        }
        if (caLam.getTenCaLam() == null || caLam.getTenCaLam().trim().isEmpty()) {
            return KetQua.error("Tên ca làm không được để trống.");
        }
        if (caLam.getGioBatDau() == null || caLam.getGioKetThuc() == null) {
            return KetQua.error("Giờ bắt đầu và kết thúc không được để trống.");
        }
        try {
            int rows = repository.saveCaLam(caLam);
            if (rows <= 0) {
                return KetQua.error("Không thể lưu ca làm. Vui lòng thử lại.");
            }
            return KetQua.success(caLam, "Đã lưu ca làm '" + caLam.getTenCaLam() + "'.");
        } catch (Exception e) {
            return KetQua.error("Lỗi lưu ca làm: " + e.getMessage());
        }
    }

    // Legacy compat
    public KetQua<CaLam> themCaLam(String ma, String ten, String gioBD, String gioKT, double sg, boolean ot) {
        KetQua<Void> permission = validateAttendanceManagePermission();
        if (!permission.isSuccess()) return KetQua.error(permission.getMessage());
        CaLam existing = repository.findCaLamById(ma.trim());
        if (existing != null) return KetQua.error("Mã ca đã tồn tại.");
        LocalTime tBD, tKT;
        try {
            tBD = LocalTime.parse(gioBD.trim());
            tKT = LocalTime.parse(gioKT.trim());
        } catch (Exception e) {
            return KetQua.error("Định dạng giờ không hợp lệ (HH:mm).");
        }
        try {
            CaLam ca = new CaLam(ma.trim(), ten.trim(), tBD, tKT);
            ca.setSoGioChuan(sg);
            ca.setChoPhepLamThem(ot);
            int rows = repository.saveCaLam(ca);
            if (rows <= 0) {
                return KetQua.error("Không thể thêm ca làm. Vui lòng thử lại.");
            }
            return KetQua.success(ca, "Đã thêm ca làm '" + ten + "'.");
        } catch (Exception e) {
            return KetQua.error("Lỗi lưu ca làm: " + e.getMessage());
        }
    }

    public KetQua<CaLam> suaCaLam(String ma, String ten, String gioBD, String gioKT, double sg, boolean ot) {
        KetQua<Void> permission = validateAttendanceManagePermission();
        if (!permission.isSuccess()) return KetQua.error(permission.getMessage());
        CaLam ca = repository.findCaLamById(ma);
        if (ca == null) return KetQua.error("Không tìm thấy ca làm.");
        LocalTime tBD, tKT;
        try {
            tBD = LocalTime.parse(gioBD.trim());
            tKT = LocalTime.parse(gioKT.trim());
        } catch (Exception e) {
            return KetQua.error("Định dạng giờ không hợp lệ (HH:mm).");
        }
        try {
            ca.setTenCaLam(ten.trim());
            ca.setGioBatDau(tBD);
            ca.setGioKetThuc(tKT);
            ca.setSoGioChuan(sg);
            ca.setChoPhepLamThem(ot);
            int rows = repository.saveCaLam(ca);
            if (rows <= 0) {
                return KetQua.error("Không thể cập nhật ca làm. Vui lòng thử lại.");
            }
            return KetQua.success(ca, "Đã cập nhật ca làm.");
        } catch (Exception e) {
            return KetQua.error("Lỗi cập nhật ca làm: " + e.getMessage());
        }
    }

    public KetQua<Void> xoaCaLam(String ma) {
        KetQua<Void> permission = validateAttendanceManagePermission();
        if (!permission.isSuccess()) return permission;
        CaLam ca = repository.findCaLamById(ma);
        if (ca == null) return KetQua.error("Không tìm thấy ca làm.");
        int rows = repository.deleteCaLam(ma);
        if (rows <= 0) {
            return KetQua.error("Không thể xóa ca làm. Vui lòng thử lại.");
        }
        return KetQua.success(null, "Đã ngừng ca làm '" + ca.getTenCaLam() + "'.");
    }

    // =====================================================================
    // CHẤM CÔNG
    // =====================================================================
    /**
     * Check-in: xác thực NV chưa check-in hôm nay, tính trạng thái đi muộn.
     */
    public KetQua<ChamCong> checkIn(String maNV, String maCaLam, String phuongThuc) {
        KetQua<Void> permission = validateCheckInOutPermission(ACTION_ATTENDANCE_CHECKIN, maNV);
        if (!permission.isSuccess()) return KetQua.error(permission.getMessage());
        LocalDate today = LocalDate.now();
        if (ValidationUtils.isBlank(maNV)) {
            return KetQua.error("Mã nhân viên không hợp lệ.");
        }
        if (repository.findChamCongByNVAndNgay(maNV, today) != null) {
            return KetQua.error("Bạn đã check-in hôm nay rồi.");
        }
        CaLam caLam = repository.findCaLamById(maCaLam);
        if (caLam == null) {
            return KetQua.error("Ca làm không hợp lệ.");
        }
        ChamCong cc = taoBanGhiCheckIn(maNV, today, caLam, LocalDateTime.now(), resolvePhuongThuc(phuongThuc), false);
        int id = repository.saveChamCong(cc);
        if (id <= 0) {
            return KetQua.error("Không thể lưu chấm công check-in. Vui lòng thử lại.");
        }
        return KetQua.success(cc, "Check-in thành công lúc " + cc.getGioVao().toLocalTime().toString().substring(0, 5) + ".");
    }

    // Legacy: used by existing AttendancePanel
    public KetQua<ChamCong> checkIn(String maNV, String maCaLam) {
        return checkIn(maNV, maCaLam, HRMConstants.PHUONG_THUC_CHAM_CONG_THU_CONG);
    }

    /**
     * Check-in thủ công với ca làm được chọn và cờ OT.
     */
    public KetQua<ChamCong> checkIn(String maNV, String maCaLam, boolean laOT) {
        KetQua<Void> permission = validateCheckInOutPermission(ACTION_ATTENDANCE_CHECKIN, maNV);
        if (!permission.isSuccess()) return KetQua.error(permission.getMessage());
        if (ValidationUtils.isBlank(maNV)) return KetQua.error("Mã nhân viên không hợp lệ.");
        if (repository.findChamCongByNVAndNgay(maNV, LocalDate.now()) != null)
            return KetQua.error("Bạn đã check-in hôm nay rồi.");
        if (laOT && !coOTDaDuyetHomNay(maNV))
            return KetQua.error("Bạn chưa có đơn OT được duyệt cho hôm nay. Không thể đánh dấu ca OT.");
        CaLam caLam = repository.findCaLamById(maCaLam);
        if (caLam == null) return KetQua.error("Ca làm không hợp lệ.");
        ChamCong cc = taoBanGhiCheckIn(maNV, LocalDate.now(), caLam, LocalDateTime.now(),
                ChamCong.PhuongThuc.THU_CONG, laOT);
        int id = repository.saveChamCong(cc);
        if (id <= 0) return KetQua.error("Không thể lưu chấm công check-in. Vui lòng thử lại.");
        String otTag = laOT ? " [CA OT]" : "";
        return KetQua.success(cc, "Check-in thành công" + otTag + " — Ca: "
            + caLam.getTenCaLam() + " (" + caLam.getGioBatDau() + " - " + caLam.getGioKetThuc() + ")");
    }

    /**
     * Check-out: tính soGioLam, gioLamThem nếu có OT đã duyệt.
     */
    public KetQua<ChamCong> checkOut(String maNV) {
        KetQua<Void> permission = validateCheckInOutPermission(ACTION_ATTENDANCE_CHECKIN, maNV);
        if (!permission.isSuccess()) return KetQua.error(permission.getMessage());
        if (ValidationUtils.isBlank(maNV)) {
            return KetQua.error("Mã nhân viên không hợp lệ.");
        }
        LocalDate today = LocalDate.now();
        ChamCong cc = repository.findChamCongByNVAndNgay(maNV, today);
        if (cc == null) {
            return KetQua.error("Bạn chưa check-in hôm nay.");
        }
        if (cc.daCheckOut()) {
            return KetQua.error("Bạn đã check-out rồi.");
        }
        LocalDateTime gioRa = LocalDateTime.now();
        cc.setGioRa(gioRa);
        cc.setSoGioLam(cc.tinhSoGioLam());
        // Kiểm tra giờ làm thêm
        boolean hasOT = repository.hasDuyetForNVAndNgay(maNV, today);
        if (hasOT) {
            CaLam caLam = repository.findCaLamById(cc.getMaCaLam());
            if (caLam != null && cc.getSoGioLam() > caLam.getSoGioChuan()) {
                cc.setGioLamThem(Math.round((cc.getSoGioLam() - caLam.getSoGioChuan()) * 100.0) / 100.0);
            }
        }
        int rows = repository.updateChamCong(cc);
        if (rows <= 0) {
            return KetQua.error("Không thể lưu chấm công check-out. Vui lòng thử lại.");
        }
        return KetQua.success(cc, "Check-out thành công. Tổng giờ làm: "
                + String.format("%.2f", cc.getSoGioLam()) + "h.");
    }

    /**
     * Thêm chấm công thủ công (cho admin/HR).
     */
    public KetQua<ChamCong> themChamCongThuCong(String maNV, LocalDate ngay, String maCaLam,
                                                        LocalDateTime gioVao, LocalDateTime gioRa,
                                                        ChamCong.TrangThai trangThai, String ghiChu) {
        KetQua<Void> permission = validateAttendanceManagePermission();
        if (!permission.isSuccess()) return KetQua.error(permission.getMessage());
        if (ValidationUtils.isBlank(maNV)) {
            return KetQua.error("Mã nhân viên không hợp lệ.");
        }
        if (ngay == null) {
            return KetQua.error("Ngày chấm công không được để trống.");
        }
        if (gioVao == null || gioRa == null) {
            return KetQua.error("Giờ vào và giờ ra không được để trống.");
        }
        if (!gioRa.isAfter(gioVao)) {
            return KetQua.error("Giờ ra phải sau giờ vào.");
        }
        CaLam caLam = repository.findCaLamById(maCaLam);
        if (caLam == null) {
            return KetQua.error("Mã ca làm không hợp lệ.");
        }
        if (repository.findChamCongByNVAndNgay(maNV, ngay) != null) {
            return KetQua.error("Nhân viên đã có bản ghi chấm công ngày " + ngay + ".");
        }
        ChamCong cc = new ChamCong();
        cc.setMaNV(maNV);
        cc.setNgay(ngay);
        cc.setMaCaLam(maCaLam);
        cc.setTenCaLam(caLam.getTenCaLam());
        cc.setGioVao(gioVao);
        cc.setGioRa(gioRa);
        if (gioVao != null && gioRa != null) {
            cc.setSoGioLam(cc.tinhSoGioLam());
        }
        cc.setTrangThai(trangThai != null ? trangThai : ChamCong.TrangThai.DUNG_GIO);
        cc.setPhuongThucChamCong(ChamCong.PhuongThuc.THU_CONG);
        cc.setGhiChu(ghiChu);
        int id = repository.saveChamCong(cc);
        if (id <= 0) {
            return KetQua.error("Không thể lưu chấm công thủ công. Vui lòng thử lại.");
        }
        return KetQua.success(cc, "Đã thêm chấm công thủ công cho ngày " + ngay + ".");
    }

    public KetQua<ChamCong> suaChamCongThuCong(int maChamCong, LocalDate ngay, String maCaLam,
            LocalDateTime gioVao, LocalDateTime gioRa, ChamCong.TrangThai trangThai, String lyDoChinhSua) {
        KetQua<Void> permission = validateAttendanceManagePermission();
        if (!permission.isSuccess()) return KetQua.error(permission.getMessage());
        ChamCong cc = repository.findChamCongById(maChamCong);
        if (cc == null) {
            return KetQua.error("Không tìm thấy bản ghi chấm công cần sửa.");
        }
        if (ngay == null) {
            return KetQua.error("Ngày chấm công không được để trống.");
        }
        if (gioVao == null || gioRa == null) {
            return KetQua.error("Giờ vào và giờ ra không được để trống.");
        }
        if (!gioRa.isAfter(gioVao)) {
            return KetQua.error("Giờ ra phải sau giờ vào.");
        }
        if (ValidationUtils.isBlank(lyDoChinhSua)) {
            return KetQua.error("Vui lòng nhập lý do chỉnh sửa.");
        }
        CaLam caLam = repository.findCaLamById(maCaLam);
        if (caLam == null) {
            return KetQua.error("Mã ca làm không hợp lệ.");
        }
        ChamCong duplicate = repository.findChamCongByNVAndNgayExcludingId(cc.getMaNV(), ngay, maChamCong);
        if (duplicate != null) {
            return KetQua.error("Nhân viên đã có bản ghi chấm công khác trong ngày " + ngay + ".");
        }
        cc.setNgay(ngay);
        cc.setMaCaLam(maCaLam);
        cc.setTenCaLam(caLam.getTenCaLam());
        cc.setGioVao(gioVao);
        cc.setGioRa(gioRa);
        cc.setSoGioLam(cc.tinhSoGioLam());
        cc.setTrangThai(trangThai != null ? trangThai : ChamCong.TrangThai.DUNG_GIO);
        cc.setNguoiChinhSua(resolveCurrentEditorCode());
        cc.setLyDoChinhSua(lyDoChinhSua.trim());
        cc.setNgayChinhSua(LocalDateTime.now());
        int rows = repository.updateChamCongByManager(cc);
        if (rows <= 0) {
            return KetQua.error("Không thể cập nhật chấm công. Vui lòng thử lại.");
        }
        return KetQua.success(cc, "Đã cập nhật chấm công ngày " + ngay + ".");
    }

    public ChamCong getChamCongHomNay(String maNV) {
        if (!canViewAttendanceOf(maNV)) return null;
        return repository.findChamCongByNVAndNgay(maNV, LocalDate.now());
    }

    public List<ChamCong> getChamCongTheoThang(String maNV, int thang, int nam) {
        if (!canViewAttendanceOf(maNV)) return java.util.Collections.emptyList();
        return repository.findByNVAndThang(maNV, thang, nam);
    }

    public List<ChamCong> getChamCongTatCaTheoThang(int thang, int nam) {
        return repository.findByThang(thang, nam);
    }

    public List<ChamCong> getChamCongTatCaTheoThangByScope(int thang, int nam, String currentMaNV) {
        com.hrm.model.DataScope scope = getScopeForAction(ACTION_ATTENDANCE_VIEW);
        return repository.findByThangByScope(thang, nam, scope, currentMaNV);
    }

    public List<ChamCong> getLichSuChamCong(String maNV, LocalDate tu, LocalDate den) {
        if (!canViewAttendanceOf(maNV)) return java.util.Collections.emptyList();
        // Ước lượng bằng findByNVAndThang cho khoảng của tháng
        return repository.findByNVAndThang(maNV, tu.getMonthValue(), tu.getYear());
    }

    // =====================================================================
    // ĐĂNG KÝ LÀM THÊM (OT)
    // =====================================================================
    /**
     * Tạo đơn đăng ký làm thêm.
     * Validate: tổng OT trong tháng không vượt quá 40 giờ.
     */
    public KetQua<DangKyLamThem> taoDonLamThem(String maNV, LocalDate ngay, double soGio, String lyDo) {
        KetQua<Void> permission = validateSelfActionPermission(ACTION_OVERTIME_REQUEST, maNV);
        if (!permission.isSuccess()) return KetQua.error(permission.getMessage());
        KetQua<Void> overtimeValidation = validateOvertimeInput(soGio, lyDo);
        if (!overtimeValidation.isSuccess()) return KetQua.error(overtimeValidation.getMessage());
        // Kiểm tra tổng OT trong tháng
        double tongOTThang = repository.getTongGioOTDaDuyetTrongThang(maNV, ngay.getMonthValue(), ngay.getYear());
        if (tongOTThang + soGio > OT_TONG_TOI_DA_THANG) {
            double conLai = OT_TONG_TOI_DA_THANG - tongOTThang;
            return KetQua.error(String.format(
                    "Vượt giới hạn OT tháng (tối đa %g giờ). Còn có thể đăng ký: %.1f giờ.",
                    OT_TONG_TOI_DA_THANG, conLai));
        }
        DangKyLamThem dk = new DangKyLamThem(maNV, ngay, soGio, lyDo.trim());
        int id = repository.saveDangKyLamThem(dk);
        if (id <= 0) {
            return KetQua.error("Không thể tạo đơn đăng ký làm thêm. Vui lòng thử lại.");
        }
        return KetQua.success(dk, "Đã tạo đơn đăng ký làm thêm " + soGio + " giờ.");
    }

    /**
     * Duyệt đơn OT.
     */
    public KetQua<DangKyLamThem> duyetDonLamThem(int maDK, String nguoiDuyetId) {
        return processOTRequest(maDK, nguoiDuyetId, true);
    }

    /**
     * Từ chối đơn OT.
     */
    public KetQua<DangKyLamThem> tuChoiDonLamThem(int maDK, String nguoiDuyetId) {
        return processOTRequest(maDK, nguoiDuyetId, false);
    }

    public List<DangKyLamThem> getDonChoDuyetOTByScope(String currentMaNV) {
        com.hrm.model.DataScope scope = getScopeForAction(ACTION_OVERTIME_APPROVE);
        return repository.findChoDuyetOTByScope(scope, currentMaNV);
    }

    public List<DangKyLamThem> getDonLamThemCuaNV(String maNV) {
        return repository.findByMaNV(maNV);
    }

    // Legacy compat
    public KetQua<DangKyLamThem> duyetDonLamThem(int maDK, String nguoiDuyetId, double heSoOT) {
        if (heSoOT <= 0) {
            return KetQua.error("Hệ số OT phải lớn hơn 0.");
        }
        return processOTRequest(maDK, nguoiDuyetId, true, heSoOT);
    }

    /** Lấy đơn OT theo ID. */
    public DangKyLamThem getDonLamThemById(int maDK) {
        return repository.findById(maDK);
    }

    /** Get all OT requests (all statuses). */
    public List<DangKyLamThem> getDonLamThemTatCa() {
        return repository.findAllDangKyLamThem();
    }

    public List<DangKyLamThem> getDonLamThemTatCaByApproveScope(String currentMaNV) {
        com.hrm.model.DataScope scope = getScopeForAction(ACTION_OVERTIME_APPROVE);
        return repository.findAllDangKyLamThemByScope(scope, currentMaNV);
    }

    /** Update heSoOT for an OT request without approving. */
    public KetQua<Void> capNhatHeSoOT(int maDK, double heSo) {
        if (heSo <= 0) return KetQua.error("Hệ số OT phải lớn hơn 0.");
        DangKyLamThem dk = repository.findById(maDK);
        if (dk == null) return KetQua.error("Không tìm thấy đơn OT.");
        KetQua<Void> permission = validateOvertimeApprovePermission(dk.getMaNV());
        if (!permission.isSuccess()) return permission;
        int rows = repository.updateHeSoOT(maDK, heSo);
        if (rows <= 0) {
            return KetQua.error("Không thể cập nhật hệ số OT. Vui lòng thử lại.");
        }
        return KetQua.success(null, "Đã cập nhật hệ số OT thành x" + heSo + ".");
    }

    // =====================================================================
    // CẤU HÌNH PHỤ CẤP & KHẤU TRỪ
    // =====================================================================
    public List<CauHinhPhuCap> getAllCauHinhPC() {
        return repository.findAllCauHinhPC();
    }

    public CauHinhPhuCap getCauHinhPCById(int maPC) {
        return repository.findCauHinhPCById(maPC);
    }

    public KetQua<CauHinhPhuCap> themCauHinhPC(ThanhPhanLuong.Loai loai, String tenKhoan,
            CauHinhPhuCap.KieuTinh kieuTinh, double giaTri, String nguon) {
        KetQua<Void> permission = validateAllowanceManagePermission();
        if (!permission.isSuccess()) return KetQua.error(permission.getMessage());
        KetQua<Void> validation = validateKhoanLuongInput(tenKhoan, giaTri);
        if (!validation.isSuccess()) return KetQua.error(validation.getMessage());
        CauHinhPhuCap pc = new CauHinhPhuCap(loai, tenKhoan.trim(), kieuTinh, giaTri, nguon);
        int id = repository.insertCauHinhPC(pc);
        if (id <= 0) {
            return KetQua.error("Không thể thêm khoản phụ cấp. Vui lòng thử lại.");
        }
        return KetQua.success(pc, "Đã thêm khoản '" + tenKhoan + "'.");
    }

    public KetQua<CauHinhPhuCap> suaCauHinhPC(int maPC, ThanhPhanLuong.Loai loai, String tenKhoan,
            CauHinhPhuCap.KieuTinh kieuTinh, double giaTri, String nguon) {
        KetQua<Void> permission = validateAllowanceManagePermission();
        if (!permission.isSuccess()) return KetQua.error(permission.getMessage());
        CauHinhPhuCap pc = repository.findCauHinhPCById(maPC);
        if (pc == null) return KetQua.error("Không tìm thấy khoản phụ cấp.");
        KetQua<Void> validation = validateKhoanLuongInput(tenKhoan, giaTri);
        if (!validation.isSuccess()) return KetQua.error(validation.getMessage());
        pc.setLoai(loai);
        pc.setTenKhoan(tenKhoan.trim());
        pc.setKieuTinh(kieuTinh);
        pc.setGiaTri(giaTri);
        pc.setNguon(nguon);
        int rows = repository.updateCauHinhPC(pc);
        if (rows <= 0) {
            return KetQua.error("Không thể cập nhật khoản phụ cấp. Vui lòng thử lại.");
        }
        return KetQua.success(pc, "Đã cập nhật khoản '" + tenKhoan + "'.");
    }

    public KetQua<Void> xoaCauHinhPC(int maPC) {
        KetQua<Void> permission = validateAllowanceManagePermission();
        if (!permission.isSuccess()) return permission;
        CauHinhPhuCap pc = repository.findCauHinhPCById(maPC);
        if (pc == null) return KetQua.error("Không tìm thấy khoản phụ cấp.");
        int rows = repository.deactivateCauHinhPC(maPC);
        if (rows <= 0) {
            return KetQua.error("Không thể ngừng khoản phụ cấp. Vui lòng thử lại.");
        }
        return KetQua.success(null, "Đã ngừng khoản '" + pc.getTenKhoan() + "'.");
    }

    // =====================================================================
    // CHECK-IN TỰ ĐỘNG (từ chamcongnew)
    // =====================================================================
    /**
     * Check-in TỰ ĐỘNG — hệ thống tự nhận diện ca làm theo giờ hiện tại.
     * Logic: ưu tiên ca đang diễn ra (now trong [gioBD-30ph, gioKT]),
     *        fallback ca gần nhất trong vòng ±2h.
     */
    public KetQua<ChamCong> checkInAuto(String maNV, boolean laOT) {
        KetQua<Void> permission = validateCheckInOutPermission(ACTION_ATTENDANCE_CHECKIN, maNV);
        if (!permission.isSuccess()) return KetQua.error(permission.getMessage());
        if (ValidationUtils.isBlank(maNV)) return KetQua.error("Mã nhân viên không hợp lệ.");
        if (repository.findChamCongByNVAndNgay(maNV, LocalDate.now()) != null)
            return KetQua.error("Bạn đã check-in hôm nay rồi.");
        if (laOT && !coOTDaDuyetHomNay(maNV))
            return KetQua.error(
                "Bạn chưa có đơn OT được duyệt cho hôm nay. Không thể đánh dấu ca OT.");
        java.time.LocalTime nowTime = LocalDateTime.now().toLocalTime();
        List<CaLam> dsCaLam = repository.findActiveCaLam();
        if (dsCaLam.isEmpty())
            return KetQua.error("Không có ca làm nào đang hoạt động.");
        // Bước 1: ca đang trong cửa sổ [gioBD-30ph → gioKT]
        CaLam caLamPhuHop = dsCaLam.stream()
            .filter(ca -> trongCuaSoCheckIn(ca, nowTime))
            .min(java.util.Comparator.comparingLong(ca -> khoangCachDenGioBatDau(ca, nowTime)))
            .orElse(null);
        // Bước 2: fallback — ca gần nhất trong ±2h
        if (caLamPhuHop == null) {
            caLamPhuHop = dsCaLam.stream()
                .filter(ca -> khoangCachDenGioBatDau(ca, nowTime) <= 120)
                .min(java.util.Comparator.comparingLong(ca -> khoangCachDenGioBatDau(ca, nowTime)))
                .orElse(null);
        }
        if (caLamPhuHop == null)
            return KetQua.error("Không tìm thấy ca làm phù hợp với giờ hiện tại ("
                + nowTime.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")) + ").");
        ChamCong cc = taoBanGhiCheckIn(
                maNV,
                LocalDate.now(),
                caLamPhuHop,
                LocalDateTime.now(),
                ChamCong.PhuongThuc.THU_CONG,
                laOT);
        int id = repository.saveChamCong(cc);
        if (id <= 0) {
            return KetQua.error("Không thể lưu chấm công. Vui lòng thử lại.");
        }
        String otTag = laOT ? " [CA OT]" : "";
        return KetQua.success(cc, "Check-in thành công" + otTag + " — Ca: "
            + caLamPhuHop.getTenCaLam()
            + " (" + caLamPhuHop.getGioBatDau() + " - " + caLamPhuHop.getGioKetThuc() + ")");
    }

    private boolean trongCuaSoCheckIn(CaLam ca, java.time.LocalTime now) {
        java.time.LocalTime batDau = ca.getGioBatDau().minusMinutes(30);
        java.time.LocalTime ketThuc = ca.getGioKetThuc();
        if (!batDau.isAfter(ketThuc)) {
            return !now.isBefore(batDau) && !now.isAfter(ketThuc);
        } else {
            return !now.isBefore(batDau) || !now.isAfter(ketThuc);
        }
    }

    private long khoangCachDenGioBatDau(CaLam ca, java.time.LocalTime now) {
        long diff = java.time.Duration.between(now, ca.getGioBatDau()).toMinutes();
        if (diff > 720)  diff -= 1440;
        if (diff < -720) diff += 1440;
        return Math.abs(diff);
    }

    /** Trả về ca làm gợi ý theo giờ hiện tại (dùng để pre-select dropdown check-in). */
    public CaLam getCaLamGoiY() {
        LocalTime now = LocalTime.now();
        List<CaLam> ds = repository.findActiveCaLam();
        CaLam caGoiY = ds.stream()
            .filter(ca -> trongCuaSoCheckIn(ca, now))
            .min(java.util.Comparator.comparingLong(ca -> khoangCachDenGioBatDau(ca, now)))
            .orElse(null);
        if (caGoiY == null) {
            caGoiY = ds.stream()
                .filter(ca -> khoangCachDenGioBatDau(ca, now) <= 120)
                .min(java.util.Comparator.comparingLong(ca -> khoangCachDenGioBatDau(ca, now)))
                .orElse(null);
        }
        return caGoiY;
    }

    /** Kiểm tra nhân viên có đơn OT được duyệt cho hôm nay không. */
    public boolean coOTDaDuyetHomNay(String maNV) {
        return repository.coOTDaDuyetTheoNgay(maNV, LocalDate.now());
    }

    public String getMaNVByTaiKhoan(int maTaiKhoan) {
        return repository.getMaNVByTaiKhoan(maTaiKhoan);
    }

    public String getMaNhanVienById(String maNV) {
        return repository.getMaNhanVienById(maNV);
    }

    public ChamCongDAO.NhanVienInfo findNhanVienByMa(String maNV) {
        if (!canViewAttendanceOf(maNV)) return null;
        return repository.findNhanVienByMa(maNV);
    }

    public ChamCongDAO.NhanVienInfo findNhanVienByMaForManualAttendance(String maNV) {
        KetQua<Void> permission = validateAttendanceManagePermission();
        if (!permission.isSuccess()) return null;
        return repository.findNhanVienByMa(maNV);
    }

    // =====================================================================
    // ĐĂNG KÝ LÀM THÊM — overload với khoảng giờ
    // =====================================================================
    /** Tạo đơn OT theo khoảng giờ (gioVao → gioRa), tính soGio tự động. */
    public KetQua<DangKyLamThem> taoDonLamThem(String maNV, LocalDate ngay,
            LocalTime gioVao, LocalTime gioRa, String lyDo) {
        KetQua<Void> permission = validateSelfActionPermission(ACTION_OVERTIME_REQUEST, maNV);
        if (!permission.isSuccess()) return KetQua.error(permission.getMessage());
        if (gioVao == null || gioRa == null)
            return KetQua.error("Vui lòng nhập giờ bắt đầu và kết thúc OT.");
        double soGio = DangKyLamThem.tinhSoGioOT(gioVao, gioRa);
        KetQua<Void> overtimeValidation = validateOvertimeInput(soGio, lyDo);
        if (!overtimeValidation.isSuccess()) {
            if (overtimeValidation.getMessage().contains("Số giờ OT")) {
                return KetQua.error("Khoảng thời gian OT phải từ 0.5 đến 8 giờ.");
            }
            return KetQua.error(overtimeValidation.getMessage());
        }
        DangKyLamThem dk = new DangKyLamThem(maNV, ngay, gioVao, gioRa, lyDo.trim());
        int id = repository.saveDangKyLamThem(dk);
        if (id <= 0) {
            return KetQua.error("Không thể tạo đơn OT. Vui lòng thử lại.");
        }
        return KetQua.success(dk, "Đã tạo đơn OT: " + gioVao + " → " + gioRa
            + " (" + String.format("%.1f", soGio) + " giờ).");
    }

    /** Xóa đơn OT đang chờ duyệt. */
    public KetQua<Void> xoaDonLamThem(int maDK, String maNV) {
        DangKyLamThem dk = repository.findById(maDK);
        if (dk == null) return KetQua.error("Không tìm thấy đơn OT.");
        if (!dk.dangChoDuyet()) return KetQua.error("Chỉ xóa được đơn đang chờ duyệt.");
        KetQua<Void> permission = validateSelfActionPermission(ACTION_OVERTIME_REQUEST, maNV);
        if (!permission.isSuccess()) return permission;
        if (ValidationUtils.isBlank(maNV) || !maNV.equals(dk.getMaNV())) {
            return KetQua.error("Bạn không có quyền xóa đơn OT này.");
        }
        int rows = repository.deleteDangKyLamThem(maDK);
        if (rows <= 0) {
            return KetQua.error("Không thể xóa đơn OT. Vui lòng thử lại.");
        }
        return KetQua.success(null, "Đã xóa đơn OT.");
    }

    private KetQua<Void> validateOvertimeInput(double soGio, String lyDo) {
        if (soGio < 0.5 || soGio > 8) {
            return KetQua.error("Số giờ OT phải từ 0.5 đến 8 giờ.");
        }
        if (lyDo == null || lyDo.trim().isEmpty()) {
            return KetQua.error("Vui lòng nhập lý do làm thêm.");
        }
        return KetQua.success(null, "");
    }

    private KetQua<Void> validateKhoanLuongInput(String tenKhoan, double giaTri) {
        if (tenKhoan == null || tenKhoan.trim().isEmpty()) {
            return KetQua.error("Tên khoản không được để trống.");
        }
        if (giaTri <= 0) {
            return KetQua.error("Giá trị phải lớn hơn 0.");
        }
        return KetQua.success(null, "");
    }

    private KetQua<DangKyLamThem> processOTRequest(int maDK, String nguoiDuyetId, boolean approve) {
        return processOTRequest(maDK, nguoiDuyetId, approve, null);
    }

    private KetQua<DangKyLamThem> processOTRequest(int maDK, String nguoiDuyetId, boolean approve, Double heSoOT) {
        DangKyLamThem dk = repository.findById(maDK);
        KetQua<Void> requestValidation = validateOTRequestForProcessing(dk);
        if (!requestValidation.isSuccess()) return KetQua.error(requestValidation.getMessage());
        KetQua<Void> permission = validateOvertimeApprovePermission(dk.getMaNV());
        if (!permission.isSuccess()) return KetQua.error(permission.getMessage());
        if (SelfApprovalGuard.isSelfAction(nguoiDuyetId, dk.getMaNV())
                && !SelfApprovalGuard.currentUserCanBypassSelfRestriction()) {
            return KetQua.error(approve
                    ? "Bạn không thể tự duyệt đơn làm thêm của chính mình."
                    : "Bạn không thể tự từ chối đơn làm thêm của chính mình.");
        }
        if (approve && heSoOT != null) {
            int hesoRows = repository.updateHeSoOT(maDK, heSoOT);
            if (hesoRows <= 0) {
                System.err.println("Canh bao: Khong the cap nhat he so OT cho don " + maDK);
            }
            dk.setHeSoOT(heSoOT);
        }
        LocalDateTime now = LocalDateTime.now();
        int rows = repository.updateTrangThai(maDK, approve ? HRMConstants.TRANG_THAI_DA_DUYET : HRMConstants.TRANG_THAI_TU_CHOI, nguoiDuyetId, now);
        if (rows <= 0) {
            return KetQua.error(approve
                    ? "Không thể duyệt đơn làm thêm. Vui lòng thử lại."
                    : "Không thể từ chối đơn làm thêm. Vui lòng thử lại.");
        }
        dk.setTrangThai(approve ? DangKyLamThem.TrangThai.DA_DUYET : DangKyLamThem.TrangThai.TU_CHOI);
        dk.setNguoiDuyet(nguoiDuyetId);
        dk.setNgayDuyet(now);
        return KetQua.success(dk, approve ? "Đã duyệt đơn làm thêm." : "Đã từ chối đơn làm thêm.");
    }

    private ChamCong taoBanGhiCheckIn(String maNV, LocalDate ngay, CaLam caLam, LocalDateTime gioVao,
            ChamCong.PhuongThuc phuongThuc, boolean laOT) {
        ChamCong cc = new ChamCong();
        cc.setMaNV(maNV);
        cc.setNgay(ngay);
        cc.setMaCaLam(caLam.getId());
        cc.setTenCaLam(caLam.getTenCaLam());
        cc.setGioVao(gioVao);
        cc.setPhuongThucChamCong(phuongThuc);
        cc.setTrangThai(gioVao.toLocalTime().isAfter(caLam.getGioBatDau().plusMinutes(PHUT_DI_MUON))
                ? ChamCong.TrangThai.DI_MUON : ChamCong.TrangThai.DUNG_GIO);
        cc.setLaOT(laOT);
        return cc;
    }

    private ChamCong.PhuongThuc resolvePhuongThuc(String phuongThuc) {
        if (ValidationUtils.isBlank(phuongThuc)) return ChamCong.PhuongThuc.THU_CONG;
        try {
            return ChamCong.PhuongThuc.fromDbValue(phuongThuc);
        } catch (IllegalArgumentException ignored) {
            return ChamCong.PhuongThuc.THU_CONG;
        }
    }

    private KetQua<Void> validateOTRequestForProcessing(DangKyLamThem dk) {
        if (dk == null) {
            return KetQua.error("Không tìm thấy đơn OT.");
        }
        if (!dk.dangChoDuyet()) {
            return KetQua.error("Đơn đã được xử lý rồi.");
        }
        return KetQua.success(null, "");
    }

    private KetQua<Void> validateAttendanceManagePermission() {
        TaiKhoan currentUser = SessionContext.getInstance().getCurrentUser();
        if (currentUser == null) return KetQua.error("Bạn chưa đăng nhập.");
        if (SessionContext.getInstance().isAdmin()) {
            return KetQua.success(null, "");
        }
        if (!currentUser.coQuyen(ACTION_ATTENDANCE_MANAGE)) {
            return KetQua.error("Bạn không có quyền quản lý chấm công.");
        }
        return KetQua.success(null, "");
    }

    private KetQua<Void> validateAllowanceManagePermission() {
        TaiKhoan currentUser = SessionContext.getInstance().getCurrentUser();
        if (currentUser == null) return KetQua.error("Bạn chưa đăng nhập.");
        if (SessionContext.getInstance().isAdmin()) {
            return KetQua.success(null, "");
        }
        if (!currentUser.coQuyen(ACTION_ALLOWANCE_MANAGE)) {
            return KetQua.error("Bạn không có quyền quản lý phụ cấp và khấu trừ.");
        }
        return KetQua.success(null, "");
    }

    private KetQua<Void> validateSelfActionPermission(String action, String targetMaNV) {
        TaiKhoan currentUser = SessionContext.getInstance().getCurrentUser();
        if (currentUser == null) return KetQua.error("Bạn chưa đăng nhập.");
        if (SessionContext.getInstance().isAdmin()) {
            return KetQua.success(null, "");
        }
        if (!currentUser.coQuyen(action)) {
            return KetQua.error("Bạn không có quyền thực hiện thao tác này.");
        }
        DataScope scope = getScopeForAction(action);
        if (scope == DataScope.ALL) return KetQua.success(null, "");
        String currentMaNV = currentUser.getMaNV();
        if (scope == DataScope.SELF && !ValidationUtils.isBlank(currentMaNV) && currentMaNV.equals(targetMaNV)) {
            return KetQua.success(null, "");
        }
        return KetQua.error("Bạn chỉ được thao tác trên dữ liệu của chính mình.");
    }

    private KetQua<Void> validateCheckInOutPermission(String action, String targetMaNV) {
        KetQua<Void> selfPermission = validateSelfActionPermission(action, targetMaNV);
        if (selfPermission.isSuccess()) {
            return selfPermission;
        }
        KetQua<Void> managePermission = validateAttendanceManagePermission();
        if (managePermission.isSuccess()) {
            return KetQua.success(null, "");
        }
        return selfPermission;
    }

    private KetQua<Void> validateOvertimeApprovePermission(String targetMaNV) {
        TaiKhoan currentUser = SessionContext.getInstance().getCurrentUser();
        if (currentUser == null) return KetQua.error("Bạn chưa đăng nhập.");
        if (SessionContext.getInstance().isAdmin()) {
            return KetQua.success(null, "");
        }
        if (!currentUser.coQuyen(ACTION_OVERTIME_APPROVE)) {
            return KetQua.error("Bạn không có quyền duyệt đơn OT.");
        }
        DataScope scope = getScopeForAction(ACTION_OVERTIME_APPROVE);
        if (isScopeSufficient(scope, resolveTargetScope(targetMaNV))) {
            return KetQua.success(null, "");
        }
        return KetQua.error("Phạm vi quyền duyệt OT không đủ cho đơn này.");
    }

    private boolean canViewAttendanceOf(String targetMaNV) {
        TaiKhoan currentUser = SessionContext.getInstance().getCurrentUser();
        if (currentUser == null) return false;
        if (SessionContext.getInstance().isAdmin()) {
            return true;
        }
        if (!currentUser.coQuyen(ACTION_ATTENDANCE_VIEW)) {
            return false;
        }
        return isScopeSufficient(getScopeForAction(ACTION_ATTENDANCE_VIEW), resolveTargetScope(targetMaNV));
    }

    private DataScope resolveTargetScope(String targetMaNV) {
        TaiKhoan currentUser = SessionContext.getInstance().getCurrentUser();
        String currentMaNV = currentUser != null ? currentUser.getMaNV() : null;
        if (ValidationUtils.isBlank(currentMaNV) || ValidationUtils.isBlank(targetMaNV)) return DataScope.NONE;
        if (currentMaNV.equals(targetMaNV)) return DataScope.SELF;
        return repository.resolveScopeBetweenEmployees(currentMaNV, targetMaNV);
    }

    private boolean isScopeSufficient(DataScope grantedScope, DataScope requiredScope) {
        if (grantedScope == null || requiredScope == null) return false;
        if (grantedScope == DataScope.ALL) return requiredScope != DataScope.NONE;
        if (grantedScope == requiredScope) return true;
        if (grantedScope == DataScope.DEPT) {
            return requiredScope == DataScope.DEPT
                    || requiredScope == DataScope.TEAM
                    || requiredScope == DataScope.SELF;
        }
        return grantedScope == DataScope.TEAM
                && (requiredScope == DataScope.TEAM || requiredScope == DataScope.SELF);
    }

    private com.hrm.model.DataScope getScopeForAction(String action) {
        return XacThucBUS.getInstance().getScopeForAction(action);
    }

    private String resolveCurrentEditorCode() {
        TaiKhoan currentUser = SessionContext.getInstance().getCurrentUser();
        if (currentUser == null) return null;
        if (!ValidationUtils.isBlank(currentUser.getMaNV())) {
            return currentUser.getMaNV();
        }
        return currentUser.getTenDangNhap();
    }

}
