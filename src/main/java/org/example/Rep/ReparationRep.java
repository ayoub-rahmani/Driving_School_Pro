package org.example.Rep;

import org.example.Entities.Reparation;
import org.example.Utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ReparationRep {

    private Connection getConnection() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }

    // Create or Update
    public Reparation save(Reparation reparation) {
        String sql;

        if (reparation.getId() == null) {
            // Insert new record
            sql = "INSERT INTO reparation (vehicule_id, facture_id, description, date_reparation, " +
                    "cout, prestataire, facture_path, notes) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        } else {
            // Update existing record
            sql = "UPDATE reparation SET vehicule_id = ?, facture_id = ?, description = ?, " +
                    "date_reparation = ?, cout = ?, prestataire = ?, facture_path = ?, notes = ? " +
                    "WHERE id = ?";
        }

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setLong(1, reparation.getVehiculeId());
            stmt.setObject(2, reparation.getFactureId(), Types.BIGINT); // Handle null
            stmt.setString(3, reparation.getDescription());
            stmt.setDate(4, reparation.getDateReparation() != null ? Date.valueOf(reparation.getDateReparation()) : null);
            stmt.setDouble(5, reparation.getCout());
            stmt.setString(6, reparation.getPrestataire());
            stmt.setString(7, reparation.getFacturePath());
            stmt.setString(8, reparation.getNotes());

            if (reparation.getId() != null) {
                stmt.setLong(9, reparation.getId());
            }

            // Execute the update
            stmt.executeUpdate();

            // Get the generated ID for new records
            if (reparation.getId() == null) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        reparation.setId(generatedKeys.getLong(1));
                    } else {
                        throw new SQLException("Creating reparation failed, no ID obtained.");
                    }
                }
            }

            return reparation;
        } catch (SQLException e) {
            throw new RuntimeException("Error saving reparation: " + e.getMessage(), e);
        }
    }

    // Read
    public List<Reparation> findAll() {
        String sql = "SELECT * FROM reparation ORDER BY date_reparation DESC";
        List<Reparation> reparations = new ArrayList<>();

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                reparations.add(mapRowToReparation(rs));
            }

            return reparations;
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving all reparations: " + e.getMessage(), e);
        }
    }

    public Optional<Reparation> findById(Long id) {
        String sql = "SELECT * FROM reparation WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToReparation(rs));
                } else {
                    return Optional.empty();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding reparation by ID: " + e.getMessage(), e);
        }
    }

    public List<Reparation> findByVehiculeId(Long vehiculeId) {
        String sql = "SELECT * FROM reparation WHERE vehicule_id = ? ORDER BY date_reparation DESC";
        List<Reparation> reparations = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, vehiculeId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    reparations.add(mapRowToReparation(rs));
                }
            }

            return reparations;
        } catch (SQLException e) {
            throw new RuntimeException("Error finding reparations by vehicule ID: " + e.getMessage(), e);
        }
    }

    public List<Reparation> findByDescription(String description) {
        String sql = "SELECT * FROM reparation WHERE LOWER(description) LIKE ? ORDER BY date_reparation DESC";
        List<Reparation> reparations = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + description.toLowerCase() + "%");

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    reparations.add(mapRowToReparation(rs));
                }
            }

            return reparations;
        } catch (SQLException e) {
            throw new RuntimeException("Error finding reparations by description: " + e.getMessage(), e);
        }
    }

    public List<Reparation> findByVehiculeIdAndDescription(Long vehiculeId, String description) {
        String sql = "SELECT * FROM reparation WHERE vehicule_id = ? AND LOWER(description) LIKE ? ORDER BY date_reparation DESC";
        List<Reparation> reparations = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, vehiculeId);
            stmt.setString(2, "%" + description.toLowerCase() + "%");

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    reparations.add(mapRowToReparation(rs));
                }
            }

            return reparations;
        } catch (SQLException e) {
            throw new RuntimeException("Error finding reparations by vehicule ID and description: " + e.getMessage(), e);
        }
    }

    public List<Reparation> findByDateRange(LocalDate startDate, LocalDate endDate) {
        String sql = "SELECT * FROM reparation WHERE date_reparation BETWEEN ? AND ? ORDER BY date_reparation DESC";
        List<Reparation> reparations = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(startDate));
            stmt.setDate(2, Date.valueOf(endDate));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    reparations.add(mapRowToReparation(rs));
                }
            }

            return reparations;
        } catch (SQLException e) {
            throw new RuntimeException("Error finding reparations by date range: " + e.getMessage(), e);
        }
    }

    public List<Reparation> findByVehiculeIdAndDateRange(Long vehiculeId, LocalDate startDate, LocalDate endDate) {
        String sql = "SELECT * FROM reparation WHERE vehicule_id = ? AND date_reparation BETWEEN ? AND ? ORDER BY date_reparation DESC";
        List<Reparation> reparations = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, vehiculeId);
            stmt.setDate(2, Date.valueOf(startDate));
            stmt.setDate(3, Date.valueOf(endDate));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    reparations.add(mapRowToReparation(rs));
                }
            }

            return reparations;
        } catch (SQLException e) {
            throw new RuntimeException("Error finding reparations by vehicule ID and date range: " + e.getMessage(), e);
        }
    }

    public List<Reparation> findByPrestataire(String prestataire) {
        String sql = "SELECT * FROM reparation WHERE LOWER(prestataire) LIKE ? ORDER BY date_reparation DESC";
        List<Reparation> reparations = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + prestataire.toLowerCase() + "%");

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    reparations.add(mapRowToReparation(rs));
                }
            }

            return reparations;
        } catch (SQLException e) {
            throw new RuntimeException("Error finding reparations by prestataire: " + e.getMessage(), e);
        }
    }

    public List<Reparation> findByVehiculeIdAndPrestataire(Long vehiculeId, String prestataire) {
        String sql = "SELECT * FROM reparation WHERE vehicule_id = ? AND LOWER(prestataire) LIKE ? ORDER BY date_reparation DESC";
        List<Reparation> reparations = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, vehiculeId);
            stmt.setString(2, "%" + prestataire.toLowerCase() + "%");

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    reparations.add(mapRowToReparation(rs));
                }
            }

            return reparations;
        } catch (SQLException e) {
            throw new RuntimeException("Error finding reparations by vehicule ID and prestataire: " + e.getMessage(), e);
        }
    }

    // Delete
    public void delete(Reparation reparation) {
        if (reparation.getId() != null) {
            deleteById(reparation.getId());
        }
    }

    public void deleteById(Long id) {
        String sql = "DELETE FROM reparation WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error deleting reparation: " + e.getMessage(), e);
        }
    }

    // Helper method to map a ResultSet row to a Reparation object
    private Reparation mapRowToReparation(ResultSet rs) throws SQLException {
        Long id = rs.getLong("id");
        Long vehiculeId = rs.getLong("vehicule_id");

        // Handle null for facture_id
        Long factureId = rs.getObject("facture_id") != null ? rs.getLong("facture_id") : null;

        String description = rs.getString("description");
        LocalDate dateReparation = rs.getDate("date_reparation") != null ?
                rs.getDate("date_reparation").toLocalDate() : null;
        double cout = rs.getDouble("cout");
        String prestataire = rs.getString("prestataire");
        String facturePath = rs.getString("facture_path");
        String notes = rs.getString("notes");

        return new Reparation(
                id, vehiculeId, factureId, description, dateReparation,
                cout, prestataire, facturePath, notes
        );
    }
}

