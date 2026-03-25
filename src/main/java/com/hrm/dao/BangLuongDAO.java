package com.hrm.dao;

import com.hrm.model.DataScope;
import com.hrm.model.BangLuong;
import com.hrm.model.ChiTietLuong;
import com.hrm.model.ThanhPhanLuong;
import com.hrm.util.DaoHelper;
import com.hrm.util.DatabaseConnection;
import com.hrm.util.HRMConstants;
import com.hrm.util.SessionContext;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * JDBC DAO cho Salary (Bảng lương, Chi tiết lương, Thành phần lương).
 * Singleton pattern.
 *
 * NOTE: Existing model field mappings:
 *
 * BangLuong: maBL, maNV, ngayBD, ngayKT, trangThai, ngayTao
 *   → DB: BANGLUONG(id, thang, nam, tenBangLuong, ngayTao, ngayKhoa,
 *                   nguoiTao, nguoiKhoa, trangThai ENUM('dang_xu_ly','da_khoa'))
 *
 * ChiTietLuong: maChiTietLuong, maBL, maNV, tenNV, luongCoBan, tongLuongChucVu,
 *               tienOT, tongKhauTru, tongLuong, luongThucNhan, soNgayCong, tongGioOT
 *   → DB: CHITIETLUONG(id, maBangLuong, maNV, luongCoSo, tongLuongChucVu,
 *                      luongLamThem, tongThuNhap, tongKhauTru, luongThucLanh,
 *                      soNgayCong, soGioLamThem)
 *
 * ThanhPhanLuong: maTp, maCTLuong, loai(Loai enum), tenKhoan, soTien, nguon
 *   → DB: THANHPHANLUONG(id, maChiTiet, tenThanhPhan, loai, soTien, ghiChu)
 */
public class BangLuongDAO {

    private static BangLuongDAO instance;
    private BangLuongDAO() {}

    public static synchronized BangLuongDAO getInstance() {
        if (instance == null) {
            instance = new BangLuongDAO();
        }
        return instance;
    }

