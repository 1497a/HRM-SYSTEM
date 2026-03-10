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
    private JComboBox<KyLuongItem> cbKyLuong; 
    
    private HopDongService hopDongService = new HopDongService();
    private ChiTietLuongService chiTietLuongService = new ChiTietLuongService();

    public ContractAndSalaryPanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JLabel lblTitle = new JLabel("QUẢN LÝ HỢP ĐỒNG & LƯƠNG");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setBorder(new EmptyBorder(15, 20, 15, 20));
        add(lblTitle, BorderLayout.NORTH);

        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        tabbedPane.addTab("Hợp đồng lao động", createContractTab());
        tabbedPane.addTab("Bảng lương tổng hợp", createSalaryTab());

        add(tabbedPane, BorderLayout.CENTER);

        // Load dữ liệu ban đầu
        loadDataContract();
        loadKyLuongToCombo();
    }

    // ================== TAB HỢP ĐỒNG ==================
    private JPanel createContractTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnAdd = new JButton("Thêm Hợp đồng");
        JButton btnEdit = new JButton("Sửa");
        JButton btnDelete = new JButton("Xóa");
        JButton btnRefresh = new JButton("Làm mới");

        toolbar.add(btnAdd);
        toolbar.add(btnEdit);
        toolbar.add(btnDelete);
        toolbar.add(btnRefresh);

        String[] cols = {"Mã HĐ", "Mã NV", "Loại HĐ", "Lương cơ sở", "Ngày hiệu lực", "Trạng thái"};
        DefaultTableModel model = new DefaultTableModel(cols, 0); 
        tableContract = new JTable(model);
        tableContract.setRowHeight(30);
        
        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(new JScrollPane(tableContract), BorderLayout.CENTER);

        btnRefresh.addActionListener(e -> loadDataContract());

        btnAdd.addActionListener(e -> {
            ContractDialog dialog = new ContractDialog(SwingUtilities.getWindowAncestor(this), this::loadDataContract);
            dialog.setVisible(true);
        });

        btnEdit.addActionListener(e -> {
            int row = tableContract.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Vui lòng click chọn 1 hợp đồng để sửa!");
                return;
            }
            String maHD = tableContract.getValueAt(row, 0).toString();
            String maNV = tableContract.getValueAt(row, 1).toString();
            String loaiHD = tableContract.getValueAt(row, 2).toString();
            String luongStr = tableContract.getValueAt(row, 3).toString();
            String ngayHL = tableContract.getValueAt(row, 4).toString();
            String trangThai = tableContract.getValueAt(row, 5).toString();

            ContractDialog dialog = new ContractDialog(SwingUtilities.getWindowAncestor(this), this::loadDataContract, maHD, maNV, loaiHD, luongStr, ngayHL, trangThai);
            dialog.setVisible(true);
        });

        btnDelete.addActionListener(e -> {
            int row = tableContract.getSelectedRow();
            if (row == -1) return;
            String maHD = tableContract.getValueAt(row, 0).toString();
            if (JOptionPane.showConfirmDialog(this, "Xóa vĩnh viễn hợp đồng mã " + maHD + "?", "Xác nhận", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                if (hopDongService.deleteHopDong(maHD)) {
                    JOptionPane.showMessageDialog(this, "Xóa thành công!");
                    loadDataContract();
                }
            }
        });
        return panel;
    }

    public void loadDataContract() {
        DefaultTableModel model = (DefaultTableModel) tableContract.getModel();
        model.setRowCount(0); 
        try {
            List<HopDong> dsHopDong = hopDongService.getAllHopDong();
            for (HopDong hd : dsHopDong) {
                model.addRow(new Object[]{ hd.getMaHopDong(), hd.getMaNV(), hd.getLoaiHopDong(), String.format("%,.0f VNĐ", hd.getLuongCoSo()), hd.getNgayHieuLuc(), hd.getTrangThai() });
            }
        } catch (Exception e) { e.printStackTrace(); }
    }


    // ================== TAB BẢNG LƯƠNG ==================
    private JPanel createSalaryTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        
        toolbar.add(new JLabel("Chọn kỳ lương: "));
        cbKyLuong = new JComboBox<>();
        toolbar.add(cbKyLuong);

        JButton btnTinhLuong = new JButton("Tính lương");
        JButton btnKhoaBangLuong = new JButton("Khóa bảng lương");
        JButton btnViewDetail = new JButton("Xem phiếu chi tiết");
        
        toolbar.add(btnTinhLuong);
        toolbar.add(btnKhoaBangLuong);
        toolbar.add(btnViewDetail);

        String[] cols = {"Mã NV", "Lương cơ sở", "Tổng thu nhập", "Tổng khấu trừ", "Thực lãnh", "Trạng thái"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        tableSalary = new JTable(model);
        tableSalary.setRowHeight(30);

        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(new JScrollPane(tableSalary), BorderLayout.CENTER);

        // Đổi kỳ lương -> Load data + Bật/tắt nút
        cbKyLuong.addActionListener(e -> {
            KyLuongItem selected = (KyLuongItem) cbKyLuong.getSelectedItem();
            if (selected != null) {
                boolean isKhoa = "da_khoa".equals(selected.getTrangThai());
                btnTinhLuong.setEnabled(!isKhoa);
                btnKhoaBangLuong.setEnabled(!isKhoa);
                loadDataSalary();
            }
        });

        // Nút Tính lương
        btnTinhLuong.addActionListener(e -> {
            KyLuongItem selected = (KyLuongItem) cbKyLuong.getSelectedItem();
            if (selected == null) return;
            
            if (JOptionPane.showConfirmDialog(this, "Tính lại lương cho " + selected.getTenBangLuong() + "?", "Xác nhận", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                if (chiTietLuongService.tinhLuong(selected.getMaBangLuong())) {
                    JOptionPane.showMessageDialog(this, "Đã tính lương xong!");
                    loadDataSalary();
                }
            }
        });

        // Nút Khóa
        btnKhoaBangLuong.addActionListener(e -> {
            KyLuongItem selected = (KyLuongItem) cbKyLuong.getSelectedItem();
            if (selected == null) return;
            if (JOptionPane.showConfirmDialog(this, "Khóa bảng lương " + selected.getTenBangLuong() + "?", "Xác nhận", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                if (chiTietLuongService.khoaBangLuong(selected.getMaBangLuong())) {
                    JOptionPane.showMessageDialog(this, "Đã khóa bảng lương!");
                    loadKyLuongToCombo();
                }
            }
        });

        // NÚT XEM CHI TIẾT (Đã sửa để khớp dữ liệu thật)
        btnViewDetail.addActionListener(e -> {
            int selectedRow = tableSalary.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn một nhân viên!");
                return;
            }
            
            String maNV = tableSalary.getValueAt(selectedRow, 0).toString();
            String thucLanh = tableSalary.getValueAt(selectedRow, 4).toString();
            KyLuongItem selectedKy = (KyLuongItem) cbKyLuong.getSelectedItem();
            
            if (selectedKy != null) {
                PayslipDialog dialog = new PayslipDialog(
                    (Frame) SwingUtilities.getWindowAncestor(this), 
                    maNV, 
                    selectedKy.getMaBangLuong(), 
                    selectedKy.getTenBangLuong(), 
                    thucLanh
                );
                dialog.setVisible(true);
            }
        });

        return panel;
    }

    private void loadKyLuongToCombo() {
        int selectedIndex = cbKyLuong.getSelectedIndex(); 
        cbKyLuong.removeAllItems();
        try {
            List<String[]> ds = chiTietLuongService.getDanhSachKyLuong();
            for (String[] row : ds) {
                cbKyLuong.addItem(new KyLuongItem(Integer.parseInt(row[0]), row[1], row[2]));
            }
            if (selectedIndex >= 0 && selectedIndex < cbKyLuong.getItemCount()) {
                cbKyLuong.setSelectedIndex(selectedIndex);
            }
        } catch(Exception e) { e.printStackTrace(); }
    }

    public void loadDataSalary() {
        DefaultTableModel model = (DefaultTableModel) tableSalary.getModel();
        model.setRowCount(0); 
        KyLuongItem selectedKy = (KyLuongItem) cbKyLuong.getSelectedItem();
        if (selectedKy == null) return; 
        try {
            List<ChiTietLuong> dsLuong = chiTietLuongService.getSalaryDetailsByBangLuong(selectedKy.getMaBangLuong());
            for (ChiTietLuong c : dsLuong) {
                model.addRow(new Object[]{
                    c.getTenNV(),
                    String.format("%,.0f VNĐ", c.getLuongCoBan()),
                    String.format("%,.0f VNĐ", c.getTongLuong()),
                    String.format("%,.0f VNĐ", c.getTongKhauTru()),
                    String.format("%,.0f VNĐ", c.getLuongThucNhan()),
                    c.getTrangThai() != null ? c.getTrangThai().getDisplayName() : "Chưa rõ"
                });
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    // Hàm quan trọng để Menu không bị lỗi
    public void selectTab(int index) {
        if (tabbedPane != null && index >= 0 && index < tabbedPane.getTabCount()) {
            tabbedPane.setSelectedIndex(index);
        }
    }

    class KyLuongItem {
        private int maBangLuong;
        private String tenBangLuong;
        private String trangThai;

        public KyLuongItem(int maBangLuong, String tenBangLuong, String trangThai) {
            this.maBangLuong = maBangLuong;
            this.tenBangLuong = tenBangLuong;
            this.trangThai = trangThai;
        }

        public int getMaBangLuong() { return maBangLuong; }
        public String getTenBangLuong() { return tenBangLuong; }
        public String getTrangThai() { return trangThai; }
        
        @Override
        public String toString() {
            return tenBangLuong + ("da_khoa".equals(trangThai) ? " (Đã khóa)" : ""); 
        }
    }
}