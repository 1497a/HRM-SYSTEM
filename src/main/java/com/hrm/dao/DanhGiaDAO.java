package com.hrm.dao;

import com.hrm.model.TieuChiDanhGia;
import com.hrm.model.DotDanhGia;
import com.hrm.model.ChiTietDanhGia;
import com.hrm.model.DanhGiaHieuSuat;
import com.hrm.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC repository cho Performance Evaluation.
 * Singleton pattern.
 *
 * Đã cập nhật hoàn chỉnh để:
 * - maNV, nguoiDanhGia là String (ví dụ: "NV001", "NV002")
 * - Sử dụng maTieuChi thay vì id cho nhất quán
 * - Thêm xử lý lỗi chi tiết, comment rõ ràng
 * - Đồng bộ với model DotDanhGia, TieuChiDanhGia đã sửa (maDot, maTieuChi)
 */
public class DanhGiaDAO {

    private static DanhGiaDAO instance;

    private DanhGiaDAO() {}

    public static synchronized DanhGiaDAO getInstance() {
        if (instance == null) {
            instance = new DanhGiaDAO();
        }
        return instance;
    }

    // =====================================================================
    // DOTDANHGIA (Đợt đánh giá)
    // =====================================================================

    /** Thêm mới đợt đánh giá, trả về maDot được sinh tự động */
    public int insertCycle(DotDanhGia cycle) {
        String sql = "INSERT INTO DOTDANHGIA (tenDot, nam, kyDanhGia, tuNgay, denNgay, trangThai) "
                   + "VALUES (?,?,?,?,?,?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, cycle.getTenDot());
            ps.setInt(2, cycle.getNam());
            ps.setString(3, cycle.getKyDanhGia());
            ps.setDate(4, cycle.getTuNgay() != null ? Date.valueOf(cycle.getTuNgay()) : null);
            ps.setDate(5, cycle.getDenNgay() != null ? Date.valueOf(cycle.getDenNgay()) : null);
            ps.setString(6, cycleStatusToDb(cycle.getTrangThai()));
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int maDot = keys.getInt(1);
                    cycle.setId(maDot);
                    return maDot;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi thêm đợt đánh giá: " + e.getMessage(), e);
        }
        return 0;
    }

    /** Cập nhật thông tin đợt đánh giá */
    public void updateCycle(DotDanhGia cycle) {
        String sql = "UPDATE DOTDANHGIA SET tenDot=?, nam=?, kyDanhGia=?, tuNgay=?, denNgay=?, trangThai=? "
                   + "WHERE maDot=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cycle.getTenDot());
            ps.setInt(2, cycle.getNam());
            ps.setString(3, cycle.getKyDanhGia());
            ps.setDate(4, cycle.getTuNgay() != null ? Date.valueOf(cycle.getTuNgay()) : null);
            ps.setDate(5, cycle.getDenNgay() != null ? Date.valueOf(cycle.getDenNgay()) : null);
            ps.setString(6, cycleStatusToDb(cycle.getTrangThai()));
            ps.setInt(7, cycle.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi cập nhật đợt đánh giá #" + cycle.getId() + ": " + e.getMessage(), e);
        }
    }

    /** Lấy tất cả các đợt đánh giá */
    public List<DotDanhGia> findAllCycles() {
        String sql = "SELECT maDot, tenDot, nam, kyDanhGia, tuNgay, denNgay, trangThai "
                   + "FROM DOTDANHGIA ORDER BY nam DESC, kyDanhGia DESC";
        List<DotDanhGia> result = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(mapCycle(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tải tất cả đợt đánh giá: " + e.getMessage(), e);
        }
        return result;
    }

    /** Tìm đợt đánh giá theo mã */
    public DotDanhGia findCycleById(int maDot) {
        String sql = "SELECT maDot, tenDot, nam, kyDanhGia, tuNgay, denNgay, trangThai "
                   + "FROM DOTDANHGIA WHERE maDot=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maDot);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapCycle(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tìm đợt đánh giá #" + maDot + ": " + e.getMessage(), e);
        }
        return null;
    }

    private DotDanhGia mapCycle(ResultSet rs) throws SQLException {
        DotDanhGia cycle = new DotDanhGia();
        cycle.setId(rs.getInt("maDot"));
        cycle.setTenDot(rs.getString("tenDot"));
        cycle.setNam(rs.getInt("nam"));
        cycle.setKyDanhGia(rs.getString("kyDanhGia"));
        Date tuNgay = rs.getDate("tuNgay");
        if (tuNgay != null) cycle.setTuNgay(tuNgay.toLocalDate());
        Date denNgay = rs.getDate("denNgay");
        if (denNgay != null) cycle.setDenNgay(denNgay.toLocalDate());
        cycle.setTrangThai(dbToCycleStatus(rs.getString("trangThai")));
        return cycle;
    }

    private String cycleStatusToDb(DotDanhGia.TrangThai status) {
        if (status == null) return "chua_bat_dau";
        return switch (status) {
            case DANG_DIEN_RA -> "dang_dien_ra";
            case DA_KET_THUC  -> "da_ket_thuc";
            default           -> "chua_bat_dau";
        };
    }

    private DotDanhGia.TrangThai dbToCycleStatus(String db) {
        if (db == null) return DotDanhGia.TrangThai.CHUA_BAT_DAU;
        return switch (db) {
            case "dang_dien_ra" -> DotDanhGia.TrangThai.DANG_DIEN_RA;
            case "da_ket_thuc"  -> DotDanhGia.TrangThai.DA_KET_THUC;
            default             -> DotDanhGia.TrangThai.CHUA_BAT_DAU;
        };
    }

    // =====================================================================
    // TIEUCHIDANHGIA + DOTDANHGIA_TIEUCHI (Tiêu chí đánh giá)
    // =====================================================================

    /** Lấy danh sách tiêu chí của một đợt, kèm trọng số */
    public List<TieuChiDanhGia> findCriteriaByDot(int maDot) {
        String sql = "SELECT tc.maTieuChi, tc.tenTieuChi, tc.moTa, tc.nhomTieuChi, "
                   + "tc.diemToiDa, tc.trangThai, dt.trongSo "
                   + "FROM TIEUCHIDANHGIA tc "
                   + "JOIN DOTDANHGIA_TIEUCHI dt ON tc.maTieuChi = dt.maTieuChi "
                   + "WHERE dt.maDot=? ORDER BY tc.maTieuChi";
        List<TieuChiDanhGia> result = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maDot);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    TieuChiDanhGia c = mapCriteria(rs);
                    c.setTrongSo(rs.getDouble("trongSo"));
                    result.add(c);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tải tiêu chí của đợt #" + maDot + ": " + e.getMessage(), e);
        }
        return result;
    }

    /** Lấy tất cả tiêu chí đang hoạt động */
    public List<TieuChiDanhGia> findAllCriteria() {
        String sql = "SELECT maTieuChi, tenTieuChi, moTa, nhomTieuChi, diemToiDa, trangThai "
                   + "FROM TIEUCHIDANHGIA WHERE trangThai = 'hoatDong' OR trangThai IS NULL ORDER BY maTieuChi";
        List<TieuChiDanhGia> result = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(mapCriteria(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tải tất cả tiêu chí: " + e.getMessage(), e);
        }
        return result;
    }

    /** Xóa toàn bộ liên kết tiêu chí cũ của đợt, sau đó thêm mới batch */
    public void setCriteriasForDot(int maDot, List<TieuChiDanhGia> criterias) {
        String delSql = "DELETE FROM DOTDANHGIA_TIEUCHI WHERE maDot=?";
        String insSql = "INSERT INTO DOTDANHGIA_TIEUCHI (maDot, maTieuChi, trongSo, batBuoc) VALUES (?,?,?,1)";
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Xóa cũ
                try (PreparedStatement del = conn.prepareStatement(delSql)) {
                    del.setInt(1, maDot);
                    del.executeUpdate();
                }
                // Thêm mới
                try (PreparedStatement ins = conn.prepareStatement(insSql)) {
                    for (TieuChiDanhGia c : criterias) {
                        ins.setInt(1, maDot);
                        ins.setInt(2, c.getMaTieuChi());
                        ins.setDouble(3, c.getTrongSo());
                        ins.addBatch();
                    }
                    ins.executeBatch();
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi cập nhật tiêu chí cho đợt #" + maDot + ": " + e.getMessage(), e);
        }
    }

    private TieuChiDanhGia mapCriteria(ResultSet rs) throws SQLException {
        TieuChiDanhGia c = new TieuChiDanhGia();
        c.setMaTieuChi(rs.getInt("maTieuChi"));
        c.setTenTieuChi(rs.getString("tenTieuChi"));
        c.setMoTa(rs.getString("moTa"));
        c.setNhomTieuChi(rs.getString("nhomTieuChi"));
        c.setDiemToiDa(rs.getDouble("diemToiDa"));
        String tt = rs.getString("trangThai");
        c.setHoatDong(tt == null || "hoatDong".equals(tt));
        return c;
    }

    // =====================================================================
    // DANHGIAHIEUSUAT (Đánh giá hiệu suất)
    // =====================================================================

    /** Thêm mới một đánh giá hiệu suất, trả về maDanhGia */
    public int insertSubmission(DanhGiaHieuSuat sub) {
        String sql = "INSERT INTO DANHGIAHIEUSUAT (maDot, maNV, nguoiDanhGia, tongDiem, xepLoai, "
                   + "nhanXetChung, ngayDanhGia, trangThai) VALUES (?,?,?,?,?,?,?,?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, sub.getDotDanhGiaId());
            ps.setString(2, sub.getNhanVienId()); // String
            ps.setString(3, sub.getNguoiDanhGiaId()); // String
            ps.setDouble(4, sub.getTongDiem());
            ps.setString(5, ratingToDb(sub.getXepLoai()));
            ps.setString(6, sub.getNhanXetChung());
            ps.setTimestamp(7, sub.getNgayDanhGia() != null ? Timestamp.valueOf(sub.getNgayDanhGia()) : Timestamp.valueOf(LocalDateTime.now()));
            ps.setString(8, "da_danh_gia");
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int maDanhGia = keys.getInt(1);
                    sub.setId(maDanhGia);
                    return maDanhGia;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi thêm đánh giá hiệu suất: " + e.getMessage(), e);
        }
        return 0;
    }

    /** Cập nhật đánh giá hiệu suất */
    public void updateSubmission(DanhGiaHieuSuat sub) {
        String sql = "UPDATE DANHGIAHIEUSUAT SET tongDiem=?, xepLoai=?, nhanXetChung=?, "
                   + "ngayDanhGia=?, trangThai=? WHERE maDanhGia=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, sub.getTongDiem());
            ps.setString(2, ratingToDb(sub.getXepLoai()));
            ps.setString(3, sub.getNhanXetChung());
            ps.setTimestamp(4, sub.getNgayDanhGia() != null ? Timestamp.valueOf(sub.getNgayDanhGia()) : Timestamp.valueOf(LocalDateTime.now()));
            ps.setString(5, "da_danh_gia");
            ps.setInt(6, sub.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi cập nhật đánh giá #" + sub.getId() + ": " + e.getMessage(), e);
        }
    }

    /** Tìm đánh giá theo đợt và nhân viên */
    public DanhGiaHieuSuat findSubmissionByDotAndNV(int maDot, String maNV) {
        String sql = "SELECT dg.*, t.hoTen as tenNhanVien, e.hoTen as tenNguoiDanhGia, d.tenDot "
                   + "FROM DANHGIAHIEUSUAT dg "
                   + "LEFT JOIN THONGTINCANHAN t ON dg.maNV = t.maNV "
                   + "LEFT JOIN THONGTINCANHAN e ON dg.nguoiDanhGia = e.maNV "
                   + "LEFT JOIN DOTDANHGIA d ON dg.maDot = d.maDot "
                   + "WHERE dg.maDot=? AND dg.maNV=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maDot);
            ps.setString(2, maNV);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapSubmission(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tìm đánh giá của NV " + maNV + " trong đợt #" + maDot + ": " + e.getMessage(), e);
        }
        return null;
    }

    /** Lấy tất cả đánh giá của một đợt */
    public List<DanhGiaHieuSuat> findByDot(int maDot) {
        String sql = "SELECT dg.*, t.hoTen as tenNhanVien, e.hoTen as tenNguoiDanhGia, d.tenDot "
                   + "FROM DANHGIAHIEUSUAT dg "
                   + "LEFT JOIN THONGTINCANHAN t ON dg.maNV = t.maNV "
                   + "LEFT JOIN THONGTINCANHAN e ON dg.nguoiDanhGia = e.maNV "
                   + "LEFT JOIN DOTDANHGIA d ON dg.maDot = d.maDot "
                   + "WHERE dg.maDot=? ORDER BY dg.maDanhGia";
        List<DanhGiaHieuSuat> result = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maDot);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapSubmission(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tải đánh giá theo đợt #" + maDot + ": " + e.getMessage(), e);
        }
        return result;
    }

    /** Lấy tất cả đánh giá */
    public List<DanhGiaHieuSuat> findAll() {
        String sql = "SELECT dg.*, t.hoTen as tenNhanVien, e.hoTen as tenNguoiDanhGia, d.tenDot "
                   + "FROM DANHGIAHIEUSUAT dg "
                   + "LEFT JOIN THONGTINCANHAN t ON dg.maNV = t.maNV "
                   + "LEFT JOIN THONGTINCANHAN e ON dg.nguoiDanhGia = e.maNV "
                   + "LEFT JOIN DOTDANHGIA d ON dg.maDot = d.maDot "
                   + "ORDER BY dg.ngayDanhGia DESC";
        List<DanhGiaHieuSuat> result = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(mapSubmission(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tải tất cả đánh giá: " + e.getMessage(), e);
        }
        return result;
    }

    /** Lấy đánh giá theo phạm vi quyền (scope) */
    public List<DanhGiaHieuSuat> findAllByScope(com.hrm.model.DataScope scope, String currentMaNV) {
        List<DanhGiaHieuSuat> result = new ArrayList<>();
        if (scope == com.hrm.model.DataScope.NONE) return result;

        if (scope == com.hrm.model.DataScope.DEPT) {
            return findAllByDeptSubtree(currentMaNV);
        }

        String sqlBase = "SELECT dg.*, t.hoTen as tenNhanVien, e.hoTen as tenNguoiDanhGia, d.tenDot "
                       + "FROM DANHGIAHIEUSUAT dg "
                       + "LEFT JOIN THONGTINCANHAN t ON dg.maNV = t.maNV "
                       + "LEFT JOIN THONGTINCANHAN e ON dg.nguoiDanhGia = e.maNV "
                       + "LEFT JOIN DOTDANHGIA d ON dg.maDot = d.maDot "
                       + "LEFT JOIN BONHIEM b ON dg.maNV = b.maNV AND b.trangThai = 'hieu_luc' AND b.loaiBoNhiem = 'chinh' ";

        String sqlCondition;
        switch (scope) {
            case ALL:
                sqlCondition = "WHERE 1=1 ";
                break;
            case TEAM:
                sqlCondition = "WHERE b.maQuanLy = ? ";
                break;
            case SELF:
                sqlCondition = "WHERE (dg.maNV = ? OR dg.nguoiDanhGia = ?) ";
                break;
            default:
                return result;
        }

        String sql = sqlBase + sqlCondition + "ORDER BY dg.ngayDanhGia DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (scope == com.hrm.model.DataScope.SELF) {
                ps.setString(1, currentMaNV);
                ps.setString(2, currentMaNV);
            } else if (scope != com.hrm.model.DataScope.ALL) {
                ps.setString(1, currentMaNV);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapSubmission(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tải đánh giá theo phạm vi: " + e.getMessage(), e);
        }
        return result;
    }

    private java.util.Set<String> getDeptSubtree(String currentMaNV, java.sql.Connection conn) throws SQLException {
        String rootSql = "SELECT b.maPhongBan FROM BONHIEM b WHERE b.maNV=? AND b.trangThai='hieu_luc' AND b.loaiBoNhiem='chinh' LIMIT 1";
        String rootDept = null;
        try (PreparedStatement ps = conn.prepareStatement(rootSql)) {
            ps.setString(1, currentMaNV);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) rootDept = rs.getString(1); }
        }
        java.util.Set<String> depts = new java.util.LinkedHashSet<>();
        if (rootDept == null) return depts;
        java.util.Queue<String> queue = new java.util.LinkedList<>();
        queue.add(rootDept);
        String childSql = "SELECT maPhongBan FROM PHONGBAN WHERE phongBanCha=?";
        while (!queue.isEmpty()) {
            String cur = queue.poll();
            if (depts.add(cur)) {
                try (PreparedStatement ps = conn.prepareStatement(childSql)) {
                    ps.setString(1, cur);
                    try (ResultSet rs = ps.executeQuery()) { while (rs.next()) queue.add(rs.getString(1)); }
                }
            }
        }
        return depts;
    }

    private List<DanhGiaHieuSuat> findAllByDeptSubtree(String currentMaNV) {
        List<DanhGiaHieuSuat> result = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection()) {
            java.util.Set<String> depts = getDeptSubtree(currentMaNV, conn);
            if (depts.isEmpty()) return result;
            String ph = String.join(",", java.util.Collections.nCopies(depts.size(), "?"));
            String sql = "SELECT dg.*, t.hoTen as tenNhanVien, e.hoTen as tenNguoiDanhGia, d.tenDot "
                       + "FROM DANHGIAHIEUSUAT dg "
                       + "LEFT JOIN THONGTINCANHAN t ON dg.maNV = t.maNV "
                       + "LEFT JOIN THONGTINCANHAN e ON dg.nguoiDanhGia = e.maNV "
                       + "LEFT JOIN DOTDANHGIA d ON dg.maDot = d.maDot "
                       + "LEFT JOIN BONHIEM b ON dg.maNV = b.maNV AND b.trangThai = 'hieu_luc' AND b.loaiBoNhiem = 'chinh' "
                       + "WHERE b.maPhongBan IN (" + ph + ") ORDER BY dg.ngayDanhGia DESC";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                int i = 1; for (String d : depts) ps.setString(i++, d);
                try (ResultSet rs = ps.executeQuery()) { while (rs.next()) result.add(mapSubmission(rs)); }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tải đánh giá theo phòng ban: " + e.getMessage(), e);
        }
        return result;
    }

    private DanhGiaHieuSuat mapSubmission(ResultSet rs) throws SQLException {
        DanhGiaHieuSuat sub = new DanhGiaHieuSuat();
        sub.setId(rs.getInt("maDanhGia"));
        sub.setDotDanhGiaId(rs.getInt("maDot"));
        sub.setNhanVienId(rs.getString("maNV"));
        sub.setNguoiDanhGiaId(rs.getString("nguoiDanhGia"));
        sub.setTongDiem(rs.getDouble("tongDiem"));
        sub.setXepLoai(dbToRating(rs.getString("xepLoai")));
        sub.setNhanXetChung(rs.getString("nhanXetChung"));
        Timestamp ngayDG = rs.getTimestamp("ngayDanhGia");
        if (ngayDG != null) sub.setNgayDanhGia(ngayDG.toLocalDateTime());
        // Transient fields
        try { sub.setTenNhanVien(rs.getString("tenNhanVien")); } catch (SQLException ignored) {}
        try { sub.setTenNguoiDanhGia(rs.getString("tenNguoiDanhGia")); } catch (SQLException ignored) {}
        try { sub.setTenDot(rs.getString("tenDot")); } catch (SQLException ignored) {}
        return sub;
    }

    private String ratingToDb(DanhGiaHieuSuat.XepLoai rating) {
        if (rating == null) return "trung_binh";
        return switch (rating) {
            case XUAT_SAC   -> "xuat_sac";
            case TOT        -> "tot";
            case KHA        -> "kha";
            case TRUNG_BINH -> "trung_binh";
            default         -> "yeu";
        };
    }

    private DanhGiaHieuSuat.XepLoai dbToRating(String db) {
        if (db == null) return DanhGiaHieuSuat.XepLoai.YEU;
        return switch (db) {
            case "xuat_sac"   -> DanhGiaHieuSuat.XepLoai.XUAT_SAC;
            case "tot"        -> DanhGiaHieuSuat.XepLoai.TOT;
            case "kha"        -> DanhGiaHieuSuat.XepLoai.KHA;
            case "trung_binh" -> DanhGiaHieuSuat.XepLoai.TRUNG_BINH;
            default           -> DanhGiaHieuSuat.XepLoai.YEU;
        };
    }

    // =====================================================================
    // CHITIETDANHGIA (Chi tiết đánh giá)
    // =====================================================================

    /** Xóa toàn bộ chi tiết cũ và thêm mới batch */
    public void saveScores(int maDanhGia, List<ChiTietDanhGia> scores) {
        String delSql = "DELETE FROM CHITIETDANHGIA WHERE maDanhGia=?";
        String insSql = "INSERT INTO CHITIETDANHGIA (maDanhGia, maTieuChi, diem, nhanXet) VALUES (?,?,?,?)";
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement del = conn.prepareStatement(delSql)) {
                    del.setInt(1, maDanhGia);
                    del.executeUpdate();
                }
                try (PreparedStatement ins = conn.prepareStatement(insSql)) {
                    for (ChiTietDanhGia score : scores) {
                        ins.setInt(1, maDanhGia);
                        ins.setInt(2, score.getTieuChiId());
                        ins.setDouble(3, score.getDiem());
                        ins.setString(4, score.getNhanXet());
                        ins.addBatch();
                    }
                    ins.executeBatch();
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi lưu chi tiết điểm cho đánh giá #" + maDanhGia + ": " + e.getMessage(), e);
        }
    }

    /** Lấy chi tiết điểm của một đánh giá */
    public List<ChiTietDanhGia> findScoresByDanhGia(int maDanhGia) {
        String sql = "SELECT cd.maChiTiet, cd.maDanhGia, cd.maTieuChi, cd.diem, cd.nhanXet, "
                   + "tc.tenTieuChi, dt.trongSo "
                   + "FROM CHITIETDANHGIA cd "
                   + "LEFT JOIN TIEUCHIDANHGIA tc ON cd.maTieuChi = tc.maTieuChi "
                   + "LEFT JOIN DANHGIAHIEUSUAT dg ON cd.maDanhGia = dg.maDanhGia "
                   + "LEFT JOIN DOTDANHGIA_TIEUCHI dt ON dg.maDot = dt.maDot AND cd.maTieuChi = dt.maTieuChi "
                   + "WHERE cd.maDanhGia=? ORDER BY cd.maTieuChi";
        List<ChiTietDanhGia> result = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maDanhGia);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ChiTietDanhGia score = new ChiTietDanhGia();
                    score.setTieuChiId(rs.getInt("maTieuChi"));
                    score.setDiem(rs.getDouble("diem"));
                    score.setNhanXet(rs.getString("nhanXet"));
                    try { score.setTenTieuChi(rs.getString("tenTieuChi")); } catch (SQLException ignored) {}
                    try { score.setTrongSo(rs.getDouble("trongSo")); } catch (SQLException ignored) {}
                    result.add(score);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tải chi tiết điểm cho đánh giá #" + maDanhGia + ": " + e.getMessage(), e);
        }
        return result;
    }

    // =====================================================================
    // Legacy / Helper methods (dùng cho GUI cũ nếu cần)
    // =====================================================================

    public List<DotDanhGia> getAllCycles() {
        return findAllCycles();
    }

    public DotDanhGia getCycle(int id) {
        return findCycleById(id);
    }

    public List<TieuChiDanhGia> getAllCriteria() {
        return findAllCriteria();
    }

    public List<DanhGiaHieuSuat> getSubmissionsByCycle(int cycleId) {
        return findByDot(cycleId);
    }

    public List<DanhGiaHieuSuat> getAllSubmissions() {
        return findAll();
    }

    public DanhGiaHieuSuat findSubmission(int cycleId, String employeeId) {
        return findSubmissionByDotAndNV(cycleId, employeeId);
    }

    public List<DotDanhGia> getOpenCycles() {
        List<DotDanhGia> all = findAllCycles();
        List<DotDanhGia> open = new ArrayList<>();
        for (DotDanhGia c : all) {
            if (c.getTrangThai() == DotDanhGia.TrangThai.DANG_DIEN_RA) {
                open.add(c);
            }
        }
        return open;
    }

    public TieuChiDanhGia getCriteria(int maTieuChi) {
        String sql = "SELECT maTieuChi, tenTieuChi, moTa, nhomTieuChi, diemToiDa, trangThai "
                   + "FROM TIEUCHIDANHGIA WHERE maTieuChi=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maTieuChi);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapCriteria(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tìm tiêu chí #" + maTieuChi + ": " + e.getMessage(), e);
        }
        return null;
    }

    public TieuChiDanhGia saveCriteria(TieuChiDanhGia c) {
        if (c.getMaTieuChi() == 0) {
            String sql = "INSERT INTO TIEUCHIDANHGIA (tenTieuChi, moTa, nhomTieuChi, diemToiDa, trangThai) "
                       + "VALUES (?,?,?,?,?)";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, c.getTenTieuChi());
                ps.setString(2, c.getMoTa());
                ps.setString(3, c.getNhomTieuChi());
                ps.setDouble(4, c.getDiemToiDa());
                ps.setString(5, c.isHoatDong() ? "hoatDong" : "ngung_hoat_dong");
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) c.setMaTieuChi(keys.getInt(1));
                }
            } catch (SQLException e) {
                throw new RuntimeException("Lỗi thêm tiêu chí: " + e.getMessage(), e);
            }
        } else {
            String sql = "UPDATE TIEUCHIDANHGIA SET tenTieuChi=?, moTa=?, nhomTieuChi=?, diemToiDa=?, trangThai=? "
                       + "WHERE maTieuChi=?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, c.getTenTieuChi());
                ps.setString(2, c.getMoTa());
                ps.setString(3, c.getNhomTieuChi());
                ps.setDouble(4, c.getDiemToiDa());
                ps.setString(5, c.isHoatDong() ? "hoatDong" : "ngung_hoat_dong");
                ps.setInt(6, c.getMaTieuChi());
                ps.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException("Lỗi cập nhật tiêu chí #" + c.getMaTieuChi() + ": " + e.getMessage(), e);
            }
        }
        return c;
    }

    public void deleteCriteria(int maTieuChi) {
        String sql = "UPDATE TIEUCHIDANHGIA SET trangThai='ngung_hoat_dong' WHERE maTieuChi=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maTieuChi);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi vô hiệu hóa tiêu chí #" + maTieuChi + ": " + e.getMessage(), e);
        }
    }

    /** Lấy danh sách maNV đã được đánh giá trong một đợt */
    public List<String> getEvaluatedMaNVInCycle(int maDot) {
        String sql = "SELECT maNV FROM DANHGIAHIEUSUAT WHERE maDot=?";
        List<String> result = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maDot);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(rs.getString("maNV"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tải danh sách đã đánh giá trong đợt #" + maDot + ": " + e.getMessage(), e);
        }
        return result;
    }

    /** Lấy tất cả đánh giá của một nhân viên */
    public List<DanhGiaHieuSuat> getSubmissionsByEmployee(String maNV) {
        String sql = "SELECT dg.*, t.hoTen as tenNhanVien, e.hoTen as tenNguoiDanhGia, d.tenDot "
                   + "FROM DANHGIAHIEUSUAT dg "
                   + "LEFT JOIN THONGTINCANHAN t ON dg.maNV = t.maNV "
                   + "LEFT JOIN THONGTINCANHAN e ON dg.nguoiDanhGia = e.maNV "
                   + "LEFT JOIN DOTDANHGIA d ON dg.maDot = d.maDot "
                   + "WHERE dg.maNV=? ORDER BY dg.ngayDanhGia DESC";
        List<DanhGiaHieuSuat> result = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maNV);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(mapSubmission(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tải đánh giá của NV " + maNV + ": " + e.getMessage(), e);
        }
        return result;
    }

    /** Lấy tất cả đánh giá liên quan đến người dùng (là người được đánh giá hoặc người đánh giá) */
    public List<DanhGiaHieuSuat> getSubmissionsRelatedToUser(String maNV) {
        String sql = "SELECT dg.*, t.hoTen as tenNhanVien, e.hoTen as tenNguoiDanhGia, d.tenDot "
                   + "FROM DANHGIAHIEUSUAT dg "
                   + "LEFT JOIN THONGTINCANHAN t ON dg.maNV = t.maNV "
                   + "LEFT JOIN THONGTINCANHAN e ON dg.nguoiDanhGia = e.maNV "
                   + "LEFT JOIN DOTDANHGIA d ON dg.maDot = d.maDot "
                   + "WHERE dg.maNV=? OR dg.nguoiDanhGia=? ORDER BY dg.ngayDanhGia DESC";
        List<DanhGiaHieuSuat> result = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maNV);
            ps.setString(2, maNV);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(mapSubmission(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tải đánh giá liên quan đến NV " + maNV + ": " + e.getMessage(), e);
        }
        return result;
    }

    /** Lấy đánh giá của các nhân viên trực thuộc (cấp dưới) của một quản lý */
    public List<DanhGiaHieuSuat> getSubmissionsForManagedEmployees(String managerMaNV) {
        String sql = "SELECT dg.*, t.hoTen as tenNhanVien, e.hoTen as tenNguoiDanhGia, d.tenDot "
                   + "FROM DANHGIAHIEUSUAT dg "
                   + "LEFT JOIN THONGTINCANHAN t ON dg.maNV = t.maNV "
                   + "LEFT JOIN THONGTINCANHAN e ON dg.nguoiDanhGia = e.maNV "
                   + "LEFT JOIN DOTDANHGIA d ON dg.maDot = d.maDot "
                   + "LEFT JOIN BONHIEM b ON dg.maNV = b.maNV AND b.trangThai='hieu_luc' AND b.loaiBoNhiem='chinh' "
                   + "WHERE b.maQuanLy=? ORDER BY dg.ngayDanhGia DESC";
        List<DanhGiaHieuSuat> result = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, managerMaNV);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapSubmission(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tải đánh giá cấp dưới của quản lý " + managerMaNV + ": " + e.getMessage(), e);
        }
        return result;
    }

    /** Lấy đánh giá của toàn bộ nhân viên trong một phòng ban */
    public List<DanhGiaHieuSuat> getSubmissionsForDepartment(String maPhongBan) {
        String sql = "SELECT dg.*, t.hoTen as tenNhanVien, e.hoTen as tenNguoiDanhGia, d.tenDot "
                   + "FROM DANHGIAHIEUSUAT dg "
                   + "LEFT JOIN THONGTINCANHAN t ON dg.maNV = t.maNV "
                   + "LEFT JOIN THONGTINCANHAN e ON dg.nguoiDanhGia = e.maNV "
                   + "LEFT JOIN DOTDANHGIA d ON dg.maDot = d.maDot "
                   + "LEFT JOIN BONHIEM b ON dg.maNV = b.maNV AND b.trangThai='hieu_luc' AND b.loaiBoNhiem='chinh' "
                   + "WHERE b.maPhongBan=? ORDER BY dg.ngayDanhGia DESC";
        List<DanhGiaHieuSuat> result = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maPhongBan);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapSubmission(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tải đánh giá theo phòng ban " + maPhongBan + ": " + e.getMessage(), e);
        }
        return result;
    }
}