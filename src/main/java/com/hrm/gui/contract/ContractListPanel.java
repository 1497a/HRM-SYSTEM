package com.hrm.gui.contract;

import com.hrm.bus.HopDongBUS;
import com.hrm.bus.XacThucBUS;
import com.hrm.gui.components.PurpleTable;
import com.hrm.util.UIHelper;
import com.hrm.model.DataScope;
import com.hrm.model.HopDongLaoDong;
import com.hrm.model.NhanVien;
import com.hrm.model.TaiKhoan;
import com.hrm.util.HRMConstants;
import com.hrm.util.PermissionCodes;
import com.hrm.util.SessionContext;
import com.hrm.util.UIColors;
import com.hrm.util.UIFonts;

import javax.swing.BorderFactory;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;

import javax.swing.SwingUtilities;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Panel danh sach hop dong lao dong.
 */
public class ContractListPanel extends JPanel {

    private static final int COL_MA_HOP_DONG = 0;
    private static final int COL_MA_NV = 2;
    private static final int COL_TRANG_THAI = 8;
    private static final String LABEL_HET_HIEU_LUC = "Hết hiệu lực";
    private static final Map<String, String> TRANG_THAI_OPTIONS = new LinkedHashMap<>();
    private static final String[] COL_NAMES = {
        "Mã HĐ", "Số HĐ", "Mã NV", "Loại HĐ", "Lương cơ sở",
        "Ngày ký", "Ngày hiệu lực", "Ngày hết hiệu lực", "Trạng thái"
    };

    static {
        TRANG_THAI_OPTIONS.put("Chờ duyệt", HRMConstants.TRANG_THAI_CHO_DUYET);
        TRANG_THAI_OPTIONS.put("Hiệu lực", HRMConstants.TRANG_THAI_HIEU_LUC);
        TRANG_THAI_OPTIONS.put(LABEL_HET_HIEU_LUC, HRMConstants.TRANG_THAI_HET_HAN);
        TRANG_THAI_OPTIONS.put("Thanh lý", HRMConstants.TRANG_THAI_THANH_LY);
    }

    private final HopDongBUS hopDongService = HopDongBUS.getInstance();
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private PurpleTable table;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    private JComboBox<String> cboTrangThai;
    private JComboBox<Object> cboNhanVien;
    private JButton btnTao;
    private JButton btnXemChiTiet;
    private JTextField txtTimKiem;
    private List<HopDongLaoDong> danhSachHienThi = new ArrayList<>();

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

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        filterPanel.setOpaque(false);

        txtTimKiem = UIHelper.createSearchField("Tìm theo mã HĐ, số HĐ, mã NV...");
        filterPanel.add(new JLabel("Tìm kiếm:"));
        filterPanel.add(txtTimKiem);

        JLabel lblTrangThai = new JLabel("Trạng thái:");
        lblTrangThai.setFont(UIFonts.TEXT_NORMAL);
        lblTrangThai.setForeground(UIColors.TEXT_DARK);

        cboTrangThai = new JComboBox<>();
        cboTrangThai.addItem(HRMConstants.ALL);
        TRANG_THAI_OPTIONS.keySet().forEach(cboTrangThai::addItem);
        cboTrangThai.setFont(UIFonts.TEXT_NORMAL);
        cboTrangThai.setPreferredSize(new Dimension(160, 32));

        JLabel lblNhanVien = new JLabel("Nhân viên:");
        lblNhanVien.setFont(UIFonts.TEXT_NORMAL);
        lblNhanVien.setForeground(UIColors.TEXT_DARK);

        cboNhanVien = new JComboBox<>();
        cboNhanVien.addItem(HRMConstants.ALL);
        cboNhanVien.setFont(UIFonts.TEXT_NORMAL);

        DataScope contractScope = XacThucBUS.getInstance().getScopeForAction(PermissionCodes.CONTRACT_VIEW);
        boolean isManager = contractScope == DataScope.ALL
                || contractScope == DataScope.DEPT
                || contractScope == DataScope.TEAM;
        if (isManager) {
            TaiKhoan currentUser = SessionContext.getInstance().getCurrentUser();
            List<NhanVien> dsNV = com.hrm.bus.NhanVienBUS.getInstance()
                    .getAllByActionScope(PermissionCodes.EMPLOYEE_VIEW, currentUser != null ? currentUser.getMaNV() : null);
            for (NhanVien nv : dsNV) {
                cboNhanVien.addItem(nv);
            }
        }

