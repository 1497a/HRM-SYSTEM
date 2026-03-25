package com.hrm.gui.employee;

import com.hrm.model.BoNhiem;
import com.hrm.model.HopDongLaoDong;
import com.hrm.model.NhanVien;
import com.hrm.model.ThongTinCaNhan;
import com.hrm.bus.BoNhiemBUS;
import com.hrm.bus.HopDongBUS;
import com.hrm.bus.KetQua;
import com.hrm.bus.NhanVienBUS;
import com.hrm.util.DialogUtil;
import com.hrm.util.HRMConstants;
import com.hrm.util.SessionContext;
import com.hrm.util.UIColors;
import com.hrm.util.UIFonts;
import com.hrm.util.UIHelper;

import javax.swing.*;
import java.awt.*;

/**
 * Dialog ho so nhan vien voi 2 tab:
 *   Tab 1 - Thong tin ca nhan (TabThongTinCaNhanPanel)
 *   Tab 2 - Bo nhiem hien tai (TabBoNhiemPanel)
 */
public class EmployeeDetailPanel extends JDialog {

    private final NhanVienBUS nvService      = NhanVienBUS.getInstance();
    private final BoNhiemBUS  boNhiemService = BoNhiemBUS.getInstance();
    private final HopDongBUS  hopDongService = HopDongBUS.getInstance();
    private final String maNV;
    private NhanVien       nhanVien;
    private ThongTinCaNhan ttcn;
    private BoNhiem        boNhiemHienTai;
    private HopDongLaoDong hopDong;
    private boolean canViewAppointmentData;
    private boolean canViewContractData;
    private boolean dataChanged = false;
    private boolean personalEditMode = false;
    private boolean statusEditMode   = false;
    private JButton btnSuaThongTin;
    private JButton btnDoiTrangThai;
    private TabThongTinCaNhanPanel personalTab;
    public boolean isDataChanged() { return dataChanged; }

    public EmployeeDetailPanel(Frame parent, String maNV) {
        super(parent, "Hồ sơ nhân viên", true);
        this.maNV = maNV;
        loadData();
        buildUI();
        setSize(750, 600);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(true);
    }

    private void loadData() {
        nhanVien       = nvService.getByMaNhanVien(maNV);
        ttcn           = nvService.getThongTinCaNhan(maNV);
        canViewAppointmentData = boNhiemService.canViewEmployeeAppointments(maNV);
        canViewContractData = hopDongService.canViewContractOf(maNV);
        boNhiemHienTai = boNhiemService.getBoNhiemChinhHieuLucInScope(maNV);
        hopDong        = hopDongService.getHieuLucInScope(maNV);
    }

