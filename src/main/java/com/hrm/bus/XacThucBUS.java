package com.hrm.bus;

import com.hrm.model.Quyen;
import com.hrm.model.VaiTro;
import com.hrm.model.TaiKhoan;
import com.hrm.dao.TaiKhoanDAO;
import com.hrm.util.PasswordUtil;
import com.hrm.util.SessionContext;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Authentication Service - xử lý đăng nhập, đăng xuất và quản lý tài khoản.
 * Sử dụng TaiKhoanDAO để truy xuất cơ sở dữ liệu thực.
 */
public class XacThucBUS {

    private static final int MIN_PASSWORD_LENGTH = 6;
    private static final String ROLE_ADMIN = "ADMIN";
    private static final String USERNAME_ADMIN = "admin";

    private static XacThucBUS instance;
    private final TaiKhoanDAO taiKhoanRepo;

    private XacThucBUS() {
        this.taiKhoanRepo = new TaiKhoanDAO();
    }

    public static synchronized XacThucBUS getInstance() {
        if (instance == null) {
            instance = new XacThucBUS();
        }
        return instance;
    }

    // =====================================================================
    // ==================== Authentication =================================
    // =====================================================================

    /**
     * Xác thực người dùng bằng tên đăng nhập và mật khẩu.
     * Kiểm tra: tài khoản tồn tại, đang hoạt động, không bị khóa, mật khẩu đúng.
     *
     * @param username tên đăng nhập
     * @param password mật khẩu dạng plaintext
     * @return TaiKhoan nếu thành công, null nếu thất bại
     */
    public TaiKhoan authenticate(String username, String password) {
        if (isBlank(username) || isBlank(password)) return null;

        TaiKhoan user = taiKhoanRepo.findByUsername(username.trim());
        if (user == null || !user.isHoatDong() || user.isBiKhoa()) return null;
        if (!PasswordUtil.verifyPassword(password, user.getMatKhau())) return null;

        SessionContext.getInstance().setCurrentUser(user);
        return user;
    }

    /**
     * Đăng xuất người dùng hiện tại.
     */
    public void logout() {
        SessionContext.getInstance().clearSession();
    }

    /**
     * Kiểm tra có người dùng đang đăng nhập không.
     */
    public boolean isLoggedIn() {
        return SessionContext.getInstance().isLoggedIn();
    }

    /**
     * Lấy người dùng đang đăng nhập.
     */
    public TaiKhoan getCurrentUser() {
        return SessionContext.getInstance().getCurrentUser();
    }

    /**
     * Kiểm tra người dùng hiện tại có quyền chỉ định không.
     */
    public boolean hasPermission(String permissionCode) {
        TaiKhoan user = getCurrentUser();
        return user != null && user.coQuyen(permissionCode);
    }

    private boolean isBuiltInAdminAccount(TaiKhoan user) {
        if (user == null) return false;
        return USERNAME_ADMIN.equalsIgnoreCase(user.getTenDangNhap());
    }

    private boolean isCurrentAdminSelfUpdate(TaiKhoan target) {
        if (target == null) return false;
        TaiKhoan current = SessionContext.getInstance().getCurrentUser();
        if (current == null || current.getId() != target.getId()) return false;
        return current.coVaiTro(ROLE_ADMIN);
    }

    /**
     * Lấy phạm vi dữ liệu (phamVi) của hành động từ quyền đã nạp vào session.
     * Scope được lưu trực tiếp trong VAITRO_QUYEN.phamVi — không còn hậu tố _ALL/_DEPT/_TEAM/_SELF.
     *
     * @param action Mã quyền (e.g. "LEAVE_VIEW", "EMPLOYEE_VIEW")
     * @return Phạm vi dữ liệu (DataScope)
     */
    public com.hrm.model.DataScope getScopeForAction(String action) {
        TaiKhoan user = getCurrentUser();
        if (user == null) return com.hrm.model.DataScope.NONE;
        if (USERNAME_ADMIN.equalsIgnoreCase(user.getTenDangNhap()) || user.coVaiTro(ROLE_ADMIN)) {
            return com.hrm.model.DataScope.ALL;
        }
        for (com.hrm.model.VaiTro role : user.getVaiTros()) {
            for (com.hrm.model.Quyen q : role.getQuyens()) {
                if (action.equals(q.getId()) && q.getPhamVi() != null) {
                    return q.getPhamVi();
                }
            }
        }
        return com.hrm.model.DataScope.NONE;
    }

