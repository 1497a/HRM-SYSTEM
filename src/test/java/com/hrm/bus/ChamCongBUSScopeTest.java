package com.hrm.bus;

import com.hrm.model.DataScope;
import com.hrm.model.Quyen;
import com.hrm.model.TaiKhoan;
import com.hrm.model.VaiTro;
import com.hrm.util.PermissionCodes;
import com.hrm.util.SessionContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ChamCongBUSScopeTest {

    @AfterEach
    void tearDown() {
        SessionContext.getInstance().clearSession();
    }

    @Test
    @DisplayName("isScopeSufficient: DEPT du cho TEAM va SELF")
    void isScopeSufficient_deptCoversTeamAndSelf() throws Exception {
        Method method = ChamCongBUS.class.getDeclaredMethod("isScopeSufficient", DataScope.class, DataScope.class);
        method.setAccessible(true);

        boolean teamAllowed = (boolean) method.invoke(ChamCongBUS.getInstance(), DataScope.DEPT, DataScope.TEAM);
        boolean selfAllowed = (boolean) method.invoke(ChamCongBUS.getInstance(), DataScope.DEPT, DataScope.SELF);

        assertTrue(teamAllowed);
        assertTrue(selfAllowed);
    }

    @Test
    @DisplayName("isScopeSufficient: TEAM khong du cho DEPT")
    void isScopeSufficient_teamDoesNotCoverDept() throws Exception {
        Method method = ChamCongBUS.class.getDeclaredMethod("isScopeSufficient", DataScope.class, DataScope.class);
        method.setAccessible(true);

        boolean result = (boolean) method.invoke(ChamCongBUS.getInstance(), DataScope.TEAM, DataScope.DEPT);

        assertFalse(result);
    }

    @Test
    @DisplayName("validateSelfActionPermission: SELF scope cho phep thao tac tren chinh minh")
    void validateSelfActionPermission_selfScope_allowed() throws Exception {
        SessionContext.getInstance().setCurrentUser(createUserWithScope(
                "NV010", PermissionCodes.ATTENDANCE_CHECKIN, DataScope.SELF
        ));

        Method method = ChamCongBUS.class.getDeclaredMethod(
                "validateSelfActionPermission", String.class, String.class
        );
        method.setAccessible(true);
        KetQua<?> result = (KetQua<?>) method.invoke(
                ChamCongBUS.getInstance(),
                PermissionCodes.ATTENDANCE_CHECKIN,
                "NV010"
        );

        assertTrue(result.isSuccess());
    }

    @Test
    @DisplayName("validateSelfActionPermission: SELF scope chan thao tac tren nguoi khac")
    void validateSelfActionPermission_otherEmployee_blocked() throws Exception {
        SessionContext.getInstance().setCurrentUser(createUserWithScope(
                "NV010", PermissionCodes.ATTENDANCE_CHECKIN, DataScope.SELF
        ));

        Method method = ChamCongBUS.class.getDeclaredMethod(
                "validateSelfActionPermission", String.class, String.class
        );
        method.setAccessible(true);
        KetQua<?> result = (KetQua<?>) method.invoke(
                ChamCongBUS.getInstance(),
                PermissionCodes.ATTENDANCE_CHECKIN,
                "NV011"
        );

        assertFalse(result.isSuccess());
        assertEquals("Bạn chỉ được thao tác trên dữ liệu của chính mình.", result.getMessage());
    }

    private TaiKhoan createUserWithScope(String maNV, String permissionId, DataScope scope) {
        TaiKhoan user = new TaiKhoan();
        user.setTenDangNhap("scope.tester");
        user.setNhanVienId(maNV);
        VaiTro role = new VaiTro("TEST_SCOPE", "Test scope", null);
        Quyen permission = new Quyen(permissionId, permissionId, "TEST");
        permission.setPhamVi(scope);
        role.themQuyen(permission);
        user.themVaiTro(role);
        return user;
    }
}
