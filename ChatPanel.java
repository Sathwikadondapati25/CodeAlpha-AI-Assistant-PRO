import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class ChatPanel extends JPanel {
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel statePanel = new JPanel(cardLayout);
    private final JPanel messagesContainer = new JPanel();
    private final JScrollPane scrollPane;
    private final JPanel viewportWrapper = new JPanel(new BorderLayout());
    private final JPanel typingRow = new JPanel();
    private final ThinkingIndicator thinkingIndicator = new ThinkingIndicator();
    private final ScrollToBottomButton scrollButton = new ScrollToBottomButton();
    private final JPanel chatLayer = new JPanel(new BorderLayout());
    private final JPanel chatContainer = new JPanel(new BorderLayout());
    private int messageCount;
    private int maxBubbleWidth = 500;

    public ChatPanel() {
        setLayout(new BorderLayout());
        setBackground(UiPalette.BG);
        setOpaque(true);

        statePanel.setOpaque(false);
        statePanel.add(buildWelcomePanel(), "welcome");

        messagesContainer.setLayout(new BoxLayout(messagesContainer, BoxLayout.Y_AXIS));
        messagesContainer.setBackground(UiPalette.BG);
        messagesContainer.setBorder(BorderFactory.createEmptyBorder(12, 12, 24, 12));

        viewportWrapper.setOpaque(false);
        viewportWrapper.add(messagesContainer, BorderLayout.CENTER);

        scrollPane = new JScrollPane(viewportWrapper);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setViewportBorder(null);
        scrollPane.getViewport().setBackground(UiPalette.BG);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(20);
        scrollPane.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {
            @Override
            public void adjustmentValueChanged(AdjustmentEvent e) {
                updateScrollButtonState();
            }
        });
        scrollPane.getViewport().addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                updateScrollButtonState();
            }
        });

        typingRow.setOpaque(false);
        typingRow.setLayout(new BoxLayout(typingRow, BoxLayout.X_AXIS));
        typingRow.setVisible(false);
        typingRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        typingRow.setBorder(BorderFactory.createEmptyBorder(0, 16, 12, 16));
        thinkingIndicator.setVisible(false);
        typingRow.add(thinkingIndicator);
        typingRow.add(Box.createHorizontalGlue());

        chatContainer.setBackground(UiPalette.BG);
        chatContainer.setOpaque(true);
        chatContainer.add(scrollPane, BorderLayout.CENTER);
        chatContainer.add(typingRow, BorderLayout.SOUTH);

        chatLayer.setOpaque(false);

        // holder for scroll button in bottom-right
        JPanel buttonHolder = new JPanel(new BorderLayout());
        buttonHolder.setOpaque(false);
        JPanel rightHolder = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 12, 12));
        rightHolder.setOpaque(false);
        rightHolder.add(scrollButton);
        buttonHolder.add(rightHolder, BorderLayout.SOUTH);

        chatLayer.add(chatContainer, BorderLayout.CENTER);
        chatLayer.add(buttonHolder, BorderLayout.SOUTH);

        statePanel.add(chatLayer, "chat");
        cardLayout.show(statePanel, "welcome");
        add(statePanel, BorderLayout.CENTER);
    }

    private JPanel buildWelcomePanel() {
        JPanel welcome = new JPanel(new BorderLayout());
        welcome.setOpaque(false);

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

        JLabel badge = new JLabel("Nova AI Assistant");
        badge.setOpaque(true);
        badge.setBackground(new Color(0xFFF2EA));
        badge.setForeground(UiPalette.ACCENT.darker());
        badge.setFont(new Font("Segoe UI", Font.BOLD, 12));
        badge.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        badge.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = new JLabel("How can I help you today?");
        title.setFont(UiPalette.FONT_TITLE);
        title.setForeground(UiPalette.TEXT);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Ask a question or continue any existing conversation.");
        subtitle.setFont(UiPalette.FONT_SUBTITLE);
        subtitle.setForeground(UiPalette.TEXT_SECONDARY);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        center.add(Box.createVerticalGlue());
        center.add(badge);
        center.add(Box.createVerticalStrut(18));
        center.add(title);
        center.add(Box.createVerticalStrut(14));
        center.add(subtitle);
        center.add(Box.createVerticalGlue());

        welcome.add(center, BorderLayout.CENTER);
        return welcome;
    }

    public void addMessage(Message message) {
        if (messageCount == 0) {
            cardLayout.show(statePanel, "chat");
        }
        hideTyping();
        int width = chatContainer.getWidth();
        // set bubble max width to 600px (no fixed column width)
        int bubbleMax = 600;
        // Row uses BorderLayout so we can anchor block to left or right cleanly
        JPanel row = new JPanel(new BorderLayout()) {
            @Override
            public Dimension getMaximumSize() {
                Dimension pref = getPreferredSize();
                return new Dimension(Integer.MAX_VALUE, pref.height);
            }
        };
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));

        MessageBubble bubble = new MessageBubble(message);
        bubble.setMaxWidth(bubbleMax);
        bubble.setAlignmentY(Component.TOP_ALIGNMENT);

        // Block holds bubble and timestamp together (timestamp directly below bubble)
        JPanel block = new JPanel();
        block.setLayout(new BoxLayout(block, BoxLayout.Y_AXIS));
        block.setOpaque(false);

        // bubble then small spacing then timestamp (4px)
        if (message.isUserMessage()) {
            bubble.setAlignmentX(Component.RIGHT_ALIGNMENT);
            block.add(bubble);
            block.add(Box.createVerticalStrut(6));
            javax.swing.JLabel time = new javax.swing.JLabel(message.getTimestamp() == null ? "" : message.getTimestamp().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")));
            time.setFont(UiPalette.FONT_SMALL);
            time.setForeground(UiPalette.TEXT_SECONDARY);
            time.setAlignmentX(Component.RIGHT_ALIGNMENT);
            block.add(time);
            row.add(block, BorderLayout.EAST);
        } else {
            bubble.setAlignmentX(Component.LEFT_ALIGNMENT);
            block.add(bubble);
            block.add(Box.createVerticalStrut(6));
            javax.swing.JLabel time = new javax.swing.JLabel(message.getTimestamp() == null ? "" : message.getTimestamp().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")));
            time.setFont(UiPalette.FONT_SMALL);
            time.setForeground(UiPalette.TEXT_SECONDARY);
            time.setAlignmentX(Component.LEFT_ALIGNMENT);
            block.add(time);
            row.add(block, BorderLayout.WEST);
        }

        messagesContainer.add(row);
        messagesContainer.add(Box.createVerticalStrut(4));
        messageCount++;
        messagesContainer.revalidate();
        messagesContainer.repaint();
        scrollToBottom();
    }

    public void showTyping() {
        cardLayout.show(statePanel, "chat");
        thinkingIndicator.start();
        typingRow.setVisible(true);
        typingRow.revalidate();
        scrollToBottom();
    }

    public void hideTyping() {
        thinkingIndicator.stop();
        typingRow.setVisible(false);
        typingRow.removeAll();
        typingRow.add(thinkingIndicator);
    }

    public void clearMessages() {
        messagesContainer.removeAll();
        messagesContainer.revalidate();
        messagesContainer.repaint();
        messageCount = 0;
        hideTyping();
        cardLayout.show(statePanel, "welcome");
    }

    public void scrollToBottom() {
        SwingUtilities.invokeLater(() -> {
            JScrollPane parent = scrollPane;
            parent.getVerticalScrollBar().setValue(parent.getVerticalScrollBar().getMaximum());
            scrollButton.hideButton();
        });
    }

    private void updateScrollButtonState() {
        SwingUtilities.invokeLater(() -> {
            int max = scrollPane.getVerticalScrollBar().getMaximum();
            int value = scrollPane.getVerticalScrollBar().getValue();
            boolean atBottom = value + scrollPane.getViewport().getHeight() >= max - 4;
            boolean shouldShow = messageCount > 0 && !atBottom;
            if (shouldShow) {
                scrollButton.showButton();
            } else {
                scrollButton.hideButton();
            }
        });
    }

    public void applyTheme(ThemeManager.AppTheme theme) {
        Color bg = ThemeManager.background(theme);
        setBackground(bg);
        messagesContainer.setBackground(bg);
        scrollPane.getViewport().setBackground(bg);
        thinkingIndicator.setBackground(bg);
        repaint();
    }

    // keep layout responsive; avoid forcing a centered column or fixed widths
    {
        chatContainer.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                // simply revalidate viewport so messages expand to available width
                viewportWrapper.revalidate();
            }
        });
    }

    private class ScrollToBottomButton extends JPanel {
        private float alpha = 0f;
        private boolean active;
        private final Timer fadeTimer;

        ScrollToBottomButton() {
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(40, 40));
            setVisible(false);
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    scrollToBottom();
                }
            });

            Timer timer = new Timer(20, null);
            fadeTimer = timer;
            timer.addActionListener(e -> {
                if (active && alpha < 1f) {
                    alpha = Math.min(1f, alpha + 0.12f);
                    repaint();
                } else if (!active && alpha > 0f) {
                    alpha = Math.max(0f, alpha - 0.12f);
                    repaint();
                } else {
                    if (!active) {
                        super.setVisible(false);
                    }
                    fadeTimer.stop();
                }
            });
        }

        void showButton() {
            if (isVisible() && alpha >= 1f) {
                return;
            }
            active = true;
            super.setVisible(true);
            if (!fadeTimer.isRunning()) {
                fadeTimer.start();
            }
        }

        void hideButton() {
            active = false;
            if (!fadeTimer.isRunning()) {
                fadeTimer.start();
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (alpha <= 0f) {
                return;
            }
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int size = getWidth();
            Color fill = new Color(255, 255, 255, (int) (230 * alpha));
            g2.setColor(fill);
            g2.fillOval(0, 0, size, size);
            g2.setColor(new Color(0, 0, 0, (int) (12 * alpha)));
            g2.drawOval(1, 1, size - 2, size - 2);
            g2.setColor(new Color(0x1F2937));
            g2.setStroke(new java.awt.BasicStroke(2.5f, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
            int arrowX = size / 2;
            int arrowY = size / 2 + 1;
            g2.drawLine(arrowX - 6, arrowY - 3, arrowX, arrowY + 2);
            g2.drawLine(arrowX, arrowY + 2, arrowX + 6, arrowY - 3);
            g2.drawLine(arrowX - 5, arrowY + 1, arrowX + 5, arrowY + 1);
            g2.dispose();
        }
    }

    private class ThinkingIndicator extends JPanel {
        private int frame;
        private final Timer blinkTimer;

        ThinkingIndicator() {
            setOpaque(false);
            setPreferredSize(new Dimension(120, 26));
            blinkTimer = new Timer(300, e -> {
                frame = (frame + 1) % 4;
                repaint();
            });
        }

        public void start() {
            if (!blinkTimer.isRunning()) {
                frame = 0;
                blinkTimer.start();
            }
            setVisible(true);
        }

        public void stop() {
            blinkTimer.stop();
            setVisible(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int dotSize = 8;
            int spacing = 14;
            int totalWidth = dotSize * 3 + spacing * 2;
            int startX = (getWidth() - totalWidth) / 2;
            int y = getHeight() / 2 - dotSize / 2;
            for (int i = 0; i < 3; i++) {
                int alpha = (frame == i) ? 220 : 100;
                g2.setColor(new Color(31, 41, 55, alpha));
                g2.fillOval(startX + i * (dotSize + spacing), y, dotSize, dotSize);
            }
            g2.dispose();
        }
    }
}
