package phong.demo.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import phong.demo.DTO.Service_DTO;

import java.time.LocalDate;
import java.util.ArrayList;

import java.util.List;

@Entity
@Table(name = "employees")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private long employee_id;

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL)
    @JsonManagedReference("employee-appointmentService")
    private List<AppointmentService> appointmentServices;

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL)
    @JsonManagedReference("timeFrame-employee")
    private List<TimeFrame> timeFrame;

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL)
    @JsonManagedReference("employee-payment")
    private List<Payment> payments;


    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL)
    @JsonManagedReference("employee-EmployeeDailyFinance")
    private List<EmployeeDailyFinance> employeeDailyFinances;



    private String username;

    private String email;

    private String servicePassword;

    private boolean available;

    private LocalDate Dob;

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("handler-employee")
    private List<Handler> Employee_Handler = new ArrayList<>();

    // default constructor
    public Employee() {
    }

    public Employee(String username, String servicePassword, String email, boolean available, LocalDate dob) {

        this.username = username;

        this.servicePassword = servicePassword;

        this.email = email;

        this.available = available;

        Dob = dob;
    }

    public long getId() {
        return employee_id;
    }

    public void setId(long id) {
        this.employee_id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getServicePassword() {
        return servicePassword;
    }

    public void setServicePassword(String servicePassword) {
        this.servicePassword = servicePassword;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public LocalDate getDob() {
        return Dob;
    }

    public void setDob(LocalDate dob) {
        Dob = dob;
    }

    public long getEmployee_id() {
        return employee_id;
    }

    public void setEmployee_id(long employee_id) {
        this.employee_id = employee_id;
    }

    public List<Handler> getEmployee_Handler() {
        return Employee_Handler;
    }

    public void setEmployee_Handler(List<Handler> employee_Handler) {
        Employee_Handler = employee_Handler;
    }

    public void Add_Handler(Handler handler){

        Employee_Handler.add(handler);

        handler.setEmployee(this);
    }
    public void Remove_Handler(Handler handler){

        Employee_Handler.remove(handler);

        handler.setEmployee(null);
    }

    public void Add_Servive(Service service){

        Employee_Handler.add(new Handler(this,service));


    }

    public List<AppointmentService> getAppointmentServices() {
        return appointmentServices;
    }

    public void setAppointmentServices(List<AppointmentService> appointmentServices) {
        this.appointmentServices = appointmentServices;
    }

    public List<TimeFrame> getTimeFrame() {
        return timeFrame;
    }

    public void setTimeFrame(List<TimeFrame> timeFrame) {
        this.timeFrame = timeFrame;
    }

    public List<Payment> getPayments() {
        return payments;
    }

    public void setPayments(List<Payment> payments) {
        this.payments = payments;
    }

    public List<EmployeeDailyFinance> getEmployeeDailyFinances() {
        return employeeDailyFinances;
    }

    public void setEmployeeDailyFinances(List<EmployeeDailyFinance> employeeDailyFinances) {
        this.employeeDailyFinances = employeeDailyFinances;
    }
}

