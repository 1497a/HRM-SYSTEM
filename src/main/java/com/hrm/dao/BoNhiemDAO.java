package com.hrm.dao;

import com.hrm.model.BoNhiem;
import com.hrm.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository cho bảng BONHIEM.
 * Singleton pattern - sử dụng MySQL JDBC.
 */
public class BoNhiemDAO {

    private static BoNhiemDAO instance;

    private BoNhiemDAO() {
    }

    public static synchronized BoNhiemDAO getInstance() {
        if (instance == null) {
            instance = new BoNhiemDAO();
        }
        return instance;
    }

    // ============================
    // Mapping helper
    // ============================

    private BoNhiem mapRow(ResultSet rs) throws SQLException {
        BoNhiem bn = new BoNhiem();
        bn.setMaBoNhiem(rs.getInt("maBoNhiem"));
        bn.setMaNV(rs.getInt("maNV"));
        bn.setPhongBanId(rs.getString("maPhongBan"));
        bn.setChucVuId(rs.getString("maChucVu"));
        bn.setLoaiBoNhiem(rs.getString("loaiBoNhiem"));
        bn.setTyLeHuongLuong(rs.getDouble("tyLeHuongLuong"));
        bn.setMaQuanLy(rs.getInt("maQuanLy")); // 0 if null
        bn.setNguoiDuyet(rs.getInt("nguoiDuyet")); // 0 if null

        Date tuNgay = rs.getDate("tuNgay");
        if (tuNgay != null) bn.setTuNgay(tuNgay.toLocalDate());

        Date denNgay = rs.getDate("denNgay");
        if (denNgay != null) bn.setDenNgay(denNgay.toLocalDate());

        Timestamp ngayPheDuyet = rs.getTimestamp("ngayPheDuyet");
        if (ngayPheDuyet != null) bn.setNgayPheDuyet(ngayPheDuyet.toLocalDateTime());

        bn.setLyDo(rs.getString("lyDo"));
        bn.setTrangThai(rs.getString("trangThai"));
        return bn;
    }

    private void trySetTransient(ResultSet rs, BoNhiem bn) {
        try { bn.setTenNV(rs.getString("hoTen")); } catch (SQLException ignored) {}
        try { bn.setMaNhanVien(rs.getString("maNhanVien")); } catch (SQLException ignored) {}
        try { bn.setTenPhongBan(rs.getString("tenPhongBan")); } catch (SQLException ignored) {}
        try { bn.setTenChucVu(rs.getString("tenChucVu")); } catch (SQLException ignored) {}
        try { bn.setTenQuanLy(rs.getString("tenQuanLy")); } catch (SQLException ignored) {}
    }

    // ============================
    // insert - returns generated maBoNhiem
    // ============================

