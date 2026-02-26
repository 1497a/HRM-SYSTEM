package com.hrm.repo;

import com.hrm.model.BangLuong;
import com.hrm.model.ChiTietLuong;
import com.hrm.model.ThanhPhanLuong;
import com.hrm.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC repository cho Salary (Bảng lương, Chi tiết lương, Thành phần lương).
 * Singleton pattern.
 *
 * NOTE: Existing model field mappings:
 *
 * BangLuong: maBL, maNV, ngayBD, ngayKT, trangThai, ngayTao
 *   → DB: BANGLUONG(maBangLuong, thang, nam, tenBangLuong, ngayTao, ngayKhoa,
 *                   nguoiTao, nguoiKhoa, trangThai ENUM('dang_xu_ly','da_khoa'))
 *
 * ChiTietLuong: maChiTietLuong, maBL, maNV, tenNV, luongCoBan, tongLuongChucVu,
 *               tienOT, tongKhauTru, tongLuong, luongThucNhan, soNgayCong, tongGioOT
 *   → DB: CHITIETLUONG(maChiTiet, maBangLuong, maNV, luongCoSo, tongLuongChucVu,
 *                      luongLamThem, tongThuNhap, tongKhauTru, luongThucLanh,
 *                      soNgayCong, soGioLamThem)
 *
 * ThanhPhanLuong: maTp, maCTLuong, loai(Loai enum), tenKhoan, soTien, nguon
 *   → DB: THANHPHANLUONG(maThanhPhan, maChiTiet, tenThanhPhan, loai, soTien, ghiChu)
 */
public class BangLuongRepository {

    private static BangLuongRepository instance;

    private BangLuongRepository() {}

    public static synchronized BangLuongRepository getInstance() {
        if (instance == null) {
            instance = new BangLuongRepository();
        }
        return instance;
    }

    // =====================================================================
    // BANGLUONG
    // =====================================================================

