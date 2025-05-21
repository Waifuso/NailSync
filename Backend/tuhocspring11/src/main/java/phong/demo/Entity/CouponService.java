package phong.demo.Entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.*;

@Entity
@Table (name = "CouponService")
public class CouponService {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private boolean isUsed;

    @ManyToOne
    @JoinColumn
    @JsonBackReference("user-coupon_service")
    private User user;

    @ManyToOne
    @JoinColumn
    @JsonBackReference("couponService-coupon")
    private Coupon coupon;

    public CouponService() {

    }

    public CouponService(boolean isUsed, User user, Coupon coupon) {
        this.isUsed = isUsed;
        this.user = user;
        this.coupon = coupon;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public boolean isUsed() {
        return isUsed;
    }

    public void setUsed(boolean used) {
        isUsed = used;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Coupon getCoupon() {
        return coupon;
    }

    public void setCoupon(Coupon coupon) {
        this.coupon = coupon;
    }


}
