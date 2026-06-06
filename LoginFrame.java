import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

public class LoginFrame extends JFrame {
    private static final Color BG = new Color(0xF8F7F4);
    private static final Color CARD = new Color(0xFFFFFF);
    private static final Color TEXT = new Color(0x1F2937);
    private static final Color SUBTEXT = new Color(0x6B7280);
    private static final Color ACCENT = new Color(0xE89B7A);
    private static final Color BORDER = new Color(0xE5E7EB);

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel rootPanel = new JPanel(cardLayout);

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JCheckBox showPasswordCheck;
    private JCheckBox rememberMeCheck;
    private char defaultEchoChar;

    private JLabel profileNameLabel;

    public LoginFrame() {
        setTitle("Nova AI Assistant PRO - Login");
        setSize(480, 420);
        setMinimumSize(new Dimension(440, 380));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        getContentPane().setBackground(BG);
        getContentPane().setLayout(new BorderLayout());

        rootPanel.setBackground(BG);
        getContentPane().add(rootPanel, BorderLayout.CENTER);

        rootPanel.add(buildLoginPanel(), "login");
        rootPanel.add(buildProfilePanel(), "profile");

        cardLayout.show(rootPanel, "login");
    }

    private JPanel buildLoginPanel() {
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setBackground(BG);

        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true),
                BorderFactory.createEmptyBorder(18, 22, 18, 22)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 0, 4, 0);
        gbc.weightx = 1.0;

        JLabel title = new JLabel("Nova AI Assistant PRO", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(TEXT);
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 6, 0);
        card.add(title, gbc);

        JLabel subtitle = new JLabel("Login to your assistant dashboard", SwingConstants.CENTER);
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(SUBTEXT);
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 14, 0);
        card.add(subtitle, gbc);

        JLabel userLabel = new JLabel("Username");
        userLabel.setForeground(TEXT);
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        gbc.gridy = 2;
        gbc.insets = new Insets(2, 2, 2, 2);
        card.add(userLabel, gbc);

        usernameField = new JTextField();
        usernameField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        usernameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        gbc.gridy = 3;
        gbc.insets = new Insets(0, 0, 6, 0);
        card.add(usernameField, gbc);

        JLabel passLabel = new JLabel("Password");
        passLabel.setForeground(TEXT);
        passLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        gbc.gridy = 4;
        gbc.insets = new Insets(4, 2, 2, 2);
        card.add(passLabel, gbc);

        passwordField = new JPasswordField();
        defaultEchoChar = passwordField.getEchoChar();
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        passwordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        gbc.gridy = 5;
        gbc.insets = new Insets(0, 0, 4, 0);
        card.add(passwordField, gbc);

        JPanel optionsRow = new JPanel(new BorderLayout());
        optionsRow.setOpaque(false);
        rememberMeCheck = new JCheckBox("Remember me");
        rememberMeCheck.setOpaque(false);
        rememberMeCheck.setForeground(SUBTEXT);
        rememberMeCheck.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        showPasswordCheck = new JCheckBox("Show password");
        showPasswordCheck.setOpaque(false);
        showPasswordCheck.setForeground(SUBTEXT);
        showPasswordCheck.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        showPasswordCheck.addActionListener(e -> toggleShowPassword());

        optionsRow.add(rememberMeCheck, BorderLayout.WEST);
        optionsRow.add(showPasswordCheck, BorderLayout.EAST);

        gbc.gridy = 6;
        gbc.insets = new Insets(2, 0, 8, 0);
        card.add(optionsRow, gbc);

        JButton loginButton = buildPrimaryButton("Login");
        loginButton.addActionListener(this::handleLogin);
        gbc.gridy = 7;
        gbc.insets = new Insets(4, 0, 4, 0);
        card.add(loginButton, gbc);

        JButton createAccountButton = buildSecondaryButton("Create New Account");
        createAccountButton.addActionListener(e -> handleCreateAccount());
        gbc.gridy = 8;
        gbc.insets = new Insets(2, 0, 0, 0);
        card.add(createAccountButton, gbc);

        outer.add(card);

        // Pre-fill "Remember me" username if available.
        String remembered = FileStore.loadRememberedUsername();
        if (remembered != null && !remembered.isEmpty()) {
            usernameField.setText(remembered);
            rememberMeCheck.setSelected(true);
        }

        return outer;
    }

    private JPanel buildProfilePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG);

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true),
                BorderFactory.createEmptyBorder(20, 24, 20, 24)
        ));

        profileNameLabel = new JLabel("Welcome,", SwingConstants.LEFT);
        profileNameLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        profileNameLabel.setForeground(TEXT);

        JLabel hint = new JLabel("You are logged in as administrator.", SwingConstants.LEFT);
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        hint.setForeground(SUBTEXT);

        JPanel top = new JPanel(new BorderLayout(0, 4));
        top.setOpaque(false);
        top.add(profileNameLabel, BorderLayout.NORTH);
        top.add(hint, BorderLayout.CENTER);

        JButton launchButton = buildPrimaryButton("Open Nova AI Assistant");
        launchButton.addActionListener(e -> openMainApplication(usernameField.getText().trim()));

        JButton logoutButton = buildSecondaryButton("Logout");
        logoutButton.addActionListener(e -> handleLogout());

        JPanel bottom = new JPanel();
        bottom.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 12, 0));
        bottom.setOpaque(false);
        bottom.add(launchButton);
        bottom.add(logoutButton);

        card.add(top, BorderLayout.NORTH);
        card.add(bottom, BorderLayout.SOUTH);

        panel.add(card, BorderLayout.CENTER);
        return panel;
    }

    private JButton buildPrimaryButton(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setForeground(Color.BLACK);
        button.setBackground(CARD);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ACCENT, 1, true),
                BorderFactory.createEmptyBorder(8, 18, 8, 18)
        ));
        button.setOpaque(true);
        return button;
    }

    private JButton buildSecondaryButton(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setForeground(SUBTEXT);
        button.setBackground(CARD);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        button.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        button.setOpaque(false);
        return button;
    }

    private void toggleShowPassword() {
        if (showPasswordCheck.isSelected()) {
            passwordField.setEchoChar((char) 0);
        } else {
            passwordField.setEchoChar(defaultEchoChar);
        }
    }

    private void handleLogin(ActionEvent e) {
        String username = usernameField.getText().trim().toLowerCase();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both username and password.", "Missing Information",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        java.util.Properties users = FileStore.loadUsers();
        if (users.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No registered accounts found. Please create an account first.",
                    "Account Not Configured",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String savedPassword = users.getProperty(username);
        if (savedPassword == null || !savedPassword.equals(password)) {
            JOptionPane.showMessageDialog(this,
                    "Invalid username or password.",
                    "Login Failed",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (rememberMeCheck.isSelected()) {
            FileStore.saveRememberedUsername(username);
        } else {
            FileStore.clearRememberedUsername();
        }

        profileNameLabel.setText("Welcome, " + username);
        openMainApplication(username);
    }

    private void openMainApplication(String username) {
        String lowerUsername = username == null ? "" : username.trim().toLowerCase();
        String displayName = lowerUsername.isEmpty() ? "guest" : lowerUsername;
        String userFolder = "data/" + displayName;
        new java.io.File(userFolder).mkdirs();
        FileStore fileStore = new FileStore(userFolder + "/chat_history.txt", userFolder + "/profile.properties");
        UserProfile profile = fileStore.loadUserProfile();
        if ("Guest".equalsIgnoreCase(profile.getName()) || profile.getName().isEmpty() || "guest".equals(profile.getName())) {
            fileStore.saveUserProfile(new UserProfile(displayName, profile.getEmail(), profile.getTheme()));
        }
        SwingUtilities.invokeLater(() -> {
            NovaAIFrame mainFrame = new NovaAIFrame(lowerUsername);
            mainFrame.setVisible(true);
            dispose();
        });
    }

    private void handleCreateAccount() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(CARD);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(4, 0, 4, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JTextField userField = new JTextField();
        JPasswordField passField = new JPasswordField();

        gbc.gridy = 0;
        panel.add(new JLabel("Username"), gbc);
        gbc.gridy = 1;
        panel.add(userField, gbc);
        gbc.gridy = 2;
        panel.add(new JLabel("Password"), gbc);
        gbc.gridy = 3;
        panel.add(passField, gbc);

        int result = JOptionPane.showConfirmDialog(
                this,
                panel,
                "Create / Update Account",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String user = userField.getText().trim().toLowerCase();
            String pass = new String(passField.getPassword());
            if (user.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Username and password cannot be empty.", "Invalid Input",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            FileStore.saveUser(user, pass);
            JOptionPane.showMessageDialog(this, "Account saved successfully.");
        }
    }

    private void handleLogout() {
        int choice = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to logout?",
                "Logout",
                JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            passwordField.setText("");
            cardLayout.show(rootPanel, "login");
        }
    }
}

