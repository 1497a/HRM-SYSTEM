package com.hrm.bus;

import com.hrm.model.BoNhiem;
import com.hrm.model.TaiKhoan;
import com.hrm.model.VaiTro;
import com.hrm.util.SessionContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class BoNhiemBUSValidationTest {

    @AfterEach
    void tearDown() {
        SessionContext.getInstance().clearSession();
    }

    @Test
    @DisplayName("taoBoNhiem: chan tu bo nhiem chinh minh")
    void taoBoNhiem_selfAction_blocked() {
        TaiKhoan user = new TaiKhoan();
        user.setTenDangNhap("lananh.dang");
        user.setNhanVienId("NV003");
        user.themVaiTro(new VaiTro("NHAN_SU", "Nhan su", null));
        SessionContext.getInstance().setCurrentUser(user);

        BoNhiem boNhiem = new BoNhiem();
        boNhiem.setNhanVienId("NV003");

        KetQua<BoNhiem> result = BoNhiemBUS.getInstance().taoBoNhiem(boNhiem);

        assertFalse(result.isSuccess());
        assertEquals("Bạn không thể tự tạo bổ nhiệm cho chính mình.", result.getMessage());
    }

    @Test
    @DisplayName("tuChoiBoNhiem: ly do rong thi bi chan")
    void tuChoiBoNhiem_blankReason_blocked() {
        KetQua<BoNhiem> result = BoNhiemBUS.getInstance().tuChoiBoNhiem(1, " ");

        assertFalse(result.isSuccess());
        assertEquals("Lý do từ chối không được để trống.", result.getMessage());
    }
}
