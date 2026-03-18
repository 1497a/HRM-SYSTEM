package com.hrm.bus;

import com.hrm.model.NhanVien;
import com.hrm.model.Quyen;
import com.hrm.model.TaiKhoan;
import com.hrm.model.VaiTro;
import com.hrm.util.SessionContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NhanVienBUSRuleTest {

    @AfterEach
    void tearDown() {
        SessionContext.getInstance().clearSession();
    }

    @Test
    @DisplayName("validateTrangThaiTransition: nhan vien da nghi viec thi khong duoc phuc hoi")
    void validateTrangThaiTransition_quitEmployee_blocked() throws Exception {
        Method method = NhanVienBUS.class.getDeclaredMethod(
                "validateTrangThaiTransition", String.class, String.class
        );
        method.setAccessible(true);

        KetQua<?> result = (KetQua<?>) method.invoke(
                NhanVienBUS.getInstance(),
                "nghi_viec",
                "dang_lam_viec"
        );

        assertFalse(result.isSuccess());
        assertEquals("Nhan vien da nghi viec, khong the thay doi trang thai.", result.getMessage());
    }

    @Test
    @DisplayName("validateStatusPermission: chan tu doi trang thai cua chinh minh")
    void validateStatusPermission_selfStatusChange_blocked() throws Exception {
        TaiKhoan user = new TaiKhoan();
        user.setTenDangNhap("nv001");
        user.setNhanVienId("NV001");
        VaiTro role = new VaiTro("TRUONG_PHONG_NS", "TP NS", null);
        role.themQuyen(new Quyen("EMPLOYEE_STATUS_UPDATE", "Cap nhat trang thai", "EMPLOYEE"));
        user.themVaiTro(role);
        SessionContext.getInstance().setCurrentUser(user);

        NhanVien target = new NhanVien();
        target.setMaNhanVien("NV001");

        Method method = NhanVienBUS.class.getDeclaredMethod("validateStatusPermission", NhanVien.class);
        method.setAccessible(true);
        KetQua<?> result = (KetQua<?>) method.invoke(NhanVienBUS.getInstance(), target);

        assertFalse(result.isSuccess());
        assertEquals("Khong the tu doi trang thai cua chinh minh.", result.getMessage());
    }

    @Test
    @DisplayName("validateStatusPermission: co quyen update thi pass voi nhan vien khac")
    void validateStatusPermission_authorizedOtherEmployee_allowed() throws Exception {
        TaiKhoan user = new TaiKhoan();
        user.setTenDangNhap("tp.ns");
        user.setNhanVienId("NV002");
        VaiTro role = new VaiTro("TRUONG_PHONG_NS", "TP NS", null);
        role.themQuyen(new Quyen("EMPLOYEE_STATUS_UPDATE", "Cap nhat trang thai", "EMPLOYEE"));
        user.themVaiTro(role);
        SessionContext.getInstance().setCurrentUser(user);

        NhanVien target = new NhanVien();
        target.setMaNhanVien("NV003");

        Method method = NhanVienBUS.class.getDeclaredMethod("validateStatusPermission", NhanVien.class);
        method.setAccessible(true);
        KetQua<?> result = (KetQua<?>) method.invoke(NhanVienBUS.getInstance(), target);

        assertTrue(result.isSuccess());
    }
}
