package org.example.Utils;

/**
 * Class representing an exam item for the dashboard
 */
public class ExamItem {
    private int id;
    private String candidat;
    private String type;
    private String date;
    private String lieu;

    public ExamItem(int id, String candidat, String type, String date, String lieu) {
        this.id = id;
        this.candidat = candidat;
        this.type = type;
        this.date = date;
        this.lieu = lieu;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getCandidat() {
        return candidat;
    }

    public String getType() {
        return type;
    }

    public String getDate() {
        return date;
    }

    public String getLieu() {
        return lieu;
    }
}

