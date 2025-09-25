package org.example.Controllers;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
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
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.Entities.Candidat;
import org.example.Entities.Examen;
import org.example.Entities.User;
import org.example.Rep.CandidatRep;
import org.example.Rep.ExamenRep;
import org.example.Service.AuditLogService;
import org.example.Service.CandidatService;
import org.example.Service.ExamenService;
import org.example.Utils.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;
import javafx.stage.Modality;
import org.example.Entities.TypeSeance;
import org.example.Rep.SeanceRep;
import org.example.Service.SeanceService;

public class ExamenController implements Initializable {

    // Table components
    @FXML private TableView<Examen> examenTable;
    @FXML private TableColumn<Examen, Long> idColumn;
    @FXML private TableColumn<Examen, String> typeExamenColumn;
    @FXML private TableColumn<Examen, LocalDate> dateExamenColumn;
    @FXML private TableColumn<Examen, String> candidatNameColumn;
    @FXML private TableColumn<Examen, String> candidatCINColumn; // Added CIN column

    // Candidate information fields
    @FXML private TextField nomCandidatField, prenomCandidatField, categorieCandidatField, dateInscriptionCandidatField;
    @FXML private TextField cinCandidatField, telephoneCandidatField, statutCandidatField;

    // Form components
    @FXML private TextField lieuExamenField, rechercheField;
    @FXML private DatePicker dateExamenPicker, startDatePicker, endDatePicker;
    @FXML private ComboBox<String> typeExamenComboBox, filterTypeComboBox, filterStatusComboBox;
    @FXML private ComboBox<Candidat> candidatComboBox; // Added for CIN search


    @FXML private TextField searchField;

    @FXML private HBox typeBadgesContainer; // Container for the badges
    @FXML private Label codeBadge;
    @FXML private Label conduiteBadge;

    // Action buttons
    @FXML private Button ajouterBtn, modifierBtn, supprimerBtn, annulerBtn, enregistrerBtn, BackBtn, rechercheBtn, resetFilterBtn, applyFilterBtn, exportBtn, dashboardBtn, refreshDashboardBtn;

    // Dashboard components
    @FXML private VBox dashboardPane;

    @FXML private StackPane examTypeBadgeContainer; // Container for the badge
    @FXML private Label examTypeBadge; // Single badge that will change text and style
    @FXML private ScrollPane formScrollPane;
    @FXML private PieChart typeExamenChart;
    @FXML private BarChart<String, Number> examensParMoisChart;

    @FXML private ImageView moniteurPhoto;

    private ExamenService examenService;
    private Examen selectedExamen;
    private CandidatService candidatService;
    private boolean isDashboardVisible = false;
    private CandidatRep candidatRep = new CandidatRep();    // ADDED

    // Dashboard labels
    @FXML private Label totalExamensDashboardLabel;
    @FXML private Label examensValidesDashboardLabel;
    @FXML private Label examensEnAttenteDashboardLabel;

    // Map to store candidat objects by ID for quick lookup
    private Map<Long, Candidat> candidatMap = new HashMap<>();

    // Random color generator
    private Random random = new Random();
    private AuditLogService auditLogService;

    // In the ExamenController class, add a field for SeanceService
    private SeanceService seanceService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumns();
        examenService = new ExamenService(new ExamenRep());
        candidatService = new CandidatService(new CandidatRep());
        examenService.setCandidatRep(new CandidatRep());
        auditLogService = new AuditLogService();
        applyRoleBasedAccess();

        // Load all candidates and create a map for quick lookup
        loadCandidatMap();

        setupCandidatNameColumn();
        setupCandidatCINColumn(); // Setup the new CIN column
        setupCandidatComboBox();

        setupComboBox();
        setupFilterComboBoxes();
        loadExamens();
        setupSelectionListener();
        clearForm();
        disableForm(true);
        // Setup keyboard shortcuts
        setupKeyboardShortcuts();
        // Setup type badge with default state
        setupExamTypeBadge();

