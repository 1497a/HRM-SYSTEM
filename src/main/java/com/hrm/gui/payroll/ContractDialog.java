package com.hrm.gui.payroll;

import com.hrm.service.HopDongService;

import javax.swing.*;
import java.awt.*;

public class ContractDialog extends JDialog {

    private JTextField txtMaNV, txtSoHD, txtLuong, txtNgay;
    private JComboBox<String> cbLoaiHD, cbTrangThai;
    private HopDongService hopDongService;
    private Runnable onSuccess;
    private String maHopDongEdit; // Nếu khác null -> Đang ở chế độ Sửa

    // Constructor dùng cho THÊM MỚI
    public ContractDialog(Window parent, Runnable onSuccess) {
        this(parent, onSuccess, null, "", "", "", "", "");
    }

    // Constructor dùng chung (có nạp sẵn dữ liệu để SỬA)
    public ContractDialog(Window parent, Runnable onSuccess, String maHopDong, String maNV, String loaiHD, String luong, String ngay, String trangThai) {
        super(parent, maHopDong == null ? "Thêm Hợp đồng" : "Cập nhật Hợp đồng", Dialog.ModalityType.APPLICATION_MODAL);
        this.onSuccess = onSuccess;
        this.hopDongService = new HopDongService();
        this.maHopDongEdit = maHopDong;
        
        setSize(420, 420);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridLayout(7, 2, 10, 15));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        formPanel.add(new JLabel("Mã Nhân Viên (VD: NV001):"));
        txtMaNV = new JTextField(maNV);
        formPanel.add(txtMaNV);

        formPanel.add(new JLabel("Số Hợp Đồng (VD: HD003):"));
        txtSoHD = new JTextField();
        formPanel.add(txtSoHD);

        formPanel.add(new JLabel("Loại Hợp Đồng:"));
        cbLoaiHD = new JComboBox<>(new String[]{"Thử việc", "Xác định thời hạn", "Không xác định"});
        if (loaiHD.contains("xac_dinh_thoi_han")) cbLoaiHD.setSelectedItem("Xác định thời hạn");
        else if (loaiHD.contains("khong_xac_dinh")) cbLoaiHD.setSelectedItem("Không xác định");
        formPanel.add(cbLoaiHD);

        formPanel.add(new JLabel("Lương Cơ Sở (VNĐ):"));
        txtLuong = new JTextField(luong.replaceAll("[^\\d.]", "")); // Xóa chữ VNĐ đi để dễ sửa
        formPanel.add(txtLuong);

        formPanel.add(new JLabel("Ngày Hiệu Lực (YYYY-MM-DD):"));
        txtNgay = new JTextField(ngay);
        formPanel.add(txtNgay);

        formPanel.add(new JLabel("Trạng thái:"));
        cbTrangThai = new JComboBox<>(new String[]{"Hiệu lực", "Hết hạn", "Thanh lý", "Hủy"});
        if (trangThai.contains("het_han")) cbTrangThai.setSelectedItem("Hết hạn");
        else if (trangThai.contains("thanh_ly")) cbTrangThai.setSelectedItem("Thanh lý");
        else if (trangThai.contains("huy")) cbTrangThai.setSelectedItem("Hủy");
        formPanel.add(cbTrangThai);

        // Khóa các trường không được phép đổi nếu đang Sửa
        if (maHopDongEdit != null) {
            txtMaNV.setEnabled(false);
            txtSoHD.setText("Không thể sửa");
            txtSoHD.setEnabled(false);
        } else {
            cbTrangThai.setEnabled(false); // Thêm mới thì mặc định là Hiệu lực
        }

        add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        JButton btnSave = new JButton("Lưu");
        JButton btnCancel = new JButton("Hủy");
        
        btnCancel.addActionListener(e -> dispose());
        btnSave.addActionListener(e -> handleSave());

        buttonPanel.add(btnSave);
        buttonPanel.add(btnCancel);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void handleSave() {
        try {
            String maNV = txtMaNV.getText().trim();
            String soHD = txtSoHD.getText().trim();
            String loaiHD = (String) cbLoaiHD.getSelectedItem();
            String luong = txtLuong.getText().trim();
            String ngay = txtNgay.getText().trim();
            String trangThai = (String) cbTrangThai.getSelectedItem();

            if (maNV.isEmpty() || luong.isEmpty() || ngay.isEmpty() || (maHopDongEdit == null && soHD.isEmpty())) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin!");
                return;
            }

            boolean success;
            if (maHopDongEdit == null) {
                // CHẠY HÀM THÊM
                success = hopDongService.addHopDong(soHD, maNV, loaiHD, luong, ngay);
            } else {
                // CHẠY HÀM SỬA
                success = hopDongService.updateHopDong(maHopDongEdit, loaiHD, luong, ngay, trangThai);
            }
            
            if (success) {
                JOptionPane.showMessageDialog(this, maHopDongEdit == null ? "Thêm hợp đồng thành công!" : "Cập nhật thành công!");
                if (onSuccess != null) onSuccess.run();
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi! Kiểm tra lại thông tin.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi định dạng (Hãy kiểm tra lại số tiền hoặc ngày): \n" + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}