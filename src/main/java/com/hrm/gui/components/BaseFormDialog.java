package com.hrm.gui.components;

import com.hrm.util.DialogUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class BaseFormDialog extends JDialog {

    protected BaseFormDialog(Frame parent, String title) {
        super(parent, title, true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    protected BaseFormDialog(Frame parent, String title, boolean modal) {
        super(parent, title, modal);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    protected JPanel createMainPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        return panel;
    }

    protected JPanel createButtonPanel(JButton... buttons) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        for (JButton button : buttons) {
            panel.add(button);
        }
        return panel;
    }

    protected JScrollPane createScrollPane(Component component) {
        return new JScrollPane(component);
    }

    protected void showInfo(String message)    { DialogUtil.showInfo(this, message); }
    protected void showWarning(String message) { DialogUtil.showWarn(this, message); }
    protected void showError(String message)   { DialogUtil.showError(this, message); }
    protected void showSuccess(String message) { DialogUtil.showSuccess(this, message); }
    protected void showError(String message, String title)       { DialogUtil.showError(this, message, title); }
    protected boolean showYesNo(String msg, String title)        { return DialogUtil.showYesNo(this, msg, title); }
    protected boolean showYesNoWarning(String msg, String title) { return DialogUtil.showYesNoWarning(this, msg, title); }
    protected boolean showOkCancel(String msg, String title)     { return DialogUtil.showOkCancel(this, msg, title); }
}
