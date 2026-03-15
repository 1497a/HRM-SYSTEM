package com.hrm.gui;

import com.hrm.bus.XacThucBUS;
import com.hrm.gui.admin.DepartmentPanel;
import com.hrm.gui.admin.PositionPanel;
import com.hrm.gui.admin.RoleManagementPanel;
import com.hrm.gui.admin.UserManagementPanel;
import com.hrm.gui.appointment.AppointmentListPanel;
import com.hrm.gui.attendance.AttendancePanel;
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
import java.util.List;
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
        btnOrganization = createMenuButton("Phòng ban & Chức vụ");
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
        try {
            renderContent(createPageShell(title, factory.get()));
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(
                    this,
                    "Khong mo duoc man hinh '" + title + "'.\nChi tiet: " + ex.getMessage(),
                    "Loi mo man hinh",
                    JOptionPane.ERROR_MESSAGE
            );
            JPanel errorPanel = new JPanel(new BorderLayout());
            errorPanel.setBackground(UIColors.WHITE);
            errorPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
            errorPanel.add(new JLabel("Khong tai duoc man hinh. Vui long xem log de biet chi tiet."), BorderLayout.NORTH);
            renderContent(createPageShell(title, errorPanel));
        }
    }

    private void showDashboard() {
        setActiveButton(btnDashboard);
        renderContent(new DashboardPanel(authService));
    }

    private void showSettings() {
        setActiveButton(btnSettings);
        renderContent(new SettingsPanel(authService));
    }

    private void showLeaveManagement() { showContent(btnLeave, "Quản lý nghỉ phép", LeaveListPanel::new); }
    private void showAttendance() { showContent(btnAttendance, "Chấm công & Làm thêm giờ", AttendancePanel::new); }
    private void showPerformanceEvaluation() { showContent(btnPerformance, "Đánh giá hiệu suất", EvalCycleListPanel::new); }
    private void showUserManagement() { showContent(btnUsers, "Quản lý tài khoản", UserManagementPanel::new); }
    private void showRoleManagement() { showContent(btnRoles, "Quản lý vai trò", RoleManagementPanel::new); }
    private void showEmployeeManagement() { showContent(btnEmployees, "Hồ sơ nhân viên", EmployeeListPanel::new); }
    private void showAppointmentManagement() { showContent(btnAppointments, "Bổ nhiệm", AppointmentListPanel::new); }
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
        renderContent(createPageShell("Phòng ban & Chức vụ", tabs));
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