    // =====================================================================
    // BANG_LUONGS
    // =====================================================================
    /** Insert a new BangLuong. Returns generated id (stored in maBL field). */
    public int insertBangLuong(BangLuong bl) {
        int thang = bl.getThang();
        int nam = bl.getNam();
        String tenBangLuong = "Bảng lương tháng " + thang + "/" + nam;
        int nguoiTao = SessionContext.getInstance().getCurrentUser() != null
                ? SessionContext.getInstance().getCurrentUser().getId() : 0;
        String sql = "INSERT INTO BANGLUONG (thang, nam, tenBangLuong, ngayTao, nguoiTao, trangThai) "
                + "VALUES (?,?,?,NOW(),?,?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, thang);
            ps.setInt(2, nam);
            ps.setString(3, tenBangLuong);
            if (nguoiTao > 0) {
                ps.setInt(4, nguoiTao);
            } else {
                ps.setNull(4, Types.INTEGER);
            }
            ps.setString(5, blStatusToDb(bl.getTrangThai()));
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    bl.setMaBL(id);
                    bl.setTenBangLuong(tenBangLuong);
                    if (nguoiTao > 0) {
                        bl.setNguoiTao(String.valueOf(nguoiTao));
                    }
                    return id;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi thêm bảng lương: " + e.getMessage(), e);
        }
        return 0;
    }

    public BangLuong findByThangNam(int thang, int nam) {
        String sql = baseBangLuongSelect() + " WHERE bl.thang=? AND bl.nam=?";
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

    public BangLuong findById(int id) {
        String sql = baseBangLuongSelect() + " WHERE bl.maBangLuong=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapBangLuong(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tìm bảng lương theo ID: " + e.getMessage(), e);
        }
        return null;
    }

    public List<BangLuong> findAll() {
        String sql = baseBangLuongSelect() + " ORDER BY bl.nam DESC, bl.thang DESC";
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

    public List<BangLuong> findAllByScope(DataScope scope, String currentMaNV) {
        List<BangLuong> result = new ArrayList<>();
        if (scope == null || scope == DataScope.NONE) {
            return result;
        }
        if (scope == DataScope.ALL) {
            return findAll();
        }
        try (Connection conn = DatabaseConnection.getConnection()) {
            ScopeFilter filter = buildPayrollScopeFilter(scope, currentMaNV, conn);
            if (filter == null) {
                return result;
            }
            String sql = baseBangLuongSelect()
                    + " WHERE bl.maBangLuong IN (SELECT DISTINCT cl.maBangLuong FROM CHITIETLUONG cl "
                    + "LEFT JOIN BONHIEM b ON cl.maNV = b.maNV "
                    + "AND b.trangThai = '" + HRMConstants.TRANG_THAI_HIEU_LUC + "' "
                    + "AND b.loaiBoNhiem = '" + HRMConstants.LOAI_BO_NHIEM_CHINH + "' "
                    + filter.whereClause + ")"
                    + " ORDER BY bl.nam DESC, bl.thang DESC";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                bindScopeParams(ps, filter.params);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        result.add(mapBangLuong(rs));
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tải danh sách bảng lương theo scope: " + e.getMessage(), e);
        }
        return result;
    }

    public int lockBangLuong(int id, int nguoiKhoa) {
        String sql = "UPDATE BANGLUONG SET trangThai='" + HRMConstants.TRANG_THAI_DA_KHOA + "', ngayKhoa=NOW(), nguoiKhoa=? WHERE maBangLuong=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, nguoiKhoa);
            ps.setInt(2, id);
            return ps.executeUpdate();
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
            bl.setThang(thang); bl.setNam(nam);
        }
        bl.setTenBangLuong(rs.getString("tenBangLuong"));
        String tt = rs.getString("trangThai");
        bl.setTrangThai(dbToBlStatus(tt));
        Timestamp ngayTao = rs.getTimestamp("ngayTao");
        if (ngayTao != null) bl.setNgayTao(ngayTao.toLocalDateTime());
        Timestamp ngayDuyet = rs.getTimestamp("ngayDuyet");
        if (ngayDuyet != null) bl.setNgayDuyet(ngayDuyet.toLocalDateTime());
        Timestamp ngayKhoa = rs.getTimestamp("ngayKhoa");
        if (ngayKhoa != null) bl.setNgayKhoa(ngayKhoa.toLocalDateTime());
        bl.setNguoiTao(readActorDisplay(rs, "nguoiTao", "tenNguoiTao"));
        bl.setNguoiDuyet(readActorDisplay(rs, "nguoiDuyet", "tenNguoiDuyet"));
        bl.setNguoiKhoa(readActorDisplay(rs, "nguoiKhoa", "tenNguoiKhoa"));
        return bl;
    }

    private String baseBangLuongSelect() {
        return "SELECT bl.maBangLuong, bl.thang, bl.nam, bl.tenBangLuong, bl.ngayTao, bl.ngayDuyet, bl.ngayKhoa, "
                + "bl.nguoiTao, bl.nguoiDuyet, bl.nguoiKhoa, bl.trangThai, "
                + "COALESCE(tt_tao.hoTen, tk_tao.tenDangNhap) AS tenNguoiTao, "
                + "COALESCE(tt_duyet.hoTen, tk_duyet.tenDangNhap) AS tenNguoiDuyet, "
                + "COALESCE(tt_khoa.hoTen, tk_khoa.tenDangNhap) AS tenNguoiKhoa "
                + "FROM BANGLUONG bl "
                + "LEFT JOIN TAIKHOAN tk_tao ON bl.nguoiTao = tk_tao.maTaiKhoan "
                + "LEFT JOIN THONGTINCANHAN tt_tao ON tk_tao.maNV = tt_tao.maNV "
                + "LEFT JOIN TAIKHOAN tk_duyet ON bl.nguoiDuyet = tk_duyet.maTaiKhoan "
                + "LEFT JOIN THONGTINCANHAN tt_duyet ON tk_duyet.maNV = tt_duyet.maNV "
                + "LEFT JOIN TAIKHOAN tk_khoa ON bl.nguoiKhoa = tk_khoa.maTaiKhoan "
                + "LEFT JOIN THONGTINCANHAN tt_khoa ON tk_khoa.maNV = tt_khoa.maNV";
    }

    private String readActorDisplay(ResultSet rs, String idColumn, String nameColumn) throws SQLException {
        int actorId = rs.getInt(idColumn);
        if (rs.wasNull()) {
            return null;
        }
        String actorName = rs.getString(nameColumn);
        if (actorName == null || actorName.trim().isEmpty()) {
            return String.valueOf(actorId);
        }
        return actorName + " (#" + actorId + ")";
    }

    private String blStatusToDb(BangLuong.TrangThai tt) {
        if (tt == null) return HRMConstants.TRANG_THAI_DANG_XU_LY;
        switch (tt) {
            case DA_TINH:  return HRMConstants.TRANG_THAI_DANG_XU_LY;
            case DA_DUYET: return HRMConstants.TRANG_THAI_DA_DUYET;
            case DA_KHOA:  return HRMConstants.TRANG_THAI_DA_KHOA;
            default:       return HRMConstants.TRANG_THAI_DANG_XU_LY;
        }
    }

    private BangLuong.TrangThai dbToBlStatus(String db) {
        if (db == null) return BangLuong.TrangThai.NHAP;
        if (HRMConstants.TRANG_THAI_DANG_XU_LY.equals(db)) return BangLuong.TrangThai.DA_TINH;
        if (HRMConstants.TRANG_THAI_DA_DUYET.equals(db))   return BangLuong.TrangThai.DA_DUYET;
        if (HRMConstants.TRANG_THAI_DA_KHOA.equals(db))    return BangLuong.TrangThai.DA_KHOA;
        return BangLuong.TrangThai.NHAP;
    }

    public int approveBangLuong(int id, int nguoiDuyet) {
        String sql = "UPDATE BANGLUONG SET trangThai='" + HRMConstants.TRANG_THAI_DA_DUYET + "', ngayDuyet=NOW(), nguoiDuyet=? "
                + "WHERE maBangLuong=? AND trangThai='" + HRMConstants.TRANG_THAI_DANG_XU_LY + "'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, nguoiDuyet);
            ps.setInt(2, id);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi duyệt bảng lương: " + e.getMessage(), e);
        }
    }

    public List<Object[]> getCauHinhPhuCapRaw() {
        String sql = "SELECT tenKhoan, kieuTinh, giaTri, nguon "
                + "FROM CAUHINH_PHUCAP WHERE hoatDong=TRUE AND loai='phu_cap' AND xepLoaiApDung IS NULL";
        List<Object[]> result = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(new Object[]{
                        rs.getString("tenKhoan"),
                        rs.getString("kieuTinh"),
                        rs.getDouble("giaTri"),
                        rs.getString("nguon")
                });
            }
        } catch (SQLException e) {
            // Return empty list if CAUHINH_PHUCAP not configured
        }
        return result;
    }

    // =====================================================================
    // CHI_TIET_LUONGS
    // =====================================================================
    /** Insert a ChiTietLuong record. Returns generated id (stored in maChiTietLuong). */
    public int insertChiTiet(ChiTietLuong ctl) {
        String sql = "INSERT INTO CHITIETLUONG (maBangLuong, maNV, luongCoSo, tongLuongChucVu, "
                + "luongLamThem, tongThuNhap, tongKhauTru, luongThucLanh, soNgayCong, soGioLamThem, ghiChu) "
                + "VALUES (?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, ctl.getMaBL());
            ps.setString(2, ctl.getMaNV());
            ps.setDouble(3, ctl.getLuongCoBan());
            ps.setDouble(4, ctl.getTongLuongChucVu());
            ps.setDouble(5, ctl.getTienOT());
            ps.setDouble(6, ctl.getTongLuong());
            ps.setDouble(7, ctl.getTongKhauTru());
            ps.setDouble(8, ctl.getLuongThucNhan());
            ps.setDouble(9, ctl.getSoNgayCong());
            ps.setDouble(10, ctl.getTongGioOT());
            ps.setString(11, ctl.getGhiChu());
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
    public List<ChiTietLuong> findByBangLuong(int bangLuongId) {
        String sql = "SELECT cl.maChiTiet, cl.maBangLuong, cl.maNV, cl.luongCoSo, cl.tongLuongChucVu, cl.luongLamThem, cl.tongThuNhap, cl.tongKhauTru, cl.luongThucLanh, cl.soNgayCong, cl.soGioLamThem, cl.ghiChu, t.hoTen FROM CHITIETLUONG cl "
                + "LEFT JOIN THONGTINCANHAN t ON cl.maNV = t.maNV "
                + "WHERE cl.maBangLuong=? ORDER BY cl.maNV";
        List<ChiTietLuong> result = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bangLuongId);
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

    public List<ChiTietLuong> findByBangLuongByScope(int bangLuongId, DataScope scope, String currentMaNV) {
        List<ChiTietLuong> result = new ArrayList<>();
        if (scope == null || scope == DataScope.NONE) {
            return result;
        }
        if (scope == DataScope.ALL) {
            return findByBangLuong(bangLuongId);
        }
        try (Connection conn = DatabaseConnection.getConnection()) {
            ScopeFilter filter = buildPayrollScopeFilter(scope, currentMaNV, conn);
            if (filter == null) {
                return result;
            }
            String sql = "SELECT cl.maChiTiet, cl.maBangLuong, cl.maNV, cl.luongCoSo, cl.tongLuongChucVu, "
                    + "cl.luongLamThem, cl.tongThuNhap, cl.tongKhauTru, cl.luongThucLanh, cl.soNgayCong, "
                    + "cl.soGioLamThem, cl.ghiChu, t.hoTen "
                    + "FROM CHITIETLUONG cl "
                    + "LEFT JOIN THONGTINCANHAN t ON cl.maNV = t.maNV "
                    + "LEFT JOIN BONHIEM b ON cl.maNV = b.maNV "
                    + "AND b.trangThai = '" + HRMConstants.TRANG_THAI_HIEU_LUC + "' "
                    + "AND b.loaiBoNhiem = '" + HRMConstants.LOAI_BO_NHIEM_CHINH + "' "
                    + "WHERE cl.maBangLuong=? " + filter.whereClause + " ORDER BY cl.maNV";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                int index = 1;
                ps.setInt(index++, bangLuongId);
                bindScopeParams(ps, filter.params, index);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        ChiTietLuong ctl = mapChiTietLuong(rs);
                        ctl.setDanhSachThanhPhan(findByChiTiet(ctl.getMaChiTietLuong()));
                        result.add(ctl);
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tải chi tiết lương theo scope: " + e.getMessage(), e);
        }
        return result;
    }

    public ChiTietLuong findByBangLuongAndNV(int bangLuongId, String nhanVienId) {
        String sql = "SELECT cl.maChiTiet, cl.maBangLuong, cl.maNV, cl.luongCoSo, cl.tongLuongChucVu, cl.luongLamThem, cl.tongThuNhap, cl.tongKhauTru, cl.luongThucLanh, cl.soNgayCong, cl.soGioLamThem, cl.ghiChu, t.hoTen FROM CHITIETLUONG cl "
                + "LEFT JOIN THONGTINCANHAN t ON cl.maNV = t.maNV "
                + "WHERE cl.maBangLuong=? AND cl.maNV=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bangLuongId);
            ps.setString(2, nhanVienId);
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

    public ChiTietLuong findByBangLuongAndNVByScope(int bangLuongId, String nhanVienId,
                                                    DataScope scope, String currentMaNV) {
        if (scope == null || scope == DataScope.NONE) {
            return null;
        }
        if (scope == DataScope.ALL) {
            return findByBangLuongAndNV(bangLuongId, nhanVienId);
        }
        try (Connection conn = DatabaseConnection.getConnection()) {
            ScopeFilter filter = buildPayrollScopeFilter(scope, currentMaNV, conn);
            if (filter == null) {
                return null;
            }
            String sql = "SELECT cl.maChiTiet, cl.maBangLuong, cl.maNV, cl.luongCoSo, cl.tongLuongChucVu, "
                    + "cl.luongLamThem, cl.tongThuNhap, cl.tongKhauTru, cl.luongThucLanh, cl.soNgayCong, "
                    + "cl.soGioLamThem, cl.ghiChu, t.hoTen "
                    + "FROM CHITIETLUONG cl "
                    + "LEFT JOIN THONGTINCANHAN t ON cl.maNV = t.maNV "
                    + "LEFT JOIN BONHIEM b ON cl.maNV = b.maNV "
                    + "AND b.trangThai = '" + HRMConstants.TRANG_THAI_HIEU_LUC + "' "
                    + "AND b.loaiBoNhiem = '" + HRMConstants.LOAI_BO_NHIEM_CHINH + "' "
                    + "WHERE cl.maBangLuong=? AND cl.maNV=? " + filter.whereClause;
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                int index = 1;
                ps.setInt(index++, bangLuongId);
                ps.setString(index++, nhanVienId);
                bindScopeParams(ps, filter.params, index);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        ChiTietLuong ctl = mapChiTietLuong(rs);
                        ctl.setDanhSachThanhPhan(findByChiTiet(ctl.getMaChiTietLuong()));
                        return ctl;
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tải chi tiết lương NV theo scope: " + e.getMessage(), e);
        }
        return null;
    }

    /** Lấy mã phòng ban hiện tại của nhân viên từ bổ nhiệm chính còn hiệu lực. */
    public String getPhongBanCuaNV(String nhanVienId) {
        String sql = "SELECT b.maPhongBan FROM BONHIEM b "
                + "WHERE b.maNV=? AND b.trangThai='" + HRMConstants.TRANG_THAI_HIEU_LUC + "' AND b.loaiBoNhiem='" + HRMConstants.LOAI_BO_NHIEM_CHINH + "' "
                + "ORDER BY b.tuNgay DESC LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nhanVienId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("maPhongBan");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tải phòng ban của nhân viên: " + e.getMessage(), e);
        }
        return null;
    }

    public int deleteChiTietByBangLuong(int bangLuongId) {
        String sql = "DELETE FROM CHITIETLUONG WHERE maBangLuong=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bangLuongId);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi xóa chi tiết bảng lương: " + e.getMessage(), e);
        }
    }

    public int deleteChiTietByBangLuongAndNV(int bangLuongId, String nhanVienId) {
        String sql = "DELETE FROM CHITIETLUONG WHERE maBangLuong=? AND maNV=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bangLuongId);
            ps.setString(2, nhanVienId);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi xóa chi tiết lương nhân viên: " + e.getMessage(), e);
        }
    }

    private ChiTietLuong mapChiTietLuong(ResultSet rs) throws SQLException {
        ChiTietLuong ctl = new ChiTietLuong();
        ctl.setMaChiTietLuong(rs.getInt("maChiTiet"));
        ctl.setMaBL(rs.getInt("maBangLuong"));
        ctl.setMaNV(rs.getString("maNV"));
        ctl.setLuongCoBan(rs.getDouble("luongCoSo"));
        ctl.setTongLuongChucVu(rs.getDouble("tongLuongChucVu"));
        ctl.setTienOT(rs.getDouble("luongLamThem"));
        ctl.setTongLuong(rs.getDouble("tongThuNhap"));
        ctl.setTongKhauTru(rs.getDouble("tongKhauTru"));
        ctl.setLuongThucNhan(rs.getDouble("luongThucLanh"));
        ctl.setSoNgayCong(rs.getDouble("soNgayCong"));
        ctl.setTongGioOT(rs.getDouble("soGioLamThem"));
        ctl.setGhiChu(rs.getString("ghiChu"));
        // transient
        try { ctl.setTenNV(rs.getString("hoTen")); } catch (SQLException ignored) {}
        return ctl;
    }

    // =====================================================================
    // THANH_PHAN_LUONGS
    // =====================================================================
    /** Batch insert list of ThanhPhanLuong. */
    public int insertThanhPhanBatch(List<ThanhPhanLuong> list) {
        if (list == null || list.isEmpty()) return 0;
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
            int[] counts = ps.executeBatch();
            int total = 0;
            for (int c : counts) total += (c >= 0 ? c : 0);
            return total;
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi thêm thành phần lương: " + e.getMessage(), e);
        }
    }

    public List<ThanhPhanLuong> findByChiTiet(int chiTietId) {
        String sql = "SELECT maThanhPhan, maChiTiet, tenThanhPhan, loai, soTien, ghiChu "
                + "FROM THANHPHANLUONG WHERE maChiTiet=? ORDER BY loai, maThanhPhan";
        List<ThanhPhanLuong> result = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, chiTietId);
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

    private ScopeFilter buildPayrollScopeFilter(DataScope scope, String currentMaNV, Connection conn) throws SQLException {
        if (scope == null || scope == DataScope.NONE) {
            return null;
        }
        if (scope == DataScope.ALL) {
            return new ScopeFilter("", Collections.emptyList());
        }
        if (scope == DataScope.SELF) {
            return new ScopeFilter("AND cl.maNV = ?", Collections.singletonList(currentMaNV));
        }
        if (scope == DataScope.TEAM) {
            List<String> params = new ArrayList<>();
            params.add(currentMaNV);
            params.add(currentMaNV);
            return new ScopeFilter("AND (b.maQuanLy = ? OR cl.maNV = ?)", params);
        }
        if (scope == DataScope.DEPT) {
            java.util.Set<String> deptIds = DaoHelper.getDeptSubtree(currentMaNV, conn);
            if (deptIds.isEmpty()) {
                return null;
            }
            String placeholders = String.join(",", Collections.nCopies(deptIds.size(), "?"));
            return new ScopeFilter("AND b.maPhongBan IN (" + placeholders + ")", new ArrayList<>(deptIds));
        }
        return null;
    }

    private void bindScopeParams(PreparedStatement ps, List<String> params) throws SQLException {
        bindScopeParams(ps, params, 1);
    }

    private void bindScopeParams(PreparedStatement ps, List<String> params, int startIndex) throws SQLException {
        int index = startIndex;
        for (String param : params) {
            ps.setString(index++, param);
        }
    }

    private static class ScopeFilter {
        private final String whereClause;
        private final List<String> params;

        private ScopeFilter(String whereClause, List<String> params) {
            this.whereClause = whereClause;
            this.params = params;
        }
    }

    // =====================================================================
    // Helpers for salary calculation
    // =====================================================================
    /**
     * Returns luongCoSo from the labour contract that was effective during the given pay period.
     * Checks both trangThai and actual date range so expired-but-not-yet-updated contracts
     * are not mistakenly picked up, and future contracts are excluded.
     *
     * @param nhanVienId  employee ID
     * @param ngayBatDauKy  first day of the pay period
     * @param ngayCuoiKy    last day of the pay period
     */
    public double getLuongCoSoFromHopDong(String nhanVienId, java.time.LocalDate ngayBatDauKy, java.time.LocalDate ngayCuoiKy) {
        String sql = "SELECT luongCoSo FROM HOPDONGLAODONG "
                + "WHERE maNV=? "
                + "  AND trangThai IN ('" + HRMConstants.TRANG_THAI_HIEU_LUC + "','" + HRMConstants.TRANG_THAI_HET_HAN + "') "
                + "  AND ngayHieuLuc <= ? "
                + "  AND (ngayHetHieuLuc IS NULL OR ngayHetHieuLuc >= ?) "
                + "ORDER BY ngayHieuLuc DESC LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nhanVienId);
            ps.setDate(2, java.sql.Date.valueOf(ngayCuoiKy));
            ps.setDate(3, java.sql.Date.valueOf(ngayBatDauKy));
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
     * luongChucVu = SUM( tyLeHuongLuong/100 * phuCapChucVu )
     */
    public double getTongLuongChucVu(String nhanVienId, double luongCoSo) {
        String sql = "SELECT b.tyLeHuongLuong, cv.phuCapChucVu "
                + "FROM BONHIEM b "
                + "JOIN CHUCVU cv ON b.maChucVu = cv.maChucVu "
                + "WHERE b.maNV=? AND b.trangThai='" + HRMConstants.TRANG_THAI_HIEU_LUC + "'";
        double total = 0;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nhanVienId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    double tyLe = rs.getDouble("tyLeHuongLuong");
                    double phuCap = rs.getDouble("phuCapChucVu");
                    // Theo logic mới: lương chức vụ thực chất chỉ là phụ cấp chức vụ
                    // Có thể tính theo tỷ lệ bổ nhiệm (nếu kiêm nhiệm chỉ được hưởng % phụ cấp)
                    total += (tyLe / 100.0) * phuCap;
                }
            }
        } catch (SQLException e) {
            // Return 0 on error
        }
        return total;
    }

    /** Tính số ngày công thực tế từ CHAMCONG trong tháng/năm theo giờ làm thực tế.
     *  nghi_phep/cong_tac luôn = 1 ngày; còn lại = soGioLam / soGioChuan (ca làm, fallback 8h). */
    public double getSoNgayCong(String nhanVienId, int thang, int nam) {
        String sql = "SELECT COALESCE(SUM("
                + "CASE WHEN c.trangThai IN ('nghi_phep','cong_tac') THEN 1.0 "
                + "ELSE c.soGioLam / COALESCE(cl.soGioChuan, 8.0) END"
                + "), 0) "
                + "FROM CHAMCONG c "
                + "LEFT JOIN CALAM cl ON c.maCaLam = cl.maCaLam "
                + "WHERE c.maNV=? AND MONTH(c.ngay)=? AND YEAR(c.ngay)=? "
                + "AND c.trangThai IN ('dung_gio','di_muon','ve_som','nghi_phep','cong_tac')";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nhanVienId);
            ps.setInt(2, thang);
            ps.setInt(3, nam);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble(1);
            }
        } catch (SQLException e) {
            // Default 0 on query error so caller can apply fallback policy.
        }
        return 0.0;
    }

    /** Tổng số bản ghi chấm công trong tháng để phân biệt chưa có dữ liệu với vắng mặt toàn bộ. */
    public int getTongBanGhiChamCong(String nhanVienId, int thang, int nam) {
        String sql = "SELECT COUNT(*) FROM CHAMCONG WHERE maNV=? AND MONTH(ngay)=? AND YEAR(ngay)=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nhanVienId);
            ps.setInt(2, thang);
            ps.setInt(3, nam);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            // Default 0 on query error so caller can apply fallback policy.
        }
        return 0;
    }

    /** Tổng tiền OT = giờ OT quy đổi * đơn giá giờ công chuẩn.
     *  @param ngayLamViecTrongThang số ngày làm việc thực tế của tháng (T2=20, T1=21, ...) */
    public double getTienOT(String nhanVienId, int thang, int nam, double luongCoBan, int ngayLamViecTrongThang) {
        String sql = "SELECT COALESCE(SUM(soGio * heSoOT), 0) FROM DANGKY_LAMTHEM "
                + "WHERE maNV=? AND MONTH(ngay)=? AND YEAR(ngay)=? AND trangThai='" + HRMConstants.TRANG_THAI_DA_DUYET + "'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nhanVienId);
            ps.setInt(2, thang);
            ps.setInt(3, nam);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    double tongGioOTQuyDoi = rs.getDouble(1);
                    return tongGioOTQuyDoi * (luongCoBan / ngayLamViecTrongThang / 8.0);
                }
            }
        } catch (SQLException e) {
            // Default 0 on query error.
        }
        return 0.0;
    }

    /** Tổng số giờ làm thực tế từ CHAMCONG trong tháng/năm. */
    public double getTongGioLam(String nhanVienId, int thang, int nam) {
        String sql = "SELECT COALESCE(SUM(soGioLam),0) FROM CHAMCONG "
                + "WHERE maNV=? AND MONTH(ngay)=? AND YEAR(ngay)=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nhanVienId);
            ps.setInt(2, thang);
            ps.setInt(3, nam);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble(1);
            }
        } catch (SQLException e) {
            // Default 0 on query error.
        }
        return 0.0;
    }

    /** % thưởng hiệu suất ứng với xepLoai, lấy từ CAUHINH_PHUCAP. Trả về 0 nếu không tìm thấy. */
    public double getThuongHieuSuatRate(String xepLoai) {
        if (xepLoai == null) return 0.0;
        String sql = "SELECT giaTri FROM CAUHINH_PHUCAP "
                + "WHERE xepLoaiApDung=? AND hoatDong=TRUE AND loai='phu_cap' LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, xepLoai);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble("giaTri");
            }
        } catch (SQLException e) {
            // Return 0 on error
        }
        return 0.0;
    }

    /** Xếp loại đánh giá hiệu suất đã xác nhận gần nhất của nhân viên. */
    public String getXepLoaiMoiNhat(String maNV) {
        String sql = "SELECT xepLoai FROM DANHGIAHIEUSUAT "
                + "WHERE maNV=? AND trangThai='da_xac_nhan' "
                + "ORDER BY ngayDanhGia DESC LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maNV);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("xepLoai");
            }
        } catch (SQLException e) {
            // Return null if no evaluation found
        }
        return null;
    }

    /** Tổng giờ OT thô (để lưu vào soGioLamThem và hiển thị chi tiết). */
    public double getTongGioOT(String nhanVienId, int thang, int nam) {
        String sql = "SELECT COALESCE(SUM(soGio),0) FROM DANGKY_LAMTHEM "
                + "WHERE maNV=? AND MONTH(ngay)=? AND YEAR(ngay)=? AND trangThai='" + HRMConstants.TRANG_THAI_DA_DUYET + "'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nhanVienId);
            ps.setInt(2, thang);
            ps.setInt(3, nam);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble(1);
            }
        } catch (SQLException e) {
            // Default 0 on query error.
        }
        return 0.0;
    }
}
