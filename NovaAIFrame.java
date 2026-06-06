import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

public class NovaAIFrame extends JFrame {
    private static final String APP_VERSION = "2.0.0";

    private final FileStore fileStore;
    private final AIEngine aiEngine;
    private final Map<String, List<Message>> sessionMessages;
    private final Map<String, ChatSession> chatSessions;

    private UserProfile profile;
    private String activeSessionId;

    private ModernSidebar modernSidebar;
    private ChatPanel chatPanel;
    private ChatInputBar chatInputBar;
    private JPanel mainPanel;
    private List<String> visibleSessionIds = new ArrayList<>();

    public String getProfileName() {
        return profile != null ? profile.getName() : "";
    }

    private final String currentUser;

public NovaAIFrame(String username) {
    String lowerUsername = username != null ? username.toLowerCase() : "";
    this.currentUser = lowerUsername;

    String userFolder = "data/" + lowerUsername;

    new File(userFolder).mkdirs();

    fileStore = new FileStore(
            userFolder + "/chat_history.txt",
            userFolder + "/profile.properties"
    );

    aiEngine = new AIEngine();
    sessionMessages = new LinkedHashMap<>();
    chatSessions = new LinkedHashMap<>();
    profile = fileStore.loadUserProfile();

    
    initializeUI();
    loadSessions();
    applyTheme(profile.getTheme());
    ensureWelcomeMessage();
}

public String getCurrentUser() {
    return currentUser;
}



    public ThemeManager.AppTheme getCurrentTheme() {
        return profile.getTheme();
    }

    private void initializeUI() {
        setTitle("Nova AI Assistant PRO");
        setSize(1180, 760);
        setMinimumSize(new Dimension(980, 640));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setLayout(new BorderLayout());
        getContentPane().setBackground(UiPalette.BG);

        setJMenuBar(buildMenuBar());
        buildSidebar();
        buildMainArea();
    }

    private JMenuBar buildMenuBar() {
        JMenuBar bar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        ModernMenuItem exportChatFormatted = new ModernMenuItem("Export Chat (PDF)", new EmojiIcon("📄"));
        exportChatFormatted.addActionListener(e -> exportChatsFormatted());
        ModernMenuItem exportChatRaw = new ModernMenuItem("Export Chat Backup", new EmojiIcon("💾"));
        exportChatRaw.addActionListener(e -> exportChatsRaw());
        ModernMenuItem exportNotesItem = new ModernMenuItem("Export Notes", new EmojiIcon("📝"));
        exportNotesItem.addActionListener(e -> NotesManager.exportNotes(this));
        fileMenu.add(exportChatFormatted);
        fileMenu.add(exportChatRaw);
        fileMenu.add(new ModernSeparator());
        fileMenu.add(exportNotesItem);

        JMenu viewMenu = new JMenu("View");
        ModernMenuItem statsItem = new ModernMenuItem("Statistics Dashboard", new EmojiIcon("📊"));
        statsItem.addActionListener(e -> StatisticsManager.showDashboard(this, profile.getTheme()));
        
        JMenu themeMenu = new JMenu("Theme Switcher");
        ButtonGroup themeGroup = new ButtonGroup();
        for (ThemeManager.AppTheme theme : ThemeManager.AppTheme.values()) {
            JRadioButtonMenuItem item = new JRadioButtonMenuItem(theme.getDisplayName());
            item.addActionListener(e -> switchTheme(theme));
            themeGroup.add(item);
            themeMenu.add(item);
            if (theme == profile.getTheme()) {
                item.setSelected(true);
            }
        }
        viewMenu.add(statsItem);
        viewMenu.add(new ModernSeparator());
        viewMenu.add(themeMenu);

        JMenu toolsMenu = new JMenu("Tools");
        ModernMenuItem resumeItem = new ModernMenuItem("Resume Assistant", new EmojiIcon("📄"));
        resumeItem.addActionListener(e -> ResumeAssistant.showAssistant(this));
        ModernMenuItem interviewItem = new ModernMenuItem("Interview Simulator", new EmojiIcon("🎤"));
        interviewItem.addActionListener(e -> InterviewSimulator.showSimulator(this));
        ModernMenuItem notesItem = new ModernMenuItem("Notes Manager", new EmojiIcon("📝"));
        notesItem.addActionListener(e -> NotesManager.showManager(this));
        ModernMenuItem plannerItem = new ModernMenuItem("Study Planner", new EmojiIcon("📅"));
        plannerItem.addActionListener(e -> StudyPlanner.showPlanner(this));
        toolsMenu.add(resumeItem);
        toolsMenu.add(interviewItem);
        toolsMenu.add(notesItem);
        toolsMenu.add(plannerItem);

        JMenu settingsMenu = new JMenu("Settings");
        ModernMenuItem userSettingsItem = new ModernMenuItem("User Settings", new EmojiIcon("⚙"));
        userSettingsItem.addActionListener(e -> showSettingsDialog());
        ModernMenuItem logoutItem = new ModernMenuItem("Logout", new EmojiIcon("🚪"));
        logoutItem.addActionListener(e -> handleLogout());
        settingsMenu.add(userSettingsItem);
        settingsMenu.add(new ModernSeparator());
        settingsMenu.add(logoutItem);

        JMenu helpMenu = new JMenu("Help");
        ModernMenuItem aboutItem = new ModernMenuItem("About", new EmojiIcon("ℹ"));
        aboutItem.addActionListener(e -> showAboutDialog());
        ModernMenuItem shortcutsItem = new ModernMenuItem("Keyboard Shortcuts", new EmojiIcon("⌨"));
        shortcutsItem.addActionListener(e -> showShortcutsDialog());
        ModernMenuItem versionItem = new ModernMenuItem("Version Info", new EmojiIcon("🏷"));
        versionItem.addActionListener(e -> showVersionInfoDialog());
        helpMenu.add(aboutItem);
        helpMenu.add(shortcutsItem);
        helpMenu.add(versionItem);

        bar.add(fileMenu);
        bar.add(viewMenu);
        bar.add(toolsMenu);
        bar.add(settingsMenu);
        bar.add(helpMenu);
        return bar;
    }

