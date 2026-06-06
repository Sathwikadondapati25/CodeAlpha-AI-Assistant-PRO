import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class InterviewSimulator extends JDialog {
    private final Path historyPath;
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final int POINTS_PER_QUESTION = 10;

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cardPanel = new JPanel(cardLayout);

    private final JLabel modeLabel = new JLabel();
    private final JLabel progressLabel = new JLabel();
    private final JLabel questionLabel = new JLabel();
    private final JTextArea answerArea = new JTextArea(8, 40);
    private final JLabel scoreLabel = new JLabel("Score: 0 / 0");
    private final JTextPane summaryPane = new JTextPane();
    private final DefaultListModel<String> historyListModel = new DefaultListModel<>();
    private final JList<String> historyList = new JList<>(historyListModel);
    private final JTextArea historyDetailArea = new JTextArea(12, 40);

    private InterviewMode selectedMode;
    private List<InterviewQuestion> activeQuestions = Collections.emptyList();
    private final List<AnswerRecord> currentAnswers = new ArrayList<>();
    private int questionIndex;
    private int runningScore;
    private int maxPossibleScore;

    public InterviewSimulator(JFrame owner) {
        super(owner, "Interview Simulator", true);
        this.historyPath = getHistoryPath(owner);
        setMinimumSize(new Dimension(920, 620));
        setSize(960, 660);
        setLocationRelativeTo(owner);

        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBackground(ThemeManager.LIGHT_BG);
        root.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        setContentPane(root);

        cardPanel.setOpaque(false);
        cardPanel.add(buildModePanel(), "mode");
        cardPanel.add(buildInterviewPanel(), "interview");
        cardPanel.add(buildSummaryPanel(), "summary");
        cardPanel.add(buildHistoryPanel(), "history");

        root.add(cardPanel, BorderLayout.CENTER);
        root.add(buildFooterBar(), BorderLayout.SOUTH);

        cardLayout.show(cardPanel, "mode");
        refreshHistoryList();
    }

    private static Path getHistoryPath(JFrame owner) {
        if (owner instanceof NovaAIFrame) {
            String username = ((NovaAIFrame) owner).getCurrentUser();
            return Paths.get("data", username, "interview_history.txt");
        }
        return Paths.get("data", "interview_history.txt");
    }

    public static void showSimulator(JFrame owner) {
        InterviewSimulator dialog = new InterviewSimulator(owner);
        dialog.setVisible(true);
    }

    private JPanel buildModePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder("Select Interview Mode"));
        ThemeManager.stylePanel(panel, false, true);

        JLabel intro = new JLabel("Choose a track. You will answer questions one at a time.");
        intro.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        ThemeManager.styleText(intro, false);
        intro.setAlignmentX(Component.LEFT_ALIGNMENT);

        JRadioButton javaMode = new JRadioButton("Java Interview — OOP, JVM, collections, Spring basics");
        JRadioButton pythonMode = new JRadioButton("Python Interview — syntax, data structures, libraries");
        JRadioButton hrMode = new JRadioButton("HR Interview — behavioral and situational questions");
        javaMode.setSelected(true);

        ButtonGroup group = new ButtonGroup();
        group.add(javaMode);
        group.add(pythonMode);
        group.add(hrMode);

        panel.add(intro);
        panel.add(Box.createVerticalStrut(16));
        for (JRadioButton button : Arrays.asList(javaMode, pythonMode, hrMode)) {
            button.setOpaque(false);
            button.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            ThemeManager.styleSecondaryText(button, false);
            button.setAlignmentX(Component.LEFT_ALIGNMENT);
            panel.add(button);
            panel.add(Box.createVerticalStrut(6));
        }
        panel.add(Box.createVerticalStrut(14));

        JButton startButton = new JButton("Start Interview");
        ThemeManager.styleButton(startButton, false);
        startButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        startButton.addActionListener(e -> {
            if (javaMode.isSelected()) {
                selectedMode = InterviewMode.JAVA;
            } else if (pythonMode.isSelected()) {
                selectedMode = InterviewMode.PYTHON;
            } else {
                selectedMode = InterviewMode.HR;
            }
            beginInterview();
        });
        panel.add(startButton);

        return panel;
    }

    private JPanel buildInterviewPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createTitledBorder("Live Interview"));
        ThemeManager.stylePanel(panel, false, false);

        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setOpaque(false);
        modeLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        progressLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        scoreLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        ThemeManager.styleText(modeLabel, false);
        ThemeManager.styleSecondaryText(progressLabel, false);
        ThemeManager.styleText(scoreLabel, false);
        top.add(modeLabel);
        top.add(progressLabel);
        top.add(scoreLabel);

        questionLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        questionLabel.setBorder(BorderFactory.createEmptyBorder(8, 4, 8, 4));
        ThemeManager.styleText(questionLabel, false);

        answerArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        answerArea.setLineWrap(true);
        answerArea.setWrapStyleWord(true);
        answerArea.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.setOpaque(false);
        JButton skipButton = new JButton("Skip Question");
        JButton submitButton = new JButton("Submit Answer");
        ThemeManager.styleButton(skipButton, false);
        ThemeManager.styleButton(submitButton, false);
        skipButton.addActionListener(e -> recordAnswer(true));
        submitButton.addActionListener(e -> recordAnswer(false));
        actions.add(skipButton);
        actions.add(submitButton);

        JPanel center = new JPanel(new BorderLayout(0, 8));
        center.setOpaque(false);
        center.add(questionLabel, BorderLayout.NORTH);
        center.add(new JScrollPane(answerArea), BorderLayout.CENTER);
        center.add(actions, BorderLayout.SOUTH);

        panel.add(top, BorderLayout.NORTH);
        panel.add(center, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildSummaryPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createTitledBorder("Result Summary"));
        ThemeManager.stylePanel(panel, false, false);

        summaryPane.setEditable(false);
        summaryPane.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        panel.add(new JScrollPane(summaryPane), BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.setOpaque(false);
        JButton againButton = new JButton("New Interview");
        JButton historyButton = new JButton("View History");
        ThemeManager.styleButton(againButton, false);
        ThemeManager.styleButton(historyButton, false);
        againButton.addActionListener(e -> cardLayout.show(cardPanel, "mode"));
        historyButton.addActionListener(e -> {
            refreshHistoryList();
            cardLayout.show(cardPanel, "history");
        });
        actions.add(againButton);
        actions.add(historyButton);
        panel.add(actions, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createTitledBorder("Interview History"));
        ThemeManager.stylePanel(panel, false, true);

        historyList.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        historyList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                showHistoryDetail(historyList.getSelectedIndex());
            }
        });

        historyDetailArea.setEditable(false);
        historyDetailArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        historyDetailArea.setLineWrap(true);
        historyDetailArea.setWrapStyleWord(true);

        JPanel split = new JPanel(new GridLayout(1, 2, 8, 0));
        split.setOpaque(false);
        split.add(new JScrollPane(historyList));
        split.add(new JScrollPane(historyDetailArea));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.setOpaque(false);
        JButton backButton = new JButton("Back to Modes");
        ThemeManager.styleButton(backButton, false);
        backButton.addActionListener(e -> cardLayout.show(cardPanel, "mode"));
        actions.add(backButton);

        panel.add(split, BorderLayout.CENTER);
        panel.add(actions, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildFooterBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        bar.setOpaque(false);

        JButton historyButton = new JButton("History");
        JButton closeButton = new JButton("Close");
        ThemeManager.styleButton(historyButton, false);
        ThemeManager.styleButton(closeButton, false);

        historyButton.addActionListener(e -> {
            refreshHistoryList();
            cardLayout.show(cardPanel, "history");
        });
        closeButton.addActionListener(e -> dispose());

        bar.add(historyButton);
        bar.add(closeButton);
        return bar;
    }

    private void beginInterview() {
        activeQuestions = QuestionBank.questionsFor(selectedMode);
        currentAnswers.clear();
        questionIndex = 0;
        runningScore = 0;
        maxPossibleScore = activeQuestions.size() * POINTS_PER_QUESTION;

        modeLabel.setText(selectedMode.getDisplayName());
        cardLayout.show(cardPanel, "interview");
        showCurrentQuestion();
    }

    private void showCurrentQuestion() {
        if (questionIndex >= activeQuestions.size()) {
            finishInterview();
            return;
        }
        InterviewQuestion question = activeQuestions.get(questionIndex);
        progressLabel.setText("Question " + (questionIndex + 1) + " of " + activeQuestions.size());
        questionLabel.setText("<html><body style='width:520px'>" + escapeHtml(question.getText()) + "</body></html>");
        answerArea.setText("");
        answerArea.requestFocus();
        updateScoreLabel();
    }

    private void recordAnswer(boolean skipped) {
        if (questionIndex >= activeQuestions.size()) {
            return;
        }
        InterviewQuestion question = activeQuestions.get(questionIndex);
        String answer = skipped ? "" : answerArea.getText().trim();
        if (!skipped && answer.isEmpty()) {
            int choice = JOptionPane.showConfirmDialog(
                    this,
                    "You have not typed an answer. Submit anyway?",
                    "Empty Answer",
                    JOptionPane.YES_NO_OPTION);
            if (choice != JOptionPane.YES_OPTION) {
                return;
            }
        }

        int score = skipped ? 0 : scoreAnswer(question, answer);
        runningScore += score;
        currentAnswers.add(new AnswerRecord(question.getText(), answer, score, POINTS_PER_QUESTION, question.getTip()));

        String feedback = skipped
                ? "Skipped. Score: 0 / " + POINTS_PER_QUESTION
                : "Score: " + score + " / " + POINTS_PER_QUESTION + "\n" + question.getTip();
        JOptionPane.showMessageDialog(this, feedback, "Answer Recorded", JOptionPane.INFORMATION_MESSAGE);
        updateScoreLabel();

        questionIndex++;
        showCurrentQuestion();
    }

    private void finishInterview() {
        String sessionId = "interview-" + UUID.randomUUID().toString().substring(0, 8);
        LocalDateTime finishedAt = LocalDateTime.now();
        InterviewResult result = new InterviewResult(
                sessionId,
                selectedMode.getDisplayName(),
                finishedAt,
                runningScore,
                maxPossibleScore,
                new ArrayList<>(currentAnswers));

        saveResult(result);
        showSummary(result);
        refreshHistoryList();
        cardLayout.show(cardPanel, "summary");
    }

    private void showSummary(InterviewResult result) {
        summaryPane.setText("");
        StyledDocument doc = summaryPane.getStyledDocument();
        SimpleAttributeSet bold = new SimpleAttributeSet();
        StyleConstants.setBold(bold, true);
        SimpleAttributeSet normal = new SimpleAttributeSet();

        appendStyled(doc, result.getMode() + " — Result Summary\n", bold);
        appendStyled(doc, repeat('=', 52) + "\n\n", normal);
        appendStyled(doc, "Finished: " + result.getFinishedAt().format(TIME_FORMAT) + "\n", normal);
        appendStyled(doc, String.format("Total Score: %d / %d (%.0f%%)\n", result.getScore(), result.getMaxScore(),
                result.getPercentage()), bold);
        appendStyled(doc, "Grade: " + result.getGrade() + "\n\n", bold);

        int qNum = 1;
        for (AnswerRecord record : result.getAnswers()) {
            appendStyled(doc, "Q" + qNum + ". " + record.getQuestion() + "\n", bold);
            appendStyled(doc, "Your answer: "
                    + (record.getAnswer().isEmpty() ? "[skipped]" : record.getAnswer()) + "\n", normal);
            appendStyled(doc, "Score: " + record.getScore() + " / " + record.getMaxScore() + "\n", normal);
            appendStyled(doc, "Tip: " + record.getTip() + "\n\n", normal);
            qNum++;
        }
        appendStyled(doc, "Session saved to " + historyPath.toString().replace('\\', '/') + "\n", normal);
        summaryPane.setCaretPosition(0);
    }

    private void updateScoreLabel() {
        int answeredPoints = runningScore;
        int possibleSoFar = Math.min(questionIndex, activeQuestions.size()) * POINTS_PER_QUESTION;
        int totalPossible = activeQuestions.size() * POINTS_PER_QUESTION;
        scoreLabel.setText("Score: " + answeredPoints + " / " + totalPossible
                + "  (answered so far: " + possibleSoFar + " pts possible)");
    }

    private static int scoreAnswer(InterviewQuestion question, String answer) {
        if (answer == null || answer.trim().isEmpty()) {
            return 0;
        }
        String normalized = answer.toLowerCase();
        int keywordHits = 0;
        for (String keyword : question.getKeywords()) {
            if (normalized.contains(keyword.toLowerCase())) {
                keywordHits++;
            }
        }
        int keywordScore = question.getKeywords().length == 0
                ? 4
                : (int) Math.round((keywordHits / (double) question.getKeywords().length) * 7);

        int lengthScore = 0;
        if (answer.length() >= 40) {
            lengthScore = 2;
        } else if (answer.length() >= 15) {
            lengthScore = 1;
        }

        int clarityBonus = answer.contains(".") || answer.split("\\s+").length >= 8 ? 1 : 0;
        return Math.min(POINTS_PER_QUESTION, keywordScore + lengthScore + clarityBonus);
    }

    private void saveResult(InterviewResult result) {
        try {
            Path parent = historyPath.getParent();
            if (parent != null && !Files.exists(parent)) {
                Files.createDirectories(parent);
            }
            if (!Files.exists(historyPath)) {
                Files.createFile(historyPath);
            }
            try (BufferedWriter writer = Files.newBufferedWriter(
                    historyPath,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.APPEND)) {
                writer.write(result.toStorageHeader());
                writer.newLine();
                for (AnswerRecord record : result.getAnswers()) {
                    writer.write(result.toStorageAnswerLine(record));
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<InterviewResult> loadHistory() {
        if (!Files.exists(historyPath)) {
            return Collections.emptyList();
        }
        Map<String, InterviewResult> byId = new LinkedHashMap<>();
        try {
            for (String line : Files.readAllLines(historyPath, StandardCharsets.UTF_8)) {
                if (line.startsWith("INTERVIEW|")) {
                    InterviewResult result = InterviewResult.fromHeader(line);
                    if (result != null) {
                        byId.put(result.getSessionId(), result);
                    }
                } else if (line.startsWith("QA|")) {
                    AnswerRecord record = AnswerRecord.fromStorageLine(line);
                    if (record != null && byId.containsKey(record.getSessionId())) {
                        byId.get(record.getSessionId()).getAnswers().add(record);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ArrayList<>(byId.values());
    }

    private void refreshHistoryList() {
        historyListModel.clear();
        List<InterviewResult> results = loadHistory();
        Collections.reverse(results);
        for (InterviewResult result : results) {
            historyListModel.addElement(result.getListLabel());
        }
        historyDetailArea.setText(results.isEmpty()
                ? "No interviews saved yet. Complete a session to build history."
                : "Select an interview to view details.");
    }

    private void showHistoryDetail(int index) {
        List<InterviewResult> results = loadHistory();
        Collections.reverse(results);
        if (index < 0 || index >= results.size()) {
            return;
        }
        InterviewResult result = results.get(index);
        StringBuilder detail = new StringBuilder();
        detail.append(result.getMode()).append('\n');
        detail.append(result.getFinishedAt().format(TIME_FORMAT)).append('\n');
        detail.append("Score: ").append(result.getScore()).append(" / ").append(result.getMaxScore());
        detail.append(String.format(" (%.0f%%) — Grade %s%n%n", result.getPercentage(), result.getGrade()));
        int q = 1;
        for (AnswerRecord record : result.getAnswers()) {
            detail.append("Q").append(q++).append(". ").append(record.getQuestion()).append('\n');
            detail.append("Answer: ").append(record.getAnswer().isEmpty() ? "[skipped]" : record.getAnswer()).append('\n');
            detail.append("Score: ").append(record.getScore()).append('/').append(record.getMaxScore()).append('\n');
            detail.append("Tip: ").append(record.getTip()).append("\n\n");
        }
        historyDetailArea.setText(detail.toString());
        historyDetailArea.setCaretPosition(0);
    }

    private static void appendStyled(StyledDocument doc, String text, SimpleAttributeSet style) {
        try {
            doc.insertString(doc.getLength(), text, style);
        } catch (Exception ignored) {
        }
    }

    private static String escapeHtml(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private static String repeat(char ch, int count) {
        StringBuilder builder = new StringBuilder(count);
        for (int i = 0; i < count; i++) {
            builder.append(ch);
        }
        return builder.toString();
    }

    private enum InterviewMode {
        JAVA("Java Interview"),
        PYTHON("Python Interview"),
        HR("HR Interview");

        private final String displayName;

        InterviewMode(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    private static final class InterviewQuestion {
        private final String text;
        private final String[] keywords;
        private final String tip;

        InterviewQuestion(String text, String[] keywords, String tip) {
            this.text = text;
            this.keywords = keywords;
            this.tip = tip;
        }

        public String getText() {
            return text;
        }

        public String[] getKeywords() {
            return keywords;
        }

        public String getTip() {
            return tip;
        }
    }

    private static final class AnswerRecord {
        private String sessionId;
        private final String question;
        private final String answer;
        private final int score;
        private final int maxScore;
        private final String tip;

        AnswerRecord(String question, String answer, int score, int maxScore, String tip) {
            this.question = question;
            this.answer = answer;
            this.score = score;
            this.maxScore = maxScore;
            this.tip = tip;
        }

        AnswerRecord(String sessionId, String question, String answer, int score, int maxScore, String tip) {
            this.sessionId = sessionId;
            this.question = question;
            this.answer = answer;
            this.score = score;
            this.maxScore = maxScore;
            this.tip = tip;
        }

        public String getSessionId() {
            return sessionId;
        }

        public String getQuestion() {
            return question;
        }

        public String getAnswer() {
            return answer;
        }

        public int getScore() {
            return score;
        }

        public int getMaxScore() {
            return maxScore;
        }

        public String getTip() {
            return tip;
        }

        static AnswerRecord fromStorageLine(String line) {
            String[] parts = line.split("\\|", 8);
            if (parts.length < 7) {
                return null;
            }
            String tip = parts.length > 7 ? parts[7].replace("\\n", "\n") : "";
            return new AnswerRecord(
                    parts[1],
                    parts[2].replace("\\n", "\n"),
                    parts[3].replace("\\n", "\n"),
                    Integer.parseInt(parts[4]),
                    Integer.parseInt(parts[5]),
                    tip);
        }
    }

    private static final class InterviewResult {
        private final String sessionId;
        private final String mode;
        private final LocalDateTime finishedAt;
        private final int score;
        private final int maxScore;
        private final List<AnswerRecord> answers;

        InterviewResult(String sessionId, String mode, LocalDateTime finishedAt, int score, int maxScore,
                List<AnswerRecord> answers) {
            this.sessionId = sessionId;
            this.mode = mode;
            this.finishedAt = finishedAt;
            this.score = score;
            this.maxScore = maxScore;
            this.answers = answers;
        }

        public String getSessionId() {
            return sessionId;
        }

        public String getMode() {
            return mode;
        }

        public LocalDateTime getFinishedAt() {
            return finishedAt;
        }

        public int getScore() {
            return score;
        }

        public int getMaxScore() {
            return maxScore;
        }

        public List<AnswerRecord> getAnswers() {
            return answers;
        }

        public double getPercentage() {
            return maxScore == 0 ? 0 : (score * 100.0) / maxScore;
        }

        public String getGrade() {
            double pct = getPercentage();
            if (pct >= 90) {
                return "A — Excellent";
            }
            if (pct >= 75) {
                return "B — Strong";
            }
            if (pct >= 60) {
                return "C — Good effort";
            }
            if (pct >= 40) {
                return "D — Needs practice";
            }
            return "F — Keep preparing";
        }

        public String getListLabel() {
            return finishedAt.format(TIME_FORMAT) + " | " + mode + " | "
                    + score + "/" + maxScore + " (" + String.format("%.0f", getPercentage()) + "%)";
        }

        String toStorageHeader() {
            return "INTERVIEW|" + sessionId + "|" + finishedAt.format(TIME_FORMAT) + "|" + mode + "|" + score + "|"
                    + maxScore;
        }

        String toStorageAnswerLine(AnswerRecord record) {
            return "QA|" + sessionId + "|" + sanitize(record.getQuestion()) + "|"
                    + sanitize(record.getAnswer()) + "|" + record.getScore() + "|" + record.getMaxScore()
                    + "|" + sanitize(record.getTip());
        }

        static InterviewResult fromHeader(String line) {
            String[] parts = line.split("\\|", 6);
            if (parts.length != 6) {
                return null;
            }
            return new InterviewResult(
                    parts[1],
                    parts[3],
                    LocalDateTime.parse(parts[2], TIME_FORMAT),
                    Integer.parseInt(parts[4]),
                    Integer.parseInt(parts[5]),
                    new ArrayList<>());
        }
    }

    private static String sanitize(String value) {
        return value.replace("|", "/").replace("\n", "\\n").replace("\r", "");
    }

    private static final class QuestionBank {
        private QuestionBank() {
        }

        static List<InterviewQuestion> questionsFor(InterviewMode mode) {
            switch (mode) {
                case PYTHON:
                    return pythonQuestions();
                case HR:
                    return hrQuestions();
                case JAVA:
                default:
                    return javaQuestions();
            }
        }

        private static List<InterviewQuestion> javaQuestions() {
            return Arrays.asList(
                    new InterviewQuestion(
                            "What is the difference between JDK, JRE, and JVM?",
                            new String[] {"jdk", "jre", "jvm", "compile", "runtime"},
                            "JDK is for development, JRE runs programs, JVM executes bytecode."),
                    new InterviewQuestion(
                            "Explain OOP pillars in Java with a short example.",
                            new String[] {"encapsulation", "inheritance", "polymorphism", "abstraction", "class"},
                            "Mention four pillars and tie each to a Java feature like interfaces or overriding."),
                    new InterviewQuestion(
                            "What is the difference between == and equals() in Java?",
                            new String[] {"reference", "content", "equals", "override", "string"},
                            "== compares references; equals() compares object content when properly overridden."),
                    new InterviewQuestion(
                            "When would you use ArrayList vs LinkedList?",
                            new String[] {"arraylist", "linkedlist", "access", "insert", "performance"},
                            "ArrayList is better for random access; LinkedList for frequent insert/delete in the middle."),
                    new InterviewQuestion(
                            "What are checked and unchecked exceptions?",
                            new String[] {"checked", "unchecked", "compile", "runtime", "try"},
                            "Checked must be handled at compile time; unchecked extend RuntimeException."),
                    new InterviewQuestion(
                            "Explain Java memory areas: stack vs heap.",
                            new String[] {"stack", "heap", "method", "object", "garbage"},
                            "Stack holds method frames; heap stores objects managed by garbage collection."),
                    new InterviewQuestion(
                            "What is the purpose of the final keyword in Java?",
                            new String[] {"final", "variable", "method", "class", "inherit"},
                            "final prevents reassignment, overriding, or subclassing depending on context."),
                    new InterviewQuestion(
                            "How does Spring Boot simplify Java backend development?",
                            new String[] {"spring", "boot", "auto", "configuration", "starter"},
                            "Discuss auto-configuration, starters, embedded server, and rapid REST setup."));
        }

        private static List<InterviewQuestion> pythonQuestions() {
            return Arrays.asList(
                    new InterviewQuestion(
                            "List key differences between a list and a tuple in Python.",
                            new String[] {"mutable", "immutable", "list", "tuple", "performance"},
                            "Lists are mutable; tuples are immutable and often used for fixed data."),
                    new InterviewQuestion(
                            "What are list comprehensions and why use them?",
                            new String[] {"comprehension", "concise", "loop", "readable", "list"},
                            "They provide compact syntax to build lists from iterables."),
                    new InterviewQuestion(
                            "Explain how Python manages memory and garbage collection.",
                            new String[] {"reference", "counting", "garbage", "memory", "object"},
                            "Mention reference counting and cyclic garbage collector."),
                    new InterviewQuestion(
                            "What is the difference between a module and a package?",
                            new String[] {"module", "package", "import", "namespace", "__init__"},
                            "Module is a single file; package is a folder of modules with __init__.py."),
                    new InterviewQuestion(
                            "When would you use a dictionary vs a set?",
                            new String[] {"dictionary", "set", "key", "unique", "lookup"},
                            "Dict maps keys to values; set stores unique elements with fast membership tests."),
                    new InterviewQuestion(
                            "How do you handle exceptions in Python?",
                            new String[] {"try", "except", "finally", "raise", "exception"},
                            "Use try/except/finally and raise custom exceptions when needed."),
                    new InterviewQuestion(
                            "What are decorators in Python?",
                            new String[] {"decorator", "function", "wrap", "syntax", "@"},
                            "Decorators modify or extend behavior of functions/classes using @ syntax."),
                    new InterviewQuestion(
                            "Name popular Python libraries for data science and their use.",
                            new String[] {"numpy", "pandas", "matplotlib", "scikit", "data"},
                            "NumPy for arrays, Pandas for tables, Matplotlib/Seaborn for plots, scikit-learn for ML."));
        }

        private static List<InterviewQuestion> hrQuestions() {
            return Arrays.asList(
                    new InterviewQuestion(
                            "Tell me about yourself.",
                            new String[] {"background", "skills", "role", "achievement", "goal"},
                            "Use a 60–90 second pitch: present, past highlights, and why this role."),
                    new InterviewQuestion(
                            "Describe a challenge you faced and how you solved it.",
                            new String[] {"situation", "task", "action", "result", "learned"},
                            "Use the STAR method: Situation, Task, Action, Result."),
                    new InterviewQuestion(
                            "What is your greatest strength and weakness?",
                            new String[] {"strength", "weakness", "improve", "example", "honest"},
                            "Pick a real strength with proof and a weakness you are actively improving."),
                    new InterviewQuestion(
                            "Why do you want to join our company?",
                            new String[] {"mission", "culture", "role", "growth", "research"},
                            "Show you researched the company and align with their values and goals."),
                    new InterviewQuestion(
                            "Tell me about a time you worked in a team.",
                            new String[] {"team", "collaborate", "communicate", "conflict", "deliver"},
                            "Highlight collaboration, communication, and shared outcomes."),
                    new InterviewQuestion(
                            "How do you handle tight deadlines or pressure?",
                            new String[] {"prioritize", "plan", "communicate", "calm", "deadline"},
                            "Explain prioritization, planning, and proactive communication."),
                    new InterviewQuestion(
                            "Where do you see yourself in five years?",
                            new String[] {"growth", "skills", "impact", "career", "learn"},
                            "Balance ambition with commitment to developing relevant skills."),
                    new InterviewQuestion(
                            "Do you have any questions for us?",
                            new String[] {"team", "expectations", "growth", "projects", "culture"},
                            "Ask thoughtful questions about team, success metrics, and growth opportunities."));
        }
    }
}
