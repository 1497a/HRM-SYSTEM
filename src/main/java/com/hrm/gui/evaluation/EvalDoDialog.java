package com.hrm.gui.evaluation;

import com.hrm.model.TieuChiDanhGia;
import com.hrm.model.ChiTietDanhGia;
import com.hrm.model.DanhGiaHieuSuat;
import com.hrm.model.TaiKhoan;
import com.hrm.model.BoNhiem;
import com.hrm.bus.DanhGiaBUS;
import com.hrm.bus.XacThucBUS;
import com.hrm.dao.BoNhiemDAO;
import com.hrm.util.SessionContext;
import com.hrm.util.UIColors;
import com.hrm.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Dialog Đánh giá nhân viên.
 * Quan ly chon nhan vien (co hien thi chuc vu), nhap diem theo tieu chi, xem diem du kien.
 */
public class EvalDoDialog extends JDialog {

    private final DanhGiaBUS evalService;
    private final TaiKhoan currentUser;
    private final int cycleId;
    private final String cycleName;
    private final List<TieuChiDanhGia> cycleCriteria;

    private JComboBox<NhanVienItem> cboEmployee;
    private JLabel lblChucVu;
    private JPanel criteriaPanel;
    private final Map<Integer, JSpinner> scoreSpinners = new HashMap<>();
    private final Map<Integer, JTextArea> commentFields = new HashMap<>();
    private JTextArea txtNhanXetChung;
    private JLabel lblDiemDuKien;
    private JButton btnLuu;
    private JButton btnHuy;

    private boolean successful = false;

    public EvalDoDialog(Frame parent, int cycleId, String cycleName) {
        super(parent, "Đánh giá nhân viên", true);
        this.evalService  = DanhGiaBUS.getInstance();
        this.currentUser  = SessionContext.getInstance().getCurrentUser();
        this.cycleId   = cycleId;
        this.cycleName = cycleName;
        this.cycleCriteria = evalService.getCriteriaByCycle(cycleId);

        initComponents();
        setupLayout();

        setSize(720, 680);
        setMinimumSize(new Dimension(680, 580));
        setLocationRelativeTo(parent);
    }

    // â”€â”€ init â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    private void initComponents() {
        // Employee combo â€” show hoTen + tenChucVu, store maNV
        cboEmployee = new JComboBox<>();
        cboEmployee.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        loadEmployees();

        lblChucVu = new JLabel(" ");
        lblChucVu.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblChucVu.setForeground(Color.GRAY);
        cboEmployee.addActionListener(e -> updateChucVuLabel());

        // Criteria
        criteriaPanel = new JPanel();
        criteriaPanel.setLayout(new BoxLayout(criteriaPanel, BoxLayout.Y_AXIS));
        criteriaPanel.setBackground(UIColors.WHITE);

        for (TieuChiDanhGia c : cycleCriteria) {
            criteriaPanel.add(buildCriteriaRow(c));
            criteriaPanel.add(Box.createVerticalStrut(6));
        }

        // General comment
        txtNhanXetChung = new JTextArea(3, 40);
        txtNhanXetChung.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtNhanXetChung.setLineWrap(true);
        txtNhanXetChung.setWrapStyleWord(true);

        // Preview
        lblDiemDuKien = new JLabel("Điểm dự kiến: 0.00 - Xếp loại: -");
        lblDiemDuKien.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblDiemDuKien.setForeground(new Color(0, 100, 180));

        // Buttons
        btnLuu  = UIHelper.createSuccessButton("Lưu đánh giá");
        btnHuy  = UIHelper.createDefaultButton("Hủy");
        btnLuu.setPreferredSize(new Dimension(140, 38));
        btnHuy.setPreferredSize(new Dimension(90, 38));
        btnLuu.addActionListener(e -> luuDanhGia());
        btnHuy.addActionListener(e -> dispose());

        if (cycleCriteria.isEmpty()) {
            btnLuu.setEnabled(false);
            JOptionPane.showMessageDialog(this,
                    "Đợt đánh giá này chưa được cấu hình tiêu chí. Vui lòng cấu hình trước khi đánh giá.",
                    "Thông báo",
                    JOptionPane.WARNING_MESSAGE);
        }

