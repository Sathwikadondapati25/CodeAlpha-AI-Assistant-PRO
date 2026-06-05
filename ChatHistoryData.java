import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ChatHistoryData {
    private final Map<String, ChatSession> sessions = new LinkedHashMap<>();
    private final Map<String, List<Message>> messagesBySession = new LinkedHashMap<>();

    public Map<String, ChatSession> getSessions() {
        return sessions;
    }

    public Map<String, List<Message>> getMessagesBySession() {
        return messagesBySession;
    }
}
