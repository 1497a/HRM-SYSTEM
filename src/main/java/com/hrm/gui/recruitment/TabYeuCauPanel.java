package com.hrm.gui.recruitment;

import com.hrm.bus.ChucVuBUS;
import com.hrm.bus.KetQua;
import com.hrm.bus.PhongBanBUS;
import com.hrm.bus.TuyenDungBUS;
import com.hrm.model.ChucVu;
import com.hrm.model.PhongBan;
import com.hrm.model.YeuCauTuyenDung;
import com.hrm.util.PermissionCodes;
import com.hrm.util.UIFonts;
import com.hrm.util.SessionContext;
import com.hrm.util.UIColors;
import com.hrm.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

import static javax.swing.RowSorter.SortKey;
import static javax.swing.SortOrder.ASCENDING;

class TabYeuCauPanel extends JPanel {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final TuyenDungBUS service;
    private JTable tbl;
    private DefaultTableModel model;
    private JButton btnTao;
    private JButton btnPheDuyet;
    private JButton btnTuChoi;
    TabYeuCauPanel(TuyenDungBUS service) {
        this.service = service;
        setLayout(new BorderLayout(8, 8));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(12, 12, 12, 12));
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        toolbar.setOpaque(false);
        btnTao = UIHelper.createPrimaryButton("Tạo yêu cầu");
        btnPheDuyet = UIHelper.createSuccessButton("Phê duyệt");
        btnTuChoi = UIHelper.createDangerButton("Từ chối");
        btnTao.addActionListener(e -> taoYeuCau());
        btnPheDuyet.addActionListener(e -> pheDuyet());
        btnTuChoi.addActionListener(e -> tuChoi());
        JComboBox<String> cboTrangThai = new JComboBox<>(
                new String[]{"Tất cả", "Chờ duyệt", "Đã duyệt", "Từ chối", "Đã tuyển đủ"});
        JButton btnLamMoi = new JButton("Làm mới");
        btnLamMoi.addActionListener(e -> load());
        toolbar.add(btnTao);
        toolbar.add(btnPheDuyet);
        toolbar.add(btnTuChoi);
        toolbar.add(new JLabel("Trạng thái:"));
        toolbar.add(cboTrangThai);
        toolbar.add(btnLamMoi);
        String[] cols = {"Mã YC", "Vị trí", "Phòng ban", "Số lượng", "Hạn tuyển dụng", "Trạng thái"};
        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
            @Override
            public Class<?> getColumnClass(int col) {
                return (col == 0 || col == 3) ? Integer.class : String.class;
            }
        };
        tbl = TabUtils.buildTable(model);
        TabUtils.applyColWidths(tbl, new int[]{70, 220, 180, 80, 120, 130});
        tbl.getColumnModel().getColumn(5).setCellRenderer(new RecruitmentStatusRenderer());
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        tbl.setRowSorter(sorter);
        sorter.setComparator(0, Comparator.comparingInt(a -> (Integer) a));
        sorter.setComparator(3, Comparator.comparingInt(a -> (Integer) a));
        sorter.setComparator(4, TabUtils.dateComparator());
        sorter.setSortKeys(List.of(new SortKey(0, ASCENDING)));
        UIHelper.attachStatusFilter(sorter, cboTrangThai, 5);
        JScrollPane scroll = new JScrollPane(tbl);
        scroll.setBorder(new TitledBorder("Danh sách yêu cầu tuyển dụng"));
        add(toolbar, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        setupPermissions();
        load();
    }

    private void setupPermissions() {
        SessionContext sc = SessionContext.getInstance();
        boolean canRequest = sc.hasPermission(PermissionCodes.RECRUITMENT_REQUEST) || sc.hasPermission(PermissionCodes.RECRUITMENT_MANAGE);
        boolean canManage = sc.hasPermission(PermissionCodes.RECRUITMENT_MANAGE);
        btnTao.setVisible(canRequest);
        btnPheDuyet.setVisible(canManage);
        btnTuChoi.setVisible(canManage);
    }

    void load() {
        model.setRowCount(0);
        try {
            for (YeuCauTuyenDung yc : service.getAllYeuCau()) {
                String han = yc.getHanTuyenDung() != null ? yc.getHanTuyenDung().format(DATE_FMT) : "";
                model.addRow(new Object[]{
                        yc.getMaYeuCau(),
                        yc.getTenChucVu() != null ? yc.getTenChucVu() : (yc.getMaChucVu() != null ? yc.getMaChucVu() : ""),
                        yc.getTenPhongBan() != null ? yc.getTenPhongBan() : (yc.getId() != null ? yc.getId() : ""),
                        yc.getSoLuong(),
                        han,
                        yc.getTrangThaiDisplay()
                });
            }
        } catch (Exception e) {
            TabUtils.showError(this, "Lỗi tải yêu cầu tuyển dụng: " + e.getMessage());
        }
    }

    private void taoYeuCau() {
        List<PhongBan> dsPhongBan = new PhongBanBUS().getActiveDepartments();
        List<ChucVu> dsChucVu = new ChucVuBUS().getRecruitablePositions();
        JComboBox<PhongBan> cboPhongBan = new JComboBox<>();
        for (PhongBan pb : dsPhongBan) {
            // Bo qua phong ban goc (Cong ty) - chi cho chon phong ban thuc su
            if (pb.getPhongBanChaId() != null) {
                cboPhongBan.addItem(pb);
            }
        }
        cboPhongBan.setRenderer((list, v, i, s, f) -> new JLabel(v != null ? v.getTenPhongBan() : ""));
        JComboBox<ChucVu> cboChucVu = new JComboBox<>();
        for (ChucVu cv : dsChucVu) {
            cboChucVu.addItem(cv);
        }
        cboChucVu.setRenderer((list, v, i, s, f) -> new JLabel(v != null ? v.getTenChucVu() : ""));
        JSpinner spinSoLuong = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
        SpinnerDateModel dm = new SpinnerDateModel(
                java.util.Date.from(LocalDate.now().plusMonths(1).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()),
                null, null, java.util.Calendar.DAY_OF_MONTH);
        JSpinner spinHan = new JSpinner(dm);
        spinHan.setEditor(new JSpinner.DateEditor(spinHan, "dd/MM/yyyy"));
        JTextField txtLyDo = new JTextField(20);
        txtLyDo.setFont(com.hrm.util.UIFonts.TEXT_NORMAL);
        JPanel form = new JPanel(new GridLayout(5, 2, 8, 8));
        form.add(new JLabel("Phòng ban (*):"));
        form.add(cboPhongBan);
        form.add(new JLabel("Chức vụ / Vị trí (*):"));
        form.add(cboChucVu);
        form.add(new JLabel("Số lượng:"));
        form.add(spinSoLuong);
        form.add(new JLabel("Hạn tuyển dụng:"));
        form.add(spinHan);
        form.add(new JLabel("Lý do:"));
        form.add(txtLyDo);
        if (JOptionPane.showConfirmDialog(this, form, "Tạo yêu cầu tuyển dụng",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
            return;
        }
        PhongBan pb = (PhongBan) cboPhongBan.getSelectedItem();
        ChucVu cv = (ChucVu) cboChucVu.getSelectedItem();
        if (pb == null || cv == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn phòng ban và chức vụ.", "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        LocalDate hanTuyenDung = ((java.util.Date) spinHan.getValue()).toInstant()
                .atZone(java.time.ZoneId.systemDefault()).toLocalDate();
        try {
            YeuCauTuyenDung yc = new YeuCauTuyenDung();
            yc.setId(pb.getId());
            yc.setMaChucVu(cv.getId());
            yc.setSoLuong((int) spinSoLuong.getValue());
            yc.setHanTuyenDung(hanTuyenDung);
            yc.setLyDo(txtLyDo.getText().trim());
            KetQua<?> sr = service.taoYeuCau(yc);
            if (sr.isSuccess()) {
                JOptionPane.showMessageDialog(this, "Tạo yêu cầu thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                load();
            } else {
                TabUtils.showError(this, sr.getMessage());
            }
        } catch (Exception ex) {
            TabUtils.showError(this, "Lỗi tạo yêu cầu: " + ex.getMessage());
        }
    }

    private void pheDuyet() {
        int viewRow = tbl.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn yêu cầu.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int maYC = (Integer) model.getValueAt(tbl.convertRowIndexToModel(viewRow), 0);
        try {
            KetQua<?> r = service.duyetYeuCau(maYC);
            if (r.isSuccess()) {
                JOptionPane.showMessageDialog(this, "Đã phê duyệt.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                load();
            } else {
                TabUtils.showError(this, r.getMessage());
            }
        } catch (Exception ex) {
            TabUtils.showError(this, "Lỗi phê duyệt: " + ex.getMessage());
        }
    }

    private void tuChoi() {
        int viewRow = tbl.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn yêu cầu.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int maYC = (Integer) model.getValueAt(tbl.convertRowIndexToModel(viewRow), 0);
        if (JOptionPane.showConfirmDialog(this, "Từ chối yêu cầu #" + maYC + "?", "Xác nhận",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            KetQua<?> r = service.tuChoiYeuCau(maYC);
            if (r.isSuccess()) {
                JOptionPane.showMessageDialog(this, "Đã từ chối.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                load();
            } else {
                TabUtils.showError(this, r.getMessage());
            }
        } catch (Exception ex) {
            TabUtils.showError(this, "Lỗi từ chối: " + ex.getMessage());
        }
    }
}
