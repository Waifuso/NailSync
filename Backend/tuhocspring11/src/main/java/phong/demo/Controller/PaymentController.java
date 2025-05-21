package phong.demo.Controller;


import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.MessagingException;
import org.springframework.web.bind.annotation.*;
import phong.demo.DTO.PaymentDTO;
import phong.demo.Entity.*;
import phong.demo.Repository.*;
import phong.demo.Springpro.ResponseMessagge;
import phong.demo.Springpro.SendEmailService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(path = "/api/payment")
public class PaymentController {

    @Autowired
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final ServiceRepository serviceRepository;
    private final CouponRepository couponRepository;
    private final CouponServiceRepository couponServiceRepository;
    private final PaymentServiceRepository paymentServiceRepository;
    private final EmployeeReposittory employeeReposittory;
    private SendEmailService sendEmailService;



    public PaymentController(PaymentRepository paymentRepository, UserRepository userRepository, ServiceRepository serviceRepository, CouponRepository couponRepository, CouponServiceRepository couponServiceRepository, PaymentServiceRepository paymentServiceRepository, SendEmailService sendEmailService, EmployeeReposittory employeeReposittory) {
        this.paymentRepository = paymentRepository;
        this.userRepository = userRepository;
        this.serviceRepository = serviceRepository;
        this.couponRepository = couponRepository;
        this.couponServiceRepository = couponServiceRepository;
        this.paymentServiceRepository = paymentServiceRepository;
        this.sendEmailService = sendEmailService;
        this.employeeReposittory = employeeReposittory;
    }

    //Get userRanks and userPoints
    @GetMapping(path = "/userRank&Points/{userId}")
    public ResponseEntity<?> userRankingAndPoints (@PathVariable Long userId) {
        Optional<User> RankingAndPoints = userRepository.findById(userId);
        if (RankingAndPoints.isEmpty()) {
            return ResponseEntity.badRequest().body(new ResponseMessagge("There are no ranking or points display"));
        }
        return ResponseEntity.ok(RankingAndPoints.get().getProfile());
    }


    //Get all the olds payments by user Id
    @GetMapping(path = "/userPayment/{userId}")
    public ResponseEntity<?> oldPayments (@PathVariable Long userId) {

        List<Payment> oldPay = paymentRepository.findAllById(userId);
        if (oldPay.isEmpty()){
            return ResponseEntity.badRequest().body(new ResponseMessagge("No old payments found"));
        }
        return ResponseEntity.ok(oldPay);
    }


    // make a payment, update ranking, total points and send out notification with confirmation payment
    @PostMapping(path = "/pay")
    public ResponseEntity<?> payment (@RequestBody PaymentDTO paymentDTO) {

        // check User

        Optional<User> existingID = userRepository.findById(paymentDTO.getUserId());
        if (existingID.isEmpty()) {
            return ResponseEntity.badRequest().body(new ResponseMessagge("User not found"));
        }
        User user = existingID.get();

        //check employee

        Optional<Employee> existingEmployeeID = employeeReposittory.findById(paymentDTO.getEmployeeId());
        if (existingEmployeeID.isEmpty()) {
            return ResponseEntity.badRequest().body(new ResponseMessagge("Employee not found"));
        }
        Employee employee = existingEmployeeID.get();

        // check Service
        List<Service> existingSer = serviceRepository.findAllById(paymentDTO.getServiceId());
        if (existingSer.size() != paymentDTO.getServiceId().size()) {
            return ResponseEntity.badRequest().body(new ResponseMessagge("One or more services not found"));

        }

        // sum Total

        int totalPrice = existingSer.stream()
                .mapToInt(Service::getPrice)      // get price
                .sum();                        // sum them

        // check Ranking
        user.setTotalSpend(user.getTotalSpend() + totalPrice);

        int newRanking = user.getTotalSpend();

        Ranking ranking = Ranking.fromMoneySpent(newRanking);
        user.getProfile().setRanking(ranking);
        user.getProfile().setTotalPoints(newRanking);



        // check coupon
        String couponCode = paymentDTO.getCouponCode();

        int offDiscount = 0;

        if (couponCode != null && !couponCode.trim().isEmpty()) {
            // User provided a coupon — validate and apply it
            Optional<Coupon> optionalCoupon = couponRepository.findByCode(couponCode);

            if (optionalCoupon.isEmpty()) {
                return ResponseEntity.badRequest().body(new ResponseMessagge("Coupon code is invalid"));
            }

            Coupon coupon = optionalCoupon.get();

            // Optionally check expiry
            if (coupon.getExpiryDate().isBefore(LocalDateTime.now())) {
                return ResponseEntity.badRequest().body(new ResponseMessagge("Coupon is expired"));
            }

            // Optionally mark it as used or check if used
            Optional<CouponService> couponService = couponServiceRepository.findByUserAndCoupon(user, coupon);
            if (couponService.isEmpty() || couponService.get().isUsed()) {
                return ResponseEntity.badRequest().body(new ResponseMessagge("Coupon has already been used"));
            }

            // Calculate discount
            int discountPercentage = coupon.getDiscountPercentage();
            int discountedPrice = totalPrice - (totalPrice * discountPercentage / 100);
            offDiscount += (totalPrice * discountPercentage / 100);

            totalPrice = discountedPrice;

            // Mark as used
            couponService.get().setUsed(true);
            couponServiceRepository.save(couponService.get());

        }

        int discount = ranking.getDiscountPercentage();

        int discountTotalPrice = totalPrice - (totalPrice * discount / 100);

        offDiscount += (totalPrice * discount / 100);

        totalPrice = discountTotalPrice;

        LocalDate todayDate = LocalDate.now();

        LocalTime todayTime = LocalTime.now();


        Payment payment = new Payment();
        payment.setPaymentMethod(paymentDTO.getPaymentMethod());
        payment.setDate(todayDate);
        payment.setTime(todayTime);
        payment.setUser(user);
        payment.setAmount(totalPrice);
        payment.setEmployee(employee);
        payment.setTipping(paymentDTO.getTipping());
        payment.setDiscount(offDiscount);

        Payment savedPayment = paymentRepository.save(payment);


        for (Long serviceId: paymentDTO.getServiceId()){
            Service service = serviceRepository.findById(serviceId)
                    .orElseThrow(() -> new RuntimeException("Service ID not found: " + serviceId));
            PaymentService ps = new PaymentService(savedPayment, service);
            paymentServiceRepository.save(ps);
        }
        userRepository.save(user);


        try {
            try {
                sendEmailService.paymentSending(
                        user.getEmail(),
                        "Your NAILSYNC Confirmation Payment",
                        payment.getAmount()
                );
            } catch (jakarta.mail.MessagingException e) {
                throw new RuntimeException(e);
            }
        } catch (MessagingException e) {
            e.printStackTrace();
        }


        return ResponseEntity.ok(new ResponseMessagge("Payment successful. Amount charged: " + totalPrice + " with ranking: " + ranking.getDisplayName()));
    }


}
