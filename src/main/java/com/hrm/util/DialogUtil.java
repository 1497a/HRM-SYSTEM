package com.hrm.util;

import java.awt.Component;

import javax.swing.JOptionPane;

public final class DialogUtil {

    private DialogUtil() {}

    public static void showSuccess(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Thành công", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showInfo(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showWarn(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Cảnh báo", JOptionPane.WARNING_MESSAGE);
    }

    public static void showError(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }
}
