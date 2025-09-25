package org.example.Rep;

import org.example.Entities.AuditLog;
import org.example.Entities.Candidat;
import org.example.Utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class AuditLogRep {
    private Connection connection;

    private Connection getConnection() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }

    public void add(AuditLog log) throws SQLException {
        String query = "INSERT INTO audit_logs (action, entity_type, entity_id, details, user_id, username, timestamp) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet generatedKeys = null;

        try {
            conn = getConnection();
            stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);

            stmt.setString(1, log.getAction());
            stmt.setString(2, log.getEntityType());

            if (log.getEntityId() != null) {
                stmt.setLong(3, log.getEntityId());
            } else {
                stmt.setNull(3, Types.BIGINT);
            }

            stmt.setString(4, log.getDetails());

            if (log.getUserId() != null) {
                stmt.setInt(5, log.getUserId());
            } else {
                stmt.setNull(5, Types.INTEGER);
            }

            stmt.setString(6, log.getUsername());

            if (log.getTimestamp() != null) {
                stmt.setTimestamp(7, log.getTimestamp());
            } else {
                stmt.setTimestamp(7, new Timestamp(System.currentTimeMillis()));
            }

            stmt.executeUpdate();

            generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                log.setId(generatedKeys.getLong(1));
            }
        } finally {
            if (generatedKeys != null) try { generatedKeys.close(); } catch (SQLException e) { /* ignored */ }
            if (stmt != null) try { stmt.close(); } catch (SQLException e) { /* ignored */ }
            // Don't close the connection here
        }
    }

    public List<AuditLog> findAll() throws SQLException {
        List<AuditLog> logs = new ArrayList<>();
        String query = "SELECT * FROM audit_logs ORDER BY timestamp DESC";

        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(query);

            while (rs.next()) {
                logs.add(mapResultSetToAuditLog(rs));
            }

            return logs;
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { /* ignored */ }
            if (stmt != null) try { stmt.close(); } catch (SQLException e) { /* ignored */ }
            // Don't close the connection here
        }
    }

    public List<AuditLog> findByUserId(Integer userId) throws SQLException {
        List<AuditLog> logs = new ArrayList<>();
        String query = "SELECT * FROM audit_logs WHERE user_id = ? ORDER BY timestamp DESC";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, userId);
            rs = stmt.executeQuery();

            while (rs.next()) {
                logs.add(mapResultSetToAuditLog(rs));
            }

            return logs;
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { /* ignored */ }
            if (stmt != null) try { stmt.close(); } catch (SQLException e) { /* ignored */ }
            // Don't close the connection here
        }
    }

    public List<AuditLog> findByEntityTypeAndId(String entityType, Long entityId) throws SQLException {
        List<AuditLog> logs = new ArrayList<>();
        String query = "SELECT * FROM audit_logs WHERE entity_type = ? AND entity_id = ? ORDER BY timestamp DESC";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            stmt = conn.prepareStatement(query);
            stmt.setString(1, entityType);
            stmt.setLong(2, entityId);
            rs = stmt.executeQuery();

            while (rs.next()) {
                logs.add(mapResultSetToAuditLog(rs));
            }

            return logs;
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { /* ignored */ }
            if (stmt != null) try { stmt.close(); } catch (SQLException e) { /* ignored */ }
            // Don't close the connection here
        }
    }

    public List<AuditLog> findFiltered(String entityType, String action, Integer userId, LocalDate fromDate, LocalDate toDate) throws SQLException {
        List<AuditLog> logs = new ArrayList<>();
        StringBuilder queryBuilder = new StringBuilder("SELECT * FROM audit_logs WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (entityType != null && !entityType.isEmpty()) {
            queryBuilder.append(" AND entity_type = ?");
            params.add(entityType);
        }

        if (action != null && !action.isEmpty()) {
            queryBuilder.append(" AND action = ?");
            params.add(action);
        }

        if (userId != null) {
            queryBuilder.append(" AND user_id = ?");
            params.add(userId);
        }

        if (fromDate != null) {
            queryBuilder.append(" AND DATE(timestamp) >= ?");
            params.add(java.sql.Date.valueOf(fromDate));
        }

        if (toDate != null) {
            queryBuilder.append(" AND DATE(timestamp) <= ?");
            params.add(java.sql.Date.valueOf(toDate));
        }

        queryBuilder.append(" ORDER BY timestamp DESC");

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            stmt = conn.prepareStatement(queryBuilder.toString());

            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            rs = stmt.executeQuery();

            while (rs.next()) {
                logs.add(mapResultSetToAuditLog(rs));
            }

            return logs;
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { /* ignored */ }
            if (stmt != null) try { stmt.close(); } catch (SQLException e) { /* ignored */ }
            // Don't close the connection here
        }
    }

    public List<AuditLog> search(String searchTerm) throws SQLException {
        List<AuditLog> logs = new ArrayList<>();
        String query = "SELECT * FROM audit_logs WHERE " +
                "action LIKE ? OR " +
                "entity_type LIKE ? OR " +
                "details LIKE ? OR " +
                "username LIKE ? OR " +
                "entity_id LIKE ? "+
                "ORDER BY timestamp DESC";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            stmt = conn.prepareStatement(query);

            String term = "%" + searchTerm + "%";
            stmt.setString(1, term);
            stmt.setString(2, term);
            stmt.setString(3, term);
            stmt.setString(4, term);
            stmt.setString(5, term);

            rs = stmt.executeQuery();

            while (rs.next()) {
                logs.add(mapResultSetToAuditLog(rs));
            }

            return logs;
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { /* ignored */ }
            if (stmt != null) try { stmt.close(); } catch (SQLException e) { /* ignored */ }
            // Don't close the connection here
        }
    }

    public int getTotalCount() throws SQLException {
        String query = "SELECT COUNT(*) FROM audit_logs";

        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(query);

            if (rs.next()) {
                return rs.getInt(1);
            }

            return 0;
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { /* ignored */ }
            if (stmt != null) try { stmt.close(); } catch (SQLException e) { /* ignored */ }
            // Don't close the connection here
        }
    }

    public int getCountForDate(LocalDate date) throws SQLException {
        String query = "SELECT COUNT(*) FROM audit_logs WHERE DATE(timestamp) = ?";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            stmt = conn.prepareStatement(query);
            stmt.setDate(1, java.sql.Date.valueOf(date));
            rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }

            return 0;
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { /* ignored */ }
            if (stmt != null) try { stmt.close(); } catch (SQLException e) { /* ignored */ }
            // Don't close the connection here
        }
    }

    public int getActiveUsersCount() throws SQLException {
        String query = "SELECT COUNT(DISTINCT user_id) FROM audit_logs";

        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(query);

            if (rs.next()) {
                return rs.getInt(1);
            }

            return 0;
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { /* ignored */ }
            if (stmt != null) try { stmt.close(); } catch (SQLException e) { /* ignored */ }
            // Don't close the connection here
        }
    }

    public Map<String, Integer> getActionCounts(int limit) throws SQLException {
        Map<String, Integer> actionCounts = new HashMap<>();
        String query = "SELECT action, COUNT(*) as count FROM audit_logs GROUP BY action ORDER BY count DESC LIMIT ?";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, limit);
            rs = stmt.executeQuery();

            while (rs.next()) {
                actionCounts.put(rs.getString("action"), rs.getInt("count"));
            }

            return actionCounts;
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { /* ignored */ }
            if (stmt != null) try { stmt.close(); } catch (SQLException e) { /* ignored */ }
            // Don't close the connection here
        }
    }
    public boolean delete(long id) throws SQLException {
        String query = "DELETE FROM audit_logs WHERE id = ?";

        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = getConnection();
            ps = conn.prepareStatement(query);
            ps.setLong(1, id);

            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        } finally {
            if (ps != null) try { ps.close(); } catch (SQLException e) { /* ignored */ }
            // Don't close the connection here
        }
    }

    public Map<String, Integer> getUserCounts(int limit) throws SQLException {
        Map<String, Integer> userCounts = new HashMap<>();
        String query = "SELECT username, COUNT(*) as count FROM audit_logs GROUP BY username ORDER BY count DESC LIMIT ?";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, limit);
            rs = stmt.executeQuery();

            while (rs.next()) {
                userCounts.put(rs.getString("username"), rs.getInt("count"));
            }

            return userCounts;
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { /* ignored */ }
            if (stmt != null) try { stmt.close(); } catch (SQLException e) { /* ignored */ }
            // Don't close the connection here
        }
    }

    public Map<String, Integer> getEntityTypeCounts(int limit) throws SQLException {
        Map<String, Integer> entityTypeCounts = new HashMap<>();
        String query = "SELECT entity_type, COUNT(*) as count FROM audit_logs GROUP BY entity_type ORDER BY count DESC LIMIT ?";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, limit);
            rs = stmt.executeQuery();

            while (rs.next()) {
                entityTypeCounts.put(rs.getString("entity_type"), rs.getInt("count"));
            }

            return entityTypeCounts;
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { /* ignored */ }
            if (stmt != null) try { stmt.close(); } catch (SQLException e) { /* ignored */ }
            // Don't close the connection here
        }
    }

    public List<AuditLog> findRecent(int limit) throws SQLException {
        List<AuditLog> logs = new ArrayList<>();
        String query = "SELECT * FROM audit_logs ORDER BY timestamp DESC LIMIT ?";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, limit);
            rs = stmt.executeQuery();

            while (rs.next()) {
                logs.add(mapResultSetToAuditLog(rs));
            }

            return logs;
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { /* ignored */ }
            if (stmt != null) try { stmt.close(); } catch (SQLException e) { /* ignored */ }
            // Don't close the connection here
        }
    }

    public List<AuditLog> findPaginated(int offset, int limit) throws SQLException {
        List<AuditLog> logs = new ArrayList<>();
        String query = "SELECT * FROM audit_logs ORDER BY timestamp DESC LIMIT ? OFFSET ?";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, limit);
            stmt.setInt(2, offset);
            rs = stmt.executeQuery();

            while (rs.next()) {
                logs.add(mapResultSetToAuditLog(rs));
            }

            return logs;
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { /* ignored */ }
            if (stmt != null) try { stmt.close(); } catch (SQLException e) { /* ignored */ }
            // Don't close the connection here
        }
    }

    private AuditLog mapResultSetToAuditLog(ResultSet rs) throws SQLException {
        AuditLog log = new AuditLog();
        log.setId(rs.getLong("id"));
        log.setAction(rs.getString("action"));
        log.setEntityType(rs.getString("entity_type"));
        log.setEntityId(rs.getLong("entity_id"));
        log.setDetails(rs.getString("details"));
        log.setUserId(rs.getInt("user_id"));
        log.setUsername(rs.getString("username"));
        log.setTimestamp(rs.getTimestamp("timestamp"));
        return log;
    }
}

