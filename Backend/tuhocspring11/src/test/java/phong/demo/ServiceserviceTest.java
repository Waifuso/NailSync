package phong.demo;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import phong.demo.DTO.Service_DTO;
import phong.demo.Entity.Appointment;
import phong.demo.Entity.AppointmentService;
import phong.demo.Entity.Handler;
import phong.demo.Entity.Service;
import phong.demo.Repository.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.mockito.MockitoAnnotations;
import phong.demo.Service.Serviceservice;

public class ServiceserviceTest {

    @Mock
    private EmployeeReposittory employeeReposittory;

    @Mock
    private ServiceRepository serviceRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private AppointmentServiceRepository appointmentServiceRepository;

    @InjectMocks
    private Serviceservice serviceservice;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        serviceservice = new Serviceservice(employeeReposittory, serviceRepository, appointmentRepository, appointmentServiceRepository);
    }

    @Test
    void testTotalAmount_success() {
        Service s1 = new Service();
        s1.setDuration(30);
        Service s2 = new Service();
        s2.setDuration(45);
        List<Service> mockServices = Arrays.asList(s1, s2);
        when(serviceRepository.findAllById(Arrays.asList(1L, 2L))).thenReturn(mockServices);

        Integer result = serviceservice.totalAmount(Arrays.asList(1L, 2L));

        assertEquals(75, result);
    }

    @Test
    void testReturnService_emptyAppointment() {
        when(appointmentRepository.findByDateAndStartTime(any(), any())).thenReturn(Optional.empty());
        List<Service_DTO> services = serviceservice.returnService(LocalDate.now(), LocalTime.NOON, 1L);
        assertTrue(services.isEmpty());
    }

    @Test
    void testGetServiceNameList() {
        Service s = new Service();
        s.setService_name("Nail Polish");
        when(serviceRepository.findAll()).thenReturn(List.of(s));
        List<String> result = serviceservice.getServicenameList();
        assertEquals(List.of("Nail Polish"), result);
    }

    @Test
    void testGetServiceList() {
        Service s = new Service();
        s.setService_name("Gel");
        s.setPrice(40);
        s.setDuration(30);
        when(serviceRepository.findAll()).thenReturn(List.of(s));
        List<Service_DTO> list = serviceservice.getServiceList();
        assertEquals(1, list.size());
        assertEquals("Gel", list.get(0).getService_name());
        assertEquals(40, list.get(0).getPrice());
        assertEquals(30, list.get(0).getDuration());
    }
}
