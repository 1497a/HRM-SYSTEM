package com.hrm.bus;

import com.hrm.dao.TuyenDungDAO;
import com.hrm.model.DataScope;
import com.hrm.model.HopDongLaoDong;
import com.hrm.model.NhanVien;
import com.hrm.model.RecruitmentStatus;
import com.hrm.model.TaiKhoan;
import com.hrm.model.ThongTinCaNhan;
import com.hrm.model.TinTuyenDung;
import com.hrm.model.UngVien;
import com.hrm.model.YeuCauTuyenDung;
import com.hrm.util.HRMConstants;
import com.hrm.util.PermissionCodes;
import com.hrm.util.SessionContext;
import com.hrm.util.ValidationUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service quản lý tuyển dụng nhân sự.
 * Singleton pattern.
 */
public class TuyenDungBUS {

    static final String ACTION_RECRUITMENT_VIEW = PermissionCodes.RECRUITMENT_VIEW;
    static final String ACTION_RECRUITMENT_REQUEST = PermissionCodes.RECRUITMENT_REQUEST;
    static final String ACTION_RECRUITMENT_MANAGE = PermissionCodes.RECRUITMENT_MANAGE;
    static final String ACTION_RECRUITMENT_CANDIDATE_CREATE = PermissionCodes.RECRUITMENT_CANDIDATE_CREATE;
    static final String ACTION_RECRUITMENT_CANDIDATE_REVIEW = PermissionCodes.RECRUITMENT_CANDIDATE_REVIEW;
    static final String ACTION_RECRUITMENT_CANDIDATE_CONVERT = PermissionCodes.RECRUITMENT_CANDIDATE_CONVERT;
    static final String ACTION_RECRUITMENT_CANDIDATE_EDIT = PermissionCodes.RECRUITMENT_CANDIDATE_EDIT;
    private static TuyenDungBUS instance;
    private final TuyenDungDAO recruitmentRepo = TuyenDungDAO.getInstance();
    private TuyenDungBUS() {
    }

    public static synchronized TuyenDungBUS getInstance() {
        if (instance == null) {
            instance = new TuyenDungBUS();
        }
        return instance;
    }

    // ============================
    // Yêu cầu tuyển dụng
    // ============================
    public List<YeuCauTuyenDung> getAllYeuCau() {
        return recruitmentRepo.findAllYeuCauByScope(getScopeForAction(ACTION_RECRUITMENT_VIEW), getCurrentMaNV());
    }

    public List<YeuCauTuyenDung> getYeuCauDaDuyet() {
        return getAllYeuCau().stream()
                .filter(yc -> RecruitmentStatus.YeuCau.DA_DUYET.equals(yc.getTrangThai()))
                .collect(Collectors.toList());
    }

    public KetQua<YeuCauTuyenDung> taoYeuCau(YeuCauTuyenDung yc) {
        KetQua<Void> permission = requireAnyPermission(
                "Bạn không có quyền tạo yêu cầu tuyển dụng.",
                ACTION_RECRUITMENT_REQUEST,
                ACTION_RECRUITMENT_MANAGE
        );
        if (!permission.isSuccess()) {
            return KetQua.error(permission.getMessage());
        }
        if (ValidationUtils.isBlank(yc.getId())) {
            return KetQua.error("Phòng ban không được để trống.");
        }
        if (ValidationUtils.isBlank(yc.getMaChucVu())) {
            return KetQua.error("Chức vụ/vị trí tuyển dụng không được để trống.");
        }
        if (yc.getSoLuong() <= 0) {
            return KetQua.error("Số lượng tuyển dụng phải lớn hơn 0.");
        }
        if (yc.getHanTuyenDung() == null) {
            return KetQua.error("Hạn tuyển dụng không được để trống.");
        }
        if (yc.getHanTuyenDung().isBefore(LocalDate.now())) {
            return KetQua.error("Hạn tuyển dụng phải từ hôm nay trở đi.");
        }
        yc.setTrangThai(RecruitmentStatus.YeuCau.CHO_DUYET);
        try {
            int maYC = recruitmentRepo.insertYeuCau(yc);
            if (maYC <= 0) {
                return KetQua.error("Không thể tạo yêu cầu tuyển dụng. Vui lòng thử lại.");
            }
            yc.setMaYeuCau(maYC);
            return KetQua.success(yc, "Tạo yêu cầu tuyển dụng thành công. Đang chờ phê duyệt.");
        } catch (Exception e) {
            return KetQua.error("Lỗi tạo yêu cầu tuyển dụng: " + e.getMessage());
        }
    }

