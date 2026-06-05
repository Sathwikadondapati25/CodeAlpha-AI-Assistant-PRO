import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class ModernSidebar extends JPanel {
    public interface SidebarActions {
        void onNewChat();

        void onSelectSession(String sessionId);

        void onRenameSession(String sessionId);

        void onFavoriteSession(String sessionId);

        void onDeleteSession(String sessionId);

        void onPinSession(String sessionId);

        void onLogout();

        void onSearch(String query);
    }

    private final SidebarActions actions;
    private JLabel usernameLabel;
    private AvatarPanel avatarPanel;
    private JTextField searchField;
    private JPanel searchFieldContainer;
    private JPanel conversationList;
    private final Map<String, ConversationCard> cards = new HashMap<>();
    private String activeSessionId;

    public ModernSidebar(SidebarActions actions) {
        this.actions = actions;
        setLayout(new BorderLayout(0, 12));
        setPreferredSize(new Dimension(UiPalette.SIDEBAR_WIDTH, 0));
        setBackground(UiPalette.SIDEBAR);
        setBorder(BorderFactory.createEmptyBorder(16, 14, 16, 14));

        add(buildProfileHeader(), BorderLayout.NORTH);
        add(buildScrollSection(), BorderLayout.CENTER);
    }

    private JPanel buildProfileHeader() {
        JPanel column = new JPanel();
        column.setOpaque(false);
        column.setLayout(new BoxLayout(column, BoxLayout.Y_AXIS));

        column.add(buildProfileRow());
        column.add(Box.createVerticalStrut(14));
        column.add(buildButtonsRow());

        searchFieldContainer = new JPanel();
        searchFieldContainer.setOpaque(false);
        searchFieldContainer.setLayout(new BoxLayout(searchFieldContainer, BoxLayout.Y_AXIS));
        searchFieldContainer.add(Box.createVerticalStrut(8));
        searchFieldContainer.add(buildSearchFieldWrap());
        searchFieldContainer.setVisible(false); // Hidden by default

        column.add(searchFieldContainer);

        return column;
    }

    private JPanel buildProfileRow() {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);
        avatarPanel = new AvatarPanel("Guest");
        left.add(avatarPanel);
        usernameLabel = new JLabel("Guest");
        usernameLabel.setFont(UiPalette.FONT_USERNAME);
        usernameLabel.setForeground(UiPalette.TEXT);
        left.add(usernameLabel);

        IconButton logout = new IconButton("") {
            private boolean isHovered = false;
            
            {
                addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseEntered(java.awt.event.MouseEvent e) {
                        isHovered = true;
                        repaint();
                    }
                    @Override
                    public void mouseExited(java.awt.event.MouseEvent e) {
                        isHovered = false;
                        repaint();
                    }
                });
            }
            
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                Color bg = getModel().isPressed() ? new Color(0xE5E7EB) : (isHovered ? new Color(0xF3F4F6) : Color.WHITE);
                int w = getWidth();
                int h = getHeight();
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, w - 1, h - 1, 10, 10);
                
                int cx = w / 2;
                int cy = h / 2;
                g2.setColor(UiPalette.TEXT);
                g2.setStroke(new java.awt.BasicStroke(2.0f, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
                // Draw door frame
                g2.drawRect(cx - 6, cy - 6, 8, 10);
                // Draw door handle
                g2.fillOval(cx + 1, cy, 2, 2);
                // Draw arrow exiting
                int[] xPts = {cx + 4, cx + 8, cx + 6};
                int[] yPts = {cy - 2, cy - 2, cy - 4};
                g2.drawPolyline(xPts, yPts, 3);
                g2.drawLine(cx + 4, cy - 2, cx + 8, cy - 2);
                g2.dispose();
            }
        };
        logout.setToolTipText("Logout");
        logout.setPreferredSize(new Dimension(36, 36));
        logout.addActionListener(e -> actions.onLogout());

        row.add(left, BorderLayout.CENTER);
        row.add(logout, BorderLayout.EAST);
        return row;
    }

    private JPanel buildButtonsRow() {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        NewChatButton newChatBtn = new NewChatButton();
        newChatBtn.setPreferredSize(new Dimension(170, 44));
        newChatBtn.addActionListener(e -> actions.onNewChat());

        SidebarSearchButton searchBtn = new SidebarSearchButton();
        searchBtn.setPreferredSize(new Dimension(44, 44));

        searchBtn.addActionListener(e -> {
            searchFieldContainer.setVisible(!searchFieldContainer.isVisible());
            if (searchFieldContainer.isVisible()) {
                searchField.requestFocusInWindow();
            } else {
                searchField.setText("");
                actions.onSearch("");
            }
            revalidate();
            repaint();
        });

        row.add(newChatBtn, BorderLayout.CENTER);
        row.add(searchBtn, BorderLayout.EAST);
        return row;
    }

    private JPanel buildSearchFieldWrap() {
        RoundedPanel searchFieldWrap = new RoundedPanel(UiPalette.CARD, 14);
        searchFieldWrap.setBorderColor(UiPalette.BORDER, 1);
        searchFieldWrap.setLayout(new BorderLayout());
        searchFieldWrap.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        JPanel leftIconPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UiPalette.TEXT_SECONDARY);
                g2.setStroke(new java.awt.BasicStroke(1.8f, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
                int w = getWidth();
                int h = getHeight();
                int cx = w / 2;
                int cy = h / 2;
                int size = 8;
                int ox = cx - 5;
                int oy = cy - 5;
                g2.drawOval(ox, oy, size, size);
                g2.drawLine(ox + 6, oy + 6, ox + 10, oy + 10);
                g2.dispose();
            }
        };
        leftIconPanel.setOpaque(false);
        leftIconPanel.setPreferredSize(new Dimension(32, 44));

        searchField = new JTextField();
        searchField.setFont(UiPalette.FONT_SMALL);
        searchField.setForeground(UiPalette.TEXT);
        searchField.setBackground(UiPalette.CARD);
        searchField.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 12));
        searchField.putClientProperty("JTextField.placeholderText", "Search chats");

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { actions.onSearch(searchField.getText()); }
            @Override
            public void removeUpdate(DocumentEvent e) { actions.onSearch(searchField.getText()); }
            @Override
            public void changedUpdate(DocumentEvent e) { actions.onSearch(searchField.getText()); }
        });


        searchField.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ESCAPE) {
                    searchField.setText("");
                    actions.onSearch("");
                    searchFieldContainer.setVisible(false);
                    revalidate();
                    repaint();
                }
            }
        });

        searchFieldWrap.add(leftIconPanel, BorderLayout.WEST);
        searchFieldWrap.add(searchField, BorderLayout.CENTER);
        return searchFieldWrap;
    }

    static class NewChatButton extends JButton {
        private boolean hovered = false;
        private final int radius = 14;

        public NewChatButton() {
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));

            addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    hovered = true;
                    repaint();
                }
                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    hovered = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int arc = radius * 2;

            // Background color (dynamic hover)
            Color bg = getModel().isPressed() ? new Color(0xE5E7EB) : (hovered ? new Color(0xF3F4F6) : Color.WHITE);
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, w - 1, h - 1, arc, arc);

            // Border
            g2.setColor(UiPalette.BORDER);
            g2.setStroke(new java.awt.BasicStroke(1.0f));
            g2.drawRoundRect(0, 0, w - 1, h - 1, arc, arc);

            // Plus Icon (crisp and peach-colored)
            int cy = h / 2;
            int cx = 20;
            g2.setColor(UiPalette.ACCENT);
            g2.setStroke(new java.awt.BasicStroke(2.2f, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
            g2.drawLine(cx, cy - 6, cx, cy + 6);
            g2.drawLine(cx - 6, cy, cx + 6, cy);

            // Text
            g2.setColor(UiPalette.TEXT);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
            java.awt.FontMetrics fm = g2.getFontMetrics();
            int textX = cx + 14;
            int textY = (h + fm.getAscent() - fm.getDescent()) / 2;
            g2.drawString("New Chat", textX, textY);

            g2.dispose();
        }
    }

    static class SidebarSearchButton extends JButton {
        private boolean hovered = false;
        private final int radius = 14;

        public SidebarSearchButton() {
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));

            addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    hovered = true;
                    repaint();
                }
                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    hovered = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int arc = radius * 2;

            // Background color (dynamic hover)
            Color bg = getModel().isPressed() ? new Color(0xE5E7EB) : (hovered ? new Color(0xF3F4F6) : Color.WHITE);
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, w - 1, h - 1, arc, arc);

            // Border
            g2.setColor(UiPalette.BORDER);
            g2.setStroke(new java.awt.BasicStroke(1.0f));
            g2.drawRoundRect(0, 0, w - 1, h - 1, arc, arc);

            // Magnifying Glass Search Icon
            int cx = w / 2;
            int cy = h / 2;
            g2.setColor(UiPalette.TEXT);
            g2.setStroke(new java.awt.BasicStroke(2.0f, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
            
            int size = 9;
            int ox = cx - 6;
            int oy = cy - 6;
            g2.drawOval(ox, oy, size, size);
            g2.drawLine(ox + 7, oy + 7, ox + 12, oy + 12);

            g2.dispose();
        }
    }

    private JScrollPane buildScrollSection() {
        conversationList = new JPanel();
        conversationList.setLayout(new BoxLayout(conversationList, BoxLayout.Y_AXIS));
        conversationList.setBackground(UiPalette.SIDEBAR);
        conversationList.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));

        JLabel historyLabel = new JLabel("Conversations");
        historyLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        historyLabel.setForeground(UiPalette.TEXT_SECONDARY);
        historyLabel.setBorder(BorderFactory.createEmptyBorder(4, 4, 8, 4));
        historyLabel.setAlignmentX(LEFT_ALIGNMENT);

        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBackground(UiPalette.SIDEBAR);
        container.add(historyLabel);
        container.add(conversationList);

        JScrollPane scroll = new JScrollPane(container);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(UiPalette.SIDEBAR);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    public void setUsername(String name) {
        String display = name == null || name.isEmpty() ? "Guest" : name;
        usernameLabel.setText(display);
        avatarPanel.setInitials(display);
    }

    public String getSearchQuery() {
        return searchField.getText();
    }

    public void refreshSessions(List<SessionView> sessions, String activeId) {
        this.activeSessionId = activeId;
        conversationList.removeAll();
        cards.clear();

        ConversationCard.CardListener listener = new ConversationCard.CardListener() {
            @Override
            public void onSelect(String sessionId) {
                actions.onSelectSession(sessionId);
            }

            @Override
            public void onRename(String sessionId) {
                actions.onRenameSession(sessionId);
            }

            @Override
            public void onFavorite(String sessionId) {
                actions.onFavoriteSession(sessionId);
            }

            @Override
            public void onDelete(String sessionId) {
                actions.onDeleteSession(sessionId);
            }

            @Override
            public void onPin(String sessionId) {
                actions.onPinSession(sessionId);
            }
        };

        for (SessionView view : sessions) {
            boolean active = view.id.equals(activeId);
            ConversationCard card = new ConversationCard(view.id, view.title, view.favorite, view.pinned, active, listener);
            card.setAlignmentX(LEFT_ALIGNMENT);
            cards.put(view.id, card);
            conversationList.add(card);
            conversationList.add(Box.createVerticalStrut(6));
        }

        conversationList.revalidate();
        conversationList.repaint();
    }

    public static final class SessionView {
        public final String id;
        public final String title;
        public final boolean favorite;
        public final boolean pinned;

        public SessionView(String id, String title, boolean favorite, boolean pinned) {
            this.id = id;
            this.title = title;
            this.favorite = favorite;
            this.pinned = pinned;
        }
    }

    /** Circular avatar with user initials. */
    static class AvatarPanel extends JPanel {
        private String initials = "?";

        AvatarPanel(String name) {
            setOpaque(false);
            setPreferredSize(new Dimension(36, 36));
            setMinimumSize(new Dimension(36, 36));
            setMaximumSize(new Dimension(36, 36));
            setInitials(name);
        }

        void setInitials(String name) {
            if (name == null || name.trim().isEmpty()) {
                initials = "?";
            } else {
                String[] parts = name.trim().split("\\s+");
                if (parts.length >= 2) {
                    initials = ("" + parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase();
                } else {
                    initials = name.substring(0, Math.min(2, name.length())).toUpperCase();
                }
            }
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(UiPalette.ACCENT);
            g2.fillOval(0, 0, getWidth() - 1, getHeight() - 1);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
            java.awt.FontMetrics fm = g2.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(initials)) / 2;
            int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
            g2.drawString(initials, x, y);
            g2.dispose();
        }
    }
}
