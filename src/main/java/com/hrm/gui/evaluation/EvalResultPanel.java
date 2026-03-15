package com.hrm.gui.evaluation;

import com.hrm.bus.DanhGiaBUS;
import com.hrm.model.ChiTietDanhGia;
import com.hrm.model.DanhGiaHieuSuat;
import com.hrm.model.DotDanhGia;
import com.hrm.model.NhanVien;
import com.hrm.model.TaiKhoan;
import com.hrm.util.SessionContext;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

public class EvalResultPanel extends JPanel {
    private final DanhGiaBUS evalService;
    private final TaiKhoan currentUser;
    private final boolean isManager;
    private final String myMaNV;
    private final int presetCycleId;

    private JTable submissionTable;
    private DefaultTableModel submissionModel;
    private JTable detailTable;
    private DefaultTableModel detailModel;
    private JTextArea txtComment;
    private JComboBox<Object> cboNhanVien;
    private JComboBox<Object> cboKyDanhGia;

    private List<DanhGiaHieuSuat> cachedSubmissions;

    private static final DateTimeFormatter DT_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public EvalResultPanel() {
        this(-1);
    }

    public EvalResultPanel(int presetCycleId) {
        this.evalService = DanhGiaBUS.getInstance();
        this.currentUser = SessionContext.getInstance().getCurrentUser();
        this.isManager = currentUser.coQuyen("EVAL_MANAGE") || currentUser.coQuyen("EVAL_REVIEW");
        this.presetCycleId = presetCycleId;

        this.myMaNV = currentUser.getNhanVienId();

        initComponents();
        setupLayout();
        loadData();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15));

        cboNhanVien = new JComboBox<>();
        if (isManager) {
            cboNhanVien.addItem("Tất cả");
            List<NhanVien> dsNV = com.hrm.bus.NhanVienBUS.getInstance()
                    .getAllByActionScope("EVAL_VIEW", currentUser.getNhanVienId());
            for (NhanVien nv : dsNV) {
                cboNhanVien.addItem(nv);
            }
            cboNhanVien.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value,
                                                              int index, boolean isSelected, boolean cellHasFocus) {
                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    if (value instanceof NhanVien) {
                        NhanVien nv = (NhanVien) value;
                        setText("[" + nv.getMaNhanVien() + "] " + nv.getHoTen());
                    } else if (value != null) {
                        setText(value.toString());
                    }
                    return this;
                }
            });
        }

        cboKyDanhGia = new JComboBox<>();
        cboKyDanhGia.addItem("Tất cả kỳ đánh giá");
        List<DotDanhGia> dsKy = evalService.getAllCycles();
        if (dsKy != null) {
            for (DotDanhGia ky : dsKy) {
                String ten = ky.getTenDot() != null ? ky.getTenDot() : ("Đợt #" + ky.getId());
                cboKyDanhGia.addItem(new KyDanhGiaItem(ky.getId(), ten));
            }
        }
        cboKyDanhGia.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                                                          int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof KyDanhGiaItem) {
                    setText(((KyDanhGiaItem) value).displayText);
                } else if (value != null) {
                    setText(value.toString());
                }
                return this;
            }
        });

        if (presetCycleId > 0) {
            for (int i = 0; i < cboKyDanhGia.getItemCount(); i++) {
                Object item = cboKyDanhGia.getItemAt(i);
                if (item instanceof KyDanhGiaItem && ((KyDanhGiaItem) item).cycleId == presetCycleId) {
                    cboKyDanhGia.setSelectedIndex(i);
                    break;
                }
            }
        }

        String[] subColumns = {"ID", "Kỳ đánh giá", "Nhân viên", "Người đánh giá", "Điểm", "Xếp loại", "Ngày"};
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
                    if ("Xuất sắc".equals(rating))        { c.setBackground(com.hrm.util.UIColors.LIGHT_GREEN_BG); c.setForeground(com.hrm.util.UIColors.DARK_GREEN); }
                    else if ("Tốt".equals(rating))        { c.setBackground(com.hrm.util.UIColors.LIGHT_GREEN_BG); c.setForeground(com.hrm.util.UIColors.DARK_GREEN); }
                    else if ("Khá".equals(rating))        { c.setBackground(com.hrm.util.UIColors.LIGHT_YELLOW_BG); c.setForeground(com.hrm.util.UIColors.WARNING_TEXT_AMBER); }
                    else if ("Trung bình".equals(rating)) { c.setBackground(com.hrm.util.UIColors.LIGHT_RED_BG); c.setForeground(com.hrm.util.UIColors.WARNING_TEXT_AMBER); }
                    else                                  { c.setBackground(com.hrm.util.UIColors.LIGHT_RED_BG); c.setForeground(com.hrm.util.UIColors.DANGER_RED); }
                }
                return c;
            }
        });
        TableRowSorter<DefaultTableModel> submissionSorter = new TableRowSorter<>(submissionModel);
        submissionTable.setRowSorter(submissionSorter);

        // Chỉ cho sort cột 0 (ID) và cột 2 (Nhân viên)
        for (int i = 0; i < 7; i++) {
            submissionSorter.setSortable(i, false);
        }
        submissionSorter.setComparator(0, Comparator.comparingInt(a -> (Integer) a));
        submissionSorter.setSortable(0, true); // ID
        submissionSorter.setSortable(2, true); // Nhân viên

        // Comparator tiếng Việt cho cột Nhân viên
        submissionSorter.setComparator(2, com.hrm.util.UIHelper.vietnameseNameComparator());

        // Mặc định sort theo ID tăng dần
        submissionSorter.setSortKeys(List.of(new RowSorter.SortKey(0, SortOrder.ASCENDING)));
        String[] detailColumns = {"Tiêu chí", "Trọng số", "Điểm (1-10)", "Điểm quy đổi", "Nhận xét"};
        detailModel = new DefaultTableModel(detailColumns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        detailTable = new JTable(detailModel);
        detailTable.setRowHeight(25);

        txtComment = new JTextArea(3, 40);
        txtComment.setEditable(false);
        txtComment.setLineWrap(true);
        txtComment.setWrapStyleWord(true);
        txtComment.setBackground(com.hrm.util.UIColors.LIGHT_GRAY_BG);

        if (isManager) {
            cboNhanVien.addActionListener(e -> loadData());
        }
        cboKyDanhGia.addActionListener(e -> loadData());
    }

    private void setupLayout() {
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        if (isManager) {
            topPanel.add(new JLabel("Nhân viên:"));
            topPanel.add(cboNhanVien);
        }
        topPanel.add(new JLabel("Kỳ đánh giá:"));
        topPanel.add(cboKyDanhGia);
        JButton btnLamMoi = new JButton("Lam moi");
        btnLamMoi.addActionListener(e -> loadData());
        topPanel.add(btnLamMoi);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setDividerLocation(250);

        JScrollPane subScroll = new JScrollPane(submissionTable);
        subScroll.setBorder(new TitledBorder("Danh sách đánh giá"));
        splitPane.setTopComponent(subScroll);

        JPanel detailPanel = new JPanel(new BorderLayout(10, 10));
        detailPanel.setBorder(new TitledBorder("Chi tiết đánh giá (chọn hàng để xem)"));
        JScrollPane detailScroll = new JScrollPane(detailTable);
        JPanel commentPanel = new JPanel(new BorderLayout(5, 5));
        commentPanel.add(new JLabel("Nhận xét chung:"), BorderLayout.NORTH);
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

        String filterMaNV = null;
        if (isManager && cboNhanVien.getSelectedItem() instanceof NhanVien) {
            filterMaNV = ((NhanVien) cboNhanVien.getSelectedItem()).getMaNhanVien();
        }

        int filterKyId = -1;
        if (cboKyDanhGia.getSelectedItem() instanceof KyDanhGiaItem) {
            filterKyId = ((KyDanhGiaItem) cboKyDanhGia.getSelectedItem()).cycleId;
        }

        cachedSubmissions = evalService.getAllSubmissionsByScope(myMaNV);
        if (cachedSubmissions == null) return;

        for (DanhGiaHieuSuat sub : cachedSubmissions) {
            if (filterMaNV != null && !filterMaNV.equals(sub.getEmployeeId())) continue;
            if (filterKyId > 0 && sub.getDotDanhGiaId() != filterKyId) continue;

            submissionModel.addRow(new Object[]{
                    sub.getId(),
                    sub.getTenDot() != null ? sub.getTenDot() : ("#" + sub.getDotDanhGiaId()),
                    sub.getEmployeeName() != null ? sub.getEmployeeName() : ("#" + sub.getEmployeeId()),
                    sub.getTenNguoiDanhGia() != null ? sub.getTenNguoiDanhGia() : ("#" + sub.getEvaluatorId()),
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

        int modelRow = submissionTable.convertRowIndexToModel(viewRow);
        int submissionId = (int) submissionModel.getValueAt(modelRow, 0);

        DanhGiaHieuSuat sub = cachedSubmissions.stream()
                .filter(s -> s.getId() == submissionId).findFirst().orElse(null);
        if (sub == null) {
            detailModel.setRowCount(0);
            txtComment.setText("");
            return;
        }

        if (sub.getScores().isEmpty()) {
            sub.setChiTietDanhGias(evalService.loadScores(sub.getId()));
        }

        detailModel.setRowCount(0);
        for (ChiTietDanhGia score : sub.getScores()) {
            detailModel.addRow(new Object[]{
                    score.getCriteriaName() != null ? score.getCriteriaName() : ("TC#" + score.getCriteriaId()),
                    String.format("%.0f%%", score.getTrongSo()),
                    String.format("%.1f", score.getScore()),
                    String.format("%.2f", score.getWeightedScore()),
                    score.getComment() != null ? score.getComment() : ""
            });
        }

        if (!sub.getScores().isEmpty()) {
            detailModel.addRow(new Object[]{
                    "TỔNG CỘNG", "100%", "",
                    String.format("%.2f", sub.getOverallScore()),
                    sub.getXepLoai() != null ? sub.getXepLoai().getTenHienThi() : ""
            });
        }

        txtComment.setText(sub.getGeneralComment() != null ? sub.getGeneralComment() : "");
    }

    private static class KyDanhGiaItem {
        private final int cycleId;
        private final String displayText;

        private KyDanhGiaItem(int cycleId, String displayText) {
            this.cycleId = cycleId;
            this.displayText = displayText;
        }
    }
}
