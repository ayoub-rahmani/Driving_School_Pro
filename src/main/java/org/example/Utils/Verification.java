package org.example.Utils;

import javafx.scene.control.*;
import java.time.LocalDate;
import java.time.Period;
import java.util.regex.Pattern;

/**
 * Utility class for input validation
 */
public class Verification {

    // Regular expressions for validation
    private static final String NAME_REGEX = "^[A-Za-zÀ-ÿ\\s'-]+$";
    private static final String PHONE_REGEX = "^[1-9]\\d{7}$";
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    private static final String CIN_REGEX = "^[01]\\d{7}$";
    private static final String NUMERIC_REGEX = "^\\d+(\\.\\d+)?$";

    /**
     * Validates a name field
     *
     * @param field The TextField to validate
     * @param errorLabel The Label to display error messages
     * @return true if valid, false otherwise
     */
    public static boolean validateName(TextField field, Label errorLabel) {
        String value = field.getText().trim();

        if (value.isEmpty()) {
            setErrorMessage(field, errorLabel, "Ce champ ne peut pas être vide");
            return false;
        }

        if (!Pattern.matches(NAME_REGEX, value)) {
            setErrorMessage(field, errorLabel, "Format invalide. Utilisez uniquement des lettres, espaces, apostrophes et tirets");
            return false;
        }

        clearErrorMessage(field, errorLabel);
        return true;
    }

    /**
     * Validates a phone number field
     *
     * @param field The TextField to validate
     * @param errorLabel The Label to display error messages
     * @return true if valid, false otherwise
     */
    public static boolean validatePhone(TextField field, Label errorLabel) {
        String value = field.getText().trim();

        if (value.isEmpty()) {
            setErrorMessage(field, errorLabel, "Ce champ ne peut pas être vide");
            return false;
        }

        if (!Pattern.matches(PHONE_REGEX, value)) {
            setErrorMessage(field, errorLabel, "Le numéro de téléphone doit contenir 8 chiffres");
            return false;
        }

        clearErrorMessage(field, errorLabel);
        return true;
    }

    /**
     * Validates an email field
     *
     * @param field The TextField to validate
     * @param errorLabel The Label to display error messages
     * @return true if valid, false otherwise
     */
    public static boolean validateEmail(TextField field, Label errorLabel) {
        String value = field.getText().trim();

        if (value.isEmpty()) {
            setErrorMessage(field, errorLabel, "Ce champ ne peut pas être vide");
            return false;
        }

        if (!Pattern.matches(EMAIL_REGEX, value)) {
            setErrorMessage(field, errorLabel, "Format d'email invalide");
            return false;
        }

        if (value.contains("..")) {
            setErrorMessage(field, errorLabel, "L'email ne peut pas contenir des points consécutifs");
            return false;
        }

        if (value.startsWith(".") || value.endsWith(".")) {
            setErrorMessage(field, errorLabel, "L'email ne peut pas commencer ou finir par un point");
            return false;
        }

        clearErrorMessage(field, errorLabel);
        return true;
    }

    /**
     * Validates a CIN field
     *
     * @param field The TextField to validate
     * @param errorLabel The Label to display error messages
     * @return true if valid, false otherwise
     */
    public static boolean validateCIN(TextField field, Label errorLabel) {
        String value = field.getText().trim();

        if (value.isEmpty()) {
            setErrorMessage(field, errorLabel, "Ce champ ne peut pas être vide");
            return false;
        }

        if (!Pattern.matches(CIN_REGEX, value)) {
            setErrorMessage(field, errorLabel, "Le CIN doit contenir 8 chiffres et doit commencer par 0 ou 1");
            return false;
        }

        clearErrorMessage(field, errorLabel);
        return true;
    }

    /**
     * Validates a numeric field (integer or decimal)
     *
     * @param field The TextField to validate
     * @param errorLabel The Label to display error messages
     * @param minValue The minimum allowed value
     * @return true if valid, false otherwise
     */
    public static boolean validateNumeric(TextField field, Label errorLabel, double minValue) {
        String value = field.getText().trim();

        if (value.isEmpty()) {
            setErrorMessage(field, errorLabel, "Ce champ ne peut pas être vide");
            return false;
        }

        if (!Pattern.matches(NUMERIC_REGEX, value)) {
            setErrorMessage(field, errorLabel, "Veuillez entrer un nombre valide");
            return false;
        }

        double numericValue;
        try {
            numericValue = Double.parseDouble(value);
        } catch (NumberFormatException e) {
            setErrorMessage(field, errorLabel, "Format de nombre invalide");
            return false;
        }

        if (numericValue < minValue) {
            setErrorMessage(field, errorLabel, "La valeur doit être supérieure à " + minValue);
            return false;
        }

        clearErrorMessage(field, errorLabel);
        return true;
    }

