package com.hrm.gui.notification;

import com.hrm.bus.KetQua;
import com.hrm.bus.ThongBaoBUS;
import com.hrm.model.TaiKhoan;
import com.hrm.model.ThongBao;
import com.hrm.util.DialogUtil;
import com.hrm.util.HRMConstants;
import com.hrm.util.UIColors;
import com.hrm.util.UIFonts;
import com.hrm.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Tab 1 - Thong bao cua nguoi dung hien tai.
 */
class TabMyNotificationsPanel extends JPanel {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final ThongBaoBUS service;
    private final TaiKhoan currentUser;
    private JTable tblThongBao;
    private DefaultTableModel modelThongBao;
    private TableRowSorter<DefaultTableModel> sorterTB;
    private JTextField txtTimKiem;
    private JComboBox<String> cboTrangThaiFilter;
    private List<ThongBao> danhSachThongBao;
    TabMyNotificationsPanel(ThongBaoBUS service, TaiKhoan currentUser) {
        this.service = service;
        this.currentUser = currentUser;
        setLayout(new BorderLayout(8, 8));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(12, 12, 12, 12));
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        toolbar.setOpaque(false);
        JButton btnDanhDauDaDoc = UIHelper.createPrimaryButton("Đánh dấu đã đọc");
        JButton btnDanhDauTatCa = UIHelper.createDefaultButton("Đánh dấu tất cả đã đọc");
        JButton btnLamMoi = UIHelper.createDefaultButton("Làm mới");
        btnDanhDauDaDoc.addActionListener(e -> danhDauDaDoc());
        btnDanhDauTatCa.addActionListener(e -> danhDauTatCa());
        btnLamMoi.addActionListener(e -> loadThongBao());
        txtTimKiem = UIHelper.createSearchField("Tìm theo tiêu đề, loại thông báo...");
        cboTrangThaiFilter = new JComboBox<>(new String[]{"Tất cả", "Chưa đọc", "Đã đọc"});
        toolbar.add(new JLabel("Tìm kiếm:"));
        toolbar.add(txtTimKiem);
        toolbar.add(new JLabel("Trạng thái:"));
        toolbar.add(cboTrangThaiFilter);
        toolbar.add(btnDanhDauDaDoc);
        toolbar.add(btnDanhDauTatCa);
        toolbar.add(btnLamMoi);
        String[] cols = {"Mã", "Tiêu đề", "Loại", "Ngày tạo", "Trạng thái"};
        modelThongBao = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        tblThongBao = new JTable(modelThongBao);
        sorterTB = new TableRowSorter<>(modelThongBao);
        tblThongBao.setRowSorter(sorterTB);
        UIHelper.attachSearchAndStatusFilter(txtTimKiem, cboTrangThaiFilter, sorterTB, 4, new int[]{1, 2});
        tblThongBao.setRowHeight(28);
        tblThongBao.setFont(com.hrm.util.UIFonts.TEXT_NORMAL);
        tblThongBao.getTableHeader().setFont(com.hrm.util.UIFonts.BOLD_NORMAL);
        tblThongBao.getTableHeader().setBackground(UIColors.PRIMARY_PURPLE);
        tblThongBao.getTableHeader().setForeground(UIColors.TEXT_DARK);
        tblThongBao.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblThongBao.setSelectionBackground(UIColors.LIGHT_PURPLE);
        tblThongBao.setSelectionForeground(UIColors.TEXT_DARK);
        tblThongBao.getColumnModel().getColumn(0).setMinWidth(0);
        tblThongBao.getColumnModel().getColumn(0).setMaxWidth(0);
        tblThongBao.getColumnModel().getColumn(0).setWidth(0);
        tblThongBao.getColumnModel().getColumn(1).setPreferredWidth(350);
        tblThongBao.getColumnModel().getColumn(2).setPreferredWidth(150);
        tblThongBao.getColumnModel().getColumn(3).setPreferredWidth(160);
        tblThongBao.getColumnModel().getColumn(4).setPreferredWidth(100);
        tblThongBao.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                setHorizontalAlignment(SwingConstants.CENTER);
                String trangThai = value != null ? value.toString() : "";
                setText(trangThai);
                c.setFont(com.hrm.util.UIFonts.TEXT_NORMAL);
                if (isSelected) {
                    c.setForeground(table.getSelectionForeground());
                } else if ("Đã đọc".equals(trangThai)) {
                    c.setForeground(UIColors.SUCCESS_GREEN);
                } else {
                    c.setForeground(UIColors.DANGER_RED);
                    c.setFont(com.hrm.util.UIFonts.BOLD_SMALL);
                }
                return c;
            }
        });
        tblThongBao.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int viewRow = tblThongBao.getSelectedRow();
                if (viewRow >= 0 && danhSachThongBao != null) {
                    int modelRow = tblThongBao.convertRowIndexToModel(viewRow);
                    if (modelRow >= 0 && modelRow < danhSachThongBao.size()) {
                        ThongBao tb = danhSachThongBao.get(modelRow);
                        if (!tb.isDaDoc()) markAsRead(tb.getMaThongBao());
                        showNoiDung(tb);
                    }
                }
            }
        });
        JScrollPane scroll = new JScrollPane(tblThongBao);
        scroll.setBorder(new TitledBorder("Danh sách thông báo"));
        add(toolbar, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        loadThongBao();
    }

    void loadThongBao() {
        modelThongBao.setRowCount(0);
        if (currentUser == null) return;
        try {
            danhSachThongBao = service.getThongBaoCuaToi(currentUser.getId());
            for (ThongBao tb : danhSachThongBao) {
                String ngayTao = tb.getNgayTao() != null ? tb.getNgayTao().format(DATE_FORMAT) : "";
                modelThongBao.addRow(new Object[]{
                    tb.getMaThongBao(), tb.getTieuDe(), HRMConstants.display(tb.getLoaiThongBao()),
                    ngayTao, tb.isDaDoc() ? "Đã đọc" : "Chưa đọc"
                });
            }
        } catch (Exception ex) {
            DialogUtil.showError(this, "Lỗi tải thông báo: " + ex.getMessage());
        }
    }

    private void danhDauDaDoc() {
        int viewRow = tblThongBao.getSelectedRow();
        if (viewRow < 0) {
            DialogUtil.showInfo(this, "Vui lòng chọn thông báo cần đánh dấu.");
            return;
        }
        int row = tblThongBao.convertRowIndexToModel(viewRow);
        int maThongBao = (int) modelThongBao.getValueAt(row, 0);
        try {
            KetQua<Void> result = service.danhDauDaDoc(maThongBao);
            if (result.isSuccess()) {
                DialogUtil.showSuccess(this, result.getMessage());
                loadThongBao();
            } else {
                DialogUtil.showError(this, result.getMessage());
            }
        } catch (Exception ex) {
            DialogUtil.showError(this, "Lỗi: " + ex.getMessage());
        }
    }

    private void danhDauTatCa() {
        if (currentUser == null) return;
        try {
            KetQua<Void> result = service.danhDauTatCaDaDoc(currentUser.getId());
            if (result.isSuccess()) {
                loadThongBao();
                DialogUtil.showSuccess(this, "Đã đánh dấu tất cả là đã đọc.");
            } else {
                DialogUtil.showError(this, result.getMessage());
            }
        } catch (Exception ex) {
            DialogUtil.showError(this, "Lỗi: " + ex.getMessage());
        }
    }

    private void markAsRead(int maThongBao) {
        KetQua<Void> result = service.danhDauDaDoc(maThongBao);
        if (result.isSuccess()) loadThongBao();
        // silent on error — auto-mark khi mở nội dung, không làm ngắt luồng UI
    }

    private void showNoiDung(ThongBao tb) {
        JTextArea area = new JTextArea(tb.getNoiDung() != null ? tb.getNoiDung() : "");
        area.setEditable(false);
        area.setFont(UIFonts.TEXT_NORMAL);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBackground(Color.WHITE);
        area.setBorder(new EmptyBorder(8, 8, 8, 8));
        JScrollPane scroll = new JScrollPane(area);
        scroll.setPreferredSize(new Dimension(420, 200));
        JOptionPane.showMessageDialog(this, scroll,
                tb.getTieuDe() != null ? tb.getTieuDe() : "Thông báo",
                JOptionPane.INFORMATION_MESSAGE);
    }
}
