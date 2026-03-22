package com.hrm.gui.recruitment;

import com.hrm.bus.TuyenDungBUS;
import com.hrm.model.UngVien;
import com.hrm.util.DialogUtil;
import com.hrm.util.PermissionCodes;
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
    private final TuyenDungBUS service;
    private JTable tbl;
    private DefaultTableModel model;
    private JButton btnTaoUV;
    TabUngVienPanel(TuyenDungBUS service) {
        this.service = service;
        setLayout(new BorderLayout(8, 8));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(12, 12, 12, 12));
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        toolbar.setOpaque(false);
        btnTaoUV = UIHelper.createPrimaryButton("+ Tạo ứng viên");
        JButton btnXemChiTiet = UIHelper.createDefaultButton("Xem chi tiết");
        JButton btnLamMoi = UIHelper.createDefaultButton("Làm mới");
        btnTaoUV.addActionListener(e -> taoUngVien());
        btnXemChiTiet.addActionListener(e -> xemChiTiet());
        btnLamMoi.addActionListener(e -> load());
        JComboBox<String> cboTrangThai = new JComboBox<>(new String[]{
                "Tất cả", "Mới", "Đang phỏng vấn", "Trúng tuyển", "Từ chối", "Đã chuyển thành nhân viên"
        });
        toolbar.add(btnTaoUV);
        toolbar.add(btnXemChiTiet);
        toolbar.add(btnLamMoi);
        toolbar.add(new JLabel("Trạng thái:"));
        toolbar.add(cboTrangThai);
        String[] cols = {"Mã UV", "Họ tên", "Email", "Điện thoại", "Vị trí", "Ngày nộp", "Trạng thái"};
        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
            @Override
            public Class<?> getColumnClass(int col) {
                return col == 0 ? Integer.class : String.class;
            }
        };
        tbl = TabUtils.buildTable(model);
        TabUtils.applyColWidths(tbl, new int[]{70, 180, 220, 130, 250, 110, 140});
        tbl.getColumnModel().getColumn(6).setCellRenderer(new RecruitmentStatusRenderer());
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        tbl.setRowSorter(sorter);
        for (int i = 0; i < model.getColumnCount(); i++) sorter.setSortable(i, false);
        sorter.setSortable(0, true);
        sorter.setSortable(1, true);
        sorter.setSortable(4, true);
        sorter.setComparator(0, Comparator.comparingInt(a -> (Integer) a));
        sorter.setComparator(1, UIHelper.vietnameseNameComparator());
        sorter.setComparator(4, Comparator.comparing(String::toString, String.CASE_INSENSITIVE_ORDER));
        sorter.setComparator(5, TabUtils.dateComparator());
        sorter.setSortKeys(List.of(new SortKey(0, ASCENDING)));
        UIHelper.attachStatusFilter(sorter, cboTrangThai, 6);
        // Mở chi tiết khi double-click
        tbl.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) xemChiTiet();
            }
        });
        JScrollPane scroll = new JScrollPane(tbl);
        scroll.setBorder(new TitledBorder("Danh sách ứng viên"));
        add(toolbar, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        btnTaoUV.setVisible(SessionContext.getInstance().hasPermission(PermissionCodes.RECRUITMENT_CANDIDATE_CREATE));
        load();
    }

    void load() {
        model.setRowCount(0);
        try {
            for (UngVien uv : service.getAllUngVien()) {
                String nop = uv.getNgayTao() != null ? uv.getNgayTao().format(DATE_FMT) : "";
                String pb = uv.getTenPhongBan();
                String cv = uv.getTenChucVu();
                String viTri = (pb != null && !pb.isBlank() ? pb : "") +
                        (pb != null && !pb.isBlank() && cv != null && !cv.isBlank() ? " - " : "") +
                        (cv != null && !cv.isBlank() ? cv : "");
                if (viTri.isBlank()) viTri = "[Chưa xác định]";
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
        if (dialog.isThanhCong()) load();
    }

    private void xemChiTiet() {
        int viewRow = tbl.getSelectedRow();
        if (viewRow < 0) {
            DialogUtil.showWarn(this, "Vui lòng chọn ứng viên.");
            return;
        }
        int maUV = (Integer) model.getValueAt(tbl.convertRowIndexToModel(viewRow), 0);
        HopThoaiChiTietUngVien dialog = new HopThoaiChiTietUngVien(
                SwingUtilities.getWindowAncestor(this), service, maUV);
        dialog.setVisible(true);
        if (dialog.isDaThayDoi()) load();
    }
}
