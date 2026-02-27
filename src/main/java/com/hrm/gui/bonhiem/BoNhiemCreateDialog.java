package com.hrm.gui.bonhiem;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import com.hrm.model.BoNhiem;
import com.hrm.service.BoNhiemService;
import com.hrm.service.BoNhiemService.ServiceResult;
import com.hrm.util.UIColors;
import com.hrm.util.UIHelper;


public class BoNhiemCreateDialog extends JDialog {

    private final BoNhiemService service = new BoNhiemService();

    private JTextField txtMaNV, txtMaPhongBan, txtMaChucVu, txtTyLeHuongLuong, txtLyDo;
    private JComboBox<String> cbLoaiBoNhiem;
    private JTextField txtTuNgay, txtDenNgay;
    private JButton btnLuu, btnHuy;

    private boolean saved = false;

    public BoNhiemCreateDialog(Frame parent) {
        super(parent, "Tạo yêu cầu bổ nhiệm mới", true);
        setSize(550, 600);
        setLocationRelativeTo(parent);

        initComponents();
    }

    private void initComponents() {
        JPanel main = new JPanel(new GridBagLayout());
        main.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        main.setBackground(UIColors.LIGHT_GRAY_BG);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int y = 0;

        gbc.gridx = 0; gbc.gridy = y;
        main.add(new JLabel("Mã nhân viên:"), gbc);
        txtMaNV = new JTextField(20);
        gbc.gridx = 1; gbc.weightx = 1.0;
        main.add(txtMaNV, gbc);
        y++;

        gbc.gridx = 0; gbc.gridy = y; gbc.weightx = 0;
        main.add(new JLabel("Mã phòng ban:"), gbc);
        txtMaPhongBan = new JTextField(20);
        gbc.gridx = 1; gbc.weightx = 1.0;
        main.add(txtMaPhongBan, gbc);
        y++;

        gbc.gridx = 0; gbc.gridy = y; gbc.weightx = 0;
        main.add(new JLabel("Mã chức vụ:"), gbc);
        txtMaChucVu = new JTextField(20);
        gbc.gridx = 1; gbc.weightx = 1.0;
        main.add(txtMaChucVu, gbc);
        y++;

        gbc.gridx = 0; gbc.gridy = y; gbc.weightx = 0;
        main.add(new JLabel("Loại bổ nhiệm:"), gbc);
        cbLoaiBoNhiem = new JComboBox<>(new String[]{"Chính", "Kiêm nhiệm"});
        gbc.gridx = 1; gbc.weightx = 1.0;
        main.add(cbLoaiBoNhiem, gbc);
        y++;

        gbc.gridx = 0; gbc.gridy = y; gbc.weightx = 0;
        main.add(new JLabel("Tỷ lệ hưởng lương (%):"), gbc);
        txtTyLeHuongLuong = new JTextField("100", 20);
        gbc.gridx = 1; gbc.weightx = 1.0;
        main.add(txtTyLeHuongLuong, gbc);
        y++;

        gbc.gridx = 0; gbc.gridy = y; gbc.weightx = 0;
        main.add(new JLabel("Từ ngày (dd/MM/yyyy):"), gbc);
        txtTuNgay = new JTextField(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), 20);
        gbc.gridx = 1; gbc.weightx = 1.0;
        main.add(txtTuNgay, gbc);
        y++;

        gbc.gridx = 0; gbc.gridy = y; gbc.weightx = 0;
        main.add(new JLabel("Đến ngày (dd/MM/yyyy - để trống nếu vô thời hạn):"), gbc);
        txtDenNgay = new JTextField(20);
        gbc.gridx = 1; gbc.weightx = 1.0;
        main.add(txtDenNgay, gbc);
        y++;

        gbc.gridx = 0; gbc.gridy = y; gbc.weightx = 0;
        main.add(new JLabel("Lý do:"), gbc);
        txtLyDo = new JTextField(20);
        gbc.gridx = 1; gbc.weightx = 1.0;
        main.add(txtLyDo, gbc);
        y++;

