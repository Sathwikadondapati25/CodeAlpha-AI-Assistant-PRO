import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;

public class IconButton extends JButton {
    protected Color normalBg = Color.WHITE;
    protected Color hoverBg = new Color(0xF3F4F6);
    protected Color pressedBg = new Color(0xE5E7EB);
    private boolean hovered;
    protected boolean circular;

    public IconButton(String text) {
        super(text);
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setFont(new Font("Segoe UI", Font.PLAIN, 16));
        setForeground(UiPalette.TEXT);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hovered = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hovered = false;
                repaint();
            }
        });
    }

    public static IconButton circular(String glyph, Color bg, Color fg, int size) {
        IconButton button = new IconButton(glyph);
        button.circular = true;
        button.normalBg = bg;
        button.hoverBg = bg.brighter();
        button.pressedBg = bg.darker();
        button.setForeground(fg);
        button.setPreferredSize(new Dimension(size, size));
        button.setMinimumSize(new Dimension(size, size));
        button.setMaximumSize(new Dimension(size, size));
        return button;
    }

    public void setNormalBg(Color normalBg) {
        this.normalBg = normalBg;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color bg = getModel().isPressed() ? pressedBg : (hovered ? hoverBg : normalBg);
        int w = getWidth();
        int h = getHeight();

        if (circular) {
            g2.setColor(bg);
            g2.fillOval(0, 0, w - 1, h - 1);
        } else {
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, w - 1, h - 1, 10, 10);
        }

        g2.dispose();
        super.paintComponent(g);
    }
}
