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
 * Service quan ly nhan vien.
 * Singleton pattern.
 */
public class NhanVienBUS {
    private static final String ACTION_EMPLOYEE_VIEW = PermissionCodes.EMPLOYEE_VIEW;
    private static final String ACTION_EMPLOYEE_UPDATE = PermissionCodes.EMPLOYEE_UPDATE;
    private static final String ACTION_EMPLOYEE_STATUS_UPDATE = PermissionCodes.EMPLOYEE_STATUS_UPDATE;
    private static final String TRANG_THAI_DANG_LAM_VIEC = HRMConstants.TRANG_THAI_DANG_LAM_VIEC;
    private static final String TRANG_THAI_TAM_NGHI = "tam_nghi";
    private static final String TRANG_THAI_NGHI_VIEC = "nghi_viec";
    private static final String MA_LOAI_PHEP_NAM = "PHEP_NAM";
    private static final String REGEX_EMAIL = "^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$";
    private static final String REGEX_PHONE = "0\\d{9,10}";
    private static final String ROLE_ADMIN = HRMConstants.ROLE_ADMIN;
    private static NhanVienBUS instance;
    private final NhanVienDAO nvRepo = NhanVienDAO.getInstance();
    private final ThongTinCaNhanDAO ttcnRepo = ThongTinCaNhanDAO.getInstance();
    private final BoNhiemDAO boNhiemRepo = BoNhiemDAO.getInstance();
    private final TaiKhoanDAO taiKhoanRepo = TaiKhoanDAO.getInstance();
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
            return KetQua.error("Ma nhan vien khong duoc de trong.");
        }
        if (nvRepo.existsByMaNhanVien(nv.getMaNhanVien().trim())) {
            return KetQua.error("Ma nhan vien '" + nv.getMaNhanVien() + "' da ton tai.");
        }
        if (ValidationUtils.isBlank(ttcn.getHoTen())) {
            return KetQua.error("Ho ten khong duoc de trong.");
        }
        if (ttcn.getNgaySinh() != null) {
            String birthErr = ValidationUtils.validateBirthDate(ttcn.getNgaySinh());
            if (birthErr != null) {
                return KetQua.error(birthErr);
            }
        }
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
            return KetQua.error("Ngay vao lam khong duoc de trong.");
        }
        if (ValidationUtils.isBlank(nv.getTrangThai())) {
            nv.setTrangThai(TRANG_THAI_DANG_LAM_VIEC);
        }
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                String maNV = nvRepo.insert(conn, nv);
                ttcn.setMaNV(maNV);
                ttcnRepo.insert(conn, ttcn);
                insertSoDungPhep(conn, maNV, LocalDate.now().getYear(), MA_LOAI_PHEP_NAM, 12.0);
                conn.commit();
                nv.setHoTen(ttcn.getHoTen());
                return KetQua.success(nv, "Tao ho so nhan vien thanh cong. Ma NV: " + maNV);
            } catch (Exception ex) {
                conn.rollback();
                return KetQua.error("Loi khi tao ho so: " + ex.getMessage());
            }
        } catch (SQLException e) {
            return KetQua.error("Loi ket noi co so du lieu: " + e.getMessage());
        }
    }

    public KetQua<ThongTinCaNhan> capNhatThongTinCaNhan(ThongTinCaNhan ttcn) {
        if (ValidationUtils.isBlank(ttcn.getMaNV())) {
            return KetQua.error("Ma nhan vien khong hop le.");
        }
        if (!canEditEmployeeProfile(ttcn.getMaNV())) {
            return KetQua.error("Ban khong co quyen cap nhat thong tin nhan vien nay.");
        }
        KetQua<Void> cccdValidation = validateCCCD(ttcn.getCccd(), ttcn.getMaNV());
        if (!cccdValidation.isSuccess()) {
            return KetQua.error(cccdValidation.getMessage());
        }
        if (ttcn.getNgaySinh() != null) {
            String birthErr = ValidationUtils.validateBirthDate(ttcn.getNgaySinh());
            if (birthErr != null) {
                return KetQua.error(birthErr);
            }
        }
        String emailErr = ValidationUtils.validateEmail(ttcn.getEmail());
        if (emailErr != null) return KetQua.error(emailErr);
        String phoneErr = ValidationUtils.validatePhone(ttcn.getDienThoai());
        if (phoneErr != null) return KetQua.error(phoneErr);
        try {
            ttcnRepo.update(ttcn);
            return KetQua.success(ttcn, "Cap nhat thong tin ca nhan thanh cong.");
        } catch (Exception e) {
            return KetQua.error("Loi cap nhat thong tin ca nhan: " + e.getMessage());
        }
    }

    public KetQua<NhanVien> capNhatTrangThai(String maNV, String trangThaiMoi, String lyDo) {
        if (ValidationUtils.isBlank(maNV)) {
            return KetQua.error("Ma nhan vien khong hop le.");
        }
        NhanVien nv = nvRepo.findByMaNhanVien(maNV);
        if (nv == null) {
            return KetQua.error("Khong tim thay nhan vien voi ma: " + maNV);
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
        nvRepo.update(nv);
        if (TRANG_THAI_NGHI_VIEC.equals(trangThaiMoi)) {
            boNhiemRepo.endAllActiveBoNhiemForNV(maNV, LocalDate.now());
            taiKhoanRepo.deactivateByMaNV(maNV);
        }
        return KetQua.success(nv, "Cap nhat trang thai nhan vien thanh cong.");
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
            return KetQua.error("CCCD phai la 12 chu so.");
        }
        if (ttcnRepo.existsByCCCD(cccd, maNV)) {
            return KetQua.error("CCCD '" + cccdRaw + "' da duoc dang ky cho nhan vien khac.");
        }
        return KetQua.success(null, "");
    }

    private KetQua<Void> validateTrangThaiTransition(String trangThaiHienTai, String trangThaiMoi) {
        boolean valid = false;
        if (TRANG_THAI_DANG_LAM_VIEC.equals(trangThaiHienTai)
                && (TRANG_THAI_TAM_NGHI.equals(trangThaiMoi) || TRANG_THAI_NGHI_VIEC.equals(trangThaiMoi))) {
            valid = true;
        }
        if (TRANG_THAI_TAM_NGHI.equals(trangThaiHienTai)
                && (TRANG_THAI_DANG_LAM_VIEC.equals(trangThaiMoi) || TRANG_THAI_NGHI_VIEC.equals(trangThaiMoi))) {
            valid = true;
        }
        if (valid) {
            return KetQua.success(null, "");
        }
        if (TRANG_THAI_NGHI_VIEC.equals(trangThaiHienTai)) {
            return KetQua.error("Nhan vien da nghi viec, khong the thay doi trang thai.");
        }
        return KetQua.error("Chuyen trang thai khong hop le tu '" + trangThaiHienTai + "' sang '" + trangThaiMoi + "'.");
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
            return KetQua.error("Phien dang nhap khong hop le.");
        }
        if (target == null || ValidationUtils.isBlank(target.getMaNhanVien())) {
            return KetQua.error("Nhan vien muc tieu khong hop le.");
        }
        String currentMaNV = currentUser.getMaNV();
        if (SelfApprovalGuard.isSelfAction(currentMaNV, target.getMaNhanVien())
                && !SelfApprovalGuard.currentUserCanBypassSelfRestriction()) {
            return KetQua.error("Khong the tu doi trang thai cua chinh minh.");
        }
        if (!isCurrentUserAllowedForAction(currentUser, ACTION_EMPLOYEE_STATUS_UPDATE)) {
            return KetQua.error("Ban khong co quyen doi trang thai nhan vien nay.");
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
