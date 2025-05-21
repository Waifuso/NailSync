package phong.demo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.ResponseEntity;
import phong.demo.Controller.Employee_controller;
import phong.demo.DTO.*;
import phong.demo.Entity.*;
import phong.demo.Repository.EmployeeReposittory;
import phong.demo.Repository.ServiceRepository;
import phong.demo.Repository.TimeFrameRepository;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class EmployeeControllerTest {

    @Mock private EmployeeReposittory employeeReposittory;
    @Mock private ServiceRepository serviceRepository;
    @Mock private TimeFrameRepository timeFrameRepository;

    @InjectMocks
    private Employee_controller controller;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    // ✅ /info/name
    @Test
    void testGetEmployeeInfo() {
        Employee e1 = new Employee(); e1.setId(1L); e1.setUsername("john");
        Employee e2 = new Employee(); e2.setId(2L); e2.setUsername("jane");

        when(employeeReposittory.findAll()).thenReturn(List.of(e1, e2));

        JSon_Object<List<Json_object_withID>> response = controller.Employeeinfo();

        //assertThat().hasSize(2);
        //assertThat(response.getList().get(0).getMess()).isEqualTo("john");
    }

    // ✅ Find/service/{username}
    @Test
    void testFindByUsername_EmployeeFoundWithService() {
        Employee employee = new Employee(); employee.setEmployee_id(1L); employee.setUsername("john");

        Handler handler = new Handler();
        Service service = new Service(); service.setService_name("Nail"); service.setPrice(30); service.setDuration(60);
        handler.setService(service); handler.setEmployee(employee);

        employee.setEmployee_Handler(List.of(handler));

        when(employeeReposittory.findByUsername("john")).thenReturn(Optional.of(employee));

        ResponseEntity<?> response = controller.findByUsername("john");
        List<?> result = (List<?>) response.getBody();

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(result).hasSize(1);
    }

    // ✅ /Create/modify
//    @Test
//    void testCreateEmployee_Success() {
//        Employee_CreateRequest req = new Employee_CreateRequest();
//        req.setUser_name("kathy");
//        req.setDob(LocalDate.of(1990, 1, 1));
//        //req.setService(List.of(new Service_DTO("Haircut", 20, 30)));
//
//        when(employeeReposittory.existsByUsername("kathy")).thenReturn(false);
//        when(serviceRepository.findByServiceName("Haircut")).thenReturn(Optional.empty());
//        when(serviceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
//        when(employeeReposittory.save(any())).thenAnswer(inv -> inv.getArgument(0));
//
//        ResponseEntity<?> response = controller.Create_Employee_ver2(req);
//
//        assertThat(response.getStatusCodeValue()).isEqualTo(200);
//        //assertThat(((JSOn_objectString) response.getBody()).getMess()).contains("success");
//    }

//    @Test
//    void testCreateEmployee_UsernameExists() {
//        Employee_CreateRequest req = new Employee_CreateRequest("kathy", "");
//        req.setUser_name("kathy");
//
//        when(employeeReposittory.existsByUsername("kathy")).thenReturn(true);
//
//        ResponseEntity<?> response = controller.Create_Employee_ver2(req);
//        assertThat(response.getStatusCodeValue()).isEqualTo(400);
//    }

    // ✅ POST /{username}/service
    @Test
    void testAddServiceToEmployee_Success() {
        Employee employee = new Employee();
        employee.setUsername("john");
        employee.setEmployee_Handler(new ArrayList<>());

        Service_DTO serviceDTO = new Service_DTO("Massage", 50, 45);

        when(employeeReposittory.findByUsername("john")).thenReturn(Optional.of(employee));
        when(serviceRepository.findByServiceName("Massage")).thenReturn(Optional.empty());
        when(serviceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(employeeReposittory.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ResponseEntity<?> response = controller.addServiceToEmployee("john", serviceDTO);

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        //assertThat(((JSOn_objectString) response.getBody()).getMess()).contains("success");
    }

    // ✅ PUT /edit/{id}
//    @Test
//    void testUpdateEmployee_Success() {
//        Employee emp = new Employee(); emp.setId(1L); emp.setUsername("john"); emp.setServicePassword("oldpass");
//
//        Update_Employee__DTO dto = new Update_Employee__DTO();
//        dto.setUsername("johnny");
//        dto.setServicePassword("newpass");
//
//        when(employeeReposittory.findById(1L)).thenReturn(Optional.of(emp));
//        when(employeeReposittory.existsByUsername("johnny")).thenReturn(false);
//
//        ResponseEntity<?> response = controller.updateEmployee(1L, dto);
//
//        assertThat(response.getStatusCodeValue()).isEqualTo(200);
//    }

//    @Test
//    void testUpdateEmployee_SamePasswordRejected() {
//        Employee emp = new Employee(); emp.setServicePassword("same");
//
//        Update_Employee__DTO dto = new Update_Employee__DTO();
//        dto.setServicePassword("same");
//
//        when(employeeReposittory.findById(1L)).thenReturn(Optional.of(emp));
//
//        ResponseEntity<?> response = controller.updateEmployee(1L, dto);
//        assertThat(response.getStatusCodeValue()).isEqualTo(400);
//    }

    // ✅ DELETE /{id}
    @Test
    void testDeleteEmployee_Success() {
        Employee employee = new Employee(); employee.setId(1L);

        when(employeeReposittory.findById(1L)).thenReturn(Optional.of(employee));

        ResponseEntity<?> response = controller.deleteEmployee(1L);

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        //assertThat(((JSOn_objectString) response.getBody()).getMess()).contains("deleted");
    }

    @Test
    void testDeleteEmployee_NotFound() {
        when(employeeReposittory.findById(999L)).thenReturn(Optional.empty());

        ResponseEntity<?> response = controller.deleteEmployee(999L);
        assertThat(response.getStatusCodeValue()).isEqualTo(404);
    }

}