    public KetQua<Void> duyetYeuCau(int maYC) {
        KetQua<Void> permission = requirePermission(
                ACTION_RECRUITMENT_MANAGE,
                "Bạn không có quyền duyệt yêu cầu tuyển dụng."
        );
        if (!permission.isSuccess()) {
            return permission;
        }
        return updateYeuCauTrangThai(
                maYC,
                RecruitmentStatus.YeuCau.DA_DUYET,
                "Đã phê duyệt yêu cầu tuyển dụng #"
        );
    }

    public KetQua<Void> tuChoiYeuCau(int maYC) {
        KetQua<Void> permission = requirePermission(
                ACTION_RECRUITMENT_MANAGE,
                "Bạn không có quyền từ chối yêu cầu tuyển dụng."
        );
        if (!permission.isSuccess()) {
            return permission;
        }
        return updateYeuCauTrangThai(
                maYC,
                RecruitmentStatus.YeuCau.TU_CHOI,
                "Đã từ chối yêu cầu tuyển dụng #"
        );
    }

    // ============================
    // Tin tuyển dụng
    // ============================
    public List<TinTuyenDung> getAllTinTuyenDung() {
        return recruitmentRepo.findAllTinByScope(getScopeForAction(ACTION_RECRUITMENT_VIEW), getCurrentMaNV());
    }

    public KetQua<TinTuyenDung> taoTin(TinTuyenDung tin) {
        KetQua<Void> permission = requirePermission(
                ACTION_RECRUITMENT_CANDIDATE_REVIEW,
                "Bạn không có quyền tạo tin tuyển dụng."
        );
        if (!permission.isSuccess()) {
            return KetQua.error(permission.getMessage());
        }
        if (ValidationUtils.isBlank(tin.getTieuDe())) {
            return KetQua.error("Tiêu đề tin tuyển dụng không được để trống.");
        }
        if (tin.getMaYeuCau() <= 0) {
            return KetQua.error("Vui lòng chọn yêu cầu tuyển dụng liên kết.");
        }
        if (tin.getHanNopHoSo() == null) {
            return KetQua.error("Hạn nộp hồ sơ không được để trống.");
        }
        if (tin.getHanNopHoSo().isBefore(LocalDate.now())) {
            return KetQua.error("Hạn nộp hồ sơ phải từ hôm nay trở đi.");
        }
        String salaryErr = ValidationUtils.validateFreeText(tin.getMucLuong(), "Muc luong");
        if (salaryErr != null) return KetQua.error(salaryErr);
        String locationErr = ValidationUtils.validateFreeText(tin.getDiaDiem(), "Dia diem");
        if (locationErr != null) return KetQua.error(locationErr);
        tin.setTrangThai(RecruitmentStatus.Tin.DANG_TUYEN);
        tin.setNgayTao(LocalDate.now());
        try {
            int maTin = recruitmentRepo.insertTin(tin);
            if (maTin <= 0) {
                return KetQua.error("Không thể tạo tin tuyển dụng.");
            }
            tin.setMaTin(maTin);
            return KetQua.success(tin, "Tạo tin tuyển dụng thành công.");
        } catch (Exception e) {
            return KetQua.error("Lỗi tạo tin tuyển dụng: " + e.getMessage());
        }
    }

