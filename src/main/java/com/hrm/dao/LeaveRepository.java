package com.hrm.repo;

import com.hrm.model.LeaveBalance;
import com.hrm.model.LeaveRequest;
import com.hrm.model.LeaveType;
import com.hrm.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC repository cho Leave Management.
 * Singleton pattern.
 *
 * NOTE: Existing LeaveRequest model uses English field names:
 *   id, employeeId, leaveTypeCode, startDate, endDate, totalDays, reason,
 *   approverId, approverNote, status, employeeName, leaveTypeName
 *
 * LeaveBalance model:
 *   employeeId, leaveTypeCode, year, totalDays, usedDays
 *
 * LeaveType model:
 *   code, name, defaultDays, paid
 *
 * These map to DB columns:
 *   DONXINNGHIPHEP: maDon, maNV, maLoaiPhep, tuNgay, denNgay, soNgayNghi,
 *                   lyDo, nguoiDuyet, ngayDuyet, lyDoTuChoi, trangThai
 *   SODUNGPHEP:     maSoDung, maNV, nam, maLoaiPhep, soNgayDuocCap, soNgayDaDung
 *   LOAIPHEP:       maLoaiPhep, tenLoaiPhep, coLuong, canChungTu, soNgayToiDa, trangThai
 */
public class LeaveRepository {

    private static LeaveRepository instance;

    private LeaveRepository() {}

    public static synchronized LeaveRepository getInstance() {
        if (instance == null) {
            instance = new LeaveRepository();
        }
        return instance;
    }

    // =====================================================================
    // DONXINNGHIPHEP (LeaveRequest)
    // =====================================================================

    /** Insert a new leave request. Returns generated maDon. */
    public int insert(LeaveRequest req) {
        String sql = "INSERT INTO DONXINNGHIPHEP (maNV, maLoaiPhep, tuNgay, denNgay, soNgayNghi, "
                + "lyDo, trangThai) VALUES (?,?,?,?,?,?,?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, req.getEmployeeId());
            ps.setString(2, req.getLeaveTypeCode());
            ps.setDate(3, req.getStartDate() != null ? Date.valueOf(req.getStartDate()) : null);
            ps.setDate(4, req.getEndDate() != null ? Date.valueOf(req.getEndDate()) : null);
            ps.setDouble(5, req.getTotalDays());
            ps.setString(6, req.getReason());
            ps.setString(7, statusToDb(req.getStatus()));
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

    public void updateTrangThai(int maDon, String trangThai, int nguoiDuyet,
                                 LocalDateTime ngayDuyet, String lyDoTuChoi) {
        String sql = "UPDATE DONXINNGHIPHEP SET trangThai=?, nguoiDuyet=?, ngayDuyet=?, "
                + "lyDoTuChoi=? WHERE maDon=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, trangThai);
            if (nguoiDuyet > 0) ps.setInt(2, nguoiDuyet); else ps.setNull(2, Types.INTEGER);
            ps.setTimestamp(3, ngayDuyet != null ? Timestamp.valueOf(ngayDuyet) : null);
            ps.setString(4, lyDoTuChoi);
            ps.setInt(5, maDon);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi cập nhật trạng thái đơn: " + e.getMessage(), e);
        }
    }

