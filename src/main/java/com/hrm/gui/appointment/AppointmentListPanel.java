package com.hrm.gui.appointment;

import com.hrm.gui.components.PurpleButton;
import com.hrm.gui.components.PurpleTable;
import com.hrm.model.BoNhiem;
import com.hrm.bus.BoNhiemBUS;
import com.hrm.util.SessionContext;
import com.hrm.util.UIColors;
import com.hrm.util.UIHelper;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel danh sách bổ nhiệm nhân viên.
 */
public class AppointmentListPanel extends JPanel {

    private final BoNhiemBUS boNhiemService = BoNhiemBUS.getInstance();
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private PurpleTable table;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;

    private JComboBox<String> cboTrangThai;
    private JTextField txtTimKiem;
    private PurpleButton btnTao;
    private PurpleButton btnXemChiTiet;

    private List<BoNhiem> danhSachHienThi = new ArrayList<>();

    private static final String[] COL_NAMES = {
        "Mã BN", "Mã NV", "Họ tên", "Phòng ban", "Chức vụ", "Người quản lý",
        "Loại", "Tỷ lệ lương", "Từ ngày", "Đến ngày", "Trạng thái"
    };

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

    // ============================
    // Build sections
    // ============================

    private JPanel buildNorthPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setOpaque(false);

        // Tiêu đề
        JLabel lblTitle = new JLabel("QUẢN LÝ BỔ NHIỆM");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(UIColors.PRIMARY_PURPLE);
        lblTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        panel.add(lblTitle, BorderLayout.NORTH);

        // Filter panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        filterPanel.setOpaque(false);

        JLabel lblTrangThai = new JLabel("Trạng thái:");
        lblTrangThai.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblTrangThai.setForeground(UIColors.TEXT_DARK);

        cboTrangThai = new JComboBox<>(new String[]{
            "Tất cả", "Chờ duyệt", "Hiệu lực", "Kết thúc", "Từ chối"
        });
        cboTrangThai.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cboTrangThai.setPreferredSize(new Dimension(160, 32));

        JLabel lblTimKiem = new JLabel("Tìm theo tên:");
        lblTimKiem.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblTimKiem.setForeground(UIColors.TEXT_DARK);

        txtTimKiem = new JTextField(20);
        txtTimKiem.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtTimKiem.setPreferredSize(new Dimension(200, 32));

        filterPanel.add(lblTrangThai);
        filterPanel.add(cboTrangThai);
        filterPanel.add(Box.createHorizontalStrut(16));
        filterPanel.add(lblTimKiem);
        filterPanel.add(txtTimKiem);

