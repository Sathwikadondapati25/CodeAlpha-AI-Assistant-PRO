import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class IntentScoringEngine {
    private final Map<IntentClassifier.Intent, Map<String, Integer>> intentKeywords;
    private static final int MIN_SCORE_THRESHOLD = 2;

    public IntentScoringEngine() {
        this.intentKeywords = createIntentKeywords();
    }

    public IntentClassifier.Intent score(String text) {
        IntentClassifier.Intent bestIntent = IntentClassifier.Intent.GENERAL;
        int maxScore = 0;

        for (Map.Entry<IntentClassifier.Intent, Map<String, Integer>> entry : intentKeywords.entrySet()) {
            IntentClassifier.Intent intent = entry.getKey();
            Map<String, Integer> keywords = entry.getValue();
            int score = 0;
            for (Map.Entry<String, Integer> kw : keywords.entrySet()) {
                int count = scoreMatch(text, kw.getKey());
                score += count * kw.getValue();
            }
            if (score > maxScore) {
                maxScore = score;
                bestIntent = intent;
            }
        }

        if (maxScore >= MIN_SCORE_THRESHOLD) {
            return bestIntent;
        }
        return IntentClassifier.Intent.GENERAL;
    }

    int scoreMatch(String text, String keyword) {
        if (keyword.length() <= 3) {
            // Short acronyms/words matched strictly using word boundaries to avoid substring false positives (e.g. 'ml' in 'html')
            String regex = "\\b" + Pattern.quote(keyword) + "\\b";
            Pattern pattern = Pattern.compile(regex);
            java.util.regex.Matcher matcher = pattern.matcher(text);
            int count = 0;
            while (matcher.find()) {
                count++;
            }
            return count;
        } else {
            // Substring matching for phrases and longer words
            int count = 0;
            int idx = 0;
            while ((idx = text.indexOf(keyword, idx)) != -1) {
                count++;
                idx += keyword.length();
            }
            return count;
        }
    }

    private Map<IntentClassifier.Intent, Map<String, Integer>> createIntentKeywords() {
        Map<IntentClassifier.Intent, Map<String, Integer>> maps = new HashMap<>();

        // GREETING
        Map<String, Integer> greeting = new HashMap<>();
        greeting.put("hello", 8);
        greeting.put("hi", 6);
        greeting.put("hey", 6);
        greeting.put("good morning", 8);
        greeting.put("good evening", 8);
        greeting.put("good afternoon", 8);
        greeting.put("greetings", 8);
        maps.put(IntentClassifier.Intent.GREETING, greeting);

        // HTML
        Map<String, Integer> html = new HashMap<>();
        html.put("html", 10);
        html.put("hypertext", 8);
        html.put("markup", 8);
        html.put("web page", 4);
        html.put("website", 2);
        maps.put(IntentClassifier.Intent.HTML, html);

        // CSS
        Map<String, Integer> css = new HashMap<>();
        css.put("css", 10);
        css.put("styling", 6);
        css.put("stylesheet", 8);
        css.put("style sheet", 8);
        css.put("flexbox", 8);
        css.put("grid", 3);
        maps.put(IntentClassifier.Intent.CSS, css);

        // JAVASCRIPT
        Map<String, Integer> js = new HashMap<>();
        js.put("javascript", 10);
        js.put("js", 8);
        js.put("es6", 8);
        js.put("dom", 4);
        js.put("scripting language", 5);
        maps.put(IntentClassifier.Intent.JAVASCRIPT, js);

        // REACT
        Map<String, Integer> react = new HashMap<>();
        react.put("react", 10);
        react.put("reactjs", 10);
        react.put("component", 4);
        react.put("jsx", 8);
        react.put("virtual dom", 8);
        react.put("hooks", 6);
        maps.put(IntentClassifier.Intent.REACT, react);

        // SQL
        Map<String, Integer> sql = new HashMap<>();
        sql.put("sql", 10);
        sql.put("query language", 8);
        sql.put("join", 4);
        sql.put("queries", 5);
        sql.put("select", 3);
        maps.put(IntentClassifier.Intent.SQL, sql);

        // DBMS
        Map<String, Integer> dbms = new HashMap<>();
        dbms.put("dbms", 10);
        dbms.put("database", 8);
        dbms.put("rdbms", 9);
        dbms.put("mysql", 6);
        dbms.put("mongodb", 6);
        dbms.put("nosql", 8);
        maps.put(IntentClassifier.Intent.DBMS, dbms);

        // OOP
        Map<String, Integer> oop = new HashMap<>();
        oop.put("object oriented", 10);
        oop.put("inheritance", 8);
        oop.put("polymorphism", 8);
        oop.put("encapsulation", 8);
        oop.put("abstraction", 8);
        oop.put("class", 3);
        oop.put("object", 2);
        maps.put(IntentClassifier.Intent.OOP, oop);

        // DSA
        Map<String, Integer> dsa = new HashMap<>();
        dsa.put("dsa", 10);
        dsa.put("data structure", 8);
        dsa.put("algorithm", 8);
        dsa.put("stack", 4);
        dsa.put("queue", 4);
        dsa.put("linked list", 7);
        dsa.put("tree", 4);
        dsa.put("graph", 4);
        dsa.put("recursion", 5);
        maps.put(IntentClassifier.Intent.DSA, dsa);

        // OS
        Map<String, Integer> os = new HashMap<>();
        os.put("operating system", 10);
        os.put("os", 8);
        os.put("deadlock", 8);
        os.put("paging", 7);
        os.put("virtual memory", 7);
        os.put("cpu scheduling", 7);
        maps.put(IntentClassifier.Intent.OS, os);

        // COMPUTER_NETWORKS
        Map<String, Integer> cn = new HashMap<>();
        cn.put("computer networks", 10);
        cn.put("network", 7);
        cn.put("tcp", 8);
        cn.put("udp", 8);
        cn.put("ip address", 8);
        cn.put("dns", 8);
        cn.put("osi model", 9);
        maps.put(IntentClassifier.Intent.COMPUTER_NETWORKS, cn);

        // JAVA
        Map<String, Integer> java = new HashMap<>();
        java.put("java", 10);
        java.put("jvm", 8);
        java.put("jdk", 8);
        java.put("spring boot", 8);
        java.put("servlet", 6);
        maps.put(IntentClassifier.Intent.JAVA, java);

        // PYTHON
        Map<String, Integer> python = new HashMap<>();
        python.put("python", 10);
        python.put("py", 8);
        python.put("numpy", 7);
        python.put("pandas", 7);
        maps.put(IntentClassifier.Intent.PYTHON, python);

        // C_PROGRAMMING
        Map<String, Integer> cprog = new HashMap<>();
        cprog.put("c programming", 10);
        cprog.put("c language", 9);
        cprog.put("pointer", 6);
        cprog.put("malloc", 7);
        cprog.put("printf", 6);
        maps.put(IntentClassifier.Intent.C_PROGRAMMING, cprog);

        // AI
        Map<String, Integer> ai = new HashMap<>();
        ai.put("artificial intelligence", 10);
        ai.put("ai", 8);
        maps.put(IntentClassifier.Intent.AI, ai);

        // ML
        Map<String, Integer> ml = new HashMap<>();
        ml.put("machine learning", 10);
        ml.put("ml", 8);
        ml.put("supervised", 6);
        ml.put("unsupervised", 6);
        maps.put(IntentClassifier.Intent.ML, ml);

        // DATA_SCIENCE
        Map<String, Integer> ds = new HashMap<>();
        ds.put("data science", 10);
        ds.put("analytics", 6);
        ds.put("data analysis", 7);
        maps.put(IntentClassifier.Intent.DATA_SCIENCE, ds);

        // WEB_DEVELOPMENT
        Map<String, Integer> web = new HashMap<>();
        web.put("web development", 10);
        web.put("frontend", 6);
        web.put("backend", 6);
        web.put("full stack", 8);
        maps.put(IntentClassifier.Intent.WEB_DEVELOPMENT, web);

        // CAREER
        Map<String, Integer> career = new HashMap<>();
        career.put("career", 10);
        career.put("placement", 8);
        career.put("job", 6);
        career.put("roadmap", 5);
        maps.put(IntentClassifier.Intent.CAREER, career);

        // INTERVIEW
        Map<String, Integer> interview = new HashMap<>();
        interview.put("interview", 10);
        interview.put("hr round", 8);
        interview.put("mock interview", 8);
        maps.put(IntentClassifier.Intent.INTERVIEW, interview);

        // RESUME
        Map<String, Integer> resume = new HashMap<>();
        resume.put("resume", 10);
        resume.put("cv", 8);
        resume.put("ats", 7);
        maps.put(IntentClassifier.Intent.RESUME, resume);

        // COLLEGE
        Map<String, Integer> college = new HashMap<>();
        college.put("college", 10);
        college.put("study", 5);
        college.put("cgpa", 8);
        college.put("semester", 8);
        maps.put(IntentClassifier.Intent.COLLEGE, college);

        // PROJECTS
        Map<String, Integer> projects = new HashMap<>();
        projects.put("project", 10);
        projects.put("projects", 10);
        projects.put("project idea", 9);
        maps.put(IntentClassifier.Intent.PROJECTS, projects);

        // INTERNSHIP
        Map<String, Integer> internship = new HashMap<>();
        internship.put("internship", 10);
        internship.put("intern", 8);
        maps.put(IntentClassifier.Intent.INTERNSHIP, internship);

        // MOTIVATION
        Map<String, Integer> motivation = new HashMap<>();
        motivation.put("motivate", 10);
        motivation.put("motivation", 10);
        motivation.put("demotivated", 8);
        motivation.put("stuck", 4);
        maps.put(IntentClassifier.Intent.MOTIVATION, motivation);

        // FOLLOW_UP
        Map<String, Integer> followUp = new HashMap<>();
        followUp.put("tell me more", 8);
        followUp.put("can you explain", 8);
        followUp.put("explain", 4);
        followUp.put("elaborate", 5);
        maps.put(IntentClassifier.Intent.FOLLOW_UP, followUp);

        return maps;
    }
}
