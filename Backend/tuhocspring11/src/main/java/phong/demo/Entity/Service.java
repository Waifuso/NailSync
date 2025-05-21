package phong.demo.Entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Service")
public class Service {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String serviceName;

    //CuaNu
    @OneToMany (mappedBy = "service", cascade = CascadeType.ALL)
    @JsonManagedReference("service-appointmentService")
    private List<AppointmentService> appointmentServices;

    @OneToMany(mappedBy = "service", cascade = CascadeType.ALL)
    @JsonManagedReference("service-paymentService")
    private List<PaymentService> paymentServices;


    private Integer price;

    private int duration = 0;


    @OneToMany(mappedBy = "service", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("service-Handler")
    private List<Handler> Service_Handler = new ArrayList<>();

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getService_name() {
        return serviceName;
    }

    public void setService_name(String service_name) {
        this.serviceName = service_name;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public List<Handler> getService_Handler() {
        return Service_Handler;
    }

    public void setService_Handler(List<Handler> service_Handler) {
        Service_Handler = service_Handler;
    }

    public List<AppointmentService> getAppointmentServices() {
        return appointmentServices;
    }

    public void setAppointmentServices(List<AppointmentService> appointmentServices) {
        this.appointmentServices = appointmentServices;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }


}


