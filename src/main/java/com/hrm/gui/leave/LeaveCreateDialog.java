package com.hrm.gui.leave;

import com.hrm.model.LoaiPhep;
import com.hrm.model.TaiKhoan;
import com.hrm.bus.KetQua;
import com.hrm.bus.NghiPhepBUS;
import com.hrm.gui.components.BaseFormDialog;
import com.hrm.util.SessionContext;
import com.hrm.util.UIFonts;
import com.hrm.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import java.awt.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

/**
 * Leave Create Dialog
 */
public class LeaveCreateDialog extends BaseFormDialog {

    private final NghiPhepBUS leaveService;
    private final TaiKhoan currentUser;
    private JComboBox<LoaiPhep> cboLeaveType;
    private JSpinner spnStartDate;
    private JSpinner spnEndDate;
    private JLabel lblTotalDays;
    private JTextArea txtReason;
    private JButton btnSubmit;
    private JButton btnCancel;
    private boolean successful = false;
    public LeaveCreateDialog(Frame parent) {
        super(parent, "Tạo Đơn Nghỉ Phép", true);
        this.leaveService = NghiPhepBUS.getInstance();
        this.currentUser = SessionContext.getInstance().getCurrentUser();
        initComponents();
        setupLayout();
        setupEvents();
        setSize(450, 400);
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    private void initComponents() {
        // Leave type combo
        cboLeaveType = new JComboBox<>();
        for (LoaiPhep type : leaveService.getAllLeaveTypes()) {
            cboLeaveType.addItem(type);
        }
        // Date spinners
        SpinnerDateModel startModel = new SpinnerDateModel(new Date(), null, null, java.util.Calendar.DAY_OF_MONTH);
        spnStartDate = new JSpinner(startModel);
        spnStartDate.setEditor(new JSpinner.DateEditor(spnStartDate, "dd/MM/yyyy"));
        SpinnerDateModel endModel = new SpinnerDateModel(new Date(), null, null, java.util.Calendar.DAY_OF_MONTH);
        spnEndDate = new JSpinner(endModel);
        spnEndDate.setEditor(new JSpinner.DateEditor(spnEndDate, "dd/MM/yyyy"));
        // Total days label
        lblTotalDays = new JLabel("0 ngày làm việc");
        lblTotalDays.setFont(UIFonts.BOLD_NORMAL);
        lblTotalDays.setForeground(new Color(0, 102, 153));
        // Reason text area
        txtReason = new JTextArea(4, 30);
        txtReason.setLineWrap(true);
        txtReason.setWrapStyleWord(true);
        // Buttons
        btnSubmit = UIHelper.createSuccessButton("Gửi đơn");
        btnCancel = UIHelper.createDefaultButton("Hủy");
    }

    private void setupLayout() {
        JPanel mainPanel = createMainPanel();
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = UIHelper.gbcFill(0, 0);
        gbc.insets = new Insets(8, 8, 8, 8);
        int row = 0;
        // Employee info
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Nhân viên:"), gbc);
        gbc.gridx = 1;
        JLabel lblEmployee = new JLabel(currentUser.getHoTen());
        lblEmployee.setFont(com.hrm.util.UIFonts.BOLD_SMALL);
        formPanel.add(lblEmployee, gbc);
        // Leave type
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Loại phép:"), gbc);
        gbc.gridx = 1;
        formPanel.add(cboLeaveType, gbc);
        // Start date
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Từ ngày:"), gbc);
        gbc.gridx = 1;
        formPanel.add(spnStartDate, gbc);
        // End date
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Đến ngày:"), gbc);
        gbc.gridx = 1;
        formPanel.add(spnEndDate, gbc);
        // Total days
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Tổng số ngày:"), gbc);
        gbc.gridx = 1;
        formPanel.add(lblTotalDays, gbc);
        // Reason
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        formPanel.add(new JLabel("Lý do:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        formPanel.add(new JScrollPane(txtReason), gbc);
        // Button panel
        JPanel buttonPanel = createButtonPanel(btnSubmit, btnCancel);
        ((FlowLayout) buttonPanel.getLayout()).setAlignment(FlowLayout.CENTER);
        ((FlowLayout) buttonPanel.getLayout()).setHgap(15);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        setContentPane(mainPanel);
    }

    private void setupEvents() {
        // Calculate days when dates change
        spnStartDate.addChangeListener(e -> calculateDays());
        spnEndDate.addChangeListener(e -> calculateDays());
        btnSubmit.addActionListener(e -> submitRequest());
        btnCancel.addActionListener(e -> dispose());
        calculateDays();
    }

    private void calculateDays() {
        Date startDate = (Date) spnStartDate.getValue();
        Date endDate = (Date) spnEndDate.getValue();
        LocalDate start = startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate end = endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        int days = leaveService.calculateBusinessDays(start, end);
        lblTotalDays.setText(days + " ngày làm việc");
    }

    private void submitRequest() {
        LoaiPhep leaveType = (LoaiPhep) cboLeaveType.getSelectedItem();
        Date startDate = (Date) spnStartDate.getValue();
        Date endDate = (Date) spnEndDate.getValue();
        String reason = txtReason.getText().trim();
        if (reason.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng nhập lý do nghỉ phép",
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
            txtReason.requestFocus();
            return;
        }
        LocalDate start = startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate end = endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        if (start.isAfter(end)) {
            JOptionPane.showMessageDialog(this, "Ngay bat dau phai truoc hoac bang Ngay ket thuc.",
                    "Loi nhap lieu", JOptionPane.ERROR_MESSAGE);
            spnStartDate.requestFocus();
            return;
        }
        KetQua<?> result = leaveService.createRequest(
                currentUser.getMaNV(),
                currentUser.getHoTen(),
                leaveType.getId(),
                start,
                end,
                reason);
        if (result.isSuccess()) {
            JOptionPane.showMessageDialog(this,
                    result.getMessage(),
                    "Thành công",
                    JOptionPane.INFORMATION_MESSAGE);
            successful = true;
            dispose();
        } else {
            JOptionPane.showMessageDialog(this,
                    result.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSuccessful() {
        return successful;
    }

}
