package com.hrm.bus;

import com.hrm.model.DataScope;
import com.hrm.model.Quyen;
import com.hrm.model.TaiKhoan;
import com.hrm.model.VaiTro;
import com.hrm.util.HRMConstants;
import com.hrm.util.SessionContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class XacThucBUSScopeTest {

    @AfterEach
    void tearDown() {
        SessionContext.getInstance().clearSession();
    }

    @Test
    @DisplayName("getScopeForAction: admin luon co ALL")
    void getScopeForAction_adminAlwaysAll() {
        TaiKhoan user = new TaiKhoan();
        user.setTenDangNhap(HRMConstants.USERNAME_ADMIN);
        SessionContext.getInstance().setCurrentUser(user);

        assertEquals(DataScope.ALL, XacThucBUS.getInstance().getScopeForAction("ANY_PERMISSION"));
    }

    @Test
    @DisplayName("getScopeForAction: lay dung scope tu quyen da gan")
    void getScopeForAction_returnRoleScope() {
        TaiKhoan user = new TaiKhoan();
        VaiTro role = new VaiTro("TRUONG_PHONG", "Truong phong", null);
        Quyen permission = new Quyen("LEAVE_APPROVE", "Duyet nghi phep", "LEAVE");
        permission.setPhamVi(DataScope.DEPT);
        role.themQuyen(permission);
        user.themVaiTro(role);
        SessionContext.getInstance().setCurrentUser(user);

        assertEquals(DataScope.DEPT, XacThucBUS.getInstance().getScopeForAction("LEAVE_APPROVE"));
    }

    @Test
    @DisplayName("getScopeForAction: khong co quyen thi tra NONE")
    void getScopeForAction_missingPermission_returnNone() {
        TaiKhoan user = new TaiKhoan();
        user.themVaiTro(new VaiTro("NHAN_VIEN", "Nhan vien", null));
        SessionContext.getInstance().setCurrentUser(user);

        assertEquals(DataScope.NONE, XacThucBUS.getInstance().getScopeForAction("REPORT_VIEW"));
    }
}
