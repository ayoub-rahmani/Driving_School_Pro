package org.example.Controllers;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.Entities.*;
import org.example.Rep.*;
import org.example.Service.*;
import org.example.Utils.ConfirmationDialog;
import org.example.Utils.NotificationManager;
import org.example.Utils.SessionManager;
import org.example.Utils.Verification;
import java.awt.Desktop;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.example.Utils.DocumentPaiement;
import org.example.Utils.DocumentDepense;
import org.example.Utils.PdfGeneratorService;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;
import javafx.util.Duration;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Rotate;

public class PaiementController implements Initializable {

    // Services
    private PaiementService paiementService;
    private DepenseService depenseService;
    private TarifService tarifService;
    private CandidatService candidatService;
    private MoniteurService moniteurService;
    private VehiculeService vehiculeService;
    private SeanceService seanceService;
    private ExamenService examenService;
    private ReparationService reparationService;
    private AuditLogService auditLogService;

    // Data
    private ObservableList<Paiement> paiements = FXCollections.observableArrayList();
    private ObservableList<Depense> depenses = FXCollections.observableArrayList();
    private ObservableList<Tarif> tarifs = FXCollections.observableArrayList();
    private ObservableList<Reparation> reparations = FXCollections.observableArrayList();
    private Map<Long, Candidat> candidatMap = new HashMap<>();
    private Map<Long, Moniteur> moniteurMap = new HashMap<>();
    private Map<Long, Vehicule> vehiculeMap = new HashMap<>();
    private Map<Long, Reparation> reparationMap = new HashMap<>();
    private Paiement selectedPaiement;
    private Depense selectedDepense;
    private Tarif selectedTarif;
    private Reparation selectedReparation;
    private boolean isDashboardVisible = false;
    private boolean isDepenseTabActive = false;
    private boolean isTarifTabActive = false;
    private double maxMontant = 0.0;

    // Paiement Tab Components
    @FXML private TabPane mainTabPane;
    @FXML private Tab paiementTab;
    @FXML private Tab depenseTab;
    @FXML private Tab tarifTab;

    // Paiement Table
    @FXML private TableView<Paiement> paiementTable;
    @FXML private TableColumn<Paiement, String> paiementIdColumn;
    @FXML private TableColumn<Paiement, String> datePaiementColumn;
    @FXML private TableColumn<Paiement, String> montantColumn;
    @FXML private TableColumn<Paiement, String> methodePaiementColumn;
    @FXML private TableColumn<Paiement, String> referenceColumn;
    @FXML private TableColumn<Paiement, String> statutColumn;
    @FXML private TableColumn<Paiement, String> candidatNameColumn;


    @FXML
    private Button generatePaiementPdfBtn;
    @FXML
    private Button generateDepensePdfBtn;

    // Paiement Form
    @FXML private ComboBox<Candidat> candidatComboBox;
    @FXML private DatePicker datePaiementPicker;
    @FXML private TextField montantField;
    @FXML private Label montantLabel;
    @FXML private Label maxMontantLabel;
    @FXML private ComboBox<String> methodePaiementComboBox;
    @FXML private TextField referenceField;
    @FXML private RadioButton paiementCompletRadio;
    @FXML private RadioButton paiementPartielRadio;
    @FXML private ToggleGroup typePaiementGroup;
    @FXML private ComboBox<String> statutComboBox;
    @FXML private TextArea notesField;
    @FXML private ProgressBar paiementProgressBar;
    @FXML private Label totalDuLabel;
    @FXML private Label totalPayeLabel;
    @FXML private Label resteAPayerLabel;
    @FXML private Label remiseLabel;
    @FXML private TextField remiseField;
    @FXML private Label paymentStatusBadge;

    // Depense Table
    @FXML private TableView<Depense> depenseTable;
    @FXML private TableColumn<Depense, String> depenseIdColumn;
    @FXML private TableColumn<Depense, String> categorieColumn;
    @FXML private TableColumn<Depense, String> montantDepenseColumn;
    @FXML private TableColumn<Depense, String> dateDepenseColumn;
    @FXML private TableColumn<Depense, String> descriptionColumn;
    @FXML private TableColumn<Depense, String> vehiculeColumn;
    @FXML private TableColumn<Depense, String> moniteurColumn;

    // Depense Form
    @FXML private ComboBox<String> categorieComboBox;
    @FXML private TextField montantDepenseField;
    @FXML private DatePicker dateDepensePicker;
    @FXML private TextArea descriptionField;
    @FXML private ComboBox<Vehicule> vehiculeComboBox;
    @FXML private ComboBox<Moniteur> moniteurComboBox;
    @FXML private ComboBox<String> typeVehiculeDepenseComboBox;
    @FXML private ComboBox<Reparation> reparationComboBox;
    @FXML private GridPane reparationContainer;
    @FXML private GridPane autresContainer;
    @FXML private TextField typeAutreDepenseField;
    @FXML private GridPane moniteurContainer;
    @FXML private VBox vehiculeContainer;

    // Tarif Table
    @FXML private TableView<Tarif> tarifTable;
    @FXML private TableColumn<Tarif, String> tarifIdColumn;
    @FXML private TableColumn<Tarif, String> typeServiceTarifColumn;
    @FXML private TableColumn<Tarif, String> montantTarifColumn;
    @FXML private TableColumn<Tarif, String> descriptionTarifColumn;
    @FXML private TableColumn<Tarif, String> remiseTarifColumn;

    // Tarif Form
    @FXML private ComboBox<String> typeServiceTarifComboBox;
    @FXML private TextField montantTarifField;
    @FXML private TextField remiseTarifField;
    @FXML private TextArea descriptionTarifField;

    // Search and Filter
    @FXML private TextField paiementRechercheField;
    @FXML private TextField depenseRechercheField;
    @FXML private TextField tarifRechercheField;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private ComboBox<String> filterMethodePaiementComboBox;
    @FXML private ComboBox<String> filterStatutComboBox;
    @FXML private ComboBox<Candidat> filterCandidatComboBox;

    // Action Buttons
    @FXML private Button paiementAjouterBtn, paiementModifierBtn, paiementSupprimerBtn, paiementAnnulerBtn, paiementEnregistrerBtn, backBtn;
    @FXML private Button depenseAjouterBtn, depenseModifierBtn, depenseSupprimerBtn, depenseAnnulerBtn, depenseEnregistrerBtn;
    @FXML private Button tarifModifierBtn, tarifAnnulerBtn, tarifEnregistrerBtn;
    @FXML private Button resetFilterBtn, applyFilterBtn, paiementExportBtn, dashboardBtn, refreshDashboardBtn;
    @FXML private Button paiementRechercheBtn, depenseRechercheBtn, tarifRechercheBtn;
    @FXML private Button genererReferenceBtn, payerTousBtn, payerToutesReparationsBtn;

    // Dashboard Components
    @FXML private VBox dashboardPane;
    @FXML private ScrollPane formScrollPane;
    @FXML private Label totalRevenusLabel;
    @FXML private Label totalDepensesLabel;
    @FXML private Label beneficeNetLabel;
    @FXML private Label tauxRentabiliteLabel;
    @FXML private PieChart revenusByTypeChart;
    @FXML private PieChart depensesByCategorieChart;
    @FXML private BarChart<String, Number> revenusDepensesMensuelChart;
    @FXML private PieChart methodePaiementChart;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            // Initialize services
            paiementService = new PaiementService(new PaiementRep());
            depenseService = new DepenseService(new DepenseRep());
            tarifService = new TarifService(new TarifRep());
            candidatService = new CandidatService(new CandidatRep());
            moniteurService = new MoniteurService(new MoniteurRep());
            vehiculeService = new VehiculeService(new VehiculeRep());
            seanceService = new SeanceService(new SeanceRep(), new VehiculeRep(), new MoniteurRep());
            examenService = new ExamenService(new ExamenRep());
            reparationService = new ReparationService(new ReparationRep(), new VehiculeRep());
            auditLogService = new AuditLogService();

            // Apply role-based access control
            applyRoleBasedAccess();

            // Setup UI components
            setupTableColumns();
            setupComboBoxes();
            setupTabListeners();
            setupRadioButtons();


            // Load data
            loadInitialData();
            setupSelectionListeners();
            clearForm();

            // Explicitly disable all forms at startup
            if (isDepenseTabActive) {
                disableDepenseForm(true);
            } else if (isTarifTabActive) {
                disableTarifForm(true);
            } else {
                disablePaiementForm(true);
            }

            // Disable modification buttons until selection
            if (paiementModifierBtn != null) paiementModifierBtn.setDisable(true);
            if (paiementSupprimerBtn != null) paiementSupprimerBtn.setDisable(true);
            if (depenseModifierBtn != null) depenseModifierBtn.setDisable(true);
            if (depenseSupprimerBtn != null) depenseSupprimerBtn.setDisable(true);
            if (tarifModifierBtn != null) tarifModifierBtn.setDisable(true);

            generatePaiementPdfBtn.disableProperty().bind(
                    paiementTable.getSelectionModel().selectedItemProperty().isNull());

            generateDepensePdfBtn.disableProperty().bind(
                    depenseTable.getSelectionModel().selectedItemProperty().isNull());

            // Setup keyboard shortcuts
            setupKeyboardShortcuts();

            // Fix layout issues
            fixLayoutIssues();

