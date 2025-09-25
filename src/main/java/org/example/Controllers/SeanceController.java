package org.example.Controllers;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
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
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import netscape.javascript.JSObject;
import org.example.Entities.*;
import org.example.Rep.CandidatRep;
import org.example.Rep.MoniteurRep;
import org.example.Rep.SeanceRep;
import org.example.Rep.VehiculeRep;
import org.example.Service.AuditLogService;
import org.example.Service.CandidatService;
import org.example.Service.MoniteurService;
import org.example.Service.SeanceService;
import org.example.Service.VehiculeService;
import org.example.Utils.AccessControl;
import org.example.Utils.ConfirmationDialog;
import org.example.Utils.NotificationManager;
import org.example.Utils.SessionManager;
import org.example.Utils.Verification;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Contrôleur pour la gestion des séances
 */
public class SeanceController implements Initializable {
    // Services
    private SeanceService seanceService;
    private CandidatService candidatService;
    private MoniteurService moniteurService;
    private VehiculeService vehiculeService;
    private AuditLogService auditLogService;
    private Seance selectedSeance;

    // Données
    private ObservableList<Seance> seances = FXCollections.observableArrayList();
    private boolean modeModification = false;
    private boolean isDashboardVisible = false;

    // Coordonnées sélectionnées sur la carte
    private float selectedLatitude = 0;
    private float selectedLongitude = 0;
    private String selectedAddress = "";

    // Map to store objects by ID for quick lookup
    private Map<Long, Candidat> candidatMap = new HashMap<>();
    private Map<Long, Moniteur> moniteurMap = new HashMap<>();
    private Map<Integer, Vehicule> vehiculeMap = new HashMap<>();

    // Random color generator for charts
    private Random random = new Random();

    // Formatters
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // Table components
    @FXML private TableView<Seance> seanceTable;
    @FXML private TableColumn<Seance, Integer> idColumn;
    @FXML private TableColumn<Seance, String> typeSeanceColumn;
    @FXML private TableColumn<Seance, String> typePermisColumn;
    @FXML private TableColumn<Seance, String> dateDebutColumn;
    @FXML private TableColumn<Seance, String> candidatNameColumn;
    @FXML private TableColumn<Seance, String> moniteurNameColumn;
    @FXML private TableColumn<Seance, String> vehiculeInfoColumn;
    @FXML private TableColumn<Seance, String> adresseColumn;

    // Form components
    @FXML private ComboBox<TypeSeance> typeSeanceComboBox;
    @FXML private ComboBox<TypePermis> typePermisComboBox;
    @FXML private ComboBox<Candidat> candidatComboBox;
    @FXML private ComboBox<Moniteur> moniteurComboBox;
    @FXML private ComboBox<Vehicule> vehiculeComboBox;
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<String> heureComboBox;
    @FXML private TextField rechercheField;
    @FXML private TextField adresseField;
    @FXML private Button chooseLocationBtn;
    @FXML private Button changeLocationBtn;
    @FXML private Label locationInfoLabel;

    // Map components
    @FXML private WebView mapWebView;
    @FXML private Label latitudeLabel;
    @FXML private Label longitudeLabel;

    // Filter components
    @FXML private ComboBox<TypeSeance> filterTypeSeanceComboBox;
    @FXML private ComboBox<TypePermis> filterTypePermisComboBox;
    @FXML private ComboBox<Candidat> filterCandidatComboBox;
    @FXML private ComboBox<Moniteur> filterMoniteurComboBox;
    @FXML private ComboBox<Vehicule> filterVehiculeComboBox;
    @FXML private DatePicker filterStartDatePicker;
    @FXML private DatePicker filterEndDatePicker;

    // Action buttons
    @FXML private Button ajouterBtn, modifierBtn, supprimerBtn, annulerBtn, enregistrerBtn, backBtn,
            rechercheBtn, resetFilterBtn, applyFilterBtn, exportBtn, dashboardBtn, refreshDashboardBtn;

    // Dashboard components
    @FXML private VBox dashboardPane;
    @FXML private ScrollPane formScrollPane;
    @FXML private PieChart typeSeanceChart;
    @FXML private BarChart<String, Number> seancesParMoisChart;
    @FXML private Label totalSeancesDashboardLabel;
    @FXML private Label seancesCodeDashboardLabel;
    @FXML private Label seancesConduiteDashboardLabel;

    // Map dialog stage
    private Stage mapDialogStage;
    private WebView mapDialogWebView;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize services
        SeanceRep seanceRep = new SeanceRep();
        VehiculeRep vehiculeRep = new VehiculeRep();
        MoniteurRep moniteurRep = new MoniteurRep();
        CandidatRep candidatRep = new CandidatRep();

        seanceService = new SeanceService(seanceRep, vehiculeRep, moniteurRep);
        candidatService = new CandidatService(candidatRep);
        moniteurService = new MoniteurService(moniteurRep);
        vehiculeService = new VehiculeService(vehiculeRep);
        auditLogService = new AuditLogService();
        selectedSeance = new Seance();

        // Apply role-based access control
        applyRoleBasedAccess();

        // Setup UI components
        setupTableColumns();
        setupComboBoxes();
        setupLocationControls();

        // Load data
        loadAllData();
        setupSelectionListener();
        clearForm();
        disableForm(true);

        // Setup keyboard shortcuts
        setupKeyboardShortcuts();

