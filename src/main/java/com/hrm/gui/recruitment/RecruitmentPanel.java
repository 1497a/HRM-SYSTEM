package com.hrm.gui.recruitment;

import com.hrm.model.TinTuyenDung;
import com.hrm.model.UngVien;
import com.hrm.model.YeuCauTuyenDung;
import com.hrm.model.PhongBan;
import com.hrm.model.ChucVu;
import com.hrm.bus.TuyenDungBUS;
import com.hrm.bus.PhongBanBUS;
import com.hrm.bus.ChucVuBUS;
import com.hrm.bus.KetQua;
import com.hrm.util.SessionContext;
import com.hrm.util.UIColors;
import com.hrm.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.List;

import static javax.swing.RowSorter.SortKey;
import static javax.swing.SortOrder.ASCENDING;

/**
 * Panel quản lý tuyển dụng.
 * Tab 1: Yêu cầu tuyển dụng
 * Tab 2: Tin tuyển dụng
 * Tab 3: Ứng viên
 */
public class RecruitmentPanel extends JPanel {

    private final TuyenDungBUS recruitmentService;

    // Tab 1 - Yêu cầu tuyển dụng
    private JTable tblYeuCau;
    private DefaultTableModel modelYeuCau;
    private TableRowSorter<DefaultTableModel> sorterYeuCau;
    private JComboBox<String> cboTrangThaiYC;
    private JButton btnTaoYeuCau;
    private JButton btnPheDuyet;
    private JButton btnTuChoi;

    // Tab 2 - Tin tuyển dụng
    private JTable tblTin;
    private DefaultTableModel modelTin;
    private TableRowSorter<DefaultTableModel> sorterTin;
    private JComboBox<String> cboTrangThaiTin;
    private JButton btnDangTin;
    private JButton btnDongTin;
    private JButton btnLamMoiTin;

    // Tab 3 - Ứng viên
    private JTable tblUngVien;
    private DefaultTableModel modelUngVien;
    private TableRowSorter<DefaultTableModel> sorterUngVien;
    private JComboBox<String> cboTrangThaiUV;
    private JButton btnChuyenTrangThai;
    private JButton btnChuyenNV;
    private JButton btnTaoUngVien;
    private JButton btnLamMoiUV;

    private List<YeuCauTuyenDung> danhSachYC;
    private List<TinTuyenDung> danhSachTin;
    private List<UngVien> danhSachUV;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public RecruitmentPanel() {
        this.recruitmentService = TuyenDungBUS.getInstance();
        setLayout(new BorderLayout());
        setBackground(UIColors.LIGHT_GRAY_BG);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(com.hrm.util.UIFonts.TEXT_MEDIUM);
        tabbedPane.setBackground(UIColors.WHITE);

        tabbedPane.addTab("Yêu cầu tuyển dụng", buildYeuCauTab());
        tabbedPane.addTab("Tin tuyển dụng", buildTinTab());
        tabbedPane.addTab("Ứng viên", buildUngVienTab());

        add(tabbedPane, BorderLayout.CENTER);

        setupPermissions();
        loadAll();
    }

    private void setupPermissions() {
        SessionContext sc = SessionContext.getInstance();
        boolean canRequest = sc.coQuyen("RECRUITMENT_REQUEST") || sc.coQuyen("RECRUITMENT_MANAGE");
        boolean canManage  = sc.coQuyen("RECRUITMENT_MANAGE");

        btnTaoYeuCau.setVisible(canRequest);
        btnPheDuyet.setVisible(canManage);
        btnTuChoi.setVisible(canManage);

        btnDangTin.setVisible(canManage);
        btnDongTin.setVisible(canManage);

        btnTaoUngVien.setVisible(canManage);
        btnChuyenTrangThai.setVisible(canManage);
        btnChuyenNV.setVisible(canManage);
    }

    // ────────────────────────────────────────────────
    // Tab 1: Yêu cầu tuyển dụng
    // ────────────────────────────────────────────────

    private JPanel buildYeuCauTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(UIColors.LIGHT_GRAY_BG);
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        toolbar.setOpaque(false);

