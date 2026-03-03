package com.hrm.gui.admin;

import com.hrm.gui.components.CheckboxTree;
import com.hrm.model.Quyen;
import com.hrm.model.VaiTro;
import com.hrm.bus.XacThucBUS;
import com.hrm.bus.KetQua;
import com.hrm.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Dialog tạo/sửa vai trò với ma trận phân quyền theo module x hành động.
 * Đã nâng cấp sử dụng CheckboxTree để hiển thị toàn bộ chi tiết quyền rõ ràng.
 */
public class RoleFormDialog extends JDialog {
    private final XacThucBUS authService;
    private final VaiTro editingRole;
    private boolean successful = false;

    private JTextField txtCode;
    private JTextField txtName;
    private JTextArea txtDescription;

    // Tree hiển thị quyền
    private CheckboxTree permissionTree;
    private CheckboxTree.CheckboxTreeNode rooTreeNode;

    // Tất cả permission (code -> Quyen)
    private final Map<String, Quyen> allPermMap = new HashMap<>();

    private static final Map<String, String> MODULE_VI;
    static {
        MODULE_VI = new LinkedHashMap<>();
        MODULE_VI.put("Employee",     "Nhân viên");
        MODULE_VI.put("Leave",        "Nghỉ phép");
        MODULE_VI.put("Evaluation",   "Đánh giá");
        MODULE_VI.put("TaiKhoan",         "Tài khoản");
        MODULE_VI.put("VaiTro",         "Vai trò");
        MODULE_VI.put("Report",       "Báo cáo");
        MODULE_VI.put("Settings",     "Cài đặt");
        MODULE_VI.put("Organization", "Tổ chức");
        MODULE_VI.put("Appointment",  "Bổ nhiệm");
        MODULE_VI.put("Attendance",   "Chấm công");
        MODULE_VI.put("Contract",     "Hợp đồng");
        MODULE_VI.put("Payroll",      "Lương");
        MODULE_VI.put("Recruitment",  "Tuyển dụng");
    }

    public RoleFormDialog(Frame parent, VaiTro role) {
        super(parent, role == null ? "Tạo vai trò mới" : "Sửa vai trò", true);
        this.authService = XacThucBUS.getInstance();
        this.editingRole = role;

        initComponents();
        setupLayout();
        if (role != null) {
            loadRoleData();
        }

        setSize(600, 650);
        setLocationRelativeTo(parent);
    }

    /** Tạo mã vai trò tự động dạng VT001, VT002, ... */
    private String generateRoleCode() {
        List<VaiTro> roles = authService.getAllRoles();
        int maxNum = 0;
        for (VaiTro r : roles) {
            String code = r.getCode();
            if (code != null && code.matches("VT\\d+")) {
                try {
                    int num = Integer.parseInt(code.substring(2));
                    if (num > maxNum) maxNum = num;
                } catch (NumberFormatException ignored) {}
            }
        }
        return String.format("VT%03d", maxNum + 1);
    }

    private void initComponents() {
        // Mã vai trò luôn chỉ đọc — tự sinh cho vai trò mới, cố định cho vai trò đang sửa
        txtCode = new JTextField(20);
        txtCode.setEditable(false);
        txtCode.setBackground(new Color(240, 240, 240));

        txtName = new JTextField(20);
        txtDescription = new JTextArea(3, 20);
        txtDescription.setLineWrap(true);
        txtDescription.setWrapStyleWord(true);

        if (editingRole == null) {
            txtCode.setText(generateRoleCode());
        }

        // Tải tất cả quyền
        List<Quyen> allPermissions = authService.getAllPermissions();
        for (Quyen p : allPermissions) {
            allPermMap.put(p.getCode(), p);
        }

        // Nhóm các quyền theo module
        Map<String, List<Quyen>> permsByModule = allPermissions.stream()
                .filter(p -> p.getModule() != null)
                .collect(Collectors.groupingBy(Quyen::getModule));

        // Xây dựng cây quyền
        rooTreeNode = new CheckboxTree.CheckboxTreeNode("Tất cả quyền hạn");
        
        List<String> modules = permsByModule.keySet().stream().sorted().collect(Collectors.toList());
        for (String module : modules) {
            String moduleName = MODULE_VI.getOrDefault(module, module);
            CheckboxTree.CheckboxTreeNode moduleNode = new CheckboxTree.CheckboxTreeNode(moduleName);
            rooTreeNode.add(moduleNode);

            List<Quyen> modulePerms = permsByModule.get(module);
            // Sắp xếp theo tên quyền để dễ nhìn
            modulePerms.sort(Comparator.comparing(Quyen::getName));
            
            for (Quyen p : modulePerms) {
                CheckboxTree.CheckboxTreeNode permNode = new CheckboxTree.CheckboxTreeNode(p.getName(), p.getCode());
                moduleNode.add(permNode);
            }
        }
        
        permissionTree = new CheckboxTree(rooTreeNode);
    }

