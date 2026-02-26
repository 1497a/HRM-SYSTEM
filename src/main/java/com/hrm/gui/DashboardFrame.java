package com.hrm.gui;

import com.hrm.gui.leave.LeaveListPanel;
import com.hrm.gui.leave.LeaveCreateDialog;
import com.hrm.gui.evaluation.EvalCycleListPanel;
import com.hrm.gui.evaluation.EvalResultPanel;
import com.hrm.model.User;
import com.hrm.service.AuthService;
import com.hrm.util.SessionContext;
import com.hrm.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Dashboard Frame - Main screen after login
 * Shows user info and role-based menu
 */
public class DashboardFrame extends JFrame {
    private User currentUser;
    private JPanel contentPanel;
    private JPanel mainContentArea;
    private JLabel lblStatus;
    private CardLayout cardLayout;

    // Menu Items
    private JMenuItem mnuEmployeeList;
    private JMenuItem mnuEmployeeCreate;
    private JMenuItem mnuLeaveList;
    private JMenuItem mnuLeaveRequest;
    private JMenuItem mnuLeaveApprove;
    private JMenuItem mnuEvalList;
    private JMenuItem mnuEvalSelf;
    private JMenuItem mnuEvalReview;
    private JMenuItem mnuEvalManage;
    private JMenuItem mnuUserManage;
    private JMenuItem mnuRoleManage;
    private JMenuItem mnuReports;
    private JMenuItem mnuSettings;

    private static final String CARD_DASHBOARD = "DASHBOARD";
    private static final String CARD_LEAVE = "LEAVE";
    private static final String CARD_EVAL = "EVAL";

    public DashboardFrame() {
        this.currentUser = SessionContext.getInstance().getCurrentUser();
        initComponents();
        setupMenuBar();
        setupLayout();
        applyRolePermissions();
        centerOnScreen();
    }