    /** Alias cho hasPermission() */
    public boolean coQuyen(String maQuyen) {
        return hasPermission(maQuyen);
    }

    /**
     * Kiểm tra người dùng hiện tại có vai trò chỉ định không.
     */
    public boolean hasRole(String roleCode) {
        TaiKhoan user = getCurrentUser();
        return user != null && user.coVaiTro(roleCode);
    }

    /**
     * Đổi mật khẩu cho người dùng (yêu cầu xác minh mật khẩu cũ).
     */
    public KetQua<Void> changePassword(int userId, String oldPass, String newPass) {
        if (isBlank(oldPass)) {
            return KetQua.error("Mật khẩu cũ không được để trống.");
        }
        KetQua<Void> passwordValidation = validateNewPassword(newPass);
        if (!passwordValidation.isSuccess()) return passwordValidation;

        TaiKhoan user = taiKhoanRepo.findById(userId);
        if (user == null) {
            return KetQua.error("Không tìm thấy tài khoản.");
        }
        if (!PasswordUtil.verifyPassword(oldPass, user.getMatKhau())) {
            return KetQua.error("Mật khẩu cũ không đúng.");
        }

        return updatePassword(userId, newPass, "Đổi mật khẩu thành công.", "Không thể cập nhật mật khẩu. Vui lòng thử lại.");
    }

    /**
     * Admin reset mật khẩu người dùng khác (không cần mật khẩu cũ).
     */
    public KetQua<Void> resetPassword(int userId, String newPass) {
        KetQua<Void> passwordValidation = validateNewPassword(newPass);
        if (!passwordValidation.isSuccess()) return passwordValidation;
        return updatePassword(userId, newPass, "Đã cập nhật mật khẩu thành công.", "Không thể cập nhật mật khẩu.");
    }

    // =====================================================================
    // ==================== TaiKhoan Management =============================
    // =====================================================================

    public List<TaiKhoan> getAllUsers() {
        return taiKhoanRepo.findAll();
    }

    /**
     * Tạo tài khoản mới.
     *
     * @param tenDangNhap tên đăng nhập
     * @param matKhau     mật khẩu plaintext (sẽ hash)
     * @param maNV        mã nhân viên liên kết (String, có thể null)
     * @param maVaiTro    mã vai trò
     * @param email       email
     * @return KetQua chứa ID tài khoản mới hoặc lỗi
     */
    public KetQua<Integer> createUser(String tenDangNhap, String matKhau,
                                      String maNV, String maVaiTro, String email) {
        if (isBlank(tenDangNhap)) {
            return KetQua.error("Tên đăng nhập không được để trống.");
        }
        if (matKhau == null || matKhau.isEmpty()) {
            return KetQua.error("Mật khẩu không được để trống.");
        }
        if (isBlank(maVaiTro)) {
            return KetQua.error("Vai trò không được để trống.");
        }

        String trimmedUsername = tenDangNhap.trim();
        if (taiKhoanRepo.existsByUsername(trimmedUsername)) {
            return KetQua.error("Tên đăng nhập '" + trimmedUsername + "' đã tồn tại.");
        }

        // Kiểm tra mỗi nhân viên chỉ có 1 tài khoản
        String normalizedMaNV = normalizeOptional(maNV);
        if (normalizedMaNV != null) {
            TaiKhoan existing = taiKhoanRepo.findByMaNV(normalizedMaNV);
            if (existing != null) {
                return KetQua.error("Nhân viên này đã có tài khoản ('" + existing.getTenDangNhap()
                        + "'). Mỗi nhân viên chỉ được có một tài khoản.");
            }
        }

        String hashedPassword = PasswordUtil.hashPassword(matKhau);
        int newId = taiKhoanRepo.insert(trimmedUsername, hashedPassword, normalizedMaNV, maVaiTro.trim(), email);

        if (newId < 0) {
            return KetQua.error("Không thể tạo tài khoản. Vui lòng thử lại.");
        }
        return KetQua.success(newId, "Tạo tài khoản thành công.");
    }

    /**
     * Cập nhật thông tin tài khoản.
     */
    public KetQua<Void> updateUser(TaiKhoan user) {
        TaiKhoan existing = findExistingUser(user);
        if (existing == null) {
            return KetQua.error("Không tìm thấy tài khoản.");
        }

        enforceAdminProtection(existing, user);

        try {
            taiKhoanRepo.update(user);
            // Cập nhật vai trò (lưu trực tiếp trong cột maVaiTro)
            updatePrimaryRole(user);
            return KetQua.success(null, "Cập nhật tài khoản thành công.");
        } catch (Exception e) {
            System.err.println("Lỗi updateUser: " + e.getMessage());
            e.printStackTrace();
            return KetQua.error("Không thể cập nhật tài khoản: " + e.getMessage());
        }
    }

