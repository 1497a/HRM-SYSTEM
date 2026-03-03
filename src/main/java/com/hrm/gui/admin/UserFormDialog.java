package com.hrm.gui.admin;

import com.hrm.model.NhanVien;
import com.hrm.model.VaiTro;
import com.hrm.model.TaiKhoan;
import com.hrm.bus.NhanVienBUS;
import com.hrm.bus.XacThucBUS;
import com.hrm.bus.KetQua;
import com.hrm.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

/**
 * TaiKhoan Form Dialog - Create/Edit user accounts
 * Khi tao moi: chon nhan vien tu danh sach, chi duoc 1 tai khoan/NV
 */
public class UserFormDialog extends JDialog {
    private final XacThucBUS authService;
    private final NhanVienBUS nhanVienService;
    private final TaiKhoan editingUser;
    private boolean successful = false;

    // For create mode
    private JComboBox<NhanVien> cboNhanVien;

    // For edit mode
    private JTextField txtUsername;
    private JTextField txtFullName;
    private JTextField txtEmail;

    private JPasswordField txtPassword;
    private JPanel rolesPanel;
    private ButtonGroup roleGroup;
    private JCheckBox chkActive;
    private JCheckBox chkLocked;

    public UserFormDialog(Frame parent, TaiKhoan user) {
        super(parent, user == null ? "Tao tai khoan moi" : "Sua tai khoan", true);
        this.authService = XacThucBUS.getInstance();
        this.nhanVienService = NhanVienBUS.getInstance();
        this.editingUser = user;

        initComponents();
        setupLayout();
        if (user != null) {
            loadUserData();
        }

        setSize(480, user == null ? 560 : 520);
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        txtPassword = new JPasswordField(20);
        chkActive = new JCheckBox("Hoat dong");
        chkActive.setSelected(true);
        chkLocked = new JCheckBox("Khoa tai khoan");

        if (editingUser == null) {
            // CREATE mode: dropdown nhan vien
            List<NhanVien> dsNV = nhanVienService.getDangLamViec();
            cboNhanVien = new JComboBox<>();
            for (NhanVien nv : dsNV) {
                cboNhanVien.addItem(nv);
            }
            cboNhanVien.setRenderer((list, value, index, isSelected, cellHasFocus) -> {
                JLabel label = new JLabel();
                if (value != null) {
                    String hoTen = value.getHoTen() != null ? value.getHoTen() : "";
                    String ma = value.getMaNhanVien() != null ? value.getMaNhanVien() : "";
                    label.setText(hoTen + " (" + ma + ")");
                }
                if (isSelected) {
                    label.setBackground(list.getSelectionBackground());
                    label.setForeground(list.getSelectionForeground());
                    label.setOpaque(true);
                }
                return label;
            });
        } else {
            // EDIT mode: text fields
            txtUsername = new JTextField(20);
            txtUsername.setEditable(false);
            txtFullName = new JTextField(20);
            txtEmail = new JTextField(20);
        }

        // Roles panel
        rolesPanel = new JPanel();
        rolesPanel.setLayout(new BoxLayout(rolesPanel, BoxLayout.Y_AXIS));
        roleGroup = new ButtonGroup();

        List<VaiTro> roles = authService.getAllRoles();
        for (VaiTro role : roles) {
            JRadioButton rb = new JRadioButton(role.getName() + " (" + role.getCode() + ")");
            rb.setActionCommand(role.getCode());
            roleGroup.add(rb);
            rolesPanel.add(rb);
        }

        if (editingUser == null && rolesPanel.getComponentCount() > 0) {
            Component first = rolesPanel.getComponent(0);
            if (first instanceof JRadioButton) {
                ((JRadioButton) first).setSelected(true);
            }
        }
    }

    private void setupLayout() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        int row = 0;

