package phong.demo.DTO;

import java.time.LocalTime;

public class TimeFrame_DTO {

    LocalTime  AvailableStart;

    LocalTime AvailableEnd;

    public TimeFrame_DTO(LocalTime availableStart, LocalTime availableEnd) {
        AvailableStart = availableStart;
        AvailableEnd = availableEnd;
    }

    public LocalTime getAvailableStart() {
        return AvailableStart;
    }

    public void setAvailableStart(LocalTime availableStart) {
        AvailableStart = availableStart;
    }

    public LocalTime getAvailableEnd() {
        return AvailableEnd;
    }

    public void setAvailableEnd(LocalTime availableEnd) {
        AvailableEnd = availableEnd;
    }
}
