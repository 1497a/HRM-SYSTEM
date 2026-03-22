package com.hrm.dao;

import com.hrm.model.ChucVu;
import com.hrm.util.DatabaseConnection;
import com.hrm.util.HRMConstants;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository JDBC cho bảng CHUCVU.
 */
public class ChucVuDAO {
    private static ChucVuDAO instance;
    public static synchronized ChucVuDAO getInstance() {
        if (instance == null) {
            instance = new ChucVuDAO();
        }
        return instance;
    }

    /**
     * Lấy tất cả chức vụ.
     */
    public List<ChucVu> findAll() {
        List<ChucVu> list = new ArrayList<>();
        String sql = "SELECT maChucVu, tenChucVu, capBac, phuCapChucVu, moTa, maVaiTro, trangThai "
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
        String sql = "SELECT maChucVu, tenChucVu, capBac, phuCapChucVu, moTa, maVaiTro, trangThai "
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
        String sql = "SELECT maChucVu, tenChucVu, capBac, phuCapChucVu, moTa, maVaiTro, trangThai "
                   + "FROM CHUCVU WHERE trangThai = '" + HRMConstants.TRANG_THAI_HOAT_DONG + "' ORDER BY capBac, maChucVu";
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
        String sql = "SELECT COUNT(*) FROM CHUCVU WHERE maChucVu = ? AND trangThai = '" + HRMConstants.TRANG_THAI_HOAT_DONG + "'";
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
    public int save(ChucVu position) {
        String sql = "INSERT INTO CHUCVU (maChucVu, tenChucVu, capBac, phuCapChucVu, moTa, maVaiTro, trangThai) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, position.getId());
            ps.setString(2, position.getTenChucVu());
            ps.setInt(3, position.getCapBac());
            ps.setDouble(4, position.getPhuCapChucVu());
            ps.setString(5, position.getMoTa());
            ps.setString(6, position.getMaVaiTro());
            ps.setString(7, position.getTrangThai());
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Loi ChucVuDAO.save: " + e.getMessage(), e);
        }
    }

    /**
     * Cập nhật thông tin chức vụ.
     */
    public int update(ChucVu position) {
        String sql = "UPDATE CHUCVU SET tenChucVu = ?, capBac = ?, phuCapChucVu = ?, moTa = ?, maVaiTro = ?, trangThai = ? "
                   + "WHERE maChucVu = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, position.getTenChucVu());
            ps.setInt(2, position.getCapBac());
            ps.setDouble(3, position.getPhuCapChucVu());
            ps.setString(4, position.getMoTa());
            ps.setString(5, position.getMaVaiTro());
            ps.setString(6, position.getTrangThai());
            ps.setString(7, position.getId());
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Loi ChucVuDAO.update: " + e.getMessage(), e);
        }
    }

    // =====================================================================
    // ==================== Private Helpers ================================
    // =====================================================================
    /**
     * Cập nhật chỉ trường maVaiTro của chức vụ (dùng cho màn hình mapping ChucVu → VaiTro).
     */
    public int updateMaVaiTro(String maChucVu, String maVaiTro) {
        String sql = "UPDATE CHUCVU SET maVaiTro = ? WHERE maChucVu = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (maVaiTro == null || maVaiTro.isEmpty()) {
                ps.setNull(1, java.sql.Types.VARCHAR);
            } else {
                ps.setString(1, maVaiTro.trim());
            }
            ps.setString(2, maChucVu);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Loi ChucVuDAO.updateMaVaiTro: " + e.getMessage(), e);
        }
    }

    private ChucVu mapRow(ResultSet rs) throws SQLException {
        ChucVu cv = new ChucVu(
                rs.getString("maChucVu"),
                rs.getString("tenChucVu"),
                rs.getInt("capBac"),
                rs.getDouble("phuCapChucVu"),
                rs.getString("moTa"),
                rs.getString("trangThai")
        );
        cv.setMaVaiTro(rs.getString("maVaiTro"));
        return cv;
    }
}
