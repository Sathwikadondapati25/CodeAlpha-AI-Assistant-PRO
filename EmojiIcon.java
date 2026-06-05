import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.Icon;

public class EmojiIcon implements Icon {
    private final String emoji;
    private final int size;

    public EmojiIcon(String emoji) {
        this.emoji = emoji;
        this.size = 16;
    }

    public EmojiIcon(String emoji, int size) {
        this.emoji = emoji;
        this.size = size;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Segoe UI Emoji provides native colored emojis on Windows
        g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, size));
        
        FontMetrics fm = g2.getFontMetrics();
        int iconHeight = getIconHeight();
        // Center vertically in the bounding box
        int emojiY = y + (iconHeight - fm.getHeight()) / 2 + fm.getAscent();
        
        g2.drawString(emoji, x, emojiY);
        g2.dispose();
    }

    @Override
    public int getIconWidth() {
        return size;
    }

    @Override
    public int getIconHeight() {
        return size;
    }
}
