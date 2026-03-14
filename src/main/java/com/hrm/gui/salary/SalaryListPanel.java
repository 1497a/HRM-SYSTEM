package com.hrm.gui.salary;

import com.hrm.bus.KetQua;
import com.hrm.bus.LuongBUS;
import com.hrm.bus.XacThucBUS;
import com.hrm.model.BangLuong;
import com.hrm.model.ChiTietLuong;
import com.hrm.model.DataScope;
import com.hrm.model.TaiKhoan;
import com.hrm.util.SessionContext;
import com.hrm.util.UIColors;
import com.hrm.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class SalaryListPanel extends JPanel {

    private final LuongBUS salaryService;
    private final DataScope currentScope;
    private final boolean canCalculate;
    private final boolean canLock;
    private final String maNVHienTai;
    private final java.util.Set<String> maNVTrongPhamVi; // null = ALL

    private JTable tblBangLuong;
    private DefaultTableModel modelBangLuong;
    private JButton btnTinhLuong;
    private JButton btnTinhLaiBangLuong;
    private JButton btnDuyetBangLuong;
    private JButton btnKhoaBangLuong;

    private JTable tblChiTiet;
    private DefaultTableModel modelChiTiet;
    private JButton btnTinhLaiNhanVien;
    private JTabbedPane tabbedPane;

    private List<BangLuong> danhSachBL = new ArrayList<>();
    private List<ChiTietLuong> currentChiTietList = new ArrayList<>();
    private int selectedMaBL = -1;

    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final NumberFormat MONEY_FORMAT = NumberFormat.getNumberInstance(new Locale("vi", "VN"));

    public SalaryListPanel() {
        this.salaryService = LuongBUS.getInstance();

        SessionContext session = SessionContext.getInstance();
        TaiKhoan currentUser = session.getCurrentUser();
        this.currentScope = XacThucBUS.getInstance().getScopeForAction("PAYROLL_VIEW");
        this.canCalculate = session.coQuyen("PAYROLL_CALCULATE");
        this.canLock      = session.coQuyen("PAYROLL_LOCK");
        this.maNVHienTai = currentUser != null ? currentUser.getNhanVienId() : null;
        if ((currentScope == DataScope.DEPT || currentScope == DataScope.TEAM) && maNVHienTai != null) {
            this.maNVTrongPhamVi = com.hrm.bus.NhanVienBUS.getInstance()
                    .getAllByActionScope("PAYROLL_VIEW", maNVHienTai).stream()
                    .map(com.hrm.model.NhanVien::getMaNhanVien)
                    .collect(Collectors.toSet());
        } else {
            this.maNVTrongPhamVi = null;
        }

        setLayout(new BorderLayout());
        setBackground(UIColors.LIGHT_GRAY_BG);

        if (currentScope == DataScope.SELF) {
            add(new SalarySelfViewPanel(salaryService, maNVHienTai), BorderLayout.CENTER);
        } else {
            buildManagementView();
        }
    }

    private void buildManagementView() {
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(com.hrm.util.UIFonts.TEXT_MEDIUM);
        tabbedPane.setBackground(UIColors.WHITE);
        tabbedPane.addTab("Bảng lương", buildBangLuongTab());
        tabbedPane.addTab("Chi tiết lương", buildChiTietTab());
        add(tabbedPane, BorderLayout.CENTER);

        btnTinhLuong.setVisible(canCalculate);
        btnTinhLaiBangLuong.setVisible(canCalculate);
        btnDuyetBangLuong.setVisible(canLock);         // chỉ TRUONG_PHONG_KT / TONG_GIAM_DOC
        btnKhoaBangLuong.setVisible(canLock);          // chỉ TRUONG_PHONG_KT / TONG_GIAM_DOC
        btnTinhLaiNhanVien.setVisible(canCalculate);

        loadBangLuong();
    }


    private JPanel buildBangLuongTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(UIColors.LIGHT_GRAY_BG);
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        toolbar.setOpaque(false);

        btnTinhLuong = UIHelper.createSuccessButton("Tính lương tháng mới");
        btnTinhLaiBangLuong = UIHelper.createPrimaryButton("Tính lại kỳ lương");
        btnDuyetBangLuong = UIHelper.createWarningButton("Duyệt bảng lương");
        btnKhoaBangLuong = UIHelper.createDangerButton("Khóa bảng lương");

        btnTinhLuong.addActionListener(e -> tinhLuongThangMoi());
        btnTinhLaiBangLuong.addActionListener(e -> tinhLaiBangLuong());
        btnDuyetBangLuong.addActionListener(e -> duyetBangLuong());
        btnKhoaBangLuong.addActionListener(e -> khoaBangLuong());

        toolbar.add(btnTinhLuong);
        toolbar.add(btnTinhLaiBangLuong);
        toolbar.add(btnDuyetBangLuong);
        toolbar.add(btnKhoaBangLuong);

        String[] cols = {"Mã BL", "Tháng", "Năm", "Tên bảng lương", "Ngày tạo", "Trạng thái"};
        modelBangLuong = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        modelBangLuong.setColumnIdentifiers(new Object[]{"Mã BL", "Tháng", "Năm", "Tên bảng lương", "Ngày tạo",
                "Người tạo", "Ngày duyệt", "Người duyệt", "Ngày khóa", "Người khóa", "Trạng thái"});

        tblBangLuong = new JTable(modelBangLuong);
        tblBangLuong.setRowHeight(28);
        tblBangLuong.setFont(com.hrm.util.UIFonts.TEXT_NORMAL);
        tblBangLuong.getTableHeader().setFont(com.hrm.util.UIFonts.BOLD_NORMAL);
        tblBangLuong.getTableHeader().setBackground(UIColors.PRIMARY_PURPLE);
        tblBangLuong.getTableHeader().setForeground(UIColors.TEXT_DARK);
        tblBangLuong.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblBangLuong.setSelectionBackground(UIColors.LIGHT_PURPLE);
        tblBangLuong.setSelectionForeground(UIColors.TEXT_DARK);

        int[] widths = {60, 70, 70, 230, 145, 150, 145, 150, 145, 150, 120};
        for (int i = 0; i < widths.length; i++) {
            tblBangLuong.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
        tblBangLuong.getColumnModel().getColumn(10).setCellRenderer(new SalaryStatusRenderer());

        tblBangLuong.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = tblBangLuong.getSelectedRow();
                if (row >= 0) {
                    selectedMaBL = (int) modelBangLuong.getValueAt(row, 0);
                    loadChiTiet(selectedMaBL);
                    updateActionButtonsForSelection();
                    tabbedPane.setSelectedIndex(1);
                }
            }
        });

        JScrollPane scroll = new JScrollPane(tblBangLuong);
        scroll.setBorder(new TitledBorder("Danh sách kỳ bảng lương"));

        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildChiTietTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(UIColors.LIGHT_GRAY_BG);
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        toolbar.setOpaque(false);
        btnTinhLaiNhanVien = UIHelper.createPrimaryButton("Tính lại nhân viên");
        btnTinhLaiNhanVien.addActionListener(e -> tinhLaiNhanVien());
        toolbar.add(btnTinhLaiNhanVien);

        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(buildChiTietTablePanel("Chi tiết lương nhân viên (double-click để xem chi tiết)"),
                BorderLayout.CENTER);
        return panel;
    }

    private JScrollPane buildChiTietTablePanel(String title) {
        String[] cols = {"Mã NV", "Họ tên", "Lương cơ bản", "Lương chức vụ",
                "Tiền OT", "Tổng thu nhập", "Khấu trừ", "Thực lãnh", "Số ngày công"};
        modelChiTiet = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        modelChiTiet.setColumnIdentifiers(new Object[]{"Mã NV", "Họ tên", "Lương cơ bản", "Lương chức vụ",
                "Tiền OT", "Tổng thu nhập", "Khấu trừ", "Thực lãnh", "Số ngày công", "Số giờ OT", "Ghi chú"});

        tblChiTiet = new JTable(modelChiTiet);
        tblChiTiet.setRowHeight(28);
        tblChiTiet.setFont(com.hrm.util.UIFonts.TEXT_NORMAL);
        tblChiTiet.getTableHeader().setFont(com.hrm.util.UIFonts.BOLD_NORMAL);
        tblChiTiet.getTableHeader().setBackground(UIColors.PRIMARY_PURPLE);
        tblChiTiet.getTableHeader().setForeground(UIColors.TEXT_DARK);
        tblChiTiet.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblChiTiet.setSelectionBackground(UIColors.LIGHT_PURPLE);
        tblChiTiet.setSelectionForeground(UIColors.TEXT_DARK);

        int[] widths = {70, 150, 125, 125, 110, 125, 110, 125, 95, 95, 220};
        for (int i = 0; i < widths.length; i++) {
            tblChiTiet.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        for (int i = 2; i <= 7; i++) {
            tblChiTiet.getColumnModel().getColumn(i).setCellRenderer(rightRenderer);
        }
        tblChiTiet.getColumnModel().getColumn(9).setCellRenderer(rightRenderer);

        tblChiTiet.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateActionButtonsForSelection();
            }
        });

        tblChiTiet.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && selectedMaBL >= 0) {
                    showChiTietDialog();
                }
            }
        });

        JScrollPane scroll = new JScrollPane(tblChiTiet);
        scroll.setBorder(new TitledBorder(title));
        return scroll;
    }

    private void loadBangLuong() {
        try {
            danhSachBL = salaryService.getAll();
            loadBangLuongManagement();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Lỗi tải danh sách bảng lương: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadBangLuongManagement() {
        modelBangLuong.setRowCount(0);
        for (BangLuong bl : danhSachBL) {
            modelBangLuong.addRow(new Object[]{
                    bl.getMaBL(),
                    bl.getThang(),
                    bl.getNam(),
                    buildBangLuongName(bl),
                    formatDateTime(bl.getNgayTao()),
                    safeText(bl.getNguoiTao()),
                    formatDateTime(bl.getNgayDuyet()),
                    safeText(bl.getNguoiDuyet()),
                    formatDateTime(bl.getNgayKhoa()),
                    safeText(bl.getNguoiKhoa()),
                    formatTrangThai(bl)
            });
        }
        updateActionButtonsForSelection();
    }

    private void loadChiTiet(int maBL) {
        try {
            currentChiTietList = applyScopeFilter(salaryService.getChiTiet(maBL));
            fillChiTietTable(currentChiTietList);
            updateActionButtonsForSelection();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Lỗi tải chi tiết lương: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private List<ChiTietLuong> applyScopeFilter(List<ChiTietLuong> list) {
        if (list == null) {
            return new ArrayList<>();
        }
        if (currentScope == DataScope.ALL || currentScope == DataScope.NONE) {
            return new ArrayList<>(list);
        }
        if ((currentScope == DataScope.DEPT || currentScope == DataScope.TEAM) && maNVTrongPhamVi != null) {
            return list.stream()
                    .filter(ct -> maNVTrongPhamVi.contains(ct.getMaNV()))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>(list);
    }

    private void fillChiTietTable(List<ChiTietLuong> list) {
        modelChiTiet.setRowCount(0);
        for (ChiTietLuong ct : list) {
            modelChiTiet.addRow(new Object[]{
                    ct.getMaNV(),
                    ct.getTenNV() != null ? ct.getTenNV() : "",
                    formatMoney(ct.getLuongCoBan()),
                    formatMoney(ct.getTongLuongChucVu()),
                    formatMoney(ct.getTienOT()),
                    formatMoney(ct.getTongLuong()),
                    formatMoney(ct.getTongKhauTru()),
                    formatMoney(ct.getLuongThucNhan()),
                    ct.getSoNgayCong(),
                    formatHours(ct.getTongGioOT()),
                    safeText(ct.getGhiChu())
            });
        }
    }

    private void tinhLuongThangMoi() {
        JSpinner spinThang = new JSpinner(new SpinnerNumberModel(
                java.time.LocalDate.now().getMonthValue(), 1, 12, 1));
        JSpinner spinNam = new JSpinner(new SpinnerNumberModel(
                java.time.LocalDate.now().getYear(), 2000, 2100, 1));
        spinThang.setFont(com.hrm.util.UIFonts.TEXT_MEDIUM);
        spinNam.setFont(com.hrm.util.UIFonts.TEXT_MEDIUM);

        JPanel panel = new JPanel(new GridLayout(2, 2, 8, 8));
        panel.add(new JLabel("Tháng:"));
        panel.add(spinThang);
        panel.add(new JLabel("Năm:"));
        panel.add(spinNam);

        int result = JOptionPane.showConfirmDialog(this, panel,
                "Tính lương tháng mới", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        int thang = (int) spinThang.getValue();
        int nam = (int) spinNam.getValue();

        try {
            KetQua<BangLuong> sr = salaryService.tinhLuong(thang, nam);
            if (sr.isSuccess()) {
                JOptionPane.showMessageDialog(this,
                        "Tính lương tháng " + thang + "/" + nam + " thành công!",
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
                loadBangLuong();
            } else {
                JOptionPane.showMessageDialog(this, sr.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Lỗi tính lương: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void tinhLaiBangLuong() {
        BangLuong bangLuong = getSelectedBangLuong();
        if (bangLuong == null) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng chọn bảng lương cần tính lại.",
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (bangLuong.getTrangThai() != BangLuong.TrangThai.DA_TINH) {
            JOptionPane.showMessageDialog(this,
                    "Chỉ được tính lại bảng lương ở trạng thái Đã tính.",
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Tính lại toàn bộ kỳ lương " + buildBangLuongName(bangLuong) + "?",
                "Xác nhận tính lại", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            KetQua<Void> sr = salaryService.tinhLaiBangLuong(bangLuong.getMaBL());
            if (sr.isSuccess()) {
                JOptionPane.showMessageDialog(this, sr.getMessage(), "Thành công", JOptionPane.INFORMATION_MESSAGE);
                loadBangLuong();
                restoreBangLuongSelection(bangLuong.getMaBL());
            } else {
                JOptionPane.showMessageDialog(this, sr.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Lỗi tính lại bảng lương: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void tinhLaiNhanVien() {
        BangLuong bangLuong = getSelectedBangLuong();
        if (bangLuong == null) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng chọn bảng lương trước.",
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (bangLuong.getTrangThai() != BangLuong.TrangThai.DA_TINH) {
            JOptionPane.showMessageDialog(this,
                    "Chỉ được tính lại nhân viên khi bảng lương ở trạng thái Đã tính.",
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int row = tblChiTiet.getSelectedRow();
        if (row < 0 || row >= currentChiTietList.size()) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng chọn nhân viên cần tính lại.",
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        ChiTietLuong ct = currentChiTietList.get(row);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Tính lại lương cho nhân viên " + ct.getMaNV() + " - " + ct.getTenNV() + "?",
                "Xác nhận tính lại", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            KetQua<Void> sr = salaryService.tinhLaiChoNhanVien(bangLuong.getMaBL(), ct.getMaNV());
            if (sr.isSuccess()) {
                JOptionPane.showMessageDialog(this, sr.getMessage(), "Thành công", JOptionPane.INFORMATION_MESSAGE);
                loadChiTiet(bangLuong.getMaBL());
                restoreChiTietSelection(ct.getMaNV());
            } else {
                JOptionPane.showMessageDialog(this, sr.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Lỗi tính lại lương nhân viên: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void duyetBangLuong() {
        int row = tblBangLuong.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng chọn bảng lương cần duyệt.",
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int maBL = (int) modelBangLuong.getValueAt(row, 0);
        String tenBL = (String) modelBangLuong.getValueAt(row, 3);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Duyệt bảng lương: " + tenBL + "?",
                "Xác nhận duyệt", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            KetQua<Void> sr = salaryService.duyetBangLuong(maBL);
            if (sr.isSuccess()) {
                JOptionPane.showMessageDialog(this,
                        "Đã duyệt bảng lương thành công.",
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
                loadBangLuong();
            } else {
                JOptionPane.showMessageDialog(this, sr.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Lỗi duyệt bảng lương: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void khoaBangLuong() {
        int row = tblBangLuong.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng chọn bảng lương cần khóa.",
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int maBL = (int) modelBangLuong.getValueAt(row, 0);
        String tenBL = (String) modelBangLuong.getValueAt(row, 3);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Khóa bảng lương: " + tenBL + "?\nHành động này không thể hoàn tác.",
                "Xác nhận khóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            KetQua<Void> sr = salaryService.khoaBangLuong(maBL);
            if (sr.isSuccess()) {
                JOptionPane.showMessageDialog(this,
                        "Đã khóa bảng lương thành công.",
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
                loadBangLuong();
            } else {
                JOptionPane.showMessageDialog(this, sr.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Lỗi khóa bảng lương: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showChiTietDialog() {
        int row = tblChiTiet.getSelectedRow();
        if (row < 0 || row >= currentChiTietList.size()) {
            return;
        }

        try {
            ChiTietLuong ct = currentChiTietList.get(row);
            SalaryDetailDialog dialog = new SalaryDetailDialog(
                    (Frame) SwingUtilities.getWindowAncestor(this), ct);
            dialog.setVisible(true);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Lỗi mở chi tiết: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private BangLuong getSelectedBangLuong() {
        if (selectedMaBL < 0) {
            return null;
        }
        for (BangLuong bl : danhSachBL) {
            if (bl.getMaBL() == selectedMaBL) {
                return bl;
            }
        }
        return null;
    }

    private void updateActionButtonsForSelection() {
        BangLuong bangLuong = getSelectedBangLuong();
        boolean canRecalculateBangLuong = canCalculate
                && bangLuong != null
                && bangLuong.getTrangThai() == BangLuong.TrangThai.DA_TINH;
        if (btnTinhLaiBangLuong != null) {
            btnTinhLaiBangLuong.setEnabled(canRecalculateBangLuong);
        }
        if (btnTinhLaiNhanVien != null) {
            boolean hasSelectedRow = tblChiTiet != null && tblChiTiet.getSelectedRow() >= 0;
            btnTinhLaiNhanVien.setEnabled(canRecalculateBangLuong && hasSelectedRow);
        }
    }

    private void restoreBangLuongSelection(int maBL) {
        for (int i = 0; i < modelBangLuong.getRowCount(); i++) {
            if (((int) modelBangLuong.getValueAt(i, 0)) == maBL) {
                tblBangLuong.setRowSelectionInterval(i, i);
                return;
            }
        }
    }

    private void restoreChiTietSelection(String maNV) {
        for (int i = 0; i < currentChiTietList.size(); i++) {
            if (maNV.equals(currentChiTietList.get(i).getMaNV())) {
                tblChiTiet.setRowSelectionInterval(i, i);
                updateActionButtonsForSelection();
                return;
            }
        }
        updateActionButtonsForSelection();
    }

    private String buildBangLuongName(BangLuong bl) {
        return "Bảng lương tháng " + bl.getThang() + "/" + bl.getNam();
    }

    private String buildBangLuongLabel(BangLuong bl) {
        return buildBangLuongName(bl) + " - " + formatTrangThai(bl);
    }

    private String formatTrangThai(BangLuong bl) {
        return bl.getTrangThai() != null ? bl.getTrangThai().toString() : "";
    }

    private String formatDateTime(LocalDateTime value) {
        return value != null ? value.format(DATE_TIME_FORMAT) : "";
    }

    private String formatMoney(double amount) {
        return MONEY_FORMAT.format((long) amount) + " đ";
    }

    private String formatHours(double hours) {
        if (hours == Math.rint(hours)) {
            return String.valueOf((long) hours);
        }
        return String.format(Locale.US, "%.2f", hours);
    }

    private String safeText(String value) {
        return value != null ? value : "";
    }

}
