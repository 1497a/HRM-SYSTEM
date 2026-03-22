package com.hrm.bus;

import com.hrm.dao.BoNhiemDAO;
import com.hrm.dao.ChucVuDAO;
import com.hrm.model.ChucVu;
import com.hrm.model.DataScope;
import com.hrm.model.TaiKhoan;
import com.hrm.util.HRMConstants;
import com.hrm.util.PermissionCodes;
import com.hrm.util.SessionContext;
import com.hrm.util.ValidationUtils;

import java.util.List;
import java.util.stream.Collectors;

public class ChucVuBUS {
    private static final String ACTION_POSITION_MANAGE = PermissionCodes.POSITION_MANAGE;
    private final ChucVuDAO positionRepo = ChucVuDAO.getInstance();
    private final BoNhiemDAO boNhiemRepo = BoNhiemDAO.getInstance();
    public List<ChucVu> getAllPositions() {
        return positionRepo.findAll();
    }

    public List<ChucVu> getActivePositions() {
        return positionRepo.findActive();
    }

    public List<ChucVu> getRecruitablePositions() {
        return getActivePositions().stream()
                .filter(position -> position.getCapBac() >= 3)
                .collect(Collectors.toList());
    }

    public ChucVu getByMaChucVu(String maChucVu) {
        return positionRepo.findById(maChucVu);
    }

    public boolean existsActiveByCode(String maChucVu) {
        return positionRepo.existsActiveByCode(maChucVu);
    }

    public KetQua<Void> addPosition(String maChucVu, String tenChucVu, int capBac,
                                    double phuCapChucVu, String moTa, String maVaiTro) {
        KetQua<Void> permission = validateManagePermission();
        if (!permission.isSuccess()) {
            return permission;
        }
        if (ValidationUtils.isBlank(maChucVu)) {
            return KetQua.error("Mã chức vụ không được để trống.");
        }
        if (ValidationUtils.isBlank(tenChucVu)) {
            return KetQua.error("Tên chức vụ không được để trống.");
        }
        if (positionRepo.existsById(maChucVu.trim())) {
            return KetQua.error("Mã chức vụ '" + maChucVu + "' đã tồn tại.");
        }
        if (phuCapChucVu < 0) {
            return KetQua.error("Phụ cấp không được âm.");
        }
        ChucVu position = new ChucVu(
                maChucVu.trim(),
                tenChucVu.trim(),
                capBac,
                phuCapChucVu,
                moTa,
                HRMConstants.TRANG_THAI_HOAT_DONG
        );
        position.setMaVaiTro(ValidationUtils.isBlank(maVaiTro) ? null : maVaiTro.trim());
        positionRepo.save(position);
        return KetQua.success(null, "Thêm chức vụ thành công.");
    }

    public KetQua<Void> updatePosition(String maChucVu, String tenMoi, int capBacMoi,
                                       double phuCapMoi, String moTaMoi, String maVaiTroMoi) {
        KetQua<Void> permission = validateManagePermission();
        if (!permission.isSuccess()) {
            return permission;
        }
        ChucVu position = positionRepo.findById(maChucVu);
        if (position == null) {
            return KetQua.error("Không tìm thấy chức vụ.");
        }
        if (ValidationUtils.isBlank(tenMoi)) {
            return KetQua.error("Tên chức vụ không được để trống.");
        }
        if (phuCapMoi < 0) {
            return KetQua.error("Phụ cấp không được âm.");
        }
        position.setTenChucVu(tenMoi.trim());
        position.setCapBac(capBacMoi);
        position.setPhuCapChucVu(phuCapMoi);
        position.setMoTa(moTaMoi);
        position.setMaVaiTro(ValidationUtils.isBlank(maVaiTroMoi) ? null : maVaiTroMoi.trim());
        positionRepo.update(position);
        return KetQua.success(null, "Cập nhật chức vụ thành công.");
    }

    public KetQua<Void> setMaVaiTro(String maChucVu, String maVaiTro) {
        TaiKhoan currentUser = SessionContext.getInstance().getCurrentUser();
        if (currentUser == null) {
            return KetQua.error("Phiên đăng nhập không hợp lệ.");
        }
        if (!SessionContext.getInstance().isAdmin()
                && !currentUser.coQuyen(PermissionCodes.ROLE_UPDATE)) {
            return KetQua.error("Bạn không có quyền cấu hình mapping vai trò.");
        }
        positionRepo.updateMaVaiTro(maChucVu, ValidationUtils.isBlank(maVaiTro) ? null : maVaiTro.trim());
        return KetQua.success(null, "Cập nhật mapping vai trò thành công.");
    }

    public KetQua<Void> deactivatePosition(String maChucVu) {
        KetQua<Void> permission = validateManagePermission();
        if (!permission.isSuccess()) {
            return permission;
        }
        ChucVu position = positionRepo.findById(maChucVu);
        if (position == null) {
            return KetQua.error("Không tìm thấy chức vụ.");
        }
        if (boNhiemRepo.hasActiveBoNhiemByChucVu(maChucVu)) {
            return KetQua.error("Không thể ngừng hoạt động chức vụ vì vẫn còn nhân viên đang giữ chức vụ này.");
        }
        position.setTrangThai(HRMConstants.TRANG_THAI_NGUNG_HOAT_DONG);
        positionRepo.update(position);
        return KetQua.success(null, "Đã ngừng hoạt động chức vụ.");
    }

    public KetQua<Void> activatePosition(String maChucVu) {
        KetQua<Void> permission = validateManagePermission();
        if (!permission.isSuccess()) {
            return permission;
        }
        ChucVu position = positionRepo.findById(maChucVu);
        if (position == null) {
            return KetQua.error("Không tìm thấy chức vụ.");
        }
        position.setTrangThai(HRMConstants.TRANG_THAI_HOAT_DONG);
        positionRepo.update(position);
        return KetQua.success(null, "Đã kích hoạt lại chức vụ.");
    }

    private KetQua<Void> validateManagePermission() {
        TaiKhoan currentUser = SessionContext.getInstance().getCurrentUser();
        if (currentUser == null) {
            return KetQua.error("Phiên đăng nhập không hợp lệ.");
        }
        if (SessionContext.getInstance().isAdmin()) {
            return KetQua.success(null, "");
        }
        if (!currentUser.coQuyen(ACTION_POSITION_MANAGE)) {
            return KetQua.error("Bạn không có quyền quản lý chức vụ.");
        }
        if (XacThucBUS.getInstance().getScopeForAction(ACTION_POSITION_MANAGE) != DataScope.ALL) {
            return KetQua.error("Quyền quản lý chức vụ yêu cầu phạm vi ALL.");
        }
        return KetQua.success(null, "");
    }

}
