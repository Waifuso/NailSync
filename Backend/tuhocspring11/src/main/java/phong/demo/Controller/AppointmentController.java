package phong.demo.Controller;


import com.fasterxml.jackson.annotation.JsonIgnore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.MessagingException;
import org.springframework.web.bind.annotation.*;
import phong.demo.DTO.AppointmentDetailsDTO;
import phong.demo.DTO.BookingDTO;
import phong.demo.Entity.*;
import phong.demo.Repository.*;
import phong.demo.Service.ProfileService;
import phong.demo.Service.TimeframeService;
import phong.demo.Springpro.RandomNumberService;
import phong.demo.Springpro.ResponseMessagge;
import phong.demo.Springpro.SendEmailService;


import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(path = "/api/appointments")
public class AppointmentController {

    @Autowired
    private final AppointmentRepository appointmentRepository;
    private final EmployeeReposittory employeeReposittory;
    private final ServiceRepository serviceRepository;
    private final UserRepository userRepository;
    private final HandlerRepository handlerRepository;
    private final AppointmentServiceRepository appointmentServiceRepository;
    private TimeframeService timeframeService;
    private final TimeFrameRepository timeFrameRepository;
    private final SmallTimeFrameRepository smallTimeFrameRepository;
    private RandomNumberService randomNumberService;
    private SendEmailService sendEmailService;
    private ProfileService profileService;


    private final Logger logger = LoggerFactory.getLogger(AppointmentController.class);




    public AppointmentController(AppointmentRepository appointmentRepository, EmployeeReposittory employeeReposittory, ServiceRepository serviceRepository, UserRepository userRepository, HandlerRepository handlerRepository, AppointmentServiceRepository appointmentServiceRepository, TimeframeService timeframeService, TimeFrameRepository timeFrameRepository, SmallTimeFrameRepository smallTimeFrameRepository, RandomNumberService randomNumberService, SendEmailService sendEmailService,ProfileService profileService ) {
        this.appointmentRepository = appointmentRepository;
        this.employeeReposittory = employeeReposittory;
        this.serviceRepository = serviceRepository;
        this.userRepository = userRepository;
        this.handlerRepository = handlerRepository;
        this.appointmentServiceRepository = appointmentServiceRepository;
        this.timeframeService = timeframeService;
        this.timeFrameRepository = timeFrameRepository;
        this.smallTimeFrameRepository = smallTimeFrameRepository;
        this.randomNumberService = randomNumberService;
        this.sendEmailService = sendEmailService;
        this.profileService = profileService;
    }

    @GetMapping(path = "/info/{date}")
    public List<Appointment> getAllAppointment(){
        return appointmentRepository.findAll();
    }

