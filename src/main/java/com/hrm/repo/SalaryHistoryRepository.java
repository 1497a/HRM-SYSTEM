package com.hrm.repo;

import com.hrm.model.SalaryHistory;
import com.hrm.util.DatabaseConnection;

import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository JDBC cho bảng LICHSU_HESOLUONG.
 * Áp dụng Singleton pattern nhất quán với các Repository khác trong dự án.
 */
public class SalaryHistoryRepository {

    // ====================================================
    // Singleton
    // ====================================================

    private static SalaryHistoryRepository instance;

    private SalaryHistoryRepository() {}

    public static SalaryHistoryRepository getInstance() {
        if (instance == null) {
            instance = new SalaryHistoryRepository();
        }
        return instance;
    }

    // ====================================================
    // Public API
    // ====================================================

    /**
     * Chèn một bản ghi lịch sử mới vào LICHSU_HESOLUONG.
     * maLichSu được DB tự sinh (AUTO_INCREMENT).
     *
     * @param h đối tượng SalaryHistory cần lưu
     * @return maLichSu do DB sinh ra, hoặc 0 nếu thất bại
     */
    public int insert(SalaryHistory h) {
        String sql = "INSERT INTO LICHSU_HESOLUONG "
                + "(maChucVu, heSoLuongCu, heSoLuongMoi, phuCapCu, phuCapMoi, ngayThayDoi) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, h.getMaChucVu());
            ps.setDouble(2, h.getHeSoLuongCu());
            ps.setDouble(3, h.getHeSoLuongMoi());
            ps.setDouble(4, h.getPhuCapCu());
            ps.setDouble(5, h.getPhuCapMoi());

            // ngayThayDoi trong model là String "dd/MM/yyyy"; chuyển sang java.sql.Date cho DB
            java.sql.Date sqlDate = parseDateString(h.getNgayThayDoi());
            if (sqlDate != null) {
                ps.setDate(6, sqlDate);
            } else {
                ps.setNull(6, Types.DATE);
            }

            ps.executeUpdate();

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi SalaryHistoryRepository.insert: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Lấy toàn bộ lịch sử thay đổi hệ số lương, mới nhất trước.
     *
     * @return danh sách SalaryHistory
     */
    public List<SalaryHistory> findAll() {
        List<SalaryHistory> list = new ArrayList<>();
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
            System.err.println("Lỗi SalaryHistoryRepository.findAll: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Lấy lịch sử thay đổi hệ số lương theo mã chức vụ, mới nhất trước.
     *
     * @param maChucVu mã chức vụ cần tra cứu
     * @return danh sách SalaryHistory tương ứng
     */
    public List<SalaryHistory> findByMaChucVu(String maChucVu) {
        List<SalaryHistory> list = new ArrayList<>();
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
            System.err.println("Lỗi SalaryHistoryRepository.findByMaChucVu: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Lưu một bản ghi lịch sử. Giữ lại để tương thích với PositionService.
     * Thực chất gọi insert() bên trong.
     *
     * @param history đối tượng SalaryHistory cần lưu
     */
    public void save(SalaryHistory history) {
        insert(history);
    }

    /**
     * Trả về 0 vì ID được DB tự sinh (AUTO_INCREMENT).
     * Giữ lại để tương thích với PositionService.
     *
     * @return 0
     */
    public int generateId() {
        return 0;
    }

    // ====================================================
    // Private helpers
    // ====================================================

    /**
     * Ánh xạ một hàng ResultSet sang đối tượng SalaryHistory.
     * ngayThayDoi được định dạng thành "dd/MM/yyyy".
     */
    private SalaryHistory mapRow(ResultSet rs) throws SQLException {
        int id = rs.getInt("maLichSu");
        String maChucVu = rs.getString("maChucVu");
        double heSoLuongCu = rs.getDouble("heSoLuongCu");
        double heSoLuongMoi = rs.getDouble("heSoLuongMoi");
        double phuCapCu = rs.getDouble("phuCapCu");
        double phuCapMoi = rs.getDouble("phuCapMoi");

        String ngayThayDoi = "";
        Timestamp ts = rs.getTimestamp("ngayThayDoi");
        if (ts != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            ngayThayDoi = sdf.format(ts);
        }

        // nguoiThayDoi: cột DB là INT (FK sang NHANVIEN), model dùng String tên.
        // Trả về chuỗi rỗng vì thông tin này không được lưu khi insert từ ứng dụng.
        String nguoiThayDoi = "";

        return new SalaryHistory(id, maChucVu, heSoLuongCu, heSoLuongMoi,
                phuCapCu, phuCapMoi, ngayThayDoi, nguoiThayDoi);
    }

    /**
     * Phân tích chuỗi ngày "dd/MM/yyyy" thành java.sql.Date.
     *
     * @param dateStr chuỗi ngày định dạng "dd/MM/yyyy"
     * @return java.sql.Date, hoặc null nếu phân tích thất bại
     */
    private java.sql.Date parseDateString(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            sdf.setLenient(false);
            java.util.Date parsed = sdf.parse(dateStr.trim());
            return new java.sql.Date(parsed.getTime());
        } catch (ParseException e) {
            System.err.println("SalaryHistoryRepository: không phân tích được ngày '" + dateStr + "': " + e.getMessage());
            return null;
        }
    }
}
