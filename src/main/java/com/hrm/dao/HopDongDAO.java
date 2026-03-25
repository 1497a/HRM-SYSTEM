package com.hrm.dao;

import com.hrm.model.HopDongLaoDong;
import com.hrm.util.DatabaseConnection;
import com.hrm.util.HRMConstants;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository cho bảng HOPDONGLAODONG.
 * Singleton pattern - sử dụng MySQL JDBC.
 */
public class HopDongDAO {

    private static HopDongDAO instance;
    private HopDongDAO() {
    }

    public static synchronized HopDongDAO getInstance() {
        if (instance == null) {
            instance = new HopDongDAO();
        }
        return instance;
    }

    // ============================
    // Mapping helper
    // ============================
    private HopDongLaoDong mapRow(ResultSet rs) throws SQLException {
        HopDongLaoDong hd = new HopDongLaoDong();
        hd.setMaHopDong(rs.getInt("maHopDong"));
        hd.setSoHopDong(rs.getString("soHopDong"));
        hd.setMaNV(rs.getString("maNV"));
        hd.setLoaiHopDong(rs.getString("loaiHopDong"));
        hd.setLuongCoSo(rs.getLong("luongCoSo"));
        Date ngayKy = rs.getDate("ngayKy");
        if (ngayKy != null) hd.setNgayKy(ngayKy.toLocalDate());
        Date ngayHieuLuc = rs.getDate("ngayHieuLuc");
        if (ngayHieuLuc != null) hd.setNgayHieuLuc(ngayHieuLuc.toLocalDate());
        Date ngayHetHieuLuc = rs.getDate("ngayHetHieuLuc");
        if (ngayHetHieuLuc != null) hd.setNgayHetHieuLuc(ngayHetHieuLuc.toLocalDate());
        hd.setFileDinhKem(rs.getString("fileDinhKem"));
        hd.setNoiDung(rs.getString("noiDung"));
        hd.setTrangThai(rs.getString("trangThai"));
        Date ngayThanhLy = rs.getDate("ngayThanhLy");
        if (ngayThanhLy != null) hd.setNgayThanhLy(ngayThanhLy.toLocalDate());
        hd.setLyDoThanhLy(rs.getString("lyDoThanhLy"));
        hd.setGhiChu(rs.getString("ghiChu"));
        hd.setNguoiTao(rs.getString("nguoiTao"));
        hd.setNguoiDuyet(rs.getString("nguoiDuyet"));
        Timestamp ngayDuyet = rs.getTimestamp("ngayDuyet");
        if (ngayDuyet != null) hd.setNgayDuyet(ngayDuyet.toLocalDateTime());
        return hd;
    }

