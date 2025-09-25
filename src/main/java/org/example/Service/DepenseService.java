package org.example.Service;

import org.example.Entities.Depense;
import org.example.Rep.DepenseRep;

import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DepenseService {

    private DepenseRep depenseRep;

    public DepenseService(DepenseRep depenseRep) {
        this.depenseRep = depenseRep;
    }

    public Depense saveDepense(Depense depense) {
        return depenseRep.save(depense);
    }

    public Depense updateDepense(Depense depense) {
        return depenseRep.update(depense);
    }

    public boolean deleteDepense(Long id) {
        return depenseRep.delete(id);
    }

    public Depense getDepenseById(Long id) {
        return depenseRep.getById(id);
    }

    public List<Depense> getAllDepenses() {
        return depenseRep.getAll();
    }

    public List<Depense> getDepensesByVehiculeId(Long vehiculeId) {
        return depenseRep.getByVehiculeId(vehiculeId);
    }

    public List<Depense> getDepensesByMoniteurId(Long moniteurId) {
        return depenseRep.getByMoniteurId(moniteurId);
    }

    public List<Depense> getDepensesByDateRange(LocalDate startDate, LocalDate endDate) {
        return depenseRep.getByDateRange(startDate, endDate);
    }

    public List<Depense> getDepensesByCategorie(String categorie) {
        return depenseRep.getByCategorie(categorie);
    }

    public double getTotalDepenses() {
        return depenseRep.getAll().stream()
                .mapToDouble(Depense::getMontant)
                .sum();
    }

    public double getTotalDepensesByCategorie(String categorie) {
        return depenseRep.getByCategorie(categorie).stream()
                .mapToDouble(Depense::getMontant)
                .sum();
    }

    public double getTotalDepensesByDateRange(LocalDate startDate, LocalDate endDate) {
        return depenseRep.getByDateRange(startDate, endDate).stream()
                .mapToDouble(Depense::getMontant)
                .sum();
    }

    public double getTotalDepensesByVehiculeId(Long vehiculeId) {
        return depenseRep.getByVehiculeId(vehiculeId).stream()
                .mapToDouble(Depense::getMontant)
                .sum();
    }

    public double getTotalDepensesByMoniteurId(Long moniteurId) {
        return depenseRep.getByMoniteurId(moniteurId).stream()
                .mapToDouble(Depense::getMontant)
                .sum();
    }

    public Map<String, Double> getDepensesByCategorieSummary() {
        List<Depense> allDepenses = depenseRep.getAll();
        Map<String, Double> summary = new HashMap<>();

        for (Depense depense : allDepenses) {
            String categorie = depense.getCategorie();
            double montant = depense.getMontant();

            summary.put(categorie, summary.getOrDefault(categorie, 0.0) + montant);
        }

        return summary;
    }

    public Map<Month, Double> getDepensesByMonthForYear(int year) {
        List<Depense> allDepenses = depenseRep.getAll();
        Map<Month, Double> monthlyDepenses = new HashMap<>();

        for (Month month : Month.values()) {
            monthlyDepenses.put(month, 0.0);
        }

        for (Depense depense : allDepenses) {
            LocalDate date = depense.getDateDepense();
            if (date.getYear() == year) {
                Month month = date.getMonth();
                double montant = depense.getMontant();

                monthlyDepenses.put(month, monthlyDepenses.get(month) + montant);
            }
        }

        return monthlyDepenses;
    }
}

