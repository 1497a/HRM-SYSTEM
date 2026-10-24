package com.hrm.gui.admin;

import com.hrm.bus.KetQua;
import com.hrm.bus.XacThucBUS;
import com.hrm.gui.components.BaseFormDialog;
import com.hrm.model.DataScope;
import com.hrm.model.Quyen;
import com.hrm.model.VaiTro;
import com.hrm.util.PermissionCodes;
import com.hrm.util.SessionContext;
import com.hrm.util.UIHelper;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RoleFormDialog extends BaseFormDialog {
    private static final ScopeOption[] SCOPE_OPTIONS = {
            new ScopeOption(DataScope.NONE, "Không cấp quyền"),
            new ScopeOption(DataScope.SELF, "Cá nhân"),
            new ScopeOption(DataScope.TEAM, "Nhóm trực tiếp"),
            new ScopeOption(DataScope.DEPT, "Phòng ban"),
            new ScopeOption(DataScope.ALL, "Toàn bộ")
    };

    /** Dùng cho quyền không cần scope (hành động nhị phân: có/không). */
    private static final ScopeOption[] BINARY_SCOPE_OPTIONS = {
            new ScopeOption(DataScope.NONE, "Không cấp quyền"),
            new ScopeOption(DataScope.ALL, "Có quyền")
    };

    private final XacThucBUS authService;
    private final VaiTro editingRole;
    private final SessionContext sessionContext;
    private final Map<String, Quyen> allPermMap = new HashMap<>();
    /** modelRow -> true nếu quyền đó cần scope (coPhamVi=true), false nếu là quyền nhị phân. */
    private final Map<Integer, Boolean> rowCoPhamVi = new HashMap<>();
    private boolean successful = false;
    private boolean deleted = false;
    private JTextField txtCode;
    private JTextField txtName;
    private JTextArea txtDescription;
    private JTextField txtPermissionSearch;
    private JTable permissionTable;
    private DefaultTableModel permissionTableModel;
    private TableRowSorter<DefaultTableModel> permissionSorter;

    public RoleFormDialog(Frame parent, VaiTro role) {
        super(parent, role == null ? "Tạo vai trò mới" : "Sửa vai trò", true);
        this.authService = XacThucBUS.getInstance();
        this.editingRole = role;
        this.sessionContext = SessionContext.getInstance();
        initComponents();
        setupLayout();
        loadPermissionRows();
        if (role != null) {
            loadRoleData();
        }
        setSize(960, 720);
        setLocationRelativeTo(parent);
    }

    private String generateRoleCode() {
        List<VaiTro> roles = authService.getAllRoles();
        int maxNum = 0;
        for (VaiTro role : roles) {
            String code = role.getId();
            if (code == null || !code.matches("VT\\d+")) {
                continue;
            }
            try {
                maxNum = Math.max(maxNum, Integer.parseInt(code.substring(2)));
            } catch (NumberFormatException ignored) {
            }
        }
        return String.format("VT%03d", maxNum + 1);
    }

    private void initComponents() {
        txtCode = new JTextField(20);
        txtCode.setEditable(false);
        txtCode.setBackground(Color.WHITE);

        txtName = new JTextField(20);

        txtDescription = new JTextArea(3, 20);
        txtDescription.setLineWrap(true);
        txtDescription.setWrapStyleWord(true);

        txtPermissionSearch = UIHelper.createSearchField("Tìm theo tên quyền...");

        if (editingRole == null) {
            txtCode.setText(generateRoleCode());
        }

        permissionTableModel = new DefaultTableModel(
                new Object[]{"Mã quyền", "Tên quyền", "Mô tả quyền", "Nhóm quyền", "Phạm vi"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4;
            }
        };

        permissionTable = new JTable(permissionTableModel);
        permissionTable.setRowHeight(28);
        permissionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        permissionSorter = new TableRowSorter<>(permissionTableModel);
        permissionSorter.setComparator(1, Comparator.nullsLast(String::compareToIgnoreCase));
        permissionSorter.setComparator(3, Comparator.nullsLast(String::compareToIgnoreCase));
        permissionTable.setRowSorter(permissionSorter);
        UIHelper.attachTextSearch(txtPermissionSearch, permissionSorter, 1);

        TableColumn scopeColumn = permissionTable.getColumnModel().getColumn(4);
        scopeColumn.setCellEditor(new ScopeCellEditor());
        scopeColumn.setCellRenderer(new ScopeCellRenderer());
    }

    private void setupLayout() {
        JPanel mainPanel = createMainPanel();

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = UIHelper.gbc(0, 0);

        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Mã vai trò:"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        formPanel.add(txtCode, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Tên vai trò:"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        formPanel.add(txtName, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        formPanel.add(new JLabel("Mô tả:"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        formPanel.add(new JScrollPane(txtDescription), gbc);

        JPanel notePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        notePanel.setOpaque(false);
        notePanel.add(new JLabel("Nếu chọn \"Không cấp quyền\" thì quyền đó sẽ không được gán cho vai trò."));

        JPanel scopeHelpPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        scopeHelpPanel.setOpaque(false);
        scopeHelpPanel.add(new JLabel("Phạm vi: Cá nhân = chỉ bản thân, Nhóm trực tiếp = cấp dưới trực tiếp, Phòng ban = toàn bộ nhân sự trong phòng, Toàn bộ = toàn công ty. Quyền hành động chỉ có Không cấp / Có quyền."));

        JPanel topPanel = new JPanel(new BorderLayout(0, 8));
        topPanel.add(formPanel, BorderLayout.NORTH);

        JPanel helpPanel = new JPanel(new GridLayout(2, 1, 0, 4));
        helpPanel.setOpaque(false);
        helpPanel.add(notePanel);
        helpPanel.add(scopeHelpPanel);
        topPanel.add(helpPanel, BorderLayout.SOUTH);

        JPanel permissionPanel = new JPanel(new BorderLayout(0, 8));
        permissionPanel.setOpaque(false);
        permissionPanel.setBorder(new TitledBorder("Danh sách quyền và phạm vi"));

        JLabel lblSearchHint = new JLabel("Tìm theo: Tên quyền");
        lblSearchHint.setFont(new Font("Segoe UI", Font.ITALIC, 12));

        JPanel searchPanel = new JPanel(new BorderLayout(0, 4));
        searchPanel.setOpaque(false);

        JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        searchRow.setOpaque(false);
        searchRow.add(new JLabel("Tìm kiếm:"));
        searchRow.add(txtPermissionSearch);

        searchPanel.add(searchRow, BorderLayout.NORTH);
        searchPanel.add(lblSearchHint, BorderLayout.SOUTH);

        JScrollPane tableScroll = createScrollPane(permissionTable);
        tableScroll.setBorder(BorderFactory.createEmptyBorder());

        permissionPanel.add(searchPanel, BorderLayout.NORTH);
        permissionPanel.add(tableScroll, BorderLayout.CENTER);

        JPanel buttonPanel = createButtonPanel();

        JButton btnSave = UIHelper.createSuccessButton("Lưu");
        btnSave.addActionListener(e -> save());

        if (editingRole != null && sessionContext.hasPermission(PermissionCodes.ROLE_DELETE)) {
            JButton btnDelete = UIHelper.createDangerButton("Xóa");
            btnDelete.addActionListener(e -> deleteRole());
            buttonPanel.add(btnDelete);
        }

        JButton btnCancel = UIHelper.createDefaultButton("Hủy");
        btnCancel.addActionListener(e -> dispose());

        buttonPanel.add(btnSave);
        buttonPanel.add(btnCancel);

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(permissionPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        setContentPane(mainPanel);
    }

    private void loadPermissionRows() {
        permissionTableModel.setRowCount(0);
        rowCoPhamVi.clear();
        allPermMap.clear();

        List<Quyen> permissions = new ArrayList<>(authService.getAllPermissions());
        permissions.sort(Comparator
                .comparing(Quyen::getNhomQuyen, Comparator.nullsLast(String::compareToIgnoreCase))
                .thenComparing(Quyen::getTenQuyen, Comparator.nullsLast(String::compareToIgnoreCase)));

        for (Quyen permission : permissions) {
            allPermMap.put(permission.getId(), permission);
            int row = permissionTableModel.getRowCount();
            rowCoPhamVi.put(row, permission.isCoPhamVi());
            permissionTableModel.addRow(new Object[]{
                    permission.getId(),
                    safe(permission.getTenQuyen()),
                    safe(permission.getMoTa()),
                    safe(permission.getNhomQuyen()),
                    DataScope.NONE
            });
        }
    }

    private void loadRoleData() {
        txtCode.setText(editingRole.getId());
        txtName.setText(editingRole.getTenVaiTro());
        txtDescription.setText(editingRole.getMoTa() != null ? editingRole.getMoTa() : "");

        Map<String, DataScope> scopeByPermission = new HashMap<>();
        Map<String, Boolean> grantedByPermission = new HashMap<>();
        for (Quyen permission : editingRole.getQuyens()) {
            grantedByPermission.put(permission.getId(), true);
            scopeByPermission.put(permission.getId(),
                    permission.getPhamVi() != null ? permission.getPhamVi() : DataScope.NONE);
        }

        for (int row = 0; row < permissionTableModel.getRowCount(); row++) {
            String permissionCode = String.valueOf(permissionTableModel.getValueAt(row, 0));
            boolean needsScope = rowCoPhamVi.getOrDefault(row, true);
            boolean granted = grantedByPermission.getOrDefault(permissionCode, false);
            DataScope scope = scopeByPermission.getOrDefault(permissionCode, DataScope.NONE);
            if (!needsScope) {
                scope = granted ? DataScope.ALL : DataScope.NONE;
            }
            permissionTableModel.setValueAt(scope, row, 4);
        }
    }

    private void save() {
        String code = txtCode.getText().trim().toUpperCase();
        String name = txtName.getText().trim();
        String description = txtDescription.getText().trim();

        if (name.isEmpty()) {
            showError("Vui lòng nhập tên vai trò.");
            txtName.requestFocus();
            return;
        }

        if (permissionTable.isEditing()) {
            permissionTable.getCellEditor().stopCellEditing();
        }

        List<Quyen> selectedPermissions = new ArrayList<>();
        for (int row = 0; row < permissionTableModel.getRowCount(); row++) {
            String permissionCode = String.valueOf(permissionTableModel.getValueAt(row, 0));
            DataScope scope = extractScope(permissionTableModel.getValueAt(row, 4));
            if (scope == null) {
                showError("Phạm vi không hợp lệ cho quyền " + permissionCode + ".");
                return;
            }
            if (scope == DataScope.NONE) {
                continue;
            }

            Quyen source = allPermMap.get(permissionCode);
            if (source == null) {
                continue;
            }

            Quyen permission = new Quyen(source.getId(), source.getTenQuyen(), source.getNhomQuyen());
            permission.setMoTa(source.getMoTa());
            permission.setCoPhamVi(source.isCoPhamVi());
            if (source.isCoPhamVi()) {
                permission.setPhamVi(scope);
            }
            selectedPermissions.add(permission);
        }

        if (editingRole == null) {
            KetQua<Void> createResult = authService.createRole(code, name, description.isEmpty() ? null : description);
            if (!createResult.isSuccess()) {
                showError(createResult.getMessage());
                return;
            }

            KetQua<Void> permissionResult = authService.setRolePermissions(code, selectedPermissions);
            if (!permissionResult.isSuccess()) {
                showWarning("Đã tạo vai trò nhưng không thể gán quyền: " + permissionResult.getMessage());
            }
        } else {
            editingRole.setTenVaiTro(name);
            editingRole.setMoTa(description.isEmpty() ? null : description);

            KetQua<Void> updateResult = authService.updateRole(editingRole);
            if (!updateResult.isSuccess()) {
                showError(updateResult.getMessage());
                return;
            }

            KetQua<Void> permissionResult = authService.setRolePermissions(editingRole.getId(), selectedPermissions);
            if (!permissionResult.isSuccess()) {
                showWarning("Đã cập nhật vai trò nhưng không thể cập nhật quyền: " + permissionResult.getMessage());
            }
        }

        successful = true;
        showInfo(editingRole == null ? "Đã tạo vai trò thành công." : "Đã cập nhật vai trò thành công.");
        dispose();
    }

    private void deleteRole() {
        if (editingRole == null) {
            return;
        }

        if (permissionTable.isEditing()) {
            permissionTable.getCellEditor().stopCellEditing();
        }

        String roleCode = editingRole.getId();
        if (!showYesNoWarning("Bạn có chắc muốn xóa vai trò '" + roleCode + "'?", "Xác nhận xóa")) {
            return;
        }

        KetQua<Void> result = authService.deleteRole(roleCode);
        if (!result.isSuccess()) {
            showError(result.getMessage());
            return;
        }

        deleted = true;
        successful = false;
        showInfo("Đã xóa vai trò thành công.");
        dispose();
    }

    public boolean isSuccessful() {
        return successful;
    }

    public boolean isDeleted() {
        return deleted;
    }

    private DataScope extractScope(Object value) {
        if (value instanceof DataScope) {
            return (DataScope) value;
        }
        if (value instanceof ScopeOption) {
            return ((ScopeOption) value).scope;
        }
        if (value instanceof String) {
            for (ScopeOption option : SCOPE_OPTIONS) {
                if (option.label.equals(value) || option.scope.name().equals(value)) {
                    return option.scope;
                }
            }
        }
        return null;
    }

    private String safe(String value) {
        return value != null ? value : "";
    }

    private class ScopeCellEditor extends AbstractCellEditor implements javax.swing.table.TableCellEditor {
        private final JComboBox<ScopeOption> fullCombo = new JComboBox<>(SCOPE_OPTIONS);
        private final JComboBox<ScopeOption> binaryCombo = new JComboBox<>(BINARY_SCOPE_OPTIONS);
        private JComboBox<ScopeOption> activeCombo = fullCombo;

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            int modelRow = table.convertRowIndexToModel(row);
            boolean needsScope = rowCoPhamVi.getOrDefault(modelRow, true);
            activeCombo = needsScope ? fullCombo : binaryCombo;
            DataScope current = extractScope(value);
            if (!needsScope && current != null && current != DataScope.NONE) {
                current = DataScope.ALL;
            }

            ScopeOption[] opts = needsScope ? SCOPE_OPTIONS : BINARY_SCOPE_OPTIONS;
            ScopeOption selected = opts[0];
            for (ScopeOption opt : opts) {
                if (opt.scope == current) {
                    selected = opt;
                    break;
                }
            }
            activeCombo.setSelectedItem(selected);
            return activeCombo;
        }

        @Override
        public Object getCellEditorValue() {
            return activeCombo.getSelectedItem();
        }
    }

    private class ScopeCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setHorizontalAlignment(SwingConstants.CENTER);

            DataScope scope = null;
            if (value instanceof DataScope) {
                scope = (DataScope) value;
            } else if (value instanceof ScopeOption) {
                scope = ((ScopeOption) value).scope;
            }

            if (scope != null) {
                int modelRow = table.convertRowIndexToModel(row);
                boolean needsScope = rowCoPhamVi.getOrDefault(modelRow, true);
                setText(!needsScope
                        ? (scope == DataScope.NONE ? "Không cấp quyền" : "Có quyền")
                        : toLabel(scope));
            } else {
                setText(value != null ? value.toString() : "");
            }
            return this;
        }

        private String toLabel(DataScope scope) {
            for (ScopeOption option : SCOPE_OPTIONS) {
                if (option.scope == scope) {
                    return option.label;
                }
            }
            return scope != null ? scope.name() : "";
        }
    }

    private static class ScopeOption {
        private final DataScope scope;
        private final String label;

        private ScopeOption(DataScope scope, String label) {
            this.scope = scope;
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }
}
