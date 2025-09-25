package org.example.Utils;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Initializes application components on startup
 */
public class ApplicationInitializer {
    private static final Logger LOGGER = Logger.getLogger(ApplicationInitializer.class.getName());
    private static boolean initialized = false;

    /**
     * Initialize all application components
     */
    public static void initialize() {
        if (initialized) {
            return;
        }

        try {
            // Initialize database connection
            DatabaseConnection.getInstance();
            LOGGER.info("Database connection initialized");

            initialized = true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize application components", e);
        }
    }

    /**
     * Shutdown all application components
     */
    public static void shutdown() {
        try {
            // Close database connection
            DatabaseConnection.getInstance().closeConnection();
            LOGGER.info("Database connection closed");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error during application shutdown", e);
        }
    }
}