    // ============================
    // insert - returns generated maHopDong
    // ============================
    public int insert(HopDongLaoDong hd) throws SQLException {
        String sql = "INSERT INTO HOPDONGLAODONG "
                + "(soHopDong, maNV, loaiHopDong, luongCoSo, ngayKy, ngayHieuLuc, "
                + " ngayHetHieuLuc, fileDinhKem, noiDung, trangThai, "
                + " ngayThanhLy, lyDoThanhLy, ghiChu, nguoiTao) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setInsertParams(ps, hd);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    hd.setMaHopDong(id);
                    return id;
                }
            }
        }
        throw new SQLException("Không lấy được maHopDong sau khi insert.");
    }

    // ============================
    // update
    // ============================
    public int update(HopDongLaoDong hd) {
        String sql = "UPDATE HOPDONGLAODONG SET "
                + "soHopDong=?, maNV=?, loaiHopDong=?, luongCoSo=?, ngayKy=?, ngayHieuLuc=?, "
                + "ngayHetHieuLuc=?, fileDinhKem=?, noiDung=?, trangThai=?, "
                + "ngayThanhLy=?, lyDoThanhLy=?, ghiChu=?, nguoiTao=? "
                + "WHERE maHopDong=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            setInsertParams(ps, hd);
            ps.setInt(15, hd.getMaHopDong());
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi cập nhật hợp đồng: " + e.getMessage(), e);
        }
    }

    // ============================
    // updateTrangThai — dùng cho thanh_ly, huy, het_han
    // ============================
    public int updateTrangThai(int maHopDong, String trangThai) {
        String sql = "UPDATE HOPDONGLAODONG SET trangThai=? WHERE maHopDong=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, trangThai);
            ps.setInt(2, maHopDong);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi cập nhật trạng thái hợp đồng: " + e.getMessage(), e);
        }
    }

    // ============================
    // updateThanhLy
    // ============================
    public int updateThanhLy(int maHopDong, LocalDate ngayThanhLy, String lyDoThanhLy, String nguoiThanhLy) {
        String sql = "UPDATE HOPDONGLAODONG "
                + "SET trangThai=?, ngayThanhLy=?, lyDoThanhLy=?, nguoiThanhLy=?, "
                + "ngayHetHieuLuc = CASE "
                + "    WHEN ngayHetHieuLuc IS NULL OR ngayHetHieuLuc > ? THEN ? "
                + "    ELSE ngayHetHieuLuc "
                + "END "
                + "WHERE maHopDong=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            Date liquidationDate = ngayThanhLy != null ? Date.valueOf(ngayThanhLy) : null;
            ps.setString(1, HRMConstants.TRANG_THAI_THANH_LY);
            ps.setDate(2, liquidationDate);
            ps.setString(3, lyDoThanhLy);
            ps.setString(4, nguoiThanhLy);
            ps.setDate(5, liquidationDate);
            ps.setDate(6, liquidationDate);
            ps.setInt(7, maHopDong);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi thanh lý hợp đồng: " + e.getMessage(), e);
        }
    }

    // ============================
    // updateApproval — dùng khi phê duyệt hợp đồng
    // ============================
    public int updateApproval(int maHopDong, String trangThai, String nguoiDuyet, LocalDateTime ngayDuyet) {
        String sql = "UPDATE HOPDONGLAODONG SET trangThai=?, nguoiDuyet=?, ngayDuyet=? WHERE maHopDong=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, trangThai);
            ps.setString(2, nguoiDuyet);
            ps.setTimestamp(3, Timestamp.valueOf(ngayDuyet));
            ps.setInt(4, maHopDong);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi phê duyệt hợp đồng: " + e.getMessage(), e);
        }
    }

    // ============================
    // findById
    // ============================
    public HopDongLaoDong findById(int maHopDong) {
        String sql = buildJoinQuery("WHERE h.maHopDong = ?", "LIMIT 1");
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maHopDong);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    HopDongLaoDong hd = mapRow(rs);
                    hd.setTenNV(rs.getString("hoTen"));
                    return hd;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tải hợp đồng theo ID: " + e.getMessage(), e);
        }
        return null;
    }

    // ============================
    // findByMaNV - with tenNV transient
    // ============================
    public List<HopDongLaoDong> findByMaNV(String maNV) {
        String sql = buildJoinQuery("WHERE h.maNV = ?", "ORDER BY h.ngayHieuLuc DESC");
        List<HopDongLaoDong> result = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maNV);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    HopDongLaoDong hd = mapRow(rs);
                    hd.setTenNV(rs.getString("hoTen"));
                    result.add(hd);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tải hợp đồng theo nhân viên: " + e.getMessage(), e);
        }
        return result;
    }

    // ============================
    // findHieuLuc - returns current active contract
    // ============================
    public HopDongLaoDong findHieuLuc(String maNV) {
        String sql = buildJoinQuery("WHERE h.maNV = ? AND h.trangThai = '" + HRMConstants.TRANG_THAI_HIEU_LUC + "'", "LIMIT 1");
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maNV);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    HopDongLaoDong hd = mapRow(rs);
                    hd.setTenNV(rs.getString("hoTen"));
                    return hd;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tải hợp đồng hiệu lực: " + e.getMessage(), e);
        }
        return null;
    }

    public HopDongLaoDong findChoDuyet(String maNV) {
        String sql = buildJoinQuery("WHERE h.maNV = ? AND h.trangThai = '" + HRMConstants.TRANG_THAI_CHO_DUYET + "'", "LIMIT 1");
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maNV);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    HopDongLaoDong hd = mapRow(rs);
                    hd.setTenNV(rs.getString("hoTen"));
                    return hd;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tải hợp đồng chờ duyệt: " + e.getMessage(), e);
        }
        return null;
    }

    // ============================
    // findSapHetHan - contracts expiring in N days, with tenNV
    // ============================
    public List<HopDongLaoDong> findSapHetHan(int soNgay) {
        LocalDate today = LocalDate.now();
        LocalDate deadline = today.plusDays(soNgay);
        String sql = buildJoinQuery(
                "WHERE h.trangThai = '" + HRMConstants.TRANG_THAI_HIEU_LUC + "' AND h.ngayHetHieuLuc IS NOT NULL "
                        + "AND h.ngayHetHieuLuc >= ? AND h.ngayHetHieuLuc <= ?",
                "ORDER BY h.ngayHetHieuLuc ASC");
        List<HopDongLaoDong> result = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(today));
            ps.setDate(2, Date.valueOf(deadline));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    HopDongLaoDong hd = mapRow(rs);
                    hd.setTenNV(rs.getString("hoTen"));
                    result.add(hd);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tải hợp đồng sắp hết hạn: " + e.getMessage(), e);
        }
        return result;
    }

    // ============================
    // findAll - with tenNV
    // ============================
    public List<HopDongLaoDong> findAll() {
        String sql = buildJoinQuery("", "ORDER BY h.maHopDong DESC");
        List<HopDongLaoDong> result = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                HopDongLaoDong hd = mapRow(rs);
                hd.setTenNV(rs.getString("hoTen"));
                result.add(hd);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tải danh sách hợp đồng: " + e.getMessage(), e);
        }
        return result;
    }

    // ============================
    // countByYearMonth - for auto code generation
    // ============================
    public int countByYearMonth(int year, int month) {
        String prefix = String.format("HD-%04d%02d-", year, month);
        String sql = "SELECT COUNT(*) FROM HOPDONGLAODONG WHERE soHopDong LIKE ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, prefix + "%");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi countByYearMonth: " + e.getMessage());
        }
        return 0;
    }

    // ============================
    // expireHetHanContracts — cập nhật trạng thái hết hạn
    // ============================
    /**
     * Cập nhật trangThai → 'het_han' cho các hợp đồng đã qua ngayHetHieuLuc.
     * Gọi lazy mỗi khi load danh sách hợp đồng.
     */
    public int expireHetHanContracts() {
        String sql = "UPDATE HOPDONGLAODONG SET trangThai='" + HRMConstants.TRANG_THAI_HET_HAN + "' "
                + "WHERE trangThai='" + HRMConstants.TRANG_THAI_HIEU_LUC + "' AND ngayHetHieuLuc IS NOT NULL AND ngayHetHieuLuc < CURDATE()";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Loi expire hop dong het han: " + e.getMessage(), e);
        }
    }

    // ============================
    // existsBySoHopDong
    // ============================
    public boolean existsBySoHopDong(String soHopDong, int excludeId) {
        String sql = "SELECT COUNT(*) FROM HOPDONGLAODONG WHERE soHopDong=? AND maHopDong<>?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, soHopDong);
            ps.setInt(2, excludeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi kiểm tra số hợp đồng: " + e.getMessage(), e);
        }
        return false;
    }

    // ============================
    // Private helpers
    // ============================
    private String buildJoinQuery(String whereClause, String orderAndLimit) {
        return "SELECT h.*, t.hoTen FROM HOPDONGLAODONG h "
                + "LEFT JOIN THONGTINCANHAN t ON h.maNV = t.maNV "
                + (whereClause.isEmpty() ? "" : whereClause + " ")
                + orderAndLimit;
    }

    private void setInsertParams(PreparedStatement ps, HopDongLaoDong hd) throws SQLException {
        ps.setString(1, hd.getSoHopDong());
        ps.setString(2, hd.getMaNV());
        ps.setString(3, hd.getLoaiHopDong());
        ps.setLong(4, hd.getLuongCoSo());
        ps.setDate(5, hd.getNgayKy() != null ? Date.valueOf(hd.getNgayKy()) : null);
        ps.setDate(6, hd.getNgayHieuLuc() != null ? Date.valueOf(hd.getNgayHieuLuc()) : null);
        ps.setDate(7, hd.getNgayHetHieuLuc() != null ? Date.valueOf(hd.getNgayHetHieuLuc()) : null);
        ps.setString(8, hd.getFileDinhKem());
        ps.setString(9, hd.getNoiDung());
        ps.setString(10, hd.getTrangThai());
        ps.setDate(11, hd.getNgayThanhLy() != null ? Date.valueOf(hd.getNgayThanhLy()) : null);
        ps.setString(12, hd.getLyDoThanhLy());
        ps.setString(13, hd.getGhiChu());
        ps.setString(14, hd.getNguoiTao());
    }
}
