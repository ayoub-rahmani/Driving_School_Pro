package org.example.Controllers;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.Entities.Moniteur;
import org.example.Entities.User;
import org.example.Rep.MoniteurRep;
import org.example.Service.AuditLogService;
import org.example.Service.MoniteurService;
import org.example.Utils.NotificationManager;
import org.example.Utils.SessionManager;
import org.example.Utils.Verification;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import javafx.stage.Modality;
import org.example.Utils.ConfirmationDialog;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.VBox;

public class MoniteurController implements Initializable {

    @FXML private ComboBox<String> statutFilter;
    @FXML private ComboBox<String> categorieFilter;
    @FXML private DatePicker dateFilter;

    @FXML private TableView<Moniteur> moniteurTable;
    @FXML private TableColumn<Moniteur, Long> idColumn;
    @FXML private TableColumn<Moniteur, String> nomColumn;
    @FXML private TableColumn<Moniteur, String> prenomColumn;
    @FXML private TableColumn<Moniteur, String> cinColumn;

    @FXML private TextField rechercheField, nomField, prenomField, cinField, telephoneField, numPermisField, salaireField, experienceField, diplomesField, motifField;
    @FXML private DatePicker dateNaissancePicker, dateEmbauchePicker, dateFinContratPicker;
    @FXML private CheckBox categorieA, categorieB, categorieC;
    @FXML private RadioButton disponibleOui, disponibleNon;
    @FXML private TextArea notesField;

    @FXML private Button ajouterBtn, modifierBtn, supprimerBtn, annulerBtn, enregistrerBtn, BackBtn, exportBtn;
    @FXML private Button dashboardBtn, refreshDashboardBtn;

    @FXML private Label moniteurNameLabel;
    @FXML private ImageView moniteurPhoto;

    // Add these new fields for availability indicator
    @FXML private StackPane availabilityIndicator;
    @FXML private Label availabilityLabel;

    // Add this field for license badges container
    @FXML private HBox licenseBadgesContainer;

    @FXML private ToggleGroup disponibiliteGroup;

    // Dashboard components
    @FXML private VBox dashboardPane;
    @FXML private ScrollPane formScrollPane;
    @FXML private Label totalMoniteursLabel;
    @FXML private Label moniteursDisponiblesLabel;
    @FXML private Label moniteursIndisponiblesLabel;
    @FXML private PieChart categoriePermisChart;
    @FXML private BarChart<String, Number> experienceChart;

    private MoniteurService moniteurService;
    private Moniteur selectedMoniteur;
    private AuditLogService auditLogService;

    // Dashboard state
    private boolean isDashboardVisible = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumns();
        moniteurService = new MoniteurService(new MoniteurRep());

        // Initialize audit log service
        auditLogService = new AuditLogService();

        // Apply role-based access control
        applyRoleBasedAccess();

        // Initialize filters
        statutFilter.setValue("Tous les statuts");
        categorieFilter.setValue("Toutes les catégories");

        loadMoniteurs();
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

        // Set up checkbox listeners to update license badges in real-time
        categorieA.selectedProperty().addListener((obs, oldVal, newVal) -> {
            updateLicenseBadges();
        });

        categorieB.selectedProperty().addListener((obs, oldVal, newVal) -> {
            updateLicenseBadges();
        });

        categorieC.selectedProperty().addListener((obs, oldVal, newVal) -> {
            updateLicenseBadges();
        });

        // Setup keyboard shortcuts
        setupKeyboardShortcuts();

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

