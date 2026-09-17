package gui;

import java.awt.*;

/** Central color and font palette so every screen stays visually consistent. */
public final class Theme {
    private Theme() {
    }

    public static final Color PRIMARY = new Color(0x2E7D32);
    public static final Color PRIMARY_LIGHT = new Color(0x66BB6A);
    public static final Color PRIMARY_DARK = new Color(0x1B5E20);
    public static final Color ACCENT = new Color(0x00897B);
    public static final Color WARNING = new Color(0xEF6C00);
    public static final Color DANGER = new Color(0xC62828);
    public static final Color BACKGROUND = new Color(0xF3F6F4);
    public static final Color CARD_BG = Color.WHITE;
    public static final Color SIDEBAR_BG = new Color(0x1B3A2F);
    public static final Color SIDEBAR_TEXT = new Color(0xE8F5E9);
    public static final Color SIDEBAR_HOVER = new Color(0x27523F);
    public static final Color TEXT_DARK = new Color(0x212121);
    public static final Color TEXT_MUTED = new Color(0x757575);

    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 24);
    public static final Font FONT_HEADING = new Font("Segoe UI", Font.BOLD, 17);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_BODY_BOLD = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_NAV = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font FONT_BIG_NUMBER = new Font("Segoe UI", Font.BOLD, 30);
}
