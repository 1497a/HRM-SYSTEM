package com.hrm.gui;

import com.hrm.bus.XacThucBUS;
import com.hrm.bus.KetQua;
import com.hrm.gui.components.PurpleButton;
import com.hrm.gui.components.RoundedPanel;
import com.hrm.gui.leave.LeaveListPanel;
import com.hrm.gui.evaluation.EvalCycleListPanel;
import com.hrm.gui.admin.UserManagementPanel;
import com.hrm.gui.admin.RoleManagementPanel;
import com.hrm.gui.employee.EmployeeListPanel;
import com.hrm.gui.appointment.AppointmentListPanel;
import com.hrm.gui.contract.ContractListPanel;
import com.hrm.gui.salary.SalaryListPanel;
import com.hrm.gui.notification.NotificationPanel;
import com.hrm.gui.recruitment.RecruitmentPanel;
import com.hrm.gui.report.ReportPanel;
import com.hrm.model.TaiKhoan;
import com.hrm.util.SessionContext;
import com.hrm.util.UIColors;
import com.hrm.gui.admin.DepartmentPanel;
import com.hrm.gui.admin.PositionPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import com.hrm.gui.attendance.AttendancePanel;

/**
 * MainFrame - Main application frame with purple theme
 * Features header, sidebar navigation, and dynamic content area
 * Uses SessionContext for session management
 */
public class MainFrame extends JFrame {

    private JPanel headerPanel;
    private JPanel contentPanel;
    private JPanel sidebarPanel;
    private JLabel lblUserName;
    private JLabel lblUserRole;

    // Menu buttons
    private JButton btnDashboard;
    private JButton btnUsers;
    private JButton btnRoles;
    private JButton btnEmployees;
    private JButton btnOrganization; // NV2 - Phòng ban & Chức vụ
    private JButton btnAppointments; // NV3 - Bổ nhiệm
    private JButton btnAttendance; // NV4 - Chấm công
    private JButton btnContracts; // NV5 - Hợp đồng
    private JButton btnPayroll; // NV6 - Lương
    private JButton btnLeave;
    private JButton btnPerformance;
    private JButton btnNotifications; // NV10 - Thông báo
    private JButton btnRecruitment; // NV11 - Tuyển dụng
    private JButton btnReports;
    private JButton btnSettings;
    private JButton btnLogout;

    private JButton currentActiveButton;
    private final XacThucBUS authService;

    public MainFrame() {
        this.authService = XacThucBUS.getInstance();

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

        // Show dashboard by default
        showDashboard();
    }

    private void initComponents() {
        setTitle("HRM System - Hệ thống quản lý nhân sự");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1200, 750);
        setMinimumSize(new Dimension(1000, 650));

