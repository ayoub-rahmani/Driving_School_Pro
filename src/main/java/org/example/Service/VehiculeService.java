package org.example.Service;

import org.example.Entities.TypePermis;
import org.example.Entities.Vehicule;
import org.example.Rep.VehiculeRep;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class VehiculeService {

    private final VehiculeRep vehiculeRep;
    private final AuditLogService auditLogService;

    public VehiculeService(VehiculeRep vehiculeRep) {
        this.vehiculeRep = vehiculeRep;
        this.auditLogService = new AuditLogService();
    }

    // Create and Update
    public Vehicule saveVehicule(Vehicule vehicule) {
        // Validate uniqueness before saving
        List<String> uniquenessErrors = validateUniqueness(vehicule);
        if (!uniquenessErrors.isEmpty()) {
            throw new IllegalArgumentException(String.join(", ", uniquenessErrors));
        }

        // Save the vehicle
        Vehicule savedVehicule = vehiculeRep.save(vehicule);

        // Log the action
        String action = (vehicule.getId() == null) ? "CREATE" : "UPDATE";
        auditLogService.logAction(
                action,
                "VEHICULE",
                savedVehicule.getId(),
                action + " vehicule " + savedVehicule.getMarque() + " " + savedVehicule.getModele() + " (" + savedVehicule.getMatricule() + ")"
        );

        return savedVehicule;
    }

    // Validate uniqueness of fields
    public List<String> validateUniqueness(Vehicule vehicule) {
        List<String> errors = new ArrayList<>();
        Long excludeId = vehicule.getId(); // Will be null for new records

        // Check matricule uniqueness
        if (vehiculeRep.existsByMatricule(vehicule.getMatricule(), excludeId)) {
            errors.add("Un véhicule avec cette immatriculation existe déjà");
        }

        return errors;
    }

    // Read
    public List<Vehicule> getAllVehicules() {
        return vehiculeRep.findAll();
    }

    public Optional<Vehicule> getVehiculeById(Long id) {
        return vehiculeRep.findById(id);
    }

    public List<Vehicule> searchVehicules(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getAllVehicules();
        }
        return vehiculeRep.findByMarqueOrModeleOrMatricule(searchTerm.trim());
    }

    // Delete
    public void deleteVehicule(Vehicule vehicule) {
        if (vehicule != null && vehicule.getId() != null) {
            // Log the delete action
            auditLogService.logAction(
                    "DELETE",
                    "VEHICULE",
                    vehicule.getId(),
                    "Suppression du véhicule " + vehicule.getMarque() + " " + vehicule.getModele() + " (" + vehicule.getMatricule() + ")"
            );

            vehiculeRep.delete(vehicule);
        }
    }

    public void deleteVehiculeById(Long id) {
        Optional<Vehicule> vehicule = getVehiculeById(id);
        if (vehicule.isPresent()) {
            deleteVehicule(vehicule.get());
        } else {
            vehiculeRep.deleteById(id);
        }
    }

    // Business methods
    public List<Vehicule> getAvailableVehicules() {
        return vehiculeRep.findByDisponible(true);
    }

    public List<Vehicule> getVehiculesByType(TypePermis type) {
        return vehiculeRep.findByType(type);
    }

    public List<Vehicule> getExpiringDocuments(int daysThreshold) {
        return vehiculeRep.findExpiringDocuments(daysThreshold);
    }

    public List<Vehicule> filterVehicules(String typeFilter, String disponibiliteFilter) {
        List<Vehicule> allVehicules = getAllVehicules();

        return allVehicules.stream()
                .filter(v -> {
                    // Filter by type
                    if (typeFilter != null && !typeFilter.equals("Tous les types")) {
                        TypePermis type;
                        try {
                            type = TypePermis.valueOf(typeFilter);
                            if (v.getType() != type) return false;
                        } catch (IllegalArgumentException e) {
                            // Invalid type filter, ignore it
                        }
                    }

                    // Filter by disponibilité
                    if (disponibiliteFilter != null && !disponibiliteFilter.equals("Tous les statuts")) {
                        boolean isDisponible = disponibiliteFilter.equals("Disponible");
                        if (v.isDisponible() != isDisponible) return false;
                    }

                    return true;
                })
                .collect(Collectors.toList());
    }

    // Statistics methods
    public int countVehiculesByType(TypePermis type) {
        return (int) getAllVehicules().stream()
                .filter(v -> v.getType() == type)
                .count();
    }

    public int countAvailableVehicules() {
        return (int) getAllVehicules().stream()
                .filter(Vehicule::isDisponible)
                .count();
    }

    public int countVehiculesWithExpiringDocuments(int daysThreshold) {
        LocalDate thresholdDate = LocalDate.now().plusDays(daysThreshold);

        return (int) getAllVehicules().stream()
                .filter(v ->
                        (v.getDateVignette() != null && !v.getDateVignette().isBefore(LocalDate.now()) && v.getDateVignette().isBefore(thresholdDate)) ||
                                (v.getDateAssurance() != null && !v.getDateAssurance().isBefore(LocalDate.now()) && v.getDateAssurance().isBefore(thresholdDate)) ||
                                (v.getDateVisiteTechnique() != null && !v.getDateVisiteTechnique().isBefore(LocalDate.now()) && v.getDateVisiteTechnique().isBefore(thresholdDate)) ||
                                (v.getDateProchainEntretien() != null && !v.getDateProchainEntretien().isBefore(LocalDate.now()) && v.getDateProchainEntretien().isBefore(thresholdDate))
                )
                .count();
    }

    // Methods for filtering by specific document expiration
    public List<Vehicule> getVehiculesWithExpiringVignette(int daysThreshold) {
        LocalDate thresholdDate = LocalDate.now().plusDays(daysThreshold);

        return getAllVehicules().stream()
                .filter(v -> v.getDateVignette() != null &&
                        !v.getDateVignette().isBefore(LocalDate.now()) &&
                        v.getDateVignette().isBefore(thresholdDate))
                .collect(Collectors.toList());
    }

    public List<Vehicule> getVehiculesWithExpiringAssurance(int daysThreshold) {
        LocalDate thresholdDate = LocalDate.now().plusDays(daysThreshold);

        return getAllVehicules().stream()
                .filter(v -> v.getDateAssurance() != null &&
                        !v.getDateAssurance().isBefore(LocalDate.now()) &&
                        v.getDateAssurance().isBefore(thresholdDate))
                .collect(Collectors.toList());
    }

    public List<Vehicule> getVehiculesWithExpiringVisiteTechnique(int daysThreshold) {
        LocalDate thresholdDate = LocalDate.now().plusDays(daysThreshold);

        return getAllVehicules().stream()
                .filter(v -> v.getDateVisiteTechnique() != null &&
                        !v.getDateVisiteTechnique().isBefore(LocalDate.now()) &&
                        v.getDateVisiteTechnique().isBefore(thresholdDate))
                .collect(Collectors.toList());
    }

    public List<Vehicule> getVehiculesWithExpiringEntretien(int daysThreshold) {
        LocalDate thresholdDate = LocalDate.now().plusDays(daysThreshold);

        return getAllVehicules().stream()
                .filter(v -> v.getDateProchainEntretien() != null &&
                        !v.getDateProchainEntretien().isBefore(LocalDate.now()) &&
                        v.getDateProchainEntretien().isBefore(thresholdDate))
                .collect(Collectors.toList());
    }
}

