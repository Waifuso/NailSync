package phong.demo.DTO;

public class Service_DTO {

    private String service_name;

    private Integer price;

    private int duration;

    public Service_DTO(String service_name, Integer price, int duration) {
        this.service_name = service_name;
        this.price = price;
        this.duration = duration;
    }

    public String getService_name() {
        return service_name;
    }

    public void setService_name(String service_name) {
        this.service_name = service_name;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    @Override
    public String toString() {
        return "Service_DTO{" +
                "service_name='" + service_name + '\'' +
                ", price=" + price +
                ", duration=" + duration +
                '}';
    }
}