        // Header panel - Purple
        headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UIColors.PRIMARY_PURPLE);
        headerPanel.setPreferredSize(new Dimension(0, 65));

        // Sidebar panel
        sidebarPanel = new JPanel();
        sidebarPanel.setPreferredSize(new Dimension(240, 0));
        sidebarPanel.setBackground(UIColors.WHITE);

        // Content panel
        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(UIColors.LIGHT_GRAY_BG);

        // Create menu buttons
        btnDashboard = createMenuButton("Trang chủ", "dashboard");

        // Nhân sự
        btnEmployees = createMenuButton("Hồ sơ nhân viên", "employees");
        btnOrganization = createMenuButton("Cơ cấu tổ chức", "organization");
        btnAppointments = createMenuButton("Bổ nhiệm", "appointments");
        btnRecruitment = createMenuButton("Tuyển dụng", "recruitment");

        // Chấm công & Lương
        btnAttendance = createMenuButton("Chấm công", "attendance");
        btnContracts = createMenuButton("Hợp đồng lao động", "contracts");
        btnPayroll = createMenuButton("Tính lương", "payroll");

        // Chính sách
        btnLeave = createMenuButton("Nghỉ phép", "leave");
        btnPerformance = createMenuButton("Đánh giá hiệu suất", "performance");

        // Hệ thống
        btnUsers = createMenuButton("Quản lý tài khoản", "users");
        btnRoles = createMenuButton("Quản lý vai trò", "roles");
        btnNotifications = createMenuButton("Thông báo", "notifications");
        btnReports = createMenuButton("Báo cáo", "reports");
        btnSettings = createMenuButton("Cài đặt", "settings");
        btnLogout = createMenuButton("Đăng xuất", "logout");
        btnLogout.setForeground(UIColors.DANGER_RED);
    }

    private JButton createMenuButton(String text, String actionCommand) {
        JButton button = new JButton(text);
        button.setActionCommand(actionCommand);
        button.setFont(com.hrm.util.UIFonts.TEXT_MEDIUM);
        button.setForeground(UIColors.TEXT_DARK);
        button.setBackground(UIColors.WHITE);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(220, 45));
        button.setMaximumSize(new Dimension(220, 45));
        button.setBorder(new EmptyBorder(10, 20, 10, 10));

        // Hover effect - Light purple
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
                    button.setBackground(UIColors.WHITE);
                    button.setForeground(UIColors.TEXT_DARK);
                    if (button == btnLogout) {
                        button.setForeground(UIColors.DANGER_RED);
                    }
                }
            }
        });

        return button;
    }

    private void setActiveButton(JButton button) {
        // Reset previous active button
        if (currentActiveButton != null) {
            currentActiveButton.setBackground(UIColors.WHITE);
            currentActiveButton.setForeground(UIColors.TEXT_DARK);
            currentActiveButton.setBorder(new EmptyBorder(10, 20, 10, 10));
        }

        currentActiveButton = button;

        if (button != null) {
            // Active state - Purple left border + light purple background
            button.setBackground(UIColors.LIGHT_PURPLE);
            button.setForeground(UIColors.PRIMARY_PURPLE);
            button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 4, 0, 0, UIColors.PRIMARY_PURPLE),
                    new EmptyBorder(10, 16, 10, 10)));
        }
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        TaiKhoan user = SessionContext.getInstance().getCurrentUser();
        String displayName = user != null ? user.getHoTen() : "Guest";
        String roleName = user != null ? user.getVaiTros().toString() : "";

        // ========================
        // HEADER PANEL
        // ========================
        headerPanel.setBorder(new EmptyBorder(0, 20, 0, 20));

        // Left - Logo
        JPanel logoSection = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        logoSection.setOpaque(false);

        JLabel lblLogo = new JLabel("HRM System");
        lblLogo.setFont(com.hrm.util.UIFonts.HEADER_H2);
        lblLogo.setForeground(com.hrm.util.UIColors.WHITE);
        logoSection.add(lblLogo);

        // Right - TaiKhoan info + Logout
        JPanel userSection = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        userSection.setOpaque(false);

        // TaiKhoan name in header
        JLabel lblHeaderUser = new JLabel(displayName);
        lblHeaderUser.setFont(com.hrm.util.UIFonts.TEXT_MEDIUM);
        lblHeaderUser.setForeground(com.hrm.util.UIColors.WHITE);

        // VaiTro badge
        JLabel lblHeaderRole = new JLabel(roleName);
        lblHeaderRole.setFont(com.hrm.util.UIFonts.TEXT_SMALL);
        lblHeaderRole.setForeground(new Color(255, 255, 255, 180));

        // Logout button in header
        JButton btnHeaderLogout = new JButton("Đăng xuất");
        btnHeaderLogout.setFont(com.hrm.util.UIFonts.TEXT_SMALL);
        btnHeaderLogout.setForeground(com.hrm.util.UIColors.WHITE);
        btnHeaderLogout.setBackground(UIColors.DARK_PURPLE);
        btnHeaderLogout.setBorderPainted(false);
        btnHeaderLogout.setFocusPainted(false);
        btnHeaderLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnHeaderLogout.setBorder(new EmptyBorder(8, 15, 8, 15));
        btnHeaderLogout.addActionListener(e -> logout());
        btnHeaderLogout.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btnHeaderLogout.setBackground(UIColors.PURPLE_HOVER);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                btnHeaderLogout.setBackground(UIColors.DARK_PURPLE);
            }
        });

        JLabel separator = new JLabel(" | ");
        separator.setForeground(new Color(255, 255, 255, 100));

        userSection.add(lblHeaderUser);
        userSection.add(separator);
        userSection.add(lblHeaderRole);
        userSection.add(Box.createHorizontalStrut(10));
        userSection.add(btnHeaderLogout);

        headerPanel.add(logoSection, BorderLayout.WEST);
        headerPanel.add(userSection, BorderLayout.EAST);

        // ========================
        // SIDEBAR PANEL with Scroll
        // ========================
        JPanel sidebarContent = new JPanel();
        sidebarContent.setLayout(new BoxLayout(sidebarContent, BoxLayout.Y_AXIS));
        sidebarContent.setBackground(UIColors.WHITE);

        // Profile section with light purple background
        JPanel profilePanel = new JPanel();
        profilePanel.setLayout(new BoxLayout(profilePanel, BoxLayout.Y_AXIS));
        profilePanel.setBackground(UIColors.LIGHT_PURPLE);
        profilePanel.setMaximumSize(new Dimension(240, 120));
        profilePanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        profilePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Avatar circle
        JLabel lblAvatar = new JLabel(getInitials(displayName));
        lblAvatar.setFont(com.hrm.util.UIFonts.HEADER_H1);
        lblAvatar.setForeground(com.hrm.util.UIColors.WHITE);
        lblAvatar.setBackground(UIColors.PRIMARY_PURPLE);
        lblAvatar.setOpaque(true);
        lblAvatar.setPreferredSize(new Dimension(60, 60));
        lblAvatar.setMinimumSize(new Dimension(60, 60));
        lblAvatar.setMaximumSize(new Dimension(60, 60));
        lblAvatar.setHorizontalAlignment(SwingConstants.CENTER);
        lblAvatar.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblUserName = new JLabel(displayName);
        lblUserName.setFont(com.hrm.util.UIFonts.BOLD_MEDIUM);
        lblUserName.setForeground(UIColors.TEXT_DARK);
        lblUserName.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblUserRole = new JLabel(roleName);
        lblUserRole.setFont(com.hrm.util.UIFonts.TEXT_SMALL);
        lblUserRole.setForeground(UIColors.TEXT_GRAY);
        lblUserRole.setAlignmentX(Component.LEFT_ALIGNMENT);

        profilePanel.add(lblAvatar);
        profilePanel.add(Box.createVerticalStrut(10));
        profilePanel.add(lblUserName);
        profilePanel.add(Box.createVerticalStrut(2));
        profilePanel.add(lblUserRole);

        sidebarContent.add(profilePanel);
        sidebarContent.add(Box.createVerticalStrut(15));

        // Menu section
        JLabel lblMenu = new JLabel("MENU");
        lblMenu.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblMenu.setForeground(UIColors.TEXT_GRAY);
        lblMenu.setBorder(new EmptyBorder(5, 20, 10, 0));
        lblMenu.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblMenu.setMaximumSize(new Dimension(240, 30));
        sidebarContent.add(lblMenu);

        // All menu items - flat list
        sidebarContent.add(btnDashboard);
        sidebarContent.add(btnEmployees);
        sidebarContent.add(btnOrganization);
        sidebarContent.add(btnAppointments);
        sidebarContent.add(btnRecruitment);
        sidebarContent.add(btnAttendance);
        sidebarContent.add(btnContracts);
        sidebarContent.add(btnPayroll);
        sidebarContent.add(btnLeave);
        sidebarContent.add(btnPerformance);
        sidebarContent.add(btnUsers);
        sidebarContent.add(btnRoles);
        sidebarContent.add(btnNotifications);
        sidebarContent.add(btnReports);
        sidebarContent.add(btnSettings);

        // Push logout to bottom
        sidebarContent.add(Box.createVerticalGlue());
        sidebarContent.add(btnLogout);
        sidebarContent.add(Box.createVerticalStrut(15));

        // Wrap in scroll pane
        JScrollPane sidebarScroll = new JScrollPane(sidebarContent);
        sidebarScroll.setPreferredSize(new Dimension(240, 0));
        sidebarScroll.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, UIColors.BORDER_GRAY));
        sidebarScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        sidebarScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        sidebarScroll.getVerticalScrollBar().setUnitIncrement(16);

        // ========================
        // ADD TO FRAME
        // ========================
        add(headerPanel, BorderLayout.NORTH);
        add(sidebarScroll, BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);
    }

    private String getInitials(String name) {
        if (name == null || name.isEmpty())
            return "?";
        String[] parts = name.split(" ");
        if (parts.length >= 2) {
            return ("" + parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase();
        }
        return ("" + name.charAt(0)).toUpperCase();
    }

    private void setupEvents() {
        // Window closing
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                confirmExit();
            }
        });

        // Menu button actions
        btnDashboard.addActionListener(e -> showDashboard());

        // Nhan su
        btnEmployees.addActionListener(e -> showEmployeeManagement());
        btnOrganization.addActionListener(e -> showOrganization());
        btnAppointments.addActionListener(e -> showAppointmentManagement());
        btnRecruitment.addActionListener(e -> showRecruitment());

        // Cham cong & Luong
        btnAttendance.addActionListener(e -> showAttendance());
        btnContracts.addActionListener(e -> showContractManagement());
        btnPayroll.addActionListener(e -> showSalaryManagement());

        // Chinh sach
        btnLeave.addActionListener(e -> showLeaveManagement());
        btnPerformance.addActionListener(e -> showPerformanceEvaluation());

        // He thong
        btnUsers.addActionListener(e -> showUserManagement());
        btnRoles.addActionListener(e -> showRoleManagement());
        btnNotifications.addActionListener(e -> showNotifications());
        btnReports.addActionListener(e -> showReports());
        btnSettings.addActionListener(e -> showSettings());
        btnLogout.addActionListener(e -> logout());
    }

    private void setupPermissions() {
        SessionContext sc = SessionContext.getInstance();

        com.hrm.bus.XacThucBUS auth = com.hrm.bus.XacThucBUS.getInstance();
        com.hrm.model.DataScope none = com.hrm.model.DataScope.NONE;

        // Hien/an button theo quyen — dung getScopeForAction() cho cac action co scope
        btnEmployees   .setVisible(auth.getScopeForAction("EMPLOYEE_VIEW")    != none);
        btnOrganization.setVisible(sc.coQuyen("DEPARTMENT_VIEW") || sc.coQuyen("POSITION_VIEW"));
        btnAppointments.setVisible(auth.getScopeForAction("APPOINTMENT_VIEW") != none);
        btnRecruitment .setVisible(auth.getScopeForAction("RECRUITMENT_VIEW") != none);
        btnAttendance  .setVisible(auth.getScopeForAction("ATTENDANCE_VIEW")  != none);
        btnContracts   .setVisible(auth.getScopeForAction("CONTRACT_VIEW")    != none);
        btnPayroll     .setVisible(auth.getScopeForAction("PAYROLL_VIEW")     != none);
        btnLeave       .setVisible(auth.getScopeForAction("LEAVE_VIEW")       != none);
        btnPerformance .setVisible(auth.getScopeForAction("EVAL_VIEW")        != none);
        btnUsers       .setVisible(sc.coQuyen("USER_VIEW"));
        btnRoles       .setVisible(sc.coQuyen("ROLE_VIEW"));
        btnReports     .setVisible(sc.coQuyen("REPORT_VIEW"));
        btnSettings    .setVisible(sc.coQuyen("SETTINGS_VIEW"));

        // Everyone can see notifications
        btnNotifications.setVisible(true);

    }


    private void showDashboard() {
        setActiveButton(btnDashboard);
        contentPanel.removeAll();

        com.hrm.model.DataScope empScope =
                com.hrm.bus.XacThucBUS.getInstance().getScopeForAction("EMPLOYEE_VIEW");

        JPanel dashPanel;
        if (empScope == com.hrm.model.DataScope.ALL
                || empScope == com.hrm.model.DataScope.DEPT
                || empScope == com.hrm.model.DataScope.TEAM) {
            dashPanel = buildManagerDashboard(empScope);
        } else {
            dashPanel = buildPersonalDashboard();
        }

        contentPanel.add(dashPanel);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    /** Dashboard quản lý: thống kê toàn hệ thống / phòng ban / team tùy scope */
    private JPanel buildManagerDashboard(com.hrm.model.DataScope scope) {
        com.hrm.bus.XacThucBUS auth = com.hrm.bus.XacThucBUS.getInstance();
        TaiKhoan cu = SessionContext.getInstance().getCurrentUser();
        String maNV = cu != null ? cu.getNhanVienId() : null;

        JPanel root = new JPanel(new BorderLayout(0, 20));
        root.setBackground(UIColors.LIGHT_GRAY_BG);
        root.setBorder(new EmptyBorder(25, 25, 25, 25));

        // ── Tiêu đề (không lặp tên người dùng vì sidebar đã hiện) ──
        JLabel title = new JLabel("Tổng quan hệ thống");
        title.setFont(com.hrm.util.UIFonts.HEADER_H1);
        title.setForeground(UIColors.TEXT_DARK);
        root.add(title, BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);

        // ── Stat cards ──
        java.util.List<JPanel> cards = new java.util.ArrayList<>();

        // Nhân viên đang làm việc
        if (auth.getScopeForAction("EMPLOYEE_VIEW") != com.hrm.model.DataScope.NONE) {
            try {
                long dangLam = com.hrm.dao.NhanVienDAO.getInstance().findAll().stream()
                        .filter(nv -> "dang_lam_viec".equals(nv.getTrangThai())).count();
                cards.add(RoundedPanel.createStatCard("NV đang làm việc",
                        String.valueOf(dangLam), UIColors.PRIMARY_PURPLE));
            } catch (Exception ignored) {
                cards.add(RoundedPanel.createStatCard("NV đang làm việc", "—", UIColors.PRIMARY_PURPLE));
            }
        }

        // Nghỉ phép chờ duyệt (theo scope)
        if (auth.getScopeForAction("LEAVE_VIEW") != com.hrm.model.DataScope.NONE) {
            try {
                long pending = com.hrm.dao.NghiPhepDAO.getInstance()
                        .findChoDuyetByScope(scope, maNV).size();
                cards.add(RoundedPanel.createStatCard("Đơn nghỉ chờ duyệt",
                        String.valueOf(pending), UIColors.DANGER_RED));
            } catch (Exception ignored) {
                cards.add(RoundedPanel.createStatCard("Đơn nghỉ chờ duyệt", "—", UIColors.DANGER_RED));
            }
        }

        // Bảng lương tháng này
        if (auth.getScopeForAction("PAYROLL_VIEW") != com.hrm.model.DataScope.NONE) {
            try {
                java.time.LocalDate today = java.time.LocalDate.now();
                com.hrm.model.BangLuong bl = com.hrm.dao.BangLuongDAO.getInstance()
                        .findByThangNam(today.getMonthValue(), today.getYear());
                String blStatus = bl != null ? bl.getTrangThai().getDisplayName() : "Chưa tạo";
                cards.add(RoundedPanel.createStatCard("Lương tháng " + today.getMonthValue(),
                        blStatus, UIColors.SUCCESS_GREEN));
            } catch (Exception ignored) {
                cards.add(RoundedPanel.createStatCard("Lương tháng này", "—", UIColors.SUCCESS_GREEN));
            }
        }

        // Tuyển dụng đang mở
        if (auth.getScopeForAction("RECRUITMENT_VIEW") != com.hrm.model.DataScope.NONE) {
            try {
                long dangTuyen = com.hrm.dao.TuyenDungDAO.getInstance().findAllTin()
                        .stream().filter(t -> "dang_tuyen".equals(t.getTrangThai())).count();
                cards.add(RoundedPanel.createStatCard("Tuyển dụng đang mở",
                        String.valueOf(dangTuyen), UIColors.WARNING_YELLOW));
            } catch (Exception ignored) {
                cards.add(RoundedPanel.createStatCard("Tuyển dụng đang mở", "—", UIColors.WARNING_YELLOW));
            }
        }

        // Grid cards (tối đa 3 cột)
        int cols = Math.min(cards.size(), 3);
        if (cols > 0) {
            int rows = (int) Math.ceil((double) cards.size() / cols);
            JPanel cardsGrid = new JPanel(new GridLayout(rows, cols, 18, 18));
            cardsGrid.setOpaque(false);
            cardsGrid.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
            for (JPanel c : cards) cardsGrid.add(c);
            body.add(cardsGrid);
            body.add(javax.swing.Box.createVerticalStrut(28));
        }



        JScrollPane scroll = new JScrollPane(body);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        root.add(scroll, BorderLayout.CENTER);
        return root;
    }

    /** Dashboard cá nhân: phép còn lại, đơn chờ, lương gần nhất */
    private JPanel buildPersonalDashboard() {
        TaiKhoan cu = SessionContext.getInstance().getCurrentUser();
        String maNV = cu != null ? cu.getNhanVienId() : null;
        int year = java.time.LocalDate.now().getYear();

        JPanel root = new JPanel(new BorderLayout(0, 20));
        root.setBackground(UIColors.LIGHT_GRAY_BG);
        root.setBorder(new EmptyBorder(25, 25, 25, 25));

        JLabel title = new JLabel("Thông tin của tôi");
        title.setFont(com.hrm.util.UIFonts.HEADER_H1);
        title.setForeground(UIColors.TEXT_DARK);
        root.add(title, BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);

        // ── Stat cards ──
        JPanel cardsGrid = new JPanel(new GridLayout(1, 3, 18, 18));
        cardsGrid.setOpaque(false);
        cardsGrid.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);

        // Phép năm còn lại
        try {
            com.hrm.model.SoDungPhep phepNam =
                    com.hrm.dao.NghiPhepDAO.getInstance()
                            .findByMaNVAndNamAndLoai(maNV, year, "PHEP_NAM");
            double conLai = phepNam != null ? phepNam.getRemainingDays() : 0;
            cardsGrid.add(RoundedPanel.createStatCard("Phép năm còn lại",
                    String.valueOf((int) conLai) + " ngày", UIColors.PRIMARY_PURPLE));
        } catch (Exception ignored) {
            cardsGrid.add(RoundedPanel.createStatCard("Phép năm còn lại", "—", UIColors.PRIMARY_PURPLE));
        }

        // Đơn nghỉ chờ duyệt của tôi
        try {
            long choDuyet = maNV == null ? 0 :
                    com.hrm.dao.NghiPhepDAO.getInstance().findByMaNV(maNV).stream()
                            .filter(d -> com.hrm.model.DonXinNghiPhep.TrangThai.CHO_DUYET.equals(d.getTrangThai()))
                            .count();
            cardsGrid.add(RoundedPanel.createStatCard("Đơn đang chờ duyệt",
                    String.valueOf(choDuyet), UIColors.WARNING_YELLOW));
        } catch (Exception ignored) {
            cardsGrid.add(RoundedPanel.createStatCard("Đơn đang chờ duyệt", "—", UIColors.WARNING_YELLOW));
        }

        // Lương tháng gần nhất
        try {
            String luongText = "Chưa có";
            if (maNV != null) {
                java.util.List<com.hrm.model.BangLuong> allBL =
                        com.hrm.dao.BangLuongDAO.getInstance().findAll();
                // Lấy bảng lương mới nhất (lớn nhất maBL) có chi tiết của nhân viên này
                for (int i = allBL.size() - 1; i >= 0; i--) {
                    com.hrm.model.ChiTietLuong ctl =
                            com.hrm.dao.BangLuongDAO.getInstance()
                                    .findByBangLuongAndNV(allBL.get(i).getMaBL(), maNV);
                    if (ctl != null) {
                        java.text.NumberFormat fmt =
                                java.text.NumberFormat.getNumberInstance(new java.util.Locale("vi", "VN"));
                        luongText = fmt.format((long) ctl.getLuongThucNhan()) + " đ";
                        break;
                    }
                }
            }
            cardsGrid.add(RoundedPanel.createStatCard("Lương gần nhất", luongText, UIColors.SUCCESS_GREEN));
        } catch (Exception ignored) {
            cardsGrid.add(RoundedPanel.createStatCard("Lương gần nhất", "—", UIColors.SUCCESS_GREEN));
        }

        body.add(cardsGrid);
        body.add(javax.swing.Box.createVerticalStrut(28));



        JScrollPane scroll = new JScrollPane(body);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        root.add(scroll, BorderLayout.CENTER);
        return root;
    }

    /** Tạo nút Quick Action với màu accent và icon text */
    private JButton createQuickActionButton(String label, Color accent,
                                             java.awt.event.ActionListener action) {
        JButton btn = new JButton(label);
        btn.setFont(com.hrm.util.UIFonts.BOLD_NORMAL);
        btn.setForeground(accent);
        btn.setBackground(UIColors.WHITE);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(accent, 1, true),
                new EmptyBorder(10, 18, 10, 18)));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(action);
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(accent);
                btn.setForeground(com.hrm.util.UIColors.WHITE);
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(UIColors.WHITE);
                btn.setForeground(accent);
            }
        });
        return btn;
    }

    private void showSettings() {
        setActiveButton(btnSettings);
        contentPanel.removeAll();

        JPanel settingsPanel = new JPanel(new BorderLayout());
        settingsPanel.setBackground(UIColors.LIGHT_GRAY_BG);
        settingsPanel.setBorder(new EmptyBorder(25, 25, 25, 25));

        JLabel lblHeader = new JLabel("Cài đặt tài khoản");
        lblHeader.setFont(com.hrm.util.UIFonts.HEADER_H1);
        lblHeader.setForeground(UIColors.TEXT_DARK);
        settingsPanel.add(lblHeader, BorderLayout.NORTH);

        // Change password card
        RoundedPanel passwordCard = RoundedPanel.createFlatCard();
        passwordCard.setLayout(new GridBagLayout());
        passwordCard.setBorder(new EmptyBorder(25, 25, 25, 25));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        JLabel lblPasswordTitle = new JLabel("Đổi mật khẩu");
        lblPasswordTitle.setFont(com.hrm.util.UIFonts.HEADER_H3);
        lblPasswordTitle.setForeground(UIColors.PRIMARY_PURPLE);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        passwordCard.add(lblPasswordTitle, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 1;
        JLabel lbl1 = new JLabel("Mật khẩu hiện tại:");
        lbl1.setFont(com.hrm.util.UIFonts.TEXT_MEDIUM);
        passwordCard.add(lbl1, gbc);

        JPasswordField txtCurrentPass = new JPasswordField(20);
        txtCurrentPass.setFont(com.hrm.util.UIFonts.TEXT_MEDIUM);
        gbc.gridx = 1;
        passwordCard.add(txtCurrentPass, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        JLabel lbl2 = new JLabel("Mật khẩu mới:");
        lbl2.setFont(com.hrm.util.UIFonts.TEXT_MEDIUM);
        passwordCard.add(lbl2, gbc);

        JPasswordField txtNewPass = new JPasswordField(20);
        txtNewPass.setFont(com.hrm.util.UIFonts.TEXT_MEDIUM);
        gbc.gridx = 1;
        passwordCard.add(txtNewPass, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        JLabel lbl3 = new JLabel("Xác nhận mật khẩu:");
        lbl3.setFont(com.hrm.util.UIFonts.TEXT_MEDIUM);
        passwordCard.add(lbl3, gbc);

        JPasswordField txtConfirmPass = new JPasswordField(20);
        txtConfirmPass.setFont(com.hrm.util.UIFonts.TEXT_MEDIUM);
        gbc.gridx = 1;
        passwordCard.add(txtConfirmPass, gbc);

        PurpleButton btnChangePassword = new PurpleButton("Đổi mật khẩu");
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.insets = new Insets(20, 8, 8, 8);
        passwordCard.add(btnChangePassword, gbc);

        btnChangePassword.addActionListener(e -> {
            String currentPass = new String(txtCurrentPass.getPassword());
            String newPass = new String(txtNewPass.getPassword());
            String confirmPass = new String(txtConfirmPass.getPassword());

            if (currentPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin",
                        "Loi", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (!newPass.equals(confirmPass)) {
                JOptionPane.showMessageDialog(this, "Mật khẩu xác nhận không khớp",
                        "Loi", JOptionPane.WARNING_MESSAGE);
                return;
            }

            TaiKhoan currentUser = authService.getCurrentUser();
            if (currentUser != null) {
                KetQua<Void> result = authService.changePassword(currentUser.getId(), currentPass, newPass);
                if (result.isSuccess()) {
                    JOptionPane.showMessageDialog(this, "Đổi mật khẩu thành công!",
                            "Thong bao", JOptionPane.INFORMATION_MESSAGE);
                    txtCurrentPass.setText("");
                    txtNewPass.setText("");
                    txtConfirmPass.setText("");
                } else {
                    JOptionPane.showMessageDialog(this, result.getMessage(),
                            "Loi", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Không tìm thấy người dùng hiện tại",
                        "Loi", JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        centerPanel.setOpaque(false);
        centerPanel.setBorder(new EmptyBorder(25, 0, 0, 0));
        centerPanel.add(passwordCard);

        settingsPanel.add(centerPanel, BorderLayout.CENTER);

        contentPanel.add(settingsPanel);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void showPlaceholder(String title) {
        contentPanel.removeAll();

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UIColors.LIGHT_GRAY_BG);
        panel.setBorder(new EmptyBorder(25, 25, 25, 25));

        JLabel lblHeader = new JLabel(title);
        lblHeader.setFont(com.hrm.util.UIFonts.HEADER_H1);
        lblHeader.setForeground(UIColors.TEXT_DARK);
        panel.add(lblHeader, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);

        JLabel lblPlaceholder = new JLabel("Chức năng đang phát triển...");
        lblPlaceholder.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        lblPlaceholder.setForeground(UIColors.TEXT_GRAY);
        centerPanel.add(lblPlaceholder);

        panel.add(centerPanel, BorderLayout.CENTER);

        contentPanel.add(panel);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void showLeaveManagement() {
        setActiveButton(btnLeave);
        contentPanel.removeAll();

        JPanel wrapperPanel = new JPanel(new BorderLayout());
        wrapperPanel.setBackground(UIColors.LIGHT_GRAY_BG);
        wrapperPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel lblHeader = new JLabel("Quản lý nghỉ phép");
        lblHeader.setFont(com.hrm.util.UIFonts.HEADER_H1);
        lblHeader.setForeground(UIColors.TEXT_DARK);
        lblHeader.setBorder(new EmptyBorder(0, 10, 15, 0));
        wrapperPanel.add(lblHeader, BorderLayout.NORTH);

        // Add LeaveListPanel
        LeaveListPanel leavePanel = new LeaveListPanel();
        wrapperPanel.add(leavePanel, BorderLayout.CENTER);

        contentPanel.add(wrapperPanel);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void showAttendance() {
        setActiveButton(btnAttendance);
        contentPanel.removeAll();

        JPanel wrapperPanel = new JPanel(new BorderLayout());
        wrapperPanel.setBackground(UIColors.LIGHT_GRAY_BG);
        wrapperPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel lblHeader = new JLabel("Chấm công & Làm thêm giờ");
        lblHeader.setFont(com.hrm.util.UIFonts.HEADER_H1);
        lblHeader.setForeground(UIColors.TEXT_DARK);
        lblHeader.setBorder(new EmptyBorder(0, 10, 15, 0));
        wrapperPanel.add(lblHeader, BorderLayout.NORTH);

        // Nhúng AttendancePanel vào content area
        AttendancePanel attendancePanel = new AttendancePanel();
        wrapperPanel.add(attendancePanel, BorderLayout.CENTER);

        contentPanel.add(wrapperPanel);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void showPerformanceEvaluation() {
        setActiveButton(btnPerformance);
        contentPanel.removeAll();

        JPanel wrapperPanel = new JPanel(new BorderLayout());
        wrapperPanel.setBackground(UIColors.LIGHT_GRAY_BG);
        wrapperPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel lblHeader = new JLabel("Đánh giá hiệu suất");
        lblHeader.setFont(com.hrm.util.UIFonts.HEADER_H1);
        lblHeader.setForeground(UIColors.TEXT_DARK);
        lblHeader.setBorder(new EmptyBorder(0, 10, 15, 0));
        wrapperPanel.add(lblHeader, BorderLayout.NORTH);

        // Add EvalCycleListPanel
        EvalCycleListPanel evalPanel = new EvalCycleListPanel();
        wrapperPanel.add(evalPanel, BorderLayout.CENTER);

        contentPanel.add(wrapperPanel);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void showUserManagement() {
        setActiveButton(btnUsers);
        contentPanel.removeAll();

        JPanel wrapperPanel = new JPanel(new BorderLayout());
        wrapperPanel.setBackground(UIColors.LIGHT_GRAY_BG);
        wrapperPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel lblHeader = new JLabel("Quản lý tài khoản");
        lblHeader.setFont(com.hrm.util.UIFonts.HEADER_H1);
        lblHeader.setForeground(UIColors.TEXT_DARK);
        lblHeader.setBorder(new EmptyBorder(0, 10, 15, 0));
        wrapperPanel.add(lblHeader, BorderLayout.NORTH);

        // Add UserManagementPanel
        UserManagementPanel userPanel = new UserManagementPanel();
        wrapperPanel.add(userPanel, BorderLayout.CENTER);

        contentPanel.add(wrapperPanel);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void showRoleManagement() {
        setActiveButton(btnRoles);
        contentPanel.removeAll();

        JPanel wrapperPanel = new JPanel(new BorderLayout());
        wrapperPanel.setBackground(UIColors.LIGHT_GRAY_BG);
        wrapperPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel lblHeader = new JLabel("Quản lý vai trò");
        lblHeader.setFont(com.hrm.util.UIFonts.HEADER_H1);
        lblHeader.setForeground(UIColors.TEXT_DARK);
        lblHeader.setBorder(new EmptyBorder(0, 10, 15, 0));
        wrapperPanel.add(lblHeader, BorderLayout.NORTH);

        // Add RoleManagementPanel
        RoleManagementPanel rolePanel = new RoleManagementPanel();
        wrapperPanel.add(rolePanel, BorderLayout.CENTER);

        contentPanel.add(wrapperPanel);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn đăng xuất?",
                "Xác nhận đăng xuất",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            authService.logout();
            dispose();
            new LoginFrame().setVisible(true);
        }
    }

    private void confirmExit() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn thoát ứng dụng?",
                "Xác nhận thoát",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            authService.logout();
            System.exit(0);
        }
    }

    private void showOrganization() {
        setActiveButton(btnOrganization);
        contentPanel.removeAll();

        JPanel wrapperPanel = new JPanel(new BorderLayout());
        wrapperPanel.setBackground(UIColors.LIGHT_GRAY_BG);
        wrapperPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Header
        JLabel lblHeader = new JLabel("Quản lý Tổ chức & Chức vụ");
        lblHeader.setFont(com.hrm.util.UIFonts.HEADER_H1);
        lblHeader.setForeground(UIColors.TEXT_DARK);
        lblHeader.setBorder(new EmptyBorder(0, 10, 15, 0));
        wrapperPanel.add(lblHeader, BorderLayout.NORTH);

        // Tạo JTabbedPane với 2 tab
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(com.hrm.util.UIFonts.TEXT_MEDIUM);
        tabbedPane.setBackground(UIColors.WHITE);

        // Tab 1: Phòng ban
        DepartmentPanel departmentPanel = new DepartmentPanel();
        tabbedPane.addTab("Phòng ban", departmentPanel);

        // Tab 2: Chức vụ
        PositionPanel positionPanel = new PositionPanel();
        tabbedPane.addTab("Chức vụ", positionPanel);

        wrapperPanel.add(tabbedPane, BorderLayout.CENTER);

        contentPanel.add(wrapperPanel);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void showEmployeeManagement() {
        setActiveButton(btnEmployees);
        contentPanel.removeAll();
        JPanel wrapperPanel = new JPanel(new BorderLayout());
        wrapperPanel.setBackground(UIColors.LIGHT_GRAY_BG);
        wrapperPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        JLabel lblHeader = new JLabel("Hồ sơ nhân viên");
        lblHeader.setFont(com.hrm.util.UIFonts.HEADER_H1);
        lblHeader.setForeground(UIColors.TEXT_DARK);
        lblHeader.setBorder(new EmptyBorder(0, 10, 15, 0));
        wrapperPanel.add(lblHeader, BorderLayout.NORTH);
        wrapperPanel.add(new EmployeeListPanel(), BorderLayout.CENTER);
        contentPanel.add(wrapperPanel);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void showAppointmentManagement() {
        setActiveButton(btnAppointments);
        contentPanel.removeAll();
        JPanel wrapperPanel = new JPanel(new BorderLayout());
        wrapperPanel.setBackground(UIColors.LIGHT_GRAY_BG);
        wrapperPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        JLabel lblHeader = new JLabel("Bổ nhiệm & Phân công");
        lblHeader.setFont(com.hrm.util.UIFonts.HEADER_H1);
        lblHeader.setForeground(UIColors.TEXT_DARK);
        lblHeader.setBorder(new EmptyBorder(0, 10, 15, 0));
        wrapperPanel.add(lblHeader, BorderLayout.NORTH);
        wrapperPanel.add(new AppointmentListPanel(), BorderLayout.CENTER);
        contentPanel.add(wrapperPanel);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void showContractManagement() {
        setActiveButton(btnContracts);
        contentPanel.removeAll();
        JPanel wrapperPanel = new JPanel(new BorderLayout());
        wrapperPanel.setBackground(UIColors.LIGHT_GRAY_BG);
        wrapperPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        JLabel lblHeader = new JLabel("Hợp đồng lao động");
        lblHeader.setFont(com.hrm.util.UIFonts.HEADER_H1);
        lblHeader.setForeground(UIColors.TEXT_DARK);
        lblHeader.setBorder(new EmptyBorder(0, 10, 15, 0));
        wrapperPanel.add(lblHeader, BorderLayout.NORTH);
        wrapperPanel.add(new ContractListPanel(), BorderLayout.CENTER);
        contentPanel.add(wrapperPanel);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void showSalaryManagement() {
        setActiveButton(btnPayroll);
        contentPanel.removeAll();
        JPanel wrapperPanel = new JPanel(new BorderLayout());
        wrapperPanel.setBackground(UIColors.LIGHT_GRAY_BG);
        wrapperPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        JLabel lblHeader = new JLabel("Tính lương");
        lblHeader.setFont(com.hrm.util.UIFonts.HEADER_H1);
        lblHeader.setForeground(UIColors.TEXT_DARK);
        lblHeader.setBorder(new EmptyBorder(0, 10, 15, 0));
        wrapperPanel.add(lblHeader, BorderLayout.NORTH);
        wrapperPanel.add(new SalaryListPanel(), BorderLayout.CENTER);
        contentPanel.add(wrapperPanel);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void showNotifications() {
        setActiveButton(btnNotifications);
        contentPanel.removeAll();
        JPanel wrapperPanel = new JPanel(new BorderLayout());
        wrapperPanel.setBackground(UIColors.LIGHT_GRAY_BG);
        wrapperPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        JLabel lblHeader = new JLabel("Thông báo");
        lblHeader.setFont(com.hrm.util.UIFonts.HEADER_H1);
        lblHeader.setForeground(UIColors.TEXT_DARK);
        lblHeader.setBorder(new EmptyBorder(0, 10, 15, 0));
        wrapperPanel.add(lblHeader, BorderLayout.NORTH);
        wrapperPanel.add(new NotificationPanel(), BorderLayout.CENTER);
        contentPanel.add(wrapperPanel);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void showRecruitment() {
        setActiveButton(btnRecruitment);
        contentPanel.removeAll();
        JPanel wrapperPanel = new JPanel(new BorderLayout());
        wrapperPanel.setBackground(UIColors.LIGHT_GRAY_BG);
        wrapperPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        JLabel lblHeader = new JLabel("Tuyển dụng");
        lblHeader.setFont(com.hrm.util.UIFonts.HEADER_H1);
        lblHeader.setForeground(UIColors.TEXT_DARK);
        lblHeader.setBorder(new EmptyBorder(0, 10, 15, 0));
        wrapperPanel.add(lblHeader, BorderLayout.NORTH);
        wrapperPanel.add(new RecruitmentPanel(), BorderLayout.CENTER);
        contentPanel.add(wrapperPanel);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void showReports() {
        setActiveButton(btnReports);
        contentPanel.removeAll();
        JPanel wrapperPanel = new JPanel(new BorderLayout());
        wrapperPanel.setBackground(UIColors.LIGHT_GRAY_BG);
        wrapperPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        JLabel lblHeader = new JLabel("Báo cáo");
        lblHeader.setFont(com.hrm.util.UIFonts.HEADER_H1);
        lblHeader.setForeground(UIColors.TEXT_DARK);
        lblHeader.setBorder(new EmptyBorder(0, 10, 15, 0));
        wrapperPanel.add(lblHeader, BorderLayout.NORTH);
        wrapperPanel.add(new ReportPanel(), BorderLayout.CENTER);
        contentPanel.add(wrapperPanel);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

}
