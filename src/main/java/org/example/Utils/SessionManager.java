package org.example.Utils;

import org.example.Entities.User;
import org.example.Service.UserService;

import java.sql.SQLException;

/**
 * Manages user session
 */
public class SessionManager {

    private static User currentUser;

    // Change lastLoginTime to a local variable within setLoginTime, initialized by PREFS value
    private static long getLastLoginTimeFromPreferences() {
        return PreferencesManager.getLastLoginTime();
    }
    /**
     * Set the current user
     *
     * @param user The user to set as current
     */
    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    /**
     * Get the current user
     *
     * @return The current user
     */
    public static User getCurrentUser() {
        return currentUser;
    }

    // Change the implementation of setLoginTime to properly track the previous login time
    public static void setLoginTime(long time) {

        // Get the previous login time from preferences before updating
        long previousLoginTime = PreferencesManager.getLastLoginTime();

        // Only update the last login time in preferences if this is a new login session
        // (not an auto-login or continuation of the same session)
        if (currentUser != null && previousLoginTime > 0) {
            // Save the previous login time to a separate preference
            PreferencesManager.savePreviousLoginTime(previousLoginTime);
        }

        // Now save the current login time as the last login time
        PreferencesManager.saveLastLoginTime(time);
    }

    // Update getLastLoginTime to return the previous login time, not the current one
    public static long getLastLoginTime() {
        // Return the previous login time from preferences
        return PreferencesManager.getPreviousLoginTime();
    }

    /**
     * Clear the current session
     */
    public static void clearSession() {
        currentUser = null;
        // Disable auto-login when explicitly logging out
        PreferencesManager.disableAutoLogin();
    }

    /**
     * Try to restore session from saved preferences
     *
     * @return true if session was restored successfully
     */
    public static boolean tryAutoLogin() {
        if (PreferencesManager.isAutoLoginEnabled()) {
            int userId = PreferencesManager.getSavedUserId();
            if (userId > 0) {
                try {
                    UserService userService = new UserService();
                    User user = userService.findById(userId);
                    if (user != null) {
                        setCurrentUser(user);
                        setLoginTime(System.currentTimeMillis());//set again here also as precaution
                        return true;
                    }
                } catch (SQLException e) {
                    System.err.println("Error during auto-login: " + e.getMessage());
                } catch (Exception e) {
                    System.err.println("Unexpected error during auto-login: " + e.getMessage());
                }
            }
        }
        return false;
    }

    /**
     * Save the current session for auto-login
     */
    public static void saveSessionForAutoLogin() {
        if (currentUser != null) {
            PreferencesManager.enableAutoLogin(currentUser.getId(), currentUser.getUsername());
        }
    }
}

