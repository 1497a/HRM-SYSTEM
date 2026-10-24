package com.hrm.gui.salary;

import com.hrm.model.BangLuong;
import com.hrm.util.UIColors;
import com.hrm.util.UIFonts;
import com.hrm.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BangLuongDetailDialog extends JDialog {

    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public BangLuongDetailDialog(Frame owner, BangLuong bl) {
        super(owner, "Chi tiết bảng lương", true);
        setLayout(new BorderLayout());
        setMinimumSize(new Dimension(480, 360));

        add(buildInfoPanel(bl), BorderLayout.CENTER);
        add(buildButtonPanel(), BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(owner);
    }

    private JPanel buildInfoPanel(BangLuong bl) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(16, 20, 8, 20));

        GridBagConstraints lc = new GridBagConstraints();
        lc.anchor = GridBagConstraints.WEST;
        lc.insets = new Insets(5, 0, 5, 12);
        lc.gridx = 0;

        GridBagConstraints vc = new GridBagConstraints();
        vc.anchor = GridBagConstraints.WEST;
        vc.insets = new Insets(5, 0, 5, 0);
        vc.gridx = 1;
        vc.fill = GridBagConstraints.HORIZONTAL;
        vc.weightx = 1.0;

        int row = 0;

        // Mã BL
        addRow(panel, lc, vc, row++, "Mã bảng lương:", String.valueOf(bl.getMaBL()), null);

        // Kỳ lương
        addRow(panel, lc, vc, row++, "Kỳ lương:", bl.getThang() + "/" + bl.getNam(), null);

        // Tên bảng lương
        addRow(panel, lc, vc, row++, "Tên bảng lương:", safeText(bl.getTenBangLuong()), null);

        // Ngày tạo + người tạo
        addRow(panel, lc, vc, row++, "Ngày tạo:", formatDateTime(bl.getNgayTao()), null);
        addRow(panel, lc, vc, row++, "Người tạo:", safeText(bl.getNguoiTao()), null);

        // Trạng thái
        Color statusColor = getStatusColor(bl.getTrangThai());
        addRow(panel, lc, vc, row++, "Trạng thái:", bl.getTrangThai() != null ? bl.getTrangThai().toString() : "", statusColor);

        // Người duyệt / ngày duyệt (nếu có)
        if (bl.getNguoiDuyet() != null || bl.getNgayDuyet() != null) {
            addRow(panel, lc, vc, row++, "Người duyệt:", safeText(bl.getNguoiDuyet()), null);
            addRow(panel, lc, vc, row++, "Ngày duyệt:", formatDateTime(bl.getNgayDuyet()), null);
        }

        // Người khóa / ngày khóa (nếu có)
        if (bl.getNguoiKhoa() != null || bl.getNgayKhoa() != null) {
            addRow(panel, lc, vc, row++, "Người khóa:", safeText(bl.getNguoiKhoa()), null);
            addRow(panel, lc, vc, row++, "Ngày khóa:", formatDateTime(bl.getNgayKhoa()), null);
        }

        // Ghi chú (nếu có)
        if (bl.getGhiChu() != null && !bl.getGhiChu().isBlank()) {
            addRow(panel, lc, vc, row++, "Ghi chú:", bl.getGhiChu(), null);
        }

        return panel;
    }

    private void addRow(JPanel panel, GridBagConstraints lc, GridBagConstraints vc,
                        int row, String label, String value, Color valueColor) {
        lc.gridy = row;
        JLabel lblLabel = new JLabel(label);
        lblLabel.setFont(UIFonts.TEXT_NORMAL);
        lblLabel.setForeground(Color.GRAY);
        panel.add(lblLabel, lc);

        vc.gridy = row;
        JLabel lblValue = new JLabel(value);
        lblValue.setFont(UIFonts.BOLD_NORMAL);
        if (valueColor != null) {
            lblValue.setForeground(valueColor);
        }
        panel.add(lblValue, vc);
    }

    private JPanel buildButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 8));
        panel.setBackground(Color.WHITE);
        JButton btnDong = UIHelper.createDefaultButton("Đóng");
        btnDong.addActionListener(e -> dispose());
        panel.add(btnDong);
        return panel;
    }

    private Color getStatusColor(BangLuong.TrangThai trangThai) {
        if (trangThai == null) return null;
        switch (trangThai) {
            case DA_KHOA: return UIColors.DANGER_RED;
            case DA_DUYET: return UIColors.SUCCESS_GREEN;
            case DA_TINH: return UIColors.PRIMARY_PURPLE;
            default: return Color.GRAY;
        }
    }

    private String formatDateTime(LocalDateTime dt) {
        return dt != null ? dt.format(DATE_TIME_FORMAT) : "—";
    }

    private String safeText(String s) {
        return s != null && !s.isBlank() ? s : "—";
    }
}