    private void initComponents() {
        setTitle("HRM System - Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 750);
        setMinimumSize(new Dimension(900, 650));

        lblStatus = new JLabel("Sẵn sàng");
        lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 12));
    }

    private void setupMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // File Menu
        JMenu mnuFile = new JMenu("Hệ Thống");
        mnuFile.setMnemonic('H');

        JMenuItem mnuDashboard = new JMenuItem("Trang chủ");
        mnuDashboard.addActionListener(e -> showDashboard());

        JMenuItem mnuProfile = new JMenuItem("Thông tin cá nhân");
        mnuProfile.addActionListener(e -> showProfile());

        JMenuItem mnuChangePassword = new JMenuItem("Đổi mật khẩu");
        mnuChangePassword.addActionListener(e -> showMessage("Chức năng đổi mật khẩu"));

        JMenuItem mnuLogout = new JMenuItem("Đăng xuất");
        mnuLogout.addActionListener(e -> performLogout());

        JMenuItem mnuExit = new JMenuItem("Thoát");
        mnuExit.addActionListener(e -> System.exit(0));

        mnuFile.add(mnuDashboard);
        mnuFile.addSeparator();
        mnuFile.add(mnuProfile);
        mnuFile.add(mnuChangePassword);
        mnuFile.addSeparator();
        mnuFile.add(mnuLogout);
        mnuFile.add(mnuExit);

        // Employee Menu
        JMenu mnuEmployee = new JMenu("Nhân Viên");
        mnuEmployee.setMnemonic('N');

        mnuEmployeeList = new JMenuItem("Danh sách nhân viên");
        mnuEmployeeList.addActionListener(e -> showMessage("Danh sách nhân viên"));

        mnuEmployeeCreate = new JMenuItem("Thêm nhân viên mới");
        mnuEmployeeCreate.addActionListener(e -> showMessage("Thêm nhân viên mới"));

        mnuEmployee.add(mnuEmployeeList);
        mnuEmployee.add(mnuEmployeeCreate);

        // Leave Menu
        JMenu mnuLeave = new JMenu("Nghỉ Phép");
        mnuLeave.setMnemonic('P');

        mnuLeaveList = new JMenuItem("Quản lý nghỉ phép");
        mnuLeaveList.addActionListener(e -> showLeavePanel());

        mnuLeaveRequest = new JMenuItem("Tạo đơn nghỉ phép");
        mnuLeaveRequest.addActionListener(e -> createLeaveRequest());

        mnuLeaveApprove = new JMenuItem("Duyệt đơn (cho Quản lý)");
        mnuLeaveApprove.addActionListener(e -> showLeavePanel());

        mnuLeave.add(mnuLeaveList);
        mnuLeave.add(mnuLeaveRequest);
        mnuLeave.addSeparator();
        mnuLeave.add(mnuLeaveApprove);

        // Evaluation Menu
        JMenu mnuEvaluation = new JMenu("Đánh Giá");
        mnuEvaluation.setMnemonic('D');

        mnuEvalList = new JMenuItem("Quản lý đánh giá");
        mnuEvalList.addActionListener(e -> showEvaluationPanel());

        mnuEvalSelf = new JMenuItem("Kết quả của tôi");
        mnuEvalSelf.addActionListener(e -> showEvalResults());

        mnuEvalReview = new JMenuItem("Đánh giá nhân viên");
        mnuEvalReview.addActionListener(e -> showEvaluationPanel());

        mnuEvalManage = new JMenuItem("Cấu hình kỳ đánh giá");
        mnuEvalManage.addActionListener(e -> showEvaluationPanel());

        mnuEvaluation.add(mnuEvalList);
        mnuEvaluation.add(mnuEvalSelf);
        mnuEvaluation.addSeparator();
        mnuEvaluation.add(mnuEvalReview);
        mnuEvaluation.add(mnuEvalManage);

        // Admin Menu
        JMenu mnuAdmin = new JMenu("Quản Trị");
        mnuAdmin.setMnemonic('Q');

        mnuUserManage = new JMenuItem("Quản lý tài khoản");
        mnuUserManage.addActionListener(e -> showMessage("Quản lý tài khoản"));

        mnuRoleManage = new JMenuItem("Quản lý vai trò");
        mnuRoleManage.addActionListener(e -> showMessage("Quản lý vai trò"));

        mnuReports = new JMenuItem("Báo cáo");
        mnuReports.addActionListener(e -> showMessage("Báo cáo"));

        mnuSettings = new JMenuItem("Cài đặt hệ thống");
        mnuSettings.addActionListener(e -> showMessage("Cài đặt hệ thống"));

        mnuAdmin.add(mnuUserManage);
        mnuAdmin.add(mnuRoleManage);
        mnuAdmin.addSeparator();
        mnuAdmin.add(mnuReports);
        mnuAdmin.add(mnuSettings);

        // Help Menu
        JMenu mnuHelp = new JMenu("Trợ Giúp");
        mnuHelp.setMnemonic('T');

        JMenuItem mnuAbout = new JMenuItem("Giới thiệu");
        mnuAbout.addActionListener(e -> showAbout());

        mnuHelp.add(mnuAbout);

        // Add menus to bar
        menuBar.add(mnuFile);
        menuBar.add(mnuEmployee);
        menuBar.add(mnuLeave);
        menuBar.add(mnuEvaluation);
        menuBar.add(mnuAdmin);
        menuBar.add(mnuHelp);

        setJMenuBar(menuBar);
    }

    private void setupLayout() {
        JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.setBackground(new Color(245, 245, 245));

        // Header Panel - User Info
        JPanel headerPanel = createHeaderPanel();

        // Main Content Area with CardLayout
        cardLayout = new CardLayout();
        mainContentArea = new JPanel(cardLayout);
        mainContentArea.setBackground(new Color(245, 245, 245));

        // Dashboard Card
        JPanel dashboardPanel = createDashboardPanel();
        mainContentArea.add(dashboardPanel, CARD_DASHBOARD);

        // Leave Panel Card
        LeaveListPanel leavePanel = new LeaveListPanel();
        mainContentArea.add(leavePanel, CARD_LEAVE);

        // Evaluation Panel Card
        EvalCycleListPanel evalPanel = new EvalCycleListPanel();
        mainContentArea.add(evalPanel, CARD_EVAL);

        // Status Panel
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY),
            new EmptyBorder(5, 10, 5, 10)));
        statusPanel.setBackground(new Color(240, 240, 240));
        statusPanel.add(lblStatus, BorderLayout.WEST);

        JLabel lblTime = new JLabel(java.time.LocalDateTime.now().format(
            java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        lblTime.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusPanel.add(lblTime, BorderLayout.EAST);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(mainContentArea, BorderLayout.CENTER);
        mainPanel.add(statusPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.setOpaque(false);

        // Content Panel - Dashboard Cards
        contentPanel = new JPanel(new GridLayout(2, 3, 15, 15));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(new EmptyBorder(20, 10, 20, 10));

        // Quick Access Cards
        contentPanel.add(createQuickCard("Thông Tin Cá Nhân", "Xem và cập nhật thông tin",
            new Color(52, 152, 219), this::showProfile));
        contentPanel.add(createQuickCard("Nghỉ Phép", "Quản lý đơn nghỉ phép",
            new Color(46, 204, 113), this::showLeavePanel));
        contentPanel.add(createQuickCard("Đánh Giá", "Xem và quản lý đánh giá",
            new Color(155, 89, 182), this::showEvaluationPanel));
        contentPanel.add(createQuickCard("Tạo Đơn Phép", "Tạo đơn xin nghỉ phép mới",
            new Color(241, 196, 15), this::createLeaveRequest));
        contentPanel.add(createQuickCard("Kết Quả Đánh Giá", "Xem kết quả đánh giá của tôi",
            new Color(230, 126, 34), this::showEvalResults));
        contentPanel.add(createQuickCard("Trở Về Trang Chủ", "Quay lại màn hình chính",
            new Color(149, 165, 166), this::showDashboard));

        // Wrap content in scroll pane
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setOpaque(false);

        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout(15, 0));
        headerPanel.setBackground(new Color(0, 102, 153));
        headerPanel.setBorder(new EmptyBorder(15, 20, 15, 20));

        // Welcome Message
        JPanel welcomePanel = new JPanel(new GridLayout(2, 1));
        welcomePanel.setOpaque(false);

        JLabel lblWelcome = new JLabel("Xin chào, " + currentUser.getFullName());
        lblWelcome.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblWelcome.setForeground(Color.WHITE);

        JLabel lblRole = new JLabel("Vai trò: " + currentUser.getRoleNames());
        lblRole.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblRole.setForeground(new Color(200, 220, 240));

        welcomePanel.add(lblWelcome);
        welcomePanel.add(lblRole);

        // User Avatar/Icon
        JLabel lblAvatar = new JLabel();
        lblAvatar.setPreferredSize(new Dimension(60, 60));
        lblAvatar.setOpaque(true);
        lblAvatar.setBackground(new Color(0, 80, 120));
        lblAvatar.setHorizontalAlignment(SwingConstants.CENTER);
        lblAvatar.setText(getInitials(currentUser.getFullName()));
        lblAvatar.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblAvatar.setForeground(Color.WHITE);

        // Navigation buttons
        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        navPanel.setOpaque(false);

        JButton btnHome = UIHelper.createNavButton("Trang chủ");
        btnHome.addActionListener(e -> showDashboard());

        JButton btnLeave = UIHelper.createNavButton("Nghỉ phép");
        btnLeave.addActionListener(e -> showLeavePanel());

        JButton btnEval = UIHelper.createNavButton("Đánh giá");
        btnEval.addActionListener(e -> showEvaluationPanel());

        JButton btnLogout = UIHelper.createStyledButton("Đăng Xuất", UIHelper.DANGER_COLOR, Color.WHITE);
        btnLogout.addActionListener(e -> performLogout());

        navPanel.add(btnHome);
        navPanel.add(btnLeave);
        navPanel.add(btnEval);
        navPanel.add(Box.createHorizontalStrut(20));
        navPanel.add(btnLogout);

        headerPanel.add(lblAvatar, BorderLayout.WEST);
        headerPanel.add(welcomePanel, BorderLayout.CENTER);
        headerPanel.add(navPanel, BorderLayout.EAST);

        return headerPanel;
    }

    private JPanel createQuickCard(String title, String description, Color color, Runnable action) {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220)),
            new EmptyBorder(20, 20, 20, 20)
        ));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Color stripe at top
        JPanel stripe = new JPanel();
        stripe.setBackground(color);
        stripe.setPreferredSize(new Dimension(0, 5));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(new Color(50, 50, 50));

        JLabel lblDesc = new JLabel("<html>" + description + "</html>");
        lblDesc.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblDesc.setForeground(Color.GRAY);

        JPanel textPanel = new JPanel(new BorderLayout(5, 5));
        textPanel.setOpaque(false);
        textPanel.add(lblTitle, BorderLayout.NORTH);
        textPanel.add(lblDesc, BorderLayout.CENTER);

        card.add(stripe, BorderLayout.NORTH);
        card.add(textPanel, BorderLayout.CENTER);

        // Click handler
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                action.run();
            }

            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                card.setBackground(new Color(248, 248, 248));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                card.setBackground(Color.WHITE);
            }
        });

        return card;
    }

    private void applyRolePermissions() {
        AuthService auth = AuthService.getInstance();

        // Employee Menu
        mnuEmployeeList.setEnabled(auth.hasPermission("EMPLOYEE_VIEW") || auth.hasPermission("VIEW_SELF"));
        mnuEmployeeCreate.setEnabled(auth.hasPermission("EMPLOYEE_CREATE"));

        // Leave Menu - all users can view their own
        mnuLeaveList.setEnabled(true);
        mnuLeaveRequest.setEnabled(auth.hasPermission("LEAVE_CREATE"));
        mnuLeaveApprove.setEnabled(auth.hasPermission("LEAVE_APPROVE"));

        // Evaluation Menu
        mnuEvalList.setEnabled(true);
        mnuEvalSelf.setEnabled(auth.hasPermission("EVAL_VIEW_SELF"));
        mnuEvalReview.setEnabled(auth.hasPermission("EVAL_REVIEW"));
        mnuEvalManage.setEnabled(auth.hasPermission("EVAL_MANAGE"));

        // Admin Menu
        mnuUserManage.setEnabled(auth.hasPermission("USER_VIEW"));
        mnuRoleManage.setEnabled(auth.hasPermission("ROLE_VIEW"));
        mnuReports.setEnabled(auth.hasPermission("REPORT_VIEW"));
        mnuSettings.setEnabled(auth.hasPermission("SETTINGS_VIEW"));

        updateStatus();
    }

    private void updateStatus() {
        String permissions = currentUser.hasRole("ADMIN") ? "Toàn quyền" :
            "Quyền hạn theo vai trò";
        lblStatus.setText("Vai trò: " + currentUser.getRoleNames() + " | " + permissions);
    }

    private String getInitials(String fullName) {
        if (fullName == null || fullName.isEmpty()) return "?";
        String[] parts = fullName.split(" ");
        if (parts.length >= 2) {
            return ("" + parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase();
        }
        return ("" + fullName.charAt(0)).toUpperCase();
    }

    // Navigation methods
    private void showDashboard() {
        cardLayout.show(mainContentArea, CARD_DASHBOARD);
        lblStatus.setText("Trang chủ | Vai trò: " + currentUser.getRoleNames());
    }

    private void showLeavePanel() {
        // Refresh the leave panel
        mainContentArea.remove(mainContentArea.getComponent(1));
        LeaveListPanel newLeavePanel = new LeaveListPanel();
        mainContentArea.add(newLeavePanel, CARD_LEAVE, 1);
        cardLayout.show(mainContentArea, CARD_LEAVE);
        lblStatus.setText("Quản lý nghỉ phép | Vai trò: " + currentUser.getRoleNames());
    }

    private void showEvaluationPanel() {
        // Refresh the eval panel
        mainContentArea.remove(mainContentArea.getComponent(2));
        EvalCycleListPanel newEvalPanel = new EvalCycleListPanel();
        mainContentArea.add(newEvalPanel, CARD_EVAL, 2);
        cardLayout.show(mainContentArea, CARD_EVAL);
        lblStatus.setText("Quản lý đánh giá | Vai trò: " + currentUser.getRoleNames());
    }

    private void createLeaveRequest() {
        LeaveCreateDialog dialog = new LeaveCreateDialog(this);
        dialog.setVisible(true);
        if (dialog.isSuccessful()) {
            showLeavePanel();
        }
    }

    private void showEvalResults() {
        EvalResultPanel resultPanel = new EvalResultPanel();
        JDialog dialog = new JDialog(this, "Kết quả đánh giá của tôi", true);
        dialog.setContentPane(resultPanel);
        dialog.setSize(800, 600);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void showProfile() {
        StringBuilder info = new StringBuilder();
        info.append("THÔNG TIN TÀI KHOẢN\n");
        info.append("==================\n\n");
        info.append("Họ tên: ").append(currentUser.getFullName()).append("\n");
        info.append("Tên đăng nhập: ").append(currentUser.getUsername()).append("\n");
        info.append("Email: ").append(currentUser.getEmail()).append("\n");
        info.append("Vai trò: ").append(currentUser.getRoleNames()).append("\n\n");
        info.append("QUYỀN HẠN:\n");
        info.append("---------\n");

        currentUser.getRoles().forEach(role -> {
            role.getPermissions().forEach(p -> {
                info.append("- ").append(p.getName()).append("\n");
            });
        });

        JTextArea textArea = new JTextArea(info.toString());
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(400, 400));

        JOptionPane.showMessageDialog(this, scrollPane, "Thông Tin Cá Nhân",
            JOptionPane.INFORMATION_MESSAGE);
    }

    private void showAbout() {
        JOptionPane.showMessageDialog(this,
            "HRM SYSTEM\n" +
            "Hệ Thống Quản Lý Nhân Sự\n\n" +
            "Phiên bản: 1.0\n" +
            "Module: Leave + Evaluation\n" +
            "Java Swing Desktop Application\n\n" +
            "© 2024 HRM System",
            "Giới Thiệu",
            JOptionPane.INFORMATION_MESSAGE);
    }

    private void showMessage(String feature) {
        JOptionPane.showMessageDialog(this,
            "Chức năng: " + feature + "\n\nĐang phát triển...",
            "Thông Báo",
            JOptionPane.INFORMATION_MESSAGE);
    }

    private void performLogout() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Bạn có chắc muốn đăng xuất?",
            "Xác Nhận Đăng Xuất",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            AuthService.getInstance().logout();
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
            this.dispose();
        }
    }

    private void centerOnScreen() {
        setLocationRelativeTo(null);
    }
}
