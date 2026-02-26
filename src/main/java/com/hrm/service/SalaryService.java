package com.hrm.service;

import com.hrm.model.BangLuong;
import com.hrm.model.ChiTietLuong;
import com.hrm.model.NhanVien;
import com.hrm.model.ThanhPhanLuong;
import com.hrm.repo.BangLuongRepository;
import com.hrm.repo.NhanVienRepository;
import com.hrm.util.SessionContext;

import java.time.LocalDate;
import java.util.List;

/**
 * Service quản lý lương nhân viên.
 * Singleton pattern.
 */
public class SalaryService {

    private static SalaryService instance;

    private final BangLuongRepository bangLuongRepo = BangLuongRepository.getInstance();
    private final NhanVienRepository nvRepo = NhanVienRepository.getInstance();

    private SalaryService() {
    }

    public static synchronized SalaryService getInstance() {
        if (instance == null) {
            instance = new SalaryService();
        }
        return instance;
    }

    // ============================
    // getAll
    // ============================

    public List<BangLuong> getAll() {
        return bangLuongRepo.findAll();
    }

    // ============================
    // tinhLuong
    // ============================

    /**
     * Tính lương cho tháng/năm. Nếu bảng lương đã tồn tại thì dùng lại,
     * nếu chưa thì tạo mới. Tính chi tiết lương cho từng nhân viên đang làm việc.
     */
    public ServiceResult<BangLuong> tinhLuong(int thang, int nam) {
        // Validate tháng
        if (thang < 1 || thang > 12) {
            return ServiceResult.error("Tháng không hợp lệ. Phải từ 1 đến 12.");
        }
        // Validate năm
        if (nam <= 2000) {
            return ServiceResult.error("Năm không hợp lệ. Phải lớn hơn 2000.");
        }

        try {
            // Lấy hoặc tạo bảng lương
            BangLuong bangLuong = bangLuongRepo.findByThangNam(thang, nam);
            if (bangLuong == null) {
                bangLuong = new BangLuong();
                LocalDate ngayBD = LocalDate.of(nam, thang, 1);
                LocalDate ngayKT = ngayBD.withDayOfMonth(ngayBD.lengthOfMonth());
                bangLuong.setNgayBD(ngayBD);
                bangLuong.setNgayKT(ngayKT);
                bangLuong.setTrangThai(BangLuong.TrangThai.DA_TINH);
                bangLuongRepo.insertBangLuong(bangLuong);
            }

            final int maBL = bangLuong.getMaBL();

            // Lấy danh sách nhân viên đang làm việc
            List<NhanVien> danhSachNV = nvRepo.findDangLamViec();

            for (NhanVien nv : danhSachNV) {
                int maNV = nv.getId();

                // Kiểm tra đã có chi tiết lương chưa (tránh tính lại)
                ChiTietLuong existing = bangLuongRepo.findByBangLuongAndNV(maBL, maNV);
                if (existing != null) {
                    continue;
                }

                // Lấy lương cơ bản từ hợp đồng hiệu lực
                double luongCoBan = bangLuongRepo.getLuongCoSoFromHopDong(maNV);

                // Tính lương chức vụ từ bổ nhiệm hiệu lực
                double tongLuongChucVu = bangLuongRepo.getTongLuongChucVu(maNV, luongCoBan);

                // Số ngày công (mặc định 22 ngày/tháng)
                int soNgayCong = 22;

                // Tiền OT (đơn giản hoá: 0)
                double tienOT = 0.0;

                // Tổng thu nhập
                double tongThuNhap = luongCoBan + tongLuongChucVu + tienOT;

                // Tính thuế TNCN luỹ tiến
                double thueTNCN = tinhThueTNCN(tongThuNhap);

                // Các khoản bảo hiểm tính trên lương cơ bản
                double bhxh = luongCoBan * 0.08;   // BHXH 8%
                double bhyt = luongCoBan * 0.015;  // BHYT 1.5%
                double bhtn = luongCoBan * 0.01;   // BHTN 1%

                double tongKhauTru = thueTNCN + bhxh + bhyt + bhtn;
                double luongThucNhan = tongThuNhap - tongKhauTru;

                // Tạo ChiTietLuong
                ChiTietLuong ctl = new ChiTietLuong();
                ctl.setMaBL(maBL);
                ctl.setMaNV(maNV);
                ctl.setTenNV(nv.getHoTen() != null ? nv.getHoTen() : "");
                ctl.setLuongCoBan(luongCoBan);
                ctl.setTongLuongChucVu(tongLuongChucVu);
                ctl.setTienOT(tienOT);
                ctl.setTongLuong(tongThuNhap);
                ctl.setTongKhauTru(tongKhauTru);
                ctl.setLuongThucNhan(luongThucNhan);
                ctl.setSoNgayCong(soNgayCong);
                ctl.setTongGioOT(0);
                ctl.setTrangThai(ChiTietLuong.TrangThai.DA_TINH);

                // Thêm các thành phần lương chi tiết vào danh sách
                ctl.themThanhPhan(new ThanhPhanLuong(
                        ThanhPhanLuong.Loai.KHAU_TRU, "Thuế TNCN", thueTNCN, "LuatThue"));
                ctl.themThanhPhan(new ThanhPhanLuong(
                        ThanhPhanLuong.Loai.KHAU_TRU, "BHXH (8%)", bhxh, "LuatDinhBHXH"));
                ctl.themThanhPhan(new ThanhPhanLuong(
                        ThanhPhanLuong.Loai.KHAU_TRU, "BHYT (1.5%)", bhyt, "LuatDinhBHYT"));
                ctl.themThanhPhan(new ThanhPhanLuong(
                        ThanhPhanLuong.Loai.KHAU_TRU, "BHTN (1%)", bhtn, "LuatDinhBHTN"));

                // Lưu chi tiết lương
                int maChiTiet = bangLuongRepo.insertChiTiet(ctl);

                // Lưu thành phần lương
                if (maChiTiet > 0) {
                    for (ThanhPhanLuong tp : ctl.getDanhSachThanhPhan()) {
                        tp.setMaCTLuong(maChiTiet);
                    }
                    bangLuongRepo.insertThanhPhanBatch(ctl.getDanhSachThanhPhan());
                }
            }

            return ServiceResult.success(bangLuong, "Tính lương tháng " + thang + "/" + nam + " thành công.");

        } catch (Exception e) {
            return ServiceResult.error("Lỗi khi tính lương: " + e.getMessage());
        }
    }

