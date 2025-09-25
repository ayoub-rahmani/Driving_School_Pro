package org.example.Service;

import org.example.Entities.Moniteur;
import org.example.Rep.MoniteurRep;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class MoniteurService {

    private final MoniteurRep moniteurRep;

    public MoniteurService(MoniteurRep moniteurRep) {
        this.moniteurRep = moniteurRep;
    }

    // Create and Update
    public Moniteur saveMoniteur(Moniteur moniteur) {
        // Business validation logic
        if (moniteur.getDateNaissance() != null &&
                ChronoUnit.YEARS.between(moniteur.getDateNaissance(), LocalDate.now()) < 21) {
            throw new IllegalArgumentException("Le moniteur doit avoir au moins 21 ans");
        }

        // Validate uniqueness before saving
        List<String> uniquenessErrors = validateUniqueness(moniteur);
        if (!uniquenessErrors.isEmpty()) {
            throw new IllegalArgumentException(String.join(", ", uniquenessErrors));
        }

        return moniteurRep.save(moniteur);
    }

    // NEW METHOD: Validate uniqueness of fields
    public List<String> validateUniqueness(Moniteur moniteur) {
        List<String> errors = new ArrayList<>();
        Long excludeId = moniteur.getId(); // Will be null for new records

        // Check CIN uniqueness within moniteur table
        if (moniteurRep.existsByCin(moniteur.getCin(), excludeId)) {
            errors.add("Un moniteur avec ce CIN existe déjà");
        }

        // Check CIN uniqueness in candidat table
        if (moniteurRep.existsCinInCandidat(moniteur.getCin())) {
            errors.add("Un candidat avec ce CIN existe déjà");
        }

        // Check telephone uniqueness within moniteur table
        if (moniteurRep.existsByTelephone(moniteur.getTelephone(), excludeId)) {
            errors.add("Un moniteur avec ce numéro de téléphone existe déjà");
        }

        // Check telephone uniqueness in candidat table
        if (moniteurRep.existsTelephoneInCandidat(moniteur.getTelephone())) {
            errors.add("Un candidat avec ce numéro de téléphone existe déjà");
        }

        // Check numPermis uniqueness
        if (moniteurRep.existsByNumPermis(moniteur.getNumPermis(), excludeId)) {
            errors.add("Un moniteur avec ce numéro de permis existe déjà");
        }

        return errors;
    }

    // Read
    public List<Moniteur> getAllMoniteurs() {
        return moniteurRep.findAll();
    }

    public Optional<Moniteur> getMoniteurById(Long id) {
        return moniteurRep.findById(id);
    }

    public List<Moniteur> searchMoniteurs(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getAllMoniteurs();
        }
        return moniteurRep.findByNomOrPrenomOrCin(searchTerm.trim());
    }

    // Delete
    public void deleteMoniteur(Moniteur moniteur) {
        moniteurRep.delete(moniteur);
    }

    public void deleteMoniteurById(Long id) {
        moniteurRep.deleteById(id);
    }

    // Business methods
    public List<Moniteur> getAvailableMoniteurs() {
        return moniteurRep.findByDisponible(true);
    }

    public List<Moniteur> getMoniteursByCategoriePermis(String categorie) {
        return moniteurRep.findByCategoriePermis(categorie);
    }

    public double calculateAverageSalary() {
        List<Moniteur> allMoniteurs = moniteurRep.findAll();
        if (allMoniteurs.isEmpty()) {
            return 0.0;
        }

        double totalSalary = allMoniteurs.stream()
                .mapToDouble(Moniteur::getSalaire)
                .sum();
        return totalSalary / allMoniteurs.size();
    }
    public List<Moniteur> filterMoniteurs(String status, String category) {
        List<Moniteur> allMoniteurs = getAllMoniteurs();

        return allMoniteurs.stream()
                .filter(m -> {
                    // Filter by status
                    if (status != null && !status.equals("Tous les statuts")) {
                        boolean isDisponible = status.equals("Disponible");
                        if (m.isDisponible() != isDisponible) return false;
                    }

                    // Filter by category
                    if (category != null && !category.equals("Toutes les catégories")) {
                        if (!m.getCategoriesPermis().contains(category)) return false;
                    }


                    return true;
                })
                .collect(Collectors.toList());
    }

    public List<Moniteur> getRecentlyHiredMoniteurs(int months) {
        LocalDate cutoffDate = LocalDate.now().minusMonths(months);

        return moniteurRep.findAll().stream()
                .filter(m -> m.getDateEmbauche().isAfter(cutoffDate))
                .collect(Collectors.toList());
    }
}