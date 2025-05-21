package phong.demo.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import phong.demo.Entity.Appointment;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface AppointmentRepository extends JpaRepository <Appointment, Long> {
    Optional<Appointment> findByConfirmationNumber (int confirmationNumber);
    List<Appointment> findAllByUserId(Long userId);
    Optional<Appointment> findByDateAndStartTime(LocalDate date, LocalTime startTime);
    List<Appointment> findByDate(LocalDate date);



}
