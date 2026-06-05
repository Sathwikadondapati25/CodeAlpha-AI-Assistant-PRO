import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;

import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class RoundedPanel extends JPanel {
    private Color fillColor = UiPalette.CARD;
    private int radius = UiPalette.RADIUS_CARD;
    private boolean drawShadow;
    private Color borderColor;
    private int borderWidth;

    public RoundedPanel() {
        setOpaque(false);
    }

    public RoundedPanel(Color fillColor, int radius) {
        this();
        this.fillColor = fillColor;
        this.radius = radius;
    }

    public void setFillColor(Color fillColor) {
        this.fillColor = fillColor;
        repaint();
    }

    public void setRadius(int radius) {
        this.radius = radius;
        repaint();
    }

    public void setDrawShadow(boolean drawShadow) {
        this.drawShadow = drawShadow;
        repaint();
    }

    public void setBorderColor(Color borderColor, int borderWidth) {
        this.borderColor = borderColor;
        this.borderWidth = borderWidth;
        repaint();
    }

    public void setContentPadding(int vertical, int horizontal) {
        setBorder(new EmptyBorder(vertical, horizontal, vertical, horizontal));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();
        int arc = radius * 2;

        if (drawShadow) {
            g2.setColor(UiPalette.SHADOW);
            g2.fillRoundRect(3, 4, w - 6, h - 4, arc, arc);
        }

        g2.setColor(fillColor);
        g2.fillRoundRect(0, 0, w - 1, h - 1, arc, arc);

        if (borderColor != null && borderWidth > 0) {
            g2.setColor(borderColor);
            g2.drawRoundRect(0, 0, w - 1, h - 1, arc, arc);
        }

        g2.dispose();
        super.paintComponent(g);
    }
}
