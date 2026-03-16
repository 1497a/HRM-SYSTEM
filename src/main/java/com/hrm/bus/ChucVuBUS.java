package com.hrm.bus;

import com.hrm.dao.BoNhiemDAO;
import com.hrm.dao.ChucVuDAO;
import com.hrm.model.ChucVu;
import com.hrm.model.DataScope;
import com.hrm.model.TaiKhoan;
import com.hrm.util.SessionContext;

import java.util.List;
import java.util.stream.Collectors;

public class ChucVuBUS {
    private static final String ACTION_POSITION_MANAGE = "POSITION_MANAGE";
    private static final String TRANG_THAI_HOAT_DONG = "hoatdong";
    private static final String TRANG_THAI_NGUNG_HOAT_DONG = "ngung_hoat_dong";

    private final ChucVuDAO positionRepo = new ChucVuDAO();
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

    public ChucVu getById(String maChucVu) {
        return positionRepo.findById(maChucVu);
    }

    public boolean existsActiveByCode(String maChucVu) {
        return positionRepo.existsActiveByCode(maChucVu);
    }

    public KetQua<Void> addPosition(String maChucVu, String tenChucVu, int capBac,
                                    double phuCapChucVu, String moTa) {
        KetQua<Void> permission = validateManagePermission();
        if (!permission.isSuccess()) {
            return permission;
        }
        if (isBlank(maChucVu)) {
            return KetQua.error("Ma chuc vu khong duoc de trong.");
        }
        if (isBlank(tenChucVu)) {
            return KetQua.error("Ten chuc vu khong duoc de trong.");
        }
        if (positionRepo.existsById(maChucVu.trim())) {
            return KetQua.error("Ma chuc vu '" + maChucVu + "' da ton tai.");
        }
        if (phuCapChucVu < 0) {
            return KetQua.error("Phu cap khong duoc am.");
        }

        ChucVu position = new ChucVu(
                maChucVu.trim(),
                tenChucVu.trim(),
                capBac,
                phuCapChucVu,
                moTa,
                TRANG_THAI_HOAT_DONG
        );
        positionRepo.save(position);
        return KetQua.success(null, "Them chuc vu thanh cong.");
    }

    public KetQua<Void> updatePosition(String maChucVu, String tenMoi, int capBacMoi,
                                       double phuCapMoi, String moTaMoi) {
        KetQua<Void> permission = validateManagePermission();
        if (!permission.isSuccess()) {
            return permission;
        }
        ChucVu position = positionRepo.findById(maChucVu);
        if (position == null) {
            return KetQua.error("Khong tim thay chuc vu.");
        }
        if (isBlank(tenMoi)) {
            return KetQua.error("Ten chuc vu khong duoc de trong.");
        }
        if (phuCapMoi < 0) {
            return KetQua.error("Phu cap khong duoc am.");
        }

        position.setTenChucVu(tenMoi.trim());
        position.setCapBac(capBacMoi);
        position.setPhuCapChucVu(phuCapMoi);
        position.setMoTa(moTaMoi);
        positionRepo.update(position);
        return KetQua.success(null, "Cap nhat chuc vu thanh cong.");
    }

    public KetQua<Void> deactivatePosition(String maChucVu) {
        KetQua<Void> permission = validateManagePermission();
        if (!permission.isSuccess()) {
            return permission;
        }
        ChucVu position = positionRepo.findById(maChucVu);
        if (position == null) {
            return KetQua.error("Khong tim thay chuc vu.");
        }
        if (boNhiemRepo.hasActiveBoNhiemByChucVu(maChucVu)) {
            return KetQua.error("Khong the ngung hoat dong chuc vu vi van con nhan vien dang giu chuc vu nay.");
        }
        position.setTrangThai(TRANG_THAI_NGUNG_HOAT_DONG);
        positionRepo.update(position);
        return KetQua.success(null, "Da ngung hoat dong chuc vu.");
    }

    public KetQua<Void> activatePosition(String maChucVu) {
        KetQua<Void> permission = validateManagePermission();
        if (!permission.isSuccess()) {
            return permission;
        }
        ChucVu position = positionRepo.findById(maChucVu);
        if (position == null) {
            return KetQua.error("Khong tim thay chuc vu.");
        }
        position.setTrangThai(TRANG_THAI_HOAT_DONG);
        positionRepo.update(position);
        return KetQua.success(null, "Da kich hoat lai chuc vu.");
    }

    private KetQua<Void> validateManagePermission() {
        TaiKhoan currentUser = SessionContext.getInstance().getCurrentUser();
        if (currentUser == null) {
            return KetQua.error("Phien dang nhap khong hop le.");
        }
        if ("admin".equalsIgnoreCase(currentUser.getTenDangNhap()) || currentUser.coVaiTro("ADMIN")) {
            return KetQua.success(null, "");
        }
        if (!currentUser.coQuyen(ACTION_POSITION_MANAGE)) {
            return KetQua.error("Ban khong co quyen quan ly chuc vu.");
        }
        if (XacThucBUS.getInstance().getScopeForAction(ACTION_POSITION_MANAGE) != DataScope.ALL) {
            return KetQua.error("Quyen quan ly chuc vu yeu cau pham vi ALL.");
        }
        return KetQua.success(null, "");
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
