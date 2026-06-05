import java.awt.Color;
import java.awt.Font;

public final class UiPalette {
    public static final Color BG = new Color(0xF8F7F4);
    public static final Color SIDEBAR = new Color(0xF1EFEB);
    public static final Color CARD = Color.WHITE;
    public static final Color TEXT = new Color(0x1F2937);
    public static final Color TEXT_SECONDARY = new Color(0x6B7280);
    public static final Color ACCENT = new Color(0xE89B7A);
    public static final Color BORDER = new Color(0xE5E7EB);
    public static final Color USER_BUBBLE = new Color(0xE89B7A);
    public static final Color AI_BUBBLE = Color.WHITE;
    public static final Color USER_BUBBLE_TEXT = Color.WHITE;
    public static final Color HOVER_OVERLAY = new Color(0, 0, 0, 12);
    public static final Color ACTIVE_CARD = new Color(0xE89B7A, true);
    public static final Color ACTIVE_CARD_BG = new Color(255, 247, 243);
    public static final Color SHADOW = new Color(0, 0, 0, 28);

    public static final int RADIUS_CARD = 12;
    public static final int RADIUS_INPUT = 28;
    public static final int RADIUS_BUBBLE = 18;
    public static final int SIDEBAR_WIDTH = 250;

    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 32);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.PLAIN, 15);
    public static final Font FONT_USERNAME = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_CARD = new Font("Segoe UI", Font.PLAIN, 13);

    private UiPalette() {
    }
}
