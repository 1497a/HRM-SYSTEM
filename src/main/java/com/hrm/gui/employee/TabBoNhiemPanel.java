package com.hrm.gui.employee;

import com.hrm.gui.components.PurpleTable;
import com.hrm.model.BoNhiem;
import com.hrm.bus.BoNhiemBUS;
import com.hrm.util.UIColors;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Tab 2 cua EmployeeDetailPanel: Bo nhiem hien tai + lich su bo nhiem.
 */
class TabBoNhiemPanel extends JPanel {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String[] COLS = {"Phong ban", "Chuc vu", "Loai", "Tu ngay", "Den ngay", "Trang thai"};

    TabBoNhiemPanel(BoNhiem boNhiemHienTai, BoNhiemBUS boNhiemService, String maNV) {
        setLayout(new BorderLayout(0, 12));
        setBackground(UIColors.WHITE);
        setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        // Current appointment section
        JPanel currentPanel = new JPanel(new BorderLayout(0, 8));
        currentPanel.setBackground(UIColors.WHITE);
        currentPanel.add(buildSectionTitle("Bo nhiem hien tai"), BorderLayout.NORTH);

        JPanel detailGrid = buildInfoGrid();
        if (boNhiemHienTai != null) {
            String tenPB = boNhiemHienTai.getTenPhongBan() != null ? boNhiemHienTai.getTenPhongBan() : safe(String.valueOf(boNhiemHienTai.getId()));
            String tenCV = boNhiemHienTai.getTenChucVu()   != null ? boNhiemHienTai.getTenChucVu()   : safe(String.valueOf(boNhiemHienTai.getId()));
            addInfoRow(detailGrid, 0, "Phong ban:",         tenPB);
            addInfoRow(detailGrid, 1, "Chuc vu:",           tenCV);
            addInfoRow(detailGrid, 2, "Loai bo nhiem:",     safe(boNhiemHienTai.getLoaiBoNhiemDisplay()));
            addInfoRow(detailGrid, 3, "Ty le huong luong:", boNhiemHienTai.getTyLeHuongLuong() + "%");
            addInfoRow(detailGrid, 4, "Tu ngay:",
                    boNhiemHienTai.getTuNgay() != null ? boNhiemHienTai.getTuNgay().format(DATE_FMT) : "");
            addInfoRow(detailGrid, 5, "Den ngay:",
                    boNhiemHienTai.getDenNgay() != null ? boNhiemHienTai.getDenNgay().format(DATE_FMT) : "Khong xac dinh");
            addInfoRow(detailGrid, 6, "Trang thai:",
                    buildStatusLabel(boNhiemHienTai.getTrangThai(), boNhiemHienTai.getTrangThaiDisplay()));
        } else {
            JLabel noData = new JLabel("  Chua co bo nhiem chinh hieu luc.");
            noData.setFont(new Font("Segoe UI", Font.ITALIC, 13));
            noData.setForeground(UIColors.TEXT_GRAY);
            detailGrid.add(noData, buildGbc(0, 0, 2));
        }
        currentPanel.add(detailGrid, BorderLayout.CENTER);
        add(currentPanel, BorderLayout.NORTH);

        // Appointment history table
        JPanel historyPanel = new JPanel(new BorderLayout(0, 6));
        historyPanel.setBackground(UIColors.WHITE);
        historyPanel.add(buildSectionTitle("Lich su bo nhiem"), BorderLayout.NORTH);

        DefaultTableModel tableModel = PurpleTable.createNonEditableModel(COLS);
        loadHistory(tableModel, boNhiemService, maNV);

        PurpleTable table = new PurpleTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setDefaultRenderer(Object.class, new BoNhiemStatusRenderer());

        int[] colWidths = {130, 130, 90, 90, 90, 90};
        for (int i = 0; i < colWidths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(colWidths[i]);
        }

        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setBorder(BorderFactory.createLineBorder(UIColors.BORDER_GRAY));
        tableScroll.setPreferredSize(new Dimension(700, 200));
        historyPanel.add(tableScroll, BorderLayout.CENTER);
        add(historyPanel, BorderLayout.CENTER);
    }

