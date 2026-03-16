package com.hrm.gui.employee;

import com.hrm.bus.NhanVienBUS;
import com.hrm.bus.PhongBanBUS;
import com.hrm.gui.components.PurpleButton;
import com.hrm.gui.components.PurpleTable;
import com.hrm.model.NhanVien;
import com.hrm.model.PhongBan;
import com.hrm.util.SessionContext;
import com.hrm.util.UIColors;
import com.hrm.util.UIHelper;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel danh sách hồ sơ nhân viên.
 */
public class EmployeeListPanel extends JPanel {

    private final NhanVienBUS nvService = NhanVienBUS.getInstance();
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private PurpleTable table;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;

    private JTextField txtSearch;
    private JComboBox<String> cboTrangThai;
    private JComboBox<String> cboPhongBan;

    private PurpleButton btnThem;
    private PurpleButton btnChiTiet;

    private List<NhanVien> danhSachHienThi = new ArrayList<>();

    private static final String[] COL_NAMES = {
        "STT", "Mã NV", "Họ tên", "Phòng ban", "Chức vụ",
        "Ngày vào làm", "Loại HĐ", "Trạng thái"
    };

    public EmployeeListPanel() {
        setLayout(new BorderLayout(0, 8));
        setBackground(UIColors.LIGHT_GRAY_BG);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        add(buildNorthPanel(), BorderLayout.NORTH);
        add(buildCenterPanel(), BorderLayout.CENTER);
        add(buildSouthPanel(), BorderLayout.SOUTH);

        setupPermissions();
        setupEvents();
        refreshTable();
    }

    private JPanel buildNorthPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setOpaque(false);

        JPanel searchWrap = new JPanel(new BorderLayout(0, 4));
        searchWrap.setOpaque(false);

        JLabel lblHint = new JLabel("Tìm theo: Mã NV / Họ tên. Nhấp đúp vào dòng để xem hồ sơ chi tiết.");
        lblHint.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblHint.setForeground(UIColors.TEXT_DARK);
        searchWrap.add(lblHint, BorderLayout.NORTH);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        searchPanel.setOpaque(false);

        JLabel lblSearch = new JLabel("Tìm kiếm:");
        lblSearch.setFont(com.hrm.util.UIFonts.TEXT_MEDIUM);
        lblSearch.setForeground(UIColors.TEXT_DARK);

        txtSearch = new JTextField(20);
        txtSearch.setFont(com.hrm.util.UIFonts.TEXT_MEDIUM);
        txtSearch.setPreferredSize(new Dimension(220, 32));

        JLabel lblPhongBan = new JLabel("Phòng ban:");
        lblPhongBan.setFont(com.hrm.util.UIFonts.TEXT_MEDIUM);
        lblPhongBan.setForeground(UIColors.TEXT_DARK);

        cboPhongBan = new JComboBox<>(new String[]{"Tất cả phòng ban"});
        cboPhongBan.setFont(com.hrm.util.UIFonts.TEXT_MEDIUM);
        cboPhongBan.setPreferredSize(new Dimension(180, 32));

        JLabel lblTrangThai = new JLabel("Trạng thái:");
        lblTrangThai.setFont(com.hrm.util.UIFonts.TEXT_MEDIUM);
        lblTrangThai.setForeground(UIColors.TEXT_DARK);

        cboTrangThai = new JComboBox<>(new String[]{
            "Tất cả", "Đang làm việc", "Tạm nghỉ", "Nghỉ việc"
        });
        cboTrangThai.setFont(com.hrm.util.UIFonts.TEXT_MEDIUM);
        cboTrangThai.setPreferredSize(new Dimension(160, 32));

        searchPanel.add(lblSearch);
        searchPanel.add(txtSearch);
        searchPanel.add(Box.createHorizontalStrut(4));
        searchPanel.add(lblPhongBan);
        searchPanel.add(cboPhongBan);
        searchPanel.add(Box.createHorizontalStrut(4));
        searchPanel.add(lblTrangThai);
        searchPanel.add(cboTrangThai);
        searchPanel.add(Box.createHorizontalStrut(8));

