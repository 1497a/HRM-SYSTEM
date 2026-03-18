package com.hrm.dao;

import com.hrm.model.SoDungPhep;
import com.hrm.model.DonXinNghiPhep;
import com.hrm.model.LoaiPhep;
import com.hrm.util.DaoHelper;
import com.hrm.util.DatabaseConnection;
import com.hrm.util.HRMConstants;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC repository cho Leave Management.
 * Singleton pattern.
 *
 * NOTE: Existing DonXinNghiPhep model uses English field names:
 *   id, employeeId, leaveTypeCode, startDate, endDate, totalDays, reason,
 *   approverId, approverNote, status, employeeName, leaveTypeName
 *
 * SoDungPhep model:
 *   employeeId, leaveTypeCode, year, totalDays, usedDays
 *
 * LoaiPhep model:
 *   code, name, defaultDays, paid
 *
 * These map to DB columns:
 *   DONXINNGHIPHEP: maDon, maNV, maLoaiPhep, tuNgay, denNgay, soNgayNghi,
 *                   lyDo, nguoiDuyet(maTaiKhoan), ngayDuyet, lyDoTuChoi, trangThai
 *   SODUNGPHEP:     maSoDung, maNV, nam, maLoaiPhep, soNgayDuocCap, soNgayDaDung
 *   LOAIPHEP:       maLoaiPhep, tenLoaiPhep, coLuong, canChungTu, soNgayToiDa, trangThai
 */
public class NghiPhepDAO {

    private static final String BASE_SELECT = "SELECT d.*, t.hoTen, lp.tenLoaiPhep, t2.hoTen AS tenNguoiDuyet "
            + "FROM DONXINNGHIPHEP d "
            + "LEFT JOIN THONGTINCANHAN t ON d.maNV = t.maNV "
            + "LEFT JOIN LOAIPHEP lp ON d.maLoaiPhep = lp.maLoaiPhep "
            + "LEFT JOIN THONGTINCANHAN t2 ON d.nguoiDuyet = t2.maNV ";
    private static final String BASE_SELECT_WITH_BONHIEM = BASE_SELECT
            + "LEFT JOIN BONHIEM b ON d.maNV = b.maNV AND b.trangThai = 'hieu_luc' AND b.loaiBoNhiem = 'chinh' ";
    private static NghiPhepDAO instance;
    private NghiPhepDAO() {}

    public static synchronized NghiPhepDAO getInstance() {
        if (instance == null) {
            instance = new NghiPhepDAO();
        }
        return instance;
    }

