package org.example.Controllers;

import javafx.animation.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.Entities.AuditLog;
import org.example.Entities.User;
import org.example.Service.AuditLogService;
import org.example.Service.UserService;
import org.example.Utils.AccessControl;
import org.example.Utils.ConfirmationDialog;
import org.example.Utils.NotificationManager;
import org.example.Utils.SessionManager;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import javafx.geometry.Insets;

public class LogsController implements Initializable {

    @FXML private TableView<AuditLog> logsTable;
    @FXML private TableColumn<AuditLog, Long> idColumn;
    @FXML private TableColumn<AuditLog, String> actionColumn;
    @FXML private TableColumn<AuditLog, String> entityTypeColumn;
    @FXML private TableColumn<AuditLog, Long> entityIdColumn;
    @FXML private TableColumn<AuditLog, String> detailsColumn;
    @FXML private TableColumn<AuditLog, Integer> userIdColumn;
    @FXML private TableColumn<AuditLog, String> usernameColumn;
    @FXML private TableColumn<AuditLog, Date> timestampColumn;

    @FXML private ComboBox<String> filterTypeComboBox;
    @FXML private ComboBox<String> filterActionComboBox;
    @FXML private ComboBox<String> filterUserComboBox;
    @FXML private DatePicker fromDatePicker;
    @FXML private DatePicker toDatePicker;
    @FXML private Button applyFilterBtn;
    @FXML private Button resetFilterBtn;
    @FXML private Button exportBtn;
    @FXML private Button backBtn;
    @FXML private Button settingsBtn;
    @FXML private Button helpBtn;
    @FXML private Button searchBtn;
    @FXML private Button refreshStatsBtn;
    @FXML private Button deleteLogBtn;
    @FXML private TextField searchField;

    // Statistics section
    @FXML private Label totalLogsLabel;
    @FXML private Label todayLogsLabel;
    @FXML private Label activeUsersLabel;
    @FXML private VBox recentActivityContainer;

