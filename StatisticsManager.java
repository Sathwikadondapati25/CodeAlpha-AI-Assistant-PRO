import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

public class StatisticsManager {
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private StatisticsManager() {
    }

    private static Path getChatPath(JFrame owner) {
        if (owner instanceof NovaAIFrame) {
            String username = ((NovaAIFrame) owner).getCurrentUser();
            return Paths.get("data", username, "chat_history.txt");
        }
        return Paths.get("data", "chat_history.txt");
    }

    private static Path getNotesPath(JFrame owner) {
        if (owner instanceof NovaAIFrame) {
            String username = ((NovaAIFrame) owner).getCurrentUser();
            return Paths.get("data", username, "notes.txt");
        }
        return Paths.get("data", "notes.txt");
    }

    private static Path getTasksPath(JFrame owner) {
        if (owner instanceof NovaAIFrame) {
            String username = ((NovaAIFrame) owner).getCurrentUser();
            return Paths.get("data", username, "study_tasks.txt");
        }
        return Paths.get("data", "study_tasks.txt");
    }

    private static Path getInterviewPath(JFrame owner) {
        if (owner instanceof NovaAIFrame) {
            String username = ((NovaAIFrame) owner).getCurrentUser();
            return Paths.get("data", username, "interview_history.txt");
        }
        return Paths.get("data", "interview_history.txt");
    }

    private static Path getProfilePath(JFrame owner) {
        if (owner instanceof NovaAIFrame) {
            String username = ((NovaAIFrame) owner).getCurrentUser();
            return Paths.get("data", username, "profile.properties");
        }
        return Paths.get("data", "user_profile.properties");
    }

    public static AppStatistics collect(JFrame owner) {
        AppStatistics stats = new AppStatistics();
        stats.setProfileName(loadProfileName(owner));

        Path chatPath = getChatPath(owner);
        Path notesPath = getNotesPath(owner);
        Path tasksPath = getTasksPath(owner);
        Path interviewPath = getInterviewPath(owner);

        if (Files.exists(chatPath)) {
            try {
                List<String> lines = Files.readAllLines(chatPath, StandardCharsets.UTF_8);
                for (String line : lines) {
                    if (line.startsWith("SESSION|")) {
                        stats.incrementChatSessions();
                        String[] parts = line.split("\\|", 6);
                        if (parts.length >= 4 && "true".equalsIgnoreCase(parts[3])) {
                            stats.incrementFavoriteChats();
                        }
                    } else if (line.contains("|user|")) {
                        stats.incrementUserMessages();
                    } else if (line.contains("|bot|")) {
                        stats.incrementBotMessages();
                    }
                }
            } catch (IOException ignored) {
            }
        }

        if (Files.exists(notesPath)) {
            try {
                for (String line : Files.readAllLines(notesPath, StandardCharsets.UTF_8)) {
                    if (line.startsWith("NOTE|")) {
                        stats.incrementNotes();
                    }
                }
            } catch (IOException ignored) {
            }
        }

        if (Files.exists(tasksPath)) {
            try {
                for (String line : Files.readAllLines(tasksPath, StandardCharsets.UTF_8)) {
                    if (line.startsWith("GOAL|")) {
                        stats.incrementStudyGoals();
                        String[] parts = line.split("\\|", 7);
                        if (parts.length >= 6 && "true".equalsIgnoreCase(parts[5])) {
                            stats.incrementCompletedGoals();
                        }
                    } else if (line.startsWith("EXAM|")) {
                        stats.incrementExams();
                    }
                }
            } catch (IOException ignored) {
            }
        }

        if (Files.exists(interviewPath)) {
            try {
                for (String line : Files.readAllLines(interviewPath, StandardCharsets.UTF_8)) {
                    if (line.startsWith("INTERVIEW|")) {
                        stats.incrementInterviews();
                        String[] parts = line.split("\\|");
                        if (parts.length >= 6) {
                            try {
                                int score = Integer.parseInt(parts[4]);
                                int max = Integer.parseInt(parts[5]);
                                stats.addInterviewScore(score, max);
                            } catch (NumberFormatException ignored) {
                            }
                        }
                    }
                }
            } catch (IOException ignored) {
            }
        }

        return stats;
    }

