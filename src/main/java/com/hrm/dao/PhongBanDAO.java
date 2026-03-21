package com.hrm.dao;

import com.hrm.model.PhongBan;
import com.hrm.util.DatabaseConnection;
import com.hrm.util.HRMConstants;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository JDBC cho bảng PHONGBAN.
 */
public class PhongBanDAO {

    private static PhongBanDAO instance;
    public static synchronized PhongBanDAO getInstance() {
        if (instance == null) {
            instance = new PhongBanDAO();
        }
        return instance;
    }

    /**
     * Lấy tất cả phòng ban.
     */
    public List<PhongBan> findAll() {
        List<PhongBan> list = new ArrayList<>();
        String sql = "SELECT maPhongBan, tenPhongBan, phongBanCha, trangThai FROM PHONGBAN ORDER BY maPhongBan";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi PhongBanDAO.findAll: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Tìm phòng ban theo mã.
     */
    public PhongBan findById(String maPhongBan) {
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
            System.err.println("Lỗi PhongBanDAO.findById: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Lấy danh sách phòng ban con trực tiếp.
     */
    public List<PhongBan> findChildren(String maPhongBan) {
        List<PhongBan> list = new ArrayList<>();
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
            System.err.println("Lỗi PhongBanDAO.findChildren: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Lấy danh sách phòng ban đang hoạt động.
     */
    public List<PhongBan> findActive() {
        List<PhongBan> list = new ArrayList<>();
        String sql = "SELECT maPhongBan, tenPhongBan, phongBanCha, trangThai "
                + "FROM PHONGBAN WHERE trangThai = ? ORDER BY maPhongBan";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, HRMConstants.TRANG_THAI_HOAT_DONG);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi PhongBanDAO.findActive: " + e.getMessage());
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
            System.err.println("Lỗi PhongBanDAO.existsById: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Thêm phòng ban mới vào cơ sở dữ liệu.
     */
    public void save(PhongBan department) {
        String sql = "INSERT INTO PHONGBAN (maPhongBan, tenPhongBan, phongBanCha, trangThai) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, department.getId());
            ps.setString(2, department.getTenPhongBan());
            if (department.getPhongBanChaId() != null) {
                ps.setObject(3, department.getPhongBanChaId());
            } else {
                ps.setNull(3, Types.VARCHAR);
            }
            ps.setString(4, department.getTrangThai());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Lỗi PhongBanDAO.save: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Cập nhật thông tin phòng ban.
     */
    public void update(PhongBan department) {
        String sql = "UPDATE PHONGBAN SET tenPhongBan = ?, phongBanCha = ?, trangThai = ? WHERE maPhongBan = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, department.getTenPhongBan());
            if (department.getPhongBanChaId() != null) {
                ps.setObject(2, department.getPhongBanChaId());
            } else {
                ps.setNull(2, Types.VARCHAR);
            }
            ps.setString(3, department.getTrangThai());
            ps.setString(4, department.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Lỗi PhongBanDAO.update: " + e.getMessage());
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
        String sql = "SELECT 1 FROM PHONGBAN WHERE phongBanCha = ? AND trangThai = ? LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maPhongBan);
            ps.setString(2, HRMConstants.TRANG_THAI_HOAT_DONG);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Lỗi PhongBanDAO.hasActiveChildren: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // =====================================================================
    // ==================== Private Helpers ================================
    // =====================================================================
    private PhongBan mapRow(ResultSet rs) throws SQLException {
        return new PhongBan(
                rs.getString("maPhongBan"),
                rs.getString("tenPhongBan"),
                rs.getString("phongBanCha"),
                rs.getString("trangThai")
        );
    }
}
