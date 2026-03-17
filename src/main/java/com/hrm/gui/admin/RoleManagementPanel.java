package com.hrm.gui.admin;

import com.hrm.model.VaiTro;
import com.hrm.bus.XacThucBUS;
import com.hrm.bus.KetQua;
import com.hrm.util.PermissionCodes;
import com.hrm.util.SessionContext;
import com.hrm.util.UIColors;
import com.hrm.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
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

    private JTable table;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    private JButton btnCreate;
    private JButton btnEdit;
    private JButton btnDelete;


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
        setBackground(UIColors.LIGHT_GRAY_BG);

        // Buttons
        btnCreate = UIHelper.createPrimaryButton("Tạo mới");
        btnCreate.addActionListener(e -> createRole());

        btnEdit = UIHelper.createPrimaryButton("Sửa");
        btnEdit.addActionListener(e -> editRole());

        btnDelete = UIHelper.createDangerButton("Xóa");
        btnDelete.addActionListener(e -> deleteRole());

        // Table
        String[] columns = {"Mã vai trò", "Tên vai trò", "Mô tả", "Số quyền", "Hệ thống"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setRowHeight(30);
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
                        c.setBackground(com.hrm.util.UIColors.LIGHT_YELLOW_BG);
                    } else {
                        c.setBackground(com.hrm.util.UIColors.WHITE);
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
    }

    private void setupLayout() {
        // Top panel - buttons
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setOpaque(false);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);
        JButton btnLamMoi = new JButton("Làm mới");
        btnLamMoi.addActionListener(e -> loadData());
        buttonPanel.add(btnLamMoi);
        if (sessionContext.hasPermission(PermissionCodes.ROLE_CREATE)) {
            buttonPanel.add(btnCreate);
        }
        if (sessionContext.hasPermission(PermissionCodes.ROLE_UPDATE)) {
            buttonPanel.add(btnEdit);
        }
        if (sessionContext.hasPermission(PermissionCodes.ROLE_DELETE)) {
            buttonPanel.add(btnDelete);
        }

        topPanel.add(buttonPanel, BorderLayout.EAST);

        // Center - table
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(new TitledBorder("Danh sách vai trò"));

        // Info panel
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        infoPanel.setOpaque(false);
        infoPanel.add(new JLabel("Lưu ý: Vai trò hệ thống (ADMIN) không thể xóa."));

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(infoPanel, BorderLayout.SOUTH);
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
            JOptionPane.showMessageDialog(this,
                    "Vui lòng chọn vai trò cần sửa.",
                    "Thông báo",
                    JOptionPane.WARNING_MESSAGE);
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
            JOptionPane.showMessageDialog(this,
                    "Vui lòng chọn vai trò cần xóa.",
                    "Thông báo",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = table.convertRowIndexToModel(selectedRow);
        String roleCode = (String) tableModel.getValueAt(modelRow, 0);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn xóa vai trò '" + roleCode + "'?",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            KetQua<Void> result = authService.deleteRole(roleCode);
            if (result.isSuccess()) {
                JOptionPane.showMessageDialog(this,
                        "Đã xóa vai trò thành công!",
                        "Thông báo",
                        JOptionPane.INFORMATION_MESSAGE);
                loadData();
            } else {
                JOptionPane.showMessageDialog(this,
                        result.getMessage(),
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
