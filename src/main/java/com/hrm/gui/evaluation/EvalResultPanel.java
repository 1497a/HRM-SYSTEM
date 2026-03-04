package com.hrm.gui.evaluation;

import com.hrm.model.ChiTietDanhGia;
import com.hrm.model.DanhGiaHieuSuat;
import com.hrm.model.TaiKhoan;
import com.hrm.model.BoNhiem;
import com.hrm.bus.DanhGiaBUS;
import com.hrm.dao.BoNhiemDAO;
import com.hrm.util.SessionContext;
import com.hrm.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Evaluation Result Panel - view evaluation results with role-based access control.
 */
public class EvalResultPanel extends JPanel {
    private final DanhGiaBUS evalService;
    private final TaiKhoan currentUser;
    private final boolean isAdmin;
    private final boolean isDirector;
    private final boolean isDeptHead;
    private final boolean isManager;
    private final int myMaNV;
    private final String myMaPhongBan;

    private JTable submissionTable;
    private DefaultTableModel submissionModel;
    private JTable detailTable;
    private DefaultTableModel detailModel;
    private JTextArea txtComment;
    private JComboBox<Object> cboNhanVien;

    // Cached submissions for detail view
    private List<DanhGiaHieuSuat> cachedSubmissions;

    private static final DateTimeFormatter DT_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public EvalResultPanel() {
        this.evalService = DanhGiaBUS.getInstance();
        this.currentUser = SessionContext.getInstance().getCurrentUser();
        this.isAdmin    = currentUser.coQuyen("EVAL_VIEW_ALL");
        this.isDirector = false;
        this.isDeptHead = false;
        this.isManager  = currentUser.coQuyen("EVAL_VIEW_ALL") || currentUser.coQuyen("EVAL_VIEW_TEAM");

        int tmpMaNV = 0;
        String tmpDept = null;
        if (currentUser.getMaNV() != null) {
            tmpMaNV = currentUser.getMaNV();
            try {
                BoNhiem bn = BoNhiemDAO.getInstance().findBoNhiemChinhHieuLuc(tmpMaNV);
                if (bn != null) tmpDept = bn.getPhongBanId();
            } catch (Exception ignored) {}
        }
        this.myMaNV = tmpMaNV;
        this.myMaPhongBan = tmpDept;

        initComponents();
        setupLayout();
        loadData();
    }

