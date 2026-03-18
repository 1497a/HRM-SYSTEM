package com.hrm.util;

import javax.swing.*;
import javax.swing.table.TableRowSorter;
import javax.swing.RowFilter;
import java.awt.*;
import java.awt.Font;
import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * UI Helper - provides styled components that work across all Look and Feels
 */
public class UIHelper {

    /**
     * Create a styled button with proper colors that work with all Look and Feels
     */
    public static JButton createStyledButton(String text, Color bgColor, Color fgColor) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        styleButton(btn, bgColor, fgColor);
        return btn;
    }

    /**
     * Create a primary button (purple – matches app theme)
     */
    public static JButton createPrimaryButton(String text) {
        return createStyledButton(text, UIColors.PRIMARY_PURPLE, Color.WHITE);
    }

    /**
     * Create a success button (green)
     */
    public static JButton createSuccessButton(String text) {
        return createStyledButton(text, UIColors.SUCCESS_GREEN, Color.WHITE);
    }

    /**
     * Create a danger button (red)
     */
    public static JButton createDangerButton(String text) {
        return createStyledButton(text, UIColors.DANGER_RED, Color.WHITE);
    }

    /**
     * Create a warning button (orange)
     */
    public static JButton createWarningButton(String text) {
        return createStyledButton(text, new Color(255, 193, 7), Color.WHITE);
    }

    /**
     * Style an existing button
     */
    public static void styleButton(JButton btn, Color bgColor, Color fgColor) {
        btn.setBackground(bgColor);
        btn.setForeground(fgColor);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        // Add hover effect
        final Color originalBg = bgColor;
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (btn.isEnabled()) {
                    btn.setBackground(bgColor.darker());
                }
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (btn.isEnabled()) {
                    btn.setBackground(originalBg);
                }
            }
        });
    }

    /**
     * Create a default button (gray)
     */
    public static JButton createDefaultButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public static GridBagConstraints gbc(int x, int y) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        return gbc;
    }

    public static GridBagConstraints gbcFill(int x, int y) {
        GridBagConstraints gbc = gbc(x, y);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        return gbc;
    }

    // ── Sorting helpers ───────────────────────────────────────────────────────
    /**
     * Extracts the last word (first name / Tên) from a Vietnamese full name.
     * E.g. "Nguyễn Văn An" → "An"
     */
    public static String getFirstName(String hoTen) {
        if (hoTen == null || hoTen.isBlank()) return "";
        String[] parts = hoTen.trim().split("\\s+");
        return parts[parts.length - 1];
    }

    /**
     * Returns a Vietnamese-locale-aware Comparator that sorts by first name (Tên).
     * Correctly handles accented characters (Á, Â, Ă, etc.).
     */
    @SuppressWarnings("deprecation")
    public static Comparator<Object> vietnameseNameComparator() {
        Collator collator = Collator.getInstance(new Locale("vi", "VN"));
        collator.setStrength(Collator.PRIMARY);
        return (a, b) -> collator.compare(
            getFirstName(a == null ? "" : a.toString()),
            getFirstName(b == null ? "" : b.toString()));
    }

    // ── Filter helpers ────────────────────────────────────────────────────────
    /**
     * Attaches a status RowFilter to a TableRowSorter driven by a JComboBox.
     * Selecting HRMConstants.ALL removes the filter; any other value filters
     * the specified column using a case-insensitive contains match.
     *
     * @param sorter      the TableRowSorter to apply the filter on
     * @param combo       the JComboBox whose selection drives the filter
     * @param columnIndex the table column index to filter against
     */
    public static void attachStatusFilter(TableRowSorter<?> sorter,
                                          JComboBox<String> combo,
                                          int columnIndex) {
        combo.addActionListener(e -> {
            String selected = (String) combo.getSelectedItem();
            if (selected == null || HRMConstants.ALL.equals(selected)) {
                sorter.setRowFilter(null);
            } else {
                sorter.setRowFilter(RowFilter.regexFilter(
                    "(?i)" + Pattern.quote(selected), columnIndex));
            }
        });
    }
}
