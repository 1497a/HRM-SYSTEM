package com.hrm.gui.payroll;

import com.hrm.service.HopDongService;

import javax.swing.*;
import java.awt.*;

public class ContractDialog extends JDialog {

    private JTextField txtMaNV, txtSoHD, txtLuong, txtNgay;
    private JComboBox<String> cbLoaiHD;
    private HopDongService hopDongService;
    private Runnable onSuccess;

    public ContractDialog(Window parent, Runnable onSuccess) {
        super(parent, "Thêm Hợp đồng", Dialog.ModalityType.APPLICATION_MODAL);
        this.onSuccess = onSuccess;
        this.hopDongService = new HopDongService();
        
        setSize(400, 350);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridLayout(6, 2, 10, 15));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        formPanel.add(new JLabel("Mã Nhân Viên (VD: NV001):"));
        txtMaNV = new JTextField();
        formPanel.add(txtMaNV);

        formPanel.add(new JLabel("Số Hợp Đồng (VD: HD003):"));
        txtSoHD = new JTextField();
        formPanel.add(txtSoHD);

        formPanel.add(new JLabel("Loại Hợp Đồng:"));
        cbLoaiHD = new JComboBox<>(new String[]{"Thử việc", "Xác định thời hạn", "Không xác định"});
        formPanel.add(cbLoaiHD);

        formPanel.add(new JLabel("Lương Cơ Sở (VNĐ):"));
        txtLuong = new JTextField();
        formPanel.add(txtLuong);

        formPanel.add(new JLabel("Ngày Hiệu Lực (YYYY-MM-DD):"));
        txtNgay = new JTextField();
        formPanel.add(txtNgay);

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

            if (maNV.isEmpty() || soHD.isEmpty() || luong.isEmpty() || ngay.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin!");
                return;
            }

            boolean success = hopDongService.addHopDong(soHD, maNV, loaiHD, luong, ngay);
            
            if (success) {
                JOptionPane.showMessageDialog(this, "Thêm hợp đồng thành công!");
                if (onSuccess != null) {
                    onSuccess.run(); // Cập nhật lại bảng
                }
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi! Kiểm tra lại Mã NV có tồn tại không hoặc Số HĐ đã bị trùng.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi định dạng (Hãy kiểm tra lại số tiền hoặc ngày): \n" + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}