package com.hrm.bus;

import com.hrm.dao.BoNhiemDAO;
import com.hrm.dao.NhanVienDAO;
import com.hrm.dao.TaiKhoanDAO;
import com.hrm.dao.ThongTinCaNhanDAO;
import com.hrm.model.NhanVien;
import com.hrm.model.TaiKhoan;
import com.hrm.model.ThongTinCaNhan;
import com.hrm.util.DatabaseConnection;
import com.hrm.util.HRMConstants;
import com.hrm.util.PermissionCodes;
import com.hrm.util.SessionContext;
import com.hrm.util.ValidationUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Service quản lý nhân viên.
 * Singleton pattern.
 */
public class NhanVienBUS {
    private static final String ACTION_EMPLOYEE_VIEW = PermissionCodes.EMPLOYEE_VIEW;
    private static final String ACTION_EMPLOYEE_UPDATE = PermissionCodes.EMPLOYEE_UPDATE;
    private static final String ACTION_EMPLOYEE_STATUS_UPDATE = PermissionCodes.EMPLOYEE_STATUS_UPDATE;
    private static NhanVienBUS instance;
    private final NhanVienDAO nvRepo = NhanVienDAO.getInstance();
    private final ThongTinCaNhanDAO ttcnRepo = ThongTinCaNhanDAO.getInstance();
    private final BoNhiemDAO boNhiemRepo = BoNhiemDAO.getInstance();
    private final TaiKhoanDAO taiKhoanRepo = TaiKhoanDAO.getInstance();
    private final HopDongBUS hopDongBus = HopDongBUS.getInstance();
    private NhanVienBUS() {
    }

    public static synchronized NhanVienBUS getInstance() {
        if (instance == null) {
            instance = new NhanVienBUS();
        }
        return instance;
    }

