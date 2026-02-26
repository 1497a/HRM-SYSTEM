package com.hrm.gui.recruitment;

import com.hrm.model.TinTuyenDung;
import com.hrm.model.UngVien;
import com.hrm.model.YeuCauTuyenDung;
import com.hrm.service.RecruitmentService;
import com.hrm.service.ServiceResult;
import com.hrm.util.UIColors;
import com.hrm.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Panel quản lý tuyển dụng.
 * Tab 1: Yêu cầu tuyển dụng.
 * Tab 2: Tin tuyển dụng.
 * Tab 3: Ứng viên.
 */
public class RecruitmentPanel extends JPanel {

    private final RecruitmentService recruitmentService;

    // Tab 1 - Yêu cầu tuyển dụng
    private JTable tblYeuCau;
    private DefaultTableModel modelYeuCau;
    private JButton btnTaoYeuCau;
    private JButton btnPheDuyet;
    private JButton btnTuChoi;
    private JButton btnLamMoiYC;

    // Tab 2 - Tin tuyển dụng
    private JTable tblTin;
    private DefaultTableModel modelTin;
    private JButton btnDangTin;
    private JButton btnDongTin;
    private JButton btnLamMoiTin;

    // Tab 3 - Ứng viên
    private JTable tblUngVien;
    private DefaultTableModel modelUngVien;
    private JButton btnChuyenTrangThai;
    private JButton btnChuyenNV;
    private JButton btnLamMoiUV;

    private List<YeuCauTuyenDung> danhSachYC;
    private List<TinTuyenDung> danhSachTin;
    private List<UngVien> danhSachUV;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public RecruitmentPanel() {
        this.recruitmentService = RecruitmentService.getInstance();
        setLayout(new BorderLayout());
        setBackground(UIColors.LIGHT_GRAY_BG);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tabbedPane.setBackground(UIColors.WHITE);

        tabbedPane.addTab("Yêu cầu tuyển dụng", buildYeuCauTab());
        tabbedPane.addTab("Tin tuyển dụng", buildTinTab());
        tabbedPane.addTab("Ứng viên", buildUngVienTab());

        add(tabbedPane, BorderLayout.CENTER);

        loadAll();
    }

    // =======================
    // Build Tab 1 - Yêu cầu
    // =======================

