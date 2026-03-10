package com.hrm.gui.payroll;

import com.hrm.model.HopDong;
import com.hrm.service.HopDongService;
import com.hrm.model.ChiTietLuong;
import com.hrm.service.ChiTietLuongService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ContractAndSalaryPanel extends JPanel {

    private JTable tableContract, tableSalary;
    private JTabbedPane tabbedPane;
    
    // Khai báo 2 Service để kéo dữ liệu từ Database lên
    private HopDongService hopDongService = new HopDongService();
    private ChiTietLuongService chiTietLuongService = new ChiTietLuongService();

    public ContractAndSalaryPanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // Tiêu đề
        JLabel lblTitle = new JLabel("QUẢN LÝ HỢP ĐỒNG & LƯƠNG");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setBorder(new EmptyBorder(15, 20, 15, 20));
        add(lblTitle, BorderLayout.NORTH);

        // TabbedPane
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tabbedPane.addTab("Hợp đồng lao động", createContractTab());
        tabbedPane.addTab("Bảng lương tổng hợp", createSalaryTab());

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createContractTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnAdd = new JButton("Thêm Hợp đồng");
        toolbar.add(btnAdd);

        // Các cột của bảng Hợp đồng
        String[] cols = {"Mã HĐ", "Mã NV", "Loại HĐ", "Lương cơ sở", "Ngày hiệu lực", "Trạng thái"};
        DefaultTableModel model = new DefaultTableModel(cols, 0); 
        
        // --- GỌI DỮ LIỆU THẬT TỪ DATABASE ---
        try {
            List<HopDong> dsHopDong = hopDongService.getAllHopDong();
            for (HopDong hd : dsHopDong) {
                model.addRow(new Object[]{
                    hd.getMaHopDong(),
                    hd.getMaNV(),
                    hd.getLoaiHopDong(),
                    String.format("%,.0f VNĐ", hd.getLuongCoSo()), // Định dạng có dấu phẩy
                    hd.getNgayHieuLuc(),
                    hd.getTrangThai()
                });
            }
        } catch (Exception e) {
            System.out.println("Lỗi load dữ liệu Hợp Đồng: " + e.getMessage());
        }

        tableContract = new JTable(model);
        tableContract.setRowHeight(30);
        
        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(new JScrollPane(tableContract), BorderLayout.CENTER);

        btnAdd.addActionListener(e -> {
            ContractDialog dialog = new ContractDialog((JFrame) SwingUtilities.getWindowAncestor(this));
            dialog.setVisible(true);
        });

        return panel;
    }

    private JPanel createSalaryTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnViewDetail = new JButton("Xem phiếu lương chi tiết");
        toolbar.add(btnViewDetail);

        // Các cột của bảng Lương
        String[] cols = {"Mã NV", "Lương cơ sở", "Tổng thu nhập", "Tổng khấu trừ", "Thực lãnh", "Trạng thái"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);

        // --- LẤY DỮ LIỆU THẬT TỪ DATABASE ---
        try {
            List<ChiTietLuong> dsLuong = chiTietLuongService.getAllSalaryDetails();
            for (ChiTietLuong c : dsLuong) {
                model.addRow(new Object[]{
                    c.getTenNV(), // Lấy mã nhân viên (như NV001)
                    String.format("%,.0f VNĐ", c.getLuongCoBan()),
                    String.format("%,.0f VNĐ", c.getTongLuong()),
                    String.format("%,.0f VNĐ", c.getTongKhauTru()),
                    String.format("%,.0f VNĐ", c.getLuongThucNhan()),
                    c.getTrangThai() != null ? c.getTrangThai().getDisplayName() : "Chưa rõ" // Lấy tên trạng thái từ Enum
                });
            }
        } catch (Exception e) {
            System.out.println("Lỗi load dữ liệu lương: " + e.getMessage());
        }

        tableSalary = new JTable(model);
        tableSalary.setRowHeight(30);

        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(new JScrollPane(tableSalary), BorderLayout.CENTER);

        btnViewDetail.addActionListener(e -> {
            int selectedRow = tableSalary.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn một nhân viên để xem!");
                return;
            }
            PayslipDialog dialog = new PayslipDialog((JFrame) SwingUtilities.getWindowAncestor(this));
            dialog.setVisible(true);
        });

        return panel;
    }

    // Hàm hỗ trợ chọn tab từ các nút bấm ở MainFrame
    public void selectTab(int index) {
        if (tabbedPane != null && index >= 0 && index < tabbedPane.getTabCount()) {
            tabbedPane.setSelectedIndex(index);
        }
    }
}