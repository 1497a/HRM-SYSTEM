package com.hrm.dao;

import com.hrm.model.ThongBao;
import com.hrm.util.DatabaseConnection;

import java.sql.*;
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
        Timestamp ngayTao = rs.getTimestamp("ngayTao");
        if (ngayTao != null) {
            tb.setNgayTao(ngayTao.toLocalDateTime());
        }
        return tb;
    }

    private String buildInsertSql() {
        return "INSERT INTO THONGBAO (tieuDe, noiDung, loaiThongBao, maTaiKhoanGui, "
                + "maTaiKhoanNhan, daDoc, ngayTao) "
                + "VALUES (?, ?, ?, ?, ?, FALSE, NOW())";
    }

    private void bindInsertStatement(PreparedStatement ps, ThongBao tb) throws SQLException {
        ps.setString(1, tb.getTieuDe());
        ps.setString(2, tb.getNoiDung());
        ps.setString(3, tb.getLoaiThongBao());
        if (tb.getMaTaiKhoanGui() == 0) {
            ps.setNull(4, Types.INTEGER);
        } else {
            ps.setInt(4, tb.getMaTaiKhoanGui());
        }
        ps.setInt(5, tb.getMaTaiKhoanNhan());
    }

    /**
     * Chèn thông báo mới, trả về ID được sinh ra.
     */
    public int insert(ThongBao tb) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     buildInsertSql(),
                     Statement.RETURN_GENERATED_KEYS)) {
            bindInsertStatement(ps, tb);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Loi insert thong bao: " + e.getMessage(), e);
        }
        return -1;
    }

    /**
     * Chèn nhiều thông báo cùng lúc (bulk insert) trong một connection.
     */
    public int insertBulk(List<ThongBao> list) {
        if (list == null || list.isEmpty()) return 0;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(buildInsertSql())) {
            conn.setAutoCommit(false);
            try {
                for (ThongBao tb : list) {
                    bindInsertStatement(ps, tb);
                    ps.addBatch();
                }
                int[] counts = ps.executeBatch();
                conn.commit();
                int total = 0;
                for (int c : counts) total += (c >= 0 ? c : 0);
                return total;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Loi insertBulk thong bao: " + e.getMessage(), e);
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
    public int markAsRead(int maThongBao) {
        String sql = "UPDATE THONGBAO SET daDoc = TRUE, ngayDoc = NOW() WHERE maThongBao = ? AND daDoc = FALSE";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maThongBao);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Loi markAsRead thong bao " + maThongBao + ": " + e.getMessage(), e);
        }
    }

    /**
     * Đánh dấu tất cả thông báo của người nhận là đã đọc.
     */
    public int markAllAsRead(int maTaiKhoanNhan) {
        String sql = "UPDATE THONGBAO SET daDoc = TRUE, ngayDoc = NOW() WHERE maTaiKhoanNhan = ? AND daDoc = FALSE";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maTaiKhoanNhan);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Loi markAllAsRead cho tai khoan " + maTaiKhoanNhan + ": " + e.getMessage(), e);
        }
    }
    public Integer findMaTaiKhoanByMaNV(String maNV) {
        String sql = "SELECT maTaiKhoan FROM TAIKHOAN WHERE maNV = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maNV);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Loi findMaTaiKhoanByMaNV: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public List<Integer> findTaiKhoanByPhongBan(String maPhongBan) {
        return findTaiKhoanByBoNhiemField("maPhongBan", maPhongBan, "findTaiKhoanByPhongBan");
    }

    public List<Integer> findTaiKhoanByChucVu(String maChucVu) {
        return findTaiKhoanByBoNhiemField("maChucVu", maChucVu, "findTaiKhoanByChucVu");
    }

    public List<Integer> findAllActiveTaiKhoan() {
        List<Integer> list = new ArrayList<>();
        String sql = "SELECT maTaiKhoan FROM TAIKHOAN WHERE hoatDong = TRUE AND biKhoa = FALSE";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(rs.getInt(1));
            }
        } catch (SQLException e) {
            System.err.println("Loi findAllActiveTaiKhoan: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    private List<Integer> findTaiKhoanByBoNhiemField(String fieldName, String value, String logTag) {
        List<Integer> list = new ArrayList<>();
        String sql = "SELECT DISTINCT tk.maTaiKhoan FROM TAIKHOAN tk "
                + "JOIN NHANVIEN nv ON tk.maNV = nv.maNV "
                + "JOIN BONHIEM bn ON nv.maNV = bn.maNV "
                + "WHERE bn." + fieldName + " = ? AND bn.trangThai = 'hieu_luc' "
                + "AND nv.trangThai = 'dang_lam_viec' AND tk.hoatDong = TRUE";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, value);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            System.err.println("Loi " + logTag + ": " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }
}
