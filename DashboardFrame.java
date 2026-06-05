import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class DashboardFrame extends JFrame {
    private static final Color BACKGROUND = UiPalette.BG;
    private static final Color SIDEBAR_BG = new Color(0xF1EFEB);
    private static final Color CARD_BG = UiPalette.CARD;
    private static final Color CARD_SHADOW = new Color(0, 0, 0, 18);
    private static final Color ACCENT = UiPalette.ACCENT;
    private static final Color SIDEBAR_ACCENT = new Color(0xE89B7A);
    private static final Color TEXT = UiPalette.TEXT;
    private static final Color SUBTEXT = UiPalette.TEXT_SECONDARY;
    private static final int SIDEBAR_WIDTH = 260;

    private final List<Conversation> conversations = new ArrayList<>();
    private List<Conversation> visibleConversations = new ArrayList<>();
    private final JTextField searchField = new RoundedTextField();
    private final JTextField inputField = new RoundedTextField();
    private static final String SEARCH_PLACEHOLDER = "Search conversations...";
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("h:mm a");

    private final JPanel cardsPanel = new JPanel();
    private final JLabel conversationTitleLabel = new JLabel("New Chat");
    private final JLabel statusLabel = new JLabel(" ");
    private final ChatPanel chatPanel = new ChatPanel();
    private Conversation activeConversation;

    public DashboardFrame() {
        setTitle("Nova AI Assistant PRO - Dashboard");
        setSize(1080, 720);
        setMinimumSize(new Dimension(960, 640));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BACKGROUND);

        add(buildSidebar(), BorderLayout.WEST);
        add(buildMainArea(), BorderLayout.CENTER);

        createNewConversation();
    }

    private JPanel buildSidebar() {
        RoundedPanel sidebar = new RoundedPanel(new BorderLayout(12, 12), 20, SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(SIDEBAR_WIDTH, 0));
        sidebar.setBorder(BorderFactory.createEmptyBorder(16, 12, 16, 12));

        JPanel userRow = new JPanel(new BorderLayout(12, 0));
        userRow.setOpaque(false);

        // Avatar rendering as circular panel with initials
        AvatarLabel avatar = new AvatarLabel("GU", SIDEBAR_ACCENT);
        avatar.setPreferredSize(new Dimension(48, 48));

        JPanel userInfo = new JPanel();
        userInfo.setOpaque(false);
        userInfo.setLayout(new BoxLayout(userInfo, BoxLayout.Y_AXIS));
        JLabel usernameLabel = new JLabel("SA sath");
        usernameLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        usernameLabel.setForeground(TEXT);
        usernameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        userInfo.add(usernameLabel);

        AccountMenuButton accountButton = new AccountMenuButton();
        accountButton.setText("Profile ▼");

        userRow.add(avatar, BorderLayout.WEST);
        userRow.add(userInfo, BorderLayout.CENTER);
        userRow.add(accountButton, BorderLayout.EAST);

        RoundedButton newChatButton = createNewChatButton("New Chat");
        newChatButton.setPreferredSize(new Dimension(SIDEBAR_WIDTH - 36, 42));
        newChatButton.setMaximumSize(new Dimension(SIDEBAR_WIDTH - 36, 42));
        newChatButton.addActionListener(e -> createNewConversation());

        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        searchField.setColumns(1);
        searchField.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                filterConversations();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filterConversations();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                filterConversations();
            }
        });
        searchField.setToolTipText("Search conversations");

        RoundedPanel searchWrap = new RoundedPanel(new BorderLayout(), 16, Color.WHITE);
        searchWrap.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        searchField.setText(SEARCH_PLACEHOLDER);
        searchField.setColumns(12);
        searchField.setOpaque(false);
        searchField.setForeground(SUBTEXT);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        searchField.setBorder(null);
        searchField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                if (SEARCH_PLACEHOLDER.equals(searchField.getText())) {
                    searchField.setText("");
                    searchField.setForeground(TEXT);
                }
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                if (searchField.getText().trim().isEmpty()) {
                    searchField.setText(SEARCH_PLACEHOLDER);
                    searchField.setForeground(SUBTEXT);
                }
            }
        });
        JLabel searchIcon = new JLabel("🔍");
        searchIcon.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 15));
        searchIcon.setForeground(SUBTEXT);
        searchWrap.add(searchIcon, BorderLayout.WEST);
        searchWrap.add(searchField, BorderLayout.CENTER);

        cardsPanel.setLayout(new BoxLayout(cardsPanel, BoxLayout.Y_AXIS));
        cardsPanel.setOpaque(false);

        JScrollPane scroll = new JScrollPane(cardsPanel);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.getVerticalScrollBar().setUnitIncrement(18);

        JPanel sidebarTop = new JPanel();
        sidebarTop.setOpaque(false);
        sidebarTop.setLayout(new BoxLayout(sidebarTop, BoxLayout.Y_AXIS));
        sidebarTop.add(userRow);
        sidebarTop.add(Box.createVerticalStrut(14));
        // New Chat full width
        JPanel newChatHolder = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        newChatHolder.setOpaque(false);
        newChatHolder.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        newChatHolder.add(newChatButton);
        sidebarTop.add(newChatHolder);
        sidebarTop.add(Box.createVerticalStrut(12));
        sidebarTop.add(searchWrap);

        JPanel historyLabelPanel = new JPanel(new BorderLayout());
        historyLabelPanel.setOpaque(false);
        JLabel historyLabel = new JLabel("Chats");
        historyLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        historyLabel.setForeground(TEXT);
        historyLabelPanel.add(historyLabel, BorderLayout.WEST);

        JPanel historySection = new JPanel(new BorderLayout(0, 8));
        historySection.setOpaque(false);
        historySection.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        historySection.add(historyLabelPanel, BorderLayout.NORTH);
        historySection.add(scroll, BorderLayout.CENTER);

        sidebar.add(sidebarTop, BorderLayout.NORTH);
        sidebar.add(historySection, BorderLayout.CENTER);
        return sidebar;
    }

    private JPanel buildMainArea() {
        JPanel main = new JPanel(new BorderLayout(12, 12));
        main.setOpaque(true);
        main.setBackground(BACKGROUND);
        main.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        RoundedPanel content = new RoundedPanel(new BorderLayout(), 26, CARD_BG);
        content.setOpaque(false);
        content.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        conversationTitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        conversationTitleLabel.setForeground(TEXT);
        JLabel headerSubtitle = new JLabel("AI-powered chat for your questions.");
        headerSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        headerSubtitle.setForeground(SUBTEXT);

        JPanel titleBlock = new JPanel();
        titleBlock.setOpaque(false);
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.add(conversationTitleLabel);
        titleBlock.add(Box.createVerticalStrut(4));
        titleBlock.add(headerSubtitle);

        header.add(titleBlock, BorderLayout.WEST);
        content.add(header, BorderLayout.NORTH);
        content.add(chatPanel, BorderLayout.CENTER);

        main.add(content, BorderLayout.CENTER);
        CenteredPanel inputHolder = new CenteredPanel();
        inputHolder.setOpaque(false);
        inputHolder.add(buildInputArea());
        main.add(inputHolder, BorderLayout.SOUTH);
        return main;
    }

    private class CenteredPanel extends JPanel {
        CenteredPanel() {
            super(new java.awt.BorderLayout());
            JPanel holder = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 0, 8));
            holder.setOpaque(false);
            add(holder, java.awt.BorderLayout.CENTER);
        }

        @Override
        public java.awt.Component add(java.awt.Component comp) {
            // add into inner flow layout holder
            java.awt.Component holder = getComponent(0);
            if (holder instanceof JPanel) {
                ((JPanel) holder).add(comp);
            }
            return comp;
        }
    }

    private JPanel buildInputArea() {
        RoundedPanel inputArea = new RoundedPanel(new BorderLayout(10, 0), 26, CARD_BG);
        inputArea.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        inputArea.setOpaque(false);

        RoundedPanel inputWrap = new RoundedPanel(new BorderLayout(10, 0), 22, new Color(0xFEF7F1));
        inputWrap.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        RoundedButton plusButton = createIconButton("+");
        plusButton.setPreferredSize(new Dimension(44, 44));
        plusButton.addActionListener(e -> createNewConversation());

        RoundedButton micButton = createIconButton("Mic");
        micButton.setPreferredSize(new Dimension(54, 44));
        micButton.addActionListener(e -> inputField.requestFocusInWindow());

        RoundedButton sendButton = createIconButton("Send");
        sendButton.setPreferredSize(new Dimension(64, 44));
        sendButton.addActionListener(this::sendMessage);

        inputField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        inputField.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        inputField.setOpaque(false);
        inputField.addActionListener(this::sendMessage);
        // simple placeholder behavior
        final String placeholder = "Ask anything";
        inputField.setText(placeholder);
        inputField.setForeground(SUBTEXT);
        inputField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                if (inputField.getText().equals(placeholder)) {
                    inputField.setText("");
                    inputField.setForeground(TEXT);
                }
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                if (inputField.getText().trim().isEmpty()) {
                    inputField.setText(placeholder);
                    inputField.setForeground(SUBTEXT);
                }
            }
        });

        JPanel leftControls = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftControls.setOpaque(false);
        leftControls.add(plusButton);
        leftControls.add(micButton);

        inputWrap.add(leftControls, BorderLayout.WEST);
        inputWrap.add(inputField, BorderLayout.CENTER);
        inputWrap.add(sendButton, BorderLayout.EAST);
        inputField.setPreferredSize(new Dimension(0, 46));

        // constrain input area width to max 800 and let FlowLayout in CenteredPanel center it
        inputArea.setPreferredSize(new Dimension(800, inputArea.getPreferredSize().height));
        inputArea.setMaximumSize(new Dimension(800, 200));

        statusLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        statusLabel.setForeground(SUBTEXT);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(8, 4, 0, 0));

        inputArea.add(inputWrap, BorderLayout.CENTER);
        inputArea.add(statusLabel, BorderLayout.SOUTH);
        return inputArea;
    }

    private RoundedButton createButton(String text) {
        RoundedButton button = new RoundedButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setForeground(TEXT);
        button.setBackground(Color.WHITE);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
        return button;
    }

    private RoundedButton createIconButton(String icon) {
        RoundedButton button = new RoundedButton(icon);
        button.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 18));
        button.setForeground(TEXT);
        button.setBackground(new Color(0xFFFBF7));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        return button;
    }

    private void sendMessage(ActionEvent e) {
        String text = inputField.getText().trim();
        if (text.isEmpty() || activeConversation == null) {
            return;
        }
        inputField.setText("");

        Message userMessage = new Message(activeConversation.id, "You", text, true);
        activeConversation.addMessage(userMessage);
        chatPanel.addMessage(userMessage);
        updateTitleFromFirstMessage(activeConversation, text);

        statusLabel.setText("Nova AI is typing...");
        chatPanel.showTyping();

        Timer timer = new Timer(700, event -> {
            Message reply = new Message(activeConversation.id, "Nova AI",
                    "Thanks for your message. I can help with Java, Python, AI, projects, and interview prep.", false);
            activeConversation.addMessage(reply);
            chatPanel.hideTyping();
            chatPanel.addMessage(reply);
            statusLabel.setText(" ");
            filterConversations();
        });
        timer.setRepeats(false);
        timer.start();
    }

    private void createNewConversation() {
        Conversation conversation = new Conversation("chat-" + UUID.randomUUID(), "New Chat");
        conversations.add(0, conversation);
        filterConversations();
        setActiveConversation(conversation);
    }

    private void setActiveConversation(Conversation conversation) {
        activeConversation = conversation;
        conversationTitleLabel.setText(conversation.title);
        chatPanel.clearMessages();
        for (Message message : conversation.messages) {
            chatPanel.addMessage(message);
        }
        filterConversations();
    }

    private void updateTitleFromFirstMessage(Conversation conversation, String firstText) {
        if ("New Chat".equals(conversation.title)) {
            conversation.title = firstText.length() > 28 ? firstText.substring(0, 28) + "..." : firstText;
            conversationTitleLabel.setText(conversation.title);
        }
    }

    private void renameConversation(String conversationId) {
        visibleConversations.stream()
                .filter(c -> c.id.equals(conversationId))
                .findFirst()
                .ifPresent(c -> {
                    String newTitle = JOptionPane.showInputDialog(this, "Rename conversation:", c.title);
                    if (newTitle != null && !newTitle.trim().isEmpty()) {
                        c.title = newTitle.trim();
                        if (c == activeConversation) {
                            conversationTitleLabel.setText(c.title);
                        }
                        filterConversations();
                    }
                });
    }

    private void toggleFavoriteConversation(String conversationId) {
        visibleConversations.stream()
                .filter(c -> c.id.equals(conversationId))
                .findFirst()
                .ifPresent(c -> {
                    c.setFavorite(!c.isFavorite());
                    filterConversations();
                });
    }

    private void deleteConversation(String conversationId) {
        visibleConversations.stream()
                .filter(c -> c.id.equals(conversationId))
                .findFirst()
                .ifPresent(c -> {
                    int choice = JOptionPane.showConfirmDialog(this, "Delete this conversation?", "Confirm Delete",
                            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                    if (choice == JOptionPane.YES_OPTION) {
                        conversations.remove(c);
                        if (c == activeConversation) {
                            activeConversation = null;
                            if (!conversations.isEmpty()) {
                                setActiveConversation(conversations.get(0));
                            } else {
                                conversationTitleLabel.setText("New Chat");
                                chatPanel.clearMessages();
                            }
                        }
                        filterConversations();
                    }
                });
    }

    private void filterConversations() {
        String query = searchField.getText() == null ? "" : searchField.getText().trim();
        if (SEARCH_PLACEHOLDER.equals(query)) {
            query = "";
        }
        String normalizedQuery = query.toLowerCase();
        visibleConversations = conversations.stream()
                .filter(c -> normalizedQuery.isEmpty() || c.title.toLowerCase().contains(normalizedQuery) || c.getPreview().toLowerCase().contains(normalizedQuery))
                .sorted(Comparator.comparing(Conversation::isFavorite).reversed()
                        .thenComparing(Conversation::getLastUpdated).reversed())
                .collect(Collectors.toList());

        cardsPanel.removeAll();
        if (visibleConversations.isEmpty()) {
            JLabel empty = new JLabel("No conversations found.");
            empty.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            empty.setForeground(SUBTEXT);
            empty.setBorder(BorderFactory.createEmptyBorder(16, 8, 16, 8));
            cardsPanel.add(empty);
        } else {
            ConversationCard.CardListener listener = new ConversationCard.CardListener() {
                @Override
                public void onSelect(String sessionId) {
                    conversations.stream()
                            .filter(c -> c.id.equals(sessionId))
                            .findFirst()
                            .ifPresent(DashboardFrame.this::setActiveConversation);
                }

                @Override
                public void onRename(String sessionId) {
                    renameConversation(sessionId);
                }

                @Override
                public void onFavorite(String sessionId) {
                    toggleFavoriteConversation(sessionId);
                }

                @Override
                public void onDelete(String sessionId) {
                    deleteConversation(sessionId);
                }
            };

            for (Conversation conversation : visibleConversations) {
                boolean selected = conversation == activeConversation;
                ConversationCard card = new ConversationCard(conversation.id, conversation.title,
                        conversation.isFavorite(), selected, listener);
                card.setPreview(conversation.getPreview());
                card.setTimestamp(conversation.getLastUpdated().format(TIME_FORMATTER));
                card.setAlignmentX(Component.LEFT_ALIGNMENT);
                cardsPanel.add(card);
                cardsPanel.add(Box.createVerticalStrut(10));
            }
        }
        cardsPanel.revalidate();
        cardsPanel.repaint();
    }

    private static class RoundedPanel extends JPanel {
        private final int radius;
        private final Color backgroundColor;

        public RoundedPanel(BorderLayout layout, int radius, Color backgroundColor) {
            super(layout);
            this.radius = radius;
            this.backgroundColor = backgroundColor;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int width = getWidth();
            int height = getHeight();
            g2.setColor(CARD_SHADOW);
            g2.fillRoundRect(4, 4, width - 8, height - 8, radius, radius);
            g2.setColor(backgroundColor);
            g2.fillRoundRect(0, 0, width - 8, height - 8, radius, radius);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class RoundedButton extends JButton {
        public RoundedButton(String text) {
            super(text);
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setBackground(new Color(0xFFFBF7));
            setForeground(TEXT);
            setFont(new Font("Segoe UI", Font.PLAIN, 16));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private RoundedButton createNewChatButton(String text) {
        RoundedButton b = new RoundedButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 14));
        b.setForeground(Color.WHITE);
        b.setBackground(SIDEBAR_ACCENT);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        b.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                b.setBackground(SIDEBAR_ACCENT.darker());
                b.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                b.setBackground(SIDEBAR_ACCENT);
                b.repaint();
            }
        });
        return b;
    }

    private static class AvatarLabel extends JLabel {
        private final String initials;
        private final Color bg;

        AvatarLabel(String initials, Color bg) {
            super(initials, JLabel.CENTER);
            this.initials = initials == null ? "U" : initials;
            this.bg = bg == null ? SIDEBAR_ACCENT : bg;
            setForeground(Color.WHITE);
            setFont(new Font("Segoe UI", Font.BOLD, 14));
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
            int size = Math.min(w, h);
            int x = (w - size) / 2;
            int y = (h - size) / 2;
            g2.setColor(bg);
            g2.fillOval(x, y, size, size);
            g2.setColor(getForeground());
            g2.setFont(getFont());
            Font fm = g2.getFont();
            int sw = g2.getFontMetrics(fm).stringWidth(getText());
            int sh = g2.getFontMetrics(fm).getAscent();
            g2.drawString(getText(), w / 2 - sw / 2, h / 2 + sh / 4);
            g2.dispose();
        }
    }

    private class MicButton extends RoundedButton {
        public MicButton() {
            super("");
            setBackground(Color.WHITE);
            setPreferredSize(new Dimension(44, 44));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
            g2.setColor(new Color(0xFF9F00));
            int centerX = getWidth() / 2;
            int centerY = getHeight() / 2;
            g2.fillRoundRect(centerX - 5, centerY - 9, 10, 16, 10, 10);
            g2.fillOval(centerX - 7, centerY - 16, 14, 14);
            g2.fillRect(centerX - 3, centerY + 6, 6, 6);
            g2.fillRect(centerX - 8, centerY + 12, 16, 4);
            g2.dispose();
        }
    }

    private class SendButton extends RoundedButton {
        public SendButton() {
            super("");
            setBackground(ACCENT);
            setForeground(Color.WHITE);
            setPreferredSize(new Dimension(48, 48));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getForeground());
            int x = getWidth() / 2;
            int y = getHeight() / 2;
            int size = 10;
            int[] xs = {x - size / 2, x - size / 2, x + size / 2};
            int[] ys = {y - size / 2, y + size / 2, y};
            g2.fillPolygon(xs, ys, 3);
            g2.dispose();
        }
    }

    private class AccountMenuButton extends RoundedButton {
        private boolean hovered;

        public AccountMenuButton() {
            super("Account ▼");
            setPreferredSize(new Dimension(120, 40));
            setBackground(Color.WHITE);
            setForeground(TEXT);
            setFont(new Font("Segoe UI", Font.PLAIN, 13));
            setToolTipText("Account Options");
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addActionListener(e -> showProfileMenu(AccountMenuButton.this));
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

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(hovered ? new Color(0xF4F2EF) : getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private void showProfileMenu(AccountMenuButton source) {
        JPopupMenu menu = new JPopupMenu();
        ThemeManager.stylePopupMenu(menu);
        ModernMenuItem profileItem = new ModernMenuItem("Profile", new EmojiIcon("👤"));
        ModernMenuItem settingsItem = new ModernMenuItem("Settings", new EmojiIcon("⚙"));
        ModernMenuItem logoutItem = new ModernMenuItem("Logout", new EmojiIcon("🚪"));
        profileItem.addActionListener(e -> JOptionPane.showMessageDialog(this, "Profile details are not editable in this demo."));
        settingsItem.addActionListener(e -> JOptionPane.showMessageDialog(this, "Settings are not available in this demo."));
        logoutItem.addActionListener(e -> dispose());
        menu.add(profileItem);
        menu.add(settingsItem);
        menu.addSeparator();
        menu.add(logoutItem);
        menu.show(source, 0, source.getHeight());
    }

    private static class RoundedTextField extends JTextField {
        private static final int FIELD_RADIUS = 20;

        public RoundedTextField() {
            setOpaque(false);
            setBorder(new EmptyBorder(10, 14, 10, 14));
            setFont(new Font("Segoe UI", Font.PLAIN, 14));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(255, 255, 255, 230));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), FIELD_RADIUS, FIELD_RADIUS);
            g2.setColor(new Color(0xD9D5D0));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, FIELD_RADIUS, FIELD_RADIUS);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class Conversation {
        private final String id;
        private String title;
        private boolean favorite;
        private final List<Message> messages = new ArrayList<>();
        private LocalDateTime lastUpdated;
        private String preview;

        private Conversation(String id, String title) {
            this.id = id;
            this.title = title;
            this.lastUpdated = LocalDateTime.now();
            this.preview = "No messages yet.";
        }

        private void addMessage(Message message) {
            messages.add(message);
            this.lastUpdated = LocalDateTime.now();
            String content = message.getContent();
            this.preview = content.length() > 32 ? content.substring(0, 32) + "..." : content;
        }

        private boolean isFavorite() {
            return favorite;
        }

        private void setFavorite(boolean favorite) {
            this.favorite = favorite;
        }

        private LocalDateTime getLastUpdated() {
            return lastUpdated;
        }

        private String getPreview() {
            return preview;
        }
    }
}
