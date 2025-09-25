package org.example.Utils;

import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import org.example.Entities.User;

public class AccessControl {

    /**
     * Checks if the current user has the required role to perform an action
     * @param requiredRole The role required ("Administrateur" or "Secrétaire")
     * @return true if the user has the required role, false otherwise
     */
    public static boolean hasRole(String requiredRole) {
        User currentUser = SessionManager.getCurrentUser();

        if (currentUser == null) {
            return false;
        }

        // Admin can do everything
        if (currentUser.isAdmin()) {
            return true;
        }

        // Check if user has the specific required role
        return currentUser.getRole().equalsIgnoreCase(requiredRole);
    }

    /**
     * Checks if the current user has the required permission and shows an error if not
     * @param permissionCheck A function that returns true if the user has permission
     * @param errorMessage Message to display if permission is denied
     * @return true if the user has permission, false otherwise
     */
    public static boolean checkPermission(boolean permissionCheck, String errorMessage) {
        if (!permissionCheck) {
            showAccessDeniedAlert(errorMessage);
            return false;
        }
        return true;
    }

    /**
     * Shows an access denied alert
     * @param message The error message to display
     */
    public static void showAccessDeniedAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Accès refusé");
        alert.setHeaderText("Permission insuffisante");
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Configures a button to be disabled if the user doesn't have the required role
     * @param button The button to configure
     * @param permissionRequired A function that returns true if the user has permission
     */
    public static void setupButtonAccess(Button button, boolean permissionRequired) {
        button.setDisable(!permissionRequired);
        button.setVisible(permissionRequired);
        button.setManaged(permissionRequired);
    }
}