    public KetQua<Void> dongTin(int maTin) {
        KetQua<Void> permission = requirePermission(
                ACTION_RECRUITMENT_MANAGE,
                "B?n kh�ng c� quy?n d�ng tin tuy?n d?ng."
        );
        if (!permission.isSuccess()) {
            return permission;
        }
        TinTuyenDung tin = recruitmentRepo.findTinById(maTin);
        if (tin == null) {
            return KetQua.error("Không tìm thấy tin tuyển dụng #" + maTin);
        }
        if (RecruitmentStatus.Tin.DA_DONG.equals(tin.getTrangThai())) {
            return KetQua.error("Tin tuyển dụng đã được đóng.");
        }
        tin.setTrangThai(RecruitmentStatus.Tin.DA_DONG);
        try {
            int rows = recruitmentRepo.updateTin(tin);
            if (rows <= 0) {
                return KetQua.error("Không thể đóng tin tuyển dụng #" + maTin + ". Vui lòng thử lại.");
            }
            return KetQua.success(null, "Đã đóng tin tuyển dụng #" + maTin);
        } catch (Exception e) {
            return KetQua.error("Lỗi đóng tin tuyển dụng: " + e.getMessage());
        }
    }

    // ============================
    // Ứng viên
    // ============================
    public List<UngVien> getAllUngVien() {
        return recruitmentRepo.findAllUngVienByScope(getScopeForAction(ACTION_RECRUITMENT_VIEW), getCurrentMaNV());
    }

    public UngVien getUngVienById(int maUngVien) {
        return recruitmentRepo.findByIdInScope(maUngVien, getScopeForAction(ACTION_RECRUITMENT_VIEW), getCurrentMaNV());
    }

    public KetQua<UngVien> tiepNhanUngVien(UngVien uv) {
        KetQua<Void> permission = requirePermission(
                ACTION_RECRUITMENT_CANDIDATE_CREATE,
                "Bạn không có quyền tiếp nhận ứng viên."
        );
        if (!permission.isSuccess()) {
            return KetQua.error(permission.getMessage());
        }
        if (ValidationUtils.isBlank(uv.getHoTen())) {
            return KetQua.error("Họ tên ứng viên không được để trống.");
        }
        if (ValidationUtils.isBlank(uv.getEmail())) {
            return KetQua.error("Email ứng viên không được để trống.");
        }
        if (uv.getMaTin() <= 0) {
            return KetQua.error("Vui lòng chọn tin tuyển dụng liên kết.");
        }
        String emailErr = ValidationUtils.validateEmail(uv.getEmail());
        if (emailErr != null) return KetQua.error(emailErr);
        String phoneErr = ValidationUtils.validatePhone(uv.getDienThoai());
        if (phoneErr != null) return KetQua.error(phoneErr);
        if (uv.getNgaySinh() != null) {
            String birthErr = ValidationUtils.validateBirthDate(uv.getNgaySinh());
            if (birthErr != null) return KetQua.error(birthErr);
        }
        TinTuyenDung tin = recruitmentRepo.findTinById(uv.getMaTin());
        if (tin != null) {
            YeuCauTuyenDung yc = recruitmentRepo.findYeuCauById(tin.getMaYeuCau());
            if (yc != null) {
                if (RecruitmentStatus.YeuCau.DA_TUYEN_DU.equals(yc.getTrangThai())) {
                    return KetQua.error("Yêu cầu tuyển dụng đã tuyển đủ số lượng, không thể tiếp nhận thêm ứng viên.");
                }
                if (yc.getHanTuyenDung() != null && LocalDate.now().isAfter(yc.getHanTuyenDung())) {
                    return KetQua.error("Đã quá hạn tuyển dụng (" + yc.getHanTuyenDung() + "), không thể tiếp nhận thêm ứng viên.");
                }
            }
        }
        uv.setTrangThai(RecruitmentStatus.UngVien.MOI);
        uv.setNgayTao(LocalDate.now());
        try {
            int maUV = recruitmentRepo.insertUngVien(uv);
            if (maUV <= 0) {
                return KetQua.error("Không thể tiếp nhận ứng viên.");
            }
            uv.setMaUngVien(maUV);
            return KetQua.success(uv, "Tiếp nhận ứng viên thành công.");
        } catch (Exception e) {
            return KetQua.error("Lỗi tiếp nhận ứng viên: " + e.getMessage());
        }
    }