        // In the initialize method, after the other service initializations, add:
        seanceService = new SeanceService(new SeanceRep(), null, null);
    }
    /**
     * Sets up the exam type badge with default state
     */
    private void setupExamTypeBadge() {
        // Initially show a neutral badge when no exam is selected
        if (examTypeBadge != null) {
            examTypeBadge.setText("Type d'examen");
            examTypeBadge.getStyleClass().remove("exam-type-code");
            examTypeBadge.getStyleClass().remove("exam-type-conduite");
            examTypeBadge.getStyleClass().add("exam-type-neutral");
        }
    }
    /**
     * Updates the exam type badge based on the selected exam type
     */
    private void updateExamTypeBadge(String examType) {
        if (examTypeBadge == null) return;

        // Remove all type-specific classes first
        examTypeBadge.getStyleClass().remove("exam-type-code");
        examTypeBadge.getStyleClass().remove("exam-type-conduite");
        examTypeBadge.getStyleClass().remove("exam-type-neutral");

        if (examType == null) {
            // Show neutral badge when no type is selected
            examTypeBadge.setText("Type d'examen");
            examTypeBadge.getStyleClass().add("exam-type-neutral");
        } else if (examType.equals("Code")) {
            // Show Code badge
            examTypeBadge.setText("Code");
            examTypeBadge.getStyleClass().add("exam-type-code");
        } else if (examType.equals("Conduite")) {
            // Show Conduite badge
            examTypeBadge.setText("Conduite");
            examTypeBadge.getStyleClass().add("exam-type-conduite");
        }
    }

    private void loadCandidatMap() {
        try {
            List<Candidat> candidats = candidatService.getAllCandidats();
            candidatMap.clear();
            for (Candidat candidat : candidats) {
                candidatMap.put(candidat.getId(), candidat);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        typeExamenColumn.setCellValueFactory(new PropertyValueFactory<>("typeExamen"));
        dateExamenColumn.setCellValueFactory(new PropertyValueFactory<>("dateExamen"));
    }

    private void setupCandidatNameColumn() {
        candidatNameColumn.setCellValueFactory(cellData -> {
            Examen examen = cellData.getValue();
            Candidat candidat = candidatMap.get(examen.getCandidatId());
            String candidateName = candidat != null ? candidat.getNom() + " " + candidat.getPrenom() : "Inconnu";
            return new SimpleStringProperty(candidateName);
        });
    }

    // Setup the new CIN column
    private void setupCandidatCINColumn() {
        candidatCINColumn.setCellValueFactory(cellData -> {
            Examen examen = cellData.getValue();
            Candidat candidat = candidatMap.get(examen.getCandidatId());
            String cin = candidat != null ? candidat.getCin() : "N/A";
            return new SimpleStringProperty(cin);
        });
    }

    // Added for CIN search
    private void setupCandidatComboBox() {
        try {
            List<Candidat> candidats = candidatService.getAllCandidats();
            ObservableList<Candidat> candidatsList = FXCollections.observableArrayList(candidats);
            candidatComboBox.setItems(candidatsList);

            // Set the display format for the ComboBox
            candidatComboBox.setCellFactory(lv -> new ListCell<Candidat>() {
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

            // Set the display format for the selected item
            candidatComboBox.setButtonCell(new ListCell<Candidat>() {
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

            // When a candidate is selected, populate all candidate information
            candidatComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    loadCandidatInfo(newVal);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Stage stage = (Stage) (candidatComboBox != null ? candidatComboBox.getScene().getWindow() : null);
            if (stage != null) {
                NotificationManager.showError(stage, "Erreur", "Erreur lors du chargement des candidats: " + e.getMessage());
            }
        }
    }

    private void loadCandidatInfo(Candidat candidat) {
        if (candidat != null) {
            try {
                // Set hidden candidat ID for the exam
                if (selectedExamen == null) {
                    selectedExamen = new Examen();
                    selectedExamen.setDateExamen(LocalDate.now()); // Set default date
                    selectedExamen.setLieuExamen(""); // Initialize with empty string
                    selectedExamen.setFraisInscription(0.0); // Initialize with 0
                    selectedExamen.setEstValide(false); // Initialize as not validated
                }
                selectedExamen.setCandidatId(candidat.getId());

                // Fill all candidate information fields safely
                nomCandidatField.setText(candidat.getNom() != null ? candidat.getNom() : "");
                prenomCandidatField.setText(candidat.getPrenom() != null ? candidat.getPrenom() : "");
                cinCandidatField.setText(candidat.getCin() != null ? candidat.getCin() : "");
                telephoneCandidatField.setText(candidat.getTelephone() != null ? candidat.getTelephone() : "");

                // Handle categories safely
                String categories = candidat.getCategoriesPermis() != null ?
                        String.join(", ", candidat.getCategoriesPermis()) : "";
                categorieCandidatField.setText(categories);

                // Handle date safely
                String dateInscription = candidat.getDateInscription() != null ?
                        candidat.getDateInscription().toString() : "";
                dateInscriptionCandidatField.setText(dateInscription);

                statutCandidatField.setText(candidat.isActif() ? "Actif" : "Inactif");

                enableCandidatInfoFields(true);

                // Only show notification if the stage exists
            } catch (Exception e) {
                System.err.println("Error loading candidate info: " + e.getMessage());
                e.printStackTrace();

                if (candidatComboBox.getScene() != null && candidatComboBox.getScene().getWindow() != null) {
                    Stage stage = (Stage) candidatComboBox.getScene().getWindow();
                    NotificationManager.showError(stage, "Erreur", "Erreur lors du chargement des informations du candidat: " + e.getMessage());
                }
            }
        }
    }

    private void loadExamens() {
        try {
            examenTable.getItems().clear();
            List<Examen> examens = examenService.getAllExamens();
            examenTable.getItems().addAll(examens);

            if (examenTable.getScene() != null) {
                Stage stage = (Stage) examenTable.getScene().getWindow();
                NotificationManager.showInfo(stage, "Examens chargés", examens.size() + " examen(s) chargé(s)");
            }
        } catch (Exception e) {
            if (examenTable.getScene() != null) {
                Stage stage = (Stage) examenTable.getScene().getWindow();
                NotificationManager.showError(stage, "Erreur de chargement", "Erreur lors du chargement des examens: " + e.getMessage());
            }
            e.printStackTrace();
        }
    }

    private void enableCandidatInfoFields(boolean enable) {
        nomCandidatField.setDisable(!enable);
        prenomCandidatField.setDisable(!enable);
        categorieCandidatField.setDisable(!enable);
        dateInscriptionCandidatField.setDisable(!enable);
        cinCandidatField.setDisable(!enable);
        telephoneCandidatField.setDisable(!enable);
        statutCandidatField.setDisable(!enable);
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

        // Secretary cannot delete candidates
        supprimerBtn.setDisable(!currentUser.canDeleteCandidats());
    }
    private void clearCandidatInfoFields() {
        nomCandidatField.clear();
        prenomCandidatField.clear();
        categorieCandidatField.clear();
        dateInscriptionCandidatField.clear();
        cinCandidatField.clear();
        telephoneCandidatField.clear();
        statutCandidatField.clear();
    }

    private void populateForm(Examen examen) {
        if (examen == null) return;

        typeExamenComboBox.setValue(examen.getTypeExamen());
        dateExamenPicker.setValue(examen.getDateExamen());
        lieuExamenField.setText(examen.getLieuExamen());
        // Update type badges based on the exam type
        updateExamTypeBadge(examen.getTypeExamen());

        // Find and select the corresponding candidate in the combo box
        Candidat candidat = candidatMap.get(examen.getCandidatId());
        if (candidat != null) {
            // First clear the selection to avoid triggering the listener
            candidatComboBox.getSelectionModel().clearSelection();

            // Then select the candidate
            candidatComboBox.getSelectionModel().select(candidat);

            // Manually load candidate info to ensure it's displayed correctly
            loadCandidatInfo(candidat);
        } else {
            candidatComboBox.getSelectionModel().clearSelection();
            clearCandidatInfoFields();
        }
    }

    private void clearForm() {
        typeExamenComboBox.setValue(null);
        dateExamenPicker.setValue(null);
        lieuExamenField.clear();
        candidatComboBox.setValue(null);
        clearCandidatInfoFields();
        selectedExamen = null;
    }

    private void disableForm(boolean disable) {
        typeExamenComboBox.setDisable(disable);
        dateExamenPicker.setDisable(disable);
        lieuExamenField.setDisable(disable);
        candidatComboBox.setDisable(disable);
        enregistrerBtn.setDisable(disable);
        annulerBtn.setDisable(disable);
    }

    private void setupSelectionListener() {
        examenTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            selectedExamen = newSelection;
            populateForm(selectedExamen);
        });
    }

    private void setupComboBox() {
        typeExamenComboBox.getItems().addAll("Code", "Conduite");
    }

    private void setupFilterComboBoxes() {
        // Setup type filter
        filterTypeComboBox.getItems().add("Tous les types");
        filterTypeComboBox.getItems().addAll("Code", "Conduite");
        filterTypeComboBox.setValue("Tous les types");

        // Setup status filter
        filterStatusComboBox.getItems().add("Tous les statuts");
        filterStatusComboBox.getItems().addAll("Validé", "Non validé");
        filterStatusComboBox.setValue("Tous les statuts");
    }

    // Event Handlers
    @FXML private void handleAjouter() {
        clearForm();
        disableForm(false);

        Stage stage = (Stage) ajouterBtn.getScene().getWindow();
        NotificationManager.showInfo(stage, "Nouvel examen", "Veuillez remplir le formulaire pour ajouter un nouvel examen");
    }

    @FXML private void handleModifier() {
        if (examenTable.getSelectionModel().getSelectedItem() != null) {
            disableForm(false);

            Stage stage = (Stage) modifierBtn.getScene().getWindow();
            NotificationManager.showInfo(stage, "Modification", "Modification de l'examen en cours");
        } else {
            Stage stage = (Stage) modifierBtn.getScene().getWindow();
            NotificationManager.showWarning(stage, "Sélection requise", "Veuillez sélectionner un examen à modifier");
        }
    }

    @FXML
    private void handleSupprimer() {
        if (selectedExamen != null) {
            // Check if user has permission to delete
            User currentUser = SessionManager.getCurrentUser();
            if (currentUser == null || !currentUser.canDeleteCandidats()) {
                AccessControl.showAccessDeniedAlert(
                        "Vous n'avez pas la permission de supprimer des candidats. Cette action est réservée aux administrateurs.");
                return;
            }
            Stage stage = (Stage) supprimerBtn.getScene().getWindow();
            boolean confirmed = ConfirmationDialog.show(
                    stage,
                    "Confirmation de suppression",
                    "Supprimer l'examen",
                    "Êtes-vous sûr de vouloir supprimer cet examen ? Cette action est irréversible.",
                    ConfirmationDialog.DialogType.DELETE
            );

            if (confirmed) {
                try {
                    // Log the delete action
                    auditLogService.logAction(
                            "DELETE",
                            "EXAMEN",
                            selectedExamen.getId(),
                            "Suppression d'examen de " + selectedExamen.getTypeExamen() + " d'ID :  " + selectedExamen.getId()
                    );
                    examenService.deleteExamen(selectedExamen);
                    examenTable.getItems().remove(selectedExamen);
                    clearForm();

                    NotificationManager.showSuccess(stage, "Examen supprimé", "L'examen a été supprimé avec succès");
                } catch (Exception e) {
                    NotificationManager.showError(stage, "Erreur de suppression", "Erreur lors de la suppression: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } else {
            Stage stage = (Stage) supprimerBtn.getScene().getWindow();
            NotificationManager.showWarning(stage, "Sélection requise", "Veuillez sélectionner un examen à supprimer");
        }
    }

    @FXML private void handleAnnuler() {
        clearForm();
        disableForm(true);

        Stage stage = (Stage) annulerBtn.getScene().getWindow();
        NotificationManager.showInfo(stage, "Opération annulée", "L'opération a été annulée");
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
    private void handleEnregistrer() {
        if (validateForm()) {
            try {
                Candidat selectedCandidat = candidatComboBox.getSelectionModel().getSelectedItem();
                if (selectedCandidat == null) {
                    Stage stage = (Stage) enregistrerBtn.getScene().getWindow();
                    NotificationManager.showWarning(stage, "Candidat requis", "Veuillez sélectionner un candidat");
                    return;
                }

                Long candidatId = selectedCandidat.getId();

                String typeExamen = typeExamenComboBox.getValue();
                LocalDate dateExamen = dateExamenPicker.getValue();

                // In the handleEnregistrer() method, replace the existing validation for "Conduite" exam with this updated logic:
                // Check if we're trying to add a "Conduite" exam, and if so, check for a prior "Code" exam and conduite sessions
                if ("Conduite".equals(typeExamen)) {
                    if (!hasCodeExamen(candidatId, dateExamen)) {
                        Stage stage = (Stage) enregistrerBtn.getScene().getWindow();
                        NotificationManager.showWarning(stage, "Examen impossible", "Le candidat doit passer un examen de code avant de passer un examen de conduite.");
                        return; // Prevent saving the exam
                    }

                    // Check if candidate has at least one conduite session
                    int conduiteSessionsCount = seanceService.countSeancesByTypeAndCandidat(TypeSeance.Conduite, candidatId);
                    if (conduiteSessionsCount < 1) {
                        Stage stage = (Stage) enregistrerBtn.getScene().getWindow();
                        NotificationManager.showWarning(stage, "Examen impossible", "Le candidat doit avoir au moins une séance de conduite avant de passer l'examen de conduite.");
                        return; // Prevent saving the exam
                    }
                } else if ("Code".equals(typeExamen)) {
                    // Check if candidate has at least one code session
                    int codeSessionsCount = seanceService.countSeancesByTypeAndCandidat(TypeSeance.Code, candidatId);
                    if (codeSessionsCount < 1) {
                        Stage stage = (Stage) enregistrerBtn.getScene().getWindow();
                        NotificationManager.showWarning(stage, "Examen impossible", "Le candidat doit avoir au moins une séance de code avant de passer l'examen de code.");
                        return; // Prevent saving the exam
                    }
                }

                Examen examen;
                if (selectedExamen == null || selectedExamen.getId() == null) {
                    // Create new examen
                    examen = new Examen(
                            null, // ID
                            candidatId,
                            typeExamenComboBox.getValue(),
                            dateExamenPicker.getValue(),
                            lieuExamenField.getText(),0,true
                    );
                } else {
                    // Update existing examen
                    examen = selectedExamen;
                    examen.setCandidatId(candidatId);
                    examen.setTypeExamen(typeExamenComboBox.getValue());
                    examen.setDateExamen(dateExamenPicker.getValue());
                    examen.setLieuExamen(lieuExamenField.getText());
                }

                Stage stage = (Stage) enregistrerBtn.getScene().getWindow();
                boolean confirmed = ConfirmationDialog.show(
                        stage,
                        "Confirmation d'enregistrement",
                        "Enregistrer l'examen",
                        "Êtes-vous sûr de vouloir enregistrer ces informations ?",
                        ConfirmationDialog.DialogType.SAVE
                );

                if (confirmed) {
                    examen = examenService.saveExamen(examen);
                    // Log the action
                    String action = (selectedCandidat == null) ? "CREATE" : "UPDATE";
                    auditLogService.logAction(
                            action,
                            "EXAMEN",
                            examen.getId(),
                            action + " examen de " + examen.getTypeExamen() + " d'ID : " + examen.getId() + " de " + selectedCandidat.getNom()+" " + selectedCandidat.getPrenom());
                    loadExamens();
                    disableForm(true);

                    if (selectedExamen == null || selectedExamen.getId() == null) {
                        NotificationManager.showSuccess(stage, "Examen ajouté", "Le nouvel examen a été ajouté avec succès");
                    } else {
                        NotificationManager.showSuccess(stage, "Examen modifié", "L'examen a été modifié avec succès");
                    }
                }
            } catch (Exception e) {
                Stage stage = (Stage) enregistrerBtn.getScene().getWindow();
                NotificationManager.showError(stage, "Erreur d'enregistrement", "Erreur lors de l'enregistrement: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private boolean hasCodeExamen(Long candidatId, LocalDate dateExamen ) {
        List<Examen> examens = examenService.getAllExamens();
        for (Examen examen : examens) {
            if (examen.getCandidatId().equals(candidatId) && examen.isEstValide() && "Code".equals(examen.getTypeExamen()) &&dateExamen.isAfter(examen.getDateExamen())) {
                return true;
            }
        }
        return false;
    }
    private void setupKeyboardShortcuts() {
        Platform.runLater(() -> {
            if (examenTable.getScene() != null) {
                examenTable.getScene().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                    // Ctrl+F to focus on search field
                    if (event.isControlDown() && event.getCode() == KeyCode.F) {
                        searchField.requestFocus();
                        event.consume();
                    }

                    // F1 for help
                    if (event.getCode() == KeyCode.F1) {
                        handleHelp(new ActionEvent(examenTable, null));
                        event.consume();
                    }
                    // F2 for settings
                    if (event.getCode() == KeyCode.F2) {
                        handleSettings(new ActionEvent(examenTable, null));
                        event.consume();
                    }


                    // Ctrl+N for new examen
                    if (event.isControlDown() && event.getCode() == KeyCode.N) {
                        handleAjouter();
                        event.consume();
                    }

                    // Ctrl+E to edit selected examen
                    if (event.isControlDown() && event.getCode() == KeyCode.E) {
                        if (selectedExamen != null) {
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

                    // Ctrl+R to refresh the examen list
                    if (event.isControlDown() && event.getCode() == KeyCode.R) {
                        loadExamens();
                        event.consume();
                    }

                    // Alt+B to go back to main menu
                    if (event.isAltDown() && event.getCode() == KeyCode.B) {
                        handleBack(new ActionEvent(examenTable, null));
                        event.consume();
                    }
                });
            }
        });
    }

    @FXML
    private void handleRecherche() {
        String searchText = rechercheField.getText().toLowerCase();
        try {
            // Modified to search by CIN or name
            List<Examen> examens;
            if (searchText.isEmpty()) {
                examens = examenService.getAllExamens();
            } else {
                examens = examenService.searchExamensByCandidatCinOrName(searchText);
            }

            examenTable.getItems().clear();
            examenTable.getItems().addAll(examens);

            Stage stage = (Stage) rechercheBtn.getScene().getWindow();
            NotificationManager.showInfo(stage, "Recherche terminée", examens.size() + " résultat(s) trouvé(s)");
        } catch (Exception e) {
            Stage stage = (Stage) rechercheBtn.getScene().getWindow();
            NotificationManager.showError(stage, "Erreur de recherche", "Erreur lors de la recherche: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleApplyFilter() {
        try {
            String type = filterTypeComboBox.getValue();
            LocalDate startDate = startDatePicker.getValue();
            LocalDate endDate = endDatePicker.getValue();
            String status = filterStatusComboBox.getValue();

            List<Examen> filteredExamens = examenService.filterExamens(type, startDate, endDate, status);

            examenTable.getItems().clear();
            examenTable.getItems().addAll(filteredExamens);

            Stage stage = (Stage) applyFilterBtn.getScene().getWindow();
            NotificationManager.showInfo(stage, "Filtres appliqués", filteredExamens.size() + " résultat(s) trouvé(s)");
        } catch (Exception e) {
            Stage stage = (Stage) applyFilterBtn.getScene().getWindow();
            NotificationManager.showError(stage, "Erreur de filtrage", "Erreur lors de l'application des filtres: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleResetFilter() {
        filterTypeComboBox.setValue("Tous les types");
        startDatePicker.setValue(null);
        endDatePicker.setValue(null);
        filterStatusComboBox.setValue("Tous les statuts");

        loadExamens();

        Stage stage = (Stage) resetFilterBtn.getScene().getWindow();
        NotificationManager.showInfo(stage, "Filtres réinitialisés", "Tous les filtres ont été réinitialisés");
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
            writer.write("ID,Candidat ID,Type d'examen,Date d'examen,Lieu d'examen,Frais d'inscription,Validé\n");

            // Write data
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            for (Examen examen : examenTable.getItems()) {
                writer.write(String.format("%d,%d,%s,%s,%s,%.2f,%b\n",
                        examen.getId(),
                        examen.getCandidatId(),
                        examen.getTypeExamen(),
                        examen.getDateExamen().format(formatter),
                        examen.getLieuExamen(),
                        examen.getFraisInscription(),
                        examen.isEstValide()));
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

    // Generate a random color in CSS format
    private String getRandomColor() {
        int r = random.nextInt(200); // Limit to 200 to avoid very light colors
        int g = random.nextInt(200);
        int b = random.nextInt(200);
        return String.format("rgb(%d, %d, %d)", r, g, b);
    }

    @FXML
    private void refreshDashboard() {
        try {
            List<Examen> examens = examenService.getAllExamens();

            // Update statistics
            int total = examens.size();
            long valides = examens.stream().filter(Examen::isEstValide).count();
            long enAttente = total - valides;

            // Update the main dashboard labels
            totalExamensDashboardLabel.setText(String.valueOf(total));
            examensValidesDashboardLabel.setText(String.valueOf(valides));
            examensEnAttenteDashboardLabel.setText(String.valueOf(enAttente));

            // Update pie chart with random colors
            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
            Map<String, Long> typeCount = examens.stream()
                    .collect(Collectors.groupingBy(Examen::getTypeExamen, Collectors.counting()));

            typeCount.forEach((type, count) -> {
                PieChart.Data data = new PieChart.Data(type + " (" + count + ")", count);
                pieChartData.add(data);
            });

            typeExamenChart.setData(pieChartData);

            // Apply random colors to pie chart slices
            int colorIndex = 0;
            for (PieChart.Data data : pieChartData) {
                String color = getRandomColor();
                data.getNode().setStyle("-fx-pie-color: " + color + ";");
                colorIndex++;
            }

            // Update bar chart with random colors
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Nombre d'examens");

            Map<Month, Long> monthCount = examens.stream()
                    .collect(Collectors.groupingBy(e -> e.getDateExamen().getMonth(), Collectors.counting()));

            // Sort by month
            Map<Month, Long> sortedMonthCount = new TreeMap<>();
            sortedMonthCount.putAll(monthCount);

            sortedMonthCount.forEach((month, count) ->
                    series.getData().add(new XYChart.Data<>(
                            month.getDisplayName(TextStyle.SHORT, Locale.FRENCH), count))
            );

            examensParMoisChart.getData().clear();
            examensParMoisChart.getData().add(series);

            // Apply random colors to bar chart bars
            for (XYChart.Data<String, Number> data : series.getData()) {
                String color = getRandomColor();
                data.getNode().setStyle("-fx-bar-fill: " + color + ";");
            }

            Stage stage = (Stage) refreshDashboardBtn.getScene().getWindow();
            NotificationManager.showSuccess(stage, "Tableau de bord actualisé", "Les statistiques ont été mises à jour avec des couleurs aléatoires");
        } catch (Exception e) {
            Stage stage = (Stage) refreshDashboardBtn.getScene().getWindow();
            NotificationManager.showError(stage, "Erreur d'actualisation", "Erreur lors de l'actualisation du tableau de bord: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean validateForm() {
        boolean isValid = true;

        // Create a temporary error label if statusLabel is being used for other purposes
        Label tempErrorLabel = new Label();

        // Validate candidate selection
        if (candidatComboBox.getSelectionModel().getSelectedItem() == null) {
            Stage stage = (Stage) candidatComboBox.getScene().getWindow();
            NotificationManager.showWarning(stage, "Validation", "Veuillez sélectionner un candidat");
            isValid = false;
        }

        // Validate type examen selection
        if (typeExamenComboBox.getValue() == null) {
            typeExamenComboBox.setStyle("-fx-border-color: #ef4444; -fx-border-width: 1px;");

            Stage stage = (Stage) typeExamenComboBox.getScene().getWindow();
            NotificationManager.showWarning(stage, "Validation", "Veuillez sélectionner un type d'examen");
            isValid = false;
        } else {
            typeExamenComboBox.setStyle("");
        }


        // Validate lieu examen
        if (!Verification.validateRequired(lieuExamenField, tempErrorLabel)) {
            Stage stage = (Stage) lieuExamenField.getScene().getWindow();
            NotificationManager.showWarning(stage, "Validation", "Veuillez entrer un lieu d'examen");
            isValid = false;
        }


        return isValid;
    }
}
