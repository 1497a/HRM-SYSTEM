package com.hrm.gui.evaluation;

import com.hrm.model.TieuChiDanhGia;
import com.hrm.bus.DanhGiaBUS;
import com.hrm.bus.KetQua;
import com.hrm.gui.components.BaseFormDialog;
import com.hrm.util.UIColors;
import com.hrm.util.UIHelper;
import com.hrm.util.UIFonts;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Evaluation Configuration Dialog - manage criteria
 */
public class EvalConfigDialog extends BaseFormDialog {
    private final DanhGiaBUS evalService;
    private enum FormMode { ADD, EDIT }

    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtName;
    private JTextArea txtDescription;
    private JSpinner spnWeight;
    private JButton btnAdd;
    private JButton btnUpdate;
    private JButton btnDelete;
    private JButton btnNew;
    private JLabel lblTotalWeight;
    private JLabel lblMode;
    private int selectedId = -1;
    private FormMode formMode = FormMode.ADD;
    public EvalConfigDialog(Frame parent) {
        super(parent, "Cấu Hình Tiêu Chí Đánh Giá", true);
        this.evalService = DanhGiaBUS.getInstance();
        initComponents();
        setupLayout();
        setupEvents();
        loadData();
        setSize(700, 500);
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        String[] columns = {"ID", "Tên tiêu chí", "Mô tả", "Trọng số (%)"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setRowHeight(25);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(0).setPreferredWidth(40);
        table.getColumnModel().getColumn(1).setPreferredWidth(150);
        table.getColumnModel().getColumn(2).setPreferredWidth(250);
        table.getColumnModel().getColumn(3).setPreferredWidth(80);
        txtName = new JTextField(20);
        txtDescription = new JTextArea(3, 20);
        txtDescription.setLineWrap(true);
        txtDescription.setWrapStyleWord(true);
        spnWeight = new JSpinner(new SpinnerNumberModel(10, 1, 100, 1));
        btnAdd = UIHelper.createSuccessButton("Thêm mới");
        btnUpdate = UIHelper.createPrimaryButton("Cập nhật");
        btnDelete = UIHelper.createDangerButton("Xóa");
        btnNew = UIHelper.createPrimaryButton("Làm mới");
        btnUpdate.setEnabled(false);
        btnDelete.setEnabled(false);
        lblTotalWeight = new JLabel("Tổng trọng số: 0%");
        lblTotalWeight.setFont(UIFonts.BOLD_NORMAL);
        lblMode = new JLabel();
        lblMode.setFont(com.hrm.util.UIFonts.BOLD_NORMAL);
        updateModeLabel();
    }

    private void setupLayout() {
        JPanel mainPanel = createMainPanel();
        JScrollPane tableScroll = createScrollPane(table);
        tableScroll.setBorder(new TitledBorder("Danh sách tiêu chí"));
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(new TitledBorder("Thông tin tiêu chí"));
        GridBagConstraints gbc = UIHelper.gbc(0, 0);
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Tên tiêu chí:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        formPanel.add(txtName, gbc);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        formPanel.add(new JLabel("Mô tả:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        formPanel.add(new JScrollPane(txtDescription), gbc);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(new JLabel("Trọng số (%):"), gbc);
        gbc.gridx = 1;
        formPanel.add(spnWeight, gbc);
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(lblMode, gbc);
        JPanel buttonPanel = new JPanel(new GridLayout(2, 2, 6, 6));
        buttonPanel.add(btnAdd);
        buttonPanel.add(btnNew);
        buttonPanel.add(btnUpdate);
        buttonPanel.add(btnDelete);
        gbc.gridy = 4;
        formPanel.add(buttonPanel, gbc);
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setPreferredSize(new Dimension(350, 0));
        rightPanel.add(formPanel, BorderLayout.CENTER);
        JPanel notePanel = new JPanel(new BorderLayout(5, 0));
        notePanel.setBackground(UIColors.BG_WARNING);
        notePanel.setBorder(new EmptyBorder(8, 10, 8, 10));
        notePanel.add(new JLabel("<html><b>Lưu ý:</b> Tổng trọng số của tất cả tiêu chí phải bằng 100%</html>"), BorderLayout.CENTER);
        notePanel.add(lblTotalWeight, BorderLayout.EAST);
        mainPanel.add(tableScroll, BorderLayout.CENTER);
        mainPanel.add(rightPanel, BorderLayout.EAST);
        mainPanel.add(notePanel, BorderLayout.SOUTH);
        setContentPane(mainPanel);
    }

    private void setupEvents() {
        table.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) {
                return;
            }
            int row = table.getSelectedRow();
            if (row >= 0) {
                populateFormFromRow(row);
                setFormMode(FormMode.EDIT);
            } else {
                setFormMode(FormMode.ADD);
            }
        });
        btnAdd.addActionListener(e -> addCriteria());
        btnUpdate.addActionListener(e -> updateCriteria());
        btnDelete.addActionListener(e -> deleteCriteria());
        btnNew.addActionListener(e -> clearForm());
    }

