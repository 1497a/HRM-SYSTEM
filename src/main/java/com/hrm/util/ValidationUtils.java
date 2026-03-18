package com.hrm.util;

import java.time.LocalDate;
import java.util.regex.Pattern;

/**
 * Centralized input validation utilities.
 * All methods return null when input is valid (or blank/optional),
 * and a Vietnamese error message string when invalid.
 */
public final class ValidationUtils {

    private static final int DEFAULT_TEXT_LIMIT = 255;
    private static final int NOTIFICATION_TITLE_LIMIT = 200;
    private static final int NOTIFICATION_CONTENT_LIMIT = 4000;
    private ValidationUtils() {}

    public static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private static final Pattern EMAIL = Pattern.compile(
        "^[\\w.+\\-]+@[\\w\\-]+(\\.[\\w\\-]+)*\\.[a-zA-Z]{2,}$");
    private static final Pattern PHONE = Pattern.compile("^0\\d{9,10}$");
    /**
     * Validates email format. Blank/null is treated as valid (optional field).
     * @return null if valid, error message if invalid
     */
    public static String validateEmail(String email) {
        if (email == null || email.isBlank()) return null;
        return EMAIL.matcher(email.trim()).matches()
            ? null
            : "Email không hợp lệ (ví dụ: example@gmail.com)";
    }

    /**
     * Validates phone number format. Must start with 0, 10–11 digits total.
     * Blank/null is treated as valid (optional field).
     * @return null if valid, error message if invalid
     */
    public static String validatePhone(String phone) {
        if (phone == null || phone.isBlank()) return null;
        return PHONE.matcher(phone.trim()).matches()
            ? null
            : "Số điện thoại không hợp lệ (10–11 chữ số, bắt đầu bằng 0)";
    }

    /**
     * Validates that birth date is a date in the past (not today, not future).
     * @return null if valid, error message if invalid or null
     */
    public static String validateBirthDate(LocalDate date) {
        if (date == null) return "Ngày sinh không hợp lệ";
        return date.isBefore(LocalDate.now())
            ? null
            : "Ngày sinh phải là ngày trong quá khứ";
    }
    public static String validateRequired(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            return fieldName + " không được để trống.";
        }
        return null;
    }

    public static String validateMaxLength(String value, int maxLength, String fieldName) {
        if (value == null || value.isBlank()) return null;
        return value.trim().length() <= maxLength
            ? null
            : fieldName + " không được vượt quá " + maxLength + " ký tự.";
    }

    public static String validatePositive(long value, String fieldName) {
        return value > 0 ? null : fieldName + " phải lớn hơn 0.";
    }

    public static String validateNotificationTitle(String title) {
        String required = validateRequired(title, "Tiêu đề thông báo");
        if (required != null) return required;
        return validateMaxLength(title, NOTIFICATION_TITLE_LIMIT, "Tiêu đề thông báo");
    }

    public static String validateNotificationContent(String content) {
        String required = validateRequired(content, "Nội dung thông báo");
        if (required != null) return required;
        return validateMaxLength(content, NOTIFICATION_CONTENT_LIMIT, "Nội dung thông báo");
    }

    public static String validateFreeText(String value, String fieldName) {
        return validateMaxLength(value, DEFAULT_TEXT_LIMIT, fieldName);
    }
}
