package org.example.Controllers;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import javafx.stage.Stage;
import org.example.Entities.User;
import org.example.Service.AuditLogService;
import org.example.Utils.AccessControl;
import org.example.Utils.ConfirmationDialog;
import org.example.Utils.DatabaseBackupManager;
import org.example.Utils.NotificationManager;
import org.example.Utils.SessionManager;
import java.net.URL;

import java.util.List;
import java.util.ResourceBundle;

public class BackupController implements Initializable {

    @FXML private TableView<DatabaseBackupManager.BackupInfo> backupTable;
    @FXML private TableColumn<DatabaseBackupManager.BackupInfo, String> fileNameColumn;
    @FXML private TableColumn<DatabaseBackupManager.BackupInfo, String> fileSizeColumn;
    @FXML private TableColumn<DatabaseBackupManager.BackupInfo, String> creationDateColumn;

    @FXML private Button createBackupBtn;
    @FXML private Button restoreBackupBtn;
    @FXML private Button deleteBackupBtn;
    @FXML private Button closeBtn;

    @FXML private ProgressIndicator progressIndicator;
    @FXML private Label statusLabel;

    private DatabaseBackupManager backupManager;
    private AuditLogService auditLogService;
    private User currentUser;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        backupManager = DatabaseBackupManager.getInstance();
        auditLogService = new AuditLogService();
        currentUser = SessionManager.getCurrentUser();

        // Check if user is admin
        if (currentUser == null || !currentUser.isAdmin()) {
            AccessControl.showAccessDeniedAlert("Seuls les administrateurs peuvent gérer les sauvegardes.");
            Platform.runLater(() -> {
                Stage stage = (Stage) backupTable.getScene().getWindow();
                stage.close();
            });
            return;
        }