    private void loadData() {
        tableModel.setRowCount(0);
        List<TieuChiDanhGia> criteriaList = evalService.getAllCriteria();
        for (TieuChiDanhGia c : criteriaList) {
                Object[] row = {c.getId(), c.getTenTieuChi(), c.getMoTa(), c.getDiemToiDa()};
            tableModel.addRow(row);
        }
        updateTotalWeight();
        clearForm();
    }

    private void updateTotalWeight() {
        int total = evalService.getTotalWeight();
        lblTotalWeight.setText("Tổng trọng số: " + total + "%");
        if (total == 100) {
            lblTotalWeight.setForeground(new Color(46, 204, 113));
        } else {
            lblTotalWeight.setForeground(com.hrm.util.UIColors.DANGER_RED);
        }
    }

    private void clearForm() {
        selectedId = -1;
        txtName.setText("");
        txtDescription.setText("");
        spnWeight.setValue(10);
        table.clearSelection();
        setFormMode(FormMode.ADD);
    }

    private void addCriteria() {
        int w = getWeightValue();
        KetQua<?> result = evalService.saveCriteria(
                txtName.getText().trim(), txtDescription.getText().trim(), "", w, w);
        handleResult(result);
    }

    private void updateCriteria() {
        if (selectedId < 0) return;
        int w = getWeightValue();
        KetQua<?> result = evalService.updateCriteria(
                selectedId, txtName.getText().trim(), txtDescription.getText().trim(), "", w, w);
        handleResult(result);
    }

    private void handleResult(KetQua<?> result) {
        if (result.isSuccess()) {
            loadData();
            JOptionPane.showMessageDialog(this, result.getMessage(), "Thành công", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, result.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteCriteria() {
        if (selectedId < 0) {
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn xóa tiêu chí này?",
                "Xác nhận",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            KetQua<?> result = evalService.deleteCriteria(selectedId);
            if (result.isSuccess()) {
                loadData();
            } else {
                JOptionPane.showMessageDialog(this, result.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void populateFormFromRow(int viewRow) {
        int row = table.convertRowIndexToModel(viewRow);
        selectedId = (int) tableModel.getValueAt(row, 0);
        txtName.setText((String) tableModel.getValueAt(row, 1));
        txtDescription.setText((String) tableModel.getValueAt(row, 2));
        Object weightObj = tableModel.getValueAt(row, 3);
        if (weightObj instanceof Number) {
            spnWeight.setValue(((Number) weightObj).intValue());
        } else {
            spnWeight.setValue(Integer.parseInt(String.valueOf(weightObj)));
        }
    }

    private int getWeightValue() {
        Object value = spnWeight.getValue();
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return Integer.parseInt(String.valueOf(value));
    }

    private void setFormMode(FormMode mode) {
        formMode = mode;
        boolean editing = mode == FormMode.EDIT && selectedId > 0;
        btnAdd.setEnabled(mode == FormMode.ADD);
        btnUpdate.setEnabled(editing);
        btnDelete.setEnabled(editing);
        updateModeLabel();
    }

    private void updateModeLabel() {
        if (formMode == FormMode.EDIT) {
            lblMode.setText("Chế độ: CẬP NHẬT");
            lblMode.setForeground(new Color(41, 128, 185));
        } else {
            lblMode.setText("Chế độ: THÊM MỚI");
            lblMode.setForeground(new Color(39, 174, 96));
        }
    }
}
