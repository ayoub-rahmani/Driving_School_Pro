package org.example.Utils;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.example.Entities.Vehicule;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SafeDocumentExpirationAlert {

    private static class ExpirationItem {
        private final Vehicule vehicule;
        private final String documentType;
        private final LocalDate expirationDate;
        private final long daysUntilExpiration;
        private final boolean isExpired;

        public ExpirationItem(Vehicule vehicule, String documentType, LocalDate expirationDate) {
            this.vehicule = vehicule;
            this.documentType = documentType;
            this.expirationDate = expirationDate;

            LocalDate today = LocalDate.now();
            this.daysUntilExpiration = ChronoUnit.DAYS.between(today, expirationDate);
            this.isExpired = expirationDate.isBefore(today) || expirationDate.isEqual(today);
        }

        public Vehicule getVehicule() {
            return vehicule;
        }

        public String getDocumentType() {
            return documentType;
        }

        public LocalDate getExpirationDate() {
            return expirationDate;
        }

        public long getDaysUntilExpiration() {
            return daysUntilExpiration;
        }

        public boolean isExpired() {
            return isExpired;
        }

        public boolean isExpiringSoon(int threshold) {
            return !isExpired && daysUntilExpiration <= threshold;
        }

        public String getStatus(int threshold) {
            if (isExpired) {
                return "Expiré";
            } else if (daysUntilExpiration <= threshold) {
                return "Expire bientôt";
            } else {
                return "Valide";
            }
        }

        public Color getStatusColor() {
            if (isExpired) {
                return Color.RED;
            } else if (daysUntilExpiration <= 7) {
                return Color.ORANGE;
            } else if (daysUntilExpiration <= 30) {
                return Color.GOLD;
            } else {
                return Color.GREEN;
            }
        }
    }

    public static void showExpirationAlert(Stage parentStage, List<Vehicule> vehicules, int daysThreshold) {
        // Run on JavaFX thread to avoid threading issues
        Platform.runLater(() -> {
            try {
                // Create a list of expiration items
                List<ExpirationItem> expirationItems = new ArrayList<>();

                // Check each vehicle for expiring documents
                for (Vehicule vehicule : vehicules) {
                    // Check vignette
                    if (vehicule.getDateVignette() != null) {
                        expirationItems.add(new ExpirationItem(vehicule, "Vignette", vehicule.getDateVignette()));
                    }

                    // Check assurance
                    if (vehicule.getDateAssurance() != null) {
                        expirationItems.add(new ExpirationItem(vehicule, "Assurance", vehicule.getDateAssurance()));
                    }

                    // Check visite technique
                    if (vehicule.getDateVisiteTechnique() != null) {
                        expirationItems.add(new ExpirationItem(vehicule, "Visite Technique", vehicule.getDateVisiteTechnique()));
                    }

                    // Check prochain entretien
                    if (vehicule.getDateProchainEntretien() != null) {
                        expirationItems.add(new ExpirationItem(vehicule, "Entretien", vehicule.getDateProchainEntretien()));
                    }
                }

                // Filter items that are expired or expiring soon
                List<ExpirationItem> relevantItems = new ArrayList<>();
                for (ExpirationItem item : expirationItems) {
                    if (item.isExpired() || item.isExpiringSoon(daysThreshold)) {
                        relevantItems.add(item);
                    }
                }

                // If no relevant items, show a message and return
                if (relevantItems.isEmpty()) {
                    NotificationManager.showInfo(parentStage, "Documents à jour",
                            "Tous les documents sont à jour. Aucun document n'expire dans les " + daysThreshold + " prochains jours.");
                    return;
                }

                // Create observable lists for each tab
                ObservableList<ExpirationItem> allItems = FXCollections.observableArrayList(relevantItems);
                ObservableList<ExpirationItem> expiredItems = FXCollections.observableArrayList(
                        relevantItems.stream().filter(ExpirationItem::isExpired).collect(Collectors.toList()));
                ObservableList<ExpirationItem> expiringSoonItems = FXCollections.observableArrayList(
                        relevantItems.stream()
                                .filter(item -> !item.isExpired() && item.isExpiringSoon(daysThreshold))
                                .collect(Collectors.toList()));

                // Create a new stage for the alert
                Stage alertStage = new Stage();
                alertStage.initOwner(parentStage);
                alertStage.initModality(Modality.APPLICATION_MODAL);
                alertStage.initStyle(StageStyle.DECORATED);
                alertStage.setTitle("Alerte d'expiration de documents");
                alertStage.setMinWidth(800);
                alertStage.setMinHeight(500);

                // Create the main layout
                BorderPane mainLayout = new BorderPane();
                mainLayout.setPadding(new Insets(20));

                // Create header with title
                HBox header = new HBox(10);
                header.setAlignment(Pos.CENTER_LEFT);

                Label titleLabel = new Label("Documents expirant ou expirés");
                titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

                header.getChildren().add(titleLabel);

                // Create tab pane
                TabPane tabPane = new TabPane();
                tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

                // Create tabs
                Tab allTab = new Tab("Tout (" + allItems.size() + ")");
                allTab.setContent(createTableView(allItems, daysThreshold));

                Tab expiredTab = new Tab("Expirée (" + expiredItems.size() + ")");
                expiredTab.setContent(createTableView(expiredItems, daysThreshold));

                Tab expiringSoonTab = new Tab("Expire bientôt (" + expiringSoonItems.size() + ")");
                expiringSoonTab.setContent(createTableView(expiringSoonItems, daysThreshold));

                tabPane.getTabs().addAll(allTab, expiredTab, expiringSoonTab);

                // Create button section
                HBox buttonBox = new HBox(10);
                buttonBox.setAlignment(Pos.CENTER_RIGHT);
                buttonBox.setPadding(new Insets(20, 0, 0, 0));

                Button closeButton = new Button("Fermer");
                closeButton.setOnAction(e -> alertStage.close());

                buttonBox.getChildren().add(closeButton);

                // Assemble the layout
                VBox content = new VBox(20);
                content.getChildren().addAll(header, tabPane, buttonBox);
                mainLayout.setCenter(content);

                // Set the scene and show the stage
                Scene scene = new Scene(mainLayout);
                scene.getStylesheets().add(SafeDocumentExpirationAlert.class.getResource("/Styles/Common.css").toExternalForm());
                alertStage.setScene(scene);
                alertStage.show();

            } catch (Exception e) {
                e.printStackTrace();
                NotificationManager.showError(parentStage, "Erreur",
                        "Une erreur s'est produite lors de l'affichage des alertes d'expiration: " + e.getMessage());
            }
        });
    }

    private static TableView<ExpirationItem> createTableView(ObservableList<ExpirationItem> items, int daysThreshold) {
        TableView<ExpirationItem> tableView = new TableView<>(items);
        tableView.setPlaceholder(new Label("Aucun document à afficher"));

        // Create columns
        TableColumn<ExpirationItem, String> vehiculeColumn = new TableColumn<>("Véhicule");
        vehiculeColumn.setCellValueFactory(cellData ->
                javafx.beans.binding.Bindings.createStringBinding(
                        () -> cellData.getValue().getVehicule().getMarque() + " " +
                                cellData.getValue().getVehicule().getModele() + " (" +
                                cellData.getValue().getVehicule().getMatricule() + ")"));
        vehiculeColumn.setPrefWidth(200);

        TableColumn<ExpirationItem, String> documentTypeColumn = new TableColumn<>("Type de document");
        documentTypeColumn.setCellValueFactory(cellData ->
                javafx.beans.binding.Bindings.createStringBinding(
                        () -> cellData.getValue().getDocumentType()));
        documentTypeColumn.setPrefWidth(150);

        TableColumn<ExpirationItem, String> expirationDateColumn = new TableColumn<>("Date d'expiration");
        expirationDateColumn.setCellValueFactory(cellData ->
                javafx.beans.binding.Bindings.createStringBinding(
                        () -> cellData.getValue().getExpirationDate().format(
                                DateTimeFormatter.ofPattern("dd/MM/yyyy"))));
        expirationDateColumn.setPrefWidth(150);

        TableColumn<ExpirationItem, String> daysColumn = new TableColumn<>("Jours restants");
        daysColumn.setCellValueFactory(cellData -> {
            ExpirationItem item = cellData.getValue();
            return javafx.beans.binding.Bindings.createStringBinding(() -> {
                if (item.isExpired()) {
                    return "Expiré";
                } else {
                    return item.getDaysUntilExpiration() + " jour(s)";
                }
            });
        });
        daysColumn.setPrefWidth(120);

        TableColumn<ExpirationItem, String> statusColumn = new TableColumn<>("Statut");
        statusColumn.setCellValueFactory(cellData ->
                javafx.beans.binding.Bindings.createStringBinding(
                        () -> cellData.getValue().getStatus(daysThreshold)));
        statusColumn.setPrefWidth(120);

        // Add status indicator with colored circle
        statusColumn.setCellFactory(column -> new TableCell<ExpirationItem, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    ExpirationItem expirationItem = getTableView().getItems().get(getIndex());

                    HBox container = new HBox(10);
                    container.setAlignment(Pos.CENTER_LEFT);

                    Circle circle = new Circle(5);
                    circle.setFill(expirationItem.getStatusColor());

                    Label label = new Label(item);

                    container.getChildren().addAll(circle, label);
                    setGraphic(container);
                }
            }
        });

        tableView.getColumns().addAll(vehiculeColumn, documentTypeColumn, expirationDateColumn,
                daysColumn, statusColumn);

        return tableView;
    }
}