        searchWrap.add(searchPanel, BorderLayout.CENTER);
        panel.add(searchWrap, BorderLayout.CENTER);
        return panel;
    }

    private JScrollPane buildCenterPanel() {
        tableModel = PurpleTable.createNonEditableModel(COL_NAMES);
        table = new PurpleTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setDefaultRenderer(Object.class, new StatusColorRenderer());

        int[] widths = {45, 80, 160, 160, 140, 110, 130, 120};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        for (int i = 0; i < COL_NAMES.length; i++) {
            sorter.setSortable(i, false);
        }
        sorter.setSortable(1, true);
        sorter.setSortable(2, true);
        sorter.setComparator(2, UIHelper.vietnameseNameComparator());
        sorter.setSortKeys(List.of(new RowSorter.SortKey(1, SortOrder.ASCENDING)));

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(UIColors.BORDER_GRAY));
        return scroll;
    }

    private JPanel buildSouthPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        panel.setOpaque(false);

        btnThem = new PurpleButton("+ Tạo hồ sơ");
        btnChiTiet = new PurpleButton("Xem chi tiết");
        btnChiTiet.setEnabled(false);

        panel.add(btnThem);
        panel.add(btnChiTiet);

        JButton btnLamMoi = new JButton("Làm mới");
        btnLamMoi.addActionListener(e -> refreshTable());
        panel.add(btnLamMoi);

        return panel;
    }

    private void setupEvents() {
        btnThem.addActionListener(e -> showAddDialog());
        btnChiTiet.addActionListener(e -> showHoSoDialog());

        txtSearch.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                applyFilter();
            }
        });

        cboTrangThai.addActionListener(e -> applyFilter());
        cboPhongBan.addActionListener(e -> applyFilter());
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateActionButtons();
            }
        });

        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    showHoSoDialog();
                }
            }
        });
    }

    private void setupPermissions() {
        SessionContext sc = SessionContext.getInstance();
        boolean canCreate = sc.coVaiTro("ADMIN") || sc.coQuyen("EMPLOYEE_CREATE");
        btnThem.setVisible(canCreate);
        updateActionButtons();
    }

    public void refreshTable() {
        com.hrm.model.TaiKhoan currentUser = SessionContext.getInstance().getCurrentUser();
        String userId = currentUser != null ? currentUser.getNhanVienId() : null;
        danhSachHienThi = nvService.getAllByScope(userId);
        tableModel.setRowCount(0);
        int stt = 1;
        for (NhanVien nv : danhSachHienThi) {
            tableModel.addRow(new Object[]{
                stt++,
                nv.getMaNhanVien(),
                nv.getHoTen() != null ? nv.getHoTen() : "",
                nv.getTenPhongBanHienTai() != null ? nv.getTenPhongBanHienTai() : "",
                nv.getTenChucVuHienTai() != null ? nv.getTenChucVuHienTai() : "",
                nv.getNgayVaoLam() != null ? nv.getNgayVaoLam().format(dtf) : "",
                nv.getLoaiHopDongDisplay(),
                nv.getTrangThaiDisplay()
            });
        }
        rebuildDeptCombo();
        applyFilter();
        updateActionButtons();
    }

    private void rebuildDeptCombo() {
        String selected = (String) cboPhongBan.getSelectedItem();
        cboPhongBan.removeActionListener(e -> applyFilter());
        cboPhongBan.removeAllItems();
        cboPhongBan.addItem("Tất cả phòng ban");
        try {
            for (PhongBan d : new PhongBanBUS().getActiveDepartments()) {
                cboPhongBan.addItem(d.getTenPhongBan());
            }
        } catch (Exception ignored) {
        }
        if (selected != null) {
            for (int i = 0; i < cboPhongBan.getItemCount(); i++) {
                if (selected.equals(cboPhongBan.getItemAt(i))) {
                    cboPhongBan.setSelectedIndex(i);
                    break;
                }
            }
        }
        cboPhongBan.addActionListener(e -> applyFilter());
    }

    private void applyFilter() {
        String searchText = txtSearch.getText().toLowerCase().trim();
        String trangThaiFilter = (String) cboTrangThai.getSelectedItem();
        String phongBanFilter = (String) cboPhongBan.getSelectedItem();

        RowFilter<DefaultTableModel, Object> rf = new RowFilter<DefaultTableModel, Object>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ? extends Object> entry) {
                String maNV = entry.getStringValue(1).toLowerCase();
                String hoTen = entry.getStringValue(2).toLowerCase();
                boolean matchSearch = searchText.isEmpty()
                        || maNV.contains(searchText) || hoTen.contains(searchText);

                String tenPhongBan = entry.getStringValue(3);
                boolean matchDept = "Tất cả phòng ban".equals(phongBanFilter)
                        || phongBanFilter == null
                        || phongBanFilter.equals(tenPhongBan);

                String trangThai = entry.getStringValue(7);
                boolean matchStatus = true;
                if ("Đang làm việc".equals(trangThaiFilter)) {
                    matchStatus = trangThai.contains("lam viec") || trangThai.contains("Đang làm việc");
                } else if ("Tạm nghỉ".equals(trangThaiFilter)) {
                    matchStatus = trangThai.contains("Tạm nghỉ") || trangThai.contains("Tam nghi");
                } else if ("Nghỉ việc".equals(trangThaiFilter)) {
                    matchStatus = trangThai.contains("Nghỉ việc") || trangThai.contains("Nghi viec");
                }
                return matchSearch && matchDept && matchStatus;
            }
        };

        sorter.setRowFilter(rf);
        updateActionButtons();
    }

    private void showAddDialog() {
        EmployeeFormDialog dialog = new EmployeeFormDialog(
                (Frame) SwingUtilities.getWindowAncestor(this), null, null);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            refreshTable();
        }
    }

    private void showHoSoDialog() {
        NhanVien selected = getSelectedNhanVien();
        if (selected == null) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng chọn một nhân viên để xem chi tiết.",
                    "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        EmployeeDetailPanel dialog = new EmployeeDetailPanel(
                (Frame) SwingUtilities.getWindowAncestor(this), selected.getMaNhanVien());
        dialog.setVisible(true);
        if (dialog.isDataChanged()) {
            refreshTable();
        }
    }

    private void updateActionButtons() {
        if (btnChiTiet != null) {
            btnChiTiet.setEnabled(getSelectedNhanVien() != null);
        }
    }

    private NhanVien getSelectedNhanVien() {
        int viewRow = table.getSelectedRow();
        if (viewRow == -1) {
            return null;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        if (modelRow < 0 || modelRow >= tableModel.getRowCount()) {
            return null;
        }
        String maNV = (String) tableModel.getValueAt(modelRow, 1);
        for (NhanVien nv : danhSachHienThi) {
            if (maNV.equals(nv.getMaNhanVien())) {
                return nv;
            }
        }
        return null;
    }

    private class StatusColorRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object value,
                boolean isSelected, boolean hasFocus, int row, int col) {
            Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, col);
            setHorizontalAlignment(SwingConstants.CENTER);
            setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));

            if (!isSelected) {
                c.setBackground(row % 2 == 0 ? com.hrm.util.UIColors.WHITE : UIColors.TABLE_ALT_ROW);
                c.setForeground(UIColors.TEXT_DARK);

                if (col == 7 && value != null) {
                    String val = value.toString();
                    if (val.contains("lam viec") || val.contains("Đang")) {
                        c.setForeground(UIColors.SUCCESS_GREEN);
                    } else if (val.contains("Tạm nghỉ") || val.contains("Tam nghi")) {
                        c.setForeground(com.hrm.util.UIColors.WARNING_TEXT_AMBER);
                    } else if (val.contains("Nghỉ việc") || val.contains("Nghi viec")) {
                        c.setForeground(UIColors.DANGER_RED);
                    }
                    ((JLabel) c).setFont(com.hrm.util.UIFonts.BOLD_SMALL);
                }
            }
            return c;
        }
    }
}