    /**
     * Xóa tài khoản theo ID.
     */
    public KetQua<Void> deleteUser(int id) {
        TaiKhoan user = taiKhoanRepo.findById(id);
        if (user == null) {
            return KetQua.error("Không tìm thấy tài khoản.");
        }

        TaiKhoan current = SessionContext.getInstance().getCurrentUser();
        if (current != null && current.getId() == id && current.coVaiTro(ROLE_ADMIN)) {
            return KetQua.error("Không thể xóa tài khoản admin đang đăng nhập.");
        }
        if (USERNAME_ADMIN.equalsIgnoreCase(user.getTenDangNhap())) {
            return KetQua.error("Không thể xóa tài khoản admin hệ thống.");
        }

        try {
            taiKhoanRepo.delete(id);
            return KetQua.success(null, "Xóa tài khoản thành công.");
        } catch (Exception e) {
            System.err.println("Lỗi deleteUser: " + e.getMessage());
            e.printStackTrace();
            return KetQua.error("Không thể xóa tài khoản: " + e.getMessage());
        }
    }

    // =====================================================================
    // ==================== VaiTro Management ================================
    // =====================================================================

    public List<VaiTro> getAllRoles() {
        return taiKhoanRepo.findAllRoles();
    }

    public KetQua<Void> createRole(String maVaiTro, String tenVaiTro, String moTa) {
        if (isBlank(maVaiTro)) {
            return KetQua.error("Mã vai trò không được để trống.");
        }
        if (isBlank(tenVaiTro)) {
            return KetQua.error("Tên vai trò không được để trống.");
        }

        try {
            int rows = taiKhoanRepo.insertRole(maVaiTro.trim(), tenVaiTro.trim(), moTa);
            if (rows > 0) {
                return KetQua.success(null, "Tạo vai trò thành công.");
            }
            return KetQua.error("Không thể tạo vai trò. Mã vai trò có thể đã tồn tại.");
        } catch (Exception e) {
            System.err.println("Lỗi createRole: " + e.getMessage());
            e.printStackTrace();
            return KetQua.error("Không thể tạo vai trò: " + e.getMessage());
        }
    }

    public KetQua<Void> updateRole(VaiTro role) {
        if (role == null) {
            return KetQua.error("Thông tin vai trò không hợp lệ.");
        }
        try {
            taiKhoanRepo.updateRole(role);
            return KetQua.success(null, "Cập nhật vai trò thành công.");
        } catch (Exception e) {
            System.err.println("Lỗi updateRole: " + e.getMessage());
            e.printStackTrace();
            return KetQua.error("Không thể cập nhật vai trò: " + e.getMessage());
        }
    }

    public KetQua<Void> deleteRole(String code) {
        if (isBlank(code)) {
            return KetQua.error("Mã vai trò không hợp lệ.");
        }
        boolean deleted = taiKhoanRepo.deleteRole(code.trim());
        if (deleted) {
            return KetQua.success(null, "Xóa vai trò thành công.");
        }
        return KetQua.error("Không thể xóa vai trò. Vai trò có thể là hệ thống hoặc đang được sử dụng.");
    }

    // =====================================================================
    // ==================== Quyen Management ================================
    // =====================================================================

    public List<Quyen> getAllPermissions() {
        return taiKhoanRepo.findAllPermissions();
    }

    public KetQua<Void> setRolePermissions(String maVaiTro, List<Quyen> permissions) {
        if (isBlank(maVaiTro)) {
            return KetQua.error("Mã vai trò không hợp lệ.");
        }
        String normalizedRoleCode = maVaiTro.trim();
        if (ROLE_ADMIN.equalsIgnoreCase(normalizedRoleCode)) {
            return KetQua.error("Không thể chỉnh quyền vai trò ADMIN. ADMIN luôn toàn quyền.");
        }
        if (permissions == null) {
            return KetQua.error("Danh sách quyền không hợp lệ.");
        }

        try {
            taiKhoanRepo.setRolePermissions(normalizedRoleCode, permissions);
            return KetQua.success(null, "Cập nhật quyền cho vai trò thành công.");
        } catch (Exception e) {
            System.err.println("Lỗi setRolePermissions: " + e.getMessage());
            e.printStackTrace();
            return KetQua.error("Không thể cập nhật quyền vai trò: " + e.getMessage());
        }
    }

