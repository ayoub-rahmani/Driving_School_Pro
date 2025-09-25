package org.example.Utils;

/**
 * Class representing a payment item for the dashboard
 */
public class PaymentItem {
    private int id;
    private String candidat;
    private double montant;
    private String date;
    private String methode;
    private String statut;

    public PaymentItem(int id, String candidat, double montant, String date, String methode, String statut) {
        this.id = id;
        this.candidat = candidat;
        this.montant = montant;
        this.date = date;
        this.methode = methode;
        this.statut = statut;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getCandidat() {
        return candidat;
    }

    public double getMontant() {
        return montant;
    }

    public String getDate() {
        return date;
    }

    public String getMethode() {
        return methode;
    }

    public String getStatut() {
        return statut;
    }
}
