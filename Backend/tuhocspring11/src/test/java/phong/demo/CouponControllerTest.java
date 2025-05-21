package phong.demo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.ResponseEntity;
import phong.demo.Controller.CouponController;
import phong.demo.Entity.*;
import phong.demo.Repository.*;
import phong.demo.Springpro.ResponseMessagge;
import phong.demo.Springpro.SendEmailService;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class CouponControllerTest {

    @Mock private CouponRepository couponRepository;
    @Mock private ProfileRepository profileRepository;
    @Mock private CouponServiceRepository couponServiceRepository;
    @Mock private SendEmailService sendEmailService;

    @InjectMocks
    private CouponController couponController;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    // ✅ /generate
    @Test
    void testGenerateCoupon_Success() {
        Coupon coupon = new Coupon("ABC123", 10, "10% OFF", LocalDateTime.now().plusDays(1));

        when(couponRepository.findByCode("ABC123")).thenReturn(Optional.empty());

        ResponseEntity<?> response = couponController.generateCoupon(coupon);

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(((ResponseMessagge) response.getBody()).getMessage()).isEqualTo("Coupon created successfully");
        verify(couponRepository).save(any(Coupon.class));
    }

    @Test
    void testGenerateCoupon_AlreadyExists() {
        Coupon coupon = new Coupon("ABC123", 10, "10% OFF", LocalDateTime.now().plusDays(1));
        when(couponRepository.findByCode("ABC123")).thenReturn(Optional.of(coupon));

        ResponseEntity<?> response = couponController.generateCoupon(coupon);

        assertThat(response.getStatusCodeValue()).isEqualTo(400);
        assertThat(((ResponseMessagge) response.getBody()).getMessage()).contains("Coupon existed");
    }

    @Test
    void testGenerateCoupon_Expired() {
        Coupon coupon = new Coupon("EXPIRED", 20, "Expired", LocalDateTime.now().minusDays(1));
        when(couponRepository.findByCode("EXPIRED")).thenReturn(Optional.empty());

        ResponseEntity<?> response = couponController.generateCoupon(coupon);

        assertThat(response.getStatusCodeValue()).isEqualTo(400);
        assertThat(((ResponseMessagge) response.getBody()).getMessage()).contains("Code expired");
    }

    // ✅ /send
    @Test
    void testSendCoupon_Success() throws Exception {
        Long userId = 1L;
        Integer points = 100;
        String code = "NEWCODE";

        User user = new User();
        user.setId(userId);
        user.setEmail("user@email.com");

        Profile profile = new Profile();
        profile.setId(userId);
        profile.setUser(user);

        Coupon coupon = new Coupon();
        coupon.setCode(code);
        coupon.setCouponServices(new ArrayList<>());

        when(profileRepository.findById(userId)).thenReturn(Optional.of(profile));
        when(couponRepository.findByCode(code)).thenReturn(Optional.of(coupon));

        ResponseEntity<?> response = couponController.sendingCoupon(userId, points, code);

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(((ResponseMessagge) response.getBody()).getMessage()).contains("Successfully redeemed");

        verify(couponServiceRepository).save(any(CouponService.class));
        verify(sendEmailService).redeemCodeEmail(eq("user@email.com"), any(), eq(code), eq(points));
    }

    @Test
    void testSendCoupon_UserNotFound() {
        when(profileRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseEntity<?> response = couponController.sendingCoupon(99L, 100, "ANY");

        assertThat(response.getStatusCodeValue()).isEqualTo(400);
        assertThat(((ResponseMessagge) response.getBody()).getMessage()).contains("User not found");
    }

//    @Test
//    void testSendCoupon_CouponAlreadyOwned() {
//        Long userId = 1L;
//        String code = "DUPLICATE";
//
//        User user = new User(); user.setId(userId);
//        Profile profile = new Profile(); profile.setUser(user);
//
//        Coupon coupon = new Coupon();
//        CouponService existingService = new CouponService(false, user, coupon);
//        coupon.setCouponServices(List.of(existingService));
//
//        when(profileRepository.findById(userId)).thenReturn(Optional.of(profile));
//        when(couponRepository.findByCode(code)).thenReturn(Optional.of(coupon));
//
//        ResponseEntity<?> response = couponController.sendingCoupon(userId, 100, code);
//
//        assertThat(response.getStatusCodeValue()).isEqualTo(400);
//        assertThat(((ResponseMessagge) response.getBody()).getMessage()).contains("had this coupon");
//    }

    // ✅ /clear
    @Test
    void testClearCoupon_Success() {
        Long userId = 1L;
        String code = "REMOVE";

        User user = new User(); user.setId(userId);
        Profile profile = new Profile(); profile.setUser(user);

        Coupon coupon = new Coupon(); coupon.setCode(code);

        CouponService couponService = new CouponService(false, user, coupon);

        when(profileRepository.findById(userId)).thenReturn(Optional.of(profile));
        when(couponRepository.findByCode(code)).thenReturn(Optional.of(coupon));
        when(couponServiceRepository.findByUserAndCoupon(user, coupon)).thenReturn(Optional.of(couponService));

        ResponseEntity<?> response = couponController.clearCoupon(userId, 100, code);

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(((ResponseMessagge) response.getBody()).getMessage()).contains("Successfully cleared");
        verify(couponServiceRepository).delete(couponService);
    }

    @Test
    void testClearCoupon_AlreadyUsed() {
        User user = new User(); user.setId(1L);
        Profile profile = new Profile(); profile.setUser(user);
        Coupon coupon = new Coupon();
        CouponService couponService = new CouponService(true, user, coupon);

        when(profileRepository.findById(1L)).thenReturn(Optional.of(profile));
        when(couponRepository.findByCode("XYZ")).thenReturn(Optional.of(coupon));
        when(couponServiceRepository.findByUserAndCoupon(user, coupon)).thenReturn(Optional.of(couponService));

        ResponseEntity<?> response = couponController.clearCoupon(1L, 100, "XYZ");

        assertThat(response.getStatusCodeValue()).isEqualTo(400);
        assertThat(((ResponseMessagge) response.getBody()).getMessage()).contains("used this coupon");
    }

    @Test
    void testClearCoupon_NotOwned() {
        User user = new User(); user.setId(1L);
        Profile profile = new Profile(); profile.setUser(user);
        Coupon coupon = new Coupon();

        when(profileRepository.findById(1L)).thenReturn(Optional.of(profile));
        when(couponRepository.findByCode("XYZ")).thenReturn(Optional.of(coupon));
        when(couponServiceRepository.findByUserAndCoupon(user, coupon)).thenReturn(Optional.empty());

        ResponseEntity<?> response = couponController.clearCoupon(1L, 100, "XYZ");

        assertThat(response.getStatusCodeValue()).isEqualTo(400);
        assertThat(((ResponseMessagge) response.getBody()).getMessage()).contains("don't have this coupon");
    }
}