    public KetQua<UngVien> capNhatThongTinUV(UngVien uv) {
        KetQua<Void> permission = requirePermission(
                ACTION_RECRUITMENT_CANDIDATE_EDIT,
                "Bạn không có quyền chỉnh sửa thông tin ứng viên."
        );
        if (!permission.isSuccess()) {
            return KetQua.error(permission.getMessage());
        }
        UngVien existing = recruitmentRepo.findByIdInScope(uv.getMaUngVien(),
                getScopeForAction(ACTION_RECRUITMENT_CANDIDATE_EDIT), getCurrentMaNV());
        if (existing == null) {
            return KetQua.error("Không tìm thấy ứng viên #" + uv.getMaUngVien());
        }
        if (daChuyenThanhNhanVien(existing)) {
            return KetQua.error("Không thể sửa ứng viên đã chuyển thành nhân viên.");
        }
        if (ValidationUtils.isBlank(uv.getHoTen())) {
            return KetQua.error("Họ tên không được để trống.");
        }
        if (ValidationUtils.isBlank(uv.getEmail())) {
            return KetQua.error("Email không được để trống.");
        }
        String emailErr = ValidationUtils.validateEmail(uv.getEmail());
        if (emailErr != null) return KetQua.error(emailErr);
        if (ValidationUtils.isBlank(uv.getDienThoai())) {
            return KetQua.error("Số điện thoại không được để trống.");
        }
        String phoneErr = ValidationUtils.validatePhone(uv.getDienThoai());
        if (phoneErr != null) return KetQua.error(phoneErr);
        if (uv.getNgaySinh() == null) {
            return KetQua.error("Ngày sinh không được để trống.");
        }
        String birthErr = ValidationUtils.validateBirthDate(uv.getNgaySinh());
        if (birthErr != null) return KetQua.error(birthErr);
        if (ValidationUtils.isBlank(uv.getDiaChi())) {
            return KetQua.error("Địa chỉ không được để trống.");
        }
        if (ValidationUtils.isBlank(uv.getNguonUngTuyen())) {
            return KetQua.error("Nguồn ứng tuyển không được để trống.");
        }
        try {
            int rows = recruitmentRepo.updateUngVienInfo(uv);
            if (rows <= 0) {
                return KetQua.error("Không thể cập nhật thông tin ứng viên.");
            }
            return KetQua.success(uv, "Cập nhật thông tin ứng viên thành công.");
        } catch (Exception e) {
            return KetQua.error("Lỗi cập nhật: " + e.getMessage());
        }
    }

