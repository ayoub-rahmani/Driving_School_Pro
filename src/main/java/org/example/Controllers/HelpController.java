package org.example.Controllers;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.EventTarget;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.prefs.Preferences;

public class HelpController implements Initializable {

    // UI Components
    @FXML private BorderPane rootPane;
    @FXML private Accordion helpAccordion;
    @FXML private WebView helpContentView;
    @FXML private TextField searchField;
    @FXML private Button searchButton;
    @FXML private ListView<String> searchResultsList;
    @FXML private TabPane mainTabPane;
    @FXML private Tab contentTab;
    @FXML private Tab searchTab;
    @FXML private Button printButton;
    @FXML private Button backButton;
    @FXML private Button forwardButton;
    @FXML private Button homeButton;
    @FXML private Label breadcrumbLabel;
    @FXML private Label lastUpdatedLabel;
    @FXML private Label versionLabel;
    @FXML private Hyperlink contactLink;
    @FXML private Hyperlink websiteLink;
    @FXML private ComboBox<String> fontSizeComboBox;
    @FXML private ToggleButton darkModeToggle;
    @FXML private Button closeBtn;

    // Section TitledPanes
    @FXML private TitledPane generalSection;
    @FXML private TitledPane candidatsSection;
    @FXML private TitledPane moniteursSection;
    @FXML private TitledPane vehiculesSection;
    @FXML private TitledPane examensSection;
    @FXML private TitledPane paiementsSection;
    @FXML private TitledPane settingsSection;

    // Data structures
    private final Map<String, String> helpContent = new HashMap<>();
    private final Stack<String> navigationHistory = new Stack<>();
    private final Stack<String> forwardHistory = new Stack<>();
    private final StringProperty currentSection = new SimpleStringProperty("general");
    private final Preferences prefs = Preferences.userNodeForPackage(HelpController.class);

    // Constants
    private static final String APP_VERSION = "1.3.5";
    private static final String LAST_UPDATED = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    private static final String PREFS_FONT_SIZE = "help_font_size";
    private static final String PREFS_DARK_MODE = "help_dark_mode";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize help content
        initializeHelpContent();

        // Set up preferences first to ensure dark mode is applied before content loads
        setupPreferences();

        // Set up the WebView with initial content
        setupWebView();

        // Set up search functionality
        setupSearch();

        // Set up navigation
        setupNavigation();

        // Set up accordion listeners
        setupAccordion();

        // Set up hyperlinks
        setupHyperlinks();

        // Set up metadata
        setupMetadata();

        // Add keyboard shortcuts
        setupKeyboardShortcuts();

        // Expand the general section by default
        if (generalSection != null) {
            Platform.runLater(() -> {
                helpAccordion.setExpandedPane(generalSection);

                // Add fade-in animation to main elements
                addFadeInAnimation(helpContentView, 300);
                addFadeInAnimation(helpAccordion, 500);
            });
        }