    /** Returns all leave requests with tenNV and tenLoaiPhep transients loaded. */
    public List<LeaveRequest> findAll() {
        String sql = "SELECT d.*, t.hoTen, lp.tenLoaiPhep FROM DONXINNGHIPHEP d "
                + "LEFT JOIN THONGTINCANHAN t ON d.maNV = t.maNV "
                + "LEFT JOIN LOAIPHEP lp ON d.maLoaiPhep = lp.maLoaiPhep "
                + "ORDER BY d.maDon DESC";
        List<LeaveRequest> result = new ArrayList<>();
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

    public List<LeaveRequest> findByMaNV(int maNV) {
        String sql = "SELECT d.*, t.hoTen, lp.tenLoaiPhep FROM DONXINNGHIPHEP d "
                + "LEFT JOIN THONGTINCANHAN t ON d.maNV = t.maNV "
                + "LEFT JOIN LOAIPHEP lp ON d.maLoaiPhep = lp.maLoaiPhep "
                + "WHERE d.maNV=? ORDER BY d.maDon DESC";
        List<LeaveRequest> result = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maNV);
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

    public List<LeaveRequest> findChoDuyet() {
        String sql = "SELECT d.*, t.hoTen, lp.tenLoaiPhep FROM DONXINNGHIPHEP d "
                + "LEFT JOIN THONGTINCANHAN t ON d.maNV = t.maNV "
                + "LEFT JOIN LOAIPHEP lp ON d.maLoaiPhep = lp.maLoaiPhep "
                + "WHERE d.trangThai='cho_duyet' ORDER BY d.maDon DESC";
        List<LeaveRequest> result = new ArrayList<>();
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

    public LeaveRequest findById(int maDon) {
        String sql = "SELECT d.*, t.hoTen, lp.tenLoaiPhep FROM DONXINNGHIPHEP d "
                + "LEFT JOIN THONGTINCANHAN t ON d.maNV = t.maNV "
                + "LEFT JOIN LOAIPHEP lp ON d.maLoaiPhep = lp.maLoaiPhep "
                + "WHERE d.maDon=?";
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

    private LeaveRequest mapLeaveRequest(ResultSet rs) throws SQLException {
        LeaveRequest req = new LeaveRequest();
        req.setId(rs.getInt("maDon"));
        req.setEmployeeId(rs.getInt("maNV"));
        req.setLeaveTypeCode(rs.getString("maLoaiPhep"));
        Date tuNgay = rs.getDate("tuNgay");
        if (tuNgay != null) req.setStartDate(tuNgay.toLocalDate());
        Date denNgay = rs.getDate("denNgay");
        if (denNgay != null) req.setEndDate(denNgay.toLocalDate());
        req.setTotalDays((int) rs.getDouble("soNgayNghi"));
        req.setReason(rs.getString("lyDo"));
        req.setApproverNote(rs.getString("lyDoTuChoi"));
        int nd = rs.getInt("nguoiDuyet");
        if (!rs.wasNull()) req.setApproverId(nd);
        String tt = rs.getString("trangThai");
        req.setStatus(dbToStatus(tt));
        // transient
        try { req.setEmployeeName(rs.getString("hoTen")); } catch (SQLException ignored) {}
        try { req.setLeaveTypeName(rs.getString("tenLoaiPhep")); } catch (SQLException ignored) {}
        return req;
    }

    private String statusToDb(LeaveRequest.Status status) {
        if (status == null) return "cho_duyet";
        switch (status) {
            case APPROVED:  return "da_duyet";
            case REJECTED:  return "tu_choi";
            case CANCELLED: return "huy";
            default:        return "cho_duyet";
        }
    }

    private LeaveRequest.Status dbToStatus(String db) {
        if (db == null) return LeaveRequest.Status.PENDING;
        switch (db) {
            case "da_duyet": return LeaveRequest.Status.APPROVED;
            case "tu_choi":  return LeaveRequest.Status.REJECTED;
            case "huy":      return LeaveRequest.Status.CANCELLED;
            default:         return LeaveRequest.Status.PENDING;
        }
    }

    // =====================================================================
    // SODUNGPHEP (LeaveBalance)
    // =====================================================================

    public LeaveBalance findByMaNVAndNamAndLoai(int maNV, int nam, String maLoaiPhep) {
        String sql = "SELECT s.*, lp.tenLoaiPhep FROM SODUNGPHEP s "
                + "LEFT JOIN LOAIPHEP lp ON s.maLoaiPhep = lp.maLoaiPhep "
                + "WHERE s.maNV=? AND s.nam=? AND s.maLoaiPhep=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maNV);
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

    public List<LeaveBalance> findByMaNVAndNam(int maNV, int nam) {
        String sql = "SELECT s.*, lp.tenLoaiPhep FROM SODUNGPHEP s "
                + "LEFT JOIN LOAIPHEP lp ON s.maLoaiPhep = lp.maLoaiPhep "
                + "WHERE s.maNV=? AND s.nam=?";
        List<LeaveBalance> result = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maNV);
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

    public void insertSoDungPhep(int maNV, int nam, String maLoaiPhep, double soNgayDuocCap) {
        String sql = "INSERT INTO SODUNGPHEP (maNV, nam, maLoaiPhep, soNgayDuocCap, soNgayDaDung) "
                + "VALUES (?,?,?,?,0)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maNV);
            ps.setInt(2, nam);
            ps.setString(3, maLoaiPhep);
            ps.setDouble(4, soNgayDuocCap);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi thêm số dụng phép: " + e.getMessage(), e);
        }
    }

    public void capNhatSoDaDung(int maNV, int nam, String maLoaiPhep, double soNgayThem) {
        String sql = "UPDATE SODUNGPHEP SET soNgayDaDung = soNgayDaDung + ? "
                + "WHERE maNV=? AND nam=? AND maLoaiPhep=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, soNgayThem);
            ps.setInt(2, maNV);
            ps.setInt(3, nam);
            ps.setString(4, maLoaiPhep);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi cập nhật số ngày đã dùng: " + e.getMessage(), e);
        }
    }

    private LeaveBalance mapLeaveBalance(ResultSet rs) throws SQLException {
        int maNV = rs.getInt("maNV");
        String maLoaiPhep = rs.getString("maLoaiPhep");
        int nam = rs.getInt("nam");
        int soNgayDuocCap = (int) rs.getDouble("soNgayDuocCap");
        LeaveBalance lb = new LeaveBalance(maNV, maLoaiPhep, nam, soNgayDuocCap);
        lb.setUsedDays((int) rs.getDouble("soNgayDaDung"));
        return lb;
    }

    // =====================================================================
    // LOAIPHEP (LeaveType)
    // =====================================================================

    public List<LeaveType> findAllLoaiPhep() {
        String sql = "SELECT maLoaiPhep, tenLoaiPhep, coLuong, soNgayToiDa, trangThai FROM LOAIPHEP ORDER BY maLoaiPhep";
        List<LeaveType> result = new ArrayList<>();
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

    public List<LeaveType> findActiveLoaiPhep() {
        String sql = "SELECT maLoaiPhep, tenLoaiPhep, coLuong, soNgayToiDa, trangThai FROM LOAIPHEP "
                + "WHERE trangThai='hoat_dong' ORDER BY maLoaiPhep";
        List<LeaveType> result = new ArrayList<>();
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

    public LeaveType findLoaiPhepById(String maLoaiPhep) {
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

    private LeaveType mapLeaveType(ResultSet rs) throws SQLException {
        String code = rs.getString("maLoaiPhep");
        String name = rs.getString("tenLoaiPhep");
        int defaultDays = rs.getInt("soNgayToiDa");
        boolean paid = rs.getBoolean("coLuong");
        return new LeaveType(code, name, defaultDays, paid);
    }
}