    public KetQua<Void> capNhatTrangThaiUV(int maUV, String trangThai) {
        KetQua<Void> permission = requirePermission(
                ACTION_RECRUITMENT_MANAGE,
                "Bạn không có quyền cập nhật trạng thái ứng viên."
        );
        if (!permission.isSuccess()) {
            return permission;
        }
        if (!RecruitmentStatus.isUngVienStatusEditable(trangThai)) {
            return KetQua.error("Trạng thái ứng viên không hợp lệ.");
        }
        UngVien uv = recruitmentRepo.findByIdInScope(maUV, getScopeForAction(ACTION_RECRUITMENT_CANDIDATE_REVIEW), getCurrentMaNV());
        if (uv == null) {
            return KetQua.error("Không tìm thấy ứng viên #" + maUV);
        }
        if (daChuyenThanhNhanVien(uv)) {
            return KetQua.error("Ứng viên đã được chuyển thành nhân viên, không thể cập nhật trạng thái.");
        }
        // Kiểm tra hạn mức khi đánh dấu "trúng_tuyển"
        if (RecruitmentStatus.UngVien.TRUNG_TUYEN.equals(trangThai)) {
            TinTuyenDung tin = recruitmentRepo.findTinById(uv.getMaTin());
            if (tin != null) {
                YeuCauTuyenDung yc = recruitmentRepo.findYeuCauById(tin.getMaYeuCau());
                if (yc != null) {
                    int daTrungTuyen = recruitmentRepo.countTrungTuyenByYeuCau(tin.getMaYeuCau());
                    if (daTrungTuyen >= yc.getSoLuong()) {
                        return KetQua.error("Đã đủ số lượng trúng tuyển (" + yc.getSoLuong()
                                + "). Không thể đánh dấu thêm ứng viên trúng tuyển.");
                    }
                }
            }
        }
        uv.setTrangThai(trangThai);
        try {
            int rows = recruitmentRepo.updateUngVien(uv);
            if (rows <= 0) {
                return KetQua.error("Không thể cập nhật trạng thái ứng viên #" + maUV + ". Vui lòng thử lại.");
            }
            return KetQua.success(null, "Cập nhật trạng thái ứng viên thành công.");
        } catch (Exception e) {
            return KetQua.error("Lỗi cập nhật trạng thái ứng viên: " + e.getMessage());
        }
    }

    public KetQua<String> taoThongDiepXacNhanChuyenUVThanhNV(int maUV) {
        KetQua<Void> permission = requirePermission(
                ACTION_RECRUITMENT_CANDIDATE_CONVERT,
                "Bạn không có quyền chuyển ứng viên thành nhân viên."
        );
        if (!permission.isSuccess()) {
            return KetQua.error(permission.getMessage());
        }
        KetQua<UngVien> checkResult = validateUngVienForConversion(maUV);
        if (!checkResult.isSuccess()) {
            return KetQua.error(checkResult.getMessage());
        }
        UngVien uv = checkResult.getData();
        TransferContext context = loadTransferContext(uv);
        String pbInfo = context.phongBanInfo;
        String cvInfo = context.chucVuInfo;
        boolean thieuThongTin = context.missingAppointmentInfo;
        String msg = "Chuyển \"" + uv.getHoTen() + "\" thành nhân viên chính thức?\n\n"
                + "Thông tin bổ nhiệm (sẽ tạo sau khi hợp đồng được phê duyệt):\n"
                + "  Phòng ban : " + pbInfo + "\n  Chức vụ   : " + cvInfo
                + (thieuThongTin ? "\n\nCHÚ Ý: Thiếu thông tin, bổ nhiệm sẽ cần tạo thủ công sau khi duyệt hợp đồng." : "");
        return KetQua.success(msg, "OK");
    }

    public KetQua<Void> chuyenUVThanhNV(int maUV) {
        KetQua<Void> permission = requirePermission(
                ACTION_RECRUITMENT_CANDIDATE_CONVERT,
                "Bạn không có quyền chuyển ứng viên thành nhân viên."
        );
        if (!permission.isSuccess()) {
            return KetQua.error(permission.getMessage());
        }
        KetQua<UngVien> checkResult = validateUngVienForConversion(maUV);
        if (!checkResult.isSuccess()) {
            return KetQua.error(checkResult.getMessage());
        }
        UngVien uv = checkResult.getData();
        try {
            KetQua<NhanVien> ketQuaNV = taoNhanVienTuUngVien(uv);
            if (!ketQuaNV.isSuccess()) {
                return KetQua.error("Lỗi tạo hồ sơ nhân viên: " + ketQuaNV.getMessage());
            }
            String maNV = ketQuaNV.getData().getMaNhanVien();
            TransferContext context = loadTransferContext(uv);
            String hopDongNote = tuDongTaoHopDong(maNV, context);
            capNhatUngVienDaChuyen(uv, maNV);
            capNhatTrangThaiYeuCauNeuDuSoLuong(context.tin);
            String thongTinTK = taoThongTinTaiKhoan(maNV);
            return KetQua.success(null,
                    "Chuyển ứng viên thành nhân viên thành công.\nMã NV: " + maNV + hopDongNote
                    + " | Bổ nhiệm: sẽ tự động tạo sau khi hợp đồng thử việc được phê duyệt"
                    + thongTinTK);
        } catch (Exception e) {
            return KetQua.error("Lỗi chuyển ứng viên thành nhân viên: " + e.getMessage());
        }
    }

