package phong.demo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.ResponseEntity;
import phong.demo.Controller.AppointmentController;
import phong.demo.DTO.BookingDTO;
import phong.demo.Entity.*;
import phong.demo.Repository.*;
import phong.demo.Service.ProfileService;
import phong.demo.Service.TimeframeService;
import phong.demo.Springpro.RandomNumberService;
import phong.demo.Springpro.ResponseMessagge;
import phong.demo.Springpro.SendEmailService;

import java.time.*;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class AppointmentControllerTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private AppointmentServiceRepository appointmentServiceRepository;

    @Mock
    private TimeframeService timeframeService;

    @Mock private EmployeeReposittory employeeReposittory;
    @Mock private ServiceRepository serviceRepository;
    @Mock private UserRepository userRepository;
    @Mock private HandlerRepository handlerRepository;
    @Mock private TimeFrameRepository timeFrameRepository;
    @Mock private SmallTimeFrameRepository smallTimeFrameRepository;
    @Mock private RandomNumberService randomNumberService;
    @Mock private SendEmailService sendEmailService;
    @Mock private ProfileService profileService;

    @InjectMocks
    private AppointmentController appointmentController;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testChangeAppointment_SuccessfulUpdate() {
        // Given
        Long appointmentId = 1L;
        LocalDate newDate = LocalDate.of(2025, 5, 10);
        LocalTime newTime = LocalTime.of(14, 0);

        Appointment appointment = new Appointment();
        appointment.setId(appointmentId);
        appointment.setDate(LocalDate.of(2025, 5, 7));
        appointment.setStartTime(LocalTime.of(12, 0));
        appointment.setEndTime(LocalTime.of(13, 30)); // 90 minutes

        AppointmentService appointmentService = new AppointmentService();
        Employee employee = new Employee();
        employee.setUsername("nailtech1");
        appointmentService.setEmployee(employee);

        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));
        when(appointmentServiceRepository.findByAppointmentId(appointmentId)).thenReturn(List.of(appointmentService));
        when(timeframeService.checkTimeframe(eq(employee), eq(newTime), eq(newTime.plusMinutes(90)), eq(newDate))).thenReturn(true);
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);

        // When
        ResponseEntity<?> response = appointmentController.changeAppointment(appointmentId, newDate, newTime);

        // Then
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(((ResponseMessagge) response.getBody()).getMessage()).isEqualTo("Appointment updated successfully");

        verify(appointmentRepository).save(appointment);
        assertThat(appointment.getDate()).isEqualTo(newDate);
        assertThat(appointment.getStartTime()).isEqualTo(newTime);
        assertThat(appointment.getEndTime()).isEqualTo(newTime.plusMinutes(90));
    }

    @Test
    void testChangeAppointment_AppointmentNotFound() {
        when(appointmentRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseEntity<?> response = appointmentController.changeAppointment(1L, LocalDate.now(), LocalTime.now());

        assertThat(response.getStatusCodeValue()).isEqualTo(400);
        assertThat(((ResponseMessagge) response.getBody()).getMessage()).contains("Couldn't find the appointment");
    }

    @Test
    void testChangeAppointment_ConflictedTime() {
        Long appointmentId = 1L;
        Appointment appointment = new Appointment();
        appointment.setStartTime(LocalTime.of(10, 0));
        appointment.setEndTime(LocalTime.of(11, 30)); // 90 minutes

        AppointmentService appointmentService = new AppointmentService();
        Employee employee = new Employee();
        appointmentService.setEmployee(employee);

        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));
        when(appointmentServiceRepository.findByAppointmentId(appointmentId)).thenReturn(List.of(appointmentService));
        when(timeframeService.checkTimeframe(any(), any(), any(), any())).thenReturn(false);

        ResponseEntity<?> response = appointmentController.changeAppointment(appointmentId, LocalDate.now(), LocalTime.of(9, 0));

        assertThat(response.getStatusCodeValue()).isEqualTo(400);
        assertThat(((ResponseMessagge) response.getBody()).getMessage()).isEqualTo("Conflicted hours");
    }

    @Test
    void testGetAppointmentsByUserId_Found() {
        User user = new User(); user.setId(1L);
        Appointment appt = new Appointment(); appt.setId(100L); appt.setUser(user);
        Service service = new Service(); service.setId(55L);
        Employee employee = new Employee(); employee.setUsername("john");
        AppointmentService apptService = new AppointmentService(); apptService.setService(service); apptService.setEmployee(employee);

        when(appointmentRepository.findAllByUserId(1L)).thenReturn(List.of(appt));
        when(appointmentServiceRepository.findByAppointmentId(100L)).thenReturn(List.of(apptService));

        ResponseEntity<?> response = appointmentController.getAppointmentByUserId(1L);
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(((List<?>) response.getBody())).hasSize(1);
    }

    @Test
    void testGetAppointmentsByUserId_Empty() {
        when(appointmentRepository.findAllByUserId(1L)).thenReturn(Collections.emptyList());
        ResponseEntity<?> response = appointmentController.getAppointmentByUserId(1L);
        assertThat(response.getStatusCodeValue()).isEqualTo(400);
    }

    // ✅ POST /booking
    @Test
    void testBookingAppointment_Success() {
        BookingDTO dto = new BookingDTO();
        dto.setUserId(1L);
        dto.setEmployeeId(2L);
        dto.setDate(LocalDate.now());
        dto.setTime(LocalTime.NOON);
        dto.setServiceId(List.of(10L));

        User user = new User(); user.setId(1L); user.setEmail("test@email.com");
        Employee employee = new Employee(); employee.setEmployee_id(2L);
        Service service = new Service(); service.setId(10L); service.setDuration(30);
        Handler handler = new Handler();

        TimeFrame timeFrame = new TimeFrame();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(employeeReposittory.findById(2L)).thenReturn(Optional.of(employee));
        when(serviceRepository.findAllById(dto.getServiceId())).thenReturn(List.of(service));
        when(handlerRepository.findByServiceIdAndEmployeeId(10L, 2L)).thenReturn(Optional.of(handler));
        when(timeframeService.checkTimeframe(any(), any(), any(), any())).thenReturn(true);
        when(timeFrameRepository.findByDateAndEmployeeId(dto.getDate(), 2L)).thenReturn(Optional.of(timeFrame));
        when(randomNumberService.generateRandomNumber(anyInt())).thenReturn(123456);

        ResponseEntity<?> response = appointmentController.bookingAppointment(dto);
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(((ResponseMessagge) response.getBody()).getMessage()).contains("successfully");
    }

    @Test
    void testBookingAppointment_UserNotFound() {
        BookingDTO dto = new BookingDTO(); dto.setUserId(100L);
        when(userRepository.findById(100L)).thenReturn(Optional.empty());

        ResponseEntity<?> response = appointmentController.bookingAppointment(dto);
        assertThat(response.getStatusCodeValue()).isEqualTo(400);
    }

    @Test
    void testBookingAppointment_EmployeeNotFound() {
        BookingDTO dto = new BookingDTO(); dto.setUserId(1L); dto.setEmployeeId(2L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(new User()));
        when(employeeReposittory.findById(2L)).thenReturn(Optional.empty());

        ResponseEntity<?> response = appointmentController.bookingAppointment(dto);
        assertThat(response.getStatusCodeValue()).isEqualTo(400);
    }

    @Test
    void testBookingAppointment_ServiceNotFound() {
        BookingDTO dto = new BookingDTO(); dto.setUserId(1L); dto.setEmployeeId(2L); dto.setServiceId(List.of(1L));
        when(userRepository.findById(1L)).thenReturn(Optional.of(new User()));
        when(employeeReposittory.findById(2L)).thenReturn(Optional.of(new Employee()));
        when(serviceRepository.findAllById(dto.getServiceId())).thenReturn(Collections.emptyList());

        ResponseEntity<?> response = appointmentController.bookingAppointment(dto);
        assertThat(response.getStatusCodeValue()).isEqualTo(400);
    }

    @Test
    void testBookingAppointment_ConflictHours() {
        BookingDTO dto = new BookingDTO(); dto.setUserId(1L); dto.setEmployeeId(2L); dto.setServiceId(List.of(1L));
        Service service = new Service(); service.setDuration(30);

        when(userRepository.findById(1L)).thenReturn(Optional.of(new User()));
        when(employeeReposittory.findById(2L)).thenReturn(Optional.of(new Employee()));
        when(serviceRepository.findAllById(dto.getServiceId())).thenReturn(List.of(service));
        when(timeframeService.checkTimeframe(any(), any(), any(), any())).thenReturn(false);

        //ResponseEntity<?> response = appointmentController.bookingAppointment(dto);
        //assertThat(response.getStatusCodeValue()).isEqualTo(400);
    }

    // ✅ DELETE /cancel/{id}
    @Test
    void testCancelAppointment_Success() {
        Appointment appt = new Appointment(); appt.setId(1L);

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appt));

        ResponseEntity<?> response = appointmentController.cancelAppointment(1L);

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(((ResponseMessagge) response.getBody()).getMessage()).contains("canceled");
    }

    @Test
    void testCancelAppointment_NotFound() {
        when(appointmentRepository.findById(999L)).thenReturn(Optional.empty());

        ResponseEntity<?> response = appointmentController.cancelAppointment(999L);
        assertThat(response.getStatusCodeValue()).isEqualTo(400);
    }

    // ✅ PUT /change/{appointmentId}/{date}/{time}
    @Test
    void testChangeAppointment_Success() {
        Appointment appt = new Appointment();
        appt.setStartTime(LocalTime.of(10, 0));
        appt.setEndTime(LocalTime.of(11, 0));
        appt.setId(1L);

        Employee employee = new Employee();
        AppointmentService apptService = new AppointmentService();
        apptService.setEmployee(employee);

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appt));
        when(appointmentServiceRepository.findByAppointmentId(1L)).thenReturn(List.of(apptService));
        when(timeframeService.checkTimeframe(any(), any(), any(), any())).thenReturn(true);

        ResponseEntity<?> response = appointmentController.changeAppointment(1L, LocalDate.now(), LocalTime.of(13, 0));
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(((ResponseMessagge) response.getBody()).getMessage()).contains("updated");
    }

    @Test
    void testChangeAppointment_Conflict() {
        Appointment appt = new Appointment(); appt.setStartTime(LocalTime.of(10, 0)); appt.setEndTime(LocalTime.of(11, 0));
        appt.setId(1L);

        AppointmentService apptService = new AppointmentService(); apptService.setEmployee(new Employee());

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appt));
        when(appointmentServiceRepository.findByAppointmentId(1L)).thenReturn(List.of(apptService));
        when(timeframeService.checkTimeframe(any(), any(), any(), any())).thenReturn(false);

        ResponseEntity<?> response = appointmentController.changeAppointment(1L, LocalDate.now(), LocalTime.of(9, 0));
        assertThat(response.getStatusCodeValue()).isEqualTo(400);
    }
}
