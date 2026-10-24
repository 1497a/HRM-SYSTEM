package com.hrm.bus;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NghiPhepBUSValidationTest {

    @Test
    @DisplayName("calculateBusinessDays: bo qua thu bay va chu nhat")
    void calculateBusinessDays_skipWeekend() {
        int result = NghiPhepBUS.getInstance().calculateBusinessDays(
                LocalDate.of(2026, 3, 20),
                LocalDate.of(2026, 3, 24)
        );

        assertEquals(3, result);
    }

    @Test
    @DisplayName("createRequest: chan ma nhan vien rong")
    void createRequest_blankEmployeeId_blocked() {
        KetQua<?> result = NghiPhepBUS.getInstance().createRequest(
                " ",
                "Nhan vien A",
                "AL",
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(2),
                "Phep nam"
        );

        assertFalse(result.isSuccess());
        assertEquals("Mã nhân viên không hợp lệ.", result.getMessage());
    }

    @Test
    @DisplayName("createRequest: chan ngay bat dau trong qua khu")
    void createRequest_pastStartDate_blocked() {
        KetQua<?> result = NghiPhepBUS.getInstance().createRequest(
                "NV001",
                "Nhan vien A",
                "AL",
                LocalDate.now().minusDays(1),
                LocalDate.now().plusDays(1),
                "Phep nam"
        );

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Ngày bắt đầu phải từ hôm nay trở đi."));
    }

    @Test
    @DisplayName("createRequest: chan ly do de trong")
    void createRequest_blankReason_blocked() {
        KetQua<?> result = NghiPhepBUS.getInstance().createRequest(
                "NV001",
                "Nhan vien A",
                "AL",
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(2),
                " "
        );

        assertFalse(result.isSuccess());
        assertEquals("Ly do nghi phep khong duoc de trong.", result.getMessage());
    }
}
