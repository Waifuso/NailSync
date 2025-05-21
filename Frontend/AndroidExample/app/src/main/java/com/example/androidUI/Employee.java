package com.example.androidUI;

/**
 * Model class representing an employee/contact for messaging
 */
public class Employee {
    private int id;
    private String name;
    private String role;
    private String avatarUrl;
    private boolean isOnline;

    /**
     * Basic constructor with required fields
     */
    public Employee(int id, String name) {
        this.id = id;
        this.name = name;
        this.role = "";
        this.avatarUrl = null;
        this.isOnline = false;
    }

    /**
     * Full constructor with all fields
     */
    public Employee(int id, String name, String role, String avatarUrl, boolean isOnline) {
        this.id = id;
        this.name = name;
        this.role = role;
        this.avatarUrl = avatarUrl;
        this.isOnline = isOnline;
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }

    /**
     * Helper method to get the first letter of the name (for avatar placeholders)
     */
    public String getInitial() {
        if (name != null && !name.isEmpty()) {
            return name.substring(0, 1).toUpperCase();
        }
        return "?";
    }

    @Override
    public String toString() {
        return "Employee{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}