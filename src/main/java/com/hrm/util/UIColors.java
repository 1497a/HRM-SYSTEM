package com.hrm.util;

import java.awt.Color;

public final class UIColors {

    private UIColors() {}

    public static final Color PRIMARY_PURPLE = new Color(138, 43, 226);
    public static final Color LIGHT_PURPLE   = new Color(230, 217, 255);
    public static final Color SUCCESS_GREEN  = new Color(40, 167, 69);
    public static final Color DANGER_RED     = new Color(220, 53, 69);
    public static final Color TEXT_DARK      = new Color(51, 51, 51);
    public static Color darker(Color color) {
        return new Color(
            Math.max(0, (int)(color.getRed() * 0.8)),
            Math.max(0, (int)(color.getGreen() * 0.8)),
            Math.max(0, (int)(color.getBlue() * 0.8))
        );
    }
}
