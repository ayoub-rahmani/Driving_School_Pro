package org.example.Service;

import org.example.Entities.Moniteur;
import org.example.Entities.Seance;
import org.example.Entities.TypePermis;
import org.example.Entities.TypeSeance;
import org.example.Entities.Vehicule;
import org.example.Rep.MoniteurRep;
import org.example.Rep.SeanceRep;
import org.example.Rep.VehiculeRep;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class SeanceService {
    private final SeanceRep seanceRep;
    private final VehiculeRep vehiculeRep;
    private final MoniteurRep moniteurRep;

    public SeanceService(SeanceRep seanceRep, VehiculeRep vehiculeRep, MoniteurRep moniteurRep) {
        this.seanceRep = seanceRep;
        this.vehiculeRep = vehiculeRep;
        this.moniteurRep = moniteurRep;
    }



    public void deleteSeance(int id) {
        seanceRep.deleteById(id);
    }


    public List<Seance> getSeancesByMoniteur(long moniteurId) {
        return seanceRep.findByMoniteur(moniteurId);
    }

    public List<Seance> getSeancesByCandidat(long candidatId) {
        return seanceRep.findByCandidat(candidatId);
    }

    public List<Seance> getSeancesByVehicule(int vehiculeId) {
        return seanceRep.findByVehicule(vehiculeId);
    }

    public List<Seance> getSeancesByTypeSeance(TypeSeance typeSeance) {
        return seanceRep.findByTypeSeance(typeSeance);
    }

    // Méthode pour compter les séances par type et candidat
    public int countSeancesByTypeAndCandidat(TypeSeance typeSeance, Long candidatId) {
        List<Seance> seances = seanceRep.findByCandidat(candidatId);
        return (int) seances.stream()
                .filter(seance -> seance.getTypeseance() == typeSeance)
                .count();
    }

    // CRUD operations
    public Seance saveSeance(Seance seance) {
        return seanceRep.save(seance);
    }

    public List<Seance> getAllSeances() {
        return seanceRep.findAll();
    }

    public Optional<Seance> getSeanceById(int id) {
        return seanceRep.findById(id);
    }

    public void deleteSeance(Seance seance) {
        seanceRep.delete(seance);
    }

    // Additional operations
    public List<Seance> findByMoniteur(long id_moniteur) {
        return seanceRep.findByMoniteur(id_moniteur);
    }

    public List<Seance> findByCandidat(long id_candidat) {
        return seanceRep.findByCandidat(id_candidat);
    }

    public List<Seance> findByVehicule(int id_vehicule) {
        return seanceRep.findByVehicule(id_vehicule);
    }

    public List<Seance> findByDate(LocalDateTime date) {
        return seanceRep.findByDate(date);
    }

    public List<Seance> findByTypeSeance(TypeSeance typeSeance) {
        return seanceRep.findByTypeSeance(typeSeance);
    }

    public List<Seance> findByTypePermis(TypePermis typePermis) {
        return seanceRep.findByTypePermis(typePermis);
    }

    public List<Seance> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return seanceRep.findByDateRange(startDate, endDate);
    }

    public List<Seance> findConflictingSeances(Seance seance) {
        return seanceRep.findConflictingSeances(seance);
    }

    public List<Seance> searchSeances(String searchTerm) {
        return seanceRep.searchSeances(searchTerm);
    }

    public List<Seance> filterSeances(TypeSeance typeSeance, TypePermis typePermis,
                                      LocalDateTime startDate, LocalDateTime endDate,
                                      Long moniteurId, Long candidatId, Integer vehiculeId) {
        return seanceRep.filterSeances(typeSeance, typePermis, startDate, endDate, moniteurId, candidatId, vehiculeId);
    }

    /**
     * Check if a moniteur is available at a specific time
     * @param moniteurId The moniteur ID
     * @param dateTime The date and time to check
     * @return true if available, false if already booked
     */
    public boolean isMoniteurAvailableAt(long moniteurId, LocalDateTime dateTime) {
        // Calculate the time window (1.5 hours before and after)
        LocalDateTime startWindow = dateTime.minusHours(1).minusMinutes(30);
        LocalDateTime endWindow = dateTime.plusHours(1).plusMinutes(30);

        // Find any seances for this moniteur in the time window
        List<Seance> conflictingSeances = seanceRep.findByMoniteurAndTimeRange(
                moniteurId, startWindow, endWindow);

        // If there are no conflicting seances, the moniteur is available
        return conflictingSeances.isEmpty();
    }

    /**
     * Check if a vehicle is available at a specific time
     * @param vehiculeId The vehicle ID
     * @param dateTime The date and time to check
     * @return true if available, false if already booked
     */
    public boolean isVehiculeAvailableAt(int vehiculeId, LocalDateTime dateTime) {
        // Calculate the time window (1.5 hours before and after)
        LocalDateTime startWindow = dateTime.minusHours(1).minusMinutes(30);
        LocalDateTime endWindow = dateTime.plusHours(1).plusMinutes(30);

        // Find any seances for this vehicle in the time window
        List<Seance> conflictingSeances = seanceRep.findByVehiculeAndTimeRange(
                vehiculeId, startWindow, endWindow);

        // If there are no conflicting seances, the vehicle is available
        return conflictingSeances.isEmpty();
    }

    // Add a new method to check if a candidate is available at a specific time
    public boolean isCandidatAvailableAt(long candidatId, LocalDateTime dateTime) {
        // Calculate the time window (1.5 hours before and after)
        LocalDateTime startWindow = dateTime.minusHours(1).minusMinutes(30);
        LocalDateTime endWindow = dateTime.plusHours(1).plusMinutes(30);

        // Find any seances for this candidate in the time window
        List<Seance> conflictingSeances = seanceRep.findByCandidatAndTimeRange(
                candidatId, startWindow, endWindow);

        // If there are no conflicting seances, the candidate is available
        return conflictingSeances.isEmpty();
    }

    /**
     * Get all available moniteurs at a specific time
     * @param dateTime The date and time to check
     * @param typePermis The type of license (optional)
     * @return List of available moniteurs
     */
    public List<Moniteur> getAvailableMoniteursAt(LocalDateTime dateTime, TypePermis typePermis) {
        // Get all moniteurs
        List<Moniteur> allMoniteurs = moniteurRep.findAll();

        // Filter by availability
        List<Moniteur> availableMoniteurs = allMoniteurs.stream()
                .filter(m -> m.isDisponible() && isMoniteurAvailableAt(m.getId(), dateTime))
                .collect(Collectors.toList());

        // Filter by permis type if specified
        if (typePermis != null) {
            String permisCategory = convertTypePermisToCategory(typePermis);
            availableMoniteurs = availableMoniteurs.stream()
                    .filter(m -> m.getCategoriesPermis().contains(permisCategory))
                    .collect(Collectors.toList());
        }

        return availableMoniteurs;
    }

    /**
     * Get all available vehicles at a specific time
     * @param dateTime The date and time to check
     * @param typePermis The type of license (optional)
     * @return List of available vehicles
     */
    public List<Vehicule> getAvailableVehiculesAt(LocalDateTime dateTime, TypePermis typePermis) {
        // Get all vehicles
        List<Vehicule> allVehicules = vehiculeRep.findAll();

        // Filter by availability
        List<Vehicule> availableVehicules = allVehicules.stream()
                .filter(v -> v.isDisponible() && isVehiculeAvailableAt(v.getId().intValue(), dateTime))
                .collect(Collectors.toList());

        // Filter by permis type if specified
        if (typePermis != null) {
            availableVehicules = availableVehicules.stream()
                    .filter(v -> v.getType() == typePermis)
                    .collect(Collectors.toList());
        }

        return availableVehicules;
    }

    /**
     * Helper method to convert TypePermis to category string
     */
    public String convertTypePermisToCategory(TypePermis typePermis) {
        switch (typePermis) {
            case Moto:
                return "A";
            case Voiture:
                return "B";
            case Camion:
                return "C";
            default:
                return "";
        }
    }

    /**
     * Generate a seance automatically with available moniteur and vehicule
     * Prioritizes moniteurs and vehicules that the candidate has used before
     */
    public Seance generateSeance(long candidatId, LocalDateTime dateTime, TypePermis typePermis,
                                 TypeSeance typeSeance) {
        // Find all seances for this candidate
        List<Seance> candidatSeances = seanceRep.findByCandidat(candidatId);

        // Find available moniteurs
        List<Moniteur> availableMoniteurs = getAvailableMoniteursAt(dateTime, typePermis);
        if (availableMoniteurs.isEmpty()) {
            throw new RuntimeException("Aucun moniteur disponible pour cette date et ce type de permis");
        }

        // Try to find a moniteur that the candidate has worked with before
        Moniteur selectedMoniteur = null;
        for (Seance pastSeance : candidatSeances) {
            long pastMoniteurId = pastSeance.getId_moniteur();
            for (Moniteur moniteur : availableMoniteurs) {
                if (moniteur.getId() == pastMoniteurId) {
                    selectedMoniteur = moniteur;
                    break;
                }
            }
            if (selectedMoniteur != null) break;
        }

        // If no previous moniteur is available, select the first available one
        if (selectedMoniteur == null) {
            selectedMoniteur = availableMoniteurs.get(0);
        }

        // Create seance
        Seance seance = new Seance();
        seance.setId_candidat(candidatId);
        seance.setId_moniteur(selectedMoniteur.getId());
        seance.setDate_debut(dateTime);
        seance.setTypeseance(typeSeance);
        seance.setTypepermis(typePermis);

        // If it's a driving lesson, find an available vehicle
        if (typeSeance == TypeSeance.Conduite) {
            List<Vehicule> availableVehicules = getAvailableVehiculesAt(dateTime, typePermis);
            if (availableVehicules.isEmpty()) {
                throw new RuntimeException("Aucun véhicule disponible pour cette date et ce type de permis");
            }

            // Try to find a vehicle that the candidate has used before
            Vehicule selectedVehicule = null;
            for (Seance pastSeance : candidatSeances) {
                Integer pastVehiculeId = pastSeance.getId_vehicule();
                if (pastVehiculeId != null) {
                    for (Vehicule vehicule : availableVehicules) {
                        if (vehicule.getId().intValue() == pastVehiculeId) {
                            selectedVehicule = vehicule;
                            break;
                        }
                    }
                }
            if (selectedVehicule != null)
                break;
        }

        // If no previous vehicle is available, select the first available one
        if (selectedVehicule == null) {
            selectedVehicule = availableVehicules.get(0);
        }

        seance.setId_vehicule(selectedVehicule.getId().intValue());
    }

    // Save and return the seance
        return saveSeance(seance);
}
}

