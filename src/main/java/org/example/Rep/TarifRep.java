package org.example.Rep;

import org.example.Entities.Tarif;
import org.example.Utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TarifRep {

    // Remove the connection instance variable
    // private Connection connection;

    public TarifRep() {
        // No need to store the connection in the constructor
    }

    // Helper method to get a fresh connection for each operation
    private Connection getConnection() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }

    public Tarif save(Tarif tarif) {
        String sql = "INSERT INTO tarifs (type_service, montant, description, actif) VALUES (?, ?, ?, ?)";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, tarif.getTypeService());
            statement.setDouble(2, tarif.getMontant());
            statement.setString(3, tarif.getDescription());
            statement.setBoolean(4, tarif.isActif());

            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating tarif failed, no rows affected.");
            }

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    tarif.setId(generatedKeys.getLong(1));
                } else {
                    throw new SQLException("Creating tarif failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        return tarif;
    }

    public Tarif update(Tarif tarif) {
        String sql = "UPDATE tarifs SET type_service = ?, montant = ?, description = ?, actif = ? WHERE id = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, tarif.getTypeService());
            statement.setDouble(2, tarif.getMontant());
            statement.setString(3, tarif.getDescription());
            statement.setBoolean(4, tarif.isActif());
            statement.setLong(5, tarif.getId());

            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Updating tarif failed, no rows affected.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        return tarif;
    }

    public boolean delete(Long id) {
        String sql = "DELETE FROM tarifs WHERE id = ?";

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

    public Tarif getById(Long id) {
        String sql = "SELECT * FROM tarifs WHERE id = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return extractTarifFromResultSet(resultSet);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Tarif> getAll() {
        List<Tarif> tarifs = new ArrayList<>();
        String sql = "SELECT * FROM tarifs";

        try (Connection connection = getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                Tarif tarif = extractTarifFromResultSet(resultSet);
                tarifs.add(tarif);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tarifs;
    }

    public Tarif getByTypeService(String typeService) {
        String sql = "SELECT * FROM tarifs WHERE type_service = ? AND actif = true";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, typeService);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return extractTarifFromResultSet(resultSet);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Tarif extractTarifFromResultSet(ResultSet resultSet) throws SQLException {
        Tarif tarif = new Tarif();
        tarif.setId(resultSet.getLong("id"));
        tarif.setTypeService(resultSet.getString("type_service"));
        tarif.setMontant(resultSet.getDouble("montant"));
        tarif.setDescription(resultSet.getString("description"));
        tarif.setActif(resultSet.getBoolean("actif"));
        return tarif;
    }
}