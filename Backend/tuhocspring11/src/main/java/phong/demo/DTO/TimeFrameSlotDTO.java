package phong.demo.DTO;

import java.util.List;

public class TimeFrameSlotDTO {

    private List<TimeFrame_DTO> slots;

    public TimeFrameSlotDTO() {
    }

    public TimeFrameSlotDTO(List<TimeFrame_DTO> slots) {
        this.slots = slots;
    }

    public List<TimeFrame_DTO> getSlots() {
        return slots;
    }

    public void setSlots(List<TimeFrame_DTO> slots) {
        this.slots = slots;
    }
}
