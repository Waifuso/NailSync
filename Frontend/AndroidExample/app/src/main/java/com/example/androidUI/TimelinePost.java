package com.example.androidUI;

import android.graphics.Bitmap;

/**
 * Model class for a timeline post
 */
public class TimelinePost {
    private String id;
    private String employeeName;
    private String message;  // This represents the caption in the API
    private String imageUrl;
    private String timestamp;

    /**
     * Constructor for TimelinePost
     *
     * @param id           Unique identifier for the post
     * @param employeeName Name of the employee who created the post
     * @param message      Text content/caption of the post
     * @param imageUrl     URL of the post image (can be null/empty for text-only posts)
     * @param timestamp    Creation time of the post
     */
    public TimelinePost(String id, String employeeName, String message, String imageUrl, String timestamp) {
        this.id = id;
        this.employeeName = employeeName;
        this.message = message;
        this.imageUrl = imageUrl;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}