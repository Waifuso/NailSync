package phong.demo.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.List;


@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String username;
    private String password;
    private String email;

    private Integer resetNums ;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonManagedReference("user-appointments")
    private List<Appointment> appointmentList;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonManagedReference("user-payment")
    private List<Payment> paymentList;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonManagedReference("user-coupon_service")
    private List<CouponService> couponServices;


    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference("user-profiles")
    private Profile profile;


    private LocalDate joinedDate;

    @Column(nullable = false)
    private Integer totalSpend = 0;



    // default constructor
    public User() {
    }

    public User(String username, String password, String email) {

        this.username = username;
        this.email = email;
        this.password = password;
        this.totalSpend = 0;

        this.resetNums = 0;

    }
    @PrePersist
    public void Oncreaete(){
        joinedDate = LocalDate.now();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getResetNums() {
        return resetNums;
    }

    public void setResetNums(Integer resetNums) {
        this.resetNums = resetNums;
    }

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }

    public LocalDate getJoinedDate() {
        return joinedDate;
    }

    public void setJoinedDate(LocalDate joinedDate) {
        this.joinedDate = joinedDate;
    }

    public Integer getTotalSpend() {
        return totalSpend;
    }

    public void setTotalSpend(Integer totalSpend) {
        this.totalSpend = totalSpend;
    }

    public List<Appointment> getAppointmentList() {
        return appointmentList;
    }

    public void setAppointmentList(List<Appointment> appointmentList) {
        this.appointmentList = appointmentList;
    }

    public List<Payment> getPaymentList() {
        return paymentList;
    }

    public void setPaymentList(List<Payment> paymentList) {
        this.paymentList = paymentList;
    }

    public List<CouponService> getCouponServices() {
        return couponServices;
    }

    public void setCouponServices(List<CouponService> couponServices) {
        this.couponServices = couponServices;
    }
}


