package com.hrm.gui.contract;

import com.hrm.gui.components.BaseFormDialog;
import com.hrm.gui.components.PurpleButton;
import com.hrm.model.HopDongLaoDong;
import com.hrm.model.NhanVien;
import com.hrm.bus.HopDongBUS;
import com.hrm.bus.NhanVienBUS;
import com.hrm.bus.KetQua;
import com.hrm.util.HRMConstants;
import com.hrm.util.PermissionCodes;
import com.hrm.util.UIFonts;
import com.hrm.util.UIColors;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.Font;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.ZoneId;
import java.util.Date;
import java.util.Locale;

/**
 * Dialog tạo/xem hợp đồng lao động.
 */
public class ContractFormDialog extends BaseFormDialog {

    private boolean saved = false;
    // Mode
    private final HopDongLaoDong hopDongHienThi;
    // Form fields
    private JLabel lblSoHopDong;
    private JComboBox<NhanVien> cboNhanVien;
    private JComboBox<String> cboLoaiHopDong;
    private JFormattedTextField txtLuongCoSo;
    private JSpinner spnNgayKy;
    private JSpinner spnNgayHieuLuc;
    private JSpinner spnNgayHetHieuLuc;
    private JTextArea txtGhiChu;
    private JCheckBox chkKhongXacDinhNgayHet;
    // Audit fields (chỉ hiển thị trong view mode)
    private JPanel pnlAudit;
    // Buttons
    private PurpleButton btnLuu;
    private PurpleButton btnPheDuyet;
    private PurpleButton btnThanhLy;
    private JButton btnHuy;
    /**
     * Constructor tạo mới hợp đồng.
     */
    public ContractFormDialog(Frame parent) {
        this(parent, null);
    }

    /**
     * Constructor xem/hiển thị hợp đồng đã có.
     */
    public ContractFormDialog(Frame parent, HopDongLaoDong hopDong) {
        super(parent,
              hopDong == null ? "Tạo Hợp Đồng Lao Động" : "Chi Tiết Hợp Đồng #" + hopDong.getMaHopDong(),
              true);
        this.hopDongHienThi = hopDong;
        initComponents();
        layoutComponents();
        if (hopDong != null) {
            fillForm(hopDong);
            setReadOnly();
        }
        pack();
        setMinimumSize(new Dimension(520, 580));
        setLocationRelativeTo(parent);
    }

