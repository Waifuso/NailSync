package phong.demo.Entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "employee_finance")
public class EmployeeDailyFinance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate date;

    // the amount of the income + tip
    private double Totalincome;

    private double income;

    private double tip;


    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    @JsonBackReference("employee-EmployeeDailyFinance")
    private Employee employee;

    public EmployeeDailyFinance() {}

    public EmployeeDailyFinance(LocalDate date, double totalincome, double income, double tip, Employee employee) {
        this.date = date;
        Totalincome = totalincome;
        this.income = income;
        this.tip = tip;
        this.employee = employee;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public double getTotalincome() {
        return Totalincome;
    }

    public void setTotalincome(double totalincome) {
        Totalincome = totalincome;
    }

    public double getIncome() {
        return income;
    }

    public void setIncome(double income) {
        this.income = income;
    }

    public double getTip() {
        return tip;
    }

    public void setTip(double tip) {
        this.tip = tip;
    }
}
