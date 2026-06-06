import java.awt.BorderLayout;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class StudyPlanner extends JDialog {
    private final Path tasksPath;
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final List<StudyTask> tasks = new ArrayList<>();
    private final List<ExamEntry> exams = new ArrayList<>();

    private final DefaultListModel<String> dailyListModel = new DefaultListModel<>();
    private final JList<String> dailyList = new JList<>(dailyListModel);
    private final DefaultListModel<String> examListModel = new DefaultListModel<>();
    private final JList<String> examList = new JList<>(examListModel);

    private final JLabel countdownLabel = new JLabel(" ");
    private final JProgressBar progressBar = new JProgressBar(0, 100);
    private final JLabel progressLabel = new JLabel(" ");

    private final JTextField goalTitleField = new JTextField();
    private final JTextArea goalDetailsArea = new JTextArea(3, 24);
    private final JTextField goalDueField = new JTextField(LocalDate.now().format(DATE_FORMAT));

    private final JTextField examNameField = new JTextField();
    private final JTextField examSubjectField = new JTextField();
    private final JTextField examDateField = new JTextField();

    public StudyPlanner(JFrame owner) {
        super(owner, "Study Planner", true);
        this.tasksPath = getTasksPath(owner);
        setMinimumSize(new Dimension(960, 620));
        setSize(1000, 660);
        setLocationRelativeTo(owner);

        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBackground(ThemeManager.LIGHT_BG);
        root.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        setContentPane(root);

        root.add(buildProgressPanel(), BorderLayout.NORTH);
        root.add(buildTabs(), BorderLayout.CENTER);
        root.add(buildFooter(), BorderLayout.SOUTH);

        loadData();
        refreshAll();
    }

    private static Path getTasksPath(JFrame owner) {
        if (owner instanceof NovaAIFrame) {
            String username = ((NovaAIFrame) owner).getCurrentUser();
            return Paths.get("data", username, "study_tasks.txt");
        }
        return Paths.get("data", "study_tasks.txt");
    }

    public static void showPlanner(JFrame owner) {
        StudyPlanner dialog = new StudyPlanner(owner);
        dialog.setVisible(true);
    }

    private JPanel buildProgressPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder("Progress Tracking"));
        ThemeManager.stylePanel(panel, false, true);

        progressBar.setStringPainted(true);
        progressLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        countdownLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        ThemeManager.styleText(progressLabel, false);
        ThemeManager.styleText(countdownLabel, false);

        panel.add(progressLabel);
        panel.add(progressBar);
        panel.add(countdownLabel);
        return panel;
    }

    private JTabbedPane buildTabs() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Daily Goals", buildDailyGoalsPanel());
        tabs.addTab("Exam Countdown", buildExamPanel());
        return tabs;
    }

    private JPanel buildDailyGoalsPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        ThemeManager.stylePanel(panel, false, false);

        JPanel form = new JPanel(new GridLayout(0, 1, 6, 6));
        form.setBorder(BorderFactory.createTitledBorder("Add Daily Goal"));
        ThemeManager.stylePanel(form, false, true);

        JLabel titleLabel = new JLabel("Goal title");
        JLabel detailsLabel = new JLabel("Details (optional)");
        JLabel dueLabel = new JLabel("Due date (yyyy-MM-dd)");
        ThemeManager.styleText(titleLabel, false);
        ThemeManager.styleText(detailsLabel, false);
        ThemeManager.styleText(dueLabel, false);
        ThemeManager.styleInput(goalTitleField, false);
        goalDetailsArea.setLineWrap(true);
        goalDetailsArea.setWrapStyleWord(true);
        ThemeManager.styleInput(goalDueField, false);

        form.add(titleLabel);
        form.add(goalTitleField);
        form.add(detailsLabel);
        form.add(new JScrollPane(goalDetailsArea));
        form.add(dueLabel);
        form.add(goalDueField);

        JButton addGoalButton = new JButton("Add Goal");
        ThemeManager.styleButton(addGoalButton, false);
        addGoalButton.addActionListener(e -> addDailyGoal());
        form.add(addGoalButton);

        JPanel listPanel = new JPanel(new BorderLayout());
        listPanel.setBorder(BorderFactory.createTitledBorder("Today's & Upcoming Goals"));
        dailyList.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        listPanel.add(new JScrollPane(dailyList), BorderLayout.CENTER);

        JPanel listActions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        listActions.setOpaque(false);
        JButton completeButton = new JButton("Mark Complete");
        JButton deleteGoalButton = new JButton("Delete Goal");
        ThemeManager.styleButton(completeButton, false);
        ThemeManager.styleButton(deleteGoalButton, false);
        completeButton.addActionListener(e -> toggleSelectedGoal());
        deleteGoalButton.addActionListener(e -> deleteSelectedGoal());
        listActions.add(completeButton);
        listActions.add(deleteGoalButton);
        listPanel.add(listActions, BorderLayout.SOUTH);

        JPanel split = new JPanel(new GridLayout(1, 2, 10, 0));
        split.setOpaque(false);
        split.add(form);
        split.add(listPanel);

        panel.add(split, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildExamPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        ThemeManager.stylePanel(panel, false, false);

        JPanel form = new JPanel(new GridLayout(0, 1, 6, 6));
        form.setBorder(BorderFactory.createTitledBorder("Add Exam"));
        ThemeManager.stylePanel(form, false, true);

        JLabel nameLabel = new JLabel("Exam name");
        JLabel subjectLabel = new JLabel("Subject");
        JLabel dateLabel = new JLabel("Exam date (yyyy-MM-dd)");
        ThemeManager.styleText(nameLabel, false);
        ThemeManager.styleText(subjectLabel, false);
        ThemeManager.styleText(dateLabel, false);
        ThemeManager.styleInput(examNameField, false);
        ThemeManager.styleInput(examSubjectField, false);
        ThemeManager.styleInput(examDateField, false);
        examDateField.setText(LocalDate.now().plusDays(30).format(DATE_FORMAT));

        form.add(nameLabel);
        form.add(examNameField);
        form.add(subjectLabel);
        form.add(examSubjectField);
        form.add(dateLabel);
        form.add(examDateField);

        JButton addExamButton = new JButton("Add Exam");
        ThemeManager.styleButton(addExamButton, false);
        addExamButton.addActionListener(e -> addExam());
        form.add(addExamButton);

        JPanel listPanel = new JPanel(new BorderLayout());
        listPanel.setBorder(BorderFactory.createTitledBorder("Scheduled Exams"));
        examList.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        examList.addListSelectionListener(e -> updateExamCountdown());
        listPanel.add(new JScrollPane(examList), BorderLayout.CENTER);

        JPanel listActions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        listActions.setOpaque(false);
        JButton deleteExamButton = new JButton("Delete Exam");
        ThemeManager.styleButton(deleteExamButton, false);
        deleteExamButton.addActionListener(e -> deleteSelectedExam());
        listActions.add(deleteExamButton);
        listPanel.add(listActions, BorderLayout.SOUTH);

        JPanel split = new JPanel(new GridLayout(1, 2, 10, 0));
        split.setOpaque(false);
        split.add(form);
        split.add(listPanel);

        panel.add(split, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildFooter() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bar.setOpaque(false);
        JButton refreshButton = new JButton("Refresh");
        JButton closeButton = new JButton("Close");
        ThemeManager.styleButton(refreshButton, false);
        ThemeManager.styleButton(closeButton, false);
        refreshButton.addActionListener(e -> refreshAll());
        closeButton.addActionListener(e -> dispose());
        bar.add(refreshButton);
        bar.add(closeButton);
        return bar;
    }

    private void loadData() {
        tasks.clear();
        exams.clear();
        if (!Files.exists(tasksPath)) {
            return;
        }
        try {
            for (String line : Files.readAllLines(tasksPath, StandardCharsets.UTF_8)) {
                if (line.startsWith("GOAL|")) {
                    StudyTask task = StudyTask.fromStorageLine(line);
                    if (task != null) {
                        tasks.add(task);
                    }
                } else if (line.startsWith("EXAM|")) {
                    ExamEntry exam = ExamEntry.fromStorageLine(line);
                    if (exam != null) {
                        exams.add(exam);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void persistData() {
        try {
            Path parent = tasksPath.getParent();
            if (parent != null && !Files.exists(parent)) {
                Files.createDirectories(parent);
            }
            try (BufferedWriter writer = Files.newBufferedWriter(
                    tasksPath, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                for (StudyTask task : tasks) {
                    writer.write(task.toStorageLine());
                    writer.newLine();
                }
                for (ExamEntry exam : exams) {
                    writer.write(exam.toStorageLine());
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Failed to save: " + e.getMessage(),
                    "Save Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshAll() {
        refreshDailyList();
        refreshExamList();
        refreshProgress();
        updateExamCountdown();
    }

    private void refreshDailyList() {
        dailyListModel.clear();
        LocalDate today = LocalDate.now();
        List<StudyTask> sorted = tasks.stream()
                .sorted(Comparator.comparing(StudyTask::getDueDate).thenComparing(StudyTask::isCompleted))
                .collect(Collectors.toList());
        for (StudyTask task : sorted) {
            String prefix = task.isCompleted() ? "[x] " : "[ ] ";
            String dueTag = task.getDueDate().equals(today) ? " (today)" : " (due " + task.getDueDate() + ")";
            dailyListModel.addElement(prefix + task.getTitle() + dueTag);
        }
    }

    private void refreshExamList() {
        examListModel.clear();
        exams.stream()
                .sorted(Comparator.comparing(ExamEntry::getExamDate))
                .forEach(exam -> {
                    long days = ChronoUnit.DAYS.between(LocalDate.now(), exam.getExamDate());
                    examListModel.addElement(exam.getName() + " — " + exam.getSubject() + " | "
                            + exam.getExamDate() + " (" + days + " days)");
                });
    }

    private void refreshProgress() {
        long total = tasks.size();
        long done = tasks.stream().filter(StudyTask::isCompleted).count();
        long todayTotal = tasks.stream().filter(t -> t.getDueDate().equals(LocalDate.now())).count();
        long todayDone = tasks.stream()
                .filter(t -> t.getDueDate().equals(LocalDate.now()) && t.isCompleted())
                .count();

        int percent = total == 0 ? 0 : (int) Math.round((done * 100.0) / total);
        progressBar.setValue(percent);
        progressBar.setString(percent + "% complete");
        progressLabel.setText("Overall: " + done + " / " + total + " goals completed"
                + "  |  Today: " + todayDone + " / " + todayTotal);
    }

    private void updateExamCountdown() {
        List<ExamEntry> sorted = exams.stream()
                .sorted(Comparator.comparing(ExamEntry::getExamDate))
                .collect(Collectors.toList());
        int idx = examList.getSelectedIndex();
        if (idx >= 0 && idx < sorted.size()) {
            showCountdown(sorted.get(idx));
            return;
        }
        ExamEntry nearest = exams.stream()
                .filter(e -> !e.getExamDate().isBefore(LocalDate.now()))
                .min(Comparator.comparing(ExamEntry::getExamDate))
                .orElse(null);
        if (nearest == null) {
            countdownLabel.setText("No upcoming exams — add one in the Exam Countdown tab.");
            return;
        }
        showCountdown(nearest);
    }

    private void showCountdown(ExamEntry exam) {
        long days = ChronoUnit.DAYS.between(LocalDate.now(), exam.getExamDate());
        if (days < 0) {
            countdownLabel.setText(exam.getName() + " was " + Math.abs(days) + " day(s) ago.");
        } else if (days == 0) {
            countdownLabel.setText("Exam today: " + exam.getName() + " (" + exam.getSubject() + ")");
        } else {
            countdownLabel.setText("Countdown: " + days + " day(s) until " + exam.getName()
                    + " (" + exam.getSubject() + ") on " + exam.getExamDate());
        }
    }

    private void addDailyGoal() {
        String title = goalTitleField.getText().trim();
        if (title.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter a goal title.", "Daily Goal", JOptionPane.WARNING_MESSAGE);
            return;
        }
        LocalDate due;
        try {
            due = LocalDate.parse(goalDueField.getText().trim(), DATE_FORMAT);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Invalid due date. Use yyyy-MM-dd.", "Daily Goal",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        StudyTask task = new StudyTask(
                "goal-" + UUID.randomUUID().toString().substring(0, 8),
                title,
                goalDetailsArea.getText().trim(),
                due,
                false,
                LocalDateTime.now());
        tasks.add(task);
        goalTitleField.setText("");
        goalDetailsArea.setText("");
        goalDueField.setText(LocalDate.now().format(DATE_FORMAT));
        persistData();
        refreshAll();
    }

    private void toggleSelectedGoal() {
        StudyTask task = getSelectedTask();
        if (task == null) {
            JOptionPane.showMessageDialog(this, "Select a goal first.", "Daily Goals", JOptionPane.WARNING_MESSAGE);
            return;
        }
        task.setCompleted(!task.isCompleted());
        persistData();
        refreshAll();
    }

    private void deleteSelectedGoal() {
        StudyTask task = getSelectedTask();
        if (task == null) {
            return;
        }
        int choice = JOptionPane.showConfirmDialog(this, "Delete this goal?", "Delete Goal", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            tasks.remove(task);
            persistData();
            refreshAll();
        }
    }

    private StudyTask getSelectedTask() {
        int idx = dailyList.getSelectedIndex();
        if (idx < 0) {
            return null;
        }
        List<StudyTask> sorted = tasks.stream()
                .sorted(Comparator.comparing(StudyTask::getDueDate).thenComparing(StudyTask::isCompleted))
                .collect(Collectors.toList());
        return idx < sorted.size() ? sorted.get(idx) : null;
    }

    private void addExam() {
        String name = examNameField.getText().trim();
        String subject = examSubjectField.getText().trim();
        if (name.isEmpty() || subject.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter exam name and subject.", "Exam", JOptionPane.WARNING_MESSAGE);
            return;
        }
        LocalDate examDate;
        try {
            examDate = LocalDate.parse(examDateField.getText().trim(), DATE_FORMAT);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Invalid exam date. Use yyyy-MM-dd.", "Exam",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        ExamEntry exam = new ExamEntry(
                "exam-" + UUID.randomUUID().toString().substring(0, 8),
                name,
                subject,
                examDate,
                LocalDateTime.now());
        exams.add(exam);
        examNameField.setText("");
        examSubjectField.setText("");
        examDateField.setText(LocalDate.now().plusDays(30).format(DATE_FORMAT));
        persistData();
        refreshAll();
    }

    private void deleteSelectedExam() {
        int idx = examList.getSelectedIndex();
        if (idx < 0) {
            return;
        }
        List<ExamEntry> sorted = exams.stream()
                .sorted(Comparator.comparing(ExamEntry::getExamDate))
                .collect(Collectors.toList());
        if (idx >= sorted.size()) {
            return;
        }
        ExamEntry exam = sorted.get(idx);
        int choice = JOptionPane.showConfirmDialog(this, "Delete exam \"" + exam.getName() + "\"?",
                "Delete Exam", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            exams.remove(exam);
            persistData();
            refreshAll();
        }
    }

    private static final class StudyTask {
        private final String id;
        private final String title;
        private final String details;
        private final LocalDate dueDate;
        private boolean completed;
        private final LocalDateTime createdAt;

        StudyTask(String id, String title, String details, LocalDate dueDate, boolean completed,
                LocalDateTime createdAt) {
            this.id = id;
            this.title = title;
            this.details = details == null ? "" : details;
            this.dueDate = dueDate;
            this.completed = completed;
            this.createdAt = createdAt;
        }

        public String getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

        public String getDetails() {
            return details;
        }

        public LocalDate getDueDate() {
            return dueDate;
        }

        public boolean isCompleted() {
            return completed;
        }

        public void setCompleted(boolean completed) {
            this.completed = completed;
        }

        String toStorageLine() {
            return "GOAL|" + id + "|" + sanitize(title) + "|" + sanitize(details) + "|"
                    + dueDate.format(DATE_FORMAT) + "|" + completed + "|" + createdAt.format(TIME_FORMAT);
        }

        static StudyTask fromStorageLine(String line) {
            String[] parts = line.split("\\|", 7);
            if (parts.length != 7) {
                return null;
            }
            return new StudyTask(
                    parts[1],
                    parts[2].replace("\\n", "\n"),
                    parts[3].replace("\\n", "\n"),
                    LocalDate.parse(parts[4], DATE_FORMAT),
                    Boolean.parseBoolean(parts[5]),
                    LocalDateTime.parse(parts[6], TIME_FORMAT));
        }

        private static String sanitize(String value) {
            return value.replace("|", "/").replace("\n", "\\n").replace("\r", "");
        }
    }

    private static final class ExamEntry {
        private final String id;
        private final String name;
        private final String subject;
        private final LocalDate examDate;
        private final LocalDateTime createdAt;

        ExamEntry(String id, String name, String subject, LocalDate examDate, LocalDateTime createdAt) {
            this.id = id;
            this.name = name;
            this.subject = subject;
            this.examDate = examDate;
            this.createdAt = createdAt;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getSubject() {
            return subject;
        }

        public LocalDate getExamDate() {
            return examDate;
        }

        String toStorageLine() {
            return "EXAM|" + id + "|" + sanitize(name) + "|" + sanitize(subject) + "|"
                    + examDate.format(DATE_FORMAT) + "|" + createdAt.format(TIME_FORMAT);
        }

        static ExamEntry fromStorageLine(String line) {
            String[] parts = line.split("\\|", 6);
            if (parts.length != 6) {
                return null;
            }
            return new ExamEntry(
                    parts[1],
                    parts[2].replace("\\n", "\n"),
                    parts[3].replace("\\n", "\n"),
                    LocalDate.parse(parts[4], DATE_FORMAT),
                    LocalDateTime.parse(parts[5], TIME_FORMAT));
        }

        private static String sanitize(String value) {
            return value.replace("|", "/").replace("\n", "\\n").replace("\r", "");
        }
    }
}
