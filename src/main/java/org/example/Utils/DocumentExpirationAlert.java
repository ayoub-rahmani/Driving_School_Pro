package org.example.Utils;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.example.Entities.Vehicule;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for displaying document expiration alerts
 */
public class DocumentExpirationAlert {

    private static final int DEFAULT_WARNING_DAYS = 30;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Shows an alert for expiring or expired documents
     *
     * @param owner The owner stage
     * @param vehicules The list of vehicles to check
     * @param warningDays The number of days before expiration to show a warning
     */
    public static void showExpirationAlert(Stage owner, List<Vehicule> vehicules, int warningDays) {
        List<DocumentWarning> warnings = getDocumentWarnings(vehicules, warningDays);

        if (warnings.isEmpty()) {
            return; // No warnings to show
        }

        // Create the alert stage
        Stage alertStage = new Stage();
        alertStage.initOwner(owner);
        alertStage.initModality(Modality.APPLICATION_MODAL);
        alertStage.initStyle(StageStyle.DECORATED);
        alertStage.setTitle("Documents à renouveler");
        alertStage.setMinWidth(650);
        alertStage.setMinHeight(450);

        // Create the content
        VBox root = new VBox(15);
        root.setPadding(new Insets(25));
        root.setAlignment(Pos.TOP_CENTER);
        root.setStyle("-fx-background-color: white;");

        // Header
        Label headerLabel = new Label("Attention: Documents à renouveler");
        headerLabel.setFont(Font.font("System", FontWeight.BOLD, 20));
        headerLabel.setTextFill(Color.web("#1e3a8a"));

        // Description
        Label descriptionLabel = new Label("Les documents suivants arrivent à expiration ou sont déjà expirés:");
        descriptionLabel.setFont(Font.font("System", 14));
        descriptionLabel.setTextFill(Color.web("#4b5563"));

        // Separator
        Separator separator = new Separator();
        separator.setPrefWidth(Double.MAX_VALUE);

        // Warnings container
        VBox warningsContainer = new VBox(12);
        warningsContainer.setPadding(new Insets(5));

        // Count expired and soon-to-expire documents
        int expiredCount = 0;
        int soonToExpireCount = 0;

        for (DocumentWarning warning : warnings) {
            if (warning.isExpired) {
                expiredCount++;
            } else {
                soonToExpireCount++;
            }
        }

        // Summary labels
        HBox summaryBox = new HBox(20);
        summaryBox.setAlignment(Pos.CENTER);
        summaryBox.setPadding(new Insets(10, 0, 10, 0));

        if (expiredCount > 0) {
            Label expiredSummary = new Label(expiredCount + " document(s) expiré(s)");
            expiredSummary.setFont(Font.font("System", FontWeight.BOLD, 14));
            expiredSummary.setTextFill(Color.web("#dc2626"));
            expiredSummary.setStyle("-fx-background-color: #fee2e2; -fx-padding: 8px 16px; -fx-background-radius: 4px;");
            summaryBox.getChildren().add(expiredSummary);
        }

        if (soonToExpireCount > 0) {
            Label soonSummary = new Label(soonToExpireCount + " document(s) à expirer bientôt");
            soonSummary.setFont(Font.font("System", FontWeight.BOLD, 14));
            soonSummary.setTextFill(Color.web("#9a3412"));
            soonSummary.setStyle("-fx-background-color: #ffedd5; -fx-padding: 8px 16px; -fx-background-radius: 4px;");
            summaryBox.getChildren().add(soonSummary);
        }

        // Add each warning
        for (DocumentWarning warning : warnings) {
            HBox warningBox = createWarningBox(warning);
            warningsContainer.getChildren().add(warningBox);
        }

        // Wrap in scroll pane
        ScrollPane scrollPane = new ScrollPane(warningsContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(300);
        scrollPane.setStyle("-fx-background-color: white; -fx-background: white; -fx-border-color: #e5e7eb; -fx-border-radius: 4px;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        // Confirmation button
        Button confirmButton = new Button("J'ai compris");
        confirmButton.setOnAction(e -> alertStage.close());
        confirmButton.setPrefWidth(180);
        confirmButton.setPrefHeight(40);
        confirmButton.setFont(Font.font("System", FontWeight.BOLD, 14));
        confirmButton.setStyle("-fx-background-color: #1e40af; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 4px;");

        // Add hover effect
        confirmButton.setOnMouseEntered(e ->
                confirmButton.setStyle("-fx-background-color: #1e3a8a; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 4px;")
        );
        confirmButton.setOnMouseExited(e ->
                confirmButton.setStyle("-fx-background-color: #1e40af; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 4px;")
        );

        // Add all to root
        root.getChildren().addAll(headerLabel, descriptionLabel, separator, summaryBox, scrollPane, confirmButton);

        // Set the scene
        javafx.scene.Scene scene = new javafx.scene.Scene(root);

        // Add stylesheets
        scene.getStylesheets().add(DocumentExpirationAlert.class.getResource("/Styles/Common.css").toExternalForm());

        alertStage.setScene(scene);
        alertStage.showAndWait();
    }

    /**
     * Creates a warning box for a document
     *
     * @param warning The document warning
     * @return An HBox containing the warning information
     */
    private static HBox createWarningBox(DocumentWarning warning) {
        HBox box = new HBox(15);
        box.setPadding(new Insets(15));
        box.setAlignment(Pos.CENTER_LEFT);

        // Set different styles based on expiration status
        if (warning.isExpired) {
            box.setStyle("-fx-background-color: #fef2f2; -fx-border-color: #fee2e2; -fx-border-width: 1px; -fx-border-radius: 6px; -fx-background-radius: 6px;");
        } else {
            box.setStyle("-fx-background-color: #fff7ed; -fx-border-color: #ffedd5; -fx-border-width: 1px; -fx-border-radius: 6px; -fx-background-radius: 6px;");
        }

        // Status indicator
        Label statusIndicator = new Label();
        statusIndicator.setMinSize(24, 24);
        statusIndicator.setMaxSize(24, 24);

        if (warning.isExpired) {
            statusIndicator.setStyle("-fx-background-color: #dc2626; -fx-background-radius: 12px;");
        } else {
            statusIndicator.setStyle("-fx-background-color: #ea580c; -fx-background-radius: 12px;");
        }

        // Vehicle info
        VBox vehicleInfoBox = new VBox(3);
        Label vehicleLabel = new Label(warning.vehicleInfo);
        vehicleLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        vehicleLabel.setTextFill(Color.web("#1f2937"));

        vehicleInfoBox.getChildren().add(vehicleLabel);
        vehicleInfoBox.setPrefWidth(200);

        // Document info
        VBox documentInfo = new VBox(5);
        Label documentTypeLabel = new Label(warning.documentType);
        documentTypeLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        documentTypeLabel.setTextFill(Color.web("#1f2937"));

        Label statusLabel = new Label(warning.statusMessage);
        statusLabel.setFont(Font.font("System", 13));
        statusLabel.setTextFill(warning.isExpired ? Color.web("#dc2626") : Color.web("#ea580c"));

        documentInfo.getChildren().addAll(documentTypeLabel, statusLabel);

        // Add all to box
        box.getChildren().addAll(statusIndicator, vehicleInfoBox, documentInfo);
        HBox.setHgrow(documentInfo, Priority.ALWAYS);

        return box;
    }

    /**
     * Gets a list of document warnings for the given vehicles
     *
     * @param vehicules The list of vehicles to check
     * @param warningDays The number of days before expiration to show a warning
     * @return A list of document warnings
     */
    private static List<DocumentWarning> getDocumentWarnings(List<Vehicule> vehicules, int warningDays) {
        List<DocumentWarning> warnings = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (Vehicule vehicule : vehicules) {
            String vehicleInfo = vehicule.getMarque() + " " + vehicule.getModele() + " (" + vehicule.getMatricule() + ")";

            // Check vignette
            if (vehicule.getDateVignette() != null) {
                if (vehicule.getDateVignette().isBefore(today)) {
                    warnings.add(new DocumentWarning(
                            vehicleInfo,
                            "Vignette",
                            "EXPIRÉE depuis le " + vehicule.getDateVignette().format(DATE_FORMATTER),
                            true
                    ));
                } else if (vehicule.getDateVignette().isBefore(today.plusDays(warningDays))) {
                    long daysLeft = ChronoUnit.DAYS.between(today, vehicule.getDateVignette());
                    warnings.add(new DocumentWarning(
                            vehicleInfo,
                            "Vignette",
                            "Expire le " + vehicule.getDateVignette().format(DATE_FORMATTER) + " (dans " + daysLeft + " jour(s))",
                            false
                    ));
                }
            }

            // Check assurance
            if (vehicule.getDateAssurance() != null) {
                if (vehicule.getDateAssurance().isBefore(today)) {
                    warnings.add(new DocumentWarning(
                            vehicleInfo,
                            "Assurance",
                            "EXPIRÉE depuis le " + vehicule.getDateAssurance().format(DATE_FORMATTER),
                            true
                    ));
                } else if (vehicule.getDateAssurance().isBefore(today.plusDays(warningDays))) {
                    long daysLeft = ChronoUnit.DAYS.between(today, vehicule.getDateAssurance());
                    warnings.add(new DocumentWarning(
                            vehicleInfo,
                            "Assurance",
                            "Expire le " + vehicule.getDateAssurance().format(DATE_FORMATTER) + " (dans " + daysLeft + " jour(s))",
                            false
                    ));
                }
            }

            // Check visite technique
            if (vehicule.getDateVisiteTechnique() != null) {
                if (vehicule.getDateVisiteTechnique().isBefore(today)) {
                    warnings.add(new DocumentWarning(
                            vehicleInfo,
                            "Visite Technique",
                            "EXPIRÉE depuis le " + vehicule.getDateVisiteTechnique().format(DATE_FORMATTER),
                            true
                    ));
                } else if (vehicule.getDateVisiteTechnique().isBefore(today.plusDays(warningDays))) {
                    long daysLeft = ChronoUnit.DAYS.between(today, vehicule.getDateVisiteTechnique());
                    warnings.add(new DocumentWarning(
                            vehicleInfo,
                            "Visite Technique",
                            "Expire le " + vehicule.getDateVisiteTechnique().format(DATE_FORMATTER) + " (dans " + daysLeft + " jour(s))",
                            false
                    ));
                }
            }

            // Check prochain entretien
            if (vehicule.getDateProchainEntretien() != null) {
                if (vehicule.getDateProchainEntretien().isBefore(today)) {
                    warnings.add(new DocumentWarning(
                            vehicleInfo,
                            "Entretien",
                            "EN RETARD depuis le " + vehicule.getDateProchainEntretien().format(DATE_FORMATTER),
                            true
                    ));
                } else if (vehicule.getDateProchainEntretien().isBefore(today.plusDays(warningDays))) {
                    long daysLeft = ChronoUnit.DAYS.between(today, vehicule.getDateProchainEntretien());
                    warnings.add(new DocumentWarning(
                            vehicleInfo,
                            "Entretien",
                            "Prévu le " + vehicule.getDateProchainEntretien().format(DATE_FORMATTER) + " (dans " + daysLeft + " jour(s))",
                            false
                    ));
                }
            }
        }

        return warnings;
    }

    /**
     * Inner class to represent a document warning
     */
    private static class DocumentWarning {
        public final String vehicleInfo;
        public final String documentType;
        public final String statusMessage;
        public final boolean isExpired;

        public DocumentWarning(String vehicleInfo, String documentType, String statusMessage, boolean isExpired) {
            this.vehicleInfo = vehicleInfo;
            this.documentType = documentType;
            this.statusMessage = statusMessage;
            this.isExpired = isExpired;
        }
    }

    /**
     * Shows an alert for expiring or expired documents with default warning days
     *
     * @param owner The owner stage
     * @param vehicules The list of vehicles to check
     */
    public static void showExpirationAlert(Stage owner, List<Vehicule> vehicules) {
        showExpirationAlert(owner, vehicules, DEFAULT_WARNING_DAYS);
    }
}

