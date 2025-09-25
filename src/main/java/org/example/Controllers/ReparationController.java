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
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.Entities.Reparation;
import org.example.Entities.User;
import org.example.Entities.Vehicule;
import org.example.Rep.ReparationRep;
import org.example.Rep.VehiculeRep;
import org.example.Service.AuditLogService;
import org.example.Service.ReparationService;
import org.example.Service.VehiculeService;
import org.example.Utils.ConfirmationDialog;
import org.example.Utils.FileStorageService;
import org.example.Utils.NotificationManager;
import org.example.Utils.SessionManager;

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

public class ReparationController implements Initializable {

    @FXML private TableView<Reparation> reparationTable;
    @FXML private TableColumn<Reparation, Long> idColumn;
    @FXML private TableColumn<Reparation, String> descriptionColumn;
    @FXML private TableColumn<Reparation, LocalDate> dateReparationColumn;
    @FXML private TableColumn<Reparation, Double> coutColumn;
    @FXML private TableColumn<Reparation, String> prestataireColumn;

    @FXML private TextField rechercheField, descriptionField, coutField, prestataireField, factureIdField;
    @FXML private DatePicker dateReparationPicker;
    @FXML private TextArea notesField;
    @FXML private Label vehiculeInfoLabel, facturePathLabel;
    @FXML private Button ajouterBtn, modifierBtn, supprimerBtn, annulerBtn, enregistrerBtn, backBtn, exportBtn;
    @FXML private Button uploadFactureBtn, viewFactureBtn, deleteFactureBtn;
    @FXML private ComboBox<String> prestataireFilter;
    @FXML private DatePicker dateDebutFilter, dateFinFilter;
    @FXML private HBox factureActionsBox;

    private ReparationService reparationService;
    private VehiculeService vehiculeService;
    private Reparation selectedReparation;
    private AuditLogService auditLogService;
    private FileStorageService fileStorageService;
    private Long vehiculeId;
    private Vehicule vehicule;
    private String uploadedFilePath;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumns();
        reparationService = new ReparationService(new ReparationRep(), new VehiculeRep());
        vehiculeService = new VehiculeService(new VehiculeRep());
        fileStorageService = new FileStorageService();
        auditLogService = new AuditLogService();

        // Apply role-based access control
        applyRoleBasedAccess();

        setupSelectionListener();
        clearForm();
        disableForm(true);

        // Setup keyboard shortcuts
        setupKeyboardShortcuts();

        // Initialize prestataire filter with common values
        ObservableList<String> prestataires = FXCollections.observableArrayList(
                "Tous les prestataires",
                "Garage Central",
                "Auto Clim",
                "Pneu Express",
                "Garage Poids Lourds",
                "Carrosserie Express",
                "Électricité Auto"
        );
        prestataireFilter.setItems(prestataires);
        prestataireFilter.setValue("Tous les prestataires");