            // Always disable reference field - it should only be generated
            if (referenceField != null) {
                referenceField.setDisable(true);
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error initializing PaiementController: " + e.getMessage());
        }
    }

    // Add a refresh button handler method
    @FXML
    private void handleRefresh() {
        try {
            // Show loading indicator or message
            Stage stage = (Stage) mainTabPane.getScene().getWindow();
            NotificationManager.showInfo(stage, "Rafraîchissement", "Chargement des données en cours...");

            // Use a background thread to load data to avoid UI freezing
            Thread loadDataThread = new Thread(() -> {
                Platform.runLater(() -> {
                    try {
                        // Reload all necessary data
                        loadCandidats();
                        loadPaiements();

                        // Add these lines to refresh moniteurs and vehicules
                        loadMoniteurs();
                        loadVehicules();

                        // Show success message
                        NotificationManager.showSuccess(stage, "Rafraîchissement terminé", "Les données ont été rafraîchies avec succès");
                    } catch (Exception e) {
                        e.printStackTrace();
                        NotificationManager.showError(stage, "Erreur", "Erreur lors du rafraîchissement des données: " + e.getMessage());
                    }
                });
            });

            loadDataThread.setDaemon(true);
            loadDataThread.start();
        } catch (Exception e) {
            e.printStackTrace();
            Stage stage = (Stage) mainTabPane.getScene().getWindow();
            NotificationManager.showError(stage, "Erreur", "Erreur lors du rafraîchissement: " + e.getMessage());
        }
    }

    // Add helper methods to disable specific forms
    private void disablePaiementForm(boolean disable) {
        if (candidatComboBox != null) candidatComboBox.setDisable(disable);
        if (datePaiementPicker != null) datePaiementPicker.setDisable(disable);
        if (montantField != null) {
            boolean isComplet = paiementCompletRadio != null && paiementCompletRadio.isSelected();
            montantField.setDisable(disable || isComplet);
        }
        if (methodePaiementComboBox != null) methodePaiementComboBox.setDisable(disable);
        if (referenceField != null) referenceField.setDisable(true); // Always disabled
        if (statutComboBox != null) statutComboBox.setDisable(disable);
        if (notesField != null) notesField.setDisable(disable);
        if (paiementCompletRadio != null) paiementCompletRadio.setDisable(disable);
        if (paiementPartielRadio != null) paiementPartielRadio.setDisable(disable);
        if (remiseField != null) remiseField.setDisable(disable);
        if (paiementAnnulerBtn != null) paiementAnnulerBtn.setDisable(disable);
        if (paiementEnregistrerBtn != null) paiementEnregistrerBtn.setDisable(disable);
        if (genererReferenceBtn != null) genererReferenceBtn.setDisable(disable);
    }

    private void disableDepenseForm(boolean disable) {
        if (categorieComboBox != null) categorieComboBox.setDisable(disable);
        if (dateDepensePicker != null) dateDepensePicker.setDisable(disable);
        if (montantDepenseField != null) {
            boolean isReparation = typeVehiculeDepenseComboBox != null &&
                    "REPARATION".equals(typeVehiculeDepenseComboBox.getValue()) &&
                    reparationComboBox != null &&
                    reparationComboBox.getValue() != null;
            montantDepenseField.setDisable(disable || isReparation);
        }
        if (descriptionField != null) descriptionField.setDisable(disable);
        if (moniteurComboBox != null) moniteurComboBox.setDisable(disable);
        if (vehiculeComboBox != null) vehiculeComboBox.setDisable(disable);
        if (typeVehiculeDepenseComboBox != null) typeVehiculeDepenseComboBox.setDisable(disable);
        if (reparationComboBox != null) reparationComboBox.setDisable(disable);
        if (typeAutreDepenseField != null) typeAutreDepenseField.setDisable(disable);
        if (depenseAnnulerBtn != null) depenseAnnulerBtn.setDisable(disable);
        if (depenseEnregistrerBtn != null) depenseEnregistrerBtn.setDisable(disable);
    }

    private void disableTarifForm(boolean disable) {
        if (typeServiceTarifComboBox != null) typeServiceTarifComboBox.setDisable(disable);
        if (montantTarifField != null) montantTarifField.setDisable(disable);
        if (remiseTarifField != null) remiseTarifField.setDisable(disable);
        if (descriptionTarifField != null) descriptionTarifField.setDisable(disable);
        if (tarifAnnulerBtn != null) tarifAnnulerBtn.setDisable(disable);
        if (tarifEnregistrerBtn != null) tarifEnregistrerBtn.setDisable(disable);
    }

    // Update the disableForm method to use the specific form disabling methods
    private void disableForm(boolean disable) {
        if (isDepenseTabActive) {
            disableDepenseForm(disable);
        } else if (isTarifTabActive) {
            disableTarifForm(disable);
        } else {
            disablePaiementForm(disable);
        }
    }

    private void fixLayoutIssues() {
        Platform.runLater(() -> {
            // Fix ScrollPane configuration
            if (formScrollPane != null) {
                formScrollPane.setFitToWidth(true);
                formScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
                formScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
                formScrollPane.setPannable(true);

                // Add padding to the content to push it down
                if (formScrollPane.getContent() != null) {
                    Node content = formScrollPane.getContent();
                    if (content instanceof Region) {
                        ((Region) content).setPadding(new Insets(20, 10, 10, 10));
                    }
                }
            }

        });
    }

    private void updateDepenseFormByCategory(String category) {
        // Hide all containers first
        if (moniteurContainer != null) moniteurContainer.setVisible(false);
        if (vehiculeContainer != null) vehiculeContainer.setVisible(false);
        if (reparationContainer != null) reparationContainer.setVisible(false);
        if (autresContainer != null) autresContainer.setVisible(false);

        // Show appropriate containers based on category
        if (category.equals("MONITEUR")) {
            if (moniteurContainer != null) {
                moniteurContainer.setVisible(true);
                // Ensure proper positioning
                GridPane.setColumnIndex(moniteurContainer, 2);
                GridPane.setRowIndex(moniteurContainer, 1);
                GridPane.setColumnSpan(moniteurContainer, 2);

                // Add this line to ensure moniteurs are loaded
                loadMoniteurs();
            }
        } else if (category.equals("VEHICULE")) {
            if (vehiculeContainer != null) {
                // Ensure proper positioning
                GridPane.setColumnIndex(vehiculeContainer, 2);
                GridPane.setRowIndex(vehiculeContainer, 1);
                GridPane.setColumnSpan(vehiculeContainer, 2);

                // Make sure ComboBoxes have proper width
                if (vehiculeComboBox != null) {
                    vehiculeComboBox.setMaxWidth(250);
                    vehiculeComboBox.setPrefWidth(250);
                }

                if (typeVehiculeDepenseComboBox != null) {
                    typeVehiculeDepenseComboBox.setMaxWidth(250);
                    typeVehiculeDepenseComboBox.setPrefWidth(250);
                }

                vehiculeContainer.setVisible(true);

                // Add this line to ensure vehicules are loaded
                loadVehicules();

                // Reset type selection
                if (typeVehiculeDepenseComboBox != null) {
                    typeVehiculeDepenseComboBox.setValue(null);
                    // Ensure reparation container is hidden when resetting type
                    if (reparationContainer != null) reparationContainer.setVisible(false);
                }
            }
        } else if (category.equals("AUTRES")) {
            if (autresContainer != null) {
                autresContainer.setVisible(true);
                // Ensure proper positioning
                GridPane.setColumnIndex(autresContainer, 2);
                GridPane.setRowIndex(autresContainer, 1);
                GridPane.setColumnSpan(autresContainer, 2);
            }
        }
    }
    @FXML
    private void handleGeneratePaiementPdf(ActionEvent event) {
        if (selectedPaiement == null) {
            Stage stage = (Stage) generatePaiementPdfBtn.getScene().getWindow();
            NotificationManager.showWarning(stage, "Sélection requise", "Veuillez sélectionner un paiement.");
            return;
        }

        try {
            Candidat candidat = candidatMap.get(selectedPaiement.getCandidatId());
            if (candidat == null) {
                Stage stage = (Stage) generatePaiementPdfBtn.getScene().getWindow();
                NotificationManager.showError(stage, "Erreur", "Candidat associé introuvable.");
                return;
            }

            String filePath = PdfGeneratorService.generatePaiementPdf(selectedPaiement, candidat);
            PdfGeneratorService.openPdfFile(filePath);

            Stage stage = (Stage) generatePaiementPdfBtn.getScene().getWindow();
            NotificationManager.showSuccess(stage, "Document généré", "Document généré avec succès: " + filePath);
        } catch (IOException e) {
            Stage stage = (Stage) generatePaiementPdfBtn.getScene().getWindow();
            NotificationManager.showError(stage, "Erreur de génération", "Erreur lors de la génération du document: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleGenerateDepensePdf(ActionEvent event) {
        if (selectedDepense == null) {
            Stage stage = (Stage) generateDepensePdfBtn.getScene().getWindow();
            NotificationManager.showWarning(stage, "Sélection requise", "Veuillez sélectionner une dépense.");
            return;
        }

        try {
            // Préparer les entités associées
            Optional<Moniteur> moniteur = Optional.empty();
            if (selectedDepense.getMoniteurId() != null) {
                moniteur = Optional.ofNullable(moniteurMap.get(selectedDepense.getMoniteurId()));
            }

            Optional<Vehicule> vehicule = Optional.empty();
            if (selectedDepense.getVehiculeId() != null) {
                vehicule = Optional.ofNullable(vehiculeMap.get(selectedDepense.getVehiculeId()));
            }

            Optional<Reparation> reparation = Optional.empty();
            if (selectedDepense.getReparationId() != null) {
                // Assuming getReparationById returns Optional<Reparation>
                reparation = reparationService.getReparationById(selectedDepense.getReparationId());
            }

            String filePath = PdfGeneratorService.generateDepensePdf(selectedDepense, moniteur, vehicule, reparation);
            PdfGeneratorService.openPdfFile(filePath);

            Stage stage = (Stage) generateDepensePdfBtn.getScene().getWindow();
            NotificationManager.showSuccess(stage, "Document généré", "Document généré avec succès: " + filePath);
        } catch (IOException e) {
            Stage stage = (Stage) generateDepensePdfBtn.getScene().getWindow();
            NotificationManager.showError(stage, "Erreur de génération", "Erreur lors de la génération du document: " + e.getMessage());
            e.printStackTrace();
        }
    }





    private void setupTableColumns() {
        try {
            // Paiement Table
            if (paiementIdColumn != null) {
                paiementIdColumn.setCellValueFactory(cellData ->
                        new SimpleStringProperty(cellData.getValue().getId().toString()));
            }
            // Configure scroll pane to properly show all content
            if (formScrollPane != null) {
                formScrollPane.setFitToWidth(true);
                formScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
                formScrollPane.setPannable(true);
            }
            if (datePaiementColumn != null) {
                datePaiementColumn.setCellValueFactory(cellData ->
                        new SimpleStringProperty(cellData.getValue().getDatePaiement().toString()));
            }
            if (montantColumn != null) {
                montantColumn.setCellValueFactory(cellData ->
                        new SimpleStringProperty(String.format("%.2f DT", cellData.getValue().getMontant())));
            }
            if (methodePaiementColumn != null) {
                methodePaiementColumn.setCellValueFactory(cellData ->
                        new SimpleStringProperty(cellData.getValue().getMethodePaiement()));
            }
            if (referenceColumn != null) {
                referenceColumn.setCellValueFactory(cellData ->
                        new SimpleStringProperty(cellData.getValue().getReference()));
            }
            if (statutColumn != null) {
                statutColumn.setCellValueFactory(cellData ->
                        new SimpleStringProperty(cellData.getValue().getStatut()));
            }

            if (candidatNameColumn != null) {
                candidatNameColumn.setCellValueFactory(cellData -> {
                    Paiement paiement = cellData.getValue();
                    Candidat candidat = candidatMap.get(paiement.getCandidatId());
                    String candidatName = candidat != null ? candidat.getNom() + " " + candidat.getPrenom() : "Inconnu";
                    return new SimpleStringProperty(candidatName);
                });
            }

            // Style the statut column with badges
            if (statutColumn != null) {
                statutColumn.setCellFactory(column -> new TableCell<Paiement, String>() {
                    @Override
                    protected void updateItem(String statut, boolean empty) {
                        super.updateItem(statut, empty);
                        if (empty || statut == null) {
                            setText(null);
                            setGraphic(null);
                            setStyle("");
                        } else {
                            Label badge = new Label(statut);
                            badge.getStyleClass().add("statut-badge");

                            if (statut.equals("COMPLET")) {
                                badge.getStyleClass().add("statut-complet");
                            } else if (statut.equals("PARTIEL")) {
                                badge.getStyleClass().add("statut-partiel");
                            } else if (statut.equals("REMBOURSEMENT")) {
                                badge.getStyleClass().add("statut-remboursement");
                            }

                            setGraphic(badge);
                            setText(null);
                        }
                    }
                });
            }

            // Depense Table
            if (depenseIdColumn != null) {
                depenseIdColumn.setCellValueFactory(cellData ->
                        new SimpleStringProperty(cellData.getValue().getId().toString()));
            }
            if (categorieColumn != null) {
                categorieColumn.setCellValueFactory(cellData ->
                        new SimpleStringProperty(cellData.getValue().getCategorie()));
            }
            if (montantDepenseColumn != null) {
                montantDepenseColumn.setCellValueFactory(cellData ->
                        new SimpleStringProperty(String.format("%.2f DT", cellData.getValue().getMontant())));
            }
            if (dateDepenseColumn != null) {
                dateDepenseColumn.setCellValueFactory(cellData ->
                        new SimpleStringProperty(cellData.getValue().getDateDepense().toString()));
            }
            if (descriptionColumn != null) {
                descriptionColumn.setCellValueFactory(cellData ->
                        new SimpleStringProperty(cellData.getValue().getDescription()));
            }

            if (vehiculeColumn != null) {
                vehiculeColumn.setCellValueFactory(cellData -> {
                    Depense depense = cellData.getValue();
                    if (depense.getVehiculeId() != null) {
                        Vehicule vehicule = vehiculeMap.get(depense.getVehiculeId());
                        String vehiculeInfo = vehicule != null ?
                                vehicule.getMarque() + " " + vehicule.getModele() + " (" + vehicule.getMatricule() + ")" : "Inconnu";
                        return new SimpleStringProperty(vehiculeInfo);
                    }
                    return new SimpleStringProperty("N/A");
                });
            }

            if (moniteurColumn != null) {
                moniteurColumn.setCellValueFactory(cellData -> {
                    Depense depense = cellData.getValue();
                    if (depense.getMoniteurId() != null) {
                        Moniteur moniteur = moniteurMap.get(depense.getMoniteurId());
                        String moniteurName = moniteur != null ? moniteur.getNom() + " " + moniteur.getPrenom() : "Inconnu";
                        return new SimpleStringProperty(moniteurName);
                    }
                    return new SimpleStringProperty("N/A");
                });
            }

            // Style the categorie column with badges
            if (categorieColumn != null) {
                categorieColumn.setCellFactory(column -> new TableCell<Depense, String>() {
                    @Override
                    protected void updateItem(String categorie, boolean empty) {
                        super.updateItem(categorie, empty);
                        if (empty || categorie == null) {
                            setText(null);
                            setGraphic(null);
                            setStyle("");
                        } else {
                            Label badge = new Label(categorie);
                            badge.getStyleClass().add("categorie-badge");

                            if (categorie.equals("MONITEUR")) {
                                badge.getStyleClass().add("categorie-moniteur");
                            } else if (categorie.equals("VEHICULE")) {
                                badge.getStyleClass().add("categorie-vehicule");
                            } else if (categorie.equals("AUTRES")) {
                                badge.getStyleClass().add("categorie-autre");
                            }

                            setGraphic(badge);
                            setText(null);
                        }
                    }
                });
            }

            // Tarif Table
            if (tarifIdColumn != null) {
                tarifIdColumn.setCellValueFactory(cellData ->
                        new SimpleStringProperty(cellData.getValue().getId().toString()));
            }
            if (typeServiceTarifColumn != null) {
                typeServiceTarifColumn.setCellValueFactory(cellData ->
                        new SimpleStringProperty(cellData.getValue().getTypeService()));
            }
            if (montantTarifColumn != null) {
                montantTarifColumn.setCellValueFactory(cellData ->
                        new SimpleStringProperty(String.format("%.2f DT", cellData.getValue().getMontant())));
            }
            if (descriptionTarifColumn != null) {
                descriptionTarifColumn.setCellValueFactory(cellData ->
                        new SimpleStringProperty(cellData.getValue().getDescription()));
            }
            if (remiseTarifColumn != null) {
                remiseTarifColumn.setCellValueFactory(cellData ->
                        new SimpleStringProperty(String.format("%.2f%%", cellData.getValue().getRemise())));
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error setting up table columns: " + e.getMessage());
        }
    }

    private void setupComboBoxes() {
        try {
            // Paiement Form ComboBoxes
            if (methodePaiementComboBox != null) {
                methodePaiementComboBox.setItems(FXCollections.observableArrayList(
                        "ESPECES", "CARTE", "CHEQUE", "VIREMENT"));
            }

            // Skip setting items for statutComboBox if it's null
            if (statutComboBox != null) {
                statutComboBox.setItems(FXCollections.observableArrayList(
                        "COMPLET", "PARTIEL", "REMBOURSEMENT"));
            }

            // Depense Form ComboBoxes
            if (categorieComboBox != null) {
                categorieComboBox.setItems(FXCollections.observableArrayList(
                        "MONITEUR", "VEHICULE", "AUTRES"));
            }

            // Type de dépense véhicule
            if (typeVehiculeDepenseComboBox != null) {
                typeVehiculeDepenseComboBox.setItems(FXCollections.observableArrayList(
                        "DOCUMENTS", "REPARATION"));
            }

            // Tarif Form ComboBoxes
            if (typeServiceTarifComboBox != null) {
                typeServiceTarifComboBox.setItems(FXCollections.observableArrayList(
                        "SEANCE_CODE", "SEANCE_CONDUITE", "EXAMEN_CODE", "EXAMEN_CONDUITE"));
            }

            // Filter ComboBoxes
            if (filterMethodePaiementComboBox != null) {
                filterMethodePaiementComboBox.setItems(FXCollections.observableArrayList(
                        "Toutes les méthodes", "ESPECES", "CARTE", "CHEQUE", "VIREMENT"));
                filterMethodePaiementComboBox.setValue("Toutes les méthodes");
            }

            if (filterStatutComboBox != null) {
                filterStatutComboBox.setItems(FXCollections.observableArrayList(
                        "Tous les statuts", "COMPLET", "PARTIEL", "REMBOURSEMENT"));
                filterStatutComboBox.setValue("Tous les statuts");
            }

            // Add listeners for candidat changes
            if (candidatComboBox != null) {
                candidatComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
                    if (newVal != null) {
                        updatePaiementProgress(newVal.getId());

                        // Show montant field only when candidat is selected and payment type is selected
                        boolean showMontant = paiementCompletRadio.isSelected() || paiementPartielRadio.isSelected();
                        if (montantLabel != null) montantLabel.setVisible(showMontant);
                        if (montantField != null) montantField.setVisible(showMontant);

                        // Update max amount label
                        if (showMontant && maxMontantLabel != null) {
                            double resteAPayer = calculateResteAPayer(newVal.getId());
                            maxMontantLabel.setText("(Max: " + String.format("%.2f", resteAPayer) + " DT)");
                            maxMontantLabel.setVisible(true);
                        }
                    }
                });
            }

            // Add listeners for montant changes
            if (montantField != null) {
                montantField.textProperty().addListener((obs, oldVal, newVal) -> {
                    if (newVal != null && !newVal.isEmpty() && candidatComboBox != null && candidatComboBox.getValue() != null) {
                        try {
                            double montant = Double.parseDouble(newVal);
                            double resteAPayer = calculateResteAPayer(candidatComboBox.getValue().getId());

                            // Validate montant against reste à payer
                            if (montant > resteAPayer) {
                                montantField.setStyle("-fx-border-color: #ef4444; -fx-border-width: 1px;");
                                Stage stage = (Stage) montantField.getScene().getWindow();

                                // Use NotificationManager for validation errors
                                NotificationManager.showError(
                                        stage,
                                        "Montant invalide",
                                        "Le montant ne peut pas dépasser le reste à payer (" + String.format("%.2f", resteAPayer) + " DT)"
                                );

                                // Reset to max value
                                montantField.setText(String.format("%.2f", resteAPayer));
                            } else {
                                montantField.setStyle("");
                                updatePaiementProgressBar(montant);
                            }
                        } catch (NumberFormatException e) {
                            // Ignore invalid input
                        }
                    }
                });
            }

            // Add listener for remise changes
            if (remiseField != null) {
                remiseField.textProperty().addListener((obs, oldVal, newVal) -> {
                    if (newVal != null && !newVal.isEmpty() && candidatComboBox != null && candidatComboBox.getValue() != null) {
                        try {
                            double remise = Double.parseDouble(newVal);

                            // Validate remise percentage
                            if (remise < 0 || remise > 100) {
                                remiseField.setStyle("-fx-border-color: #ef4444; -fx-border-width: 1px;");
                                Stage stage = (Stage) remiseField.getScene().getWindow();

                                // Use NotificationManager for validation errors
                                NotificationManager.showError(
                                        stage,
                                        "Remise invalide",
                                        "La remise doit être comprise entre 0 et 100%"
                                );

                                // Reset to valid value
                                remiseField.setText(remise < 0 ? "0" : "100");
                            } else {
                                remiseField.setStyle("");
                                updateMontantWithRemise();
                            }
                        } catch (NumberFormatException e) {
                            // Ignore invalid input
                        }
                    }
                });
            }

            // Add listener for categorie changes in depense
            if (categorieComboBox != null) {
                categorieComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
                    if (newVal != null) {
                        updateDepenseFormByCategory(newVal);
                    }
                });
            }


            if (typeVehiculeDepenseComboBox != null && vehiculeComboBox != null) {
                typeVehiculeDepenseComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
                    if (reparationContainer != null) {
                        // Always hide first to avoid UI glitches
                        reparationContainer.setVisible(false);

                        // Only show if both conditions are met
                        if (newVal != null && vehiculeComboBox.getValue() != null && newVal.equals("REPARATION")) {
                            // Use Platform.runLater to ensure UI updates properly
                            Platform.runLater(() -> {
                                reparationContainer.setVisible(true);
                                loadReparationsForVehicule(vehiculeComboBox.getValue().getId());
                            });
                        } else {
                            // Enable montant field if not reparation
                            if (montantDepenseField != null) montantDepenseField.setDisable(false);
                        }
                    }
                });
            }

            // Add listener for véhicule changes
            if (vehiculeComboBox != null && typeVehiculeDepenseComboBox != null) {
                vehiculeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
                    if (newVal != null && typeVehiculeDepenseComboBox.getValue() != null) {
                        if (typeVehiculeDepenseComboBox.getValue().equals("REPARATION") && reparationContainer != null) {
                            reparationContainer.setVisible(true);
                            loadReparationsForVehicule(newVal.getId());
                        }
                    }
                });
            }
            // Add listener for categorie changes in depense
            if (categorieComboBox != null) {
                categorieComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
                    if (newVal != null) {
                        updateDepenseFormByCategory(newVal);
                    }
                });
            }

            // Add listener for type véhicule dépense changes
            if (typeVehiculeDepenseComboBox != null && vehiculeComboBox != null) {
                typeVehiculeDepenseComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
                    if (newVal != null && vehiculeComboBox.getValue() != null) {
                        if (newVal.equals("REPARATION") && reparationContainer != null) {
                            reparationContainer.setVisible(true);
                            loadReparationsForVehicule(vehiculeComboBox.getValue().getId());
                        } else {
                            if (reparationContainer != null) reparationContainer.setVisible(false);
                            // Enable montant field if not reparation
                            if (montantDepenseField != null) montantDepenseField.setDisable(false);
                        }
                    }
                });
            }

            // Add listener for véhicule changes
            if (vehiculeComboBox != null && typeVehiculeDepenseComboBox != null) {
                vehiculeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
                    if (newVal != null && typeVehiculeDepenseComboBox.getValue() != null) {
                        if (typeVehiculeDepenseComboBox.getValue().equals("REPARATION") && reparationContainer != null) {
                            reparationContainer.setVisible(true);
                            loadReparationsForVehicule(newVal.getId());
                        }
                    }
                });
            }

            // Add listener for reparation changes to update montant
            if (reparationComboBox != null) {
                reparationComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
                    if (newVal != null && montantDepenseField != null) {
                        montantDepenseField.setText(String.format("%.2f", newVal.getCout()));
                        montantDepenseField.setDisable(true);
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error setting up combo boxes: " + e.getMessage());
        }
    }

    private void setupRadioButtons() {
        try {
            // Create toggle group if not already set in FXML
            if (typePaiementGroup == null && paiementCompletRadio != null && paiementPartielRadio != null) {
                typePaiementGroup = new ToggleGroup();
                paiementCompletRadio.setToggleGroup(typePaiementGroup);
                paiementPartielRadio.setToggleGroup(typePaiementGroup);

                // Add styling classes to match Vehicule interface
                paiementCompletRadio.getStyleClass().add("status-active");
                paiementPartielRadio.getStyleClass().add("status-inactive");
            }

            // Add listeners for payment type changes
            if (paiementCompletRadio != null) {
                paiementCompletRadio.selectedProperty().addListener((obs, oldVal, newVal) -> {
                    if (newVal) {
                        if (remiseLabel != null) remiseLabel.setVisible(true);
                        if (remiseField != null) remiseField.setVisible(true);

                        if (candidatComboBox != null && candidatComboBox.getValue() != null) {
                            if (montantLabel != null) montantLabel.setVisible(true);
                            if (montantField != null) {
                                montantField.setVisible(true);
                                montantField.setDisable(true); // Disable the field for "complet" payment
                            }
                            if (maxMontantLabel != null) maxMontantLabel.setVisible(true);
                            updateMontantWithRemise();
                        }
                    }
                });
            }

            if (paiementPartielRadio != null) {
                paiementPartielRadio.selectedProperty().addListener((obs, oldVal, newVal) -> {
                    if (newVal) {
                        if (remiseLabel != null) remiseLabel.setVisible(false);
                        if (remiseField != null) remiseField.setVisible(false);

                        if (candidatComboBox != null && candidatComboBox.getValue() != null) {
                            if (montantLabel != null) montantLabel.setVisible(true);
                            if (montantField != null) {
                                montantField.setVisible(true);
                                // Clear montant field for partial payment
                                montantField.clear();
                            }
                            if (maxMontantLabel != null) {
                                maxMontantLabel.setVisible(true);
                                double resteAPayer = calculateResteAPayer(candidatComboBox.getValue().getId());
                                maxMontantLabel.setText("(Max: " + String.format("%.2f", resteAPayer) + " DT)");
                            }
                        }
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error setting up radio buttons: " + e.getMessage());
        }
    }

    private void updateMontantWithRemise() {
        if (candidatComboBox == null || candidatComboBox.getValue() == null || montantField == null) return;

        Long candidatId = candidatComboBox.getValue().getId();
        double totalDu = calculateTotalDuForCandidat(candidatId);

        // Get remise percentage from tarif
        double remisePercentage = 0;
        try {
            if (remiseField != null && !remiseField.getText().isEmpty()) {
                remisePercentage = Double.parseDouble(remiseField.getText());
            }
        } catch (NumberFormatException e) {
            remisePercentage = 0;
        }

        // Calculate montant with remise
        double montantWithRemise = totalDu * (1 - (remisePercentage / 100));
        montantField.setText(String.format("%.2f", montantWithRemise));

        // Update the labels to show the discount is applied to the required amount
        if (totalDuLabel != null) {
            totalDuLabel.setText("Total dû: " + String.format("%.2f", montantWithRemise) + " DT");
        }

        // Update total paid and reste à payer
        double totalPaye = calculateTotalPayeForCandidat(candidatId);
        if (totalPayeLabel != null) {
            totalPayeLabel.setText("Total payé: " + String.format("%.2f", totalPaye) + " DT");
        }

        if (resteAPayerLabel != null) {
            double resteAPayer = montantWithRemise - totalPaye;
            resteAPayerLabel.setText("Reste à payer: " + String.format("%.2f", resteAPayer) + " DT");
        }

        // Update progress bar - with complete payment, it should show 100%
        if (paiementProgressBar != null) {
            paiementProgressBar.setProgress(1.0);
            paiementProgressBar.setStyle("-fx-accent: #10b981;"); // Green for complete
        }

        // Update payment status badge
        if (paymentStatusBadge != null) {
            paymentStatusBadge.setText("COMPLET");
            paymentStatusBadge.getStyleClass().removeAll("payment-status-partial", "payment-status-pending");
            paymentStatusBadge.getStyleClass().add("payment-status-complete");
        }
    }

    private void loadReparationsForVehicule(Long vehiculeId) {
        if (vehiculeId == null || reparationComboBox == null) return;

        try {
            // Use the existing method from ReparationService
            List<Reparation> vehiculeReparations = reparationService.getReparationsByVehiculeId(vehiculeId);

            // Filter to show only unpaid repairs
            List<Reparation> unpaidReparations = vehiculeReparations.stream()
                    .filter(r -> !r.isPaye())
                    .collect(Collectors.toList());

            reparationComboBox.setItems(FXCollections.observableArrayList(unpaidReparations));

            if (!unpaidReparations.isEmpty()) {
                reparationComboBox.setValue(unpaidReparations.get(0));

                // Set the montant field to the cost of the selected reparation and disable it
                if (montantDepenseField != null) {
                    montantDepenseField.setText(String.format("%.2f", unpaidReparations.get(0).getCout()));
                    montantDepenseField.setDisable(true);
                }
            } else {
                // No unpaid repairs
                if (reparationComboBox.getScene() != null) {
                    Stage stage = (Stage) reparationComboBox.getScene().getWindow();
                    NotificationManager.showInfo(stage, "Information", "Aucune réparation non payée pour ce véhicule.");
                }

                // Enable montant field if no reparation is selected
                if (montantDepenseField != null) {
                    montantDepenseField.setDisable(false);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (reparationComboBox.getScene() != null) {
                Stage stage = (Stage) reparationComboBox.getScene().getWindow();
                NotificationManager.showError(stage, "Erreur", "Erreur lors du chargement des réparations: " + e.getMessage());
            }
        }
    }

    private void setupTabListeners() {
        if (mainTabPane == null) return;

        mainTabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            // Use Platform.runLater to improve UI responsiveness during tab switching
            Platform.runLater(() -> {
                if (newTab == paiementTab) {
                    isDepenseTabActive = false;
                    isTarifTabActive = false;
                    clearForm();
                    disableForm(true); // Ensure form is disabled first
                    loadPaiements();
                } else if (newTab == depenseTab) {
                    isDepenseTabActive = true;
                    isTarifTabActive = false;
                    clearForm();
                    disableForm(true); // Ensure form is disabled first
                    loadDepenses();
                    // Add these two lines to load moniteurs and vehicules when switching to depense tab
                    loadMoniteurs();
                    loadVehicules();
                } else if (newTab == tarifTab) {
                    isDepenseTabActive = false;
                    isTarifTabActive = true;
                    clearForm();
                    disableForm(true); // Ensure form is disabled first
                    loadTarifs();
                }
            });
        });
    }

    private void setupSelectionListeners() {
        // Paiement table selection listener
        if (paiementTable != null) {
            paiementTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    selectedPaiement = newSelection;
                    fillPaiementForm(selectedPaiement);
                    // Keep form disabled until modify button is clicked
                    disableForm(true);
                    if (paiementModifierBtn != null) paiementModifierBtn.setDisable(false);
                    if (paiementSupprimerBtn != null) paiementSupprimerBtn.setDisable(false);
                } else {
                    selectedPaiement = null;
                    clearForm();
                    disableForm(true);
                    if (paiementModifierBtn != null) paiementModifierBtn.setDisable(true);
                    if (paiementSupprimerBtn != null) paiementSupprimerBtn.setDisable(true);
                }
            });
        }

        // Depense table selection listener
        if (depenseTable != null) {
            depenseTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    selectedDepense = newSelection;
                    fillDepenseForm(selectedDepense);
                    // Keep form disabled until modify button is clicked
                    disableForm(true);
                    if (depenseModifierBtn != null) depenseModifierBtn.setDisable(false);
                    if (depenseSupprimerBtn != null) depenseSupprimerBtn.setDisable(false);
                } else {
                    selectedDepense = null;
                    clearForm();
                    disableForm(true);
                    if (depenseModifierBtn != null) depenseModifierBtn.setDisable(true);
                    if (depenseSupprimerBtn != null) depenseSupprimerBtn.setDisable(true);
                }
            });
        }
        // Tarif table selection listener
        if (tarifTable != null) {
            tarifTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    selectedTarif = newSelection;
                    fillTarifForm(selectedTarif);
                    // Keep form disabled until modify button is clicked
                    disableForm(true);
                    if (tarifModifierBtn != null) tarifModifierBtn.setDisable(false);
                } else {
                    selectedTarif = null;
                    clearForm();
                    disableForm(true);
                    if (tarifModifierBtn != null) tarifModifierBtn.setDisable(true);
                }
            });
        }
    }

    private void setupKeyboardShortcuts() {
        // Global keyboard shortcuts
        Platform.runLater(() -> {
            if (paiementTable != null && paiementTable.getScene() != null) {
                Scene scene = paiementTable.getScene();
                scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                    if (event.isControlDown()) {
                        if (event.getCode() == KeyCode.N) {
                            // Ctrl+N: New
                            handleAjouter(null);
                            event.consume();
                        } else if (event.getCode() == KeyCode.S) {
                            // Ctrl+S: Save
                            handleEnregistrer(null);
                            event.consume();
                        } else if (event.getCode() == KeyCode.D) {
                            // Ctrl+D: Delete
                            handleSupprimer(null);
                            event.consume();
                        } else if (event.getCode() == KeyCode.F) {
                            // Ctrl+F: Filter/Search
                            if (paiementTab != null && paiementTab.isSelected() && paiementRechercheField != null) {
                                paiementRechercheField.requestFocus();
                            } else if (depenseTab != null && depenseTab.isSelected() && depenseRechercheField != null) {
                                depenseRechercheField.requestFocus();
                            } else if (tarifTab != null && tarifTab.isSelected() && tarifRechercheField != null) {
                                tarifRechercheField.requestFocus();
                            }
                            event.consume();
                        } else if (event.getCode() == KeyCode.B) {
                            // Ctrl+B: Dashboard
                            toggleDashboard();
                            event.consume();
                        }
                    } else if (event.getCode() == KeyCode.ESCAPE) {
                        // Escape: Cancel
                        handleAnnuler(null);
                        event.consume();
                    }
                });
            }
        });
    }
    // Add this method to load data in background
    private void loadDataInBackground(Runnable dataLoadingTask) {


        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                dataLoadingTask.run();
                return null;
            }
        };

        task.setOnSucceeded(e -> {
            Platform.runLater(() -> {
            });
        });

        new Thread(task).start();
    }

    private void loadInitialData() {
        // Only load what's needed for the current tab
        if (paiementTab.isSelected()) {
            loadPaiements();
        } else if (depenseTab.isSelected()) {
            loadDepenses();
        } else if (tarifTab.isSelected()) {
            loadTarifs();
        }

        // Load common data needed for all tabs
        loadCandidats();

        // Add these two lines to load moniteurs and vehicules data
        loadMoniteurs();
        loadVehicules();
    }

    private void loadCandidats() {
        try {
            List<Candidat> allCandidats = candidatService.getAllCandidats();
            candidatMap.clear();

            // Filter candidates with remaining balance
            List<Candidat> candidatsWithBalance = new ArrayList<>();

            for (Candidat candidat : allCandidats) {
                candidatMap.put(candidat.getId(), candidat);

                // Calculate if candidate has remaining balance
                double totalDu = calculateTotalDuForCandidat(candidat.getId());
                double totalPaye = calculateTotalPayeForCandidat(candidat.getId());
                double resteAPayer = totalDu - totalPaye;

                // Only add candidates with positive balance and total due > 0
                // Use a small epsilon value to account for floating-point precision issues
                if (resteAPayer > 0.01 && totalDu > 0) {
                    candidatsWithBalance.add(candidat);
                }
            }

            // Update ComboBoxes with filtered candidates
            if (candidatComboBox != null) {
                candidatComboBox.setItems(FXCollections.observableArrayList(candidatsWithBalance));
            }

            // For filter ComboBox, include all candidates plus null option
            if (filterCandidatComboBox != null) {
                ObservableList<Candidat> filterCandidats = FXCollections.observableArrayList(allCandidats);
                filterCandidats.add(0, null);
                filterCandidatComboBox.setItems(filterCandidats);
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (paiementTable != null && paiementTable.getScene() != null) {
                Stage stage = (Stage) paiementTable.getScene().getWindow();
                NotificationManager.showError(stage, "Erreur", "Erreur lors du chargement des candidats: " + e.getMessage());
            }
        }
    }

    private void loadMoniteurs() {
        try {
            List<Moniteur> moniteurs = moniteurService.getAllMoniteurs();
            moniteurMap.clear();
            for (Moniteur moniteur : moniteurs) {
                moniteurMap.put(moniteur.getId(), moniteur);
            }

            // Update ComboBox
            if (moniteurComboBox != null) {
                moniteurComboBox.setItems(FXCollections.observableArrayList(moniteurs));
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (paiementTable != null && paiementTable.getScene() != null) {
                Stage stage = (Stage) paiementTable.getScene().getWindow();
                NotificationManager.showError(stage, "Erreur", "Erreur lors du chargement des moniteurs: " + e.getMessage());
            }
        }
    }

    private void loadVehicules() {
        try {
            List<Vehicule> vehicules = vehiculeService.getAllVehicules();
            vehiculeMap.clear();
            for (Vehicule vehicule : vehicules) {
                vehiculeMap.put(vehicule.getId(), vehicule);
            }

            // Update ComboBox
            if (vehiculeComboBox != null) {
                vehiculeComboBox.setItems(FXCollections.observableArrayList(vehicules));
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (paiementTable != null && paiementTable.getScene() != null) {
                Stage stage = (Stage) paiementTable.getScene().getWindow();
                NotificationManager.showError(stage, "Erreur", "Erreur lors du chargement des véhicules: " + e.getMessage());
            }
        }
    }

    private void loadPaiements() {
        try {
            List<Paiement> allPaiements = paiementService.getAllPaiements();
            paiements.clear();
            paiements.addAll(allPaiements);
            if (paiementTable != null) {
                paiementTable.setItems(paiements);
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (paiementTable != null && paiementTable.getScene() != null) {
                Stage stage = (Stage) paiementTable.getScene().getWindow();
                NotificationManager.showError(stage, "Erreur", "Erreur lors du chargement des paiements: " + e.getMessage());
            }
        }
    }

    private void loadDepenses() {
        try {
            List<Depense> allDepenses = depenseService.getAllDepenses();
            depenses.clear();
            depenses.addAll(allDepenses);
            if (depenseTable != null) {
                depenseTable.setItems(depenses);
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (depenseTable != null && depenseTable.getScene() != null) {
                Stage stage = (Stage) depenseTable.getScene().getWindow();
                NotificationManager.showError(stage, "Erreur", "Erreur lors du chargement des dépenses: " + e.getMessage());
            }
        }
    }

    private void loadTarifs() {
        try {
            List<Tarif> allTarifs = tarifService.getAllTarifs();
            tarifs.clear();
            tarifs.addAll(allTarifs);
            if (tarifTable != null) {
                tarifTable.setItems(tarifs);
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (tarifTable != null && tarifTable.getScene() != null) {
                Stage stage = (Stage) tarifTable.getScene().getWindow();
                NotificationManager.showError(stage, "Erreur", "Erreur lors du chargement des tarifs: " + e.getMessage());
            }
        }
    }
    private void fillPaiementForm(Paiement paiement) {
        if (paiement == null) return;

        // Set form values
        if (candidatComboBox != null) candidatComboBox.setValue(candidatMap.get(paiement.getCandidatId()));
        if (datePaiementPicker != null) datePaiementPicker.setValue(paiement.getDatePaiement());
        if (montantField != null) montantField.setText(String.format("%.2f", paiement.getMontant()));
        if (methodePaiementComboBox != null) methodePaiementComboBox.setValue(paiement.getMethodePaiement());
        if (referenceField != null) referenceField.setText(paiement.getReference());
        if (statutComboBox != null) statutComboBox.setValue(paiement.getStatut());
        if (notesField != null) notesField.setText(paiement.getNotes());

        // Set payment type
        if (paiement.getStatut().equals("COMPLET")) {
            if (paiementCompletRadio != null) paiementCompletRadio.setSelected(true);
            if (remiseField != null) remiseField.setText(String.format("%.2f", paiement.getRemise()));
            if (remiseLabel != null) remiseLabel.setVisible(true);
            if (remiseField != null) remiseField.setVisible(true);

            // For complete payments with discount, update the UI to show correct values
            if (paiement.getRemise() > 0) {
                Long candidatId = paiement.getCandidatId();
                double totalDu = calculateTotalDuForCandidat(candidatId);
                double discountedAmount = totalDu * (1 - (paiement.getRemise() / 100));

                // Update labels to reflect the discounted amount
                if (totalDuLabel != null) totalDuLabel.setText("Total dû: " + String.format("%.2f", discountedAmount) + " DT");
                if (totalPayeLabel != null) totalPayeLabel.setText("Total payé: " + String.format("%.2f", discountedAmount) + " DT");
                if (resteAPayerLabel != null) resteAPayerLabel.setText("Reste à payer: 0.00 DT");

                // Set progress bar to 100%
                if (paiementProgressBar != null) {
                    paiementProgressBar.setProgress(1.0);
                    paiementProgressBar.setStyle("-fx-accent: #10b981;"); // Green for complete
                }

                // Update payment status badge
                updatePaymentStatusBadge("COMPLET");

                // Skip the regular updatePaiementProgress call
                return;
            }
        } else {
            if (paiementPartielRadio != null) paiementPartielRadio.setSelected(true);
            if (remiseLabel != null) remiseLabel.setVisible(false);
            if (remiseField != null) remiseField.setVisible(false);
        }

        // Show montant field
        if (montantLabel != null) montantLabel.setVisible(true);
        if (montantField != null) montantField.setVisible(true);
        if (maxMontantLabel != null) {
            double resteAPayer = calculateResteAPayer(paiement.getCandidatId());
            maxMontantLabel.setText("(Max: " + String.format("%.2f", resteAPayer) + " DT)");
            maxMontantLabel.setVisible(true);
        }

        // Update payment progress
        updatePaiementProgress(paiement.getCandidatId());

        // Update status badge
        updatePaymentStatusBadge(paiement.getStatut());
    }

    private void fillDepenseForm(Depense depense) {
        if (depense == null) return;

        // Set form values
        if (categorieComboBox != null) categorieComboBox.setValue(depense.getCategorie());
        if (dateDepensePicker != null) dateDepensePicker.setValue(depense.getDateDepense());
        if (montantDepenseField != null) montantDepenseField.setText(String.format("%.2f", depense.getMontant()));
        if (descriptionField != null) descriptionField.setText(depense.getDescription());

        // Update form based on category
        updateDepenseFormByCategory(depense.getCategorie());

        // Set category-specific values
        if (depense.getCategorie().equals("MONITEUR")) {
            if (moniteurComboBox != null) moniteurComboBox.setValue(moniteurMap.get(depense.getMoniteurId()));
        } else if (depense.getCategorie().equals("VEHICULE")) {
            if (vehiculeComboBox != null) vehiculeComboBox.setValue(vehiculeMap.get(depense.getVehiculeId()));

            // Set type if available
            if (depense.getTypeVehiculeDepense() != null && typeVehiculeDepenseComboBox != null) {
                typeVehiculeDepenseComboBox.setValue(depense.getTypeVehiculeDepense());

                if (depense.getTypeVehiculeDepense().equals("REPARATION") && depense.getReparationId() != null) {
                    loadReparationsForVehicule(depense.getVehiculeId());

                    // Find and select the reparation
                    if (reparationComboBox != null) {
                        for (Reparation reparation : reparationComboBox.getItems()) {
                            if (reparation.getId().equals(depense.getReparationId())) {
                                reparationComboBox.setValue(reparation);
                                break;
                            }
                        }
                    }
                }
            }
        } else if (depense.getCategorie().equals("AUTRES")) {
            if (typeAutreDepenseField != null) typeAutreDepenseField.setText(depense.getTypeAutreDepense());
        }
    }

    private void fillTarifForm(Tarif tarif) {
        if (tarif == null) return;

        // Set form values
        if (typeServiceTarifComboBox != null) typeServiceTarifComboBox.setValue(tarif.getTypeService());
        if (montantTarifField != null) montantTarifField.setText(String.format("%.2f", tarif.getMontant()));
        if (remiseTarifField != null) remiseTarifField.setText(String.format("%.2f", tarif.getRemise()));
        if (descriptionTarifField != null) descriptionTarifField.setText(tarif.getDescription());
    }

    private void clearForm() {
        if (isDepenseTabActive) {
            // Clear depense form
            if (categorieComboBox != null) categorieComboBox.setValue(null);
            if (dateDepensePicker != null) dateDepensePicker.setValue(LocalDate.now());
            if (montantDepenseField != null) montantDepenseField.clear();
            if (descriptionField != null) descriptionField.clear();
            if (moniteurComboBox != null) moniteurComboBox.setValue(null);
            if (vehiculeComboBox != null) vehiculeComboBox.setValue(null);
            if (typeVehiculeDepenseComboBox != null) typeVehiculeDepenseComboBox.setValue(null);
            if (reparationComboBox != null) reparationComboBox.setValue(null);
            if (typeAutreDepenseField != null) typeAutreDepenseField.clear();

            // Hide all containers
            if (moniteurContainer != null) moniteurContainer.setVisible(false);
            if (vehiculeContainer != null) vehiculeContainer.setVisible(false);
            if (reparationContainer != null) reparationContainer.setVisible(false);
            if (autresContainer != null) autresContainer.setVisible(false);

            // Clear selection
            if (depenseTable != null) depenseTable.getSelectionModel().clearSelection();
            selectedDepense = null;
        } else if (isTarifTabActive) {
            // Clear tarif form
            if (typeServiceTarifComboBox != null) typeServiceTarifComboBox.setValue(null);
            if (montantTarifField != null) montantTarifField.clear();
            if (remiseTarifField != null) remiseTarifField.clear();
            if (descriptionTarifField != null) descriptionTarifField.clear();

            // Clear selection
            if (tarifTable != null) tarifTable.getSelectionModel().clearSelection();
            selectedTarif = null;
        } else {
            // Clear paiement form
            if (candidatComboBox != null) candidatComboBox.setValue(null);
            if (datePaiementPicker != null) datePaiementPicker.setValue(LocalDate.now());
            if (montantField != null) montantField.clear();
            if (methodePaiementComboBox != null) methodePaiementComboBox.setValue(null);
            if (referenceField != null) referenceField.clear();
            if (statutComboBox != null) statutComboBox.setValue(null);
            if (notesField != null) notesField.clear();
            if (remiseField != null) remiseField.clear();

            // Reset radio buttons
            if (paiementCompletRadio != null) paiementCompletRadio.setSelected(false);
            if (paiementPartielRadio != null) paiementPartielRadio.setSelected(false);

            // Hide fields
            if (montantLabel != null) montantLabel.setVisible(false);
            if (montantField != null) montantField.setVisible(false);
            if (maxMontantLabel != null) maxMontantLabel.setVisible(false);
            if (remiseLabel != null) remiseLabel.setVisible(false);
            if (remiseField != null) remiseField.setVisible(false);

            // Reset progress bar
            if (paiementProgressBar != null) paiementProgressBar.setProgress(0);
            if (totalDuLabel != null) totalDuLabel.setText("Total dû: 0.00 DT");
            if (totalPayeLabel != null) totalPayeLabel.setText("Total payé: 0.00 DT");
            if (resteAPayerLabel != null) resteAPayerLabel.setText("Reste à payer: 0.00 DT");

            // Reset status badge
            if (paymentStatusBadge != null) {
                paymentStatusBadge.setText("Nouveau paiement");
                paymentStatusBadge.getStyleClass().removeAll("payment-status-complete", "payment-status-partial", "payment-status-pending");
            }

            // Clear selection
            if (paiementTable != null) paiementTable.getSelectionModel().clearSelection();
            selectedPaiement = null;
        }
    }

    private void applyRoleBasedAccess() {
        User currentUser = SessionManager.getCurrentUser();

        // Restrict access to certain features based on role
        if (paiementSupprimerBtn != null) {
            paiementSupprimerBtn.setVisible(true); // Always visible
            paiementSupprimerBtn.setDisable(!currentUser.canDeleteCandidats()); // But disabled for non-managers
        }
        if (depenseSupprimerBtn != null) {
            depenseSupprimerBtn.setVisible(true);
            depenseSupprimerBtn.setDisable(!currentUser.canDeleteCandidats());
        }
        if (tarifModifierBtn != null) tarifModifierBtn.setVisible(currentUser.canDeleteCandidats());
        if (dashboardBtn != null) dashboardBtn.setVisible(currentUser.canDeleteCandidats());
    }


    private void updatePaiementProgress(Long candidatId) {
        if (candidatId == null) return;

        double totalDu = calculateTotalDuForCandidat(candidatId);
        double totalPaye = calculateTotalPayeForCandidat(candidatId);
        double resteAPayer = totalDu - totalPaye;

        // Update progress bar
        if (paiementProgressBar != null) {
            double progress = totalDu > 0 ? totalPaye / totalDu : 0;
            paiementProgressBar.setProgress(progress);

            // Change color based on progress
            if (progress >= 1.0) {
                paiementProgressBar.setStyle("-fx-accent: #10b981;"); // Green for complete
            } else if (progress >= 0.5) {
                paiementProgressBar.setStyle("-fx-accent: #f59e0b;"); // Orange for partial
            } else {
                paiementProgressBar.setStyle("-fx-accent: #ef4444;"); // Red for low
            }
        }

        // Update labels
        if (totalDuLabel != null) totalDuLabel.setText("Total dû: " + String.format("%.2f", totalDu) + " DT");
        if (totalPayeLabel != null) totalPayeLabel.setText("Total payé: " + String.format("%.2f", totalPaye) + " DT");
        if (resteAPayerLabel != null) resteAPayerLabel.setText("Reste à payer: " + String.format("%.2f", resteAPayer) + " DT");

        // Store max montant for validation
        maxMontant = resteAPayer;
    }

    private void updatePaiementProgressBar(double montant) {
        if (candidatComboBox == null || candidatComboBox.getValue() == null) return;

        Long candidatId = candidatComboBox.getValue().getId();
        double totalDu = calculateTotalDuForCandidat(candidatId);
        double totalPaye = calculateTotalPayeForCandidat(candidatId);

        // Add current montant to simulate progress
        double simulatedProgress = totalDu > 0 ? (totalPaye + montant) / totalDu : 0;

        // Cap at 100%
        simulatedProgress = Math.min(simulatedProgress, 1.0);

        if (paiementProgressBar != null) {
            paiementProgressBar.setProgress(simulatedProgress);

            // Change color based on progress
            if (simulatedProgress >= 1.0) {
                paiementProgressBar.setStyle("-fx-accent: #10b981;"); // Green for complete
            } else if (simulatedProgress >= 0.5) {
                paiementProgressBar.setStyle("-fx-accent: #f59e0b;"); // Orange for partial
            } else {
                paiementProgressBar.setStyle("-fx-accent: #ef4444;"); // Red for low
            }
        }
    }

    private void updatePaymentStatusBadge(String status) {
        if (paymentStatusBadge == null) return;

        paymentStatusBadge.setText(status);
        paymentStatusBadge.getStyleClass().removeAll("payment-status-complete", "payment-status-partial", "payment-status-pending");

        if (status.equals("COMPLET")) {
            paymentStatusBadge.getStyleClass().add("payment-status-complete");
        } else if (status.equals("PARTIEL")) {
            paymentStatusBadge.getStyleClass().add("payment-status-partial");
        } else {
            paymentStatusBadge.getStyleClass().add("payment-status-pending");
        }
    }

    private double calculateTotalDuForCandidat(Long candidatId) {
        if (candidatId == null) return 0;

        try {
            // Calculate total due based on services used by the candidate
            double totalSeanceCode = seanceService.countSeancesByTypeAndCandidat(TypeSeance.Code, candidatId) *
                    tarifService.getMontantTarifByTypeService("SEANCE_CODE");

            double totalSeanceConduite = seanceService.countSeancesByTypeAndCandidat(TypeSeance.Conduite, candidatId) *
                    tarifService.getMontantTarifByTypeService("SEANCE_CONDUITE");

            double totalExamenCode = examenService.countExamensByTypeAndCandidat("CODE", candidatId) *
                    tarifService.getMontantTarifByTypeService("EXAMEN_CODE");

            double totalExamenConduite = examenService.countExamensByTypeAndCandidat("CONDUITE", candidatId) *
                    tarifService.getMontantTarifByTypeService("EXAMEN_CONDUITE");

            return totalSeanceCode + totalSeanceConduite + totalExamenCode + totalExamenConduite;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    private double calculateTotalPayeForCandidat(Long candidatId) {
        if (candidatId == null) return 0;

        try {
            // Use the existing method from PaiementService
            return paiementService.getTotalPaiementsByCandidatId(candidatId);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    private double calculateResteAPayer(Long candidatId) {
        if (candidatId == null) return 0;

        double totalDu = calculateTotalDuForCandidat(candidatId);
        double totalPaye = calculateTotalPayeForCandidat(candidatId);
        return Math.max(0, totalDu - totalPaye);
    }

    @FXML
    private void handleAjouter(ActionEvent event) {
        clearForm();
        disableForm(false);

        if (isDepenseTabActive) {
            if (depenseModifierBtn != null) depenseModifierBtn.setDisable(true);
            if (depenseSupprimerBtn != null) depenseSupprimerBtn.setDisable(true);
        } else if (isTarifTabActive) {
            // Tarifs can only be modified, not added
        } else {
            if (paiementModifierBtn != null) paiementModifierBtn.setDisable(true);
            if (paiementSupprimerBtn != null) paiementSupprimerBtn.setDisable(true);
        }
    }

    @FXML
    private void handleModifier(ActionEvent event) {
        if (isDepenseTabActive) {
            if (selectedDepense == null) {
                showAlert("Aucune dépense sélectionnée", "Veuillez sélectionner une dépense à modifier.");
                return;
            }
            fillDepenseForm(selectedDepense);
        } else if (isTarifTabActive) {
            if (selectedTarif == null) {
                showAlert("Aucun tarif sélectionné", "Veuillez sélectionner un tarif à modifier.");
                return;
            }
            fillTarifForm(selectedTarif);
        } else {
            if (selectedPaiement == null) {
                showAlert("Aucun paiement sélectionné", "Veuillez sélectionner un paiement à modifier.");
                return;
            }
            fillPaiementForm(selectedPaiement);
        }

        disableForm(false);
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
            //settingsStage.getIcons().add(new Image(getClass().getResourceAsStream("/org/example/Images/settings_icon.png")));

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


    @FXML
    private void handleSupprimer(ActionEvent event) {
        if (isDepenseTabActive) {
            if (selectedDepense == null) {
                Stage stage = (Stage) depenseSupprimerBtn.getScene().getWindow();
                NotificationManager.showWarning(stage, "Sélection requise", "Veuillez sélectionner une dépense à supprimer");
                return;
            }

            // Rest of the delete code for depense
            Stage stage = (Stage) depenseSupprimerBtn.getScene().getWindow();
            boolean confirmed = ConfirmationDialog.show(
                    stage,
                    "Confirmation de suppression",
                    "Supprimer la dépense",
                    "Êtes-vous sûr de vouloir supprimer cette dépense ? Cette action est irréversible.",
                    ConfirmationDialog.DialogType.DELETE
            );

            if (confirmed) {
                try {
                    depenseService.deleteDepense(selectedDepense.getId());
                    loadDepenses();
                    clearForm();
                    disableForm(true);

                    NotificationManager.showSuccess(
                            stage,
                            "Succès",
                            "La dépense a été supprimée avec succès"
                    );
                } catch (Exception e) {
                    NotificationManager.showError(
                            stage,
                            "Erreur",
                            "Erreur lors de la suppression de la dépense: " + e.getMessage()
                    );
                    e.printStackTrace();
                }
            }
        } else {


            if (selectedPaiement == null) {
                Stage stage = (Stage) paiementSupprimerBtn.getScene().getWindow();
                NotificationManager.showWarning(stage, "Sélection requise", "Veuillez sélectionner un paiement à supprimer");
                return;
            }

            Stage stage = (Stage) paiementSupprimerBtn.getScene().getWindow();
            boolean confirmed = ConfirmationDialog.show(
                    stage,
                    "Confirmation de suppression",
                    "Supprimer le paiement",
                    "Êtes-vous sûr de vouloir supprimer ce paiement ? Cette action est irréversible.",
                    ConfirmationDialog.DialogType.DELETE
            );

            if (confirmed) {
                try {
                    // Log the delete action
                    auditLogService.logAction(
                            "DELETE",
                            "PAIEMENT",
                            selectedPaiement.getId(),
                            "Suppression du paiement ID: " + selectedPaiement.getId()
                    );

                    paiementService.deletePaiement(selectedPaiement.getId());
                    loadPaiements();
                    clearForm();
                    disableForm(true);

                    NotificationManager.showSuccess(
                            stage,
                            "Succès",
                            "Le paiement a été supprimé avec succès"
                    );
                } catch (Exception e) {
                    NotificationManager.showError(
                            stage,
                            "Erreur",
                            "Erreur lors de la suppression du paiement: " + e.getMessage()
                    );
                    e.printStackTrace();
                }
            }
        }
    }



    @FXML
    private void handleAnnuler(ActionEvent event) {
        clearForm();
        disableForm(true);
    }

    @FXML
    private void handleEnregistrer(ActionEvent event) {
        if (isDepenseTabActive) {
            saveDepense();
        } else if (isTarifTabActive) {
            saveTarif();
        } else {
            savePaiement();
        }
    }

    private void savePaiement() {
        try {
            // Validate form
            if (candidatComboBox == null || candidatComboBox.getValue() == null) {
                Stage stage = (Stage) paiementEnregistrerBtn.getScene().getWindow();
                NotificationManager.showWarning(stage, "Champ obligatoire", "Veuillez sélectionner un candidat.");
                return;
            }

            if (typePaiementGroup == null || typePaiementGroup.getSelectedToggle() == null) {
                Stage stage = (Stage) paiementEnregistrerBtn.getScene().getWindow();
                NotificationManager.showWarning(stage, "Champ obligatoire", "Veuillez sélectionner un type de paiement.");
                return;
            }

            if (montantField == null || montantField.getText().isEmpty()) {
                Stage stage = (Stage) paiementEnregistrerBtn.getScene().getWindow();
                NotificationManager.showWarning(stage, "Champ obligatoire", "Veuillez saisir un montant.");
                return;
            }

            if (methodePaiementComboBox == null || methodePaiementComboBox.getValue() == null) {
                Stage stage = (Stage) paiementEnregistrerBtn.getScene().getWindow();
                NotificationManager.showWarning(stage, "Champ obligatoire", "Veuillez sélectionner une méthode de paiement.");
                return;
            }

            // Get form values
            Candidat candidat = candidatComboBox.getValue();
            LocalDate datePaiement = datePaiementPicker.getValue();
            double montant = Double.parseDouble(montantField.getText());
            String methodePaiement = methodePaiementComboBox.getValue();
            String reference = referenceField.getText();

            // Determine payment status based on radio button selection
            String statut = paiementCompletRadio.isSelected() ? "COMPLET" : "PARTIEL";

            // Get remise if applicable
            double remise = 0;
            if (paiementCompletRadio.isSelected() && remiseField != null && !remiseField.getText().isEmpty()) {
                remise = Double.parseDouble(remiseField.getText());
            }

            // Create or update paiement
            Paiement paiement;
            Stage stage = (Stage) paiementEnregistrerBtn.getScene().getWindow();

            if (selectedPaiement == null) {
                // Create confirmation dialog for new payment
                boolean confirmed = ConfirmationDialog.show(
                        stage,
                        "Confirmation d'enregistrement",
                        "Enregistrer le paiement",
                        "Êtes-vous sûr de vouloir enregistrer ce paiement ?",
                        ConfirmationDialog.DialogType.SAVE
                );

                if (!confirmed) return;

                // Create new paiement
                paiement = new Paiement();
                paiement.setCandidatId(candidat.getId());
                paiement.setDatePaiement(datePaiement);
                paiement.setMontant(montant);
                paiement.setMethodePaiement(methodePaiement);
                paiement.setReference(reference);
                paiement.setStatut(statut);
                paiement.setNotes("");
                paiement.setRemise(remise);

                // Save to database using the appropriate method from PaiementService
                paiementService.savePaiement(paiement);

                // Log the action
                auditLogService.logAction("CREATE", "PAIEMENT", paiement.getId(),
                        "Création d'un nouveau paiement pour " + candidat.getNom() + " " + candidat.getPrenom());

                NotificationManager.showSuccess(stage, "Succès", "Le paiement a été enregistré avec succès.");
            } else {
                // Create confirmation dialog for updating payment
                boolean confirmed = ConfirmationDialog.show(
                        stage,
                        "Confirmation de modification",
                        "Modifier le paiement",
                        "Êtes-vous sûr de vouloir modifier ce paiement ?",
                        ConfirmationDialog.DialogType.SAVE
                );

                if (!confirmed) return;

                // Update existing paiement
                paiement = selectedPaiement;
                paiement.setCandidatId(candidat.getId());
                paiement.setDatePaiement(datePaiement);
                paiement.setMontant(montant);
                paiement.setMethodePaiement(methodePaiement);
                paiement.setReference(reference);
                paiement.setStatut(statut);
                paiement.setNotes("");
                paiement.setRemise(remise);

                // Save to database using the appropriate method from PaiementService
                paiementService.updatePaiement(paiement);

                // Log the action
                auditLogService.logAction("UPDATE", "PAIEMENT", paiement.getId(),
                        "Modification du paiement pour " + candidat.getNom() + " " + candidat.getPrenom());

                NotificationManager.showSuccess(stage, "Succès", "Le paiement a été mis à jour avec succès.");
            }

            // Refresh data and reset form
            loadPaiements();
            clearForm();
            disableForm(true);
        } catch (NumberFormatException e) {
            Stage stage = (Stage) paiementEnregistrerBtn.getScene().getWindow();
            NotificationManager.showWarning(stage, "Format invalide", "Le montant doit être un nombre valide.");
        } catch (Exception e) {
            e.printStackTrace();
            Stage stage = (Stage) paiementEnregistrerBtn.getScene().getWindow();
            NotificationManager.showError(stage, "Erreur", "Erreur lors de l'enregistrement du paiement: " + e.getMessage());
        }
    }

    private void saveDepense() {
        try {
            // Validate form
            if (categorieComboBox == null || categorieComboBox.getValue() == null) {
                Stage stage = (Stage) depenseEnregistrerBtn.getScene().getWindow();
                NotificationManager.showWarning(stage, "Champ obligatoire", "Veuillez sélectionner une catégorie.");
                return;
            }

            if (montantDepenseField == null || montantDepenseField.getText().isEmpty()) {
                Stage stage = (Stage) depenseEnregistrerBtn.getScene().getWindow();
                NotificationManager.showWarning(stage, "Champ obligatoire", "Veuillez saisir un montant.");
                return;
            }

            // Get form values
            String categorie = categorieComboBox.getValue();
            LocalDate dateDepense = dateDepensePicker.getValue();
            double montant = Double.parseDouble(montantDepenseField.getText());
            String description = descriptionField.getText();

            // Create or update depense
            Depense depense;
            Stage stage = (Stage) depenseEnregistrerBtn.getScene().getWindow();

            if (selectedDepense == null) {
                // Create confirmation dialog for new expense
                boolean confirmed = ConfirmationDialog.show(
                        stage,
                        "Confirmation d'enregistrement",
                        "Enregistrer la dépense",
                        "Êtes-vous sûr de vouloir enregistrer cette dépense ?",
                        ConfirmationDialog.DialogType.SAVE
                );

                if (!confirmed) return;

                // Create new depense
                depense = new Depense();
                depense.setCategorie(categorie);
                depense.setDateDepense(dateDepense);
                depense.setMontant(montant);
                depense.setDescription(description);

                // Set category-specific values
                if (categorie.equals("MONITEUR")) {
                    if (moniteurComboBox == null || moniteurComboBox.getValue() == null) {
                        NotificationManager.showWarning(stage, "Champ obligatoire", "Veuillez sélectionner un moniteur.");
                        return;
                    }
                    depense.setMoniteurId(moniteurComboBox.getValue().getId());
                } else if (categorie.equals("VEHICULE")) {
                    if (vehiculeComboBox == null || vehiculeComboBox.getValue() == null) {
                        NotificationManager.showWarning(stage, "Champ obligatoire", "Veuillez sélectionner un véhicule.");
                        return;
                    }
                    depense.setVehiculeId(vehiculeComboBox.getValue().getId());

                    if (typeVehiculeDepenseComboBox != null && typeVehiculeDepenseComboBox.getValue() != null) {
                        depense.setTypeVehiculeDepense(typeVehiculeDepenseComboBox.getValue());

                        if (typeVehiculeDepenseComboBox.getValue().equals("REPARATION")) {
                            if (reparationComboBox == null || reparationComboBox.getValue() == null) {
                                NotificationManager.showWarning(stage, "Champ obligatoire", "Veuillez sélectionner une réparation.");
                                return;
                            }
                            depense.setReparationId(reparationComboBox.getValue().getId());

                            // Mark the reparation as paid
                            Reparation reparation = reparationComboBox.getValue();
                            reparation.setPaye(true);
                            reparationService.updateReparation(reparation);
                        }
                    }
                } else if (categorie.equals("AUTRES")) {
                    if (typeAutreDepenseField != null && !typeAutreDepenseField.getText().isEmpty()) {
                        depense.setTypeAutreDepense(typeAutreDepenseField.getText());
                    }
                }

                // Save to database using the appropriate method from DepenseService
                depenseService.saveDepense(depense);

                // Log the action
                auditLogService.logAction("CREATE", "DEPENSE", depense.getId(),
                        "Création d'une nouvelle dépense de catégorie " + categorie);

                NotificationManager.showSuccess(stage, "Succès", "La dépense a été enregistrée avec succès.");
            } else {
                // Create confirmation dialog for updating expense
                boolean confirmed = ConfirmationDialog.show(
                        stage,
                        "Confirmation de modification",
                        "Modifier la dépense",
                        "Êtes-vous sûr de vouloir modifier cette dépense ?",
                        ConfirmationDialog.DialogType.SAVE
                );

                if (!confirmed) return;

                // Update existing depense
                depense = selectedDepense;
                depense.setCategorie(categorie);
                depense.setDateDepense(dateDepense);
                depense.setMontant(montant);
                depense.setDescription(description);

                // Reset all category-specific values
                depense.setMoniteurId(null);
                depense.setVehiculeId(null);
                depense.setTypeVehiculeDepense(null);
                depense.setReparationId(null);
                depense.setTypeAutreDepense(null);

                // Set category-specific values
                if (categorie.equals("MONITEUR")) {
                    if (moniteurComboBox == null || moniteurComboBox.getValue() == null) {
                        NotificationManager.showWarning(stage, "Champ obligatoire", "Veuillez sélectionner un moniteur.");
                        return;
                    }
                    depense.setMoniteurId(moniteurComboBox.getValue().getId());
                } else if (categorie.equals("VEHICULE")) {
                    if (vehiculeComboBox == null || vehiculeComboBox.getValue() == null) {
                        NotificationManager.showWarning(stage, "Champ obligatoire", "Veuillez sélectionner un véhicule.");
                        return;
                    }
                    depense.setVehiculeId(vehiculeComboBox.getValue().getId());

                    if (typeVehiculeDepenseComboBox != null && typeVehiculeDepenseComboBox.getValue() != null) {
                        depense.setTypeVehiculeDepense(typeVehiculeDepenseComboBox.getValue());

                        if (typeVehiculeDepenseComboBox.getValue().equals("REPARATION")) {
                            if (reparationComboBox == null || reparationComboBox.getValue() == null) {
                                NotificationManager.showWarning(stage, "Champ obligatoire", "Veuillez sélectionner une réparation.");
                                return;
                            }
                            depense.setReparationId(reparationComboBox.getValue().getId());

                            // Mark the reparation as paid
                            Reparation reparation = reparationComboBox.getValue();
                            reparation.setPaye(true);
                            reparationService.updateReparation(reparation);
                        }
                    }
                } else if (categorie.equals("AUTRES")) {
                    if (typeAutreDepenseField != null && !typeAutreDepenseField.getText().isEmpty()) {
                        depense.setTypeAutreDepense(typeAutreDepenseField.getText());
                    }
                }

                // Save to database using the appropriate method from DepenseService
                depenseService.updateDepense(depense);

                // Log the action
                auditLogService.logAction("UPDATE", "DEPENSE", depense.getId(),
                        "Modification de la dépense de catégorie " + categorie);

                NotificationManager.showSuccess(stage, "Succès", "La dépense a été mise à jour avec succès.");
            }

            // Refresh data and reset form
            loadDepenses();
            clearForm();
            disableForm(true);
        } catch (NumberFormatException e) {
            Stage stage = (Stage) depenseEnregistrerBtn.getScene().getWindow();
            NotificationManager.showWarning(stage, "Format invalide", "Le montant doit être un nombre valide.");
        } catch (Exception e) {
            e.printStackTrace();
            Stage stage = (Stage) depenseEnregistrerBtn.getScene().getWindow();
            NotificationManager.showError(stage, "Erreur", "Erreur lors de l'enregistrement de la dépense: " + e.getMessage());
        }
    }

    private void saveTarif() {
        try {
            // Validate form
            if (typeServiceTarifComboBox == null || typeServiceTarifComboBox.getValue() == null) {
                Stage stage = (Stage) tarifEnregistrerBtn.getScene().getWindow();
                NotificationManager.showWarning(stage, "Champ obligatoire", "Veuillez sélectionner un type de service.");
                return;
            }

            if (montantTarifField == null || montantTarifField.getText().isEmpty()) {
                Stage stage = (Stage) tarifEnregistrerBtn.getScene().getWindow();
                NotificationManager.showWarning(stage, "Champ obligatoire", "Veuillez saisir un montant.");
                return;
            }

            // Get form values
            String typeService = typeServiceTarifComboBox.getValue();
            double montant = Double.parseDouble(montantTarifField.getText());
            double remise = 0;
            if (remiseTarifField != null && !remiseTarifField.getText().isEmpty()) {
                remise = Double.parseDouble(remiseTarifField.getText());
            }
            String description = descriptionTarifField.getText();

            // Update existing tarif
            if (selectedTarif == null) {
                Stage stage = (Stage) tarifEnregistrerBtn.getScene().getWindow();
                NotificationManager.showWarning(stage, "Erreur", "Aucun tarif sélectionné pour modification.");
                return;
            }

            Stage stage = (Stage) tarifEnregistrerBtn.getScene().getWindow();
            boolean confirmed = ConfirmationDialog.show(
                    stage,
                    "Confirmation de modification",
                    "Modifier le tarif",
                    "Êtes-vous sûr de vouloir modifier ce tarif ?",
                    ConfirmationDialog.DialogType.SAVE
            );

            if (!confirmed) return;

            selectedTarif.setTypeService(typeService);
            selectedTarif.setMontant(montant);
            selectedTarif.setRemise(remise);
            selectedTarif.setDescription(description);

            // Save to database using the existing method from TarifService
            tarifService.updateTarif(selectedTarif);

            // Log the action
            auditLogService.logAction("UPDATE", "TARIF", selectedTarif.getId(),
                    "Modification du tarif pour " + typeService);

            NotificationManager.showSuccess(stage, "Succès", "Le tarif a été mis à jour avec succès.");

            // Refresh data and reset form
            loadTarifs();
            clearForm();
            disableForm(true);
        } catch (NumberFormatException e) {
            Stage stage = (Stage) tarifEnregistrerBtn.getScene().getWindow();
            NotificationManager.showWarning(stage, "Format invalide", "Le montant doit être un nombre valide.");
        } catch (Exception e) {
            e.printStackTrace();
            Stage stage = (Stage) tarifEnregistrerBtn.getScene().getWindow();
            NotificationManager.showError(stage, "Erreur", "Erreur lors de l'enregistrement du tarif: " + e.getMessage());
        }
    }

    @FXML
    private void handleRecherche(ActionEvent event) {
        if (isDepenseTabActive) {
            rechercheDepenses();
        } else if (isTarifTabActive) {
            rechercheTarifs();
        } else {
            recherchePaiements();
        }
    }

    private void recherchePaiements() {
        try {
            String searchTerm = paiementRechercheField.getText();
            // Since there's no direct search method in PaiementService, we'll filter the list manually
            if (searchTerm == null || searchTerm.trim().isEmpty()) {
                loadPaiements();
                return;
            }

            String searchTermLower = searchTerm.toLowerCase().trim();
            List<Paiement> allPaiements = paiementService.getAllPaiements();
            List<Paiement> filteredPaiements = allPaiements.stream()
                    .filter(p -> {
                        // Search in reference, method, status
                        if (p.getReference() != null && p.getReference().toLowerCase().contains(searchTermLower)) {
                            return true;
                        }
                        if (p.getMethodePaiement() != null && p.getMethodePaiement().toLowerCase().contains(searchTermLower)) {
                            return true;
                        }
                        if (p.getStatut() != null && p.getStatut().toLowerCase().contains(searchTermLower)) {
                            return true;
                        }

                        // Search in candidat name
                        Candidat candidat = candidatMap.get(p.getCandidatId());
                        if (candidat != null) {
                            String candidatName = (candidat.getNom() + " " + candidat.getPrenom()).toLowerCase();
                            if (candidatName.contains(searchTermLower)) {
                                return true;
                            }
                        }

                        return false;
                    })
                    .collect(Collectors.toList());

            paiements.clear();
            paiements.addAll(filteredPaiements);
        } catch (Exception e) {
            e.printStackTrace();
            NotificationManager.showError(
                    (Stage) paiementTable.getScene().getWindow(),
                    "Erreur",
                    "Erreur lors de la recherche: " + e.getMessage()
            );
        }
    }

    private void rechercheDepenses() {
        try {
            String searchTerm = depenseRechercheField.getText();
            // Since there's no direct search method in DepenseService, we'll filter the list manually
            if (searchTerm == null || searchTerm.trim().isEmpty()) {
                loadDepenses();
                return;
            }

            String searchTermLower = searchTerm.toLowerCase().trim();
            List<Depense> allDepenses = depenseService.getAllDepenses();
            List<Depense> filteredDepenses = allDepenses.stream()
                    .filter(d -> {
                        // Search in category, description
                        if (d.getCategorie() != null && d.getCategorie().toLowerCase().contains(searchTermLower)) {
                            return true;
                        }
                        if (d.getDescription() != null && d.getDescription().toLowerCase().contains(searchTermLower)) {
                            return true;
                        }

                        // Search in moniteur name
                        if (d.getMoniteurId() != null) {
                            Moniteur moniteur = moniteurMap.get(d.getMoniteurId());
                            if (moniteur != null) {
                                String moniteurName = (moniteur.getNom() + " " + moniteur.getPrenom()).toLowerCase();
                                if (moniteurName.contains(searchTermLower)) {
                                    return true;
                                }
                            }
                        }

                        // Search in vehicule info
                        if (d.getVehiculeId() != null) {
                            Vehicule vehicule = vehiculeMap.get(d.getVehiculeId());
                            if (vehicule != null) {
                                String vehiculeInfo = (vehicule.getMarque() + " " + vehicule.getModele() + " " + vehicule.getMatricule()).toLowerCase();
                                if (vehiculeInfo.contains(searchTermLower)) {
                                    return true;
                                }
                            }
                        }

                        return false;
                    })
                    .collect(Collectors.toList());

            depenses.clear();
            depenses.addAll(filteredDepenses);
        } catch (Exception e) {
            e.printStackTrace();
            NotificationManager.showError(
                    (Stage) depenseTable.getScene().getWindow(),
                    "Erreur",
                    "Erreur lors de la recherche: " + e.getMessage()
            );
        }
    }

    private void rechercheTarifs() {
        try {
            String searchTerm = tarifRechercheField.getText();
            // Since there's no direct search method in TarifService, we'll filter the list manually
            if (searchTerm == null || searchTerm.trim().isEmpty()) {
                loadTarifs();
                return;
            }

            String searchTermLower = searchTerm.toLowerCase().trim();
            List<Tarif> allTarifs = tarifService.getAllTarifs();
            List<Tarif> filteredTarifs = allTarifs.stream()
                    .filter(t -> {
                        // Search in type service, description
                        if (t.getTypeService() != null && t.getTypeService().toLowerCase().contains(searchTermLower)) {
                            return true;
                        }
                        if (t.getDescription() != null && t.getDescription().toLowerCase().contains(searchTermLower)) {
                            return true;
                        }

                        return false;
                    })
                    .collect(Collectors.toList());

            tarifs.clear();
            tarifs.addAll(filteredTarifs);
        } catch (Exception e) {
            e.printStackTrace();
            NotificationManager.showError(
                    (Stage) tarifTable.getScene().getWindow(),
                    "Erreur",
                    "Erreur lors de la recherche: " + e.getMessage()
            );
        }
    }

    @FXML
    private void handleApplyFilter(ActionEvent event) {
        try {
            // Get filter values
            LocalDate startDate = startDatePicker.getValue();
            LocalDate endDate = endDatePicker.getValue();
            String methodePaiement = filterMethodePaiementComboBox.getValue();
            if (methodePaiement != null && methodePaiement.equals("Toutes les méthodes")) {
                methodePaiement = null;
            }
            String statut = filterStatutComboBox.getValue();
            if (statut != null && statut.equals("Tous les statuts")) {
                statut = null;
            }
            Long candidatId;
            if (filterCandidatComboBox.getValue() != null) {
                candidatId = filterCandidatComboBox.getValue().getId();
            } else {
                candidatId = null;
            }

            // Apply filters manually since there's no direct filter method in PaiementService
            List<Paiement> allPaiements = paiementService.getAllPaiements();
            String finalMethodePaiement = methodePaiement;
            String finalStatut = statut;
            List<Paiement> filteredPaiements = allPaiements.stream()
                    .filter(p -> {
                        // Filter by date range
                        if (startDate != null && p.getDatePaiement().isBefore(startDate)) {
                            return false;
                        }
                        if (endDate != null && p.getDatePaiement().isAfter(endDate)) {
                            return false;
                        }

                        // Filter by payment method
                        if (finalMethodePaiement != null && !p.getMethodePaiement().equals(finalMethodePaiement)) {
                            return false;
                        }

                        // Filter by status
                        if (finalStatut != null && !p.getStatut().equals(finalStatut)) {
                            return false;
                        }

                        // Filter by candidat
                        if (candidatId != null && !p.getCandidatId().equals(candidatId)) {
                            return false;
                        }

                        return true;
                    })
                    .collect(Collectors.toList());

            paiements.clear();
            paiements.addAll(filteredPaiements);
        } catch (Exception e) {
            e.printStackTrace();
            NotificationManager.showError(
                    (Stage) paiementTable.getScene().getWindow(),
                    "Erreur",
                    "Erreur lors de l'application des filtres: " + e.getMessage()
            );
        }
    }

    @FXML
    private void handleResetFilter(ActionEvent event) {
        // Reset filter fields
        if (startDatePicker != null) startDatePicker.setValue(null);
        if (endDatePicker != null) endDatePicker.setValue(null);
        if (filterMethodePaiementComboBox != null) filterMethodePaiementComboBox.setValue("Toutes les méthodes");
        if (filterStatutComboBox != null) filterStatutComboBox.setValue("Tous les statuts");
        if (filterCandidatComboBox != null) filterCandidatComboBox.setValue(null);

        // Reload all paiements
        loadPaiements();
    }

    @FXML
    private void handleExportPaiements(ActionEvent event) {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Exporter les paiements");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
            fileChooser.setInitialFileName("paiements_" + LocalDate.now() + ".csv");

            File file = fileChooser.showSaveDialog((Stage) paiementTable.getScene().getWindow());
            if (file != null) {
                exportPaiementsToCSV(file);

                NotificationManager.showSuccess(
                        (Stage) paiementTable.getScene().getWindow(),
                        "Succès",
                        "Les paiements ont été exportés avec succès."
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
            NotificationManager.showError(
                    (Stage) paiementTable.getScene().getWindow(),
                    "Erreur",
                    "Erreur lors de l'exportation des paiements: " + e.getMessage()
            );
        }
    }

    private void exportPaiementsToCSV(File file) throws IOException {
        try (FileWriter writer = new FileWriter(file)) {
            // Write header
            writer.write("ID,Candidat,Date,Montant,Méthode,Référence,Statut,Notes\n");

            // Write data
            for (Paiement paiement : paiements) {
                Candidat candidat = candidatMap.get(paiement.getCandidatId());
                String candidatName = candidat != null ? candidat.getNom() + " " + candidat.getPrenom() : "Inconnu";

                writer.write(paiement.getId() + ",");
                writer.write("\"" + candidatName + "\",");
                writer.write(paiement.getDatePaiement() + ",");
                writer.write(paiement.getMontant() + ",");
                writer.write(paiement.getMethodePaiement() + ",");
                writer.write("\"" + paiement.getReference() + "\",");
                writer.write(paiement.getStatut() + ",");
                writer.write("\"" + paiement.getNotes() + "\"\n");
            }
        }
    }

    @FXML
    private void handleGenererReference(ActionEvent event) {
        if (referenceField == null) return;

        // Generate a unique reference
        String prefix = "PAY";
        String timestamp = String.valueOf(System.currentTimeMillis()).substring(6);
        String random = String.valueOf((int) (Math.random() * 1000));
        String reference = prefix + "-" + timestamp + "-" + random;

        referenceField.setText(reference);
    }

    @FXML
    private void toggleDashboard() {
        isDashboardVisible = !isDashboardVisible;
        formScrollPane.setVisible(!isDashboardVisible);
        dashboardPane.setVisible(isDashboardVisible);

        /*if (isDashboardVisible) {
            refreshDashboard();
        }*/
    }


    @FXML
    private void handleRefreshDashboard(ActionEvent event) {
        updateDashboard();
    }

    private void updateDashboard() {
        try {
            // Calculate financial metrics
            double totalRevenus = paiementService.getTotalPaiements();
            double totalDepensesAmount = depenseService.getTotalDepenses();
            double beneficeNet = totalRevenus - totalDepensesAmount;
            double tauxRentabilite = totalRevenus > 0 ? (beneficeNet / totalRevenus) * 100 : 0;

            // Update labels
            if (totalRevenusLabel != null) totalRevenusLabel.setText(String.format("%.2f DT", totalRevenus));
            if (totalDepensesLabel != null) totalDepensesLabel.setText(String.format("%.2f DT", totalDepensesAmount));
            if (beneficeNetLabel != null) beneficeNetLabel.setText(String.format("%.2f DT", beneficeNet));
            if (tauxRentabiliteLabel != null) tauxRentabiliteLabel.setText(String.format("%.2f%%", tauxRentabilite));

            // Update charts
            updateRevenusChart();
            updateDepensesChart();
            updateMethodePaiementChart();
            updateMensuelChart();
        } catch (Exception e) {
            e.printStackTrace();
            if (dashboardPane != null && dashboardPane.getScene() != null) {
                NotificationManager.showError(
                        (Stage) dashboardPane.getScene().getWindow(),
                        "Erreur",
                        "Erreur lors de la mise à jour du tableau de bord: " + e.getMessage()
                );
            }
        }
    }

    private void updateRevenusChart() {
        if (revenusByTypeChart == null) return;

        try {
            // Get revenue by type using the existing method from PaiementService
            Map<String, Double> revenueByType = paiementService.getPaiementsByTypeServiceSummary();

            // Create chart data
            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
            for (Map.Entry<String, Double> entry : revenueByType.entrySet()) {
                pieChartData.add(new PieChart.Data(entry.getKey(), entry.getValue()));
            }

            revenusByTypeChart.setData(pieChartData);
            revenusByTypeChart.setTitle("Revenus par type de service");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateDepensesChart() {
        if (depensesByCategorieChart == null) return;

        try {
            // Get expenses by category using the existing method from DepenseService
            Map<String, Double> expensesByCategory = depenseService.getDepensesByCategorieSummary();

            // Create chart data
            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
            for (Map.Entry<String, Double> entry : expensesByCategory.entrySet()) {
                pieChartData.add(new PieChart.Data(entry.getKey(), entry.getValue()));
            }

            depensesByCategorieChart.setData(pieChartData);
            depensesByCategorieChart.setTitle("Dépenses par catégorie");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateMethodePaiementChart() {
        if (methodePaiementChart == null) return;

        try {
            // Get payment methods distribution using the existing method from PaiementService
            Map<String, Double> paymentMethodDistribution = paiementService.getPaiementsByMethodePaiementSummary();

            // Create chart data
            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
            for (Map.Entry<String, Double> entry : paymentMethodDistribution.entrySet()) {
                pieChartData.add(new PieChart.Data(entry.getKey(), entry.getValue()));
            }

            methodePaiementChart.setData(pieChartData);
            methodePaiementChart.setTitle("Méthodes de paiement");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateMensuelChart() {
        if (revenusDepensesMensuelChart == null) return;

        try {
            revenusDepensesMensuelChart.getData().clear();

            // Get monthly data for current year
            int currentYear = Year.now().getValue();
            Map<Month, Double> monthlyRevenues = paiementService.getPaiementsByMonthForYear(currentYear);
            Map<Month, Double> monthlyExpenses = depenseService.getDepensesByMonthForYear(currentYear);

            // Create series
            XYChart.Series<String, Number> revenueSeries = new XYChart.Series<>();
            revenueSeries.setName("Revenus");

            XYChart.Series<String, Number> expenseSeries = new XYChart.Series<>();
            expenseSeries.setName("Dépenses");

            // Add data points
            for (Month month : Month.values()) {
                String monthName = month.getDisplayName(TextStyle.SHORT, Locale.FRANCE);
                double revenue = monthlyRevenues.getOrDefault(month, 0.0);
                double expense = monthlyExpenses.getOrDefault(month, 0.0);

                revenueSeries.getData().add(new XYChart.Data<>(monthName, revenue));
                expenseSeries.getData().add(new XYChart.Data<>(monthName, expense));
            }

            revenusDepensesMensuelChart.getData().addAll(revenueSeries, expenseSeries);
            revenusDepensesMensuelChart.setTitle("Revenus et dépenses mensuels");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message) {
        Stage stage = (Stage) mainTabPane.getScene().getWindow();
        NotificationManager.showWarning(stage, title, message);
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

}
