package phong.demo.DTO;


import phong.demo.Entity.Employee;
import phong.demo.Entity.PaymentMethod;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class PaymentDTO {

    private Long userId;

    private List<Long> serviceId;

    private String couponCode;

    private PaymentMethod paymentMethod;
    //
    private Long employeeId;
    //
    private double tipping;

    public PaymentDTO () {

    }

    public PaymentDTO(List<Long> serviceId, String couponCode, PaymentMethod paymentMethod, Long employeeId, double discount, double tipping) {
        this.serviceId = serviceId;
        this.couponCode = couponCode;
        this.paymentMethod = paymentMethod;
        this.employeeId = employeeId;
        this.tipping = tipping;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public List<Long> getServiceId() {
        return serviceId;
    }

    public void setServiceId(List<Long> serviceId) {
        this.serviceId = serviceId;
    }

    public String getCouponCode() {
        return couponCode;
    }

    public void setCouponCode(String couponCode) {
        this.couponCode = couponCode;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public double getTipping() {
        return tipping;
    }

    public void setTipping(double tipping) {
        this.tipping = tipping;
    }
}
