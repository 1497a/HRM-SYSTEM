package com.hrm.gui.salary;

import com.hrm.model.BangLuong;
import com.hrm.model.ChiTietLuong;
import com.hrm.service.SalaryService;
import com.hrm.service.ServiceResult;
import com.hrm.util.UIColors;
import com.hrm.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Panel quản lý bảng lương.
 * Tab 1: Danh sách các kỳ bảng lương (BangLuong).
 * Tab 2: Chi tiết lương của kỳ được chọn (ChiTietLuong).
 */
public class SalaryListPanel extends JPanel {

    private final SalaryService salaryService;

    // Tab 1 - Bảng lương
    private JTable tblBangLuong;
    private DefaultTableModel modelBangLuong;
    private JButton btnTinhLuong;
    private JButton btnKhoaBangLuong;
    private JButton btnLamMoiBL;

    // Tab 2 - Chi tiết lương
    private JTable tblChiTiet;
    private DefaultTableModel modelChiTiet;
    private JButton btnLamMoiCT;

    private JTabbedPane tabbedPane;

    // Dữ liệu hiện tại
    private List<BangLuong> danhSachBL;
    private int selectedMaBL = -1;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final NumberFormat MONEY_FORMAT = NumberFormat.getNumberInstance(new Locale("vi", "VN"));

    public SalaryListPanel() {
        this.salaryService = SalaryService.getInstance();
        setLayout(new BorderLayout());
        setBackground(UIColors.LIGHT_GRAY_BG);

        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tabbedPane.setBackground(UIColors.WHITE);

        tabbedPane.addTab("Bảng lương", buildBangLuongTab());
        tabbedPane.addTab("Chi tiết lương", buildChiTietTab());

        add(tabbedPane, BorderLayout.CENTER);

        loadBangLuong();
    }

    // =======================
    // Build Tab 1
    // =======================

