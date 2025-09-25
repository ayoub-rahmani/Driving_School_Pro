package org.example.Controllers;

import javafx.animation.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.Entities.User;
import org.example.Service.UserService;
import org.example.Service.EmailService;
import org.example.Utils.NotificationManager;
import org.example.Utils.Verification;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Random;
import java.util.ResourceBundle;

public class PasswordRecoveryController implements Initializable {

    @FXML private VBox step1Container;
    @FXML private VBox step2Container;
    @FXML private VBox step3Container;

    @FXML private Circle step1Circle;
    @FXML private Circle step2Circle;
    @FXML private Circle step3Circle;

    @FXML private TextField usernameField;
    @FXML private Label emailLabel;
    @FXML private Label usernameErrorLabel;

    @FXML private TextField digit1Field;
    @FXML private TextField digit2Field;
    @FXML private TextField digit3Field;
    @FXML private TextField digit4Field;

    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label passwordErrorLabel;

    @FXML private Label errorLabel;
    @FXML private Label successLabel;

    private UserService userService;
    private EmailService emailService;
    private User recoveryUser;
    private String verificationCode;

    public PasswordRecoveryController() throws SQLException {
        userService = new UserService();
        // Initialize EmailService in the initialize method instead
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Clear any messages
        errorLabel.setText("");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        successLabel.setText("");
        successLabel.setVisible(false);
        successLabel.setManaged(false);

        usernameErrorLabel.setText("");
        usernameErrorLabel.setVisible(false);
        usernameErrorLabel.setManaged(false);

        passwordErrorLabel.setText("");
        passwordErrorLabel.setVisible(false);
        passwordErrorLabel.setManaged(false);

        // Initialize EmailService
        try {
            emailService = new EmailService();
            if (!emailService.isConfigured()) {
                System.out.println("Email service is using default configuration. Emails will be simulated.");
            }
        } catch (Exception e) {
            System.err.println("Failed to initialize EmailService: " + e.getMessage());
            e.printStackTrace();
            showError("Erreur de configuration du service email. Contactez l'administrateur.");
        }

        // Setup digit fields to automatically move focus
        setupDigitFields();

        // Show the first step
        showStep1();
    }

    private void setupDigitFields() {
        // Only allow numbers in digit fields
        digit1Field.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                digit1Field.setText(newValue.replaceAll("[^\\d]", ""));
                return;
            }

            // Limit to one character
            if (newValue.length() > 1) {
                digit1Field.setText(newValue.substring(0, 1));
            }