        panel.add(filterPanel, BorderLayout.CENTER);
        return panel;
    }

    private JScrollPane buildCenterPanel() {
        tableModel = PurpleTable.createNonEditableModel(COL_NAMES);
        table = new PurpleTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setDefaultRenderer(Object.class, new StatusColorRenderer());

        // Chiều rộng cột
        int[] widths = {60, 70, 160, 140, 140, 140, 90, 80, 90, 90, 90};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        // Chỉ cho phép sort cột 1 (Mã NV) và cột 2 (Họ tên)
        for (int i = 0; i < COL_NAMES.length; i++) {
            sorter.setSortable(i, false); // tắt hết trước
        }
        sorter.setSortable(1, true); // Mã NV
        sorter.setSortable(2, true); // Họ tên

        // Comparator tiếng Việt cho cột Họ tên
        sorter.setComparator(2, UIHelper.vietnameseNameComparator());

        // Mặc định sort theo Mã NV tăng dần
        sorter.setSortKeys(List.of(new RowSorter.SortKey(1, SortOrder.ASCENDING)));

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

        return panel;
    }

    // ============================
    // Events
    // ============================

    private void setupEvents() {
        btnTao.addActionListener(e -> showCreateDialog());
        btnXemChiTiet.addActionListener(e -> showDetailDialog());

        cboTrangThai.addActionListener(e -> applyFilter());
        txtTimKiem.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { applyFilter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { applyFilter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { applyFilter(); }
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

    // ============================
    // Permissions
    // ============================

    private void setupPermissions() {
        btnTao.setVisible(SessionContext.getInstance().coQuyen("APPOINTMENT_CREATE"));
    }

    // ============================
    // Data loading
    // ============================

    public void loadData() {
        com.hrm.model.TaiKhoan currentUser = SessionContext.getInstance().getCurrentUser();
        String currentMaNV = (currentUser != null) ? currentUser.getNhanVienId() : null;
        danhSachHienThi = boNhiemService.getAllByScope(currentMaNV);
        tableModel.setRowCount(0);

        for (BoNhiem bn : danhSachHienThi) {
            tableModel.addRow(new Object[]{
                bn.getMaBoNhiem(),
                bn.getMaNhanVien() != null ? bn.getMaNhanVien() : String.valueOf(bn.getMaNV()),
                bn.getTenNV() != null ? bn.getTenNV() : "",
                bn.getTenPhongBan() != null ? bn.getTenPhongBan() : bn.getId(),
                bn.getTenChucVu() != null ? bn.getTenChucVu() : bn.getChucVuId(),
                bn.getTenQuanLy() != null ? bn.getTenQuanLy() : (bn.getQuanLyId() != null ? bn.getQuanLyId() : "-"),
                bn.getLoaiBoNhiemDisplay(),
                String.format("%.0f%%", bn.getTyLeHuongLuong()),
                bn.getTuNgay() != null ? bn.getTuNgay().format(dtf) : "",
                bn.getDenNgay() != null ? bn.getDenNgay().format(dtf) : "Không thời hạn",
                bn.getTrangThaiDisplay()
            });
        }

        applyFilter();
    }

    private void applyFilter() {
        String trangThaiFilter = (String) cboTrangThai.getSelectedItem();
        String keyword = txtTimKiem != null ? txtTimKiem.getText().trim().toLowerCase() : "";

        RowFilter<DefaultTableModel, Object> rf = new RowFilter<DefaultTableModel, Object>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ? extends Object> entry) {
                // Lọc theo tên nhân viên (cột 2)
                if (!keyword.isEmpty()) {
                    String tenNV = entry.getStringValue(2).toLowerCase();
                    if (!tenNV.contains(keyword)) return false;
                }
                // Lọc theo trạng thái (cột 10)
                if (!"Tất cả".equals(trangThaiFilter)) {
                    String trangThai = entry.getStringValue(10);
                    if ("Chờ duyệt".equals(trangThaiFilter) && !"Cho duyet".equals(trangThai)) return false;
                    if ("Hiệu lực".equals(trangThaiFilter) && !"Hieu luc".equals(trangThai)) return false;
                    if ("Kết thúc".equals(trangThaiFilter) && !"Het hieu luc".equals(trangThai)) return false;
                    if ("Từ chối".equals(trangThaiFilter) && !"Tu choi".equals(trangThai)) return false;
                }
                return true;
            }
        };

        sorter.setRowFilter(rf);
    }

    // ============================
    // Actions
    // ============================

    private void showCreateDialog() {
        AppointmentFormDialog dialog = new AppointmentFormDialog(
                (Frame) SwingUtilities.getWindowAncestor(this));
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            loadData();
        }
    }

    private void showDetailDialog() {
        BoNhiem selected = getSelectedBoNhiem();
        if (selected == null) return;
        AppointmentFormDialog dialog = new AppointmentFormDialog(
                (Frame) SwingUtilities.getWindowAncestor(this), selected);
        dialog.setVisible(true);
        if (dialog.isSaved() || dialog.isActionTaken()) {
            loadData();
        }
    }

    // ============================
    // Helper: get selected BoNhiem
    // ============================

    private BoNhiem getSelectedBoNhiem() {
        int viewRow = table.getSelectedRow();
        if (viewRow == -1) return null;
        int modelRow = table.convertRowIndexToModel(viewRow);
        if (modelRow < 0 || modelRow >= danhSachHienThi.size()) return null;
        int maBoNhiem = (int) tableModel.getValueAt(modelRow, 0);
        for (BoNhiem bn : danhSachHienThi) {
            if (bn.getMaBoNhiem() == maBoNhiem) return bn;
        }
        return null;
    }

    // ============================
    // Custom renderer for status coloring
    // ============================

    private class StatusColorRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object value,
                boolean isSelected, boolean hasFocus, int row, int col) {
            Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, col);
            setHorizontalAlignment(SwingConstants.CENTER);
            setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));

            if (!isSelected) {
                c.setBackground(row % 2 == 0 ? Color.WHITE : UIColors.TABLE_ALT_ROW);
                c.setForeground(UIColors.TEXT_DARK);

                // Tô màu cột trạng thái (cột 10)
                if (col == 10 && value != null) {
                    String val = value.toString();
                    if ("Hiệu lực".equals(val)) {
                        c.setForeground(UIColors.SUCCESS_GREEN);
                        ((JLabel) c).setFont(new Font("Segoe UI", Font.BOLD, 12));
                    } else if ("Chờ duyệt".equals(val)) {
                        c.setForeground(new Color(230, 120, 0));
                        ((JLabel) c).setFont(new Font("Segoe UI", Font.BOLD, 12));
                    } else if ("Từ chối".equals(val) || "Tu choi".equals(val)
                            || "Hết hiệu lực".equals(val) || "Het hieu luc".equals(val)) {
                        c.setForeground(UIColors.DANGER_RED);
                        ((JLabel) c).setFont(new Font("Segoe UI", Font.BOLD, 12));
                    }
                }
            }
            return c;
        }
    }
}
