package phong.demo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.cglib.core.Local;
import org.springframework.http.ResponseEntity;
import phong.demo.Controller.TimeframeController;
import phong.demo.DTO.*;
import phong.demo.Entity.*;
import phong.demo.Repository.EmployeeReposittory;
import phong.demo.Repository.TimeFrameRepository;
import phong.demo.Service.Serviceservice;
import phong.demo.Service.TimeframeService;
import phong.demo.Springpro.ResponseMessagge;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class TimeframeControllerTest {

    @Mock private TimeframeService timeFrameService;
    @Mock private TimeFrameRepository timeFrameRepository;
    @Mock private EmployeeReposittory employeeReposittory;
    @Mock private Serviceservice serviceservice;

    @InjectMocks
    private TimeframeController controller;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    // ✅ GET /get/appointment/employee
    @Test
    void testAvailableTimeslot_Found() {
        LocalDate date = LocalDate.of(2025, 5, 20);
        TimeFrame tf = new TimeFrame();
        tf.setSmallTimeframeList(List.of(new SmallTimeframe(), new SmallTimeframe()));

        when(timeFrameRepository.findByDateAndEmployeeId(date, 1L)).thenReturn(Optional.of(tf));

        ResponseEntity<?> response = controller.availableTimeslot(1L, date);

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(((List<?>) response.getBody())).hasSize(2);
    }

    @Test
    void testAvailableTimeslot_NotFound() {
        LocalDate date = LocalDate.of(2025, 5, 20);
        when(timeFrameRepository.findByDateAndEmployeeId(date, 1L)).thenReturn(Optional.empty());

        ResponseEntity<?> response = controller.availableTimeslot(1L, date);

        assertThat(response.getStatusCodeValue()).isEqualTo(400);
        //assertThat(((JSOn_objectString) response.getBody()).getMess()).contains("can not find");
    }

    // ✅ GET /{date}/{time}/{employeeId}
    @Test
    void testServicesInAppointment_Found() {
        when(serviceservice.returnService(any(), any(), anyLong()))
                .thenReturn(List.of(new Service_DTO("Hair", 30, 45)));

        ResponseEntity<?> response = controller.servicesInAppointment(LocalDate.now(), LocalTime.NOON, 1L);

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        //assertThat(((JSon_Object<?>) response.getBody()).getList()).hasSize(1);
    }

    @Test
    void testServicesInAppointment_NotFound() {
        when(serviceservice.returnService(any(), any(), anyLong())).thenReturn(Collections.emptyList());

        ResponseEntity<?> response = controller.servicesInAppointment(LocalDate.now(), LocalTime.NOON, 1L);

        assertThat(response.getStatusCodeValue()).isEqualTo(400);
        assertThat(((ResponseMessagge) response.getBody()).getMessage()).contains("No services found");
    }

    // ✅ POST /generate
    @Test
    void testGenerateTimeFrames_Success() {
        ResponseEntity<?> response = controller.generateTimeFrames(1L, 4, 2025,
                LocalTime.of(9, 0), LocalTime.of(17, 30));

        verify(timeFrameService).timeFrame(eq(1L), eq(4), eq(2025), any(), any());
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
    }

    @Test
    void testGenerateTimeFrames_IllegalArgument() {
        doThrow(new IllegalArgumentException("Invalid input"))
                .when(timeFrameService).timeFrame(anyLong(), anyInt(), anyInt(), any(), any());

        ResponseEntity<?> response = controller.generateTimeFrames(1L, 4, 2025,
                LocalTime.of(9, 0), LocalTime.of(17, 30));

        assertThat(response.getStatusCodeValue()).isEqualTo(400);
    }

    // ✅ POST /employee/{id}
    @Test
    void testCreateTimeframe_Success() {
        TimeFrame tf = new TimeFrame();
        Employee emp = new Employee();
        emp.setTimeFrame(new ArrayList<>());

        when(employeeReposittory.findById(1L)).thenReturn(Optional.of(emp));
        when(timeFrameRepository.save(any())).thenReturn(tf);
        when(employeeReposittory.save(any())).thenReturn(emp);

        ResponseEntity<?> response = controller.createTimeframe(1L, tf);

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(" The requested timeframe has already been created");
    }

    // ✅ POST /checkAvailability
    @Test
    void testCheckAvailability_ReturnsSlots() {
        ServiceIdsDTO dto = new ServiceIdsDTO(List.of(1L, 2L));
        LocalTime start = LocalTime.parse("07:00");
        LocalTime end = LocalTime.parse("09:00");
        List<TimeFrame_DTO> slots = List.of(new TimeFrame_DTO(start, end));

        when(timeFrameService.Checkavaiable(anyLong(), any(), any())).thenReturn(slots);

        ResponseEntity<?> response = controller.checkAvailability(1L, LocalDate.now(), dto);

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        //assertThat(((TimeFrameSlotDTO) response.getBody()).getTimeFrameList()).hasSize(1);
    }

    // ✅ PUT /modify/{id}
    @Test
    void testModifyTimeframe_Success() {
        TimeFrame requestTf = new TimeFrame();
        requestTf.setDate(LocalDate.now());
        requestTf.setShiftStartTime(LocalTime.of(10, 0));
        requestTf.setShiftEndTime(LocalTime.of(17, 0));

        Employee emp = new Employee(); emp.setEmployee_id(1L);
        TimeFrame existingTf = new TimeFrame();

        when(employeeReposittory.findById(1L)).thenReturn(Optional.of(emp));
        when(timeFrameRepository.findByDateAndEmployeeId(requestTf.getDate(), 1L))
                .thenReturn(Optional.of(existingTf));

        ResponseEntity<?> response = controller.TimeframeModify(1L, requestTf);

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        //assertThat(((JSOn_objectString) response.getBody()).getMess()).contains("modify success");
    }

}
