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
import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ChamCongBUSOvertimeTest {

    @AfterEach
    void tearDown() {
        SessionContext.getInstance().clearSession();
    }

    @Test
    @DisplayName("validateOvertimeInput: chan OT duoi 0.5 gio")
    void validateOvertimeInput_tooShort_blocked() throws Exception {
        Method method = ChamCongBUS.class.getDeclaredMethod("validateOvertimeInput", double.class, String.class);
        method.setAccessible(true);

        KetQua<?> result = (KetQua<?>) method.invoke(ChamCongBUS.getInstance(), 0.4, "Tang ca");

        assertFalse(result.isSuccess());
        assertEquals("Số giờ OT phải từ 0.5 đến 8 giờ.", result.getMessage());
    }

    @Test
    @DisplayName("validateOvertimeInput: chan ly do rong")
    void validateOvertimeInput_blankReason_blocked() throws Exception {
        Method method = ChamCongBUS.class.getDeclaredMethod("validateOvertimeInput", double.class, String.class);
        method.setAccessible(true);

        KetQua<?> result = (KetQua<?>) method.invoke(ChamCongBUS.getInstance(), 2.0, " ");

        assertFalse(result.isSuccess());
        assertEquals("Vui lòng nhập lý do làm thêm.", result.getMessage());
    }

    @Test
    @DisplayName("taoDonLamThem theo khoang gio: chan khi thoi gian OT vuot 8 gio")
    void taoDonLamThem_rangeTooLong_blocked() {
        SessionContext.getInstance().setCurrentUser(createOvertimeRequester("NV010"));

        KetQua<?> result = ChamCongBUS.getInstance().taoDonLamThem(
                "NV010",
                LocalDate.now().plusDays(1),
                LocalTime.of(18, 0),
                LocalTime.of(3, 0),
                "Tang ca release"
        );

        assertFalse(result.isSuccess());
        assertEquals("Khoảng thời gian OT phải từ 0.5 đến 8 giờ.", result.getMessage());
    }

    @Test
    @DisplayName("taoDonLamThem theo khoang gio: cho phep tinh OT qua nua dem")
    void taoDonLamThem_crossMidnight_validHours() {
        assertEquals(4.0, com.hrm.model.DangKyLamThem.tinhSoGioOT(
                LocalTime.of(22, 0),
                LocalTime.of(2, 0)
        ));
    }

    private TaiKhoan createOvertimeRequester(String maNV) {
        TaiKhoan user = new TaiKhoan();
        user.setTenDangNhap("ot.tester");
        user.setNhanVienId(maNV);
        VaiTro role = new VaiTro("TEST_OT", "Test OT", null);
        Quyen permission = new Quyen(PermissionCodes.OVERTIME_REQUEST, "OT request", "OT");
        permission.setPhamVi(DataScope.SELF);
        role.themQuyen(permission);
        user.themVaiTro(role);
        return user;
    }
}
