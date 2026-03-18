package com.hrm.gui.recruitment;

import com.hrm.bus.TuyenDungBUS;
import com.hrm.util.UIColors;
import com.hrm.util.UIFonts;

import javax.swing.*;
import java.awt.*;

public class RecruitmentPanel extends JPanel {

    public RecruitmentPanel() {
        TuyenDungBUS service = TuyenDungBUS.getInstance();
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(UIFonts.TEXT_NORMAL);
        tabs.setBackground(Color.WHITE);
        tabs.addTab("Yêu cầu tuyển dụng", new TabYeuCauPanel(service));
        tabs.addTab("Tin tuyển dụng",     new TabTinPanel(service));
        tabs.addTab("Ứng viên",           new TabUngVienPanel(service));
        add(tabs, BorderLayout.CENTER);
    }
}
