package com.hrm.gui.admin;

import com.hrm.model.NhanVien;
import com.hrm.model.ThongTinCaNhan;
import com.hrm.model.VaiTro;
import com.hrm.model.TaiKhoan;
import com.hrm.bus.NhanVienBUS;
import com.hrm.bus.XacThucBUS;
import com.hrm.bus.KetQua;
import com.hrm.util.UIHelper;
import com.hrm.util.ValidationUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * TaiKhoan Form Dialog - Create/Edit user accounts.
 * Khi tao moi: chon nhan vien, mat khau mac dinh = ngaySinh (ddMMyyyy).
 * Khi sua: hien thi ma NV, trang thai dung radio button (chi chon 1 trong 3).
 */
public class UserFormDialog extends JDialog {
    private final XacThucBUS authService;
    private final NhanVienBUS nhanVienService;
    private final TaiKhoan editingUser;
    private boolean successful = false;

    // Create mode
    private JComboBox<NhanVien> cboNhanVien;
    private JLabel lblDefaultPassword;

    // Edit mode
    private JTextField txtUsername;
    private JTextField txtMaNV;
    private JTextField txtFullName;
    private JTextField txtEmail;

    private JPasswordField txtPassword;
    private JPanel rolesPanel;
    private ButtonGroup roleGroup;

    // Status (edit mode) - radio buttons, mutually exclusive
    private JRadioButton rdoHoatDong;
    private JRadioButton rdoBiKhoa;
    private ButtonGroup statusGroup;

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

        setSize(500, user == null ? 560 : 540);
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        txtPassword = new JPasswordField(20);

        if (editingUser == null) {
            // CREATE mode
            List<NhanVien> dsNV = nhanVienService.getDangLamViec();
            cboNhanVien = new JComboBox<>();
            for (NhanVien nv : dsNV) {
                cboNhanVien.addItem(nv);
            }
            cboNhanVien.setRenderer((list, value, index, isSelected, cellHasFocus) -> {
                JLabel label = new JLabel();
                if (value != null) {
                    label.setText(value.getHoTen() + " (" + value.getMaNhanVien() + ")");
                }
                if (isSelected) {
                    label.setBackground(list.getSelectionBackground());
                    label.setForeground(list.getSelectionForeground());
                    label.setOpaque(true);
                }
                return label;
            });

            // When employee changes, auto-fill default password from DOB
            lblDefaultPassword = new JLabel("Mat khau mac dinh: (chua chon nhan vien)");
            lblDefaultPassword.setFont(new Font("Segoe UI", Font.ITALIC, 12));
            lblDefaultPassword.setForeground(new Color(100, 100, 200));

            cboNhanVien.addActionListener(e -> updateDefaultPassword());
            updateDefaultPassword();

        } else {
            // EDIT mode
            txtUsername = new JTextField(20);
            txtUsername.setEditable(false);
            txtMaNV = new JTextField(10);
            txtMaNV.setEditable(false);
            txtMaNV.setBackground(new Color(245, 245, 245));
            txtFullName = new JTextField(20);
            txtEmail = new JTextField(20);

            // Status: radio buttons (mutually exclusive)
            rdoHoatDong = new JRadioButton("Hoat dong");
            rdoBiKhoa = new JRadioButton("Bi khoa");
            statusGroup = new ButtonGroup();
            statusGroup.add(rdoHoatDong);
            statusGroup.add(rdoBiKhoa);
        }

        // Roles panel
        rolesPanel = new JPanel();
        rolesPanel.setLayout(new BoxLayout(rolesPanel, BoxLayout.Y_AXIS));
        roleGroup = new ButtonGroup();

