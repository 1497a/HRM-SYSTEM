package com.hrm.repo;

import com.hrm.model.ChiTietLuong;
import com.hrm.util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ChiTietLuongRepository {
    
    public List<ChiTietLuong> getAllSalaryDetails() {
        List<ChiTietLuong> list = new ArrayList<>();
        // Kết hợp CHITIETLUONG, NHANVIEN và BANGLUONG
        String sql = "SELECT n.maNhanVien, c.luongCoSo, c.tongThuNhap, c.tongKhauTru, c.luongThucLanh, b.trangThai " +
                     "FROM CHITIETLUONG c " +
                     "JOIN NHANVIEN n ON c.maNV = n.maNV " +
                     "JOIN BANGLUONG b ON c.maBangLuong = b.maBangLuong";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                ChiTietLuong c = new ChiTietLuong();
                // Tạm dùng setTenNV để lưu cái mã NV001 hiển thị lên bảng cho tiện
                c.setTenNV(rs.getString("maNhanVien")); 
                c.setLuongCoBan(rs.getDouble("luongCoSo"));
                c.setTongLuong(rs.getDouble("tongThuNhap"));
                c.setTongKhauTru(rs.getDouble("tongKhauTru"));
                c.setLuongThucNhan(rs.getDouble("luongThucLanh"));
                
                // Xử lý cái Enum TrangThai cực xịn của ông
                String tt = rs.getString("trangThai");
                if ("da_khoa".equals(tt)) {
                    c.setTrangThai(ChiTietLuong.TrangThai.DA_DUYET);
                } else {
                    c.setTrangThai(ChiTietLuong.TrangThai.CHUA_TINH);
                }
                
                list.add(c);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}