package com.hrm.gui.admin;

import com.hrm.bus.KetQua;
import com.hrm.bus.NhanVienBUS;
import com.hrm.bus.XacThucBUS;
import com.hrm.gui.components.BaseFormDialog;
import com.hrm.model.NhanVien;
import com.hrm.model.TaiKhoan;
import com.hrm.model.ThongTinCaNhan;
import com.hrm.model.VaiTro;
import com.hrm.util.HRMConstants;
import com.hrm.util.PermissionCodes;
import com.hrm.util.SessionContext;
import com.hrm.util.UIHelper;
import com.hrm.util.ValidationUtils;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * TaiKhoan Form Dialog - Create/Edit user accounts.
 * Khi tao moi: chon nhan vien, mat khau mac dinh = ngaySinh (ddMMyyyy).
 * Khi sua: hien thi ma NV, trang thai dung radio button (chi chon 1 trong 3).
 */
public class UserFormDialog extends BaseFormDialog {

    private final XacThucBUS authService;
    private final NhanVienBUS nhanVienService;
    private final TaiKhoan editingUser;
    private final TaiKhoan currentUser;
    private final SessionContext sessionContext;
    private boolean successful = false;
    private boolean deleted = false;
    private JComboBox<NhanVien> cboNhanVien;
    private JLabel lblDefaultPassword;
    private JTextField txtUsername;
    private JTextField txtMaNV;
    private JTextField txtFullName;
    private JTextField txtEmail;
    private JPasswordField txtPassword;
    private JPanel rolesPanel;
    private ButtonGroup roleGroup;
    private JRadioButton rdoHoatDong;
    private JRadioButton rdoBiKhoa;
    private ButtonGroup statusGroup;

