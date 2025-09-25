package org.example.Entities;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Entité représentant une séance de formation
 */
public class Seance {
    private int id_seance;
    private long id_moniteur;
    private long id_candidat;
    private Integer id_vehicule; // Nullable pour les séances de code
    private LocalDateTime date_debut;
    private TypeSeance typeseance;
    private TypePermis typepermis;
    private float longtitude;
    private float latitude;
    private String adresse; // Ajout du champ adresse pour stocker l'adresse du lieu de rendez-vous

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /**
     * Constructeur par défaut
     */
    public Seance() {
    }

    /**
     * Constructeur avec tous les paramètres
     */
    public Seance(int id_seance, long id_moniteur, long id_candidat, Integer id_vehicule,
                  LocalDateTime date_debut, TypeSeance typeseance, TypePermis typepermis,
                  float longtitude, float latitude, String adresse) {
        this.id_seance = id_seance;
        this.id_moniteur = id_moniteur;
        this.id_candidat = id_candidat;
        this.id_vehicule = id_vehicule;
        this.date_debut = date_debut;
        this.typeseance = typeseance;
        this.typepermis = typepermis;
        this.longtitude = longtitude;
        this.latitude = latitude;
        this.adresse = adresse;
    }

    // Getters et Setters

    public int getId_seance() {
        return id_seance;
    }

    public void setId_seance(int id_seance) {
        this.id_seance = id_seance;
    }

    public long getId_moniteur() {
        return id_moniteur;
    }

    public void setId_moniteur(long id_moniteur) {
        this.id_moniteur = id_moniteur;
    }

    public long getId_candidat() {
        return id_candidat;
    }

    public void setId_candidat(long id_candidat) {
        this.id_candidat = id_candidat;
    }

    public Integer getId_vehicule() {
        return id_vehicule;
    }

    public void setId_vehicule(Integer id_vehicule) {
        this.id_vehicule = id_vehicule;
    }

    public LocalDateTime getDate_debut() {
        return date_debut;
    }

    public void setDate_debut(LocalDateTime date_debut) {
        this.date_debut = date_debut;
    }

    public TypeSeance getTypeseance() {
        return typeseance;
    }

    public void setTypeseance(TypeSeance typeseance) {
        this.typeseance = typeseance;
    }

    public TypePermis getTypepermis() {
        return typepermis;
    }

    public void setTypepermis(TypePermis typepermis) {
        this.typepermis = typepermis;
    }

    public float getLongtitude() {
        return longtitude;
    }

    public void setLongtitude(float longtitude) {
        this.longtitude = longtitude;
    }

    public float getLatitude() {
        return latitude;
    }

    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    /**
     * Calcule la date de fin de la séance (1 heure après le début)
     * @return La date de fin de la séance
     */
    public LocalDateTime getDateFin() {
        return date_debut != null ? date_debut.plusHours(1) : null;
    }

    /**
     * Retourne une représentation formatée de la date et heure de début
     * @return La date et heure formatées
     */
    public String getFormattedDateTime() {
        return date_debut != null ? date_debut.format(DATE_FORMATTER) : "";
    }

    /**
     * Vérifie si la séance est une séance de code
     * @return true si c'est une séance de code, false sinon
     */
    public boolean isCodeSeance() {
        return typeseance == TypeSeance.Code;
    }

    /**
     * Vérifie si la séance est une séance de conduite
     * @return true si c'est une séance de conduite, false sinon
     */
    public boolean isConduiteSeance() {
        return typeseance == TypeSeance.Conduite;
    }

    /**
     * Vérifie si la séance est dans le futur
     * @return true si la séance est dans le futur, false sinon
     */
    public boolean isFutureSeance() {
        return date_debut != null && date_debut.isAfter(LocalDateTime.now());
    }

    /**
     * Vérifie si la séance est en cours
     * @return true si la séance est en cours, false sinon
     */
    public boolean isOngoingSeance() {
        if (date_debut == null) return false;
        LocalDateTime now = LocalDateTime.now();
        return now.isAfter(date_debut) && now.isBefore(getDateFin());
    }

    /**
     * Vérifie si la séance est terminée
     * @return true si la séance est terminée, false sinon
     */
    public boolean isCompletedSeance() {
        return date_debut != null && LocalDateTime.now().isAfter(getDateFin());
    }

    @Override
    public String toString() {
        return "Seance{" +
                "id_seance=" + id_seance +
                ", id_moniteur=" + id_moniteur +
                ", id_candidat=" + id_candidat +
                ", id_vehicule=" + id_vehicule +
                ", date_debut=" + getFormattedDateTime() +
                ", typeseance=" + typeseance +
                ", typepermis=" + typepermis +
                ", longtitude=" + longtitude +
                ", latitude=" + latitude +
                ", adresse='" + adresse + '\'' +
                '}';
    }
}
