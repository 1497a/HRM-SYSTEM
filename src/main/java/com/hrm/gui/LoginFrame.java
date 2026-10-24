package com.hrm.gui;

import com.hrm.bus.XacThucBUS;
import com.hrm.gui.components.PurpleButton;
import com.hrm.model.TaiKhoan;
import com.hrm.util.UIColors;
import com.hrm.util.UIFonts;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * LoginFrame - Split-panel login screen with purple theme.
 */
public class LoginFrame extends JFrame {
    private static final String APP_TITLE = "HRM System - Đăng nhập";
    private static final String LOGIN_TEXT = "ĐĂNG NHẬP";
    private static final String PROCESSING_TEXT = "Đang xử lý...";
    private static final Dimension FRAME_SIZE = new Dimension(900, 550);
    private static final Dimension FIELD_SIZE = new Dimension(280, 40);
    private static final Dimension BUTTON_SIZE = new Dimension(280, 45);
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
        setTitle(APP_TITLE);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setSize(FRAME_SIZE);
        txtUsername = createTextField();
        txtPassword = createPasswordField();
        btnLogin = new PurpleButton(LOGIN_TEXT);
        btnLogin.setPreferredSize(BUTTON_SIZE);
        chkShowPassword = new JCheckBox("Hiển thị mật khẩu");
        chkShowPassword.setFont(UIFonts.BOLD_SMALL);
        chkShowPassword.setForeground(Color.GRAY);
        chkShowPassword.setOpaque(false);
        chkShowPassword.setFocusPainted(false);
        lblError = new JLabel(" ");
        lblError.setForeground(UIColors.DANGER_RED);
        lblError.setFont(UIFonts.BOLD_SMALL);
        lblError.setHorizontalAlignment(SwingConstants.CENTER);
    }

    private JTextField createTextField() {
        JTextField field = new JTextField(20);
        configureInputField(field);
        return field;
    }

    private JPasswordField createPasswordField() {
        JPasswordField field = new JPasswordField(20);
        configureInputField(field);
        field.setEchoChar('•');
        return field;
    }

    private void configureInputField(JTextField field) {
        field.setFont(UIFonts.TEXT_NORMAL);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(8, 5, 8, 5)));
        field.setPreferredSize(FIELD_SIZE);
        field.setMaximumSize(FIELD_SIZE);
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    private void setupLayout() {
        JPanel mainPanel = new JPanel(new GridLayout(1, 2));
        mainPanel.setPreferredSize(FRAME_SIZE);
        mainPanel.add(createWelcomePanel());
        mainPanel.add(createLoginPanel());
        setContentPane(mainPanel);
    }

    private JPanel createWelcomePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(UIColors.PRIMARY_PURPLE);
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(0, 50, 0, 50));
        JLabel lblWelcome = new JLabel("CHÀO MỪNG");
        lblWelcome.setFont(new Font("Segoe UI", Font.BOLD, 42));
        lblWelcome.setForeground(Color.WHITE);
        lblWelcome.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel lblSubtitle = new JLabel("Đăng nhập vào hệ thống quản lý nhân sự");
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        lblSubtitle.setForeground(new Color(255, 255, 255, 200));
        lblSubtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPanel line = new JPanel();
        line.setBackground(new Color(255, 255, 255, 100));
        line.setPreferredSize(new Dimension(60, 4));
        line.setMaximumSize(new Dimension(60, 4));
        line.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(lblWelcome);
        content.add(Box.createVerticalStrut(15));
        content.add(line);
        content.add(Box.createVerticalStrut(15));
        content.add(lblSubtitle);
        panel.add(content);
        return panel;
    }

    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.add(createFormPanel());
        return panel;
    }

    private JPanel createFormPanel() {
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setOpaque(false);
        formPanel.setBorder(new EmptyBorder(0, 60, 0, 60));
        JLabel lblTitle = new JLabel(LOGIN_TEXT);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 32));
        lblTitle.setForeground(UIColors.TEXT_DARK);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel lblUsername = createFieldLabel("Tên đăng nhập");
        JLabel lblPassword = createFieldLabel("Mật khẩu");
        chkShowPassword.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblError.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblError.setMaximumSize(new Dimension(280, 30));
        btnLogin.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnLogin.setMaximumSize(BUTTON_SIZE);
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
        return formPanel;
    }

    private JLabel createFieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(UIFonts.TEXT_NORMAL);
        label.setForeground(Color.GRAY);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private void setupEvents() {
        btnLogin.addActionListener(this::performLogin);
        chkShowPassword.addActionListener(e -> togglePasswordVisibility());
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
        KeyAdapter clearErrorAdapter = new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                clearError();
            }
        };
        txtUsername.addKeyListener(clearErrorAdapter);
        txtPassword.addKeyListener(clearErrorAdapter);
        SwingUtilities.invokeLater(() -> txtUsername.requestFocusInWindow());
    }

    private void togglePasswordVisibility() {
        txtPassword.setEchoChar(chkShowPassword.isSelected() ? (char) 0 : '•');
    }

    private void performLogin(ActionEvent e) {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());
        if (username.isEmpty()) {
            showError("Vui lòng nhập tên đăng nhập.");
            txtUsername.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            showError("Vui lòng nhập mật khẩu.");
            txtPassword.requestFocus();
            return;
        }
        setLoadingState(true);
        SwingWorker<TaiKhoan, Void> worker = new SwingWorker<>() {
            @Override
            protected TaiKhoan doInBackground() {
                return authService.authenticate(username, password);
            }
            @Override
            protected void done() {
                try {
                    TaiKhoan user = get();
                    if (user != null) {
                        MainFrame mainFrame = new MainFrame();
                        mainFrame.setVisible(true);
                        dispose();
                    } else {
                        showError("Tên đăng nhập hoặc mật khẩu không đúng.");
                        txtPassword.setText("");
                        txtPassword.requestFocus();
                    }
                } catch (Exception ex) {
                    showError("Lỗi đăng nhập: " + ex.getMessage());
                    ex.printStackTrace();
                } finally {
                    setLoadingState(false);
                }
            }
        };
        worker.execute();
    }

    private void setLoadingState(boolean loading) {
        btnLogin.setEnabled(!loading);
        btnLogin.setText(loading ? PROCESSING_TEXT : LOGIN_TEXT);
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
