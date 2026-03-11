package com.hrm.gui.employee;

import com.hrm.gui.components.PurpleTable;
import com.hrm.model.BoNhiem;
import com.hrm.model.ChamCong;
import com.hrm.model.HopDongLaoDong;
import com.hrm.model.NhanVien;
import com.hrm.model.ThongTinCaNhan;
import com.hrm.bus.ChamCongBUS;
import com.hrm.bus.BoNhiemBUS;
import com.hrm.bus.HopDongBUS;
import com.hrm.bus.NhanVienBUS;
import com.hrm.bus.KetQua;
import com.hrm.util.SessionContext;
import com.hrm.util.UIColors;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * EmployeeDetailPanel - Modal dialog showing full employee profile.
 *
 * Displays:
 *   Tab 1 - Thong tin ca nhan  : personal info (ThongTinCaNhan) + NhanVien fields
 *   Tab 2 - Bo nhiem hien tai  : current effective appointment + appointment history table
 *   Tab 3 - Lich su cham cong  : monthly attendance history with month/year filter
 *
 * Opened from EmployeeListPanel when the user clicks "Xem ho so".
 * Receives an int maNV (employee primary-key id).
 */
public class EmployeeDetailPanel extends JDialog {

    // =====================================================================
    // Services
    // =====================================================================
    private final NhanVienBUS nvService       = NhanVienBUS.getInstance();
    private final BoNhiemBUS  boNhiemService  = BoNhiemBUS.getInstance();
    private final HopDongBUS  hopDongService  = HopDongBUS.getInstance();

    // =====================================================================
    // Data
    // =====================================================================
    private final String maNV;
    private NhanVien        nhanVien;
    private ThongTinCaNhan  thongTinCaNhan;
    private BoNhiem         boNhiemHienTai;
    private HopDongLaoDong  hopDongHieuLuc;

    // =====================================================================
    // Formatters
    // =====================================================================
    private static final DateTimeFormatter DATE_FMT     = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FMT     = DateTimeFormatter.ofPattern("HH:mm");

    // =====================================================================
    // Tab 2 â€“ appointment history table
    // =====================================================================
    private static final String[] COL_BO_NHIEM = {
        "Phòng ban", "Chức vụ", "Loại", "Từ ngày", "Đến ngày", "Trạng thái"
    };
    private DefaultTableModel boNhiemTableModel;

    // =====================================================================
    // State
    // =====================================================================
    private boolean dataChanged = false;
    private boolean personalEditMode = false;
    private boolean statusEditMode = false;

    private JButton btnSuaThongTin;
    private JButton btnDoiTrangThai;
    private JComboBox<String> cboTrangThaiNhanVien;

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
    private JTextField txtTrinhDoHocVan;
    private JTextField txtFileCV;
    private JTextArea txtKinhNghiem;

    public boolean isDataChanged() { return dataChanged; }

    // =====================================================================
    // Constructor
    // =====================================================================

    /**
     * Creates and opens the employee detail dialog.
     *
     * @param parent  owner frame (used for positioning)
     * @param maNV    primary-key id of the employee to display
     */
    public EmployeeDetailPanel(Frame parent, String maNV) {
        super(parent, "Ho so nhan vien", true);
        this.maNV = maNV;

        loadData();
        buildUI();

        setSize(750, 600);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(true);
    }

    // =====================================================================
    // Data loading
    // =====================================================================

    private void loadData() {
        nhanVien       = nvService.getById(maNV);
        thongTinCaNhan = nvService.getThongTinCaNhan(maNV);
        boNhiemHienTai = boNhiemService.getBoNhiemChinhHieuLuc(maNV);
        hopDongHieuLuc = hopDongService.getHieuLuc(maNV);
    }

    // =====================================================================
    // Top-level UI construction
    // =====================================================================

    private void buildUI() {
        // Title bar label with employee name
        String dialogTitle = "Hồ sơ nhân viên";
        if (nhanVien != null && nhanVien.getHoTen() != null && !nhanVien.getHoTen().isEmpty()) {
            dialogTitle = "Hồ sơ: " + nhanVien.getHoTen();
        }
        setTitle(dialogTitle);

        // Root panel
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(UIColors.WHITE);

        // Header band
        root.add(buildHeader(), BorderLayout.NORTH);

        // Tabbed pane
        JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP);
        tabs.setFont(com.hrm.util.UIFonts.BOLD_NORMAL);
        tabs.setBackground(UIColors.LIGHT_GRAY_BG);
        tabs.addTab("Thông tin cá nhân",  buildPersonalTab());
        tabs.addTab("Bổ nhiệm hiện tại",  buildAppointmentTab());
        root.add(tabs, BorderLayout.CENTER);

