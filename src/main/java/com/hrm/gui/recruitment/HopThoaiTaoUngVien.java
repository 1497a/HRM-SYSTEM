package com.hrm.gui.recruitment;

import com.hrm.bus.TuyenDungBUS;
import com.hrm.bus.KetQua;
import com.hrm.model.TinTuyenDung;
import com.hrm.model.UngVien;
import com.hrm.util.DialogUtil;
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
 * Dialog tạo mới hoặc chỉnh sửa ứng viên.
 */
public class HopThoaiTaoUngVien extends JDialog {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final TuyenDungBUS tuyenDungBUS;
    private final UngVien ungVienEdit; // null = tạo mới, non-null = sửa
    private boolean thanhCong = false;

    // Fields
    private JComboBox<TinTuyenDung> cboTin;
    private JLabel lblTinReadOnly;
    private JTextField txtHoTen;
    private JTextField txtEmail;
    private JTextField txtDienThoai;
    private JTextField txtNgaySinh;
    private JComboBox<String> cboGioiTinh;
    private JTextField txtDiaChi;
    private JComboBox<String> cboTrinhDo;
    private JTextArea txtKinhNghiem;
    private JTextField txtNguonUngTuyen;
    private JTextArea txtNhanXet;

    /** Constructor tạo mới ứng viên */
    public HopThoaiTaoUngVien(Window owner, TuyenDungBUS tuyenDungBUS) {
        this(owner, tuyenDungBUS, null);
    }

    /** Constructor chỉnh sửa ứng viên */
    public HopThoaiTaoUngVien(Window owner, TuyenDungBUS tuyenDungBUS, UngVien ungVien) {
        super(owner, ungVien == null ? "Tạo ứng viên mới" : "Chỉnh sửa ứng viên", ModalityType.APPLICATION_MODAL);
        this.tuyenDungBUS = tuyenDungBUS;
        this.ungVienEdit = ungVien;
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

        if (ungVienEdit == null) {
            // Chế độ tạo mới: hiển thị combobox chọn tin
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
            addRow(p, row++, "Tin tuyển dụng (*):", cboTin, lbl, fld);
        } else {
            // Chế độ sửa: hiển thị tên tin dưới dạng label
            String tenTin = ungVienEdit.getTenTin() != null ? ungVienEdit.getTenTin() : "[Không rõ]";
            lblTinReadOnly = new JLabel(tenTin);
            lblTinReadOnly.setFont(com.hrm.util.UIFonts.TEXT_NORMAL);
            addRow(p, row++, "Tin tuyển dụng:", lblTinReadOnly, lbl, fld);
            // Pre-fill các trường
            txtHoTen.setText(ungVienEdit.getHoTen() != null ? ungVienEdit.getHoTen() : "");
            txtEmail.setText(ungVienEdit.getEmail() != null ? ungVienEdit.getEmail() : "");
            txtDienThoai.setText(ungVienEdit.getDienThoai() != null ? ungVienEdit.getDienThoai() : "");
            if (ungVienEdit.getNgaySinh() != null) {
                txtNgaySinh.setText(ungVienEdit.getNgaySinh().format(DATE_FMT));
            }
            if (ungVienEdit.getGioiTinh() != null) {
                for (int i = 0; i < cboGioiTinh.getItemCount(); i++) {
                    if (cboGioiTinh.getItemAt(i).equals(ungVienEdit.getGioiTinh())) {
                        cboGioiTinh.setSelectedIndex(i);
                        break;
                    }
                }
            }
            txtDiaChi.setText(ungVienEdit.getDiaChi() != null ? ungVienEdit.getDiaChi() : "");
            if (ungVienEdit.getTrinhDoHocVan() != null) {
                for (int i = 0; i < cboTrinhDo.getItemCount(); i++) {
                    if (cboTrinhDo.getItemAt(i).equals(ungVienEdit.getTrinhDoHocVan())) {
                        cboTrinhDo.setSelectedIndex(i);
                        break;
                    }
                }
            }
            txtKinhNghiem.setText(ungVienEdit.getKinhNghiem() != null ? ungVienEdit.getKinhNghiem() : "");
            txtNguonUngTuyen.setText(ungVienEdit.getNguonUngTuyen() != null ? ungVienEdit.getNguonUngTuyen() : "");
            txtNhanXet.setText(ungVienEdit.getNhanXet() != null ? ungVienEdit.getNhanXet() : "");
        }

        addRow(p, row++, "Họ tên (*):",              txtHoTen,                         lbl, fld);
        addRow(p, row++, "Email (*):",                txtEmail,                         lbl, fld);
        addRow(p, row++, "Điện thoại (*):",           txtDienThoai,                     lbl, fld);
        addRow(p, row++, "Ngày sinh (*) (dd/MM/yyyy):", txtNgaySinh,                    lbl, fld);
        addRow(p, row++, "Giới tính:",                cboGioiTinh,                      lbl, fld);
        addRow(p, row++, "Địa chỉ (*):",              txtDiaChi,                        lbl, fld);
        addRow(p, row++, "Trình độ học vấn:",         cboTrinhDo,                       lbl, fld);
        addRow(p, row++, "Kinh nghiệm:",              new JScrollPane(txtKinhNghiem),   lbl, fld);
        addRow(p, row++, "Nguồn ứng tuyển (*):",      txtNguonUngTuyen,                 lbl, fld);
        addRow(p, row++, "Nhận xét ban đầu:",         new JScrollPane(txtNhanXet),      lbl, fld);
        return p;
    }

