package phong.demo.DTO;

import phong.demo.Entity.Appointment;
import phong.demo.Entity.Service;

import java.util.List;
import java.util.Optional;

public class AppointmentDetailsDTO {
    private Appointment appointment;
    private List<Long> serviceId;
    private String employeeName;

    public AppointmentDetailsDTO(Appointment appointment, List<Long> serviceId, String employeeName) {
        this.appointment = appointment;
        this.serviceId = serviceId;
        this.employeeName = employeeName;
    }

    public Appointment getAppointment() {
        return appointment;
    }

    public void setAppointment(Appointment appointment) {
        this.appointment = appointment;
    }

    public List<Long> getServiceId() {
        return serviceId;
    }

    public void setServiceId(List<Long> serviceId) {
        this.serviceId = serviceId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

}
