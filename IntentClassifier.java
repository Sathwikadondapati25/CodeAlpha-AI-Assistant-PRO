import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

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
        FOLLOW_UP
    }

    private static final Pattern NON_TEXT = Pattern.compile("[^a-z0-9\\s]");
    private static final Map<String, String> SYNONYMS = createSynonyms();

    public Intent classify(String input, ConversationMemory memory) {
        String text = normalize(input);
        if (text.isEmpty()) {
            return Intent.GENERAL;
        }

        if (memory.getPendingQuestion().contains("python_goal")) {
            if (containsAny(text, "ai", "machine learning", "ml")) {
                return Intent.FOLLOW_UP;
            }
            if (containsAny(text, "web", "django", "flask")) {
                return Intent.FOLLOW_UP;
            }
            if (containsAny(text, "automation", "script", "scripting")) {
                return Intent.FOLLOW_UP;
            }
        }

        if (containsAny(text, "hello", "hi", "hey", "good morning", "good evening")) {
            return Intent.GREETING;
        }
        if (containsAny(text, "java", "jvm", "spring")) {
            return Intent.JAVA;
        }
        if (containsAny(text, "python", "py")) {
            return Intent.PYTHON;
        }
        if (containsAny(text, "c programming", "c language", "pointer", "malloc", "printf")) {
            return Intent.C_PROGRAMMING;
        }
        if (containsAny(text, "artificial intelligence", "ai", "intelligent system")) {
            return Intent.AI;
        }
        if (containsAny(text, "machine learning", "ml", "model training")) {
            return Intent.ML;
        }
        if (containsAny(text, "data science", "analytics", "data analysis")) {
            return Intent.DATA_SCIENCE;
        }
        if (containsAny(text, "web development", "frontend", "backend", "full stack", "html css", "javascript")) {
            return Intent.WEB_DEVELOPMENT;
        }
        if (containsAny(text, "career", "placement", "job", "roadmap")) {
            return Intent.CAREER;
        }
        if (containsAny(text, "interview", "interview tips", "hr round")) {
            return Intent.INTERVIEW;
        }
        if (containsAny(text, "resume", "cv", "resume building", "ats")) {
            return Intent.RESUME;
        }
        if (containsAny(text, "college", "study", "cgpa", "semester")) {
            return Intent.COLLEGE;
        }
        if (containsAny(text, "project", "project idea", "portfolio")) {
            return Intent.PROJECTS;
        }
        if (containsAny(text, "internship", "intern", "apply internship")) {
            return Intent.INTERNSHIP;
        }
        if (containsAny(text, "motivate", "motivation", "demotivated", "stuck")) {
            return Intent.MOTIVATION;
        }
        if (containsAny(text, "tell me more", "can you explain", "why", "how")) {
            return Intent.FOLLOW_UP;
        }
        return Intent.GENERAL;
    }

    public String normalize(String input) {
        String lower = input == null ? "" : input.toLowerCase(Locale.ENGLISH).trim();
        lower = NON_TEXT.matcher(lower).replaceAll(" ");
        lower = lower.replaceAll("\\s+", " ").trim();
        for (Map.Entry<String, String> entry : SYNONYMS.entrySet()) {
            lower = lower.replace(entry.getKey(), entry.getValue());
        }
        return lower;
    }

    private boolean containsAny(String text, String... keywords) {
        List<String> keys = Arrays.asList(keywords);
        for (String keyword : keys) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private static Map<String, String> createSynonyms() {
        Map<String, String> synonyms = new HashMap<>();
        synonyms.put("artifical intelligence", "artificial intelligence");
        synonyms.put("machin learning", "machine learning");
        synonyms.put("datascience", "data science");
        synonyms.put("career guidance", "career");
        synonyms.put("placements", "placement");
        synonyms.put("internships", "internship");
        synonyms.put("proj ideas", "project idea");
        synonyms.put("ml", "machine learning");
        synonyms.put("ds", "data science");
        synonyms.put("web dev", "web development");
        synonyms.put("c lang", "c language");
        return synonyms;
    }
}
