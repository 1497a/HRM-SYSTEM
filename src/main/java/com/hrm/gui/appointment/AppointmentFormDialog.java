package com.hrm.gui.appointment;

import com.hrm.gui.components.BaseFormDialog;
import com.hrm.gui.components.PurpleButton;
import com.hrm.model.BoNhiem;
import com.hrm.model.PhongBan;
import com.hrm.model.NhanVien;
import com.hrm.model.ChucVu;
import com.hrm.bus.BoNhiemBUS;
import com.hrm.bus.ChucVuBUS;
import com.hrm.bus.NhanVienBUS;
import com.hrm.bus.KetQua;
import com.hrm.bus.PhongBanBUS;
import com.hrm.util.HRMConstants;
import com.hrm.util.UIFonts;
import com.hrm.util.PermissionCodes;
import com.hrm.util.SessionContext;
import com.hrm.util.UIColors;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

/**
 * 
 * Dialog tạo/xem bổ nhiệm nhân viên.
 * 
 */
public class AppointmentFormDialog extends BaseFormDialog {

    private boolean saved = false;
    private boolean actionTaken = false;
    // Mode: view (read-only khi có boNhiem)
    private final BoNhiem boNhiemHienThi;
    // Services
    private final PhongBanBUS departmentService = new PhongBanBUS();
    private final ChucVuBUS positionService = new ChucVuBUS();
    // Form fields
    private JComboBox<NhanVien> cboNhanVien;
    private JComboBox<PhongBan> cboPhongBan;
    private JComboBox<ChucVu> cboChucVu;
    private JComboBox<String> cboLoaiBoNhiem;
    private JComboBox<Object> cboQuanLy; // cap tren truc tiep (nullable)
    private JSpinner spnTyLe;
    private JSpinner spnTuNgay;
    private JTextArea txtGhiChu;
    private JTextField txtDenNgay;
    private JTextField txtNguoiDuyet;
    private JTextField txtNgayDuyet;
    // Buttons
    private PurpleButton btnLuu;
    private JButton btnHuy;
    /**
     * 
     * Constructor tạo mới bổ nhiệm.
     * 
     */
    public AppointmentFormDialog(Frame parent) {
        this(parent, null);
    }

    /**
     * 
     * Constructor xem/hiển thị bổ nhiệm đã có.
     * 
     */
    public AppointmentFormDialog(Frame parent, BoNhiem boNhiem) {
        super(parent, boNhiem == null ? "Tạo Bổ Nhiệm Mới" : "Chi Tiết Bổ Nhiệm #" + boNhiem.getId(), true);
        this.boNhiemHienThi = boNhiem;
        initComponents();
        layoutComponents();
        if (boNhiem != null) {
            fillForm(boNhiem);
            setReadOnly();
        }
        pack();
        setMinimumSize(new Dimension(480, 520));
        setLocationRelativeTo(parent);
    }

