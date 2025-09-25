package org.example.Utils;

import org.example.Entities.Candidat;

import java.time.LocalDate;
import java.util.List;

public class Document {

    private String autoEcoleNom = "Auto-École Sécurité Routière"; // Example data
    private String autoEcoleAdresse = "123 Avenue de la République, Bizerte";
    private String autoEcoleTelephone = "+216 12 345 678";

    private String documentType; // e.g., "Fiche Candidat", "Liste des Candidats"
    private LocalDate generationDate = LocalDate.now();

    private Candidat candidat; // For single-candidate fiche
    private List<Candidat> candidats; // For lists of candidates

    // Constructors (one for single candidate, one for lists)
    public Document(Candidat candidat,String documentType) {
        this.candidat = candidat;
        this.documentType=documentType;
    }

    public Document(List<Candidat> candidats,String documentType) {
        this.candidats = candidats;
        this.documentType=documentType;
    }


    // Getters for all fields
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

    public Candidat getCandidat() {
        return candidat;
    }

    public List<Candidat> getCandidats() {
        return candidats;
    }

}