        // Close button at the bottom
        root.add(buildFooter(), BorderLayout.SOUTH);
        tabs.addChangeListener(e -> updateActionButtonsByTab(tabs));
        updateActionButtonsByTab(tabs);

        setContentPane(root);
    }

    // =====================================================================
    // Header
    // =====================================================================

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UIColors.PRIMARY_PURPLE);
        header.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));

        String name = "(Không rõ tên)";
        String code = "#" + maNV;
        if (thongTinCaNhan != null && thongTinCaNhan.getHoTen() != null) {
            name = thongTinCaNhan.getHoTen();
        } else if (nhanVien != null && nhanVien.getHoTen() != null) {
            name = nhanVien.getHoTen();
        }
        if (nhanVien != null && nhanVien.getMaNhanVien() != null) {
            code = nhanVien.getMaNhanVien();
        }

        JLabel lblName = new JLabel(name);
        lblName.setFont(com.hrm.util.UIFonts.HEADER_H3);
        lblName.setForeground(UIColors.WHITE);

        JLabel lblCode = new JLabel(code);
        lblCode.setFont(com.hrm.util.UIFonts.TEXT_NORMAL);
        lblCode.setForeground(new Color(220, 210, 255));

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.add(lblName);
        textPanel.add(Box.createVerticalStrut(2));
        textPanel.add(lblCode);

        header.add(textPanel, BorderLayout.WEST);

        // Status badge on the right
        if (nhanVien != null) {
            JLabel badge = createStatusBadge(nhanVien.getTrangThai(), nhanVien.getTrangThaiDisplay());
            JPanel badgeWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            badgeWrap.setOpaque(false);
            badgeWrap.add(badge);
            header.add(badgeWrap, BorderLayout.EAST);
        }

        return header;
    }

    // =====================================================================
    // Tab 1  Personal information
    // =====================================================================

    private JScrollPane buildPersonalTab() {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(UIColors.WHITE);
        content.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        // ---- Section: Thong tin nhan vien --------------------------------
        content.add(buildSectionTitle("Thông tin nhân viên"));
        content.add(Box.createVerticalStrut(8));

        JPanel nvPanel = buildInfoGrid();
        addInfoRow(nvPanel, 0, "Mã nhân viên:",
                nhanVien != null ? safe(nhanVien.getMaNhanVien()) : "");
        addInfoRow(nvPanel, 1, "Loại hợp đồng:",
                nhanVien != null ? safe(nhanVien.getLoaiHopDongDisplay()) : "");
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
                setText(statusDisplayOf(value != null ? value.toString() : ""));
                return this;
            }
        });
        cboTrangThaiNhanVien.setSelectedItem(nhanVien != null ? nhanVien.getTrangThai() : "dang_lam_viec");
        setStatusEditMode(false);
        addInfoRow(nvPanel, 3, "Trạng thái:", cboTrangThaiNhanVien);
        content.add(nvPanel);
        content.add(Box.createVerticalStrut(16));

        // ---- Section: Thong tin ca nhan ----------------------------------
        content.add(buildSectionTitle("Thông tin cá nhân"));
        content.add(Box.createVerticalStrut(8));

        JPanel ttcnPanel = buildInfoGrid();
        if (thongTinCaNhan != null) {
            txtHoTen = createReadOnlyTextField();
            txtNgaySinh = createReadOnlyTextField();
            cboGioiTinh = new JComboBox<>(new String[]{"nam", "nu", "khac"});
            txtCCCD = createReadOnlyTextField();
            txtDienThoai = createReadOnlyTextField();
            txtEmail = createReadOnlyTextField();
            txtDiaChi = createReadOnlyTextField();
            txtDiaChiThuongTru = createReadOnlyTextField();
            txtQueQuan = createReadOnlyTextField();
            cboTinhTrangHonNhan = new JComboBox<>(new String[]{"doc_than", "da_ket_hon", "ly_hon"});
            txtTrinhDoHocVan = createReadOnlyTextField();
            txtFileCV = createReadOnlyTextField();
            txtKinhNghiem = createReadOnlyTextArea();

            addInfoRow(ttcnPanel, 0, "Họ và tên:", txtHoTen);
            addInfoRow(ttcnPanel, 1, "Ngày sinh:", txtNgaySinh);
            addInfoRow(ttcnPanel, 2, "Giới tính:", cboGioiTinh);
            addInfoRow(ttcnPanel, 3, "CCCD:", txtCCCD);
            addInfoRow(ttcnPanel, 4, "Số điện thoại:", txtDienThoai);
            addInfoRow(ttcnPanel, 5, "Email:", txtEmail);
            addInfoRow(ttcnPanel, 6, "Địa chỉ:", txtDiaChi);
            addInfoRow(ttcnPanel, 7, "Địa chỉ thường trú:", txtDiaChiThuongTru);
            addInfoRow(ttcnPanel, 8, "Quê quán:", txtQueQuan);
            addInfoRow(ttcnPanel, 9, "Tình trạng hôn nhân:", cboTinhTrangHonNhan);
            addInfoRow(ttcnPanel, 10, "Trình độ học vấn:", txtTrinhDoHocVan);
            addInfoRow(ttcnPanel, 11, "File CV:", txtFileCV);
            addInfoRow(ttcnPanel, 12, "Kinh nghiệm:", new JScrollPane(txtKinhNghiem));

            loadPersonalFields();
            setPersonalEditMode(false);
        } else {
            JLabel noData = new JLabel("  Không có thông tin cá nhân.");
            noData.setFont(new Font("Segoe UI", Font.ITALIC, 13));
            noData.setForeground(UIColors.TEXT_GRAY);
            ttcnPanel.add(noData, buildGbc(0, 0, 2));
        }
        content.add(ttcnPanel);
        content.add(Box.createVerticalStrut(16));

        // ---- Section: Hop dong hien tai (summary only) -------------------
        content.add(buildSectionTitle("Hợp đồng lao động"));
        content.add(Box.createVerticalStrut(8));

        JPanel hdPanel = buildInfoGrid();
        if (hopDongHieuLuc != null) {
            addInfoRow(hdPanel, 0, "Số hợp đồng:",   safe(hopDongHieuLuc.getSoHopDong()));
            addInfoRow(hdPanel, 1, "Loại hợp đồng:", safe(hopDongHieuLuc.getLoaiHopDongDisplay()));
            addInfoRow(hdPanel, 2, "Ngày ký:",
                    hopDongHieuLuc.getNgayKy() != null
                            ? hopDongHieuLuc.getNgayKy().format(DATE_FMT) : "");
            addInfoRow(hdPanel, 3, "Hiệu lực từ:",
                    hopDongHieuLuc.getNgayHieuLuc() != null
                            ? hopDongHieuLuc.getNgayHieuLuc().format(DATE_FMT) : "");
            addInfoRow(hdPanel, 4, "Hiệu lực đến:",
                    hopDongHieuLuc.getNgayHetHieuLuc() != null
                            ? hopDongHieuLuc.getNgayHetHieuLuc().format(DATE_FMT)
                            : "Không xác định");
            addInfoRow(hdPanel, 5, "Trạng thái:",
                    buildStatusLabelComponent(
                            hopDongHieuLuc.getTrangThai(),
                            hopDongHieuLuc.getTrangThaiDisplay()));
        } else {
            JLabel noHd = new JLabel("  Chưa có hợp đồng hiệu lực.");
            noHd.setFont(new Font("Segoe UI", Font.ITALIC, 13));
            noHd.setForeground(UIColors.TEXT_GRAY);
            hdPanel.add(noHd, buildGbc(0, 0, 2));
        }
        content.add(hdPanel);

        // Filler to push content up
        content.add(Box.createVerticalGlue());

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    // =====================================================================
    // Tab 2  Appointment
    // =====================================================================

    private JPanel buildAppointmentTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(UIColors.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        // ---- Current effective appointment --------------------------------
        JPanel currentPanel = new JPanel(new BorderLayout(0, 8));
        currentPanel.setBackground(UIColors.WHITE);

        currentPanel.add(buildSectionTitle("Bổ nhiệm hiện tại"), BorderLayout.NORTH);

        JPanel detailGrid = buildInfoGrid();
        if (boNhiemHienTai != null) {
            String tenPB = boNhiemHienTai.getTenPhongBan() != null
                    ? boNhiemHienTai.getTenPhongBan()
                    : safe(String.valueOf(boNhiemHienTai.getId()));
            String tenCV = boNhiemHienTai.getTenChucVu() != null
                    ? boNhiemHienTai.getTenChucVu()
                    : safe(String.valueOf(boNhiemHienTai.getId()));

            addInfoRow(detailGrid, 0, "Phòng ban:",         tenPB);
            addInfoRow(detailGrid, 1, "Chức vụ:",           tenCV);
            addInfoRow(detailGrid, 2, "Loại bổ nhiệm:",     safe(boNhiemHienTai.getLoaiBoNhiemDisplay()));
            addInfoRow(detailGrid, 3, "Tỷ lệ hưởng lương:", boNhiemHienTai.getTyLeHuongLuong() + "%");
            addInfoRow(detailGrid, 4, "Từ ngày:",
                    boNhiemHienTai.getTuNgay() != null
                            ? boNhiemHienTai.getTuNgay().format(DATE_FMT) : "");
            addInfoRow(detailGrid, 5, "Đến ngày:",
                    boNhiemHienTai.getDenNgay() != null
                            ? boNhiemHienTai.getDenNgay().format(DATE_FMT) : "Không xác định");
            addInfoRow(detailGrid, 6, "Trạng thái:",
                    buildStatusLabelComponent(
                            boNhiemHienTai.getTrangThai(),
                            boNhiemHienTai.getTrangThaiDisplay()));
        } else {
            JLabel noData = new JLabel("  Chưa có bổ nhiệm chính hiệu lực.");
            noData.setFont(new Font("Segoe UI", Font.ITALIC, 13));
            noData.setForeground(UIColors.TEXT_GRAY);
            detailGrid.add(noData, buildGbc(0, 0, 2));
        }
        currentPanel.add(detailGrid, BorderLayout.CENTER);
        panel.add(currentPanel, BorderLayout.NORTH);

        // ---- Appointment history table ------------------------------------
        JPanel historyPanel = new JPanel(new BorderLayout(0, 6));
        historyPanel.setBackground(UIColors.WHITE);

        JLabel histTitle = buildSectionTitle("Lịch sử bổ nhiệm");
        historyPanel.add(histTitle, BorderLayout.NORTH);

        boNhiemTableModel = PurpleTable.createNonEditableModel(COL_BO_NHIEM);
        populateBoNhiemTable();

        PurpleTable boNhiemTable = new PurpleTable(boNhiemTableModel);
        boNhiemTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        boNhiemTable.setDefaultRenderer(Object.class, new AppointmentStatusRenderer());

        // Column widths
        int[] colWidths = {130, 130, 90, 90, 90, 90};
        for (int i = 0; i < colWidths.length; i++) {
            boNhiemTable.getColumnModel().getColumn(i).setPreferredWidth(colWidths[i]);
        }

        JScrollPane tableScroll = new JScrollPane(boNhiemTable);
        tableScroll.setBorder(BorderFactory.createLineBorder(UIColors.BORDER_GRAY));
        tableScroll.setPreferredSize(new Dimension(700, 200));
        historyPanel.add(tableScroll, BorderLayout.CENTER);

        panel.add(historyPanel, BorderLayout.CENTER);
        return panel;
    }


    // =====================================================================
    // Data population helpers
    // =====================================================================

    private void populateBoNhiemTable() {
        boNhiemTableModel.setRowCount(0);
        try {
            List<BoNhiem> list = boNhiemService.getByMaNV(maNV);
            if (list == null || list.isEmpty()) return;
            for (BoNhiem bn : list) {
                String tenPB = bn.getTenPhongBan() != null
                        ? bn.getTenPhongBan() : safe(bn.getChucVuId());
                String tenCV = bn.getTenChucVu() != null
                        ? bn.getTenChucVu() : safe(bn.getChucVuId());
                boNhiemTableModel.addRow(new Object[]{
                    tenPB,
                    tenCV,
                    safe(bn.getLoaiBoNhiemDisplay()),
                    bn.getTuNgay() != null ? bn.getTuNgay().format(DATE_FMT) : "",
                    bn.getDenNgay() != null ? bn.getDenNgay().format(DATE_FMT) : "Không xác định",
                    safe(bn.getTrangThaiDisplay())
                });
            }
        } catch (Exception ex) {
            // Silently ignore; table stays empty
        }
    }


    // =====================================================================
    // Footer
    // =====================================================================

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(UIColors.LIGHT_GRAY_BG);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIColors.BORDER_GRAY));

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 10));
        right.setOpaque(false);

        btnSuaThongTin = new JButton("Sửa thông tin");
        stylePrimaryButton(btnSuaThongTin);
        btnSuaThongTin.setPreferredSize(new Dimension(130, 34));
        btnSuaThongTin.addActionListener(e -> onSuaThongTinClick());

        btnDoiTrangThai = new JButton("Đổi trạng thái");
        styleWarningButton(btnDoiTrangThai);
        btnDoiTrangThai.setPreferredSize(new Dimension(145, 34));
        btnDoiTrangThai.addActionListener(e -> onDoiTrangThaiClick());

        JButton btnClose = new JButton("Đóng");
        btnClose.setFont(com.hrm.util.UIFonts.BOLD_NORMAL);
        btnClose.setFocusPainted(false);
        btnClose.setPreferredSize(new Dimension(90, 34));
        btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClose.addActionListener(e -> dispose());

        SessionContext sc = SessionContext.getInstance();
        boolean canUpdate = sc.coVaiTro("ADMIN") || sc.coQuyen("EMPLOYEE_UPDATE");
        String myMaNV = sc.getCurrentUser() != null ? sc.getCurrentUser().getNhanVienId() : null;
        boolean isSelfView = maNV != null && maNV.equals(myMaNV);

        // Nhan vien co the tu sua thong tin ca nhan cua minh, nhung khong doi trang thai
        btnSuaThongTin .setVisible(canUpdate || isSelfView);
        btnDoiTrangThai.setVisible(canUpdate && !isSelfView);

        right.add(btnSuaThongTin);
        right.add(btnDoiTrangThai);
        right.add(btnClose);
        footer.add(right, BorderLayout.EAST);
        return footer;
    }

    private void onSuaThongTinClick() {
        if (thongTinCaNhan == null) return;
        if (statusEditMode) {
            JOptionPane.showMessageDialog(this,
                    "Đang ở chế độ đổi trạng thái. Vui lòng lưu thao tác đó trước.",
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!personalEditMode) {
            personalEditMode = true;
            setPersonalEditMode(true);
            btnSuaThongTin.setText("Lưu");
            return;
        }

        if (!hasPersonalChanges()) {
            personalEditMode = false;
            setPersonalEditMode(false);
            btnSuaThongTin.setText("Sửa thông tin");
            return;
        }

        KetQua<ThongTinCaNhan> result = savePersonalInfoInline();
        if (!result.isSuccess()) {
            JOptionPane.showMessageDialog(this, result.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        dataChanged = true;
        personalEditMode = false;
        btnSuaThongTin.setText("Sửa thông tin");
        loadData();
        buildUI();
        revalidate();
        repaint();
    }

    private void onDoiTrangThaiClick() {
        if (nhanVien == null) return;
        if (personalEditMode) {
            JOptionPane.showMessageDialog(this,
                    "Đang ở chế độ sửa thông tin. Vui lòng lưu trước.",
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!statusEditMode) {
            statusEditMode = true;
            setStatusEditMode(true);
            btnDoiTrangThai.setText("Lưu trạng thái");
            revalidate();
            repaint();
            return;
        }

        String trangThaiMoi = (String) cboTrangThaiNhanVien.getSelectedItem();
        if (trangThaiMoi != null && trangThaiMoi.equals(nhanVien.getTrangThai())) {
            statusEditMode = false;
            setStatusEditMode(false);
            btnDoiTrangThai.setText("Đổi trạng thái");
            return;
        }
        KetQua<NhanVien> result = nvService.capNhatTrangThai(nhanVien.getMaNhanVien(), trangThaiMoi, "");
        if (!result.isSuccess()) {
            JOptionPane.showMessageDialog(this, result.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        dataChanged = true;
        statusEditMode = false;
        setStatusEditMode(false);
        btnDoiTrangThai.setText("Đổi trạng thái");
        loadData();
        buildUI();
        revalidate();
        repaint();
    }

    private boolean hasPersonalChanges() {
        if (thongTinCaNhan == null) return false;

        String ngaySinhCu = thongTinCaNhan.getNgaySinh() != null ? thongTinCaNhan.getNgaySinh().format(DATE_FMT) : "";

        if (!equalsNorm(txtHoTen.getText(), thongTinCaNhan.getHoTen())) return true;
        if (!equalsNorm(txtNgaySinh.getText(), ngaySinhCu)) return true;
        if (!equalsNorm((String) cboGioiTinh.getSelectedItem(), thongTinCaNhan.getGioiTinh())) return true;
        if (!equalsNorm(txtCCCD.getText(), thongTinCaNhan.getCccd())) return true;
        if (!equalsNorm(txtDienThoai.getText(), thongTinCaNhan.getDienThoai())) return true;
        if (!equalsNorm(txtEmail.getText(), thongTinCaNhan.getEmail())) return true;
        if (!equalsNorm(txtDiaChi.getText(), thongTinCaNhan.getDiaChi())) return true;
        if (!equalsNorm(txtDiaChiThuongTru.getText(), thongTinCaNhan.getDiaChiThuongTru())) return true;
        if (!equalsNorm(txtQueQuan.getText(), thongTinCaNhan.getQueQuan())) return true;
        if (!equalsNorm((String) cboTinhTrangHonNhan.getSelectedItem(), thongTinCaNhan.getTinhTrangHonNhan())) return true;
        if (!equalsNorm(txtTrinhDoHocVan.getText(), thongTinCaNhan.getTrinhDoHocVan())) return true;
        if (!equalsNorm(txtFileCV.getText(), thongTinCaNhan.getFileCV())) return true;
        if (!equalsNorm(txtKinhNghiem.getText(), thongTinCaNhan.getKinhNghiem())) return true;
        return false;
    }

    private boolean equalsNorm(String a, String b) {
        return norm(a).equals(norm(b));
    }

    private String norm(String s) {
        return s == null ? "" : s.trim();
    }

    private KetQua<ThongTinCaNhan> savePersonalInfoInline() {
        if (thongTinCaNhan == null) {
            thongTinCaNhan = new ThongTinCaNhan();
            thongTinCaNhan.setMaNV(maNV);
        }

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
        String trinhDoHocVan = txtTrinhDoHocVan.getText().trim();
        String fileCV = txtFileCV.getText().trim();
        String kinhNghiem = txtKinhNghiem.getText().trim();

        if (hoTen.isEmpty()) {
            return KetQua.error("Họ tên không được để trống.");
        }

        LocalDate ngaySinh = null;
        if (!ngaySinhStr.isEmpty()) {
            try {
                ngaySinh = LocalDate.parse(ngaySinhStr, DATE_FMT);
            } catch (DateTimeParseException ex) {
                return KetQua.error("Ngày sinh không hợp lệ. Định dạng dd/MM/yyyy.");
            }
            if (ngaySinh.isAfter(LocalDate.now())) {
                return KetQua.error("Ngày sinh không thể ở tương lai.");
            }
        }

        if (!email.isEmpty() && !isValidEmail(email)) {
            return KetQua.error("Email không hợp lệ.");
        }

        if (!dienThoai.isEmpty() && !isValidPhone(dienThoai)) {
            return KetQua.error("Số điện thoại không hợp lệ.");
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
        thongTinCaNhan.setTrinhDoHocVan(trinhDoHocVan.isEmpty() ? null : trinhDoHocVan);
        thongTinCaNhan.setFileCV(fileCV.isEmpty() ? null : fileCV);
        thongTinCaNhan.setKinhNghiem(kinhNghiem.isEmpty() ? null : kinhNghiem);

        return nvService.capNhatThongTinCaNhan(thongTinCaNhan);
    }

    private void setStatusEditMode(boolean editable) {
        if (cboTrangThaiNhanVien == null) return;
        cboTrangThaiNhanVien.setEnabled(editable);
        cboTrangThaiNhanVien.setBorder(editable
                ? BorderFactory.createLineBorder(UIColors.PRIMARY_PURPLE, 1)
                : BorderFactory.createEmptyBorder(1, 1, 1, 1));
        cboTrangThaiNhanVien.setBackground(editable ? Color.WHITE : new Color(240, 240, 240));
    }

    private String statusDisplayOf(String key) {
        if ("dang_lam_viec".equals(key)) return "Đang làm việc";
        if ("tam_nghi".equals(key)) return "Tạm nghỉ";
        if ("nghi_viec".equals(key)) return "Nghỉ việc";
        return key;
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

    private void loadPersonalFields() {
        if (thongTinCaNhan == null) return;
        txtHoTen.setText(safe(thongTinCaNhan.getHoTen()));
        txtNgaySinh.setText(thongTinCaNhan.getNgaySinh() != null ? thongTinCaNhan.getNgaySinh().format(DATE_FMT) : "");
        cboGioiTinh.setSelectedItem(thongTinCaNhan.getGioiTinh() != null ? thongTinCaNhan.getGioiTinh() : "nam");
        txtCCCD.setText(safe(thongTinCaNhan.getCccd()));
        txtDienThoai.setText(safe(thongTinCaNhan.getDienThoai()));
        txtEmail.setText(safe(thongTinCaNhan.getEmail()));
        txtDiaChi.setText(safe(thongTinCaNhan.getDiaChi()));
        txtDiaChiThuongTru.setText(safe(thongTinCaNhan.getDiaChiThuongTru()));
        txtQueQuan.setText(safe(thongTinCaNhan.getQueQuan()));
        cboTinhTrangHonNhan.setSelectedItem(thongTinCaNhan.getTinhTrangHonNhan() != null ? thongTinCaNhan.getTinhTrangHonNhan() : "doc_than");
        txtTrinhDoHocVan.setText(safe(thongTinCaNhan.getTrinhDoHocVan()));
        txtFileCV.setText(safe(thongTinCaNhan.getFileCV()));
        txtKinhNghiem.setText(safe(thongTinCaNhan.getKinhNghiem()));
    }

    private void setPersonalEditMode(boolean editable) {
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

        Color bg = editable ? Color.WHITE : new Color(242, 242, 242);
        Border border = editable
                ? BorderFactory.createLineBorder(UIColors.PRIMARY_PURPLE, 1)
                : BorderFactory.createLineBorder(new Color(220, 220, 220), 1);

        applyEditorVisual(txtHoTen, bg, border);
        applyEditorVisual(txtNgaySinh, bg, border);
        applyEditorVisual(txtCCCD, bg, border);
        applyEditorVisual(txtDienThoai, bg, border);
        applyEditorVisual(txtEmail, bg, border);
        applyEditorVisual(txtDiaChi, bg, border);
        applyEditorVisual(txtDiaChiThuongTru, bg, border);
        applyEditorVisual(txtQueQuan, bg, border);
        applyEditorVisual(txtTrinhDoHocVan, bg, border);
        applyEditorVisual(txtFileCV, bg, border);
        applyEditorVisual(txtKinhNghiem, bg, border);

        cboGioiTinh.setBackground(bg);
        cboTinhTrangHonNhan.setBackground(bg);
        cboGioiTinh.setBorder(border);
        cboTinhTrangHonNhan.setBorder(border);
    }

    private void stylePrimaryButton(JButton button) {
        button.setFont(com.hrm.util.UIFonts.BOLD_NORMAL);
        button.setBackground(UIColors.PRIMARY_PURPLE);
        button.setForeground(UIColors.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void styleWarningButton(JButton button) {
        button.setFont(com.hrm.util.UIFonts.BOLD_NORMAL);
        button.setBackground(new Color(230, 120, 0));
        button.setForeground(UIColors.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void updateActionButtonsByTab(JTabbedPane tabs) {
        if (btnSuaThongTin == null || btnDoiTrangThai == null) return;
        boolean isPersonalTab = tabs.getSelectedIndex() == 0;
        SessionContext sc = SessionContext.getInstance();
        boolean canUpdate = sc.coVaiTro("ADMIN") || sc.coQuyen("EMPLOYEE_UPDATE");
        String myMaNV = sc.getCurrentUser() != null ? sc.getCurrentUser().getNhanVienId() : null;
        boolean isSelfView = maNV != null && maNV.equals(myMaNV);
        btnSuaThongTin .setVisible(isPersonalTab && (canUpdate || isSelfView));
        btnDoiTrangThai.setVisible(isPersonalTab && canUpdate && !isSelfView);
    }

    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    }

    private boolean isValidPhone(String phone) {
        return phone.matches("^\\d{9,11}$");
    }

    private void applyEditorVisual(JTextComponent comp, Color bg, Border border) {
        comp.setBackground(bg);
        comp.setBorder(border);
    }

    // =====================================================================
    // Reusable UI building blocks
    // =====================================================================

    /**
     * Builds a section title label with PRIMARY_PURPLE styling and a bottom separator.
     */
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

    /**
     * Creates a two-column GridBagLayout panel for key-value info rows.
     */
    private JPanel buildInfoGrid() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(UIColors.WHITE);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        return panel;
    }

    /**
     * Adds a label+value pair into an info-grid panel at the given row index.
     * The value parameter is a String; the component is a JLabel.
     */
    private void addInfoRow(JPanel grid, int row, String labelText, String valueText) {
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(com.hrm.util.UIFonts.BOLD_NORMAL);
        lbl.setForeground(UIColors.TEXT_GRAY);
        lbl.setPreferredSize(new Dimension(170, 24));

        JLabel val = new JLabel(valueText);
        val.setFont(com.hrm.util.UIFonts.TEXT_NORMAL);
        val.setForeground(UIColors.TEXT_DARK);

        GridBagConstraints gbcLbl = new GridBagConstraints();
        gbcLbl.gridx     = 0;
        gbcLbl.gridy     = row;
        gbcLbl.anchor    = GridBagConstraints.WEST;
        gbcLbl.insets    = new Insets(3, 0, 3, 12);
        gbcLbl.fill      = GridBagConstraints.NONE;

        GridBagConstraints gbcVal = new GridBagConstraints();
        gbcVal.gridx     = 1;
        gbcVal.gridy     = row;
        gbcVal.anchor    = GridBagConstraints.WEST;
        gbcVal.insets    = new Insets(3, 0, 3, 0);
        gbcVal.fill      = GridBagConstraints.HORIZONTAL;
        gbcVal.weightx   = 1.0;

        grid.add(lbl, gbcLbl);
        grid.add(val, gbcVal);
    }

    /**
     * Adds a label+component pair (value is a Component, e.g. a colored JLabel)
     * into an info-grid panel at the given row index.
     */
    private void addInfoRow(JPanel grid, int row, String labelText, Component valueComponent) {
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(com.hrm.util.UIFonts.BOLD_NORMAL);
        lbl.setForeground(UIColors.TEXT_GRAY);
        lbl.setPreferredSize(new Dimension(170, 24));

        GridBagConstraints gbcLbl = new GridBagConstraints();
        gbcLbl.gridx     = 0;
        gbcLbl.gridy     = row;
        gbcLbl.anchor    = GridBagConstraints.WEST;
        gbcLbl.insets    = new Insets(3, 0, 3, 12);
        gbcLbl.fill      = GridBagConstraints.NONE;

        GridBagConstraints gbcVal = new GridBagConstraints();
        gbcVal.gridx     = 1;
        gbcVal.gridy     = row;
        gbcVal.anchor    = GridBagConstraints.WEST;
        gbcVal.insets    = new Insets(3, 0, 3, 0);
        gbcVal.fill      = GridBagConstraints.HORIZONTAL;
        gbcVal.weightx   = 1.0;

        grid.add(lbl, gbcLbl);
        grid.add(valueComponent, gbcVal);
    }

    /**
     * Convenience GridBagConstraints spanning 'colspan' columns at (col, row).
     */
    private GridBagConstraints buildGbc(int col, int row, int colspan) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx      = col;
        gbc.gridy      = row;
        gbc.gridwidth  = colspan;
        gbc.anchor     = GridBagConstraints.WEST;
        gbc.insets     = new Insets(4, 0, 4, 0);
        gbc.fill       = GridBagConstraints.HORIZONTAL;
        gbc.weightx    = 1.0;
        return gbc;
    }

    /**
     * Creates a colored status badge JLabel for an employee or record status.
     *
     * @param statusKey  raw status string, e.g. "dang_lam_viec", "hieu_luc"
     * @param displayText text to show inside the badge
     */
    private JLabel createStatusBadge(String statusKey, String displayText) {
        JLabel badge = new JLabel("  " + displayText + "  ") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badge.setOpaque(false);
        badge.setFont(com.hrm.util.UIFonts.BOLD_SMALL);
        badge.setForeground(UIColors.WHITE);
        badge.setBackground(resolveStatusColor(statusKey));
        badge.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        return badge;
    }

    /**
     * Returns a JLabel component (opaque, colored) suitable for embedding in
     * an info-grid row to display a status value.
     */
    private JLabel buildStatusLabelComponent(String statusKey, String displayText) {
        Color bg = resolveStatusColor(statusKey);
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
        lbl.setForeground(UIColors.WHITE);
        lbl.setBackground(bg);
        lbl.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
        return lbl;
    }

    /**
     * Maps a raw status key to a display color.
     */
    private Color resolveStatusColor(String statusKey) {
        if (statusKey == null) return UIColors.TEXT_GRAY;
        switch (statusKey) {
            case "dang_lam_viec":
            case "hieu_luc":
            case "dung_gio":
                return UIColors.SUCCESS_GREEN;
            case "tam_nghi":
            case "cho_duyet":
            case "di_muon":
            case "ve_som":
                return new Color(230, 120, 0);   // amber/warning
            case "nghi_viec":
            case "tu_choi":
            case "huy":
            case "vang_mat":
                return UIColors.DANGER_RED;
            case "het_han":
            case "het_hieu_luc":
                return UIColors.TEXT_GRAY;
            case "thanh_ly":
                return new Color(23, 162, 184);  // info blue
            case "nghi_phep":
                return new Color(0, 123, 200);
            case "cong_tac":
                return new Color(100, 60, 200);
            default:
                return UIColors.TEXT_GRAY;
        }
    }

    // =====================================================================
    // Custom table cell renderers
    // =====================================================================

    /**
     * Highlights the Trang thai column (index 5) in the appointment table.
     */
    private class AppointmentStatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int col) {
            Component c = super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, col);
            setHorizontalAlignment(SwingConstants.CENTER);
            setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));

            if (!isSelected) {
                c.setBackground(row % 2 == 0 ? Color.WHITE : UIColors.TABLE_ALT_ROW);
                c.setForeground(UIColors.TEXT_DARK);

                if (col == 5 && value != null) {
                    String v = value.toString();
                    if (v.contains("Hiệu lực") || v.contains("hieu_luc")) {
                        c.setForeground(UIColors.SUCCESS_GREEN);
                    } else if (v.contains("Chờ duyệt") || v.contains("cho_duyet")) {
                        c.setForeground(new Color(230, 120, 0));
                    } else if (v.contains("Hết") || v.contains("Tu choi")) {
                        c.setForeground(UIColors.DANGER_RED);
                    }
                    ((JLabel) c).setFont(com.hrm.util.UIFonts.BOLD_SMALL);
                }
            }
            return c;
        }
    }
   

    // =====================================================================
    // Value formatting helpers
    // =====================================================================

    /** Returns the string if non-null and non-empty, otherwise empty string. */
    private static String safe(String s) {
        return (s != null && !s.isEmpty()) ? s : "";
    }
}

