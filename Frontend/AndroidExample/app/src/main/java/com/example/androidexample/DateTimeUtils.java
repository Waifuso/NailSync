package com.example.androidexample;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class DateTimeUtils {

    /**
     * Extracts the day from a date string in format yyyy-MM-dd
     * @param dateStr Date string in format yyyy-MM-dd
     * @return Day as string
     */
    public static String extractDay(String dateStr) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat dayFormat = new SimpleDateFormat("dd", Locale.getDefault());
            return dayFormat.format(inputFormat.parse(dateStr));
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Extracts the month from a date string in format yyyy-MM-dd
     * @param dateStr Date string in format yyyy-MM-dd
     * @return Month abbreviation in uppercase (e.g., JAN, FEB)
     */
    public static String extractMonth(String dateStr) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat monthFormat = new SimpleDateFormat("MMM", Locale.getDefault());
            return monthFormat.format(inputFormat.parse(dateStr)).toUpperCase();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Calculates a time range based on a start time and duration
     * @param startTime Time in format HH:mm
     * @param durationMinutes Duration in minutes
     * @return Formatted time range (e.g., "14:30 - 15:30")
     */
    public static String calculateTimeRange(String startTime, int durationMinutes) {
        if (startTime == null || startTime.isEmpty()) {
            return "";
        }

        try {
            // Parse the start time
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            java.util.Date parsedStartTime = timeFormat.parse(startTime);

            // Calculate end time based on duration
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(parsedStartTime);
            calendar.add(Calendar.MINUTE, durationMinutes);

            // Format the result as "start - end" (e.g., "14:30 - 15:30")
            return timeFormat.format(parsedStartTime) + " - " + timeFormat.format(calendar.getTime());
        } catch (Exception e) {
            return startTime; // Fall back to just showing the start time
        }
    }

    /**
     * Formats a date string from yyyy-MM-dd to a more readable format (e.g., "October 15, 2025")
     * @param dateStr Date string in format yyyy-MM-dd
     * @return Formatted date string
     */
    public static String formatDate(String dateStr) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
            return outputFormat.format(inputFormat.parse(dateStr));
        } catch (Exception e) {
            return dateStr;
        }
    }
}