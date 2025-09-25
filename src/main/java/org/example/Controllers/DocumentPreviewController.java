package org.example.Controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for the document preview window
 */
public class DocumentPreviewController implements Initializable {

    @FXML private ImageView imagePreview;
    @FXML private Label documentTitleLabel;
    @FXML private Label documentPathLabel;
    @FXML private Button openExternalBtn;
    @FXML private VBox imageContainer;
    @FXML private VBox pdfContainer;
    @FXML private Label errorLabel;
    @FXML private Button closeButton;

    private String documentPath;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize UI components
        pdfContainer.setVisible(false);
        imageContainer.setVisible(false);
        errorLabel.setVisible(false);
    }

    /**
     * Set the document path and load the document
     * @param path The path to the document
     */
    public void setDocumentPath(String path) {
        this.documentPath = path;
        documentPathLabel.setText(path);

        if (path == null || path.isEmpty() || path.equals("Aucun fichier")) {
            showError("Aucun document à afficher");
            return;
        }

        File file = new File(path);
        if (!file.exists()) {
            showError("Le fichier n'existe pas: " + path);
            return;
        }

        // Set document title
        String fileName = file.getName();
        documentTitleLabel.setText("Aperçu: " + fileName);

        String lowerCasePath = path.toLowerCase();
        if (lowerCasePath.endsWith(".jpg") || lowerCasePath.endsWith(".jpeg") ||
                lowerCasePath.endsWith(".png") || lowerCasePath.endsWith(".gif") ||
                lowerCasePath.endsWith(".bmp")) {

            // Display image
            try {
                Image image = new Image(file.toURI().toString());
                imagePreview.setImage(image);
                imageContainer.setVisible(true);
                pdfContainer.setVisible(false);
                errorLabel.setVisible(false);
            } catch (Exception e) {
                showError("Erreur lors du chargement de l'image: " + e.getMessage());
            }
        } else if (lowerCasePath.endsWith(".pdf")) {
            // Show PDF container
            imageContainer.setVisible(false);
            pdfContainer.setVisible(true);
            errorLabel.setVisible(false);

            // Try to open with default application
            try {
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(file);
                }
            } catch (Exception e) {
                showError("Impossible d'ouvrir le document: " + e.getMessage());
            }
        } else {
            // Unsupported file type
            showError("Type de fichier non pris en charge pour l'aperçu");

            // Try to open with default application
            try {
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(file);
                }
            } catch (Exception e) {
                // Already showing an error, no need to update
            }
        }
    }

    /**
     * Show an error message
     * @param message The error message
     */
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        imageContainer.setVisible(false);
        pdfContainer.setVisible(false);
    }

    /**
     * Handle the open external button click
     */
    @FXML
    private void handleOpenExternal() {
        if (documentPath == null || documentPath.isEmpty()) {
            showError("Aucun document à ouvrir");
            return;
        }

        try {
            File file = new File(documentPath);
            if (!file.exists()) {
                showError("Le fichier n'existe pas: " + documentPath);
                return;
            }

            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(file);
            } else {
                showError("Impossible d'ouvrir le fichier avec une application externe");
            }
        } catch (IOException e) {
            showError("Erreur lors de l'ouverture du fichier: " + e.getMessage());
        }
    }

    /**
     * Handle the close button click
     */
    @FXML
    private void handleCloseButton() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
}

