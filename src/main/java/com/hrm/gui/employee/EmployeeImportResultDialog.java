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
        super(owner, "Ket qua nhap du lieu", true);
        setLayout(new BorderLayout(0, 8));
        getRootPane().setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        getContentPane().setBackground(Color.WHITE);

        add(buildSummary(result), BorderLayout.NORTH);
        if (!result.errors.isEmpty()) {
            add(buildErrorPanel(result), BorderLayout.CENTER);
        }
        add(buildButtons(), BorderLayout.SOUTH);

        pack();
        setMinimumSize(new Dimension(460, 220));
        setResizable(true);
        setLocationRelativeTo(owner);
    }

    private JPanel buildSummary(ImportResult r) {
        JPanel panel = new JPanel(new GridLayout(4, 1, 0, 4));
        panel.setOpaque(false);

        panel.add(label("Tong so dong xu ly : " + r.total(), Font.PLAIN, UIColors.TEXT_DARK));
        panel.add(label("Them moi thanh cong: " + r.added,
                         Font.BOLD, r.added > 0 ? UIColors.SUCCESS_GREEN : UIColors.TEXT_DARK));
        panel.add(label("Cap nhat thanh cong: " + r.updated,
                         Font.BOLD, r.updated > 0 ? UIColors.PRIMARY_PURPLE : UIColors.TEXT_DARK));
        panel.add(label("Loi / bo qua      : " + r.failed,
                         Font.BOLD, r.failed > 0 ? UIColors.DANGER_RED : UIColors.TEXT_DARK));
        return panel;
    }

    private JScrollPane buildErrorPanel(ImportResult r) {
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
        scroll.setBorder(BorderFactory.createTitledBorder("Chi tiet loi"));
        scroll.setPreferredSize(new Dimension(440, 160));
        return scroll;
    }

    private JPanel buildButtons() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 4));
        p.setOpaque(false);
        JButton btnClose = UIHelper.createPrimaryButton("Dong");
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