    /**
     * Validates a required field
     *
     * @param field The TextField to validate
     * @param errorLabel The Label to display error messages
     * @return true if valid, false otherwise
     */
    public static boolean validateRequired(TextField field, Label errorLabel) {
        String value = field.getText().trim();

        if (value.isEmpty()) {
            setErrorMessage(field, errorLabel, "Ce champ ne peut pas être vide");
            return false;
        }

        clearErrorMessage(field, errorLabel);
        return true;
    }
    public static boolean validateRequired1(TextArea area, Label errorLabel) {
        String value = area.getText().trim();

        if (value.isEmpty()) {
            setErrorMessage(area, errorLabel, "Ce champ ne peut pas être vide");
            return false;
        }

        clearErrorMessage(area, errorLabel);
        return true;
    }

    /**
     * Validates a birth date (cannot be in the future and must be at least 16 years old)
     *
     * @param datePicker The DatePicker to validate
     * @param errorLabel The Label to display error messages
     * @return true if valid, false otherwise
     */
    public static boolean validateBirthDate(DatePicker datePicker, Label errorLabel) {
        LocalDate date = datePicker.getValue();

        if (date == null) {
            setErrorMessage(datePicker, errorLabel, "Veuillez sélectionner une date");
            return false;
        }

        if (date.isAfter(LocalDate.now())) {
            setErrorMessage(datePicker, errorLabel, "La date de naissance ne peut pas être dans le futur");
            return false;
        }

        // Check if the person is at least 16 years old
        LocalDate today = LocalDate.now();
        Period period = Period.between(date, today);
        int age = period.getYears();

        if (age < 18) {
            setErrorMessage(datePicker, errorLabel, "Le candidat doit avoir au moins 18 ans");
            return false;
        }

        clearErrorMessage(datePicker, errorLabel);
        return true;
    }

    public static boolean validateDateEmbuche(DatePicker datePicker, Label errorLabel) {
        LocalDate date = datePicker.getValue();

        if (date == null) {
            setErrorMessage(datePicker, errorLabel, "Veuillez sélectionner une date");
            return false;
        }

        if (date.isAfter(LocalDate.now())) {
            setErrorMessage(datePicker, errorLabel, "La date d'embuche ne peut pas être dans le futur");
            return false;
        }

        clearErrorMessage(datePicker, errorLabel);
        return true;
    }
    public static boolean validateDateInscription(DatePicker datePicker, Label errorLabel) {
        LocalDate date = datePicker.getValue();

        if (date == null) {
            setErrorMessage(datePicker, errorLabel, "Veuillez sélectionner une date");
            return false;
        }

        if (date.isAfter(LocalDate.now())) {
            setErrorMessage(datePicker, errorLabel, "La date d'inscription ne peut pas être dans le futur");
            return false;
        }

        clearErrorMessage(datePicker, errorLabel);
        return true;
    }

    /**
     * Validates an appointment date (cannot be in the past)
     *
     * @param datePicker The DatePicker to validate
     * @param errorLabel The Label to display error messages
     * @return true if valid, false otherwise
     */
    public static boolean validateAppointmentDate(DatePicker datePicker, Label errorLabel) {
        LocalDate date = datePicker.getValue();

        if (date == null) {
            setErrorMessage(datePicker, errorLabel, "Veuillez sélectionner une date");
            return false;
        }

        if (date.isBefore(LocalDate.now())) {
            setErrorMessage(datePicker, errorLabel, "La date d'examen ne peut pas être dans le passé");
            return false;
        }

        clearErrorMessage(datePicker, errorLabel);
        return true;
    }

    /**
     * Validates a password field
     *
     * @param field The PasswordField to validate
     * @param errorLabel The Label to display error messages
     * @param minLength The minimum password length
     * @return true if valid, false otherwise
     */
    public static boolean validatePassword(PasswordField field, Label errorLabel, int minLength) {
        String value = field.getText();

        if (value.isEmpty()) {
            setErrorMessage(field, errorLabel, "Ce champ ne peut pas être vide");
            return false;
        }

        if (value.length() < minLength) {
            setErrorMessage(field, errorLabel, "Le mot de passe doit contenir au moins " + minLength + " caractères");
            return false;
        }

        clearErrorMessage(field, errorLabel);
        return true;
    }

