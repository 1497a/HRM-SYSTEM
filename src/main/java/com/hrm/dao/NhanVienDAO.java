package com.hrm.dao;

import com.hrm.model.NhanVien;
import com.hrm.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository cho bảng NHANVIEN.
 * Singleton pattern - sử dụng MySQL JDBC.
 * Mã nhân viên (maNV) là String (ví dụ: NV001, NV002, ...).
 */
public class NhanVienDAO {

    private static NhanVienDAO instance;

    private NhanVienDAO() {
    }

    public static synchronized NhanVienDAO getInstance() {
        if (instance == null) {
            instance = new NhanVienDAO();
        }
        return instance;
    }

    // ============================
    // Mapping helper
    // ============================

    private NhanVien mapRow(ResultSet rs) throws SQLException {
        NhanVien nv = new NhanVien();
        nv.setMaNhanVien(rs.getString("maNV"));
        nv.setLoaiHopDong(rs.getString("loaiHopDong"));
        Date ngayVaoLam = rs.getDate("ngayVaoLam");
        if (ngayVaoLam != null) {
            nv.setNgayVaoLam(ngayVaoLam.toLocalDate());
        }
        nv.setTrangThai(rs.getString("trangThai"));
        nv.setGhiChu(rs.getString("ghiChu"));
        return nv;
    }

    // ============================
    // findByMaNhanVien (chính thức, dùng String)
    // ============================

