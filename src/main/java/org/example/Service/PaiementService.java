package org.example.Service;

import org.example.Entities.Paiement;
import org.example.Rep.PaiementRep;

import java.time.LocalDate;
import java.time.Month;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PaiementService {

    private PaiementRep paiementRep;

    public PaiementService(PaiementRep paiementRep) {
        this.paiementRep = paiementRep;
    }

    public Paiement savePaiement(Paiement paiement) {
        return paiementRep.save(paiement);
    }

    public Paiement updatePaiement(Paiement paiement) {
        return paiementRep.update(paiement);
    }

    public boolean deletePaiement(Long id) {
        return paiementRep.delete(id);
    }
    // Pour PaiementService.java
    public Map<String, Double> getPaiementsByStatutSummary() {
        List<Paiement> allPaiements = paiementRep.getAll();
        Map<String, Double> summary = new HashMap<>();

        for (Paiement paiement : allPaiements) {
            String statut = paiement.getStatut();
            double montant = paiement.getMontant();

            summary.put(statut, summary.getOrDefault(statut, 0.0) + montant);
        }

        return summary;
    }

    public Paiement getPaiementById(Long id) {
        return paiementRep.getById(id);
    }

    public List<Paiement> getAllPaiements() {
        return paiementRep.getAll();
    }


    public double getTotalPaiements() {
        return paiementRep.getAll().stream()
                .mapToDouble(Paiement::getMontant)
                .sum();
    }

    public double getTotalPaiementsByCandidatId(Long candidatId) {
        return paiementRep.getByCandidatId(candidatId).stream()
                .mapToDouble(Paiement::getMontant)
                .sum();
    }

    public double getTotalPaiementsByDateRange(LocalDate startDate, LocalDate endDate) {
        return paiementRep.getByDateRange(startDate, endDate).stream()
                .mapToDouble(Paiement::getMontant)
                .sum();
    }


    public double getTotalPaiementsByMethodePaiement(String methodePaiement) {
        return paiementRep.getByMethodePaiement(methodePaiement).stream()
                .mapToDouble(Paiement::getMontant)
                .sum();
    }

    public double getTotalPaiementsByStatut(String statut) {
        return paiementRep.getByStatut(statut).stream()
                .mapToDouble(Paiement::getMontant)
                .sum();
    }

    public Map<String, Double> getPaiementsByTypeServiceSummary() {
        List<Paiement> allPaiements = paiementRep.getAll();
        Map<String, Double> summary = new HashMap<>();

        for (Paiement paiement : allPaiements) {
            String typeService = paiement.getTypeService();
            double montant = paiement.getMontant();

            summary.put(typeService, summary.getOrDefault(typeService, 0.0) + montant);
        }

        return summary;
    }

    public Map<String, Double> getPaiementsByMethodePaiementSummary() {
        List<Paiement> allPaiements = paiementRep.getAll();
        Map<String, Double> summary = new HashMap<>();

        for (Paiement paiement : allPaiements) {
            String methodePaiement = paiement.getMethodePaiement();
            double montant = paiement.getMontant();

            summary.put(methodePaiement, summary.getOrDefault(methodePaiement, 0.0) + montant);
        }

        return summary;
    }

    public Map<Month, Double> getPaiementsByMonthForYear(int year) {
        List<Paiement> allPaiements = paiementRep.getAll();
        Map<Month, Double> monthlyPaiements = new HashMap<>();

        for (Month month : Month.values()) {
            monthlyPaiements.put(month, 0.0);
        }

        for (Paiement paiement : allPaiements) {
            LocalDate date = paiement.getDatePaiement();
            if (date.getYear() == year) {
                Month month = date.getMonth();
                double montant = paiement.getMontant();

                monthlyPaiements.put(month, monthlyPaiements.get(month) + montant);
            }
        }

        return monthlyPaiements;
    }
}

