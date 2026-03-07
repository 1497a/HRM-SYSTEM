package com.hrm.dao;

import com.hrm.model.Quyen;
import com.hrm.model.VaiTro;
import com.hrm.model.TaiKhoan;
import com.hrm.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Repository JDBC cho bảng TAIKHOAN, VAITRO, QUYEN, VAITRO_QUYEN, TAIKHOAN_QUYEN.
 * Đã cập nhật để maNV là String (dạng "NV001", "NV002", ...).
 */
public class TaiKhoanDAO {

    // =====================================================================
    // ==================== TaiKhoan (TAIKHOAN) Methods ========================
    // =====================================================================

    /**
     * Tìm người dùng theo tên đăng nhập, nạp đầy đủ vai trò và quyền.
     */
    public TaiKhoan findByUsername(String username) {
        if (username == null || username.trim().isEmpty()) return null;

        String sql = "SELECT maTaiKhoan, tenDangNhap, matKhau, email, hoatDong, biKhoa, maNV "
                   + "FROM TAIKHOAN WHERE tenDangNhap = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    TaiKhoan user = mapRowToUser(rs);
                    loadUserRoleAndPermissions(user, user.getId());
                    return user;
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi findByUsername '" + username + "': " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Tìm người dùng theo ID tài khoản (maTaiKhoan - int).
     */
    public TaiKhoan findById(int id) {
        String sql = "SELECT maTaiKhoan, tenDangNhap, matKhau, email, hoatDong, biKhoa, maNV "
                   + "FROM TAIKHOAN WHERE maTaiKhoan = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    TaiKhoan user = mapRowToUser(rs);
                    loadUserRoleAndPermissions(user, user.getId());
                    return user;
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi findById " + id + ": " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Lấy tất cả người dùng, nạp vai trò và quyền.
     */
    public List<TaiKhoan> findAll() {
        List<TaiKhoan> list = new ArrayList<>();
        String sql = "SELECT maTaiKhoan, tenDangNhap, matKhau, email, hoatDong, biKhoa, maNV "
                   + "FROM TAIKHOAN ORDER BY maTaiKhoan";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                TaiKhoan user = mapRowToUser(rs);
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
     * Thêm tài khoản mới, trả về ID được sinh ra (maTaiKhoan - int).
     * maNV là String (có thể null).
     */
    public int insert(String tenDangNhap, String matKhau, String maNV, String maVaiTro, String email) {
        String sql = "INSERT INTO TAIKHOAN (tenDangNhap, matKhau, maNV, maVaiTro, email, hoatDong, biKhoa, ngayTao, ngayCapNhat) "
                   + "VALUES (?, ?, ?, ?, ?, TRUE, FALSE, NOW(), NOW())";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, tenDangNhap.trim());
            ps.setString(2, matKhau);
            if (maNV != null && !maNV.trim().isEmpty()) {
                ps.setString(3, maNV.trim());
            } else {
                ps.setNull(3, Types.VARCHAR);
            }
            ps.setString(4, maVaiTro != null ? maVaiTro.trim() : null);
            ps.setString(5, email != null ? email.trim() : null);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi insert tài khoản '" + tenDangNhap + "': " + e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Cập nhật thông tin tài khoản (hoatDong, biKhoa, email).
     * Vai trò được cập nhật riêng qua updateRole().
     */
    public void update(TaiKhoan user) {
        String sql = "UPDATE TAIKHOAN SET hoatDong = ?, biKhoa = ?, email = ?, ngayCapNhat = NOW() "
                   + "WHERE maTaiKhoan = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, user.isHoatDong());
            ps.setBoolean(2, user.isBiKhoa());
            ps.setString(3, user.getEmail());
            ps.setInt(4, user.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Lỗi update tài khoản #" + user.getId() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Cập nhật mật khẩu (đã hash sẵn).
     */
    public boolean updatePassword(int id, String hashedPassword) {
        String sql = "UPDATE TAIKHOAN SET matKhau = ?, ngayCapNhat = NOW() WHERE maTaiKhoan = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, hashedPassword);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi updatePassword #" + id + ": " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Xóa tài khoản theo ID (xóa liên kết quyền trước).
     */
    public void delete(int id) {
        String delPerms = "DELETE FROM TAIKHOAN_QUYEN WHERE maTaiKhoan = ?";
        String delUser  = "DELETE FROM TAIKHOAN WHERE maTaiKhoan = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Xóa quyền liên kết
                try (PreparedStatement ps = conn.prepareStatement(delPerms)) {
                    ps.setInt(1, id);
                    ps.executeUpdate();
                }
                // Xóa tài khoản
                try (PreparedStatement ps = conn.prepareStatement(delUser)) {
                    ps.setInt(1, id);
                    ps.executeUpdate();
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi delete tài khoản #" + id + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Tìm tài khoản theo maNV (String).
     */
    public TaiKhoan findByMaNV(String maNV) {
        if (maNV == null || maNV.trim().isEmpty()) return null;

        String sql = "SELECT maTaiKhoan, tenDangNhap, matKhau, email, hoatDong, biKhoa, maNV "
                   + "FROM TAIKHOAN WHERE maNV = ? LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maNV.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    TaiKhoan user = mapRowToUser(rs);
                    loadUserRoleAndPermissions(user, user.getId());
                    return user;
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi findByMaNV '" + maNV + "': " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Cập nhật vai trò cho tài khoản.
     */
    public void updateRole(int maTaiKhoan, String maVaiTro) {
        String sql = "UPDATE TAIKHOAN SET maVaiTro = ?, ngayCapNhat = NOW() WHERE maTaiKhoan = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maVaiTro != null ? maVaiTro.trim() : null);
            ps.setInt(2, maTaiKhoan);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Lỗi updateRole #" + maTaiKhoan + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Vô hiệu hóa tài khoản liên kết với nhân viên (khi NV nghỉ việc).
     */
    public void deactivateByMaNV(String maNV) {
        if (maNV == null || maNV.trim().isEmpty()) return;

        String sql = "UPDATE TAIKHOAN SET hoatDong = FALSE, ngayCapNhat = NOW() WHERE maNV = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maNV.trim());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Lỗi deactivateByMaNV '" + maNV + "': " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Kiểm tra tên đăng nhập đã tồn tại chưa.
     */
    public boolean existsByUsername(String username) {
        if (username == null || username.trim().isEmpty()) return false;

        String sql = "SELECT 1 FROM TAIKHOAN WHERE tenDangNhap = ? LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Lỗi existsByUsername '" + username + "': " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // =====================================================================
    // ==================== VaiTro (VAITRO) Methods ==========================
    // =====================================================================

    public List<VaiTro> findAllRoles() {
        List<VaiTro> list = new ArrayList<>();
        String sql = "SELECT maVaiTro, tenVaiTro, moTa, laVaiTroHeThong FROM VAITRO "
                   + "WHERE trangThai = TRUE OR trangThai IS NULL ORDER BY maVaiTro";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                VaiTro role = new VaiTro(
                        rs.getString("maVaiTro"),
                        rs.getString("tenVaiTro"),
                        rs.getString("moTa")
                );
                role.setLaHeThong(rs.getBoolean("laVaiTroHeThong"));
                // Nạp quyền
                List<Quyen> perms = findPermissionsByRole(role.getId());
                role.getQuyens().addAll(perms);
                list.add(role);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi findAllRoles: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    public VaiTro findRoleByCode(String code) {
        if (code == null || code.trim().isEmpty()) return null;

        String sql = "SELECT maVaiTro, tenVaiTro, moTa, laVaiTroHeThong FROM VAITRO WHERE maVaiTro = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    VaiTro role = new VaiTro(
                            rs.getString("maVaiTro"),
                            rs.getString("tenVaiTro"),
                            rs.getString("moTa")
                    );
                    role.setLaHeThong(rs.getBoolean("laVaiTroHeThong"));
                    role.getQuyens().addAll(findPermissionsByRole(code));
                    return role;
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi findRoleByCode '" + code + "': " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public int insertRole(String maVaiTro, String tenVaiTro, String moTa) {
        String sql = "INSERT INTO VAITRO (maVaiTro, tenVaiTro, moTa, laVaiTroHeThong, trangThai) "
                   + "VALUES (?, ?, ?, FALSE, TRUE)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maVaiTro.trim());
            ps.setString(2, tenVaiTro.trim());
            ps.setString(3, moTa != null ? moTa.trim() : null);
            return ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Lỗi insertRole '" + maVaiTro + "': " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    public void updateRole(VaiTro role) {
        if (role == null || role.getId() == null) return;

        String sql = "UPDATE VAITRO SET tenVaiTro = ?, moTa = ? WHERE maVaiTro = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, role.getTenVaiTro());
            ps.setString(2, role.getMoTa());
            ps.setString(3, role.getId().trim());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Lỗi updateRole '" + role.getId() + "': " + e.getMessage());
            e.printStackTrace();
        }
    }

    public boolean deleteRole(String code) {
        if (code == null || code.trim().isEmpty()) return false;

        String checkSystem = "SELECT laVaiTroHeThong FROM VAITRO WHERE maVaiTro = ?";
        String checkInUse  = "SELECT COUNT(*) FROM TAIKHOAN WHERE maVaiTro = ?";
        String delPerms    = "DELETE FROM VAITRO_QUYEN WHERE maVaiTro = ?";
        String delRole     = "DELETE FROM VAITRO WHERE maVaiTro = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            // Kiểm tra vai trò hệ thống
            try (PreparedStatement ps = conn.prepareStatement(checkSystem)) {
                ps.setString(1, code.trim());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getBoolean(1)) {
                        System.err.println("Không thể xóa vai trò hệ thống: " + code);
                        return false;
                    }
                }
            }

            // Kiểm tra đang được sử dụng
            try (PreparedStatement ps = conn.prepareStatement(checkInUse)) {
                ps.setString(1, code.trim());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        System.err.println("Vai trò đang được sử dụng, không thể xóa: " + code);
                        return false;
                    }
                }
            }

            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = conn.prepareStatement(delPerms)) {
                    ps.setString(1, code.trim());
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement(delRole)) {
                    ps.setString(1, code.trim());
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
            System.err.println("Lỗi deleteRole '" + code + "': " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // =====================================================================
    // ==================== Quyen (QUYEN) Methods ==========================
    // =====================================================================

    public List<Quyen> findAllPermissions() {
        List<Quyen> list = new ArrayList<>();
        String sql = "SELECT maQuyen, tenQuyen, nhomQuyen FROM QUYEN ORDER BY nhomQuyen, maQuyen";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Quyen(
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

    public List<Quyen> findPermissionsByRole(String maVaiTro) {
        if (maVaiTro == null || maVaiTro.trim().isEmpty()) return new ArrayList<>();

        List<Quyen> list = new ArrayList<>();
        String sql = "SELECT q.maQuyen, q.tenQuyen, q.nhomQuyen "
                   + "FROM QUYEN q JOIN VAITRO_QUYEN vq ON q.maQuyen = vq.maQuyen "
                   + "WHERE vq.maVaiTro = ? ORDER BY q.nhomQuyen, q.maQuyen";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maVaiTro.trim());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Quyen(
                            rs.getString("maQuyen"),
                            rs.getString("tenQuyen"),
                            rs.getString("nhomQuyen")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi findPermissionsByRole '" + maVaiTro + "': " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    public void setRolePermissions(String maVaiTro, List<String> permissionCodes) {
        if (maVaiTro == null || maVaiTro.trim().isEmpty()) return;

        String deleteSql = "DELETE FROM VAITRO_QUYEN WHERE maVaiTro = ?";
        String insertSql = "INSERT INTO VAITRO_QUYEN (maVaiTro, maQuyen) VALUES (?, ?)";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Xóa quyền cũ
                try (PreparedStatement ps = conn.prepareStatement(deleteSql)) {
                    ps.setString(1, maVaiTro.trim());
                    ps.executeUpdate();
                }
                // Thêm quyền mới
                if (permissionCodes != null && !permissionCodes.isEmpty()) {
                    try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                        for (String maQuyen : permissionCodes) {
                            if (maQuyen != null && !maQuyen.trim().isEmpty()) {
                                ps.setString(1, maVaiTro.trim());
                                ps.setString(2, maQuyen.trim());
                                ps.addBatch();
                            }
                        }
                        ps.executeBatch();
                    }
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi setRolePermissions cho vai trò '" + maVaiTro + "': " + e.getMessage());
            e.printStackTrace();
        }
    }

    // =====================================================================
    // ==================== Private Helpers ================================
    // =====================================================================

    private TaiKhoan mapRowToUser(ResultSet rs) throws SQLException {
        int id = rs.getInt("maTaiKhoan");
        String username = rs.getString("tenDangNhap");
        String password = rs.getString("matKhau");
        String email = rs.getString("email");
        boolean hoatDong = rs.getBoolean("hoatDong");
        boolean biKhoa = rs.getBoolean("biKhoa");
        String maNV = rs.getString("maNV");  // String

        // Lấy họ tên nếu có
        String fullName = resolveFullName(maNV, username);

        TaiKhoan user = new TaiKhoan(id, username, password, fullName, email);
        user.setHoatDong(hoatDong);
        user.setBiKhoa(biKhoa);
        if (maNV != null && !maNV.trim().isEmpty()) {
            user.setNhanVienId(maNV.trim());
        }
        return user;
    }

    private void loadUserRoleAndPermissions(TaiKhoan user, int maTaiKhoan) {
        // Lấy vai trò (giả sử mỗi tài khoản chỉ có 1 vai trò)
        String sqlRole = "SELECT maVaiTro FROM TAIKHOAN WHERE maTaiKhoan = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlRole)) {
            ps.setInt(1, maTaiKhoan);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String maVaiTro = rs.getString("maVaiTro");
                    if (maVaiTro != null && !maVaiTro.trim().isEmpty()) {
                        VaiTro role = findRoleByCode(maVaiTro.trim());
                        if (role != null) {
                            user.getVaiTros().add(role);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi loadUserRoleAndPermissions (role) #" + maTaiKhoan + ": " + e.getMessage());
        }
    }

    private String resolveFullName(String maNV, String fallback) {
        if (maNV == null || maNV.trim().isEmpty()) return fallback;

        String sql = "SELECT hoTen FROM THONGTINCANHAN WHERE maNV = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maNV.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String hoTen = rs.getString("hoTen");
                    if (hoTen != null && !hoTen.trim().isEmpty()) {
                        return hoTen.trim();
                    }
                }
            }
        } catch (SQLException ignored) {
            // Không báo lỗi nặng nếu bảng THONGTINCANHAN chưa có
        }
        return fallback;
    }
}