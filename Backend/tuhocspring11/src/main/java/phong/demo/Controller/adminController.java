package phong.demo.Controller;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.MessagingException;
import org.springframework.web.bind.annotation.*;
import phong.demo.DTO.JSOn_objectString;
import phong.demo.DTO.JSon_Object;
import phong.demo.Entity.*;
import phong.demo.Repository.*;
import phong.demo.Service.DailyFinanceService;
import phong.demo.Service.MessageAnalyze;
import phong.demo.Springpro.SendEmailService;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/api/admin")
public class adminController {

    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private DailyfinanceRepository dailyfinanceRepository;

    private SendEmailService sendEmailService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DailyFinanceService dailyFinanceService;

    @Autowired
    private AppointmentRepository appointmentRepository;

    private final Logger logger = LoggerFactory.getLogger(adminController.class);

    @Autowired
    private EmployeeReposittory employeeReposittory;

    public adminController(PaymentRepository paymentRepository, SendEmailService sendEmailService) {
        this.paymentRepository = paymentRepository;
        this.sendEmailService = sendEmailService;
    }

    /**
     * the code for sending the daily report via the email
     * @param localDate
     * @return the string whether the email has sent succesfully or not
     */

    @GetMapping("/total/income/viaEmail")
    public ResponseEntity<?>SendReport(@RequestParam LocalDate localDate){


        if (localDate == null){

            return ResponseEntity.badRequest().build();
        }

        String email = "jacobtdang@gmail.com";

        if (dailyfinanceRepository.existsByDate(localDate)){

            Optional<DailyFinance> dailyFinanceOptional = dailyfinanceRepository.findByDate(localDate);

            if(dailyFinanceOptional.isPresent()) {

                DailyFinance dailyFinance = dailyFinanceOptional.get();


                try {
                    try {
                        sendEmailService.dailyReport(email
                                ,
                                "Your NAILSYNC Old Daily Report",
                                dailyFinance.getTotal()
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

        Double totalIncome =  dailyFinanceService.getFinance(localDate);



        try {
            try {
                sendEmailService.dailyReport(
                        email,
                        "Your NAILSYNC Confirmation Code",
                        totalIncome
                );
            } catch (jakarta.mail.MessagingException e) {
                throw new RuntimeException(e);
            }
        } catch (MessagingException e) {
            e.printStackTrace();
        }

        return  ResponseEntity.ok(totalIncome);
    }


    /**
     * @param localDate
     * @return the total of the income
     */
    @GetMapping("finance")
    public ResponseEntity<?> getFinance(@RequestParam LocalDate localDate){

        Double total = dailyFinanceService.getFinance(localDate);

        DailyFinance dailyFinance = new DailyFinance(localDate,total);

        dailyfinanceRepository.save(dailyFinance);

        return ResponseEntity.ok(new JSon_Object<Double>(total));
    }

    /**
     * @return the total amount of user the store currently have.
     */
    @GetMapping("usercount")
    public ResponseEntity<?>getNumberofUser(){

        return ResponseEntity.ok(new JSon_Object<Long>(userRepository.findAll().stream().count()));


    }

    /**
     * @return the total amount of employee the store currently have
     */
    @GetMapping("employee")
    public ResponseEntity<?>getNumberofEmployee(){

        return ResponseEntity.ok(new JSon_Object<Long>(employeeReposittory.findAll().stream().count()));


    }

    /**
     *
     * @param localDate
     * @return how many appointment within the date
     */
    @GetMapping("appointmentCount")
    public  ResponseEntity<?>getNumberofAppointment(LocalDate localDate){

        return ResponseEntity.ok(new JSon_Object<Long>(appointmentRepository.findByDate(localDate).stream().count()));
    }

    @GetMapping("/allemployee/allappointment")
    public ResponseEntity<?>GetAllAppointment (LocalDate localDate){

        List<Employee> employees = employeeReposittory.findAll();

        Map<String,List<SmallTimeframe>> employeeAndappointmenttimeHashMap = new HashMap<>();

        for (Employee employee:employees){

            List<SmallTimeframe> smallTimeframes = new ArrayList<>();

           String name  = employee.getUsername();

            TimeFrame timeFrame = employee.getTimeFrame().stream().filter(T->T.getDate().equals(localDate)).findFirst().orElse(null);

            if (timeFrame != null){
                smallTimeframes = timeFrame.getSmallTimeframeList();

            }
            employeeAndappointmenttimeHashMap.put(name,smallTimeframes);

        }


        return ResponseEntity.ok(new JSon_Object<Map<String,List<SmallTimeframe>>>(employeeAndappointmenttimeHashMap));
    }

    @GetMapping("/week/revenue")
    public ResponseEntity<?> weekrevenue(){
        ZoneId iowaZone = ZoneId.of("America/Chicago");
        LocalDate today = LocalDate.now(iowaZone);
        List<Double> Incomes = new ArrayList<>();


        for (int i = 0; i < 6; i++) {

            double income = 0;

            today = today.minusDays(1);

            income = dailyFinanceService.getFinance(today);

            logger.info("today: " + today.toString() + "finance" + income);

            Incomes.add(income);

        }

        return ResponseEntity.ok(new JSon_Object<String>(Incomes.toString()));
    }

























}
