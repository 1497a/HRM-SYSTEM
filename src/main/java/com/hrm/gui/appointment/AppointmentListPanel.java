package com.hrm.gui.appointment;

import com.hrm.gui.components.PurpleButton;
import com.hrm.gui.components.PurpleTable;
import com.hrm.model.BoNhiem;
import com.hrm.bus.BoNhiemBUS;
import com.hrm.bus.KetQua;
import com.hrm.util.SessionContext;
import com.hrm.util.UIColors;

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
    private PurpleButton btnTao;
    private PurpleButton btnPheDuyet;
    private PurpleButton btnTuChoi;
    private PurpleButton btnLamMoi;

    private List<BoNhiem> danhSachHienThi = new ArrayList<>();

    private static final String[] COL_NAMES = {
        "Mã BN", "Mã NV", "Họ tên", "Phòng ban", "Chức vụ",
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
            "Tất cả", "Chờ duyệt", "Hiệu lực", "Kết thúc"
        });
        cboTrangThai.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cboTrangThai.setPreferredSize(new Dimension(160, 32));

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

        // Chiều rộng cột
        int[] widths = {60, 70, 160, 140, 140, 90, 90, 100, 100, 100};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(UIColors.BORDER_GRAY));
        return scroll;
    }

    private JPanel buildSouthPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        panel.setOpaque(false);

        btnTao = new PurpleButton("+ Tạo bổ nhiệm");
        btnPheDuyet = PurpleButton.success("Phê duyệt");
        btnTuChoi = PurpleButton.danger("Từ chối");
        btnLamMoi = PurpleButton.secondary("Làm mới");

        panel.add(btnTao);
        panel.add(btnPheDuyet);
        panel.add(btnTuChoi);
        panel.add(btnLamMoi);

        return panel;
    }

    // ============================
    // Events
    // ============================

    private void setupEvents() {
        btnTao.addActionListener(e -> showCreateDialog());
        btnPheDuyet.addActionListener(e -> pheDuyetBoNhiem());
        btnTuChoi.addActionListener(e -> tuChoiBoNhiem());
        btnLamMoi.addActionListener(e -> loadData());

        cboTrangThai.addActionListener(e -> applyFilter());

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
        SessionContext sc = SessionContext.getInstance();
        boolean canCreate = sc.hasRole("ADMIN") || sc.hasPermission("APPOINTMENT_CREATE");
        boolean canApprove = sc.hasRole("ADMIN") || sc.hasPermission("APPOINTMENT_APPROVE");

        btnTao.setVisible(canCreate);
        btnPheDuyet.setVisible(canApprove);
        btnTuChoi.setVisible(canApprove);
    }

    // ============================
    // Data loading
    // ============================

    public void loadData() {
        danhSachHienThi = boNhiemService.getAll();
        tableModel.setRowCount(0);

        for (BoNhiem bn : danhSachHienThi) {
            tableModel.addRow(new Object[]{
                bn.getMaBoNhiem(),
                bn.getMaNV(),
                bn.getTenNV() != null ? bn.getTenNV() : "",
                bn.getTenPhongBan() != null ? bn.getTenPhongBan() : bn.getId(),
                bn.getTenChucVu() != null ? bn.getTenChucVu() : bn.getChucVuId(),
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

        RowFilter<DefaultTableModel, Object> rf = new RowFilter<DefaultTableModel, Object>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ? extends Object> entry) {
                if ("Tất cả".equals(trangThaiFilter)) return true;
                String trangThai = entry.getStringValue(9);
                if ("Chờ duyệt".equals(trangThaiFilter)) return "Chờ duyệt".equals(trangThai);
                if ("Hiệu lực".equals(trangThaiFilter)) return "Hiệu lực".equals(trangThai);
                if ("Kết thúc".equals(trangThaiFilter)) {
                    return "Hết hiệu lực".equals(trangThai) || "Từ chối".equals(trangThai);
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
        if (dialog.isSaved()) {
            loadData();
        }
    }

    private void pheDuyetBoNhiem() {
        BoNhiem selected = getSelectedBoNhiem();
        if (selected == null) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng chọn một bổ nhiệm để phê duyệt.",
                    "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (!"cho_duyet".equals(selected.getTrangThai())) {
            JOptionPane.showMessageDialog(this,
                    "Chỉ có thể phê duyệt bổ nhiệm đang ở trạng thái chờ duyệt.",
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Xác nhận phê duyệt bổ nhiệm #" + selected.getMaBoNhiem() + "?",
                "Xác nhận phê duyệt", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        int userId = 0;
        if (SessionContext.getInstance().getCurrentUser() != null) {
            userId = SessionContext.getInstance().getCurrentUser().getId();
        }

        KetQua<BoNhiem> result = boNhiemService.pheDuyetBoNhiem(
                selected.getMaBoNhiem(), userId);

        if (result.isSuccess()) {
            JOptionPane.showMessageDialog(this, result.getMessage(),
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
            loadData();
        } else {
            JOptionPane.showMessageDialog(this, result.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void tuChoiBoNhiem() {
        BoNhiem selected = getSelectedBoNhiem();
        if (selected == null) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng chọn một bổ nhiệm để từ chối.",
                    "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (!"cho_duyet".equals(selected.getTrangThai())) {
            JOptionPane.showMessageDialog(this,
                    "Chỉ có thể từ chối bổ nhiệm đang ở trạng thái chờ duyệt.",
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JTextField txtLyDo = new JTextField(30);
        txtLyDo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        Object[] message = {"Lý do từ chối:", txtLyDo};

        int confirm = JOptionPane.showConfirmDialog(this, message,
                "Từ chối bổ nhiệm #" + selected.getMaBoNhiem(),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (confirm != JOptionPane.OK_OPTION) return;

        KetQua<BoNhiem> result = boNhiemService.tuChoiBoNhiem(
                selected.getMaBoNhiem(), txtLyDo.getText().trim());

        if (result.isSuccess()) {
            JOptionPane.showMessageDialog(this, result.getMessage(),
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
            loadData();
        } else {
            JOptionPane.showMessageDialog(this, result.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
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

                // Tô màu cột trạng thái (cột 9)
                if (col == 9 && value != null) {
                    String val = value.toString();
                    if ("Hiệu lực".equals(val)) {
                        c.setForeground(UIColors.SUCCESS_GREEN);
                        ((JLabel) c).setFont(new Font("Segoe UI", Font.BOLD, 12));
                    } else if ("Chờ duyệt".equals(val)) {
                        c.setForeground(new Color(230, 120, 0));
                        ((JLabel) c).setFont(new Font("Segoe UI", Font.BOLD, 12));
                    } else if ("Từ chối".equals(val) || "Hết hiệu lực".equals(val)) {
                        c.setForeground(UIColors.DANGER_RED);
                        ((JLabel) c).setFont(new Font("Segoe UI", Font.BOLD, 12));
                    }
                }
            }
            return c;
        }
    }
}
