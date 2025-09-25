package org.example.Utils;

/**
 * Class representing an alert item for the dashboard
 */
public class AlertItem {
    private String type; // "maintenance" or "document"
    private String vehicle;
    private String document; // Only for document type
    private String date; // For maintenance type
    private String expiry; // For document type
    private String severity; // "high" or "medium"

    // Constructor for maintenance alert
    public AlertItem(String type, String vehicle, String date, String severity) {
        this.type = type;
        this.vehicle = vehicle;
        this.date = date;
        this.severity = severity;
    }

    // Constructor for document alert
    public AlertItem(String type, String vehicle, String document, String expiry, String severity) {
        this.type = type;
        this.vehicle = vehicle;
        this.document = document;
        this.expiry = expiry;
        this.severity = severity;
    }

    // Getters
    public String getType() {
        return type;
    }

    public String getVehicle() {
        return vehicle;
    }

    public String getDocument() {
        return document;
    }

    public String getDate() {
        return date;
    }

    public String getExpiry() {
        return expiry;
    }

    public String getSeverity() {
        return severity;
    }
}
