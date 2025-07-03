package com.example.financeapp;

import java.util.prefs.Preferences;

public class ThemeManager {
    private static final String THEME_KEY = "app_theme";
    private static final Preferences prefs = Preferences.userNodeForPackage(ThemeManager.class);

    public static void saveTheme(String themeName) {
        prefs.put(THEME_KEY, themeName);
    }

    public static String loadTheme() {
        return prefs.get(THEME_KEY, "lightMode.css"); // default is "light"
    }
}

