package com.hrm.gui.notification;

import com.hrm.model.PhongBan;
import com.hrm.model.NhanVien;
import com.hrm.model.ChucVu;
import com.hrm.model.DataScope;
import com.hrm.model.ThongBao;
import com.hrm.model.TaiKhoan;
import com.hrm.dao.ThongBaoDAO;
import com.hrm.bus.PhongBanBUS;
import com.hrm.bus.NhanVienBUS;
import com.hrm.bus.ChucVuBUS;
import com.hrm.bus.KetQua;
import com.hrm.bus.ThongBaoBUS;
import com.hrm.bus.XacThucBUS;
import com.hrm.util.SessionContext;
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
 * Panel thông báo.
 * Tab 1: Thông báo của người dùng hiện tại.
 * Tab 2: Gửi thông báo (chỉ ADMIN/HR).
 */
public class NotificationPanel extends JPanel {
    private static final String RECIPIENT_ALL_COMPANY = "Toàn công ty";
    private static final String RECIPIENT_SCOPE = "Trong phạm vi của tôi";
    private static final String RECIPIENT_EMPLOYEE = "Nhân viên cụ thể";
    private static final String RECIPIENT_DEPARTMENT = "Phòng ban";
    private static final String RECIPIENT_POSITION = "Chức vụ";
    private static final String RECIPIENT_SELF = "Chính tôi";

    private final ThongBaoBUS thongBaoService;
    private final ThongBaoDAO thongBaoRepo;
    private final TaiKhoan currentUser;
    private final DataScope sendScope;
    private final boolean canSend;

    // Tab 1
    private JTable tblThongBao;
    private DefaultTableModel modelThongBao;
    private JButton btnDanhDauDaDoc;
    private JButton btnDanhDauTatCa;
    private JButton btnLamMoi;

    // Tab 2
    private JTextField txtTieuDe;
    private JTextArea txtNoiDung;
    private JComboBox<String> cboLoai;
    private JComboBox<String> cboRecipientType;
    private JPanel recipientDetailPanel;
    private JComboBox<NhanVien> cboNhanVien;
    private JComboBox<PhongBan> cboPhongBan;
    private JComboBox<ChucVu> cboChucVu;
    private JButton btnGui;

    private List<ThongBao> danhSachThongBao;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public NotificationPanel() {
        this.thongBaoService = ThongBaoBUS.getInstance();
        this.thongBaoRepo = ThongBaoDAO.getInstance();
        this.currentUser = SessionContext.getInstance().getCurrentUser();
        this.sendScope = XacThucBUS.getInstance().getScopeForAction("NOTIFICATION_SEND");

        this.canSend = sendScope != DataScope.NONE;

        setLayout(new BorderLayout());
        setBackground(UIColors.LIGHT_GRAY_BG);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(com.hrm.util.UIFonts.TEXT_MEDIUM);
        tabbedPane.setBackground(UIColors.WHITE);

        tabbedPane.addTab("Thông báo của tôi", buildMyNotificationsTab());

        if (canSend) {
            tabbedPane.addTab("Gửi thông báo", buildSendTab());
        }

        add(tabbedPane, BorderLayout.CENTER);

        loadThongBao();
    }

    // =======================
    // Build Tab 1
    // =======================

