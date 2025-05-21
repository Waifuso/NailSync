package phong.demo.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import phong.demo.Entity.Payment;
import phong.demo.Entity.PaymentService;

import java.time.LocalDate;
import java.util.List;


public interface PaymentServiceRepository extends JpaRepository<PaymentService, Long> {



}
