package org.example.Service;

import org.example.Entities.Candidat;
import org.example.Entities.Examen;
import org.example.Rep.CandidatRep;
import org.example.Rep.ExamenRep;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ExamenService {

    private CandidatRep candidatRep;

    private final ExamenRep examenRep;

    public ExamenService(ExamenRep examenRep) {
        this.examenRep = examenRep;
    }

    public void setCandidatRep(CandidatRep candidatRep) {
        this.candidatRep = candidatRep;
    }

    public Optional<Candidat> getCandidatById(Long candidatId) {
        return candidatRep.findById(candidatId);
    }

    // Create and Update
    public Examen saveExamen(Examen examen) {
        // Add any business logic/validation here
        return examenRep.save(examen);
    }

    // Read
    public List<Examen> getAllExamens() {
        return examenRep.findAll();
    }

    public Optional<Examen> getExamenById(Long id) {
        return examenRep.findById(id);
    }
    // Pour ExamenService.java
    public int countExamensByTypeAndCandidat(String typeExamen, Long candidatId) {
        List<Examen> allExamens = examenRep.findAll();
        return (int) allExamens.stream()
                .filter(examen -> examen.getCandidatId().equals(candidatId) &&
                        examen.getTypeExamen().equalsIgnoreCase(typeExamen))
                .count();
    }

    // Delete
    public void deleteExamen(Examen examen) {
        examenRep.delete(examen);
    }

    public void deleteExamenById(Long id) {
        examenRep.deleteById(id);
    }

    public List<Examen> searchExamens(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getAllExamens();
        }
        return examenRep.findBySearchTerm(searchTerm.trim());
    }

    // New method to search by CIN or name
    public List<Examen> searchExamensByCandidatCinOrName(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getAllExamens();
        }

        // First, find candidates matching the search term
        List<Candidat> matchingCandidats = candidatRep.findByNomOrPrenomOrCin(searchTerm.trim());

        if (matchingCandidats.isEmpty()) {
            return new ArrayList<>();
        }

        // Get all exams
        List<Examen> allExamens = examenRep.findAll();

        // Filter exams by matching candidates
        return allExamens.stream()
                .filter(examen -> matchingCandidats.stream()
                        .anyMatch(candidat -> candidat.getId().equals(examen.getCandidatId())))
                .collect(Collectors.toList());
    }

    // New method for filtering examens
    public List<Examen> filterExamens(String type, LocalDate startDate, LocalDate endDate, String status) {
        return examenRep.findByFilters(type, startDate, endDate, status);
    }
}

