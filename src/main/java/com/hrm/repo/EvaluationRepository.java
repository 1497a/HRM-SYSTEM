package com.hrm.repo;

import com.hrm.model.EvalCriteria;
import com.hrm.model.EvalCycle;
import com.hrm.model.EvalScore;
import com.hrm.model.EvalSubmission;
import com.hrm.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC repository cho Performance Evaluation.
 * Singleton pattern.
 *
 * NOTE: Existing model field mappings:
 *
 * EvalCycle: id(maDot), name(tenDot), year(nam), quarter(kyDanhGia via mapping),
 *            startDate(tuNgay), endDate(denNgay), status(trangThai)
 *
 * EvalCriteria: id(maTieuChi), name(tenTieuChi), description(moTa),
 *               weight(trongSo from DOTDANHGIA_TIEUCHI), maxScore(diemToiDa), active(trangThai)
 *
 * EvalSubmission: id(maDanhGia), cycleId(maDot), employeeId(maNV), evaluatorId(nguoiDanhGia),
 *                 overallScore(tongDiem), rating(xepLoai), generalComment(nhanXetChung),
 *                 submittedAt(ngayDanhGia), employeeName(transient)
 *
 * EvalScore: criteriaId(maTieuChi), score(diem), comment(nhanXet), criteriaName(transient)
 */
public class EvaluationRepository {

    private static EvaluationRepository instance;

    private EvaluationRepository() {}

    public static synchronized EvaluationRepository getInstance() {
        if (instance == null) {
            instance = new EvaluationRepository();
        }
        return instance;
    }

    // =====================================================================
    // DOTDANHGIA (EvalCycle)
    // =====================================================================

