package com.hrm.service;

import com.hrm.model.Permission;
import com.hrm.model.Role;
import com.hrm.model.User;
import com.hrm.repo.TaiKhoanRepository;
import com.hrm.util.PasswordUtil;
import com.hrm.util.SessionContext;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Authentication Service - xử lý đăng nhập, đăng xuất và quản lý tài khoản.
 * Sử dụng TaiKhoanRepository để truy xuất cơ sở dữ liệu thực.
 */
public class AuthService {

    private static AuthService instance;
    private final TaiKhoanRepository taiKhoanRepo;

    private AuthService() {
        this.taiKhoanRepo = new TaiKhoanRepository();
    }

    public static synchronized AuthService getInstance() {
        if (instance == null) {
            instance = new AuthService();
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
     * @return User nếu thành công, null nếu thất bại
     */
    public User authenticate(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            return null;
        }
        if (password == null || password.isEmpty()) {
            return null;
        }

        User user = taiKhoanRepo.findByUsername(username.trim());

        if (user == null) {
            return null;
        }

        if (!user.isActive()) {
            return null;
        }

        if (user.isLocked()) {
            return null;
        }

        if (!PasswordUtil.verifyPassword(password, user.getPassword())) {
            return null;
        }

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
    public User getCurrentUser() {
        return SessionContext.getInstance().getCurrentUser();
    }

    /**
     * Kiểm tra người dùng hiện tại có quyền chỉ định không.
     */
    public boolean hasPermission(String permissionCode) {
        User user = getCurrentUser();
        return user != null && user.hasPermission(permissionCode);
    }

    /**
     * Kiểm tra người dùng hiện tại có vai trò chỉ định không.
     */
    public boolean hasRole(String roleCode) {
        User user = getCurrentUser();
        return user != null && user.hasRole(roleCode);
    }

    /**
     * Đổi mật khẩu cho người dùng.
     * Xác minh mật khẩu cũ trước khi cập nhật.
     *
     * @param userId   ID tài khoản
     * @param oldPass  mật khẩu cũ dạng plaintext
     * @param newPass  mật khẩu mới dạng plaintext
     * @return ServiceResult thành công hoặc thất bại kèm thông báo
     */
    public ServiceResult<Void> changePassword(int userId, String oldPass, String newPass) {
        if (oldPass == null || oldPass.isEmpty()) {
            return ServiceResult.error("Mật khẩu cũ không được để trống.");
        }
        if (newPass == null || newPass.length() < 6) {
            return ServiceResult.error("Mật khẩu mới phải có ít nhất 6 ký tự.");
        }

        User user = taiKhoanRepo.findById(userId);
        if (user == null) {
            return ServiceResult.error("Không tìm thấy tài khoản.");
        }

        if (!PasswordUtil.verifyPassword(oldPass, user.getPassword())) {
            return ServiceResult.error("Mật khẩu cũ không đúng.");
        }

        String hashedNew = PasswordUtil.hashPassword(newPass);
        boolean updated = taiKhoanRepo.updatePassword(userId, hashedNew);
        if (updated) {
            return ServiceResult.success(null, "Đổi mật khẩu thành công.");
        }
        return ServiceResult.error("Không thể cập nhật mật khẩu. Vui lòng thử lại.");
    }

    // =====================================================================
    // ==================== User Management ================================
    // =====================================================================

    /**
     * Lấy danh sách tất cả tài khoản.
     */
    public List<User> getAllUsers() {
        return taiKhoanRepo.findAll();
    }

    /**
     * Tạo tài khoản mới.
     *
     * @param tenDangNhap tên đăng nhập
     * @param matKhau     mật khẩu dạng plaintext (sẽ được hash)
     * @param maNV        mã nhân viên liên kết (có thể null)
     * @param maVaiTro    mã vai trò
     * @param email       địa chỉ email
     * @return ServiceResult chứa ID tài khoản mới hoặc lỗi
     */
    public ServiceResult<Integer> createUser(String tenDangNhap, String matKhau,
                                              Integer maNV, String maVaiTro, String email) {
        if (tenDangNhap == null || tenDangNhap.trim().isEmpty()) {
            return ServiceResult.error("Tên đăng nhập không được để trống.");
        }
        if (matKhau == null || matKhau.isEmpty()) {
            return ServiceResult.error("Mật khẩu không được để trống.");
        }
        if (maVaiTro == null || maVaiTro.trim().isEmpty()) {
            return ServiceResult.error("Vai trò không được để trống.");
        }

        String trimmedUsername = tenDangNhap.trim();
        if (taiKhoanRepo.existsByUsername(trimmedUsername)) {
            return ServiceResult.error("Tên đăng nhập '" + trimmedUsername + "' đã tồn tại.");
        }

        String hashedPassword = PasswordUtil.hashPassword(matKhau);
        int newId = taiKhoanRepo.insert(trimmedUsername, hashedPassword, maNV, maVaiTro.trim(), email);

        if (newId < 0) {
            return ServiceResult.error("Không thể tạo tài khoản. Vui lòng thử lại.");
        }
        return ServiceResult.success(newId, "Tạo tài khoản thành công.");
    }

    /**
     * Cập nhật thông tin tài khoản.
     *
     * @param user đối tượng User với thông tin đã được chỉnh sửa
     * @return ServiceResult thành công hoặc lỗi
     */
    public ServiceResult<Void> updateUser(User user) {
        if (user == null) {
            return ServiceResult.error("Thông tin tài khoản không hợp lệ.");
        }
        try {
            taiKhoanRepo.update(user);
            return ServiceResult.success(null, "Cập nhật tài khoản thành công.");
        } catch (Exception e) {
            System.err.println("Lỗi updateUser: " + e.getMessage());
            e.printStackTrace();
            return ServiceResult.error("Không thể cập nhật tài khoản: " + e.getMessage());
        }
    }

    /**
     * Xóa tài khoản theo ID.
     *
     * @param id ID tài khoản cần xóa
     * @return ServiceResult thành công hoặc lỗi
     */
    public ServiceResult<Void> deleteUser(int id) {
        User user = taiKhoanRepo.findById(id);
        if (user == null) {
            return ServiceResult.error("Không tìm thấy tài khoản.");
        }
        if ("admin".equalsIgnoreCase(user.getUsername())) {
            return ServiceResult.error("Không thể xóa tài khoản admin hệ thống.");
        }
        try {
            taiKhoanRepo.delete(id);
            return ServiceResult.success(null, "Xóa tài khoản thành công.");
        } catch (Exception e) {
            System.err.println("Lỗi deleteUser: " + e.getMessage());
            e.printStackTrace();
            return ServiceResult.error("Không thể xóa tài khoản: " + e.getMessage());
        }
    }

    // =====================================================================
    // ==================== Role Management ================================
    // =====================================================================

    /**
     * Lấy danh sách tất cả vai trò.
     */
    public List<Role> getAllRoles() {
        return taiKhoanRepo.findAllRoles();
    }

    /**
     * Tạo vai trò mới.
     *
     * @param maVaiTro  mã vai trò
     * @param tenVaiTro tên vai trò
     * @param moTa      mô tả
     * @return ServiceResult thành công hoặc lỗi
     */
    public ServiceResult<Void> createRole(String maVaiTro, String tenVaiTro, String moTa) {
        if (maVaiTro == null || maVaiTro.trim().isEmpty()) {
            return ServiceResult.error("Mã vai trò không được để trống.");
        }
        if (tenVaiTro == null || tenVaiTro.trim().isEmpty()) {
            return ServiceResult.error("Tên vai trò không được để trống.");
        }
        try {
            int rows = taiKhoanRepo.insertRole(maVaiTro.trim(), tenVaiTro.trim(), moTa);
            if (rows > 0) {
                return ServiceResult.success(null, "Tạo vai trò thành công.");
            }
            return ServiceResult.error("Không thể tạo vai trò. Mã vai trò có thể đã tồn tại.");
        } catch (Exception e) {
            System.err.println("Lỗi createRole: " + e.getMessage());
            e.printStackTrace();
            return ServiceResult.error("Không thể tạo vai trò: " + e.getMessage());
        }
    }

    /**
     * Cập nhật thông tin vai trò.
     *
     * @param role đối tượng Role với thông tin đã được chỉnh sửa
     * @return ServiceResult thành công hoặc lỗi
     */
    public ServiceResult<Void> updateRole(Role role) {
        if (role == null) {
            return ServiceResult.error("Thông tin vai trò không hợp lệ.");
        }
        try {
            taiKhoanRepo.updateRole(role);
            return ServiceResult.success(null, "Cập nhật vai trò thành công.");
        } catch (Exception e) {
            System.err.println("Lỗi updateRole: " + e.getMessage());
            e.printStackTrace();
            return ServiceResult.error("Không thể cập nhật vai trò: " + e.getMessage());
        }
    }

    /**
     * Xóa vai trò theo mã.
     * Không thể xóa vai trò hệ thống hoặc vai trò đang được sử dụng.
     *
     * @param code mã vai trò cần xóa
     * @return ServiceResult thành công hoặc lỗi
     */
    public ServiceResult<Void> deleteRole(String code) {
        if (code == null || code.trim().isEmpty()) {
            return ServiceResult.error("Mã vai trò không hợp lệ.");
        }
        boolean deleted = taiKhoanRepo.deleteRole(code.trim());
        if (deleted) {
            return ServiceResult.success(null, "Xóa vai trò thành công.");
        }
        return ServiceResult.error("Không thể xóa vai trò. Vai trò có thể là vai trò hệ thống hoặc đang được sử dụng.");
    }

    // =====================================================================
    // ==================== Permission Management ==========================
    // =====================================================================

    /**
     * Lấy danh sách tất cả quyền.
     */
    public List<Permission> getAllPermissions() {
        return taiKhoanRepo.findAllPermissions();
    }

    /**
     * Gán danh sách quyền mới cho vai trò (xóa cũ, thêm mới).
     *
     * @param maVaiTro        mã vai trò
     * @param permissionCodes danh sách mã quyền mới
     * @return ServiceResult thành công hoặc lỗi
     */
    public ServiceResult<Void> setRolePermissions(String maVaiTro, List<String> permissionCodes) {
        if (maVaiTro == null || maVaiTro.trim().isEmpty()) {
            return ServiceResult.error("Mã vai trò không hợp lệ.");
        }
        if (permissionCodes == null) {
            return ServiceResult.error("Danh sách quyền không hợp lệ.");
        }
        try {
            taiKhoanRepo.setRolePermissions(maVaiTro.trim(), permissionCodes);
            return ServiceResult.success(null, "Cập nhật quyền cho vai trò thành công.");
        } catch (Exception e) {
            System.err.println("Lỗi setRolePermissions: " + e.getMessage());
            e.printStackTrace();
            return ServiceResult.error("Không thể cập nhật quyền vai trò: " + e.getMessage());
        }
    }

    /**
     * Thêm hoặc cập nhật quyền đặc biệt cho một tài khoản.
     *
     * @param maTaiKhoan ID tài khoản
     * @param maQuyen    mã quyền
     * @param choPhep    true = cấp thêm, false = thu hồi
     * @return ServiceResult thành công hoặc lỗi
     */
    public ServiceResult<Void> setUserPermission(int maTaiKhoan, String maQuyen, boolean choPhep) {
        if (maQuyen == null || maQuyen.trim().isEmpty()) {
            return ServiceResult.error("Mã quyền không hợp lệ.");
        }
        try {
            taiKhoanRepo.setUserPermission(maTaiKhoan, maQuyen.trim(), choPhep);
            return ServiceResult.success(null, "Cập nhật quyền tài khoản thành công.");
        } catch (Exception e) {
            System.err.println("Lỗi setUserPermission: " + e.getMessage());
            e.printStackTrace();
            return ServiceResult.error("Không thể cập nhật quyền tài khoản: " + e.getMessage());
        }
    }

    /**
     * Xóa quyền đặc biệt của tài khoản (quay về quyền theo vai trò).
     *
     * @param maTaiKhoan ID tài khoản
     * @param maQuyen    mã quyền
     * @return ServiceResult thành công hoặc lỗi
     */
    public ServiceResult<Void> removeUserPermission(int maTaiKhoan, String maQuyen) {
        if (maQuyen == null || maQuyen.trim().isEmpty()) {
            return ServiceResult.error("Mã quyền không hợp lệ.");
        }
        try {
            taiKhoanRepo.removeUserPermission(maTaiKhoan, maQuyen.trim());
            return ServiceResult.success(null, "Đã gỡ quyền đặc biệt của tài khoản.");
        } catch (Exception e) {
            System.err.println("Lỗi removeUserPermission: " + e.getMessage());
            e.printStackTrace();
            return ServiceResult.error("Không thể gỡ quyền tài khoản: " + e.getMessage());
        }
    }

    /**
     * Lấy các quyền đặc biệt (override) của một tài khoản.
     *
     * @param maTaiKhoan ID tài khoản
     * @return Map maQuyen -> choPhep
     */
    public Map<String, Boolean> getUserPermissions(int maTaiKhoan) {
        return taiKhoanRepo.findUserPermissions(maTaiKhoan);
    }

    /**
     * Tìm tài khoản theo maNV nhân viên.
     */
    public com.hrm.model.User findByMaNV(int maNV) {
        return taiKhoanRepo.findByMaNV(maNV);
    }

    /**
     * Tìm vai trò theo mã.
     */
    public com.hrm.model.Role getRoleByCode(String code) {
        return taiKhoanRepo.findRoleByCode(code);
    }

    /**
     * Tạo vai trò và gán danh sách quyền.
     */
    public ServiceResult<Void> createRoleWithPermissions(String maVaiTro, String tenVaiTro, java.util.List<String> permissions) {
        ServiceResult<Void> createResult = createRole(maVaiTro, tenVaiTro, null);
        if (!createResult.isSuccess()) return createResult;
        if (permissions != null && !permissions.isEmpty()) {
            return setRolePermissions(maVaiTro, permissions);
        }
        return createResult;
    }

    /**
     * Gán vai trò cho tài khoản (ghi đè vai trò cũ — single-role).
     */
    public ServiceResult<Void> assignRoleToUser(int maTaiKhoan, String maVaiTro) {
        try {
            taiKhoanRepo.updateRole(maTaiKhoan, maVaiTro);
            return ServiceResult.success(null, "Gán vai trò thành công.");
        } catch (Exception e) {
            return ServiceResult.error("Không thể gán vai trò: " + e.getMessage());
        }
    }

    /**
     * Tính quyền hiệu dụng theo công thức RBAC động:
     * Quyền hiệu dụng = (Quyền từ vai trò) ∪ (UserPermission choPhep=true) − (UserPermission choPhep=false)
     *
     * @param maTaiKhoan ID tài khoản
     * @return tập mã quyền hiệu dụng
     */
    public Set<String> getEffectivePermissions(int maTaiKhoan) {
        User user = taiKhoanRepo.findById(maTaiKhoan);
        if (user == null) return new HashSet<>();

        // Bước 1: Tập quyền từ vai trò
        Set<String> fromRoles = new HashSet<>();
        user.getRoles().forEach(role ->
            role.getPermissions().forEach(p -> fromRoles.add(p.getCode()))
        );

        // Bước 2: Áp dụng UserPermission overrides
        Map<String, Boolean> overrides = user.getUserPermissions();
        Set<String> effective = new HashSet<>(fromRoles);
        overrides.forEach((code, granted) -> {
            if (granted) {
                effective.add(code);    // explicitly granted
            } else {
                effective.remove(code); // explicitly denied
            }
        });

        return effective;
    }
}