    private void buildSidebar() {
        modernSidebar = new ModernSidebar(new ModernSidebar.SidebarActions() {
            @Override
            public void onNewChat() {
                createNewSession(true);
            }

            @Override
            public void onSelectSession(String sessionId) {
                switchSession(sessionId);
            }

            @Override
            public void onRenameSession(String sessionId) {
                activeSessionId = sessionId;
                renameSelectedConversation();
            }

            @Override
            public void onFavoriteSession(String sessionId) {
                activeSessionId = sessionId;
                toggleFavoriteSelectedConversation();
            }

            @Override
            public void onDeleteSession(String sessionId) {
                activeSessionId = sessionId;
                deleteSelectedConversation();
            }

            @Override
            public void onPinSession(String sessionId) {
                activeSessionId = sessionId;
                togglePinSelectedConversation();
            }

            @Override
            public void onLogout() {
                handleLogout();
            }

            @Override
            public void onSearch(String query) {
                refreshSessionList(query);
            }
        });
        modernSidebar.setUsername(profile.getName());
        refreshSessionList("");
        add(modernSidebar, BorderLayout.WEST);
    }

    private void buildMainArea() {
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setOpaque(false);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        chatPanel = new ChatPanel();
        mainPanel.add(chatPanel, BorderLayout.CENTER);

        chatInputBar = new ChatInputBar(new ChatInputBar.InputListener() {
            @Override
            public void onSend(String text) {
                sendUserMessage(text);
            }

            @Override
            public void onNewChat() {
                createNewSession(true);
            }

            @Override
            public void onAttachFile(File file) {
                appendMessage(profile.getName(),
                        "[Attached file: " + file.getName() + "]",
                        true);
            }

            @Override
            public void onNewNote() {
                NotesManager.showManager(NovaAIFrame.this);
            }
        });
        mainPanel.add(chatInputBar, BorderLayout.SOUTH);
        add(mainPanel, BorderLayout.CENTER);
    }

