package com.hrm.service;

import com.hrm.model.NhanVien;
import com.hrm.model.ThongTinCaNhan;
import com.hrm.repo.BoNhiemRepository;
import com.hrm.repo.NhanVienRepository;
import com.hrm.repo.TaiKhoanRepository;
import com.hrm.repo.ThongTinCaNhanRepository;
import com.hrm.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Service quản lý nhân viên.
 * Singleton pattern.
 */
public class NhanVienService {

    private static NhanVienService instance;

    private final NhanVienRepository nvRepo = NhanVienRepository.getInstance();
    private final ThongTinCaNhanRepository ttcnRepo = ThongTinCaNhanRepository.getInstance();
    private final BoNhiemRepository boNhiemRepo = BoNhiemRepository.getInstance();
    private final TaiKhoanRepository taiKhoanRepo = new TaiKhoanRepository();

    private NhanVienService() {
    }

    public static synchronized NhanVienService getInstance() {
        if (instance == null) {
            instance = new NhanVienService();
        }
        return instance;
    }

    // ============================
    // taoHoSo - tạo nhân viên mới + thông tin cá nhân trong transaction
    // ============================

    public ServiceResult<NhanVien> taoHoSo(NhanVien nv, ThongTinCaNhan ttcn) {
        // Validate mã nhân viên
        if (nv.getMaNhanVien() == null || nv.getMaNhanVien().trim().isEmpty()) {
            return ServiceResult.error("Mã nhân viên không được để trống.");
        }
        if (nvRepo.existsByMaNhanVien(nv.getMaNhanVien().trim())) {
            return ServiceResult.error("Mã nhân viên '" + nv.getMaNhanVien() + "' đã tồn tại.");
        }

        // Validate họ tên
        if (ttcn.getHoTen() == null || ttcn.getHoTen().trim().isEmpty()) {
            return ServiceResult.error("Họ tên không được để trống.");
        }

        // Validate CCCD format (12 chữ số)
        if (ttcn.getCccd() != null && !ttcn.getCccd().trim().isEmpty()) {
            if (!ttcn.getCccd().trim().matches("\\d{12}")) {
                return ServiceResult.error("CCCD phải là 12 chữ số.");
            }
            if (ttcnRepo.existsByCCCD(ttcn.getCccd().trim(), 0)) {
                return ServiceResult.error("CCCD '" + ttcn.getCccd() + "' đã được đăng ký cho nhân viên khác.");
            }
        }

        // Validate ngày vào làm
        if (nv.getNgayVaoLam() == null) {
            return ServiceResult.error("Ngày vào làm không được để trống.");
        }

        // Đặt trạng thái mặc định
        if (nv.getTrangThai() == null || nv.getTrangThai().isEmpty()) {
            nv.setTrangThai("dang_lam_viec");
        }

        // Transaction: insert NhanVien + ThongTinCaNhan + SoDungPhep
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Insert NhanVien
                int maNV = nvRepo.insert(conn, nv);
                ttcn.setMaNV(maNV);

                // Insert ThongTinCaNhan
                ttcnRepo.insert(conn, ttcn);

                // Tạo SoDungPhep cho năm hiện tại - phép năm 12 ngày
                int namHienTai = LocalDate.now().getYear();
                insertSoDungPhep(conn, maNV, namHienTai, "PHEP_NAM", 12.0);

                conn.commit();
                nv.setHoTen(ttcn.getHoTen());
                return ServiceResult.success(nv, "Tạo hồ sơ nhân viên thành công.");
            } catch (Exception ex) {
                conn.rollback();
                return ServiceResult.error("Lỗi khi tạo hồ sơ: " + ex.getMessage());
            }
        } catch (SQLException e) {
            return ServiceResult.error("Lỗi kết nối cơ sở dữ liệu: " + e.getMessage());
        }
    }

    // ============================
    // capNhatThongTinCaNhan
    // ============================

    public ServiceResult<ThongTinCaNhan> capNhatThongTinCaNhan(ThongTinCaNhan ttcn) {
        // Validate CCCD
        if (ttcn.getCccd() != null && !ttcn.getCccd().trim().isEmpty()) {
            if (!ttcn.getCccd().trim().matches("\\d{12}")) {
                return ServiceResult.error("CCCD phải là 12 chữ số.");
            }
            if (ttcnRepo.existsByCCCD(ttcn.getCccd().trim(), ttcn.getMaNV())) {
                return ServiceResult.error("CCCD '" + ttcn.getCccd() + "' đã được đăng ký cho nhân viên khác.");
            }
        }

        // Validate email
        if (ttcn.getEmail() != null && !ttcn.getEmail().trim().isEmpty()) {
            if (!ttcn.getEmail().trim().matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
                return ServiceResult.error("Địa chỉ email không hợp lệ.");
            }
        }

        // Validate số điện thoại (10-11 chữ số)
        if (ttcn.getDienThoai() != null && !ttcn.getDienThoai().trim().isEmpty()) {
            if (!ttcn.getDienThoai().trim().matches("0\\d{9,10}")) {
                return ServiceResult.error("Số điện thoại không hợp lệ (phải bắt đầu bằng 0 và có 10-11 chữ số).");
            }
        }

        try {
            ttcnRepo.update(ttcn);
            return ServiceResult.success(ttcn, "Cập nhật thông tin cá nhân thành công.");
        } catch (Exception e) {
            return ServiceResult.error("Lỗi cập nhật: " + e.getMessage());
        }
    }

    // ============================
    // capNhatTrangThai
    // ============================

    public ServiceResult<NhanVien> capNhatTrangThai(int maNV, String trangThaiMoi, String lyDo) {
        NhanVien nv = nvRepo.findById(maNV);
        if (nv == null) {
            return ServiceResult.error("Không tìm thấy nhân viên.");
        }

        String trangThaiHienTai = nv.getTrangThai();

        // Kiểm tra transition hợp lệ
        boolean valid = false;
        if ("dang_lam_viec".equals(trangThaiHienTai) && "tam_nghi".equals(trangThaiMoi)) valid = true;
        if ("dang_lam_viec".equals(trangThaiHienTai) && "nghi_viec".equals(trangThaiMoi)) valid = true;
        if ("tam_nghi".equals(trangThaiHienTai) && "dang_lam_viec".equals(trangThaiMoi)) valid = true;

        if (!valid) {
            if ("nghi_viec".equals(trangThaiHienTai)) {
                return ServiceResult.error("Nhân viên đã nghỉ việc, không thể thay đổi trạng thái.");
            }
            return ServiceResult.error("Không thể chuyển từ trạng thái '"
                    + nv.getTrangThaiDisplay() + "' sang '" + trangThaiMoi + "'.");
        }

        nv.setTrangThai(trangThaiMoi);
        nvRepo.update(nv);

        // Nếu nghỉ việc: kết thúc tất cả bổ nhiệm đang hiệu lực + vô hiệu hóa tài khoản
        if ("nghi_viec".equals(trangThaiMoi)) {
            boNhiemRepo.endAllActiveBoNhiemForNV(maNV, LocalDate.now());
            taiKhoanRepo.deactivateByMaNV(maNV);
        }

        return ServiceResult.success(nv, "Cập nhật trạng thái nhân viên thành công.");
    }

    // ============================
    // Getters
    // ============================

    public List<NhanVien> getAll() {
        return nvRepo.findAll();
    }

    public List<NhanVien> getDangLamViec() {
        return nvRepo.findDangLamViec();
    }

    public NhanVien getById(int id) {
        return nvRepo.findById(id);
    }

    public NhanVien getByMaNhanVien(String maNhanVien) {
        return nvRepo.findByMaNhanVien(maNhanVien);
    }

    public ThongTinCaNhan getThongTinCaNhan(int maNV) {
        return ttcnRepo.findByMaNV(maNV);
    }

    public String generateMaNhanVien() {
        return nvRepo.generateMaNhanVien();
    }

    // ============================
    // Private helper
    // ============================

    private void insertSoDungPhep(Connection conn, int maNV, int nam, String maLoaiPhep,
                                   double soNgayDuocCap) throws SQLException {
        String sql = "INSERT INTO SODUNGPHEP (maNV, nam, maLoaiPhep, soNgayDuocCap, soNgayDaDung) "
                + "VALUES (?, ?, ?, ?, 0) "
                + "ON DUPLICATE KEY UPDATE soNgayDuocCap=VALUES(soNgayDuocCap)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maNV);
            ps.setInt(2, nam);
            ps.setString(3, maLoaiPhep);
            ps.setDouble(4, soNgayDuocCap);
            ps.executeUpdate();
        }
    }
}