        // Load initial content
        loadHelpContent("general");
    }

    /**
     * Add fade-in animation to a node
     */
    private void addFadeInAnimation(Node node, int delayMs) {
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(600), node);
        fadeTransition.setFromValue(0.0);
        fadeTransition.setToValue(1.0);
        fadeTransition.setDelay(Duration.millis(delayMs));

        Platform.runLater(() -> {
            node.setOpacity(0);
            fadeTransition.play();
        });
    }

    /**
     * Set up keyboard shortcuts
     */
    private void setupKeyboardShortcuts() {
        // Add keyboard event handler to the scene
        Platform.runLater(() -> {
            if (rootPane.getScene() != null) {
                rootPane.getScene().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                    // Ctrl+F for search
                    if (event.isControlDown() && event.getCode() == KeyCode.F) {
                        mainTabPane.getSelectionModel().select(searchTab);
                        searchField.requestFocus();
                        event.consume();
                    }

                    // F1 for home
                    if (event.getCode() == KeyCode.F1) {
                        navigateToSection("general");
                        event.consume();
                    }

                    // Alt+Left for back
                    if (event.isAltDown() && event.getCode() == KeyCode.LEFT) {
                        if (!navigationHistory.isEmpty()) {
                            navigateBack();
                        }
                        event.consume();
                    }

                    // Alt+Right for forward
                    if (event.isAltDown() && event.getCode() == KeyCode.RIGHT) {
                        if (!forwardHistory.isEmpty()) {
                            navigateForward();
                        }
                        event.consume();
                    }

                    // Ctrl+P for print
                    if (event.isControlDown() && event.getCode() == KeyCode.P) {
                        printCurrentContent();
                        event.consume();
                    }

                    // Escape to close
                    if (event.getCode() == KeyCode.ESCAPE) {
                        handleClose();
                        event.consume();
                    }
                });
            }
        });
    }

    /**
     * Initialize the help content map with all available sections
     */
    private void initializeHelpContent() {
        // General section
        helpContent.put("general", getGeneralContent());
        helpContent.put("quickstart", getQuickStartContent());
        helpContent.put("interface", getInterfaceContent());

        // Candidats section - IMPROVED
        helpContent.put("candidats_add", getCandidatsAddContent());
        helpContent.put("candidats_edit", getCandidatsEditContent());
        helpContent.put("candidats_search", getCandidatsSearchContent());
        helpContent.put("candidats_documents", getCandidatsDocumentsContent());
        helpContent.put("candidats_dashboard", getCandidatsDashboardContent());

        // Moniteurs section
        helpContent.put("moniteurs_add", getMoniteursAddContent());
        helpContent.put("moniteurs_edit", getMoniteursEditContent());
        helpContent.put("moniteurs_qualifications", getMoniteursQualificationsContent());
        helpContent.put("moniteurs_dashboard", getMoniteursDashboardContent());

        // Vehicules section
        helpContent.put("vehicules_add", getVehiculesAddContent());
        helpContent.put("vehicules_maintenance", getVehiculesMaintenanceContent());
        helpContent.put("vehicules_insurance", getVehiculesInsuranceContent());

        // Examens section - IMPROVED
        helpContent.put("examens_schedule", getExamensScheduleContent());
        helpContent.put("examens_results", getExamensResultsContent());
        helpContent.put("examens_statistics", getExamensStatisticsContent());
        helpContent.put("examens_dashboard", getExamensDashboardContent());

        // Paiements section
        helpContent.put("paiements_payments", getPaiementsPaymentsContent());
        helpContent.put("paiements_invoices", getPaiementsInvoicesContent());
        helpContent.put("paiements_reports", getPaiementsReportsContent());

        // Settings section
        helpContent.put("settings_general", getSettingsGeneralContent());
        helpContent.put("settings_users", getSettingsUsersContent());
        helpContent.put("settings_backup", getSettingsBackupContent());

        // Keyboard shortcuts section - NEW
        helpContent.put("keyboard_shortcuts", getKeyboardShortcutsContent());
    }

    /**
     * Set up the WebView with initial configuration
     */
    private void setupWebView() {
        WebEngine engine = helpContentView.getEngine();

        // Handle link clicks within the WebView
        engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                Document doc = engine.getDocument();
                NodeList links = doc.getElementsByTagName("a");

                for (int i = 0; i < links.getLength(); i++) {
                    Element link = (Element) links.item(i);
                    ((EventTarget) link).addEventListener("click", evt -> {
                        evt.preventDefault();
                        String href = link.getAttribute("href");
                        if (href.startsWith("#")) {
                            // Internal link - navigate to section
                            String sectionId = href.substring(1);
                            navigateToSection(sectionId);
                        } else if (href.startsWith("http")) {
                            // External link - open in browser
                            openExternalLink(href);
                        }
                    }, false);
                }

                // Apply dark mode to WebView content if enabled
                if (darkModeToggle.isSelected()) {
                    engine.executeScript(
                            "document.body.classList.add('dark-mode');"
                    );
                }

                // Apply font size to WebView content
                applyFontSize(fontSizeComboBox.getValue());
            }
        });

        // Enable JavaScript for interactive elements
        engine.setJavaScriptEnabled(true);

        // Add context menu for right-click
        Platform.runLater(() -> {
            helpContentView.setContextMenuEnabled(false);

            ContextMenu contextMenu = new ContextMenu();
            MenuItem backItem = new MenuItem("Précédent");
            backItem.setOnAction(e -> navigateBack());
            backItem.disableProperty().bind(backButton.disabledProperty());

            MenuItem forwardItem = new MenuItem("Suivant");
            forwardItem.setOnAction(e -> navigateForward());
            forwardItem.disableProperty().bind(forwardButton.disabledProperty());

            MenuItem homeItem = new MenuItem("Accueil");
            homeItem.setOnAction(e -> navigateToSection("general"));

            MenuItem printItem = new MenuItem("Imprimer");
            printItem.setOnAction(e -> printCurrentContent());

            contextMenu.getItems().addAll(backItem, forwardItem, homeItem, new SeparatorMenuItem(), printItem);

            helpContentView.setOnContextMenuRequested(e -> {
                contextMenu.show(helpContentView, e.getScreenX(), e.getScreenY());
            });
        });
    }

    /**
     * Set up search functionality
     */
    private void setupSearch() {
        // Set up search button action
        searchButton.setOnAction(event -> performSearch());

        // Set up search field enter key action
        searchField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                performSearch();
            }
        });

        // Set up search results list selection
        searchResultsList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.startsWith("Aucun résultat")) {
                String sectionId = extractSectionIdFromResult(newVal);
                navigateToSection(sectionId);
                mainTabPane.getSelectionModel().select(contentTab);
            }
        });
    }

    /**
     * Extract section ID from search result text
     */
    private String extractSectionIdFromResult(String result) {
        // Format: "Section Title (Category)"
        try {
            String title = result.substring(0, result.lastIndexOf(" ("));
            String category = result.substring(result.lastIndexOf("(") + 1, result.lastIndexOf(")"));

            // Map the title and category to a section ID
            String sectionId = "";

            if (category.equalsIgnoreCase("Général")) {
                if (title.contains("Introduction")) sectionId = "general";
                else if (title.contains("Démarrage")) sectionId = "quickstart";
                else if (title.contains("Interface")) sectionId = "interface";
                else if (title.contains("Raccourcis")) sectionId = "keyboard_shortcuts";
                else sectionId = "general";
            } else if (category.equalsIgnoreCase("Candidats")) {
                if (title.contains("Ajouter")) sectionId = "candidats_add";
                else if (title.contains("Modifier")) sectionId = "candidats_edit";
                else if (title.contains("Rechercher")) sectionId = "candidats_search";
                else if (title.contains("Documents")) sectionId = "candidats_documents";
                else if (title.contains("Tableau de bord")) sectionId = "candidats_dashboard";
                else sectionId = "candidats_add";
            } else if (category.equalsIgnoreCase("Moniteurs")) {
                if (title.contains("Ajouter")) sectionId = "moniteurs_add";
                else if (title.contains("Modifier")) sectionId = "moniteurs_edit";
                else if (title.contains("Qualifications")) sectionId = "moniteurs_qualifications";
                else if (title.contains("Tableau de bord")) sectionId = "moniteurs_dashboard";
                else sectionId = "moniteurs_add";
            } else if (category.equalsIgnoreCase("Véhicules")) {
                if (title.contains("Ajouter")) sectionId = "vehicules_add";
                else if (title.contains("Maintenance")) sectionId = "vehicules_maintenance";
                else if (title.contains("Assurances")) sectionId = "vehicules_insurance";
                else sectionId = "vehicules_add";
            } else if (category.equalsIgnoreCase("Examens")) {
                if (title.contains("Planifier")) sectionId = "examens_schedule";
                else if (title.contains("Résultats")) sectionId = "examens_results";
                else if (title.contains("Statistiques")) sectionId = "examens_statistics";
                else if (title.contains("Tableau de bord")) sectionId = "examens_dashboard";
                else sectionId = "examens_schedule";
            } else if (category.equalsIgnoreCase("Paiements")) {
                if (title.contains("Enregistrer")) sectionId = "paiements_payments";
                else if (title.contains("Factures")) sectionId = "paiements_invoices";
                else if (title.contains("Rapports")) sectionId = "paiements_reports";
                else sectionId = "paiements_payments";
            } else if (category.equalsIgnoreCase("Paramètres")) {
                if (title.contains("généraux")) sectionId = "settings_general";
                else if (title.contains("Utilisateurs")) sectionId = "settings_users";
                else if (title.contains("Sauvegarde")) sectionId = "settings_backup";
                else sectionId = "settings_general";
            } else {
                sectionId = "general";
            }

            return sectionId;
        } catch (Exception e) {
            // In case of any error, return the general section
            return "general";
        }
    }

    /**
     * Perform search based on the search field text
     */
    private void performSearch() {
        String query = searchField.getText().trim().toLowerCase();
        if (query.isEmpty()) {
            return;
        }

        // Clear previous results
        searchResultsList.getItems().clear();

        // Create a list to store results for sorting
        List<String> results = new ArrayList<>();

        // Search through all help content
        for (Map.Entry<String, String> entry : helpContent.entrySet()) {
            String sectionId = entry.getKey();
            String content = entry.getValue();

            // Check if the content contains the search query
            if (content.toLowerCase().contains(query)) {
                // Add to search results with a user-friendly format
                String sectionTitle = getSectionTitle(sectionId);
                String category = getCategoryFromSectionId(sectionId);
                results.add(sectionTitle + " (" + category + ")");
            }
        }

        // Sort results alphabetically
        Collections.sort(results);
        searchResultsList.getItems().addAll(results);

        // Switch to search tab to show results
        mainTabPane.getSelectionModel().select(searchTab);

        // Show message if no results found
        if (searchResultsList.getItems().isEmpty()) {
            searchResultsList.getItems().add("Aucun résultat trouvé pour \"" + query + "\"");
        }

        // Show a toast notification with the number of results
        if (!results.isEmpty()) {
            showToast(results.size() + " résultat(s) trouvé(s) pour \"" + query + "\"");
        }
    }

    /**
     * Show a toast notification
     */
    private void showToast(String message) {
        Platform.runLater(() -> {
            StackPane toastPane = new StackPane();
            toastPane.setStyle("-fx-background-color: #1a365d; -fx-background-radius: 5; -fx-padding: 10 15;");

            Label toastLabel = new Label(message);
            toastLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");

            toastPane.getChildren().add(toastLabel);
            toastPane.setOpacity(0);
            toastPane.setTranslateY(50);

            // Add to the root pane
            rootPane.getChildren().add(toastPane);
            StackPane.setAlignment(toastPane, Pos.BOTTOM_CENTER);
            StackPane.setMargin(toastPane, new javafx.geometry.Insets(0, 0, 20, 0));

            // Animate in
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), toastPane);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.play();

            // Animate out after delay
            Timeline timeline = new Timeline(
                    new KeyFrame(Duration.millis(2500), event -> {
                        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), toastPane);
                        fadeOut.setFromValue(1);
                        fadeOut.setToValue(0);
                        fadeOut.setOnFinished(e -> rootPane.getChildren().remove(toastPane));
                        fadeOut.play();
                    })
            );
            timeline.play();
        });
    }

    /**
     * Get the category name from a section ID
     */
    private String getCategoryFromSectionId(String sectionId) {
        if (sectionId.startsWith("general") || sectionId.equals("keyboard_shortcuts")) return "Général";
        if (sectionId.startsWith("candidats")) return "Candidats";
        if (sectionId.startsWith("moniteurs")) return "Moniteurs";
        if (sectionId.startsWith("vehicules")) return "Véhicules";
        if (sectionId.startsWith("examens")) return "Examens";
        if (sectionId.startsWith("paiements")) return "Paiements";
        if (sectionId.startsWith("settings")) return "Paramètres";
        return "Général";
    }

    /**
     * Set up navigation controls
     */
    private void setupNavigation() {
        // Back button
        backButton.setOnAction(event -> navigateBack());
        backButton.setDisable(true);

        // Forward button
        forwardButton.setOnAction(event -> navigateForward());
        forwardButton.setDisable(true);

        // Home button
        homeButton.setOnAction(event -> {
            navigateToSection("general");
        });

        // Print button
        printButton.setOnAction(event -> printCurrentContent());
    }

    /**
     * Navigate to the previous section in history
     */
    private void navigateBack() {
        if (!navigationHistory.isEmpty()) {
            // Save current section to forward history
            forwardHistory.push(currentSection.get());

            // Navigate to previous section
            String previousSection = navigationHistory.pop();
            loadHelpContent(previousSection, false);

            // Update navigation buttons
            updateNavigationButtons();
        }
    }

    /**
     * Navigate to the next section in forward history
     */
    private void navigateForward() {
        if (!forwardHistory.isEmpty()) {
            // Save current section to history
            navigationHistory.push(currentSection.get());

            // Navigate to next section
            String nextSection = forwardHistory.pop();
            loadHelpContent(nextSection, false);

            // Update navigation buttons
            updateNavigationButtons();
        }
    }

    /**
     * Update the state of navigation buttons
     */
    private void updateNavigationButtons() {
        backButton.setDisable(navigationHistory.isEmpty());
        forwardButton.setDisable(forwardHistory.isEmpty());
    }

    /**
     * Print the current help content
     */
    private void printCurrentContent() {
        try {
            // Get the content from WebView
            WebEngine engine = helpContentView.getEngine();
            String content = (String) engine.executeScript("document.documentElement.outerHTML");

            // Create a temporary HTML file
            java.io.File tempFile = java.io.File.createTempFile("help_print_", ".html");
            try (java.io.FileWriter writer = new java.io.FileWriter(tempFile)) {
                // Add print-specific CSS
                String printCSS = "<style>@media print { " +
                        "body { font-family: 'Segoe UI', Arial, sans-serif; color: black; background-color: white; } " +
                        "h1 { color: black; } " +
                        "h2, h3 { color: black; } " +
                        "a { color: black; text-decoration: none; } " +
                        "}</style>";
                content = content.replace("</head>", printCSS + "</head>");

                // Add title and metadata
                String title = getSectionTitle(currentSection.get());
                String category = getCategoryFromSectionId(currentSection.get());
                content = content.replace("<title></title>", "<title>" + title + " - " + category + " - Aide Auto-École</title>");

                writer.write(content);
            }

            // Open the file in the default browser for printing
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(tempFile.toURI());
                showToast("Préparation de l'impression...");
            } else {
                showAlert("Impression", "Veuillez ouvrir le fichier manuellement pour l'imprimer: " + tempFile.getAbsolutePath(), Alert.AlertType.INFORMATION);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'imprimer le contenu: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /**
     * Set up accordion listeners
     */
    private void setupAccordion() {
        if (helpAccordion != null) {
            for (TitledPane pane : helpAccordion.getPanes()) {
                pane.expandedProperty().addListener((obs, oldVal, newVal) -> {
                    if (newVal) {
                        String sectionId = getSectionIdFromPane(pane);
                        if (sectionId != null) {
                            loadHelpContent(sectionId);
                        }
                    }
                });
            }
        }
    }

    /**
     * Get section ID from a TitledPane
     */
    private String getSectionIdFromPane(TitledPane pane) {
        if (pane == generalSection) return "general";
        if (pane == candidatsSection) return "candidats_add";
        if (pane == moniteursSection) return "moniteurs_add";
        if (pane == vehiculesSection) return "vehicules_add";
        if (pane == examensSection) return "examens_schedule";
        if (pane == paiementsSection) return "paiements_payments";
        if (pane == settingsSection) return "settings_general";
        return null;
    }

    /**
     * Set up hyperlinks
     */
    private void setupHyperlinks() {
        // Contact link
        contactLink.setOnAction(event -> {
            try {
                Desktop.getDesktop().mail(new URI("mailto:support@auto-ecole.com"));
                showToast("Ouverture du client de messagerie...");
            } catch (IOException | URISyntaxException e) {
                e.printStackTrace();
                showAlert("Erreur", "Impossible d'ouvrir le client de messagerie", Alert.AlertType.ERROR);
            }
        });

        // Website link
        websiteLink.setOnAction(event -> {
            try {
                Desktop.getDesktop().browse(new URI("https://www.auto-ecole.com"));
                showToast("Ouverture du site web...");
            } catch (IOException | URISyntaxException e) {
                e.printStackTrace();
                showAlert("Erreur", "Impossible d'ouvrir le navigateur", Alert.AlertType.ERROR);
            }
        });
    }

    /**
     * Set up preferences (font size, dark mode)
     */
    private void setupPreferences() {
        // Font size preference
        fontSizeComboBox.getItems().addAll("Petit", "Normal", "Grand", "Très grand");
        String savedFontSize = prefs.get(PREFS_FONT_SIZE, "Normal");
        fontSizeComboBox.setValue(savedFontSize);

        fontSizeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                applyFontSize(newVal);
                prefs.put(PREFS_FONT_SIZE, newVal);
                showToast("Taille de texte: " + newVal);
            }
        });

        // Dark mode preference
        boolean darkMode = prefs.getBoolean(PREFS_DARK_MODE, false);
        darkModeToggle.setSelected(darkMode);

        darkModeToggle.selectedProperty().addListener((obs, oldVal, newVal) -> {
            applyDarkMode(newVal);
            prefs.putBoolean(PREFS_DARK_MODE, newVal);
            showToast(newVal ? "Mode sombre activé" : "Mode clair activé");
        });

        // Apply initial dark mode setting
        Platform.runLater(() -> {
            applyDarkMode(darkMode);
            // Apply initial font size
            applyFontSize(savedFontSize);
        });
    }

    /**
     * Apply font size to the WebView
     */
    private void applyFontSize(String size) {
        if (size == null) {
            size = "Normal"; // Default if null
        }

        String fontSize;
        switch (size) {
            case "Petit":
                fontSize = "90%";
                break;
            case "Grand":
                fontSize = "120%";
                break;
            case "Très grand":
                fontSize = "150%";
                break;
            default:
                fontSize = "100%";
                break;
        }

        WebEngine engine = helpContentView.getEngine();
        if (engine.getDocument() != null) {
            engine.executeScript(
                    "document.body.style.fontSize = '" + fontSize + "';"
            );
        }
    }

    /**
     * Apply dark mode to the WebView and UI
     */
    private void applyDarkMode(boolean darkMode) {
        // Apply to UI
        if (rootPane != null && rootPane.getScene() != null) {
            if (darkMode) {
                if (!rootPane.getScene().getRoot().getStyleClass().contains("dark")) {
                    rootPane.getScene().getRoot().getStyleClass().add("dark");
                }
            } else {
                rootPane.getScene().getRoot().getStyleClass().remove("dark");
            }
        }

        // Apply to WebView if it's already loaded
        WebEngine engine = helpContentView.getEngine();
        if (engine.getDocument() != null) {
            if (darkMode) {
                engine.executeScript(
                        "document.body.classList.add('dark-mode');"
                );
            } else {
                engine.executeScript(
                        "document.body.classList.remove('dark-mode');"
                );
            }
        }

        // Force refresh of accordion content to ensure dark mode is applied
        if (helpAccordion != null) {
            for (TitledPane pane : helpAccordion.getPanes()) {
                // Toggle expanded state to force refresh
                if (pane.isExpanded()) {
                    Platform.runLater(() -> {
                        pane.setExpanded(false);
                        Platform.runLater(() -> pane.setExpanded(true));
                    });
                }
            }
        }
    }

    /**
     * Set up metadata (version, last updated)
     */
    private void setupMetadata() {
        if (versionLabel != null) {
            versionLabel.setText("Version " + APP_VERSION);
        }

        if (lastUpdatedLabel != null) {
            lastUpdatedLabel.setText("Dernière mise à jour: " + LAST_UPDATED);
        }
    }

    /**
     * Navigate to a specific section
     */
    void navigateToSection(String sectionId) {
        // Save current section to history
        String currentSectionId = currentSection.get();
        if (!currentSectionId.equals(sectionId)) {
            navigationHistory.push(currentSectionId);
            forwardHistory.clear();
        }

        // Load the new section
        loadHelpContent(sectionId);

        // Update navigation buttons
        updateNavigationButtons();

        // Expand the corresponding accordion pane
        expandAccordionForSection(sectionId);
    }

    /**
     * Expand the accordion pane for a specific section
     */
    private void expandAccordionForSection(String sectionId) {
        TitledPane paneToExpand = null;

        if (sectionId.startsWith("general") || sectionId.equals("keyboard_shortcuts")) {
            paneToExpand = generalSection;
        } else if (sectionId.startsWith("candidats")) {
            paneToExpand = candidatsSection;
        } else if (sectionId.startsWith("moniteurs")) {
            paneToExpand = moniteursSection;
        } else if (sectionId.startsWith("vehicules")) {
            paneToExpand = vehiculesSection;
        } else if (sectionId.startsWith("examens")) {
            paneToExpand = examensSection;
        } else if (sectionId.startsWith("paiements")) {
            paneToExpand = paiementsSection;
        } else if (sectionId.startsWith("settings")) {
            paneToExpand = settingsSection;
        }

        if (paneToExpand != null && !paneToExpand.isExpanded()) {
            helpAccordion.setExpandedPane(paneToExpand);
        }
    }

    /**
     * Load help content into the WebView
     */
    private void loadHelpContent(String sectionId) {
        loadHelpContent(sectionId, true);
    }

    /**
     * Load help content into the WebView with option to update current section
     */
    private void loadHelpContent(String sectionId, boolean updateCurrentSection) {
        String htmlContent = helpContent.getOrDefault(sectionId, getNotFoundContent());
        helpContentView.getEngine().loadContent(htmlContent);

        if (updateCurrentSection) {
            currentSection.set(sectionId);
        }

        // Update breadcrumb
        updateBreadcrumb(sectionId);

        // Re-apply dark mode to ensure it persists across page loads
        if (darkModeToggle.isSelected()) {
            Platform.runLater(() -> {
                WebEngine engine = helpContentView.getEngine();
                engine.executeScript(
                        "document.body.classList.add('dark-mode');"
                );
            });
        }

        // Re-apply font size to ensure it persists across page loads
        Platform.runLater(() -> {
            applyFontSize(fontSizeComboBox.getValue());
        });
    }

    /**
     * Update breadcrumb based on current section
     */
    private void updateBreadcrumb(String sectionId) {
        String category = getCategoryFromSectionId(sectionId);
        String sectionTitle = getSectionTitle(sectionId);

        if (breadcrumbLabel != null) {
            breadcrumbLabel.setText("Aide > " + category + " > " + sectionTitle);
        }
    }

    /**
     * Get the title for a section ID
     */
    private String getSectionTitle(String sectionId) {
        switch (sectionId) {
            // General
            case "general": return "Introduction";
            case "quickstart": return "Démarrage rapide";
            case "interface": return "Interface utilisateur";
            case "keyboard_shortcuts": return "Raccourcis clavier";

            // Candidats
            case "candidats_add": return "Ajouter un candidat";
            case "candidats_edit": return "Modifier un candidat";
            case "candidats_search": return "Rechercher des candidats";
            case "candidats_documents": return "Documents candidats";
            case "candidats_dashboard": return "Tableau de bord candidats";

            // Moniteurs
            case "moniteurs_add": return "Ajouter un moniteur";
            case "moniteurs_edit": return "Modifier un moniteur";
            case "moniteurs_qualifications": return "Qualifications";
            case "moniteurs_dashboard": return "Tableau de bord moniteurs";

            // Vehicules
            case "vehicules_add": return "Ajouter un véhicule";
            case "vehicules_maintenance": return "Maintenance";
            case "vehicules_insurance": return "Assurances";

            // Examens
            case "examens_schedule": return "Planifier un examen";
            case "examens_results": return "Résultats d'examen";
            case "examens_statistics": return "Statistiques";
            case "examens_dashboard": return "Tableau de bord examens";

            // Paiements
            case "paiements_payments": return "Enregistrer un paiement";
            case "paiements_invoices": return "Factures et reçus";
            case "paiements_reports": return "Rapports financiers";

            // Settings
            case "settings_general": return "Paramètres généraux";
            case "settings_users": return "Utilisateurs et permissions";
            case "settings_backup": return "Sauvegarde et restauration";

            default: return "Section inconnue";
        }
    }

    /**
     * Open an external link in the default browser
     */
    private void openExternalLink(String url) {
        try {
            Desktop.getDesktop().browse(new URI(url));
            showToast("Ouverture du lien externe...");
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir le lien: " + url, Alert.AlertType.ERROR);
        }
    }

    /**
     * Show an alert dialog
     */
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        // Apply styling to the alert
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("/Styles/Help.css").toExternalForm());

        // Apply dark mode if active
        if (darkModeToggle.isSelected()) {
            dialogPane.getStyleClass().add("dark");
        }

        alert.showAndWait();
    }

    /**
     * Close the help window
     */
    @FXML
    private void handleClose() {
        // Add a fade-out animation
        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), rootPane);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(event -> {
            Stage stage = (Stage) rootPane.getScene().getWindow();
            stage.close();
        });
        fadeOut.play();
    }

    /**
     * Event handlers for section links
     */
    @FXML
    public void handleGeneralLink(ActionEvent event) {
        Hyperlink link = (Hyperlink) event.getSource();
        String linkText = link.getText().toLowerCase();

        if (linkText.contains("introduction")) {
            navigateToSection("general");
        } else if (linkText.contains("démarrage")) {
            navigateToSection("quickstart");
        } else if (linkText.contains("interface")) {
            navigateToSection("interface");
        } else if (linkText.contains("raccourcis")) {
            navigateToSection("keyboard_shortcuts");
        }
    }

    @FXML
    public void handleCandidatsLink(ActionEvent event) {
        Hyperlink link = (Hyperlink) event.getSource();
        String linkText = link.getText().toLowerCase();

        if (linkText.contains("ajouter")) {
            navigateToSection("candidats_add");
        } else if (linkText.contains("modifier")) {
            navigateToSection("candidats_edit");
        } else if (linkText.contains("rechercher")) {
            navigateToSection("candidats_search");
        } else if (linkText.contains("documents")) {
            navigateToSection("candidats_documents");
        } else if (linkText.contains("tableau")) {
            navigateToSection("candidats_dashboard");
        }
    }

    @FXML
    public void handleMoniteursLink(ActionEvent event) {
        Hyperlink link = (Hyperlink) event.getSource();
        String linkText = link.getText().toLowerCase();

        if (linkText.contains("ajouter")) {
            navigateToSection("moniteurs_add");
        } else if (linkText.contains("modifier")) {
            navigateToSection("moniteurs_edit");
        } else if (linkText.contains("qualifications")) {
            navigateToSection("moniteurs_qualifications");
        } else if (linkText.contains("tableau")) {
            navigateToSection("moniteurs_dashboard");
        }
    }

    @FXML
    public void handleVehiculesLink(ActionEvent event) {
        Hyperlink link = (Hyperlink) event.getSource();
        String linkText = link.getText().toLowerCase();

        if (linkText.contains("ajouter")) {
            navigateToSection("vehicules_add");
        } else if (linkText.contains("maintenance")) {
            navigateToSection("vehicules_maintenance");
        } else if (linkText.contains("assurances")) {
            navigateToSection("vehicules_insurance");
        }
    }

    @FXML
    public void handleExamensLink(ActionEvent event) {
        Hyperlink link = (Hyperlink) event.getSource();
        String linkText = link.getText().toLowerCase();

        if (linkText.contains("planifier")) {
            navigateToSection("examens_schedule");
        } else if (linkText.contains("résultats")) {
            navigateToSection("examens_results");
        } else if (linkText.contains("statistiques")) {
            navigateToSection("examens_statistics");
        } else if (linkText.contains("tableau")) {
            navigateToSection("examens_dashboard");
        }
    }

    @FXML
    public void handlePaiementsLink(ActionEvent event) {
        Hyperlink link = (Hyperlink) event.getSource();
        String linkText = link.getText().toLowerCase();

        if (linkText.contains("enregistrer")) {
            navigateToSection("paiements_payments");
        } else if (linkText.contains("factures")) {
            navigateToSection("paiements_invoices");
        } else if (linkText.contains("rapports")) {
            navigateToSection("paiements_reports");
        }
    }

    @FXML
    public void handleSettingsLink(ActionEvent event) {
        Hyperlink link = (Hyperlink) event.getSource();
        String linkText = link.getText().toLowerCase();

        if (linkText.contains("généraux")) {
            navigateToSection("settings_general");
        } else if (linkText.contains("utilisateurs")) {
            navigateToSection("settings_users");
        } else if (linkText.contains("sauvegarde")) {
            navigateToSection("settings_backup");
        }
    }

    /**
     * Get HTML content for the General section
     */
    private String getGeneralContent() {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head>");
        html.append("<style>");
        html.append(getCommonStyles());
        html.append("</style>");
        html.append("</head><body>");

        html.append("<h1>Bienvenue dans l'Aide</h1>");
        html.append("<p>Bienvenue dans le système de gestion Auto-École Pro. Ce système vous permet de gérer efficacement tous les aspects de votre auto-école, y compris les candidats, les moniteurs, les véhicules, les examens et les paiements.</p>");

        html.append("<div class='card'>");
        html.append("<h2>Fonctionnalités principales</h2>");
        html.append("<ul class='feature-list'>");
        html.append("<li><span class='icon check'></span>Gestion complète des candidats et moniteurs</li>");
        html.append("<li><span class='icon check'></span>Suivi des véhicules et maintenance</li>");
        html.append("<li><span class='icon check'></span>Planification et gestion des examens</li>");
        html.append("<li><span class='icon check'></span>Gestion financière et facturation</li>");
        html.append("<li><span class='icon check'></span>Tableaux de bord et statistiques</li>");
        html.append("</ul>");
        html.append("</div>");

        html.append("<div class='card'>");
        html.append("<h2>Comment utiliser cette aide</h2>");
        html.append("<ul>");
        html.append("<li>Utilisez le menu de gauche pour naviguer entre les différentes sections</li>");
        html.append("<li>Utilisez la barre de recherche pour trouver rapidement une information (Ctrl+F)</li>");
        html.append("<li>Utilisez le bouton d'impression pour imprimer une section (Ctrl+P)</li>");
        html.append("<li>Naviguez avec les flèches de navigation ou les raccourcis clavier (Alt+← et Alt+→)</li>");
        html.append("</ul>");
        html.append("</div>");

        html.append("<div class='info-box'>");
        html.append("<h3>Astuce</h3>");
        html.append("<p>Vous pouvez accéder à cette aide à tout moment en cliquant sur l'icône d'aide (?) dans la barre d'en-tête de l'application ou en appuyant sur F1.</p>");
        html.append("</div>");

        html.append("<div class='metadata-box'>");
        html.append("<h3>Version du système</h3>");
        html.append("<table class='metadata-table'>");
        html.append("<tr><td>Version de l'application:</td><td>" + APP_VERSION + "</td></tr>");
        html.append("<tr><td>Date de mise à jour:</td><td>" + LAST_UPDATED + "</td></tr>");
        html.append("<tr><td>Base de données:</td><td>MySQL 8.0</td></tr>");
        html.append("</table>");
        html.append("</div>");

        html.append("</body></html>");
        return html.toString();
    }

    /**
     * Get HTML content for the Quick Start section
     */
    private String getQuickStartContent() {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head>");
        html.append("<style>");
        html.append(getCommonStyles());
        html.append("</style>");
        html.append("</head><body>");

        html.append("<h1>Démarrage rapide</h1>");
        html.append("<p>Ce guide vous aidera à prendre en main rapidement le système de gestion Auto-École Pro. Suivez ces étapes pour commencer à utiliser l'application efficacement.</p>");

        html.append("<div class='step-card'>");
        html.append("<h2>Étape 1: Connexion au système</h2>");
        html.append("<p>Pour vous connecter au système:</p>");
        html.append("<ol>");
        html.append("<li>Lancez l'application Auto-École Pro</li>");
        html.append("<li>Entrez votre nom d'utilisateur et mot de passe</li>");
        html.append("<li>Cliquez sur le bouton \"Connexion\"</li>");
        html.append("</ol>");
        html.append("<div class='tip-box'>");
        html.append("<p>Si vous avez oublié votre mot de passe, cliquez sur \"Mot de passe oublié\" et suivez les instructions.</p>");
        html.append("</div>");
        html.append("</div>");

        html.append("<div class='step-card'>");
        html.append("<h2>Étape 2: Navigation dans l'interface</h2>");
        html.append("<p>L'interface principale est divisée en plusieurs sections:</p>");
        html.append("<ul class='feature-list'>");
        html.append("<li><span class='icon arrow'></span><strong>Menu principal:</strong> Situé à gauche, permet d'accéder aux différents modules</li>");
        html.append("<li><span class='icon arrow'></span><strong>Barre d'outils:</strong> En haut, contient les actions rapides et la recherche</li>");
        html.append("<li><span class='icon arrow'></span><strong>Espace de travail:</strong> Au centre, affiche le contenu du module sélectionné</li>");
        html.append("<li><span class='icon arrow'></span><strong>Barre d'état:</strong> En bas, affiche des informations sur l'utilisateur et le système</li>");
        html.append("</ul>");
        html.append("</div>");

        html.append("<div class='step-card'>");
        html.append("<h2>Étape 3: Premiers pas</h2>");
        html.append("<p>Voici quelques actions recommandées pour débuter:</p>");

        html.append("<div class='card-grid'>");
        html.append("<div class='mini-card'>");
        html.append("<h3>Ajouter des candidats</h3>");
        html.append("<p>Commencez par ajouter vos candidats dans le système pour pouvoir suivre leur progression.</p>");
        html.append("<a href='#candidats_add' class='button'>Voir la section Candidats</a>");
        html.append("</div>");

        html.append("<div class='mini-card'>");
        html.append("<h3>Configurer les moniteurs</h3>");
        html.append("<p>Ajoutez les moniteurs et leurs qualifications pour pouvoir les assigner aux candidats.</p>");
        html.append("<a href='#moniteurs_add' class='button'>Voir la section Moniteurs</a>");
        html.append("</div>");
        html.append("</div>");
        html.append("</div>");

        html.append("<div class='warning-box'>");
        html.append("<h3>Important</h3>");
        html.append("<p>N'oubliez pas de sauvegarder régulièrement vos données en utilisant la fonction de sauvegarde dans les paramètres du système.</p>");
        html.append("</div>");

        html.append("</body></html>");
        return html.toString();
    }

    /**
     * Get HTML content for the Interface section
     */
    private String getInterfaceContent() {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head>");
        html.append("<style>");
        html.append(getCommonStyles());
        html.append("</style>");
        html.append("</head><body>");

        html.append("<h1>Interface utilisateur</h1>");
        html.append("<p>L'interface utilisateur du système Auto-École Pro est conçue pour être intuitive et efficace. Cette section vous présente les différents éléments de l'interface et leur utilisation.</p>");

        html.append("<div class='section-card'>");
        html.append("<h2>Structure générale</h2>");
        html.append("<div class='grid-container'>");

        html.append("<div class='grid-item'>");
        html.append("<h3>Menu principal</h3>");
        html.append("<ul>");
        html.append("<li>Tableau de bord</li>");
        html.append("<li>Gestion des candidats</li>");
        html.append("<li>Gestion des moniteurs</li>");
        html.append("<li>Gestion des véhicules</li>");
        html.append("<li>Gestion des examens</li>");
        html.append("<li>Gestion des paiements</li>");
        html.append("<li>Paramètres</li>");
        html.append("</ul>");
        html.append("</div>");

        html.append("<div class='grid-item'>");
        html.append("<h3>Barre d'outils</h3>");
        html.append("<ul>");
        html.append("<li>Recherche globale</li>");
        html.append("<li>Notifications</li>");
        html.append("<li>Profil utilisateur</li>");
        html.append("<li>Aide</li>");
        html.append("<li>Déconnexion</li>");
        html.append("</ul>");
        html.append("</div>");

        html.append("<div class='grid-item'>");
        html.append("<h3>Espace de travail</h3>");
        html.append("<ul>");
        html.append("<li>Tableaux de données</li>");
        html.append("<li>Formulaires</li>");
        html.append("<li>Filtres et recherche</li>");
        html.append("<li>Actions contextuelles</li>");
        html.append("<li>Visualisations</li>");
        html.append("</ul>");
        html.append("</div>");

        html.append("</div>");
        html.append("</div>");

        html.append("<div class='section-card'>");
        html.append("<h2>Éléments d'interface communs</h2>");
        html.append("<div class='grid-container'>");

        html.append("<div class='grid-item'>");
        html.append("<h3>Tableaux de données</h3>");
        html.append("<p>Les tableaux permettent d'afficher et de manipuler des listes d'éléments:</p>");
        html.append("<ul>");
        html.append("<li>Cliquez sur un en-tête de colonne pour trier les données</li>");
        html.append("<li>Double-cliquez sur une ligne pour ouvrir les détails</li>");
        html.append("<li>Utilisez la barre de recherche pour filtrer les résultats</li>");
        html.append("</ul>");
        html.append("</div>");

        html.append("<div class='grid-item'>");
        html.append("<h3>Formulaires</h3>");
        html.append("<p>Les formulaires permettent d'ajouter ou de modifier des données:</p>");
        html.append("<ul>");
        html.append("<li>Les champs obligatoires sont marqués d'un astérisque (*)</li>");
        html.append("<li>Utilisez Tab pour naviguer entre les champs</li>");
        html.append("<li>Appuyez sur Entrée pour soumettre le formulaire</li>");
        html.append("</ul>");
        html.append("</div>");

        html.append("<div class='grid-item'>");
        html.append("<h3>Tableaux de bord</h3>");
        html.append("<p>Les tableaux de bord offrent une vue d'ensemble avec des statistiques et graphiques:</p>");
        html.append("<ul>");
        html.append("<li>Utilisez les filtres pour affiner les données</li>");
        html.append("<li>Survolez les graphiques pour voir les détails</li>");
        html.append("<li>Utilisez le bouton d'actualisation pour mettre à jour les données</li>");
        html.append("</ul>");
        html.append("</div>");

        html.append("</div>");
        html.append("</div>");

        html.append("<div class='success-box'>");
        html.append("<h3>Conseil</h3>");
        html.append("<p>Vous pouvez personnaliser certains aspects de l'interface, comme le thème et la taille des caractères, dans les paramètres du système.</p>");
        html.append("</div>");

        html.append("<div class='info-box'>");
        html.append("<h3>Raccourcis clavier</h3>");
        html.append("<p>Pour une liste complète des raccourcis clavier disponibles, consultez la section <a href='#keyboard_shortcuts'>Raccourcis clavier</a>.</p>");
        html.append("</div>");

        html.append("</body></html>");
        return html.toString();
    }

    /**
     * Get HTML content for the Keyboard Shortcuts section
     */
    private String getKeyboardShortcutsContent() {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head>");
        html.append("<style>");
        html.append(getCommonStyles());
        html.append("</style>");
        html.append("</head><body>");

        html.append("<h1>Raccourcis clavier</h1>");
        html.append("<p>Cette section présente tous les raccourcis clavier disponibles dans l'application Auto-École Pro. L'utilisation de ces raccourcis peut considérablement améliorer votre productivité.</p>");

        html.append("<div class='section-card'>");
        html.append("<h2>Raccourcis globaux</h2>");
        html.append("<p>Ces raccourcis fonctionnent dans toute l'application:</p>");
        html.append("<table class='shortcut-table'>");
        html.append("<tr><th>Raccourci</th><th>Action</th></tr>");
        html.append("<tr><td><span class='badge'>F1</span></td><td>Ouvrir l'aide</td></tr>");
        html.append("<tr><td><span class='badge'>Ctrl</span> + <span class='badge'>F</span></td><td>Recherche</td></tr>");
        html.append("<tr><td><span class='badge'>Ctrl</span> + <span class='badge'>P</span></td><td>Imprimer</td></tr>");
        html.append("<tr><td><span class='badge'>Ctrl</span> + <span class='badge'>S</span></td><td>Enregistrer</td></tr>");
        html.append("<tr><td><span class='badge'>Ctrl</span> + <span class='badge'>N</span></td><td>Nouveau</td></tr>");
        html.append("<tr><td><span class='badge'>Ctrl</span> + <span class='badge'>D</span></td><td>Afficher/Masquer le tableau de bord</td></tr>");
        html.append("<tr><td><span class='badge'>Ctrl</span> + <span class='badge'>R</span></td><td>Actualiser</td></tr>");
        html.append("<tr><td><span class='badge'>Alt</span> + <span class='badge'>B</span></td><td>Retour à l'écran principal</td></tr>");
        html.append("<tr><td><span class='badge'>Échap</span></td><td>Annuler/Fermer</td></tr>");
        html.append("</table>");
        html.append("</div>");

        html.append("<div class='section-card'>");
        html.append("<h2>Navigation</h2>");
        html.append("<table class='shortcut-table'>");
        html.append("<tr><th>Raccourci</th><th>Action</th></tr>");
        html.append("<tr><td><span class='badge'>Alt</span> + <span class='badge'>C</span></td><td>Aller à la gestion des candidats</td></tr>");
        html.append("<tr><td><span class='badge'>Alt</span> + <span class='badge'>M</span></td><td>Aller à la gestion des moniteurs</td></tr>");
        html.append("<tr><td><span class='badge'>Alt</span> + <span class='badge'>V</span></td><td>Aller à la gestion des véhicules</td></tr>");
        html.append("<tr><td><span class='badge'>Alt</span> + <span class='badge'>E</span></td><td>Aller à la gestion des examens</td></tr>");
        html.append("<tr><td><span class='badge'>Alt</span> + <span class='badge'>P</span></td><td>Aller à la gestion des paiements</td></tr>");
        html.append("<tr><td><span class='badge'>Alt</span> + <span class='badge'>S</span></td><td>Aller aux paramètres</td></tr>");
        html.append("<tr><td><span class='badge'>Alt</span> + <span class='badge'>D</span></td><td>Aller au tableau de bord</td></tr>");
        html.append("</table>");
        html.append("</div>");

        html.append("<div class='section-card'>");
        html.append("<h2>Gestion des candidats</h2>");
        html.append("<table class='shortcut-table'>");
        html.append("<tr><th>Raccourci</th><th>Action</th></tr>");
        html.append("<tr><td><span class='badge'>Ctrl</span> + <span class='badge'>N</span></td><td>Ajouter un candidat</td></tr>");
        html.append("<tr><td><span class='badge'>Ctrl</span> + <span class='badge'>E</span></td><td>Modifier le candidat sélectionné</td></tr>");
        html.append("<tr><td><span class='badge'>Ctrl</span> + <span class='badge'>F</span></td><td>Rechercher un candidat</td></tr>");
        html.append("<tr><td><span class='badge'>Ctrl</span> + <span class='badge'>G</span></td><td>Générer la fiche du candidat</td></tr>");
        html.append("<tr><td><span class='badge'>Ctrl</span> + <span class='badge'>I</span></td><td>Importer des candidats</td></tr>");
        html.append("<tr><td><span class='badge'>Ctrl</span> + <span class='badge'>X</span></td><td>Exporter la liste des candidats</td></tr>");
        html.append("</table>");
        html.append("</div>");

        html.append("<div class='section-card'>");
        html.append("<h2>Gestion des examens</h2>");
        html.append("<table class='shortcut-table'>");
        html.append("<tr><th>Raccourci</th><th>Action</th></tr>");
        html.append("<tr><td><span class='badge'>Ctrl</span> + <span class='badge'>N</span></td><td>Planifier un nouvel examen</td></tr>");
        html.append("<tr><td><span class='badge'>Ctrl</span> + <span class='badge'>E</span></td><td>Modifier l'examen sélectionné</td></tr>");
        html.append("<tr><td><span class='badge'>Ctrl</span> + <span class='badge'>F</span></td><td>Rechercher un examen</td></tr>");
        html.append("<tr><td><span class='badge'>Ctrl</span> + <span class='badge'>V</span></td><td>Valider un examen</td></tr>");
        html.append("<tr><td><span class='badge'>Ctrl</span> + <span class='badge'>R</span></td><td>Actualiser la liste des examens</td></tr>");
        html.append("</table>");
        html.append("</div>");

        html.append("<div class='tip-box'>");
        html.append("<h3>Astuce</h3>");
        html.append("<p>Vous pouvez voir les raccourcis disponibles dans chaque écran en survolant les boutons avec la souris. Une infobulle apparaîtra avec le raccourci correspondant.</p> ");
        html.append("</div>");

        html.append("<div class='success-box'>");
        html.append("<h3>Personnalisation</h3>");
        html.append("<p>Vous pouvez personnaliser certains raccourcis clavier dans les paramètres de l'application.</p>");
        html.append("</div>");

        html.append("</body></html>");
        return html.toString();
    }

    /**
     * Get HTML content for the Candidats Add section - IMPROVED
     */
    private String getCandidatsAddContent() {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head>");
        html.append("<style>");
        html.append(getCommonStyles());
        html.append("</style>");
        html.append("</head><body>");

        html.append("<h1>Ajouter un candidat</h1>");
        html.append("<p>Cette section explique comment ajouter un nouveau candidat dans le système. Suivez ces étapes pour enregistrer correctement toutes les informations nécessaires.</p>");

        html.append("<div class='step-card'>");
        html.append("<h2>Étape 1: Accéder au formulaire d'ajout</h2>");
        html.append("<p>Pour accéder au formulaire d'ajout d'un candidat:</p>");
        html.append("<ol>");
        html.append("<li>Cliquez sur \"Gestion des Candidats\" dans le menu principal</li>");
        html.append("<li>Cliquez sur le bouton \"Ajouter\" dans la barre d'outils</li>");
        html.append("</ol>");
        html.append("<p class='note'>Vous pouvez également utiliser le raccourci clavier <span class='badge'>Ctrl</span> + <span class='badge'>N</span> lorsque vous êtes dans la section Candidats.</p>");
        html.append("</div>");

        html.append("<div class='step-card'>");
        html.append("<h2>Étape 2: Remplir les informations personnelles</h2>");
        html.append("<p>Remplissez les champs suivants avec les informations du candidat:</p>");

        html.append("<div class='grid-container'>");
        html.append("<div class='grid-item'>");
        html.append("<h3>Informations obligatoires</h3>");
        html.append("<ul>");
        html.append("<li><strong>Nom</strong> - Nom de famille du candidat</li>");
        html.append("<li><strong>Prénom</strong> - Prénom du candidat</li>");
        html.append("<li><strong>CIN</strong> - Carte d'Identité Nationale (format: 8 chiffres)</li>");
        html.append("<li><strong>Date de naissance</strong> - Utilisez le sélecteur de date</li>");
        html.append("<li><strong>Téléphone</strong> - Numéro de téléphone (format: 8 chiffres)</li>");
        html.append("<li><strong>Adresse</strong> - Adresse complète du candidat</li>");
        html.append("<li><strong>Email</strong> - Adresse email valide</li>");
        html.append("<li><strong>Catégorie(s) de permis</strong> - Sélectionnez au moins une catégorie</li>");
        html.append("</ul>");
        html.append("</div>");

        html.append("<div class='grid-item'>");
        html.append("<h3>Informations optionnelles</h3>");
        html.append("<ul>");
        html.append("<li><strong>Frais d'inscription</strong> - Montant payé à l'inscription</li>");
        html.append("<li><strong>Notes</strong> - Informations supplémentaires</li>");
        html.append("<li><strong>Photo d'identité</strong> - Photo du candidat</li>");
        html.append("<li><strong>Photo CIN</strong> - Scan de la carte d'identité</li>");
        html.append("<li><strong>Certificat médical</strong> - Scan du certificat médical</li>");
        html.append("</ul>");
        html.append("</div>");
        html.append("</div>");

        html.append("<div class='warning-box'>");
        html.append("<h3>Attention</h3>");
        html.append("<p>Les champs CIN, email et téléphone doivent être uniques pour chaque candidat et ne peuvent pas être utilisés par un moniteur.</p>");
        html.append("</div>");
        html.append("</div>");

        html.append("<div class='step-card'>");
        html.append("<h2>Étape 3: Ajouter des documents</h2>");
        html.append("<p>Vous pouvez ajouter des documents pour le candidat:</p>");
        html.append("<ol>");
        html.append("<li>Cliquez sur le bouton \"Sélectionner\" à côté du type de document</li>");
        html.append("<li>Choisissez le fichier à partir de votre ordinateur</li>");
        html.append("<li>Le document sera automatiquement associé au candidat</li>");
        html.append("</ol>");
        html.append("<p>Formats supportés: JPG, PNG, PDF</p>");
        html.append("<div class='info-box'>");
        html.append("<p>Vous pouvez visualiser les documents ajoutés en cliquant sur le bouton \"Voir\" à côté de chaque document.</p>");
        html.append("</div>");
        html.append("</div>");

        html.append("<div class='step-card'>");
        html.append("<h2>Étape 4: Enregistrer le candidat</h2>");
        html.append("<p>Une fois toutes les informations saisies:</p>");
        html.append("<ol>");
        html.append("<li>Vérifiez que tous les champs obligatoires sont remplis</li>");
        html.append("<li>Cliquez sur le bouton \"Enregistrer\" pour sauvegarder le candidat</li>");
        html.append("<li>Une confirmation s'affichera si l'enregistrement est réussi</li>");
        html.append("</ol>");
        html.append("<p>Vous pouvez également utiliser le raccourci clavier <span class='badge'>Ctrl</span> + <span class='badge'>S</span> pour enregistrer.</p>");
        html.append("</div>");

        html.append("<div class='success-box'>");
        html.append("<h3>Après l'ajout</h3>");
        html.append("<p>Après avoir ajouté un candidat, vous pouvez:</p>");
        html.append("<ul>");
        html.append("<li>Générer sa fiche d'inscription en PDF en cliquant sur \"Générer Fiche\"</li>");
        html.append("<li>Planifier un examen pour ce candidat</li>");
        html.append("<li>Enregistrer un paiement</li>");
        html.append("</ul>");
        html.append("</div>");

        html.append("</body></html>");
        return html.toString();
    }

    /**
     * Get HTML content for the Candidats Edit section - IMPROVED
     */
    private String getCandidatsEditContent() {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head>");
        html.append("<style>");
        html.append(getCommonStyles());
        html.append("</style>");
        html.append("</head><body>");

        html.append("<h1>Modifier un candidat</h1>");
        html.append("<p>Cette section explique comment modifier les informations d'un candidat existant. Suivez ces étapes pour mettre à jour correctement les données.</p>");

        html.append("<div class='step-card'>");
        html.append("<h2>Étape 1: Sélectionner un candidat</h2>");
        html.append("<p>Pour modifier un candidat existant:</p>");
        html.append("<ol>");
        html.append("<li>Accédez à la section \"Gestion des Candidats\" dans le menu principal</li>");
        html.append("<li>Recherchez le candidat à modifier en utilisant la barre de recherche ou les filtres</li>");
        html.append("<li>Sélectionnez le candidat dans la liste en cliquant sur sa ligne</li>");
        html.append("<li>Cliquez sur le bouton \"Modifier\" dans la barre d'outils</li>");
        html.append("</ol>");
        html.append("<div class='info-box'>");
        html.append("<h3>Astuce</h3>");
        html.append("<p>Vous pouvez également double-cliquer sur un candidat dans la liste pour ouvrir directement le formulaire de modification.</p>");
        html.append("<p>Ou utiliser le raccourci clavier <span class='badge'>Ctrl</span> + <span class='badge'>E</span> après avoir sélectionné un candidat.</p>");
        html.append("</div>");
        html.append("</div>");

        html.append("<div class='step-card'>");
        html.append("<h2>Étape 2: Modifier les informations</h2>");
        html.append("<p>Le formulaire de modification s'ouvre avec les informations actuelles du candidat:</p>");
        html.append("<ul>");
        html.append("<li>Modifiez les champs nécessaires</li>");
        html.append("<li>Les champs obligatoires sont marqués d'un astérisque (*)</li>");
        html.append("<li>Vous pouvez ajouter, modifier ou supprimer des documents</li>");
        html.append("<li>Vous pouvez changer le statut du candidat (actif/inactif)</li>");
        html.append("</ul>");

        html.append("<div class='warning-box'>");
        html.append("<h3>Attention</h3>");
        html.append("<p>La modification du CIN, de l'email ou du téléphone peut échouer si la nouvelle valeur est déjà utilisée par un autre candidat ou moniteur.</p>");
        html.append("</div>");
        html.append("</div>");

        html.append("<div class='step-card'>");
        html.append("<h2>Étape 3: Enregistrer les modifications</h2>");
        html.append("<p>Une fois les modifications effectuées:</p>");
        html.append("<ol>");
        html.append("<li>Vérifiez que tous les champs obligatoires sont correctement remplis</li>");
        html.append("<li>Cliquez sur le bouton \"Enregistrer\" pour sauvegarder les modifications</li>");
        html.append("<li>Une confirmation s'affichera si la mise à jour est réussie</li>");
        html.append("</ol>");
        html.append("<p>Vous pouvez également utiliser le raccourci clavier <span class='badge'>Ctrl</span> + <span class='badge'>S</span> pour enregistrer.</p>");

        html.append("<div class='info-box'>");
        html.append("<h3>Annulation</h3>");
        html.append("<p>Pour annuler les modifications et revenir à l'état précédent, cliquez sur le bouton \"Annuler\" ou utilisez la touche <span class='badge'>Échap</span>.</p>");
        html.append("</div>");
        html.append("</div>");

        html.append("<div class='step-card'>");
        html.append("<h2>Étape 4: Actions supplémentaires</h2>");
        html.append("<p>Après avoir modifié un candidat, vous pouvez effectuer d'autres actions:</p>");
        html.append("<div class='grid-container'>");

        html.append("<div class='grid-item'>");
        html.append("<h3>Gestion des documents</h3>");
        html.append("<ul>");
        html.append("<li>Générer une fiche PDF mise à jour</li>");
        html.append("<li>Imprimer les informations du candidat</li>");
        html.append("<li>Exporter les données au format CSV</li>");
        html.append("</ul>");
        html.append("</div>");

        html.append("<div class='grid-item'>");
        html.append("<h3>Actions liées</h3>");
        html.append("<ul>");
        html.append("<li>Planifier un examen pour ce candidat</li>");
        html.append("<li>Enregistrer un paiement</li>");
        html.append("<li>Consulter l'historique des examens</li>");
        html.append("</ul>");
        html.append("</div>");

        html.append("</div>");
        html.append("</div>");

        html.append("<div class='success-box'>");
        html.append("<h3>Historique des modifications</h3>");
        html.append("<p>Le système conserve un historique des modifications apportées aux candidats. Cet historique peut être consulté par les administrateurs dans la section Paramètres > Journaux d'activité.</p>");
        html.append("</div>");

        html.append("</body></html>");
        return html.toString();
    }

    /**
     * Get HTML content for the Candidats Search section - IMPROVED
     */
    private String getCandidatsSearchContent() {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head>");
        html.append("<style>");
        html.append(getCommonStyles());
        html.append("</style>");
        html.append("</head><body>");

        html.append("<h1>Rechercher des candidats</h1>");
        html.append("<p>Cette section explique comment rechercher et filtrer efficacement les candidats dans le système.</p>");

        html.append("<div class='section-card'>");
        html.append("<h2>Recherche rapide</h2>");
        html.append("<p>La recherche rapide vous permet de trouver un candidat en utilisant différents critères:</p>");
        html.append("<ol>");
        html.append("<li>Accédez à la section \"Gestion des Candidats\" dans le menu principal</li>");
        html.append("<li>Utilisez le champ de recherche en haut de la page</li>");
        html.append("<li>Saisissez l'un des critères suivants:</li>");
        html.append("</ol>");

        html.append("<div class='grid-container'>");
        html.append("<div class='grid-item'>");
        html.append("<h3>Critères de recherche</h3>");
        html.append("<ul>");
        html.append("<li><strong>Nom ou prénom</strong> - Recherche par nom ou prénom (partiel ou complet)</li>");
        html.append("<li><strong>CIN</strong> - Recherche par numéro de carte d'identité</li>");
        html.append("<li><strong>Téléphone</strong> - Recherche par numéro de téléphone</li>");
        html.append("<li><strong>Email</strong> - Recherche par adresse email</li>");
        html.append("</ul>");
        html.append("</div>");

        html.append("<div class='grid-item'>");
        html.append("<h3>Astuces de recherche</h3>");
        html.append("<ul>");
        html.append("<li>La recherche n'est pas sensible à la casse</li>");
        html.append("<li>Vous pouvez saisir une partie du nom ou prénom</li>");
        html.append("<li>Pour les CIN et téléphones, saisissez au moins 4 chiffres</li>");
        html.append("<li>Appuyez sur <span class='badge'>Entrée</span> ou cliquez sur l'icône de recherche pour lancer la recherche</li>");
        html.append("</ul>");
        html.append("</div>");
        html.append("</div>");

        html.append("<div class='info-box'>");
        html.append("<h3>Raccourci clavier</h3>");
        html.append("<p>Utilisez <span class='badge'>Ctrl</span> + <span class='badge'>F</span> pour placer le curseur directement dans le champ de recherche.</p>");
        html.append("</div>");
        html.append("</div>");

        html.append("<div class='section-card'>");
        html.append("<h2>Recherche avancée avec filtres</h2>");
        html.append("<p>Pour une recherche plus précise, utilisez les filtres avancés:</p>");

        html.append("<div class='grid-container'>");
        html.append("<div class='grid-item'>");
        html.append("<h3>Filtres disponibles</h3>");
        html.append("<ul>");
        html.append("<li><strong>Statut</strong> - Actif, Inactif ou Tous</li>");
        html.append("<li><strong>Catégorie de permis</strong> - A, B, C ou Toutes</li>");
        html.append("<li><strong>Date d'inscription</strong> - Sélectionnez une période</li>");
        html.append("<li><strong>Progression</strong> - En cours, Terminé, En attente d'examen</li>");
        html.append("</ul>");
        html.append("</div>");

        html.append("<div class='grid-item'>");
        html.append("<h3>Application des filtres</h3>");
        html.append("<ol>");
        html.append("<li>Sélectionnez les filtres souhaités</li>");
        html.append("<li>Cliquez sur \"Appliquer les filtres\"</li>");
        html.append("<li>Pour réinitialiser tous les filtres, cliquez sur \"Réinitialiser\"</li>");
        html.append("</ol>");
        html.append("</div>");
        html.append("</div>");

        html.append("<div class='tip-box'>");
        html.append("<h3>Combinaison de filtres</h3>");
        html.append("<p>Vous pouvez combiner la recherche textuelle avec les filtres pour des résultats encore plus précis. Par exemple, recherchez tous les candidats nommés \"Martin\" qui sont inscrits pour la catégorie B.</p>");
        html.append("</div>");
        html.append("</div>");

        html.append("<div class='section-card'>");
        html.append("<h2>Gestion des résultats de recherche</h2>");

        html.append("<div class='grid-container'>");
        html.append("<div class='grid-item'>");
        html.append("<h3>Actions sur les résultats</h3>");
        html.append("<ul>");
        html.append("<li><strong>Tri</strong> - Cliquez sur les en-têtes de colonnes pour trier les résultats</li>");
        html.append("<li><strong>Sélection</strong> - Cliquez sur une ligne pour sélectionner un candidat</li>");
        html.append("<li><strong>Détails</strong> - Double-cliquez sur un candidat pour voir ses détails</li>");
        html.append("<li><strong>Actions groupées</strong> - Sélectionnez plusieurs candidats pour des actions en masse</li>");
        html.append("</ul>");
        html.append("</div>");

        html.append("<div class='grid-item'>");
        html.append("<h3>Exportation des résultats</h3>");
        html.append("<p>Vous pouvez exporter les résultats de recherche dans différents formats:</p>");
        html.append("<ul>");
        html.append("<li><strong>CSV</strong> - Pour l'analyse dans Excel ou d'autres tableurs</li>");
        html.append("<li><strong>PDF</strong> - Pour l'impression ou l'archivage</li>");
        html.append("<li><strong>Liste par catégorie</strong> - Génère une liste filtrée par catégorie de permis</li>");
        html.append("</ul>");
        html.append("<p>Cliquez sur le bouton \"Exporter\" et choisissez le format souhaité.</p>");
        html.append("</div>");
        html.append("</div>");

        html.append("<div class='success-box'>");
        html.append("<h3>Recherches fréquentes</h3>");
        html.append("<p>Le système mémorise vos recherches récentes. Vous pouvez y accéder en cliquant sur l'icône d'historique à côté du champ de recherche.</p>");
        html.append("</div>");
        html.append("</div>");

        html.append("</body></html>");
        return html.toString();
    }

    /**
     * Get HTML content for the Candidats Documents section - IMPROVED
     */
    private String getCandidatsDocumentsContent() {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head>");
        html.append("<style>");
        html.append(getCommonStyles());
        html.append("</style>");
        html.append("</head><body>");

        html.append("<h1>Documents candidats</h1>");
        html.append("<p>Cette section explique comment gérer les documents associés aux candidats, y compris l'ajout, la visualisation et la génération de documents.</p>");

        html.append("<div class='section-card'>");
        html.append("<h2>Types de documents</h2>");
        html.append("<p>Le système gère plusieurs types de documents pour chaque candidat:</p>");

        html.append("<div class='grid-container'>");
        html.append("<div class='grid-item'>");
        html.append("<h3>Documents d'identité</h3>");
        html.append("<ul>");
        html.append("<li><strong>Photo d'identité</strong> - Photo du candidat pour les dossiers</li>");
        html.append("<li><strong>Photo CIN</strong> - Scan de la carte d'identité nationale</li>");
        html.append("<li><strong>Certificat médical</strong> - Document médical obligatoire</li>");
        html.append("</ul>");
        html.append("</div>");

        html.append("<div class='grid-item'>");
        html.append("<h3>Documents générés</h3>");
        html.append("<ul>");
        html.append("<li><strong>Fiche d'inscription</strong> - Résumé des informations du candidat</li>");
        html.append("<li><strong>Attestation de formation</strong> - Preuve de suivi des cours</li>");
        html.append("<li><strong>Reçus de paiement</strong> - Justificatifs des paiements effectués</li>");
        html.append("<li><strong>Résultats d'examens</strong> - Documents officiels des résultats</li>");
        html.append("</ul>");
        html.append("</div>");
        html.append("</div>");

        html.append("<div class='info-box'>");
        html.append("<h3>Formats supportés</h3>");
        html.append("<p>Le système accepte les formats suivants: JPG, PNG, PDF. La taille maximale par fichier est de 5 Mo.</p>");
        html.append("</div>");
        html.append("</div>");

        html.append("<div class='section-card'>");
        html.append("<h2>Ajouter des documents</h2>");
        html.append("<p>Pour ajouter un document à un candidat:</p>");
        html.append("<ol>");
        html.append("<li>Sélectionnez le candidat dans la liste</li>");
        html.append("<li>Cliquez sur \"Modifier\" pour accéder au formulaire</li>");
        html.append("<li>Dans la section Documents, cliquez sur le bouton \"Sélectionner\" à côté du type de document souhaité</li>");
        html.append("<li>Choisissez le fichier à partir de votre ordinateur</li>");
        html.append("<li>Le document sera automatiquement associé au candidat après l'enregistrement</li>");
        html.append("</ol>");

        html.append("<div class='tip-box'>");
        html.append("<h3>Astuce</h3>");
        html.append("<p>Vous pouvez également faire glisser-déposer les fichiers directement sur la zone de document correspondante.</p>");
        html.append("</div>");
        html.append("</div>");

        html.append("<div class='section-card'>");
        html.append("<h2>Visualiser les documents</h2>");
        html.append("<p>Pour visualiser un document existant:</p>");
        html.append("<ol>");
        html.append("<li>Sélectionnez le candidat dans la liste</li>");
        html.append("<li>Dans la section Documents, cliquez sur le bouton \"Voir\" à côté du document</li>");
        html.append("<li>Les images s'afficheront directement dans l'application</li>");
        html.append("<li>Les PDF s'ouvriront dans votre visionneuse PDF par défaut</li>");
        html.append("</ol>");

        html.append("<div class='warning-box'>");
        html.append("<h3>Attention</h3>");
        html.append("<p>Si un document ne s'ouvre pas correctement, vérifiez que vous avez les applications nécessaires installées sur votre ordinateur (visionneuse PDF, etc.).</p>");
        html.append("</div>");
        html.append("</div>");

        html.append("<div class='section-card'>");
        html.append("<h2>Générer des documents</h2>");
        html.append("<p>Le système peut générer automatiquement plusieurs types de documents:</p>");

        html.append("<div class='grid-container'>");
        html.append("<div class='grid-item'>");
        html.append("<h3>Fiche d'inscription</h3>");
        html.append("<ol>");
        html.append("<li>Sélectionnez le candidat dans la liste</li>");
        html.append("<li>Cliquez sur le bouton \"Générer Fiche\"</li>");
        html.append("<li>Choisissez le format (PDF ou PNG)</li>");
        html.append("<li>Le document sera généré et s'ouvrira automatiquement</li>");
        html.append("<li>Une copie est automatiquement sauvegardée dans le dossier du candidat</li>");
        html.append("</ol>");
        html.append("</div>");

        html.append("<div class='grid-item'>");
        html.append("<h3>Listes et rapports</h3>");
        html.append("<ul>");
        html.append("<li><strong>Liste des candidats actifs</strong> - Génère une liste de tous les candidats actifs</li>");
        html.append("<li><strong>Liste par catégorie</strong> - Génère une liste filtrée par catégorie de permis</li>");
        html.append("<li><strong>Résultats de recherche</strong> - Exporte les résultats de recherche actuels</li>");
        html.append("</ul>");
        html.append("<p>Utilisez les boutons correspondants dans la barre d'outils pour générer ces documents.</p>");
        html.append("</div>");
        html.append("</div>");

        html.append("<div class='success-box'>");
        html.append("<h3>Personnalisation</h3>");
        html.append("<p>Les administrateurs peuvent personnaliser les modèles de documents générés dans la section Paramètres > Modèles de documents.</p>");
        html.append("</div>");
        html.append("</div>");

        html.append("<div class='section-card'>");
        html.append("<h2>Gestion des documents</h2>");
        html.append("<p>Autres fonctionnalités de gestion des documents:</p>");
        html.append("<ul>");
        html.append("<li><strong>Remplacer</strong> - Pour mettre à jour un document existant, sélectionnez simplement un nouveau fichier</li>");
        html.append("<li><strong>Supprimer</strong> - Cliquez sur l'icône de suppression à côté du document (nécessite confirmation)</li>");
        html.append("<li><strong>Imprimer</strong> - Ouvrez le document puis utilisez la fonction d'impression de votre visionneuse</li>");
        html.append("<li><strong>Envoyer par email</strong> - Utilisez le bouton \"Envoyer\" pour transmettre le document au candidat</li>");
        html.append("</ul>");

        html.append("<div class='warning-box'>");
        html.append("<h3>Important</h3>");
        html.append("<p>La suppression d'un document est définitive et ne peut pas être annulée. Assurez-vous d'avoir une sauvegarde si nécessaire.</p>");
        html.append("</div>");
        html.append("</div>");

        html.append("</body></html>");
        return html.toString();
    }

    /**
     * Get HTML content for the Candidats Dashboard section - NEW
     */
    private String getCandidatsDashboardContent() {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head>");
        html.append("<style>");
        html.append(getCommonStyles());
        html.append("</style>");
        html.append("</head><body>");

        html.append("<h1>Tableau de bord candidats</h1>");
        html.append("<p>Cette section explique comment utiliser le tableau de bord des candidats pour visualiser et analyser les données importantes concernant vos candidats.</p>");

        html.append("<div class='section-card'>");
        html.append("<h2>Accéder au tableau de bord</h2>");
        html.append("<p>Pour accéder au tableau de bord des candidats:</p>");
        html.append("<ol>");
        html.append("<li>Accédez à la section \"Gestion des Candidats\" dans le menu principal</li>");
        html.append("<li>Cliquez sur le bouton \"Tableau de bord\" dans la barre d'outils</li>");
        html.append("</ol>");
        html.append("<p>Vous pouvez également utiliser le raccourci clavier <span class='badge'>Ctrl</span> + <span class='badge'>D</span> lorsque vous êtes dans la section Candidats.</p>");
        html.append("</div>");

        html.append("<div class='section-card'>");
        html.append("<h2>Vue d'ensemble</h2>");
        html.append("<p>Le tableau de bord présente plusieurs indicateurs clés:</p>");

        html.append("<div class='grid-container'>");
        html.append("<div class='grid-item'>");
        html.append("<h3>Statistiques générales</h3>");
        html.append("<ul>");
        html.append("<li><strong>Nombre total de candidats</strong> - Tous les candidats enregistrés</li>");
        html.append("<li><strong>Candidats actifs</strong> - Candidats actuellement en formation</li>");
        html.append("<li><strong>Candidats inactifs</strong> - Candidats ayant terminé ou interrompu leur formation</li>");
        html.append("</ul>");
        html.append("</div>");

        html.append("<div class='grid-item'>");
        html.append("<h3>Graphiques et visualisations</h3>");
        html.append("<ul>");
        html.append("<li><strong>Répartition par catégorie de permis</strong> - Graphique circulaire montrant la distribution</li>");
        html.append("<li><strong>Inscriptions par mois</strong> - Graphique à barres montrant l'évolution des inscriptions</li>");
        html.append("<li><strong>Taux de réussite aux examens</strong> - Pourcentage de candidats ayant réussi leurs examens</li>");
        html.append("</ul>");
        html.append("</div>");
        html.append("</div>");

        html.append("<div class='info-box'>");
        html.append("<h3>Actualisation des données</h3>");
        html.append("<p>Les données du tableau de bord sont actualisées automatiquement à chaque ouverture. Vous pouvez également cliquer sur le bouton \"Actualiser\" pour mettre à jour manuellement les statistiques.</p>");
        html.append("</div>");
        html.append("</div>");

        html.append("<div class='section-card'>");
        html.append("<h2>Filtrer les données</h2>");
        html.append("<p>Vous pouvez affiner les données affichées dans le tableau de bord:</p>");
        html.append("<ul>");
        html.append("<li><strong>Période</strong> - Sélectionnez une période spécifique (mois en cours, trimestre, année, etc.)</li>");
        html.append("<li><strong>Catégorie de permis</strong> - Filtrez par type de permis (A, B, C)</li>");
        html.append("<li><strong>Statut</strong> - Filtrez par statut des candidats (actif, inactif)</li>");
        html.append("</ul>");
        html.append("<p>Après avoir sélectionné vos filtres, cliquez sur \"Appliquer\" pour mettre à jour les visualisations.</p>");

        html.append("<div class='tip-box'>");
        html.append("<h3>Comparaison</h3>");
        html.append("<p>Utilisez l'option \"Comparer avec la période précédente\" pour voir l'évolution des indicateurs par rapport à la période antérieure.</p>");
        html.append("</div>");
        html.append("</div>");

        html.append("<div class='section-card'>");
        html.append("<h2>Analyses avancées</h2>");
        html.append("<p>Le tableau de bord offre également des analyses plus détaillées:</p>");

        html.append("<div class='grid-container'>");
        html.append("<div class='grid-item'>");
        html.append("<h3>Tendances et prévisions</h3>");
        html.append("<ul>");
        html.append("<li><strong>Tendance des inscriptions</strong> - Évolution et prévision des nouvelles inscriptions</li>");
        html.append("<li><strong>Taux d'abandon</strong> - Pourcentage de candidats qui interrompent leur formation</li>");
        html.append("<li><strong>Durée moyenne de formation</strong> - Temps moyen pour obtenir le permis</li>");
        html.append("</ul>");
        html.append("</div>");

        html.append("<div class='grid-item'>");
        html.append("<h3>Indicateurs de performance</h3>");
        html.append("<ul>");
        html.append("<li><strong>Taux de réussite par moniteur</strong> - Comparaison des résultats par moniteur</li>");
        html.append("<li><strong>Taux de réussite par catégorie</strong> - Comparaison des résultats par type de permis</li>");
        html.append("<li><strong>Nombre moyen de tentatives</strong> - Tentatives moyennes avant réussite</li>");
        html.append("</ul>");
        html.append("</div>");
        html.append("</div>");

        html.append("<div class='success-box'>");
        html.append("<h3>Exportation des rapports</h3>");
        html.append("<p>Vous pouvez exporter l'ensemble du tableau de bord ou des graphiques spécifiques en cliquant sur le bouton \"Exporter\" et en choisissant le format souhaité (PDF, Excel, PNG).</p>");
        html.append("</div>");
        html.append("</div>");

        html.append("</body></html>");
        return html.toString();
    }

    /**
     * Get HTML content for the Moniteurs Add section - IMPROVED
     */
    private String getMoniteursAddContent() {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head>");
        html.append("<style>");
        html.append(getCommonStyles());
        html.append("</style>");
        html.append("</head><body>");

        html.append("<h1>Ajouter un moniteur</h1>");
        html.append("<p>Cette section explique comment ajouter un nouveau moniteur dans le système. Suivez ces étapes pour enregistrer correctement toutes les informations nécessaires.</p>");

        html.append("<div class='step-card'>");
        html.append("<h2>Étape 1: Accéder au formulaire d'ajout</h2>");
        html.append("<p>Pour accéder au formulaire d'ajout d'un moniteur:</p>");
        html.append("<ol>");
        html.append("<li>Cliquez sur \"Gestion des Moniteurs\" dans le menu principal</li>");
        html.append("<li>Cliquez sur le bouton \"Ajouter\" dans la barre d'outils</li>");
        html.append("</ol>");
        html.append("<p class='note'>Vous pouvez également utiliser le raccourci clavier <span class='badge'>Ctrl</span> + <span class='badge'>N</span> lorsque vous êtes dans la section Moniteurs.</p>");
        html.append("</div>");

        html.append("<div class='step-card'>");
        html.append("<h2>Étape 2: Remplir les informations personnelles</h2>");
        html.append("<p>Remplissez les champs suivants avec les informations du moniteur:</p>");

        html.append("<div class='grid-container'>");
        html.append("<div class='grid-item'>");
        html.append("<h3>Informations obligatoires</h3>");
        html.append("<ul>");
        html.append("<li><strong>Nom</strong> - Nom de famille du moniteur</li>");
        html.append("<li><strong>Prénom</strong> - Prénom du moniteur</li>");
        html.append("<li><strong>CIN</strong> - Carte d'Identité Nationale (format: 8 chiffres)</li>");
        html.append("<li><strong>Date de naissance</strong> - Utilisez le sélecteur de date</li>");
        html.append("<li><strong>Téléphone</strong> - Numéro de téléphone (format: 8 chiffres)</li>");
        html.append("<li><strong>Date d'embauche</strong> - Date de début du contrat</li>");
        html.append("<li><strong>Date de fin de contrat</strong> - Date de fin prévue du contrat</li>");
        html.append("<li><strong>Numéro de permis</strong> - Numéro du permis de conduire</li>");
        html.append("<li><strong>Catégorie(s) de permis</strong> - Sélectionnez les catégories qu'il peut enseigner</li>");
        html.append("<li><strong>Salaire</strong> - Salaire mensuel du moniteur</li>");
        html.append("</ul>");
        html.append("</div>");

        html.append("<div class='grid-item'>");
        html.append("<h3>Informations optionnelles</h3>");
        html.append("<ul>");
        html.append("<li><strong>Expérience</strong> - Années d'expérience en tant que moniteur</li>");
        html.append("<li><strong>Diplômes</strong> - Qualifications et certifications</li>");
        html.append("<li><strong>Disponibilité</strong> - Statut de disponibilité du moniteur</li>");
        html.append("<li><strong>Motif</strong> - Raison d'indisponibilité si applicable</li>");
        html.append("<li><strong>Notes</strong> - Informations supplémentaires</li>");
        html.append("</ul>");
        html.append("</div>");
        html.append("</div>");

        html.append("<div class='warning-box'>");
        html.append("<h3>Attention</h3>");
        html.append("<p>Les champs CIN, numéro de permis et téléphone doivent être uniques pour chaque moniteur et ne peuvent pas être utilisés par un candidat.</p>");
        html.append("</div>");
        html.append("</div>");

        html.append("<div class='step-card'>");
        html.append("<h2>Étape 3: Définir les qualifications</h2>");
        html.append("<p>Sélectionnez les catégories de permis que le moniteur est habilité à enseigner:</p>");
        html.append("<ul>");
        html.append("<li><strong>Catégorie A</strong> - Pour les motos</li>");
        html.append("<li><strong>Catégorie B</strong> - Pour les voitures</li>");
        html.append("<li><strong>Catégorie C</strong> - Pour les poids lourds</li>");
        html.append("</ul>");
        html.append("<p>Un moniteur doit être qualifié pour au moins une catégorie de permis.</p>");

        html.append("<div class='info-box'>");
        html.append("<h3>Badges de qualification</h3>");
        html.append("<p>Les badges colorés indiquent visuellement les qualifications du moniteur:</p>");
        html.append("<ul>");
        html.append("<li><span class='badge badge-a'>A</span> - Rouge pour la catégorie A</li>");
        html.append("<li><span class='badge badge-b'>B</span> - Bleu pour la catégorie B</li>");
        html.append("<li><span class='badge badge-c'>C</span> - Vert pour la catégorie C</li>");
        html.append("</ul>");
        html.append("</div>");
        html.append("</div>");

        html.append("<div class='step-card'>");
        html.append("<h2>Étape 4: Enregistrer le moniteur</h2>");
        html.append("<p>Une fois toutes les informations saisies:</p>");
        html.append("<ol>");
        html.append("<li>Vérifiez que tous les champs obligatoires sont remplis</li>");
        html.append("<li>Cliquez sur le bouton \"Enregistrer\" pour sauvegarder le moniteur</li>");
        html.append("<li>Une confirmation s'affichera si l'enregistrement est réussi</li>");
        html.append("</ol>");
        html.append("<p>Vous pouvez également utiliser le raccourci clavier <span class='badge'>Ctrl</span> + <span class='badge'>S</span> pour enregistrer.</p>");
        html.append("</div>");

        html.append("<div class='success-box'>");
        html.append("<h3>Après l'ajout</h3>");
        html.append("<p>Après avoir ajouté un moniteur, vous pouvez:</p>");
        html.append("<ul>");
        html.append("<li>Assigner des candidats à ce moniteur</li>");
        html.append("<li>Planifier des séances de conduite</li>");
        html.append("<li>Consulter son planning dans le calendrier</li>");
        html.append("</ul>");
        html.append("</div>");

        html.append("</body></html>");
        return html.toString();
    }

    /**
     * Get HTML content for the Moniteurs Edit section
     */
    private String getMoniteursEditContent() {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head>");
        html.append("<style>");
        html.append(getCommonStyles());
        html.append("</style>");
        html.append("</head><body>");

        html.append("<h1>Modifier un moniteur</h1>");
        html.append("<p>Cette section explique comment modifier les informations d'un moniteur existant. Suivez ces étapes pour mettre à jour correctement les données.</p>");

        html.append("<div class='step-card'>");
        html.append("<h2>Étape 1: Sélectionner un moniteur</h2>");
        html.append("<p>Pour modifier un moniteur existant:</p>");
        html.append("<ol>");
        html.append("<li>Accédez à la section \"Gestion des Moniteurs\" dans le menu principal</li>");
        html.append("<li>Recherchez le moniteur à modifier en utilisant la barre de recherche ou les filtres</li>");
        html.append("<li>Sélectionnez le moniteur dans la liste en cliquant sur sa ligne</li>");
        html.append("<li>Cliquez sur le bouton \"Modifier\" dans la barre d'outils</li>");
        html.append("</ol>");
        html.append("<div class='info-box'>");
        html.append("<h3>Astuce</h3>");
        html.append("<p>Vous pouvez également double-cliquer sur un moniteur dans la liste pour ouvrir directement le formulaire de modification.</p>");
        html.append("<p>Ou utiliser le raccourci clavier <span class='badge'>Ctrl</span> + <span class='badge'>E</span> après avoir sélectionné un moniteur.</p>");
        html.append("</div>");
        html.append("</div>");

        html.append("<div class='step-card'>");
        html.append("<h2>Étape 2: Modifier les informations</h2>");
        html.append("<p>Le formulaire de modification s'ouvre avec les informations actuelles du moniteur:</p>");
        html.append("<ul>");
        html.append("<li>Modifiez les champs nécessaires</li>");
        html.append("<li>Les champs obligatoires sont marqués d'un astérisque (*)</li>");
        html.append("<li>Vous pouvez modifier les qualifications (catégories de permis)</li>");
        html.append("<li>Vous pouvez changer le statut de disponibilité du moniteur</li>");
        html.append("</ul>");

        html.append("<div class='warning-box'>");
        html.append("<h3>Attention</h3>");
        html.append("<p>La modification du CIN, du numéro de permis ou du téléphone peut échouer si la nouvelle valeur est déjà utilisée par un autre moniteur ou candidat.</p>");
        html.append("</div>");
        html.append("</div>");

        html.append("<div class='step-card'>");
        html.append("<h2>Étape 3: Gérer la disponibilité</h2>");
        html.append("<p>Vous pouvez mettre à jour le statut de disponibilité du moniteur:</p>");
        html.append("<ol>");
        html.append("<li>Sélectionnez \"Disponible\" ou \"Non disponible\" dans les options de disponibilité</li>");
        html.append("<li>Si vous sélectionnez \"Non disponible\", vous devez indiquer un motif (congé, maladie, formation, etc.)</li>");
        html.append("<li>L'indicateur de disponibilité changera de couleur en fonction du statut:</li>");
        html.append("<ul>");
        html.append("<li><span style='color: green;'>●</span> Vert pour disponible</li>");
        html.append("<li><span style='color: red;'>●</span> Rouge pour non disponible</li>");
        html.append("</ul>");
        html.append("</ol>");

        html.append("<div class='info-box'>");
        html.append("<h3>Impact sur la planification</h3>");
        html.append("<p>Un moniteur marqué comme non disponible ne pourra pas être assigné à de nouvelles séances de conduite ou examens jusqu'à ce que son statut soit changé.</p>");
        html.append("</div>");
        html.append("</div>");

        html.append("<div class='step-card'>");
        html.append("<h2>Étape 4: Enregistrer les modifications</h2>");
        html.append("<p>Une fois les modifications effectuées:</p>");
        html.append("<ol>");
        html.append("<li>Vérifiez que tous les champs obligatoires sont correctement remplis</li>");
        html.append("<li>Cliquez sur le bouton \"Enregistrer\" pour sauvegarder les modifications</li>");
        html.append("<li>Une confirmation s'affichera si la mise à jour est réussie</li>");
        html.append("</ol>");
        html.append("<p>Vous pouvez également utiliser le raccourci clavier <span class='badge'>Ctrl</span> + <span class='badge'>S</span> pour enregistrer.</p>");

        html.append("<div class='info-box'>");
        html.append("<h3>Annulation</h3>");
        html.append("<p>Pour annuler les modifications et revenir à l'état précédent, cliquez sur le bouton \"Annuler\" ou utilisez la touche <span class='badge'>Échap</span>.</p>");
        html.append("</div>");
        html.append("</div>");

        html.append("<div class='success-box'>");
        html.append("<h3>Historique des modifications</h3>");
        html.append("<p>Le système conserve un historique des modifications apportées aux moniteurs. Cet historique peut être consulté par les administrateurs dans la section Paramètres > Journaux d'activité.</p>");
        html.append("</div>");

        html.append("</body></html>");
        return html.toString();
    }

    /**
     * Get HTML content for the Moniteurs Qualifications section
     */
    private String getMoniteursQualificationsContent() {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head>");
        html.append("<style>");
        html.append(getCommonStyles());
        html.append("</style>");
        html.append("</head><body>");

        html.append("<h1>Qualifications des moniteurs</h1>");
        html.append("<p>Cette section explique comment gérer les qualifications des moniteurs, y compris les catégories de permis qu'ils peuvent enseigner et leurs certifications.</p>");

        html.append("<div class='section-card'>");
        html.append("<h2>Types de qualifications</h2>");
        html.append("<p>Les moniteurs peuvent être qualifiés pour enseigner différentes catégories de permis:</p>");

        html.append("<div class='grid-container'>");
        html.append("<div class='grid-item'>");
        html.append("<h3>Catégories de permis</h3>");
        html.append("<ul>");
        html.append("<li><strong>Catégorie A</strong> - Pour les motos et deux-roues motorisés</li>");
        html.append("<li><strong>Catégorie B</strong> - Pour les voitures et véhicules légers</li>");
        html.append("<li><strong>Catégorie C</strong> - Pour les poids lourds et véhicules de transport</li>");
        html.append("</ul>");
        html.append("<p>Un moniteur peut être qualifié pour une ou plusieurs catégories.</p>");
        html.append("</div>");

        html.append("<div class='grid-item'>");
        html.append("<h3>Certifications requises</h3>");
        html.append("<ul>");
        html.append("<li><strong>BEPECASER</strong> - Brevet pour l'Exercice de la Profession d'Enseignant de la Conduite Automobile et de la Sécurité Routière</li>");
        html.append("<li><strong>Mention deux-roues</strong> - Pour enseigner la catégorie A</li>");
        html.append("<li><strong>Mention groupe lourd</strong> - Pour enseigner la catégorie C</li>");
        html.append("<li><strong>Autorisation d'enseigner</strong> - Document officiel délivré par les autorités</li>");
        html.append("</ul>");
        html.append("</div>");
        html.append("</div>");

        html.append("<div class='info-box'>");
        html.append("<h3>Validité des qualifications</h3>");
        html.append("<p>Les autorisations d'enseigner doivent être renouvelées tous les 5 ans. Le système vous alertera lorsqu'une autorisation approche de sa date d'expiration.</p>");
        html.append("</div>");
        html.append("</div>");

        html.append("<div class='section-card'>");
        html.append("<h2>Ajouter ou modifier des qualifications</h2>");
        html.append("<p>Pour gérer les qualifications d'un moniteur:</p>");
        html.append("<ol>");
        html.append("<li>Accédez à la section \"Gestion des Moniteurs\"</li>");
        html.append("<li>Sélectionnez le moniteur concerné</li>");
        html.append("<li>Cliquez sur \"Modifier\" pour accéder au formulaire</li>");
        html.append("<li>Dans la section Qualifications, cochez ou décochez les catégories appropriées</li>");
        html.append("<li>Si nécessaire, ajoutez les informations de certification dans le champ \"Diplômes\"</li>");
        html.append("<li>Cliquez sur \"Enregistrer\" pour sauvegarder les modifications</li>");
        html.append("</ol>");

        html.append("<div class='warning-box'>");
        html.append("<h3>Important</h3>");
        html.append("<p>Un moniteur doit être qualifié pour au moins une catégorie de permis. Le système ne permettra pas d'enregistrer un moniteur sans qualification.</p>");
        html.append("</div>");
        html.append("</div>");

        html.append("<div class='section-card'>");
        html.append("<h2>Visualisation des qualifications</h2>");
        html.append("<p>Les qualifications des moniteurs sont visualisées de plusieurs façons dans le système:</p>");

        html.append("<div class='grid-container'>");
        html.append("<div class='grid-item'>");
        html.append("<h3>Badges de qualification</h3>");
        html.append("<p>Dans l'interface, les qualifications sont représentées par des badges colorés:</p>");
        html.append("<ul>");
        html.append("<li><span class='badge badge-a'>A</span> - Badge rouge pour la catégorie A</li>");
        html.append("<li><span class='badge badge-b'>B</span> - Badge bleu pour la catégorie B</li>");
        html.append("<li><span class='badge badge-c'>C</span> - Badge vert pour la catégorie C</li>");
        html.append("</ul>");
        html.append("<p>Ces badges apparaissent dans la liste des moniteurs et sur leur fiche détaillée.</p>");
        html.append("</div>");

        html.append("<div class='grid-item'>");
        html.append("<h3>Filtrage par qualification</h3>");
        html.append("<p>Vous pouvez filtrer la liste des moniteurs par qualification:</p>");
        html.append("<ol>");
        html.append("<li>Utilisez le filtre \"Catégorie\" dans la barre de filtres</li>");
        html.append("<li>Sélectionnez la catégorie souhaitée (A, B, C ou Toutes)</li>");
        html.append("<li>La liste affichera uniquement les moniteurs qualifiés pour cette catégorie</li>");
        html.append("</ol>");
        html.append("<p>Cette fonctionnalité est utile pour trouver rapidement un moniteur disponible pour une catégorie spécifique.</p>");
        html.append("</div>");
        html.append("</div>");

        html.append("<div class='tip-box'>");
        html.append("<h3>Astuce</h3>");
        html.append("<p>Lors de la planification d'une leçon de conduite, le système proposera automatiquement uniquement les moniteurs qualifiés pour la catégorie de permis du candidat.</p>");
        html.append("</div>");
        html.append("</div>");

        html.append("<div class='section-card'>");
        html.append("<h2>Gestion des certifications</h2>");
        html.append("<p>Pour une gestion complète des certifications des moniteurs:</p>");
        html.append("<ol>");
        html.append("<li>Accédez à la section \"Gestion des Moniteurs\"</li>");
        html.append("<li>Cliquez sur l'onglet \"Certifications\" dans la barre d'outils</li>");
        html.append("<li>Vous verrez un tableau récapitulatif de toutes les certifications et leurs dates d'expiration</li>");
        html.append("<li>Utilisez le bouton \"Ajouter une certification\" pour enregistrer une nouvelle certification</li>");
        html.append("<li>Utilisez le bouton \"Renouveler\" pour mettre à jour une certification existante</li>");
        html.append("</ol>");

        html.append("<div class='success-box'>");
        html.append("<h3>Alertes d'expiration</h3>");
        html.append("<p>Le système génère automatiquement des alertes 3 mois avant l'expiration d'une certification. Ces alertes apparaissent dans le tableau de bord et peuvent être configurées pour être envoyées par email.</p>");
        html.append("</div>");
        html.append("</div>");

        html.append("</body></html>");
        return html.toString();
    }

    /**
     * Get HTML content for the Moniteurs Dashboard section - NEW
     */
    private String getMoniteursDashboardContent() {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head>");
        html.append("<style>");
        html.append(getCommonStyles());
        html.append("</style>");
        html.append("</head><body>");

        html.append("<h1>Tableau de bord moniteurs</h1>");
        html.append("<p>Cette section explique comment utiliser le tableau de bord des moniteurs pour visualiser et analyser les données importantes concernant vos moniteurs.</p>");

        html.append("<div class='section-card'>");
        html.append("<h2>Accéder au tableau de bord</h2>");
        html.append("<p>Pour accéder au tableau de bord des moniteurs:</p>");
        html.append("<ol>");
        html.append("<li>Accédez à la section \"Gestion des Moniteurs\" dans le menu principal</li>");
        html.append("<li>Cliquez sur le bouton \"Tableau de bord\" dans la barre d'outils</li>");
        html.append("</ol>");
        html.append("<p>Vous pouvez également utiliser le raccourci clavier <span class='badge'>Ctrl</span> + <span class='badge'>D</span> lorsque vous êtes dans la section Moniteurs.</p>");
        html.append("</div>");

        html.append("<div class='section-card'>");
        html.append("<h2>Vue d'ensemble</h2>");
        html.append("<p>Le tableau de bord présente plusieurs indicateurs clés:</p>");

        html.append("<div class='grid-container'>");
        html.append("<div class='grid-item'>");
        html.append("<h3>Statistiques générales</h3>");
        html.append("<ul>");
        html.append("<li><strong>Nombre total de moniteurs</strong> - Tous les moniteurs enregistrés</li>");
        html.append("<li><strong>Moniteurs disponibles</strong> - Moniteurs actuellement disponibles</li>");
        html.append("<li><strong>Moniteurs indisponibles</strong> - Moniteurs temporairement indisponibles</li>");
        html.append("</ul>");
        html.append("</div>");

        html.append("<div class='grid-item'>");
        html.append("<h3>Graphiques et visualisations</h3>");
        html.append("<ul>");
        html.append("<li><strong>Répartition par qualification</strong> - Graphique circulaire montrant la distribution des qualifications</li>");
        html.append("<li><strong>Expérience professionnelle</strong> - Graphique à barres montrant la répartition par années d'expérience</li>");
        html.append("<li><strong>Taux de réussite des élèves</strong> - Pourcentage de réussite des candidats par moniteur</li>");
        html.append("</ul>");
        html.append("</div>");
        html.append("</div>");

        html.append("<div class='info-box'>");
        html.append("<h3>Actualisation des données</h3>");
        html.append("<p>Les données du tableau de bord sont actualisées automatiquement à chaque ouverture. Vous pouvez également cliquer sur le bouton \"Actualiser\" pour mettre à jour manuellement les statistiques.</p>");
        html.append("</div>");
        html.append("</div>");

        html.append("<div class='section-card'>");
        html.append("<h2>Analyse de la charge de travail</h2>");
        html.append("<p>Le tableau de bord permet d'analyser la charge de travail des moniteurs:</p>");

        html.append("<div class='grid-container'>");
        html.append("<div class='grid-item'>");
        html.append("<h3>Heures de conduite</h3>");
        html.append("<ul>");
        html.append("<li><strong>Heures par moniteur</strong> - Nombre d'heures de conduite effectuées par chaque moniteur</li>");
        html.append("<li><strong>Comparaison mensuelle</strong> - Évolution de la charge de travail sur plusieurs mois</li>");
        html.append("<li><strong>Répartition hebdomadaire</strong> - Distribution des heures sur les jours de la semaine</li>");
        html.append("</ul>");
        html.append("</div>");

        html.append("<div class='grid-item'>");
        html.append("<h3>Examens accompagnés</h3>");
        html.append("<ul>");
        html.append("<li><strong>Nombre d'examens</strong> - Examens accompagnés par chaque moniteur</li>");
        html.append("<li><strong>Taux de réussite</strong> - Pourcentage de réussite aux examens par moniteur</li>");
        html.append("<li><strong>Comparaison par catégorie</strong> - Résultats par type de permis</li>");
        html.append("</ul>");
        html.append("</div>");
        html.append("</div>");

        html.append("<div class='tip-box'>");
        html.append("<h3>Équilibrage de charge</h3>");
        html.append("<p>Utilisez ces informations pour équilibrer la charge de travail entre les moniteurs et éviter la surcharge de certains employés. Un équilibre optimal améliore la qualité de l'enseignement et la satisfaction des moniteurs.</p>");
        html.append("</div>");
        html.append("</div>");

        html.append("<div class='section-card'>");
        html.append("<h2>Suivi des performances</h2>");
        html.append("<p>Le tableau de bord permet également de suivre les performances des moniteurs:</p>");

        html.append("<div class='grid-container'>");
        html.append("<div class='grid-item'>");
        html.append("<h3>Indicateurs de performance</h3>");
        html.append("<ul>");
        html.append("<li><strong>Taux de réussite global</strong> - Pourcentage de candidats ayant réussi leur examen</li>");
        html.append("<li><strong>Satisfaction des candidats</strong> - Évaluations données par les candidats</li>");
        html.append("<li><strong>Nombre moyen de leçons</strong> - Leçons nécessaires avant que les candidats soient prêts pour l'examen</li>");
        html.append("</ul>");
        html.append("</div>");

        html.append("<div class='grid-item'>");
        html.append("<h3>Comparaison et classement</h3>");
        html.append("<ul>");
        html.append("<li><strong>Classement par performance</strong> - Moniteurs classés selon leurs résultats</li>");
        html.append("<li><strong>Évolution dans le temps</strong> - Amélioration ou détérioration des performances</li>");
        html.append("<li><strong>Comparaison avec les moyennes</strong> - Performance individuelle vs moyenne de l'auto-école</li>");
        html.append("</ul>");
        html.append("</div>");
        html.append("</div>");

        html.append("<div class='success-box'>");
        html.append("<h3>Exportation des rapports</h3>");
        html.append("<p>Vous pouvez exporter l'ensemble du tableau de bord ou des graphiques spécifiques en cliquant sur le bouton \"Exporter\" et en choisissant le format souhaité (PDF, Excel, PNG).</p>");
        html.append("</div>");
        html.append("</div>");

        html.append("</body></html>");
        return html.toString();
    }

    /**
     * Get HTML content for the Vehicules Add section
     */
    private String getVehiculesAddContent() {
        // Implementation for vehicules add help content
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head>");
        html.append("<style>");
        html.append(getCommonStyles());
        html.append("</style>");
        html.append("</head><body>");

        html.append("<h1>Ajouter un véhicule</h1>");
        html.append("<p>Cette section explique comment ajouter un nouveau véhicule dans le système. Suivez ces étapes pour enregistrer correctement toutes les informations nécessaires.</p>");

        // Content for vehicules add section...
        html.append("<div class='step-card'>");
        html.append("<h2>Étape 1: Accéder au formulaire d'ajout</h2>");
        html.append("<p>Pour accéder au formulaire d'ajout d'un véhicule:</p>");
        html.append("<ol>");
        html.append("<li>Cliquez sur \"Gestion des Véhicules\" dans le menu principal</li>");
        html.append("<li>Cliquez sur le bouton \"Ajouter\" dans la barre d'outils</li>");
        html.append("</ol>");
        html.append("<p class='note'>Vous pouvez également utiliser le raccourci clavier <span class='badge'>Ctrl</span> + <span class='badge'>N</span> lorsque vous êtes dans la section Véhicules.</p>");
        html.append("</div>");

        html.append("<div class='step-card'>");
        html.append("<h2>Étape 2: Remplir les informations du véhicule</h2>");
        html.append("<p>Remplissez les champs suivants avec les informations du véhicule:</p>");

        html.append("<div class='grid-container'>");
        html.append("<div class='grid-item'>");
        html.append("<h3>Informations obligatoires</h3>");
        html.append("<ul>");
        html.append("<li><strong>Immatriculation</strong> - Numéro d'immatriculation du véhicule</li>");
        html.append("<li><strong>Marque</strong> - Marque du véhicule</li>");
        html.append("<li><strong>Modèle</strong> - Modèle du véhicule</li>");
        html.append("<li><strong>Année</strong> - Année de mise en circulation</li>");
        html.append("<li><strong>Type</strong> - Type de véhicule (voiture, moto, poids lourd)</li>");
        html.append("<li><strong>Catégorie</strong> - Catégorie de permis correspondante (A, B, C)</li>");
        html.append("<li><strong>Date d'acquisition</strong> - Date d'achat ou de location</li>");
        html.append("</ul>");
        html.append("</div>");

        html.append("<div class='grid-item'>");
        html.append("<h3>Informations optionnelles</h3>");
        html.append("<ul>");
        html.append("<li><strong>Kilométrage</strong> - Kilométrage actuel du véhicule</li>");
        html.append("<li><strong>Couleur</strong> - Couleur du véhicule</li>");
        html.append("<li><strong>Carburant</strong> - Type de carburant utilisé</li>");
        html.append("<li><strong>Numéro de série</strong> - Numéro de série du véhicule</li>");
        html.append("<li><strong>Notes</strong> - Informations supplémentaires</li>");
        html.append("</ul>");
        html.append("</div>");
        html.append("</div>");

        html.append("<div class='warning-box'>");
        html.append("<h3>Attention</h3>");
        html.append("<p>Le numéro d'immatriculation doit être unique dans le système. Vérifiez qu'il est correctement saisi.</p>");
        html.append("</div>");
        html.append("</div>");

        html.append("<div class='step-card'>");
        html.append("<h2>Étape 3: Ajouter les informations d'assurance</h2>");
        html.append("<p>Renseignez les détails de l'assurance du véhicule:</p>");
        html.append("<ul>");
        html.append("<li><strong>Compagnie d'assurance</strong> - Nom de la compagnie d'assurance</li>");
        html.append("<li><strong>Numéro de police</strong> - Numéro de la police d'assurance</li>");
        html.append("<li><strong>Date de début</strong> - Date de début de validité de l'assurance</li>");
        html.append("<li><strong>Date de fin</strong> - Date d'expiration de l'assurance</li>");
        html.append("<li><strong>Type de couverture</strong> - Niveau de couverture (tous risques, tiers, etc.)</li>");
        html.append("</ul>");

        html.append("<div class='info-box'>");
        html.append("<h3>Rappels automatiques</h3>");
        html.append("<p>Le système générera automatiquement des rappels 30 jours avant l'expiration de l'assurance pour vous permettre de la renouveler à temps.</p>");
        html.append("</div>");
        html.append("</div>");

        html.append("<div class='step-card'>");
        html.append("<h2>Étape 4: Enregistrer le véhicule</h2>");
        html.append("<p>Une fois toutes les informations saisies:</p>");
        html.append("<ol>");
        html.append("<li>Vérifiez que tous les champs obligatoires sont remplis</li>");
        html.append("<li>Cliquez sur le bouton \"Enregistrer\" pour sauvegarder le véhicule</li>");
        html.append("<li>Une confirmation s'affichera si l'enregistrement est réussi</li>");
        html.append("</ol>");
        html.append("<p>Vous pouvez également utiliser le raccourci clavier <span class='badge'>Ctrl</span> + <span class='badge'>S</span> pour enregistrer.</p>");
        html.append("</div>");

        html.append("<div class='success-box'>");
        html.append("<h3>Après l'ajout</h3>");
        html.append("<p>Après avoir ajouté un véhicule, vous pouvez:</p>");
        html.append("<ul>");
        html.append("<li>Planifier des maintenances régulières</li>");
        html.append("<li>Assigner le véhicule à des moniteurs spécifiques</li>");
        html.append("<li>Suivre son utilisation et son kilométrage</li>");
        html.append("</ul>");
        html.append("</div>");

        html.append("</body></html>");
        return html.toString();
    }

    /**
     * Get HTML content for the Vehicules Maintenance section
     */
    private String getVehiculesMaintenanceContent() {
        // Implementation for vehicules maintenance help content
        return getPlaceholderContent("Maintenance des véhicules");
    }

    /**
     * Get HTML content for the Vehicules Insurance section
     */
    private String getVehiculesInsuranceContent() {
        // Implementation for vehicules insurance help content
        return getPlaceholderContent("Assurances des véhicules");
    }

    /**
     * Get HTML content for the Examens Schedule section - IMPROVED
     */
    private String getExamensScheduleContent() {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head>");
        html.append("<style>");
        html.append(getCommonStyles());
        html.append("</style>");
        html.append("</head><body>");

        html.append("<h1>Planifier un examen</h1>");
        html.append("<p>Cette section explique comment planifier un examen pour un candidat. Suivez ces étapes pour enregistrer correctement toutes les informations nécessaires.</p>");

        html.append("<div class='step-card'>");
        html.append("<h2>Étape 1: Accéder au formulaire de planification</h2>");
        html.append("<p>Pour accéder au formulaire de planification d'un examen:</p>");
        html.append("<ol>");
        html.append("<li>Cliquez sur \"Gestion des Examens\" dans le menu principal</li>");
        html.append("<li>Cliquez sur le bouton \"Ajouter\" dans la barre d'outils</li>");
        html.append("</ol>");
        html.append("<p class='note'>Vous pouvez également utiliser le raccourci clavier <span class='badge'>Ctrl</span> + <span class='badge'>N</span> lorsque vous êtes dans la section Examens.</p>");
        html.append("</div>");

        html.append("<div class='step-card'>");
        html.append("<h2>Étape 2: Sélectionner un candidat</h2>");
        html.append("<p>Sélectionnez le candidat pour lequel vous souhaitez planifier un examen:</p>");
        html.append("<ol>");
        html.append("<li>Utilisez le menu déroulant pour sélectionner un candidat</li>");
        html.append("<li>Vous pouvez rechercher un candidat par nom, prénom ou CIN</li>");
        html.append("<li>Une fois sélectionné, les informations du candidat s'afficheront automatiquement</li>");
        html.append("</ol>");

        html.append("<div class='info-box'>");
        html.append("<h3>Vérification automatique</h3>");
        html.append("<p>Le système vérifie automatiquement l'éligibilité du candidat pour passer un examen. Pour un examen de conduite, le candidat doit avoir déjà passé un examen de code.</p>");
        html.append("</div>");
        html.append("</div>");

        html.append("<div class='step-card'>");
        html.append("<h2>Étape 3: Remplir les détails de l'examen</h2>");
        html.append("<p>Renseignez les informations suivantes:</p>");

        html.append("<div class='grid-container'>");
        html.append("<div class='grid-item'>");
        html.append("<h3>Informations obligatoires</h3>");
        html.append("<ul>");
        html.append("<li><strong>Type d'examen</strong> - Sélectionnez \"Code\" ou \"Conduite\"</li>");
        html.append("<li><strong>Date d'examen</strong> - Sélectionnez la date prévue</li>");
        html.append("<li><strong>Lieu d'examen</strong> - Indiquez le centre d'examen</li>");
        html.append("<li><strong>Frais d'inscription</strong> - Montant des frais pour cet examen</li>");
        html.append("</ul>");
        html.append("</div>");

        html.append("<div class='grid-item'>");
        html.append("<h3>Informations optionnelles</h3>");
        html.append("<ul>");
        html.append("<li><strong>Moniteur accompagnateur</strong> - Moniteur qui accompagnera le candidat</li>");
        html.append("<li><strong>Véhicule</strong> - Véhicule qui sera utilisé (pour examen de conduite)</li>");
        html.append("<li><strong>Heure de rendez-vous</strong> - Heure à laquelle le candidat doit se présenter</li>");
        html.append("<li><strong>Notes</strong> - Informations supplémentaires</li>");
        html.append("</ul>");
        html.append("</div>");
        html.append("</div>");

        html.append("<div class='warning-box'>");
        html.append("<h3>Attention</h3>");
        html.append("<p>Pour un examen de conduite, assurez-vous que le candidat a bien réussi son examen de code au préalable. Le système vous alertera si ce n'est pas le cas.</p>");
        html.append("</div>");
        html.append("</div>");

        html.append("<div class='step-card'>");
        html.append("<h2>Étape 4: Vérifier les disponibilités</h2>");
        html.append("<p>Avant de confirmer la planification:</p>");
        html.append("<ol>");
        html.append("<li>Vérifiez que la date choisie ne tombe pas un jour férié ou un dimanche</li>");
        html.append("<li>Si vous avez sélectionné un moniteur, vérifiez sa disponibilité à cette date</li>");
        html.append("<li>Si vous avez sélectionné un véhicule, vérifiez sa disponibilité à cette date</li>");
        html.append("</ol>");

        html.append("<div class='tip-box'>");
        html.append("<h3>Astuce</h3>");
        html.append("<p>Utilisez le bouton \"Vérifier disponibilités\" pour que le système effectue automatiquement ces vérifications et vous alerte en cas de conflit.</p>");
        html.append("</div>");
        html.append("</div>");

        html.append("<div class='step-card'>");
        html.append("<h2>Étape 5: Enregistrer l'examen</h2>");
        html.append("<p>Une fois toutes les informations saisies:</p>");
        html.append("<ol>");
        html.append("<li>Vérifiez que tous les champs obligatoires sont remplis</li>");
        html.append("<li>Cliquez sur le bouton \"Enregistrer\" pour planifier l'examen</li>");
        html.append("<li>Une confirmation s'affichera si l'enregistrement est réussi</li>");
        html.append("</ol>");
        html.append("<p>Vous pouvez également utiliser le raccourci clavier <span class='badge'>Ctrl</span> + <span class='badge'>S</span> pour enregistrer.</p>");
        html.append("</div>");

        html.append("<div class='success-box'>");
        html.append("<h3>Après la planification</h3>");
        html.append("<p>Après avoir planifié un examen, vous pouvez:</p>");
        html.append("<ul>");
        html.append("<li>Imprimer une convocation pour le candidat</li>");
        html.append("<li>Envoyer une notification par email ou SMS</li>");
        html.append("<li>Visualiser l'examen dans le calendrier général</li>");
        html.append("<li>Modifier ou annuler l'examen si nécessaire</li>");
        html.append("</ul>");
        html.append("</div>");

        html.append("</body></html>");
        return html.toString();
    }

    /**
     * Get HTML content for the Examens Results section - IMPROVED
     */
    private String getExamensResultsContent() {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head>");
        html.append("<style>");
        html.append(getCommonStyles());
        html.append("</style>");
        html.append("</head><body>");

        html.append("<h1>Résultats d'examen</h1>");
        html.append("<p>Cette section explique comment enregistrer, consulter et gérer les résultats d'examens des candidats.</p>");

        html.append("<div class='section-card'>");
        html.append("<h2>Enregistrer un résultat d'examen</h2>");
        html.append("<p>Pour enregistrer le résultat d'un examen:</p>");
        html.append("<ol>");
        html.append("<li>Accédez à la section \"Gestion des Examens\" dans le menu principal</li>");
        html.append("<li>Recherchez et sélectionnez l'examen concerné dans la liste</li>");
        html.append("<li>Cliquez sur le bouton \"Modifier\" dans la barre d'outils</li>");
        html.append("<li>Dans le formulaire, cochez ou décochez la case \"Validé\" selon le résultat</li>");
        html.append("<li>Si nécessaire, ajoutez des commentaires dans le champ \"Notes\"</li>");
        html.append("<li>Cliquez sur \"Enregistrer\" pour sauvegarder le résultat</li>");
        html.append("</ol>");

        html.append("<div class='tip-box'>");
        html.append("<h3>Raccourci</h3>");
        html.append("<p>Vous pouvez également utiliser le bouton \"Valider\" directement dans la liste des examens pour marquer rapidement un examen comme réussi.</p>");
        html.append("</div>");
        html.append("</div>");

        html.append("<div class='section-card'>");
        html.append("<h2>Consulter les résultats</h2>");
        html.append("<p>Plusieurs méthodes sont disponibles pour consulter les résultats d'examens:</p>");

        html.append("<div class='grid-container'>");
        html.append("<div class='grid-item'>");
        html.append("<h3>Vue par examen</h3>");
        html.append("<ol>");
        html.append("<li>Accédez à la section \"Gestion des Examens\"</li>");
        html.append("<li>Utilisez les filtres pour afficher les examens passés</li>");
        html.append("<li>La colonne \"Statut\" indique si l'examen est validé ou non</li>");
        html.append("<li>Vous pouvez trier la liste en cliquant sur l'en-tête de colonne</li>");
        html.append("</ol>");
        html.append("</div>");

        html.append("<div class='grid-item'>");
        html.append("<h3>Vue par candidat</h3>");
        html.append("<ol>");
        html.append("<li>Accédez à la section \"Gestion des Candidats\"</li>");
        html.append("<li>Sélectionnez un candidat dans la liste</li>");
        html.append("<li>Cliquez sur l'onglet \"Examens\" dans la fiche du candidat</li>");
        html.append("<li>Vous verrez l'historique complet des examens du candidat</li>");
        html.append("</ol>");
        html.append("</div>");
        html.append("</div>");

        html.append("<div class='info-box'>");
        html.append("<h3>Indicateurs visuels</h3>");
        html.append("<p>Les résultats d'examens sont indiqués par des codes couleur:</p>");
        html.append("<ul>");
        html.append("<li><span style='color: green;'>●</span> Vert pour les examens réussis</li>");
        html.append("<li><span style='color: red;'>●</span> Rouge pour les examens échoués</li>");
        html.append("<li><span style='color: orange;'>●</span> Orange pour les examens à venir</li>");
        html.append("</ul>");
        html.append("</div>");
        html.append("</div>");

        html.append("<div class='section-card'>");
        html.append("<h2>Gestion des échecs et reprogrammation</h2>");
        html.append("<p>En cas d'échec à un examen, vous pouvez facilement reprogrammer une nouvelle tentative:</p>");
        html.append("<ol>");
        html.append("<li>Sélectionnez l'examen échoué dans la liste</li>");
        html.append("<li>Cliquez sur le bouton \"Reprogrammer\" dans la barre d'outils</li>");
        html.append("<li>Un nouveau formulaire s'ouvre avec les informations pré-remplies</li>");
        html.append("<li>Modifiez la date et autres détails si nécessaire</li>");
        html.append("<li>Cliquez sur \"Enregistrer\" pour créer le nouvel examen</li>");
        html.append("</ol>");

        html.append("<div class='warning-box'>");
        html.append("<h3>Délais réglementaires</h3>");
        html.append("<p>Le système vérifie automatiquement que les délais réglementaires entre deux tentatives sont respectés. Un message d'avertissement s'affichera si la nouvelle date est trop proche de la précédente tentative.</p>");
        html.append("</div>");
        html.append("</div>");

        html.append("<div class='section-card'>");
        html.append("<h2>Génération de documents</h2>");
        html.append("<p>Vous pouvez générer plusieurs types de documents liés aux résultats d'examens:</p>");
        html.append("<ul>");
        html.append("<li><strong>Attestation de réussite</strong> - Document officiel pour les examens réussis</li>");
        html.append("<li><strong>Relevé de résultats</strong> - Récapitulatif détaillé des résultats</li>");
        html.append("<li><strong>Convocation pour nouvelle tentative</strong> - En cas d'échec</li>");
        html.append("<li><strong>Rapport statistique</strong> - Analyse des résultats sur une période</li>");
        html.append("</ul>");
        html.append("<p>Pour générer un document, sélectionnez l'examen concerné puis cliquez sur le bouton correspondant dans la barre d'outils.</p>");

        html.append("<div class='success-box'>");
        html.append("<h3>Envoi automatique</h3>");
        html.append("<p>Vous pouvez configurer le système pour envoyer automatiquement les résultats par email aux candidats. Cette option est disponible dans les paramètres de l'application.</p>");
        html.append("</div>");
        html.append("</div>");

        html.append("</body></html>");
        return html.toString();
    }

    /**
     * Get HTML content for the Examens Statistics section - IMPROVED
     */
    private String getExamensStatisticsContent() {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head>");
        html.append("<style>");
        html.append(getCommonStyles());
        html.append("</style>");
        html.append("</head><body>");

        html.append("<h1>Statistiques des examens</h1>");
        html.append("<p>Cette section explique comment accéder et interpréter les statistiques relatives aux examens dans le système.</p>");

        html.append("<div class='section-card'>");
        html.append("<h2>Accéder aux statistiques</h2>");
        html.append("<p>Pour accéder aux statistiques des examens:</p>");
        html.append("<ol>");
        html.append("<li>Accédez à la section \"Gestion des Examens\" dans le menu principal</li>");
        html.append("<li>Cliquez sur le bouton \"Statistiques\" dans la barre d'outils</li>");
        html.append("<li>Ou cliquez sur le bouton \"Tableau de bord\" pour voir les statistiques dans le contexte général</li>");
        html.append("</ol>");
        html.append("<p>Vous pouvez également utiliser le raccourci clavier <span class='badge'>Ctrl</span> + <span class='badge'>D</span> lorsque vous êtes dans la section Examens pour afficher le tableau de bord.</p>");
        html.append("</div>");

        html.append("<div class='section-card'>");
        html.append("<h2>Vue d'ensemble des statistiques</h2>");
        html.append("<p>Le tableau de bord des examens présente plusieurs indicateurs clés:</p>");

        html.append("<div class='grid-container'>");
        html.append("<div class='grid-item'>");
        html.append("<h3>Indicateurs généraux</h3>");
        html.append("<ul>");
        html.append("<li><strong>Nombre total d'examens</strong> - Tous les examens enregistrés</li>");
        html.append("<li><strong>Examens validés</strong> - Nombre d'examens réussis</li>");
        html.append("<li><strong>Examens en attente</strong> - Nombre d'examens à venir</li>");
        html.append("<li><strong>Taux de réussite global</strong> - Pourcentage de réussite tous examens confondus</li>");
        html.append("</ul>");
        html.append("</div>");

        html.append("<div class='grid-item'>");
        html.append("<h3>Graphiques et visualisations</h3>");
        html.append("<ul>");
        html.append("<li><strong>Répartition par type</strong> - Graphique circulaire montrant la répartition Code/Conduite</li>");
        html.append("<li><strong>Examens par mois</strong> - Graphique à barres montrant l'évolution mensuelle</li>");
        html.append("<li><strong>Taux de réussite par type</strong> - Comparaison des taux de réussite Code vs Conduite</li>");
        html.append("<li><strong>Taux de réussite par moniteur</strong> - Classement des moniteurs selon les résultats</li>");
        html.append("</ul>");
        html.append("</div>");
        html.append("</div>");

        html.append("<div class='info-box'>");
        html.append("<h3>Actualisation des données</h3>");
        html.append("<p>Les statistiques sont actualisées automatiquement à chaque ouverture. Vous pouvez également cliquer sur le bouton \"Actualiser\" pour mettre à jour manuellement les données.</p>");
        html.append("</div>");
        html.append("</div>");

        html.append("<div class='section-card'>");
        html.append("<h2>Filtrer les statistiques</h2>");
        html.append("<p>Vous pouvez affiner les statistiques affichées:</p>");
        html.append("<ul>");
        html.append("<li><strong>Période</strong> - Sélectionnez une période spécifique (mois en cours, trimestre, année, etc.)</li>");
        html.append("<li><strong>Type d'examen</strong> - Filtrez par type (Code, Conduite ou Tous)</li>");
        html.append("<li><strong>Statut</strong> - Filtrez par statut (Validé, Non validé, En attente, Tous)</li>");
        html.append("<li><strong>Moniteur</strong> - Filtrez par moniteur spécifique</li>");
        html.append("</ul>");
        html.append("<p>Après avoir sélectionné vos filtres, cliquez sur \"Appliquer\" pour mettre à jour les visualisations.</p>");

        html.append("<div class='tip-box'>");
        html.append("<h3>Comparaison</h3>");
        html.append("<p>Utilisez l'option \"Comparer avec la période précédente\" pour voir l'évolution des indicateurs par rapport à la période antérieure.</p>");
        html.append("</div>");
        html.append("</div>");

        html.append("<div class='section-card'>");
        html.append("<h2>Analyses avancées</h2>");
        html.append("<p>Le système propose également des analyses plus détaillées:</p>");

        html.append("<div class='grid-container'>");
        html.append("<div class='grid-item'>");
        html.append("<h3>Analyse des tendances</h3>");
        html.append("<ul>");
        html.append("<li><strong>Évolution du taux de réussite</strong> - Graphique linéaire montrant l'évolution dans le temps</li>");
        html.append("<li><strong>Prévisions</strong> - Estimation du nombre d'examens à venir</li>");
        html.append("<li><strong>Analyse saisonnière</strong> - Identification des périodes à forte/faible activité</li>");
        html.append("</ul>");
        html.append("</div>");

        html.append("<div class='grid-item'>");
        html.append("<h3>Analyse des facteurs de réussite</h3>");
        html.append("<ul>");
        html.append("<li><strong>Impact du moniteur</strong> - Corrélation entre moniteur et taux de réussite</li>");
        html.append("<li><strong>Impact du nombre de leçons</strong> - Corrélation entre nombre de leçons et réussite</li>");
        html.append("<li><strong>Impact du centre d'examen</strong> - Comparaison des taux de réussite par centre</li>");
        html.append("</ul>");
        html.append("</div>");
        html.append("</div>");

        html.append("<div class='success-box'>");
        html.append("<h3>Exportation des rapports</h3>");
        html.append("<p>Vous pouvez exporter l'ensemble des statistiques ou des graphiques spécifiques en cliquant sur le bouton \"Exporter\" et en choisissant le format souhaité (PDF, Excel, PNG).</p>");
        html.append("</div>");
        html.append("</div>");

        html.append("<div class='section-card'>");
        html.append("<h2>Rapports prédéfinis</h2>");
        html.append("<p>Le système propose plusieurs rapports prédéfinis pour faciliter l'analyse:</p>");
        html.append("<ul>");
        html.append("<li><strong>Rapport mensuel</strong> - Synthèse des examens du mois avec comparaison au mois précédent</li>");
        html.append("<li><strong>Rapport trimestriel</strong> - Analyse détaillée sur trois mois avec tendances</li>");
        html.append("<li><strong>Rapport par moniteur</strong> - Performance détaillée de chaque moniteur</li>");
        html.append("<li><strong>Rapport par catégorie</strong> - Analyse par catégorie de permis (A, B, C)</li>");
        html.append("</ul>");
        html.append("<p>Pour générer un rapport, cliquez sur le bouton \"Rapports\" puis sélectionnez le type de rapport souhaité.</p>");

        html.append("<div class='info-box'>");
        html.append("<h3>Rapports automatiques</h3>");
        html.append("<p>Vous pouvez configurer l'envoi automatique de rapports par email à une fréquence définie (hebdomadaire, mensuelle, etc.) dans les paramètres de l'application.</p>");
        html.append("</div>");
        html.append("</div>");

        html.append("</body></html>");
        return html.toString();
    }

    /**
     * Get HTML content for the Examens Dashboard section - NEW
     */
    private String getExamensDashboardContent() {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head>");
        html.append("<style>");
        html.append(getCommonStyles());
        html.append("</style>");
        html.append("</head><body>");

        html.append("<h1>Tableau de bord examens</h1>");
        html.append("<p>Cette section explique comment utiliser le tableau de bord des examens pour visualiser et analyser les données importantes concernant les examens.</p>");

        html.append("<div class='section-card'>");
        html.append("<h2>Accéder au tableau de bord</h2>");
        html.append("<p>Pour accéder au tableau de bord des examens:</p>");
        html.append("<ol>");
        html.append("<li>Accédez à la section \"Gestion des Examens\" dans le menu principal</li>");
        html.append("<li>Cliquez sur le bouton \"Tableau de bord\" dans la barre d'outils</li>");
        html.append("</ol>");
        html.append("<p>Vous pouvez également utiliser le raccourci clavier <span class='badge'>Ctrl</span> + <span class='badge'>D</span> lorsque vous êtes dans la section Examens.</p>");
        html.append("</div>");

        html.append("<div class='section-card'>");
        html.append("<h2>Vue d'ensemble</h2>");
        html.append("<p>Le tableau de bord présente plusieurs indicateurs clés:</p>");

        html.append("<div class='grid-container'>");
        html.append("<div class='grid-item'>");
        html.append("<h3>Statistiques générales</h3>");
        html.append("<ul>");
        html.append("<li><strong>Nombre total d'examens</strong> - Tous les examens enregistrés</li>");
        html.append("<li><strong>Examens à venir</strong> - Examens planifiés pour les prochains jours</li>");
        html.append("<li><strong>Examens passés</strong> - Examens déjà effectués</li>");
        html.append("<li><strong>Taux de réussite global</strong> - Pourcentage de réussite tous examens confondus</li>");
        html.append("</ul>");
        html.append("</div>");

        html.append("<div class='grid-item'>");
        html.append("<h3>Graphiques et visualisations</h3>");
        html.append("<ul>");
        html.append("<li><strong>Répartition par type</strong> - Graphique circulaire montrant la répartition Code/Conduite</li>");
        html.append("<li><strong>Examens par mois</strong> - Graphique à barres montrant l'évolution mensuelle</li>");
        html.append("<li><strong>Taux de réussite par catégorie</strong> - Comparaison des résultats par type de permis</li>");
        html.append("</ul>");
        html.append("</div>");
        html.append("</div>");

        html.append("<div class='info-box'>");
        html.append("<h3>Actualisation des données</h3>");
        html.append("<p>Les données du tableau de bord sont actualisées automatiquement à chaque ouverture. Vous pouvez également cliquer sur le bouton \"Actualiser\" pour mettre à jour manuellement les statistiques.</p>");
        html.append("</div>");
        html.append("</div>");

        html.append("<div class='section-card'>");
        html.append("<h2>Calendrier des examens</h2>");
        html.append("<p>Le tableau de bord inclut un calendrier interactif des examens:</p>");
        html.append("<ul>");
        html.append("<li><strong>Vue mensuelle</strong> - Aperçu de tous les examens du mois</li>");
        html.append("<li><strong>Vue hebdomadaire</strong> - Planning détaillé de la semaine</li>");
        html.append("<li><strong>Vue journalière</strong> - Examens prévus pour une journée spécifique</li>");
        html.append("</ul>");
        html.append("<p>Chaque examen est représenté par un bloc coloré dans le calendrier:</p>");
        html.append("<ul>");
        html.append("<li><span style='color: blue;'>■</span> Bleu pour les examens de code</li>");
        html.append("<li><span style='color: green;'>■</span> Vert pour les examens de conduite</li>");
        html.append("</ul>");

        html.append("<div class='tip-box'>");
        html.append("<h3>Navigation rapide</h3>");
        html.append("<p>Cliquez sur un examen dans le calendrier pour afficher ses détails. Vous pouvez également faire glisser un examen pour modifier sa date (si vous avez les permissions nécessaires).</p>");
        html.append("</div>");
        html.append("</div>");

        html.append("<div class='section-card'>");
        html.append("<h2>Examens à venir</h2>");
        html.append("<p>Le tableau de bord affiche une liste des prochains examens:</p>");
        html.append("<ul>");
        html.append("<li><strong>Aujourd'hui</strong> - Examens prévus pour la journée en cours</li>");
        html.append("<li><strong>Cette semaine</strong> - Examens prévus pour les 7 prochains jours</li>");
        html.append("<li><strong>Ce mois</strong> - Examens prévus pour les 30 prochains jours</li>");
        html.append("</ul>");
        html.append("<p>Pour chaque examen, vous pouvez voir:</p>");
        html.append("<ul>");
        html.append("<li>Le nom du candidat</li>");
        html.append("<li>Le type d'examen</li>");
        html.append("<li>La date et l'heure</li>");
        html.append("<li>Le lieu</li>");
        html.append("<li>Le moniteur accompagnateur (si applicable)</li>");
        html.append("</ul>");

        html.append("<div class='success-box'>");
        html.append("<h3>Actions rapides</h3>");
        html.append("<p>Depuis cette liste, vous pouvez effectuer plusieurs actions rapides:</p>");
        html.append("<ul>");
        html.append("<li>Modifier un examen</li>");
        html.append("<li>Annuler un examen</li>");
        html.append("<li>Imprimer une convocation</li>");
        html.append("<li>Envoyer un rappel au candidat</li>");
        html.append("</ul>");
        html.append("</div>");
        html.append("</div>");

        html.append("<div class='section-card'>");
        html.append("<h2>Analyse des résultats</h2>");
        html.append("<p>Le tableau de bord propose une analyse détaillée des résultats d'examens:</p>");

        html.append("<div class='grid-container'>");
        html.append("<div class='grid-item'>");
        html.append("<h3>Taux de réussite</h3>");
        html.append("<ul>");
        html.append("<li><strong>Par type d'examen</strong> - Code vs Conduite</li>");
        html.append("<li><strong>Par catégorie de permis</strong> - A, B, C</li>");
        html.append("<li><strong>Par moniteur</strong> - Classement des moniteurs</li>");
        html.append("<li><strong>Par centre d'examen</strong> - Comparaison des centres</li>");
        html.append("</ul>");
        html.append("</div>");

        html.append("<div class='grid-item'>");
        html.append("<h3>Évolution dans le temps</h3>");
        html.append("<ul>");
        html.append("<li><strong>Tendance mensuelle</strong> - Évolution du taux de réussite</li>");
        html.append("<li><strong>Comparaison annuelle</strong> - Cette année vs année précédente</li>");
        html.append("<li><strong>Prévisions</strong> - Tendance future basée sur les données historiques</li>");
        html.append("</ul>");
        html.append("</div>");
        html.append("</div>");

        html.append("<div class='info-box'>");
        html.append("<h3>Exportation</h3>");
        html.append("<p>Vous pouvez exporter l'ensemble du tableau de bord ou des graphiques spécifiques en cliquant sur le bouton \"Exporter\" et en choisissant le format souhaité (PDF, Excel, PNG).</p>");
        html.append("</div>");
        html.append("</div>");

        html.append("</body></html>");
        return html.toString();
    }

    /**
     * Get HTML content for the Paiements Payments section
     */
    private String getPaiementsPaymentsContent() {
        // Implementation for paiements payments help content
        return getPlaceholderContent("Enregistrer un paiement");
    }

    /**
     * Get HTML content for the Paiements Invoices section
     */
    private String getPaiementsInvoicesContent() {
        // Implementation for paiements invoices help content
        return getPlaceholderContent("Factures et reçus");
    }

    /**
     * Get HTML content for the Paiements Reports section
     */
    private String getPaiementsReportsContent() {
        // Implementation for paiements reports help content
        return getPlaceholderContent("Rapports financiers");
    }

    /**
     * Get HTML content for the Settings General section
     */
    private String getSettingsGeneralContent() {
        // Implementation for settings general help content
        return getPlaceholderContent("Paramètres généraux");
    }

    /**
     * Get HTML content for the Settings Users section
     */
    private String getSettingsUsersContent() {
        // Implementation for settings users help content
        return getPlaceholderContent("Utilisateurs et permissions");
    }

    /**
     * Get HTML content for the Settings Backup section
     */
    private String getSettingsBackupContent() {
        // Implementation for settings backup help content
        return getPlaceholderContent("Sauvegarde et restauration");
    }

    /**
     * Get HTML content for the Not Found section
     */
    private String getNotFoundContent() {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head>");
        html.append("<style>");
        html.append(getCommonStyles());
        html.append("</style>");
        html.append("</head><body>");

        html.append("<h1>Section non trouvée</h1>");
        html.append("<p>La section demandée n'existe pas ou est en cours de développement.</p>");
        html.append("<p>Veuillez sélectionner une autre section dans le menu de gauche ou utiliser la recherche pour trouver l'information dont vous avez besoin.</p>");

        html.append("<div class='info-box'>");
        html.append("<h3>Besoin d'aide supplémentaire?</h3>");
        html.append("<p>Si vous ne trouvez pas l'information que vous cherchez, n'hésitez pas à contacter notre équipe de support:</p>");
        html.append("<ul>");
        html.append("<li>Email: <a href='mailto:support@auto-ecole.com'>support@auto-ecole.com</a></li>");
        html.append("<li>Téléphone: 01 23 45 67 89</li>");
        html.append("</ul>");
        html.append("</div>");

        html.append("</body></html>");
        return html.toString();
    }

    /**
     * Get placeholder content for sections not yet implemented
     */
    private String getPlaceholderContent(String title) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head>");
        html.append("<style>");
        html.append(getCommonStyles());
        html.append("</style>");
        html.append("</head><body>");

        html.append("<h1>" + title + "</h1>");
        html.append("<p>Cette section est en cours de développement. Le contenu complet sera disponible prochainement.</p>");

        html.append("<div class='info-box'>");
        html.append("<h3>Contenu à venir</h3>");
        html.append("<p>Cette section contiendra des informations détaillées sur " + title.toLowerCase() + ".</p>");
        html.append("<p>En attendant, vous pouvez consulter les autres sections de l'aide ou contacter notre équipe de support pour toute question spécifique.</p>");
        html.append("</div>");

        html.append("</body></html>");
        return html.toString();
    }

    /**
     * Get common CSS styles for all help content
     */
    private String getCommonStyles() {
        return """
            body {
                font-family: 'Segoe UI', Arial, sans-serif;
                line-height: 1.6;
                color: #333;
                margin: 0;
                padding: 20px;
                background-color: #f9f9f9;
            }
            
            .dark-mode {
                background-color: #1a1a1a;
                color: #e0e0e0;
            }
            
            .dark-mode h1, .dark-mode h2, .dark-mode h3 {
                color: #f0f0f0;
            }
            
            .dark-mode a {
                color: #4da3ff;
            }
            
            .dark-mode .card, .dark-mode .section-card, .dark-mode .step-card {
                background-color: #2a2a2a;
                border-color: #444;
            }
            
            .dark-mode .info-box {
                background-color: #1a365d;
                border-color: #2a4a7f;
            }
            
            .dark-mode .warning-box {
                background-color: #7f1d1d;
                border-color: #9f2f2f;
            }
            
            .dark-mode .success-box {
                background-color: #1a4731;
                border-color: #2a6745;
            }
            
            .dark-mode .tip-box {
                background-color: #7e2e00;
                border-color: #9e4e20;
            }
            
            h1 {
                color: #1a365d;
                border-bottom: 2px solid #4299e1;
                padding-bottom: 10px;
                margin-top: 0;
            }
            
            h2 {
                color: #2c5282;
                margin-top: 25px;
            }
            
            h3 {
                color: #2b6cb0;
            }
            
            a {
                color: #3182ce;
                text-decoration: none;
            }
            
            a:hover {
                text-decoration: underline;
            }
            
            ul, ol {
                padding-left: 20px;
            }
            
            li {
                margin-bottom: 5px;
            }
            
            .card, .section-card, .step-card {
                background-color: #fff;
                border: 1px solid #e2e8f0;
                border-radius: 8px;
                padding: 20px;
                margin-bottom: 20px;
                box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
            }
            
            .step-card {
                border-left: 4px solid #4299e1;
            }
            
            .grid-container {
                display: grid;
                grid-template-columns: 1fr;
                gap: 20px;
            }
            
            @media (min-width: 768px) {
                .grid-container {
                    grid-template-columns: 1fr 1fr;
                }
            }
            
            .grid-item {
                background-color: rgba(0, 0, 0, 0.02);
                padding: 15px;
                border-radius: 6px;
            }
            
            .info-box, .warning-box, .success-box, .tip-box {
                padding: 15px;
                border-radius: 6px;
                margin: 15px 0;
            }
            
            .info-box {
                background-color: #ebf8ff;
                border: 1px solid #bee3f8;
            }
            
            .warning-box {
                background-color: #fff5f5;
                border: 1px solid #fed7d7;
            }
            
            .success-box {
                background-color: #f0fff4;
                border: 1px solid #c6f6d5;
            }
            
            .tip-box {
                background-color: #fffaf0;
                border: 1px solid #feebc8;
            }
            
            .info-box h3 {
                color: #2b6cb0;
                margin-top: 0;
            }
            
            .warning-box h3 {
                color: #c53030;
                margin-top: 0;
            }
            
            .success-box h3 {
                color: #2f855a;
                margin-top: 0;
            }
            
            .tip-box h3 {
                color: #c05621;
                margin-top: 0;
            }
            
            .note {
                font-style: italic;
                color: #718096;
                font-size: 0.9em;
            }
            
            .feature-list {
                list-style-type: none;
                padding-left: 5px;
            }
            
            .feature-list li {
                margin-bottom: 10px;
                position: relative;
                padding-left: 25px;
            }
            
            .icon {
                display: inline-block;
                width: 18px;
                height: 18px;
                position: absolute;
                left: 0;
                top: 3px;
            }
            
            .icon.check::before {
                content: '✓';
                color: #38a169;
                font-weight: bold;
            }
            
            .icon.arrow::before {
                content: '→';
                color: #3182ce;
                font-weight: bold;
            }
            
            .badge {
                display: inline-block;
                background-color: #e2e8f0;
                color: #4a5568;
                padding: 2px 6px;
                border-radius: 4px;
                font-size: 0.85em;
                font-family: monospace;
            }
            
            .badge-a {
                background-color: #fed7d7;
                color: #c53030;
            }
            
            .badge-b {
                background-color: #bee3f8;
                color: #2b6cb0;
            }
            
            .badge-c {
                background-color: #c6f6d5;
                color: #2f855a;
            }
            
            .shortcut-table {
                width: 100%;
                border-collapse: collapse;
                margin: 15px 0;
            }
            
            .shortcut-table th, .shortcut-table td {
                padding: 8px 12px;
                text-align: left;
                border-bottom: 1px solid #e2e8f0;
            }
            
            .shortcut-table th {
                background-color: #f7fafc;
                font-weight: 600;
            }
            
            .button {
                display: inline-block;
                background-color: #4299e1;
                color: white;
                padding: 8px 16px;
                border-radius: 4px;
                font-weight: 500;
                text-decoration: none;
                transition: background-color 0.2s;
            }
            
            .button:hover {
                background-color: #3182ce;
                text-decoration: none;
            }
            
            .metadata-table {
                width: 100%;
                border-collapse: collapse;
            }
            
            .metadata-table tr {
                border-bottom: 1px solid #e2e8f0;
            }
            
            .metadata-table td {
                padding: 8px 0;
            }
            
            .metadata-table td:first-child {
                font-weight: 500;
                width: 40%;
            }
            
            .card-grid {
                display: grid;
                grid-template-columns: 1fr;
                gap: 15px;
            }
            
            @media (min-width: 768px) {
                .card-grid {
                    grid-template-columns: 1fr 1fr;
                }
            }
            
            .mini-card {
                background-color: #fff;
                border: 1px solid #e2e8f0;
                border-radius: 6px;
                padding: 15px;
                box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
            }
            
            .dark-mode .mini-card {
                background-color: #2a2a2a;
                border-color: #444;
            }
            
            .dark-mode .grid-item {
                background-color: rgba(255, 255, 255, 0.05);
            }
            
            .dark-mode .shortcut-table th {
                background-color: #2a2a2a;
            }
            
            .dark-mode .shortcut-table td, .dark-mode .shortcut-table th, .dark-mode .metadata-table tr {
                border-color: #444;
            }
            
            .dark-mode .badge {
                background-color: #4a5568;
                color: #e2e8f0;
            }
            
            .dark-mode .button {
                background-color: #3182ce;
            }
            
            .dark-mode .button:hover {
                background-color: #2b6cb0;
            }
        """;
    }
}