    // ============================
    // Init components
    // ============================
    private void initComponents() {
        // Số hợp đồng - tự động tạo
        lblSoHopDong = new JLabel("(Tự động tạo khi lưu)");
        lblSoHopDong.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        lblSoHopDong.setForeground(Color.GRAY);
        // Nhân viên từ danh sách
        cboNhanVien = new JComboBox<>();
        cboNhanVien.setFont(UIFonts.TEXT_NORMAL);
        java.util.List<NhanVien> dsNV = NhanVienBUS.getInstance().getDangLamViec();
        for (NhanVien nv : dsNV) {
            cboNhanVien.addItem(nv);
        }
        cboNhanVien.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof NhanVien) {
                    NhanVien nv = (NhanVien) value;
                    setText("[" + nv.getMaNhanVien() + "] - " + (nv.getHoTen() != null ? nv.getHoTen() : ""));
                }
                return this;
            }
        });
        // Loại hợp đồng
        cboLoaiHopDong = new JComboBox<>(new String[]{
            HRMConstants.LOAI_HOP_DONG_THU_VIEC, HRMConstants.LOAI_HOP_DONG_XAC_DINH, HRMConstants.LOAI_HOP_DONG_KHONG_XAC_DINH
        });
        cboLoaiHopDong.setFont(UIFonts.TEXT_NORMAL);
        cboLoaiHopDong.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value != null) setText(HRMConstants.display(value.toString()));
                return this;
            }
        });
        cboLoaiHopDong.addActionListener(e -> onLoaiHopDongChanged());
        // Lương cơ sở
        NumberFormat fmt = NumberFormat.getIntegerInstance(new Locale("vi", "VN"));
        fmt.setGroupingUsed(true);
        txtLuongCoSo = new JFormattedTextField(fmt);
        txtLuongCoSo.setFont(UIFonts.TEXT_NORMAL);
        txtLuongCoSo.setColumns(20);
        txtLuongCoSo.setValue(0L);
        // Ngày ký
        SpinnerDateModel ngayKyModel = new SpinnerDateModel(new Date(), null, null, java.util.Calendar.DAY_OF_MONTH);
        spnNgayKy = new JSpinner(ngayKyModel);
        spnNgayKy.setEditor(new JSpinner.DateEditor(spnNgayKy, "dd/MM/yyyy"));
        spnNgayKy.setFont(UIFonts.TEXT_NORMAL);
        // Ngày hiệu lực
        SpinnerDateModel ngayHieuLucModel = new SpinnerDateModel(new Date(), null, null, java.util.Calendar.DAY_OF_MONTH);
        spnNgayHieuLuc = new JSpinner(ngayHieuLucModel);
        spnNgayHieuLuc.setEditor(new JSpinner.DateEditor(spnNgayHieuLuc, "dd/MM/yyyy"));
        spnNgayHieuLuc.setFont(UIFonts.TEXT_NORMAL);
        // Ngày hết hiệu lực
        SpinnerDateModel ngayHetModel = new SpinnerDateModel(new Date(), null, null, java.util.Calendar.DAY_OF_MONTH);
        spnNgayHetHieuLuc = new JSpinner(ngayHetModel);
        spnNgayHetHieuLuc.setEditor(new JSpinner.DateEditor(spnNgayHetHieuLuc, "dd/MM/yyyy"));
        spnNgayHetHieuLuc.setFont(UIFonts.TEXT_NORMAL);
        // Checkbox không xác định ngày hết hiệu lực
        chkKhongXacDinhNgayHet = new JCheckBox("Không xác định thời hạn kết thúc");
        chkKhongXacDinhNgayHet.setFont(com.hrm.util.UIFonts.TEXT_NORMAL);
        chkKhongXacDinhNgayHet.setOpaque(false);
        chkKhongXacDinhNgayHet.addActionListener(e ->
                spnNgayHetHieuLuc.setEnabled(!chkKhongXacDinhNgayHet.isSelected()));
        // Ghi chú
        txtGhiChu = new JTextArea(3, 20);
        txtGhiChu.setFont(UIFonts.TEXT_NORMAL);
        txtGhiChu.setLineWrap(true);
        txtGhiChu.setWrapStyleWord(true);
        // Buttons
        btnLuu = new PurpleButton("Lưu");
        btnPheDuyet = new PurpleButton("Phê duyệt");
        btnThanhLy = PurpleButton.warning("Thanh lý");
        btnHuy = new JButton("Hủy");
        btnHuy.setFont(UIFonts.TEXT_NORMAL);
        btnHuy.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLuu.addActionListener(e -> luuHopDong());
        btnHuy.addActionListener(e -> dispose());
        btnPheDuyet.addActionListener(e -> pheDuyetHopDong());
        btnThanhLy.addActionListener(e -> thanhLyHopDong());
        // Ẩn mặc định, setReadOnly() sẽ bật nếu đủ điều kiện
        btnPheDuyet.setVisible(false);
        btnThanhLy.setVisible(false);
        // Audit panel — chỉ hiển thị trong view mode
        pnlAudit = new JPanel(new GridLayout(0, 2, 8, 4));
        pnlAudit.setBackground(new Color(245, 245, 250));
        pnlAudit.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 220)),
                new EmptyBorder(8, 12, 8, 12)));
        pnlAudit.setVisible(false);
    }

    private void onLoaiHopDongChanged() {
        String loai = (String) cboLoaiHopDong.getSelectedItem();
        boolean isKhongXacDinh = HRMConstants.LOAI_HOP_DONG_KHONG_XAC_DINH.equals(loai);
        if (isKhongXacDinh) {
            chkKhongXacDinhNgayHet.setSelected(true);
            spnNgayHetHieuLuc.setEnabled(false);
        } else {
            chkKhongXacDinhNgayHet.setSelected(false);
            spnNgayHetHieuLuc.setEnabled(true);
        }
    }

    // ============================
    // Layout
    // ============================
    private void layoutComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(new EmptyBorder(16, 16, 16, 16));
        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                new EmptyBorder(16, 16, 16, 16)));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.anchor = GridBagConstraints.WEST;
        // Tiêu đề form
        JLabel lblTitle = new JLabel("THÔNG TIN HỢP ĐỒNG LAO ĐỘNG");
        lblTitle.setFont(com.hrm.util.UIFonts.HEADER_SUB);
        lblTitle.setForeground(UIColors.PRIMARY_PURPLE);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 8, 12, 8);
        formPanel.add(lblTitle, gbc);
        gbc.gridwidth = 1;
        gbc.insets = new Insets(6, 8, 6, 8);
        // Số hợp đồng (read-only label)
        addFormRow(formPanel, gbc, 1, "Số hợp đồng", lblSoHopDong);
        // Nhân viên
        addFormRow(formPanel, gbc, 2, "Nhân viên (*)", cboNhanVien);
        // Loại hợp đồng
        addFormRow(formPanel, gbc, 3, "Loại hợp đồng (*)", cboLoaiHopDong);
        // Lương cơ sở
        addFormRow(formPanel, gbc, 4, "Lương cơ sở (đ) (*)", txtLuongCoSo);
        // Ngày ký
        addFormRow(formPanel, gbc, 5, "Ngày ký (*)", spnNgayKy);
        // Ngày hiệu lực
        addFormRow(formPanel, gbc, 6, "Ngày hiệu lực (*)", spnNgayHieuLuc);
        // Row: Ngày hết hiệu lực + checkbox
        gbc.gridx = 0; gbc.gridy = 7;
        JLabel lblHetHieuLuc = new JLabel("Ngày hết hiệu lực:");
        lblHetHieuLuc.setFont(UIFonts.TEXT_NORMAL);
        lblHetHieuLuc.setForeground(UIColors.TEXT_DARK);
        formPanel.add(lblHetHieuLuc, gbc);
        gbc.gridx = 1; gbc.gridy = 7;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        JPanel ngayHetPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        ngayHetPanel.setOpaque(false);
        ngayHetPanel.add(spnNgayHetHieuLuc);
        ngayHetPanel.add(Box.createHorizontalStrut(8));
        ngayHetPanel.add(chkKhongXacDinhNgayHet);
        formPanel.add(ngayHetPanel, gbc);
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        // Ghi chú
        gbc.gridx = 0; gbc.gridy = 8;
        JLabel lblGhiChu = new JLabel("Ghi chú:");
        lblGhiChu.setFont(UIFonts.TEXT_NORMAL);
        lblGhiChu.setForeground(UIColors.TEXT_DARK);
        formPanel.add(lblGhiChu, gbc);
        gbc.gridx = 1; gbc.gridy = 8;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        JScrollPane scrollGhiChu = new JScrollPane(txtGhiChu);
        scrollGhiChu.setPreferredSize(new Dimension(280, 70));
        formPanel.add(scrollGhiChu, gbc);
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(pnlAudit, BorderLayout.NORTH);
        // Button panel
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setOpaque(false);
        btnPanel.setBorder(new EmptyBorder(8, 0, 0, 0));
        btnPanel.add(btnThanhLy);
        btnPanel.add(btnPheDuyet);
        btnPanel.add(btnHuy);
        btnPanel.add(btnLuu);
        mainPanel.add(btnPanel, BorderLayout.SOUTH);
        setContentPane(mainPanel);
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, int row, String label, JComponent field) {
        gbc.gridx = 0; gbc.gridy = row;
        JLabel lbl = new JLabel(label + ":");
        lbl.setFont(UIFonts.TEXT_NORMAL);
        lbl.setForeground(UIColors.TEXT_DARK);
        panel.add(lbl, gbc);
        gbc.gridx = 1; gbc.gridy = row;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        panel.add(field, gbc);
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
    }

    // ============================
    // Fill form (view mode)
    // ============================
    private void fillForm(HopDongLaoDong hd) {
        lblSoHopDong.setText(hd.getSoHopDong() != null ? hd.getSoHopDong() : "(chua co)");
        lblSoHopDong.setFont(com.hrm.util.UIFonts.TEXT_NORMAL);
        lblSoHopDong.setForeground(UIColors.TEXT_DARK);
        // Pre-select nhân viên
        for (int i = 0; i < cboNhanVien.getItemCount(); i++) {
            if (cboNhanVien.getItemAt(i).getMaNhanVien().equals(hd.getMaNV())) {
                cboNhanVien.setSelectedIndex(i);
                break;
            }
        }
        // Loại hợp đồng - combo now uses DB values directly
        cboLoaiHopDong.setSelectedItem(hd.getLoaiHopDong());
        txtLuongCoSo.setValue(hd.getLuongCoSo());
        if (hd.getNgayKy() != null) {
            spnNgayKy.setValue(Date.from(hd.getNgayKy().atStartOfDay(ZoneId.systemDefault()).toInstant()));
        }
        if (hd.getNgayHieuLuc() != null) {
            spnNgayHieuLuc.setValue(Date.from(hd.getNgayHieuLuc().atStartOfDay(ZoneId.systemDefault()).toInstant()));
        }
        if (hd.getNgayHetHieuLuc() != null) {
            spnNgayHetHieuLuc.setValue(Date.from(hd.getNgayHetHieuLuc().atStartOfDay(ZoneId.systemDefault()).toInstant()));
        } else {
            chkKhongXacDinhNgayHet.setSelected(true);
            spnNgayHetHieuLuc.setEnabled(false);
        }
        txtGhiChu.setText(hd.getNoiDung() != null ? hd.getNoiDung() : "");
        // Audit fields
        if (hd.getNguoiTao() != null || hd.getNguoiDuyet() != null) {
            pnlAudit.removeAll();
            DateTimeFormatter dtFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            addAuditRow("Người tạo:", hd.getNguoiTao() != null ? hd.getNguoiTao() : "(hệ thống)");
            addAuditRow("Người phê duyệt:", hd.getNguoiDuyet() != null ? hd.getNguoiDuyet() : "(chưa duyệt)");
            addAuditRow("Ngày phê duyệt:", hd.getNgayDuyet() != null ? hd.getNgayDuyet().format(dtFmt) : "—");
            pnlAudit.setVisible(true);
        }
    }

    private void addAuditRow(String labelText, String value) {
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(UIFonts.TEXT_NORMAL.deriveFont(Font.BOLD));
        lbl.setForeground(UIColors.TEXT_DARK);
        JLabel val = new JLabel(value);
        val.setFont(UIFonts.TEXT_NORMAL);
        val.setForeground(Color.DARK_GRAY);
        pnlAudit.add(lbl);
        pnlAudit.add(val);
    }

    private void setReadOnly() {
        cboNhanVien.setEnabled(false);
        cboLoaiHopDong.setEnabled(false);
        txtLuongCoSo.setEditable(false);
        spnNgayKy.setEnabled(false);
        spnNgayHieuLuc.setEnabled(false);
        spnNgayHetHieuLuc.setEnabled(false);
        chkKhongXacDinhNgayHet.setEnabled(false);
        txtGhiChu.setEditable(false);
        btnLuu.setVisible(false);
        setTitle("Chi Tiết Hợp Đồng");
        com.hrm.util.SessionContext sc = com.hrm.util.SessionContext.getInstance();
        String trangThai = hopDongHienThi.getTrangThai();
        btnPheDuyet.setVisible(sc.hasPermission(PermissionCodes.CONTRACT_APPROVE) && HRMConstants.TRANG_THAI_CHO_DUYET.equals(trangThai));
        btnThanhLy.setVisible(sc.hasPermission(PermissionCodes.CONTRACT_MANAGE)
                && (HRMConstants.TRANG_THAI_HIEU_LUC.equals(trangThai) || HRMConstants.TRANG_THAI_HET_HIEU_LUC.equals(trangThai)));
    }

    private void pheDuyetHopDong() {
        if (!showYesNo("Phê duyệt hợp đồng số '" + hopDongHienThi.getSoHopDong() + "'?\nHợp đồng sẽ có hiệu lực ngay.",
                "Xác nhận phê duyệt")) return;
        KetQua<Void> result = HopDongBUS.getInstance().pheDuyetHopDong(hopDongHienThi.getMaHopDong());
        if (result.isSuccess()) {
            showSuccess(result.getMessage());
            saved = true;
            dispose();
        } else {
            showError(result.getMessage());
        }
    }

    private void thanhLyHopDong() {
        String confirmMsg = "Xác nhận thanh lý hợp đồng số '" + hopDongHienThi.getSoHopDong() + "'?";
        if (HopDongBUS.getInstance().isHopDongHetHan(hopDongHienThi.getMaHopDong())) {
            confirmMsg = "Hợp đồng nay ĐÃ HẾT HẠN. Bạn có muốn thanh lý không?\n"
                    + "Số HD: " + hopDongHienThi.getSoHopDong() + "\n"
                    + "ưu ý: Các bổ nhiệm hiệu lực sẽ bị kết thúc.";
        }
        if (!showYesNo(confirmMsg, "Xác nhận thanh lý")) return;
        KetQua<Void> result = HopDongBUS.getInstance().thanhLyHopDong(hopDongHienThi.getMaHopDong());
        if (result.isSuccess()) {
            showSuccess(result.getMessage());
            saved = true;
            dispose();
        } else {
            showError(result.getMessage());
        }
    }

    // ============================
    // Save action
    // ============================
    private void luuHopDong() {
        // Validate nhân viên
        NhanVien selectedNV = (NhanVien) cboNhanVien.getSelectedItem();
        if (selectedNV == null) {
            showError("Vui lòng chọn nhân viên.", "Lỗi nhập liệu");
            cboNhanVien.requestFocus();
            return;
        }
        String maNV = selectedNV.getMaNhanVien();
        // Lương cơ sở
        long luongCoSo = 0;
        try {
            Object val = txtLuongCoSo.getValue();
            if (val instanceof Number) {
                luongCoSo = ((Number) val).longValue();
            }
        } catch (Exception ex) {
            luongCoSo = 0;
        }
        if (luongCoSo <= 0) {
            showError("Lương cơ sở phải lớn hơn 0.", "Lỗi nhập liệu");
            txtLuongCoSo.requestFocus();
            return;
        }
        Date ngayKyDate = (Date) spnNgayKy.getValue();
        Date ngayHieuLucDate = (Date) spnNgayHieuLuc.getValue();
        LocalDate ngayKy = ngayKyDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate ngayHieuLuc = ngayHieuLucDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        if (ngayKy.isAfter(ngayHieuLuc)) {
            showError("Ngày ký phải trước hoặc bằng Ngày hiệu lực.", "Lỗi nhập liệu");
            spnNgayKy.requestFocus();
            return;
        }
        // Ngày hết hiệu lực (nullable)
        LocalDate ngayHetHieuLuc = null;
        if (!chkKhongXacDinhNgayHet.isSelected()) {
            Date ngayHetDate = (Date) spnNgayHetHieuLuc.getValue();
            ngayHetHieuLuc = ngayHetDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            if (ngayHetHieuLuc.isBefore(ngayHieuLuc)) {
                showError("Ngày hết hiệu lực phải sau hoặc bằng Ngày hiệu lực.", "Lỗi nhập liệu");
                spnNgayHetHieuLuc.requestFocus();
                return;
            }
        }
        // Combo uses DB values directly
        String loaiHopDong = (String) cboLoaiHopDong.getSelectedItem();
        if (HRMConstants.LOAI_HOP_DONG_KHONG_XAC_DINH.equals(loaiHopDong)) {
            ngayHetHieuLuc = null; // Đảm bảo null
        }
        // Ghi chú
        String ghiChu = txtGhiChu.getText().trim();
        // Tạo HopDongLaoDong object (soHopDong tự động tạo trong service)
        HopDongLaoDong hd = new HopDongLaoDong();
        hd.setMaNV(maNV);
        hd.setLoaiHopDong(loaiHopDong);
        hd.setLuongCoSo(luongCoSo);
        hd.setNgayKy(ngayKy);
        hd.setNgayHieuLuc(ngayHieuLuc);
        hd.setNgayHetHieuLuc(ngayHetHieuLuc);
        hd.setNoiDung(ghiChu.isEmpty() ? null : ghiChu);
        // Gọi service
        KetQua<HopDongLaoDong> result = HopDongBUS.getInstance().taoHopDong(hd);
        if (result.isSuccess()) {
            showSuccess(result.getMessage());
            saved = true;
            dispose();
        } else {
            showError(result.getMessage());
        }
    }

    // ============================
    // Getter
    // ============================
    public boolean isSaved() {
        return saved;
    }
}