    private void sendUserMessage(String text) {
        if (text == null || text.trim().isEmpty()) {
            return;
        }
        appendMessage(profile.getName(), text.trim(), true);
        showTypingIndicator();

        Timer timer = new Timer(700, e -> {
            hideTypingIndicator();
            String response = aiEngine.respond(text.trim());
            appendMessage("Nova AI", response, false);
            updateSessionTitle(activeSessionId);
        });
        timer.setRepeats(false);
        timer.start();
    }

    private void showTypingIndicator() {
        chatPanel.showTyping();
        chatInputBar.setTyping(true);
    }

    private void hideTypingIndicator() {
        chatPanel.hideTyping();
        chatInputBar.setTyping(false);
    }

    private void appendMessage(String sender, String content, boolean userMessage) {
        Message message = new Message(activeSessionId, sender, content, LocalDateTime.now(), userMessage);
        sessionMessages.computeIfAbsent(activeSessionId, key -> new ArrayList<>()).add(message);
        ChatSession session = chatSessions.get(activeSessionId);
        if (session != null) {
            session.touch(message.getTimestamp());
        }
        chatPanel.addMessage(message);
        fileStore.appendMessage(message);
    }

    private void switchSession(String sessionId) {
        activeSessionId = sessionId;
        refreshSessionList(modernSidebar.getSearchQuery());
        renderActiveSession();
    }

    private void renderActiveSession() {
        chatPanel.clearMessages();
        List<Message> messages = sessionMessages.getOrDefault(activeSessionId, new ArrayList<>());
        for (Message message : messages) {
            chatPanel.addMessage(message);
        }
    }

    private void loadSessions() {
        ChatHistoryData history = fileStore.loadChatHistory();
        chatSessions.clear();
        sessionMessages.clear();
        chatSessions.putAll(history.getSessions());
        sessionMessages.putAll(history.getMessagesBySession());

        if (chatSessions.isEmpty()) {
            createNewSession(false);
            return;
        }
        List<String> ordered = sortedSessionIds("");
        activeSessionId = ordered.isEmpty()
                ? chatSessions.keySet().iterator().next()
                : ordered.get(0);
    }

    private void createNewSession(boolean select) {
        String sessionId = "session-" + UUID.randomUUID().toString().substring(0, 8);
        sessionMessages.put(sessionId, new ArrayList<>());
        chatSessions.put(sessionId, ChatSession.createNew(sessionId, "New Conversation"));
        refreshSessionList(modernSidebar != null ? modernSidebar.getSearchQuery() : "");
        if (select) {
            activeSessionId = sessionId;
            chatPanel.clearMessages();
            appendMessage("Nova AI", "New chat started. Ask me anything about coding, AI, career, or studies.", false);
            persistAllSessions();
            chatInputBar.focusInput();
        } else if (activeSessionId == null) {
            activeSessionId = sessionId;
        }
        if (!select) {
            persistAllSessions();
        }
    }

    private void ensureWelcomeMessage() {
        if (activeSessionId == null) {
            createNewSession(true);
            return;
        }
        if (sessionMessages.get(activeSessionId).isEmpty()) {
            appendMessage("Nova AI", "Welcome to Nova AI Assistant PRO. How can I help you today?", false);
        } else {
            renderActiveSession();
        }
        refreshSessionList("");
    }

    private void updateSessionTitle(String sessionId) {
        ChatSession session = chatSessions.get(sessionId);
        if (session == null || session.isCustomTitle()) {
            return;
        }
        List<Message> messages = sessionMessages.get(sessionId);
        if (messages == null || messages.isEmpty()) {
            return;
        }
        String smartTitle = FileStore.inferSmartTitle(messages);
        session.setTitleQuietly(smartTitle);
        refreshSessionList(modernSidebar.getSearchQuery());
        persistAllSessions();
    }

