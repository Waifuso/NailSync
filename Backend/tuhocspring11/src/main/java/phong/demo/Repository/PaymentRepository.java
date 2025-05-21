package phong.demo.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import phong.demo.Entity.Payment;

import java.time.LocalDate;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {


    List<Payment> findAllById(Long userId);
    List<Payment> findAllByDate(LocalDate localDate);
    List<Payment> findAllByDateAndEmployeeId (LocalDate date, Long employeeId);

}