    private void setupLayout() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // --- Phần form thông tin ---
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Mã vai trò:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(txtCode, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Tên vai trò:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(txtName, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        formPanel.add(new JLabel("Mô tả:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(new JScrollPane(txtDescription), gbc);

        // --- Danh sách phân quyền dạng Cây ---
        JScrollPane treeScroll = new JScrollPane(permissionTree);
        treeScroll.setBorder(new TitledBorder("Cây phân quyền chi tiết"));
        treeScroll.setPreferredSize(new Dimension(540, 350));
        
        // --- Nút Lưu / Hủy ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnSave = UIHelper.createSuccessButton("Lưu");
        btnSave.addActionListener(e -> save());
        JButton btnCancel = UIHelper.createDefaultButton("Hủy");
        btnCancel.addActionListener(e -> dispose());
        buttonPanel.add(btnSave);
        buttonPanel.add(btnCancel);

        JPanel topPanel = new JPanel(new BorderLayout(0, 4));
        topPanel.add(formPanel, BorderLayout.NORTH);

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(treeScroll, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private void loadRoleData() {
        txtCode.setText(editingRole.getCode());
        txtName.setText(editingRole.getName());
        txtDescription.setText(editingRole.getMoTa() != null ? editingRole.getMoTa() : "");

        Set<String> roleCodes = editingRole.getQuyens().stream()
                .map(Quyen::getCode).collect(Collectors.toSet());

        // Đệ quy để chọn các node trong cây
        checkNodesByCodes(rooTreeNode, roleCodes);
        
        // Cập nhật lại giao diện (vì load programatically không kích hoạt sự kiện click)
        permissionTree.repaint();
    }
    
    // Hàm phụ trợ xử lý chọn node từ CSDL
    private void checkNodesByCodes(CheckboxTree.CheckboxTreeNode node, Set<String> validCodes) {
        if (node.isLeaf()) {
            if (node.getUserObjectCode() != null && validCodes.contains(node.getUserObjectCode())) {
                node.setSelected(true);
            }
        } else {
            boolean allSelected = true;
            boolean anySelected = false;
            
            for (int i = 0; i < node.getChildCount(); i++) {
                CheckboxTree.CheckboxTreeNode child = (CheckboxTree.CheckboxTreeNode) node.getChildAt(i);
                checkNodesByCodes(child, validCodes);
                
                if (child.isSelected()) {
                    anySelected = true;
                } else if (child.isPartialSelection()) {
                    anySelected = true;
                    allSelected = false;
                } else {
                    allSelected = false;
                }
            }
            
            if (node.getChildCount() > 0) {
                if (allSelected) {
                    node.setSelected(true);
                } else if (anySelected) {
                    node.setPartialSelection(true);
                }
            }
        }
    }

    private void save() {
        String code = txtCode.getText().trim().toUpperCase();
        String name = txtName.getText().trim();
        String description = txtDescription.getText().trim();

        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập tên vai trò.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        List<String> selectedCodes = new ArrayList<>();
        List<CheckboxTree.CheckboxTreeNode> selectedLeaves = permissionTree.getSelectedLeaves();
        for (CheckboxTree.CheckboxTreeNode leave : selectedLeaves) {
            if (leave.getUserObjectCode() != null) {
                selectedCodes.add(leave.getUserObjectCode());
            }
        }

        if (editingRole == null) {
            KetQua<Void> createResult = authService.createRole(code, name, description.isEmpty() ? null : description);
            if (!createResult.isSuccess()) {
                JOptionPane.showMessageDialog(this, createResult.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            KetQua<Void> permResult = authService.setRolePermissions(code, selectedCodes);
            if (!permResult.isSuccess()) {
                JOptionPane.showMessageDialog(this,
                        "Đã tạo vai trò nhưng không thể gán quyền: " + permResult.getMessage(),
                        "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            }
        } else {
            editingRole.setTenVaiTro(name);
            editingRole.setMoTa(description);
            KetQua<Void> updateResult = authService.updateRole(editingRole);
            if (!updateResult.isSuccess()) {
                JOptionPane.showMessageDialog(this, updateResult.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            KetQua<Void> permResult = authService.setRolePermissions(editingRole.getCode(), selectedCodes);
            if (!permResult.isSuccess()) {
                JOptionPane.showMessageDialog(this,
                        "Đã cập nhật vai trò nhưng không thể cập nhật quyền: " + permResult.getMessage(),
                        "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            }
        }

        successful = true;
        JOptionPane.showMessageDialog(this,
                editingRole == null ? "Đã tạo vai trò thành công!" : "Đã cập nhật vai trò thành công!",
                "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        dispose();
    }

    public boolean isSuccessful() {
        return successful;
    }
}
