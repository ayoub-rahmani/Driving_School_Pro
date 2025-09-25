package org.example.Rep;

import org.example.Entities.DrivingSchoolInfo;
import org.example.Utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DrivingSchoolRepository {
    private Connection connection;

    private Connection getConnection() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }

    // Create a new driving school
    public void add(DrivingSchoolInfo school) throws SQLException {
        String query = "INSERT INTO driving_schools (name, matricule_fiscale, logo_path, address, phone_number, email, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet generatedKeys = null;

        try {
            conn = getConnection();
            stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);

            stmt.setString(1, school.getName());
            stmt.setString(2, school.getMatriculeFiscale());
            stmt.setString(3, school.getLogoPath());
            stmt.setString(4, school.getAddress());
            stmt.setString(5, school.getPhoneNumber());
            stmt.setString(6, school.getEmail());
            stmt.setTimestamp(7, Timestamp.valueOf(school.getCreatedAt() != null ? school.getCreatedAt() : LocalDateTime.now()));
            stmt.setTimestamp(8, Timestamp.valueOf(school.getUpdatedAt() != null ? school.getUpdatedAt() : LocalDateTime.now()));

            stmt.executeUpdate();

            generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                school.setId(generatedKeys.getLong(1));
            }
        } finally {
            if (generatedKeys != null) try { generatedKeys.close(); } catch (SQLException e) { /* ignored */ }
            if (stmt != null) try { stmt.close(); } catch (SQLException e) { /* ignored */ }
        }
    }

    // Update an existing driving school
    public boolean update(DrivingSchoolInfo school) throws SQLException {
        String query = "UPDATE driving_schools SET name = ?, matricule_fiscale = ?, logo_path = ?, address = ?, " +
                "phone_number = ?, email = ?, updated_at = ? WHERE id = ?";

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = getConnection();
            stmt = conn.prepareStatement(query);

            stmt.setString(1, school.getName());
            stmt.setString(2, school.getMatriculeFiscale());
            stmt.setString(3, school.getLogoPath());
            stmt.setString(4, school.getAddress());
            stmt.setString(5, school.getPhoneNumber());
            stmt.setString(6, school.getEmail());
            stmt.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setLong(8, school.getId());

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } finally {
            if (stmt != null) try { stmt.close(); } catch (SQLException e) { /* ignored */ }
        }
    }

    // Delete a driving school
    public boolean delete(long id) throws SQLException {
        String query = "DELETE FROM driving_schools WHERE id = ?";

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = getConnection();
            stmt = conn.prepareStatement(query);
            stmt.setLong(1, id);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } finally {
            if (stmt != null) try { stmt.close(); } catch (SQLException e) { /* ignored */ }
        }
    }

    // Find all driving schools
    public List<DrivingSchoolInfo> findAll() throws SQLException {
        List<DrivingSchoolInfo> schools = new ArrayList<>();
        String query = "SELECT * FROM driving_schools ORDER BY name ASC";

        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(query);

            while (rs.next()) {
                schools.add(mapResultSetToDrivingSchool(rs));
            }
            return schools;
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { /* ignored */ }
            if (stmt != null) try { stmt.close(); } catch (SQLException e) { /* ignored */ }
        }
    }

    // Find by ID
    public DrivingSchoolInfo findById(Long id) throws SQLException {
        String query = "SELECT * FROM driving_schools WHERE id = ?";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            stmt = conn.prepareStatement(query);
            stmt.setLong(1, id);
            rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToDrivingSchool(rs);
            }
            return null;
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { /* ignored */ }
            if (stmt != null) try { stmt.close(); } catch (SQLException e) { /* ignored */ }
        }
    }

    // Find by matricule fiscale
    public DrivingSchoolInfo findByMatriculeFiscale(String matriculeFiscale) throws SQLException {
        String query = "SELECT * FROM driving_schools WHERE matricule_fiscale = ?";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            stmt = conn.prepareStatement(query);
            stmt.setString(1, matriculeFiscale);
            rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToDrivingSchool(rs);
            }
            return null;
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { /* ignored */ }
            if (stmt != null) try { stmt.close(); } catch (SQLException e) { /* ignored */ }
        }
    }

    // Search by name or matricule fiscale
    public List<DrivingSchoolInfo> search(String searchTerm) throws SQLException {
        List<DrivingSchoolInfo> schools = new ArrayList<>();
        String query = "SELECT * FROM driving_schools WHERE name LIKE ? OR matricule_fiscale LIKE ? ORDER BY name ASC";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            stmt = conn.prepareStatement(query);
            String term = "%" + searchTerm + "%";
            stmt.setString(1, term);
            stmt.setString(2, term);
            rs = stmt.executeQuery();

            while (rs.next()) {
                schools.add(mapResultSetToDrivingSchool(rs));
            }
            return schools;
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { /* ignored */ }
            if (stmt != null) try { stmt.close(); } catch (SQLException e) { /* ignored */ }
        }
    }

    // Get total count
    public int getTotalCount() throws SQLException {
        String query = "SELECT COUNT(*) FROM driving_schools";

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
        }
    }

    // Get paginated results
    public List<DrivingSchoolInfo> findPaginated(int offset, int limit) throws SQLException {
        List<DrivingSchoolInfo> schools = new ArrayList<>();
        String query = "SELECT * FROM driving_schools ORDER BY name ASC LIMIT ? OFFSET ?";

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
                schools.add(mapResultSetToDrivingSchool(rs));
            }
            return schools;
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { /* ignored */ }
            if (stmt != null) try { stmt.close(); } catch (SQLException e) { /* ignored */ }
        }
    }

    // Helper method to map ResultSet to DrivingSchoolInfo
    private DrivingSchoolInfo mapResultSetToDrivingSchool(ResultSet rs) throws SQLException {
        DrivingSchoolInfo school = new DrivingSchoolInfo();
        school.setId(rs.getLong("id"));
        school.setName(rs.getString("name"));
        school.setMatriculeFiscale(rs.getString("matricule_fiscale"));
        school.setLogoPath(rs.getString("logo_path"));
        school.setAddress(rs.getString("address"));
        school.setPhoneNumber(rs.getString("phone_number"));
        school.setEmail(rs.getString("email"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            school.setCreatedAt(createdAt.toLocalDateTime());
        }
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            school.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        return school;
    }
}