        // Note hướng dẫn
        JPanel notePanel = new JPanel();
        notePanel.setBackground(new Color(255, 255, 200));
        notePanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        JLabel lblNote = new JLabel("<html><b>Lưu ý:</b> Ngày đến để trống = vô thời hạn. Tỷ lệ từ 0–100%.<br>Mã NV, phòng ban, chức vụ phải tồn tại trong hệ thống.</html>");
        notePanel.add(lblNote);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBackground(UIColors.LIGHT_GRAY_BG);

        btnLuu = UIHelper.createSuccessButton("Lưu yêu cầu");
        btnHuy = UIHelper.createDangerButton("Hủy");

        btnLuu.addActionListener(e -> luuYeuCau());
        btnHuy.addActionListener(e -> dispose());

        btnPanel.add(btnLuu);
        btnPanel.add(btnHuy);

        add(notePanel, BorderLayout.NORTH);
        add(main, BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);
    }

    private void luuYeuCau() {
    try {
        int maNV = Integer.parseInt(txtMaNV.getText().trim());
        if (maNV <= 0) {
            throw new IllegalArgumentException("Mã nhân viên phải lớn hơn 0");
        }

        String maPhongBan = txtMaPhongBan.getText().trim();
        if (maPhongBan.isEmpty()) {
            throw new IllegalArgumentException("Mã phòng ban không được để trống");
        }

        String maChucVu = txtMaChucVu.getText().trim();
        if (maChucVu.isEmpty()) {
            throw new IllegalArgumentException("Mã chức vụ không được để trống");
        }

        double tyLe = Double.parseDouble(txtTyLeHuongLuong.getText().trim());
        if (tyLe < 0 || tyLe > 100) {
            throw new IllegalArgumentException("Tỷ lệ hưởng lương phải từ 0 đến 100%");
        }

        LocalDate tuNgay = LocalDate.parse(txtTuNgay.getText().trim(), DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        LocalDate denNgay = null;
        String denNgayText = txtDenNgay.getText().trim();
        if (!denNgayText.isEmpty()) {
            denNgay = LocalDate.parse(denNgayText, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            if (denNgay.isBefore(tuNgay)) {
                throw new IllegalArgumentException("Ngày kết thúc phải sau hoặc bằng ngày bắt đầu");
            }
        }

        String lyDo = txtLyDo.getText().trim();

        BoNhiem.LoaiBoNhiem loai = cbLoaiBoNhiem.getSelectedItem().equals("Chính") 
            ? BoNhiem.LoaiBoNhiem.CHINH 
            : BoNhiem.LoaiBoNhiem.KIEM_NHIEM;

        BoNhiem bn = new BoNhiem(maNV, maPhongBan, maChucVu, loai, tyLe, null, tuNgay, denNgay, lyDo);

        ServiceResult<BoNhiem> res = service.taoYeuCau(bn);

        if (res.isSuccess()) {
            JOptionPane.showMessageDialog(this, 
                res.getMessage() != null ? res.getMessage() : "Tạo yêu cầu bổ nhiệm thành công!", 
                "Thành công", 
                JOptionPane.INFORMATION_MESSAGE);
            saved = true;
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, 
                res.getMessage() != null ? res.getMessage() : "Không thể tạo yêu cầu bổ nhiệm!", 
                "Lỗi", 
                JOptionPane.ERROR_MESSAGE);
        }
    } catch (NumberFormatException ex) {
        JOptionPane.showMessageDialog(this, 
            "Dữ liệu số không hợp lệ! Vui lòng kiểm tra mã NV hoặc tỷ lệ.", 
            "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
    } catch (DateTimeParseException ex) {
        JOptionPane.showMessageDialog(this, 
            "Định dạng ngày không đúng! Vui lòng nhập theo dạng dd/MM/yyyy.", 
            "Lỗi định dạng ngày", JOptionPane.ERROR_MESSAGE);
    } catch (IllegalArgumentException ex) {
        JOptionPane.showMessageDialog(this, 
            ex.getMessage(), 
            "Lỗi nhập liệu", JOptionPane.WARNING_MESSAGE);
    } catch (Exception ex) {
        JOptionPane.showMessageDialog(this, 
            "Có lỗi xảy ra: " + ex.getMessage(), 
            "Lỗi hệ thống", JOptionPane.ERROR_MESSAGE);
        ex.printStackTrace();  
    }
}

    public boolean isSaved() {
        return saved;
    }
}
