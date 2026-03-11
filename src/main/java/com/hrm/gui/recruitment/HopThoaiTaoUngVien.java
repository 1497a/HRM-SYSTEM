package com.hrm.gui.recruitment;

import com.hrm.bus.TuyenDungBUS;
import com.hrm.bus.KetQua;
import com.hrm.model.TinTuyenDung;
import com.hrm.model.UngVien;
import com.hrm.util.UIColors;
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
        super(owner, "Tao ung vien moi", ModalityType.APPLICATION_MODAL);
        this.tuyenDungBUS = tuyenDungBUS;
        initUI();
        pack();
        setMinimumSize(new Dimension(520, 600));
        setLocationRelativeTo(owner);
    }

    private void initUI() {
        JPanel main = new JPanel(new BorderLayout(8, 8));
        main.setBorder(new EmptyBorder(16, 16, 8, 16));
        main.setBackground(UIColors.WHITE);

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
        placeholder.setTieuDe("-- Chon tin tuyen dung --");
        cboTin.addItem(placeholder);
        for (TinTuyenDung t : dsTin) cboTin.addItem(t);
        cboTin.setRenderer((list, value, index, sel, focus) ->
                new JLabel(value != null ? value.getTieuDe() : ""));

        txtHoTen        = field(25);
        txtEmail        = field(25);
        txtDienThoai    = field(15);
        txtNgaySinh     = field(12);
        txtNgaySinh.setToolTipText("Dinh dang: dd/MM/yyyy");

        cboGioiTinh = new JComboBox<>(new String[]{"nam", "nu", "khac"});
        cboGioiTinh.setFont(com.hrm.util.UIFonts.TEXT_NORMAL);

        txtDiaChi = field(30);

        cboTrinhDo = new JComboBox<>(new String[]{
            "Trung hoc pho thong", "Trung cap", "Cao dang", "Dai hoc", "Thac si", "Tien si", "Khac"
        });
        cboTrinhDo.setFont(com.hrm.util.UIFonts.TEXT_NORMAL);

        txtKinhNghiem   = area(4);
        txtNguonUngTuyen = field(20);
        txtNhanXet      = area(3);

        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(UIColors.WHITE);
        GridBagConstraints lbl = labelGBC();
        GridBagConstraints fld = fieldGBC();

        int row = 0;
        addRow(p, row++, "Tin tuyen dung (*):", cboTin,              lbl, fld);
        addRow(p, row++, "Ho ten (*):",         txtHoTen,            lbl, fld);
        addRow(p, row++, "Email:",               txtEmail,            lbl, fld);
        addRow(p, row++, "Dien thoai:",          txtDienThoai,        lbl, fld);
        addRow(p, row++, "Ngay sinh (dd/MM/yyyy):", txtNgaySinh,      lbl, fld);
        addRow(p, row++, "Gioi tinh:",           cboGioiTinh,         lbl, fld);
        addRow(p, row++, "Dia chi:",             txtDiaChi,           lbl, fld);
        addRow(p, row++, "Trinh do hoc van:",    cboTrinhDo,          lbl, fld);
        addRow(p, row++, "Kinh nghiem:",         new JScrollPane(txtKinhNghiem), lbl, fld);
        addRow(p, row++, "Nguon ung tuyen:",     txtNguonUngTuyen,    lbl, fld);
        addRow(p, row++, "Nhan xet ban dau:",    new JScrollPane(txtNhanXet),    lbl, fld);

        return p;
    }

    private JPanel buildButtons() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        panel.setBackground(UIColors.WHITE);

        JButton btnLuu   = UIHelper.createSuccessButton("Luu");
        JButton btnHuy   = UIHelper.createDefaultButton("Huy");

        btnLuu.addActionListener(e -> luu());
        btnHuy.addActionListener(e -> dispose());

        panel.add(btnHuy);
        panel.add(btnLuu);
        return panel;
    }

    private void luu() {
        String hoTen = txtHoTen.getText().trim();
        if (hoTen.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui long nhap ho ten.", "Loi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        TinTuyenDung tin = (TinTuyenDung) cboTin.getSelectedItem();
        if (tin == null || tin.getMaTin() == 0) {
            JOptionPane.showMessageDialog(this, "Vui long chon tin tuyen dung.", "Loi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        LocalDate ngaySinh = null;
        String nsStr = txtNgaySinh.getText().trim();
        if (!nsStr.isEmpty()) {
            try {
                ngaySinh = LocalDate.parse(nsStr, DATE_FMT);
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(this, "Ngay sinh khong hop le. Dinh dang: dd/MM/yyyy", "Loi", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String dobErr = ValidationUtils.validateBirthDate(ngaySinh);
            if (dobErr != null) {
                JOptionPane.showMessageDialog(this, dobErr, "Loi", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        // Validate email and phone
        String emailErr = ValidationUtils.validateEmail(txtEmail.getText().trim());
        if (emailErr != null) {
            JOptionPane.showMessageDialog(this, emailErr, "Loi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String phoneErr = ValidationUtils.validatePhone(txtDienThoai.getText().trim());
        if (phoneErr != null) {
            JOptionPane.showMessageDialog(this, phoneErr, "Loi", JOptionPane.WARNING_MESSAGE);
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
            JOptionPane.showMessageDialog(this, "Da tao ung vien thanh cong!", "Thanh cong", JOptionPane.INFORMATION_MESSAGE);
            thanhCong = true;
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, kq.getMessage(), "Loi", JOptionPane.ERROR_MESSAGE);
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
