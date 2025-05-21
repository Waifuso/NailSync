package phong.demo.DTO;

import java.util.List;

public class ServiceIdsDTO {

    private List<Long> serviceIds;

    public ServiceIdsDTO() {
    }

    public ServiceIdsDTO(List<Long> serviceIds) {
        this.serviceIds = serviceIds;
    }

    // Getters and Setters
    public List<Long> getServiceIds() {
        return serviceIds;
    }
    public void setServiceIds(List<Long> serviceIds) {
        this.serviceIds = serviceIds;
    }
}

