package com.hrm.gui.attendance;

import com.hrm.model.*;
import com.hrm.bus.ChamCongBUS;
import com.hrm.bus.KetQua;
import com.hrm.bus.XacThucBUS;
import com.hrm.dao.ChamCongDAO;
import com.hrm.util.HRMConstants;
import com.hrm.util.PermissionCodes;
import com.hrm.util.UIFonts;
import com.hrm.util.SessionContext;
import com.hrm.util.DialogUtil;
import com.hrm.gui.components.PurpleTable;
import com.hrm.util.UIColors;
import com.hrm.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableRowSorter;

import java.awt.*;
import java.awt.Font;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;

public class AttendancePanel extends JPanel {

    private final ChamCongBUS svc;
    private final TaiKhoan currentUser;
    private final boolean canManage;         // ATTENDANCE_MANAGE
    private final boolean canManageAllowance; // ALLOWANCE_MANAGE
    private final boolean canViewTeam; // ATTENDANCE_VIEW with TEAM/DEPT/ALL scope
    private final boolean canCheckInSelf;
    private final boolean canRequestOTSelf;
    private final boolean canApproveOT;
    private final DataScope attendanceScope;
    private final DataScope overtimeApproveScope;
    private String maNVCaNhan = null;
    private final NumberFormat moneyFmt;
    private JTabbedPane tabbedPane;
    // Tab 1
    private PurpleTable tableChamCong; private DefaultTableModel modelCC;
    private JComboBox<String> cboThang, cboNam, cboMaNVFilter, cboTrangThaiFilter;
    private JPanel statsPanel;
    private List<ChamCong> dsChamCongTongHop = new ArrayList<>();
    // Tab 1 -- search
    private JTextField txtTimKiemCC;
    // Tab 2
    private PurpleTable tableCaLam; private DefaultTableModel modelCaLam;
    private JTextField txtTimKiemCaLam; private TableRowSorter<DefaultTableModel> sorterCaLam;
    // Tab 3
    private PurpleTable tableDonOT; private DefaultTableModel modelOT;
    private JTextField txtTimKiemOT; private TableRowSorter<DefaultTableModel> sorterOT;
    // Tab 5
    private PurpleTable tablePC; private DefaultTableModel modelPC;
    private JTextField txtTimKiemPC; private TableRowSorter<DefaultTableModel> sorterPC;
    // Tab -- Cham cong ca nhan
    private JLabel lblCaNhanStatus;
    private JButton btnCheckInCN, btnCheckOutCN;
    private JComboBox<CaLam> cboCaLamCaNhan;
    private List<CaLam> dsCaLamCN;
    private DefaultTableModel modelLichSuCaNhan;
    private PurpleTable tableLichSuCaNhan;
    private JComboBox<String> cboThangCN, cboNamCN;
    private JPanel statsPanelCN;
    private javax.swing.Timer clockTimer;
    // Fields for personal attendance
    private String maNVTimKiem = null;
    private JTextField txtMaNVCN;
    private JPanel infoPanelCN;
    private JLabel lblHoTenCNCN, lblEmailCNCN, lblChucVuCNCN, lblPhongBanCNCN, lblTrangThaiCNCN;
    private JPanel chamCongStatusPanelCN;
    private JPanel histPanelCN;
    // Tab -- Dang ky OT (Employee/HR/Manager)
    private DefaultTableModel modelDonOTCaNhan;
    private PurpleTable tableDonOTCaNhan;
    public AttendancePanel() {
        svc = ChamCongBUS.getInstance();
        currentUser = SessionContext.getInstance().getCurrentUser();
        SessionContext sc = SessionContext.getInstance();
        attendanceScope = XacThucBUS.getInstance().getScopeForAction(PermissionCodes.ATTENDANCE_VIEW);
        canManage         = sc.hasPermission(PermissionCodes.ATTENDANCE_MANAGE);
        canManageAllowance = sc.hasPermission(PermissionCodes.ALLOWANCE_MANAGE);
        overtimeApproveScope = sc.hasPermission(PermissionCodes.OVERTIME_APPROVE)
            ? XacThucBUS.getInstance().getScopeForAction(PermissionCodes.OVERTIME_APPROVE)
            : DataScope.NONE;
        canViewTeam = attendanceScope == DataScope.ALL
                   || attendanceScope == DataScope.DEPT
                   || attendanceScope == DataScope.TEAM;
        canCheckInSelf = sc.hasPermission(PermissionCodes.ATTENDANCE_CHECKIN);
        canRequestOTSelf = sc.hasPermission(PermissionCodes.OVERTIME_REQUEST);
        canApproveOT = sc.hasPermission(PermissionCodes.OVERTIME_APPROVE)
            && (overtimeApproveScope == DataScope.ALL
            || overtimeApproveScope == DataScope.DEPT
            || overtimeApproveScope == DataScope.TEAM);
        if (currentUser != null) {
            maNVCaNhan = svc.getMaNVByTaiKhoan(currentUser.getId());
        }
        moneyFmt = NumberFormat.getInstance(new Locale("vi", "VN"));
        setLayout(new BorderLayout()); setBackground(Color.WHITE);
        initTabs();
    }

    private void initTabs() {
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(UIFonts.TEXT_NORMAL);
        tabbedPane.setBackground(Color.WHITE);
        if (canManage) {
            tabbedPane.addTab("Tổng hợp chấm công", tabTongHop());
            tabbedPane.addTab("Quản lý ca làm", tabCaLam());
            if (canApproveOT) tabbedPane.addTab("Duyệt đơn OT", tabDuyetOT());
            if (canManageAllowance) tabbedPane.addTab("Phụ cấp & Khấu trừ", tabPhuCap());
            if (maNVCaNhan != null && canCheckInSelf) {
                tabbedPane.addTab("Chấm công cá nhân", tabCaNhan());
            }
            if (maNVCaNhan != null && canRequestOTSelf) {
                tabbedPane.addTab("Đăng ký OT", tabDangKyOT());
            }
        } else if (canViewTeam) {
            if (maNVCaNhan != null && canCheckInSelf) tabbedPane.addTab("Chấm công cá nhân", tabCaNhan());
            tabbedPane.addTab("Tổng hợp chấm công", tabTongHopReadOnly());
            if (canManageAllowance) tabbedPane.addTab("Phụ cấp & Khấu trừ", tabPhuCap());
            if (canApproveOT) tabbedPane.addTab("Duyệt đơn OT", tabDuyetOT());
            if (maNVCaNhan != null && canRequestOTSelf) tabbedPane.addTab("Đăng ký OT", tabDangKyOT());
        } else {
            if (maNVCaNhan != null && canCheckInSelf) {
                tabbedPane.addTab("Chấm công cá nhân", tabCaNhan());
            }
            if (maNVCaNhan != null && canRequestOTSelf) {
                tabbedPane.addTab("Đăng ký OT", tabDangKyOT());
            }
            if (tabbedPane.getTabCount() == 0) {
                JPanel p = new JPanel(new GridBagLayout()); p.setBackground(Color.WHITE);
                JLabel l = new JLabel("Tài khoản của bạn chưa được liên kết với nhân viên nào. Vui lòng liên hệ quản trị để được hỗ trợ.");
                l.setFont(UIFonts.TEXT_NORMAL); l.setForeground(Color.GRAY);
                p.add(l);
                tabbedPane.addTab("Thông báo", p);
            }
        }
        add(tabbedPane, BorderLayout.CENTER);
    }

    // ===============================================
    //  TAB 1 -- TONG HOP CHAM CONG (Admin only)
    // ===============================================
    private JPanel tabTongHop() {
        JPanel p=panel();
        JPanel tb=new JPanel(new FlowLayout(FlowLayout.LEFT,10,5)); tb.setOpaque(false);
        initTongHopFilters(tb);
        tb.add(Box.createHorizontalStrut(12));
        tb.add(new JLabel("Tháng:")); cboThang=combMonth(); tb.add(cboThang);
        tb.add(new JLabel("Năm:")); cboNam=combYear(); tb.add(cboNam);
        JButton bL=btn("Lọc dữ liệu",UIColors.PRIMARY_PURPLE); bL.addActionListener(e->loadCC()); tb.add(bL);
        tb.add(Box.createHorizontalStrut(20));
        JButton bT=btn("+ Thêm thủ công",UIColors.SUCCESS_GREEN); bT.addActionListener(e->dlgThemCC()); tb.add(bT);
        JButton bS=btn("Xem chi tiết",UIColors.PRIMARY_PURPLE); bS.addActionListener(e->dlgChiTietCC()); tb.add(bS);
        JLabel lblHint = new JLabel("Tìm theo mã nhân viên hoặc họ tên.");
        lblHint.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblHint.setForeground(UIColors.TEXT_DARK);
        JPanel north = new JPanel(new BorderLayout(0, 2)); north.setOpaque(false);
        north.add(tb, BorderLayout.CENTER);
        north.add(lblHint, BorderLayout.SOUTH);
        p.add(north,BorderLayout.NORTH);
        String[]cols={"Mã NV","Họ tên","Ngày","Ca làm","Giờ vào","Giờ ra","Số giờ","OT","Trạng thái"};
        modelCC=mdl(cols); tableChamCong=tbl(modelCC);
        tableChamCong.getColumnModel().getColumn(8).setCellRenderer(new com.hrm.gui.components.StatusCellRenderer());
        for (int i : new int[]{2, 3, 4, 5, 6, 7}) tableChamCong.getColumnModel().getColumn(i).setCellRenderer(CENTER_R);
        p.add(new JScrollPane(tableChamCong),BorderLayout.CENTER);
        statsPanel=new JPanel(new FlowLayout(FlowLayout.LEFT,30,10));
        statsPanel.setOpaque(false); statsPanel.setBorder(new EmptyBorder(5,0,0,0));
        p.add(statsPanel,BorderLayout.SOUTH); loadCC(); return p;
    }

    private void loadCC() {
        modelCC.setRowCount(0);
        int th = Integer.parseInt((String) cboThang.getSelectedItem());
        int nm = Integer.parseInt((String) cboNam.getSelectedItem());
        List<ChamCong> ds = canManage
            ? svc.getChamCongTatCaTheoThang(th, nm)
            : svc.getChamCongTatCaTheoThangByScope(th, nm, maNVCaNhan);
        dsChamCongTongHop = ds;
        DateTimeFormatter fN = DateTimeFormatter.ofPattern("dd/MM");
        DateTimeFormatter fG = DateTimeFormatter.ofPattern("HH:mm");
        for (ChamCong cc : ds) {
            String maNhanVien = svc.getMaNhanVienById(cc.getMaNV());
            modelCC.addRow(new Object[]{
                maNhanVien,
                cc.getTenNhanVien() != null ? cc.getTenNhanVien() : "NV-" + cc.getMaNV(),
                cc.getNgay().format(fN),
                    cc.getTenCaLam() != null ? cc.getTenCaLam() : cc.getMaCaLam(),
                cc.getGioVao() != null ? cc.getGioVao().format(fG) : "-",
                cc.getGioRa()  != null ? cc.getGioRa().format(fG)  : "-",
                String.format("%.1f", cc.getSoGioLam()),
                cc.getGioLamThem() > 0 ? String.format("%.1f", cc.getGioLamThem()) : "-",
                cc.getTrangThai() != null ? cc.getTrangThai().getDisplayName() : "N/A"
            });
        }
        if (statsPanel == null) return;
        long dg = ds.stream().filter(c -> c.getTrangThai() == ChamCong.TrangThai.DUNG_GIO).count();
        long dm = ds.stream().filter(c -> c.getTrangThai() == ChamCong.TrangThai.DI_MUON).count();
        long vm = ds.stream().filter(c -> c.getTrangThai() == ChamCong.TrangThai.VANG_MAT).count();
        statsPanel.removeAll();
        statsPanel.add(lbl("Tổng:", String.valueOf(ds.size()), UIColors.PRIMARY_PURPLE));
        statsPanel.add(lbl("Đúng giờ:", String.valueOf(dg), UIColors.SUCCESS_GREEN));
        statsPanel.add(lbl("Đi muộn:", String.valueOf(dm), UIColors.DANGER_RED));
        statsPanel.add(lbl("Vắng:", String.valueOf(vm), Color.GRAY));
        statsPanel.revalidate(); statsPanel.repaint();
        refreshTongHopFilterOptionsFromTable();
        applyCCRowFilter();
    }

