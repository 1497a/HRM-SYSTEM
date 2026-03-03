package com.hrm.dao;

import com.hrm.model.ThongBao;
import com.hrm.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository JDBC cho bảng THONGBAO.
 * Singleton pattern.
 */
public class ThongBaoDAO {

    private static ThongBaoDAO instance;

    private ThongBaoDAO() {
    }

    public static synchronized ThongBaoDAO getInstance() {
        if (instance == null) {
            instance = new ThongBaoDAO();
        }
        return instance;
    }

    // ============================
    // Mapping helper
    // ============================

    private ThongBao mapRow(ResultSet rs) throws SQLException {
        ThongBao tb = new ThongBao();
        tb.setMaThongBao(rs.getInt("maThongBao"));
        tb.setTieuDe(rs.getString("tieuDe"));
        tb.setNoiDung(rs.getString("noiDung"));
        tb.setLoaiThongBao(rs.getString("loaiThongBao"));
        tb.setMaTaiKhoanGui(rs.getInt("maTaiKhoanGui"));
        tb.setMaTaiKhoanNhan(rs.getInt("maTaiKhoanNhan"));
        tb.setDaDoc(rs.getBoolean("daDoc"));
        Timestamp ngayDoc = rs.getTimestamp("ngayDoc");
        if (ngayDoc != null) {
            tb.setNgayDoc(ngayDoc.toLocalDateTime());
        }
        tb.setLinkLienQuan(rs.getString("linkLienQuan"));
        Timestamp ngayTao = rs.getTimestamp("ngayTao");
        if (ngayTao != null) {
            tb.setNgayTao(ngayTao.toLocalDateTime());
        }
        return tb;
    }

    /**
     * Chèn thông báo mới, trả về ID được sinh ra.
     */
    public int insert(ThongBao tb) {
        String sql = "INSERT INTO THONGBAO (tieuDe, noiDung, loaiThongBao, maTaiKhoanGui, "
                + "maTaiKhoanNhan, daDoc, linkLienQuan, ngayTao) "
                + "VALUES (?, ?, ?, ?, ?, FALSE, ?, NOW())";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, tb.getTieuDe());
            ps.setString(2, tb.getNoiDung());
            ps.setString(3, tb.getLoaiThongBao());
            if (tb.getMaTaiKhoanGui() == 0) {
                ps.setNull(4, Types.INTEGER);
            } else {
                ps.setInt(4, tb.getMaTaiKhoanGui());
            }
            ps.setInt(5, tb.getMaTaiKhoanNhan());
            ps.setString(6, tb.getLinkLienQuan());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi insert thông báo: " + e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Chèn nhiều thông báo cùng lúc (bulk insert) trong một connection.
     */
    public void insertBulk(List<ThongBao> list) {
        if (list == null || list.isEmpty()) return;
        String sql = "INSERT INTO THONGBAO (tieuDe, noiDung, loaiThongBao, maTaiKhoanGui, "
                + "maTaiKhoanNhan, daDoc, linkLienQuan, ngayTao) "
                + "VALUES (?, ?, ?, ?, ?, FALSE, ?, NOW())";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            conn.setAutoCommit(false);
            try {
                for (ThongBao tb : list) {
                    ps.setString(1, tb.getTieuDe());
                    ps.setString(2, tb.getNoiDung());
                    ps.setString(3, tb.getLoaiThongBao());
                    if (tb.getMaTaiKhoanGui() == 0) {
                        ps.setNull(4, Types.INTEGER);
                    } else {
                        ps.setInt(4, tb.getMaTaiKhoanGui());
                    }
                    ps.setInt(5, tb.getMaTaiKhoanNhan());
                    ps.setString(6, tb.getLinkLienQuan());
                    ps.addBatch();
                }
                ps.executeBatch();
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi insertBulk thông báo: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Lấy tất cả thông báo của người nhận, mới nhất trước.
     */
    public List<ThongBao> findByNguoiNhan(int maTaiKhoanNhan) {
        List<ThongBao> list = new ArrayList<>();
        String sql = "SELECT * FROM THONGBAO WHERE maTaiKhoanNhan = ? ORDER BY ngayTao DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maTaiKhoanNhan);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi findByNguoiNhan: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Lấy các thông báo chưa đọc của người nhận.
     */
    public List<ThongBao> findUnreadByNguoiNhan(int maTaiKhoanNhan) {
        List<ThongBao> list = new ArrayList<>();
        String sql = "SELECT * FROM THONGBAO WHERE maTaiKhoanNhan = ? AND daDoc = FALSE ORDER BY ngayTao DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maTaiKhoanNhan);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi findUnreadByNguoiNhan: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Đếm số thông báo chưa đọc của người nhận.
     */
    public int countUnread(int maTaiKhoanNhan) {
        String sql = "SELECT COUNT(*) FROM THONGBAO WHERE maTaiKhoanNhan = ? AND daDoc = FALSE";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maTaiKhoanNhan);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi countUnread: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Đánh dấu một thông báo là đã đọc.
     */
    public void markAsRead(int maThongBao) {
        String sql = "UPDATE THONGBAO SET daDoc = TRUE, ngayDoc = NOW() WHERE maThongBao = ? AND daDoc = FALSE";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maThongBao);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Lỗi markAsRead: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Đánh dấu tất cả thông báo của người nhận là đã đọc.
     */
    public void markAllAsRead(int maTaiKhoanNhan) {
        String sql = "UPDATE THONGBAO SET daDoc = TRUE, ngayDoc = NOW() WHERE maTaiKhoanNhan = ? AND daDoc = FALSE";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maTaiKhoanNhan);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Lỗi markAllAsRead: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
