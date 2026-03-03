package com.hrm.bus;

import com.hrm.model.Quyen;
import com.hrm.model.VaiTro;
import com.hrm.model.TaiKhoan;
import com.hrm.dao.TaiKhoanDAO;
import com.hrm.util.PasswordUtil;
import com.hrm.util.SessionContext;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Authentication Service - xử lý đăng nhập, đăng xuất và quản lý tài khoản.
 * Sử dụng TaiKhoanDAO để truy xuất cơ sở dữ liệu thực.
 */
public class XacThucBUS {

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
        if (username == null || username.trim().isEmpty()) {
            return null;
        }
        if (password == null || password.isEmpty()) {
            return null;
        }

        TaiKhoan user = taiKhoanRepo.findByUsername(username.trim());

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
    public TaiKhoan getCurrentUser() {
        return SessionContext.getInstance().getCurrentUser();
    }

    /**
     * Kiểm tra người dùng hiện tại có quyền chỉ định không.
     */
    public boolean hasPermission(String permissionCode) {
        TaiKhoan user = getCurrentUser();
        return user != null && user.hasPermission(permissionCode);
    }

    /**
     * Kiểm tra người dùng hiện tại có vai trò chỉ định không.
     */
    public boolean hasRole(String roleCode) {
        TaiKhoan user = getCurrentUser();
        return user != null && user.hasRole(roleCode);
    }

    /**
     * Đổi mật khẩu cho người dùng.
     * Xác minh mật khẩu cũ trước khi cập nhật.
     *
     * @param userId   ID tài khoản
     * @param oldPass  mật khẩu cũ dạng plaintext
     * @param newPass  mật khẩu mới dạng plaintext
     * @return KetQua thành công hoặc thất bại kèm thông báo
     */
    public KetQua<Void> changePassword(int userId, String oldPass, String newPass) {
        if (oldPass == null || oldPass.isEmpty()) {
            return KetQua.error("Mật khẩu cũ không được để trống.");
        }
        if (newPass == null || newPass.length() < 6) {
            return KetQua.error("Mật khẩu mới phải có ít nhất 6 ký tự.");
        }

        TaiKhoan user = taiKhoanRepo.findById(userId);
        if (user == null) {
            return KetQua.error("Không tìm thấy tài khoản.");
        }

        if (!PasswordUtil.verifyPassword(oldPass, user.getPassword())) {
            return KetQua.error("Mật khẩu cũ không đúng.");
        }

        String hashedNew = PasswordUtil.hashPassword(newPass);
        boolean updated = taiKhoanRepo.updatePassword(userId, hashedNew);
        if (updated) {
            return KetQua.success(null, "Đổi mật khẩu thành công.");
        }
        return KetQua.error("Không thể cập nhật mật khẩu. Vui lòng thử lại.");
    }

    // =====================================================================
    // ==================== TaiKhoan Management ================================
    // =====================================================================