    // ============================
    // khoaBangLuong
    // ============================

    public ServiceResult<Void> khoaBangLuong(int maBangLuong) {
        try {
            int userId = 0;
            if (SessionContext.getInstance().getCurrentUser() != null) {
                userId = SessionContext.getInstance().getCurrentUser().getId();
            }
            bangLuongRepo.lockBangLuong(maBangLuong, userId);
            return ServiceResult.success(null, "Khóa bảng lương thành công.");
        } catch (Exception e) {
            return ServiceResult.error("Lỗi khi khóa bảng lương: " + e.getMessage());
        }
    }

    // ============================
    // getChiTiet
    // ============================

    public List<ChiTietLuong> getChiTiet(int maBangLuong) {
        return bangLuongRepo.findByBangLuong(maBangLuong);
    }

    // ============================
    // Private helper: tính thuế TNCN luỹ tiến
    // ============================

    /**
     * Tính thuế thu nhập cá nhân theo biểu thuế luỹ tiến.
     * Giảm trừ bản thân: 11,000,000 đồng/tháng.
     * Bậc thuế:
     *   1. 0   - 5,000,000:    5%
     *   2. 5M  - 10,000,000:  10%
     *   3. 10M - 18,000,000:  15%
     *   4. 18M - 32,000,000:  20%
     *   5. 32M - 52,000,000:  25%
     *   6. 52M - 80,000,000:  30%
     *   7. Trên 80,000,000:   35%
     */
    private double tinhThueTNCN(double tongThuNhap) {
        final double GIAM_TRU_BAN_THAN = 11_000_000.0;
        double thuNhapChiuThue = tongThuNhap - GIAM_TRU_BAN_THAN;

        if (thuNhapChiuThue <= 0) {
            return 0.0;
        }

        double thue = 0.0;
        // Bậc 1: 0 - 5,000,000 (5%)
        double bac1 = Math.min(thuNhapChiuThue, 5_000_000.0);
        thue += bac1 * 0.05;
        if (thuNhapChiuThue <= 5_000_000) return thue;

        // Bậc 2: 5,000,000 - 10,000,000 (10%)
        double bac2 = Math.min(thuNhapChiuThue - 5_000_000, 5_000_000.0);
        thue += bac2 * 0.10;
        if (thuNhapChiuThue <= 10_000_000) return thue;

        // Bậc 3: 10,000,000 - 18,000,000 (15%)
        double bac3 = Math.min(thuNhapChiuThue - 10_000_000, 8_000_000.0);
        thue += bac3 * 0.15;
        if (thuNhapChiuThue <= 18_000_000) return thue;

        // Bậc 4: 18,000,000 - 32,000,000 (20%)
        double bac4 = Math.min(thuNhapChiuThue - 18_000_000, 14_000_000.0);
        thue += bac4 * 0.20;
        if (thuNhapChiuThue <= 32_000_000) return thue;

        // Bậc 5: 32,000,000 - 52,000,000 (25%)
        double bac5 = Math.min(thuNhapChiuThue - 32_000_000, 20_000_000.0);
        thue += bac5 * 0.25;
        if (thuNhapChiuThue <= 52_000_000) return thue;

        // Bậc 6: 52,000,000 - 80,000,000 (30%)
        double bac6 = Math.min(thuNhapChiuThue - 52_000_000, 28_000_000.0);
        thue += bac6 * 0.30;
        if (thuNhapChiuThue <= 80_000_000) return thue;

        // Bậc 7: Trên 80,000,000 (35%)
        double bac7 = thuNhapChiuThue - 80_000_000;
        thue += bac7 * 0.35;

        return thue;
    }
}