    private void refreshSessionList(String query) {
        visibleSessionIds = sortedSessionIds(query);
        List<ModernSidebar.SessionView> views = new ArrayList<>();
        for (String id : visibleSessionIds) {
            ChatSession session = chatSessions.get(id);
            if (session != null) {
                views.add(new ModernSidebar.SessionView(id, session.getTitle(), session.isFavorite(), session.isPinned()));
            }
        }
        if (modernSidebar != null) {
            modernSidebar.refreshSessions(views, activeSessionId);
        }
        if (!visibleSessionIds.isEmpty() && (activeSessionId == null || !visibleSessionIds.contains(activeSessionId))) {
            activeSessionId = visibleSessionIds.get(0);
            renderActiveSession();
        }
    }

    private List<String> sortedSessionIds(String query) {
        String normalized = query == null ? "" : query.trim().toLowerCase();
        return chatSessions.values().stream()
                .filter(session -> matchesSearch(session.getId(), normalized))
                .sorted((a, b) -> {
                    if (a.isPinned() != b.isPinned()) {
                        return a.isPinned() ? -1 : 1;
                    }
                    if (a.isFavorite() != b.isFavorite()) {
                        return a.isFavorite() ? -1 : 1;
                    }
                    return b.getUpdatedAt().compareTo(a.getUpdatedAt());
                })
                .map(ChatSession::getId)
                .collect(Collectors.toList());
    }

    private boolean matchesSearch(String sessionId, String normalized) {
        if (normalized.isEmpty()) {
            return true;
        }
        ChatSession session = chatSessions.get(sessionId);
        if (session != null && session.getTitle().toLowerCase().contains(normalized)) {
            return true;
        }
        List<Message> messages = sessionMessages.getOrDefault(sessionId, new ArrayList<>());
        for (Message message : messages) {
            if (message.getContent().toLowerCase().contains(normalized)
                    || message.getSender().toLowerCase().contains(normalized)) {
                return true;
            }
        }
        return false;
    }

    private void renameSelectedConversation() {
        if (activeSessionId == null || !chatSessions.containsKey(activeSessionId)) {
            return;
        }
        ChatSession session = chatSessions.get(activeSessionId);
        String newTitle = JOptionPane.showInputDialog(this, "Enter a new name for this chat:", "Rename Chat",
                JOptionPane.PLAIN_MESSAGE);
        if (newTitle == null) {
            return;
        }
        String trimmed = newTitle.trim();
        if (trimmed.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Chat name cannot be empty.", "Rename Chat",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        session.setTitle(trimmed);
        session.setCustomTitle(true);
        refreshSessionList(modernSidebar.getSearchQuery());
        persistAllSessions();
    }

    private void toggleFavoriteSelectedConversation() {
        if (activeSessionId == null || !chatSessions.containsKey(activeSessionId)) {
            return;
        }
        ChatSession session = chatSessions.get(activeSessionId);
        session.setFavorite(!session.isFavorite());
        refreshSessionList(modernSidebar.getSearchQuery());
        persistAllSessions();
    }

    private void togglePinSelectedConversation() {
        if (activeSessionId == null || !chatSessions.containsKey(activeSessionId)) {
            return;
        }
        ChatSession session = chatSessions.get(activeSessionId);
        session.setPinned(!session.isPinned());
        refreshSessionList(modernSidebar.getSearchQuery());
        persistAllSessions();
    }

    private void deleteSelectedConversation() {
        if (activeSessionId == null || !sessionMessages.containsKey(activeSessionId)) {
            return;
        }
        int option = JOptionPane.showConfirmDialog(this, "Delete selected conversation?", "Delete Chat",
                JOptionPane.YES_NO_OPTION);
        if (option != JOptionPane.YES_OPTION) {
            return;
        }

        sessionMessages.remove(activeSessionId);
        chatSessions.remove(activeSessionId);
        persistAllSessions();

        if (sessionMessages.isEmpty()) {
            createNewSession(true);
        } else {
            activeSessionId = sessionMessages.keySet().iterator().next();
            renderActiveSession();
        }
        refreshSessionList(modernSidebar.getSearchQuery());
    }

    private void persistAllSessions() {
        fileStore.saveChatHistory(chatSessions, sessionMessages);
    }

    private void switchTheme(ThemeManager.AppTheme theme) {
        profile = new UserProfile(profile.getName(), profile.getEmail(), theme);
        fileStore.saveUserProfile(profile);
        applyTheme(theme);
    }

    private void handleLogout() {
        int choice = JOptionPane.showConfirmDialog(this, "Are you sure you want to logout?", "Logout",
                JOptionPane.YES_NO_OPTION);
        if (choice != JOptionPane.YES_OPTION) {
            return;
        }
        SwingUtilities.invokeLater(() -> {
            dispose();
            new LoginFrame().setVisible(true);
        });
    }

    private void exportChatsFormatted() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new java.io.File("nova_chat_export_formatted.txt"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            boolean success = fileStore.exportFormattedHistory(chooser.getSelectedFile().getAbsolutePath());
            JOptionPane.showMessageDialog(this, success ? "Chat exported (readable format)." : "Export failed.");
        }
    }

