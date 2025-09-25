package org.example.Service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.example.Entities.Candidat;
import org.example.Rep.CandidatRep;
import org.example.Utils.CandidatPdfGenerator;
import org.example.Utils.Document;
import org.example.Utils.PdfGenerator;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class CandidatService {

    private static final String DOCUMENTS_OUTPUT_DIR = "documents/generated/";
    private final CandidatRep candidatRep;

    public CandidatService(CandidatRep candidatRep) {
        this.candidatRep = candidatRep;
    }
    // Ajoutez ces méthodes à votre classe CandidatService existante

    /**
     * Génère une fiche PDF pour un candidat
     * @param candidatId L'ID du candidat
     * @return Le chemin du fichier PDF généré
     * @throws IOException En cas d'erreur d'E/S
     */
    public String generateCandidatFichePDF(Long candidatId) throws IOException {
        Optional<Candidat> candidatOpt = getCandidatById(candidatId);
        if (!candidatOpt.isPresent()) {
            throw new IOException("Candidat introuvable avec l'ID: " + candidatId);
        }

        Candidat candidat = candidatOpt.get();
        return CandidatPdfGenerator.generateCandidatFichePDF(candidat);
    }

    /**
     * Génère une liste PDF des candidats actifs
     * @return Le chemin du fichier PDF généré
     * @throws IOException En cas d'erreur d'E/S
     */
    public String generateActiveCandiatsListPDF() throws IOException {
        List<Candidat> activeCandidats = getAllCandidats().stream()
                .filter(Candidat::isActif)
                .collect(Collectors.toList());

        if (activeCandidats.isEmpty()) {
            throw new IOException("Aucun candidat actif trouvé");
        }

        return CandidatPdfGenerator.generateCandidatsListPDF(activeCandidats, "LISTE DES CANDIDATS ACTIFS");
    }

    /**
     * Génère une liste PDF des candidats par catégorie
     * @param category La catégorie de permis
     * @return Le chemin du fichier PDF généré
     * @throws IOException En cas d'erreur d'E/S
     */
    public String generateCandidatsByCategoryPDF(String category) throws IOException {
        List<Candidat> candidatsByCategory = getAllCandidats().stream()
                .filter(c -> c.getCategoriesPermis().contains(category))
                .collect(Collectors.toList());

        if (candidatsByCategory.isEmpty()) {
            throw new IOException("Aucun candidat trouvé pour la catégorie: " + category);
        }

        return CandidatPdfGenerator.generateCandidatsByCategoryPDF(candidatsByCategory, category);
    }

    /**
     * Génère une liste PDF des résultats de recherche
     * @param searchTerm Le terme de recherche
     * @return Le chemin du fichier PDF généré
     * @throws IOException En cas d'erreur d'E/S
     */
    public String generateSearchResultsPDF(String searchTerm) throws IOException {
        List<Candidat> searchResults = searchCandidats(searchTerm);

        if (searchResults.isEmpty()) {
            throw new IOException("Aucun résultat trouvé pour la recherche: " + searchTerm);
        }

        return CandidatPdfGenerator.generateSearchResultsPDF(searchResults, searchTerm);
    }

    // Create and Update
    public Candidat saveCandidat(Candidat candidat) {
        // Business validation logic
        if (candidat.getDateNaissance() != null &&
                ChronoUnit.YEARS.between(candidat.getDateNaissance(), LocalDate.now()) < 16) {
            throw new IllegalArgumentException("Le candidat doit avoir au moins 16 ans");
        }

        // Validate uniqueness before saving
        List<String> uniquenessErrors = validateUniqueness(candidat);
        if (!uniquenessErrors.isEmpty()) {
            throw new IllegalArgumentException(String.join(", ", uniquenessErrors));
        }

        return candidatRep.save(candidat);
    }

    // NEW METHOD: Validate uniqueness of fields
    public List<String> validateUniqueness(Candidat candidat) {
        List<String> errors = new ArrayList<>();
        Long excludeId = candidat.getId(); // Will be null for new records

        // Check CIN uniqueness within candidat table
        if (candidatRep.existsByCin(candidat.getCin(), excludeId)) {
            errors.add("Un candidat avec ce CIN existe déjà");
        }

        // Check CIN uniqueness in moniteur table
        if (candidatRep.existsCinInMoniteur(candidat.getCin())) {
            errors.add("Un moniteur avec ce CIN existe déjà");
        }

        // Check email uniqueness
        if (candidatRep.existsByEmail(candidat.getEmail(), excludeId)) {
            errors.add("Un candidat avec cet email existe déjà");
        }

        // Check telephone uniqueness within candidat table
        if (candidatRep.existsByTelephone(candidat.getTelephone(), excludeId)) {
            errors.add("Un candidat avec ce numéro de téléphone existe déjà");
        }

        // Check telephone uniqueness in moniteur table
        if (candidatRep.existsTelephoneInMoniteur(candidat.getTelephone())) {
            errors.add("Un moniteur avec ce numéro de téléphone existe déjà");
        }

        return errors;
    }

    // Read
    public List<Candidat> getAllCandidats() {
        return candidatRep.findAll();
    }

    public Optional<Candidat> getCandidatById(Long id) {
        return candidatRep.findById(id);
    }

    public List<Candidat> searchCandidats(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getAllCandidats();
        }
        return candidatRep.findByNomOrPrenomOrCin(searchTerm.trim());
    }

    // Delete
    public void deleteCandidat(Candidat candidat) {
        candidatRep.delete(candidat);
    }

    public void deleteCandidatById(Long id) {
        candidatRep.deleteById(id);
    }

    // Business methods
    public List<Candidat> getActiveCandidats() {
        return candidatRep.findByActif(true);
    }

    public List<Candidat> getInactiveCandidats() {
        return candidatRep.findByActif(false);
    }

    public List<Candidat> getCandidatsByCategoriePermis(String categorie) {
        return candidatRep.findByCategoriePermis(categorie);
    }

    public List<Candidat> getRecentCandidats(int months) {
        LocalDate cutoffDate = LocalDate.now().minusMonths(months);

        return candidatRep.findAll().stream()
                .filter(c -> c.getDateInscription().isAfter(cutoffDate))
                .collect(Collectors.toList());
    }

    // Dashboard methods
    public Map<String, Long> getCategoriesDistribution() {
        Map<String, Long> distribution = new HashMap<>();
        List<Candidat> candidats = getAllCandidats();

        // Count occurrences of each category
        for (Candidat candidat : candidats) {
            for (String category : candidat.getCategoriesPermis()) {
                distribution.put(category, distribution.getOrDefault(category, 0L) + 1);
            }
        }

        return distribution;
    }

    public Map<Month, Long> getInscriptionsParMois() {
        List<Candidat> candidats = getAllCandidats();

        return candidats.stream()
                .collect(Collectors.groupingBy(c -> c.getDateInscription().getMonth(), Collectors.counting()));
    }

    public Map<String, Long> getStatistiques() {
        Map<String, Long> stats = new HashMap<>();
        List<Candidat> candidats = getAllCandidats();

        long total = candidats.size();
        long actifs = candidats.stream().filter(Candidat::isActif).count();
        long inactifs = total - actifs;

        stats.put("total", total);
        stats.put("actifs", actifs);
        stats.put("inactifs", inactifs);

        return stats;
    }

    // Document management
    public boolean hasRequiredDocuments(Candidat candidat) {
        return documentExists(candidat.getCheminPhotoCIN()) &&
                documentExists(candidat.getCheminPhotoIdentite()) &&
                documentExists(candidat.getCheminCertificatMedical());
    }

    private boolean documentExists(String cheminDocument) {
        if (cheminDocument == null || cheminDocument.trim().isEmpty()) {
            return false;
        }
        return new File(cheminDocument).exists();
    }

    // View document functionality - returns the file path if it exists
    public String viewDocument(Long candidatId, String documentType) {
        Optional<Candidat> candidatOpt = getCandidatById(candidatId);
        if (!candidatOpt.isPresent()) {
            throw new IllegalArgumentException("Candidat non trouvé avec ID: " + candidatId);
        }

        Candidat candidat = candidatOpt.get();
        String cheminDocument = null;

        switch (documentType.toLowerCase()) {
            case "cin":
                cheminDocument = candidat.getCheminPhotoCIN();
                break;
            case "photo":
                cheminDocument = candidat.getCheminPhotoIdentite();
                break;
            case "certificat":
                cheminDocument = candidat.getCheminCertificatMedical();
                break;
            default:
                throw new IllegalArgumentException("Type de document non reconnu: " + documentType);
        }

        if (cheminDocument == null || cheminDocument.trim().isEmpty() || !new File(cheminDocument).exists()) {
            throw new IllegalStateException("Document non disponible ou introuvable");
        }

        return cheminDocument;
    }


    /**
     * Helper method to convert PDF to PNG
     */
    private void saveAsImage(String pdfFilePath, String outputImageName) throws IOException {
        try (PDDocument document = PDDocument.load(new File(pdfFilePath))) {
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            BufferedImage image = pdfRenderer.renderImageWithDPI(0, 300, ImageType.RGB);  // Render first page

            // Write the image to a PNG file
            File outputFile = new File(outputImageName);
            ImageIO.write(image, "png", outputFile);
        }
    }


    /**
     * Filter candidates based on criteria
     * @param status Status filter (Actif, Inactif, or null for all)
     * @param category Category filter (A, B, C, or null for all)
     * @param date Date filter (null for all)
     * @return Filtered list of candidates
     */
    public List<Candidat> filterCandidats(String status, String category, LocalDate date) {
        List<Candidat> allCandidats = getAllCandidats();

        return allCandidats.stream()
                .filter(c -> {
                    // Filter by status
                    if (status != null && !status.equals("Tous les statuts")) {
                        boolean isActif = status.equals("Actif");
                        if (c.isActif() != isActif) return false;
                    }

                    // Filter by category
                    if (category != null && !category.equals("Toutes les catégories")) {
                        if (!c.getCategoriesPermis().contains(category)) return false;
                    }

                    // Filter by date
                    if (date != null) {
                        if (!c.getDateInscription().equals(date)) return false;
                    }

                    return true;
                })
                .collect(Collectors.toList());
    }
}