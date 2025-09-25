package org.example.Controllers;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.Entities.DrivingSchoolInfo;
import org.example.Services.DrivingSchoolService;
import org.example.Utils.ConfirmationDialog;
import org.example.Utils.NotificationManager;
import org.example.Utils.Verification;
import org.example.Service.AuditLogService;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ResourceBundle;
import java.util.UUID;

public class DrivingSchoolController implements Initializable {

    // FXML elements
    @FXML private TextField nameField;
    @FXML private TextField matriculeField;
    @FXML private TextField addressField;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;
    @FXML private ImageView logoImageView;
    @FXML private Label logoPathLabel;
    @FXML private Label schoolNameLabel;
    @FXML private Button saveButton;

    // Error labels
    @FXML private Label nameErrorLabel;
    @FXML private Label matriculeErrorLabel;
    @FXML private Label addressErrorLabel;
    @FXML private Label phoneErrorLabel;
    @FXML private Label emailErrorLabel;

    // Service
    private DrivingSchoolService drivingSchoolService;
    private AuditLogService auditLogService;

    // Current school info
    private DrivingSchoolInfo schoolInfo;

    // Logo path
    private String currentLogoPath;
    private String newLogoPath;

    // Constants for styling
    private static final String ERROR_STYLE = "-fx-border-color: #ef4444; -fx-border-width: 2px;";
    private static final String NORMAL_STYLE = "-fx-border-color: #DEE2E6; -fx-border-width: 1px;";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize services
        drivingSchoolService = new DrivingSchoolService();
        auditLogService = new AuditLogService();

        // Initialize error labels
        initializeErrorLabels();

        // Load school info
        loadSchoolInfo();

