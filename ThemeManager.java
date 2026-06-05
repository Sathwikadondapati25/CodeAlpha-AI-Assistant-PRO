import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
public class ThemeManager {
    public enum AppTheme {
        LIGHT("light", "Light", false),
        DARK("dark", "Dark", true),
        MIDNIGHT("midnight", "Midnight", true);

        private final String id;
        private final String displayName;
        private final boolean dark;

        AppTheme(String id, String displayName, boolean dark) {
            this.id = id;
            this.displayName = displayName;
            this.dark = dark;
        }

        public String getId() {
            return id;
        }

        public String getDisplayName() {
            return displayName;
        }

        public boolean isDark() {
            return dark;
        }

        public static AppTheme fromId(String id) {
            if (id == null) {
                return LIGHT;
            }
            for (AppTheme theme : values()) {
                if (theme.id.equalsIgnoreCase(id.trim())) {
                    return theme;
                }
            }
            return LIGHT;
        }
    }

    private static AppTheme currentTheme = AppTheme.LIGHT;

    // Light palette
    public static final Color LIGHT_BG = new Color(0xF8F7F4);
    public static final Color LIGHT_PANEL = new Color(0xEFEDE7);
    public static final Color LIGHT_CARD = new Color(0xFFFFFF);
    public static final Color LIGHT_TEXT = new Color(0x1F2937);
    public static final Color LIGHT_SECONDARY_TEXT = new Color(0x6B7280);
    public static final Color LIGHT_BORDER = new Color(0xE5E7EB);
    public static final Color LIGHT_ACCENT = new Color(0xE89B7A);

    // Dark palette
    public static final Color DARK_BG = new Color(0x0F172A);
    public static final Color DARK_SIDEBAR = new Color(0x111827);
    public static final Color DARK_GLASS = new Color(30, 34, 45, 230);
    public static final Color DARK_TEXT = new Color(0xF8FAFC);
    public static final Color DARK_SECONDARY_TEXT = new Color(0xCBD5E1);
    public static final Color DARK_BORDER = new Color(78, 86, 104, 180);
    public static final Color DARK_AI_BUBBLE = new Color(47, 54, 72);

    // Midnight palette
    public static final Color MIDNIGHT_BG = new Color(0x050816);
    public static final Color MIDNIGHT_SIDEBAR = new Color(0x0B1224);
    public static final Color MIDNIGHT_PANEL = new Color(0x111C33);
    public static final Color MIDNIGHT_TEXT = new Color(0xE2E8F0);
    public static final Color MIDNIGHT_BORDER = new Color(0x334155);

    public static final Color USER_MESSAGE = new Color(0xE89B7A);
    public static final Color ACCENT_ORANGE = new Color(0xF97316);
    public static final Color HOVER_COLOR = new Color(0xFB923C);

    public static AppTheme getCurrentTheme() {
        return currentTheme;
    }

    public static void setCurrentTheme(AppTheme theme) {
        currentTheme = theme == null ? AppTheme.LIGHT : theme;
    }

    public static boolean isDark(AppTheme theme) {
        return theme != null && theme.isDark();
    }

    public static boolean isDarkMode() {
        return currentTheme.isDark();
    }

    public static Color background(AppTheme theme) {
        switch (theme) {
            case MIDNIGHT:
                return MIDNIGHT_BG;
            case DARK:
                return DARK_BG;
            case LIGHT:
            default:
                return LIGHT_BG;
        }
    }

    public static Color sidebar(AppTheme theme) {
        switch (theme) {
            case MIDNIGHT:
                return MIDNIGHT_SIDEBAR;
            case DARK:
                return DARK_SIDEBAR;
            case LIGHT:
            default:
                return LIGHT_PANEL;
        }
    }

    public static Color panel(AppTheme theme) {
        switch (theme) {
            case MIDNIGHT:
                return MIDNIGHT_PANEL;
            case DARK:
                return DARK_GLASS;
            case LIGHT:
            default:
                return LIGHT_CARD;
        }
    }

