package com.hrm.gui.recruitment;

import com.hrm.util.UIColors;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Comparator;

final class TabUtils {

    private TabUtils() {}

    static JTable buildTable(DefaultTableModel model) {
        JTable t = new JTable(model);
        t.setRowHeight(28);
        t.setFont(com.hrm.util.UIFonts.TEXT_NORMAL);
        t.getTableHeader().setFont(com.hrm.util.UIFonts.BOLD_NORMAL);
        t.getTableHeader().setBackground(UIColors.PRIMARY_PURPLE);
        t.getTableHeader().setForeground(UIColors.TEXT_DARK);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.setSelectionBackground(UIColors.LIGHT_PURPLE);
        t.setSelectionForeground(UIColors.TEXT_DARK);
        return t;
    }

    static void applyColWidths(JTable t, int[] widths) {
        for (int i = 0; i < widths.length && i < t.getColumnCount(); i++) {
            t.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
    }

    static void showError(Component parent, String msg) {
        JOptionPane.showMessageDialog(parent, msg, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    static Comparator<Object> dateComparator() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return Comparator.comparing(o -> {
            if (!(o instanceof String s) || s.trim().isEmpty()) return null;
            try { return LocalDate.parse(s.trim(), fmt); } catch (DateTimeParseException e) { return null; }
        }, Comparator.nullsLast(Comparator.naturalOrder()));
    }
}
