package com.hrm.gui.leave;

import com.hrm.model.DonXinNghiPhep;
import com.hrm.model.TaiKhoan;
import com.hrm.bus.KetQua;
import com.hrm.bus.NghiPhepBUS;
import com.hrm.util.HRMConstants;
import com.hrm.util.SessionContext;
import com.hrm.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.time.format.DateTimeFormatter;

/**
 * Leave Approve Dialog
 */
public class LeaveApproveDialog extends JDialog {
    private final NghiPhepBUS leaveService;
    private final TaiKhoan currentUser;
    private final int requestId;
    private DonXinNghiPhep request;

    private JTextArea txtNote;
    private JButton btnApprove;
    private JButton btnReject;
    private JButton btnCancel;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public LeaveApproveDialog(Frame parent, int requestId) {
        super(parent, "Duyệt đơn nghỉ phép", true);
        this.leaveService = NghiPhepBUS.getInstance();
        this.currentUser = SessionContext.getInstance().getCurrentUser();
        this.requestId = requestId;
        this.request = leaveService.getRequest(requestId);

        if (request == null) {
            JOptionPane.showMessageDialog(parent,
                    "Không tìm thấy đơn nghỉ phép: " + requestId,
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
            dispose();
            return;
        }

        initComponents();
        setupLayout();
        setupEvents();

        setSize(500, 450);
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    private void initComponents() {
        txtNote = new JTextArea(3, 30);
        txtNote.setLineWrap(true);
        txtNote.setWrapStyleWord(true);

        btnApprove = UIHelper.createSuccessButton("Duyệt");
        btnApprove.setPreferredSize(new Dimension(100, 35));

        btnReject = UIHelper.createDangerButton("Từ chối");
        btnReject.setPreferredSize(new Dimension(100, 35));

        btnCancel = UIHelper.createDefaultButton("Đóng");
        btnCancel.setPreferredSize(new Dimension(100, 35));
    }

    private void setupLayout() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Request info panel
        JPanel infoPanel = new JPanel(new GridLayout(0, 2, 10, 8));
        infoPanel.setBorder(new TitledBorder("Thông tin đơn nghỉ phép"));

        infoPanel.add(new JLabel("Mã đơn:"));
        infoPanel.add(createValueLabel("#" + request.getId()));

        infoPanel.add(new JLabel("Nhân viên:"));
        infoPanel.add(createValueLabel(request.getEmployeeName()));

        infoPanel.add(new JLabel("Loại phép:"));
        infoPanel.add(createValueLabel(request.getLeaveTypeName()));

        infoPanel.add(new JLabel("Từ ngày:"));
        infoPanel.add(createValueLabel(request.getStartDate().format(DATE_FORMAT)));

        infoPanel.add(new JLabel("Đến ngày:"));
        infoPanel.add(createValueLabel(request.getEndDate().format(DATE_FORMAT)));

        infoPanel.add(new JLabel("Số ngày:"));
        infoPanel.add(createValueLabel(request.getTotalDays() + " ngày"));

        infoPanel.add(new JLabel("Trang thái:"));
        JLabel lblStatus = createValueLabel(request.getTrangThai().toString());
        if (request.getTrangThai() == DonXinNghiPhep.TrangThai.CHO_DUYET) {
            lblStatus.setForeground(new Color(230, 126, 34));
        }
        infoPanel.add(lblStatus);

        // Reason panel
        JPanel reasonPanel = new JPanel(new BorderLayout(5, 5));
        reasonPanel.setBorder(new TitledBorder("Lý do nghỉ phép"));
        JTextArea txtReason = new JTextArea(request.getReason());
        txtReason.setEditable(false);
        txtReason.setLineWrap(true);
        txtReason.setWrapStyleWord(true);
        txtReason.setBackground(com.hrm.util.UIColors.LIGHT_GRAY_BG);
        reasonPanel.add(new JScrollPane(txtReason), BorderLayout.CENTER);

        // Note panel
        JPanel notePanel = new JPanel(new BorderLayout(5, 5));
        notePanel.setBorder(new TitledBorder("Ghi chú của người duyệt"));
        notePanel.add(new JScrollPane(txtNote), BorderLayout.CENTER);

        // Center panel
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.add(infoPanel, BorderLayout.NORTH);

        JPanel middlePanel = new JPanel(new GridLayout(2, 1, 10, 10));
        middlePanel.add(reasonPanel);
        middlePanel.add(notePanel);
        centerPanel.add(middlePanel, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.add(btnApprove);
        buttonPanel.add(btnReject);
        buttonPanel.add(btnCancel);

        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private JLabel createValueLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(com.hrm.util.UIFonts.BOLD_SMALL);
        return label;
    }

    private void setupEvents() {
        btnApprove.addActionListener(e -> processRequest(true));
        btnReject.addActionListener(e -> processRequest(false));
        btnCancel.addActionListener(e -> dispose());
    }

    private void processRequest(boolean approve) {
        String note = txtNote.getText().trim();

        if (!approve && note.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng nhập lý do từ chối.",
                    "Thông báo",
                    JOptionPane.WARNING_MESSAGE);
            txtNote.requestFocus();
            return;
        }

        String action = approve ? "Duyệt" : "Từ chối";
        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn " + action + " đơn nghỉ phép này?",
                "Xác nhận",
                JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        String approverId = HRMConstants.USERNAME_ADMIN;
        String approverName = "Quản trị viên";
        if (currentUser.getNhanVienId() != null && !currentUser.getNhanVienId().trim().isEmpty()) {
            approverId = currentUser.getNhanVienId();
            approverName = currentUser.getHoTen();
        }

        KetQua<?> result = leaveService.processRequest(
                requestId,
                approve,
                approverId,
                approverName,
                note);

        if (result.isSuccess()) {
            JOptionPane.showMessageDialog(this,
                    result.getMessage(),
                    "Thành công",
                    JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this,
                    result.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
