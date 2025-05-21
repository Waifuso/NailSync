package phong.demo.Entity;


import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Entity
@Table(name = "payment")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn (name = "UserId", nullable = false)
    @JsonBackReference("user-payment")
    private User user;

    @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL)
    @JsonManagedReference("paymentService-payment")
    private List<PaymentService> paymentServices;

    private double amount;

    private LocalDate date;

    private LocalTime time;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod; // ENUM: CASH, CREDIT_CARD, etc.

    @ManyToOne
    @JoinColumn (name = "EmployeeId", nullable = false)
    @JsonBackReference("employee-payment")
    private Employee employee;

    private double discount;

    private double tipping;

    public Payment() {

    }


    public Payment(User user, Service service, double amount, LocalDate date, LocalTime time, PaymentMethod paymentMethod, Employee employee, double discount, double tipping) {
        this.user = user;
        this.amount = amount;
        this.date = date;
        this.time = time;
        this.paymentMethod = paymentMethod;
        this.employee = employee;
        this.discount = discount;
        this.tipping = tipping;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }


    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getTime() {
        return time;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public List<PaymentService> getPaymentServices() {
        return paymentServices;
    }

    public void setPaymentServices(List<PaymentService> paymentServices) {
        this.paymentServices = paymentServices;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
    }

    public double getTipping() {
        return tipping;
    }

    public void setTipping(double tipping) {
        this.tipping = tipping;
    }
}
