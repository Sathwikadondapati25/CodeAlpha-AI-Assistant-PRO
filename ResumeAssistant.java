import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

public class ResumeAssistant extends JDialog {
    private static final Path DEFAULT_EXPORT_DIR = Paths.get("data", "resumes");
    private static final DateTimeFormatter FILE_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final JTextField nameField = new JTextField();
    private final JTextArea educationArea = new JTextArea(4, 28);
    private final JTextArea skillsArea = new JTextArea(4, 28);
    private final JTextArea projectsArea = new JTextArea(6, 28);
    private final JTextArea previewArea = new JTextArea(22, 50);

    public ResumeAssistant(JFrame owner) {
        super(owner, "Resume Assistant", true);
        setMinimumSize(new Dimension(980, 640));
        setSize(1020, 680);
        setLocationRelativeTo(owner);
        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBackground(ThemeManager.LIGHT_BG);
        root.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        setContentPane(root);

        root.add(buildFormPanel(), BorderLayout.WEST);
        root.add(buildPreviewPanel(), BorderLayout.CENTER);
        root.add(buildButtonBar(), BorderLayout.SOUTH);

        prefillFromProfile(owner);
    }

    public static void showAssistant(JFrame owner) {
        ResumeAssistant dialog = new ResumeAssistant(owner);
        dialog.setVisible(true);
    }

    private JPanel buildFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setPreferredSize(new Dimension(360, 0));
        panel.setBorder(BorderFactory.createTitledBorder("Your Details"));
        ThemeManager.stylePanel(panel, false, true);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(4, 8, 4, 8);

        Font labelFont = new Font("Segoe UI", Font.BOLD, 13);
        Font fieldFont = new Font("Segoe UI", Font.PLAIN, 13);

        int row = 0;
        gbc.gridy = row++;
        panel.add(label("Full Name *", labelFont), gbc);
        gbc.gridy = row++;
        styleField(nameField, fieldFont);
        panel.add(nameField, gbc);

        gbc.gridy = row++;
        panel.add(label("Education", labelFont), gbc);
        gbc.gridy = row++;
        styleArea(educationArea, fieldFont,
                "B.Tech Computer Science — XYZ University (2022–2026)\nCGPA: 8.5 / 10");
        panel.add(wrap(educationArea), gbc);

        gbc.gridy = row++;
        panel.add(label("Skills", labelFont), gbc);
        gbc.gridy = row++;
        styleArea(skillsArea, fieldFont,
                "Java, Python, SQL, Git, REST APIs, Problem Solving");
        panel.add(wrap(skillsArea), gbc);

        gbc.gridy = row++;
        panel.add(label("Projects", labelFont), gbc);
        gbc.gridy = row++;
        styleArea(projectsArea, fieldFont,
                "Nova AI Assistant PRO — Java Swing chatbot with file-based history\n"
                        + "E-Commerce API — Spring Boot backend with JWT authentication");
        panel.add(wrap(projectsArea), gbc);

