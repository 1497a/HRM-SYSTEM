package com.hrm.gui;

import com.hrm.bus.XacThucBUS;
import com.hrm.gui.evaluation.EvalCycleListPanel;
import com.hrm.gui.evaluation.EvalResultPanel;
import com.hrm.gui.leave.LeaveCreateDialog;
import com.hrm.gui.leave.LeaveListPanel;
import com.hrm.model.DataScope;
import com.hrm.model.TaiKhoan;
import com.hrm.util.SessionContext;
import com.hrm.util.UIColors;
import com.hrm.util.UIFonts;
import com.hrm.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Dashboard Frame - Main screen after login.
 */
public class DashboardFrame extends JFrame {
    private static final String CARD_DASHBOARD = "DASHBOARD";
    private static final String CARD_LEAVE = "LEAVE";
    private static final String CARD_EVAL = "EVAL";

    private final TaiKhoan currentUser;
    private JPanel mainContentArea;
    private JLabel lblStatus;
    private CardLayout cardLayout;

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

    public DashboardFrame() {
        this.currentUser = SessionContext.getInstance().getCurrentUser();
        initComponents();
        setJMenuBar(createMenuBar());
        setContentPane(createMainPanel());
        applyRolePermissions();
        centerOnScreen();
    }

    private void initComponents() {
        setTitle("HRM System - Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 750);
        setMinimumSize(new Dimension(900, 650));

        lblStatus = new JLabel("Sẵn sàng");
        lblStatus.setFont(UIFonts.TEXT_SMALL);
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(createSystemMenu());
        menuBar.add(createEmployeeMenu());
        menuBar.add(createLeaveMenu());
        menuBar.add(createEvaluationMenu());
        menuBar.add(createAdminMenu());
        menuBar.add(createHelpMenu());
        return menuBar;
    }

    private JMenu createSystemMenu() {
        JMenu menu = createMenu("Hệ thống", 'H');
        menu.add(createMenuItem("Trang chủ", e -> showDashboard()));
        menu.addSeparator();
        menu.add(createMenuItem("Thông tin cá nhân", e -> showProfile()));
        menu.add(createMenuItem("Đổi mật khẩu", e -> showMessage("Đổi mật khẩu")));
        menu.addSeparator();
        menu.add(createMenuItem("Đăng xuất", e -> performLogout()));
        menu.add(createMenuItem("Thoát", e -> System.exit(0)));
        return menu;
    }

    private JMenu createEmployeeMenu() {
        JMenu menu = createMenu("Nhân viên", 'N');
        mnuEmployeeList = createMenuItem("Danh sách nhân viên", e -> showMessage("Danh sách nhân viên"));
        mnuEmployeeCreate = createMenuItem("Thêm nhân viên mới", e -> showMessage("Thêm nhân viên mới"));
        menu.add(mnuEmployeeList);
        menu.add(mnuEmployeeCreate);
        return menu;
    }

    private JMenu createLeaveMenu() {
        JMenu menu = createMenu("Nghỉ phép", 'P');
        mnuLeaveList = createMenuItem("Quản lý nghỉ phép", e -> showLeavePanel());
        mnuLeaveRequest = createMenuItem("Tạo đơn nghỉ phép", e -> createLeaveRequest());
        mnuLeaveApprove = createMenuItem("Duyệt đơn (cho quản lý)", e -> showLeavePanel());
        menu.add(mnuLeaveList);
        menu.add(mnuLeaveRequest);
        menu.addSeparator();
        menu.add(mnuLeaveApprove);
        return menu;
    }

    private JMenu createEvaluationMenu() {
        JMenu menu = createMenu("Đánh giá", 'Đ');
        mnuEvalList = createMenuItem("Quản lý đánh giá", e -> showEvaluationPanel());
        mnuEvalSelf = createMenuItem("Kết quả của tôi", e -> showEvalResults());
        mnuEvalReview = createMenuItem("Đánh giá nhân viên", e -> showEvaluationPanel());
        mnuEvalManage = createMenuItem("Cấu hình kỳ đánh giá", e -> showEvaluationPanel());
        menu.add(mnuEvalList);
        menu.add(mnuEvalSelf);
        menu.addSeparator();
        menu.add(mnuEvalReview);
        menu.add(mnuEvalManage);
        return menu;
    }

    private JMenu createAdminMenu() {
        JMenu menu = createMenu("Quản trị", 'Q');
        mnuUserManage = createMenuItem("Quản lý tài khoản", e -> showMessage("Quản lý tài khoản"));
        mnuRoleManage = createMenuItem("Quản lý vai trò", e -> showMessage("Quản lý vai trò"));
        mnuReports = createMenuItem("Báo cáo", e -> showMessage("Báo cáo"));
        mnuSettings = createMenuItem("Cài đặt hệ thống", e -> showMessage("Cài đặt hệ thống"));
        menu.add(mnuUserManage);
        menu.add(mnuRoleManage);
        menu.addSeparator();
        menu.add(mnuReports);
        menu.add(mnuSettings);
        return menu;
    }

    private JMenu createHelpMenu() {
        JMenu menu = createMenu("Trợ giúp", 'T');
        menu.add(createMenuItem("Giới thiệu", e -> showAbout()));
        return menu;
    }

    private JMenu createMenu(String text, char mnemonic) {
        JMenu menu = new JMenu(text);
        menu.setMnemonic(mnemonic);
        return menu;
    }

    private JMenuItem createMenuItem(String text, java.awt.event.ActionListener action) {
        JMenuItem item = new JMenuItem(text);
        item.addActionListener(action);
        return item;
    }

    private JPanel createMainPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(UIColors.LIGHT_GRAY_BG);
        mainPanel.add(createHeaderPanel(), BorderLayout.NORTH);
        mainPanel.add(createContentArea(), BorderLayout.CENTER);
        mainPanel.add(createStatusPanel(), BorderLayout.SOUTH);
        return mainPanel;
    }

