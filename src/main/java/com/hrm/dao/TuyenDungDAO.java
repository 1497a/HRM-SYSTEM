package com.hrm.dao;

import com.hrm.model.TinTuyenDung;
import com.hrm.model.UngVien;
import com.hrm.model.YeuCauTuyenDung;
import com.hrm.model.DataScope;
import com.hrm.model.RecruitmentStatus;
import com.hrm.util.DaoHelper;
import com.hrm.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Repository JDBC cho module Tuyển dụng.
 * Singleton pattern.
 */
public class TuyenDungDAO {

    private static final String YEUCAU_BASE_SELECT = "SELECT yc.*, pb.tenPhongBan, cv.tenChucVu "
            + "FROM YEUCAUTUYENDUNG yc "
            + "LEFT JOIN PHONGBAN pb ON yc.maPhongBan = pb.maPhongBan "
            + "LEFT JOIN CHUCVU cv ON yc.maChucVu = cv.maChucVu ";
    private static final String TIN_BASE_SELECT = "SELECT t.*, pb.tenPhongBan, cv.tenChucVu, yc.soLuong, "
            + "(SELECT COUNT(*) FROM UNGVIEN uv WHERE uv.maTin = t.maTin) AS soUngVien "
            + "FROM TINTUYENDUNG t "
            + "JOIN YEUCAUTUYENDUNG yc ON t.maYeuCau = yc.maYeuCau "
            + "LEFT JOIN PHONGBAN pb ON yc.maPhongBan = pb.maPhongBan "
            + "LEFT JOIN CHUCVU cv ON yc.maChucVu = cv.maChucVu ";
    private static final String UV_BASE_SELECT = "SELECT uv.*, t.tieuDe AS tenTin, cv.tenChucVu, pb.tenPhongBan "
            + "FROM UNGVIEN uv "
            + "LEFT JOIN TINTUYENDUNG t ON uv.maTin = t.maTin "
            + "LEFT JOIN YEUCAUTUYENDUNG yc ON t.maYeuCau = yc.maYeuCau "
            + "LEFT JOIN CHUCVU cv ON yc.maChucVu = cv.maChucVu "
            + "LEFT JOIN PHONGBAN pb ON yc.maPhongBan = pb.maPhongBan ";
    private static TuyenDungDAO instance;
    private TuyenDungDAO() {
    }

    public static synchronized TuyenDungDAO getInstance() {
        if (instance == null) {
            instance = new TuyenDungDAO();
        }
        return instance;
    }

    // =====================================================================
    // ==================== YeuCauTuyenDung ================================
    // =====================================================================
    private YeuCauTuyenDung mapYeuCau(ResultSet rs) throws SQLException {
        YeuCauTuyenDung yc = new YeuCauTuyenDung();
        yc.setMaYeuCau(rs.getInt("maYeuCau"));
        yc.setId(rs.getString("maPhongBan"));
        yc.setMaChucVu(rs.getString("maChucVu"));
        yc.setSoLuong(rs.getInt("soLuong"));
        yc.setLyDo(rs.getString("lyDo"));
        yc.setMucLuongDuKien(rs.getString("mucLuongDuKien"));
        yc.setYeuCauKinhNghiem(rs.getString("yeuCauKinhNghiem"));
        yc.setYeuCauHocVan(rs.getString("yeuCauHocVan"));
        yc.setYeuCauKhac(rs.getString("yeuCauKhac"));
        Date han = rs.getDate("hanTuyenDung");
        if (han != null) yc.setHanTuyenDung(han.toLocalDate());
        int nguoiDuyet = rs.getInt("nguoiDuyet");
        yc.setNguoiDuyet(rs.wasNull() ? 0 : nguoiDuyet);
        Timestamp ngayDuyet = rs.getTimestamp("ngayDuyet");
        if (ngayDuyet != null) yc.setNgayDuyet(ngayDuyet.toLocalDateTime());
        yc.setTrangThai(rs.getString("trangThai"));
        return yc;
    }

    private void applyYeuCauDetails(YeuCauTuyenDung yc, ResultSet rs) throws SQLException {
        yc.setTenPhongBan(rs.getString("tenPhongBan"));
        yc.setTenChucVu(rs.getString("tenChucVu"));
    }

    private YeuCauTuyenDung mapYeuCauWithDetails(ResultSet rs) throws SQLException {
        YeuCauTuyenDung yc = mapYeuCau(rs);
        applyYeuCauDetails(yc, rs);
        return yc;
    }

