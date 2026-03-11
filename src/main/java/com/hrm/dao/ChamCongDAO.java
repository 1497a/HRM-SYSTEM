package com.hrm.dao;

import com.hrm.model.CaLam;
import com.hrm.model.CauHinhPhuCap;
import com.hrm.model.ChamCong;
import com.hrm.model.DangKyLamThem;
import com.hrm.model.ThanhPhanLuong;
import com.hrm.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC DAO cho Attendance (Ca làm, Chấm công, Đăng ký làm thêm).
 * Singleton pattern.
 */
public class ChamCongDAO {

    private static ChamCongDAO instance;

    private ChamCongDAO() {}

    public static synchronized ChamCongDAO getInstance() {
        if (instance == null) {
            instance = new ChamCongDAO();
        }
        return instance;
    }

    // =====================================================================
    // CA_LAMS
    // =====================================================================

    public List<CaLam> findAllCaLam() {
        String sql = "SELECT maCaLam, tenCaLam, gioBatDau, gioKetThuc, soGioChuan, "
                + "choPhepLamThem, moTa, trangThai FROM CALAM ORDER BY maCaLam";
        List<CaLam> result = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(mapCaLam(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tải danh sách ca làm: " + e.getMessage(), e);
        }
        return result;
    }

    public List<CaLam> findActiveCaLam() {
        String sql = "SELECT maCaLam, tenCaLam, gioBatDau, gioKetThuc, soGioChuan, "
                + "choPhepLamThem, moTa, trangThai FROM CALAM WHERE trangThai = 'hoatDong' ORDER BY maCaLam";
        List<CaLam> result = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(mapCaLam(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tải ca làm hoạt động: " + e.getMessage(), e);
        }
        return result;
    }

    public CaLam findCaLamById(String id) {
        String sql = "SELECT maCaLam, tenCaLam, gioBatDau, gioKetThuc, soGioChuan, "
                + "choPhepLamThem, moTa, trangThai FROM CALAM WHERE maCaLam = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapCaLam(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tìm ca làm: " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * INSERT or UPDATE depending on whether the record already exists.
     */
    public void saveCaLam(CaLam caLam) {
        boolean exists = findCaLamById(caLam.getMaCaLam()) != null;
        if (exists) {
            String sql = "UPDATE CALAM SET tenCaLam=?, gioBatDau=?, gioKetThuc=?, soGioChuan=?, "
                    + "choPhepLamThem=?, moTa=?, trangThai=? WHERE maCaLam=?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, caLam.getTenCaLam());
                ps.setTime(2, Time.valueOf(caLam.getGioBatDau()));
                ps.setTime(3, Time.valueOf(caLam.getGioKetThuc()));
                ps.setDouble(4, caLam.getSoGioChuan());
                ps.setBoolean(5, caLam.isChoPhepLamThem());
                ps.setString(6, caLam.getMoTa());
                ps.setString(7, caLam.getTrangThai() != null ? caLam.getTrangThai().getDbValue() : "hoatDong");
                ps.setString(8, caLam.getMaCaLam());
                ps.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException("Lỗi cập nhật ca làm: " + e.getMessage(), e);
            }
        } else {
            String sql = "INSERT INTO CALAM (maCaLam, tenCaLam, gioBatDau, gioKetThuc, soGioChuan, "
                    + "choPhepLamThem, moTa, trangThai) VALUES (?,?,?,?,?,?,?,?)";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, caLam.getMaCaLam());
                ps.setString(2, caLam.getTenCaLam());
                ps.setTime(3, Time.valueOf(caLam.getGioBatDau()));
                ps.setTime(4, Time.valueOf(caLam.getGioKetThuc()));
                ps.setDouble(5, caLam.getSoGioChuan());
                ps.setBoolean(6, caLam.isChoPhepLamThem());
                ps.setString(7, caLam.getMoTa());
                ps.setString(8, caLam.getTrangThai() != null ? caLam.getTrangThai().getDbValue() : "hoatDong");
                ps.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException("Lỗi thêm ca làm: " + e.getMessage(), e);
            }
        }
    }