    private void loadHistory(DefaultTableModel model, BoNhiemBUS service, String maNV) {
        try {
            List<BoNhiem> list = service.getByMaNV(maNV);
            if (list == null) return;
            for (BoNhiem bn : list) {
                String tenPB = bn.getTenPhongBan() != null ? bn.getTenPhongBan() : safe(bn.getChucVuId());
                String tenCV = bn.getTenChucVu()   != null ? bn.getTenChucVu()   : safe(bn.getChucVuId());
                model.addRow(new Object[]{
                    tenPB, tenCV, safe(bn.getLoaiBoNhiemDisplay()),
                    bn.getTuNgay() != null ? bn.getTuNgay().format(DATE_FMT) : "",
                    bn.getDenNgay() != null ? bn.getDenNgay().format(DATE_FMT) : "Khong xac dinh",
                    safe(bn.getTrangThaiDisplay())
                });
            }
        } catch (Exception ignored) {}
    }

    // ---- UI helpers ----

    private JLabel buildSectionTitle(String text) {
        JLabel lbl = new JLabel(text.toUpperCase()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(UIColors.PRIMARY_PURPLE);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawLine(0, getHeight() - 2, getWidth(), getHeight() - 2);
            }
        };
        lbl.setFont(com.hrm.util.UIFonts.BOLD_NORMAL);
        lbl.setForeground(UIColors.PRIMARY_PURPLE);
        lbl.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private JPanel buildInfoGrid() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(UIColors.WHITE);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        return p;
    }

    private void addInfoRow(JPanel grid, int row, String labelText, String valueText) {
        JLabel val = new JLabel(valueText);
        val.setFont(com.hrm.util.UIFonts.TEXT_NORMAL);
        val.setForeground(UIColors.TEXT_DARK);
        addInfoRow(grid, row, labelText, (Component) val);
    }

    private void addInfoRow(JPanel grid, int row, String labelText, Component valueComponent) {
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(com.hrm.util.UIFonts.BOLD_NORMAL);
        lbl.setForeground(UIColors.TEXT_GRAY);
        lbl.setPreferredSize(new Dimension(170, 24));

        GridBagConstraints gl = new GridBagConstraints();
        gl.gridx = 0; gl.gridy = row; gl.anchor = GridBagConstraints.WEST;
        gl.insets = new Insets(3, 0, 3, 12);

        GridBagConstraints gv = new GridBagConstraints();
        gv.gridx = 1; gv.gridy = row; gv.anchor = GridBagConstraints.WEST;
        gv.insets = new Insets(3, 0, 3, 0); gv.fill = GridBagConstraints.HORIZONTAL; gv.weightx = 1.0;

        grid.add(lbl, gl);
        grid.add(valueComponent, gv);
    }

    private GridBagConstraints buildGbc(int col, int row, int colspan) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = col; gbc.gridy = row; gbc.gridwidth = colspan;
        gbc.anchor = GridBagConstraints.WEST; gbc.insets = new Insets(4, 0, 4, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        return gbc;
    }

    private JLabel buildStatusLabel(String statusKey, String displayText) {
        Color bg = TabThongTinCaNhanPanel.resolveColor(statusKey);
        JLabel lbl = new JLabel("  " + safe(displayText) + "  ") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground()); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose(); super.paintComponent(g);
            }
        };
        lbl.setOpaque(false); lbl.setFont(com.hrm.util.UIFonts.BOLD_SMALL);
        lbl.setForeground(UIColors.WHITE); lbl.setBackground(bg);
        lbl.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
        return lbl;
    }

    private static String safe(String s) { return (s != null && !s.isEmpty()) ? s : ""; }

    private static class BoNhiemStatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int col) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
            setHorizontalAlignment(SwingConstants.CENTER);
            setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
            if (!isSelected) {
                c.setBackground(row % 2 == 0 ? UIColors.WHITE : UIColors.TABLE_ALT_ROW);
                c.setForeground(UIColors.TEXT_DARK);
                if (col == 5 && value != null) {
                    String v = value.toString();
                    if (v.contains("Hieu luc") || v.contains("hieu_luc")) c.setForeground(UIColors.SUCCESS_GREEN);
                    else if (v.contains("Cho duyet") || v.contains("cho_duyet")) c.setForeground(UIColors.WARNING_TEXT_AMBER);
                    else if (v.contains("Het") || v.contains("Tu choi")) c.setForeground(UIColors.DANGER_RED);
                    ((JLabel) c).setFont(com.hrm.util.UIFonts.BOLD_SMALL);
                }
            }
            return c;
        }
    }
}
