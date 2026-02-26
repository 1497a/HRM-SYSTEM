package com.hrm.service;

import com.hrm.model.ThongBao;
import com.hrm.repo.ThongBaoRepository;
import com.hrm.repo.TaiKhoanRepository;
import com.hrm.model.User;

import java.util.List;

/**
 * Service cho module Thông báo.
 * Singleton pattern.
 */
public class ThongBaoService {

    private static ThongBaoService instance;
    private final ThongBaoRepository thongBaoRepo;
    private final TaiKhoanRepository taiKhoanRepo;

    private ThongBaoService() {
        this.thongBaoRepo = ThongBaoRepository.getInstance();
        this.taiKhoanRepo = new TaiKhoanRepository();
    }

    public static synchronized ThongBaoService getInstance() {
        if (instance == null) {
            instance = new ThongBaoService();
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
        User user = findTaiKhoanByMaNV(maNV);
        if (user == null) {
            System.err.println("ThongBaoService: Không tìm thấy tài khoản cho maNV=" + maNV);
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
     * Đánh dấu một thông báo là đã đọc.
     */
    public ServiceResult<Void> danhDauDaDoc(int maThongBao) {
        try {
            thongBaoRepo.markAsRead(maThongBao);
            return ServiceResult.success(null, "Đã đánh dấu là đã đọc.");
        } catch (Exception e) {
            System.err.println("Lỗi danhDauDaDoc: " + e.getMessage());
            return ServiceResult.error("Không thể đánh dấu thông báo: " + e.getMessage());
        }
    }

    /**
     * Đánh dấu tất cả thông báo của người nhận là đã đọc.
     */
    public ServiceResult<Void> danhDauTatCaDaDoc(int maTaiKhoanNhan) {
        try {
            thongBaoRepo.markAllAsRead(maTaiKhoanNhan);
            return ServiceResult.success(null, "Đã đánh dấu tất cả là đã đọc.");
        } catch (Exception e) {
            System.err.println("Lỗi danhDauTatCaDaDoc: " + e.getMessage());
            return ServiceResult.error("Không thể cập nhật thông báo: " + e.getMessage());
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

    private User findTaiKhoanByMaNV(int maNV) {
        // Tìm tài khoản có maNV tương ứng
        try {
            java.util.List<User> all = taiKhoanRepo.findAll();
            for (User u : all) {
                // User.getId() = maTaiKhoan, không phải maNV
                // Dùng truy vấn trực tiếp thông qua TaiKhoanRepository
            }
        } catch (Exception e) {
            System.err.println("Lỗi findTaiKhoanByMaNV: " + e.getMessage());
        }
        return findUserByMaNVDirect(maNV);
    }

    private User findUserByMaNVDirect(int maNV) {
        String sql = "SELECT tk.maTaiKhoan FROM TAIKHOAN tk WHERE tk.maNV = ?";
        try (java.sql.Connection conn = com.hrm.util.DatabaseConnection.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maNV);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int maTaiKhoan = rs.getInt("maTaiKhoan");
                    return taiKhoanRepo.findById(maTaiKhoan);
                }
            }
        } catch (java.sql.SQLException e) {
            System.err.println("Lỗi findUserByMaNVDirect: " + e.getMessage());
        }
        return null;
    }
}
