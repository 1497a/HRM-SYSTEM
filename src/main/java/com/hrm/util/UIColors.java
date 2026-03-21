package com.hrm.util;

import java.awt.Color;

public final class UIColors {

    private UIColors() {}

    public static final Color PRIMARY_PURPLE = new Color(138, 43, 226);
    public static final Color LIGHT_PURPLE   = new Color(230, 217, 255);
    public static final Color SUCCESS_GREEN  = new Color(40, 167, 69);
    public static final Color DANGER_RED     = new Color(220, 53, 69);
    public static final Color TEXT_DARK      = new Color(51, 51, 51);
    // Trạng thái / nhấn
    public static final Color WARNING_ORANGE = new Color(230, 120, 0);
    public static final Color INFO_TEAL      = new Color(23, 162, 184);
    public static final Color ACCENT_YELLOW  = new Color(255, 193, 7);
    public static final Color LEAVE_BLUE     = new Color(0, 123, 200);
    public static final Color TRIP_PURPLE    = new Color(100, 60, 200);
    // Nền bảng & nhãn trạng thái
    public static final Color TABLE_ROW_ALT  = new Color(250, 248, 255);
    public static final Color BG_SUCCESS     = new Color(200, 255, 200);
    public static final Color BG_DANGER      = new Color(255, 200, 200);
    public static final Color BG_WARNING     = new Color(255, 255, 200);
    public static Color darker(Color color) {
        return new Color(
            Math.max(0, (int)(color.getRed() * 0.8)),
            Math.max(0, (int)(color.getGreen() * 0.8)),
            Math.max(0, (int)(color.getBlue() * 0.8))
        );
    }
}
