package com.hrm.util;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.RowFilter;
import java.awt.*;
import java.awt.Font;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.text.Collator;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
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
        return createStyledButton(text, UIColors.ACCENT_YELLOW, Color.WHITE);
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

    // ── Search helpers ─────────────────────────────────────────────────────────

    /**
     * Strips Vietnamese diacritics and lowercases the text for accent-insensitive search.
     * "Nguyễn" and "nguyen" both normalize to "nguyen".
     */
    public static String normalizeSearch(String value) {
        if (value == null) return "";
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .replace("đ", "d")
                .replace("Đ", "D");
        return normalized.trim().toLowerCase();
    }

    /**
     * Creates a JTextField(18) with a placeholder hint text.
     */
    public static JTextField createSearchField(String placeholder) {
        JTextField field = new JTextField(18);
        field.putClientProperty("JTextField.placeholderText", placeholder);
        return field;
    }

    /**
     * Attaches a real-time text search to a TableRowSorter.
     * On each key release the keyword and each cell value are normalized
     * (diacritics stripped, lowercased) before comparison so both accented
     * and unaccented input work.
     *
     * @param field      the search JTextField
     * @param sorter     the TableRowSorter to filter
     * @param searchCols column indices to search across (OR logic)
     */
    public static void attachTextSearch(JTextField field,
                                        TableRowSorter<DefaultTableModel> sorter,
                                        int... searchCols) {
        field.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                final String keyword = normalizeSearch(field.getText());
                if (keyword.isEmpty()) {
                    sorter.setRowFilter(null);
                    return;
                }
                sorter.setRowFilter(new RowFilter<DefaultTableModel, Integer>() {
                    @Override
                    public boolean include(Entry<? extends DefaultTableModel,
                                           ? extends Integer> entry) {
                        for (int col : searchCols) {
                            Object val = entry.getValue(col);
                            if (val != null && normalizeSearch(val.toString()).contains(keyword)) {
                                return true;
                            }
                        }
                        return false;
                    }
                });
            }
        });
    }

    /**
     * Attaches combined text-search + status-combo filter to a TableRowSorter.
     * Replaces {@link #attachStatusFilter}; handles the combo ActionListener internally.
     * Both filters are combined with AND logic.
     *
     * @param searchField search JTextField
     * @param statusCombo status JComboBox (first item = "Tất cả" clears filter)
     * @param sorter      the TableRowSorter to filter
     * @param statusCol   column index for exact status match
     * @param searchCols  column indices to search with normalize (OR across columns)
     */
    public static void attachSearchAndStatusFilter(JTextField searchField,
                                                   JComboBox<String> statusCombo,
                                                   TableRowSorter<DefaultTableModel> sorter,
                                                   int statusCol,
                                                   int[] searchCols) {
        Runnable applyFilter = () -> {
            final String keyword = normalizeSearch(searchField.getText());
            String status = (String) statusCombo.getSelectedItem();
            boolean hasKeyword = !keyword.isEmpty();
            boolean hasStatus = status != null && !HRMConstants.ALL.equals(status);

            if (!hasKeyword && !hasStatus) {
                sorter.setRowFilter(null);
                return;
            }
            List<RowFilter<DefaultTableModel, Object>> filters = new ArrayList<>();
            if (hasKeyword) {
                filters.add(new RowFilter<DefaultTableModel, Object>() {
                    @Override
                    public boolean include(Entry<? extends DefaultTableModel,
                                           ? extends Object> entry) {
                        for (int col : searchCols) {
                            Object val = entry.getValue(col);
                            if (val != null && normalizeSearch(val.toString()).contains(keyword)) {
                                return true;
                            }
                        }
                        return false;
                    }
                });
            }
            if (hasStatus) {
                filters.add(RowFilter.regexFilter(
                        "(?i)" + Pattern.quote(status), statusCol));
            }
            sorter.setRowFilter(filters.size() == 1
                    ? filters.get(0)
                    : RowFilter.andFilter(filters));
        };

        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) { applyFilter.run(); }
        });
        statusCombo.addActionListener(e -> applyFilter.run());
    }
}