    /**
     * Tìm tài khoản theo mã nhân viên (String).
     */
    public TaiKhoan findByMaNV(String maNV) {
        if (isBlank(maNV)) {
            return null;
        }
        return taiKhoanRepo.findByMaNV(maNV);
    }

    /**
     * Tìm vai trò theo mã.
     */
    public VaiTro getRoleByCode(String code) {
        return taiKhoanRepo.findRoleByCode(code);
    }

    /**
     * Tạo vai trò kèm quyền (tùy chọn).
     */
    public KetQua<Void> createRoleWithPermissions(String maVaiTro, String tenVaiTro, List<Quyen> permissions) {
        KetQua<Void> createResult = createRole(maVaiTro, tenVaiTro, null);
        if (!createResult.isSuccess()) return createResult;

        if (permissions != null && !permissions.isEmpty()) {
            return setRolePermissions(maVaiTro, permissions);
        }
        return createResult;
    }

    /**
     * Gán vai trò cho tài khoản (ghi đè vai trò cũ).
     */
    public KetQua<Void> assignRoleToUser(int maTaiKhoan, String maVaiTro) {
        TaiKhoan target = taiKhoanRepo.findById(maTaiKhoan);
        if (target == null) {
            return KetQua.error("Không tìm thấy tài khoản.");
        }

        if (isProtectedAdminRoleChange(target, maVaiTro)) {
            return KetQua.error("Không thể thay đổi vai trò của tài khoản admin.");
        }

        try {
            taiKhoanRepo.updateRole(maTaiKhoan, maVaiTro);
            return KetQua.success(null, "Gán vai trò thành công.");
        } catch (Exception e) {
            return KetQua.error("Không thể gán vai trò: " + e.getMessage());
        }
    }

    /**
     * Tính quyền hiệu dụng theo RBAC động.
     */
    public Set<String> getEffectivePermissions(int maTaiKhoan) {
        TaiKhoan user = taiKhoanRepo.findById(maTaiKhoan);
        if (user == null) return new HashSet<>();

        Set<String> fromRoles = new HashSet<>();
        user.getVaiTros().forEach(role ->
                role.getQuyens().forEach(p -> fromRoles.add(p.getId()))
        );

        return fromRoles;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String normalizeOptional(String value) {
        if (isBlank(value)) return null;
        return value.trim();
    }

    private KetQua<Void> validateNewPassword(String newPass) {
        if (isBlank(newPass)) {
            return KetQua.error("Mật khẩu mới không được để trống.");
        }
        if (newPass.length() < MIN_PASSWORD_LENGTH) {
            return KetQua.error("Mật khẩu mới phải có ít nhất " + MIN_PASSWORD_LENGTH + " ký tự.");
        }
        return KetQua.success(null, "");
    }

    private KetQua<Void> updatePassword(int userId, String newPass, String successMsg, String failMsg) {
        String hashedNew = PasswordUtil.hashPassword(newPass);
        boolean updated = taiKhoanRepo.updatePassword(userId, hashedNew);
        if (updated) return KetQua.success(null, successMsg);
        return KetQua.error(failMsg);
    }

    private void enforceAdminProtection(TaiKhoan existing, TaiKhoan updateTarget) {
        if (!(isBuiltInAdminAccount(existing) || isCurrentAdminSelfUpdate(existing))) {
            return;
        }
        updateTarget.setHoatDong(true);
        updateTarget.setBiKhoa(false);
        VaiTro adminRole = getRoleByCode(ROLE_ADMIN);
        if (adminRole == null) return;
        List<VaiTro> fixedRoles = new ArrayList<>();
        fixedRoles.add(adminRole);
        updateTarget.setVaiTros(fixedRoles);
    }

    private void updatePrimaryRole(TaiKhoan user) {
        if (user.getVaiTros() == null || user.getVaiTros().isEmpty()) {
            return;
        }
        taiKhoanRepo.updateRole(user.getId(), user.getVaiTros().get(0).getId());
    }

    private TaiKhoan findExistingUser(TaiKhoan user) {
        if (user == null) return null;
        return taiKhoanRepo.findById(user.getId());
    }

    private boolean isProtectedAdminRoleChange(TaiKhoan target, String maVaiTro) {
        return !ROLE_ADMIN.equalsIgnoreCase(maVaiTro)
                && (isBuiltInAdminAccount(target) || isCurrentAdminSelfUpdate(target));
    }
}
