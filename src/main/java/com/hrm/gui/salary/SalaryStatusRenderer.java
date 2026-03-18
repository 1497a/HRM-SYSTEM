package com.hrm.gui.salary;

import com.hrm.util.UIColors;
import com.hrm.util.UIFonts;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * Cell renderer to colorize the trang thai column of bang luong table.
 */
class SalaryStatusRenderer extends DefaultTableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int col) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
        setHorizontalAlignment(SwingConstants.CENTER);
        if (!isSelected && value != null) {
            String v = value.toString();
            if (v.contains("Đã khóa")) {
                c.setForeground(UIColors.SUCCESS_GREEN);
            } else if (v.contains("Đã duyệt")) {
                c.setForeground(new Color(230, 120, 0));
            } else if (v.contains("Đã tính")) {
                c.setForeground(new Color(23, 162, 184));
            } else {
                c.setForeground(UIColors.TEXT_DARK);
            }
            ((JLabel) c).setFont(com.hrm.util.UIFonts.BOLD_SMALL);
        }
        return c;
    }
}
