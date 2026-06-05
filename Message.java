import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Message {
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final String sessionId;
    private final String sender;
    private final String content;
    private final LocalDateTime timestamp;
    private final boolean userMessage;

    public Message(String sessionId, String sender, String content, boolean userMessage) {
        this(sessionId, sender, content, LocalDateTime.now(), userMessage);
    }

    public Message(String sessionId, String sender, String content, LocalDateTime timestamp, boolean userMessage) {
        this.sessionId = sessionId;
        this.sender = sender;
        this.content = content;
        this.timestamp = timestamp;
        this.userMessage = userMessage;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getSender() {
        return sender;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public boolean isUserMessage() {
        return userMessage;
    }

    public String toStorageLine() {
        return timestamp.format(TIME_FORMAT)
                + "|" + sessionId
                + "|" + (userMessage ? "user" : "bot")
                + "|" + sender
                + "|" + content.replace("\n", "\\n");
    }

    public static Message fromStorageLine(String line) {
        String[] parts = line.split("\\|", 5);
        if (parts.length != 5) {
            return null;
        }
        LocalDateTime time = LocalDateTime.parse(parts[0], TIME_FORMAT);
        boolean isUser = "user".equalsIgnoreCase(parts[2]);
        return new Message(parts[1], parts[3], parts[4].replace("\\n", "\n"), time, isUser);
    }
}
