package com.hrm.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ValidationUtilsTest {

    @Test
    @DisplayName("validateEmail: email hop le tra null")
    void validateEmail_valid_returnNull() {
        assertNull(ValidationUtils.validateEmail("user@example.com"));
    }

    @Test
    @DisplayName("validateEmail: email sai tra message")
    void validateEmail_invalid_returnMessage() {
        assertEquals(
                "Email không hợp lệ (ví dụ: example@gmail.com)",
                ValidationUtils.validateEmail("abc")
        );
    }

    @Test
    @DisplayName("validateBirthDate: ngay hom nay la khong hop le")
    void validateBirthDate_today_blocked() {
        assertEquals(
                "Ngày sinh phải là ngày trong quá khứ",
                ValidationUtils.validateBirthDate(LocalDate.now())
        );
    }
}
