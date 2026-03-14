package com.hrm.gui;

import com.hrm.bus.KetQua;
import com.hrm.bus.XacThucBUS;
import com.hrm.gui.admin.DepartmentPanel;
import com.hrm.gui.admin.PositionPanel;
import com.hrm.gui.admin.RoleManagementPanel;
import com.hrm.gui.admin.UserManagementPanel;
import com.hrm.gui.appointment.AppointmentListPanel;
import com.hrm.gui.attendance.AttendancePanel;
import com.hrm.gui.components.PurpleButton;
import com.hrm.gui.components.RoundedPanel;
import com.hrm.gui.contract.ContractListPanel;
import com.hrm.gui.employee.EmployeeListPanel;
import com.hrm.gui.evaluation.EvalCycleListPanel;
import com.hrm.gui.leave.LeaveListPanel;
import com.hrm.gui.notification.NotificationPanel;
import com.hrm.gui.recruitment.RecruitmentPanel;
import com.hrm.gui.report.ReportPanel;
import com.hrm.gui.salary.SalaryListPanel;
import com.hrm.model.DataScope;
import com.hrm.model.TaiKhoan;
import com.hrm.util.SessionContext;
import com.hrm.util.UIColors;
import com.hrm.util.UIFonts;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

public class MainFrame extends JFrame {
    private static final Dimension SIDEBAR_SIZE = new Dimension(240, 0);
    private static final Dimension MENU_BUTTON_SIZE = new Dimension(220, 45);

    private final XacThucBUS authService;

    private JPanel headerPanel;
    private JPanel contentPanel;
    private JLabel lblUserName;
    private JLabel lblUserRole;
    private JButton currentActiveButton;

    private JButton btnDashboard;
    private JButton btnUsers;
    private JButton btnRoles;
    private JButton btnEmployees;
    private JButton btnOrganization;
    private JButton btnAppointments;
    private JButton btnAttendance;
    private JButton btnContracts;
    private JButton btnPayroll;
    private JButton btnLeave;
    private JButton btnPerformance;
    private JButton btnNotifications;
    private JButton btnRecruitment;
    private JButton btnReports;
    private JButton btnSettings;
    private JButton btnLogout;

    public MainFrame() {
        authService = XacThucBUS.getInstance();
        if (!authService.isLoggedIn()) {
            JOptionPane.showMessageDialog(null, "Phiên làm việc không hợp lệ");
            dispose();
            new LoginFrame().setVisible(true);
            return;
        }

        initComponents();
        setupLayout();
        setupEvents();
        setupPermissions();
        setLocationRelativeTo(null);
        showDashboard();
    }

    private void initComponents() {
        setTitle("HRM System - Hệ thống quản lý nhân sự");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1200, 750);
        setMinimumSize(new Dimension(1000, 650));

        headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UIColors.PRIMARY_PURPLE);
        headerPanel.setPreferredSize(new Dimension(0, 65));

        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(UIColors.LIGHT_GRAY_BG);

        btnDashboard = createMenuButton("Trang chủ");
        btnEmployees = createMenuButton("Hồ sơ nhân viên");
        btnOrganization = createMenuButton("Cơ cấu tổ chức");
        btnAppointments = createMenuButton("Bổ nhiệm");
        btnRecruitment = createMenuButton("Tuyển dụng");
        btnAttendance = createMenuButton("Chấm công");
        btnContracts = createMenuButton("Hợp đồng lao động");
        btnPayroll = createMenuButton("Tính lương");
        btnLeave = createMenuButton("Nghỉ phép");
        btnPerformance = createMenuButton("Đánh giá hiệu suất");
        btnUsers = createMenuButton("Quản lý tài khoản");
        btnRoles = createMenuButton("Quản lý vai trò");
        btnNotifications = createMenuButton("Thông báo");
        btnReports = createMenuButton("Báo cáo");
        btnSettings = createMenuButton("Cài đặt");
        btnLogout = createMenuButton("Đăng xuất");
        btnLogout.setForeground(UIColors.DANGER_RED);
    }

    private JButton createMenuButton(String text) {
        JButton button = new JButton(text);
        button.setFont(UIFonts.TEXT_MEDIUM);
        button.setForeground(UIColors.TEXT_DARK);
        button.setBackground(UIColors.WHITE);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(MENU_BUTTON_SIZE);
        button.setMaximumSize(MENU_BUTTON_SIZE);
        button.setBorder(new EmptyBorder(10, 20, 10, 10));
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (button != currentActiveButton) {
                    button.setBackground(UIColors.LIGHT_PURPLE);
                    button.setForeground(UIColors.PRIMARY_PURPLE);
                }
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (button != currentActiveButton) {
                    resetMenuButton(button);
                }
            }
        });
        return button;
    }

    private void resetMenuButton(JButton button) {
        button.setBackground(UIColors.WHITE);
        button.setForeground(button == btnLogout ? UIColors.DANGER_RED : UIColors.TEXT_DARK);
        button.setBorder(new EmptyBorder(10, 20, 10, 10));
    }

    private void setActiveButton(JButton button) {
        if (currentActiveButton != null) {
            resetMenuButton(currentActiveButton);
        }
        currentActiveButton = button;
        button.setBackground(UIColors.LIGHT_PURPLE);
        button.setForeground(UIColors.PRIMARY_PURPLE);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, UIColors.PRIMARY_PURPLE),
                new EmptyBorder(10, 16, 10, 10)));
    }

    private void setupLayout() {
        setLayout(new BorderLayout());
        TaiKhoan user = SessionContext.getInstance().getCurrentUser();
        String displayName = user != null ? user.getHoTen() : "Khách";
        String roleName = user != null ? user.getVaiTros().toString() : "";

        headerPanel.setBorder(new EmptyBorder(0, 20, 0, 20));
        headerPanel.add(createLogoPanel(), BorderLayout.WEST);
        headerPanel.add(createHeaderUserPanel(displayName, roleName), BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);
        add(createSidebar(displayName, roleName), BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);
    }

    private JPanel createLogoPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        panel.setOpaque(false);
        JLabel label = new JLabel("HRM System");
        label.setFont(UIFonts.HEADER_H2);
        label.setForeground(UIColors.WHITE);
        panel.add(label);
        return panel;
    }

    private JPanel createHeaderUserPanel(String displayName, String roleName) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        panel.setOpaque(false);

        JLabel lblHeaderUser = new JLabel(displayName);
        lblHeaderUser.setFont(UIFonts.TEXT_MEDIUM);
        lblHeaderUser.setForeground(UIColors.WHITE);

        JLabel lblHeaderRole = new JLabel(roleName);
        lblHeaderRole.setFont(UIFonts.TEXT_SMALL);
        lblHeaderRole.setForeground(new Color(255, 255, 255, 180));

        JLabel separator = new JLabel(" | ");
        separator.setForeground(new Color(255, 255, 255, 100));

        panel.add(lblHeaderUser);
        panel.add(separator);
        panel.add(lblHeaderRole);
        panel.add(Box.createHorizontalStrut(10));
        panel.add(createHeaderLogoutButton());
        return panel;
    }

    private JButton createHeaderLogoutButton() {
        JButton button = new JButton("Đăng xuất");
        button.setFont(UIFonts.TEXT_SMALL);
        button.setForeground(UIColors.WHITE);
        button.setBackground(UIColors.DARK_PURPLE);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(8, 15, 8, 15));
        button.addActionListener(e -> logout());
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(UIColors.PURPLE_HOVER);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(UIColors.DARK_PURPLE);
            }
        });
        return button;
    }

    private JScrollPane createSidebar(String displayName, String roleName) {
        JPanel sidebarContent = new JPanel();
        sidebarContent.setLayout(new BoxLayout(sidebarContent, BoxLayout.Y_AXIS));
        sidebarContent.setBackground(UIColors.WHITE);
        sidebarContent.add(createProfilePanel(displayName, roleName));
        sidebarContent.add(Box.createVerticalStrut(15));
        sidebarContent.add(createMenuLabel());
        for (JButton button : navigationButtons()) {
            sidebarContent.add(button);
        }
        sidebarContent.add(Box.createVerticalGlue());
        sidebarContent.add(btnLogout);
        sidebarContent.add(Box.createVerticalStrut(15));

        JScrollPane scrollPane = new JScrollPane(sidebarContent);
        scrollPane.setPreferredSize(SIDEBAR_SIZE);
        scrollPane.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, UIColors.BORDER_GRAY));
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        return scrollPane;
    }

    private JPanel createProfilePanel(String displayName, String roleName) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(UIColors.LIGHT_PURPLE);
        panel.setMaximumSize(new Dimension(240, 120));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblAvatar = new JLabel(getInitials(displayName));
        lblAvatar.setFont(UIFonts.HEADER_H1);
        lblAvatar.setForeground(UIColors.WHITE);
        lblAvatar.setBackground(UIColors.PRIMARY_PURPLE);
        lblAvatar.setOpaque(true);
        lblAvatar.setPreferredSize(new Dimension(60, 60));
        lblAvatar.setMaximumSize(new Dimension(60, 60));
        lblAvatar.setHorizontalAlignment(SwingConstants.CENTER);
        lblAvatar.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblUserName = new JLabel(displayName);
        lblUserName.setFont(UIFonts.BOLD_MEDIUM);
        lblUserName.setForeground(UIColors.TEXT_DARK);
        lblUserName.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblUserRole = new JLabel(roleName);
        lblUserRole.setFont(UIFonts.TEXT_SMALL);
        lblUserRole.setForeground(UIColors.TEXT_GRAY);
        lblUserRole.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(lblAvatar);
        panel.add(Box.createVerticalStrut(10));
        panel.add(lblUserName);
        panel.add(Box.createVerticalStrut(2));
        panel.add(lblUserRole);
        return panel;
    }

    private JLabel createMenuLabel() {
        JLabel lblMenu = new JLabel("MENU");
        lblMenu.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblMenu.setForeground(UIColors.TEXT_GRAY);
        lblMenu.setBorder(new EmptyBorder(5, 20, 10, 0));
        lblMenu.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblMenu.setMaximumSize(new Dimension(240, 30));
        return lblMenu;
    }

    private List<JButton> navigationButtons() {
        return List.of(btnDashboard, btnEmployees, btnOrganization, btnAppointments, btnRecruitment,
                btnAttendance, btnContracts, btnPayroll, btnLeave, btnPerformance,
                btnUsers, btnRoles, btnNotifications, btnReports, btnSettings);
    }

    private String getInitials(String name) {
        if (name == null || name.isEmpty()) {
            return "?";
        }
        String[] parts = name.split(" ");
        return parts.length >= 2
                ? ("" + parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase()
                : String.valueOf(name.charAt(0)).toUpperCase();
    }

    private void setupEvents() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                confirmExit();
            }
        });

        bind(btnDashboard, this::showDashboard);
        bind(btnEmployees, this::showEmployeeManagement);
        bind(btnOrganization, this::showOrganization);
        bind(btnAppointments, this::showAppointmentManagement);
        bind(btnRecruitment, this::showRecruitment);
        bind(btnAttendance, this::showAttendance);
        bind(btnContracts, this::showContractManagement);
        bind(btnPayroll, this::showSalaryManagement);
        bind(btnLeave, this::showLeaveManagement);
        bind(btnPerformance, this::showPerformanceEvaluation);
        bind(btnUsers, this::showUserManagement);
        bind(btnRoles, this::showRoleManagement);
        bind(btnNotifications, this::showNotifications);
        bind(btnReports, this::showReports);
        bind(btnSettings, this::showSettings);
        bind(btnLogout, this::logout);
    }

    private void bind(AbstractButton button, Runnable action) {
        button.addActionListener(e -> action.run());
    }

    private void setupPermissions() {
        SessionContext sc = SessionContext.getInstance();
        DataScope none = DataScope.NONE;

        btnEmployees.setVisible(authService.getScopeForAction("EMPLOYEE_VIEW") != none);
        btnOrganization.setVisible(sc.coQuyen("DEPARTMENT_VIEW") || sc.coQuyen("POSITION_VIEW"));
        btnAppointments.setVisible(authService.getScopeForAction("APPOINTMENT_VIEW") != none);
        btnRecruitment.setVisible(authService.getScopeForAction("RECRUITMENT_VIEW") != none);
        btnAttendance.setVisible(authService.getScopeForAction("ATTENDANCE_VIEW") != none);
        btnContracts.setVisible(authService.getScopeForAction("CONTRACT_VIEW") != none);
        btnPayroll.setVisible(authService.getScopeForAction("PAYROLL_VIEW") != none);
        btnLeave.setVisible(authService.getScopeForAction("LEAVE_VIEW") != none);
        btnPerformance.setVisible(authService.getScopeForAction("EVAL_VIEW") != none);
        btnUsers.setVisible(sc.coQuyen("USER_VIEW"));
        btnRoles.setVisible(sc.coQuyen("ROLE_VIEW"));
        btnReports.setVisible(sc.coQuyen("REPORT_VIEW"));
        btnSettings.setVisible(sc.coQuyen("SETTINGS_VIEW"));
    }

    private JPanel createPageShell(String title, JComponent component) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(UIColors.LIGHT_GRAY_BG);
        wrapper.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel header = new JLabel(title);
        header.setFont(UIFonts.HEADER_H1);
        header.setForeground(UIColors.TEXT_DARK);
        header.setBorder(new EmptyBorder(0, 10, 15, 0));

        wrapper.add(header, BorderLayout.NORTH);
        wrapper.add(component, BorderLayout.CENTER);
        return wrapper;
    }

    private void renderContent(JComponent component) {
        contentPanel.removeAll();
        contentPanel.add(component, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void showContent(JButton button, String title, Supplier<? extends JComponent> factory) {
        setActiveButton(button);
        renderContent(createPageShell(title, factory.get()));
    }

    private JScrollPane createTransparentScroll(JComponent component) {
        JScrollPane scrollPane = new JScrollPane(component);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        return scrollPane;
    }

    private JLabel createPageTitle(String title) {
        JLabel label = new JLabel(title);
        label.setFont(UIFonts.HEADER_H1);
        label.setForeground(UIColors.TEXT_DARK);
        return label;
    }

    private void showDashboard() {
        setActiveButton(btnDashboard);
        DataScope scope = authService.getScopeForAction("EMPLOYEE_VIEW");
        JPanel dashboard = (scope == DataScope.ALL || scope == DataScope.DEPT || scope == DataScope.TEAM)
                ? buildManagerDashboard(scope)
                : buildPersonalDashboard();
        renderContent(dashboard);
    }

    private JPanel buildManagerDashboard(DataScope scope) {
        TaiKhoan currentUser = SessionContext.getInstance().getCurrentUser();
        String maNV = currentUser != null ? currentUser.getNhanVienId() : null;

        JPanel root = new JPanel(new BorderLayout(0, 20));
        root.setBackground(UIColors.LIGHT_GRAY_BG);
        root.setBorder(new EmptyBorder(25, 25, 25, 25));
        root.add(createPageTitle("Tổng quan hệ thống"), BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);

        List<JPanel> cards = new ArrayList<>();
        addManagerStatCards(cards, scope, maNV);

        if (!cards.isEmpty()) {
            int cols = Math.min(cards.size(), 3);
            int rows = (int) Math.ceil((double) cards.size() / cols);
            JPanel grid = new JPanel(new GridLayout(rows, cols, 18, 18));
            grid.setOpaque(false);
            for (JPanel card : cards) {
                grid.add(card);
            }
            body.add(grid);
            body.add(Box.createVerticalStrut(28));
        }

        root.add(createTransparentScroll(body), BorderLayout.CENTER);
        return root;
    }

    private void addManagerStatCards(List<JPanel> cards, DataScope scope, String maNV) {
        if (authService.getScopeForAction("EMPLOYEE_VIEW") != DataScope.NONE) {
            try {
                long activeEmployees = com.hrm.dao.NhanVienDAO.getInstance().findAll().stream()
                        .filter(nv -> "dang_lam_viec".equals(nv.getTrangThai())).count();
                cards.add(RoundedPanel.createStatCard("NV đang làm việc", String.valueOf(activeEmployees), UIColors.PRIMARY_PURPLE));
            } catch (Exception ignored) {
                cards.add(RoundedPanel.createStatCard("NV đang làm việc", "—", UIColors.PRIMARY_PURPLE));
            }
        }
        if (authService.getScopeForAction("LEAVE_VIEW") != DataScope.NONE) {
            try {
                long pending = com.hrm.dao.NghiPhepDAO.getInstance().findChoDuyetByScope(scope, maNV).size();
                cards.add(RoundedPanel.createStatCard("Đơn nghỉ chờ duyệt", String.valueOf(pending), UIColors.DANGER_RED));
            } catch (Exception ignored) {
                cards.add(RoundedPanel.createStatCard("Đơn nghỉ chờ duyệt", "—", UIColors.DANGER_RED));
            }
        }
        if (authService.getScopeForAction("PAYROLL_VIEW") != DataScope.NONE) {
            try {
                LocalDate today = LocalDate.now();
                com.hrm.model.BangLuong payroll = com.hrm.dao.BangLuongDAO.getInstance()
                        .findByThangNam(today.getMonthValue(), today.getYear());
                String status = payroll != null ? payroll.getTrangThai().getDisplayName() : "Chưa tạo";
                cards.add(RoundedPanel.createStatCard("Lương tháng " + today.getMonthValue(), status, UIColors.SUCCESS_GREEN));
            } catch (Exception ignored) {
                cards.add(RoundedPanel.createStatCard("Lương tháng này", "—", UIColors.SUCCESS_GREEN));
            }
        }
        if (authService.getScopeForAction("RECRUITMENT_VIEW") != DataScope.NONE) {
            try {
                long opening = com.hrm.dao.TuyenDungDAO.getInstance().findAllTin().stream()
                        .filter(t -> "dang_tuyen".equals(t.getTrangThai())).count();
                cards.add(RoundedPanel.createStatCard("Tuyển dụng đang mở", String.valueOf(opening), UIColors.WARNING_YELLOW));
            } catch (Exception ignored) {
                cards.add(RoundedPanel.createStatCard("Tuyển dụng đang mở", "—", UIColors.WARNING_YELLOW));
            }
        }
    }

    private JPanel buildPersonalDashboard() {
        TaiKhoan currentUser = SessionContext.getInstance().getCurrentUser();
        String maNV = currentUser != null ? currentUser.getNhanVienId() : null;
        int year = LocalDate.now().getYear();

        JPanel root = new JPanel(new BorderLayout(0, 20));
        root.setBackground(UIColors.LIGHT_GRAY_BG);
        root.setBorder(new EmptyBorder(25, 25, 25, 25));
        root.add(createPageTitle("Thông tin của tôi"), BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);

        JPanel cardsGrid = new JPanel(new GridLayout(1, 3, 18, 18));
        cardsGrid.setOpaque(false);
        addPersonalStatCards(cardsGrid, maNV, year);
        body.add(cardsGrid);
        body.add(Box.createVerticalStrut(28));

        root.add(createTransparentScroll(body), BorderLayout.CENTER);
        return root;
    }

    private void addPersonalStatCards(JPanel cardsGrid, String maNV, int year) {
        try {
            com.hrm.model.SoDungPhep phepNam = com.hrm.dao.NghiPhepDAO.getInstance()
                    .findByMaNVAndNamAndLoai(maNV, year, "PHEP_NAM");
            double remaining = phepNam != null ? phepNam.getRemainingDays() : 0;
            cardsGrid.add(RoundedPanel.createStatCard("Phép năm còn lại", (int) remaining + " ngày", UIColors.PRIMARY_PURPLE));
        } catch (Exception ignored) {
            cardsGrid.add(RoundedPanel.createStatCard("Phép năm còn lại", "—", UIColors.PRIMARY_PURPLE));
        }
        try {
            long pending = maNV == null ? 0 : com.hrm.dao.NghiPhepDAO.getInstance().findByMaNV(maNV).stream()
                    .filter(d -> com.hrm.model.DonXinNghiPhep.TrangThai.CHO_DUYET.equals(d.getTrangThai())).count();
            cardsGrid.add(RoundedPanel.createStatCard("Đơn đang chờ duyệt", String.valueOf(pending), UIColors.WARNING_YELLOW));
        } catch (Exception ignored) {
            cardsGrid.add(RoundedPanel.createStatCard("Đơn đang chờ duyệt", "—", UIColors.WARNING_YELLOW));
        }
        try {
            String luongText = "Chưa có";
            if (maNV != null) {
                List<com.hrm.model.BangLuong> payrolls = com.hrm.dao.BangLuongDAO.getInstance().findAll();
                for (int i = payrolls.size() - 1; i >= 0; i--) {
                    com.hrm.model.ChiTietLuong detail = com.hrm.dao.BangLuongDAO.getInstance()
                            .findByBangLuongAndNV(payrolls.get(i).getMaBL(), maNV);
                    if (detail != null) {
                        luongText = NumberFormat.getNumberInstance(new Locale("vi", "VN"))
                                .format((long) detail.getLuongThucNhan()) + " đ";
                        break;
                    }
                }
            }
            cardsGrid.add(RoundedPanel.createStatCard("Lương gần nhất", luongText, UIColors.SUCCESS_GREEN));
        } catch (Exception ignored) {
            cardsGrid.add(RoundedPanel.createStatCard("Lương gần nhất", "—", UIColors.SUCCESS_GREEN));
        }
    }

    private void showSettings() {
        setActiveButton(btnSettings);
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UIColors.LIGHT_GRAY_BG);
        panel.setBorder(new EmptyBorder(25, 25, 25, 25));
        panel.add(createPageTitle("Cài đặt tài khoản"), BorderLayout.NORTH);

        RoundedPanel card = RoundedPanel.createFlatCard();
        card.setLayout(new GridBagLayout());
        card.setBorder(new EmptyBorder(25, 25, 25, 25));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;

        JLabel title = new JLabel("Đổi mật khẩu");
        title.setFont(UIFonts.HEADER_H3);
        title.setForeground(UIColors.PRIMARY_PURPLE);
        card.add(title, gbc);

        gbc.gridwidth = 1;
        JPasswordField current = new JPasswordField(20);
        JPasswordField next = new JPasswordField(20);
        JPasswordField confirm = new JPasswordField(20);
        addPasswordRow(card, gbc, 1, "Mật khẩu hiện tại:", current);
        addPasswordRow(card, gbc, 2, "Mật khẩu mới:", next);
        addPasswordRow(card, gbc, 3, "Xác nhận mật khẩu:", confirm);

        PurpleButton changeButton = new PurpleButton("Đổi mật khẩu");
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.insets = new Insets(20, 8, 8, 8);
        card.add(changeButton, gbc);
        changeButton.addActionListener(e -> changePassword(current, next, confirm));

        JPanel center = new JPanel(new FlowLayout(FlowLayout.LEFT));
        center.setOpaque(false);
        center.setBorder(new EmptyBorder(25, 0, 0, 0));
        center.add(card);
        panel.add(center, BorderLayout.CENTER);
        renderContent(panel);
    }

    private void addPasswordRow(JPanel panel, GridBagConstraints gbc, int row, String labelText, JPasswordField field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        JLabel label = new JLabel(labelText);
        label.setFont(UIFonts.TEXT_MEDIUM);
        panel.add(label, gbc);

        field.setFont(UIFonts.TEXT_MEDIUM);
        gbc.gridx = 1;
        panel.add(field, gbc);
    }

    private void changePassword(JPasswordField current, JPasswordField next, JPasswordField confirm) {
        String currentPass = new String(current.getPassword());
        String newPass = new String(next.getPassword());
        String confirmPass = new String(confirm.getPassword());
        if (currentPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin", "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!newPass.equals(confirmPass)) {
            JOptionPane.showMessageDialog(this, "Mật khẩu xác nhận không khớp", "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        TaiKhoan currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy người dùng hiện tại", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        KetQua<Void> result = authService.changePassword(currentUser.getId(), currentPass, newPass);
        if (result.isSuccess()) {
            JOptionPane.showMessageDialog(this, "Đổi mật khẩu thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            current.setText("");
            next.setText("");
            confirm.setText("");
        } else {
            JOptionPane.showMessageDialog(this, result.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showLeaveManagement() { showContent(btnLeave, "Quản lý nghỉ phép", LeaveListPanel::new); }
    private void showAttendance() { showContent(btnAttendance, "Chấm công & Làm thêm giờ", AttendancePanel::new); }
    private void showPerformanceEvaluation() { showContent(btnPerformance, "Đánh giá hiệu suất", EvalCycleListPanel::new); }
    private void showUserManagement() { showContent(btnUsers, "Quản lý tài khoản", UserManagementPanel::new); }
    private void showRoleManagement() { showContent(btnRoles, "Quản lý vai trò", RoleManagementPanel::new); }
    private void showEmployeeManagement() { showContent(btnEmployees, "Hồ sơ nhân viên", EmployeeListPanel::new); }
    private void showAppointmentManagement() { showContent(btnAppointments, "Bổ nhiệm & Phân công", AppointmentListPanel::new); }
    private void showContractManagement() { showContent(btnContracts, "Hợp đồng lao động", ContractListPanel::new); }
    private void showSalaryManagement() { showContent(btnPayroll, "Tính lương", SalaryListPanel::new); }
    private void showNotifications() { showContent(btnNotifications, "Thông báo", NotificationPanel::new); }
    private void showRecruitment() { showContent(btnRecruitment, "Tuyển dụng", RecruitmentPanel::new); }
    private void showReports() { showContent(btnReports, "Báo cáo", ReportPanel::new); }

    private void showOrganization() {
        setActiveButton(btnOrganization);
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(UIFonts.TEXT_MEDIUM);
        tabs.setBackground(UIColors.WHITE);
        tabs.addTab("Phòng ban", new DepartmentPanel());
        tabs.addTab("Chức vụ", new PositionPanel());
        renderContent(createPageShell("Quản lý Tổ chức & Chức vụ", tabs));
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc muốn đăng xuất?",
                "Xác nhận đăng xuất", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            authService.logout();
            dispose();
            new LoginFrame().setVisible(true);
        }
    }

    private void confirmExit() {
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc muốn thoát ứng dụng?",
                "Xác nhận thoát", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            authService.logout();
            System.exit(0);
        }
    }
}
