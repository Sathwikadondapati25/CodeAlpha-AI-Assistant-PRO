import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JMenuItem;
import javax.swing.SwingConstants;

public class ModernMenuItem extends JMenuItem {
    private static final int HEIGHT = 38;
    private static final int PREFERRED_WIDTH = 220;

    public ModernMenuItem(String text) {
        super(text);
        init();
    }

    public ModernMenuItem(String text, Icon icon) {
        super(text, icon);
        init();
    }

    private void init() {
        setOpaque(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setFont(new Font("Segoe UI", Font.PLAIN, 13));
        setHorizontalAlignment(SwingConstants.LEFT);
        setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(PREFERRED_WIDTH, HEIGHT);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        boolean isSelected = getModel().isArmed() || getModel().isSelected();
        boolean isDark = ThemeManager.isDarkMode();

        if (isSelected) {
            // Hover color matching the app theme (orange-peach accent)
            g2.setColor(new Color(0xE89B7A));
            g2.fillRoundRect(6, 2, getWidth() - 12, HEIGHT - 4, 8, 8);
            setForeground(Color.WHITE);
        } else {
            setForeground(isDark ? new Color(0xF8FAFC) : new Color(0x1F2937));
        }

        // Paint Icon
        Icon icon = getIcon();
        int textX = 14;
        if (icon != null) {
            int iconWidth = icon.getIconWidth();
            int iconHeight = icon.getIconHeight();
            int iconX = 14; // Left margin for icons
            int iconY = (HEIGHT - iconHeight) / 2;
            
            icon.paintIcon(this, g2, iconX, iconY);
            textX = iconX + iconWidth + 10;
        }
        
        // Paint Text
        g2.setFont(getFont());
        g2.setColor(getForeground());
        FontMetrics fm = g2.getFontMetrics();
        int textY = (HEIGHT - fm.getHeight()) / 2 + fm.getAscent();
        g2.drawString(getText(), textX, textY);
        
        g2.dispose();
    }
}
