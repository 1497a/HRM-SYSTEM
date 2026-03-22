package com.hrm.gui.leave;

import com.hrm.model.DonXinNghiPhep;
import com.hrm.model.NhanVien;
import com.hrm.model.SoDungPhep;
import com.hrm.model.TaiKhoan;
import com.hrm.bus.NghiPhepBUS;
import com.hrm.util.HRMConstants;
import com.hrm.util.UIColors;
import com.hrm.util.PermissionCodes;
import com.hrm.util.SessionContext;
import com.hrm.gui.components.PurpleTable;
import com.hrm.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
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
    private final boolean canApprove;
    private PurpleTable table;
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
        com.hrm.bus.XacThucBUS authBus = com.hrm.bus.XacThucBUS.getInstance();
        com.hrm.model.DataScope leaveViewScope = authBus.getScopeForAction(PermissionCodes.LEAVE_VIEW);
        com.hrm.model.DataScope leaveApproveScope = authBus.getScopeForAction(PermissionCodes.LEAVE_APPROVE);
        this.isManager = leaveViewScope != com.hrm.model.DataScope.NONE
                      && leaveViewScope != com.hrm.model.DataScope.SELF;
        this.canApprove = leaveApproveScope != com.hrm.model.DataScope.NONE;
        initComponents();
        setupLayout();
        loadData();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        // Status combo
        cboStatus = new JComboBox<>(new String[]{"Tất cả",
                DonXinNghiPhep.TrangThai.CHO_DUYET.getTenHienThi(),
                DonXinNghiPhep.TrangThai.DA_DUYET.getTenHienThi(),
                DonXinNghiPhep.TrangThai.TU_CHOI.getTenHienThi()});
        cboStatus.addActionListener(e -> applyFilter());
        // Nhan vien combo (only for manager/hr/director)
        cboNhanVien = new JComboBox<>();
        if (isManager) {
            cboNhanVien.addItem("Tất cả");
            String currentMaNV = currentUser.getMaNV();
            List<NhanVien> dsNV = com.hrm.bus.NhanVienBUS.getInstance().getAllByActionScope(PermissionCodes.LEAVE_VIEW, currentMaNV);
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
        btnCreate = UIHelper.createSuccessButton("Tạo đơn mới");
        btnCreate.addActionListener(e -> createRequest());
        btnApprove = UIHelper.createPrimaryButton("Xử lý đơn");
        btnApprove.setEnabled(canApprove);
        btnApprove.addActionListener(e -> approveRequest());
        // Table
        String[] columns = {"ID", "Nhân viên", "Loại phép", "Từ ngày", "Đến ngày",
                "Số ngày", "Lý do", "Trạng thái", "Người duyệt"};
        tableModel = PurpleTable.createNonEditableModel(columns);
        table = new PurpleTable(tableModel);
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
                        c.setBackground(UIColors.BG_SUCCESS);
                    } else if (com.hrm.model.DonXinNghiPhep.TrangThai.TU_CHOI.getTenHienThi().equals(status)) {
                        c.setBackground(UIColors.BG_DANGER);
                    } else if (com.hrm.model.DonXinNghiPhep.TrangThai.CHO_DUYET.getTenHienThi().equals(status)) {
                        c.setBackground(UIColors.BG_WARNING);
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
        balancePanel.setBorder(BorderFactory.createTitledBorder("Số ngày phép còn lại"));
    }

    private void setupLayout() {
        // Top panel: hint + filters + balance
        JPanel topPanel = new JPanel(new BorderLayout(10, 4));
        topPanel.setBorder(new EmptyBorder(0, 0, 10, 0));
        JLabel lblHint = new JLabel("Nhấp đúp vào đơn để xem chi tiết. Chọn đơn rồi nhấn 'Xử lý đơn' để duyệt/từ chối.");
        lblHint.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblHint.setForeground(UIColors.TEXT_DARK);
        topPanel.add(lblHint, BorderLayout.NORTH);
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        filterPanel.setOpaque(false);
        if (isManager) {
            filterPanel.add(new JLabel("Nhân viên:"));
            filterPanel.add(cboNhanVien);
        }
        filterPanel.add(new JLabel("Trạng thái:"));
        filterPanel.add(cboStatus);
        topPanel.add(filterPanel, BorderLayout.CENTER);
        topPanel.add(balancePanel, BorderLayout.SOUTH);
        // Center panel - table
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        // South panel - action buttons
        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        southPanel.setOpaque(false);
        southPanel.add(btnCreate);
        if (canApprove) {
            southPanel.add(btnApprove);
        }
        JButton btnLamMoi = UIHelper.createDefaultButton("Làm mới");
        btnLamMoi.addActionListener(e -> loadData());
        southPanel.add(btnLamMoi);
        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(southPanel, BorderLayout.SOUTH);
    }

    private void loadData() {
        tableModel.setRowCount(0);
        String empId = currentUser.getMaNV();
        String filterMaNV = isManager && cboNhanVien.getSelectedItem() instanceof NhanVien
                ? ((NhanVien) cboNhanVien.getSelectedItem()).getMaNhanVien() : null;
        List<DonXinNghiPhep> requests = isManager
                ? leaveService.getAllRequestsByScope(empId)
                : leaveService.getMyRequests(empId);
        for (DonXinNghiPhep req : requests) {
            if (filterMaNV != null && !filterMaNV.equals(req.getMaNV())) continue;
            tableModel.addRow(new Object[]{
                req.getId(), req.getTenNhanVien(), req.getTenLoaiPhep(),
                req.getTuNgay().format(DATE_FORMAT), req.getDenNgay().format(DATE_FORMAT),
                req.getSoNgayNghi(), req.getLyDo(),
                req.getTrangThai().getTenHienThi(),
                req.getTenNguoiDuyet() != null ? req.getTenNguoiDuyet() : "-"
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
        sorter.setRowFilter("Tất cả".equals(s) ? null : RowFilter.regexFilter("^" + s + "$", 7));
    }

    private void updateBalanceDisplay(String empId) {
        balancePanel.removeAll();
        List<SoDungPhep> balances = leaveService.getBalances(empId);
        for (SoDungPhep balance : balances) {
            JLabel lbl = new JLabel(getLeaveTypeName(balance.getMaLoaiPhep()) +
                    ": " + balance.getSoNgayConLai() + "/" + balance.getSoNgayDuocCap() + " ngay");
            lbl.setFont(com.hrm.util.UIFonts.BOLD_SMALL);
            if (balance.getSoNgayConLai() <= 3) {
                lbl.setForeground(com.hrm.util.UIColors.DANGER_RED);
            }
            balancePanel.add(lbl);
        }
        balancePanel.revalidate();
        balancePanel.repaint();
    }

    private String getLeaveTypeName(String code) {
        return leaveService.getAllLeaveTypes().stream()
                .filter(t -> t.getId().equals(code))
                    .map(t -> t.getTenLoaiPhep())
                .findFirst()
                .orElse(code);
    }

    private void createRequest() {
        if (currentUser.getMaNV() == null) {
            String message = SessionContext.getInstance().hasRole(HRMConstants.ROLE_ADMIN)
                    ? "Tài khoản admin không cần tạo yêu cầu nghỉ phép."
                   : "Tài khoản của bạn chưa gắn mã nhân viên nên không thể tạo đơn nghỉ phép.";
            JOptionPane.showMessageDialog(this,
                    message,
                    "Thông báo",
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
                    "Vui lòng chọn đơn cần duyệt",
                    "Thông báo",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = table.convertRowIndexToModel(selectedRow);
        int requestId = (int) tableModel.getValueAt(modelRow, 0);
        String status = (String) tableModel.getValueAt(modelRow, 7);
        if (!DonXinNghiPhep.TrangThai.CHO_DUYET.getTenHienThi().equals(status)) {
            JOptionPane.showMessageDialog(this,
                    "Chỉ có thể duyệt đơn đang chờ duyệt",
                    "Thông báo",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        LeaveApproveDialog dialog = new LeaveApproveDialog(
                (Frame) SwingUtilities.getWindowAncestor(this), requestId);
        dialog.setVisible(true);
        loadData();
    }

}
