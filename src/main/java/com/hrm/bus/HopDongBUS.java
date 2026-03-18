package com.hrm.bus;

import com.hrm.dao.BoNhiemDAO;
import com.hrm.dao.HopDongDAO;
import com.hrm.dao.NhanVienDAO;
import com.hrm.model.DataScope;
import com.hrm.model.HopDongLaoDong;
import com.hrm.model.NhanVien;
import com.hrm.model.TaiKhoan;
import com.hrm.util.HRMConstants;
import com.hrm.util.PermissionCodes;
import com.hrm.util.SessionContext;
import com.hrm.util.ValidationUtils;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class HopDongBUS {

    private static final String ACTION_CONTRACT_CREATE  = PermissionCodes.CONTRACT_CREATE;
    private static final String ACTION_CONTRACT_APPROVE = PermissionCodes.CONTRACT_APPROVE;
    private static final String ACTION_CONTRACT_MANAGE  = PermissionCodes.CONTRACT_MANAGE;
    private static final String LOAI_THU_VIEC = "thu_viec";
    private static final String LOAI_XAC_DINH_THOI_HAN = "xac_dinh_thoi_han";
    private static final String LOAI_KHONG_XAC_DINH_THOI_HAN = "khong_xac_dinh";
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

    /**
     * Tạo hợp đồng thử việc nội bộ (dùng khi chuyển ứng viên thành NV).
     * Bỏ qua permission check vì đây là thao tác hệ thống.
     */
    public KetQua<HopDongLaoDong> taoHopDongSystem(String maNV, long luongCoSo, String ghiChu) {
        NhanVien nv = nvRepo.findByMaNhanVien(maNV);
        if (nv == null) {
            return KetQua.error("Khong tim thay nhan vien " + maNV);
        }
        HopDongLaoDong existing = hopDongRepo.findHieuLuc(maNV);
        if (existing != null) {
            return KetQua.error("Nhan vien da co hop dong hieu luc.");
        }
        HopDongLaoDong pending = hopDongRepo.findChoDuyet(maNV);
        if (pending != null) {
            return KetQua.error("Nhan vien da co hop dong cho duyet.");
        }
        HopDongLaoDong hd = new HopDongLaoDong();
        hd.setMaNV(maNV);
        hd.setSoHopDong(generateSoHopDong());
        hd.setLoaiHopDong(LOAI_THU_VIEC);
        hd.setLuongCoSo(luongCoSo);
        hd.setNgayKy(LocalDate.now());
        hd.setNgayHieuLuc(LocalDate.now());
        hd.setNgayHetHieuLuc(LocalDate.now().plusDays(60));
        hd.setTrangThai(HRMConstants.TRANG_THAI_CHO_DUYET);
        hd.setGhiChu(ghiChu);
        try {
            hopDongRepo.insert(hd);
            return KetQua.success(hd, "Tao hop dong thu viec thanh cong (cho phe duyet).");
        } catch (Exception e) {
            return KetQua.error("Loi tao hop dong tu dong: " + e.getMessage());
        }
    }

    public KetQua<HopDongLaoDong> taoHopDong(HopDongLaoDong hd) {
        if (hd == null || ValidationUtils.isBlank(hd.getMaNV())) {
            return KetQua.error("Nhan vien hop dong khong hop le.");
        }
        KetQua<Void> permission = validateContractPermission(ACTION_CONTRACT_CREATE, hd.getMaNV());
        if (!permission.isSuccess()) {
            return KetQua.error(permission.getMessage());
        }
        if (SelfApprovalGuard.isSelfAction(getCurrentUserNhanVienId(), hd.getMaNV())
                && !SelfApprovalGuard.currentUserCanBypassSelfRestriction()) {
            return KetQua.error("Ban khong the tu ky hop dong cho chinh minh.");
        }
        NhanVien nv = nvRepo.findByMaNhanVien(hd.getMaNV());
        if (nv == null) {
            return KetQua.error("Khong tim thay nhan vien.");
        }
        if (hd.getLuongCoSo() <= 0) {
            return KetQua.error("Luong co so phai lon hon 0.");
        }
        hd.setSoHopDong(generateSoHopDong());
        KetQua<Void> dateValidation = validateNgayKyVaNgayHieuLuc(hd);
        if (!dateValidation.isSuccess()) {
            return KetQua.error(dateValidation.getMessage());
        }
        KetQua<Void> typeValidation = validateTheoLoaiHopDong(hd);
        if (!typeValidation.isSuccess()) {
            return KetQua.error(typeValidation.getMessage());
        }
        HopDongLaoDong existing = hopDongRepo.findHieuLuc(hd.getMaNV());
        if (existing != null) {
            return KetQua.error("Nhan vien da co hop dong dang hieu luc (so HD: " + existing.getSoHopDong() + ").");
        }
        HopDongLaoDong pending = hopDongRepo.findChoDuyet(hd.getMaNV());
        if (pending != null) {
            return KetQua.error("Nhan vien da co hop dong dang cho phe duyet (so HD: " + pending.getSoHopDong() + ").");
        }
        hd.setTrangThai(HRMConstants.TRANG_THAI_CHO_DUYET);
        try {
            hopDongRepo.insert(hd);
            return KetQua.success(hd, "Tao hop dong thanh cong. Hop dong dang cho phe duyet.");
        } catch (Exception e) {
            return KetQua.error("Loi tao hop dong: " + e.getMessage());
        }
    }

    public KetQua<Void> pheDuyetHopDong(int maHopDong) {
        TaiKhoan currentUser = SessionContext.getInstance().getCurrentUser();
        if (currentUser == null) return KetQua.error("Phien dang nhap khong hop le.");
        if (!SessionContext.getInstance().isAdmin()
                && !currentUser.coQuyen(ACTION_CONTRACT_APPROVE)) {
            return KetQua.error("Ban khong co quyen phe duyet hop dong.");
        }
        HopDongLaoDong hd = findById(maHopDong);
        if (hd == null) return KetQua.error("Khong tim thay hop dong.");
        if (!HRMConstants.TRANG_THAI_CHO_DUYET.equals(hd.getTrangThai())) {
            return KetQua.error("Chi phe duyet hop dong dang o trang thai 'cho phe duyet'.");
        }
        hopDongRepo.updateTrangThai(maHopDong, HRMConstants.TRANG_THAI_HIEU_LUC);
        NhanVien nv = nvRepo.findByMaNhanVien(hd.getMaNV());
        if (nv != null) {
            nv.setLoaiHopDong(hd.getLoaiHopDong());
            nvRepo.update(nv);
        }
        return KetQua.success(null, "Hop dong da duoc phe duyet va co hieu luc.");
    }

    public KetQua<Void> thanhLyHopDong(int maHopDong) {
        HopDongLaoDong hd = findById(maHopDong);
        if (hd == null) {
            return KetQua.error("Khong tim thay hop dong.");
        }
        KetQua<Void> permission = validateContractPermission(ACTION_CONTRACT_MANAGE, hd.getMaNV());
        if (!permission.isSuccess()) {
            return permission;
        }
        if (HRMConstants.TRANG_THAI_THANH_LY.equals(hd.getTrangThai())) {
            return KetQua.error("Hop dong da duoc thanh ly.");
        }
        if ("het_han".equals(hd.getTrangThai())) {
            return KetQua.error("Khong the thanh ly hop dong da het han. Chi thanh ly hop dong dang hieu luc.");
        }
        hopDongRepo.updateTrangThai(maHopDong, HRMConstants.TRANG_THAI_THANH_LY);
        if (hd.getMaNV() != null) {
            BoNhiemDAO.getInstance().endAllActiveBoNhiemForNV(hd.getMaNV(), LocalDate.now());
        }
        return KetQua.success(null, "Thanh ly hop dong thanh cong. Da ket thuc cac bo nhiem hieu luc.");
    }

    public boolean isHopDongHetHan(int maHopDong) {
        HopDongLaoDong hd = findById(maHopDong);
        return hd != null && "het_han".equals(hd.getTrangThai());
    }

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

    private KetQua<Void> validateContractPermission(String action, String targetMaNV) {
        TaiKhoan currentUser = SessionContext.getInstance().getCurrentUser();
        if (currentUser == null) {
            return KetQua.error("Phien dang nhap khong hop le.");
        }
        if (SessionContext.getInstance().isAdmin()) {
            return KetQua.success(null, "");
        }
        if (!currentUser.coQuyen(action)) {
            return KetQua.error("Ban khong co quyen thao tac hop dong nay.");
        }
        if (!isTargetWithinActionScope(action, currentUser.getMaNV(), targetMaNV)) {
            return KetQua.error("Ban khong co pham vi thao tac tren nhan vien nay.");
        }
        return KetQua.success(null, "");
    }

    private boolean isTargetWithinActionScope(String action, String currentMaNV, String targetMaNV) {
        DataScope scope = XacThucBUS.getInstance().getScopeForAction(action);
        if (scope == DataScope.ALL) {
            return true;
        }
        if (scope == DataScope.NONE || ValidationUtils.isBlank(currentMaNV) || ValidationUtils.isBlank(targetMaNV)) {
            return false;
        }
        if (scope == DataScope.SELF) {
            return targetMaNV.equalsIgnoreCase(currentMaNV);
        }
        for (NhanVien nv : NhanVienBUS.getInstance().getAllByActionScope(action, currentMaNV)) {
            if (targetMaNV.equalsIgnoreCase(nv.getMaNhanVien())) {
                return true;
            }
        }
        return false;
    }

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
            return KetQua.error("Ngay ky khong duoc de trong.");
        }
        if (hd.getNgayHieuLuc() == null) {
            return KetQua.error("Ngay hieu luc khong duoc de trong.");
        }
        if (hd.getNgayHieuLuc().isBefore(hd.getNgayKy())) {
            return KetQua.error("Ngay hieu luc phai bang hoac sau ngay ky.");
        }
        return KetQua.success(null, "");
    }

    private KetQua<Void> validateTheoLoaiHopDong(HopDongLaoDong hd) {
        String loai = hd.getLoaiHopDong();
        if (ValidationUtils.isBlank(loai)) {
            return KetQua.error("Loai hop dong khong duoc de trong.");
        }
        if (LOAI_THU_VIEC.equals(loai)) {
            return validateHopDongThuViec(hd);
        }
        if (LOAI_XAC_DINH_THOI_HAN.equals(loai)) {
            return validateHopDongXacDinhThoiHan(hd);
        }
        if (!LOAI_KHONG_XAC_DINH_THOI_HAN.equals(loai)) {
            return KetQua.error("Loai hop dong khong hop le.");
        }
        return KetQua.success(null, "");
    }

    private KetQua<Void> validateHopDongThuViec(HopDongLaoDong hd) {
        if (hd.getNgayHetHieuLuc() == null) {
            return KetQua.error("Hop dong thu viec phai co ngay het hieu luc.");
        }
        long soNgay = ChronoUnit.DAYS.between(hd.getNgayHieuLuc(), hd.getNgayHetHieuLuc());
        if (soNgay > 60) {
            return KetQua.error("Hop dong thu viec khong duoc vuot qua 60 ngay.");
        }
        if (soNgay <= 0) {
            return KetQua.error("Ngay het hieu luc phai sau ngay hieu luc.");
        }
        return KetQua.success(null, "");
    }

    private KetQua<Void> validateHopDongXacDinhThoiHan(HopDongLaoDong hd) {
        if (hd.getNgayHetHieuLuc() == null) {
            return KetQua.error("Hop dong xac dinh thoi han phai co ngay het hieu luc.");
        }
        long soThang = ChronoUnit.MONTHS.between(hd.getNgayHieuLuc(), hd.getNgayHetHieuLuc());
        if (soThang > 36) {
            return KetQua.error("Hop dong xac dinh thoi han khong duoc vuot qua 36 thang.");
        }
        if (hd.getNgayHetHieuLuc().isBefore(hd.getNgayHieuLuc())) {
            return KetQua.error("Ngay het hieu luc phai sau ngay hieu luc.");
        }
        return KetQua.success(null, "");
    }

    private String getCurrentUserNhanVienId() {
        TaiKhoan currentUser = SessionContext.getInstance().getCurrentUser();
        return currentUser != null ? currentUser.getMaNV() : null;
    }

    private String generateSoHopDong() {
        LocalDate today = LocalDate.now();
        int seq = hopDongRepo.countByYearMonth(today.getYear(), today.getMonthValue()) + 1;
        return String.format("HD-%04d%02d-%04d", today.getYear(), today.getMonthValue(), seq);
    }

}
