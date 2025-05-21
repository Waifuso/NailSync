package phong.demo.Entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


@Entity
@Table(name = "coupon")
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String code;

    private int discountPercentage;

    private String message;

    private LocalDateTime expiryDate;

    @OneToMany(mappedBy = "coupon", cascade = CascadeType.ALL)
    @JsonManagedReference("couponService-coupon")
    private List<CouponService> couponServices;

    public Coupon() {

    }

    public Coupon(String code, int discountPercentage, String message, LocalDateTime expiryDate) {
        this.code = code;
        this.discountPercentage = discountPercentage;
        this.message = message;
        this.expiryDate = expiryDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public int getDiscountPercentage() {
        return discountPercentage;
    }

    public void setDiscountPercentage(int discountPercentage) {
        this.discountPercentage = discountPercentage;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDateTime expiryDate) {
        this.expiryDate = expiryDate;
    }

    public List<CouponService> getCouponServices() {
        return couponServices;
    }

    public void setCouponServices(List<CouponService> couponServices) {
        this.couponServices = couponServices;
    }
}


