package com.hrm.gui.recruitment;

import com.hrm.bus.KetQua;
import com.hrm.bus.TuyenDungBUS;
import com.hrm.model.RecruitmentStatus;
import com.hrm.model.UngVien;
import com.hrm.util.SessionContext;
import com.hrm.util.UIColors;
import com.hrm.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

import static javax.swing.RowSorter.SortKey;
import static javax.swing.SortOrder.ASCENDING;

class TabUngVienPanel extends JPanel {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String[] TRANG_THAI_CODES = {
            RecruitmentStatus.UngVien.MOI,
            RecruitmentStatus.UngVien.DANG_PHONG_VAN,
            RecruitmentStatus.UngVien.TRUNG_TUYEN,
            RecruitmentStatus.UngVien.TU_CHOI
    };

    private final TuyenDungBUS service;
    private JTable tbl;
    private DefaultTableModel model;
    private JButton btnTaoUV;
    private JButton btnChuyenTrangThai;
    private JButton btnChuyenNV;

    TabUngVienPanel(TuyenDungBUS service) {
        this.service = service;
        setLayout(new BorderLayout(8, 8));
        setBackground(UIColors.LIGHT_GRAY_BG);
        setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        toolbar.setOpaque(false);

        btnTaoUV = UIHelper.createPrimaryButton("+ Tạo ứng viên");
        btnChuyenTrangThai = UIHelper.createPrimaryButton("Chuyển trạng thái");
        btnChuyenNV = UIHelper.createSuccessButton("Chuyển thành NV");
        JButton btnLamMoi = UIHelper.createDefaultButton("Làm mới");

        btnTaoUV.addActionListener(e -> taoUngVien());
        btnChuyenTrangThai.addActionListener(e -> chuyenTrangThai());
        btnChuyenNV.addActionListener(e -> chuyenThanhNV());
        btnLamMoi.addActionListener(e -> load());

        JComboBox<String> cboTrangThai = new JComboBox<>(new String[]{
                "Tất cả", "Mới", "Đang phỏng vấn", "Trúng tuyển", "Từ chối", "Đã chuyển thành nhân viên"
        });

        toolbar.add(btnTaoUV);
        toolbar.add(btnChuyenTrangThai);
        toolbar.add(btnChuyenNV);
        toolbar.add(btnLamMoi);
        toolbar.add(new JLabel("Trạng thái:"));
        toolbar.add(cboTrangThai);

        String[] cols = {"Mã UV", "Họ tên", "Email", "Điện thoại", "Vị trí", "Ngày nộp", "Trạng thái"};
        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int col) {
                return col == 0 ? Integer.class : String.class;
            }
        };

        tbl = TabUtils.buildTable(model);
        TabUtils.applyColWidths(tbl, new int[]{70, 180, 220, 130, 220, 110, 140});
        tbl.getColumnModel().getColumn(6).setCellRenderer(new RecruitmentStatusRenderer());

        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        tbl.setRowSorter(sorter);
        for (int i = 0; i < model.getColumnCount(); i++) {
            sorter.setSortable(i, false);
        }
        sorter.setSortable(0, true);
        sorter.setSortable(1, true);
        sorter.setSortable(4, true);
        sorter.setComparator(0, Comparator.comparingInt(a -> (Integer) a));
        sorter.setComparator(1, UIHelper.vietnameseNameComparator());
        sorter.setComparator(4, Comparator.comparing(String::toString, String.CASE_INSENSITIVE_ORDER));
        sorter.setComparator(5, TabUtils.dateComparator());
        sorter.setSortKeys(List.of(new SortKey(0, ASCENDING)));
        UIHelper.attachStatusFilter(sorter, cboTrangThai, 6);

        JScrollPane scroll = new JScrollPane(tbl);
        scroll.setBorder(new TitledBorder("Danh sách ứng viên"));

        add(toolbar, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);

        boolean canManage = SessionContext.getInstance().coQuyen("RECRUITMENT_MANAGE");
        btnTaoUV.setVisible(canManage);
        btnChuyenTrangThai.setVisible(canManage);
        btnChuyenNV.setVisible(canManage);

        load();
    }

    void load() {
        model.setRowCount(0);
        try {
            for (UngVien uv : service.getAllUngVien()) {
                String nop = uv.getNgayTao() != null ? uv.getNgayTao().format(DATE_FMT) : "";
                String viTri = uv.getTenTin() != null && !uv.getTenTin().isBlank()
                        ? uv.getTenTin() : "[Chưa có tin]";
                model.addRow(new Object[]{
                        uv.getMaUngVien(),
                        uv.getHoTen(),
                        uv.getEmail(),
                        uv.getDienThoai(),
                        viTri,
                        nop,
                        uv.getTrangThaiDisplay()
                });
            }
        } catch (Exception e) {
            TabUtils.showError(this, "Lỗi tải ứng viên: " + e.getMessage());
        }
    }

    private void taoUngVien() {
        HopThoaiTaoUngVien dialog = new HopThoaiTaoUngVien(SwingUtilities.getWindowAncestor(this), service);
        dialog.setVisible(true);
        if (dialog.isThanhCong()) {
            load();
        }
    }

    private void chuyenTrangThai() {
        int viewRow = tbl.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn ứng viên.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int maUV = (Integer) model.getValueAt(tbl.convertRowIndexToModel(viewRow), 0);

        String[] labels = {"Mới", "Đang phỏng vấn", "Trúng tuyển", "Từ chối"};
        JComboBox<String> cbo = new JComboBox<>(labels);
        cbo.setFont(com.hrm.util.UIFonts.TEXT_MEDIUM);

        if (JOptionPane.showConfirmDialog(this, new Object[]{"Trạng thái mới:", cbo},
                "Chuyển trạng thái ứng viên", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            KetQua<?> r = service.capNhatTrangThaiUV(maUV, TRANG_THAI_CODES[cbo.getSelectedIndex()]);
            if (r.isSuccess()) {
                JOptionPane.showMessageDialog(this, "Đã cập nhật trạng thái.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                load();
            } else {
                TabUtils.showError(this, r.getMessage());
            }
        } catch (Exception ex) {
            TabUtils.showError(this, "Lỗi cập nhật trạng thái: " + ex.getMessage());
        }
    }

    private void chuyenThanhNV() {
        int viewRow = tbl.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn ứng viên.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int maUV = (Integer) model.getValueAt(tbl.convertRowIndexToModel(viewRow), 0);

        KetQua<String> preview = service.taoThongDiepXacNhanChuyenUVThanhNV(maUV);
        if (!preview.isSuccess()) {
            TabUtils.showError(this, preview.getMessage());
            return;
        }

        if (JOptionPane.showConfirmDialog(this, preview.getData(), "Xác nhận chuyển thành nhân viên",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            KetQua<?> r = service.chuyenUVThanhNV(maUV);
            if (r.isSuccess()) {
                JOptionPane.showMessageDialog(this, "Đã chuyển thành nhân viên thành công!\n" + r.getMessage(),
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
                load();
            } else {
                TabUtils.showError(this, r.getMessage());
            }
        } catch (Exception ex) {
            TabUtils.showError(this, "Lỗi chuyển thành nhân viên: " + ex.getMessage());
        }
    }
}
