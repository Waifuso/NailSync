package phong.demo.Entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "timeFrame")
public class TimeFrame {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Ngày của ca làm việc
    @Column(nullable = false)
    private LocalDate date;

    // Giờ bắt đầu ca làm việc (do nhân viên chọn)
    @Column(nullable = false)
    private LocalTime shiftStartTime;

    // Giờ kết thúc ca làm việc (do nhân viên chọn)
    @Column(nullable = false)
    private LocalTime shiftEndTime;

    private boolean available = true;

    // Mối quan hệ nhiều-đến-một với Employee
    @ManyToOne
    @JoinColumn(name = "employee_id")
    @JsonBackReference("timeFrame-employee")
    private Employee employee;

    // Danh sách các SmallTimeframe (các khoảng nhỏ trong ca)
    @OneToMany(mappedBy = "timeFrame", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("timeFrame-smallTimeFrame")
    private List<SmallTimeframe> smallTimeframeList = new ArrayList<>();

    public TimeFrame() {
    }

    public TimeFrame(LocalDate date, LocalTime shiftStartTime, LocalTime shiftEndTime) {
        this.date = date;
        this.shiftStartTime = shiftStartTime;
        this.shiftEndTime = shiftEndTime;
    }

    // Getters & Setters
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
    public LocalTime getShiftStartTime() {
        return shiftStartTime;
    }
    public void setShiftStartTime(LocalTime shiftStartTime) {
        this.shiftStartTime = shiftStartTime;
    }
    public LocalTime getShiftEndTime() {
        return shiftEndTime;
    }
    public void setShiftEndTime(LocalTime shiftEndTime) {
        this.shiftEndTime = shiftEndTime;
    }
    public boolean isAvailable() {
        return available;
    }
    public void setAvailable(boolean available) {
        this.available = available;
    }
    public Employee getEmployee() {
        return employee;
    }
    public void setEmployee(Employee employee) {
        this.employee = employee;
    }
    public List<SmallTimeframe> getSmallTimeframeList() {
        return smallTimeframeList;
    }
    public void setSmallTimeframeList(List<SmallTimeframe> smallTimeframeList) {
        this.smallTimeframeList = smallTimeframeList;
    }

    // Phương thức tiện ích để thêm 1 SmallTimeframe
    public void addSmallTimeframe(SmallTimeframe smallTimeframe) {
        this.smallTimeframeList.add(smallTimeframe);
        smallTimeframe.setTimeFrame(this);
    }


    // Phương thức tiện ích để xóa 1 SmallTimeframe
    public void removeSmallTimeframe(SmallTimeframe smallTimeframe) {
        this.smallTimeframeList.remove(smallTimeframe);
        smallTimeframe.setTimeFrame(null);
    }
}

