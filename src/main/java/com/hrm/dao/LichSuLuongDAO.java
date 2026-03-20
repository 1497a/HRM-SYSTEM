package com.hrm.dao;

import com.hrm.model.LichSuHeSoLuong;
import com.hrm.util.DatabaseConnection;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository JDBC cho bang LICHSU_HESOLUONG.
 */
public class LichSuLuongDAO {

    private static LichSuLuongDAO instance;

    private LichSuLuongDAO() {}

    public static LichSuLuongDAO getInstance() {
        if (instance == null) {
            instance = new LichSuLuongDAO();
        }
        return instance;
    }

    /**
     * Chen mot ban ghi lich su moi vao LICHSU_HESOLUONG.
     * @return maLichSu do DB sinh ra, hoac 0 neu that bai
     */
    public int insert(LichSuHeSoLuong h) {
        String sql = "INSERT INTO LICHSU_HESOLUONG "
                + "(maChucVu, heSoLuongCu, heSoLuongMoi, phuCapCu, phuCapMoi, ngayThayDoi) "
                + "VALUES (?, ?, ?, ?, ?, NOW())";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, h.getChucVuId());
            ps.setDouble(2, h.getHeSoLuongCu());
            ps.setDouble(3, h.getHeSoLuongMoi());
            ps.setDouble(4, h.getPhuCapCu());
            ps.setDouble(5, h.getPhuCapMoi());
            ps.executeUpdate();

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Loi LichSuLuongDAO.insert: " + e.getMessage());
        }
        return 0;
    }

    /** Lay toan bo lich su thay doi he so luong, moi nhat truoc. */
    public List<LichSuHeSoLuong> findAll() {
        List<LichSuHeSoLuong> list = new ArrayList<>();
        String sql = "SELECT maLichSu, maChucVu, heSoLuongCu, heSoLuongMoi, "
                + "phuCapCu, phuCapMoi, ngayThayDoi "
                + "FROM LICHSU_HESOLUONG ORDER BY ngayThayDoi DESC, maLichSu DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Loi LichSuLuongDAO.findAll: " + e.getMessage());
        }
        return list;
    }

    /** Lay lich su thay doi theo ma chuc vu, moi nhat truoc. */
    public List<LichSuHeSoLuong> findByMaChucVu(String maChucVu) {
        List<LichSuHeSoLuong> list = new ArrayList<>();
        String sql = "SELECT maLichSu, maChucVu, heSoLuongCu, heSoLuongMoi, "
                + "phuCapCu, phuCapMoi, ngayThayDoi "
                + "FROM LICHSU_HESOLUONG WHERE maChucVu = ? "
                + "ORDER BY ngayThayDoi DESC, maLichSu DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maChucVu);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Loi LichSuLuongDAO.findByMaChucVu: " + e.getMessage());
        }
        return list;
    }

    /** Alias de tuong thich voi code cu. */
    public void save(LichSuHeSoLuong history) {
        insert(history);
    }

    private LichSuHeSoLuong mapRow(ResultSet rs) throws SQLException {
        int id = rs.getInt("maLichSu");
        String maChucVu = rs.getString("maChucVu");
        double heSoLuongCu = rs.getDouble("heSoLuongCu");
        double heSoLuongMoi = rs.getDouble("heSoLuongMoi");
        double phuCapCu = rs.getDouble("phuCapCu");
        double phuCapMoi = rs.getDouble("phuCapMoi");

        String ngayThayDoi = "";
        Timestamp ts = rs.getTimestamp("ngayThayDoi");
        if (ts != null) {
            ngayThayDoi = new SimpleDateFormat("dd/MM/yyyy").format(ts);
        }

        return new LichSuHeSoLuong(id, maChucVu, heSoLuongCu, heSoLuongMoi,
                phuCapCu, phuCapMoi, ngayThayDoi, "");
    }
}
