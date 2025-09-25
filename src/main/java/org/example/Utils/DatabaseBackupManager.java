package org.example.Utils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.example.Service.AuditLogService;

/**
 * Manages database backups for the application.
 * Provides functionality to create, list, and restore database backups.
 */
public class DatabaseBackupManager {
    private static final Logger LOGGER = Logger.getLogger(DatabaseBackupManager.class.getName());
    private static DatabaseBackupManager instance;

    // Configuration
    private static final String BACKUP_DIRECTORY = "backups";
    private static final int MAX_BACKUPS = 30; // Maximum number of backups to keep

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
    private AuditLogService auditLogService;

    private boolean mysqlDumpUsed = false;

    private DatabaseBackupManager() {
        try {
            // Create backup directory if it doesn't exist
            File backupDir = new File(BACKUP_DIRECTORY);
            if (!backupDir.exists()) {
                backupDir.mkdirs();
            }

            auditLogService = new AuditLogService();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error initializing DatabaseBackupManager", e);
        }
    }

    public static synchronized DatabaseBackupManager getInstance() {
        if (instance == null) {
            instance = new DatabaseBackupManager();
        }
        return instance;
    }

    /**
     * Returns whether mysqldump was used for the last backup operation
     * @return true if mysqldump was used, false if Java backup was used
     */
    public boolean wasMysqldumpUsed() {
        return mysqlDumpUsed;
    }

    /**
     * Creates a database backup
     *
     * @param initiatedBy Who initiated the backup (auto, username, etc.)
     * @return The path to the created backup file
     * @throws IOException If there's an error creating the backup
     * @throws SQLException If there's a database error
     */
    public String createBackup(String initiatedBy) throws IOException, SQLException {
        // Reset the flag at the start of each backup
        mysqlDumpUsed = false;

        // Get database connection properties
        String dbName = getDatabaseName();
        String timestamp = dateFormat.format(new Date());
        String backupFileName = dbName + "_" + timestamp + ".sql";
        String backupFilePath = BACKUP_DIRECTORY + File.separator + backupFileName;

        // Try using mysqldump first if it's available
        boolean mysqldumpSuccess = false;
        if (isMysqldumpAvailable()) {
            try {
                mysqldumpSuccess = tryMysqldumpBackup(dbName, backupFilePath);
            } catch (Exception e) {
                LOGGER.log(Level.FINE, "mysqldump backup failed: " + e.getMessage(), e);
            }
        }

        // If mysqldump succeeded, set the flag
        if (mysqldumpSuccess) {
            mysqlDumpUsed = true;
        }

        // If mysqldump failed, use Java-based backup
        if (!mysqldumpSuccess) {
            LOGGER.info("Falling back to Java-based backup");
            createJavaBackup(dbName, backupFilePath);
        }

        // Log the backup creation
        auditLogService.logAction(
                "CREATE",
                "BACKUP",
                null,
                "Database backup created by " + initiatedBy + ": " + backupFileName
        );

        // Clean up old backups if we exceed the maximum
        cleanupOldBackups();

        return backupFilePath;
    }

    /**
     * Checks if mysqldump is available on the system
     * @return true if mysqldump is available, false otherwise
     */
    private boolean isMysqldumpAvailable() {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("mysqldump", "--version");
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (Exception e) {
            // If any exception occurs, mysqldump is not available
            return false;
        }
    }