    /**
     * Chèn yêu cầu tuyển dụng mới, trả về ID được sinh ra.
     */
    public int insertYeuCau(YeuCauTuyenDung yc) {
        String sql = "INSERT INTO YEUCAUTUYENDUNG (maPhongBan, maChucVu, soLuong, lyDo, "
                + "mucLuongDuKien, yeuCauKinhNghiem, yeuCauHocVan, yeuCauKhac, "
                + "hanTuyenDung, trangThai) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 'cho_duyet')";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, yc.getId());
            ps.setString(2, yc.getMaChucVu());
            ps.setInt(3, yc.getSoLuong());
            ps.setString(4, yc.getLyDo());
            ps.setString(5, yc.getMucLuongDuKien());
            ps.setString(6, yc.getYeuCauKinhNghiem());
            ps.setString(7, yc.getYeuCauHocVan());
            ps.setString(8, yc.getYeuCauKhac());
            ps.setDate(9, yc.getHanTuyenDung() != null ? Date.valueOf(yc.getHanTuyenDung()) : null);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi insertYeuCau: " + e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Cập nhật trạng thái yêu cầu tuyển dụng.
     */
    public void updateYeuCauTrangThai(int maYeuCau, String trangThai, int nguoiDuyet, LocalDateTime ngayDuyet) {
        String sql = "UPDATE YEUCAUTUYENDUNG SET trangThai = ?, nguoiDuyet = ?, ngayDuyet = ? WHERE maYeuCau = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, trangThai);
            if (nguoiDuyet == 0) {
                ps.setNull(2, Types.INTEGER);
            } else {
                ps.setInt(2, nguoiDuyet);
            }
            ps.setTimestamp(3, ngayDuyet != null ? Timestamp.valueOf(ngayDuyet) : null);
            ps.setInt(4, maYeuCau);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Lỗi updateYeuCauTrangThai: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Lấy tất cả yêu cầu tuyển dụng với tên phòng ban, chức vụ.
     */
    public List<YeuCauTuyenDung> findAllYeuCau() {
        List<YeuCauTuyenDung> list = new ArrayList<>();
        String sql = YEUCAU_BASE_SELECT + "ORDER BY yc.maYeuCau DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapYeuCauWithDetails(rs));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi findAllYeuCau: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    public List<YeuCauTuyenDung> findAllYeuCauByScope(DataScope scope, String currentMaNV) {
        return queryYeuCauByScope(scope, currentMaNV, null);
    }

    /**
     * Tìm yêu cầu tuyển dụng theo ID với transients.
     */
    public YeuCauTuyenDung findYeuCauById(int maYeuCau) {
        String sql = YEUCAU_BASE_SELECT + "WHERE yc.maYeuCau = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maYeuCau);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapYeuCauWithDetails(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi findYeuCauById: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    // =====================================================================
    // ==================== TinTuyenDung ===================================
    // =====================================================================
    private TinTuyenDung mapTin(ResultSet rs) throws SQLException {
        TinTuyenDung tin = new TinTuyenDung();
        tin.setMaTin(rs.getInt("maTin"));
        tin.setMaYeuCau(rs.getInt("maYeuCau"));
        tin.setTieuDe(rs.getString("tieuDe"));
        tin.setNoiDung(rs.getString("noiDung"));
        tin.setMucLuong(rs.getString("mucLuong"));
        tin.setDiaDiem(rs.getString("diaDiem"));
        Date han = rs.getDate("hanNopHoSo");
        if (han != null) tin.setHanNopHoSo(han.toLocalDate());
        tin.setTrangThai(rs.getString("trangThai"));
        Date ngayTao = rs.getDate("ngayTao");
        if (ngayTao != null) tin.setNgayTao(ngayTao.toLocalDate());
        return tin;
    }

    private void applyTinDetails(TinTuyenDung tin, ResultSet rs) throws SQLException {
        tin.setTenPhongBan(rs.getString("tenPhongBan"));
        tin.setTenChucVu(rs.getString("tenChucVu"));
        tin.setSoUngVien(rs.getInt("soUngVien"));
        tin.setSoLuongCanTuyen(rs.getInt("soLuong"));
    }

    private TinTuyenDung mapTinWithDetails(ResultSet rs) throws SQLException {
        TinTuyenDung tin = mapTin(rs);
        applyTinDetails(tin, rs);
        return tin;
    }

    /**
     * Chèn tin tuyển dụng mới, trả về ID được sinh ra.
     */
    public int insertTin(TinTuyenDung tin) {
        String sql = "INSERT INTO TINTUYENDUNG (maYeuCau, tieuDe, noiDung, mucLuong, diaDiem, "
                + "hanNopHoSo, trangThai, ngayTao, ngayCapNhat) "
                + "VALUES (?, ?, ?, ?, ?, ?, 'dang_tuyen', CURDATE(), CURDATE())";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, tin.getMaYeuCau());
            ps.setString(2, tin.getTieuDe());
            ps.setString(3, tin.getNoiDung());
            ps.setString(4, tin.getMucLuong());
            ps.setString(5, tin.getDiaDiem());
            ps.setDate(6, tin.getHanNopHoSo() != null ? Date.valueOf(tin.getHanNopHoSo()) : null);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi insertTin: " + e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Cập nhật tin tuyển dụng.
     */
    public void updateTin(TinTuyenDung tin) {
        String sql = "UPDATE TINTUYENDUNG SET tieuDe=?, noiDung=?, mucLuong=?, diaDiem=?, "
                + "hanNopHoSo=?, trangThai=?, ngayCapNhat=CURDATE() WHERE maTin=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tin.getTieuDe());
            ps.setString(2, tin.getNoiDung());
            ps.setString(3, tin.getMucLuong());
            ps.setString(4, tin.getDiaDiem());
            ps.setDate(5, tin.getHanNopHoSo() != null ? Date.valueOf(tin.getHanNopHoSo()) : null);
            ps.setString(6, tin.getTrangThai());
            ps.setInt(7, tin.getMaTin());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Lỗi updateTin: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Lấy tất cả tin tuyển dụng với transients + soUngVien.
     */
    public List<TinTuyenDung> findAllTin() {
        List<TinTuyenDung> list = new ArrayList<>();
        String sql = TIN_BASE_SELECT + "ORDER BY t.maTin DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapTinWithDetails(rs));
            }
        } catch (SQLException e) {
            System.err.println("Loi findAllTin: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    public List<TinTuyenDung> findAllTinByScope(DataScope scope, String currentMaNV) {
        return queryTinByScope(scope, currentMaNV, null);
    }

    /**
     * Tim tin tuyen dung theo ID voi transients.
     */
    public TinTuyenDung findTinById(int maTin) {
        String sql = TIN_BASE_SELECT + "WHERE t.maTin = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maTin);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapTinWithDetails(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Loi findTinById: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public TinTuyenDung findTinByIdInScope(int maTin, DataScope scope, String currentMaNV) {
        List<TinTuyenDung> list = queryTinByScope(scope, currentMaNV, maTin);
        return list.isEmpty() ? null : list.get(0);
    }

    // =====================================================================
    // ==================== UngVien ========================================
    // =====================================================================
    private UngVien mapUngVien(ResultSet rs) throws SQLException {
        UngVien uv = new UngVien();
        uv.setMaUngVien(rs.getInt("maUngVien"));
        uv.setMaTin(rs.getInt("maTin"));
        uv.setHoTen(rs.getString("hoTen"));
        uv.setEmail(rs.getString("email"));
        uv.setDienThoai(rs.getString("dienThoai"));
        Date ngaySinh = rs.getDate("ngaySinh");
        if (ngaySinh != null) uv.setNgaySinh(ngaySinh.toLocalDate());
        uv.setGioiTinh(rs.getString("gioiTinh"));
        uv.setDiaChi(rs.getString("diaChi"));
        uv.setTrinhDoHocVan(rs.getString("trinhDoHocVan"));
        uv.setKinhNghiem(rs.getString("kinhNghiem"));
            uv.setFileCv(rs.getString("fileCv"));
        uv.setNguonUngTuyen(rs.getString("nguonUngTuyen"));
        uv.setTrangThai(rs.getString("trangThai"));
        uv.setNhanXet(rs.getString("nhanXet"));
        uv.setMaNV(rs.getString("maNV"));
        Date ngayTao = rs.getDate("ngayTao");
        if (ngayTao != null) uv.setNgayTao(ngayTao.toLocalDate());
        return uv;
    }

    private void applyUngVienDetails(UngVien uv, ResultSet rs) throws SQLException {
        uv.setTenTin(rs.getString("tenTin"));
        uv.setTenChucVu(rs.getString("tenChucVu"));
        uv.setTenPhongBan(rs.getString("tenPhongBan"));
    }

    private UngVien mapUngVienWithDetails(ResultSet rs) throws SQLException {
        UngVien uv = mapUngVien(rs);
        applyUngVienDetails(uv, rs);
        return uv;
    }

    /**
     * Chèn ứng viên mới, trả về ID được sinh ra.
     */
    public int insertUngVien(UngVien uv) {
        String sql = "INSERT INTO UNGVIEN (maTin, hoTen, email, dienThoai, ngaySinh, gioiTinh, "
                + "diaChi, trinhDoHocVan, kinhNghiem, fileCv, nguonUngTuyen, trangThai, nhanXet, ngayTao) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURDATE())";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, uv.getMaTin());
            ps.setString(2, uv.getHoTen());
            ps.setString(3, uv.getEmail());
            ps.setString(4, uv.getDienThoai());
            ps.setDate(5, uv.getNgaySinh() != null ? Date.valueOf(uv.getNgaySinh()) : null);
            ps.setString(6, uv.getGioiTinh());
            ps.setString(7, uv.getDiaChi());
            ps.setString(8, uv.getTrinhDoHocVan());
            ps.setString(9, uv.getKinhNghiem());
            ps.setString(10, uv.getFileCv());
            ps.setString(11, uv.getNguonUngTuyen());
            ps.setString(12, uv.getTrangThai() != null ? uv.getTrangThai() : "moi");
            ps.setString(13, uv.getNhanXet());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi insertUngVien: " + e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Cập nhật thông tin ứng viên.
     */
    public void updateUngVien(UngVien uv) {
        String sql = "UPDATE UNGVIEN SET trangThai=?, nhanXet=?, maNV=? WHERE maUngVien=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, uv.getTrangThai());
            ps.setString(2, uv.getNhanXet());
            if (uv.getMaNV() == null || uv.getMaNV().isEmpty()) {
                ps.setNull(3, Types.VARCHAR);
            } else {
                ps.setString(3, uv.getMaNV());
            }
            ps.setInt(4, uv.getMaUngVien());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Lỗi updateUngVien: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Lấy tất cả ứng viên với tenTin transient.
     */
    public List<UngVien> findAllUngVien() {
        List<UngVien> list = new ArrayList<>();
        String sql = UV_BASE_SELECT + "ORDER BY uv.maUngVien DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapUngVienWithDetails(rs));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi findAllUngVien: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    public List<UngVien> findAllUngVienByScope(DataScope scope, String currentMaNV) {
        return queryUngVienByScope(scope, currentMaNV, null);
    }

    /**
     * Lấy ứng viên theo mã tin tuyển dụng.
     */
    public List<UngVien> findByMaTin(int maTin) {
        List<UngVien> list = new ArrayList<>();
        String sql = UV_BASE_SELECT + "WHERE uv.maTin = ? ORDER BY uv.maUngVien DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maTin);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapUngVienWithDetails(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi findByMaTin: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Tìm ứng viên theo ID.
     */
    public UngVien findById(int maUngVien) {
        String sql = UV_BASE_SELECT + "WHERE uv.maUngVien = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maUngVien);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapUngVienWithDetails(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi findById (UngVien): " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public UngVien findByIdInScope(int maUngVien, DataScope scope, String currentMaNV) {
        List<UngVien> list = queryUngVienByScope(scope, currentMaNV, maUngVien);
        return list.isEmpty() ? null : list.get(0);
    }

    private List<YeuCauTuyenDung> queryYeuCauByScope(DataScope scope, String currentMaNV, Integer maYeuCau) {
        List<YeuCauTuyenDung> list = new ArrayList<>();
        String orderBy = maYeuCau == null ? " ORDER BY yc.maYeuCau DESC" : "";
        try (Connection conn = DatabaseConnection.getConnection()) {
            ScopeQuery scopeQuery = buildScopeQuery(scope, currentMaNV, conn, "yc.maPhongBan");
            if (scopeQuery == null) return list;
            StringBuilder sql = new StringBuilder(YEUCAU_BASE_SELECT).append(scopeQuery.whereClause);
            if (maYeuCau != null) {
                sql.append(scopeQuery.hasWhere ? " AND " : "WHERE ").append("yc.maYeuCau = ?");
            }
            sql.append(orderBy);
            try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
                int index = bindParams(ps, scopeQuery.params);
                if (maYeuCau != null) ps.setInt(index, maYeuCau);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) list.add(mapYeuCauWithDetails(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Loi queryYeuCauByScope: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    private List<TinTuyenDung> queryTinByScope(DataScope scope, String currentMaNV, Integer maTin) {
        List<TinTuyenDung> list = new ArrayList<>();
        String orderBy = maTin == null ? " ORDER BY t.maTin DESC" : "";
        try (Connection conn = DatabaseConnection.getConnection()) {
            ScopeQuery scopeQuery = buildScopeQuery(scope, currentMaNV, conn, "yc.maPhongBan");
            if (scopeQuery == null) return list;
            StringBuilder sql = new StringBuilder(TIN_BASE_SELECT).append(scopeQuery.whereClause);
            if (maTin != null) {
                sql.append(scopeQuery.hasWhere ? " AND " : "WHERE ").append("t.maTin = ?");
            }
            sql.append(orderBy);
            try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
                int index = bindParams(ps, scopeQuery.params);
                if (maTin != null) ps.setInt(index, maTin);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) list.add(mapTinWithDetails(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Loi queryTinByScope: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    private List<UngVien> queryUngVienByScope(DataScope scope, String currentMaNV, Integer maUngVien) {
        List<UngVien> list = new ArrayList<>();
        String orderBy = maUngVien == null ? " ORDER BY uv.maUngVien DESC" : "";
        try (Connection conn = DatabaseConnection.getConnection()) {
            ScopeQuery scopeQuery = buildScopeQuery(scope, currentMaNV, conn, "yc.maPhongBan");
            if (scopeQuery == null) return list;
            StringBuilder sql = new StringBuilder(UV_BASE_SELECT).append(scopeQuery.whereClause);
            if (maUngVien != null) {
                sql.append(scopeQuery.hasWhere ? " AND " : "WHERE ").append("uv.maUngVien = ?");
            }
            sql.append(orderBy);
            try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
                int index = bindParams(ps, scopeQuery.params);
                if (maUngVien != null) ps.setInt(index, maUngVien);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) list.add(mapUngVienWithDetails(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Loi queryUngVienByScope: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    private ScopeQuery buildScopeQuery(DataScope scope, String currentMaNV, Connection conn, String deptColumn) throws SQLException {
        if (scope == null || scope == DataScope.NONE) return null;
        if (scope == DataScope.ALL) {
            return new ScopeQuery("", Collections.emptyList(), false);
        }
        if (scope == DataScope.DEPT) {
            Set<String> deptIds = DaoHelper.getDeptSubtree(currentMaNV, conn);
            if (deptIds.isEmpty()) return null;
            String placeholders = String.join(",", Collections.nCopies(deptIds.size(), "?"));
            return new ScopeQuery(
                    "WHERE " + deptColumn + " IN (" + placeholders + ")",
                    new ArrayList<>(deptIds),
                    true
            );
        }
        return null;
    }

    private int bindParams(PreparedStatement ps, List<String> params) throws SQLException {
        int index = 1;
        for (String param : params) {
            ps.setString(index++, param);
        }
        return index;
    }

    private static class ScopeQuery {
        private final String whereClause;
        private final List<String> params;
        private final boolean hasWhere;

        private ScopeQuery(String whereClause, List<String> params, boolean hasWhere) {
            this.whereClause = whereClause;
            this.params = params;
            this.hasWhere = hasWhere;
        }
    }

    /**
     * Đếm số ứng viên đã được chuyển thành nhân viên từ một yêu cầu tuyển dụng.
     */
    public int countConvertedByYeuCau(int maYeuCau) {
        String sql = "SELECT COUNT(*) FROM UNGVIEN uv "
                + "JOIN TINTUYENDUNG t ON uv.maTin = t.maTin "
                + "WHERE t.maYeuCau = ? AND uv.trangThai = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maYeuCau);
            ps.setString(2, RecruitmentStatus.UngVien.DA_CHUYEN_NHAN_VIEN);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Loi countConvertedByYeuCau: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Dem so ung vien co trang thai "trung_tuyen" cho mot yeu cau tuyen dung.
     * Dung de kiem tra han muc truoc khi danh dau them ung vien trung tuyen.
     */
    public int countTrungTuyenByYeuCau(int maYeuCau) {
        String sql = "SELECT COUNT(*) FROM UNGVIEN uv "
                + "JOIN TINTUYENDUNG t ON uv.maTin = t.maTin "
                + "WHERE t.maYeuCau = ? AND uv.trangThai IN (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maYeuCau);
            ps.setString(2, RecruitmentStatus.UngVien.TRUNG_TUYEN);
            ps.setString(3, RecruitmentStatus.UngVien.DA_CHUYEN_NHAN_VIEN);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Loi countTrungTuyenByYeuCau: " + e.getMessage());
        }
        return 0;
    }

}
