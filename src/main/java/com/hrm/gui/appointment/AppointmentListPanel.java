package com.hrm.gui.appointment;

import com.hrm.bus.BoNhiemBUS;
import com.hrm.gui.components.PurpleButton;
import com.hrm.gui.components.PurpleTable;
import com.hrm.model.BoNhiem;
import com.hrm.util.HRMConstants;
import com.hrm.util.PermissionCodes;
import com.hrm.util.UIFonts;
import com.hrm.util.SessionContext;
import com.hrm.util.UIColors;
import com.hrm.util.UIHelper;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.text.Normalizer;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AppointmentListPanel extends JPanel {

    private static final Map<String, String> TRANG_THAI_OPTIONS = new LinkedHashMap<>();
    private static final Map<String, String> LOAI_OPTIONS = new LinkedHashMap<>();
    static {
        TRANG_THAI_OPTIONS.put("Chờ duyệt", HRMConstants.TRANG_THAI_CHO_DUYET);
        TRANG_THAI_OPTIONS.put("Hiệu lực", HRMConstants.TRANG_THAI_HIEU_LUC);
        TRANG_THAI_OPTIONS.put("Hết hiệu lực", HRMConstants.TRANG_THAI_HET_HIEU_LUC);
        TRANG_THAI_OPTIONS.put("Từ chối", HRMConstants.TRANG_THAI_TU_CHOI);
        LOAI_OPTIONS.put("Chính", "chinh");
        LOAI_OPTIONS.put("Kiêm nhiệm", "kiem_nhiem");
    }

    private static final int COL_MA_BN = 0;
    private static final int COL_MA_NV = 1;
    private static final int COL_HO_TEN = 2;
    private static final int COL_PHONG_BAN = 3;
    private static final int COL_CHUC_VU = 4;
    private static final int COL_LOAI = 5;
    private static final int COL_TU_NGAY = 6;
    private static final int COL_TRANG_THAI = 7;
    private static final String[] COL_NAMES = {
        "Mã BN", "Mã NV", "Họ tên", "Phòng ban", "Chức vụ", "Loại", "Từ ngày", "Trạng thái"
    };
    private final BoNhiemBUS boNhiemService = BoNhiemBUS.getInstance();
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private PurpleTable table;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    private JComboBox<String> cboTrangThai;
    private JComboBox<String> cboLoai;
    private JTextField txtTimKiem;
    private PurpleButton btnTao;
    private PurpleButton btnXemChiTiet;
    private List<BoNhiem> danhSachHienThi = new ArrayList<>();
    public AppointmentListPanel() {
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
        JLabel lblTrangThai = new JLabel("Trạng thái:");
        lblTrangThai.setFont(UIFonts.TEXT_NORMAL);
        lblTrangThai.setForeground(UIColors.TEXT_DARK);
        cboTrangThai = new JComboBox<>();
        cboTrangThai.addItem(HRMConstants.ALL);
        TRANG_THAI_OPTIONS.keySet().forEach(cboTrangThai::addItem);
        cboTrangThai.setFont(UIFonts.TEXT_NORMAL);
        cboTrangThai.setPreferredSize(new Dimension(160, 32));
        JLabel lblLoai = new JLabel("Loại:");
        lblLoai.setFont(UIFonts.TEXT_NORMAL);
        lblLoai.setForeground(UIColors.TEXT_DARK);
        cboLoai = new JComboBox<>();
        cboLoai.addItem(HRMConstants.ALL);
        LOAI_OPTIONS.keySet().forEach(cboLoai::addItem);
        cboLoai.setFont(UIFonts.TEXT_NORMAL);
        cboLoai.setPreferredSize(new Dimension(140, 32));
        JLabel lblTimKiem = new JLabel("Tìm kiếm:");
        lblTimKiem.setFont(UIFonts.TEXT_NORMAL);
        lblTimKiem.setForeground(UIColors.TEXT_DARK);
        txtTimKiem = new JTextField(20);
        txtTimKiem.setFont(UIFonts.TEXT_NORMAL);
        txtTimKiem.setPreferredSize(new Dimension(200, 32));
        filterPanel.add(lblTrangThai);
        filterPanel.add(cboTrangThai);
        filterPanel.add(Box.createHorizontalStrut(16));
        filterPanel.add(lblLoai);
        filterPanel.add(cboLoai);
        filterPanel.add(Box.createHorizontalStrut(16));
        filterPanel.add(lblTimKiem);
        filterPanel.add(txtTimKiem);
        JLabel lblHint = new JLabel("Có thể tìm theo mã NV, tên nhân viên hoặc phòng ban");
        lblHint.setFont(UIFonts.TEXT_NORMAL);
        lblHint.setForeground(Color.GRAY);
        lblHint.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));
        panel.add(filterPanel, BorderLayout.CENTER);
        panel.add(lblHint, BorderLayout.SOUTH);
        return panel;
    }

    private JScrollPane buildCenterPanel() {
        tableModel = new DefaultTableModel(COL_NAMES, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == COL_MA_BN) {
                    return Integer.class;
                }
                return String.class;
            }
        };
        table = new PurpleTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(COL_TRANG_THAI).setCellRenderer(new com.hrm.gui.components.StatusCellRenderer());
        int[] widths = {70, 80, 180, 160, 160, 110, 100, 110};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);
        for (int i = 0; i < COL_NAMES.length; i++) {
            sorter.setSortable(i, false);
        }
        sorter.setSortable(COL_MA_BN, true);
        sorter.setSortable(COL_MA_NV, true);
        sorter.setSortable(COL_HO_TEN, true);
        sorter.setSortable(COL_TU_NGAY, true);
        sorter.setComparator(COL_MA_BN, (a, b) -> Integer.compare((Integer) a, (Integer) b));
        sorter.setComparator(COL_HO_TEN, UIHelper.vietnameseNameComparator());
        sorter.setSortKeys(List.of(new RowSorter.SortKey(COL_MA_BN, SortOrder.DESCENDING)));
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        return scroll;
    }

    private JPanel buildSouthPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        panel.setOpaque(false);
        btnTao = new PurpleButton("+ Tạo bổ nhiệm");
        panel.add(btnTao);
        btnXemChiTiet = new PurpleButton("Xem chi tiết");
        btnXemChiTiet.setEnabled(false);
        panel.add(btnXemChiTiet);
        JButton btnLamMoi = new JButton("Làm mới");
        btnLamMoi.addActionListener(e -> loadData());
        panel.add(btnLamMoi);
        return panel;
    }

    private void setupEvents() {
        btnTao.addActionListener(e -> showCreateDialog());
        btnXemChiTiet.addActionListener(e -> showDetailDialog());
        cboTrangThai.addActionListener(e -> applyFilter());
        cboLoai.addActionListener(e -> applyFilter());
        txtTimKiem.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                applyFilter();
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                applyFilter();
            }
            @Override
            public void changedUpdate(DocumentEvent e) {
                applyFilter();
            }
        });
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                btnXemChiTiet.setEnabled(table.getSelectedRow() != -1);
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
        btnTao.setVisible(SessionContext.getInstance().hasPermission(PermissionCodes.APPOINTMENT_CREATE));
    }

    public void loadData() {
        com.hrm.model.TaiKhoan currentUser = SessionContext.getInstance().getCurrentUser();
        String currentMaNV = currentUser != null ? currentUser.getMaNV() : null;
        danhSachHienThi = boNhiemService.getAllByScope(currentMaNV);
        tableModel.setRowCount(0);
        for (BoNhiem bn : danhSachHienThi) {
            tableModel.addRow(new Object[]{
                bn.getId(),
                    bn.getMaNV(),
                bn.getTenNV() != null ? bn.getTenNV() : "",
                bn.getTenPhongBan() != null ? bn.getTenPhongBan() : bn.getMaPhongBan(),
                bn.getTenChucVu() != null ? bn.getTenChucVu() : bn.getMaChucVu(),
                HRMConstants.display(bn.getLoaiBoNhiem()),
                bn.getTuNgay() != null ? bn.getTuNgay().format(dtf) : "",
                HRMConstants.display(bn.getTrangThai())
            });
        }
        applyFilter();
    }

    private void applyFilter() {
        String trangThaiFilter = (String) cboTrangThai.getSelectedItem();
        String loaiFilterDisplay = (String) cboLoai.getSelectedItem();
        String keyword = normalize(txtTimKiem != null ? txtTimKiem.getText() : "");
        RowFilter<DefaultTableModel, Object> rf = new RowFilter<DefaultTableModel, Object>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ? extends Object> entry) {
                int modelRow = (Integer) entry.getIdentifier();
                if (modelRow < 0 || modelRow >= danhSachHienThi.size()) {
                    return false;
                }
                BoNhiem bn = danhSachHienThi.get(modelRow);
                if (!keyword.isEmpty()) {
                    String tenNV = normalize(bn.getTenNV());
                    String pb = normalize(bn.getTenPhongBan());
                    String maNV = normalize(bn.getMaNV());
                    if (!tenNV.contains(keyword) && !pb.contains(keyword) && !maNV.contains(keyword)) {
                        return false;
                    }
                }
                if (!HRMConstants.ALL.equals(loaiFilterDisplay)) {
                    String expected = LOAI_OPTIONS.get(loaiFilterDisplay);
                    if (!bn.getLoaiBoNhiem().equals(expected)) return false;
                }
                if (!HRMConstants.ALL.equals(trangThaiFilter)) {
                    String expected = TRANG_THAI_OPTIONS.get(trangThaiFilter);
                    if (!bn.getTrangThai().equals(expected)) return false;
                }
                return true;
            }
        };
        sorter.setRowFilter(rf);
    }

    private void showCreateDialog() {
        AppointmentFormDialog dialog = new AppointmentFormDialog(
            (Frame) SwingUtilities.getWindowAncestor(this)
        );
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            loadData();
        }
    }

    private void showDetailDialog() {
        BoNhiem selected = getSelectedBoNhiem();
        if (selected == null) {
            return;
        }
        AppointmentFormDialog dialog = new AppointmentFormDialog(
            (Frame) SwingUtilities.getWindowAncestor(this), selected
        );
        dialog.setVisible(true);
        if (dialog.isSaved() || dialog.isActionTaken()) {
            loadData();
        }
    }

    private BoNhiem getSelectedBoNhiem() {
        int viewRow = table.getSelectedRow();
        if (viewRow == -1) {
            return null;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        if (modelRow < 0 || modelRow >= danhSachHienThi.size()) {
            return null;
        }
        int maBoNhiem = (int) tableModel.getValueAt(modelRow, COL_MA_BN);
        for (BoNhiem bn : danhSachHienThi) {
            if (bn.getId() == maBoNhiem) {
                return bn;
            }
        }
        return null;
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
            .replaceAll("\\p{M}+", "")
            .replace("đ", "d")
            .replace("Đ", "D");
        return normalized.trim().toLowerCase();
    }
}
