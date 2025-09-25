package org.example.Entities;

import java.time.LocalDate;

public class Examen {
    private Long id;
    private Long candidatId;  // Foreign key to Candidat
    private String typeExamen; // "Code" or "Conduite"
    private LocalDate dateExamen;
    private String lieuExamen;
    private double fraisInscription;
    private boolean estValide;

    public Examen(Long id, Long candidatId, String typeExamen, LocalDate dateExamen, String lieuExamen, double fraisInscription, boolean estValide) {
        this.id = id;
        this.candidatId = candidatId;
        this.typeExamen = typeExamen;
        this.dateExamen = dateExamen;
        this.lieuExamen = lieuExamen;
        this.fraisInscription = fraisInscription;
        this.estValide = estValide;
    }

    public Examen() {

    }

    // Getters and setters for all fields
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCandidatId() {
        return candidatId;
    }

    public void setCandidatId(Long candidatId) {
        this.candidatId = candidatId;
    }

    public String getTypeExamen() {
        return typeExamen;
    }

    public void setTypeExamen(String typeExamen) {
        this.typeExamen = typeExamen;
    }

    public LocalDate getDateExamen() {
        return dateExamen;
    }

    public void setDateExamen(LocalDate dateExamen) {
        this.dateExamen = dateExamen;
    }

    public String getLieuExamen() {
        return lieuExamen;
    }

    public void setLieuExamen(String lieuExamen) {
        this.lieuExamen = lieuExamen;
    }

    public double getFraisInscription() {
        return fraisInscription;
    }

    public void setFraisInscription(double fraisInscription) {
        this.fraisInscription = fraisInscription;
    }

    public boolean isEstValide() {
        return estValide;
    }

    public void setEstValide(boolean estValide) {
        this.estValide = estValide;
    }
}