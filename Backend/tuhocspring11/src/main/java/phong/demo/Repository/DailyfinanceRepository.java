package phong.demo.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import phong.demo.Entity.DailyFinance;
import phong.demo.Entity.EmployeeDailyFinance;

import java.time.LocalDate;
import java.util.Optional;

public interface DailyfinanceRepository extends JpaRepository<DailyFinance,Long> {

    boolean existsByDate(LocalDate localDate);

    Optional<DailyFinance> findByDate(LocalDate date);
}
