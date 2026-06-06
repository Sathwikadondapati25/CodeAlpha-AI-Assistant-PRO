import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextNormalizer {
    private static final Pattern NON_TEXT = Pattern.compile("[^a-z0-9\\s]");
    private final Map<String, String> slangMap;
    private final Map<String, String> synonymsMap;

    public TextNormalizer() {
        this.slangMap = createSlangMap();
        this.synonymsMap = createSynonymsMap();
    }

    public String normalize(String input) {
        if (input == null) {
            return "";
        }
        String lower = input.toLowerCase(Locale.ENGLISH).trim();
        
        // Handle programming language specific abbreviations
        lower = lower.replace("c++", "cpp");
        lower = lower.replace("c#", "csharp");
        
        // Remove symbols except alphanumeric and spaces
        lower = NON_TEXT.matcher(lower).replaceAll(" ");
        lower = lower.replaceAll("\\s+", " ").trim();

        // Translate slang tokens (token-by-token translation)
        String[] tokens = lower.split(" ");
        StringBuilder sb = new StringBuilder();
        for (String token : tokens) {
            sb.append(slangMap.getOrDefault(token, token)).append(" ");
        }
        lower = sb.toString().trim();

        // Translate multi-word and single-word synonyms using word boundaries (\bkey\b)
        for (Map.Entry<String, String> entry : synonymsMap.entrySet()) {
            String target = "\\b" + Pattern.quote(entry.getKey()) + "\\b";
            lower = lower.replaceAll(target, Matcher.quoteReplacement(entry.getValue()));
        }

        // Final cleanup of extra spaces
        lower = lower.replaceAll("\\s+", " ").trim();
        return lower;
    }

    private Map<String, String> createSlangMap() {
        Map<String, String> slang = new HashMap<>();
        slang.put("wt", "what");
        slang.put("whats", "what is");
        slang.put("pls", "please");
        slang.put("plz", "please");
        slang.put("u", "you");
        slang.put("ur", "your");
        slang.put("r", "are");
        slang.put("hii", "hi");
        slang.put("heyy", "hey");
        slang.put("explaination", "explanation");
        slang.put("explainations", "explanations");
        slang.put("lang", "language");
        slang.put("webpage", "web page");
        slang.put("databases", "database");
        slang.put("oop", "object oriented");
        slang.put("oops", "object oriented");
        return slang;
    }

    private Map<String, String> createSynonymsMap() {
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
        synonyms.put("operating systems", "operating system");
        synonyms.put("computer network", "computer networks");
        synonyms.put("networking", "computer networks");
        return synonyms;
    }
}