    public static void showDashboard(JFrame owner, ThemeManager.AppTheme theme) {
        AppStatistics stats = collect(owner);
        JDialog dialog = new JDialog(owner, "Statistics Dashboard", true);
        dialog.setSize(720, 520);
        dialog.setMinimumSize(new Dimension(640, 460));
        dialog.setLocationRelativeTo(owner);
        ThemeManager.styleDialog(dialog, theme);

        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        ThemeManager.applyRootBackground(root, theme);

        JLabel title = new JLabel("Nova AI — Usage Statistics", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        ThemeManager.styleText(title, theme);

        JPanel cards = new JPanel(new GridLayout(3, 3, 10, 10));
        cards.setOpaque(false);
        cards.add(statCard("Chat Sessions", String.valueOf(stats.getChatSessions()), theme));
        cards.add(statCard("Messages", stats.getUserMessages() + " user / " + stats.getBotMessages() + " bot", theme));
        cards.add(statCard("Favorite Chats", String.valueOf(stats.getFavoriteChats()), theme));
        cards.add(statCard("Notes", String.valueOf(stats.getNotes()), theme));
        cards.add(statCard("Study Goals", stats.getCompletedGoals() + " / " + stats.getStudyGoals() + " done", theme));
        cards.add(statCard("Exams Tracked", String.valueOf(stats.getExams()), theme));
        cards.add(statCard("Interviews", String.valueOf(stats.getInterviews()), theme));
        cards.add(statCard("Avg Interview", stats.getAverageInterviewPercent() + "%", theme));
        cards.add(statCard("Profile", stats.getProfileName(), theme));

        JTextArea details = new JTextArea(stats.toDetailedReport(owner));
        details.setEditable(false);
        details.setFont(new Font("Consolas", Font.PLAIN, 12));
        ThemeManager.styleTextArea(details, theme);
        details.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JPanel center = new JPanel(new BorderLayout(8, 8));
        center.setOpaque(false);
        center.setBorder(BorderFactory.createTitledBorder("Detailed Report"));
        ThemeManager.stylePanel(center, theme, false);
        center.add(new JScrollPane(details), BorderLayout.CENTER);

        JButton refreshButton = new JButton("Refresh");
        JButton closeButton = new JButton("Close");
        ThemeManager.styleButton(refreshButton, theme);
        ThemeManager.styleButton(closeButton, theme);
        refreshButton.addActionListener(e -> details.setText(collect(owner).toDetailedReport(owner)));
        closeButton.addActionListener(e -> dialog.dispose());

        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        JPanel buttons = new JPanel();
        buttons.setOpaque(false);
        buttons.add(refreshButton);
        buttons.add(closeButton);
        footer.add(buttons, BorderLayout.EAST);

        root.add(title, BorderLayout.NORTH);
        root.add(cards, BorderLayout.CENTER);
        root.add(center, BorderLayout.SOUTH);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(root, BorderLayout.CENTER);
        wrapper.add(footer, BorderLayout.SOUTH);

        dialog.setContentPane(wrapper);
        dialog.setVisible(true);
    }

    private static JPanel statCard(String label, String value, ThemeManager.AppTheme theme) {
        JPanel card = new JPanel(new BorderLayout(4, 4));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeManager.border(theme)),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));
        ThemeManager.stylePanel(card, theme, false);

        JLabel title = new JLabel(label, SwingConstants.CENTER);
        JLabel val = new JLabel(value, SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        val.setFont(new Font("Segoe UI", Font.BOLD, 18));
        ThemeManager.styleSecondaryText(title, theme);
        ThemeManager.styleText(val, theme);

        card.add(title, BorderLayout.NORTH);
        card.add(val, BorderLayout.CENTER);
        return card;
    }

    private static String loadProfileName(JFrame owner) {
        Path profilePath = getProfilePath(owner);
        if (!Files.exists(profilePath)) {
            return "Guest";
        }
        Properties props = new Properties();
        try {
            props.load(Files.newBufferedReader(profilePath, StandardCharsets.UTF_8));
            return props.getProperty("name", "Guest");
        } catch (IOException e) {
            return "Guest";
        }
    }

    public static final class AppStatistics {
        private String profileName = "Guest";
        private int chatSessions;
        private int userMessages;
        private int botMessages;
        private int favoriteChats;
        private int notes;
        private int studyGoals;
        private int completedGoals;
        private int exams;
        private int interviews;
        private int interviewScoreSum;
        private int interviewMaxSum;

        public String getProfileName() {
            return profileName;
        }

        public void setProfileName(String profileName) {
            this.profileName = profileName;
        }

        public int getChatSessions() {
            return chatSessions;
        }

        public void incrementChatSessions() {
            chatSessions++;
        }

        public int getUserMessages() {
            return userMessages;
        }

        public void incrementUserMessages() {
            userMessages++;
        }

        public int getBotMessages() {
            return botMessages;
        }

        public void incrementBotMessages() {
            botMessages++;
        }

        public int getFavoriteChats() {
            return favoriteChats;
        }

        public void incrementFavoriteChats() {
            favoriteChats++;
        }

        public int getNotes() {
            return notes;
        }

        public void incrementNotes() {
            notes++;
        }

        public int getStudyGoals() {
            return studyGoals;
        }

        public void incrementStudyGoals() {
            studyGoals++;
        }

        public int getCompletedGoals() {
            return completedGoals;
        }

        public void incrementCompletedGoals() {
            completedGoals++;
        }

        public int getExams() {
            return exams;
        }

        public void incrementExams() {
            exams++;
        }

        public int getInterviews() {
            return interviews;
        }

        public void incrementInterviews() {
            interviews++;
        }

        public void addInterviewScore(int score, int max) {
            interviewScoreSum += score;
            interviewMaxSum += max;
        }

        public String getAverageInterviewPercent() {
            if (interviewMaxSum == 0) {
                return "0";
            }
            return String.format("%.0f", (interviewScoreSum * 100.0) / interviewMaxSum);
        }

        public String toDetailedReport(JFrame owner) {
            StringBuilder report = new StringBuilder();
            report.append("NOVA AI ASSISTANT PRO — STATISTICS REPORT\n");
            report.append("Generated: ").append(LocalDateTime.now().format(TIME_FORMAT)).append("\n");
            report.append(repeat('=', 50)).append("\n\n");
            report.append("User: ").append(profileName).append("\n");
            report.append("Theme-aware dashboard from local data files.\n\n");

            report.append("[ CHAT ]\n");
            report.append("  Sessions: ").append(chatSessions).append("\n");
            report.append("  User messages: ").append(userMessages).append("\n");
            report.append("  Bot messages: ").append(botMessages).append("\n");
            report.append("  Favorite chats: ").append(favoriteChats).append("\n");
            report.append("  Total messages: ").append(userMessages + botMessages).append("\n\n");

            report.append("[ NOTES ]\n");
            report.append("  Saved notes: ").append(notes).append("\n\n");

            report.append("[ STUDY PLANNER ]\n");
            report.append("  Goals: ").append(studyGoals).append("\n");
            report.append("  Completed: ").append(completedGoals).append("\n");
            report.append("  Completion rate: ")
                    .append(studyGoals == 0 ? "0" : String.format("%.0f", completedGoals * 100.0 / studyGoals))
                    .append("%\n");
            report.append("  Exams scheduled: ").append(exams).append("\n\n");

            report.append("[ INTERVIEW SIMULATOR ]\n");
            report.append("  Sessions completed: ").append(interviews).append("\n");
            report.append("  Average score: ").append(getAverageInterviewPercent()).append("%\n\n");

            report.append("[ STORAGE FILES ]\n");
            report.append("  ").append(getChatPath(owner).toString().replace('\\', '/')).append("\n");
            report.append("  ").append(getNotesPath(owner).toString().replace('\\', '/')).append("\n");
            report.append("  ").append(getTasksPath(owner).toString().replace('\\', '/')).append("\n");
            report.append("  ").append(getInterviewPath(owner).toString().replace('\\', '/')).append("\n");
            report.append("  ").append(getProfilePath(owner).toString().replace('\\', '/')).append("\n");
            return report.toString();
        }

        private static String repeat(char ch, int count) {
            StringBuilder b = new StringBuilder(count);
            for (int i = 0; i < count; i++) {
                b.append(ch);
            }
            return b.toString();
        }
    }
}
