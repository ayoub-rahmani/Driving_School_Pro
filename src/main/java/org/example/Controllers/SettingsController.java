package org.example.Controllers;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.Entities.User;
import org.example.Service.UserService;
import org.example.Utils.NotificationManager;
import org.example.Utils.SessionManager;
import org.example.Utils.Verification;
import org.example.Utils.ConfirmationDialog;
import org.example.Service.AuditLogService;
import org.example.Utils.AccessControl;
import org.example.Utils.DatabaseBackupManager;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import org.example.Entities.DrivingSchoolInfo;
import org.example.Services.DrivingSchoolService;
import javafx.scene.image.Image;
import java.io.File;
import java.sql.SQLException;

public class SettingsController implements Initializable {

    // User info display
    @FXML private Label currentUsernameLabel;
    @FXML private Label currentUsernameDisplay;
    @FXML private Label userFullNameLabel;
    @FXML private Label userRoleLabel;
    @FXML private ImageView userAvatarView;
    @FXML private Label lastLoginLabel;

    // Theme settings
    @FXML private ToggleGroup themeToggleGroup;
    @FXML private RadioButton lightThemeRadio;
    @FXML private RadioButton darkThemeRadio;

    // Language settings
    @FXML private ComboBox<String> languageComboBox;

    // Username change fields
    @FXML private TextField newUsernameField;
    @FXML private PasswordField passwordForUsernameField;
    @FXML private Label usernameErrorLabel;
    @FXML private Label usernameSuccessLabel;

    // Email change fields
    @FXML private TextField emailField;
    @FXML private PasswordField passwordForEmailField;
    @FXML private Label emailErrorLabel;
    @FXML private Label emailSuccessLabel;
    @FXML private Label aboutTitleLabel; // Référence au label du titre dans la section "À propos"
    @FXML private ImageView aboutLogoImageView; // Référence à l'image du logo dans la section "À propos"
    // Phone number change fields
    @FXML private TextField phoneNumberField;
    @FXML private PasswordField passwordForPhoneField;
    @FXML private Label phoneErrorLabel;
    @FXML private Label phoneSuccessLabel;

    // Password change fields
    @FXML private PasswordField currentPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label passwordErrorLabel;
    @FXML private Label passwordSuccessLabel;

    // Notification settings
    @FXML private CheckBox emailNotificationsCheckbox;
    @FXML private CheckBox smsNotificationsCheckbox;
    @FXML private CheckBox appNotificationsCheckbox;

    // System settings
    @FXML private Slider fontSizeSlider;
    @FXML private ComboBox<String> dateFormatComboBox;
    @FXML private CheckBox autoSaveCheckbox;
    @FXML private Spinner<Integer> autoSaveIntervalSpinner;

    // Backup management
    @FXML private TitledPane backupManagementPane;
    @FXML private Button manageBackupsBtn;

    // Action buttons
    @FXML private Button saveUsernameBtn;
    @FXML private Button saveEmailBtn;
    @FXML private Button savePhoneBtn;
    @FXML private Button savePasswordBtn;
    @FXML private Button saveNotificationsBtn;
    @FXML private Button saveSystemSettingsBtn;
    @FXML private Button logoutBtn;
    @FXML private Button closeBtn;
    @FXML private Button backBtn;

    private UserService userService;
    private AuditLogService auditLogService;
    private User currentUser;
    private Stage mainStage; // Reference to the main application stage

    // Constants for styling
    private static final String ERROR_STYLE = "-fx-border-color: #ef4444; -fx-border-width: 2px;";
    private static final String FOCUS_STYLE = "-fx-border-color: #007BFF; -fx-border-width: 2px;";
    private static final String NORMAL_STYLE = "-fx-border-color: #DEE2E6; -fx-border-width: 1px;";
    private static final String ERROR_TEXT_STYLE = "-fx-text-fill: #ef4444; -fx-font-weight: bold;";
    private static final String SUCCESS_TEXT_STYLE = "-fx-text-fill: #28a745; -fx-font-weight: bold;";

