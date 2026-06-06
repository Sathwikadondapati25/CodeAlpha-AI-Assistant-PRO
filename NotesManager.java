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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class NotesManager extends JDialog {
    private final Path notesPath;
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final List<Note> notes = new ArrayList<>();
    private final DefaultListModel<String> listModel = new DefaultListModel<>();
    private final JList<String> notesList = new JList<>(listModel);
    private final JTextField searchField = new JTextField();
    private final JTextField titleField = new JTextField();
    private final JTextArea contentArea = new JTextArea(16, 40);
    private final JLabel statusLabel = new JLabel(" ");

    private String selectedNoteId;
    private List<Note> visibleNotes = new ArrayList<>();

    public NotesManager(JFrame owner) {
        super(owner, "Notes Manager", true);
        this.notesPath = getNotesPath(owner);
        setMinimumSize(new Dimension(900, 580));
        setSize(940, 620);
        setLocationRelativeTo(owner);

        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBackground(ThemeManager.LIGHT_BG);
        root.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        setContentPane(root);

        root.add(buildListPanel(), BorderLayout.WEST);
        root.add(buildEditorPanel(), BorderLayout.CENTER);
        root.add(buildActionBar(), BorderLayout.SOUTH);

        loadNotes();
        refreshList();
    }

    private static Path getNotesPath(JFrame owner) {
        if (owner instanceof NovaAIFrame) {
            String username = ((NovaAIFrame) owner).getCurrentUser();
            return Paths.get("data", username, "notes.txt");
        }
        return Paths.get("data", "notes.txt");
    }

    public static void showManager(JFrame owner) {
        NotesManager dialog = new NotesManager(owner);
        dialog.setVisible(true);
    }

    public static void exportNotes(JFrame owner) {
        List<Note> all = loadAllNotes(owner);
        if (all.isEmpty()) {
            JOptionPane.showMessageDialog(owner, "No notes to export.", "Export Notes",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new java.io.File("nova_notes_export.txt"));
        if (chooser.showSaveDialog(owner) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        StringBuilder export = new StringBuilder();
        export.append("NOVA AI ASSISTANT PRO — NOTES EXPORT\n");
        export.append("Exported: ").append(LocalDateTime.now().format(TIME_FORMAT)).append("\n");
        export.append(repeat('=', 60)).append("\n\n");

        for (Note note : all) {
            export.append("TITLE: ").append(note.getTitle()).append("\n");
            export.append("UPDATED: ").append(note.getUpdatedAt().format(TIME_FORMAT)).append("\n");
            export.append(repeat('-', 60)).append("\n");
            export.append(note.getContent()).append("\n\n");
        }

        try {
            java.nio.file.Path target = chooser.getSelectedFile().toPath();
            String name = target.getFileName().toString().toLowerCase();
            if (!name.endsWith(".txt")) {
                target = target.resolveSibling(target.getFileName().toString() + ".txt");
            }
            Files.write(target, export.toString().getBytes(StandardCharsets.UTF_8));
            JOptionPane.showMessageDialog(owner,
                    "Notes exported successfully:\n" + target.toAbsolutePath(),
                    "Export Notes",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(owner, "Export failed: " + e.getMessage(),
                    "Export Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static List<Note> loadAllNotes(JFrame owner) {
        List<Note> loaded = new ArrayList<>();
        Path path = getNotesPath(owner);
        if (!Files.exists(path)) {
            return loaded;
        }
        try {
            for (String line : Files.readAllLines(path, StandardCharsets.UTF_8)) {
                Note note = Note.fromStorageLine(line);
                if (note != null) {
                    loaded.add(note);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        loaded.sort((a, b) -> b.getUpdatedAt().compareTo(a.getUpdatedAt()));
        return loaded;
    }

    private static String repeat(char ch, int count) {
        StringBuilder builder = new StringBuilder(count);
        for (int i = 0; i < count; i++) {
            builder.append(ch);
        }
        return builder.toString();
    }

    private JPanel buildListPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setPreferredSize(new Dimension(280, 0));
        panel.setBorder(BorderFactory.createTitledBorder("Notes"));
        ThemeManager.stylePanel(panel, false, true);

        searchField.setToolTipText("Search by title or content");
        ThemeManager.styleInput(searchField, false);
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                refreshList();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                refreshList();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                refreshList();
            }
        });

        notesList.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        notesList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                selectNoteAt(notesList.getSelectedIndex());
            }
        });

        JPanel top = new JPanel(new BorderLayout(4, 4));
        top.setOpaque(false);
        JLabel searchLabel = new JLabel("Search");
        ThemeManager.styleText(searchLabel, false);
        top.add(searchLabel, BorderLayout.NORTH);
        top.add(searchField, BorderLayout.CENTER);

        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(notesList), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildEditorPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createTitledBorder("Note Editor"));
        ThemeManager.stylePanel(panel, false, false);

        JLabel titleLabel = new JLabel("Title");
        titleField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        ThemeManager.styleText(titleLabel, false);
        ThemeManager.styleInput(titleField, false);

        contentArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        contentArea.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JPanel titleRow = new JPanel(new BorderLayout(4, 4));
        titleRow.setOpaque(false);
        titleRow.add(titleLabel, BorderLayout.NORTH);
        titleRow.add(titleField, BorderLayout.CENTER);

        statusLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        ThemeManager.styleSecondaryText(statusLabel, false);

        panel.add(titleRow, BorderLayout.NORTH);
        panel.add(new JScrollPane(contentArea), BorderLayout.CENTER);
        panel.add(statusLabel, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildActionBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        bar.setOpaque(false);

        JButton addButton = new JButton("Add Note");
        JButton saveButton = new JButton("Save");
        JButton deleteButton = new JButton("Delete");
        JButton clearButton = new JButton("Clear");
        JButton closeButton = new JButton("Close");

        ThemeManager.styleButton(addButton, false);
        ThemeManager.styleButton(saveButton, false);
        ThemeManager.styleButton(deleteButton, false);
        ThemeManager.styleButton(clearButton, false);
        ThemeManager.styleButton(closeButton, false);

        addButton.addActionListener(e -> addNote());
        saveButton.addActionListener(e -> saveCurrentNote());
        deleteButton.addActionListener(e -> deleteCurrentNote());
        clearButton.addActionListener(e -> clearEditor());
        closeButton.addActionListener(e -> dispose());

        bar.add(addButton);
        bar.add(saveButton);
        bar.add(deleteButton);
        bar.add(clearButton);
        bar.add(closeButton);
        return bar;
    }

    private void loadNotes() {
        notes.clear();
        if (!Files.exists(notesPath)) {
            return;
        }
        try {
            for (String line : Files.readAllLines(notesPath, StandardCharsets.UTF_8)) {
                Note note = Note.fromStorageLine(line);
                if (note != null) {
                    notes.add(note);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void persistNotes() {
        try {
            Path parent = notesPath.getParent();
            if (parent != null && !Files.exists(parent)) {
                Files.createDirectories(parent);
            }
            try (BufferedWriter writer = Files.newBufferedWriter(
                    notesPath, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                for (Note note : notes) {
                    writer.write(note.toStorageLine());
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Failed to save notes: " + e.getMessage(),
                    "Save Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshList() {
        String query = searchField.getText().trim().toLowerCase();
        visibleNotes = notes.stream()
                .filter(note -> query.isEmpty()
                        || note.getTitle().toLowerCase().contains(query)
                        || note.getContent().toLowerCase().contains(query))
                .sorted((a, b) -> b.getUpdatedAt().compareTo(a.getUpdatedAt()))
                .collect(Collectors.toList());

        listModel.clear();
        for (Note note : visibleNotes) {
            listModel.addElement(note.getTitle());
        }

        if (selectedNoteId != null) {
            int idx = indexOfVisible(selectedNoteId);
            if (idx >= 0) {
                notesList.setSelectedIndex(idx);
            } else {
                clearEditor();
            }
        }
        statusLabel.setText(visibleNotes.size() + " note(s) shown · " + notes.size() + " total");
    }

    private int indexOfVisible(String noteId) {
        for (int i = 0; i < visibleNotes.size(); i++) {
            if (visibleNotes.get(i).getId().equals(noteId)) {
                return i;
            }
        }
        return -1;
    }

    private void selectNoteAt(int index) {
        if (index < 0 || index >= visibleNotes.size()) {
            return;
        }
        Note note = visibleNotes.get(index);
        selectedNoteId = note.getId();
        titleField.setText(note.getTitle());
        contentArea.setText(note.getContent());
        statusLabel.setText("Last updated: " + note.getUpdatedAt().format(TIME_FORMAT));
    }

    private void addNote() {
        String title = JOptionPane.showInputDialog(this, "Note title:", "Add Note", JOptionPane.PLAIN_MESSAGE);
        if (title == null) {
            return;
        }
        String trimmed = title.trim();
        if (trimmed.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Title cannot be empty.", "Add Note", JOptionPane.WARNING_MESSAGE);
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        Note note = new Note("note-" + UUID.randomUUID().toString().substring(0, 8), trimmed, "", now, now);
        notes.add(0, note);
        persistNotes();
        selectedNoteId = note.getId();
        refreshList();
        selectNoteAt(indexOfVisible(note.getId()));
        contentArea.requestFocus();
    }

    private void saveCurrentNote() {
        String title = titleField.getText().trim();
        if (title.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a title before saving.", "Save Note",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        String content = contentArea.getText();
        LocalDateTime now = LocalDateTime.now();

        if (selectedNoteId == null) {
            Note note = new Note("note-" + UUID.randomUUID().toString().substring(0, 8), title, content, now, now);
            notes.add(0, note);
            selectedNoteId = note.getId();
        } else {
            Note existing = findNote(selectedNoteId);
            if (existing == null) {
                Note note = new Note("note-" + UUID.randomUUID().toString().substring(0, 8), title, content, now, now);
                notes.add(0, note);
                selectedNoteId = note.getId();
            } else {
                existing.setTitle(title);
                existing.setContent(content);
                existing.setUpdatedAt(now);
            }
        }
        persistNotes();
        refreshList();
        statusLabel.setText("Saved at " + now.format(TIME_FORMAT));
    }

    private void deleteCurrentNote() {
        if (selectedNoteId == null) {
            JOptionPane.showMessageDialog(this, "Select a note to delete.", "Delete Note", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int choice = JOptionPane.showConfirmDialog(this, "Delete this note?", "Delete Note", JOptionPane.YES_NO_OPTION);
        if (choice != JOptionPane.YES_OPTION) {
            return;
        }
        notes.removeIf(note -> note.getId().equals(selectedNoteId));
        persistNotes();
        clearEditor();
        refreshList();
    }

    private void clearEditor() {
        selectedNoteId = null;
        titleField.setText("");
        contentArea.setText("");
        notesList.clearSelection();
        statusLabel.setText("Ready — select or add a note");
    }

    private Note findNote(String id) {
        for (Note note : notes) {
            if (note.getId().equals(id)) {
                return note;
            }
        }
        return null;
    }

    private static final class Note {
        private final String id;
        private String title;
        private String content;
        private final LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        Note(String id, String title, String content, LocalDateTime createdAt, LocalDateTime updatedAt) {
            this.id = id;
            this.title = title;
            this.content = content == null ? "" : content;
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
        }

        public String getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content == null ? "" : content;
        }

        public LocalDateTime getUpdatedAt() {
            return updatedAt;
        }

        public void setUpdatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
        }

        String toStorageLine() {
            return "NOTE|" + id + "|" + sanitize(title) + "|" + createdAt.format(TIME_FORMAT) + "|"
                    + updatedAt.format(TIME_FORMAT) + "|" + sanitize(content);
        }

        static Note fromStorageLine(String line) {
            if (line == null || !line.startsWith("NOTE|")) {
                return null;
            }
            String[] parts = line.split("\\|", 6);
            if (parts.length != 6) {
                return null;
            }
            return new Note(
                    parts[1],
                    parts[2].replace("\\n", "\n"),
                    parts[5].replace("\\n", "\n"),
                    LocalDateTime.parse(parts[3], TIME_FORMAT),
                    LocalDateTime.parse(parts[4], TIME_FORMAT));
        }

        private static String sanitize(String value) {
            return value.replace("|", "/").replace("\n", "\\n").replace("\r", "");
        }
    }
}
