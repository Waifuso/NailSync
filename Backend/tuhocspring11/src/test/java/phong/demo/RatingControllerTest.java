package phong.demo;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import phong.demo.Controller.RatingController;
import phong.demo.DTO.RatingDTO;
import phong.demo.Entity.*;
import phong.demo.Repository.RatingRepository;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class RatingControllerTest {

    @Mock
    private RatingRepository ratingRepository;

    @InjectMocks
    private RatingController ratingController;

    public RatingControllerTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGettingAllReviews() {
        // Prepare test data
        Service service = new Service();
        service.setService_name("Gel Polish");

        Employee employee = new Employee();
        employee.setUsername("nailtech123");

        AppointmentService appointmentService = new AppointmentService();
        appointmentService.setService(service);
        appointmentService.setEmployee(employee);

        Appointment appointment = new Appointment();
        appointment.setDate(LocalDate.of(2025, 5, 7));
        appointment.setAppointmentServices(List.of(appointmentService));

        Rating rating = new Rating();
        rating.setAppointment(appointment);
        rating.setStar(5);
        rating.setComment("Great service!");

        when(ratingRepository.findAll()).thenReturn(List.of(rating));

        // Act
        List<RatingDTO> result = ratingController.gettingAllReviews();

        // Assert
        assertThat(result).hasSize(1);
        RatingDTO dto = result.get(0);
        assertThat(dto.getDate()).isEqualTo(appointment.getDate());
        assertThat(dto.getServiceName()).containsExactly("Gel Polish");
        assertThat(dto.getEmployee()).isEqualTo("nailtech123");
    }
}
