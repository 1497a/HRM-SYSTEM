package com.hrm.gui.recruitment;

import com.hrm.bus.TuyenDungBUS;
import com.hrm.util.UIColors;

import javax.swing.*;
import java.awt.*;

public class RecruitmentPanel extends JPanel {

    public RecruitmentPanel() {
        TuyenDungBUS service = TuyenDungBUS.getInstance();
        setLayout(new BorderLayout());
        setBackground(UIColors.LIGHT_GRAY_BG);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(com.hrm.util.UIFonts.TEXT_MEDIUM);
        tabs.setBackground(UIColors.WHITE);
        tabs.addTab("Yêu cầu tuyển dụng", new TabYeuCauPanel(service));
        tabs.addTab("Tin tuyển dụng",     new TabTinPanel(service));
        tabs.addTab("Ứng viên",           new TabUngVienPanel(service));
        add(tabs, BorderLayout.CENTER);
    }
}