    private boolean checkDeptHead() {
        if (currentUser.getMaNV() == null) return false;
        try {
            BoNhiem bn = BoNhiemDAO.getInstance().findBoNhiemChinhHieuLuc(currentUser.getMaNV());
            if (bn != null && bn.getChucVuId() != null) {
                String cv = bn.getChucVuId().toLowerCase();
                return cv.contains("truong_phong") || cv.contains("truongphong");
            }
        } catch (Exception ignored) {}
        return false;
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15));

        cboNhanVien = new JComboBox<>();
        if (isManager) {
            cboNhanVien.addItem("Tat ca");
            List<com.hrm.model.NhanVien> dsNV = com.hrm.bus.NhanVienBUS.getInstance().getAllByActionScope("EVAL_VIEW", currentUser.getId());
            for (com.hrm.model.NhanVien nv : dsNV) {
                cboNhanVien.addItem(nv);
            }
            cboNhanVien.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value,
                        int index, boolean isSelected, boolean cellHasFocus) {
                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    if (value instanceof com.hrm.model.NhanVien) {
                        com.hrm.model.NhanVien nv = (com.hrm.model.NhanVien) value;
                        setText("[" + nv.getMaNhanVien() + "] " + nv.getHoTen());
                    } else if (value != null) {
                        setText(value.toString());
                    }
                    return this;
                }
            });
            cboNhanVien.addActionListener(e -> loadData());
        }

        String[] subColumns = {"ID", "Ky danh gia", "Nhan vien", "Nguoi danh gia", "Diem", "Xep loai", "Ngay"};
        submissionModel = new DefaultTableModel(subColumns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        submissionTable = new JTable(submissionModel);
        submissionTable.setRowHeight(28);
        submissionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        submissionTable.getSelectionModel().addListSelectionListener(e -> showDetail());

        submissionTable.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected && value != null) {
                    String rating = value.toString();
                    if ("Xuat sac".equals(rating))        { c.setBackground(new Color(200, 255, 200)); c.setForeground(new Color(0, 100, 0)); }
                    else if ("Tot".equals(rating))        { c.setBackground(new Color(220, 255, 220)); c.setForeground(new Color(0, 128, 0)); }
                    else if ("Kha".equals(rating))        { c.setBackground(new Color(255, 255, 200)); c.setForeground(new Color(128, 128, 0)); }
                    else if ("Trung binh".equals(rating)) { c.setBackground(new Color(255, 220, 200)); c.setForeground(new Color(200, 100, 0)); }
                    else                                  { c.setBackground(new Color(255, 200, 200)); c.setForeground(Color.RED); }
                }
                return c;
            }
        });

        String[] detailColumns = {"Tieu chi", "Trong so", "Diem (1-10)", "Diem quy doi", "Nhan xet"};
        detailModel = new DefaultTableModel(detailColumns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        detailTable = new JTable(detailModel);
        detailTable.setRowHeight(25);

        txtComment = new JTextArea(3, 40);
        txtComment.setEditable(false);
        txtComment.setLineWrap(true);
        txtComment.setWrapStyleWord(true);
        txtComment.setBackground(new Color(245, 245, 245));
    }

    private void setupLayout() {
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        if (isManager) {
            topPanel.add(new JLabel("Nhan vien:"));
            topPanel.add(cboNhanVien);
        }
        JButton btnRefresh = UIHelper.createDefaultButton("Lam moi");
        btnRefresh.addActionListener(e -> loadData());
        topPanel.add(btnRefresh);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setDividerLocation(250);

        JScrollPane subScroll = new JScrollPane(submissionTable);
        subScroll.setBorder(new TitledBorder("Danh sach danh gia"));
        splitPane.setTopComponent(subScroll);

        JPanel detailPanel = new JPanel(new BorderLayout(10, 10));
        detailPanel.setBorder(new TitledBorder("Chi tiet danh gia (chon hang de xem)"));
        JScrollPane detailScroll = new JScrollPane(detailTable);
        JPanel commentPanel = new JPanel(new BorderLayout(5, 5));
        commentPanel.add(new JLabel("Nhan xet chung:"), BorderLayout.NORTH);
        commentPanel.add(new JScrollPane(txtComment), BorderLayout.CENTER);
        detailPanel.add(detailScroll, BorderLayout.CENTER);
        detailPanel.add(commentPanel, BorderLayout.SOUTH);
        splitPane.setBottomComponent(detailPanel);

        add(topPanel, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);
    }

    private void loadData() {
        submissionModel.setRowCount(0);
        detailModel.setRowCount(0);
        txtComment.setText("");

        int filterMaNV = -1;
        if (isManager && cboNhanVien.getSelectedItem() instanceof com.hrm.model.NhanVien) {
            filterMaNV = ((com.hrm.model.NhanVien) cboNhanVien.getSelectedItem()).getId();
        }

        // Fetch all allowed submissions using new Scope architecture
        cachedSubmissions = evalService.getAllSubmissionsByScope(myMaNV);

        if (cachedSubmissions == null) return;
        for (DanhGiaHieuSuat sub : cachedSubmissions) {
            if (filterMaNV > 0 && sub.getEmployeeId() != filterMaNV) {
                continue;   // filter locally for fast UX
            }

            submissionModel.addRow(new Object[]{
                sub.getId(),
                sub.getTenDot() != null ? sub.getTenDot() : "#" + sub.getDotDanhGiaId(),
                sub.getEmployeeName() != null ? sub.getEmployeeName() : "#" + sub.getEmployeeId(),
                sub.getTenNguoiDanhGia() != null ? sub.getTenNguoiDanhGia() : "#" + sub.getEvaluatorId(),
                String.format("%.2f", sub.getOverallScore()),
                sub.getXepLoai() != null ? sub.getXepLoai().getTenHienThi() : "?",
                sub.getSubmittedAt() != null ? sub.getSubmittedAt().format(DT_FORMAT) : ""
            });
        }
    }

    private void showDetail() {
        int viewRow = submissionTable.getSelectedRow();
        if (viewRow < 0 || cachedSubmissions == null) {
            detailModel.setRowCount(0);
            txtComment.setText("");
            return;
        }

        // Convert view index to model index (important if table is sorted)
        int modelRow = submissionTable.convertRowIndexToModel(viewRow);

        // Get submission ID from column 0 of model row for accurate lookup
        int submissionId = (int) submissionModel.getValueAt(modelRow, 0);
        DanhGiaHieuSuat sub = null;
        for (DanhGiaHieuSuat s : cachedSubmissions) {
            if (s.getId() == submissionId) { sub = s; break; }
        }
        if (sub == null) {
            detailModel.setRowCount(0);
            txtComment.setText("");
            return;
        }

        // If scores not yet loaded, load now
        if (sub.getScores().isEmpty()) {
            sub.setChiTietDanhGias(evalService.loadScores(sub.getId()));
        }

        detailModel.setRowCount(0);
        for (ChiTietDanhGia score : sub.getScores()) {
            detailModel.addRow(new Object[]{
                score.getCriteriaName() != null ? score.getCriteriaName() : "TC#" + score.getCriteriaId(),
                String.format("%.0f%%", score.getTrongSo()),
                String.format("%.1f", score.getScore()),
                String.format("%.2f", score.getWeightedScore()),
                score.getComment() != null ? score.getComment() : ""
            });
        }

        if (!sub.getScores().isEmpty()) {
            detailModel.addRow(new Object[]{
                "TONG CONG", "100%", "",
                String.format("%.2f", sub.getOverallScore()),
                sub.getXepLoai() != null ? sub.getXepLoai().getTenHienThi() : ""
            });
        }

        txtComment.setText(sub.getGeneralComment() != null ? sub.getGeneralComment() : "");
    }
}
