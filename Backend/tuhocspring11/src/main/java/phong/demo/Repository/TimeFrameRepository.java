package phong.demo.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import phong.demo.Entity.Employee;
import phong.demo.Entity.TimeFrame;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TimeFrameRepository extends JpaRepository<TimeFrame,Long> {

    Optional<TimeFrame> findByDate(LocalDate date);
    Optional<TimeFrame> findByDateAndEmployeeId (LocalDate date, Long employeeId);


    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END " +
            "FROM TimeFrame t " +
            "WHERE t.employee.employee_id = :employeeId " +
            "AND FUNCTION('YEAR', t.date) = :year " +
            "AND FUNCTION('MONTH', t.date) = :month")
    boolean existsByEmployeeIdAndMonthAndYear(@Param("employeeId") Long employeeId,
                                              @Param("month") int month,
                                              @Param("year") int year);





}
