package org.example.Rep;

import org.example.Entities.TypePermis;
import org.example.Entities.Vehicule;
import org.example.Utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class VehiculeRep {

    private Connection getConnection() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }

    // Create or Update
    public Vehicule save(Vehicule vehicule) {
        String sql;

        if (vehicule.getId() == null) {
            // Insert new record
            sql = "INSERT INTO vehicule (marque, modele, matricule, kilometrage, type_permis, " +
                    "date_mise_en_service, date_prochain_entretien, date_vignette, date_assurance, " +
                    "date_visite_technique, papiers, disponible, motif_indisponibilite, notes) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        } else {
            // Update existing record
            sql = "UPDATE vehicule SET marque = ?, modele = ?, matricule = ?, kilometrage = ?, " +
                    "type_permis = ?, date_mise_en_service = ?, date_prochain_entretien = ?, " +
                    "date_vignette = ?, date_assurance = ?, date_visite_technique = ?, " +
                    "papiers = ?, disponible = ?, motif_indisponibilite = ?, notes = ? " +
                    "WHERE id = ?";
        }

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, vehicule.getMarque());
            stmt.setString(2, vehicule.getModele());
            stmt.setString(3, vehicule.getMatricule());
            stmt.setInt(4, vehicule.getKilometrage());
            stmt.setString(5, vehicule.getType() != null ? vehicule.getType().name() : null);
            stmt.setDate(6, vehicule.getDateMiseEnService() != null ? Date.valueOf(vehicule.getDateMiseEnService()) : null);
            stmt.setDate(7, vehicule.getDateProchainEntretien() != null ? Date.valueOf(vehicule.getDateProchainEntretien()) : null);
            stmt.setDate(8, vehicule.getDateVignette() != null ? Date.valueOf(vehicule.getDateVignette()) : null);
            stmt.setDate(9, vehicule.getDateAssurance() != null ? Date.valueOf(vehicule.getDateAssurance()) : null);
            stmt.setDate(10, vehicule.getDateVisiteTechnique() != null ? Date.valueOf(vehicule.getDateVisiteTechnique()) : null);
            stmt.setString(11, vehicule.getPapiers());
            stmt.setBoolean(12, vehicule.isDisponible());
            stmt.setString(13, vehicule.getMotifIndisponibilite());
            stmt.setString(14, vehicule.getNotes());

            if (vehicule.getId() != null) {
                stmt.setLong(15, vehicule.getId());
            }

            // Execute the update
            stmt.executeUpdate();

            // Get the generated ID for new records
            if (vehicule.getId() == null) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        vehicule.setId(generatedKeys.getLong(1));
                    } else {
                        throw new SQLException("Creating vehicule failed, no ID obtained.");
                    }
                }
            }

            return vehicule;
        } catch (SQLException e) {
            throw new RuntimeException("Error saving vehicule: " + e.getMessage(), e);
        }
    }

    // Read
    public List<Vehicule> findAll() {
        String sql = "SELECT * FROM vehicule";
        List<Vehicule> vehicules = new ArrayList<>();

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                vehicules.add(mapRowToVehicule(rs));
            }

            return vehicules;
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving all vehicules: " + e.getMessage(), e);
        }
    }

    public Optional<Vehicule> findById(Long id) {
        String sql = "SELECT * FROM vehicule WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToVehicule(rs));
                } else {
                    return Optional.empty();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding vehicule by ID: " + e.getMessage(), e);
        }
    }

    public List<Vehicule> findByMarqueOrModeleOrMatricule(String searchTerm) {
        String sql = "SELECT * FROM vehicule WHERE LOWER(marque) LIKE ? OR LOWER(modele) LIKE ? OR LOWER(matricule) LIKE ?";
        List<Vehicule> vehicules = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String searchPattern = "%" + searchTerm.toLowerCase() + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            stmt.setString(3, searchPattern);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    vehicules.add(mapRowToVehicule(rs));
                }
            }

            return vehicules;
        } catch (SQLException e) {
            throw new RuntimeException("Error searching vehicules: " + e.getMessage(), e);
        }
    }

    // Delete
    public void delete(Vehicule vehicule) {
        if (vehicule.getId() != null) {
            deleteById(vehicule.getId());
        }
    }

    public void deleteById(Long id) {
        String sql = "DELETE FROM vehicule WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error deleting vehicule: " + e.getMessage(), e);
        }
    }

    // Additional queries
    public List<Vehicule> findByDisponible(boolean disponible) {
        String sql = "SELECT * FROM vehicule WHERE disponible = ?";
        List<Vehicule> vehicules = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setBoolean(1, disponible);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    vehicules.add(mapRowToVehicule(rs));
                }
            }

            return vehicules;
        } catch (SQLException e) {
            throw new RuntimeException("Error finding vehicules by disponibilité: " + e.getMessage(), e);
        }
    }

    public List<Vehicule> findByType(TypePermis type) {
        String sql = "SELECT * FROM vehicule WHERE type_permis = ?";
        List<Vehicule> vehicules = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, type.name());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    vehicules.add(mapRowToVehicule(rs));
                }
            }

            return vehicules;
        } catch (SQLException e) {
            throw new RuntimeException("Error finding vehicules by type: " + e.getMessage(), e);
        }
    }

    public List<Vehicule> findExpiringDocuments(int daysThreshold) {
        LocalDate thresholdDate = LocalDate.now().plusDays(daysThreshold);
        Date sqlThresholdDate = Date.valueOf(thresholdDate);

        String sql = "SELECT * FROM vehicule WHERE " +
                "(date_vignette IS NOT NULL AND date_vignette <= ?) OR " +
                "(date_assurance IS NOT NULL AND date_assurance <= ?) OR " +
                "(date_visite_technique IS NOT NULL AND date_visite_technique <= ?) OR " +
                "(date_prochain_entretien IS NOT NULL AND date_prochain_entretien <= ?)";

        List<Vehicule> vehicules = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, sqlThresholdDate);
            stmt.setDate(2, sqlThresholdDate);
            stmt.setDate(3, sqlThresholdDate);
            stmt.setDate(4, sqlThresholdDate);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    vehicules.add(mapRowToVehicule(rs));
                }
            }

            return vehicules;
        } catch (SQLException e) {
            throw new RuntimeException("Error finding vehicules with expiring documents: " + e.getMessage(), e);
        }
    }

    // Uniqueness validation
    public boolean existsByMatricule(String matricule, Long excludeId) {
        String sql = "SELECT COUNT(*) FROM vehicule WHERE matricule = ?";
        if (excludeId != null) {
            sql += " AND id != ?";
        }

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, matricule);
            if (excludeId != null) {
                stmt.setLong(2, excludeId);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
                return false;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error checking matricule uniqueness: " + e.getMessage(), e);
        }
    }

    private Vehicule mapRowToVehicule(ResultSet rs) throws SQLException {
        Long id = rs.getLong("id");
        String marque = rs.getString("marque");
        String modele = rs.getString("modele");
        String matricule = rs.getString("matricule");
        int kilometrage = rs.getInt("kilometrage");

        TypePermis type = null;
        String typeStr = rs.getString("type_permis");
        if (typeStr != null && !typeStr.isEmpty()) {
            try {
                type = TypePermis.valueOf(typeStr);
            } catch (IllegalArgumentException e) {
                // Handle invalid enum value
                System.err.println("Invalid TypePermis value: " + typeStr);
            }
        }

        LocalDate dateMiseEnService = rs.getDate("date_mise_en_service") != null ?
                rs.getDate("date_mise_en_service").toLocalDate() : null;
        LocalDate dateProchainEntretien = rs.getDate("date_prochain_entretien") != null ?
                rs.getDate("date_prochain_entretien").toLocalDate() : null;
        LocalDate dateVignette = rs.getDate("date_vignette") != null ?
                rs.getDate("date_vignette").toLocalDate() : null;
        LocalDate dateAssurance = rs.getDate("date_assurance") != null ?
                rs.getDate("date_assurance").toLocalDate() : null;
        LocalDate dateVisiteTechnique = rs.getDate("date_visite_technique") != null ?
                rs.getDate("date_visite_technique").toLocalDate() : null;

        String papiers = rs.getString("papiers");
        boolean disponible = rs.getBoolean("disponible");
        String motifIndisponibilite = rs.getString("motif_indisponibilite");
        String notes = rs.getString("notes");

        return new Vehicule(
                id, marque, modele, matricule, kilometrage, type,
                dateMiseEnService, dateProchainEntretien, dateVignette,
                dateAssurance, dateVisiteTechnique, papiers,
                disponible, motifIndisponibilite, notes
        );
    }
}

