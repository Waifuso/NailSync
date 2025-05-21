
package phong.demo.Entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

@Entity
@Table(name = "handler")
public class Handler {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // Surrogate primary key

    // Many-to-One relationship with Employee (customer)
    @ManyToOne
    @JoinColumn
    @JsonBackReference("handler-employee")
    private Employee employee;


    // Many-to-One relationship with Service without an explicit JoinColumn annotation
    @ManyToOne
    @JoinColumn
    @JsonBackReference("service-Handler")
    private Service service;



    // Default constructor
    public Handler() {}

    // Convenience constructor
    public Handler(Employee employee, Service service) {
        this.employee = employee;
        this.service = service;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Employee getEmployee() {
        return employee;
    }
    public void setEmployee(Employee employee) {
        this.employee = employee;
    }
    public Service getService() {
        return service;
    }
    public void setService(Service service) {
        this.service = service;
    }
}
