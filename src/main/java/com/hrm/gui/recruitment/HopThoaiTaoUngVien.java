package com.hrm.gui.recruitment;

import com.hrm.bus.TuyenDungBUS;
import com.hrm.bus.KetQua;
import com.hrm.model.TinTuyenDung;
import com.hrm.model.UngVien;
import com.hrm.util.DialogUtil;
import com.hrm.util.UIColors;
import com.hrm.util.UIFonts;
import com.hrm.util.UIHelper;
import com.hrm.util.ValidationUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Dialog nhap day du thong tin ung vien.
 */
public class HopThoaiTaoUngVien extends JDialog {

    private final TuyenDungBUS tuyenDungBUS;
    private boolean thanhCong = false;
    // Fields
    private JComboBox<TinTuyenDung> cboTin;
    private JTextField txtHoTen;
    private JTextField txtEmail;
    private JTextField txtDienThoai;
    private JTextField txtNgaySinh;       // dd/MM/yyyy
    private JComboBox<String> cboGioiTinh;
    private JTextField txtDiaChi;
    private JComboBox<String> cboTrinhDo;
    private JTextArea txtKinhNghiem;
    private JTextField txtNguonUngTuyen;
    private JTextArea txtNhanXet;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    public HopThoaiTaoUngVien(Window owner, TuyenDungBUS tuyenDungBUS) {
        super(owner, "Tạo ứng viên mới", ModalityType.APPLICATION_MODAL);
        this.tuyenDungBUS = tuyenDungBUS;
        initUI();
        pack();
        setMinimumSize(new Dimension(520, 600));
        setLocationRelativeTo(owner);
    }

    private void initUI() {
        JPanel main = new JPanel(new BorderLayout(8, 8));
        main.setBorder(new EmptyBorder(16, 16, 8, 16));
        main.setBackground(Color.WHITE);
        main.add(buildForm(), BorderLayout.CENTER);
        main.add(buildButtons(), BorderLayout.SOUTH);
        setContentPane(main);
    }

