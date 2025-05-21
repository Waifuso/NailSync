package phong.demo.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import phong.demo.DTO.*;
import phong.demo.Entity.Employee;
import phong.demo.Entity.Service;
import phong.demo.Entity.SmallTimeframe;
import phong.demo.Entity.TimeFrame;
import phong.demo.Repository.EmployeeReposittory;
import phong.demo.Repository.TimeFrameRepository;
import phong.demo.Service.Serviceservice;
import phong.demo.Service.TimeframeService;
import phong.demo.Service.TimeframeService;
import phong.demo.Springpro.ResponseMessagge;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/timeframe")
public class TimeframeController {

    @Autowired
    private TimeframeService timeFrameService;

    @Autowired
    private TimeFrameRepository timeFrameRepository;

    @Autowired
    private EmployeeReposittory employeeReposittory;

    @Autowired
    private Serviceservice serviceservice;

    public TimeframeController(TimeframeService timeFrameService, TimeFrameRepository timeFrameRepository, EmployeeReposittory employeeReposittory, Serviceservice serviceservice) {
        this.timeFrameService = timeFrameService;
        this.timeFrameRepository = timeFrameRepository;
        this.employeeReposittory = employeeReposittory;
        this.serviceservice = serviceservice;
    }

    /**
     * the function use to get all the the time frame that the employee is busy
     * @param id
     * @param localDate
     * @return
     */
    @GetMapping("/get/appointment/employee")
    public ResponseEntity<?> availableTimeslot(@RequestParam long id,@RequestParam LocalDate localDate){


        Optional<TimeFrame>  optionalTimeFrame = timeFrameRepository.findByDateAndEmployeeId(localDate,id);

        if (optionalTimeFrame.isEmpty()){

            return ResponseEntity.badRequest().body(new JSOn_objectString("can not find the timeframe"));
        }

        TimeFrame timeFrame= optionalTimeFrame.get();

        List<SmallTimeframe> smallTimeframes = timeFrame.getSmallTimeframeList();



        return ResponseEntity.ok(smallTimeframes);
    }


    @GetMapping(path = "/{date}/{time}/{employeeId}")
    public ResponseEntity<?> servicesInAppointment (@PathVariable LocalDate date, @PathVariable LocalTime time, @PathVariable Long employeeId) {
        List<Service_DTO> services = serviceservice.returnService(date, time, employeeId);

        if (services.isEmpty()) {
            return ResponseEntity.badRequest().body(new ResponseMessagge("No services found for the appointment"));
        }

        return ResponseEntity.ok(new JSon_Object<>(services));
    }








    /**
     * API to generate timeframes for an employee for the whole month
     * Example:
     * /api/timeframe/generate?employeeId=1&month=4&year=2025&startShiftTime=09:00&endShiftTime=17:30
     */
    @PostMapping("/generate")
    public ResponseEntity<?> generateTimeFrames(
            @RequestParam Long id,
            @RequestParam Integer month,
            @RequestParam Integer year,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startShiftTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime endShiftTime) {



        try {

            timeFrameService.timeFrame(id, month, year, startShiftTime, endShiftTime);
            return ResponseEntity.ok("TimeFrames successfully created for the month.");
        } catch (IllegalArgumentException e) {

            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (jakarta.persistence.EntityNotFoundException e) {

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unexpected error: " + e.getMessage());
        }
    }



    @PostMapping("/employee/{id}")
    public ResponseEntity<?> createTimeframe(@PathVariable long id, @RequestBody TimeFrame timeFrame){

        Optional<Employee> OptionEmployee = employeeReposittory.findById(id);



        Employee employee = OptionEmployee.get();

        employee.getTimeFrame().add(timeFrame);

        timeFrame.setEmployee(employee);

        TimeFrame saved_Timeframe  = timeFrameRepository.save(timeFrame);

        Employee saveEmployee = employeeReposittory.save(employee);




        return  ResponseEntity.ok(" The requested timeframe has already been created");
    }

    /**
     * Endpoint to check available slots for an employee on a given date,
     * with a list of service IDs that determine the required time.
     * <p>
     * Example usage in Postman:
     * POST /api/time/checkAvailability?employeeId=1&date=2025-08-22
     * Body (raw JSON): [101, 102, 103]
     */
    @PostMapping("/checkAvailability")
    public ResponseEntity<?> checkAvailability(
            @RequestParam long employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestBody ServiceIdsDTO serviceIdsDTO
    ) {
        // Call your Checkavaiable(...) method on the TimeframeService
        List<TimeFrame_DTO> freeSlots = timeFrameService.Checkavaiable(employeeId, date, serviceIdsDTO.getServiceIds());

        TimeFrameSlotDTO timeFrameSlotDTO = new TimeFrameSlotDTO(freeSlots);
        // Return the free slots as JSONObject
        return ResponseEntity.ok(timeFrameSlotDTO);
    }

    // the code use for modifying the time frame of employee
    @PutMapping("/modify/{id}")
    public ResponseEntity<?> TimeframeModify(@PathVariable long id,@RequestBody TimeFrame RequestTimeFrame){

        Optional<Employee> OptionalEm = employeeReposittory.findById(id);

        if (OptionalEm.isEmpty()){

            return ResponseEntity.badRequest().body(new JSOn_objectString(" The employee does not exist"));
        }

        Employee employee = OptionalEm.get();



        Optional<TimeFrame> OptionaltimeFrame = timeFrameRepository.findByDateAndEmployeeId(RequestTimeFrame.getDate(), employee.getEmployee_id());

        if (OptionaltimeFrame.isEmpty()){

            ResponseEntity.badRequest().body(new JSOn_objectString(" can not find the date where the employee work"));
        }

        TimeFrame timeFrame = OptionaltimeFrame.get();

        if (RequestTimeFrame.getShiftStartTime()!= null){

            timeFrame.setShiftStartTime(RequestTimeFrame.getShiftStartTime());

        }

        if (RequestTimeFrame.getShiftEndTime()!= null){

            timeFrame.setShiftEndTime(RequestTimeFrame.getShiftEndTime());

        }


        timeFrameRepository.save(timeFrame);

        return  ResponseEntity.ok(new JSOn_objectString(" modify success"));

    }


    /**
     * the function use for deleting all the time frame;
     */

    @DeleteMapping("Delete")
    public void DeleteAll(){

        timeFrameRepository.deleteAllInBatch();
    }
}
