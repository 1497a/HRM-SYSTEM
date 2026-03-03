package com.hrm.gui.salary;

import com.hrm.model.ChiTietLuong;
import com.hrm.model.ThanhPhanLuong;
import com.hrm.util.UIColors;
import com.hrm.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * Dialog hiển thị chi tiết lương đầy đủ của một nhân viên trong một kỳ lương.
 * Bao gồm thông tin tổng hợp và bảng thành phần lương (ThanhPhanLuong).
 */
public class SalaryDetailDialog extends JDialog {

    private final ChiTietLuong chiTiet;
    private static final NumberFormat MONEY_FORMAT = NumberFormat.getNumberInstance(new Locale("vi", "VN"));

    public SalaryDetailDialog(Frame parent, ChiTietLuong chiTiet) {
        super(parent, "Chi tiết lương nhân viên", true);
        this.chiTiet = chiTiet;
        initUI();
        pack();
        setMinimumSize(new Dimension(600, 520));
        setLocationRelativeTo(parent);
    }

    private void initUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(12, 12));
        mainPanel.setBackground(UIColors.LIGHT_GRAY_BG);
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        mainPanel.add(buildInfoPanel(), BorderLayout.NORTH);
        mainPanel.add(buildThanhPhanPanel(), BorderLayout.CENTER);
        mainPanel.add(buildButtonPanel(), BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    // =======================
    // Build info panel
    // =======================

    private JPanel buildInfoPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(UIColors.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIColors.BORDER_GRAY),
                new EmptyBorder(15, 15, 15, 15)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.anchor = GridBagConstraints.WEST;

        // Title
        JLabel lblTitle = new JLabel("Thông tin lương nhân viên");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(UIColors.PRIMARY_PURPLE);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 4;
        panel.add(lblTitle, gbc);
        gbc.gridwidth = 1;

        // Row 1: Mã NV, Họ tên
        gbc.gridy = 1; gbc.gridx = 0;
        panel.add(makeLabel("Mã nhân viên:"), gbc);
        gbc.gridx = 1;
        panel.add(makeValue(String.valueOf(chiTiet.getMaNV())), gbc);
        gbc.gridx = 2;
        panel.add(makeLabel("Họ tên:"), gbc);
        gbc.gridx = 3;
        panel.add(makeValue(chiTiet.getTenNV() != null ? chiTiet.getTenNV() : ""), gbc);

        // Row 2: Lương cơ bản, Lương chức vụ
        gbc.gridy = 2; gbc.gridx = 0;
        panel.add(makeLabel("Lương cơ bản:"), gbc);
        gbc.gridx = 1;
        panel.add(makeValueMoney(chiTiet.getLuongCoBan()), gbc);
        gbc.gridx = 2;
        panel.add(makeLabel("Lương chức vụ:"), gbc);
        gbc.gridx = 3;
        panel.add(makeValueMoney(chiTiet.getTongLuongChucVu()), gbc);

        // Row 3: Tiền OT, Tổng thu nhập
        gbc.gridy = 3; gbc.gridx = 0;
        panel.add(makeLabel("Tiền làm thêm (OT):"), gbc);
        gbc.gridx = 1;
        panel.add(makeValueMoney(chiTiet.getTienOT()), gbc);
        gbc.gridx = 2;
        panel.add(makeLabel("Tổng thu nhập:"), gbc);
        gbc.gridx = 3;
        JLabel lblTongThu = makeValueMoney(chiTiet.getTongLuong());
        lblTongThu.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTongThu.setForeground(UIColors.INFO_BLUE);
        panel.add(lblTongThu, gbc);

        // Row 4: Khấu trừ, Thực lãnh
        gbc.gridy = 4; gbc.gridx = 0;
        panel.add(makeLabel("Tổng khấu trừ:"), gbc);
        gbc.gridx = 1;
        JLabel lblKhauTru = makeValueMoney(chiTiet.getTongKhauTru());
        lblKhauTru.setForeground(UIColors.DANGER_RED);
        panel.add(lblKhauTru, gbc);
        gbc.gridx = 2;
        panel.add(makeLabel("Lương thực lãnh:"), gbc);
        gbc.gridx = 3;
        JLabel lblThucLanh = makeValueMoney(chiTiet.getLuongThucNhan());
        lblThucLanh.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblThucLanh.setForeground(UIColors.SUCCESS_GREEN);
        panel.add(lblThucLanh, gbc);

        // Row 5: Số ngày công
        gbc.gridy = 5; gbc.gridx = 0;
        panel.add(makeLabel("Số ngày công:"), gbc);
        gbc.gridx = 1;
        panel.add(makeValue(String.valueOf(chiTiet.getSoNgayCong())), gbc);

        return panel;
    }

    // =======================
    // Build thành phần lương panel
    // =======================

    private JPanel buildThanhPhanPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UIColors.LIGHT_GRAY_BG);

        String[] cols = {"Khoản", "Loại", "Số tiền"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        List<ThanhPhanLuong> danhSach = chiTiet.getDanhSachThanhPhan();
        if (danhSach != null) {
            for (ThanhPhanLuong tp : danhSach) {
                String loaiDisplay = tp.getLoai() != null ? tp.getLoai().toString() : "";
                model.addRow(new Object[]{
                        tp.getTenKhoan(),
                        loaiDisplay,
                        formatMoney(tp.getSoTien())
                });
            }
        }

        JTable table = new JTable(model);
        table.setRowHeight(26);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(UIColors.PRIMARY_PURPLE);
        table.getTableHeader().setForeground(UIColors.WHITE);
        table.setSelectionBackground(UIColors.LIGHT_PURPLE);

        // Column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(250);
        table.getColumnModel().getColumn(1).setPreferredWidth(120);
        table.getColumnModel().getColumn(2).setPreferredWidth(150);

        // Right-align money
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        table.getColumnModel().getColumn(2).setCellRenderer(rightRenderer);

        // Color rows by type
        table.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, col);
                setHorizontalAlignment(SwingConstants.CENTER);
                if (!isSelected && value != null) {
                    if ("Khấu trừ".equals(value.toString())) {
                        c.setForeground(UIColors.DANGER_RED);
                    } else {
                        c.setForeground(UIColors.SUCCESS_GREEN);
                    }
                    ((JLabel) c).setFont(new Font("Segoe UI", Font.BOLD, 12));
                }
                return c;
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new TitledBorder("Thành phần lương"));

        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    // =======================
    // Button panel
    // =======================

    private JPanel buildButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setOpaque(false);

        JButton btnDong = UIHelper.createDefaultButton("Đóng");
        btnDong.addActionListener(e -> dispose());
        panel.add(btnDong);
        return panel;
    }

    // =======================
    // Helpers
    // =======================

    private JLabel makeLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lbl.setForeground(UIColors.TEXT_GRAY);
        return lbl;
    }

    private JLabel makeValue(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(UIColors.TEXT_DARK);
        return lbl;
    }

    private JLabel makeValueMoney(double amount) {
        return makeValue(formatMoney(amount));
    }

    private String formatMoney(double amount) {
        return MONEY_FORMAT.format((long) amount) + " đ";
    }
}
