package phong.demo.Entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.persistence.Id;

@Entity
@Table (name = "AppointmentService")
public class AppointmentService {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn
    @JsonBackReference("appointmentService-appointment")
    private Appointment appointment;

    @ManyToOne
    @JoinColumn
    @JsonBackReference("service-appointmentService")
    private Service service;

    @ManyToOne
    @JoinColumn
    @JsonBackReference("employee-appointmentService")
    private Employee employee;

    public AppointmentService () {

    }

    public AppointmentService (Appointment appointment, Service service, Employee employee) {
        this.appointment = appointment;
        this.service = service;
        this.employee = employee;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Appointment getAppointment() {
        return appointment;
    }

    public void setAppointment(Appointment appointment) {
        this.appointment = appointment;
    }

    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }


}

