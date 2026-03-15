package com.hrm.bus;

import com.hrm.dao.BoNhiemDAO;
import com.hrm.dao.PhongBanDAO;
import com.hrm.model.PhongBan;

import java.util.List;

/**
 * Service quan ly phong ban.
 */
public class PhongBanBUS {
    private static final String TRANG_THAI_HOAT_DONG = "hoatDong";
    private static final String TRANG_THAI_NGUNG_HOAT_DONG = "ngung_hoat_dong";

    private final PhongBanDAO repository = new PhongBanDAO();
    private final BoNhiemDAO boNhiemRepo = BoNhiemDAO.getInstance();

    public List<PhongBan> getAllDepartments() {
        return repository.findAll();
    }

    public List<PhongBan> getActiveDepartments() {
        return repository.findActive();
    }

    public PhongBan getById(String maPhongBan) {
        return repository.findById(maPhongBan);
    }

    public KetQua<Void> addDepartment(String maPhongBan, String tenPhongBan, String phongBanCha) {
        if (isBlank(maPhongBan)) {
            return KetQua.error("Mã phòng ban không được để trống.");
        }
        if (isBlank(tenPhongBan)) {
            return KetQua.error("Tên phòng ban không được để trống.");
        }
        if (repository.existsById(maPhongBan.trim())) {
            return KetQua.error("Mã phòng ban '" + maPhongBan.trim() + "' đã tồn tại trong hệ thống.");
        }

        if (!isBlank(phongBanCha)) {
            PhongBan cha = repository.findById(phongBanCha.trim());
            if (cha == null) {
                return KetQua.error("Phòng ban cha không tồn tại.");
            }
            if (!isActiveStatus(cha.getTrangThai())) {
                return KetQua.error("Phòng ban cha '" + cha.getTenPhongBan() + "' đã ngừng hoạt động.");
            }
        }

        String maCha = normalizeOptional(phongBanCha);
        PhongBan dept = new PhongBan(maPhongBan.trim(), tenPhongBan.trim(), maCha, TRANG_THAI_HOAT_DONG);
        repository.save(dept);
        return KetQua.success(null, "Thêm phòng ban thành công.");
    }

    public KetQua<Void> updateDepartment(String maPhongBan, String tenMoi, String phongBanChaMoi) {
        PhongBan dept = repository.findById(maPhongBan);
        if (dept == null) {
            return KetQua.error("Không tìm thấy phòng ban.");
        }
        if (isBlank(tenMoi)) {
            return KetQua.error("Tên phòng ban không được để trống.");
        }

        if (!isBlank(phongBanChaMoi)) {
            PhongBan cha = repository.findById(phongBanChaMoi.trim());
            if (cha == null) {
                return KetQua.error("Phòng ban cha không tồn tại.");
            }
            if (!isActiveStatus(cha.getTrangThai())) {
                return KetQua.error("Phòng ban cha '" + cha.getTenPhongBan() + "' đã ngừng hoạt động.");
            }
            if (isDescendant(maPhongBan, phongBanChaMoi.trim())) {
                return KetQua.error("Không thể chọn phòng ban con/cháu làm phòng ban cha. Sẽ tạo vòng lặp trong cây tổ chức.");
            }
        }

        String maCha = normalizeOptional(phongBanChaMoi);
        dept.setTenPhongBan(tenMoi.trim());
        dept.setPhongBanChaId(maCha);
        repository.update(dept);
        return KetQua.success(null, "Cập nhật phòng ban thành công.");
    }

    public KetQua<Void> deactivateDepartment(String maPhongBan) {
        PhongBan dept = repository.findById(maPhongBan);
        if (dept == null) {
            return KetQua.error("Không tìm thấy phòng ban.");
        }

        List<PhongBan> danhSachCon = repository.findChildren(maPhongBan);
        for (PhongBan con : danhSachCon) {
            if (isActiveStatus(con.getTrangThai())) {
                return KetQua.error("Không thể ngừng hoạt động. Phòng ban '" + con.getTenPhongBan() + "' vẫn đang hoạt động. Vui lòng ngừng các phòng ban con trước.");
            }
        }

        if (boNhiemRepo.hasActiveBoNhiemInDepartment(maPhongBan)) {
            return KetQua.error("Không thể ngừng hoạt động. Phòng ban vẫn còn bổ nhiệm đang hiệu lực. Vui lòng kết thúc các bổ nhiệm trước.");
        }

        dept.setTrangThai(TRANG_THAI_NGUNG_HOAT_DONG);
        repository.update(dept);
        return KetQua.success(null, "Đã ngừng hoạt động phòng ban.");
    }

    /**
     * Kich hoat lai phong ban da ngung hoat dong.
     */
    public KetQua<Void> activateDepartment(String maPhongBan) {
        PhongBan dept = repository.findById(maPhongBan);
        if (dept == null) {
            return KetQua.error("Không tìm thấy phòng ban.");
        }

        String maCha = dept.getPhongBanChaId();
        if (!isBlank(maCha)) {
            PhongBan cha = repository.findById(maCha.trim());
            if (cha != null && !isActiveStatus(cha.getTrangThai())) {
                return KetQua.error("Không thể kích hoạt. Phòng ban cha '" + cha.getTenPhongBan() + "' đang ngừng hoạt động.");
            }
        }

        dept.setTrangThai(TRANG_THAI_HOAT_DONG);
        repository.update(dept);
        return KetQua.success(null, "Đã kích hoạt lại phòng ban.");
    }

    private boolean isActiveStatus(String status) {
        return "hoatdong".equals(normalizeStatus(status));
    }

    private String normalizeStatus(String status) {
        if (status == null) return "";
        return status.toLowerCase().replace("_", "").replace(" ", "").replace("-", "");
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String normalizeOptional(String value) {
        if (isBlank(value)) return null;
        return value.trim();
    }

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
