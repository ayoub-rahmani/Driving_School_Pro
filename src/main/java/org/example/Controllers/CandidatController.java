package org.example.Controllers;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.*;
import javafx.scene.effect.Light;
import javafx.scene.effect.Lighting;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import org.example.Entities.User;
import org.example.Service.AuditLogService;
import org.example.Utils.AccessControl;
import org.example.Utils.NotificationManager;
import org.example.Utils.SessionManager;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import org.example.Entities.Candidat;
import org.example.Service.CandidatService;
import org.example.Utils.ConfirmationDialog;

import java.awt.Desktop;
import java.io.FileWriter;
import java.io.IOException;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;


import javafx.stage.FileChooser;

import org.example.Rep.CandidatRep;

import org.example.Utils.Verification;

import java.io.File;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;
import javafx.stage.Modality;
import javafx.scene.layout.StackPane;
import javafx.scene.effect.DropShadow;
import javafx.util.Duration;
import javafx.scene.transform.Rotate;
import javafx.scene.effect.GaussianBlur;

public class CandidatController implements Initializable {

    @FXML private ComboBox<String> documentFormatComboBox;

    private CandidatService candidatService;

    // Table components
    @FXML private TableView<Candidat> candidatTable;
    @FXML private Button BackBtn;
    @FXML private TableColumn<Candidat, Long> idColumn;
    @FXML private TableColumn<Candidat, String> nomColumn;
    @FXML private TableColumn<Candidat, String> prenomColumn;
    @FXML private TableColumn<Candidat, String> cinColumn; // Added for CIN display

    @FXML private Button generateFicheBtn;
    @FXML private Button generateListBtn;
    @FXML private Button generateCategoryListBtn;
    @FXML private Button generateSearchResultsBtn;
    @FXML private ComboBox<String> categoryComboBox;

    // Search components
    @FXML private TextField rechercheField;
    @FXML private Button rechercheBtn;

    // Form components
    @FXML
    private TextField nomField;

    @FXML
    private TextField prenomField;
    @FXML private TextField cinField;
    @FXML private DatePicker dateNaissancePicker;

    @FXML
    private TextField emailField;

    @FXML
    private TextField telephoneField;
    @FXML private TextField adresseField;
    @FXML private DatePicker dateInscriptionPicker;
    @FXML private CheckBox categorieA;
    @FXML private CheckBox categorieB;
    @FXML private CheckBox categorieC;
    @FXML private RadioButton actifOui;
    @FXML private RadioButton actifNon;
    @FXML private TextField fraisInscriptionField;
    @FXML private TextField notesField;

    // PDF components
    @FXML private Button selectCINBtn;
    @FXML private Button selectPhotoBtn;
    @FXML private Button selectCertificatBtn;
    @FXML private Button viewCINBtn;
    @FXML private Button viewPhotoBtn;
    @FXML private Button viewCertificatBtn;
    @FXML private Label cinFileLabel;
    @FXML private Label photoFileLabel;
    @FXML private Label certificatFileLabel;
    @FXML private ImageView documentPreview;

    //FXML for Documents
    @FXML private Label cinPathLabel;
    @FXML private Label photoPathLabel;
    @FXML private Label certificatPathLabel;
    @FXML private Label fichePdfPathLabel;





    // Action buttons
    @FXML
    private Button ajouterBtn;
    @FXML private Button modifierBtn;
    @FXML private Button supprimerBtn;
    @FXML private Button annulerBtn;
    @FXML private Button enregistrerBtn;
    @FXML private Button exportBtn;
    @FXML private Button dashboardBtn;
    @FXML private Button refreshDashboardBtn;
    @FXML private Button applyFilterBtn;
    @FXML private Button resetFilterBtn;

    // Filter components
    @FXML private ComboBox<String> filterStatusComboBox;
    @FXML private ComboBox<String> filterCategoryComboBox;
    @FXML private DatePicker filterDatePicker;

    // Dashboard components
    @FXML private VBox dashboardPane;
    @FXML private ScrollPane formScrollPane;
    @FXML private Label totalCandidatsLabel;
    @FXML private Label candidatsActifsLabel;
    @FXML private Label candidatsInactifsLabel;
    @FXML private PieChart categoriePermisChart;
    @FXML private BarChart<String, Number> inscriptionsParMoisChart;

    // Status label
    @FXML private Label candidatNameLabel;
    @FXML private Label statusBadge;
    @FXML private Label categoryBadge;
    @FXML private Label dateBadge;
    @FXML private ImageView candidatPhoto;

    // Toggle group
    @FXML private ToggleGroup actifGroup;

    // Service for business logic
    private Candidat selectedCandidat;

    // File paths for documents
    private String tempCheminPhotoCIN;
    private String tempCheminPhotoIdentite;
    private String tempCheminCertificatMedical;

    // Base directory for document storage
    private final String DOCUMENTS_DIR = "documents/candidats/";

    // Dashboard state
    private boolean isDashboardVisible = false;
    private AuditLogService auditLogService;