        // Setup field listeners for validation
        setupFieldListeners();

    }

    /**
     * Initializes error labels with empty text and hidden visibility
     */
    private void initializeErrorLabels() {
        // Initialize all error labels to be empty and hidden
        if (nameErrorLabel != null) {
            nameErrorLabel.setText("");
            nameErrorLabel.setVisible(false);
        }

        if (matriculeErrorLabel != null) {
            matriculeErrorLabel.setText("");
            matriculeErrorLabel.setVisible(false);
        }

        if (addressErrorLabel != null) {
            addressErrorLabel.setText("");
            addressErrorLabel.setVisible(false);
        }

        if (phoneErrorLabel != null) {
            phoneErrorLabel.setText("");
            phoneErrorLabel.setVisible(false);
        }

        if (emailErrorLabel != null) {
            emailErrorLabel.setText("");
            emailErrorLabel.setVisible(false);
        }
    }

    /**
     * Loads the driving school information from the database
     */
    private void loadSchoolInfo() {
        try {
            schoolInfo = drivingSchoolService.getDrivingSchool();

            // Populate fields with existing data
            if (schoolInfo != null) {
                nameField.setText(schoolInfo.getName());
                matriculeField.setText(schoolInfo.getMatriculeFiscale());
                addressField.setText(schoolInfo.getAddress());
                phoneField.setText(schoolInfo.getPhoneNumber());
                emailField.setText(schoolInfo.getEmail());

                // Update school name label
                schoolNameLabel.setText(schoolInfo.getName());

                // Load logo if exists
                currentLogoPath = schoolInfo.getLogoPath();
                if (currentLogoPath != null && !currentLogoPath.isEmpty()) {
                    try {
                        File logoFile = new File(currentLogoPath);
                        if (logoFile.exists()) {
                            Image logoImage = new Image(logoFile.toURI().toString());
                            logoImageView.setImage(logoImage);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Stage stage = (Stage) nameField.getScene().getWindow();
            NotificationManager.showError(stage, "Erreur de chargement",
                    "Impossible de charger les informations de l'école de conduite.");
        }
    }

    /**
     * Sets up field listeners for validation feedback
     */
    private void setupFieldListeners() {
        // Add focus listeners to all fields for visual feedback
        setupFieldListener(nameField, nameErrorLabel);
        setupFieldListener(matriculeField, matriculeErrorLabel);
        setupFieldListener(addressField, addressErrorLabel);
        setupFieldListener(phoneField, phoneErrorLabel);
        setupFieldListener(emailField, emailErrorLabel);
    }

    /**
     * Sets up a focus listener for a text field
     * @param field The field to set up
     * @param errorLabel The associated error label
     */
    private void setupFieldListener(TextField field, Label errorLabel) {
        if (field != null) {
            field.focusedProperty().addListener((obs, oldVal, newVal) -> {
                if (!newVal) { // Field lost focus
                    validateField(field, errorLabel);
                }
            });
        }
    }

    /**
     * Validates a field based on its type
     * @param field The field to validate
     * @param errorLabel The associated error label
     * @return true if valid, false otherwise
     */
    private boolean validateField(TextField field, Label errorLabel) {
        if (field == nameField) {
            return validateName(field, errorLabel);
        } else if (field == matriculeField) {
            return validateMatricule(field, errorLabel);
        } else if (field == addressField) {
            return validateAddress(field, errorLabel);
        } else if (field == phoneField) {
            return validatePhone(field, errorLabel);
        } else if (field == emailField) {
            return validateEmail(field, errorLabel);
        }
        return true;
    }

    /**
     * Validates the name field
     * @param field The name field
     * @param errorLabel The associated error label
     * @return true if valid, false otherwise
     */
    private boolean validateName(TextField field, Label errorLabel) {
        if (!Verification.validateRequired(field, errorLabel)) {
            showFieldError(field, errorLabel, "Le nom de l'école ne peut pas être vide");
            Stage stage = (Stage) field.getScene().getWindow();
            NotificationManager.showWarning(stage, "Validation", "Le nom de l'école ne peut pas être vide");
            return false;
        }

        if (!Verification.validateName(field, errorLabel)) {
            showFieldError(field, errorLabel, "Le nom doit contenir uniquement des lettres, espaces, apostrophes et tirets");
            Stage stage = (Stage) field.getScene().getWindow();
            NotificationManager.showWarning(stage, "Validation", "Le nom doit contenir uniquement des lettres, espaces, apostrophes et tirets");
            return false;
        }

        clearFieldError(field, errorLabel);
        return true;
    }

    /**
     * Validates the matricule field
     * @param field The matricule field
     * @param errorLabel The associated error label
     * @return true if valid, false otherwise
     */
    private boolean validateMatricule(TextField field, Label errorLabel) {
        if (!Verification.validateRequired(field, errorLabel)) {
            showFieldError(field, errorLabel, "Le matricule fiscale ne peut pas être vide");
            Stage stage = (Stage) field.getScene().getWindow();
            NotificationManager.showWarning(stage, "Validation", "Le matricule fiscale ne peut pas être vide");
            return false;
        }

        // Add specific validation for matricule format if needed

        clearFieldError(field, errorLabel);
        return true;
    }

    /**
     * Validates the address field
     * @param field The address field
     * @param errorLabel The associated error label
     * @return true if valid, false otherwise
     */
    private boolean validateAddress(TextField field, Label errorLabel) {
        if (!Verification.validateRequired(field, errorLabel)) {
            showFieldError(field, errorLabel, "L'adresse ne peut pas être vide");
            Stage stage = (Stage) field.getScene().getWindow();
            NotificationManager.showWarning(stage, "Validation", "L'adresse ne peut pas être vide");
            return false;
        }

        clearFieldError(field, errorLabel);
        return true;
    }


    /**
     * Validates the phone field
     * @param field The phone field
     * @param errorLabel The associated error label
     * @return true if valid, false otherwise
     */
    private boolean validatePhone(TextField field, Label errorLabel) {
        if (!Verification.validateRequired(field, errorLabel)) {
            showFieldError(field, errorLabel, "Le numéro de téléphone ne peut pas être vide");
            Stage stage = (Stage) field.getScene().getWindow();
            NotificationManager.showWarning(stage, "Validation", "Le numéro de téléphone ne peut pas être vide");
            return false;
        }

        if (!Verification.validatePhone(field, errorLabel)) {
            showFieldError(field, errorLabel, "Le numéro de téléphone doit contenir 8 chiffres (ne commence pas par 0)");
            Stage stage = (Stage) field.getScene().getWindow();
            NotificationManager.showWarning(stage, "Validation", "Le numéro de téléphone doit contenir 8 chiffres (ne commence pas par 0)");
            return false;
        }

        clearFieldError(field, errorLabel);
        return true;
    }
    @FXML public void handleBack(ActionEvent event) {
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

    /**
     * Validates the email field
     * @param field The email field
     * @param errorLabel The associated error label
     * @return true if valid, false otherwise
     */
    private boolean validateEmail(TextField field, Label errorLabel) {
        if (!Verification.validateRequired(field, errorLabel)) {
            showFieldError(field, errorLabel, "L'email ne peut pas être vide");
            Stage stage = (Stage) field.getScene().getWindow();
            NotificationManager.showWarning(stage, "Validation", "L'email ne peut pas être vide");
            return false;
        }

        if (!Verification.validateEmail(field, errorLabel)) {
            showFieldError(field, errorLabel, "Format d'email invalide");
            Stage stage = (Stage) field.getScene().getWindow();
            NotificationManager.showWarning(stage, "Validation", "Format d'email invalide");
            return false;
        }

        clearFieldError(field, errorLabel);
        return true;
    }

    /**
     * Shows an error message for a field
     * @param field The field with error
     * @param errorLabel The error label to display the message
     * @param message The error message
     */
    private void showFieldError(TextField field, Label errorLabel, String message) {
        if (field != null) {
            field.setStyle(ERROR_STYLE);
        }

        if (errorLabel != null) {
            errorLabel.setText(message);
            errorLabel.setVisible(true);
        }
    }

    /**
     * Clears error styling and message for a field
     * @param field The field to clear
     * @param errorLabel The error label to clear
     */
    private void clearFieldError(TextField field, Label errorLabel) {
        if (field != null) {
            field.setStyle(NORMAL_STYLE);
        }

        if (errorLabel != null) {
            errorLabel.setText("");
            errorLabel.setVisible(false);
        }
    }

    /**
     * Validates all fields
     * @return true if all fields are valid, false otherwise
     */
    private boolean validateAllFields() {
        boolean nameValid = validateName(nameField, nameErrorLabel);
        boolean matriculeValid = validateMatricule(matriculeField, matriculeErrorLabel);
        boolean addressValid = validateAddress(addressField, addressErrorLabel);
        boolean phoneValid = validatePhone(phoneField, phoneErrorLabel);
        boolean emailValid = validateEmail(emailField, emailErrorLabel);

        return nameValid && matriculeValid && addressValid && phoneValid && emailValid;
    }

    /**
     * Handles the upload logo button click
     */
    @FXML
    private void handleUploadLogo(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner un logo");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null) {
            try {
                // Generate a unique filename for the logo
                String fileName = "logo_" + UUID.randomUUID().toString() + getFileExtension(selectedFile.getName());

                // Define the destination directory (create if it doesn't exist)
                Path destinationDir = Paths.get("src/main/resources/images/logos");
                if (!Files.exists(destinationDir)) {
                    Files.createDirectories(destinationDir);
                }

                // Define the destination path
                Path destinationPath = destinationDir.resolve(fileName);

                // Copy the file to the destination
                Files.copy(selectedFile.toPath(), destinationPath, StandardCopyOption.REPLACE_EXISTING);

                // Update the logo path
                newLogoPath = destinationPath.toString();

                // Update the UI
                Image logoImage = new Image(destinationPath.toUri().toString());
                logoImageView.setImage(logoImage);
                logoPathLabel.setText(newLogoPath);

                // Show success notification
                NotificationManager.showSuccess(stage, "Logo chargé",
                        "Le logo a été chargé avec succès. N'oubliez pas d'enregistrer les modifications.");

            } catch (Exception e) {
                e.printStackTrace();
                NotificationManager.showError(stage, "Erreur de chargement",
                        "Impossible de charger le logo: " + e.getMessage());
            }
        }
    }

    /**
     * Gets the file extension from a filename
     * @param filename The filename
     * @return The file extension including the dot
     */
    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return filename.substring(lastDotIndex);
        }
        return "";
    }

    /**
     * Handles the save button click
     */
    @FXML
    private void handleSave(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        // Validate all fields but don't show generic message
        boolean isValid = true;

        // Validate each field individually to show specific error messages
        boolean nameValid = validateName(nameField, nameErrorLabel);
        boolean matriculeValid = validateMatricule(matriculeField, matriculeErrorLabel);
        boolean addressValid = validateAddress(addressField, addressErrorLabel);
        boolean phoneValid = validatePhone(phoneField, phoneErrorLabel);
        boolean emailValid = validateEmail(emailField, emailErrorLabel);

        isValid = nameValid && matriculeValid && addressValid && phoneValid && emailValid;

        // If validation failed, return without showing generic message
        if (!isValid) {
            return;
        }

        // Show confirmation dialog
        boolean confirmed = ConfirmationDialog.show(
                stage,
                "Confirmation de modification",
                "Enregistrer les modifications",
                "Êtes-vous sûr de vouloir enregistrer les modifications de l'école de conduite ?",
                ConfirmationDialog.DialogType.SAVE
        );

        if (confirmed) {
            try {
                // Update school info object
                schoolInfo.setName(nameField.getText().trim());
                schoolInfo.setMatriculeFiscale(matriculeField.getText().trim());
                schoolInfo.setAddress(addressField.getText().trim());
                schoolInfo.setPhoneNumber(phoneField.getText().trim());
                schoolInfo.setEmail(emailField.getText().trim());
                schoolInfo.setUpdatedAt(LocalDateTime.now());

                // Update logo path if a new one was selected
                if (newLogoPath != null && !newLogoPath.isEmpty()) {
                    schoolInfo.setLogoPath(newLogoPath);
                }

                // Save to database
                drivingSchoolService.updateDrivingSchool(schoolInfo);

                // Log the action
                auditLogService.logAction(
                        "UPDATE",
                        "DRIVING_SCHOOL",
                        schoolInfo.getId(),
                        "Mise à jour des informations de l'école de conduite: " + schoolInfo.getName()
                );

                // Update UI
                schoolNameLabel.setText(schoolInfo.getName());

                // Show success notification
                NotificationManager.showSuccess(stage, "Modifications enregistrées",
                        "Les informations de l'école de conduite ont été mises à jour avec succès.");

            } catch (SQLException e) {
                e.printStackTrace();
                NotificationManager.showError(stage, "Erreur d'enregistrement",
                        "Impossible d'enregistrer les modifications: " + e.getMessage());
            }
        }
    }

    /**
     * Handles the back button click
     */

    /**
     * Handles the settings button click
     */
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

    /**
     * Handles the help button click
     */
    @FXML
    public void handleHelp(ActionEvent event) {
        try {
            // Load the help view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/IHM/HelpIHM.fxml"));
            Parent helpRoot = loader.load();

            // Get the controller
            HelpController helpController = loader.getController();

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
            helpStage.setTitle("Aide - Auto-��cole Pro");
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

    /**
     * Shows an error alert
     * @param title The alert title
     * @param content The alert content
     */
    private void showErrorAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}