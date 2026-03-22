package com.hrm.gui.recruitment;

import com.hrm.gui.components.PurpleTable;
import com.hrm.util.DialogUtil;
import com.hrm.util.UIFonts;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Comparator;

final class TabUtils {

    private TabUtils() {}

    static PurpleTable buildTable(DefaultTableModel model) {
        return new PurpleTable(model);
    }

    static void applyColWidths(JTable t, int[] widths) {
        for (int i = 0; i < widths.length && i < t.getColumnCount(); i++) {
            t.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
    }

    static void showError(Component parent, String msg) {
        DialogUtil.showError(parent, msg);
    }

    static Comparator<Object> dateComparator() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return Comparator.comparing(o -> {
            if (!(o instanceof String s) || s.trim().isEmpty()) return null;
            try { return LocalDate.parse(s.trim(), fmt); } catch (DateTimeParseException e) { return null; }
        }, Comparator.nullsLast(Comparator.naturalOrder()));
    }
}
