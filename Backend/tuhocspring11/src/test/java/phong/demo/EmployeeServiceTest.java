package phong.demo;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import phong.demo.DTO.Service_DTO;
import phong.demo.Entity.Employee;
import phong.demo.Entity.Handler;
import phong.demo.Entity.Service;
import phong.demo.Repository.EmployeeReposittory;
import phong.demo.Service.EmployeeService;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class) // ✅ Use Mockito test runner
public class EmployeeServiceTest {

    @Mock
    private EmployeeReposittory employeeReposittory;

    @InjectMocks
    private EmployeeService employeeService; // ✅ Auto injects the mock repo

//    @Test
//    public void testGetAllEmployeename() {
//        Employee employee1 = mock(Employee.class);
//        when(employee1.getUsername()).thenReturn("Alice");
//
//        Employee employee2 = mock(Employee.class);
//        when(employee2.getUsername()).thenReturn("Bob");
//
//        when(employeeReposittory.findAll()).thenReturn(List.of(employee1, employee2));
//
//        //List<String> names = employeeService.getAllEmployeename();
//
//        //assertEquals(List.of(null), names);
//    }
//
//    @Test
//    public void testGetEmployeeAndService() {
//        Service service = new Service();
//        service.setService_name("Manicure");
//        service.setPrice(25);
//        service.setDuration(30);
//
//        Handler handler = new Handler();
//        handler.setService(service);
//
//        Employee employee = new Employee();
//        employee.setUsername("Charlie");
//        employee.setEmployee_Handler(List.of(handler));
//
//        when(employeeReposittory.findAll()).thenReturn(List.of(employee));
//
//        //Map<String, List<Service_DTO>> result = employeeService.getEmployeeAndService();
//
////        assertEquals(nullable());
////        assertEquals("Charlie", result.keySet().iterator().next());
////        Service_DTO dto = result.get("Charlie").get(0);
////        assertEquals("Manicure", dto.getService_name());
////        assertEquals(25, dto.getPrice());
////        assertEquals(30, dto.getDuration());
//    }

    @Test
    void testGetAllEmployeename1() {
        // Arrange
        Employee e1 = new Employee();
        e1.setUsername("Alice");

        Employee e2 = new Employee();
        e2.setUsername("Bob");

        when(employeeReposittory.findAll()).thenReturn(Arrays.asList(e1, e2));

        // Act
        List<String> names = employeeService.getAllEmployeename();

        // Assert
        assertThat(names).containsExactlyInAnyOrder("Alice", "Bob");
    }

    @Test
    void testGetEmployeeAndService1() {
        // Arrange
        phong.demo.Entity.Service s1 = new phong.demo.Entity.Service();
        s1.setService_name("Polish Change");
        s1.setPrice(100);
        s1.setDuration(45);

        Handler h1 = new Handler();
        h1.setService(s1);

        Employee emp = new Employee();
        emp.setUsername("Nu");
        emp.setEmployee_Handler(List.of(h1));

        when(employeeReposittory.findAll()).thenReturn(List.of(emp));

        // Act
        Map<String, List<Service_DTO>> result = employeeService.getEmployeeAndService();

        // Assert
        assertThat(result).containsKey("Nu");
        assertThat(result.get("Nu")).hasSize(1);
        assertThat(result.get("Nu").get(0).getService_name()).isEqualTo("Polish Change");
    }

}