    public void deleteCaLam(String id) {
        String sql = "UPDATE CALAM SET trangThai='ngung_hoat_dong' WHERE maCaLam=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi xóa ca làm: " + e.getMessage(), e);
        }
    }

    private CaLam mapCaLam(ResultSet rs) throws SQLException {
        CaLam ca = new CaLam();
        ca.setMaCaLam(rs.getString("maCaLam")); // column name is maCaLam
        ca.setTenCaLam(rs.getString("tenCaLam"));
        Time gioBD = rs.getTime("gioBatDau");
        if (gioBD != null) ca.setGioBatDau(gioBD.toLocalTime());
        Time gioKT = rs.getTime("gioKetThuc");
        if (gioKT != null) ca.setGioKetThuc(gioKT.toLocalTime());
        ca.setSoGioChuan(rs.getDouble("soGioChuan"));
        ca.setChoPhepLamThem(rs.getBoolean("choPhepLamThem"));
        ca.setMoTa(rs.getString("moTa"));
        String tt = rs.getString("trangThai");
        if (tt != null) {
            try {
                ca.setTrangThai(CaLam.TrangThai.fromDbValue(tt));
            } catch (IllegalArgumentException ignored) {
                ca.setTrangThai(CaLam.TrangThai.HOAT_DONG);
            }
        }
        return ca;
    }

    // =====================================================================
    // CHAM_CONGS
    // =====================================================================

    /**
     * INSERT a new ChamCong record. Returns the generated id.
     */
    public int saveChamCong(ChamCong cc) {
        String sql = "INSERT INTO CHAMCONG (maNV, ngay, maCaLam, gioVao, gioRa, soGioLam, "
                + "gioLamThem, trangThai, phuongThucChamCong, ghiChu) "
                + "VALUES (?,?,?,?,?,?,?,?,?,?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, cc.getMaNV());
            ps.setDate(2, Date.valueOf(cc.getNgay()));
            ps.setString(3, cc.getMaCaLam());
            ps.setTimestamp(4, cc.getGioVao() != null ? Timestamp.valueOf(cc.getGioVao()) : null);
            ps.setTimestamp(5, cc.getGioRa() != null ? Timestamp.valueOf(cc.getGioRa()) : null);
            ps.setDouble(6, cc.getSoGioLam());
            ps.setDouble(7, cc.getGioLamThem());
            ps.setString(8, cc.getTrangThai() != null ? cc.getTrangThai().getDbValue() : "dung_gio");
            ps.setString(9, cc.getPhuongThucChamCong() != null ? cc.getPhuongThucChamCong().getDbValue() : "thu_cong");
            ps.setString(10, cc.getGhiChu());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    cc.setMaChamCong(id);
                    return id;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi thêm chấm công: " + e.getMessage(), e);
        }
        return 0;
    }

    public void updateChamCong(ChamCong cc) {
        String sql = "UPDATE CHAMCONG SET gioVao=?, gioRa=?, soGioLam=?, gioLamThem=?, "
                + "trangThai=?, phuongThucChamCong=?, ghiChu=? WHERE maChamCong=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, cc.getGioVao() != null ? Timestamp.valueOf(cc.getGioVao()) : null);
            ps.setTimestamp(2, cc.getGioRa() != null ? Timestamp.valueOf(cc.getGioRa()) : null);
            ps.setDouble(3, cc.getSoGioLam());
            ps.setDouble(4, cc.getGioLamThem());
            ps.setString(5, cc.getTrangThai() != null ? cc.getTrangThai().getDbValue() : "dung_gio");
            ps.setString(6, cc.getPhuongThucChamCong() != null ? cc.getPhuongThucChamCong().getDbValue() : "thu_cong");
            ps.setString(7, cc.getGhiChu());
            ps.setInt(8, cc.getMaChamCong());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi cập nhật chấm công: " + e.getMessage(), e);
        }
    }

    public ChamCong findChamCongByNVAndNgay(String nhanVienId, LocalDate ngay) {
        String sql = "SELECT c.maChamCong, c.maNV, c.ngay, c.maCaLam, c.gioVao, c.gioRa, c.soGioLam, c.gioLamThem, c.trangThai, c.phuongThucChamCong, c.ghiChu, t.hoTen, ca.tenCaLam FROM CHAMCONG c "
                + "LEFT JOIN THONGTINCANHAN t ON c.maNV = t.maNV "
                + "LEFT JOIN CALAM ca ON c.maCaLam = ca.maCaLam "
                + "WHERE c.maNV=? AND c.ngay=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nhanVienId);
            ps.setDate(2, Date.valueOf(ngay));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapChamCong(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tìm chấm công: " + e.getMessage(), e);
        }
        return null;
    }

    public List<ChamCong> findByNVAndThang(String nhanVienId, int thang, int nam) {
        String sql = "SELECT c.maChamCong, c.maNV, c.ngay, c.maCaLam, c.gioVao, c.gioRa, c.soGioLam, c.gioLamThem, c.trangThai, c.phuongThucChamCong, c.ghiChu, t.hoTen, ca.tenCaLam FROM CHAMCONG c "
                + "LEFT JOIN THONGTINCANHAN t ON c.maNV = t.maNV "
                + "LEFT JOIN CALAM ca ON c.maCaLam = ca.maCaLam "
                + "WHERE c.maNV=? AND MONTH(c.ngay)=? AND YEAR(c.ngay)=? ORDER BY c.ngay";
        List<ChamCong> result = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nhanVienId);
            ps.setInt(2, thang);
            ps.setInt(3, nam);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapChamCong(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tải chấm công theo tháng: " + e.getMessage(), e);
        }
        return result;
    }

    public List<ChamCong> findByThang(int thang, int nam) {
        String sql = "SELECT c.maChamCong, c.maNV, c.ngay, c.maCaLam, c.gioVao, c.gioRa, c.soGioLam, c.gioLamThem, c.trangThai, c.phuongThucChamCong, c.ghiChu, t.hoTen, ca.tenCaLam FROM CHAMCONG c "
                + "LEFT JOIN THONGTINCANHAN t ON c.maNV = t.maNV "
                + "LEFT JOIN CALAM ca ON c.maCaLam = ca.maCaLam "
                + "WHERE MONTH(c.ngay)=? AND YEAR(c.ngay)=? ORDER BY c.ngay, c.maNV";
        List<ChamCong> result = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, thang);
            ps.setInt(2, nam);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapChamCong(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tải chấm công tất cả: " + e.getMessage(), e);
        }
        return result;
    }

    public List<ChamCong> findByThangByScope(int thang, int nam, com.hrm.model.DataScope scope, String currentMaNV) {
        List<ChamCong> result = new ArrayList<>();
        if (scope == com.hrm.model.DataScope.NONE) return result;

        if (scope == com.hrm.model.DataScope.DEPT) {
            return findChamCongByDeptSubtree(thang, nam, currentMaNV);
        }

        String sqlBase = "SELECT c.maChamCong, c.maNV, c.ngay, c.maCaLam, c.gioVao, c.gioRa, c.soGioLam, c.gioLamThem, c.trangThai, c.phuongThucChamCong, c.ghiChu, t.hoTen, ca.tenCaLam FROM CHAMCONG c "
                + "LEFT JOIN THONGTINCANHAN t ON c.maNV = t.maNV "
                + "LEFT JOIN CALAM ca ON c.maCaLam = ca.maCaLam "
                + "LEFT JOIN BONHIEM b ON c.maNV = b.maNV AND b.trangThai = 'hieu_luc' AND b.loaiBoNhiem = 'chinh' "
                + "WHERE MONTH(c.ngay)=? AND YEAR(c.ngay)=? ";

        String sqlCondition = "";
        switch (scope) {
            case ALL:
                break;
            case TEAM:
                sqlCondition = " AND b.maQuanLy = ? ";
                break;
            case SELF:
                sqlCondition = " AND c.maNV = ? ";
                break;
            default:
                return result;
        }

        String sql = sqlBase + sqlCondition + " ORDER BY c.ngay, c.maNV";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, thang);
            ps.setInt(2, nam);
            if (scope != com.hrm.model.DataScope.ALL) {
                ps.setString(3, currentMaNV);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapChamCong(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tải chấm công theo scope: " + e.getMessage(), e);
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

    private List<ChamCong> findChamCongByDeptSubtree(int thang, int nam, String currentMaNV) {
        List<ChamCong> result = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection()) {
            java.util.Set<String> depts = getDeptSubtree(currentMaNV, conn);
            if (depts.isEmpty()) return result;
            String ph = String.join(",", java.util.Collections.nCopies(depts.size(), "?"));
            String sql = "SELECT c.maChamCong, c.maNV, c.ngay, c.maCaLam, c.gioVao, c.gioRa, c.soGioLam, c.gioLamThem, c.trangThai, c.phuongThucChamCong, c.ghiChu, t.hoTen, ca.tenCaLam FROM CHAMCONG c "
                    + "LEFT JOIN THONGTINCANHAN t ON c.maNV = t.maNV "
                    + "LEFT JOIN CALAM ca ON c.maCaLam = ca.maCaLam "
                    + "LEFT JOIN BONHIEM b ON c.maNV = b.maNV AND b.trangThai = 'hieu_luc' AND b.loaiBoNhiem = 'chinh' "
                    + "WHERE MONTH(c.ngay)=? AND YEAR(c.ngay)=? AND b.maPhongBan IN (" + ph + ") ORDER BY c.ngay, c.maNV";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, thang); ps.setInt(2, nam);
                int i = 3; for (String d : depts) ps.setString(i++, d);
                try (ResultSet rs = ps.executeQuery()) { while (rs.next()) result.add(mapChamCong(rs)); }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tải chấm công theo phòng ban: " + e.getMessage(), e);
        }
        return result;
    }

    public double getTongGioLamThemTrongThang(String nhanVienId, int thang, int nam) {
        String sql = "SELECT COALESCE(SUM(gioLamThem), 0) FROM CHAMCONG "
                + "WHERE maNV=? AND MONTH(ngay)=? AND YEAR(ngay)=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nhanVienId);
            ps.setInt(2, thang);
            ps.setInt(3, nam);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tính giờ làm thêm: " + e.getMessage(), e);
        }
        return 0;
    }

    private ChamCong mapChamCong(ResultSet rs) throws SQLException {
        ChamCong cc = new ChamCong();
        cc.setMaChamCong(rs.getInt("maChamCong"));
        cc.setMaNV(rs.getString("maNV"));
        Date ngay = rs.getDate("ngay");
        if (ngay != null) cc.setNgay(ngay.toLocalDate());
        cc.setMaCaLam(rs.getString("maCaLam"));
        // tenCaLam from JOIN
        try { cc.setTenCaLam(rs.getString("tenCaLam")); } catch (SQLException ignored) {}
        // employeeName from JOIN
        try { cc.setEmployeeName(rs.getString("hoTen")); } catch (SQLException ignored) {}
        Timestamp gioVao = rs.getTimestamp("gioVao");
        if (gioVao != null) cc.setGioVao(gioVao.toLocalDateTime());
        Timestamp gioRa = rs.getTimestamp("gioRa");
        if (gioRa != null) cc.setGioRa(gioRa.toLocalDateTime());
        cc.setSoGioLam(rs.getDouble("soGioLam"));
        cc.setGioLamThem(rs.getDouble("gioLamThem"));
        String tt = rs.getString("trangThai");
        if (tt != null) {
            try { cc.setTrangThai(ChamCong.TrangThai.fromDbValue(tt)); }
            catch (IllegalArgumentException ignored) {}
        }
        String pt = rs.getString("phuongThucChamCong");
        if (pt != null) {
            try { cc.setPhuongThucChamCong(ChamCong.PhuongThuc.fromDbValue(pt)); }
            catch (IllegalArgumentException ignored) {}
        }
        cc.setGhiChu(rs.getString("ghiChu"));
        return cc;
    }

    // =====================================================================
    // DANG_KY_LAM_THEMS
    // =====================================================================

    /**
     * INSERT a new DangKyLamThem. Returns generated id.
     */
    public int saveDangKyLamThem(DangKyLamThem dk) {
        String sql = "INSERT INTO DANGKY_LAMTHEM (maNV, ngay, soGio, heSoOT, lyDo, trangThai) VALUES (?,?,?,?,?,?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, dk.getMaNV());
            ps.setDate(2, Date.valueOf(dk.getNgay()));
            ps.setDouble(3, dk.getSoGio());
            ps.setDouble(4, dk.getHeSoOT() > 0 ? dk.getHeSoOT() : 1.5);
            ps.setString(5, dk.getLyDo());
            ps.setString(6, dk.getTrangThai() != null ? dk.getTrangThai().getDbValue() : "cho_duyet");
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    dk.setMaDK(id);
                    return id;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi thêm đăng ký làm thêm: " + e.getMessage(), e);
        }
        return 0;
    }

    public void updateTrangThai(int id, String trangThai, String nguoiDuyet, LocalDateTime ngayDuyet) {
        String sql = "UPDATE DANGKY_LAMTHEM SET trangThai=?, nguoiDuyet=?, ngayDuyet=? WHERE maDK=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, trangThai);
            if (nguoiDuyet != null && !nguoiDuyet.isEmpty()) ps.setString(2, nguoiDuyet); else ps.setNull(2, Types.VARCHAR);
            ps.setTimestamp(3, ngayDuyet != null ? Timestamp.valueOf(ngayDuyet) : null);
            ps.setInt(4, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi cập nhật trạng thái đơn OT: " + e.getMessage(), e);
        }
    }

    public List<DangKyLamThem> findByMaNV(String nhanVienId) {
        String sql = "SELECT d.maDK, d.maNV, d.ngay, d.soGio, d.heSoOT, d.lyDo, d.trangThai, d.nguoiDuyet, d.ngayDuyet, t.hoTen FROM DANGKY_LAMTHEM d "
                + "LEFT JOIN THONGTINCANHAN t ON d.maNV = t.maNV "
                + "WHERE d.maNV=? ORDER BY d.ngay DESC";
        List<DangKyLamThem> result = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nhanVienId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapDangKyLamThem(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tải đơn OT theo nhân viên: " + e.getMessage(), e);
        }
        return result;
    }

    /** Returns all pending OT requests with tenNV transient filled. */
    public List<DangKyLamThem> findChoDuyet() {
        String sql = "SELECT d.maDK, d.maNV, d.ngay, d.soGio, d.heSoOT, d.lyDo, d.trangThai, d.nguoiDuyet, d.ngayDuyet, t.hoTen FROM DANGKY_LAMTHEM d "
                + "LEFT JOIN THONGTINCANHAN t ON d.maNV = t.maNV "
                + "WHERE d.trangThai='cho_duyet' ORDER BY d.ngay DESC";
        List<DangKyLamThem> result = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(mapDangKyLamThem(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tải đơn OT chờ duyệt: " + e.getMessage(), e);
        }
        return result;
    }

    public DangKyLamThem findById(int id) {
        String sql = "SELECT d.maDK, d.maNV, d.ngay, d.soGio, d.heSoOT, d.lyDo, d.trangThai, d.nguoiDuyet, d.ngayDuyet, t.hoTen FROM DANGKY_LAMTHEM d "
                + "LEFT JOIN THONGTINCANHAN t ON d.maNV = t.maNV "
                + "WHERE d.maDK=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapDangKyLamThem(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tìm đơn OT: " + e.getMessage(), e);
        }
        return null;
    }

    /** Returns true if there is an approved OT request for this employee on this day. */
    public boolean hasDuyetForNVAndNgay(String nhanVienId, LocalDate ngay) {
        String sql = "SELECT COUNT(*) FROM DANGKY_LAMTHEM WHERE maNV=? AND ngay=? AND trangThai='da_duyet'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nhanVienId);
            ps.setDate(2, Date.valueOf(ngay));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi kiểm tra đơn OT: " + e.getMessage(), e);
        }
        return false;
    }

    /** Sum of approved OT hours for this employee in the given month/year. */
    public double getTongGioOTDaDuyetTrongThang(String nhanVienId, int thang, int nam) {
        String sql = "SELECT COALESCE(SUM(soGio), 0) FROM DANGKY_LAMTHEM "
                + "WHERE maNV=? AND MONTH(ngay)=? AND YEAR(ngay)=? AND trangThai='da_duyet'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nhanVienId);
            ps.setInt(2, thang);
            ps.setInt(3, nam);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tính OT đã duyệt: " + e.getMessage(), e);
        }
        return 0;
    }

    private DangKyLamThem mapDangKyLamThem(ResultSet rs) throws SQLException {
        DangKyLamThem dk = new DangKyLamThem();
        dk.setMaDK(rs.getInt("maDK"));
        dk.setMaNV(rs.getString("maNV"));
        Date ngay = rs.getDate("ngay");
        if (ngay != null) dk.setNgay(ngay.toLocalDate());
        dk.setSoGio(rs.getDouble("soGio"));
        try { double hs = rs.getDouble("heSoOT"); if (!rs.wasNull() && hs > 0) dk.setHeSoOT(hs); } catch (SQLException ignored) {}
        dk.setLyDo(rs.getString("lyDo"));
        String nd = rs.getString("nguoiDuyet");
        if (nd != null) dk.setNguoiDuyet(nd);
        Timestamp ngayDuyet = rs.getTimestamp("ngayDuyet");
        if (ngayDuyet != null) dk.setNgayDuyet(ngayDuyet.toLocalDateTime());
        String tt = rs.getString("trangThai");
        if (tt != null) {
            try { dk.setTrangThai(DangKyLamThem.TrangThai.fromDbValue(tt)); }
            catch (IllegalArgumentException ignored) {}
        }
        // transient
        try { dk.setEmployeeName(rs.getString("hoTen")); } catch (SQLException ignored) {}
        return dk;
    }

    // =====================================================================
    // Count working days in a month for a specific employee (not absent)
    // =====================================================================

    public int countNgayCong(String nhanVienId, int thang, int nam) {
        String sql = "SELECT COUNT(*) FROM CHAMCONG WHERE maNV=? AND MONTH(ngay)=? AND YEAR(ngay)=? "
                + "AND trangThai != 'vang_mat'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nhanVienId);
            ps.setInt(2, thang);
            ps.setInt(3, nam);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi đếm ngày công: " + e.getMessage(), e);
        }
        return 0;
    }

    // =====================================================================
    // DANG_KY_LAM_THEMS — findAll
    // =====================================================================

    /** Returns all OT requests (all statuses), newest first. */
    public List<DangKyLamThem> findAllDangKyLamThem() {
        String sql = "SELECT d.maDK, d.maNV, d.ngay, d.soGio, d.heSoOT, d.lyDo, d.trangThai, d.nguoiDuyet, d.ngayDuyet, t.hoTen FROM DANGKY_LAMTHEM d "
                + "LEFT JOIN THONGTINCANHAN t ON d.maNV = t.maNV "
                + "ORDER BY d.ngay DESC";
        List<DangKyLamThem> result = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(mapDangKyLamThem(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tải tất cả đơn OT: " + e.getMessage(), e);
        }
        return result;
    }

    public List<DangKyLamThem> findAllDangKyLamThemByScope(com.hrm.model.DataScope scope, String currentMaNV) {
        return getOTByScopeAndStatus(scope, currentMaNV, null);
    }

    public List<DangKyLamThem> findChoDuyetOTByScope(com.hrm.model.DataScope scope, String currentMaNV) {
        return getOTByScopeAndStatus(scope, currentMaNV, "cho_duyet");
    }

    private List<DangKyLamThem> getOTByScopeAndStatus(com.hrm.model.DataScope scope, String currentMaNV, String statusValue) {
        List<DangKyLamThem> result = new ArrayList<>();
        if (scope == com.hrm.model.DataScope.NONE) return result;

        if (scope == com.hrm.model.DataScope.DEPT) {
            return getOTByDeptSubtree(currentMaNV, statusValue);
        }

        String sqlBase = "SELECT d.maDK, d.maNV, d.ngay, d.soGio, d.heSoOT, d.lyDo, d.trangThai, d.nguoiDuyet, d.ngayDuyet, t.hoTen FROM DANGKY_LAMTHEM d "
                + "LEFT JOIN THONGTINCANHAN t ON d.maNV = t.maNV "
                + "LEFT JOIN BONHIEM b ON d.maNV = b.maNV AND b.trangThai = 'hieu_luc' AND b.loaiBoNhiem = 'chinh' ";

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

        String sql = sqlBase + sqlCondition + " ORDER BY d.ngay DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (scope != com.hrm.model.DataScope.ALL) {
                ps.setString(1, currentMaNV);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapDangKyLamThem(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tải đơn OT theo scope: " + e.getMessage(), e);
        }
        return result;
    }

    private List<DangKyLamThem> getOTByDeptSubtree(String currentMaNV, String statusValue) {
        List<DangKyLamThem> result = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection()) {
            java.util.Set<String> depts = getDeptSubtree(currentMaNV, conn);
            if (depts.isEmpty()) return result;
            String ph = String.join(",", java.util.Collections.nCopies(depts.size(), "?"));
            String statusClause = statusValue != null ? " AND d.trangThai = '" + statusValue + "'" : "";
            String sql = "SELECT d.maDK, d.maNV, d.ngay, d.soGio, d.heSoOT, d.lyDo, d.trangThai, d.nguoiDuyet, d.ngayDuyet, t.hoTen FROM DANGKY_LAMTHEM d "
                    + "LEFT JOIN THONGTINCANHAN t ON d.maNV = t.maNV "
                    + "LEFT JOIN BONHIEM b ON d.maNV = b.maNV AND b.trangThai = 'hieu_luc' AND b.loaiBoNhiem = 'chinh' "
                    + "WHERE b.maPhongBan IN (" + ph + ")" + statusClause + " ORDER BY d.ngay DESC";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                int i = 1; for (String d : depts) ps.setString(i++, d);
                try (ResultSet rs = ps.executeQuery()) { while (rs.next()) result.add(mapDangKyLamThem(rs)); }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tải đơn OT theo phòng ban: " + e.getMessage(), e);
        }
        return result;
    }

    /** Update heSoOT on a DangKyLamThem record. */
    public void updateHeSoOT(int id, double heSo) {
        String sql = "UPDATE DANGKY_LAMTHEM SET heSoOT=? WHERE maDK=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, heSo);
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi cập nhật hệ số OT: " + e.getMessage(), e);
        }
    }

    // =====================================================================
    // CAU_HINH_PHU_CAPS
    // =====================================================================

    public List<CauHinhPhuCap> findAllCauHinhPC() {
        String sql = "SELECT maCauHinh, loai, tenKhoan, kieuTinh, giaTri, nguon, hoatDong "
                + "FROM CAUHINH_PHUCAP ORDER BY loai, tenKhoan";
        List<CauHinhPhuCap> result = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(mapCauHinhPC(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tải cấu hình phụ cấp: " + e.getMessage(), e);
        }
        return result;
    }

    public CauHinhPhuCap findCauHinhPCById(int id) {
        String sql = "SELECT maCauHinh, loai, tenKhoan, kieuTinh, giaTri, nguon, hoatDong "
                + "FROM CAUHINH_PHUCAP WHERE maCauHinh=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapCauHinhPC(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tìm cấu hình phụ cấp: " + e.getMessage(), e);
        }
        return null;
    }

    public int insertCauHinhPC(CauHinhPhuCap pc) {
        String sql = "INSERT INTO CAUHINH_PHUCAP (loai, tenKhoan, kieuTinh, giaTri, nguon, hoatDong) "
                + "VALUES (?,?,?,?,?,1)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, pc.getLoai() == ThanhPhanLuong.Loai.PHU_CAP ? "phu_cap" : "khau_tru");
            ps.setString(2, pc.getTenKhoan());
            ps.setString(3, pc.getKieuTinh() == CauHinhPhuCap.KieuTinh.CO_DINH ? "co_dinh" : "phan_tram");
            ps.setDouble(4, pc.getGiaTri());
            ps.setString(5, pc.getNguon());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    pc.setMaPC(id);
                    return id;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi thêm cấu hình phụ cấp: " + e.getMessage(), e);
        }
        return 0;
    }

    public void updateCauHinhPC(CauHinhPhuCap pc) {
        String sql = "UPDATE CAUHINH_PHUCAP SET loai=?, tenKhoan=?, kieuTinh=?, giaTri=?, nguon=? WHERE maCauHinh=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, pc.getLoai() == ThanhPhanLuong.Loai.PHU_CAP ? "phu_cap" : "khau_tru");
            ps.setString(2, pc.getTenKhoan());
            ps.setString(3, pc.getKieuTinh() == CauHinhPhuCap.KieuTinh.CO_DINH ? "co_dinh" : "phan_tram");
            ps.setDouble(4, pc.getGiaTri());
            ps.setString(5, pc.getNguon());
            ps.setInt(6, pc.getMaPC());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi cập nhật cấu hình phụ cấp: " + e.getMessage(), e);
        }
    }

    public void deactivateCauHinhPC(int id) {
        String sql = "UPDATE CAUHINH_PHUCAP SET hoatDong=0 WHERE maCauHinh=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi ngừng cấu hình phụ cấp: " + e.getMessage(), e);
        }
    }

    private CauHinhPhuCap mapCauHinhPC(ResultSet rs) throws SQLException {
        CauHinhPhuCap pc = new CauHinhPhuCap();
        pc.setMaPC(rs.getInt("maCauHinh"));
        String loaiStr = rs.getString("loai");
        pc.setLoai("phu_cap".equals(loaiStr) ? ThanhPhanLuong.Loai.PHU_CAP : ThanhPhanLuong.Loai.KHAU_TRU);
        pc.setTenKhoan(rs.getString("tenKhoan"));
        String kieuStr = rs.getString("kieuTinh");
        pc.setKieuTinh("co_dinh".equals(kieuStr) ? CauHinhPhuCap.KieuTinh.CO_DINH : CauHinhPhuCap.KieuTinh.PHAN_TRAM);
        pc.setGiaTri(rs.getDouble("giaTri"));
        pc.setNguon(rs.getString("nguon"));
        pc.setHoatDong(rs.getBoolean("hoatDong"));
        return pc;
    }

    // =====================================================================
    // UTILITY — NhanVienInfo, mã hiển thị, tài khoản → nhân viên
    // =====================================================================

    /** Thông tin nhân viên dùng để hiển thị trong panel chấm công. */
    public static class NhanVienInfo {
        public final String maNV;        // VD: "NV001"
        public final String maNhanVien;  // alias = maNV
        public final String hoTen;
        public final String email;
        public final String tenChucVu;
        public final String tenPhongBan;
        public final String trangThai;

        public NhanVienInfo(String maNV, String hoTen,
                            String email, String tenChucVu, String tenPhongBan, String trangThai) {
            this.maNV        = maNV        != null ? maNV        : "";
            this.maNhanVien  = this.maNV;
            this.hoTen       = hoTen       != null ? hoTen       : "";
            this.email       = email       != null ? email       : "";
            this.tenChucVu   = tenChucVu   != null ? tenChucVu   : "";
            this.tenPhongBan = tenPhongBan != null ? tenPhongBan : "";
            this.trangThai   = trangThai   != null ? trangThai   : "";
        }
    }

    /** Trả về maNV (VD: "NV001") — sau refactor maNV đã là VARCHAR PK. */
    public String getMaNhanVienById(String maNV) {
        return maNV != null ? maNV : "";
    }

    /** Tìm NhanVienInfo theo maNV (VD: "NV001"). */
    public NhanVienInfo findNhanVienByMa(String maNV) {
        String sql = "SELECT n.maNV, t.hoTen, t.email, "
                + "cv.tenChucVu, pb.tenPhongBan, n.trangThai "
                + "FROM NHANVIEN n "
                + "LEFT JOIN THONGTINCANHAN t ON n.maNV = t.maNV "
                + "LEFT JOIN BONHIEM b ON n.maNV = b.maNV "
                + "    AND b.trangThai = 'hieu_luc' AND b.loaiBoNhiem = 'chinh' "
                + "LEFT JOIN CHUCVU cv ON b.maChucVu = cv.maChucVu "
                + "LEFT JOIN PHONGBAN pb ON b.maPhongBan = pb.maPhongBan "
                + "WHERE n.maNV = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maNV);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new NhanVienInfo(
                        rs.getString("maNV"),
                        rs.getString("hoTen"),
                        rs.getString("email"),
                        rs.getString("tenChucVu"),
                        rs.getString("tenPhongBan"),
                        rs.getString("trangThai")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("[DB] findNhanVienByMa: " + e.getMessage());
        }
        return null;
    }

    /** Lấy maNV (VARCHAR PK) từ maTaiKhoan (int PK của TAIKHOAN). */
    public String getMaNVByTaiKhoan(int maTaiKhoan) {
        String sql = "SELECT maNV FROM TAIKHOAN WHERE maTaiKhoan=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maTaiKhoan);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString(1);
            }
        } catch (SQLException e) {
            System.err.println("[DB] getMaNVByTaiKhoan: " + e.getMessage());
        }
        return null;
    }

    /** Alias cho findAllDangKyLamThem() — tương thích với chamcongnew. */
    public List<com.hrm.model.DangKyLamThem> findAllDonOT() {
        return findAllDangKyLamThem();
    }

    /** Kiểm tra nhân viên có đơn OT đã duyệt cho ngày chỉ định không. */
    public boolean coOTDaDuyetTheoNgay(String maNV, java.time.LocalDate ngay) {
        String sql = "SELECT COUNT(*) FROM DANGKY_LAMTHEM WHERE maNV=? AND ngay=? AND trangThai='da_duyet'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maNV);
            ps.setDate(2, java.sql.Date.valueOf(ngay));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("[DB] coOTDaDuyetTheoNgay: " + e.getMessage());
        }
        return false;
    }

    /** Xóa đơn OT (chỉ xóa nếu đang ở trạng thái chờ duyệt). */
    public void deleteDangKyLamThem(int maDK) {
        String sql = "DELETE FROM DANGKY_LAMTHEM WHERE maDK=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maDK);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[DB] deleteDangKyLamThem: " + e.getMessage());
        }
    }

    /** Alias cho findActiveCaLam() — tương thích với chamcongnew. */
    public List<com.hrm.model.CaLam> findCaLamHoatDong() {
        return findActiveCaLam();
    }
}
