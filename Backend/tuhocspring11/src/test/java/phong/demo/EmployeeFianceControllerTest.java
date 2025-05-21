package phong.demo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.ResponseEntity;
import phong.demo.Controller.EmployeeFianceController;
import phong.demo.DTO.JSOn_objectString;
import phong.demo.DTO.JSon_Object;
import phong.demo.Entity.*;
import phong.demo.Repository.*;
import phong.demo.Springpro.SendEmailService;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class EmployeeFianceControllerTest {

    @Mock private PaymentRepository paymentRepository;
    @Mock private EmployeeReposittory employeeReposittory;
    @Mock private EmployeeDailyFinanceRepository employeeDailyFinanceRepository;
    @Mock private SendEmailService sendEmailService;

    @InjectMocks
    private EmployeeFianceController controller;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    // ✅ Case 1: Invalid Date
    @Test
    void testEmployeeFinance_InvalidDate() {
        ResponseEntity<?> response = controller.employeeFinance(1L, null);
        assertThat(response.getStatusCodeValue()).isEqualTo(400);
        //assertThat(((JSOn_objectString) response.getBody()).getMess()).contains("invalid");
    }

    // ✅ Case 2: Employee not found
    @Test
    void testEmployeeFinance_EmployeeNotFound() {
        when(employeeReposittory.findById(1L)).thenReturn(Optional.empty());

        ResponseEntity<?> response = controller.employeeFinance(1L, LocalDate.now());
        assertThat(response.getStatusCodeValue()).isEqualTo(400);
        //assertThat(((JSOn_objectString) response.getBody()).getMess()).contains("does not exist");
    }

    // ✅ Case 3: Employee has old report
    @Test
    void testEmployeeFinance_OldReportExists() {
        LocalDate date = LocalDate.now();
        Employee employee = new Employee(); employee.setEmail("employee@mail.com");

        EmployeeDailyFinance report = new EmployeeDailyFinance(date, 100, 80, 20, employee);

        when(employeeReposittory.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeDailyFinanceRepository.existsByDate(date)).thenReturn(true);
        when(employeeDailyFinanceRepository.findByDate(date)).thenReturn(Optional.of(report));

        ResponseEntity<?> response = controller.employeeFinance(1L, date);

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        //assertThat(((JSon_Object<?>) response.getBody()).getList()).contains("The old report is on its way");
    }

    // ✅ Case 4: No payments on that day
    @Test
    void testEmployeeFinance_NoPayments() {
        LocalDate date = LocalDate.now();
        Employee employee = new Employee(); employee.setEmail("e@x.com");

        when(employeeReposittory.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeDailyFinanceRepository.existsByDate(date)).thenReturn(false);
        when(paymentRepository.findAllByDateAndEmployeeId(date, 1L)).thenReturn(Collections.emptyList());

        ResponseEntity<?> response = controller.employeeFinance(1L, date);

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        //assertThat(((JSon_Object<?>) response.getBody()).getList()).contains(0);
    }

    // ✅ Case 5: Calculate and save new report
    @Test
    void testEmployeeFinance_CalculateAndSaveReport() {
        LocalDate date = LocalDate.of(2025, 5, 8);
        Employee employee = new Employee(); employee.setEmail("abc@mail.com");

        Payment p1 = new Payment(); p1.setAmount(100); p1.setTipping(10);
        Payment p2 = new Payment(); p2.setAmount(200); p2.setTipping(20);

        List<Payment> payments = List.of(p1, p2);

        when(employeeReposittory.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeDailyFinanceRepository.existsByDate(date)).thenReturn(false);
        when(paymentRepository.findAllByDateAndEmployeeId(date, 1L)).thenReturn(payments);
        when(employeeDailyFinanceRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        ResponseEntity<?> response = controller.employeeFinance(1L, date);

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        //assertThat(((JSOn_objectString) response.getBody()).getMess()).contains("Successfully save");
        verify(employeeDailyFinanceRepository).save(any(EmployeeDailyFinance.class));
    }
}

