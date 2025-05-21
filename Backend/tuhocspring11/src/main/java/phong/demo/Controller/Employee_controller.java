package phong.demo.Controller;


import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import phong.demo.DTO.*;
import phong.demo.Entity.Employee;
import phong.demo.Entity.Handler;
import phong.demo.Entity.Service;
import phong.demo.Repository.EmployeeReposittory;
import phong.demo.Repository.ServiceRepository;
import phong.demo.Repository.TimeFrameRepository;

import java.util.*;

@RestController
@RequestMapping(path = "api/employee")
public class Employee_controller {

    @Autowired
    private  EmployeeReposittory employeeReposittory;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private TimeFrameRepository timeFrameRepository;




    @GetMapping("/info/name")
    public JSon_Object<List<Json_object_withID>> Employeeinfo(){

        ArrayList<Json_object_withID> objectlist = new ArrayList<>();


        List<Employee> all = employeeReposittory.findAll();
        for (Employee employee : all){


            objectlist.add(new Json_object_withID(employee.getUsername(), employee.getId()));
        }

        return new JSon_Object<>(objectlist);
    }




    /**
     *  The function use to look for the service of the employee
     * @param username
     * @return the list of service
     */
    @GetMapping("Find/service/{username}")
    public ResponseEntity<?> findByUsername(@PathVariable String username) {

        Optional<Employee> employeeOptional = employeeReposittory.findByUsername(username);

        if (employeeOptional.isEmpty()) {

            return ResponseEntity.notFound().build();
        }
        Employee employee = employeeOptional.get();

        List<Service_DTO> answer = new ArrayList<>();


        for (Handler handler : employee.getEmployee_Handler()) {
            // Add service if it's not null
            if (handler.getEmployee().getId() == employee.getEmployee_id()) {

                Service_DTO serviceDto = new Service_DTO(handler.getService().getService_name(),handler.getService().getPrice(),handler.getService().getDuration());

                answer.add(serviceDto);
            }
        }

        return ResponseEntity.ok(answer);
    }


    /**
     * The function use fo registeringthe Employee with @param username and date of birth
     * @param Em_request
     * @return the response type with the message inside
     */
    @PostMapping("/Create/modify")
    public ResponseEntity<?> Create_Employee_ver2(@NotNull @RequestBody Employee_CreateRequest Em_request) {

    Employee Cur_Em = new Employee();

    // Validate employee username
    if (Em_request.getUser_name() == null) {
        return ResponseEntity.badRequest().body(new JSOn_objectString("Employee name cannot be null"));
    }
    if (employeeReposittory.existsByUsername(Em_request.getUser_name())) {
        return ResponseEntity.badRequest().body(new JSOn_objectString("The username already exists."));
    }
    Cur_Em.setUsername(Em_request.getUser_name());

    // Validate Date of Birth
    if (Em_request.getDob() == null) {
        return ResponseEntity.badRequest().body("Date of birth cannot be null");
    }
    Cur_Em.setDob(Em_request.getDob());

    // Set email and password for the employee
    Cur_Em.setEmail(Em_request.getUser_name() + "@luckynail.com");
    Cur_Em.setServicePassword(Em_request.getUser_name() + Em_request.getDob().toString());
    Cur_Em.setAvailable(true);

    // Process each service from the request
    ArrayList<Service_DTO> serviceDTOList = Em_request.getService();
    if (serviceDTOList != null) {
        for (Service_DTO serviceDto : serviceDTOList) {
            if (serviceDto.getService_name() == null
                    || serviceDto.getPrice() == null
                    || serviceDto.getPrice() < 0
                    || serviceDto.getService_name().trim().isEmpty()) {

                return ResponseEntity.badRequest().body(new JSOn_objectString("Invalid data input"));

            } else {
                //  find an existing service by its name
                Optional<Service> existingService = serviceRepository.findByServiceName(serviceDto.getService_name());

                Service serviceToUse;
                if (existingService.isPresent()) {
                    // If found, reuse the existing service
                    serviceToUse = existingService.get();
                } else {
                    // Otherwise, create a new service and save it
                    Service newService = new Service();

                    newService.setService_name(serviceDto.getService_name());

                    newService.setPrice(serviceDto.getPrice());

                    newService.setDuration(serviceDto.getDuration());

                    serviceToUse = serviceRepository.save(newService);
                }
                // Create a handler to link the employee with the service
                Handler handler = new Handler();

                handler.setService(serviceToUse);

                Cur_Em.Add_Handler(handler);
            }
        }
    }

    // Save the employee along with its service handlers
    Employee savedEmployee = employeeReposittory.save(Cur_Em);

    return ResponseEntity.ok(new JSOn_objectString("Employee create success !!!!"));
}


