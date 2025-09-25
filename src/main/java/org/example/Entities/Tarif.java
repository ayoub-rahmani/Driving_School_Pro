package org.example.Entities;

public class Tarif {
    private Long id;
    private String typeService;
    private double montant;
    private String description;
    private boolean actif;
    private double remise;

    // Constructeur par défaut
    public Tarif() {
        this.actif = true;
        this.remise = 0.0;
    }

    // Constructeur avec tous les champs sauf remise
    public Tarif(Long id, String typeService, double montant, String description) {
        this.id = id;
        this.typeService = typeService;
        this.montant = montant;
        this.description = description;
        this.actif = true;
        this.remise = 0.0;
    }

    // Constructeur avec tous les champs
    public Tarif(Long id, String typeService, double montant, String description, double remise) {
        this.id = id;
        this.typeService = typeService;
        this.montant = montant;
        this.description = description;
        this.actif = true;
        this.remise = remise;
    }

    // Constructeur complet
    public Tarif(Long id, String typeService, double montant, String description, boolean actif, double remise) {
        this.id = id;
        this.typeService = typeService;
        this.montant = montant;
        this.description = description;
        this.actif = actif;
        this.remise = remise;
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTypeService() {
        return typeService;
    }

    public void setTypeService(String typeService) {
        this.typeService = typeService;
    }

    public double getMontant() {
        return montant;
    }

    public void setMontant(double montant) {
        this.montant = montant;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isActif() {
        return actif;
    }

    public void setActif(boolean actif) {
        this.actif = actif;
    }

    public double getRemise() {
        return remise;
    }

    public void setRemise(double remise) {
        this.remise = remise;
    }

    @Override
    public String toString() {
        return "Tarif{" +
                "id=" + id +
                ", typeService='" + typeService + '\'' +
                ", montant=" + montant +
                ", description='" + description + '\'' +
                ", actif=" + actif +
                ", remise=" + remise +
                '}';
    }
}

