package org.example.Controllers;

import javafx.animation.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.Entities.User;
import org.example.Service.AuditLogService;
import org.example.Service.UserService;
import org.example.Utils.AccessControl;
import org.example.Utils.ConfirmationDialog;
import org.example.Utils.NotificationManager;
import org.example.Utils.SessionManager;
import org.example.Utils.Verification;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class UserManagementController implements Initializable {

    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, Integer> idColumn;
    @FXML private TableColumn<User, String> usernameColumn;
    @FXML private TableColumn<User, String> fullNameColumn;
    @FXML private TableColumn<User, String> emailColumn;
    @FXML private TableColumn<User, String> phoneColumn;
    @FXML private TableColumn<User, String> roleColumn;
    @FXML private TableColumn<User, Boolean> activeColumn;
    @FXML private TableColumn<User, Timestamp> lastLoginColumn;

    @FXML private TextField usernameField;
    @FXML private TextField fullNameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private ComboBox<String> roleComboBox;

    @FXML private Button addBtn;
    @FXML private Button editBtn;
    @FXML private Button deleteBtn;
    @FXML private Button saveBtn;
    @FXML private Button cancelBtn;
    @FXML private Button backBtn;
    @FXML private Button viewLogsBtn;

    @FXML private TextField searchField;
    @FXML private Button searchBtn;

    // Error labels for validation
    @FXML private Label usernameErrorLabel;
    @FXML private Label emailErrorLabel;
    @FXML private Label phoneErrorLabel;
    @FXML private Label passwordErrorLabel;
    @FXML private Label fullNameErrorLabel;

    // Constants for styling
    private static final String ERROR_STYLE = "-fx-border-color: #ef4444; -fx-border-width: 2px;";
    private static final String FOCUS_STYLE = "-fx-border-color: #007BFF; -fx-border-width: 2px;";
    private static final String NORMAL_STYLE = "-fx-border-color: #DEE2E6; -fx-border-width: 1px;";
    private static final String ERROR_TEXT_STYLE = "-fx-text-fill: #ef4444; -fx-font-weight: bold;";
    private static final String SUCCESS_TEXT_STYLE = "-fx-text-fill: #28a745; -fx-font-weight: bold;";

    private UserService userService;
    private AuditLogService auditLogService;
    private User selectedUser;
    private boolean isEditMode = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize services
        try {
            userService = new UserService();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        auditLogService = new AuditLogService();

        // Apply role-based access control
        applyRoleBasedAccess();

        // Set up table columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        fullNameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
        activeColumn.setCellValueFactory(new PropertyValueFactory<>("active"));
        lastLoginColumn.setCellValueFactory(new PropertyValueFactory<>("lastLogin"));

        // Format password column to show dots instead of actual password
        passwordField.setPromptText("Mot de passe");
        confirmPasswordField.setPromptText("Confirmer le mot de passe");

        // Load users
        loadUsers();

        // Set up role combo box
        roleComboBox.setItems(FXCollections.observableArrayList("Administrateur", "Secrétaire"));
        roleComboBox.setValue("Secrétaire");

        // Set up table selection listener
        userTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedUser = newSelection;
                populateForm(selectedUser);
            }
        });

        // Set initial form state
        disableForm(true);

        // Initialize error labels
        initializeErrorLabels();
    }

    private void initializeErrorLabels() {
        // Initialize error labels if they exist
        if (usernameErrorLabel != null) {
            usernameErrorLabel.setVisible(false);
            usernameErrorLabel.setManaged(false);
        }

        if (emailErrorLabel != null) {
            emailErrorLabel.setVisible(false);
            emailErrorLabel.setManaged(false);
        }

        if (phoneErrorLabel != null) {
            phoneErrorLabel.setVisible(false);
            phoneErrorLabel.setManaged(false);
        }

        if (passwordErrorLabel != null) {
            passwordErrorLabel.setVisible(false);
            passwordErrorLabel.setManaged(false);
        }

        if (fullNameErrorLabel != null) {
            fullNameErrorLabel.setVisible(false);
            fullNameErrorLabel.setManaged(false);
        }

        // Add focus listeners to all input fields
        setupFieldListeners();
    }

    private void setupFieldListeners() {
        // Add focus listeners to all input fields
        setupFieldListener(usernameField);
        setupFieldListener(fullNameField);
        setupFieldListener(emailField);
        setupFieldListener(phoneField);
        setupFieldListener(passwordField);
        setupFieldListener(confirmPasswordField);
    }

    private void setupFieldListener(TextField field) {
        if (field != null) {
            field.focusedProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal) { // Field gained focus
                    field.setStyle(FOCUS_STYLE);
                } else { // Field lost focus
                    field.setStyle(NORMAL_STYLE);
                }
            });
        }
    }

    // Update the showErrorLabel method to also display a notification
    private void showErrorLabel(Label errorLabel, String message, TextField field) {
        if (errorLabel != null) {
            errorLabel.setText(message);
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);
            shakeNode(errorLabel); // Shake the error label
        }

        if (field != null) {
            field.setStyle(ERROR_STYLE);
            shakeNode(field);

            // Show notification at the bottom of the screen
            Stage stage = (Stage) field.getScene().getWindow();
            if (stage != null) {
                NotificationManager.showWarning(stage, "Validation", message);
            }
        }
    }

    // Add this method for the shake animation
    private void shakeNode(Node node) {
        if (node == null) {
            return; // Don't attempt to shake a null node
        }

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(node.translateXProperty(), 0)),
                new KeyFrame(Duration.millis(100), new KeyValue(node.translateXProperty(), -10)),
                new KeyFrame(Duration.millis(200), new KeyValue(node.translateXProperty(), 10)),
                new KeyFrame(Duration.millis(300), new KeyValue(node.translateXProperty(), -10)),
                new KeyFrame(Duration.millis(400), new KeyValue(node.translateXProperty(), 10)),
                new KeyFrame(Duration.millis(500), new KeyValue(node.translateXProperty(), 0))
        );

        // Add a subtle red glow effect during shake
        DropShadow errorGlow = new DropShadow();
        errorGlow.setColor(Color.web("#ef4444", 0.5));
        errorGlow.setRadius(10);

        // Store original effect
        Object originalEffect = node.getEffect();

        // Apply error effect
        node.setEffect(errorGlow);

        // Reset effect after animation
        timeline.setOnFinished(event -> node.setEffect((javafx.scene.effect.Effect) originalEffect));

        timeline.play();
    }

    private void hideErrorLabel(Label errorLabel) {
        if (errorLabel != null) {
            errorLabel.setVisible(false);
            errorLabel.setManaged(false);
        }
    }

    private void hideAllErrorLabels() {
        hideErrorLabel(usernameErrorLabel);
        hideErrorLabel(emailErrorLabel);
        hideErrorLabel(phoneErrorLabel);
        hideErrorLabel(passwordErrorLabel);
        hideErrorLabel(fullNameErrorLabel);
    }

    private void applyRoleBasedAccess() {
        User currentUser = SessionManager.getCurrentUser();

        if (currentUser == null || !currentUser.canManageUsers()) {
            // Disable the entire UI if not an admin
            userTable.setDisable(true);
            addBtn.setDisable(true);
            editBtn.setDisable(true);
            deleteBtn.setDisable(true);
            saveBtn.setDisable(true);
            cancelBtn.setDisable(true);
            viewLogsBtn.setDisable(true);

            // Show warning
            AccessControl.showAccessDeniedAlert("Seuls les administrateurs peuvent gérer les utilisateurs.");
        }
    }

    private void loadUsers() {
        try {
            List<User> users = userService.findAll();
            ObservableList<User> userData = FXCollections.observableArrayList(users);
            userTable.setItems(userData);
        } catch (SQLException e) {
            e.printStackTrace();
            Stage stage = (Stage) userTable.getScene().getWindow();
            NotificationManager.showError(stage, "Erreur", "Erreur lors du chargement des utilisateurs: " + e.getMessage());
        }
    }

    private void populateForm(User user) {
        if (user != null) {
            usernameField.setText(user.getUsername());
            fullNameField.setText(user.getFullName());
            emailField.setText(user.getEmail());
            phoneField.setText(user.getPhoneNumber());
            passwordField.clear(); // Don't show password
            confirmPasswordField.clear();
            roleComboBox.setValue(user.getRole());
        }
    }

    private void clearForm() {
        usernameField.clear();
        fullNameField.clear();
        emailField.clear();
        phoneField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
        roleComboBox.setValue("Secrétaire");
        selectedUser = null;
        hideAllErrorLabels();
    }

    private void disableForm(boolean disable) {
        usernameField.setDisable(disable);
        fullNameField.setDisable(disable);
        emailField.setDisable(disable);
        phoneField.setDisable(disable);
        passwordField.setDisable(disable);
        confirmPasswordField.setDisable(disable);
        roleComboBox.setDisable(disable);
        saveBtn.setDisable(disable);
        cancelBtn.setDisable(disable);
    }

    @FXML
    private void handleAddUser() {
        clearForm();
        disableForm(false);
        isEditMode = false;

        // Show notification
        Stage stage = (Stage) addBtn.getScene().getWindow();
        NotificationManager.showInfo(stage, "Nouvel utilisateur",
                "Veuillez remplir le formulaire pour ajouter un nouvel utilisateur");
    }

    @FXML
    private void handleEditUser() {
        if (selectedUser == null) {
            Stage stage = (Stage) editBtn.getScene().getWindow();
            NotificationManager.showWarning(stage, "Sélection requise", "Veuillez sélectionner un utilisateur à modifier");
            return;
        }

        User currentUser = SessionManager.getCurrentUser();

        // Prevent modification of own account's role
        if (selectedUser.getId() == currentUser.getId()) {
            roleComboBox.setDisable(true);
        } else {
            roleComboBox.setDisable(false);
        }

        disableForm(false);
        isEditMode = true;

        // Show notification
        Stage stage = (Stage) editBtn.getScene().getWindow();
        NotificationManager.showInfo(stage, "Modification d'utilisateur",
                "Vous modifiez l'utilisateur: " + selectedUser.getUsername());
    }

    @FXML
    private void handleDeleteUser() {
        if (selectedUser == null) {
            Stage stage = (Stage) deleteBtn.getScene().getWindow();
            NotificationManager.showWarning(stage, "Sélection requise", "Veuillez sélectionner un utilisateur à supprimer");
            return;
        }

        User currentUser = SessionManager.getCurrentUser();

        // Prevent deletion of own account
        if (selectedUser.getId() == currentUser.getId()) {
            Stage stage = (Stage) deleteBtn.getScene().getWindow();
            NotificationManager.showWarning(stage, "Action non autorisée",
                    "Vous ne pouvez pas supprimer votre propre compte.");
            shakeNode(deleteBtn); // Add visual feedback
            return;
        }

        Stage stage = (Stage) deleteBtn.getScene().getWindow();
        boolean confirmed = ConfirmationDialog.show(
                stage,
                "Confirmation de suppression",
                "Supprimer l'utilisateur",
                "Êtes-vous sûr de vouloir supprimer cet utilisateur ? Cette action est irréversible.",
                ConfirmationDialog.DialogType.DELETE
        );

        if (confirmed) {
            try {
                // Log the action
                auditLogService.logAction(
                        "DELETE",
                        "USER",
                        (long) selectedUser.getId(),
                        "Suppression de l'utilisateur " + selectedUser.getUsername()
                );

                boolean success = userService.deleteUser(selectedUser.getId());
                if (success) {
                    loadUsers();
                    clearForm();
                    NotificationManager.showSuccess(stage, "Utilisateur supprimé",
                            "L'utilisateur a été supprimé avec succès");
                } else {
                    NotificationManager.showError(stage, "Erreur",
                            "Erreur lors de la suppression de l'utilisateur");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                NotificationManager.showError(stage, "Erreur",
                        "Erreur lors de la suppression de l'utilisateur: " + e.getMessage());
            }
        }
    }
    @FXML
    private void handleSettings(ActionEvent event) {
        try {
            // Load the settings FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/IHM/SettingsIHM.fxml"));
            Parent root = loader.load();

            // Create a new stage for the settings window
            Stage settingsStage = new Stage();
            settingsStage.setTitle("Paramètres - Auto-École Pro");
            //settingsStage.getIcons().add(new Image(getClass().getResourceAsStream("/org/example/Images/settings_icon.png")));

            // Set minimum dimensions
            settingsStage.setMinWidth(600);
            settingsStage.setMinHeight(400);

            // Create the scene
            Scene scene = new Scene(root);

            // Add stylesheet
            scene.getStylesheets().add(getClass().getResource("/Styles/Common.css").toExternalForm());

            settingsStage.setScene(scene);

            // Set modality (makes the settings window modal)
            settingsStage.initModality(Modality.APPLICATION_MODAL);

            // Set owner (sets the parent window)
            settingsStage.initOwner(((Node) event.getSource()).getScene().getWindow());

            // Show the settings window
            settingsStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            // Show error alert
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Impossible d'ouvrir les paramètres");
            alert.setContentText("Une erreur s'est produite lors de l'ouverture des paramètres: " + e.getMessage());
            alert.showAndWait();
        }
    }
    @FXML
    public void handleHelp(ActionEvent event) {
        try {
            // Load the help view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/IHM/HelpIHM.fxml"));
            Parent helpRoot = loader.load();



            // Get the current stage
            Stage currentStage = null;
            if (event.getSource() instanceof Node) {
                currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            }

            if (currentStage == null) {
                throw new IllegalStateException("Cannot find current stage");
            }

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
            // Show error notification
            Stage stage = null;
            if (event.getSource() instanceof Node) {
                stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            }

            if (stage != null) {
                NotificationManager.showError(stage, "Erreur",
                        "Impossible de charger l'aide: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleSaveUser() {
        // Hide all error labels first
        hideAllErrorLabels();

        // Validate form
        if (!validateForm()) {
            return;
        }

        try {
            User user;
            if (isEditMode) {
                user = selectedUser;
            } else {
                user = new User();
                user.setCreatedAt(new Timestamp(System.currentTimeMillis()));
            }

            user.setUsername(usernameField.getText());
            user.setFullName(fullNameField.getText());
            user.setEmail(emailField.getText());
            user.setPhoneNumber(phoneField.getText());
            user.setRole(roleComboBox.getValue());

            // Only update password if provided
            if (!passwordField.getText().isEmpty()) {
                user.setPassword(passwordField.getText());
            }

            Stage stage = (Stage) saveBtn.getScene().getWindow();

            // Create confirmation dialog
            boolean confirmed = ConfirmationDialog.show(
                    stage,
                    isEditMode ? "Confirmation de modification" : "Confirmation d'ajout",
                    isEditMode ? "Modifier l'utilisateur" : "Ajouter l'utilisateur",
                    isEditMode ? "Êtes-vous sûr de vouloir modifier cet utilisateur ?" : "Êtes-vous sûr de vouloir ajouter cet utilisateur ?",
                    ConfirmationDialog.DialogType.SAVE
            );

            if (confirmed) {
                boolean success;

                if (isEditMode) {
                    success = userService.updateUser(user);
                    if (success) {
                        // Log the action
                        auditLogService.logAction(
                                "UPDATE",
                                "USER",
                                (long) user.getId(),
                                "UPDATE utilisateur " + user.getUsername()
                        );
                    }
                } else {
                    // Verify username doesn't already exist
                    if (userService.findByUsername(user.getUsername()) != null) {
                        showErrorLabel(usernameErrorLabel, "Ce nom d'utilisateur existe déjà", usernameField);
                        return;
                    }

                    // New user must have a password
                    if (passwordField.getText().isEmpty()) {
                        showErrorLabel(passwordErrorLabel, "Le mot de passe est obligatoire pour un nouvel utilisateur", passwordField);
                        return;
                    }

                    // Add the new user
                    boolean added = userService.addUser(user);
                    if (added) {
                        // Get the newly created user to get its ID
                        user = userService.findByUsername(user.getUsername());
                        success = (user != null && user.getId() > 0);

                        if (success) {
                            // Log the action
                            auditLogService.logAction(
                                    "CREATE",
                                    "USER",
                                    (long) user.getId(),
                                    "CREATE utilisateur " + user.getUsername()
                            );
                        }
                    } else {
                        success = false;
                    }
                }

                if (success) {
                    loadUsers();
                    clearForm();
                    disableForm(true);

                    NotificationManager.showSuccess(stage, isEditMode ? "Utilisateur modifié" : "Utilisateur ajouté",
                            isEditMode ? "L'utilisateur a été modifié avec succès" : "L'utilisateur a été ajouté avec succès");
                } else {
                    NotificationManager.showError(stage, "Erreur",
                            "Erreur lors de la sauvegarde de l'utilisateur");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Stage stage = (Stage) saveBtn.getScene().getWindow();
            NotificationManager.showError(stage, "Erreur",
                    "Erreur lors de la sauvegarde de l'utilisateur: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        clearForm();
        disableForm(true);
        isEditMode = false;
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
    private void handleViewLogs() {
        try {
            // Get the selected user
            User selectedUser = userTable.getSelectionModel().getSelectedItem();

            if (selectedUser == null) {
                Stage stage = (Stage) viewLogsBtn.getScene().getWindow();
                NotificationManager.showWarning(stage, "Aucun utilisateur sélectionné",
                        "Veuillez sélectionner un utilisateur pour voir ses logs.");
                return;
            }

            // Load the logs view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/IHM/LogsIHM.fxml"));
            Parent root = loader.load();

            // Get the controller and set the user filter
            LogsController logsController = loader.getController();
            logsController.filterByUser(selectedUser.getId());

            // Create a new stage for the logs view
            Stage logsStage = new Stage();
            logsStage.setTitle("Logs d'activité - " + selectedUser.getUsername());
            logsStage.setScene(new Scene(root));
            logsStage.initModality(Modality.APPLICATION_MODAL);
            logsStage.initOwner(viewLogsBtn.getScene().getWindow());
            logsStage.show();

        } catch (IOException e) {
            e.printStackTrace();
            Stage stage = (Stage) viewLogsBtn.getScene().getWindow();
            NotificationManager.showError(stage, "Erreur",
                    "Erreur lors de l'ouverture des logs: " + e.getMessage());
        }
    }

    @FXML
    private void handleSearch() {
        String searchTerm = searchField.getText().trim().toLowerCase();
        if (searchTerm.isEmpty()) {
            loadUsers();
            return;
        }

        try {
            List<User> allUsers = userService.findAll();
            ObservableList<User> filteredUsers = FXCollections.observableArrayList();

            for (User user : allUsers) {
                if (user.getUsername().toLowerCase().contains(searchTerm) ||
                        (user.getFullName() != null && user.getFullName().toLowerCase().contains(searchTerm)) ||
                        (user.getEmail() != null && user.getEmail().toLowerCase().contains(searchTerm)) ||
                        (user.getPhoneNumber() != null && user.getPhoneNumber().toLowerCase().contains(searchTerm))) {
                    filteredUsers.add(user);
                }
            }

            userTable.setItems(filteredUsers);

            Stage stage = (Stage) searchBtn.getScene().getWindow();
            NotificationManager.showInfo(stage, "Recherche",
                    filteredUsers.size() + " utilisateur(s) trouvé(s)");
        } catch (SQLException e) {
            e.printStackTrace();
            Stage stage = (Stage) searchBtn.getScene().getWindow();
            NotificationManager.showError(stage, "Erreur",
                    "Erreur lors de la recherche: " + e.getMessage());
        }
    }

    // Update the validateForm method to use the notification approach
    private boolean validateForm() {
        boolean isValid = true;

        // Validate username
        if (!Verification.validateRequired(usernameField, null)) {
            showErrorLabel(usernameErrorLabel, "Le nom d'utilisateur est requis", usernameField);
            isValid = false;
        } else if (isEditMode && !usernameField.getText().equals(selectedUser.getUsername())) {
            try {
                if (userService.findByUsername(usernameField.getText()) != null) {
                    showErrorLabel(usernameErrorLabel, "Ce nom d'utilisateur existe déjà", usernameField);
                    isValid = false;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        // Validate email
        if (!Verification.validateEmail(emailField, null)) {
            showErrorLabel(emailErrorLabel, "L'email est invalide", emailField);
            isValid = false;
        } else if (!emailField.getText().isEmpty()) {
            try {
                User existingUser = userService.findByEmail(emailField.getText());
                if (existingUser != null && (selectedUser == null || existingUser.getId() != selectedUser.getId())) {
                    showErrorLabel(emailErrorLabel, "Cet email est déjà utilisé", emailField);
                    isValid = false;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        // Validate phone number
        if (!Verification.validatePhone(phoneField, null)) {
            showErrorLabel(phoneErrorLabel, "Le numéro de téléphone est invalide", phoneField);
            isValid = false;
        } else if (!phoneField.getText().isEmpty()) {
            try {
                User existingUser = userService.findByPhoneNumber(phoneField.getText());
                if (existingUser != null && (selectedUser == null || existingUser.getId() != selectedUser.getId())) {
                    showErrorLabel(phoneErrorLabel, "Ce numéro de téléphone est déjà utilisé", phoneField);
                    isValid = false;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        // Validate password if provided or if adding new user
        if (!isEditMode || !passwordField.getText().isEmpty()) {
            if (!Verification.validatePassword(passwordField, null, 5)) {
                showErrorLabel(passwordErrorLabel, "Le mot de passe doit contenir au moins 5 caractères", passwordField);
                isValid = false;
            }

            if (!Verification.validatePasswordsMatch(passwordField, confirmPasswordField, null)) {
                showErrorLabel(passwordErrorLabel, "Les mots de passe ne correspondent pas", confirmPasswordField);
                isValid = false;
            }
        }

        // Validate full name if provided
        if (!fullNameField.getText().trim().isEmpty() && !Verification.validateName(fullNameField, null)) {
            showErrorLabel(fullNameErrorLabel, "Le nom complet est invalide", fullNameField);
            isValid = false;
        }

        return isValid;
    }
}

