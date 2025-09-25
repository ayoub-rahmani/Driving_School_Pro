package org.example.Entities;

import java.time.LocalDate;
import java.util.Objects;

public class Vehicule {
    private Long id;
    private String marque;
    private String modele;
    private String matricule;
    private int kilometrage;
    private TypePermis type;
    private LocalDate dateMiseEnService;
    private LocalDate dateProchainEntretien;
    private LocalDate dateVignette;
    private LocalDate dateAssurance;
    private LocalDate dateVisiteTechnique;
    private String papiers;
    private boolean disponible;
    private String motifIndisponibilite;
    private String notes;

    public Vehicule() {
    }

    public Vehicule(Long id, String marque, String modele, String matricule, int kilometrage, TypePermis type,
                    LocalDate dateMiseEnService, LocalDate dateProchainEntretien, LocalDate dateVignette,
                    LocalDate dateAssurance, LocalDate dateVisiteTechnique, String papiers, boolean disponible,
                    String motifIndisponibilite, String notes) {
        this.id = id;
        this.marque = marque;
        this.modele = modele;
        this.matricule = matricule;
        this.kilometrage = kilometrage;
        this.type = type;
        this.dateMiseEnService = dateMiseEnService;
        this.dateProchainEntretien = dateProchainEntretien;
        this.dateVignette = dateVignette;
        this.dateAssurance = dateAssurance;
        this.dateVisiteTechnique = dateVisiteTechnique;
        this.papiers = papiers;
        this.disponible = disponible;
        this.motifIndisponibilite = motifIndisponibilite;
        this.notes = notes;
    }

    // Constructor without ID for new vehicles
    public Vehicule(String marque, String modele, String matricule, int kilometrage, TypePermis type,
                    LocalDate dateMiseEnService, LocalDate dateProchainEntretien, LocalDate dateVignette,
                    LocalDate dateAssurance, LocalDate dateVisiteTechnique, String papiers, boolean disponible,
                    String motifIndisponibilite, String notes) {
        this(null, marque, modele, matricule, kilometrage, type, dateMiseEnService, dateProchainEntretien,
                dateVignette, dateAssurance, dateVisiteTechnique, papiers, disponible, motifIndisponibilite, notes);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMarque() {
        return marque;
    }

    public void setMarque(String marque) {
        this.marque = marque;
    }

    public String getModele() {
        return modele;
    }

    public void setModele(String modele) {
        this.modele = modele;
    }

    public String getMatricule() {
        return matricule;
    }

    public void setMatricule(String matricule) {
        this.matricule = matricule;
    }

    public int getKilometrage() {
        return kilometrage;
    }

    public void setKilometrage(int kilometrage) {
        this.kilometrage = kilometrage;
    }

    public TypePermis getType() {
        return type;
    }

    public void setType(TypePermis type) {
        this.type = type;
    }

    public LocalDate getDateMiseEnService() {
        return dateMiseEnService;
    }

    public void setDateMiseEnService(LocalDate dateMiseEnService) {
        this.dateMiseEnService = dateMiseEnService;
    }

    public LocalDate getDateProchainEntretien() {
        return dateProchainEntretien;
    }

    public void setDateProchainEntretien(LocalDate dateProchainEntretien) {
        this.dateProchainEntretien = dateProchainEntretien;
    }

    public LocalDate getDateVignette() {
        return dateVignette;
    }

    public void setDateVignette(LocalDate dateVignette) {
        this.dateVignette = dateVignette;
    }

    public LocalDate getDateAssurance() {
        return dateAssurance;
    }

    public void setDateAssurance(LocalDate dateAssurance) {
        this.dateAssurance = dateAssurance;
    }

    public LocalDate getDateVisiteTechnique() {
        return dateVisiteTechnique;
    }

    public void setDateVisiteTechnique(LocalDate dateVisiteTechnique) {
        this.dateVisiteTechnique = dateVisiteTechnique;
    }

    public String getPapiers() {
        return papiers;
    }

    public void setPapiers(String papiers) {
        this.papiers = papiers;
    }

    public boolean isDisponible() {
        return disponible;
    }

    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }

    public String getMotifIndisponibilite() {
        return motifIndisponibilite;
    }

    public void setMotifIndisponibilite(String motifIndisponibilite) {
        this.motifIndisponibilite = motifIndisponibilite;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vehicule vehicule = (Vehicule) o;
        return Objects.equals(id, vehicule.id) ||
                Objects.equals(matricule, vehicule.matricule);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, matricule);
    }

    @Override
    public String toString() {
        return getMarque() +" "+ getMatricule();
    }

    // Helper method to check if a document is about to expire
    public boolean isDocumentExpiring(LocalDate documentDate, int daysThreshold) {
        if (documentDate == null) {
            return false;
        }
        LocalDate today = LocalDate.now();
        return !documentDate.isBefore(today) &&
                documentDate.isBefore(today.plusDays(daysThreshold));
    }

    // Check if any document is expiring soon
    public boolean hasExpiringDocuments(int daysThreshold) {
        return isDocumentExpiring(dateVignette, daysThreshold) ||
                isDocumentExpiring(dateAssurance, daysThreshold) ||
                isDocumentExpiring(dateVisiteTechnique, daysThreshold) ||
                isDocumentExpiring(dateProchainEntretien, daysThreshold);
    }
}

