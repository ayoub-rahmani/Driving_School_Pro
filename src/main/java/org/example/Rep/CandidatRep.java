package org.example.Rep;

import org.example.Entities.Candidat;
import org.example.Utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class CandidatRep {

    private Connection getConnection() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }

    // Create
    public Candidat save(Candidat candidat) {
        String sql;

        if (candidat.getId() == null) {
            // Insert new record
            sql = "INSERT INTO candidat (nom, prenom, cin, date_naissance, telephone, adresse, email, " +
                    "date_inscription, categories_permis, chemin_photo_cin, chemin_photo_identite, " +
                    "chemin_certificat_medical,chemin_fiche_pdf, actif) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?, ?)";
        } else {
            // Update existing record
            sql = "UPDATE candidat SET nom = ?, prenom = ?, cin = ?, date_naissance = ?, telephone = ?, " +
                    "adresse = ?, email = ?, date_inscription = ?, categories_permis = ?, " +
                    "chemin_photo_cin = ?, chemin_photo_identite = ?, chemin_certificat_medical = ?, chemin_fiche_pdf = ?, actif = ? " +
                    "WHERE id = ?";
        }

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, candidat.getNom());
            stmt.setString(2, candidat.getPrenom());
            stmt.setString(3, candidat.getCin());
            stmt.setDate(4, Date.valueOf(candidat.getDateNaissance()));
            stmt.setString(5, candidat.getTelephone());
            stmt.setString(6, candidat.getAdresse());
            stmt.setString(7, candidat.getEmail());
            stmt.setDate(8, Date.valueOf(candidat.getDateInscription()));
            stmt.setString(9, String.join(",", candidat.getCategoriesPermis()));
            stmt.setString(10, candidat.getCheminPhotoCIN());
            stmt.setString(11, candidat.getCheminPhotoIdentite());
            stmt.setString(12, candidat.getCheminCertificatMedical());
            stmt.setString(13, candidat.getCheminFichePdf());
            stmt.setBoolean(14, candidat.isActif());

            if (candidat.getId() != null) {
                stmt.setLong(15, candidat.getId());
            }

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating/updating candidat failed, no rows affected.");
            }

            if (candidat.getId() == null) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        candidat.setId(generatedKeys.getLong(1));
                    } else {
                        throw new SQLException("Creating candidat failed, no ID obtained.");
                    }
                }
            }

            return candidat;
        } catch (SQLException e) {
            throw new RuntimeException("Error saving candidat: " + e.getMessage(), e);
        }
    }

    // Read
    public List<Candidat> findAll() {
        String sql = "SELECT * FROM candidat";
        List<Candidat> candidats = new ArrayList<>();

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                candidats.add(mapRowToCandidat(rs));
            }

            return candidats;
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving all candidats: " + e.getMessage(), e);
        }
    }

    public Optional<Candidat> findById(Long id) {
        String sql = "SELECT * FROM candidat WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToCandidat(rs));
                } else {
                    return Optional.empty();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding candidat by ID: " + e.getMessage(), e);
        }
    }

    public List<Candidat> findByNomOrPrenomOrCin(String searchTerm) {
        String sql = "SELECT * FROM candidat WHERE LOWER(nom) LIKE ? OR LOWER(prenom) LIKE ? OR LOWER(cin) LIKE ?";
        List<Candidat> candidats = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String searchPattern = "%" + searchTerm.toLowerCase() + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            stmt.setString(3, searchPattern);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    candidats.add(mapRowToCandidat(rs));
                }
            }

            return candidats;
        } catch (SQLException e) {
            throw new RuntimeException("Error searching candidats: " + e.getMessage(), e);
        }
    }

    // Delete
    public void delete(Candidat candidat) {
        if (candidat.getId() != null) {
            deleteById(candidat.getId());
        }
    }

    public void deleteById(Long id) {
        String sql = "DELETE FROM candidat WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error deleting candidat: " + e.getMessage(), e);
        }
    }

    // Additional queries
    public List<Candidat> findByActif(boolean actif) {
        String sql = "SELECT * FROM candidat WHERE actif = ?";
        List<Candidat> candidats = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setBoolean(1, actif);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    candidats.add(mapRowToCandidat(rs));
                }
            }

            return candidats;
        } catch (SQLException e) {
            throw new RuntimeException("Error finding candidats by status: " + e.getMessage(), e);
        }
    }

    public List<Candidat> findByCategoriePermis(String categorie) {
        String sql = "SELECT * FROM candidat WHERE categories_permis LIKE ?";
        List<Candidat> candidats = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Look for the category in the comma-separated list
            stmt.setString(1, "%" + categorie + "%");

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    candidats.add(mapRowToCandidat(rs));
                }
            }

            return candidats;
        } catch (SQLException e) {
            throw new RuntimeException("Error finding candidats by categorie: " + e.getMessage(), e);
        }
    }

    // NEW METHODS FOR UNIQUENESS VALIDATION
    public boolean existsByCin(String cin, Long excludeId) {
        String sql = "SELECT COUNT(*) FROM candidat WHERE cin = ?";
        if (excludeId != null) {
            sql += " AND id != ?";
        }

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, cin);
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
            throw new RuntimeException("Error checking CIN uniqueness: " + e.getMessage(), e);
        }
    }

    public boolean existsByEmail(String email, Long excludeId) {
        String sql = "SELECT COUNT(*) FROM candidat WHERE email = ?";
        if (excludeId != null) {
            sql += " AND id != ?";
        }

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
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
            throw new RuntimeException("Error checking email uniqueness: " + e.getMessage(), e);
        }
    }

    public boolean existsByTelephone(String telephone, Long excludeId) {
        String sql = "SELECT COUNT(*) FROM candidat WHERE telephone = ?";
        if (excludeId != null) {
            sql += " AND id != ?";
        }

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, telephone);
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
            throw new RuntimeException("Error checking telephone uniqueness: " + e.getMessage(), e);
        }
    }

    public boolean existsCinInMoniteur(String cin) {
        String sql = "SELECT COUNT(*) FROM moniteur WHERE cin = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, cin);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
                return false;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error checking CIN in moniteur table: " + e.getMessage(), e);
        }
    }

    public boolean existsTelephoneInMoniteur(String telephone) {
        String sql = "SELECT COUNT(*) FROM moniteur WHERE telephone = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, telephone);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
                return false;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error checking telephone in moniteur table: " + e.getMessage(), e);
        }
    }

    private Candidat mapRowToCandidat(ResultSet rs) throws SQLException {
        Long id = rs.getLong("id");
        String nom = rs.getString("nom");
        String prenom = rs.getString("prenom");
        String cin = rs.getString("cin");
        LocalDate dateNaissance = rs.getDate("date_naissance").toLocalDate();
        String telephone = rs.getString("telephone");
        String adresse = rs.getString("adresse");
        String email = rs.getString("email");
        LocalDate dateInscription = rs.getDate("date_inscription").toLocalDate();

        // Parse categories from comma-separated string
        String categoriesString = rs.getString("categories_permis");
        List<String> categories = new ArrayList<>(Arrays.asList(categoriesString.split(",")));

        String cheminPhotoCIN = rs.getString("chemin_photo_cin");
        String cheminPhotoIdentite = rs.getString("chemin_photo_identite");
        String cheminCertificatMedical = rs.getString("chemin_certificat_medical");
        String cheminFichePdf = rs.getString("chemin_fiche_pdf");
        boolean actif = rs.getBoolean("actif");

        return new Candidat(id, nom, prenom, cin, dateNaissance, telephone,
                adresse, email, dateInscription, categories, cheminPhotoCIN,
                cheminPhotoIdentite, cheminCertificatMedical,cheminFichePdf,"", actif);
    }
}