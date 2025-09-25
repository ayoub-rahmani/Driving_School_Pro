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
import org.example.Entities.Depense;
import org.example.Entities.DrivingSchoolInfo;
import org.example.Entities.Moniteur;
import org.example.Entities.Paiement;
import org.example.Entities.Reparation;
import org.example.Entities.Vehicule;
import org.example.Services.DrivingSchoolService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class PaiementPdfGenerator {

    private static final String DOCUMENTS_OUTPUT_DIR = "documents/generated/paiements/";
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
     * Génère un reçu de paiement
     * @param paiement Le paiement
     * @param candidat Le candidat associé
     * @return Le chemin du fichier PDF généré
     * @throws IOException En cas d'erreur d'E/S
     */
    public static String generatePaiementPdf(Paiement paiement, Candidat candidat) throws IOException {
        if (paiement == null) {
            throw new IllegalArgumentException("Le paiement ne peut pas être null");
        }

        if (candidat == null) {
            throw new IllegalArgumentException("Le candidat ne peut pas être null");
        }

        String fileName = DOCUMENTS_OUTPUT_DIR + "Recu_Paiement_" + paiement.getId() + "_" +
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
        Paragraph title = new Paragraph("REÇU DE PAIEMENT")
                .setBold()
                .setFontSize(18)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(ColorConstants.DARK_GRAY)
                .setMarginBottom(20);
        document.add(title);

        // Ajouter une ligne séparatrice
        document.add(new LineSeparator(new com.itextpdf.kernel.pdf.canvas.draw.SolidLine(1))
                .setMarginBottom(20));

        // Ajouter les informations du paiement
        Table infoTable = new Table(UnitValue.createPercentArray(new float[]{30, 70}));
        infoTable.setWidth(UnitValue.createPercentValue(100));

        // Informations du paiement
        infoTable.addCell(createLabelCell("N° Paiement:"));
        infoTable.addCell(createValueCell(paiement.getId().toString()));

        infoTable.addCell(createLabelCell("Date:"));
        infoTable.addCell(createValueCell(paiement.getDatePaiement().format(DATE_FORMATTER)));

        infoTable.addCell(createLabelCell("Candidat:"));
        infoTable.addCell(createValueCell(candidat.getNom() + " " + candidat.getPrenom()));

        infoTable.addCell(createLabelCell("CIN:"));
        infoTable.addCell(createValueCell(candidat.getCin()));

        infoTable.addCell(createLabelCell("Montant:"));
        infoTable.addCell(createValueCell(String.format("%.2f DT", paiement.getMontant())));

        infoTable.addCell(createLabelCell("Méthode:"));
        infoTable.addCell(createValueCell(paiement.getMethodePaiement()));

        infoTable.addCell(createLabelCell("Référence:"));
        infoTable.addCell(createValueCell(paiement.getReference()));

        infoTable.addCell(createLabelCell("Statut:"));
        Cell statutCell = createValueCell(paiement.getStatut());

        // Colorer le statut selon sa valeur
        if (paiement.getStatut().equals("COMPLET")) {
            statutCell.setFontColor(ColorConstants.GREEN);
        } else if (paiement.getStatut().equals("PARTIEL")) {
            statutCell.setFontColor(ColorConstants.ORANGE);
        } else if (paiement.getStatut().equals("REMBOURSEMENT")) {
            statutCell.setFontColor(ColorConstants.RED);
        }

        infoTable.addCell(statutCell);

        if (paiement.getRemise() > 0) {
            infoTable.addCell(createLabelCell("Remise:"));
            infoTable.addCell(createValueCell(String.format("%.2f%%", paiement.getRemise())));
        }

        document.add(infoTable);

        // Ajouter les notes si disponibles
        if (paiement.getNotes() != null && !paiement.getNotes().isEmpty()) {
            document.add(new Paragraph("Notes:")
                    .setBold()
                    .setFontSize(12)
                    .setMarginTop(20)
                    .setMarginBottom(5));

            document.add(new Paragraph(paiement.getNotes())
                    .setFontSize(10)
                    .setMarginBottom(20));
        }

        // Ajouter une section pour la signature
        document.add(new Paragraph("Signature:")
                .setBold()
                .setFontSize(12)
                .setMarginTop(50)
                .setMarginBottom(5));

        // Ajouter une ligne pour la signature
        Table signatureTable = new Table(1);
        signatureTable.setWidth(UnitValue.createPercentValue(50));

        Cell signatureCell = new Cell();
        signatureCell.setBorder(Border.NO_BORDER);
        signatureCell.setBorderBottom(new com.itextpdf.layout.borders.SolidBorder(1));
        signatureCell.setHeight(40);
        signatureTable.addCell(signatureCell);

        document.add(signatureTable);

        // Fermer le document
        document.close();

        return fileName;
    }

    /**
     * Génère un PDF pour une dépense
     * @param depense La dépense
     * @param moniteur Le moniteur associé (optionnel)
     * @param vehicule Le véhicule associé (optionnel)
     * @param reparation La réparation associée (optionnel)
     * @return Le chemin du fichier PDF généré
     * @throws IOException En cas d'erreur d'E/S
     */
    public static String generateDepensePdf(Depense depense, Optional<Moniteur> moniteur,
                                            Optional<Vehicule> vehicule, Optional<Reparation> reparation) throws IOException {
        if (depense == null) {
            throw new IllegalArgumentException("La dépense ne peut pas être null");
        }

        String fileName = DOCUMENTS_OUTPUT_DIR + "Fiche_Depense_" + depense.getId() + "_" +
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
        Paragraph title = new Paragraph("FICHE DE DÉPENSE")
                .setBold()
                .setFontSize(18)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(ColorConstants.DARK_GRAY)
                .setMarginBottom(20);
        document.add(title);

        // Ajouter une ligne séparatrice
        document.add(new LineSeparator(new com.itextpdf.kernel.pdf.canvas.draw.SolidLine(1))
                .setMarginBottom(20));

        // Ajouter les informations de la dépense
        Table infoTable = new Table(UnitValue.createPercentArray(new float[]{30, 70}));
        infoTable.setWidth(UnitValue.createPercentValue(100));

        // Informations de la dépense
        infoTable.addCell(createLabelCell("N° Dépense:"));
        infoTable.addCell(createValueCell(depense.getId().toString()));

        infoTable.addCell(createLabelCell("Date:"));
        infoTable.addCell(createValueCell(depense.getDateDepense().format(DATE_FORMATTER)));

        infoTable.addCell(createLabelCell("Catégorie:"));
        infoTable.addCell(createValueCell(depense.getCategorie()));

        infoTable.addCell(createLabelCell("Montant:"));
        infoTable.addCell(createValueCell(String.format("%.2f DT", depense.getMontant())));

        // Informations spécifiques à la catégorie
        if (depense.getCategorie().equals("MONITEUR") && moniteur.isPresent()) {
            Moniteur m = moniteur.get();
            infoTable.addCell(createLabelCell("Moniteur:"));
            infoTable.addCell(createValueCell(m.getNom() + " " + m.getPrenom()));
        } else if (depense.getCategorie().equals("VEHICULE") && vehicule.isPresent()) {
            Vehicule v = vehicule.get();
            infoTable.addCell(createLabelCell("Véhicule:"));
            infoTable.addCell(createValueCell(v.getMarque() + " " + v.getModele() + " (" + v.getMatricule() + ")"));

            if (depense.getTypeVehiculeDepense() != null) {
                infoTable.addCell(createLabelCell("Type:"));
                infoTable.addCell(createValueCell(depense.getTypeVehiculeDepense()));

                if (depense.getTypeVehiculeDepense().equals("REPARATION") && reparation.isPresent()) {
                    Reparation r = reparation.get();
                    infoTable.addCell(createLabelCell("Réparation:"));
                    infoTable.addCell(createValueCell(r.getDescription()));
                    infoTable.addCell(createLabelCell("Coût réparation:"));
                    infoTable.addCell(createValueCell(String.format("%.2f DT", r.getCout())));
                }
            }
        } else if (depense.getCategorie().equals("AUTRES") && depense.getTypeAutreDepense() != null) {
            infoTable.addCell(createLabelCell("Type:"));
            infoTable.addCell(createValueCell(depense.getTypeAutreDepense()));
        }

        document.add(infoTable);

        // Ajouter la description si disponible
        if (depense.getDescription() != null && !depense.getDescription().isEmpty()) {
            document.add(new Paragraph("Description:")
                    .setBold()
                    .setFontSize(12)
                    .setMarginTop(20)
                    .setMarginBottom(5));

            document.add(new Paragraph(depense.getDescription())
                    .setFontSize(10)
                    .setMarginBottom(20));
        }

        // Ajouter une section pour la signature
        document.add(new Paragraph("Signature:")
                .setBold()
                .setFontSize(12)
                .setMarginTop(50)
                .setMarginBottom(5));

        // Ajouter une ligne pour la signature
        Table signatureTable = new Table(1);
        signatureTable.setWidth(UnitValue.createPercentValue(50));

        Cell signatureCell = new Cell();
        signatureCell.setBorder(Border.NO_BORDER);
        signatureCell.setBorderBottom(new com.itextpdf.layout.borders.SolidBorder(1));
        signatureCell.setHeight(40);
        signatureTable.addCell(signatureCell);

        document.add(signatureTable);

        // Fermer le document
        document.close();

        return fileName;
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
     * Crée une cellule d'étiquette
     * @param text Le texte de l'étiquette
     * @return La cellule créée
     */
    private static Cell createLabelCell(String text) {
        Cell cell = new Cell();
        cell.setBorder(Border.NO_BORDER);
        cell.setPadding(5);
        cell.add(new Paragraph(text).setBold().setFontSize(10));
        return cell;
    }

    /**
     * Crée une cellule de valeur
     * @param text Le texte de la valeur
     * @return La cellule créée
     */
    private static Cell createValueCell(String text) {
        Cell cell = new Cell();
        cell.setBorder(Border.NO_BORDER);
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