    private KetQua<UngVien> validateUngVienForConversion(int maUV) {
        UngVien uv = recruitmentRepo.findByIdInScope(maUV, getScopeForAction(ACTION_RECRUITMENT_CANDIDATE_CONVERT), getCurrentMaNV());
        if (uv == null) {
            return KetQua.error("Không tìm thấy ứng viên #" + maUV);
        }
        if (daChuyenThanhNhanVien(uv)) {
            return KetQua.error("Ứng viên này đã được chuyển thành nhân viên (mã NV: " + uv.getMaNV() + ").");
        }
        if (!RecruitmentStatus.UngVien.TRUNG_TUYEN.equals(uv.getTrangThai())) {
            return KetQua.error("Chỉ có thể chuyển ứng viên trạng thái 'Trúng tuyển' thành nhân viên.");
        }
        TinTuyenDung tinConv = recruitmentRepo.findTinById(uv.getMaTin());
        if (tinConv != null) {
            YeuCauTuyenDung ycConv = recruitmentRepo.findYeuCauById(tinConv.getMaYeuCau());
            if (ycConv != null && RecruitmentStatus.YeuCau.DA_TUYEN_DU.equals(ycConv.getTrangThai())) {
            return KetQua.error("Yêu cầu tuyển dụng đã tuyển đủ số lượng, không thể chuyển thêm nhân viên.");
        }
        }
        return KetQua.success(uv, "OK");
    }

    private KetQua<NhanVien> taoNhanVienTuUngVien(UngVien uv) {
        NhanVien nv = new NhanVien();
        nv.setMaNhanVien(NhanVienBUS.getInstance().generateMaNhanVien());
        nv.setTrangThai(HRMConstants.TRANG_THAI_DANG_LAM_VIEC);
        nv.setNgayVaoLam(LocalDate.now());
        ThongTinCaNhan ttcn = new ThongTinCaNhan();
        ttcn.setHoTen(uv.getHoTen());
        ttcn.setEmail(uv.getEmail());
        ttcn.setDienThoai(uv.getDienThoai());
        ttcn.setNgaySinh(uv.getNgaySinh());
        ttcn.setGioiTinh(uv.getGioiTinh());
        ttcn.setDiaChi(uv.getDiaChi());
        ttcn.setTrinhDoHocVan(uv.getTrinhDoHocVan());
        ttcn.setKinhNghiem(uv.getKinhNghiem());
        ttcn.setFileCv(uv.getFileCv());
        return NhanVienBUS.getInstance().taoHoSo(nv, ttcn);
    }

