import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextArea;
import javax.swing.OverlayLayout;
import javax.swing.SwingUtilities;

public class ChatInputBar extends JPanel {
    public interface InputListener {
        void onSend(String text);

        void onNewChat();

        void onAttachFile(File file);

        default void onNewNote() {
        }
    }

    private final InputListener listener;
    private final JTextArea inputArea;
    private final JLabel placeholderLabel;
    private boolean showingPlaceholder = true;

    public ChatInputBar(InputListener listener) {
        this.listener = listener;
        setOpaque(false);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(12, 24, 20, 24));

        final RoundedPanel shell = new RoundedPanel(UiPalette.CARD, UiPalette.RADIUS_INPUT);
        shell.setLayout(new BorderLayout(12, 0));
        shell.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        shell.setBackground(Color.WHITE);
        shell.setBorderColor(UiPalette.BORDER, 1);
        shell.setDrawShadow(true);
        // Ensure enough vertical room so left/right circular buttons are not clipped.
        shell.setPreferredSize(new Dimension(0, 60));
        shell.setMinimumSize(new Dimension(0, 60));


        // Circular attach button with a custom + glyph drawn in paint (no unicode/emoji).
        IconButton plusButton = new IconButton("") {
            {
                circular = true;
                normalBg = new Color(0xFFFBF7);
                hoverBg = new Color(0xF3F4F6);
                pressedBg = new Color(0xE5E7EB);
                setForeground(new Color(0x374151));
                setPreferredSize(new Dimension(40, 40));
                setMinimumSize(new Dimension(40, 40));
                setMaximumSize(new Dimension(40, 40));
                setToolTipText("New Chat / Attach File");
                setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(new Color(0x374151));
                g2.setStroke(new java.awt.BasicStroke(2.0f, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));

                int w = getWidth();
                int h = getHeight();
                int size = 14; // total + size
                int half = size / 2;

                int cx = w / 2;
                int cy = h / 2;

                // vertical
                g2.drawLine(cx, cy - half, cx, cy + half);
                // horizontal
                g2.drawLine(cx - half, cy, cx + half, cy);

                g2.dispose();
            }
        };
        plusButton.addActionListener(e -> showPlusMenu(plusButton));



        JPanel inputWrap = new JPanel();
        inputWrap.setOpaque(false);
        inputWrap.setLayout(new OverlayLayout(inputWrap));
        inputWrap.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        inputArea = new JTextArea(1, 20);
        inputArea.setFont(UiPalette.FONT_BODY);
        inputArea.setForeground(new Color(0x1F2937));
        inputArea.setLineWrap(true);
        inputArea.setWrapStyleWord(true);
        inputArea.setOpaque(false);
        inputArea.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
        inputArea.setBackground(new Color(0,0,0,0));
        inputArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER && !e.isShiftDown()) {
                    e.consume();
                    sendMessage();
                }
            }
        });
        inputArea.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                updatePlaceholder();
            }

            @Override
            public void focusLost(FocusEvent e) {
                updatePlaceholder();
            }
        });

        placeholderLabel = new JLabel("Ask anything...");
        placeholderLabel.setFont(UiPalette.FONT_BODY);
        placeholderLabel.setForeground(new Color(0x9CA3AF));
        placeholderLabel.setAlignmentX(0.0f);
        placeholderLabel.setAlignmentY(0.5f);
        placeholderLabel.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 0));

        inputWrap.add(inputArea);
        inputWrap.add(placeholderLabel);
        inputArea.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                updatePlaceholder();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                updatePlaceholder();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                updatePlaceholder();
            }
        });

        MicButton micButton = new MicButton();
        micButton.setToolTipText("Voice Input");
        micButton.addActionListener(e -> javax.swing.JOptionPane.showMessageDialog(
            SwingUtilities.getWindowAncestor(this),
            "Voice input is not available yet.",
            "Voice Input",
            javax.swing.JOptionPane.INFORMATION_MESSAGE));

        SendButton sendButton = new SendButton();



