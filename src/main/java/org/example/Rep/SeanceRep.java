package org.example.Rep;

import org.example.Entities.Seance;
import org.example.Entities.TypePermis;
import org.example.Entities.TypeSeance;
import org.example.Utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SeanceRep {

    private Connection getConnection() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }

    // Create or Update
    public Seance save(Seance seance) {
        String sql;

        if (seance.getId_seance() == 0) {
            // Insert new record
            sql = "INSERT INTO Seance (id_moniteur, id_candidat, id_vehicule, date_debut, typeseance, typepermis, longtitude, latitude, adresse) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        } else {
            // Update existing record
            sql = "UPDATE Seance SET id_moniteur = ?, id_candidat = ?, id_vehicule = ?, date_debut = ?, " +
                    "typeseance = ?, typepermis = ?, longtitude = ?, latitude = ?, adresse = ? WHERE id_seance = ?";
        }

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setLong(1, seance.getId_moniteur());
            stmt.setLong(2, seance.getId_candidat());

            if (seance.getId_vehicule() != null) {
                stmt.setInt(3, seance.getId_vehicule());
            } else {
                stmt.setNull(3, Types.INTEGER);
            }

            stmt.setTimestamp(4, Timestamp.valueOf(seance.getDate_debut()));
            stmt.setString(5, seance.getTypeseance().toString());
            stmt.setString(6, seance.getTypepermis().toString());
            stmt.setFloat(7, seance.getLongtitude());
            stmt.setFloat(8, seance.getLatitude());
            stmt.setString(9, seance.getAdresse());

            if (seance.getId_seance() != 0) {
                stmt.setInt(10, seance.getId_seance());
            }

            // Execute the update
            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating/updating seance failed, no rows affected.");
            }

            // Get the generated ID for new records
            if (seance.getId_seance() == 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        seance.setId_seance(generatedKeys.getInt(1));
                    } else {
                        throw new SQLException("Creating seance failed, no ID obtained.");
                    }
                }
            }

            return seance;
        } catch (SQLException e) {
            throw new RuntimeException("Error saving seance: " + e.getMessage(), e);
        }
    }

    // Read
    public List<Seance> findAll() {
        String sql = "SELECT * FROM Seance";
        List<Seance> seances = new ArrayList<>();

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                seances.add(mapRowToSeance(rs));
            }

            return seances;
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving all seances: " + e.getMessage(), e);
        }
    }

    public Optional<Seance> findById(int id) {
        String sql = "SELECT * FROM Seance WHERE id_seance = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToSeance(rs));
                } else {
                    return Optional.empty();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding seance by ID: " + e.getMessage(), e);
        }
    }

    // Delete
    public void delete(Seance seance) {
        if (seance.getId_seance() != 0) {
            deleteById(seance.getId_seance());
        }
    }

    public void deleteById(int id) {
        String sql = "DELETE FROM Seance WHERE id_seance = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error deleting seance: " + e.getMessage(), e);
        }
    }

    // Additional queries
    public List<Seance> findByMoniteur(long id_moniteur) {
        String sql = "SELECT * FROM Seance WHERE id_moniteur = ?";
        List<Seance> seances = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id_moniteur);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    seances.add(mapRowToSeance(rs));
                }
            }

            return seances;
        } catch (SQLException e) {
            throw new RuntimeException("Error finding seances by moniteur: " + e.getMessage(), e);
        }
    }

    public List<Seance> findByCandidat(long id_candidat) {
        String sql = "SELECT * FROM Seance WHERE id_candidat = ?";
        List<Seance> seances = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id_candidat);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    seances.add(mapRowToSeance(rs));
                }
            }

            return seances;
        } catch (SQLException e) {
            throw new RuntimeException("Error finding seances by candidat: " + e.getMessage(), e);
        }
    }

    public List<Seance> findByVehicule(int id_vehicule) {
        String sql = "SELECT * FROM Seance WHERE id_vehicule = ?";
        List<Seance> seances = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id_vehicule);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    seances.add(mapRowToSeance(rs));
                }
            }

            return seances;
        } catch (SQLException e) {
            throw new RuntimeException("Error finding seances by vehicule: " + e.getMessage(), e);
        }
    }

    public List<Seance> findByDate(LocalDateTime date) {
        String sql = "SELECT * FROM Seance WHERE DATE(date_debut) = ?";
        List<Seance> seances = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(date.toLocalDate()));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    seances.add(mapRowToSeance(rs));
                }
            }

            return seances;
        } catch (SQLException e) {
            throw new RuntimeException("Error finding seances by date: " + e.getMessage(), e);
        }
    }

    public List<Seance> findByTypeSeance(TypeSeance typeSeance) {
        String sql = "SELECT * FROM Seance WHERE typeseance = ?";
        List<Seance> seances = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, typeSeance.toString());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    seances.add(mapRowToSeance(rs));
                }
            }

            return seances;
        } catch (SQLException e) {
            throw new RuntimeException("Error finding seances by type: " + e.getMessage(), e);
        }
    }

    public List<Seance> findByTypePermis(TypePermis typePermis) {
        String sql = "SELECT * FROM Seance WHERE typepermis = ?";
        List<Seance> seances = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, typePermis.toString());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    seances.add(mapRowToSeance(rs));
                }
            }

            return seances;
        } catch (SQLException e) {
            throw new RuntimeException("Error finding seances by permis type: " + e.getMessage(), e);
        }
    }

    // Improved findByDateRange method to better handle time ranges
    public List<Seance> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        String sql = "SELECT * FROM Seance WHERE " +
                "((date_debut >= ? AND date_debut < ?) OR " +  // Seance starts within range
                "(TIMESTAMPADD(HOUR, 2, date_debut) > ? AND date_debut <= ?) OR " +  // Seance ends within range
                "(date_debut <= ? AND TIMESTAMPADD(HOUR, 2, date_debut) >= ?))";  // Seance spans the entire range

        List<Seance> seances = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setTimestamp(1, Timestamp.valueOf(startDate));
            stmt.setTimestamp(2, Timestamp.valueOf(endDate));
            stmt.setTimestamp(3, Timestamp.valueOf(startDate));
            stmt.setTimestamp(4, Timestamp.valueOf(endDate));
            stmt.setTimestamp(5, Timestamp.valueOf(startDate));
            stmt.setTimestamp(6, Timestamp.valueOf(endDate));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    seances.add(mapRowToSeance(rs));
                }
            }

            return seances;
        } catch (SQLException e) {
            throw new RuntimeException("Error finding seances by date range: " + e.getMessage(), e);
        }
    }

    /**
     * Find seances that conflict with the given seance (same time, same moniteur, same vehicule, or same candidat)
     * Using a 3-hour window for moniteur, vehicule, and candidat availability
     */
    public List<Seance> findConflictingSeances(Seance seance) {
        String sql = "SELECT * FROM Seance WHERE " +
                // Check if the time ranges overlap within a 3-hour window
                "((date_debut <= ? AND TIMESTAMPADD(HOUR, 1.5, date_debut) > ?) OR " +
                "(date_debut >= ? AND date_debut < TIMESTAMPADD(HOUR, 1.5, ?))) " +
                // Check if the resources are the same (moniteur, vehicule, or candidat)
                "AND (id_moniteur = ? OR (id_vehicule = ? AND id_vehicule IS NOT NULL) OR id_candidat = ?) " +
                // Exclude the current seance if it's being updated
                "AND id_seance != ?";

        List<Seance> seances = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Set parameters for time window
            stmt.setTimestamp(1, Timestamp.valueOf(seance.getDate_debut().plusHours(1).plusMinutes(30)));
            stmt.setTimestamp(2, Timestamp.valueOf(seance.getDate_debut()));
            stmt.setTimestamp(3, Timestamp.valueOf(seance.getDate_debut().minusHours(1).minusMinutes(30)));
            stmt.setTimestamp(4, Timestamp.valueOf(seance.getDate_debut()));

            // Set parameters for resources
            stmt.setLong(5, seance.getId_moniteur());

            if (seance.getId_vehicule() != null) {
                stmt.setInt(6, seance.getId_vehicule());
            } else {
                stmt.setNull(6, Types.INTEGER);
            }

            stmt.setLong(7, seance.getId_candidat());
            stmt.setInt(8, seance.getId_seance());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    seances.add(mapRowToSeance(rs));
                }
            }

            return seances;
        } catch (SQLException e) {
            throw new RuntimeException("Error finding conflicting seances: " + e.getMessage(), e);
        }
    }

    /**
     * Find seances for a specific moniteur within a time range
     */
    public List<Seance> findByMoniteurAndTimeRange(long moniteurId, LocalDateTime startTime, LocalDateTime endTime) {
        String sql = "SELECT * FROM Seance WHERE id_moniteur = ? AND " +
                "((date_debut >= ? AND date_debut < ?) OR " +
                "(TIMESTAMPADD(HOUR, 1.5, date_debut) > ? AND date_debut <= ?))";

        List<Seance> seances = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, moniteurId);
            stmt.setTimestamp(2, Timestamp.valueOf(startTime));
            stmt.setTimestamp(3, Timestamp.valueOf(endTime));
            stmt.setTimestamp(4, Timestamp.valueOf(startTime));
            stmt.setTimestamp(5, Timestamp.valueOf(endTime));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    seances.add(mapRowToSeance(rs));
                }
            }

            return seances;
        } catch (SQLException e) {
            throw new RuntimeException("Error finding seances by moniteur and time range: " + e.getMessage(), e);
        }
    }

    /**
     * Find seances for a specific vehicle within a time range
     */
    public List<Seance> findByVehiculeAndTimeRange(int vehiculeId, LocalDateTime startTime, LocalDateTime endTime) {
        String sql = "SELECT * FROM Seance WHERE id_vehicule = ? AND " +
                "((date_debut >= ? AND date_debut < ?) OR " +
                "(TIMESTAMPADD(HOUR, 1.5, date_debut) > ? AND date_debut <= ?))";

        List<Seance> seances = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, vehiculeId);
            stmt.setTimestamp(2, Timestamp.valueOf(startTime));
            stmt.setTimestamp(3, Timestamp.valueOf(endTime));
            stmt.setTimestamp(4, Timestamp.valueOf(startTime));
            stmt.setTimestamp(5, Timestamp.valueOf(endTime));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    seances.add(mapRowToSeance(rs));
                }
            }

            return seances;
        } catch (SQLException e) {
            throw new RuntimeException("Error finding seances by vehicule and time range: " + e.getMessage(), e);
        }
    }

    /**
     * Find seances by candidat within a time range
     */
    public List<Seance> findByCandidatAndTimeRange(long candidatId, LocalDateTime startTime, LocalDateTime endTime) {
        String sql = "SELECT * FROM Seance WHERE id_candidat = ? AND " +
                "((date_debut >= ? AND date_debut < ?) OR " +
                "(TIMESTAMPADD(HOUR, 1.5, date_debut) > ? AND date_debut <= ?))";

        List<Seance> seances = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, candidatId);
            stmt.setTimestamp(2, Timestamp.valueOf(startTime));
            stmt.setTimestamp(3, Timestamp.valueOf(endTime));
            stmt.setTimestamp(4, Timestamp.valueOf(startTime));
            stmt.setTimestamp(5, Timestamp.valueOf(endTime));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    seances.add(mapRowToSeance(rs));
                }
            }

            return seances;
        } catch (SQLException e) {
            throw new RuntimeException("Error finding seances by candidat and time range: " + e.getMessage(), e);
        }
    }

    private Seance mapRowToSeance(ResultSet rs) throws SQLException {
        Seance seance = new Seance();

        seance.setId_seance(rs.getInt("id_seance"));
        seance.setId_moniteur(rs.getLong("id_moniteur"));
        seance.setId_candidat(rs.getLong("id_candidat"));

        // Handle nullable id_vehicule
        int idVehicule = rs.getInt("id_vehicule");
        if (!rs.wasNull()) {
            seance.setId_vehicule(idVehicule);
        }

        seance.setDate_debut(rs.getTimestamp("date_debut").toLocalDateTime());

        String typeSeanceStr = rs.getString("typeseance");
        if (typeSeanceStr != null) {
            seance.setTypeseance(TypeSeance.valueOf(typeSeanceStr));
        }

        String typePermisStr = rs.getString("typepermis");
        if (typePermisStr != null) {
            seance.setTypepermis(TypePermis.valueOf(typePermisStr));
        }

        seance.setLongtitude(rs.getFloat("longtitude"));
        seance.setLatitude(rs.getFloat("latitude"));

        // Get the address
        seance.setAdresse(rs.getString("adresse"));

        return seance;
    }

    // Search method
    public List<Seance> searchSeances(String searchTerm) {
        String sql = "SELECT s.* FROM Seance s " +
                "LEFT JOIN candidat c ON s.id_candidat = c.id " +
                "LEFT JOIN moniteur m ON s.id_moniteur = m.id " +
                "WHERE LOWER(c.nom) LIKE ? OR LOWER(c.prenom) LIKE ? OR " +
                "LOWER(m.nom) LIKE ? OR LOWER(m.prenom) LIKE ? OR " +
                "LOWER(s.adresse) LIKE ? OR " +
                "CAST(s.id_seance AS CHAR) LIKE ?";

        List<Seance> seances = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String searchPattern = "%" + searchTerm.toLowerCase() + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            stmt.setString(3, searchPattern);
            stmt.setString(4, searchPattern);
            stmt.setString(5, searchPattern);
            stmt.setString(6, searchPattern);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    seances.add(mapRowToSeance(rs));
                }
            }

            return seances;
        } catch (SQLException e) {
            throw new RuntimeException("Error searching seances: " + e.getMessage(), e);
        }
    }

    // Filter method
    public List<Seance> filterSeances(TypeSeance typeSeance, TypePermis typePermis,
                                      LocalDateTime startDate, LocalDateTime endDate,
                                      Long moniteurId, Long candidatId, Integer vehiculeId) {
        StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM Seance WHERE 1=1");
        List<Object> params = new ArrayList<>();

        // Add type filter if provided
        if (typeSeance != null) {
            sqlBuilder.append(" AND typeseance = ?");
            params.add(typeSeance.toString());
        }

        // Add permis type filter if provided
        if (typePermis != null) {
            sqlBuilder.append(" AND typepermis = ?");
            params.add(typePermis.toString());
        }

        // Add date range filter if provided
        if (startDate != null) {
            sqlBuilder.append(" AND date_debut >= ?");
            params.add(Timestamp.valueOf(startDate));
        }

        if (endDate != null) {
            sqlBuilder.append(" AND date_debut <= ?");
            params.add(Timestamp.valueOf(endDate));
        }

        // Add moniteur filter if provided
        if (moniteurId != null) {
            sqlBuilder.append(" AND id_moniteur = ?");
            params.add(moniteurId);
        }

        // Add candidat filter if provided
        if (candidatId != null) {
            sqlBuilder.append(" AND id_candidat = ?");
            params.add(candidatId);
        }

        // Add vehicule filter if provided
        if (vehiculeId != null) {
            sqlBuilder.append(" AND id_vehicule = ?");
            params.add(vehiculeId);
        }

        List<Seance> seances = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sqlBuilder.toString())) {

            // Set parameters
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    seances.add(mapRowToSeance(rs));
                }
            }

            return seances;
        } catch (SQLException e) {
            throw new RuntimeException("Error filtering seances: " + e.getMessage(), e);
        }
    }
}

