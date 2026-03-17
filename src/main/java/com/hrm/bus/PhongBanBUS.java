package com.hrm.bus;

import com.hrm.dao.BoNhiemDAO;
import com.hrm.dao.PhongBanDAO;
import com.hrm.model.DataScope;
import com.hrm.model.PhongBan;
import com.hrm.model.TaiKhoan;
import com.hrm.util.HRMConstants;
import com.hrm.util.PermissionCodes;

import java.util.List;

public class PhongBanBUS {
    private static final String ACTION_DEPARTMENT_MANAGE = PermissionCodes.DEPARTMENT_MANAGE;
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
        KetQua<Void> permission = validateManagePermission();
        if (!permission.isSuccess()) {
            return permission;
        }
        if (isBlank(maPhongBan)) {
            return KetQua.error("Ma phong ban khong duoc de trong.");
        }
        if (isBlank(tenPhongBan)) {
            return KetQua.error("Ten phong ban khong duoc de trong.");
        }
        if (repository.existsById(maPhongBan.trim())) {
            return KetQua.error("Ma phong ban '" + maPhongBan.trim() + "' da ton tai trong he thong.");
        }

        if (!isBlank(phongBanCha)) {
            PhongBan cha = repository.findById(phongBanCha.trim());
            if (cha == null) {
                return KetQua.error("Phong ban cha khong ton tai.");
            }
            if (!isActiveStatus(cha.getTrangThai())) {
                return KetQua.error("Phong ban cha '" + cha.getTenPhongBan() + "' da ngung hoat dong.");
            }
        }

        String maCha = normalizeOptional(phongBanCha);
        PhongBan department = new PhongBan(maPhongBan.trim(), tenPhongBan.trim(), maCha, TRANG_THAI_HOAT_DONG);
        repository.save(department);
        return KetQua.success(null, "Them phong ban thanh cong.");
    }

    public KetQua<Void> updateDepartment(String maPhongBan, String tenMoi, String phongBanChaMoi) {
        KetQua<Void> permission = validateManagePermission();
        if (!permission.isSuccess()) {
            return permission;
        }
        PhongBan department = repository.findById(maPhongBan);
        if (department == null) {
            return KetQua.error("Khong tim thay phong ban.");
        }
        if (isBlank(tenMoi)) {
            return KetQua.error("Ten phong ban khong duoc de trong.");
        }

        if (!isBlank(phongBanChaMoi)) {
            PhongBan cha = repository.findById(phongBanChaMoi.trim());
            if (cha == null) {
                return KetQua.error("Phong ban cha khong ton tai.");
            }
            if (!isActiveStatus(cha.getTrangThai())) {
                return KetQua.error("Phong ban cha '" + cha.getTenPhongBan() + "' da ngung hoat dong.");
            }
            if (isDescendant(maPhongBan, phongBanChaMoi.trim())) {
                return KetQua.error("Khong the chon phong ban con/chau lam phong ban cha.");
            }
        }

        department.setTenPhongBan(tenMoi.trim());
        department.setPhongBanChaId(normalizeOptional(phongBanChaMoi));
        repository.update(department);
        return KetQua.success(null, "Cap nhat phong ban thanh cong.");
    }

    public KetQua<Void> deactivateDepartment(String maPhongBan) {
        KetQua<Void> permission = validateManagePermission();
        if (!permission.isSuccess()) {
            return permission;
        }
        PhongBan department = repository.findById(maPhongBan);
        if (department == null) {
            return KetQua.error("Khong tim thay phong ban.");
        }

        List<PhongBan> children = repository.findChildren(maPhongBan);
        for (PhongBan child : children) {
            if (isActiveStatus(child.getTrangThai())) {
                return KetQua.error("Khong the ngung hoat dong khi phong ban con van dang hoat dong.");
            }
        }

        if (boNhiemRepo.hasActiveBoNhiemInDepartment(maPhongBan)) {
            return KetQua.error("Khong the ngung hoat dong khi phong ban van con bo nhiem hieu luc.");
        }

        department.setTrangThai(TRANG_THAI_NGUNG_HOAT_DONG);
        repository.update(department);
        return KetQua.success(null, "Da ngung hoat dong phong ban.");
    }

    public KetQua<Void> activateDepartment(String maPhongBan) {
        KetQua<Void> permission = validateManagePermission();
        if (!permission.isSuccess()) {
            return permission;
        }
        PhongBan department = repository.findById(maPhongBan);
        if (department == null) {
            return KetQua.error("Khong tim thay phong ban.");
        }

        String maCha = department.getPhongBanChaId();
        if (!isBlank(maCha)) {
            PhongBan cha = repository.findById(maCha.trim());
            if (cha != null && !isActiveStatus(cha.getTrangThai())) {
                return KetQua.error("Khong the kich hoat khi phong ban cha dang ngung hoat dong.");
            }
        }

        department.setTrangThai(TRANG_THAI_HOAT_DONG);
        repository.update(department);
        return KetQua.success(null, "Da kich hoat lai phong ban.");
    }

    private KetQua<Void> validateManagePermission() {
        TaiKhoan currentUser = com.hrm.util.SessionContext.getInstance().getCurrentUser();
        if (currentUser == null) {
            return KetQua.error("Phien dang nhap khong hop le.");
        }
        if (HRMConstants.USERNAME_ADMIN.equalsIgnoreCase(currentUser.getTenDangNhap()) || currentUser.coVaiTro(HRMConstants.ROLE_ADMIN)) {
            return KetQua.success(null, "");
        }
        if (!currentUser.coQuyen(ACTION_DEPARTMENT_MANAGE)) {
            return KetQua.error("Ban khong co quyen quan ly phong ban.");
        }
        if (XacThucBUS.getInstance().getScopeForAction(ACTION_DEPARTMENT_MANAGE) != DataScope.ALL) {
            return KetQua.error("Quyen quan ly phong ban yeu cau pham vi ALL.");
        }
        return KetQua.success(null, "");
    }

    private boolean isActiveStatus(String status) {
        return "hoatdong".equals(normalizeStatus(status));
    }

    private String normalizeStatus(String status) {
        if (status == null) {
            return "";
        }
        return status.toLowerCase().replace("_", "").replace(" ", "").replace("-", "");
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String normalizeOptional(String value) {
        if (isBlank(value)) {
            return null;
        }
        return value.trim();
    }

    private boolean isDescendant(String maCha, String maCon) {
        if (maCon == null) {
            return false;
        }
        if (maCon.equals(maCha)) {
            return true;
        }
        PhongBan child = repository.findById(maCon);
        if (child == null) {
            return false;
        }
        return isDescendant(maCha, child.getPhongBanChaId());
    }
}
