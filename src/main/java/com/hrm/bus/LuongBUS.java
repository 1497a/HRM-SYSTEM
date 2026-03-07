package com.hrm.bus;

import com.hrm.model.*;
import com.hrm.dao.BangLuongDAO;
import com.hrm.dao.NhanVienDAO;
import com.hrm.util.SessionContext;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Service quản lý lương nhân viên.
 * Singleton pattern.
 * Đã cập nhật để maNV là String (ví dụ: "NV001", "NV002", ...).
 */
public class LuongBUS {

    private static LuongBUS instance;

    private final BangLuongDAO bangLuongRepo = BangLuongDAO.getInstance();
    private final NhanVienDAO nvRepo = NhanVienDAO.getInstance();

    private LuongBUS() {
    }

    public static synchronized LuongBUS getInstance() {
        if (instance == null) {
            instance = new LuongBUS();
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
    public KetQua<BangLuong> tinhLuong(int thang, int nam) {
        // Validate tháng/năm
        if (thang < 1 || thang > 12) {
            return KetQua.error("Tháng không hợp lệ. Phải từ 1 đến 12.");
        }
        if (nam <= 2000) {
            return KetQua.error("Năm không hợp lệ. Phải lớn hơn 2000.");
        }

        try {
            // Lấy hoặc tạo bảng lương
            BangLuong bangLuong = bangLuongRepo.findByThangNam(thang, nam);
            if (bangLuong == null) {
                bangLuong = new BangLuong();
                LocalDate ngayBD = LocalDate.of(nam, thang, 1);
                LocalDate ngayKT = ngayBD.withDayOfMonth(ngayBD.lengthOfMonth());
                bangLuong.setThang(thang);
                bangLuong.setNam(nam);
                bangLuong.setNgayBatDau(ngayBD);
                bangLuong.setNgayKetThuc(ngayKT);
                bangLuong.setTrangThai(BangLuong.TrangThai.DA_TINH);
                bangLuongRepo.insertBangLuong(bangLuong);
            }

            final int maBL = bangLuong.getMaBL();

            // Lấy danh sách nhân viên đang làm việc
            List<NhanVien> danhSachNV = nvRepo.findDangLamViec();

            for (NhanVien nv : danhSachNV) {
                String maNV = nv.getMaNhanVien(); // String

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

                // Phụ cấp từ CAUHINHPHUCAP
                List<Object[]> bonusCfgs = bangLuongRepo.getCauHinhPhuCapRaw();
                List<ThanhPhanLuong> bonusItems = new ArrayList<>();
                double tongBonus = 0;
                for (Object[] cfg : bonusCfgs) {
                    String ten  = (String) cfg[0];
                    String kieu = (String) cfg[1];
                    double gia  = (Double) cfg[2];
                    String ng   = (String) cfg[3];
                    double amt  = "phan_tram".equals(kieu) ? gia / 100.0 * luongCoBan : gia;
                    tongBonus += amt;
                    bonusItems.add(new ThanhPhanLuong(ThanhPhanLuong.Loai.PHU_CAP, ten, amt, ng));
                }

                // Tổng thu nhập
                double tongThuNhap = luongCoBan + tongLuongChucVu + tienOT + tongBonus;

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
                ctl.setMaNV(maNV); // String
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

                // Thêm phụ cấp
                for (ThanhPhanLuong bonus : bonusItems) {
                    ctl.themThanhPhan(bonus);
                }

                // Thêm các khoản khấu trừ
                ctl.themThanhPhan(new ThanhPhanLuong(ThanhPhanLuong.Loai.KHAU_TRU, "Thuế TNCN", thueTNCN, "LuatThue"));
                ctl.themThanhPhan(new ThanhPhanLuong(ThanhPhanLuong.Loai.KHAU_TRU, "BHXH (8%)", bhxh, "LuatDinhBHXH"));
                ctl.themThanhPhan(new ThanhPhanLuong(ThanhPhanLuong.Loai.KHAU_TRU, "BHYT (1.5%)", bhyt, "LuatDinhBHYT"));
                ctl.themThanhPhan(new ThanhPhanLuong(ThanhPhanLuong.Loai.KHAU_TRU, "BHTN (1%)", bhtn, "LuatDinhBHTN"));

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

            return KetQua.success(bangLuong, "Tính lương tháng " + thang + "/" + nam + " thành công.");

        } catch (Exception e) {
            return KetQua.error("Lỗi khi tính lương: " + e.getMessage());
        }
    }

    // ============================
    // duyetBangLuong
    // ============================

    public KetQua<Void> duyetBangLuong(int maBangLuong) {
        try {
            BangLuong bl = bangLuongRepo.findById(maBangLuong);
            if (bl == null) {
                return KetQua.error("Không tìm thấy bảng lương #" + maBangLuong);
            }
            if (bl.getTrangThai() != BangLuong.TrangThai.DA_TINH) {
                return KetQua.error("Chỉ có thể duyệt bảng lương ở trạng thái 'Đã tính'.");
            }

            int userId = SessionContext.getInstance().getCurrentUser() != null
                    ? SessionContext.getInstance().getCurrentUser().getId() : 0;

            bangLuongRepo.approveBangLuong(maBangLuong, userId);
            return KetQua.success(null, "Đã duyệt bảng lương #" + maBangLuong);
        } catch (Exception e) {
            return KetQua.error("Lỗi khi duyệt bảng lương: " + e.getMessage());
        }
    }

    // ============================
    // khoaBangLuong
    // ============================

    public KetQua<Void> khoaBangLuong(int maBangLuong) {
        try {
            BangLuong bl = bangLuongRepo.findById(maBangLuong);
            if (bl == null) {
                return KetQua.error("Không tìm thấy bảng lương #" + maBangLuong);
            }
            if (bl.getTrangThai() != BangLuong.TrangThai.DA_DUYET) {
                return KetQua.error("Cần duyệt bảng lương trước khi khóa.");
            }

            int userId = SessionContext.getInstance().getCurrentUser() != null
                    ? SessionContext.getInstance().getCurrentUser().getId() : 0;

            bangLuongRepo.lockBangLuong(maBangLuong, userId);
            return KetQua.success(null, "Đã khóa bảng lương #" + maBangLuong);
        } catch (Exception e) {
            return KetQua.error("Lỗi khi khóa bảng lương: " + e.getMessage());
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

    private double tinhThueTNCN(double tongThuNhap) {
        final double GIAM_TRU_BAN_THAN = 11_000_000.0;
        double thuNhapChiuThue = tongThuNhap - GIAM_TRU_BAN_THAN;

        if (thuNhapChiuThue <= 0) return 0.0;

        double thue = 0.0;

        // Bậc 1: 0 - 5 triệu (5%)
        double bac1 = Math.min(thuNhapChiuThue, 5_000_000.0);
        thue += bac1 * 0.05;
        if (thuNhapChiuThue <= 5_000_000) return thue;

        // Bậc 2: 5 - 10 triệu (10%)
        double bac2 = Math.min(thuNhapChiuThue - 5_000_000, 5_000_000.0);
        thue += bac2 * 0.10;
        if (thuNhapChiuThue <= 10_000_000) return thue;

        // Bậc 3: 10 - 18 triệu (15%)
        double bac3 = Math.min(thuNhapChiuThue - 10_000_000, 8_000_000.0);
        thue += bac3 * 0.15;
        if (thuNhapChiuThue <= 18_000_000) return thue;

        // Bậc 4: 18 - 32 triệu (20%)
        double bac4 = Math.min(thuNhapChiuThue - 18_000_000, 14_000_000.0);
        thue += bac4 * 0.20;
        if (thuNhapChiuThue <= 32_000_000) return thue;

        // Bậc 5: 32 - 52 triệu (25%)
        double bac5 = Math.min(thuNhapChiuThue - 32_000_000, 20_000_000.0);
        thue += bac5 * 0.25;
        if (thuNhapChiuThue <= 52_000_000) return thue;

        // Bậc 6: 52 - 80 triệu (30%)
        double bac6 = Math.min(thuNhapChiuThue - 52_000_000, 28_000_000.0);
        thue += bac6 * 0.30;
        if (thuNhapChiuThue <= 80_000_000) return thue;

        // Bậc 7: Trên 80 triệu (35%)
        double bac7 = thuNhapChiuThue - 80_000_000;
        thue += bac7 * 0.35;

        return thue;
    }
}