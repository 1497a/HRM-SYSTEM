package com.hrm.gui;

import com.hrm.model.TaiKhoan;
import com.hrm.bus.XacThucBUS;
import com.hrm.gui.components.PurpleButton;
import com.hrm.util.UIColors;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * LoginFrame - Split-panel login screen with purple theme
 * Uses Mock XacThucBUS for authentication
 */
public class LoginFrame extends JFrame {

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private PurpleButton btnLogin;
    private JLabel lblError;
    private JCheckBox chkShowPassword;

    private final XacThucBUS authService;

    public LoginFrame() {
        this.authService = XacThucBUS.getInstance();
        initComponents();
        setupLayout();
        setupEvents();
        centerOnScreen();
    }

    private void initComponents() {
        setTitle("HRM System - Dang Nhap");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setSize(900, 550);

        // Username field
        txtUsername = new JTextField(20);
        txtUsername.setFont(com.hrm.util.UIFonts.TEXT_MEDIUM);
        txtUsername.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, UIColors.BORDER_GRAY),
                BorderFactory.createEmptyBorder(8, 5, 8, 5)));
        txtUsername.setPreferredSize(new Dimension(280, 40));

        // Password field
        txtPassword = new JPasswordField(20);
        txtPassword.setFont(com.hrm.util.UIFonts.TEXT_MEDIUM);
        txtPassword.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, UIColors.BORDER_GRAY),
                BorderFactory.createEmptyBorder(8, 5, 8, 5)));
        txtPassword.setPreferredSize(new Dimension(280, 40));

        // Login button
        btnLogin = new PurpleButton("DANG NHAP");
        btnLogin.setPreferredSize(new Dimension(280, 45));

        // Show password checkbox
        chkShowPassword = new JCheckBox("Hien thi mat khau");
        chkShowPassword.setFont(com.hrm.util.UIFonts.TEXT_SMALL);
        chkShowPassword.setForeground(UIColors.TEXT_GRAY);
        chkShowPassword.setOpaque(false);
        chkShowPassword.setFocusPainted(false);

        // Error label
        lblError = new JLabel(" ");
        lblError.setForeground(UIColors.DANGER_RED);
        lblError.setFont(com.hrm.util.UIFonts.TEXT_SMALL);
        lblError.setHorizontalAlignment(SwingConstants.CENTER);
    }

    private void setupLayout() {
        // Main panel with split layout
        JPanel mainPanel = new JPanel(new GridLayout(1, 2));
        mainPanel.setPreferredSize(new Dimension(900, 550));

        // ========================
        // LEFT PANEL - Purple Welcome
        // ========================
        JPanel leftPanel = new JPanel(new GridBagLayout());
        leftPanel.setBackground(UIColors.PRIMARY_PURPLE);

        JPanel welcomeContent = new JPanel();
        welcomeContent.setLayout(new BoxLayout(welcomeContent, BoxLayout.Y_AXIS));
        welcomeContent.setOpaque(false);
        welcomeContent.setBorder(new EmptyBorder(0, 50, 0, 50));

        JLabel lblWelcome = new JLabel("WELCOME");
        lblWelcome.setFont(com.hrm.util.UIFonts.DISPLAY_LARGE);
        lblWelcome.setForeground(com.hrm.util.UIColors.WHITE);
        lblWelcome.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblSubtitle = new JLabel("To the best HR Management System");
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        lblSubtitle.setForeground(new Color(255, 255, 255, 200));
        lblSubtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Decorative line
        JPanel line = new JPanel();
        line.setBackground(new Color(255, 255, 255, 100));
        line.setPreferredSize(new Dimension(60, 4));
        line.setMaximumSize(new Dimension(60, 4));
        line.setAlignmentX(Component.LEFT_ALIGNMENT);

        welcomeContent.add(lblWelcome);
        welcomeContent.add(Box.createVerticalStrut(15));
        welcomeContent.add(line);
        welcomeContent.add(Box.createVerticalStrut(15));
        welcomeContent.add(lblSubtitle);

        leftPanel.add(welcomeContent);

        // ========================
        // RIGHT PANEL - Login Form
        // ========================
        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setBackground(UIColors.WHITE);

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setOpaque(false);
        formPanel.setBorder(new EmptyBorder(0, 60, 0, 60));

        // Login title
        JLabel lblTitle = new JLabel("LOGIN");
        lblTitle.setFont(com.hrm.util.UIFonts.DISPLAY_TITLE);
        lblTitle.setForeground(UIColors.TEXT_DARK);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Username section
        JLabel lblUsername = new JLabel("Ten dang nhap");
        lblUsername.setFont(com.hrm.util.UIFonts.TEXT_NORMAL);
        lblUsername.setForeground(UIColors.TEXT_LIGHT_GRAY);
        lblUsername.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtUsername.setMaximumSize(new Dimension(280, 40));
        txtUsername.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Password section
        JLabel lblPassword = new JLabel("Mat khau");
        lblPassword.setFont(com.hrm.util.UIFonts.TEXT_NORMAL);
        lblPassword.setForeground(UIColors.TEXT_LIGHT_GRAY);
        lblPassword.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtPassword.setMaximumSize(new Dimension(280, 40));
        txtPassword.setAlignmentX(Component.LEFT_ALIGNMENT);

        chkShowPassword.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Error label
        lblError.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblError.setMaximumSize(new Dimension(280, 30));

        // Login button
        btnLogin.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnLogin.setMaximumSize(new Dimension(280, 45));

        // Demo accounts info - static label
        JLabel lblDemo = new JLabel("<html><div style='text-align:center;'><b>Tai khoan:</b> admin / 123</div></html>");
        lblDemo.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblDemo.setForeground(UIColors.TEXT_LIGHT_GRAY);
        lblDemo.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblDemo.setHorizontalAlignment(SwingConstants.CENTER);
        lblDemo.setMaximumSize(new Dimension(280, 120));

        // Assemble form
        formPanel.add(lblTitle);
        formPanel.add(Box.createVerticalStrut(40));
        formPanel.add(lblUsername);
        formPanel.add(Box.createVerticalStrut(8));
        formPanel.add(txtUsername);
        formPanel.add(Box.createVerticalStrut(25));
        formPanel.add(lblPassword);
        formPanel.add(Box.createVerticalStrut(8));
        formPanel.add(txtPassword);
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(chkShowPassword);
        formPanel.add(Box.createVerticalStrut(15));
        formPanel.add(lblError);
        formPanel.add(Box.createVerticalStrut(15));
        formPanel.add(btnLogin);
        formPanel.add(Box.createVerticalStrut(30));
        formPanel.add(lblDemo);

        rightPanel.add(formPanel);

        // Add panels to main
        mainPanel.add(leftPanel);
        mainPanel.add(rightPanel);

        setContentPane(mainPanel);
    }

    private void setupEvents() {
        btnLogin.addActionListener(this::performLogin);

        // Show/hide password
        chkShowPassword.addActionListener(e -> {
            if (chkShowPassword.isSelected()) {
                txtPassword.setEchoChar((char) 0);
            } else {
                txtPassword.setEchoChar('\u2022');
            }
        });

        // Enter key to login
        KeyAdapter enterKeyAdapter = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    performLogin(null);
                }
            }
        };
        txtUsername.addKeyListener(enterKeyAdapter);
        txtPassword.addKeyListener(enterKeyAdapter);

        // Clear error when typing
        KeyAdapter clearErrorAdapter = new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                clearError();
            }
        };
        txtUsername.addKeyListener(clearErrorAdapter);
        txtPassword.addKeyListener(clearErrorAdapter);

        // Focus on username field
        SwingUtilities.invokeLater(() -> txtUsername.requestFocusInWindow());
    }

    private void performLogin(ActionEvent e) {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (username.isEmpty()) {
            showError("Vui long nhap ten dang nhap!");
            txtUsername.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            showError("Vui long nhap mat khau!");
            txtPassword.requestFocus();
            return;
        }

        // Disable button during login
        btnLogin.setEnabled(false);
        btnLogin.setText("Dang xu ly...");

        // Use SwingWorker for login operation
        SwingWorker<TaiKhoan, Void> worker = new SwingWorker<TaiKhoan, Void>() {
            @Override
            protected TaiKhoan doInBackground() {
                return authService.authenticate(username, password);
            }

            @Override
            protected void done() {
                try {
                    TaiKhoan user = get();
                    if (user != null) {
                        // Success - open MainFrame
                        MainFrame mainFrame = new MainFrame();
                        mainFrame.setVisible(true);
                        dispose();
                    } else {
                        showError("Ten dang nhap hoac mat khau khong dung!");
                        txtPassword.setText("");
                        txtPassword.requestFocus();
                    }
                } catch (Exception ex) {
                    showError("Loi dang nhap: " + ex.getMessage());
                    ex.printStackTrace();
                } finally {
                    btnLogin.setEnabled(true);
                    btnLogin.setText("DANG NHAP");
                }
            }
        };
        worker.execute();
    }

    private void showError(String message) {
        lblError.setText(message);
        lblError.setForeground(UIColors.DANGER_RED);
    }

    private void clearError() {
        lblError.setText(" ");
    }

    private void centerOnScreen() {
        setLocationRelativeTo(null);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            UIManager.put("TitledBorder.titleColor", UIColors.TEXT_DARK);
            UIManager.put("TabbedPane.foreground", UIColors.TEXT_DARK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            LoginFrame frame = new LoginFrame();
            frame.setVisible(true);
        });
    }
}
