package com.ciphershield.ui.theme;

import java.awt.Font;

/**
 * Typography definitions for CipherShield.
 * Uses system sans-serif fonts (Segoe UI on Windows) with clean fallbacks.
 */
public class ThemeFonts {
    private static final String SANS = "Segoe UI";
    private static final String MONO = "Consolas";

    public static final Font TITLE = new Font(SANS, Font.BOLD, 26);
    public static final Font SUBTITLE = new Font(SANS, Font.BOLD, 18);
    public static final Font SECTION = new Font(SANS, Font.BOLD, 16);
    public static final Font BODY = new Font(SANS, Font.PLAIN, 15);
    public static final Font BODY_BOLD = new Font(SANS, Font.BOLD, 15);
    public static final Font SMALL = new Font(SANS, Font.PLAIN, 13);
    public static final Font BUTTON = new Font(SANS, Font.BOLD, 14);
    public static final Font CODE = new Font(MONO, Font.PLAIN, 14);
    public static final Font STAT_VALUE = new Font(SANS, Font.BOLD, 34);
}
