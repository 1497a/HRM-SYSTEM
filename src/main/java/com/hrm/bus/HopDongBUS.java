package com.hrm.bus;

import com.hrm.model.HopDongLaoDong;
import com.hrm.model.NhanVien;
import com.hrm.dao.BoNhiemDAO;
import com.hrm.dao.HopDongDAO;
import com.hrm.dao.NhanVienDAO;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Service quản lý hợp đồng lao động.
 * Singleton pattern.
 */
public class HopDongBUS {
    private static final String LOAI_THU_VIEC = "thu_viec";
    private static final String LOAI_XAC_DINH_THOI_HAN = "xac_dinh_thoi_han";
    private static final String TRANG_THAI_HIEU_LUC = "hieu_luc";
    private static final String TRANG_THAI_THANH_LY = "thanh_ly";

    private static HopDongBUS instance;

    private final HopDongDAO hopDongRepo = HopDongDAO.getInstance();
    private final NhanVienDAO nvRepo = NhanVienDAO.getInstance();

    private HopDongBUS() {
    }

    public static synchronized HopDongBUS getInstance() {
        if (instance == null) {
            instance = new HopDongBUS();
        }
        return instance;
    }

    // ============================
    // taoHopDong
    // ============================

    public KetQua<HopDongLaoDong> taoHopDong(HopDongLaoDong hd) {
        if (SelfApprovalGuard.isSelfAction(getCurrentUserNhanVienId(), hd.getMaNV())
                && !SelfApprovalGuard.currentUserCanBypassSelfRestriction()) {
            return KetQua.error("Bạn không thể tự ký hợp đồng cho chính mình.");
        }

        // Validate nhân viên
        NhanVien nv = nvRepo.findByMaNhanVien(hd.getMaNV());
        if (nv == null) {
            return KetQua.error("Không tìm thấy nhân viên.");
        }

        // Tự động tạo số hợp đồng
        hd.setSoHopDong(generateSoHopDong());

        KetQua<Void> dateValidation = validateNgayKyVaNgayHieuLuc(hd);
        if (!dateValidation.isSuccess()) return KetQua.error(dateValidation.getMessage());

        // Validate theo loại hợp đồng
        KetQua<Void> typeValidation = validateTheoLoaiHopDong(hd);
        if (!typeValidation.isSuccess()) return KetQua.error(typeValidation.getMessage());
        // khong_xac_dinh: ngayHetHieuLuc có thể null

        // Kiểm tra nhân viên đã có hợp đồng hiệu lực chưa
        HopDongLaoDong existing = hopDongRepo.findHieuLuc(hd.getMaNV());
        if (existing != null) {
            return KetQua.error("Nhân viên đã có hợp đồng đang hiệu lực (số HĐ: "
                    + existing.getSoHopDong() + "). Hãy thanh lý hợp đồng cũ trước.");
        }

        // Thiết lập trạng thái
        hd.setTrangThai(TRANG_THAI_HIEU_LUC);

        try {
            hopDongRepo.insert(hd);

            // Đồng bộ loại hợp đồng lên NHANVIEN
            nv.setLoaiHopDong(hd.getLoaiHopDong());
            nvRepo.update(nv);

            return KetQua.success(hd, "Tạo hợp đồng lao động thành công.");
        } catch (Exception e) {
            return KetQua.error("Lỗi tạo hợp đồng: " + e.getMessage());
        }
    }

    // ============================
    // thanhLyHopDong
    // ============================

    public KetQua<Void> thanhLyHopDong(int maHopDong) {
        HopDongLaoDong hd = findById(maHopDong);
        if (hd == null) {
            return KetQua.error("Khong tim thay hop dong.");
        }
        if (TRANG_THAI_THANH_LY.equals(hd.getTrangThai())) {
            return KetQua.error("Hop dong da duoc thanh ly.");
        }
        if ("het_han".equals(hd.getTrangThai())) {
            return KetQua.error("Khong the thanh ly hop dong da het han. Chi thanh ly hop dong dang hieu luc.");
        }
        hopDongRepo.updateTrangThai(maHopDong, TRANG_THAI_THANH_LY);
        // Ket thuc tat ca bo nhiem hieu luc cua nhan vien
        if (hd.getMaNV() != null) {
            BoNhiemDAO.getInstance().endAllActiveBoNhiemForNV(hd.getMaNV(), LocalDate.now());
        }
        return KetQua.success(null, "Thanh ly hop dong thanh cong. Da ket thuc cac bo nhiem hieu luc.");
    }

