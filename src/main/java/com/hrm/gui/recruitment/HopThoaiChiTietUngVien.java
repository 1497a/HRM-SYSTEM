package com.hrm.gui.recruitment;

import com.hrm.bus.KetQua;
import com.hrm.bus.TuyenDungBUS;
import com.hrm.model.RecruitmentStatus;
import com.hrm.model.UngVien;
import com.hrm.model.RecruitmentStatus;
import com.hrm.util.DialogUtil;
import com.hrm.util.PermissionCodes;
import com.hrm.util.SessionContext;
import com.hrm.util.UIColors;
import com.hrm.util.UIFonts;
import com.hrm.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.time.format.DateTimeFormatter;

/**
 * Dialog xem chi tiết thông tin ứng viên.
 * Bao gồm chức năng chuyển trạng thái và chuyển thành nhân viên.
 */
class HopThoaiChiTietUngVien extends JDialog {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String[] TRANG_THAI_LABELS = {"Mới", "Đang phỏng vấn", "Trúng tuyển", "Từ chối"};
    private static final String[] TRANG_THAI_CODES = {
            RecruitmentStatus.UngVien.MOI,
            RecruitmentStatus.UngVien.DANG_PHONG_VAN,
            RecruitmentStatus.UngVien.TRUNG_TUYEN,
            RecruitmentStatus.UngVien.TU_CHOI
    };
    private final TuyenDungBUS service;
    private UngVien ungVien;
    private boolean daThayDoi = false;
    // Labels hiển thị
    private JLabel lblTrangThai;
    private JLabel lblMaNV;
    HopThoaiChiTietUngVien(Window owner, TuyenDungBUS service, int maUngVien) {
        super(owner, "Chi tiết ứng viên", ModalityType.APPLICATION_MODAL);
        this.service = service;
        this.ungVien = service.getUngVienById(maUngVien);
        initUI();
        pack();
        setMinimumSize(new Dimension(560, 620));
        setLocationRelativeTo(owner);
    }

    private void initUI() {
        JPanel main = new JPanel(new BorderLayout(8, 8));
        main.setBorder(new EmptyBorder(16, 16, 8, 16));
        main.setBackground(Color.WHITE);
        main.add(buildInfo(), BorderLayout.CENTER);
        main.add(buildButtons(), BorderLayout.SOUTH);
        setContentPane(main);
    }

