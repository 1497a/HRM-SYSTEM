package com.hrm.gui;

import com.hrm.bus.KetQua;
import com.hrm.bus.XacThucBUS;
import com.hrm.gui.components.PurpleButton;
import com.hrm.gui.components.RoundedPanel;
import com.hrm.model.TaiKhoan;
import com.hrm.util.DialogUtil;
import com.hrm.util.UIColors;
import com.hrm.util.UIFonts;
import com.hrm.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.Font;

/**
 * Panel cai dat tai khoan - cho phep doi ten dang nhap va doi mat khau.
 */
class SettingsPanel extends JPanel {

    private final XacThucBUS authService;
    SettingsPanel(XacThucBUS authService) {
        this.authService = authService;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(25, 25, 25, 25));
        JLabel title = new JLabel("Cài đặt tài khoản");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(UIColors.TEXT_DARK);
        add(title, BorderLayout.NORTH);
        JPanel center = new JPanel(new FlowLayout(FlowLayout.LEFT));
        center.setOpaque(false);
        center.setBorder(new EmptyBorder(25, 0, 0, 0));
        center.add(buildUsernameCard());
        center.add(buildPasswordCard());
        add(center, BorderLayout.CENTER);
    }

    private RoundedPanel buildUsernameCard() {
        RoundedPanel card = RoundedPanel.createFlatCard();
        card.setLayout(new GridBagLayout());
        card.setBorder(new EmptyBorder(25, 25, 25, 25));

        GridBagConstraints gbc = UIHelper.gbc(0, 0);
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.gridwidth = 2;

        JLabel heading = new JLabel("Đổi tên đăng nhập");
        heading.setFont(UIFonts.HEADER_SUB);
        heading.setForeground(UIColors.PRIMARY_PURPLE);
        card.add(heading, gbc);

        gbc.gridwidth = 1;
        JTextField currentUsername = new JTextField(20);
        currentUsername.setEditable(false);
        currentUsername.setFocusable(false);
        currentUsername.setBackground(new Color(245, 245, 245));

        JTextField newUsername = new JTextField(20);
        JPasswordField currentPassword = new JPasswordField(20);

        TaiKhoan user = authService.getCurrentUser();
        currentUsername.setText(user != null ? user.getTenDangNhap() : "");

        addTextRow(card, gbc, 1, "Tài khoản hiện tại:", currentUsername);
        addTextRow(card, gbc, 2, "Tài khoản mới:", newUsername);
        addPasswordRow(card, gbc, 3, "Mật khẩu hiện tại:", currentPassword);

        PurpleButton changeButton = new PurpleButton("Đổi tên đăng nhập");
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.insets = new Insets(20, 8, 8, 8);
        card.add(changeButton, gbc);
        changeButton.addActionListener(e -> changeUsername(currentUsername, newUsername, currentPassword));
        return card;
    }

    private RoundedPanel buildPasswordCard() {
        RoundedPanel card = RoundedPanel.createFlatCard();
        card.setLayout(new GridBagLayout());
        card.setBorder(new EmptyBorder(25, 25, 25, 25));
        GridBagConstraints gbc = UIHelper.gbc(0, 0);
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.gridwidth = 2;
        JLabel heading = new JLabel("Đổi mật khẩu");
        heading.setFont(UIFonts.HEADER_SUB);
        heading.setForeground(UIColors.PRIMARY_PURPLE);
        card.add(heading, gbc);
        gbc.gridwidth = 1;
        JPasswordField current = new JPasswordField(20);
        JPasswordField next    = new JPasswordField(20);
        JPasswordField confirm = new JPasswordField(20);
        addPasswordRow(card, gbc, 1, "Mật khẩu hiện tại:", current);
        addPasswordRow(card, gbc, 2, "Mật khẩu mới:",      next);
        addPasswordRow(card, gbc, 3, "Xác nhận mật khẩu:", confirm);
        PurpleButton changeButton = new PurpleButton("Đổi mật khẩu");
        gbc.gridx = 1; gbc.gridy = 4;
        gbc.insets = new Insets(20, 8, 8, 8);
        card.add(changeButton, gbc);
        changeButton.addActionListener(e -> changePassword(current, next, confirm));
        return card;
    }

    private void addTextRow(JPanel panel, GridBagConstraints gbc, int row, String labelText, JTextField field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        JLabel label = new JLabel(labelText);
        label.setFont(UIFonts.TEXT_NORMAL);
        panel.add(label, gbc);
        field.setFont(UIFonts.TEXT_NORMAL);
        gbc.gridx = 1;
        panel.add(field, gbc);
    }

    private void addPasswordRow(JPanel panel, GridBagConstraints gbc, int row, String labelText, JPasswordField field) {
        gbc.gridx = 0; gbc.gridy = row;
        JLabel label = new JLabel(labelText);
        label.setFont(UIFonts.TEXT_NORMAL);
        panel.add(label, gbc);
        field.setFont(UIFonts.TEXT_NORMAL);
        gbc.gridx = 1;
        panel.add(field, gbc);
    }

    private void changePassword(JPasswordField current, JPasswordField next, JPasswordField confirm) {
        String currentPass = new String(current.getPassword());
        String newPass     = new String(next.getPassword());
        String confirmPass = new String(confirm.getPassword());
        if (currentPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
            DialogUtil.showWarn(this, "Vui lòng nhập đầy đủ thông tin");
            if (currentPass.isEmpty()) current.requestFocusInWindow();
            else if (newPass.isEmpty()) next.requestFocusInWindow();
            else confirm.requestFocusInWindow();
            return;
        }
        if (!newPass.equals(confirmPass)) {
            DialogUtil.showWarn(this, "Mật khẩu xác nhận không khớp");
            confirm.requestFocusInWindow();
            return;
        }
        TaiKhoan currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            DialogUtil.showError(this, "Không tìm thấy người dùng hiện tại");
            return;
        }
        KetQua<Void> result = authService.changePassword(currentUser.getId(), currentPass, newPass);
        if (result.isSuccess()) {
            DialogUtil.showInfo(this, "Đổi mật khẩu thành công!");
            current.setText(""); next.setText(""); confirm.setText("");
        } else {
            DialogUtil.showError(this, result.getMessage());
        }
    }

    private void changeUsername(JTextField currentUsername, JTextField newUsername, JPasswordField currentPassword) {
        TaiKhoan currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            DialogUtil.showError(this, "Không tìm thấy người dùng hiện tại");
            return;
        }

        String currentName = currentUsername.getText().trim();
        String nextName = newUsername.getText() != null ? newUsername.getText().trim() : "";
        String password = new String(currentPassword.getPassword());

        if (nextName.isEmpty() || password.isEmpty()) {
            DialogUtil.showWarn(this, "Vui lòng nhập đầy đủ thông tin");
            if (nextName.isEmpty()) {
                newUsername.requestFocusInWindow();
            } else {
                currentPassword.requestFocusInWindow();
            }
            return;
        }
        if (nextName.equals(currentName)) {
            DialogUtil.showWarn(this, "Tên đăng nhập mới phải khác tên hiện tại");
            newUsername.requestFocusInWindow();
            newUsername.selectAll();
            return;
        }

        KetQua<Void> result = authService.changeUsername(currentUser.getId(), nextName, password);
        if (!result.isSuccess()) {
            DialogUtil.showError(this, result.getMessage());
            currentPassword.setText("");
            return;
        }

        DialogUtil.showInfo(this, "Đổi tên đăng nhập thành công. Vui lòng đăng nhập lại bằng tên mới.");
        authService.logout();
        Window window = SwingUtilities.getWindowAncestor(this);
        if (window != null) {
            window.dispose();
        }
        new LoginFrame().setVisible(true);
    }
}
