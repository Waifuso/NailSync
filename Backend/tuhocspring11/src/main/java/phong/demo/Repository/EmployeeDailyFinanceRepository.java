package phong.demo.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import phong.demo.Entity.DailyFinance;
import phong.demo.Entity.EmployeeDailyFinance;

import java.time.LocalDate;
import java.util.Optional;

public interface EmployeeDailyFinanceRepository extends JpaRepository<EmployeeDailyFinance, DailyFinance> {


    boolean existsByDate(LocalDate localDate);

    Optional<EmployeeDailyFinance> findByDate(LocalDate localDate);
}
