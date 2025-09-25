package org.example.Rep;

import org.example.Entities.Moniteur;
import org.example.Utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class MoniteurRep {

    private Connection getConnection() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }

    // Create
    public Moniteur save(Moniteur moniteur) {
        String sql;

        if (moniteur.getId() == null) {
            // Insert new record
            sql = "INSERT INTO moniteur (nom, prenom, cin, date_naissance, telephone, date_embauche, date_fin_contrat, num_permis, categories_permis, disponible, motif, salaire, experience, diplomes, notes) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        } else {
            // Update existing record
            sql = "UPDATE moniteur SET nom = ?, prenom = ?, cin = ?, date_naissance = ?, telephone = ?, " +
                    "date_embauche = ?, date_fin_contrat = ?, num_permis = ?, categories_permis = ?, disponible = ?, motif = ?, salaire = ?, experience = ?, diplomes = ?, notes = ? " +
                    "WHERE id = ?";
        }

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {


            stmt.setString(1, moniteur.getNom());
            stmt.setString(2, moniteur.getPrenom());
            stmt.setString(3, moniteur.getCin());
            stmt.setDate(4, moniteur.getDateNaissance() != null ? Date.valueOf(moniteur.getDateNaissance()) : null);
            stmt.setString(5, moniteur.getTelephone());
            stmt.setDate(6, Date.valueOf(moniteur.getDateEmbauche()));
            stmt.setDate(7, moniteur.getDateFinContrat() != null ? Date.valueOf(moniteur.getDateFinContrat()) : null);
            stmt.setString(8, moniteur.getNumPermis());
            stmt.setString(9, String.join(",", moniteur.getCategoriesPermis()));
            stmt.setBoolean(10, moniteur.isDisponible());
            stmt.setString(11, moniteur.getMotif() != null ? moniteur.getMotif().trim() : "");
            stmt.setDouble(12, moniteur.getSalaire());
            stmt.setDouble(13, moniteur.getExperience());

            stmt.setString(14, moniteur.getDiplomes() != null ? moniteur.getDiplomes().trim() : "");
            stmt.setString(15, moniteur.getNotes() != null ? moniteur.getNotes().trim() : "");

            if (moniteur.getId() != null) {
                stmt.setLong(16, moniteur.getId());
            }

            // Execute the update
            stmt.executeUpdate();

            // Get the generated ID for new records
            if (moniteur.getId() == null) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        moniteur.setId(generatedKeys.getLong(1));
                    } else {
                        throw new SQLException("Creating moniteur failed, no ID obtained.");
                    }
                }
            }

            return moniteur;
        } catch (SQLException e) {
            throw new RuntimeException("Error saving moniteur: " + e.getMessage(), e);
        }
    }

    // Read
    public List<Moniteur> findAll() {
        String sql = "SELECT * FROM moniteur";
        List<Moniteur> moniteurs = new ArrayList<>();

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                moniteurs.add(mapRowToMoniteur(rs));
            }

            return moniteurs;
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving all moniteurs: " + e.getMessage(), e);
        }
    }

    public Optional<Moniteur> findById(Long id) {
        String sql = "SELECT * FROM moniteur WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToMoniteur(rs));
                } else {
                    return Optional.empty();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding moniteur by ID: " + e.getMessage(), e);
        }
    }

    public List<Moniteur> findByNomOrPrenomOrCin(String searchTerm) {
        String sql = "SELECT * FROM moniteur WHERE LOWER(nom) LIKE ? OR LOWER(prenom) LIKE ? OR LOWER(cin) LIKE ?";
        List<Moniteur> moniteurs = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String searchPattern = "%" + searchTerm.toLowerCase() + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            stmt.setString(3, searchPattern);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    moniteurs.add(mapRowToMoniteur(rs));
                }
            }

            return moniteurs;
        } catch (SQLException e) {
            throw new RuntimeException("Error searching moniteurs: " + e.getMessage(), e);
        }
    }

    // Delete
    public void delete(Moniteur moniteur) {
        if (moniteur.getId() != null) {
            deleteById(moniteur.getId());
        }
    }

    public void deleteById(Long id) {
        String sql = "DELETE FROM moniteur WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error deleting moniteur: " + e.getMessage(), e);
        }
    }

    // Additional queries
    public List<Moniteur> findByDisponible(boolean disponible) {
        String sql = "SELECT * FROM moniteur WHERE disponible = ?";
        List<Moniteur> moniteurs = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setBoolean(1, disponible);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    moniteurs.add(mapRowToMoniteur(rs));
                }
            }

            return moniteurs;
        } catch (SQLException e) {
            throw new RuntimeException("Error finding moniteurs by disponibilité: " + e.getMessage(), e);
        }
    }

    public List<Moniteur> findByCategoriePermis(String categorie) {
        String sql = "SELECT * FROM moniteur WHERE categories_permis LIKE ?";
        List<Moniteur> moniteurs = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Look for the category in the comma-separated list
            stmt.setString(1, "%" + categorie + "%");

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    moniteurs.add(mapRowToMoniteur(rs));
                }
            }

            return moniteurs;
        } catch (SQLException e) {
            throw new RuntimeException("Error finding moniteurs by categorie: " + e.getMessage(), e);
        }
    }

    // NEW METHODS FOR UNIQUENESS VALIDATION
    public boolean existsByCin(String cin, Long excludeId) {
        String sql = "SELECT COUNT(*) FROM moniteur WHERE cin = ?";
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

    public boolean existsByTelephone(String telephone, Long excludeId) {
        String sql = "SELECT COUNT(*) FROM moniteur WHERE telephone = ?";
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

    public boolean existsByNumPermis(String numPermis, Long excludeId) {
        String sql = "SELECT COUNT(*) FROM moniteur WHERE num_permis = ?";
        if (excludeId != null) {
            sql += " AND id != ?";
        }

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, numPermis);
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
            throw new RuntimeException("Error checking numPermis uniqueness: " + e.getMessage(), e);
        }
    }

    public boolean existsCinInCandidat(String cin) {
        String sql = "SELECT COUNT(*) FROM candidat WHERE cin = ?";

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
            throw new RuntimeException("Error checking CIN in candidat table: " + e.getMessage(), e);
        }
    }

    public boolean existsTelephoneInCandidat(String telephone) {
        String sql = "SELECT COUNT(*) FROM candidat WHERE telephone = ?";

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
            throw new RuntimeException("Error checking telephone in candidat table: " + e.getMessage(), e);
        }
    }

    private Moniteur mapRowToMoniteur(ResultSet rs) throws SQLException {
        Long id = rs.getLong("id");
        String nom = rs.getString("nom") != null ? rs.getString("nom") : "";
        String prenom = rs.getString("prenom")!= null ? rs.getString("prenom") : "";
        String cin = rs.getString("cin")!= null ? rs.getString("cin") : "";
        LocalDate dateNaissance = rs.getDate("date_naissance") != null ? rs.getDate("date_naissance").toLocalDate() : null;
        String telephone = rs.getString("telephone")!= null ? rs.getString("telephone") : "";
        LocalDate dateEmbauche = rs.getDate("date_embauche")!= null ? rs.getDate("date_embauche").toLocalDate() : null;
        LocalDate dateFinContrat = rs.getDate("date_fin_contrat") != null ? rs.getDate("date_fin_contrat").toLocalDate() : null;
        String numPermis = rs.getString("num_permis")!= null ? rs.getString("num_permis") : "";

        // Parse categories from comma-separated string
        String categoriesString = rs.getString("categories_permis");
        List<String> categories = new ArrayList<>(Arrays.asList(categoriesString.split(",")));

        boolean disponible = rs.getBoolean("disponible");
        String motif = rs.getString("motif") != null ? rs.getString("motif") : "";
        double salaire = rs.getDouble("salaire");
        double experience = rs.getDouble("experience");
        String diplomes = rs.getString("diplomes")!= null ? rs.getString("diplomes") : "";
        String notes = rs.getString("notes")!= null ? rs.getString("notes") : "";

        return new Moniteur(id, nom, prenom, cin, dateNaissance, telephone,
                dateEmbauche,dateFinContrat, numPermis, categories, disponible,motif, salaire,experience,diplomes,notes);
    }
}