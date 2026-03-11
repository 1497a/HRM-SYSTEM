package com.hrm.util;

import java.awt.Font;

/**
 * UIFonts - Standardized font definitions for HRM System
 */
public final class UIFonts {

    private UIFonts() {
        // Utility class
    }

    public static final String FONT_FAMILY = "Segoe UI";

    // Text fonts
    public static final Font TEXT_SMALL = new Font(FONT_FAMILY, Font.PLAIN, 12);
    public static final Font TEXT_NORMAL = new Font(FONT_FAMILY, Font.PLAIN, 13);
    public static final Font TEXT_MEDIUM = new Font(FONT_FAMILY, Font.PLAIN, 14);

    // Bold fonts
    public static final Font BOLD_SMALL = new Font(FONT_FAMILY, Font.BOLD, 12);
    public static final Font BOLD_NORMAL = new Font(FONT_FAMILY, Font.BOLD, 13);
    public static final Font BOLD_MEDIUM = new Font(FONT_FAMILY, Font.BOLD, 14);

    // Header fonts
    public static final Font HEADER_SUB = new Font(FONT_FAMILY, Font.BOLD, 16);
    public static final Font HEADER_H3 = new Font(FONT_FAMILY, Font.BOLD, 18);
    public static final Font HEADER_H2 = new Font(FONT_FAMILY, Font.BOLD, 20);
    public static final Font HEADER_H1 = new Font(FONT_FAMILY, Font.BOLD, 24);
    
    // Large fonts
    public static final Font DISPLAY_TITLE = new Font(FONT_FAMILY, Font.BOLD, 32);
    public static final Font DISPLAY_LARGE = new Font(FONT_FAMILY, Font.BOLD, 42);

}
