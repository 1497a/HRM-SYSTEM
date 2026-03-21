package com.hrm.gui.admin;

import com.hrm.bus.ChucVuBUS;
import com.hrm.bus.KetQua;
import com.hrm.model.ChucVu;
import com.hrm.util.DialogUtil;
import com.hrm.util.HRMConstants;
import com.hrm.util.PermissionCodes;
import com.hrm.util.SessionContext;
import com.hrm.util.UIHelper;
import com.hrm.util.UIFonts;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.text.NumberFormat;
import java.util.Locale;

public class PositionPanel extends JPanel {

    private static final String STATUS_ALL      = "Tất cả";
    private static final String STATUS_ACTIVE   = "Hoạt động";
    private static final String STATUS_INACTIVE = "Ngừng hoạt động";
    private final ChucVuBUS service = new ChucVuBUS();
    private final NumberFormat moneyFmt = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
    private JTable table;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    private JTextField txtSearch;
    private JComboBox<String> cboFilter;
    private JButton btnThem;
    public PositionPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
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
        tableModel = new DefaultTableModel(
                new Object[]{"Ma CV", "Ten chuc vu", "Cap bac", "Phu cap (VND)", "Trang thai"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
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
        btnThem = UIHelper.createPrimaryButton("+ Them");
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnPanel.add(btnThem);
        JButton btnLamMoi = new JButton("Lam moi");
        btnLamMoi.addActionListener(e -> refreshTable());
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
        refreshTable();
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

    private void refreshTable() {
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
        Object[] fields = {
                "Ma chuc vu (*):", txtMa,
                "Ten chuc vu (*):", txtTen,
                "Cap bac (1 = cao nhat):", txtCapBac,
                "Phu cap (VND):", txtPhuCap,
                "Mo ta:", new JScrollPane(txtMoTa)
        };
        int ok = JOptionPane.showConfirmDialog(this, fields, "Them chuc vu moi", JOptionPane.OK_CANCEL_OPTION);
        if (ok != JOptionPane.OK_OPTION) {
            return;
        }
        String maChucVu = txtMa.getText().trim();
        if (maChucVu.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ma chuc vu khong duoc de trong.", "Loi nhap lieu", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (service.existsActiveByCode(maChucVu)) {
            JOptionPane.showMessageDialog(this,
                    "Ma chuc vu '" + maChucVu + "' da ton tai va dang hoat dong. Vui long dung ma khac.",
                    "Trung ma chuc vu", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String tenChucVu = txtTen.getText().trim();
        if (tenChucVu.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ten chuc vu khong duoc de trong.", "Loi nhap lieu", JOptionPane.ERROR_MESSAGE);
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
        KetQua<Void> kq = service.addPosition(
                maChucVu,
                tenChucVu,
                capBac,
                phuCap,
                txtMoTa.getText().trim()
        );
        if (!kq.isSuccess()) {
            DialogUtil.showError(this, kq.getMessage());
            return;
        }
        refreshTable();
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
        Frame frame = (Frame) SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(frame, "Chi tiet chuc vu - " + pos.getTenChucVu(), true);
        JTextField txtMa = new JTextField(pos.getId());
        txtMa.setEnabled(false);
        JTextField txtTen = new JTextField(pos.getTenChucVu());
        JTextField txtCapBac = new JTextField(String.valueOf(pos.getCapBac()));
        JTextField txtPhuCap = new JTextField(String.valueOf(pos.getPhuCapChucVu()));
        JTextArea txtMoTa = new JTextArea(pos.getMoTa() != null ? pos.getMoTa() : "", 3, 20);
        txtMoTa.setLineWrap(true);
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
                "Ma chuc vu:", "Ten chuc vu (*):", "Cap bac:", "Phu cap (VND):", "Mo ta:", "Trang thai:"
        };
        JComponent[] fields = {txtMa, txtTen, txtCapBac, txtPhuCap, new JScrollPane(txtMoTa), cboTrangThai};
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
        JButton btnHuy = UIHelper.createDefaultButton("Huy");
        btnHuy.addActionListener(e -> dialog.dispose());
        btnPanel.add(btnHuy);
        if (canEdit) {
            JButton btnLuu = UIHelper.createSuccessButton("Luu");
            btnLuu.addActionListener(e -> {
                String tenChucVu = txtTen.getText().trim();
                if (tenChucVu.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Ten chuc vu khong duoc de trong.", "Loi nhap lieu", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                Integer capBac = parseCapBac(txtCapBac.getText(), dialog);
                if (capBac == null) {
                    return;
                }
                Double phuCap = parsePhuCap(txtPhuCap.getText(), dialog);
                if (phuCap == null) {
                    return;
                }
                KetQua<Void> kqCapNhat = service.updatePosition(
                        ma,
                        tenChucVu,
                        capBac,
                        phuCap,
                        txtMoTa.getText().trim()
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
                refreshTable();
                dialog.dispose();
                DialogUtil.showSuccess(PositionPanel.this, "Cap nhat chuc vu thanh cong!");
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
            JOptionPane.showMessageDialog(parent, "Cấp bậc không được để trống.", "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
            return null;
        }
        try {
            int capBac = Integer.parseInt(text);
            if (capBac < 1 || capBac > 20) {
                JOptionPane.showMessageDialog(parent, "Cấp bậc phải là số từ 1 đến 20.", "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
                return null;
            }
            return capBac;
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(parent, "Cấp bậc phải là số nguyên hợp lệ.", "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    private Double parsePhuCap(String rawValue, Component parent) {
        String text = rawValue == null ? "" : rawValue.trim();
        if (text.isEmpty()) {
            JOptionPane.showMessageDialog(parent, "Phu cap khong duoc de trong.", "Loi nhap lieu", JOptionPane.ERROR_MESSAGE);
            return null;
        }
        try {
            double phuCap = Double.parseDouble(text);
            if (phuCap < 0) {
                JOptionPane.showMessageDialog(parent, "Phu cap phai la so >= 0.", "Loi nhap lieu", JOptionPane.ERROR_MESSAGE);
                return null;
            }
            return phuCap;
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(parent, "Phu cap phai la so hop le.", "Loi nhap lieu", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }
}
