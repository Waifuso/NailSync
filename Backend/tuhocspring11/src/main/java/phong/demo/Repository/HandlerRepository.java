package phong.demo.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import phong.demo.Entity.Employee;
import phong.demo.Entity.Handler;
import phong.demo.Entity.Service;

import java.util.Optional;

public interface HandlerRepository extends JpaRepository<Handler, Long> {
    Optional<Handler> findByServiceIdAndEmployeeId(Long serviceId, Long employeeId);


}
