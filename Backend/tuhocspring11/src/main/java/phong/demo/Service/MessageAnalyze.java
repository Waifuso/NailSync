package phong.demo.Service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import phong.demo.DTO.Service_DTO;
import phong.demo.Entity.Appointment;
import phong.demo.Entity.User;
import phong.demo.Repository.UserRepository;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MessageAnalyze {

    @Autowired
    private  EmployeeService employeeService;

    @Autowired
    private  Serviceservice serviceservice;

    @Autowired
    private UserRepository userRepository;


    private final Logger logger = LoggerFactory.getLogger(MessageAnalyze.class);

    public String Analyze(String message) {
        // the list of employee name
        List<String> employees = employeeService.getAllEmployeename();

        // the list of service name
        List<String> services = serviceservice.getServicenameList();

        // the map of key is employee and value is type of service
        Map<String, List<Service_DTO>> EmployeeAndService = employeeService.getEmployeeAndService();

        // the map of the key is service and the value is the name of people of provide that service
        Map<String, List<String>> ServiceAndEmployee = serviceservice.getServiceAndEmployee();

        // the list of possible ask for service
        List<String> ArraystringofPossibleListforService = List.of("price list", "price", "service", "services", "spa service", "service?","offers","offer");
        Set<String> SetstringofPossibleListforService = new HashSet<>(ArraystringofPossibleListforService);

        List<String> stringofPossibleListforEmployee = List.of("employee","staff");
        boolean EmployeeMentioned = false;

        boolean ServiceMenttioned = false;


        String cleaned = message
                .toLowerCase()
                // replace every non-letter/digit/space with a space
                .replaceAll("[^a-z0-9\\s]", " ")
                .trim()
                .replaceAll("\\s+", " ");

        String[] tokens =cleaned.split(" ");

        for (int i = 0; i < tokens.length;i++){

        }

        for (String word:tokens){

            if (SetstringofPossibleListforService.contains(word) || services.contains(word)){

                ServiceMenttioned = true;
            } else if (employees.stream().map(h -> h.toLowerCase()).collect(Collectors.toList()).contains(word)) {

                EmployeeMentioned = true;
            }

            if (ServiceMenttioned == true && EmployeeMentioned == true){

                break;
            }
        }

        if(ServiceMenttioned == true && EmployeeMentioned == true){

            return  EmployeeAndService.entrySet().stream()
                    .map(e ->
                            e.getKey()
                                    + " has those service: "
                                    + e.getValue().stream()
                                    .map(Service_DTO::getService_name)
                                    .collect(Collectors.joining(", "))
                    )
                    .collect(Collectors.joining(" | "));

        }else if(ServiceMenttioned == true && EmployeeMentioned == false){

            String csv = EmployeeAndService.entrySet().stream()
                    .map(e ->
                            e.getKey()
                                    + " has those service: "
                                    + e.getValue().stream()
                                    .map(Service_DTO::getService_name)
                                    .collect(Collectors.joining(", "))
                    )
                    .collect(Collectors.joining(" | "));


            String servicelistToString = String.valueOf(serviceservice.getServiceList().stream().map(Service_DTO::toString).collect(Collectors.toList()));


            logger.info(servicelistToString);
            return " if the user ask for service only here is the list of service we have : " + servicelistToString

                    +"if the user might mention the name of someone but the analyze system can not detect the employee name in database " +

                    " Here is the other option they can choose : " + csv;

        }else if(ServiceMenttioned == false && EmployeeMentioned == true){

            return "if the user ask about employee here is the list of employee we have " + employees;
        }

        return " if the user ask about appoinment time direct them to the left side of the screen after log in"
                + "other than that answer normally"+ "here is extra information in case the analyze error " +
                "service: " + services.toString() +"employee" + employees.toString();
    }


    /**
     * the function is
     * @param id
     * @return
     */
    public String extraInformationUser(long id ){

        Optional<User> userOptional = userRepository.findById(id);

        User user = userOptional.get();

        List<Appointment>  appointments = user.getAppointmentList().stream().filter(appointment -> appointment.getDate().equals(LocalDate.now())).collect(Collectors.toList());

        String CustomerInformation = "You are currently talking with the customer name " + user.getUsername()
                + " their current Ranking is " + user.getProfile().getRanking() + " their current point is "
                + user.getProfile().getTotalPoints()  + " here is the list of their appointmnet for today" +
                appointments.toString() ;

        return CustomerInformation;
    }



}
