package com.hrm.gui.salary;

import com.hrm.bus.LuongBUS;
import com.hrm.model.BangLuong;
import com.hrm.model.ChiTietLuong;
import com.hrm.util.UIColors;
import com.hrm.util.UIFonts;
import com.hrm.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Panel phieu luong ca nhan (DataScope.SELF):
 * cho phep chon ky luong qua combobox va xem chi tiet.
 */
class SalarySelfViewPanel extends JPanel {

    private static final NumberFormat MONEY_FORMAT = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
    private final LuongBUS salaryService;
    private final String maNVHienTai;
    private JTable tblChiTiet;
    private DefaultTableModel modelChiTiet;
    private List<ChiTietLuong> currentChiTietList = new ArrayList<>();
    private int selectedMaBL = -1;
    private JComboBox<BangLuong> cboKyLuong;
    private JButton btnXemChiTiet;
    SalarySelfViewPanel(LuongBUS salaryService, String maNVHienTai) {
        this.salaryService = salaryService;
        this.maNVHienTai = maNVHienTai;
        setLayout(new BorderLayout(12, 12));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(12, 12, 12, 12));
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        toolbar.setOpaque(false);
        JLabel lblTitle = new JLabel("Phiếu lương của tôi");
        lblTitle.setFont(UIFonts.HEADER_SUB);
        toolbar.add(lblTitle);
        toolbar.add(Box.createHorizontalStrut(12));
        toolbar.add(new JLabel("Kỳ lương:"));
        cboKyLuong = new JComboBox<>();
        cboKyLuong.setFont(UIFonts.TEXT_NORMAL);
        cboKyLuong.setPreferredSize(new Dimension(260, 34));
        cboKyLuong.setRenderer(new javax.swing.DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                BangLuong bl = (BangLuong) value;
                String text = bl == null ? "" : buildBangLuongLabel(bl);
                return super.getListCellRendererComponent(list, text, index, isSelected, cellHasFocus);
            }
        });
        cboKyLuong.addActionListener(e -> {
            BangLuong selected = (BangLuong) cboKyLuong.getSelectedItem();
            if (selected != null) {
                selectedMaBL = selected.getMaBL();
                loadChiTietCaNhan(selectedMaBL);
            }
        });
        toolbar.add(cboKyLuong);
        btnXemChiTiet = UIHelper.createPrimaryButton("Xem chi tiết");
        btnXemChiTiet.addActionListener(e -> showChiTietDialog());
        toolbar.add(btnXemChiTiet);
        add(toolbar, BorderLayout.NORTH);
        add(buildChiTietTablePanel("Phiếu lương cá nhân"), BorderLayout.CENTER);
        loadBangLuong();
    }

    private void loadBangLuong() {
        try {
            List<BangLuong> allBL = salaryService.getAll();
            List<BangLuong> kyLuongCaNhan = new ArrayList<>();
            if (maNVHienTai != null) {
                for (BangLuong bl : allBL) {
                    if (salaryService.getChiTietCaNhan(bl.getMaBL(), maNVHienTai) != null) {
                        kyLuongCaNhan.add(bl);
                    }
                }
            }
            kyLuongCaNhan.sort(Comparator.comparingInt(BangLuong::getNam).reversed()
                    .thenComparing(Comparator.comparingInt(BangLuong::getThang).reversed()));
            DefaultComboBoxModel<BangLuong> comboModel = new DefaultComboBoxModel<>();
            for (BangLuong bl : kyLuongCaNhan) comboModel.addElement(bl);
            cboKyLuong.setModel(comboModel);
            btnXemChiTiet.setEnabled(comboModel.getSize() > 0);
            if (comboModel.getSize() > 0) {
                cboKyLuong.setSelectedIndex(0);
                BangLuong selected = (BangLuong) cboKyLuong.getSelectedItem();
                if (selected != null) {
                    selectedMaBL = selected.getMaBL();
                    loadChiTietCaNhan(selectedMaBL);
                }
            } else {
                selectedMaBL = -1;
                currentChiTietList = new ArrayList<>();
                modelChiTiet.setRowCount(0);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi tải danh sách bảng lương: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadChiTietCaNhan(int maBL) {
        try {
            ChiTietLuong ct = maNVHienTai != null ? salaryService.getChiTietCaNhan(maBL, maNVHienTai) : null;
            currentChiTietList = new ArrayList<>();
            if (ct != null) currentChiTietList.add(ct);
            fillChiTietTable(currentChiTietList);
            btnXemChiTiet.setEnabled(ct != null);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi tải phiếu lương cá nhân: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JScrollPane buildChiTietTablePanel(String title) {
        modelChiTiet = new DefaultTableModel(0, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        modelChiTiet.setColumnIdentifiers(new Object[]{
            "Mã NV", "Họ tên", "Lương cơ bản", "Lương chức vụ",
            "Tiền OT", "Tổng thu nhập", "Khấu trừ", "Thực lãnh",
            "Số ngày công", "Số giờ OT", "Ghi chú"
        });
        tblChiTiet = new JTable(modelChiTiet);
        tblChiTiet.setRowHeight(28);
        tblChiTiet.setFont(com.hrm.util.UIFonts.TEXT_NORMAL);
        tblChiTiet.getTableHeader().setFont(com.hrm.util.UIFonts.BOLD_NORMAL);
        tblChiTiet.getTableHeader().setBackground(UIColors.PRIMARY_PURPLE);
        tblChiTiet.getTableHeader().setForeground(UIColors.TEXT_DARK);
        tblChiTiet.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblChiTiet.setSelectionBackground(UIColors.LIGHT_PURPLE);
        tblChiTiet.setSelectionForeground(UIColors.TEXT_DARK);
        int[] widths = {70, 150, 125, 125, 110, 125, 110, 125, 95, 95, 220};
        for (int i = 0; i < widths.length; i++) {
            tblChiTiet.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        for (int i = 2; i <= 7; i++) tblChiTiet.getColumnModel().getColumn(i).setCellRenderer(rightRenderer);
        tblChiTiet.getColumnModel().getColumn(9).setCellRenderer(rightRenderer);
        tblChiTiet.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && selectedMaBL >= 0) showChiTietDialog();
            }
        });
        JScrollPane scroll = new JScrollPane(tblChiTiet);
        scroll.setBorder(new TitledBorder(title));
        return scroll;
    }

    private void fillChiTietTable(List<ChiTietLuong> list) {
        modelChiTiet.setRowCount(0);
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
                ct.getSoNgayCong(),
                formatHours(ct.getTongGioOT()),
                ct.getGhiChu() != null ? ct.getGhiChu() : ""
            });
        }
    }

    private void showChiTietDialog() {
        int row = tblChiTiet.getSelectedRow();
        if (row < 0 || row >= currentChiTietList.size()) return;
        try {
            ChiTietLuong ct = currentChiTietList.get(row);
            SalaryDetailDialog dialog = new SalaryDetailDialog(
                    (Frame) SwingUtilities.getWindowAncestor(this), ct);
            dialog.setVisible(true);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi mở chi tiết: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String buildBangLuongLabel(BangLuong bl) {
        return "Bảng lương tháng " + bl.getThang() + "/" + bl.getNam()
                + " - " + (bl.getTrangThai() != null ? bl.getTrangThai().toString() : "");
    }

    private String formatMoney(double amount) {
        return MONEY_FORMAT.format((long) amount) + " đ";
    }

    private String formatHours(double hours) {
        if (hours == Math.rint(hours)) return String.valueOf((long) hours);
        return String.format(Locale.US, "%.2f", hours);
    }
}
