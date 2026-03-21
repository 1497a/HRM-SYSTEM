package com.hrm.gui.contract;

import com.hrm.bus.HopDongBUS;
import com.hrm.bus.XacThucBUS;
import com.hrm.gui.components.PurpleButton;
import com.hrm.gui.components.PurpleTable;
import com.hrm.model.DataScope;
import com.hrm.model.HopDongLaoDong;
import com.hrm.model.TaiKhoan;
import com.hrm.util.HRMConstants;
import com.hrm.util.PermissionCodes;
import com.hrm.util.UIFonts;
import com.hrm.util.SessionContext;
import com.hrm.util.UIColors;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import java.awt.*;
import java.awt.Font;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Panel danh sách hợp đồng lao động.
 */
public class ContractListPanel extends JPanel {

    private final HopDongBUS hopDongService = HopDongBUS.getInstance();
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private PurpleTable table;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    private JComboBox<String> cboTrangThai;
    private JComboBox<Object> cboNhanVien;
    private PurpleButton btnTao;
    private PurpleButton btnXemChiTiet;
    private List<HopDongLaoDong> danhSachHienThi = new ArrayList<>();
    private static final String[] COL_NAMES = {
        "Mã HĐ", "Số HĐ", "Mã NV", "Loại HĐ", "Lương cơ sở",
        "Ngày ký", "Ngày hiệu lực", "Ngày hết hiệu lực", "Trạng thái"
    };
    public ContractListPanel() {
        setLayout(new BorderLayout(0, 8));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        add(buildNorthPanel(), BorderLayout.NORTH);
        add(buildCenterPanel(), BorderLayout.CENTER);
        add(buildSouthPanel(), BorderLayout.SOUTH);
        setupPermissions();
        setupEvents();
        loadData();
    }

    private JPanel buildNorthPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setOpaque(false);
        JLabel lblTitle = new JLabel("QUẢN LÝ HỢP ĐỒNG LAO ĐỘNG");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(UIColors.PRIMARY_PURPLE);
        lblTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        panel.add(lblTitle, BorderLayout.NORTH);
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        filterPanel.setOpaque(false);
        JLabel lblTrangThai = new JLabel("Trạng thái:");
        lblTrangThai.setFont(UIFonts.TEXT_NORMAL);
        lblTrangThai.setForeground(UIColors.TEXT_DARK);
        cboTrangThai = new JComboBox<>(new String[]{
            "Tất cả", "Chờ phê duyệt", "Hiệu lực", "Hết hiệu lực", "Thanh lý"
        });
        cboTrangThai.setFont(UIFonts.TEXT_NORMAL);
        cboTrangThai.setPreferredSize(new Dimension(160, 32));
        JLabel lblNhanVien = new JLabel("Nhân viên:");
        lblNhanVien.setFont(UIFonts.TEXT_NORMAL);
        lblNhanVien.setForeground(UIColors.TEXT_DARK);
        cboNhanVien = new JComboBox<>();
        cboNhanVien.setFont(UIFonts.TEXT_NORMAL);
        cboNhanVien.addItem("Tất cả");
        DataScope contractScope = XacThucBUS.getInstance().getScopeForAction(PermissionCodes.CONTRACT_VIEW);
        boolean isManager = contractScope == DataScope.ALL
                || contractScope == DataScope.DEPT
                || contractScope == DataScope.TEAM;
        if (isManager) {
            com.hrm.model.TaiKhoan currentUser = SessionContext.getInstance().getCurrentUser();
            List<com.hrm.model.NhanVien> dsNV = com.hrm.bus.NhanVienBUS.getInstance()
                    .getAllByActionScope(PermissionCodes.EMPLOYEE_VIEW, currentUser != null ? currentUser.getMaNV() : null);
            for (com.hrm.model.NhanVien nv : dsNV) {
                cboNhanVien.addItem(nv);
            }
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
        if (isManager) {
            filterPanel.add(lblNhanVien);
            filterPanel.add(cboNhanVien);
        }
        filterPanel.add(lblTrangThai);
        filterPanel.add(cboTrangThai);
        panel.add(filterPanel, BorderLayout.CENTER);
        return panel;
    }

