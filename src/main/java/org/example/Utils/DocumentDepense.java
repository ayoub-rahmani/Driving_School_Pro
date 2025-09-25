package org.example.Utils;

import org.example.Entities.Depense;
import org.example.Entities.Moniteur;
import org.example.Entities.Vehicule;
import org.example.Entities.Reparation;

import java.time.LocalDate;

public class DocumentDepense {
    private String autoEcoleNom = "Auto-École Sécurité Routière";
    private String autoEcoleAdresse = "123 Avenue de la République, Bizerte";
    private String autoEcoleTelephone = "+216 12 345 678";

    private String documentType = "Fiche de Dépense";
    private LocalDate generationDate = LocalDate.now();

    private Depense depense;
    private Moniteur moniteur;
    private Vehicule vehicule;
    private Reparation reparation;

    // Constructor
    public DocumentDepense(Depense depense) {
        this.depense = depense;
    }

    // Getters
    public String getAutoEcoleNom() {
        return autoEcoleNom;
    }

    public String getAutoEcoleAdresse() {
        return autoEcoleAdresse;
    }

    public String getAutoEcoleTelephone() {
        return autoEcoleTelephone;
    }

    public String getDocumentType() {
        return documentType;
    }

    public LocalDate getGenerationDate() {
        return generationDate;
    }

    public Depense getDepense() {
        return depense;
    }

    public Moniteur getMoniteur() {
        return moniteur;
    }

    public Vehicule getVehicule() {
        return vehicule;
    }

    public Reparation getReparation() {
        return reparation;
    }

    // Setters for related entities
    public void setMoniteur(Moniteur moniteur) {
        this.moniteur = moniteur;
    }

    public void setVehicule(Vehicule vehicule) {
        this.vehicule = vehicule;
    }

    public void setReparation(Reparation reparation) {
        this.reparation = reparation;
    }

    // Setters for customization
    public void setAutoEcoleNom(String autoEcoleNom) {
        this.autoEcoleNom = autoEcoleNom;
    }

    public void setAutoEcoleAdresse(String autoEcoleAdresse) {
        this.autoEcoleAdresse = autoEcoleAdresse;
    }

    public void setAutoEcoleTelephone(String autoEcoleTelephone) {
        this.autoEcoleTelephone = autoEcoleTelephone;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }
}

