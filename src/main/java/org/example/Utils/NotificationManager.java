package org.example.Utils;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

public class NotificationManager {

    private static final int NOTIFICATION_WIDTH = 350;
    private static final int NOTIFICATION_HEIGHT = 80;
    private static final int NOTIFICATION_SPACING = 10;
    private static final int NOTIFICATION_PADDING = 15;
    private static final int NOTIFICATION_OFFSET_X = 20;
    private static final int NOTIFICATION_OFFSET_Y = 50;
    private static final int NOTIFICATION_DURATION = 2500; // 3 seconds (was 5)

    private static final int FADE_OUT_DURATION = 300; // milliseconds
    private static final int SLIDE_DISTANCE = 0;  // No slide

    private static final List<Popup> activeNotifications = new ArrayList<>();

    /**
     * Shows a success notification
     * @param stage The stage to show the notification on
     * @param title The title of the notification
     * @param message The message of the notification
     */
    public static void showSuccess(Stage stage, String title, String message) {
        showNotification(stage, title, message, "success", "M9,20.42L2.79,14.21L5.62,11.38L9,14.77L18.88,4.88L21.71,7.71L9,20.42Z");
    }

    /**
     * Shows an error notification
     * @param stage The stage to show the notification on
     * @param title The title of the notification
     * @param message The message of the notification
     */
    public static void showError(Stage stage, String title, String message) {
        showNotification(stage, title, message, "error", "M19,6.41L17.59,5L12,10.59L6.41,5L5,6.41L10.59,12L5,17.59L6.41,19L12,13.41L17.59,19L19,17.59L13.41,12L19,6.41Z");
    }

    /**
     * Shows a warning notification
     * @param stage The stage to show the notification on
     * @param title The title of the notification
     * @param message The message of the notification
     */
    public static void showWarning(Stage stage, String title, String message) {
        showNotification(stage, title, message, "warning", "M13,14H11V10H13M13,18H11V16H13M1,21H23L12,2L1,21Z");
    }

    /**
     * Shows an info notification
     * @param stage The stage to show the notification on
     * @param title The title of the notification
     * @param message The message of the notification
     */
    public static void showInfo(Stage stage, String title, String message) {
        showNotification(stage, title, message, "info", "M13,9H11V7H13M13,17H11V11H13M12,2A10,10 0 0,0 2,12A10,10 0 0,0 12,22A10,10 0 0,0 22,12A10,10 0 0,0 12,2Z");
    }