    private JPanel buildForm() {
        // Load active tin tuyen dung
        List<TinTuyenDung> dsTin = tuyenDungBUS.getAllTinTuyenDung().stream()
                .filter(t -> "dang_tuyen".equals(t.getTrangThai()))
                .collect(java.util.stream.Collectors.toList());
        cboTin = new JComboBox<>();
        TinTuyenDung placeholder = new TinTuyenDung();
        placeholder.setTieuDe("-- Chọn tin tuyển dụng --");
        cboTin.addItem(placeholder);
        for (TinTuyenDung t : dsTin) cboTin.addItem(t);
        cboTin.setRenderer((list, value, index, sel, focus) ->
                new JLabel(value != null ? value.getTieuDe() : ""));
        txtHoTen        = field(25);
        txtEmail        = field(25);
        txtDienThoai    = field(15);
        txtNgaySinh     = field(12);
        txtNgaySinh.setToolTipText("Định dạng: dd/MM/yyyy");
        cboGioiTinh = new JComboBox<>(new String[]{"nam", "nữ", "khác"});
        cboGioiTinh.setFont(com.hrm.util.UIFonts.TEXT_NORMAL);
        txtDiaChi = field(30);
        cboTrinhDo = new JComboBox<>(new String[]{
            "Trung học", "Trung cấp", "Cao đẳng", "Đại học", "Thạc sĩ", "Tiến sĩ", "Khác"
        });
        cboTrinhDo.setFont(com.hrm.util.UIFonts.TEXT_NORMAL);
        txtKinhNghiem   = area(4);
        txtNguonUngTuyen = field(20);
        txtNhanXet      = area(3);
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.WHITE);
        GridBagConstraints lbl = labelGBC();
        GridBagConstraints fld = fieldGBC();
        int row = 0;
        addRow(p, row++, "Tin tuyển dụng (*):", cboTin,              lbl, fld);
        addRow(p, row++, "Họ tên (*):",         txtHoTen,            lbl, fld);
        addRow(p, row++, "Email:",               txtEmail,            lbl, fld);
        addRow(p, row++, "Điện thoại:",          txtDienThoai,        lbl, fld);
        addRow(p, row++, "Ngày sinh (dd/MM/yyyy):", txtNgaySinh,      lbl, fld);
        addRow(p, row++, "Giới tính:",           cboGioiTinh,         lbl, fld);
        addRow(p, row++, "Địa chỉ:",             txtDiaChi,           lbl, fld);
        addRow(p, row++, "Trình độ học vấn:",    cboTrinhDo,          lbl, fld);
        addRow(p, row++, "Kinh nghiệm:",         new JScrollPane(txtKinhNghiem), lbl, fld);
        addRow(p, row++, "Nguồn ứng tuyển:",     txtNguonUngTuyen,    lbl, fld);
        addRow(p, row++, "Nhận xét ban đầu:",    new JScrollPane(txtNhanXet),    lbl, fld);
        return p;
    }

    private JPanel buildButtons() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        panel.setBackground(Color.WHITE);
        JButton btnLuu   = UIHelper.createSuccessButton("Lưu");
        JButton btnHuy   = UIHelper.createDefaultButton("Hủy");
        btnLuu.addActionListener(e -> luu());
        btnHuy.addActionListener(e -> dispose());
        panel.add(btnHuy);
        panel.add(btnLuu);
        return panel;
    }

    private void luu() {
        String hoTen = txtHoTen.getText().trim();
        if (hoTen.isEmpty()) {
            DialogUtil.showWarn(this, "Vui lòng nhập họ tên.");
            txtHoTen.requestFocus();
            return;
        }
        TinTuyenDung tin = (TinTuyenDung) cboTin.getSelectedItem();
        if (tin == null || tin.getMaTin() == 0) {
            DialogUtil.showWarn(this, "Vui lòng chọn tin tuyển dụng.");
            cboTin.requestFocus();
            return;
        }
        LocalDate ngaySinh = null;
        String nsStr = txtNgaySinh.getText().trim();
        if (!nsStr.isEmpty()) {
            try {
                ngaySinh = LocalDate.parse(nsStr, DATE_FMT);
            } catch (DateTimeParseException ex) {
                DialogUtil.showWarn(this, "Ngày sinh không hợp lệ. Định dạng: dd/MM/yyyy");
                txtNgaySinh.requestFocus();
                return;
            }
            String dobErr = ValidationUtils.validateBirthDate(ngaySinh);
            if (dobErr != null) {
                DialogUtil.showWarn(this, dobErr);
                txtNgaySinh.requestFocus();
                return;
            }
        }
        // Validate email and phone
        String emailErr = ValidationUtils.validateEmail(txtEmail.getText().trim());
        if (emailErr != null) {
            DialogUtil.showWarn(this, emailErr);
            txtEmail.requestFocus();
            return;
        }
        String phoneErr = ValidationUtils.validatePhone(txtDienThoai.getText().trim());
        if (phoneErr != null) {
            DialogUtil.showWarn(this, phoneErr);
            txtDienThoai.requestFocus();
            return;
        }
        UngVien uv = new UngVien();
        uv.setMaTin(tin.getMaTin());
        uv.setHoTen(hoTen);
        uv.setEmail(txtEmail.getText().trim());
        uv.setDienThoai(txtDienThoai.getText().trim());
        uv.setNgaySinh(ngaySinh);
        uv.setGioiTinh((String) cboGioiTinh.getSelectedItem());
        uv.setDiaChi(txtDiaChi.getText().trim());
        uv.setTrinhDoHocVan((String) cboTrinhDo.getSelectedItem());
        uv.setKinhNghiem(txtKinhNghiem.getText().trim());
        uv.setNguonUngTuyen(txtNguonUngTuyen.getText().trim());
        uv.setNhanXet(txtNhanXet.getText().trim());
        uv.setTrangThai("moi");
        uv.setNgayTao(LocalDate.now());
        KetQua<?> kq = tuyenDungBUS.tiepNhanUngVien(uv);
        if (kq.isSuccess()) {
            DialogUtil.showSuccess(this, "Đã tạo ứng viên thành công!");
            thanhCong = true;
            dispose();
        } else {
            DialogUtil.showError(this, kq.getMessage());
        }
    }

    public boolean isThanhCong() {
        return thanhCong;
    }

    // ── helpers ──────────────────────────────────────────────────────────────
    private JTextField field(int cols) {
        JTextField f = new JTextField(cols);
        f.setFont(com.hrm.util.UIFonts.TEXT_NORMAL);
        return f;
    }

    private JTextArea area(int rows) {
        JTextArea a = new JTextArea(rows, 25);
        a.setFont(com.hrm.util.UIFonts.TEXT_NORMAL);
        a.setLineWrap(true);
        a.setWrapStyleWord(true);
        return a;
    }

    private GridBagConstraints labelGBC() {
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0; g.anchor = GridBagConstraints.WEST;
        g.insets = new Insets(4, 4, 4, 8);
        return g;
    }

    private GridBagConstraints fieldGBC() {
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 1; g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1.0; g.insets = new Insets(4, 0, 4, 4);
        return g;
    }

    private void addRow(JPanel p, int row, String label, Component comp,
                        GridBagConstraints lblGbc, GridBagConstraints fldGbc) {
        lblGbc.gridy = row; fldGbc.gridy = row;
        JLabel lbl = new JLabel(label);
        lbl.setFont(com.hrm.util.UIFonts.TEXT_NORMAL);
        p.add(lbl, lblGbc);
        p.add(comp, fldGbc);
    }
}
