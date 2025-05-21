package phong.demo.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import phong.demo.Entity.Employee;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface EmployeeReposittory extends JpaRepository<Employee,Long> {

    Optional<Employee>findByUsername(String username);

    boolean existsByUsername(String username);

    Optional<Employee> findByEmail(String email);

    // Returns employees whose TimeFrame record matches the specified date.
    @Query("SELECT DISTINCT e FROM Employee e JOIN e.timeFrame t WHERE t.date = :date")
    List<Employee> findEmployeesByTimeFrameDate(@Param("date") LocalDate date);





}
