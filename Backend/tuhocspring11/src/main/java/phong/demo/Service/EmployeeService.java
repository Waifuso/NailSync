package phong.demo.Service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import phong.demo.DTO.Service_DTO;
import phong.demo.Entity.Employee;
import phong.demo.Entity.Handler;
import phong.demo.Repository.EmployeeReposittory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class EmployeeService {


    @Autowired
    private EmployeeReposittory employeeReposittory;

    private final Logger logger = LoggerFactory.getLogger(EmployeeService.class);


    public List<String> getAllEmployeename(){


        List<String> employees = employeeReposittory.findAll().stream().map(employee -> employee.getUsername()).collect(Collectors.toList());

        logger.info(employees.toString());

        return employees;
    }



    public Map<String,List<Service_DTO>>getEmployeeAndService(){
         Map<String,List<Service_DTO>> EmployeeAndServicelist = new HashMap<>();


         for (Employee employee:employeeReposittory.findAll()){

             String name = employee.getUsername();
             List<Service_DTO> serviceDtos  = employee.getEmployee_Handler().stream().map(Handler-> new Service_DTO(Handler.getService().getService_name(),Handler.getService().getPrice(),Handler.getService().getDuration())).collect(Collectors.toList());

             EmployeeAndServicelist.put(name,serviceDtos);


         }

         logger.info(EmployeeAndServicelist.toString());
        return EmployeeAndServicelist;
    }




}
