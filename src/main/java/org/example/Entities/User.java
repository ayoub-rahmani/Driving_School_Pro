package org.example.Entities;

import java.sql.Timestamp;

public class User {
    private int id;
    private String username;
    private String password;
    private String email;
    private String phoneNumber;
    private String role;  // "ADMIN" or "SECRETARY"
    private String fullName;
    private Timestamp createdAt;
    private Timestamp lastLogin;
    private boolean active;

    // Constructors
    public User() {
    }

    public User(int id, String username, String password, String email, String phoneNumber,
                String role, String fullName, Timestamp createdAt, Timestamp lastLogin, boolean active) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.role = role;
        this.fullName = fullName;
        this.createdAt = createdAt;
        this.lastLogin = lastLogin;
        this.active = active;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(Timestamp lastLogin) {
        this.lastLogin = lastLogin;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    // Permission helper methods
    public boolean isAdmin() {
        return "Administrateur".equalsIgnoreCase(this.role);
    }

    public boolean isSecretary() {
        return "Secrétaire".equalsIgnoreCase(this.role);
    }

    // Check if user has access to specific features
    public boolean canAccessStatistics() {
        return isAdmin(); // Only admins can access statistics
    }

    public boolean canAccessPayments() {
        return isAdmin(); // Only admins can access payments
    }

    public boolean canDeleteCandidats() {
        return isAdmin(); // Only admins can delete candidates
    }

    public boolean canManageUsers() {
        return isAdmin(); // Only admins can manage users
    }

    public boolean canAccessLogs() {
        return isAdmin(); // Only admins can access logs
    }
}