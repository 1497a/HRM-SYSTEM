package com.hrm.bus;

import com.hrm.model.NhanVien;
import com.hrm.model.ThongTinCaNhan;
import com.hrm.dao.BoNhiemDAO;
import com.hrm.dao.NhanVienDAO;
import com.hrm.dao.TaiKhoanDAO;
import com.hrm.dao.ThongTinCaNhanDAO;
import com.hrm.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Service quản lý nhân viên.
 * Singleton pattern.
 * Đã cập nhật để maNV là String (dạng "NV001", "NV002", ...).
 */
public class NhanVienBUS {

    private static NhanVienBUS instance;

    private final NhanVienDAO nvRepo = NhanVienDAO.getInstance();
    private final ThongTinCaNhanDAO ttcnRepo = ThongTinCaNhanDAO.getInstance();
    private final BoNhiemDAO boNhiemRepo = BoNhiemDAO.getInstance();
    private final TaiKhoanDAO taiKhoanRepo = new TaiKhoanDAO();

    private NhanVienBUS() {
    }

    public static synchronized NhanVienBUS getInstance() {
        if (instance == null) {
            instance = new NhanVienBUS();
        }
        return instance;
    }

    // ============================
    // taoHoSo - tạo nhân viên mới + thông tin cá nhân trong transaction
    // ============================