    @Override
    public void initialize(URL location, ResourceBundle resources) {

        Platform.runLater(this::initializeKeyboardShortcuts);

        // Initialize document format combo box
        if(documentFormatComboBox!=null){
            documentFormatComboBox.setValue("PDF");
        }

        // Initialize audit log service
        auditLogService = new AuditLogService();

        // Apply role-based access control
        applyRoleBasedAccess();

        // Initialize the table columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nomColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        prenomColumn.setCellValueFactory(new PropertyValueFactory<>("prenom"));

        // Add CIN column if it exists
        if (cinColumn != null) {
            cinColumn.setCellValueFactory(new PropertyValueFactory<>("cin"));
        }
        if (candidatPhoto != null) {
            candidatPhoto.setFitWidth(120.0);
            candidatPhoto.setFitHeight(120.0);
            candidatPhoto.setPreserveRatio(true);
            candidatPhoto.getStyleClass().add("fixed-size-image");
        }

        // Initialize service with repository
        CandidatRep candidatRep = new CandidatRep();
        candidatService = new CandidatService(candidatRep);

        // Load all candidats from database
        loadCandidats();

        // Set up table selection listener
        candidatTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedCandidat = newSelection;
                populateForm(selectedCandidat);
            }
        });

        // Initialize document directory
        initDocumentDirectory();

        // Setup filter components
        setupFilterComponents();

        // Initial UI state
        clearForm();
        disableForm(true);
        setupDocumentButtons();


        // Set up radio button listeners to update status badge in real-time
        actifOui.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                updateStatusBadge(true);
            }
        });

        actifNon.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                updateStatusBadge(false);
            }
        });
    }

    // Helper method to update the status badge
    private void updateStatusBadge(boolean isActive) {
        if (isActive) {
            // Set badge text to "Actif"
            statusBadge.setText("Actif");

            // Remove any existing style classes
            statusBadge.getStyleClass().removeAll("badge", "payment-complete", "payment-pending", "payment-overdue");

            // Add the success style class
            statusBadge.getStyleClass().add("badge");
        } else {
            // Set badge text to "Inactif"
            statusBadge.setText("Inactif");

            // Remove any existing style classes
            statusBadge.getStyleClass().removeAll("badge", "payment-complete", "payment-pending", "payment-overdue");

            // Add the danger style class
            statusBadge.getStyleClass().add("payment-overdue");
        }
    }
    private void updateCandidatPhoto(String photoPath) {
        if (candidatPhoto == null) return;

        try {
            // Save the current dimensions before changing the image
            double currentFitWidth = candidatPhoto.getFitWidth();
            double currentFitHeight = candidatPhoto.getFitHeight();

            Image image;
            if (photoPath != null && !photoPath.isEmpty() && new File(photoPath).exists()) {
                // Load the candidate's photo if it exists
                image = new Image(new File(photoPath).toURI().toString());
            } else {
                // Load the default photo if no candidate photo exists
                image = new Image(getClass().getResourceAsStream("/images/prof.png"));
            }

            candidatPhoto.setImage(image);

            // Ensure the ImageView maintains its size
            candidatPhoto.setFitWidth(currentFitWidth > 0 ? currentFitWidth : 40.0);
            candidatPhoto.setFitHeight(currentFitHeight > 0 ? currentFitHeight : 40.0);
            candidatPhoto.setPreserveRatio(false);

            // Apply CSS to maintain container size
            candidatPhoto.getStyleClass().add("/Styles/fixed-size-image");
        } catch (Exception e) {
            System.err.println("Error loading candidate photo: " + e.getMessage());
            // Fallback to default image if there's an error
            try {
                Image defaultImage = new Image(getClass().getResourceAsStream("/images/prof.png"));
                candidatPhoto.setImage(defaultImage);
            } catch (Exception ex) {
                // If even the default image fails, just log the error
                System.err.println("Error loading default candidate image: " + ex.getMessage());
            }
        }
    }

    private void initDocumentDirectory() {
        try {
            Path dirPath = Paths.get(DOCUMENTS_DIR);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }
        } catch (Exception e) {
            Stage stage = (Stage) candidatTable.getScene().getWindow();
            NotificationManager.showError(stage, "Erreur de répertoire", "Erreur lors de la création du répertoire documents: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadCandidats() {
        try {
            candidatTable.getItems().clear();
            List<Candidat> candidats = candidatService.getAllCandidats();
            candidatTable.getItems().addAll(candidats);

            if (candidatTable.getScene() != null) {
                Stage stage = (Stage) candidatTable.getScene().getWindow();
                NotificationManager.showInfo(stage, "Candidats chargés", candidats.size() + " candidat(s) chargé(s)");
            }
        } catch (Exception e) {
            if (candidatTable.getScene() != null) {
                Stage stage = (Stage) candidatTable.getScene().getWindow();
                NotificationManager.showError(stage, "Erreur de chargement", "Erreur lors du chargement des candidats: " + e.getMessage());
            }
            e.printStackTrace();
        }
    }

    private void populateForm(Candidat candidat) {
        nomField.setText(candidat.getNom());
        prenomField.setText(candidat.getPrenom());
        cinField.setText(candidat.getCin());
        dateNaissancePicker.setValue(candidat.getDateNaissance());
        telephoneField.setText(candidat.getTelephone());
        adresseField.setText(candidat.getAdresse());
        emailField.setText(candidat.getEmail());
        dateInscriptionPicker.setValue(candidat.getDateInscription());

        // Set categories
        categorieA.setSelected(candidat.getCategoriesPermis().contains("A"));
        categorieB.setSelected(candidat.getCategoriesPermis().contains("B"));
        categorieC.setSelected(candidat.getCategoriesPermis().contains("C"));

        // Set active status
        if (candidat.isActif()) {
            actifOui.setSelected(true);
        } else {
            actifNon.setSelected(true);
        }

        // PDF paths
        tempCheminPhotoCIN = candidat.getCheminPhotoCIN();
        tempCheminPhotoIdentite = candidat.getCheminPhotoIdentite();
        tempCheminCertificatMedical = candidat.getCheminCertificatMedical();

        // Update document labels
        updateDocumentLabels();
        updateCandidatPhoto(candidat.getCheminPhotoIdentite());

        // Set the label
        cinPathLabel.setText(candidat.getCheminPhotoCIN());
        photoPathLabel.setText(candidat.getCheminPhotoIdentite());
        certificatPathLabel.setText(candidat.getCheminCertificatMedical());
        fichePdfPathLabel.setText(candidat.getCheminFichePdf() != null ? candidat.getCheminFichePdf() : "Aucun fichier");

        // Update candidate info header
        candidatNameLabel.setText(candidat.getNom() + " " + candidat.getPrenom());

        // Update status badge based on candidate's active status
        updateStatusBadge(candidat.isActif());

        categoryBadge.setText("Type " + String.join(", ", candidat.getCategoriesPermis()));
        dateBadge.setText("Inscrit le " + candidat.getDateInscription().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
    }

    private void updateDocumentLabels() {
        cinFileLabel.setText(tempCheminPhotoCIN != null ? new File(tempCheminPhotoCIN).getName() : "Aucun fichier");
        photoFileLabel.setText(tempCheminPhotoIdentite != null ? new File(tempCheminPhotoIdentite).getName() : "Aucun fichier");
        certificatFileLabel.setText(tempCheminCertificatMedical != null ? new File(tempCheminCertificatMedical).getName() : "Aucun fichier");

        // Enable/disable view buttons based on document availability
        viewCINBtn.setDisable(tempCheminPhotoCIN == null);
        viewPhotoBtn.setDisable(tempCheminPhotoIdentite == null);
        viewCertificatBtn.setDisable(tempCheminCertificatMedical == null);
    }

    private void clearForm() {
        nomField.clear();
        prenomField.clear();
        cinField.clear();
        dateNaissancePicker.setValue(null);
        telephoneField.clear();
        adresseField.clear();
        emailField.clear();
        dateInscriptionPicker.setValue(LocalDate.now());
        categorieA.setSelected(false);
        categorieB.setSelected(false);
        categorieC.setSelected(false);
        actifOui.setSelected(true);
        if (fraisInscriptionField != null) {
            fraisInscriptionField.clear();
        }
        if (notesField != null) {
            notesField.clear();
        }

        // Reset document paths
        tempCheminPhotoCIN = null;
        tempCheminPhotoIdentite = null;
        tempCheminCertificatMedical = null;
        updateDocumentLabels();

        // Clear preview
        documentPreview.setImage(null);
        updateCandidatPhoto(null);

        selectedCandidat = null;

        // Reset candidate info header
        candidatNameLabel.setText("Nouveau Candidat");

        // Reset status badge to default (Nouveau with badge style)
        statusBadge.setText("Nouveau");
        statusBadge.getStyleClass().removeAll("payment-complete", "payment-pending", "payment-overdue");
        statusBadge.getStyleClass().add("badge");

        categoryBadge.setText("Type -");
        dateBadge.setText("Inscrit le " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
    }

    private void disableForm(boolean disable) {
        nomField.setDisable(disable);
        prenomField.setDisable(disable);
        cinField.setDisable(disable);
        dateNaissancePicker.setDisable(disable);
        telephoneField.setDisable(disable);
        adresseField.setDisable(disable);
        emailField.setDisable(disable);
        dateInscriptionPicker.setDisable(disable);
        categorieA.setDisable(disable);
        categorieB.setDisable(disable);
        categorieC.setDisable(disable);
        actifOui.setDisable(disable);
        actifNon.setDisable(disable);
        if (fraisInscriptionField != null) {
            fraisInscriptionField.setDisable(disable);
        }
        if (notesField != null) {
            notesField.setDisable(disable);
        }

        // PDF buttons
        selectCINBtn.setDisable(disable);
        selectPhotoBtn.setDisable(disable);
        selectCertificatBtn.setDisable(disable);
        viewCINBtn.setDisable(disable || tempCheminPhotoCIN == null);
        viewPhotoBtn.setDisable(disable || tempCheminPhotoIdentite == null);
        viewCertificatBtn.setDisable(disable || tempCheminCertificatMedical == null);

        enregistrerBtn.setDisable(disable);
        annulerBtn.setDisable(disable);
    }

    private void setupFilterComponents() {
        // Setup status filter
        filterStatusComboBox.getItems().add("Tous les statuts");
        filterStatusComboBox.getItems().addAll("Actif", "Inactif");
        filterStatusComboBox.setValue("Tous les statuts");

        // Setup category filter
        filterCategoryComboBox.getItems().add("Toutes les catégories");
        filterCategoryComboBox.getItems().addAll("A", "B", "C");
        filterCategoryComboBox.setValue("Toutes les catégories");
    }

    // Event handlers
    @FXML
    private void handleRecherche() {
        String searchText = rechercheField.getText().toLowerCase();
        try {
            List<Candidat> candidats = candidatService.searchCandidats(searchText);
            candidatTable.getItems().clear();
            candidatTable.getItems().addAll(candidats);
            //statusLabel.setText("Recherche terminée: " + candidats.size() + " résultat(s)");

            Stage stage = (Stage) rechercheField.getScene().getWindow();
            NotificationManager.showInfo(stage, "Recherche terminée", candidats.size() + " résultat(s) trouvé(s)");

            // If there's only one result, select it automatically
            if (candidats.size() == 1) {
                candidatTable.getSelectionModel().select(0);
                selectedCandidat = candidats.get(0);
                populateForm(selectedCandidat);
            }
        } catch (Exception e) {
            //statusLabel.setText("Erreur lors de la recherche: " + e.getMessage());

            Stage stage = (Stage) rechercheField.getScene().getWindow();
            NotificationManager.showError(stage, "Erreur de recherche", "Erreur lors de la recherche: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleApplyFilter() {
        try {
            String status = filterStatusComboBox.getValue();
            String category = filterCategoryComboBox.getValue();


            List<Candidat> allCandidats = candidatService.getAllCandidats();
            List<Candidat> filteredCandidats = allCandidats.stream()
                    .filter(c -> {
                        // Filter by status
                        if (!"Tous les statuts".equals(status)) {
                            boolean isActif = "Actif".equals(status);
                            if (c.isActif() != isActif) return false;
                        }

                        // Filter by category
                        if (!"Toutes les catégories".equals(category)) {
                            if (!c.getCategoriesPermis().contains(category)) return false;
                        }

                        // Filter by date

                        return true;
                    })
                    .collect(Collectors.toList());

            candidatTable.getItems().clear();
            candidatTable.getItems().addAll(filteredCandidats);

            //statusLabel.setText("Filtres appliqués: " + filteredCandidats.size() + " résultat(s)");

            Stage stage = (Stage) applyFilterBtn.getScene().getWindow();
            NotificationManager.showInfo(stage, "Filtres appliqués", filteredCandidats.size() + " résultat(s) trouvé(s)");
        } catch (Exception e) {
            //statusLabel.setText("Erreur lors de l'application des filtres: " + e.getMessage());

            Stage stage = (Stage) applyFilterBtn.getScene().getWindow();
            NotificationManager.showError(stage, "Erreur de filtrage", "Erreur lors de l'application des filtres: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleResetFilter() {
        loadCandidats();
        filterStatusComboBox.setValue("Tous les statuts");
        filterCategoryComboBox.setValue("Toutes les catégories");


        //statusLabel.setText("Filtres réinitialisés");

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
                //statusLabel.setText("Données exportées avec succès vers " + file.getName());

                Stage stage = (Stage) exportBtn.getScene().getWindow();
                NotificationManager.showSuccess(stage, "Export réussi", "Données exportées avec succès vers " + file.getName());
            }
        } catch (Exception e) {
            //statusLabel.setText("Erreur lors de l'exportation: " + e.getMessage());

            Stage stage = (Stage) exportBtn.getScene().getWindow();
            NotificationManager.showError(stage, "Erreur d'exportation", "Erreur lors de l'exportation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void exportToCSV(File file) throws IOException {
        try (FileWriter writer = new FileWriter(file)) {
            // Write header
            writer.write("ID,Nom,Prénom,CIN,Date de naissance,Téléphone,Email,Adresse,Date d'inscription,Catégories,Actif\n");

            // Write data
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            for (Candidat candidat : candidatTable.getItems()) {
                writer.write(String.format("%d,%s,%s,%s,%s,%s,%s,%s,%s,%s,%b\n",
                        candidat.getId(),
                        candidat.getNom(),
                        candidat.getPrenom(),
                        candidat.getCin(),
                        candidat.getDateNaissance().format(formatter),
                        candidat.getTelephone(),
                        candidat.getEmail(),
                        candidat.getAdresse().replace(",", ";"), // Escape commas in address
                        candidat.getDateInscription().format(formatter),
                        String.join("-", candidat.getCategoriesPermis()),
                        candidat.isActif()));
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
            List<Candidat> candidats = candidatService.getAllCandidats();

            // Update statistics
            int total = candidats.size();
            long actifs = candidats.stream().filter(Candidat::isActif).count();
            long inactifs = total - actifs;

            totalCandidatsLabel.setText(String.valueOf(total));
            candidatsActifsLabel.setText(String.valueOf(actifs));
            candidatsInactifsLabel.setText(String.valueOf(inactifs));

            // Update pie chart for license categories
            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
            Map<String, Long> categoryCount = new HashMap<>();

            // Count occurrences of each category
            for (Candidat candidat : candidats) {
                for (String category : candidat.getCategoriesPermis()) {
                    categoryCount.put(category, categoryCount.getOrDefault(category, 0L) + 1);
                }
            }

            categoryCount.forEach((category, count) ->
                    pieChartData.add(new PieChart.Data(category + " (" + count + ")", count))
            );

            categoriePermisChart.setData(pieChartData);

            // Update bar chart for registrations by month
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Nombre d'inscriptions");

            Map<Month, Long> monthCount = candidats.stream()
                    .collect(Collectors.groupingBy(c -> c.getDateInscription().getMonth(), Collectors.counting()));

            // Sort by month
            Map<Month, Long> sortedMonthCount = new TreeMap<>();
            sortedMonthCount.putAll(monthCount);

            sortedMonthCount.forEach((month, count) ->
                    series.getData().add(new XYChart.Data<>(
                            month.getDisplayName(TextStyle.SHORT, Locale.FRENCH), count))
            );

            inscriptionsParMoisChart.getData().clear();
            inscriptionsParMoisChart.getData().add(series);

            //statusLabel.setText("Tableau de bord actualisé");

            Stage stage = (Stage) refreshDashboardBtn.getScene().getWindow();
            NotificationManager.showSuccess(stage, "Tableau de bord actualisé", "Les statistiques ont été mises à jour");
        } catch (Exception e) {
            //statusLabel.setText("Erreur lors de l'actualisation du tableau de bord: " + e.getMessage());

            Stage stage = (Stage) refreshDashboardBtn.getScene().getWindow();
            NotificationManager.showError(stage, "Erreur d'actualisation", "Erreur lors de l'actualisation du tableau de bord: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAjouter() {
        clearForm();
        disableForm(false);
        dateInscriptionPicker.setValue(LocalDate.now());
        //statusLabel.setText("Ajout d'un nouveau candidat");

        Stage stage = (Stage) ajouterBtn.getScene().getWindow();
        NotificationManager.showInfo(stage, "Nouveau candidat", "Veuillez remplir le formulaire pour ajouter un nouveau candidat");
    }

    @FXML
    private void handleModifier() {
        if (selectedCandidat != null) {
            disableForm(false);
            //statusLabel.setText("Modification du candidat: " + selectedCandidat.getNom());

            Stage stage = (Stage) modifierBtn.getScene().getWindow();
            NotificationManager.showInfo(stage, "Modification", "Modification du candidat: " + selectedCandidat.getNom());
        } else {
            //statusLabel.setText("Veuillez sélectionner un candidat à modifier");

            Stage stage = (Stage) modifierBtn.getScene().getWindow();
            NotificationManager.showWarning(stage, "Sélection requise", "Veuillez sélectionner un candidat à modifier");
        }
    }

    /**
     * Apply role-based access control based on the current user's role
     */
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

    @FXML
    private void handleSupprimer() {
        if (selectedCandidat != null) {
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
                    "Supprimer le candidat",
                    "Êtes-vous sûr de vouloir supprimer ce candidat ? Cette action est irréversible." +
                            " (Toutes les données reliées à ce candidat vont être supprimées aussi)",
                    ConfirmationDialog.DialogType.DELETE
            );

            if (confirmed) {
                try {
                    // Log the delete action
                    auditLogService.logAction(
                            "DELETE",
                            "CANDIDAT",
                            selectedCandidat.getId(),
                            "Suppression du candidat " + selectedCandidat.getNom() + " " + selectedCandidat.getPrenom()
                    );

                    candidatService.deleteCandidat(selectedCandidat);
                    candidatTable.getItems().remove(selectedCandidat);
                    clearForm();

                    NotificationManager.showSuccess(stage, "Candidat supprimé", "Le candidat a été supprimé avec succès");
                } catch (Exception e) {
                    NotificationManager.showError(stage, "Erreur de suppression", "Erreur lors de la suppression: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } else {
            Stage stage = (Stage) supprimerBtn.getScene().getWindow();
            NotificationManager.showWarning(stage, "Sélection requise", "Veuillez sélectionner un candidat à supprimer");
        }
    }

    @FXML
    private void handleAnnuler() {
        clearForm();
        disableForm(true);
        //statusLabel.setText("Opération annulée");

        Stage stage = (Stage) annulerBtn.getScene().getWindow();
        NotificationManager.showInfo(stage, "Opération annulée", "L'opération a été annulée");
    }

    @FXML
    private void handleEnregistrer() {
        if (validateForm()) {
            // Collect categories
            List<String> categories = new ArrayList<>();
            if (categorieA.isSelected()) categories.add("A");
            if (categorieB.isSelected()) categories.add("B");
            if (categorieC.isSelected()) categories.add("C");

            boolean isActif = actifOui.isSelected();

            try {
                Candidat candidat;
                if (selectedCandidat == null) {
                    // Create new candidat
                    candidat = new Candidat(
                            null, // ID
                            nomField.getText(),
                            prenomField.getText(),
                            cinField.getText(),
                            dateNaissancePicker.getValue(),
                            telephoneField.getText(),
                            adresseField.getText(),
                            emailField.getText(),
                            dateInscriptionPicker.getValue(),
                            categories,
                            tempCheminPhotoCIN,
                            tempCheminPhotoIdentite,
                            tempCheminCertificatMedical,
                            null, // cheminFichePdf
                            null, // cheminFichePng
                            isActif
                    );
                } else {
                    // Update existing candidat
                    candidat = selectedCandidat;
                    candidat.setNom(nomField.getText());
                    candidat.setPrenom(prenomField.getText());
                    candidat.setCin(cinField.getText());
                    candidat.setDateNaissance(dateNaissancePicker.getValue());
                    candidat.setTelephone(telephoneField.getText());
                    candidat.setAdresse(adresseField.getText());
                    candidat.setEmail(emailField.getText());
                    candidat.setDateInscription(dateInscriptionPicker.getValue());
                    candidat.setCategoriesPermis(categories);
                    candidat.setCheminPhotoCIN(tempCheminPhotoCIN);
                    candidat.setCheminPhotoIdentite(tempCheminPhotoIdentite);
                    candidat.setCheminCertificatMedical(tempCheminCertificatMedical);
                    candidat.setActif(isActif);
                }

                // Check uniqueness before saving
                List<String> uniquenessErrors = candidatService.validateUniqueness(candidat);
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
                        "Enregistrer le candidat",
                        "Êtes-vous sûr de vouloir enregistrer ces informations ?",
                        ConfirmationDialog.DialogType.SAVE
                );

                if (confirmed) {
                    // Save to database
                    candidatService.saveCandidat(candidat);
                    // Log the action
                    String action = (selectedCandidat == null) ? "CREATE" : "UPDATE";
                    auditLogService.logAction(
                            action,
                            "CANDIDAT",
                            candidat.getId(),
                            action + " candidat " + candidat.getNom() + " " + candidat.getPrenom());

                    // Refresh table
                    loadCandidats();
                    disableForm(true);

                    if (selectedCandidat == null) {
                        NotificationManager.showSuccess(stage, "Candidat ajouté", "Le candidat a été ajouté avec succès");
                    } else {
                        NotificationManager.showSuccess(stage, "Candidat modifié", "Le candidat a été modifié avec succès");
                    }
                }
            } catch (Exception e) {
                Stage stage = (Stage) enregistrerBtn.getScene().getWindow();
                NotificationManager.showError(stage, "Erreur d'enregistrement", "Erreur lors de l'enregistrement: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private boolean validateForm() {
        boolean isValid = true;
        Label tempErrorLabel = new Label();

        // Validate required text fields
        if (!Verification.validateName(nomField, tempErrorLabel)) {
            //statusLabel.setText("Le nom est requis et doit contenir uniquement des lettres");

            Stage stage = (Stage) nomField.getScene().getWindow();
            NotificationManager.showWarning(stage, "Validation", "Le nom est requis et doit contenir uniquement des lettres");
            isValid = false;
        }

        if (!Verification.validateName(prenomField, tempErrorLabel)) {
            //statusLabel.setText("Le prénom est requis et doit contenir uniquement des lettres");

            Stage stage = (Stage) prenomField.getScene().getWindow();
            NotificationManager.showWarning(stage, "Validation", "Le prénom est requis et doit contenir uniquement des lettres");
            isValid = false;
        }

        if (!Verification.validateCIN(cinField, tempErrorLabel)) {
            //statusLabel.setText("Le CIN est requis et doit être un nombre valide");

            Stage stage = (Stage) cinField.getScene().getWindow();
            NotificationManager.showWarning(stage, "Validation", tempErrorLabel.getText());
            isValid = false;
        }

        // Validate birth date (cannot be in the future)
        if (!Verification.validateBirthDate(dateNaissancePicker, tempErrorLabel)) {
            //statusLabel.setText(tempErrorLabel.getText());

            Stage stage = (Stage) dateNaissancePicker.getScene().getWindow();
            NotificationManager.showWarning(stage, "Validation", tempErrorLabel.getText());
            isValid = false;
        }

        if (!Verification.validatePhone(telephoneField, tempErrorLabel)) {
            //statusLabel.setText("Le téléphone est requis et doit contenir 8 chiffres");

            Stage stage = (Stage) telephoneField.getScene().getWindow();
            NotificationManager.showWarning(stage, "Validation", "Le téléphone est requis et doit contenir 8 chiffres (ne commence pas par 0)");
            isValid = false;
        }

        if (!Verification.validateEmail(emailField, tempErrorLabel)) {
            //statusLabel.setText("L'email est requis et doit être valide");

            Stage stage = (Stage) emailField.getScene().getWindow();
            NotificationManager.showWarning(stage, "Validation", "L'email est requis et doit être valide");
            isValid = false;
        }

        if (!Verification.validateRequired(adresseField, tempErrorLabel)) {
            //statusLabel.setText("L'adresse est requise");

            Stage stage = (Stage) adresseField.getScene().getWindow();
            NotificationManager.showWarning(stage, "Validation", "L'adresse est requise");
            isValid = false;
        }

        // Validate inscription date
        if (!Verification.validateDateInscription(dateInscriptionPicker, tempErrorLabel)) {
            //statusLabel.setText(tempErrorLabel.getText());

            Stage stage = (Stage) dateInscriptionPicker.getScene().getWindow();
            NotificationManager.showWarning(stage, "Validation", tempErrorLabel.getText());
            isValid = false;
        }

        // Validate at least one category is selected
        if (!Verification.validateCheckboxGroup(tempErrorLabel, categorieA, categorieB, categorieC)) {
            //statusLabel.setText("Au moins une catégorie de permis doit être sélectionnée");

            Stage stage = (Stage) categorieA.getScene().getWindow();
            NotificationManager.showWarning(stage, "Validation", "Au moins une catégorie de permis doit être sélectionnée");
            isValid = false;
        }

        return isValid;
    }

    @FXML
    private void handleSelectCIN() {
        File file = selectDocument("Photo de CIN");
        if (file != null) {
            try {
                String uniqueFileName = generateUniqueFileName(selectedCandidat, "CIN", file.getName());
                Path destination = Paths.get(DOCUMENTS_DIR, uniqueFileName);
                Files.copy(file.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);
                tempCheminPhotoCIN = destination.toString();
                updateDocumentLabels();
                //statusLabel.setText("Photo de CIN ajoutée: " + file.getName());

                Stage stage = (Stage) selectCINBtn.getScene().getWindow();
                NotificationManager.showSuccess(stage, "Document ajouté", "Photo de CIN ajoutée: " + file.getName());
            } catch (Exception e) {
                //statusLabel.setText("Erreur lors de l'ajout de la photo CIN: " + e.getMessage());

                Stage stage = (Stage) selectCINBtn.getScene().getWindow();
                NotificationManager.showError(stage, "Erreur de document", "Erreur lors de l'ajout de la photo CIN: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleSelectPhoto() {
        File file = selectDocument("Photo d'identité");
        if (file != null) {
            try {
                String uniqueFileName = generateUniqueFileName(selectedCandidat, "PHOTO", file.getName());
                Path destination = Paths.get(DOCUMENTS_DIR, uniqueFileName);
                Files.copy(file.toPath(), destination, StandardCopyOption .REPLACE_EXISTING);
                tempCheminPhotoIdentite = destination.toString();
                updateDocumentLabels();
                //statusLabel.setText("Photo d'identité ajoutée: " + file.getName());

                Stage stage = (Stage) selectPhotoBtn.getScene().getWindow();
                NotificationManager.showSuccess(stage, "Document ajouté", "Photo d'identité ajoutée: " + file.getName());
            } catch (Exception e) {
                //statusLabel.setText("Erreur lors de l'ajout de la photo d'identité: " + e.getMessage());

                Stage stage = (Stage) selectPhotoBtn.getScene().getWindow();
                NotificationManager.showError(stage, "Erreur de document", "Erreur lors de l'ajout de la photo d'identité: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleSelectCertificat() {
        File file = selectDocument("Certificat médical");
        if (file != null) {
            try {
                String uniqueFileName = generateUniqueFileName(selectedCandidat, "CERTIF", file.getName());
                Path destination = Paths.get(DOCUMENTS_DIR, uniqueFileName);
                Files.copy(file.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);
                tempCheminCertificatMedical = destination.toString();
                updateDocumentLabels();
                //statusLabel.setText("Certificat médical ajouté: " + file.getName());

                Stage stage = (Stage) selectCertificatBtn.getScene().getWindow();
                NotificationManager.showSuccess(stage, "Document ajouté", "Certificat médical ajouté: " + file.getName());
            } catch (Exception e) {
                //statusLabel.setText("Erreur lors de l'ajout du certificat médical: " + e.getMessage());

                Stage stage = (Stage) selectCertificatBtn.getScene().getWindow();
                NotificationManager.showError(stage, "Erreur de document", "Erreur lors de l'ajout du certificat médical: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private File selectDocument(String title) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner " + title);
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"),
                new FileChooser.ExtensionFilter("Documents PDF", "*.pdf"),
                new FileChooser.ExtensionFilter("Tous les fichiers", "*.*")
        );
        return fileChooser.showOpenDialog(selectCINBtn.getScene().getWindow());
    }

    private String generateUniqueFileName(Candidat candidat, String docType, String originalFileName) {
        String extension = originalFileName.substring(originalFileName.lastIndexOf('.'));
        String prefix = (candidat != null) ? candidat.getCin() : cinField.getText();
        if (prefix.isEmpty()) {
            prefix = "TEMP";
        }
        return prefix + "_" + docType + "_" + System.currentTimeMillis() + extension;
    }

    @FXML
    private void handleViewCIN() {
        viewDocument(tempCheminPhotoCIN);
    }

    @FXML
    private void handleViewPhoto() {
        viewDocument(tempCheminPhotoIdentite);
    }

    @FXML
    private void handleViewCertificat() {
        viewDocument(tempCheminCertificatMedical);
    }

    @FXML
    private void handleViewCinPath() {
        showDocumentPreview(cinPathLabel.getText());
    }

    @FXML
    private void handleViewPhotoPath() {
        showDocumentPreview(photoPathLabel.getText());
    }

    @FXML
    private void handleViewCertificatPath() {
        showDocumentPreview(certificatPathLabel.getText());
    }

    @FXML
    private void handleViewFichePdfPath() {
        showDocumentPreview(fichePdfPathLabel.getText());
    }

    private void showDocumentPreview(String documentPath) {
        try {
            System.out.println("showDocumentPreview: documentPath = " + documentPath);

            // Get the URL of the FXML file
            URL fxmlLocation = getClass().getResource("/IHM/DocumentPreview.fxml");
            System.out.println("FXML Location: " + fxmlLocation);

            if (fxmlLocation == null) {
                //statusLabel.setText("Could not load fxml, please make sure name is correct");

                Stage stage = (Stage) candidatTable.getScene().getWindow();
                NotificationManager.showError(stage, "Erreur de chargement", "Could not load fxml, please make sure name is correct");
                throw new IOException("Cannot resolve DocumentPreview.fxml");
            }

            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent root = loader.load();

            DocumentPreviewController controller = loader.getController();
            controller.setDocumentPath(documentPath);

            Stage stage = new Stage();
            stage.setTitle("Aperçu du document");
            stage.setScene(new Scene(root, 800, 600));
            stage.show();

        } catch (IOException e) {
            //statusLabel.setText("Erreur lors de l'ouverture de la fenêtre d'aperçu: " + e.getMessage());

            Stage stage = (Stage) candidatTable.getScene().getWindow();
            NotificationManager.showError(stage, "Erreur d'aperçu", "Erreur lors de l'ouverture de la fenêtre d'aperçu: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void viewDocument(String path) {
        if (path == null) {
            //statusLabel.setText("Aucun document à afficher");

            Stage stage = (Stage) candidatTable.getScene().getWindow();
            NotificationManager.showWarning(stage, "Document manquant", "Aucun document à afficher");
            return;
        }

        try {
            File file = new File(path);
            if (!file.exists()) {
                //statusLabel.setText("Le fichier n'existe pas: " + path);

                Stage stage = (Stage) candidatTable.getScene().getWindow();
                NotificationManager.showError(stage, "Fichier introuvable", "Le fichier n'existe pas: " + path);
                return;
            }

            String lowerCasePath = path.toLowerCase();
            if (lowerCasePath.endsWith(".jpg") || lowerCasePath.endsWith(".jpeg") ||
                    lowerCasePath.endsWith(".png") || lowerCasePath.endsWith(".gif") ||
                    lowerCasePath.endsWith(".bmp")) {

                // Display image in the preview
                Image image = new Image(file.toURI().toString());
                documentPreview.setImage(image);
                //statusLabel.setText("PDF affiché: " + file.getName());

                Stage stage = (Stage) documentPreview.getScene().getWindow();
                NotificationManager.showInfo(stage, "Document affiché", "Document affiché: " + file.getName());
            } else {
                // For non-image files, try to open with default application
                try {
                    java.awt.Desktop.getDesktop().open(file);
                    //statusLabel.setText("PDF ouvert: " + file.getName());

                    Stage stage = (Stage) candidatTable.getScene().getWindow();
                    NotificationManager.showInfo(stage, "Document ouvert", "Document ouvert: " + file.getName());
                } catch (Exception e) {
                    //statusLabel.setText("Impossible d'ouvrir le document: " + e.getMessage());

                    Stage stage = (Stage) candidatTable.getScene().getWindow();
                    NotificationManager.showError(stage, "Erreur d'ouverture", "Impossible d'ouvrir le document: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            //statusLabel.setText("Erreur lors de l'affichage du document: " + e.getMessage());

            Stage stage = (Stage) candidatTable.getScene().getWindow();
            NotificationManager.showError(stage, "Erreur d'affichage", "Erreur lors de l'affichage du document: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupDocumentButtons() {
        // Setup category combo box
        categoryComboBox.getItems().addAll("A", "B", "C");
        categoryComboBox.setValue("B");

        // Enable/disable buttons based on selection
        generateFicheBtn.disableProperty().bind(
                candidatTable.getSelectionModel().selectedItemProperty().isNull());


        // Add event handlers
        // ***  USE THE NEW handleGenerateDocument METHOD  ***
        generateFicheBtn.setOnAction(this::handleGenerateDocument);
        generateListBtn.setOnAction(this::handleGenerateDocument);
        generateCategoryListBtn.setOnAction(this::handleGenerateDocument);
    }

    // Add this to the initialize() method after setting up other components
    @FXML
    private void handleGenerateDocument(ActionEvent event) {  // ActionEvent is important
        if(documentFormatComboBox != null){
            String format = documentFormatComboBox.getValue(); // Get the selected format
        }

        Button sourceButton = (Button) event.getSource(); // Determine which button was clicked
        String documentType = "";

        if (sourceButton == generateFicheBtn) {
            documentType = "Fiche Candidat";
        } else if (sourceButton == generateListBtn) {
            documentType = "Liste Tous Candidats Actifs";
        } else if (sourceButton == generateCategoryListBtn) {
            documentType = "Liste par Catégorie";
        } else if (sourceButton == generateSearchResultsBtn) {
            documentType = "Liste Résultats Recherche";
        }

        try {
            String filePath = null;

            switch (documentType) {
                case "Fiche Candidat":
                    if (selectedCandidat != null) {
                        filePath = candidatService.generateCandidatFichePDF(selectedCandidat.getId()); // Always generate PDF in this example
                    } else {
                        //statusLabel.setText("Veuillez sélectionner un candidat.");

                        Stage stage = (Stage) sourceButton.getScene().getWindow();
                        NotificationManager.showWarning(stage, "Sélection requise", "Veuillez sélectionner un candidat.");
                        return;
                    }
                    break;
                case "Liste Tous Candidats Actifs":
                    filePath = candidatService.generateActiveCandiatsListPDF();
                    break;
                case "Liste par Catégorie":
                    String category = categoryComboBox.getValue();
                    filePath = candidatService.generateCandidatsByCategoryPDF(category);
                    break;
                case "Liste Résultats Recherche":
                    String searchTerm = rechercheField.getText();
                    filePath = candidatService.generateSearchResultsPDF(searchTerm);
                    break;
                default:
                    //statusLabel.setText("Type de document non reconnu.");

                    Stage stage = (Stage) sourceButton.getScene().getWindow();
                    NotificationManager.showError(stage, "Type inconnu", "Type de document non reconnu.");
                    return;
            }

            if (filePath != null) {
                openPdfFile(filePath); // Always open as PDF in this example
                //statusLabel.setText("Document généré: " + filePath);

                Stage stage = (Stage) sourceButton.getScene().getWindow();
                NotificationManager.showSuccess(stage, "Document généré", "Document généré avec succès: " + filePath);
            }

        } catch (IOException e) {
            //statusLabel.setText("Erreur lors de la génération du document: " + e.getMessage());

            Stage stage = (Stage) sourceButton.getScene().getWindow();
            NotificationManager.showError(stage, "Erreur de génération", "Erreur lors de la génération du document: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Helper method to open a PDF file with the default PDF viewer
     */
    private void openPdfFile(String pdfPath) {
        try {
            if (Desktop.isDesktopSupported()) {
                File file = new File(pdfPath);
                Desktop.getDesktop().open(file);
            } else {
                //statusLabel.setText("PDF généré: " + pdfPath + " (Impossible d'ouvrir automatiquement)");

                Stage stage = (Stage) candidatTable.getScene().getWindow();
                NotificationManager.showInfo(stage, "PDF généré", "PDF généré: " + pdfPath + " (Impossible d'ouvrir automatiquement)");
            }
        } catch (IOException e) {
            //statusLabel.setText("Erreur lors de l'ouverture du PDF: " + e.getMessage());

            Stage stage = (Stage) candidatTable.getScene().getWindow();
            NotificationManager.showError(stage, "Erreur d'ouverture", "Erreur lors de l'ouverture du PDF: " + e.getMessage());
            e.printStackTrace();
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
            helpStage.setTitle("Aide - Auto-cole Pro");
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
    private void setupKeyboardShortcuts() {
        Platform.runLater(() -> {
            if (candidatTable.getScene() != null) {
                candidatTable.getScene().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                    // Ctrl+F to focus on search field
                    if (event.isControlDown() && event.getCode() == KeyCode.F) {
                        rechercheField.requestFocus();
                        event.consume();
                    }

                    // F1 for help
                    if (event.getCode() == KeyCode.F1) {
                        handleHelp(new ActionEvent(candidatTable, null));
                        event.consume();
                    }
                    // F2 for settings
                    if (event.getCode() == KeyCode.F2) {
                        handleSettings(new ActionEvent(candidatTable, null));
                        event.consume();
                    }


                    // Ctrl+N for new moniteur
                    if (event.isControlDown() && event.getCode() == KeyCode.N) {
                        handleAjouter();
                        event.consume();
                    }

                    // Ctrl+E to edit selected moniteur
                    if (event.isControlDown() && event.getCode() == KeyCode.E) {
                        if (selectedCandidat != null) {
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

                    // Ctrl+R to refresh the moniteur list
                    if (event.isControlDown() && event.getCode() == KeyCode.R) {
                        loadCandidats();
                        event.consume();
                    }

                    // Alt+B to go back to main menu
                    if (event.isAltDown() && event.getCode() == KeyCode.B) {
                        handleBack(new ActionEvent(candidatTable, null));
                        event.consume();
                    }
                });
            }
        });
    }

    /**
     * This method should be called after the scene is set
     * For example, in the initialize method after all components are initialized
     */
    public void initializeKeyboardShortcuts() {
        // We need to wait for the scene to be fully initialized
        Platform.runLater(() -> {
            if (candidatTable.getScene() != null) {
                setupKeyboardShortcuts();
            } else {
                // If the scene is not yet available, try again later
                Platform.runLater(this::initializeKeyboardShortcuts);
            }
        });
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

    /**
     * Handles back navigation with a simple fade transition
     * @param event The action event
     */
    @FXML
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