    /**
     * Lấy danh sách tất cả tài khoản.
     */
    public List<TaiKhoan> getAllUsers() {
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
     * @return KetQua chứa ID tài khoản mới hoặc lỗi
     */
    public KetQua<Integer> createUser(String tenDangNhap, String matKhau,
                                              Integer maNV, String maVaiTro, String email) {
        if (tenDangNhap == null || tenDangNhap.trim().isEmpty()) {
            return KetQua.error("Tên đăng nhập không được để trống.");
        }
        if (matKhau == null || matKhau.isEmpty()) {
            return KetQua.error("Mật khẩu không được để trống.");
        }
        if (maVaiTro == null || maVaiTro.trim().isEmpty()) {
            return KetQua.error("Vai trò không được để trống.");
        }

        String trimmedUsername = tenDangNhap.trim();
        if (taiKhoanRepo.existsByUsername(trimmedUsername)) {
            return KetQua.error("Tên đăng nhập '" + trimmedUsername + "' đã tồn tại.");
        }

        // Kiểm tra mỗi nhân viên chỉ được có 1 tài khoản
        if (maNV != null && maNV > 0) {
            TaiKhoan existing = taiKhoanRepo.findByMaNV(maNV);
            if (existing != null) {
                return KetQua.error("Nhân viên này đã có tài khoản ('" + existing.getUsername()
                        + "'). Mỗi nhân viên chỉ được có một tài khoản.");
            }
        }

        String hashedPassword = PasswordUtil.hashPassword(matKhau);
        int newId = taiKhoanRepo.insert(trimmedUsername, hashedPassword, maNV, maVaiTro.trim(), email);

        if (newId < 0) {
            return KetQua.error("Không thể tạo tài khoản. Vui lòng thử lại.");
        }
        return KetQua.success(newId, "Tạo tài khoản thành công.");
    }

    /**
     * Cập nhật thông tin tài khoản.
     *
     * @param user đối tượng TaiKhoan với thông tin đã được chỉnh sửa
     * @return KetQua thành công hoặc lỗi
     */
    public KetQua<Void> updateUser(TaiKhoan user) {
        if (user == null) {
            return KetQua.error("Thông tin tài khoản không hợp lệ.");
        }
        try {
            taiKhoanRepo.update(user);
            return KetQua.success(null, "Cập nhật tài khoản thành công.");
        } catch (Exception e) {
            System.err.println("Lỗi updateUser: " + e.getMessage());
            e.printStackTrace();
            return KetQua.error("Không thể cập nhật tài khoản: " + e.getMessage());
        }
    }

    /**
     * Xóa tài khoản theo ID.
     *
     * @param id ID tài khoản cần xóa
     * @return KetQua thành công hoặc lỗi
     */
    public KetQua<Void> deleteUser(int id) {
        TaiKhoan user = taiKhoanRepo.findById(id);
        if (user == null) {
            return KetQua.error("Không tìm thấy tài khoản.");
        }
        if ("admin".equalsIgnoreCase(user.getUsername())) {
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

    /**
     * Lấy danh sách tất cả vai trò.
     */
    public List<VaiTro> getAllRoles() {
        return taiKhoanRepo.findAllRoles();
    }

    /**
     * Tạo vai trò mới.
     *
     * @param maVaiTro  mã vai trò
     * @param tenVaiTro tên vai trò
     * @param moTa      mô tả
     * @return KetQua thành công hoặc lỗi
     */
    public KetQua<Void> createRole(String maVaiTro, String tenVaiTro, String moTa) {
        if (maVaiTro == null || maVaiTro.trim().isEmpty()) {
            return KetQua.error("Mã vai trò không được để trống.");
        }
        if (tenVaiTro == null || tenVaiTro.trim().isEmpty()) {
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

    /**
     * Cập nhật thông tin vai trò.
     *
     * @param role đối tượng VaiTro với thông tin đã được chỉnh sửa
     * @return KetQua thành công hoặc lỗi
     */
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

    /**
     * Xóa vai trò theo mã.
     * Không thể xóa vai trò hệ thống hoặc vai trò đang được sử dụng.
     *
     * @param code mã vai trò cần xóa
     * @return KetQua thành công hoặc lỗi
     */
    public KetQua<Void> deleteRole(String code) {
        if (code == null || code.trim().isEmpty()) {
            return KetQua.error("Mã vai trò không hợp lệ.");
        }
        boolean deleted = taiKhoanRepo.deleteRole(code.trim());
        if (deleted) {
            return KetQua.success(null, "Xóa vai trò thành công.");
        }
        return KetQua.error("Không thể xóa vai trò. Vai trò có thể là vai trò hệ thống hoặc đang được sử dụng.");
    }

    // =====================================================================
    // ==================== Quyen Management ==========================
    // =====================================================================

    /**
     * Lấy danh sách tất cả quyền.
     */
    public List<Quyen> getAllPermissions() {
        return taiKhoanRepo.findAllPermissions();
    }

    /**
     * Gán danh sách quyền mới cho vai trò (xóa cũ, thêm mới).
     *
     * @param maVaiTro        mã vai trò
     * @param permissionCodes danh sách mã quyền mới
     * @return KetQua thành công hoặc lỗi
     */
    public KetQua<Void> setRolePermissions(String maVaiTro, List<String> permissionCodes) {
        if (maVaiTro == null || maVaiTro.trim().isEmpty()) {
            return KetQua.error("Mã vai trò không hợp lệ.");
        }
        if (permissionCodes == null) {
            return KetQua.error("Danh sách quyền không hợp lệ.");
        }
        try {
            taiKhoanRepo.setRolePermissions(maVaiTro.trim(), permissionCodes);
            return KetQua.success(null, "Cập nhật quyền cho vai trò thành công.");
        } catch (Exception e) {
            System.err.println("Lỗi setRolePermissions: " + e.getMessage());
            e.printStackTrace();
            return KetQua.error("Không thể cập nhật quyền vai trò: " + e.getMessage());
        }
    }

    /**
     * Thêm hoặc cập nhật quyền đặc biệt cho một tài khoản.
     *
     * @param maTaiKhoan ID tài khoản
     * @param maQuyen    mã quyền
     * @param choPhep    true = cấp thêm, false = thu hồi
     * @return KetQua thành công hoặc lỗi
     */
    public KetQua<Void> setUserPermission(int maTaiKhoan, String maQuyen, boolean choPhep) {
        if (maQuyen == null || maQuyen.trim().isEmpty()) {
            return KetQua.error("Mã quyền không hợp lệ.");
        }
        try {
            taiKhoanRepo.setUserPermission(maTaiKhoan, maQuyen.trim(), choPhep);
            return KetQua.success(null, "Cập nhật quyền tài khoản thành công.");
        } catch (Exception e) {
            System.err.println("Lỗi setUserPermission: " + e.getMessage());
            e.printStackTrace();
            return KetQua.error("Không thể cập nhật quyền tài khoản: " + e.getMessage());
        }
    }

    /**
     * Xóa quyền đặc biệt của tài khoản (quay về quyền theo vai trò).
     *
     * @param maTaiKhoan ID tài khoản
     * @param maQuyen    mã quyền
     * @return KetQua thành công hoặc lỗi
     */
    public KetQua<Void> removeUserPermission(int maTaiKhoan, String maQuyen) {
        if (maQuyen == null || maQuyen.trim().isEmpty()) {
            return KetQua.error("Mã quyền không hợp lệ.");
        }
        try {
            taiKhoanRepo.removeUserPermission(maTaiKhoan, maQuyen.trim());
            return KetQua.success(null, "Đã gỡ quyền đặc biệt của tài khoản.");
        } catch (Exception e) {
            System.err.println("Lỗi removeUserPermission: " + e.getMessage());
            e.printStackTrace();
            return KetQua.error("Không thể gỡ quyền tài khoản: " + e.getMessage());
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
    public com.hrm.model.TaiKhoan findByMaNV(int maNV) {
        return taiKhoanRepo.findByMaNV(maNV);
    }

    /**
     * Tìm vai trò theo mã.
     */
    public com.hrm.model.VaiTro getRoleByCode(String code) {
        return taiKhoanRepo.findRoleByCode(code);
    }

    /**
     * Tạo vai trò và gán danh sách quyền.
     */
    public KetQua<Void> createRoleWithPermissions(String maVaiTro, String tenVaiTro, java.util.List<String> permissions) {
        KetQua<Void> createResult = createRole(maVaiTro, tenVaiTro, null);
        if (!createResult.isSuccess()) return createResult;
        if (permissions != null && !permissions.isEmpty()) {
            return setRolePermissions(maVaiTro, permissions);
        }
        return createResult;
    }

    /**
     * Gán vai trò cho tài khoản (ghi đè vai trò cũ — single-role).
     */
    public KetQua<Void> assignRoleToUser(int maTaiKhoan, String maVaiTro) {
        try {
            taiKhoanRepo.updateRole(maTaiKhoan, maVaiTro);
            return KetQua.success(null, "Gán vai trò thành công.");
        } catch (Exception e) {
            return KetQua.error("Không thể gán vai trò: " + e.getMessage());
        }
    }

    /**
     * Tính quyền hiệu dụng theo công thức RBAC động:
     * Quyền hiệu dụng = (Quyền từ vai trò) ∪ (TaiKhoanQuyen choPhep=true) − (TaiKhoanQuyen choPhep=false)
     *
     * @param maTaiKhoan ID tài khoản
     * @return tập mã quyền hiệu dụng
     */
    public Set<String> getEffectivePermissions(int maTaiKhoan) {
        TaiKhoan user = taiKhoanRepo.findById(maTaiKhoan);
        if (user == null) return new HashSet<>();

        // Bước 1: Tập quyền từ vai trò
        Set<String> fromRoles = new HashSet<>();
        user.getRoles().forEach(role ->
            role.getQuyens().forEach(p -> fromRoles.add(p.getCode()))
        );

        // Bước 2: Áp dụng TaiKhoanQuyen overrides
        Map<String, Boolean> overrides = user.getNgoaiLeQuyen();
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