    private AuditLogService auditLogService;
    private UserService userService;
    private Integer filterUserId = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize services
        auditLogService = new AuditLogService();
        try {
            userService = new UserService();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // Apply role-based access control
        applyRoleBasedAccess();

        // Setup table columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        actionColumn.setCellValueFactory(new PropertyValueFactory<>("action"));
        entityTypeColumn.setCellValueFactory(new PropertyValueFactory<>("entityType"));
        entityIdColumn.setCellValueFactory(new PropertyValueFactory<>("entityId"));
        detailsColumn.setCellValueFactory(new PropertyValueFactory<>("details"));
        userIdColumn.setCellValueFactory(new PropertyValueFactory<>("userId"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        timestampColumn.setCellValueFactory(new PropertyValueFactory<>("timestamp"));

        // Format timestamp column
        timestampColumn.setCellFactory(column -> new TableCell<AuditLog, Date>() {
            private final SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

            @Override
            protected void updateItem(Date item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(format.format(item));
                }
            }
        });

        // Setup filter combos
        setupFilterCombos();

        // Load logs
        loadLogs();

        // Load statistics
        loadStatistics();

        // Setup context menu for right-click on table rows
        setupContextMenu();
    }

    private void setupContextMenu() {
        // Create context menu
        ContextMenu contextMenu = new ContextMenu();

        // Create menu items
        MenuItem viewItem = new MenuItem("Voir les détails");
        MenuItem deleteItem = new MenuItem("Supprimer");
        deleteItem.getStyleClass().add("menu-item-delete");

        // Add actions to menu items
        viewItem.setOnAction(event -> {
            AuditLog selectedLog = logsTable.getSelectionModel().getSelectedItem();
            if (selectedLog != null) {
                showLogDetails(selectedLog);
            }
        });

        deleteItem.setOnAction(event -> {
            AuditLog selectedLog = logsTable.getSelectionModel().getSelectedItem();
            if (selectedLog != null) {
                handleDeleteLog(selectedLog);
            }
        });

        // Add menu items to context menu
        contextMenu.getItems().addAll(viewItem, deleteItem);

        // Set context menu on table
        logsTable.setContextMenu(contextMenu);

        // Show context menu on right-click
        logsTable.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.SECONDARY && !logsTable.getSelectionModel().isEmpty()) {
                contextMenu.show(logsTable, event.getScreenX(), event.getScreenY());
            }
        });
    }

    private void showLogDetails(AuditLog log) {
        try {
            // Create a dialog
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Détails du log");
            dialog.setHeaderText("Détails du log #" + log.getId());

            // Set dialog content
            VBox content = new VBox(10);
            content.setPadding(new Insets(20));

            // Add log details
            content.getChildren().addAll(
                    createDetailRow("ID:", String.valueOf(log.getId())),
                    createDetailRow("Action:", log.getAction()),
                    createDetailRow("Type d'entité:", log.getEntityType()),
                    createDetailRow("ID d'entité:", String.valueOf(log.getEntityId())),
                    createDetailRow("Détails:", log.getDetails()),
                    createDetailRow("ID Utilisateur:", String.valueOf(log.getUserId())),
                    createDetailRow("Nom d'utilisateur:", log.getUsername()),
                    createDetailRow("Horodatage:", new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(log.getTimestamp()))
            );

            // Set dialog content
            dialog.getDialogPane().setContent(content);

            // Add buttons
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

            // Show dialog
            dialog.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            Stage stage = (Stage) logsTable.getScene().getWindow();
            NotificationManager.showError(stage, "Erreur", "Erreur lors de l'affichage des détails: " + e.getMessage());
        }
    }

    private HBox createDetailRow(String label, String value) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);

        Label labelNode = new Label(label);
        labelNode.setStyle("-fx-font-weight: bold;");
        labelNode.setMinWidth(120);

        Label valueNode = new Label(value);
        valueNode.setWrapText(true);

        row.getChildren().addAll(labelNode, valueNode);
        return row;
    }

    @FXML
    private void handleDeleteLog() {
        AuditLog selectedLog = logsTable.getSelectionModel().getSelectedItem();
        if (selectedLog != null) {
            handleDeleteLog(selectedLog);
        } else {
            Stage stage = (Stage) logsTable.getScene().getWindow();
            NotificationManager.showWarning(stage, "Sélection requise", "Veuillez sélectionner un log à supprimer");
        }
    }

    private void handleDeleteLog(AuditLog log) {
        Stage stage = (Stage) logsTable.getScene().getWindow();

        // Check if user has admin rights
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser == null || !currentUser.canAccessLogs()) {
            NotificationManager.showWarning(stage, "Accès refusé",
                    "Vous n'avez pas les droits nécessaires pour supprimer des logs");
            return;
        }

        // Show confirmation dialog
        boolean confirmed = ConfirmationDialog.show(
                stage,
                "Confirmation de suppression",
                "Supprimer le log",
                "Êtes-vous sûr de vouloir supprimer ce log ? Cette action est irréversible.",
                ConfirmationDialog.DialogType.DELETE
        );

        if (confirmed) {
            try {
                // Delete log
                boolean success = auditLogService.deleteLogById(log.getId());

                if (success) {
                    // Log the deletion action
                    auditLogService.logAction(
                            "DELETE",
                            "AUDIT_LOG",
                            log.getId(),
                            "Suppression du log #" + log.getId()
                    );

                    // Remove from table
                    logsTable.getItems().remove(log);

                    // Refresh statistics
                    loadStatistics();

                    // Show success notification
                    NotificationManager.showSuccess(stage, "Log supprimé",
                            "Le log a été supprimé avec succès");
                } else {
                    NotificationManager.showError(stage, "Erreur",
                            "Erreur lors de la suppression du log");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                NotificationManager.showError(stage, "Erreur",
                        "Erreur lors de la suppression du log: " + e.getMessage());
            }
        }
    }

    private void applyRoleBasedAccess() {
        User currentUser = SessionManager.getCurrentUser();

        if (currentUser == null || !currentUser.canAccessLogs()) {
            // Disable the entire UI if not an admin
            logsTable.setDisable(true);
            filterTypeComboBox.setDisable(true);
            filterActionComboBox.setDisable(true);
            filterUserComboBox.setDisable(true);
            fromDatePicker.setDisable(true);
            toDatePicker.setDisable(true);
            applyFilterBtn.setDisable(true);
            resetFilterBtn.setDisable(true);
            exportBtn.setDisable(true);
            searchField.setDisable(true);
            searchBtn.setDisable(true);
            refreshStatsBtn.setDisable(true);
            if (deleteLogBtn != null) {
                deleteLogBtn.setDisable(true);
            }

            // Show warning
            AccessControl.showAccessDeniedAlert("Seuls les administrateurs peuvent accéder aux logs.");
        }
    }

    private void setupFilterCombos() {
        // Setup entity type filter
        filterTypeComboBox.getItems().addAll("Tous les types", "USER", "CANDIDAT", "MONITEUR", "EXAMEN", "VEHICULE");
        filterTypeComboBox.setValue("Tous les types");

        // Setup action filter
        filterActionComboBox.getItems().addAll("Toutes les actions", "CREATE", "UPDATE", "DELETE", "LOGIN", "LOGOUT");
        filterActionComboBox.setValue("Toutes les actions");

        // Setup user filter
        filterUserComboBox.getItems().add("Tous les utilisateurs");
        filterUserComboBox.setValue("Tous les utilisateurs");

        try {
            List<User> users = userService.findAll();
            for (User user : users) {
                filterUserComboBox.getItems().add(user.getId() + " - " + user.getUsername());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadLogs() {
        try {
            List<AuditLog> logs;

            if (filterUserId != null) {
                logs = auditLogService.getLogsByUser(filterUserId);
            } else {
                logs = auditLogService.getAllLogs();
            }

            ObservableList<AuditLog> logData = FXCollections.observableArrayList(logs);
            logsTable.setItems(logData);
        } catch (SQLException e) {
            e.printStackTrace();
            Stage stage = (Stage) logsTable.getScene().getWindow();
            NotificationManager.showError(stage, "Erreur", "Erreur lors du chargement des logs: " + e.getMessage());
        }
    }

    private void loadStatistics() {
        try {
            // Total logs
            int totalLogs = auditLogService.getTotalLogsCount();
            totalLogsLabel.setText(String.valueOf(totalLogs));

            // Today's logs
            int todayLogs = auditLogService.getLogsCountForDate(LocalDate.now());
            todayLogsLabel.setText(String.valueOf(todayLogs));

            // Active users
            int activeUsers = auditLogService.getActiveUsersCount();
            activeUsersLabel.setText(String.valueOf(activeUsers));

            // Load recent activity only
            loadRecentActivity();

        } catch (SQLException e) {
            e.printStackTrace();
            Stage stage = (Stage) logsTable.getScene().getWindow();
            NotificationManager.showError(stage, "Erreur",
                    "Erreur lors du chargement des statistiques: " + e.getMessage());

            // Set default values to prevent UI from breaking
            totalLogsLabel.setText("0");
            todayLogsLabel.setText("0");
            activeUsersLabel.setText("0");
        }
    }

    // Improved loadRecentActivity method with better styling
    private void loadRecentActivity() {
        try {
            recentActivityContainer.getChildren().clear();

            List<AuditLog> recentLogs = auditLogService.getRecentLogs(5);

            if (recentLogs.isEmpty()) {
                Label emptyLabel = new Label("Aucune activité récente");
                emptyLabel.getStyleClass().add("stat-item-label");
                recentActivityContainer.getChildren().add(emptyLabel);
                return;
            }

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM HH:mm");

            for (AuditLog log : recentLogs) {
                String actionText = log.getAction() + " - " + log.getEntityType();
                String timeText = dateFormat.format(log.getTimestamp());

                HBox item = new HBox(15); // Increased spacing
                item.getStyleClass().add("stat-item");
                item.setPadding(new Insets(8, 5, 8, 5)); // Add padding for better spacing

                Label actionLabel = new Label(actionText);
                actionLabel.getStyleClass().add("stat-item-label");
                actionLabel.setWrapText(true); // Allow text wrapping
                HBox.setHgrow(actionLabel, Priority.ALWAYS);

                Label timeLabel = new Label(timeText);
                timeLabel.getStyleClass().add("stat-item-value");
                timeLabel.setMinWidth(80); // Fixed width for time values
                timeLabel.setAlignment(Pos.CENTER_RIGHT);

                item.getChildren().addAll(actionLabel, timeLabel);
                recentActivityContainer.getChildren().add(item);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Add error handling
            Label errorLabel = new Label("Erreur: " + e.getMessage());
            errorLabel.getStyleClass().add("error-label");
            recentActivityContainer.getChildren().add(errorLabel);
        }
    }

    public void filterByUser(Integer userId) {
        this.filterUserId = userId;
        loadLogs();

        if (userId != null) {
            try {
                User user = userService.findById(userId);
                if (user != null) {
                    filterUserComboBox.setValue(user.getId() + " - " + user.getUsername());
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleSearch() {
        String searchTerm = searchField.getText().trim();
        if (searchTerm.isEmpty()) {
            loadLogs();
            return;
        }

        try {
            List<AuditLog> logs = auditLogService.searchLogs(searchTerm);
            ObservableList<AuditLog> logData = FXCollections.observableArrayList(logs);
            logsTable.setItems(logData);

            Stage stage = (Stage) searchBtn.getScene().getWindow();
            NotificationManager.showInfo(stage, "Recherche",
                    logs.size() + " résultat(s) trouvé(s) pour \"" + searchTerm + "\"");
        } catch (SQLException e) {
            e.printStackTrace();
            Stage stage = (Stage) searchBtn.getScene().getWindow();
            NotificationManager.showError(stage, "Erreur",
                    "Erreur lors de la recherche: " + e.getMessage());
        }
    }

    @FXML
    private void handleApplyFilter() {
        try {
            // Get filter values
            String entityType = filterTypeComboBox.getValue().equals("Tous les types") ? null : filterTypeComboBox.getValue();
            String action = filterActionComboBox.getValue().equals("Toutes les actions") ? null : filterActionComboBox.getValue();

            // Get user ID from combo box
            Integer userId = null;
            String userSelection = filterUserComboBox.getValue();
            if (userSelection != null && !userSelection.equals("Tous les utilisateurs")) {
                String[] parts = userSelection.split(" - ");
                userId = Integer.parseInt(parts[0]);
            }

            // Get dates
            LocalDate fromDate = fromDatePicker.getValue();
            LocalDate toDate = toDatePicker.getValue();

            // Apply filters
            List<AuditLog> logs = auditLogService.getFilteredLogs(entityType, action, userId, fromDate, toDate);
            ObservableList<AuditLog> logData = FXCollections.observableArrayList(logs);
            logsTable.setItems(logData);

            Stage stage = (Stage) applyFilterBtn.getScene().getWindow();
            NotificationManager.showInfo(stage, "Filtrage",
                    logs.size() + " log(s) correspond(ent) aux critères de filtrage.");
        } catch (SQLException e) {
            e.printStackTrace();
            Stage stage = (Stage) applyFilterBtn.getScene().getWindow();
            NotificationManager.showError(stage, "Erreur",
                    "Erreur lors de l'application des filtres: " + e.getMessage());
        }
    }

    @FXML
    private void handleResetFilter() {
        filterTypeComboBox.setValue("Tous les types");
        filterActionComboBox.setValue("Toutes les actions");
        filterUserComboBox.setValue("Tous les utilisateurs");
        fromDatePicker.setValue(null);
        toDatePicker.setValue(null);
        filterUserId = null;
        searchField.clear();
        loadLogs();
    }

    @FXML
    private void handleExport() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exporter les logs");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("CSV", "*.csv")
        );

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String defaultFileName = "logs_" + dateFormat.format(new Date()) + ".csv";
        fileChooser.setInitialFileName(defaultFileName);

        Stage stage = (Stage) exportBtn.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                // Write header
                writer.write("ID,Action,Type d'entité,ID d'entité,Détails,ID Utilisateur,Nom d'utilisateur,Horodatage\n");

                // Write data
                SimpleDateFormat timestampFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                for (AuditLog log : logsTable.getItems()) {
                    writer.write(String.format("%d,%s,%s,%d,\"%s\",%d,%s,%s\n",
                            log.getId(),
                            log.getAction(),
                            log.getEntityType(),
                            log.getEntityId(),
                            log.getDetails().replace("\"", "\"\""), // Escape quotes
                            log.getUserId(),
                            log.getUsername(),
                            log.getTimestamp() != null ? timestampFormat.format(log.getTimestamp()) : ""
                    ));
                }

                NotificationManager.showSuccess(stage, "Export réussi", "Logs exportés avec succès vers " + file.getName());
            } catch (IOException e) {
                e.printStackTrace();
                NotificationManager.showError(stage, "Erreur d'export", "Erreur lors de l'export des logs: " + e.getMessage());
            }
        }
    }

    @FXML
    private void refreshStatistics() {
        loadStatistics();
        Stage stage = (Stage) refreshStatsBtn.getScene().getWindow();
        NotificationManager.showInfo(stage, "Statistiques", "Les statistiques ont été actualisées.");
    }

    @FXML
    public void handleBack(ActionEvent event) {
        try {
            // Get the stage
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene currentScene = ((Node) event.getSource()).getScene();

            // Preload the main view to avoid lag
            URL fxmlLocation = getClass().getResource("/IHM/MainIHM.fxml");
            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent root = loader.load();

            // Create a semi-transparent black overlay for transition
            StackPane fadeOverlay = new StackPane();
            fadeOverlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.85);"); // Darker black background
            fadeOverlay.setOpacity(0);

            // Create a container for current scene and overlay
            StackPane transitionRoot = new StackPane();
            transitionRoot.getChildren().addAll(currentScene.getRoot(), fadeOverlay);

            // Create transition scene
            Scene transitionScene = new Scene(transitionRoot, currentScene.getWidth(), currentScene.getHeight());
            stage.setScene(transitionScene);

            // Apply blur to current scene
            GaussianBlur blur = new GaussianBlur(0);
            currentScene.getRoot().setEffect(blur);

            // Fade in overlay and blur background - smoother with longer duration
            FadeTransition fadeIn = new FadeTransition(Duration.millis(400), fadeOverlay);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.setInterpolator(Interpolator.EASE_IN);

            Timeline blurTimeline = new Timeline(
                    new KeyFrame(Duration.ZERO, new KeyValue(blur.radiusProperty(), 0, Interpolator.EASE_OUT)),
                    new KeyFrame(Duration.millis(400), new KeyValue(blur.radiusProperty(), 8, Interpolator.EASE_OUT))
            );

            // Start fade in and blur animations
            ParallelTransition parallelIn = new ParallelTransition(fadeIn, blurTimeline);

            parallelIn.setOnFinished(e -> {
                // Short pause for visual effect
                PauseTransition pause = new PauseTransition(Duration.millis(100));
                pause.setOnFinished(pe -> {
                    // Prepare the new scene with black background to avoid white flash
                    StackPane rootWrapper = new StackPane(root);
                    rootWrapper.setStyle("-fx-background-color: black;");
                    root.setOpacity(0);
                    Scene newScene = new Scene(rootWrapper, currentScene.getWidth(), currentScene.getHeight());

                    // Set the scene
                    stage.setTitle("Menu Principal");
                    stage.setScene(newScene);

                    // Fade in the new view - smoother with longer duration
                    FadeTransition fadeInNewView = new FadeTransition(Duration.millis(400), root);
                    fadeInNewView.setFromValue(0);
                    fadeInNewView.setToValue(1);
                    fadeInNewView.setInterpolator(Interpolator.EASE_OUT);
                    fadeInNewView.play();
                });
                pause.play();
            });

            parallelIn.play();
        } catch (Exception e) {
            e.printStackTrace();
            // Fallback to direct scene change if animation fails
            try {
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                URL fxmlLocation = getClass().getResource("/IHM/MainIHM.fxml");
                FXMLLoader loader = new FXMLLoader(fxmlLocation);
                Parent root = loader.load();
                stage.setTitle("Menu Principal");
                stage.setScene(new Scene(root));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    @FXML
    private void handleSettings(ActionEvent event) {
        try {
            // Load the settings view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/IHM/SettingsIHM.fxml"));
            Parent settingsRoot = loader.load();

            // Get the controller
            SettingsController settingsController = loader.getController();

            // Get the current stage
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Create a new stage for settings
            Stage settingsStage = new Stage();
            settingsStage.setTitle("Paramètres");
            settingsStage.initModality(Modality.WINDOW_MODAL);
            settingsStage.initOwner(currentStage);

            // Set the main stage reference in the settings controller
            settingsController.setMainStage(currentStage);

            // Create scene and set it on the stage
            Scene settingsScene = new Scene(settingsRoot);
            settingsStage.setScene(settingsScene);
            settingsStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            NotificationManager.showError(stage, "Erreur",
                    "Impossible de charger les paramètres: " + e.getMessage());
        }
    }

    @FXML
    private void handleHelp(ActionEvent event) {
        try {
            // Load the help view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/IHM/HelpIHM.fxml"));
            Parent helpRoot = loader.load();

            // Get the controller
            HelpController helpController = loader.getController();

            // Get the current stage
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Create a new stage for help
            Stage helpStage = new Stage();
            helpStage.setTitle("Aide - Auto-École Pro");
            helpStage.initModality(Modality.WINDOW_MODAL);
            helpStage.initOwner(currentStage);

            // Create scene and set it on the stage
            Scene helpScene = new Scene(helpRoot);
            helpStage.setScene(helpScene);

            // Set minimum dimensions for the help window
            helpStage.setMinWidth(800);
            helpStage.setMinHeight(600);

            helpStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            NotificationManager.showError(stage, "Erreur",
                    "Impossible de charger l'aide: " + e.getMessage());
        }
    }
}

