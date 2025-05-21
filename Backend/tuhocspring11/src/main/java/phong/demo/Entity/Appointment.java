package phong.demo.Entity;


import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Appointments")
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JoinColumn (name = "UserId", nullable = false)
    @JsonBackReference("user-appointments")
    private User user;

    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;

    private Integer confirmationNumber;
    private String status;

    @OneToOne(mappedBy = "appointment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference("appointment-rating")
    private Rating rating;




    @OneToMany(mappedBy = "appointment", cascade = CascadeType.ALL)
    @JsonManagedReference("appointmentService-appointment")
    private List<AppointmentService> appointmentServices;

    public Appointment () {

    }

    public Appointment (User user, LocalDate date, LocalTime startTime, LocalTime endTime) {
        this.user = user;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.confirmationNumber = 0;

    }



    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public Integer getConfirmationNumber() {
        return confirmationNumber;
    }

    public void setConfirmationNumber(Integer confirmationNumber) {
        this.confirmationNumber = confirmationNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<AppointmentService> getAppointmentServices() {
        return appointmentServices;
    }

    public void setAppointmentServices(List<AppointmentService> appointmentServices) {
        this.appointmentServices = appointmentServices;
    }




    @Override
    public String toString() {
        return "Appointment{" +
                "date=" + date +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                '}';
    }
}
