package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.example.Utils.ApplicationInitializer;
import org.example.Utils.DatabaseBackupManager;
import org.example.Utils.DatabaseConnection;
import org.example.Utils.SessionManager;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main extends Application {
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            // Initialize the database backup system
            initializeBackupSystem();

            // Try auto-login first
            boolean autoLoginSuccessful = SessionManager.tryAutoLogin();

            // Determine which FXML to load based on auto-login result
            String fxmlPath = autoLoginSuccessful ? "/IHM/MainIHM.fxml" : "/IHM/LoginIHM.fxml";
            String windowTitle = autoLoginSuccessful ? "Auto-École - Système de Gestion" : "Auto-École - Login";

            // Load the appropriate FXML file
            URL url = getClass().getResource(fxmlPath);
            if (url == null) {
                throw new IllegalArgumentException("FXML file not found: " + fxmlPath);
            }
            // Set application icon
            primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/2.jpg")));

            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();

            Rectangle2D visualBounds = Screen.getPrimary().getVisualBounds();

            // Create a scene with the visual bounds dimensions
            Scene scene = new Scene(root, visualBounds.getWidth(), visualBounds.getHeight());
            primaryStage.setTitle(windowTitle);
            primaryStage.setScene(scene);

            // Position the window to respect the taskbar
            primaryStage.setX(visualBounds.getMinX());
            primaryStage.setY(visualBounds.getMinY());
            primaryStage.setWidth(visualBounds.getWidth());
            primaryStage.setHeight(visualBounds.getHeight());

            primaryStage.setMaximized(true);

            primaryStage.show();

        } catch (IOException e) {
            System.err.println("Erreur lors du chargement de l'interface principale: " + e.getMessage());
            e.printStackTrace();
            showErrorAlert("Erreur de chargement",
                    "Impossible de charger l'interface utilisateur",
                    "Détails: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println("Erreur d'argument: " + e.getMessage());
            e.printStackTrace();
            showErrorAlert("Fichier non trouvé",
                    "Le fichier FXML principal n'a pas été trouvé",
                    "Vérifiez que tous les fichiers sont correctement installés.");
        } catch (Exception e) {
            System.err.println("Erreur inattendue: " + e.getMessage());
            e.printStackTrace();
            showErrorAlert("Erreur inattendue",
                    "Une erreur inattendue s'est produite",
                    "Détails: " + e.getMessage());
        }
    }

    /**
     * Initialize the database backup system
     */
    /**
     * Initialize the backup system
     */
    private void initializeBackupSystem() {
        try {
            // Initialize database connection
            ApplicationInitializer.initialize();
            LOGGER.info("Database backup system initialized successfully");
        } catch (Exception e) {
            LOGGER.severe("Failed to initialize backup system: " + e.getMessage());
        }
    }
    @Override
    public void stop() {
        // Shutdown application components
        ApplicationInitializer.shutdown();
    }

    /**
     * Shows an error alert to the user
     */
    private void showErrorAlert(String title, String header, String content) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}