        btnTaoYeuCau   = UIHelper.createPrimaryButton("Tạo yêu cầu");
        btnPheDuyet    = UIHelper.createSuccessButton("Phê duyệt");
        btnTuChoi      = UIHelper.createDangerButton("Từ chối");

        btnTaoYeuCau.addActionListener(e -> taoYeuCau());
        btnPheDuyet.addActionListener(e -> pheDuyetYeuCau());
        btnTuChoi.addActionListener(e -> tuChoiYeuCau());

        cboTrangThaiYC = new JComboBox<>(new String[]{"Tất cả", "Chờ duyệt", "Đã duyệt", "Từ chối", "Đã tuyển đủ"});

        toolbar.add(btnTaoYeuCau);
        toolbar.add(btnPheDuyet);
        toolbar.add(btnTuChoi);
        toolbar.add(new JLabel("Trạng thái:"));
        toolbar.add(cboTrangThaiYC);

        String[] cols = {"Mã YC", "Vị trí", "Phòng ban", "Số lượng", "Hạn tuyển dụng", "Trạng thái"};
        modelYeuCau = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
            @Override public Class<?> getColumnClass(int columnIndex) {
                return (columnIndex == 0 || columnIndex == 3) ? Integer.class : String.class;
            }
        };

        tblYeuCau = buildTable(modelYeuCau);
        applyColWidths(tblYeuCau, new int[]{70, 220, 180, 80, 120, 130});
        tblYeuCau.getColumnModel().getColumn(5).setCellRenderer(new StatusCellRenderer());

        sorterYeuCau = new TableRowSorter<>(modelYeuCau);
        tblYeuCau.setRowSorter(sorterYeuCau);

        sorterYeuCau.setComparator(0, Comparator.comparingInt(a -> (Integer) a));
        sorterYeuCau.setComparator(3, Comparator.comparingInt(a -> (Integer) a));
        sorterYeuCau.setComparator(4, dateStringComparator());

        sorterYeuCau.setSortable(1, true);
        sorterYeuCau.setSortable(2, true);

        sorterYeuCau.setSortKeys(List.of(new SortKey(0, ASCENDING)));

        UIHelper.attachStatusFilter(sorterYeuCau, cboTrangThaiYC, 5);

        JScrollPane scroll = new JScrollPane(tblYeuCau);
        scroll.setBorder(new TitledBorder("Danh sách yêu cầu tuyển dụng"));

        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    // ────────────────────────────────────────────────
    // Tab 2: Tin tuyển dụng
    // ────────────────────────────────────────────────

    private JPanel buildTinTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(UIColors.LIGHT_GRAY_BG);
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        toolbar.setOpaque(false);

        btnDangTin    = UIHelper.createSuccessButton("Đăng tin");
        btnDongTin    = UIHelper.createDangerButton("Đóng tin");
        btnLamMoiTin  = UIHelper.createDefaultButton("Làm mới");

        btnDangTin.addActionListener(e -> dangTin());
        btnDongTin.addActionListener(e -> dongTin());
        btnLamMoiTin.addActionListener(e -> loadTin());

        cboTrangThaiTin = new JComboBox<>(new String[]{"Tất cả", "Đang tuyển", "Tạm dừng", "Đã đóng"});

        toolbar.add(btnDangTin);
        toolbar.add(btnDongTin);
        toolbar.add(btnLamMoiTin);
        toolbar.add(new JLabel("Trạng thái:"));
        toolbar.add(cboTrangThaiTin);

        String[] cols = {"Mã tin", "Tiêu đề", "Phòng ban", "Chức vụ", "Hạn nộp", "Số đơn", "Trạng thái"};
        modelTin = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
            @Override public Class<?> getColumnClass(int columnIndex) {
                return (columnIndex == 0 || columnIndex == 5) ? Integer.class : String.class;
            }
        };

        tblTin = buildTable(modelTin);
        applyColWidths(tblTin, new int[]{60, 220, 160, 140, 110, 70, 110});
        tblTin.getColumnModel().getColumn(6).setCellRenderer(new StatusCellRenderer());

        sorterTin = new TableRowSorter<>(modelTin);
        tblTin.setRowSorter(sorterTin);

        sorterTin.setComparator(0, Comparator.comparingInt(a -> (Integer) a));
        sorterTin.setComparator(5, Comparator.comparingInt(a -> (Integer) a));
        sorterTin.setComparator(4, dateStringComparator());

        sorterTin.setSortable(1, true);
        sorterTin.setSortable(2, true);
        sorterTin.setSortable(3, true);

        sorterTin.setSortKeys(List.of(new SortKey(0, ASCENDING)));

        UIHelper.attachStatusFilter(sorterTin, cboTrangThaiTin, 6);

        JScrollPane scroll = new JScrollPane(tblTin);
        scroll.setBorder(new TitledBorder("Danh sách tin tuyển dụng"));

        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    // ────────────────────────────────────────────────
    // Tab 3: Ứng viên
    // ────────────────────────────────────────────────

    private JPanel buildUngVienTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(UIColors.LIGHT_GRAY_BG);
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        toolbar.setOpaque(false);

        btnTaoUngVien       = UIHelper.createPrimaryButton("+ Tạo ứng viên");
        btnChuyenTrangThai  = UIHelper.createPrimaryButton("Chuyển trạng thái");
        btnChuyenNV         = UIHelper.createSuccessButton("Chuyển thành NV");
        btnLamMoiUV         = UIHelper.createDefaultButton("Làm mới");

        btnTaoUngVien.addActionListener(e -> taoUngVien());
        btnChuyenTrangThai.addActionListener(e -> chuyenTrangThaiUV());
        btnChuyenNV.addActionListener(e -> chuyenUVThanhNV());
        btnLamMoiUV.addActionListener(e -> loadUngVien());

        cboTrangThaiUV = new JComboBox<>(new String[]{
                "Tất cả", "Mới", "Đang phỏng vấn", "Trúng tuyển", "Từ chối", "Đã chuyển thành nhân viên"
        });

        toolbar.add(btnTaoUngVien);
        toolbar.add(btnChuyenTrangThai);
        toolbar.add(btnChuyenNV);
        toolbar.add(btnLamMoiUV);
        toolbar.add(new JLabel("Trạng thái:"));
        toolbar.add(cboTrangThaiUV);

        String[] cols = {"Mã UV", "Họ tên", "Email", "Điện thoại", "Vị trí", "Ngày nộp", "Trạng thái"};
        modelUngVien = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
            @Override public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 0 ? Integer.class : String.class;
            }
        };

        tblUngVien = buildTable(modelUngVien);
        applyColWidths(tblUngVien, new int[]{70, 180, 220, 130, 220, 110, 140});
        tblUngVien.getColumnModel().getColumn(6).setCellRenderer(new StatusCellRenderer());

        sorterUngVien = new TableRowSorter<>(modelUngVien);
        tblUngVien.setRowSorter(sorterUngVien);

        for (int i = 0; i < modelUngVien.getColumnCount(); i++) {
            sorterUngVien.setSortable(i, false);
        }
        sorterUngVien.setSortable(0, true);
        sorterUngVien.setSortable(1, true);
        sorterUngVien.setSortable(4, true);

        sorterUngVien.setComparator(0, Comparator.comparingInt(a -> (Integer) a));
        sorterUngVien.setComparator(1, UIHelper.vietnameseNameComparator());
        sorterUngVien.setComparator(4, Comparator.comparing(String::toString, String.CASE_INSENSITIVE_ORDER));
        sorterUngVien.setComparator(5, dateStringComparator());

        sorterUngVien.setSortKeys(List.of(new SortKey(0, ASCENDING)));

        UIHelper.attachStatusFilter(sorterUngVien, cboTrangThaiUV, 6);

        JScrollPane scroll = new JScrollPane(tblUngVien);
        scroll.setBorder(new TitledBorder("Danh sách ứng viên"));

        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    // ────────────────────────────────────────────────
    // Comparator cho cột ngày "dd/MM/yyyy"
    // ────────────────────────────────────────────────
    private Comparator<Object> dateStringComparator() {
        return Comparator.comparing(o -> {
            if (o == null || !(o instanceof String s) || s.trim().isEmpty()) {
                return null;
            }
            try {
                return LocalDate.parse(s.trim(), DATE_FORMAT);
            } catch (DateTimeParseException e) {
                return null;
            }
        }, Comparator.nullsLast(Comparator.naturalOrder()));
    }

    // ────────────────────────────────────────────────
    // Load dữ liệu
    // ────────────────────────────────────────────────

    private void loadAll() {
        loadYeuCau();
        loadTin();
        loadUngVien();
    }

    private void loadYeuCau() {
        modelYeuCau.setRowCount(0);
        try {
            danhSachYC = recruitmentService.getAllYeuCau();
            for (YeuCauTuyenDung yc : danhSachYC) {
                String han = yc.getHanTuyenDung() != null ? yc.getHanTuyenDung().format(DATE_FORMAT) : "";
                modelYeuCau.addRow(new Object[]{
                        yc.getMaYeuCau(),
                        yc.getTenChucVu() != null ? yc.getTenChucVu() : (yc.getMaChucVu() != null ? yc.getMaChucVu() : ""),
                        yc.getTenPhongBan() != null ? yc.getTenPhongBan() : (yc.getId() != null ? yc.getId() : ""),
                        yc.getSoLuong(),
                        han,
                        yc.getTrangThaiDisplay()
                });
            }
        } catch (Exception e) {
            showError("Lỗi tải yêu cầu tuyển dụng: " + e.getMessage());
        }
    }

    private void loadTin() {
        modelTin.setRowCount(0);
        try {
            danhSachTin = recruitmentService.getAllTinTuyenDung();
            for (TinTuyenDung tin : danhSachTin) {
                String het = tin.getHanNopHoSo() != null ? tin.getHanNopHoSo().format(DATE_FORMAT) : "";
                Integer soDon = tin.getSoUngVien() != 0 ? tin.getSoUngVien() : 0;
                modelTin.addRow(new Object[]{
                        tin.getMaTin(),
                        tin.getTieuDe(),
                        tin.getTenPhongBan() != null ? tin.getTenPhongBan() : "",
                        tin.getTenChucVu()   != null ? tin.getTenChucVu()   : "",
                        het,
                        soDon,
                        tin.getTrangThaiDisplay()
                });
            }
        } catch (Exception e) {
            showError("Lỗi tải tin tuyển dụng: " + e.getMessage());
        }
    }

    private void loadUngVien() {
        modelUngVien.setRowCount(0);
        try {
            danhSachUV = recruitmentService.getAllUngVien();
            for (UngVien uv : danhSachUV) {
                String nop = uv.getNgayTao() != null ? uv.getNgayTao().format(DATE_FORMAT) : "";
                String viTri = uv.getTenTin() != null && !uv.getTenTin().isBlank()
                        ? uv.getTenTin()
                        : (uv.getTenTin() != null ? "Mã tin: " + uv.getMaTin() : "[Chưa có tin]");
                modelUngVien.addRow(new Object[]{
                        uv.getMaUngVien(),
                        uv.getHoTen(),
                        uv.getEmail(),
                        uv.getDienThoai(),
                        viTri,
                        nop,
                        uv.getTrangThaiDisplay()
                });
            }
        } catch (Exception e) {
            showError("Lỗi tải ứng viên: " + e.getMessage());
        }
    }

    // ────────────────────────────────────────────────
    // Hành động - Tab 1
    // ────────────────────────────────────────────────

    private void taoYeuCau() {
        PhongBanBUS pbBUS = new PhongBanBUS();
        ChucVuBUS cvBUS = new ChucVuBUS();
        java.util.List<PhongBan> dsPhongBan = pbBUS.getActiveDepartments();
        java.util.List<ChucVu> dsChucVu = cvBUS.getRecruitablePositions();

        JComboBox<PhongBan> cboPhongBan = new JComboBox<>();
        for (PhongBan pb : dsPhongBan) cboPhongBan.addItem(pb);
        cboPhongBan.setRenderer((list, value, index, sel, focus) ->
                new JLabel(value != null ? value.getTenPhongBan() : ""));

        JComboBox<ChucVu> cboChucVu = new JComboBox<>();
        for (ChucVu cv : dsChucVu) cboChucVu.addItem(cv);
        cboChucVu.setRenderer((list, value, index, sel, focus) ->
                new JLabel(value != null ? value.getTenChucVu() : ""));

        JSpinner spinSoLuong = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));

        SpinnerDateModel dateModel = new SpinnerDateModel(
                java.util.Date.from(java.time.LocalDate.now().plusMonths(1)
                        .atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()),
                null, null, java.util.Calendar.DAY_OF_MONTH);
        JSpinner spinHan = new JSpinner(dateModel);
        spinHan.setEditor(new JSpinner.DateEditor(spinHan, "dd/MM/yyyy"));

        JTextField txtLyDo = new JTextField(20);
        txtLyDo.setFont(com.hrm.util.UIFonts.TEXT_NORMAL);

        JPanel form = new JPanel(new GridLayout(5, 2, 8, 8));
        form.add(new JLabel("Phòng ban (*):")); form.add(cboPhongBan);
        form.add(new JLabel("Chức vụ / Vị trí (*):")); form.add(cboChucVu);
        form.add(new JLabel("Số lượng:")); form.add(spinSoLuong);
        form.add(new JLabel("Hạn tuyển dụng:")); form.add(spinHan);
        form.add(new JLabel("Lý do:")); form.add(txtLyDo);

        int result = JOptionPane.showConfirmDialog(this, form,
                "Tạo yêu cầu tuyển dụng", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;

        PhongBan pb = (PhongBan) cboPhongBan.getSelectedItem();
        ChucVu cv = (ChucVu) cboChucVu.getSelectedItem();
        if (pb == null || cv == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn phòng ban và chức vụ.",
                    "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        java.util.Date selectedDate = (java.util.Date) spinHan.getValue();
        java.time.LocalDate hanTuyenDung = selectedDate.toInstant()
                .atZone(java.time.ZoneId.systemDefault()).toLocalDate();

        try {
            YeuCauTuyenDung yc = new YeuCauTuyenDung();
            yc.setId(pb.getId());
            yc.setMaChucVu(cv.getId());
            yc.setSoLuong((int) spinSoLuong.getValue());
            yc.setHanTuyenDung(hanTuyenDung);
            yc.setLyDo(txtLyDo.getText().trim());
            yc.setTrangThai("cho_duyet");

            KetQua<?> sr = recruitmentService.taoYeuCau(yc);
            if (sr.isSuccess()) {
                JOptionPane.showMessageDialog(this, "Tạo yêu cầu thành công!",
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
                loadYeuCau();
            } else {
                showError(sr.getMessage());
            }
        } catch (Exception ex) {
            showError("Lỗi tạo yêu cầu: " + ex.getMessage());
        }
    }

    private void pheDuyetYeuCau() {
        int viewRow = tblYeuCau.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn yêu cầu.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = tblYeuCau.convertRowIndexToModel(viewRow);
        int maYC = (Integer) modelYeuCau.getValueAt(modelRow, 0);

        try {
            KetQua<?> r = recruitmentService.duyetYeuCau(maYC);
            if (r.isSuccess()) {
                JOptionPane.showMessageDialog(this, "Đã phê duyệt.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                loadYeuCau();
            } else {
                showError(r.getMessage());
            }
        } catch (Exception ex) {
            showError("Lỗi phê duyệt: " + ex.getMessage());
        }
    }

    private void tuChoiYeuCau() {
        int viewRow = tblYeuCau.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn yêu cầu.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = tblYeuCau.convertRowIndexToModel(viewRow);
        int maYC = (Integer) modelYeuCau.getValueAt(modelRow, 0);

        int opt = JOptionPane.showConfirmDialog(this,
                "Từ chối yêu cầu #" + maYC + "?", "Xác nhận",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (opt != JOptionPane.YES_OPTION) return;

        try {
            KetQua<?> r = recruitmentService.tuChoiYeuCau(maYC);
            if (r.isSuccess()) {
                JOptionPane.showMessageDialog(this, "Đã từ chối.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                loadYeuCau();
            } else {
                showError(r.getMessage());
            }
        } catch (Exception ex) {
            showError("Lỗi từ chối: " + ex.getMessage());
        }
    }

    // ────────────────────────────────────────────────
    // Hành động - Tab 2
    // ────────────────────────────────────────────────

    private void dangTin() {
        java.util.List<YeuCauTuyenDung> dsYCDaDuyet = recruitmentService.getAllYeuCau().stream()
                .filter(yc -> "da_duyet".equals(yc.getTrangThai()))
                .collect(java.util.stream.Collectors.toList());

        if (dsYCDaDuyet.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Không có yêu cầu nào đã được duyệt. Vui lòng duyệt yêu cầu trước.",
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JTextField txtTieuDe = new JTextField(25);
        txtTieuDe.setFont(com.hrm.util.UIFonts.TEXT_MEDIUM);

        JComboBox<YeuCauTuyenDung> cboYeuCau = new JComboBox<>();
        for (YeuCauTuyenDung yc : dsYCDaDuyet) cboYeuCau.addItem(yc);
        cboYeuCau.setRenderer((list, value, index, sel, focus) -> {
            if (value == null) return new JLabel("");
            String pb = value.getTenPhongBan() != null ? value.getTenPhongBan() : value.getId();
            String cv = value.getTenChucVu() != null ? value.getTenChucVu() : value.getMaChucVu();
            return new JLabel("#" + value.getMaYeuCau() + " - " + pb + " - " + cv);
        });

        JTextField txtMucLuong = new JTextField(20);
        txtMucLuong.setFont(com.hrm.util.UIFonts.TEXT_MEDIUM);

        JTextField txtDiaDiem = new JTextField(20);
        txtDiaDiem.setFont(com.hrm.util.UIFonts.TEXT_MEDIUM);

        SpinnerDateModel dateModel = new SpinnerDateModel(
                java.util.Date.from(java.time.LocalDate.now().plusMonths(1)
                        .atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()),
                null, null, java.util.Calendar.DAY_OF_MONTH);
        JSpinner spinHanNop = new JSpinner(dateModel);
        spinHanNop.setEditor(new JSpinner.DateEditor(spinHanNop, "dd/MM/yyyy"));

        JPanel form = new JPanel(new GridLayout(5, 2, 8, 8));
        form.add(new JLabel("Yêu cầu tuyển dụng:"));
        form.add(cboYeuCau);
        form.add(new JLabel("Tiêu đề tin:"));
        form.add(txtTieuDe);
        form.add(new JLabel("Mức lương:"));
        form.add(txtMucLuong);
        form.add(new JLabel("Địa điểm:"));
        form.add(txtDiaDiem);
        form.add(new JLabel("Hạn nộp hồ sơ:"));
        form.add(spinHanNop);

        int result = JOptionPane.showConfirmDialog(this, form,
                "Đăng tin tuyển dụng", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;

        String tieuDe = txtTieuDe.getText().trim();
        if (tieuDe.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập tiêu đề.",
                    "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        YeuCauTuyenDung selectedYC = (YeuCauTuyenDung) cboYeuCau.getSelectedItem();
        if (selectedYC == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn yêu cầu tuyển dụng.",
                    "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        java.util.Date selectedDate = (java.util.Date) spinHanNop.getValue();
        java.time.LocalDate hanNop = selectedDate.toInstant()
                .atZone(java.time.ZoneId.systemDefault()).toLocalDate();

        try {
            TinTuyenDung tin = new TinTuyenDung();
            tin.setTieuDe(tieuDe);
            tin.setMaYeuCau(selectedYC.getMaYeuCau());
            tin.setMucLuong(txtMucLuong.getText().trim());
            tin.setDiaDiem(txtDiaDiem.getText().trim());
            tin.setHanNopHoSo(hanNop);
            tin.setTrangThai("dang_tuyen");

            KetQua<?> sr = recruitmentService.taoTin(tin);
            if (sr.isSuccess()) {
                JOptionPane.showMessageDialog(this, "Đã đăng tin tuyển dụng!",
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
                loadTin();
            } else {
                showError(sr.getMessage());
            }
        } catch (Exception ex) {
            showError("Lỗi đăng tin: " + ex.getMessage());
        }
    }

    private void dongTin() {
        int viewRow = tblTin.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn tin.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = tblTin.convertRowIndexToModel(viewRow);
        int maTin = (Integer) modelTin.getValueAt(modelRow, 0);

        int opt = JOptionPane.showConfirmDialog(this,
                "Đóng tin #" + maTin + "?", "Xác nhận",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (opt != JOptionPane.YES_OPTION) return;

        try {
            KetQua<?> r = recruitmentService.dongTin(maTin);
            if (r.isSuccess()) {
                JOptionPane.showMessageDialog(this, "Đã đóng tin.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                loadTin();
            } else {
                showError(r.getMessage());
            }
        } catch (Exception ex) {
            showError("Lỗi đóng tin: " + ex.getMessage());
        }
    }

    // ────────────────────────────────────────────────
    // Hành động - Tab 3
    // ────────────────────────────────────────────────

    private void taoUngVien() {
        HopThoaiTaoUngVien dialog = new HopThoaiTaoUngVien(
                SwingUtilities.getWindowAncestor(this), recruitmentService);
        dialog.setVisible(true);
        if (dialog.isThanhCong()) {
            loadUngVien();
        }
    }

    private void chuyenTrangThaiUV() {
        int viewRow = tblUngVien.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn ứng viên.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = tblUngVien.convertRowIndexToModel(viewRow);
        int maUV = (Integer) modelUngVien.getValueAt(modelRow, 0);

        UngVien uv = findUngVien(maUV);
        if (uv == null) {
            showError("Không tìm thấy ứng viên.");
            return;
        }

        if (uv.getMaNV() != null && !uv.getMaNV().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Ứng viên đã được chuyển thành nhân viên → không thể thay đổi trạng thái tuyển dụng.",
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String[] codes  = {"moi", "dang_phong_van", "trung_tuyen", "tu_choi"};
        String[] labels = {"Mới", "Đang phỏng vấn", "Trúng tuyển", "Từ chối"};

        JComboBox<String> cbo = new JComboBox<>(labels);
        cbo.setFont(com.hrm.util.UIFonts.TEXT_MEDIUM);

        int opt = JOptionPane.showConfirmDialog(this,
                new Object[]{"Trạng thái mới:", cbo},
                "Chuyển trạng thái ứng viên", JOptionPane.OK_CANCEL_OPTION);

        if (opt != JOptionPane.OK_OPTION) return;

        String newStatus = codes[cbo.getSelectedIndex()];

        try {
            KetQua<?> r = recruitmentService.capNhatTrangThaiUV(maUV, newStatus);
            if (r.isSuccess()) {
                JOptionPane.showMessageDialog(this, "Đã cập nhật trạng thái.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                loadUngVien();
            } else {
                showError(r.getMessage());
            }
        } catch (Exception ex) {
            showError("Lỗi cập nhật trạng thái: " + ex.getMessage());
        }
    }

    private void chuyenUVThanhNV() {
        int viewRow = tblUngVien.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn ứng viên.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = tblUngVien.convertRowIndexToModel(viewRow);
        int maUV = (Integer) modelUngVien.getValueAt(modelRow, 0);
        String hoTen = (String) modelUngVien.getValueAt(modelRow, 1);

        UngVien uv = findUngVien(maUV);
        if (uv == null) {
            showError("Không tìm thấy ứng viên.");
            return;
        }

        if (uv.getMaNV() != null && !uv.getMaNV().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ứng viên đã được chuyển thành nhân viên.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        if (!"trung_tuyen".equals(uv.getTrangThai())) {
            JOptionPane.showMessageDialog(this,
                    "Chỉ ứng viên trạng thái \"Trúng tuyển\" mới có thể chuyển thành nhân viên.",
                    "Không hợp lệ", JOptionPane.WARNING_MESSAGE);
            return;
        }

        TinTuyenDung tinUV = null;
        if (danhSachTin != null) {
            for (TinTuyenDung t : danhSachTin) {
                if (t.getMaTin() == uv.getMaTin()) {
                    tinUV = t;
                    break;
                }
            }
        }
        String pbInfo = (tinUV != null && tinUV.getTenPhongBan() != null && !tinUV.getTenPhongBan().isEmpty())
                ? tinUV.getTenPhongBan() : "(chưa xác định)";
        String cvInfo = (tinUV != null && tinUV.getTenChucVu() != null && !tinUV.getTenChucVu().isEmpty())
                ? tinUV.getTenChucVu() : "(chưa xác định)";
        boolean thieu = "(chưa xác định)".equals(pbInfo) || "(chưa xác định)".equals(cvInfo);

        String confirmMsg = "Chuyển \"" + hoTen + "\" thành nhân viên chính thức?\n\n"
                + "Sẽ tự động bổ nhiệm:\n"
                + "  Phòng ban : " + pbInfo + "\n"
                + "  Chức vụ   : " + cvInfo
                + (thieu ? "\n\nCHÚ Ý: Thiếu thông tin - bổ nhiệm cần tạo thủ công sau." : "");

        int opt = JOptionPane.showConfirmDialog(this,
                confirmMsg,
                "Xác nhận chuyển thành nhân viên", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (opt != JOptionPane.YES_OPTION) return;

        try {
            KetQua<?> r = recruitmentService.chuyenUVThanhNV(maUV);
            if (r.isSuccess()) {
                JOptionPane.showMessageDialog(this,
                        "Đã chuyển thành nhân viên thành công!\n" + r.getMessage(),
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
                loadUngVien();
            } else {
                showError(r.getMessage());
            }
        } catch (Exception ex) {
            showError("Lỗi chuyển thành nhân viên: " + ex.getMessage());
        }
    }

    // ────────────────────────────────────────────────
    // Helper methods
    // ────────────────────────────────────────────────

    private UngVien findUngVien(int maUV) {
        if (danhSachUV == null) return null;
        for (UngVien u : danhSachUV) {
            if (u.getMaUngVien() == maUV) return u;
        }
        return null;
    }

    private JTable buildTable(DefaultTableModel model) {
        JTable t = new JTable(model);
        t.setRowHeight(28);
        t.setFont(com.hrm.util.UIFonts.TEXT_NORMAL);
        t.getTableHeader().setFont(com.hrm.util.UIFonts.BOLD_NORMAL);
        t.getTableHeader().setBackground(UIColors.PRIMARY_PURPLE);
        t.getTableHeader().setForeground(UIColors.TEXT_DARK);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.setSelectionBackground(UIColors.LIGHT_PURPLE);
        t.setSelectionForeground(UIColors.TEXT_DARK);
        return t;
    }

    private void applyColWidths(JTable t, int[] w) {
        for (int i = 0; i < w.length && i < t.getColumnCount(); i++) {
            t.getColumnModel().getColumn(i).setPreferredWidth(w[i]);
        }
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    private static class StatusCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int col) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
            setHorizontalAlignment(CENTER);
            if (!isSelected && value != null) {
                String v = value.toString();
                if (v.contains("Đã duyệt") || v.contains("Đã tuyển đủ") ||
                    v.contains("Trúng tuyển") || v.contains("Đang tuyển") ||
                    v.contains("Đã chuyển thành nhân viên")) {
                    c.setForeground(UIColors.SUCCESS_GREEN);
                } else if (v.contains("Từ chối") || v.contains("Đã đóng")) {
                    c.setForeground(UIColors.DANGER_RED);
                } else if (v.contains("Chờ duyệt") || v.contains("Đang phỏng vấn") ||
                           v.contains("Tạm dừng")) {
                    c.setForeground(UIColors.WARNING_YELLOW);
                } else {
                    c.setForeground(UIColors.INFO_BLUE);
                }
                ((JLabel) c).setFont(com.hrm.util.UIFonts.BOLD_SMALL);
            }
            return c;
        }
    }
}