    /**
     * Shows a notification with the given parameters
     * @param stage The stage to show the notification on
     * @param title The title of the notification
     * @param message The message of the notification
     * @param type The type of notification (success, error, warning, info)
     * @param iconPath The SVG path for the icon
     */
    private static void showNotification(Stage stage, String title, String message, String type, String iconPath) {
        if (stage == null) {
            System.err.println("Cannot show notification: Stage is null");
            return;
        }

        // Run on JavaFX thread
        Platform.runLater(() -> {
            try {
                // Create notification content
                VBox notificationBox = createNotificationBox(title, message, type, iconPath);

                // Create popup
                Popup popup = new Popup();
                popup.getContent().add(notificationBox);
                popup.setAutoHide(true);

                // Calculate position (bottom right)
                double stageWidth = stage.getWidth();
                double stageHeight = stage.getHeight();

                double startX = stageWidth - NOTIFICATION_WIDTH - NOTIFICATION_OFFSET_X;
                double startY = stageHeight - NOTIFICATION_OFFSET_Y;

                // Adjust position based on existing notifications
                startY -= (activeNotifications.size() * (NOTIFICATION_HEIGHT + NOTIFICATION_SPACING));

                // Show popup
                popup.show(stage, stage.getX() + startX, stage.getY() + startY);

                // Add to active notifications
                activeNotifications.add(popup);

                // Animate in: POP!
                notificationBox.setOpacity(1); // Set to fully opaque immediately
                notificationBox.setTranslateX(0); // No slide

                // Auto-hide after duration
                Timeline hideTimeline = new Timeline(
                        new KeyFrame(Duration.millis(NOTIFICATION_DURATION - FADE_OUT_DURATION),
                                new KeyValue(notificationBox.opacityProperty(), 1),
                                new KeyValue(notificationBox.translateXProperty(), 0)
                        ),
                        new KeyFrame(Duration.millis(NOTIFICATION_DURATION),
                                event -> {
                                    popup.hide();
                                    activeNotifications.remove(popup);
                                    repositionNotifications(stage);
                                },
                                new KeyValue(notificationBox.opacityProperty(), 0) // Fade out
                        )
                );
                hideTimeline.play();

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Creates the notification box with the given parameters
     * @param title The title of the notification
     * @param message The message of the notification
     * @param type The type of notification (success, error, warning, info)
     * @param iconPath The SVG path for the icon
     * @return The notification box
     */
    private static VBox createNotificationBox(String title, String message, String type, String iconPath) {
        // Create icon
        SVGPath icon = new SVGPath();
        icon.setContent(iconPath);

        // Set icon color based on type
        switch (type) {
            case "success":
                icon.setFill(Color.web("#10b981"));
                break;
            case "error":
                icon.setFill(Color.web("#ef4444"));
                break;
            case "warning":
                icon.setFill(Color.web("#f59e0b"));
                break;
            case "info":
                icon.setFill(Color.web("#3b82f6"));
                break;
            default:
                icon.setFill(Color.web("#6b7280"));
                break;
        }

        // Scale icon
        icon.setScaleX(1.5);
        icon.setScaleY(1.5);

        // Create title label
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("notification-title");
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        // Create message label
        Label messageLabel = new Label(message);
        messageLabel.getStyleClass().add("notification-message");
        messageLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b7280;");
        messageLabel.setWrapText(true);

        // Create text container
        VBox textContainer = new VBox(5, titleLabel, messageLabel);
        textContainer.setAlignment(Pos.CENTER_LEFT);

        // Create icon container with padding
        StackPane iconContainer = new StackPane(icon);
        iconContainer.setMinWidth(40);
        iconContainer.setMinHeight(40);
        iconContainer.setAlignment(Pos.CENTER);

        // Create horizontal layout
        HBox content = new HBox(10, iconContainer, textContainer);
        content.setAlignment(Pos.CENTER_LEFT);

        // Create notification box
        VBox notificationBox = new VBox(content);
        notificationBox.setPrefWidth(NOTIFICATION_WIDTH);
        notificationBox.setMinHeight(NOTIFICATION_HEIGHT);
        notificationBox.getStyleClass().add("notification");
        notificationBox.getStyleClass().add("notification-" + type);

        // Apply styles
        notificationBox.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 8px;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.2), 10, 0, 0, 3);" +
                        "-fx-padding: " + NOTIFICATION_PADDING + "px;" +
                        "-fx-max-width: " + NOTIFICATION_WIDTH + "px;" +
                        "-fx-border-color: " + getColorForType(type) + ";" +
                        "-fx-border-width: 0 0 0 4px;" +
                        "-fx-border-radius: 8px;"
        );

        return notificationBox;
    }

    /**
     * Gets the color for the given notification type
     * @param type The type of notification
     * @return The color for the notification type
     */
    private static String getColorForType(String type) {
        switch (type) {
            case "success":
                return "#10b981";
            case "error":
                return "#ef4444";
            case "warning":
                return "#f59e0b";
            case "info":
                return "#3b82f6";
            default:
                return "#6b7280";
        }
    }

    /**
     * Repositions all active notifications
     * @param stage The stage to reposition notifications on
     */
    private static void repositionNotifications(Stage stage) {
        double stageWidth = stage.getWidth();
        double stageHeight = stage.getHeight();

        double startX = stageWidth - NOTIFICATION_WIDTH - NOTIFICATION_OFFSET_X;
        double startY = stageHeight - NOTIFICATION_OFFSET_Y;

        for (int i = 0; i < activeNotifications.size(); i++) {
            Popup popup = activeNotifications.get(i);
            double y = startY - (i * (NOTIFICATION_HEIGHT + NOTIFICATION_SPACING));
            popup.setX(stage.getX() + startX);
            popup.setY(stage.getY() + y);
        }
    }

    /**
     * Gets the stage from a node
     * @param node The node to get the stage from
     * @return The stage
     */
    public static Stage getStage(Node node) {
        if (node == null) {
            return null;
        }

        if (node.getScene() == null) {
            return null;
        }

        return (Stage) node.getScene().getWindow();
    }
}