    private String tuDongTaoHopDong(String maNV, TransferContext context) {
        long luong = 0;
        String ghiChu = "Tự động tạo khi chuyển ứng viên thành nhân viên.";
        if (context.tin != null && !ValidationUtils.isBlank(context.tin.getMucLuong())) {
            luong = parseMucLuong(context.tin.getMucLuong());
            if (luong > 0) {
                ghiChu += " Mức lương từ tin tuyển dụng: " + context.tin.getMucLuong();
            }
        } else if (context.yeuCau != null && !ValidationUtils.isBlank(context.yeuCau.getMucLuongDuKien())) {
            luong = parseMucLuong(context.yeuCau.getMucLuongDuKien());
            if (luong > 0) {
                ghiChu += " Mức lương dự kiến: " + context.yeuCau.getMucLuongDuKien();
            }
        }
        KetQua<HopDongLaoDong> kq = HopDongBUS.getInstance().taoHopDongSystem(maNV, luong, ghiChu);
        if (kq.isSuccess()) {
            return " | Hợp đồng thử việc đã tạo (chờ phê duyệt)"
                    + (luong > 0 ? ", lương: " + String.format("%,.0f", (double) luong) : ", lương: cần cập nhật");
        }
        return " | Tạo hợp đồng thất bại: " + kq.getMessage();
    }