        if (editingUser == null) {
            // CREATE: show employee dropdown
            gbc.gridx = 0; gbc.gridy = row;
            gbc.anchor = GridBagConstraints.NORTHWEST;
            formPanel.add(new JLabel("Nhan vien:"), gbc);
            gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
            cboNhanVien.setPreferredSize(new Dimension(280, 28));
            formPanel.add(cboNhanVien, gbc);
            row++;

            // Password
            gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE;
            gbc.anchor = GridBagConstraints.WEST;
            formPanel.add(new JLabel("Mat khau:"), gbc);
            gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
            formPanel.add(txtPassword, gbc);
            row++;
        } else {
            // EDIT: show editable fields
            gbc.gridx = 0; gbc.gridy = row;
            formPanel.add(new JLabel("Ten dang nhap:"), gbc);
            gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
            formPanel.add(txtUsername, gbc);
            row++;

            gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE;
            formPanel.add(new JLabel("Mat khau moi:"), gbc);
            gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
            formPanel.add(txtPassword, gbc);
            row++;

            gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE;
            formPanel.add(new JLabel("Ho ten:"), gbc);
            gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
            formPanel.add(txtFullName, gbc);
            row++;

            gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE;
            formPanel.add(new JLabel("Email:"), gbc);
            gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
            formPanel.add(txtEmail, gbc);
            row++;
        }

        // Roles
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        formPanel.add(new JLabel("Vai tro:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        JScrollPane rolesScroll = new JScrollPane(rolesPanel);
        rolesScroll.setPreferredSize(new Dimension(250, 120));
        formPanel.add(rolesScroll, gbc);
        row++;

        // Status (edit only)
        if (editingUser != null) {
            gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE;
            gbc.anchor = GridBagConstraints.WEST;
            formPanel.add(new JLabel("Trang thai:"), gbc);
            gbc.gridx = 1;
            JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
            statusPanel.add(chkActive);
            statusPanel.add(chkLocked);
            formPanel.add(statusPanel, gbc);
        }

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
        if (editingUser == null) return;
        txtUsername.setText(editingUser.getUsername());
        txtFullName.setText(editingUser.getFullName());
        txtEmail.setText(editingUser.getEmail());
        chkActive.setSelected(editingUser.isActive());
        chkLocked.setSelected(editingUser.isLocked());

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
        String password = new String(txtPassword.getPassword());

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
            // CREATE: lay thong tin tu NV duoc chon
            NhanVien selectedNV = (NhanVien) cboNhanVien.getSelectedItem();
            if (selectedNV == null) {
                JOptionPane.showMessageDialog(this, "Vui long chon nhan vien", "Loi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui long nhap mat khau", "Loi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Username = ma nhan vien
            String username = selectedNV.getMaNhanVien();
            String email = null;
            // Try to get email from ThongTinCaNhan
            try {
                com.hrm.model.ThongTinCaNhan ttcn = nhanVienService.getThongTinCaNhan(selectedNV.getId());
                if (ttcn != null) email = ttcn.getEmail();
            } catch (Exception ex) {
                // ignore
            }

            KetQua<Integer> result = authService.createUser(username, password, selectedNV.getId(), selectedRoleCode, email);
            if (!result.isSuccess()) {
                JOptionPane.showMessageDialog(this, result.getMessage(), "Loi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            successful = true;
            JOptionPane.showMessageDialog(this,
                    "Da tao tai khoan thanh cong!\nTen dang nhap: " + username,
                    "Thong bao", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            // UPDATE
            String fullName = txtFullName.getText().trim();
            String email = txtEmail.getText().trim();
            if (fullName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui long nhap ho ten", "Loi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            editingUser.setHoTen(fullName);
            editingUser.setEmail(email);
            editingUser.setHoatDong(chkActive.isSelected());
            editingUser.setBiKhoa(chkLocked.isSelected());

            final String roleCode = selectedRoleCode;
            VaiTro selectedRole = authService.getAllRoles().stream()
                    .filter(r -> r.getCode().equals(roleCode))
                    .findFirst()
                    .orElse(null);
            if (selectedRole != null) {
                editingUser.getRoles().clear();
                editingUser.getRoles().add(selectedRole);
            }

            KetQua<Void> result = authService.updateUser(editingUser);
            if (!result.isSuccess()) {
                JOptionPane.showMessageDialog(this, result.getMessage(), "Loi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!password.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Thong tin tai khoan da duoc cap nhat.\nDe doi mat khau, su dung chuc nang Doi mat khau trong Cai dat.",
                        "Thong bao", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Da cap nhat tai khoan thanh cong!",
                        "Thong bao", JOptionPane.INFORMATION_MESSAGE);
            }

            successful = true;
            dispose();
        }
    }

    public boolean isSuccessful() {
        return successful;
    }
}
