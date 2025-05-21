package com.example.androidUI;

import android.widget.Button;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AppointmentModel implements Serializable {

    private static final long serialVersionUID = 1L; // Required for Serializable

    private long id;
    private String employeeName;
    private String date;
    private String time;
    private int duration;
    private String status;
    private List<String> serviceNames = new ArrayList<>();
    private String notes;

    private boolean showPayButton = true;

    private Button btnPay, viewDetails;

    // Default constructor
    public AppointmentModel() {
    }

    // Getters and setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<String> getServiceNames() {
        return serviceNames;
    }

    public void setServiceNames(List<String> serviceNames) {
        this.serviceNames = serviceNames;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    // Helper methods
    public String getFormattedDate() {
        try {
            SimpleDateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat targetFormat = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());

            Date dateObj = originalFormat.parse(date);
            return targetFormat.format(dateObj);
        } catch (ParseException e) {
            return date; // Return original if parsing fails
        }
    }

    public String getTimeRange() {
        try {
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            SimpleDateFormat displayFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());

            Date startTime = timeFormat.parse(time);

            // Calculate end time
            Date endTime = (Date) startTime.clone();
            endTime.setTime(endTime.getTime() + (duration * 60 * 1000));

            return displayFormat.format(startTime) + " - " + displayFormat.format(endTime);
        } catch (ParseException e) {
            return time + " (" + duration + " min)"; // Fallback
        }
    }

    public String getServicesString() {
        if (serviceNames == null || serviceNames.isEmpty()) {
            return "No services";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < serviceNames.size(); i++) {
            sb.append(serviceNames.get(i));
            if (i < serviceNames.size() - 1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    public boolean shouldShowPayButton() {
        return showPayButton;
    }

    public void setShowPayButton(boolean showPayButton) {
        this.showPayButton = showPayButton;
    }

}