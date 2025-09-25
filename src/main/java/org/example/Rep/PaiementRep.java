package org.example.Rep;

import org.example.Entities.Paiement;
import org.example.Utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PaiementRep {

    // Helper method to get a fresh connection for each operation
    private Connection getConnection() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }

    public Paiement save(Paiement paiement) {
        String sql = "INSERT INTO paiements (candidat_id, date_paiement, montant, methode_paiement, reference, statut, notes, remise) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setLong(1, paiement.getCandidatId());
            statement.setDate(2, Date.valueOf(paiement.getDatePaiement()));
            statement.setDouble(3, paiement.getMontant());
            statement.setString(4, paiement.getMethodePaiement());
            statement.setString(5, paiement.getReference());
            statement.setString(6, paiement.getStatut());
            statement.setString(7, paiement.getNotes());
            statement.setDouble(8, paiement.getRemise());

            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating paiement failed, no rows affected.");
            }

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    paiement.setId(generatedKeys.getLong(1));
                } else {
                    throw new SQLException("Creating paiement failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        return paiement;
    }

    public Paiement update(Paiement paiement) {
        // Updated SQL to remove type_service, seance_id, and examen_id
        String sql = "UPDATE paiements SET candidat_id = ?, date_paiement = ?, montant = ?, methode_paiement = ?, reference = ?, statut = ?, notes = ?, remise = ? WHERE id = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, paiement.getCandidatId());
            statement.setDate(2, Date.valueOf(paiement.getDatePaiement()));
            statement.setDouble(3, paiement.getMontant());
            statement.setString(4, paiement.getMethodePaiement());
            statement.setString(5, paiement.getReference());
            statement.setString(6, paiement.getStatut());
            statement.setString(7, paiement.getNotes());
            statement.setDouble(8, paiement.getRemise());
            statement.setLong(9, paiement.getId());

            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Updating paiement failed, no rows affected.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        return paiement;
    }

    public boolean delete(Long id) {
        String sql = "DELETE FROM paiements WHERE id = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);
            int affectedRows = statement.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Paiement getById(Long id) {
        String sql = "SELECT * FROM paiements WHERE id = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return extractPaiementFromResultSet(resultSet);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Paiement> getAll() {
        List<Paiement> paiements = new ArrayList<>();
        String sql = "SELECT * FROM paiements";

        try (Connection connection = getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                Paiement paiement = extractPaiementFromResultSet(resultSet);
                paiements.add(paiement);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return paiements;
    }

    public List<Paiement> getByCandidatId(Long candidatId) {
        List<Paiement> paiements = new ArrayList<>();
        String sql = "SELECT * FROM paiements WHERE candidat_id = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, candidatId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Paiement paiement = extractPaiementFromResultSet(resultSet);
                    paiements.add(paiement);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return paiements;
    }

    public List<Paiement> getByDateRange(LocalDate startDate, LocalDate endDate) {
        List<Paiement> paiements = new ArrayList<>();
        String sql = "SELECT * FROM paiements WHERE date_paiement BETWEEN ? AND ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setDate(1, Date.valueOf(startDate));
            statement.setDate(2, Date.valueOf(endDate));
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Paiement paiement = extractPaiementFromResultSet(resultSet);
                    paiements.add(paiement);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return paiements;
    }

    // Removed getByTypeService method since type_service column doesn't exist

    public List<Paiement> getByMethodePaiement(String methodePaiement) {
        List<Paiement> paiements = new ArrayList<>();
        String sql = "SELECT * FROM paiements WHERE methode_paiement = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, methodePaiement);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Paiement paiement = extractPaiementFromResultSet(resultSet);
                    paiements.add(paiement);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return paiements;
    }

    public List<Paiement> getByStatut(String statut) {
        List<Paiement> paiements = new ArrayList<>();
        String sql = "SELECT * FROM paiements WHERE statut = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, statut);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Paiement paiement = extractPaiementFromResultSet(resultSet);
                    paiements.add(paiement);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return paiements;
    }

    private Paiement extractPaiementFromResultSet(ResultSet resultSet) throws SQLException {
        Paiement paiement = new Paiement();
        paiement.setId(resultSet.getLong("id"));
        paiement.setCandidatId(resultSet.getLong("candidat_id"));
        paiement.setDatePaiement(resultSet.getDate("date_paiement").toLocalDate());
        paiement.setMontant(resultSet.getDouble("montant"));
        paiement.setMethodePaiement(resultSet.getString("methode_paiement"));
        paiement.setReference(resultSet.getString("reference"));

        // Set default values for removed columns to maintain compatibility
        paiement.setTypeService(null);
        paiement.setSeanceId(null);
        paiement.setExamenId(null);

        paiement.setStatut(resultSet.getString("statut"));
        paiement.setNotes(resultSet.getString("notes"));

        // Handle remise column which might be added later
        try {
            paiement.setRemise(resultSet.getDouble("remise"));
        } catch (SQLException e) {
            // If remise column doesn't exist, set default value
            paiement.setRemise(0.0);
        }

        return paiement;
    }
}