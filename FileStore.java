import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

public class FileStore {
    private final Path chatHistoryPath;
    private final Path profilePath;
    private static final Path USERS_PATH = Paths.get("data", "users.properties");
    private static final Path CONFIG_PATH = Paths.get("data", "config.properties");

    public FileStore(String chatHistoryPath, String profilePath) {
        this.chatHistoryPath = Paths.get(chatHistoryPath);
        this.profilePath = Paths.get(profilePath);
        ensureFilesExist();
    }

    public synchronized void appendMessage(Message message) {
        ensureFilesExist();
        try (BufferedWriter writer = Files.newBufferedWriter(
                chatHistoryPath,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND)) {
            writer.write(message.toStorageLine());
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized List<Message> loadMessages() {
        return loadChatHistory().getMessagesBySession().values().stream()
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    public synchronized Map<String, List<Message>> loadSessions() {
        return loadChatHistory().getMessagesBySession();
    }

    public synchronized ChatHistoryData loadChatHistory() {
        ensureFilesExist();
        ChatHistoryData data = new ChatHistoryData();
        if (!Files.exists(chatHistoryPath)) {
            return data;
        }

        try {
            for (String line : Files.readAllLines(chatHistoryPath, StandardCharsets.UTF_8)) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                if (line.startsWith("SESSION|")) {
                    ChatSession session = ChatSession.fromStorageLine(line);
                    if (session != null) {
                        data.getSessions().put(session.getId(), session);
                    }
                    continue;
                }
                try {
                    Message message = Message.fromStorageLine(line);
                    if (message != null) {
                        data.getMessagesBySession()
                                .computeIfAbsent(message.getSessionId(), key -> new ArrayList<>())
                                .add(message);
                    }
                } catch (Exception ignored) {
                    // Skip malformed line and continue.
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (Map.Entry<String, List<Message>> entry : data.getMessagesBySession().entrySet()) {
            String sessionId = entry.getKey();
            ChatSession session = data.getSessions().get(sessionId);
            if (session == null) {
                session = ChatSession.createNew(sessionId, inferTitleFromMessages(entry.getValue()));
                data.getSessions().put(sessionId, session);
            } else if ("New Conversation".equals(session.getTitle())) {
                String inferred = inferTitleFromMessages(entry.getValue());
                if (!"New Conversation".equals(inferred)) {
                    session.setTitleQuietly(inferred);
                }
            }
            for (Message message : entry.getValue()) {
                session.touch(message.getTimestamp());
            }
        }
        return data;
    }

    public synchronized void saveChatHistory(Map<String, ChatSession> sessions, Map<String, List<Message>> messagesBySession) {
        ensureFilesExist();
        List<ChatSession> orderedSessions = sessions.values().stream()
                .sorted((a, b) -> {
                    if (a.isPinned() != b.isPinned()) {
                        return a.isPinned() ? -1 : 1;
                    }
                    if (a.isFavorite() != b.isFavorite()) {
                        return a.isFavorite() ? -1 : 1;
                    }
                    return b.getUpdatedAt().compareTo(a.getUpdatedAt());
                })
                .collect(Collectors.toList());

        List<Message> allMessages = new ArrayList<>();
        for (ChatSession session : orderedSessions) {
            List<Message> messages = messagesBySession.get(session.getId());
            if (messages != null) {
                allMessages.addAll(messages);
            }
        }
        for (Map.Entry<String, List<Message>> entry : messagesBySession.entrySet()) {
            if (!sessions.containsKey(entry.getKey())) {
                allMessages.addAll(entry.getValue());
            }
        }

        try (BufferedWriter writer = Files.newBufferedWriter(
                chatHistoryPath,
                StandardCharsets.UTF_8,
                StandardOpenOption.TRUNCATE_EXISTING)) {
            for (ChatSession session : orderedSessions) {
                writer.write(session.toStorageLine());
                writer.newLine();
            }
            for (Message message : allMessages) {
                writer.write(message.toStorageLine());
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String inferTitleFromMessages(List<Message> messages) {
        for (Message message : messages) {
            if (message.isUserMessage()) {
                String text = message.getContent();
                return text.length() > 34 ? text.substring(0, 34) + "..." : text;
            }
        }
        if (!messages.isEmpty()) {
            return "Chat " + messages.get(0).getTimestamp().format(java.time.format.DateTimeFormatter.ofPattern("MMM dd"));
        }
        return "New Conversation";
    }

    public synchronized boolean exportHistory(String destinationPath) {
        ensureFilesExist();
        try {
            Path destination = Paths.get(destinationPath);
            Path parent = destination.getParent();
            if (parent != null && !Files.exists(parent)) {
                Files.createDirectories(parent);
            }
            Files.copy(chatHistoryPath, destination, StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public synchronized void clearHistory() {
        ensureFilesExist();
        try {
            Files.write(chatHistoryPath, new byte[0], StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void rewriteHistory(List<Message> messages) {
        Map<String, List<Message>> bySession = new LinkedHashMap<>();
        for (Message message : messages) {
            bySession.computeIfAbsent(message.getSessionId(), key -> new ArrayList<>()).add(message);
        }
        Map<String, ChatSession> sessions = new LinkedHashMap<>();
        for (String sessionId : bySession.keySet()) {
            sessions.put(sessionId, ChatSession.createNew(sessionId, inferTitleFromMessages(bySession.get(sessionId))));
        }
        saveChatHistory(sessions, bySession);
    }

    public synchronized void saveUserProfile(UserProfile profile) {
        ensureFilesExist();
        Properties properties = new Properties();
        properties.setProperty("name", profile.getName());
        properties.setProperty("email", profile.getEmail());
        properties.setProperty("theme", profile.getTheme().getId());
        properties.setProperty("darkMode", String.valueOf(profile.isDarkMode()));
        try (BufferedWriter writer = Files.newBufferedWriter(profilePath, StandardCharsets.UTF_8, StandardOpenOption.TRUNCATE_EXISTING)) {
            properties.store(writer, "Nova AI Assistant PRO user profile");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized UserProfile loadUserProfile() {
        ensureFilesExist();
        Properties properties = new Properties();
        try {
            properties.load(Files.newBufferedReader(profilePath, StandardCharsets.UTF_8));
        } catch (IOException e) {
            return UserProfile.defaultProfile();
        }
        String name = properties.getProperty("name", "Guest");
        String email = properties.getProperty("email", "");
        String themeId = properties.getProperty("theme", "");
        if (themeId.isEmpty()) {
            boolean darkMode = Boolean.parseBoolean(properties.getProperty("darkMode", "false"));
            return new UserProfile(name, email, darkMode ? ThemeManager.AppTheme.DARK : ThemeManager.AppTheme.LIGHT);
        }
        return new UserProfile(name, email, ThemeManager.AppTheme.fromId(themeId));
    }

    public synchronized boolean exportFormattedHistory(String destinationPath) {
        ChatHistoryData history = loadChatHistory();
        try {
            Path destination = Paths.get(destinationPath);
            Path parent = destination.getParent();
            if (parent != null && !Files.exists(parent)) {
                Files.createDirectories(parent);
            }
            StringBuilder export = new StringBuilder();
            export.append("NOVA AI ASSISTANT PRO — CHAT EXPORT\n");
            export.append("Exported: ").append(java.time.LocalDateTime.now()).append("\n");
            export.append(repeatChar('=', 60)).append("\n\n");

            for (ChatSession session : history.getSessions().values()) {
                export.append("SESSION: ").append(session.getTitle()).append("\n");
                export.append("ID: ").append(session.getId());
                export.append(session.isFavorite() ? " [FAVORITE]" : "").append("\n");
                export.append(repeatChar('-', 60)).append("\n");
                List<Message> messages = history.getMessagesBySession().get(session.getId());
                if (messages == null || messages.isEmpty()) {
                    export.append("(no messages)\n\n");
                    continue;
                }
                for (Message message : messages) {
                    export.append("[").append(message.getTimestamp()).append("] ");
                    export.append(message.getSender()).append(": ");
                    export.append(message.getContent()).append("\n");
                }
                export.append("\n");
            }
            Files.write(destination, export.toString().getBytes(StandardCharsets.UTF_8));
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ===== Multi-User login storage (data/users.properties) =====

    public static synchronized Properties loadUsers() {
        ensureFileExists(USERS_PATH);
        Properties props = new Properties();
        try {
            props.load(Files.newBufferedReader(USERS_PATH, StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return props;
    }

    public static synchronized void saveUser(String username, String password) {
        ensureFileExists(USERS_PATH);
        Properties props = loadUsers();
        props.setProperty(username.toLowerCase(), password);
        try (BufferedWriter writer = Files.newBufferedWriter(USERS_PATH, StandardCharsets.UTF_8,
                StandardOpenOption.TRUNCATE_EXISTING)) {
            props.store(writer, "Nova AI Assistant PRO registered users");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static synchronized void saveRememberedUsername(String username) {
        ensureFileExists(CONFIG_PATH);
        Properties props = new Properties();
        try {
            props.load(Files.newBufferedReader(CONFIG_PATH, StandardCharsets.UTF_8));
        } catch (IOException ignored) {
        }
        props.setProperty("remember.username", username.toLowerCase());
        try (BufferedWriter writer = Files.newBufferedWriter(CONFIG_PATH, StandardCharsets.UTF_8,
                StandardOpenOption.TRUNCATE_EXISTING)) {
            props.store(writer, "Nova AI Assistant PRO configuration");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static synchronized String loadRememberedUsername() {
        ensureFileExists(CONFIG_PATH);
        Properties props = new Properties();
        try {
            props.load(Files.newBufferedReader(CONFIG_PATH, StandardCharsets.UTF_8));
        } catch (IOException e) {
            return null;
        }
        String user = props.getProperty("remember.username", "").trim().toLowerCase();
        return user.isEmpty() ? null : user;
    }

    public static synchronized void clearRememberedUsername() {
        ensureFileExists(CONFIG_PATH);
        Properties props = new Properties();
        try {
            props.load(Files.newBufferedReader(CONFIG_PATH, StandardCharsets.UTF_8));
        } catch (IOException ignored) {
        }
        props.remove("remember.username");
        try (BufferedWriter writer = Files.newBufferedWriter(CONFIG_PATH, StandardCharsets.UTF_8,
                StandardOpenOption.TRUNCATE_EXISTING)) {
            props.store(writer, "Nova AI Assistant PRO configuration");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void ensureFilesExist() {
        try {
            Path parent = chatHistoryPath.getParent();
            if (parent != null && !Files.exists(parent)) {
                Files.createDirectories(parent);
            }
            if (!Files.exists(chatHistoryPath)) {
                Files.createFile(chatHistoryPath);
            }

            Path profileParent = profilePath.getParent();
            if (profileParent != null && !Files.exists(profileParent)) {
                Files.createDirectories(profileParent);
            }
            if (!Files.exists(profilePath)) {
                Files.createFile(profilePath);
                saveUserProfile(UserProfile.defaultProfile());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String repeatChar(char ch, int count) {
        StringBuilder builder = new StringBuilder(count);
        for (int i = 0; i < count; i++) {
            builder.append(ch);
        }
        return builder.toString();
    }

    private static void ensureFileExists(Path path) {
        try {
            Path parent = path.getParent();
            if (parent != null && !Files.exists(parent)) {
                Files.createDirectories(parent);
            }
            if (!Files.exists(path)) {
                Files.createFile(path);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
