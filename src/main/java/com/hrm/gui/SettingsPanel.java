package com.hrm.gui;

import com.hrm.bus.KetQua;
import com.hrm.bus.XacThucBUS;
import com.hrm.gui.components.PurpleButton;
import com.hrm.gui.components.RoundedPanel;
import com.hrm.model.TaiKhoan;
import com.hrm.util.UIColors;
import com.hrm.util.UIFonts;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Panel cai dat tai khoan - cho phep doi mat khau.
 */
class SettingsPanel extends JPanel {

    private final XacThucBUS authService;

    SettingsPanel(XacThucBUS authService) {
        this.authService = authService;
        setLayout(new BorderLayout());
        setBackground(UIColors.LIGHT_GRAY_BG);
        setBorder(new EmptyBorder(25, 25, 25, 25));

        JLabel title = new JLabel("Cài đặt tài khoản");
        title.setFont(UIFonts.HEADER_H1);
        title.setForeground(UIColors.TEXT_DARK);
        add(title, BorderLayout.NORTH);

        JPanel center = new JPanel(new FlowLayout(FlowLayout.LEFT));
        center.setOpaque(false);
        center.setBorder(new EmptyBorder(25, 0, 0, 0));
        center.add(buildPasswordCard());
        add(center, BorderLayout.CENTER);
    }

    private RoundedPanel buildPasswordCard() {
        RoundedPanel card = RoundedPanel.createFlatCard();
        card.setLayout(new GridBagLayout());
        card.setBorder(new EmptyBorder(25, 25, 25, 25));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;

        JLabel heading = new JLabel("Đổi mật khẩu");
        heading.setFont(UIFonts.HEADER_H3);
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

    private void addPasswordRow(JPanel panel, GridBagConstraints gbc, int row, String labelText, JPasswordField field) {
        gbc.gridx = 0; gbc.gridy = row;
        JLabel label = new JLabel(labelText);
        label.setFont(UIFonts.TEXT_MEDIUM);
        panel.add(label, gbc);
        field.setFont(UIFonts.TEXT_MEDIUM);
        gbc.gridx = 1;
        panel.add(field, gbc);
    }

    private void changePassword(JPasswordField current, JPasswordField next, JPasswordField confirm) {
        String currentPass = new String(current.getPassword());
        String newPass     = new String(next.getPassword());
        String confirmPass = new String(confirm.getPassword());
        if (currentPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin", "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!newPass.equals(confirmPass)) {
            JOptionPane.showMessageDialog(this, "Mật khẩu xác nhận không khớp", "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        TaiKhoan currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy người dùng hiện tại", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        KetQua<Void> result = authService.changePassword(currentUser.getId(), currentPass, newPass);
        if (result.isSuccess()) {
            JOptionPane.showMessageDialog(this, "Đổi mật khẩu thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            current.setText(""); next.setText(""); confirm.setText("");
        } else {
            JOptionPane.showMessageDialog(this, result.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}