    /**
     * Add the service to the employee based on their user name and return the json object back
     * @param username
     * @param serviceDTO
     * @return the response type with  message  inside
     */

    @PostMapping("/{username}/service")
    public ResponseEntity<?> addServiceToEmployee(@PathVariable String username,
                                                  @RequestBody Service_DTO serviceDTO) {
        Optional<Employee> optionalEmployee = employeeReposittory.findByUsername(username);
        if (optionalEmployee.isEmpty()) {

            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new JSOn_objectString("Employee not found"));
        }

        Employee employee = optionalEmployee.get();

        // Validate service data: service name must be provided and price must be non-null and non-negative
        if (serviceDTO.getService_name() == null ||
                serviceDTO.getService_name().trim().isEmpty() ||
                serviceDTO.getPrice() == null ||
                serviceDTO.getPrice() < 0) {

            return ResponseEntity.badRequest().body(new JSOn_objectString("Invalid service data"));
        }

        boolean Is_already_exist = employee.getEmployee_Handler().stream().anyMatch(handler -> handler.getService().getService_name().equals(serviceDTO.getService_name()));

        if(Is_already_exist){

            return ResponseEntity.badRequest().body(" The service has already exist");
        }

       // find the service
        Optional<Service> Cur_service = serviceRepository.findByServiceName(serviceDTO.getService_name());

        Service savedService;

        if (Cur_service.isPresent()){

            savedService = Cur_service.get();

        }else {
            Service newService = new Service();

            newService.setService_name(serviceDTO.getService_name());

            newService.setPrice(serviceDTO.getPrice());

            newService.setDuration(serviceDTO.getDuration());

            savedService = serviceRepository.save(newService);
        }


        // Create a new Handler to link the employee with the new service
        Handler handler = new Handler();
        handler.setService(savedService);
        // Add the handler to the employee (this method also sets the employee in the handler)
        employee.Add_Handler(handler);


        employeeReposittory.save(employee);

        return ResponseEntity.ok(new JSOn_objectString("Service added successfully"));
    }


    /**
     *  update Employee based on the id
     * @param id
     * @param updateRequest
     * @return the response type with  message  inside
     */
    @PutMapping("/edit/{id}")
    public ResponseEntity<?> updateEmployee(@PathVariable long id,
                                            @RequestBody Update_Employee__DTO updateRequest) {

        // Validate the update request itself
        if (updateRequest == null) {
            return ResponseEntity.badRequest().body(new JSOn_objectString("Update request cannot be null"));
        }

        // Retrieve the current employee based on the original username from the path variable
        Optional<Employee> optionalEmployee = employeeReposittory.findById(id);
        if (optionalEmployee.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Employee employee = optionalEmployee.get();

        // Update username only if provided in the request.
        if (updateRequest.getUsername() != null) {
            // Check for duplicate only if the new username is different than the current one
            if (!employee.getUsername().equals(updateRequest.getUsername()) &&
                    employeeReposittory.existsByUsername(updateRequest.getUsername())) {

                return ResponseEntity.badRequest().body(new JSOn_objectString("The username has already been used"));
            }
            employee.setUsername(updateRequest.getUsername());
        }

        // Update date of birth only if provided
        if (updateRequest.getDob() != null) {
            employee.setDob(updateRequest.getDob());
        }

        // Update email only if provided
        if (updateRequest.getEmail() != null) {
            employee.setEmail(updateRequest.getEmail());
        }

        // Update service password if provided and ensure it's different from the existing one
        if (updateRequest.getServicePassword() != null) {
            if (employee.getServicePassword().equals(updateRequest.getServicePassword())) {

                return ResponseEntity.badRequest().body(new JSOn_objectString("Password cannot be the same as the previous one"));
            }
            employee.setServicePassword(updateRequest.getServicePassword());
        }





        // Save the updated employee entity
        Employee savedEmployee = employeeReposittory.save(employee);


        return ResponseEntity.ok(new JSOn_objectString("update successfull"));

    }

    /**
     * delete the employee
     * @param id
     * @return the response type with  message  inside
     */

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEmployee(@PathVariable Long id) {
        Optional<Employee> optionalEmployee = employeeReposittory.findById(id);
        if (optionalEmployee.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new JSOn_objectString("Employee not found"));
        }
        employeeReposittory.delete(optionalEmployee.get());

        return ResponseEntity.ok(new JSOn_objectString("Employee deleted successfully"));
    }

    // delte all the employee
    @DeleteMapping()
    public void Delete(){
        employeeReposittory.deleteAll();
    }








}
