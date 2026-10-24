package com.hrm;

import com.hrm.gui.LoginFrame;
import javax.swing.*;

public class MainApp {

    public static void main(String[] args) {
        // Set Look and Feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            UIManager.put("TitledBorder.titleColor", com.hrm.util.UIColors.TEXT_DARK);
            UIManager.put("TabbedPane.foreground", com.hrm.util.UIColors.TEXT_DARK);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Open Login Frame on EDT
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}
