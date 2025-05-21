package phong.demo;


import org.junit.jupiter.api.Test;
import phong.demo.Entity.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class RatingTest {

    @Test
    void testConstructorAndGetters() {
        Appointment appointment = new Appointment(); // You can mock this if needed
        Rating rating = new Rating(5, "Great service!", appointment);

        assertEquals(5, rating.getStar());
        assertEquals("Great service!", rating.getComment());
        assertEquals(appointment, rating.getAppointment());
    }

    @Test
    void testSetters() {
        Rating rating = new Rating();
        Appointment appointment = new Appointment();

        rating.setId(10L);
        rating.setStar(4);
        rating.setComment("Nice");
        rating.setAppointment(appointment);

        assertEquals(10L, rating.getId());
        assertEquals(4, rating.getStar());
        assertEquals("Nice", rating.getComment());
        assertEquals(appointment, rating.getAppointment());
    }

    @Test
    void testConstructorAndGetters1() {
        LocalDate date = LocalDate.of(2025, 5, 8);
        DailyFinance finance = new DailyFinance(date, 1200);

        assertEquals(date, finance.getDate());
        assertEquals(1200, finance.getTotal());
    }

    @Test
    void testSetters1() {
        DailyFinance finance = new DailyFinance(null, 0);

        finance.setId(1L);
        finance.setDate(LocalDate.of(2025, 1, 1));
        finance.setTotal(999);

        assertEquals(1L, finance.getId());
        assertEquals(LocalDate.of(2025, 1, 1), finance.getDate());
        assertEquals(999, finance.getTotal());
    }
    @Test
    void testConstructorAndGetters2() {
        LocalTime start = LocalTime.of(10, 0);
        LocalTime end = LocalTime.of(11, 0);

        SmallTimeframe smallTimeframe = new SmallTimeframe(start, end);

        assertNull(smallTimeframe.getId()); // default
        assertEquals(start, smallTimeframe.getSubStartTime());
        assertEquals(end, smallTimeframe.getSubEndTime());
        assertTrue(smallTimeframe.isAvailable());
        assertNull(smallTimeframe.getTimeFrame()); // not set yet
    }

    @Test
    void testSetters2() {
        SmallTimeframe stf = new SmallTimeframe();

        stf.setId(1L);
        stf.setSubStartTime(LocalTime.of(9, 30));
        stf.setSubEndTime(LocalTime.of(10, 15));
        stf.setAvailable(false);

        TimeFrame timeFrame = new TimeFrame();
        stf.setTimeFrame(timeFrame);

        assertEquals(1L, stf.getId());
        assertEquals(LocalTime.of(9, 30), stf.getSubStartTime());
        assertEquals(LocalTime.of(10, 15), stf.getSubEndTime());
        assertFalse(stf.isAvailable());
        assertEquals(timeFrame, stf.getTimeFrame());
    }

    @Test
    void testToString2() {
        SmallTimeframe stf = new SmallTimeframe(LocalTime.of(8, 0), LocalTime.of(9, 0));
        stf.setId(42L);
        stf.setAvailable(true);

        String result = stf.toString();
        assertTrue(result.contains("SmallTimeframe"));
        assertTrue(result.contains("subStartTime=08:00"));
        assertTrue(result.contains("subEndTime=09:00"));
        assertTrue(result.contains("available=true"));
    }
    @Test
    void testNoArgsConstructor3() {
        PaymentService ps = new PaymentService();
        assertThat(ps).isNotNull();
    }

    @Test
    void testAllArgsConstructor3() {
        Payment payment = new Payment();
        Service service = new Service();

        PaymentService ps = new PaymentService(payment, service);

        assertThat(ps.getPayment()).isEqualTo(payment);
        assertThat(ps.getService()).isEqualTo(service);
    }

    @Test
    void testSettersAndGetters3() {
        PaymentService ps = new PaymentService();

        ps.setId(10L);
        Payment payment = new Payment();
        Service service = new Service();

        ps.setPayment(payment);
        ps.setService(service);

        assertThat(ps.getId()).isEqualTo(10L);
        assertThat(ps.getPayment()).isSameAs(payment);
        assertThat(ps.getService()).isSameAs(service);
    }

    @Test
    void testNoArgsConstructor4() {
        Payment payment = new Payment();
        assertThat(payment).isNotNull();
    }

    @Test
    void testAllArgsConstructorAndGetters4() {
        User user = new User();
        Employee employee = new Employee();
        Service service = new Service();
        LocalDate date = LocalDate.now();
        LocalTime time = LocalTime.now();
        double amount = 100.0;
        double discount = 10.0;
        double tip = 5.0;
        PaymentMethod method = PaymentMethod.CASH;

        Payment payment = new Payment(user, service, amount, date, time, method, employee, discount, tip);

        assertThat(payment.getUser()).isEqualTo(user);
        assertThat(payment.getAmount()).isEqualTo(amount);
        assertThat(payment.getDate()).isEqualTo(date);
        assertThat(payment.getTime()).isEqualTo(time);
        assertThat(payment.getPaymentMethod()).isEqualTo(method);
        assertThat(payment.getEmployee()).isEqualTo(employee);
        assertThat(payment.getDiscount()).isEqualTo(discount);
        assertThat(payment.getTipping()).isEqualTo(tip);
    }

    @Test
    void testSettersAndGetters6() {
        Payment payment = new Payment();

        User user = new User();
        Employee employee = new Employee();
        PaymentService ps = new PaymentService();
        LocalDate date = LocalDate.of(2025, 5, 8);
        LocalTime time = LocalTime.of(14, 30);

        payment.setId(1L);
        payment.setUser(user);
        payment.setEmployee(employee);
        payment.setAmount(200.0);
        payment.setDate(date);
        payment.setTime(time);
        payment.setDiscount(20.0);
        payment.setTipping(15.0);
        payment.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        payment.setPaymentServices(List.of(ps));

        assertThat(payment.getId()).isEqualTo(1L);
        assertThat(payment.getUser()).isEqualTo(user);
        assertThat(payment.getEmployee()).isEqualTo(employee);
        assertThat(payment.getAmount()).isEqualTo(200.0);
        assertThat(payment.getDate()).isEqualTo(date);
        assertThat(payment.getTime()).isEqualTo(time);
        assertThat(payment.getDiscount()).isEqualTo(20.0);
        assertThat(payment.getTipping()).isEqualTo(15.0);
        assertThat(payment.getPaymentMethod()).isEqualTo(PaymentMethod.CREDIT_CARD);
        //assertThat(payment.getPaymentServices()).hasSize(1);
    }

    @Test
    void testNoArgsConstructor0() {
        Notification notification = new Notification();
        assertThat(notification).isNotNull();
    }

    @Test
    void testAllArgsConstructor() {
        String employeeName = "Jane";
        String caption = "New design uploaded";
        String imageURL = "https://example.com/image.jpg";

        Notification notification = new Notification(employeeName, caption, imageURL);

        assertThat(notification.getEmployeeName()).isEqualTo(employeeName);
        assertThat(notification.getCaption()).isEqualTo(caption);
        assertThat(notification.getImageURL()).isEqualTo(imageURL);
    }

    @Test
    void testSettersAndGetters0() {
        Notification notification = new Notification();

        notification.setId(101L);
        notification.setEmployeeName("Phong");
        notification.setCaption("Check out this style!");
        notification.setImageURL("https://nailsync.com/style123.jpg");

        assertThat(notification.getId()).isEqualTo(101L);
        assertThat(notification.getEmployeeName()).isEqualTo("Phong");
        assertThat(notification.getCaption()).isEqualTo("Check out this style!");
        assertThat(notification.getImageURL()).isEqualTo("https://nailsync.com/style123.jpg");
    }

    @Test
    void testNoArgsConstructorr() {
        Handler handler = new Handler();
        assertThat(handler).isNotNull();
    }

    @Test
    void testAllArgsConstructorAndGetterss() {
        Employee employee = new Employee();
        Service service = new Service();

        Handler handler = new Handler(employee, service);

        assertThat(handler.getEmployee()).isEqualTo(employee);
        assertThat(handler.getService()).isEqualTo(service);
    }

    @Test
    void testSettersAndGetterss() {
        Handler handler = new Handler();

        Employee employee = new Employee();
        Service service = new Service();

        handler.setId(10L);
        handler.setEmployee(employee);
        handler.setService(service);

        assertThat(handler.getId()).isEqualTo(10L);
        assertThat(handler.getEmployee()).isEqualTo(employee);
        assertThat(handler.getService()).isEqualTo(service);
    }

    @Test
    void testNoArgsConstructor() {
        EmployeeDailyFinance finance = new EmployeeDailyFinance();
        assertThat(finance).isNotNull();
    }

    @Test
    void testAllArgsConstructorAndGetters() {
        LocalDate date = LocalDate.of(2025, 5, 8);
        Employee employee = new Employee();

        EmployeeDailyFinance finance = new EmployeeDailyFinance(date, 200.0, 150.0, 50.0, employee);

        assertThat(finance.getDate()).isEqualTo(date);
        assertThat(finance.getTotalincome()).isEqualTo(200.0);
        assertThat(finance.getIncome()).isEqualTo(150.0);
        assertThat(finance.getTip()).isEqualTo(50.0);
        assertThat(finance.getEmployee()).isEqualTo(employee);
    }

    @Test
    void testSettersAndGetters() {
        EmployeeDailyFinance finance = new EmployeeDailyFinance();
        Employee employee = new Employee();
        LocalDate date = LocalDate.now();

        finance.setId(1L);
        finance.setDate(date);
        finance.setIncome(100.0);
        finance.setTip(20.0);
        finance.setTotalincome(120.0);
        finance.setEmployee(employee);

        assertThat(finance.getId()).isEqualTo(1L);
        assertThat(finance.getDate()).isEqualTo(date);
        assertThat(finance.getIncome()).isEqualTo(100.0);
        assertThat(finance.getTip()).isEqualTo(20.0);
        assertThat(finance.getTotalincome()).isEqualTo(120.0);
        assertThat(finance.getEmployee()).isEqualTo(employee);
    }

    @Test
    void testAdminEntity() {
        Admin admin = new Admin("adminUser", "securePass", "admin@mail.com");
        admin.setId(1L);

        assertThat(admin.getId()).isEqualTo(1L);
        assertThat(admin.getUsername()).isEqualTo("adminUser");
        assertThat(admin.getPassword()).isEqualTo("securePass");
        assertThat(admin.getEmail()).isEqualTo("admin@mail.com");
    }

    @Test
    void testCouponServiceEntity() {
        User user = new User();
        Coupon coupon = new Coupon();

        CouponService cs = new CouponService(true, user, coupon);
        cs.setId(10L);

        assertThat(cs.getId()).isEqualTo(10L);
        assertThat(cs.isUsed()).isTrue();
        assertThat(cs.getUser()).isEqualTo(user);
        assertThat(cs.getCoupon()).isEqualTo(coupon);
    }

    @Test
    void testEmployeeEntityAndHandler() {
        LocalDate dob = LocalDate.of(1995, 5, 10);
        Employee emp = new Employee("Tom", "12345", "tom@mail.com", true, dob);
        emp.setId(5L);

        assertThat(emp.getUsername()).isEqualTo("Tom");
        assertThat(emp.getEmail()).isEqualTo("tom@mail.com");
        assertThat(emp.getServicePassword()).isEqualTo("12345");
        assertThat(emp.isAvailable()).isTrue();
        assertThat(emp.getDob()).isEqualTo(dob);
        assertThat(emp.getId()).isEqualTo(5L);

        // Handler addition
        Service service = new Service();
        emp.Add_Servive(service);
        //assertThat(emp.getEmployee_Handler()).hasSize(1);
        assertThat(emp.getEmployee_Handler().get(0).getService()).isEqualTo(service);
        assertThat(emp.getEmployee_Handler().get(0).getEmployee()).isEqualTo(emp);
    }
}

