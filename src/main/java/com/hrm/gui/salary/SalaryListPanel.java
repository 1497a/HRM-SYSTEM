package com.hrm.gui.salary;

import com.hrm.bus.KetQua;
import com.hrm.bus.LuongBUS;
import com.hrm.bus.XacThucBUS;
import com.hrm.model.BangLuong;
import com.hrm.model.ChiTietLuong;
import com.hrm.model.DataScope;
import com.hrm.model.TaiKhoan;
import com.hrm.gui.components.PurpleTable;
import com.hrm.util.DialogUtil;
import com.hrm.util.PermissionCodes;
import com.hrm.util.UIFonts;
import com.hrm.util.SessionContext;
import com.hrm.util.UIColors;
import com.hrm.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import java.awt.*;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SalaryListPanel extends JPanel {

    private final LuongBUS salaryService;
    private final DataScope currentScope;
    private final boolean canCalculate;
    private final boolean canLock;
    private final String maNVHienTai;
    private PurpleTable tblBangLuong;
    private DefaultTableModel modelBangLuong;
    private JButton btnTinhLuong;
    private JButton btnTinhLaiBangLuong;
    private JButton btnDuyetBangLuong;
    private JButton btnKhoaBangLuong;
    private PurpleTable tblChiTiet;
    private DefaultTableModel modelChiTiet;
    private JButton btnTinhLaiNhanVien;
    private JTabbedPane tabbedPane;
    private JLabel lblChiTietTitle;
    private JButton btnXemChiTiet;
    private List<BangLuong> danhSachBL = new ArrayList<>();
    private List<ChiTietLuong> currentChiTietList = new ArrayList<>();
    private int selectedMaBL = -1;
    private JTextField txtTimKiemBL;
    private JTextField txtTimKiemCD;
    private javax.swing.table.TableRowSorter<DefaultTableModel> sorterBL;
    private javax.swing.table.TableRowSorter<DefaultTableModel> sorterCD;
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final NumberFormat MONEY_FORMAT = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
    public SalaryListPanel() {
        this.salaryService = LuongBUS.getInstance();
        SessionContext session = SessionContext.getInstance();
        TaiKhoan currentUser = session.getCurrentUser();
        this.currentScope = XacThucBUS.getInstance().getScopeForAction(PermissionCodes.PAYROLL_VIEW);
        this.canCalculate = session.hasPermission(PermissionCodes.PAYROLL_CALCULATE);
        this.canLock      = session.hasPermission(PermissionCodes.PAYROLL_LOCK);
        this.maNVHienTai = currentUser != null ? currentUser.getMaNV() : null;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        if (currentScope == DataScope.SELF) {
            add(new SalarySelfViewPanel(salaryService, maNVHienTai), BorderLayout.CENTER);
        } else {
            buildManagementView();
        }
    }

    private void buildManagementView() {
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(UIFonts.TEXT_NORMAL);
        tabbedPane.setBackground(Color.WHITE);
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
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));

        // Hint label
        JLabel lblHint = new JLabel("Tìm theo: Kỳ lương / Tên bảng lương  —  Double-click để xem chi tiết bảng lương.");
        lblHint.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblHint.setForeground(UIColors.TEXT_DARK);

        // Search toolbar (NORTH)
        btnTinhLuong = UIHelper.createSuccessButton("Tính lương tháng mới");
        btnTinhLaiBangLuong = UIHelper.createPrimaryButton("Tính lại kỳ lương");
        btnDuyetBangLuong = UIHelper.createWarningButton("Duyệt bảng lương");
        btnKhoaBangLuong = UIHelper.createDangerButton("Khóa bảng lương");
        btnXemChiTiet = UIHelper.createPrimaryButton("Xem chi tiết nhân viên");
        btnXemChiTiet.setEnabled(false);
        JButton btnLamMoi1 = UIHelper.createDefaultButton("Làm mới");
        btnTinhLuong.addActionListener(e -> tinhLuongThangMoi());
        btnTinhLaiBangLuong.addActionListener(e -> tinhLaiBangLuong());
        btnDuyetBangLuong.addActionListener(e -> duyetBangLuong());
        btnKhoaBangLuong.addActionListener(e -> khoaBangLuong());
        btnXemChiTiet.addActionListener(e -> tabbedPane.setSelectedIndex(1));
        btnLamMoi1.addActionListener(e -> loadBangLuong());
        txtTimKiemBL = UIHelper.createSearchField("Tìm theo kỳ lương, tên bảng lương...");
        JPanel searchToolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        searchToolbar.setOpaque(false);
        searchToolbar.add(new JLabel("Tìm kiếm:"));
        searchToolbar.add(txtTimKiemBL);

        // North wrapper: hint + search
        JPanel northWrapper = new JPanel(new BorderLayout(0, 4));
        northWrapper.setOpaque(false);
        northWrapper.add(searchToolbar, BorderLayout.NORTH);
        northWrapper.add(lblHint, BorderLayout.SOUTH);

        // Action buttons (will be placed SOUTH)
        JPanel actionToolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        actionToolbar.setOpaque(false);
        actionToolbar.add(btnTinhLuong);
        actionToolbar.add(btnTinhLaiBangLuong);
        actionToolbar.add(btnDuyetBangLuong);
        actionToolbar.add(btnKhoaBangLuong);
        actionToolbar.add(btnXemChiTiet);
        actionToolbar.add(btnLamMoi1);

        // Table — 6 cột
        modelBangLuong = new DefaultTableModel(
                new Object[]{"Mã BL", "Kỳ lương", "Tên bảng lương", "Ngày tạo", "Người tạo", "Trạng thái"}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        tblBangLuong = new PurpleTable(modelBangLuong);
        tblBangLuong.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        int[] widths = {60, 90, 280, 145, 160, 120};
        for (int i = 0; i < widths.length; i++) {
            tblBangLuong.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
        tblBangLuong.getColumnModel().getColumn(5).setCellRenderer(new com.hrm.gui.components.StatusCellRenderer());
        sorterBL = new javax.swing.table.TableRowSorter<>(modelBangLuong);
        tblBangLuong.setRowSorter(sorterBL);
        UIHelper.attachTextSearch(txtTimKiemBL, sorterBL, 1, 2);
        tblBangLuong.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int viewRow = tblBangLuong.getSelectedRow();
                int row = viewRow >= 0 ? tblBangLuong.convertRowIndexToModel(viewRow) : -1;
                if (row >= 0) {
                    selectedMaBL = (int) modelBangLuong.getValueAt(row, 0);
                    loadChiTiet(selectedMaBL);
                    updateActionButtonsForSelection();
                } else {
                    selectedMaBL = -1;
                }
            }
        });
        tblBangLuong.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && tblBangLuong.getSelectedRow() >= 0) {
                    showBangLuongDetailDialog();
                }
            }
        });

        JScrollPane scroll = new JScrollPane(tblBangLuong);
        scroll.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        JPanel southWrapper = new JPanel(new BorderLayout());
        southWrapper.setOpaque(false);
        southWrapper.add(actionToolbar, BorderLayout.NORTH);

        panel.add(northWrapper, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        panel.add(southWrapper, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildChiTietTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));

        // Hint label
        JLabel lblHint = new JLabel("Tìm theo: Mã NV / Họ tên  —  Double-click để xem phiếu lương chi tiết.");
        lblHint.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblHint.setForeground(UIColors.TEXT_DARK);

        lblChiTietTitle = new JLabel("Chọn một bảng lương ở tab trước.");
        lblChiTietTitle.setFont(UIFonts.BOLD_NORMAL);
        lblChiTietTitle.setForeground(UIColors.PRIMARY_PURPLE);
        btnTinhLaiNhanVien = UIHelper.createPrimaryButton("Tính lại nhân viên");
        JButton btnLamMoi2 = UIHelper.createDefaultButton("Làm mới");
        btnTinhLaiNhanVien.addActionListener(e -> tinhLaiNhanVien());
        btnLamMoi2.addActionListener(e -> { if (selectedMaBL >= 0) loadChiTiet(selectedMaBL); });
        txtTimKiemCD = UIHelper.createSearchField("Tìm theo mã NV, họ tên...");

        // Search toolbar (NORTH)
        JPanel searchToolbarCD = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        searchToolbarCD.setOpaque(false);
        searchToolbarCD.add(new JLabel("Tìm kiếm:"));
        searchToolbarCD.add(txtTimKiemCD);
        searchToolbarCD.add(Box.createHorizontalStrut(8));
        searchToolbarCD.add(lblChiTietTitle);

        JPanel northWrapper = new JPanel(new BorderLayout(0, 4));
        northWrapper.setOpaque(false);
        northWrapper.add(searchToolbarCD, BorderLayout.NORTH);
        northWrapper.add(lblHint, BorderLayout.SOUTH);

        // Action buttons (SOUTH)
        JPanel actionToolbarCD = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        actionToolbarCD.setOpaque(false);
        actionToolbarCD.add(btnTinhLaiNhanVien);
        actionToolbarCD.add(btnLamMoi2);

        panel.add(northWrapper, BorderLayout.NORTH);
        panel.add(buildChiTietTablePanel(), BorderLayout.CENTER);
        panel.add(actionToolbarCD, BorderLayout.SOUTH);
        return panel;
    }

    private JScrollPane buildChiTietTablePanel() {
        String[] cols = {"Mã NV", "Họ tên", "Lương cơ bản", "Lương chức vụ",
                "Tiền OT", "Tổng thu nhập", "Khấu trừ", "Thực lãnh", "Số ngày công"};
        modelChiTiet = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        modelChiTiet.setColumnIdentifiers(new Object[]{"Mã NV", "Họ tên", "Lương cơ bản", "Lương chức vụ",
                "Tiền OT", "Tổng thu nhập", "Khấu trừ", "Thực lãnh", "Số ngày công", "Số giờ OT"});
        tblChiTiet = new PurpleTable(modelChiTiet);
        tblChiTiet.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        int[] widths = {70, 150, 125, 125, 110, 125, 110, 125, 95, 95};
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
        sorterCD = new javax.swing.table.TableRowSorter<>(modelChiTiet);
        tblChiTiet.setRowSorter(sorterCD);
        UIHelper.attachTextSearch(txtTimKiemCD, sorterCD, 0, 1);
        JScrollPane scroll = new JScrollPane(tblChiTiet);
        scroll.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        return scroll;
    }

    private void loadBangLuong() {
        try {
            int currentSelection = selectedMaBL;
            danhSachBL = salaryService.getAllByScope(maNVHienTai);
            loadBangLuongManagement();
            if (currentSelection >= 0) {
                restoreBangLuongSelection(currentSelection);
            }
        } catch (Exception ex) {
            DialogUtil.showError(this, "Lỗi tải danh sách bảng lương: " + ex.getMessage());
        }
    }

    private void loadBangLuongManagement() {
        modelBangLuong.setRowCount(0);
        for (BangLuong bl : danhSachBL) {
            modelBangLuong.addRow(new Object[]{
                    bl.getMaBL(),
                    bl.getThang() + "/" + bl.getNam(),
                    buildBangLuongName(bl),
                    formatDateTime(bl.getNgayTao()),
                    safeText(bl.getNguoiTao()),
                    formatTrangThai(bl)
            });
        }
        updateActionButtonsForSelection();
    }

    private void loadChiTiet(int maBL) {
        try {
            currentChiTietList = salaryService.getChiTietByScope(maBL, maNVHienTai);
            fillChiTietTable(currentChiTietList);
            updateActionButtonsForSelection();
            BangLuong bl = getSelectedBangLuong();
            if (bl != null && lblChiTietTitle != null) {
                lblChiTietTitle.setText("Chi tiết lương: Bảng lương tháng " + bl.getThang() + "/" + bl.getNam());
            }
        } catch (Exception ex) {
            DialogUtil.showError(this, "Lỗi tải chi tiết lương: " + ex.getMessage());
        }
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
                    String.format("%.1f", ct.getSoNgayCong()),
                    formatHours(ct.getTongGioOT())
            });
        }
    }

    private void tinhLuongThangMoi() {
        JSpinner spinThang = new JSpinner(new SpinnerNumberModel(
                java.time.LocalDate.now().getMonthValue(), 1, 12, 1));
        JSpinner spinNam = new JSpinner(new SpinnerNumberModel(
                java.time.LocalDate.now().getYear(), 2000, 2100, 1));
        spinThang.setFont(UIFonts.TEXT_NORMAL);
        spinNam.setFont(UIFonts.TEXT_NORMAL);
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
                DialogUtil.showSuccess(this, "Tính lương tháng " + thang + "/" + nam + " thành công!");
                loadBangLuong();
                if (sr.getData() != null) {
                    restoreBangLuongSelection(sr.getData().getMaBL());
                }
            } else {
                DialogUtil.showError(this, sr.getMessage());
            }
        } catch (Exception ex) {
            DialogUtil.showError(this, "Lỗi tính lương: " + ex.getMessage());
        }
    }

    private void tinhLaiBangLuong() {
        BangLuong bangLuong = getSelectedBangLuong();
        if (bangLuong == null) {
            DialogUtil.showWarn(this, "Vui lòng chọn bảng lương cần tính lại.");
            return;
        }
        if (bangLuong.getTrangThai() != BangLuong.TrangThai.DA_TINH) {
            DialogUtil.showWarn(this, "Chỉ được tính lại bảng lương ở trạng thái Đã tính.");
            return;
        }
        if (!DialogUtil.showYesNoWarning(this,
                "Tính lại toàn bộ kỳ lương " + buildBangLuongName(bangLuong) + "?",
                "Xác nhận tính lại")) {
            return;
        }
        try {
            KetQua<Void> sr = salaryService.tinhLaiBangLuong(bangLuong.getMaBL());
            if (sr.isSuccess()) {
                DialogUtil.showSuccess(this, sr.getMessage());
                loadBangLuong();
                restoreBangLuongSelection(bangLuong.getMaBL());
            } else {
                DialogUtil.showError(this, sr.getMessage());
            }
        } catch (Exception ex) {
            DialogUtil.showError(this, "Lỗi tính lại bảng lương: " + ex.getMessage());
        }
    }

    private void tinhLaiNhanVien() {
        BangLuong bangLuong = getSelectedBangLuong();
        if (bangLuong == null) {
            DialogUtil.showWarn(this, "Vui lòng chọn bảng lương trước.");
            return;
        }
        if (bangLuong.getTrangThai() != BangLuong.TrangThai.DA_TINH) {
            DialogUtil.showWarn(this, "Chỉ được tính lại nhân viên khi bảng lương ở trạng thái Đã tính.");
            return;
        }
        int viewRow = tblChiTiet.getSelectedRow();
        if (viewRow < 0) {
            DialogUtil.showWarn(this, "Vui lòng chọn nhân viên cần tính lại.");
            return;
        }
        int row = tblChiTiet.convertRowIndexToModel(viewRow);
        if (row < 0 || row >= currentChiTietList.size()) {
            DialogUtil.showWarn(this, "Vui lòng chọn nhân viên cần tính lại.");
            return;
        }
        ChiTietLuong ct = currentChiTietList.get(row);
        if (!DialogUtil.showYesNoWarning(this,
                "Tính lại lương cho nhân viên " + ct.getMaNV() + " - " + ct.getTenNV() + "?",
                "Xác nhận tính lại")) {
            return;
        }
        try {
            KetQua<Void> sr = salaryService.tinhLaiChoNhanVien(bangLuong.getMaBL(), ct.getMaNV());
            if (sr.isSuccess()) {
                DialogUtil.showSuccess(this, sr.getMessage());
                loadBangLuong();
                restoreBangLuongSelection(bangLuong.getMaBL());
                restoreChiTietSelection(ct.getMaNV());
            } else {
                DialogUtil.showError(this, sr.getMessage());
            }
        } catch (Exception ex) {
            DialogUtil.showError(this, "Lỗi tính lại lương nhân viên: " + ex.getMessage());
        }
    }

    private void duyetBangLuong() {
        int viewRow = tblBangLuong.getSelectedRow();
        if (viewRow < 0) {
            DialogUtil.showWarn(this, "Vui lòng chọn bảng lương cần duyệt.");
            return;
        }
        int row = tblBangLuong.convertRowIndexToModel(viewRow);
        int maBL = (int) modelBangLuong.getValueAt(row, 0);
        String tenBL = (String) modelBangLuong.getValueAt(row, 2);
        if (!DialogUtil.showYesNo(this, "Duyệt bảng lương: " + tenBL + "?", "Xác nhận duyệt")) {
            return;
        }
        try {
            KetQua<Void> sr = salaryService.duyetBangLuong(maBL);
            if (sr.isSuccess()) {
                DialogUtil.showSuccess(this, "Đã duyệt bảng lương thành công.");
                loadBangLuong();
                restoreBangLuongSelection(maBL);
            } else {
                DialogUtil.showError(this, sr.getMessage());
            }
        } catch (Exception ex) {
            DialogUtil.showError(this, "Lỗi duyệt bảng lương: " + ex.getMessage());
        }
    }

    private void khoaBangLuong() {
        int viewRow = tblBangLuong.getSelectedRow();
        if (viewRow < 0) {
            DialogUtil.showWarn(this, "Vui lòng chọn bảng lương cần khóa.");
            return;
        }
        int row = tblBangLuong.convertRowIndexToModel(viewRow);
        int maBL = (int) modelBangLuong.getValueAt(row, 0);
        String tenBL = (String) modelBangLuong.getValueAt(row, 2);
        if (!DialogUtil.showYesNoWarning(this,
                "Khóa bảng lương: " + tenBL + "?\nHành động này không thể hoàn tác.",
                "Xác nhận khóa")) {
            return;
        }
        try {
            KetQua<Void> sr = salaryService.khoaBangLuong(maBL);
            if (sr.isSuccess()) {
                DialogUtil.showSuccess(this, "Đã khóa bảng lương thành công.");
                loadBangLuong();
                restoreBangLuongSelection(maBL);
            } else {
                DialogUtil.showError(this, sr.getMessage());
            }
        } catch (Exception ex) {
            DialogUtil.showError(this, "Lỗi khóa bảng lương: " + ex.getMessage());
        }
    }

    private void showBangLuongDetailDialog() {
        BangLuong bl = getSelectedBangLuong();
        if (bl == null) return;
        BangLuongDetailDialog dialog = new BangLuongDetailDialog(
                (Frame) SwingUtilities.getWindowAncestor(this), bl);
        dialog.setVisible(true);
    }

    private void showChiTietDialog() {
        int viewRow = tblChiTiet.getSelectedRow();
        if (viewRow < 0) return;
        int row = tblChiTiet.convertRowIndexToModel(viewRow);
        if (row < 0 || row >= currentChiTietList.size()) {
            return;
        }
        try {
            ChiTietLuong ct = currentChiTietList.get(row);
            SalaryDetailDialog dialog = new SalaryDetailDialog(
                    (Frame) SwingUtilities.getWindowAncestor(this), ct);
            dialog.setVisible(true);
        } catch (Exception ex) {
            DialogUtil.showError(this, "Lỗi mở chi tiết: " + ex.getMessage());
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
        if (btnXemChiTiet != null) {
            btnXemChiTiet.setEnabled(bangLuong != null);
        }
        if (btnTinhLaiNhanVien != null) {
            boolean hasSelectedRow = tblChiTiet != null && tblChiTiet.getSelectedRow() >= 0;
            btnTinhLaiNhanVien.setEnabled(canRecalculateBangLuong && hasSelectedRow);
        }
    }

    private void restoreBangLuongSelection(int maBL) {
        for (int i = 0; i < modelBangLuong.getRowCount(); i++) {
            if (((int) modelBangLuong.getValueAt(i, 0)) == maBL) {
                int viewRow = tblBangLuong.convertRowIndexToView(i);
                if (viewRow >= 0) tblBangLuong.setRowSelectionInterval(viewRow, viewRow);
                return;
            }
        }
    }

    private void restoreChiTietSelection(String maNV) {
        for (int i = 0; i < currentChiTietList.size(); i++) {
            if (maNV.equals(currentChiTietList.get(i).getMaNV())) {
                int viewRow = tblChiTiet.convertRowIndexToView(i);
                if (viewRow >= 0) tblChiTiet.setRowSelectionInterval(viewRow, viewRow);
                updateActionButtonsForSelection();
                return;
            }
        }
        updateActionButtonsForSelection();
    }

    private String buildBangLuongName(BangLuong bl) {
        return "Bảng lương tháng " + bl.getThang() + "/" + bl.getNam();
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