            // Move to next field when a digit is entered
            if (newValue.length() == 1) {
                digit2Field.requestFocus();
            }
        });

        digit2Field.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                digit2Field.setText(newValue.replaceAll("[^\\d]", ""));
                return;
            }

            if (newValue.length() > 1) {
                digit2Field.setText(newValue.substring(0, 1));
            }

            if (newValue.length() == 1) {
                digit3Field.requestFocus();
            } else if (newValue.isEmpty() && oldValue.length() == 1) {
                // If backspace was pressed, go back to previous field
                digit1Field.requestFocus();
                digit1Field.selectAll();
            }
        });

        digit3Field.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                digit3Field.setText(newValue.replaceAll("[^\\d]", ""));
                return;
            }

            if (newValue.length() > 1) {
                digit3Field.setText(newValue.substring(0, 1));
            }

            if (newValue.length() == 1) {
                digit4Field.requestFocus();
            } else if (newValue.isEmpty() && oldValue.length() == 1) {
                digit2Field.requestFocus();
                digit2Field.selectAll();
            }
        });

        digit4Field.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                digit4Field.setText(newValue.replaceAll("[^\\d]", ""));
                return;
            }

            if (newValue.length() > 1) {
                digit4Field.setText(newValue.substring(0, 1));
            }

            if (newValue.isEmpty() && oldValue.length() == 1) {
                digit3Field.requestFocus();
                digit3Field.selectAll();
            }
        });

        // Add key event handler for Enter key on the last digit field
        digit4Field.setOnKeyPressed(event -> {
            if (event.getCode().toString().equals("ENTER")) {
                handleStep2Next(new ActionEvent());
            }
        });
    }

    @FXML
    private void handleStep1Next(ActionEvent event) {
        // Clear previous messages
        hideError();
        hideSuccess();
        hideUsernameError();

        String username = usernameField.getText().trim();

        // Validate username is not empty
        if (username.isEmpty()) {
            showUsernameError("Veuillez entrer votre nom d'utilisateur");
            shakeTextField(usernameField);
            return;
        }

        try {
            // Check if EmailService is initialized
            if (emailService == null) {
                showError("Service email non disponible. Contactez l'administrateur.");
                return;
            }

            // Find user by username
            recoveryUser = userService.findByUsername(username);

            // Check if user exists
            if (recoveryUser == null) {
                showUsernameError("Nom d'utilisateur introuvable");
                shakeTextField(usernameField);
                return;
            }

            // Check if user has an email
            String email = recoveryUser.getEmail();
            if (email == null || email.isEmpty()) {
                showError("Aucune adresse email associée à ce compte");
                return;
            }

            // Generate verification code
            verificationCode = generateVerificationCode();

            // Send verification code to user's email
            boolean messageSent = emailService.sendVerificationCode(email, verificationCode);

            if (!messageSent) {
                showError("Erreur lors de l'envoi de l'email");
                return;
            }

            // Display masked email
            emailLabel.setText(maskEmail(email));

            // Move to step 2
            showStep2();

        } catch (SQLException e) {
            showError("Erreur de base de données: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            showError("Erreur inattendue: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleStep2Next(ActionEvent event) {
        // Clear previous messages
        hideError();
        hideSuccess();

        // Get entered code
        String enteredCode = digit1Field.getText() + digit2Field.getText() +
                digit3Field.getText() + digit4Field.getText();

        if (enteredCode.length() != 4) {
            showError("Veuillez entrer le code complet à 4 chiffres");
            shakeDigitFields();
            return;
        }

        // Verify code
        if (!enteredCode.equals(verificationCode)) {
            showError("Code incorrect. Veuillez réessayer.");
            shakeDigitFields();
            clearDigitFields();
            digit1Field.requestFocus();
            return;
        }

        // Move to step 3
        showStep3();
    }

    @FXML
    private void handleStep2Back(ActionEvent event) {
        showStep1();
    }

    @FXML
    private void handleStep3Back(ActionEvent event) {
        showStep2();
    }

    @FXML
    private void handlePasswordReset(ActionEvent event) {
        // Clear previous messages
        hideError();
        hideSuccess();
        hidePasswordError();

        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // Validate password length
        if (newPassword.length() < 5) {
            showPasswordError("Le mot de passe doit contenir au moins 5 caractères");
            shakePasswordField(newPasswordField);
            return;
        }

        // Validate passwords match
        if (!newPassword.equals(confirmPassword)) {
            showPasswordError("Les mots de passe ne correspondent pas");
            shakePasswordField(confirmPasswordField);
            return;
        }

        try {
            // Update password
            boolean success = userService.updatePassword(recoveryUser.getId(), newPassword);

            if (success) {
                showSuccess("Mot de passe réinitialisé avec succès");

                // Clear fields
                clearAllFields();

                // Show notification
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                NotificationManager.showSuccess(stage, "Mot de passe réinitialisé",
                        "Votre mot de passe a été réinitialisé avec succès. Vous pouvez maintenant vous connecter.");

                // Redirect to login after 3 seconds
                new Thread(() -> {
                    try {
                        Thread.sleep(3000);
                        javafx.application.Platform.runLater(() -> {
                            try {
                                // Load login screen
                                FXMLLoader loader = new FXMLLoader(getClass().getResource("/IHM/LoginIHM.fxml"));
                                Parent root = loader.load();

                                // Get current stage
                                Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();

                                // Set the scene
                                currentStage.setTitle("Auto-École - Login");
                                currentStage.setScene(new Scene(root));
                                currentStage.show();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }).start();
            } else {
                showError("Erreur lors de la réinitialisation du mot de passe");
            }
        } catch (SQLException e) {
            showError("Erreur de base de données: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleResendCode(ActionEvent event) {
        // Clear previous messages
        hideError();
        hideSuccess();

        if (recoveryUser == null) {
            showError("Erreur: utilisateur non trouvé");
            return;
        }

        String email = recoveryUser.getEmail();
        if (email == null || email.isEmpty()) {
            showError("Aucune adresse email associée à ce compte");
            return;
        }

        // Check if EmailService is initialized
        if (emailService == null) {
            showError("Service email non disponible. Contactez l'administrateur.");
            return;
        }

        // Generate new verification code
        verificationCode = generateVerificationCode();

        // Send verification code to user's email
        boolean messageSent = emailService.sendVerificationCode(email, verificationCode);

        if (!messageSent) {
            showError("Erreur lors de l'envoi de l'email");
            return;
        }

        showSuccess("Un nouveau code a été envoyé à votre adresse email");

        // Clear digit fields
        clearDigitFields();
        digit1Field.requestFocus();
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        try {
            // Load login screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/IHM/LoginIHM.fxml"));
            Parent root = loader.load();

            // Get current stage
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Set the scene
            currentStage.setTitle("Auto-École - Login");
            currentStage.setScene(new Scene(root));
            currentStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void showStep1() {
        step1Container.setVisible(true);
        step1Container.setManaged(true);

        step2Container.setVisible(false);
        step2Container.setManaged(false);

        step3Container.setVisible(false);
        step3Container.setManaged(false);

        step1Circle.getStyleClass().remove("step-inactive");
        step1Circle.getStyleClass().add("step-active");

        step2Circle.getStyleClass().remove("step-active");
        step2Circle.getStyleClass().add("step-inactive");

        step3Circle.getStyleClass().remove("step-active");
        step3Circle.getStyleClass().add("step-inactive");

        // Clear error and success messages
        hideError();
        hideSuccess();
        hideUsernameError();
    }

    @FXML
    private void showStep2() {
        step1Container.setVisible(false);
        step1Container.setManaged(false);

        step2Container.setVisible(true);
        step2Container.setManaged(true);

        step3Container.setVisible(false);
        step3Container.setManaged(false);

        step1Circle.getStyleClass().remove("step-active");
        step1Circle.getStyleClass().add("step-inactive");

        step2Circle.getStyleClass().remove("step-inactive");
        step2Circle.getStyleClass().add("step-active");

        step3Circle.getStyleClass().remove("step-active");
        step3Circle.getStyleClass().add("step-inactive");

        // Clear error and success messages
        hideError();
        hideSuccess();

        // Clear and focus on first digit field
        clearDigitFields();
        digit1Field.requestFocus();
    }

    @FXML
    private void showStep3() {
        step1Container.setVisible(false);
        step1Container.setManaged(false);

        step2Container.setVisible(false);
        step2Container.setManaged(false);

        step3Container.setVisible(true);
        step3Container.setManaged(true);

        step1Circle.getStyleClass().remove("step-active");
        step1Circle.getStyleClass().add("step-inactive");

        step2Circle.getStyleClass().remove("step-active");
        step2Circle.getStyleClass().add("step-inactive");

        step3Circle.getStyleClass().remove("step-inactive");
        step3Circle.getStyleClass().add("step-active");

        // Clear error and success messages
        hideError();
        hideSuccess();
        hidePasswordError();

        // Clear password fields
        newPasswordField.clear();
        confirmPasswordField.clear();
        newPasswordField.requestFocus();
    }

    private String generateVerificationCode() {
        Random random = new Random();
        int code = 1000 + random.nextInt(9000); // 4-digit code between 1000 and 9999
        return String.valueOf(code);
    }

    private String maskEmail(String email) {
        if (email == null || email.isEmpty()) {
            return "***@***.***";
        }

        int atIndex = email.indexOf('@');
        if (atIndex <= 1) {
            return email; // Can't mask properly if @ is at the beginning
        }

        String username = email.substring(0, atIndex);
        String domain = email.substring(atIndex);

        // Show first character and last character of username, mask the rest
        String maskedUsername = username.charAt(0) +
                "*".repeat(Math.max(0, username.length() - 2)) +
                username.charAt(username.length() - 1);

        return maskedUsername + domain;
    }

    private void clearAllFields() {
        usernameField.clear();
        clearDigitFields();
        newPasswordField.clear();
        confirmPasswordField.clear();
    }

    private void clearDigitFields() {
        digit1Field.clear();
        digit2Field.clear();
        digit3Field.clear();
        digit4Field.clear();
    }

    private void shakeTextField(TextField textField) {
        Timeline shakeTimeline = new Timeline(
                new KeyFrame(Duration.millis(0), new KeyValue(textField.translateXProperty(), 0)),
                new KeyFrame(Duration.millis(100), new KeyValue(textField.translateXProperty(), -5)),
                new KeyFrame(Duration.millis(200), new KeyValue(textField.translateXProperty(), 5)),
                new KeyFrame(Duration.millis(300), new KeyValue(textField.translateXProperty(), -5)),
                new KeyFrame(Duration.millis(400), new KeyValue(textField.translateXProperty(), 5)),
                new KeyFrame(Duration.millis(500), new KeyValue(textField.translateXProperty(), 0))
        );
        shakeTimeline.play();
    }

    private void shakeDigitFields() {
        // Shake all digit fields together
        Timeline shakeTimeline = new Timeline(
                new KeyFrame(Duration.millis(0),
                        new KeyValue(digit1Field.translateXProperty(), 0),
                        new KeyValue(digit2Field.translateXProperty(), 0),
                        new KeyValue(digit3Field.translateXProperty(), 0),
                        new KeyValue(digit4Field.translateXProperty(), 0)),
                new KeyFrame(Duration.millis(100),
                        new KeyValue(digit1Field.translateXProperty(), -5),
                        new KeyValue(digit2Field.translateXProperty(), -5),
                        new KeyValue(digit3Field.translateXProperty(), -5),
                        new KeyValue(digit4Field.translateXProperty(), -5)),
                new KeyFrame(Duration.millis(200),
                        new KeyValue(digit1Field.translateXProperty(), 5),
                        new KeyValue(digit2Field.translateXProperty(), 5),
                        new KeyValue(digit3Field.translateXProperty(), 5),
                        new KeyValue(digit4Field.translateXProperty(), 5)),
                new KeyFrame(Duration.millis(300),
                        new KeyValue(digit1Field.translateXProperty(), -5),
                        new KeyValue(digit2Field.translateXProperty(), -5),
                        new KeyValue(digit3Field.translateXProperty(), -5),
                        new KeyValue(digit4Field.translateXProperty(), -5)),
                new KeyFrame(Duration.millis(400),
                        new KeyValue(digit1Field.translateXProperty(), 5),
                        new KeyValue(digit2Field.translateXProperty(), 5),
                        new KeyValue(digit3Field.translateXProperty(), 5),
                        new KeyValue(digit4Field.translateXProperty(), 5)),
                new KeyFrame(Duration.millis(500),
                        new KeyValue(digit1Field.translateXProperty(), 0),
                        new KeyValue(digit2Field.translateXProperty(), 0),
                        new KeyValue(digit3Field.translateXProperty(), 0),
                        new KeyValue(digit4Field.translateXProperty(), 0))
        );
        shakeTimeline.play();
    }

    private void shakePasswordField(PasswordField passwordField) {
        Timeline shakeTimeline = new Timeline(
                new KeyFrame(Duration.millis(0), new KeyValue(passwordField.translateXProperty(), 0)),
                new KeyFrame(Duration.millis(100), new KeyValue(passwordField.translateXProperty(), -5)),
                new KeyFrame(Duration.millis(200), new KeyValue(passwordField.translateXProperty(), 5)),
                new KeyFrame(Duration.millis(300), new KeyValue(passwordField.translateXProperty(), -5)),
                new KeyFrame(Duration.millis(400), new KeyValue(passwordField.translateXProperty(), 5)),
                new KeyFrame(Duration.millis(500), new KeyValue(passwordField.translateXProperty(), 0))
        );
        shakeTimeline.play();
    }

    // Helper methods for showing/hiding messages
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
        successLabel.setVisible(false);
        successLabel.setManaged(false);
    }

    private void hideError() {
        errorLabel.setText("");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    private void showSuccess(String message) {
        successLabel.setText(message);
        successLabel.setVisible(true);
        successLabel.setManaged(true);
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    private void hideSuccess() {
        successLabel.setText("");
        successLabel.setVisible(false);
        successLabel.setManaged(false);
    }

    private void showUsernameError(String message) {
        usernameErrorLabel.setText(message);
        usernameErrorLabel.setVisible(true);
        usernameErrorLabel.setManaged(true);
        usernameField.getStyleClass().add("error");
    }

    private void hideUsernameError() {
        usernameErrorLabel.setText("");
        usernameErrorLabel.setVisible(false);
        usernameErrorLabel.setManaged(false);
        usernameField.getStyleClass().remove("error");
    }

    private void showPasswordError(String message) {
        passwordErrorLabel.setText(message);
        passwordErrorLabel.setVisible(true);
        passwordErrorLabel.setManaged(true);
    }

    private void hidePasswordError() {
        passwordErrorLabel.setText("");
        passwordErrorLabel.setVisible(false);
        passwordErrorLabel.setManaged(false);
        newPasswordField.getStyleClass().remove("error");
        confirmPasswordField.getStyleClass().remove("error");
    }
}

