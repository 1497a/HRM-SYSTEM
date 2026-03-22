package com.hrm.gui.admin;

import com.hrm.bus.ChucVuBUS;
import com.hrm.bus.XacThucBUS;
import com.hrm.model.ChucVu;
import com.hrm.model.VaiTro;
import com.hrm.util.DialogUtil;
import com.hrm.util.UIHelper;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Dialog cho admin cau hinh mapping ChucVu -> VaiTro.
 * Hien thi tat ca chuc vu dang hoat dong, moi dong co JComboBox chon vai tro mac dinh.
 * Khi luu, cap nhat CHUCVU.maVaiTro cho tung chuc vu thay doi.
 */
public class ChucVuRoleMappingDialog extends JDialog {

    private static final String ROLE_NONE = "(Không gán)";

    private final ChucVuBUS chucVuBUS = new ChucVuBUS();
    private final List<ChucVu> positions;
    private final List<VaiTro> roles;
    private final String[] roleItems;
    // map maChucVu -> index trong roleItems
    private final Map<String, Integer> selectedIndexMap = new HashMap<>();

    public ChucVuRoleMappingDialog(Frame owner) {
        super(owner, "Cấu hình Chức vụ - Vai trò mặc định", true);
        this.positions = chucVuBUS.getActivePositions();
        this.roles = XacThucBUS.getInstance().getAllRoles();

        // Build role items array
        roleItems = new String[roles.size() + 1];
        roleItems[0] = ROLE_NONE;
        for (int i = 0; i < roles.size(); i++) {
            VaiTro r = roles.get(i);
            roleItems[i + 1] = r.getTenVaiTro() + " (" + r.getId() + ")";
        }

        // Pre-fill selectedIndexMap from current ChucVu.maVaiTro
        for (ChucVu cv : positions) {
            selectedIndexMap.put(cv.getId(), findRoleIndex(cv.getMaVaiTro()));
        }

        initUI();
        pack();
        setMinimumSize(new Dimension(560, 400));
        setLocationRelativeTo(owner);
    }

    private void initUI() {
        JPanel content = new JPanel(new BorderLayout(8, 8));
        content.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // Table showing ChucVu + role selector
        String[] cols = {"Mã CV", "Tên chức vụ", "Cấp bậc", "Vai trò mặc định"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        for (ChucVu cv : positions) {
            model.addRow(new Object[]{
                cv.getId(),
                cv.getTenChucVu(),
                "Cap " + cv.getCapBac(),
                roleItems[selectedIndexMap.get(cv.getId())]
            });
        }

        JTable table = new JTable(model);
        table.setRowHeight(28);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(0).setPreferredWidth(60);
        table.getColumnModel().getColumn(1).setPreferredWidth(160);
        table.getColumnModel().getColumn(2).setPreferredWidth(60);
        table.getColumnModel().getColumn(3).setPreferredWidth(230);

        content.add(new JScrollPane(table), BorderLayout.CENTER);

        // Role selection panel below table
        JPanel selectPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        JLabel lblRole = new JLabel("Vai trò cho dòng được chọn:");
        JComboBox<String> cboRole = new JComboBox<>(roleItems);
        cboRole.setPreferredSize(new Dimension(250, 28));
        JButton btnApply = UIHelper.createPrimaryButton("Áp dụng");

        selectPanel.add(lblRole);
        selectPanel.add(cboRole);
        selectPanel.add(btnApply);

        // When table selection changes, update combobox to show current mapping
        table.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            int row = table.getSelectedRow();
            if (row < 0) return;
            String maCV = (String) model.getValueAt(row, 0);
            cboRole.setSelectedIndex(selectedIndexMap.getOrDefault(maCV, 0));
        });

        // Apply selected role to the selected row
        btnApply.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                DialogUtil.showError(this, "Vui lòng chọn một chức vụ trước.", "Chưa chọn");
                return;
            }
            String maCV = (String) model.getValueAt(row, 0);
            int idx = cboRole.getSelectedIndex();
            selectedIndexMap.put(maCV, idx);
            model.setValueAt(roleItems[idx], row, 3);
        });

        content.add(selectPanel, BorderLayout.SOUTH);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        JButton btnLuu = UIHelper.createSuccessButton("Lưu tất cả");
        JButton btnHuy = UIHelper.createDefaultButton("Đóng");

        btnLuu.addActionListener(e -> saveAll());
        btnHuy.addActionListener(e -> dispose());

        btnPanel.add(btnLuu);
        btnPanel.add(btnHuy);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(content, BorderLayout.CENTER);
        wrapper.add(btnPanel, BorderLayout.SOUTH);

        setContentPane(wrapper);
    }

    private void saveAll() {
        int errors = 0;
        for (ChucVu cv : positions) {
            String maCV = cv.getId();
            int idx = selectedIndexMap.getOrDefault(maCV, 0);
            String maVaiTro = extractRoleCode(roleItems[idx]);
            try {
                chucVuBUS.setMaVaiTro(maCV, maVaiTro);
            } catch (Exception ex) {
                errors++;
                System.err.println("Lỗi cập nhật mapping cho " + maCV + ": " + ex.getMessage());
            }
        }
        if (errors == 0) {
            DialogUtil.showSuccess(this, "Đã lưu mapping Chức vụ - Vai trò thành công.");
            dispose();
        } else {
            DialogUtil.showError(this, "Có " + errors + " lỗi khi lưu. Kiểm tra log để biết thêm chi tiết.");
        }
    }

    private int findRoleIndex(String maVaiTro) {
        if (maVaiTro == null || maVaiTro.isEmpty()) return 0;
        for (int i = 1; i < roleItems.length; i++) {
            if (roleItems[i].endsWith("(" + maVaiTro + ")")) return i;
        }
        return 0;
    }

    private String extractRoleCode(String item) {
        if (item == null || ROLE_NONE.equals(item)) return null;
        int start = item.lastIndexOf('(');
        int end = item.lastIndexOf(')');
        if (start >= 0 && end > start) return item.substring(start + 1, end);
        return null;
    }
}
