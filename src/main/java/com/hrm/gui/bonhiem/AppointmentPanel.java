package com.hrm.gui.bonhiem;

import com.hrm.model.BoNhiem;
import com.hrm.service.BoNhiemService;
import com.hrm.service.BoNhiemService.ServiceResult;
import com.hrm.util.SessionContext;
import com.hrm.util.UIColors;
import com.hrm.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Panel quản lý Bổ nhiệm & Điều chuyển
 */
public class AppointmentPanel extends JPanel {

    private final BoNhiemService service = new BoNhiemService();

    private JTable table;
    private DefaultTableModel model;
    private JComboBox<String> cboTrangThai;
    private JTextField txtMaNV;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public AppointmentPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(UIColors.LIGHT_GRAY_BG);

        initComponents();
        loadData(service.findAll());
    }

    private void initComponents() {
        
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        topPanel.setOpaque(false);
        topPanel.setBorder(new EmptyBorder(0, 0, 10, 0));

        topPanel.add(new JLabel("Trạng thái: "));
        cboTrangThai = new JComboBox<>(new String[]{"Tất cả", "Chờ duyệt", "Hiệu lực", "Hết hiệu lực", "Từ chối"});
        cboTrangThai.addActionListener(e -> loadData(getFilteredList()));
        topPanel.add(cboTrangThai);

        topPanel.add(Box.createHorizontalStrut(20));
        topPanel.add(new JLabel("Mã NV: "));
        txtMaNV = new JTextField(10);
        txtMaNV.addActionListener(e -> loadData(getFilteredList()));
        topPanel.add(txtMaNV);

        JButton btnRefresh = UIHelper.createDefaultButton("Làm mới");
        btnRefresh.addActionListener(e -> loadData(service.findAll()));
        topPanel.add(Box.createHorizontalStrut(10));
        topPanel.add(btnRefresh);

        add(topPanel, BorderLayout.NORTH);

       
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        toolbar.setOpaque(false);
        toolbar.setBorder(new EmptyBorder(10, 0, 10, 0));

        JButton btnTaoMoi = UIHelper.createSuccessButton("Tạo yêu cầu bổ nhiệm");
        btnTaoMoi.addActionListener(this::moDialogTaoMoi);

        JButton btnDuyet = UIHelper.createPrimaryButton("Duyệt");
        btnDuyet.addActionListener(this::thucHienDuyet);
        btnDuyet.setEnabled(SessionContext.getInstance().hasPermission("APPOINTMENT_APPROVE"));

        JButton btnTuChoi = UIHelper.createDangerButton("Từ chối");
        btnTuChoi.addActionListener(this::thucHienTuChoi);
        btnTuChoi.setEnabled(SessionContext.getInstance().hasPermission("APPOINTMENT_APPROVE"));

        JButton btnKetThuc = UIHelper.createWarningButton("Kết thúc hiệu lực");
        btnKetThuc.addActionListener(this::thucHienKetThuc);
        btnKetThuc.setEnabled(SessionContext.getInstance().hasPermission("APPOINTMENT_APPROVE"));

        toolbar.add(btnTaoMoi);
        toolbar.add(Box.createHorizontalStrut(10));
        toolbar.add(btnDuyet);
        toolbar.add(Box.createHorizontalStrut(10));
        toolbar.add(btnTuChoi);
        toolbar.add(Box.createHorizontalStrut(10));
        toolbar.add(btnKetThuc);

        add(toolbar, BorderLayout.SOUTH);

        
        String[] cols = {"STT", "Mã BN", "Mã NV", "Phòng ban", "Chức vụ", "Loại", "Tỷ lệ (%)", "Từ ngày", "Đến ngày", "Trạng thái", "Ngày duyệt", "Người duyệt", "Lý do"};
        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        table = new JTable(model);
        table.setRowHeight(32);
        table.setShowGrid(true);
        table.setGridColor(new Color(220, 220, 220));
        table.setIntercellSpacing(new Dimension(1, 1));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        
        JTableHeader header = table.getTableHeader();
        header.setVisible(true);
        header.setReorderingAllowed(false);
        header.setResizingAllowed(true);
        header.setBackground(UIColors.PRIMARY_PURPLE);
        header.setForeground(Color.BLACK);
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(100, 100, 100)));
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 35));

        
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(center); // STT
        table.getColumnModel().getColumn(1).setCellRenderer(center); // Mã BN
        table.getColumnModel().getColumn(2).setCellRenderer(center); // Mã NV
        table.getColumnModel().getColumn(6).setCellRenderer(center); // Tỷ lệ

      
        table.getColumnModel().getColumn(9).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, v, s, f, r, c);
                String status = v != null ? v.toString() : "";
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));

                if (status.contains("Chờ duyệt")) {
                    lbl.setForeground(new Color(200, 150, 0));
                    lbl.setBackground(new Color(255, 255, 200));
                } else if (status.contains("Hiệu lực")) {
                    lbl.setForeground(new Color(46, 204, 113));
                    lbl.setBackground(new Color(200, 255, 200));
                } else if (status.contains("Hết hiệu lực")) {
                    lbl.setForeground(Color.GRAY);
                    lbl.setBackground(new Color(220, 220, 220));
                } else if (status.contains("Từ chối")) {
                    lbl.setForeground(new Color(192, 57, 43));
                    lbl.setBackground(new Color(255, 200, 200));
                } else {
                    lbl.setForeground(UIColors.TEXT_DARK);
                }

                if (s) {
                    lbl.setBackground(UIColors.LIGHT_PURPLE);
                    lbl.setForeground(UIColors.DARK_PURPLE);
                }
                return lbl;
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new TitledBorder(BorderFactory.createLineBorder(UIColors.BORDER_GRAY, 1), "Danh sách bổ nhiệm"));
        add(scroll, BorderLayout.CENTER);
    }

    private List<BoNhiem> getFilteredList() {
        String ttText = (String) cboTrangThai.getSelectedItem();
        String maNVStr = txtMaNV.getText().trim();

        List<BoNhiem> list = service.findAll();

        if (ttText != null && !"Tất cả".equals(ttText)) {
            BoNhiem.TrangThai tt = switch (ttText) {
                case "Chờ duyệt" -> BoNhiem.TrangThai.CHO_DUYET;
                case "Hiệu lực" -> BoNhiem.TrangThai.HIEU_LUC;
                case "Hết hiệu lực" -> BoNhiem.TrangThai.HET_HIEU_LUC;
                case "Từ chối" -> BoNhiem.TrangThai.TU_CHOI;
                default -> null;
            };
            if (tt != null) list = list.stream().filter(b -> b.getTrangThai() == tt).toList();
        }

        if (!maNVStr.isBlank()) {
            try {
                int maNV = Integer.parseInt(maNVStr);
                list = list.stream().filter(b -> b.getMaNV() == maNV).toList();
            } catch (NumberFormatException ignored) {}
        }

        return list;
    }

    private void loadData(List<BoNhiem> ds) {
        model.setRowCount(0);
        int stt = 1;
        for (BoNhiem bn : ds) {
            String denNgayStr = bn.getDenNgay() != null ? bn.getDenNgay().format(DATE_FMT) : "Vô thời hạn";
            String ngayDuyetStr = bn.getNgayPheDuyet() != null ? bn.getNgayPheDuyet().format(DATETIME_FMT) : "Chưa duyệt";
            String nguoiDuyetStr = bn.getNguoiDuyet() != null ? String.valueOf(bn.getNguoiDuyet()) : "Chưa duyệt";
            String lyDoStr = bn.getLyDo() != null ? bn.getLyDo() : "";

            model.addRow(new Object[]{
                    stt++,
                    bn.getMaBoNhiem(),
                    bn.getMaNV(),
                    bn.getMaPhongBan(),
                    bn.getMaChucVu(),
                    bn.getLoaiBoNhiem().getDbValue().toUpperCase(),
                    String.format("%.0f%%", bn.getTyLeHuongLuong()),
                    bn.getTuNgay().format(DATE_FMT),
                    denNgayStr,
                    bn.getTrangThai().getDisplayName(),
                    ngayDuyetStr,
                    nguoiDuyetStr,
                    lyDoStr.length() > 50 ? lyDoStr.substring(0, 47) + "..." : lyDoStr
            });
        }
       
        table.getTableHeader().resizeAndRepaint();
        table.getTableHeader().repaint();
    }

   
    private void moDialogTaoMoi(ActionEvent e) {
        BoNhiemCreateDialog dlg = new BoNhiemCreateDialog((Frame) SwingUtilities.getWindowAncestor(this));
        dlg.setVisible(true);
        if (dlg.isSaved()) {
            loadData(getFilteredList());
        }
    }

    private void thucHienDuyet(ActionEvent e) {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một yêu cầu để duyệt!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int maBoNhiem = (int) model.getValueAt(row, 1); 
        int nguoiDuyet = SessionContext.getInstance().getCurrentUser().getId();

        ServiceResult<BoNhiem> res = service.pheDuyet(maBoNhiem, nguoiDuyet);
        JOptionPane.showMessageDialog(this, res.getMessage(),
                res.isSuccess() ? "Thành công" : "Lỗi",
                res.isSuccess() ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);

        if (res.isSuccess()) loadData(getFilteredList());
    }

    private void thucHienTuChoi(ActionEvent e) {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một yêu cầu để từ chối!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int maBoNhiem = (int) model.getValueAt(row, 1);

        String lyDo = JOptionPane.showInputDialog(this, "Nhập lý do từ chối:", "Từ chối bổ nhiệm", JOptionPane.QUESTION_MESSAGE);
        if (lyDo == null || lyDo.trim().isEmpty()) return;

        int nguoiDuyet = SessionContext.getInstance().getCurrentUser().getId();

        ServiceResult<BoNhiem> res = service.tuChoi(maBoNhiem, nguoiDuyet, lyDo.trim());
        JOptionPane.showMessageDialog(this, res.getMessage(),
                res.isSuccess() ? "Thành công" : "Lỗi",
                res.isSuccess() ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);

        if (res.isSuccess()) loadData(getFilteredList());
    }

    private void thucHienKetThuc(ActionEvent e) {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một bổ nhiệm để kết thúc!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int maBoNhiem = (int) model.getValueAt(row, 1);

        String input = JOptionPane.showInputDialog(this,
                "Nhập ngày kết thúc hiệu lực (dd/MM/yyyy):",
                "Kết thúc bổ nhiệm",
                JOptionPane.QUESTION_MESSAGE);

        if (input == null || input.trim().isEmpty()) return;

        try {
            LocalDate denNgay = LocalDate.parse(input.trim(), DATE_FMT);
            ServiceResult<BoNhiem> res = service.ketThuc(maBoNhiem, denNgay);
            JOptionPane.showMessageDialog(this, res.getMessage(),
                    res.isSuccess() ? "Thành công" : "Lỗi",
                    res.isSuccess() ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);

            if (res.isSuccess()) loadData(getFilteredList());
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Định dạng ngày không đúng!\nVui lòng nhập dd/MM/yyyy", "Lỗi định dạng", JOptionPane.ERROR_MESSAGE);
        }
    }
}