    private JPanel buildButtons() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        panel.setBackground(Color.WHITE);
        JButton btnLuu = UIHelper.createSuccessButton("Lưu");
        JButton btnHuy = UIHelper.createDefaultButton("Hủy");
        btnLuu.addActionListener(e -> luu());
        btnHuy.addActionListener(e -> dispose());
        panel.add(btnHuy);
        panel.add(btnLuu);
        return panel;
    }

    private void luu() {
        // ── Validate bắt buộc ────────────────────────────────────────────
        String hoTen = txtHoTen.getText().trim();
        if (hoTen.isEmpty()) {
            DialogUtil.showWarn(this, "Vui lòng nhập họ tên.");
            txtHoTen.requestFocus();
            return;
        }
        String email = txtEmail.getText().trim();
        if (email.isEmpty()) {
            DialogUtil.showWarn(this, "Vui lòng nhập email.");
            txtEmail.requestFocus();
            return;
        }
        String emailErr = ValidationUtils.validateEmail(email);
        if (emailErr != null) {
            DialogUtil.showWarn(this, emailErr);
            txtEmail.requestFocus();
            return;
        }
        String dienThoai = txtDienThoai.getText().trim();
        if (dienThoai.isEmpty()) {
            DialogUtil.showWarn(this, "Vui lòng nhập số điện thoại.");
            txtDienThoai.requestFocus();
            return;
        }
        String phoneErr = ValidationUtils.validatePhone(dienThoai);
        if (phoneErr != null) {
            DialogUtil.showWarn(this, phoneErr);
            txtDienThoai.requestFocus();
            return;
        }
        String nsStr = txtNgaySinh.getText().trim();
        if (nsStr.isEmpty()) {
            DialogUtil.showWarn(this, "Vui lòng nhập ngày sinh.");
            txtNgaySinh.requestFocus();
            return;
        }
        LocalDate ngaySinh;
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
        String diaChi = txtDiaChi.getText().trim();
        if (diaChi.isEmpty()) {
            DialogUtil.showWarn(this, "Vui lòng nhập địa chỉ.");
            txtDiaChi.requestFocus();
            return;
        }
        String nguon = txtNguonUngTuyen.getText().trim();
        if (nguon.isEmpty()) {
            DialogUtil.showWarn(this, "Vui lòng nhập nguồn ứng tuyển.");
            txtNguonUngTuyen.requestFocus();
            return;
        }

        if (ungVienEdit == null) {
            luuMoi(hoTen, email, dienThoai, ngaySinh, diaChi, nguon);
        } else {
            luuSua(hoTen, email, dienThoai, ngaySinh, diaChi, nguon);
        }
    }

    private void luuMoi(String hoTen, String email, String dienThoai,
                        LocalDate ngaySinh, String diaChi, String nguon) {
        TinTuyenDung tin = (TinTuyenDung) cboTin.getSelectedItem();
        if (tin == null || tin.getMaTin() == 0) {
            DialogUtil.showWarn(this, "Vui lòng chọn tin tuyển dụng.");
            cboTin.requestFocus();
            return;
        }
        UngVien uv = new UngVien();
        uv.setMaTin(tin.getMaTin());
        uv.setHoTen(hoTen);
        uv.setEmail(email);
        uv.setDienThoai(dienThoai);
        uv.setNgaySinh(ngaySinh);
        uv.setGioiTinh((String) cboGioiTinh.getSelectedItem());
        uv.setDiaChi(diaChi);
        uv.setTrinhDoHocVan((String) cboTrinhDo.getSelectedItem());
        uv.setKinhNghiem(txtKinhNghiem.getText().trim());
        uv.setNguonUngTuyen(nguon);
        uv.setNhanXet(txtNhanXet.getText().trim());
        KetQua<?> kq = tuyenDungBUS.tiepNhanUngVien(uv);
        if (kq.isSuccess()) {
            DialogUtil.showSuccess(this, "Đã tạo ứng viên thành công!");
            thanhCong = true;
            dispose();
        } else {
            DialogUtil.showError(this, kq.getMessage());
        }
    }

    private void luuSua(String hoTen, String email, String dienThoai,
                        LocalDate ngaySinh, String diaChi, String nguon) {
        UngVien uv = new UngVien();
        uv.setMaUngVien(ungVienEdit.getMaUngVien());
        uv.setHoTen(hoTen);
        uv.setEmail(email);
        uv.setDienThoai(dienThoai);
        uv.setNgaySinh(ngaySinh);
        uv.setGioiTinh((String) cboGioiTinh.getSelectedItem());
        uv.setDiaChi(diaChi);
        uv.setTrinhDoHocVan((String) cboTrinhDo.getSelectedItem());
        uv.setKinhNghiem(txtKinhNghiem.getText().trim());
        uv.setNguonUngTuyen(nguon);
        uv.setNhanXet(txtNhanXet.getText().trim());
        KetQua<?> kq = tuyenDungBUS.capNhatThongTinUV(uv);
        if (kq.isSuccess()) {
            DialogUtil.showSuccess(this, "Đã cập nhật ứng viên thành công!");
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
