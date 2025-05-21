package phong.demo.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import phong.demo.Entity.AppointmentService;

import java.util.List;

public interface AppointmentServiceRepository extends JpaRepository<AppointmentService, Long> {

    List<AppointmentService> findByAppointmentId (Long id);


    List<AppointmentService> findAllById(Long employeeId);
}
