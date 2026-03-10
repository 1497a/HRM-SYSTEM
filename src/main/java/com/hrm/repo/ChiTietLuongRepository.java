package com.hrm.repo;

import com.hrm.model.ChiTietLuong;
import com.hrm.util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ChiTietLuongRepository {
    
    public List<String[]> getDanhSachKyLuong() {
        List<String[]> list = new ArrayList<>();
        String sql = "SELECT maBangLuong, tenBangLuong, trangThai FROM BANGLUONG ORDER BY nam DESC, thang DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new String[]{ rs.getString("maBangLuong"), rs.getString("tenBangLuong"), rs.getString("trangThai") });
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<ChiTietLuong> getSalaryDetailsByBangLuong(int maBangLuong) {
        List<ChiTietLuong> list = new ArrayList<>();
        String sql = "SELECT n.maNhanVien, c.luongCoSo, c.tongThuNhap, c.tongKhauTru, c.luongThucLanh, b.trangThai " +
                     "FROM CHITIETLUONG c " +
                     "JOIN NHANVIEN n ON c.maNV = n.maNV " +
                     "JOIN BANGLUONG b ON c.maBangLuong = b.maBangLuong " +
                     "WHERE c.maBangLuong = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maBangLuong);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ChiTietLuong c = new ChiTietLuong();
                c.setTenNV(rs.getString("maNhanVien")); 
                c.setLuongCoBan(rs.getDouble("luongCoSo"));
                c.setTongLuong(rs.getDouble("tongThuNhap"));
                c.setTongKhauTru(rs.getDouble("tongKhauTru"));
                c.setLuongThucNhan(rs.getDouble("luongThucLanh"));
                c.setTrangThai("da_khoa".equals(rs.getString("trangThai")) ? ChiTietLuong.TrangThai.DA_DUYET : ChiTietLuong.TrangThai.CHUA_TINH);
                list.add(c);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // --- HÀM MỚI: LẤY CHI TIẾT CÁC KHOẢN THU NHẬP/KHẤU TRỪ --- [cite: 169, 171]
    public List<String[]> getThanhPhanLuong(String maNhanVien, int maBangLuong) {
        List<String[]> list = new ArrayList<>();
        String sql = "SELECT tp.loai, tp.tenKhoan, tp.soTien " +
                     "FROM THANHPHANLUONG tp " +
                     "JOIN CHITIETLUONG ct ON tp.maChiTietLuong = ct.maChiTietLuong " +
                     "JOIN NHANVIEN n ON ct.maNV = n.maNV " +
                     "WHERE n.maNhanVien = ? AND ct.maBangLuong = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maNhanVien);
            ps.setInt(2, maBangLuong);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new String[]{ rs.getString("loai"), rs.getString("tenKhoan"), rs.getString("soTien") });
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public boolean tinhLuong(int maBangLuong) {
        String sqlDeleteTp = "DELETE FROM THANHPHANLUONG WHERE maChiTietLuong IN (SELECT maChiTietLuong FROM CHITIETLUONG WHERE maBangLuong = ?)";
        String sqlDeleteCt = "DELETE FROM CHITIETLUONG WHERE maBangLuong = ?";
        
        String sqlInsertCt = "INSERT INTO CHITIETLUONG (maBangLuong, maNV, luongCoSo, tongThuNhap, tongKhauTru, luongThucLanh) " +
                             "SELECT ?, h.maNV, h.luongCoSo, (h.luongCoSo + 1000000), (h.luongCoSo * 0.105), (h.luongCoSo + 1000000 - (h.luongCoSo * 0.105)) " +
                             "FROM HOPDONGLAODONG h JOIN NHANVIEN n ON h.maNV = n.maNV " +
                             "WHERE h.trangThai = 'hieu_luc' AND n.trangThai = 'dang_lam_viec'";

        String sqlInsertTp = "INSERT INTO THANHPHANLUONG (maChiTietLuong, loai, tenKhoan, soTien, nguon) " +
                             "SELECT maChiTietLuong, 'thu_nhap', 'Phụ cấp mặc định', 1000000, 'he_thong' FROM CHITIETLUONG WHERE maBangLuong = ? " +
                             "UNION ALL " +
                             "SELECT maChiTietLuong, 'khau_tru', 'Bảo hiểm (10.5%)', (luongCoSo * -0.105), 'he_thong' FROM CHITIETLUONG WHERE maBangLuong = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps1 = conn.prepareStatement(sqlDeleteTp);
                 PreparedStatement ps2 = conn.prepareStatement(sqlDeleteCt);
                 PreparedStatement ps3 = conn.prepareStatement(sqlInsertCt);
                 PreparedStatement ps4 = conn.prepareStatement(sqlInsertTp)) {
                ps1.setInt(1, maBangLuong); ps1.executeUpdate();
                ps2.setInt(1, maBangLuong); ps2.executeUpdate();
                ps3.setInt(1, maBangLuong); ps3.executeUpdate();
                ps4.setInt(1, maBangLuong); ps4.setInt(2, maBangLuong); ps4.executeUpdate();
                conn.commit(); return true;
            } catch (SQLException e) { conn.rollback(); e.printStackTrace(); }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean khoaBangLuong(int maBangLuong) {
        String sql = "UPDATE BANGLUONG SET trangThai = 'da_khoa' WHERE maBangLuong = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maBangLuong);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }
}