    private void exportChatsRaw() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new java.io.File("nova_chat_history_backup.txt"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            boolean success = fileStore.exportHistory(chooser.getSelectedFile().getAbsolutePath());
            JOptionPane.showMessageDialog(this, success ? "Raw chat backup exported." : "Export failed.");
        }
    }

    private void showSettingsDialog() {
        ThemeManager.AppTheme theme = profile.getTheme();
        JDialog dialog = new JDialog(this, "User Settings", true);
        dialog.setSize(420, 320);
        dialog.setLocationRelativeTo(this);
        ThemeManager.styleDialog(dialog, theme);

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        ThemeManager.stylePanel(body, theme, false);

        JLabel nameLabel = new JLabel("Display Name");
        JTextField nameField = new JTextField(profile.getName());
        JLabel emailLabel = new JLabel("Email (optional)");
        JTextField emailField = new JTextField(profile.getEmail());
        JLabel themeLabel = new JLabel("Theme");
        JComboBox<ThemeManager.AppTheme> themeCombo = new JComboBox<>(ThemeManager.AppTheme.values());
        themeCombo.setSelectedItem(profile.getTheme());

        ThemeManager.styleText(nameLabel, theme);
        ThemeManager.styleText(emailLabel, theme);
        ThemeManager.styleText(themeLabel, theme);
        ThemeManager.styleInput(nameField, theme);
        ThemeManager.styleInput(emailField, theme);

        body.add(nameLabel);
        body.add(nameField);
        body.add(javax.swing.Box.createVerticalStrut(8));
        body.add(emailLabel);
        body.add(emailField);
        body.add(javax.swing.Box.createVerticalStrut(8));
        body.add(themeLabel);
        body.add(themeCombo);

        JButton saveButton = new JButton("Save Settings");
        ThemeManager.styleButton(saveButton, theme);
        saveButton.addActionListener(e -> {
            ThemeManager.AppTheme selected = (ThemeManager.AppTheme) themeCombo.getSelectedItem();
            profile = new UserProfile(nameField.getText(), emailField.getText(), selected);
            fileStore.saveUserProfile(profile);
            modernSidebar.setUsername(profile.getName());
            applyTheme(selected);
            setJMenuBar(buildMenuBar());
            dialog.dispose();
        });

        JPanel footer = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));
        ThemeManager.stylePanel(footer, theme, false);
        footer.add(saveButton);

        JPanel root = new JPanel(new BorderLayout());
        ThemeManager.applyRootBackground(root, theme);
        root.add(body, BorderLayout.CENTER);
        root.add(footer, BorderLayout.SOUTH);
        dialog.setContentPane(root);
        dialog.setVisible(true);
    }

    private void showAboutDialog() {
        ThemeManager.AppTheme theme = profile.getTheme();
        JDialog dialog = new JDialog(this, "About Nova AI Assistant PRO", true);
        dialog.setSize(520, 420);
        dialog.setLocationRelativeTo(this);
        ThemeManager.styleDialog(dialog, theme);

        String aboutText = "Nova AI Assistant PRO  v" + APP_VERSION + "\n"
                + "Modern AI desktop assistant — Java Swing\n\n"
                + "• ChatGPT-inspired UI\n"
                + "• Multi-session chat & tools\n"
                + "• Resume, Interview, Notes, Study modules\n\n"
                + "© 2026 Nova AI Assistant PRO";

        JTextArea area = new JTextArea(aboutText);
        area.setEditable(false);
        area.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 13));
        ThemeManager.styleTextArea(area, theme);

        JButton closeButton = new JButton("Close");
        ThemeManager.styleButton(closeButton, theme);
        closeButton.addActionListener(e -> dialog.dispose());

        JPanel root = new JPanel(new BorderLayout());
        ThemeManager.applyRootBackground(root, theme);
        root.add(new JScrollPane(area), BorderLayout.CENTER);
        JPanel footer = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));
        footer.add(closeButton);
        root.add(footer, BorderLayout.SOUTH);
        dialog.setContentPane(root);
        dialog.setVisible(true);
    }

    private void showShortcutsDialog() {
        ThemeManager.AppTheme theme = profile.getTheme();
        JDialog dialog = new JDialog(this, "Keyboard Shortcuts", true);
        dialog.setSize(380, 280);
        dialog.setLocationRelativeTo(this);
        ThemeManager.styleDialog(dialog, theme);

        String text = "Keyboard Shortcuts:\n\n"
                + "• Enter: Send Message\n"
                + "• Shift + Enter: Insert New Line\n"
                + "• Ctrl + N: Start New Chat\n"
                + "• Ctrl + E: Export Current Chat\n"
                + "• Ctrl + S: User Settings\n"
                + "• Ctrl + H: Help / About";

        JTextArea area = new JTextArea(text);
        area.setEditable(false);
        area.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 13));
        ThemeManager.styleTextArea(area, theme);

        JButton closeButton = new JButton("Close");
        ThemeManager.styleButton(closeButton, theme);
        closeButton.addActionListener(e -> dialog.dispose());

        JPanel root = new JPanel(new BorderLayout());
        ThemeManager.applyRootBackground(root, theme);
        root.add(new JScrollPane(area), BorderLayout.CENTER);
        JPanel footer = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));
        footer.add(closeButton);
        root.add(footer, BorderLayout.SOUTH);
        dialog.setContentPane(root);
        dialog.setVisible(true);
    }

    private void showVersionInfoDialog() {
        ThemeManager.AppTheme theme = profile.getTheme();
        JDialog dialog = new JDialog(this, "Version Info", true);
        dialog.setSize(380, 240);
        dialog.setLocationRelativeTo(this);
        ThemeManager.styleDialog(dialog, theme);

        String text = "Version Information:\n\n"
                + "• Application: Nova AI Assistant PRO\n"
                + "• Version: " + APP_VERSION + "\n"
                + "• Build Date: June 2026\n"
                + "• Runtime: Java SE 8+";

        JTextArea area = new JTextArea(text);
        area.setEditable(false);
        area.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 13));
        ThemeManager.styleTextArea(area, theme);

        JButton closeButton = new JButton("Close");
        ThemeManager.styleButton(closeButton, theme);
        closeButton.addActionListener(e -> dialog.dispose());

        JPanel root = new JPanel(new BorderLayout());
        ThemeManager.applyRootBackground(root, theme);
        root.add(new JScrollPane(area), BorderLayout.CENTER);
        JPanel footer = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));
        footer.add(closeButton);
        root.add(footer, BorderLayout.SOUTH);
        dialog.setContentPane(root);
        dialog.setVisible(true);
    }

    private void applyTheme(ThemeManager.AppTheme theme) {
        ThemeManager.setCurrentTheme(theme);
        SwingUtilities.invokeLater(() -> {
            ThemeManager.applyGlobalUiDefaults(theme);
            getContentPane().setBackground(ThemeManager.background(theme));
            if (modernSidebar != null) {
                modernSidebar.setBackground(UiPalette.SIDEBAR);
                modernSidebar.repaint();
            }
            if (mainPanel != null) {
                mainPanel.setOpaque(false);
            }
            chatPanel.applyTheme(theme);
            ThemeManager.styleMenu(getJMenuBar(), theme);
            repaint();
        });
    }
}