    /**
     * Attempts to create a backup using mysqldump
     *
     * @param dbName Database name
     * @param backupFilePath Path to save the backup
     * @return true if successful, false otherwise
     */
    private boolean tryMysqldumpBackup(String dbName, String backupFilePath) throws IOException, InterruptedException {
        String username = DatabaseConnection.getUsername();
        String password = DatabaseConnection.getPassword();
        String host = DatabaseConnection.getHost();
        String port = DatabaseConnection.getPort();

        // Create a temporary file for the password
        File passwordFile = File.createTempFile("mysql-pwd", ".cnf");
        try {
            // Write password to file for secure passing
            try (FileWriter writer = new FileWriter(passwordFile)) {
                writer.write("[client]\npassword=" + password);
            }

            // Make sure the file is deleted when the JVM exits
            passwordFile.deleteOnExit();

            // Build the mysqldump command
            List<String> command = new ArrayList<>();
            command.add("mysqldump");
            command.add("--defaults-extra-file=" + passwordFile.getAbsolutePath());
            command.add("--host=" + host);
            command.add("--port=" + port);
            command.add("--user=" + username);
            command.add("--add-drop-database");
            command.add("--databases");
            command.add(dbName);

            // Create process builder
            ProcessBuilder processBuilder = new ProcessBuilder(command);

            // Redirect error stream to capture error messages
            processBuilder.redirectErrorStream(true);

            // Start the process
            Process process = processBuilder.start();

            // Capture output and write to file
            try (InputStream is = process.getInputStream();
                 FileOutputStream fos = new FileOutputStream(backupFilePath)) {

                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                }
            }

            // Wait for the process to complete
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                LOGGER.warning("mysqldump exited with code " + exitCode);

                // Read the backup file to check for error messages
                try (BufferedReader reader = new BufferedReader(new FileReader(backupFilePath))) {
                    String line;
                    StringBuilder errorOutput = new StringBuilder();
                    while ((line = reader.readLine()) != null) {
                        errorOutput.append(line).append("\n");
                    }
                    LOGGER.warning("mysqldump output: " + errorOutput.toString());
                }

                return false;
            }

