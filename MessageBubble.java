import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class MessageBubble extends JPanel {
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    private final String content;
    private final boolean userMessage;
    private final String timeLabel;
    private int contentWidth;
    private int contentHeight;
    private int maxTextWidth = 420;

    public MessageBubble(Message message) {
        this(message.getContent(), message.isUserMessage(), message.getTimestamp());
    }

    public MessageBubble(String content, boolean userMessage, LocalDateTime timestamp) {
        this.content = content == null ? "" : content;
        this.userMessage = userMessage;
        this.timeLabel = timestamp == null ? "" : timestamp.format(TIME_FMT);
        setOpaque(false);
        setBorder(new EmptyBorder(6, 10, 6, 10));
        measure();
    }

    public void setMaxWidth(int max) {
        this.maxTextWidth = Math.max(120, max - 48);
        measure();
    }

    private void measure() {
        Font font = UiPalette.FONT_BODY;
        FontMetrics fm = getFontMetrics(font);
        String[] words = content.split("\\s+");
        StringBuilder line = new StringBuilder();
        int lines = 1;
        contentWidth = 0;

        for (String word : words) {
            String trial = line.length() == 0 ? word : line + " " + word;
            if (fm.stringWidth(trial) > maxTextWidth) {
                contentWidth = Math.max(contentWidth, fm.stringWidth(line.toString()));
                line = new StringBuilder(word);
                lines++;
            } else {
                line = new StringBuilder(trial);
            }
        }
        contentWidth = Math.max(contentWidth, fm.stringWidth(line.toString()));
        contentWidth = Math.min(maxTextWidth, contentWidth + 24);
        contentHeight = lines * fm.getHeight() + 18;
        setPreferredSize(new Dimension(contentWidth + 24, contentHeight + 20));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int bubbleW = contentWidth + 24;
        int bubbleH = contentHeight + 12;
        int x = 0;
        int y = 0;

        Color bg = userMessage ? UiPalette.USER_BUBBLE : UiPalette.AI_BUBBLE;
        g2.setColor(bg);
        g2.fillRoundRect(x, y, bubbleW, bubbleH, UiPalette.RADIUS_BUBBLE, UiPalette.RADIUS_BUBBLE);

        if (!userMessage) {
            g2.setColor(UiPalette.BORDER);
            g2.drawRoundRect(x, y, bubbleW - 1, bubbleH - 1, UiPalette.RADIUS_BUBBLE, UiPalette.RADIUS_BUBBLE);
        }

        g2.setFont(UiPalette.FONT_BODY);
        g2.setColor(userMessage ? UiPalette.USER_BUBBLE_TEXT : UiPalette.TEXT);
        drawWrappedText(g2, content, x + 12, y + 14, bubbleW - 24);

        g2.dispose();
    }

    private void drawWrappedText(Graphics2D g2, String text, int x, int y, int maxWidth) {
        FontMetrics fm = g2.getFontMetrics();
        String[] words = text.split("\\s+");
        StringBuilder line = new StringBuilder();
        int lineY = y + fm.getAscent();

        for (String word : words) {
            String trial = line.length() == 0 ? word : line + " " + word;
            if (fm.stringWidth(trial) > maxWidth) {
                g2.drawString(line.toString(), x, lineY);
                line = new StringBuilder(word);
                lineY += fm.getHeight();
            } else {
                line = new StringBuilder(trial);
            }
        }
        if (line.length() > 0) {
            g2.drawString(line.toString(), x, lineY);
        }
    }
}
