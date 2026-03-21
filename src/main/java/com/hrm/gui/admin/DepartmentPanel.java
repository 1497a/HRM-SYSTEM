package com.hrm.gui.admin;

import com.hrm.bus.KetQua;
import com.hrm.bus.PhongBanBUS;
import com.hrm.gui.components.PurpleButton;
import com.hrm.model.PhongBan;
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
import java.util.List;
import java.util.Objects;

public class DepartmentPanel extends JPanel {

    private static final String STATUS_ALL      = "Tat ca";
    private static final String STATUS_ACTIVE   = "Hoat dong";
    private static final String STATUS_INACTIVE = "Ngung hoat dong";

    private final PhongBanBUS service = new PhongBanBUS();

    private JTable table;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    private JTextField txtSearch;
    private JComboBox<String> cboFilter;

    private PurpleButton btnThem;
    private PurpleButton btnSua;
    private PurpleButton btnLamMoi;

    public DepartmentPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top panel
        JPanel topPanel = new JPanel(new BorderLayout());
        JLabel title = new JLabel("QUAN LY PHONG BAN");
        title.setFont(new Font("Arial", Font.BOLD, 16));
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        topPanel.add(title, BorderLayout.NORTH);

        JLabel lblHint = new JLabel("Tim theo: Ma / Ten phong ban / Trang thai");
        lblHint.setFont(new Font("Arial", Font.ITALIC, 11));
        topPanel.add(lblHint, BorderLayout.SOUTH);

