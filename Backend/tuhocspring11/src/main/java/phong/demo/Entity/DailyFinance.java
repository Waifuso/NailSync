package phong.demo.Entity;

import jakarta.persistence.*;

import java.time.LocalDate;
@Entity
@Table(name = "Daily_finance")
public class DailyFinance {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate date;

    private Integer total;

    public DailyFinance(LocalDate localDate, Double totalIncome) {
    }

    public DailyFinance(LocalDate date, Integer total) {

        this.date = date;
        this.total = total;
    }

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

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

}