    private JScrollPane buildCenterPanel() {
        tableModel = PurpleTable.createNonEditableModel(COL_NAMES);
        table = new PurpleTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setDefaultRenderer(Object.class, new StatusColorRenderer());
        int[] widths = {60, 120, 70, 130, 120, 100, 100, 120, 110};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);
        sorter.setComparator(0, Comparator.comparingInt(a -> (Integer) a));
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        return scroll;
    }

    private JPanel buildSouthPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        panel.setOpaque(false);
        btnTao = new PurpleButton("+ Tạo hợp đồng");
        btnXemChiTiet = new PurpleButton("Xem chi tiết");
        btnXemChiTiet.setEnabled(false);
        JButton btnLamMoi = new JButton("Làm mới");
        btnLamMoi.addActionListener(e -> loadData());
        panel.add(btnTao);
        panel.add(btnXemChiTiet);
        panel.add(btnLamMoi);
        return panel;
    }

    private void setupEvents() {
        btnTao.addActionListener(e -> showCreateDialog());
        btnXemChiTiet.addActionListener(e -> showDetailDialog());
        cboTrangThai.addActionListener(e -> applyFilter());
        cboNhanVien.addActionListener(e -> applyFilter());
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateDetailButtonState();
            }
        });
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    showDetailDialog();
                }
            }
        });
    }

    private void setupPermissions() {
        SessionContext sc = SessionContext.getInstance();
        boolean canCreate = sc.hasPermission(PermissionCodes.CONTRACT_CREATE);
        btnTao.setVisible(canCreate);
    }

    public void loadData() {
        DataScope scope = XacThucBUS.getInstance().getScopeForAction(PermissionCodes.CONTRACT_VIEW);
        TaiKhoan user = SessionContext.getInstance().getCurrentUser();
        String myMaNV = user != null ? user.getMaNV() : null;
        if (scope == DataScope.SELF) {
            danhSachHienThi = (myMaNV != null && !myMaNV.isEmpty())
                    ? hopDongService.getByMaNV(myMaNV)
                    : new ArrayList<>();
        } else if (scope == DataScope.ALL) {
            danhSachHienThi = hopDongService.getAll();
        } else {
            java.util.Set<String> maNVSet = com.hrm.bus.NhanVienBUS.getInstance()
                    .getAllByActionScope(PermissionCodes.CONTRACT_VIEW, myMaNV).stream()
                    .map(com.hrm.model.NhanVien::getMaNhanVien)
                    .collect(java.util.stream.Collectors.toSet());
            danhSachHienThi = hopDongService.getAll().stream()
                    .filter(hd -> maNVSet.contains(hd.getMaNV()))
                    .collect(java.util.stream.Collectors.toList());
        }
        tableModel.setRowCount(0);
        for (HopDongLaoDong hd : danhSachHienThi) {
            tableModel.addRow(new Object[]{
                    hd.getMaHopDong(),
                    hd.getSoHopDong(),
                    hd.getMaNV(),
                    HRMConstants.display(hd.getLoaiHopDong()),
                    String.format("%,d đ", hd.getLuongCoSo()),
                    hd.getNgayKy() != null ? hd.getNgayKy().format(dtf) : "",
                    hd.getNgayHieuLuc() != null ? hd.getNgayHieuLuc().format(dtf) : "",
                    hd.getNgayHetHieuLuc() != null ? hd.getNgayHetHieuLuc().format(dtf) : "Không xác định",
                    HRMConstants.display(hd.getTrangThai())
            });
        }
        applyFilter();
        updateDetailButtonState();
    }

    private void applyFilter() {
        String trangThaiFilter = (String) cboTrangThai.getSelectedItem();
        String tempMaNV = null;
        if (cboNhanVien != null && cboNhanVien.getSelectedItem() instanceof com.hrm.model.NhanVien) {
            tempMaNV = ((com.hrm.model.NhanVien) cboNhanVien.getSelectedItem()).getMaNhanVien();
        }
        final String filterMaNV = tempMaNV;
        RowFilter<DefaultTableModel, Object> rf = new RowFilter<DefaultTableModel, Object>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ? extends Object> entry) {
                if (filterMaNV != null) {
                    Object nvIdObj = entry.getValue(2);
                    if (nvIdObj != null && !nvIdObj.toString().equals(filterMaNV)) {
                        return false;
                    }
                }
                if (!"Tất cả".equals(trangThaiFilter)) {
                    String trangThai = entry.getStringValue(8);
                    if (!trangThaiFilter.equals(trangThai)) {
                        return false;
                    }
                }
                return true;
            }
        };
        sorter.setRowFilter(rf);
        updateDetailButtonState();
    }

    private void showCreateDialog() {
        ContractFormDialog dialog = new ContractFormDialog(
                (Frame) SwingUtilities.getWindowAncestor(this));
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            loadData();
        }
    }

    private void showDetailDialog() {
        HopDongLaoDong selected = getSelectedHopDong();
        if (selected == null) {
            return;
        }
        ContractFormDialog dialog = new ContractFormDialog(
                (Frame) SwingUtilities.getWindowAncestor(this), selected);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            loadData();
        }
    }

    private HopDongLaoDong getSelectedHopDong() {
        int viewRow = table.getSelectedRow();
        if (viewRow == -1) {
            return null;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        if (modelRow < 0 || modelRow >= danhSachHienThi.size()) {
            return null;
        }
        int maHopDong = (int) tableModel.getValueAt(modelRow, 0);
        for (HopDongLaoDong hd : danhSachHienThi) {
            if (hd.getMaHopDong() == maHopDong) {
                return hd;
            }
        }
        return null;
    }

    private void updateDetailButtonState() {
        btnXemChiTiet.setEnabled(table != null && table.getSelectedRow() != -1);
    }

    private class StatusColorRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int col) {
            Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, col);
            setHorizontalAlignment(SwingConstants.CENTER);
            setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
            setToolTipText(null);
            if (!isSelected) {
                c.setBackground(row % 2 == 0 ? Color.WHITE : UIColors.TABLE_ROW_ALT);
                c.setForeground(UIColors.TEXT_DARK);
                if (col == 8 && value != null) {
                    String val = value.toString();
                    if ("Chờ phê duyệt".equals(val)) {
                        c.setForeground(UIColors.WARNING_ORANGE);
                        ((JLabel) c).setFont(com.hrm.util.UIFonts.BOLD_SMALL);
                        setToolTipText("Hợp đồng đang chờ phê duyệt");
                    } else if ("Hiệu lực".equals(val)) {
                        c.setForeground(UIColors.SUCCESS_GREEN);
                        ((JLabel) c).setFont(com.hrm.util.UIFonts.BOLD_SMALL);
                        setToolTipText("Hợp đồng đang có hiệu lực");
                    } else if ("Hết hạn".equals(val) || "Hết hiệu lực".equals(val)) {
                        c.setForeground(UIColors.WARNING_ORANGE);
                        ((JLabel) c).setFont(com.hrm.util.UIFonts.BOLD_SMALL);
                        setToolTipText("Hợp đồng đã hết thời hạn");
                    } else if ("Thanh lý".equals(val)) {
                        c.setForeground(UIColors.DANGER_RED);
                        ((JLabel) c).setFont(com.hrm.util.UIFonts.BOLD_SMALL);
                        setToolTipText("Hợp đồng đã được thanh lý");
                    }
                }
            }
            return c;
        }
    }

}