    public static Color text(AppTheme theme) {
        switch (theme) {
            case MIDNIGHT:
                return MIDNIGHT_TEXT;
            case DARK:
                return DARK_TEXT;
            case LIGHT:
            default:
                return LIGHT_TEXT;
        }
    }

    public static Color secondaryText(AppTheme theme) {
        switch (theme) {
            case MIDNIGHT:
            case DARK:
                return DARK_SECONDARY_TEXT;
            case LIGHT:
            default:
                return LIGHT_SECONDARY_TEXT;
        }
    }

    public static Color border(AppTheme theme) {
        switch (theme) {
            case MIDNIGHT:
                return MIDNIGHT_BORDER;
            case DARK:
                return DARK_BORDER;
            case LIGHT:
            default:
                return LIGHT_BORDER;
        }
    }

    public static void applyGlobalUiDefaults(AppTheme theme) {
        UIManager.put("Menu.selectionBackground", ACCENT_ORANGE);
        UIManager.put("MenuItem.selectionBackground", ACCENT_ORANGE);
        UIManager.put("MenuItem.selectionForeground", theme.isDark() ? DARK_TEXT : LIGHT_TEXT);
        UIManager.put("OptionPane.background", background(theme));
        UIManager.put("Panel.background", background(theme));
    }

    public static void styleInput(JTextField inputField, AppTheme theme) {
        inputField.setBackground(panel(theme));
        inputField.setForeground(text(theme));
        inputField.setCaretColor(text(theme));
        inputField.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(border(theme), 16),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));
    }

    public static void styleTextArea(JTextArea area, AppTheme theme) {
        area.setBackground(panel(theme));
        area.setForeground(text(theme));
        area.setCaretColor(text(theme));
    }

    public static void styleButton(JButton button, AppTheme theme) {
        boolean dark = theme.isDark();
        Color bg = dark ? new Color(0x1E293B) : Color.WHITE;
        Color fg = dark ? DARK_TEXT : LIGHT_TEXT;
        Color edge = dark ? border(theme) : ACCENT_ORANGE;

        button.setBackground(bg);
        button.setForeground(fg);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(edge, 16),
                BorderFactory.createEmptyBorder(9, 16, 9, 16)));
        button.setOpaque(true);

        if (button.getClientProperty("hoverBound") == null) {
            button.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    button.setBackground(HOVER_COLOR);
                    button.setForeground(Color.WHITE);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    button.setBackground(bg);
                    button.setForeground(fg);
                }
            });
            button.putClientProperty("hoverBound", Boolean.TRUE);
        }
    }

    public static void stylePanel(JComponent panel, AppTheme theme, boolean sidebar) {
        if (sidebar) {
            panel.setBackground(sidebar(theme));
            panel.setBorder(BorderFactory.createCompoundBorder(
                    new RoundedBorder(border(theme), 18),
                    BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        } else {
            panel.setBackground(panel(theme));
        }
    }

    public static void styleText(Component component, AppTheme theme) {
        component.setForeground(text(theme));
    }

    public static void styleSecondaryText(Component component, AppTheme theme) {
        component.setForeground(secondaryText(theme));
    }

    public static void applyRootBackground(JComponent root, AppTheme theme) {
        root.setBackground(background(theme));
    }

    public static void styleSessionList(JList<?> list, AppTheme theme) {
        list.setBackground(theme.isDark() ? new Color(24, 34, 51, 220) : LIGHT_CARD);
        list.setForeground(text(theme));
        list.setBorder(new RoundedBorder(border(theme), 16));
    }

    public static void styleMenu(JMenuBar bar, AppTheme theme) {
        if (bar == null) return;
        Color bg = sidebar(theme);
        Color fg = text(theme);
        bar.setBackground(bg);
        bar.setForeground(fg);
        for (int i = 0; i < bar.getMenuCount(); i++) {
            javax.swing.JMenu menu = bar.getMenu(i);
            if (menu != null) {
                menu.setForeground(fg);
                menu.setBackground(bg);
                javax.swing.JPopupMenu popup = menu.getPopupMenu();
                if (popup != null) {
                    stylePopupMenu(popup);
                }
            }
        }
    }

    public static void stylePopupMenu(javax.swing.JPopupMenu popup) {
        if (popup == null) return;
        popup.setOpaque(false);
        popup.setBorder(new ModernPopupMenuBorder());
        popup.setUI(new javax.swing.plaf.basic.BasicPopupMenuUI() {
            @Override
            public void update(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                AppTheme theme = getCurrentTheme();
                g2.setColor(panel(theme));
                
                java.awt.Insets insets = c.getInsets();
                int x = insets.left - 2;
                int y = insets.top - 2;
                int w = c.getWidth() - insets.left - insets.right + 4;
                int h = c.getHeight() - insets.top - insets.bottom + 4;
                
                g2.fillRoundRect(x, y, w, h, 12, 12);
                g2.dispose();
                
                paint(g, c);
            }
        });
    }

    private static class ModernPopupMenuBorder extends javax.swing.border.AbstractBorder {
        @Override
        public java.awt.Insets getBorderInsets(Component c) {
            return new java.awt.Insets(6, 6, 10, 10);
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Draw subtle drop shadow layers
            int shadowSize = 4;
            for (int i = 0; i < shadowSize; i++) {
                g2.setColor(new Color(0, 0, 0, 8 - i * 2));
                g2.drawRoundRect(x + 4 + i, y + 4 + i, width - 10, height - 10, 12, 12);
            }
            
            // Outer outline matching theme's borders
            AppTheme theme = getCurrentTheme();
            g2.setColor(border(theme));
            g2.setStroke(new java.awt.BasicStroke(1.0f));
            g2.drawRoundRect(x + 4, y + 4, width - 11, height - 11, 12, 12);
            
            g2.dispose();
        }
    }

    public static void styleScrollPane(JScrollPane scroll, AppTheme theme, boolean sidebarViewport) {
        scroll.getViewport().setBackground(sidebarViewport ? sidebar(theme) : background(theme));
        scroll.setBorder(BorderFactory.createEmptyBorder());
    }

    public static void styleDialog(JDialog dialog, AppTheme theme) {
        Container content = dialog.getContentPane();
        if (content instanceof JComponent) {
            applyRootBackground((JComponent) content, theme);
        }
        applyGlobalUiDefaults(theme);
    }

    public static void applyThemeToWindow(Window window, AppTheme theme) {
        setCurrentTheme(theme);
        applyGlobalUiDefaults(theme);
        if (window instanceof JFrame && ((JFrame) window).getContentPane() instanceof JPanel) {
            // Frame-specific styling is handled by NovaAIFrame.applyTheme.
        }
        window.repaint();
    }

    // Backward-compatible helpers
    public static void styleInput(JTextField field, boolean darkMode) {
        styleInput(field, darkMode ? AppTheme.DARK : AppTheme.LIGHT);
    }

    public static void styleButton(JButton button, boolean darkMode) {
        styleButton(button, darkMode ? AppTheme.DARK : AppTheme.LIGHT);
    }

    public static void stylePanel(JComponent panel, boolean darkMode, boolean sidebar) {
        stylePanel(panel, darkMode ? AppTheme.DARK : AppTheme.LIGHT, sidebar);
    }

    public static void styleText(Component component, boolean darkMode) {
        styleText(component, darkMode ? AppTheme.DARK : AppTheme.LIGHT);
    }

    public static void styleSecondaryText(Component component, boolean darkMode) {
        styleSecondaryText(component, darkMode ? AppTheme.DARK : AppTheme.LIGHT);
    }

    public static void applyRootBackground(JComponent root, boolean darkMode) {
        applyRootBackground(root, darkMode ? AppTheme.DARK : AppTheme.LIGHT);
    }

    public static void styleSessionList(JList<?> list, boolean darkMode) {
        styleSessionList(list, darkMode ? AppTheme.DARK : AppTheme.LIGHT);
    }

    public static void styleMenu(JMenuBar bar, boolean darkMode) {
        styleMenu(bar, darkMode ? AppTheme.DARK : AppTheme.LIGHT);
    }
}