    // Helper method to update license badges based on selected categories
    private void updateLicenseBadges() {
        // Clear existing badges
        licenseBadgesContainer.getChildren().clear();

        // Add badges for selected categories with specific colors
        if (categorieA.isSelected()) {
            Label badgeA = new Label("A");
            badgeA.getStyleClass().add("license-badge");
            badgeA.getStyleClass().add("license-badge-a"); // Custom class for type A (red)
            licenseBadgesContainer.getChildren().add(badgeA);
        }

        if (categorieB.isSelected()) {
            Label badgeB = new Label("B");
            badgeB.getStyleClass().add("license-badge");
            badgeB.getStyleClass().add("license-badge-b"); // Custom class for type B (blue)
            licenseBadgesContainer.getChildren().add(badgeB);
        }

        if (categorieC.isSelected()) {
            Label badgeC = new Label("C");
            badgeC.getStyleClass().add("license-badge");
            badgeC.getStyleClass().add("license-badge-c"); // Custom class for type C (green)
            licenseBadgesContainer.getChildren().add(badgeC);
        }
    }

    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nomColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        prenomColumn.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        if (cinColumn != null) {
            cinColumn.setCellValueFactory(new PropertyValueFactory<>("cin"));
        }
    }

    @FXML
    private void handleClearDate() {
        dateFilter.setValue(null);
    }

    @FXML
    private void handleApplyFilter() {
        String statut = statutFilter.getValue();
        String categorie = categorieFilter.getValue();

        try {
            List<Moniteur> filteredMoniteurs = moniteurService.filterMoniteurs(statut, categorie);
            moniteurTable.getItems().clear();
            moniteurTable.getItems().addAll(filteredMoniteurs);

            Stage stage = (Stage) statutFilter.getScene().getWindow();
            NotificationManager.showInfo(stage, "Filtres appliqués", filteredMoniteurs.size() + " résultat(s) trouvé(s)");
        } catch (Exception e) {
            Stage stage = (Stage) statutFilter.getScene().getWindow();
            NotificationManager.showError(stage, "Erreur de filtrage", "Erreur lors du filtrage: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleResetFilter() {
        loadMoniteurs();
        statutFilter.setValue("Tous les statuts");
        categorieFilter.setValue("Toutes les catégories");

        Stage stage = (Stage) statutFilter.getScene().getWindow();
        NotificationManager.showInfo(stage, "Filtres réinitialisés", "Tous les filtres ont été réinitialisés");
    }

    private void loadMoniteurs() {
        try {
            moniteurTable.getItems().clear();
            List<Moniteur> moniteurs = moniteurService.getAllMoniteurs();
            moniteurTable.getItems().addAll(moniteurs);

            if (moniteurTable.getScene() != null) {
                Stage stage = (Stage) moniteurTable.getScene().getWindow();
                NotificationManager.showInfo(stage, "Moniteurs chargés", moniteurs.size() + " moniteur(s) chargé(s)");
            }
        } catch (Exception e) {
            if (moniteurTable.getScene() != null) {
                Stage stage = (Stage) moniteurTable.getScene().getWindow();
                NotificationManager.showError(stage, "Erreur de chargement", "Erreur lors du chargement des moniteurs: " + e.getMessage());
            }
            e.printStackTrace();
        }
    }

    private void populateForm(Moniteur moniteur) {
        if (moniteur == null) return;

        nomField.setText(moniteur.getNom());
        prenomField.setText(moniteur.getPrenom());
        cinField.setText(moniteur.getCin());
        dateNaissancePicker.setValue(moniteur.getDateNaissance());
        telephoneField.setText(moniteur.getTelephone());
        dateEmbauchePicker.setValue(moniteur.getDateEmbauche());
        dateFinContratPicker.setValue(moniteur.getDateFinContrat());
        numPermisField.setText(moniteur.getNumPermis());
        motifField.setText(moniteur.getMotif());
        salaireField.setText(String.valueOf(moniteur.getSalaire()));
        experienceField.setText(String.valueOf(moniteur.getExperience()));
        diplomesField.setText(moniteur.getDiplomes());
        notesField.setText(moniteur.getNotes());

        categorieA.setSelected(moniteur.getCategoriesPermis().contains("A"));
        categorieB.setSelected(moniteur.getCategoriesPermis().contains("B"));
        categorieC.setSelected(moniteur.getCategoriesPermis().contains("C"));

        disponibleOui.setSelected(moniteur.isDisponible());
        disponibleNon.setSelected(!moniteur.isDisponible());

        moniteurNameLabel.setText(moniteur.getNom() + " " + moniteur.getPrenom()); // Update header label

        // Update availability indicator based on moniteur's status
        updateAvailabilityIndicator(moniteur.isDisponible());

        // Update license badges based on moniteur's categories
        updateLicenseBadges();
    }

    private void clearForm() {
        nomField.clear(); prenomField.clear(); cinField.clear(); telephoneField.clear();
        numPermisField.clear(); salaireField.clear(); motifField.clear();
        experienceField.clear(); diplomesField.clear(); notesField.clear();

        dateNaissancePicker.setValue(null); dateEmbauchePicker.setValue(null); dateFinContratPicker.setValue(null);
        categorieA.setSelected(false); categorieB.setSelected(false); categorieC.setSelected(false);
        disponibleOui.setSelected(false); disponibleNon.setSelected(false);
        selectedMoniteur = null;
        moniteurNameLabel.setText("Nouveau Moniteur");

        // Reset availability indicator to default (available)
        updateAvailabilityIndicator(true);

        // Clear license badges
        licenseBadgesContainer.getChildren().clear();
    }

    private void disableForm(boolean disable) {
        nomField.setDisable(disable); prenomField.setDisable(disable); cinField.setDisable(disable); telephoneField.setDisable(disable);
        dateNaissancePicker.setDisable(disable); dateEmbauchePicker.setDisable(disable);dateFinContratPicker.setDisable(disable);
        numPermisField.setDisable(disable); salaireField.setDisable(disable);experienceField.setDisable(disable);
        diplomesField.setDisable(disable); notesField.setDisable(disable); motifField.setDisable(disable);
        categorieA.setDisable(disable); categorieB.setDisable(disable); categorieC.setDisable(disable);
        disponibleOui.setDisable(disable); disponibleNon.setDisable(disable);
        enregistrerBtn.setDisable(disable); annulerBtn.setDisable(disable);
    }

    private void setupSelectionListener() {
        moniteurTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            selectedMoniteur = newSelection;
            populateForm(selectedMoniteur);
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
            List<Moniteur> moniteurs = moniteurService.getAllMoniteurs();

            // Update statistics
            int total = moniteurs.size();
            long disponibles = moniteurs.stream().filter(Moniteur::isDisponible).count();
            long indisponibles = total - disponibles;

            totalMoniteursLabel.setText(String.valueOf(total));
            moniteursDisponiblesLabel.setText(String.valueOf(disponibles));
            moniteursIndisponiblesLabel.setText(String.valueOf(indisponibles));

            // Update pie chart for license categories with specific colors
            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
            Map<String, Long> categoryCount = new HashMap<>();

            // Count occurrences of each category
            for (Moniteur moniteur : moniteurs) {
                for (String category : moniteur.getCategoriesPermis()) {
                    categoryCount.put(category, categoryCount.getOrDefault(category, 0L) + 1);
                }
            }

            // Define specific colors for each category
            Map<String, String> categoryColors = new HashMap<>();
            categoryColors.put("A", "#e74c3c"); // Red for category A
            categoryColors.put("B", "#3498db"); // Blue for category B
            categoryColors.put("C", "#2ecc71"); // Green for category C

            // Add data to pie chart
            categoryCount.forEach((category, count) ->
                    pieChartData.add(new PieChart.Data(category + " (" + count + ")", count))
            );

            categoriePermisChart.setData(pieChartData);

            // Apply specific colors to pie chart slices based on category

            // Update bar chart for experience with a single color
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Nombre de moniteurs");

            // Group moniteurs by experience ranges
            Map<String, Long> experienceRanges = new HashMap<>();
            experienceRanges.put("0-2 ans", moniteurs.stream().filter(m -> m.getExperience() >= 0 && m.getExperience() < 2).count());
            experienceRanges.put("2-5 ans", moniteurs.stream().filter(m -> m.getExperience() >= 2 && m.getExperience() < 5).count());
            experienceRanges.put("5-10 ans", moniteurs.stream().filter(m -> m.getExperience() >= 5 && m.getExperience() < 10).count());
            experienceRanges.put("10+ ans", moniteurs.stream().filter(m -> m.getExperience() >= 10).count());

            // Add data to series
            for (Map.Entry<String, Long> entry : experienceRanges.entrySet()) {
                series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
            }

            experienceChart.getData().clear();
            experienceChart.getData().add(series);

            // Apply a single color to all bar chart bars
            String barColor = "#bc0c0c"; // Orange
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
            List<Moniteur> moniteurs = moniteurService.searchMoniteurs(searchText);
            moniteurTable.getItems().clear();
            moniteurTable.getItems().addAll(moniteurs);

            Stage stage = (Stage) rechercheField.getScene().getWindow();
            NotificationManager.showInfo(stage, "Recherche terminée", moniteurs.size() + " résultat(s) trouvé(s)");
        } catch (Exception e) {
            Stage stage = (Stage) rechercheField.getScene().getWindow();
            NotificationManager.showError(stage, "Erreur de recherche", "Erreur lors de la recherche: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML private void handleAjouter() {
        clearForm(); disableForm(false);

        Stage stage = (Stage) ajouterBtn.getScene().getWindow();
        NotificationManager.showInfo(stage, "Nouveau moniteur", "Veuillez remplir le formulaire pour ajouter un nouveau moniteur");
    }

    @FXML private void handleModifier() {
        if (selectedMoniteur != null) {
            disableForm(false);

            Stage stage = (Stage) modifierBtn.getScene().getWindow();
            NotificationManager.showInfo(stage, "Modification", "Modification du moniteur: " + selectedMoniteur.getNom());
        } else {
            Stage stage = (Stage) modifierBtn.getScene().getWindow();
            NotificationManager.showWarning(stage, "Sélection requise", "Veuillez sélectionner un moniteur à modifier");
        }
    }

    @FXML
    private void handleSupprimer() {
        if (selectedMoniteur != null) {
            Stage stage = (Stage) supprimerBtn.getScene().getWindow();
            boolean confirmed = ConfirmationDialog.show(
                    stage,
                    "Confirmation de suppression",
                    "Supprimer le moniteur",
                    "Êtes-vous sûr de vouloir supprimer ce moniteur ? Cette action est irréversible.",
                    ConfirmationDialog.DialogType.DELETE
            );

            if (confirmed) {
                try {
                        // Log the delete action
                    auditLogService.logAction(
                            "DELETE",
                            "MONITEUR",
                            selectedMoniteur.getId(),
                            "Suppression du Moniteur " + selectedMoniteur.getNom() + " " + selectedMoniteur.getPrenom()
                    );
                    moniteurService.deleteMoniteur(selectedMoniteur);
                    moniteurTable.getItems().remove(selectedMoniteur);
                    clearForm();

                    NotificationManager.showSuccess(stage, "Moniteur supprimé", "Le moniteur a été supprimé avec succès");
                } catch (Exception e) {
                    NotificationManager.showError(stage, "Erreur de suppression", "Erreur lors de la suppression: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } else {
            Stage stage = (Stage) supprimerBtn.getScene().getWindow();
            NotificationManager.showWarning(stage, "Sélection requise", "Veuillez sélectionner un moniteur à supprimer");
        }
    }

    @FXML private void handleAnnuler() {
        clearForm(); disableForm(true);

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
    // Add the exportToCSV method
    private void exportToCSV(File file) throws IOException {
        try (FileWriter writer = new FileWriter(file)) {
            // Write header
            writer.write("ID,Nom,Prénom,CIN,Date de naissance,Téléphone,Date d'embauche,Date fin contrat,Numéro permis,Catégories,Disponible,Motif,Salaire,Expérience,Diplômes,Notes\n");

            // Write data
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            for (Moniteur moniteur : moniteurTable.getItems()) {
                writer.write(String.format("%d,%s,%s,%s,%s,%s,%s,%s,%s,%s,%b,%s,%.2f,%.1f,%s,%s\n",
                        moniteur.getId(),
                        moniteur.getNom().replace(",", ";"),
                        moniteur.getPrenom().replace(",", ";"),
                        moniteur.getCin(),
                        moniteur.getDateNaissance() != null ? moniteur.getDateNaissance().format(formatter) : "",
                        moniteur.getTelephone(),
                        moniteur.getDateEmbauche() != null ? moniteur.getDateEmbauche().format(formatter) : "",
                        moniteur.getDateFinContrat() != null ? moniteur.getDateFinContrat().format(formatter) : "",
                        moniteur.getNumPermis(),
                        String.join("|", moniteur.getCategoriesPermis()),
                        moniteur.isDisponible(),
                        moniteur.getMotif() != null ? moniteur.getMotif().replace(",", ";") : "",
                        moniteur.getSalaire(),
                        moniteur.getExperience(),
                        moniteur.getDiplomes() != null ? moniteur.getDiplomes().replace(",", ";") : "",
                        moniteur.getNotes() != null ? moniteur.getNotes().replace(",", ";").replace("\n", " ") : ""));
            }
        }
    }

    @FXML
    private void handleEnregistrer() {
        if (validateForm()) {
            List<String> categories = new ArrayList<>();
            if (categorieA.isSelected()) categories.add("A");
            if (categorieB.isSelected()) categories.add("B");
            if (categorieC.isSelected()) categories.add("C");

            boolean isDisponible = disponibleOui.isSelected();

            try {
                // Handle optional fields
                String motif = motifField.getText().trim();

                // Handle experience field - default to 0 if empty
                double experience = 0.0;
                if (!experienceField.getText().trim().isEmpty()) {
                    try {
                        experience = Double.parseDouble(experienceField.getText().trim());
                    } catch (NumberFormatException e) {
                        // Keep default value if parsing fails
                    }
                }

                String diplomes = diplomesField.getText().trim();
                String notes = notesField.getText().trim();

                // Parse salary - this is required
                double salaire = Double.parseDouble(salaireField.getText().trim());

                Moniteur moniteur;
                if (selectedMoniteur == null) {
                    // Create new moniteur
                    moniteur = new Moniteur(
                            null, nomField.getText(), prenomField.getText(), cinField.getText(), dateNaissancePicker.getValue(),
                            telephoneField.getText(), dateEmbauchePicker.getValue(), dateFinContratPicker.getValue(), numPermisField.getText(),
                            categories, isDisponible, motif, salaire, experience, diplomes, notes
                    );
                } else {
                    // Update existing moniteur
                    moniteur = selectedMoniteur;
                    moniteur.setNom(nomField.getText());
                    moniteur.setPrenom(prenomField.getText());
                    moniteur.setCin(cinField.getText());
                    moniteur.setDateNaissance(dateNaissancePicker.getValue());
                    moniteur.setTelephone(telephoneField.getText());
                    moniteur.setDateEmbauche(dateEmbauchePicker.getValue());
                    moniteur.setDateFinContrat(dateFinContratPicker.getValue());
                    moniteur.setNumPermis(numPermisField.getText());
                    moniteur.setCategoriesPermis(categories);
                    moniteur.setDisponible(isDisponible);
                    moniteur.setMotif(motif);
                    moniteur.setSalaire(salaire);
                    moniteur.setExperience(experience);
                    moniteur.setDiplomes(diplomes);
                    moniteur.setNotes(notes);
                }

                // Check uniqueness before saving
                List<String> uniquenessErrors = moniteurService.validateUniqueness(moniteur);
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
                        "Enregistrer le moniteur",
                        "Êtes-vous sûr de vouloir enregistrer ces informations ?",
                        ConfirmationDialog.DialogType.SAVE
                );

                if (confirmed) {
                    moniteur = moniteurService.saveMoniteur(moniteur);
                    // Log the action
                    String action = (selectedMoniteur == null) ? "CREATE" : "UPDATE";
                    auditLogService.logAction(
                            action,
                            "MONITEUR",
                            moniteur.getId(),
                            action + " moniteur " + moniteur.getNom() + " " + moniteur.getPrenom());

                    // Refresh the table
                    loadMoniteurs();
                    disableForm(true);

                    if (selectedMoniteur == null) {
                        NotificationManager.showSuccess(stage, "Moniteur ajouté", "Le moniteur a été ajouté avec succès");
                    } else {
                        NotificationManager.showSuccess(stage, "Moniteur modifié", "Le moniteur a été modifié avec succès");
                    }
                }
            } catch (NumberFormatException e) {
                Stage stage = (Stage) enregistrerBtn.getScene().getWindow();
                NotificationManager.showError(stage, "Erreur de format", "Le salaire doit être un nombre valide");
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

            // Navigate to the moniteurs section
            helpController.navigateToSection("moniteurs_add");

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

    private void setupKeyboardShortcuts() {
        Platform.runLater(() -> {
            if (moniteurTable.getScene() != null) {
                moniteurTable.getScene().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                    // Ctrl+F to focus on search field
                    if (event.isControlDown() && event.getCode() == KeyCode.F) {
                        rechercheField.requestFocus();
                        event.consume();
                    }

                    // F1 for help
                    if (event.getCode() == KeyCode.F1) {
                        handleHelp(new ActionEvent(moniteurTable, null));
                        event.consume();
                    }
                    // F2 for settings
                    if (event.getCode() == KeyCode.F2) {
                        handleSettings(new ActionEvent(moniteurTable, null));
                        event.consume();
                    }


                    // Ctrl+N for new moniteur
                    if (event.isControlDown() && event.getCode() == KeyCode.N) {
                        handleAjouter();
                        event.consume();
                    }

                    // Ctrl+E to edit selected moniteur
                    if (event.isControlDown() && event.getCode() == KeyCode.E) {
                        if (selectedMoniteur != null) {
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
                        loadMoniteurs();
                        event.consume();
                    }

                    // Alt+B to go back to main menu
                    if (event.isAltDown() && event.getCode() == KeyCode.B) {
                        handleBack(new ActionEvent(moniteurTable, null));
                        event.consume();
                    }
                });
            }
        });
    }

    private boolean validateForm() {
        boolean isValid = true;
        Label tempErrorLabel = new Label();

        // Validate name fields
        if (!Verification.validateName(nomField, tempErrorLabel)) {
            Stage stage = (Stage) nomField.getScene().getWindow();
            NotificationManager.showWarning(stage, "Validation", "Le nom est requis et doit contenir uniquement des lettres");
            isValid = false;
        }

        if (!Verification.validateName(prenomField, tempErrorLabel)) {
            Stage stage = (Stage) prenomField.getScene().getWindow();
            NotificationManager.showWarning(stage, "Validation", "Le prénom est requis et doit contenir uniquement des lettres");
            isValid = false;
        }

        // Validate CIN
        if (!Verification.validateCIN(cinField, tempErrorLabel)) {
            Stage stage = (Stage) cinField.getScene().getWindow();
            NotificationManager.showWarning(stage, "Validation", tempErrorLabel.getText());
            isValid = false;
        }

        // Validate birth date (cannot be in the future)
        if (!Verification.validateBirthDate(dateNaissancePicker, tempErrorLabel)) {
            Stage stage = (Stage) dateNaissancePicker.getScene().getWindow();
            NotificationManager.showWarning(stage, "Validation", tempErrorLabel.getText());
            isValid = false;
        }

        // Validate phone
        if (!Verification.validatePhone(telephoneField, tempErrorLabel)) {
            Stage stage = (Stage) telephoneField.getScene().getWindow();
            NotificationManager.showWarning(stage, "Validation", "Le téléphone est requis et doit contenir 8 chiffres (ne commence pas par 0)");
            isValid = false;
        }

        // Validate dates
        if (!Verification.validateDateEmbuche(dateEmbauchePicker, tempErrorLabel)) {
            Stage stage = (Stage) dateEmbauchePicker.getScene().getWindow();
            NotificationManager.showWarning(stage, "Validation", tempErrorLabel.getText());
            isValid = false;
        }

        if (dateFinContratPicker.getValue() == null) {
            dateFinContratPicker.setStyle("-fx-border-color: #ef4444; -fx-border-width: 1px;");
            Stage stage = (Stage) dateFinContratPicker.getScene().getWindow();
            NotificationManager.showWarning(stage, "Validation", "La date de fin de contrat est requise");
            isValid = false;
        } else {
            dateFinContratPicker.setStyle("");
        }

        // Validate numeric fields - only salary is required
        if (!Verification.validateNumeric(salaireField, tempErrorLabel, 0)) {
            Stage stage = (Stage) salaireField.getScene().getWindow();
            NotificationManager.showWarning(stage, "Validation", "Le salaire doit être un nombre positif");
            isValid = false;
        }

        // Experience is now optional - only validate if not empty
        if (!experienceField.getText().trim().isEmpty() && !Verification.validateNumeric(experienceField, tempErrorLabel, 0)) {
            Stage stage = (Stage) experienceField.getScene().getWindow();
            NotificationManager.showWarning(stage, "Validation", "L'expérience doit être un nombre positif");
            isValid = false;
        }

        // Validate required fields
        if (!Verification.validateRequired(numPermisField, tempErrorLabel)) {
            Stage stage = (Stage) numPermisField.getScene().getWindow();
            NotificationManager.showWarning(stage, "Validation", "Le numéro de permis est requis");
            isValid = false;
        }

        // Validate at least one category is selected
        if (!Verification.validateCheckboxGroup(tempErrorLabel, categorieA, categorieB, categorieC)) {
            Stage stage = (Stage) categorieA.getScene().getWindow();
            NotificationManager.showWarning(stage, "Validation", "Au moins une catégorie de permis doit être sélectionnée");
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
            //exportBtn.setDisable(true);
            dashboardBtn.setDisable(true);
            return;
        }

        // Secretary cannot delete candidates
        supprimerBtn.setDisable(!currentUser.canDeleteCandidats());
    }
}

