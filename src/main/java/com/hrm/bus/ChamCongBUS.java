package com.hrm.bus;

import com.hrm.model.*;
import com.hrm.dao.ChamCongDAO;
import com.hrm.model.CauHinhPhuCap;

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

    // Legacy: used by existing AttendancePanel
    public List<CaLam> getDanhSachCaLam() {
        return repository.findActiveCaLam();
    }

    public List<CaLam> getTatCaCaLam() {
        return repository.findAllCaLam();
    }

    /**
     * Lưu ca làm (thêm mới hoặc cập nhật).
     * Validate: maCaLam không rỗng, giờ hợp lệ.
     */
    public KetQua<CaLam> saveCaLam(CaLam caLam) {
        if (caLam.getMaCaLam() == null || caLam.getMaCaLam().trim().isEmpty()) {
            return KetQua.error("Mã ca làm không được để trống.");
        }
        if (caLam.getTenCaLam() == null || caLam.getTenCaLam().trim().isEmpty()) {
            return KetQua.error("Tên ca làm không được để trống.");
        }
        if (caLam.getGioBatDau() == null || caLam.getGioKetThuc() == null) {
            return KetQua.error("Giờ bắt đầu và kết thúc không được để trống.");
        }
        try {
            repository.saveCaLam(caLam);
            return KetQua.success(caLam, "Đã lưu ca làm '" + caLam.getTenCaLam() + "'.");
        } catch (Exception e) {
            return KetQua.error("Lỗi lưu ca làm: " + e.getMessage());
        }
    }

    // Legacy compat
    public KetQua<CaLam> themCaLam(String ma, String ten, String gioBD, String gioKT, double sg, boolean ot) {
        CaLam existing = repository.findCaLamById(ma.trim());
        if (existing != null) return KetQua.error("Mã ca đã tồn tại.");
        try {
            CaLam ca = new CaLam(ma.trim(), ten.trim(),
                    LocalTime.parse(gioBD.trim()), LocalTime.parse(gioKT.trim()));
            ca.setSoGioChuan(sg);
            ca.setChoPhepLamThem(ot);
            repository.saveCaLam(ca);
            return KetQua.success(ca, "Đã thêm ca làm '" + ten + "'.");
        } catch (Exception e) {
            return KetQua.error("Định dạng giờ không hợp lệ (HH:mm).");
        }
    }

    public KetQua<CaLam> suaCaLam(String ma, String ten, String gioBD, String gioKT, double sg, boolean ot) {
        CaLam ca = repository.findCaLamById(ma);
        if (ca == null) return KetQua.error("Không tìm thấy ca làm.");
        try {
            ca.setTenCaLam(ten.trim());
            ca.setGioBatDau(LocalTime.parse(gioBD.trim()));
            ca.setGioKetThuc(LocalTime.parse(gioKT.trim()));
            ca.setSoGioChuan(sg);
            ca.setChoPhepLamThem(ot);
            repository.saveCaLam(ca);
            return KetQua.success(ca, "Đã cập nhật ca làm.");
        } catch (Exception e) {
            return KetQua.error("Định dạng giờ không hợp lệ.");
        }
    }

    public KetQua<Void> xoaCaLam(String ma) {
        CaLam ca = repository.findCaLamById(ma);
        if (ca == null) return KetQua.error("Không tìm thấy ca làm.");
        repository.deleteCaLam(ma);
        return KetQua.success(null, "Đã ngừng ca làm '" + ca.getTenCaLam() + "'.");
    }

    // =====================================================================
    // CHẤM CÔNG
    // =====================================================================

    /**
     * Check-in: xác thực NV chưa check-in hôm nay, tính trạng thái đi muộn.
     */
    public KetQua<ChamCong> checkIn(int maNV, String maCaLam, String phuongThuc) {
        LocalDate today = LocalDate.now();
        if (repository.findChamCongByNVAndNgay(maNV, today) != null) {
            return KetQua.error("Bạn đã check-in hôm nay rồi.");
        }
        CaLam caLam = repository.findCaLamById(maCaLam);
        if (caLam == null) {
            return KetQua.error("Ca làm không hợp lệ.");
        }

        LocalDateTime now = LocalDateTime.now();
        ChamCong cc = new ChamCong();
        cc.setMaNV(maNV);
        cc.setNgay(today);
        cc.setMaCaLam(maCaLam);
        cc.setTenCaLam(caLam.getTenCaLam());
        cc.setGioVao(now);

        // Xác định phương thức chấm công
        ChamCong.PhuongThuc pt = ChamCong.PhuongThuc.THU_CONG;
        if (phuongThuc != null) {
            try {
                pt = ChamCong.PhuongThuc.fromDbValue(phuongThuc);
            } catch (IllegalArgumentException ignored) {}
        }
        cc.setPhuongThucChamCong(pt);

        // Xác định trạng thái: đi muộn nếu quá 15 phút
        LocalTime gioBatDau = caLam.getGioBatDau();
        LocalTime gioVaoActual = now.toLocalTime();
        if (gioVaoActual.isAfter(gioBatDau.plusMinutes(PHUT_DI_MUON))) {
            cc.setTrangThai(ChamCong.TrangThai.DI_MUON);
        } else {
            cc.setTrangThai(ChamCong.TrangThai.DUNG_GIO);
        }

        repository.saveChamCong(cc);
        return KetQua.success(cc, "Check-in thành công lúc " + now.toLocalTime().toString().substring(0, 5) + ".");
    }

    // Legacy: used by existing AttendancePanel
    public KetQua<ChamCong> checkIn(int maNV, String maCaLam) {
        return checkIn(maNV, maCaLam, "thu_cong");
    }

    /**
     * Check-out: tính soGioLam, gioLamThem nếu có OT đã duyệt.
     */
    public KetQua<ChamCong> checkOut(int maNV) {
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

        repository.updateChamCong(cc);
        return KetQua.success(cc, "Check-out thành công. Tổng giờ làm: "
                + String.format("%.2f", cc.getSoGioLam()) + "h.");
    }

    /**
     * Thêm chấm công thủ công (cho admin/HR).
     */
    public KetQua<ChamCong> themChamCongThuCong(int maNV, LocalDate ngay, String maCaLam,
                                                        LocalDateTime gioVao, LocalDateTime gioRa,
                                                        ChamCong.TrangThai trangThai, String ghiChu) {
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

        repository.saveChamCong(cc);
        return KetQua.success(cc, "Đã thêm chấm công thủ công cho ngày " + ngay + ".");
    }

    public ChamCong getChamCongHomNay(int maNV) {
        return repository.findChamCongByNVAndNgay(maNV, LocalDate.now());
    }

    public List<ChamCong> getChamCongTheoThang(int maNV, int thang, int nam) {
        return repository.findByNVAndThang(maNV, thang, nam);
    }

    public List<ChamCong> getChamCongTatCaTheoThang(int thang, int nam) {
        return repository.findByThang(thang, nam);
    }

    public List<ChamCong> getChamCongTatCaTheoThangByScope(int thang, int nam, int currentUserId) {
        com.hrm.model.DataScope scope = com.hrm.bus.XacThucBUS.getInstance().getScopeForAction("ATTENDANCE_VIEW");
        return repository.findByThangByScope(thang, nam, scope, currentUserId);
    }

    // Legacy compat
    public List<ChamCong> getChamCongTheoThang(int thang, int nam) {
        return repository.findByThang(thang, nam);
    }

    public List<ChamCong> getLichSuChamCong(int maNV, LocalDate tu, LocalDate den) {
        // Approximate using findByNVAndThang for the month range
        return repository.findByNVAndThang(maNV, tu.getMonthValue(), tu.getYear());
    }

    // =====================================================================
    // ĐĂNG KÝ LÀM THÊM (OT)
    // =====================================================================

    /**
     * Tạo đơn đăng ký làm thêm.
     * Validate: tổng OT trong tháng không vượt quá 40 giờ.
     */
    public KetQua<DangKyLamThem> taoDonLamThem(int maNV, LocalDate ngay, double soGio, String lyDo) {
        if (soGio <= 0 || soGio > 8) {
            return KetQua.error("Số giờ OT phải từ 0.5 đến 8 giờ.");
        }
        if (lyDo == null || lyDo.trim().isEmpty()) {
            return KetQua.error("Vui lòng nhập lý do làm thêm.");
        }

        // Kiểm tra tổng OT trong tháng
        double tongOTThang = repository.getTongGioOTDaDuyetTrongThang(maNV, ngay.getMonthValue(), ngay.getYear());
        if (tongOTThang + soGio > OT_TONG_TOI_DA_THANG) {
            double conLai = OT_TONG_TOI_DA_THANG - tongOTThang;
            return KetQua.error(String.format(
                    "Vượt giới hạn OT tháng (tối đa %g giờ). Còn có thể đăng ký: %.1f giờ.",
                    OT_TONG_TOI_DA_THANG, conLai));
        }

        DangKyLamThem dk = new DangKyLamThem(maNV, ngay, soGio, lyDo.trim());
        repository.saveDangKyLamThem(dk);
        return KetQua.success(dk, "Đã tạo đơn đăng ký làm thêm " + soGio + " giờ.");
    }

    /**
     * Duyệt đơn OT.
     */
    public KetQua<DangKyLamThem> duyetDonLamThem(int maDK, int nguoiDuyetId) {
        DangKyLamThem dk = repository.findById(maDK);
        if (dk == null) {
            return KetQua.error("Không tìm thấy đơn OT.");
        }
        if (!dk.dangChoDuyet()) {
            return KetQua.error("Đơn đã được xử lý rồi.");
        }
        LocalDateTime now = LocalDateTime.now();
        repository.updateTrangThai(maDK, "da_duyet", nguoiDuyetId, now);
        dk.setTrangThai(DangKyLamThem.TrangThai.DA_DUYET);
        dk.setNguoiDuyet(nguoiDuyetId);
        dk.setNgayDuyet(now);
        return KetQua.success(dk, "Đã duyệt đơn làm thêm.");
    }

    /**
     * Từ chối đơn OT.
     */
    public KetQua<DangKyLamThem> tuChoiDonLamThem(int maDK, int nguoiDuyetId) {
        DangKyLamThem dk = repository.findById(maDK);
        if (dk == null) {
            return KetQua.error("Không tìm thấy đơn OT.");
        }
        if (!dk.dangChoDuyet()) {
            return KetQua.error("Đơn đã được xử lý rồi.");
        }
        LocalDateTime now = LocalDateTime.now();
        repository.updateTrangThai(maDK, "tu_choi", nguoiDuyetId, now);
        dk.setTrangThai(DangKyLamThem.TrangThai.TU_CHOI);
        dk.setNguoiDuyet(nguoiDuyetId);
        dk.setNgayDuyet(now);
        return KetQua.success(dk, "Đã từ chối đơn làm thêm.");
    }

    public List<DangKyLamThem> getDonChoDuyet() {
        return repository.findChoDuyet();
    }

    public List<DangKyLamThem> getDonChoDuyetOTByScope(int currentUserId) {
        com.hrm.model.DataScope scope = com.hrm.bus.XacThucBUS.getInstance().getScopeForAction("ATTENDANCE_APPROVE");
        return repository.findChoDuyetOTByScope(scope, currentUserId);
    }

    // Legacy compat
    public List<DangKyLamThem> getDonChoQuanLyDuyet() {
        return repository.findChoDuyet();
    }

    public List<DangKyLamThem> getDonByMaNV(int maNV) {
        return repository.findByMaNV(maNV);
    }

    public List<DangKyLamThem> getDonLamThemCuaNV(int maNV) {
        return repository.findByMaNV(maNV);
    }

    // Legacy compat
    public KetQua<DangKyLamThem> duyetDonLamThem(int maDK, int nguoiDuyetId, double heSoOT) {
        return duyetDonLamThem(maDK, nguoiDuyetId);
    }

    /** Get all OT requests (all statuses). */
    public List<DangKyLamThem> getDonLamThemTatCa() {
        return repository.findAllDangKyLamThem();
    }

    /** Get all OT requests by scope. */
    public List<DangKyLamThem> getDonLamThemTatCaByScope(int currentUserId) {
        com.hrm.model.DataScope scope = com.hrm.bus.XacThucBUS.getInstance().getScopeForAction("ATTENDANCE_VIEW");
        return repository.findAllDangKyLamThemByScope(scope, currentUserId);
    }

    /** Update heSoOT for an OT request without approving. */
    public KetQua<Void> capNhatHeSoOT(int maDK, double heSo) {
        if (heSo <= 0) return KetQua.error("Hệ số OT phải lớn hơn 0.");
        DangKyLamThem dk = repository.findById(maDK);
        if (dk == null) return KetQua.error("Không tìm thấy đơn OT.");
        repository.updateHeSoOT(maDK, heSo);
        return KetQua.success(null, "Đã cập nhật hệ số OT thành x" + heSo + ".");
    }

    // =====================================================================
    // CẤU HÌNH PHỤ CẤP & KHẤU TRỪ
    // =====================================================================

    public List<CauHinhPhuCap> getAllCauHinhPC() {
        return repository.findAllCauHinhPC();
    }

    public KetQua<CauHinhPhuCap> themCauHinhPC(ThanhPhanLuong.Loai loai, String tenKhoan,
            CauHinhPhuCap.KieuTinh kieuTinh, double giaTri, String nguon) {
        if (tenKhoan == null || tenKhoan.trim().isEmpty()) {
            return KetQua.error("Tên khoản không được để trống.");
        }
        if (giaTri <= 0) return KetQua.error("Giá trị phải lớn hơn 0.");
        CauHinhPhuCap pc = new CauHinhPhuCap(loai, tenKhoan.trim(), kieuTinh, giaTri, nguon);
        repository.insertCauHinhPC(pc);
        return KetQua.success(pc, "Đã thêm khoản '" + tenKhoan + "'.");
    }

    public KetQua<CauHinhPhuCap> suaCauHinhPC(int maPC, ThanhPhanLuong.Loai loai, String tenKhoan,
            CauHinhPhuCap.KieuTinh kieuTinh, double giaTri, String nguon) {
        CauHinhPhuCap pc = repository.findCauHinhPCById(maPC);
        if (pc == null) return KetQua.error("Không tìm thấy khoản phụ cấp.");
        if (tenKhoan == null || tenKhoan.trim().isEmpty()) {
            return KetQua.error("Tên khoản không được để trống.");
        }
        if (giaTri <= 0) return KetQua.error("Giá trị phải lớn hơn 0.");
        pc.setLoai(loai);
        pc.setTenKhoan(tenKhoan.trim());
        pc.setKieuTinh(kieuTinh);
        pc.setGiaTri(giaTri);
        pc.setNguon(nguon);
        repository.updateCauHinhPC(pc);
        return KetQua.success(pc, "Đã cập nhật khoản '" + tenKhoan + "'.");
    }

    public KetQua<Void> xoaCauHinhPC(int maPC) {
        CauHinhPhuCap pc = repository.findCauHinhPCById(maPC);
        if (pc == null) return KetQua.error("Không tìm thấy khoản phụ cấp.");
        repository.deactivateCauHinhPC(maPC);
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
    public KetQua<ChamCong> checkInAuto(int maNV, boolean laOT) {
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

        LocalDateTime now = LocalDateTime.now();
        ChamCong cc = new ChamCong();
        cc.setMaNV(maNV);
        cc.setNgay(LocalDate.now());
        cc.setMaCaLam(caLamPhuHop.getMaCaLam());
        cc.setTenCaLam(caLamPhuHop.getTenCaLam());
        cc.setGioVao(now);
        cc.setPhuongThucChamCong(ChamCong.PhuongThuc.THU_CONG);
        cc.setTrangThai(
            now.toLocalTime().isAfter(caLamPhuHop.getGioBatDau().plusMinutes(PHUT_DI_MUON))
                ? ChamCong.TrangThai.DI_MUON : ChamCong.TrangThai.DUNG_GIO);
        cc.setLaOT(laOT);
        repository.saveChamCong(cc);
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

    /** Kiểm tra nhân viên có đơn OT được duyệt cho hôm nay không. */
    public boolean coOTDaDuyetHomNay(int maNV) {
        return repository.coOTDaDuyetTheoNgay(maNV, LocalDate.now());
    }

    // =====================================================================
    // ĐĂNG KÝ LÀM THÊM — overload với khoảng giờ
    // =====================================================================

    /** Tạo đơn OT theo khoảng giờ (gioVao → gioRa), tính soGio tự động. */
    public KetQua<DangKyLamThem> taoDonLamThem(int maNV, LocalDate ngay,
            LocalTime gioVao, LocalTime gioRa, String lyDo) {
        if (gioVao == null || gioRa == null)
            return KetQua.error("Vui lòng nhập giờ bắt đầu và kết thúc OT.");
        if (lyDo == null || lyDo.trim().isEmpty())
            return KetQua.error("Vui lòng nhập lý do làm thêm.");
        double soGio = DangKyLamThem.tinhSoGioOT(gioVao, gioRa);
        if (soGio < 0.5 || soGio > 8)
            return KetQua.error("Khoảng thời gian OT phải từ 0.5 đến 8 giờ.");
        DangKyLamThem dk = new DangKyLamThem(maNV, ngay, gioVao, gioRa, lyDo.trim());
        repository.saveDangKyLamThem(dk);
        return KetQua.success(dk, "Đã tạo đơn OT: " + gioVao + " → " + gioRa
            + " (" + String.format("%.1f", soGio) + " giờ).");
    }

    /** Xóa đơn OT đang chờ duyệt. */
    public KetQua<Void> xoaDonLamThem(int maDK, int maNV) {
        DangKyLamThem dk = repository.findById(maDK);
        if (dk == null) return KetQua.error("Không tìm thấy đơn OT.");
        if (!dk.dangChoDuyet()) return KetQua.error("Chỉ xóa được đơn đang chờ duyệt.");
        repository.deleteDangKyLamThem(maDK);
        return KetQua.success(null, "Đã xóa đơn OT.");
    }
}
