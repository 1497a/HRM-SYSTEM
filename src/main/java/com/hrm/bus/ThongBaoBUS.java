package com.hrm.bus;

import com.hrm.model.ThongBao;
import com.hrm.dao.ThongBaoDAO;
import com.hrm.dao.TaiKhoanDAO;
import com.hrm.model.TaiKhoan;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Service cho module Thông báo.
 * Singleton pattern.
 */
public class ThongBaoBUS {

    private static ThongBaoBUS instance;
    private final ThongBaoDAO thongBaoRepo;
    private final TaiKhoanDAO taiKhoanRepo;

    private ThongBaoBUS() {
        this.thongBaoRepo = ThongBaoDAO.getInstance();
        this.taiKhoanRepo = new TaiKhoanDAO();
    }

    public static synchronized ThongBaoBUS getInstance() {
        if (instance == null) {
            instance = new ThongBaoBUS();
        }
        return instance;
    }

    /**
     * Gửi thông báo hệ thống (không có người gửi cụ thể).
     */
    public void guiThongBaoHeThong(int maTaiKhoanNhan, String tieuDe, String noiDung) {
        ThongBao tb = new ThongBao();
        tb.setMaTaiKhoanGui(0); // hệ thống
        tb.setMaTaiKhoanNhan(maTaiKhoanNhan);
        tb.setTieuDe(tieuDe);
        tb.setNoiDung(noiDung);
        tb.setLoaiThongBao("he_thong");
        thongBaoRepo.insert(tb);
    }

    /**
     * Gửi thông báo đến nhân viên qua mã nhân viên (maNV).
     * Tìm tài khoản liên kết với maNV rồi gửi.
     */
    public void guiThongBaoChoMaNV(int maNV, String tieuDe, String noiDung) {
        // Tìm tài khoản theo maNV
        TaiKhoan user = findTaiKhoanByMaNV(maNV);
        if (user == null) {
            System.err.println("ThongBaoBUS: Không tìm thấy tài khoản cho maNV=" + maNV);
            return;
        }
        guiThongBaoHeThong(user.getId(), tieuDe, noiDung);
    }

    /**
     * Gửi thông báo từ một người dùng đến người dùng khác.
     */
    public void guiThongBao(int maTaiKhoanGui, int maTaiKhoanNhan, String tieuDe, String noiDung) {
        ThongBao tb = new ThongBao();
        tb.setMaTaiKhoanGui(maTaiKhoanGui);
        tb.setMaTaiKhoanNhan(maTaiKhoanNhan);
        tb.setTieuDe(tieuDe);
        tb.setNoiDung(noiDung);
        tb.setLoaiThongBao("thong_bao_chung");
        thongBaoRepo.insert(tb);
    }

    /**
     * Gửi thông báo cá nhân từ người dùng đến một nhân viên cụ thể.
     */
    public void guiThongBaoCaNhan(int nguoiGui, int maNVNhan, String tieuDe, String noiDung) {
        TaiKhoan nguoiNhan = findUserByMaNVDirect(maNVNhan);
        if (nguoiNhan == null) {
            System.err.println("ThongBaoBUS: Khong tim thay tai khoan cho maNV=" + maNVNhan);
            return;
        }
        ThongBao tb = buildThongBao(nguoiGui, nguoiNhan.getId(), tieuDe, noiDung, "thong_bao_chung");
        thongBaoRepo.insert(tb);
    }

    /**
     * Gửi thông báo tới tất cả nhân viên thuộc một phòng ban.
     */
    public void guiThongBaoPhongBan(int nguoiGui, String maPhongBan, String tieuDe, String noiDung) {
        List<Integer> maTaiKhoanList = findTaiKhoanByPhongBan(maPhongBan);
        sendBulk(nguoiGui, maTaiKhoanList, tieuDe, noiDung);
    }

    /**
     * Gửi thông báo tới tất cả nhân viên có chức vụ chỉ định.
     */
    public void guiThongBaoChucVu(int nguoiGui, String maChucVu, String tieuDe, String noiDung) {
        List<Integer> maTaiKhoanList = findTaiKhoanByChucVu(maChucVu);
        sendBulk(nguoiGui, maTaiKhoanList, tieuDe, noiDung);
    }

    /**
     * Gửi thông báo tới tất cả tài khoản đang hoạt động.
     */
    public void guiThongBaoTatCa(int nguoiGui, String tieuDe, String noiDung) {
        List<Integer> maTaiKhoanList = findAllActiveTaiKhoan();
        sendBulk(nguoiGui, maTaiKhoanList, tieuDe, noiDung);
    }

    /**
     * Đánh dấu một thông báo là đã đọc.
     */
    public KetQua<Void> danhDauDaDoc(int maThongBao) {
        try {
            thongBaoRepo.markAsRead(maThongBao);
            return KetQua.success(null, "Đã đánh dấu là đã đọc.");
        } catch (Exception e) {
            System.err.println("Lỗi danhDauDaDoc: " + e.getMessage());
            return KetQua.error("Không thể đánh dấu thông báo: " + e.getMessage());
        }
    }