    public UserFormDialog(Frame parent, TaiKhoan user) {
        super(parent, user == null ? "Tạo tài khoản mới" : "Sửa tài khoản", true);
        this.authService = XacThucBUS.getInstance();
        this.nhanVienService = NhanVienBUS.getInstance();
        this.editingUser = user;
        this.currentUser = SessionContext.getInstance().getCurrentUser();
        this.sessionContext = SessionContext.getInstance();
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
            lblDefaultPassword = new JLabel("Mật khẩu mặc định: (chưa chọn nhân viên)");
            lblDefaultPassword.setFont(new Font("Segoe UI", Font.ITALIC, 12));
            lblDefaultPassword.setForeground(new Color(100, 100, 200));
            cboNhanVien.addActionListener(e -> updateDefaultPassword());
            updateDefaultPassword();
        } else {
            txtUsername = new JTextField(20);
            txtMaNV = new JTextField(10);
            txtMaNV.setEditable(false);
            applyReadOnlyStyle(txtMaNV);
            txtFullName = new JTextField(20);
            txtFullName.setEditable(false);
            applyReadOnlyStyle(txtFullName);
            txtEmail = new JTextField(20);
            txtEmail.setEditable(false);
            applyReadOnlyStyle(txtEmail);
            rdoHoatDong = new JRadioButton("Hoạt động");
            rdoBiKhoa = new JRadioButton("Bị khóa");
            statusGroup = new ButtonGroup();
            statusGroup.add(rdoHoatDong);
            statusGroup.add(rdoBiKhoa);
        }

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
            lblDefaultPassword.setText("Mật khẩu mặc định: (chưa chọn nhân viên)");
            txtPassword.setText("");
            return;
        }
        String dob = "";
        try {
            ThongTinCaNhan ttcn = nhanVienService.getThongTinCaNhan(selectedNV.getMaNhanVien());
            if (ttcn != null && ttcn.getNgaySinh() != null) {
                dob = ttcn.getNgaySinh().format(DateTimeFormatter.ofPattern("ddMMyyyy"));
            }
        } catch (Exception ignored) {
        }
        if (!dob.isEmpty()) {
            txtPassword.setText(dob);
            lblDefaultPassword.setText("Mật khẩu mặc định: " + dob + " (ngày sinh)");
        } else {
            String fallback = selectedNV.getMaNhanVien() + "@123";
            txtPassword.setText(fallback);
            lblDefaultPassword.setText("Mật khẩu mặc định: " + fallback + " (không có ngày sinh)");
        }
    }

    private void setupLayout() {
        JPanel mainPanel = createMainPanel();
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = UIHelper.gbc(0, 0);
        int row = 0;

        if (editingUser == null) {
            gbc.gridx = 0;
            gbc.gridy = row;
            gbc.anchor = GridBagConstraints.NORTHWEST;
            formPanel.add(new JLabel("Nhân viên:"), gbc);

            gbc.gridx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1;
            cboNhanVien.setPreferredSize(new Dimension(280, 28));
            formPanel.add(cboNhanVien, gbc);
            row++;

            gbc.gridx = 1;
            gbc.gridy = row;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            formPanel.add(lblDefaultPassword, gbc);
            row++;

            gbc.gridx = 0;
            gbc.gridy = row;
            gbc.fill = GridBagConstraints.NONE;
            gbc.weightx = 0;
            gbc.anchor = GridBagConstraints.WEST;
            formPanel.add(new JLabel("Mật khẩu:"), gbc);

            gbc.gridx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            formPanel.add(txtPassword, gbc);
            row++;

            gbc.gridx = 1;
            gbc.gridy = row;
            JLabel hint = new JLabel("(Có thể thay đổi mật khẩu mặc định trước khi lưu)");
            hint.setFont(new Font("Segoe UI", Font.ITALIC, 11));
            hint.setForeground(Color.GRAY);
            formPanel.add(hint, gbc);
            row++;
        } else {
            gbc.gridx = 0;
            gbc.gridy = row;
            formPanel.add(new JLabel("Tên đăng nhập:"), gbc);

            gbc.gridx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1;
            formPanel.add(txtUsername, gbc);
            row++;

            gbc.gridx = 0;
            gbc.gridy = row;
            gbc.fill = GridBagConstraints.NONE;
            gbc.weightx = 0;
            formPanel.add(new JLabel("Mã nhân viên:"), gbc);

            gbc.gridx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            formPanel.add(txtMaNV, gbc);
            row++;

            gbc.gridx = 0;
            gbc.gridy = row;
            gbc.fill = GridBagConstraints.NONE;
            gbc.weightx = 0;
            formPanel.add(new JLabel("Họ tên:"), gbc);

            gbc.gridx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            formPanel.add(txtFullName, gbc);
            row++;

            gbc.gridx = 0;
            gbc.gridy = row;
            gbc.fill = GridBagConstraints.NONE;
            gbc.weightx = 0;
            formPanel.add(new JLabel("Email:"), gbc);

            gbc.gridx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            formPanel.add(txtEmail, gbc);
            row++;
        }

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        formPanel.add(new JLabel("Vai trò:"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JScrollPane rolesScroll = new JScrollPane(rolesPanel);
        rolesScroll.setPreferredSize(new Dimension(250, 110));
        rolesScroll.setBorder(new TitledBorder(""));
        formPanel.add(rolesScroll, gbc);
        row++;

        if (editingUser != null) {
            gbc.gridx = 0;
            gbc.gridy = row;
            gbc.fill = GridBagConstraints.NONE;
            gbc.weightx = 0;
            gbc.anchor = GridBagConstraints.WEST;
            formPanel.add(new JLabel("Trạng thái:"), gbc);

            gbc.gridx = 1;
            JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
            statusPanel.add(rdoHoatDong);
            statusPanel.add(rdoBiKhoa);
            formPanel.add(statusPanel, gbc);
        }

        JPanel buttonPanel = createButtonPanel();
        if (editingUser != null && sessionContext.hasPermission(PermissionCodes.USER_DELETE)) {
            JButton btnDelete = UIHelper.createDangerButton("Xóa");
            btnDelete.addActionListener(e -> deleteUser());
            buttonPanel.add(btnDelete);
        }

        JButton btnSave = UIHelper.createSuccessButton("Lưu");
        btnSave.addActionListener(e -> save());
        JButton btnCancel = UIHelper.createDefaultButton("Hủy");
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
                NhanVien nv = nhanVienService.getByMaNhanVien(editingUser.getMaNV());
                txtMaNV.setText(nv != null ? nv.getMaNhanVien() : editingUser.getMaNV());
            } catch (Exception e) {
                txtMaNV.setText(editingUser.getMaNV());
            }
        } else {
            txtMaNV.setText("(Chưa liên kết nhân viên)");
        }
        if (editingUser.isBiKhoa()) {
            rdoBiKhoa.setSelected(true);
        } else {
            rdoHoatDong.setSelected(true);
        }
        String currentRoleCode = editingUser.getVaiTro() != null ? editingUser.getVaiTro().getId() : null;
        for (Component comp : rolesPanel.getComponents()) {
            if (comp instanceof JRadioButton) {
                JRadioButton rb = (JRadioButton) comp;
                if (rb.getActionCommand().equals(currentRoleCode)) {
                    rb.setSelected(true);
                    break;
                }
            }
        }
        if (isEditingProtectedAdminAccount()) {
            txtUsername.setEditable(false);
            applyReadOnlyStyle(txtUsername);
            if (rdoHoatDong != null) {
                rdoHoatDong.setSelected(true);
                rdoHoatDong.setEnabled(false);
            }
            if (rdoBiKhoa != null) {
                rdoBiKhoa.setSelected(false);
                rdoBiKhoa.setEnabled(false);
            }
            for (Component comp : rolesPanel.getComponents()) {
                if (comp instanceof JRadioButton) {
                    comp.setEnabled(false);
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
            showError("Vui lòng chọn vai trò");
            return;
        }
        if (editingUser == null) {
            NhanVien selectedNV = (NhanVien) cboNhanVien.getSelectedItem();
            if (selectedNV == null) {
                showError("Vui lòng chọn nhân viên");
                cboNhanVien.requestFocus();
                return;
            }
            if (password.isEmpty()) {
                showError("Vui lòng nhập mật khẩu");
                txtPassword.requestFocus();
                return;
            }
            String username = selectedNV.getMaNhanVien();
            String email = null;
            try {
                ThongTinCaNhan ttcn = nhanVienService.getThongTinCaNhan(selectedNV.getMaNhanVien());
                if (ttcn != null) email = ttcn.getEmail();
            } catch (Exception ex) {
                // ignore
            }
            KetQua<Integer> result = authService.createUser(username, password, selectedNV.getMaNhanVien(), selectedRoleCode, email);
            if (!result.isSuccess()) {
                showError(result.getMessage());
                return;
            }
            successful = true;
            showInfo("Đã tạo tài khoản thành công!\nTên đăng nhập: " + username + "\nMật khẩu: " + password);
            dispose();
        } else {
            String username = txtUsername.getText();
            if (username == null || username.trim().isEmpty()) {
                showError("Vui lòng nhập tên đăng nhập");
                txtUsername.requestFocus();
                return;
            }
            editingUser.setTenDangNhap(username.trim());
            if (!isEditingProtectedAdminAccount()) {
                boolean biKhoa = rdoBiKhoa.isSelected();
                editingUser.setHoatDong(!biKhoa);
                editingUser.setBiKhoa(biKhoa);
                final String roleCode = selectedRoleCode;
                VaiTro selectedRole = authService.getAllRoles().stream()
                        .filter(r -> r.getId().equals(roleCode))
                        .findFirst().orElse(null);
                if (selectedRole != null) {
                    editingUser.setVaiTro(selectedRole);
                }
            }
            KetQua<Void> result = authService.updateUser(editingUser);
            if (!result.isSuccess()) {
                showError(result.getMessage());
                return;
            }
            showInfo("Đã cập nhật tài khoản thành công!");
            successful = true;
            dispose();
        }
    }

    private void deleteUser() {
        if (editingUser == null) {
            return;
        }
        if (!showYesNoWarning("Bạn có chắc muốn xóa tài khoản '" + editingUser.getTenDangNhap() + "'?", "Xác nhận xóa")) {
            return;
        }
        KetQua<Void> result = authService.deleteUser(editingUser.getId());
        if (!result.isSuccess()) {
            showError(result.getMessage());
            return;
        }
        deleted = true;
        successful = false;
        showInfo("Đã xóa tài khoản thành công!");
        dispose();
    }

    private boolean isEditingProtectedAdminAccount() {
        if (editingUser == null) return false;
        if (HRMConstants.USERNAME_ADMIN.equalsIgnoreCase(editingUser.getTenDangNhap())) return true;
        if (currentUser == null || currentUser.getId() != editingUser.getId()) return false;
        return currentUser.getVaiTro() != null
                && HRMConstants.ROLE_ADMIN.equalsIgnoreCase(currentUser.getVaiTro().getId());
    }

    public boolean isSuccessful() {
        return successful;
    }

    public boolean isDeleted() {
        return deleted;
    }

    private void applyReadOnlyStyle(JTextField field) {
        field.setBackground(new Color(245, 245, 245));
        field.setCaretColor(field.getForeground());
        field.setFocusable(false);
        field.setRequestFocusEnabled(false);
        field.setCursor(Cursor.getDefaultCursor());
    }
}
