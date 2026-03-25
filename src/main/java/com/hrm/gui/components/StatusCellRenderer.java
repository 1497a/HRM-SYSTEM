package com.hrm.gui.components;

import com.hrm.util.UIColors;
import com.hrm.util.UIFonts;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * Shared status cell renderer used across all modules.
 * Uses background color to indicate status: green=success, red=danger, yellow=warning.
 */
public class StatusCellRenderer extends DefaultTableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {

        Component c = super.getTableCellRendererComponent(
                table, value, isSelected, hasFocus, row, column);

        setHorizontalAlignment(SwingConstants.CENTER);
        setFont(UIFonts.BOLD_SMALL);
        setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));

        if (isSelected) {
            c.setBackground(UIColors.LIGHT_PURPLE);
            c.setForeground(UIColors.PRIMARY_PURPLE);
        } else {
            c.setForeground(UIColors.TEXT_DARK);
            c.setBackground(resolveBackground(value));
        }

        return c;
    }

    private Color resolveBackground(Object value) {
        if (value == null) return Color.WHITE;

        String v = value.toString();

        // SUCCESS
        if (v.contains("Hiệu lực")
                || v.contains("Hoạt động")
                || v.contains("Đã duyệt")
                || v.contains("Đã khóa")
                || v.contains("Đang diễn ra")
                || v.contains("Trúng")
                || v.contains("Đã tuyển đủ")
                || v.contains("Đã chuyển")
                || v.contains("Đang tuyển")
                || v.contains("Đang làm việc")
                || v.contains("Đúng giờ")) {
            return UIColors.BG_SUCCESS;
        }

        // DANGER
        if (v.contains("Từ chối")
                || v.contains("Hết hạn")
                || v.contains("Hết hiệu lực")
                || v.contains("Thanh lý")
                || v.contains("Bị khóa")
                || v.contains("Đã đóng")
                || v.contains("Ngừng hoạt động")
                || v.contains("Đã kết thúc")
                || v.contains("Nghỉ việc")
                || v.contains("Đã hủy")
                || v.contains("muộn")
                || v.contains("sớm")
                || v.contains("Vắng")) {
            return UIColors.BG_DANGER;
        }

        // WARNING
        if (v.contains("Chờ duyệt")
                || v.contains("Tạm dừng")
                || v.contains("Tạm nghỉ")
                || v.contains("Chưa bắt đầu")
                || v.contains("Chưa")
                || v.contains("Đang phỏng vấn")
                || v.contains("Đã tính")
                || v.contains("Sắp hết hạn")
                || v.contains("Đang xử lý")
                || v.equals("Mới")) {
            return UIColors.BG_WARNING;
        }

        return Color.WHITE;
    }
}