        // Set default date range for last 3 months
        dateDebutFilter.setValue(LocalDate.now().minusMonths(3));
        dateFinFilter.setValue(LocalDate.now());
    }

    public void setVehiculeId(Long id) {
        this.vehiculeId = id;
        try {
            Optional<Vehicule> optVehicule = vehiculeService.getVehiculeById(id);
            if (optVehicule.isPresent()) {
                vehicule = optVehicule.get();
                vehiculeInfoLabel.setText(vehicule.getMarque() + " " + vehicule.getModele() +
                        " (" + vehicule.getMatricule() + ")");
                loadReparations();
            } else {
                vehiculeInfoLabel.setText("Véhicule non trouvé");
            }
        } catch (Exception e) {
            e.printStackTrace();
            vehiculeInfoLabel.setText("Erreur lors du chargement du véhicule");
        }
    }

    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        dateReparationColumn.setCellValueFactory(new PropertyValueFactory<>("dateReparation"));
        coutColumn.setCellValueFactory(new PropertyValueFactory<>("cout"));
        prestataireColumn.setCellValueFactory(new PropertyValueFactory<>("prestataire"));

        // Format date column
        dateReparationColumn.setCellFactory(column -> new TableCell<Reparation, LocalDate>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                }
            }
        });

        // Format cost column
        coutColumn.setCellFactory(column -> new TableCell<Reparation, Double>() {
            @Override
            protected void updateItem(Double cout, boolean empty) {
                super.updateItem(cout, empty);
                if (empty || cout == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f", cout));
                }
            }
        });
    }

    private void loadReparations() {
        try {
            reparationTable.getItems().clear();
            List<Reparation> reparations = reparationService.getReparationsByVehiculeId(vehiculeId);
            reparationTable.getItems().addAll(reparations);

            if (reparationTable.getScene() != null) {
                Stage stage = (Stage) reparationTable.getScene().getWindow();
                NotificationManager.showInfo(stage, "Réparations chargées",
                        reparations.size() + " réparation(s) chargée(s)");
            }
        } catch (Exception e) {
            if (reparationTable.getScene() != null) {
                Stage stage = (Stage) reparationTable.getScene().getWindow();
                NotificationManager.showError(stage, "Erreur de chargement",
                        "Erreur lors du chargement des réparations: " + e.getMessage());
            }
            e.printStackTrace();
        }
    }

    private void populateForm(Reparation reparation) {
        if (reparation == null) return;

        descriptionField.setText(reparation.getDescription());
        dateReparationPicker.setValue(reparation.getDateReparation());
        coutField.setText(String.valueOf(reparation.getCout()));
        prestataireField.setText(reparation.getPrestataire());
        factureIdField.setText(reparation.getFactureId() != null ?
                String.valueOf(reparation.getFactureId()) : "");
        notesField.setText(reparation.getNotes());

        // Handle facture path
        String facturePath = reparation.getFacturePath();
        if (facturePath != null && !facturePath.isEmpty()) {
            facturePathLabel.setText(facturePath);
            uploadedFilePath = facturePath;
            viewFactureBtn.setDisable(false);
            deleteFactureBtn.setDisable(false);
        } else {
            facturePathLabel.setText("Aucune facture attachée");
            uploadedFilePath = null;
            viewFactureBtn.setDisable(true);
            deleteFactureBtn.setDisable(true);
        }
    }

    private void clearForm() {
        descriptionField.clear();
        dateReparationPicker.setValue(LocalDate.now());
        coutField.clear();
        prestataireField.clear();
        factureIdField.clear();
        notesField.clear();
        facturePathLabel.setText("Aucune facture attachée");
        uploadedFilePath = null;
        viewFactureBtn.setDisable(true);
        deleteFactureBtn.setDisable(true);
        selectedReparation = null;
    }

    private void disableForm(boolean disable) {
        descriptionField.setDisable(disable);
        dateReparationPicker.setDisable(disable);
        coutField.setDisable(disable);
        prestataireField.setDisable(disable);
        factureIdField.setDisable(disable);
        notesField.setDisable(disable);
        enregistrerBtn.setDisable(disable);
        annulerBtn.setDisable(disable);
        uploadFactureBtn.setDisable(disable);
        deleteFactureBtn.setDisable(disable);
    }

    private void setupSelectionListener() {
        reparationTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            selectedReparation = newSelection;
            populateForm(selectedReparation);
        });
    }


    @FXML
    private void handleRecherche() {
        String searchText = rechercheField.getText().toLowerCase();
        try {
            List<Reparation> reparations = reparationService.searchReparations(vehiculeId, searchText);
            reparationTable.getItems().clear();
            reparationTable.getItems().addAll(reparations);

            Stage stage = (Stage) rechercheField.getScene().getWindow();
            NotificationManager.showInfo(stage, "Recherche terminée",
                    reparations.size() + " résultat(s) trouvé(s)");
        } catch (Exception e) {
            Stage stage = (Stage) rechercheField.getScene().getWindow();
            NotificationManager.showError(stage, "Erreur de recherche",
                    "Erreur lors de la recherche: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleApplyFilter() {
        String prestataire = prestataireFilter.getValue();
        if (prestataire.equals("Tous les prestataires")) {
            prestataire = null;
        }

        LocalDate dateDebut = dateDebutFilter.getValue();
        LocalDate dateFin = dateFinFilter.getValue();

        try {
            List<Reparation> filteredReparations = reparationService.filterReparations(
                    vehiculeId, prestataire, dateDebut, dateFin);
            reparationTable.getItems().clear();
            reparationTable.getItems().addAll(filteredReparations);

            Stage stage = (Stage) prestataireFilter.getScene().getWindow();
            NotificationManager.showInfo(stage, "Filtres appliqués",
                    filteredReparations.size() + " résultat(s) trouvé(s)");
        } catch (Exception e) {
            Stage stage = (Stage) prestataireFilter.getScene().getWindow();
            NotificationManager.showError(stage, "Erreur de filtrage",
                    "Erreur lors du filtrage: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleResetFilter() {
        prestataireFilter.setValue("Tous les prestataires");
        dateDebutFilter.setValue(LocalDate.now().minusMonths(3));
        dateFinFilter.setValue(LocalDate.now());
        loadReparations();

        Stage stage = (Stage) prestataireFilter.getScene().getWindow();
        NotificationManager.showInfo(stage, "Filtres réinitialisés",
                "Tous les filtres ont été réinitialisés");
    }

    @FXML
    private void handleAjouter() {
        clearForm();
        disableForm(false);

        Stage stage = (Stage) ajouterBtn.getScene().getWindow();
        NotificationManager.showInfo(stage, "Nouvelle réparation",
                "Veuillez remplir le formulaire pour ajouter une nouvelle réparation");
    }

    @FXML
    private void handleModifier() {
        if (selectedReparation != null) {
            disableForm(false);

            Stage stage = (Stage) modifierBtn.getScene().getWindow();
            NotificationManager.showInfo(stage, "Modification",
                    "Modification de la réparation: " + selectedReparation.getDescription());
        } else {
            Stage stage = (Stage) modifierBtn.getScene().getWindow();
            NotificationManager.showWarning(stage, "Sélection requise",
                    "Veuillez sélectionner une réparation à modifier");
        }
    }

    @FXML
    private void handleSupprimer() {
        if (selectedReparation != null) {
            Stage stage = (Stage) supprimerBtn.getScene().getWindow();
            boolean confirmed = ConfirmationDialog.show(
                    stage,
                    "Confirmation de suppression",
                    "Supprimer la réparation",
                    "Êtes-vous sûr de vouloir supprimer cette réparation ? Cette action est irréversible.",
                    ConfirmationDialog.DialogType.DELETE
            );

            if (confirmed) {
                try {
                    // Delete the facture file if it exists
                    if (selectedReparation.getFacturePath() != null && !selectedReparation.getFacturePath().isEmpty()) {
                        fileStorageService.deleteFile(selectedReparation.getFacturePath());
                    }

                    reparationService.deleteReparation(selectedReparation);
                    reparationTable.getItems().remove(selectedReparation);
                    clearForm();

                    NotificationManager.showSuccess(stage, "Réparation supprimée",
                            "La réparation a été supprimée avec succès");

                    // Log the action
                    User currentUser = SessionManager.getCurrentUser();
                    if (currentUser != null) {
                        auditLogService.logAction(
                                "DELETE",
                                "Reparation",
                                    selectedReparation.getId(),
                                "Suppression de la réparation: " + selectedReparation.getDescription()
                        );
                    }
                } catch (Exception e) {
                    NotificationManager.showError(stage, "Erreur de suppression",
                            "Erreur lors de la suppression: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } else {
            Stage stage = (Stage) supprimerBtn.getScene().getWindow();
            NotificationManager.showWarning(stage, "Sélection requise",
                    "Veuillez sélectionner une réparation à supprimer");
        }
    }

    @FXML
    private void handleAnnuler() {
        clearForm();
        disableForm(true);

        Stage stage = (Stage) annulerBtn.getScene().getWindow();
        NotificationManager.showInfo(stage, "Opération annulée", "L'opération a été annulée");
    }

    @FXML
    public void handleBack(ActionEvent event) {
        try {
            // Get the stage
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene currentScene = ((Node) event.getSource()).getScene();

            // Preload the main view to avoid lag
            URL fxmlLocation = getClass().getResource("/IHM/Vehicule.fxml");
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
                NotificationManager.showSuccess(stage, "Export réussi",
                        "Données exportées avec succès vers " + file.getName());
            }
        } catch (Exception e) {
            Stage stage = (Stage) exportBtn.getScene().getWindow();
            NotificationManager.showError(stage, "Erreur d'exportation",
                    "Erreur lors de l'exportation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void exportToCSV(File file) throws IOException {
        try (FileWriter writer = new FileWriter(file)) {
            // Write header
            writer.write("ID,Véhicule,Description,Date réparation,Coût,Prestataire,Facture ID,Notes\n");

            // Write data
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            for (Reparation reparation : reparationTable.getItems()) {
                writer.write(String.format("%d,%s,%s,%s,%.2f,%s,%s,%s\n",
                        reparation.getId(),
                        vehicule.getMarque() + " " + vehicule.getModele() + " (" + vehicule.getMatricule() + ")",
                        reparation.getDescription().replace(",", ";"),
                        reparation.getDateReparation() != null ? reparation.getDateReparation().format(formatter) : "",
                        reparation.getCout(),
                        reparation.getPrestataire() != null ? reparation.getPrestataire().replace(",", ";") : "",
                        reparation.getFactureId() != null ? reparation.getFactureId().toString() : "",
                        reparation.getNotes() != null ? reparation.getNotes().replace(",", ";").replace("\n", " ") : ""));
            }
        }
    }

    @FXML
    private void handleUploadFacture() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Sélectionner une facture");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Documents", "*.pdf", "*.jpg", "*.jpeg", "*.png")
            );
            File selectedFile = fileChooser.showOpenDialog(null);

            if (selectedFile != null) {
                // Store the file
                uploadedFilePath = fileStorageService.storeFile(selectedFile, "reparations");

                // Update the UI
                facturePathLabel.setText(uploadedFilePath);
                viewFactureBtn.setDisable(false);
                deleteFactureBtn.setDisable(false);

                // If we're editing an existing repair, update it
                if (selectedReparation != null) {
                    // Delete old file if it exists
                    if (selectedReparation.getFacturePath() != null && !selectedReparation.getFacturePath().isEmpty()) {
                        fileStorageService.deleteFile(selectedReparation.getFacturePath());
                    }

                    selectedReparation.setFacturePath(uploadedFilePath);
                    reparationService.updateReparation(selectedReparation);

                    Stage stage = (Stage) uploadFactureBtn.getScene().getWindow();
                    NotificationManager.showSuccess(stage, "Facture ajoutée",
                            "La facture a été ajoutée avec succès");
                }
            }
        } catch (Exception e) {
            Stage stage = (Stage) uploadFactureBtn.getScene().getWindow();
            NotificationManager.showError(stage, "Erreur d'upload",
                    "Erreur lors de l'upload de la facture: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleViewFacture() {
        String facturePath = selectedReparation != null ?
                selectedReparation.getFacturePath() : uploadedFilePath;

        if (facturePath == null || facturePath.isEmpty() || facturePath.equals("Aucune facture attachée")) {
            Stage stage = (Stage) viewFactureBtn.getScene().getWindow();
            NotificationManager.showWarning(stage, "Aucune facture",
                    "Aucune facture n'est attachée à cette réparation");
            return;
        }

        try {
            // Load the document preview FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/IHM/DocumentPreview.fxml"));
            Parent root = loader.load();

            // Get the controller and set the document path
            org.example.Controllers.DocumentPreviewController controller = loader.getController();
            controller.setDocumentPath(facturePath);

            // Create a new stage for the preview
            Stage previewStage = new Stage();
            previewStage.setTitle("Aperçu de la facture");
            previewStage.initModality(Modality.APPLICATION_MODAL);
            previewStage.initOwner(viewFactureBtn.getScene().getWindow());

            // Set the scene
            Scene scene = new Scene(root);
            previewStage.setScene(scene);
            previewStage.show();
        } catch (Exception e) {
            Stage stage = (Stage) viewFactureBtn.getScene().getWindow();
            NotificationManager.showError(stage, "Erreur d'affichage",
                    "Erreur lors de l'affichage de la facture: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDeleteFacture() {
        if (selectedReparation != null && selectedReparation.getFacturePath() != null &&
                !selectedReparation.getFacturePath().isEmpty()) {

            Stage stage = (Stage) deleteFactureBtn.getScene().getWindow();
            boolean confirmed = ConfirmationDialog.show(
                    stage,
                    "Confirmation de suppression",
                    "Supprimer la facture",
                    "Êtes-vous sûr de vouloir supprimer cette facture ? Cette action est irréversible.",
                    ConfirmationDialog.DialogType.DELETE
            );

            if (confirmed) {
                try {
                    // Delete the file
                    fileStorageService.deleteFile(selectedReparation.getFacturePath());

                    // Update the entity
                    selectedReparation.setFacturePath(null);
                    reparationService.updateReparation(selectedReparation);

                    // Update the UI
                    facturePathLabel.setText("Aucune facture attachée");
                    uploadedFilePath = null;
                    viewFactureBtn.setDisable(true);
                    deleteFactureBtn.setDisable(true);

                    NotificationManager.showSuccess(stage, "Facture supprimée",
                            "La facture a été supprimée avec succès");
                } catch (Exception e) {
                    NotificationManager.showError(stage, "Erreur de suppression",
                            "Erreur lors de la suppression de la facture: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } else {
            Stage stage = (Stage) deleteFactureBtn.getScene().getWindow();
            NotificationManager.showWarning(stage, "Aucune facture",
                    "Aucune facture n'est attachée à cette réparation");
        }
    }

    @FXML
    private void handleEnregistrer() {
        if (validateForm()) {
            try {
                // Parse values
                String description = descriptionField.getText().trim();
                LocalDate dateReparation = dateReparationPicker.getValue();
                double cout = Double.parseDouble(coutField.getText().trim().replace(",", "."));
                String prestataire = prestataireField.getText().trim();
                Long factureId = null;
                if (!factureIdField.getText().trim().isEmpty()) {
                    factureId = Long.parseLong(factureIdField.getText());
                }
                String notes = notesField.getText();

                Reparation reparation;
                if (selectedReparation == null) {
                    // Create new reparation
                    reparation = new Reparation(
                            vehiculeId, factureId, description, dateReparation,
                            cout, prestataire, uploadedFilePath, notes
                    );
                } else {
                    // Update existing reparation
                    reparation = selectedReparation;
                    reparation.setDescription(description);
                    reparation.setDateReparation(dateReparation);
                    reparation.setCout(cout);
                    reparation.setPrestataire(prestataire);
                    reparation.setFactureId(factureId);
                    reparation.setNotes(notes);

                    // Only update file path if a new file was uploaded
                    if (uploadedFilePath != null) {
                        reparation.setFacturePath(uploadedFilePath);
                    }
                }

                // Create confirmation dialog
                Stage stage = (Stage) enregistrerBtn.getScene().getWindow();
                boolean confirmed = ConfirmationDialog.show(
                        stage,
                        "Confirmation d'enregistrement",
                        "Enregistrer la réparation",
                        "Êtes-vous sûr de vouloir enregistrer ces informations ?",
                        ConfirmationDialog.DialogType.SAVE
                );

                if (confirmed) {
                    reparation = reparationService.saveReparation(reparation);

                    // Refresh the table
                    loadReparations();
                    clearForm();
                    disableForm(true);

                    if (selectedReparation == null) {
                        NotificationManager.showSuccess(stage, "Réparation ajoutée",
                                "La réparation a été ajoutée avec succès");
                    } else {
                        NotificationManager.showSuccess(stage, "Réparation modifiée",
                                "La réparation a été modifiée avec succès");
                    }

                    // Log the action
                    User currentUser = SessionManager.getCurrentUser();
                    if (currentUser != null) {
                        String actionType = selectedReparation == null ? "CREATE" : "UPDATE";
                        auditLogService.logAction(
                                actionType,
                                "REPARATION",
                                reparation.getId(),
                                actionType.equals("CREATE") ?
                                        "Création de la réparation: " + reparation.getDescription() :
                                        "Modification de la réparation: " + reparation.getDescription()
                        );
                    }
                }
            } catch (NumberFormatException e) {
                Stage stage = (Stage) enregistrerBtn.getScene().getWindow();
                NotificationManager.showError(stage, "Erreur de format",
                        "Le coût doit être un nombre valide");
            } catch (Exception e) {
                Stage stage = (Stage) enregistrerBtn.getScene().getWindow();
                NotificationManager.showError(stage, "Erreur d'enregistrement",
                        "Erreur lors de l'enregistrement: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private boolean validateForm() {
        List<String> errors = new ArrayList<>();
        boolean isValid = true;

        // Validate description
        if (descriptionField.getText() == null || descriptionField.getText().trim().isEmpty()) {
            descriptionField.setStyle("-fx-border-color: #ef4444; -fx-border-width: 1px;");
            errors.add("La description est requise");
            isValid = false;
        } else {
            descriptionField.setStyle("");
        }

        // Validate date
        if (dateReparationPicker.getValue() == null) {
            dateReparationPicker.setStyle("-fx-border-color: #ef4444; -fx-border-width: 1px;");
            errors.add("La date de réparation est requise");
            isValid = false;
        } else {
            // Check that date is not in the future
            if (dateReparationPicker.getValue().isAfter(LocalDate.now())) {
                dateReparationPicker.setStyle("-fx-border-color: #ef4444; -fx-border-width: 1px;");
                errors.add("La date de réparation ne peut pas être dans le futur");
                isValid = false;
            } else {
                dateReparationPicker.setStyle("");
            }
        }

        // Validate cost
        if (coutField.getText() == null || coutField.getText().trim().isEmpty()) {
            coutField.setStyle("-fx-border-color: #ef4444; -fx-border-width: 1px;");
            errors.add("Le coût est requis");
            isValid = false;
        } else {
            try {
                double cout = Double.parseDouble(coutField.getText().trim().replace(",", "."));
                if (cout < 0) {
                    coutField.setStyle("-fx-border-color: #ef4444; -fx-border-width: 1px;");
                    errors.add("Le coût doit être un nombre positif");
                    isValid = false;
                } else {
                    coutField.setStyle("");
                }
            } catch (NumberFormatException e) {
                coutField.setStyle("-fx-border-color: #ef4444; -fx-border-width: 1px;");
                errors.add("Le coût doit être un nombre valide");
                isValid = false;
            }
        }

        // Validate prestataire
        if (prestataireField.getText() == null || prestataireField.getText().trim().isEmpty()) {
            prestataireField.setStyle("-fx-border-color: #ef4444; -fx-border-width: 1px;");
            errors.add("Le prestataire est requis");
            isValid = false;
        } else {
            prestataireField.setStyle("");
        }

        // Validate factureId if provided
        if (factureIdField.getText() != null && !factureIdField.getText().trim().isEmpty()) {
            try {
                Long.parseLong(factureIdField.getText().trim());
                factureIdField.setStyle("");
            } catch (NumberFormatException e) {
                factureIdField.setStyle("-fx-border-color: #ef4444; -fx-border-width: 1px;");
                errors.add("L'ID de facture doit être un nombre entier valide");
                isValid = false;
            }
        } else {
            factureIdField.setStyle("");
        }

        // Display all errors if any
        if (!errors.isEmpty()) {
            Stage stage = (Stage) descriptionField.getScene().getWindow();
            NotificationManager.showWarning(stage, "Validation", String.join("\n", errors));
        }

        return isValid;
    }

    private void setupKeyboardShortcuts() {
        Platform.runLater(() -> {
            if (reparationTable.getScene() != null) {
                reparationTable.getScene().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                    // Ctrl+F to focus on search field
                    if (event.isControlDown() && event.getCode() == KeyCode.F) {
                        rechercheField.requestFocus();
                        event.consume();
                    }

                    // Ctrl+N for new reparation
                    if (event.isControlDown() && event.getCode() == KeyCode.N) {
                        handleAjouter();
                        event.consume();
                    }

                    // Ctrl+E to edit selected reparation
                    if (event.isControlDown() && event.getCode() == KeyCode.E) {
                        if (selectedReparation != null) {
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

                    // Ctrl+R to refresh the reparation list
                    if (event.isControlDown() && event.getCode() == KeyCode.R) {
                        loadReparations();
                        event.consume();
                    }

                    // Alt+B to go back to vehicle management
                    if (event.isAltDown() && event.getCode() == KeyCode.B) {
                        handleBack(new ActionEvent(reparationTable, null));
                        event.consume();
                    }
                });
            }
        });
    }

    private void applyRoleBasedAccess() {
        User currentUser = SessionManager.getCurrentUser();

        if (currentUser == null) {
            // Should not happen, but if no user is logged in, disable sensitive actions
            supprimerBtn.setDisable(true);
            return;
        }

        // Secretary cannot delete repairs
        supprimerBtn.setDisable(!currentUser.canDeleteCandidats());
    }
}

