import java.util.ArrayList;
import java.util.List;

public class ConversationMemory {
    private String lastTopic = "general";
    private String pendingQuestion = "";
    private final List<String> recentUserMessages = new ArrayList<>();

    public void rememberUserMessage(String message, String topic) {
        recentUserMessages.add(message);
        if (recentUserMessages.size() > 12) {
            recentUserMessages.remove(0);
        }
        if (topic != null && !topic.trim().isEmpty()) {
            lastTopic = topic;
        }
    }

    public String getLastTopic() {
        return lastTopic;
    }

    public void setPendingQuestion(String pendingQuestion) {
        this.pendingQuestion = pendingQuestion == null ? "" : pendingQuestion;
    }

    public String getPendingQuestion() {
        return pendingQuestion;
    }
}
