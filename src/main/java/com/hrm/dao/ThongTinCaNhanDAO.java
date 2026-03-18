package com.hrm.dao;

import com.hrm.model.ThongTinCaNhan;
import com.hrm.util.DatabaseConnection;

import java.sql.*;

/**
 * Repository cho bảng THONGTINCANHAN.
 * Singleton pattern - sử dụng MySQL JDBC.
 */
public class ThongTinCaNhanDAO {

    private static ThongTinCaNhanDAO instance;
    private ThongTinCaNhanDAO() {
    }

    public static synchronized ThongTinCaNhanDAO getInstance() {
        if (instance == null) {
            instance = new ThongTinCaNhanDAO();
        }
        return instance;
    }

    // ============================
    // Mapping helper
    // ============================
    private ThongTinCaNhan mapRow(ResultSet rs) throws SQLException {
        ThongTinCaNhan ttcn = new ThongTinCaNhan();
        ttcn.setMaNV(rs.getString("maNV"));
        ttcn.setHoTen(rs.getString("hoTen"));
        Date ngaySinh = rs.getDate("ngaySinh");
        if (ngaySinh != null) {
            ttcn.setNgaySinh(ngaySinh.toLocalDate());
        }
        ttcn.setGioiTinh(rs.getString("gioiTinh"));
        ttcn.setCccd(rs.getString("CCCD"));
        ttcn.setDienThoai(rs.getString("dienThoai"));
        ttcn.setEmail(rs.getString("email"));
        ttcn.setDiaChi(rs.getString("diaChi"));
        ttcn.setDiaChiThuongTru(rs.getString("diaChiThuongTru"));
        ttcn.setQueQuan(rs.getString("queQuan"));
        ttcn.setTinhTrangHonNhan(rs.getString("tinhTrangHonNhan"));
        ttcn.setFileCv(rs.getString("fileCV"));
        ttcn.setTrinhDoHocVan(rs.getString("trinhDoHocVan"));
        ttcn.setKinhNghiem(rs.getString("kinhNghiem"));
        return ttcn;
    }