    private void dlgThemCC() {
        JDialog d = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Thêm thủ công thủ công", true);
        d.setSize(500, 560); d.setLocationRelativeTo(this);
        JPanel f = new JPanel(new GridBagLayout());
        f.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints g = gbc();
        // Row 0: Ma NV + nut Tim
        g.gridx = 0; g.gridy = 0; g.weightx = 0; g.gridwidth = 1;
        f.add(new JLabel("Mã nhân viên:"), g);
        JTextField txtMaNV = new JTextField(12);
        txtMaNV.setFont(com.hrm.util.UIFonts.TEXT_NORMAL);
        g.gridx = 1; g.weightx = 1; f.add(txtMaNV, g);
        JButton btnTim = btn("Tìm", UIColors.PRIMARY_PURPLE);
        btnTim.setPreferredSize(new Dimension(70, 30));
        g.gridx = 2; g.weightx = 0; f.add(btnTim, g);
        // Row 1: Panel thong tin nhan vien (an mac dinh)
        JPanel infoPanel = new JPanel(new GridLayout(0, 2, 8, 6));
        infoPanel.setBackground(com.hrm.util.UIColors.LIGHT_PURPLE);
        infoPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIColors.PRIMARY_PURPLE, 1),
            new EmptyBorder(10, 12, 10, 12)));
        infoPanel.setVisible(false);
        JLabel lblHoTenVal  = new JLabel(""); lblHoTenVal.setFont(com.hrm.util.UIFonts.BOLD_NORMAL);
        JLabel lblEmailVal  = new JLabel(""); lblEmailVal.setFont(com.hrm.util.UIFonts.TEXT_NORMAL);
        JLabel lblChucVuVal = new JLabel(""); lblChucVuVal.setFont(com.hrm.util.UIFonts.TEXT_NORMAL);
        JLabel lblPBVal     = new JLabel(""); lblPBVal.setFont(com.hrm.util.UIFonts.TEXT_NORMAL);
        JLabel lblTTVal     = new JLabel(""); lblTTVal.setFont(com.hrm.util.UIFonts.TEXT_NORMAL);
        infoPanel.add(boldLabel("Họ tên:"));    infoPanel.add(lblHoTenVal);
        infoPanel.add(boldLabel("Email:"));     infoPanel.add(lblEmailVal);
        infoPanel.add(boldLabel("Chức vụ:"));   infoPanel.add(lblChucVuVal);
        infoPanel.add(boldLabel("Phòng ban:")); infoPanel.add(lblPBVal);
        infoPanel.add(boldLabel("Trạng thái:")); infoPanel.add(lblTTVal);
        g.gridx = 0; g.gridy = 1; g.gridwidth = 3; g.insets = new Insets(8, 8, 8, 8);
        f.add(infoPanel, g);
        g.gridwidth = 1; g.insets = new Insets(8, 8, 8, 8);
        String[] maNVRef = {null};
        btnTim.addActionListener(e -> {
            String ma = txtMaNV.getText().trim();
            if (ma.isEmpty()) { DialogUtil.showInfo(d, "Vui lòng nhập mã nhân viên."); return; }
                var info = svc.findNhanVienByMaForManualAttendance(ma);
            if (info == null) {
                infoPanel.setVisible(false); maNVRef[0] = null;
                DialogUtil.showError(d, "Không tìm thấy nhân viên: " + ma);
            } else {
                maNVRef[0] = info.maNV;
                lblHoTenVal.setText(info.hoTen);
                lblEmailVal.setText(info.email.isEmpty() ? "(Chưa có)" : info.email);
                lblChucVuVal.setText(info.tenChucVu.isEmpty() ? "(Chưa có)" : info.tenChucVu);
                lblPBVal.setText(info.tenPhongBan.isEmpty() ? "(Chưa có)" : info.tenPhongBan);
                lblTTVal.setText(formatTrangThaiNV(info.trangThai));
                lblTTVal.setForeground(HRMConstants.TRANG_THAI_DANG_LAM_VIEC.equals(info.trangThai) ? UIColors.SUCCESS_GREEN : UIColors.DANGER_RED);
                infoPanel.setVisible(true); d.revalidate(); d.repaint();
            }
        });
        txtMaNV.addActionListener(e -> btnTim.doClick());
        // Row 2: Ngay
        g.gridx = 0; g.gridy = 2; g.weightx = 0; g.gridwidth = 1;
        f.add(new JLabel("Ngày (dd/MM/yyyy):"), g);
        JTextField txtNgay = new JTextField(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        g.gridx = 1; g.gridwidth = 2; g.weightx = 1; f.add(txtNgay, g);
        // Row 3: Ca lam
        g.gridx = 0; g.gridy = 3; g.gridwidth = 1; g.weightx = 0;
        f.add(new JLabel("Ca làm:"), g);
        JComboBox<String> cCa = new JComboBox<>();
        for (CaLam ca : svc.getActiveCaLam()) cCa.addItem(ca.getId() + " - " + ca.getTenCaLam());
        g.gridx = 1; g.gridwidth = 2; g.weightx = 1; f.add(cCa, g);
        // Row 4: Gio vao
        g.gridx = 0; g.gridy = 4; g.gridwidth = 1; g.weightx = 0;
        f.add(new JLabel("Giờ vào (HH:mm):"), g);
        JTextField tV = new JTextField("08:00");
        g.gridx = 1; g.gridwidth = 2; g.weightx = 1; f.add(tV, g);
        // Row 5: Gio ra
        g.gridx = 0; g.gridy = 5; g.gridwidth = 1; g.weightx = 0;
        f.add(new JLabel("Giờ ra (HH:mm):"), g);
        JTextField tR = new JTextField("17:00");
        g.gridx = 1; g.gridwidth = 2; g.weightx = 1; f.add(tR, g);
        // Row 6: Trang thai
        g.gridx = 0; g.gridy = 6; g.gridwidth = 1; g.weightx = 0;
        f.add(new JLabel("Trạng thái:"), g);
        JComboBox<String> cTT = new JComboBox<>(new String[]{"Đúng giờ", "Đi muộn", "Về sớm", "Vắng mặt"});
        g.gridx = 1; g.gridwidth = 2; g.weightx = 1; f.add(cTT, g);
        // Row 7: Nut Luu
        JButton bLuu = btn("Lưu", UIColors.SUCCESS_GREEN);
        bLuu.setFont(UIFonts.BOLD_NORMAL);
        bLuu.setPreferredSize(new Dimension(120, 38));
        g.gridx = 0; g.gridy = 7; g.gridwidth = 3; g.insets = new Insets(20, 8, 8, 8);
        f.add(bLuu, g);
        ChamCong.TrangThai[] tts = {
            ChamCong.TrangThai.DUNG_GIO, ChamCong.TrangThai.DI_MUON,
            ChamCong.TrangThai.VE_SOM,   ChamCong.TrangThai.VANG_MAT
        };
        bLuu.addActionListener(e -> {
            try {
                if (maNVRef[0] == null) {
                    DialogUtil.showWarn(d, "Vui lòng tìm kiếm nhân viên trước khi lưu.");
                    return;
                }
                LocalDate ngay;
                try {
                    ngay = LocalDate.parse(txtNgay.getText().trim(), DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                } catch (Exception ex) {
                    DialogUtil.showError(d, "Định dạng ngày không hợp lệ. Dùng: dd/MM/yyyy");
                    return;
                }
                String maCa = ((String) cCa.getSelectedItem()).split(" - ")[0].trim();
                String[] v  = tV.getText().trim().split(":");
                String[] r  = tR.getText().trim().split(":");
                LocalDateTime gioVao = ngay.atTime(Integer.parseInt(v[0]), Integer.parseInt(v[1]));
                LocalDateTime gioRa = ngay.atTime(Integer.parseInt(r[0]), Integer.parseInt(r[1]));
                KetQua<ChamCong> res = svc.themChamCongThuCong(
                        maNVRef[0], ngay, maCa, gioVao, gioRa, tts[cTT.getSelectedIndex()], "Nhập thủ công");
                if (res.isSuccess()) { DialogUtil.showSuccess(d, res.getMessage()); d.dispose(); loadCC(); }
                else DialogUtil.showError(d, res.getMessage());
            } catch (Exception ex) {
                DialogUtil.showError(d, "Lỗi: " + ex.getMessage());
            }
        });
        d.setContentPane(f); d.setVisible(true);
    }

    private void dlgChiTietCC() {
        ChamCong selected = getSelectedChamCong();
        if (selected == null) {
            DialogUtil.showInfo(this, "Chọn bản ghi chấm công cần xem.");
            return;
        }
        JDialog d = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Chi tiết chấm công", true);
        d.setSize(620, 720);
        d.setLocationRelativeTo(this);
        ChamCongDAO.NhanVienInfo info = svc.findNhanVienByMaForManualAttendance(selected.getMaNV());
        DateTimeFormatter fDate = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter fTime = DateTimeFormatter.ofPattern("HH:mm");
        DateTimeFormatter fAudit = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        JPanel content = new JPanel(new BorderLayout(0, 12));
        content.setBorder(new EmptyBorder(16, 16, 16, 16));

        JPanel infoPanel = new JPanel(new GridLayout(0, 2, 8, 6));
        infoPanel.setBackground(UIColors.LIGHT_PURPLE);
        infoPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIColors.PRIMARY_PURPLE, 1),
            new EmptyBorder(10, 12, 10, 12)));
        infoPanel.add(boldLabel("Mã nhân viên:")); infoPanel.add(new JLabel(selected.getMaNV()));
        infoPanel.add(boldLabel("Họ tên:")); infoPanel.add(new JLabel(info != null ? info.hoTen : selected.getTenNhanVien()));
        infoPanel.add(boldLabel("Email:")); infoPanel.add(new JLabel(info != null && !info.email.isBlank() ? info.email : "(Chưa có)"));
        infoPanel.add(boldLabel("Chức vụ:")); infoPanel.add(new JLabel(info != null && !info.tenChucVu.isBlank() ? info.tenChucVu : "(Chưa có)"));
        infoPanel.add(boldLabel("Phòng ban:")); infoPanel.add(new JLabel(info != null && !info.tenPhongBan.isBlank() ? info.tenPhongBan : "(Chưa có)"));
        content.add(infoPanel, BorderLayout.NORTH);

        JPanel details = new JPanel(new GridBagLayout());
        GridBagConstraints g = gbc();
        g.insets = new Insets(6, 8, 6, 8);

        addDetailRow(details, g, 0, "Ngày:", selected.getNgay() != null ? selected.getNgay().format(fDate) : "-");
        addDetailRow(details, g, 1, "Ca làm:", selected.getTenCaLam() != null ? selected.getTenCaLam() + " (" + selected.getMaCaLam() + ")" : selected.getMaCaLam());
        addDetailRow(details, g, 2, "Giờ vào:", selected.getGioVao() != null ? selected.getGioVao().format(fTime) : "-");
        addDetailRow(details, g, 3, "Giờ ra:", selected.getGioRa() != null ? selected.getGioRa().format(fTime) : "-");
        addDetailRow(details, g, 4, "Số giờ làm:", String.format("%.1f", selected.getSoGioLam()));
        addDetailRow(details, g, 5, "Giờ làm thêm:", selected.getGioLamThem() > 0 ? String.format("%.1f", selected.getGioLamThem()) : "-");
        addDetailRow(details, g, 6, "Trạng thái:", selected.getTrangThai() != null ? selected.getTrangThai().getDisplayName() : "-");
        addDetailRow(details, g, 7, "Phương thức chấm công:", selected.getPhuongThucChamCong() != null ? selected.getPhuongThucChamCong().getDisplayName() : "-");
        addTextAreaRow(details, g, 8, "Ghi chú:", selected.getGhiChu());
        addDetailRow(details, g, 9, "Người sửa gần nhất:", selected.getNguoiChinhSua() != null && !selected.getNguoiChinhSua().isBlank() ? selected.getNguoiChinhSua() : "(chưa có)");
        addDetailRow(details, g, 10, "Thời điểm sửa:", selected.getNgayChinhSua() != null ? selected.getNgayChinhSua().format(fAudit) : "(chưa có)");
        addTextAreaRow(details, g, 11, "Lý do chỉnh sửa:", selected.getLyDoChinhSua());

        JScrollPane scroll = new JScrollPane(details);
        scroll.setBorder(BorderFactory.createTitledBorder("Thông tin chấm công"));
        content.add(scroll, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);
        JButton btnClose = btn("Đóng", Color.GRAY);
        JButton btnEdit = btn("Sửa chấm công", UIColors.SUCCESS_GREEN);
        btnEdit.addActionListener(e -> {
            d.dispose();
            dlgSuaCC(selected);
        });
        btnClose.addActionListener(e -> d.dispose());
        actions.add(btnClose);
        actions.add(btnEdit);
        content.add(actions, BorderLayout.SOUTH);

        d.setContentPane(content);
        d.setVisible(true);
    }

    private void dlgSuaCC(ChamCong selected) {
        JDialog d = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Sửa chấm công", true);
        d.setSize(560, 620);
        d.setLocationRelativeTo(this);
        JPanel f = new JPanel(new GridBagLayout());
        f.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints g = gbc();

        ChamCongDAO.NhanVienInfo info = svc.findNhanVienByMaForManualAttendance(selected.getMaNV());
        JPanel infoPanel = new JPanel(new GridLayout(0, 2, 8, 6));
        infoPanel.setBackground(UIColors.LIGHT_PURPLE);
        infoPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIColors.PRIMARY_PURPLE, 1),
            new EmptyBorder(10, 12, 10, 12)));
        infoPanel.add(boldLabel("Mã nhân viên:")); infoPanel.add(new JLabel(selected.getMaNV()));
        infoPanel.add(boldLabel("Họ tên:")); infoPanel.add(new JLabel(info != null ? info.hoTen : selected.getTenNhanVien()));
        infoPanel.add(boldLabel("Email:")); infoPanel.add(new JLabel(info != null && !info.email.isBlank() ? info.email : "(Chưa có)"));
        infoPanel.add(boldLabel("Chức vụ:")); infoPanel.add(new JLabel(info != null && !info.tenChucVu.isBlank() ? info.tenChucVu : "(Chưa có)"));
        infoPanel.add(boldLabel("Phòng ban:")); infoPanel.add(new JLabel(info != null && !info.tenPhongBan.isBlank() ? info.tenPhongBan : "(Chưa có)"));
        g.gridx = 0; g.gridy = 0; g.gridwidth = 3;
        f.add(infoPanel, g);

        DateTimeFormatter fDate = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter fTime = DateTimeFormatter.ofPattern("HH:mm");
        g.gridwidth = 1;

        g.gridx = 0; g.gridy = 1; g.weightx = 0;
        f.add(new JLabel("Ngày (dd/MM/yyyy):"), g);
        JTextField txtNgay = new JTextField(selected.getNgay() != null ? selected.getNgay().format(fDate) : "");
        g.gridx = 1; g.gridy = 1; g.gridwidth = 2; g.weightx = 1;
        f.add(txtNgay, g);

        g.gridx = 0; g.gridy = 2; g.gridwidth = 1; g.weightx = 0;
        f.add(new JLabel("Ca làm:"), g);
        JComboBox<String> cCa = new JComboBox<>();
        int selectedCaIndex = -1;
        int idx = 0;
        for (CaLam ca : svc.getActiveCaLam()) {
            String item = ca.getId() + " - " + ca.getTenCaLam();
            cCa.addItem(item);
            if (ca.getId().equals(selected.getMaCaLam())) {
                selectedCaIndex = idx;
            }
            idx++;
        }
        if (selectedCaIndex >= 0) {
            cCa.setSelectedIndex(selectedCaIndex);
        }
        g.gridx = 1; g.gridy = 2; g.gridwidth = 2; g.weightx = 1;
        f.add(cCa, g);

        g.gridx = 0; g.gridy = 3; g.gridwidth = 1; g.weightx = 0;
        f.add(new JLabel("Giờ vào (HH:mm):"), g);
        JTextField txtGioVao = new JTextField(selected.getGioVao() != null ? selected.getGioVao().format(fTime) : "");
        g.gridx = 1; g.gridy = 3; g.gridwidth = 2; g.weightx = 1;
        f.add(txtGioVao, g);

        g.gridx = 0; g.gridy = 4; g.gridwidth = 1; g.weightx = 0;
        f.add(new JLabel("Giờ ra (HH:mm):"), g);
        JTextField txtGioRa = new JTextField(selected.getGioRa() != null ? selected.getGioRa().format(fTime) : "");
        g.gridx = 1; g.gridy = 4; g.gridwidth = 2; g.weightx = 1;
        f.add(txtGioRa, g);

        g.gridx = 0; g.gridy = 5; g.gridwidth = 1; g.weightx = 0;
        f.add(new JLabel("Trạng thái:"), g);
        JComboBox<String> cTT = new JComboBox<>(new String[]{"Đúng giờ", "Đi muộn", "Về sớm", "Vắng mặt"});
        ChamCong.TrangThai currentTrangThai = selected.getTrangThai() != null ? selected.getTrangThai() : ChamCong.TrangThai.DUNG_GIO;
        cTT.setSelectedIndex(switch (currentTrangThai) {
            case DI_MUON -> 1;
            case VE_SOM -> 2;
            case VANG_MAT -> 3;
            default -> 0;
        });
        g.gridx = 1; g.gridy = 5; g.gridwidth = 2; g.weightx = 1;
        f.add(cTT, g);

        g.gridx = 0; g.gridy = 6; g.gridwidth = 1; g.weightx = 0; g.anchor = GridBagConstraints.NORTHWEST;
        f.add(new JLabel("Lý do chỉnh sửa:"), g);
        JTextArea txtLyDo = new JTextArea(4, 24);
        txtLyDo.setLineWrap(true);
        txtLyDo.setWrapStyleWord(true);
        txtLyDo.setFont(UIFonts.TEXT_NORMAL);
        g.gridx = 1; g.gridy = 6; g.gridwidth = 2; g.weightx = 1; g.fill = GridBagConstraints.BOTH;
        f.add(new JScrollPane(txtLyDo), g);

        JButton bLuu = btn("Lưu thay đổi", UIColors.SUCCESS_GREEN);
        bLuu.setFont(UIFonts.BOLD_NORMAL);
        bLuu.setPreferredSize(new Dimension(140, 38));
        g.gridx = 0; g.gridy = 7; g.gridwidth = 3; g.weightx = 0; g.fill = GridBagConstraints.HORIZONTAL; g.anchor = GridBagConstraints.CENTER;
        f.add(bLuu, g);

        ChamCong.TrangThai[] tts = {
            ChamCong.TrangThai.DUNG_GIO, ChamCong.TrangThai.DI_MUON,
            ChamCong.TrangThai.VE_SOM, ChamCong.TrangThai.VANG_MAT
        };
        bLuu.addActionListener(e -> {
            try {
                LocalDate ngay = LocalDate.parse(txtNgay.getText().trim(), fDate);
                String maCa = ((String) cCa.getSelectedItem()).split(" - ")[0].trim();
                String[] vao = txtGioVao.getText().trim().split(":");
                String[] ra = txtGioRa.getText().trim().split(":");
                LocalDateTime gioVao = ngay.atTime(Integer.parseInt(vao[0]), Integer.parseInt(vao[1]));
                LocalDateTime gioRa = ngay.atTime(Integer.parseInt(ra[0]), Integer.parseInt(ra[1]));
                KetQua<ChamCong> res = svc.suaChamCongThuCong(
                    selected.getId(), ngay, maCa, gioVao, gioRa, tts[cTT.getSelectedIndex()], txtLyDo.getText().trim());
                if (res.isSuccess()) { DialogUtil.showSuccess(d, res.getMessage()); d.dispose(); loadCC(); }
                else DialogUtil.showError(d, res.getMessage());
            } catch (Exception ex) {
                DialogUtil.showError(d, "Kiểm tra lại ngày giờ theo định dạng dd/MM/yyyy và HH:mm.", "Lỗi định dạng");
            }
        });

        d.setContentPane(f);
        d.setVisible(true);
    }

    private void addDetailRow(JPanel panel, GridBagConstraints g, int row, String label, String value) {
        g.gridx = 0; g.gridy = row; g.gridwidth = 1; g.weightx = 0; g.fill = GridBagConstraints.NONE; g.anchor = GridBagConstraints.NORTHWEST;
        panel.add(boldLabel(label), g);
        g.gridx = 1; g.weightx = 1; g.fill = GridBagConstraints.HORIZONTAL;
        JLabel val = new JLabel(value != null && !value.isBlank() ? value : "-");
        val.setFont(UIFonts.TEXT_NORMAL);
        panel.add(val, g);
    }

    private void addTextAreaRow(JPanel panel, GridBagConstraints g, int row, String label, String value) {
        g.gridx = 0; g.gridy = row; g.gridwidth = 1; g.weightx = 0; g.fill = GridBagConstraints.NONE; g.anchor = GridBagConstraints.NORTHWEST;
        panel.add(boldLabel(label), g);
        JTextArea area = new JTextArea(value != null && !value.isBlank() ? value : "-");
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFont(UIFonts.TEXT_NORMAL);
        area.setOpaque(false);
        area.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));
        g.gridx = 1; g.weightx = 1; g.fill = GridBagConstraints.HORIZONTAL;
        panel.add(area, g);
    }

    private ChamCong getSelectedChamCong() {
        if (tableChamCong == null || dsChamCongTongHop == null || dsChamCongTongHop.isEmpty()) return null;
        int viewRow = tableChamCong.getSelectedRow();
        if (viewRow < 0) return null;
        int modelRow = tableChamCong.convertRowIndexToModel(viewRow);
        if (modelRow < 0 || modelRow >= dsChamCongTongHop.size()) return null;
        return dsChamCongTongHop.get(modelRow);
    }

    private String formatTrangThaiNV(String tt) {
        return HRMConstants.display(tt);
    }

    private JLabel boldLabel(String text) {
        JLabel l = new JLabel(text); l.setFont(com.hrm.util.UIFonts.BOLD_NORMAL); return l;
    }

    // ===============================================
    //  TAB 2 -- QUAN LY CA LAM
    // ===============================================
    private JPanel tabCaLam() {
        JPanel p=panel(); JPanel hdr=new JPanel(new BorderLayout()); hdr.setOpaque(false);
        JLabel l=new JLabel("Danh sách ca làm việc"); l.setFont(new Font("Segoe UI",Font.BOLD,16)); l.setForeground(UIColors.TEXT_DARK);
        hdr.add(l,BorderLayout.WEST);
        l.setVisible(false);
        JPanel bp=new JPanel(new FlowLayout(FlowLayout.RIGHT,10,0)); bp.setOpaque(false);
        JButton bT=btn("+ Thêm ca mới",UIColors.SUCCESS_GREEN); bT.addActionListener(e->dlgCaLam(null));
        JButton bS=btn("Sửa",UIColors.PRIMARY_PURPLE); bS.addActionListener(e->{
            int row=tableCaLam.getSelectedRow(); if(row<0){DialogUtil.showInfo(this,"Chọn ca làm việc.");return;}
            int mRow=tableCaLam.convertRowIndexToModel(row);
            String maCa = (String) modelCaLam.getValueAt(mRow,0);
            CaLam ca = svc.getAllCaLam().stream().filter(c -> c.getId().equals(maCa)).findFirst().orElse(null);
            if(ca!=null)dlgCaLam(ca);});
        JButton bX=btn("Xóa",UIColors.DANGER_RED); bX.addActionListener(e->xoaCaLam());
        bp.add(bT); bp.add(bS); bp.add(bX); hdr.add(bp,BorderLayout.EAST); p.add(hdr,BorderLayout.NORTH);
        String[]cols={"Mã ca","Tên ca","Giờ bắt đầu","Giờ kết thúc","Số giờ chuẩn","Cho phép OT","Trạng thái"};
        modelCaLam=mdl(cols); tableCaLam=tbl(modelCaLam);
        tableCaLam.getColumnModel().getColumn(6).setCellRenderer(new com.hrm.gui.components.StatusCellRenderer());
        for (int i : new int[]{2, 3, 4, 5}) tableCaLam.getColumnModel().getColumn(i).setCellRenderer(CENTER_R);
        sorterCaLam = new TableRowSorter<>(modelCaLam); tableCaLam.setRowSorter(sorterCaLam);
        txtTimKiemCaLam = UIHelper.createSearchField("Tìm theo mã ca, tên ca...");
        UIHelper.attachTextSearch(txtTimKiemCaLam, sorterCaLam, 0, 1);
        JPanel searchCaLam = new JPanel(new FlowLayout(FlowLayout.LEFT,8,2)); searchCaLam.setOpaque(false);
        searchCaLam.add(new JLabel("Tìm kiếm:")); searchCaLam.add(txtTimKiemCaLam);
        hdr.add(searchCaLam, BorderLayout.CENTER);
        loadCaLam(); p.add(new JScrollPane(tableCaLam),BorderLayout.CENTER); return p;
    }

    private void loadCaLam() {
        modelCaLam.setRowCount(0); DateTimeFormatter f=DateTimeFormatter.ofPattern("HH:mm");
        for(CaLam ca:svc.getAllCaLam()) modelCaLam.addRow(new Object[]{ca.getId(),ca.getTenCaLam(),
            ca.getGioBatDau().format(f),ca.getGioKetThuc().format(f),ca.getSoGioChuan(),
            ca.isChoPhepLamThem()?"Có":"Không",ca.conHoatDong()?"Hoạt động":"Ngừng hoạt động"});
    }

    private void dlgCaLam(CaLam ex) {
        boolean edit=ex!=null;
        JDialog d=new JDialog((Frame)SwingUtilities.getWindowAncestor(this),edit?"Sửa ca":"Thêm ca mới",true);
        d.setSize(450,380); d.setLocationRelativeTo(this);
        JPanel f=new JPanel(new GridBagLayout()); f.setBorder(new EmptyBorder(20,20,20,20));
        GridBagConstraints g=gbc(); DateTimeFormatter fmt=DateTimeFormatter.ofPattern("HH:mm");
        g.gridx=0; g.gridy=0; f.add(new JLabel("Mã ca:"),g);
        JTextField tMa=new JTextField(edit?ex.getId():"",20); tMa.setEditable(!edit);
        if(edit)tMa.setBackground(Color.WHITE); g.gridx=1; g.weightx=1; f.add(tMa,g);
        g.gridx=0; g.gridy=1; g.weightx=0; f.add(new JLabel("Tên ca:"),g);
        JTextField tTen=new JTextField(edit?ex.getTenCaLam():""); g.gridx=1; f.add(tTen,g);
        g.gridx=0; g.gridy=2; f.add(new JLabel("Giờ bắt đầu:"),g);
        JTextField tBD=new JTextField(edit?ex.getGioBatDau().format(fmt):"08:00"); g.gridx=1; f.add(tBD,g);
        g.gridx=0; g.gridy=3; f.add(new JLabel("Giờ kết thúc:"),g);
        JTextField tKT=new JTextField(edit?ex.getGioKetThuc().format(fmt):"17:00"); g.gridx=1; f.add(tKT,g);
        g.gridx=0; g.gridy=4; f.add(new JLabel("Số giờ chuẩn:"),g);
        JTextField tGio=new JTextField(edit?String.valueOf(ex.getSoGioChuan()):"8.0");
        g.gridx=1; f.add(tGio,g);
        g.gridx=0; g.gridy=5; f.add(new JLabel("Cho phép OT:"),g);
        JCheckBox chk=new JCheckBox("Có",!edit||ex.isChoPhepLamThem()); chk.setOpaque(false); g.gridx=1; f.add(chk,g);
        JButton bL=btn(edit?"Cập nhật":"Thêm mới",UIColors.PRIMARY_PURPLE); bL.setFont(new Font("Segoe UI",Font.BOLD,14));
        bL.addActionListener(e->{try{String ma=tMa.getText().trim(),ten=tTen.getText().trim();
            if(ma.isEmpty()||ten.isEmpty()){DialogUtil.showInfo(d,"Không để trống.");return;}
            double sg=Double.parseDouble(tGio.getText().trim());
            KetQua<CaLam> res=edit?svc.suaCaLam(ma,ten,tBD.getText().trim(),tKT.getText().trim(),sg,chk.isSelected())
                :svc.themCaLam(ma,ten,tBD.getText().trim(),tKT.getText().trim(),sg,chk.isSelected());
            if(res.isSuccess()){DialogUtil.showSuccess(d,res.getMessage());d.dispose();loadCaLam();}
            else DialogUtil.showError(d,res.getMessage());
        }catch(NumberFormatException x){DialogUtil.showInfo(d,"Số giờ phải là số.");}
         catch(Exception x){DialogUtil.showError(d,"Lỗi: "+x.getMessage());}});
        g.gridx=0; g.gridy=6; g.gridwidth=2; g.insets=new Insets(20,8,8,8); f.add(bL,g);
        d.setContentPane(f); d.setVisible(true);
    }

    private void xoaCaLam() {
        int row=tableCaLam.getSelectedRow();
        if(row<0){DialogUtil.showInfo(this,"Vui lòng chọn ca làm.");return;}
        int mRow=tableCaLam.convertRowIndexToModel(row);
        String ma=(String)modelCaLam.getValueAt(mRow,0),ten=(String)modelCaLam.getValueAt(mRow,1);
        if(((String)modelCaLam.getValueAt(mRow,6)).contains("Ngung")){DialogUtil.showInfo(this,"Ca làm đã ngừng.");return;}
        if(DialogUtil.showYesNo(this,"Xác nhận ngừng '"+ten+"'?","Xác nhận")){
            KetQua<?> res = svc.xoaCaLam(ma);
            if (res.isSuccess()) { DialogUtil.showSuccess(this, res.getMessage()); loadCaLam(); }
            else DialogUtil.showError(this, res.getMessage());
        }
    }

    // ===============================================
    //  TAB 3 -- DUYET DON OT
    // ===============================================
    private JPanel tabDuyetOT() {
        JPanel p=panel(); JPanel hdr=new JPanel(new BorderLayout()); hdr.setOpaque(false);
        JLabel l=new JLabel("Đơn đăng ký làm thêm giờ"); l.setFont(new Font("Segoe UI",Font.BOLD,16)); l.setForeground(UIColors.TEXT_DARK);
        hdr.add(l,BorderLayout.WEST);
        JPanel bp=new JPanel(new FlowLayout(FlowLayout.RIGHT,10,0)); bp.setOpaque(false);
        JButton bD=btn("Duyệt",UIColors.SUCCESS_GREEN); bD.addActionListener(e->duyetOT());
        JButton bTC=btn("Từ chối",UIColors.DANGER_RED); bTC.addActionListener(e->tuChoiOT());
        JButton bHS=btn("Chỉnh hệ số",UIColors.PRIMARY_PURPLE); bHS.addActionListener(e->chinhHeSo());
        JButton bR=new JButton("Làm mới"); bR.setFocusPainted(false); bR.addActionListener(e->loadOT());
        txtTimKiemOT = UIHelper.createSearchField("Tìm theo mã NV, lý do...");
        JPanel searchOT = new JPanel(new FlowLayout(FlowLayout.LEFT,8,2)); searchOT.setOpaque(false);
        searchOT.add(new JLabel("Tìm kiếm:")); searchOT.add(txtTimKiemOT);
        hdr.add(searchOT, BorderLayout.CENTER);
        bp.add(bD); bp.add(bTC); bp.add(bHS); bp.add(bR); hdr.add(bp,BorderLayout.EAST); p.add(hdr,BorderLayout.NORTH);
        String[]cols={"Mã DK","Mã NV","Ngày","Số giờ","Hệ số OT","Lý do","Trạng thái","Người duyệt"};
        modelOT=mdl(cols); tableDonOT=tbl(modelOT);
        tableDonOT.getColumnModel().getColumn(6).setCellRenderer(new com.hrm.gui.components.StatusCellRenderer());
        tableDonOT.getColumnModel().getColumn(2).setCellRenderer(CENTER_R);
        tableDonOT.getColumnModel().getColumn(3).setCellRenderer(CENTER_R);
        tableDonOT.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer(){
            @Override public Component getTableCellRendererComponent(JTable t,Object v,boolean s,boolean f,int r,int c){
                super.getTableCellRendererComponent(t,v,s,f,r,c); setHorizontalAlignment(CENTER);
                setFont(new Font("Segoe UI",Font.BOLD,13)); String str=v!=null?v.toString():"";
                setForeground(str.contains("2.0")||str.contains("3.0")?UIColors.DANGER_RED:UIColors.PRIMARY_PURPLE);
                return this;}});
        sorterOT = new TableRowSorter<>(modelOT); tableDonOT.setRowSorter(sorterOT);
        UIHelper.attachTextSearch(txtTimKiemOT, sorterOT, 1, 5);
        tableDonOT.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = tableDonOT.rowAtPoint(e.getPoint());
                    if (row < 0) return;
                    int maDK = (int) modelOT.getValueAt(tableDonOT.convertRowIndexToModel(row), 0);
                    DangKyLamThem don = svc.getDonLamThemById(maDK);
                    if (don != null) hienThiChiTietOT(don);
                }
            }
        });
        loadOT(); p.add(new JScrollPane(tableDonOT),BorderLayout.CENTER);
        JPanel info=new JPanel(new FlowLayout(FlowLayout.LEFT,10,5)); info.setOpaque(false);
        info.setBorder(new EmptyBorder(5,0,0,0));
        info.add(lbl("Hệ số:","x1.5 (thường), x2.0 (cuối tuần), x3.0 (lễ)",UIColors.TEXT_DARK));
        p.add(info,BorderLayout.SOUTH); return p;
    }

    private void loadOT() {
        modelOT.setRowCount(0);
        DateTimeFormatter f = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        List<DangKyLamThem> dsOT = canManage
            ? svc.getDonLamThemTatCa()
            : svc.getDonLamThemTatCaByApproveScope(maNVCaNhan);
        for (DangKyLamThem don : dsOT) {
            String maNhanVien = svc.getMaNhanVienById(don.getMaNV());
            String tenNV = don.getTenNhanVien() != null ? don.getTenNhanVien() : maNhanVien;
            modelOT.addRow(new Object[]{
                don.getMaDK(),
                maNhanVien + " - " + tenNV,
                don.getNgay() != null ? don.getNgay().format(f) : "-",
                String.format("%.1f", don.getSoGio()),
                "x" + don.getHeSoOT(),
                don.getLyDo(),
                don.getTrangThai().getDisplayName(),
                don.getTenNguoiDuyet() != null ? don.getTenNguoiDuyet() : "-"
            });
        }
    }

    private void duyetOT() {
        int row=tableDonOT.getSelectedRow();
        if(row<0){DialogUtil.showInfo(this,"Chọn đơn."); return;}
        if(!canApproveOT){DialogUtil.showWarn(this,"Bạn không có quyền duyệt đơn OT."); return;}
        int mRow=tableDonOT.convertRowIndexToModel(row);
        int maDK=(int)modelOT.getValueAt(mRow,0);
        String tt=(String)modelOT.getValueAt(mRow,6);
        if (!DangKyLamThem.TrangThai.CHO_DUYET.getDisplayName().equals(tt)) {
            DialogUtil.showInfo(this, "Đơn đã xử lý."); return;
        }
        String[]opts={"x1.5 (ngày thường)","x2.0 (cuối tuần)","x3.0 (ngày lễ)"};
        String ch=(String)JOptionPane.showInputDialog(this,"Chọn hệ số OT:","Hệ số OT",
            JOptionPane.QUESTION_MESSAGE,null,opts,opts[0]); if(ch==null)return;
        double hs=ch.contains("2.0")?2.0:ch.contains("3.0")?3.0:1.5;
        String maNVNguoiDuyet = getMaNVNguoiDuyet();
        if(maNVNguoiDuyet==null){DialogUtil.showError(this,"Không tìm thấy thông tin nhân viên."); return;}
        KetQua<?> res=svc.duyetDonLamThem(maDK,maNVNguoiDuyet,hs);
        if(res.isSuccess()) DialogUtil.showSuccess(this,res.getMessage()); else DialogUtil.showError(this,res.getMessage()); loadOT();
    }

    private void tuChoiOT() {
        int row=tableDonOT.getSelectedRow();
        if(row<0){DialogUtil.showInfo(this,"Chọn đơn."); return;}
        if(!canApproveOT){DialogUtil.showWarn(this,"Bạn không có quyền duyệt đơn OT."); return;}
        int mRow=tableDonOT.convertRowIndexToModel(row);
        int maDK=(int)modelOT.getValueAt(mRow,0); String tt=(String)modelOT.getValueAt(mRow,6);
        if (!DangKyLamThem.TrangThai.CHO_DUYET.getDisplayName().equals(tt)) {
            DialogUtil.showInfo(this, "Đơn đã xử lý."); return;
        }
        String maNVNguoiDuyet = getMaNVNguoiDuyet();
        if(maNVNguoiDuyet==null){DialogUtil.showError(this,"Không tìm thấy thông tin nhân viên."); return;}
        KetQua<?> res = svc.tuChoiDonLamThem(maDK,maNVNguoiDuyet);
        if (res.isSuccess()) { DialogUtil.showSuccess(this, res.getMessage()); loadOT(); }
        else DialogUtil.showError(this, res.getMessage());
    }

    private String getMaNVNguoiDuyet() {
        return svc.getMaNVByTaiKhoan(currentUser.getId());
    }

    private void hienThiChiTietOT(DangKyLamThem don) {
        java.awt.Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dlg = new JDialog(owner, "Chi tiết đơn OT — #" + don.getMaDK(),
                java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        JPanel content = new JPanel(new GridBagLayout());
        content.setBorder(new EmptyBorder(18, 22, 10, 22));
        content.setBackground(Color.WHITE);
        GridBagConstraints gl = new GridBagConstraints();
        gl.insets = new java.awt.Insets(5, 4, 5, 12); gl.anchor = GridBagConstraints.WEST;
        GridBagConstraints gv = new GridBagConstraints();
        gv.insets = new java.awt.Insets(5, 0, 5, 4);
        gv.anchor = GridBagConstraints.WEST; gv.fill = GridBagConstraints.HORIZONTAL; gv.weightx = 1.0;

        DateTimeFormatter fDate = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter fDT   = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        Object[][] rows = {
            {"Mã đăng ký:",      "#" + don.getMaDK()},
            {"Mã nhân viên:",    don.getMaNV() != null ? don.getMaNV() : "-"},
            {"Tên nhân viên:",   don.getTenNhanVien() != null ? don.getTenNhanVien() : "-"},
            {"Ngày OT:",         don.getNgay() != null ? don.getNgay().format(fDate) : "-"},
            {"Số giờ OT:",       String.format("%.1f giờ", don.getSoGio())},
            {"Giờ bắt đầu OT:", don.getGioVaoOT() != null ? don.getGioVaoOT().toString() : "-"},
            {"Giờ kết thúc OT:", don.getGioRaOT()  != null ? don.getGioRaOT().toString()  : "-"},
            {"Hệ số OT:",        "x" + don.getHeSoOT()},
            {"Lý do:",           don.getLyDo() != null ? don.getLyDo() : "-"},
            {"Trạng thái:",      don.getTrangThai().getDisplayName()},
            {"Người duyệt:",     don.getTenNguoiDuyet() != null ? don.getTenNguoiDuyet()
                                 : (don.getNguoiDuyet() != null ? don.getNguoiDuyet() : "-")},
            {"Nhận xét:",        don.getNhanXet() != null && !don.getNhanXet().isBlank() ? don.getNhanXet() : "-"},
            {"Ngày tạo:",        don.getNgayTao()   != null ? don.getNgayTao().format(fDT)   : "-"},
            {"Ngày duyệt:",      don.getNgayDuyet() != null ? don.getNgayDuyet().format(fDT) : "-"},
        };

        for (int i = 0; i < rows.length; i++) {
            gl.gridx = 0; gl.gridy = i; gv.gridx = 1; gv.gridy = i;
            JLabel lbl = new JLabel(rows[i][0].toString());
            lbl.setFont(com.hrm.util.UIFonts.BOLD_NORMAL); lbl.setForeground(UIColors.TEXT_DARK);
            JLabel val = new JLabel(rows[i][1].toString());
            val.setFont(com.hrm.util.UIFonts.TEXT_NORMAL);
            if (i == 9) { // Trạng thái
                DangKyLamThem.TrangThai tt = don.getTrangThai();
                val.setFont(com.hrm.util.UIFonts.BOLD_NORMAL);
                val.setForeground(tt == DangKyLamThem.TrangThai.DA_DUYET ? UIColors.SUCCESS_GREEN
                    : tt == DangKyLamThem.TrangThai.TU_CHOI ? UIColors.DANGER_RED : UIColors.PRIMARY_PURPLE);
            }
            content.add(lbl, gl); content.add(val, gv);
        }

        JButton btnDong = btn("Đóng", UIColors.PRIMARY_PURPLE);
        btnDong.addActionListener(e -> dlg.dispose());
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 8));
        btnRow.setOpaque(false); btnRow.add(btnDong);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Color.WHITE);
        wrapper.add(content, BorderLayout.CENTER); wrapper.add(btnRow, BorderLayout.SOUTH);
        dlg.setContentPane(wrapper);
        dlg.pack();
        dlg.setMinimumSize(new Dimension(420, dlg.getHeight()));
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    private void chinhHeSo() {
        int row=tableDonOT.getSelectedRow();
        if(row<0){DialogUtil.showInfo(this,"Chọn đơn."); return;}
        if(!canApproveOT){DialogUtil.showWarn(this,"Bạn không có quyền chỉnh hệ số OT."); return;}
        int maDK=(int)modelOT.getValueAt(tableDonOT.convertRowIndexToModel(row),0);
        String input=JOptionPane.showInputDialog(this,"Nhập hệ số mới (vd: 1.5):","Chỉnh hệ số",JOptionPane.QUESTION_MESSAGE);
        if(input==null||input.trim().isEmpty())return;
        try {
            double hs = Double.parseDouble(input.trim());
            KetQua<?> res = svc.capNhatHeSoOT(maDK, hs);
            if (res.isSuccess()) { DialogUtil.showSuccess(this, res.getMessage()); loadOT(); }
            else DialogUtil.showError(this, res.getMessage());
        } catch (NumberFormatException e) {
            DialogUtil.showInfo(this, "Hệ số phải là số hợp lệ (vd: 1.5).");
        }
    }

    // ===============================================
    //  TAB 5 -- PHU CAP & KHAU TRU
    // ===============================================
    private JPanel tabPhuCap() {
        JPanel p = panel();
        JPanel hdr = new JPanel(new BorderLayout()); hdr.setOpaque(false);
        JLabel l = new JLabel("Cấu hình phụ cấp & khấu trừ");
        l.setFont(com.hrm.util.UIFonts.HEADER_SUB); l.setForeground(UIColors.TEXT_DARK);
        hdr.add(l, BorderLayout.WEST);
        l.setVisible(false);
        txtTimKiemPC = UIHelper.createSearchField("Tìm theo tên khoản, loại...");
        JPanel searchPC = new JPanel(new FlowLayout(FlowLayout.LEFT,8,2)); searchPC.setOpaque(false);
        searchPC.add(new JLabel("Tìm kiếm:")); searchPC.add(txtTimKiemPC);
        hdr.add(searchPC, BorderLayout.CENTER);
        JPanel bp = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0)); bp.setOpaque(false);
        JButton bThem = btn("+ Thêm khoản mới", UIColors.SUCCESS_GREEN); bThem.addActionListener(e -> dlgPhuCap(null));
        JButton bSua  = btn("Sửa", UIColors.PRIMARY_PURPLE);
        bSua.addActionListener(e -> {
            int row = tablePC.getSelectedRow();
            if (row < 0) { DialogUtil.showInfo(this, "Chọn khoản để sửa."); return; }
            int maPC = (int) modelPC.getValueAt(tablePC.convertRowIndexToModel(row), 0);
            CauHinhPhuCap pc = svc.getCauHinhPCById(maPC);
            if (pc != null) dlgPhuCap(pc);
        });
        JButton bXoa = btn("Xóa", UIColors.DANGER_RED); bXoa.addActionListener(e -> xoaPhuCap());
        bp.add(bThem); bp.add(bSua); bp.add(bXoa);
        hdr.add(bp, BorderLayout.EAST); p.add(hdr, BorderLayout.NORTH);
        String[] cols = {"Mã", "Loại", "Tên khoản", "Kiểu tính", "Giá trị", "Nguồn", "Trạng thái"};
        modelPC = mdl(cols); tablePC = tbl(modelPC);
        tablePC.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                super.getTableCellRendererComponent(t, v, s, f, r, c);
                String str = v != null ? v.toString() : "";
                setForeground(str.contains("cap") ? UIColors.SUCCESS_GREEN : UIColors.DANGER_RED);
                setFont(com.hrm.util.UIFonts.BOLD_NORMAL); setHorizontalAlignment(CENTER); return this;
            }
        });
        tablePC.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                super.getTableCellRendererComponent(t, v, s, f, r, c);
                setHorizontalAlignment(RIGHT); setForeground(com.hrm.util.UIColors.TEXT_DARK);
                setFont(com.hrm.util.UIFonts.BOLD_NORMAL); return this;
            }
        });
        tablePC.getColumnModel().getColumn(6).setCellRenderer(new com.hrm.gui.components.StatusCellRenderer());
        sorterPC = new TableRowSorter<>(modelPC); tablePC.setRowSorter(sorterPC);
        UIHelper.attachTextSearch(txtTimKiemPC, sorterPC, 1, 2);
        loadPhuCap(); p.add(new JScrollPane(tablePC), BorderLayout.CENTER);
        JPanel info = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5)); info.setOpaque(false);
        info.setBorder(new EmptyBorder(5, 0, 0, 0));
        info.add(lbl("Kiểu tính:", "'Cố định' = số tiền cụ thể, '% Lương CB' = phần trăm lương cơ bản", Color.GRAY));
        p.add(info, BorderLayout.SOUTH);
        return p;
    }

    private void loadPhuCap() {
        modelPC.setRowCount(0);
        for (CauHinhPhuCap pc : svc.getAllCauHinhPC()) {
            String giaTri = pc.getKieuTinh() == CauHinhPhuCap.KieuTinh.PHAN_TRAM
                ? pc.hienThiGiaTri() : fmtTien(pc.getGiaTri());
            modelPC.addRow(new Object[]{pc.getId(), pc.getLoai().getDisplayName(),
                pc.getTenKhoan(), pc.getKieuTinh().getDisplayName(), giaTri,
                pc.getNguon(), pc.isHoatDong() ? "Hoạt động" : "Ngừng"});
        }
    }

    private void dlgPhuCap(CauHinhPhuCap existing) {
        boolean edit = (existing != null);
        String title = edit ? "Sửa: " + existing.getTenKhoan() : "Thêm khoản mới";
        JDialog d = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), title, true);
        d.setSize(500, 400); d.setLocationRelativeTo(this);
        JPanel f = new JPanel(new GridBagLayout()); f.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints g = gbc();
        g.gridx=0; g.gridy=0; f.add(new JLabel("Loại:"),g);
        JComboBox<String> cboLoai = new JComboBox<>(new String[]{"Phụ cấp", "Khấu trừ"});
        if (edit) cboLoai.setSelectedIndex(existing.getLoai() == ThanhPhanLuong.Loai.PHU_CAP ? 0 : 1);
        g.gridx=1; g.weightx=1; f.add(cboLoai,g);
        g.gridx=0; g.gridy=1; g.weightx=0; f.add(new JLabel("Tên khoản:"),g);
        JTextField tTen = new JTextField(edit ? existing.getTenKhoan() : "", 25);
        g.gridx=1; g.weightx=1; f.add(tTen,g);
        g.gridx=0; g.gridy=2; g.weightx=0; f.add(new JLabel("Kiểu tính:"),g);
        JComboBox<String> cboKieu = new JComboBox<>(new String[]{"Cố định (số tiền)", "% Lương cơ bản"});
        if (edit) cboKieu.setSelectedIndex(existing.getKieuTinh() == CauHinhPhuCap.KieuTinh.CO_DINH ? 0 : 1);
        g.gridx=1; f.add(cboKieu,g);
        g.gridx=0; g.gridy=3; g.weightx=0; f.add(new JLabel("Giá trị:"),g);
        JTextField tGiaTri = new JTextField(edit ? String.valueOf(existing.getGiaTri()) : "");
        g.gridx=1; f.add(tGiaTri,g);
        g.gridx=1; g.gridy=4;
        JLabel lblHint = new JLabel("VD: 500000 (cố định) hoặc 8 (phần trăm)");
        lblHint.setFont(new Font("Segoe UI", Font.ITALIC, 11)); lblHint.setForeground(Color.GRAY);
        f.add(lblHint,g);
        cboKieu.addActionListener(e -> lblHint.setText(cboKieu.getSelectedIndex()==0
            ? "VD: 500000 (dong), 1000000 (dong)" : "VD: 8 (= 8% luong co ban), 1.5 (= 1.5%)"));
        g.gridx=0; g.gridy=5; g.weightx=0; f.add(new JLabel("Nguồn:"),g);
        JComboBox<String> cboNguon = new JComboBox<>(new String[]{"Công ty", "Luật định", "Chức vụ", "Khác"});
        if (edit) cboNguon.setSelectedItem(existing.getNguon());
        g.gridx=1; f.add(cboNguon,g);
        JButton bLuu = btn(edit ? "Cập nhật" : "Thêm mới", UIColors.PRIMARY_PURPLE);
        bLuu.setFont(UIFonts.BOLD_NORMAL);
        bLuu.addActionListener(e -> {
            try {
                String ten = tTen.getText().trim();
                if (ten.isEmpty()) { DialogUtil.showWarn(d, "Tên khoản không được để trống."); return; }
                double giaTri = Double.parseDouble(tGiaTri.getText().trim());
                if (giaTri <= 0) { DialogUtil.showWarn(d, "Giá trị phải lớn hơn 0."); return; }
                ThanhPhanLuong.Loai loai = cboLoai.getSelectedIndex()==0 ? ThanhPhanLuong.Loai.PHU_CAP : ThanhPhanLuong.Loai.KHAU_TRU;
                CauHinhPhuCap.KieuTinh kieu = cboKieu.getSelectedIndex()==0 ? CauHinhPhuCap.KieuTinh.CO_DINH : CauHinhPhuCap.KieuTinh.PHAN_TRAM;
                String nguon = (String) cboNguon.getSelectedItem();
                KetQua<?> res = edit
                    ? svc.suaCauHinhPC(existing.getId(), loai, ten, kieu, giaTri, nguon)
                    : svc.themCauHinhPC(loai, ten, kieu, giaTri, nguon);
                if (res.isSuccess()) { DialogUtil.showSuccess(d, res.getMessage()); d.dispose(); loadPhuCap(); }
                else DialogUtil.showError(d, res.getMessage());
            } catch (NumberFormatException ex) {
                DialogUtil.showError(d, "Giá trị phải là số.\nVD: 500000 hoặc 8");
            }
        });
        g.gridx=0; g.gridy=6; g.gridwidth=2; g.insets=new Insets(20,8,8,8); f.add(bLuu,g);
        d.setContentPane(f); d.setVisible(true);
    }

    private void xoaPhuCap() {
        int row = tablePC.getSelectedRow();
        if (row < 0) { DialogUtil.showInfo(this, "Chọn khoản để xóa."); return; }
        int mRow = tablePC.convertRowIndexToModel(row);
        int maPC = (int) modelPC.getValueAt(mRow, 0);
        String ten = (String) modelPC.getValueAt(mRow, 2);
        String tt  = (String) modelPC.getValueAt(mRow, 6);
        if (tt.contains("Ngung")) { DialogUtil.showInfo(this, "Khoản này đã ngừng."); return; }
        if (DialogUtil.showYesNoWarning(this, "Ngừng khoản '" + ten + "'?\n(Sẽ không áp dụng khi tính lương)", "Xác nhận")) {
            KetQua<Void> res = svc.xoaCauHinhPC(maPC);
            if (res.isSuccess()) DialogUtil.showSuccess(this, res.getMessage()); else DialogUtil.showError(this, res.getMessage()); loadPhuCap();
        }
    }

    // ===============================================
    //  TAB -- CHAM CONG CA NHAN (HR/Manager/Employee)
    // ===============================================
    private JPanel tabCaNhan() {
        JPanel p = panel();
        JPanel topPanel = new JPanel();
        topPanel.setOpaque(false);
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        // Dong ho thoi gian thuc
        JLabel lblTime = new JLabel();
        lblTime.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTime.setForeground(UIColors.PRIMARY_PURPLE);
        DateTimeFormatter fDT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        lblTime.setText(LocalDateTime.now().format(fDT));
        clockTimer = new javax.swing.Timer(1000, e2 -> lblTime.setText(LocalDateTime.now().format(fDT)));
        clockTimer.start();
        p.addHierarchyListener(e -> {
            if ((e.getChangeFlags() & java.awt.event.HierarchyEvent.DISPLAYABILITY_CHANGED) != 0
                    && !p.isDisplayable() && clockTimer != null) {
                clockTimer.stop();
            }
        });
        JPanel clockRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5)); clockRow.setOpaque(false);
        clockRow.add(lblTime); topPanel.add(clockRow);
        // Tim kiem nhan vien
        JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5)); searchRow.setOpaque(false);
        JLabel lblMaNhanVienValue = new JLabel(maNVCaNhan != null ? maNVCaNhan : "(Chưa có)");
        lblMaNhanVienValue.setFont(com.hrm.util.UIFonts.BOLD_NORMAL);
        searchRow.add(new JLabel("Mã nhân viên:")); searchRow.add(lblMaNhanVienValue);
        topPanel.add(searchRow);
        // Panel thong tin nhan vien
        infoPanelCN = new JPanel(new GridLayout(0, 2, 8, 6));
        infoPanelCN.setBackground(com.hrm.util.UIColors.LIGHT_PURPLE);
        infoPanelCN.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIColors.PRIMARY_PURPLE, 1),
            new EmptyBorder(10, 12, 10, 12)));
        infoPanelCN.setVisible(false);
        lblHoTenCNCN     = new JLabel(""); lblHoTenCNCN.setFont(com.hrm.util.UIFonts.BOLD_NORMAL);
        lblEmailCNCN     = new JLabel(""); lblEmailCNCN.setFont(com.hrm.util.UIFonts.TEXT_NORMAL);
        lblChucVuCNCN    = new JLabel(""); lblChucVuCNCN.setFont(com.hrm.util.UIFonts.TEXT_NORMAL);
        lblPhongBanCNCN  = new JLabel(""); lblPhongBanCNCN.setFont(com.hrm.util.UIFonts.TEXT_NORMAL);
        lblTrangThaiCNCN = new JLabel(""); lblTrangThaiCNCN.setFont(com.hrm.util.UIFonts.TEXT_NORMAL);
        infoPanelCN.add(boldLabel("Họ tên:"));     infoPanelCN.add(lblHoTenCNCN);
        infoPanelCN.add(boldLabel("Email:"));      infoPanelCN.add(lblEmailCNCN);
        infoPanelCN.add(boldLabel("Chức vụ:"));    infoPanelCN.add(lblChucVuCNCN);
        infoPanelCN.add(boldLabel("Phòng ban:"));  infoPanelCN.add(lblPhongBanCNCN);
        infoPanelCN.add(boldLabel("Trạng thái:")); infoPanelCN.add(lblTrangThaiCNCN);
        topPanel.add(infoPanelCN);
        // Trang thai cham cong + nut hanh dong
        lblCaNhanStatus = new JLabel("Chưa chấm công ngày hôm nay");
        lblCaNhanStatus.setFont(UIFonts.BOLD_NORMAL);
        // Dropdown chọn ca làm
        dsCaLamCN = svc.getActiveCaLam();
        cboCaLamCaNhan = new JComboBox<>(dsCaLamCN.toArray(new CaLam[0]));
        cboCaLamCaNhan.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof CaLam ca)
                    setText(ca.getTenCaLam() + "  (" + ca.getGioBatDau() + " - " + ca.getGioKetThuc() + ")");
                return this;
            }
        });
        cboCaLamCaNhan.setFont(com.hrm.util.UIFonts.TEXT_NORMAL);
        cboCaLamCaNhan.setPreferredSize(new Dimension(270, 28));
        CaLam caGoiY = svc.getCaLamGoiY();
        if (caGoiY != null) cboCaLamCaNhan.setSelectedItem(caGoiY);
        JLabel lblChonCa = new JLabel("Ca làm:");
        lblChonCa.setFont(com.hrm.util.UIFonts.BOLD_NORMAL);
        JLabel hintCaLam = new JLabel("(gợi ý theo giờ hiện tại)");
        hintCaLam.setFont(new Font("Segoe UI", Font.ITALIC, 11)); hintCaLam.setForeground(Color.GRAY);
        btnCheckInCN  = btn("Check-in",  UIColors.SUCCESS_GREEN);
        btnCheckOutCN = btn("Check-out", UIColors.DANGER_RED);
        JCheckBox chkOT = new JCheckBox("Ca này là OT");
        chkOT.setOpaque(false); chkOT.setFont(com.hrm.util.UIFonts.BOLD_NORMAL);
        chkOT.setForeground(UIColors.PRIMARY_PURPLE); chkOT.setEnabled(false);
        chkOT.setToolTipText("Chỉ có thể chọn khi bạn có đơn OT đã được duyệt cho hôm nay");
        btnCheckInCN.addActionListener(e -> {
            CaLam caChon = (CaLam) cboCaLamCaNhan.getSelectedItem();
            if (caChon == null) { DialogUtil.showWarn(this, "Vui lòng chọn ca làm."); return; }
            KetQua<ChamCong> res = svc.checkIn(maNVCaNhan, caChon.getId(), chkOT.isSelected());
            if (res.isSuccess()) DialogUtil.showSuccess(this, res.getMessage()); else DialogUtil.showError(this, res.getMessage());
            refreshCaNhan(chkOT); loadLichSuCaNhan();
        });
        btnCheckOutCN.addActionListener(e -> {
            KetQua<ChamCong> res = svc.checkOut(maNVCaNhan);
            if (res.isSuccess()) DialogUtil.showSuccess(this, res.getMessage()); else DialogUtil.showError(this, res.getMessage());
            refreshCaNhan(chkOT); loadLichSuCaNhan();
        });
        JPanel caLamRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 3)); caLamRow.setOpaque(false);
        caLamRow.add(lblChonCa); caLamRow.add(cboCaLamCaNhan); caLamRow.add(hintCaLam);
        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5)); actionRow.setOpaque(false);
        actionRow.add(btnCheckInCN); actionRow.add(btnCheckOutCN); actionRow.add(chkOT);
        chamCongStatusPanelCN = new JPanel(new BorderLayout(5, 5));
        chamCongStatusPanelCN.setBackground(com.hrm.util.UIColors.LIGHT_PURPLE);
        chamCongStatusPanelCN.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIColors.PRIMARY_PURPLE, 1),
            new EmptyBorder(10, 12, 10, 12)));
        chamCongStatusPanelCN.add(lblCaNhanStatus, BorderLayout.NORTH);
        chamCongStatusPanelCN.add(caLamRow, BorderLayout.CENTER);
        chamCongStatusPanelCN.add(actionRow, BorderLayout.SOUTH);
        chamCongStatusPanelCN.setVisible(false);
        topPanel.add(chamCongStatusPanelCN);
        p.add(topPanel, BorderLayout.NORTH);
        // Lich su cham cong
        histPanelCN = new JPanel(new BorderLayout(0, 5)); histPanelCN.setOpaque(false); histPanelCN.setVisible(false);
        JPanel histToolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5)); histToolbar.setOpaque(false);
        histToolbar.add(new JLabel("Tháng:")); cboThangCN = combMonth(); histToolbar.add(cboThangCN);
        histToolbar.add(new JLabel("Năm:")); cboNamCN = combYear(); histToolbar.add(cboNamCN);
        JButton bLoad = btn("Xem lịch sử", UIColors.PRIMARY_PURPLE);
        bLoad.addActionListener(e -> loadLichSuCaNhan()); histToolbar.add(bLoad);
        histPanelCN.add(histToolbar, BorderLayout.NORTH);
        String[] colsLS = {"Ngày", "Ca làm", "Giờ vào", "Giờ ra", "Số giờ", "OT", "Trạng thái"};
        modelLichSuCaNhan = mdl(colsLS); tableLichSuCaNhan = tbl(modelLichSuCaNhan);
        tableLichSuCaNhan.getColumnModel().getColumn(6).setCellRenderer(new com.hrm.gui.components.StatusCellRenderer());
        for (int i : new int[]{0, 1, 2, 3, 4}) tableLichSuCaNhan.getColumnModel().getColumn(i).setCellRenderer(CENTER_R);
        tableLichSuCaNhan.getColumnModel().getColumn(5).setCellRenderer(
            new DefaultTableCellRenderer() {
                @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                    super.getTableCellRendererComponent(t, v, s, f, r, c);
                    setHorizontalAlignment(CENTER); String str = v != null ? v.toString() : "";
                    setForeground("OT".equals(str) ? UIColors.PRIMARY_PURPLE : Color.GRAY);
                    setFont(new Font("Segoe UI", "OT".equals(str) ? Font.BOLD : Font.PLAIN, 13));
                    return this;
                }
            });
        tableLichSuCaNhan.getColumnModel().getColumn(5).setPreferredWidth(45);
        tableLichSuCaNhan.getColumnModel().getColumn(5).setMaxWidth(60);
        histPanelCN.add(new JScrollPane(tableLichSuCaNhan), BorderLayout.CENTER);
        statsPanelCN = new JPanel(new FlowLayout(FlowLayout.LEFT, 30, 5)); statsPanelCN.setOpaque(false);
        histPanelCN.add(statsPanelCN, BorderLayout.SOUTH);
        p.add(histPanelCN, BorderLayout.CENTER);
        maNVTimKiem = maNVCaNhan;
        loadThongTinCaNhan(p, chkOT);
        return p;
    }

    private void refreshCaNhan(JCheckBox chkOT) {
        if (maNVTimKiem == null || lblCaNhanStatus == null) return;
        if (!isViewingOwnAttendance()) {
            lblCaNhanStatus.setText("Chỉ có thể check-in/check-out cho chính bạn.");
            lblCaNhanStatus.setForeground(Color.GRAY);
            if (btnCheckInCN   != null) btnCheckInCN.setVisible(false);
            if (btnCheckOutCN  != null) btnCheckOutCN.setVisible(false);
            if (cboCaLamCaNhan != null) cboCaLamCaNhan.setVisible(false);
            if (chkOT != null) { chkOT.setVisible(false); chkOT.setEnabled(false); }
            return;
        }
        ChamCong cc = svc.getChamCongHomNay(maNVTimKiem);
        DateTimeFormatter fG = DateTimeFormatter.ofPattern("HH:mm");
        if (cc == null) {
            lblCaNhanStatus.setText("Chưa chấm công ngày hôm nay");
            lblCaNhanStatus.setForeground(Color.GRAY);
            if (btnCheckInCN   != null) btnCheckInCN.setVisible(true);
            if (btnCheckOutCN  != null) btnCheckOutCN.setVisible(false);
            if (cboCaLamCaNhan != null) cboCaLamCaNhan.setVisible(true);
            if (chkOT != null) {
                chkOT.setVisible(true); chkOT.setSelected(false);
                boolean coOT = svc.coOTDaDuyetHomNay(maNVTimKiem);
                chkOT.setEnabled(coOT);
                chkOT.setToolTipText(coOT ? "Tick vào đây nếu đây là ca OT của bạn"
                    : "Bạn chưa có đơn OT được duyệt cho hôm nay");
            }
        } else if (cc.getGioVao() == null) {
            String trangThai = cc.getTrangThai() != null ? cc.getTrangThai().getDisplayName() : "Đã có bản ghi";
            lblCaNhanStatus.setText(trangThai + " hôm nay"
                + (cc.getGhiChu() != null && !cc.getGhiChu().isBlank() ? " - " + cc.getGhiChu() : ""));
            lblCaNhanStatus.setForeground(
                cc.getTrangThai() == ChamCong.TrangThai.NGHI_PHEP ? UIColors.INFO_TEAL : Color.GRAY);
            if (btnCheckInCN   != null) btnCheckInCN.setVisible(false);
            if (btnCheckOutCN  != null) btnCheckOutCN.setVisible(false);
            if (cboCaLamCaNhan != null) cboCaLamCaNhan.setVisible(false);
            if (chkOT != null) { chkOT.setVisible(false); chkOT.setEnabled(false); }
        } else if (cc.getGioRa() == null) {
            String tenCa = cc.getTenCaLam() != null ? cc.getTenCaLam() : cc.getMaCaLam();
            String otTag = cc.isLaOT() ? "  [CA OT]" : "";
            lblCaNhanStatus.setText("Đã check-in lúc " + cc.getGioVao().format(fG) + "  -  Ca: " + tenCa + otTag);
            lblCaNhanStatus.setForeground(cc.isLaOT() ? UIColors.PRIMARY_PURPLE : UIColors.SUCCESS_GREEN);
            if (btnCheckInCN   != null) btnCheckInCN.setVisible(false);
            if (btnCheckOutCN  != null) btnCheckOutCN.setVisible(true);
            if (cboCaLamCaNhan != null) cboCaLamCaNhan.setVisible(false);
            if (chkOT != null) { chkOT.setVisible(false); chkOT.setEnabled(false); }
        } else {
            String otTag = cc.isLaOT() ? "  [CA OT]" : "";
            lblCaNhanStatus.setText("Đã check-out lúc " + cc.getGioRa().format(fG)
                + "  -  Số giờ làm: " + String.format("%.1f", cc.getSoGioLam()) + otTag);
            lblCaNhanStatus.setForeground(UIColors.INFO_TEAL);
            if (btnCheckInCN   != null) btnCheckInCN.setVisible(false);
            if (btnCheckOutCN  != null) btnCheckOutCN.setVisible(false);
            if (cboCaLamCaNhan != null) cboCaLamCaNhan.setVisible(false);
            if (chkOT != null) { chkOT.setVisible(false); chkOT.setEnabled(false); }
        }
    }

    private void loadThongTinCaNhan(JPanel panel, JCheckBox chkOT) {
        if (maNVCaNhan == null) {
            infoPanelCN.setVisible(false);
            chamCongStatusPanelCN.setVisible(false);
            if (histPanelCN != null) histPanelCN.setVisible(false);
            maNVTimKiem = null;
            return;
        }
        var info = svc.findNhanVienByMa(maNVCaNhan);
        if (info == null) {
            infoPanelCN.setVisible(false);
            chamCongStatusPanelCN.setVisible(false);
            if (histPanelCN != null) histPanelCN.setVisible(false);
            maNVTimKiem = null;
            return;
        }
        maNVTimKiem = info.maNV;
        lblHoTenCNCN.setText(info.hoTen);
        lblEmailCNCN.setText(info.email.isEmpty() ? "(Chưa có)" : info.email);
        lblChucVuCNCN.setText(info.tenChucVu.isEmpty() ? "(Chưa có)" : info.tenChucVu);
        lblPhongBanCNCN.setText(info.tenPhongBan.isEmpty() ? "(Chưa có)" : info.tenPhongBan);
        lblTrangThaiCNCN.setText(formatTrangThaiNV(info.trangThai));
        lblTrangThaiCNCN.setForeground("dang_lam_viec".equals(info.trangThai) ? UIColors.SUCCESS_GREEN : UIColors.DANGER_RED);
        infoPanelCN.setVisible(true);
        chamCongStatusPanelCN.setVisible(true);
        if (histPanelCN != null) histPanelCN.setVisible(true);
        refreshCaNhan(chkOT);
        loadLichSuCaNhan();
        panel.revalidate();
        panel.repaint();
    }

    private void searchCaNhan(JPanel panel, JCheckBox chkOT) {
        String ma = txtMaNVCN.getText().trim();
        if (ma.isEmpty()) {
            DialogUtil.showWarn(this, "Vui lòng nhập mã nhân viên.");
            return;
        }
        var info = svc.findNhanVienByMa(ma);
        if (info == null) {
            infoPanelCN.setVisible(false);
            chamCongStatusPanelCN.setVisible(false);
            if (histPanelCN != null) histPanelCN.setVisible(false);
            maNVTimKiem = null;
            DialogUtil.showError(this, "Không tìm thấy nhân viên: " + ma);
            return;
        }
        maNVTimKiem = info.maNV;
        lblHoTenCNCN.setText(info.hoTen);
        lblEmailCNCN.setText(info.email.isEmpty() ? "(Chưa có)" : info.email);
        lblChucVuCNCN.setText(info.tenChucVu.isEmpty() ? "(Chưa có)" : info.tenChucVu);
        lblPhongBanCNCN.setText(info.tenPhongBan.isEmpty() ? "(Chưa có)" : info.tenPhongBan);
        lblTrangThaiCNCN.setText(formatTrangThaiNV(info.trangThai));
        lblTrangThaiCNCN.setForeground("dang_lam_viec".equals(info.trangThai) ? UIColors.SUCCESS_GREEN : UIColors.DANGER_RED);
        infoPanelCN.setVisible(true);
        chamCongStatusPanelCN.setVisible(true);
        if (histPanelCN != null) histPanelCN.setVisible(true);
        refreshCaNhan(chkOT);
        loadLichSuCaNhan();
        panel.revalidate();
        panel.repaint();
    }

    private boolean isViewingOwnAttendance() {
        return maNVCaNhan != null && maNVCaNhan.equals(maNVTimKiem);
    }

    private void loadLichSuCaNhan() {
        if (maNVTimKiem == null || modelLichSuCaNhan == null) return;
        modelLichSuCaNhan.setRowCount(0);
        int th = Integer.parseInt((String) cboThangCN.getSelectedItem());
        int nm = Integer.parseInt((String) cboNamCN.getSelectedItem());
        LocalDate tu  = LocalDate.of(nm, th, 1);
        LocalDate den = tu.withDayOfMonth(tu.lengthOfMonth());
        List<ChamCong> ds = svc.getLichSuChamCong(maNVTimKiem, tu, den);
        DateTimeFormatter fN = DateTimeFormatter.ofPattern("dd/MM");
        DateTimeFormatter fG = DateTimeFormatter.ofPattern("HH:mm");
        for (ChamCong cc : ds) {
            modelLichSuCaNhan.addRow(new Object[]{
                cc.getNgay().format(fN),
                cc.getTenCaLam() != null ? cc.getTenCaLam() : cc.getMaCaLam(),
                cc.getGioVao() != null ? cc.getGioVao().format(fG) : "-",
                cc.getGioRa()  != null ? cc.getGioRa().format(fG)  : "-",
                String.format("%.1f", cc.getSoGioLam()),
                cc.isLaOT() ? "OT" : "-",
                cc.getTrangThai() != null ? cc.getTrangThai().getDisplayName() : "N/A"
            });
        }
        if (statsPanelCN != null) {
            long dg = ds.stream().filter(c -> c.getTrangThai() == ChamCong.TrangThai.DUNG_GIO).count();
            long dm = ds.stream().filter(c -> c.getTrangThai() == ChamCong.TrangThai.DI_MUON).count();
            long vm = ds.stream().filter(c -> c.getTrangThai() == ChamCong.TrangThai.VANG_MAT).count();
            long ot = ds.stream().filter(ChamCong::isLaOT).count();
            statsPanelCN.removeAll();
            statsPanelCN.add(lbl("Tổng ngày công:", String.valueOf(ds.size()), UIColors.PRIMARY_PURPLE));
            statsPanelCN.add(lbl("Đúng giờ:", String.valueOf(dg), UIColors.SUCCESS_GREEN));
            statsPanelCN.add(lbl("Đi muộn:", String.valueOf(dm), UIColors.DANGER_RED));
            statsPanelCN.add(lbl("Vắng:", String.valueOf(vm), Color.GRAY));
            statsPanelCN.add(lbl("Ca OT:", String.valueOf(ot), UIColors.PRIMARY_PURPLE));
            statsPanelCN.revalidate(); statsPanelCN.repaint();
        }
    }

    // ===============================================
    //  TAB -- TONG HOP READ-ONLY (HR/Manager)
    // ===============================================
    private JPanel tabTongHopReadOnly() {
        JPanel p = panel();
        JPanel tb = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5)); tb.setOpaque(false);
        tb.add(new JLabel("Tháng:")); cboThang = combMonth(); tb.add(cboThang);
        tb.add(new JLabel("Năm:")); cboNam = combYear(); tb.add(cboNam);
        JButton bL = btn("Lọc dữ liệu", UIColors.PRIMARY_PURPLE); bL.addActionListener(e -> loadCC()); tb.add(bL);
        p.add(tb, BorderLayout.NORTH);
        String[] cols = {"Mã NV", "Họ tên", "Ngày", "Ca làm", "Giờ vào", "Giờ ra", "Số giờ", "OT", "Trạng thái"};
        modelCC = mdl(cols); tableChamCong = tbl(modelCC);
        tableChamCong.getColumnModel().getColumn(8).setCellRenderer(new com.hrm.gui.components.StatusCellRenderer());
        for (int i : new int[]{2, 3, 4, 5, 6, 7}) tableChamCong.getColumnModel().getColumn(i).setCellRenderer(CENTER_R);
        p.add(new JScrollPane(tableChamCong), BorderLayout.CENTER);
        statsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 30, 10));
        statsPanel.setOpaque(false); statsPanel.setBorder(new EmptyBorder(5, 0, 0, 0));
        p.add(statsPanel, BorderLayout.SOUTH);
        loadCC(); return p;
    }

    // ===============================================
    //  TAB -- DANG KY OT (Employee/HR/Manager)
    // ===============================================
    private JPanel tabDangKyOT() {
        JPanel p = panel();
        JLabel title = new JLabel("Đăng ký làm thêm giờ (OT)");
        title.setFont(com.hrm.util.UIFonts.HEADER_SUB); title.setForeground(UIColors.TEXT_DARK);
        title.setVisible(false);
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(com.hrm.util.UIColors.LIGHT_PURPLE);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIColors.PRIMARY_PURPLE, 1),
            new EmptyBorder(12, 15, 12, 15)));
        GridBagConstraints g = gbc();
        // Row 0: Ngay
        g.gridx=0; g.gridy=0; g.weightx=0; formPanel.add(new JLabel("Ngày (dd/MM/yyyy):"),g);
        JTextField txtNgayOT = new JTextField(12);
        txtNgayOT.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        g.gridx=1; g.weightx=1; formPanel.add(txtNgayOT,g);
        // Row 1: Gio bat dau OT
        g.gridx=0; g.gridy=1; g.weightx=0; formPanel.add(new JLabel("Giờ bắt đầu OT (HH:mm):"),g);
        JTextField txtGioVaoOT = new JTextField("17:00", 10);
        g.gridx=1; g.weightx=1; formPanel.add(txtGioVaoOT,g);
        // Row 2: Gio ket thuc OT
        g.gridx=0; g.gridy=2; g.weightx=0; formPanel.add(new JLabel("Giờ kết thúc OT (HH:mm):"),g);
        JTextField txtGioRaOT = new JTextField("20:00", 10);
        g.gridx=1; g.weightx=1; formPanel.add(txtGioRaOT,g);
        // Row 3: So gio tu tinh
        g.gridx=0; g.gridy=3; g.weightx=0; formPanel.add(new JLabel("Số giờ OT (tự tính):"),g);
        JLabel lblSoGioTinhToan = new JLabel("3.0 gio");
        lblSoGioTinhToan.setFont(com.hrm.util.UIFonts.BOLD_NORMAL);
        lblSoGioTinhToan.setForeground(UIColors.PRIMARY_PURPLE);
        g.gridx=1; g.weightx=1; formPanel.add(lblSoGioTinhToan,g);
        java.awt.event.FocusAdapter recalcFocus = new java.awt.event.FocusAdapter() {
            @Override public void focusLost(java.awt.event.FocusEvent e) {
                try {
                    java.time.LocalTime vao = java.time.LocalTime.parse(txtGioVaoOT.getText().trim(), DateTimeFormatter.ofPattern("HH:mm"));
                    java.time.LocalTime ra  = java.time.LocalTime.parse(txtGioRaOT.getText().trim(),  DateTimeFormatter.ofPattern("HH:mm"));
                    double so = DangKyLamThem.tinhSoGioOT(vao, ra);
                    lblSoGioTinhToan.setText(String.format("%.1f gio", so));
                    lblSoGioTinhToan.setForeground(so<=0||so>8 ? UIColors.DANGER_RED : UIColors.PRIMARY_PURPLE);
                } catch (Exception ex) {
                    lblSoGioTinhToan.setText("? (định dạng sai)"); lblSoGioTinhToan.setForeground(UIColors.DANGER_RED);
                }
            }
        };
        txtGioVaoOT.addFocusListener(recalcFocus); txtGioRaOT.addFocusListener(recalcFocus);
        // Row 4: Ly do
        g.gridx=0; g.gridy=4; g.weightx=0; formPanel.add(new JLabel("Lý do:"),g);
        JTextField txtLyDo = new JTextField(30);
        g.gridx=1; g.weightx=1; formPanel.add(txtLyDo,g);
        // Row 5: Nut gui
        JButton btnTaoDon = btn("Gửi đơn OT", UIColors.PRIMARY_PURPLE);
        g.gridx=0; g.gridy=5; g.gridwidth=2; g.insets=new Insets(12,8,8,8); formPanel.add(btnTaoDon,g);
        g.gridwidth=1;
        JPanel topOT = new JPanel(new BorderLayout(0, 10)); topOT.setOpaque(false);
        topOT.add(title, BorderLayout.NORTH); topOT.add(formPanel, BorderLayout.CENTER);
        // Bang don OT
        String[] cols = {"Mã đơn", "Ngày", "Giờ OT", "Số giờ", "Lý do", "Trạng thái"};
        modelDonOTCaNhan = mdl(cols); tableDonOTCaNhan = tbl(modelDonOTCaNhan);
        tableDonOTCaNhan.getColumnModel().getColumn(5).setCellRenderer(new com.hrm.gui.components.StatusCellRenderer());
        for (int i : new int[]{1, 2, 3}) tableDonOTCaNhan.getColumnModel().getColumn(i).setCellRenderer(CENTER_R);
        JButton btnHuyDon = btn("Hủy đơn", UIColors.DANGER_RED);
        JButton btnLamMoi = new JButton("Làm mới"); btnLamMoi.setFocusPainted(false);
        JPanel tblHeader = new JPanel(new BorderLayout()); tblHeader.setOpaque(false);
        JLabel tblTitle = new JLabel("Đơn OT của bạn"); tblTitle.setFont(UIFonts.BOLD_NORMAL);
        tblHeader.add(tblTitle, BorderLayout.WEST);
        JPanel btnPnl = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0)); btnPnl.setOpaque(false);
        btnPnl.add(btnHuyDon); btnPnl.add(btnLamMoi); tblHeader.add(btnPnl, BorderLayout.EAST);
        JPanel tablePanel = new JPanel(new BorderLayout(0, 5)); tablePanel.setOpaque(false);
        tablePanel.add(tblHeader, BorderLayout.NORTH); tablePanel.add(new JScrollPane(tableDonOTCaNhan), BorderLayout.CENTER);
        loadDonOTCaNhan();
        btnTaoDon.addActionListener(e -> {
            try {
                LocalDate ngay = LocalDate.parse(txtNgayOT.getText().trim(), DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                java.time.LocalTime gioVao = java.time.LocalTime.parse(txtGioVaoOT.getText().trim(), DateTimeFormatter.ofPattern("HH:mm"));
                java.time.LocalTime gioRa  = java.time.LocalTime.parse(txtGioRaOT.getText().trim(),  DateTimeFormatter.ofPattern("HH:mm"));
                String lyDo = txtLyDo.getText().trim();
                if (lyDo.isEmpty()) { DialogUtil.showWarn(this, "Vui lòng nhập lý do."); return; }
                KetQua<DangKyLamThem> res = svc.taoDonLamThem(maNVCaNhan, ngay, gioVao, gioRa, lyDo);
                if (res.isSuccess()) { DialogUtil.showSuccess(this, res.getMessage()); txtLyDo.setText(""); loadDonOTCaNhan(); }
                else DialogUtil.showError(this, res.getMessage());
            } catch (Exception ex) {
                DialogUtil.showError(this, "Kiểm tra lại: Ngày (dd/MM/yyyy), Giờ (HH:mm).", "Lỗi định dạng");
            }
        });
        btnHuyDon.addActionListener(e -> {
            int row = tableDonOTCaNhan.getSelectedRow();
            if (row < 0) { DialogUtil.showInfo(this, "Chọn đơn cần hủy."); return; }
            String tt = (String) modelDonOTCaNhan.getValueAt(row, 5);
            if (!DangKyLamThem.TrangThai.CHO_DUYET.getDisplayName().equals(tt)) {
                DialogUtil.showInfo(this, "Chỉ hủy đơn chờ duyệt.");
                return;
            }
            int maDK = (int) modelDonOTCaNhan.getValueAt(row, 0);
            KetQua<Void> res = svc.xoaDonLamThem(maDK, maNVCaNhan);
            if (res.isSuccess()) DialogUtil.showSuccess(this, res.getMessage()); else DialogUtil.showError(this, res.getMessage()); loadDonOTCaNhan();
        });
        btnLamMoi.addActionListener(e -> loadDonOTCaNhan());
        p.add(topOT, BorderLayout.NORTH);
        p.add(tablePanel, BorderLayout.CENTER);
        return p;
    }

    private void loadDonOTCaNhan() {
        if (maNVCaNhan == null || modelDonOTCaNhan == null) return;
        modelDonOTCaNhan.setRowCount(0);
        DateTimeFormatter fDate = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter fTime = DateTimeFormatter.ofPattern("HH:mm");
        for (DangKyLamThem don : svc.getDonLamThemCuaNV(maNVCaNhan)) {
            String gioOT = (don.getGioVaoOT() != null && don.getGioRaOT() != null)
                ? don.getGioVaoOT().format(fTime) + " -> " + don.getGioRaOT().format(fTime)
                : String.format("%.1f gio", don.getSoGio());
            modelDonOTCaNhan.addRow(new Object[]{
                don.getMaDK(),
                don.getNgay() != null ? don.getNgay().format(fDate) : "-",
                gioOT,
                String.format("%.1f", don.getSoGio()),
                don.getLyDo(),
                don.getTrangThai().getDisplayName()
            });
        }
    }

    // ===============================================
    //  HELPERS
    // ===============================================
    private void initTongHopFilters(JPanel toolbar) {
        txtTimKiemCC = UIHelper.createSearchField("Tìm theo mã NV, họ tên...");
        txtTimKiemCC.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override public void keyReleased(java.awt.event.KeyEvent e) { applyCCRowFilter(); }
        });
        toolbar.add(new JLabel("Tìm kiếm:"));
        toolbar.add(txtTimKiemCC);
        toolbar.add(new JLabel("Mã NV:"));
        cboMaNVFilter = new JComboBox<>();
        cboMaNVFilter.addActionListener(e -> applyCCRowFilter());
        toolbar.add(cboMaNVFilter);
        toolbar.add(new JLabel("Trạng thái:"));
        cboTrangThaiFilter = new JComboBox<>();
        cboTrangThaiFilter.addActionListener(e -> applyCCRowFilter());
        toolbar.add(cboTrangThaiFilter);
        setTongHopFilterItems(cboMaNVFilter, List.of("Tất cả"), "Tất cả");
        setTongHopFilterItems(cboTrangThaiFilter, List.of("Tất cả"), "Tất cả");
    }

    private void refreshTongHopFilterOptionsFromTable() {
        if (modelCC == null || cboMaNVFilter == null || cboTrangThaiFilter == null) return;
        String selectedMaNV = (String) cboMaNVFilter.getSelectedItem();
        String selectedTrangThai = (String) cboTrangThaiFilter.getSelectedItem();
        LinkedHashSet<String> maNVItems = new LinkedHashSet<>();
        LinkedHashSet<String> trangThaiItems = new LinkedHashSet<>();
        maNVItems.add("Tất cả");
        trangThaiItems.add("Tất cả");
        for (int i = 0; i < modelCC.getRowCount(); i++) {
            Object maNV = modelCC.getValueAt(i, 0);
            Object trangThai = modelCC.getValueAt(i, 8);
            if (maNV != null && !maNV.toString().isBlank()) maNVItems.add(maNV.toString());
            if (trangThai != null && !trangThai.toString().isBlank()) trangThaiItems.add(trangThai.toString());
        }
        setTongHopFilterItems(cboMaNVFilter, new ArrayList<>(maNVItems), selectedMaNV);
        setTongHopFilterItems(cboTrangThaiFilter, new ArrayList<>(trangThaiItems), selectedTrangThai);
    }

    private void setTongHopFilterItems(JComboBox<String> combo, List<String> items, String selectedValue) {
        combo.removeAllItems();
        for (String item : items) combo.addItem(item);
        if (selectedValue != null && items.contains(selectedValue)) combo.setSelectedItem(selectedValue);
        else if (!items.isEmpty()) combo.setSelectedIndex(0);
    }

    private void applyCCRowFilter() {
        if (tableChamCong == null || modelCC == null) return;
        TableRowSorter<DefaultTableModel> sorter;
        if (tableChamCong.getRowSorter() instanceof TableRowSorter<?> existingSorter) {
            @SuppressWarnings("unchecked")
            TableRowSorter<DefaultTableModel> casted = (TableRowSorter<DefaultTableModel>) existingSorter;
            sorter = casted;
        } else {
            sorter = new TableRowSorter<>(modelCC);
            tableChamCong.setRowSorter(sorter);
        }
        final String keyword = UIHelper.normalizeSearch(txtTimKiemCC != null ? txtTimKiemCC.getText() : "");
        String maNV = cboMaNVFilter != null ? (String) cboMaNVFilter.getSelectedItem() : null;
        String trangThai = cboTrangThaiFilter != null ? (String) cboTrangThaiFilter.getSelectedItem() : null;
        List<RowFilter<Object, Object>> filters = new ArrayList<>();
        if (!keyword.isEmpty()) {
            filters.add(new RowFilter<>() {
                @Override public boolean include(Entry<? extends Object, ? extends Object> entry) {
                    for (int col : new int[]{0, 1}) {
                        Object val = entry.getValue(col);
                        if (val != null && UIHelper.normalizeSearch(val.toString()).contains(keyword)) return true;
                    }
                    return false;
                }
            });
        }
        if (maNV != null && !"Tất cả".equals(maNV)) {
            filters.add(RowFilter.regexFilter("^" + java.util.regex.Pattern.quote(maNV) + "$", 0));
        }
        if (trangThai != null && !"Tất cả".equals(trangThai)) {
            filters.add(RowFilter.regexFilter("^" + java.util.regex.Pattern.quote(trangThai) + "$", 8));
        }
        sorter.setRowFilter(filters.isEmpty() ? null : RowFilter.andFilter(filters));
        refreshTongHopStatsFromVisibleRows();
    }

    private void refreshTongHopStatsFromVisibleRows() {
        if (statsPanel == null || tableChamCong == null) return;
        int tong = tableChamCong.getRowCount();
        int dungGio = 0;
        int diMuon = 0;
        int vang = 0;
        for (int viewRow = 0; viewRow < tong; viewRow++) {
            Object value = tableChamCong.getValueAt(viewRow, 8);
            String trangThai = value != null ? value.toString() : "";
            if (com.hrm.model.ChamCong.TrangThai.DUNG_GIO.getDisplayName().equals(trangThai)) dungGio++;
            else if (com.hrm.model.ChamCong.TrangThai.DI_MUON.getDisplayName().equals(trangThai)) diMuon++;
            else if (com.hrm.model.ChamCong.TrangThai.VANG_MAT.getDisplayName().equals(trangThai)) vang++;
        }
        statsPanel.removeAll();
        statsPanel.add(lbl("Tổng:", String.valueOf(tong), UIColors.PRIMARY_PURPLE));
        statsPanel.add(lbl("Đúng giờ:", String.valueOf(dungGio), UIColors.SUCCESS_GREEN));
        statsPanel.add(lbl("Đi muộn:", String.valueOf(diMuon), UIColors.DANGER_RED));
        statsPanel.add(lbl("Vắng:", String.valueOf(vang), Color.GRAY));
        statsPanel.revalidate();
        statsPanel.repaint();
    }

    private String fmtTien(double a) { return moneyFmt.format(Math.round(a)) + " d"; }

    private JPanel panel() {
        JPanel p = new JPanel(new BorderLayout(0, 10)); p.setBackground(Color.WHITE);
        p.setBorder(new EmptyBorder(15, 15, 15, 15)); return p;
    }

    private JButton btn(String text, Color bg) {
        if (UIColors.SUCCESS_GREEN.equals(bg)) return UIHelper.createSuccessButton(text);
        if (UIColors.DANGER_RED.equals(bg))   return UIHelper.createDangerButton(text);
        if (UIColors.PRIMARY_PURPLE.equals(bg)) return UIHelper.createPrimaryButton(text);
        return UIHelper.createDefaultButton(text);
    }

    private PurpleTable tbl(DefaultTableModel m) {
        return new PurpleTable(m);
    }

    private DefaultTableModel mdl(String[] cols) {
        return PurpleTable.createNonEditableModel(cols);
    }

    private JLabel lbl(String label, String val, Color color) {
        JLabel l = new JLabel(label + " " + val); l.setFont(com.hrm.util.UIFonts.BOLD_NORMAL);
        l.setForeground(color); return l;
    }

    private GridBagConstraints gbc() {
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 8, 8, 8); g.anchor = GridBagConstraints.WEST;
        g.fill = GridBagConstraints.HORIZONTAL; return g;
    }

    private JComboBox<String> combMonth() {
        JComboBox<String> c = new JComboBox<>();
        for (int i = 1; i <= 12; i++) c.addItem(String.valueOf(i));
        c.setSelectedIndex(LocalDate.now().getMonthValue() - 1); return c;
    }

    private JComboBox<String> combYear() {
        JComboBox<String> c = new JComboBox<>(); int y = LocalDate.now().getYear();
        for (int yr = y - 2; yr <= y; yr++) c.addItem(String.valueOf(yr));
        c.setSelectedIndex(2); return c;
    }

    private static final DefaultTableCellRenderer CENTER_R = new DefaultTableCellRenderer() {
        { setHorizontalAlignment(CENTER); }
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            super.getTableCellRendererComponent(t, v, s, f, r, c); setForeground(com.hrm.util.UIColors.TEXT_DARK); return this;
        }
    };
    private static final DefaultTableCellRenderer RIGHT_R = new DefaultTableCellRenderer() {
        { setHorizontalAlignment(RIGHT); }
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            super.getTableCellRendererComponent(t, v, s, f, r, c); setForeground(com.hrm.util.UIColors.TEXT_DARK); return this;
        }
    };

}