    public int insert(BoNhiem bn) throws SQLException {
        String sql = "INSERT INTO BONHIEM "
                + "(maNV, maPhongBan, maChucVu, loaiBoNhiem, tyLeHuongLuong, maQuanLy, "
                + " nguoiDuyet, tuNgay, denNgay, ngayPheDuyet, lyDo, trangThai) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setInsertParams(ps, bn);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    bn.setMaBoNhiem(id);
                    return id;
                }
            }
        }
        throw new SQLException("Không lấy được maBoNhiem sau khi insert.");
    }

    // ============================
    // updateTrangThai
    // ============================

    public void updateTrangThai(int maBoNhiem, String trangThai, LocalDateTime ngayPheDuyet) {
        String sql = "UPDATE BONHIEM SET trangThai=?, ngayPheDuyet=? WHERE maBoNhiem=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, trangThai);
            ps.setTimestamp(2, ngayPheDuyet != null ? Timestamp.valueOf(ngayPheDuyet) : null);
            ps.setInt(3, maBoNhiem);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi cập nhật trạng thái bổ nhiệm: " + e.getMessage(), e);
        }
    }

    // ============================
    // updateLyDoTuChoi
    // ============================

    public void updateLyDoTuChoi(int maBoNhiem, String lyDo) {
        String sql = "UPDATE BONHIEM SET lyDo=? WHERE maBoNhiem=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, lyDo);
            ps.setInt(2, maBoNhiem);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Lỗi updateLyDoTuChoi: " + e.getMessage());
        }
    }

    // ============================
    // updateNguoiDuyet
    // ============================

    public void updateNguoiDuyet(int maBoNhiem, int nguoiDuyetId) {
        String sql = "UPDATE BONHIEM SET nguoiDuyet=? WHERE maBoNhiem=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, nguoiDuyetId);
            ps.setInt(2, maBoNhiem);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi cập nhật người duyệt: " + e.getMessage(), e);
        }
    }

    // ============================
    // findByMaNV - with transient names
    // ============================

    public List<BoNhiem> findByMaNV(int maNV) {
        String sql = buildJoinQuery("WHERE b.maNV = ?", "ORDER BY b.tuNgay DESC");
        List<BoNhiem> result = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maNV);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BoNhiem bn = mapRow(rs);
                    trySetTransient(rs, bn);
                    result.add(bn);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tải bổ nhiệm theo nhân viên: " + e.getMessage(), e);
        }
        return result;
    }

    // ============================
    // findBoNhiemChinhHieuLuc
    // ============================

    public BoNhiem findBoNhiemChinhHieuLuc(int maNV) {
        String sql = buildJoinQuery(
                "WHERE b.maNV = ? AND b.trangThai = 'hieu_luc' AND b.loaiBoNhiem = 'chinh'",
                "LIMIT 1");
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maNV);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    BoNhiem bn = mapRow(rs);
                    trySetTransient(rs, bn);
                    return bn;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tải bổ nhiệm chính hiệu lực: " + e.getMessage(), e);
        }
        return null;
    }

    // ============================
    // findChoDuyet
    // ============================

    public List<BoNhiem> findChoDuyet() {
        String sql = buildJoinQuery("WHERE b.trangThai = 'cho_duyet'", "ORDER BY b.maBoNhiem ASC");
        List<BoNhiem> result = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                BoNhiem bn = mapRow(rs);
                trySetTransient(rs, bn);
                result.add(bn);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tải bổ nhiệm chờ duyệt: " + e.getMessage(), e);
        }
        return result;
    }

    // ============================
    // findAll - all with transient names
    // ============================

    public List<BoNhiem> findAll() {
        String sql = buildJoinQuery("", "ORDER BY b.maBoNhiem DESC");
        List<BoNhiem> result = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                BoNhiem bn = mapRow(rs);
                trySetTransient(rs, bn);
                result.add(bn);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tải danh sách bổ nhiệm: " + e.getMessage(), e);
        }
        return result;
    }

    public List<BoNhiem> findAllByScope(com.hrm.model.DataScope scope, int currentUserId) {
        List<BoNhiem> result = new ArrayList<>();
        if (scope == com.hrm.model.DataScope.NONE) return result;

        String whereClause;
        switch (scope) {
            case ALL:
                whereClause = "";
                break;
            case DEPT:
                whereClause = "WHERE b.maPhongBan = ("
                        + "SELECT b2.maPhongBan FROM BONHIEM b2 "
                        + "WHERE b2.maNV = ? AND b2.trangThai = 'hieu_luc' AND b2.loaiBoNhiem = 'chinh'"
                        + ")";
                break;
            case TEAM:
                whereClause = "WHERE b.maQuanLy = ? OR b.maNV = ?";
                break;
            case SELF:
                whereClause = "WHERE b.maNV = ?";
                break;
            default:
                return result;
        }

        String sql = buildJoinQuery(whereClause, "ORDER BY b.maBoNhiem DESC");
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (scope == com.hrm.model.DataScope.TEAM) {
                ps.setInt(1, currentUserId);
                ps.setInt(2, currentUserId);
            } else if (scope != com.hrm.model.DataScope.ALL) {
                ps.setInt(1, currentUserId);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BoNhiem bn = mapRow(rs);
                    trySetTransient(rs, bn);
                    result.add(bn);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Loi tai danh sach bo nhiem theo scope: " + e.getMessage(), e);
        }
        return result;
    }

    // ============================
    // hasConflictingChinhBoNhiem
    // ============================

    public boolean hasConflictingChinhBoNhiem(int maNV, LocalDate tuNgay, LocalDate denNgay, int excludeId) {
        // Kiểm tra overlap với bổ nhiệm chính đang hiệu lực hoặc chờ duyệt
        String sql = "SELECT COUNT(*) FROM BONHIEM "
                + "WHERE maNV=? AND loaiBoNhiem='chinh' "
                + "AND trangThai IN ('hieu_luc','cho_duyet') "
                + "AND maBoNhiem <> ? "
                + "AND (denNgay IS NULL OR denNgay >= ?) "
                + "AND tuNgay <= ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maNV);
            ps.setInt(2, excludeId);
            ps.setDate(3, Date.valueOf(tuNgay));
            ps.setDate(4, denNgay != null ? Date.valueOf(denNgay) : Date.valueOf(LocalDate.of(9999, 12, 31)));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi kiểm tra xung đột bổ nhiệm: " + e.getMessage(), e);
        }
        return false;
    }

    /**
     * Kiểm tra phòng ban có bổ nhiệm đang hiệu lực không (để validate trước khi ngưng phòng ban).
     */
    public boolean hasActiveBoNhiemInDepartment(String maPhongBan) {
        String sql = "SELECT COUNT(*) FROM BONHIEM WHERE maPhongBan=? AND trangThai='hieu_luc'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maPhongBan);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi kiểm tra bổ nhiệm phòng ban: " + e.getMessage(), e);
        }
        return false;
    }

    // ============================
    // endBoNhiem - set denNgay + trangThai=het_hieu_luc
    // ============================

    public void endBoNhiem(int maBoNhiem, LocalDate denNgay) {
        String sql = "UPDATE BONHIEM SET denNgay=?, trangThai='het_hieu_luc' WHERE maBoNhiem=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(denNgay));
            ps.setInt(2, maBoNhiem);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi kết thúc bổ nhiệm: " + e.getMessage(), e);
        }
    }

    /**
     * Kết thúc tất cả bổ nhiệm chính hiệu lực của một nhân viên (dùng khi nghỉ việc).
     */
    public void endAllActiveBoNhiemForNV(int maNV, LocalDate denNgay) {
        String sql = "UPDATE BONHIEM SET denNgay=?, trangThai='het_hieu_luc' "
                + "WHERE maNV=? AND trangThai='hieu_luc'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(denNgay));
            ps.setInt(2, maNV);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi kết thúc tất cả bổ nhiệm: " + e.getMessage(), e);
        }
    }

    // ============================
    // Private helper: build SELECT with JOINs
    // ============================

    private String buildJoinQuery(String whereClause, String orderAndLimit) {
        return "SELECT b.*, t.hoTen, nv_code.maNhanVien, pb.tenPhongBan, cv.tenChucVu, t_ql.hoTen AS tenQuanLy "
                + "FROM BONHIEM b "
                + "LEFT JOIN THONGTINCANHAN t ON b.maNV = t.maNV "
                + "LEFT JOIN NHANVIEN nv_code ON b.maNV = nv_code.maNV "
                + "LEFT JOIN PHONGBAN pb ON b.maPhongBan = pb.maPhongBan "
                + "LEFT JOIN CHUCVU cv ON b.maChucVu = cv.maChucVu "
                + "LEFT JOIN THONGTINCANHAN t_ql ON b.maQuanLy = t_ql.maNV "
                + (whereClause.isEmpty() ? "" : whereClause + " ")
                + orderAndLimit;
    }

    private void setInsertParams(PreparedStatement ps, BoNhiem bn) throws SQLException {
        ps.setInt(1, bn.getMaNV());
        ps.setString(2, bn.getPhongBanId());   // maPhongBan  ← FIXED (was chucVuId)
        ps.setString(3, bn.getChucVuId());     // maChucVu
        ps.setString(4, bn.getLoaiBoNhiem());
        ps.setDouble(5, bn.getTyLeHuongLuong());
        if (bn.getMaQuanLy() > 0) {
            ps.setInt(6, bn.getMaQuanLy());
        } else {
            ps.setNull(6, Types.INTEGER);
        }
        if (bn.getNguoiDuyet() > 0) {
            ps.setInt(7, bn.getNguoiDuyet());
        } else {
            ps.setNull(7, Types.INTEGER);
        }
        ps.setDate(8, bn.getTuNgay() != null ? Date.valueOf(bn.getTuNgay()) : null);
        ps.setDate(9, bn.getDenNgay() != null ? Date.valueOf(bn.getDenNgay()) : null);
        ps.setTimestamp(10, bn.getNgayPheDuyet() != null ? Timestamp.valueOf(bn.getNgayPheDuyet()) : null);
        ps.setString(11, bn.getLyDo());
        ps.setString(12, bn.getTrangThai());
    }
}