    // ============================
    // Init components
    // ============================
    private void initComponents() {
        // Nhân viên
        cboNhanVien = new JComboBox<>();
        cboNhanVien.setFont(UIFonts.TEXT_NORMAL);
        loadNhanVien();
        // Phòng ban
        cboPhongBan = new JComboBox<>();
        cboPhongBan.setFont(UIFonts.TEXT_NORMAL);
        loadDepartments();
        // Chức vụ
        cboChucVu = new JComboBox<>();
        cboChucVu.setFont(UIFonts.TEXT_NORMAL);
        loadPositions();
        // Cấp trên trực tiếp
        cboQuanLy = new JComboBox<>();
        cboQuanLy.setFont(UIFonts.TEXT_NORMAL);
        loadQuanLy();
        // Loại bổ nhiệm
        // DB ENUM: 'chinh', 'kiem_nhiem'
        cboLoaiBoNhiem = new JComboBox<>(new String[] { HRMConstants.LOAI_BO_NHIEM_CHINH, HRMConstants.LOAI_BO_NHIEM_KIEM_NHIEM });
        cboLoaiBoNhiem.setFont(UIFonts.TEXT_NORMAL);
        cboLoaiBoNhiem.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value != null) setText(HRMConstants.display(value.toString()));
                return this;
            }
        });
        // Tỷ lệ hưởng lương (0-100%)
        SpinnerNumberModel tyLeModel = new SpinnerNumberModel(100, 0, 100, 1);
        spnTyLe = new JSpinner(tyLeModel);
        spnTyLe.setFont(UIFonts.TEXT_NORMAL);
        spnTyLe.setPreferredSize(new Dimension(80, 32));
        // Từ ngày
        SpinnerDateModel tuNgayModel = new SpinnerDateModel(new Date(), null, null, java.util.Calendar.DAY_OF_MONTH);
        spnTuNgay = new JSpinner(tuNgayModel);
        spnTuNgay.setEditor(new JSpinner.DateEditor(spnTuNgay, "dd/MM/yyyy"));
        spnTuNgay.setFont(UIFonts.TEXT_NORMAL);
        // Ghi chú
        txtGhiChu = new JTextArea(3, 20);
        txtGhiChu.setFont(UIFonts.TEXT_NORMAL);
        txtGhiChu.setLineWrap(true);
        txtGhiChu.setWrapStyleWord(true);
        txtDenNgay = new JTextField();
        txtDenNgay.setFont(UIFonts.BOLD_NORMAL);
        txtDenNgay.setEditable(false);
        txtDenNgay.setBackground(Color.WHITE);
        // Người duyệt, Ngày duyệt
        txtNguoiDuyet = new JTextField();
        txtNguoiDuyet.setFont(UIFonts.BOLD_NORMAL);
        txtNguoiDuyet.setEditable(false);
        txtNguoiDuyet.setBackground(Color.WHITE);
        txtNgayDuyet = new JTextField();
        txtNgayDuyet.setFont(UIFonts.BOLD_NORMAL);
        txtNgayDuyet.setEditable(false);
        txtNgayDuyet.setBackground(Color.WHITE);
        // Buttons
        btnLuu = new PurpleButton("Lưu");
        btnHuy = new JButton("Hủy");
        btnHuy.setFont(UIFonts.TEXT_NORMAL);
        btnHuy.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLuu.addActionListener(e -> luuBoNhiem());
        btnHuy.addActionListener(e -> dispose());
    }

    private void loadDepartments() {
        List<PhongBan> departments = departmentService.getActiveDepartments();
        for (PhongBan dept : departments) {
            cboPhongBan.addItem(dept);
        }
        cboPhongBan.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof PhongBan) {
                    setText(((PhongBan) value).getTenPhongBan());
                }
                return this;
            }
        });
    }

    private void loadPositions() {
        List<ChucVu> positions = positionService.getActivePositions();
        for (ChucVu pos : positions) {
            cboChucVu.addItem(pos);
        }
        cboChucVu.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof ChucVu) {
                    setText(((ChucVu) value).getTenChucVu());
                }
                return this;
            }
        });
    }

    private void loadNhanVien() {
        List<NhanVien> list = NhanVienBUS.getInstance().getDangLamViec();
        for (NhanVien nv : list) {
            cboNhanVien.addItem(nv);
        }
        DefaultListCellRenderer nvRenderer = new DefaultListCellRenderer() {
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
        };
        cboNhanVien.setRenderer(nvRenderer);
    }

    private void loadQuanLy() {
        cboQuanLy.addItem("(Không có)");
        List<NhanVien> list = NhanVienBUS.getInstance().getDangLamViec();
        for (NhanVien nv : list) {
            cboQuanLy.addItem(nv);
        }
        cboQuanLy.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof NhanVien) {
                    NhanVien nv = (NhanVien) value;
                    setText("[" + nv.getMaNhanVien() + "] - " + (nv.getHoTen() != null ? nv.getHoTen() : ""));
                } else if (value != null) {
                    setText(value.toString());
                }
                return this;
            }
        });
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
        JLabel lblTitle = new JLabel("THÔNG TIN BỔ NHIỆM");
        lblTitle.setFont(com.hrm.util.UIFonts.HEADER_SUB);
        lblTitle.setForeground(UIColors.PRIMARY_PURPLE);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 8, 12, 8);
        formPanel.add(lblTitle, gbc);
        lblTitle.setVisible(false);
        gbc.gridwidth = 1;
        gbc.insets = new Insets(6, 8, 6, 8);
        // Nhân viên
        addFormRow(formPanel, gbc, 1, "Nhân viên (*)", cboNhanVien);
        // Phòng ban
        addFormRow(formPanel, gbc, 2, "Phòng ban (*)", cboPhongBan);
        // Chức vụ
        addFormRow(formPanel, gbc, 3, "Chức vụ (*)", cboChucVu);
        // Cấp trên trực tiếp
        addFormRow(formPanel, gbc, 4, "Cấp trên trực tiếp", cboQuanLy);
        // Loại bổ nhiệm
        addFormRow(formPanel, gbc, 5, "Loại bổ nhiệm (*)", cboLoaiBoNhiem);
        // Tỷ lệ lương
        addFormRow(formPanel, gbc, 6, "Tỷ lệ hưởng lương (%)", spnTyLe);
        // Từ ngày
        addFormRow(formPanel, gbc, 7, "Từ ngày (*)", spnTuNgay);
        // Ghi chú
        gbc.gridx = 0;
        gbc.gridy = 8;
        JLabel lblGhiChu = new JLabel("Ghi chú:");
        lblGhiChu.setFont(UIFonts.TEXT_NORMAL);
        lblGhiChu.setForeground(UIColors.TEXT_DARK);
        formPanel.add(lblGhiChu, gbc);
        gbc.gridx = 1;
        gbc.gridy = 8;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        JScrollPane scrollGhiChu = new JScrollPane(txtGhiChu);
        scrollGhiChu.setPreferredSize(new Dimension(240, 70));
        formPanel.add(scrollGhiChu, gbc);
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        // Chỉ hiển thị người duyệt / ngày duyệt nếu có boNhiemHienThi
        if (boNhiemHienThi != null) {
            addFormRow(formPanel, gbc, 11, "Ngày kết thúc", txtDenNgay);
            addFormRow(formPanel, gbc, 9, "Người duyệt", txtNguoiDuyet);
            addFormRow(formPanel, gbc, 10, "Ngày duyệt", txtNgayDuyet);
        }
        mainPanel.add(formPanel, BorderLayout.CENTER);
        // Button panel
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setOpaque(false);
        btnPanel.setBorder(new EmptyBorder(8, 0, 0, 0));
        btnPanel.add(btnHuy);
        btnPanel.add(btnLuu);
        // Action buttons for view mode (conditionally shown by status + permission)
        if (boNhiemHienThi != null && SessionContext.getInstance().hasPermission(PermissionCodes.APPOINTMENT_APPROVE)) {
            String status = boNhiemHienThi.getTrangThai();
            if (HRMConstants.TRANG_THAI_CHO_DUYET.equals(status)) {
                JButton btnTuChoi = new JButton("Từ chối");
                styleActionButton(btnTuChoi, new Color(192, 57, 43));
                btnTuChoi.addActionListener(e -> tuChoiBoNhiem());
                JButton btnPheDuyet = new JButton("Phê duyệt");
                styleActionButton(btnPheDuyet, new Color(46, 164, 79));
                btnPheDuyet.addActionListener(e -> pheDuyetBoNhiem());
                btnPanel.add(btnTuChoi);
                btnPanel.add(btnPheDuyet);
            } else if (HRMConstants.TRANG_THAI_HIEU_LUC.equals(status)) {
                JButton btnKetThuc = new JButton("Kết thúc");
                styleActionButton(btnKetThuc, new Color(192, 57, 43));
                btnKetThuc.addActionListener(e -> ketThucBoNhiem());
                btnPanel.add(btnKetThuc);
            }
        }
        mainPanel.add(btnPanel, BorderLayout.SOUTH);
        setContentPane(mainPanel);
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, int row, String label, JComponent field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        JLabel lbl = new JLabel(label + ":");
        lbl.setFont(UIFonts.TEXT_NORMAL);
        lbl.setForeground(UIColors.TEXT_DARK);
        panel.add(lbl, gbc);
        gbc.gridx = 1;
        gbc.gridy = row;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        panel.add(field, gbc);
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
    }

    // ============================
    // Fill form (view mode)
    // ============================
    private void fillForm(BoNhiem bn) {
        // Pre-select nhân viên
        for (int i = 0; i < cboNhanVien.getItemCount(); i++) {
            if (cboNhanVien.getItemAt(i).getMaNhanVien().equals(bn.getMaNV())) {
                cboNhanVien.setSelectedIndex(i);
                break;
            }
        }
        // Chọn phòng ban
        for (int i = 0; i < cboPhongBan.getItemCount(); i++) {
            PhongBan dept = cboPhongBan.getItemAt(i);
            if (dept.getId() != null && dept.getId().equals(bn.getMaPhongBan())) {
                cboPhongBan.setSelectedIndex(i);
                break;
            }
        }
        // Chọn chức vụ
        for (int i = 0; i < cboChucVu.getItemCount(); i++) {
            ChucVu pos = cboChucVu.getItemAt(i);
            if (pos.getId().equals(bn.getMaChucVu())) {
                cboChucVu.setSelectedIndex(i);
                break;
            }
        }
        // Loại bổ nhiệm
        String loai = bn.getLoaiBoNhiem() != null ? bn.getLoaiBoNhiem() : HRMConstants.LOAI_BO_NHIEM_CHINH;
        for (int i = 0; i < cboLoaiBoNhiem.getItemCount(); i++) {
            if (cboLoaiBoNhiem.getItemAt(i).equals(loai)) {
                cboLoaiBoNhiem.setSelectedIndex(i);
                break;
            }
        }
        // Tỷ lệ
        spnTyLe.setValue((int) bn.getTyLeHuongLuong());
        // Từ ngày
        if (bn.getTuNgay() != null) {
            Date date = Date.from(bn.getTuNgay().atStartOfDay(ZoneId.systemDefault()).toInstant());
            spnTuNgay.setValue(date);
        }
        if (bn.getDenNgay() != null) {
            java.time.format.DateTimeFormatter ngayFormatter = java.time.format.DateTimeFormatter
                    .ofPattern("dd/MM/yyyy");
            txtDenNgay.setText(bn.getDenNgay().format(ngayFormatter));
        } else {
            txtDenNgay.setText("Không thời hạn");
        }
        // Cấp trên trực tiếp
        if (bn.getMaQuanLy() != null && !bn.getMaQuanLy().isEmpty()) {
            for (int i = 1; i < cboQuanLy.getItemCount(); i++) {
                Object item = cboQuanLy.getItemAt(i);
                if (item instanceof NhanVien && ((NhanVien) item).getMaNhanVien().equals(bn.getMaQuanLy())) {
                    cboQuanLy.setSelectedIndex(i);
                    break;
                }
            }
        }
        // Ghi chú
        txtGhiChu.setText(bn.getLyDo() != null ? bn.getLyDo() : "");
        // Hiển thị người duyệt, ngày duyệt
        String nguoiDuyet = bn.getTenNguoiDuyet();
        if (nguoiDuyet == null || nguoiDuyet.isEmpty()) {
            // Hiển thị mã nếu tên bị rỗng
            nguoiDuyet = bn.getNguoiDuyet();
        }
        if (HRMConstants.USERNAME_ADMIN.equals(nguoiDuyet)) {
            nguoiDuyet = "Quản trị viên";
        }
        txtNguoiDuyet.setText(nguoiDuyet != null ? nguoiDuyet : "Chưa duyệt");
        if (bn.getNgayPheDuyet() != null) {
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter
                    .ofPattern("dd/MM/yyyy HH:mm");
            txtNgayDuyet.setText(bn.getNgayPheDuyet().format(formatter));
        } else {
            txtNgayDuyet.setText("Chưa phê duyệt");
        }
    }

    private void setReadOnly() {
        cboNhanVien.setEnabled(false);
        cboPhongBan.setEnabled(false);
        cboChucVu.setEnabled(false);
        cboQuanLy.setEnabled(false);
        cboLoaiBoNhiem.setEnabled(false);
        spnTyLe.setEnabled(false);
        spnTuNgay.setEnabled(false);
        txtGhiChu.setEditable(false);
        btnLuu.setVisible(false);
        setTitle("Chi Tiết Bổ Nhiệm");
    }

    // ============================
    // Save action
    // ============================
    private void luuBoNhiem() {
        // Validate nhân viên
        NhanVien selectedNV = (NhanVien) cboNhanVien.getSelectedItem();
        if (selectedNV == null) {
            showError("Vui lòng chọn nhân viên.", "Lỗi nhập liệu");
            cboNhanVien.requestFocus();
            return;
        }
        String maNV = selectedNV.getMaNhanVien();
        // Validate phòng ban
        PhongBan selectedDept = (PhongBan) cboPhongBan.getSelectedItem();
        if (selectedDept == null) {
            showError("Vui lòng chọn phòng ban.", "Lỗi nhập liệu");
            cboPhongBan.requestFocus();
            return;
        }
        // Validate chức vụ
        ChucVu selectedPos = (ChucVu) cboChucVu.getSelectedItem();
        if (selectedPos == null) {
            showError("Vui lòng chọn chức vụ.", "Lỗi nhập liệu");
            cboChucVu.requestFocus();
            return;
        }
        // Lấy loại bổ nhiệm (giá trị đã là DB value: 'chinh', 'kiem_nhiem')
        String loaiBoNhiem = (String) cboLoaiBoNhiem.getSelectedItem();
        // Tỷ lệ hưởng lương
        int tyLe = (int) spnTyLe.getValue();
        // Từ ngày
        Date tuNgayDate = (Date) spnTuNgay.getValue();
        LocalDate tuNgay = tuNgayDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        // Ghi chú / lý do
        String ghiChu = txtGhiChu.getText().trim();
        // Cấp trên trực tiếp
        String maQuanLy = null;
        Object quanLyItem = cboQuanLy.getSelectedItem();
        if (quanLyItem instanceof NhanVien) {
            maQuanLy = ((NhanVien) quanLyItem).getMaNhanVien();
        }
        // Tạo BoNhiem object
        BoNhiem bn = new BoNhiem();
        bn.setMaNV(maNV);
        bn.setMaPhongBan(selectedDept.getId());
        bn.setMaChucVu(selectedPos.getId());
        bn.setMaQuanLy(maQuanLy);
        bn.setLoaiBoNhiem(loaiBoNhiem);
        bn.setTyLeHuongLuong(tyLe);
        bn.setTuNgay(tuNgay);
        bn.setLyDo(ghiChu.isEmpty() ? null : ghiChu);
        // Gọi service
        KetQua<BoNhiem> result = BoNhiemBUS.getInstance().taoBoNhiem(bn);
        if (result.isSuccess()) {
            showSuccess(result.getMessage());
            saved = true;
            dispose();
        } else {
            showError(result.getMessage());
        }
    }

    private static void styleActionButton(JButton btn, Color bg) {
        btn.setFont(com.hrm.util.UIFonts.BOLD_NORMAL);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    // ============================
    // Getters
    // ============================
    public boolean isSaved() {
        return saved;
    }

    public boolean isActionTaken() {
        return actionTaken;
    }

    // ============================
    // Action methods (view mode)
    // ============================
    private void pheDuyetBoNhiem() {
        if (!showYesNo("Xác nhận phê duyệt bổ nhiệm #" + boNhiemHienThi.getId() + "?",
                "Xác nhận phê duyệt")) return;
        String userId = HRMConstants.USERNAME_ADMIN;
        if (SessionContext.getInstance().getCurrentUser() != null) {
            String nvId = SessionContext.getInstance().getCurrentUser().getMaNV();
            if (nvId != null && !nvId.trim().isEmpty()) {
                userId = nvId;
            }
        }
        KetQua<BoNhiem> result = BoNhiemBUS.getInstance().pheDuyetBoNhiem(
                boNhiemHienThi.getId(), userId);
        if (result.isSuccess()) {
            showSuccess(result.getMessage());
            actionTaken = true;
            dispose();
        } else {
            showError(result.getMessage());
        }
    }

    private void tuChoiBoNhiem() {
        JTextField txtLyDo = new JTextField(30);
        txtLyDo.setFont(UIFonts.TEXT_NORMAL);
        int confirm = JOptionPane.showConfirmDialog(this, new Object[] { "Lý do từ chối:", txtLyDo },
                "Từ chối bổ nhiệm #" + boNhiemHienThi.getId(),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (confirm != JOptionPane.OK_OPTION)
            return;
        KetQua<BoNhiem> result = BoNhiemBUS.getInstance().tuChoiBoNhiem(
                boNhiemHienThi.getId(), txtLyDo.getText().trim());
        if (result.isSuccess()) {
            showSuccess(result.getMessage());
            actionTaken = true;
            dispose();
        } else {
            showError(result.getMessage());
        }
    }

    private void ketThucBoNhiem() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Kết thúc bổ nhiệm này ngay hôm nay?",
                "Kết thúc bổ nhiệm #" + boNhiemHienThi.getId(),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.OK_OPTION)
            return;
        KetQua<BoNhiem> result = BoNhiemBUS.getInstance().ketThucBoNhiem(
                boNhiemHienThi.getId(), LocalDate.now());
        if (result.isSuccess()) {
            showSuccess(result.getMessage());
            actionTaken = true;
            dispose();
        } else {
            showError(result.getMessage());
        }
    }

}
