package com.hrm.gui.appointment;

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
 * Dialog tạo/xem bổ nhiệm nhân viên.
 */
public class AppointmentFormDialog extends JDialog {

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
    private JComboBox<Object> cboQuanLy;   // cap tren truc tiep (nullable)
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
     * Constructor tạo mới bổ nhiệm.
     */
    public AppointmentFormDialog(Frame parent) {
        this(parent, null);
    }

    /**
     * Constructor xem/hiển thị bổ nhiệm đã có.
     */
    public AppointmentFormDialog(Frame parent, BoNhiem boNhiem) {
        super(parent, boNhiem == null ? "Tạo Bổ Nhiệm Mới" : "Chi Tiết Bổ Nhiệm #" + boNhiem.getMaBoNhiem(), true);
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
        cboNhanVien.setFont(com.hrm.util.UIFonts.TEXT_MEDIUM);
        loadNhanVien();

        // Phòng ban
        cboPhongBan = new JComboBox<>();
        cboPhongBan.setFont(com.hrm.util.UIFonts.TEXT_MEDIUM);
        loadDepartments();

        // Chức vụ
        cboChucVu = new JComboBox<>();
        cboChucVu.setFont(com.hrm.util.UIFonts.TEXT_MEDIUM);
        loadPositions();

        // Cấp trên trực tiếp
        cboQuanLy = new JComboBox<>();
        cboQuanLy.setFont(com.hrm.util.UIFonts.TEXT_MEDIUM);
        loadQuanLy();

        // Loại bổ nhiệm
        // DB ENUM: 'chinh', 'kiem_nhiem'
        cboLoaiBoNhiem = new JComboBox<>(new String[]{"chinh", "kiem_nhiem"});
        cboLoaiBoNhiem.setFont(com.hrm.util.UIFonts.TEXT_MEDIUM);
        cboLoaiBoNhiem.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value != null) {
                    switch (value.toString()) {
                        case "chinh":      setText("Chính thức");  break;
                        case "kiem_nhiem": setText("Kiêm nhiệm");  break;
                        default:           setText(value.toString());
                    }
                }
                return this;
            }
        });

        // Tỷ lệ hưởng lương (0-100%)
        SpinnerNumberModel tyLeModel = new SpinnerNumberModel(100, 0, 100, 1);
        spnTyLe = new JSpinner(tyLeModel);
        spnTyLe.setFont(com.hrm.util.UIFonts.TEXT_MEDIUM);
        spnTyLe.setPreferredSize(new Dimension(80, 32));

        // Từ ngày
        SpinnerDateModel tuNgayModel = new SpinnerDateModel(new Date(), null, null, java.util.Calendar.DAY_OF_MONTH);
        spnTuNgay = new JSpinner(tuNgayModel);
        spnTuNgay.setEditor(new JSpinner.DateEditor(spnTuNgay, "dd/MM/yyyy"));
        spnTuNgay.setFont(com.hrm.util.UIFonts.TEXT_MEDIUM);

        // Ghi chú
        txtGhiChu = new JTextArea(3, 20);
        txtGhiChu.setFont(com.hrm.util.UIFonts.TEXT_MEDIUM);
        txtGhiChu.setLineWrap(true);
        txtGhiChu.setWrapStyleWord(true);

        txtDenNgay = new JTextField();
        txtDenNgay.setFont(com.hrm.util.UIFonts.BOLD_MEDIUM);
        txtDenNgay.setEditable(false);
        txtDenNgay.setBackground(com.hrm.util.UIColors.LIGHT_GRAY_BG);

        // Người duyệt, Ngày duyệt
        txtNguoiDuyet = new JTextField();
        txtNguoiDuyet.setFont(com.hrm.util.UIFonts.BOLD_MEDIUM);
        txtNguoiDuyet.setEditable(false);
        txtNguoiDuyet.setBackground(com.hrm.util.UIColors.LIGHT_GRAY_BG);

        txtNgayDuyet = new JTextField();
        txtNgayDuyet.setFont(com.hrm.util.UIFonts.BOLD_MEDIUM);
        txtNgayDuyet.setEditable(false);
        txtNgayDuyet.setBackground(com.hrm.util.UIColors.LIGHT_GRAY_BG);

        // Buttons
        btnLuu = new PurpleButton("Lưu");
        btnHuy = new JButton("Hủy");
        btnHuy.setFont(com.hrm.util.UIFonts.TEXT_MEDIUM);
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
        mainPanel.setBackground(UIColors.LIGHT_GRAY_BG);
        mainPanel.setBorder(new EmptyBorder(16, 16, 16, 16));

        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(UIColors.WHITE);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIColors.BORDER_GRAY),
                new EmptyBorder(16, 16, 16, 16)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.anchor = GridBagConstraints.WEST;

        // Tiêu đề form
        JLabel lblTitle = new JLabel("THÔNG TIN BỔ NHIỆM");
        lblTitle.setFont(com.hrm.util.UIFonts.HEADER_SUB);
        lblTitle.setForeground(UIColors.PRIMARY_PURPLE);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 8, 12, 8);
        formPanel.add(lblTitle, gbc);
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
        gbc.gridx = 0; gbc.gridy = 8;
        JLabel lblGhiChu = new JLabel("Ghi chú:");
        lblGhiChu.setFont(com.hrm.util.UIFonts.TEXT_MEDIUM);
        lblGhiChu.setForeground(UIColors.TEXT_DARK);
        formPanel.add(lblGhiChu, gbc);

        gbc.gridx = 1; gbc.gridy = 8;
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
            if ("cho_duyet".equals(status)) {
                JButton btnTuChoi = new JButton("Từ chối");
                styleActionButton(btnTuChoi, new Color(192, 57, 43));
                btnTuChoi.addActionListener(e -> tuChoiBoNhiem());

                JButton btnPheDuyet = new JButton("Phê duyệt");
                styleActionButton(btnPheDuyet, new Color(46, 164, 79));
                btnPheDuyet.addActionListener(e -> pheDuyetBoNhiem());

                btnPanel.add(btnTuChoi);
                btnPanel.add(btnPheDuyet);
            } else if ("hieu_luc".equals(status)) {
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
        gbc.gridx = 0; gbc.gridy = row;
        JLabel lbl = new JLabel(label + ":");
        lbl.setFont(com.hrm.util.UIFonts.TEXT_MEDIUM);
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
            if (dept.getId() == bn.getPhongBanId()) {
                cboPhongBan.setSelectedIndex(i);
                break;
            }
        }

        // Chọn chức vụ
        for (int i = 0; i < cboChucVu.getItemCount(); i++) {
            ChucVu pos = cboChucVu.getItemAt(i);
            if (pos.getId().equals(bn.getChucVuId())) {
                cboChucVu.setSelectedIndex(i);
                break;
            }
        }

        // Loại bổ nhiệm
        String loai = bn.getLoaiBoNhiem() != null ? bn.getLoaiBoNhiem() : "chinh_thuc";
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
            java.time.format.DateTimeFormatter ngayFormatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
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
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
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
            JOptionPane.showMessageDialog(this, "Vui lòng chọn nhân viên.",
                    "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String maNV = selectedNV.getMaNhanVien();

        // Validate phòng ban
        PhongBan selectedDept = (PhongBan) cboPhongBan.getSelectedItem();
        if (selectedDept == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn phòng ban.",
                    "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Validate chức vụ
        ChucVu selectedPos = (ChucVu) cboChucVu.getSelectedItem();
        if (selectedPos == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn chức vụ.",
                    "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
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
        bn.setPhongBanId(selectedDept.getId());
        bn.setChucVuId(selectedPos.getId());
        bn.setMaQuanLy(maQuanLy);
        bn.setLoaiBoNhiem(loaiBoNhiem);
        bn.setTyLeHuongLuong(tyLe);
        bn.setTuNgay(tuNgay);
        bn.setLyDo(ghiChu.isEmpty() ? null : ghiChu);

        // Gọi service
        KetQua<BoNhiem> result = BoNhiemBUS.getInstance().taoBoNhiem(bn);

        if (result.isSuccess()) {
            JOptionPane.showMessageDialog(this, result.getMessage(),
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
            saved = true;
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, result.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void styleActionButton(JButton btn, Color bg) {
        btn.setFont(com.hrm.util.UIFonts.BOLD_NORMAL);
        btn.setBackground(bg);
        btn.setForeground(com.hrm.util.UIColors.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    // ============================
    // Getters
    // ============================

    public boolean isSaved() { return saved; }

    public boolean isActionTaken() { return actionTaken; }

    // ============================
    // Action methods (view mode)
    // ============================

    private void pheDuyetBoNhiem() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Xác nhận phê duyệt bổ nhiệm #" + boNhiemHienThi.getMaBoNhiem() + "?",
                "Xác nhận phê duyệt", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        String userId = HRMConstants.USERNAME_ADMIN;
        if (SessionContext.getInstance().getCurrentUser() != null) {
            String nvId = SessionContext.getInstance().getCurrentUser().getNhanVienId();
            if (nvId != null && !nvId.trim().isEmpty()) {
                userId = nvId;
            }
        }
        KetQua<BoNhiem> result = BoNhiemBUS.getInstance().pheDuyetBoNhiem(
                boNhiemHienThi.getMaBoNhiem(), userId);
        if (result.isSuccess()) {
            JOptionPane.showMessageDialog(this, result.getMessage(), "Thành công", JOptionPane.INFORMATION_MESSAGE);
            actionTaken = true;
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, result.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void tuChoiBoNhiem() {
        JTextField txtLyDo = new JTextField(30);
        txtLyDo.setFont(com.hrm.util.UIFonts.TEXT_MEDIUM);
        int confirm = JOptionPane.showConfirmDialog(this, new Object[]{"Lý do từ chối:", txtLyDo},
                "Từ chối bổ nhiệm #" + boNhiemHienThi.getMaBoNhiem(),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (confirm != JOptionPane.OK_OPTION) return;

        KetQua<BoNhiem> result = BoNhiemBUS.getInstance().tuChoiBoNhiem(
                boNhiemHienThi.getMaBoNhiem(), txtLyDo.getText().trim());
        if (result.isSuccess()) {
            JOptionPane.showMessageDialog(this, result.getMessage(), "Thành công", JOptionPane.INFORMATION_MESSAGE);
            actionTaken = true;
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, result.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void ketThucBoNhiem() {
        SpinnerDateModel dateModel = new SpinnerDateModel(new Date(), null, null, java.util.Calendar.DAY_OF_MONTH);
        JSpinner spnDenNgay = new JSpinner(dateModel);
        spnDenNgay.setEditor(new JSpinner.DateEditor(spnDenNgay, "dd/MM/yyyy"));
        spnDenNgay.setFont(com.hrm.util.UIFonts.TEXT_MEDIUM);

        int confirm = JOptionPane.showConfirmDialog(this,
                new Object[]{"Ngày kết thúc:", spnDenNgay},
                "Kết thúc bổ nhiệm #" + boNhiemHienThi.getMaBoNhiem(),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (confirm != JOptionPane.OK_OPTION) return;

        LocalDate denNgay = ((Date) spnDenNgay.getValue())
                .toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        KetQua<BoNhiem> result = BoNhiemBUS.getInstance().ketThucBoNhiem(
                boNhiemHienThi.getMaBoNhiem(), denNgay);
        if (result.isSuccess()) {
            JOptionPane.showMessageDialog(this, result.getMessage(), "Thành công", JOptionPane.INFORMATION_MESSAGE);
            actionTaken = true;
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, result.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}