            return true;
        } finally {
            // Delete the password file
            passwordFile.delete();
        }
    }

    /**
     * Creates a database backup using pure Java (no external tools)
     */
    private void createJavaBackup(String dbName, String backupFilePath) throws IOException, SQLException {
        LOGGER.info("Utilisation de la méthode de sauvegarde Java (mysqldump non disponible)");

        try (FileWriter writer = new FileWriter(backupFilePath)) {
            // Write backup header
            writer.write("-- Java-based MySQL backup\n");
            writer.write("-- Generated on: " + new Date() + "\n");
            writer.write("-- Database: " + dbName + "\n\n");

            // Get database connection
            Connection conn = DatabaseConnection.getConnection();

            // Write database recreation
            writer.write("-- Create database\n");
            writer.write("CREATE DATABASE IF NOT EXISTS `" + dbName + "`;\n");
            writer.write("USE `" + dbName + "`;\n\n");

            // Get all tables
            try (Statement stmt = conn.createStatement();
                 ResultSet tables = stmt.executeQuery("SHOW TABLES")) {

                while (tables.next()) {
                    String tableName = tables.getString(1);

                    // Skip system tables
                    if (tableName.startsWith("sys_") || tableName.startsWith("mysql_")) {
                        continue;
                    }

                    writer.write("-- Table structure for table `" + tableName + "`\n");
                    writer.write("DROP TABLE IF EXISTS `" + tableName + "`;\n");

                    // Get table creation SQL
                    try (Statement showStmt = conn.createStatement();
                         ResultSet rs = showStmt.executeQuery("SHOW CREATE TABLE `" + tableName + "`")) {

                        if (rs.next()) {
                            String createTableSQL = rs.getString(2);
                            writer.write(createTableSQL + ";\n\n");
                        }
                    }

                    // Get table data
                    writer.write("-- Dumping data for table `" + tableName + "`\n");

                    // Get column metadata
                    try (Statement colStmt = conn.createStatement();
                         ResultSet columns = colStmt.executeQuery("SHOW COLUMNS FROM `" + tableName + "`")) {

                        List<String> columnNames = new ArrayList<>();
                        while (columns.next()) {
                            columnNames.add(columns.getString("Field"));
                        }

                        if (!columnNames.isEmpty()) {
                            // Build column list for INSERT
                            StringBuilder columnList = new StringBuilder();
                            for (String column : columnNames) {
                                if (columnList.length() > 0) {
                                    columnList.append(", ");
                                }
                                columnList.append("`").append(column).append("`");
                            }

                            // Get data
                            try (Statement dataStmt = conn.createStatement();
                                 ResultSet rs = dataStmt.executeQuery("SELECT * FROM `" + tableName + "`")) {

                                boolean hasData = false;
                                int rowCount = 0;

                                while (rs.next()) {
                                    if (rowCount % 100 == 0) { // Start a new INSERT statement every 100 rows
                                        if (hasData) {
                                            writer.write(";\n");
                                        }
                                        writer.write("INSERT INTO `" + tableName + "` (" + columnList + ") VALUES\n");
                                        hasData = true;
                                    } else if (hasData) {
                                        writer.write(",\n");
                                    }

                                    StringBuilder values = new StringBuilder();
                                    for (int i = 1; i <= columnNames.size(); i++) {
                                        if (i > 1) {
                                            values.append(", ");
                                        }

                                        Object value = rs.getObject(i);
                                        if (value == null) {
                                            values.append("NULL");
                                        } else if (value instanceof Number) {
                                            values.append(value.toString());
                                        } else if (value instanceof Boolean) {
                                            values.append((Boolean) value ? "1" : "0");
                                        } else if (value instanceof Date) {
                                            values.append("'").append(new java.sql.Timestamp(((Date) value).getTime())).append("'");
                                        } else {
                                            // Escape string values
                                            String strValue = value.toString().replace("'", "''");
                                            values.append("'").append(strValue).append("'");
                                        }
                                    }

                                    writer.write("(" + values + ")");
                                    rowCount++;
                                }

                                if (hasData) {
                                    writer.write(";\n");
                                }
                            }
                        }
                    }

                    writer.write("\n");
                }
            }
        }
    }

    /**
     * Lists all available database backups
     *
     * @return List of backup file information
     */
    public List<BackupInfo> listBackups() {
        File backupDir = new File(BACKUP_DIRECTORY);
        if (!backupDir.exists() || !backupDir.isDirectory()) {
            return new ArrayList<>();
        }

        File[] backupFiles = backupDir.listFiles((dir, name) -> name.endsWith(".sql"));
        if (backupFiles == null) {
            return new ArrayList<>();
        }

        return Arrays.stream(backupFiles)
                .map(file -> {
                    String fileName = file.getName();
                    long fileSize = file.length();
                    Date creationDate = new Date(file.lastModified());

                    return new BackupInfo(fileName, fileSize, creationDate, file.getAbsolutePath());
                })
                .sorted(Comparator.comparing(BackupInfo::getCreationDate).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Restores a database from a backup file
     *
     * @param backupFilePath Path to the backup file
     * @param initiatedBy Who initiated the restore (username)
     * @return true if successful, false otherwise
     * @throws IOException If there's an error reading the backup
     * @throws SQLException If there's a database error
     */
    public boolean restoreBackup(String backupFilePath, String initiatedBy) throws IOException, SQLException {
        // Create a backup before restoring (safety measure)
        createBackup("pre-restore_" + initiatedBy);

        // Skip the mysql command-line client attempt since it's not available
        // Go directly to Java-based restore
        boolean success = restoreBackupJava(backupFilePath, getDatabaseName());

        if (success) {
            // Log the restore action
            auditLogService.logAction(
                    "RESTORE",
                    "BACKUP",
                    null,
                    "Database restored from backup by " + initiatedBy + ": " + new File(backupFilePath).getName()
            );
        }

        return success;
    }

    /**
     * Restores a database from a backup file using Java
     */
    private boolean restoreBackupJava(String backupFilePath, String dbName) throws IOException, SQLException {
        LOGGER.info("Performing Java-based restore from " + backupFilePath);

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();

            // Disable foreign key checks temporarily
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("SET FOREIGN_KEY_CHECKS = 0");
            }

            // Read the backup file and execute SQL statements
            try (BufferedReader reader = new BufferedReader(new FileReader(backupFilePath))) {
                StringBuilder sqlStatement = new StringBuilder();
                String line;
                int statementsExecuted = 0;

                while ((line = reader.readLine()) != null) {
                    // Skip comments and empty lines
                    if (line.startsWith("--") || line.trim().isEmpty()) {
                        continue;
                    }

                    sqlStatement.append(line);

                    // If the line ends with a semicolon, execute the statement
                    if (line.trim().endsWith(";")) {
                        String sql = sqlStatement.toString();
                        try (Statement stmt = conn.createStatement()) {
                            stmt.execute(sql);
                            statementsExecuted++;

                            // Log progress periodically
                            if (statementsExecuted % 100 == 0) {
                                LOGGER.info("Restored " + statementsExecuted + " SQL statements so far");
                            }
                        } catch (SQLException e) {
                            // Log the error but continue with other statements
                            LOGGER.log(Level.WARNING, "Error executing SQL: " + e.getMessage());
                        }

                        // Reset for the next statement
                        sqlStatement.setLength(0);
                    }
                }

                LOGGER.info("Restore completed: " + statementsExecuted + " SQL statements executed");
            }

            // Re-enable foreign key checks
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("SET FOREIGN_KEY_CHECKS = 1");
            }

            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to restore database", e);
            return false;
        } finally {
            // Make sure to close the connection
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.WARNING, "Error closing connection", e);
                }
            }
        }
    }

    /**
     * Deletes a specific backup file
     *
     * @param backupFilePath Path to the backup file
     * @param initiatedBy Who initiated the delete (username)
     * @return true if successful, false otherwise
     */
    public boolean deleteBackup(String backupFilePath, String initiatedBy) {
        File backupFile = new File(backupFilePath);
        if (backupFile.exists() && backupFile.isFile()) {
            boolean deleted = backupFile.delete();
            if (deleted) {
                try {
                    // Log the delete action
                    auditLogService.logAction(
                            "DELETE",
                            "BACKUP",
                            null,
                            "Database backup deleted by " + initiatedBy + ": " + backupFile.getName()
                    );
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Failed to log backup deletion", e);
                }
            }
            return deleted;
        }
        return false;
    }

    /**
     * Cleans up old backups if we exceed the maximum number
     */
    private void cleanupOldBackups() {
        File backupDir = new File(BACKUP_DIRECTORY);
        if (!backupDir.exists() || !backupDir.isDirectory()) {
            return;
        }

        File[] backupFiles = backupDir.listFiles((dir, name) -> name.endsWith(".sql"));
        if (backupFiles == null || backupFiles.length <= MAX_BACKUPS) {
            return;
        }

        // Sort files by last modified date (oldest first)
        Arrays.sort(backupFiles, Comparator.comparingLong(File::lastModified));

        // Delete oldest files until we're under the limit
        int filesToDelete = backupFiles.length - MAX_BACKUPS;
        for (int i = 0; i < filesToDelete; i++) {
            boolean deleted = backupFiles[i].delete();
            if (deleted) {
                LOGGER.info("Deleted old backup: " + backupFiles[i].getName());
            } else {
                LOGGER.warning("Failed to delete old backup: " + backupFiles[i].getName());
            }
        }
    }

    /**
     * Gets the database name from the connection URL
     */
    private String getDatabaseName() {
        String url = DatabaseConnection.getUrl();
        // Extract database name from JDBC URL
        // Format: jdbc:mysql://localhost:3306/driving_school
        int lastSlashIndex = url.lastIndexOf('/');
        if (lastSlashIndex != -1 && lastSlashIndex < url.length() - 1) {
            String dbName = url.substring(lastSlashIndex + 1);
            // Remove any parameters
            int paramIndex = dbName.indexOf('?');
            if (paramIndex > 0) {
                dbName = dbName.substring(0, paramIndex);
            }
            return dbName;
        }
        return "unknown_db";
    }

    /**
     * Class to hold backup file information
     */
    public static class BackupInfo {
        private final String fileName;
        private final long fileSize;
        private final Date creationDate;
        private final String filePath;

        public BackupInfo(String fileName, long fileSize, Date creationDate, String filePath) {
            this.fileName = fileName;
            this.fileSize = fileSize;
            this.creationDate = creationDate;
            this.filePath = filePath;
        }

        public String getFileName() {
            return fileName;
        }

        public long getFileSize() {
            return fileSize;
        }

        public Date getCreationDate() {
            return creationDate;
        }

        public String getFilePath() {
            return filePath;
        }

        public String getFormattedSize() {
            if (fileSize < 1024) {
                return fileSize + " B";
            } else if (fileSize < 1024 * 1024) {
                return String.format("%.2f KB", fileSize / 1024.0);
            } else {
                return String.format("%.2f MB", fileSize / (1024.0 * 1024.0));
            }
        }

        public String getFormattedDate() {
            return new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(creationDate);
        }

        @Override
        public String toString() {
            return fileName + " (" + getFormattedSize() + ") - " + getFormattedDate();
        }
    }
}

