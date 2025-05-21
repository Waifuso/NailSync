package phong.demo.Entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import java.time.LocalTime;

@Entity
@Table(name = "small_timeframe")
public class SmallTimeframe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Giờ bắt đầu khoảng nhỏ

    private LocalTime subStartTime;

    // Giờ kết thúc khoảng nhỏ

    private LocalTime subEndTime;

    private boolean available = true;

    // Mối quan hệ nhiều-đến-một với TimeFrame cha
    @ManyToOne
    @JoinColumn(name = "time_frame_id")
    @JsonBackReference("timeFrame-smallTimeFrame")
    private TimeFrame timeFrame;

    public SmallTimeframe() {
    }

    public SmallTimeframe(LocalTime subStartTime, LocalTime subEndTime) {
        this.subStartTime = subStartTime;
        this.subEndTime = subEndTime;
    }

    // Getters & Setters
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public LocalTime getSubStartTime() {
        return subStartTime;
    }
    public void setSubStartTime(LocalTime subStartTime) {
        this.subStartTime = subStartTime;
    }
    public LocalTime getSubEndTime() {
        return subEndTime;
    }
    public void setSubEndTime(LocalTime subEndTime) {
        this.subEndTime = subEndTime;
    }
    public boolean isAvailable() {
        return available;
    }
    public void setAvailable(boolean available) {
        this.available = available;
    }
    public TimeFrame getTimeFrame() {
        return timeFrame;
    }
    public void setTimeFrame(TimeFrame timeFrame) {
        this.timeFrame = timeFrame;
    }

    @Override
    public String toString() {
        return "SmallTimeframe{" +
                "id=" + id +
                ", subStartTime=" + subStartTime +
                ", subEndTime=" + subEndTime +
                ", available=" + available +
                ", timeFrame=" + timeFrame +
                '}';
    }
}
