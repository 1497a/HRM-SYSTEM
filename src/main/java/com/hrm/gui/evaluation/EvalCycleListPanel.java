package com.hrm.gui.evaluation;

import com.hrm.gui.components.PurpleTable;
import com.hrm.bus.DanhGiaBUS;
import com.hrm.bus.KetQua;
import com.hrm.model.DotDanhGia;
import com.hrm.model.TaiKhoan;
import com.hrm.model.TieuChiDanhGia;
import com.hrm.util.DialogUtil;
import com.hrm.util.PermissionCodes;
import com.hrm.util.UIFonts;
import com.hrm.util.SessionContext;
import com.hrm.util.UIColors;
import com.hrm.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class EvalCycleListPanel extends JPanel {

    private final DanhGiaBUS evalService;
    private final TaiKhoan currentUser;
    private final boolean isAdmin;
    private final boolean isManager;
    private PurpleTable cycleTable;
    private DefaultTableModel cycleTableModel;
    private JButton btnTaoDot;
    private JButton btnOpenCycle;
    private JButton btnCloseCycle;
    private JButton btnConfigCriteria;
    private JButton btnEvaluate;
    private JButton btnViewDetail;
    private TableRowSorter<DefaultTableModel> sorter;
    private JTextField txtTimKiem;
    private JComboBox<String> cboTrangThai;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String STATUS_CHUA = "Chưa bắt đầu";
    private static final String STATUS_DANG = "Đang diễn ra";
    private static final String STATUS_DA_KET = "Đã kết thúc";
    public EvalCycleListPanel() {
        this.evalService = DanhGiaBUS.getInstance();
        this.currentUser = SessionContext.getInstance().getCurrentUser();
        SessionContext session = SessionContext.getInstance();
        this.isAdmin   = session.hasPermission(PermissionCodes.EVAL_MANAGE);
        this.isManager = isAdmin || session.hasPermission(PermissionCodes.EVAL_REVIEW);
        initComponents();
        setupLayout();
        loadData();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(Color.WHITE);
        String[] cycleColumns = {"ID", "Tên đợt đánh giá", "Kỳ", "Năm", "Bắt đầu", "Kết thúc", "Trạng thái"};
        cycleTableModel = PurpleTable.createNonEditableModel(cycleColumns);
        cycleTable = new PurpleTable(cycleTableModel);
        sorter = new TableRowSorter<>(cycleTableModel);
        cycleTable.setRowSorter(sorter);
        sorter.setComparator(0, java.util.Comparator.comparingInt(a -> (Integer) a));
        sorter.setSortKeys(java.util.List.of(new javax.swing.RowSorter.SortKey(0, javax.swing.SortOrder.ASCENDING)));
        txtTimKiem = UIHelper.createSearchField("Tìm theo tên đợt, kỳ, năm...");
        cboTrangThai = new JComboBox<>(new String[]{"Tất cả", STATUS_CHUA, STATUS_DANG, STATUS_DA_KET});
        UIHelper.attachSearchAndStatusFilter(txtTimKiem, cboTrangThai, sorter, 6, new int[]{1, 2, 3});
        cycleTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        cycleTable.getSelectionModel().addListSelectionListener(e -> onCycleSelected());
        cycleTable.getColumnModel().getColumn(0).setPreferredWidth(40);
        cycleTable.getColumnModel().getColumn(1).setPreferredWidth(260);
        cycleTable.getColumnModel().getColumn(2).setPreferredWidth(60);
        cycleTable.getColumnModel().getColumn(3).setPreferredWidth(60);
        cycleTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        cycleTable.getColumnModel().getColumn(5).setPreferredWidth(100);
        cycleTable.getColumnModel().getColumn(6).setPreferredWidth(120);
        cycleTable.getColumnModel().getColumn(6).setCellRenderer(new com.hrm.gui.components.StatusCellRenderer());
        btnTaoDot = UIHelper.createSuccessButton("+ Tạo đợt mới");
        btnOpenCycle = UIHelper.createPrimaryButton("Mở kỳ đánh giá");
        btnCloseCycle = UIHelper.createDangerButton("Đóng kỳ đánh giá");
        btnConfigCriteria = UIHelper.createPrimaryButton("Cấu hình tiêu chí");
        btnEvaluate = UIHelper.createPrimaryButton("Đánh giá nhân viên");
        btnViewDetail = UIHelper.createPrimaryButton("Xem chi tiết");
        btnOpenCycle.setEnabled(false);
        btnCloseCycle.setEnabled(false);
        btnEvaluate.setEnabled(false);
        btnTaoDot.addActionListener(e -> taoDot());
        btnOpenCycle.addActionListener(e -> openCycle());
        btnCloseCycle.addActionListener(e -> closeCycle());
        btnConfigCriteria.addActionListener(e -> configCriteria());
        btnEvaluate.addActionListener(e -> evaluateEmployee());
        btnViewDetail.addActionListener(e -> viewResults());
        cycleTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && cycleTable.getSelectedRow() >= 0) {
                    viewResults();
                }
            }
        });
    }

    private void setupLayout() {
        // NORTH: hint + search
        JLabel lblHint = new JLabel("Tìm theo: Tên đợt đánh giá / Kỳ / Năm");
        lblHint.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblHint.setForeground(UIColors.TEXT_DARK);
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        searchPanel.setOpaque(false);
        searchPanel.add(new JLabel("Tìm kiếm:"));
        searchPanel.add(txtTimKiem);
        searchPanel.add(new JLabel("Trạng thái:"));
        searchPanel.add(cboTrangThai);
        JPanel northPanel = new JPanel(new BorderLayout(0, 2));
        northPanel.setOpaque(false);
        northPanel.add(searchPanel, BorderLayout.NORTH);
        northPanel.add(lblHint, BorderLayout.SOUTH);
        // CENTER: table
        JScrollPane cycleScroll = new JScrollPane(cycleTable);
        cycleScroll.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        // SOUTH: action buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        btnPanel.setOpaque(false);
        if (isAdmin) {
            btnPanel.add(btnTaoDot);
            btnPanel.add(btnOpenCycle);
            btnPanel.add(btnCloseCycle);
            btnPanel.add(btnConfigCriteria);
        }
        if (isManager) btnPanel.add(btnEvaluate);
        btnPanel.add(btnViewDetail);
        JButton btnLamMoi = UIHelper.createDefaultButton("Làm mới");
        btnLamMoi.addActionListener(e -> loadData());
        btnPanel.add(btnLamMoi);
        add(northPanel, BorderLayout.NORTH);
        add(cycleScroll, BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);
    }

    private void loadData() {
        cycleTableModel.setRowCount(0);
        try {
            List<DotDanhGia> cycles = evalService.getAllCycles();
            cycles.sort(java.util.Comparator.comparingInt(DotDanhGia::getId));
            for (DotDanhGia cycle : cycles) {
                cycleTableModel.addRow(new Object[]{
                        cycle.getId(),
                        cycle.getTenDot(),
                        kyDisplay(cycle.getKyDanhGia()),
                        cycle.getNam(),
                        cycle.getTuNgay() != null ? cycle.getTuNgay().format(DATE_FORMAT) : "",
                        cycle.getDenNgay() != null ? cycle.getDenNgay().format(DATE_FORMAT) : "",
                        statusDisplay(cycle.getTrangThai())
                });
            }
        } catch (Exception ex) {
            DialogUtil.showError(this, "Lỗi tải dữ liệu: " + ex.getMessage());
        }
    }

    private void onCycleSelected() {
        int row = cycleTable.getSelectedRow();
        if (row < 0) return;
        String status = (String) cycleTableModel.getValueAt(cycleTable.convertRowIndexToModel(row), 6);
        btnOpenCycle.setEnabled(isAdmin && STATUS_CHUA.equals(status));
        btnCloseCycle.setEnabled(isAdmin && STATUS_DANG.equals(status));
        btnEvaluate.setEnabled(isManager && STATUS_DANG.equals(status));
    }

    private void taoDot() {
        JTextField txtTen = new JTextField(25);
        txtTen.setFont(com.hrm.util.UIFonts.TEXT_NORMAL);
        String[] kyOptions = {"Quý 1", "Quý 2", "Quý 3", "Quý 4", "Cả năm"};
        String[] kyValues = {"quy_1", "quy_2", "quy_3", "quy_4", "ca_nam"};
        JComboBox<String> cboKy = new JComboBox<>(kyOptions);
        cboKy.setFont(com.hrm.util.UIFonts.TEXT_NORMAL);
        JSpinner spinNam = new JSpinner(new SpinnerNumberModel(LocalDate.now().getYear(), 2020, 2099, 1));
        spinNam.setFont(com.hrm.util.UIFonts.TEXT_NORMAL);
        SpinnerDateModel mdBD = new SpinnerDateModel(java.util.Date.from(
                LocalDate.now().atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()),
                null, null, java.util.Calendar.DAY_OF_MONTH);
        JSpinner spinBD = new JSpinner(mdBD);
        spinBD.setEditor(new JSpinner.DateEditor(spinBD, "dd/MM/yyyy"));
        SpinnerDateModel mdKT = new SpinnerDateModel(java.util.Date.from(
                LocalDate.now().plusMonths(3).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()),
                null, null, java.util.Calendar.DAY_OF_MONTH);
        JSpinner spinKT = new JSpinner(mdKT);
        spinKT.setEditor(new JSpinner.DateEditor(spinKT, "dd/MM/yyyy"));
        JPanel formInfo = new JPanel(new GridLayout(5, 2, 8, 8));
        formInfo.add(new JLabel("Tên đợt đánh giá (*):")); formInfo.add(txtTen);
        formInfo.add(new JLabel("Kỳ đánh giá:"));          formInfo.add(cboKy);
        formInfo.add(new JLabel("Năm:"));                  formInfo.add(spinNam);
        formInfo.add(new JLabel("Ngày bắt đầu:"));         formInfo.add(spinBD);
        formInfo.add(new JLabel("Ngày kết thúc:"));        formInfo.add(spinKT);
        List<TieuChiDanhGia> allCriteria = evalService.getAllCriteria();
        JPanel criteriaPanel = new JPanel(new GridLayout(0, 1, 4, 4));
        criteriaPanel.setBorder(BorderFactory.createTitledBorder("Chọn các tiêu chí"));
        List<JCheckBox> checkBoxes = new java.util.ArrayList<>();
        JLabel lblTotal = new JLabel("Tổng trọng số: 0%");
        lblTotal.setFont(com.hrm.util.UIFonts.BOLD_NORMAL);
        lblTotal.setForeground(UIColors.DANGER_RED);
        Runnable calcTotal = () -> {
            int total = 0;
            for (JCheckBox cb : checkBoxes) {
                if (cb.isSelected()) {
                    total += (int) ((TieuChiDanhGia) cb.getClientProperty("criteria")).getTrongSo();
                }
            }
            lblTotal.setText("Tổng trọng số (bắt buộc 100%): " + total + "%");
            lblTotal.setForeground(total == 100 ? UIColors.SUCCESS_GREEN : UIColors.DANGER_RED);
        };
        for (TieuChiDanhGia c : allCriteria) {
            JCheckBox cb = new JCheckBox(c.getTenTieuChi() + " (" + (int) c.getTrongSo() + "%)");
            cb.putClientProperty("criteria", c);
            cb.addActionListener(e -> calcTotal.run());
            checkBoxes.add(cb);
            criteriaPanel.add(cb);
        }
        int initialTotal = checkBoxes.stream()
                .mapToInt(cb -> (int) ((TieuChiDanhGia) cb.getClientProperty("criteria")).getTrongSo())
                .sum();
        if (initialTotal == 100) {
            for (JCheckBox cb : checkBoxes) cb.setSelected(true);
        }
        calcTotal.run();
        JPanel scrollCriteriaPanel = new JPanel(new BorderLayout());
        JScrollPane scrollPane = new JScrollPane(criteriaPanel);
        scrollPane.setPreferredSize(new Dimension(300, 150));
        scrollCriteriaPanel.add(scrollPane, BorderLayout.CENTER);
        scrollCriteriaPanel.add(lblTotal, BorderLayout.SOUTH);
        JPanel mainForm = new JPanel(new BorderLayout(0, 10));
        mainForm.add(formInfo, BorderLayout.NORTH);
        mainForm.add(scrollCriteriaPanel, BorderLayout.CENTER);
        while (true) {
            int res = JOptionPane.showConfirmDialog(this, mainForm,
                    "Tạo đợt đánh giá mới", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (res != JOptionPane.OK_OPTION) return;
            String ten = txtTen.getText().trim();
            if (ten.isEmpty()) {
                DialogUtil.showWarn(this, "Vui lòng nhập tên đợt đánh giá.");
                SwingUtilities.invokeLater(() -> txtTen.requestFocusInWindow());
                continue;
            }
            List<TieuChiDanhGia> selectedCriteria = new java.util.ArrayList<>();
            int totalWeight = 0;
            for (JCheckBox cb : checkBoxes) {
                if (cb.isSelected()) {
                    TieuChiDanhGia c = (TieuChiDanhGia) cb.getClientProperty("criteria");
                    selectedCriteria.add(c);
                    totalWeight += (int) c.getTrongSo();
                }
            }
            if (totalWeight != 100) {
                DialogUtil.showWarn(this, "Tổng trọng số các tiêu chí được chọn phải chính xác 100%.");
                continue;
            }
            LocalDate tuNgay = toLocalDate((java.util.Date) spinBD.getValue());
            LocalDate denNgay = toLocalDate((java.util.Date) spinKT.getValue());
            int kyIdx = cboKy.getSelectedIndex();
            KetQua<?> kq = evalService.taoDotDanhGia(
                    ten, (int) spinNam.getValue(), kyValues[kyIdx], tuNgay, denNgay, selectedCriteria);
            showResult(kq);
            if (kq.isSuccess()) {
                loadData();
                break;
            }
        }
    }

    private void openCycle() {
        int row = cycleTable.getSelectedRow();
        if (row < 0) return;
        int cycleId = (int) cycleTableModel.getValueAt(cycleTable.convertRowIndexToModel(row), 0);
        KetQua<?> result = evalService.openCycle(cycleId);
        showResult(result);
        if (result.isSuccess()) loadData();
    }

    private void closeCycle() {
        int row = cycleTable.getSelectedRow();
        if (row < 0) return;
        if (!DialogUtil.showYesNo(this,
                "Bạn có chắc muốn đóng kỳ đánh giá này?\nSau khi đóng sẽ không thể sửa đổi.",
                "Xác nhận")) return;
        int cycleId = (int) cycleTableModel.getValueAt(cycleTable.convertRowIndexToModel(row), 0);
        KetQua<?> result = evalService.closeCycle(cycleId);
        showResult(result);
        if (result.isSuccess()) loadData();
    }

    private void configCriteria() {
        EvalConfigDialog dialog = new EvalConfigDialog((Frame) SwingUtilities.getWindowAncestor(this));
        dialog.setVisible(true);
    }

    private void evaluateEmployee() {
        if (currentUser.getMaNV() == null || currentUser.getMaNV().isEmpty()) {
            DialogUtil.showWarn(this, "Tài khoản quản trị viên không đánh giá nhân viên");
            return;
        }
        int row = cycleTable.getSelectedRow();
        if (row < 0) {
            DialogUtil.showWarn(this, "Vui lòng chọn đợt đánh giá.");
            return;
        }
        int cycleId = (int) cycleTableModel.getValueAt(cycleTable.convertRowIndexToModel(row), 0);
        String cycleName = (String) cycleTableModel.getValueAt(cycleTable.convertRowIndexToModel(row), 1);
        EvalDoDialog dialog = new EvalDoDialog((Frame) SwingUtilities.getWindowAncestor(this), cycleId, cycleName);
        dialog.setVisible(true);
        if (dialog.isSuccessful()) {
            loadData();
            onCycleSelected();
        }
    }

    private void viewResults() {
        int selectedRow = cycleTable.getSelectedRow();
        int selectedCycleId = -1;
        if (selectedRow >= 0) {
            selectedCycleId = (int) cycleTableModel.getValueAt(selectedRow, 0);
        }
        EvalResultPanel resultPanel = new EvalResultPanel(selectedCycleId);
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                selectedRow >= 0 ? "Kết quả đánh giá - kỳ đã chọn" : "Kết quả đánh giá",
                true);
        dialog.setContentPane(resultPanel);
        dialog.setSize(1000, 650);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private String statusDisplay(DotDanhGia.TrangThai t) {
        if (t == null) return STATUS_CHUA;
        switch (t) {
            case DANG_DIEN_RA: return STATUS_DANG;
            case DA_KET_THUC: return STATUS_DA_KET;
            default: return STATUS_CHUA;
        }
    }

    private String kyDisplay(String ky) {
        if (ky == null) return "";
        switch (ky) {
            case "quy_1": return "Q1";
            case "quy_2": return "Q2";
            case "quy_3": return "Q3";
            case "quy_4": return "Q4";
            case "ca_nam": return "Cả năm";
            default: return ky;
        }
    }

    private LocalDate toLocalDate(java.util.Date d) {
        return d.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
    }

    private void showResult(KetQua<?> result) {
        if (result.isSuccess()) {
            DialogUtil.showSuccess(this, result.getMessage());
        } else {
            DialogUtil.showError(this, result.getMessage());
        }
    }

}
