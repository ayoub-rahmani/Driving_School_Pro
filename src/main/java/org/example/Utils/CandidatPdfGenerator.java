package org.example.Utils;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.events.Event;
import com.itextpdf.kernel.events.IEventHandler;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.LineSeparator;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import com.itextpdf.io.image.ImageDataFactory;

import org.example.Entities.Candidat;
import org.example.Entities.DrivingSchoolInfo;
import org.example.Services.DrivingSchoolService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class CandidatPdfGenerator {

    private static final String DOCUMENTS_OUTPUT_DIR = "documents/generated/candidats/";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    static {
        // Ensure output directory exists
        try {
            Path dirPath = Paths.get(DOCUMENTS_OUTPUT_DIR);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Génère une fiche PDF pour un candidat
     * @param candidat Le candidat
     * @return Le chemin du fichier PDF généré
     * @throws IOException En cas d'erreur d'E/S
     */
    public static String generateCandidatFichePDF(Candidat candidat) throws IOException {
        if (candidat == null) {
            throw new IllegalArgumentException("Le candidat ne peut pas être null");
        }

        String fileName = DOCUMENTS_OUTPUT_DIR + "Fiche_Candidat_" + candidat.getId() + "_" +
                LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".pdf";

        // Récupérer les informations de l'auto-école
        DrivingSchoolInfo schoolInfo = null;
        try {
            DrivingSchoolService drivingSchoolService = new DrivingSchoolService();
            schoolInfo = drivingSchoolService.getDrivingSchool();
        } catch (Exception e) {
            e.printStackTrace();
            // Continuer avec des valeurs par défaut si les infos de l'auto-école ne sont pas disponibles
        }

        // Créer le document PDF
        PdfWriter writer = new PdfWriter(fileName);
        PdfDocument pdf = new PdfDocument(writer);

        // Ajouter un gestionnaire d'événements pour le pied de page
        pdf.addEventHandler(PdfDocumentEvent.END_PAGE, new FooterEventHandler());

        Document document = new Document(pdf, PageSize.A4);
        document.setMargins(50, 36, 50, 36); // Haut, droite, bas, gauche

        // Ajouter l'en-tête de l'auto-école
        addDrivingSchoolHeader(document, schoolInfo);

        // Ajouter le titre du document
        Paragraph title = new Paragraph("FICHE CANDIDAT")
                .setBold()
                .setFontSize(18)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(ColorConstants.DARK_GRAY)
                .setMarginBottom(20);
        document.add(title);

        // Ajouter une ligne séparatrice
        document.add(new LineSeparator(new com.itextpdf.kernel.pdf.canvas.draw.SolidLine(1))
                .setMarginBottom(20));

        // Créer une table pour les informations du candidat (2 colonnes: photo et infos)
        Table infoTable = new Table(UnitValue.createPercentArray(new float[]{30, 70}));
        infoTable.setWidth(UnitValue.createPercentValue(100));

        // Ajouter la photo du candidat si disponible
        Cell photoCell = new Cell();
        photoCell.setBorder(Border.NO_BORDER);
        photoCell.setVerticalAlignment(VerticalAlignment.TOP);

        if (candidat.getCheminPhotoIdentite() != null && !candidat.getCheminPhotoIdentite().isEmpty()) {
            try {
                File photoFile = new File(candidat.getCheminPhotoIdentite());
                if (photoFile.exists()) {
                    Image photo = new Image(ImageDataFactory.create(candidat.getCheminPhotoIdentite()));
                    photo.setWidth(100);
                    photo.setHeight(120);
                    photoCell.add(photo);
                } else {
                    photoCell.add(new Paragraph("Photo non disponible"));
                }
            } catch (Exception e) {
                photoCell.add(new Paragraph("Erreur de chargement de la photo"));
                e.printStackTrace();
            }
        } else {
            photoCell.add(new Paragraph("Photo non disponible"));
        }

        infoTable.addCell(photoCell);

        // Ajouter les informations du candidat
        Cell infoCell = new Cell();
        infoCell.setBorder(Border.NO_BORDER);
        infoCell.setVerticalAlignment(VerticalAlignment.TOP);

        // Informations personnelles
        infoCell.add(createInfoSection("Informations personnelles", new String[][] {
                {"Nom", candidat.getNom()},
                {"Prénom", candidat.getPrenom()},
                {"CIN", candidat.getCin()},
                {"Date de naissance", candidat.getDateNaissance().format(DATE_FORMATTER)},
                {"Adresse", candidat.getAdresse()},
                {"Téléphone", candidat.getTelephone()},
                {"Email", candidat.getEmail()}
        }));

        // Informations d'inscription
        infoCell.add(createInfoSection("Informations d'inscription", new String[][] {
                {"Date d'inscription", candidat.getDateInscription().format(DATE_FORMATTER)},
                {"Catégories de permis", String.join(", ", candidat.getCategoriesPermis())},
                {"Statut", candidat.isActif() ? "Actif" : "Inactif"}
        }));

        infoTable.addCell(infoCell);
        document.add(infoTable);

        // Ajouter une section pour les documents fournis
        document.add(new Paragraph("Documents fournis")
                .setBold()
                .setFontSize(14)
                .setMarginTop(20)
                .setMarginBottom(10));

        Table documentsTable = new Table(UnitValue.createPercentArray(new float[]{50, 50}));
        documentsTable.setWidth(UnitValue.createPercentValue(100));

        // Vérifier si les documents sont disponibles
        boolean hasCIN = candidat.getCheminPhotoCIN() != null && !candidat.getCheminPhotoCIN().isEmpty();
        boolean hasCertificat = candidat.getCheminCertificatMedical() != null && !candidat.getCheminCertificatMedical().isEmpty();

        documentsTable.addCell(createDocumentCell("Photo CIN", hasCIN));
        documentsTable.addCell(createDocumentCell("Certificat médical", hasCertificat));

        document.add(documentsTable);

        // Fermer le document
        document.close();

        return fileName;
    }

    /**
     * Génère une liste PDF des candidats actifs
     * @param candidats La liste des candidats
     * @return Le chemin du fichier PDF généré
     * @throws IOException En cas d'erreur d'E/S
     */
    public static String generateCandidatsListPDF(List<Candidat> candidats, String title) throws IOException {
        if (candidats == null || candidats.isEmpty()) {
            throw new IllegalArgumentException("La liste des candidats ne peut pas être vide");
        }

        String fileName = DOCUMENTS_OUTPUT_DIR + "Liste_Candidats_" +
                LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".pdf";

        // Récupérer les informations de l'auto-école
        DrivingSchoolInfo schoolInfo = null;
        try {
            DrivingSchoolService drivingSchoolService = new DrivingSchoolService();
            schoolInfo = drivingSchoolService.getDrivingSchool();
        } catch (Exception e) {
            e.printStackTrace();
            // Continuer avec des valeurs par défaut si les infos de l'auto-école ne sont pas disponibles
        }

        // Créer le document PDF
        PdfWriter writer = new PdfWriter(fileName);
        PdfDocument pdf = new PdfDocument(writer);

        // Ajouter un gestionnaire d'événements pour le pied de page
        pdf.addEventHandler(PdfDocumentEvent.END_PAGE, new FooterEventHandler());

        Document document = new Document(pdf, PageSize.A4);
        document.setMargins(50, 36, 50, 36); // Haut, droite, bas, gauche

        // Ajouter l'en-tête de l'auto-école
        addDrivingSchoolHeader(document, schoolInfo);

        // Ajouter le titre du document
        Paragraph docTitle = new Paragraph(title)
                .setBold()
                .setFontSize(18)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(ColorConstants.DARK_GRAY)
                .setMarginBottom(20);
        document.add(docTitle);

        // Ajouter une ligne séparatrice
        document.add(new LineSeparator(new com.itextpdf.kernel.pdf.canvas.draw.SolidLine(1))
                .setMarginBottom(20));

        // Ajouter le nombre total de candidats
        document.add(new Paragraph("Nombre total de candidats: " + candidats.size())
                .setFontSize(12)
                .setMarginBottom(10));

        // Créer une table pour la liste des candidats
        Table candidatsTable = new Table(UnitValue.createPercentArray(new float[]{5, 20, 20, 15, 15, 25}));
        candidatsTable.setWidth(UnitValue.createPercentValue(100));

        // Ajouter l'en-tête de la table
        candidatsTable.addHeaderCell(createHeaderCell("N°"));
        candidatsTable.addHeaderCell(createHeaderCell("Nom"));
        candidatsTable.addHeaderCell(createHeaderCell("Prénom"));
        candidatsTable.addHeaderCell(createHeaderCell("CIN"));
        candidatsTable.addHeaderCell(createHeaderCell("Téléphone"));
        candidatsTable.addHeaderCell(createHeaderCell("Catégories"));

        // Ajouter les données des candidats
        int counter = 1;
        for (Candidat candidat : candidats) {
            candidatsTable.addCell(createCell(String.valueOf(counter++)));
            candidatsTable.addCell(createCell(candidat.getNom()));
            candidatsTable.addCell(createCell(candidat.getPrenom()));
            candidatsTable.addCell(createCell(candidat.getCin()));
            candidatsTable.addCell(createCell(candidat.getTelephone()));
            candidatsTable.addCell(createCell(String.join(", ", candidat.getCategoriesPermis())));
        }

        document.add(candidatsTable);

        // Fermer le document
        document.close();

        return fileName;
    }

    /**
     * Génère une liste PDF des candidats par catégorie
     * @param candidats La liste des candidats
     * @param category La catégorie de permis
     * @return Le chemin du fichier PDF généré
     * @throws IOException En cas d'erreur d'E/S
     */
    public static String generateCandidatsByCategoryPDF(List<Candidat> candidats, String category) throws IOException {
        return generateCandidatsListPDF(candidats, "LISTE DES CANDIDATS - CATÉGORIE " + category);
    }

    /**
     * Génère une liste PDF des résultats de recherche
     * @param candidats La liste des candidats
     * @param searchTerm Le terme de recherche
     * @return Le chemin du fichier PDF généré
     * @throws IOException En cas d'erreur d'E/S
     */
    public static String generateSearchResultsPDF(List<Candidat> candidats, String searchTerm) throws IOException {
        return generateCandidatsListPDF(candidats, "RÉSULTATS DE RECHERCHE: \"" + searchTerm + "\"");
    }

    /**
     * Ajoute l'en-tête de l'auto-école au document
     * @param document Le document PDF
     * @param schoolInfo Les informations de l'auto-école
     * @throws IOException En cas d'erreur d'E/S
     */
    private static void addDrivingSchoolHeader(Document document, DrivingSchoolInfo schoolInfo) throws IOException {
        // Créer une table pour l'en-tête (2 colonnes: logo et infos)
        Table headerTable = new Table(UnitValue.createPercentArray(new float[]{20, 80}));
        headerTable.setWidth(UnitValue.createPercentValue(100));
        headerTable.setMarginBottom(20);

        // Ajouter le logo si disponible
        Cell logoCell = new Cell();
        logoCell.setBorder(Border.NO_BORDER);
        logoCell.setVerticalAlignment(VerticalAlignment.MIDDLE);

        if (schoolInfo != null && schoolInfo.getLogoPath() != null && !schoolInfo.getLogoPath().isEmpty()) {
            try {
                File logoFile = new File(schoolInfo.getLogoPath());
                if (logoFile.exists()) {
                    Image logo = new Image(ImageDataFactory.create(schoolInfo.getLogoPath()));
                    logo.setWidth(60);
                    logo.setHeight(60);
                    logoCell.add(logo);
                } else {
                    logoCell.add(new Paragraph(""));
                }
            } catch (Exception e) {
                logoCell.add(new Paragraph(""));
                e.printStackTrace();
            }
        } else {
            logoCell.add(new Paragraph(""));
        }

        headerTable.addCell(logoCell);

        // Ajouter les informations de l'auto-école
        Cell infoCell = new Cell();
        infoCell.setBorder(Border.NO_BORDER);
        infoCell.setVerticalAlignment(VerticalAlignment.MIDDLE);
        infoCell.setTextAlignment(TextAlignment.RIGHT);

        String schoolName = (schoolInfo != null) ? schoolInfo.getName() : "Auto-École Sécurité Routière";
        String matricule = (schoolInfo != null) ? schoolInfo.getMatriculeFiscale() : "";
        String address = (schoolInfo != null) ? schoolInfo.getAddress() : "123 Avenue de la République, Bizerte";
        String phone = (schoolInfo != null) ? schoolInfo.getPhoneNumber() : "+216 12 345 678";
        String email = (schoolInfo != null) ? schoolInfo.getEmail() : "";

        Paragraph infoParagraph = new Paragraph();
        infoParagraph.add(new Text(schoolName).setBold().setFontSize(16));
        infoParagraph.add("\n");

        if (matricule != null && !matricule.isEmpty()) {
            infoParagraph.add(new Text("Matricule Fiscale: " + matricule).setFontSize(10));
            infoParagraph.add("\n");
        }

        infoParagraph.add(new Text("Adresse: " + address).setFontSize(10));
        infoParagraph.add("\n");
        infoParagraph.add(new Text("Téléphone: " + phone).setFontSize(10));

        if (email != null && !email.isEmpty()) {
            infoParagraph.add("\n");
            infoParagraph.add(new Text("Email: " + email).setFontSize(10));
        }

        infoCell.add(infoParagraph);
        headerTable.addCell(infoCell);

        document.add(headerTable);
    }

    /**
     * Crée une section d'informations
     * @param title Le titre de la section
     * @param data Les données à afficher
     * @return La section créée
     */
    private static Table createInfoSection(String title, String[][] data) {
        // Ajouter le titre de la section
        Paragraph sectionTitle = new Paragraph(title)
                .setBold()
                .setFontSize(12)
                .setMarginTop(10)
                .setMarginBottom(5);

        // Créer une table pour les données
        Table table = new Table(UnitValue.createPercentArray(new float[]{30, 70}));
        table.setWidth(UnitValue.createPercentValue(100));

        // Ajouter les données
        for (String[] row : data) {
            Cell labelCell = new Cell();
            labelCell.setBorder(Border.NO_BORDER);
            labelCell.add(new Paragraph(row[0] + ":").setBold().setFontSize(10));
            table.addCell(labelCell);

            Cell valueCell = new Cell();
            valueCell.setBorder(Border.NO_BORDER);
            valueCell.add(new Paragraph(row[1]).setFontSize(10));
            table.addCell(valueCell);
        }

        // Créer une table pour la section complète
        Table sectionTable = new Table(1);
        sectionTable.setWidth(UnitValue.createPercentValue(100));
        sectionTable.setMarginBottom(10);

        Cell titleCell = new Cell();
        titleCell.setBorder(Border.NO_BORDER);
        titleCell.add(sectionTitle);
        sectionTable.addCell(titleCell);

        Cell dataCell = new Cell();
        dataCell.setBorder(Border.NO_BORDER);
        dataCell.add(table);
        sectionTable.addCell(dataCell);

        return sectionTable;
    }

    /**
     * Crée une cellule pour un document
     * @param documentName Le nom du document
     * @param isAvailable Si le document est disponible
     * @return La cellule créée
     */
    private static Cell createDocumentCell(String documentName, boolean isAvailable) {
        Cell cell = new Cell();
        cell.setBorder(Border.NO_BORDER);
        cell.setVerticalAlignment(VerticalAlignment.MIDDLE);

        Paragraph p = new Paragraph();
        p.add(new Text(documentName + ": ").setBold().setFontSize(10));

        if (isAvailable) {
            p.add(new Text("Fourni").setFontColor(ColorConstants.GREEN).setFontSize(10));
        } else {
            p.add(new Text("Non fourni").setFontColor(ColorConstants.RED).setFontSize(10));
        }

        cell.add(p);
        return cell;
    }

    /**
     * Crée une cellule d'en-tête pour une table
     * @param text Le texte de l'en-tête
     * @return La cellule créée
     */
    private static Cell createHeaderCell(String text) {
        Cell cell = new Cell();
        cell.setBackgroundColor(ColorConstants.LIGHT_GRAY);
        cell.setTextAlignment(TextAlignment.CENTER);
        cell.setVerticalAlignment(VerticalAlignment.MIDDLE);
        cell.setPadding(5);
        cell.add(new Paragraph(text).setBold().setFontSize(10));
        return cell;
    }

    /**
     * Crée une cellule pour une table
     * @param text Le texte de la cellule
     * @return La cellule créée
     */
    private static Cell createCell(String text) {
        Cell cell = new Cell();
        cell.setTextAlignment(TextAlignment.LEFT);
        cell.setVerticalAlignment(VerticalAlignment.MIDDLE);
        cell.setPadding(5);
        cell.add(new Paragraph(text).setFontSize(10));
        return cell;
    }

    /**
     * Gestionnaire d'événements pour le pied de page
     */
    static class FooterEventHandler implements IEventHandler {
        @Override
        public void handleEvent(Event event) {
            PdfDocumentEvent docEvent = (PdfDocumentEvent) event;
            PdfDocument pdf = docEvent.getDocument();
            PdfPage page = docEvent.getPage();
            Rectangle pageSize = page.getPageSize();

            PdfCanvas pdfCanvas = new PdfCanvas(page);

            // Correction du constructeur Canvas - utiliser (PdfCanvas, Rectangle, boolean)
            Canvas canvas = new Canvas(pdfCanvas, new Rectangle(36, 20, pageSize.getWidth() - 72, 50), true);

            // Ajouter la date de génération à gauche
            Paragraph leftFooter = new Paragraph("Généré le: " + LocalDate.now().format(DATE_FORMATTER))
                    .setFontSize(8)
                    .setFontColor(ColorConstants.GRAY);
            canvas.showTextAligned(leftFooter, 0, 30, TextAlignment.LEFT);

            // Ajouter le numéro de page à droite
            Paragraph rightFooter = new Paragraph(String.format("Page %d / %d", pdf.getPageNumber(page), pdf.getNumberOfPages()))
                    .setFontSize(8)
                    .setFontColor(ColorConstants.GRAY);
            canvas.showTextAligned(rightFooter, pageSize.getWidth() - 72, 30, TextAlignment.RIGHT);

            canvas.close();
        }
    }
}