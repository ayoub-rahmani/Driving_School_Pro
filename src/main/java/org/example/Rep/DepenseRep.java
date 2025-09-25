package org.example.Rep;

import org.example.Entities.Depense;
import org.example.Utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DepenseRep {

    // Helper method to get a fresh connection for each operation
    private Connection getConnection() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }

    public Depense save(Depense depense) {
        String sql = "INSERT INTO depenses (categorie, montant, date_depense, description, vehicule_id, moniteur_id, " +
                "type_vehicule_depense, reparation_id, type_autre_depense, paye, date_creation) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, depense.getCategorie());
            statement.setDouble(2, depense.getMontant());
            statement.setDate(3, Date.valueOf(depense.getDateDepense()));
            statement.setString(4, depense.getDescription());

            if (depense.getVehiculeId() != null) {
                statement.setLong(5, depense.getVehiculeId());
            } else {
                statement.setNull(5, Types.BIGINT);
            }

            if (depense.getMoniteurId() != null) {
                statement.setLong(6, depense.getMoniteurId());
            } else {
                statement.setNull(6, Types.BIGINT);
            }

            // Set the new fields
            statement.setString(7, depense.getTypeVehiculeDepense());

            if (depense.getReparationId() != null) {
                statement.setLong(8, depense.getReparationId());
            } else {
                statement.setNull(8, Types.BIGINT);
            }

            statement.setString(9, depense.getTypeAutreDepense());
            statement.setBoolean(10, depense.isPaye());

            if (depense.getDateCreation() != null) {
                statement.setDate(11, Date.valueOf(depense.getDateCreation()));
            } else {
                statement.setDate(11, Date.valueOf(LocalDate.now()));
            }

            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating depense failed, no rows affected.");
            }

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    depense.setId(generatedKeys.getLong(1));
                } else {
                    throw new SQLException("Creating depense failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        return depense;
    }

    public Depense update(Depense depense) {
        String sql = "UPDATE depenses SET categorie = ?, montant = ?, date_depense = ?, description = ?, " +
                "vehicule_id = ?, moniteur_id = ?, type_vehicule_depense = ?, reparation_id = ?, " +
                "type_autre_depense = ?, paye = ?, date_modification = ? WHERE id = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, depense.getCategorie());
            statement.setDouble(2, depense.getMontant());
            statement.setDate(3, Date.valueOf(depense.getDateDepense()));
            statement.setString(4, depense.getDescription());

            if (depense.getVehiculeId() != null) {
                statement.setLong(5, depense.getVehiculeId());
            } else {
                statement.setNull(5, Types.BIGINT);
            }

            if (depense.getMoniteurId() != null) {
                statement.setLong(6, depense.getMoniteurId());
            } else {
                statement.setNull(6, Types.BIGINT);
            }

            // Set the new fields
            statement.setString(7, depense.getTypeVehiculeDepense());

            if (depense.getReparationId() != null) {
                statement.setLong(8, depense.getReparationId());
            } else {
                statement.setNull(8, Types.BIGINT);
            }

            statement.setString(9, depense.getTypeAutreDepense());
            statement.setBoolean(10, depense.isPaye());
            statement.setDate(11, Date.valueOf(LocalDate.now())); // Set current date for modification
            statement.setLong(12, depense.getId());

            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Updating depense failed, no rows affected.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        return depense;
    }

    public boolean delete(Long id) {
        String sql = "DELETE FROM depenses WHERE id = ?";

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

    public Depense getById(Long id) {
        String sql = "SELECT * FROM depenses WHERE id = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return extractDepenseFromResultSet(resultSet);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Depense> getAll() {
        List<Depense> depenses = new ArrayList<>();
        String sql = "SELECT * FROM depenses";

        try (Connection connection = getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                Depense depense = extractDepenseFromResultSet(resultSet);
                depenses.add(depense);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return depenses;
    }

    public List<Depense> getByVehiculeId(Long vehiculeId) {
        List<Depense> depenses = new ArrayList<>();
        String sql = "SELECT * FROM depenses WHERE vehicule_id = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, vehiculeId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Depense depense = extractDepenseFromResultSet(resultSet);
                    depenses.add(depense);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return depenses;
    }

    public List<Depense> getByMoniteurId(Long moniteurId) {
        List<Depense> depenses = new ArrayList<>();
        String sql = "SELECT * FROM depenses WHERE moniteur_id = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, moniteurId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Depense depense = extractDepenseFromResultSet(resultSet);
                    depenses.add(depense);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return depenses;
    }

    public List<Depense> getByDateRange(LocalDate startDate, LocalDate endDate) {
        List<Depense> depenses = new ArrayList<>();
        String sql = "SELECT * FROM depenses WHERE date_depense BETWEEN ? AND ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setDate(1, Date.valueOf(startDate));
            statement.setDate(2, Date.valueOf(endDate));
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Depense depense = extractDepenseFromResultSet(resultSet);
                    depenses.add(depense);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return depenses;
    }

    public List<Depense> getByCategorie(String categorie) {
        List<Depense> depenses = new ArrayList<>();
        String sql = "SELECT * FROM depenses WHERE categorie = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, categorie);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Depense depense = extractDepenseFromResultSet(resultSet);
                    depenses.add(depense);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return depenses;
    }


    private Depense extractDepenseFromResultSet(ResultSet resultSet) throws SQLException {
        Depense depense = new Depense();
        depense.setId(resultSet.getLong("id"));
        depense.setCategorie(resultSet.getString("categorie"));
        depense.setMontant(resultSet.getDouble("montant"));
        depense.setDateDepense(resultSet.getDate("date_depense").toLocalDate());
        depense.setDescription(resultSet.getString("description"));

        Long vehiculeId = resultSet.getLong("vehicule_id");
        if (!resultSet.wasNull()) {
            depense.setVehiculeId(vehiculeId);
        }

        Long moniteurId = resultSet.getLong("moniteur_id");
        if (!resultSet.wasNull()) {
            depense.setMoniteurId(moniteurId);
        }

        // Extract the new fields
        depense.setTypeVehiculeDepense(resultSet.getString("type_vehicule_depense"));

        Long reparationId = resultSet.getLong("reparation_id");
        if (!resultSet.wasNull()) {
            depense.setReparationId(reparationId);
        }

        depense.setTypeAutreDepense(resultSet.getString("type_autre_depense"));

        // Extract paye field
        depense.setPaye(resultSet.getBoolean("paye"));

        // Extract date fields if they exist in the result set
        try {
            Date dateCreation = resultSet.getDate("date_creation");
            if (dateCreation != null) {
                depense.setDateCreation(dateCreation.toLocalDate());
            }

            Date dateModification = resultSet.getDate("date_modification");
            if (dateModification != null) {
                depense.setDateModification(dateModification.toLocalDate());
            }
        } catch (SQLException e) {
            // Ignore if these columns don't exist
        }

        return depense;
    }
}