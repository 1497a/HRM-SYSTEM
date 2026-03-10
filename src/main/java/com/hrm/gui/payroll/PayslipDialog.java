package com.hrm.gui.payroll;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class PayslipDialog extends JDialog {

    public PayslipDialog(JFrame parent) {
        super(parent, "Chi tiết Phiếu Lương", true);
        setSize(500, 400);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        // Thông tin chung
        JPanel infoPanel = new JPanel(new GridLayout(2, 1));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        infoPanel.add(new JLabel("Mã NV: NV001 | Họ tên: Nguyễn Văn A (Mock)"));
        infoPanel.add(new JLabel("Kỳ lương: Tháng 3 / 2026 | Lương thực lãnh: 15,500,000 VNĐ"));
        add(infoPanel, BorderLayout.NORTH);

        // Bảng chi tiết các khoản
        String[] cols = {"Loại", "Tên khoản", "Số tiền (VNĐ)"};
        Object[][] data = {
            {"Thu nhập", "Lương cơ bản", "15,000,000"},
            {"Thu nhập", "Phụ cấp trách nhiệm", "1,500,000"},
            {"Thu nhập", "Tiền OT", "500,000"},
            {"Khấu trừ", "Bảo hiểm XH (8%)", "- 1,200,000"},
            {"Khấu trừ", "Thuế TNCN", "- 300,000"}
        };
        JTable tableDetail = new JTable(new DefaultTableModel(data, cols));
        tableDetail.setRowHeight(25);
        
        add(new JScrollPane(tableDetail), BorderLayout.CENTER);

        // Nút đóng
        JPanel bottomPanel = new JPanel();
        JButton btnClose = new JButton("Đóng");
        btnClose.addActionListener(e -> dispose());
        bottomPanel.add(btnClose);
        add(bottomPanel, BorderLayout.SOUTH);
    }
}