package phong.demo.DTO;

import java.util.List;

public class ServiceIds_DTO {

    private List<Long> serviceIds;

    public ServiceIds_DTO() {
    }

    public ServiceIds_DTO(List<Long> serviceIds) {
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
