package com.hrm.bus;

import com.hrm.dao.BangLuongDAO;
import com.hrm.dao.NhanVienDAO;
import com.hrm.model.BangLuong;
import com.hrm.model.ChiTietLuong;
import com.hrm.model.NhanVien;
import com.hrm.model.ThanhPhanLuong;
import com.hrm.util.SessionContext;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class LuongBUS {

    private static final int NGAY_CONG_CHUAN = 26;

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

    public List<BangLuong> getAll() {
        return bangLuongRepo.findAll();
    }

    public BangLuong getBangLuongTheoKy(int thang, int nam) {
        return bangLuongRepo.findByThangNam(thang, nam);
    }

    public KetQua<BangLuong> tinhLuong(int thang, int nam) {
        if (thang < 1 || thang > 12) {
            return KetQua.error("Tháng không hợp lệ. Phải từ 1 đến 12.");
        }
        if (nam <= 2000) {
            return KetQua.error("Năm không hợp lệ. Phải lớn hơn 2000.");
        }
        if (!laKyLuongDaKetThuc(thang, nam)) {
            return KetQua.error("Chỉ được tính lương cho kỳ đã kết thúc.");
        }

        try {
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

            for (NhanVien nv : nvRepo.findDangLamViec()) {
                tinhLuongChoNhanVien(bangLuong.getMaBL(), thang, nam, nv, false);
            }

            return KetQua.success(bangLuong, "Tính lương tháng " + thang + "/" + nam + " thành công.");
        } catch (Exception e) {
            return KetQua.error("Lỗi khi tính lương: " + e.getMessage());
        }
    }

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

    public KetQua<Void> tinhLaiBangLuong(int maBangLuong) {
        try {
            BangLuong bl = bangLuongRepo.findById(maBangLuong);
            if (bl == null) {
                return KetQua.error("Không tìm thấy bảng lương #" + maBangLuong);
            }
            if (bl.getTrangThai() != BangLuong.TrangThai.DA_TINH) {
                return KetQua.error("Chỉ được tính lại khi bảng lương đang ở trạng thái 'Đã tính'.");
            }

            bangLuongRepo.deleteChiTietByBangLuong(maBangLuong);
            for (NhanVien nv : nvRepo.findDangLamViec()) {
                tinhLuongChoNhanVien(maBangLuong, bl.getThang(), bl.getNam(), nv, true);
            }
            return KetQua.success(null, "Đã tính lại toàn bộ bảng lương #" + maBangLuong);
        } catch (Exception e) {
            return KetQua.error("Lỗi khi tính lại bảng lương: " + e.getMessage());
        }
    }

    public KetQua<Void> tinhLaiChoNhanVien(int maBangLuong, String maNV) {
        try {
            BangLuong bl = bangLuongRepo.findById(maBangLuong);
            if (bl == null) {
                return KetQua.error("Không tìm thấy bảng lương #" + maBangLuong);
            }
            if (bl.getTrangThai() != BangLuong.TrangThai.DA_TINH) {
                return KetQua.error("Chỉ được tính lại khi bảng lương đang ở trạng thái 'Đã tính'.");
            }
            if (maNV == null || maNV.trim().isEmpty()) {
                return KetQua.error("Mã nhân viên không hợp lệ.");
            }

            NhanVien target = timNhanVienDangLamViec(maNV);
            if (target == null) {
                return KetQua.error("Không tìm thấy nhân viên đang làm việc: " + maNV);
            }

            bangLuongRepo.deleteChiTietByBangLuongAndNV(maBangLuong, maNV);
            tinhLuongChoNhanVien(maBangLuong, bl.getThang(), bl.getNam(), target, true);
            return KetQua.success(null, "Đã tính lại lương cho nhân viên " + maNV);
        } catch (Exception e) {
            return KetQua.error("Lỗi khi tính lại lương nhân viên: " + e.getMessage());
        }
    }

    public List<ChiTietLuong> getChiTiet(int maBangLuong) {
        BangLuong bangLuong = bangLuongRepo.findById(maBangLuong);
        List<ChiTietLuong> danhSach = bangLuongRepo.findByBangLuong(maBangLuong);
        if (bangLuong != null) {
            boSungDuLieuChamCong(bangLuong, danhSach);
        }
        return danhSach;
    }

    public ChiTietLuong getChiTietCaNhan(int maBangLuong, String maNV) {
        ChiTietLuong chiTiet = bangLuongRepo.findByBangLuongAndNV(maBangLuong, maNV);
        BangLuong bangLuong = bangLuongRepo.findById(maBangLuong);
        if (chiTiet != null && bangLuong != null) {
            chiTiet.setTongGioLam(bangLuongRepo.getTongGioLam(maNV, bangLuong.getThang(), bangLuong.getNam()));
        }
        return chiTiet;
    }

    public String getPhongBanCuaNV(String maNV) {
        return bangLuongRepo.getPhongBanCuaNV(maNV);
    }

    private NhanVien timNhanVienDangLamViec(String maNV) {
        for (NhanVien nv : nvRepo.findDangLamViec()) {
            if (maNV.equals(nv.getMaNhanVien())) {
                return nv;
            }
        }
        return null;
    }

    private void tinhLuongChoNhanVien(int maBL, int thang, int nam, NhanVien nv, boolean forceRecalculate) {
        String maNV = nv.getMaNhanVien();
        if (!forceRecalculate) {
            ChiTietLuong existing = bangLuongRepo.findByBangLuongAndNV(maBL, maNV);
            if (existing != null) {
                return;
            }
        }

        double luongCoBan = bangLuongRepo.getLuongCoSoFromHopDong(maNV);
        double tongLuongChucVu = bangLuongRepo.getTongLuongChucVu(maNV, luongCoBan);

        int tongBanGhiChamCong = bangLuongRepo.getTongBanGhiChamCong(maNV, thang, nam);
        int soNgayCong = bangLuongRepo.getSoNgayCong(maNV, thang, nam);
        if (tongBanGhiChamCong == 0) {
            soNgayCong = 0;
        }

        double luongCoBanThucTe = luongCoBan / NGAY_CONG_CHUAN * soNgayCong;
        double tongGioLam = bangLuongRepo.getTongGioLam(maNV, thang, nam);
        double tongGioOT = bangLuongRepo.getTongGioOT(maNV, thang, nam);
        double tienOT = bangLuongRepo.getTienOT(maNV, thang, nam, luongCoBan);

        List<Object[]> bonusCfgs = bangLuongRepo.getCauHinhPhuCapRaw();
        List<ThanhPhanLuong> bonusItems = new ArrayList<>();
        double tongBonus = 0;
        for (Object[] cfg : bonusCfgs) {
            String ten = (String) cfg[0];
            String kieu = (String) cfg[1];
            double gia = (Double) cfg[2];
            String nguon = (String) cfg[3];
            double amt = "phan_tram".equals(kieu) ? gia / 100.0 * luongCoBan : gia;
            tongBonus += amt;
            bonusItems.add(new ThanhPhanLuong(ThanhPhanLuong.Loai.PHU_CAP, ten, amt, nguon));
        }

        double tongThuNhap = luongCoBanThucTe + tongLuongChucVu + tienOT + tongBonus;
        double thueTNCN = tinhThueTNCN(tongThuNhap);
        double bhxh = luongCoBan * 0.08;
        double bhyt = luongCoBan * 0.015;
        double bhtn = luongCoBan * 0.01;
        double tongKhauTru = thueTNCN + bhxh + bhyt + bhtn;
        double luongThucNhan = tongThuNhap - tongKhauTru;

        ChiTietLuong ctl = new ChiTietLuong();
        ctl.setMaBL(maBL);
        ctl.setMaNV(maNV);
        ctl.setTenNV(nv.getHoTen() != null ? nv.getHoTen() : "");
        ctl.setLuongCoBan(luongCoBanThucTe);
        ctl.setTongLuongChucVu(tongLuongChucVu);
        ctl.setTienOT(tienOT);
        ctl.setTongLuong(tongThuNhap);
        ctl.setTongKhauTru(tongKhauTru);
        ctl.setLuongThucNhan(luongThucNhan);
        ctl.setSoNgayCong(soNgayCong);
        ctl.setTongGioLam(tongGioLam);
        ctl.setTongGioOT(tongGioOT);
        ctl.setTrangThai(ChiTietLuong.TrangThai.DA_TINH);

        for (ThanhPhanLuong bonus : bonusItems) {
            ctl.themThanhPhan(bonus);
        }

        if (soNgayCong < NGAY_CONG_CHUAN) {
            int soNgayVang = NGAY_CONG_CHUAN - soNgayCong;
            double tienTru = luongCoBan / NGAY_CONG_CHUAN * soNgayVang;
            ctl.themThanhPhan(new ThanhPhanLuong(
                    ThanhPhanLuong.Loai.KHAU_TRU,
                    "Trừ ngày vắng (" + soNgayVang + " ngày)",
                    tienTru,
                    "ChamCong"));
        }

        if (tienOT > 0) {
            ctl.themThanhPhan(new ThanhPhanLuong(
                    ThanhPhanLuong.Loai.PHU_CAP,
                    "Tiền làm thêm giờ (" + tongGioOT + "h)",
                    tienOT,
                    "DangKyLamThem"));
        }

        ctl.themThanhPhan(new ThanhPhanLuong(ThanhPhanLuong.Loai.KHAU_TRU, "Thuế TNCN", thueTNCN, "LuatThue"));
        ctl.themThanhPhan(new ThanhPhanLuong(ThanhPhanLuong.Loai.KHAU_TRU, "BHXH (8%)", bhxh, "LuatDinhBHXH"));
        ctl.themThanhPhan(new ThanhPhanLuong(ThanhPhanLuong.Loai.KHAU_TRU, "BHYT (1.5%)", bhyt, "LuatDinhBHYT"));
        ctl.themThanhPhan(new ThanhPhanLuong(ThanhPhanLuong.Loai.KHAU_TRU, "BHTN (1%)", bhtn, "LuatDinhBHTN"));

        int maChiTiet = bangLuongRepo.insertChiTiet(ctl);
        if (maChiTiet > 0) {
            for (ThanhPhanLuong tp : ctl.getDanhSachThanhPhan()) {
                tp.setMaCTLuong(maChiTiet);
            }
            bangLuongRepo.insertThanhPhanBatch(ctl.getDanhSachThanhPhan());
        }
    }

    private boolean laKyLuongDaKetThuc(int thang, int nam) {
        LocalDate homNay = LocalDate.now();
        LocalDate ngayCuoiKy = LocalDate.of(nam, thang, 1).withDayOfMonth(LocalDate.of(nam, thang, 1).lengthOfMonth());
        return ngayCuoiKy.isBefore(homNay);
    }

    private void boSungDuLieuChamCong(BangLuong bangLuong, List<ChiTietLuong> danhSach) {
        for (ChiTietLuong chiTiet : danhSach) {
            chiTiet.setTongGioLam(
                    bangLuongRepo.getTongGioLam(chiTiet.getMaNV(), bangLuong.getThang(), bangLuong.getNam()));
        }
    }

    private double tinhThueTNCN(double tongThuNhap) {
        final double GIAM_TRU_BAN_THAN = 11_000_000.0;
        double thuNhapChiuThue = tongThuNhap - GIAM_TRU_BAN_THAN;

        if (thuNhapChiuThue <= 0) {
            return 0.0;
        }

        double thue = 0.0;
        double bac1 = Math.min(thuNhapChiuThue, 5_000_000.0);
        thue += bac1 * 0.05;
        if (thuNhapChiuThue <= 5_000_000) {
            return thue;
        }

        double bac2 = Math.min(thuNhapChiuThue - 5_000_000, 5_000_000.0);
        thue += bac2 * 0.10;
        if (thuNhapChiuThue <= 10_000_000) {
            return thue;
        }

        double bac3 = Math.min(thuNhapChiuThue - 10_000_000, 8_000_000.0);
        thue += bac3 * 0.15;
        if (thuNhapChiuThue <= 18_000_000) {
            return thue;
        }

        double bac4 = Math.min(thuNhapChiuThue - 18_000_000, 14_000_000.0);
        thue += bac4 * 0.20;
        if (thuNhapChiuThue <= 32_000_000) {
            return thue;
        }

        double bac5 = Math.min(thuNhapChiuThue - 32_000_000, 20_000_000.0);
        thue += bac5 * 0.25;
        if (thuNhapChiuThue <= 52_000_000) {
            return thue;
        }

        double bac6 = Math.min(thuNhapChiuThue - 52_000_000, 28_000_000.0);
        thue += bac6 * 0.30;
        if (thuNhapChiuThue <= 80_000_000) {
            return thue;
        }

        double bac7 = thuNhapChiuThue - 80_000_000;
        thue += bac7 * 0.35;
        return thue;
    }
}