    public KetQua<NhanVien> taoHoSo(NhanVien nv, ThongTinCaNhan ttcn) {
        // Validate mã nhân viên (String)
        if (nv.getMaNhanVien() == null || nv.getMaNhanVien().trim().isEmpty()) {
            return KetQua.error("Mã nhân viên không được để trống.");
        }
        if (nvRepo.existsByMaNhanVien(nv.getMaNhanVien().trim())) {
            return KetQua.error("Mã nhân viên '" + nv.getMaNhanVien() + "' đã tồn tại.");
        }

        // Validate họ tên
        if (ttcn.getHoTen() == null || ttcn.getHoTen().trim().isEmpty()) {
            return KetQua.error("Họ tên không được để trống.");
        }

        // Validate CCCD format (12 chữ số)
        if (ttcn.getCccd() != null && !ttcn.getCccd().trim().isEmpty()) {
            if (!ttcn.getCccd().trim().matches("\\d{12}")) {
                return KetQua.error("CCCD phải là 12 chữ số.");
            }
            if (ttcnRepo.existsByCCCD(ttcn.getCccd().trim(), nv.getMaNhanVien())) {
                return KetQua.error("CCCD '" + ttcn.getCccd() + "' đã được đăng ký cho nhân viên khác.");
            }
        }

        // Validate ngày vào làm
        if (nv.getNgayVaoLam() == null) {
            return KetQua.error("Ngày vào làm không được để trống.");
        }

        // Đặt trạng thái mặc định
        if (nv.getTrangThai() == null || nv.getTrangThai().trim().isEmpty()) {
            nv.setTrangThai("dang_lam_viec");
        }

        // Transaction: insert NhanVien + ThongTinCaNhan + SoDungPhep
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Insert NhanVien (trả về String maNV đã insert)
                String maNV = nvRepo.insert(conn, nv);
                ttcn.setMaNV(maNV);

                // Insert ThongTinCaNhan
                ttcnRepo.insert(conn, ttcn);

                // Tạo SoDungPhep cho năm hiện tại - phép năm 12 ngày
                int namHienTai = LocalDate.now().getYear();
                insertSoDungPhep(conn, maNV, namHienTai, "PHEP_NAM", 12.0);

                conn.commit();

                // Gán họ tên để trả về đầy đủ thông tin
                nv.setHoTen(ttcn.getHoTen());
                return KetQua.success(nv, "Tạo hồ sơ nhân viên thành công. Mã NV: " + maNV);
            } catch (Exception ex) {
                conn.rollback();
                return KetQua.error("Lỗi khi tạo hồ sơ: " + ex.getMessage());
            }
        } catch (SQLException e) {
            return KetQua.error("Lỗi kết nối cơ sở dữ liệu: " + e.getMessage());
        }
    }

    // ============================
    // capNhatThongTinCaNhan
    // ============================

    public KetQua<ThongTinCaNhan> capNhatThongTinCaNhan(ThongTinCaNhan ttcn) {
        if (ttcn.getMaNV() == null || ttcn.getMaNV().trim().isEmpty()) {
            return KetQua.error("Mã nhân viên không hợp lệ.");
        }

        // Validate CCCD
        if (ttcn.getCccd() != null && !ttcn.getCccd().trim().isEmpty()) {
            if (!ttcn.getCccd().trim().matches("\\d{12}")) {
                return KetQua.error("CCCD phải là 12 chữ số.");
            }
            if (ttcnRepo.existsByCCCD(ttcn.getCccd().trim(), ttcn.getMaNV())) {
                return KetQua.error("CCCD '" + ttcn.getCccd() + "' đã được đăng ký cho nhân viên khác.");
            }
        }

        // Validate email
        if (ttcn.getEmail() != null && !ttcn.getEmail().trim().isEmpty()) {
            if (!ttcn.getEmail().trim().matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
                return KetQua.error("Địa chỉ email không hợp lệ.");
            }
        }

        // Validate số điện thoại (10-11 chữ số, bắt đầu bằng 0)
        if (ttcn.getDienThoai() != null && !ttcn.getDienThoai().trim().isEmpty()) {
            if (!ttcn.getDienThoai().trim().matches("0\\d{9,10}")) {
                return KetQua.error("Số điện thoại không hợp lệ (phải bắt đầu bằng 0 và có 10-11 chữ số).");
            }
        }

        try {
            ttcnRepo.update(ttcn);
            return KetQua.success(ttcn, "Cập nhật thông tin cá nhân thành công.");
        } catch (Exception e) {
            return KetQua.error("Lỗi cập nhật thông tin cá nhân: " + e.getMessage());
        }
    }

    // ============================
    // capNhatTrangThai
    // ============================

    public KetQua<NhanVien> capNhatTrangThai(String maNV, String trangThaiMoi, String lyDo) {
        if (maNV == null || maNV.trim().isEmpty()) {
            return KetQua.error("Mã nhân viên không hợp lệ.");
        }

        NhanVien nv = nvRepo.findByMaNhanVien(maNV);
        if (nv == null) {
            return KetQua.error("Không tìm thấy nhân viên với mã: " + maNV);
        }

        String trangThaiHienTai = nv.getTrangThai();

        // Kiểm tra transition hợp lệ
        boolean valid = false;
        if ("dang_lam_viec".equals(trangThaiHienTai) && ("tam_nghi".equals(trangThaiMoi) || "nghi_viec".equals(trangThaiMoi))) {
            valid = true;
        }
        if ("tam_nghi".equals(trangThaiHienTai) && ("dang_lam_viec".equals(trangThaiMoi) || "nghi_viec".equals(trangThaiMoi))) {
            valid = true;
        }
        if (!valid) {
            if ("nghi_viec".equals(trangThaiHienTai)) {
                return KetQua.error("Nhân viên đã nghỉ việc, không thể thay đổi trạng thái.");
            }
            return KetQua.error("Chuyển trạng thái không hợp lệ từ '" + trangThaiHienTai + "' sang '" + trangThaiMoi + "'.");
        }

        nv.setTrangThai(trangThaiMoi);
        nvRepo.update(nv);

        // Nếu nghỉ việc: kết thúc tất cả bổ nhiệm đang hiệu lực + vô hiệu hóa tài khoản
        if ("nghi_viec".equals(trangThaiMoi)) {
            boNhiemRepo.endAllActiveBoNhiemForNV(maNV, LocalDate.now());
            taiKhoanRepo.deactivateByMaNV(maNV);
        }

        return KetQua.success(nv, "Cập nhật trạng thái nhân viên thành công.");
    }

    // ============================
    // Getters
    // ============================

    public List<NhanVien> getAll() {
        return nvRepo.findAll();
    }

    public List<NhanVien> getAllByScope(String currentMaNV) {
        com.hrm.model.DataScope scope = com.hrm.bus.XacThucBUS.getInstance().getScopeForAction("EMPLOYEE_VIEW");
        return nvRepo.findAllByScope(scope, currentMaNV);
    }

    public List<NhanVien> getAllByActionScope(String action, String currentMaNV) {
        com.hrm.model.DataScope scope = com.hrm.bus.XacThucBUS.getInstance().getScopeForAction(action);
        return nvRepo.findAllByScope(scope, currentMaNV);
    }

    public List<NhanVien> getDangLamViec() {
        return nvRepo.findDangLamViec();
    }

    public NhanVien getByMaNhanVien(String maNhanVien) {
        return nvRepo.findByMaNhanVien(maNhanVien);
    }

    public NhanVien getById(String maNV) {
        return nvRepo.findByMaNhanVien(maNV);
    }

    public ThongTinCaNhan getThongTinCaNhan(String maNV) {
        return ttcnRepo.findByMaNV(maNV);
    }

    public String generateMaNhanVien() {
        return nvRepo.generateMaNhanVien();
    }

    public List<NhanVien> getNhanVienByMaQuanLy(String maQuanLy) {
        return nvRepo.findNhanVienByMaQuanLy(maQuanLy);
    }

    // ============================
    // Private helper
    // ============================

    private void insertSoDungPhep(Connection conn, String maNV, int nam, String maLoaiPhep,
                                  double soNgayDuocCap) throws SQLException {
        String sql = "INSERT INTO SODUNGPHEP (maNV, nam, maLoaiPhep, soNgayDuocCap, soNgayDaDung) "
                + "VALUES (?, ?, ?, ?, 0) "
                + "ON DUPLICATE KEY UPDATE soNgayDuocCap=VALUES(soNgayDuocCap)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maNV);
            ps.setInt(2, nam);
            ps.setString(3, maLoaiPhep);
            ps.setDouble(4, soNgayDuocCap);
            ps.executeUpdate();
        }
    }
}