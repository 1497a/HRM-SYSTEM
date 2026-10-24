package com.hrm.bus;

import com.hrm.model.TaiKhoan;
import com.hrm.model.VaiTro;
import com.hrm.util.HRMConstants;
import com.hrm.util.SessionContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SelfApprovalGuardTest {

    @AfterEach
    void tearDown() {
        SessionContext.getInstance().clearSession();
    }

    @Test
    @DisplayName("isSelfAction: cung ma nhan vien thi la self action")
    void isSelfAction_sameEmployee_returnTrue() {
        assertTrue(SelfApprovalGuard.isSelfAction("NV001", "NV001"));
    }

    @Test
    @DisplayName("isSelfAction: khac ma nhan vien thi khong phai self action")
    void isSelfAction_differentEmployee_returnFalse() {
        assertFalse(SelfApprovalGuard.isSelfAction("NV001", "NV002"));
    }

    @Test
    @DisplayName("currentUserCanBypassSelfRestriction: admin he thong duoc bypass")
    void currentUserCanBypass_adminUsername_returnTrue() {
        TaiKhoan user = new TaiKhoan();
        user.setTenDangNhap(HRMConstants.USERNAME_ADMIN);
        SessionContext.getInstance().setCurrentUser(user);

        assertTrue(SelfApprovalGuard.currentUserCanBypassSelfRestriction());
    }

    @Test
    @DisplayName("currentUserCanBypassSelfRestriction: tong giam doc duoc bypass")
    void currentUserCanBypass_tgdRole_returnTrue() {
        TaiKhoan user = new TaiKhoan();
        user.setTenDangNhap("hung.nguyen");
        user.themVaiTro(new VaiTro("TONG_GIAM_DOC", "Tong giam doc", null));
        SessionContext.getInstance().setCurrentUser(user);

        assertTrue(SelfApprovalGuard.currentUserCanBypassSelfRestriction());
    }

    @Test
    @DisplayName("currentUserCanBypassSelfRestriction: HR thong thuong khong duoc bypass")
    void currentUserCanBypass_regularRole_returnFalse() {
        TaiKhoan user = new TaiKhoan();
        user.setTenDangNhap("lananh.dang");
        user.themVaiTro(new VaiTro("NHAN_SU", "Nhan su", null));
        SessionContext.getInstance().setCurrentUser(user);

        assertFalse(SelfApprovalGuard.currentUserCanBypassSelfRestriction());
    }
}
