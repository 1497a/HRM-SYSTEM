package com.hrm.gui.employee;

import com.hrm.bus.KetQua;
import com.hrm.model.HopDongLaoDong;
import com.hrm.model.NhanVien;
import com.hrm.model.ThongTinCaNhan;
import com.hrm.util.HRMConstants;
import com.hrm.util.UIColors;
import com.hrm.util.ValidationUtils;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.text.JTextComponent;

import java.awt.*;
import java.awt.Font;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Tab 1 của EmployeeDetailPanel: Thông tin cá nhân + hợp đồng.
 * Expose public methods để EmployeeDetailPanel gọi: setEditMode, setStatusEditMode,
 * hasChanges, save, getTrangThai.
 */
class TabThongTinCaNhanPanel extends JPanel {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String[] STATUS_OPTIONS_ALL = {"dang_lam_viec", "tam_nghi", "nghi_viec"};
    private final String maNV;
    private ThongTinCaNhan ttcn;
    private JTextField txtHoTen;
    private JTextField txtNgaySinh;
    private JTextField txtCCCD;
    private JTextField txtDienThoai;
    private JTextField txtEmail;
    private JTextField txtDiaChi;
    private JTextField txtQueQuan;
    private JTextField txtTrinhDoHocVan;
    private JTextField txtFileCV;
    private JTextArea txtKinhNghiem;
    private JComboBox<String> cboGioiTinh;
    private JComboBox<String> cboTinhTrangHonNhan;
    JComboBox<String> cboTrangThaiNhanVien;
    TabThongTinCaNhanPanel(String maNV, NhanVien nhanVien, ThongTinCaNhan ttcn,
                           HopDongLaoDong hopDong, boolean canViewContract) {
        this.maNV = maNV;
        this.ttcn = ttcn;
        setLayout(new BorderLayout());
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(Color.WHITE);
        content.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        content.add(buildSectionTitle("Thông tin nhân viên"));
        content.add(Box.createVerticalStrut(8));
        JPanel nvPanel = buildInfoGrid();
        addInfoRow(nvPanel, 0, "Mã nhân viên:", nhanVien != null ? safe(nhanVien.getMaNhanVien()) : "");
        addInfoRow(nvPanel, 1, "Loại hợp đồng:", nhanVien != null ? HRMConstants.display(nhanVien.getLoaiHopDong()) : "");
        addInfoRow(nvPanel, 2, "Ngày vào làm:",
                nhanVien != null && nhanVien.getNgayVaoLam() != null
                        ? nhanVien.getNgayVaoLam().format(DATE_FMT) : "");
        cboTrangThaiNhanVien = new JComboBox<>(new String[]{HRMConstants.TRANG_THAI_DANG_LAM_VIEC, HRMConstants.TRANG_THAI_TAM_NGHI, HRMConstants.TRANG_THAI_NGHI_VIEC});
        cboTrangThaiNhanVien.setFont(com.hrm.util.UIFonts.TEXT_NORMAL);
        cboTrangThaiNhanVien.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setText(HRMConstants.display(value != null ? value.toString() : ""));
                return this;
            }
        });
        cboTrangThaiNhanVien.setSelectedItem(nhanVien != null ? nhanVien.getTrangThai() : HRMConstants.TRANG_THAI_DANG_LAM_VIEC);
        setStatusEditMode(false);
        addInfoRow(nvPanel, 3, "Trạng thái:", cboTrangThaiNhanVien);
        content.add(nvPanel);
        content.add(Box.createVerticalStrut(16));
        content.add(buildSectionTitle("Thông tin cá nhân"));
        content.add(Box.createVerticalStrut(8));
        JPanel ttcnPanel = buildInfoGrid();
        if (ttcn != null) {
            txtHoTen = createReadOnlyTextField();
            txtNgaySinh = createReadOnlyTextField();
            cboGioiTinh = new JComboBox<>(new String[]{HRMConstants.GIOI_TINH_NAM, HRMConstants.GIOI_TINH_NU, HRMConstants.GIOI_TINH_KHAC});
            cboGioiTinh.setRenderer(new DisplayValueRenderer());
            txtCCCD = createReadOnlyTextField();
            txtDienThoai = createReadOnlyTextField();
            txtEmail = createReadOnlyTextField();
            txtDiaChi = createReadOnlyTextField();
            txtQueQuan = createReadOnlyTextField();
            txtTrinhDoHocVan = createReadOnlyTextField();
            cboTinhTrangHonNhan = new JComboBox<>(new String[]{HRMConstants.HON_NHAN_DOC_THAN, HRMConstants.HON_NHAN_DA_KET_HON, HRMConstants.HON_NHAN_LY_HON});
            cboTinhTrangHonNhan.setRenderer(new DisplayValueRenderer());
            txtFileCV = createReadOnlyTextField();
            txtKinhNghiem = createReadOnlyTextArea();
            addInfoRow(ttcnPanel, 0, "Họ và tên:", txtHoTen);
            addInfoRow(ttcnPanel, 1, "Ngày sinh:", txtNgaySinh);
            addInfoRow(ttcnPanel, 2, "Giới tính:", cboGioiTinh);
            addInfoRow(ttcnPanel, 3, "CCCD:", txtCCCD);
            addInfoRow(ttcnPanel, 4, "Số điện thoại:", txtDienThoai);
            addInfoRow(ttcnPanel, 5, "Email:", txtEmail);
            addInfoRow(ttcnPanel, 6, "Địa chỉ:", txtDiaChi);
            addInfoRow(ttcnPanel, 7, "Quê quán:", txtQueQuan);
            addInfoRow(ttcnPanel, 8, "Tình trạng hôn nhân:", cboTinhTrangHonNhan);
            addInfoRow(ttcnPanel, 9, "Trình độ học vấn:", txtTrinhDoHocVan);
            addInfoRow(ttcnPanel, 10, "File CV:", txtFileCV);
            addInfoRow(ttcnPanel, 11, "Kinh nghiệm:", new JScrollPane(txtKinhNghiem));
            loadFields();
            setEditMode(false);
        } else {
            JLabel noData = new JLabel("  Không có thông tin cá nhân.");
            noData.setFont(new Font("Segoe UI", Font.ITALIC, 13));
            noData.setForeground(Color.GRAY);
            ttcnPanel.add(noData, buildGbc(0, 0, 2));
        }
        content.add(ttcnPanel);
        content.add(Box.createVerticalStrut(16));
        content.add(buildSectionTitle("Hợp đồng lao động"));
        content.add(Box.createVerticalStrut(8));
        JPanel hdPanel = buildInfoGrid();
        if (!canViewContract) {
            JLabel noPermission = new JLabel("  Bạn không có quyền xem hợp đồng của nhân viên này.");
            noPermission.setFont(new Font("Segoe UI", Font.ITALIC, 13));
            noPermission.setForeground(Color.GRAY);
            hdPanel.add(noPermission, buildGbc(0, 0, 2));
        } else if (hopDong != null) {
            addInfoRow(hdPanel, 0, "Số hợp đồng:", safe(hopDong.getSoHopDong()));
            addInfoRow(hdPanel, 1, "Loại hợp đồng:", HRMConstants.display(hopDong.getLoaiHopDong()));
            addInfoRow(hdPanel, 2, "Ngày ký:",
                    hopDong.getNgayKy() != null ? hopDong.getNgayKy().format(DATE_FMT) : "");
            addInfoRow(hdPanel, 3, "Hiệu lực từ:",
                    hopDong.getNgayHieuLuc() != null ? hopDong.getNgayHieuLuc().format(DATE_FMT) : "");
            addInfoRow(hdPanel, 4, "Hiệu lực đến:",
                    hopDong.getNgayHetHieuLuc() != null ? hopDong.getNgayHetHieuLuc().format(DATE_FMT) : "Không xác định");
            addInfoRow(hdPanel, 5, "Trạng thái:",
                    buildStatusLabel(hopDong.getTrangThai(), HRMConstants.display(hopDong.getTrangThai())));
        } else {
            JLabel noHd = new JLabel("  Chưa có hợp đồng hiệu lực.");
            noHd.setFont(new Font("Segoe UI", Font.ITALIC, 13));
            noHd.setForeground(Color.GRAY);
            hdPanel.add(noHd, buildGbc(0, 0, 2));
        }
        content.add(hdPanel);
        content.add(Box.createVerticalGlue());
        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);
    }

    void setEditMode(boolean editable) {
        if (ttcn == null) return;
        txtHoTen.setEditable(editable);
        txtNgaySinh.setEditable(editable);
        cboGioiTinh.setEnabled(editable);
        txtCCCD.setEditable(editable);
        txtDienThoai.setEditable(editable);
        txtEmail.setEditable(editable);
        txtDiaChi.setEditable(editable);
        txtQueQuan.setEditable(editable);
        txtTrinhDoHocVan.setEditable(editable);
        txtFileCV.setEditable(editable);
        txtKinhNghiem.setEditable(editable);
        Color bg = editable ? Color.WHITE : new Color(242, 242, 242);
        Border border = editable
                ? BorderFactory.createLineBorder(UIColors.PRIMARY_PURPLE, 1)
                : BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1);
        applyVisual(txtHoTen, bg, border);
        applyVisual(txtNgaySinh, bg, border);
        applyVisual(txtCCCD, bg, border);
        applyVisual(txtDienThoai, bg, border);
        applyVisual(txtEmail, bg, border);
        applyVisual(txtDiaChi, bg, border);
        applyVisual(txtQueQuan, bg, border);
        applyVisual(txtTrinhDoHocVan, bg, border);
        applyVisual(txtFileCV, bg, border);
        applyVisual(txtKinhNghiem, bg, border);
        cboGioiTinh.setBackground(bg);
        cboGioiTinh.setBorder(border);
        cboTinhTrangHonNhan.setEnabled(editable);
        cboTinhTrangHonNhan.setBackground(bg);
        cboTinhTrangHonNhan.setBorder(border);
    }

    void setStatusEditMode(boolean editable) {
        if (cboTrangThaiNhanVien == null) return;
        cboTrangThaiNhanVien.setEnabled(editable);
        cboTrangThaiNhanVien.setBorder(editable
                ? BorderFactory.createLineBorder(UIColors.PRIMARY_PURPLE, 1)
                : BorderFactory.createEmptyBorder(1, 1, 1, 1));
        cboTrangThaiNhanVien.setBackground(editable ? Color.WHITE : Color.WHITE);
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
            || !eq(txtQueQuan.getText(), ttcn.getQueQuan())
            || !eq(txtTrinhDoHocVan.getText(), ttcn.getTrinhDoHocVan())
            || !eq(txtFileCV.getText(), ttcn.getFileCv())
            || !eq(txtKinhNghiem.getText(), ttcn.getKinhNghiem());
    }

    KetQua<ThongTinCaNhan> save() {
        if (ttcn == null) {
            ttcn = new ThongTinCaNhan();
            ttcn.setMaNV(maNV);
        }
        String hoTen = txtHoTen.getText().trim();
        if (hoTen.isEmpty()) return KetQua.error("Họ tên không được để trống.");
        String ngaySinhStr = txtNgaySinh.getText().trim();
        LocalDate ngaySinh = null;
        if (!ngaySinhStr.isEmpty()) {
            try {
                ngaySinh = LocalDate.parse(ngaySinhStr, DATE_FMT);
            } catch (DateTimeParseException e) {
                return KetQua.error("Ngày sinh không hợp lệ. Định dạng dd/MM/yyyy.");
            }
            String birthErr = ValidationUtils.validateBirthDate(ngaySinh);
            if (birthErr != null) return KetQua.error(birthErr);
        }
        String email = txtEmail.getText().trim();
        String emailErr = ValidationUtils.validateEmail(email);
        if (emailErr != null) return KetQua.error(emailErr);
        String sdt = txtDienThoai.getText().trim();
        String phoneErr = ValidationUtils.validatePhone(sdt);
        if (phoneErr != null) return KetQua.error(phoneErr);
        ttcn.setHoTen(hoTen);
        ttcn.setNgaySinh(ngaySinh);
        ttcn.setGioiTinh((String) cboGioiTinh.getSelectedItem());
        ttcn.setCccd(empty(txtCCCD.getText()));
        ttcn.setDienThoai(empty(sdt));
        ttcn.setEmail(empty(email));
        String diaChi = txtDiaChi.getText().trim();
        if (diaChi.isEmpty()) return KetQua.error("Địa chỉ không được để trống.");
        ttcn.setDiaChi(diaChi);
        ttcn.setQueQuan(empty(txtQueQuan.getText().trim()));
        ttcn.setTrinhDoHocVan(empty(txtTrinhDoHocVan.getText().trim()));
        ttcn.setFileCv(empty(txtFileCV.getText().trim()));
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
            cboTrangThaiNhanVien.setSelectedItem(exists ? currentValue : model.getElementAt(0));
        }
        cboTrangThaiNhanVien.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setText(HRMConstants.display(value != null ? value.toString() : ""));
                return this;
            }
        });
    }

    private void loadFields() {
        if (ttcn == null) return;
        txtHoTen.setText(safe(ttcn.getHoTen()));
        txtNgaySinh.setText(ttcn.getNgaySinh() != null ? ttcn.getNgaySinh().format(DATE_FMT) : "");
        cboGioiTinh.setSelectedItem(ttcn.getGioiTinh() != null ? ttcn.getGioiTinh() : HRMConstants.GIOI_TINH_NAM);
        txtCCCD.setText(safe(ttcn.getCccd()));
        txtDienThoai.setText(safe(ttcn.getDienThoai()));
        txtEmail.setText(safe(ttcn.getEmail()));
        txtDiaChi.setText(safe(ttcn.getDiaChi()));
        txtQueQuan.setText(safe(ttcn.getQueQuan()));
        txtTrinhDoHocVan.setText(safe(ttcn.getTrinhDoHocVan()));
        txtFileCV.setText(safe(ttcn.getFileCv()));
        txtKinhNghiem.setText(safe(ttcn.getKinhNghiem()));
    }

    private JTextField createReadOnlyTextField() {
        JTextField f = new JTextField();
        f.setFont(com.hrm.util.UIFonts.TEXT_NORMAL);
        f.setEditable(false);
        f.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
        f.setBackground(Color.WHITE);
        return f;
    }

    private JTextArea createReadOnlyTextArea() {
        JTextArea a = new JTextArea(3, 24);
        a.setFont(com.hrm.util.UIFonts.TEXT_NORMAL);
        a.setLineWrap(true);
        a.setWrapStyleWord(true);
        a.setEditable(false);
        a.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
        a.setBackground(Color.WHITE);
        return a;
    }

    private void applyVisual(JTextComponent c, Color bg, Border border) {
        c.setBackground(bg);
        c.setBorder(border);
    }

    private JLabel buildSectionTitle(String text) {
        JLabel lbl = new JLabel(text.toUpperCase()) {
            @Override
            protected void paintComponent(Graphics g) {
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
        p.setBackground(Color.WHITE);
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
        lbl.setForeground(Color.GRAY);
        lbl.setPreferredSize(new Dimension(170, 24));
        GridBagConstraints gl = new GridBagConstraints();
        gl.gridx = 0;
        gl.gridy = row;
        gl.anchor = GridBagConstraints.WEST;
        gl.insets = new Insets(3, 0, 3, 12);
        gl.fill = GridBagConstraints.NONE;
        GridBagConstraints gv = new GridBagConstraints();
        gv.gridx = 1;
        gv.gridy = row;
        gv.anchor = GridBagConstraints.WEST;
        gv.insets = new Insets(3, 0, 3, 0);
        gv.fill = GridBagConstraints.HORIZONTAL;
        gv.weightx = 1.0;
        grid.add(lbl, gl);
        grid.add(valueComponent, gv);
    }

    private GridBagConstraints buildGbc(int col, int row, int colspan) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = col;
        gbc.gridy = row;
        gbc.gridwidth = colspan;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(4, 0, 4, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        return gbc;
    }

    private JLabel buildStatusLabel(String statusKey, String displayText) {
        Color bg = resolveColor(statusKey);
        JLabel lbl = new JLabel("  " + safe(displayText) + "  ") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lbl.setOpaque(false);
        lbl.setFont(com.hrm.util.UIFonts.BOLD_SMALL);
        lbl.setForeground(Color.WHITE);
        lbl.setBackground(bg);
        lbl.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
        return lbl;
    }

    static Color resolveColor(String key) {
        if (key == null) return Color.GRAY;
        return switch (key) {
            case HRMConstants.TRANG_THAI_DANG_LAM_VIEC, HRMConstants.TRANG_THAI_HIEU_LUC, "dung_gio" -> UIColors.SUCCESS_GREEN;
            case HRMConstants.TRANG_THAI_TAM_NGHI, HRMConstants.TRANG_THAI_CHO_DUYET, "di_muon", "ve_som" -> UIColors.WARNING_ORANGE;
            case HRMConstants.TRANG_THAI_NGHI_VIEC, HRMConstants.TRANG_THAI_TU_CHOI, HRMConstants.TRANG_THAI_HUY, "vang_mat" -> UIColors.DANGER_RED;
            case HRMConstants.TRANG_THAI_HET_HAN, HRMConstants.TRANG_THAI_HET_HIEU_LUC -> Color.GRAY;
            case HRMConstants.TRANG_THAI_THANH_LY -> UIColors.INFO_TEAL;
            case "nghi_phep" -> UIColors.LEAVE_BLUE;
            case "cong_tac" -> UIColors.TRIP_PURPLE;
            default -> Color.GRAY;
        };
    }

    private boolean eq(String a, String b) {
        String na = a == null ? "" : a.trim();
        String nb = b == null ? "" : b.trim();
        return na.equals(nb);
    }

    private static class DisplayValueRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            setText(HRMConstants.display((String) value));
            return this;
        }
    }

    private static String safe(String s) {
        return (s != null && !s.isEmpty()) ? s : "";
    }

    private static String empty(String s) {
        return (s == null || s.isEmpty()) ? null : s;
    }

}
