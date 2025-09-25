package org.example.Entities;

import java.time.LocalDate;

public class Depense {
    private Long id;
    private String categorie;
    private double montant;
    private LocalDate dateDepense;
    private String description;
    private Long vehiculeId;
    private Long moniteurId;
    private String typeVehiculeDepense;
    private Long reparationId;
    private String typeAutreDepense;
    private boolean paye; // Added paye field
    private LocalDate dateCreation; // Added dateCreation field
    private LocalDate dateModification; // Added dateModification field

    // Constructeur par défaut
    public Depense() {
        this.paye = false; // Default to unpaid
        this.dateCreation = LocalDate.now();
    }

    // Constructeur avec tous les champs
    public Depense(Long id, String categorie, double montant, LocalDate dateDepense,
                   String description, Long vehiculeId, Long moniteurId) {
        this.id = id;
        this.categorie = categorie;
        this.montant = montant;
        this.dateDepense = dateDepense;
        this.description = description;
        this.vehiculeId = vehiculeId;
        this.moniteurId = moniteurId;
        this.paye = false; // Default to unpaid
        this.dateCreation = LocalDate.now();
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCategorie() {
        return categorie;
    }

    public void setCategorie(String categorie) {
        this.categorie = categorie;
    }

    public double getMontant() {
        return montant;
    }

    public void setMontant(double montant) {
        this.montant = montant;
    }

    public LocalDate getDateDepense() {
        return dateDepense;
    }

    public void setDateDepense(LocalDate dateDepense) {
        this.dateDepense = dateDepense;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getVehiculeId() {
        return vehiculeId;
    }

    public void setVehiculeId(Long vehiculeId) {
        this.vehiculeId = vehiculeId;
    }

    public Long getMoniteurId() {
        return moniteurId;
    }

    public void setMoniteurId(Long moniteurId) {
        this.moniteurId = moniteurId;
    }

    public String getTypeVehiculeDepense() {
        return typeVehiculeDepense;
    }

    public void setTypeVehiculeDepense(String typeVehiculeDepense) {
        this.typeVehiculeDepense = typeVehiculeDepense;
    }

    public Long getReparationId() {
        return reparationId;
    }

    public void setReparationId(Long reparationId) {
        this.reparationId = reparationId;
    }

    public String getTypeAutreDepense() {
        return typeAutreDepense;
    }

    public void setTypeAutreDepense(String typeAutreDepense) {
        this.typeAutreDepense = typeAutreDepense;
    }

    // Added getter and setter for paye field
    public boolean isPaye() {
        return paye;
    }

    public void setPaye(boolean paye) {
        this.paye = paye;
    }

    // Added getters and setters for date fields
    public LocalDate getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDate dateCreation) {
        this.dateCreation = dateCreation;
    }

    public LocalDate getDateModification() {
        return dateModification;
    }

    public void setDateModification(LocalDate dateModification) {
        this.dateModification = dateModification;
    }

    @Override
    public String toString() {
        return "Depense{" +
                "id=" + id +
                ", categorie='" + categorie + '\'' +
                ", montant=" + montant +
                ", dateDepense=" + dateDepense +
                ", description='" + description + '\'' +
                ", vehiculeId=" + vehiculeId +
                ", moniteurId=" + moniteurId +
                ", typeVehiculeDepense='" + typeVehiculeDepense + '\'' +
                ", reparationId=" + reparationId +
                ", typeAutreDepense='" + typeAutreDepense + '\'' +
                ", paye=" + paye +
                ", dateCreation=" + dateCreation +
                ", dateModification=" + dateModification +
                '}';
    }
}