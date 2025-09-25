package org.example.Utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {
    private static DatabaseConnection instance;
    private static Connection connection;

    // Database configuration
    private static String url = "jdbc:mysql://localhost:3306/driving_school";
    private static String username = "root";
    private static String password = "sogut";

    // Added getters for backup functionality
    public static String getUrl() {
        return url;
    }

    public static String getUsername() {
        return username;
    }

    public static String getPassword() {
        return password;
    }

    public static String getHost() {
        // Extract host from URL
        // Format: jdbc:mysql://localhost:3306/driving_school
        String host = "localhost"; // Default
        try {
            int startIndex = url.indexOf("//") + 2;
            int endIndex = url.indexOf(":", startIndex);
            if (endIndex > startIndex) {
                host = url.substring(startIndex, endIndex);
            } else {
                endIndex = url.indexOf("/", startIndex);
                if (endIndex > startIndex) {
                    host = url.substring(startIndex, endIndex);
                }
            }
        } catch (Exception e) {
            // Use default if parsing fails
        }
        return host;
    }

    public static String getPort() {
        // Extract port from URL
        // Format: jdbc:mysql://localhost:3306/driving_school
        String port = "3306"; // Default MySQL port
        try {
            int hostIndex = url.indexOf("//") + 2;
            int portIndex = url.indexOf(":", hostIndex);
            if (portIndex > 0) {
                int endIndex = url.indexOf("/", portIndex);
                if (endIndex > portIndex) {
                    port = url.substring(portIndex + 1, endIndex);
                }
            }
        } catch (Exception e) {
            // Use default if parsing fails
        }
        return port;
    }

    private DatabaseConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            createConnection();
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found: " + e.getMessage());
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Error establishing database connection: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void createConnection() throws SQLException {
        connection = DriverManager.getConnection(url, username, password);
        // Set a longer timeout to prevent quick disconnections
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("SET SESSION wait_timeout=28800"); // 8 hours
            stmt.execute("SET SESSION interactive_timeout=28800"); // 8 hours
        }
    }

    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    /**
     * Gets a valid database connection.
     * If the current connection is closed or invalid, a new one is created.
     *
     * @return A valid database connection
     * @throws SQLException if a database access error occurs
     */
    public static synchronized Connection getConnection() throws SQLException {
        try {
            // Check if connection is valid with a 5-second timeout
            if (connection == null || connection.isClosed() || !connection.isValid(5)) {
                createConnection();
            }
        } catch (SQLException e) {
            System.err.println("Error validating connection: " + e.getMessage());
            // Try to create a new connection
            createConnection();
        }

        return connection;
    }

    /**
     * Call this method when the application shuts down
     */
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                connection = null;
            }
        } catch (SQLException e) {
            System.err.println("Error closing database connection: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

