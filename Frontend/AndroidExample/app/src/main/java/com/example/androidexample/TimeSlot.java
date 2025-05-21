package com.example.androidexample;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TimeSlot {
    private int id;
    private String startTime;
    private String endTime;
    private boolean available;

    public TimeSlot(int id, String startTime, String endTime, boolean available) {
        this.id = id;
        this.startTime = startTime;
        this.endTime = endTime;
        this.available = available;
    }

    public int getId() {
        return id;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public boolean isAvailable() {
        return available;
    }

    // Format the time for display (convert from 24h format to 12h format)
    public String getFormattedTimeRange() {
        return formatTime(startTime) + " - " + formatTime(endTime);
    }

    private String formatTime(String time24) {
        try {
            SimpleDateFormat sdf24 = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
            SimpleDateFormat sdf12 = new SimpleDateFormat("h:mm a", Locale.getDefault());
            Date date = sdf24.parse(time24);
            return sdf12.format(date);
        } catch (ParseException e) {
            return time24; // Return original if parsing fails
        }
    }
}

