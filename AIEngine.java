import java.util.HashMap;
import java.util.Map;

public class AIEngine {
    private final IntentClassifier classifier;
    private final ResponseGenerator responseGenerator;
    private final Map<String, ConversationMemory> memories;

    public AIEngine() {
        this.classifier = new IntentClassifier();
        this.responseGenerator = new ResponseGenerator();
        this.memories = new HashMap<>();
    }

    public String respond(String userInput) {
        return respond(userInput, "default-session");
    }

    public String respond(String userInput, String sessionId) {
        ConversationMemory memory;
        synchronized (memories) {
            memory = memories.computeIfAbsent(sessionId, id -> new ConversationMemory());
        }
        IntentClassifier.Intent intent = classifier.classify(userInput, memory);
        String normalized = classifier.normalize(userInput);
        return responseGenerator.generateResponse(intent, userInput, normalized, memory);
    }
}
