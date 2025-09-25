package org.example.Entities;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Moniteur {
    private Long id;
    private String nom;
    private String prenom;
    private String cin;
    private LocalDate dateNaissance;
    private String telephone;
    private LocalDate dateEmbauche;
    private LocalDate dateFinContrat;
    private String numPermis;
    private List<String> categoriesPermis; // Types de permis que le moniteur peut enseigner
    private boolean disponible;
    private String motif;
    private double salaire;
    private double experience;
    private String diplomes;
    private String notes;

    public Moniteur(Long id, String nom, String prenom, String cin, LocalDate dateNaissance, String telephone, LocalDate dateEmbauche,LocalDate dateFinContrat, String numPermis, List<String> categoriesPermis, boolean disponible,  String motif,double salaire, Double experience, String diplomes, String notes) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.cin = cin;
        this.dateNaissance = dateNaissance;
        this.telephone = telephone;
        this.dateEmbauche = dateEmbauche;
        this.dateFinContrat = dateFinContrat;
        this.numPermis = numPermis;
        this.categoriesPermis = categoriesPermis;
        this.disponible = disponible;
        this.motif = motif;
        this.salaire = salaire;
        this.experience = experience;
        this.diplomes = diplomes;
        this.notes = notes;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getDiplomes() {
        return diplomes;
    }

    public void setDiplomes(String diplomes) {
        this.diplomes = diplomes;
    }

    public double getExperience() {
        return experience;
    }

    public void setExperience(double experience) {
        this.experience = experience;
    }

    public double getSalaire() {
        return salaire;
    }

    public void setSalaire(double salaire) {
        this.salaire = salaire;
    }

    public String getMotif() {
        return motif;
    }

    public void setMotif(String motif) {
        this.motif = motif;
    }

    public boolean isDisponible() {
        return disponible;
    }

    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }

    public List<String> getCategoriesPermis() {
        return categoriesPermis;
    }

    public void setCategoriesPermis(List<String> categoriesPermis) {
        this.categoriesPermis = categoriesPermis;
    }

    public String getNumPermis() {
        return numPermis;
    }

    public void setNumPermis(String numPermis) {
        this.numPermis = numPermis;
    }

    public LocalDate getDateFinContrat() {
        return dateFinContrat;
    }

    public void setDateFinContrat(LocalDate dateFinContrat) {
        this.dateFinContrat = dateFinContrat;
    }

    public LocalDate getDateEmbauche() {
        return dateEmbauche;
    }

    public void setDateEmbauche(LocalDate dateEmbauche) {
        this.dateEmbauche = dateEmbauche;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public LocalDate getDateNaissance() {
        return dateNaissance;
    }

    public void setDateNaissance(LocalDate dateNaissance) {
        this.dateNaissance = dateNaissance;
    }

    public String getCin() {
        return cin;
    }

    public void setCin(String cin) {
        this.cin = cin;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    @Override
    public String toString() {
        return  nom +" "+prenom ;
    }
}