package phong.demo.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.*;

import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import phong.demo.DTO.TimeFrame_DTO;
import phong.demo.Entity.*;
import phong.demo.Repository.AppointmentRepository;
import phong.demo.Repository.EmployeeReposittory;
import phong.demo.Repository.TimeFrameRepository;

@Service
public class TimeframeService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private  EmployeeReposittory employeeReposittory;

    @Autowired
    private TimeFrameRepository timeFrameRepository;

    @Autowired
    private  Serviceservice serviceservice;

    private static List<YearMonth> existList = new ArrayList<YearMonth>();

    private final Logger logger = LoggerFactory.getLogger(TimeframeService.class);

    /**
     * Returns the appointment with the end time closest to the new appointment's start time. previous appointment
     */
    public static Appointment findClosestEndAptwithStarttime(List<Appointment> appointmentListBefore, LocalTime starttime) {
        Appointment closestAppointmentPrevious = null;
        long minDiff = Long.MAX_VALUE;
        for (Appointment apt : appointmentListBefore) {
            if (apt.getStartTime().isBefore(starttime)) {
                Duration diff = Duration.between(apt.getEndTime(), starttime);
                long diffCast = diff.toMinutes();
                if (diffCast < minDiff) {
                    minDiff = diffCast;
                    closestAppointmentPrevious = apt;
                }
            }
        }
        return closestAppointmentPrevious;
    }

    /**
     * Returns the appointment with the start time closest to the new appointment's end time. the appoinment after
     */
    public static Appointment findClosestStartAptWithEndtime(List<Appointment> appointmentListAfter, LocalTime endtime) {
        Appointment closestAppointmentAfter = null;
        long minDiff = Long.MAX_VALUE;
        for (Appointment apt : appointmentListAfter) {
            if (apt.getStartTime().isAfter(endtime)) {
                Duration diff = Duration.between(endtime, apt.getStartTime());
                long diffCast = diff.toMinutes();
                if (diffCast < minDiff) {
                    minDiff = diffCast;
                    closestAppointmentAfter = apt;
                }
            }
        }
        return closestAppointmentAfter;
    }

    /**
     * Checks if the new appointment (from appointmentStart to appointmentEnd on appointmentDate)
     * fits within the employee's defined timeframe and does not conflict with existing appointments.
     */
    public boolean checkTimeframe(Employee employee, LocalTime appointmentStart, LocalTime appointmentEnd, LocalDate appointmentDate) {
        // take the  time Big time frame where contain shift start and shift end

        if (appointmentDate == null) { logger.error("appointmentDate is null"); return false; }

        Optional<TimeFrame> optionalTimeFrame = employee.getTimeFrame().stream()
                .filter(tf -> tf.getDate().isEqual(appointmentDate))
                .findFirst();

        if (!optionalTimeFrame.isPresent()) {
            logger.info("No timeframe found for date: " + appointmentDate);
            return false;
        }

        TimeFrame timeFrame = optionalTimeFrame.get();
        LocalTime beginShiftTime = timeFrame.getShiftStartTime();
        LocalTime endShiftTime = timeFrame.getShiftEndTime();

        // check if the  appointment time is before the begin or end after shift start and shift end.If
        if (appointmentStart.isBefore(beginShiftTime) || appointmentEnd.isAfter(endShiftTime)) {
            return false;
        }

        List<Appointment> dateAppointments = new ArrayList<>();
        for (AppointmentService appointmentService : employee.getAppointmentServices()) {
            // check if the requested appointment has already existed then return false
            if (appointmentService.getAppointment().getDate().isEqual(appointmentDate)
                    && (appointmentService.getAppointment().getStartTime().equals(appointmentStart)
                    || appointmentService.getAppointment().getEndTime().equals(appointmentEnd))) {
                return false;
            }
            else if (appointmentService.getAppointment().getDate().isEqual(appointmentDate)){
                dateAppointments.add(appointmentService.getAppointment());
            }
        }


        // testing purpose
        logger.info(dateAppointments.toString());

        if (dateAppointments.isEmpty()) {
            return true;
        }

        Appointment previousAppointment = TimeframeService.findClosestEndAptwithStarttime(dateAppointments, appointmentStart);
        Appointment afterAppointment = TimeframeService.findClosestStartAptWithEndtime(dateAppointments, appointmentEnd);

        if (previousAppointment == null && afterAppointment != null && appointmentEnd.isBefore(afterAppointment.getStartTime())) {
            return true;
        }
        if (afterAppointment == null && previousAppointment != null && appointmentStart.isAfter(previousAppointment.getEndTime())) {
            return true;
        }
        if (previousAppointment != null && afterAppointment != null &&
                appointmentStart.isAfter(previousAppointment.getEndTime()) &&
                appointmentEnd.isBefore(afterAppointment.getStartTime())) {
            return true;
        }

        return false;
    }

    /**
     * take the id of the eployee, the date for appointment and list id of services
     * @return the list of free time in
     */
    public List<TimeFrame_DTO> Checkavaiable(long id, LocalDate date, List<Long> idsService){

        if (date == null){

            throw  new IllegalArgumentException("the date is null");
        }

        Optional<Employee> OptionalEmloyee = employeeReposittory.findById(id);


        // find the needed timeframe
        Optional<TimeFrame> OptionalTimeframe = timeFrameRepository.findByDateAndEmployeeId(date,id);

        if(OptionalTimeframe.isEmpty()){

            throw new EntityNotFoundException("can not find the requested timeframe ");
        }

        TimeFrame timeFrame = OptionalTimeframe.get();
        // the start and end time of the shift
        LocalTime start = timeFrame.getShiftStartTime();
        LocalTime end = timeFrame.getShiftEndTime();

        // required time to finish all the service
        Integer Requiretime = serviceservice.totalAmount(idsService);

        // the list of time that is available


        // the list of Free fromstart to end
        List<TimeFrame_DTO> FreeSlot = new ArrayList<>();

        // the list of time frame within a date
        List<SmallTimeframe> smallTimeframeList = timeFrame.getSmallTimeframeList();

        if (smallTimeframeList.isEmpty()){



            FreeSlot.add(new TimeFrame_DTO(start,end.minusMinutes(Requiretime)));

            return FreeSlot;
        }

        // sort the list to make it easy to handle the data
        smallTimeframeList.sort(Comparator.comparing(SmallTimeframe::getSubStartTime));

        //test
        logger.info(smallTimeframeList.toString());
        LocalTime current = start;
        // use to handle the case where it start since we allow to book when the shift start
        boolean isFirst = true;
        for (SmallTimeframe smallTimeframe:smallTimeframeList){


            long gap = Duration.between(current, smallTimeframe.getSubStartTime()).toMinutes();

            logger.info(current.toString());

            if (gap > Requiretime){

                if (isFirst == true){

                    TimeFrame_DTO timeFrameDto = new TimeFrame_DTO(current,smallTimeframe.getSubStartTime().minusMinutes(Requiretime));

                    FreeSlot.add(timeFrameDto);

                    isFirst = false;


                }else{

                    TimeFrame_DTO timeFrameDto = new TimeFrame_DTO(current,smallTimeframe.getSubStartTime().plusMinutes(1).minusMinutes(Requiretime));

                    FreeSlot.add(timeFrameDto);

                }


            } else if (gap == Requiretime) {

                if (isFirst == true){

                    TimeFrame_DTO timeFrameDto = new TimeFrame_DTO(current,smallTimeframe.getSubStartTime().minusMinutes(Requiretime));

                    FreeSlot.add(timeFrameDto);

                    isFirst = false;


                }else{

                    TimeFrame_DTO timeFrameDto = new TimeFrame_DTO(current,smallTimeframe.getSubStartTime().plusMinutes(1).minusMinutes(Requiretime));

                    FreeSlot.add(timeFrameDto);

                }

            }
            current = smallTimeframe.getSubEndTime();
        }

        logger.info(current.toString());
        // the list of time that is available


        // check for the case from last appointment to the end shift time
        long final_gap = Duration.between(current,end).toMinutes();

        if (final_gap > Requiretime){

            TimeFrame_DTO timeFrameDto = new TimeFrame_DTO(current.plusMinutes(1),end.minusMinutes(Requiretime));

            FreeSlot.add(timeFrameDto);

        } else if (final_gap == Requiretime) {

            TimeFrame_DTO timeFrameDto = new TimeFrame_DTO(current.plusMinutes(1),end);

            FreeSlot.add(timeFrameDto);

        }


        return FreeSlot;

    }

    /**
     * The function use to create the timeframe for the employee for the whole month
     * @param id
     * @param month
     * @param year
     * @param startShiftime
     * @param endShiftime
     */
    public void timeFrame(long id, Integer month , Integer year,LocalTime startShiftime,LocalTime endShiftime){
        // check the date and the year
        if(month == null || year == null){

            throw new IllegalArgumentException("Either the date or the year is null");
        }
        // the list of 30 days


        Optional<Employee> optionalEmployee = employeeReposittory.findById(id);

        if (optionalEmployee.isEmpty()){

            throw new EntityNotFoundException(" can not find the employee");
        }
        List<TimeFrame> TimeFrames = new ArrayList<>();

        Employee employee = optionalEmployee.get();

        YearMonth yearMonth = YearMonth.of(year,month);

        boolean idAndMonthAndYearsExisted = timeFrameRepository.existsByEmployeeIdAndMonthAndYear(id,month,year);

        if(idAndMonthAndYearsExisted){

            throw new IllegalArgumentException("This month and year has already been created");
        }

        for (int i = 1; i <=yearMonth.lengthOfMonth();i++ ){

            LocalDate localDate = LocalDate.of(year,month,i);

            TimeFrame timeFrame = new TimeFrame(localDate,startShiftime,endShiftime);

            timeFrame.setEmployee(employee);

            TimeFrames.add(timeFrame);
        }

        timeFrameRepository.saveAll(TimeFrames);
    }





}