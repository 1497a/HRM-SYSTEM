package com.hrm.gui.admin;

import com.hrm.model.ChucVu;
import com.hrm.model.LichSuHeSoLuong;
import com.hrm.dao.ChucVuDAO;
import com.hrm.bus.ChucVuBUS;
import com.hrm.util.SessionContext;
import com.hrm.util.UIHelper;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class PositionPanel extends JPanel {

    private ChucVuBUS service = new ChucVuBUS();
    private JTable table;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    private NumberFormat moneyFmt = NumberFormat.getNumberInstance(new Locale("vi", "VN"));

    private JTextField txtSearch;
    private JComboBox<String> cboFilter;

    // Buttons cần phân quyền
    private JButton btnThem;

    public PositionPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // ── PANEL TRÊN: Tiêu đề + Tìm kiếm + Lọc
        JPanel topPanel = new JPanel(new BorderLayout());

        JLabel title = new JLabel("QUẢN LÝ CHỨC VỤ");
        title.setFont(new Font("Arial", Font.BOLD, 16));
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        topPanel.add(title, BorderLayout.NORTH);

        // Gợi ý tìm kiếm
        JLabel lblHint = new JLabel("Tìm theo: Mã / Tên chức vụ / Trạng thái");
        lblHint.setFont(new Font("Arial", Font.ITALIC, 11));
        topPanel.add(lblHint, BorderLayout.SOUTH);

        // Panel chứa search + filter
        JPanel searchFilterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JLabel lblSearch = new JLabel("Tìm kiếm:");
        txtSearch = new JTextField(20);
        txtSearch.setToolTipText("Nhập mã hoặc tên chức vụ để tìm kiếm");

        JLabel lblFilter = new JLabel("    Trạng thái:");
        cboFilter = new JComboBox<>(new String[] {
                "\u0054\u1ea5\u0074\u0020\u0063\u1ea3",
                "\u0048\u006f\u1ea1\u0074\u0020\u0111\u1ed9\u006e\u0067",
                "\u004e\u0067\u1eeb\u006e\u0067\u0020\u0068\u006f\u1ea1\u0074\u0020\u0111\u1ed9\u006e\u0067"
        });

        searchFilterPanel.add(lblSearch);
        searchFilterPanel.add(txtSearch);
        searchFilterPanel.add(lblFilter);
        searchFilterPanel.add(cboFilter);

        topPanel.add(searchFilterPanel, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);

        // ── BẢNG
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

        // Thêm sorter để có thể filter
        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        add(new JScrollPane(table), BorderLayout.CENTER);

        // ── THANH NÚT
        btnThem = UIHelper.createPrimaryButton("+ Thêm");

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnPanel.add(btnThem);
        add(btnPanel, BorderLayout.SOUTH);

        // ── PHÂN QUYỀN
        setupPermissions();

        // ── SỰ KIỆN
        btnThem.addActionListener(e -> showAddDialog());

        // Tìm kiếm realtime
        txtSearch.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent e) {
                applyFilter();
            }
        });

        // Lọc theo trạng thái
        cboFilter.addActionListener(e -> applyFilter());

        // Double-click mở dialog chi tiết
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    showDetailDialog();
                }
            }
        });

        refreshTable();
    }

    // ── PHÂN QUYỀN

    private void setupPermissions() {
        btnThem.setVisible(SessionContext.getInstance().coQuyen("POSITION_MANAGE"));
    }

    // ── LỌC DỮ LIỆU

    private void applyFilter() {
        String searchText = txtSearch.getText().toLowerCase().trim();
        int statusFilterIndex = cboFilter.getSelectedIndex();

        RowFilter<DefaultTableModel, Object> rf = new RowFilter<DefaultTableModel, Object>() {
            public boolean include(Entry<? extends DefaultTableModel, ? extends Object> entry) {
                // Lọc theo mã (cột 0) hoặc tên (cột 1)
                String ma = entry.getStringValue(0).toLowerCase();
                String tenChucVu = entry.getStringValue(1).toLowerCase();
                boolean matchSearch = searchText.isEmpty() || ma.contains(searchText) || tenChucVu.contains(searchText);

                // Lọc theo trạng thái (cột 5)
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

    // ── LÀM MỚI BẢNG

    private void refreshTable() {
        tableModel.setRowCount(0);
        for (ChucVu p : service.getAllPositions()) {
            tableModel.addRow(new Object[] {
                    p.getId(),
                    p.getTenChucVu(),
                    "Cap " + p.getCapBac(),
                    p.getHeSoLuong() + "x",
                    moneyFmt.format(p.getPhuCapChucVu()),
                    toTrangThaiDisplay(p.getTrangThai())
            });
        }

        // Reset filter sau khi refresh
        txtSearch.setText("");
        cboFilter.setSelectedIndex(0);
    }

    // ── FORM THÊM

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
            // Kiểm tra trùng mã chức vụ đang hoạt động
            if (new ChucVuDAO().existsActiveByCode(maChucVu)) {
                JOptionPane.showMessageDialog(this,
                        "Ma chuc vu '" + maChucVu + "' da ton tai va dang hoat dong. Vui long dung ma khac.",
                        "Trung ma chuc vu", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int capBac = Integer.parseInt(txtCapBac.getText().trim());
            double heSo = Double.parseDouble(txtHeSo.getText().trim());
            double phuCap = Double.parseDouble(txtPhuCap.getText().trim());

            service.addPosition(maChucVu, txtTen.getText().trim(), capBac, heSo, phuCap,
                    txtMoTa.getText().trim());
            refreshTable();
            JOptionPane.showMessageDialog(this, "Them chuc vu thanh cong!");

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Cap bac, he so, phu cap phai la so hop le.", "Loi nhap lieu",
                    JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Loi", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── DIALOG CHI TIẾT / SỬA (double-click)

    private void showDetailDialog() {
        int row = table.getSelectedRow();
        if (row == -1) return;
        int modelRow = table.convertRowIndexToModel(row);
        String ma = (String) tableModel.getValueAt(modelRow, 0);
        ChucVu pos = service.getById(ma);
        if (pos == null) return;

        boolean canEdit = SessionContext.getInstance().coQuyen("POSITION_MANAGE");

        Frame frame = (Frame) SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(frame, "Chi tiết chức vụ - " + pos.getTenChucVu(), true);

        JTextField txtMa = new JTextField(pos.getId());
        txtMa.setEnabled(false);
        JTextField txtTen = new JTextField(pos.getTenChucVu());
        JTextField txtCapBac = new JTextField(String.valueOf(pos.getCapBac()));
        JTextField txtHeSo = new JTextField(String.valueOf(pos.getHeSoLuong()));
        JTextField txtPhuCap = new JTextField(String.valueOf(pos.getPhuCapChucVu()));
        JTextArea txtMoTa = new JTextArea(pos.getMoTa() != null ? pos.getMoTa() : "", 3, 20);
        txtMoTa.setLineWrap(true);
        JComboBox<String> cboTrangThai = new JComboBox<>(new String[]{
                "\u0048\u006f\u1ea1\u0074\u0020\u0111\u1ed9\u006e\u0067",
                "\u004e\u0067\u1eeb\u006e\u0067\u0020\u0068\u006f\u1ea1\u0074\u0020\u0111\u1ed9\u006e\u0067"
        });
        cboTrangThai.setSelectedItem(toTrangThaiDisplay(pos.getTrangThai()));
        cboTrangThai.setEnabled(canEdit);

        for (JComponent c : new JComponent[]{txtTen, txtCapBac, txtHeSo, txtPhuCap, txtMoTa}) {
            if (c instanceof JTextField) ((JTextField) c).setEditable(canEdit);
            else ((JTextArea) c).setEditable(canEdit);
        }

        // Form
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(16, 16, 8, 16));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 8, 5, 8);
        gbc.anchor = GridBagConstraints.WEST;

        String[] labels = {"Mã chức vụ:", "Tên chức vụ (*):", "Cấp bậc:", "Hệ số lương (*):", "Phụ cấp (VND):", "Mô tả:", "Trạng thái:"};
        JComponent[] flds = {txtMa, txtTen, txtCapBac, txtHeSo, txtPhuCap, new JScrollPane(txtMoTa), cboTrangThai};
        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = 0; gbc.gridy = i; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
            JLabel lbl = new JLabel(labels[i]);
            lbl.setFont(com.hrm.util.UIFonts.TEXT_NORMAL);
            form.add(lbl, gbc);
            gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
            flds[i].setPreferredSize(new Dimension(210, i == 5 ? 60 : 28));
            form.add(flds[i], gbc);
        }

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        JButton btnHuy = UIHelper.createDefaultButton("Hủy");
        btnHuy.addActionListener(e -> dialog.dispose());

        JButton btnLichSuBtn = UIHelper.createDefaultButton("Xem lịch sử hệ số");
        btnLichSuBtn.addActionListener(e -> showHistoryDialog());
        btnPanel.add(btnLichSuBtn);
        btnPanel.add(btnHuy);

        if (canEdit) {
            JButton btnLuu = UIHelper.createSuccessButton("Lưu");
            btnLuu.addActionListener(e -> {
                try {
                    int capBac = Integer.parseInt(txtCapBac.getText().trim());
                    double heSo = Double.parseDouble(txtHeSo.getText().trim());
                    double phuCap = Double.parseDouble(txtPhuCap.getText().trim());
                    service.updatePosition(ma, txtTen.getText().trim(), capBac, heSo, phuCap, txtMoTa.getText().trim());
                    String rawTrangThaiMoi = toTrangThaiRaw((String) cboTrangThai.getSelectedItem());
                    if (!normalizeTrangThai(rawTrangThaiMoi).equals(normalizeTrangThai(pos.getTrangThai()))) {
                        if ("hoat_dong".equals(rawTrangThaiMoi)) {
                            service.activatePosition(ma);
                        } else {
                            service.deactivatePosition(ma);
                        }
                    }
                    refreshTable();
                    dialog.dispose();
                    JOptionPane.showMessageDialog(PositionPanel.this, "Cập nhật chức vụ thành công!");
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(dialog, "Cap bac, he so, phu cap phai la so hop le.", "Loi nhap lieu", JOptionPane.ERROR_MESSAGE);
                } catch (IllegalArgumentException ex) {
                    JOptionPane.showMessageDialog(dialog, ex.getMessage(), "Loi", JOptionPane.ERROR_MESSAGE);
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

    // ── DIALOG XEM LỊCH SỬ

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
                new Object[] { "Ngay thay doi", "He so cu", "He so moi", "Phu cap cu (VND)", "Phu cap moi (VND)",
                        "Nguoi thay doi" },
                0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        for (LichSuHeSoLuong h : danhSach) {
            histModel.addRow(new Object[] {
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
                "Lich su he so luong — " + ten + " (" + ma + ")", true);
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
        if ("hoatdong".equals(normalized)) return "\u0048\u006f\u1ea1\u0074\u0020\u0111\u1ed9\u006e\u0067";
        if ("ngunghoatdong".equals(normalized) || "ngung".equals(normalized)) {
            return "\u004e\u0067\u1eeb\u006e\u0067\u0020\u0068\u006f\u1ea1\u0074\u0020\u0111\u1ed9\u006e\u0067";
        }
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
        String v = value.toLowerCase().trim();
        v = v.replace("áº¡", "a").replace("ạ", "a")
             .replace("á»™", "o").replace("ộ", "o")
             .replace("á»«", "u").replace("ừ", "u")
             .replace("á»", "o").replace("ờ", "o")
             .replace("Ä‘", "d").replace("đ", "d");
        v = v.replace("_", "").replace(" ", "").replace("-", "");
        return v;
    }
}
