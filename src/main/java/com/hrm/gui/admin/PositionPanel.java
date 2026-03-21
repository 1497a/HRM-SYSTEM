package com.hrm.gui.admin;

import com.hrm.bus.ChucVuBUS;
import com.hrm.bus.KetQua;
import com.hrm.gui.components.PurpleButton;
import com.hrm.model.ChucVu;
import com.hrm.model.LichSuHeSoLuong;
import com.hrm.util.DialogUtil;
import com.hrm.util.HRMConstants;
import com.hrm.util.PermissionCodes;
import com.hrm.util.SessionContext;
import com.hrm.util.UIColors;
import com.hrm.util.UIFonts;
import com.hrm.util.UIHelper;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.text.Normalizer;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class PositionPanel extends JPanel {

    private static final String STATUS_ALL      = "Tat ca";
    private static final String STATUS_ACTIVE   = "Hoat dong";
    private static final String STATUS_INACTIVE = "Ngung hoat dong";

    private final ChucVuBUS service = new ChucVuBUS();
    private final NumberFormat moneyFmt = NumberFormat.getNumberInstance(Locale.of("vi", "VN"));

    private JTable table;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    private JTextField txtSearch;
    private JComboBox<String> cboFilter;

    private PurpleButton btnThem;
    private PurpleButton btnSua;
    private PurpleButton btnLichSu;
    private PurpleButton btnLamMoi;

    public PositionPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top panel
        JPanel topPanel = new JPanel(new BorderLayout());
        JLabel title = new JLabel("QUAN LY CHUC VU");
        title.setFont(new Font("Arial", Font.BOLD, 16));
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        topPanel.add(title, BorderLayout.NORTH);

        JLabel lblHint = new JLabel("Tim theo: Ma / Ten chuc vu / Trang thai");
        lblHint.setFont(new Font("Arial", Font.ITALIC, 11));
        topPanel.add(lblHint, BorderLayout.SOUTH);

        JPanel searchFilterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel lblSearch = new JLabel("Tim kiem:");
        txtSearch = new JTextField(20);
        txtSearch.setToolTipText("Nhap ma hoac ten chuc vu de tim kiem");
        JLabel lblFilter = new JLabel("    Trang thai:");
        cboFilter = new JComboBox<>(new String[]{STATUS_ALL, STATUS_ACTIVE, STATUS_INACTIVE});
        searchFilterPanel.add(lblSearch);
        searchFilterPanel.add(txtSearch);
        searchFilterPanel.add(lblFilter);
        searchFilterPanel.add(cboFilter);
        topPanel.add(searchFilterPanel, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

        // Table - 5 cols: Ma, Ten, Cap bac, Phu cap, Trang thai
        tableModel = new DefaultTableModel(
                new Object[]{"Ma CV", "Ten chuc vu", "Cap bac", "Phu cap (VND)", "Trang thai"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(24);
        table.getColumnModel().getColumn(0).setPreferredWidth(70);
        table.getColumnModel().getColumn(1).setPreferredWidth(160);
        table.getColumnModel().getColumn(2).setPreferredWidth(70);
        table.getColumnModel().getColumn(3).setPreferredWidth(110);
        table.getColumnModel().getColumn(4).setPreferredWidth(90);
        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Buttons
        btnThem = new PurpleButton("+ Them");
        btnSua  = new PurpleButton("Sua", UIColors.SUCCESS_GREEN, UIColors.SUCCESS_GREEN.darker(), UIColors.SUCCESS_GREEN.darker());
        btnLichSu = new PurpleButton("Xem lich su he so");
        btnSua.setEnabled(false);
        btnLichSu.setEnabled(false);
        btnLichSu.addActionListener(e -> showHistoryDialog());

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnPanel.add(btnThem);
        btnPanel.add(btnSua);
        btnPanel.add(btnLichSu);
        btnLamMoi = PurpleButton.info("Lam moi");
        btnLamMoi.setToolTipText("Tai lai du lieu va xoa bo loc");
        btnLamMoi.addActionListener(e -> refreshTable());
        btnPanel.add(btnLamMoi);
        add(btnPanel, BorderLayout.SOUTH);

        setupPermissions();

        btnThem.addActionListener(e -> showAddDialog());
        btnSua.addActionListener(e -> showEditDialog());

        txtSearch.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) { applyFilter(); }
        });

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                boolean hasSelection = table.getSelectedRow() != -1;
                btnLichSu.setEnabled(hasSelection);
                btnSua.setEnabled(hasSelection &&
                        SessionContext.getInstance().hasPermission(PermissionCodes.POSITION_MANAGE));
            }
        });

        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) showDetailDialog();
            }
        });

        refreshTable();
        cboFilter.addActionListener(e -> applyFilter());
    }

    private void setupPermissions() {
        boolean canManage = SessionContext.getInstance().hasPermission(PermissionCodes.POSITION_MANAGE);
        btnThem.setVisible(canManage);
        btnSua.setVisible(canManage);
    }

    private boolean isRefreshing = false;

    private void applyFilter() {
        if (isRefreshing) return;
        String searchText = txtSearch.getText().toLowerCase().trim();
        int statusFilterIndex = cboFilter.getSelectedIndex();
        RowFilter<DefaultTableModel, Object> rf = new RowFilter<DefaultTableModel, Object>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ? extends Object> entry) {
                String ma  = entry.getStringValue(0).toLowerCase();
                String ten = entry.getStringValue(1).toLowerCase();
                boolean matchSearch = searchText.isEmpty() || ma.contains(searchText) || ten.contains(searchText);
                // trangThai is at col 4
                String trangThai = normalizeTrangThai(entry.getStringValue(4));
                boolean matchStatus = true;
                if (statusFilterIndex == 1) matchStatus = "hoatdong".equals(trangThai);
                else if (statusFilterIndex == 2) matchStatus = "ngunghoatdong".equals(trangThai) || "ngung".equals(trangThai);
                return matchSearch && matchStatus;
            }
        };
        sorter.setRowFilter(rf);
    }

    private void refreshTable() {
        isRefreshing = true;
        try {
            tableModel.setRowCount(0);
            for (ChucVu p : service.getAllPositions()) {
                tableModel.addRow(new Object[]{
                        p.getId(),
                        p.getTenChucVu(),
                        "Cap " + p.getCapBac(),
                        moneyFmt.format(p.getPhuCapChucVu()),
                        toTrangThaiDisplay(p.getTrangThai())
                });
            }
            txtSearch.setText("");
            cboFilter.setSelectedIndex(0);
            sorter.setRowFilter(null);
            table.clearSelection();
            btnSua.setEnabled(false);
            btnLichSu.setEnabled(false);
        } finally {
            isRefreshing = false;
        }
    }

    private void showAddDialog() {
        JTextField txtMa     = new JTextField();
        JTextField txtTen    = new JTextField();
        JTextField txtCapBac = new JTextField("1");
        JTextField txtPhuCap = new JTextField("0");
        JTextArea  txtMoTa   = new JTextArea(3, 20); txtMoTa.setLineWrap(true);

        Object[] fields = {
                "Ma chuc vu (*):", txtMa,
                "Ten chuc vu (*):", txtTen,
                "Cap bac (1 = cao nhat):", txtCapBac,
                "Phu cap (VND):", txtPhuCap,
                "Mo ta:", new JScrollPane(txtMoTa)
        };
        int ok = JOptionPane.showConfirmDialog(this, fields, "Them chuc vu moi", JOptionPane.OK_CANCEL_OPTION);
        if (ok != JOptionPane.OK_OPTION) return;

        int capBac;
        double phuCap;
        try {
            capBac = parseCapBac(txtCapBac.getText());
            phuCap = parsePhuCap(txtPhuCap.getText());
        } catch (NumberFormatException ex) {
            DialogUtil.showError(this, "Cap bac va phu cap phai la so hop le (vi du: 1, 1500000, 1.500.000).");
            return;
        }

        KetQua<Void> kq = service.addPosition(
                txtMa.getText().trim(), txtTen.getText().trim(), capBac, phuCap, txtMoTa.getText().trim());
        if (!kq.isSuccess()) { DialogUtil.showError(this, kq.getMessage()); return; }
        refreshTable();
        DialogUtil.showSuccess(this, kq.getMessage());
    }

    private void showEditDialog() {
        int row = table.getSelectedRow();
        if (row < 0) { DialogUtil.showWarn(this, "Vui long chon mot chuc vu de sua."); return; }
        int modelRow = table.convertRowIndexToModel(row);
        String ma = (String) tableModel.getValueAt(modelRow, 0);
        ChucVu pos = service.getByMaChucVu(ma);
        if (pos == null) return;

        boolean canEdit = SessionContext.getInstance().hasPermission(PermissionCodes.POSITION_MANAGE);
        Frame frame = (Frame) SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(frame, "Sua chuc vu - " + pos.getTenChucVu(), true);

        JTextField txtMa     = new JTextField(pos.getId()); txtMa.setEnabled(false);
        JTextField txtTen    = new JTextField(pos.getTenChucVu());
        JTextField txtCapBac = new JTextField(String.valueOf(pos.getCapBac()));
        JTextField txtPhuCap = new JTextField(String.valueOf(pos.getPhuCapChucVu()));
        JTextArea  txtMoTa   = new JTextArea(pos.getMoTa() != null ? pos.getMoTa() : "", 3, 20);
        txtMoTa.setLineWrap(true);
        JComboBox<String> cboTrangThai = new JComboBox<>(new String[]{STATUS_ACTIVE, STATUS_INACTIVE});
        cboTrangThai.setSelectedItem(toTrangThaiDisplay(pos.getTrangThai()));
        cboTrangThai.setEnabled(canEdit);

        for (JComponent c : new JComponent[]{txtTen, txtCapBac, txtPhuCap, txtMoTa}) {
            if (c instanceof JTextField) ((JTextField) c).setEditable(canEdit);
            else ((JTextArea) c).setEditable(canEdit);
        }

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(16, 16, 8, 16));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 8, 5, 8);
        gbc.anchor = GridBagConstraints.WEST;

        String[] labels = {"Ma chuc vu:", "Ten chuc vu (*):", "Cap bac:", "Phu cap (VND):", "Mo ta:", "Trang thai:"};
        JComponent[] flds = {txtMa, txtTen, txtCapBac, txtPhuCap, new JScrollPane(txtMoTa), cboTrangThai};
        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = 0; gbc.gridy = i; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
            JLabel lbl = new JLabel(labels[i]); lbl.setFont(UIFonts.TEXT_NORMAL);
            form.add(lbl, gbc);
            gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
            flds[i].setPreferredSize(new Dimension(210, i == 4 ? 60 : 28));
            form.add(flds[i], gbc);
        }

        JPanel btnPanel2 = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        JButton btnLichSuBtn = UIHelper.createDefaultButton("Xem lich su he so");
        btnLichSuBtn.addActionListener(e -> showHistoryDialog());
        btnPanel2.add(btnLichSuBtn);
        JButton btnHuy = UIHelper.createDefaultButton("Huy");
        btnHuy.addActionListener(e -> dialog.dispose());
        btnPanel2.add(btnHuy);

        if (canEdit) {
            JButton btnLuu = UIHelper.createSuccessButton("Luu");
            btnLuu.addActionListener(e -> {
                int capBac;
                double phuCap;
                try {
                    capBac = parseCapBac(txtCapBac.getText());
                    phuCap = parsePhuCap(txtPhuCap.getText());
                } catch (NumberFormatException ex) {
                    DialogUtil.showError(dialog, "Cap bac va phu cap phai la so hop le (vi du: 1, 1500000, 1.500.000).");
                    return;
                }
                btnLuu.setEnabled(false);
                KetQua<Void> kqCapNhat = service.updatePosition(
                        ma, txtTen.getText().trim(), capBac, phuCap, txtMoTa.getText().trim());
                if (!kqCapNhat.isSuccess()) {
                    btnLuu.setEnabled(true);
                    DialogUtil.showError(dialog, kqCapNhat.getMessage());
                    return;
                }

                String rawMoi = toTrangThaiRaw((String) cboTrangThai.getSelectedItem());
                if (!normalizeTrangThai(rawMoi).equals(normalizeTrangThai(pos.getTrangThai()))) {
                    KetQua<Void> kqTT;
                    if (HRMConstants.TRANG_THAI_HOAT_DONG.equals(rawMoi)) {
                        kqTT = service.activatePosition(ma);
                    } else {
                        kqTT = service.deactivatePosition(ma);
                    }
                    if (!kqTT.isSuccess()) {
                        btnLuu.setEnabled(true);
                        DialogUtil.showError(dialog, kqTT.getMessage());
                        return;
                    }
                }
                refreshTable();
                dialog.dispose();
                DialogUtil.showSuccess(PositionPanel.this, "Cap nhat chuc vu thanh cong!");
            });
            btnPanel2.add(btnLuu);
        }

        JPanel main = new JPanel(new BorderLayout());
        main.add(form, BorderLayout.CENTER);
        main.add(btnPanel2, BorderLayout.SOUTH);
        dialog.setContentPane(main);
        dialog.pack();
        dialog.setMinimumSize(new Dimension(420, 320));
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void showHistoryDialog() {
        int row = table.getSelectedRow();
        if (row == -1) {
            DialogUtil.showWarn(this, "Vui long chon mot chuc vu de xem lich su.");
            return;
        }
        int modelRow = table.convertRowIndexToModel(row);
        String ma  = (String) tableModel.getValueAt(modelRow, 0);
        String ten = (String) tableModel.getValueAt(modelRow, 1);

        List<LichSuHeSoLuong> danhSach = service.getHistoryByMaChucVu(ma);

        DefaultTableModel histModel = new DefaultTableModel(
                new Object[]{"Ngay thay đổi", "Phụ cấp cũ (VND)", "Phụ cấp mới (VND)", "Người thay đổi"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        for (LichSuHeSoLuong h : danhSach) {
            histModel.addRow(new Object[]{
                    h.getNgayThayDoi(),
                    moneyFmt.format(h.getPhuCapCu()),
                    moneyFmt.format(h.getPhuCapMoi()),
                    h.getNguoiThayDoi()
            });
        }

        JTable histTable = new JTable(histModel);
        histTable.setRowHeight(24);
        JScrollPane scroll = new JScrollPane(histTable);
        scroll.setPreferredSize(new Dimension(500, 200));

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "Lịch sử thay đổi phụ cấp -- " + ten + " (" + ma + ")", true);
        dialog.setLayout(new BorderLayout());
        if (danhSach.isEmpty()) {
            dialog.add(new JLabel("  Chưa có lịch sử thay đổi nào.", SwingConstants.CENTER), BorderLayout.CENTER);
        } else {
            dialog.add(scroll, BorderLayout.CENTER);
        }
        JButton btnDong = UIHelper.createDefaultButton("Đóng");
        btnDong.addActionListener(e -> dialog.dispose());
        JPanel footer = new JPanel(); footer.add(btnDong);
        dialog.add(footer, BorderLayout.SOUTH);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void showDetailDialog() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        String ma = (String) tableModel.getValueAt(table.convertRowIndexToModel(row), 0);
        ChucVu pos = service.getByMaChucVu(ma);
        if (pos == null) return;

        JOptionPane.showMessageDialog(this,
                "Ma:          " + pos.getId() + "\n"
                + "Ten:         " + pos.getTenChucVu() + "\n"
                + "Cap bac:     Cap " + pos.getCapBac() + "\n"
                + "Phu cap:     " + moneyFmt.format(pos.getPhuCapChucVu()) + " VND\n"
                + "Mo ta:       " + (pos.getMoTa() != null ? pos.getMoTa() : "") + "\n"
                + "Trang thai:  " + toTrangThaiDisplay(pos.getTrangThai()),
                "Chi tiet chuc vu", JOptionPane.INFORMATION_MESSAGE);
    }

    private String toTrangThaiDisplay(String raw) {
        String n = normalizeTrangThai(raw);
        if ("hoatdong".equals(n)) return STATUS_ACTIVE;
        if ("ngunghoatdong".equals(n) || "ngung".equals(n)) return STATUS_INACTIVE;
        return raw == null ? "" : raw;
    }

    private String toTrangThaiRaw(String display) {
        String n = normalizeTrangThai(display);
        if ("hoatdong".equals(n)) return HRMConstants.TRANG_THAI_HOAT_DONG;
        if ("ngunghoatdong".equals(n) || "ngung".equals(n)) return HRMConstants.TRANG_THAI_NGUNG_HOAT_DONG;
        return display == null ? "" : display;
    }

    private String normalizeTrangThai(String value) {
        if (value == null) return "";
        String v = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .replace('d', 'd').toLowerCase().trim();
        return v.replace("_", "").replace(" ", "").replace("-", "");
    }

    private int parseCapBac(String input) throws NumberFormatException {
        String value = input == null ? "" : input.trim();
        return Integer.parseInt(value);
    }

    private double parsePhuCap(String input) throws NumberFormatException {
        String value = input == null ? "" : input.trim();
        if (value.isEmpty()) {
            throw new NumberFormatException("empty");
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException ignored) {
            // Accept common VND group separators like 1.500.000 or 1,500,000
            String compact = value.replace(" ", "");
            if (compact.matches("\\d{1,3}([.,]\\d{3})+")) {
                return Double.parseDouble(compact.replace(".", "").replace(",", ""));
            }
            String normalized = compact.replace(",", ".");
            return Double.parseDouble(normalized);
        }
    }
}
