package org.example.Service;

import org.example.Entities.AuditLog;
import org.example.Entities.User;
import org.example.Rep.AuditLogRep;
import org.example.Utils.SessionManager;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AuditLogService {

    private final AuditLogRep auditLogRep;

    public AuditLogService() {
        this.auditLogRep = new AuditLogRep();
    }

    /**
     * Log an action performed by the current user
     * @param action The action performed (e.g., "CREATE", "UPDATE", "DELETE")
     * @param entityType The type of entity affected (e.g., "CANDIDAT", "USER")
     * @param entityId The ID of the entity affected
     * @param details Additional details about the action
     */
    public void logAction(String action, String entityType, Long entityId, String details) {
        try {
            User currentUser = SessionManager.getCurrentUser();
            if (currentUser == null) {
                return; // Cannot log without a user
            }

            AuditLog log = new AuditLog();
            log.setAction(action);
            log.setEntityType(entityType);
            log.setEntityId(entityId);
            log.setDetails(details);
            log.setUserId(currentUser.getId());
            log.setUsername(currentUser.getUsername());
            log.setTimestamp(new Timestamp(System.currentTimeMillis()));

            auditLogRep.add(log);
        } catch (SQLException e) {
            // Log to console but don't disrupt application flow
            System.err.println("Error logging action: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Get all audit logs
     * @return List of all audit logs
     */
    public List<AuditLog> getAllLogs() throws SQLException {
        return auditLogRep.findAll();
    }

    /**
     * Get logs for a specific user
     * @param userId The user ID
     * @return List of logs for the user
     */
    public List<AuditLog> getLogsByUser(Integer userId) throws SQLException {
        return auditLogRep.findByUserId(userId);
    }

    /**
     * Get logs for a specific entity
     * @param entityType The entity type
     * @param entityId The entity ID
     * @return List of logs for the entity
     */
    public List<AuditLog> getLogsByEntity(String entityType, Long entityId) throws SQLException {
        return auditLogRep.findByEntityTypeAndId(entityType, entityId);
    }

    /**
     * Get filtered logs based on various criteria
     * @param entityType The entity type to filter by (optional)
     * @param action The action to filter by (optional)
     * @param userId The user ID to filter by (optional)
     * @param fromDate The start date to filter by (optional)
     * @param toDate The end date to filter by (optional)
     * @return List of filtered logs
     */
    public List<AuditLog> getFilteredLogs(String entityType, String action, Integer userId,
                                          LocalDate fromDate, LocalDate toDate) throws SQLException {
        return auditLogRep.findFiltered(entityType, action, userId, fromDate, toDate);
    }

    /**
     * Search logs by a search term
     * @param searchTerm The term to search for
     * @return List of matching logs
     */
    public List<AuditLog> searchLogs(String searchTerm) throws SQLException {
        return auditLogRep.search(searchTerm);
    }

    /**
     * Get the total count of logs
     * @return Total number of logs
     */
    public int getTotalLogsCount() throws SQLException {
        return auditLogRep.getTotalCount();
    }

    /**
     * Get the count of logs for a specific date
     * @param date The date to count logs for
     * @return Number of logs for the date
     */
    public int getLogsCountForDate(LocalDate date) throws SQLException {
        return auditLogRep.getCountForDate(date);
    }

    /**
     * Get the count of active users
     * @return Number of active users
     */
    public int getActiveUsersCount() throws SQLException {
        return auditLogRep.getActiveUsersCount();
    }

    /**
     * Get the top actions by frequency as a formatted string
     * @param limit The maximum number of actions to return
     * @return A formatted string of top actions
     */
    public String getTopActions(int limit) throws SQLException {
        Map<String, Integer> actionCounts = auditLogRep.getActionCounts(limit);

        if (actionCounts.isEmpty()) {
            return "Aucune action enregistrée";
        }

        return actionCounts.entrySet().stream()
                .map(entry -> entry.getKey() + " (" + entry.getValue() + ")")
                .collect(Collectors.joining(", "));
    }

    /**
     * Get the top actions by frequency as a map
     * @param limit The maximum number of actions to return
     * @return A map of action names to counts
     */
    public Map<String, Integer> getActionCountsMap(int limit) throws SQLException {
        return auditLogRep.getActionCounts(limit);
    }

    /**
     * Get the top users by activity as a formatted string
     * @param limit The maximum number of users to return
     * @return A formatted string of top users
     */
    public String getTopUsers(int limit) throws SQLException {
        Map<String, Integer> userCounts = auditLogRep.getUserCounts(limit);

        if (userCounts.isEmpty()) {
            return "Aucun utilisateur actif";
        }

        return userCounts.entrySet().stream()
                .map(entry -> entry.getKey() + " (" + entry.getValue() + ")")
                .collect(Collectors.joining(", "));
    }
    /**
     * Delete a log by its ID
     * @param id The ID of the log to delete
     * @return true if the log was deleted, false otherwise
     */
    public boolean deleteLogById(Long id) throws SQLException {
        return auditLogRep.delete(id);
    }

    /**
     * Get the top users by activity as a map
     * @param limit The maximum number of users to return
     * @return A map of usernames to counts
     */
    public Map<String, Integer> getUserCountsMap(int limit) throws SQLException {
        return auditLogRep.getUserCounts(limit);
    }

    /**
     * Get entity type counts as a map
     * @param limit The maximum number of entity types to return
     * @return A map of entity types to counts
     */
    public Map<String, Integer> getEntityTypeCountsMap(int limit) throws SQLException {
        return auditLogRep.getEntityTypeCounts(limit);
    }

    /**
     * Get recent logs
     * @param limit The maximum number of logs to return
     * @return List of recent logs
     */
    public List<AuditLog> getRecentLogs(int limit) throws SQLException {
        return auditLogRep.findRecent(limit);
    }

    /**
     * Get paginated logs
     * @param offset The offset to start from
     * @param limit The maximum number of logs to return
     * @return List of paginated logs
     */
    public List<AuditLog> getLogsPaginated(int offset, int limit) throws SQLException {
        return auditLogRep.findPaginated(offset, limit);
    }
}

