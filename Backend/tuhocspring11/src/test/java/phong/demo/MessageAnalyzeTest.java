package phong.demo;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import phong.demo.DTO.Service_DTO;
import phong.demo.Entity.User;
import phong.demo.Repository.UserRepository;
import phong.demo.Service.EmployeeService;
import phong.demo.Service.MessageAnalyze;
import phong.demo.Service.Serviceservice;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class MessageAnalyzeTest {

    @Mock
    private EmployeeService employeeService;

    @Mock
    private Serviceservice serviceservice;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MessageAnalyze messageAnalyze;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void analyze_shouldReturnServiceAndEmployeeInfo_WhenBothMentioned() {
        List<String> employees = List.of("Alice");
        List<String> services = List.of("manicure");
        Map<String, List<Service_DTO>> employeeAndService = Map.of(
                "Alice", List.of(new Service_DTO("manicure", 20, 30))
        );

        when(employeeService.getAllEmployeename()).thenReturn(employees);
        when(serviceservice.getServicenameList()).thenReturn(services);
        when(employeeService.getEmployeeAndService()).thenReturn(employeeAndService);
        when(serviceservice.getServiceAndEmployee()).thenReturn(Map.of("manicure", List.of("Alice")));

        String response = messageAnalyze.Analyze("What service does Alice offer?");
        assertTrue(response.contains("Alice has those service: manicure"));
    }

    @Test
    void analyze_shouldReturnServiceList_WhenOnlyServiceMentioned() {
        List<String> employees = List.of("Alice");
        List<String> services = List.of("pedicure");
        Map<String, List<Service_DTO>> employeeAndService = Map.of(
                "Alice", List.of(new Service_DTO("pedicure", 25, 30))
        );

        when(employeeService.getAllEmployeename()).thenReturn(employees);
        when(serviceservice.getServicenameList()).thenReturn(services);
        when(employeeService.getEmployeeAndService()).thenReturn(employeeAndService);
        when(serviceservice.getServiceAndEmployee()).thenReturn(Map.of("pedicure", List.of("Alice")));
        when(serviceservice.getServiceList()).thenReturn(List.of(new Service_DTO("pedicure", 25, 30)));

        String response = messageAnalyze.Analyze("Can you tell me your services?");
        assertTrue(response.contains("list of service we have"));
    }

    @Test
    void analyze_shouldReturnEmployeeList_WhenOnlyEmployeeMentioned() {
        List<String> employees = List.of("Alice");
        List<String> services = List.of();

        when(employeeService.getAllEmployeename()).thenReturn(employees);
        when(serviceservice.getServicenameList()).thenReturn(services);

        String response = messageAnalyze.Analyze("I want to see Alice");
        assertTrue(response.contains("list of employee we have"));
    }

    @Test
    void analyze_shouldReturnDefaultMessage_WhenNeitherMentioned() {
        List<String> employees = List.of("Alice");
        List<String> services = List.of("manicure");

        when(employeeService.getAllEmployeename()).thenReturn(employees);
        when(serviceservice.getServicenameList()).thenReturn(services);

        String response = messageAnalyze.Analyze("How do I book?");
        assertTrue(response.contains("ask about appoinment time"));
    }

    @Test
    void extraInformationUser_shouldReturnCustomerDetails() {
        User mockUser = new User();
        mockUser.setUsername("Bob");
        phong.demo.Entity.Profile profile = new phong.demo.Entity.Profile();
        profile.setRanking(phong.demo.Entity.Ranking.GOLD);
        profile.setTotalPoints(500);
        mockUser.setProfile(profile);
        mockUser.setAppointmentList(List.of());

        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

        String response = messageAnalyze.extraInformationUser(1L);
        assertTrue(response.contains("Bob"));
        assertTrue(response.contains("GOLD"));
    }
}

