package com.hrm.gui.recruitment;

import com.hrm.bus.KetQua;
import com.hrm.bus.TuyenDungBUS;
import com.hrm.model.TinTuyenDung;
import com.hrm.model.YeuCauTuyenDung;
import com.hrm.util.DialogUtil;
import com.hrm.util.PermissionCodes;
import com.hrm.gui.components.PurpleTable;
import com.hrm.util.UIFonts;
import com.hrm.util.SessionContext;
import com.hrm.util.UIColors;
import com.hrm.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

import static javax.swing.RowSorter.SortKey;
import static javax.swing.SortOrder.ASCENDING;

class TabTinPanel extends JPanel {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final TuyenDungBUS service;
    private PurpleTable tbl;
    private DefaultTableModel model;
    private JButton btnDangTin;
    private JButton btnDongTin;
    private JTextField txtTimKiem;
    TabTinPanel(TuyenDungBUS service) {
        this.service = service;
        setLayout(new BorderLayout(8, 8));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(12, 12, 12, 12));
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        toolbar.setOpaque(false);
        btnDangTin = UIHelper.createSuccessButton("Đăng tin");
        btnDongTin = UIHelper.createDangerButton("Đóng tin");
        JButton btnLamMoi = UIHelper.createDefaultButton("Làm mới");
        btnDangTin.addActionListener(e -> dangTin());
        btnDongTin.addActionListener(e -> dongTin());
        btnLamMoi.addActionListener(e -> load());
        JComboBox<String> cboTrangThai = new JComboBox<>(
                new String[]{"Tất cả", "Đang tuyển", "Tạm dừng", "Đã đóng"});
        txtTimKiem = UIHelper.createSearchField("Tìm theo tiêu đề, phòng ban...");
        toolbar.add(btnDangTin);
        toolbar.add(btnDongTin);
        toolbar.add(btnLamMoi);
        toolbar.add(new JLabel("Tìm kiếm:"));
        toolbar.add(txtTimKiem);
        toolbar.add(new JLabel("Trạng thái:"));
        toolbar.add(cboTrangThai);
        String[] cols = {"Mã tin", "Tiêu đề", "Phòng ban", "Chức vụ", "Hạn nộp", "Cần tuyển", "Số đơn", "Trạng thái"};
        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
            @Override
            public Class<?> getColumnClass(int col) {
                return (col == 0 || col == 5 || col == 6) ? Integer.class : String.class;
            }
        };
        tbl = TabUtils.buildTable(model);
        TabUtils.applyColWidths(tbl, new int[]{60, 220, 160, 140, 110, 80, 70, 110});
        tbl.getColumnModel().getColumn(7).setCellRenderer(new com.hrm.gui.components.StatusCellRenderer());
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        tbl.setRowSorter(sorter);
        sorter.setComparator(0, Comparator.comparingInt(a -> (Integer) a));
        sorter.setComparator(5, Comparator.comparingInt(a -> (Integer) a));
        sorter.setComparator(6, Comparator.comparingInt(a -> (Integer) a));
        sorter.setComparator(4, TabUtils.dateComparator());
        sorter.setSortKeys(List.of(new SortKey(0, ASCENDING)));
        UIHelper.attachSearchAndStatusFilter(txtTimKiem, cboTrangThai, sorter, 7, new int[]{1, 2, 3});
        JScrollPane scroll = new JScrollPane(tbl);
        scroll.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        JLabel lblHint = new JLabel("Quản lý tin tuyển dụng. Nhấp đúp để xem chi tiết.");
        lblHint.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblHint.setForeground(UIColors.TEXT_DARK);
        JPanel northPanel = new JPanel(new BorderLayout(0, 4));
        northPanel.setOpaque(false);
        northPanel.add(lblHint, BorderLayout.NORTH);
        northPanel.add(toolbar, BorderLayout.CENTER);
        add(northPanel, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        boolean canManage = SessionContext.getInstance().hasPermission(PermissionCodes.RECRUITMENT_MANAGE);
        btnDangTin.setVisible(canManage);
        btnDongTin.setVisible(canManage);
        load();
    }

    void load() {
        model.setRowCount(0);
        try {
            for (TinTuyenDung tin : service.getAllTinTuyenDung()) {
                String han = tin.getHanNopHoSo() != null ? tin.getHanNopHoSo().format(DATE_FMT) : "";
                model.addRow(new Object[]{
                        tin.getMaTin(),
                        tin.getTieuDe(),
                        tin.getTenPhongBan() != null ? tin.getTenPhongBan() : "",
                        tin.getTenChucVu() != null ? tin.getTenChucVu() : "",
                        han,
                        tin.getSoLuongCanTuyen(),
                        tin.getSoUngVien(),
                        tin.getTrangThaiDisplay()
                });
            }
        } catch (Exception e) {
            TabUtils.showError(this, "Lỗi tải tin tuyển dụng: " + e.getMessage());
        }
    }

    private void dangTin() {
        List<YeuCauTuyenDung> dsYCDaDuyet = service.getYeuCauDaDuyet();
        if (dsYCDaDuyet.isEmpty()) {
            DialogUtil.showWarn(this, "Không có yêu cầu nào đã được duyệt. Vui lòng duyệt yêu cầu trước.");
            return;
        }
        JComboBox<YeuCauTuyenDung> cboYeuCau = new JComboBox<>();
        for (YeuCauTuyenDung yc : dsYCDaDuyet) {
            cboYeuCau.addItem(yc);
        }
        cboYeuCau.setRenderer((list, value, index, sel, focus) -> {
            if (value == null) return new JLabel("");
            String pb = value.getTenPhongBan() != null ? value.getTenPhongBan() : value.getId();
            String cv = value.getTenChucVu() != null ? value.getTenChucVu() : value.getMaChucVu();
            return new JLabel("#" + value.getMaYeuCau() + " - " + pb + " - " + cv);
        });
        JTextField txtTieuDe = new JTextField(25);
        JTextField txtMucLuong = new JTextField(20);
        JTextField txtDiaDiem = new JTextField(20);
        txtTieuDe.setFont(UIFonts.TEXT_NORMAL);
        txtMucLuong.setFont(UIFonts.TEXT_NORMAL);
        txtDiaDiem.setFont(UIFonts.TEXT_NORMAL);
        SpinnerDateModel dm = new SpinnerDateModel(
                java.util.Date.from(LocalDate.now().plusMonths(1).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()),
                null, null, java.util.Calendar.DAY_OF_MONTH);
        JSpinner spinHanNop = new JSpinner(dm);
        spinHanNop.setEditor(new JSpinner.DateEditor(spinHanNop, "dd/MM/yyyy"));
        // Pre-fill deadline from YeuCau when selection changes
        cboYeuCau.addActionListener(ev -> {
            YeuCauTuyenDung sel = (YeuCauTuyenDung) cboYeuCau.getSelectedItem();
            if (sel != null && sel.getHanTuyenDung() != null) {
                spinHanNop.setValue(java.util.Date.from(
                        sel.getHanTuyenDung().atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()));
            }
        });
        // Trigger once for initial selection
        if (!dsYCDaDuyet.isEmpty() && dsYCDaDuyet.get(0).getHanTuyenDung() != null) {
            spinHanNop.setValue(java.util.Date.from(
                    dsYCDaDuyet.get(0).getHanTuyenDung().atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()));
        }
        JPanel form = new JPanel(new GridLayout(5, 2, 8, 8));
        form.add(new JLabel("Yêu cầu tuyển dụng:"));
        form.add(cboYeuCau);
        form.add(new JLabel("Tiêu đề tin:"));
        form.add(txtTieuDe);
        form.add(new JLabel("Mức lương:"));
        form.add(txtMucLuong);
        form.add(new JLabel("Địa điểm:"));
        form.add(txtDiaDiem);
        form.add(new JLabel("Hạn nộp hồ sơ:"));
        form.add(spinHanNop);
        if (JOptionPane.showConfirmDialog(this, form, "Đăng tin tuyển dụng",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
            return;
        }
        String tieuDe = txtTieuDe.getText().trim();
        if (tieuDe.isEmpty()) {
            DialogUtil.showWarn(this, "Vui lòng nhập tiêu đề.");
            return;
        }
        YeuCauTuyenDung selectedYC = (YeuCauTuyenDung) cboYeuCau.getSelectedItem();
        if (selectedYC == null) {
            DialogUtil.showWarn(this, "Vui lòng chọn yêu cầu tuyển dụng.");
            return;
        }
        LocalDate hanNop = ((java.util.Date) spinHanNop.getValue()).toInstant()
                .atZone(java.time.ZoneId.systemDefault()).toLocalDate();
        try {
            TinTuyenDung tin = new TinTuyenDung();
            tin.setTieuDe(tieuDe);
            tin.setMaYeuCau(selectedYC.getMaYeuCau());
            tin.setMucLuong(txtMucLuong.getText().trim());
            tin.setDiaDiem(txtDiaDiem.getText().trim());
            tin.setHanNopHoSo(hanNop);
            KetQua<?> sr = service.taoTin(tin);
            if (sr.isSuccess()) {
                DialogUtil.showSuccess(this, "Đã đăng tin tuyển dụng!");
                load();
            } else {
                TabUtils.showError(this, sr.getMessage());
            }
        } catch (Exception ex) {
            TabUtils.showError(this, "Lỗi đăng tin: " + ex.getMessage());
        }
    }

    private void dongTin() {
        int viewRow = tbl.getSelectedRow();
        if (viewRow < 0) {
            DialogUtil.showWarn(this, "Vui lòng chọn tin.");
            return;
        }
        int maTin = (Integer) model.getValueAt(tbl.convertRowIndexToModel(viewRow), 0);
        if (!DialogUtil.showYesNo(this, "Đóng tin #" + maTin + "?", "Xác nhận")) {
            return;
        }
        try {
            KetQua<?> r = service.dongTin(maTin);
            if (r.isSuccess()) {
                DialogUtil.showSuccess(this, "Đã đóng tin.");
                load();
            } else {
                TabUtils.showError(this, r.getMessage());
            }
        } catch (Exception ex) {
            TabUtils.showError(this, "Lỗi đóng tin: " + ex.getMessage());
        }
    }
}
