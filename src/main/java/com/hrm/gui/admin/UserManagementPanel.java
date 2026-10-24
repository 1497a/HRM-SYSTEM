package com.hrm.gui.admin;

import com.hrm.bus.XacThucBUS;
import com.hrm.gui.components.PurpleTable;
import com.hrm.model.TaiKhoan;
import com.hrm.model.VaiTro;
import com.hrm.util.DialogUtil;
import com.hrm.util.PermissionCodes;
import com.hrm.util.SessionContext;
import com.hrm.util.UIColors;
import com.hrm.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.Comparator;
import java.util.List;

/**
 * TaiKhoan Management Panel - CRUD operations for users
 * Module 9: Phân quyền và bảo mật
 */
public class UserManagementPanel extends JPanel {
    private final XacThucBUS authService;
    private final SessionContext sessionContext;
    private PurpleTable table;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    private JTextField txtSearch;
    private JComboBox<String> cboTrangThai;
    private JComboBox<String> cboVaiTro;
    private JButton btnCreate;
    private JButton btnEdit;

    public UserManagementPanel() {
        this.authService = XacThucBUS.getInstance();
        this.sessionContext = SessionContext.getInstance();
        initComponents();
        setupLayout();
        loadData();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(Color.WHITE);

        txtSearch = new JTextField(20);
        txtSearch.putClientProperty("JTextField.placeholderText", "Tìm kiếm theo tên đăng nhập...");
        txtSearch.addActionListener(e -> searchUsers());

        btnCreate = UIHelper.createPrimaryButton("Tạo mới");
        btnCreate.addActionListener(e -> createUser());

        btnEdit = UIHelper.createPrimaryButton("Sửa");
        btnEdit.addActionListener(e -> editUser());
        btnEdit.setEnabled(false);

        String[] columns = {"ID", "Tên đăng nhập", "Họ tên", "Email", "Vai trò", "Trạng thái"};
        tableModel = PurpleTable.createNonEditableModel(columns);
        table = new PurpleTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(0).setPreferredWidth(40);
        table.getColumnModel().getColumn(1).setPreferredWidth(100);
        table.getColumnModel().getColumn(2).setPreferredWidth(150);
        table.getColumnModel().getColumn(3).setPreferredWidth(180);
        table.getColumnModel().getColumn(4).setPreferredWidth(150);
        table.getColumnModel().getColumn(5).setPreferredWidth(80);
        table.getColumnModel().getColumn(5).setCellRenderer(new com.hrm.gui.components.StatusCellRenderer());

        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);
        for (int i = 0; i < 6; i++) {
            sorter.setSortable(i, false);
        }
        sorter.setSortable(0, true);
        sorter.setSortable(2, true);
        sorter.setComparator(0, Comparator.comparingInt(a -> (Integer) a));
        sorter.setComparator(2, UIHelper.vietnameseNameComparator());
        sorter.setSortKeys(List.of(new RowSorter.SortKey(0, SortOrder.ASCENDING)));
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateEditButtonState();
            }
        });

        cboTrangThai = new JComboBox<>(new String[]{"Tất cả", "Hoạt động", "Bị khóa"});
        cboTrangThai.addActionListener(e -> applyFilter());

        cboVaiTro = new JComboBox<>();
        cboVaiTro.addItem("Tất cả vai trò");
        for (VaiTro vt : authService.getAllRoles()) {
            cboVaiTro.addItem(vt.getTenVaiTro());
        }
        cboVaiTro.addActionListener(e -> applyFilter());
    }

    private void setupLayout() {
        JPanel topPanel = new JPanel(new BorderLayout(10, 4));
        topPanel.setOpaque(false);

        JLabel lblHint = new JLabel("Tìm theo: Tên đăng nhập / Họ tên");
        lblHint.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblHint.setForeground(UIColors.TEXT_DARK);
        topPanel.add(lblHint, BorderLayout.SOUTH);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        searchPanel.setOpaque(false);
        searchPanel.add(new JLabel("Tìm kiếm:"));
        searchPanel.add(txtSearch);
        searchPanel.add(new JLabel("Trạng thái:"));
        searchPanel.add(cboTrangThai);
        searchPanel.add(new JLabel("Vai trò:"));
        searchPanel.add(cboVaiTro);
        topPanel.add(searchPanel, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        southPanel.setOpaque(false);
        if (sessionContext.hasPermission(PermissionCodes.USER_CREATE)) {
            southPanel.add(btnCreate);
        }
        if (sessionContext.hasPermission(PermissionCodes.USER_UPDATE)) {
            southPanel.add(btnEdit);
        }
        JButton btnLamMoi = UIHelper.createDefaultButton("Làm mới");
        btnLamMoi.addActionListener(e -> { txtSearch.setText(""); cboTrangThai.setSelectedIndex(0); cboVaiTro.setSelectedIndex(0); loadData(); applyFilter(); });
        southPanel.add(btnLamMoi);

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(southPanel, BorderLayout.SOUTH);
    }

    private void loadData() {
        tableModel.setRowCount(0);
        List<TaiKhoan> users = authService.getAllUsers();
        for (TaiKhoan user : users) {
            String status = user.isBiKhoa() ? "Bị khóa" : (user.isHoatDong() ? "Hoạt động" : "Ngừng");
            Object[] row = {
                    user.getId(),
                    user.getTenDangNhap(),
                    user.getHoTen(),
                    user.getEmail(),
                    user.getTenVaiTro(),
                    status
            };
            tableModel.addRow(row);
        }
        table.clearSelection();
        updateEditButtonState();
    }

    private void searchUsers() {
        applyFilter();
    }

    private void applyFilter() {
        String keyword = UIHelper.normalizeSearch(txtSearch.getText());
        String trangThai = (String) cboTrangThai.getSelectedItem();
        String vaiTro = (String) cboVaiTro.getSelectedItem();

        RowFilter<DefaultTableModel, Object> nameFilter = keyword.isEmpty() ? null :
                new RowFilter<DefaultTableModel, Object>() {
                    @Override
                    public boolean include(Entry<? extends DefaultTableModel, ? extends Object> entry) {
                        Object tenDangNhap = entry.getValue(1);
                        Object hoTen = entry.getValue(2);
                        String normalizedUsername = UIHelper.normalizeSearch(tenDangNhap != null ? tenDangNhap.toString() : "");
                        String normalizedFullName = UIHelper.normalizeSearch(hoTen != null ? hoTen.toString() : "");
                        return normalizedUsername.contains(keyword) || normalizedFullName.contains(keyword);
                    }
                };

        RowFilter<DefaultTableModel, Object> statusFilter =
                (trangThai == null || "Tất cả".equals(trangThai)) ? null
                        : RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(trangThai), 5);

        RowFilter<DefaultTableModel, Object> roleFilter =
                (vaiTro == null || "Tất cả vai trò".equals(vaiTro)) ? null
                        : RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(vaiTro), 4);

        List<RowFilter<DefaultTableModel, Object>> active = new java.util.ArrayList<>();
        if (nameFilter != null) active.add(nameFilter);
        if (statusFilter != null) active.add(statusFilter);
        if (roleFilter != null) active.add(roleFilter);

        if (active.isEmpty()) {
            sorter.setRowFilter(null);
        } else if (active.size() == 1) {
            sorter.setRowFilter(active.get(0));
        } else {
            sorter.setRowFilter(RowFilter.andFilter(active));
        }
        table.clearSelection();
        updateEditButtonState();
    }

    private void updateEditButtonState() {
        btnEdit.setEnabled(table.getSelectedRow() >= 0);
    }

    private void createUser() {
        UserFormDialog dialog = new UserFormDialog((Frame) SwingUtilities.getWindowAncestor(this), null);
        dialog.setVisible(true);
        if (dialog.isSuccessful()) {
            loadData();
        }
    }

    private void editUser() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            DialogUtil.showWarn(this, "Vui lòng chọn tài khoản cần sửa.");
            return;
        }

        int modelRow = table.convertRowIndexToModel(selectedRow);
        int userId = (int) tableModel.getValueAt(modelRow, 0);
        TaiKhoan user = authService.getAllUsers().stream()
                .filter(u -> u.getId() == userId)
                .findFirst()
                .orElse(null);

        if (user != null) {
            UserFormDialog dialog = new UserFormDialog((Frame) SwingUtilities.getWindowAncestor(this), user);
            dialog.setVisible(true);
            if (dialog.isSuccessful() || dialog.isDeleted()) {
                loadData();
            }
        }
    }
}