sendButton.addActionListener(e -> sendMessage());

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);
        right.setPreferredSize(new Dimension(120, 44));
        right.add(micButton);
        right.add(sendButton);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        left.setOpaque(false);
        left.add(plusButton);

        shell.add(left, BorderLayout.WEST);
        shell.add(inputWrap, BorderLayout.CENTER);
        shell.add(right, BorderLayout.EAST);
        add(shell, BorderLayout.CENTER);

        // Ensure the left/right circular controls aren't clipped by layout bounds.
            left.setBorder(BorderFactory.createEmptyBorder(0, 24, 0, 0));
            left.setPreferredSize(new Dimension(70, 60));
            right.setPreferredSize(new Dimension(140, 60));
            
        // adjust shell width to 80% of this component when resized

        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                int w = getWidth();
                int target = Math.min(850, Math.max(400, (int) (w * 0.8)));
                shell.setPreferredSize(new Dimension(target, 60));
                shell.revalidate();
                revalidate();
            }
        });

        SwingUtilities.invokeLater(() -> {
            inputArea.requestFocusInWindow();
        });
    }

    private void updatePlaceholder() {
        boolean empty = inputArea.getText().trim().isEmpty();
        showingPlaceholder = empty;
        placeholderLabel.setVisible(showingPlaceholder);
    }

    private void showPlusMenu(IconButton anchor) {
        javax.swing.JPopupMenu menu = new javax.swing.JPopupMenu();
        ThemeManager.stylePopupMenu(menu);
        ModernMenuItem attach = new ModernMenuItem("Attach File", new EmojiIcon("📎"));
        ModernMenuItem newChat = new ModernMenuItem("New Chat", new EmojiIcon("💬"));
        ModernMenuItem newNote = new ModernMenuItem("New Note", new EmojiIcon("📝"));
        attach.addActionListener(e -> chooseFile());
        newChat.addActionListener(e -> listener.onNewChat());
        newNote.addActionListener(e -> listener.onNewNote());
        menu.add(attach);
        menu.add(newChat);
        menu.add(newNote);
        menu.show(anchor, 0, -menu.getPreferredSize().height);
    }

    private void chooseFile() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            listener.onAttachFile(chooser.getSelectedFile());
        }
    }

    public void sendMessage() {
        String text = inputArea.getText().trim();
        if (text.isEmpty()) {
            return;
        }
        inputArea.setText("");
        updatePlaceholder();
        listener.onSend(text);
    }

    public void clearInput() {
        inputArea.setText("");
        updatePlaceholder();
    }

    public void focusInput() {
        inputArea.requestFocusInWindow();
    }

    public void setTyping(boolean typing) {
        if (typing) {
            placeholderLabel.setText("...");
            placeholderLabel.setVisible(true);
        } else {
            placeholderLabel.setText("Ask anything...");
            updatePlaceholder();
        }
    }

    /** Circular accent send control with arrow glyph. */
    static class SendButton extends IconButton {
        SendButton() {
            super("");
            setPreferredSize(new Dimension(44, 44));
            setMinimumSize(new Dimension(44, 44));
            setMaximumSize(new Dimension(44, 44));
            setCircularAccent();
        }

        private void setCircularAccent() {
            circular = true;
            normalBg = new Color(0xE89B7A);
            hoverBg = new Color(0xD87F62);
            pressedBg = new Color(0xC56E55);
            setForeground(Color.WHITE);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.WHITE);
            int cx = getWidth() / 2;
            int cy = getHeight() / 2;
            g2.setStroke(new java.awt.BasicStroke(2.4f, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
            // Draw upward arrow tip
            g2.drawLine(cx - 5, cy - 1, cx, cy - 6);
            g2.drawLine(cx + 5, cy - 1, cx, cy - 6);
            // Draw vertical arrow shaft
            g2.drawLine(cx, cy - 6, cx, cy + 6);
            g2.dispose();
        }
    }

    static class MicButton extends IconButton {
        MicButton() {
            super("");
            circular = true;
            normalBg = Color.WHITE;
            hoverBg = new Color(0xF3F4F6);
            pressedBg = new Color(0xE5E7EB);
            setForeground(Color.BLACK);
            setPreferredSize(new Dimension(40, 40));
            setMinimumSize(new Dimension(40, 40));
            setMaximumSize(new Dimension(40, 40));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
            int micW = 10;
            int micH = 14;
            int x = (w - micW) / 2;
            int y = (h - micH) / 2 - 2;
            g2.setColor(UiPalette.TEXT);
            g2.setStroke(new java.awt.BasicStroke(2.2f, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
            g2.drawRoundRect(x, y, micW, micH, 8, 8);
            g2.drawLine(x + micW / 2, y + micH, x + micW / 2, y + micH + 6);
            g2.drawLine(x + micW / 2 - 4, y + micH + 6, x + micW / 2 + 4, y + micH + 6);
            g2.dispose();
        }
    }
}

