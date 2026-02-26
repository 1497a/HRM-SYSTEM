package com.hrm.repo;

import com.hrm.model.Permission;
import com.hrm.model.Role;
import com.hrm.model.User;
import com.hrm.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Repository JDBC cho bảng TAIKHOAN, VAITRO, QUYEN, VAITRO_QUYEN, TAIKHOAN_QUYEN.
 */
public class TaiKhoanRepository {

    // =====================================================================
    // ==================== User (TAIKHOAN) Methods ========================
    // =====================================================================

    /**
     * Tìm người dùng theo tên đăng nhập, nạp đầy đủ vai trò và quyền.
     */
    public User findByUsername(String username) {
        String sql = "SELECT maTaiKhoan, tenDangNhap, matKhau, email, hoatDong, biKhoa, maNV "
                   + "FROM TAIKHOAN WHERE tenDangNhap = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User user = mapRowToUser(rs);
                    loadUserRoleAndPermissions(user, user.getId());
                    return user;
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi findByUsername: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Tìm người dùng theo ID, nạp đầy đủ vai trò và quyền.
     */
    public User findById(int id) {
        String sql = "SELECT maTaiKhoan, tenDangNhap, matKhau, email, hoatDong, biKhoa, maNV "
                   + "FROM TAIKHOAN WHERE maTaiKhoan = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User user = mapRowToUser(rs);
                    loadUserRoleAndPermissions(user, user.getId());
                    return user;
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi findById: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Lấy tất cả người dùng cùng vai trò.
     */
    public List<User> findAll() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT maTaiKhoan, tenDangNhap, matKhau, email, hoatDong, biKhoa, maNV "
                   + "FROM TAIKHOAN ORDER BY maTaiKhoan";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                User user = mapRowToUser(rs);
                loadUserRoleAndPermissions(user, user.getId());
                list.add(user);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi findAll users: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Thêm tài khoản mới, trả về ID được sinh ra.
     */
    public int insert(String tenDangNhap, String matKhau, Integer maNV, String maVaiTro, String email) {
        String sql = "INSERT INTO TAIKHOAN (tenDangNhap, matKhau, maNV, maVaiTro, email, hoatDong, biKhoa, ngayTao, ngayCapNhat) "
                   + "VALUES (?, ?, ?, ?, ?, TRUE, FALSE, NOW(), NOW())";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, tenDangNhap);
            ps.setString(2, matKhau);
            if (maNV != null) {
                ps.setInt(3, maNV);
            } else {
                ps.setNull(3, Types.INTEGER);
            }
            ps.setString(4, maVaiTro);
            ps.setString(5, email);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi insert tài khoản: " + e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Cập nhật thông tin tài khoản (hoatDong, biKhoa, email, maVaiTro).
     */
    public void update(User user) {
        String maVaiTro = user.getRoles().isEmpty() ? null : user.getRoles().get(0).getCode();
        String sql = "UPDATE TAIKHOAN SET hoatDong = ?, biKhoa = ?, email = ?, maVaiTro = ?, ngayCapNhat = NOW() "
                   + "WHERE maTaiKhoan = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, user.isActive());
            ps.setBoolean(2, user.isLocked());
            ps.setString(3, user.getEmail());
            ps.setString(4, maVaiTro);
            ps.setInt(5, user.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Lỗi update tài khoản: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Cập nhật mật khẩu, trả về true nếu thành công.
     */
    public boolean updatePassword(int id, String newPassword) {
        String sql = "UPDATE TAIKHOAN SET matKhau = ?, ngayCapNhat = NOW() WHERE maTaiKhoan = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newPassword);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi updatePassword: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Xóa tài khoản theo ID.
     */
    public void delete(int id) {
        // Xóa các bản ghi liên quan trước
        String delUserPerms = "DELETE FROM TAIKHOAN_QUYEN WHERE maTaiKhoan = ?";
        String delAccount   = "DELETE FROM TAIKHOAN WHERE maTaiKhoan = ?";
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps1 = conn.prepareStatement(delUserPerms)) {
                    ps1.setInt(1, id);
                    ps1.executeUpdate();
                }
                try (PreparedStatement ps2 = conn.prepareStatement(delAccount)) {
                    ps2.setInt(1, id);
                    ps2.executeUpdate();
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi delete tài khoản: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Vô hiệu hóa tài khoản liên kết với nhân viên (khi NV nghỉ việc).
     */
    public void deactivateByMaNV(int maNV) {
        String sql = "UPDATE TAIKHOAN SET hoatDong = 0, ngayCapNhat = NOW() WHERE maNV = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maNV);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Lỗi deactivateByMaNV: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Kiểm tra tên đăng nhập đã tồn tại chưa.
     */
    public boolean existsByUsername(String username) {
        String sql = "SELECT 1 FROM TAIKHOAN WHERE tenDangNhap = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Lỗi existsByUsername: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // =====================================================================
    // ==================== Role (VAITRO) Methods ==========================
    // =====================================================================

    /**
     * Lấy tất cả vai trò.
     */
    public List<Role> findAllRoles() {
        List<Role> list = new ArrayList<>();
        String sql = "SELECT maVaiTro, tenVaiTro, moTa, laVaiTroHeThong FROM VAITRO WHERE trangThai = TRUE OR trangThai IS NULL ORDER BY maVaiTro";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Role role = new Role(
                        rs.getString("maVaiTro"),
                        rs.getString("tenVaiTro"),
                        rs.getString("moTa")
                );
                role.setSystemRole(rs.getBoolean("laVaiTroHeThong"));
                list.add(role);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi findAllRoles: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Tìm vai trò theo mã, nạp cả danh sách quyền.
     */
    public Role findRoleByCode(String code) {
        String sql = "SELECT maVaiTro, tenVaiTro, moTa, laVaiTroHeThong FROM VAITRO WHERE maVaiTro = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Role role = new Role(
                            rs.getString("maVaiTro"),
                            rs.getString("tenVaiTro"),
                            rs.getString("moTa")
                    );
                    role.setSystemRole(rs.getBoolean("laVaiTroHeThong"));
                    // Nạp quyền của vai trò
                    List<Permission> perms = findPermissionsByRole(code);
                    for (Permission p : perms) {
                        role.addPermission(p);
                    }
                    return role;
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi findRoleByCode: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Thêm vai trò mới, trả về số dòng bị ảnh hưởng.
     */
    public int insertRole(String maVaiTro, String tenVaiTro, String moTa) {
        String sql = "INSERT INTO VAITRO (maVaiTro, tenVaiTro, moTa, laVaiTroHeThong, trangThai) VALUES (?, ?, ?, FALSE, TRUE)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maVaiTro);
            ps.setString(2, tenVaiTro);
            ps.setString(3, moTa);
            return ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Lỗi insertRole: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Cập nhật vai trò.
     */
    public void updateRole(Role role) {
        String sql = "UPDATE VAITRO SET tenVaiTro = ?, moTa = ? WHERE maVaiTro = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, role.getName());
            ps.setString(2, role.getDescription());
            ps.setString(3, role.getCode());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Lỗi updateRole: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Xóa vai trò nếu không được gán cho người dùng nào và không phải vai trò hệ thống.
     * Trả về true nếu xóa thành công.
     */
    public boolean deleteRole(String code) {
        // Kiểm tra vai trò hệ thống
        String checkSystem = "SELECT laVaiTroHeThong FROM VAITRO WHERE maVaiTro = ?";
        // Kiểm tra có người dùng nào đang dùng không
        String checkInUse = "SELECT COUNT(*) FROM TAIKHOAN WHERE maVaiTro = ?";
        String deletePerms = "DELETE FROM VAITRO_QUYEN WHERE maVaiTro = ?";
        String deleteRole  = "DELETE FROM VAITRO WHERE maVaiTro = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            // Kiểm tra vai trò hệ thống
            try (PreparedStatement ps = conn.prepareStatement(checkSystem)) {
                ps.setString(1, code);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getBoolean(1)) {
                        System.err.println("Không thể xóa vai trò hệ thống: " + code);
                        return false;
                    }
                }
            }
            // Kiểm tra đang được sử dụng
            try (PreparedStatement ps = conn.prepareStatement(checkInUse)) {
                ps.setString(1, code);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        System.err.println("Vai trò đang được sử dụng, không thể xóa: " + code);
                        return false;
                    }
                }
            }

            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = conn.prepareStatement(deletePerms)) {
                    ps.setString(1, code);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement(deleteRole)) {
                    ps.setString(1, code);
                    int rows = ps.executeUpdate();
                    conn.commit();
                    return rows > 0;
                }
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi deleteRole: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // =====================================================================
    // ==================== Permission (QUYEN) Methods =====================
    // =====================================================================

    /**
     * Lấy tất cả quyền.
     */
    public List<Permission> findAllPermissions() {
        List<Permission> list = new ArrayList<>();
        String sql = "SELECT maQuyen, tenQuyen, nhomQuyen FROM QUYEN ORDER BY nhomQuyen, maQuyen";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Permission(
                        rs.getString("maQuyen"),
                        rs.getString("tenQuyen"),
                        rs.getString("nhomQuyen")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi findAllPermissions: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Lấy danh sách quyền của một vai trò.
     */
    public List<Permission> findPermissionsByRole(String maVaiTro) {
        List<Permission> list = new ArrayList<>();
        String sql = "SELECT q.maQuyen, q.tenQuyen, q.nhomQuyen "
                   + "FROM QUYEN q "
                   + "JOIN VAITRO_QUYEN vq ON q.maQuyen = vq.maQuyen "
                   + "WHERE vq.maVaiTro = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maVaiTro);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Permission(
                            rs.getString("maQuyen"),
                            rs.getString("tenQuyen"),
                            rs.getString("nhomQuyen")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi findPermissionsByRole: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Gán danh sách quyền mới cho vai trò (xóa cũ, thêm mới).
     */
    public void setRolePermissions(String maVaiTro, List<String> permissionCodes) {
        String deleteSql = "DELETE FROM VAITRO_QUYEN WHERE maVaiTro = ?";
        String insertSql = "INSERT INTO VAITRO_QUYEN (maVaiTro, maQuyen) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = conn.prepareStatement(deleteSql)) {
                    ps.setString(1, maVaiTro);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                    for (String maQuyen : permissionCodes) {
                        ps.setString(1, maVaiTro);
                        ps.setString(2, maQuyen);
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi setRolePermissions: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // =====================================================================
    // ==================== User Permission Exception Methods ==============
    // =====================================================================

    /**
     * Lấy các quyền đặc biệt (override) của tài khoản.
     * key = maQuyen, value = choPhep (true = cấp thêm, false = thu hồi)
     */
    public Map<String, Boolean> findUserPermissions(int maTaiKhoan) {
        Map<String, Boolean> map = new HashMap<>();
        String sql = "SELECT maQuyen, choPhep FROM TAIKHOAN_QUYEN WHERE maTaiKhoan = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maTaiKhoan);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    map.put(rs.getString("maQuyen"), rs.getBoolean("choPhep"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi findUserPermissions: " + e.getMessage());
            e.printStackTrace();
        }
        return map;
    }

    /**
     * Thêm hoặc cập nhật một quyền đặc biệt cho tài khoản.
     */
    public void setUserPermission(int maTaiKhoan, String maQuyen, boolean choPhep) {
        String sql = "INSERT INTO TAIKHOAN_QUYEN (maTaiKhoan, maQuyen, choPhep) VALUES (?, ?, ?) "
                   + "ON DUPLICATE KEY UPDATE choPhep = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maTaiKhoan);
            ps.setString(2, maQuyen);
            ps.setBoolean(3, choPhep);
            ps.setBoolean(4, choPhep);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Lỗi setUserPermission: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Xóa một quyền đặc biệt của tài khoản (quay về quyền theo vai trò).
     */
    public void removeUserPermission(int maTaiKhoan, String maQuyen) {
        String sql = "DELETE FROM TAIKHOAN_QUYEN WHERE maTaiKhoan = ? AND maQuyen = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maTaiKhoan);
            ps.setString(2, maQuyen);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Lỗi removeUserPermission: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // =====================================================================
    // ==================== Private Helpers ================================
    // =====================================================================

    /**
     * Map một dòng ResultSet sang đối tượng User (chưa nạp roles/permissions).
     * Cột fullName được lấy từ bảng THONGTINCANHAN qua JOIN nếu cần,
     * ở đây dùng tenDangNhap làm fallback cho fullName vì TAIKHOAN không có cột hoTen.
     */
    private User mapRowToUser(ResultSet rs) throws SQLException {
        int id = rs.getInt("maTaiKhoan");
        String username = rs.getString("tenDangNhap");
        String password = rs.getString("matKhau");
        String email = rs.getString("email");
        boolean hoatDong = rs.getBoolean("hoatDong");
        boolean biKhoa = rs.getBoolean("biKhoa");

        // Thử lấy họ tên từ THONGTINCANHAN nếu có cột maNV
        String fullName = resolveFullName(id, username);

        User user = new User(id, username, password, fullName, email);
        user.setActive(hoatDong);
        user.setLocked(biKhoa);
        int maNVVal = rs.getInt("maNV");
        if (!rs.wasNull()) user.setMaNV(maNVVal);
        return user;
    }

    /**
     * Nạp vai trò và quyền cho người dùng.
     * DB chỉ có một maVaiTro trên mỗi tài khoản.
     */
    private void loadUserRoleAndPermissions(User user, int maTaiKhoan) {
        // Lấy maVaiTro của tài khoản
        String sqlRole = "SELECT maVaiTro FROM TAIKHOAN WHERE maTaiKhoan = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlRole)) {
            ps.setInt(1, maTaiKhoan);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String maVaiTro = rs.getString("maVaiTro");
                    if (maVaiTro != null && !maVaiTro.isEmpty()) {
                        Role role = findRoleByCode(maVaiTro);
                        if (role != null) {
                            user.addRole(role);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi loadUserRoleAndPermissions (role): " + e.getMessage());
            e.printStackTrace();
        }

        // Nạp quyền đặc biệt của tài khoản
        Map<String, Boolean> userPerms = findUserPermissions(maTaiKhoan);
        user.setUserPermissions(userPerms);
    }

    /**
     * Cố gắng lấy họ tên nhân viên từ bảng THONGTINCANHAN thông qua maNV trong TAIKHOAN.
     * Nếu không có, trả về tên đăng nhập.
     */
    private String resolveFullName(int maTaiKhoan, String fallback) {
        String sql = "SELECT ttcn.hoTen "
                   + "FROM TAIKHOAN tk "
                   + "JOIN THONGTINCANHAN ttcn ON tk.maNV = ttcn.maNV "
                   + "WHERE tk.maTaiKhoan = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maTaiKhoan);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String hoTen = rs.getString("hoTen");
                    if (hoTen != null && !hoTen.isEmpty()) {
                        return hoTen;
                    }
                }
            }
        } catch (SQLException e) {
            // Bảng THONGTINCANHAN chưa tồn tại hoặc không có dữ liệu — không cần báo lỗi nghiêm trọng
            System.err.println("Không thể lấy họ tên từ THONGTINCANHAN: " + e.getMessage());
        }
        return fallback;
    }
}
