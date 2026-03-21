package com.hrm.bus;

import com.hrm.dao.BoNhiemDAO;
import com.hrm.dao.PhongBanDAO;
import com.hrm.model.DataScope;
import com.hrm.model.PhongBan;
import com.hrm.model.TaiKhoan;
import com.hrm.util.HRMConstants;
import com.hrm.util.PermissionCodes;
import com.hrm.util.SessionContext;
import com.hrm.util.ValidationUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PhongBanBUS {
    private static final String ACTION_DEPARTMENT_MANAGE = PermissionCodes.DEPARTMENT_MANAGE;
    private final PhongBanDAO repository = PhongBanDAO.getInstance();
    private final BoNhiemDAO boNhiemRepo = BoNhiemDAO.getInstance();
    public List<PhongBan> getAllDepartments() {
        return repository.findAll();
    }

    public List<PhongBan> getActiveDepartments() {
        return repository.findActive();
    }

    public PhongBan getByMaPhongBan(String maPhongBan) {
        return repository.findById(maPhongBan);
    }

    public KetQua<Void> addDepartment(String maPhongBan, String tenPhongBan, String phongBanCha) {
        KetQua<Void> permission = validateManagePermission();
        if (!permission.isSuccess()) {
            return permission;
        }
        if (ValidationUtils.isBlank(maPhongBan)) {
            return KetQua.error("Mã phòng ban không được để trống.");
        }
        if (ValidationUtils.isBlank(tenPhongBan)) {
            return KetQua.error("Tên phòng ban không được để trống.");
        }
        if (repository.existsById(maPhongBan.trim())) {
            return KetQua.error("Mã phòng ban '" + maPhongBan.trim() + "' đã tồn tại trong hệ thống.");
        }
        if (!ValidationUtils.isBlank(phongBanCha)) {
            PhongBan cha = repository.findById(phongBanCha.trim());
            if (cha == null) {
                return KetQua.error("Phòng ban cha không tồn tại.");
            }
            if (!isActiveStatus(cha.getTrangThai())) {
                return KetQua.error("Phòng ban cha '" + cha.getTenPhongBan() + "' đã ngừng hoạt động.");
            }
        }
        String maCha = normalizeOptional(phongBanCha);
        PhongBan department = new PhongBan(maPhongBan.trim(), tenPhongBan.trim(), maCha, HRMConstants.TRANG_THAI_HOAT_DONG);
        repository.save(department);
        return KetQua.success(null, "Thêm phòng ban thành công.");
    }

    public KetQua<Void> updateDepartment(String maPhongBan, String tenMoi, String phongBanChaMoi) {
        KetQua<Void> permission = validateManagePermission();
        if (!permission.isSuccess()) {
            return permission;
        }
        PhongBan department = repository.findById(maPhongBan);
        if (department == null) {
            return KetQua.error("Không tìm thấy phòng ban.");
        }
        if (ValidationUtils.isBlank(tenMoi)) {
            return KetQua.error("Tên phòng ban không được để trống.");
        }
        if (!ValidationUtils.isBlank(phongBanChaMoi)) {
            PhongBan cha = repository.findById(phongBanChaMoi.trim());
            if (cha == null) {
                return KetQua.error("Phòng ban cha không tồn tại.");
            }
            if (!isActiveStatus(cha.getTrangThai())) {
                return KetQua.error("Phòng ban cha '" + cha.getTenPhongBan() + "' đã ngừng hoạt động.");
            }
            if (isDescendant(maPhongBan, phongBanChaMoi.trim())) {
                return KetQua.error("Không thể chọn phòng ban con/cháu làm phòng ban cha.");
            }
        }
        department.setTenPhongBan(tenMoi.trim());
        department.setPhongBanChaId(normalizeOptional(phongBanChaMoi));
        repository.update(department);
        return KetQua.success(null, "Cập nhật phòng ban thành công.");
    }

    public KetQua<Void> deactivateDepartment(String maPhongBan) {
        KetQua<Void> permission = validateManagePermission();
        if (!permission.isSuccess()) {
            return permission;
        }
        PhongBan department = repository.findById(maPhongBan);
        if (department == null) {
            return KetQua.error("Không tìm thấy phòng ban.");
        }
        List<PhongBan> children = repository.findChildren(maPhongBan);
        for (PhongBan child : children) {
            if (isActiveStatus(child.getTrangThai())) {
                return KetQua.error("Không thể ngừng hoạt động khi phòng ban con vẫn đang hoạt động.");
            }
        }
        if (boNhiemRepo.hasActiveBoNhiemInDepartment(maPhongBan)) {
            return KetQua.error("Không thể ngừng hoạt động khi phòng ban vẫn còn bổ nhiệm hiệu lực.");
        }
        department.setTrangThai(HRMConstants.TRANG_THAI_NGUNG_HOAT_DONG);
        repository.update(department);
        return KetQua.success(null, "Đã ngừng hoạt động phòng ban.");
    }

    public KetQua<Void> activateDepartment(String maPhongBan) {
        KetQua<Void> permission = validateManagePermission();
        if (!permission.isSuccess()) {
            return permission;
        }
        PhongBan department = repository.findById(maPhongBan);
        if (department == null) {
            return KetQua.error("Không tìm thấy phòng ban.");
        }
        String maCha = department.getPhongBanChaId();
        if (!ValidationUtils.isBlank(maCha)) {
            PhongBan cha = repository.findById(maCha.trim());
            if (cha != null && !isActiveStatus(cha.getTrangThai())) {
                return KetQua.error("Không thể kích hoạt khi phòng ban cha đang ngừng hoạt động.");
            }
        }
        department.setTrangThai(HRMConstants.TRANG_THAI_HOAT_DONG);
        repository.update(department);
        return KetQua.success(null, "Đã kích hoạt lại phòng ban.");
    }

    private KetQua<Void> validateManagePermission() {
        TaiKhoan currentUser = SessionContext.getInstance().getCurrentUser();
        if (currentUser == null) {
            return KetQua.error("Phiên đăng nhập không hợp lệ.");
        }
        if (SessionContext.getInstance().isAdmin()) {
            return KetQua.success(null, "");
        }
        if (!currentUser.coQuyen(ACTION_DEPARTMENT_MANAGE)) {
            return KetQua.error("Bạn không có quyền quản lý phòng ban.");
        }
        if (XacThucBUS.getInstance().getScopeForAction(ACTION_DEPARTMENT_MANAGE) != DataScope.ALL) {
            return KetQua.error("Quyền quản lý phòng ban yêu cầu phạm vi ALL.");
        }
        return KetQua.success(null, "");
    }

    private boolean isActiveStatus(String status) {
        return normalizeStatus(HRMConstants.TRANG_THAI_HOAT_DONG).equals(normalizeStatus(status));
    }

    private String normalizeStatus(String status) {
        if (status == null) {
            return "";
        }
        return status.toLowerCase().replace("_", "").replace(" ", "").replace("-", "");
    }

    private String normalizeOptional(String value) {
        if (ValidationUtils.isBlank(value)) {
            return null;
        }
        return value.trim();
    }

    private boolean isDescendant(String maCha, String maCon) {
        if (maCon == null) {
            return false;
        }
        Set<String> visited = new HashSet<>();
        String current = maCon;
        while (current != null) {
            if (!visited.add(current)) {
                // Existing cycle in data; stop traversal and treat as invalid relation.
                return true;
            }
            if (current.equals(maCha)) {
                return true;
            }
            PhongBan node = repository.findById(current);
            current = node == null ? null : node.getPhongBanChaId();
        }
        return false;
    }
}
