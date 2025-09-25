package org.example.Controllers;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.Entities.TypePermis;
import org.example.Entities.User;
import org.example.Entities.Vehicule;
import org.example.Rep.VehiculeRep;
import org.example.Service.AuditLogService;
import org.example.Service.VehiculeService;
import org.example.Utils.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class VehiculeController implements Initializable {

    @FXML private ComboBox<String> statutFilter;
    @FXML private ComboBox<String> typeFilter;
    @FXML private DatePicker dateFilter;

    @FXML private TableView<Vehicule> vehiculeTable;
    @FXML private TableColumn<Vehicule, Long> idColumn;
    @FXML private TableColumn<Vehicule, String> marqueColumn;
    @FXML private TableColumn<Vehicule, String> modeleColumn;
    @FXML private TableColumn<Vehicule, String> matriculeColumn;
    @FXML private TableColumn<Vehicule, TypePermis> typeColumn;
    @FXML private TableColumn<Vehicule, Integer> kilometrageColumn;

    @FXML private Button notificationBtn;
    private NotificationButton notificationButton;

    @FXML private TextField rechercheField, marqueField, modeleField, matriculeField, kilometrageField, motifField;
    @FXML private DatePicker dateMiseEnServicePicker, dateProchainEntretienPicker, dateVignettePicker,
            dateAssurancePicker, dateVisiteTechniquePicker;
    @FXML private RadioButton typeA, typeB, typeC;
    @FXML private RadioButton disponibleOui, disponibleNon;
    @FXML private TextArea papiersField, notesField;

    @FXML private Button ajouterBtn, modifierBtn, supprimerBtn, annulerBtn, enregistrerBtn, backBtn, exportBtn;
    @FXML private Button reparationsBtn,dashboardBtn, refreshDashboardBtn;

    @FXML private Label vehiculeNameLabel;
    @FXML private ImageView vehiculePhoto;

    // Add these new fields for availability indicator
    @FXML private StackPane availabilityIndicator;
    @FXML private Label availabilityLabel;

    // Add this field for type badges container
    @FXML private HBox typeBadgesContainer;

    @FXML private ToggleGroup typeGroup;
    @FXML private ToggleGroup disponibiliteGroup;

    // Dashboard components
    @FXML private VBox dashboardPane;
    @FXML private ScrollPane formScrollPane;
    @FXML private Label totalVehiculesLabel;
    @FXML private Label vehiculesDisponiblesLabel;
    @FXML private Label vehiculesIndisponiblesLabel;
    @FXML private Label documentsExpirantLabel;
    @FXML private PieChart typeVehiculeChart;
    @FXML private BarChart<String, Number> documentExpirationChart;

    private VehiculeService vehiculeService;
    private Vehicule selectedVehicule;
    private AuditLogService auditLogService;

    // Dashboard state
    private boolean isDashboardVisible = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumns();
        vehiculeService = new VehiculeService(new VehiculeRep());

        // Initialize audit log service
        auditLogService = new AuditLogService();

        // Initialize the notification button
        notificationButton = new NotificationButton();

        // Replace the notification button with our custom component
        if (notificationBtn != null) {
            // Get the parent HBox (header container)
            HBox parent = (HBox) notificationBtn.getParent();
            if (parent != null) {
                // Find the index of the notification button in the parent
                int index = parent.getChildren().indexOf(notificationBtn);
                // Remove the original button
                parent.getChildren().remove(notificationBtn);
                // Add our custom notification button at the same position
                parent.getChildren().add(index, notificationButton);
            }
        }

        // Apply role-based access control
        applyRoleBasedAccess();
        setupTypeRadioButtons();

        // Initialize filters
        statutFilter.setValue("Tous les statuts");
        typeFilter.setValue("Tous les types");

        loadVehicules();
        setupSelectionListener();
        clearForm();
        disableForm(true);

        // Set up radio button listeners to update availability indicator in real-time
        disponibleOui.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                updateAvailabilityIndicator(true);
            }
        });

        disponibleNon.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                updateAvailabilityIndicator(false);
            }
        });

        // Set up type radio buttons to update type badges in real-time
        typeA.selectedProperty().addListener((obs, oldVal, newVal) -> updateTypeBadges());
        typeB.selectedProperty().addListener((obs, oldVal, newVal) -> updateTypeBadges());
        typeC.selectedProperty().addListener((obs, oldVal, newVal) -> updateTypeBadges());

        // Setup keyboard shortcuts
        setupKeyboardShortcuts();

        // Set fixed dimensions for the vehicle photo
        if (vehiculePhoto != null) {
            vehiculePhoto.setFitWidth(120.0);
            vehiculePhoto.setFitHeight(120.0);
            vehiculePhoto.setPreserveRatio(true);
            vehiculePhoto.getStyleClass().add("fixed-size-image");
        }

        // Use a longer delay to ensure the scene is fully loaded and animations are complete
        Platform.runLater(() -> {
            // Add a small delay to ensure animations are complete
            PauseTransition delay = new PauseTransition(Duration.millis(500));
            delay.setOnFinished(e -> {
                if (vehiculeTable.getScene() != null) {
                    forceCheckExpiringDocuments();
                }
            });
            delay.play();
        });
    }
    public void forceCheckExpiringDocuments() {
        // Use a longer delay to ensure all animations are complete
        javafx.animation.PauseTransition delay = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(2.0));
        delay.setOnFinished(e -> {
            try {
                if (vehiculeTable != null && vehiculeTable.getScene() != null) {
                    Stage stage = (Stage) vehiculeTable.getScene().getWindow();
                    if (stage != null && stage.isShowing()) {
                        // Get fresh data
                        List<Vehicule> vehicules = vehiculeService.getAllVehicules();

                        // Update notification button
                        notificationButton.checkExpiringDocuments(vehicules, 30);

                        // Use the enhanced version that supports filtering
                        SafeDocumentExpirationAlert.showExpirationAlert(stage, vehicules, 30);
                    } else {
                        System.err.println("Stage is null or not showing");
                    }
                } else {
                    System.err.println("VehiculeTable or its scene is null");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        delay.play();
    }
    public void checkExpiringDocuments() {
        try {
            List<Vehicule> vehicules = vehiculeService.getAllVehicules();
            Stage stage = (Stage) vehiculeTable.getScene().getWindow();
            DocumentExpirationAlert.showExpirationAlert(stage, vehicules, 30); // Show warnings for documents expiring in 30 days
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void updateVehiculePhoto(TypePermis type) {
        if (vehiculePhoto == null) return;

        String imagePath;

        if (type == null) {
            // Default image when no type is selected
            imagePath = "/images/vehiculle.png";
        } else {
            switch (type) {
                case Moto:
                    imagePath = "/images/moto.png";
                    break;
                case Voiture:
                    imagePath = "/images/voiture.png";
                    break;
                case Camion:
                    imagePath = "/images/camion.png";
                    break;
                default:
                    imagePath = "/images/vehiculle.png";
                    break;
            }
        }

        try {
            // Save the current dimensions before changing the image
            double currentFitWidth = vehiculePhoto.getFitWidth();
            double currentFitHeight = vehiculePhoto.getFitHeight();

            // Load the new image
            Image image = new Image(getClass().getResourceAsStream(imagePath));
            vehiculePhoto.setImage(image);

            // Ensure the ImageView maintains its size
            vehiculePhoto.setFitWidth(currentFitWidth > 0 ? currentFitWidth : 50.0);
            vehiculePhoto.setFitHeight(currentFitHeight > 0 ? currentFitHeight : 50.0);
            vehiculePhoto.setPreserveRatio(false);

            // Apply CSS to maintain container size
            vehiculePhoto.getStyleClass().add("/Styles/Vehicule/fixed-size-image");
        } catch (Exception e) {
            System.err.println("Error loading vehicle image: " + e.getMessage());
            // Fallback to default image if there's an error
            try {
                Image defaultImage = new Image(getClass().getResourceAsStream("/images/vehicule.jpg"));
                vehiculePhoto.setImage(defaultImage);
            } catch (Exception ex) {
                // If even the default image fails, just log the error
                System.err.println("Error loading default vehicle image: " + ex.getMessage());
            }
        }
    }

    private void setupTypeRadioButtons() {
        // Set up type radio buttons to update type badges and photo in real-time
        typeA.selectedProperty().addListener((obs, oldVal, newVal) -> {
            updateTypeBadges();
            if (newVal) updateVehiculePhoto(TypePermis.Moto);
        });

        typeB.selectedProperty().addListener((obs, oldVal, newVal) -> {
            updateTypeBadges();
            if (newVal) updateVehiculePhoto(TypePermis.Voiture);
        });

        typeC.selectedProperty().addListener((obs, oldVal, newVal) -> {
            updateTypeBadges();
            if (newVal) updateVehiculePhoto(TypePermis.Camion);
        });
    }


    // Helper method to update the availability indicator
    private void updateAvailabilityIndicator(boolean isAvailable) {
        if (isAvailable) {
            // Remove unavailable class first
            availabilityIndicator.getStyleClass().remove("availability-indicator-unavailable");
            // Make sure the base class is there
            if (!availabilityIndicator.getStyleClass().contains("availability-indicator")) {
                availabilityIndicator.getStyleClass().add("availability-indicator");
            }
            availabilityLabel.setText("Disponible");
        } else {
            // Make sure the base class is there
            if (!availabilityIndicator.getStyleClass().contains("availability-indicator")) {
                availabilityIndicator.getStyleClass().add("availability-indicator");
            }
            // Add unavailable class if not already present
            if (!availabilityIndicator.getStyleClass().contains("availability-indicator-unavailable")) {
                availabilityIndicator.getStyleClass().add("availability-indicator-unavailable");
            }
            availabilityLabel.setText("Non disponible");
        }
    }

    // Helper method to update type badges based on selected type
    private void updateTypeBadges() {
        // Clear existing badges
        typeBadgesContainer.getChildren().clear();

        // Add badge for selected type with specific color
        if (typeA.isSelected()) {
            Label badgeA = new Label("Moto");
            badgeA.getStyleClass().add("type-badge");
            badgeA.getStyleClass().add("type-badge-a"); // Custom class for type A (red)
            typeBadgesContainer.getChildren().add(badgeA);
        } else if (typeB.isSelected()) {
            Label badgeB = new Label("Voiture");
            badgeB.getStyleClass().add("type-badge");
            badgeB.getStyleClass().add("type-badge-b"); // Custom class for type B (blue)
            typeBadgesContainer.getChildren().add(badgeB);
        } else if (typeC.isSelected()) {
            Label badgeC = new Label("Camion");
            badgeC.getStyleClass().add("type-badge");
            badgeC.getStyleClass().add("type-badge-c"); // Custom class for type C (green)
            typeBadgesContainer.getChildren().add(badgeC);
        }
    }

    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        marqueColumn.setCellValueFactory(new PropertyValueFactory<>("marque"));
        modeleColumn.setCellValueFactory(new PropertyValueFactory<>("modele"));
        matriculeColumn.setCellValueFactory(new PropertyValueFactory<>("matricule"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        kilometrageColumn.setCellValueFactory(new PropertyValueFactory<>("kilometrage"));
    }

    @FXML
    private void handleClearDate() {
        dateFilter.setValue(null);
    }
    @FXML
    private void handleReparations() {
        try {
            // Get the selected vehicle
            if (selectedVehicule == null) {
                Stage stage = (Stage) reparationsBtn.getScene().getWindow();
                NotificationManager.showWarning(stage, "Sélection requise", "Veuillez sélectionner un véhicule pour voir ses réparations");
                return;
            }

            // Load the Reparation.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/IHM/Reparation.fxml"));
            Parent root = loader.load();

            // Get the controller and set the vehicle ID
            ReparationController controller = loader.getController();
            controller.setVehiculeId(((Vehicule)selectedVehicule).getId());

            // Create a new scene
            Scene scene = new Scene(root);
            Stage stage = (Stage) reparationsBtn.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Gestion des Réparations - " + ((Vehicule)selectedVehicule).getMarque() + " " + ((Vehicule)selectedVehicule).getModele());
            stage.show();
        } catch (IOException e) {
            Stage stage = (Stage) reparationsBtn.getScene().getWindow();
            NotificationManager.showError(stage, "Erreur de navigation", "Erreur lors du chargement de l'interface de réparations: " + e.getMessage());
            e.printStackTrace();
        }
    }


    @FXML
    private void handleApplyFilter() {
        String statut = statutFilter.getValue();
        String type = typeFilter.getValue();

        try {
            List<Vehicule> filteredVehicules = vehiculeService.filterVehicules(type, statut);
            vehiculeTable.getItems().clear();
            vehiculeTable.getItems().addAll(filteredVehicules);

            Stage stage = (Stage) statutFilter.getScene().getWindow();
            NotificationManager.showInfo(stage, "Filtres appliqués", filteredVehicules.size() + " résultat(s) trouvé(s)");
        } catch (Exception e) {
            Stage stage = (Stage) statutFilter.getScene().getWindow();
            NotificationManager.showError(stage, "Erreur de filtrage", "Erreur lors du filtrage: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleResetFilter() {
        loadVehicules();
        statutFilter.setValue("Tous les statuts");
        typeFilter.setValue("Tous les types");

        Stage stage = (Stage) statutFilter.getScene().getWindow();
        NotificationManager.showInfo(stage, "Filtres réinitialisés", "Tous les filtres ont été réinitialisés");
    }

    private void loadVehicules() {
        try {
            vehiculeTable.getItems().clear();
            List<Vehicule> vehicules = vehiculeService.getAllVehicules();
            vehiculeTable.getItems().addAll(vehicules);

            if (vehiculeTable.getScene() != null) {
                Stage stage = (Stage) vehiculeTable.getScene().getWindow();
                NotificationManager.showInfo(stage, "Véhicules chargés", vehicules.size() + " véhicule(s) chargé(s)");
            }
        } catch (Exception e) {
            if (vehiculeTable.getScene() != null) {
                Stage stage = (Stage) vehiculeTable.getScene().getWindow();
                NotificationManager.showError(stage, "Erreur de chargement", "Erreur lors du chargement des véhicules: " + e.getMessage());
            }
            e.printStackTrace();
        }
    }

    private void populateForm(Vehicule vehicule) {
        if (vehicule == null) return;

        marqueField.setText(vehicule.getMarque());
        modeleField.setText(vehicule.getModele());
        matriculeField.setText(vehicule.getMatricule());
        kilometrageField.setText(String.valueOf(vehicule.getKilometrage()));
        dateMiseEnServicePicker.setValue(vehicule.getDateMiseEnService());
        dateProchainEntretienPicker.setValue(vehicule.getDateProchainEntretien());
        dateVignettePicker.setValue(vehicule.getDateVignette());
        dateAssurancePicker.setValue(vehicule.getDateAssurance());
        dateVisiteTechniquePicker.setValue(vehicule.getDateVisiteTechnique());
        papiersField.setText(vehicule.getPapiers());
        motifField.setText(vehicule.getMotifIndisponibilite());
        notesField.setText(vehicule.getNotes());

        // Set type radio buttons
        if (vehicule.getType() == TypePermis.Moto) {
            typeA.setSelected(true);
        } else if (vehicule.getType() == TypePermis.Voiture) {
            typeB.setSelected(true);
        } else if (vehicule.getType() == TypePermis.Camion) {
            typeC.setSelected(true);
        }

        // Set disponibilité radio buttons
        disponibleOui.setSelected(vehicule.isDisponible());
        disponibleNon.setSelected(!vehicule.isDisponible());

        vehiculeNameLabel.setText(vehicule.getMarque() + " " + vehicule.getModele() + " (" + vehicule.getMatricule() + ")");

        // Update availability indicator based on vehicule's status
        updateAvailabilityIndicator(vehicule.isDisponible());

        // Update type badges based on vehicule's type
        updateTypeBadges();
        updateVehiculePhoto(vehicule.getType());
    }

    private void clearForm() {
        marqueField.clear();
        modeleField.clear();
        matriculeField.clear();
        kilometrageField.clear();
        dateMiseEnServicePicker.setValue(null);
        dateProchainEntretienPicker.setValue(null);
        dateVignettePicker.setValue(null);
        dateAssurancePicker.setValue(null);
        dateVisiteTechniquePicker.setValue(null);
        papiersField.clear();
        motifField.clear();
        notesField.clear();

        updateVehiculePhoto(null);

        typeA.setSelected(false);
        typeB.setSelected(false);
        typeC.setSelected(false);
        disponibleOui.setSelected(false);
        disponibleNon.setSelected(false);

        selectedVehicule = null;
        vehiculeNameLabel.setText("Nouveau Véhicule");

        // Reset availability indicator to default (available)
        updateAvailabilityIndicator(true);

        // Clear type badges
        typeBadgesContainer.getChildren().clear();
    }

    private void disableForm(boolean disable) {
        marqueField.setDisable(disable);
        modeleField.setDisable(disable);
        matriculeField.setDisable(disable);
        kilometrageField.setDisable(disable);
        dateMiseEnServicePicker.setDisable(disable);
        dateProchainEntretienPicker.setDisable(disable);
        dateVignettePicker.setDisable(disable);
        dateAssurancePicker.setDisable(disable);
        dateVisiteTechniquePicker.setDisable(disable);
        papiersField.setDisable(disable);
        motifField.setDisable(disable);
        notesField.setDisable(disable);
        typeA.setDisable(disable);
        typeB.setDisable(disable);
        typeC.setDisable(disable);
        disponibleOui.setDisable(disable);
        disponibleNon.setDisable(disable);
        enregistrerBtn.setDisable(disable);
        annulerBtn.setDisable(disable);
    }

    private void setupSelectionListener() {
        vehiculeTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            selectedVehicule = newSelection;
            populateForm(selectedVehicule);
        });
    }

    @FXML
    private void toggleDashboard() {
        isDashboardVisible = !isDashboardVisible;
        formScrollPane.setVisible(!isDashboardVisible);
        dashboardPane.setVisible(isDashboardVisible);

        if (isDashboardVisible) {
            refreshDashboard();
        }
    }

    @FXML
    private void refreshDashboard() {
        try {
            List<Vehicule> vehicules = vehiculeService.getAllVehicules();

            // Update statistics
            int total = vehicules.size();
            long disponibles = vehicules.stream().filter(Vehicule::isDisponible).count();
            long indisponibles = total - disponibles;
            long expiringDocs = vehiculeService.countVehiculesWithExpiringDocuments(30); // Documents expiring in 30 days

            totalVehiculesLabel.setText(String.valueOf(total));
            vehiculesDisponiblesLabel.setText(String.valueOf(disponibles));
            vehiculesIndisponiblesLabel.setText(String.valueOf(indisponibles));
            documentsExpirantLabel.setText(String.valueOf(expiringDocs));

            // Update pie chart for vehicle types with specific colors
            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

            int typeACount = vehiculeService.countVehiculesByType(TypePermis.Moto);
            int typeBCount = vehiculeService.countVehiculesByType(TypePermis.Voiture);
            int typeCCount = vehiculeService.countVehiculesByType(TypePermis.Camion);

            pieChartData.add(new PieChart.Data("Moto (" + typeACount + ")", typeACount));
            pieChartData.add(new PieChart.Data("Voiture (" + typeBCount + ")", typeBCount));
            pieChartData.add(new PieChart.Data("Camion (" + typeCCount + ")", typeCCount));

            typeVehiculeChart.setData(pieChartData);

            // Update bar chart for document expiration
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Nombre de véhicules");

            // Get counts for each document type expiring in the next 30 days
            int vignettes = vehiculeService.getVehiculesWithExpiringVignette(30).size();
            int assurances = vehiculeService.getVehiculesWithExpiringAssurance(30).size();
            int visitesTechniques = vehiculeService.getVehiculesWithExpiringVisiteTechnique(30).size();
            int entretiens = vehiculeService.getVehiculesWithExpiringEntretien(30).size();

            series.getData().add(new XYChart.Data<>("Vignettes", vignettes));
            series.getData().add(new XYChart.Data<>("Assurances", assurances));
            series.getData().add(new XYChart.Data<>("Visites Tech.", visitesTechniques));
            series.getData().add(new XYChart.Data<>("Entretiens", entretiens));

            documentExpirationChart.getData().clear();
            documentExpirationChart.getData().add(series);

            // Apply colors to bar chart bars
            String barColor = "#bc0c0c"; // Red
            for (XYChart.Data<String, Number> data : series.getData()) {
                data.getNode().setStyle("-fx-bar-fill: " + barColor + ";");
            }

            Stage stage = (Stage) refreshDashboardBtn.getScene().getWindow();
            NotificationManager.showSuccess(stage, "Tableau de bord actualisé", "Les statistiques ont été mises à jour");
        } catch (Exception e) {
            Stage stage = (Stage) refreshDashboardBtn.getScene().getWindow();
            NotificationManager.showError(stage, "Erreur d'actualisation", "Erreur lors de l'actualisation du tableau de bord: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Event handlers
    @FXML private void handleRecherche() {
        String searchText = rechercheField.getText().toLowerCase();
        try {
            List<Vehicule> vehicules = vehiculeService.searchVehicules(searchText);
            vehiculeTable.getItems().clear();
            vehiculeTable.getItems().addAll(vehicules);

            Stage stage = (Stage) rechercheField.getScene().getWindow();
            NotificationManager.showInfo(stage, "Recherche terminée", vehicules.size() + " résultat(s) trouvé(s)");
        } catch (Exception e) {
            Stage stage = (Stage) rechercheField.getScene().getWindow();
            NotificationManager.showError(stage, "Erreur de recherche", "Erreur lors de la recherche: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML private void handleAjouter() {
        clearForm(); disableForm(false);

        Stage stage = (Stage) ajouterBtn.getScene().getWindow();
        NotificationManager.showInfo(stage, "Nouveau véhicule", "Veuillez remplir le formulaire pour ajouter un nouveau véhicule");
    }

    @FXML private void handleModifier() {
        if (selectedVehicule != null) {
            disableForm(false);

            Stage stage = (Stage) modifierBtn.getScene().getWindow();
            NotificationManager.showInfo(stage, "Modification", "Modification du véhicule: " + selectedVehicule.getMarque() + " " + selectedVehicule.getModele());
        } else {
            Stage stage = (Stage) modifierBtn.getScene().getWindow();
            NotificationManager.showWarning(stage, "Sélection requise", "Veuillez sélectionner un véhicule à modifier");
        }
    }

    @FXML
    private void handleSupprimer() {
        if (selectedVehicule != null) {
            Stage stage = (Stage) supprimerBtn.getScene().getWindow();
            boolean confirmed = ConfirmationDialog.show(
                    stage,
                    "Confirmation de suppression",
                    "Supprimer le véhicule",
                    "Êtes-vous sûr de vouloir supprimer ce véhicule ? Cette action est irréversible.",
                    ConfirmationDialog.DialogType.DELETE
            );

            if (confirmed) {
                try {
                    vehiculeService.deleteVehicule(selectedVehicule);
                    vehiculeTable.getItems().remove(selectedVehicule);
                    clearForm();

                    NotificationManager.showSuccess(stage, "Véhicule supprimé", "Le véhicule a été supprimé avec succès");
                } catch (Exception e) {
                    NotificationManager.showError(stage, "Erreur de suppression", "Erreur lors de la suppression: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } else {
            Stage stage = (Stage) supprimerBtn.getScene().getWindow();
            NotificationManager.showWarning(stage, "Sélection requise", "Veuillez sélectionner un véhicule à supprimer");
        }
    }

    @FXML private void handleAnnuler() {
        clearForm(); disableForm(true);

        Stage stage = (Stage) annulerBtn.getScene().getWindow();
        NotificationManager.showInfo(stage, "Opération annulée", "L'opération a été annulée");
    }

    public void handleBack(ActionEvent event) {
        try {
            // Get the stage
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene currentScene = ((Node) event.getSource()).getScene();

            // Preload the main view to avoid lag
            URL fxmlLocation = getClass().getResource("/IHM/MainIHM.fxml");
            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent root = loader.load();

            // Create a semi-transparent black overlay for transition
            StackPane fadeOverlay = new StackPane();
            fadeOverlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.85);"); // Darker black background
            fadeOverlay.setOpacity(0);

            // Create a container for current scene and overlay
            StackPane transitionRoot = new StackPane();
            transitionRoot.getChildren().addAll(currentScene.getRoot(), fadeOverlay);

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
                PauseTransition pause = new PauseTransition(Duration.millis(100));
                pause.setOnFinished(pe -> {
                    // Prepare the new scene with black background to avoid white flash
                    StackPane rootWrapper = new StackPane(root);
                    rootWrapper.setStyle("-fx-background-color: black;");
                    root.setOpacity(0);
                    Scene newScene = new Scene(rootWrapper, currentScene.getWidth(), currentScene.getHeight());

                    // Set the scene
                    stage.setTitle("Menu Principal");
                    stage.setScene(newScene);

                    // Fade in the new view - smoother with longer duration
                    FadeTransition fadeInNewView = new FadeTransition(Duration.millis(400), root);
                    fadeInNewView.setFromValue(0);
                    fadeInNewView.setToValue(1);
                    fadeInNewView.setInterpolator(Interpolator.EASE_OUT);
                    fadeInNewView.play();
                });
                pause.play();
            });

            parallelIn.play();
        } catch (Exception e) {
            e.printStackTrace();
            // Fallback to direct scene change if animation fails
            try {
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                URL fxmlLocation = getClass().getResource("/IHM/MainIHM.fxml");
                FXMLLoader loader = new FXMLLoader(fxmlLocation);
                Parent root = loader.load();
                stage.setTitle("Menu Principal");
                stage.setScene(new Scene(root));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    @FXML
    private void handleExport() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Exporter les données");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
            File file = fileChooser.showSaveDialog(null);

            if (file != null) {
                exportToCSV(file);

                Stage stage = (Stage) exportBtn.getScene().getWindow();
                NotificationManager.showSuccess(stage, "Export réussi", "Données exportées avec succès vers " + file.getName());
            }
        } catch (Exception e) {
            Stage stage = (Stage) exportBtn.getScene().getWindow();
            NotificationManager.showError(stage, "Erreur d'exportation", "Erreur lors de l'exportation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void exportToCSV(File file) throws IOException {
        try (FileWriter writer = new FileWriter(file)) {
            // Write header
            writer.write("ID,Marque,Modèle,Matricule,Kilométrage,Type,Date mise en service,Date prochain entretien," +
                    "Date vignette,Date assurance,Date visite technique,Disponible,Motif indisponibilité,Notes\n");

            // Write data
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            for (Vehicule vehicule : vehiculeTable.getItems()) {
                writer.write(String.format("%d,%s,%s,%s,%d,%s,%s,%s,%s,%s,%s,%b,%s,%s\n",
                        vehicule.getId(),
                        vehicule.getMarque().replace(",", ";"),
                        vehicule.getModele().replace(",", ";"),
                        vehicule.getMatricule(),
                        vehicule.getKilometrage(),
                        vehicule.getType() != null ? vehicule.getType().name() : "",
                        vehicule.getDateMiseEnService() != null ? vehicule.getDateMiseEnService().format(formatter) : "",
                        vehicule.getDateProchainEntretien() != null ? vehicule.getDateProchainEntretien().format(formatter) : "",
                        vehicule.getDateVignette() != null ? vehicule.getDateVignette().format(formatter) : "",
                        vehicule.getDateAssurance() != null ? vehicule.getDateAssurance().format(formatter) : "",
                        vehicule.getDateVisiteTechnique() != null ? vehicule.getDateVisiteTechnique().format(formatter) : "",
                        vehicule.isDisponible(),
                        vehicule.getMotifIndisponibilite() != null ? vehicule.getMotifIndisponibilite().replace(",", ";") : "",
                        vehicule.getNotes() != null ? vehicule.getNotes().replace(",", ";").replace("\n", " ") : ""));
            }
        }
    }

    @FXML
    private void handleEnregistrer() {
        if (validateForm()) {
            TypePermis type = null;
            if (typeA.isSelected()) type = TypePermis.Moto;
            else if (typeB.isSelected()) type = TypePermis.Voiture;
            else if (typeC.isSelected()) type = TypePermis.Camion;

            boolean isDisponible = disponibleOui.isSelected();

            try {
                // Handle optional fields
                String motif = motifField.getText();
                String papiers = papiersField.getText();
                String notes = notesField.getText();

                // Parse kilometrage - this is required
                int kilometrage = Integer.parseInt(kilometrageField.getText());

                Vehicule vehicule;
                if (selectedVehicule == null) {
                    // Create new vehicule
                    vehicule = new Vehicule(
                            null, marqueField.getText(), modeleField.getText(), matriculeField.getText(),
                            kilometrage, type, dateMiseEnServicePicker.getValue(),
                            dateProchainEntretienPicker.getValue(), dateVignettePicker.getValue(),
                            dateAssurancePicker.getValue(), dateVisiteTechniquePicker.getValue(),
                            papiers, isDisponible, motif, notes
                    );
                } else {
                    // Update existing vehicule
                    vehicule = selectedVehicule;
                    vehicule.setMarque(marqueField.getText());
                    vehicule.setModele(modeleField.getText());
                    vehicule.setMatricule(matriculeField.getText());
                    vehicule.setKilometrage(kilometrage);
                    vehicule.setType(type);
                    vehicule.setDateMiseEnService(dateMiseEnServicePicker.getValue());
                    vehicule.setDateProchainEntretien(dateProchainEntretienPicker.getValue());
                    vehicule.setDateVignette(dateVignettePicker.getValue());
                    vehicule.setDateAssurance(dateAssurancePicker.getValue());
                    vehicule.setDateVisiteTechnique(dateVisiteTechniquePicker.getValue());
                    vehicule.setPapiers(papiers);
                    vehicule.setDisponible(isDisponible);
                    vehicule.setMotifIndisponibilite(motif);
                    vehicule.setNotes(notes);
                }

                // Check uniqueness before saving
                List<String> uniquenessErrors = vehiculeService.validateUniqueness(vehicule);
                if (!uniquenessErrors.isEmpty()) {
                    Stage stage = (Stage) enregistrerBtn.getScene().getWindow();
                    NotificationManager.showError(stage, "Erreur de validation",
                            String.join("\n", uniquenessErrors));
                    return;
                }

                // Create confirmation dialog
                Stage stage = (Stage) enregistrerBtn.getScene().getWindow();
                boolean confirmed = ConfirmationDialog.show(
                        stage,
                        "Confirmation d'enregistrement",
                        "Enregistrer le véhicule",
                        "Êtes-vous sûr de vouloir enregistrer ces informations ?",
                        ConfirmationDialog.DialogType.SAVE
                );

                if (confirmed) {
                    vehicule = vehiculeService.saveVehicule(vehicule);

                    // Refresh the table
                    loadVehicules();
                    disableForm(true);

                    if (selectedVehicule == null) {
                        NotificationManager.showSuccess(stage, "Véhicule ajouté", "Le véhicule a été ajouté avec succès");
                    } else {
                        NotificationManager.showSuccess(stage, "Véhicule modifié", "Le véhicule a été modifié avec succès");
                    }
                }
            } catch (NumberFormatException e) {
                Stage stage = (Stage) enregistrerBtn.getScene().getWindow();
                NotificationManager.showError(stage, "Erreur de format", "Le kilométrage doit être un nombre entier valide");
            } catch (Exception e) {
                Stage stage = (Stage) enregistrerBtn.getScene().getWindow();
                NotificationManager.showError(stage, "Erreur d'enregistrement", "Erreur lors de l'enregistrement: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Open the help window focused on the relevant section
     */
    @FXML
    public void handleHelp(ActionEvent event) {
        try {
            // Load the help view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/IHM/HelpIHM.fxml"));
            Parent helpRoot = loader.load();

            // Get the controller
            HelpController helpController = loader.getController();

            // Navigate to the vehicules section
            helpController.navigateToSection("vehicules_add");

            // Get the current stage
            Stage currentStage = null;
            if (event.getSource() instanceof Node) {
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
            if (event.getSource() instanceof Node) {
                stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            }

            if (stage != null) {
                NotificationManager.showError(stage, "Erreur",
                        "Impossible de charger l'aide: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleSettings(ActionEvent event) {
        try {
            // Load the settings FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/IHM/SettingsIHM.fxml"));
            Parent root = loader.load();

            // Create a new stage for the settings window
            Stage settingsStage = new Stage();
            settingsStage.setTitle("Paramètres - Auto-École Pro");

            // Set minimum dimensions
            settingsStage.setMinWidth(600);
            settingsStage.setMinHeight(400);

            // Create the scene
            Scene scene = new Scene(root);

            // Add stylesheet
            scene.getStylesheets().add(getClass().getResource("/Styles/Common.css").toExternalForm());

            settingsStage.setScene(scene);

            // Set modality (makes the settings window modal)
            settingsStage.initModality(Modality.APPLICATION_MODAL);

            // Set owner (sets the parent window)
            settingsStage.initOwner(((Node) event.getSource()).getScene().getWindow());

            // Show the settings window
            settingsStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            // Show error alert
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Impossible d'ouvrir les paramètres");
            alert.setContentText("Une erreur s'est produite lors de l'ouverture des paramètres: " + e.getMessage());
            alert.showAndWait();
        }
    }

    private void setupKeyboardShortcuts() {
        Platform.runLater(() -> {
            if (vehiculeTable.getScene() != null) {
                vehiculeTable.getScene().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                    // Ctrl+F to focus on search field
                    if (event.isControlDown() && event.getCode() == KeyCode.F) {
                        rechercheField.requestFocus();
                        event.consume();
                    }

                    // F1 for help
                    if (event.getCode() == KeyCode.F1) {
                        handleHelp(new ActionEvent(vehiculeTable, null));
                        event.consume();
                    }
                    // F2 for settings
                    if (event.getCode() == KeyCode.F2) {
                        handleSettings(new ActionEvent(vehiculeTable, null));
                        event.consume();
                    }

                    // Ctrl+N for new vehicule
                    if (event.isControlDown() && event.getCode() == KeyCode.N) {
                        handleAjouter();
                        event.consume();
                    }

                    // Ctrl+E to edit selected vehicule
                    if (event.isControlDown() && event.getCode() == KeyCode.E) {
                        if (selectedVehicule != null) {
                            handleModifier();
                            event.consume();
                        }
                    }

                    // Ctrl+S to save changes
                    if (event.isControlDown() && event.getCode() == KeyCode.S) {
                        if (!enregistrerBtn.isDisabled()) {
                            handleEnregistrer();
                            event.consume();
                        }
                    }

                    // Escape to cancel/clear form
                    if (event.getCode() == KeyCode.ESCAPE) {
                        handleAnnuler();
                        event.consume();
                    }

                    // Ctrl+D to toggle dashboard
                    if (event.isControlDown() && event.getCode() == KeyCode.D) {
                        toggleDashboard();
                        event.consume();
                    }

                    // Ctrl+R to refresh the vehicule list
                    if (event.isControlDown() && event.getCode() == KeyCode.R) {
                        loadVehicules();
                        event.consume();
                    }

                    // Alt+B to go back to main menu
                    if (event.isAltDown() && event.getCode() == KeyCode.B) {
                        handleBack(new ActionEvent(vehiculeTable, null));
                        event.consume();
                    }

                    // Ctrl+M to manage repairs for the selected vehicle
                    if (event.isControlDown() && event.getCode() == KeyCode.M) {
                        if (selectedVehicule != null) {
                            handleReparations();
                            event.consume();
                        }
                    }
                });
            }
        });
    }

    private boolean validateForm() {
        boolean isValid = true;
        Label tempErrorLabel = new Label();

        // Validate brand and model fields
        if (!Verification.validateRequired(marqueField, tempErrorLabel)) {
            Stage stage = (Stage) marqueField.getScene().getWindow();
            NotificationManager.showWarning(stage, "Validation", "La marque est requise");
            isValid = false;
        }

        if (!Verification.validateRequired(modeleField, tempErrorLabel)) {
            Stage stage = (Stage) modeleField.getScene().getWindow();
            NotificationManager.showWarning(stage, "Validation", "Le modèle est requis");
            isValid = false;
        }



        // Validate matricule
        if ((!Verification.validateRequired(matriculeField, tempErrorLabel))||(!Verification.validateTunisianLicensePlate(matriculeField, tempErrorLabel))) {
            Stage stage = (Stage) matriculeField.getScene().getWindow();
            NotificationManager.showWarning(stage, "Validation", "Format d'immatriculation tunisienne invalide (ex: 123 TUN 4567)");
            isValid = false;
        }

        // Validate kilometrage
        if (!Verification.validateNumeric(kilometrageField, tempErrorLabel, 0)) {
            Stage stage = (Stage) kilometrageField.getScene().getWindow();
            NotificationManager.showWarning(stage, "Validation", "Le kilométrage doit être un nombre positif");
            isValid = false;
        }

        // Validate date mise en service
        if ((dateMiseEnServicePicker.getValue() == null)||(!Verification.validatePastOrPresentDate(dateMiseEnServicePicker, tempErrorLabel))) {
            dateMiseEnServicePicker.setStyle("-fx-border-color: #ef4444; -fx-border-width: 1px;");
            Stage stage = (Stage) dateMiseEnServicePicker.getScene().getWindow();
            NotificationManager.showWarning(stage, "Validation", "La date de mise en service est requise et doit etre dans le pass");
            isValid = false;
        } else {
            dateMiseEnServicePicker.setStyle("");
        }

        // Validate at least one type is selected
        if (!typeA.isSelected() && !typeB.isSelected() && !typeC.isSelected()) {
            Stage stage = (Stage) typeA.getScene().getWindow();
            NotificationManager.showWarning(stage, "Validation", "Veuillez sélectionner un type de véhicule");
            isValid = false;
        }

        // Validate disponibilité selection
        if (!disponibleOui.isSelected() && !disponibleNon.isSelected()) {
            Stage stage = (Stage) disponibleOui.getScene().getWindow();
            NotificationManager.showWarning(stage, "Validation", "Veuillez sélectionner la disponibilité");
            isValid = false;
        }

        return isValid;
    }

    private void applyRoleBasedAccess() {
        User currentUser = SessionManager.getCurrentUser();

        if (currentUser == null) {
            // Should not happen, but if no user is logged in, disable sensitive actions
            supprimerBtn.setDisable(true);
            dashboardBtn.setDisable(true);
            return;
        }

        // Secretary cannot delete vehicles
        supprimerBtn.setDisable(!currentUser.canDeleteCandidats());
    }
}

