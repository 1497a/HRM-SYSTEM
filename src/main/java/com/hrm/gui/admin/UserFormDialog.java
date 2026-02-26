package com.hrm.gui.admin;

import com.hrm.model.Role;
import com.hrm.model.User;
import com.hrm.service.AuthService;
import com.hrm.service.ServiceResult;
import com.hrm.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

/**
 * User Form Dialog - Create/Edit user accounts
 */
public class UserFormDialog extends JDialog {
    private final AuthService authService;
    private final User editingUser;
    private boolean successful = false;

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JTextField txtFullName;
    private JTextField txtEmail;
    private JPanel rolesPanel;
    private ButtonGroup roleGroup;
    private JCheckBox chkActive;
    private JCheckBox chkLocked;

    public UserFormDialog(Frame parent, User user) {
        super(parent, user == null ? "Tao tai khoan moi" : "Sua tai khoan", true);
        this.authService = AuthService.getInstance();
        this.editingUser = user;

        initComponents();
        setupLayout();
        if (user != null) {
            loadUserData();
        }

        setSize(450, 520);
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        txtUsername = new JTextField(20);
        txtPassword = new JPasswordField(20);
        txtFullName = new JTextField(20);
        txtEmail = new JTextField(20);
        chkActive = new JCheckBox("Hoat dong");
        chkActive.setSelected(true);
        chkLocked = new JCheckBox("Khoa tai khoan");

        // Roles panel with radio buttons (single role per account)
        rolesPanel = new JPanel();
        rolesPanel.setLayout(new BoxLayout(rolesPanel, BoxLayout.Y_AXIS));
        roleGroup = new ButtonGroup();

        List<Role> roles = authService.getAllRoles();
        for (Role role : roles) {
            JRadioButton rb = new JRadioButton(role.getName() + " (" + role.getCode() + ")");
            rb.setActionCommand(role.getCode());
            roleGroup.add(rb);
            rolesPanel.add(rb);
        }

        // Select first role by default when creating
        if (editingUser == null && rolesPanel.getComponentCount() > 0) {
            Component first = rolesPanel.getComponent(0);
            if (first instanceof JRadioButton) {
                ((JRadioButton) first).setSelected(true);
            }
        }

        if (editingUser != null) {
            txtUsername.setEditable(false);
        }
    }

    private void setupLayout() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Username
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Ten dang nhap:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(txtUsername, gbc);

        // Password
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel(editingUser == null ? "Mat khau:" : "Mat khau moi:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(txtPassword, gbc);

        // Full name
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Ho ten:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(txtFullName, gbc);

        // Email
        gbc.gridx = 0; gbc.gridy = 3; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(txtEmail, gbc);

        // Roles
        gbc.gridx = 0; gbc.gridy = 4; gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        formPanel.add(new JLabel("Vai tro:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        JScrollPane rolesScroll = new JScrollPane(rolesPanel);
        rolesScroll.setPreferredSize(new Dimension(250, 120));
        formPanel.add(rolesScroll, gbc);

        // Status
        gbc.gridx = 0; gbc.gridy = 5; gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(new JLabel("Trang thai:"), gbc);
        gbc.gridx = 1;
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        statusPanel.add(chkActive);
        statusPanel.add(chkLocked);
        formPanel.add(statusPanel, gbc);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnSave = UIHelper.createSuccessButton("Luu");
        btnSave.addActionListener(e -> save());
        JButton btnCancel = UIHelper.createDefaultButton("Huy");
        btnCancel.addActionListener(e -> dispose());
        buttonPanel.add(btnSave);
        buttonPanel.add(btnCancel);

        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private void loadUserData() {
        txtUsername.setText(editingUser.getUsername());
        txtFullName.setText(editingUser.getFullName());
        txtEmail.setText(editingUser.getEmail());
        chkActive.setSelected(editingUser.isActive());
        chkLocked.setSelected(editingUser.isLocked());

        // Select the radio button matching user's current role
        String currentRoleCode = null;
        if (editingUser.getRoles() != null && !editingUser.getRoles().isEmpty()) {
            currentRoleCode = editingUser.getRoles().get(0).getCode();
        }

        for (Component comp : rolesPanel.getComponents()) {
            if (comp instanceof JRadioButton) {
                JRadioButton rb = (JRadioButton) comp;
                if (rb.getActionCommand().equals(currentRoleCode)) {
                    rb.setSelected(true);
                    break;
                }
            }
        }
    }

    private void save() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());
        String fullName = txtFullName.getText().trim();
        String email = txtEmail.getText().trim();

        // Validation
        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui long nhap ten dang nhap", "Loi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (fullName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui long nhap ho ten", "Loi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (editingUser == null && password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui long nhap mat khau", "Loi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Get selected role code from radio buttons
        String selectedRoleCode = null;
        ButtonModel selectedModel = roleGroup.getSelection();
        if (selectedModel != null) {
            selectedRoleCode = selectedModel.getActionCommand();
        }
        if (selectedRoleCode == null || selectedRoleCode.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui long chon vai tro", "Loi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (editingUser == null) {
            // Create new user
            ServiceResult<Integer> result = authService.createUser(username, password, null, selectedRoleCode, email);
            if (!result.isSuccess()) {
                JOptionPane.showMessageDialog(this, result.getMessage(), "Loi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            successful = true;
            JOptionPane.showMessageDialog(this,
                    "Da tao tai khoan thanh cong!",
                    "Thong bao",
                    JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            // Update existing user
            editingUser.setFullName(fullName);
            editingUser.setEmail(email);
            editingUser.setActive(chkActive.isSelected());
            editingUser.setLocked(chkLocked.isSelected());

            // Update role: find the selected Role object and replace current roles list
            final String roleCode = selectedRoleCode;
            Role selectedRole = authService.getAllRoles().stream()
                    .filter(r -> r.getCode().equals(roleCode))
                    .findFirst()
                    .orElse(null);
            if (selectedRole != null) {
                editingUser.getRoles().clear();
                editingUser.getRoles().add(selectedRole);
            }

            ServiceResult<Void> result = authService.updateUser(editingUser);
            if (!result.isSuccess()) {
                JOptionPane.showMessageDialog(this, result.getMessage(), "Loi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // If a new password was provided, inform the user that password change is done via Settings
            if (!password.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Thong tin tai khoan da duoc cap nhat.\nDe doi mat khau, vui long su dung chuc nang Doi mat khau trong Cai dat.",
                        "Thong bao",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Da cap nhat tai khoan thanh cong!",
                        "Thong bao",
                        JOptionPane.INFORMATION_MESSAGE);
            }

            successful = true;
            dispose();
        }
    }

    public boolean isSuccessful() {
        return successful;
    }
}
