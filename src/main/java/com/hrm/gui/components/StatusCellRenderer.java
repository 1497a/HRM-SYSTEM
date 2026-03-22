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
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
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
        // SUCCESS: active / approved / valid / finalized / on-time states
        if (v.contains("Hi\u1ec7u l\u1ef1c")               // Hiệu lực
                || v.contains("Ho\u1ea1t \u0111\u1ed9ng")   // Hoạt động
                || v.contains("\u0110\u00e3 duy\u1ec7t")    // Đã duyệt
                || v.contains("\u0110\u00e3 kh\u00f3a")     // Đã khóa
                || v.contains("\u0110ang di\u1ec5n ra")      // Đang diễn ra
                || v.contains("Tr\u00fang")                  // Trúng (Tuyển)
                || v.contains("\u0110\u00e3 tuy\u1ec3n \u0111\u1ee7")   // Đã tuyển đủ
                || v.contains("\u0110\u00e3 chuy\u1ec3n")   // Đã chuyển (NV / thành nhân viên)
                || v.contains("\u0110ang tuy\u1ec3n")        // Đang tuyển
                || v.contains("\u0110ang l\u00e0m vi\u1ec7c") // Đang làm việc
                || v.contains("\u0110\u00fang gi\u1edd")) {  // Đúng giờ (chấm công)
            return UIColors.BG_SUCCESS;
        }
        // DANGER: rejected / expired / disabled / terminated / violation states
        if (v.contains("T\u1eeb ch\u1ed1i")                 // Từ chối
                || v.contains("H\u1ebft h\u1ea1n")           // Hết hạn
                || v.contains("H\u1ebft hi\u1ec7u l\u1ef1c") // Hết hiệu lực
                || v.contains("Thanh l\u00fd")               // Thanh lý
                || v.contains("B\u1ecb kh\u00f3a")           // Bị khóa
                || v.contains("\u0110\u00e3 \u0111\u00f3ng") // Đã đóng
                || v.contains("Ng\u1eebng ho\u1ea1t \u0111\u1ed9ng") // Ngừng hoạt động
                || v.contains("\u0110\u00e3 k\u1ebft th\u00fac")      // Đã kết thúc
                || v.contains("Ngh\u1ec9 vi\u1ec7c")         // Nghỉ việc
                || v.contains("\u0110\u00e3 h\u1ee7y")       // Đã hủy
                || v.contains("mu\u1ed9n")                   // muộn (đi muộn - chấm công)
                || v.contains("s\u1edbm")                    // sớm (về sớm - chấm công)
                || v.contains("V\u1eafng")) {                // Vắng (chấm công)
            return UIColors.BG_DANGER;
        }
        // WARNING: pending / in-progress / not-started / temporary states
        if (v.contains("Ch\u1edd duy\u1ec7t")               // Chờ duyệt
                || v.contains("T\u1ea1m d\u1eebng")          // Tạm dừng
                || v.contains("T\u1ea1m ngh\u1ec9")          // Tạm nghỉ
                || v.contains("Ch\u01b0a b\u1eaft \u0111\u1ea7u") // Chưa bắt đầu
                || v.contains("Ch\u01b0a")                   // Chưa (Chưa chấm công, etc.)
                || v.contains("\u0110ang ph\u1ecfng v\u1ea5n")     // Đang phỏng vấn
                || v.contains("\u0110\u00e3 t\u00ednh")       // Đã tính
                || v.contains("S\u1eafp h\u1ebft h\u1ea1n")  // Sắp hết hạn
                || v.contains("\u0110ang x\u1eed l\u00fd")    // Đang xử lý
                || v.equals("M\u1edbi")) {                    // Mới (ứng viên mới)
            return UIColors.BG_WARNING;
        }
        return Color.WHITE;
    }
}
