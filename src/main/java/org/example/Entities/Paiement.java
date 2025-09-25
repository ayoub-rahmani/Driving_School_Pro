package org.example.Entities;

import java.time.LocalDate;

public class Paiement {
    private Long id;
    private Long candidatId;
    private LocalDate datePaiement;
    private double montant;
    private String methodePaiement;
    private String reference;
    private String typeService;
    private Long seanceId;
    private Long examenId;
    private String statut;
    private String notes;
    private double remise;

    // Constructeur par défaut
    public Paiement() {
    }

    // Constructeur avec tous les champs
    public Paiement(Long id, Long candidatId, LocalDate datePaiement, double montant,
                    String methodePaiement, String reference, String statut, String notes) {
        this.id = id;
        this.candidatId = candidatId;
        this.datePaiement = datePaiement;
        this.montant = montant;
        this.methodePaiement = methodePaiement;
        this.reference = reference;
        this.statut = statut;
        this.notes = notes;
    }

    // Constructeur complet avec tous les champs
    public Paiement(Long id, Long candidatId, LocalDate datePaiement, double montant,
                    String methodePaiement, String reference, String typeService,
                    Long seanceId, Long examenId, String statut, String notes, double remise) {
        this.id = id;
        this.candidatId = candidatId;
        this.datePaiement = datePaiement;
        this.montant = montant;
        this.methodePaiement = methodePaiement;
        this.reference = reference;
        this.typeService = typeService;
        this.seanceId = seanceId;
        this.examenId = examenId;
        this.statut = statut;
        this.notes = notes;
        this.remise = remise;
    }

    // Getters et Setters
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

    public LocalDate getDatePaiement() {
        return datePaiement;
    }

    public void setDatePaiement(LocalDate datePaiement) {
        this.datePaiement = datePaiement;
    }

    public double getMontant() {
        return montant;
    }

    public void setMontant(double montant) {
        this.montant = montant;
    }

    public String getMethodePaiement() {
        return methodePaiement;
    }

    public void setMethodePaiement(String methodePaiement) {
        this.methodePaiement = methodePaiement;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getTypeService() {
        return typeService;
    }

    public void setTypeService(String typeService) {
        this.typeService = typeService;
    }

    public Long getSeanceId() {
        return seanceId;
    }

    public void setSeanceId(Long seanceId) {
        this.seanceId = seanceId;
    }

    public Long getExamenId() {
        return examenId;
    }

    public void setExamenId(Long examenId) {
        this.examenId = examenId;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public double getRemise() {
        return remise;
    }

    public void setRemise(double remise) {
        this.remise = remise;
    }

    @Override
    public String toString() {
        return "Paiement{" +
                "id=" + id +
                ", candidatId=" + candidatId +
                ", datePaiement=" + datePaiement +
                ", montant=" + montant +
                ", methodePaiement='" + methodePaiement + '\'' +
                ", reference='" + reference + '\'' +
                ", typeService='" + typeService + '\'' +
                ", seanceId=" + seanceId +
                ", examenId=" + examenId +
                ", statut='" + statut + '\'' +
                ", notes='" + notes + '\'' +
                ", remise=" + remise +
                '}';
    }
}

