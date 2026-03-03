package com.hrm.gui.evaluation;

import com.hrm.model.DotDanhGia;
import com.hrm.model.DanhGiaHieuSuat;
import com.hrm.model.TaiKhoan;
import com.hrm.bus.DanhGiaBUS;
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

/**
 * Panel quan ly dot danh gia hieu suat.
 */
public class EvalCycleListPanel extends JPanel {

    private final DanhGiaBUS evalService;
    private final TaiKhoan currentUser;
    private final boolean isAdmin;
    private final boolean isManager;

    private JTable cycleTable;
    private DefaultTableModel cycleTableModel;
    private JTable resultTable;
    private DefaultTableModel resultTableModel;
    private JButton btnTaoDot;
    private JButton btnOpenCycle;
    private JButton btnCloseCycle;
    private JButton btnConfigCriteria;
    private JButton btnEvaluate;
    private JButton btnViewResults;
    private JButton btnRefresh;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Status display strings — must match what we store in table col 6
    private static final String STATUS_CHUA   = "Chua bat dau";
    private static final String STATUS_DANG   = "Dang dien ra";
    private static final String STATUS_DA_KET = "Da ket thuc";

    public EvalCycleListPanel() {
        this.evalService  = DanhGiaBUS.getInstance();
        this.currentUser  = SessionContext.getInstance().getCurrentUser();
        this.isAdmin   = currentUser.hasRole("ADMIN") || currentUser.hasRole("HR");
        this.isManager = currentUser.hasRole("MANAGER") || currentUser.hasRole("DIRECTOR") || isAdmin;

        initComponents();
        setupLayout();
        loadData();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(UIColors.LIGHT_GRAY_BG);

        // Cycle table
        String[] cycleColumns = {"ID", "Ten dot danh gia", "Ky", "Nam", "Bat dau", "Ket thuc", "Trang thai"};
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
        cycleTable.getColumnModel().getColumn(1).setPreferredWidth(220);
        cycleTable.getColumnModel().getColumn(2).setPreferredWidth(60);
        cycleTable.getColumnModel().getColumn(3).setPreferredWidth(50);
        cycleTable.getColumnModel().getColumn(4).setPreferredWidth(90);
        cycleTable.getColumnModel().getColumn(5).setPreferredWidth(90);
        cycleTable.getColumnModel().getColumn(6).setPreferredWidth(110);

        cycleTable.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean sel, boolean focus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, value, sel, focus, row, col);
                if (!sel && value != null) {
                    String s = value.toString();
                    if (STATUS_DANG.equals(s))   c.setBackground(new Color(200, 255, 200));
                    else if (STATUS_DA_KET.equals(s)) c.setBackground(new Color(220, 220, 220));
                    else                         c.setBackground(new Color(255, 255, 200));
                }
                return c;
            }
        });

        // Result table
        String[] resultColumns = {"Nhan vien", "Nguoi danh gia", "Diem", "Xep loai", "Ngay danh gia"};
        resultTableModel = new DefaultTableModel(resultColumns, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        resultTable = new JTable(resultTableModel);
        resultTable.setRowHeight(25);
        resultTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        // Buttons
        btnTaoDot       = UIHelper.createSuccessButton("+ Tao dot moi");
        btnOpenCycle    = UIHelper.createPrimaryButton("Mo ky danh gia");
        btnCloseCycle   = UIHelper.createDangerButton("Dong ky danh gia");
        btnConfigCriteria = UIHelper.createDefaultButton("Cau hinh tieu chi");
        btnEvaluate     = UIHelper.createPrimaryButton("Danh gia nhan vien");
        btnViewResults  = UIHelper.createDefaultButton("Xem ket qua");
        btnRefresh      = UIHelper.createDefaultButton("Lam moi");

        btnOpenCycle.setEnabled(false);
        btnCloseCycle.setEnabled(false);
        btnEvaluate.setEnabled(false);

        btnRefresh.addActionListener(e -> loadData());
        btnTaoDot.addActionListener(e -> taoDot());
        btnOpenCycle.addActionListener(e -> openCycle());
        btnCloseCycle.addActionListener(e -> closeCycle());
        btnConfigCriteria.addActionListener(e -> configCriteria());
        btnEvaluate.addActionListener(e -> evaluateEmployee());
        btnViewResults.addActionListener(e -> viewResults());
    }

    private void setupLayout() {
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        topPanel.setOpaque(false);
        topPanel.add(btnRefresh);
        if (isAdmin) {
            topPanel.add(btnTaoDot);
            topPanel.add(btnOpenCycle);
            topPanel.add(btnCloseCycle);
            topPanel.add(btnConfigCriteria);
        }
        if (isManager) topPanel.add(btnEvaluate);
        topPanel.add(btnViewResults);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setDividerLocation(220);

        JScrollPane cycleScroll = new JScrollPane(cycleTable);
        cycleScroll.setBorder(new TitledBorder("Cac dot danh gia"));
        splitPane.setTopComponent(cycleScroll);

        JScrollPane resultScroll = new JScrollPane(resultTable);
        resultScroll.setBorder(new TitledBorder("Ket qua danh gia cua dot da chon"));
        splitPane.setBottomComponent(resultScroll);

        add(topPanel, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);
    }

    private void loadData() {
        cycleTableModel.setRowCount(0);
        resultTableModel.setRowCount(0);
        try {
            List<DotDanhGia> cycles = evalService.getAllCycles();
            for (DotDanhGia cycle : cycles) {
                cycleTableModel.addRow(new Object[]{
                    cycle.getId(),
                    cycle.getTenDot(),
                    kyDisplay(cycle.getKyDanhGia()),
                    cycle.getNam(),
                    cycle.getTuNgay()  != null ? cycle.getTuNgay().format(DATE_FORMAT)  : "",
                    cycle.getDenNgay() != null ? cycle.getDenNgay().format(DATE_FORMAT) : "",
                    statusDisplay(cycle.getTrangThai())
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Loi tai du lieu: " + ex.getMessage(),
                    "Loi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onCycleSelected() {
        int row = cycleTable.getSelectedRow();
        if (row < 0) return;

        String status = (String) cycleTableModel.getValueAt(row, 6);
        btnOpenCycle.setEnabled(isAdmin && STATUS_CHUA.equals(status));
        btnCloseCycle.setEnabled(isAdmin && STATUS_DANG.equals(status));
        btnEvaluate.setEnabled(isManager && STATUS_DANG.equals(status));

        int cycleId = (int) cycleTableModel.getValueAt(row, 0);
        loadCycleResults(cycleId);
    }

    private void loadCycleResults(int cycleId) {
        resultTableModel.setRowCount(0);
        try {
            List<DanhGiaHieuSuat> subs = evalService.getSubmissionsByCycle(cycleId);
            for (DanhGiaHieuSuat sub : subs) {
                resultTableModel.addRow(new Object[]{
                    sub.getEmployeeName() != null ? sub.getEmployeeName() : "#" + sub.getEmployeeId(),
                    sub.getTenNguoiDanhGia() != null ? sub.getTenNguoiDanhGia() : "#" + sub.getEvaluatorId(),
                    String.format("%.2f", sub.getOverallScore()),
                    sub.getXepLoai() != null ? sub.getXepLoai().getTenHienThi() : "",
                    sub.getSubmittedAt() != null
                            ? sub.getSubmittedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : ""
                });
            }
        } catch (Exception ex) {
            // Silently ignore — data may not exist yet
        }
    }

    private void taoDot() {
        JTextField txtTen = new JTextField(25);
        txtTen.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        String[] kyOptions = {"Quy 1", "Quy 2", "Quy 3", "Quy 4", "Ca nam"};
        String[] kyValues  = {"quy_1", "quy_2", "quy_3", "quy_4", "ca_nam"};
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

        JPanel form = new JPanel(new GridLayout(5, 2, 8, 8));
        form.add(new JLabel("Ten dot danh gia (*):")); form.add(txtTen);
        form.add(new JLabel("Ky danh gia:"));          form.add(cboKy);
        form.add(new JLabel("Nam:"));                  form.add(spinNam);
        form.add(new JLabel("Ngay bat dau:"));         form.add(spinBD);
        form.add(new JLabel("Ngay ket thuc:"));        form.add(spinKT);

        int res = JOptionPane.showConfirmDialog(this, form,
                "Tao dot danh gia moi", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION) return;

        String ten = txtTen.getText().trim();
        if (ten.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui long nhap ten dot danh gia.", "Loi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        LocalDate tuNgay = toLocalDate((java.util.Date) spinBD.getValue());
        LocalDate denNgay = toLocalDate((java.util.Date) spinKT.getValue());
        int kyIdx = cboKy.getSelectedIndex();

        DanhGiaBUS.KetQua<?> kq = evalService.taoDotDanhGia(
                ten, (int) spinNam.getValue(), kyValues[kyIdx], tuNgay, denNgay);
        showResult(kq);
        if (kq.isSuccess()) loadData();
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
                "Ban co chac muon dong ky danh gia nay?\nSau khi dong se khong the sua doi.",
                "Xac nhan", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        int cycleId = (int) cycleTableModel.getValueAt(row, 0);
        DanhGiaBUS.KetQua<?> result = evalService.closeCycle(cycleId);
        showResult(result);
        if (result.isSuccess()) loadData();
    }

    private void configCriteria() {
        EvalConfigDialog dialog = new EvalConfigDialog(
                (Frame) SwingUtilities.getWindowAncestor(this));
        dialog.setVisible(true);
    }

    private void evaluateEmployee() {
        int row = cycleTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui long chon dot danh gia.",
                    "Thong bao", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int cycleId = (int) cycleTableModel.getValueAt(row, 0);
        String cycleName = (String) cycleTableModel.getValueAt(row, 1);
        EvalDoDialog dialog = new EvalDoDialog(
                (Frame) SwingUtilities.getWindowAncestor(this), cycleId, cycleName);
        dialog.setVisible(true);
        if (dialog.isSuccessful()) {
            loadData();
            onCycleSelected();
        }
    }

    private void viewResults() {
        EvalResultPanel resultPanel = new EvalResultPanel();
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "Ket qua danh gia", true);
        dialog.setContentPane(resultPanel);
        dialog.setSize(800, 600);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private String statusDisplay(DotDanhGia.TrangThai t) {
        if (t == null) return STATUS_CHUA;
        switch (t) {
            case DANG_DIEN_RA: return STATUS_DANG;
            case DA_KET_THUC:  return STATUS_DA_KET;
            default:           return STATUS_CHUA;
        }
    }

    private String kyDisplay(String ky) {
        if (ky == null) return "";
        switch (ky) {
            case "quy_1": return "Q1";
            case "quy_2": return "Q2";
            case "quy_3": return "Q3";
            case "quy_4": return "Q4";
            case "ca_nam": return "Ca nam";
            default: return ky;
        }
    }

    private LocalDate toLocalDate(java.util.Date d) {
        return d.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
    }

    private void showResult(DanhGiaBUS.KetQua<?> result) {
        JOptionPane.showMessageDialog(this,
                result.getMessage(),
                result.isSuccess() ? "Thanh cong" : "Loi",
                result.isSuccess() ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);
    }
}
