package com.hrm.bus;

import com.hrm.model.PhongBan;
import com.hrm.dao.BoNhiemDAO;
import com.hrm.dao.PhongBanDAO;

import java.util.List;

/**
 * Service quản lý phòng ban.
 * Áp dụng business logic, delegate persistence xuống PhongBanDAO (JDBC).
 */
public class PhongBanBUS {

    private final PhongBanDAO repository = new PhongBanDAO();
    private final BoNhiemDAO boNhiemRepo = BoNhiemDAO.getInstance();

    /**
     * Lấy tất cả phòng ban.
     */
    public List<PhongBan> getAllDepartments() {
        return repository.findAll();
    }

    /**
     * Lấy danh sách phòng ban đang hoạt động.
     * Trạng thái "hoat_dong" theo ENUM trong DB.
     */
    public List<PhongBan> getActiveDepartments() {
        return repository.findActive();
    }

    /**
     * Tìm phòng ban theo mã.
     */
    public PhongBan getById(String maPhongBan) {
        return repository.findById(maPhongBan);
    }

    /**
     * Thêm phòng ban mới.
     *
     * @param maPhongBan  mã phòng ban (duy nhất)
     * @param tenPhongBan tên phòng ban
     * @param phongBanCha mã phòng ban cha (null nếu là phòng ban gốc)
     * @throws IllegalArgumentException nếu dữ liệu không hợp lệ
     */
    public void addDepartment(String maPhongBan, String tenPhongBan, String phongBanCha) {
        if (maPhongBan == null || maPhongBan.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã phòng ban không được để trống.");
        }
        if (tenPhongBan == null || tenPhongBan.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên phòng ban không được để trống.");
        }
        if (repository.existsById(maPhongBan.trim())) {
            throw new IllegalArgumentException("Mã phòng ban '" + maPhongBan.trim() + "' đã tồn tại trong hệ thống.");
        }

        if (phongBanCha != null && !phongBanCha.trim().isEmpty()) {
            PhongBan cha = repository.findById(phongBanCha.trim());
            if (cha == null) {
                throw new IllegalArgumentException("Phòng ban cha không tồn tại.");
            }
            if (!"hoat_dong".equals(cha.getTrangThai())) {
                throw new IllegalArgumentException(
                        "Phòng ban cha '" + cha.getTenPhongBan() + "' đã ngưng hoạt động.");
            }
        }

        String maCha = (phongBanCha != null && !phongBanCha.trim().isEmpty()) ? phongBanCha.trim() : null;
        PhongBan dept = new PhongBan(maPhongBan.trim(), tenPhongBan.trim(), maCha, "hoat_dong");
        repository.save(dept);
    }

    /**
     * Cập nhật thông tin phòng ban.
     *
     * @param maPhongBan     mã phòng ban cần cập nhật
     * @param tenMoi         tên mới
     * @param phongBanChaMoi mã phòng ban cha mới (null nếu là gốc)
     * @throws IllegalArgumentException nếu dữ liệu không hợp lệ
     */
    public void updateDepartment(String maPhongBan, String tenMoi, String phongBanChaMoi) {
        PhongBan dept = repository.findById(maPhongBan);
        if (dept == null) {
            throw new IllegalArgumentException("Không tìm thấy phòng ban.");
        }
        if (tenMoi == null || tenMoi.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên phòng ban không được để trống.");
        }

        if (phongBanChaMoi != null && !phongBanChaMoi.trim().isEmpty()) {
            PhongBan cha = repository.findById(phongBanChaMoi.trim());
            if (cha == null) {
                throw new IllegalArgumentException("Phòng ban cha không tồn tại.");
            }
            if (!"hoat_dong".equals(cha.getTrangThai())) {
                throw new IllegalArgumentException(
                        "Phòng ban cha '" + cha.getTenPhongBan() + "' đã ngưng hoạt động.");
            }
            if (isDescendant(maPhongBan, phongBanChaMoi.trim())) {
                throw new IllegalArgumentException(
                        "Không thể chọn phòng ban con/cháu làm phòng ban cha. Sẽ tạo vòng lặp trong cây tổ chức.");
            }
        }

        String maCha = (phongBanChaMoi != null && !phongBanChaMoi.trim().isEmpty()) ? phongBanChaMoi.trim() : null;
        dept.setTenPhongBan(tenMoi.trim());
        dept.setPhongBanChaId(maCha);
        repository.update(dept);
    }

    /**
     * Ngưng hoạt động phòng ban.
     * Phải ngưng tất cả phòng ban con trước.
     *
     * @param maPhongBan mã phòng ban cần ngưng
     * @throws IllegalArgumentException nếu không thể ngưng
     */
    public void deactivateDepartment(String maPhongBan) {
        PhongBan dept = repository.findById(maPhongBan);
        if (dept == null) {
            throw new IllegalArgumentException("Không tìm thấy phòng ban.");
        }

        List<PhongBan> danhSachCon = repository.findChildren(maPhongBan);
        for (PhongBan con : danhSachCon) {
            if ("hoat_dong".equals(con.getTrangThai())) {
                throw new IllegalArgumentException(
                        "Không thể ngưng hoạt động. Phòng ban '" + con.getTenPhongBan()
                        + "' vẫn đang hoạt động. Vui lòng ngưng các phòng ban con trước.");
            }
        }

        if (boNhiemRepo.hasActiveBoNhiemInDepartment(maPhongBan)) {
            throw new IllegalArgumentException(
                    "Không thể ngưng hoạt động. Phòng ban vẫn còn bổ nhiệm đang hiệu lực. "
                    + "Vui lòng kết thúc các bổ nhiệm trước.");
        }

        dept.setTrangThai("ngung_hoat_dong");
        repository.update(dept);
    }

    // =====================================================================
    // ==================== Private Helpers ================================
    // =====================================================================

    /**
     * Kiểm tra maCon có phải là hậu duệ (con/cháu) của maCha không.
     * Dùng để ngăn chặn vòng lặp trong cây phòng ban.
     */
    private boolean isDescendant(String maCha, String maCon) {
        if (maCon == null) {
            return false;
        }
        if (maCon.equals(maCha)) {
            return true;
        }
        PhongBan con = repository.findById(maCon);
        if (con == null) {
            return false;
        }
        return isDescendant(maCha, con.getPhongBanChaId());
    }
}
