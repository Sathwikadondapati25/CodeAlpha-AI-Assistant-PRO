public class UserProfile {
    private final String name;
    private final String email;
    private final ThemeManager.AppTheme theme;

    public UserProfile(String name, ThemeManager.AppTheme theme) {
        this(name, "", theme);
    }

    public UserProfile(String name, String email, ThemeManager.AppTheme theme) {
        this.name = name == null || name.trim().isEmpty() ? "Guest" : name.trim();
        this.email = email == null ? "" : email.trim();
        this.theme = theme == null ? ThemeManager.AppTheme.LIGHT : theme;
    }

    public static UserProfile defaultProfile() {
        return new UserProfile("Guest", ThemeManager.AppTheme.LIGHT);
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public ThemeManager.AppTheme getTheme() {
        return theme;
    }

    public boolean isDarkMode() {
        return theme.isDark();
    }
}