    private JPanel buildMyNotificationsTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(UIColors.LIGHT_GRAY_BG);
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));

        // Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        toolbar.setOpaque(false);

        btnDanhDauDaDoc = UIHelper.createPrimaryButton("Đánh dấu đã đọc");
        btnDanhDauTatCa = UIHelper.createDefaultButton("Đánh dấu tất cả đã đọc");
        btnLamMoi = UIHelper.createDefaultButton("Làm mới");

        btnDanhDauDaDoc.addActionListener(e -> danhDauDaDoc());
        btnDanhDauTatCa.addActionListener(e -> danhDauTatCa());
        btnLamMoi.addActionListener(e -> loadThongBao());

        toolbar.add(btnDanhDauDaDoc);
        toolbar.add(btnDanhDauTatCa);
        toolbar.add(btnLamMoi);

        // Table
        String[] cols = {"Mã", "Tiêu đề", "Loại", "Ngày tạo", "Trạng thái"};
        modelThongBao = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        tblThongBao = new JTable(modelThongBao);
        tblThongBao.setRowHeight(28);
        tblThongBao.setFont(com.hrm.util.UIFonts.TEXT_NORMAL);
        tblThongBao.getTableHeader().setFont(com.hrm.util.UIFonts.BOLD_NORMAL);
        tblThongBao.getTableHeader().setBackground(UIColors.PRIMARY_PURPLE);
        tblThongBao.getTableHeader().setForeground(UIColors.TEXT_DARK);
        tblThongBao.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblThongBao.setSelectionBackground(UIColors.LIGHT_PURPLE);
        tblThongBao.setSelectionForeground(UIColors.TEXT_DARK);

        // Hide Mã column
        tblThongBao.getColumnModel().getColumn(0).setMinWidth(0);
        tblThongBao.getColumnModel().getColumn(0).setMaxWidth(0);
        tblThongBao.getColumnModel().getColumn(0).setWidth(0);

        // Column widths
        tblThongBao.getColumnModel().getColumn(1).setPreferredWidth(350);
        tblThongBao.getColumnModel().getColumn(2).setPreferredWidth(150);
        tblThongBao.getColumnModel().getColumn(3).setPreferredWidth(160);
        tblThongBao.getColumnModel().getColumn(4).setPreferredWidth(100);

        // Renderer for status column
        tblThongBao.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                setHorizontalAlignment(SwingConstants.CENTER);
                String trangThai = value != null ? value.toString() : "";
                setText(trangThai);
                c.setFont(com.hrm.util.UIFonts.TEXT_NORMAL);

                if (isSelected) {
                    c.setForeground(table.getSelectionForeground());
                } else if ("Đã đọc".equals(trangThai)) {
                    c.setForeground(UIColors.SUCCESS_GREEN);
                } else {
                    c.setForeground(UIColors.DANGER_RED);
                    c.setFont(com.hrm.util.UIFonts.BOLD_SMALL);
                }
                return c;
            }
        });

        // Row click -> mark as read
        tblThongBao.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = tblThongBao.getSelectedRow();
                if (row >= 0 && danhSachThongBao != null && row < danhSachThongBao.size()) {
                    ThongBao tb = danhSachThongBao.get(row);
                    if (!tb.isDaDoc()) {
                        markAsRead(tb.getMaThongBao());
                        showNoiDung(tb);
                    } else {
                        showNoiDung(tb);
                    }
                }
            }
        });

        JScrollPane scroll = new JScrollPane(tblThongBao);
        scroll.setBorder(new TitledBorder("Danh sách thông báo"));

        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    // =======================
    // Build Tab 2
    // =======================

    private JPanel buildSendTab() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBackground(UIColors.LIGHT_GRAY_BG);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(UIColors.WHITE);
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIColors.BORDER_GRAY),
                new EmptyBorder(20, 20, 20, 20)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        // Title
        JLabel lblTitle = new JLabel("Gửi thông báo");
        lblTitle.setFont(com.hrm.util.UIFonts.HEADER_SUB);
        lblTitle.setForeground(UIColors.PRIMARY_PURPLE);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        form.add(lblTitle, gbc);
        gbc.gridwidth = 1;

        JLabel lblScope = new JLabel("Phạm vi gửi: " + getScopeDisplayName());
        lblScope.setFont(com.hrm.util.UIFonts.TEXT_NORMAL);
        lblScope.setForeground(UIColors.TEXT_DARK);
        gbc.gridy = 1; gbc.gridx = 0; gbc.gridwidth = 2;
        form.add(lblScope, gbc);
        gbc.gridwidth = 1;

        // Tiêu đề
        gbc.gridy = 2; gbc.gridx = 0;
        form.add(new JLabel("Tiêu đề:"), gbc);
        txtTieuDe = new JTextField(35);
        txtTieuDe.setFont(com.hrm.util.UIFonts.TEXT_MEDIUM);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        form.add(txtTieuDe, gbc);
        gbc.fill = GridBagConstraints.NONE;

        // Nội dung
        gbc.gridy = 3; gbc.gridx = 0;
        form.add(new JLabel("Nội dung:"), gbc);
        txtNoiDung = new JTextArea(5, 35);
        txtNoiDung.setFont(com.hrm.util.UIFonts.TEXT_MEDIUM);
        txtNoiDung.setLineWrap(true);
        txtNoiDung.setWrapStyleWord(true);
        JScrollPane scrollNoiDung = new JScrollPane(txtNoiDung);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.BOTH;
        form.add(scrollNoiDung, gbc);
        gbc.fill = GridBagConstraints.NONE;

        // Loại thông báo
        gbc.gridy = 4; gbc.gridx = 0;
        form.add(new JLabel("Loại thông báo:"), gbc);
        cboLoai = new JComboBox<>(new String[]{"Hệ thống", "Đơn từ", "Thông báo chung"});
        cboLoai.setFont(com.hrm.util.UIFonts.TEXT_MEDIUM);
        cboLoai.setPreferredSize(new Dimension(250, 32));
        gbc.gridx = 1;
        form.add(cboLoai, gbc);

        // Gửi đến
        gbc.gridy = 5; gbc.gridx = 0;
        form.add(new JLabel("Gửi đến:"), gbc);
        cboRecipientType = new JComboBox<>();
        cboRecipientType.setFont(com.hrm.util.UIFonts.TEXT_MEDIUM);
        cboRecipientType.setPreferredSize(new Dimension(220, 32));
        gbc.gridx = 1;
        form.add(cboRecipientType, gbc);

        // Panel chi tiết người nhận (CardLayout)
        recipientDetailPanel = new JPanel(new CardLayout());
        recipientDetailPanel.setOpaque(false);

        recipientDetailPanel.add(createEmptyCard(), RECIPIENT_ALL_COMPANY);
        recipientDetailPanel.add(createEmptyCard(), RECIPIENT_SCOPE);
        recipientDetailPanel.add(createEmptyCard(), RECIPIENT_SELF);

        // Card nhân viên cụ thể
        cboNhanVien = new JComboBox<>();
        cboNhanVien.setFont(com.hrm.util.UIFonts.TEXT_NORMAL);
        cboNhanVien.setPreferredSize(new Dimension(300, 32));
        cboNhanVien.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof NhanVien) {
                    NhanVien nv = (NhanVien) value;
                    setText("[" + nv.getMaNhanVien() + "] " + (nv.getHoTen() != null ? nv.getHoTen() : ""));
                }
                return this;
            }
        });
        JPanel cardNV = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        cardNV.setOpaque(false);
        cardNV.add(cboNhanVien);
        recipientDetailPanel.add(cardNV, RECIPIENT_EMPLOYEE);

        // Card phòng ban
        cboPhongBan = new JComboBox<>();
        cboPhongBan.setFont(com.hrm.util.UIFonts.TEXT_NORMAL);
        cboPhongBan.setPreferredSize(new Dimension(300, 32));
        for (PhongBan d : new PhongBanBUS().getActiveDepartments()) {
            cboPhongBan.addItem(d);
        }
        cboPhongBan.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof PhongBan) setText(((PhongBan) value).getTenPhongBan());
                return this;
            }
        });
        JPanel cardPB = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        cardPB.setOpaque(false);
        cardPB.add(cboPhongBan);
        recipientDetailPanel.add(cardPB, RECIPIENT_DEPARTMENT);

        // Card chức vụ
        cboChucVu = new JComboBox<>();
        cboChucVu.setFont(com.hrm.util.UIFonts.TEXT_NORMAL);
        cboChucVu.setPreferredSize(new Dimension(300, 32));
        for (ChucVu p : new ChucVuBUS().getAllPositions()) {
            cboChucVu.addItem(p);
        }
        cboChucVu.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof ChucVu) setText(((ChucVu) value).getTenChucVu());
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
                showRecipientCard(sel);
            }
        });

        gbc.gridy = 6; gbc.gridx = 1;
        form.add(recipientDetailPanel, gbc);

        // Nút gửi
        gbc.gridy = 7; gbc.gridx = 1;
        gbc.insets = new Insets(16, 8, 8, 8);
        btnGui = UIHelper.createSuccessButton("Gửi thông báo");
        btnGui.addActionListener(e -> guiThongBao());
        form.add(btnGui, gbc);

        loadScopedNhanVienOptions();
        updateRecipientTypeOptions();

        panel.add(form, BorderLayout.NORTH);
        return panel;
    }

    // =======================
    // Data loading
    // =======================

    private void loadThongBao() {
        modelThongBao.setRowCount(0);
        if (currentUser == null) return;

        try {
            danhSachThongBao = thongBaoRepo.findByNguoiNhan(currentUser.getId());
            for (ThongBao tb : danhSachThongBao) {
                String ngayTao = tb.getNgayTao() != null ? tb.getNgayTao().format(DATE_FORMAT) : "";
                modelThongBao.addRow(new Object[]{
                        tb.getMaThongBao(),
                        tb.getTieuDe(),
                        tb.getLoaiDisplay(),
                        ngayTao,
                        tb.isDaDoc() ? "Đã đọc" : "Chưa đọc"
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Lỗi tải thông báo: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    // =======================
    // Actions
    // =======================

    private void danhDauDaDoc() {
        int row = tblThongBao.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng chọn thông báo cần đánh dấu.",
                    "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int maThongBao = (int) modelThongBao.getValueAt(row, 0);
        try {
            KetQua<Void> result = thongBaoService.danhDauDaDoc(maThongBao);
            if (result.isSuccess()) {
                loadThongBao();
            } else {
                JOptionPane.showMessageDialog(this,
                        result.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void danhDauTatCa() {
        if (currentUser == null) return;
        try {
            KetQua<Void> result = thongBaoService.danhDauTatCaDaDoc(currentUser.getId());
            if (result.isSuccess()) {
                loadThongBao();
                JOptionPane.showMessageDialog(this,
                        "Đã đánh dấu tất cả là đã đọc.",
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        result.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void markAsRead(int maThongBao) {
        try {
            thongBaoService.danhDauDaDoc(maThongBao);
            loadThongBao();
        } catch (Exception ex) {
            // Silently fail for auto-read
        }
    }

    private void showNoiDung(ThongBao tb) {
        JTextArea area = new JTextArea(tb.getNoiDung() != null ? tb.getNoiDung() : "");
        area.setEditable(false);
        area.setFont(com.hrm.util.UIFonts.TEXT_MEDIUM);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBackground(UIColors.LIGHT_GRAY_BG);
        area.setBorder(new EmptyBorder(8, 8, 8, 8));
        JScrollPane scroll = new JScrollPane(area);
        scroll.setPreferredSize(new Dimension(420, 200));

        JOptionPane.showMessageDialog(this, scroll,
                tb.getTieuDe() != null ? tb.getTieuDe() : "Thông báo",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void guiThongBao() {
        String tieuDe = txtTieuDe.getText().trim();
        String noiDung = txtNoiDung.getText().trim();
        String loaiThongBao = getSelectedLoaiCode();

        if (tieuDe.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng nhập tiêu đề thông báo.",
                    "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (noiDung.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng nhập nội dung thông báo.",
                    "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (currentUser == null) return;

        String recipientType = (String) cboRecipientType.getSelectedItem();
        if (recipientType == null) return;

        if (RECIPIENT_EMPLOYEE.equals(recipientType) && cboNhanVien.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng chọn nhân viên để gửi.",
                    "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (RECIPIENT_DEPARTMENT.equals(recipientType) && cboPhongBan.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng chọn phòng ban để gửi.",
                    "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (RECIPIENT_POSITION.equals(recipientType) && cboChucVu.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng chọn chức vụ để gửi.",
                    "Lỗi", JOptionPane.WARNING_MESSAGE);
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

        int confirm = JOptionPane.showConfirmDialog(this,
                "Gửi thông báo \"" + tieuDe + "\" đến " + target + "?",
                "Xác nhận gửi", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            if (RECIPIENT_ALL_COMPANY.equals(recipientType)) {
                thongBaoService.guiThongBaoTatCa(currentUser.getId(), tieuDe, noiDung, loaiThongBao);
            } else if (RECIPIENT_SCOPE.equals(recipientType) || RECIPIENT_SELF.equals(recipientType)) {
                thongBaoService.guiThongBaoTheoDanhSachMaNV(
                        currentUser.getId(),
                        getScopedNhanVienIds(),
                        tieuDe,
                        noiDung,
                        loaiThongBao
                );
            } else if (RECIPIENT_EMPLOYEE.equals(recipientType)) {
                NhanVien nv = (NhanVien) cboNhanVien.getSelectedItem();
                thongBaoService.guiThongBaoCaNhan(currentUser.getId(), nv.getMaNhanVien(), tieuDe, noiDung, loaiThongBao);
            } else if (RECIPIENT_DEPARTMENT.equals(recipientType)) {
                PhongBan dept = (PhongBan) cboPhongBan.getSelectedItem();
                thongBaoService.guiThongBaoPhongBan(currentUser.getId(), dept.getId(), tieuDe, noiDung, loaiThongBao);
            } else if (RECIPIENT_POSITION.equals(recipientType)) {
                ChucVu pos = (ChucVu) cboChucVu.getSelectedItem();
                thongBaoService.guiThongBaoChucVu(currentUser.getId(), pos.getId(), tieuDe, noiDung, loaiThongBao);
            }

            JOptionPane.showMessageDialog(this,
                    "Đã gửi thông báo thành công.",
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);

            txtTieuDe.setText("");
            txtNoiDung.setText("");
            loadThongBao();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Lỗi gửi thông báo: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel createEmptyCard() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        return panel;
    }

    private void updateRecipientTypeOptions() {
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        switch (sendScope) {
            case ALL:
                model.addElement(RECIPIENT_ALL_COMPANY);
                model.addElement(RECIPIENT_EMPLOYEE);
                model.addElement(RECIPIENT_DEPARTMENT);
                model.addElement(RECIPIENT_POSITION);
                break;
            case DEPT:
            case TEAM:
                model.addElement(RECIPIENT_SCOPE);
                model.addElement(RECIPIENT_EMPLOYEE);
                break;
            case SELF:
                model.addElement(RECIPIENT_SELF);
                break;
            default:
                break;
        }
        cboRecipientType.setModel(model);
        if (model.getSize() > 0) {
            cboRecipientType.setSelectedIndex(0);
            showRecipientCard((String) model.getSelectedItem());
        }
    }

    private void loadScopedNhanVienOptions() {
        cboNhanVien.removeAllItems();
        if (currentUser == null || currentUser.getNhanVienId() == null) return;

        for (NhanVien nv : NhanVienBUS.getInstance().getAllByActionScope("NOTIFICATION_SEND", currentUser.getNhanVienId())) {
            cboNhanVien.addItem(nv);
        }
    }

    private void showRecipientCard(String selection) {
        CardLayout cl = (CardLayout) recipientDetailPanel.getLayout();
        cl.show(recipientDetailPanel, selection);
    }

    private String getSelectedLoaiCode() {
        Object selected = cboLoai.getSelectedItem();
        if ("Hệ thống".equals(selected)) return "he_thong";
        if ("Đơn từ".equals(selected)) return "don_tu";
        return "thong_bao_chung";
    }

    private List<String> getScopedNhanVienIds() {
        java.util.ArrayList<String> ids = new java.util.ArrayList<>();
        for (int i = 0; i < cboNhanVien.getItemCount(); i++) {
            NhanVien nv = cboNhanVien.getItemAt(i);
            if (nv != null && nv.getMaNhanVien() != null && !nv.getMaNhanVien().trim().isEmpty()) {
                ids.add(nv.getMaNhanVien());
            }
        }
        return ids;
    }

    private String getScopeDisplayName() {
        switch (sendScope) {
            case ALL:
                return "Toàn công ty";
            case DEPT:
                return "Phòng ban của bạn";
            case TEAM:
                return "Nhóm của bạn";
            case SELF:
                return "Chỉ bản thân";
            default:
                return "Không có quyền gửi";
        }
    }
}
