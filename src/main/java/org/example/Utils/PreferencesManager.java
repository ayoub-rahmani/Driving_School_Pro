package org.example.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Utility class to manage application preferences
 */
public class PreferencesManager {

    private static final String PREFS_FILE = "app_preferences.properties";
    private static final String AUTO_LOGIN_KEY = "auto_login_enabled";
    private static final String SAVED_USERNAME_KEY = "saved_username";
    private static final String SAVED_USER_ID_KEY = "saved_user_id";
    private static final String THEME_KEY = "app_theme";
    private static final String SESSION_TIMEOUT_KEY = "session_timeout_minutes";
    private static final String LAST_LOGIN_TIME_KEY = "last_login_time"; // New key for last login time
    private static final String PREVIOUS_LOGIN_TIME_KEY = "previous_login_time";

    private static Properties properties;

    static {
        properties = new Properties();
        loadProperties();
    }

    /**
     * Load properties from file
     */
    private static void loadProperties() {
        File file = new File(PREFS_FILE);
        if (file.exists()) {
            try (FileInputStream fis = new FileInputStream(file)) {
                properties.load(fis);
            } catch (IOException e) {
                System.err.println("Error loading preferences: " + e.getMessage());
            }
        }
    }

    /**
     * Save properties to file
     */
    private static void saveProperties() {
        try (FileOutputStream fos = new FileOutputStream(PREFS_FILE)) {
            properties.store(fos, "Auto-École Application Preferences");
        } catch (IOException e) {
            System.err.println("Error saving preferences: " + e.getMessage());
        }
    }

    /**
     * Enable auto-login for a user
     *
     * @param userId The user ID
     * @param username The username
     */
    public static void enableAutoLogin(int userId, String username) {
        properties.setProperty(AUTO_LOGIN_KEY, "true");
        properties.setProperty(SAVED_USERNAME_KEY, username);
        properties.setProperty(SAVED_USER_ID_KEY, String.valueOf(userId));
        saveProperties();
    }

    /**
     * Disable auto-login
     */
    public static void disableAutoLogin() {
        properties.setProperty(AUTO_LOGIN_KEY, "false");
        saveProperties();
    }

    /**
     * Check if auto-login is enabled
     *
     * @return true if auto-login is enabled
     */
    public static boolean isAutoLoginEnabled() {
        return "true".equals(properties.getProperty(AUTO_LOGIN_KEY, "false"));
    }

    /**
     * Get the saved username for auto-login
     *
     * @return The saved username or null if not found
     */
    public static String getSavedUsername() {
        return properties.getProperty(SAVED_USERNAME_KEY);
    }

    /**
     * Get the saved user ID for auto-login
     *
     * @return The saved user ID or -1 if not found
     */
    public static int getSavedUserId() {
        String userIdStr = properties.getProperty(SAVED_USER_ID_KEY);
        if (userIdStr != null) {
            try {
                return Integer.parseInt(userIdStr);
            } catch (NumberFormatException e) {
                return -1;
            }
        }
        return -1;
    }



    /**
     * Save the last login time
     *
     * @param timestamp The timestamp of the last login
     */
    public static void saveLastLoginTime(long timestamp) {
        properties.setProperty(LAST_LOGIN_TIME_KEY, String.valueOf(timestamp));
        saveProperties();
    }

    /**
     * Get the last login time
     *
     * @return The timestamp of the last login or 0 if not found
     */
    public static long getLastLoginTime() {
        String timeStr = properties.getProperty(LAST_LOGIN_TIME_KEY, "0");
        try {
            return Long.parseLong(timeStr);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Save the previous login time
     *
     * @param timestamp The timestamp of the previous login
     */
    public static void savePreviousLoginTime(long timestamp) {
        properties.setProperty(PREVIOUS_LOGIN_TIME_KEY, String.valueOf(timestamp));
        saveProperties();
    }

    /**
     * Get the previous login time
     *
     * @return The timestamp of the previous login or 0 if not found
     */
    public static long getPreviousLoginTime() {
        String timeStr = properties.getProperty(PREVIOUS_LOGIN_TIME_KEY, "0");
        try {
            return Long.parseLong(timeStr);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}

