package com.hrm.dao;

import com.hrm.model.ChucVu;
import com.hrm.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository JDBC cho bảng CHUCVU.
 */
public class ChucVuDAO {

    /**
     * Lấy tất cả chức vụ.
     */
    public List<ChucVu> findAll() {
        List<ChucVu> list = new ArrayList<>();
        String sql = "SELECT maChucVu, tenChucVu, capBac, heSoLuong, phuCapChucVu, moTa, trangThai "
                   + "FROM CHUCVU ORDER BY capBac, maChucVu";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi ChucVuDAO.findAll: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Tìm chức vụ theo mã.
     */
    public ChucVu findById(String maChucVu) {
        String sql = "SELECT maChucVu, tenChucVu, capBac, heSoLuong, phuCapChucVu, moTa, trangThai "
                   + "FROM CHUCVU WHERE maChucVu = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maChucVu);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi ChucVuDAO.findById: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Lấy danh sách chức vụ đang hoạt động.
     */
    public List<ChucVu> findActive() {
        List<ChucVu> list = new ArrayList<>();
        String sql = "SELECT maChucVu, tenChucVu, capBac, heSoLuong, phuCapChucVu, moTa, trangThai "
                   + "FROM CHUCVU WHERE trangThai = 'hoatDong' ORDER BY capBac, maChucVu";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi ChucVuDAO.findActive: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Kiểm tra mã chức vụ đang hoạt động đã tồn tại chưa (chống tạo trùng).
     */
    public boolean existsActiveByCode(String maChucVu) {
        String sql = "SELECT COUNT(*) FROM CHUCVU WHERE maChucVu = ? AND trangThai = 'hoat_dong'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maChucVu);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Lỗi ChucVuDAO.existsActiveByCode: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Kiểm tra mã chức vụ có tồn tại không.
     */
    public boolean existsById(String maChucVu) {
        String sql = "SELECT 1 FROM CHUCVU WHERE maChucVu = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maChucVu);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Lỗi ChucVuDAO.existsById: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Thêm chức vụ mới vào cơ sở dữ liệu.
     */
    public void save(ChucVu position) {
        String sql = "INSERT INTO CHUCVU (maChucVu, tenChucVu, capBac, heSoLuong, phuCapChucVu, moTa, trangThai) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, position.getId());
            ps.setString(2, position.getTenChucVu());
            ps.setInt(3, position.getCapBac());
            ps.setDouble(4, position.getHeSoLuong());
            ps.setDouble(5, position.getPhuCapChucVu());
            ps.setString(6, position.getMoTa());
            ps.setString(7, position.getTrangThai());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Lỗi ChucVuDAO.save: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Cập nhật thông tin chức vụ.
     */
    public void update(ChucVu position) {
        String sql = "UPDATE CHUCVU SET tenChucVu = ?, capBac = ?, heSoLuong = ?, phuCapChucVu = ?, moTa = ?, trangThai = ? "
                   + "WHERE maChucVu = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, position.getTenChucVu());
            ps.setInt(2, position.getCapBac());
            ps.setDouble(3, position.getHeSoLuong());
            ps.setDouble(4, position.getPhuCapChucVu());
            ps.setString(5, position.getMoTa());
            ps.setString(6, position.getTrangThai());
            ps.setString(7, position.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Lỗi ChucVuDAO.update: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // =====================================================================
    // ==================== Private Helpers ================================
    // =====================================================================

    private ChucVu mapRow(ResultSet rs) throws SQLException {
        return new ChucVu(
                rs.getString("maChucVu"),
                rs.getString("tenChucVu"),
                rs.getInt("capBac"),
                rs.getDouble("heSoLuong"),
                rs.getDouble("phuCapChucVu"),
                rs.getString("moTa"),
                rs.getString("trangThai")
        );
    }
}
