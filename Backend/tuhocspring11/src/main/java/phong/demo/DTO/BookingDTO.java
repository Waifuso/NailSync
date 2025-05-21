package phong.demo.DTO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class BookingDTO {

    private Long userId;

    private List<Long> serviceId;

    private Long employeeId;

    private LocalDate date;

    private LocalTime time;

    public BookingDTO () {

    }

    public BookingDTO (Long userId, List<Long> serviceId, Long employeeId, LocalDate date, LocalTime time) {
        this.userId = userId;
        this.serviceId = serviceId;
        this.employeeId = employeeId;
        this.date = date;
        this.time = time;

    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public List<Long> getServiceId() {
        return serviceId;
    }

    public void setServiceId(List<Long> serviceId) {
        this.serviceId = serviceId;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
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

}
