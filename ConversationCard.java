import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;


public class ConversationCard extends JPanel {
    public interface CardListener {
        void onSelect(String sessionId);

        void onRename(String sessionId);

        void onFavorite(String sessionId);

        void onDelete(String sessionId);

        default void onPin(String sessionId) {
        }
    }

    private final String sessionId;
    private final JLabel titleLabel;
    private final JLabel previewLabel;
    private final JLabel timestampLabel;
    private final JLabel iconLabel;
    private final JLabel pinIndicator;
    private final ThreeDotButton menuButton;
    private boolean active;
    private boolean hovered;
    private boolean favorite;
    private boolean pinned;


    public ConversationCard(String sessionId, String title, boolean favorite, boolean active, CardListener listener) {
        this(sessionId, title, favorite, false, active, listener);
    }

    public ConversationCard(String sessionId, String title, boolean favorite, boolean pinned, boolean active, CardListener listener) {
        this.sessionId = sessionId;
        this.favorite = favorite;
        this.pinned = pinned;
        this.active = active;

        setOpaque(false);
        // Reduce horizontal gaps slightly to prevent right-side cropping while keeping 80px height.
        setLayout(new BorderLayout(8, 0));
        setPreferredSize(new Dimension(0, 80));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Left chat icon - custom drawn
        JPanel iconPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();
                int cx = w / 2;
                int cy = h / 2;
                g2.setColor(UiPalette.ACCENT);
                g2.setStroke(new java.awt.BasicStroke(2.0f, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
                // Draw speech bubble
                g2.drawRoundRect(cx - 10, cy - 8, 18, 12, 3, 3);
                // Draw pointer
                int[] xPts = {cx + 4, cx + 6, cx + 3};
                int[] yPts = {cy + 4, cy + 6, cy + 8};
                g2.drawPolyline(xPts, yPts, 3);
                g2.dispose();
            }
        };
        iconPanel.setOpaque(false);
        iconPanel.setPreferredSize(new Dimension(40, 40));
        
        iconLabel = new JLabel();
        iconLabel.setOpaque(false);

        // Middle text panel: title row + preview row
        titleLabel = new JLabel(formatTitle(title));
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(UiPalette.TEXT);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        titleLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));

        previewLabel = new JLabel("");
        previewLabel.setFont(UiPalette.FONT_CARD);
        previewLabel.setForeground(UiPalette.TEXT_SECONDARY);
        previewLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        previewLabel.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
        previewLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 18));

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.add(titleLabel);
        textPanel.add(previewLabel);

        // Right panel: top row pin + menu, bottom row timestamp
        timestampLabel = new JLabel("");
        timestampLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        timestampLabel.setForeground(UiPalette.TEXT_SECONDARY);
        timestampLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);

        pinIndicator = new JLabel(new PinIcon());
        pinIndicator.setVisible(pinned);
        pinIndicator.setAlignmentY(Component.CENTER_ALIGNMENT);

        menuButton = new ThreeDotButton();
        menuButton.setPreferredSize(new Dimension(34, 34));
        menuButton.setMaximumSize(new Dimension(34, 34));
        menuButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        menuButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showMenu(listener);
            }
        });
        menuButton.setAlignmentX(Component.RIGHT_ALIGNMENT);
        menuButton.setAlignmentY(Component.CENTER_ALIGNMENT);

        JPanel topRightRow = new JPanel();
        topRightRow.setOpaque(false);
        topRightRow.setLayout(new BoxLayout(topRightRow, BoxLayout.X_AXIS));
        topRightRow.add(Box.createHorizontalGlue());
        topRightRow.add(pinIndicator);
        topRightRow.add(Box.createHorizontalStrut(6));
        topRightRow.add(menuButton);

        JPanel rightPanel = new JPanel();
        rightPanel.setOpaque(false);
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setPreferredSize(new Dimension(92, 0));
        rightPanel.setMaximumSize(new Dimension(92, Integer.MAX_VALUE));
        rightPanel.add(topRightRow);
        rightPanel.add(Box.createVerticalGlue());
        rightPanel.add(timestampLabel);

        // Build the main card layout: icon + text + controls
        removeAll();
        setLayout(new BorderLayout(8, 0));
        add(iconPanel, BorderLayout.WEST);
        add(textPanel, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);

        // Mouse interactions
        MouseAdapter click = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // Keep existing behavior: clicking anywhere except menuButton selects.
                // Pin currently consumes clicks, so this avoids accidental selects.
                if (e.getSource() != menuButton) {
                    listener.onSelect(sessionId);
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                hovered = true;
                setOpaque(true);
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hovered = false;
                setOpaque(false);
                repaint();
            }
        };
        addMouseListener(click);
        titleLabel.addMouseListener(click);
        previewLabel.addMouseListener(click);
        iconLabel.addMouseListener(click);

        // initialize empty preview text
        setPreview(null);
    }

    public void setActive(boolean active) {
        this.active = active;
        repaint();
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
        // Preserve the current title text but rebuild favorite prefix.
        String raw = titleLabel.getText();
        if (raw.startsWith("★ ")) {
            raw = raw.substring(2);
        }
        titleLabel.setText(formatTitle(raw));
        repaint();
    }

    public void setPinned(boolean pinned) {
        this.pinned = pinned;
        if (pinIndicator != null) {
            pinIndicator.setVisible(pinned);
        }
        repaint();
    }

    public void setTitle(String title) {
        titleLabel.setText(formatTitle(title));
    }

    // Show actual last message preview.
    // If no messages exist, show: "Start chatting..."
    public void setPreview(String preview) {
        String safe = preview == null ? "" : preview.trim();
        if (safe.isEmpty()) {
            safe = "Start chatting...";
            previewLabel.setForeground(UiPalette.TEXT_SECONDARY);
        } else {
            previewLabel.setForeground(UiPalette.TEXT_SECONDARY);
            if (safe.length() > 48) {
                safe = safe.substring(0, 48) + "...";
            }
        }
        previewLabel.setText(safe);
        setVisible(true);
    }

    public void setTimestamp(String timestamp) {
        timestampLabel.setText(timestamp == null ? "" : timestamp);
    }

    private String formatTitle(String title) {
        String clean = title == null ? "Conversation" : title.trim();
        if (clean.startsWith("★ ")) {
            clean = clean.substring(2);
        }
        String prefix = favorite ? "★ " : "";
        if (clean.length() > 28) {
            clean = clean.substring(0, 28) + "...";
        }
        return prefix + clean;
    }

    private void showMenu(CardListener listener) {
        JPopupMenu menu = new JPopupMenu();
        ThemeManager.stylePopupMenu(menu);
        ModernMenuItem rename = new ModernMenuItem("Rename Chat");
        ModernMenuItem fav = new ModernMenuItem(favorite ? "Unfavorite Chat" : "Favorite Chat");
        ModernMenuItem pinItem = new ModernMenuItem(pinned ? "Unpin Chat" : "Pin Chat");
        ModernMenuItem delete = new ModernMenuItem("Delete Chat");

        rename.setIcon(new PencilIcon());
        fav.setIcon(new StarIcon());
        pinItem.setIcon(new PinIcon());
        delete.setIcon(new TrashIcon());

        rename.addActionListener(e -> listener.onRename(sessionId));
        fav.addActionListener(e -> listener.onFavorite(sessionId));
        pinItem.addActionListener(e -> listener.onPin(sessionId));
        delete.addActionListener(e -> listener.onDelete(sessionId));
        
        menu.add(rename);
        menu.add(fav);
        menu.addSeparator();
        menu.add(pinItem);
        menu.addSeparator();
        menu.add(delete);
        menu.show(menuButton, 0, menuButton.getHeight());
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth();
        int h = getHeight();

        // shadow
        g2.setColor(UiPalette.SHADOW);
        g2.fillRoundRect(4, 6, w - 8, h - 6, UiPalette.RADIUS_CARD, UiPalette.RADIUS_CARD);

        // background
        g2.setColor(UiPalette.CARD);
        g2.fillRoundRect(0, 0, w - 8, h - 8, UiPalette.RADIUS_CARD, UiPalette.RADIUS_CARD);

        // hover overlay
        if (hovered) {
            g2.setColor(UiPalette.HOVER_OVERLAY);
            g2.fillRoundRect(0, 0, w - 8, h - 8, UiPalette.RADIUS_CARD, UiPalette.RADIUS_CARD);
        }

        // active indicator
        if (active) {
            g2.setColor(UiPalette.ACCENT);
            g2.fillRoundRect(0, 0, 6, h - 8, 6, 6);
        }

        g2.setColor(UiPalette.BORDER);
        g2.drawRoundRect(0, 0, w - 8, h - 8, UiPalette.RADIUS_CARD, UiPalette.RADIUS_CARD);
        g2.dispose();
        super.paintComponent(g);
    }

    private static class ThreeDotButton extends JPanel {
        ThreeDotButton() {
            setOpaque(false);
            setPreferredSize(new Dimension(46, 34));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    setOpaque(true);
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    setOpaque(false);
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (isOpaque()) {
                g2.setColor(new Color(245, 244, 243));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            }
            // Draw three dots
            g2.setColor(UiPalette.TEXT_SECONDARY);
            int w = getWidth();
            int h = getHeight();
            int cy = h / 2;
            int dotRadius = 2;
            int spacing = 8;
            int startX = (w - (spacing * 2 + dotRadius * 2)) / 2;
            g2.fillOval(startX, cy - dotRadius, dotRadius * 2, dotRadius * 2);
            g2.fillOval(startX + spacing, cy - dotRadius, dotRadius * 2, dotRadius * 2);
            g2.fillOval(startX + spacing * 2, cy - dotRadius, dotRadius * 2, dotRadius * 2);
            g2.dispose();
        }
    }

    public static class PencilIcon implements Icon {
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(0x6B7280));
            g2.setStroke(new java.awt.BasicStroke(1.5f, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
            int[] xs = {x + 3, x + 5, x + 12, x + 10};
            int[] ys = {y + 13, y + 11, y + 4, y + 6};
            g2.drawPolygon(xs, ys, 4);
            g2.drawLine(x + 4, y + 12, x + 11, y + 5);
            g2.dispose();
        }
        @Override public int getIconWidth() { return 16; }
        @Override public int getIconHeight() { return 16; }
    }

    public static class StarIcon implements Icon {
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(0x6B7280));
            g2.setStroke(new java.awt.BasicStroke(1.5f, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
            int cx = x + 8;
            int cy = y + 8;
            int[] xPoints = new int[10];
            int[] yPoints = new int[10];
            double rOuter = 7;
            double rInner = 3;
            for (int i = 0; i < 10; i++) {
                double angle = Math.toRadians(-90 + i * 36);
                double r = (i % 2 == 0) ? rOuter : rInner;
                xPoints[i] = (int) (cx + r * Math.cos(angle));
                yPoints[i] = (int) (cy + r * Math.sin(angle));
            }
            g2.drawPolygon(xPoints, yPoints, 10);
            g2.dispose();
        }
        @Override public int getIconWidth() { return 16; }
        @Override public int getIconHeight() { return 16; }
    }

    public static class PinIcon implements Icon {
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(UiPalette.ACCENT);
            g2.setStroke(new java.awt.BasicStroke(1.5f, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
            int cx = x + 8;
            int cy = y + 8;
            g2.drawOval(cx - 3, cy - 6, 6, 6);
            g2.drawLine(cx, cy - 3, cx, cy + 5);
            g2.drawLine(cx - 5, cy, cx + 5, cy);
            g2.dispose();
        }
        @Override public int getIconWidth() { return 16; }
        @Override public int getIconHeight() { return 16; }
    }

    public static class TrashIcon implements Icon {
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(0xEF4444));
            g2.setStroke(new java.awt.BasicStroke(1.5f, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
            g2.drawLine(x + 2, y + 4, x + 14, y + 4);
            g2.drawRect(x + 6, y + 2, 4, 2);
            int[] xs = {x + 3, x + 4, x + 12, x + 13};
            int[] ys = {y + 4, y + 14, y + 14, y + 4};
            g2.drawPolyline(xs, ys, 4);
            g2.drawLine(x + 6, y + 6, x + 6, y + 12);
            g2.drawLine(x + 10, y + 6, x + 10, y + 12);
            g2.dispose();
        }
        @Override public int getIconWidth() { return 16; }
        @Override public int getIconHeight() { return 16; }
    }
}

