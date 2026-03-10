package com.hrm.gui.payroll;

import com.hrm.repo.ChiTietLuongRepository;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class PayslipDialog extends JDialog {
    private ChiTietLuongRepository repo = new ChiTietLuongRepository();

    public PayslipDialog(Frame parent, String maNV, int maBangLuong, String tenKy, String thucLanh) {
        super(parent, "Chi tiết Phiếu Lương", true);
        setSize(500, 450);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        JPanel header = new JPanel(new GridLayout(2, 1));
        header.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        header.add(new JLabel("Mã Nhân Viên: " + maNV));
        header.add(new JLabel("Kỳ lương: " + tenKy + " | Thực lãnh: " + thucLanh));
        add(header, BorderLayout.NORTH);

        String[] cols = {"Loại", "Tên khoản", "Số tiền (VNĐ)"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        
        // Kéo dữ liệu thật từ bảng THANHPHANLUONG [cite: 171]
        List<String[]> list = repo.getThanhPhanLuong(maNV, maBangLuong);
        for (String[] row : list) {
            String loai = row[0].equals("thu_nhap") ? "Thu nhập" : "Khấu trừ";
            double tien = Double.parseDouble(row[2]);
            model.addRow(new Object[]{ loai, row[1], String.format("%,.0f", tien) });
        }

        JTable table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JButton btnClose = new JButton("Đóng");
        btnClose.addActionListener(e -> dispose());
        JPanel footer = new JPanel();
        footer.add(btnClose);
        add(footer, BorderLayout.SOUTH);
    }
}