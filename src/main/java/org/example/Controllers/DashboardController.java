package org.example.Controllers;

import com.itextpdf.kernel.events.IEventHandler;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import javafx.util.Duration;
import org.example.Service.*;
import org.example.Rep.*;
import org.example.Entities.*;
import org.example.Utils.NotificationManager;
import org.example.Utils.AlertItem;
import org.example.Utils.ExamItem;
import org.example.Utils.PaymentItem;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.Optional;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.element.Table;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.layout.element.Image;
import com.itextpdf.io.image.ImageDataFactory;
import java.sql.SQLException;
import org.example.Services.DrivingSchoolService;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Text;
import com.itextpdf.io.image.ImageDataFactory;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import com.itextpdf.kernel.pdf.canvas.draw.ILineDrawer;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.canvas.draw.DottedLine;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.element.LineSeparator;
import com.itextpdf.kernel.events.Event;
import com.itextpdf.kernel.events.IEventHandler;
import com.itextpdf.kernel.events.PdfDocumentEvent;

public class DashboardController implements Initializable {

    // Services
    private CandidatService candidatService;
    private MoniteurService moniteurService;
    private VehiculeService vehiculeService;
    private ExamenService examenService;
    private SeanceService seanceService;
    private PaiementService paiementService;
    private DepenseService depenseService;
    private ReparationService reparationService;

    // FXML Components - Overview Tab
    @FXML private Label totalCandidatsLabel;
    @FXML private Label activeCandidatsLabel;
    @FXML private Label inactiveCandidatsLabel;
    @FXML private Label totalMoniteursLabel;
    @FXML private Label availableMoniteursLabel;
    @FXML private Label unavailableMoniteursLabel;
    @FXML private Label totalVehiculesLabel;
    @FXML private Label availableVehiculesLabel;
    @FXML private Label unavailableVehiculesLabel;
    @FXML private Label totalExamensLabel;
    @FXML private Label examensReussisLabel;
    @FXML private Label examensEchouesLabel;
    @FXML private Label dateLabel;
    @FXML private Label timeframeLabel;
    @FXML private Label totalRevenueLabel;
    @FXML private Label revenueThisMonthLabel;
    @FXML private Label totalExpensesLabel;
    @FXML private Label expensesThisMonthLabel;
    @FXML private ProgressBar revenueProgressBar;
    @FXML private ProgressBar expensesProgressBar;
    @FXML private ComboBox<String> timeframeSelector;
    @FXML private TabPane dashboardTabs;
    @FXML private BarChart<String, Number> inscriptionsChart;
    @FXML private PieChart typeExamenChart;
    @FXML private VBox alertsContainer;
    @FXML private VBox upcomingExamsContainer;

    // FXML Components - Candidats Tab
    @FXML private Label totalCandidatsTab2Label;
    @FXML private Label activeCandidatsTab2Label;
    @FXML private Label inactiveCandidatsTab2Label;
    @FXML private Label candidatsActivePercentLabel;
    @FXML private Label inactiveCandidatsPercentLabel;
    @FXML private Label seancesThisMonthLabel;
    @FXML private ProgressBar candidatsActiveProgressBar;
    @FXML private BarChart<String, Number> inscriptionsTab2Chart;
    @FXML private PieChart categoriesChart;

    // FXML Components - Moniteurs Tab
    @FXML private Label totalMoniteursTab3Label;
    @FXML private Label availableMoniteursTab3Label;
    @FXML private Label unavailableMoniteursTab3Label;
    @FXML private Label moniteursAvailablePercentLabel;
    @FXML private ProgressBar moniteursAvailableProgressBar;
    @FXML private PieChart moniteursCategoriesChart;
    @FXML private BarChart<String, Number> moniteursWorkloadChart;

    // FXML Components - Véhicules Tab
    @FXML private Label totalVehiculesTab4Label;
    @FXML private Label maintenanceVehiculesLabel;
    @FXML private Label documentsExpiringLabel;
    @FXML private Label vehiculesAvailablePercentLabel;
    @FXML private ProgressBar vehiculesAvailableProgressBar;
    @FXML private PieChart vehiculesTypeChart;
    @FXML private BarChart<String, Number> vehiculesUsageChart;
    @FXML private VBox vehiculesAlertsContainer;

    // FXML Components - Finances Tab
    @FXML private Label totalRevenueTab5Label;
    @FXML private Label revenueThisMonthTab5Label;
    @FXML private Label totalExpensesTab5Label;
    @FXML private Label expensesThisMonthTab5Label;
    @FXML private Label totalProfitLabel;
    @FXML private Label profitThisMonthLabel;
    @FXML private LineChart<String, Number> revenueByMonthChart;
    @FXML private PieChart expensesByCategoryChart;
    @FXML private PieChart paymentMethodsChart;
    @FXML private VBox recentPaymentsContainer;

    // Buttons
    @FXML private Button backBtn;
    @FXML private Button refreshBtn;
    @FXML private Button exportBtn;
    @FXML private Button settingsBtn;
    @FXML private Button helpBtn;


    @FXML private Button pdfBtn;

    private DrivingSchoolService drivingSchoolService;


    // Current timeframe
    private String currentTimeframe = "month";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize services
        initializeServices();

        // Set current date
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        dateLabel.setText(LocalDate.now().format(dateFormat));

        // Initialize timeframe selector
        initializeTimeframeSelector();

