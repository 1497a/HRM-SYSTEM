package com.hrm.gui.notification;

import com.hrm.model.Department;
import com.hrm.model.NhanVien;
import com.hrm.model.Position;
import com.hrm.model.ThongBao;
import com.hrm.model.User;
import com.hrm.repo.ThongBaoRepository;
import com.hrm.service.DepartmentService;
import com.hrm.service.NhanVienService;
import com.hrm.service.PositionService;
import com.hrm.service.ServiceResult;
import com.hrm.service.ThongBaoService;
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

    private final ThongBaoService thongBaoService;
    private final ThongBaoRepository thongBaoRepo;
    private final User currentUser;
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
    private JComboBox<Department> cboPhongBan;
    private JComboBox<Position> cboChucVu;
    private JButton btnGui;

    private List<ThongBao> danhSachThongBao;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public NotificationPanel() {
        this.thongBaoService = ThongBaoService.getInstance();
        this.thongBaoRepo = ThongBaoRepository.getInstance();
        this.currentUser = SessionContext.getInstance().getCurrentUser();

        SessionContext sc = SessionContext.getInstance();
        this.canSend = sc.hasRole("ADMIN") || sc.hasRole("HR") || sc.hasPermission("NOTIFICATION_SEND");

        setLayout(new BorderLayout());
        setBackground(UIColors.LIGHT_GRAY_BG);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 14));
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
        String[] cols = {"Mã", "Tiêu đề", "Loại", "Ngày tạo", "Đã đọc"};
        modelThongBao = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        tblThongBao = new JTable(modelThongBao);
        tblThongBao.setRowHeight(28);
        tblThongBao.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tblThongBao.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
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
        tblThongBao.getColumnModel().getColumn(4).setPreferredWidth(80);

        // Renderer for "Đã đọc" column
        tblThongBao.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                setHorizontalAlignment(SwingConstants.CENTER);
                if (!isSelected && value != null) {
                    boolean daDoc = Boolean.parseBoolean(value.toString());
                    if (daDoc) {
                        c.setForeground(UIColors.SUCCESS_GREEN);
                        setText("Đã đọc");
                    } else {
                        c.setForeground(UIColors.DANGER_RED);
                        setText("Chưa đọc");
                        c.setFont(new Font("Segoe UI", Font.BOLD, 12));
                    }
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
        JLabel lblTitle = new JLabel("Gui thong bao he thong");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(UIColors.PRIMARY_PURPLE);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        form.add(lblTitle, gbc);
        gbc.gridwidth = 1;

        // Tiêu đề
        gbc.gridy = 1; gbc.gridx = 0;
        form.add(new JLabel("Tieu de:"), gbc);
        txtTieuDe = new JTextField(35);
        txtTieuDe.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        form.add(txtTieuDe, gbc);
        gbc.fill = GridBagConstraints.NONE;

        // Nội dung
        gbc.gridy = 2; gbc.gridx = 0;
        form.add(new JLabel("Noi dung:"), gbc);
        txtNoiDung = new JTextArea(5, 35);
        txtNoiDung.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtNoiDung.setLineWrap(true);
        txtNoiDung.setWrapStyleWord(true);
        JScrollPane scrollNoiDung = new JScrollPane(txtNoiDung);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.BOTH;
        form.add(scrollNoiDung, gbc);
        gbc.fill = GridBagConstraints.NONE;

        // Loại thông báo
        gbc.gridy = 3; gbc.gridx = 0;
        form.add(new JLabel("Loai thong bao:"), gbc);
        cboLoai = new JComboBox<>(new String[]{"he_thong", "don_tu", "thong_bao_chung"});
        cboLoai.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cboLoai.setPreferredSize(new Dimension(250, 32));
        gbc.gridx = 1;
        form.add(cboLoai, gbc);

        // Gửi đến
        gbc.gridy = 4; gbc.gridx = 0;
        form.add(new JLabel("Gui den:"), gbc);
        cboRecipientType = new JComboBox<>(new String[]{
            "Tat ca nhan vien", "Nhan vien cu the", "Phong ban", "Chuc vu"
        });
        cboRecipientType.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cboRecipientType.setPreferredSize(new Dimension(220, 32));
        gbc.gridx = 1;
        form.add(cboRecipientType, gbc);

        // Panel chi tiết người nhận (CardLayout)
        recipientDetailPanel = new JPanel(new CardLayout());
        recipientDetailPanel.setOpaque(false);

        JPanel emptyCard = new JPanel();
        emptyCard.setOpaque(false);
        recipientDetailPanel.add(emptyCard, "Tat ca nhan vien");

        // Card nhân viên cụ thể
        cboNhanVien = new JComboBox<>();
        cboNhanVien.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cboNhanVien.setPreferredSize(new Dimension(300, 32));
        for (NhanVien nv : NhanVienService.getInstance().getDangLamViec()) {
            cboNhanVien.addItem(nv);
        }
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
        recipientDetailPanel.add(cardNV, "Nhan vien cu the");

        // Card phòng ban
        cboPhongBan = new JComboBox<>();
        cboPhongBan.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cboPhongBan.setPreferredSize(new Dimension(300, 32));
        for (Department d : new DepartmentService().getActiveDepartments()) {
            cboPhongBan.addItem(d);
        }
        cboPhongBan.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Department) setText(((Department) value).getTenPhongBan());
                return this;
            }
        });
        JPanel cardPB = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        cardPB.setOpaque(false);
        cardPB.add(cboPhongBan);
        recipientDetailPanel.add(cardPB, "Phong ban");

        // Card chức vụ
        cboChucVu = new JComboBox<>();
        cboChucVu.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cboChucVu.setPreferredSize(new Dimension(300, 32));
        for (Position p : new PositionService().getAllPositions()) {
            cboChucVu.addItem(p);
        }
        cboChucVu.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Position) setText(((Position) value).getTenChucVu());
                return this;
            }
        });
        JPanel cardCV = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        cardCV.setOpaque(false);
        cardCV.add(cboChucVu);
        recipientDetailPanel.add(cardCV, "Chuc vu");

        cboRecipientType.addActionListener(e -> {
            String sel = (String) cboRecipientType.getSelectedItem();
            if (sel != null) {
                CardLayout cl = (CardLayout) recipientDetailPanel.getLayout();
                cl.show(recipientDetailPanel, sel);
            }
        });

        gbc.gridy = 5; gbc.gridx = 1;
        form.add(recipientDetailPanel, gbc);

        // Nút gửi
        gbc.gridy = 6; gbc.gridx = 1;
        gbc.insets = new Insets(16, 8, 8, 8);
        btnGui = UIHelper.createSuccessButton("Gui thong bao");
        btnGui.addActionListener(e -> guiThongBao());
        form.add(btnGui, gbc);

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
                        tb.isDaDoc()
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
            ServiceResult<Void> result = thongBaoService.danhDauDaDoc(maThongBao);
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
            ServiceResult<Void> result = thongBaoService.danhDauTatCaDaDoc(currentUser.getId());
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
        area.setFont(new Font("Segoe UI", Font.PLAIN, 14));
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

        if (tieuDe.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Vui long nhap tieu de thong bao.",
                    "Loi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (noiDung.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Vui long nhap noi dung thong bao.",
                    "Loi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (currentUser == null) return;

        String recipientType = (String) cboRecipientType.getSelectedItem();

        if ("Nhan vien cu the".equals(recipientType) && cboNhanVien.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this,
                    "Vui long chon nhan vien de gui.",
                    "Loi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if ("Phong ban".equals(recipientType) && cboPhongBan.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this,
                    "Vui long chon phong ban de gui.",
                    "Loi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if ("Chuc vu".equals(recipientType) && cboChucVu.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this,
                    "Vui long chon chuc vu de gui.",
                    "Loi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String target;
        if ("Tat ca nhan vien".equals(recipientType)) {
            target = "tat ca nhan vien";
        } else if ("Nhan vien cu the".equals(recipientType)) {
            NhanVien nv = (NhanVien) cboNhanVien.getSelectedItem();
            target = nv.getMaNhanVien() + " - " + nv.getHoTen();
        } else if ("Phong ban".equals(recipientType)) {
            target = "phong ban: " + ((Department) cboPhongBan.getSelectedItem()).getTenPhongBan();
        } else {
            target = "chuc vu: " + ((Position) cboChucVu.getSelectedItem()).getTenChucVu();
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Gui thong bao \"" + tieuDe + "\" den " + target + "?",
                "Xac nhan gui", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            if ("Tat ca nhan vien".equals(recipientType)) {
                thongBaoService.guiThongBaoTatCa(currentUser.getId(), tieuDe, noiDung);
            } else if ("Nhan vien cu the".equals(recipientType)) {
                NhanVien nv = (NhanVien) cboNhanVien.getSelectedItem();
                thongBaoService.guiThongBaoCaNhan(currentUser.getId(), nv.getId(), tieuDe, noiDung);
            } else if ("Phong ban".equals(recipientType)) {
                Department dept = (Department) cboPhongBan.getSelectedItem();
                thongBaoService.guiThongBaoPhongBan(currentUser.getId(), dept.getMaPhongBan(), tieuDe, noiDung);
            } else if ("Chuc vu".equals(recipientType)) {
                Position pos = (Position) cboChucVu.getSelectedItem();
                thongBaoService.guiThongBaoChucVu(currentUser.getId(), pos.getMaChucVu(), tieuDe, noiDung);
            }

            JOptionPane.showMessageDialog(this,
                    "Da gui thong bao thanh cong.",
                    "Thanh cong", JOptionPane.INFORMATION_MESSAGE);

            txtTieuDe.setText("");
            txtNoiDung.setText("");
            loadThongBao();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Loi gui thong bao: " + ex.getMessage(),
                    "Loi", JOptionPane.ERROR_MESSAGE);
        }
    }
}
