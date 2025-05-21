package phong.demo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.ResponseEntity;
import phong.demo.Controller.PaymentController;
import phong.demo.DTO.PaymentDTO;
import phong.demo.Entity.*;
import phong.demo.Repository.*;
import phong.demo.Springpro.ResponseMessagge;
import phong.demo.Springpro.SendEmailService;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class PaymentControllerTest {

    @Mock private PaymentRepository paymentRepository;
    @Mock private UserRepository userRepository;
    @Mock private ServiceRepository serviceRepository;
    @Mock private CouponRepository couponRepository;
    @Mock private CouponServiceRepository couponServiceRepository;
    @Mock private PaymentServiceRepository paymentServiceRepository;
    @Mock private EmployeeReposittory employeeReposittory;
    @Mock private SendEmailService sendEmailService;

    @InjectMocks
    private PaymentController paymentController;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    // ✅ Payment success with no coupon
    @Test
    void testPayment_Success_NoCoupon() {
        PaymentDTO dto = new PaymentDTO();
        dto.setUserId(1L);
        dto.setEmployeeId(2L);
        dto.setServiceId(List.of(10L, 11L));
        dto.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        dto.setTipping(5);

        User user = new User(); user.setId(1L); user.setTotalSpend(100); user.setEmail("user@test.com");
        Profile profile = new Profile(); profile.setRanking(Ranking.BRONZE); profile.setTotalPoints(0);
        user.setProfile(profile);

        Employee employee = new Employee(); employee.setId(2L);

        Service s1 = new Service(); s1.setId(10L); s1.setPrice(30);
        Service s2 = new Service(); s2.setId(11L); s2.setPrice(20);

        Payment savedPayment = new Payment(); savedPayment.setId(999L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(employeeReposittory.findById(2L)).thenReturn(Optional.of(employee));
        when(serviceRepository.findAllById(dto.getServiceId())).thenReturn(List.of(s1, s2));
        when(paymentRepository.save(any())).thenReturn(savedPayment);
        when(serviceRepository.findById(10L)).thenReturn(Optional.of(s1));
        when(serviceRepository.findById(11L)).thenReturn(Optional.of(s2));

        ResponseEntity<?> response = paymentController.payment(dto);

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(((ResponseMessagge) response.getBody()).getMessage())
                .contains("Payment successful").contains("Bronze");

        verify(userRepository).save(user);
        verify(paymentServiceRepository, times(2)).save(any());
    }

    // ✅ User not found
    @Test
    void testPayment_UserNotFound() {
        PaymentDTO dto = new PaymentDTO();
        dto.setUserId(99L);
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseEntity<?> response = paymentController.payment(dto);

        assertThat(response.getStatusCodeValue()).isEqualTo(400);
        assertThat(((ResponseMessagge) response.getBody()).getMessage()).contains("User not found");
    }

    // ✅ Employee not found
    @Test
    void testPayment_EmployeeNotFound() {
        PaymentDTO dto = new PaymentDTO();
        dto.setUserId(1L);
        dto.setEmployeeId(99L);
        User user = new User(); user.setId(1L); user.setProfile(new Profile());

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(employeeReposittory.findById(99L)).thenReturn(Optional.empty());

        ResponseEntity<?> response = paymentController.payment(dto);

        assertThat(response.getStatusCodeValue()).isEqualTo(400);
        assertThat(((ResponseMessagge) response.getBody()).getMessage()).contains("Employee not found");
    }

    // ✅ Coupon invalid
    @Test
    void testPayment_InvalidCoupon() {
        PaymentDTO dto = new PaymentDTO();
        dto.setUserId(1L);
        dto.setEmployeeId(2L);
        dto.setServiceId(List.of(10L));
        dto.setCouponCode("INVALID");

        User user = new User(); user.setId(1L); user.setEmail("test@x.com");
        Profile profile = new Profile(); profile.setRanking(Ranking.SILVER);
        user.setProfile(profile);

        Employee emp = new Employee(); emp.setId(2L);
        Service s = new Service(); s.setId(10L); s.setPrice(100);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(employeeReposittory.findById(2L)).thenReturn(Optional.of(emp));
        when(serviceRepository.findAllById(List.of(10L))).thenReturn(List.of(s));
        when(couponRepository.findByCode("INVALID")).thenReturn(Optional.empty());

        ResponseEntity<?> response = paymentController.payment(dto);

        assertThat(response.getStatusCodeValue()).isEqualTo(400);
        assertThat(((ResponseMessagge) response.getBody()).getMessage()).contains("Coupon code is invalid");
    }

    // ✅ Coupon already used
//    @Test
//    void testPayment_UsedCoupon() {
//        PaymentDTO dto = new PaymentDTO();
//        dto.setUserId(1L);
//        dto.setEmployeeId(2L);
//        dto.setServiceId(List.of(10L));
//        dto.setCouponCode("USED");
//
//        User user = new User(); user.setId(1L); user.setProfile(new Profile());
//        Employee emp = new Employee(); emp.setId(2L);
//        Service s = new Service(); s.setId(10L); s.setPrice(100);
//        Coupon c = new Coupon(); c.setCode("USED");
//        CouponService cs = new CouponService(true, user, c); // already used
//
//        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
//        when(employeeReposittory.findById(2L)).thenReturn(Optional.of(emp));
//        when(serviceRepository.findAllById(List.of(10L))).thenReturn(List.of(s));
//        when(couponRepository.findByCode("USED")).thenReturn(Optional.of(c));
//        when(couponServiceRepository.findByUserAndCoupon(user, c)).thenReturn(Optional.of(cs));
//
//        ResponseEntity<?> response = paymentController.payment(dto);
//
//        assertThat(response.getStatusCodeValue()).isEqualTo(400);
//        assertThat(((ResponseMessagge) response.getBody()).getMessage()).contains("already been used");
//    }
}