    private JPanel createContentArea() {
        cardLayout = new CardLayout();
        mainContentArea = new JPanel(cardLayout);
        mainContentArea.setBackground(UIColors.LIGHT_GRAY_BG);
        mainContentArea.add(createDashboardPanel(), CARD_DASHBOARD);
        mainContentArea.add(new LeaveListPanel(), CARD_LEAVE);
        mainContentArea.add(new EvalCycleListPanel(), CARD_EVAL);
        return mainContentArea;
    }

    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.setOpaque(false);

        JPanel contentPanel = new JPanel(new GridLayout(2, 3, 15, 15));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(new EmptyBorder(20, 10, 20, 10));

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

        JPanel welcomePanel = new JPanel(new GridLayout(2, 1));
        welcomePanel.setOpaque(false);

        JLabel lblWelcome = new JLabel("Xin chào, " + currentUser.getHoTen());
        lblWelcome.setFont(UIFonts.HEADER_H2);
        lblWelcome.setForeground(UIColors.WHITE);

        JLabel lblRole = new JLabel("Vai trò: " + currentUser.getVaiTros());
        lblRole.setFont(UIFonts.TEXT_MEDIUM);
        lblRole.setForeground(new Color(200, 220, 240));

        welcomePanel.add(lblWelcome);
        welcomePanel.add(lblRole);

        JLabel lblAvatar = new JLabel(getInitials(currentUser.getHoTen()));
        lblAvatar.setPreferredSize(new Dimension(60, 60));
        lblAvatar.setOpaque(true);
        lblAvatar.setBackground(new Color(0, 80, 120));
        lblAvatar.setHorizontalAlignment(SwingConstants.CENTER);
        lblAvatar.setFont(UIFonts.HEADER_H1);
        lblAvatar.setForeground(UIColors.WHITE);

        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        navPanel.setOpaque(false);
        navPanel.add(createNavButton("Trang chủ", e -> showDashboard()));
        navPanel.add(createNavButton("Nghỉ phép", e -> showLeavePanel()));
        navPanel.add(createNavButton("Đánh giá", e -> showEvaluationPanel()));
        navPanel.add(Box.createHorizontalStrut(20));
        navPanel.add(createLogoutButton());