        // Initial preview
        updatePreview();
    }

    private void loadEmployees() {
        BoNhiemDAO boNhiemDAO = BoNhiemDAO.getInstance();
        List<TaiKhoan> users = XacThucBUS.getInstance().getAllUsers();

        // Get list of employees already evaluated in this cycle to prevent duplicates
        List<String> alreadyEvaluated = evalService.getEvaluatedMaNVInCycle(cycleId);

        for (TaiKhoan user : users) {
            if (user.getNhanVienId() == null || user.getNhanVienId().isEmpty()) continue;
            if (user.getId() == currentUser.getId()) continue;  // can't self-evaluate
            if (!user.isHoatDong()) continue;

            String maNV = user.getNhanVienId();

            // Skip employees already evaluated in this cycle
            if (alreadyEvaluated.contains(maNV)) continue;

            String hoTen = user.getHoTen() != null ? user.getHoTen() : user.getTenDangNhap();
            String tenChucVu = "";
            String tenPhongBan = "";
            try {
                BoNhiem bn = boNhiemDAO.findBoNhiemChinhHieuLuc(maNV);
                if (bn != null) {
                    if (bn.getTenChucVu() != null) tenChucVu = bn.getTenChucVu();
                    if (bn.getTenPhongBan() != null) tenPhongBan = bn.getTenPhongBan();
                }
            } catch (Exception ignored) {}

            cboEmployee.addItem(new NhanVienItem(maNV, hoTen, tenChucVu, tenPhongBan));
        }

        if (cboEmployee.getItemCount() == 0) {
            JOptionPane.showMessageDialog(null,
                    "Tất cả nhân viên trong đợt này đã được đánh giá.",
                    "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private JPanel buildCriteriaRow(TieuChiDanhGia c) {
        JPanel panel = new JPanel(new BorderLayout(8, 4));
        panel.setBackground(UIColors.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 210, 210)),
                new EmptyBorder(8, 10, 8, 10)));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));

        // Header: name + weight on left, score spinner on right
        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);

        JLabel lblTen = new JLabel(c.getTenTieuChi() + "  [Trọng số: " + (int) c.getDiemToiDa() + "%]");
        lblTen.setFont(new Font("Segoe UI", Font.BOLD, 13));
        headerRow.add(lblTen, BorderLayout.WEST);

        JSpinner spn = new JSpinner(new SpinnerNumberModel(5.0, 1.0, 10.0, 0.5));
        spn.setPreferredSize(new Dimension(75, 28));
        ((JSpinner.NumberEditor) spn.getEditor()).getFormat().setMaximumFractionDigits(1);
        spn.addChangeListener(e -> updatePreview());
        scoreSpinners.put(c.getId(), spn);

        JPanel scoreRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        scoreRow.setOpaque(false);
        scoreRow.add(new JLabel("Điểm (1-10):"));
        scoreRow.add(spn);
        headerRow.add(scoreRow, BorderLayout.EAST);

        // Description
        JLabel lblMoTa = new JLabel("<html><i>"
                + (c.getMoTa() != null && !c.getMoTa().isEmpty() ? c.getMoTa() : "&nbsp;")
                + "</i></html>");
        lblMoTa.setForeground(Color.GRAY);
        lblMoTa.setFont(new Font("Segoe UI", Font.ITALIC, 12));

        // Comment field
        JTextArea txtCmt = new JTextArea(2, 30);
        txtCmt.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtCmt.setLineWrap(true);
        txtCmt.setWrapStyleWord(true);
        commentFields.put(c.getId(), txtCmt);

        JPanel center = new JPanel(new BorderLayout(4, 4));
        center.setOpaque(false);
        center.add(lblMoTa, BorderLayout.NORTH);
        center.add(new JScrollPane(txtCmt), BorderLayout.CENTER);

        panel.add(headerRow, BorderLayout.NORTH);
        panel.add(center, BorderLayout.CENTER);
        return panel;
    }

    // â”€â”€ layout â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    private void setupLayout() {
        JPanel main = new JPanel(new BorderLayout(10, 10));
        main.setBorder(new EmptyBorder(14, 14, 10, 14));
        main.setBackground(UIColors.LIGHT_GRAY_BG);

        // â”€â”€ top: cycle info + employee selector â”€â”€
        JPanel topPanel = new JPanel(new GridBagLayout());
        topPanel.setBackground(UIColors.WHITE);
        topPanel.setBorder(new TitledBorder("Thông tin đánh giá"));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(5, 8, 5, 8);
        gc.anchor = GridBagConstraints.WEST;

        gc.gridx = 0; gc.gridy = 0;
        topPanel.add(new JLabel("Đợt đánh giá:"), gc);
        gc.gridx = 1; gc.weightx = 1; gc.fill = GridBagConstraints.HORIZONTAL;
        JLabel lblCycleName = new JLabel(cycleName);
        lblCycleName.setFont(new Font("Segoe UI", Font.BOLD, 13));
        topPanel.add(lblCycleName, gc);

        gc.gridx = 0; gc.gridy = 1; gc.weightx = 0; gc.fill = GridBagConstraints.NONE;
        topPanel.add(new JLabel("Nhân viên được đánh giá:"), gc);
        gc.gridx = 1; gc.weightx = 1; gc.fill = GridBagConstraints.HORIZONTAL;
        topPanel.add(cboEmployee, gc);

        gc.gridx = 1; gc.gridy = 2;
        topPanel.add(lblChucVu, gc);

        // â”€â”€ center: criteria scroll â”€â”€
        JScrollPane criteriaScroll = new JScrollPane(criteriaPanel);
        criteriaScroll.setBorder(new TitledBorder("Tiêu chí đánh giá"));
        criteriaScroll.getVerticalScrollBar().setUnitIncrement(16);

        // â”€â”€ bottom: comment + preview + buttons â”€â”€
        JPanel commentPanel = new JPanel(new BorderLayout(5, 5));
        commentPanel.setOpaque(false);
        commentPanel.setBorder(new TitledBorder("Nhận xét chung"));
        commentPanel.add(new JScrollPane(txtNhanXetChung), BorderLayout.CENTER);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 8));
        btnRow.setOpaque(false);
        btnRow.add(lblDiemDuKien);
        btnRow.add(Box.createHorizontalStrut(20));
        btnRow.add(btnHuy);
        btnRow.add(btnLuu);

        JPanel bottom = new JPanel(new BorderLayout(8, 8));
        bottom.setOpaque(false);
        bottom.add(commentPanel, BorderLayout.CENTER);
        bottom.add(btnRow, BorderLayout.SOUTH);

        main.add(topPanel, BorderLayout.NORTH);
        main.add(criteriaScroll, BorderLayout.CENTER);
        main.add(bottom, BorderLayout.SOUTH);

        setContentPane(main);
        updateChucVuLabel();
    }

    // â”€â”€ actions â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    private void updateChucVuLabel() {
        NhanVienItem item = (NhanVienItem) cboEmployee.getSelectedItem();
        if (item == null) { lblChucVu.setText(" "); return; }
        StringBuilder sb = new StringBuilder("Mã NV: ").append(item.maNV);
        if (!item.tenChucVu.isEmpty()) sb.append("  |  Chức vụ: ").append(item.tenChucVu);
        if (!item.tenPhongBan.isEmpty()) sb.append("  |  Phòng ban: ").append(item.tenPhongBan);
        lblChucVu.setText(sb.toString());
    }

    private void updatePreview() {
        List<ChiTietDanhGia> scores = buildScores();
        double total = scores.stream().mapToDouble(ChiTietDanhGia::getDiemCoTrong).sum();
        total = Math.round(total * 100.0) / 100.0;
        String xepLoai = DanhGiaHieuSuat.XepLoai.tuDiem(total).getTenHienThi();
        lblDiemDuKien.setText(String.format("Điểm dự kiến: %.2f - Xếp loại: %s", total, xepLoai));

        // Color hint
        if (total >= 9.0)      lblDiemDuKien.setForeground(new Color(0, 153, 51));
        else if (total >= 8.0) lblDiemDuKien.setForeground(new Color(0, 100, 180));
        else if (total >= 6.5) lblDiemDuKien.setForeground(new Color(100, 140, 0));
        else if (total >= 5.0) lblDiemDuKien.setForeground(new Color(200, 130, 0));
        else                   lblDiemDuKien.setForeground(Color.RED);
    }

    private List<ChiTietDanhGia> buildScores() {
        List<ChiTietDanhGia> scores = new ArrayList<>();
        for (TieuChiDanhGia c : cycleCriteria) {
            JSpinner spn = scoreSpinners.get(c.getId());
            JTextArea txt = commentFields.get(c.getId());
            if (spn != null) {
                ChiTietDanhGia ct = new ChiTietDanhGia(
                        c.getId(),
                        c.getTenTieuChi(),
                        c.getDiemToiDa(),      // trongSo = weight %
                        ((Number) spn.getValue()).doubleValue()
                );
                if (txt != null) ct.setNhanXet(txt.getText().trim());
                scores.add(ct);
            }
        }
        return scores;
    }

    private void luuDanhGia() {
        NhanVienItem selected = (NhanVienItem) cboEmployee.getSelectedItem();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn nhân viên cần đánh giá.",
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        List<ChiTietDanhGia> scores = buildScores();
        if (scores.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Không có tiêu chí nào để đánh giá.",
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        DanhGiaBUS.KetQua<?> result = evalService.submitEvaluation(
                cycleId,
                selected.maNV,        // employee maNV (not taiKhoan.id!)
                selected.hoTen,
                currentUser.getNhanVienId() != null ? currentUser.getNhanVienId() : String.valueOf(currentUser.getId()),
                currentUser.getHoTen(),
                scores,
                txtNhanXetChung.getText().trim()
        );

        if (result.isSuccess()) {
            JOptionPane.showMessageDialog(this, result.getMessage(),
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
            successful = true;
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, result.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSuccessful() { return successful; }

    // â”€â”€ inner class â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    private static class NhanVienItem {
        final String maNV;
        final String hoTen;
        final String tenChucVu;
        final String tenPhongBan;

        NhanVienItem(String maNV, String hoTen, String tenChucVu, String tenPhongBan) {
            this.maNV = maNV;
            this.hoTen = hoTen;
            this.tenChucVu = tenChucVu != null ? tenChucVu : "";
            this.tenPhongBan = tenPhongBan != null ? tenPhongBan : "";
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder("[").append(maNV).append("] ").append(hoTen);
            if (!tenChucVu.isEmpty()) sb.append(" - ").append(tenChucVu);
            if (!tenPhongBan.isEmpty()) sb.append(" | ").append(tenPhongBan);
            return sb.toString();
        }
    }
}
