package phong.demo.DTO;

import phong.demo.Entity.Rating;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class RatingDTO {

    private Rating rating;

    private List<String> serviceName = new ArrayList<>();

    private String employee;

    private LocalDate date;

    public RatingDTO() {

    }

    public RatingDTO(Rating rating, List<String> serviceName, String employee, LocalDate date) {
        this.rating = rating;
        this.serviceName = serviceName;
        this.employee = employee;
        this.date = date;
    }

    public Rating getRating() {
        return rating;
    }

    public void setRating(Rating rating) {
        this.rating = rating;
    }

    public List<String> getServiceName() {
        return serviceName;
    }

    public void setServiceName(List<String> serviceName) {
        this.serviceName = serviceName;
    }

    public String getEmployee() {
        return employee;
    }

    public void setEmployee(String employee) {
        this.employee = employee;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }
}
