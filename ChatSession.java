import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ChatSession {
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final String id;
    private String title;
    private boolean favorite;
    private boolean pinned;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ChatSession(String id, String title, boolean favorite, boolean pinned, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.title = title == null || title.trim().isEmpty() ? "New Conversation" : title.trim();
        this.favorite = favorite;
        this.pinned = pinned;
        this.createdAt = createdAt == null ? LocalDateTime.now() : createdAt;
        this.updatedAt = updatedAt == null ? this.createdAt : updatedAt;
    }

    public ChatSession(String id, String title, boolean favorite, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this(id, title, favorite, false, createdAt, updatedAt);
    }

    public static ChatSession createNew(String id, String title) {
        LocalDateTime now = LocalDateTime.now();
        return new ChatSession(id, title, false, false, now, now);
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title == null || title.trim().isEmpty() ? "New Conversation" : title.trim();
        touch();
    }

    public void setTitleQuietly(String title) {
        this.title = title == null || title.trim().isEmpty() ? "New Conversation" : title.trim();
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
        touch();
    }

    public boolean isPinned() {
        return pinned;
    }

    public void setPinned(boolean pinned) {
        this.pinned = pinned;
        touch();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void touch() {
        updatedAt = LocalDateTime.now();
    }

    public void touch(LocalDateTime time) {
        if (time != null && (updatedAt == null || time.isAfter(updatedAt))) {
            updatedAt = time;
        }
    }

    public String displayLabel() {
        return (favorite ? "★ " : "  ") + title;
    }

    public String toStorageLine() {
        return "SESSION|"
                + id
                + "|" + title.replace("|", "/").replace("\n", " ")
                + "|" + favorite
                + "|" + createdAt.format(TIME_FORMAT)
                + "|" + updatedAt.format(TIME_FORMAT)
                + "|" + pinned;
    }

    public static ChatSession fromStorageLine(String line) {
        if (line == null || !line.startsWith("SESSION|")) {
            return null;
        }
        String[] parts = line.split("\\|", 7);
        if (parts.length < 6) {
            return null;
        }
        LocalDateTime created = LocalDateTime.parse(parts[4], TIME_FORMAT);
        LocalDateTime updated = LocalDateTime.parse(parts[5], TIME_FORMAT);
        boolean fav = Boolean.parseBoolean(parts[3]);
        boolean pin = parts.length == 7 ? Boolean.parseBoolean(parts[6]) : false;
        return new ChatSession(parts[1], parts[2], fav, pin, created, updated);
    }
}
