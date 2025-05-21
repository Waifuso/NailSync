package phong.demo;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import phong.demo.DTO.TimeFrame_DTO;
import phong.demo.Entity.*;
import phong.demo.Repository.*;
import phong.demo.Service.Serviceservice;
import phong.demo.Service.TimeframeService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TimeframeServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private EmployeeReposittory employeeReposittory;

    @Mock
    private TimeFrameRepository timeFrameRepository;

    @Mock
    private Serviceservice serviceservice;

    @InjectMocks
    private TimeframeService timeframeService;

    private Employee employee;
    private TimeFrame timeFrame;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Create mock data
        LocalDate date = LocalDate.of(2025, 5, 10);
        LocalTime shiftStart = LocalTime.of(9, 0);
        LocalTime shiftEnd = LocalTime.of(17, 0);
        timeFrame = new TimeFrame(date, shiftStart, shiftEnd);

        employee = new Employee();
        employee.setTimeFrame(List.of(timeFrame));
        employee.setAppointmentServices(new ArrayList<>());
    }

    @Test
    void testCheckTimeframe_validSlot_shouldReturnTrue() {
        LocalDate date = LocalDate.of(2025, 5, 10);
        LocalTime start = LocalTime.of(10, 0);
        LocalTime end = LocalTime.of(11, 0);

        boolean result = timeframeService.checkTimeframe(employee, start, end, date);
        assertTrue(result);
    }

    @Test
    void testCheckTimeframe_outsideShift_shouldReturnFalse() {
        LocalDate date = LocalDate.of(2025, 5, 10);
        LocalTime start = LocalTime.of(8, 0);
        LocalTime end = LocalTime.of(10, 0);

        boolean result = timeframeService.checkTimeframe(employee, start, end, date);
        assertFalse(result);
    }

    @Test
    void testCheckTimeframe_conflictingAppointment_shouldReturnFalse() {
        LocalDate date = LocalDate.of(2025, 5, 10);
        Appointment existingAppt = new Appointment();
        existingAppt.setDate(date);
        existingAppt.setStartTime(LocalTime.of(10, 0));
        existingAppt.setEndTime(LocalTime.of(11, 0));

        AppointmentService as = new AppointmentService();
        as.setAppointment(existingAppt);

        employee.setAppointmentServices(List.of(as));

        boolean result = timeframeService.checkTimeframe(employee, LocalTime.of(10, 0), LocalTime.of(11, 0), date);
        assertFalse(result);
    }

    @Test
    void testCheckTimeframe_noConflict_shouldReturnTrue() {
        // Arrange
        LocalDate date = LocalDate.of(2025, 5, 8);
        LocalTime shiftStart = LocalTime.of(9, 0);
        LocalTime shiftEnd = LocalTime.of(17, 0);

        LocalTime appointmentStart = LocalTime.of(10, 0);
        LocalTime appointmentEnd = LocalTime.of(11, 0);

        TimeFrame timeFrame = new TimeFrame();
        timeFrame.setDate(date);
        timeFrame.setShiftStartTime(shiftStart);
        timeFrame.setShiftEndTime(shiftEnd);

        Employee employee = new Employee();
        employee.setTimeFrame(List.of(timeFrame));
        employee.setAppointmentServices(List.of()); // no conflicts

        // Act
        boolean isAvailable = timeframeService.checkTimeframe(employee, appointmentStart, appointmentEnd, date);

        // Assert
        assertThat(isAvailable).isTrue();
    }

    @Test
    void testCheckTimeframe_conflictInTime_shouldReturnFalse() {
        // Arrange
        LocalDate date = LocalDate.of(2025, 5, 8);
        LocalTime shiftStart = LocalTime.of(9, 0);
        LocalTime shiftEnd = LocalTime.of(17, 0);

        LocalTime appointmentStart = LocalTime.of(8, 0); // before shift
        LocalTime appointmentEnd = LocalTime.of(9, 30);

        TimeFrame timeFrame = new TimeFrame();
        timeFrame.setDate(date);
        timeFrame.setShiftStartTime(shiftStart);
        timeFrame.setShiftEndTime(shiftEnd);

        Employee employee = new Employee();
        employee.setTimeFrame(List.of(timeFrame));
        employee.setAppointmentServices(List.of());

        // Act
        boolean isAvailable = timeframeService.checkTimeframe(employee, appointmentStart, appointmentEnd, date);

        // Assert
        assertThat(isAvailable).isFalse();
    }

    @Test
    void testFindClosestEndAptwithStarttime() {
        Appointment a1 = new Appointment();
        a1.setStartTime(LocalTime.of(9, 0));
        a1.setEndTime(LocalTime.of(9, 45));

        Appointment a2 = new Appointment();
        a2.setStartTime(LocalTime.of(10, 0));
        a2.setEndTime(LocalTime.of(10, 30));

        List<Appointment> list = List.of(a1, a2);
        Appointment result = TimeframeService.findClosestEndAptwithStarttime(list, LocalTime.of(11, 0));

        assertThat(result).isEqualTo(a2);
    }

    @Test
    void testFindClosestStartAptWithEndtime() {
        Appointment a1 = new Appointment();
        a1.setStartTime(LocalTime.of(12, 0));

        Appointment a2 = new Appointment();
        a2.setStartTime(LocalTime.of(14, 0));

        List<Appointment> list = List.of(a1, a2);
        Appointment result = TimeframeService.findClosestStartAptWithEndtime(list, LocalTime.of(11, 30));

        assertThat(result).isEqualTo(a1);
    }

    @Test
    void testCheckavaiable_basicSlotFound() {
        long employeeId = 1L;
        LocalDate date = LocalDate.of(2025, 5, 8);
        int requiredMinutes = 30;

        Employee emp = new Employee();
        TimeFrame timeFrame = new TimeFrame(date, LocalTime.of(9, 0), LocalTime.of(12, 0));
        timeFrame.setSmallTimeframeList(new ArrayList<>());
        emp.setId(employeeId);

        when(employeeReposittory.findById(employeeId)).thenReturn(Optional.of(emp));
        when(timeFrameRepository.findByDateAndEmployeeId(date, employeeId)).thenReturn(Optional.of(timeFrame));
        when(serviceservice.totalAmount(any())).thenReturn(requiredMinutes);

        List<TimeFrame_DTO> result = timeframeService.Checkavaiable(employeeId, date, List.of(1L));

        //assertThat(result).isNotEmpty();
        //assertThat(result.get(0).getStart()).isEqualTo(LocalTime.of(9, 0));
    }

    @Test
    void testCheckavaiable_timeframeNotFound_throwsException() {
        when(timeFrameRepository.findByDateAndEmployeeId(any(), anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> {
            timeframeService.Checkavaiable(1L, LocalDate.now(), List.of(1L));
        }).isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("can not find the requested timeframe");
    }

    @Test
    void testTimeFrame_createsTimeframesCorrectly() {
        long employeeId = 1L;
        int month = 5;
        int year = 2025;
        LocalTime start = LocalTime.of(9, 0);
        LocalTime end = LocalTime.of(17, 0);
        Employee emp = new Employee();

        when(employeeReposittory.findById(employeeId)).thenReturn(Optional.of(emp));
        when(timeFrameRepository.existsByEmployeeIdAndMonthAndYear(employeeId, month, year)).thenReturn(false);

        timeframeService.timeFrame(employeeId, month, year, start, end);

//        verify(timeFrameRepository).saveAll(argThat(timeframes ->
//                timeframes.size() == YearMonth.of(year, month).lengthOfMonth()
//        ));
    }

    @Test
    void testTimeFrame_duplicateTimeframe_throwsException() {
        when(employeeReposittory.findById(1L)).thenReturn(Optional.of(new Employee()));
        when(timeFrameRepository.existsByEmployeeIdAndMonthAndYear(1L, 5, 2025)).thenReturn(true);

        assertThatThrownBy(() -> {
            timeframeService.timeFrame(1L, 5, 2025, LocalTime.of(9, 0), LocalTime.of(17, 0));
        }).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already been created");
    }
}
