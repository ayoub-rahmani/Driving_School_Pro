package org.example.Entities;

import java.time.LocalDate;
import java.util.List;

public class Candidat {
    private Long id;
    private String nom;
    private String prenom;
    private String cin;
    private LocalDate dateNaissance;
    private String telephone;
    private String adresse;
    private String email;
    private LocalDate dateInscription;
    private List<String> categoriesPermis; // Types de permis que le candidat souhaite obtenir
    private String cheminPhotoCIN; // Chemin vers le document scanné de la CIN
    private String cheminPhotoIdentite; // Chemin vers la photo d'identité
    private String cheminCertificatMedical; // Chemin vers le certificat médical scanné
    private String cheminFichePdf; // Chemin vers la fiche PDF générée
    private String cheminFichePng; // Chemin vers la fiche PNG générée
    private boolean actif;

    public Candidat(Long id, String nom, String prenom, String cin, LocalDate dateNaissance,
                    String telephone, String adresse, String email, LocalDate dateInscription,
                    List<String> categoriesPermis, String cheminPhotoCIN, String cheminPhotoIdentite,
                    String cheminCertificatMedical, String cheminFichePdf, String cheminFichePng, boolean actif) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.cin = cin;
        this.dateNaissance = dateNaissance;
        this.telephone = telephone;
        this.adresse = adresse;
        this.email = email;
        this.dateInscription = dateInscription;
        this.categoriesPermis = categoriesPermis;
        this.cheminPhotoCIN = cheminPhotoCIN;
        this.cheminPhotoIdentite = cheminPhotoIdentite;
        this.cheminCertificatMedical = cheminCertificatMedical;
        this.cheminFichePdf = cheminFichePdf;
        this.cheminFichePng = cheminFichePng;
        this.actif = actif;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getCin() {
        return cin;
    }

    public void setCin(String cin) {
        this.cin = cin;
    }

    public LocalDate getDateNaissance() {
        return dateNaissance;
    }

    public void setDateNaissance(LocalDate dateNaissance) {
        this.dateNaissance = dateNaissance;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDate getDateInscription() {
        return dateInscription;
    }

    public void setDateInscription(LocalDate dateInscription) {
        this.dateInscription = dateInscription;
    }

    public List<String> getCategoriesPermis() {
        return categoriesPermis;
    }

    public void setCategoriesPermis(List<String> categoriesPermis) {
        this.categoriesPermis = categoriesPermis;
    }

    public String getCheminPhotoCIN() {
        return cheminPhotoCIN;
    }

    public void setCheminPhotoCIN(String cheminPhotoCIN) {
        this.cheminPhotoCIN = cheminPhotoCIN;
    }

    public String getCheminPhotoIdentite() {
        return cheminPhotoIdentite;
    }

    public void setCheminPhotoIdentite(String cheminPhotoIdentite) {
        this.cheminPhotoIdentite = cheminPhotoIdentite;
    }

    public String getCheminCertificatMedical() {
        return cheminCertificatMedical;
    }

    public void setCheminCertificatMedical(String cheminCertificatMedical) {
        this.cheminCertificatMedical = cheminCertificatMedical;
    }

    public boolean isActif() {
        return actif;
    }

    public void setActif(boolean actif) {
        this.actif = actif;
    }

    public String getCheminFichePdf() {
        return cheminFichePdf;
    }

    public void setCheminFichePdf(String cheminFichePdf) {
        this.cheminFichePdf = cheminFichePdf;
    }
    public String getCheminFichePng() {
        return cheminFichePng;
    }

    public void setCheminFichePng(String cheminFichePng) {
        this.cheminFichePng = cheminFichePng;
    }
    @Override
    public String toString() {
        return nom + " " + prenom;
    }
}