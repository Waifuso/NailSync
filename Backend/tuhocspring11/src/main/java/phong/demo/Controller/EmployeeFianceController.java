package phong.demo.Controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.MessagingException;
import org.springframework.web.bind.annotation.*;
import phong.demo.DTO.JSOn_objectString;
import phong.demo.DTO.JSon_Object;
import phong.demo.Entity.DailyFinance;
import phong.demo.Entity.Employee;
import phong.demo.Entity.EmployeeDailyFinance;
import phong.demo.Entity.Payment;
import phong.demo.Repository.EmployeeDailyFinanceRepository;
import phong.demo.Repository.EmployeeReposittory;
import phong.demo.Repository.PaymentRepository;
import phong.demo.Service.TimeframeService;
import phong.demo.Springpro.SendEmailService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/finance")
public class EmployeeFianceController {


    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private EmployeeReposittory employeeReposittory;

    @Autowired
    private EmployeeDailyFinanceRepository employeeDailyFinanceRepository;
    private SendEmailService sendEmailService;

    public EmployeeFianceController(PaymentRepository paymentRepository, EmployeeReposittory employeeReposittory, EmployeeDailyFinanceRepository employeeDailyFinanceRepository, SendEmailService sendEmailService) {
        this.paymentRepository = paymentRepository;
        this.employeeReposittory = employeeReposittory;
        this.employeeDailyFinanceRepository = employeeDailyFinanceRepository;
        this.sendEmailService = sendEmailService;
    }

    private final Logger logger = LoggerFactory.getLogger(EmployeeFianceController.class);


    @GetMapping("/get/daily/income")
    public ResponseEntity<?> employeeFinance(@RequestParam long id, @RequestParam LocalDate localDate){

        if (localDate == null){

            return ResponseEntity.badRequest().body(new JSOn_objectString(" the date is invalid "));
        }

        Optional<Employee> optionalEmployee = employeeReposittory.findById(id);

        if (optionalEmployee.isEmpty()){

            return ResponseEntity.badRequest().body(new JSOn_objectString("The employee does not exist"));
        }

        Employee employee = optionalEmployee.get();

        if (employeeDailyFinanceRepository.existsByDate(localDate)){

            Optional<EmployeeDailyFinance> employeeDailyFinance = employeeDailyFinanceRepository.findByDate(localDate);

            if(employeeDailyFinance.isPresent()) {

                EmployeeDailyFinance employeeDailyFinance1 = employeeDailyFinance.get();

                try {
                    try {
                        sendEmailService.sendEmployeeDailyIncomeEmail(
                                employee.getEmail(),
                                "NAILSYNC DAILY EMPLOYEE INCOME",
                                employeeDailyFinance1.getIncome(), employeeDailyFinance1.getTip(), employeeDailyFinance1.getTotalincome(), localDate
                        );
                    } catch (jakarta.mail.MessagingException e) {
                        throw new RuntimeException(e);
                    }
                } catch (MessagingException e) {
                    e.printStackTrace();
                }
            }

            return ResponseEntity.ok().body(new JSon_Object<>("The old report is on its way"));



        }


        List<Payment> paymentList = paymentRepository.findAllByDateAndEmployeeId(localDate,id);

        if (paymentList.isEmpty()){
            return ResponseEntity.ok(new JSon_Object<>(0));
        }

        double totalEarn = 0;

        double income = 0;


        double totalTtip = 0;


        for (Payment payment: paymentList){

            income+= payment.getAmount();

            totalTtip += payment.getTipping();

        }

        totalEarn = income + totalTtip;

        EmployeeDailyFinance employeeDailyFinance = new EmployeeDailyFinance(localDate,totalEarn,income,totalTtip,employee);

        employeeDailyFinanceRepository.save(employeeDailyFinance);


        try {
            try {
                sendEmailService.sendEmployeeDailyIncomeEmail(
                        employee.getEmail(),
                        "NAILSYNC DAILY EMPLOYEE INCOME",
                        income, totalTtip, totalEarn, localDate
                );
            } catch (jakarta.mail.MessagingException e) {
                throw new RuntimeException(e);
            }
        } catch (MessagingException e) {
            e.printStackTrace();
        }

        return  ResponseEntity.ok(new JSOn_objectString("Successfully save the income"));
    }





}
