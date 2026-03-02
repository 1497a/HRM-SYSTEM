package com.hrm.gui.appointment;

import com.hrm.gui.components.PurpleButton;
import com.hrm.model.BoNhiem;
import com.hrm.model.Department;
import com.hrm.model.NhanVien;
import com.hrm.model.Position;
import com.hrm.repo.DepartmentRepository;
import com.hrm.repo.PositionRepository;
import com.hrm.service.BoNhiemService;
import com.hrm.service.NhanVienService;
import com.hrm.service.ServiceResult;
import com.hrm.util.UIColors;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
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

    // Mode: view (read-only khi có boNhiem)
    private final BoNhiem boNhiemHienThi;

    // Repositories
    private final DepartmentRepository departmentRepo = new DepartmentRepository();
    private final PositionRepository positionRepo = new PositionRepository();

    // Form fields
    private JComboBox<NhanVien> cboNhanVien;
    private JComboBox<Department> cboPhongBan;
    private JComboBox<Position> cboChucVu;
    private JComboBox<String> cboLoaiBoNhiem;
    private JSpinner spnTyLe;
    private JSpinner spnTuNgay;
    private JTextArea txtGhiChu;

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
        cboNhanVien.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        loadNhanVien();

        // Phòng ban
        cboPhongBan = new JComboBox<>();
        cboPhongBan.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        loadDepartments();

        // Chức vụ
        cboChucVu = new JComboBox<>();
        cboChucVu.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        loadPositions();

        // Loại bổ nhiệm
        cboLoaiBoNhiem = new JComboBox<>(new String[]{"chinh_thuc", "phu", "kiem_nhiem"});
        cboLoaiBoNhiem.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cboLoaiBoNhiem.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value != null) {
                    switch (value.toString()) {
                        case "chinh_thuc": setText("Chính thức"); break;
                        case "phu":        setText("Phụ");         break;
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
        spnTyLe.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        spnTyLe.setPreferredSize(new Dimension(80, 32));

        // Từ ngày
        SpinnerDateModel tuNgayModel = new SpinnerDateModel(new Date(), null, null, java.util.Calendar.DAY_OF_MONTH);
        spnTuNgay = new JSpinner(tuNgayModel);
        spnTuNgay.setEditor(new JSpinner.DateEditor(spnTuNgay, "dd/MM/yyyy"));
        spnTuNgay.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        // Ghi chú
        txtGhiChu = new JTextArea(3, 20);
        txtGhiChu.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtGhiChu.setLineWrap(true);
        txtGhiChu.setWrapStyleWord(true);

        // Buttons
        btnLuu = new PurpleButton("Lưu");
        btnHuy = new JButton("Hủy");
        btnHuy.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btnHuy.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnLuu.addActionListener(e -> luuBoNhiem());
        btnHuy.addActionListener(e -> dispose());
    }

    private void loadDepartments() {
        List<Department> departments = departmentRepo.findActive();
        for (Department dept : departments) {
            cboPhongBan.addItem(dept);
        }
        cboPhongBan.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Department) {
                    setText(((Department) value).getTenPhongBan());
                }
                return this;
            }
        });
    }

    private void loadPositions() {
        List<Position> positions = positionRepo.findActive();
        for (Position pos : positions) {
            cboChucVu.addItem(pos);
        }
        cboChucVu.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Position) {
                    setText(((Position) value).getTenChucVu());
                }
                return this;
            }
        });
    }

    private void loadNhanVien() {
        List<NhanVien> list = NhanVienService.getInstance().getDangLamViec();
        for (NhanVien nv : list) {
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
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
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
        // Loại bổ nhiệm
        addFormRow(formPanel, gbc, 4, "Loại bổ nhiệm (*)", cboLoaiBoNhiem);
        // Tỷ lệ lương
        addFormRow(formPanel, gbc, 5, "Tỷ lệ hưởng lương (%)", spnTyLe);
        // Từ ngày
        addFormRow(formPanel, gbc, 6, "Từ ngày (*)", spnTuNgay);

        // Ghi chú
        gbc.gridx = 0; gbc.gridy = 7;
        JLabel lblGhiChu = new JLabel("Ghi chú:");
        lblGhiChu.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblGhiChu.setForeground(UIColors.TEXT_DARK);
        formPanel.add(lblGhiChu, gbc);

        gbc.gridx = 1; gbc.gridy = 7;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        JScrollPane scrollGhiChu = new JScrollPane(txtGhiChu);
        scrollGhiChu.setPreferredSize(new Dimension(240, 70));
        formPanel.add(scrollGhiChu, gbc);
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // Button panel
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setOpaque(false);
        btnPanel.setBorder(new EmptyBorder(8, 0, 0, 0));
        btnPanel.add(btnHuy);
        btnPanel.add(btnLuu);

        mainPanel.add(btnPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, int row, String label, JComponent field) {
        gbc.gridx = 0; gbc.gridy = row;
        JLabel lbl = new JLabel(label + ":");
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
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
            if (cboNhanVien.getItemAt(i).getId() == bn.getMaNV()) {
                cboNhanVien.setSelectedIndex(i);
                break;
            }
        }

        // Chọn phòng ban
        for (int i = 0; i < cboPhongBan.getItemCount(); i++) {
            Department dept = cboPhongBan.getItemAt(i);
            if (dept.getMaPhongBan().equals(bn.getMaPhongBan())) {
                cboPhongBan.setSelectedIndex(i);
                break;
            }
        }

        // Chọn chức vụ
        for (int i = 0; i < cboChucVu.getItemCount(); i++) {
            Position pos = cboChucVu.getItemAt(i);
            if (pos.getMaChucVu().equals(bn.getMaChucVu())) {
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

        // Ghi chú
        txtGhiChu.setText(bn.getLyDo() != null ? bn.getLyDo() : "");
    }

    private void setReadOnly() {
        cboNhanVien.setEnabled(false);
        cboPhongBan.setEnabled(false);
        cboChucVu.setEnabled(false);
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
        int maNV = selectedNV.getId();

        // Validate phòng ban
        Department selectedDept = (Department) cboPhongBan.getSelectedItem();
        if (selectedDept == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn phòng ban.",
                    "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Validate chức vụ
        Position selectedPos = (Position) cboChucVu.getSelectedItem();
        if (selectedPos == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn chức vụ.",
                    "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Lấy loại bổ nhiệm (map display -> db value)
        String loaiBoNhiem = (String) cboLoaiBoNhiem.getSelectedItem();
        if ("chinh_thuc".equals(loaiBoNhiem)) loaiBoNhiem = "chinh";

        // Tỷ lệ hưởng lương
        int tyLe = (int) spnTyLe.getValue();

        // Từ ngày
        Date tuNgayDate = (Date) spnTuNgay.getValue();
        LocalDate tuNgay = tuNgayDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        // Ghi chú / lý do
        String ghiChu = txtGhiChu.getText().trim();

        // Tạo BoNhiem object
        BoNhiem bn = new BoNhiem();
        bn.setMaNV(maNV);
        bn.setMaPhongBan(selectedDept.getMaPhongBan());
        bn.setMaChucVu(selectedPos.getMaChucVu());
        bn.setLoaiBoNhiem(loaiBoNhiem);
        bn.setTyLeHuongLuong(tyLe);
        bn.setTuNgay(tuNgay);
        bn.setLyDo(ghiChu.isEmpty() ? null : ghiChu);

        // Gọi service
        ServiceResult<BoNhiem> result = BoNhiemService.getInstance().taoBoNhiem(bn);

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

    // ============================
    // Getter
    // ============================

    public boolean isSaved() {
        return saved;
    }
}