        // Set up table columns
        fileNameColumn.setCellValueFactory(new PropertyValueFactory<>("fileName"));
        fileSizeColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getFormattedSize()));
        creationDateColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getFormattedDate()));

        // Set up selection listener
        backupTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean hasSelection = newSelection != null;
            restoreBackupBtn.setDisable(!hasSelection);
            deleteBackupBtn.setDisable(!hasSelection);
        });

        // Initial state
        restoreBackupBtn.setDisable(true);
        deleteBackupBtn.setDisable(true);
        progressIndicator.setVisible(false);

        // Load backups
        loadBackups();
    }

    private void loadBackups() {
        Task<List<DatabaseBackupManager.BackupInfo>> task = new Task<List<DatabaseBackupManager.BackupInfo>>() {
            @Override
            protected List<DatabaseBackupManager.BackupInfo> call() {
                return backupManager.listBackups();
            }
        };

        task.setOnRunning(e -> {
            progressIndicator.setVisible(true);
            statusLabel.setText("Chargement des sauvegardes...");
        });

        task.setOnSucceeded(e -> {
            List<DatabaseBackupManager.BackupInfo> backups = task.getValue();
            ObservableList<DatabaseBackupManager.BackupInfo> backupData =
                    FXCollections.observableArrayList(backups);
            backupTable.setItems(backupData);

            progressIndicator.setVisible(false);
            statusLabel.setText(backups.size() + " sauvegarde(s) trouvée(s)");
        });

        task.setOnFailed(e -> {
            progressIndicator.setVisible(false);
            statusLabel.setText("Erreur lors du chargement des sauvegardes");

            Stage stage = (Stage) backupTable.getScene().getWindow();
            NotificationManager.showError(stage, "Erreur",
                    "Erreur lors du chargement des sauvegardes: " + task.getException().getMessage());
        });

        new Thread(task).start();
    }

    @FXML
    private void handleCreateBackup(ActionEvent event) {
        Stage stage = (Stage) createBackupBtn.getScene().getWindow();

        Task<String> task = new Task<String>() {
            @Override
            protected String call() throws Exception {
                return backupManager.createBackup(currentUser.getUsername());
            }
        };

        task.setOnRunning(e -> {
            progressIndicator.setVisible(true);
            statusLabel.setText("Création de la sauvegarde en cours...");
            createBackupBtn.setDisable(true);
        });

        task.setOnSucceeded(e -> {
            progressIndicator.setVisible(false);
            String backupPath = task.getValue();
            boolean isJavaBackup = backupPath != null && !backupPath.isEmpty() &&
                    !backupManager.wasMysqldumpUsed();

            if (isJavaBackup) {
                statusLabel.setText("Sauvegarde créée avec succès (méthode Java)");
                NotificationManager.showSuccess(stage, "Sauvegarde créée",
                        "La sauvegarde a été créée avec succès en utilisant la méthode Java.");
            } else {
                statusLabel.setText("Sauvegarde créée avec succès");
                NotificationManager.showSuccess(stage, "Sauvegarde créée",
                        "La sauvegarde a été créée avec succès.");
            }

            createBackupBtn.setDisable(false);

            // Reload backups
            loadBackups();
        });

        task.setOnFailed(e -> {
            progressIndicator.setVisible(false);
            statusLabel.setText("Erreur lors de la création de la sauvegarde");
            createBackupBtn.setDisable(false);

            NotificationManager.showError(stage, "Erreur",
                    "Erreur lors de la création de la sauvegarde: " + task.getException().getMessage());
        });

        new Thread(task).start();
    }

    @FXML
    private void handleRestoreBackup(ActionEvent event) {
        DatabaseBackupManager.BackupInfo selectedBackup = backupTable.getSelectionModel().getSelectedItem();
        if (selectedBackup == null) {
            Stage stage = (Stage) restoreBackupBtn.getScene().getWindow();
            NotificationManager.showWarning(stage, "Sélection requise",
                    "Veuillez sélectionner une sauvegarde à restaurer");
            return;
        }

        Stage stage = (Stage) restoreBackupBtn.getScene().getWindow();
        boolean confirmed = ConfirmationDialog.show(
                stage,
                "Confirmation de restauration",
                "Restaurer la sauvegarde",
                "Êtes-vous sûr de vouloir restaurer cette sauvegarde ? Cette action remplacera toutes les données actuelles.",
                ConfirmationDialog.DialogType.CONFIRMER
        );

        if (confirmed) {
            Task<Boolean> task = new Task<Boolean>() {
                @Override
                protected Boolean call() throws Exception {
                    return backupManager.restoreBackup(selectedBackup.getFilePath(), currentUser.getUsername());
                }
            };

            task.setOnRunning(e -> {
                progressIndicator.setVisible(true);
                statusLabel.setText("Restauration de la sauvegarde en cours...");
                restoreBackupBtn.setDisable(true);
                createBackupBtn.setDisable(true);
                deleteBackupBtn.setDisable(true);
            });

            task.setOnSucceeded(e -> {
                progressIndicator.setVisible(false);
                boolean success = task.getValue();

                if (success) {
                    statusLabel.setText("Sauvegarde restaurée avec succès");

                    Platform.runLater(() -> {
                        NotificationManager.showSuccess(stage, "Sauvegarde restaurée",
                                "La sauvegarde a été restaurée avec succès. Veuillez redémarrer l'application pour appliquer les changements.");

                        boolean restart = ConfirmationDialog.show(
                                stage,
                                "Redémarrage nécessaire",
                                "Redémarrer l'application",
                                "Pour appliquer les changements, l'application doit être redémarrée. Voulez-vous redémarrer maintenant ?",
                                ConfirmationDialog.DialogType.CONFIRMER
                        );

                        if (restart) {
                            // Close the current window first
                            stage.close();

                            // Then exit the application after a short delay
                            new Thread(() -> {
                                try {
                                    Thread.sleep(500);
                                    Platform.exit();
                                } catch (InterruptedException ex) {
                                    // Ignore
                                }
                            }).start();
                        }
                    });
                } else {
                    statusLabel.setText("Erreur lors de la restauration de la sauvegarde");
                    Platform.runLater(() -> {
                        NotificationManager.showError(stage, "Erreur",
                                "Erreur lors de la restauration de la sauvegarde.");
                    });
                }

                restoreBackupBtn.setDisable(false);
                createBackupBtn.setDisable(false);
                deleteBackupBtn.setDisable(false);
            });

            task.setOnFailed(e -> {
                progressIndicator.setVisible(false);
                statusLabel.setText("Erreur lors de la restauration de la sauvegarde");

                NotificationManager.showError(stage, "Erreur",
                        "Erreur lors de la restauration de la sauvegarde: " + task.getException().getMessage());

                restoreBackupBtn.setDisable(false);
                createBackupBtn.setDisable(false);
                deleteBackupBtn.setDisable(false);
            });

            new Thread(task).start();
        }
    }

    @FXML
    private void handleDeleteBackup(ActionEvent event) {
        DatabaseBackupManager.BackupInfo selectedBackup = backupTable.getSelectionModel().getSelectedItem();
        if (selectedBackup == null) {
            Stage stage = (Stage) deleteBackupBtn.getScene().getWindow();
            NotificationManager.showWarning(stage, "Sélection requise",
                    "Veuillez sélectionner une sauvegarde à supprimer");
            return;
        }

        Stage stage = (Stage) deleteBackupBtn.getScene().getWindow();
        boolean confirmed = ConfirmationDialog.show(
                stage,
                "Confirmation de suppression",
                "Supprimer la sauvegarde",
                "Êtes-vous sûr de vouloir supprimer cette sauvegarde ? Cette action est irréversible.",
                ConfirmationDialog.DialogType.DELETE
        );

        if (confirmed) {
            Task<Boolean> task = new Task<Boolean>() {
                @Override
                protected Boolean call() throws Exception {
                    return backupManager.deleteBackup(selectedBackup.getFilePath(), currentUser.getUsername());
                }
            };

            task.setOnRunning(e -> {
                progressIndicator.setVisible(true);
                statusLabel.setText("Suppression de la sauvegarde en cours...");
                deleteBackupBtn.setDisable(true);
            });

            task.setOnSucceeded(e -> {
                progressIndicator.setVisible(false);
                boolean success = task.getValue();

                if (success) {
                    statusLabel.setText("Sauvegarde supprimée avec succès");
                    NotificationManager.showSuccess(stage, "Sauvegarde supprimée",
                            "La sauvegarde a été supprimée avec succès.");

                    // Reload backups
                    loadBackups();
                } else {
                    statusLabel.setText("Erreur lors de la suppression de la sauvegarde");
                    NotificationManager.showError(stage, "Erreur",
                            "Erreur lors de la suppression de la sauvegarde.");
                }

                deleteBackupBtn.setDisable(false);
            });

            task.setOnFailed(e -> {
                progressIndicator.setVisible(false);
                statusLabel.setText("Erreur lors de la suppression de la sauvegarde");

                NotificationManager.showError(stage, "Erreur",
                        "Erreur lors de la suppression de la sauvegarde: " + task.getException().getMessage());

                deleteBackupBtn.setDisable(false);
            });

            new Thread(task).start();
        }
    }

    @FXML
    private void handleRefresh(ActionEvent event) {
        loadBackups();
    }

    @FXML
    private void handleClose(ActionEvent event) {
        Stage stage = (Stage) closeBtn.getScene().getWindow();
        stage.close();
    }
}

