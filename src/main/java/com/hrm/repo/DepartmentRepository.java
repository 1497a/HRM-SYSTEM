package com.hrm.repo;

import com.hrm.model.Department;
import com.hrm.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository JDBC cho bảng PHONGBAN.
 */
public class DepartmentRepository {

    /**
     * Lấy tất cả phòng ban.
     */
    public List<Department> findAll() {
        List<Department> list = new ArrayList<>();
        String sql = "SELECT maPhongBan, tenPhongBan, phongBanCha, trangThai FROM PHONGBAN ORDER BY maPhongBan";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi DepartmentRepository.findAll: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Tìm phòng ban theo mã.
     */
    public Department findById(String maPhongBan) {
        String sql = "SELECT maPhongBan, tenPhongBan, phongBanCha, trangThai FROM PHONGBAN WHERE maPhongBan = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maPhongBan);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi DepartmentRepository.findById: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Lấy danh sách phòng ban con trực tiếp.
     */
    public List<Department> findChildren(String maPhongBan) {
        List<Department> list = new ArrayList<>();
        String sql = "SELECT maPhongBan, tenPhongBan, phongBanCha, trangThai FROM PHONGBAN WHERE phongBanCha = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maPhongBan);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi DepartmentRepository.findChildren: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Lấy danh sách phòng ban đang hoạt động.
     */
    public List<Department> findActive() {
        List<Department> list = new ArrayList<>();
        String sql = "SELECT maPhongBan, tenPhongBan, phongBanCha, trangThai FROM PHONGBAN WHERE trangThai = 'hoat_dong' ORDER BY maPhongBan";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi DepartmentRepository.findActive: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Kiểm tra mã phòng ban có tồn tại không.
     */
    public boolean existsById(String maPhongBan) {
        String sql = "SELECT 1 FROM PHONGBAN WHERE maPhongBan = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maPhongBan);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Lỗi DepartmentRepository.existsById: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Thêm phòng ban mới vào cơ sở dữ liệu.
     */
    public void save(Department department) {
        String sql = "INSERT INTO PHONGBAN (maPhongBan, tenPhongBan, phongBanCha, trangThai) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, department.getMaPhongBan());
            ps.setString(2, department.getTenPhongBan());
            if (department.getPhongBanCha() != null) {
                ps.setString(3, department.getPhongBanCha());
            } else {
                ps.setNull(3, Types.VARCHAR);
            }
            ps.setString(4, department.getTrangThai());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Lỗi DepartmentRepository.save: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Cập nhật thông tin phòng ban.
     */
    public void update(Department department) {
        String sql = "UPDATE PHONGBAN SET tenPhongBan = ?, phongBanCha = ?, trangThai = ? WHERE maPhongBan = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, department.getTenPhongBan());
            if (department.getPhongBanCha() != null) {
                ps.setString(2, department.getPhongBanCha());
            } else {
                ps.setNull(2, Types.VARCHAR);
            }
            ps.setString(3, department.getTrangThai());
            ps.setString(4, department.getMaPhongBan());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Lỗi DepartmentRepository.update: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Kiểm tra phòng ban có bổ nhiệm đang hoạt động không.
     * Tạm thời trả về false vì module BoNhiem chưa được tích hợp ở đây.
     */
    public boolean hasActiveAppointments(String maPhongBan) {
        return false;
    }

    /**
     * Kiểm tra phòng ban có phòng ban con đang hoạt động không.
     */
    public boolean hasActiveChildren(String maPhongBan) {
        String sql = "SELECT 1 FROM PHONGBAN WHERE phongBanCha = ? AND trangThai = 'hoat_dong' LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maPhongBan);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Lỗi DepartmentRepository.hasActiveChildren: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // =====================================================================
    // ==================== Private Helpers ================================
    // =====================================================================

    private Department mapRow(ResultSet rs) throws SQLException {
        return new Department(
                rs.getString("maPhongBan"),
                rs.getString("tenPhongBan"),
                rs.getString("phongBanCha"),
                rs.getString("trangThai")
        );
    }
}
