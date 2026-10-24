package com.hrm.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PasswordUtilTest {

    @Test
    @DisplayName("hashPassword + verifyPassword: mat khau dung thi verify true")
    void hashAndVerify_correctPassword_returnTrue() {
        String hash = PasswordUtil.hashPassword("secret123");

        assertTrue(PasswordUtil.verifyPassword("secret123", hash));
    }

    @Test
    @DisplayName("verifyPassword: mat khau sai thi false")
    void verifyPassword_wrongPassword_returnFalse() {
        String hash = PasswordUtil.hashPassword("secret123");

        assertFalse(PasswordUtil.verifyPassword("wrong", hash));
    }

    @Test
    @DisplayName("hashPassword: mat khau rong thi nem exception")
    void hashPassword_blankPassword_throwException() {
        assertThrows(IllegalArgumentException.class, () -> PasswordUtil.hashPassword(""));
    }
}
