package com.hrm.gui.employee;

import com.hrm.model.HopDongLaoDong;
import com.hrm.model.NhanVien;
import com.hrm.model.ThongTinCaNhan;
import com.hrm.bus.KetQua;
import com.hrm.util.UIColors;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Tab 1 cua EmployeeDetailPanel: Thong tin ca nhan + hop dong.
 * Expose public methods de EmployeeDetailPanel goi: setEditMode, setStatusEditMode,
 * hasChanges, save, getTrangThai.
 */
class TabThongTinCaNhanPanel extends JPanel {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String[] STATUS_OPTIONS_ALL = {"dang_lam_viec", "tam_nghi", "nghi_viec"};

    private final String maNV;
    private ThongTinCaNhan ttcn;

    // Cac truong co the chinh sua
    private JTextField txtHoTen, txtNgaySinh, txtCCCD, txtDienThoai, txtEmail;
    private JTextField txtDiaChi, txtDiaChiThuongTru, txtQueQuan, txtTrinhDoHocVan, txtFileCV;
    private JTextArea  txtKinhNghiem;
    private JComboBox<String> cboGioiTinh, cboTinhTrangHonNhan;
    JComboBox<String> cboTrangThaiNhanVien; // dung boi EmployeeDetailPanel

    TabThongTinCaNhanPanel(String maNV, NhanVien nhanVien, ThongTinCaNhan ttcn, HopDongLaoDong hopDong) {
        this.maNV = maNV;
        this.ttcn = ttcn;
        setLayout(new BorderLayout());

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(UIColors.WHITE);
        content.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        // Section: Thong tin nhan vien
        content.add(buildSectionTitle("Thông tin nhân viên"));
        content.add(Box.createVerticalStrut(8));
        JPanel nvPanel = buildInfoGrid();
        addInfoRow(nvPanel, 0, "Mã nhân viên:", nhanVien != null ? safe(nhanVien.getMaNhanVien()) : "");
        addInfoRow(nvPanel, 1, "Loại hợp đồng:", nhanVien != null ? safe(nhanVien.getLoaiHopDongDisplay()) : "");
        addInfoRow(nvPanel, 2, "Ngày vào làm:",
                nhanVien != null && nhanVien.getNgayVaoLam() != null
                        ? nhanVien.getNgayVaoLam().format(DATE_FMT) : "");
        cboTrangThaiNhanVien = new JComboBox<>(new String[]{"dang_lam_viec", "tam_nghi", "nghi_viec"});
        cboTrangThaiNhanVien.setFont(com.hrm.util.UIFonts.TEXT_NORMAL);
        cboTrangThaiNhanVien.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setText(trangThaiDisplay(value != null ? value.toString() : ""));
                return this;
            }
        });
        cboTrangThaiNhanVien.setSelectedItem(nhanVien != null ? nhanVien.getTrangThai() : "dang_lam_viec");
        setStatusEditMode(false);
        addInfoRow(nvPanel, 3, "Trạng thái:", cboTrangThaiNhanVien);
        content.add(nvPanel);
        content.add(Box.createVerticalStrut(16));

        // Section: Thong tin ca nhan
        content.add(buildSectionTitle("Thông tin cá nhân"));
        content.add(Box.createVerticalStrut(8));
        JPanel ttcnPanel = buildInfoGrid();
        if (ttcn != null) {
            txtHoTen          = createReadOnlyTextField();
            txtNgaySinh       = createReadOnlyTextField();
            cboGioiTinh       = new JComboBox<>(new String[]{"nam", "nu", "khac"});
            txtCCCD           = createReadOnlyTextField();
            txtDienThoai      = createReadOnlyTextField();
            txtEmail          = createReadOnlyTextField();
            txtDiaChi         = createReadOnlyTextField();
            txtDiaChiThuongTru = createReadOnlyTextField();
            txtQueQuan        = createReadOnlyTextField();
            cboTinhTrangHonNhan = new JComboBox<>(new String[]{"doc_than", "da_ket_hon", "ly_hon"});
            txtTrinhDoHocVan  = createReadOnlyTextField();
            txtFileCV         = createReadOnlyTextField();
            txtKinhNghiem     = createReadOnlyTextArea();

            addInfoRow(ttcnPanel, 0,  "Họ và tên:",            txtHoTen);
            addInfoRow(ttcnPanel, 1,  "Ngày sinh:",            txtNgaySinh);
            addInfoRow(ttcnPanel, 2,  "Giới tính:",            cboGioiTinh);
            addInfoRow(ttcnPanel, 3,  "CCCD:",                 txtCCCD);
            addInfoRow(ttcnPanel, 4,  "Số điện thoại:",        txtDienThoai);
            addInfoRow(ttcnPanel, 5,  "Email:",                txtEmail);
            addInfoRow(ttcnPanel, 6,  "Địa chỉ:",              txtDiaChi);
            addInfoRow(ttcnPanel, 7,  "Địa chỉ thường trú:",   txtDiaChiThuongTru);
            addInfoRow(ttcnPanel, 8,  "Quê quán:",             txtQueQuan);
            addInfoRow(ttcnPanel, 9,  "Tình trạng hôn nhân:",  cboTinhTrangHonNhan);
            addInfoRow(ttcnPanel, 10, "Trình độ học vấn:",     txtTrinhDoHocVan);
            addInfoRow(ttcnPanel, 11, "File CV:",              txtFileCV);
            addInfoRow(ttcnPanel, 12, "Kinh nghiệm:",          new JScrollPane(txtKinhNghiem));
            loadFields();
            setEditMode(false);
        } else {
            JLabel noData = new JLabel("  Không có thông tin cá nhân.");
            noData.setFont(new Font("Segoe UI", Font.ITALIC, 13));
            noData.setForeground(UIColors.TEXT_GRAY);
            ttcnPanel.add(noData, buildGbc(0, 0, 2));
        }
        content.add(ttcnPanel);
        content.add(Box.createVerticalStrut(16));

        // Section: Hop dong lao dong
        content.add(buildSectionTitle("Hợp đồng lao động"));
        content.add(Box.createVerticalStrut(8));
        JPanel hdPanel = buildInfoGrid();
        if (hopDong != null) {
            addInfoRow(hdPanel, 0, "Số hợp đồng:",   safe(hopDong.getSoHopDong()));
            addInfoRow(hdPanel, 1, "Loại hợp đồng:", safe(hopDong.getLoaiHopDongDisplay()));
            addInfoRow(hdPanel, 2, "Ngày ký:",
                    hopDong.getNgayKy() != null ? hopDong.getNgayKy().format(DATE_FMT) : "");
            addInfoRow(hdPanel, 3, "Hiệu lực từ:",
                    hopDong.getNgayHieuLuc() != null ? hopDong.getNgayHieuLuc().format(DATE_FMT) : "");
            addInfoRow(hdPanel, 4, "Hiệu lực đến:",
                    hopDong.getNgayHetHieuLuc() != null ? hopDong.getNgayHetHieuLuc().format(DATE_FMT) : "Không xác định");
            addInfoRow(hdPanel, 5, "Trạng thái:",
                    buildStatusLabel(hopDong.getTrangThai(), hopDong.getTrangThaiDisplay()));
        } else {
            JLabel noHd = new JLabel("  Chưa có hợp đồng hiệu lực.");
            noHd.setFont(new Font("Segoe UI", Font.ITALIC, 13));
            noHd.setForeground(UIColors.TEXT_GRAY);
            hdPanel.add(noHd, buildGbc(0, 0, 2));
        }
        content.add(hdPanel);
        content.add(Box.createVerticalGlue());

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);
    }

    // ---- Public API for EmployeeDetailPanel ----

    void setEditMode(boolean editable) {
        if (ttcn == null) return;
        txtHoTen.setEditable(editable);
        txtNgaySinh.setEditable(editable);
        cboGioiTinh.setEnabled(editable);
        txtCCCD.setEditable(editable);
        txtDienThoai.setEditable(editable);
        txtEmail.setEditable(editable);
        txtDiaChi.setEditable(editable);
        txtDiaChiThuongTru.setEditable(editable);
        txtQueQuan.setEditable(editable);
        cboTinhTrangHonNhan.setEnabled(editable);
        txtTrinhDoHocVan.setEditable(editable);
        txtFileCV.setEditable(editable);
        txtKinhNghiem.setEditable(editable);

        Color bg = editable ? UIColors.WHITE : new Color(242, 242, 242);
        Border border = editable
                ? BorderFactory.createLineBorder(UIColors.PRIMARY_PURPLE, 1)
                : BorderFactory.createLineBorder(UIColors.BORDER_GRAY, 1);
        applyVisual(txtHoTen, bg, border);
        applyVisual(txtNgaySinh, bg, border);
        applyVisual(txtCCCD, bg, border);
        applyVisual(txtDienThoai, bg, border);
        applyVisual(txtEmail, bg, border);
        applyVisual(txtDiaChi, bg, border);
        applyVisual(txtDiaChiThuongTru, bg, border);
        applyVisual(txtQueQuan, bg, border);
        applyVisual(txtTrinhDoHocVan, bg, border);
        applyVisual(txtFileCV, bg, border);
        applyVisual(txtKinhNghiem, bg, border);
        cboGioiTinh.setBackground(bg);         cboGioiTinh.setBorder(border);
        cboTinhTrangHonNhan.setBackground(bg);  cboTinhTrangHonNhan.setBorder(border);
    }

    void setStatusEditMode(boolean editable) {
        if (cboTrangThaiNhanVien == null) return;
        cboTrangThaiNhanVien.setEnabled(editable);
        cboTrangThaiNhanVien.setBorder(editable
                ? BorderFactory.createLineBorder(UIColors.PRIMARY_PURPLE, 1)
                : BorderFactory.createEmptyBorder(1, 1, 1, 1));
        cboTrangThaiNhanVien.setBackground(editable ? UIColors.WHITE : UIColors.LIGHT_GRAY_BG);
    }

    boolean hasChanges() {
        if (ttcn == null) return false;
        String ns = ttcn.getNgaySinh() != null ? ttcn.getNgaySinh().format(DATE_FMT) : "";
        return !eq(txtHoTen.getText(), ttcn.getHoTen())
            || !eq(txtNgaySinh.getText(), ns)
            || !eq((String) cboGioiTinh.getSelectedItem(), ttcn.getGioiTinh())
            || !eq(txtCCCD.getText(), ttcn.getCccd())
            || !eq(txtDienThoai.getText(), ttcn.getDienThoai())
            || !eq(txtEmail.getText(), ttcn.getEmail())
            || !eq(txtDiaChi.getText(), ttcn.getDiaChi())
            || !eq(txtDiaChiThuongTru.getText(), ttcn.getDiaChiThuongTru())
            || !eq(txtQueQuan.getText(), ttcn.getQueQuan())
            || !eq((String) cboTinhTrangHonNhan.getSelectedItem(), ttcn.getTinhTrangHonNhan())
            || !eq(txtTrinhDoHocVan.getText(), ttcn.getTrinhDoHocVan())
            || !eq(txtFileCV.getText(), ttcn.getFileCV())
            || !eq(txtKinhNghiem.getText(), ttcn.getKinhNghiem());
    }

    KetQua<ThongTinCaNhan> save() {
        if (ttcn == null) { ttcn = new ThongTinCaNhan(); ttcn.setMaNV(maNV); }
        String hoTen = txtHoTen.getText().trim();
        if (hoTen.isEmpty()) return KetQua.error("Họ tên không được để trống.");

        String ngaySinhStr = txtNgaySinh.getText().trim();
        LocalDate ngaySinh = null;
        if (!ngaySinhStr.isEmpty()) {
            try { ngaySinh = LocalDate.parse(ngaySinhStr, DATE_FMT); }
            catch (DateTimeParseException e) { return KetQua.error("Ngày sinh không hợp lệ. Định dạng dd/MM/yyyy."); }
            if (ngaySinh.isAfter(LocalDate.now())) return KetQua.error("Ngày sinh không thể ở tương lai.");
        }
        String email = txtEmail.getText().trim();
        if (!email.isEmpty() && !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"))
            return KetQua.error("Email không hợp lệ.");
        String sdt = txtDienThoai.getText().trim();
        if (!sdt.isEmpty() && !sdt.matches("^\\d{9,11}$"))
            return KetQua.error("Số điện thoại không hợp lệ.");

        ttcn.setHoTen(hoTen);
        ttcn.setNgaySinh(ngaySinh);
        ttcn.setGioiTinh((String) cboGioiTinh.getSelectedItem());
        ttcn.setCccd(empty(txtCCCD.getText()));
        ttcn.setDienThoai(empty(sdt));
        ttcn.setEmail(empty(email));
        ttcn.setDiaChi(empty(txtDiaChi.getText().trim()));
        ttcn.setDiaChiThuongTru(empty(txtDiaChiThuongTru.getText().trim()));
        ttcn.setQueQuan(empty(txtQueQuan.getText().trim()));
        ttcn.setTinhTrangHonNhan((String) cboTinhTrangHonNhan.getSelectedItem());
        ttcn.setTrinhDoHocVan(empty(txtTrinhDoHocVan.getText().trim()));
        ttcn.setFileCV(empty(txtFileCV.getText().trim()));
        ttcn.setKinhNghiem(empty(txtKinhNghiem.getText().trim()));
        return com.hrm.bus.NhanVienBUS.getInstance().capNhatThongTinCaNhan(ttcn);
    }

    String getCurrentTrangThai() {
        return (String) cboTrangThaiNhanVien.getSelectedItem();
    }

    void configureStatusOptions() {
        if (cboTrangThaiNhanVien == null) {
            return;
        }
        String currentValue = (String) cboTrangThaiNhanVien.getSelectedItem();
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>(STATUS_OPTIONS_ALL);
        cboTrangThaiNhanVien.setModel(model);
        if (currentValue != null) {
            boolean exists = false;
            for (int i = 0; i < model.getSize(); i++) {
                if (currentValue.equals(model.getElementAt(i))) {
                    exists = true;
                    break;
                }
            }
            if (exists) {
                cboTrangThaiNhanVien.setSelectedItem(currentValue);
            } else {
                cboTrangThaiNhanVien.setSelectedItem(model.getElementAt(0));
            }
        }
        cboTrangThaiNhanVien.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setText(trangThaiDisplay(value != null ? value.toString() : ""));
                return this;
            }
        });
    }

    // ---- Private helpers ----

    private void loadFields() {
        if (ttcn == null) return;
        txtHoTen.setText(safe(ttcn.getHoTen()));
        txtNgaySinh.setText(ttcn.getNgaySinh() != null ? ttcn.getNgaySinh().format(DATE_FMT) : "");
        cboGioiTinh.setSelectedItem(ttcn.getGioiTinh() != null ? ttcn.getGioiTinh() : "nam");
        txtCCCD.setText(safe(ttcn.getCccd()));
        txtDienThoai.setText(safe(ttcn.getDienThoai()));
        txtEmail.setText(safe(ttcn.getEmail()));
        txtDiaChi.setText(safe(ttcn.getDiaChi()));
        txtDiaChiThuongTru.setText(safe(ttcn.getDiaChiThuongTru()));
        txtQueQuan.setText(safe(ttcn.getQueQuan()));
        cboTinhTrangHonNhan.setSelectedItem(ttcn.getTinhTrangHonNhan() != null ? ttcn.getTinhTrangHonNhan() : "doc_than");
        txtTrinhDoHocVan.setText(safe(ttcn.getTrinhDoHocVan()));
        txtFileCV.setText(safe(ttcn.getFileCV()));
        txtKinhNghiem.setText(safe(ttcn.getKinhNghiem()));
    }

    private JTextField createReadOnlyTextField() {
        JTextField f = new JTextField();
        f.setFont(com.hrm.util.UIFonts.TEXT_NORMAL);
        f.setEditable(false);
        f.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
        f.setBackground(UIColors.WHITE);
        return f;
    }

    private JTextArea createReadOnlyTextArea() {
        JTextArea a = new JTextArea(3, 24);
        a.setFont(com.hrm.util.UIFonts.TEXT_NORMAL);
        a.setLineWrap(true);
        a.setWrapStyleWord(true);
        a.setEditable(false);
        a.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
        a.setBackground(UIColors.WHITE);
        return a;
    }

    private void applyVisual(JTextComponent c, Color bg, Border border) {
        c.setBackground(bg); c.setBorder(border);
    }

    private JLabel buildSectionTitle(String text) {
        JLabel lbl = new JLabel(text.toUpperCase()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(UIColors.PRIMARY_PURPLE);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawLine(0, getHeight() - 2, getWidth(), getHeight() - 2);
            }
        };
        lbl.setFont(com.hrm.util.UIFonts.BOLD_NORMAL);
        lbl.setForeground(UIColors.PRIMARY_PURPLE);
        lbl.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private JPanel buildInfoGrid() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(UIColors.WHITE);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        return p;
    }

    private void addInfoRow(JPanel grid, int row, String labelText, String valueText) {
        JLabel val = new JLabel(valueText);
        val.setFont(com.hrm.util.UIFonts.TEXT_NORMAL);
        val.setForeground(UIColors.TEXT_DARK);
        addInfoRow(grid, row, labelText, (Component) val);
    }

    private void addInfoRow(JPanel grid, int row, String labelText, Component valueComponent) {
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(com.hrm.util.UIFonts.BOLD_NORMAL);
        lbl.setForeground(UIColors.TEXT_GRAY);
        lbl.setPreferredSize(new Dimension(170, 24));

        GridBagConstraints gl = new GridBagConstraints();
        gl.gridx = 0; gl.gridy = row; gl.anchor = GridBagConstraints.WEST;
        gl.insets = new Insets(3, 0, 3, 12); gl.fill = GridBagConstraints.NONE;

        GridBagConstraints gv = new GridBagConstraints();
        gv.gridx = 1; gv.gridy = row; gv.anchor = GridBagConstraints.WEST;
        gv.insets = new Insets(3, 0, 3, 0); gv.fill = GridBagConstraints.HORIZONTAL; gv.weightx = 1.0;

        grid.add(lbl, gl);
        grid.add(valueComponent, gv);
    }

    private GridBagConstraints buildGbc(int col, int row, int colspan) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = col; gbc.gridy = row; gbc.gridwidth = colspan;
        gbc.anchor = GridBagConstraints.WEST; gbc.insets = new Insets(4, 0, 4, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        return gbc;
    }

    private JLabel buildStatusLabel(String statusKey, String displayText) {
        Color bg = resolveColor(statusKey);
        JLabel lbl = new JLabel("  " + safe(displayText) + "  ") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground()); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose(); super.paintComponent(g);
            }
        };
        lbl.setOpaque(false); lbl.setFont(com.hrm.util.UIFonts.BOLD_SMALL);
        lbl.setForeground(UIColors.WHITE); lbl.setBackground(bg);
        lbl.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
        return lbl;
    }

    static Color resolveColor(String key) {
        if (key == null) return UIColors.TEXT_GRAY;
        return switch (key) {
            case "dang_lam_viec", "hieu_luc", "dung_gio" -> UIColors.SUCCESS_GREEN;
            case "tam_nghi", "cho_duyet", "di_muon", "ve_som" -> UIColors.WARNING_TEXT_AMBER;
            case "nghi_viec", "tu_choi", "huy", "vang_mat"   -> UIColors.DANGER_RED;
            case "het_han", "het_hieu_luc"                    -> UIColors.TEXT_GRAY;
            case "thanh_ly"   -> new Color(23, 162, 184);
            case "nghi_phep"  -> new Color(0, 123, 200);
            case "cong_tac"   -> new Color(100, 60, 200);
            default           -> UIColors.TEXT_GRAY;
        };
    }

    private String trangThaiDisplay(String key) {
        return switch (key) {
            case "dang_lam_viec" -> "Đang làm việc";
            case "tam_nghi"      -> "Tạm nghỉ";
            case "nghi_viec"     -> "Nghỉ việc";
            default -> key;
        };
    }

    private boolean eq(String a, String b) {
        String na = a == null ? "" : a.trim();
        String nb = b == null ? "" : b.trim();
        return na.equals(nb);
    }

    private static String safe(String s) { return (s != null && !s.isEmpty()) ? s : ""; }
    private static String empty(String s) { return (s == null || s.isEmpty()) ? null : s; }
}