    private void buildUI() {
        String title = "Hồ sơ nhân viên";
        if (nhanVien != null && nhanVien.getHoTen() != null && !nhanVien.getHoTen().isEmpty())
            title = "Hồ sơ: " + nhanVien.getHoTen();
        setTitle(title);
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.WHITE);
        root.add(buildHeader(), BorderLayout.NORTH);
        personalTab = new TabThongTinCaNhanPanel(maNV, nhanVien, ttcn, hopDong, canViewContractData);
        JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP);
        tabs.setFont(com.hrm.util.UIFonts.BOLD_NORMAL);
        tabs.setBackground(Color.WHITE);
        tabs.addTab("Thông tin cá nhân", personalTab);
        if (canViewAppointmentData) {
            tabs.addTab("Bổ nhiệm hiện tại", new TabBoNhiemPanel(boNhiemHienTai, boNhiemService, maNV));
        }
        tabs.addChangeListener(e -> updateButtons(tabs));
        root.add(tabs, BorderLayout.CENTER);
        root.add(buildFooter(), BorderLayout.SOUTH);
        updateButtons(tabs);
        setContentPane(root);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UIColors.PRIMARY_PURPLE);
        header.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));
        String name = "(Không rõ tên)";
        String code = "#" + maNV;
        if (ttcn != null && ttcn.getHoTen() != null)       name = ttcn.getHoTen();
        else if (nhanVien != null && nhanVien.getHoTen() != null) name = nhanVien.getHoTen();
        if (nhanVien != null && nhanVien.getMaNhanVien() != null)  code = nhanVien.getMaNhanVien();
        JLabel lblName = new JLabel(name);
        lblName.setFont(UIFonts.HEADER_SUB);
        lblName.setForeground(Color.WHITE);
        JLabel lblCode = new JLabel(code);
        lblCode.setFont(com.hrm.util.UIFonts.TEXT_NORMAL);
        lblCode.setForeground(new Color(220, 210, 255));
        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.add(lblName);
        textPanel.add(Box.createVerticalStrut(2));
        textPanel.add(lblCode);
        header.add(textPanel, BorderLayout.WEST);
        if (nhanVien != null) {
            JLabel badge = createStatusBadge(nhanVien.getTrangThai(), HRMConstants.display(nhanVien.getTrangThai()));
            JPanel wrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            wrap.setOpaque(false);
            wrap.add(badge);
            header.add(wrap, BorderLayout.EAST);
        }
        return header;
    }

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(Color.WHITE);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY));
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 10));
        right.setOpaque(false);
        btnSuaThongTin = UIHelper.createPrimaryButton("Sửa thông tin");
        btnSuaThongTin.setPreferredSize(new Dimension(130, 34));
        btnSuaThongTin.addActionListener(e -> onSuaThongTin());
        btnDoiTrangThai = UIHelper.createWarningButton("Đổi trạng thái");
        btnDoiTrangThai.setPreferredSize(new Dimension(145, 34));
        btnDoiTrangThai.addActionListener(e -> onDoiTrangThai());
        JButton btnClose = UIHelper.createDefaultButton("Đóng");
        btnClose.setPreferredSize(new Dimension(90, 34));
        btnClose.addActionListener(e -> dispose());
        SessionContext sc = SessionContext.getInstance();
        boolean canUpdate = nvService.canEditEmployeeProfile(maNV);
        boolean canChangeStatus = nvService.canChangeEmployeeStatus(maNV);
        String myMaNV = sc.getCurrentUser() != null ? sc.getCurrentUser().getMaNV() : null;
        boolean isSelf = maNV != null && maNV.equals(myMaNV);
        personalTab.configureStatusOptions();
        btnSuaThongTin.setVisible(canUpdate);
        btnDoiTrangThai.setVisible(canChangeStatus && !isSelf);
        right.add(btnSuaThongTin);
        right.add(btnDoiTrangThai);
        right.add(btnClose);
        footer.add(right, BorderLayout.EAST);
        return footer;
    }

    private void onSuaThongTin() {
        if (statusEditMode) {
            DialogUtil.showWarn(this, "Đang ở chế độ đổi trạng thái, vui lòng lưu trước.");
            return;
        }
        if (!personalEditMode) {
            personalEditMode = true;
            personalTab.setEditMode(true);
            btnSuaThongTin.setText("Lưu thông tin");
            return;
        }
        if (!personalTab.hasChanges()) {
            personalEditMode = false;
            personalTab.setEditMode(false);
            btnSuaThongTin.setText("Sửa thông tin");
            return;
        }
        KetQua<ThongTinCaNhan> result = personalTab.save();
        if (!result.isSuccess()) {
            DialogUtil.showError(this, result.getMessage());
            return;
        }
        DialogUtil.showSuccess(this, "Lưu thông tin thành công.");
        dataChanged = true;
        personalEditMode = false;
        btnSuaThongTin.setText("Sửa thông tin");
        loadData();
        buildUI();
        revalidate();
        repaint();
    }

    private void onDoiTrangThai() {
        if (nhanVien == null) return;
        if (personalEditMode) {
            DialogUtil.showWarn(this, "Đang ở chế độ sửa thông tin. Vui lòng lưu trước.");
            return;
        }
        if (!statusEditMode) {
            statusEditMode = true;
            personalTab.setStatusEditMode(true);
            btnDoiTrangThai.setText("Lưu trạng thái");
            revalidate();
            repaint();
            return;
        }
        String trangThaiMoi = personalTab.getCurrentTrangThai();
        if (trangThaiMoi != null && trangThaiMoi.equals(nhanVien.getTrangThai())) {
            statusEditMode = false;
            personalTab.setStatusEditMode(false);
            btnDoiTrangThai.setText("Đổi trạng thái");
            return;
        }
        KetQua<NhanVien> result = nvService.capNhatTrangThai(nhanVien.getMaNhanVien(), trangThaiMoi, "");
        if (!result.isSuccess()) {
            DialogUtil.showError(this, result.getMessage());
            return;
        }
        DialogUtil.showSuccess(this, result.getMessage());
        dataChanged = true;
        statusEditMode = false;
        personalTab.setStatusEditMode(false);
        btnDoiTrangThai.setText("Đổi trạng thái");
        loadData();
        buildUI();
        revalidate();
        repaint();
    }

    private void updateButtons(JTabbedPane tabs) {
        if (btnSuaThongTin == null || btnDoiTrangThai == null) return;
        boolean isPersonalTab = tabs.getSelectedIndex() == 0;
        SessionContext sc = SessionContext.getInstance();
        boolean canUpdate = nvService.canEditEmployeeProfile(maNV);
        boolean canChangeStatus = nvService.canChangeEmployeeStatus(maNV);
        String myMaNV = sc.getCurrentUser() != null ? sc.getCurrentUser().getMaNV() : null;
        boolean isSelf = maNV != null && maNV.equals(myMaNV);
        personalTab.configureStatusOptions();
        btnSuaThongTin.setVisible(isPersonalTab && canUpdate);
        btnDoiTrangThai.setVisible(isPersonalTab && canChangeStatus && !isSelf);
    }

    private JLabel createStatusBadge(String statusKey, String displayText) {
        Color bg = TabThongTinCaNhanPanel.resolveColor(statusKey);
        JLabel badge = new JLabel("  " + (displayText != null ? displayText : "") + "  ") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground()); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose(); super.paintComponent(g);
            }
        };
        badge.setOpaque(false); badge.setFont(com.hrm.util.UIFonts.BOLD_SMALL);
        badge.setForeground(Color.WHITE); badge.setBackground(bg);
        badge.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        return badge;
    }
}

