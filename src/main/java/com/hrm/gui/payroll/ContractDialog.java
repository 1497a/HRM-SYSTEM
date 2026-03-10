package com.hrm.gui.payroll;

import javax.swing.*;
import java.awt.*;

public class ContractDialog extends JDialog {

    public ContractDialog(JFrame parent) {
        super(parent, "Thêm / Cập nhật Hợp đồng", true);
        setSize(400, 350);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridLayout(6, 2, 10, 15));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Các trường nhập liệu bám sát bảng HOPDONGLAODONG
        formPanel.add(new JLabel("Mã Nhân Viên:"));
        formPanel.add(new JTextField());

        formPanel.add(new JLabel("Số Hợp Đồng:"));
        formPanel.add(new JTextField());

        formPanel.add(new JLabel("Loại Hợp Đồng:"));
        formPanel.add(new JComboBox<>(new String[]{"Thử việc", "Xác định thời hạn", "Không xác định"}));

        formPanel.add(new JLabel("Lương Cơ Sở (VNĐ):"));
        formPanel.add(new JTextField());

        formPanel.add(new JLabel("Ngày Hiệu Lực (YYYY-MM-DD):"));
        formPanel.add(new JTextField());

        add(formPanel, BorderLayout.CENTER);

        // Nút bấm
        JPanel buttonPanel = new JPanel();
        JButton btnSave = new JButton("Lưu");
        JButton btnCancel = new JButton("Hủy");
        
        btnCancel.addActionListener(e -> dispose());
        btnSave.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Lưu hợp đồng thành công (Mock)!");
            dispose();
        });

        buttonPanel.add(btnSave);
        buttonPanel.add(btnCancel);
        add(buttonPanel, BorderLayout.SOUTH);
    }
}