        // Load dashboard data
        loadDashboardData();
    }

    private void initializeServices() {
        candidatService = new CandidatService(new CandidatRep());
        moniteurService = new MoniteurService(new MoniteurRep());
        vehiculeService = new VehiculeService(new VehiculeRep());
        examenService = new ExamenService(new ExamenRep());
        seanceService = new SeanceService(new SeanceRep(), new VehiculeRep(), new MoniteurRep());
        paiementService = new PaiementService(new PaiementRep());
        depenseService = new DepenseService(new DepenseRep());
        reparationService = new ReparationService(new ReparationRep(), new VehiculeRep());
        drivingSchoolService = new DrivingSchoolService();
    }

    // Update the timeframe selector to include "Depuis le début" option
    private void initializeTimeframeSelector() {
        ObservableList<String> timeframes = FXCollections.observableArrayList(
                "Cette semaine", "Ce mois", "Ce trimestre", "Cette année", "Depuis le début"
        );
        timeframeSelector.setItems(timeframes);
        timeframeSelector.setValue("Ce mois");
    }

    // Update the handleTimeframeChange method to handle the new option
    @FXML
    public void handleTimeframeChange(ActionEvent event) {
        String selectedTimeframe = timeframeSelector.getValue();
        switch (selectedTimeframe) {
            case "Cette semaine":
                currentTimeframe = "week";
                timeframeLabel.setText("Vue hebdomadaire");
                break;
            case "Ce mois":
                currentTimeframe = "month";
                timeframeLabel.setText("Vue mensuelle");
                break;
            case "Ce trimestre":
                currentTimeframe = "quarter";
                timeframeLabel.setText("Vue trimestrielle");
                break;
            case "Cette année":
                currentTimeframe = "year";
                timeframeLabel.setText("Vue annuelle");
                break;
            case "Depuis le début":
                currentTimeframe = "all";
                timeframeLabel.setText("Vue complète");
                break;
        }
        loadDashboardData();
    }

    private void loadDashboardData() {
        // Show loading indicator
        showLoadingIndicator(true);

        // Use CompletableFuture to load data asynchronously
        CompletableFuture.runAsync(() -> {
            try {
                // Load all data
                loadStatistics();
                loadCharts();
                loadAlerts();
                loadUpcomingExams();
                loadRecentPayments();

                // Update UI on JavaFX thread
                Platform.runLater(() -> {
                    updateStatisticsUI();
                    updateChartsUI();
                    updateAlertsUI();
                    updateUpcomingExamsUI();
                    updateRecentPaymentsUI();
                    showLoadingIndicator(false);
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    showLoadingIndicator(false);
                    NotificationManager.showError(getStage(), "Erreur",
                            "Une erreur est survenue lors du chargement des données: " + e.getMessage());
                });
            }
        });
    }

    private void showLoadingIndicator(boolean show) {
        // Implement loading indicator logic here
        // For example, show/hide a loading pane or spinner
    }

    // Fix the loadStatistics method to respect the timeframe
    private void loadStatistics() {
        try {
            // Get start date based on timeframe
            LocalDate startDate;
            LocalDate now = LocalDate.now();

            switch (currentTimeframe) {
                case "week":
                    startDate = now.minusDays(now.getDayOfWeek().getValue() - 1);
                    break;
                case "month":
                    startDate = now.withDayOfMonth(1);
                    break;
                case "quarter":
                    int currentMonth = now.getMonthValue();
                    int quarterStartMonth = ((currentMonth - 1) / 3) * 3 + 1;
                    startDate = now.withMonth(quarterStartMonth).withDayOfMonth(1);
                    break;
                case "year":
                    startDate = now.withDayOfYear(1);
                    break;
                case "all":
                    startDate = LocalDate.of(2000, 1, 1);
                    break;
                default:
                    startDate = now.withDayOfMonth(1);
            }

            // Candidats statistics
            List<Candidat> allCandidats = candidatService.getAllCandidats();
            int totalCandidats = allCandidats.size();
            int activeCandidats = (int) allCandidats.stream().filter(c -> c.isActif()).count();
            int inactiveCandidats = totalCandidats - activeCandidats;

            // Moniteurs statistics
            List<Moniteur> allMoniteurs = moniteurService.getAllMoniteurs();
            int totalMoniteurs = allMoniteurs.size();
            int availableMoniteurs = (int) allMoniteurs.stream().filter(m -> m.isDisponible()).count();
            int unavailableMoniteurs = totalMoniteurs - availableMoniteurs;

            // Vehicules statistics
            List<Vehicule> allVehicules = vehiculeService.getAllVehicules();
            int totalVehicules = allVehicules.size();
            int availableVehicules = (int) allVehicules.stream().filter(v -> v.isDisponible()).count();
            int unavailableVehicules = totalVehicules - availableVehicules;

            // Examens statistics - filter by timeframe
            List<Examen> allExamens = examenService.getAllExamens().stream()
                    .filter(e -> e.getDateExamen().isEqual(startDate) || e.getDateExamen().isAfter(startDate))
                    .collect(Collectors.toList());
            int totalExamens = allExamens.size();
            int examensReussis = (int) allExamens.stream().filter(e -> e.isEstValide()).count();
            int examensEchoues = totalExamens - examensReussis;

            // Maintenance and documents statistics
            int vehiculesNeedingMaintenance = (int) allVehicules.stream()
                    .filter(v -> v.getDateProchainEntretien() != null &&
                            v.getDateProchainEntretien().isBefore(LocalDate.now().plusDays(7)))
                    .count();

            int documentsExpiringSoon = (int) allVehicules.stream()
                    .filter(v -> v.hasExpiringDocuments(30))
                    .count();

            // Financial statistics - filter by timeframe
            List<Paiement> allPaiements = paiementService.getAllPaiements().stream()
                    .filter(p -> p.getDatePaiement().isEqual(startDate) || p.getDatePaiement().isAfter(startDate))
                    .collect(Collectors.toList());

            List<Depense> allDepenses = depenseService.getAllDepenses().stream()
                    .filter(d -> d.getDateDepense().isEqual(startDate) || d.getDateDepense().isAfter(startDate))
                    .collect(Collectors.toList());

            double totalRevenue = allPaiements.stream().mapToDouble(p -> p.getMontant()).sum();
            double totalExpenses = allDepenses.stream().mapToDouble(d -> d.getMontant()).sum();

            // This month's financial data
            LocalDate firstDayOfMonth = LocalDate.now().withDayOfMonth(1);

            double revenueThisMonth = allPaiements.stream()
                    .filter(p -> p.getDatePaiement().isAfter(firstDayOfMonth) || p.getDatePaiement().isEqual(firstDayOfMonth))
                    .mapToDouble(p -> p.getMontant())
                    .sum();

            double expensesThisMonth = allDepenses.stream()
                    .filter(d -> d.getDateDepense().isAfter(firstDayOfMonth) || d.getDateDepense().isEqual(firstDayOfMonth))
                    .mapToDouble(d -> d.getMontant())
                    .sum();

            // Sessions this month - filter by timeframe
            List<Seance> allSessions = seanceService.getAllSeances().stream()
                    .filter(s -> s.getDate_debut().toLocalDate().isEqual(startDate) ||
                            s.getDate_debut().toLocalDate().isAfter(startDate))
                    .collect(Collectors.toList());

            int seancesThisMonth = (int) allSessions.stream()
                    .filter(s -> s.getDate_debut().toLocalDate().isAfter(firstDayOfMonth) ||
                            s.getDate_debut().toLocalDate().isEqual(firstDayOfMonth))
                    .count();

            // Store statistics for UI update
            this.statistics = new HashMap<>();
            this.statistics.put("totalCandidats", totalCandidats);
            this.statistics.put("activeCandidats", activeCandidats);
            this.statistics.put("inactiveCandidats", inactiveCandidats);
            this.statistics.put("totalMoniteurs", totalMoniteurs);
            this.statistics.put("availableMoniteurs", availableMoniteurs);
            this.statistics.put("unavailableMoniteurs", unavailableMoniteurs);
            this.statistics.put("totalVehicules", totalVehicules);
            this.statistics.put("availableVehicules", availableVehicules);
            this.statistics.put("unavailableVehicules", unavailableVehicules);
            this.statistics.put("totalExamens", totalExamens);
            this.statistics.put("examensReussis", examensReussis);
            this.statistics.put("examensEchoues", examensEchoues);
            this.statistics.put("vehiculesNeedingMaintenance", vehiculesNeedingMaintenance);
            this.statistics.put("documentsExpiringSoon", documentsExpiringSoon);
            this.statistics.put("totalRevenue", totalRevenue);
            this.statistics.put("revenueThisMonth", revenueThisMonth);
            this.statistics.put("totalExpenses", totalExpenses);
            this.statistics.put("expensesThisMonth", expensesThisMonth);
            this.statistics.put("seancesThisMonth", seancesThisMonth);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors du chargement des statistiques: " + e.getMessage());
        }
    }

    // Store statistics for UI update
    private Map<String, Object> statistics;

    private void updateStatisticsUI() {
        // Overview Tab
        totalCandidatsLabel.setText(String.valueOf(statistics.get("totalCandidats")));
        activeCandidatsLabel.setText(String.valueOf(statistics.get("activeCandidats")) + " actifs");
        inactiveCandidatsLabel.setText(String.valueOf(statistics.get("inactiveCandidats")) + " inactifs");

        totalMoniteursLabel.setText(String.valueOf(statistics.get("totalMoniteurs")));
        availableMoniteursLabel.setText(String.valueOf(statistics.get("availableMoniteurs")) + " disponibles");
        unavailableMoniteursLabel.setText(String.valueOf(statistics.get("unavailableMoniteurs")) + " indisponibles");

        totalVehiculesLabel.setText(String.valueOf(statistics.get("totalVehicules")));
        availableVehiculesLabel.setText(String.valueOf(statistics.get("availableVehicules")) + " disponibles");
        unavailableVehiculesLabel.setText(String.valueOf(statistics.get("unavailableVehicules")) + " indisponibles");

        totalExamensLabel.setText(String.valueOf(statistics.get("totalExamens")));
        examensReussisLabel.setText(String.valueOf(statistics.get("examensReussis")) + " réussis");
        examensEchouesLabel.setText(String.valueOf(statistics.get("examensEchoues")) + " échoués");

        // Financial data
        double totalRevenue = (double) statistics.get("totalRevenue");
        double revenueThisMonth = (double) statistics.get("revenueThisMonth");
        double totalExpenses = (double) statistics.get("totalExpenses");
        double expensesThisMonth = (double) statistics.get("expensesThisMonth");

        totalRevenueLabel.setText(String.format("%,.0f DT", totalRevenue));
        revenueThisMonthLabel.setText(String.format("%,.0f DT ce mois", revenueThisMonth));
        totalExpensesLabel.setText(String.format("%,.0f DT", totalExpenses));
        expensesThisMonthLabel.setText(String.format("%,.0f DT ce mois", expensesThisMonth));

        // Progress bars
        revenueProgressBar.setProgress(revenueThisMonth / (totalRevenue > 0 ? totalRevenue : 1));
        expensesProgressBar.setProgress(expensesThisMonth / (totalExpenses > 0 ? totalExpenses : 1));

        // Candidats Tab
        totalCandidatsTab2Label.setText(String.valueOf(statistics.get("totalCandidats")));
        activeCandidatsTab2Label.setText(String.valueOf(statistics.get("activeCandidats")));
        inactiveCandidatsTab2Label.setText(String.valueOf(statistics.get("inactiveCandidats")));

        int totalCandidats = (int) statistics.get("totalCandidats");
        int activeCandidats = (int) statistics.get("activeCandidats");
        double activePercent = totalCandidats > 0 ? (double) activeCandidats / totalCandidats : 0;
        candidatsActiveProgressBar.setProgress(activePercent);
        candidatsActivePercentLabel.setText(String.format("%.0f%% actifs", activePercent * 100));
        inactiveCandidatsPercentLabel.setText(String.format("%.0f%% du total", (1 - activePercent) * 100));

        seancesThisMonthLabel.setText(String.valueOf(statistics.get("seancesThisMonth")) + " séances ce mois");

        // Moniteurs Tab
        totalMoniteursTab3Label.setText(String.valueOf(statistics.get("totalMoniteurs")));
        availableMoniteursTab3Label.setText(String.valueOf(statistics.get("availableMoniteurs")));
        unavailableMoniteursTab3Label.setText(String.valueOf(statistics.get("unavailableMoniteurs")));

        int totalMoniteurs = (int) statistics.get("totalMoniteurs");
        int availableMoniteurs = (int) statistics.get("availableMoniteurs");
        double availablePercent = totalMoniteurs > 0 ? (double) availableMoniteurs / totalMoniteurs : 0;
        moniteursAvailableProgressBar.setProgress(availablePercent);
        moniteursAvailablePercentLabel.setText(String.format("%.0f%% disponibles", availablePercent * 100));

        // Véhicules Tab
        totalVehiculesTab4Label.setText(String.valueOf(statistics.get("totalVehicules")));
        maintenanceVehiculesLabel.setText(String.valueOf(statistics.get("vehiculesNeedingMaintenance")));
        documentsExpiringLabel.setText(String.valueOf(statistics.get("documentsExpiringSoon")));

        int totalVehicules = (int) statistics.get("totalVehicules");
        int availableVehicules = (int) statistics.get("availableVehicules");
        double vehiclesAvailablePercent = totalVehicules > 0 ? (double) availableVehicules / totalVehicules : 0;
        vehiculesAvailableProgressBar.setProgress(vehiclesAvailablePercent);
        vehiculesAvailablePercentLabel.setText(String.format("%.0f%% disponibles", vehiclesAvailablePercent * 100));

        // Finances Tab
        totalRevenueTab5Label.setText(String.format("%,.0f DT", totalRevenue));
        revenueThisMonthTab5Label.setText(String.format("%,.0f DT ce mois", revenueThisMonth));
        totalExpensesTab5Label.setText(String.format("%,.0f DT", totalExpenses));
        expensesThisMonthTab5Label.setText(String.format("%,.0f DT ce mois", expensesThisMonth));

        double totalProfit = totalRevenue - totalExpenses;
        double profitThisMonth = revenueThisMonth - expensesThisMonth;
        totalProfitLabel.setText(String.format("%,.0f DT", totalProfit));
        profitThisMonthLabel.setText(String.format("%,.0f DT ce mois", profitThisMonth));
    }

    private void loadCharts() {
        try {
            // Generate chart data
            this.chartData = new HashMap<>();

            // Inscriptions par mois
            this.chartData.put("inscriptionsData", generateInscriptionsData());

            // Répartition des examens par type
            this.chartData.put("examTypeData", generateExamTypeData());

            // Répartition par catégorie de permis
            this.chartData.put("licenseCategories", generateLicenseCategoriesData());

            // Charge de travail des moniteurs
            this.chartData.put("moniteursWorkload", generateMoniteursWorkloadData());

            // Répartition des véhicules par type
            this.chartData.put("vehicleTypes", generateVehicleTypesData());

            // Utilisation des véhicules
            this.chartData.put("vehicleUsage", generateVehicleUsageData());

            // Revenus par mois
            this.chartData.put("revenueByMonth", generateRevenueByMonthData());

            // Dépenses par catégorie
            this.chartData.put("expensesByCategory", generateExpensesByCategoryData());

            // Méthodes de paiement
            this.chartData.put("paymentMethods", generatePaymentMethodsData());

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors du chargement des données des graphiques: " + e.getMessage());
        }
    }

    // Store chart data for UI update
    private Map<String, Object> chartData;

    @SuppressWarnings("unchecked")
    private void updateChartsUI() {
        // Clear existing chart data
        inscriptionsChart.getData().clear();
        typeExamenChart.getData().clear();
        inscriptionsTab2Chart.getData().clear();
        categoriesChart.getData().clear();
        moniteursCategoriesChart.getData().clear();
        moniteursWorkloadChart.getData().clear();
        vehiculesTypeChart.getData().clear();
        vehiculesUsageChart.getData().clear();
        revenueByMonthChart.getData().clear();
        expensesByCategoryChart.getData().clear();
        paymentMethodsChart.getData().clear();

        // Revenue by month for Overview tab
        XYChart.Series<String, Number> revenueSeries = new XYChart.Series<>();
        revenueSeries.setName("Revenus");

        List<Map<String, Object>> revenueData = (List<Map<String, Object>>) chartData.get("revenueByMonth");
        for (Map<String, Object> data : revenueData) {
            revenueSeries.getData().add(new XYChart.Data<>(
                    (String) data.get("month"),
                    (Number) data.get("amount")
            ));
        }

        inscriptionsChart.getData().add(revenueSeries);

        // Keep inscriptions data for Candidats tab
        XYChart.Series<String, Number> inscriptionsSeries = new XYChart.Series<>();
        inscriptionsSeries.setName("Inscriptions");

        List<Map<String, Object>> inscriptionsData = (List<Map<String, Object>>) chartData.get("inscriptionsData");
        for (Map<String, Object> data : inscriptionsData) {
            inscriptionsSeries.getData().add(new XYChart.Data<>(
                    (String) data.get("month"),
                    (Number) data.get("count")
            ));
        }

        inscriptionsTab2Chart.getData().add(inscriptionsSeries);

        // Répartition des examens par type
        // For typeExamenChart
        typeExamenChart.setData(FXCollections.observableArrayList());
        ObservableList<PieChart.Data> examTypeData = FXCollections.observableArrayList();
        List<Map<String, Object>> examTypes = (List<Map<String, Object>>) chartData.get("examTypeData");
        for (Map<String, Object> data : examTypes) {
            examTypeData.add(new PieChart.Data(
                    (String) data.get("type"),
                    (double) data.get("percentage")
            ));
        }
        typeExamenChart.setData(examTypeData);

        // Répartition par catégorie de permis
        // For categoriesChart
        categoriesChart.setData(FXCollections.observableArrayList());
        ObservableList<PieChart.Data> licenseData = FXCollections.observableArrayList();
        List<Map<String, Object>> licenseCategories = (List<Map<String, Object>>) chartData.get("licenseCategories");
        for (Map<String, Object> data : licenseCategories) {
            licenseData.add(new PieChart.Data(
                    (String) data.get("category"),
                    (double) data.get("percentage")
            ));
        }
        categoriesChart.setData(licenseData);

        // For moniteursCategoriesChart - create a new data set
        moniteursCategoriesChart.setData(FXCollections.observableArrayList());
        ObservableList<PieChart.Data> moniteursCatData = FXCollections.observableArrayList();
        for (Map<String, Object> data : licenseCategories) {
            moniteursCatData.add(new PieChart.Data(
                    (String) data.get("category"),
                    (double) data.get("percentage")
            ));
        }
        moniteursCategoriesChart.setData(moniteursCatData);

        // Charge de travail des moniteurs

        // Répartition des véhicules par type
        // For vehiculesTypeChart
        vehiculesTypeChart.setData(FXCollections.observableArrayList());
        ObservableList<PieChart.Data> vehicleTypeData = FXCollections.observableArrayList();
        List<Map<String, Object>> vehicleTypes = (List<Map<String, Object>>) chartData.get("vehicleTypes");
        for (Map<String, Object> data : vehicleTypes) {
            vehicleTypeData.add(new PieChart.Data(
                    (String) data.get("type"),
                    (double) data.get("percentage")
            ));
        }
        vehiculesTypeChart.setData(vehicleTypeData);

        // Utilisation des véhicules

        // Revenus par mois

        // Dépenses par catégorie
        // For expensesByCategoryChart
        expensesByCategoryChart.setData(FXCollections.observableArrayList());
        ObservableList<PieChart.Data> expensesData = FXCollections.observableArrayList();
        List<Map<String, Object>> expensesByCategory = (List<Map<String, Object>>) chartData.get("expensesByCategory");
        for (Map<String, Object> data : expensesByCategory) {
            expensesData.add(new PieChart.Data(
                    (String) data.get("category"),
                    (double) data.get("percentage")
            ));
        }
        expensesByCategoryChart.setData(expensesData);

        // Méthodes de paiement
        // For paymentMethodsChart
        paymentMethodsChart.setData(FXCollections.observableArrayList());
        ObservableList<PieChart.Data> paymentMethodsData = FXCollections.observableArrayList();
        List<Map<String, Object>> paymentMethods = (List<Map<String, Object>>) chartData.get("paymentMethods");
        for (Map<String, Object> data : paymentMethods) {
            paymentMethodsData.add(new PieChart.Data(
                    (String) data.get("method"),
                    (double) data.get("percentage")
            ));
        }
        paymentMethodsChart.setData(paymentMethodsData);

        XYChart.Series<String, Number> workloadSeries = new XYChart.Series<>();
        workloadSeries.setName("Séances");

        List<Map<String, Object>> moniteursWorkload = (List<Map<String, Object>>) chartData.get("moniteursWorkload");
        for (Map<String, Object> data : moniteursWorkload) {
            workloadSeries.getData().add(new XYChart.Data<>(
                    (String) data.get("week"),
                    (Number) data.get("count")
            ));
        }

        moniteursWorkloadChart.getData().add(workloadSeries);

        // Utilisation des véhicules
        XYChart.Series<String, Number> usageSeries = new XYChart.Series<>();
        usageSeries.setName("Heures");

        List<Map<String, Object>> vehicleUsage = (List<Map<String, Object>>) chartData.get("vehicleUsage");
        for (Map<String, Object> data : vehicleUsage) {
            usageSeries.getData().add(new XYChart.Data<>(
                    (String) data.get("type"),
                    (Number) data.get("hours")
            ));
        }

        vehiculesUsageChart.getData().add(usageSeries);

        // Revenus par mois
        revenueSeries = new XYChart.Series<>();
        revenueSeries.setName("Revenus");

        List<Map<String, Object>> revenueByMonth = (List<Map<String, Object>>) chartData.get("revenueByMonth");
        for (Map<String, Object> data : revenueByMonth) {
            revenueSeries.getData().add(new XYChart.Data<>(
                    (String) data.get("month"),
                    (Number) data.get("amount")
            ));
        }

        revenueByMonthChart.getData().add(revenueSeries);
    }

    // Modify the generateInscriptionsData method to filter by timeframe
    private List<Map<String, Object>> generateInscriptionsData() {
        List<Map<String, Object>> result = new ArrayList<>();

        // Get all candidates
        List<Candidat> candidats = candidatService.getAllCandidats();

        // Define month names
        String[] monthNames = {"Jan", "Fév", "Mar", "Avr", "Mai", "Juin", "Juil", "Août", "Sep", "Oct", "Nov", "Déc"};

        // Initialize all months with 0
        Map<String, Integer> countByMonth = new LinkedHashMap<>();
        for (String month : monthNames) {
            countByMonth.put(month, 0);
        }

        // Get start date based on timeframe
        LocalDate startDate;
        LocalDate now = LocalDate.now();

        switch (currentTimeframe) {
            case "week":
                // Start from the beginning of the current week (Monday)
                startDate = now.minusDays(now.getDayOfWeek().getValue() - 1);
                break;
            case "month":
                // Start from the beginning of the current month
                startDate = now.withDayOfMonth(1);
                break;
            case "quarter":
                // Start from the beginning of the current quarter
                int currentMonth = now.getMonthValue();
                int quarterStartMonth = ((currentMonth - 1) / 3) * 3 + 1;
                startDate = now.withMonth(quarterStartMonth).withDayOfMonth(1);
                break;
            case "year":
                // Start from the beginning of the current year
                startDate = now.withDayOfYear(1);
                break;
            case "all":
                // Start from a very early date to include all data
                startDate = LocalDate.of(2000, 1, 1);
                break;
            default:
                startDate = now.withDayOfMonth(1); // Default to month
        }

        // Count inscriptions by month, filtered by timeframe
        for (Candidat candidat : candidats) {
            if (candidat.getDateInscription() != null &&
                    (candidat.getDateInscription().isEqual(startDate) || candidat.getDateInscription().isAfter(startDate))) {
                int monthIndex = candidat.getDateInscription().getMonthValue() - 1;
                String monthName = monthNames[monthIndex];
                countByMonth.put(monthName, countByMonth.get(monthName) + 1);
            }
        }

        // Convert to result format
        for (Map.Entry<String, Integer> entry : countByMonth.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("month", entry.getKey());
            item.put("count", entry.getValue());
            result.add(item);
        }

        return result;
    }

    private List<Map<String, Object>> generateExamTypeData() {
        List<Map<String, Object>> result = new ArrayList<>();

        // Get all exams
        List<Examen> examens = examenService.getAllExamens();

        // Count by type
        Map<String, Integer> countByType = new HashMap<>();
        countByType.put("Code", 0);
        countByType.put("Conduite", 0);

        for (Examen examen : examens) {
            String type = examen.getTypeExamen();
            countByType.put(type, countByType.getOrDefault(type, 0) + 1);
        }

        // Calculate percentages
        int total = examens.size();

        // If no exams, return sample data
        if (total == 0) {
            Map<String, Object> codeItem = new HashMap<>();
            codeItem.put("type", "Code");
            codeItem.put("percentage", 45.0);
            result.add(codeItem);

            Map<String, Object> conduiteItem = new HashMap<>();
            conduiteItem.put("type", "Conduite");
            conduiteItem.put("percentage", 55.0);
            result.add(conduiteItem);

            return result;
        }

        // Convert to result format
        for (Map.Entry<String, Integer> entry : countByType.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("type", entry.getKey());

            item.put("percentage", (double) entry.getValue() / total * 100);
            result.add(item);
        }

        return result;
    }

    // Improve the generateLicenseCategoriesData method to better handle categories
    private List<Map<String, Object>> generateLicenseCategoriesData() {
        List<Map<String, Object>> result = new ArrayList<>();

        // Get all candidates
        List<Candidat> candidats = candidatService.getAllCandidats();

        // Count by category
        Map<String, Integer> countByCategory = new HashMap<>();

        // Initialize with common categories
        countByCategory.put("A", 0);
        countByCategory.put("B", 0);
        countByCategory.put("C", 0);

        int totalCategories = 0;

        for (Candidat candidat : candidats) {
            List<String> categories = candidat.getCategoriesPermis();
            if (categories != null && !categories.isEmpty()) {
                for (String category : categories) {
                    countByCategory.put(category, countByCategory.getOrDefault(category, 0) + 1);
                    totalCategories++;
                }
            }
        }

        // If no categories found, return sample data
        if (totalCategories == 0) {
            Map<String, Object> catA = new HashMap<>();
            catA.put("category", "A");
            catA.put("percentage", 25.0);
            result.add(catA);

            Map<String, Object> catB = new HashMap<>();
            catB.put("category", "B");
            catB.put("percentage", 60.0);
            result.add(catB);

            Map<String, Object> catC = new HashMap<>();
            catC.put("category", "C");
            catC.put("percentage", 15.0);
            result.add(catC);

            return result;
        }

        // Convert to result format
        for (Map.Entry<String, Integer> entry : countByCategory.entrySet()) {
            if (entry.getValue() > 0) {  // Only include categories that have at least one candidate
                Map<String, Object> item = new HashMap<>();
                item.put("category", entry.getKey());
                item.put("percentage", (double) entry.getValue() / totalCategories * 100);
                result.add(item);
            }
        }

        return result;
    }

    private List<Map<String, Object>> generateMoniteursWorkloadData() {
        List<Map<String, Object>> result = new ArrayList<>();

        // For demo purposes, return sample data
        Map<String, Object> week1 = new HashMap<>();
        week1.put("week", "Semaine 1");
        week1.put("count", 12);
        result.add(week1);

        Map<String, Object> week2 = new HashMap<>();
        week2.put("week", "Semaine 2");
        week2.put("count", 18);
        result.add(week2);

        Map<String, Object> week3 = new HashMap<>();
        week3.put("week", "Semaine 3");
        week3.put("count", 15);
        result.add(week3);

        Map<String, Object> week4 = new HashMap<>();
        week4.put("week", "Semaine 4");
        week4.put("count", 22);
        result.add(week4);

        return result;
    }

    private List<Map<String, Object>> generateVehicleTypesData() {
        List<Map<String, Object>> result = new ArrayList<>();

        // Get all vehicles
        List<Vehicule> vehicules = vehiculeService.getAllVehicules();

        // Count by type
        Map<TypePermis, Integer> countByType = new HashMap<>();

        for (Vehicule vehicule : vehicules) {
            TypePermis type = vehicule.getType();
            countByType.put(type, countByType.getOrDefault(type, 0) + 1);
        }

        // Calculate percentages
        int total = vehicules.size();

        // If no vehicles, return sample data
        if (total == 0) {
            Map<String, Object> moto = new HashMap<>();
            moto.put("type", "Moto");
            moto.put("percentage", 25.0);
            result.add(moto);

            Map<String, Object> voiture = new HashMap<>();
            voiture.put("type", "Voiture");
            voiture.put("percentage", 60.0);
            result.add(voiture);

            Map<String, Object> camion = new HashMap<>();
            camion.put("type", "Camion");
            camion.put("percentage", 15.0);
            result.add(camion);

            return result;
        }

        // Convert to result format
        for (Map.Entry<TypePermis, Integer> entry : countByType.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("type", entry.getKey().toString());
            item.put("percentage", (double) entry.getValue() / total * 100);
            result.add(item);
        }

        return result;
    }

    private List<Map<String, Object>> generateVehicleUsageData() {
        List<Map<String, Object>> result = new ArrayList<>();

        // For demo purposes, return sample data
        Map<String, Object> moto = new HashMap<>();
        moto.put("type", "Moto");
        moto.put("hours", 25);
        result.add(moto);

        Map<String, Object> voiture = new HashMap<>();
        voiture.put("type", "Voiture");
        voiture.put("hours", 45);
        result.add(voiture);

        Map<String, Object> camion = new HashMap<>();
        camion.put("type", "Camion");
        camion.put("hours", 18);
        result.add(camion);

        return result;
    }

    // Modify the generateRevenueByMonthData method
    private List<Map<String, Object>> generateRevenueByMonthData() {
        List<Map<String, Object>> result = new ArrayList<>();

        // Define month names
        String[] monthNames = {"Jan", "Fév", "Mar", "Avr", "Mai", "Juin", "Juil", "Août", "Sep", "Oct", "Nov", "Déc"};

        // Get all payments
        List<Paiement> paiements = paiementService.getAllPaiements();

        // Initialize all months with 0
        Map<String, Double> amountByMonth = new LinkedHashMap<>();
        for (String month : monthNames) {
            amountByMonth.put(month, 0.0);
        }

        // Get start date based on timeframe
        LocalDate startDate;
        LocalDate now = LocalDate.now();

        switch (currentTimeframe) {
            case "week":
                startDate = now.minusDays(now.getDayOfWeek().getValue() - 1);
                break;
            case "month":
                startDate = now.withDayOfMonth(1);
                break;
            case "quarter":
                int currentMonth = now.getMonthValue();
                int quarterStartMonth = ((currentMonth - 1) / 3) * 3 + 1;
                startDate = now.withMonth(quarterStartMonth).withDayOfMonth(1);
                break;
            case "year":
                startDate = now.withDayOfYear(1);
                break;
            case "all":
                startDate = LocalDate.of(2000, 1, 1);
                break;
            default:
                startDate = now.withDayOfMonth(1);
        }

        // Sum payments by month, filtered by timeframe
        for (Paiement paiement : paiements) {
            if (paiement.getDatePaiement() != null &&
                    (paiement.getDatePaiement().isEqual(startDate) || paiement.getDatePaiement().isAfter(startDate))) {
                int monthIndex = paiement.getDatePaiement().getMonthValue() - 1;
                String monthName = monthNames[monthIndex];
                amountByMonth.put(monthName, amountByMonth.get(monthName) + paiement.getMontant());
            }
        }

        // Convert to result format
        for (Map.Entry<String, Double> entry : amountByMonth.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("month", entry.getKey());
            item.put("amount", entry.getValue());
            result.add(item);
        }

        return result;
    }

    private List<Map<String, Object>> generateExpensesByCategoryData() {
        List<Map<String, Object>> result = new ArrayList<>();

        // Get all expenses
        List<Depense> depenses = depenseService.getAllDepenses();

        // Count by category
        Map<String, Double> amountByCategory = new HashMap<>();

        for (Depense depense : depenses) {
            String categorie = depense.getCategorie();
            amountByCategory.put(categorie, amountByCategory.getOrDefault(categorie, 0.0) + depense.getMontant());
        }

        // Calculate percentages
        double total = depenses.stream().mapToDouble(Depense::getMontant).sum();

        // If no expenses, return sample data
        if (total == 0) {
            Map<String, Object> carburant = new HashMap<>();
            carburant.put("category", "Carburant");
            carburant.put("percentage", 35.0);
            result.add(carburant);

            Map<String, Object> entretien = new HashMap<>();
            entretien.put("category", "Entretien");
            entretien.put("percentage", 25.0);
            result.add(entretien);

            Map<String, Object> salaires = new HashMap<>();
            salaires.put("category", "Salaires");
            salaires.put("percentage", 30.0);
            result.add(salaires);

            Map<String, Object> loyer = new HashMap<>();
            loyer.put("category", "Loyer");
            loyer.put("percentage", 10.0);
            result.add(loyer);

            return result;
        }

        // Convert to result format
        for (Map.Entry<String, Double> entry : amountByCategory.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("category", entry.getKey());
            item.put("percentage", entry.getValue() / total * 100);
            result.add(item);
        }

        return result;
    }

    private List<Map<String, Object>> generatePaymentMethodsData() {
        List<Map<String, Object>> result = new ArrayList<>();

        // Get all payments
        List<Paiement> paiements = paiementService.getAllPaiements();

        // Count by method
        Map<String, Integer> countByMethod = new HashMap<>();

        for (Paiement paiement : paiements) {
            String methode = paiement.getMethodePaiement();
            countByMethod.put(methode, countByMethod.getOrDefault(methode, 0) + 1);
        }

        // Calculate percentages
        int total = paiements.size();

        // If no payments, return sample data
        if (total == 0) {
            Map<String, Object> especes = new HashMap<>();
            especes.put("method", "Espèces");
            especes.put("percentage", 45.0);
            result.add(especes);

            Map<String, Object> carte = new HashMap<>();
            carte.put("method", "Carte");
            carte.put("percentage", 35.0);
            result.add(carte);

            Map<String, Object> virement = new HashMap<>();
            virement.put("method", "Virement");
            virement.put("percentage", 20.0);
            result.add(virement);

            return result;
        }

        // Convert to result format
        for (Map.Entry<String, Integer> entry : countByMethod.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("method", entry.getKey());
            item.put("percentage", (double) entry.getValue() / total * 100);
            result.add(item);
        }

        return result;
    }

    private void loadAlerts() {
        try {
            List<AlertItem> alerts = new ArrayList<>();

            // Get vehicles needing maintenance
            List<Vehicule> vehiculesNeedingMaintenance = vehiculeService.getAllVehicules().stream()
                    .filter(v -> v.getDateProchainEntretien() != null &&
                            v.getDateProchainEntretien().isBefore(LocalDate.now().plusDays(7)))
                    .collect(Collectors.toList());

            for (Vehicule v : vehiculesNeedingMaintenance) {
                DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                alerts.add(new AlertItem(
                        "maintenance",
                        v.getMarque() + " " + v.getModele() + " (" + v.getMatricule() + ")",
                        v.getDateProchainEntretien().format(dateFormat),
                        v.getDateProchainEntretien().isBefore(LocalDate.now()) ? "high" : "medium"
                ));
            }

            // Get vehicles with expiring documents
            List<Vehicule> vehiculesWithExpiringDocs = vehiculeService.getAllVehicules().stream()
                    .filter(v -> v.hasExpiringDocuments(30))
                    .collect(Collectors.toList());

            for (Vehicule v : vehiculesWithExpiringDocs) {
                DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy");

                // Check which document is expiring
                if (v.getDateAssurance() != null && v.isDocumentExpiring(v.getDateAssurance(), 30)) {
                    alerts.add(new AlertItem(
                            "document",
                            v.getMarque() + " " + v.getModele() + " (" + v.getMatricule() + ")",
                            "Assurance",
                            v.getDateAssurance().format(dateFormat),
                            v.getDateAssurance().isBefore(LocalDate.now()) ? "high" : "medium"
                    ));
                }

                if (v.getDateVignette() != null && v.isDocumentExpiring(v.getDateVignette(), 30)) {
                    alerts.add(new AlertItem(
                            "document",
                            v.getMarque() + " " + v.getModele() + " (" + v.getMatricule() + ")",
                            "Vignette",
                            v.getDateVignette().format(dateFormat),
                            v.getDateVignette().isBefore(LocalDate.now()) ? "high" : "medium"
                    ));
                }

                if (v.getDateVisiteTechnique() != null && v.isDocumentExpiring(v.getDateVisiteTechnique(), 30)) {
                    alerts.add(new AlertItem(
                            "document",
                            v.getMarque() + " " + v.getModele() + " (" + v.getMatricule() + ")",
                            "Visite technique",
                            v.getDateVisiteTechnique().format(dateFormat),
                            v.getDateVisiteTechnique().isBefore(LocalDate.now()) ? "high" : "medium"
                    ));
                }
            }

            // Store alerts for UI update
            this.alerts = alerts;

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors du chargement des alertes: " + e.getMessage());
        }
    }

    // Store alerts for UI update
    private List<AlertItem> alerts;

    private void updateAlertsUI() {
        // Clear existing alerts
        alertsContainer.getChildren().clear();
        vehiculesAlertsContainer.getChildren().clear();

        if (alerts.isEmpty()) {
            Label noAlertsLabel = new Label("Aucune alerte");
            noAlertsLabel.getStyleClass().add("no-data-label");
            alertsContainer.getChildren().add(noAlertsLabel);
            vehiculesAlertsContainer.getChildren().add(new Label("Aucune alerte"));
            return;
        }

        for (AlertItem alert : alerts) {
            HBox alertBox = createAlertBox(alert);
            alertsContainer.getChildren().add(alertBox);
            vehiculesAlertsContainer.getChildren().add(createAlertBox(alert)); // Create a new instance for the second container
        }
    }

    private HBox createAlertBox(AlertItem alert) {
        HBox alertBox = new HBox();
        alertBox.setSpacing(10);
        alertBox.setPadding(new Insets(10));
        alertBox.setAlignment(Pos.CENTER_LEFT);
        alertBox.getStyleClass().add("alert-item");

        if ("high".equals(alert.getSeverity())) {
            alertBox.getStyleClass().add("alert-high");
        } else {
            alertBox.getStyleClass().add("alert-medium");
        }

        // Icon
        StackPane iconContainer = new StackPane();
        iconContainer.getStyleClass().add("alert-icon-container");

        SVGPath icon = new SVGPath();
        if ("maintenance".equals(alert.getType())) {
            icon.setContent("M13.78 15.44L19.78 21.44L18.36 22.86L12.33 16.86L13.78 15.44M17.5 10A3.5 3.5 0 0 0 14 6.5C14 4.5 15.5 3 17.5 3S21 4.5 21 6.5 19.5 10 17.5 10M5 20V12H2L12 3L22 12H9V10.5L5 20M6 10V8H12V10H6Z");
        } else {
            icon.setContent("M14,2H6A2,2 0 0,0 4,4V20A2,2 0 0,0 6,22H18A2,2 0 0,0 20,20V8L14,2M18,20H6V4H13V9H18V20M10,19L12,15H9V10L7,14H10V19Z");
        }

        if ("high".equals(alert.getSeverity())) {
            icon.setFill(Color.RED);
        } else {
            icon.setFill(Color.ORANGE);
        }

        iconContainer.getChildren().add(icon);

        // Content
        VBox contentBox = new VBox();
        contentBox.setSpacing(5);
        HBox.setHgrow(contentBox, Priority.ALWAYS);

        Label vehicleLabel = new Label(alert.getVehicle());
        vehicleLabel.getStyleClass().add("alert-title");

        Label detailsLabel = new Label();
        detailsLabel.getStyleClass().add("alert-details");

        if ("maintenance".equals(alert.getType())) {
            detailsLabel.setText("Maintenance prévue: " + alert.getDate());
        } else {
            detailsLabel.setText(alert.getDocument() + " expire le: " + alert.getExpiry());
        }

        contentBox.getChildren().addAll(vehicleLabel, detailsLabel);

        // Badge
        Label badgeLabel = new Label("high".equals(alert.getSeverity()) ? "Urgent" : "À venir");
        badgeLabel.getStyleClass().addAll("alert-badge", "high".equals(alert.getSeverity()) ? "badge-high" : "badge-medium");

        alertBox.getChildren().addAll(iconContainer, contentBox, badgeLabel);

        return alertBox;
    }

    private void loadUpcomingExams() {
        try {
            List<ExamItem> upcomingExams = new ArrayList<>();

            // Get upcoming exams
            LocalDate twoWeeksLater = LocalDate.now().plusDays(14); // Next 14 days

            List<Examen> examens = examenService.getAllExamens().stream()
                    .filter(e -> e.getDateExamen().isAfter(LocalDate.now()) && e.getDateExamen().isBefore(twoWeeksLater))
                    .sorted(Comparator.comparing(Examen::getDateExamen))
                    .limit(5)
                    .collect(Collectors.toList());

            DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            for (Examen e : examens) {
                Optional<Candidat> candidatOpt = candidatService.getCandidatById(e.getCandidatId());
                if (!candidatOpt.isPresent()) continue;
                Candidat candidat = candidatOpt.get();
                upcomingExams.add(new ExamItem(
                        e.getId().intValue(),
                        candidat.getNom() + " " + candidat.getPrenom(),
                        e.getTypeExamen(),
                        e.getDateExamen().format(dateFormat),
                        e.getLieuExamen()
                ));
            }

            // Store upcoming exams for UI update
            this.upcomingExams = upcomingExams;

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors du chargement des examens à venir: " + e.getMessage());
        }
    }

    // Store upcoming exams for UI update
    private List<ExamItem> upcomingExams;

    private void updateUpcomingExamsUI() {
        // Clear existing exams
        upcomingExamsContainer.getChildren().clear();

        if (upcomingExams.isEmpty()) {
            Label noExamsLabel = new Label("Aucun examen à venir");
            noExamsLabel.getStyleClass().add("no-data-label");
            upcomingExamsContainer.getChildren().add(noExamsLabel);
            return;
        }

        for (ExamItem exam : upcomingExams) {
            HBox examBox = new HBox();
            examBox.setSpacing(10);
            examBox.setPadding(new Insets(10));
            examBox.setAlignment(Pos.CENTER_LEFT);
            examBox.getStyleClass().add("exam-item");

            // Icon
            StackPane iconContainer = new StackPane();
            iconContainer.getStyleClass().add("exam-icon-container");

            SVGPath icon = new SVGPath();
            if ("Code".equals(exam.getType())) {
                icon.setContent("M14,2H6A2,2 0 0,0 4,4V20A2,2 0 0,0 6,22H18A2,2 0 0,0 20,20V8L14,2M18,20H6V4H13V9H18V20M10,19L12,15H9V10L7,14H10V19Z");
            } else {
                icon.setContent("M18,18.5A1.5,1.5 0 0,1 16.5,17A1.5,1.5 0 0,1 18,15.5A1.5,1.5 0 0,1 19.5,17A1.5,1.5 0 0,1 18,18.5M19.5,9.5L21.46,12H17V9.5M6,18.5A1.5,1.5 0 0,1 4.5,17A1.5,1.5 0 0,1 6,15.5A1.5,1.5 0 0,1 7.5,17A1.5,1.5 0 0,1 6,18.5M20,8L23,12V17H21A3,3 0 0,1 18,20A3,3 0 0,1 15,17H9A3,3 0 0,1 6,20A3,3 0 0,1 3,17H1V6C1,4.89 1.89,4 3,4H17V8H20M8,6V9H5V11H8V14H10V11H13V9H10V6H8Z");
            }

            icon.setFill(Color.web("#bc0c0c"));
            iconContainer.getChildren().add(icon);

            // Content
            VBox contentBox = new VBox();
            contentBox.setSpacing(5);
            HBox.setHgrow(contentBox, Priority.ALWAYS);

            Label candidatLabel = new Label(exam.getCandidat());
            candidatLabel.getStyleClass().add("exam-title");

            Label detailsLabel = new Label(exam.getType() + " - " + exam.getDate() + " - " + exam.getLieu());
            detailsLabel.getStyleClass().add("exam-details");

            contentBox.getChildren().addAll(candidatLabel, detailsLabel);

            // Badge
            Label badgeLabel = new Label(exam.getType());
            badgeLabel.getStyleClass().addAll("exam-badge", "Code".equals(exam.getType()) ? "badge-code" : "badge-conduite");

            examBox.getChildren().addAll(iconContainer, contentBox, badgeLabel);

            upcomingExamsContainer.getChildren().add(examBox);
        }
    }

    private void loadRecentPayments() {
        try {
            List<PaymentItem> recentPayments = new ArrayList<>();

            // Get recent payments
            List<Paiement> paiements = paiementService.getAllPaiements().stream()
                    .sorted(Comparator.comparing(Paiement::getDatePaiement).reversed())
                    .limit(5)
                    .collect(Collectors.toList());

            DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            for (Paiement p : paiements) {
                Optional<Candidat> candidatOpt = candidatService.getCandidatById(p.getCandidatId());
                if (!candidatOpt.isPresent()) continue;
                Candidat candidat = candidatOpt.get();
                recentPayments.add(new PaymentItem(
                        p.getId().intValue(),
                        candidat.getNom() + " " + candidat.getPrenom(),
                        p.getMontant(),
                        p.getDatePaiement().format(dateFormat),
                        p.getMethodePaiement(),
                        p.getStatut()
                ));
            }

            // Store recent payments for UI update
            this.recentPayments = recentPayments;

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors du chargement des paiements récents: " + e.getMessage());
        }
    }

    // Store recent payments for UI update
    private List<PaymentItem> recentPayments;

    private void updateRecentPaymentsUI() {
        // Clear existing payments
        recentPaymentsContainer.getChildren().clear();

        if (recentPayments.isEmpty()) {
            Label noPaymentsLabel = new Label("Aucun paiement récent");
            noPaymentsLabel.getStyleClass().add("no-data-label");
            recentPaymentsContainer.getChildren().add(noPaymentsLabel);
            return;
        }

        for (PaymentItem payment : recentPayments) {
            HBox paymentBox = new HBox();
            paymentBox.setSpacing(10);
            paymentBox.setPadding(new Insets(10));
            paymentBox.setAlignment(Pos.CENTER_LEFT);
            paymentBox.getStyleClass().add("payment-item");

            // Icon
            StackPane iconContainer = new StackPane();
            iconContainer.getStyleClass().add("payment-icon-container");

            SVGPath icon = new SVGPath();
            icon.setContent("M20,8H4V6H20M20,18H4V12H20M20,4H4C2.89,4 2,4.89 2,6V18A2,2 0 0,0 4,20H20A2,2 0 0,0 22,18V6C22,4.89 21.1,4 20,4Z");
            icon.setFill(Color.web("#bc0c0c"));
            iconContainer.getChildren().add(icon);

            // Content
            VBox contentBox = new VBox();
            contentBox.setSpacing(5);
            HBox.setHgrow(contentBox, Priority.ALWAYS);

            Label candidatLabel = new Label(payment.getCandidat());
            candidatLabel.getStyleClass().add("payment-title");

            Label detailsLabel = new Label(payment.getDate() + " - " + payment.getMethode());
            detailsLabel.getStyleClass().add("payment-details");

            contentBox.getChildren().addAll(candidatLabel, detailsLabel);

            // Amount and Status
            VBox statusBox = new VBox();
            statusBox.setSpacing(5);
            statusBox.setAlignment(Pos.CENTER_RIGHT);

            Label amountLabel = new Label(String.format("%.0f DT", payment.getMontant()));
            amountLabel.getStyleClass().add("payment-amount");

            Label statusLabel = new Label(payment.getStatut());
            statusLabel.getStyleClass().addAll("payment-status", "COMPLET".equals(payment.getStatut()) ? "status-paid" : "status-pending");

            statusBox.getChildren().addAll(amountLabel, statusLabel);

            paymentBox.getChildren().addAll(iconContainer, contentBox, statusBox);

            recentPaymentsContainer.getChildren().add(paymentBox);
        }
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
    public void handleRefresh(ActionEvent event) {
        loadDashboardData();
    }

    @FXML
    public void handleExport(ActionEvent event) {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Exporter les données");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
            fileChooser.setInitialFileName("dashboard_data.csv");

            File file = fileChooser.showSaveDialog(getStage());
            if (file != null) {
                exportDataToCSV(file);
                NotificationManager.showInfo(getStage(), "Export réussi", "Les données ont été exportées avec succès.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            NotificationManager.showError(getStage(), "Erreur d'exportation", "Une erreur est survenue lors de l'exportation des données: " + e.getMessage());
        }
    }

    private void exportDataToCSV(File file) throws IOException {
        try (FileWriter writer = new FileWriter(file)) {
            // Write header
            writer.write("Catégorie,Métrique,Valeur\n");

            // Write statistics
            writer.write("Candidats,Total," + statistics.get("totalCandidats") + "\n");
            writer.write("Candidats,Actifs," + statistics.get("activeCandidats") + "\n");
            writer.write("Candidats,Inactifs," + statistics.get("inactiveCandidats") + "\n");

            writer.write("Moniteurs,Total," + statistics.get("totalMoniteurs") + "\n");
            writer.write("Moniteurs,Disponibles," + statistics.get("availableMoniteurs") + "\n");
            writer.write("Moniteurs,Indisponibles," + statistics.get("unavailableMoniteurs") + "\n");

            writer.write("Véhicules,Total," + statistics.get("totalVehicules") + "\n");
            writer.write("Véhicules,Disponibles," + statistics.get("availableVehicules") + "\n");
            writer.write("Véhicules,Indisponibles," + statistics.get("unavailableVehicules") + "\n");

            writer.write("Examens,Total," + statistics.get("totalExamens") + "\n");
            writer.write("Examens,Réussis," + statistics.get("examensReussis") + "\n");
            writer.write("Examens,Échoués," + statistics.get("examensEchoues") + "\n");

            writer.write("Finances,Revenus totaux," + statistics.get("totalRevenue") + "\n");
            writer.write("Finances,Revenus ce mois," + statistics.get("revenueThisMonth") + "\n");
            writer.write("Finances,Dépenses totales," + statistics.get("totalExpenses") + "\n");
            writer.write("Finances,Dépenses ce mois," + statistics.get("expensesThisMonth") + "\n");
        }
    }

    @FXML
    public void handleSettings(ActionEvent event) {
        try {
            // Load the settings FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/IHM/SettingsIHM.fxml"));
            Parent root = loader.load();

            // Create a new stage for the settings window
            Stage settingsStage = new Stage();
            settingsStage.setTitle("Paramètres - Auto-École Pro");

            // Set modality (makes the settings window modal)
            settingsStage.initModality(Modality.APPLICATION_MODAL);

            // Set owner (sets the parent window)
            settingsStage.initOwner(((Node) event.getSource()).getScene().getWindow());

            // Create the scene
            Scene scene = new Scene(root);
            settingsStage.setScene(scene);

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

    private Stage getStage() {
        return (Stage) backBtn.getScene().getWindow();
    }

    @FXML
    public void handleGeneratePDF(ActionEvent event) {
        try {
            // Fetch driving school information
            DrivingSchoolInfo schoolInfo = drivingSchoolService.getDrivingSchool();
            if (schoolInfo == null) {
                NotificationManager.showError(getStage(), "Erreur", "Impossible de récupérer les informations de l'auto-école.");
                return;
            }

            // Open a file chooser to select the save location
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Enregistrer le rapport PDF");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
            fileChooser.setInitialFileName("dashboard_report_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".pdf");

            File file = fileChooser.showSaveDialog(getStage());
            if (file == null) {
                return; // User canceled the dialog
            }

            // Create the PDF
            PdfWriter writer = new PdfWriter(file);
            PdfDocument pdf = new PdfDocument(writer);

            // Add page event for footer
            pdf.addEventHandler(PdfDocumentEvent.END_PAGE, new FooterEventHandler());

            Document document = new Document(pdf, PageSize.A4);
            document.setMargins(50, 36, 50, 36); // Top, right, bottom, left

            // Add Driving School Header
            addDrivingSchoolHeader(document, schoolInfo);

            // Add Dashboard Data
            addDashboardData(document);

            // Close the document
            document.close();

            NotificationManager.showInfo(getStage(), "Succès", "Le rapport PDF a été généré avec succès : " + file.getAbsolutePath());

        } catch (SQLException e) {
            e.printStackTrace();
            NotificationManager.showError(getStage(), "Erreur", "Erreur lors de la récupération des informations de l'auto-école : " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            NotificationManager.showError(getStage(), "Erreur", "Une erreur est survenue lors de la génération du PDF : " + e.getMessage());
        }
    }

    private void addDrivingSchoolHeader(Document document, DrivingSchoolInfo schoolInfo) throws IOException {
        // Create a table for the header (2 columns: logo and info)
        Table headerTable = new Table(UnitValue.createPercentArray(new float[]{20, 80}));
        headerTable.setWidth(UnitValue.createPercentValue(100));
        headerTable.setMarginBottom(20);

        // Add logo if available
        if (schoolInfo.getLogoPath() != null && !schoolInfo.getLogoPath().isEmpty()) {
            try {
                Image logo = new Image(ImageDataFactory.create(schoolInfo.getLogoPath()));
                logo.setWidth(60);
                logo.setHeight(60);
                Cell logoCell = new Cell()
                        .add(logo)
                        .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER)
                        .setVerticalAlignment(VerticalAlignment.MIDDLE);
                headerTable.addCell(logoCell);
            } catch (Exception e) {
                // If logo fails to load, add an empty cell
                headerTable.addCell(new Cell().setBorder(Border.NO_BORDER));
            }
        } else {
            headerTable.addCell(new Cell().setBorder(Border.NO_BORDER));
        }

        // Add driving school information
        Paragraph infoParagraph = new Paragraph();
        infoParagraph.add(new Text(schoolInfo.getName() + "\n").setBold().setFontSize(16));
        infoParagraph.add(new Text("Matricule Fiscale: " + schoolInfo.getMatriculeFiscale() + "\n").setFontSize(10));
        infoParagraph.add(new Text("Adresse: " + schoolInfo.getAddress() + "\n").setFontSize(10));
        infoParagraph.add(new Text("Téléphone: " + schoolInfo.getPhoneNumber() + "\n").setFontSize(10));
        infoParagraph.add(new Text("Email: " + schoolInfo.getEmail()).setFontSize(10));

        Cell infoCell = new Cell()
                .add(infoParagraph)
                .setBorder(Border.NO_BORDER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setTextAlignment(TextAlignment.RIGHT);
        headerTable.addCell(infoCell);

        document.add(headerTable);

        // Add a title for the report
        Paragraph title = new Paragraph("Rapport du Tableau de Bord")
                .setBold()
                .setFontSize(18)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(ColorConstants.DARK_GRAY)
                .setMarginBottom(10);
        document.add(title);

        // Add timeframe information
        Paragraph timeframe = new Paragraph("Période: " + timeframeLabel.getText())
                .setFontSize(12)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20);
        document.add(timeframe);

        // Add a separator line
        Table separator = new Table(UnitValue.createPercentArray(new float[]{100}));
        separator.setWidth(UnitValue.createPercentValue(100));
        separator.addCell(new Cell()
                .setHeight(2)
                .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                .setBorder(Border.NO_BORDER));
        document.add(separator);
    }

    private void addDashboardData(Document document) {
        // Add statistics sections using tables
        addSection(document, "Candidats", new String[][]{
                {"Total des Candidats", statistics.get("totalCandidats") + ""},
                {"Candidats Actifs", statistics.get("activeCandidats") + ""},
                {"Candidats Inactifs", statistics.get("inactiveCandidats") + ""}
        });

        addSection(document, "Moniteurs", new String[][]{
                {"Total des Moniteurs", statistics.get("totalMoniteurs") + ""},
                {"Moniteurs Disponibles", statistics.get("availableMoniteurs") + ""},
                {"Moniteurs Indisponibles", statistics.get("unavailableMoniteurs") + ""}
        });

        addSection(document, "Véhicules", new String[][]{
                {"Total des Véhicules", statistics.get("totalVehicules") + ""},
                {"Véhicules Disponibles", statistics.get("availableVehicules") + ""},
                {"Véhicules Indisponibles", statistics.get("unavailableVehicules") + ""},
                {"Véhicules Nécessitant une Maintenance", statistics.get("vehiculesNeedingMaintenance") + ""},
                {"Documents à Renouveler", statistics.get("documentsExpiringSoon") + ""}
        });

        addSection(document, "Examens", new String[][]{
                {"Total des Examens", statistics.get("totalExamens") + ""},
                {"Examens Réussis", statistics.get("examensReussis") + ""},
                {"Examens Échoués", statistics.get("examensEchoues") + ""}
        });

        addSection(document, "Finances", new String[][]{
                {"Revenus Totaux", String.format("%,.0f DT", (double) statistics.get("totalRevenue"))},
                {"Revenus ce Mois", String.format("%,.0f DT", (double) statistics.get("revenueThisMonth"))},
                {"Dépenses Totales", String.format("%,.0f DT", (double) statistics.get("totalExpenses"))},
                {"Dépenses ce Mois", String.format("%,.0f DT", (double) statistics.get("expensesThisMonth"))},
                {"Bénéfice Total", String.format("%,.0f DT", (double) statistics.get("totalRevenue") - (double) statistics.get("totalExpenses"))},
                {"Bénéfice ce Mois", String.format("%,.0f DT", (double) statistics.get("revenueThisMonth") - (double) statistics.get("expensesThisMonth"))}
        });
    }

    private void addSection(Document document, String sectionTitle, String[][] data) {
        // Add section title
        Paragraph title = new Paragraph(sectionTitle)
                .setBold()
                .setFontSize(14)
                .setFontColor(ColorConstants.WHITE)
                .setBackgroundColor(ColorConstants.DARK_GRAY)
                .setPadding(5)
                .setMarginTop(20)
                .setMarginBottom(5);
        document.add(title);

        // Create a table for the section data
        Table table = new Table(UnitValue.createPercentArray(new float[]{70, 30}));
        table.setWidth(UnitValue.createPercentValue(100));
        table.setMarginBottom(10);

        // Add table headers
        table.addHeaderCell(new Cell()
                .add(new Paragraph("Métrique").setBold())
                .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                .setPadding(5));
        table.addHeaderCell(new Cell()
                .add(new Paragraph("Valeur").setBold())
                .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                .setPadding(5));

        // Add data rows
        for (String[] row : data) {
            table.addCell(new Cell()
                    .add(new Paragraph(row[0]))
                    .setPadding(5));
            table.addCell(new Cell()
                    .add(new Paragraph(row[1]))
                    .setPadding(5));
        }

        document.add(table);
    }

    // Footer Event Handler for page numbers and date

}

class FooterEventHandler implements IEventHandler {
    @Override
    public void handleEvent(Event event) {
        PdfDocumentEvent docEvent = (PdfDocumentEvent) event;
        PdfDocument pdf = docEvent.getDocument();
        PdfPage page = docEvent.getPage();
        PdfCanvas pdfCanvas = new PdfCanvas(page);
        // Use the correct Canvas constructor with immediateFlush set to true
        Canvas canvas = new Canvas(pdfCanvas, new Rectangle(36, 20, page.getPageSize().getWidth() - 72, 30), true);

        // Add generation date on the left
        Paragraph leftFooter = new Paragraph("Généré le: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                .setFontSize(8)
                .setFontColor(ColorConstants.GRAY);
        canvas.add(leftFooter.setTextAlignment(TextAlignment.LEFT));

        // Add page number on the right
        Paragraph rightFooter = new Paragraph(String.format("Page %d / %d", pdf.getPageNumber(page), pdf.getNumberOfPages()))
                .setFontSize(8)
                .setFontColor(ColorConstants.GRAY);
        canvas.add(rightFooter.setTextAlignment(TextAlignment.RIGHT));

        canvas.close();
    }
}
