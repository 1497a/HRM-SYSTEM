package com.hrm.gui.employee;

import com.hrm.bus.EmployeeImportExportService.ImportResult;
import com.hrm.util.UIColors;
import com.hrm.util.UIFonts;
import com.hrm.util.UIHelper;

import javax.swing.*;
import java.awt.*;

/**
 * Dialog hiển thị kết quả sau khi nhập dữ liệu từ Excel.
 */
public class EmployeeImportResultDialog extends JDialog {

    public EmployeeImportResultDialog(Frame owner, ImportResult result) {
        super(owner, "Kết quả nhập dữ liệu", true);
        setLayout(new BorderLayout(0, 8));
        getRootPane().setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        getContentPane().setBackground(Color.WHITE);

        add(buildSummary(result), BorderLayout.NORTH);
        if (!result.errors.isEmpty()) {
            add(buildDetailPanel(result), BorderLayout.CENTER);
        }
        add(buildButtons(), BorderLayout.SOUTH);

        pack();
        setMinimumSize(new Dimension(460, 240));
        setResizable(true);
        setLocationRelativeTo(owner);
    }

    private JPanel buildSummary(ImportResult r) {
        JPanel panel = new JPanel(new GridLayout(5, 1, 0, 4));
        panel.setOpaque(false);

        panel.add(label("Tổng số dòng xử lý: " + r.total(), Font.PLAIN, UIColors.TEXT_DARK));
        panel.add(label("Thêm mới thành công: " + r.added,
                Font.BOLD, r.added > 0 ? UIColors.SUCCESS_GREEN : UIColors.TEXT_DARK));
        panel.add(label("Cập nhật thành công: " + r.updated,
                Font.BOLD, r.updated > 0 ? UIColors.PRIMARY_PURPLE : UIColors.TEXT_DARK));
        panel.add(label("Bỏ qua: " + r.skipped,
                Font.BOLD, r.skipped > 0 ? UIColors.WARNING_ORANGE : UIColors.TEXT_DARK));
        panel.add(label("Lỗi: " + r.failed,
                Font.BOLD, r.failed > 0 ? UIColors.DANGER_RED : UIColors.TEXT_DARK));
        return panel;
    }

    private JScrollPane buildDetailPanel(ImportResult r) {
        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setFont(UIFonts.TEXT_NORMAL);
        area.setBackground(new Color(255, 248, 248));
        area.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        StringBuilder sb = new StringBuilder();
        for (String err : r.errors) {
            sb.append(err).append('\n');
        }
        area.setText(sb.toString());
        area.setCaretPosition(0);

        JScrollPane scroll = new JScrollPane(area);
        scroll.setBorder(BorderFactory.createTitledBorder("Chi tiết"));
        scroll.setPreferredSize(new Dimension(440, 180));
        return scroll;
    }

    private JPanel buildButtons() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 4));
        p.setOpaque(false);
        JButton btnClose = UIHelper.createPrimaryButton("Đóng");
        btnClose.addActionListener(e -> dispose());
        p.add(btnClose);
        return p;
    }

    private JLabel label(String text, int style, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", style, 13));
        l.setForeground(color);
        return l;
    }
}