    /**
     * Đánh dấu tất cả thông báo của người nhận là đã đọc.
     */
    public KetQua<Void> danhDauTatCaDaDoc(int maTaiKhoanNhan) {
        try {
            thongBaoRepo.markAllAsRead(maTaiKhoanNhan);
            return KetQua.success(null, "Đã đánh dấu tất cả là đã đọc.");
        } catch (Exception e) {
            System.err.println("Lỗi danhDauTatCaDaDoc: " + e.getMessage());
            return KetQua.error("Không thể cập nhật thông báo: " + e.getMessage());
        }
    }

    /**
     * Lấy tất cả thông báo của người dùng hiện tại.
     */
    public List<ThongBao> getThongBaoCuaToi(int maTaiKhoanNhan) {
        return thongBaoRepo.findByNguoiNhan(maTaiKhoanNhan);
    }

    /**
     * Đếm số thông báo chưa đọc.
     */
    public int demChuaDoc(int maTaiKhoanNhan) {
        return thongBaoRepo.countUnread(maTaiKhoanNhan);
    }

    // ============================
    // Private helpers
    // ============================

    private TaiKhoan findTaiKhoanByMaNV(int maNV) {
        // Tìm tài khoản có maNV tương ứng
        try {
            java.util.List<TaiKhoan> all = taiKhoanRepo.findAll();
            for (TaiKhoan u : all) {
                // TaiKhoan.getId() = maTaiKhoan, không phải maNV
                // Dùng truy vấn trực tiếp thông qua TaiKhoanDAO
            }
        } catch (Exception e) {
            System.err.println("Lỗi findTaiKhoanByMaNV: " + e.getMessage());
        }
        return findUserByMaNVDirect(maNV);
    }

    private TaiKhoan findUserByMaNVDirect(int maNV) {
        String sql = "SELECT tk.maTaiKhoan FROM TAIKHOAN tk WHERE tk.maNV = ?";
        try (Connection conn = com.hrm.util.DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maNV);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int maTaiKhoan = rs.getInt("maTaiKhoan");
                    return taiKhoanRepo.findById(maTaiKhoan);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi findUserByMaNVDirect: " + e.getMessage());
        }
        return null;
    }

    private ThongBao buildThongBao(int nguoiGui, int nguoiNhan, String tieuDe, String noiDung, String loai) {
        ThongBao tb = new ThongBao();
        tb.setMaTaiKhoanGui(nguoiGui);
        tb.setMaTaiKhoanNhan(nguoiNhan);
        tb.setTieuDe(tieuDe);
        tb.setNoiDung(noiDung);
        tb.setLoaiThongBao(loai);
        return tb;
    }

    private void sendBulk(int nguoiGui, List<Integer> maTaiKhoanList, String tieuDe, String noiDung) {
        if (maTaiKhoanList.isEmpty()) return;
        List<ThongBao> batch = new ArrayList<>();
        for (int maTK : maTaiKhoanList) {
            batch.add(buildThongBao(nguoiGui, maTK, tieuDe, noiDung, "thong_bao_chung"));
        }
        thongBaoRepo.insertBulk(batch);
    }

    private List<Integer> findTaiKhoanByPhongBan(String maPhongBan) {
        List<Integer> list = new ArrayList<>();
        String sql = "SELECT DISTINCT tk.maTaiKhoan FROM TAIKHOAN tk "
                + "JOIN NHANVIEN nv ON tk.maNV = nv.maNV "
                + "JOIN BONHIEM bn ON nv.maNV = bn.maNV "
                + "WHERE bn.maPhongBan = ? AND bn.trangThai = 'hieu_luc' "
                + "AND nv.trangThai = 'dang_lam_viec' AND tk.hoatDong = TRUE";
        try (Connection conn = com.hrm.util.DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maPhongBan);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(rs.getInt(1));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi findTaiKhoanByPhongBan: " + e.getMessage());
        }
        return list;
    }

    private List<Integer> findTaiKhoanByChucVu(String maChucVu) {
        List<Integer> list = new ArrayList<>();
        String sql = "SELECT DISTINCT tk.maTaiKhoan FROM TAIKHOAN tk "
                + "JOIN NHANVIEN nv ON tk.maNV = nv.maNV "
                + "JOIN BONHIEM bn ON nv.maNV = bn.maNV "
                + "WHERE bn.maChucVu = ? AND bn.trangThai = 'hieu_luc' "
                + "AND nv.trangThai = 'dang_lam_viec' AND tk.hoatDong = TRUE";
        try (Connection conn = com.hrm.util.DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maChucVu);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(rs.getInt(1));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi findTaiKhoanByChucVu: " + e.getMessage());
        }
        return list;
    }

    private List<Integer> findAllActiveTaiKhoan() {
        List<Integer> list = new ArrayList<>();
        String sql = "SELECT maTaiKhoan FROM TAIKHOAN WHERE hoatDong = TRUE AND biKhoa = FALSE";
        try (Connection conn = com.hrm.util.DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(rs.getInt(1));
        } catch (SQLException e) {
            System.err.println("Lỗi findAllActiveTaiKhoan: " + e.getMessage());
        }
        return list;
    }
}