        headerPanel.add(lblAvatar, BorderLayout.WEST);
        headerPanel.add(welcomePanel, BorderLayout.CENTER);
        headerPanel.add(navPanel, BorderLayout.EAST);
        return headerPanel;
    }

    private JButton createNavButton(String text, java.awt.event.ActionListener action) {
        JButton button = UIHelper.createNavButton(text);
        button.addActionListener(action);
        return button;
    }

    private JButton createLogoutButton() {
        JButton button = UIHelper.createStyledButton("Đăng xuất", UIColors.DANGER_RED, UIColors.WHITE);
        button.addActionListener(e -> performLogout());
        return button;
    }

    private JPanel createStatusPanel() {
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, UIColors.BORDER_GRAY),
                new EmptyBorder(5, 10, 5, 10)));
        statusPanel.setBackground(UIColors.LIGHT_GRAY_BG);
        statusPanel.add(lblStatus, BorderLayout.WEST);

        JLabel lblTime = new JLabel(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        lblTime.setFont(UIFonts.TEXT_SMALL);
        statusPanel.add(lblTime, BorderLayout.EAST);
        return statusPanel;
    }

    private void applyRolePermissions() {
        XacThucBUS auth = XacThucBUS.getInstance();

        mnuEmployeeList.setEnabled(auth.getScopeForAction("EMPLOYEE_VIEW") != DataScope.NONE);
        mnuEmployeeCreate.setEnabled(auth.coQuyen("EMPLOYEE_CREATE"));

        mnuLeaveList.setEnabled(true);
        mnuLeaveRequest.setEnabled(auth.coQuyen("LEAVE_CREATE"));
        mnuLeaveApprove.setEnabled(auth.getScopeForAction("LEAVE_APPROVE") != DataScope.NONE);

        mnuEvalList.setEnabled(true);
        mnuEvalSelf.setEnabled(auth.getScopeForAction("EVAL_VIEW") != DataScope.NONE);
        mnuEvalReview.setEnabled(auth.getScopeForAction("EVAL_REVIEW") != DataScope.NONE);
        mnuEvalManage.setEnabled(auth.coQuyen("EVAL_MANAGE"));

        mnuUserManage.setEnabled(auth.coQuyen("USER_VIEW"));
        mnuRoleManage.setEnabled(auth.coQuyen("ROLE_VIEW"));
        mnuReports.setEnabled(auth.coQuyen("REPORT_VIEW"));
        mnuSettings.setEnabled(auth.coQuyen("CHANGE_PASSWORD"));

        updateStatus();
    }

    private void updateStatus() {
        String permissions = currentUser.coVaiTro("ADMIN") ? "Toàn quyền" : "Quyền hạn theo vai trò";
        lblStatus.setText("Vai trò: " + currentUser.getVaiTros() + " | " + permissions);
    }

    private String getInitials(String fullName) {
        if (fullName == null || fullName.isEmpty()) {
            return "?";
        }
        String[] parts = fullName.split(" ");
        if (parts.length >= 2) {
            return ("" + parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase();
        }
        return ("" + fullName.charAt(0)).toUpperCase();
    }

    private void showDashboard() {
        showCard(CARD_DASHBOARD, "Trang chủ");
    }

    private void showLeavePanel() {
        refreshCard(CARD_LEAVE, new LeaveListPanel(), "Quản lý nghỉ phép");
    }

    private void showEvaluationPanel() {
        refreshCard(CARD_EVAL, new EvalCycleListPanel(), "Quản lý đánh giá");
    }

    private void refreshCard(String cardName, JComponent component, String statusText) {
        mainContentArea.add(component, cardName);
        showCard(cardName, statusText);
    }

    private void showCard(String cardName, String statusText) {
        cardLayout.show(mainContentArea, cardName);
        lblStatus.setText(statusText + " | Vai trò: " + currentUser.getVaiTros());
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
        JTextArea textArea = new JTextArea(buildProfileText());
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(400, 400));

        JOptionPane.showMessageDialog(this, scrollPane, "Thông tin cá nhân", JOptionPane.INFORMATION_MESSAGE);
    }

    private String buildProfileText() {
        StringBuilder info = new StringBuilder();
        info.append("THÔNG TIN TÀI KHOẢN\n");
        info.append("==================\n\n");
        info.append("Họ tên: ").append(currentUser.getHoTen()).append('\n');
        info.append("Tên đăng nhập: ").append(currentUser.getTenDangNhap()).append('\n');
        info.append("Email: ").append(currentUser.getEmail()).append('\n');
        info.append("Vai trò: ").append(currentUser.getVaiTros()).append("\n\n");
        info.append("QUYỀN HẠN:\n");
        info.append("---------\n");

        currentUser.getVaiTros().forEach(role ->
                role.getQuyens().forEach(permission ->
                        info.append("- ").append(permission.getTenQuyen()).append('\n')));
        return info.toString();
    }

    private void showAbout() {
        JOptionPane.showMessageDialog(this,
                "HRM SYSTEM\n" +
                        "Hệ thống quản lý nhân sự\n\n" +
                        "Phiên bản: 1.0\n" +
                        "Module: Leave + Evaluation\n" +
                        "Java Swing Desktop Application\n\n" +
                        "© 2024 HRM System",
                "Giới thiệu",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void showMessage(String feature) {
        JOptionPane.showMessageDialog(this,
                "Chức năng: " + feature + "\n\nĐang phát triển...",
                "Thông báo",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void performLogout() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn đăng xuất?",
                "Xác nhận đăng xuất",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            XacThucBUS.getInstance().logout();
            new LoginFrame().setVisible(true);
            dispose();
        }
    }

    private void centerOnScreen() {
        setLocationRelativeTo(null);
    }
}
