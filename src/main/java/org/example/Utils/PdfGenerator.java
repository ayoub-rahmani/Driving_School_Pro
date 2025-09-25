package org.example.Utils;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.example.Entities.Candidat;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.apache.pdfbox.pdmodel.font.PDType1Font.HELVETICA;
import static org.apache.pdfbox.pdmodel.font.PDType1Font.HELVETICA_BOLD;

public class PdfGenerator {

    private static final String DOCUMENTS_OUTPUT_DIR = "documents/generated/";
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

    public static String generatePdfDocument(Document document) throws IOException {
        String baseOutputName = DOCUMENTS_OUTPUT_DIR + document.getDocumentType().replace(" ", "_") + "_" +
                LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        String outputFileName = baseOutputName + ".pdf";

        try (PDDocument pdDocument = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            pdDocument.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(pdDocument, page)) {
                // Header
                contentStream.beginText();
                contentStream.setFont(HELVETICA_BOLD, 16);
                contentStream.newLineAtOffset(50, 750);
                contentStream.showText(document.getAutoEcoleNom());
                contentStream.endText();

                // Document Type (Title)
                contentStream.beginText();
                contentStream.setFont(HELVETICA_BOLD, 14);
                contentStream.newLineAtOffset(220, 650);
                contentStream.showText(document.getDocumentType());
                contentStream.endText();

                float y = 600;

                if (document.getCandidat() != null) {
                    generateCandidatFicheContent(contentStream, document.getCandidat(), pdDocument, y);
                } else if (document.getCandidats() != null) {
                    generateCandidatsListContent(contentStream, document.getCandidats(),y);
                }

                // Footer
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD_OBLIQUE, 10);
                contentStream.newLineAtOffset(50, 50);
                contentStream.showText("Document généré le " + document.getGenerationDate().format(DATE_FORMATTER));
                contentStream.endText();
            } // contentStream is automatically closed here

