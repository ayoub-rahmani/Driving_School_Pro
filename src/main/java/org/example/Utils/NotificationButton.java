package org.example.Utils;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import javafx.util.Duration;
import org.example.Entities.Vehicule;
import org.example.Service.VehiculeService;
import org.example.Rep.VehiculeRep;

import java.time.LocalDate;
import java.util.List;

/**
 * A notification button component that shows a bell icon with a red dot indicator
 * when there are document expiration notifications.
 */
public class NotificationButton extends Button {

    private final Circle notificationDot;
    private final SVGPath bellIcon;
    private final StackPane iconContainer;
    private int notificationCount = 0;
    private VehiculeService vehiculeService;

    public NotificationButton() {
        // Set button style
        getStyleClass().add("icon-button");
        setTooltip(new Tooltip("Notifications"));

        // Create the bell icon
        bellIcon = new SVGPath();
        bellIcon.setContent("M12,22A2,2 0 0,0 14,20H10A2,2 0 0,0 12,22M18,16V11C18,7.93 16.36,5.36 13.5,4.68V4A1.5,1.5 0 0,0 12,2.5A1.5,1.5 0 0,0 10.5,4V4.68C7.63,5.36 6,7.92 6,11V16L4,18V19H20V18L18,16Z");
        bellIcon.setFill(Color.WHITE);

        // Create a container for the icon and notification dot
        iconContainer = new StackPane();
        iconContainer.setAlignment(Pos.CENTER);

        // Create the notification dot (initially invisible)
        notificationDot = new Circle(5);
        notificationDot.setFill(Color.RED);
        notificationDot.setTranslateX(7);
        notificationDot.setTranslateY(-7);
        notificationDot.setVisible(false);

        // Add components to the container
        iconContainer.getChildren().addAll(bellIcon, notificationDot);

        // Set the container as the button's graphic
        setGraphic(iconContainer);

        // Initialize the vehicle service
        vehiculeService = new VehiculeService(new VehiculeRep());

        // Set up the click handler
        setOnAction(e -> showNotifications());
    }

    /**
     * Updates the notification count and shows/hides the notification dot accordingly.
     *
     * @param count The number of notifications
     */
    public void updateNotificationCount(int count) {
        this.notificationCount = count;

        if (count > 0) {
            if (!notificationDot.isVisible()) {
                notificationDot.setVisible(true);

                // Add animation for the notification dot
                ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(200), notificationDot);
                scaleTransition.setFromX(0);
                scaleTransition.setFromY(0);
                scaleTransition.setToX(1);
                scaleTransition.setToY(1);
                scaleTransition.play();

                FadeTransition fadeTransition = new FadeTransition(Duration.millis(200), notificationDot);
                fadeTransition.setFromValue(0);
                fadeTransition.setToValue(1);
                fadeTransition.play();
            }
        } else {
            notificationDot.setVisible(false);
        }
    }

    /**
     * Shows the notification popup with document expiration alerts.
     */
    private void showNotifications() {
        if (getScene() == null) return;

        // Get the current stage
        javafx.stage.Stage stage = (javafx.stage.Stage) getScene().getWindow();

        // Get the list of vehicles
        List<Vehicule> vehicules = getVehicules();

        // Show the enhanced document expiration alert
        SafeDocumentExpirationAlert.showExpirationAlert(stage, vehicules, 30);
    }
    /**
     * Gets the list of vehicles from the service.
     *
     * @return A list of vehicles
     */
    private List<Vehicule> getVehicules() {
        try {
            return vehiculeService.getAllVehicules();
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    /**
     * Checks for expiring documents and updates the notification count.
     *
     * @param vehicules The list of vehicles to check
     * @param daysThreshold The number of days before expiration to show a warning
     */
    public void checkExpiringDocuments(List<Vehicule> vehicules, int daysThreshold) {
        int count = 0;
        LocalDate today = LocalDate.now();
        LocalDate thresholdDate = today.plusDays(daysThreshold);

        for (Vehicule vehicule : vehicules) {
            // Check vignette
            if (vehicule.getDateVignette() != null &&
                    (vehicule.getDateVignette().isBefore(today) ||
                            vehicule.getDateVignette().isBefore(thresholdDate))) {
                count++;
            }

            // Check assurance
            if (vehicule.getDateAssurance() != null &&
                    (vehicule.getDateAssurance().isBefore(today) ||
                            vehicule.getDateAssurance().isBefore(thresholdDate))) {
                count++;
            }

            // Check visite technique
            if (vehicule.getDateVisiteTechnique() != null &&
                    (vehicule.getDateVisiteTechnique().isBefore(today) ||
                            vehicule.getDateVisiteTechnique().isBefore(thresholdDate))) {
                count++;
            }

            // Check prochain entretien
            if (vehicule.getDateProchainEntretien() != null &&
                    (vehicule.getDateProchainEntretien().isBefore(today) ||
                            vehicule.getDateProchainEntretien().isBefore(thresholdDate))) {
                count++;
            }
        }

        updateNotificationCount(count);
    }
}