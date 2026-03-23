package com.hrm.gui.admin;

import com.hrm.bus.KetQua;
import com.hrm.bus.PhongBanBUS;
import com.hrm.model.PhongBan;
import com.hrm.util.DialogUtil;
import com.hrm.util.HRMConstants;
import com.hrm.util.UIFonts;
import com.hrm.util.PermissionCodes;
import com.hrm.util.SessionContext;
import com.hrm.gui.components.PurpleTable;
import com.hrm.util.UIColors;
import com.hrm.util.UIHelper;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.List;
import java.util.Objects;

public class DepartmentPanel extends JPanel {

    private static final String STATUS_ALL = "Tất cả";
    private static final String STATUS_ACTIVE = "Hoạt động";
    private static final String STATUS_INACTIVE = "Ngừng hoạt động";
    private final PhongBanBUS service = new PhongBanBUS();
    private PurpleTable table;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    private JTextField txtSearch;
    private JComboBox<String> cboFilter;
    private JButton btnThem;
    public DepartmentPanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JPanel topPanel = new JPanel(new BorderLayout());
        JLabel lblHint = new JLabel("Tìm theo: Mã / Tên phòng ban / Trạng thái");
        lblHint.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblHint.setForeground(UIColors.TEXT_DARK);
        topPanel.add(lblHint, BorderLayout.SOUTH);
        JPanel searchFilterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel lblSearch = new JLabel("Tìm kiếm:");
        lblSearch.setFont(UIFonts.TEXT_NORMAL);
        lblSearch.setForeground(UIColors.TEXT_DARK);
        txtSearch = new JTextField(20);
        txtSearch.setFont(UIFonts.TEXT_NORMAL);
        txtSearch.setToolTipText("Nhập mã hoặc tên phòng ban để tìm kiếm");
        JLabel lblFilter = new JLabel("    Trạng thái:");
        lblFilter.setFont(UIFonts.TEXT_NORMAL);
        lblFilter.setForeground(UIColors.TEXT_DARK);
        cboFilter = new JComboBox<>(new String[]{STATUS_ALL, STATUS_ACTIVE, STATUS_INACTIVE});
        searchFilterPanel.add(lblSearch);
        searchFilterPanel.add(txtSearch);
        searchFilterPanel.add(lblFilter);
        searchFilterPanel.add(cboFilter);
        topPanel.add(searchFilterPanel, BorderLayout.NORTH);
        add(topPanel, BorderLayout.NORTH);
        tableModel = PurpleTable.createNonEditableModel(
                new Object[]{"Mã PB", "Tên phòng ban", "Phòng ban cha", "Trạng thái"});
        table = new PurpleTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(0).setPreferredWidth(70);
        table.getColumnModel().getColumn(1).setPreferredWidth(180);
        table.getColumnModel().getColumn(2).setPreferredWidth(160);
        table.getColumnModel().getColumn(3).setPreferredWidth(100);
        table.getColumnModel().getColumn(3).setCellRenderer(new com.hrm.gui.components.StatusCellRenderer());
        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        add(scroll, BorderLayout.CENTER);
        btnThem = UIHelper.createPrimaryButton("+ Thêm");
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnPanel.add(btnThem);
        JButton btnLamMoi = UIHelper.createDefaultButton("Làm mới");
        btnLamMoi.addActionListener(e -> refreshTable());
        btnPanel.add(btnLamMoi);
        add(btnPanel, BorderLayout.SOUTH);
        setupPermissions();
        btnThem.addActionListener(e -> showAddDialog());
        txtSearch.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent e) {
                applyFilter();
            }
        });
        cboFilter.addActionListener(e -> applyFilter());
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    showDetailDialog();
                }
            }
        });
        refreshTable();
    }

    private void setupPermissions() {
        btnThem.setVisible(SessionContext.getInstance().hasPermission(PermissionCodes.DEPARTMENT_MANAGE));
    }

    private void applyFilter() {
        String searchText = txtSearch.getText().toLowerCase().trim();
        int statusFilterIndex = cboFilter.getSelectedIndex();
        RowFilter<DefaultTableModel, Object> rf = new RowFilter<DefaultTableModel, Object>() {
            public boolean include(Entry<? extends DefaultTableModel, ? extends Object> entry) {
                String ma = entry.getStringValue(0).toLowerCase();
                String tenPhongBan = entry.getStringValue(1).toLowerCase();
                boolean matchSearch = searchText.isEmpty() || ma.contains(searchText) || tenPhongBan.contains(searchText);
                String trangThai = entry.getStringValue(3);
                boolean matchStatus = true;
                if (statusFilterIndex == 1) {
                    matchStatus = STATUS_ACTIVE.equals(trangThai);
                } else if (statusFilterIndex == 2) {
                    matchStatus = STATUS_INACTIVE.equals(trangThai);
                }
                return matchSearch && matchStatus;
            }
        };
        sorter.setRowFilter(rf);
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        for (PhongBan d : service.getAllDepartments()) {
            String tenCha = "- (gốc)";
            if (d.getPhongBanChaId() != null) {
            PhongBan cha = service.getByMaPhongBan(d.getPhongBanChaId());
                if (cha != null) {
                    tenCha = cha.getTenPhongBan();
                }
            }
            tableModel.addRow(new Object[]{
                    d.getId(),
                    d.getTenPhongBan(),
                    tenCha,
                    toTrangThaiDisplay(d.getTrangThai())
            });
        }
        txtSearch.setText("");
        cboFilter.setSelectedIndex(0);
    }

    private void showAddDialog() {
        JTextField txtMa = new JTextField();
        JTextField txtTen = new JTextField();
        List<PhongBan> dsActive = service.getActiveDepartments();
        JComboBox<String> comboCha = buildParentCombo(dsActive, null);
        Object[] fields = {
                "Mã phòng ban (*):", txtMa,
                "Tên phòng ban (*):", txtTen,
                "Phòng ban cha:", comboCha
        };
        int ok = JOptionPane.showConfirmDialog(this, fields, "Thêm phòng ban mới", JOptionPane.OK_CANCEL_OPTION);
        if (ok != JOptionPane.OK_OPTION) {
            return;
        }
        String maCha = getSelectedMa(comboCha, dsActive);
        KetQua<Void> kq = service.addDepartment(txtMa.getText().trim(), txtTen.getText().trim(), maCha);
        if (!kq.isSuccess()) {
            DialogUtil.showError(this, kq.getMessage());
            return;
        }
        refreshTable();
        DialogUtil.showSuccess(this, kq.getMessage());
    }

    private void showDetailDialog() {
        int row = table.getSelectedRow();
        if (row == -1) return;
        int modelRow = table.convertRowIndexToModel(row);
        String ma = (String) tableModel.getValueAt(modelRow, 0);
        PhongBan dept = service.getByMaPhongBan(ma);
        if (dept == null) return;
        boolean canEdit = SessionContext.getInstance().hasPermission(PermissionCodes.DEPARTMENT_MANAGE);
        Frame frame = (Frame) SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(frame, "Chi tiết phòng ban - " + dept.getTenPhongBan(), true);
        JTextField txtMa = new JTextField(dept.getId());
        txtMa.setEnabled(false);
        JTextField txtTen = new JTextField(dept.getTenPhongBan());
        txtTen.setEditable(canEdit);
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
        JComponent[] fields = {txtMa, txtTen, comboCha, cboTrangThai};
        for (int i = 0; i < fields.length; i++) {
            gbc.gridx = 0;
            gbc.gridy = i;
            gbc.fill = GridBagConstraints.NONE;
            gbc.weightx = 0;
            JLabel lbl = new JLabel(
                    i == 0 ? "Mã phòng ban:" :
                    i == 1 ? "Tên phòng ban (*):" :
                    i == 2 ? "Phòng ban cha:" : "Trạng thái:");
            lbl.setFont(UIFonts.TEXT_NORMAL);
            form.add(lbl, gbc);
            gbc.gridx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1.0;
            fields[i].setPreferredSize(new Dimension(220, 28));
            form.add(fields[i], gbc);
        }
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        JButton btnHuy = UIHelper.createDefaultButton("Hủy");
        btnHuy.addActionListener(e -> dialog.dispose());
        btnPanel.add(btnHuy);
        if (canEdit) {
            JButton btnLuu = UIHelper.createSuccessButton("Lưu");
            btnLuu.addActionListener(e -> {
                String maCha = getSelectedMa(comboCha, dsActive);
                String tenMoi = txtTen.getText().trim();
                if (!Objects.equals(tenMoi, dept.getTenPhongBan()) || !Objects.equals(maCha, dept.getPhongBanChaId())) {
                    KetQua<Void> kqCapNhat = service.updateDepartment(ma, tenMoi, maCha);
                    if (!kqCapNhat.isSuccess()) {
                        DialogUtil.showError(dialog, kqCapNhat.getMessage());
                        return;
                    }
                }
                String rawTrangThaiMoi = toTrangThaiRaw((String) cboTrangThai.getSelectedItem());
                if (!rawTrangThaiMoi.equals(dept.getTrangThai())) {
                    KetQua<Void> kqTrangThai;
                    if (HRMConstants.TRANG_THAI_HOAT_DONG.equals(rawTrangThaiMoi)) {
                        kqTrangThai = service.activateDepartment(ma);
                    } else {
                        kqTrangThai = service.deactivateDepartment(ma);
                    }
                    if (!kqTrangThai.isSuccess()) {
                        DialogUtil.showError(dialog, kqTrangThai.getMessage());
                        return;
                    }
                }
                refreshTable();
                dialog.dispose();
                DialogUtil.showSuccess(DepartmentPanel.this, "Cập nhật phòng ban thành công!");
            });
            btnPanel.add(btnLuu);
        }
        JPanel main = new JPanel(new BorderLayout());
        main.add(form, BorderLayout.CENTER);
        main.add(btnPanel, BorderLayout.SOUTH);
        dialog.setContentPane(main);
        dialog.pack();
        dialog.setMinimumSize(new Dimension(400, 230));
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private JComboBox<String> buildParentCombo(List<PhongBan> dsActive, String maChaHienTai) {
        String[] items = new String[dsActive.size() + 1];
        items[0] = "- Không có (phòng ban gốc)";
        int selectedIndex = 0;
        for (int i = 0; i < dsActive.size(); i++) {
            items[i + 1] = dsActive.get(i).toString();
            if (dsActive.get(i).getId().equals(maChaHienTai)) {
                selectedIndex = i + 1;
            }
        }
        JComboBox<String> combo = new JComboBox<>(items);
        combo.setSelectedIndex(selectedIndex);
        return combo;
    }

    private String getSelectedMa(JComboBox<String> combo, List<PhongBan> dsActive) {
        if (combo.getSelectedIndex() == 0) {
            return null;
        }
        int idx = combo.getSelectedIndex() - 1;
        return dsActive.get(idx).getId();
    }

    private String toTrangThaiDisplay(String raw) {
        if (HRMConstants.TRANG_THAI_HOAT_DONG.equals(raw)) return STATUS_ACTIVE;
        if (HRMConstants.TRANG_THAI_NGUNG_HOAT_DONG.equals(raw)) return STATUS_INACTIVE;
        return raw == null ? "" : raw;
    }

    private String toTrangThaiRaw(String display) {
        if (STATUS_ACTIVE.equals(display)) return HRMConstants.TRANG_THAI_HOAT_DONG;
        if (STATUS_INACTIVE.equals(display)) return HRMConstants.TRANG_THAI_NGUNG_HOAT_DONG;
        return display == null ? "" : display;
    }
}
