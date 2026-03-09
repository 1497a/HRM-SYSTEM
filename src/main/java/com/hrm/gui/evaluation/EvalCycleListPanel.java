package com.hrm.gui.evaluation;

import com.hrm.bus.DanhGiaBUS;
import com.hrm.model.DotDanhGia;
import com.hrm.model.TaiKhoan;
import com.hrm.model.TieuChiDanhGia;
import com.hrm.util.SessionContext;
import com.hrm.util.UIColors;
import com.hrm.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class EvalCycleListPanel extends JPanel {

    private final DanhGiaBUS evalService;
    private final TaiKhoan currentUser;
    private final boolean isAdmin;
    private final boolean isManager;

    private JTable cycleTable;
    private DefaultTableModel cycleTableModel;
    private JButton btnTaoDot;
    private JButton btnOpenCycle;
    private JButton btnCloseCycle;
    private JButton btnConfigCriteria;
    private JButton btnEvaluate;
    private JButton btnViewDetail;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String STATUS_CHUA = "Chưa bắt đầu";
    private static final String STATUS_DANG = "Đang diễn ra";
    private static final String STATUS_DA_KET = "Đã kết thúc";

    public EvalCycleListPanel() {
        this.evalService = DanhGiaBUS.getInstance();
        this.currentUser = SessionContext.getInstance().getCurrentUser();
        this.isAdmin   = currentUser.coQuyen("EVAL_MANAGE");
        this.isManager = isAdmin || currentUser.coQuyen("EVAL_REVIEW");

        initComponents();
        setupLayout();
        loadData();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(UIColors.LIGHT_GRAY_BG);

        String[] cycleColumns = {"ID", "Tên đợt đánh giá", "Kỳ", "Năm", "Bắt đầu", "Kết thúc", "Trạng thái"};
        cycleTableModel = new DefaultTableModel(cycleColumns, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };

        cycleTable = new JTable(cycleTableModel);
        cycleTable.setRowHeight(28);
        cycleTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cycleTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        cycleTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        cycleTable.getSelectionModel().addListSelectionListener(e -> onCycleSelected());

        cycleTable.getColumnModel().getColumn(0).setPreferredWidth(40);
        cycleTable.getColumnModel().getColumn(1).setPreferredWidth(260);
        cycleTable.getColumnModel().getColumn(2).setPreferredWidth(60);
        cycleTable.getColumnModel().getColumn(3).setPreferredWidth(60);
        cycleTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        cycleTable.getColumnModel().getColumn(5).setPreferredWidth(100);
        cycleTable.getColumnModel().getColumn(6).setPreferredWidth(120);

        cycleTable.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                                                           boolean sel, boolean focus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, value, sel, focus, row, col);
                if (!sel && value != null) {
                    String s = value.toString();
                    if (STATUS_DANG.equals(s)) c.setBackground(new Color(200, 255, 200));
                    else if (STATUS_DA_KET.equals(s)) c.setBackground(new Color(220, 220, 220));
                    else c.setBackground(new Color(255, 255, 200));
                }
                return c;
            }
        });

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
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        topPanel.setOpaque(false);

        if (isAdmin) {
            topPanel.add(btnTaoDot);
            topPanel.add(btnOpenCycle);
            topPanel.add(btnCloseCycle);
            topPanel.add(btnConfigCriteria);
        }
        if (isManager) topPanel.add(btnEvaluate);
        topPanel.add(btnViewDetail);

        JScrollPane cycleScroll = new JScrollPane(cycleTable);
        cycleScroll.setBorder(new TitledBorder("Các đợt đánh giá (nhấp đúp để xem kết quả)"));

        add(topPanel, BorderLayout.NORTH);
        add(cycleScroll, BorderLayout.CENTER);
    }

    private void loadData() {
        cycleTableModel.setRowCount(0);
        try {
            List<DotDanhGia> cycles = evalService.getAllCycles();
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
            JOptionPane.showMessageDialog(this,
                    "Lỗi tải dữ liệu: " + ex.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onCycleSelected() {
        int row = cycleTable.getSelectedRow();
        if (row < 0) return;
        String status = (String) cycleTableModel.getValueAt(row, 6);
        btnOpenCycle.setEnabled(isAdmin && STATUS_CHUA.equals(status));
        btnCloseCycle.setEnabled(isAdmin && STATUS_DANG.equals(status));
        btnEvaluate.setEnabled(isManager && STATUS_DANG.equals(status));
    }

    private void taoDot() {
        JTextField txtTen = new JTextField(25);
        txtTen.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        String[] kyOptions = {"Quý 1", "Quý 2", "Quý 3", "Quý 4", "Cả năm"};
        String[] kyValues = {"quy_1", "quy_2", "quy_3", "quy_4", "ca_nam"};
        JComboBox<String> cboKy = new JComboBox<>(kyOptions);
        cboKy.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JSpinner spinNam = new JSpinner(new SpinnerNumberModel(LocalDate.now().getYear(), 2020, 2099, 1));
        spinNam.setFont(new Font("Segoe UI", Font.PLAIN, 13));

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
        criteriaPanel.setBorder(new TitledBorder("Chọn các tiêu chí"));
        List<JCheckBox> checkBoxes = new java.util.ArrayList<>();

        JLabel lblTotal = new JLabel("Tổng trọng số: 0%");
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTotal.setForeground(UIColors.DANGER_RED);

        java.awt.event.ActionListener calcTotalAction = e -> {
            int total = 0;
            for (JCheckBox cb : checkBoxes) {
                if (cb.isSelected()) {
                    TieuChiDanhGia c =
                            (TieuChiDanhGia) cb.getClientProperty("criteria");
                    total += c.getDiemToiDa();
                }
            }
            lblTotal.setText("Tổng trọng số (bắt buộc 100%): " + total + "%");
            lblTotal.setForeground(total == 100 ? UIColors.SUCCESS_GREEN : UIColors.DANGER_RED);
            criteriaPanel.repaint();
        };

        for (TieuChiDanhGia c : allCriteria) {
            JCheckBox cb = new JCheckBox(c.getName() + " (" + (int) c.getDiemToiDa() + "%)");
            cb.putClientProperty("criteria", c);
            cb.addActionListener(calcTotalAction);
            checkBoxes.add(cb);
            criteriaPanel.add(cb);
        }

        int initialTotal = checkBoxes.stream()
                .mapToInt(cb -> (int) ((TieuChiDanhGia) cb.getClientProperty("criteria")).getDiemToiDa())
                .sum();
        if (initialTotal == 100) {
            for (JCheckBox cb : checkBoxes) cb.setSelected(true);
        }
        calcTotalAction.actionPerformed(null);

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
                JOptionPane.showMessageDialog(this,
                        "Vui lòng nhập tên đợt đánh giá.",
                        "Lỗi", JOptionPane.WARNING_MESSAGE);
                continue;
            }

            List<TieuChiDanhGia> selectedCriteria = new java.util.ArrayList<>();
            int totalWeight = 0;
            for (JCheckBox cb : checkBoxes) {
                if (cb.isSelected()) {
                    TieuChiDanhGia c =
                            (TieuChiDanhGia) cb.getClientProperty("criteria");
                    c.setTrongSo(c.getDiemToiDa()); // diemToiDa dùng làm trọng số
                    selectedCriteria.add(c);
                    totalWeight += c.getDiemToiDa();
                }
            }

            if (totalWeight != 100) {
                JOptionPane.showMessageDialog(this,
                        "Tổng trọng số các tiêu chí được chọn phải chính xác 100%.",
                        "Lỗi trọng số", JOptionPane.WARNING_MESSAGE);
                continue;
            }

            LocalDate tuNgay = toLocalDate((java.util.Date) spinBD.getValue());
            LocalDate denNgay = toLocalDate((java.util.Date) spinKT.getValue());
            int kyIdx = cboKy.getSelectedIndex();

            DanhGiaBUS.KetQua<?> kq = evalService.taoDotDanhGia(
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
        int cycleId = (int) cycleTableModel.getValueAt(row, 0);
        DanhGiaBUS.KetQua<?> result = evalService.openCycle(cycleId);
        showResult(result);
        if (result.isSuccess()) loadData();
    }

    private void closeCycle() {
        int row = cycleTable.getSelectedRow();
        if (row < 0) return;
        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn đóng kỳ đánh giá này?\nSau khi đóng sẽ không thể sửa đổi.",
                "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        int cycleId = (int) cycleTableModel.getValueAt(row, 0);
        DanhGiaBUS.KetQua<?> result = evalService.closeCycle(cycleId);
        showResult(result);
        if (result.isSuccess()) loadData();
    }

    private void configCriteria() {
        EvalConfigDialog dialog = new EvalConfigDialog((Frame) SwingUtilities.getWindowAncestor(this));
        dialog.setVisible(true);
    }

    private void evaluateEmployee() {
        if (currentUser.getNhanVienId() == null || currentUser.getNhanVienId().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Tài khoản quản trị viên không đánh giá nhân viên\n",
                    "Không thể thực hiện",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        int row = cycleTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng chọn đợt đánh giá.",
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int cycleId = (int) cycleTableModel.getValueAt(row, 0);
        String cycleName = (String) cycleTableModel.getValueAt(row, 1);

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

    private void showResult(DanhGiaBUS.KetQua<?> result) {
        JOptionPane.showMessageDialog(this,
                result.getMessage(),
                result.isSuccess() ? "Thành công" : "Lỗi",
                result.isSuccess() ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);
    }
}