    // ============================
    // findByMaNV
    // ============================
    public ThongTinCaNhan findByMaNV(String maNV) {
        String sql = "SELECT * FROM THONGTINCANHAN WHERE maNV = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maNV);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tải thông tin cá nhân: " + e.getMessage(), e);
        }
        return null;
    }

    // ============================
    // insert (upsert using ON DUPLICATE KEY)
    // ============================
    public void insert(ThongTinCaNhan ttcn) throws SQLException {
        String sql = "INSERT INTO THONGTINCANHAN "
                + "(maNV, hoTen, ngaySinh, gioiTinh, CCCD, dienThoai, email, "
                + " diaChi, diaChiThuongTru, queQuan, tinhTrangHonNhan, fileCV, trinhDoHocVan, kinhNghiem) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE "
                + "hoTen=VALUES(hoTen), ngaySinh=VALUES(ngaySinh), gioiTinh=VALUES(gioiTinh), "
                + "CCCD=VALUES(CCCD), dienThoai=VALUES(dienThoai), email=VALUES(email), "
                + "diaChi=VALUES(diaChi), diaChiThuongTru=VALUES(diaChiThuongTru), "
                + "queQuan=VALUES(queQuan), tinhTrangHonNhan=VALUES(tinhTrangHonNhan), "
                + "fileCV=VALUES(fileCV), trinhDoHocVan=VALUES(trinhDoHocVan), kinhNghiem=VALUES(kinhNghiem)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            setParams(ps, ttcn);
            ps.executeUpdate();
        }
    }

    /**
     * Insert với Connection đã có (dùng cho transaction).
     */
    public void insert(Connection conn, ThongTinCaNhan ttcn) throws SQLException {
        String sql = "INSERT INTO THONGTINCANHAN "
                + "(maNV, hoTen, ngaySinh, gioiTinh, CCCD, dienThoai, email, "
                + " diaChi, diaChiThuongTru, queQuan, tinhTrangHonNhan, fileCV, trinhDoHocVan, kinhNghiem) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE "
                + "hoTen=VALUES(hoTen), ngaySinh=VALUES(ngaySinh), gioiTinh=VALUES(gioiTinh), "
                + "CCCD=VALUES(CCCD), dienThoai=VALUES(dienThoai), email=VALUES(email), "
                + "diaChi=VALUES(diaChi), diaChiThuongTru=VALUES(diaChiThuongTru), "
                + "queQuan=VALUES(queQuan), tinhTrangHonNhan=VALUES(tinhTrangHonNhan), "
                + "fileCV=VALUES(fileCV), trinhDoHocVan=VALUES(trinhDoHocVan), kinhNghiem=VALUES(kinhNghiem)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            setParams(ps, ttcn);
            ps.executeUpdate();
        }
    }

    // ============================
    // update
    // ============================
    public void update(ThongTinCaNhan ttcn) {
        String sql = "UPDATE THONGTINCANHAN SET "
                + "hoTen=?, ngaySinh=?, gioiTinh=?, CCCD=?, dienThoai=?, email=?, "
                + "diaChi=?, diaChiThuongTru=?, queQuan=?, tinhTrangHonNhan=?, "
                + "fileCV=?, trinhDoHocVan=?, kinhNghiem=? "
                + "WHERE maNV=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ttcn.getHoTen());
            ps.setDate(2, ttcn.getNgaySinh() != null ? Date.valueOf(ttcn.getNgaySinh()) : null);
            ps.setString(3, ttcn.getGioiTinh());
            ps.setString(4, ttcn.getCccd());
            ps.setString(5, ttcn.getDienThoai());
            ps.setString(6, ttcn.getEmail());
            ps.setString(7, ttcn.getDiaChi());
            ps.setString(8, ttcn.getDiaChiThuongTru());
            ps.setString(9, ttcn.getQueQuan());
            ps.setString(10, ttcn.getTinhTrangHonNhan());
            ps.setString(11, ttcn.getFileCv());
            ps.setString(12, ttcn.getTrinhDoHocVan());
            ps.setString(13, ttcn.getKinhNghiem());
            ps.setString(14, ttcn.getMaNV());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi cập nhật thông tin cá nhân: " + e.getMessage(), e);
        }
    }

    // ============================
    // existsByCCCD - check uniqueness excluding current NV
    // ============================
    public boolean existsByCCCD(String cccd, String excludeMaNV) {
        String sql = "SELECT COUNT(*) FROM THONGTINCANHAN WHERE CCCD = ? AND maNV <> ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cccd);
            ps.setString(2, excludeMaNV);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi kiểm tra CCCD: " + e.getMessage(), e);
        }
        return false;
    }

    // ============================
    // Private helper
    // ============================
    private void setParams(PreparedStatement ps, ThongTinCaNhan ttcn) throws SQLException {
        ps.setString(1, ttcn.getMaNV());
        ps.setString(2, ttcn.getHoTen());
        ps.setDate(3, ttcn.getNgaySinh() != null ? Date.valueOf(ttcn.getNgaySinh()) : null);
        ps.setString(4, ttcn.getGioiTinh());
        ps.setString(5, ttcn.getCccd());
        ps.setString(6, ttcn.getDienThoai());
        ps.setString(7, ttcn.getEmail());
        ps.setString(8, ttcn.getDiaChi());
        ps.setString(9, ttcn.getDiaChiThuongTru());
        ps.setString(10, ttcn.getQueQuan());
        ps.setString(11, ttcn.getTinhTrangHonNhan());
        ps.setString(12, ttcn.getFileCv());
        ps.setString(13, ttcn.getTrinhDoHocVan());
        ps.setString(14, ttcn.getKinhNghiem());
    }
}
