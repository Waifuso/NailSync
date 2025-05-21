package phong.demo.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import phong.demo.Entity.Coupon;
import phong.demo.Entity.CouponService;
import phong.demo.Entity.User;

import java.util.Optional;

public interface CouponServiceRepository extends JpaRepository<CouponService, Long> {

    Optional<CouponService> findByUserAndCoupon (User user, Coupon coupon);
}