    private JPanel buildYeuCauTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(UIColors.LIGHT_GRAY_BG);
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));

        // Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        toolbar.setOpaque(false);

        btnTaoYeuCau = UIHelper.createSuccessButton("Tạo yêu cầu");
        btnPheDuyet = UIHelper.createPrimaryButton("Phê duyệt");
        btnTuChoi = UIHelper.createDangerButton("Từ chối");
        btnLamMoiYC = UIHelper.createDefaultButton("Làm mới");

        btnTaoYeuCau.addActionListener(e -> taoYeuCau());
        btnPheDuyet.addActionListener(e -> pheDuyetYeuCau());
        btnTuChoi.addActionListener(e -> tuChoiYeuCau());
        btnLamMoiYC.addActionListener(e -> loadYeuCau());

        toolbar.add(btnTaoYeuCau);
        toolbar.add(btnPheDuyet);
        toolbar.add(btnTuChoi);
        toolbar.add(btnLamMoiYC);

        // Table
        String[] cols = {"Mã YC", "Vị trí", "Phòng ban", "Số lượng", "Ngày tạo", "Trạng thái"};
        modelYeuCau = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        tblYeuCau = buildTable(modelYeuCau);

        int[] widths = {70, 200, 180, 80, 120, 130};
        applyColWidths(tblYeuCau, widths);
        tblYeuCau.getColumnModel().getColumn(5).setCellRenderer(new StatusCellRenderer());

        JScrollPane scroll = new JScrollPane(tblYeuCau);
        scroll.setBorder(new TitledBorder("Danh sách yêu cầu tuyển dụng"));

        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    // =======================
    // Build Tab 2 - Tin
    // =======================

    private JPanel buildTinTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(UIColors.LIGHT_GRAY_BG);
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));

        // Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        toolbar.setOpaque(false);

        btnDangTin = UIHelper.createSuccessButton("Đăng tin");
        btnDongTin = UIHelper.createDangerButton("Đóng tin");
        btnLamMoiTin = UIHelper.createDefaultButton("Làm mới");

        btnDangTin.addActionListener(e -> dangTin());
        btnDongTin.addActionListener(e -> dongTin());
        btnLamMoiTin.addActionListener(e -> loadTin());

        toolbar.add(btnDangTin);
        toolbar.add(btnDongTin);
        toolbar.add(btnLamMoiTin);

        // Table
        String[] cols = {"Mã tin", "Tiêu đề", "Ngày đăng", "Ngày hết hạn", "Số đơn", "Trạng thái"};
        modelTin = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        tblTin = buildTable(modelTin);

        int[] widths = {70, 280, 120, 130, 80, 130};
        applyColWidths(tblTin, widths);
        tblTin.getColumnModel().getColumn(5).setCellRenderer(new StatusCellRenderer());

        JScrollPane scroll = new JScrollPane(tblTin);
        scroll.setBorder(new TitledBorder("Danh sách tin tuyển dụng"));

        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    // =======================
    // Build Tab 3 - Ứng viên
    // =======================

    private JPanel buildUngVienTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(UIColors.LIGHT_GRAY_BG);
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));

        // Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        toolbar.setOpaque(false);

        btnChuyenTrangThai = UIHelper.createPrimaryButton("Chuyển trạng thái");
        btnChuyenNV = UIHelper.createSuccessButton("Chuyển thành nhân viên");
        btnLamMoiUV = UIHelper.createDefaultButton("Làm mới");

        btnChuyenTrangThai.addActionListener(e -> chuyenTrangThaiUV());
        btnChuyenNV.addActionListener(e -> chuyenUVThanhNV());
        btnLamMoiUV.addActionListener(e -> loadUngVien());

        toolbar.add(btnChuyenTrangThai);
        toolbar.add(btnChuyenNV);
        toolbar.add(btnLamMoiUV);

        // Table
        String[] cols = {"Mã UV", "Họ tên", "Email", "Điện thoại", "Vị trí ứng tuyển", "Ngày nộp", "Trạng thái"};
        modelUngVien = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        tblUngVien = buildTable(modelUngVien);

        int[] widths = {70, 160, 200, 120, 200, 110, 130};
        applyColWidths(tblUngVien, widths);
        tblUngVien.getColumnModel().getColumn(6).setCellRenderer(new StatusCellRenderer());

        JScrollPane scroll = new JScrollPane(tblUngVien);
        scroll.setBorder(new TitledBorder("Danh sách ứng viên"));

        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    // =======================
    // Data loading
    // =======================

    private void loadAll() {
        loadYeuCau();
        loadTin();
        loadUngVien();
    }

    private void loadYeuCau() {
        modelYeuCau.setRowCount(0);
        try {
            danhSachYC = recruitmentService.getAllYeuCau();
            for (YeuCauTuyenDung yc : danhSachYC) {
                String ngayTao = yc.getHanTuyenDung() != null ? yc.getHanTuyenDung().format(DATE_FORMAT) : "";
                modelYeuCau.addRow(new Object[]{
                        yc.getMaYeuCau(),
                        yc.getTenChucVu() != null ? yc.getTenChucVu() : yc.getMaChucVu(),
                        yc.getTenPhongBan() != null ? yc.getTenPhongBan() : yc.getMaPhongBan(),
                        yc.getSoLuong(),
                        ngayTao,
                        yc.getTrangThaiDisplay()
                });
            }
        } catch (Exception ex) {
            showError("Lỗi tải yêu cầu tuyển dụng: " + ex.getMessage());
        }
    }

    private void loadTin() {
        modelTin.setRowCount(0);
        try {
            danhSachTin = recruitmentService.getAllTinTuyenDung();
            for (TinTuyenDung tin : danhSachTin) {
                String ngayDang = tin.getNgayTao() != null ? tin.getNgayTao().format(DATE_FORMAT) : "";
                String ngayHetHan = tin.getHanNopHoSo() != null ? tin.getHanNopHoSo().format(DATE_FORMAT) : "";
                modelTin.addRow(new Object[]{
                        tin.getMaTin(),
                        tin.getTieuDe(),
                        ngayDang,
                        ngayHetHan,
                        tin.getSoUngVien(),
                        tin.getTrangThaiDisplay()
                });
            }
        } catch (Exception ex) {
            showError("Lỗi tải tin tuyển dụng: " + ex.getMessage());
        }
    }

    private void loadUngVien() {
        modelUngVien.setRowCount(0);
        try {
            danhSachUV = recruitmentService.getAllUngVien();
            for (UngVien uv : danhSachUV) {
                String ngayNop = uv.getNgayTao() != null ? uv.getNgayTao().format(DATE_FORMAT) : "";
                modelUngVien.addRow(new Object[]{
                        uv.getMaUngVien(),
                        uv.getHoTen(),
                        uv.getEmail(),
                        uv.getDienThoai(),
                        uv.getTenTin() != null ? uv.getTenTin() : "Mã tin: " + uv.getMaTin(),
                        ngayNop,
                        uv.getTrangThaiDisplay()
                });
            }
        } catch (Exception ex) {
            showError("Lỗi tải danh sách ứng viên: " + ex.getMessage());
        }
    }

    // =======================
    // Actions - Tab 1
    // =======================

    private void taoYeuCau() {
        JTextField txtViTri = new JTextField(20);
        JTextField txtPhongBan = new JTextField(20);
        JSpinner spinSoLuong = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
        JTextField txtLyDo = new JTextField(20);

        txtViTri.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtPhongBan.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtLyDo.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JPanel form = new JPanel(new GridLayout(4, 2, 8, 8));
        form.add(new JLabel("Vị trí tuyển dụng:"));
        form.add(txtViTri);
        form.add(new JLabel("Phòng ban (mã):"));
        form.add(txtPhongBan);
        form.add(new JLabel("Số lượng:"));
        form.add(spinSoLuong);
        form.add(new JLabel("Lý do:"));
        form.add(txtLyDo);

        int result = JOptionPane.showConfirmDialog(this, form,
                "Tạo yêu cầu tuyển dụng", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;

        String viTri = txtViTri.getText().trim();
        String phongBan = txtPhongBan.getText().trim();
        if (viTri.isEmpty() || phongBan.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng nhập vị trí và phòng ban.",
                    "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            YeuCauTuyenDung yc = new YeuCauTuyenDung();
            yc.setMaChucVu(viTri);
            yc.setMaPhongBan(phongBan);
            yc.setSoLuong((int) spinSoLuong.getValue());
            yc.setLyDo(txtLyDo.getText().trim());
            yc.setTrangThai("cho_duyet");

            ServiceResult<?> sr = recruitmentService.taoYeuCau(yc);
            if (sr.isSuccess()) {
                JOptionPane.showMessageDialog(this, "Tạo yêu cầu thành công!",
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
                loadYeuCau();
            } else {
                showError(sr.getMessage());
            }
        } catch (Exception ex) {
            showError("Lỗi tạo yêu cầu: " + ex.getMessage());
        }
    }

    private void pheDuyetYeuCau() {
        int row = tblYeuCau.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn yêu cầu cần phê duyệt.",
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int maYC = (int) modelYeuCau.getValueAt(row, 0);
        try {
            ServiceResult<?> sr = recruitmentService.duyetYeuCau(maYC);
            if (sr.isSuccess()) {
                JOptionPane.showMessageDialog(this, "Đã phê duyệt yêu cầu.",
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
                loadYeuCau();
            } else {
                showError(sr.getMessage());
            }
        } catch (Exception ex) {
            showError("Lỗi phê duyệt: " + ex.getMessage());
        }
    }

    private void tuChoiYeuCau() {
        int row = tblYeuCau.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn yêu cầu cần từ chối.",
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int maYC = (int) modelYeuCau.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Từ chối yêu cầu tuyển dụng #" + maYC + "?",
                "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            ServiceResult<?> sr = recruitmentService.tuChoiYeuCau(maYC);
            if (sr.isSuccess()) {
                JOptionPane.showMessageDialog(this, "Đã từ chối yêu cầu.",
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
                loadYeuCau();
            } else {
                showError(sr.getMessage());
            }
        } catch (Exception ex) {
            showError("Lỗi từ chối: " + ex.getMessage());
        }
    }

    // =======================
    // Actions - Tab 2
    // =======================

    private void dangTin() {
        JTextField txtTieuDe = new JTextField(25);
        JSpinner spinMaYC = new JSpinner(new SpinnerNumberModel(1, 1, 9999, 1));
        JTextField txtMucLuong = new JTextField(20);

        txtTieuDe.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtMucLuong.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JPanel form = new JPanel(new GridLayout(3, 2, 8, 8));
        form.add(new JLabel("Tiêu đề tin:"));
        form.add(txtTieuDe);
        form.add(new JLabel("Mã yêu cầu (maYC):"));
        form.add(spinMaYC);
        form.add(new JLabel("Mức lương:"));
        form.add(txtMucLuong);

        int result = JOptionPane.showConfirmDialog(this, form,
                "Đăng tin tuyển dụng", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;

        String tieuDe = txtTieuDe.getText().trim();
        if (tieuDe.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập tiêu đề.",
                    "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            TinTuyenDung tin = new TinTuyenDung();
            tin.setTieuDe(tieuDe);
            tin.setMaYeuCau((int) spinMaYC.getValue());
            tin.setMucLuong(txtMucLuong.getText().trim());
            tin.setTrangThai("dang_tuyen");

            ServiceResult<?> sr = recruitmentService.taoTin(tin);
            if (sr.isSuccess()) {
                JOptionPane.showMessageDialog(this, "Đã đăng tin tuyển dụng!",
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
                loadTin();
            } else {
                showError(sr.getMessage());
            }
        } catch (Exception ex) {
            showError("Lỗi đăng tin: " + ex.getMessage());
        }
    }

    private void dongTin() {
        int row = tblTin.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn tin cần đóng.",
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int maTin = (int) modelTin.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Đóng tin tuyển dụng #" + maTin + "?",
                "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            ServiceResult<?> sr = recruitmentService.dongTin(maTin);
            if (sr.isSuccess()) {
                JOptionPane.showMessageDialog(this, "Đã đóng tin tuyển dụng.",
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
                loadTin();
            } else {
                showError(sr.getMessage());
            }
        } catch (Exception ex) {
            showError("Lỗi đóng tin: " + ex.getMessage());
        }
    }

    // =======================
    // Actions - Tab 3
    // =======================

    private void chuyenTrangThaiUV() {
        int row = tblUngVien.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn ứng viên.",
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int maUV = (int) modelUngVien.getValueAt(row, 0);

        String[] options = {"moi", "dang_phong_van", "trung_tuyen", "tu_choi"};
        String[] displayOptions = {"Mới", "Đang phỏng vấn", "Trúng tuyển", "Từ chối"};

        JComboBox<String> cbo = new JComboBox<>(displayOptions);
        cbo.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        int result = JOptionPane.showConfirmDialog(this,
                new Object[]{"Chọn trạng thái mới:", cbo},
                "Chuyển trạng thái ứng viên", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;

        String trangThaiMoi = options[cbo.getSelectedIndex()];

        try {
            ServiceResult<?> sr = recruitmentService.capNhatTrangThaiUV(maUV, trangThaiMoi);
            if (sr.isSuccess()) {
                JOptionPane.showMessageDialog(this, "Đã cập nhật trạng thái ứng viên.",
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
                loadUngVien();
            } else {
                showError(sr.getMessage());
            }
        } catch (Exception ex) {
            showError("Lỗi cập nhật: " + ex.getMessage());
        }
    }

    private void chuyenUVThanhNV() {
        int row = tblUngVien.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn ứng viên.",
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String trangThai = (String) modelUngVien.getValueAt(row, 6);
        if (!"Trúng tuyển".equals(trangThai)) {
            JOptionPane.showMessageDialog(this,
                    "Chỉ có thể chuyển ứng viên có trạng thái 'Trúng tuyển' thành nhân viên.",
                    "Không hợp lệ", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int maUV = (int) modelUngVien.getValueAt(row, 0);
        String hoTen = (String) modelUngVien.getValueAt(row, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Chuyển ứng viên \"" + hoTen + "\" thành nhân viên chính thức?",
                "Xác nhận", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            ServiceResult<?> sr = recruitmentService.chuyenUVThanhNV(maUV);
            if (sr.isSuccess()) {
                JOptionPane.showMessageDialog(this,
                        "Đã chuyển ứng viên \"" + hoTen + "\" thành nhân viên thành công!",
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
                loadUngVien();
            } else {
                showError(sr.getMessage());
            }
        } catch (Exception ex) {
            showError("Lỗi chuyển ứng viên: " + ex.getMessage());
        }
    }

    // =======================
    // Helpers
    // =======================

    private JTable buildTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setRowHeight(28);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(UIColors.PRIMARY_PURPLE);
        table.getTableHeader().setForeground(UIColors.WHITE);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setSelectionBackground(UIColors.LIGHT_PURPLE);
        table.setSelectionForeground(UIColors.TEXT_DARK);
        return table;
    }

    private void applyColWidths(JTable table, int[] widths) {
        for (int i = 0; i < widths.length && i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Custom renderer for trạng thái columns.
     */
    private static class StatusCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int col) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
            setHorizontalAlignment(SwingConstants.CENTER);
            if (!isSelected && value != null) {
                String v = value.toString();
                if (v.contains("Đã duyệt") || v.contains("Trúng tuyển") || v.contains("Đang tuyển")) {
                    c.setForeground(UIColors.SUCCESS_GREEN);
                } else if (v.contains("Từ chối") || v.contains("Đã đóng")) {
                    c.setForeground(UIColors.DANGER_RED);
                } else if (v.contains("Chờ duyệt") || v.contains("Đang phỏng vấn") || v.contains("Tạm dừng")) {
                    c.setForeground(UIColors.WARNING_YELLOW);
                } else {
                    c.setForeground(UIColors.INFO_BLUE);
                }
                ((JLabel) c).setFont(new Font("Segoe UI", Font.BOLD, 12));
            }
            return c;
        }
    }
}