    public NhanVien findByMaNhanVien(String maNhanVien) {
        if (maNhanVien == null || maNhanVien.trim().isEmpty()) {
            return null;
        }

        String sql = "SELECT n.*, t.hoTen, pb.maPhongBan AS maPhongBanHT, "
                + "pb.tenPhongBan, cv.tenChucVu "
                + "FROM NHANVIEN n "
                + "LEFT JOIN THONGTINCANHAN t ON n.maNV = t.maNV "
                + "LEFT JOIN BONHIEM b ON n.maNV = b.maNV "
                + "  AND b.trangThai = 'hieu_luc' AND b.loaiBoNhiem = 'chinh' "
                + "LEFT JOIN PHONGBAN pb ON b.maPhongBan = pb.maPhongBan "
                + "LEFT JOIN CHUCVU cv ON b.maChucVu = cv.maChucVu "
                + "WHERE n.maNV = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maNhanVien);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    NhanVien nv = mapRow(rs);
                    nv.setHoTen(rs.getString("hoTen"));
                    nv.setMaPhongBanHienTai(rs.getString("maPhongBanHT"));
                    nv.setTenPhongBanHienTai(rs.getString("tenPhongBan"));
                    nv.setTenChucVuHienTai(rs.getString("tenChucVu"));
                    return nv;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tìm nhân viên theo maNV: " + maNhanVien + " - " + e.getMessage(), e);
        }
        return null;
    }

    // ============================
    // findAll - with hoTen + phongban/chucvu current
    // ============================

    public List<NhanVien> findAll() {
        String sql = "SELECT n.*, t.hoTen, pb.maPhongBan AS maPhongBanHT, "
                + "pb.tenPhongBan, cv.tenChucVu "
                + "FROM NHANVIEN n "
                + "LEFT JOIN THONGTINCANHAN t ON n.maNV = t.maNV "
                + "LEFT JOIN BONHIEM b ON n.maNV = b.maNV "
                + "  AND b.trangThai = 'hieu_luc' AND b.loaiBoNhiem = 'chinh' "
                + "LEFT JOIN PHONGBAN pb ON b.maPhongBan = pb.maPhongBan "
                + "LEFT JOIN CHUCVU cv ON b.maChucVu = cv.maChucVu "
                + "WHERE n.maNV != 'admin' "
                + "ORDER BY n.maNV";

        List<NhanVien> result = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                NhanVien nv = mapRow(rs);
                nv.setHoTen(rs.getString("hoTen"));
                nv.setMaPhongBanHienTai(rs.getString("maPhongBanHT"));
                nv.setTenPhongBanHienTai(rs.getString("tenPhongBan"));
                nv.setTenChucVuHienTai(rs.getString("tenChucVu"));
                result.add(nv);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tải danh sách nhân viên: " + e.getMessage(), e);
        }
        return result;
    }

    // ============================
    // findAllByScope 
    // ============================

    public List<NhanVien> findAllByScope(com.hrm.model.DataScope scope, String currentMaNV) {
        List<NhanVien> result = new ArrayList<>();
        if (scope == com.hrm.model.DataScope.NONE) return result;

        String sqlBase = "SELECT n.*, t.hoTen, pb.maPhongBan AS maPhongBanHT, "
                + "pb.tenPhongBan, cv.tenChucVu "
                + "FROM NHANVIEN n "
                + "LEFT JOIN THONGTINCANHAN t ON n.maNV = t.maNV "
                + "LEFT JOIN BONHIEM b ON n.maNV = b.maNV AND b.trangThai = 'hieu_luc' AND b.loaiBoNhiem = 'chinh' "
                + "LEFT JOIN PHONGBAN pb ON b.maPhongBan = pb.maPhongBan "
                + "LEFT JOIN CHUCVU cv ON b.maChucVu = cv.maChucVu ";

        String sqlCondition = "";
        switch (scope) {
            case ALL:
                sqlCondition = " WHERE n.maNV != 'admin' ORDER BY n.maNV";
                break;
            case DEPT:
                sqlCondition = " WHERE b.maPhongBan = (SELECT b2.maPhongBan FROM BONHIEM b2 WHERE b2.maNV = ? AND b2.trangThai = 'hieu_luc' AND b2.loaiBoNhiem = 'chinh') AND n.maNV != 'admin' ORDER BY n.maNV";
                break;
            case TEAM:
                sqlCondition = " WHERE (b.maQuanLy = ? OR n.maNV = ?) AND n.maNV != 'admin' ORDER BY n.maNV";
                break;
            case SELF:
                sqlCondition = " WHERE n.maNV = ? AND n.maNV != 'admin' ORDER BY n.maNV";
                break;
            default:
                return result;
        }

        String finalSql = sqlBase + sqlCondition;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(finalSql)) {

            if (scope == com.hrm.model.DataScope.TEAM) {
                ps.setString(1, currentMaNV);
                ps.setString(2, currentMaNV);
            } else if (scope != com.hrm.model.DataScope.ALL) {
                ps.setString(1, currentMaNV);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    NhanVien nv = mapRow(rs);
                    nv.setHoTen(rs.getString("hoTen"));
                    nv.setMaPhongBanHienTai(rs.getString("maPhongBanHT"));
                    nv.setTenPhongBanHienTai(rs.getString("tenPhongBan"));
                    nv.setTenChucVuHienTai(rs.getString("tenChucVu"));
                    result.add(nv);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tải danh sách nhân viên theo scope: " + e.getMessage(), e);
        }
        return result;
    }

    // ============================
    // findByTrangThai
    // ============================

    public List<NhanVien> findByTrangThai(String trangThai) {
        String sql = "SELECT n.*, t.hoTen, pb.maPhongBan AS maPhongBanHT, "
                + "pb.tenPhongBan, cv.tenChucVu "
                + "FROM NHANVIEN n "
                + "LEFT JOIN THONGTINCANHAN t ON n.maNV = t.maNV "
                + "LEFT JOIN BONHIEM b ON n.maNV = b.maNV "
                + "  AND b.trangThai = 'hieu_luc' AND b.loaiBoNhiem = 'chinh' "
                + "LEFT JOIN PHONGBAN pb ON b.maPhongBan = pb.maPhongBan "
                + "LEFT JOIN CHUCVU cv ON b.maChucVu = cv.maChucVu "
                + "WHERE n.trangThai = ? AND n.maNV != 'admin' "
                + "ORDER BY n.maNV";

        List<NhanVien> result = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, trangThai);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    NhanVien nv = mapRow(rs);
                    nv.setHoTen(rs.getString("hoTen"));
                    nv.setMaPhongBanHienTai(rs.getString("maPhongBanHT"));
                    nv.setTenPhongBanHienTai(rs.getString("tenPhongBan"));
                    nv.setTenChucVuHienTai(rs.getString("tenChucVu"));
                    result.add(nv);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tải nhân viên theo trạng thái: " + e.getMessage(), e);
        }
        return result;
    }

    // ============================
    // findDangLamViec - shortcut
    // ============================

    public List<NhanVien> findDangLamViec() {
        return findByTrangThai("dang_lam_viec");
    }

    // ============================
    // insert - trả về mã NV đã insert (String)
    // ============================

    public String insert(NhanVien nv) throws SQLException {
        String sql = "INSERT INTO NHANVIEN (maNV, loaiHopDong, ngayVaoLam, trangThai, ghiChu) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nv.getMaNhanVien());
            ps.setString(2, nv.getLoaiHopDong());
            ps.setDate(3, nv.getNgayVaoLam() != null ? Date.valueOf(nv.getNgayVaoLam()) : null);
            ps.setString(4, nv.getTrangThai());
            ps.setString(5, nv.getGhiChu());
            ps.executeUpdate();
            return nv.getMaNhanVien(); // trả về mã đã insert
        }
    }

    /**
     * Insert dùng Connection đã có (dùng cho transaction).
     */
    public String insert(Connection conn, NhanVien nv) throws SQLException {
        String sql = "INSERT INTO NHANVIEN (maNV, loaiHopDong, ngayVaoLam, trangThai, ghiChu) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nv.getMaNhanVien());
            ps.setString(2, nv.getLoaiHopDong());
            ps.setDate(3, nv.getNgayVaoLam() != null ? Date.valueOf(nv.getNgayVaoLam()) : null);
            ps.setString(4, nv.getTrangThai());
            ps.setString(5, nv.getGhiChu());
            ps.executeUpdate();
            return nv.getMaNhanVien();
        }
    }

    // ============================
    // update - chỉ cập nhật các trường cơ bản
    // ============================

    public void update(NhanVien nv) {
        String sql = "UPDATE NHANVIEN SET loaiHopDong=?, trangThai=?, ghiChu=? WHERE maNV=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nv.getLoaiHopDong());
            ps.setString(2, nv.getTrangThai());
            ps.setString(3, nv.getGhiChu());
            ps.setString(4, nv.getMaNhanVien());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi cập nhật nhân viên " + nv.getMaNhanVien() + ": " + e.getMessage(), e);
        }
    }

    // ============================
    // existsByMaNhanVien
    // ============================

    public boolean existsByMaNhanVien(String maNhanVien) {
        if (maNhanVien == null || maNhanVien.trim().isEmpty()) return false;

        String sql = "SELECT 1 FROM NHANVIEN WHERE maNV = ? LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maNhanVien);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi kiểm tra tồn tại maNV: " + maNhanVien + " - " + e.getMessage(), e);
        }
    }

    // ============================
    // findNhanVienByMaQuanLy (dùng String cho maQuanLy)
    // ============================

    public List<NhanVien> findNhanVienByMaQuanLy(String maQuanLy) {
        String sql = "SELECT n.*, t.hoTen, pb.maPhongBan AS maPhongBanHT, "
                + "pb.tenPhongBan, cv.tenChucVu "
                + "FROM NHANVIEN n "
                + "LEFT JOIN THONGTINCANHAN t ON n.maNV = t.maNV "
                + "JOIN BONHIEM b ON n.maNV = b.maNV "
                + "LEFT JOIN PHONGBAN pb ON b.maPhongBan = pb.maPhongBan "
                + "LEFT JOIN CHUCVU cv ON b.maChucVu = cv.maChucVu "
                + "WHERE b.trangThai = 'hieu_luc' AND b.maQuanLy = ? AND n.maNV != 'admin' "
                + "ORDER BY n.maNV";

        List<NhanVien> result = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maQuanLy);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    NhanVien nv = mapRow(rs);
                    nv.setHoTen(rs.getString("hoTen"));
                    nv.setMaPhongBanHienTai(rs.getString("maPhongBanHT"));
                    nv.setTenPhongBanHienTai(rs.getString("tenPhongBan"));
                    nv.setTenChucVuHienTai(rs.getString("tenChucVu"));
                    result.add(nv);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tìm nhân viên theo quản lý " + maQuanLy + ": " + e.getMessage(), e);
        }
        return result;
    }

    // ============================
    // generateMaNhanVien - NV001, NV002, ...
    // ============================

    public String generateMaNhanVien() {
        String sql = "SELECT MAX(maNV) FROM NHANVIEN WHERE maNV LIKE 'NV%' ORDER BY maNV DESC LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                String maxMa = rs.getString(1);
                if (maxMa != null && maxMa.startsWith("NV")) {
                    try {
                        int num = Integer.parseInt(maxMa.substring(2));
                        return String.format("NV%03d", num + 1);
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi sinh mã nhân viên: " + e.getMessage(), e);
        }
        return "NV001";
    }
}