package com.hrm.gui.notification;

import com.hrm.bus.ChucVuBUS;
import com.hrm.bus.KetQua;
import com.hrm.bus.NhanVienBUS;
import com.hrm.bus.PhongBanBUS;
import com.hrm.bus.ThongBaoBUS;
import com.hrm.model.ChucVu;
import com.hrm.model.DataScope;
import com.hrm.model.NhanVien;
import com.hrm.model.PhongBan;
import com.hrm.model.TaiKhoan;
import com.hrm.util.DialogUtil;
import com.hrm.util.HRMConstants;
import com.hrm.util.PermissionCodes;
import com.hrm.util.UIFonts;
import com.hrm.util.SessionContext;
import com.hrm.util.UIColors;
import com.hrm.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Tab 2 - Gửi thông báo (chỉ ADMIN/HR và các role có quyền NOTIFICATION_SEND).
 */
class TabSendNotificationPanel extends JPanel {

    private static final String RECIPIENT_ALL_COMPANY = "Toàn công ty";
    private static final String RECIPIENT_SCOPE = "Trong phạm vi của tôi";
    private static final String RECIPIENT_EMPLOYEE = "Nhân viên cụ thể";
    private static final String RECIPIENT_DEPARTMENT = "Phòng ban";
    private static final String RECIPIENT_POSITION = "Chức vụ";
    private static final String RECIPIENT_SELF = "Chính tôi";
    private final ThongBaoBUS service;
    private final TaiKhoan currentUser;
    private final DataScope sendScope;
    private JTextField txtTieuDe;
    private JTextArea txtNoiDung;
    private JComboBox<String> cboLoai;
    private JComboBox<String> cboRecipientType;
    private JPanel recipientDetailPanel;
    private JComboBox<NhanVien> cboNhanVien;
    private JComboBox<PhongBan> cboPhongBan;
    private JComboBox<ChucVu> cboChucVu;
    TabSendNotificationPanel(ThongBaoBUS service, TaiKhoan currentUser, DataScope sendScope) {
        this.service = service;
        this.currentUser = currentUser;
        this.sendScope = sendScope;
        setLayout(new BorderLayout(12, 12));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(20, 20, 20, 20));
        if (!SessionContext.getInstance().hasPermission(PermissionCodes.NOTIFICATION_SEND)) {
            add(buildNoPermissionPanel(), BorderLayout.CENTER);
            return;
        }
        add(buildForm(), BorderLayout.NORTH);
    }

    private JPanel buildNoPermissionPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                new EmptyBorder(24, 24, 24, 24)));
        JLabel label = new JLabel("Bạn không có quyền gửi thông báo.");
        label.setFont(UIFonts.TEXT_NORMAL);
        label.setForeground(UIColors.TEXT_DARK);
        panel.add(label);
        return panel;
    }

    private JPanel buildForm() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                new EmptyBorder(20, 20, 20, 20)));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        JLabel lblTitle = new JLabel("Gửi thông báo");
        lblTitle.setFont(com.hrm.util.UIFonts.HEADER_SUB);
        lblTitle.setForeground(UIColors.PRIMARY_PURPLE);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        form.add(lblTitle, gbc);
        JLabel lblScope = new JLabel("Phạm vi gửi: " + getScopeDisplayName());
        lblScope.setFont(com.hrm.util.UIFonts.TEXT_NORMAL);
        lblScope.setForeground(UIColors.TEXT_DARK);
        gbc.gridy = 1;
        form.add(lblScope, gbc);
        gbc.gridwidth = 1;
        gbc.gridy = 2;
        gbc.gridx = 0;
        form.add(new JLabel("Tiêu đề:"), gbc);
        txtTieuDe = new JTextField(35);
        txtTieuDe.setFont(UIFonts.TEXT_NORMAL);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        form.add(txtTieuDe, gbc);
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridy = 3;
        gbc.gridx = 0;
        form.add(new JLabel("Nội dung:"), gbc);
        txtNoiDung = new JTextArea(5, 35);
        txtNoiDung.setFont(UIFonts.TEXT_NORMAL);
        txtNoiDung.setLineWrap(true);
        txtNoiDung.setWrapStyleWord(true);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        form.add(new JScrollPane(txtNoiDung), gbc);
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridy = 4;
        gbc.gridx = 0;
        form.add(new JLabel("Loại thông báo:"), gbc);
        cboLoai = new JComboBox<>(new String[]{"Hệ thống", "Đơn từ", "Thông báo chung"});
        cboLoai.setFont(UIFonts.TEXT_NORMAL);
        cboLoai.setPreferredSize(new Dimension(250, 32));
        gbc.gridx = 1;
        form.add(cboLoai, gbc);
        gbc.gridy = 5;
        gbc.gridx = 0;
        form.add(new JLabel("Gửi đến:"), gbc);
        cboRecipientType = new JComboBox<>();
        cboRecipientType.setFont(UIFonts.TEXT_NORMAL);
        cboRecipientType.setPreferredSize(new Dimension(220, 32));
        gbc.gridx = 1;
        form.add(cboRecipientType, gbc);
        recipientDetailPanel = new JPanel(new CardLayout());
        recipientDetailPanel.setOpaque(false);
        recipientDetailPanel.add(new JPanel(), RECIPIENT_ALL_COMPANY);
        recipientDetailPanel.add(new JPanel(), RECIPIENT_SCOPE);
        recipientDetailPanel.add(new JPanel(), RECIPIENT_SELF);
        cboNhanVien = new JComboBox<>();
        cboNhanVien.setFont(com.hrm.util.UIFonts.TEXT_NORMAL);
        cboNhanVien.setPreferredSize(new Dimension(300, 32));
        cboNhanVien.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean sel, boolean focus) {
                super.getListCellRendererComponent(list, value, index, sel, focus);
                if (value instanceof NhanVien nv) {
                    setText("[" + nv.getMaNhanVien() + "] " + (nv.getHoTen() != null ? nv.getHoTen() : ""));
                }
                return this;
            }
        });
        JPanel cardNV = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        cardNV.setOpaque(false);
        cardNV.add(cboNhanVien);
        recipientDetailPanel.add(cardNV, RECIPIENT_EMPLOYEE);
        cboPhongBan = new JComboBox<>();
        cboPhongBan.setFont(com.hrm.util.UIFonts.TEXT_NORMAL);
        cboPhongBan.setPreferredSize(new Dimension(300, 32));
        for (PhongBan d : new PhongBanBUS().getActiveDepartments()) {
            cboPhongBan.addItem(d);
        }
        cboPhongBan.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean sel, boolean focus) {
                super.getListCellRendererComponent(list, value, index, sel, focus);
                if (value instanceof PhongBan) {
                    setText(((PhongBan) value).getTenPhongBan());
                }
                return this;
            }
        });
        JPanel cardPB = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        cardPB.setOpaque(false);
        cardPB.add(cboPhongBan);
        recipientDetailPanel.add(cardPB, RECIPIENT_DEPARTMENT);
        cboChucVu = new JComboBox<>();
        cboChucVu.setFont(com.hrm.util.UIFonts.TEXT_NORMAL);
        cboChucVu.setPreferredSize(new Dimension(300, 32));
        for (ChucVu p : new ChucVuBUS().getAllPositions()) {
            cboChucVu.addItem(p);
        }
        cboChucVu.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean sel, boolean focus) {
                super.getListCellRendererComponent(list, value, index, sel, focus);
                if (value instanceof ChucVu) {
                    setText(((ChucVu) value).getTenChucVu());
                }
                return this;
            }
        });
        JPanel cardCV = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        cardCV.setOpaque(false);
        cardCV.add(cboChucVu);
        recipientDetailPanel.add(cardCV, RECIPIENT_POSITION);
        cboRecipientType.addActionListener(e -> {
            String sel = (String) cboRecipientType.getSelectedItem();
            if (sel != null) {
                ((CardLayout) recipientDetailPanel.getLayout()).show(recipientDetailPanel, sel);
            }
        });
        gbc.gridy = 6;
        gbc.gridx = 1;
        form.add(recipientDetailPanel, gbc);
        JButton btnGui = UIHelper.createSuccessButton("Gửi thông báo");
        btnGui.addActionListener(e -> guiThongBao());
        gbc.gridy = 7;
        gbc.insets = new Insets(16, 8, 8, 8);
        form.add(btnGui, gbc);
        loadScopedNhanVienOptions();
        updateRecipientTypeOptions();
        return form;
    }

    private void updateRecipientTypeOptions() {
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        switch (sendScope) {
            case ALL -> {
                model.addElement(RECIPIENT_ALL_COMPANY);
                model.addElement(RECIPIENT_EMPLOYEE);
                model.addElement(RECIPIENT_DEPARTMENT);
                model.addElement(RECIPIENT_POSITION);
            }
            case DEPT, TEAM -> {
                model.addElement(RECIPIENT_SCOPE);
                model.addElement(RECIPIENT_EMPLOYEE);
            }
            case SELF -> model.addElement(RECIPIENT_SELF);
            default -> {
            }
        }
        cboRecipientType.setModel(model);
        if (model.getSize() > 0) {
            cboRecipientType.setSelectedIndex(0);
            ((CardLayout) recipientDetailPanel.getLayout()).show(
                    recipientDetailPanel, (String) model.getSelectedItem());
        }
    }

    private void loadScopedNhanVienOptions() {
        cboNhanVien.removeAllItems();
        if (currentUser == null || currentUser.getMaNV() == null) {
            return;
        }
        for (NhanVien nv : NhanVienBUS.getInstance()
                .getAllByActionScope(PermissionCodes.NOTIFICATION_SEND, currentUser.getMaNV())) {
            cboNhanVien.addItem(nv);
        }
    }

    private void guiThongBao() {
        String tieuDe = txtTieuDe.getText().trim();
        String noiDung = txtNoiDung.getText().trim();
        if (tieuDe.isEmpty()) {
            DialogUtil.showWarn(this, "Vui lòng nhập tiêu đề thông báo.");
            txtTieuDe.requestFocus();
            return;
        }
        if (noiDung.isEmpty()) {
            DialogUtil.showWarn(this, "Vui lòng nhập nội dung thông báo.");
            txtNoiDung.requestFocus();
            return;
        }
        if (currentUser == null) {
            return;
        }
        String recipientType = (String) cboRecipientType.getSelectedItem();
        if (recipientType == null) {
            return;
        }
        if (RECIPIENT_EMPLOYEE.equals(recipientType) && cboNhanVien.getSelectedItem() == null) {
            DialogUtil.showWarn(this, "Vui lòng chọn nhân viên để gửi.");
            cboNhanVien.requestFocus();
            return;
        }
        if (RECIPIENT_DEPARTMENT.equals(recipientType) && cboPhongBan.getSelectedItem() == null) {
            DialogUtil.showWarn(this, "Vui lòng chọn phòng ban để gửi.");
            cboPhongBan.requestFocus();
            return;
        }
        if (RECIPIENT_POSITION.equals(recipientType) && cboChucVu.getSelectedItem() == null) {
            DialogUtil.showWarn(this, "Vui lòng chọn chức vụ để gửi.");
            cboChucVu.requestFocus();
            return;
        }
        String target;
        if (RECIPIENT_ALL_COMPANY.equals(recipientType)) {
            target = "toàn công ty";
        } else if (RECIPIENT_SCOPE.equals(recipientType)) {
            target = getScopeDisplayName().toLowerCase();
        } else if (RECIPIENT_SELF.equals(recipientType)) {
            target = "chính bạn";
        } else if (RECIPIENT_EMPLOYEE.equals(recipientType)) {
            NhanVien nv = (NhanVien) cboNhanVien.getSelectedItem();
            target = nv.getMaNhanVien() + " - " + nv.getHoTen();
        } else if (RECIPIENT_DEPARTMENT.equals(recipientType)) {
            target = "phòng ban: " + ((PhongBan) cboPhongBan.getSelectedItem()).getTenPhongBan();
        } else {
            target = "chức vụ: " + ((ChucVu) cboChucVu.getSelectedItem()).getTenChucVu();
        }
        if (!DialogUtil.showYesNo(this,
                "Gửi thông báo \"" + tieuDe + "\" đến " + target + "?",
                "Xác nhận gửi")) {
            return;
        }
        String loai = getSelectedLoaiCode();
        try {
            KetQua<Void> result;
            if (RECIPIENT_ALL_COMPANY.equals(recipientType)) {
                result = service.guiThongBaoTatCa(currentUser.getId(), tieuDe, noiDung, loai);
            } else if (RECIPIENT_SCOPE.equals(recipientType) || RECIPIENT_SELF.equals(recipientType)) {
                result = service.guiThongBaoTheoDanhSachMaNV(currentUser.getId(), getScopedNhanVienIds(), tieuDe, noiDung, loai);
            } else if (RECIPIENT_EMPLOYEE.equals(recipientType)) {
                result = service.guiThongBaoCaNhan(
                        currentUser.getId(),
                        ((NhanVien) cboNhanVien.getSelectedItem()).getMaNhanVien(),
                        tieuDe,
                        noiDung,
                        loai);
            } else if (RECIPIENT_DEPARTMENT.equals(recipientType)) {
                result = service.guiThongBaoPhongBan(
                        currentUser.getId(),
                        ((PhongBan) cboPhongBan.getSelectedItem()).getId(),
                        tieuDe,
                        noiDung,
                        loai);
            } else {
                result = service.guiThongBaoChucVu(
                        currentUser.getId(),
                        ((ChucVu) cboChucVu.getSelectedItem()).getId(),
                        tieuDe,
                        noiDung,
                        loai);
            }
            if (!result.isSuccess()) {
                DialogUtil.showError(this, result.getMessage());
                return;
            }
            DialogUtil.showSuccess(this, result.getMessage());
            txtTieuDe.setText("");
            txtNoiDung.setText("");
        } catch (Exception ex) {
            DialogUtil.showError(this, "Lỗi gửi thông báo: " + ex.getMessage());
        }
    }

    private String getSelectedLoaiCode() {
        Object sel = cboLoai.getSelectedItem();
        if (HRMConstants.display(HRMConstants.LOAI_TB_HE_THONG).equals(sel)) return HRMConstants.LOAI_TB_HE_THONG;
        if (HRMConstants.display(HRMConstants.LOAI_TB_DON_TU).equals(sel)) return HRMConstants.LOAI_TB_DON_TU;
        return HRMConstants.LOAI_TB_CHUNG;
    }

    private List<String> getScopedNhanVienIds() {
        List<String> ids = new ArrayList<>();
        for (int i = 0; i < cboNhanVien.getItemCount(); i++) {
            NhanVien nv = cboNhanVien.getItemAt(i);
            if (nv != null && nv.getMaNhanVien() != null && !nv.getMaNhanVien().trim().isEmpty()) {
                ids.add(nv.getMaNhanVien());
            }
        }
        return ids;
    }

    private String getScopeDisplayName() {
        return switch (sendScope) {
            case ALL -> "Toàn công ty";
            case DEPT -> "Phòng ban của bạn";
            case TEAM -> "Nhóm của bạn";
            case SELF -> "Chỉ bản thân";
            default -> "Không có quyền gửi";
        };
    }

}
