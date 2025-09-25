package org.example.Utils;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.Optional;

public class ConfirmationDialog {

    /**
     * Shows a styled confirmation dialog
     *
     * @param owner The owner window
     * @param title The dialog title
     * @param headerText The header text
     * @param contentText The content text
     * @param type The type of dialog (delete or save)
     * @return true if confirmed, false otherwise
     */
    public static boolean show(Stage owner, String title, String headerText, String contentText, DialogType type) {
        // Create the custom dialog
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.initOwner(owner);
        dialog.initModality(Modality.APPLICATION_MODAL);

        // Set dialog style
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getStyleClass().add("custom-dialog");
        dialogPane.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-width: 1px;");

        // Create header with icon
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(10, 10, 10, 10));

        // Set background color based on type
        String backgroundColor;
        String iconPath;

        if (type == DialogType.DELETE) {
            backgroundColor = "#ffebee"; // Light red for delete
            iconPath = "/images/delete_icon.png"; // Replace with your icon path
        } else {
            backgroundColor = "#e3f2fd"; // Light blue for save
            iconPath = "/images/save_icon.png"; // Replace with your icon path
        }

        header.setStyle("-fx-background-color: " + backgroundColor + ";");

        // Try to load icon, use text fallback if not found
        ImageView icon = null;
        try {
            Image image = new Image(ConfirmationDialog.class.getResourceAsStream(iconPath));
            icon = new ImageView(image);
            icon.setFitHeight(32);
            icon.setFitWidth(32);
        } catch (Exception e) {
            // Fallback icon using text
            Text iconText = new Text(type == DialogType.DELETE ? "⚠" : "💾");
            iconText.setFont(Font.font("System", FontWeight.BOLD, 24));
            iconText.setFill(type == DialogType.DELETE ? Color.RED : Color.BLUE);
            header.getChildren().add(iconText);
        }

        if (icon != null) {
            header.getChildren().add(icon);
        }

        // Header text
        Label headerLabel = new Label(headerText);
        headerLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        header.getChildren().add(headerLabel);

        // Content
        VBox content = new VBox(10);
        content.setPadding(new Insets(20, 10, 10, 10));

        Label contentLabel = new Label(contentText);
        contentLabel.setWrapText(true);
        contentLabel.setMaxWidth(400);
        content.getChildren().add(contentLabel);

        // Combine header and content
        VBox fullContent = new VBox();
        fullContent.getChildren().addAll(header, content);
        dialog.getDialogPane().setContent(fullContent);

        // Create buttons
        ButtonType confirmButtonType;
        ButtonType cancelButtonType = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);

        if (type == DialogType.DELETE) {
            confirmButtonType = new ButtonType("Supprimer", ButtonBar.ButtonData.OK_DONE);
        } else if (type == DialogType.LOGOUT) {
            confirmButtonType = new ButtonType("Déconnecter", ButtonBar.ButtonData.OK_DONE);
        } else if (type == DialogType.CONFIRMER){
            confirmButtonType = new ButtonType("CONFIRMER", ButtonBar.ButtonData.OK_DONE);
        }else {
            confirmButtonType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        }

        dialog.getDialogPane().getButtonTypes().addAll(confirmButtonType, cancelButtonType);

        // Style buttons
        Button confirmButton = (Button) dialog.getDialogPane().lookupButton(confirmButtonType);
        Button cancelButton = (Button) dialog.getDialogPane().lookupButton(cancelButtonType);

        if (type == DialogType.DELETE) {
            confirmButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
        } else if (type == DialogType.LOGOUT) {
            confirmButton.setStyle("-fx-background-color: #ff9800; -fx-text-fill: white;");
        } else if (type == DialogType.CONFIRMER){
            confirmButton.setStyle("-fx-background-color: #2196f3; -fx-text-fill: white;");
        }else{
            confirmButton.setStyle("-fx-background-color: #2196f3; -fx-text-fill: white;");
        }

        cancelButton.setStyle("-fx-background-color: #e0e0e0;");

        // Add hover effect
        confirmButton.setOnMouseEntered(e -> {
            if (type == DialogType.DELETE) {
                confirmButton.setStyle("-fx-background-color: #d32f2f; -fx-text-fill: white;");
            } else if (type == DialogType.LOGOUT) {
                confirmButton.setStyle("-fx-background-color: #f57c00; -fx-text-fill: white;");
            } else if (type == DialogType.CONFIRMER){
                confirmButton.setStyle("-fx-background-color: #1976d2; -fx-text-fill: white;");
            }else{
                confirmButton.setStyle("-fx-background-color: #2196f3; -fx-text-fill: white;");
            }
        });

        confirmButton.setOnMouseExited(e -> {
            if (type == DialogType.DELETE) {
                confirmButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
            } else if (type == DialogType.LOGOUT) {
                confirmButton.setStyle("-fx-background-color: #ff9800; -fx-text-fill: white;");
            } else if (type == DialogType.CONFIRMER){
                confirmButton.setStyle("-fx-background-color: #1976d2; -fx-text-fill: white;");
            }else{
                confirmButton.setStyle("-fx-background-color: #2196f3; -fx-text-fill: white;");
            }
        });

        cancelButton.setOnMouseEntered(e ->
                cancelButton.setStyle("-fx-background-color: #bdbdbd;")
        );

        cancelButton.setOnMouseExited(e ->
                cancelButton.setStyle("-fx-background-color: #e0e0e0;")
        );

        // Show dialog and wait for response
        Optional<ButtonType> result = dialog.showAndWait();

        return result.isPresent() && result.get() == confirmButtonType;
    }

    public enum DialogType {
        DELETE,
        SAVE,
        LOGOUT,
        CONFIRMER
    }
}

