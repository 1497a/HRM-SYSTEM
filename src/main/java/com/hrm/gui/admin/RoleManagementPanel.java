package com.hrm.gui.admin;

import com.hrm.model.VaiTro;
import com.hrm.bus.XacThucBUS;
import com.hrm.bus.KetQua;
import com.hrm.util.DialogUtil;
import com.hrm.util.PermissionCodes;
import com.hrm.util.SessionContext;
import com.hrm.gui.components.PurpleTable;
import com.hrm.util.UIColors;
import com.hrm.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.List;

/**
 * VaiTro Management Panel - CRUD operations for roles with permission assignment
 * Module 9: Phân quyền và bảo mật
 */
public class RoleManagementPanel extends JPanel {
    private final XacThucBUS authService;
    private final SessionContext sessionContext;
    private PurpleTable table;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    private JButton btnCreate;
    private JButton btnEdit;
    private JButton btnDelete;
    private JTextField txtTimKiem;
    public RoleManagementPanel() {
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
        // Buttons
        btnCreate = UIHelper.createPrimaryButton("Tạo mới");
        btnCreate.addActionListener(e -> createRole());
        btnEdit = UIHelper.createPrimaryButton("Sửa");
        btnEdit.addActionListener(e -> editRole());
        btnDelete = UIHelper.createDangerButton("Xóa");
        btnDelete.addActionListener(e -> deleteRole());
        // Table
        String[] columns = {"Mã vai trò", "Tên vai trò", "Mô tả", "Số quyền", "Hệ thống"};
        tableModel = PurpleTable.createNonEditableModel(columns);
        table = new PurpleTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(0).setPreferredWidth(100);
        table.getColumnModel().getColumn(1).setPreferredWidth(150);
        table.getColumnModel().getColumn(2).setPreferredWidth(300);
        table.getColumnModel().getColumn(3).setPreferredWidth(80);
        table.getColumnModel().getColumn(4).setPreferredWidth(80);
        // System role cell renderer
        table.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    String isSystem = (String) value;
                    if ("Có".equals(isSystem)) {
                        c.setBackground(UIColors.BG_WARNING);
                    } else {
                        c.setBackground(Color.WHITE);
                    }
                }
                setHorizontalAlignment(SwingConstants.CENTER);
                return c;
            }
        });
        // Sorter – sort by role name (col 1) using Vietnamese locale
        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);
        sorter.setComparator(1, UIHelper.vietnameseNameComparator());
        sorter.setSortKeys(List.of(new RowSorter.SortKey(1, SortOrder.ASCENDING)));
        txtTimKiem = UIHelper.createSearchField("Tìm theo mã hoặc tên vai trò...");
        UIHelper.attachTextSearch(txtTimKiem, sorter, 0, 1);
    }

    private void setupLayout() {
        // NORTH: hint + search
        JLabel lblHint = new JLabel("Tìm theo: Mã vai trò / Tên vai trò");
        lblHint.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblHint.setForeground(UIColors.TEXT_DARK);
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        searchPanel.setOpaque(false);
        searchPanel.add(new JLabel("Tìm kiếm:"));
        searchPanel.add(txtTimKiem);
        JPanel northPanel = new JPanel(new BorderLayout(0, 2));
        northPanel.setOpaque(false);
        northPanel.add(searchPanel, BorderLayout.NORTH);
        northPanel.add(lblHint, BorderLayout.SOUTH);
        // CENTER: table
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        // SOUTH: action buttons + info note
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        buttonPanel.setOpaque(false);
        JButton btnLamMoi = UIHelper.createDefaultButton("Làm mới");
        btnLamMoi.addActionListener(e -> loadData());
        buttonPanel.add(btnLamMoi);
        if (sessionContext.hasPermission(PermissionCodes.ROLE_UPDATE)) {
            JButton btnMapping = UIHelper.createDefaultButton("Cấu hình Chức vụ → Vai trò");
            btnMapping.addActionListener(e -> {
                Frame frame = (Frame) SwingUtilities.getWindowAncestor(this);
                new ChucVuRoleMappingDialog(frame).setVisible(true);
            });
            buttonPanel.add(btnMapping);
        }
        if (sessionContext.hasPermission(PermissionCodes.ROLE_CREATE)) buttonPanel.add(btnCreate);
        if (sessionContext.hasPermission(PermissionCodes.ROLE_UPDATE)) buttonPanel.add(btnEdit);
        if (sessionContext.hasPermission(PermissionCodes.ROLE_DELETE)) buttonPanel.add(btnDelete);
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        infoPanel.setOpaque(false);
        infoPanel.add(new JLabel("Lưu ý: Vai trò hệ thống (ADMIN) không thể xóa."));
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.setOpaque(false);
        southPanel.add(buttonPanel, BorderLayout.NORTH);
        southPanel.add(infoPanel, BorderLayout.SOUTH);
        add(northPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(southPanel, BorderLayout.SOUTH);
    }

    private void loadData() {
        tableModel.setRowCount(0);
        List<VaiTro> roles = authService.getAllRoles();
        for (VaiTro role : roles) {
            Object[] row = {
                role.getId(),
                role.getTenVaiTro(),
                role.getMoTa(),
                role.getQuyens().size(),
                role.isLaHeThong() ? "Có" : "-"
            };
            tableModel.addRow(row);
        }
    }

    private void createRole() {
        RoleFormDialog dialog = new RoleFormDialog(
                (Frame) SwingUtilities.getWindowAncestor(this), null);
        dialog.setVisible(true);
        if (dialog.isSuccessful()) {
            loadData();
        }
    }

    private void editRole() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            DialogUtil.showWarn(this, "Vui lòng chọn vai trò cần sửa.");
            return;
        }
        int modelRow = table.convertRowIndexToModel(selectedRow);
        String roleCode = (String) tableModel.getValueAt(modelRow, 0);
        VaiTro role = authService.getRoleByCode(roleCode);
        if (role != null) {
            RoleFormDialog dialog = new RoleFormDialog(
                    (Frame) SwingUtilities.getWindowAncestor(this), role);
            dialog.setVisible(true);
            if (dialog.isSuccessful()) {
                loadData();
            }
        }
    }

    private void deleteRole() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            DialogUtil.showWarn(this, "Vui lòng chọn vai trò cần xóa.");
            return;
        }
        int modelRow = table.convertRowIndexToModel(selectedRow);
        String roleCode = (String) tableModel.getValueAt(modelRow, 0);
        if (DialogUtil.showYesNoWarning(this,
                "Bạn có chắc muốn xóa vai trò '" + roleCode + "'?",
                "Xác nhận xóa")) {
            KetQua<Void> result = authService.deleteRole(roleCode);
            if (result.isSuccess()) {
                DialogUtil.showInfo(this, "Đã xóa vai trò thành công!");
                loadData();
            } else {
                DialogUtil.showError(this, result.getMessage());
            }
        }
    }
}
