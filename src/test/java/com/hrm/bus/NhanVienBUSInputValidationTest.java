package com.hrm.bus;

import com.hrm.model.NhanVien;
import com.hrm.model.ThongTinCaNhan;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NhanVienBUSInputValidationTest {

    @Test
    @DisplayName("taoHoSo: chan ma nhan vien rong")
    void taoHoSo_blankEmployeeId_blocked() {
        NhanVien nv = new NhanVien();
        nv.setMaNhanVien(" ");
        nv.setNgayVaoLam(LocalDate.now());

        ThongTinCaNhan ttcn = new ThongTinCaNhan();
        ttcn.setHoTen("Nhan vien test");
        ttcn.setEmail("test@example.com");
        ttcn.setDienThoai("0912345678");

        KetQua<?> result = NhanVienBUS.getInstance().taoHoSo(nv, ttcn);

        assertFalse(result.isSuccess());
        assertEquals("Ma nhan vien khong duoc de trong.", result.getMessage());
    }

    @Test
    @DisplayName("validateCCCD: chan CCCD khong du 12 so")
    void validateCccd_invalidFormat_blocked() throws Exception {
        Method method = NhanVienBUS.class.getDeclaredMethod("validateCCCD", String.class, String.class);
        method.setAccessible(true);

        KetQua<?> result = (KetQua<?>) method.invoke(NhanVienBUS.getInstance(), "123", "NV999");

        assertFalse(result.isSuccess());
        assertEquals("CCCD phai la 12 chu so.", result.getMessage());
    }

    @Test
    @DisplayName("validateCCCD: CCCD rong duoc bo qua")
    void validateCccd_blank_allowed() throws Exception {
        Method method = NhanVienBUS.class.getDeclaredMethod("validateCCCD", String.class, String.class);
        method.setAccessible(true);

        KetQua<?> result = (KetQua<?>) method.invoke(NhanVienBUS.getInstance(), " ", "NV999");

        assertTrue(result.isSuccess());
    }
}
