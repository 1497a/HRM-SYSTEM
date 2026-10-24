package com.hrm.gui.admin;

import com.hrm.bus.ChucVuBUS;
import com.hrm.bus.KetQua;
import com.hrm.bus.XacThucBUS;
import com.hrm.model.ChucVu;
import com.hrm.model.VaiTro;
import com.hrm.util.DialogUtil;
import com.hrm.util.HRMConstants;
import com.hrm.util.PermissionCodes;
import com.hrm.util.SessionContext;
import com.hrm.gui.components.PurpleTable;
import com.hrm.util.UIColors;
import com.hrm.util.UIHelper;
import com.hrm.util.UIFonts;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.Color;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class PositionPanel extends JPanel {

    private static final String STATUS_ALL      = "Tất cả";
    private static final String STATUS_ACTIVE   = "Hoạt động";
    private static final String STATUS_INACTIVE = "Ngừng hoạt động";
    private final ChucVuBUS service = new ChucVuBUS();
    private final NumberFormat moneyFmt = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
    private PurpleTable table;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    private JTextField txtSearch;
    private JComboBox<String> cboFilter;
    private JButton btnThem;
    public PositionPanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        JLabel lblHint = new JLabel("Tìm theo: Mã / Tên chức vụ / Trạng thái");
        lblHint.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblHint.setForeground(UIColors.TEXT_DARK);
        topPanel.add(lblHint, BorderLayout.SOUTH);
        JPanel searchFilterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchFilterPanel.setOpaque(false);
        JLabel lblSearch = new JLabel("Tìm kiếm:");
        txtSearch = new JTextField(20);
        txtSearch.setToolTipText("Nhập mã hoặc tên chức vụ để tìm kiếm");
        JLabel lblFilter = new JLabel("    Trạng thái:");
        cboFilter = new JComboBox<>(new String[]{STATUS_ALL, STATUS_ACTIVE, STATUS_INACTIVE});
        searchFilterPanel.add(lblSearch);
        searchFilterPanel.add(txtSearch);
        searchFilterPanel.add(lblFilter);
        searchFilterPanel.add(cboFilter);
        topPanel.add(searchFilterPanel, BorderLayout.NORTH);
        add(topPanel, BorderLayout.NORTH);
        tableModel = PurpleTable.createNonEditableModel(
                new Object[]{"Mã CV", "Tên chức vụ", "Cấp bậc", "Phụ cấp (VND)", "Trạng thái"});
        table = new PurpleTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(0).setPreferredWidth(70);
        table.getColumnModel().getColumn(1).setPreferredWidth(160);
        table.getColumnModel().getColumn(2).setPreferredWidth(70);
        table.getColumnModel().getColumn(3).setPreferredWidth(110);
        table.getColumnModel().getColumn(4).setPreferredWidth(90);
        table.getColumnModel().getColumn(4).setCellRenderer(new com.hrm.gui.components.StatusCellRenderer());
        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        add(scroll, BorderLayout.CENTER);
        btnThem = UIHelper.createPrimaryButton("+ Thêm");
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnPanel.setOpaque(false);
        btnPanel.add(btnThem);
        JButton btnLamMoi = UIHelper.createDefaultButton("Làm mới");
        btnLamMoi.addActionListener(e -> loadData());
        btnPanel.add(btnLamMoi);
        add(btnPanel, BorderLayout.SOUTH);
        setupPermissions();
        btnThem.addActionListener(e -> showAddDialog());
        txtSearch.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                applyFilter();
            }
        });
        cboFilter.addActionListener(e -> applyFilter());
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    showDetailDialog();
                }
            }
        });
        loadData();
    }

    private void setupPermissions() {
        btnThem.setVisible(SessionContext.getInstance().hasPermission(PermissionCodes.POSITION_MANAGE));
    }

    private void applyFilter() {
        String searchText = txtSearch.getText().toLowerCase().trim();
        int statusFilterIndex = cboFilter.getSelectedIndex();
        RowFilter<DefaultTableModel, Object> rf = new RowFilter<DefaultTableModel, Object>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ? extends Object> entry) {
                String ma = entry.getStringValue(0).toLowerCase();
                String tenChucVu = entry.getStringValue(1).toLowerCase();
                boolean matchSearch = searchText.isEmpty() || ma.contains(searchText) || tenChucVu.contains(searchText);
                String trangThai = entry.getStringValue(4);
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

    public void loadData() {
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
    }

    private void showAddDialog() {
        JTextField txtMa = new JTextField();
        JTextField txtTen = new JTextField();
        JTextField txtCapBac = new JTextField("1");
        JTextField txtPhuCap = new JTextField("0");
        JTextArea txtMoTa = new JTextArea(3, 20);
        txtMoTa.setLineWrap(true);
        boolean canSetRole = SessionContext.getInstance().hasPermission(PermissionCodes.ROLE_UPDATE);
        JComboBox<String> cboVaiTro = buildRoleComboBox(null);
        cboVaiTro.setEnabled(canSetRole);
        Object[] fields = canSetRole
                ? new Object[]{
                        "Mã chức vụ (*):", txtMa,
                        "Tên chức vụ (*):", txtTen,
                        "Cấp bậc (1 = cao nhất):", txtCapBac,
                        "Phụ cấp (VND):", txtPhuCap,
                        "Mô tả:", new JScrollPane(txtMoTa),
                        "Vai trò mặc định:", cboVaiTro}
                : new Object[]{
                        "Mã chức vụ (*):", txtMa,
                        "Tên chức vụ (*):", txtTen,
                        "Cấp bậc (1 = cao nhất):", txtCapBac,
                        "Phụ cấp (VND):", txtPhuCap,
                        "Mô tả:", new JScrollPane(txtMoTa)};
        int ok = JOptionPane.showConfirmDialog(this, fields, "Thêm chức vụ mới", JOptionPane.OK_CANCEL_OPTION);
        if (ok != JOptionPane.OK_OPTION) {
            return;
        }
        String maChucVu = txtMa.getText().trim();
        if (maChucVu.isEmpty()) {
            DialogUtil.showError(this, "Mã chức vụ không được để trống.", "Lỗi nhập liệu");
            return;
        }
        if (service.existsActiveByCode(maChucVu)) {
            DialogUtil.showError(this,
                    "Mã chức vụ '" + maChucVu + "' đã tồn tại và đang hoạt động. Vui lòng dùng mã khác.");
            return;
        }
        String tenChucVu = txtTen.getText().trim();
        if (tenChucVu.isEmpty()) {
            DialogUtil.showError(this, "Tên chức vụ không được để trống.", "Lỗi nhập liệu");
            return;
        }
        Integer capBac = parseCapBac(txtCapBac.getText(), this);
        if (capBac == null) {
            return;
        }
        Double phuCap = parsePhuCap(txtPhuCap.getText(), this);
        if (phuCap == null) {
            return;
        }
        String selectedRole = getSelectedRoleCode(cboVaiTro);
        KetQua<Void> kq = service.addPosition(
                maChucVu,
                tenChucVu,
                capBac,
                phuCap,
                txtMoTa.getText().trim(),
                selectedRole
        );
        if (!kq.isSuccess()) {
            DialogUtil.showError(this, kq.getMessage());
            return;
        }
        loadData();
        DialogUtil.showSuccess(this, kq.getMessage());
    }

    private void showDetailDialog() {
        int row = table.getSelectedRow();
        if (row == -1) {
            return;
        }
        int modelRow = table.convertRowIndexToModel(row);
        String ma = (String) tableModel.getValueAt(modelRow, 0);
        ChucVu pos = service.getByMaChucVu(ma);
        if (pos == null) {
            return;
        }
        boolean canEdit = SessionContext.getInstance().hasPermission(PermissionCodes.POSITION_MANAGE);
        boolean canEditRole = SessionContext.getInstance().hasPermission(PermissionCodes.ROLE_UPDATE);
        Frame frame = (Frame) SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(frame, "Chi tiết chức vụ - " + pos.getTenChucVu(), true);
        JTextField txtMa = new JTextField(pos.getId());
        txtMa.setEnabled(false);
        JTextField txtTen = new JTextField(pos.getTenChucVu());
        JTextField txtCapBac = new JTextField(String.valueOf(pos.getCapBac()));
        JTextField txtPhuCap = new JTextField(String.valueOf(pos.getPhuCapChucVu()));
        JTextArea txtMoTa = new JTextArea(pos.getMoTa() != null ? pos.getMoTa() : "", 3, 20);
        txtMoTa.setLineWrap(true);
        JComboBox<String> cboVaiTro = buildRoleComboBox(pos.getMaVaiTro());
        cboVaiTro.setEnabled(canEditRole);
        JComboBox<String> cboTrangThai = new JComboBox<>(new String[]{STATUS_ACTIVE, STATUS_INACTIVE});
        cboTrangThai.setSelectedItem(toTrangThaiDisplay(pos.getTrangThai()));
        cboTrangThai.setEnabled(canEdit);
        for (JComponent c : new JComponent[]{txtTen, txtCapBac, txtPhuCap, txtMoTa}) {
            if (c instanceof JTextField) {
                ((JTextField) c).setEditable(canEdit);
            } else {
                ((JTextArea) c).setEditable(canEdit);
            }
        }
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(16, 16, 8, 16));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 8, 5, 8);
        gbc.anchor = GridBagConstraints.WEST;
        String[] labels = {
                "Mã chức vụ:", "Tên chức vụ (*):", "Cấp bậc:", "Phụ cấp (VND):", "Mô tả:", "Vai trò mặc định:", "Trạng thái:"
        };
        JComponent[] fields = {txtMa, txtTen, txtCapBac, txtPhuCap, new JScrollPane(txtMoTa), cboVaiTro, cboTrangThai};
        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = 0;
            gbc.gridy = i;
            gbc.fill = GridBagConstraints.NONE;
            gbc.weightx = 0;
            JLabel lbl = new JLabel(labels[i]);
            lbl.setFont(UIFonts.TEXT_NORMAL);
            form.add(lbl, gbc);
            gbc.gridx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1.0;
            fields[i].setPreferredSize(new Dimension(210, i == 4 ? 60 : 28));
            form.add(fields[i], gbc);
        }
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        JButton btnHuy = UIHelper.createDefaultButton("Hủy");
        btnHuy.addActionListener(e -> dialog.dispose());
        btnPanel.add(btnHuy);
        if (canEdit) {
            JButton btnLuu = UIHelper.createSuccessButton("Lưu");
            btnLuu.addActionListener(e -> {
                String tenChucVu = txtTen.getText().trim();
                if (tenChucVu.isEmpty()) {
                    DialogUtil.showError(dialog, "Tên chức vụ không được để trống.", "Lỗi nhập liệu");
                    txtTen.requestFocusInWindow();
                    return;
                }
                Integer capBac = parseCapBac(txtCapBac.getText(), dialog);
                if (capBac == null) {
                    txtCapBac.requestFocusInWindow();
                    return;
                }
                Double phuCap = parsePhuCap(txtPhuCap.getText(), dialog);
                if (phuCap == null) {
                    txtPhuCap.requestFocusInWindow();
                    return;
                }
                KetQua<Void> kqCapNhat = service.updatePosition(
                        ma,
                        tenChucVu,
                        capBac,
                        phuCap,
                        txtMoTa.getText().trim(),
                        getSelectedRoleCode(cboVaiTro)
                );
                if (!kqCapNhat.isSuccess()) {
                    DialogUtil.showError(dialog, kqCapNhat.getMessage());
                    return;
                }
                String rawTrangThaiMoi = toTrangThaiRaw((String) cboTrangThai.getSelectedItem());
                if (!rawTrangThaiMoi.equals(pos.getTrangThai())) {
                    KetQua<Void> kqTrangThai;
                    if (HRMConstants.TRANG_THAI_HOAT_DONG.equals(rawTrangThaiMoi)) {
                        kqTrangThai = service.activatePosition(ma);
                    } else {
                        kqTrangThai = service.deactivatePosition(ma);
                    }
                    if (!kqTrangThai.isSuccess()) {
                        DialogUtil.showError(dialog, kqTrangThai.getMessage());
                        return;
                    }
                }
                loadData();
                dialog.dispose();
                DialogUtil.showSuccess(PositionPanel.this, "Cập nhật chức vụ thành công!");
            });
            btnPanel.add(btnLuu);
        }
        JPanel main = new JPanel(new BorderLayout());
        main.add(form, BorderLayout.CENTER);
        main.add(btnPanel, BorderLayout.SOUTH);
        dialog.setContentPane(main);
        dialog.pack();
        dialog.setMinimumSize(new Dimension(420, 320));
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private static final String ROLE_NONE = "(Không gán)";

    /** Tao JComboBox danh sach vai tro; selectedMaVaiTro = null → chon "(Khong gan)" */
    private JComboBox<String> buildRoleComboBox(String selectedMaVaiTro) {
        List<VaiTro> roles = XacThucBUS.getInstance().getAllRoles();
        String[] items = new String[roles.size() + 1];
        items[0] = ROLE_NONE;
        int selectedIdx = 0;
        for (int i = 0; i < roles.size(); i++) {
            VaiTro r = roles.get(i);
            items[i + 1] = r.getTenVaiTro() + " (" + r.getId() + ")";
            if (r.getId().equals(selectedMaVaiTro)) {
                selectedIdx = i + 1;
            }
        }
        JComboBox<String> cbo = new JComboBox<>(items);
        cbo.setSelectedIndex(selectedIdx);
        return cbo;
    }

    /** Lay maVaiTro tu combobox; null neu chon "(Khong gan)" */
    private String getSelectedRoleCode(JComboBox<String> cbo) {
        String selected = (String) cbo.getSelectedItem();
        if (selected == null || ROLE_NONE.equals(selected)) return null;
        int start = selected.lastIndexOf('(');
        int end = selected.lastIndexOf(')');
        if (start >= 0 && end > start) return selected.substring(start + 1, end);
        return null;
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

    private Integer parseCapBac(String rawValue, Component parent) {
        String text = rawValue == null ? "" : rawValue.trim();
        if (text.isEmpty()) {
            DialogUtil.showError(parent, "Cấp bậc không được để trống.", "Lỗi nhập liệu");
            return null;
        }
        try {
            int capBac = Integer.parseInt(text);
            if (capBac < 1 || capBac > 20) {
                DialogUtil.showError(parent, "Cấp bậc phải là số từ 1 đến 20.", "Lỗi nhập liệu");
                return null;
            }
            return capBac;
        } catch (NumberFormatException e) {
            DialogUtil.showError(parent, "Cấp bậc phải là số nguyên hợp lệ.", "Lỗi nhập liệu");
            return null;
        }
    }

    private Double parsePhuCap(String rawValue, Component parent) {
        String text = rawValue == null ? "" : rawValue.trim();
        if (text.isEmpty()) {
            DialogUtil.showError(parent, "Phu cap khong duoc de trong.", "Lỗi nhập liệu");
            return null;
        }
        try {
            double phuCap = Double.parseDouble(text);
            if (phuCap < 0) {
                DialogUtil.showError(parent, "Phu cap phai la so >= 0.", "Lỗi nhập liệu");
                return null;
            }
            return phuCap;
        } catch (NumberFormatException e) {
            DialogUtil.showError(parent, "Phu cap phai la so hop le.", "Lỗi nhập liệu");
            return null;
        }
    }
}
