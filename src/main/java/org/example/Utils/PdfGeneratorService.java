package org.example.Utils;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.example.Entities.*;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class PdfGeneratorService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Méthode pour générer un PDF de paiement
    /**
     * Génère un reçu de paiement
     * @param paiement Le paiement
     * @param candidat Le candidat associé
     * @return Le chemin du fichier PDF généré
     * @throws IOException En cas d'erreur d'E/S
     */
    public static String generatePaiementPdf(Paiement paiement, Candidat candidat) throws IOException {
        return PaiementPdfGenerator.generatePaiementPdf(paiement, candidat);
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
        return PaiementPdfGenerator.generateDepensePdf(depense, moniteur, vehicule, reparation);
    }

    // Méthode helper pour ajouter un champ au PDF
    private static void addField(PDPageContentStream contentStream, String label, String value, float y) throws IOException {
        // Label
        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
        contentStream.newLineAtOffset(50, y);
        contentStream.showText(label);
        contentStream.endText();

        // Value
        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA, 12);
        contentStream.newLineAtOffset(180, y);
        contentStream.showText(value != null ? value : "");
        contentStream.endText();
    }

    // Méthode pour ouvrir le fichier PDF généré
    public static void openPdfFile(String filePath) {
        try {
            File file = new File(filePath);
            if (file.exists()) {
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(file);
                } else {
                    System.out.println("Awt Desktop is not supported!");
                }
            } else {
                System.out.println("File does not exist!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}