    /** Insert a new evaluation cycle. Returns generated maDot. */
    public int insertCycle(EvalCycle cycle) {
        String sql = "INSERT INTO DOTDANHGIA (tenDot, nam, kyDanhGia, tuNgay, denNgay, moTa, trangThai) "
                + "VALUES (?,?,?,?,?,?,?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, cycle.getName());
            ps.setInt(2, cycle.getYear());
            ps.setString(3, quarterToKy(cycle.getQuarter()));
            ps.setDate(4, cycle.getStartDate() != null ? Date.valueOf(cycle.getStartDate()) : null);
            ps.setDate(5, cycle.getEndDate() != null ? Date.valueOf(cycle.getEndDate()) : null);
            ps.setNull(6, Types.VARCHAR);
            ps.setString(7, cycleStatusToDb(cycle.getStatus()));
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    cycle.setId(id);
                    return id;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi thêm đợt đánh giá: " + e.getMessage(), e);
        }
        return 0;
    }

    public void updateCycle(EvalCycle cycle) {
        String sql = "UPDATE DOTDANHGIA SET tenDot=?, nam=?, kyDanhGia=?, tuNgay=?, denNgay=?, trangThai=? "
                + "WHERE maDot=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cycle.getName());
            ps.setInt(2, cycle.getYear());
            ps.setString(3, quarterToKy(cycle.getQuarter()));
            ps.setDate(4, cycle.getStartDate() != null ? Date.valueOf(cycle.getStartDate()) : null);
            ps.setDate(5, cycle.getEndDate() != null ? Date.valueOf(cycle.getEndDate()) : null);
            ps.setString(6, cycleStatusToDb(cycle.getStatus()));
            ps.setInt(7, cycle.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi cập nhật đợt đánh giá: " + e.getMessage(), e);
        }
    }

    public List<EvalCycle> findAllCycles() {
        String sql = "SELECT maDot, tenDot, nam, kyDanhGia, tuNgay, denNgay, trangThai FROM DOTDANHGIA ORDER BY maDot DESC";
        List<EvalCycle> result = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(mapCycle(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tải đợt đánh giá: " + e.getMessage(), e);
        }
        return result;
    }

    public EvalCycle findCycleById(int maDot) {
        String sql = "SELECT maDot, tenDot, nam, kyDanhGia, tuNgay, denNgay, trangThai FROM DOTDANHGIA WHERE maDot=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maDot);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapCycle(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tìm đợt đánh giá: " + e.getMessage(), e);
        }
        return null;
    }

    private EvalCycle mapCycle(ResultSet rs) throws SQLException {
        EvalCycle cycle = new EvalCycle();
        cycle.setId(rs.getInt("maDot"));
        cycle.setName(rs.getString("tenDot"));
        cycle.setYear(rs.getInt("nam"));
        cycle.setQuarter(kyToQuarter(rs.getString("kyDanhGia")));
        Date tuNgay = rs.getDate("tuNgay");
        if (tuNgay != null) cycle.setStartDate(tuNgay.toLocalDate());
        Date denNgay = rs.getDate("denNgay");
        if (denNgay != null) cycle.setEndDate(denNgay.toLocalDate());
        String tt = rs.getString("trangThai");
        cycle.setStatus(dbToCycleStatus(tt));
        return cycle;
    }

    private String quarterToKy(int quarter) {
        switch (quarter) {
            case 1: return "quy_1";
            case 2: return "quy_2";
            case 3: return "quy_3";
            case 4: return "quy_4";
            default: return "nam";
        }
    }

    private int kyToQuarter(String ky) {
        if (ky == null) return 0;
        switch (ky) {
            case "quy_1": return 1;
            case "quy_2": return 2;
            case "quy_3": return 3;
            case "quy_4": return 4;
            default: return 0;
        }
    }

    private String cycleStatusToDb(EvalCycle.Status status) {
        if (status == null) return "chua_bat_dau";
        switch (status) {
            case OPEN:   return "dang_dien_ra";
            case CLOSED: return "da_ket_thuc";
            default:     return "chua_bat_dau";
        }
    }

    private EvalCycle.Status dbToCycleStatus(String db) {
        if (db == null) return EvalCycle.Status.DRAFT;
        switch (db) {
            case "dang_dien_ra": return EvalCycle.Status.OPEN;
            case "da_ket_thuc":  return EvalCycle.Status.CLOSED;
            default:             return EvalCycle.Status.DRAFT;
        }
    }

    // =====================================================================
    // TIEUCHIDANHGIA + DOTDANHGIA_TIEUCHI (EvalCriteria)
    // =====================================================================

    /**
     * Returns criteria for a cycle with trongSo loaded from DOTDANHGIA_TIEUCHI.
     */
    public List<EvalCriteria> findCriteriaByDot(int maDot) {
        String sql = "SELECT tc.maTieuChi, tc.tenTieuChi, tc.moTa, tc.nhomTieuChi, "
                + "tc.diemToiDa, tc.trangThai, dt.trongSo "
                + "FROM TIEUCHIDANHGIA tc "
                + "JOIN DOTDANHGIA_TIEUCHI dt ON tc.maTieuChi = dt.maTieuChi "
                + "WHERE dt.maDot=? ORDER BY tc.maTieuChi";
        List<EvalCriteria> result = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maDot);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapCriteriaWithTrongSo(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tải tiêu chí theo đợt: " + e.getMessage(), e);
        }
        return result;
    }

    public List<EvalCriteria> findAllCriteria() {
        String sql = "SELECT maTieuChi, tenTieuChi, moTa, nhomTieuChi, diemToiDa, trangThai FROM TIEUCHIDANHGIA ORDER BY maTieuChi";
        List<EvalCriteria> result = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(mapCriteria(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tải tiêu chí: " + e.getMessage(), e);
        }
        return result;
    }

    /**
     * Delete all criteria links for this cycle then batch-insert the provided list.
     */
    public void setCriteriasForDot(int maDot, List<EvalCriteria> criterias) {
        String delSql = "DELETE FROM DOTDANHGIA_TIEUCHI WHERE maDot=?";
        String insSql = "INSERT INTO DOTDANHGIA_TIEUCHI (maDot, maTieuChi, trongSo, batBuoc) VALUES (?,?,?,1)";
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement del = conn.prepareStatement(delSql)) {
                    del.setInt(1, maDot);
                    del.executeUpdate();
                }
                try (PreparedStatement ins = conn.prepareStatement(insSql)) {
                    for (EvalCriteria c : criterias) {
                        ins.setInt(1, maDot);
                        ins.setInt(2, c.getId());
                        ins.setInt(3, c.getWeight());
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
            throw new RuntimeException("Lỗi cập nhật tiêu chí cho đợt: " + e.getMessage(), e);
        }
    }

    private EvalCriteria mapCriteria(ResultSet rs) throws SQLException {
        EvalCriteria c = new EvalCriteria();
        c.setId(rs.getInt("maTieuChi"));
        c.setName(rs.getString("tenTieuChi"));
        c.setDescription(rs.getString("moTa"));
        c.setMaxScore((int) rs.getDouble("diemToiDa"));
        String tt = rs.getString("trangThai");
        c.setActive(tt == null || tt.equals("hoat_dong"));
        return c;
    }

    private EvalCriteria mapCriteriaWithTrongSo(ResultSet rs) throws SQLException {
        EvalCriteria c = mapCriteria(rs);
        c.setWeight((int) rs.getDouble("trongSo"));
        return c;
    }

    // =====================================================================
    // DANHGIAHIEUSUAT (EvalSubmission)
    // =====================================================================

    /** Insert a new submission. Returns generated maDanhGia. */
    public int insertSubmission(EvalSubmission sub) {
        String sql = "INSERT INTO DANHGIAHIEUSUAT (maDot, maNV, nguoiDanhGia, tongDiem, xepLoai, "
                + "nhanXetChung, ngayDanhGia, trangThai) VALUES (?,?,?,?,?,?,?,?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, sub.getCycleId());
            ps.setInt(2, sub.getEmployeeId());
            ps.setInt(3, sub.getEvaluatorId());
            ps.setDouble(4, sub.getOverallScore());
            ps.setString(5, ratingToDb(sub.getRating()));
            ps.setString(6, sub.getGeneralComment());
            ps.setTimestamp(7, sub.getSubmittedAt() != null ? Timestamp.valueOf(sub.getSubmittedAt()) : Timestamp.valueOf(java.time.LocalDateTime.now()));
            ps.setString(8, "da_danh_gia");
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    sub.setId(id);
                    return id;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi thêm đánh giá: " + e.getMessage(), e);
        }
        return 0;
    }

    public void updateSubmission(EvalSubmission sub) {
        String sql = "UPDATE DANHGIAHIEUSUAT SET tongDiem=?, xepLoai=?, nhanXetChung=?, "
                + "ngayDanhGia=?, trangThai=? WHERE maDanhGia=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, sub.getOverallScore());
            ps.setString(2, ratingToDb(sub.getRating()));
            ps.setString(3, sub.getGeneralComment());
            ps.setTimestamp(4, sub.getSubmittedAt() != null ? Timestamp.valueOf(sub.getSubmittedAt()) : Timestamp.valueOf(java.time.LocalDateTime.now()));
            ps.setString(5, "da_danh_gia");
            ps.setInt(6, sub.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi cập nhật đánh giá: " + e.getMessage(), e);
        }
    }

    public EvalSubmission findSubmissionByDotAndNV(int maDot, int maNV) {
        String sql = "SELECT dg.*, t.hoTen FROM DANHGIAHIEUSUAT dg "
                + "LEFT JOIN THONGTINCANHAN t ON dg.maNV = t.maNV "
                + "WHERE dg.maDot=? AND dg.maNV=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maDot);
            ps.setInt(2, maNV);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapSubmission(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tìm đánh giá: " + e.getMessage(), e);
        }
        return null;
    }

    public List<EvalSubmission> findByDot(int maDot) {
        String sql = "SELECT dg.*, t.hoTen FROM DANHGIAHIEUSUAT dg "
                + "LEFT JOIN THONGTINCANHAN t ON dg.maNV = t.maNV "
                + "WHERE dg.maDot=? ORDER BY dg.maDanhGia";
        List<EvalSubmission> result = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maDot);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapSubmission(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tải đánh giá theo đợt: " + e.getMessage(), e);
        }
        return result;
    }

    public List<EvalSubmission> findAll() {
        String sql = "SELECT dg.*, t.hoTen FROM DANHGIAHIEUSUAT dg "
                + "LEFT JOIN THONGTINCANHAN t ON dg.maNV = t.maNV "
                + "ORDER BY dg.maDanhGia DESC";
        List<EvalSubmission> result = new ArrayList<>();
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

    private EvalSubmission mapSubmission(ResultSet rs) throws SQLException {
        EvalSubmission sub = new EvalSubmission();
        sub.setId(rs.getInt("maDanhGia"));
        sub.setCycleId(rs.getInt("maDot"));
        sub.setEmployeeId(rs.getInt("maNV"));
        sub.setEvaluatorId(rs.getInt("nguoiDanhGia"));
        sub.setOverallScore(rs.getDouble("tongDiem"));
        String xl = rs.getString("xepLoai");
        sub.setRating(dbToRating(xl));
        sub.setGeneralComment(rs.getString("nhanXetChung"));
        Timestamp ngayDG = rs.getTimestamp("ngayDanhGia");
        if (ngayDG != null) sub.setSubmittedAt(ngayDG.toLocalDateTime());
        // transient
        try { sub.setEmployeeName(rs.getString("hoTen")); } catch (SQLException ignored) {}
        return sub;
    }

    private String ratingToDb(EvalSubmission.Rating rating) {
        if (rating == null) return "trung_binh";
        switch (rating) {
            case EXCELLENT: return "xuat_sac";
            case GOOD:      return "tot";
            case FAIR:      return "kha";
            case AVERAGE:   return "trung_binh";
            default:        return "yeu";
        }
    }

    private EvalSubmission.Rating dbToRating(String db) {
        if (db == null) return EvalSubmission.Rating.POOR;
        switch (db) {
            case "xuat_sac":   return EvalSubmission.Rating.EXCELLENT;
            case "tot":        return EvalSubmission.Rating.GOOD;
            case "kha":        return EvalSubmission.Rating.FAIR;
            case "trung_binh": return EvalSubmission.Rating.AVERAGE;
            default:           return EvalSubmission.Rating.POOR;
        }
    }

    // =====================================================================
    // CHITIETDANHGIA (EvalScore)
    // =====================================================================

    /**
     * Delete all scores for this evaluation then batch-insert the new list.
     */
    public void saveScores(int maDanhGia, List<EvalScore> scores) {
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
                    for (EvalScore score : scores) {
                        ins.setInt(1, maDanhGia);
                        ins.setInt(2, score.getCriteriaId());
                        ins.setDouble(3, score.getScore());
                        ins.setString(4, score.getComment());
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
            throw new RuntimeException("Lỗi lưu điểm đánh giá: " + e.getMessage(), e);
        }
    }

    public List<EvalScore> findScoresByDanhGia(int maDanhGia) {
        String sql = "SELECT cd.maChiTiet, cd.maDanhGia, cd.maTieuChi, cd.diem, cd.nhanXet, "
                + "tc.tenTieuChi, dt.trongSo FROM CHITIETDANHGIA cd "
                + "LEFT JOIN TIEUCHIDANHGIA tc ON cd.maTieuChi = tc.maTieuChi "
                + "LEFT JOIN DANHGIAHIEUSUAT dg ON cd.maDanhGia = dg.maDanhGia "
                + "LEFT JOIN DOTDANHGIA_TIEUCHI dt ON dg.maDot = dt.maDot AND cd.maTieuChi = dt.maTieuChi "
                + "WHERE cd.maDanhGia=? ORDER BY cd.maTieuChi";
        List<EvalScore> result = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maDanhGia);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    EvalScore score = new EvalScore();
                    score.setCriteriaId(rs.getInt("maTieuChi"));
                    score.setScore(rs.getDouble("diem"));
                    score.setComment(rs.getString("nhanXet"));
                    // transient
                    try { score.setCriteriaName(rs.getString("tenTieuChi")); } catch (SQLException ignored) {}
                    try { score.setWeight((int) rs.getDouble("trongSo")); } catch (SQLException ignored) {}
                    result.add(score);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tải điểm đánh giá: " + e.getMessage(), e);
        }
        return result;
    }

    // Legacy compatibility methods used by existing GUI
    public List<EvalCycle> getAllCycles() { return findAllCycles(); }
    public EvalCycle getCycle(int id) { return findCycleById(id); }
    public List<EvalCriteria> getAllCriteria() { return findAllCriteria(); }
    public List<EvalSubmission> getSubmissionsByCycle(int cycleId) { return findByDot(cycleId); }
    public List<EvalSubmission> getAllSubmissions() { return findAll(); }
    public EvalSubmission findSubmission(int cycleId, int employeeId) { return findSubmissionByDotAndNV(cycleId, employeeId); }

    public List<EvalCycle> getOpenCycles() {
        List<EvalCycle> all = findAllCycles();
        List<EvalCycle> open = new ArrayList<>();
        for (EvalCycle c : all) {
            if (c.isOpen()) open.add(c);
        }
        return open;
    }

    public EvalCriteria getCriteria(int id) {
        String sql = "SELECT maTieuChi, tenTieuChi, moTa, nhomTieuChi, diemToiDa, trangThai FROM TIEUCHIDANHGIA WHERE maTieuChi=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapCriteria(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tìm tiêu chí: " + e.getMessage(), e);
        }
        return null;
    }

    public EvalCriteria saveCriteria(EvalCriteria c) {
        if (c.getId() == 0) {
            String sql = "INSERT INTO TIEUCHIDANHGIA (tenTieuChi, moTa, diemToiDa, trangThai) VALUES (?,?,?,?)";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, c.getName());
                ps.setString(2, c.getDescription());
                ps.setDouble(3, c.getMaxScore());
                ps.setString(4, c.isActive() ? "hoat_dong" : "ngung_hoat_dong");
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) c.setId(keys.getInt(1));
                }
            } catch (SQLException e) {
                throw new RuntimeException("Lỗi thêm tiêu chí: " + e.getMessage(), e);
            }
        } else {
            String sql = "UPDATE TIEUCHIDANHGIA SET tenTieuChi=?, moTa=?, diemToiDa=?, trangThai=? WHERE maTieuChi=?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, c.getName());
                ps.setString(2, c.getDescription());
                ps.setDouble(3, c.getMaxScore());
                ps.setString(4, c.isActive() ? "hoat_dong" : "ngung_hoat_dong");
                ps.setInt(5, c.getId());
                ps.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException("Lỗi cập nhật tiêu chí: " + e.getMessage(), e);
            }
        }
        return c;
    }

    public void deleteCriteria(int id) {
        String sql = "UPDATE TIEUCHIDANHGIA SET trangThai='ngung_hoat_dong' WHERE maTieuChi=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi xóa tiêu chí: " + e.getMessage(), e);
        }
    }

    public int getTotalWeight() {
        // For legacy usage, sum trongSo of the latest open cycle, or return 0
        return 0;
    }

    public EvalSubmission saveSubmission(EvalSubmission sub) {
        if (sub.getId() == 0) {
            insertSubmission(sub);
        } else {
            updateSubmission(sub);
        }
        return sub;
    }

    public List<EvalSubmission> getSubmissionsByEmployee(int employeeId) {
        String sql = "SELECT dg.*, t.hoTen FROM DANHGIAHIEUSUAT dg "
                + "LEFT JOIN THONGTINCANHAN t ON dg.maNV = t.maNV "
                + "WHERE dg.maNV=? ORDER BY dg.ngayDanhGia DESC";
        List<EvalSubmission> result = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(mapSubmission(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tải đánh giá theo nhân viên: " + e.getMessage(), e);
        }
        return result;
    }

    public EvalSubmission getSubmission(int id) {
        String sql = "SELECT dg.*, t.hoTen FROM DANHGIAHIEUSUAT dg "
                + "LEFT JOIN THONGTINCANHAN t ON dg.maNV = t.maNV "
                + "WHERE dg.maDanhGia=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapSubmission(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tìm đánh giá: " + e.getMessage(), e);
        }
        return null;
    }
}
