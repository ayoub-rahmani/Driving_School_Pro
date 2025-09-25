package org.example.Entities;

import java.time.LocalDate;
import java.util.Objects;

public class Reparation {
    private Long id;
    private Long vehiculeId;
    private Long factureId;
    private String description;
    private LocalDate dateReparation;
    private double cout;
    private String prestataire;
    private String facturePath;
    private String notes;
    private boolean paye;

    public Reparation() {
    }

    public Reparation(Long id, Long vehiculeId, Long factureId, String description, LocalDate dateReparation,
                      double cout, String prestataire, String facturePath, String notes) {
        this.id = id;
        this.vehiculeId = vehiculeId;
        this.factureId = factureId;
        this.description = description;
        this.dateReparation = dateReparation;
        this.cout = cout;
        this.prestataire = prestataire;
        this.facturePath = facturePath;
        this.notes = notes;
    }

    // Constructor without ID for new repairs
    public Reparation(Long vehiculeId, Long factureId, String description, LocalDate dateReparation,
                      double cout, String prestataire, String facturePath, String notes) {
        this(null, vehiculeId, factureId, description, dateReparation, cout, prestataire, facturePath, notes);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getVehiculeId() {
        return vehiculeId;
    }

    public void setVehiculeId(Long vehiculeId) {
        this.vehiculeId = vehiculeId;
    }

    public Long getFactureId() {
        return factureId;
    }

    public void setFactureId(Long factureId) {
        this.factureId = factureId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getDateReparation() {
        return dateReparation;
    }

    public void setDateReparation(LocalDate dateReparation) {
        this.dateReparation = dateReparation;
    }

    public double getCout() {
        return cout;
    }

    public void setCout(double cout) {
        this.cout = cout;
    }

    public String getPrestataire() {
        return prestataire;
    }

    public void setPrestataire(String prestataire) {
        this.prestataire = prestataire;
    }

    public String getFacturePath() {
        return facturePath;
    }

    public void setFacturePath(String facturePath) {
        this.facturePath = facturePath;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public boolean isPaye() {
        return paye;
    }

    public void setPaye(boolean paye) {
        this.paye = paye;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Reparation that = (Reparation) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return getDescription();
    }
}