        // Add listeners for date and time changes
        datePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && heureComboBox.getValue() != null) {
                filterAvailableMoniteurs();
                filterAvailableVehicules();
                filterAvailableCandidats();
            }
        });

        heureComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && datePicker.getValue() != null) {
                filterAvailableMoniteurs();
                filterAvailableVehicules();
                filterAvailableCandidats();
            }
        });

        // Add listener for type permis changes
        typePermisComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                // Filter moniteurs by type of permit
                filterAvailableMoniteurs();

                // Filter vehicles by type of permit if it's a driving session
                if (typeSeanceComboBox.getValue() == TypeSeance.Conduite) {
                    filterAvailableVehicules();
                }
            }
        });
    }

    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id_seance"));

        typeSeanceColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getTypeseance().toString()));

        typePermisColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getTypepermis().toString()));

        dateDebutColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(formatDateTime(cellData.getValue().getDate_debut())));

        candidatNameColumn.setCellValueFactory(cellData -> {
            Candidat candidat = candidatMap.get(cellData.getValue().getId_candidat());
            String candidatName = candidat != null ? candidat.getNom() + " " + candidat.getPrenom() : "Inconnu";
            return new SimpleStringProperty(candidatName);
        });

        moniteurNameColumn.setCellValueFactory(cellData -> {
            Moniteur moniteur = moniteurMap.get(cellData.getValue().getId_moniteur());
            String moniteurName = moniteur != null ? moniteur.getNom() + " " + moniteur.getPrenom() : "Inconnu";
            return new SimpleStringProperty(moniteurName);
        });

        vehiculeInfoColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getId_vehicule() == null) {
                return new SimpleStringProperty("N/A");
            }
            Vehicule vehicule = vehiculeMap.get(cellData.getValue().getId_vehicule());
            String vehiculeInfo = vehicule != null ?
                    vehicule.getMarque() + " " + vehicule.getModele() + " (" + vehicule.getMatricule() + ")" : "Inconnu";
            return new SimpleStringProperty(vehiculeInfo);
        });

        adresseColumn.setCellValueFactory(cellData -> {
            String adresse = cellData.getValue().getAdresse();
            return new SimpleStringProperty(adresse != null ? adresse : "");
        });
    }

    private void setupComboBoxes() {
        // Type de séance
        typeSeanceComboBox.setItems(FXCollections.observableArrayList(TypeSeance.values()));
        filterTypeSeanceComboBox.setItems(FXCollections.observableArrayList(TypeSeance.values()));

        // Type de permis
        typePermisComboBox.setItems(FXCollections.observableArrayList(TypePermis.values()));
        filterTypePermisComboBox.setItems(FXCollections.observableArrayList(TypePermis.values()));

        // Heures
        List<String> heures = new ArrayList<>();
        for (int h = 8; h <= 18; h++) {
            heures.add(String.format("%02d:00", h));
            if (h < 18) heures.add(String.format("%02d:30", h));
        }
        heureComboBox.setItems(FXCollections.observableArrayList(heures));
        heureComboBox.setValue("08:00");

        // Add listeners
        typeSeanceComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == TypeSeance.Code) {
                vehiculeComboBox.setDisable(true);
                vehiculeComboBox.setValue(null);
                chooseLocationBtn.setDisable(true);
                changeLocationBtn.setDisable(true);
                adresseField.setDisable(true);
                adresseField.clear();
                selectedLatitude = 0;
                selectedLongitude = 0;
                selectedAddress = "";
                latitudeLabel.setText("0.000000");
                longitudeLabel.setText("0.000000");
                locationInfoLabel.setText("Lieu de rendez-vous non requis pour les séances de code");
            } else {
                // Only enable vehicle and location fields if in modification mode AND it's a driving session
                boolean shouldEnable = modeModification && newVal == TypeSeance.Conduite;
                vehiculeComboBox.setDisable(!shouldEnable);
                chooseLocationBtn.setDisable(!shouldEnable);
                changeLocationBtn.setDisable(!shouldEnable || (selectedLatitude == 0 && selectedLongitude == 0));
                adresseField.setDisable(!shouldEnable);

                if (newVal == TypeSeance.Conduite) {
                    locationInfoLabel.setText("Aucun lieu de rendez-vous sélectionné");

                    // Filter vehicles by availability and type of permit
                    if (shouldEnable) {
                        filterAvailableVehicules();
                    }
                }
            }

            // Filter moniteurs by type of permit
            if (typePermisComboBox.getValue() != null) {
                filterAvailableMoniteurs();
            }
        });
    }

    private void setupLocationControls() {
        // Initialize location controls
        chooseLocationBtn = new Button("Choisir un lieu de rendez-vous");
        chooseLocationBtn.getStyleClass().add("location-button");
        chooseLocationBtn.setOnAction(e -> openMapDialog());
        chooseLocationBtn.setDisable(true);

        changeLocationBtn = new Button("Changer le lieu");
        changeLocationBtn.getStyleClass().add("location-button");
        changeLocationBtn.setOnAction(e -> openMapDialog());
        changeLocationBtn.setDisable(true);

        locationInfoLabel = new Label("Aucun lieu de rendez-vous sélectionné");
        locationInfoLabel.getStyleClass().add("location-info");
    }

    /**
     * Filters moniteurs based on availability for the selected date and time
     * Now also checks if the moniteur is generally available (not on leave, etc.)
     */
    private void filterAvailableMoniteurs() {
        try {
            if (datePicker.getValue() == null || heureComboBox.getValue() == null) {
                return;
            }

            // Create a LocalDateTime from the selected date and time
            LocalDate selectedDate = datePicker.getValue();
            LocalTime selectedTime = LocalTime.parse(heureComboBox.getValue());
            LocalDateTime selectedDateTime = LocalDateTime.of(selectedDate, selectedTime);

            // Get all moniteurs
            List<Moniteur> allMoniteurs = moniteurService.getAllMoniteurs();

            // First filter: only show moniteurs that are generally available (not on leave, etc.)
            List<Moniteur> generallyAvailableMoniteurs = allMoniteurs.stream()
                    .filter(Moniteur::isDisponible)
                    .collect(Collectors.toList());

            // Second filter: check if they're available at the specific time
            List<Moniteur> availableMoniteurs = generallyAvailableMoniteurs.stream()
                    .filter(m -> seanceService.isMoniteurAvailableAt(m.getId(), selectedDateTime))
                    .collect(Collectors.toList());

            // Third filter: check if they have the required permit type
            if (typePermisComboBox.getValue() != null) {
                String permisCategory = seanceService.convertTypePermisToCategory(typePermisComboBox.getValue());
                availableMoniteurs = availableMoniteurs.stream()
                        .filter(m -> m.getCategoriesPermis().contains(permisCategory))
                        .collect(Collectors.toList());
            }

            // If we're editing an existing seance, include the currently selected moniteur
            if (selectedSeance != null && selectedSeance.getId_seance() != 0) {
                Moniteur currentMoniteur = moniteurMap.get(selectedSeance.getId_moniteur());
                if (currentMoniteur != null && !availableMoniteurs.contains(currentMoniteur)) {
                    availableMoniteurs.add(currentMoniteur);
                }
            }

            moniteurComboBox.setItems(FXCollections.observableArrayList(availableMoniteurs));

            // Clear selection if current selection is not available
            if (moniteurComboBox.getValue() != null &&
                    !availableMoniteurs.contains(moniteurComboBox.getValue())) {
                moniteurComboBox.setValue(null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Stage stage = (Stage) moniteurComboBox.getScene().getWindow();
            NotificationManager.showError(stage, "Erreur",
                    "Erreur lors du filtrage des moniteurs disponibles: " + e.getMessage());
        }
    }

    /**
     * Filters candidates based on availability for the selected date and time
     */
    private void filterAvailableCandidats() {
        try {
            if (datePicker.getValue() == null || heureComboBox.getValue() == null) {
                return;
            }

            // Create a LocalDateTime from the selected date and time
            LocalDate selectedDate = datePicker.getValue();
            LocalTime selectedTime = LocalTime.parse(heureComboBox.getValue());
            LocalDateTime selectedDateTime = LocalDateTime.of(selectedDate, selectedTime);

            // Get all candidates
            List<Candidat> allCandidats = candidatService.getAllCandidats();

            // Filter: only show active candidates
            List<Candidat> activeCandidats = allCandidats.stream()
                    .filter(Candidat::isActif)
                    .collect(Collectors.toList());

            // Filter: check if they're available at the specific time
            List<Candidat> availableCandidats = activeCandidats.stream()
                    .filter(c -> isCandidatAvailableAt(c.getId(), selectedDateTime))
                    .collect(Collectors.toList());

            // If we're editing an existing seance, include the currently selected candidat
            if (selectedSeance != null && selectedSeance.getId_seance() != 0) {
                Candidat currentCandidat = candidatMap.get(selectedSeance.getId_candidat());
                if (currentCandidat != null && !availableCandidats.contains(currentCandidat)) {
                    availableCandidats.add(currentCandidat);
                }
            }

            candidatComboBox.setItems(FXCollections.observableArrayList(availableCandidats));

            // Clear selection if current selection is not available
            if (candidatComboBox.getValue() != null &&
                    !availableCandidats.contains(candidatComboBox.getValue())) {
                candidatComboBox.setValue(null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Stage stage = (Stage) candidatComboBox.getScene().getWindow();
            NotificationManager.showError(stage, "Erreur",
                    "Erreur lors du filtrage des candidats disponibles: " + e.getMessage());
        }
    }

    /**
     * Filters vehicles based on availability for the selected date and time
     * Now also checks if the vehicle is generally available (not in repair, etc.)
     */
    private void filterAvailableVehicules() {
        try {
            if (datePicker.getValue() == null || heureComboBox.getValue() == null ||
                    typeSeanceComboBox.getValue() != TypeSeance.Conduite) {
                return;
            }

            // Create a LocalDateTime from the selected date and time
            LocalDate selectedDate = datePicker.getValue();
            LocalTime selectedTime = LocalTime.parse(heureComboBox.getValue());
            LocalDateTime selectedDateTime = LocalDateTime.of(selectedDate, selectedTime);

            // Get all vehicles
            List<Vehicule> allVehicules = vehiculeService.getAllVehicules();

            // First filter: only show vehicles that are generally available (not in repair, etc.)
            List<Vehicule> generallyAvailableVehicules = allVehicules.stream()
                    .filter(Vehicule::isDisponible)
                    .collect(Collectors.toList());

            // Second filter: check if they're available at the specific time
            List<Vehicule> availableVehicules = generallyAvailableVehicules.stream()
                    .filter(v -> seanceService.isVehiculeAvailableAt(v.getId().intValue(), selectedDateTime))
                    .collect(Collectors.toList());

            // Third filter: check if they match the required permit type
            if (typePermisComboBox.getValue() != null) {
                availableVehicules = availableVehicules.stream()
                        .filter(v -> v.getType() == typePermisComboBox.getValue())
                        .collect(Collectors.toList());
            }

            // If we're editing an existing seance, include the currently selected vehicle
            if (selectedSeance != null && selectedSeance.getId_seance() != 0 && selectedSeance.getId_vehicule() != null) {
                Vehicule currentVehicule = vehiculeMap.get(selectedSeance.getId_vehicule());
                if (currentVehicule != null && !availableVehicules.contains(currentVehicule)) {
                    availableVehicules.add(currentVehicule);
                }
            }

            vehiculeComboBox.setItems(FXCollections.observableArrayList(availableVehicules));

            // Clear selection if current selection is not available
            if (vehiculeComboBox.getValue() != null &&
                    !availableVehicules.contains(vehiculeComboBox.getValue())) {
                vehiculeComboBox.setValue(null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Stage stage = (Stage) vehiculeComboBox.getScene().getWindow();
            NotificationManager.showError(stage, "Erreur",
                    "Erreur lors du filtrage des véhicules disponibles: " + e.getMessage());
        }
    }

    private void filterVehiclesByPermisType(TypePermis typePermis) {
        try {
            List<Vehicule> allVehicules = vehiculeService.getAllVehicules();

            // Filter by type and general availability
            List<Vehicule> filteredVehicules = allVehicules.stream()
                    .filter(v -> v.getType() == typePermis && v.isDisponible())
                    .collect(Collectors.toList());

            vehiculeComboBox.setItems(FXCollections.observableArrayList(filteredVehicules));

            // If current selection doesn't match the filter, clear it
            if (vehiculeComboBox.getValue() != null && vehiculeComboBox.getValue().getType() != typePermis) {
                vehiculeComboBox.setValue(null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Stage stage = (Stage) vehiculeComboBox.getScene().getWindow();
            NotificationManager.showError(stage, "Erreur", "Erreur lors du filtrage des véhicules: " + e.getMessage());
        }
    }

    private void filterMoniteursByPermisType(TypePermis typePermis) {
        try {
            // Convert TypePermis to the string format used in Moniteur (A, B, C)
            String permisCategory = seanceService.convertTypePermisToCategory(typePermis);

            // Get all moniteurs
            List<Moniteur> allMoniteurs = moniteurService.getAllMoniteurs();

            // Filter moniteurs by category and general availability
            List<Moniteur> filteredMoniteurs = allMoniteurs.stream()
                    .filter(m -> m.isDisponible() && m.getCategoriesPermis().contains(permisCategory))
                    .collect(Collectors.toList());

            moniteurComboBox.setItems(FXCollections.observableArrayList(filteredMoniteurs));

            // If current selection doesn't have the required category, clear it
            if (moniteurComboBox.getValue() != null && !moniteurComboBox.getValue().getCategoriesPermis().contains(permisCategory)) {
                moniteurComboBox.setValue(null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Stage stage = (Stage) moniteurComboBox.getScene().getWindow();
            NotificationManager.showError(stage, "Erreur", "Erreur lors du filtrage des moniteurs: " + e.getMessage());
        }
    }


    // Fix the openMapDialog method to improve the map display and address retrieval
    @FXML
    private void openMapDialog() {
        try {
            // Create a new stage for the map
            mapDialogStage = new Stage();
            mapDialogStage.setTitle("Choisir un lieu de rendez-vous");
            mapDialogStage.initModality(Modality.APPLICATION_MODAL);

            // Set the owner to the current stage
            if (chooseLocationBtn.getScene() != null && chooseLocationBtn.getScene().getWindow() instanceof Stage) {
                mapDialogStage.initOwner((Stage) chooseLocationBtn.getScene().getWindow());
            }

            // Create a WebView for the map
            mapDialogWebView = new WebView();
            mapDialogWebView.setPrefSize(800, 600);

            // Create a root layout with a button at the bottom
            VBox root = new VBox(0); // No spacing between elements
            root.setAlignment(javafx.geometry.Pos.CENTER);

            // Add the WebView
            root.getChildren().add(mapDialogWebView);

            // Add a button to confirm the selection
            Button confirmButton = new Button("Confirmer la sélection");
            confirmButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10px 20px;");
            confirmButton.setOnAction(e -> {
                // Execute JavaScript to get the current marker position
                try {
                    Object result = mapDialogWebView.getEngine().executeScript("getCurrentMarkerPosition()");
                    if (result != null && result instanceof String) {
                        String[] parts = ((String) result).split(",", 3); // Split into max 3 parts
                        if (parts.length >= 3) {
                            double lat = Double.parseDouble(parts[0]);
                            double lng = Double.parseDouble(parts[1]);
                            String address = parts[2];

                            // Call the onLocationSelected method directly
                            new MapBridge().onLocationSelected(lat, lng, address);
                        }
                    } else {
                        NotificationManager.showWarning(mapDialogStage, "Sélection requise",
                                "Veuillez d'abord sélectionner un point sur la carte");
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    NotificationManager.showError(mapDialogStage, "Erreur",
                            "Veuillez d'abord sélectionner un point sur la carte");
                }
            });

            // Add the button to the layout
            HBox buttonBox = new HBox(confirmButton);
            buttonBox.setAlignment(javafx.geometry.Pos.CENTER);
            buttonBox.setPadding(new javafx.geometry.Insets(10, 0, 10, 0));
            root.getChildren().add(buttonBox);

            // Create scene and set it on the stage
            Scene scene = new Scene(root, 800, 650);
            mapDialogStage.setScene(scene);

            // Load the map HTML directly
            WebEngine webEngine = mapDialogWebView.getEngine();

            // Create the HTML content with the getCurrentMarkerPosition function
            String mapHtml = "<!DOCTYPE html>\n" +
                    "<html>\n" +
                    "<head>\n" +
                    "    <title>Carte de rendez-vous</title>\n" +
                    "    <meta charset=\"utf-8\" />\n" +
                    "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                    "    <link rel=\"stylesheet\" href=\"https://unpkg.com/leaflet@1.7.1/dist/leaflet.css\" />\n" +
                    "    <script src=\"https://unpkg.com/leaflet@1.7.1/dist/leaflet.js\"></script>\n" +
                    "    <style>\n" +
                    "html, body {\n" +
                    "    height: 100%;\n" +
                    "    margin: 0;\n" +
                    "    padding: 0;\n" +
                    "}\n" +
                    "#map {\n" +
                    "    height: 100%;\n" +
                    "    width: 100%;\n" +
                    "}\n" +
                    ".info-box {\n" +
                    "    padding: 15px;\n" +
                    "    background: white;\n" +
                    "    border-radius: 8px;\n" +
                    "    box-shadow: 0 0 15px rgba(0,0,0,0.2);\n" +
                    "    max-width: 300px;\n" +
                    "}\n" +
                    ".info-box h4 {\n" +
                    "    margin: 0 0 10px;\n" +
                    "    color: #3498db;\n" +
                    "    font-size: 16px;\n" +
                    "}\n" +
                    ".info-box p {\n" +
                    "    margin: 5px 0;\n"+
                    "}\n" +
                    ".info-box p {\n" +
                    "    margin: 5px 0;\n" +
                    "    font-size: 14px;\n" +
                    "}\n" +
                    ".instructions {\n" +
                    "    background-color: #f8f9fa;\n" +
                    "    padding: 10px;\n" +
                    "    border-radius: 5px;\n" +
                    "    margin-bottom: 10px;\n" +
                    "    text-align: center;\n" +
                    "}\n" +
                    "    </style>\n" +
                    "</head>\n" +
                    "<body>\n" +
                    "    <div class=\"instructions\">Cliquez sur la carte pour sélectionner un lieu de rendez-vous, puis cliquez sur \"Confirmer la sélection\"</div>\n" +
                    "    <div id=\"map\"></div>\n" +
                    "    <script>\n" +
                    "        var map;\n" +
                    "        var markers = [];\n" +
                    "        var currentMarkerData = null;\n" +
                    "        \n" +
                    "        function initMap() {\n" +
                    "            // Initialize the map centered on Tunisia\n" +
                    "            map = L.map('map').setView([36.8065, 10.1815], 10);\n" +
                    "            \n" +
                    "            // Add OpenStreetMap tiles\n" +
                    "            L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {\n" +
                    "                maxZoom: 19,\n" +
                    "                attribution: '&copy; <a href=\"https://openstreetmap.org/copyright\">OpenStreetMap contributors</a>'\n" +
                    "            }).addTo(map);\n" +
                    "            \n" +
                    "            // Add click event to map\n" +
                    "            map.on('click', function(e) {\n" +
                    "                onMapClick(e);\n" +
                    "            });\n" +
                    "        }\n" +
                    "        \n" +
                    "        function onMapClick(e) {\n" +
                    "            var lat = e.latlng.lat;\n" +
                    "            var lng = e.latlng.lng;\n" +
                    "\n" +
                    "            // Reverse geocode to get address\n" +
                    "            fetch(`https://nominatim.openstreetmap.org/reverse?format=json&lat=${lat}&lon=${lng}&zoom=18&addressdetails=1`)\n" +
                    "                .then(response => response.json())\n" +
                    "                .then(data => {\n" +
                    "                    var address = data.display_name || 'Adresse non disponible';\n" +
                    "                    showLocationPopup(lat, lng, address);\n" +
                    "                })\n" +
                    "                .catch(error => {\n" +
                    "                    console.error(\"Error getting address:\", error);\n" +
                    "                    showLocationPopup(lat, lng, \"Coordonnées: \" + lat.toFixed(6) + \", \" + lng.toFixed(6));\n" +
                    "                });\n" +
                    "        }\n" +
                    "        \n" +
                    "        function showLocationPopup(lat, lng, address) {\n" +
                    "            // Clear existing markers\n" +
                    "            clearMarkers();\n" +
                    "\n" +
                    "            // Create a new marker\n" +
                    "            var marker = L.marker([lat, lng]).addTo(map);\n" +
                    "            \n" +
                    "            // Create popup content\n" +
                    "            var popupContent = \n" +
                    "                '<div class=\"info-box\">' +\n" +
                    "                '<h4>Point sélectionné</h4>' +\n" +
                    "                '<p>' + address + '</p>' +\n" +
                    "                '<p>Latitude: ' + lat.toFixed(6) + '</p>' +\n" +
                    "                '<p>Longitude: ' + lng.toFixed(6) + '</p>' +\n" +
                    "                '</div>';\n" +
                    "            \n" +
                    "            // Add popup with address\n" +
                    "            marker.bindPopup(popupContent).openPopup();\n" +
                    "            \n" +
                    "            // Add to markers array\n" +
                    "            markers.push(marker);\n" +
                    "            \n" +
                    "            // Store current marker data\n" +
                    "            currentMarkerData = {\n" +
                    "                lat: lat,\n" +
                    "                lng: lng,\n" +
                    "                address: address\n" +
                    "            };\n" +
                    "            \n" +
                    "            // Center map on marker\n" +
                    "            map.setView([lat, lng], 15);\n" +
                    "        }\n" +
                    "        \n" +
                    "        function clearMarkers() {\n" +
                    "            // Remove all markers from map\n" +
                    "            for (var i = 0; i < markers.length; i++) {\n" +
                    "                map.removeLayer(markers[i]);\n" +
                    "            }\n" +
                    "            markers = [];\n" +
                    "            currentMarkerData = null;\n" +
                    "        }\n" +
                    "\n" +
                    "        function getCurrentMarkerPosition() {\n" +
                    "            if (currentMarkerData) {\n" +
                    "                return currentMarkerData.lat + ',' + currentMarkerData.lng + ',' + currentMarkerData.address;\n" +
                    "            }\n" +
                    "            return null;\n" +
                    "        }\n" +
                    "        \n" +
                    "        // Initialize the map when the page loads\n" +
                    "        window.onload = function() {\n" +
                    "            initMap();\n" +
                    "        };\n" +
                    "    </script>\n" +
                    "</body>\n" +
                    "</html>";

            webEngine.loadContent(mapHtml);

            // Configure WebEngine settings
            webEngine.setJavaScriptEnabled(true);

            // Wait for the page to load
            webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
                if (newState == Worker.State.SUCCEEDED) {
                    // Expose the controller to JavaScript
                    JSObject window = (JSObject) webEngine.executeScript("window");
                    window.setMember("javaController", new MapBridge());

                    // If we already have coordinates, show the marker
                    if (selectedLatitude != 0 || selectedLongitude != 0) {
                        webEngine.executeScript(String.format(
                                "showLocationPopup(%f, %f, '%s')",
                                selectedLatitude,
                                selectedLongitude,
                                selectedAddress.replace("'", "\\'")
                        ));
                    }
                }
            });

            // Show the dialog
            mapDialogStage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            Stage currentStage = null;
            if (chooseLocationBtn.getScene() != null && chooseLocationBtn.getScene().getWindow() instanceof Stage) {
                currentStage = (Stage) chooseLocationBtn.getScene().getWindow();
            }
            NotificationManager.showError(currentStage, "Erreur", "Erreur lors de l'ouverture de la carte: " + e.getMessage());
        }
    }

    private void loadAllData() {
        try {
            // Load candidates
            List<Candidat> candidats = candidatService.getAllCandidats();
            candidatMap.clear();
            for (Candidat candidat : candidats) {
                candidatMap.put(candidat.getId(), candidat);
            }
            candidatComboBox.setItems(FXCollections.observableArrayList(candidats));
            filterCandidatComboBox.setItems(FXCollections.observableArrayList(candidats));

            // Setup display for candidat combo boxes
            setupCandidatComboBoxDisplay(candidatComboBox);
            setupCandidatComboBoxDisplay(filterCandidatComboBox);

            // Load moniteurs
            List<Moniteur> moniteurs = moniteurService.getAllMoniteurs();
            moniteurMap.clear();
            for (Moniteur moniteur : moniteurs) {
                moniteurMap.put(moniteur.getId(), moniteur);
            }
            moniteurComboBox.setItems(FXCollections.observableArrayList(moniteurs));
            filterMoniteurComboBox.setItems(FXCollections.observableArrayList(moniteurs));

            // Setup display for moniteur combo boxes
            setupMoniteurComboBoxDisplay(moniteurComboBox);
            setupMoniteurComboBoxDisplay(filterMoniteurComboBox);

            // Load vehicules
            List<Vehicule> vehicules = vehiculeService.getAllVehicules();
            vehiculeMap.clear();
            for (Vehicule vehicule : vehicules) {
                vehiculeMap.put(vehicule.getId().intValue(), vehicule);
            }
            vehiculeComboBox.setItems(FXCollections.observableArrayList(vehicules));
            filterVehiculeComboBox.setItems(FXCollections.observableArrayList(vehicules));

            // Setup display for vehicule combo boxes
            setupVehiculeComboBoxDisplay(vehiculeComboBox);
            setupVehiculeComboBoxDisplay(filterVehiculeComboBox);

            // Load seances
            loadSeances();

        } catch (Exception e) {
            e.printStackTrace();
            Stage stage = (Stage) seanceTable.getScene().getWindow();
            NotificationManager.showError(stage, "Erreur de chargement", "Erreur lors du chargement des données: " + e.getMessage());
        }
    }

    private void setupCandidatComboBoxDisplay(ComboBox<Candidat> comboBox) {
        comboBox.setCellFactory(lv -> new ListCell<Candidat>() {
            @Override
            protected void updateItem(Candidat item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getNom() + " " + item.getPrenom() + " - " + item.getCin());
                }
            }
        });

        comboBox.setButtonCell(new ListCell<Candidat>() {
            @Override
            protected void updateItem(Candidat item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getNom() + " " + item.getPrenom());
                }
            }
        });
    }

    private void setupMoniteurComboBoxDisplay(ComboBox<Moniteur> comboBox) {
        comboBox.setCellFactory(lv -> new ListCell<Moniteur>() {
            @Override
            protected void updateItem(Moniteur item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    String disponibiliteInfo = item.isDisponible() ? "" : " (Non disponible)";
                    setText(item.getNom() + " " + item.getPrenom() + " - Permis: " +
                            String.join(", ", item.getCategoriesPermis()) + disponibiliteInfo);
                }
            }
        });

        comboBox.setButtonCell(new ListCell<Moniteur>() {
            @Override
            protected void updateItem(Moniteur item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getNom() + " " + item.getPrenom());
                }
            }
        });
    }

    private void setupVehiculeComboBoxDisplay(ComboBox<Vehicule> comboBox) {
        comboBox.setCellFactory(lv -> new ListCell<Vehicule>() {
            @Override
            protected void updateItem(Vehicule item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    String disponibiliteInfo = item.isDisponible() ? "" : " (Non disponible)";
                    setText(item.getMarque() + " " + item.getModele() + " (" + item.getMatricule() + ") - Type: " +
                            item.getType() + disponibiliteInfo);
                }
            }
        });

        comboBox.setButtonCell(new ListCell<Vehicule>() {
            @Override
            protected void updateItem(Vehicule item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getMarque() + " " + item.getModele());
                }
            }
        });
    }

    private void loadSeances() {
        try {
            seanceTable.getItems().clear();
            List<Seance> seances = seanceService.getAllSeances();
            seanceTable.getItems().addAll(seances);

            if (seanceTable.getScene() != null) {
                Stage stage = (Stage) seanceTable.getScene().getWindow();
                NotificationManager.showInfo(stage, "Séances chargées", seances.size() + " séance(s) chargée(s)");
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (seanceTable.getScene() != null) {
                Stage stage = (Stage) seanceTable.getScene().getWindow();
                NotificationManager.showError(stage, "Erreur de chargement", "Erreur lors du chargement des séances: " + e.getMessage());
            }
        }
    }

    private void setupSelectionListener() {
        seanceTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            selectedSeance = newSelection;
            modeModification = false;
            populateForm(selectedSeance);
        });
    }

    private void populateForm(Seance seance) {
        if (seance == null) return;

        typeSeanceComboBox.setValue(seance.getTypeseance());
        typePermisComboBox.setValue(seance.getTypepermis());
        datePicker.setValue(seance.getDate_debut().toLocalDate());
        heureComboBox.setValue(seance.getDate_debut().format(DateTimeFormatter.ofPattern("HH:mm")));
        adresseField.setText(seance.getAdresse() != null ? seance.getAdresse() : "");

        // Set candidat
        Candidat candidat = candidatMap.get(seance.getId_candidat());
        candidatComboBox.setValue(candidat);

        // Set moniteur
        Moniteur moniteur = moniteurMap.get(seance.getId_moniteur());
        moniteurComboBox.setValue(moniteur);

        // Set vehicule if applicable
        if (seance.getId_vehicule() != null) {
            Vehicule vehicule = vehiculeMap.get(seance.getId_vehicule());
            vehiculeComboBox.setValue(vehicule);
        } else {
            vehiculeComboBox.setValue(null);
        }

        // Set map coordinates
        selectedLatitude = seance.getLatitude();
        selectedLongitude = seance.getLongtitude();
        latitudeLabel.setText(String.format("%.6f", selectedLatitude));
        longitudeLabel.setText(String.format("%.6f", selectedLongitude));

        // Update location info
        if (seance.getTypeseance() == TypeSeance.Conduite) {
            if (selectedLatitude != 0 || selectedLongitude != 0) {
                locationInfoLabel.setText("Lieu sélectionné: " + (seance.getAdresse() != null ? seance.getAdresse() : "Coordonnées: " + selectedLatitude + ", " + selectedLongitude));
            } else {
                locationInfoLabel.setText("Aucun lieu de rendez-vous sélectionné");
            }
        } else {
            locationInfoLabel.setText("Lieu de rendez-vous non requis pour les séances de code");
        }

        // Ensure location buttons are disabled when not in edit mode
        chooseLocationBtn.setDisable(true);
        changeLocationBtn.setDisable(true);

        // Ensure vehicle and location fields are disabled when not in edit mode
        vehiculeComboBox.setDisable(true);
        chooseLocationBtn.setDisable(true);
        changeLocationBtn.setDisable(true);
        adresseField.setDisable(true);
    }

    private void clearForm() {
        typeSeanceComboBox.setValue(null);
        typePermisComboBox.setValue(null);
        datePicker.setValue(LocalDate.now());
        heureComboBox.setValue("08:00");
        candidatComboBox.setValue(null);
        moniteurComboBox.setValue(null);
        vehiculeComboBox.setValue(null);
        adresseField.clear();

        selectedLatitude = 0;
        selectedLongitude = 0;
        selectedAddress = "";
        latitudeLabel.setText("0.000000");
        longitudeLabel.setText("0.000000");
        locationInfoLabel.setText("Aucun lieu de rendez-vous sélectionné");

        selectedSeance = null;

        // Reset location buttons
        chooseLocationBtn.setDisable(true);
        changeLocationBtn.setDisable(true);
    }

    private void disableForm(boolean disable) {
        typeSeanceComboBox.setDisable(disable);
        typePermisComboBox.setDisable(disable);
        datePicker.setDisable(disable);
        heureComboBox.setDisable(disable);
        candidatComboBox.setDisable(disable);
        moniteurComboBox.setDisable(disable);

        // Always disable vehicle and location for code sessions or when form is disabled
        boolean isCodeSession = typeSeanceComboBox.getValue() == TypeSeance.Code;
        vehiculeComboBox.setDisable(disable || isCodeSession);
        chooseLocationBtn.setDisable(disable || isCodeSession);
        changeLocationBtn.setDisable(disable || isCodeSession || (selectedLatitude == 0 && selectedLongitude == 0));
        adresseField.setDisable(disable || isCodeSession);

        enregistrerBtn.setDisable(disable);
        annulerBtn.setDisable(disable);
    }

    private void applyRoleBasedAccess() {
        User currentUser = SessionManager.getCurrentUser();

        if (currentUser == null) {
            // Should not happen, but if no user is logged in, disable sensitive actions
            supprimerBtn.setDisable(true);
            exportBtn.setDisable(true);
            dashboardBtn.setDisable(true);
            return;
        }

        // Secretary cannot delete seances
        supprimerBtn.setDisable(!currentUser.canDeleteCandidats());
    }

    private void setupKeyboardShortcuts() {
        Platform.runLater(() -> {
            if (seanceTable.getScene() != null) {
                seanceTable.getScene().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                    // Ctrl+F to focus on search field
                    if (event.isControlDown() && event.getCode() == KeyCode.F) {
                        rechercheField.requestFocus();
                        event.consume();
                    }

                    // F1 for help
                    if (event.getCode() == KeyCode.F1) {
                        handleHelp(new ActionEvent(seanceTable, null));
                        event.consume();
                    }

                    // F2 for settings
                    if (event.getCode() == KeyCode.F2) {
                        handleSettings(new ActionEvent(seanceTable, null));
                        event.consume();
                    }

                    // Ctrl+N for new seance
                    if (event.isControlDown() && event.getCode() == KeyCode.N) {
                        handleAjouter();
                        event.consume();
                    }

                    // Ctrl+E to edit selected seance
                    if (event.isControlDown() && event.getCode() == KeyCode.E) {
                        if (selectedSeance != null) {
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

                    // Ctrl+R to refresh the seance list
                    if (event.isControlDown() && event.getCode() == KeyCode.R) {
                        loadSeances();
                        event.consume();
                    }

                    // Alt+B to go back to main menu
                    if (event.isAltDown() && event.getCode() == KeyCode.B) {
                        handleBack(new ActionEvent(seanceTable, null));
                        event.consume();
                    }
                });
            }
        });
    }

    // Event handlers
    @FXML
    private void handleAjouter() {
        clearForm();
        modeModification = true;
        disableForm(false);

        // Initially disable location buttons until a driving session is selected
        chooseLocationBtn.setDisable(true);
        changeLocationBtn.setDisable(true);
        vehiculeComboBox.setDisable(true);
        adresseField.setDisable(true);

        Stage stage = (Stage) ajouterBtn.getScene().getWindow();
        NotificationManager.showInfo(stage, "Nouvelle séance", "Veuillez remplir le formulaire pour ajouter une nouvelle séance");
    }

    @FXML
    private void handleModifier() {
        if (selectedSeance != null) {
            modeModification = true;
            disableForm(false);

            // Special handling for location buttons based on session type
            boolean isDrivingSession = selectedSeance.getTypeseance() == TypeSeance.Conduite;
            chooseLocationBtn.setDisable(!isDrivingSession);
            changeLocationBtn.setDisable(!isDrivingSession || (selectedLatitude == 0 && selectedLongitude == 0));
            vehiculeComboBox.setDisable(!isDrivingSession);
            adresseField.setDisable(!isDrivingSession);

            Stage stage = (Stage) modifierBtn.getScene().getWindow();
            NotificationManager.showInfo(stage, "Modification", "Modification de la séance en cours");
        } else {
            Stage stage = (Stage) modifierBtn.getScene().getWindow();
            NotificationManager.showWarning(stage, "Sélection requise", "Veuillez sélectionner une séance à modifier");
        }
    }

    @FXML
    private void handleSupprimer() {
        if (selectedSeance != null) {
            // Check if user has permission to delete
            User currentUser = SessionManager.getCurrentUser();
            if (currentUser == null || !currentUser.canDeleteCandidats()) {
                AccessControl.showAccessDeniedAlert(
                        "Vous n'avez pas la permission de supprimer des séances. Cette action est réservée aux administrateurs.");
                return;
            }

            Stage stage = (Stage) supprimerBtn.getScene().getWindow();
            boolean confirmed = ConfirmationDialog.show(
                    stage,
                    "Confirmation de suppression",
                    "Supprimer la séance",
                    "Êtes-vous sûr de vouloir supprimer cette séance ? Cette action est irréversible.",
                    ConfirmationDialog.DialogType.DELETE
            );

            if (confirmed) {
                try {
                    seanceService.deleteSeance(selectedSeance);
                    seanceTable.getItems().remove(selectedSeance);
                    clearForm();

                    NotificationManager.showSuccess(stage, "Séance supprimée", "La séance a été supprimée avec succès");
                } catch (Exception e) {
                    NotificationManager.showError(stage, "Erreur de suppression", "Erreur lors de la suppression: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } else {
            Stage stage = (Stage) supprimerBtn.getScene().getWindow();
            NotificationManager.showWarning(stage, "Sélection requise", "Veuillez sélectionner une séance à supprimer");
        }
    }

    @FXML
    private void handleAnnuler() {
        clearForm();
        modeModification = false;
        disableForm(true);

        Stage stage = (Stage) annulerBtn.getScene().getWindow();
        NotificationManager.showInfo(stage, "Opération annulée", "L'opération a été annulée");
    }

    @FXML
    private void handleEnregistrer() {
        if (validateForm()) {
            try {
                // Get form values
                TypeSeance typeSeance = typeSeanceComboBox.getValue();
                TypePermis typePermis = typePermisComboBox.getValue();
                LocalDate date = datePicker.getValue();
                LocalTime heure = LocalTime.parse(heureComboBox.getValue());
                LocalDateTime dateTime = LocalDateTime.of(date, heure);

                Candidat candidat = candidatComboBox.getValue();
                Moniteur moniteur = moniteurComboBox.getValue();
                Vehicule vehicule = vehiculeComboBox.getValue();

                // Create or update seance
                Seance seance;
                if (selectedSeance == null) {
                    seance = new Seance();
                } else {
                    seance = selectedSeance;
                }

                seance.setTypeseance(typeSeance);
                seance.setTypepermis(typePermis);
                seance.setDate_debut(dateTime);
                seance.setId_candidat(candidat.getId());
                seance.setId_moniteur(moniteur.getId());
                seance.setAdresse(adresseField.getText());

                if (typeSeance == TypeSeance.Conduite && vehicule != null) {
                    seance.setId_vehicule(vehicule.getId().intValue());
                    seance.setLatitude(selectedLatitude);
                    seance.setLongtitude(selectedLongitude);
                } else {
                    seance.setId_vehicule(null);
                    seance.setLatitude(0);
                    seance.setLongtitude(0);
                }

                // Check for conflicts one more time before saving
                List<Seance> conflicts = seanceService.findConflictingSeances(seance);
                if (!conflicts.isEmpty()) {
                    Stage stage = (Stage) enregistrerBtn.getScene().getWindow();
                    StringBuilder conflictMessage = new StringBuilder("Des conflits ont été détectés:\n");

                    for (Seance conflict : conflicts) {
                        Moniteur conflictMoniteur = moniteurMap.get(conflict.getId_moniteur());
                        String moniteurName = conflictMoniteur != null ?
                                conflictMoniteur.getNom() + " " + conflictMoniteur.getPrenom() : "Inconnu";

                        conflictMessage.append("- Séance le ")
                                .append(conflict.getFormattedDateTime())
                                .append(" avec ")
                                .append(moniteurName);

                        if (conflict.getId_vehicule() != null) {
                            Vehicule conflictVehicule = vehiculeMap.get(conflict.getId_vehicule());
                            if (conflictVehicule != null) {
                                conflictMessage.append(" (")
                                        .append(conflictVehicule.getMarque())
                                        .append(" ")
                                        .append(conflictVehicule.getModele())
                                        .append(")");
                            }
                        }

                        conflictMessage.append("\n");
                    }

                    NotificationManager.showWarning(stage, "Conflits détectés", conflictMessage.toString());
                    return;
                }

                // Confirm save
                Stage stage = (Stage) enregistrerBtn.getScene().getWindow();
                boolean confirmed = ConfirmationDialog.show(
                        stage,
                        "Confirmation d'enregistrement",
                        "Enregistrer la séance",
                        "Êtes-vous sûr de vouloir enregistrer ces informations ?",
                        ConfirmationDialog.DialogType.SAVE
                );

                if (confirmed) {
                    seance = seanceService.saveSeance(seance);
                    loadSeances();
                    disableForm(true);

                    if (selectedSeance == null) {
                        NotificationManager.showSuccess(stage, "Séance ajoutée", "La séance a été ajoutée avec succès");
                    } else {
                        NotificationManager.showSuccess(stage, "Séance modifiée", "La séance a été modifiée avec succès");
                    }
                    modeModification = false;
                }
            } catch (Exception e) {
                Stage stage = (Stage) enregistrerBtn.getScene().getWindow();
                NotificationManager.showError(stage, "Erreur d'enregistrement", "Erreur lors de l'enregistrement: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    // Add this method to handle the "Générer automatiquement" button
    @FXML
    private void handleGenererSeance() {
        try {
            // Check if required fields are filled
            if (candidatComboBox.getValue() == null ||
                    typeSeanceComboBox.getValue() == null ||
                    typePermisComboBox.getValue() == null ||
                    datePicker.getValue() == null ||
                    heureComboBox.getValue() == null) {

                Stage stage = (Stage) enregistrerBtn.getScene().getWindow();
                NotificationManager.showWarning(stage, "Champs requis",
                        "Veuillez sélectionner au minimum un candidat, un type de séance, un type de permis, une date et une heure");
                return;
            }

            // Create a LocalDateTime from the selected date and time
            LocalDate selectedDate = datePicker.getValue();
            LocalTime selectedTime = LocalTime.parse(heureComboBox.getValue());
            LocalDateTime selectedDateTime = LocalDateTime.of(selectedDate, selectedTime);

            // Get form values
            TypeSeance typeSeance = typeSeanceComboBox.getValue();
            TypePermis typePermis = typePermisComboBox.getValue();
            Candidat candidat = candidatComboBox.getValue();

            // Check if candidate is available at this time
            if (!isCandidatAvailableAt(candidat.getId(), selectedDateTime)) {
                Stage stage = (Stage) enregistrerBtn.getScene().getWindow();
                NotificationManager.showWarning(stage, "Candidat non disponible",
                        "Ce candidat est déjà inscrit à une autre séance à cette heure");
                return;
            }

            // Confirm generation
            Stage stage = (Stage) enregistrerBtn.getScene().getWindow();
            boolean confirmed = ConfirmationDialog.show(
                    stage,
                    "Confirmation de génération automatique",
                    "Générer une séance automatiquement",
                    "Voulez-vous attribuer automatiquement un moniteur et un véhicule disponibles pour cette séance ?",
                    ConfirmationDialog.DialogType.SAVE
            );

            if (confirmed) {
                // Find available moniteurs with priority for previously used ones
                List<Moniteur> availableMoniteurs = seanceService.getAvailableMoniteursAt(
                        selectedDateTime, typePermis);

                if (availableMoniteurs.isEmpty()) {
                    NotificationManager.showWarning(stage, "Aucun moniteur disponible",
                            "Aucun moniteur n'est disponible pour cette date, heure et type de permis");
                    return;
                }

                // Find previous moniteurs used by this candidate
                List<Seance> candidatSeances = seanceService.findByCandidat(candidat.getId());
                Moniteur selectedMoniteur = null;

                // Try to find a moniteur that the candidate has worked with before
                for (Seance pastSeance : candidatSeances) {
                    long pastMoniteurId = pastSeance.getId_moniteur();
                    for (Moniteur moniteur : availableMoniteurs) {
                        if (moniteur.getId() == pastMoniteurId) {
                            selectedMoniteur = moniteur;
                            break;
                        }
                    }
                    if (selectedMoniteur != null) break;
                }

                // If no previous moniteur is available, select the first available one
                if (selectedMoniteur == null && !availableMoniteurs.isEmpty()) {
                    selectedMoniteur = availableMoniteurs.get(0);
                }

                // Set the selected moniteur
                if (selectedMoniteur != null) {
                    moniteurComboBox.setValue(selectedMoniteur);
                }

                // If it's a driving lesson, find an available vehicle
                if (typeSeance == TypeSeance.Conduite) {
                    List<Vehicule> availableVehicules = seanceService.getAvailableVehiculesAt(
                            selectedDateTime, typePermis);

                    if (availableVehicules.isEmpty()) {
                        NotificationManager.showWarning(stage, "Aucun véhicule disponible",
                                "Aucun véhicule n'est disponible pour cette date, heure et type de permis");
                        return;
                    }

                    // Try to find a vehicle that the candidate has used before
                    Vehicule selectedVehicule = null;
                    for (Seance pastSeance : candidatSeances) {
                        Integer pastVehiculeId = pastSeance.getId_vehicule();
                        if (pastVehiculeId != null) {
                            for (Vehicule vehicule : availableVehicules) {
                                if (vehicule.getId().intValue() == pastVehiculeId) {
                                    selectedVehicule = vehicule;
                                    break;
                                }
                            }
                        }
                        if (selectedVehicule != null) break;
                    }

                    // If no previous vehicle is available, select the first available one
                    if (selectedVehicule == null && !availableVehicules.isEmpty()) {
                        selectedVehicule = availableVehicules.get(0);
                    }

                    // Set the selected vehicle
                    if (selectedVehicule != null) {
                        vehiculeComboBox.setValue(selectedVehicule);
                    }
                }

                NotificationManager.showSuccess(stage, "Attribution réussie",
                        "Un moniteur" + (typeSeance == TypeSeance.Conduite ? " et un véhicule" : "") +
                                " ont été attribués automatiquement");
            }
        } catch (Exception e) {
            Stage stage = (Stage) enregistrerBtn.getScene().getWindow();
            NotificationManager.showError(stage, "Erreur d'attribution",
                    "Erreur lors de l'attribution automatique: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRecherche() {
        String searchText = rechercheField.getText().toLowerCase();
        try {
            List<Seance> seances = seanceService.searchSeances(searchText);
            seanceTable.getItems().clear();
            seanceTable.getItems().addAll(seances);

            Stage stage = (Stage) rechercheBtn.getScene().getWindow();
            NotificationManager.showInfo(stage, "Recherche terminée", seances.size() + " résultat(s) trouvé(s)");
        } catch (Exception e) {
            Stage stage = (Stage) rechercheBtn.getScene().getWindow();
            NotificationManager.showError(stage, "Erreur de recherche", "Erreur lors de la recherche: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleApplyFilter() {
        try {
            TypeSeance typeSeance = filterTypeSeanceComboBox.getValue();
            TypePermis typePermis = filterTypePermisComboBox.getValue();
            LocalDateTime startDate = filterStartDatePicker.getValue() != null ?
                    filterStartDatePicker.getValue().atStartOfDay() : null;
            LocalDateTime endDate = filterEndDatePicker.getValue() != null ?
                    filterEndDatePicker.getValue().atTime(23, 59, 59) : null;

            Long moniteurId = filterMoniteurComboBox.getValue() != null ?
                    filterMoniteurComboBox.getValue().getId() : null;
            Long candidatId = filterCandidatComboBox.getValue() != null ?
                    filterCandidatComboBox.getValue().getId() : null;
            Integer vehiculeId = filterVehiculeComboBox.getValue() != null ?
                    filterVehiculeComboBox.getValue().getId().intValue() : null;

            List<Seance> filteredSeances = seanceService.filterSeances(
                    typeSeance, typePermis, startDate, endDate, moniteurId, candidatId, vehiculeId);

            seanceTable.getItems().clear();
            seanceTable.getItems().addAll(filteredSeances);

            Stage stage = (Stage) applyFilterBtn.getScene().getWindow();
            NotificationManager.showInfo(stage, "Filtres appliqués", filteredSeances.size() + " résultat(s) trouvé(s)");
        } catch (Exception e) {
            Stage stage = (Stage) applyFilterBtn.getScene().getWindow();
            NotificationManager.showError(stage, "Erreur de filtrage", "Erreur lors de l'application des filtres: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleResetFilter() {
        filterTypeSeanceComboBox.setValue(null);
        filterTypePermisComboBox.setValue(null);
        filterStartDatePicker.setValue(null);
        filterEndDatePicker.setValue(null);
        filterMoniteurComboBox.setValue(null);
        filterCandidatComboBox.setValue(null);
        filterVehiculeComboBox.setValue(null);

        loadSeances();

        Stage stage = (Stage) resetFilterBtn.getScene().getWindow();
        NotificationManager.showInfo(stage, "Filtres réinitialisés", "Tous les filtres ont été réinitialisés");
    }

    @FXML public void handleBack(ActionEvent event) {
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
            writer.write("ID,Type de séance,Type de permis,Date et heure,Candidat ID,Moniteur ID,Véhicule ID,Latitude,Longitude,Adresse\n");

            // Write data
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            for (Seance seance : seanceTable.getItems()) {
                writer.write(String.format("%d,%s,%s,%s,%d,%d,%s,%.6f,%.6f,%s\n",
                        seance.getId_seance(),
                        seance.getTypeseance(),
                        seance.getTypepermis(),
                        seance.getDate_debut().format(formatter),
                        seance.getId_candidat(),
                        seance.getId_moniteur(),
                        seance.getId_vehicule() != null ? seance.getId_vehicule() : "N/A",
                        seance.getLatitude(),
                        seance.getLongtitude(),
                        seance.getAdresse() != null ? seance.getAdresse().replace(",", ";") : ""));
            }
        }
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
            List<Seance> seances = seanceService.getAllSeances();

            // Update statistics
            int total = seances.size();
            long codeCount = seances.stream().filter(s -> s.getTypeseance() == TypeSeance.Code).count();
            long conduiteCount = seances.stream().filter(s -> s.getTypeseance() == TypeSeance.Conduite).count();

            totalSeancesDashboardLabel.setText(String.valueOf(total));
            seancesCodeDashboardLabel.setText(String.valueOf(codeCount));
            seancesConduiteDashboardLabel.setText(String.valueOf(conduiteCount));

            // Update pie chart for seance types
            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
            pieChartData.add(new PieChart.Data("Code (" + codeCount + ")", codeCount));
            pieChartData.add(new PieChart.Data("Conduite (" + conduiteCount + ")", conduiteCount));

            typeSeanceChart.setData(pieChartData);

            // Apply colors to pie chart slices
            pieChartData.get(0).getNode().setStyle("-fx-pie-color: #3498db;"); // Blue for Code
            pieChartData.get(1).getNode().setStyle("-fx-pie-color: #2ecc71;"); // Green for Conduite

            // Update bar chart for seances by month
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Nombre de séances");

            Map<Month, Long> monthCount = seances.stream()
                    .collect(Collectors.groupingBy(s -> s.getDate_debut().getMonth(), Collectors.counting()));

            // Sort by month
            Map<Month, Long> sortedMonthCount = new TreeMap<>();
            sortedMonthCount.putAll(monthCount);

            sortedMonthCount.forEach((month, count) ->
                    series.getData().add(new XYChart.Data<>(
                            month.getDisplayName(TextStyle.SHORT, Locale.FRENCH), count))
            );

            seancesParMoisChart.getData().clear();
            seancesParMoisChart.getData().add(series);

            // Apply colors to bar chart bars
            for (XYChart.Data<String, Number> data : series.getData()) {
                data.getNode().setStyle("-fx-bar-fill: #e74c3c;"); // Red for bars
            }

            Stage stage = (Stage) refreshDashboardBtn.getScene().getWindow();
            NotificationManager.showSuccess(stage, "Tableau de bord actualisé", "Les statistiques ont été mises à jour");
        } catch (Exception e) {
            Stage stage = (Stage) refreshDashboardBtn.getScene().getWindow();
            NotificationManager.showError(stage, "Erreur d'actualisation", "Erreur lors de l'actualisation du tableau de bord: " + e.getMessage());
            e.printStackTrace();
        }
    }

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

            // Show the help stage
            Scene helpScene = new Scene(helpRoot, 800, 600);
            helpStage.setScene(helpScene);
            helpStage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            NotificationManager.showError(stage, "Erreur", "Impossible d'afficher l'aide: " + e.getMessage());
        }
    }

    @FXML
    public void handleSettings(ActionEvent event) {
        try {
            // Load the settings view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/IHM/SettingsIHM.fxml"));
            Parent settingsRoot = loader.load();

            // Get the controller
            SettingsController settingsController = loader.getController();

            // Get the current stage
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Create a new stage for settings
            Stage settingsStage = new Stage();
            settingsStage.setTitle("Paramètres - Auto-École Pro");
            settingsStage.initModality(Modality.WINDOW_MODAL);
            settingsStage.initOwner(currentStage);

            // Show the settings stage
            Scene settingsScene = new Scene(settingsRoot);
            settingsStage.setScene(settingsScene);
            settingsStage.showAndWait();

            // Reload data after settings changes
            loadAllData();

        } catch (Exception e) {
            e.printStackTrace();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            NotificationManager.showError(stage, "Erreur", "Impossible d'afficher les paramètres: " + e.getMessage());
        }
    }

    private boolean validateForm() {
        boolean isValid = true;
        Label tempErrorLabel = new Label();

        // Validate type seance
        if (typeSeanceComboBox.getValue() == null) {
            typeSeanceComboBox.setStyle("-fx-border-color: #ef4444; -fx-border-width: 1px;");
            isValid = false;
        } else {
            typeSeanceComboBox.setStyle("");
        }

        // Validate type permis
        if (typePermisComboBox.getValue() == null) {
            typePermisComboBox.setStyle("-fx-border-color: #ef4444; -fx-border-width: 1px;");
            isValid = false;
        } else {
            typePermisComboBox.setStyle("");
        }

        // Validate date
        if (datePicker.getValue() == null) {
            datePicker.setStyle("-fx-border-color: #ef4444; -fx-border-width: 1px;");
            isValid = false;
        } else if (datePicker.getValue().isBefore(LocalDate.now())) {
            datePicker.setStyle("-fx-border-color: #ef4444; -fx-border-width: 1px;");
            Stage stage = (Stage) datePicker.getScene().getWindow();
            NotificationManager.showWarning(stage, "Validation", "La date ne peut pas être dans le passé");
            isValid = false;
        } else {
            datePicker.setStyle("");
        }

        // Validate heure
        if (heureComboBox.getValue() == null) {
            heureComboBox.setStyle("-fx-border-color: #ef4444; -fx-border-width: 1px;");
            isValid = false;
        } else {
            heureComboBox.setStyle("");
        }

        // Validate candidat
        if (candidatComboBox.getValue() == null) {
            candidatComboBox.setStyle("-fx-border-color: #ef4444; -fx-border-width: 1px;");
            isValid = false;
        } else {
            candidatComboBox.setStyle("");
        }

        // Validate moniteur
        if (moniteurComboBox.getValue() == null) {
            moniteurComboBox.setStyle("-fx-border-color: #ef4444; -fx-border-width: 1px;");
            isValid = false;
        } else {
            moniteurComboBox.setStyle("");
        }

        // Validate vehicule for driving lessons
        if (typeSeanceComboBox.getValue() == TypeSeance.Conduite && vehiculeComboBox.getValue() == null) {
            vehiculeComboBox.setStyle("-fx-border-color: #ef4444; -fx-border-width: 1px;");
            isValid = false;
        } else {
            vehiculeComboBox.setStyle("");
        }

        // Validate location for driving lessons
        if (typeSeanceComboBox.getValue() == TypeSeance.Conduite &&
                (selectedLatitude == 0 && selectedLongitude == 0)) {
            locationInfoLabel.setStyle("-fx-text-fill: #ef4444;");
            locationInfoLabel.setText("Veuillez sélectionner un lieu de rendez-vous");
            isValid = false;
        } else {
            locationInfoLabel.setStyle("");
        }

        // Check for conflicts with other seances
        if (isValid && candidatComboBox.getValue() != null) {
            LocalDateTime selectedDateTime = LocalDateTime.of(
                    datePicker.getValue(),
                    LocalTime.parse(heureComboBox.getValue())
            );

            // Check candidate availability
            boolean skipCandidatCheck = selectedSeance != null &&
                    selectedSeance.getId_seance() != 0 &&
                    selectedSeance.getId_candidat() == candidatComboBox.getValue().getId();

            if (!skipCandidatCheck && !isCandidatAvailableAt(candidatComboBox.getValue().getId(), selectedDateTime)) {
                candidatComboBox.setStyle("-fx-border-color: #ef4444; -fx-border-width: 1px;");
                Stage stage = (Stage) candidatComboBox.getScene().getWindow();
                NotificationManager.showWarning(stage, "Validation",
                        "Ce candidat n'est pas disponible à cette heure (déjà inscrit à une autre séance)");
                isValid = false;
            } else {
                candidatComboBox.setStyle("");
            }

            // Check moniteur availability
            boolean skipMoniteurCheck = selectedSeance != null &&
                    selectedSeance.getId_seance() != 0 &&
                    selectedSeance.getId_moniteur() == moniteurComboBox.getValue().getId();

            if (!skipMoniteurCheck && !isMoniteurAvailableAt(moniteurComboBox.getValue().getId(), selectedDateTime)) {
                moniteurComboBox.setStyle("-fx-border-color: #ef4444; -fx-border-width: 1px;");
                Stage stage = (Stage) moniteurComboBox.getScene().getWindow();
                NotificationManager.showWarning(stage, "Validation",
                        "Ce moniteur n'est pas disponible à cette heure (occupé dans une autre séance)");
                isValid = false;
            } else {
                moniteurComboBox.setStyle("");
            }

            // Check vehicle availability for driving lessons
            if (typeSeanceComboBox.getValue() == TypeSeance.Conduite && vehiculeComboBox.getValue() != null) {
                boolean skipVehiculeCheck = selectedSeance != null &&
                        selectedSeance.getId_seance() != 0 &&
                        selectedSeance.getId_vehicule() != null &&
                        selectedSeance.getId_vehicule() == vehiculeComboBox.getValue().getId().intValue();

                if (!skipVehiculeCheck && !isVehiculeAvailableAt(vehiculeComboBox.getValue().getId().intValue(), selectedDateTime)) {
                    vehiculeComboBox.setStyle("-fx-border-color: #ef4444; -fx-border-width: 1px;");
                    Stage stage = (Stage) vehiculeComboBox.getScene().getWindow();
                    NotificationManager.showWarning(stage, "Validation",
                            "Ce véhicule n'est pas disponible à cette heure (utilisé dans une autre séance)");
                    isValid = false;
                } else {
                    vehiculeComboBox.setStyle("");
                }
            }

            if (!isValid) {
                Stage stage = (Stage) typeSeanceComboBox.getScene().getWindow();
                NotificationManager.showWarning(stage, "Validation", "Veuillez remplir tous les champs obligatoires");
            }
        }

        return isValid;
    }

    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return dateTime.format(DATE_TIME_FORMATTER);
    }

    // Bridge class to communicate between Java and JavaScript
    public class MapBridge {
        public void onLocationSelected(double latitude, double longitude, String address) {
            Platform.runLater(() -> {
                selectedLatitude = (float) latitude;
                selectedLongitude = (float) longitude;
                selectedAddress = address;
                adresseField.setText(address);

                latitudeLabel.setText(String.format("%.6f", latitude));
                longitudeLabel.setText(String.format("%.6f", longitude));

                locationInfoLabel.setText("Lieu sélectionné: " + address);
                locationInfoLabel.setStyle("");

                // Close the map dialog
                if (mapDialogStage != null) {
                    mapDialogStage.close();
                }

                // Update UI
                chooseLocationBtn.setDisable(true);
                changeLocationBtn.setDisable(false);
            });
        }
    }

    // Update the isMoniteurAvailableAt method to use 1.5 hours before and after
    private boolean isMoniteurAvailableAt(long moniteurId, LocalDateTime dateTime) {
        try {
            // First check if the moniteur is generally available (not on leave, etc.)
            Moniteur moniteur = moniteurMap.get(moniteurId);
            if (moniteur != null && !moniteur.isDisponible()) {
                return false;
            }

            // Calculate the time window (1.5 hours before and after)
            LocalDateTime startWindow = dateTime.minusMinutes(120); // 1.5 hours before
            LocalDateTime endWindow = dateTime.plusMinutes(120);    // 1.5 hours after

            // Get all seances for this moniteur
            List<Seance> moniteurSeances = seanceService.findByMoniteur(moniteurId);

            // Check if any seance conflicts with the time window
            for (Seance seance : moniteurSeances) {
                // Skip the current seance if we're editing
                if (selectedSeance != null && seance.getId_seance() == selectedSeance.getId_seance()) {
                    continue;
                }

                LocalDateTime seanceStart = seance.getDate_debut();
                LocalDateTime seanceEnd = seanceStart; // 1.5 hours after start

                // Check if there's an overlap
                if ((seanceStart.isBefore(endWindow) && seanceEnd.isAfter(startWindow))) {
                    return false; // Conflict found
                }
            }

            return true; // No conflicts
        } catch (Exception e) {
            e.printStackTrace();
            return false; // In case of error, assume not available
        }
    }

    // Update the isVehiculeAvailableAt method to use 1.5 hours before and after
    private boolean isVehiculeAvailableAt(int vehiculeId, LocalDateTime dateTime) {
        try {
            // First check if the vehicle is generally available (not in repair, etc.)
            Vehicule vehicule = vehiculeMap.get(vehiculeId);
            if (vehicule != null && !vehicule.isDisponible()) {
                return false;
            }

            // Calculate the time window (1.5 hours before and after)
            LocalDateTime startWindow = dateTime.minusMinutes(120); // 1.5 hours before
            LocalDateTime endWindow = dateTime.plusMinutes(120);    // 1.5 hours after

            // Get all seances for this vehicle
            List<Seance> vehiculeSeances = seanceService.findByVehicule(vehiculeId);

            // Check if any seance conflicts with the time window
            for (Seance seance : vehiculeSeances) {
                // Skip the current seance if we're editing
                if (selectedSeance != null && seance.getId_seance() == selectedSeance.getId_seance()) {
                    continue;
                }

                LocalDateTime seanceStart = seance.getDate_debut();
                LocalDateTime seanceEnd = seanceStart; // 1.5 hours after start

                // Check if there's an overlap
                if ((seanceStart.isBefore(endWindow) && seanceEnd.isAfter(startWindow))) {
                    return false; // Conflict found
                }
            }

            return true; // No conflicts
        } catch (Exception e) {
            e.printStackTrace();
            return false; // In case of error, assume not available
        }
    }

    // Update the isCandidatAvailableAt method to use 1.5 hours before and after
    private boolean isCandidatAvailableAt(long candidatId, LocalDateTime dateTime) {
        try {
            // First check if the candidate is active
            Candidat candidat = candidatMap.get(candidatId);
            if (candidat != null && !candidat.isActif()) {
                return false;
            }

            // Calculate the time window (1.5 hours before and after)
            LocalDateTime startWindow = dateTime.minusMinutes(120);
            LocalDateTime endWindow = dateTime.plusMinutes(120);

            // Get all seances for this candidate
            List<Seance> candidatSeances = seanceService.findByCandidat(candidatId);

            // Check if any seance conflicts with the time window
            for (Seance seance : candidatSeances) {
                // Skip the current seance if we're editing
                if (selectedSeance != null && seance.getId_seance() == selectedSeance.getId_seance()) {
                    continue;
                }

                LocalDateTime seanceStart = seance.getDate_debut();
                LocalDateTime seanceEnd = seanceStart; // 1.5 hours after start

                // Check if there's an overlap
                if ((seanceStart.isBefore(endWindow) && seanceEnd.isAfter(startWindow))) {
                    return false; // Conflict found
                }
            }

            return true; // No conflicts
        } catch (Exception e) {
            e.printStackTrace();
            return false; // In case of error, assume not available
        }
    }
}
