package com.hrm.gui.leave;

import com.hrm.model.DonXinNghiPhep;
import com.hrm.model.NhanVien;
import com.hrm.model.SoDungPhep;
import com.hrm.model.TaiKhoan;
import com.hrm.bus.NghiPhepBUS;
import com.hrm.util.SessionContext;
import com.hrm.util.UIHelper;

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

/**
 * Leave List Panel - displays leave requests
 */
public class LeaveListPanel extends JPanel {
    private final NghiPhepBUS leaveService;
    private final TaiKhoan currentUser;
    private final boolean isManager;

    private JTable table;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    private JComboBox<String> cboStatus;
    private JComboBox<Object> cboNhanVien;
    private JButton btnCreate;
    private JButton btnApprove;

    private JPanel balancePanel;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public LeaveListPanel() {
        this.leaveService = NghiPhepBUS.getInstance();
        this.currentUser = SessionContext.getInstance().getCurrentUser();
        this.isManager = currentUser.coQuyen("LEAVE_VIEW_ALL") || currentUser.coQuyen("LEAVE_VIEW_TEAM");

        initComponents();
        setupLayout();
        loadData();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15));

        // Status combo
        cboStatus = new JComboBox<>(new String[]{"Tat ca", "Cho duyet", "Da duyet", "Tu choi"});
        cboStatus.addActionListener(e -> applyFilter());

        // Nhan vien combo (only for manager/hr/director)
        cboNhanVien = new JComboBox<>();
        if (isManager) {
            cboNhanVien.addItem("Tat ca");
            String currentMaNV = currentUser.getNhanVienId();
            List<NhanVien> dsNV = com.hrm.bus.NhanVienBUS.getInstance().getAllByActionScope("LEAVE_VIEW", currentMaNV);
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
            cboNhanVien.addActionListener(e -> loadData());
        }

        // Buttons
        btnCreate = UIHelper.createSuccessButton("Tao don moi");
        btnCreate.addActionListener(e -> createRequest());

        btnApprove = UIHelper.createPrimaryButton("Xu ly don");
        btnApprove.setEnabled(isManager);
        btnApprove.addActionListener(e -> approveRequest());

        // Table
        String[] columns = {"ID", "Nhan vien", "Loai phep", "Tu ngay", "Den ngay",
                "So ngay", "Ly do", "Trang thai", "Nguoi duyet"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setRowHeight(28);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(0).setPreferredWidth(40);
        table.getColumnModel().getColumn(1).setPreferredWidth(160);
        table.getColumnModel().getColumn(2).setPreferredWidth(100);
        table.getColumnModel().getColumn(3).setPreferredWidth(90);
        table.getColumnModel().getColumn(4).setPreferredWidth(90);
        table.getColumnModel().getColumn(5).setPreferredWidth(70);
        table.getColumnModel().getColumn(6).setPreferredWidth(300);
        table.getColumnModel().getColumn(7).setPreferredWidth(120);
        table.getColumnModel().getColumn(8).setPreferredWidth(140);

        // Status cell renderer
        table.getColumnModel().getColumn(7).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    String status = (String) value;
                    if (com.hrm.model.DonXinNghiPhep.TrangThai.DA_DUYET.getTenHienThi().equals(status)) {
                        c.setBackground(new Color(200, 255, 200));
                    } else if (com.hrm.model.DonXinNghiPhep.TrangThai.TU_CHOI.getTenHienThi().equals(status)) {
                        c.setBackground(new Color(255, 200, 200));
                    } else if (com.hrm.model.DonXinNghiPhep.TrangThai.CHO_DUYET.getTenHienThi().equals(status)) {
                        c.setBackground(new Color(255, 255, 200));
                    } else {
                        c.setBackground(Color.WHITE);
                    }
                }
                return c;
            }
        });

        // Sorter – sort by employee name (col 1) using Vietnamese locale
        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        // Chỉ cho sort cột 0 (ID) và cột 1 (Nhân viên)
        for (int i = 0; i < 9; i++) {
            sorter.setSortable(i, false);
        }
        sorter.setComparator(0,Comparator.comparingInt(a -> (Integer) a));
         // ID
        sorter.setSortable(1, true); // Nhân viên

        // Comparator tiếng Việt cho cột Nhân viên
        sorter.setComparator(1, UIHelper.vietnameseNameComparator());

        // Mặc định sort theo ID tăng dần
        sorter.setSortKeys(List.of(new RowSorter.SortKey(0, SortOrder.ASCENDING)));

        // Balance Panel
        balancePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 5));
        balancePanel.setBorder(new TitledBorder("So ngay phep con lai"));
    }

    private void setupLayout() {
        // Top panel
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setBorder(new EmptyBorder(0, 0, 10, 0));

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        if (isManager) {
            filterPanel.add(new JLabel("Nhan vien:"));
            filterPanel.add(cboNhanVien);
        }
        filterPanel.add(new JLabel("Trang thai:"));
        filterPanel.add(cboStatus);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.add(btnCreate);
        if (isManager) {
            buttonPanel.add(btnApprove);
        }

        topPanel.add(filterPanel, BorderLayout.WEST);
        topPanel.add(buttonPanel, BorderLayout.EAST);
        topPanel.add(balancePanel, BorderLayout.SOUTH);

        // Center panel - table
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(new TitledBorder("Danh sach don nghi phep"));

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void loadData() {
        tableModel.setRowCount(0);
        String empId = currentUser.getNhanVienId();
        String filterMaNV = isManager && cboNhanVien.getSelectedItem() instanceof NhanVien
                ? ((NhanVien) cboNhanVien.getSelectedItem()).getMaNhanVien() : null;

        List<DonXinNghiPhep> requests = isManager
                ? leaveService.getAllRequestsByScope(empId)
                : leaveService.getMyRequests(empId);

        for (DonXinNghiPhep req : requests) {
            if (filterMaNV != null && !filterMaNV.equals(req.getEmployeeId())) continue;
            tableModel.addRow(new Object[]{
                req.getId(), req.getEmployeeName(), req.getLeaveTypeName(),
                req.getStartDate().format(DATE_FORMAT), req.getEndDate().format(DATE_FORMAT),
                req.getTotalDays(), req.getReason(),
                req.getTrangThai().getTenHienThi(),
                req.getApproverName() != null ? req.getApproverName() : "-"
            });
        }

        applyFilter();
        String targetEmpId = filterMaNV != null ? filterMaNV : empId;
        if (targetEmpId != null) {
            updateBalanceDisplay(targetEmpId);
        } else {
            balancePanel.removeAll();
            balancePanel.revalidate();
            balancePanel.repaint();
        }
    }

    private void applyFilter() {
        String s = (String) cboStatus.getSelectedItem();
        sorter.setRowFilter("Tat ca".equals(s) ? null : RowFilter.regexFilter("^" + s + "$", 7));
    }

    private void updateBalanceDisplay(String empId) {
        balancePanel.removeAll();
        List<SoDungPhep> balances = leaveService.getBalances(empId);
        for (SoDungPhep balance : balances) {
            JLabel lbl = new JLabel(getLeaveTypeName(balance.getLeaveTypeCode()) +
                    ": " + balance.getRemainingDays() + "/" + balance.getTotalDays() + " ngay");
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
            if (balance.getRemainingDays() <= 3) {
                lbl.setForeground(Color.RED);
            }
            balancePanel.add(lbl);
        }
        balancePanel.revalidate();
        balancePanel.repaint();
    }

    private String getLeaveTypeName(String code) {
        return leaveService.getAllLeaveTypes().stream()
                .filter(t -> t.getId().equals(code))
                .map(t -> t.getName())
                .findFirst()
                .orElse(code);
    }

    private void createRequest() {
        if (currentUser.getNhanVienId() == null) {
            String message = currentUser.coVaiTro("ADMIN")
                    ? "Tai khoan admin khong can tao yeu cau nghi phep."
                    : "Tai khoan cua ban chua gan ma nhan vien nen khong the tao don nghi phep.";
            JOptionPane.showMessageDialog(this,
                    message,
                    "Thong bao",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        LeaveCreateDialog dialog = new LeaveCreateDialog(
                (Frame) SwingUtilities.getWindowAncestor(this));
        dialog.setVisible(true);
        if (dialog.isSuccessful()) {
            loadData();
        }
    }

    private void approveRequest() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                    "Vui long chon don can duyet",
                    "Thong bao",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = table.convertRowIndexToModel(selectedRow);
        int requestId = (int) tableModel.getValueAt(modelRow, 0);
        String status = (String) tableModel.getValueAt(modelRow, 7);

        if (!DonXinNghiPhep.TrangThai.CHO_DUYET.getTenHienThi().equals(status)) {
            JOptionPane.showMessageDialog(this,
                    "Chi co the duyet don dang cho duyet",
                    "Thong bao",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        LeaveApproveDialog dialog = new LeaveApproveDialog(
                (Frame) SwingUtilities.getWindowAncestor(this), requestId);
        dialog.setVisible(true);
        loadData();
    }
}
