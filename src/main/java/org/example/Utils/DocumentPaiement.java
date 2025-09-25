package org.example.Utils;

import org.example.Entities.Paiement;
import org.example.Entities.Candidat;

import java.time.LocalDate;

public class DocumentPaiement {
    private String autoEcoleNom = "Auto-École Sécurité Routière";
    private String autoEcoleAdresse = "123 Avenue de la République, Bizerte";
    private String autoEcoleTelephone = "+216 12 345 678";

    private String documentType = "Reçu de Paiement";
    private LocalDate generationDate = LocalDate.now();

    private Paiement paiement;
    private Candidat candidat;

    // Constructor
    public DocumentPaiement(Paiement paiement, Candidat candidat) {
        this.paiement = paiement;
        this.candidat = candidat;
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

    public Paiement getPaiement() {
        return paiement;
    }

    public Candidat getCandidat() {
        return candidat;
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
