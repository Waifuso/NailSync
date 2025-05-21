package phong.demo.Service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import phong.demo.DTO.Service_DTO;
import phong.demo.Entity.Appointment;
import phong.demo.Entity.AppointmentService;
import phong.demo.Entity.Handler;
import phong.demo.Repository.AppointmentRepository;
import phong.demo.Repository.AppointmentServiceRepository;
import phong.demo.Repository.EmployeeReposittory;
import phong.demo.Repository.ServiceRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class Serviceservice {

    @Autowired
    private EmployeeReposittory employeeReposittory;
    @Autowired
    private ServiceRepository serviceRepository;
    private AppointmentRepository appointmentRepository;
    private AppointmentServiceRepository appointmentServiceRepository;
    //private static final Logger logger = LoggerFactory.getLogger(Serviceservice.class);

    public Serviceservice(EmployeeReposittory employeeReposittory, ServiceRepository serviceRepository, AppointmentRepository appointmentRepository, AppointmentServiceRepository appointmentServiceRepository) {
        this.employeeReposittory = employeeReposittory;
        this.serviceRepository = serviceRepository;
        this.appointmentRepository = appointmentRepository;
        this.appointmentServiceRepository = appointmentServiceRepository;
    }

    public Integer totalAmount(List<Long> serviceList){

        List<phong.demo.Entity.Service> services = serviceRepository.findAllById(serviceList);

        if (services.size() < serviceList.size()){

            throw new EntityNotFoundException(" one of the service in the list might not exist ");
        }

        Integer total = services.stream().mapToInt(phong.demo.Entity.Service::getDuration).sum();

        return total;
    }

    public List<Service_DTO> returnService (LocalDate date, LocalTime startTime, Long employeeId ) {
        Optional<Appointment> appointmentOptional = appointmentRepository.findByDateAndStartTime(date, startTime);
        if (appointmentOptional.isEmpty()) {
            return Collections.emptyList();
        }
        List<Service_DTO> serviceList = new ArrayList<>();

        Appointment appointment = appointmentOptional.get();

        List<AppointmentService> appointmentServiceList = appointment.getAppointmentServices();

        for (AppointmentService appointmentService1 : appointmentServiceList) {

            serviceList.add(new Service_DTO(appointmentService1.getService().getService_name(),appointmentService1.getService().getPrice(),appointmentService1.getService().getDuration()));
        }
        return serviceList;
    }


    /**
     * Return the list of service name
     */

    public List<String> getServicenameList(){

        List<String> servicenames = serviceRepository.findAll().stream().map(service -> service.getService_name()).collect(Collectors.toList());


        return servicenames;
    }

    /**
     * return the map with  the key is the name of the service and
     */
    public Map<String,List<String>>getServiceAndEmployee(){

        Map<String,List<String>> ServiceAndEmployeeMap = new HashMap<>();

        for (phong.demo.Entity.Service service:serviceRepository.findAll()){

            // the name of the service
            String serviceName = service.getService_name();
            // the list of employee name
            List<String> employeenames = service.getService_Handler().stream().map(Handler->Handler.getEmployee().getUsername()).collect(Collectors.toList());

            ServiceAndEmployeeMap.put(serviceName,employeenames);

        }
        return ServiceAndEmployeeMap;
    }

    /**
     * return the list of service include name,price and duration in each service
     */
    public List<Service_DTO> getServiceList(){

        return  serviceRepository.findAll().stream().map(service -> new Service_DTO(service.getService_name(), service.getPrice(),service.getDuration())).collect(Collectors.toList());
    }
}
