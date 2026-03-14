package com.hrm.gui.recruitment;

import com.hrm.model.RecruitmentStatus;
import com.hrm.util.UIColors;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

class RecruitmentStatusRenderer extends DefaultTableCellRenderer {
    private static final Set<String> SUCCESS_STATUSES = statusSet(
            RecruitmentStatus.displayYeuCau(RecruitmentStatus.YeuCau.DA_DUYET),
            RecruitmentStatus.displayYeuCau(RecruitmentStatus.YeuCau.DA_TUYEN_DU),
            RecruitmentStatus.displayTin(RecruitmentStatus.Tin.DANG_TUYEN),
            RecruitmentStatus.displayUngVien(RecruitmentStatus.UngVien.TRUNG_TUYEN, null),
            RecruitmentStatus.displayUngVien(RecruitmentStatus.UngVien.DA_CHUYEN_NHAN_VIEN, "NV")
    );

    private static final Set<String> WARNING_STATUSES = statusSet(
            RecruitmentStatus.displayYeuCau(RecruitmentStatus.YeuCau.CHO_DUYET),
            RecruitmentStatus.displayTin(RecruitmentStatus.Tin.TAM_DUNG),
            RecruitmentStatus.displayUngVien(RecruitmentStatus.UngVien.DANG_PHONG_VAN, null)
    );

    private static final Set<String> DANGER_STATUSES = statusSet(
            RecruitmentStatus.displayYeuCau(RecruitmentStatus.YeuCau.TU_CHOI),
            RecruitmentStatus.displayTin(RecruitmentStatus.Tin.DA_DONG),
            RecruitmentStatus.displayUngVien(RecruitmentStatus.UngVien.TU_CHOI, null)
    );

    private static Set<String> statusSet(String... values) {
        return Set.copyOf(new LinkedHashSet<>(Arrays.asList(values)));
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int col) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
        setHorizontalAlignment(CENTER);
        if (!isSelected && value != null) {
            String v = value.toString();
            if (SUCCESS_STATUSES.contains(v)) {
                c.setForeground(UIColors.SUCCESS_GREEN);
            } else if (DANGER_STATUSES.contains(v)) {
                c.setForeground(UIColors.DANGER_RED);
            } else if (WARNING_STATUSES.contains(v)) {
                c.setForeground(UIColors.WARNING_YELLOW);
            } else {
                c.setForeground(UIColors.INFO_BLUE);
            }
            ((JLabel) c).setFont(com.hrm.util.UIFonts.BOLD_SMALL);
        }
        return c;
    }
}
