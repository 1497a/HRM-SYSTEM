package com.hrm.gui.appointment;

import com.hrm.bus.BoNhiemBUS;
import com.hrm.gui.components.PurpleButton;
import com.hrm.gui.components.PurpleTable;
import com.hrm.model.BoNhiem;
import com.hrm.util.PermissionCodes;
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
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.text.Normalizer;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class AppointmentListPanel extends JPanel {

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
        setBackground(UIColors.LIGHT_GRAY_BG);
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

        JLabel lblTitle = new JLabel("QUẢN LÝ BỔ NHIỆM");
        lblTitle.setFont(com.hrm.util.UIFonts.HEADER_H2);
        lblTitle.setForeground(UIColors.PRIMARY_PURPLE);
        lblTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        panel.add(lblTitle, BorderLayout.NORTH);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        filterPanel.setOpaque(false);

        JLabel lblTrangThai = new JLabel("Trạng thái:");
        lblTrangThai.setFont(com.hrm.util.UIFonts.TEXT_MEDIUM);
        lblTrangThai.setForeground(UIColors.TEXT_DARK);

        cboTrangThai = new JComboBox<>(new String[]{
            "Tất cả", "Chờ duyệt", "Hiệu lực", "Kết thúc", "Từ chối"
        });
        cboTrangThai.setFont(com.hrm.util.UIFonts.TEXT_MEDIUM);
        cboTrangThai.setPreferredSize(new Dimension(160, 32));

        JLabel lblLoai = new JLabel("Loại:");
        lblLoai.setFont(com.hrm.util.UIFonts.TEXT_MEDIUM);
        lblLoai.setForeground(UIColors.TEXT_DARK);

        cboLoai = new JComboBox<>(new String[]{
            "Tất cả", "Chính", "Kiêm nhiệm"
        });
        cboLoai.setFont(com.hrm.util.UIFonts.TEXT_MEDIUM);
        cboLoai.setPreferredSize(new Dimension(140, 32));

        JLabel lblTimKiem = new JLabel("Tìm kiếm:");
        lblTimKiem.setFont(com.hrm.util.UIFonts.TEXT_MEDIUM);
        lblTimKiem.setForeground(UIColors.TEXT_DARK);

        txtTimKiem = new JTextField(20);
        txtTimKiem.setFont(com.hrm.util.UIFonts.TEXT_MEDIUM);
        txtTimKiem.setPreferredSize(new Dimension(200, 32));
        txtTimKiem.setToolTipText("Có thể tìm theo mã NV, tên nhân viên hoặc phòng ban");

        filterPanel.add(lblTrangThai);
        filterPanel.add(cboTrangThai);
        filterPanel.add(Box.createHorizontalStrut(16));
        filterPanel.add(lblLoai);
        filterPanel.add(cboLoai);
        filterPanel.add(Box.createHorizontalStrut(16));
        filterPanel.add(lblTimKiem);
        filterPanel.add(txtTimKiem);

        panel.add(filterPanel, BorderLayout.CENTER);
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
        table.setDefaultRenderer(Object.class, new StatusColorRenderer());

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
        scroll.setBorder(BorderFactory.createLineBorder(UIColors.BORDER_GRAY));
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
        String currentMaNV = currentUser != null ? currentUser.getNhanVienId() : null;
        danhSachHienThi = boNhiemService.getAllByScope(currentMaNV);
        tableModel.setRowCount(0);

        for (BoNhiem bn : danhSachHienThi) {
            tableModel.addRow(new Object[]{
                bn.getMaBoNhiem(),
                bn.getMaNhanVien() != null ? bn.getMaNhanVien() : String.valueOf(bn.getMaNV()),
                bn.getTenNV() != null ? bn.getTenNV() : "",
                bn.getTenPhongBan() != null ? bn.getTenPhongBan() : bn.getPhongBanId(),
                bn.getTenChucVu() != null ? bn.getTenChucVu() : bn.getChucVuId(),
                bn.getLoaiBoNhiemDisplay(),
                bn.getTuNgay() != null ? bn.getTuNgay().format(dtf) : "",
                bn.getTrangThaiDisplay()
            });
        }

        applyFilter();
    }

    private void applyFilter() {
        String trangThaiFilter = (String) cboTrangThai.getSelectedItem();
        String loaiFilter = normalize((String) cboLoai.getSelectedItem());
        String keyword = normalize(txtTimKiem != null ? txtTimKiem.getText() : "");

        RowFilter<DefaultTableModel, Object> rf = new RowFilter<DefaultTableModel, Object>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ? extends Object> entry) {
                int modelRow = (Integer) entry.getIdentifier();
                if (modelRow < 0 || modelRow >= danhSachHienThi.size()) {
                    return false;
                }
                BoNhiem boNhiem = danhSachHienThi.get(modelRow);

                if (!keyword.isEmpty()) {
                    String tenNV = normalize(boNhiem.getTenNV());
                    String phongBan = normalize(boNhiem.getTenPhongBan());
                    String maNV = normalize(boNhiem.getMaNhanVien() != null ? boNhiem.getMaNhanVien() : boNhiem.getMaNV());
                    boolean matched = tenNV.contains(keyword)
                        || phongBan.contains(keyword)
                        || maNV.contains(keyword);
                    if (!matched) {
                        return false;
                    }
                }

                if (!"tat ca".equals(loaiFilter)) {
                    String loai = normalize(boNhiem.getLoaiBoNhiemDisplay());
                    String loaiRaw = normalize(boNhiem.getLoaiBoNhiem());
                    if ("chinh".equals(loaiFilter) && !("chinh".equals(loai) || "chinh".equals(loaiRaw))) {
                        return false;
                    }
                    if ("kiem nhiem".equals(loaiFilter) && !("kiem nhiem".equals(loai) || "kiem_nhiem".equals(loaiRaw))) {
                        return false;
                    }
                }

                if (!"Tất cả".equals(trangThaiFilter)) {
                    String trangThai = entry.getStringValue(COL_TRANG_THAI);
                    if ("Chờ duyệt".equals(trangThaiFilter) && !"Cho duyet".equals(trangThai)) {
                        return false;
                    }
                    if ("Hiệu lực".equals(trangThaiFilter) && !"Hieu luc".equals(trangThai)) {
                        return false;
                    }
                    if ("Kết thúc".equals(trangThaiFilter) && !"Het hieu luc".equals(trangThai)) {
                        return false;
                    }
                    if ("Từ chối".equals(trangThaiFilter) && !"Tu choi".equals(trangThai)) {
                        return false;
                    }
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
            if (bn.getMaBoNhiem() == maBoNhiem) {
                return bn;
            }
        }
        return null;
    }

    private class StatusColorRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
            JTable t, Object value, boolean isSelected, boolean hasFocus, int row, int col
        ) {
            Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, col);
            setHorizontalAlignment(SwingConstants.CENTER);
            setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));

            if (!isSelected) {
                c.setBackground(row % 2 == 0 ? com.hrm.util.UIColors.WHITE : UIColors.TABLE_ALT_ROW);
                c.setForeground(UIColors.TEXT_DARK);

                if (col == COL_TRANG_THAI && value != null) {
                    String val = value.toString();
                    if ("Hieu luc".equals(val)) {
                        c.setForeground(UIColors.SUCCESS_GREEN);
                        ((JLabel) c).setFont(com.hrm.util.UIFonts.BOLD_SMALL);
                    } else if ("Cho duyet".equals(val)) {
                        c.setForeground(com.hrm.util.UIColors.WARNING_TEXT_AMBER);
                        ((JLabel) c).setFont(com.hrm.util.UIFonts.BOLD_SMALL);
                    } else if ("Tu choi".equals(val) || "Het hieu luc".equals(val)) {
                        c.setForeground(UIColors.DANGER_RED);
                        ((JLabel) c).setFont(com.hrm.util.UIFonts.BOLD_SMALL);
                    }
                }
            }
            return c;
        }
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
            .replaceAll("\\p{M}+", "")
            .replace('đ', 'd')
            .replace('Đ', 'D');
        return normalized.trim().toLowerCase();
    }
}
