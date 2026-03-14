package com.hrm.gui.admin;

import com.hrm.bus.ChucVuBUS;
import com.hrm.bus.KetQua;
import com.hrm.model.ChucVu;
import com.hrm.model.LichSuHeSoLuong;
import com.hrm.util.DialogUtil;
import com.hrm.util.SessionContext;
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

    private static final String STATUS_ALL = "Tất cả";
    private static final String STATUS_ACTIVE = "Hoạt động";
    private static final String STATUS_INACTIVE = "Ngừng hoạt động";

    private final ChucVuBUS service = new ChucVuBUS();
    private JTable table;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    private NumberFormat moneyFmt = NumberFormat.getNumberInstance(new Locale("vi", "VN"));

    private JTextField txtSearch;
    private JComboBox<String> cboFilter;
    private JButton btnThem;

    public PositionPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel topPanel = new JPanel(new BorderLayout());

        JLabel title = new JLabel("QUẢN LÝ CHỨC VỤ");
        title.setFont(new Font("Arial", Font.BOLD, 16));
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        topPanel.add(title, BorderLayout.NORTH);

        // Gợi ý tìm kiếm
        JLabel lblHint = new JLabel("Tìm theo: Mã / Tên chức vụ / Trạng thái");
        lblHint.setFont(new Font("Arial", Font.ITALIC, 11));
        topPanel.add(lblHint, BorderLayout.SOUTH);

        JPanel searchFilterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JLabel lblSearch = new JLabel("Tìm kiếm:");
        txtSearch = new JTextField(20);
        txtSearch.setToolTipText("Nhập mã hoặc tên chức vụ để tìm kiếm");

        JLabel lblFilter = new JLabel("    Trang thai:");
        cboFilter = new JComboBox<>(new String[]{STATUS_ALL, STATUS_ACTIVE, STATUS_INACTIVE});

        searchFilterPanel.add(lblSearch);
        searchFilterPanel.add(txtSearch);
        searchFilterPanel.add(lblFilter);
        searchFilterPanel.add(cboFilter);

        topPanel.add(searchFilterPanel, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(
                new Object[] { "Mã CV", "Tên chức vụ", "Cấp bậc", "Hệ số lương", "Phụ cấp (VND)", "Trạng thái" }, 0) {
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
        table.getColumnModel().getColumn(3).setPreferredWidth(90);
        table.getColumnModel().getColumn(4).setPreferredWidth(110);
        table.getColumnModel().getColumn(5).setPreferredWidth(90);

        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        add(new JScrollPane(table), BorderLayout.CENTER);

        btnThem = UIHelper.createPrimaryButton("+ Them");
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnPanel.add(btnThem);
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
        btnThem.setVisible(SessionContext.getInstance().coQuyen("POSITION_MANAGE"));
    }

    private void applyFilter() {
        String searchText = txtSearch.getText().toLowerCase().trim();
        int statusFilterIndex = cboFilter.getSelectedIndex();

        RowFilter<DefaultTableModel, Object> rf = new RowFilter<DefaultTableModel, Object>() {
            public boolean include(Entry<? extends DefaultTableModel, ? extends Object> entry) {
                String ma = entry.getStringValue(0).toLowerCase();
                String tenChucVu = entry.getStringValue(1).toLowerCase();
                boolean matchSearch = searchText.isEmpty() || ma.contains(searchText) || tenChucVu.contains(searchText);

                String trangThai = normalizeTrangThai(entry.getStringValue(5));
                boolean matchStatus = true;

                if (statusFilterIndex == 1) {
                    matchStatus = "hoatdong".equals(trangThai);
                } else if (statusFilterIndex == 2) {
                    matchStatus = "ngunghoatdong".equals(trangThai) || "ngung".equals(trangThai);
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
                    p.getHeSoLuong() + "x",
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
        JTextField txtHeSo = new JTextField("1.0");
        JTextField txtPhuCap = new JTextField("0");
        JTextArea txtMoTa = new JTextArea(3, 20);
        txtMoTa.setLineWrap(true);

        Object[] fields = {
                "Ma chuc vu (*):", txtMa,
                "Ten chuc vu (*):", txtTen,
                "Cap bac (1=cao nhat):", txtCapBac,
                "He so luong (*):", txtHeSo,
                "Phu cap (VND):", txtPhuCap,
                "Mo ta:", new JScrollPane(txtMoTa)
        };

        int ok = JOptionPane.showConfirmDialog(this, fields, "Them chuc vu moi", JOptionPane.OK_CANCEL_OPTION);
        if (ok != JOptionPane.OK_OPTION) {
            return;
        }

        try {
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

            int capBac = Integer.parseInt(txtCapBac.getText().trim());
            double heSo = Double.parseDouble(txtHeSo.getText().trim());
            double phuCap = Double.parseDouble(txtPhuCap.getText().trim());

            KetQua<Void> kq = service.addPosition(maChucVu, txtTen.getText().trim(), capBac, heSo, phuCap,
                    txtMoTa.getText().trim());
            if (!kq.isSuccess()) {
                DialogUtil.showError(this, kq.getMessage());
                return;
            }

            refreshTable();
            DialogUtil.showSuccess(this, kq.getMessage());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Cap bac, he so, phu cap phai la so hop le.", "Loi nhap lieu",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showDetailDialog() {
        int row = table.getSelectedRow();
        if (row == -1) return;

        int modelRow = table.convertRowIndexToModel(row);
        String ma = (String) tableModel.getValueAt(modelRow, 0);
        ChucVu pos = service.getById(ma);
        if (pos == null) return;

        boolean canEdit = SessionContext.getInstance().coQuyen("POSITION_MANAGE");

        Frame frame = (Frame) SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(frame, "Chi tiet chuc vu - " + pos.getTenChucVu(), true);

        JTextField txtMa = new JTextField(pos.getId());
        txtMa.setEnabled(false);
        JTextField txtTen = new JTextField(pos.getTenChucVu());
        JTextField txtCapBac = new JTextField(String.valueOf(pos.getCapBac()));
        JTextField txtHeSo = new JTextField(String.valueOf(pos.getHeSoLuong()));
        JTextField txtPhuCap = new JTextField(String.valueOf(pos.getPhuCapChucVu()));
        JTextArea txtMoTa = new JTextArea(pos.getMoTa() != null ? pos.getMoTa() : "", 3, 20);
        txtMoTa.setLineWrap(true);

        JComboBox<String> cboTrangThai = new JComboBox<>(new String[]{STATUS_ACTIVE, STATUS_INACTIVE});
        cboTrangThai.setSelectedItem(toTrangThaiDisplay(pos.getTrangThai()));
        cboTrangThai.setEnabled(canEdit);

        for (JComponent c : new JComponent[]{txtTen, txtCapBac, txtHeSo, txtPhuCap, txtMoTa}) {
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
                "Ma chuc vu:", "Ten chuc vu (*):", "Cap bac:", "He so luong (*):",
                "Phu cap (VND):", "Mo ta:", "Trang thai:"
        };
        JComponent[] flds = {txtMa, txtTen, txtCapBac, txtHeSo, txtPhuCap, new JScrollPane(txtMoTa), cboTrangThai};

        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = 0;
            gbc.gridy = i;
            gbc.fill = GridBagConstraints.NONE;
            gbc.weightx = 0;

            JLabel lbl = new JLabel(labels[i]);
            lbl.setFont(com.hrm.util.UIFonts.TEXT_NORMAL);
            form.add(lbl, gbc);

            gbc.gridx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1.0;
            flds[i].setPreferredSize(new Dimension(210, i == 5 ? 60 : 28));
            form.add(flds[i], gbc);
        }

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        JButton btnHuy = UIHelper.createDefaultButton("Huy");
        btnHuy.addActionListener(e -> dialog.dispose());

        JButton btnLichSuBtn = UIHelper.createDefaultButton("Xem lich su he so");
        btnLichSuBtn.addActionListener(e -> showHistoryDialog());
        btnPanel.add(btnLichSuBtn);
        btnPanel.add(btnHuy);

        if (canEdit) {
            JButton btnLuu = UIHelper.createSuccessButton("Luu");
            btnLuu.addActionListener(e -> {
                try {
                    int capBac = Integer.parseInt(txtCapBac.getText().trim());
                    double heSo = Double.parseDouble(txtHeSo.getText().trim());
                    double phuCap = Double.parseDouble(txtPhuCap.getText().trim());

                    KetQua<Void> kqCapNhat = service.updatePosition(
                            ma,
                            txtTen.getText().trim(),
                            capBac,
                            heSo,
                            phuCap,
                            txtMoTa.getText().trim()
                    );
                    if (!kqCapNhat.isSuccess()) {
                        DialogUtil.showError(dialog, kqCapNhat.getMessage());
                        return;
                    }

                    String rawTrangThaiMoi = toTrangThaiRaw((String) cboTrangThai.getSelectedItem());
                    if (!normalizeTrangThai(rawTrangThaiMoi).equals(normalizeTrangThai(pos.getTrangThai()))) {
                        KetQua<Void> kqTrangThai;
                        if ("hoat_dong".equals(rawTrangThaiMoi)) {
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
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(dialog,
                            "Cap bac, he so, phu cap phai la so hop le.",
                            "Loi nhap lieu",
                            JOptionPane.ERROR_MESSAGE);
                }
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

    private void showHistoryDialog() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Vui long chon mot chuc vu de xem lich su.");
            return;
        }

        int modelRow = table.convertRowIndexToModel(row);
        String ma = (String) tableModel.getValueAt(modelRow, 0);
        String ten = (String) tableModel.getValueAt(modelRow, 1);

        List<LichSuHeSoLuong> danhSach = service.getHistoryByMaChucVu(ma);

        DefaultTableModel histModel = new DefaultTableModel(
                new Object[]{"Ngay thay doi", "He so cu", "He so moi", "Phu cap cu (VND)", "Phu cap moi (VND)", "Nguoi thay doi"},
                0
        ) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        for (LichSuHeSoLuong h : danhSach) {
            histModel.addRow(new Object[]{
                    h.getNgayThayDoi(),
                    h.getHeSoLuongCu() + "x",
                    h.getHeSoLuongMoi() + "x",
                    moneyFmt.format(h.getPhuCapCu()),
                    moneyFmt.format(h.getPhuCapMoi()),
                    h.getNguoiThayDoi()
            });
        }

        JTable histTable = new JTable(histModel);
        histTable.setRowHeight(24);
        JScrollPane scroll = new JScrollPane(histTable);
        scroll.setPreferredSize(new Dimension(640, 200));

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "Lich su he so luong - " + ten + " (" + ma + ")", true);
        dialog.setLayout(new BorderLayout());

        if (danhSach.isEmpty()) {
            dialog.add(new JLabel("  Chua co lich su thay doi nao.", SwingConstants.CENTER), BorderLayout.CENTER);
        } else {
            dialog.add(scroll, BorderLayout.CENTER);
        }

        JButton btnDong = new JButton("Dong");
        btnDong.addActionListener(e -> dialog.dispose());
        JPanel footer = new JPanel();
        footer.add(btnDong);
        dialog.add(footer, BorderLayout.SOUTH);

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private String toTrangThaiDisplay(String raw) {
        String normalized = normalizeTrangThai(raw);
        if ("hoatdong".equals(normalized)) return STATUS_ACTIVE;
        if ("ngunghoatdong".equals(normalized) || "ngung".equals(normalized)) return STATUS_INACTIVE;
        return raw == null ? "" : raw;
    }

    private String toTrangThaiRaw(String display) {
        String normalized = normalizeTrangThai(display);
        if ("hoatdong".equals(normalized)) return "hoat_dong";
        if ("ngunghoatdong".equals(normalized) || "ngung".equals(normalized)) return "ngung";
        return display == null ? "" : display;
    }

    private String normalizeTrangThai(String value) {
        if (value == null) return "";
        String v = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .replace('đ', 'd')
                .replace('Đ', 'D')
                .toLowerCase()
                .trim();
        return v.replace("_", "").replace(" ", "").replace("-", "");
    }
}
