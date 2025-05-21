package com.example.androidexample;

import java.util.List;

public class StaffSchedule {
    private String employeeName;
    private List<TimeSlot> timeSlots;

    public StaffSchedule(String employeeName, List<TimeSlot> timeSlots) {
        this.employeeName = employeeName;
        this.timeSlots = timeSlots;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public List<TimeSlot> getTimeSlots() {
        return timeSlots;
    }

    public boolean hasTimeSlots() {
        return timeSlots != null && !timeSlots.isEmpty();
    }
}
