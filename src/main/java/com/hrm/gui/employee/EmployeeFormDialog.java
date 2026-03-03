package com.hrm.gui.employee;

import com.hrm.gui.components.PurpleButton;
import com.hrm.model.NhanVien;
import com.hrm.model.ThongTinCaNhan;
import com.hrm.bus.NhanVienBUS;
import com.hrm.bus.KetQua;
import com.hrm.util.UIColors;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Dialog tạo mới / chỉnh sửa hồ sơ nhân viên.
 * Gồm 2 tab: Thông tin lao động + Thông tin cá nhân.
 */
public class EmployeeFormDialog extends JDialog {

    private final NhanVienBUS nvService = NhanVienBUS.getInstance();
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final boolean isEdit;
    private NhanVien nhanVien;
    private ThongTinCaNhan thongTinCaNhan;
    private boolean saved = false;

    // Tab 1 - Thông tin lao động
    private JTextField txtMaNhanVien;
    private JComboBox<String> cboLoaiHopDong;
    private JTextField txtNgayVaoLam;
    private JTextArea txtGhiChu;

    // Tab 2 - Thông tin cá nhân
    private JTextField txtHoTen;
    private JTextField txtNgaySinh;
    private JComboBox<String> cboGioiTinh;
    private JTextField txtCCCD;
    private JTextField txtDienThoai;
    private JTextField txtEmail;
    private JTextField txtDiaChi;
    private JTextField txtDiaChiThuongTru;
    private JTextField txtQueQuan;
    private JComboBox<String> cboTinhTrangHonNhan;

    /**
     * @param parent  Frame cha
     * @param nv      null = tạo mới; có giá trị = sửa
     * @param ttcn    null = tạo mới; có giá trị = sửa
     */
    public EmployeeFormDialog(Frame parent, NhanVien nv, ThongTinCaNhan ttcn) {
        super(parent, nv == null ? "Them moi ho so nhan vien" : "Chinh sua ho so nhan vien", true);
        this.nhanVien = nv != null ? nv : new NhanVien();
        this.thongTinCaNhan = ttcn != null ? ttcn : new ThongTinCaNhan();
        this.isEdit = nv != null;

        setSize(680, 560);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);

