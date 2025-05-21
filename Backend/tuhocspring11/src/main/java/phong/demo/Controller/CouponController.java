package phong.demo.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.MessagingException;
import org.springframework.web.bind.annotation.*;
import phong.demo.Entity.Coupon;
import phong.demo.Entity.CouponService;
import phong.demo.Entity.Profile;
import phong.demo.Entity.User;
import phong.demo.Repository.CouponRepository;
import phong.demo.Repository.CouponServiceRepository;
import phong.demo.Repository.ProfileRepository;
import phong.demo.Springpro.ResponseMessagge;
import phong.demo.Springpro.SendEmailService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(path = "api/coupon")
public class CouponController {

    @Autowired
    private final CouponRepository couponRepository;
    private final ProfileRepository profileRepository;
    private final CouponServiceRepository couponServiceRepository;
    private SendEmailService sendEmailService;

    public CouponController(CouponRepository couponRepository, ProfileRepository profileRepository, CouponServiceRepository couponServiceRepository, SendEmailService sendEmailService) {
        this.couponRepository = couponRepository;
        this.profileRepository = profileRepository;
        this.couponServiceRepository = couponServiceRepository;
        this.sendEmailService = sendEmailService;
    }


    // make, generate coupon

    @PostMapping(path = "/generate")
    public ResponseEntity<?> generateCoupon (@RequestBody Coupon coupon) {
        Optional<Coupon> existCoupon = couponRepository.findByCode(coupon.getCode());
        if (existCoupon.isPresent()) {
            return ResponseEntity.badRequest().body(new ResponseMessagge("Coupon existed"));
        }
        LocalDateTime current =LocalDateTime.now();
        if (current.isAfter(coupon.getExpiryDate())){
            return ResponseEntity.badRequest().body(new ResponseMessagge("Code expired"));
        }
        Coupon coupon1 = new Coupon(coupon.getCode(), coupon.getDiscountPercentage(), coupon.getMessage(), coupon.getExpiryDate());

        couponRepository.save(coupon1);

        return ResponseEntity.ok().body(new ResponseMessagge("Coupon created successfully"));
    }

    //Update userPoints, send notifications, redeem coupon
    @PutMapping(path = "/send/{userId}/{userPoints}/{couponCode}")
    public ResponseEntity<?> sendingCoupon (@PathVariable Long userId, @PathVariable Integer userPoints, @PathVariable String couponCode) {
        Optional<Profile> profile = profileRepository.findById(userId);
        if (profile.isEmpty()) {
            return ResponseEntity.badRequest().body(new ResponseMessagge("User not found"));
        }
        Profile profile1 = profile.get();

//        if (profile1.getTotalPoints() - userPoints < 0) {
//            return ResponseEntity.badRequest().body(new ResponseMessagge("Not enough points to redeem reward"));
//        }

        Optional<Coupon> couponOptional = couponRepository.findByCode(couponCode);
        if (couponOptional.isEmpty()) {
            return ResponseEntity.badRequest().body(new ResponseMessagge("Coupon not found"));

        }
        Coupon coupon = couponOptional.get();

        List<CouponService> couponService1 = coupon.getCouponServices();
        for (CouponService couponService : couponService1) {
            if (couponService.getUser().getId() == (userId) &&
                    couponService.getCoupon().getCode().equals(couponCode)) {
                return ResponseEntity.badRequest().body(new ResponseMessagge("You had this coupon"));
            }
        }


        CouponService couponService = new CouponService(false, profile1.getUser(), coupon);
        profile1.setTotalPoints(userPoints);
        profileRepository.save(profile1);
        couponServiceRepository.save(couponService);
        //send email
        try {
            try {
                sendEmailService.redeemCodeEmail(
                        profile1.getUser().getEmail(),
                        "Your NAILSYNC Confirmation Code Redeem",
                        couponCode, userPoints
                );
            } catch (jakarta.mail.MessagingException e) {
                throw new RuntimeException(e);
            }
        } catch (MessagingException e) {
            e.printStackTrace();
        }



        return ResponseEntity.ok().body(new ResponseMessagge("Successfully redeemed coupon, please check your email"));
    }


    @PutMapping(path = "/clear/{userId}/{userPoints}/{couponCode}")
    public ResponseEntity<?> clearCoupon(@PathVariable Long userId, @PathVariable Integer userPoints, @PathVariable String couponCode) {
        Optional<Profile> profile = profileRepository.findById(userId);
        if (profile.isEmpty()) {
            return ResponseEntity.badRequest().body(new ResponseMessagge("User not found"));
        }
        Profile profile1 = profile.get();
        User user = profile1.getUser();

        Optional<Coupon> couponOptional = couponRepository.findByCode(couponCode);
        if (couponOptional.isEmpty()) {
            return ResponseEntity.badRequest().body(new ResponseMessagge("Coupon not found"));
        }
        Coupon coupon = couponOptional.get();

        Optional<CouponService> couponService1 = couponServiceRepository.findByUserAndCoupon(user, coupon);
        if (couponService1.isEmpty()) {
            return ResponseEntity.badRequest().body(new ResponseMessagge("You don't have this coupon"));
        }

        CouponService couponService = couponService1.get();

        if (couponService.isUsed()) {
            return ResponseEntity.badRequest().body(new ResponseMessagge("You used this coupon"));
        }

        profile1.setTotalPoints(userPoints);
        profileRepository.save(profile1); // make sure to persist

        couponServiceRepository.delete(couponService);

        // Optional: send email to confirm deletion

        return ResponseEntity.ok().body(new ResponseMessagge("Successfully cleared coupon, please check your email"));
    }


    // change price of coupon
    // get all the coupon
}