    // =====================================================================
    // DONXINNGHIPHEP (DonXinNghiPhep)
    // =====================================================================
    /** Insert a new leave request. Returns generated maDon. */
    public int insert(DonXinNghiPhep req) {
        String sql = "INSERT INTO DONXINNGHIPHEP (maNV, maLoaiPhep, tuNgay, denNgay, soNgayNghi, "
                + "lyDo, trangThai) VALUES (?,?,?,?,?,?,?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, req.getMaNV());
            ps.setString(2, req.getMaLoaiPhep());
            ps.setDate(3, req.getTuNgay() != null ? Date.valueOf(req.getTuNgay()) : null);
            ps.setDate(4, req.getDenNgay() != null ? Date.valueOf(req.getDenNgay()) : null);
            ps.setDouble(5, req.getSoNgayNghi());
            ps.setString(6, req.getLyDo());
            ps.setString(7, statusToDb(req.getTrangThai()));
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    req.setId(id);
                    return id;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi thêm đơn nghỉ phép: " + e.getMessage(), e);
        }
        return 0;
    }

    public void updateTrangThai(int maDon, String trangThai, String nguoiDuyet,
                                 LocalDateTime ngayDuyet, String lyDoTuChoi) {
        String sql = "UPDATE DONXINNGHIPHEP SET trangThai=?, nguoiDuyet=?, ngayDuyet=?, "
                + "lyDoTuChoi=? WHERE maDon=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, trangThai);
            if (nguoiDuyet != null && !nguoiDuyet.isEmpty()) ps.setString(2, nguoiDuyet); else ps.setNull(2, Types.VARCHAR);
            ps.setTimestamp(3, ngayDuyet != null ? Timestamp.valueOf(ngayDuyet) : null);
            ps.setString(4, lyDoTuChoi);
            ps.setInt(5, maDon);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi cập nhật trạng thái đơn: " + e.getMessage(), e);
        }
    }

    /** Returns all leave requests with tenNV and tenLoaiPhep transients loaded. */
    public List<DonXinNghiPhep> findAll() {
        String sql = BASE_SELECT + "ORDER BY d.maDon DESC";
        List<DonXinNghiPhep> result = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(mapLeaveRequest(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tải danh sách đơn nghỉ: " + e.getMessage(), e);
        }
        return result;
    }

    public List<DonXinNghiPhep> findByMaNV(String maNV) {
        String sql = BASE_SELECT + "WHERE d.maNV=? ORDER BY d.maDon DESC";
        List<DonXinNghiPhep> result = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maNV);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapLeaveRequest(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tải đơn theo nhân viên: " + e.getMessage(), e);
        }
        return result;
    }

    public List<DonXinNghiPhep> findChoDuyet() {
        String sql = BASE_SELECT + "WHERE d.trangThai='cho_duyet' ORDER BY d.maDon DESC";
        List<DonXinNghiPhep> result = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(mapLeaveRequest(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tải đơn chờ duyệt: " + e.getMessage(), e);
        }
        return result;
    }

    public List<DonXinNghiPhep> findAllByScope(com.hrm.model.DataScope scope, String currentMaNV) {
        return getByScopeAndStatus(scope, currentMaNV, null);
    }

    public List<DonXinNghiPhep> findChoDuyetByScope(com.hrm.model.DataScope scope, String currentMaNV) {
        return getByScopeAndStatus(scope, currentMaNV, "cho_duyet");
    }

    private List<DonXinNghiPhep> getByScopeAndStatus(com.hrm.model.DataScope scope, String currentMaNV, String statusValue) {
        List<DonXinNghiPhep> result = new ArrayList<>();
        if (scope == com.hrm.model.DataScope.NONE) return result;
        if (scope == com.hrm.model.DataScope.DEPT) {
            return getByDeptSubtree(currentMaNV, statusValue);
        }
        String sqlCondition = "";
        switch (scope) {
            case ALL:
                sqlCondition = " WHERE 1=1 ";
                break;
            case TEAM:
                sqlCondition = " WHERE b.maQuanLy = ? ";
                break;
            case SELF:
                sqlCondition = " WHERE d.maNV = ? ";
                break;
            default:
                return result;
        }
        if (statusValue != null) {
            sqlCondition += " AND d.trangThai = '" + statusValue + "' ";
        }
        String sql = BASE_SELECT_WITH_BONHIEM + sqlCondition + " ORDER BY d.maDon DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (scope != com.hrm.model.DataScope.ALL) {
                ps.setString(1, currentMaNV);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapLeaveRequest(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tải đơn theo scope: " + e.getMessage(), e);
        }
        return result;
    }

    private List<DonXinNghiPhep> getByDeptSubtree(String currentMaNV, String statusValue) {
        List<DonXinNghiPhep> result = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection()) {
            java.util.Set<String> depts = DaoHelper.getDeptSubtree(currentMaNV, conn);
            if (depts.isEmpty()) return result;
            String ph = String.join(",", java.util.Collections.nCopies(depts.size(), "?"));
            String statusClause = statusValue != null ? " AND d.trangThai = '" + statusValue + "'" : "";
            String sql = BASE_SELECT_WITH_BONHIEM
                    + "WHERE b.maPhongBan IN (" + ph + ")" + statusClause + " ORDER BY d.maDon DESC";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                int i = 1; for (String d : depts) ps.setString(i++, d);
                try (ResultSet rs = ps.executeQuery()) { while (rs.next()) result.add(mapLeaveRequest(rs)); }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tải đơn nghỉ phép theo phòng ban: " + e.getMessage(), e);
        }
        return result;
    }

    public DonXinNghiPhep findById(int maDon) {
        String sql = BASE_SELECT + "WHERE d.maDon=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maDon);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapLeaveRequest(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tìm đơn nghỉ phép: " + e.getMessage(), e);
        }
        return null;
    }

    private DonXinNghiPhep mapLeaveRequest(ResultSet rs) throws SQLException {
        DonXinNghiPhep req = new DonXinNghiPhep();
        req.setId(rs.getInt("maDon"));
        req.setMaNV(rs.getString("maNV"));
        req.setMaLoaiPhep(rs.getString("maLoaiPhep"));
        Date tuNgay = rs.getDate("tuNgay");
        if (tuNgay != null) req.setTuNgay(tuNgay.toLocalDate());
        Date denNgay = rs.getDate("denNgay");
        if (denNgay != null) req.setDenNgay(denNgay.toLocalDate());
        req.setSoNgayNghi(rs.getInt("soNgayNghi"));
        req.setLyDo(rs.getString("lyDo"));
        req.setLyDoTuChoi(rs.getString("lyDoTuChoi"));
        String nd = rs.getString("nguoiDuyet");
        if (nd != null) req.setMaNguoiDuyet(nd);
        String tt = rs.getString("trangThai");
        req.setTrangThai(dbToStatus(tt));
        applyLeaveRequestDetails(req, rs);
        return req;
    }

    private void applyLeaveRequestDetails(DonXinNghiPhep req, ResultSet rs) {
        try {
            req.setTenNhanVien(rs.getString("hoTen"));
        } catch (SQLException ignored) {}
        try {
            req.setTenLoaiPhep(rs.getString("tenLoaiPhep"));
        } catch (SQLException ignored) {}
        try {
            req.setTenNguoiDuyet(rs.getString("tenNguoiDuyet"));
        } catch (SQLException ignored) {}
    }

    private String statusToDb(DonXinNghiPhep.TrangThai status) {
        if (status == null) return "cho_duyet";
        switch (status) {
            case DA_DUYET:  return "da_duyet";
            case TU_CHOI:  return "tu_choi";
            case HUY: return "huy";
            default:        return "cho_duyet";
        }
    }

    private DonXinNghiPhep.TrangThai dbToStatus(String db) {
        if (db == null) return DonXinNghiPhep.TrangThai.CHO_DUYET;
        switch (db) {
            case "da_duyet": return DonXinNghiPhep.TrangThai.DA_DUYET;
            case "tu_choi":  return DonXinNghiPhep.TrangThai.TU_CHOI;
            case "huy":      return DonXinNghiPhep.TrangThai.HUY;
            default:         return DonXinNghiPhep.TrangThai.CHO_DUYET;
        }
    }

    // =====================================================================
    // SODUNGPHEP (SoDungPhep)
    // =====================================================================
    public SoDungPhep findByMaNVAndNamAndLoai(String maNV, int nam, String maLoaiPhep) {
        String sql = "SELECT s.*, lp.tenLoaiPhep FROM SODUNGPHEP s "
                + "LEFT JOIN LOAIPHEP lp ON s.maLoaiPhep = lp.maLoaiPhep "
                + "WHERE s.maNV=? AND s.nam=? AND s.maLoaiPhep=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maNV);
            ps.setInt(2, nam);
            ps.setString(3, maLoaiPhep);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapLeaveBalance(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tải số dụng phép: " + e.getMessage(), e);
        }
        return null;
    }

    public List<SoDungPhep> findByMaNVAndNam(String maNV, int nam) {
        String sql = "SELECT s.*, lp.tenLoaiPhep FROM SODUNGPHEP s "
                + "LEFT JOIN LOAIPHEP lp ON s.maLoaiPhep = lp.maLoaiPhep "
                + "WHERE s.maNV=? AND s.nam=?";
        List<SoDungPhep> result = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maNV);
            ps.setInt(2, nam);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapLeaveBalance(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tải số dụng phép theo năm: " + e.getMessage(), e);
        }
        return result;
    }

    public void insertSoDungPhep(String maNV, int nam, String maLoaiPhep, double soNgayDuocCap) {
        String sql = "INSERT INTO SODUNGPHEP (maNV, nam, maLoaiPhep, soNgayDuocCap, soNgayDaDung) "
                + "VALUES (?,?,?,?,0)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maNV);
            ps.setInt(2, nam);
            ps.setString(3, maLoaiPhep);
            ps.setDouble(4, soNgayDuocCap);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi thêm số dụng phép: " + e.getMessage(), e);
        }
    }

    public void capNhatSoDaDung(String maNV, int nam, String maLoaiPhep, double soNgayThem) {
        String sql = "UPDATE SODUNGPHEP SET soNgayDaDung = soNgayDaDung + ? "
                + "WHERE maNV=? AND nam=? AND maLoaiPhep=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, soNgayThem);
            ps.setString(2, maNV);
            ps.setInt(3, nam);
            ps.setString(4, maLoaiPhep);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi cập nhật số ngày đã dùng: " + e.getMessage(), e);
        }
    }

    private SoDungPhep mapLeaveBalance(ResultSet rs) throws SQLException {
        String maNV = rs.getString("maNV");
        String maLoaiPhep = rs.getString("maLoaiPhep");
        int nam = rs.getInt("nam");
        int soNgayDuocCap = rs.getInt("soNgayDuocCap");
        SoDungPhep lb = new SoDungPhep(maNV, maLoaiPhep, nam, soNgayDuocCap);
        lb.setSoNgayDaDung(rs.getInt("soNgayDaDung"));
        return lb;
    }

    // =====================================================================
    // LOAIPHEP (LoaiPhep)
    // =====================================================================
    public List<LoaiPhep> findAllLoaiPhep() {
        String sql = "SELECT maLoaiPhep, tenLoaiPhep, coLuong, soNgayToiDa, trangThai FROM LOAIPHEP ORDER BY maLoaiPhep";
        List<LoaiPhep> result = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(mapLeaveType(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tải loại phép: " + e.getMessage(), e);
        }
        return result;
    }

    public List<LoaiPhep> findActiveLoaiPhep() {
        String sql = "SELECT maLoaiPhep, tenLoaiPhep, coLuong, soNgayToiDa, trangThai FROM LOAIPHEP "
                + "WHERE trangThai='" + HRMConstants.TRANG_THAI_HOAT_DONG + "' ORDER BY maLoaiPhep";
        List<LoaiPhep> result = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(mapLeaveType(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tải loại phép hoạt động: " + e.getMessage(), e);
        }
        return result;
    }

    public LoaiPhep findLoaiPhepById(String maLoaiPhep) {
        String sql = "SELECT maLoaiPhep, tenLoaiPhep, coLuong, soNgayToiDa, trangThai FROM LOAIPHEP WHERE maLoaiPhep=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maLoaiPhep);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapLeaveType(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tìm loại phép: " + e.getMessage(), e);
        }
        return null;
    }

    private LoaiPhep mapLeaveType(ResultSet rs) throws SQLException {
        String code = rs.getString("maLoaiPhep");
        String name = rs.getString("tenLoaiPhep");
        int defaultDays = rs.getInt("soNgayToiDa");
        boolean paid = rs.getBoolean("coLuong");
        return new LoaiPhep(code, name, defaultDays, paid);
    }

}
