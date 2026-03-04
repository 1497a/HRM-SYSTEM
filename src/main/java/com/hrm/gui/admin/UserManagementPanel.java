package com.hrm.gui.admin;

import com.hrm.model.TaiKhoan;
import com.hrm.bus.XacThucBUS;
import com.hrm.bus.KetQua;
import com.hrm.util.SessionContext;
import com.hrm.util.UIColors;
import com.hrm.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * TaiKhoan Management Panel - CRUD operations for users
 * Module 9: Phân quyền và bảo mật
 */
public class UserManagementPanel extends JPanel {
    private final XacThucBUS authService;
    private final SessionContext sessionContext;

    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;
    private JButton btnCreate;
    private JButton btnEdit;
    private JButton btnDelete;
    private JButton btnRefresh;

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
        setBackground(UIColors.LIGHT_GRAY_BG);

        // Search field
        txtSearch = new JTextField(20);
        txtSearch.putClientProperty("JTextField.placeholderText", "Tim kiem theo ten dang nhap...");
        txtSearch.addActionListener(e -> searchUsers());

        // Buttons
        btnCreate = UIHelper.createSuccessButton("Tao moi");
        btnCreate.addActionListener(e -> createUser());

        btnEdit = UIHelper.createPrimaryButton("Sua");
        btnEdit.addActionListener(e -> editUser());

        btnDelete = UIHelper.createDangerButton("Xoa");
        btnDelete.addActionListener(e -> deleteUser());

        btnRefresh = UIHelper.createDefaultButton("Lam moi");
        btnRefresh.addActionListener(e -> loadData());

        // Table
        String[] columns = {"ID nhan vien", "Ten dang nhap", "Ho ten", "Email", "Vai tro", "Trang thai"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setRowHeight(30);
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
                    if ("Hoat dong".equals(status)) {
                        c.setBackground(new Color(200, 255, 200));
                    } else if ("Khoa".equals(status)) {
                        c.setBackground(new Color(255, 200, 200));
                    } else {
                        c.setBackground(new Color(255, 255, 200));
                    }
                }
                setHorizontalAlignment(SwingConstants.CENTER);
                return c;
            }
        });
    }

    private void setupLayout() {
        // Top panel - search and buttons
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setOpaque(false);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        searchPanel.setOpaque(false);
        searchPanel.add(new JLabel("Tim kiem:"));
        searchPanel.add(txtSearch);
        searchPanel.add(btnRefresh);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);
        if (sessionContext.coQuyen("USER_CREATE")) {
            buttonPanel.add(btnCreate);
        }
        if (sessionContext.coQuyen("USER_UPDATE")) {
            buttonPanel.add(btnEdit);
        }
        if (sessionContext.coQuyen("USER_DELETE")) {
            buttonPanel.add(btnDelete);
        }

        topPanel.add(searchPanel, BorderLayout.WEST);
        topPanel.add(buttonPanel, BorderLayout.EAST);

        // Center - table
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(new TitledBorder("Danh sach tai khoan"));

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void loadData() {
        tableModel.setRowCount(0);
        List<TaiKhoan> users = authService.getAllUsers();

        for (TaiKhoan user : users) {
            String status = user.isBiKhoa() ? "Khoa" : (user.isHoatDong() ? "Hoat dong" : "Ngung");
            Object[] row = {
                user.getId(),
                user.getTenDangNhap(),
                user.getHoTen(),
                user.getEmail(),
                user.getVaiTros().toString(),
                status
            };
            tableModel.addRow(row);
        }
    }

    private void searchUsers() {
        String keyword = txtSearch.getText().toLowerCase().trim();
        tableModel.setRowCount(0);
        List<TaiKhoan> users = authService.getAllUsers();

        for (TaiKhoan user : users) {
            if (user.getTenDangNhap().toLowerCase().contains(keyword) ||
                user.getHoTen().toLowerCase().contains(keyword)) {
                String status = user.isBiKhoa() ? "Khoa" : (user.isHoatDong() ? "Hoat dong" : "Ngung");
                Object[] row = {
                    user.getId(),
                    user.getTenDangNhap(),
                    user.getHoTen(),
                    user.getEmail(),
                    user.getVaiTros().toString(),
                    status
                };
                tableModel.addRow(row);
            }
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
            JOptionPane.showMessageDialog(this,
                    "Vui long chon tai khoan can sua",
                    "Thong bao",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int userId = (int) tableModel.getValueAt(selectedRow, 0);
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
            JOptionPane.showMessageDialog(this,
                    "Vui long chon tai khoan can xoa",
                    "Thong bao",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int userId = (int) tableModel.getValueAt(selectedRow, 0);
        String username = (String) tableModel.getValueAt(selectedRow, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Ban co chac muon xoa tai khoan '" + username + "'?",
                "Xac nhan xoa",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            KetQua<Void> result = authService.deleteUser(userId);
            if (result.isSuccess()) {
                JOptionPane.showMessageDialog(this,
                        "Da xoa tai khoan thanh cong!",
                        "Thong bao",
                        JOptionPane.INFORMATION_MESSAGE);
                loadData();
            } else {
                JOptionPane.showMessageDialog(this,
                        result.getMessage(),
                        "Loi",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }


}