    public SettingsController() throws SQLException {
        userService = new UserService();
    }

    // Method to set the main stage reference
    public void setMainStage(Stage mainStage) {
        this.mainStage = mainStage;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            auditLogService = new AuditLogService();
            // Clear all message labels
            clearAllMessages();

            // Get current user from session
            currentUser = SessionManager.getCurrentUser();

            if (currentUser != null) {
                // Display current username
                if (currentUsernameLabel != null) {
                    currentUsernameLabel.setText(currentUser.getUsername());
                }
                if (currentUsernameDisplay != null) {
                    currentUsernameDisplay.setText(currentUser.getUsername());
                }

                // Set user full name if available
                if (userFullNameLabel != null) {
                    if (currentUser.getFullName() != null && !currentUser.getFullName().isEmpty()) {
                        userFullNameLabel.setText(currentUser.getFullName());
                    } else {
                        userFullNameLabel.setText(currentUser.getUsername());
                    }
                }

                // Set user role if available
                if (userRoleLabel != null) {
                    if (currentUser.getRole() != null && !currentUser.getRole().isEmpty()) {
                        userRoleLabel.setText(currentUser.getRole());
                    } else {
                        userRoleLabel.setText("Utilisateur");
                    }
                }

                // Set email if available
                if (emailField != null && currentUser.getEmail() != null) {
                    emailField.setPromptText(currentUser.getEmail());
                }

                // Set phone number if available
                if (phoneNumberField != null && currentUser.getPhoneNumber() != null) {
                    phoneNumberField.setPromptText(currentUser.getPhoneNumber());
                }

                // Set last login time if available
                if (lastLoginLabel != null) {
                    long lastLoginTimestamp = SessionManager.getLastLoginTime();
                    if (lastLoginTimestamp > 0) {
                        // Format the timestamp into a readable date/time string
                        java.util.Date lastLoginDate = new java.util.Date(lastLoginTimestamp);
                        java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("dd/MM/yyyy à HH:mm");
                        String formattedDate = dateFormat.format(lastLoginDate);
                        lastLoginLabel.setText("Dernière connexion: " + formattedDate);
                    } else {
                        // No previous login recorded
                        lastLoginLabel.setText("Première connexion");
                    }
                }

                // Show/hide backup management pane based on admin role
                if (backupManagementPane != null) {
                    boolean isAdmin = currentUser.isAdmin();
                    backupManagementPane.setVisible(isAdmin);
                    backupManagementPane.setManaged(isAdmin);

                    if (manageBackupsBtn != null) {
                        manageBackupsBtn.setDisable(!isAdmin);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error initializing settings controller: " + e.getMessage());
            e.printStackTrace();
        }

        // Initialize theme settings
        if (lightThemeRadio != null && darkThemeRadio != null) {
            lightThemeRadio.setSelected(true);
        }

        // Initialize language settings
        if (languageComboBox != null) {
            languageComboBox.getItems().addAll("Français", "English", "العربية");
            languageComboBox.setValue("Français");
        }

        // Initialize notification settings
        if (emailNotificationsCheckbox != null) {
            emailNotificationsCheckbox.setSelected(true);
        }
        if (appNotificationsCheckbox != null) {
            appNotificationsCheckbox.setSelected(true);
        }

        // Initialize system settings
        if (dateFormatComboBox != null) {
            dateFormatComboBox.getItems().addAll("DD/MM/YYYY", "MM/DD/YYYY", "YYYY-MM-DD");
            dateFormatComboBox.setValue("DD/MM/YYYY");
        }

        if (autoSaveCheckbox != null) {
            autoSaveCheckbox.setSelected(true);
        }

        if (autoSaveIntervalSpinner != null) {
            SpinnerValueFactory<Integer> valueFactory =
                    new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 60, 5);
            autoSaveIntervalSpinner.setValueFactory(valueFactory);
        }

        // Apply initial styles to error and success labels
        setupLabelStyles();

        // Add listeners for visual feedback
        setupFieldListeners();
        loadDrivingSchoolInfo();
    }
    private void loadDrivingSchoolInfo() {
        try {
            // Créer une instance du service
            DrivingSchoolService drivingSchoolService = new DrivingSchoolService();

            // Récupérer les informations de l'auto-école
            DrivingSchoolInfo schoolInfo = drivingSchoolService.getDrivingSchool();

            // Mettre à jour le nom de l'auto-école dans la section "À propos"
            if (aboutTitleLabel != null && schoolInfo.getName() != null && !schoolInfo.getName().isEmpty()) {
                aboutTitleLabel.setText(schoolInfo.getName());
            }

            // Mettre à jour le logo de l'auto-école dans la section "À propos"
            if (aboutLogoImageView != null && schoolInfo.getLogoPath() != null && !schoolInfo.getLogoPath().isEmpty()) {
                try {
                    File logoFile = new File(schoolInfo.getLogoPath());
                    if (logoFile.exists()) {
                        Image logoImage = new Image(logoFile.toURI().toString());
                        aboutLogoImageView.setImage(logoImage);
                    }
                } catch (Exception e) {
                    System.err.println("Erreur lors du chargement du logo: " + e.getMessage());
                    e.printStackTrace();
                    // Garder l'image par défaut en cas d'erreur
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors du chargement des informations de l'auto-école: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupLabelStyles() {
        // Set styles for error labels
        if (usernameErrorLabel != null) {
            usernameErrorLabel.setStyle(ERROR_TEXT_STYLE);
            usernameErrorLabel.setVisible(false);
            usernameErrorLabel.setManaged(false);
        }

        if (emailErrorLabel != null) {
            emailErrorLabel.setStyle(ERROR_TEXT_STYLE);
            emailErrorLabel.setVisible(false);
            emailErrorLabel.setManaged(false);
        }

        if (phoneErrorLabel != null) {
            phoneErrorLabel.setStyle(ERROR_TEXT_STYLE);
            phoneErrorLabel.setVisible(false);
            phoneErrorLabel.setManaged(false);
        }

        if (passwordErrorLabel != null) {
            passwordErrorLabel.setStyle(ERROR_TEXT_STYLE);
            passwordErrorLabel.setVisible(false);
            passwordErrorLabel.setManaged(false);
        }

        // Set styles for success labels
        if (usernameSuccessLabel != null) {
            usernameSuccessLabel.setStyle(SUCCESS_TEXT_STYLE);
            usernameSuccessLabel.setVisible(false);
            usernameSuccessLabel.setManaged(false);
        }

        if (emailSuccessLabel != null) {
            emailSuccessLabel.setStyle(SUCCESS_TEXT_STYLE);
            emailSuccessLabel.setVisible(false);
            emailSuccessLabel.setManaged(false);
        }

        if (phoneSuccessLabel != null) {
            phoneSuccessLabel.setStyle(SUCCESS_TEXT_STYLE);
            phoneSuccessLabel.setVisible(false);
            phoneSuccessLabel.setManaged(false);
        }

        if (passwordSuccessLabel != null) {
            passwordSuccessLabel.setStyle(SUCCESS_TEXT_STYLE);
            passwordSuccessLabel.setVisible(false);
            passwordSuccessLabel.setManaged(false);
        }
    }

    private void clearAllMessages() {
        // Clear all error and success labels
        clearUsernameMessages();
        clearEmailMessages();
        clearPhoneMessages();
        clearPasswordMessages();
    }

    private void clearUsernameMessages() {
        if (usernameErrorLabel != null) {
            usernameErrorLabel.setText("");
            usernameErrorLabel.setVisible(false);
            usernameErrorLabel.setManaged(false);
        }
        if (usernameSuccessLabel != null) {
            usernameSuccessLabel.setText("");
            usernameSuccessLabel.setVisible(false);
            usernameSuccessLabel.setManaged(false);
        }

        // Reset field styles
        if (newUsernameField != null) {
            newUsernameField.setStyle(NORMAL_STYLE);
        }
        if (passwordForUsernameField != null) {
            passwordForUsernameField.setStyle(NORMAL_STYLE);
        }
    }

    private void clearEmailMessages() {
        if (emailErrorLabel != null) {
            emailErrorLabel.setText("");
            emailErrorLabel.setVisible(false);
            emailErrorLabel.setManaged(false);
        }
        if (emailSuccessLabel != null) {
            emailSuccessLabel.setText("");
            emailSuccessLabel.setVisible(false);
            emailSuccessLabel.setManaged(false);
        }

        // Reset field styles
        if (emailField != null) {
            emailField.setStyle(NORMAL_STYLE);
        }
        if (passwordForEmailField != null) {
            passwordForEmailField.setStyle(NORMAL_STYLE);
        }
    }

    private void clearPhoneMessages() {
        if (phoneErrorLabel != null) {
            phoneErrorLabel.setText("");
            phoneErrorLabel.setVisible(false);
            phoneErrorLabel.setManaged(false);
        }
        if (phoneSuccessLabel != null) {
            phoneSuccessLabel.setText("");
            phoneSuccessLabel.setVisible(false);
            phoneSuccessLabel.setManaged(false);
        }

        // Reset field styles
        if (phoneNumberField != null) {
            phoneNumberField.setStyle(NORMAL_STYLE);
        }
        if (passwordForPhoneField != null) {
            passwordForPhoneField.setStyle(NORMAL_STYLE);
        }
    }

    private void clearPasswordMessages() {
        if (passwordErrorLabel != null) {
            passwordErrorLabel.setText("");
            passwordErrorLabel.setVisible(false);
            passwordErrorLabel.setManaged(false);
        }
        if (passwordSuccessLabel != null) {
            passwordSuccessLabel.setText("");
            passwordSuccessLabel.setVisible(false);
            passwordSuccessLabel.setManaged(false);
        }

        // Reset field styles
        if (currentPasswordField != null) {
            currentPasswordField.setStyle(NORMAL_STYLE);
        }
        if (newPasswordField != null) {
            newPasswordField.setStyle(NORMAL_STYLE);
        }
        if (confirmPasswordField != null) {
            confirmPasswordField.setStyle(NORMAL_STYLE);
        }
    }

    private void setupFieldListeners() {
        // Username field listeners
        setupFieldListener(newUsernameField);
        setupFieldListener(passwordForUsernameField);

        // Email field listeners
        setupFieldListener(emailField);
        setupFieldListener(passwordForEmailField);

        // Phone field listeners
        setupFieldListener(phoneNumberField);
        setupFieldListener(passwordForPhoneField);

        // Password field listeners
        setupFieldListener(currentPasswordField);
        setupFieldListener(newPasswordField);
        setupFieldListener(confirmPasswordField);

        // Add listener for auto-save checkbox to enable/disable interval spinner
        if (autoSaveCheckbox != null && autoSaveIntervalSpinner != null) {
            autoSaveCheckbox.selectedProperty().addListener((obs, oldVal, newVal) -> {
                autoSaveIntervalSpinner.setDisable(!newVal);
            });
        }
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

    private void showError(Label errorLabel, String message, TextField field) {
        if (errorLabel != null) {
            errorLabel.setText(message);
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);
            errorLabel.setStyle(ERROR_TEXT_STYLE);
        }

        if (field != null) {
            field.setStyle(ERROR_STYLE);
            shakeNode(field);
        }
    }

    private void showSuccess(Label successLabel, String message) {
        if (successLabel != null) {
            successLabel.setText(message);
            successLabel.setVisible(true);
            successLabel.setManaged(true);
        }
    }

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

    /**
     * Validates that the new password is different from the current one
     *
     * @param currentPassword The current password
     * @param newPassword The new password
     * @param errorLabel The Label to display error messages
     * @param field The field to highlight on error
     * @return true if valid, false otherwise
     */
    private boolean validateNewPasswordDifferent(String currentPassword, String newPassword, Label errorLabel, TextField field) {
        if (currentPassword.equals(newPassword)) {
            showError(errorLabel, "Le nouveau mot de passe doit être différent de l'ancien", field);
            return false;
        }
        return true;
    }

    @FXML
    private void handleSaveUsername(ActionEvent event) {
        // Clear previous messages
        clearUsernameMessages();

        // Validate new username
        if (!Verification.validateRequired(newUsernameField, usernameErrorLabel)) {
            showError(usernameErrorLabel, "Le nom d'utilisateur est requis", newUsernameField);
            shakeNode(newUsernameField);
            return;
        }

        // Validate password for confirmation
        if (!Verification.validatePassword(passwordForUsernameField, usernameErrorLabel, 1)) {
            showError(usernameErrorLabel, "Mot de passe invalide", passwordForUsernameField);
            shakeNode(passwordForUsernameField);
            return;
        }

        String newUsername = newUsernameField.getText().trim();
        String password = passwordForUsernameField.getText().trim();

        // Check if the new username is the same as the current one
        if (currentUser.getUsername().equals(newUsername)) {
            showError(usernameErrorLabel, "Le nouveau nom d'utilisateur est identique à l'actuel", newUsernameField);
            return;
        }

        try {
            // Verify current password
            User authenticatedUser = userService.authenticate(currentUser.getUsername(), password);

            if (authenticatedUser == null) {
                showError(usernameErrorLabel, "Mot de passe incorrect", passwordForUsernameField);
                return;
            }

            // Create confirmation dialog
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            boolean confirmed = ConfirmationDialog.show(
                    stage,
                    "Confirmation de modification",
                    "Modifier le nom d'utilisateur",
                    "Êtes-vous sûr de vouloir modifier votre nom d'utilisateur ?",
                    ConfirmationDialog.DialogType.SAVE
            );

            if (confirmed) {
                // Update username
                boolean success = userService.updateUsername(currentUser.getId(), newUsername);

                if (success) {

                    // Log the action
                    auditLogService.logAction(
                            "UPDATE",
                            "USER",
                            (long) currentUser.getId(),
                            "Modification du nom d'utilisateur de " + currentUser.getUsername() + " à " + newUsername
                    );
                    // Update current user in session
                    currentUser.setUsername(newUsername);
                    SessionManager.setCurrentUser(currentUser);

                    // Update displayed username
                    if (currentUsernameLabel != null) {
                        currentUsernameLabel.setText(newUsername);
                    }
                    if (currentUsernameDisplay != null) {
                        currentUsernameDisplay.setText(newUsername);
                    }

                    // Clear fields
                    newUsernameField.clear();
                    passwordForUsernameField.clear();

                    // Show success message
                    showSuccess(usernameSuccessLabel, "Nom d'utilisateur mis à jour avec succès");

                    // Show notification
                    NotificationManager.showSuccess(stage, "Nom d'utilisateur mis à jour",
                            "Votre nom d'utilisateur a été modifié avec succès.");
                } else {
                    showError(usernameErrorLabel, "Erreur lors de la mise à jour du nom d'utilisateur", null);
                }
            }
        } catch (SQLException e) {
            showError(usernameErrorLabel, "Erreur de base de données: " + e.getMessage(), null);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSaveEmail(ActionEvent event) {
        // Clear previous messages
        clearEmailMessages();

        // Validate new email
        if (!Verification.validateEmail(emailField, emailErrorLabel)) {
            showError(emailErrorLabel, "Format d'email invalide", emailField);
            shakeNode(emailField);
            return;
        }

        // Validate password for confirmation
        if (!Verification.validatePassword(passwordForEmailField, emailErrorLabel, 1)) {
            showError(emailErrorLabel, "Mot de passe invalide", passwordForEmailField);
            shakeNode(passwordForEmailField);
            return;
        }

        String newEmail = emailField.getText().trim();
        String password = passwordForEmailField.getText().trim();

        // Check if the new email is the same as the current one
        if (currentUser.getEmail() != null && currentUser.getEmail().equals(newEmail)) {
            showError(emailErrorLabel, "La nouvelle adresse email est identique à l'actuelle", emailField);
            return;
        }

        try {
            // Verify current password
            User authenticatedUser = userService.authenticate(currentUser.getUsername(), password);

            if (authenticatedUser == null) {
                showError(emailErrorLabel, "Mot de passe incorrect", passwordForEmailField);
                return;
            }

            // Create confirmation dialog
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            boolean confirmed = ConfirmationDialog.show(
                    stage,
                    "Confirmation de modification",
                    "Modifier l'adresse email",
                    "Êtes-vous sûr de vouloir modifier votre adresse email ?",
                    ConfirmationDialog.DialogType.SAVE
            );

            if (confirmed) {
                // Update email
                boolean success = userService.updateEmail(currentUser.getId(), newEmail);

                if (success) {

                    // Log the action
                    auditLogService.logAction(
                            "UPDATE",
                            "USER",
                            (long) currentUser.getId(),
                            "Modification d'email d'utilisateur de " + currentUser.getEmail() + " à " + newEmail
                    );
                    // Update current user in session
                    currentUser.setEmail(newEmail);
                    SessionManager.setCurrentUser(currentUser);

                    // Clear fields
                    emailField.clear();
                    passwordForEmailField.clear();

                    // Update prompt text to show new email
                    emailField.setPromptText(newEmail);

                    // Show success message
                    showSuccess(emailSuccessLabel, "Adresse email mise à jour avec succès");

                    // Show notification
                    NotificationManager.showSuccess(stage, "Adresse email mise à jour",
                            "Votre adresse email a été modifiée avec succès.");
                } else {
                    showError(emailErrorLabel, "Erreur lors de la mise à jour de l'adresse email", null);
                }
            }
        } catch (SQLException e) {
            showError(emailErrorLabel, "Erreur de base de données: " + e.getMessage(), null);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSavePhoneNumber(ActionEvent event) {
        // Clear previous messages
        clearPhoneMessages();

        // Validate new phone number
        if (!Verification.validatePhone(phoneNumberField, phoneErrorLabel)) {
            showError(phoneErrorLabel, "Numéro de téléphone invalide", phoneNumberField);
            shakeNode(phoneNumberField);
            return;
        }

        // Validate password for confirmation
        if (!Verification.validatePassword(passwordForPhoneField, phoneErrorLabel, 1)) {
            showError(phoneErrorLabel, "Mot de passe invalide", passwordForPhoneField);
            shakeNode(passwordForPhoneField);
            return;
        }

        String newPhoneNumber = phoneNumberField.getText().trim();
        String password = passwordForPhoneField.getText().trim();

        // Check if the new phone number is the same as the current one
        if (currentUser.getPhoneNumber() != null && currentUser.getPhoneNumber().equals(newPhoneNumber)) {
            showError(phoneErrorLabel, "Le nouveau numéro de téléphone est identique à l'actuel", phoneNumberField);
            return;
        }

        try {
            // Verify current password
            User authenticatedUser = userService.authenticate(currentUser.getUsername(), password);

            if (authenticatedUser == null) {
                showError(phoneErrorLabel, "Mot de passe incorrect", passwordForPhoneField);
                return;
            }

            // Create confirmation dialog
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            boolean confirmed = ConfirmationDialog.show(
                    stage,
                    "Confirmation de modification",
                    "Modifier le numéro de téléphone",
                    "Êtes-vous sûr de vouloir modifier votre numéro de téléphone ?",
                    ConfirmationDialog.DialogType.SAVE
            );

            if (confirmed) {
                // Update phone number
                boolean success = userService.updatePhoneNumber(currentUser.getId(), newPhoneNumber);

                if (success) {

                    // Log the action
                    auditLogService.logAction(
                            "UPDATE",
                            "USER",
                            (long) currentUser.getId(),
                            "Modification du numéro de téléphone d'utilisateur de " + currentUser.getPhoneNumber() + " à " + newPhoneNumber
                    );
                    // Update current user in session
                    currentUser.setPhoneNumber(newPhoneNumber);
                    SessionManager.setCurrentUser(currentUser);

                    // Clear fields
                    phoneNumberField.clear();
                    passwordForPhoneField.clear();

                    // Update prompt text to show new phone number
                    phoneNumberField.setPromptText(newPhoneNumber);

                    // Show success message
                    showSuccess(phoneSuccessLabel, "Numéro de téléphone mis à jour avec succès");

                    // Show notification
                    NotificationManager.showSuccess(stage, "Numéro de téléphone mis à jour",
                            "Votre numéro de téléphone a été modifié avec succès.");
                } else {
                    showError(phoneErrorLabel, "Erreur lors de la mise à jour du numéro de téléphone", null);
                }
            }
        } catch (SQLException e) {
            showError(phoneErrorLabel, "Erreur de base de données: " + e.getMessage(), null);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSavePassword(ActionEvent event) {
        // Clear previous messages
        clearPasswordMessages();

        // Validate current password
        if (!Verification.validatePassword(currentPasswordField, passwordErrorLabel, 1)) {
            showError(passwordErrorLabel, "Mot de passe actuel requis", currentPasswordField);
            shakeNode(currentPasswordField);
            return;
        }

        // Validate new password
        if (!Verification.validatePassword(newPasswordField, passwordErrorLabel, 5)) {
            showError(passwordErrorLabel, "Le nouveau mot de passe doit contenir au moins 5 caractères", newPasswordField);
            shakeNode(newPasswordField);
            return;
        }

        // Validate password confirmation
        if (!Verification.validatePasswordsMatch(newPasswordField, confirmPasswordField, passwordErrorLabel)) {
            showError(passwordErrorLabel, "Les mots de passe ne correspondent pas", confirmPasswordField);
            shakeNode(confirmPasswordField);
            return;
        }

        String currentPassword = currentPasswordField.getText().trim();
        String newPassword = newPasswordField.getText().trim();

        // Validate that new password is different from current password
        if (!validateNewPasswordDifferent(currentPassword, newPassword, passwordErrorLabel, newPasswordField)) {
            return;
        }

        try {
            // Verify current password
            User authenticatedUser = userService.authenticate(currentUser.getUsername(), currentPassword);

            if (authenticatedUser == null) {
                showError(passwordErrorLabel, "Mot de passe actuel incorrect", currentPasswordField);
                return;
            }

            // Create confirmation dialog
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            boolean confirmed = ConfirmationDialog.show(
                    stage,
                    "Confirmation de modification",
                    "Modifier le mot de passe",
                    "Êtes-vous sûr de vouloir modifier votre mot de passe ?",
                    ConfirmationDialog.DialogType.SAVE
            );

            if (confirmed) {
                // Update password
                boolean success = userService.updatePassword(currentUser.getId(), newPassword);

                if (success) {
                    // Log the action
                    auditLogService.logAction(
                            "UPDATE",
                            "USER",
                            (long) currentUser.getId(),
                            "Modification du mot de passe d'utilisateur"
                    );
                    // Clear fields
                    currentPasswordField.clear();
                    newPasswordField.clear();
                    confirmPasswordField.clear();

                    // Show success message
                    showSuccess(passwordSuccessLabel, "Mot de passe mis à jour avec succès");

                    // Show notification
                    NotificationManager.showSuccess(stage, "Mot de passe mis à jour",
                            "Votre mot de passe a été modifié avec succès.");
                } else {
                    showError(passwordErrorLabel, "Erreur lors de la mise à jour du mot de passe", null);
                }
            }
        } catch (SQLException e) {
            showError(passwordErrorLabel, "Erreur de base de données: " + e.getMessage(), null);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSaveNotifications(ActionEvent event) {
        // Implementation for saving notification settings
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        // Show success notification
        NotificationManager.showSuccess(stage, "Paramètres de notification enregistrés",
                "Vos préférences de notification ont été mises à jour avec succès.");
    }

    @FXML
    private void handleSaveSystemSettings(ActionEvent event) {
        // Implementation for saving system settings
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        // Show success notification
        NotificationManager.showSuccess(stage, "Paramètres système enregistrés",
                "Vos paramètres système ont été mises à jour avec succès.");
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        // Show confirmation dialog
        boolean confirmed = ConfirmationDialog.show(
                stage,
                "Confirmation de déconnexion",
                "Déconnexion",
                "Êtes-vous sûr de vouloir vous déconnecter ?",
                ConfirmationDialog.DialogType.LOGOUT
        );

        if (confirmed) {
            try {
                // Clear current user session
                SessionManager.clearSession();

                // Get the current stage (settings window)
                Stage settingsStage = (Stage) ((Node) event.getSource()).getScene().getWindow();

                // Close the settings window
                settingsStage.close();

                // If we have a reference to the main stage, navigate it to login
                if (mainStage != null) {
                    // Load login screen
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/IHM/LoginIHM.fxml"));
                    Parent root = loader.load();

                    // Set the scene on the main stage
                    mainStage.setTitle("Auto-École - Login");
                    mainStage.setScene(new Scene(root));
                    mainStage.show();
                } else {
                    // Fallback if we don't have the main stage reference
                    // Try to find the main stage from the window owner
                    if (settingsStage.getOwner() != null && settingsStage.getOwner() instanceof Stage) {
                        Stage ownerStage = (Stage) settingsStage.getOwner();

                        // Load login screen
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/IHM/LoginIHM.fxml"));
                        Parent root = loader.load();

                        // Set the scene on the owner stage
                        ownerStage.setTitle("Auto-École - Login");
                        ownerStage.setScene(new Scene(root));
                        ownerStage.show();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger l'écran de connexion");
            }
        }
    }

    @FXML
    private void handleClose(ActionEvent event) {
        // Close the settings window
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleBack(ActionEvent event) {
        // Close the settings window and return to previous screen
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleBackupManagement(ActionEvent event) {
        // Check if user is admin
        if (currentUser == null || !currentUser.isAdmin()) {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            NotificationManager.showWarning(stage, "Accès refusé",
                    "Seuls les administrateurs peuvent gérer les sauvegardes.");
            return;
        }

        try {
            // Load the backup management view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/IHM/BackupIHM.fxml"));
            Parent root = loader.load();

            // Create a new stage for the backup management
            Stage backupStage = new Stage();
            backupStage.setTitle("Gestion des sauvegardes - Auto-École Pro");

            // Set minimum dimensions
            backupStage.setMinWidth(800);
            backupStage.setMinHeight(600);

            // Create scene and set it on the stage
            Scene scene = new Scene(root);

            // Add stylesheet if needed
            if (getClass().getResource("/Styles/Common.css") != null) {
                scene.getStylesheets().add(getClass().getResource("/Styles/Common.css").toExternalForm());
            }

            backupStage.setScene(scene);

            // Set modality (makes the backup window modal)
            backupStage.initModality(Modality.APPLICATION_MODAL);

            // Set owner (sets the parent window)
            backupStage.initOwner(((Node) event.getSource()).getScene().getWindow());

            // Show the backup management window
            backupStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            NotificationManager.showError(stage, "Erreur",
                    "Erreur lors de l'ouverture de la gestion des sauvegardes: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}