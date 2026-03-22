package com.hrm.gui.admin;

import com.hrm.gui.components.PurpleTable;
import com.hrm.model.TaiKhoan;
import com.hrm.model.VaiTro;
import com.hrm.bus.XacThucBUS;
import com.hrm.bus.KetQua;
import com.hrm.util.DialogUtil;
import com.hrm.util.PermissionCodes;
import com.hrm.util.SessionContext;
import com.hrm.util.UIColors;
import com.hrm.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
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
    private JButton btnDelete;
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
        // Search field
        txtSearch = new JTextField(20);
        txtSearch.putClientProperty("JTextField.placeholderText", "Tìm kiếm theo tên đăng nhập...");
        txtSearch.addActionListener(e -> searchUsers());
        // Buttons
        btnCreate = UIHelper.createPrimaryButton("Tạo mới");
        btnCreate.addActionListener(e -> createUser());
        btnEdit = UIHelper.createPrimaryButton("Sửa");
        btnEdit.addActionListener(e -> editUser());
        btnDelete = UIHelper.createDangerButton("Xóa");
        btnDelete.addActionListener(e -> deleteUser());
        // Table
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
        // Status cell renderer
        table.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    String status = (String) value;
                    if ("Hoạt động".equals(status)) {
                        c.setBackground(UIColors.BG_SUCCESS);
                    } else if ("Bị khóa".equals(status)) {
                        c.setBackground(UIColors.BG_DANGER);
                    } else {
                        c.setBackground(UIColors.BG_WARNING);
                    }
                }
                setHorizontalAlignment(SwingConstants.CENTER);
                return c;
            }
        });
        // Sorter – sort by name (col 2) using Vietnamese locale
        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);
        // Chỉ cho sort cột 0 (ID) và cột 2 (Họ tên)
        for (int i = 0; i < 6; i++) {
            sorter.setSortable(i, false);
        }
        sorter.setSortable(0, true); // ID
        sorter.setSortable(2, true); // Họ tên
        // Comparator đúng kiểu
        sorter.setComparator(0, Comparator.comparingInt(a -> (Integer) a)); // sort số nguyên
        sorter.setComparator(2, UIHelper.vietnameseNameComparator());       // sort tiếng Việt
        // Mặc định sort theo ID tăng dần
        sorter.setSortKeys(List.of(new RowSorter.SortKey(0, SortOrder.ASCENDING)));
        // Status filter combo
        cboTrangThai = new JComboBox<>(new String[]{"Tất cả", "Hoạt động", "Bị khóa"});
        cboTrangThai.addActionListener(e -> applyFilter());
        // Role filter combo
        cboVaiTro = new JComboBox<>();
        cboVaiTro.addItem("Tất cả vai trò");
        for (VaiTro vt : authService.getAllRoles()) {
            cboVaiTro.addItem(vt.getTenVaiTro());
        }
        cboVaiTro.addActionListener(e -> applyFilter());
    }

    private void setupLayout() {
        // Top panel - hint + search/filters
        JPanel topPanel = new JPanel(new BorderLayout(10, 4));
        topPanel.setOpaque(false);
        JLabel lblHint = new JLabel("Tìm theo: Tên đăng nhập / Họ tên. Nhấp đúp vào dòng để sửa.");
        lblHint.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblHint.setForeground(UIColors.TEXT_DARK);
        topPanel.add(lblHint, BorderLayout.NORTH);
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        searchPanel.setOpaque(false);
        searchPanel.add(new JLabel("Tìm kiếm:"));
        searchPanel.add(txtSearch);
        searchPanel.add(new JLabel("Trạng thái:"));
        searchPanel.add(cboTrangThai);
        searchPanel.add(new JLabel("Vai trò:"));
        searchPanel.add(cboVaiTro);
        topPanel.add(searchPanel, BorderLayout.CENTER);
        // Center - table
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        // South - action buttons
        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        southPanel.setOpaque(false);
        if (sessionContext.hasPermission(PermissionCodes.USER_CREATE)) {
            southPanel.add(btnCreate);
        }
        if (sessionContext.hasPermission(PermissionCodes.USER_UPDATE)) {
            southPanel.add(btnEdit);
        }
        if (sessionContext.hasPermission(PermissionCodes.USER_DELETE)) {
            southPanel.add(btnDelete);
        }
        JButton btnLamMoi = UIHelper.createDefaultButton("Làm mới");
        btnLamMoi.addActionListener(e -> loadData());
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
    }

    private void searchUsers() {
        applyFilter();
    }

    private void applyFilter() {
        String keyword = txtSearch.getText().toLowerCase().trim();
        String trangThai = (String) cboTrangThai.getSelectedItem();
        String vaiTro = (String) cboVaiTro.getSelectedItem();
        RowFilter<DefaultTableModel, Object> nameFilter = keyword.isEmpty() ? null :
            RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(keyword), 1, 2);
        RowFilter<DefaultTableModel, Object> statusFilter = (trangThai == null || "Tất cả".equals(trangThai)) ? null :
            RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(trangThai), 5);
        RowFilter<DefaultTableModel, Object> roleFilter = (vaiTro == null || "Tất cả vai trò".equals(vaiTro)) ? null :
            RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(vaiTro), 4);
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
    }

    private void createUser() {
        UserFormDialog dialog = new UserFormDialog(
                (Frame) SwingUtilities.getWindowAncestor(this), null);
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
            UserFormDialog dialog = new UserFormDialog(
                    (Frame) SwingUtilities.getWindowAncestor(this), user);
            dialog.setVisible(true);
            if (dialog.isSuccessful()) {
                loadData();
            }
        }
    }

    private void deleteUser() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            DialogUtil.showWarn(this, "Vui lòng chọn tài khoản cần xóa.");
            return;
        }
        int modelRow = table.convertRowIndexToModel(selectedRow);
        int userId = (int) tableModel.getValueAt(modelRow, 0);
        String username = (String) tableModel.getValueAt(modelRow, 1);
        if (DialogUtil.showYesNoWarning(this,
                "Bạn có chắc muốn xóa tài khoản '" + username + "'?",
                "Xác nhận xóa")) {
            KetQua<Void> result = authService.deleteUser(userId);
            if (result.isSuccess()) {
                DialogUtil.showInfo(this, "Đã xóa tài khoản thành công!");
                loadData();
            } else {
                DialogUtil.showError(this, result.getMessage());
            }
        }
    }

}
