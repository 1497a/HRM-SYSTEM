package com.hrm.repo;

import com.hrm.model.HopDong;
import com.hrm.util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HopDongRepository {
    
    public List<HopDong> getAllHopDong() {
        List<HopDong> list = new ArrayList<>();
        String sql = "SELECT maHopDong, maNV, loaiHopDong, luongCoSo, ngayHieuLuc, trangThai FROM HOPDONGLAODONG";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                HopDong hd = new HopDong();
                hd.setMaHopDong(rs.getString("maHopDong"));
                hd.setMaNV(rs.getString("maNV"));
                hd.setLoaiHopDong(rs.getString("loaiHopDong"));
                hd.setLuongCoSo(rs.getDouble("luongCoSo"));
                hd.setNgayHieuLuc(rs.getDate("ngayHieuLuc"));
                hd.setTrangThai(rs.getString("trangThai"));
                list.add(hd);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean insertHopDong(String soHopDong, String maNhanVien, String loaiHopDong, double luongCoSo, java.sql.Date ngayHieuLuc) {
        String sql = "INSERT INTO HOPDONGLAODONG (soHopDong, maNV, loaiHopDong, luongCoSo, ngayKy, ngayHieuLuc, trangThai) " +
                     "VALUES (?, (SELECT maNV FROM NHANVIEN WHERE maNhanVien = ?), ?, ?, ?, ?, 'hieu_luc')";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, soHopDong);
            ps.setString(2, maNhanVien);
            ps.setString(3, loaiHopDong);
            ps.setDouble(4, luongCoSo);
            ps.setDate(5, new java.sql.Date(System.currentTimeMillis())); 
            ps.setDate(6, ngayHieuLuc);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // LỆNH SỬA HỢP ĐỒNG
    public boolean updateHopDong(String maHopDong, String loaiHopDong, double luongCoSo, java.sql.Date ngayHieuLuc, String trangThai) {
        String sql = "UPDATE HOPDONGLAODONG SET loaiHopDong = ?, luongCoSo = ?, ngayHieuLuc = ?, trangThai = ? WHERE maHopDong = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, loaiHopDong);
            ps.setDouble(2, luongCoSo);
            ps.setDate(3, ngayHieuLuc);
            ps.setString(4, trangThai);
            ps.setString(5, maHopDong);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // LỆNH XÓA HỢP ĐỒNG
    public boolean deleteHopDong(String maHopDong) {
        String sql = "DELETE FROM HOPDONGLAODONG WHERE maHopDong = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maHopDong);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}