        initUI();
        if (isEdit) {
            loadData();
        } else {
            prefillNew();
        }
    }

    // ============================
    // Build UI
    // ============================

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout(0, 8));
        root.setBackground(UIColors.LIGHT_GRAY_BG);
        root.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // Header
        JLabel lblHeader = new JLabel(isEdit ? "Chinh sua ho so nhan vien" : "Them moi ho so nhan vien");
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblHeader.setForeground(UIColors.PRIMARY_PURPLE);
        lblHeader.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        root.add(lblHeader, BorderLayout.NORTH);

        // Tabbed pane
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tabs.addTab("Thong tin lao dong", buildTab1());
        tabs.addTab("Thong tin ca nhan", buildTab2());
        root.add(tabs, BorderLayout.CENTER);

        // Buttons
        root.add(buildButtonPanel(), BorderLayout.SOUTH);

        setContentPane(root);
    }

    private JPanel buildTab1() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        GridBagConstraints gbc = defaultGBC();

        // Mã nhân viên
        addLabel(panel, "Ma nhan vien (*):", gbc, 0, 0);
        txtMaNhanVien = new JTextField(20);
        if (isEdit) txtMaNhanVien.setEditable(false);
        addField(panel, txtMaNhanVien, gbc, 1, 0);

        // Loại hợp đồng
        addLabel(panel, "Loai hop dong:", gbc, 0, 1);
        cboLoaiHopDong = new JComboBox<>(new String[]{
            "thu_viec", "xac_dinh_thoi_han", "khong_xac_dinh"
        });
        cboLoaiHopDong.setRenderer(new LoaiHDRenderer());
        addField(panel, cboLoaiHopDong, gbc, 1, 1);

        // Ngày vào làm
        addLabel(panel, "Ngay vao lam (*):", gbc, 0, 2);
        txtNgayVaoLam = new JTextField("dd/MM/yyyy", 20);
        addField(panel, txtNgayVaoLam, gbc, 1, 2);

        // Ghi chú
        addLabel(panel, "Ghi chu:", gbc, 0, 3);
        txtGhiChu = new JTextArea(4, 20);
        txtGhiChu.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtGhiChu.setLineWrap(true);
        txtGhiChu.setWrapStyleWord(true);
        JScrollPane scrollGhiChu = new JScrollPane(txtGhiChu);
        scrollGhiChu.setPreferredSize(new Dimension(300, 80));
        gbc.gridx = 1; gbc.gridy = 3; gbc.fill = GridBagConstraints.BOTH;
        panel.add(scrollGhiChu, gbc);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        return panel;
    }

    private JPanel buildTab2() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        GridBagConstraints gbc = defaultGBC();

        addLabel(panel, "Ho ten (*):", gbc, 0, 0);
        txtHoTen = new JTextField(25);
        addField(panel, txtHoTen, gbc, 1, 0);

        addLabel(panel, "Ngay sinh:", gbc, 0, 1);
        txtNgaySinh = new JTextField("dd/MM/yyyy", 20);
        addField(panel, txtNgaySinh, gbc, 1, 1);

        addLabel(panel, "Gioi tinh:", gbc, 0, 2);
        cboGioiTinh = new JComboBox<>(new String[]{"nam", "nu", "khac"});
        cboGioiTinh.setRenderer(new GioiTinhRenderer());
        addField(panel, cboGioiTinh, gbc, 1, 2);

        addLabel(panel, "CCCD (12 chu so):", gbc, 0, 3);
        txtCCCD = new JTextField(20);
        addField(panel, txtCCCD, gbc, 1, 3);

        addLabel(panel, "Dien thoai:", gbc, 0, 4);
        txtDienThoai = new JTextField(20);
        addField(panel, txtDienThoai, gbc, 1, 4);

        addLabel(panel, "Email:", gbc, 0, 5);
        txtEmail = new JTextField(25);
        addField(panel, txtEmail, gbc, 1, 5);

        addLabel(panel, "Dia chi hien tai:", gbc, 0, 6);
        txtDiaChi = new JTextField(30);
        addField(panel, txtDiaChi, gbc, 1, 6);

        addLabel(panel, "Dia chi thuong tru:", gbc, 0, 7);
        txtDiaChiThuongTru = new JTextField(30);
        addField(panel, txtDiaChiThuongTru, gbc, 1, 7);

        addLabel(panel, "Que quan:", gbc, 0, 8);
        txtQueQuan = new JTextField(25);
        addField(panel, txtQueQuan, gbc, 1, 8);

        addLabel(panel, "Tinh trang hon nhan:", gbc, 0, 9);
        cboTinhTrangHonNhan = new JComboBox<>(new String[]{"doc_than", "da_ket_hon", "ly_hon"});
        cboTinhTrangHonNhan.setRenderer(new HonNhanRenderer());
        addField(panel, cboTinhTrangHonNhan, gbc, 1, 9);

        return panel;
    }

    private JPanel buildButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        panel.setOpaque(false);

        PurpleButton btnLuu = new PurpleButton("Luu");
        PurpleButton btnHuy = PurpleButton.secondary("Huy");

        btnLuu.setPreferredSize(new Dimension(100, 36));
        btnHuy.setPreferredSize(new Dimension(100, 36));

        btnLuu.addActionListener(e -> onSave());
        btnHuy.addActionListener(e -> dispose());

        panel.add(btnLuu);
        panel.add(btnHuy);
        return panel;
    }

    // ============================
    // Load / Prefill data
    // ============================

    private void loadData() {
        // Tab 1
        txtMaNhanVien.setText(nhanVien.getMaNhanVien());
        if (nhanVien.getLoaiHopDong() != null) {
            cboLoaiHopDong.setSelectedItem(nhanVien.getLoaiHopDong());
        }
        if (nhanVien.getNgayVaoLam() != null) {
            txtNgayVaoLam.setText(nhanVien.getNgayVaoLam().format(dtf));
        }
        txtGhiChu.setText(nhanVien.getGhiChu() != null ? nhanVien.getGhiChu() : "");

        // Tab 2
        txtHoTen.setText(thongTinCaNhan.getHoTen() != null ? thongTinCaNhan.getHoTen() : "");
        if (thongTinCaNhan.getNgaySinh() != null) {
            txtNgaySinh.setText(thongTinCaNhan.getNgaySinh().format(dtf));
        }
        if (thongTinCaNhan.getGioiTinh() != null) {
            cboGioiTinh.setSelectedItem(thongTinCaNhan.getGioiTinh());
        }
        txtCCCD.setText(thongTinCaNhan.getCccd() != null ? thongTinCaNhan.getCccd() : "");
        txtDienThoai.setText(thongTinCaNhan.getDienThoai() != null ? thongTinCaNhan.getDienThoai() : "");
        txtEmail.setText(thongTinCaNhan.getEmail() != null ? thongTinCaNhan.getEmail() : "");
        txtDiaChi.setText(thongTinCaNhan.getDiaChi() != null ? thongTinCaNhan.getDiaChi() : "");
        txtDiaChiThuongTru.setText(thongTinCaNhan.getDiaChiThuongTru() != null ? thongTinCaNhan.getDiaChiThuongTru() : "");
        txtQueQuan.setText(thongTinCaNhan.getQueQuan() != null ? thongTinCaNhan.getQueQuan() : "");
        if (thongTinCaNhan.getTinhTrangHonNhan() != null) {
            cboTinhTrangHonNhan.setSelectedItem(thongTinCaNhan.getTinhTrangHonNhan());
        }
    }

    private void prefillNew() {
        txtMaNhanVien.setText(nvService.generateMaNhanVien());
        txtNgayVaoLam.setText(LocalDate.now().format(dtf));
        cboLoaiHopDong.setSelectedItem("xac_dinh_thoi_han");
        cboGioiTinh.setSelectedItem("nam");
        cboTinhTrangHonNhan.setSelectedItem("doc_than");
    }

    // ============================
    // Save
    // ============================

    private void onSave() {
        // Collect Tab 1
        String maNhanVien = txtMaNhanVien.getText().trim();
        String loaiHopDong = (String) cboLoaiHopDong.getSelectedItem();
        String ngayVaoLamStr = txtNgayVaoLam.getText().trim();
        String ghiChu = txtGhiChu.getText().trim();

        // Collect Tab 2
        String hoTen = txtHoTen.getText().trim();
        String ngaySinhStr = txtNgaySinh.getText().trim();
        String gioiTinh = (String) cboGioiTinh.getSelectedItem();
        String cccd = txtCCCD.getText().trim();
        String dienThoai = txtDienThoai.getText().trim();
        String email = txtEmail.getText().trim();
        String diaChi = txtDiaChi.getText().trim();
        String diaChiThuongTru = txtDiaChiThuongTru.getText().trim();
        String queQuan = txtQueQuan.getText().trim();
        String tinhTrangHonNhan = (String) cboTinhTrangHonNhan.getSelectedItem();

        // Basic validation
        if (maNhanVien.isEmpty()) {
            showError("Ma nhan vien khong duoc de trong.");
            return;
        }
        if (hoTen.isEmpty()) {
            showError("Ho ten khong duoc de trong.");
            return;
        }

        // Parse ngày vào làm
        LocalDate ngayVaoLam = null;
        if (!ngayVaoLamStr.isEmpty() && !ngayVaoLamStr.equals("dd/MM/yyyy")) {
            try {
                ngayVaoLam = LocalDate.parse(ngayVaoLamStr, dtf);
            } catch (DateTimeParseException e) {
                showError("Ngay vao lam khong hop le. Dinh dang: dd/MM/yyyy");
                return;
            }
        }

        // Parse ngày sinh
        LocalDate ngaySinh = null;
        if (!ngaySinhStr.isEmpty() && !ngaySinhStr.equals("dd/MM/yyyy")) {
            try {
                ngaySinh = LocalDate.parse(ngaySinhStr, dtf);
            } catch (DateTimeParseException e) {
                showError("Ngay sinh khong hop le. Dinh dang: dd/MM/yyyy");
                return;
            }
        }

        // Build model objects
        nhanVien.setMaNhanVien(maNhanVien);
        nhanVien.setLoaiHopDong(loaiHopDong);
        nhanVien.setNgayVaoLam(ngayVaoLam);
        nhanVien.setGhiChu(ghiChu);
        if (!isEdit) {
            nhanVien.setTrangThai("dang_lam_viec");
        }

        thongTinCaNhan.setHoTen(hoTen);
        thongTinCaNhan.setNgaySinh(ngaySinh);
        thongTinCaNhan.setGioiTinh(gioiTinh);
        thongTinCaNhan.setCccd(cccd.isEmpty() ? null : cccd);
        thongTinCaNhan.setDienThoai(dienThoai.isEmpty() ? null : dienThoai);
        thongTinCaNhan.setEmail(email.isEmpty() ? null : email);
        thongTinCaNhan.setDiaChi(diaChi.isEmpty() ? null : diaChi);
        thongTinCaNhan.setDiaChiThuongTru(diaChiThuongTru.isEmpty() ? null : diaChiThuongTru);
        thongTinCaNhan.setQueQuan(queQuan.isEmpty() ? null : queQuan);
        thongTinCaNhan.setTinhTrangHonNhan(tinhTrangHonNhan);

        if (isEdit) {
            // Update thông tin lao động (chỉ cho phép sửa một số trường)
            KetQua<ThongTinCaNhan> result = nvService.capNhatThongTinCaNhan(thongTinCaNhan);
            if (!result.isSuccess()) {
                showError(result.getMessage());
                return;
            }
            JOptionPane.showMessageDialog(this, "Cap nhat ho so thanh cong.",
                    "Thanh cong", JOptionPane.INFORMATION_MESSAGE);
        } else {
            // Tạo mới
            KetQua<NhanVien> result = nvService.taoHoSo(nhanVien, thongTinCaNhan);
            if (!result.isSuccess()) {
                showError(result.getMessage());
                return;
            }
            JOptionPane.showMessageDialog(this, result.getMessage(),
                    "Thanh cong", JOptionPane.INFORMATION_MESSAGE);
        }

        saved = true;
        dispose();
    }

    public boolean isSaved() {
        return saved;
    }

    // ============================
    // Helpers
    // ============================

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Loi nhap lieu", JOptionPane.ERROR_MESSAGE);
    }

    private GridBagConstraints defaultGBC() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        return gbc;
    }

    private void addLabel(JPanel panel, String text, GridBagConstraints gbc, int col, int row) {
        gbc.gridx = col;
        gbc.gridy = row;
        gbc.weightx = 0;
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lbl.setForeground(UIColors.TEXT_DARK);
        panel.add(lbl, gbc);
    }

    private void addField(JPanel panel, JComponent field, GridBagConstraints gbc, int col, int row) {
        gbc.gridx = col;
        gbc.gridy = row;
        gbc.weightx = 1.0;
        if (field instanceof JTextField) {
            ((JTextField) field).setFont(new Font("Segoe UI", Font.PLAIN, 14));
            ((JTextField) field).setPreferredSize(new Dimension(0, 30));
        } else if (field instanceof JComboBox) {
            ((JComboBox<?>) field).setFont(new Font("Segoe UI", Font.PLAIN, 14));
        }
        panel.add(field, gbc);
    }

    // ============================
    // Renderers
    // ============================

    private static class LoaiHDRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if ("thu_viec".equals(value)) setText("Thu viec");
            else if ("xac_dinh_thoi_han".equals(value)) setText("Xac dinh thoi han");
            else if ("khong_xac_dinh".equals(value)) setText("Khong xac dinh");
            return this;
        }
    }

    private static class GioiTinhRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if ("nam".equals(value)) setText("Nam");
            else if ("nu".equals(value)) setText("Nu");
            else if ("khac".equals(value)) setText("Khac");
            return this;
        }
    }

    private static class HonNhanRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if ("doc_than".equals(value)) setText("Doc than");
            else if ("da_ket_hon".equals(value)) setText("Da ket hon");
            else if ("ly_hon".equals(value)) setText("Ly hon");
            return this;
        }
    }
}
