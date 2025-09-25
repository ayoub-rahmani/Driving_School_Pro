package org.example.Service;

import org.example.Entities.Reparation;
import org.example.Entities.Vehicule;
import org.example.Rep.ReparationRep;
import org.example.Rep.VehiculeRep;
import org.example.Utils.FileStorageService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ReparationService {

    private final ReparationRep reparationRep;
    private final VehiculeRep vehiculeRep;
    private final AuditLogService auditLogService;
    private final FileStorageService fileStorageService;

    public ReparationService(ReparationRep reparationRep) {
        this.reparationRep = reparationRep;
        this.vehiculeRep = new VehiculeRep();
        this.auditLogService = new AuditLogService();
        this.fileStorageService = new FileStorageService();
    }

    public ReparationService(ReparationRep reparationRep, VehiculeRep vehiculeRep) {
        this.reparationRep = reparationRep;
        this.vehiculeRep = vehiculeRep;
        this.auditLogService = new AuditLogService();
        this.fileStorageService = new FileStorageService();
    }

    // Create and Update
    public Reparation saveReparation(Reparation reparation) {
        // Validate before saving
        List<String> validationErrors = validateReparation(reparation);
        if (!validationErrors.isEmpty()) {
            throw new IllegalArgumentException(String.join(", ", validationErrors));
        }

        // Save the reparation
        Reparation savedReparation = reparationRep.save(reparation);

        // Log the action
        String action = (reparation.getId() == null) ? "CREATE" : "UPDATE";
        auditLogService.logAction(
                action,
                "REPARATION",
                savedReparation.getId(),
                action + " reparation pour véhicule ID: " + savedReparation.getVehiculeId() + " - " + savedReparation.getDescription()
        );

        return savedReparation;
    }

    // Update a reparation (convenience method)
    public Reparation updateReparation(Reparation reparation) {
        if (reparation.getId() == null) {
            throw new IllegalArgumentException("Cannot update a reparation without an ID");
        }
        return saveReparation(reparation);
    }

    // Validate reparation
    public List<String> validateReparation(Reparation reparation) {
        List<String> errors = new ArrayList<>();

        // Check if vehicule exists
        if (reparation.getVehiculeId() == null) {
            errors.add("L'ID du véhicule est requis");
        } else {
            Optional<Vehicule> vehicule = vehiculeRep.findById(reparation.getVehiculeId());
            if (!vehicule.isPresent()) {
                errors.add("Le véhicule spécifié n'existe pas");
            }
        }

        // Check required fields
        if (reparation.getDescription() == null || reparation.getDescription().trim().isEmpty()) {
            errors.add("La description est requise");
        }

        if (reparation.getDateReparation() == null) {
            errors.add("La date de réparation est requise");
        } else if (reparation.getDateReparation().isAfter(LocalDate.now())) {
            errors.add("La date de réparation ne peut pas être dans le futur");
        }

        if (reparation.getCout() < 0) {
            errors.add("Le coût ne peut pas être négatif");
        }

        if (reparation.getPrestataire() == null || reparation.getPrestataire().trim().isEmpty()) {
            errors.add("Le prestataire est requis");
        }

        return errors;
    }

    // Read
    public List<Reparation> getAllReparations() {
        return reparationRep.findAll();
    }

    public Optional<Reparation> getReparationById(Long id) {
        return reparationRep.findById(id);
    }

    public List<Reparation> getReparationsByVehiculeId(Long vehiculeId) {
        return reparationRep.findByVehiculeId(vehiculeId);
    }

    public List<Reparation> searchReparations(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getAllReparations();
        }
        return reparationRep.findByDescription(searchTerm.trim());
    }

    public List<Reparation> searchReparations(Long vehiculeId, String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getReparationsByVehiculeId(vehiculeId);
        }
        return reparationRep.findByVehiculeIdAndDescription(vehiculeId, searchTerm.trim());
    }

    // Delete
    public void deleteReparation(Reparation reparation) {
        if (reparation != null && reparation.getId() != null) {
            // Log the delete action
            auditLogService.logAction(
                    "DELETE",
                    "REPARATION",
                    reparation.getId(),
                    "Suppression de la réparation ID: " + reparation.getId() + " pour véhicule ID: " + reparation.getVehiculeId()
            );

            // Delete associated file if exists
            if (reparation.getFacturePath() != null && !reparation.getFacturePath().isEmpty()) {
                fileStorageService.deleteFile(reparation.getFacturePath());
            }

            reparationRep.delete(reparation);
        }
    }

    public void deleteReparationById(Long id) {
        Optional<Reparation> reparation = getReparationById(id);
        if (reparation.isPresent()) {
            deleteReparation(reparation.get());
        } else {
            reparationRep.deleteById(id);
        }
    }

    // Business methods
    public List<Reparation> getReparationsByDateRange(LocalDate startDate, LocalDate endDate) {
        return reparationRep.findByDateRange(startDate, endDate);
    }

    public List<Reparation> getReparationsByVehiculeIdAndDateRange(Long vehiculeId, LocalDate startDate, LocalDate endDate) {
        return reparationRep.findByVehiculeIdAndDateRange(vehiculeId, startDate, endDate);
    }

    public List<Reparation> getReparationsByPrestataire(String prestataire) {
        return reparationRep.findByPrestataire(prestataire);
    }

    public List<Reparation> getReparationsByVehiculeIdAndPrestataire(Long vehiculeId, String prestataire) {
        return reparationRep.findByVehiculeIdAndPrestataire(vehiculeId, prestataire);
    }

    public List<Reparation> filterReparations(Long vehiculeId, String prestataire, LocalDate startDate, LocalDate endDate) {
        // If all filters are null, return all reparations for the vehicle
        if ((prestataire == null || prestataire.isEmpty()) && startDate == null && endDate == null) {
            return getReparationsByVehiculeId(vehiculeId);
        }

        // If only prestataire is provided
        if (prestataire != null && !prestataire.isEmpty() && startDate == null && endDate == null) {
            return getReparationsByVehiculeIdAndPrestataire(vehiculeId, prestataire);
        }

        // If only date range is provided
        if ((prestataire == null || prestataire.isEmpty()) && startDate != null && endDate != null) {
            return getReparationsByVehiculeIdAndDateRange(vehiculeId, startDate, endDate);
        }

        // If both filters are provided, we need to filter manually
        List<Reparation> reparations = getReparationsByVehiculeId(vehiculeId);
        List<Reparation> filteredReparations = new ArrayList<>();

        for (Reparation reparation : reparations) {
            boolean matchesPrestataire = prestataire == null || prestataire.isEmpty() ||
                    (reparation.getPrestataire() != null &&
                            reparation.getPrestataire().toLowerCase().contains(prestataire.toLowerCase()));

            boolean matchesDateRange = (startDate == null || endDate == null) ||
                    (reparation.getDateReparation() != null &&
                            !reparation.getDateReparation().isBefore(startDate) &&
                            !reparation.getDateReparation().isAfter(endDate));

            if (matchesPrestataire && matchesDateRange) {
                filteredReparations.add(reparation);
            }
        }

        return filteredReparations;
    }

    // Statistics methods
    public double getTotalCostByVehiculeId(Long vehiculeId) {
        List<Reparation> reparations = getReparationsByVehiculeId(vehiculeId);
        return reparations.stream()
                .mapToDouble(Reparation::getCout)
                .sum();
    }

    public double getTotalCostByDateRange(LocalDate startDate, LocalDate endDate) {
        List<Reparation> reparations = getReparationsByDateRange(startDate, endDate);
        return reparations.stream()
                .mapToDouble(Reparation::getCout)
                .sum();
    }

    public int countReparationsByVehiculeId(Long vehiculeId) {
        return getReparationsByVehiculeId(vehiculeId).size();
    }
}

