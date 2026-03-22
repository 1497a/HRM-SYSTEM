package com.hrm.bus;

import com.hrm.dao.BoNhiemDAO;
import com.hrm.dao.HopDongDAO;
import com.hrm.dao.NhanVienDAO;
import com.hrm.dao.TuyenDungDAO;
import com.hrm.model.DataScope;
import com.hrm.model.HopDongLaoDong;
import com.hrm.model.NhanVien;
import com.hrm.model.TaiKhoan;
import com.hrm.model.TinTuyenDung;
import com.hrm.model.UngVien;
import com.hrm.model.YeuCauTuyenDung;
import com.hrm.util.HRMConstants;
import com.hrm.util.PermissionCodes;
import com.hrm.util.SessionContext;
import com.hrm.util.ValidationUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class HopDongBUS {

    private static final String ACTION_CONTRACT_CREATE  = PermissionCodes.CONTRACT_CREATE;
    private static final String ACTION_CONTRACT_APPROVE = PermissionCodes.CONTRACT_APPROVE;
    private static final String ACTION_CONTRACT_MANAGE  = PermissionCodes.CONTRACT_MANAGE;
    private static HopDongBUS instance;
    private final HopDongDAO hopDongRepo = HopDongDAO.getInstance();
    private final NhanVienDAO nvRepo = NhanVienDAO.getInstance();
    private final TuyenDungDAO tuyenDungRepo = TuyenDungDAO.getInstance();
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
            return KetQua.error("Không tìm thấy nhân viên " + maNV);
        }
        HopDongLaoDong existing = hopDongRepo.findHieuLuc(maNV);
        if (existing != null) {
            return KetQua.error("Nhân viên đã có hợp đồng hiệu lực.");
        }
        HopDongLaoDong pending = hopDongRepo.findChoDuyet(maNV);
        if (pending != null) {
            return KetQua.error("Nhân viên đã có hợp đồng chờ duyệt.");
        }
        HopDongLaoDong hd = new HopDongLaoDong();
        hd.setMaNV(maNV);
        hd.setSoHopDong(generateSoHopDong());
        hd.setLoaiHopDong(HRMConstants.LOAI_HOP_DONG_THU_VIEC);
        hd.setLuongCoSo(luongCoSo);
        hd.setNgayKy(LocalDate.now());
        hd.setNgayHieuLuc(LocalDate.now());
        hd.setNgayHetHieuLuc(LocalDate.now().plusDays(60));
        hd.setTrangThai(HRMConstants.TRANG_THAI_CHO_DUYET);
        hd.setGhiChu(ghiChu);
        TaiKhoan creator = SessionContext.getInstance().getCurrentUser();
        if (creator != null) {
            hd.setNguoiTao(creator.getTenDangNhap());
        }
        try {
            int id = hopDongRepo.insert(hd);
            if (id <= 0) {
                return KetQua.error("Không thể tạo hợp đồng thử việc. Vui lòng thử lại.");
            }
            return KetQua.success(hd, "Tạo hợp đồng thử việc thành công (chờ phê duyệt).");
        } catch (Exception e) {
            return KetQua.error("Lỗi tạo hợp đồng tự động: " + e.getMessage());
        }
    }

    public KetQua<HopDongLaoDong> taoHopDong(HopDongLaoDong hd) {
        if (hd == null || ValidationUtils.isBlank(hd.getMaNV())) {
            return KetQua.error("Nhân viên hợp đồng không hợp lệ.");
        }
        KetQua<Void> permission = validateContractPermission(ACTION_CONTRACT_CREATE, hd.getMaNV());
        if (!permission.isSuccess()) {
            return KetQua.error(permission.getMessage());
        }
        if (SelfApprovalGuard.isSelfAction(getCurrentUserNhanVienId(), hd.getMaNV())
                && !SelfApprovalGuard.currentUserCanBypassSelfRestriction()) {
            return KetQua.error("Bạn không thể tự ký hợp đồng cho chính mình.");
        }
        NhanVien nv = nvRepo.findByMaNhanVien(hd.getMaNV());
        if (nv == null) {
            return KetQua.error("Không tìm thấy nhân viên.");
        }
        if (hd.getLuongCoSo() <= 0) {
            return KetQua.error("Lương cơ sở phải lớn hơn 0.");
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
            return KetQua.error("Nhân viên đã có hợp đồng đang hiệu lực (số HĐ: " + existing.getSoHopDong() + ").");
        }
        HopDongLaoDong pending = hopDongRepo.findChoDuyet(hd.getMaNV());
        if (pending != null) {
            return KetQua.error("Nhân viên đã có hợp đồng đang chờ phê duyệt (số HĐ: " + pending.getSoHopDong() + ").");
        }
        hd.setTrangThai(HRMConstants.TRANG_THAI_CHO_DUYET);
        TaiKhoan currentUser = SessionContext.getInstance().getCurrentUser();
        if (currentUser != null) {
            hd.setNguoiTao(currentUser.getTenDangNhap());
        }
        try {
            int id = hopDongRepo.insert(hd);
            if (id <= 0) {
                return KetQua.error("Không thể tạo hợp đồng. Vui lòng thử lại.");
            }
            return KetQua.success(hd, "Tạo hợp đồng thành công. Hợp đồng đang chờ phê duyệt.");
        } catch (Exception e) {
            return KetQua.error("Lỗi tạo hợp đồng: " + e.getMessage());
        }
    }

    public KetQua<Void> pheDuyetHopDong(int maHopDong) {
        TaiKhoan currentUser = SessionContext.getInstance().getCurrentUser();
        if (currentUser == null) return KetQua.error("Phiên đăng nhập không hợp lệ.");
        if (!SessionContext.getInstance().isAdmin()
                && !currentUser.coQuyen(ACTION_CONTRACT_APPROVE)) {
            return KetQua.error("Bạn không có quyền phê duyệt hợp đồng.");
        }
        HopDongLaoDong hd = findById(maHopDong);
        if (hd == null) return KetQua.error("Không tìm thấy hợp đồng.");
        if (!HRMConstants.TRANG_THAI_CHO_DUYET.equals(hd.getTrangThai())) {
            return KetQua.error("Chỉ phê duyệt hợp đồng đang ở trạng thái 'chờ phê duyệt'.");
        }
        String maTK = currentUser.getTenDangNhap();
        LocalDateTime ngayDuyet = LocalDateTime.now();
        try {
            int rows = hopDongRepo.updateApproval(maHopDong, HRMConstants.TRANG_THAI_HIEU_LUC, maTK, ngayDuyet);
            if (rows <= 0) {
                return KetQua.error("Không thể phê duyệt hợp đồng. Vui lòng thử lại.");
            }
        } catch (Exception e) {
            return KetQua.error("Lỗi phê duyệt hợp đồng: " + e.getMessage());
        }
        NhanVien nv = nvRepo.findByMaNhanVien(hd.getMaNV());
        if (nv != null) {
            nv.setLoaiHopDong(hd.getLoaiHopDong());
            try {
                int nvRows = nvRepo.update(nv);
                if (nvRows <= 0) {
                    System.err.println("Canh bao: Khong the cap nhat loai hop dong cho NV " + hd.getMaNV());
                }
            } catch (Exception e) {
                System.err.println("Cảnh báo: Không thể cập nhật loại hợp đồng cho NV " + hd.getMaNV() + ": " + e.getMessage());
            }
        }
        String boNhiemMsg = "";
        if (HRMConstants.LOAI_HOP_DONG_THU_VIEC.equals(hd.getLoaiHopDong())) {
            boNhiemMsg = tuDongBoNhiemTuHopDong(hd);
        }
        return KetQua.success(null, "Hợp đồng đã được phê duyệt và có hiệu lực." + boNhiemMsg);
    }

    public KetQua<Void> thanhLyHopDong(int maHopDong) {
        HopDongLaoDong hd = findById(maHopDong);
        if (hd == null) {
            return KetQua.error("Không tìm thấy hợp đồng.");
        }
        KetQua<Void> permission = validateContractPermission(ACTION_CONTRACT_MANAGE, hd.getMaNV());
        if (!permission.isSuccess()) {
            return permission;
        }
        if (HRMConstants.TRANG_THAI_THANH_LY.equals(hd.getTrangThai())) {
            return KetQua.error("Hợp đồng đã được thanh lý.");
        }
        if (HRMConstants.TRANG_THAI_HET_HAN.equals(hd.getTrangThai())) {
            return KetQua.error("Không thể thanh lý hợp đồng đã hết hạn. Chỉ thanh lý hợp đồng đang hiệu lực.");
        }
        try {
            int rows = hopDongRepo.updateTrangThai(maHopDong, HRMConstants.TRANG_THAI_THANH_LY);
            if (rows <= 0) {
                return KetQua.error("Không thể thanh lý hợp đồng. Vui lòng thử lại.");
            }
        } catch (Exception e) {
            return KetQua.error("Lỗi thanh lý hợp đồng: " + e.getMessage());
        }
        if (hd.getMaNV() != null) {
            BoNhiemDAO.getInstance().endAllActiveBoNhiemForNV(hd.getMaNV(), LocalDate.now());
        }
        return KetQua.success(null, "Thanh lý hợp đồng thành công. Đã kết thúc các bổ nhiệm hiệu lực.");
    }

    public boolean isHopDongHetHan(int maHopDong) {
        HopDongLaoDong hd = findById(maHopDong);
        return hd != null && HRMConstants.TRANG_THAI_HET_HAN.equals(hd.getTrangThai());
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
            return KetQua.error("Phiên đăng nhập không hợp lệ.");
        }
        if (SessionContext.getInstance().isAdmin()) {
            return KetQua.success(null, "");
        }
        if (!currentUser.coQuyen(action)) {
            return KetQua.error("Bạn không có quyền thao tác hợp đồng này.");
        }
        if (!isTargetWithinActionScope(action, currentUser.getMaNV(), targetMaNV)) {
            return KetQua.error("Bạn không có phạm vi thao tác trên nhân viên này.");
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
        return hopDongRepo.findById(maHopDong);
    }

    private String tuDongBoNhiemTuHopDong(HopDongLaoDong hd) {
        UngVien uv = tuyenDungRepo.findUngVienByMaNV(hd.getMaNV());
        if (uv == null) return "";
        TinTuyenDung tin = tuyenDungRepo.findTinById(uv.getMaTin());
        if (tin == null) return "";
        YeuCauTuyenDung yc = tuyenDungRepo.findYeuCauById(tin.getMaYeuCau());
        if (yc == null || ValidationUtils.isBlank(yc.getMaChucVu())) return "";
        KetQua<Void> kq = BoNhiemBUS.getInstance().taoBoNhiemTuHopDong(
                hd.getMaNV(), yc, hd.getNgayHieuLuc(), hd.getMaHopDong());
        if (kq.isSuccess()) {
            return " | Bổ nhiệm đã được tạo tự động. Vui lòng phê duyệt trong mục Bổ nhiệm.";
        }
        return " | Tạo bổ nhiệm tự động thất bại: " + kq.getMessage();
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
        if (ValidationUtils.isBlank(loai)) {
            return KetQua.error("Loại hợp đồng không được để trống.");
        }
        if (HRMConstants.LOAI_HOP_DONG_THU_VIEC.equals(loai)) {
            return validateHopDongThuViec(hd);
        }
        if (HRMConstants.LOAI_HOP_DONG_XAC_DINH.equals(loai)) {
            return validateHopDongXacDinhThoiHan(hd);
        }
        if (!HRMConstants.LOAI_HOP_DONG_KHONG_XAC_DINH.equals(loai)) {
            return KetQua.error("Loại hợp đồng không hợp lệ.");
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
        TaiKhoan currentUser = SessionContext.getInstance().getCurrentUser();
        return currentUser != null ? currentUser.getMaNV() : null;
    }

    private String generateSoHopDong() {
        LocalDate today = LocalDate.now();
        int seq = hopDongRepo.countByYearMonth(today.getYear(), today.getMonthValue()) + 1;
        return String.format("HD-%04d%02d-%04d", today.getYear(), today.getMonthValue(), seq);
    }

}
