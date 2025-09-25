package org.example.Rep;

import org.example.Entities.Candidat;
import org.example.Entities.Examen;
import org.example.Utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ExamenRep {

    private Connection getConnection() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }

    // Create
    public Examen save(Examen examen) {
        String sql;

        if (examen.getId() == null) {
            // Insert new record
            sql = "INSERT INTO examen (candidat_id, type_examen, date_examen, lieu_examen, frais_inscription, est_valide) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
        } else {
            // Update existing record
            sql = "UPDATE examen SET candidat_id = ?, type_examen = ?, date_examen = ?, lieu_examen = ?, frais_inscription = ?, est_valide = ? " +
                    "WHERE id = ?";
        }

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setLong(1, examen.getCandidatId());
            stmt.setString(2, examen.getTypeExamen());
            stmt.setDate(3, Date.valueOf(examen.getDateExamen()));
            stmt.setString(4, examen.getLieuExamen());
            stmt.setDouble(5, examen.getFraisInscription());
            stmt.setBoolean(6, examen.isEstValide());

            if (examen.getId() != null) {
                stmt.setLong(7, examen.getId());
            }

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating/updating examen failed, no rows affected.");
            }

            if (examen.getId() == null) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        examen.setId(generatedKeys.getLong(1));
                    } else {
                        throw new SQLException("Creating examen failed, no ID obtained.");
                    }
                }
            }

            return examen;
        } catch (SQLException e) {
            throw new RuntimeException("Error saving examen: " + e.getMessage(), e);
        }
    }

    // Read
    public List<Examen> findAll() {
        String sql = "SELECT * FROM examen";
        List<Examen> examens = new ArrayList<>();

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                examens.add(mapRowToExamen(rs));
            }

            return examens;
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving all examens: " + e.getMessage(), e);
        }
    }

    public Optional<Examen> findById(Long id) {
        String sql = "SELECT * FROM examen WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToExamen(rs));
                } else {
                    return Optional.empty();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding examen by ID: " + e.getMessage(), e);
        }
    }

    // Delete
    public void delete(Examen examen) {
        if (examen.getId() != null) {
            deleteById(examen.getId());
        }
    }

    public void deleteById(Long id) {
        String sql = "DELETE FROM examen WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error deleting examen: " + e.getMessage(), e);
        }
    }

    private Examen mapRowToExamen(ResultSet rs) throws SQLException {
        Long id = rs.getLong("id");
        Long candidatId = rs.getLong("candidat_id");
        String typeExamen = rs.getString("type_examen");
        LocalDate dateExamen = rs.getDate("date_examen").toLocalDate();
        String lieuExamen = rs.getString("lieu_examen");
        double fraisInscription = rs.getDouble("frais_inscription");
        boolean estValide = rs.getBoolean("est_valide");

        return new Examen(id, candidatId, typeExamen, dateExamen, lieuExamen, fraisInscription, estValide);
    }

    // Updated method to search by candidate name
    public List<Examen> findBySearchTerm(String searchTerm) {
        String sql = "SELECT e.* FROM examen e " +
                "INNER JOIN candidat c ON e.candidat_id = c.id " +
                "WHERE LOWER(c.nom) LIKE ? OR LOWER(c.prenom) LIKE ?";
        List<Examen> examens = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String searchPattern = "%" + searchTerm.toLowerCase() + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    examens.add(mapRowToExamen(rs));
                }
            }

            return examens;
        } catch (SQLException e) {
            throw new RuntimeException("Error searching examens by candidate name: " + e.getMessage(), e);
        }
    }

    // New method for filtering examens
    public List<Examen> findByFilters(String type, LocalDate startDate, LocalDate endDate, String status) {
        StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM examen WHERE 1=1");
        List<Object> params = new ArrayList<>();

        // Add type filter if provided
        if (type != null && !type.isEmpty() && !type.equals("Tous les types")) {
            sqlBuilder.append(" AND type_examen = ?");
            params.add(type);
        }

        // Add date range filter if provided
        if (startDate != null) {
            sqlBuilder.append(" AND date_examen >= ?");
            params.add(Date.valueOf(startDate));
        }

        if (endDate != null) {
            sqlBuilder.append(" AND date_examen <= ?");
            params.add(Date.valueOf(endDate));
        }

        // Add status filter if provided
        if (status != null && !status.isEmpty() && !status.equals("Tous les statuts")) {
            boolean isValid = status.equalsIgnoreCase("Validé");
            sqlBuilder.append(" AND est_valide = ?");
            params.add(isValid);
        }

        List<Examen> examens = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sqlBuilder.toString())) {

            // Set parameters
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    examens.add(mapRowToExamen(rs));
                }
            }

            return examens;
        } catch (SQLException e) {
            throw new RuntimeException("Error filtering examens: " + e.getMessage(), e);
        }
    }
}