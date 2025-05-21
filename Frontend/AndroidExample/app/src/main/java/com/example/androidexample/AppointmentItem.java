package com.example.androidexample;

public class AppointmentItem {
    private String clientName;
    private String serviceName;
    private String date;
    private String appointmentTime;

    public AppointmentItem(String clientName, String serviceName, String date, String appointmentTime) {
        this.clientName = clientName;
        this.serviceName = serviceName;
        this.date = date;
        this.appointmentTime = appointmentTime;
    }

    public String getClientName() { return clientName; }
    public String getServiceName() { return serviceName; }
    public String getDate() { return date; }
    public String getAppointmentTime() { return appointmentTime; }
}