    public KetQua<NhanVien> taoHoSo(NhanVien nv, ThongTinCaNhan ttcn) {
        if (ValidationUtils.isBlank(nv.getMaNhanVien())) {
            return KetQua.error("Mã nhân viên không được để trống.");
        }
        if (nvRepo.existsByMaNhanVien(nv.getMaNhanVien().trim())) {
            return KetQua.error("Mã nhân viên '" + nv.getMaNhanVien() + "' đã tồn tại.");
        }
        if (ValidationUtils.isBlank(ttcn.getHoTen())) {
            return KetQua.error("Họ tên không được để trống.");
        }
        if (ttcn.getNgaySinh() == null) {
            return KetQua.error("Ngày sinh không được để trống.");
        }
        String birthErr = ValidationUtils.validateBirthDate(ttcn.getNgaySinh());
        if (birthErr != null) {
            return KetQua.error(birthErr);
        }
        if (ValidationUtils.isBlank(ttcn.getDiaChi())) {
            return KetQua.error("Địa chỉ không được để trống.");
        }
        ttcn.setDiaChi(ttcn.getDiaChi().trim());
        String emailErr = ValidationUtils.validateEmail(ttcn.getEmail());
        if (emailErr != null) {
            return KetQua.error(emailErr);
        }
        String phoneErr = ValidationUtils.validatePhone(ttcn.getDienThoai());
        if (phoneErr != null) {
            return KetQua.error(phoneErr);
        }
        KetQua<Void> cccdValidation = validateCCCD(ttcn.getCccd(), nv.getMaNhanVien());
        if (!cccdValidation.isSuccess()) {
            return KetQua.error(cccdValidation.getMessage());
        }
        if (nv.getNgayVaoLam() == null) {
            return KetQua.error("Ngày vào làm không được để trống.");
        }
        if (ValidationUtils.isBlank(nv.getTrangThai())) {
            nv.setTrangThai(HRMConstants.TRANG_THAI_DANG_LAM_VIEC);
        }
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                String maNV = nvRepo.insert(conn, nv);
                ttcn.setMaNV(maNV);
                ttcnRepo.insert(conn, ttcn);
                insertSoDungPhep(conn, maNV, LocalDate.now().getYear(), HRMConstants.LOAI_PHEP_NAM, 12.0);
                conn.commit();
                nv.setHoTen(ttcn.getHoTen());
                return KetQua.success(nv, "Tạo hồ sơ nhân viên thành công. Mã NV: " + maNV);
            } catch (Exception ex) {
                conn.rollback();
                return KetQua.error("Lỗi khi tạo hồ sơ: " + ex.getMessage());
            }
        } catch (SQLException e) {
            return KetQua.error("Lỗi kết nối cơ sở dữ liệu: " + e.getMessage());
        }
    }

    public KetQua<ThongTinCaNhan> capNhatThongTinCaNhan(ThongTinCaNhan ttcn) {
        if (ValidationUtils.isBlank(ttcn.getMaNV())) {
            return KetQua.error("Mã nhân viên không hợp lệ.");
        }
        if (!canEditEmployeeProfile(ttcn.getMaNV())) {
            return KetQua.error("Bạn không có quyền cập nhật thông tin nhân viên này.");
        }
        KetQua<Void> cccdValidation = validateCCCD(ttcn.getCccd(), ttcn.getMaNV());
        if (!cccdValidation.isSuccess()) {
            return KetQua.error(cccdValidation.getMessage());
        }
        if (ttcn.getNgaySinh() == null) {
            return KetQua.error("Ngày sinh không được để trống.");
        }
        String birthErr = ValidationUtils.validateBirthDate(ttcn.getNgaySinh());
        if (birthErr != null) {
            return KetQua.error(birthErr);
        }
        if (ValidationUtils.isBlank(ttcn.getDiaChi())) {
            return KetQua.error("Địa chỉ không được để trống.");
        }
        ttcn.setDiaChi(ttcn.getDiaChi().trim());
        String emailErr = ValidationUtils.validateEmail(ttcn.getEmail());
        if (emailErr != null) return KetQua.error(emailErr);
        String phoneErr = ValidationUtils.validatePhone(ttcn.getDienThoai());
        if (phoneErr != null) return KetQua.error(phoneErr);
        try {
            int rows = ttcnRepo.update(ttcn);
            if (rows <= 0) {
                return KetQua.error("Không thể cập nhật thông tin cá nhân. Vui lòng thử lại.");
            }
            return KetQua.success(ttcn, "Cập nhật thông tin cá nhân thành công.");
        } catch (Exception e) {
            return KetQua.error("Lỗi cập nhật thông tin cá nhân: " + e.getMessage());
        }
    }

    public KetQua<NhanVien> capNhatTrangThai(String maNV, String trangThaiMoi, String lyDo) {
        if (ValidationUtils.isBlank(maNV)) {
            return KetQua.error("Mã nhân viên không hợp lệ.");
        }
        NhanVien nv = nvRepo.findByMaNhanVien(maNV);
        if (nv == null) {
            return KetQua.error("Không tìm thấy nhân viên với mã: " + maNV);
        }
        KetQua<Void> permissionValidation = validateStatusChangePermission(nv, trangThaiMoi);
        if (!permissionValidation.isSuccess()) {
            return KetQua.error(permissionValidation.getMessage());
        }
        KetQua<Void> transitionValidation = validateTrangThaiTransition(nv.getTrangThai(), trangThaiMoi);
        if (!transitionValidation.isSuccess()) {
            return KetQua.error(transitionValidation.getMessage());
        }
        nv.setTrangThai(trangThaiMoi);
        String nguoiThucHien = null;
        TaiKhoan currentUser = SessionContext.getInstance().getCurrentUser();
        if (currentUser != null) {
            nguoiThucHien = currentUser.getMaNV();
        }
        int rows = nvRepo.updateTrangThai(maNV, trangThaiMoi, nguoiThucHien);
        if (rows <= 0) {
            return KetQua.error("Không thể cập nhật trạng thái nhân viên. Vui lòng thử lại.");
        }
        if (HRMConstants.TRANG_THAI_NGHI_VIEC.equals(trangThaiMoi)) {
            boNhiemRepo.endAllActiveBoNhiemForNV(maNV, LocalDate.now(), nguoiThucHien);
            KetQua<Void> contractResult = hopDongBus.thanhLyHopDongDoNghiViec(
                    maNV,
                    LocalDate.now(),
                    "Nhan vien nghi viec",
                    nguoiThucHien);
            if (!contractResult.isSuccess()) {
                System.err.println("Canh bao: " + contractResult.getMessage());
            }
            int deactivated = taiKhoanRepo.deactivateByMaNV(maNV);
            if (deactivated <= 0) {
                System.err.println("Canh bao: Khong the vo hieu hoa tai khoan cho NV " + maNV + " khi nghi viec.");
            }
        }
        return KetQua.success(nv, "Cập nhật trạng thái nhân viên thành công.");
    }

    public boolean canChangeEmployeeStatus(String maNV) {
        if (ValidationUtils.isBlank(maNV)) {
            return false;
        }
        NhanVien target = nvRepo.findByMaNhanVien(maNV);
        return canChangeEmployeeStatus(target);
    }

    public boolean canEditEmployeeProfile(String maNV) {
        if (ValidationUtils.isBlank(maNV)) {
            return false;
        }
        NhanVien target = nvRepo.findByMaNhanVien(maNV);
        return canEditEmployeeProfile(target);
    }

    public List<NhanVien> getAll() {
        return nvRepo.findAll();
    }

    public List<NhanVien> getAllByScope(String currentMaNV) {
        com.hrm.model.DataScope scope = XacThucBUS.getInstance().getScopeForAction(ACTION_EMPLOYEE_VIEW);
        return nvRepo.findAllByScope(scope, currentMaNV);
    }

    public List<NhanVien> getAllByActionScope(String action, String currentMaNV) {
        com.hrm.model.DataScope scope = XacThucBUS.getInstance().getScopeForAction(action);
        return nvRepo.findAllByScope(scope, currentMaNV);
    }

    public List<NhanVien> getDangLamViec() {
        return nvRepo.findDangLamViec();
    }

    public NhanVien getByMaNhanVien(String maNhanVien) {
        return nvRepo.findByMaNhanVien(maNhanVien);
    }

    public ThongTinCaNhan getThongTinCaNhan(String maNV) {
        return ttcnRepo.findByMaNV(maNV);
    }

    public String generateMaNhanVien() {
        return nvRepo.generateMaNhanVien();
    }

    public List<NhanVien> getNhanVienByMaQuanLy(String maQuanLy) {
        return nvRepo.findNhanVienByMaQuanLy(maQuanLy);
    }

    private void insertSoDungPhep(Connection conn, String maNV, int nam, String maLoaiPhep,
                                  double soNgayDuocCap) throws SQLException {
        String sql = "INSERT INTO SODUNGPHEP (maNV, nam, maLoaiPhep, soNgayDuocCap, soNgayDaDung) "
                + "VALUES (?, ?, ?, ?, 0) "
                + "ON DUPLICATE KEY UPDATE soNgayDuocCap=VALUES(soNgayDuocCap)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maNV);
            ps.setInt(2, nam);
            ps.setString(3, maLoaiPhep);
            ps.setDouble(4, soNgayDuocCap);
            ps.executeUpdate();
        }
    }

    private KetQua<Void> validateCCCD(String cccdRaw, String maNV) {
        if (ValidationUtils.isBlank(cccdRaw)) {
            return KetQua.success(null, "");
        }
        String cccd = cccdRaw.trim();
        if (!cccd.matches("\\d{12}")) {
            return KetQua.error("CCCD phải là 12 chữ số.");
        }
        if (ttcnRepo.existsByCCCD(cccd, maNV)) {
            return KetQua.error("CCCD '" + cccdRaw + "' đã được đăng ký cho nhân viên khác.");
        }
        return KetQua.success(null, "");
    }

    private KetQua<Void> validateTrangThaiTransition(String trangThaiHienTai, String trangThaiMoi) {
        boolean valid = false;
        if (HRMConstants.TRANG_THAI_DANG_LAM_VIEC.equals(trangThaiHienTai)
                && (HRMConstants.TRANG_THAI_TAM_NGHI.equals(trangThaiMoi) || HRMConstants.TRANG_THAI_NGHI_VIEC.equals(trangThaiMoi))) {
            valid = true;
        }
        if (HRMConstants.TRANG_THAI_TAM_NGHI.equals(trangThaiHienTai)
                && (HRMConstants.TRANG_THAI_DANG_LAM_VIEC.equals(trangThaiMoi) || HRMConstants.TRANG_THAI_NGHI_VIEC.equals(trangThaiMoi))) {
            valid = true;
        }
        if (valid) {
            return KetQua.success(null, "");
        }
        if (HRMConstants.TRANG_THAI_NGHI_VIEC.equals(trangThaiHienTai)) {
            return KetQua.error("Nhân viên đã nghỉ việc, không thể thay đổi trạng thái.");
        }
        return KetQua.error("Chuyển trạng thái không hợp lệ từ '" + trangThaiHienTai + "' sang '" + trangThaiMoi + "'.");
    }

    private boolean canEditEmployeeProfile(NhanVien target) {
        TaiKhoan currentUser = SessionContext.getInstance().getCurrentUser();
        if (currentUser == null || target == null || ValidationUtils.isBlank(target.getMaNhanVien())) {
            return false;
        }
        String currentMaNV = currentUser.getMaNV();
        if (SelfApprovalGuard.isSelfAction(currentMaNV, target.getMaNhanVien())) {
            return true;
        }
        return isCurrentUserAllowedForAction(currentUser, ACTION_EMPLOYEE_UPDATE)
                && isTargetWithinActionScope(ACTION_EMPLOYEE_UPDATE, currentMaNV, target.getMaNhanVien());
    }

    private boolean canChangeEmployeeStatus(NhanVien target) {
        return validateStatusPermission(target).isSuccess();
    }

    private KetQua<Void> validateStatusChangePermission(NhanVien target, String trangThaiMoi) {
        return validateStatusPermission(target);
    }

    private KetQua<Void> validateStatusPermission(NhanVien target) {
        TaiKhoan currentUser = SessionContext.getInstance().getCurrentUser();
        if (currentUser == null) {
            return KetQua.error("Phiên đăng nhập không hợp lệ.");
        }
        if (target == null || ValidationUtils.isBlank(target.getMaNhanVien())) {
            return KetQua.error("Nhân viên mục tiêu không hợp lệ.");
        }
        String currentMaNV = currentUser.getMaNV();
        if (SelfApprovalGuard.isSelfAction(currentMaNV, target.getMaNhanVien())
                && !SelfApprovalGuard.currentUserCanBypassSelfRestriction()) {
            return KetQua.error("Không thể tự đổi trạng thái của chính mình.");
        }
        if (!isCurrentUserAllowedForAction(currentUser, ACTION_EMPLOYEE_STATUS_UPDATE)) {
            return KetQua.error("Bạn không có quyền đổi trạng thái nhân viên này.");
        }
        return KetQua.success(null, "");
    }

    private boolean isCurrentUserAllowedForAction(TaiKhoan currentUser, String action) {
        return currentUser != null
                && (SessionContext.getInstance().isAdmin() || currentUser.coQuyen(action));
    }

    private boolean isTargetWithinActionScope(String action, String currentMaNV, String targetMaNV) {
        com.hrm.model.DataScope scope = XacThucBUS.getInstance().getScopeForAction(action);
        if (scope == com.hrm.model.DataScope.ALL) {
            return true;
        }
        if (scope == com.hrm.model.DataScope.NONE
                || ValidationUtils.isBlank(currentMaNV)
                || ValidationUtils.isBlank(targetMaNV)) {
            return false;
        }
        if (scope == com.hrm.model.DataScope.SELF) {
            return targetMaNV.equalsIgnoreCase(currentMaNV);
        }
        for (NhanVien nv : nvRepo.findAllByScope(scope, currentMaNV)) {
            if (targetMaNV.equalsIgnoreCase(nv.getMaNhanVien())) {
                return true;
            }
        }
        return false;
    }

}
