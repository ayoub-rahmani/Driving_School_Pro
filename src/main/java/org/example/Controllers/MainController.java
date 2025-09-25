package org.example.Controllers;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.effect.Light;
import javafx.scene.effect.Lighting;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.shape.*;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.input.KeyEvent;
import javafx.util.Duration;
import javafx.geometry.Pos;

import org.example.Entities.DrivingSchoolInfo;
import org.example.Entities.User;
import org.example.Services.DrivingSchoolService;
import org.example.Utils.AccessControl;
import org.example.Utils.NotificationManager;
import org.example.Utils.SessionManager;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalTime;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    // UI Components from first controller
    @FXML private BorderPane mainBorderPane;
    @FXML private Label userNameLabel;
    @FXML private Label userRoleLabel;
    @FXML private Button settingsButton;
    @FXML private Button logoutButton;

    // UI Components from second controller
    @FXML private Button candidatsBtn;
    @FXML private ImageView logoImageView;
    @FXML private Button moniteursBtn;
    @FXML private Button PaiementBtn;
    @FXML private Button vehiculesBtn;
    @FXML private Button examensBtn,settingsMenuBtn;
    @FXML private Button drivingSchoolBtn;

    @FXML private Label statusLabel;
    @FXML private Button settingsBtn;
    @FXML private Button helpBtn;
    @FXML private Button usersBtn;
    @FXML private Button logsBtn;
    @FXML private Button rapportBtn,SessionBtn;
    @FXML private Label headerLabel;

    // Content area for loading views
    @FXML private StackPane contentArea;
    @FXML private Label currentViewTitle;

    private User currentUser;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Get current user from session
        currentUser = SessionManager.getCurrentUser();

        // Set user information in the UI
        if (currentUser != null) {
            if (userNameLabel != null) {
                String displayName = currentUser.getFullName() != null && !currentUser.getFullName().isEmpty()
                        ? currentUser.getFullName()
                        : currentUser.getUsername();
                userNameLabel.setText(displayName);
            }

            if (userRoleLabel != null && currentUser.getRole() != null) {
                userRoleLabel.setText(currentUser.getRole());
            }
        }

        loadDrivingSchoolInfo();
        applyRoleBasedAccess();
        initializeKeyboardShortcuts();
        setupButtonAnimations();
        showWelcomeNotification();
    }

    /**
     * Set up hover animations for all navigation buttons
     */
    private void setupButtonAnimations() {
        // Apply hover animations to all navigation buttons
        setupButtonHoverEffect(candidatsBtn);
        setupButtonHoverEffect(moniteursBtn);
        setupButtonHoverEffect(PaiementBtn);
        setupButtonHoverEffect(vehiculesBtn);
        setupButtonHoverEffect(examensBtn);
        setupButtonHoverEffect(drivingSchoolBtn);
        setupButtonHoverEffect(settingsBtn);
        setupButtonHoverEffect(helpBtn);
        setupButtonHoverEffect(usersBtn);
        setupButtonHoverEffect(logsBtn);
        setupButtonHoverEffect(rapportBtn);
        setupButtonHoverEffect(logoutButton);
        setupButtonHoverEffect(SessionBtn);
        setupButtonHoverEffect(settingsMenuBtn);
    }

    /**
     * Apply hover animation effect to a button
     */
    private void setupButtonHoverEffect(Button button) {
        if (button == null) return;

        // Create a drop shadow effect for hover
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 150, 200, 0.5));
        shadow.setRadius(10);

        // Store the original style
        String originalStyle = button.getStyle();
        String hoverStyle = originalStyle + "-fx-background-color: linear-gradient(to bottom, #3498db, #2980b9); -fx-text-fill: white;";

        // Add hover effect
        button.addEventHandler(MouseEvent.MOUSE_ENTERED, e -> {
            // Scale up animation
            ScaleTransition scaleUp = new ScaleTransition(Duration.millis(200), button);
            scaleUp.setToX(1.05);
            scaleUp.setToY(1.05);
            scaleUp.play();

            // Apply shadow effect
            button.setEffect(shadow);

            // Change style
            button.setStyle(hoverStyle);
        });

        // Remove hover effect
        button.addEventHandler(MouseEvent.MOUSE_EXITED, e -> {
            // Scale down animation
            ScaleTransition scaleDown = new ScaleTransition(Duration.millis(200), button);
            scaleDown.setToX(1.0);
            scaleDown.setToY(1.0);
            scaleDown.play();

            // Remove shadow effect
            button.setEffect(null);

            // Restore original style
            button.setStyle(originalStyle);
        });

        // Add click effect
        button.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> {
            // Scale down more on click
            ScaleTransition scaleClick = new ScaleTransition(Duration.millis(100), button);
            scaleClick.setToX(0.95);
            scaleClick.setToY(0.95);
            scaleClick.play();
        });

        // Restore after click
        button.addEventHandler(MouseEvent.MOUSE_RELEASED, e -> {
            // Scale back after click
            ScaleTransition scaleRelease = new ScaleTransition(Duration.millis(100), button);
            scaleRelease.setToX(1.05);
            scaleRelease.setToY(1.05);
            scaleRelease.play();
        });
    }

    private void applyRoleBasedAccess() {
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) return;

        // Set visibility based on role
        if (usersBtn != null) {
            AccessControl.setupButtonAccess(usersBtn, currentUser.canManageUsers());
        }

        if (logsBtn != null) {
            AccessControl.setupButtonAccess(logsBtn, currentUser.canAccessLogs());
        }

        if (rapportBtn != null) {
            AccessControl.setupButtonAccess(rapportBtn, currentUser.canAccessStatistics());
        }

        if (PaiementBtn != null) {
            AccessControl.setupButtonAccess(PaiementBtn, currentUser.canAccessPayments());
        }
    }

    // User Management button handler
    @FXML
    private void handleUsers(ActionEvent event) {
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser == null || !currentUser.canManageUsers()) {
            AccessControl.showAccessDeniedAlert(
                    "Vous n'avez pas la permission de gérer les utilisateurs. Cette action est réservée aux administrateurs.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/IHM/UserManagementIHM.fxml"));
            Parent root = loader.load();

            // Apply fade transition
            fadeSceneTransition((Node) event.getSource(), root, "Gestion des Utilisateurs");
        } catch (IOException e) {
            e.printStackTrace();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            NotificationManager.showError(stage, "Erreur de navigation",
                    "Erreur lors de l'ouverture de la gestion des utilisateurs");
        }
    }

    // Logs button handler
    @FXML
    private void handleLogs(ActionEvent event) {
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser == null || !currentUser.canAccessLogs()) {
            AccessControl.showAccessDeniedAlert(
                    "Vous n'avez pas la permission d'accéder aux logs. Cette action est réservée aux administrateurs.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/IHM/LogsIHM.fxml"));
            Parent root = loader.load();

            // Apply fade transition
            fadeSceneTransition((Node) event.getSource(), root, "Logs d'activite");
        } catch (IOException e) {
            e.printStackTrace();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            NotificationManager.showError(stage, "Erreur",
                    "Erreur lors de l'ouverture des logs: " + e.getMessage());
        }
    }


    /**
     * Sets up keyboard shortcuts for the main interface
     * This method should be called in the initialize method
     */
    private void setupKeyboardShortcuts() {
        Platform.runLater(() -> {
            // Get the scene from any component
            Scene scene = null;
            if (mainBorderPane != null && mainBorderPane.getScene() != null) {
                scene = mainBorderPane.getScene();
            } else if (contentArea != null && contentArea.getScene() != null) {
                scene = contentArea.getScene();
            } else if (candidatsBtn != null && candidatsBtn.getScene() != null) {
                scene = candidatsBtn.getScene();
            }

            if (scene != null) {
                scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                    // F1 for Help
                    if (event.getCode() == KeyCode.F1) {
                        event.consume(); // Prevent the event from being processed further
                        handleHelp(new ActionEvent(helpBtn, null));
                    }
                    // F2 for settings
                    else if (event.getCode() == KeyCode.F2) {
                        handleSettings(new ActionEvent(settingsBtn, null));
                        event.consume();
                    }
                    // Ctrl+L for Logout
                    else if (event.getCode() == KeyCode.L && event.isControlDown()) {
                        event.consume();
                        handleLogout(new ActionEvent(logoutButton, null));
                    }
                    // Keyboard shortcuts for navigation with Alt key
                    else if (event.isAltDown()) {
                        switch (event.getCode()) {
                            case C: // Alt+C for Candidats
                                event.consume();
                                handleCandidats(new ActionEvent(candidatsBtn, null));
                                break;
                            case M: // Alt+M for Moniteurs
                                event.consume();
                                handleMoniteurs(new ActionEvent(moniteursBtn, null));
                                break;
                            case E: // Alt+E for Examens
                                event.consume();
                                handleExamens(new ActionEvent(examensBtn, null));
                                break;
                            case P: // Alt+P for Paiement
                                event.consume();
                                handlePaiement(new ActionEvent(PaiementBtn, null));
                                break;
                            case V: // Alt+V for Vehicules
                                event.consume();
                                handleVehicules(new ActionEvent(vehiculesBtn, null));
                                break;
                            case D: // Alt+D for Driving School
                                event.consume();
                                handleDrivingSchool(new ActionEvent(drivingSchoolBtn, null));
                                break;
                            default:
                                break;
                        }
                    }
                });
            }
        });
    }

    /**
     * This method should be called after the scene is set
     * For example, in the start method of the Application class or after loading the FXML
     */
    public void initializeKeyboardShortcuts() {
        // We need to wait for the scene to be fully initialized
        Platform.runLater(this::setupKeyboardShortcuts);
    }

    private void loadDrivingSchoolInfo() {
        try {
            // Créer une instance du service
            DrivingSchoolService drivingSchoolService = new DrivingSchoolService();

            // Récupérer les informations de l'auto-école
            DrivingSchoolInfo schoolInfo = drivingSchoolService.getDrivingSchool();

            // Mettre à jour le nom de l'auto-école dans l'en-tête
            if (headerLabel != null && schoolInfo.getName() != null && !schoolInfo.getName().isEmpty()) {
                headerLabel.setText(schoolInfo.getName());
            }

            // Mettre à jour le logo de l'auto-école
            if (logoImageView != null && schoolInfo.getLogoPath() != null && !schoolInfo.getLogoPath().isEmpty()) {
                try {
                    File logoFile = new File(schoolInfo.getLogoPath());
                    if (logoFile.exists()) {
                        javafx.scene.image.Image logoImage = new javafx.scene.image.Image(logoFile.toURI().toString());
                        logoImageView.setImage(logoImage);
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

    /**
     * Show a welcome notification when the main interface loads
     */
    public void showWelcomeNotification() {
        Stage stage = null;

        // Try to get stage from different possible root elements
        if (mainBorderPane != null && mainBorderPane.getScene() != null) {
            stage = (Stage) mainBorderPane.getScene().getWindow();
        } else if (contentArea != null && contentArea.getScene() != null) {
            stage = (Stage) contentArea.getScene().getWindow();
        } else if (candidatsBtn != null && candidatsBtn.getScene() != null) {
            stage = (Stage) candidatsBtn.getScene().getWindow();
        }

        if (stage != null) {
            // Get time-based greeting
            String greeting = getTimeBasedGreeting();

            // Get user name
            String userName = currentUser != null && currentUser.getFullName() != null && !currentUser.getFullName().isEmpty()
                    ? currentUser.getFullName()
                    : (currentUser != null ? currentUser.getUsername() : "");

            // Show welcome notification
            NotificationManager.showSuccess(stage,
                    greeting + (userName.isEmpty() ? "" : ", " + userName),
                    "Bienvenue dans le système de gestion Auto-École.");
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

    /**
     * Event handler for the Candidats button
     */
    @FXML
    public void handleCandidats(ActionEvent event) {
        loadInterface("CandidatIHM.fxml", "Gestion des Candidats", event);
    }

    /**
     * Event handler for the Moniteurs button
     */
    @FXML
    public void handleMoniteurs(ActionEvent event) {
        loadInterface("MoniteurIHM.fxml", "Gestion des Moniteurs", event);
    }

    @FXML
    public void handlePaiement(ActionEvent event) {
        loadInterface("PaiementIHM.fxml", "Gestion des Paiements", event);
    }

    /**
     * Event handler for the Examens button
     */
    @FXML
    public void handleExamens(ActionEvent event) {
        loadInterface("ExamenIHM.fxml", "Gestion des Examens", event);
    }

    /**
     * Event handler for the Vehicules button
     */
    @FXML
    public void handleVehicules(ActionEvent event) {
        try {
            // Load the Vehicule.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/IHM/Vehicule.fxml"));
            Parent root = loader.load();

            // Get the controller
            VehiculeController controller = loader.getController();

            // If we have a content area, use it, otherwise load as a new scene
            if (contentArea != null) {
                // Use fade transition for content area
                fadeContentTransition(root, "Gestion des Véhicules", (Node) event.getSource());

                // Add a delay to ensure the UI is fully loaded and visible
                PauseTransition delay = new PauseTransition(Duration.seconds(1.5));
                delay.setOnFinished(e -> {
                    System.out.println("Triggering document check after content transition");
                    controller.forceCheckExpiringDocuments();
                });
                delay.play();
            } else {
                // Load as a new scene with fade transition
                fadeSceneTransition((Node) event.getSource(), root, "Gestion des Véhicules");

                // Add a delay to ensure the UI is fully loaded and visible
                PauseTransition delay = new PauseTransition(Duration.seconds(1.5));
                delay.setOnFinished(e -> {
                    controller.forceCheckExpiringDocuments();
                });
                delay.play();
            }
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement de l'interface: Vehicule.fxml - " + e.getMessage());
            e.printStackTrace();
            showErrorAlert("Erreur de chargement", "Impossible de charger l'interface des véhicules", e.getMessage());
        }
    }

    @FXML
    public void handleSeance(ActionEvent event) {
        loadInterface("Seance.fxml", "Gestion des Seances", event);
    }

    @FXML
    public void handleDashboard(ActionEvent event) {
        loadInterface("DashboardIHM.fxml", "Rapports et Statistiques", event);
    }

    /**
     * Event handler for the Settings button
     */
    @FXML
    public void handleSettings(ActionEvent event) {
        try {
            // Load the settings view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/IHM/SettingsIHM.fxml"));
            Parent settingsRoot = loader.load();

            // Get the controller
            SettingsController settingsController = loader.getController();

            // Get the current stage
            Stage currentStage = null;
            if (mainBorderPane != null && mainBorderPane.getScene() != null) {
                currentStage = (Stage) mainBorderPane.getScene().getWindow();
            } else if (event.getSource() instanceof Node) {
                currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            }

            if (currentStage == null) {
                throw new IllegalStateException("Cannot find current stage");
            }

            // Create a new stage for settings
            Stage settingsStage = new Stage();
            settingsStage.setTitle("Paramètres");
            settingsStage.initModality(Modality.WINDOW_MODAL);
            settingsStage.initOwner(currentStage);

            // Set the main stage reference in the settings controller
            settingsController.setMainStage(currentStage);

            // Create scene and set it on the stage
            Scene settingsScene = new Scene(settingsRoot);

            // Apply current theme
            settingsStage.setScene(settingsScene);
            settingsStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            // Show error notification
            Stage stage = null;
            if (mainBorderPane != null && mainBorderPane.getScene() != null) {
                stage = (Stage) mainBorderPane.getScene().getWindow();
            } else if (event.getSource() instanceof Node) {
                stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            }

            if (stage != null) {
                NotificationManager.showError(stage, "Erreur",
                        "Impossible de charger les paramètres: " + e.getMessage());
            }
        }
    }

    /**
     * Event handler for the Logout button
     */
    @FXML
    public void handleLogout(ActionEvent event) {
        try {
            // Clear current user session
            SessionManager.clearSession();

            // Get the current stage
            Stage currentStage = null;
            if (mainBorderPane != null && mainBorderPane.getScene() != null) {
                currentStage = (Stage) mainBorderPane.getScene().getWindow();
            } else if (event.getSource() instanceof Node) {
                currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            }

            if (currentStage == null) {
                throw new IllegalStateException("Cannot find current stage");
            }

            // Load login screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/IHM/LoginIHM.fxml"));
            Parent root = loader.load();

            // Apply fade transition for logout
            fadeSceneTransition((Node) event.getSource(), root, "Auto-École - Login");
        } catch (IOException e) {
            e.printStackTrace();
            // Show error notification
            Stage stage = null;
            if (mainBorderPane != null && mainBorderPane.getScene() != null) {
                stage = (Stage) mainBorderPane.getScene().getWindow();
            } else if (event.getSource() instanceof Node) {
                stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            }

            if (stage != null) {
                NotificationManager.showError(stage, "Erreur",
                        "Impossible de charger l'écran de connexion: " + e.getMessage());
            }
        }
    }

    /**
     * Event handler for the Help button
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
            if (mainBorderPane != null && mainBorderPane.getScene() != null) {
                currentStage = (Stage) mainBorderPane.getScene().getWindow();
            } else if (event.getSource() instanceof Node) {
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
            if (mainBorderPane != null && mainBorderPane.getScene() != null) {
                stage = (Stage) mainBorderPane.getScene().getWindow();
            } else if (event.getSource() instanceof Node) {
                stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            }

            if (stage != null) {
                NotificationManager.showError(stage, "Erreur",
                        "Impossible de charger l'aide: " + e.getMessage());
            }
        }
    }

    /**
     * Helper method to load interfaces with car animation
     */
    private void loadInterface(String fxmlFile, String title, ActionEvent event) {
        try {
            // Get the FXML
            URL fxmlLocation = getClass().getResource("/IHM/" + fxmlFile);

            if (fxmlLocation == null) {
                System.err.println("The specified fxml doesn't match to the real one");
                throw new IOException("FXML file not found: " + fxmlFile);
            }

            // Load the file
            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent root = loader.load();

            // Get the controller
            Object controller = loader.getController();

            // If we have a content area, use it, otherwise load as a new scene
            if (contentArea != null) {
                // Use fade transition for content area
                fadeContentTransition(root, title, (Node) event.getSource());
            } else {
                // Load as a new scene with fade transition
                fadeSceneTransition((Node) event.getSource(), root, title);
            }

            // Special handling for Vehicule.fxml
            if (fxmlFile.equals("Vehicule.fxml") && controller instanceof VehiculeController) {
                // Add a delay to ensure the UI is fully loaded and visible
                PauseTransition delay = new PauseTransition(Duration.seconds(1.5));
                delay.setOnFinished(e -> {
                    System.out.println("Triggering document check after animation");
                    ((VehiculeController) controller).forceCheckExpiringDocuments();
                });
                delay.play();
            }
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement de l'interface: " + fxmlFile + " - " + e.getMessage());
            e.printStackTrace();
            showErrorAlert("Erreur de chargement", "Impossible de charger l'interface", e.getMessage());
        }
    }

    /**
     * Performs a smooth fade transition for content area
     * Replaces the car animation with a cleaner fade effect
     */
    private void fadeContentTransition(Parent newContent, String title, Node sourceButton) {
        // Create a snapshot of the current content
        Node currentContent = contentArea.getChildren().isEmpty() ? null : contentArea.getChildren().get(0);

        // Create a semi-transparent black overlay for transition
        StackPane fadeOverlay = new StackPane();
        fadeOverlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.85);");
        fadeOverlay.setOpacity(0);

        // Create loading text
        Label loadingLabel = new Label("Chargement de " + title + "...");
        loadingLabel.setStyle("-fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold;");

        // Add content to overlay
        fadeOverlay.getChildren().add(loadingLabel);

        // Add the overlay to the content area
        contentArea.getChildren().add(fadeOverlay);

        // Apply blur to current content if it exists
        if (currentContent != null) {
            GaussianBlur blur = new GaussianBlur(0);
            currentContent.setEffect(blur);

            // Fade in blur - smoother with longer duration
            Timeline blurTimeline = new Timeline(
                    new KeyFrame(Duration.ZERO, new KeyValue(blur.radiusProperty(), 0, Interpolator.EASE_OUT)),
                    new KeyFrame(Duration.millis(400), new KeyValue(blur.radiusProperty(), 8, Interpolator.EASE_OUT))
            );
            blurTimeline.play();
        }

        // Set initial opacity for overlay
        fadeOverlay.setOpacity(0);

        // Fade in overlay - smoother with longer duration
        FadeTransition fadeInOverlay = new FadeTransition(Duration.millis(400), fadeOverlay);
        fadeInOverlay.setFromValue(0);
        fadeInOverlay.setToValue(1);
        fadeInOverlay.setInterpolator(Interpolator.EASE_IN);

        // Start fade in animation
        fadeInOverlay.setOnFinished(e -> {
            // Short pause for visual effect
            PauseTransition delay = new PauseTransition(Duration.millis(200));
            delay.setOnFinished(event -> {
                // Clear the content area
                contentArea.getChildren().clear();

                // Add the new content
                contentArea.getChildren().add(newContent);
                newContent.setOpacity(0);

                // Update the title if available
                if (currentViewTitle != null) {
                    currentViewTitle.setText(title);
                }

                // Highlight the active sidebar item
                setActiveSidebarItem(sourceButton);

                // Fade in the new content - smoother with longer duration
                FadeTransition fadeInContent = new FadeTransition(Duration.millis(400), newContent);
                fadeInContent.setFromValue(0);
                fadeInContent.setToValue(1);
                fadeInContent.setInterpolator(Interpolator.EASE_OUT);
                fadeInContent.play();
            });
            delay.play();
        });

        fadeInOverlay.play();
    }
    /**
     * Performs a smooth fade transition for full scene change
     * Replaces the car animation with a cleaner fade effect
     */
    private void fadeSceneTransition(Node sourceNode, Parent newRoot, String title) {
        try {
            // Get the stage
            Stage stage = (Stage) sourceNode.getScene().getWindow();
            Scene currentScene = sourceNode.getScene();

            // Create a semi-transparent black overlay for transition
            StackPane fadeOverlay = new StackPane();
            fadeOverlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.85);");
            fadeOverlay.setOpacity(0);

            // Create loading text
            Label loadingLabel = new Label("Chargement de " + title + "...");
            loadingLabel.setStyle("-fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold;");

            // Add content to overlay
            fadeOverlay.getChildren().add(loadingLabel);

            // Create a container for current scene and overlay
            StackPane transitionRoot = new StackPane();
            transitionRoot.getChildren().addAll(currentScene.getRoot(), fadeOverlay);

            // Set initial opacity for overlay
            fadeOverlay.setOpacity(0);

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
                PauseTransition delay = new PauseTransition(Duration.millis(200));
                delay.setOnFinished(event -> {
                    // Prepare the new scene with black background to avoid white flash
                    StackPane rootWrapper = new StackPane(newRoot);
                    rootWrapper.setStyle("-fx-background-color: black;");
                    newRoot.setOpacity(0);
                    Scene newScene = new Scene(rootWrapper, currentScene.getWidth(), currentScene.getHeight());

                    // Set the scene
                    stage.setTitle(title);
                    stage.setScene(newScene);

                    // Fade in the new view - smoother with longer duration
                    FadeTransition fadeInNewView = new FadeTransition(Duration.millis(400), newRoot);
                    fadeInNewView.setFromValue(0);
                    fadeInNewView.setToValue(1);
                    fadeInNewView.setInterpolator(Interpolator.EASE_OUT);

                    // Add a handler for when the fade-in completes
                    fadeInNewView.setOnFinished(evt -> {
                        // If this is the vehicle page, trigger document check
                        if (title.contains("Véhicules")) {
                            // Find the controller and call checkExpiringDocuments
                            for (Node node : rootWrapper.lookupAll("*")) {
                                if (node.getId() != null && node.getId().equals("vehiculeTable")) {
                                    // We found the vehicle table, now get its controller
                                    Object controller = node.getProperties().get("controller");
                                    if (controller instanceof VehiculeController) {
                                        ((VehiculeController) controller).checkExpiringDocuments();
                                    }
                                    break;
                                }
                            }
                        }
                    });

                    fadeInNewView.play();
                });
                delay.play();
            });

            parallelIn.play();
        } catch (Exception e) {
            e.printStackTrace();
            // Fallback to direct scene change if animation fails
            try {
                Stage stage = (Stage) sourceNode.getScene().getWindow();
                stage.setTitle(title);
                stage.setScene(new Scene(newRoot));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Animate car transition for content area
     */
    private void animateCarContentTransition(Parent newContent, String title, Node sourceButton) {
        // Create a snapshot of the current content
        Node currentContent = contentArea.getChildren().isEmpty() ? null : contentArea.getChildren().get(0);

        // Create a dark overlay for transition
        StackPane loadingOverlay = new StackPane();
        loadingOverlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.8);");

        // Create a fixed road
        Pane roadContainer = new Pane();
        roadContainer.setPrefWidth(900);
        roadContainer.setPrefHeight(200);
        roadContainer.setMaxWidth(900);
        roadContainer.setMaxHeight(200);
        roadContainer.setStyle("-fx-background-color: #444444;"); // Dark asphalt

        // Road markings
        Rectangle centerLine1 = new Rectangle(100, 95, 80, 10);
        centerLine1.setFill(Color.YELLOW);
        Rectangle centerLine2 = new Rectangle(300, 95, 80, 10);
        centerLine2.setFill(Color.YELLOW);
        Rectangle centerLine3 = new Rectangle(500, 95, 80, 10);
        centerLine3.setFill(Color.YELLOW);
        Rectangle centerLine4 = new Rectangle(700, 95, 80, 10);
        centerLine4.setFill(Color.YELLOW);

        // Edge lines
        Line topLine = new Line(0, 20, 900, 20);
        topLine.setStroke(Color.WHITE);
        topLine.setStrokeWidth(5);

        Line bottomLine = new Line(0, 180, 900, 180);
        bottomLine.setStroke(Color.WHITE);
        bottomLine.setStrokeWidth(5);

        roadContainer.getChildren().addAll(topLine, bottomLine, centerLine1, centerLine2, centerLine3, centerLine4);

        // Create a professional car using a Group
        Group carGroup = new Group();

        // Car body - main shape
        Rectangle carBody = new Rectangle(0, 0, 180, 40);
        carBody.setArcWidth(20);
        carBody.setArcHeight(20);

        // Create a gradient for the car body
        Stop[] stops = new Stop[] {
                new Stop(0, Color.rgb(220, 20, 60)), // Crimson red at top
                new Stop(0.5, Color.rgb(178, 34, 34)), // Firebrick in middle
                new Stop(1, Color.rgb(139, 0, 0))  // Dark red at bottom
        };
        LinearGradient gradient = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE, stops);
        carBody.setFill(gradient);

        // Add a reflection effect to the car body
        Lighting lighting = new Lighting();
        lighting.setDiffuseConstant(1.0);
        lighting.setSpecularConstant(0.5);
        lighting.setSpecularExponent(20);
        lighting.setSurfaceScale(5.0);

        Light.Distant light = new Light.Distant();
        light.setAzimuth(-135.0);
        light.setElevation(30.0);
        lighting.setLight(light);

        carBody.setEffect(lighting);

        // Car roof
        Rectangle carRoof = new Rectangle(40, -30, 100, 30);
        carRoof.setArcWidth(40);
        carRoof.setArcHeight(20);
        carRoof.setFill(gradient);
        carRoof.setEffect(lighting);

        // Windows with glass effect
        Rectangle windshield = new Rectangle(45, -28, 30, 26);
        windshield.setArcWidth(10);
        windshield.setArcHeight(10);

        Rectangle rearWindow = new Rectangle(105, -28,30, 26);
        rearWindow.setArcWidth(10);
        rearWindow.setArcHeight(10);

        // Glass effect
        Stop[] glassStops = new Stop[] {
                new Stop(0, Color.rgb(173, 216, 230, 0.8)), // Light blue
                new Stop(1, Color.rgb(135, 206, 235, 0.9))  // Sky blue
        };
        LinearGradient glassGradient = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE, glassStops);

        windshield.setFill(glassGradient);
        rearWindow.setFill(glassGradient);

        // Add reflection to glass
        Lighting glassLighting = new Lighting();
        glassLighting.setDiffuseConstant(1.5);
        glassLighting.setSpecularConstant(0.7);
        glassLighting.setSpecularExponent(20);
        glassLighting.setSurfaceScale(3.0);
        glassLighting.setLight(light);

        windshield.setEffect(glassLighting);
        rearWindow.setEffect(glassLighting);

        // Wheels - more detailed with rims
        Group frontWheelGroup = createDetailedWheel(30, 40, 20);
        Group rearWheelGroup = createDetailedWheel(140, 40, 20);

        // Headlights
        Circle headlight = new Circle(10, 15, 8);
        Stop[] headlightStops = new Stop[] {
                new Stop(0, Color.WHITE),
                new Stop(1, Color.YELLOW)
        };
        RadialGradient headlightGradient = new RadialGradient(0, 0, 0.5, 0.5, 0.5, true, CycleMethod.NO_CYCLE, headlightStops);
        headlight.setFill(headlightGradient);

        // Taillights
        Rectangle taillight = new Rectangle(170, 10, 8, 15);
        taillight.setFill(Color.RED);
        taillight.setArcWidth(5);
        taillight.setArcHeight(5);

        // Add all parts to the car group
        carGroup.getChildren().addAll(
                carBody, carRoof, windshield, rearWindow,
                frontWheelGroup, rearWheelGroup,
                headlight, taillight
        );

        // Position the car initially at the start of the road
        carGroup.setTranslateX(20);
        carGroup.setTranslateY(80);

        // Add car to the road container
        roadContainer.getChildren().add(carGroup);

        // Create loading text
        Label loadingLabel = new Label("Chargement de " + title + "...");
        loadingLabel.setStyle("-fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold;");

        // Create a VBox to hold the road container and text
        VBox contentBox = new VBox(20);
        contentBox.setAlignment(Pos.CENTER);
        contentBox.getChildren().addAll(loadingLabel, roadContainer);

        // Add content to overlay
        loadingOverlay.getChildren().add(contentBox);

        // Add the overlay to the content area
        contentArea.getChildren().add(loadingOverlay);

        // Apply blur to current content if it exists
        if (currentContent != null) {
            GaussianBlur blur = new GaussianBlur(0);
            currentContent.setEffect(blur);

            // Fade in blur
            Timeline blurTimeline = new Timeline(
                    new KeyFrame(Duration.ZERO, new KeyValue(blur.radiusProperty(), 0, Interpolator.EASE_OUT)),
                    new KeyFrame(Duration.millis(300), new KeyValue(blur.radiusProperty(), 10, Interpolator.EASE_OUT))
            );
            blurTimeline.play();
        }

        // Set initial opacity for overlay
        loadingOverlay.setOpacity(0);

        // Fade in overlay
        FadeTransition fadeInOverlay = new FadeTransition(Duration.millis(300), loadingOverlay);
        fadeInOverlay.setFromValue(0);
        fadeInOverlay.setToValue(1);

        // ANIMATIONS

        // 1. Car movement animation
        TranslateTransition carMove = new TranslateTransition(Duration.seconds(1.8), carGroup);
        carMove.setFromX(20);
        carMove.setToX(700);
        carMove.setCycleCount(1);
        carMove.setInterpolator(Interpolator.EASE_BOTH);

        // 2. Wheel rotation animations
        Node frontWheel = ((Group)frontWheelGroup).getChildren().get(0);
        Node rearWheel = ((Group)rearWheelGroup).getChildren().get(0);

        RotateTransition frontWheelRotate = new RotateTransition(Duration.seconds(0.4), frontWheel);
        frontWheelRotate.setByAngle(360);
        frontWheelRotate.setCycleCount(RotateTransition.INDEFINITE);
        frontWheelRotate.setInterpolator(Interpolator.LINEAR);

        RotateTransition rearWheelRotate = new RotateTransition(Duration.seconds(0.4), rearWheel);
        rearWheelRotate.setByAngle(360);
        rearWheelRotate.setCycleCount(RotateTransition.INDEFINITE);
        rearWheelRotate.setInterpolator(Interpolator.LINEAR);

        // Start fade in animation
        fadeInOverlay.setOnFinished(e -> {
            // Start the car and wheel animations
            frontWheelRotate.play();
            rearWheelRotate.play();
            carMove.play();

            // After a short delay, update the content
            PauseTransition delay = new PauseTransition(Duration.seconds(1.2));
            delay.setOnFinished(event -> {
                // Clear the content area
                contentArea.getChildren().clear();

                // Add the new content
                contentArea.getChildren().add(newContent);
                newContent.setOpacity(0);

                // Update the title if available
                if (currentViewTitle != null) {
                    currentViewTitle.setText(title);
                }

                // Highlight the active sidebar item
                setActiveSidebarItem(sourceButton);

                // Fade in the new content
                FadeTransition fadeInContent = new FadeTransition(Duration.millis(300), newContent);
                fadeInContent.setFromValue(0);
                fadeInContent.setToValue(1);
                fadeInContent.play();
            });
            delay.play();
        });

        fadeInOverlay.play();
    }

    /**
     * Animate car transition for full scene change
     */
    private void animateCarTransition(Node sourceNode, Parent newRoot, String title) {
        try {
            // Get the stage
            Stage stage = (Stage) sourceNode.getScene().getWindow();
            Scene currentScene = sourceNode.getScene();

            // Create a dark overlay for transition
            StackPane loadingOverlay = new StackPane();
            loadingOverlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.8);");

            // Create a fixed road
            Pane roadContainer = new Pane();
            roadContainer.setPrefWidth(900);
            roadContainer.setPrefHeight(200);
            roadContainer.setMaxWidth(900);
            roadContainer.setMaxHeight(200);
            roadContainer.setStyle("-fx-background-color: #444444;"); // Dark asphalt

            // Road markings
            Rectangle centerLine1 = new Rectangle(100, 95, 80, 10);
            centerLine1.setFill(Color.YELLOW);
            Rectangle centerLine2 = new Rectangle(300, 95, 80, 10);
            centerLine2.setFill(Color.YELLOW);
            Rectangle centerLine3 = new Rectangle(500, 95, 80, 10);
            centerLine3.setFill(Color.YELLOW);
            Rectangle centerLine4 = new Rectangle(700, 95, 80, 10);
            centerLine4.setFill(Color.YELLOW);

            // Edge lines
            Line topLine = new Line(0, 20, 900, 20);
            topLine.setStroke(Color.WHITE);
            topLine.setStrokeWidth(5);

            Line bottomLine = new Line(0, 180, 900, 180);
            bottomLine.setStroke(Color.WHITE);
            bottomLine.setStrokeWidth(5);

            roadContainer.getChildren().addAll(topLine, bottomLine, centerLine1, centerLine2, centerLine3, centerLine4);

            // Create a professional car using a Group
            Group carGroup = new Group();

            // Car body - main shape
            Rectangle carBody = new Rectangle(0, 0, 180, 40);
            carBody.setArcWidth(20);
            carBody.setArcHeight(20);

            // Create a gradient for the car body
            Stop[] stops = new Stop[] {
                    new Stop(0, Color.rgb(220, 20, 60)), // Crimson red at top
                    new Stop(0.5, Color.rgb(178, 34, 34)), // Firebrick in middle
                    new Stop(1, Color.rgb(139, 0, 0))  // Dark red at bottom
            };
            LinearGradient gradient = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE, stops);
            carBody.setFill(gradient);

            // Add a reflection effect to the car body
            Lighting lighting = new Lighting();
            lighting.setDiffuseConstant(1.0);
            lighting.setSpecularConstant(0.5);
            lighting.setSpecularExponent(20);
            lighting.setSurfaceScale(5.0);

            Light.Distant light = new Light.Distant();
            light.setAzimuth(-135.0);
            light.setElevation(30.0);
            lighting.setLight(light);

            carBody.setEffect(lighting);

            // Car roof
            Rectangle carRoof = new Rectangle(40, -30, 100, 30);
            carRoof.setArcWidth(40);
            carRoof.setArcHeight(20);
            carRoof.setFill(gradient);
            carRoof.setEffect(lighting);

            // Windows with glass effect
            Rectangle windshield = new Rectangle(45, -28, 30, 26);
            windshield.setArcWidth(10);
            windshield.setArcHeight(10);

            Rectangle rearWindow = new Rectangle(105, -28, 30, 26);
            rearWindow.setArcWidth(10);
            rearWindow.setArcHeight(10);

            // Glass effect
            Stop[] glassStops = new Stop[] {
                    new Stop(0, Color.rgb(173, 216, 230, 0.8)), // Light blue
                    new Stop(1, Color.rgb(135, 206, 235, 0.9))  // Sky blue
            };
            LinearGradient glassGradient = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE, glassStops);

            windshield.setFill(glassGradient);
            rearWindow.setFill(glassGradient);

            // Add reflection to glass
            Lighting glassLighting = new Lighting();
            glassLighting.setDiffuseConstant(1.5);
            glassLighting.setSpecularConstant(0.7);
            glassLighting.setSpecularExponent(20);
            glassLighting.setSurfaceScale(3.0);
            glassLighting.setLight(light);

            windshield.setEffect(glassLighting);
            rearWindow.setEffect(glassLighting);

            // Wheels - more detailed with rims
            Group frontWheelGroup = createDetailedWheel(30, 40, 20);
            Group rearWheelGroup = createDetailedWheel(140, 40, 20);

            // Headlights
            Circle headlight = new Circle(10, 15, 8);
            Stop[] headlightStops = new Stop[] {
                    new Stop(0, Color.WHITE),
                    new Stop(1, Color.YELLOW)
            };
            RadialGradient headlightGradient = new RadialGradient(0, 0, 0.5, 0.5, 0.5, true, CycleMethod.NO_CYCLE, headlightStops);
            headlight.setFill(headlightGradient);

            // Taillights
            Rectangle taillight = new Rectangle(170, 10, 8, 15);
            taillight.setFill(Color.RED);
            taillight.setArcWidth(5);
            taillight.setArcHeight(5);

            // Add all parts to the car group
            carGroup.getChildren().addAll(
                    carBody, carRoof, windshield, rearWindow,
                    frontWheelGroup, rearWheelGroup,
                    headlight, taillight
            );

            // Position the car initially at the start of the road
            carGroup.setTranslateX(20);
            carGroup.setTranslateY(80);

            // Add car to the road container
            roadContainer.getChildren().add(carGroup);

            // Create loading text
            Label loadingLabel = new Label("Chargement de " + title + "...");
            loadingLabel.setStyle("-fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold;");

            // Create a VBox to hold the road container and text
            VBox contentBox = new VBox(20);
            contentBox.setAlignment(Pos.CENTER);
            contentBox.getChildren().addAll(loadingLabel, roadContainer);

            // Add content to overlay
            loadingOverlay.getChildren().add(contentBox);

            // Create a container for current scene and overlay
            StackPane transitionRoot = new StackPane();
            transitionRoot.getChildren().addAll(currentScene.getRoot(), loadingOverlay);

            // Set initial opacity for overlay
            loadingOverlay.setOpacity(0);

            // Create transition scene
            Scene transitionScene = new Scene(transitionRoot, currentScene.getWidth(), currentScene.getHeight());
            stage.setScene(transitionScene);

            // Apply blur to current scene
            GaussianBlur blur = new GaussianBlur(0);
            currentScene.getRoot().setEffect(blur);

            // ANIMATIONS

            // 1. Car movement animation
            TranslateTransition carMove = new TranslateTransition(Duration.seconds(1.8), carGroup);
            carMove.setFromX(20);
            carMove.setToX(700);
            carMove.setCycleCount(1);
            carMove.setInterpolator(Interpolator.EASE_BOTH);

            // 2. Wheel rotation animations
            Node frontWheel = ((Group)frontWheelGroup).getChildren().get(0);
            Node rearWheel = ((Group)rearWheelGroup).getChildren().get(0);

            RotateTransition frontWheelRotate = new RotateTransition(Duration.seconds(0.4), frontWheel);
            frontWheelRotate.setByAngle(360);
            frontWheelRotate.setCycleCount(RotateTransition.INDEFINITE);
            frontWheelRotate.setInterpolator(Interpolator.LINEAR);

            RotateTransition rearWheelRotate = new RotateTransition(Duration.seconds(0.4), rearWheel);
            rearWheelRotate.setByAngle(360);
            rearWheelRotate.setCycleCount(RotateTransition.INDEFINITE);
            rearWheelRotate.setInterpolator(Interpolator.LINEAR);

            // 3. Fade in overlay and blur background
            FadeTransition fadeInOverlay = new FadeTransition(Duration.millis(300), loadingOverlay);
            fadeInOverlay.setFromValue(0);
            fadeInOverlay.setToValue(1);

            Timeline blurTimeline = new Timeline(
                    new KeyFrame(Duration.ZERO, new KeyValue(blur.radiusProperty(), 0, Interpolator.EASE_OUT)),
                    new KeyFrame(Duration.millis(300), new KeyValue(blur.radiusProperty(), 10, Interpolator.EASE_OUT))
            );

            // Start fade in and blur animations
            ParallelTransition parallelIn = new ParallelTransition(fadeInOverlay, blurTimeline);

            parallelIn.setOnFinished(e -> {
                // Start the car and wheel animations
                frontWheelRotate.play();
                rearWheelRotate.play();
                carMove.play();

                // After a short delay, update the scene
                PauseTransition delay = new PauseTransition(Duration.seconds(1.2));
                delay.setOnFinished(event -> {
                    // Prepare the new scene
                    newRoot.setOpacity(0);
                    Scene newScene = new Scene(newRoot, currentScene.getWidth(), currentScene.getHeight());

                    // Set the scene
                    stage.setTitle(title);
                    stage.setScene(newScene);

                    // Fade in the new view
                    FadeTransition fadeInNewView = new FadeTransition(Duration.millis(300), newRoot);
                    fadeInNewView.setFromValue(0);
                    fadeInNewView.setToValue(1);
                    fadeInNewView.play();
                });
                delay.play();
            });

            parallelIn.play();
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Erreur d'animation", "Impossible d'animer la transition", e.getMessage());

            // Fallback to direct scene change
            try {
                Stage stage = (Stage) sourceNode.getScene().getWindow();
                stage.setTitle(title);
                stage.setScene(new Scene(newRoot));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Helper method to set the active sidebar item
     */
    private void setActiveSidebarItem(Node item) {
        // Clear active class from all sidebar items
        if (candidatsBtn != null) candidatsBtn.getStyleClass().remove("active");
        if (moniteursBtn != null) moniteursBtn.getStyleClass().remove("active");
        if (PaiementBtn != null) PaiementBtn.getStyleClass().remove("active");
        if (vehiculesBtn != null) vehiculesBtn.getStyleClass().remove("active");
        if (examensBtn != null) examensBtn.getStyleClass().remove("active");
        if (drivingSchoolBtn != null) drivingSchoolBtn.getStyleClass().remove("active");

        // Add active class to the clicked item
        if (item != null) {
            item.getStyleClass().add("active");
        }
    }

    /**
     * Helper method to show error alerts
     */
    private void showErrorAlert(String title, String header, String content) {
        javafx.application.Platform.runLater(() -> {
            try {
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
                alert.setTitle(title);
                alert.setHeaderText(header);
                alert.setContentText(content);
                alert.showAndWait();
            } catch (Exception e) {
                System.err.println("Error showing alert: " + e.getMessage());
            }
        });
    }

    @FXML
    private void handleDrivingSchool(ActionEvent event) {
        try {
            // Load the Driving School FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/IHM/DrivingSchoolIHM.fxml"));
            Parent drivingSchoolView = loader.load();

            // Get the controller
            DrivingSchoolController drivingSchoolController = loader.getController();

            // If we have a content area, use it, otherwise load as a new scene
            if (contentArea != null) {
                // Use fade transition for content area
                fadeContentTransition(drivingSchoolView, "Configuration de l'École", (Node) event.getSource());
            } else {
                // Load as a new scene with fade transition
                fadeSceneTransition((Node) event.getSource(), drivingSchoolView, "Configuration de l'École");
            }
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Erreur de chargement", "Impossible de charger la configuration de l'école", e.getMessage());
        }
    }

    /**
     * Helper method to create a detailed wheel with spokes
     */
    private Group createDetailedWheel(double centerX, double centerY, double radius) {
        Group wheelGroup = new Group();

        // Main wheel
        Circle wheel = new Circle(centerX, centerY, radius);
        wheel.setFill(Color.BLACK);

        // Wheel rim
        Circle rim = new Circle(centerX, centerY, radius * 0.7);

        // Create a metallic gradient for the rim
        Stop[] rimStops = new Stop[] {
                new Stop(0, Color.rgb(192, 192, 192)), // Silver
                new Stop(0.3, Color.rgb(220, 220, 220)), // Light silver
                new Stop(0.6, Color.rgb(192, 192, 192)), // Silver
                new Stop(1, Color.rgb(169, 169, 169))  // Dark silver
        };
        RadialGradient rimGradient = new RadialGradient(0, 0, centerX, centerY, radius * 0.7,
                false, CycleMethod.NO_CYCLE, rimStops);
        rim.setFill(rimGradient);

        // Create wheel spokes
        Group spokes = new Group();
        for (int i = 0; i < 5; i++) {
            double angle = i * (360.0 / 5);
            double radian = Math.toRadians(angle);

            double startX = centerX;
            double startY = centerY;
            double endX = centerX + (radius * 0.6) * Math.cos(radian);
            double endY = centerY + (radius * 0.6) * Math.sin(radian);

            Line spoke = new Line(startX, startY, endX, endY);
            spoke.setStroke(Color.DARKGRAY);
            spoke.setStrokeWidth(3);
            spokes.getChildren().add(spoke);
        }

        // Center cap
        Circle centerCap = new Circle(centerX, centerY, radius * 0.2);
        centerCap.setFill(Color.SILVER);
        centerCap.setStroke(Color.DARKGRAY);
        centerCap.setStrokeWidth(1);

        // Add all parts to the wheel group
        wheelGroup.getChildren().addAll(wheel, rim, spokes, centerCap);

        return wheelGroup;
    }
}