            pdDocument.save(outputFileName);

        } // pdDocument is automatically closed here
        return outputFileName;
    }



    private static void generateCandidatFicheContent(PDPageContentStream contentStream, Candidat candidat, PDDocument pdDocument, float y) throws IOException {
        addField(contentStream, "N° CIN:", candidat.getCin(), y);
        y -= 25;

        addField(contentStream, "Nom:", candidat.getNom(), y);
        y -= 25;

        addField(contentStream, "Prénom:", candidat.getPrenom(), y);
        y -= 25;

        addField(contentStream, "Date de naissance:",
                candidat.getDateNaissance().format(DATE_FORMATTER), y);
        y -= 25;

        addField(contentStream, "Téléphone:", candidat.getTelephone(), y);
        y -= 25;

        addField(contentStream, "Email:", candidat.getEmail(), y);
        y -= 25;

        addField(contentStream, "Adresse:", candidat.getAdresse(), y);
        y -= 25;

        addField(contentStream, "Date d'inscription:",
                candidat.getDateInscription().format(DATE_FORMATTER), y);
        y -= 25;

        addField(contentStream, "Catégories permis:",
                String.join(", ", candidat.getCategoriesPermis()), y);
        y -= 25;

        addField(contentStream, "Statut:", candidat.isActif() ? "Actif" : "Inactif", y);
        y -= 40;

        // Add documents section
        contentStream.beginText();
        contentStream.setFont(HELVETICA_BOLD, 14);
        contentStream.newLineAtOffset(50, y);
        contentStream.showText("Documents fournis:");
        contentStream.endText();
        y -= 25;

        boolean hasCIN = hasDocument(candidat.getCheminPhotoCIN());
        boolean hasPhoto = hasDocument(candidat.getCheminPhotoIdentite());
        boolean hasCertificat = hasDocument(candidat.getCheminCertificatMedical());

        addField(contentStream, "Photo CIN:", hasCIN ? "Oui" : "Non", y);
        y -= 25;

        addField(contentStream, "Photo d'identité:", hasPhoto ? "Oui" : "Non", y);
        y -= 25;

        addField(contentStream, "Certificat médical:", hasCertificat ? "Oui" : "Non", y);
        y -= 25;

        // Add photo if available
        if (hasPhoto) {

            try {
                PDImageXObject image = PDImageXObject.createFromFile(candidat.getCheminPhotoIdentite(), pdDocument);
                float imageWidth = 100;
                float imageHeight = 120;
                contentStream.drawImage(image, 450, 600, imageWidth, imageHeight);
            } catch (IOException e) {
                // Continue without image if it can't be loaded
                System.err.println("Couldn't load photo: " + e.getMessage());
            }

        }
    }

    private static void generateCandidatsListContent(PDPageContentStream contentStream, List<Candidat> candidats,float y) throws IOException {
        // Table header
        float[] colWidths = {100, 100, 80, 100, 100};
        float startX = 50;

        // Column headers
        contentStream.beginText();
        contentStream.setFont(HELVETICA_BOLD, 12);
        contentStream.newLineAtOffset(startX, y);
        contentStream.showText("Nom");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(HELVETICA_BOLD, 12);
        contentStream.newLineAtOffset(startX + colWidths[0], y);
        contentStream.showText("Prénom");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(HELVETICA_BOLD, 12);
        contentStream.newLineAtOffset(startX + colWidths[0] + colWidths[1], y);
        contentStream.showText("CIN");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(HELVETICA_BOLD, 12);
        contentStream.newLineAtOffset(startX + colWidths[0] + colWidths[1] + colWidths[2], y);
        contentStream.showText("Téléphone");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(HELVETICA_BOLD, 12);
        contentStream.newLineAtOffset(startX + colWidths[0] + colWidths[1] + colWidths[2] + colWidths[3], y);
        contentStream.showText("Catégories");
        contentStream.endText();

        y -= 20;

        // Horizontal line after header
        contentStream.moveTo(startX, y + 10);
        contentStream.lineTo(startX + colWidths[0] + colWidths[1] + colWidths[2] + colWidths[3] + colWidths[4], y + 10);
        contentStream.stroke();

        // List candidates
        int itemsPerPage = 25;
        int itemCount = 0;
        for (Candidat candidat : candidats) {
            // Ensure we have room for another entry
            if (y < 50) {
                break;
            }

            // Add candidate data
            contentStream.beginText();
            contentStream.setFont(HELVETICA, 10);
            contentStream.newLineAtOffset(startX, y);
            contentStream.showText(truncateText(candidat.getNom(), 15));
            contentStream.endText();

            contentStream.beginText();
            contentStream.setFont(HELVETICA, 10);
            contentStream.newLineAtOffset(startX + colWidths[0], y);
            contentStream.showText(truncateText(candidat.getPrenom(), 15));
            contentStream.endText();

            contentStream.beginText();
            contentStream.setFont(HELVETICA, 10);
            contentStream.newLineAtOffset(startX + colWidths[0] + colWidths[1], y);
            contentStream.showText(truncateText(candidat.getCin(), 10));
            contentStream.endText();

            contentStream.beginText();
            contentStream.setFont(HELVETICA, 10);
            contentStream.newLineAtOffset(startX + colWidths[0] + colWidths[1] + colWidths[2], y);
            contentStream.showText(truncateText(candidat.getTelephone(), 15));
            contentStream.endText();

            contentStream.beginText();
            contentStream.setFont(HELVETICA, 10);
            contentStream.newLineAtOffset(startX + colWidths[0] + colWidths[1] + colWidths[2] + colWidths[3], y);
            contentStream.showText(truncateText(String.join(", ", candidat.getCategoriesPermis()), 15));
            contentStream.endText();

            y -= 20;
            itemCount++;
        }
    }

    /**
     * Helper method to add a labeled field to the PDF
     */
    private static void addField(PDPageContentStream contentStream, String label, String value, float y)
            throws IOException {
        // Label
        contentStream.beginText();
        contentStream.setFont(HELVETICA_BOLD, 12);
        contentStream.newLineAtOffset(50, y);
        contentStream.showText(label);
        contentStream.endText();

        // Value
        contentStream.beginText();
        contentStream.setFont(HELVETICA, 12);
        contentStream.newLineAtOffset(180, y);
        contentStream.showText(value != null ? value : "");
        contentStream.endText();
    }

    /**
     * Helper method to check if a document exists
     */
    private static boolean hasDocument(String documentPath) {
        return documentPath != null && !documentPath.isEmpty() && new File(documentPath).exists();
    }

    /**
     * Helper method to truncate text to fit the PDF fields
     */
    private static String truncateText(String text, int maxLength) {
        if (text == null) return "";
        return text.length() <= maxLength ? text : text.substring(0, maxLength - 3) + "...";
    }
}