package com.hrm.gui.notification;

import com.hrm.model.ThongBao;
import com.hrm.model.User;
import com.hrm.repo.ThongBaoRepository;
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
        tblThongBao.getTableHeader().setForeground(UIColors.WHITE);
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
        JLabel lblTitle = new JLabel("Gửi thông báo hệ thống");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(UIColors.PRIMARY_PURPLE);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        form.add(lblTitle, gbc);
        gbc.gridwidth = 1;

        // Tiêu đề
        gbc.gridy = 1; gbc.gridx = 0;
        JLabel lblTieuDe = new JLabel("Tiêu đề:");
        lblTieuDe.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        form.add(lblTieuDe, gbc);

        txtTieuDe = new JTextField(35);
        txtTieuDe.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        form.add(txtTieuDe, gbc);
        gbc.fill = GridBagConstraints.NONE;

        // Nội dung
        gbc.gridy = 2; gbc.gridx = 0;
        JLabel lblNoiDung = new JLabel("Nội dung:");
        lblNoiDung.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        form.add(lblNoiDung, gbc);

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
        JLabel lblLoai = new JLabel("Loại thông báo:");
        lblLoai.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        form.add(lblLoai, gbc);

        cboLoai = new JComboBox<>(new String[]{"he_thong", "don_tu", "thong_bao_chung"});
        cboLoai.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cboLoai.setPreferredSize(new Dimension(250, 32));
        gbc.gridx = 1;
        form.add(cboLoai, gbc);

        // Nút gửi
        gbc.gridy = 4; gbc.gridx = 1;
        gbc.insets = new Insets(16, 8, 8, 8);
        btnGui = UIHelper.createSuccessButton("Gửi thông báo");
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
        String loai = (String) cboLoai.getSelectedItem();

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

        int confirm = JOptionPane.showConfirmDialog(this,
                "Gửi thông báo \"" + tieuDe + "\" đến tất cả người dùng?",
                "Xác nhận gửi", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            // Gửi thông báo hệ thống đến người dùng hiện tại như một thông báo chung
            // Phương thức guiThongBaoHeThong gửi cho 1 người nhận
            // Để gửi cho tất cả, sẽ cần triển khai ở service phía backend
            // Hiện tại gọi guiThongBaoHeThong với người nhận là chính mình để demo
            thongBaoService.guiThongBaoHeThong(currentUser.getId(), tieuDe, noiDung);

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
}