    @GetMapping(path = "/infoSerandEm")
    @JsonIgnore
    public List<AppointmentService> getAllInfo() {
        return appointmentServiceRepository.findAll();
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getAppointmentByUserId(@PathVariable Long userId) {
        List<Appointment> CurUserId = appointmentRepository.findAllByUserId(userId);
        if (CurUserId.isEmpty()) {
            return ResponseEntity.badRequest().body(new ResponseMessagge("There are no appointments"));
        }
        List<AppointmentDetailsDTO> result = new ArrayList<>();

        for (Appointment id : CurUserId) {
            List<Long> serviceIDD = new ArrayList<>();

            List<AppointmentService> appointmentServices = appointmentServiceRepository.findByAppointmentId(id.getId());

            String employeeName = "";

            for (AppointmentService appService : appointmentServices) {

                serviceIDD.add(appService.getService().getId());
                employeeName = appService.getEmployee().getUsername();

            }
            result.add(new AppointmentDetailsDTO(id, serviceIDD, employeeName));
        }

        return ResponseEntity.ok(result);
    }


    @PostMapping(path = "/booking")
    public ResponseEntity<?> bookingAppointment (@RequestBody BookingDTO bookingDTO) {

        Optional<User> existingID = userRepository.findById(bookingDTO.getUserId());
        if (existingID.isEmpty()) {
            return ResponseEntity.badRequest().body(new ResponseMessagge("User not found"));
        }

        User user = existingID.get();


//        if (bookingDTO.getServiceId().size() != bookingDTO.getEmployeeId().size()) {
//            return ResponseEntity.badRequest().body(new ResponseMessagge("Mismatch between number of services and assigned employees."));
//        }

        Optional<Employee> existingEm = employeeReposittory.findById(bookingDTO.getEmployeeId());

        if (existingEm.isEmpty()) {
            return ResponseEntity.badRequest().body(new ResponseMessagge("The employee not found"));
        }

        Employee employee1 = existingEm.get();

        List<Service> existingSer = serviceRepository.findAllById(bookingDTO.getServiceId());
        if (existingSer.size() != bookingDTO.getServiceId().size()) {
            return ResponseEntity.badRequest().body(new ResponseMessagge("One or more services not found"));

        }

        //timeframeService.checkTimeframe(employee1, bookingDTO.getTime(), bookingDTO.getTime(), bookingDTO.getDate());


        Appointment appointment = new Appointment();
        appointment.setUser(user);
        appointment.setDate(bookingDTO.getDate());
        appointment.setStartTime(bookingDTO.getTime());

        int totalTime = existingSer.stream()
                .mapToInt(Service::getDuration)      // get time
                .sum();                        // sum them
        appointment.setEndTime(appointment.getStartTime().plusMinutes(totalTime));

        if(!timeframeService.checkTimeframe(employee1, appointment.getStartTime(), appointment.getEndTime(), appointment.getDate())) {

            //appointment.setStatus("Declined");
            //appointmentRepository.save(appointment);
            return ResponseEntity.badRequest().body(new ResponseMessagge("Conflicted hours"));
        }



        List <AppointmentService> appointmentServices = new ArrayList<>();


        for (int i = 0; i < existingSer.size(); i++) {
            Long serviceId = bookingDTO.getServiceId().get(i);
            Optional<Handler> handler = handlerRepository.findByServiceIdAndEmployeeId(serviceId, employee1.getEmployee_id());

            if (handler.isEmpty()) {
                return ResponseEntity.badRequest().body(new ResponseMessagge("Employee ID" + employee1.getEmployee_id() + " is not allowed to perform Service ID " + serviceId));
            }

            AppointmentService appointmentService = new AppointmentService();
            appointmentService.setAppointment(appointment);
            appointmentService.setService(existingSer.get(i));
            appointmentService.setEmployee(employee1);



            appointmentServices.add(appointmentService);


        }

        Optional<TimeFrame> optionalTimeFrame = timeFrameRepository.findByDateAndEmployeeId(bookingDTO.getDate(), employee1.getEmployee_id());

        TimeFrame timeFrame = optionalTimeFrame.get();

        if (optionalTimeFrame.isPresent()) {
            SmallTimeframe smallTimeframe = new SmallTimeframe();
            smallTimeframe.setAvailable(true);
            smallTimeframe.setSubStartTime(bookingDTO.getTime());
            smallTimeframe.setSubEndTime(appointment.getStartTime().plusMinutes(totalTime));
            smallTimeframe.setTimeFrame(optionalTimeFrame.get());

            timeFrame.addSmallTimeframe(smallTimeframe);
            smallTimeFrameRepository.save(smallTimeframe);

        }
        appointment.setStatus("Approved");
        int confirmCode = randomNumberService.generateRandomNumber(100000);
        appointment.setConfirmationNumber(confirmCode);

//        int totalPrice = existingSer.stream()
//                .mapToInt(Service::getPrice)      // get price
//                .sum();                        // sum them
//
//        profileService.updateSpending(totalPrice, user.getId());



        timeFrameRepository.save(timeFrame);
        appointmentRepository.save(appointment);
        appointmentServiceRepository.saveAll(appointmentServices);

        //sendEmailService.sendEmailWithHtml(user.getEmail(), "Your NAILSYNC confirmation code", appointment.getConfirmationNumber());

        try {
            try {
                sendEmailService.sendEmailWithHtml(
                        user.getEmail(),
                        "Your NAILSYNC Confirmation Code",
                        appointment.getConfirmationNumber()
                );
            } catch (jakarta.mail.MessagingException e) {
                throw new RuntimeException(e);
            }
        } catch (MessagingException e) {
            e.printStackTrace();
        }




        return ResponseEntity.ok().body(new ResponseMessagge("Appointment booked successfully"));
    }














    @DeleteMapping(path = "/cancel/{appointmentID}")
    public ResponseEntity<?> cancelAppointment (@PathVariable Long appointmentID) {
        Optional<Appointment> confirm = appointmentRepository.findById(appointmentID);
        if (confirm.isPresent()) {
            appointmentRepository.delete(confirm.get());
            appointmentServiceRepository.deleteById(confirm.get().getId());
        return ResponseEntity.ok().body(new ResponseMessagge("Appointment canceled successfully"));
    } else {
        return ResponseEntity.badRequest().body(new ResponseMessagge("Appointment not found"));

        }
    }

    @PutMapping(path = "/change/{appointmentId}/{date}/{time}")
    public ResponseEntity<?> changeAppointment (@PathVariable Long appointmentId, @PathVariable LocalDate date, @PathVariable LocalTime time) {

        Optional<Appointment> appointmentOptional = appointmentRepository.findById(appointmentId);
        if (appointmentOptional.isEmpty()) {
            return ResponseEntity.badRequest().body(new ResponseMessagge("Couldn't find the appointment"));
        }
        Appointment appointment = appointmentOptional.get();

        LocalTime start = appointment.getStartTime();
        LocalTime end = appointment.getEndTime();

        Duration duration = Duration.between(start, end);
        long minutes = duration.toMinutes(); // 90
        
        List<AppointmentService> appointmentService = appointmentServiceRepository.findByAppointmentId(appointmentId);
        Employee employee1 = null;
        for (AppointmentService employee: appointmentService) {
            employee1 = employee.getEmployee();
            
        }
        if (!timeframeService.checkTimeframe(
                employee1,
                time,
                time.plusMinutes(minutes),
                date
        )) {
            return ResponseEntity.badRequest().body(new ResponseMessagge("Conflicted hours"));
        }

        appointment.setDate(date);
        appointment.setStartTime(time);
        appointment.setEndTime(time.plusMinutes(minutes));

        appointmentRepository.save(appointment);

        return ResponseEntity.ok(new ResponseMessagge("Appointment updated successfully"));


    }
}
