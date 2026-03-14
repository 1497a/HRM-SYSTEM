package com.hrm.gui.employee;

import com.hrm.model.BoNhiem;
import com.hrm.model.HopDongLaoDong;
import com.hrm.model.NhanVien;
import com.hrm.model.ThongTinCaNhan;
import com.hrm.bus.BoNhiemBUS;
import com.hrm.bus.HopDongBUS;
import com.hrm.bus.KetQua;
import com.hrm.bus.NhanVienBUS;
import com.hrm.util.SessionContext;
import com.hrm.util.UIColors;

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

    private boolean dataChanged = false;
    private boolean personalEditMode = false;
    private boolean statusEditMode   = false;

    private JButton btnSuaThongTin;
    private JButton btnDoiTrangThai;
    private TabThongTinCaNhanPanel personalTab;

    public boolean isDataChanged() { return dataChanged; }

    public EmployeeDetailPanel(Frame parent, String maNV) {
        super(parent, "Ho so nhan vien", true);
        this.maNV = maNV;
        loadData();
        buildUI();
        setSize(750, 600);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(true);
    }

    private void loadData() {
        nhanVien       = nvService.getById(maNV);
        ttcn           = nvService.getThongTinCaNhan(maNV);
        boNhiemHienTai = boNhiemService.getBoNhiemChinhHieuLuc(maNV);
        hopDong        = hopDongService.getHieuLuc(maNV);
    }

    private void buildUI() {
        String title = "Ho so nhan vien";
        if (nhanVien != null && nhanVien.getHoTen() != null && !nhanVien.getHoTen().isEmpty())
            title = "Ho so: " + nhanVien.getHoTen();
        setTitle(title);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UIColors.WHITE);
        root.add(buildHeader(), BorderLayout.NORTH);

        personalTab = new TabThongTinCaNhanPanel(maNV, nhanVien, ttcn, hopDong);

        JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP);
        tabs.setFont(com.hrm.util.UIFonts.BOLD_NORMAL);
        tabs.setBackground(UIColors.LIGHT_GRAY_BG);
        tabs.addTab("Thong tin ca nhan", personalTab);
        tabs.addTab("Bo nhiem hien tai", new TabBoNhiemPanel(boNhiemHienTai, boNhiemService, maNV));
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

        String name = "(Khong ro ten)";
        String code = "#" + maNV;
        if (ttcn != null && ttcn.getHoTen() != null)       name = ttcn.getHoTen();
        else if (nhanVien != null && nhanVien.getHoTen() != null) name = nhanVien.getHoTen();
        if (nhanVien != null && nhanVien.getMaNhanVien() != null)  code = nhanVien.getMaNhanVien();

        JLabel lblName = new JLabel(name);
        lblName.setFont(com.hrm.util.UIFonts.HEADER_H3);
        lblName.setForeground(UIColors.WHITE);

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
            JLabel badge = createStatusBadge(nhanVien.getTrangThai(), nhanVien.getTrangThaiDisplay());
            JPanel wrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            wrap.setOpaque(false);
            wrap.add(badge);
            header.add(wrap, BorderLayout.EAST);
        }
        return header;
    }

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(UIColors.LIGHT_GRAY_BG);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIColors.BORDER_GRAY));

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 10));
        right.setOpaque(false);

        btnSuaThongTin = new JButton("Sua thong tin");
        styleButton(btnSuaThongTin, UIColors.PRIMARY_PURPLE);
        btnSuaThongTin.setPreferredSize(new Dimension(130, 34));
        btnSuaThongTin.addActionListener(e -> onSuaThongTin());

        btnDoiTrangThai = new JButton("Doi trang thai");
        styleButton(btnDoiTrangThai, UIColors.WARNING_TEXT_AMBER);
        btnDoiTrangThai.setPreferredSize(new Dimension(145, 34));
        btnDoiTrangThai.addActionListener(e -> onDoiTrangThai());

        JButton btnClose = new JButton("Dong");
        btnClose.setFont(com.hrm.util.UIFonts.BOLD_NORMAL);
        btnClose.setFocusPainted(false);
        btnClose.setPreferredSize(new Dimension(90, 34));
        btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClose.addActionListener(e -> dispose());

        SessionContext sc = SessionContext.getInstance();
        boolean canUpdate = sc.coVaiTro("ADMIN") || sc.coQuyen("EMPLOYEE_UPDATE");
        String myMaNV = sc.getCurrentUser() != null ? sc.getCurrentUser().getNhanVienId() : null;
        boolean isSelf = maNV != null && maNV.equals(myMaNV);

        btnSuaThongTin.setVisible(canUpdate || isSelf);
        btnDoiTrangThai.setVisible(canUpdate && !isSelf);

        right.add(btnSuaThongTin);
        right.add(btnDoiTrangThai);
        right.add(btnClose);
        footer.add(right, BorderLayout.EAST);
        return footer;
    }

    private void onSuaThongTin() {
        if (statusEditMode) {
            JOptionPane.showMessageDialog(this, "Dang o che do doi trang thai. Vui long luu truoc.",
                    "Thong bao", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!personalEditMode) {
            personalEditMode = true;
            personalTab.setEditMode(true);
            btnSuaThongTin.setText("Luu");
            return;
        }
        if (!personalTab.hasChanges()) {
            personalEditMode = false;
            personalTab.setEditMode(false);
            btnSuaThongTin.setText("Sua thong tin");
            return;
        }
        KetQua<ThongTinCaNhan> result = personalTab.save();
        if (!result.isSuccess()) {
            JOptionPane.showMessageDialog(this, result.getMessage(), "Loi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        dataChanged = true;
        personalEditMode = false;
        btnSuaThongTin.setText("Sua thong tin");
        loadData();
        buildUI();
        revalidate();
        repaint();
    }

    private void onDoiTrangThai() {
        if (nhanVien == null) return;
        if (personalEditMode) {
            JOptionPane.showMessageDialog(this, "Dang o che do sua thong tin. Vui long luu truoc.",
                    "Thong bao", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!statusEditMode) {
            statusEditMode = true;
            personalTab.setStatusEditMode(true);
            btnDoiTrangThai.setText("Luu trang thai");
            revalidate();
            repaint();
            return;
        }
        String trangThaiMoi = personalTab.getCurrentTrangThai();
        if (trangThaiMoi != null && trangThaiMoi.equals(nhanVien.getTrangThai())) {
            statusEditMode = false;
            personalTab.setStatusEditMode(false);
            btnDoiTrangThai.setText("Doi trang thai");
            return;
        }
        KetQua<NhanVien> result = nvService.capNhatTrangThai(nhanVien.getMaNhanVien(), trangThaiMoi, "");
        if (!result.isSuccess()) {
            JOptionPane.showMessageDialog(this, result.getMessage(), "Loi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        dataChanged = true;
        statusEditMode = false;
        personalTab.setStatusEditMode(false);
        btnDoiTrangThai.setText("Doi trang thai");
        loadData();
        buildUI();
        revalidate();
        repaint();
    }

    private void updateButtons(JTabbedPane tabs) {
        if (btnSuaThongTin == null || btnDoiTrangThai == null) return;
        boolean isPersonalTab = tabs.getSelectedIndex() == 0;
        SessionContext sc = SessionContext.getInstance();
        boolean canUpdate = sc.coVaiTro("ADMIN") || sc.coQuyen("EMPLOYEE_UPDATE");
        String myMaNV = sc.getCurrentUser() != null ? sc.getCurrentUser().getNhanVienId() : null;
        boolean isSelf = maNV != null && maNV.equals(myMaNV);
        btnSuaThongTin.setVisible(isPersonalTab && (canUpdate || isSelf));
        btnDoiTrangThai.setVisible(isPersonalTab && canUpdate && !isSelf);
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
        badge.setForeground(UIColors.WHITE); badge.setBackground(bg);
        badge.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        return badge;
    }

    private void styleButton(JButton btn, Color bg) {
        btn.setFont(com.hrm.util.UIFonts.BOLD_NORMAL);
        btn.setBackground(bg);
        btn.setForeground(UIColors.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
}
