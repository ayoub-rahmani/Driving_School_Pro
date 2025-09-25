package org.example.Controllers;

import javafx.animation.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.BoxBlur;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.Entities.User;
import org.example.Service.UserService;
import org.example.Utils.PreferencesManager;
import org.example.Utils.SessionManager;
import org.example.Utils.Verification;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalTime;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Label errorLabel;
    @FXML private VBox loginForm;
    @FXML private CheckBox rememberMeCheckbox;
    @FXML private BorderPane rootPane;
    @FXML private Hyperlink forgotPasswordLink; // for password recovery
    @FXML private StackPane videoContainer; // Container for video background

    private MediaPlayer mediaPlayer;
    private MediaView mediaView;

    private UserService userService;
    private int loginAttempts = 0;
    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final String ERROR_COLOR = "#ef4444";
    private boolean hasLoginError = false; // Track login errors

    public LoginController() throws SQLException {
        userService = new UserService();
    }

    @FXML
    private void initialize() {
        // Initialize video background
        initializeVideoBackground();

        // Clear any error messages when the form loads
        errorLabel.setText("");

        // Add focus listeners for visual feedback
        setupFieldListeners();

        // Hide forgot password link initially
        if (forgotPasswordLink != null) {
            forgotPasswordLink.setVisible(false);
            forgotPasswordLink.setManaged(false); // Don't take up space when hidden
        }

        // Try auto login
        boolean autoLoginSuccess = SessionManager.tryAutoLogin();
        if (autoLoginSuccess) {
            // We need to use Platform.runLater because we're in initialize
            javafx.application.Platform.runLater(this::loadMainInterface);
        }
    }

    private void initializeVideoBackground() {
        try {
            // Get the video file from resources
            String videoPath = getClass().getResource("/videos/background2.mp4").toExternalForm();

            Media media = new Media(videoPath);
            mediaPlayer = new MediaPlayer(media);
            mediaView = new MediaView(mediaPlayer);

            // IMPORTANT: Set these properties to ensure the video is visible
            mediaView.setPreserveRatio(false);
            mediaView.setSmooth(true);

            // Make sure the MediaView is visible
            mediaView.setOpacity(1.0);
            mediaView.setVisible(true);
            mediaView.setManaged(true);

            // Set video properties
            mediaPlayer.setMute(true);
            mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            mediaPlayer.setAutoPlay(true);

            // Add the MediaView to the videoContainer
            if (videoContainer != null) {
                // Clear any existing children to avoid duplicates
                videoContainer.getChildren().clear();

                // Add the MediaView first, then add the rootPane on top
                videoContainer.getChildren().add(mediaView);

                // If rootPane is not already in the container, add it
                if (!videoContainer.getChildren().contains(rootPane)) {
                    videoContainer.getChildren().add(rootPane);
                }

                // Set explicit size for the MediaView to fill the entire container
                mediaView.fitWidthProperty().bind(videoContainer.widthProperty());
                mediaView.fitHeightProperty().bind(videoContainer.heightProperty());

                // Add debugging listeners

            } else {
                System.err.println("videoContainer is null - cannot add MediaView");
            }
        } catch (Exception e) {
            System.err.println("Error initializing video background: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private void setupFieldListeners() {
        usernameField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                usernameField.getParent().setStyle("-fx-border-color: #0d9488; -fx-border-width: 2px; -fx-border-radius: 4px;");
            } else {
                usernameField.getParent().setStyle("-fx-border-color: #e5e7eb; -fx-border-width: 1px; -fx-border-radius: 4px;");
            }
        });

        passwordField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                passwordField.getParent().setStyle("-fx-border-color: #0d9488; -fx-border-width: 2px; -fx-border-radius: 4px;");
            } else {
                passwordField.getParent().setStyle("-fx-border-color: #e5e7eb; -fx-border-width: 1px; -fx-border-radius: 4px;");
            }
        });

        // Add key event handler for Enter key
        passwordField.setOnKeyPressed(event -> {
            if (event.getCode().toString().equals("ENTER")) {
                loginButton.fire();
            }
        });
        // Add key event handler for Enter key in username field
        usernameField.setOnKeyPressed(event -> {
            if (event.getCode().toString().equals("ENTER")) {
                passwordField.requestFocus();
            }
        });
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        // Clear previous error
        errorLabel.setText("");

        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        // Validate username
        if (!Verification.validateRequired(usernameField, errorLabel)) {
            shakeAnimation(loginForm);
            return;
        }

        // Validate password
        if (!Verification.validatePassword(passwordField, errorLabel, 5)) {
            shakeAnimation(loginForm);
            return;
        }

        // Check if max login attempts reached
        if (loginAttempts >= MAX_LOGIN_ATTEMPTS) {
            errorLabel.setText("Trop de tentatives échouées. Veuillez réessayer plus tard.");
            disableLoginForm(true);

            // Schedule re-enabling after 30 seconds
            PauseTransition pause = new PauseTransition(Duration.seconds(30));
            pause.setOnFinished(e -> {
                disableLoginForm(false);
                loginAttempts = 0;
                errorLabel.setText("Vous pouvez maintenant réessayer.");
            });
            pause.play();
            return;
        }

        // Disable login button and show waiting text
        loginButton.setDisable(true);
        loginButton.setText("Authentification...");

        // Add a pulsing effect to the button
        FadeTransition fadeTransition = new FadeTransition(Duration.seconds(0.7), loginButton);
        fadeTransition.setFromValue(1.0);
        fadeTransition.setToValue(0.7);
        fadeTransition.setCycleCount(Timeline.INDEFINITE);
        fadeTransition.setAutoReverse(true);
        fadeTransition.play();

        // Simulate network delay (remove in production)
        Timeline delay = new Timeline(new KeyFrame(Duration.seconds(1.5), e -> {
            fadeTransition.stop();
            loginButton.setOpacity(1.0); // Reset opacity

            try {
                // Attempt to authenticate the user
                User authenticatedUser = userService.authenticate(username, password);

                if (authenticatedUser != null) {
                    // Reset login attempts on success
                    loginAttempts = 0;
                    hasLoginError = false;

                    // Set current user in session
                    SessionManager.setCurrentUser(authenticatedUser);

                    // Log the login time
                    SessionManager.setLoginTime(System.currentTimeMillis());

                    // If "Remember Me" is checked, save for auto-login
                    if (rememberMeCheckbox != null && rememberMeCheckbox.isSelected()) {
                        SessionManager.saveSessionForAutoLogin();
                    } else {
                        PreferencesManager.disableAutoLogin();
                    }

                    // Authentication successful, show success animation
                    showSuccessAnimation(event, authenticatedUser);
                } else {
                    // Authentication failed
                    loginAttempts++;
                    hasLoginError = true;
                    errorLabel.setText("Nom d'utilisateur ou mot de passe incorrect");

                    // Show forgot password link on error
                    showForgotPasswordLink();

                    shakeAnimation(loginForm);
                    loginButton.setDisable(false);
                    loginButton.setText("Se connecter");

                    // Show remaining attempts
                    if (loginAttempts > 0) {
                        int remaining = MAX_LOGIN_ATTEMPTS - loginAttempts;
                        if (remaining > 0) {
                            errorLabel.setText(errorLabel.getText() + " (" + remaining + " tentatives restantes)");
                        }
                    }
                }
            } catch (SQLException ex) {
                // Database error
                errorLabel.setText("Erreur de connexion à la base de données");
                ex.printStackTrace();
                loginButton.setDisable(false);
                loginButton.setText("Se connecter");
            }
        }));
        delay.play();
    }

    private void disableLoginForm(boolean disable) {
        loginForm.setDisable(disable);
        loginButton.setDisable(disable);

        if (disable) {
            loginForm.setOpacity(0.7);
        } else {
            loginForm.setOpacity(1.0);
        }
    }

    private void shakeAnimation(Node node) {
        TranslateTransition tt = new TranslateTransition(Duration.millis(100), node);
        tt.setFromX(0);
        tt.setByX(10);
        tt.setCycleCount(6);
        tt.setAutoReverse(true);

        // Add a subtle red glow effect during shake
        DropShadow errorGlow = new DropShadow();
        errorGlow.setColor(Color.web(ERROR_COLOR, 0.5));
        errorGlow.setRadius(20);

        // Store original effect
        Object originalEffect = node.getEffect();

        // Apply error effect
        node.setEffect(errorGlow);

        // Reset effect after animation
        tt.setOnFinished(event -> node.setEffect((javafx.scene.effect.Effect) originalEffect));

        tt.playFromStart();
    }

    private void showSuccessAnimation(ActionEvent event, User user) {
        try {
            // Get the current scene and root
            Scene currentScene = ((Node) event.getSource()).getScene();
            Parent currentRoot = currentScene.getRoot();
            Stage stage = (Stage) currentScene.getWindow();

            // Create a container for the entire animation
            StackPane animationContainer = new StackPane();
            animationContainer.getChildren().add(currentRoot);

            // Apply blur effect to current content
            BoxBlur blur = new BoxBlur(0, 0, 3);
            currentRoot.setEffect(blur);

            // Create success overlay with dark semi-transparent background
            StackPane overlay = new StackPane();
            overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7);");
            overlay.setOpacity(0);

            // Create success animation container
            VBox successBox = new VBox(20);
            successBox.setAlignment(Pos.CENTER);
            successBox.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 15; -fx-padding: 30;");
            successBox.setMaxWidth(450);
            successBox.setMaxHeight(400);
            successBox.setEffect(new DropShadow(20, Color.rgb(0, 0, 0, 0.5)));
            successBox.setScaleX(0.8);
            successBox.setScaleY(0.8);

            // Create app logo/branding at the top
            HBox logoContainer = new HBox(10);
            logoContainer.setAlignment(Pos.CENTER);

            // Create or load your app logo
            SVGPath carIcon = new SVGPath();
            carIcon.setContent("M5,11L6.5,6.5H17.5L19,11M17.5,16A1.5,1.5 0 0,1 16,14.5A1.5,1.5 0 0,1 17.5,13A1.5,1.5 0 0,1 19,14.5A1.5,1.5 0 0,1 17.5,16M6.5,16A1.5,1.5 0 0,1 5,14.5A1.5,1.5 0 0,1 6.5,13A1.5,1.5 0 0,1 8,14.5A1.5,1.5 0 0,1 6.5,16M18.92,6C18.72,5.42 18.16,5 17.5,5H6.5C5.84,5 5.28,5.42 5.08,6L3,12V20A1,1 0 0,0 4,21H5A1,1 0 0,0 6,20V19H18V20A1,1 0 0,0 19,21H20A1,1 0 0,0 21,20V12L18.92,6Z");
            carIcon.setFill(Color.rgb(13, 148, 136)); // Teal color
            carIcon.setScaleX(2.0);
            carIcon.setScaleY(2.0);

            Text appName = new Text("Auto-École");
            appName.setFill(Color.rgb(13, 148, 136));
            appName.setFont(Font.font("System", FontWeight.BOLD, 24));

            logoContainer.getChildren().addAll(carIcon, appName);

            // Create success checkmark circle with animation
            StackPane checkmarkContainer = new StackPane();
            checkmarkContainer.setMinHeight(100);

            Circle outerCircle = new Circle(50);
            outerCircle.setFill(Color.rgb(240, 253, 244)); // Light green background

            Circle innerCircle = new Circle(40);
            innerCircle.setFill(Color.rgb(16, 185, 129)); // Green color
            innerCircle.setStroke(Color.WHITE);
            innerCircle.setStrokeWidth(2);
            innerCircle.setEffect(new DropShadow(10, Color.rgb(0, 0, 0, 0.3)));
            innerCircle.setScaleX(0);
            innerCircle.setScaleY(0);

            // Create checkmark icon
            SVGPath checkmark = new SVGPath();
            checkmark.setContent("M9,20.42L2.79,14.21L5.62,11.38L9,14.77L18.88,4.88L21.71,7.71L9,20.42Z");
            checkmark.setFill(Color.WHITE);
            checkmark.setScaleX(0);
            checkmark.setScaleY(0);

            checkmarkContainer.getChildren().addAll(outerCircle, innerCircle, checkmark);

            // Create personalized greeting based on time of day
            String greeting = getTimeBasedGreeting();
            Label greetingLabel = new Label(greeting);
            greetingLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #1f2937;");

            // Create user info with personalized greeting
            String displayName = user.getFullName() != null && !user.getFullName().isEmpty()
                    ? user.getFullName()
                    : user.getUsername();
            Label userLabel = new Label(displayName);
            userLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #10b981;");

            // Create role label with styled appearance
            String role = user.getRole() != null ? user.getRole() : "Utilisateur";
            HBox roleContainer = new HBox(10);
            roleContainer.setAlignment(Pos.CENTER);

            Label roleLabel = new Label("Connecté en tant que " + role);
            roleLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #6b7280; -fx-font-style: italic;");

            SVGPath roleIcon = new SVGPath();
            if (role.equalsIgnoreCase("Administrateur")) {
                roleIcon.setContent("M12,1L3,5V11C3,16.55 6.84,21.74 12,23C17.16,21.74 21,16.55 21,11V5L12,1Z");
            } else {
                roleIcon.setContent("M12,4A4,4 0 0,1 16,8A4,4 0 0,1 12,12A4,4 0 0,1 8,8A4,4 0 0,1 12,4M12,14C16.42,14 20,15.79 20,18V20H4V18C4,15.79 7.58,14 12,14Z");
            }
            roleIcon.setFill(Color.rgb(107, 114, 128));
            roleIcon.setScaleX(0.7);
            roleIcon.setScaleY(0.7);

            roleContainer.getChildren().addAll(roleIcon, roleLabel);

            // Create loading indicator for transition
            ProgressBar progressBar = new ProgressBar(0);
            progressBar.setPrefWidth(300);
            progressBar.setStyle("-fx-accent: #10b981;");

            // Create last login info if available
            HBox lastLoginContainer = new HBox(10);
            lastLoginContainer.setAlignment(Pos.CENTER);
            lastLoginContainer.setVisible(false); // Initially hidden
            lastLoginContainer.setManaged(false);

            long lastLogin = SessionManager.getLastLoginTime();
            if (lastLogin > 0) {
                SVGPath clockIcon = new SVGPath();
                clockIcon.setContent("M12,20A8,8 0 0,0 20,12A8,8 0 0,0 12,4A8,8 0 0,0 4,12A8,8 0 0,0 12,20M12,2A10,10 0 0,1 22,12A10,10 0 0,1 12,22C6.47,22 2,17.5 2,12A10,10 0 0,1 12,2M12.5,7V12.25L17,14.92L16.25,16.15L11,13V7H12.5Z");
                clockIcon.setFill(Color.rgb(107, 114, 128));
                clockIcon.setScaleX(0.7);
                clockIcon.setScaleY(0.7);

                Label lastLoginLabel = new Label("Dernière connexion: " + formatLastLogin(lastLogin));
                lastLoginLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b7280;");

                lastLoginContainer.getChildren().addAll(clockIcon, lastLoginLabel);
                lastLoginContainer.setVisible(true);
                lastLoginContainer.setManaged(true);
            }

            // Add components to success box
            successBox.getChildren().addAll(
                    logoContainer,
                    checkmarkContainer,
                    greetingLabel,
                    userLabel,
                    roleContainer,
                    progressBar
            );

            // Add last login info if available
            if (lastLoginContainer.isVisible()) {
                successBox.getChildren().add(lastLoginContainer);
            }

            // Add success box to overlay
            overlay.getChildren().add(successBox);

            // Add overlay to the animation container
            animationContainer.getChildren().add(overlay);

            // Create a new scene with the animation container
            Scene animationScene = new Scene(animationContainer, currentScene.getWidth(), currentScene.getHeight());
            animationScene.getStylesheets().addAll(currentScene.getStylesheets());
            stage.setScene(animationScene);

            // Create animation sequence

            // 1. Fade in overlay
            FadeTransition fadeInOverlay = new FadeTransition(Duration.seconds(0.5), overlay);
            fadeInOverlay.setFromValue(0);
            fadeInOverlay.setToValue(1);
            fadeInOverlay.setInterpolator(Interpolator.EASE_IN);

            // 2. Blur the background
            Timeline blurTimeline = new Timeline(
                    new KeyFrame(Duration.ZERO, new KeyValue(blur.widthProperty(), 0)),
                    new KeyFrame(Duration.ZERO, new KeyValue(blur.heightProperty(), 0)),
                    new KeyFrame(Duration.seconds(0.5), new KeyValue(blur.widthProperty(), 15, Interpolator.EASE_BOTH)),
                    new KeyFrame(Duration.seconds(0.5), new KeyValue(blur.heightProperty(), 15, Interpolator.EASE_BOTH))
            );

            // 3. Scale up the success box
            ScaleTransition scaleBox = new ScaleTransition(Duration.seconds(0.5), successBox);
            scaleBox.setFromX(0.8);
            scaleBox.setFromY(0.8);
            scaleBox.setToX(1);
            scaleBox.setToY(1);
            scaleBox.setInterpolator(Interpolator.EASE_OUT);

            // 4. Scale and rotate the checkmark
            ScaleTransition scaleInnerCircle = new ScaleTransition(Duration.seconds(0.5), innerCircle);
            scaleInnerCircle.setDelay(Duration.seconds(0.3));
            scaleInnerCircle.setFromX(0);
            scaleInnerCircle.setFromY(0);
            scaleInnerCircle.setToX(1);
            scaleInnerCircle.setToY(1);
            scaleInnerCircle.setInterpolator(Interpolator.EASE_OUT);

            ScaleTransition scaleCheckmark = new ScaleTransition(Duration.seconds(0.5), checkmark);
            scaleCheckmark.setDelay(Duration.seconds(0.6));
            scaleCheckmark.setFromX(0);
            scaleCheckmark.setFromY(0);
            scaleCheckmark.setToX(1);
            scaleCheckmark.setToY(1);
            scaleCheckmark.setInterpolator(Interpolator.EASE_OUT);

            RotateTransition rotateCheckmark = new RotateTransition(Duration.seconds(0.5), checkmark);
            rotateCheckmark.setDelay(Duration.seconds(0.6));
            rotateCheckmark.setFromAngle(-90);
            rotateCheckmark.setToAngle(0);
            rotateCheckmark.setInterpolator(Interpolator.EASE_OUT);

            // 5. Animate the progress bar
            Timeline progressTimeline = new Timeline(
                    new KeyFrame(Duration.ZERO, new KeyValue(progressBar.progressProperty(), 0)),
                    new KeyFrame(Duration.seconds(2), new KeyValue(progressBar.progressProperty(), 1, Interpolator.EASE_BOTH))
            );

            // 6. Pause to show the success message
            PauseTransition pause = new PauseTransition(Duration.seconds(0.5));

            // 7. Fade out the overlay
            FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.5), overlay);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.setInterpolator(Interpolator.EASE_IN);

            // Play animations in sequence
            SequentialTransition sequence = new SequentialTransition(
                    new ParallelTransition(fadeInOverlay, blurTimeline, scaleBox),
                    new ParallelTransition(scaleInnerCircle, scaleCheckmark, rotateCheckmark),
                    progressTimeline,
                    pause,
                    fadeOut
            );

            sequence.setOnFinished(e -> {
                // Stop the video before loading the main interface
                if (mediaPlayer != null) {
                    mediaPlayer.stop();
                    mediaPlayer.dispose();
                }
                loadMainInterface(event);
            });
            sequence.play();

        } catch (Exception e) {
            e.printStackTrace();
            // Stop the video before loading the main interface
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.dispose();
            }
            loadMainInterface(event); // Fallback to direct loading if animation fails
        }
    }

    private String getTimeBasedGreeting() {
        LocalTime currentTime = LocalTime.now();
        int hour = currentTime.getHour();

        if (hour >= 5 && hour < 12) {
            return "Bonjour";
        } else if (hour >= 12 && hour < 18) {
            return "Bon après-midi";
        } else {
            return "Bonsoir";
        }
    }

    private String formatLastLogin(long timestamp) {
        java.util.Date date = new java.util.Date(timestamp);
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy à HH:mm");
        return sdf.format(date);
    }

    private void loadMainInterface(ActionEvent event) {
        try {
            // Stop the video before loading the main interface
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.dispose();
            }

            // Get the FXML for the main interface
            URL fxmlLocation = getClass().getResource("/IHM/MainIHM.fxml");

            if (fxmlLocation == null) {
                System.err.println("The specified fxml doesn't match to the real one");
                throw new IOException("FXML file not found: MainIHM.fxml");
            }

            // Load the file
            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent root = loader.load();

            // Get the controller
            MainController mainController = loader.getController();

            // Create a fade transition for the new scene
            FadeTransition fadeIn = new FadeTransition(Duration.millis(700), root);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.setInterpolator(Interpolator.EASE_IN);

            // Get the Stage from the event
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Set the title and scene
            stage.setTitle("Auto-École Pro");
            Scene scene = new Scene(root);

            // Apply theme from preferences
            stage.setScene(scene);

            // Maximize the window for better user experience
            stage.setMaximized(true);
            stage.show();

            // Play the fade-in animation
            fadeIn.play();

            // Show welcome notification if the controller has the method
            if (mainController != null) {
                fadeIn.setOnFinished(e -> {
                    try {
                        mainController.showWelcomeNotification();
                    } catch (Exception ex) {
                        // Method couldn't be called, that's fine
                        System.err.println("Could not show welcome notification: " + ex.getMessage());
                    }
                });
            }

        } catch (IOException e) {
            System.err.println("Erreur lors du chargement de l'interface principale: " + e.getMessage());
            e.printStackTrace();

            // Use Platform.runLater to show the alert outside of animation context
            javafx.application.Platform.runLater(() -> {
                // Show error alert
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur");
                alert.setHeaderText("Erreur de chargement");
                alert.setContentText("Impossible de charger l'interface principale. Veuillez contacter l'administrateur.");
                alert.showAndWait();
            });
        }
    }

    // Method to handle when no ActionEvent is available (e.g., from auto-login)
    private void loadMainInterface() {
        try {
            // Stop the video before loading the main interface
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.dispose();
            }

            // Get the FXML for the main interface
            URL fxmlLocation = getClass().getResource("/IHM/MainIHM.fxml");

            if (fxmlLocation == null) {
                System.err.println("The specified fxml doesn't match to the real one");
                throw new IOException("FXML file not found: MainIHM.fxml");
            }

            // Load the file
            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent root = loader.load();

            // Get the controller
            MainController mainController = loader.getController();

            // Get the current stage
            Stage stage = (Stage) usernameField.getScene().getWindow();

            // Set the title and scene
            stage.setTitle("Auto-École Pro");
            Scene scene = new Scene(root);
            stage.setScene(scene);

            // Maximize the window for better user experience
            stage.setMaximized(true);
            stage.show();

            // Show welcome notification if the controller has the method
            if (mainController != null) {
                try {
                    mainController.showWelcomeNotification();
                } catch (Exception ex) {
                    // Method couldn't be called, that's fine
                    System.err.println("Could not show welcome notification: " + ex.getMessage());
                }
            }

        } catch (IOException e) {
            System.err.println("Erreur lors du chargement de l'interface principale: " + e.getMessage());
            e.printStackTrace();

            // Show error alert
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Erreur de chargement");
            alert.setContentText("Impossible de charger l'interface principale. Veuillez contacter l'administrateur.");
            alert.showAndWait();
        }
    }

    /**
     * Show the forgot password link when login error occurs
     */
    private void showForgotPasswordLink() {
        if (forgotPasswordLink != null) {
            forgotPasswordLink.setVisible(true);
            forgotPasswordLink.setManaged(true);
        }
    }

    /**
     * Handle the forgot password link click
     */
    @FXML
    private void handleForgotPassword(ActionEvent event) {
        try {
            // Load password recovery screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/IHM/PasswordRecoveryIHM.fxml"));
            Parent root = loader.load();

            // Get current stage
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Set the scene
            currentStage.setTitle("Auto-École - Récupération de mot de passe");
            currentStage.setScene(new Scene(root));
            currentStage.show();
        } catch (IOException e) {
            errorLabel.setText("Erreur lors du chargement de l'écran de récupération");
            e.printStackTrace();
        }
    }
}