    private JPanel buildBangLuongTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(UIColors.LIGHT_GRAY_BG);
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));

        // Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        toolbar.setOpaque(false);

        btnTinhLuong = UIHelper.createSuccessButton("Tính lương tháng mới");
        btnKhoaBangLuong = UIHelper.createDangerButton("Khóa bảng lương");
        btnLamMoiBL = UIHelper.createDefaultButton("Làm mới");

        btnTinhLuong.addActionListener(e -> tinhLuongThangMoi());
        btnKhoaBangLuong.addActionListener(e -> khoaBangLuong());
        btnLamMoiBL.addActionListener(e -> loadBangLuong());

        toolbar.add(btnTinhLuong);
        toolbar.add(btnKhoaBangLuong);
        toolbar.add(btnLamMoiBL);

        // Table
        String[] cols = {"Mã BL", "Tháng", "Năm", "Tên bảng lương", "Ngày tạo", "Trạng thái"};
        modelBangLuong = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        tblBangLuong = new JTable(modelBangLuong);
        tblBangLuong.setRowHeight(28);
        tblBangLuong.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tblBangLuong.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tblBangLuong.getTableHeader().setBackground(UIColors.PRIMARY_PURPLE);
        tblBangLuong.getTableHeader().setForeground(UIColors.TEXT_DARK);
        tblBangLuong.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblBangLuong.setSelectionBackground(UIColors.LIGHT_PURPLE);
        tblBangLuong.setSelectionForeground(UIColors.TEXT_DARK);

        // Column widths
        int[] widths = {60, 70, 70, 250, 150, 120};
        for (int i = 0; i < widths.length; i++) {
            tblBangLuong.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        // Status renderer
        tblBangLuong.getColumnModel().getColumn(5).setCellRenderer(new StatusCellRenderer());

        // Row click -> load chi tiết in Tab 2
        tblBangLuong.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = tblBangLuong.getSelectedRow();
                if (row >= 0) {
                    selectedMaBL = (int) modelBangLuong.getValueAt(row, 0);
                    loadChiTiet(selectedMaBL);
                    tabbedPane.setSelectedIndex(1);
                }
            }
        });

        JScrollPane scroll = new JScrollPane(tblBangLuong);
        scroll.setBorder(new TitledBorder("Danh sách kỳ bảng lương"));

        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    // =======================
    // Build Tab 2
    // =======================

    private JPanel buildChiTietTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(UIColors.LIGHT_GRAY_BG);
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));

        // Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        toolbar.setOpaque(false);

        btnLamMoiCT = UIHelper.createDefaultButton("Làm mới");
        btnLamMoiCT.addActionListener(e -> {
            if (selectedMaBL >= 0) loadChiTiet(selectedMaBL);
        });
        toolbar.add(btnLamMoiCT);

        // Table
        String[] cols = {"Mã NV", "Họ tên", "Lương cơ bản", "Lương chức vụ",
                "Tiền OT", "Tổng thu nhập", "Khấu trừ", "Thực lãnh", "Số ngày công"};
        modelChiTiet = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        tblChiTiet = new JTable(modelChiTiet);
        tblChiTiet.setRowHeight(28);
        tblChiTiet.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tblChiTiet.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tblChiTiet.getTableHeader().setBackground(UIColors.PRIMARY_PURPLE);
        tblChiTiet.getTableHeader().setForeground(UIColors.TEXT_DARK);
        tblChiTiet.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblChiTiet.setSelectionBackground(UIColors.LIGHT_PURPLE);
        tblChiTiet.setSelectionForeground(UIColors.TEXT_DARK);

        // Column widths
        int[] widths = {70, 160, 130, 130, 110, 130, 110, 130, 100};
        for (int i = 0; i < widths.length; i++) {
            tblChiTiet.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        // Right-align money columns
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        for (int i = 2; i <= 7; i++) {
            tblChiTiet.getColumnModel().getColumn(i).setCellRenderer(rightRenderer);
        }

        // Double-click -> show detail dialog
        tblChiTiet.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && selectedMaBL >= 0) {
                    showChiTietDialog();
                }
            }
        });

        JScrollPane scroll = new JScrollPane(tblChiTiet);
        scroll.setBorder(new TitledBorder("Chi tiết lương nhân viên (double-click để xem chi tiết)"));

        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    // =======================
    // Data loading
    // =======================

    private void loadBangLuong() {
        modelBangLuong.setRowCount(0);
        try {
            danhSachBL = salaryService.getAll();
            for (BangLuong bl : danhSachBL) {
                int thang = bl.getNgayBD() != null ? bl.getNgayBD().getMonthValue() : 0;
                int nam = bl.getNgayBD() != null ? bl.getNgayBD().getYear() : 0;
                String tenBL = "Bảng lương tháng " + thang + "/" + nam;
                String ngayTao = bl.getNgayTao() != null
                        ? bl.getNgayTao().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                        : "";
                String trangThai = bl.getTrangThai() != null ? bl.getTrangThai().getDisplayName() : "";
                modelBangLuong.addRow(new Object[]{
                        bl.getMaBL(), thang, nam, tenBL, ngayTao, trangThai
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Lỗi tải danh sách bảng lương: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadChiTiet(int maBL) {
        modelChiTiet.setRowCount(0);
        try {
            List<ChiTietLuong> list = salaryService.getChiTiet(maBL);
            for (ChiTietLuong ct : list) {
                modelChiTiet.addRow(new Object[]{
                        ct.getMaNV(),
                        ct.getTenNV() != null ? ct.getTenNV() : "",
                        formatMoney(ct.getLuongCoBan()),
                        formatMoney(ct.getTongLuongChucVu()),
                        formatMoney(ct.getTienOT()),
                        formatMoney(ct.getTongLuong()),
                        formatMoney(ct.getTongKhauTru()),
                        formatMoney(ct.getLuongThucNhan()),
                        ct.getSoNgayCong()
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Lỗi tải chi tiết lương: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    // =======================
    // Actions
    // =======================

    private void tinhLuongThangMoi() {
        JSpinner spinThang = new JSpinner(new SpinnerNumberModel(
                java.time.LocalDate.now().getMonthValue(), 1, 12, 1));
        JSpinner spinNam = new JSpinner(new SpinnerNumberModel(
                java.time.LocalDate.now().getYear(), 2000, 2100, 1));
        spinThang.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        spinNam.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JPanel panel = new JPanel(new GridLayout(2, 2, 8, 8));
        panel.add(new JLabel("Tháng:"));
        panel.add(spinThang);
        panel.add(new JLabel("Năm:"));
        panel.add(spinNam);

        int result = JOptionPane.showConfirmDialog(this, panel,
                "Tính lương tháng mới", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;

        int thang = (int) spinThang.getValue();
        int nam = (int) spinNam.getValue();

        try {
            ServiceResult<BangLuong> sr = salaryService.tinhLuong(thang, nam);
            if (sr.isSuccess()) {
                JOptionPane.showMessageDialog(this,
                        "Tính lương tháng " + thang + "/" + nam + " thành công!",
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
                loadBangLuong();
            } else {
                JOptionPane.showMessageDialog(this,
                        sr.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Lỗi tính lương: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void khoaBangLuong() {
        int row = tblBangLuong.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng chọn bảng lương cần khóa.",
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int maBL = (int) modelBangLuong.getValueAt(row, 0);
        String tenBL = (String) modelBangLuong.getValueAt(row, 3);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Khóa bảng lương: " + tenBL + "?\nHành động này không thể hoàn tác.",
                "Xác nhận khóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            ServiceResult<Void> sr = salaryService.khoaBangLuong(maBL);
            if (sr.isSuccess()) {
                JOptionPane.showMessageDialog(this,
                        "Đã khóa bảng lương thành công.",
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
                loadBangLuong();
            } else {
                JOptionPane.showMessageDialog(this,
                        sr.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Lỗi khóa bảng lương: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showChiTietDialog() {
        int row = tblChiTiet.getSelectedRow();
        if (row < 0) return;

        // Reload chi tiết để có ThanhPhanLuong
        try {
            List<ChiTietLuong> list = salaryService.getChiTiet(selectedMaBL);
            if (row < list.size()) {
                ChiTietLuong ct = list.get(row);
                SalaryDetailDialog dialog = new SalaryDetailDialog(
                        (Frame) SwingUtilities.getWindowAncestor(this), ct);
                dialog.setVisible(true);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Lỗi mở chi tiết: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    // =======================
    // Helpers
    // =======================

    private String formatMoney(double amount) {
        return MONEY_FORMAT.format((long) amount) + " đ";
    }

    /**
     * Custom cell renderer for trạng thái column.
     */
    private static class StatusCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int col) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
            setHorizontalAlignment(SwingConstants.CENTER);
            if (!isSelected && value != null) {
                String v = value.toString();
                if (v.contains("Đã duyệt") || v.contains("Đã chi") || v.contains("da_khoa")) {
                    c.setForeground(UIColors.SUCCESS_GREEN);
                } else if (v.contains("Nháp")) {
                    c.setForeground(UIColors.TEXT_DARK);
                } else {
                    c.setForeground(UIColors.INFO_BLUE);
                }
                ((JLabel) c).setFont(new Font("Segoe UI", Font.BOLD, 12));
            }
            return c;
        }
    }
}
