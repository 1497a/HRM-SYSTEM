package com.hrm.gui.notification;

import com.hrm.bus.ThongBaoBUS;
import com.hrm.bus.XacThucBUS;
import com.hrm.model.DataScope;
import com.hrm.model.TaiKhoan;
import com.hrm.util.PermissionCodes;
import com.hrm.util.UIFonts;
import com.hrm.util.SessionContext;
import com.hrm.util.UIColors;

import javax.swing.*;
import java.awt.*;

/**
 * Panel thong bao - container cho 2 tab:
 *   Tab 1: Thong bao cua toi (TabMyNotificationsPanel)
 *   Tab 2: Gui thong bao (TabSendNotificationPanel) - chi hien thi khi co quyen
 */
public class NotificationPanel extends JPanel {

    public NotificationPanel() {
        ThongBaoBUS service  = ThongBaoBUS.getInstance();
        TaiKhoan currentUser = SessionContext.getInstance().getCurrentUser();
        DataScope sendScope  = XacThucBUS.getInstance().getScopeForAction(PermissionCodes.NOTIFICATION_SEND);
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(UIFonts.TEXT_NORMAL);
        tabs.setBackground(Color.WHITE);
        tabs.addTab("Thông báo của tôi", new TabMyNotificationsPanel(service, currentUser));
        if (sendScope != DataScope.NONE) {
            tabs.addTab("Gửi thông báo", new TabSendNotificationPanel(service, currentUser, sendScope));
        }
        add(tabs, BorderLayout.CENTER);
    }
}