        List<VaiTro> roles = authService.getAllRoles();
        for (VaiTro role : roles) {
            JRadioButton rb = new JRadioButton(role.getTenVaiTro() + " (" + role.getId() + ")");
            rb.setActionCommand(role.getId());
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

    private void updateDefaultPassword() {
        if (cboNhanVien == null || lblDefaultPassword == null) return;
        NhanVien selectedNV = (NhanVien) cboNhanVien.getSelectedItem();
        if (selectedNV == null) {
            lblDefaultPassword.setText("Mat khau mac dinh: (chua chon nhan vien)");
            txtPassword.setText("");
            return;
        }

        String dob = "";
        try {
            ThongTinCaNhan ttcn = nhanVienService.getThongTinCaNhan(selectedNV.getId());
            if (ttcn != null && ttcn.getNgaySinh() != null) {
                dob = ttcn.getNgaySinh().format(DateTimeFormatter.ofPattern("ddMMyyyy"));
            }
        } catch (Exception ignored) {}

        if (!dob.isEmpty()) {
            txtPassword.setText(dob);
            lblDefaultPassword.setText("Mat khau mac dinh: " + dob + " (ngay sinh)");
        } else {
            String fallback = selectedNV.getMaNhanVien() + "@123";
            txtPassword.setText(fallback);
            lblDefaultPassword.setText("Mat khau mac dinh: " + fallback + " (khong co ngay sinh)");
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
            gbc.gridx = 0; gbc.gridy = row; gbc.anchor = GridBagConstraints.NORTHWEST;
            formPanel.add(new JLabel("Nhan vien:"), gbc);
            gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1;
            cboNhanVien.setPreferredSize(new Dimension(280, 28));
            formPanel.add(cboNhanVien, gbc);
            row++;

            gbc.gridx = 1; gbc.gridy = row; gbc.fill = GridBagConstraints.HORIZONTAL;
            formPanel.add(lblDefaultPassword, gbc);
            row++;

            gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
            gbc.anchor = GridBagConstraints.WEST;
            formPanel.add(new JLabel("Mat khau:"), gbc);
            gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
            formPanel.add(txtPassword, gbc);
            row++;
            gbc.gridx = 1; gbc.gridy = row;
            JLabel hint = new JLabel("(Co the thay doi mat khau mac dinh truoc khi luu)");
            hint.setFont(new Font("Segoe UI", Font.ITALIC, 11));
            hint.setForeground(Color.GRAY);
            formPanel.add(hint, gbc);
            row++;

        } else {
            gbc.gridx = 0; gbc.gridy = row;
            formPanel.add(new JLabel("Ten dang nhap:"), gbc);
            gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1;
            formPanel.add(txtUsername, gbc);
            row++;

            gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
            formPanel.add(new JLabel("Ma nhan vien:"), gbc);
            gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
            formPanel.add(txtMaNV, gbc);
            row++;

            gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
            formPanel.add(new JLabel("Mat khau moi:"), gbc);
            gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
            formPanel.add(txtPassword, gbc);
            row++;
            gbc.gridx = 1; gbc.gridy = row;
            JLabel pwHint = new JLabel("(De trong neu khong muon doi mat khau)");
            pwHint.setFont(new Font("Segoe UI", Font.ITALIC, 11));
            pwHint.setForeground(Color.GRAY);
            formPanel.add(pwHint, gbc);
            row++;

            gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
            formPanel.add(new JLabel("Ho ten:"), gbc);
            gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
            formPanel.add(txtFullName, gbc);
            row++;

            gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
            formPanel.add(new JLabel("Email:"), gbc);
            gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
            formPanel.add(txtEmail, gbc);
            row++;
        }

        // Roles
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        formPanel.add(new JLabel("Vai tro:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        JScrollPane rolesScroll = new JScrollPane(rolesPanel);
        rolesScroll.setPreferredSize(new Dimension(250, 110));
        rolesScroll.setBorder(new TitledBorder(""));
        formPanel.add(rolesScroll, gbc);
        row++;

        // Status (edit mode only)
        if (editingUser != null) {
            gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
            gbc.anchor = GridBagConstraints.WEST;
            formPanel.add(new JLabel("Trang thai:"), gbc);
            gbc.gridx = 1;
            JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
            statusPanel.add(rdoHoatDong);
            statusPanel.add(rdoBiKhoa);
            formPanel.add(statusPanel, gbc);
        }

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
        txtUsername.setText(editingUser.getTenDangNhap());
        txtFullName.setText(editingUser.getHoTen() != null ? editingUser.getHoTen() : "");
        txtEmail.setText(editingUser.getEmail() != null ? editingUser.getEmail() : "");

        if (editingUser.getMaNV() != null) {
            try {
                NhanVien nv = nhanVienService.getById(editingUser.getMaNV());
                txtMaNV.setText(nv != null ? nv.getMaNhanVien() + " (ID: " + editingUser.getMaNV() + ")" : String.valueOf(editingUser.getMaNV()));
            } catch (Exception e) {
                txtMaNV.setText(String.valueOf(editingUser.getMaNV()));
            }
        } else {
            txtMaNV.setText("(Chua lien ket nhan vien)");
        }

        if (editingUser.isBiKhoa()) {
            rdoBiKhoa.setSelected(true);
        } else {
            rdoHoatDong.setSelected(true);
        }

        String currentRoleCode = null;
        if (editingUser.getVaiTros() != null && !editingUser.getVaiTros().isEmpty()) {
            currentRoleCode = editingUser.getVaiTros().get(0).getId();
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
        if (selectedModel != null) selectedRoleCode = selectedModel.getActionCommand();
        if (selectedRoleCode == null || selectedRoleCode.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui long chon vai tro", "Loi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (editingUser == null) {
            NhanVien selectedNV = (NhanVien) cboNhanVien.getSelectedItem();
            if (selectedNV == null) {
                JOptionPane.showMessageDialog(this, "Vui long chon nhan vien", "Loi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui long nhap mat khau", "Loi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String username = selectedNV.getMaNhanVien();
            String email = null;
            try {
                ThongTinCaNhan ttcn = nhanVienService.getThongTinCaNhan(selectedNV.getId());
                if (ttcn != null) email = ttcn.getEmail();
            } catch (Exception ex) { /* ignore */ }

            KetQua<Integer> result = authService.createUser(username, password, selectedNV.getId(), selectedRoleCode, email);
            if (!result.isSuccess()) {
                JOptionPane.showMessageDialog(this, result.getMessage(), "Loi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            successful = true;
            JOptionPane.showMessageDialog(this,
                    "Da tao tai khoan thanh cong!\nTen dang nhap: " + username + "\nMat khau: " + password,
                    "Thong bao", JOptionPane.INFORMATION_MESSAGE);
            dispose();

        } else {
            String fullName = txtFullName.getText().trim();
            String email = txtEmail.getText().trim();
            if (fullName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui long nhap ho ten", "Loi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            String emailErr = ValidationUtils.validateEmail(email);
            if (emailErr != null) {
                JOptionPane.showMessageDialog(this, emailErr, "Loi", JOptionPane.WARNING_MESSAGE);
                return;
            }

            editingUser.setHoTen(fullName);
            editingUser.setEmail(email);
            boolean biKhoa = rdoBiKhoa.isSelected();
            editingUser.setHoatDong(!biKhoa);
            editingUser.setBiKhoa(biKhoa);

            final String roleCode = selectedRoleCode;
            VaiTro selectedRole = authService.getAllRoles().stream()
                    .filter(r -> r.getId().equals(roleCode))
                    .findFirst().orElse(null);
            if (selectedRole != null) {
                editingUser.getVaiTros().clear();
                editingUser.getVaiTros().add(selectedRole);
            }

            KetQua<Void> result = authService.updateUser(editingUser);
            if (!result.isSuccess()) {
                JOptionPane.showMessageDialog(this, result.getMessage(), "Loi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!password.isEmpty()) {
                authService.resetPassword(editingUser.getId(), password);
            }

            JOptionPane.showMessageDialog(this, "Da cap nhat tai khoan thanh cong!", "Thong bao", JOptionPane.INFORMATION_MESSAGE);
            successful = true;
            dispose();
        }
    }

    public boolean isSuccessful() { return successful; }
}