    /**
     * Validates that two password fields match
     *
     * @param field1 The first PasswordField
     * @param field2 The second PasswordField
     * @param errorLabel The Label to display error messages
     * @return true if valid, false otherwise
     */
    public static boolean validatePasswordsMatch(PasswordField field1, PasswordField field2, Label errorLabel) {
        String password1 = field1.getText();
        String password2 = field2.getText();

        if (!password1.equals(password2)) {
            setErrorMessage(field2, errorLabel, "Les mots de passe ne correspondent pas");
            return false;
        }

        clearErrorMessage(field2, errorLabel);
        return true;
    }

    /**
     * Validates that at least one checkbox in a group is selected
     *
     * @param errorLabel The Label to display error messages
     * @param checkboxes The CheckBox array to validate
     * @return true if valid, false otherwise
     */
    public static boolean validateCheckboxGroup(Label errorLabel, CheckBox... checkboxes) {
        for (CheckBox checkbox : checkboxes) {
            if (checkbox.isSelected()) {
                if (errorLabel != null) {
                    errorLabel.setText("");
                }
                return true;
            }
        }

        if (errorLabel != null) {
            errorLabel.setText("Veuillez sélectionner au moins une option");
        }
        return false;
    }

    /**
     * Validates a Tunisian license plate format
     *
     * @param field The TextField to validate
     * @param errorLabel The Label to display error messages
     * @return true if valid, false otherwise
     */
    public static boolean validateTunisianLicensePlate(TextField field, Label errorLabel) {
        String value = field.getText().trim();

        if (value.isEmpty()) {
            setErrorMessage(field, errorLabel, "L'immatriculation ne peut pas être vide");
            return false;
        }

        // Tunisian license plate format: 123 TUN 4567 or variations
        // We'll be flexible with spaces and allow different formats
        String cleanValue = value.replaceAll("\\s+", "").toUpperCase(); // Remove all spaces and convert to uppercase

        // Check for the standard format: digits + letters + digits
        // Format 1: 1-4 digits + 2-3 letters + 1-4 digits (most common)
        boolean formatOne = cleanValue.matches("\\d{1,4}[A-Z]{2,3}\\d{1,4}");

        // Format 2: RS + digits (for government vehicles)
        boolean formatTwo = cleanValue.matches("RS\\d{1,5}");

        // Format 3: 1-2 digits + TN (for diplomatic vehicles)
        boolean formatThree = cleanValue.matches("\\d{1,2}TN\\d{1,3}");

        if (!(formatOne || formatTwo || formatThree)) {
            setErrorMessage(field, errorLabel, "Format d'immatriculation tunisienne invalide");
            return false;
        }

        clearErrorMessage(field, errorLabel);
        return true;
    }

    /**
     * Validates a date that cannot be in the future
     *
     * @param datePicker The DatePicker to validate
     * @param errorLabel The Label to display error messages
     * @return true if valid, false otherwise
     */
    public static boolean validatePastOrPresentDate(DatePicker datePicker, Label errorLabel) {
        LocalDate date = datePicker.getValue();

        if (date == null) {
            setErrorMessage(datePicker, errorLabel, "Veuillez sélectionner une date");
            return false;
        }

        if (date.isAfter(LocalDate.now())) {
            setErrorMessage(datePicker, errorLabel, "La date ne peut pas être dans le futur");
            return false;
        }

        clearErrorMessage(datePicker, errorLabel);
        return true;
    }

    /**
     * Sets an error message on a field and label
     *
     * @param field The Control to mark as invalid
     * @param errorLabel The Label to display the error message
     * @param message The error message
     */
    private static void setErrorMessage(Control field, Label errorLabel, String message) {
        if (field != null) {
            field.setStyle("-fx-border-color: #ef4444; -fx-border-width: 1px;");
        }

        if (errorLabel != null) {
            errorLabel.setText(message);
            errorLabel.setStyle("-fx-text-fill: #ef4444;");
        }
    }

    /**
     * Clears error styling and messages
     *
     * @param field The Control to clear
     * @param errorLabel The Label to clear
     */
    private static void clearErrorMessage(Control field, Label errorLabel) {
        if (field != null) {
            field.setStyle("");
        }

        if (errorLabel != null) {
            errorLabel.setText("");
        }
    }
}