    /** Trích số đầu tiên từ chuỗi mô tả mức lương. Nếu < 100_000 thì nhân triệu. */
    private long parseMucLuong(String mucLuong) {
        if (ValidationUtils.isBlank(mucLuong)) return 0;
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("[0-9]+([.,][0-9]+)?").matcher(mucLuong);
        if (!m.find()) return 0;
        try {
            String raw = m.group().replace(",", "").replace(".", "");
            long val = Long.parseLong(raw);
            // Nếu < 100_000 thì đơn vị là triệu đồng
            return val < 100_000 ? val * 1_000_000L : val;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void capNhatUngVienDaChuyen(UngVien uv, String maNV) {
        uv.setMaNV(maNV);
        uv.setTrangThai(RecruitmentStatus.UngVien.DA_CHUYEN_NHAN_VIEN);
        int rows = recruitmentRepo.updateUngVien(uv);
        if (rows <= 0) {
            System.err.println("Canh bao: Khong the cap nhat trang thai ung vien " + uv.getMaUngVien() + " sau khi chuyen thanh nhan vien.");
        }
    }

    private void capNhatTrangThaiYeuCauNeuDuSoLuong(TinTuyenDung tin) {
        if (tin == null) {
            return;
        }
        YeuCauTuyenDung yc = recruitmentRepo.findYeuCauById(tin.getMaYeuCau());
        if (yc == null || RecruitmentStatus.YeuCau.DA_TUYEN_DU.equals(yc.getTrangThai())) {
            return;
        }
        int daChuyen = recruitmentRepo.countConvertedByYeuCau(tin.getMaYeuCau());
        if (daChuyen >= yc.getSoLuong()) {
            int rows = recruitmentRepo.updateYeuCauTrangThai(tin.getMaYeuCau(), RecruitmentStatus.YeuCau.DA_TUYEN_DU, 0, null);
            if (rows <= 0) {
                System.err.println("Canh bao: Khong the cap nhat trang thai yeu cau tuyen dung " + tin.getMaYeuCau() + " thanh DA_TUYEN_DU.");
            }
        }
    }

    private String taoThongTinTaiKhoan(String maNV) {
        TaiKhoan tk = XacThucBUS.getInstance().findByMaNV(maNV);
        if (tk != null) {
            return " | Tài khoản: " + tk.getTenDangNhap();
        }
        return " | Tài khoản chưa được tạo";
    }

    private TransferContext loadTransferContext(UngVien uv) {
        TinTuyenDung tin = recruitmentRepo.findTinById(uv.getMaTin());
        YeuCauTuyenDung yc = tin != null ? recruitmentRepo.findYeuCauById(tin.getMaYeuCau()) : null;
        String pb = "(chưa xác định)";
        String cv = "(chưa xác định)";
        if (tin != null) {
            pb = ValidationUtils.isBlank(tin.getTenPhongBan()) ? pb : tin.getTenPhongBan();
            cv = ValidationUtils.isBlank(tin.getTenChucVu()) ? cv : tin.getTenChucVu();
        }
        if (yc != null) {
            pb = ValidationUtils.isBlank(pb) || "(chưa xác định)".equals(pb)
                    ? (ValidationUtils.isBlank(yc.getTenPhongBan()) ? pb : yc.getTenPhongBan())
                    : pb;
            cv = ValidationUtils.isBlank(cv) || "(chưa xác định)".equals(cv)
                    ? (ValidationUtils.isBlank(yc.getTenChucVu()) ? cv : yc.getTenChucVu())
                    : cv;
        }
        boolean missingInfo = "(chưa xác định)".equals(pb) || "(chưa xác định)".equals(cv);
        return new TransferContext(tin, yc, pb, cv, missingInfo);
    }

    private KetQua<Void> updateYeuCauTrangThai(int maYC, String trangThaiMoi, String successPrefix) {
        YeuCauTuyenDung yc = recruitmentRepo.findYeuCauById(maYC);
        if (yc == null) {
            return KetQua.error("Không tìm thấy yêu cầu tuyển dụng #" + maYC);
        }
        if (!RecruitmentStatus.YeuCau.CHO_DUYET.equals(yc.getTrangThai())) {
            return KetQua.error("Yêu cầu này không ở trạng thái chờ duyệt.");
        }
        try {
            int rows = recruitmentRepo.updateYeuCauTrangThai(maYC, trangThaiMoi, getCurrentUserId(), LocalDateTime.now());
            if (rows <= 0) {
                return KetQua.error("Không thể cập nhật trạng thái yêu cầu #" + maYC + ". Vui lòng thử lại.");
            }
        } catch (Exception e) {
            return KetQua.error("Lỗi cập nhật trạng thái yêu cầu: " + e.getMessage());
        }
        return KetQua.success(null, successPrefix + maYC);
    }

    private int getCurrentUserId() {
        return SessionContext.getInstance().getCurrentUser() != null
                ? SessionContext.getInstance().getCurrentUser().getId() : 0;
    }

    private String getCurrentMaNV() {
        return SessionContext.getInstance().getCurrentUser() != null
                ? SessionContext.getInstance().getCurrentUser().getMaNV() : null;
    }

    private DataScope getScopeForAction(String action) {
        return XacThucBUS.getInstance().getScopeForAction(action);
    }

    static boolean hasPermission(TaiKhoan user, String action) {
        return user != null && user.coQuyen(action);
    }

    static boolean hasAnyPermission(TaiKhoan user, String... actions) {
        if (user == null || actions == null) {
            return false;
        }
        for (String action : actions) {
            if (action != null && user.coQuyen(action)) {
                return true;
            }
        }
        return false;
    }

    private KetQua<Void> requirePermission(String action, String message) {
        TaiKhoan currentUser = SessionContext.getInstance().getCurrentUser();
        if (!hasPermission(currentUser, action)) {
            return KetQua.error(message);
        }
        return KetQua.success(null, "OK");
    }

    private KetQua<Void> requireAnyPermission(String message, String... actions) {
        TaiKhoan currentUser = SessionContext.getInstance().getCurrentUser();
        if (!hasAnyPermission(currentUser, actions)) {
            return KetQua.error(message);
        }
        return KetQua.success(null, "OK");
    }

    private boolean daChuyenThanhNhanVien(UngVien uv) {
        return uv.getMaNV() != null && !uv.getMaNV().trim().isEmpty();
    }

    private static class TransferContext {
        private final TinTuyenDung tin;
        private final YeuCauTuyenDung yeuCau;
        private final String phongBanInfo;
        private final String chucVuInfo;
        private final boolean missingAppointmentInfo;
        private TransferContext(TinTuyenDung tin, YeuCauTuyenDung yeuCau,
                String phongBanInfo, String chucVuInfo, boolean missingAppointmentInfo) {
            this.tin = tin;
            this.yeuCau = yeuCau;
            this.phongBanInfo = phongBanInfo;
            this.chucVuInfo = chucVuInfo;
            this.missingAppointmentInfo = missingAppointmentInfo;
        }
    }

}