    /** Insert a new BangLuong. Returns generated maBangLuong (stored in maBL field). */
    public int insertBangLuong(BangLuong bl) {
        int thang = bl.getNgayBD() != null ? bl.getNgayBD().getMonthValue() : 0;
        int nam = bl.getNgayBD() != null ? bl.getNgayBD().getYear() : 0;
        String tenBangLuong = "Bảng lương tháng " + thang + "/" + nam;
        String sql = "INSERT INTO BANGLUONG (thang, nam, tenBangLuong, ngayTao, trangThai) "
                + "VALUES (?,?,?,NOW(),?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, thang);
            ps.setInt(2, nam);
            ps.setString(3, tenBangLuong);
            ps.setString(4, blStatusToDb(bl.getTrangThai()));
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    bl.setMaBL(id);
                    return id;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi thêm bảng lương: " + e.getMessage(), e);
        }
        return 0;
    }

    public BangLuong findByThangNam(int thang, int nam) {
        String sql = "SELECT maBangLuong, thang, nam, tenBangLuong, ngayTao, ngayKhoa, nguoiKhoa, trangThai "
                + "FROM BANGLUONG WHERE thang=? AND nam=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, thang);
            ps.setInt(2, nam);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapBangLuong(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tìm bảng lương: " + e.getMessage(), e);
        }
        return null;
    }

    public BangLuong findById(int maBangLuong) {
        String sql = "SELECT maBangLuong, thang, nam, tenBangLuong, ngayTao, ngayKhoa, nguoiKhoa, trangThai "
                + "FROM BANGLUONG WHERE maBangLuong=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maBangLuong);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapBangLuong(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tìm bảng lương theo ID: " + e.getMessage(), e);
        }
        return null;
    }

    public List<BangLuong> findAll() {
        String sql = "SELECT maBangLuong, thang, nam, tenBangLuong, ngayTao, ngayKhoa, nguoiKhoa, trangThai "
                + "FROM BANGLUONG ORDER BY nam DESC, thang DESC";
        List<BangLuong> result = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(mapBangLuong(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tải danh sách bảng lương: " + e.getMessage(), e);
        }
        return result;
    }

    public void lockBangLuong(int maBangLuong, int nguoiKhoa) {
        String sql = "UPDATE BANGLUONG SET trangThai='da_khoa', ngayKhoa=NOW(), nguoiKhoa=? WHERE maBangLuong=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, nguoiKhoa);
            ps.setInt(2, maBangLuong);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khóa bảng lương: " + e.getMessage(), e);
        }
    }

    private BangLuong mapBangLuong(ResultSet rs) throws SQLException {
        BangLuong bl = new BangLuong();
        int maBL = rs.getInt("maBangLuong");
        bl.setMaBL(maBL);
        int thang = rs.getInt("thang");
        int nam = rs.getInt("nam");
        if (thang > 0 && nam > 0) {
            bl.setNgayBD(java.time.LocalDate.of(nam, thang, 1));
            bl.setNgayKT(java.time.LocalDate.of(nam, thang, 1).withDayOfMonth(
                    java.time.LocalDate.of(nam, thang, 1).lengthOfMonth()));
        }
        String tt = rs.getString("trangThai");
        bl.setTrangThai(dbToBlStatus(tt));
        Timestamp ngayTao = rs.getTimestamp("ngayTao");
        if (ngayTao != null) bl.setNgayTao(ngayTao.toLocalDateTime());
        return bl;
    }

    private String blStatusToDb(BangLuong.TrangThai tt) {
        if (tt == null) return "dang_xu_ly";
        // Map existing enum values to DB ENUM('dang_xu_ly','da_khoa')
        switch (tt) {
            case DA_DUYET:
            case DA_CHI:
                return "da_khoa";
            case DA_TINH:
                return "dang_xu_ly";
            default:
                return "dang_xu_ly";
        }
    }

    private BangLuong.TrangThai dbToBlStatus(String db) {
        if (db == null) return BangLuong.TrangThai.NHAP;
        switch (db) {
            case "da_khoa": return BangLuong.TrangThai.DA_DUYET;
            case "dang_xu_ly": return BangLuong.TrangThai.DA_TINH;
            default: return BangLuong.TrangThai.NHAP;
        }
    }

    // =====================================================================
    // CHITIETLUONG
    // =====================================================================

    /** Insert a ChiTietLuong record. Returns generated maChiTiet (stored in maChiTietLuong). */
    public int insertChiTiet(ChiTietLuong ctl) {
        String sql = "INSERT INTO CHITIETLUONG (maBangLuong, maNV, luongCoSo, tongLuongChucVu, "
                + "luongLamThem, tongThuNhap, tongKhauTru, luongThucLanh, soNgayCong, soGioLamThem, ghiChu) "
                + "VALUES (?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, ctl.getMaBL());
            ps.setInt(2, ctl.getMaNV());
            ps.setDouble(3, ctl.getLuongCoBan());
            ps.setDouble(4, ctl.getTongLuongChucVu());
            ps.setDouble(5, ctl.getTienOT());
            ps.setDouble(6, ctl.getTongLuong());
            ps.setDouble(7, ctl.getTongKhauTru());
            ps.setDouble(8, ctl.getLuongThucNhan());
            ps.setDouble(9, ctl.getSoNgayCong());
            ps.setDouble(10, ctl.getTongGioOT());
            ps.setString(11, ctl.getTenNV());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    ctl.setMaChiTietLuong(id);
                    return id;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi thêm chi tiết lương: " + e.getMessage(), e);
        }
        return 0;
    }

    /** Returns all ChiTietLuong for a BangLuong with tenNV transient. */
    public List<ChiTietLuong> findByBangLuong(int maBangLuong) {
        String sql = "SELECT cl.*, t.hoTen FROM CHITIETLUONG cl "
                + "LEFT JOIN THONGTINCANHAN t ON cl.maNV = t.maNV "
                + "WHERE cl.maBangLuong=? ORDER BY cl.maNV";
        List<ChiTietLuong> result = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maBangLuong);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ChiTietLuong ctl = mapChiTietLuong(rs);
                    // Load ThanhPhanLuong
                    ctl.setDanhSachThanhPhan(findByChiTiet(ctl.getMaChiTietLuong()));
                    result.add(ctl);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tải chi tiết lương: " + e.getMessage(), e);
        }
        return result;
    }

    public ChiTietLuong findByBangLuongAndNV(int maBangLuong, int maNV) {
        String sql = "SELECT cl.*, t.hoTen FROM CHITIETLUONG cl "
                + "LEFT JOIN THONGTINCANHAN t ON cl.maNV = t.maNV "
                + "WHERE cl.maBangLuong=? AND cl.maNV=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maBangLuong);
            ps.setInt(2, maNV);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ChiTietLuong ctl = mapChiTietLuong(rs);
                    ctl.setDanhSachThanhPhan(findByChiTiet(ctl.getMaChiTietLuong()));
                    return ctl;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tải chi tiết lương NV: " + e.getMessage(), e);
        }
        return null;
    }

    private ChiTietLuong mapChiTietLuong(ResultSet rs) throws SQLException {
        ChiTietLuong ctl = new ChiTietLuong();
        ctl.setMaChiTietLuong(rs.getInt("maChiTiet"));
        ctl.setMaBL(rs.getInt("maBangLuong"));
        ctl.setMaNV(rs.getInt("maNV"));
        ctl.setLuongCoBan(rs.getDouble("luongCoSo"));
        ctl.setTongLuongChucVu(rs.getDouble("tongLuongChucVu"));
        ctl.setTienOT(rs.getDouble("luongLamThem"));
        ctl.setTongLuong(rs.getDouble("tongThuNhap"));
        ctl.setTongKhauTru(rs.getDouble("tongKhauTru"));
        ctl.setLuongThucNhan(rs.getDouble("luongThucLanh"));
        ctl.setSoNgayCong((int) rs.getDouble("soNgayCong"));
        ctl.setTongGioOT(rs.getDouble("soGioLamThem"));
        // transient
        try { ctl.setTenNV(rs.getString("hoTen")); } catch (SQLException ignored) {}
        return ctl;
    }

    // =====================================================================
    // THANHPHANLUONG
    // =====================================================================

    /** Batch insert list of ThanhPhanLuong. */
    public void insertThanhPhanBatch(List<ThanhPhanLuong> list) {
        if (list == null || list.isEmpty()) return;
        String sql = "INSERT INTO THANHPHANLUONG (maChiTiet, tenThanhPhan, loai, soTien, ghiChu) "
                + "VALUES (?,?,?,?,?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            for (ThanhPhanLuong tp : list) {
                ps.setInt(1, tp.getMaCTLuong());
                ps.setString(2, tp.getTenKhoan());
                ps.setString(3, loaiToDb(tp.getLoai()));
                ps.setDouble(4, tp.getSoTien());
                ps.setString(5, tp.getNguon());
                ps.addBatch();
            }
            ps.executeBatch();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi thêm thành phần lương: " + e.getMessage(), e);
        }
    }

    public List<ThanhPhanLuong> findByChiTiet(int maChiTiet) {
        String sql = "SELECT maThanhPhan, maChiTiet, tenThanhPhan, loai, soTien, ghiChu "
                + "FROM THANHPHANLUONG WHERE maChiTiet=? ORDER BY loai, maThanhPhan";
        List<ThanhPhanLuong> result = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maChiTiet);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ThanhPhanLuong tp = new ThanhPhanLuong();
                    tp.setMaTp(rs.getInt("maThanhPhan"));
                    tp.setMaCTLuong(rs.getInt("maChiTiet"));
                    tp.setTenKhoan(rs.getString("tenThanhPhan"));
                    tp.setSoTien(rs.getDouble("soTien"));
                    tp.setNguon(rs.getString("ghiChu"));
                    String loaiStr = rs.getString("loai");
                    tp.setLoai(dbToLoai(loaiStr));
                    result.add(tp);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tải thành phần lương: " + e.getMessage(), e);
        }
        return result;
    }

    private String loaiToDb(ThanhPhanLuong.Loai loai) {
        if (loai == null) return "thu_nhap";
        switch (loai) {
            case KHAU_TRU: return "khau_tru";
            default: return "thu_nhap";
        }
    }

    private ThanhPhanLuong.Loai dbToLoai(String db) {
        if ("khau_tru".equals(db)) return ThanhPhanLuong.Loai.KHAU_TRU;
        return ThanhPhanLuong.Loai.PHU_CAP;
    }

    // =====================================================================
    // Helpers for salary calculation
    // =====================================================================

    /** Returns luongCoSo from the currently active labour contract for this employee. */
    public double getLuongCoSoFromHopDong(int maNV) {
        String sql = "SELECT luongCoSo FROM HOPDONGLAODONG WHERE maNV=? AND trangThai='hieu_luc' "
                + "ORDER BY ngayHieuLuc DESC LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maNV);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble("luongCoSo");
            }
        } catch (SQLException e) {
            // Return 0 if no contract found
        }
        return 0.0;
    }

    /**
     * Returns total bonus salary from active BONHIEM records for this employee.
     * luongChucVu = SUM( tyLeHuongLuong/100 * heSoLuong * luongCoSo + phuCapChucVu )
     */
    public double getTongLuongChucVu(int maNV, double luongCoSo) {
        String sql = "SELECT b.tyLeHuongLuong, cv.heSoLuong, cv.phuCapChucVu "
                + "FROM BONHIEM b "
                + "JOIN CHUCVU cv ON b.maChucVu = cv.maChucVu "
                + "WHERE b.maNV=? AND b.trangThai='hieu_luc'";
        double total = 0;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maNV);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    double tyLe = rs.getDouble("tyLeHuongLuong");
                    double heSo = rs.getDouble("heSoLuong");
                    double phuCap = rs.getDouble("phuCapChucVu");
                    total += (tyLe / 100.0) * heSo * luongCoSo + phuCap;
                }
            }
        } catch (SQLException e) {
            // Return 0 on error
        }
        return total;
    }
}