    private JPanel buildInfo() {
        if (ungVien == null) {
            JPanel p = new JPanel();
            p.add(new JLabel("Không tìm thấy thông tin ứng viên."));
            return p;
        }
        JPanel wrapper = new JPanel(new BorderLayout(0, 12));
        wrapper.setBackground(Color.WHITE);
        // ── Thông tin cá nhân ─────────────────────────────────────────────
        JPanel pCaNhan = new JPanel(new GridBagLayout());
        pCaNhan.setBackground(Color.WHITE);
        pCaNhan.setBorder(new TitledBorder("Thông tin cá nhân"));
        GridBagConstraints lc = labelGBC();
        GridBagConstraints fc = fieldGBC();
        int row = 0;
        String pb = ungVien.getTenPhongBan();
        String cv = ungVien.getTenChucVu();
        String viTri = "";
        if (pb != null && !pb.isBlank()) viTri += pb;
        if (pb != null && !pb.isBlank() && cv != null && !cv.isBlank()) viTri += " - ";
        if (cv != null && !cv.isBlank()) viTri += cv;
        if (viTri.isBlank()) viTri = "[Chưa xác định]";
        addRow(pCaNhan, row++, "Mã ứng viên:",   val(String.valueOf(ungVien.getMaUngVien())), lc, fc);
        addRow(pCaNhan, row++, "Họ tên:",         val(ungVien.getHoTen()), lc, fc);
        addRow(pCaNhan, row++, "Email:",           val(ungVien.getEmail()), lc, fc);
        addRow(pCaNhan, row++, "Điện thoại:",      val(ungVien.getDienThoai()), lc, fc);
        addRow(pCaNhan, row++, "Ngày sinh:",
                val(ungVien.getNgaySinh() != null ? ungVien.getNgaySinh().format(DATE_FMT) : ""), lc, fc);
        addRow(pCaNhan, row++, "Giới tính:",       val(formatGioiTinh(ungVien.getGioiTinh())), lc, fc);
        addRow(pCaNhan, row++, "Địa chỉ:",         val(ungVien.getDiaChi()), lc, fc);
        // ── Thông tin ứng tuyển ───────────────────────────────────────────
        JPanel pUngTuyen = new JPanel(new GridBagLayout());
        pUngTuyen.setBackground(Color.WHITE);
        pUngTuyen.setBorder(new TitledBorder("Thông tin ứng tuyển"));
        GridBagConstraints lc2 = labelGBC();
        GridBagConstraints fc2 = fieldGBC();
        row = 0;
        addRow(pUngTuyen, row++, "Vị trí:",             val(viTri), lc2, fc2);
        addRow(pUngTuyen, row++, "Tin tuyển dụng:",
                val(ungVien.getTenTin() != null ? ungVien.getTenTin() : ""), lc2, fc2);
        addRow(pUngTuyen, row++, "Trình độ học vấn:",   val(ungVien.getTrinhDoHocVan()), lc2, fc2);
        addRow(pUngTuyen, row++, "Nguồn ứng tuyển:",    val(ungVien.getNguonUngTuyen()), lc2, fc2);
        addRow(pUngTuyen, row++, "Ngày nộp hồ sơ:",
                val(ungVien.getNgayTao() != null ? ungVien.getNgayTao().format(DATE_FMT) : ""), lc2, fc2);
        lblTrangThai = statusLabel(ungVien.getTrangThaiDisplay());
        addRow(pUngTuyen, row++, "Trạng thái:",         lblTrangThai, lc2, fc2);
        lblMaNV = val(ungVien.getMaNV() != null ? ungVien.getMaNV() : "");
        addRow(pUngTuyen, row++, "Mã nhân viên:",        lblMaNV, lc2, fc2);
        // ── Kinh nghiệm & nhận xét ────────────────────────────────────────
        JPanel pMoTa = new JPanel(new GridBagLayout());
        pMoTa.setBackground(Color.WHITE);
        pMoTa.setBorder(new TitledBorder("Kinh nghiệm & Nhận xét"));
        GridBagConstraints lc3 = labelGBC();
        GridBagConstraints fc3 = fieldGBC();
        row = 0;
        addRow(pMoTa, row++, "Kinh nghiệm:", buildTextArea(ungVien.getKinhNghiem()), lc3, fc3);
        addRow(pMoTa, row++, "Nhận xét:",    buildTextArea(ungVien.getNhanXet()), lc3, fc3);
        wrapper.add(pCaNhan,  BorderLayout.NORTH);
        JPanel mid = new JPanel(new GridLayout(2, 1, 0, 8));
        mid.setBackground(Color.WHITE);
        mid.add(pUngTuyen);
        mid.add(pMoTa);
        wrapper.add(mid, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel buildButtons() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        panel.setBackground(Color.WHITE);
        boolean canReview = SessionContext.getInstance().hasPermission(PermissionCodes.RECRUITMENT_CANDIDATE_REVIEW);
        boolean canConvert = SessionContext.getInstance().hasPermission(PermissionCodes.RECRUITMENT_CANDIDATE_CONVERT);
        if (ungVien != null && (canReview || canConvert)) {
            JButton btnChuyenTT = UIHelper.createPrimaryButton("Chuyển trạng thái");
            JButton btnChuyenNV = UIHelper.createSuccessButton("Chuyển thành NV");
            btnChuyenTT.addActionListener(e -> chuyenTrangThai());
            btnChuyenNV.addActionListener(e -> chuyenThanhNV());
            if (canReview) panel.add(btnChuyenTT);
            if (canConvert) panel.add(btnChuyenNV);
        }
        JButton btnDong = UIHelper.createDefaultButton("Đóng");
        btnDong.addActionListener(e -> dispose());
        panel.add(btnDong);
        return panel;
    }

    private void chuyenTrangThai() {
        JComboBox<String> cbo = new JComboBox<>(TRANG_THAI_LABELS);
        cbo.setFont(UIFonts.TEXT_NORMAL);
        if (JOptionPane.showConfirmDialog(this, new Object[]{"Trạng thái mới:", cbo},
                "Chuyển trạng thái ứng viên", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) {
            return;
        }
        KetQua<?> r = service.capNhatTrangThaiUV(ungVien.getMaUngVien(), TRANG_THAI_CODES[cbo.getSelectedIndex()]);
        if (r.isSuccess()) {
            ungVien = service.getUngVienById(ungVien.getMaUngVien());
            if (ungVien != null) {
                lblTrangThai.setText(ungVien.getTrangThaiDisplay());
                applyStatusColor(lblTrangThai, ungVien.getTrangThaiDisplay());
            }
            daThayDoi = true;
            DialogUtil.showSuccess(this, "Đã cập nhật trạng thái.");
        } else {
            TabUtils.showError(this, r.getMessage());
        }
    }

    private void chuyenThanhNV() {
        KetQua<String> preview = service.taoThongDiepXacNhanChuyenUVThanhNV(ungVien.getMaUngVien());
        if (!preview.isSuccess()) {
            TabUtils.showError(this, preview.getMessage());
            return;
        }
        if (!DialogUtil.showYesNo(this, preview.getData(), "Xác nhận chuyển thành nhân viên")) {
            return;
        }
        KetQua<?> r = service.chuyenUVThanhNV(ungVien.getMaUngVien());
        if (r.isSuccess()) {
            ungVien = service.getUngVienById(ungVien.getMaUngVien());
            if (ungVien != null) {
                lblTrangThai.setText(ungVien.getTrangThaiDisplay());
                applyStatusColor(lblTrangThai, ungVien.getTrangThaiDisplay());
                lblMaNV.setText(ungVien.getMaNV() != null ? ungVien.getMaNV() : "");
            }
            daThayDoi = true;
            DialogUtil.showSuccess(this, "Đã chuyển thành nhân viên thành công!\n" + r.getMessage());
        } else {
            TabUtils.showError(this, r.getMessage());
        }
    }

    boolean isDaThayDoi() {
        return daThayDoi;
    }

    // ── helpers ──────────────────────────────────────────────────────────────
    private JLabel val(String text) {
        JLabel lbl = new JLabel(text != null ? text : "");
        lbl.setFont(UIFonts.TEXT_NORMAL);
        return lbl;
    }

    private JLabel statusLabel(String displayText) {
        JLabel lbl = new JLabel(displayText != null ? displayText : "");
        lbl.setFont(UIFonts.BOLD_SMALL);
        applyStatusColor(lbl, displayText);
        return lbl;
    }

    private void applyStatusColor(JLabel lbl, String displayText) {
        if (displayText == null) return;
        String trung = RecruitmentStatus.displayUngVien(RecruitmentStatus.UngVien.TRUNG_TUYEN, null);
        String chuyenNV = RecruitmentStatus.displayUngVien(RecruitmentStatus.UngVien.DA_CHUYEN_NHAN_VIEN, "NV");
        String tuChoi = RecruitmentStatus.displayUngVien(RecruitmentStatus.UngVien.TU_CHOI, null);
        String phongVan = RecruitmentStatus.displayUngVien(RecruitmentStatus.UngVien.DANG_PHONG_VAN, null);
        if (displayText.equals(trung) || displayText.equals(chuyenNV)) {
            lbl.setForeground(UIColors.SUCCESS_GREEN);
        } else if (displayText.equals(tuChoi)) {
            lbl.setForeground(UIColors.DANGER_RED);
        } else if (displayText.equals(phongVan)) {
            lbl.setForeground(UIColors.ACCENT_YELLOW);
        } else {
            lbl.setForeground(UIColors.INFO_TEAL);
        }
    }

    private JScrollPane buildTextArea(String text) {
        JTextArea area = new JTextArea(text != null ? text : "", 3, 25);
        area.setFont(UIFonts.TEXT_NORMAL);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setEditable(false);
        area.setBackground(Color.WHITE);
        JScrollPane sp = new JScrollPane(area);
        sp.setPreferredSize(new Dimension(300, 60));
        return sp;
    }

    private String formatGioiTinh(String g) {
        if (g == null) return "";
        return switch (g) {
            case "nam" -> "Nam";
            case "nu", "nữ" -> "Nữ";
            default -> g;
        };
    }

    private GridBagConstraints labelGBC() {
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0; g.anchor = GridBagConstraints.NORTHWEST;
        g.insets = new Insets(4, 6, 4, 8);
        return g;
    }

    private GridBagConstraints fieldGBC() {
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 1; g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1.0; g.insets = new Insets(4, 0, 4, 6);
        return g;
    }

    private void addRow(JPanel p, int row, String label, Component comp,
                        GridBagConstraints lc, GridBagConstraints fc) {
        lc.gridy = row; fc.gridy = row;
        JLabel lbl = new JLabel(label);
        lbl.setFont(UIFonts.TEXT_NORMAL);
        p.add(lbl, lc);
        p.add(comp, fc);
    }
}
