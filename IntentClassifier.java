public class IntentClassifier {
    public enum Intent {
        GREETING,
        JAVA,
        PYTHON,
        C_PROGRAMMING,
        AI,
        ML,
        DATA_SCIENCE,
        WEB_DEVELOPMENT,
        CAREER,
        INTERVIEW,
        RESUME,
        COLLEGE,
        PROJECTS,
        INTERNSHIP,
        MOTIVATION,
        GENERAL,
        FOLLOW_UP,
        HTML,
        CSS,
        JAVASCRIPT,
        REACT,
        SQL,
        DBMS,
        OOP,
        DSA,
        OS,
        COMPUTER_NETWORKS
    }

    private final TextNormalizer normalizer;
    private final IntentScoringEngine scoringEngine;

    public IntentClassifier() {
        this.normalizer = new TextNormalizer();
        this.scoringEngine = new IntentScoringEngine();
    }

    public Intent classify(String input, ConversationMemory memory) {
        String text = normalize(input);
        if (text.isEmpty()) {
            return Intent.GENERAL;
        }

        // Memory-based python follow-up overrides
        if (memory.getPendingQuestion().contains("python_goal")) {
            if (scoringEngine.scoreMatch(text, "ai") > 0 || scoringEngine.scoreMatch(text, "machine learning") > 0 || scoringEngine.scoreMatch(text, "ml") > 0) {
                return Intent.FOLLOW_UP;
            }
            if (scoringEngine.scoreMatch(text, "web") > 0 || scoringEngine.scoreMatch(text, "django") > 0 || scoringEngine.scoreMatch(text, "flask") > 0) {
                return Intent.FOLLOW_UP;
            }
            if (scoringEngine.scoreMatch(text, "automation") > 0 || scoringEngine.scoreMatch(text, "script") > 0 || scoringEngine.scoreMatch(text, "scripting") > 0) {
                return Intent.FOLLOW_UP;
            }
        }

        return scoringEngine.score(text);
    }

    public String normalize(String input) {
        return normalizer.normalize(input);
    }
}
