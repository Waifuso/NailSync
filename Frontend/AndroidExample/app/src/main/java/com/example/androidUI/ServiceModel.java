package com.example.androidUI;

/**
 * Model class representing a service that can be selected by users.
 */
public class ServiceModel {
    private int id;
    private String title;
    private String shortDescription;
    private String longDescription;
    private int iconResourceId;
    private boolean isSelected;
    private boolean isExpanded;

    /**
     * Constructor for ServiceModel.
     *
     * @param id The service ID.
     * @param title The service title.
     * @param shortDescription A short description for the service.
     * @param longDescription A more detailed description.
     * @param iconResourceId The resource ID for the service icon.
     */
    public ServiceModel(int id, String title, String shortDescription, String longDescription, int iconResourceId) {
        this.id = id;
        this.title = title;
        this.shortDescription = shortDescription;
        this.longDescription = longDescription;
        this.iconResourceId = iconResourceId;
        this.isSelected = false;
        this.isExpanded = false;
    }

    // Getters and Setters

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public String getLongDescription() {
        return longDescription;
    }

    public int getIconResourceId() {
        return iconResourceId;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public boolean isExpanded() {
        return isExpanded;
    }

    public void setExpanded(boolean expanded) {
        isExpanded = expanded;
    }

    // Toggle methods for convenience

    public void toggleSelected() {
        isSelected = !isSelected;
    }

    public void toggleExpanded() {
        isExpanded = !isExpanded;
    }
}