        JPanel searchFilterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel lblSearch = new JLabel("Tim kiem:");
        txtSearch = new JTextField(20);
        txtSearch.setToolTipText("Nhap ma hoac ten phong ban de tim kiem");
        JLabel lblFilter = new JLabel("    Trang thai:");
        cboFilter = new JComboBox<>(new String[]{STATUS_ALL, STATUS_ACTIVE, STATUS_INACTIVE});
        searchFilterPanel.add(lblSearch);
        searchFilterPanel.add(txtSearch);
        searchFilterPanel.add(lblFilter);
        searchFilterPanel.add(cboFilter);
        topPanel.add(searchFilterPanel, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

        // Table
        tableModel = new DefaultTableModel(
                new Object[]{"Ma PB", "Ten phong ban", "Phong ban cha", "Trang thai"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(24);
        table.getColumnModel().getColumn(0).setPreferredWidth(70);
        table.getColumnModel().getColumn(1).setPreferredWidth(180);
        table.getColumnModel().getColumn(2).setPreferredWidth(160);
        table.getColumnModel().getColumn(3).setPreferredWidth(100);
        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Buttons
        btnThem = new PurpleButton("+ Them");
        btnSua  = new PurpleButton("Sua", UIColors.SUCCESS_GREEN, UIColors.SUCCESS_GREEN.darker(), UIColors.SUCCESS_GREEN.darker());
        btnSua.setEnabled(false);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnPanel.add(btnThem);
        btnPanel.add(btnSua);
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
                btnSua.setEnabled(table.getSelectedRow() != -1 &&
                        SessionContext.getInstance().hasPermission(PermissionCodes.DEPARTMENT_MANAGE));
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
        boolean canManage = SessionContext.getInstance().hasPermission(PermissionCodes.DEPARTMENT_MANAGE);
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
                String ma = entry.getStringValue(0).toLowerCase();
                String ten = entry.getStringValue(1).toLowerCase();
                boolean matchSearch = searchText.isEmpty() || ma.contains(searchText) || ten.contains(searchText);
                String trangThai = normalizeTrangThai(entry.getStringValue(3));
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
            for (PhongBan d : service.getAllDepartments()) {
                String tenCha = "-- (goc) --";
                if (d.getPhongBanChaId() != null) {
                    PhongBan cha = service.getByMaPhongBan(d.getPhongBanChaId());
                    if (cha != null) tenCha = cha.getTenPhongBan();
                }
                tableModel.addRow(new Object[]{
                        d.getId(), d.getTenPhongBan(), tenCha, toTrangThaiDisplay(d.getTrangThai())
                });
            }

            txtSearch.setText("");
            cboFilter.setSelectedIndex(0);
            sorter.setRowFilter(null);
            table.clearSelection();
            btnSua.setEnabled(false);
        } finally {
            isRefreshing = false;
        }
    }

    private void showAddDialog() {
        JTextField txtMa  = new JTextField();
        JTextField txtTen = new JTextField();
        List<PhongBan> dsActive = service.getActiveDepartments();
        JComboBox<String> comboCha = buildParentCombo(dsActive, null);

        Object[] fields = {
                "Ma phong ban (*):", txtMa,
                "Ten phong ban (*):", txtTen,
                "Phong ban cha:", comboCha
        };
        int ok = JOptionPane.showConfirmDialog(this, fields, "Them phong ban moi", JOptionPane.OK_CANCEL_OPTION);
        if (ok != JOptionPane.OK_OPTION) return;

        String maCha = getSelectedMa(comboCha, dsActive);
        KetQua<Void> kq = service.addDepartment(txtMa.getText().trim(), txtTen.getText().trim(), maCha);
        if (!kq.isSuccess()) {
            DialogUtil.showError(this, kq.getMessage());
            return;
        }
        refreshTable();
        DialogUtil.showSuccess(this, kq.getMessage());
    }

    private void showEditDialog() {
        int row = table.getSelectedRow();
        if (row < 0) {
            DialogUtil.showWarn(this, "Vui long chon mot phong ban de sua.");
            return;
        }
        int modelRow = table.convertRowIndexToModel(row);
        String ma = (String) tableModel.getValueAt(modelRow, 0);
        PhongBan dept = service.getByMaPhongBan(ma);
        if (dept == null) return;

        boolean canEdit = SessionContext.getInstance().hasPermission(PermissionCodes.DEPARTMENT_MANAGE);
        Frame frame = (Frame) SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(frame, "Sua phong ban - " + dept.getTenPhongBan(), true);

        JTextField txtMa  = new JTextField(dept.getId()); txtMa.setEnabled(false);
        JTextField txtTen = new JTextField(dept.getTenPhongBan()); txtTen.setEditable(canEdit);

        List<PhongBan> dsActive = service.getActiveDepartments();
        dsActive.removeIf(d -> d.getId().equals(ma));
        JComboBox<String> comboCha = buildParentCombo(dsActive, dept.getPhongBanChaId());
        comboCha.setEnabled(canEdit);

        JComboBox<String> cboTrangThai = new JComboBox<>(new String[]{STATUS_ACTIVE, STATUS_INACTIVE});
        cboTrangThai.setSelectedItem(toTrangThaiDisplay(dept.getTrangThai()));
        cboTrangThai.setEnabled(canEdit);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(16, 16, 8, 16));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.anchor = GridBagConstraints.WEST;

        String[] labels = {"Ma phong ban:", "Ten phong ban (*):", "Phong ban cha:", "Trang thai:"};
        JComponent[] flds = {txtMa, txtTen, comboCha, cboTrangThai};
        for (int i = 0; i < flds.length; i++) {
            gbc.gridx = 0; gbc.gridy = i; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
            JLabel lbl = new JLabel(labels[i]); lbl.setFont(UIFonts.TEXT_NORMAL);
            form.add(lbl, gbc);
            gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
            flds[i].setPreferredSize(new Dimension(220, 28));
            form.add(flds[i], gbc);
        }

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        JButton btnHuy = UIHelper.createDefaultButton("Huy");
        btnHuy.addActionListener(e -> dialog.dispose());
        btnPanel.add(btnHuy);

        if (canEdit) {
            JButton btnLuu = UIHelper.createSuccessButton("Luu");
            btnLuu.addActionListener(e -> {
                String maCha = getSelectedMa(comboCha, dsActive);
                String tenMoi = txtTen.getText().trim();
                if (!Objects.equals(tenMoi, dept.getTenPhongBan()) || !Objects.equals(maCha, dept.getPhongBanChaId())) {
                    KetQua<Void> kqCapNhat = service.updateDepartment(ma, tenMoi, maCha);
                    if (!kqCapNhat.isSuccess()) { DialogUtil.showError(dialog, kqCapNhat.getMessage()); return; }
                }
                String rawMoi = toTrangThaiRaw((String) cboTrangThai.getSelectedItem());
                if (!normalizeTrangThai(rawMoi).equals(normalizeTrangThai(dept.getTrangThai()))) {
                    KetQua<Void> kqTT;
                    if (HRMConstants.TRANG_THAI_HOAT_DONG.equals(rawMoi)) {
                        kqTT = service.activateDepartment(ma);
                    } else {
                        kqTT = service.deactivateDepartment(ma);
                    }
                    if (!kqTT.isSuccess()) { DialogUtil.showError(dialog, kqTT.getMessage()); return; }
                }
                refreshTable();
                dialog.dispose();
                DialogUtil.showSuccess(DepartmentPanel.this, "Cap nhat phong ban thanh cong!");
            });
            btnPanel.add(btnLuu);
        }

        JPanel main = new JPanel(new BorderLayout());
        main.add(form, BorderLayout.CENTER);
        main.add(btnPanel, BorderLayout.SOUTH);
        dialog.setContentPane(main);
        dialog.pack();
        dialog.setMinimumSize(new Dimension(400, 220));
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void showDetailDialog() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        String ma = (String) tableModel.getValueAt(table.convertRowIndexToModel(row), 0);
        PhongBan dept = service.getByMaPhongBan(ma);
        if (dept == null) return;

        String tenCha = "(Goc)";
        if (dept.getPhongBanChaId() != null && !dept.getPhongBanChaId().trim().isEmpty()) {
            PhongBan cha = service.getByMaPhongBan(dept.getPhongBanChaId());
            tenCha = (cha != null) ? cha.getTenPhongBan() + " (" + cha.getId() + ")" : dept.getPhongBanChaId();
        }
        JOptionPane.showMessageDialog(this,
                "Ma:             " + dept.getId() + "\n"
                + "Ten:            " + dept.getTenPhongBan() + "\n"
                + "Phong ban cha:  " + tenCha + "\n"
                + "Trang thai:     " + toTrangThaiDisplay(dept.getTrangThai()),
                "Chi tiet phong ban", JOptionPane.INFORMATION_MESSAGE);
    }

    private JComboBox<String> buildParentCombo(List<PhongBan> dsActive, String maChaHienTai) {
        String[] items = new String[dsActive.size() + 1];
        items[0] = "-- Khong co (phong ban goc) --";
        int selectedIndex = 0;
        for (int i = 0; i < dsActive.size(); i++) {
            items[i + 1] = dsActive.get(i).toString();
            if (dsActive.get(i).getId().equals(maChaHienTai)) selectedIndex = i + 1;
        }
        JComboBox<String> combo = new JComboBox<>(items);
        combo.setSelectedIndex(selectedIndex);
        return combo;
    }

    private String getSelectedMa(JComboBox<String> combo, List<PhongBan> dsActive) {
        if (combo.getSelectedIndex() == 0) return null;
        return dsActive.get(combo.getSelectedIndex() - 1).getId();
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
}