        cboNhanVien.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,                        int index, boolean isSelected, boolean cellHasFocus) {
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

        if (isManager) {
            filterPanel.add(lblNhanVien);
            filterPanel.add(cboNhanVien);
        }
        filterPanel.add(lblTrangThai);
        filterPanel.add(cboTrangThai);
        JLabel lblHint = new JLabel("Tìm theo: Mã HĐ / Số HĐ / Mã NV");
        lblHint.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblHint.setForeground(UIColors.TEXT_DARK);
        panel.add(filterPanel, BorderLayout.NORTH);
        panel.add(lblHint, BorderLayout.SOUTH);
        return panel;
    }

    private JScrollPane buildCenterPanel() {
        tableModel = PurpleTable.createNonEditableModel(COL_NAMES);
        table = new PurpleTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(COL_TRANG_THAI).setCellRenderer(new com.hrm.gui.components.StatusCellRenderer());

        int[] widths = {60, 120, 70, 130, 120, 100, 100, 120, 110};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);
        sorter.setComparator(COL_MA_HOP_DONG, Comparator.comparingInt(a -> (Integer) a));

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        return scroll;
    }

    private JPanel buildSouthPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        panel.setOpaque(false);

        btnTao = UIHelper.createSuccessButton("+ Tạo hợp đồng");
        btnXemChiTiet = UIHelper.createPrimaryButton("Xem chi tiết");
        btnXemChiTiet.setEnabled(false);

        JButton btnLamMoi = UIHelper.createDefaultButton("Làm mới");
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
        txtTimKiem.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override public void keyReleased(java.awt.event.KeyEvent e) { applyFilter(); }
        });
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
            Set<String> maNVSet = com.hrm.bus.NhanVienBUS.getInstance()
                    .getAllByActionScope(PermissionCodes.CONTRACT_VIEW, myMaNV).stream()
                    .map(NhanVien::getMaNhanVien)
                    .collect(Collectors.toSet());
            danhSachHienThi = hopDongService.getAll().stream()
                    .filter(hd -> maNVSet.contains(hd.getMaNV()))
                    .collect(Collectors.toList());
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
                    getStatusDisplay(hd.getTrangThai())
            });
        }
        applyFilter();
        updateDetailButtonState();
    }

    private void applyFilter() {
        String trangThaiLabel = (String) cboTrangThai.getSelectedItem();
        String trangThaiFilter = TRANG_THAI_OPTIONS.get(trangThaiLabel);

        String tempMaNV = null;
        Object selectedNhanVien = cboNhanVien != null ? cboNhanVien.getSelectedItem() : null;
        if (selectedNhanVien instanceof NhanVien) {
            tempMaNV = ((NhanVien) selectedNhanVien).getMaNhanVien();
        }
        final String filterMaNV = tempMaNV;
        final String keyword = UIHelper.normalizeSearch(txtTimKiem != null ? txtTimKiem.getText() : "");

        RowFilter<DefaultTableModel, Object> rf = new RowFilter<>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ? extends Object> entry) {
                if (filterMaNV != null) {
                    Object nvIdObj = entry.getValue(COL_MA_NV);
                    if (nvIdObj != null && !nvIdObj.toString().equals(filterMaNV)) {
                        return false;
                    }
                }

                if (trangThaiFilter != null) {
                    HopDongLaoDong hopDong = getHopDongFromEntry(entry);
                    if (hopDong == null || !matchesTrangThaiFilter(hopDong.getTrangThai(), trangThaiFilter)) {
                        return false;
                    }
                }

                if (!keyword.isEmpty()) {
                    String maHD = UIHelper.normalizeSearch(entry.getValue(COL_MA_HOP_DONG) != null ? entry.getValue(COL_MA_HOP_DONG).toString() : "");
                    String soHD = UIHelper.normalizeSearch(entry.getValue(1) != null ? entry.getValue(1).toString() : "");
                    String maNV = UIHelper.normalizeSearch(entry.getValue(COL_MA_NV) != null ? entry.getValue(COL_MA_NV).toString() : "");
                    if (!maHD.contains(keyword) && !soHD.contains(keyword) && !maNV.contains(keyword)) {
                        return false;
                    }
                }

                return true;
            }
        };
        sorter.setRowFilter(rf);
        updateDetailButtonState();
    }

    private HopDongLaoDong getHopDongFromEntry(RowFilter.Entry<? extends DefaultTableModel, ? extends Object> entry) {
        Object idValue = entry.getValue(COL_MA_HOP_DONG);
        if (!(idValue instanceof Integer)) {
            return null;
        }
        int maHopDong = (Integer) idValue;
        for (HopDongLaoDong hd : danhSachHienThi) {
            if (hd.getMaHopDong() == maHopDong) {
                return hd;
            }
        }
        return null;
    }

    private boolean matchesTrangThaiFilter(String rawTrangThai, String filterTrangThai) {
        if (HRMConstants.TRANG_THAI_HET_HAN.equals(filterTrangThai)) {
            return HRMConstants.TRANG_THAI_HET_HAN.equals(rawTrangThai)
                    || HRMConstants.TRANG_THAI_HET_HIEU_LUC.equals(rawTrangThai);
        }
        return filterTrangThai.equals(rawTrangThai);
    }

    private String getStatusDisplay(String rawTrangThai) {
        if (HRMConstants.TRANG_THAI_HET_HAN.equals(rawTrangThai)
                || HRMConstants.TRANG_THAI_HET_HIEU_LUC.equals(rawTrangThai)) {
            return LABEL_HET_HIEU_LUC;
        }
        return HRMConstants.display(rawTrangThai);
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
        if (modelRow < 0 || modelRow >= tableModel.getRowCount()) {
            return null;
        }
        Object maHopDongValue = tableModel.getValueAt(modelRow, COL_MA_HOP_DONG);
        if (!(maHopDongValue instanceof Integer)) {
            return null;
        }
        int maHopDong = (Integer) maHopDongValue;
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

}
