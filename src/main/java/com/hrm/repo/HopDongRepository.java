package com.hrm.repo;

import com.hrm.model.HopDong;
import com.hrm.util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HopDongRepository {
    
    // Hàm lấy danh sách tất cả hợp đồng
    public List<HopDong> getAllHopDong() {
        List<HopDong> list = new ArrayList<>();
        // Tên cột SQL phải khớp với bảng HOPDONGLAODONG của bạn
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
}