        return panel;
    }

    private JPanel buildPreviewPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Resume Preview"));
        ThemeManager.stylePanel(panel, false, false);

        previewArea.setEditable(false);
        previewArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        previewArea.setLineWrap(true);
        previewArea.setWrapStyleWord(true);
        previewArea.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JScrollPane scroll = new JScrollPane(previewArea);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildButtonBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        ThemeManager.stylePanel(bar, false, false);

        JButton generateButton = new JButton("Generate Resume");
        JButton exportTxtButton = new JButton("Export TXT");
        JButton exportPdfButton = new JButton("Export PDF");
        JButton closeButton = new JButton("Close");

        ThemeManager.styleButton(generateButton, false);
        ThemeManager.styleButton(exportTxtButton, false);
        ThemeManager.styleButton(exportPdfButton, false);
        ThemeManager.styleButton(closeButton, false);

        generateButton.addActionListener(e -> generatePreview());
        exportTxtButton.addActionListener(e -> exportResume("txt"));
        exportPdfButton.addActionListener(e -> exportResume("pdf"));
        closeButton.addActionListener(e -> dispose());

        bar.add(generateButton);
        bar.add(exportTxtButton);
        bar.add(exportPdfButton);
        bar.add(closeButton);
        return bar;
    }

    private void prefillFromProfile(JFrame owner) {
        if (owner instanceof NovaAIFrame) {
            String profileName = ((NovaAIFrame) owner).getProfileName();
            if (profileName != null && !profileName.trim().isEmpty()) {
                nameField.setText(profileName.trim());
            }
        }
    }

    private void generatePreview() {
        if (!validateName()) {
            return;
        }
        previewArea.setText(buildResumeText(
                nameField.getText().trim(),
                educationArea.getText(),
                skillsArea.getText(),
                projectsArea.getText()));
        previewArea.setCaretPosition(0);
    }

    private boolean validateName() {
        if (nameField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter your name.", "Missing Name",
                    JOptionPane.WARNING_MESSAGE);
            nameField.requestFocus();
            return false;
        }
        return true;
    }

    private String currentResumeText() {
        String text = previewArea.getText();
        if (text == null || text.trim().isEmpty()) {
            if (!validateName()) {
                return null;
            }
            text = buildResumeText(
                    nameField.getText().trim(),
                    educationArea.getText(),
                    skillsArea.getText(),
                    projectsArea.getText());
            previewArea.setText(text);
        }
        return text;
    }

    private void exportResume(String format) {
        String resumeText = currentResumeText();
        if (resumeText == null) {
            return;
        }

        try {
            Files.createDirectories(DEFAULT_EXPORT_DIR);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Could not create export folder: " + e.getMessage(),
                    "Export Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String safeName = sanitizeFileName(nameField.getText().trim());
        String date = LocalDate.now().format(FILE_DATE);
        String defaultName = safeName + "_Resume_" + date + ("pdf".equals(format) ? ".pdf" : ".txt");
        Path defaultPath = DEFAULT_EXPORT_DIR.resolve(defaultName);

        JFileChooser chooser = new JFileChooser(DEFAULT_EXPORT_DIR.toFile());
        chooser.setSelectedFile(defaultPath.toFile());
        if ("pdf".equals(format)) {
            chooser.setFileFilter(new FileNameExtensionFilter("PDF Documents (*.pdf)", "pdf"));
        } else {
            chooser.setFileFilter(new FileNameExtensionFilter("Text Files (*.txt)", "txt"));
        }

        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        Path target = chooser.getSelectedFile().toPath();
        target = ensureExtension(target, format);

        try {
            if ("pdf".equals(format)) {
                writePdf(target, resumeText);
            } else {
                Files.write(target, resumeText.getBytes(StandardCharsets.UTF_8));
            }
            JOptionPane.showMessageDialog(this,
                    "Resume exported successfully:\n" + target.toAbsolutePath(),
                    "Export Complete",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Export failed: " + e.getMessage(),
                    "Export Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static String buildResumeText(String name, String education, String skills, String projects) {
        String divider = repeat('=', 72);
        String thinDivider = repeat('-', 72);
        StringBuilder resume = new StringBuilder();

        resume.append(divider).append('\n');
        resume.append(centerText(name.toUpperCase(), 72)).append('\n');
        resume.append(centerText("Professional Resume", 72)).append('\n');
        resume.append(divider).append("\n\n");

        resume.append("PROFILE SUMMARY\n");
        resume.append(thinDivider).append('\n');
        resume.append(wrapParagraph(
                "Motivated professional with strengths in "
                        + summarizeSkills(skills)
                        + ". Seeking opportunities to apply technical skills, deliver quality work, and grow with a collaborative team.",
                72)).append("\n\n");

        resume.append("EDUCATION\n");
        resume.append(thinDivider).append('\n');
        resume.append(formatSectionBody(education, "Add your degree, institution, and graduation timeline."))
                .append("\n\n");

        resume.append("TECHNICAL SKILLS\n");
        resume.append(thinDivider).append('\n');
        resume.append(formatSkills(skills)).append("\n\n");

        resume.append("PROJECTS\n");
        resume.append(thinDivider).append('\n');
        resume.append(formatProjects(projects)).append("\n\n");

        resume.append("ADDITIONAL INFORMATION\n");
        resume.append(thinDivider).append('\n');
        resume.append("• Available for full-time roles and internships\n");
        resume.append("• Open to relocation and remote collaboration\n");
        resume.append("• References available upon request\n\n");

        resume.append(divider).append('\n');
        resume.append(centerText("Generated by Nova AI Assistant PRO — Resume Assistant", 72)).append('\n');
        resume.append(divider).append('\n');

        return resume.toString();
    }

    private static String formatSectionBody(String raw, String placeholder) {
        if (raw == null || raw.trim().isEmpty()) {
            return placeholder;
        }
        return formatBulletLines(raw);
    }

    private static String formatSkills(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return "List your core technical and soft skills.";
        }
        String trimmed = raw.trim();
        if (trimmed.contains("\n")) {
            return formatBulletLines(trimmed);
        }
        String[] parts = trimmed.split(",");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            String skill = part.trim();
            if (!skill.isEmpty()) {
                builder.append("• ").append(skill).append('\n');
            }
        }
        return builder.length() == 0 ? "• " + trimmed : builder.toString().trim();
    }

    private static String formatProjects(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return "• Describe project name, tech stack, and measurable impact.";
        }
        return formatBulletLines(raw);
    }

    private static String formatBulletLines(String raw) {
        StringBuilder builder = new StringBuilder();
        for (String line : raw.split("\\R")) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            if (trimmed.startsWith("•") || trimmed.startsWith("-")) {
                builder.append(trimmed.startsWith("-") ? "•" + trimmed.substring(1) : trimmed).append('\n');
            } else if (trimmed.contains(" — ") || trimmed.contains(" - ")) {
                builder.append("• ").append(trimmed).append('\n');
            } else {
                builder.append("• ").append(trimmed).append('\n');
            }
        }
        return builder.length() == 0 ? raw.trim() : builder.toString().trim();
    }

    private static String summarizeSkills(String skills) {
        if (skills == null || skills.trim().isEmpty()) {
            return "software development and problem solving";
        }
        String flat = skills.replace('\n', ',').trim();
        String[] parts = flat.split(",");
        List<String> picked = new ArrayList<>();
        for (String part : parts) {
            String skill = part.trim();
            if (!skill.isEmpty()) {
                picked.add(skill);
            }
            if (picked.size() >= 4) {
                break;
            }
        }
        if (picked.isEmpty()) {
            return "software development and problem solving";
        }
        if (picked.size() == 1) {
            return picked.get(0);
        }
        StringBuilder summary = new StringBuilder();
        for (int i = 0; i < picked.size(); i++) {
            if (i > 0 && i == picked.size() - 1) {
                summary.append(" and ");
            } else if (i > 0) {
                summary.append(", ");
            }
            summary.append(picked.get(i));
        }
        return summary.toString();
    }

    private static String wrapParagraph(String text, int width) {
        String[] words = text.split("\\s+");
        StringBuilder line = new StringBuilder();
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (line.length() + word.length() + 1 > width) {
                result.append(line).append('\n');
                line = new StringBuilder(word);
            } else if (line.length() == 0) {
                line.append(word);
            } else {
                line.append(' ').append(word);
            }
        }
        if (line.length() > 0) {
            result.append(line);
        }
        return result.toString();
    }

    private static String centerText(String text, int width) {
        if (text.length() >= width) {
            return text;
        }
        int pad = (width - text.length()) / 2;
        return repeat(' ', pad) + text;
    }

    private static String repeat(char ch, int count) {
        StringBuilder builder = new StringBuilder(count);
        for (int i = 0; i < count; i++) {
            builder.append(ch);
        }
        return builder.toString();
    }

    private static String sanitizeFileName(String name) {
        return name.replaceAll("[\\\\/:*?\"<>|]", "_").replaceAll("\\s+", "_");
    }

    private static Path ensureExtension(Path path, String format) {
        String fileName = path.getFileName().toString().toLowerCase();
        if ("pdf".equals(format) && !fileName.endsWith(".pdf")) {
            return path.resolveSibling(path.getFileName().toString() + ".pdf");
        }
        if ("txt".equals(format) && !fileName.endsWith(".txt")) {
            return path.resolveSibling(path.getFileName().toString() + ".txt");
        }
        return path;
    }

    private static void writePdf(Path path, String text) throws IOException {
        List<String> lines = new ArrayList<>();
        for (String line : text.split("\\R", -1)) {
            lines.add(line.isEmpty() ? " " : line);
        }

        StringBuilder content = new StringBuilder();
        content.append("BT\n/F1 11 Tf\n");
        float startY = 750f;
        float lineHeight = 13f;
        float marginX = 50f;
        float bottomMargin = 50f;
        float y = startY;

        content.append(marginX).append(' ').append(startY).append(" Td\n");
        boolean firstLine = true;
        for (String line : lines) {
            if (y < bottomMargin) {
                throw new IOException("Resume content is too long for a single-page PDF. Shorten sections or export as TXT.");
            }
            if (!firstLine) {
                content.append("0 ").append(-lineHeight).append(" Td\n");
            }
            firstLine = false;
            content.append('(').append(escapePdfText(line)).append(") Tj\n");
            y -= lineHeight;
        }
        content.append("ET");

        byte[] streamBytes = content.toString().getBytes(StandardCharsets.US_ASCII);
        String stream = new String(streamBytes, StandardCharsets.US_ASCII);

        StringBuilder pdf = new StringBuilder();
        List<Integer> offsets = new ArrayList<>();
        pdf.append("%PDF-1.4\n");

        offsets.add(pdf.length());
        pdf.append("1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n");

        offsets.add(pdf.length());
        pdf.append("2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n");

        offsets.add(pdf.length());
        pdf.append("3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] ")
                .append("/Contents 4 0 R /Resources << /Font << /F1 5 0 R >> >> >>\nendobj\n");

        offsets.add(pdf.length());
        pdf.append("4 0 obj\n<< /Length ").append(streamBytes.length).append(" >>\nstream\n")
                .append(stream).append("\nendstream\nendobj\n");

        offsets.add(pdf.length());
        pdf.append("5 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>\nendobj\n");

        int xrefStart = pdf.length();
        pdf.append("xref\n0 ").append(offsets.size() + 1).append('\n');
        pdf.append("0000000000 65535 f \n");
        for (int offset : offsets) {
            pdf.append(String.format("%010d", offset)).append(" 00000 n \n");
        }
        pdf.append("trailer\n<< /Size ").append(offsets.size() + 1)
                .append(" /Root 1 0 R >>\nstartxref\n")
                .append(xrefStart).append("\n%%EOF");

        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.ISO_8859_1)) {
            writer.write(pdf.toString());
        }
    }

    private static String escapePdfText(String text) {
        StringBuilder escaped = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (ch == '\\' || ch == '(' || ch == ')') {
                escaped.append('\\');
            }
            if (ch >= 32 && ch <= 126) {
                escaped.append(ch);
            } else {
                escaped.append('?');
            }
        }
        return escaped.toString();
    }

    private static JLabel label(String text, Font font) {
        JLabel label = new JLabel(text);
        label.setFont(font);
        ThemeManager.styleText(label, false);
        return label;
    }

    private static void styleField(JTextField field, Font font) {
        field.setFont(font);
        ThemeManager.styleInput(field, false);
    }

    private static void styleArea(JTextArea area, Font font, String placeholder) {
        area.setFont(font);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setText(placeholder);
        area.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
    }

    private static JScrollPane wrap(JTextArea area) {
        JScrollPane scroll = new JScrollPane(area);
        scroll.setPreferredSize(new Dimension(320, area.getRows() > 5 ? 120 : 90));
        return scroll;
    }
}
