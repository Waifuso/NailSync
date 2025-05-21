package com.example.androidUI;

import androidx.annotation.DrawableRes;

/**
 * Represents a service item displayed in the RecyclerView.
 * Enhanced with additional properties for a more modern UI presentation.
 */
public class ServiceItem {
    private final String title;
    private final String description;
    private final int iconResId;
    private final String actionType;
    private final String actionData;
    private final int backgroundColor;

    /**
     * Constructor for basic service items
     */
    public ServiceItem(String title, String description, @DrawableRes int iconResId) {
        this(title, description, iconResId, "default", "", 0);
    }

    /**
     * Constructor for service items with specific action types
     */
    public ServiceItem(String title, String description, @DrawableRes int iconResId,
                       String actionType, String actionData) {
        this(title, description, iconResId, actionType, actionData, 0);
    }

    /**
     * Full constructor with all properties
     */
    public ServiceItem(String title, String description, @DrawableRes int iconResId,
                       String actionType, String actionData, int backgroundColor) {
        this.title = title;
        this.description = description;
        this.iconResId = iconResId;
        this.actionType = actionType;
        this.actionData = actionData;
        this.backgroundColor = backgroundColor > 0 ? backgroundColor : 0xFFF2EAF5; // Default light purple background
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public int getIconResId() {
        return iconResId;
    }

    public String getActionType() {
        return actionType;
    }

    public String getActionData() {
        return actionData;
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    /**
     * Checks if this service item has additional action data
     */
    public boolean hasActionData() {
        return actionData != null && !actionData.isEmpty();
    }
}