    /**
     * Kiem tra hop dong co the thanh ly khong (dung de canh bao UI).
     * Tra ve true neu hop dong da het han.
     */
    public boolean isHopDongHetHan(int maHopDong) {
        HopDongLaoDong hd = findById(maHopDong);
        return hd != null && "het_han".equals(hd.getTrangThai());
    }



    // ============================
    // Getters
    // ============================

    public List<HopDongLaoDong> getSapHetHan(int soNgay) {
        return hopDongRepo.findSapHetHan(soNgay);
    }

    public List<HopDongLaoDong> getByMaNV(String maNV) {
        return hopDongRepo.findByMaNV(maNV);
    }

    public List<HopDongLaoDong> getAll() {
        hopDongRepo.expireHetHanContracts();
        return hopDongRepo.findAll();
    }

    public HopDongLaoDong getHieuLuc(String maNV) {
        hopDongRepo.expireHetHanContracts();
        return hopDongRepo.findHieuLuc(maNV);
    }

    // ============================
    // Private helper
    // ============================

    private HopDongLaoDong findById(int maHopDong) {
        for (HopDongLaoDong hd : hopDongRepo.findAll()) {
            if (hd.getMaHopDong() == maHopDong) {
                return hd;
            }
        }
        return null;
    }

    private KetQua<Void> validateNgayKyVaNgayHieuLuc(HopDongLaoDong hd) {
        if (hd.getNgayKy() == null) {
            return KetQua.error("Ngày ký không được để trống.");
        }
        if (hd.getNgayHieuLuc() == null) {
            return KetQua.error("Ngày hiệu lực không được để trống.");
        }
        if (hd.getNgayHieuLuc().isBefore(hd.getNgayKy())) {
            return KetQua.error("Ngày hiệu lực phải bằng hoặc sau ngày ký.");
        }
        return KetQua.success(null, "");
    }

    private KetQua<Void> validateTheoLoaiHopDong(HopDongLaoDong hd) {
        String loai = hd.getLoaiHopDong();
        if (LOAI_THU_VIEC.equals(loai)) {
            return validateHopDongThuViec(hd);
        }
        if (LOAI_XAC_DINH_THOI_HAN.equals(loai)) {
            return validateHopDongXacDinhThoiHan(hd);
        }
        return KetQua.success(null, "");
    }

    private KetQua<Void> validateHopDongThuViec(HopDongLaoDong hd) {
        if (hd.getNgayHetHieuLuc() == null) {
            return KetQua.error("Hợp đồng thử việc phải có ngày hết hiệu lực.");
        }
        long soNgay = ChronoUnit.DAYS.between(hd.getNgayHieuLuc(), hd.getNgayHetHieuLuc());
        if (soNgay > 60) {
            return KetQua.error("Hợp đồng thử việc không được vượt quá 60 ngày.");
        }
        if (soNgay <= 0) {
            return KetQua.error("Ngày hết hiệu lực phải sau ngày hiệu lực.");
        }
        return KetQua.success(null, "");
    }

    private KetQua<Void> validateHopDongXacDinhThoiHan(HopDongLaoDong hd) {
        if (hd.getNgayHetHieuLuc() == null) {
            return KetQua.error("Hợp đồng xác định thời hạn phải có ngày hết hiệu lực.");
        }
        long soThang = ChronoUnit.MONTHS.between(hd.getNgayHieuLuc(), hd.getNgayHetHieuLuc());
        if (soThang > 36) {
            return KetQua.error("Hợp đồng xác định thời hạn không được vượt quá 36 tháng.");
        }
        if (hd.getNgayHetHieuLuc().isBefore(hd.getNgayHieuLuc())) {
            return KetQua.error("Ngày hết hiệu lực phải sau ngày hiệu lực.");
        }
        return KetQua.success(null, "");
    }

    private String getCurrentUserNhanVienId() {
        com.hrm.model.TaiKhoan currentUser = com.hrm.util.SessionContext.getInstance().getCurrentUser();
        return currentUser != null ? currentUser.getNhanVienId() : null;
    }

    private String generateSoHopDong() {
        LocalDate today = LocalDate.now();
        int seq = hopDongRepo.countByYearMonth(today.getYear(), today.getMonthValue()) + 1;
        return String.format("HD-%04d%02d-%04d", today.getYear(), today.getMonthValue(), seq);
    }
}
