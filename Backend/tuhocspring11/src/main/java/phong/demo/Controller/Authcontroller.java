package phong.demo.Controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import phong.demo.DTO.JSOn_objectString;
import phong.demo.DTO.Json_object_withID;
import phong.demo.DTO.Loginrequest;
import phong.demo.Entity.Admin;
import phong.demo.Entity.Employee;
import phong.demo.Entity.User;
import phong.demo.Repository.AdminRepository;
import phong.demo.Repository.EmployeeReposittory;
import phong.demo.Repository.UserRepository;

import java.util.Optional;

@RestController
@RequestMapping("/api/login")
public class Authcontroller {

    @Autowired
    private final UserRepository userRepository;
    @Autowired
    private  EmployeeReposittory employeeReposittory;
    @Autowired
    private AdminRepository adminRepository;

    public Authcontroller(UserRepository userRepository) {
        this.userRepository = userRepository;
    }



    // Log in by uusing user name by Postmapping

    // extra feature
    @PostMapping("")
    public ResponseEntity<String> login(@RequestBody Loginrequest loginRequest) {
        // Find user by username
        Optional<User> curUser = userRepository.findByUsername(loginRequest.getUsername());

        // If user not found, return Unauthorized
        if (curUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid username or password");
        }

        User user = curUser.get();

        // Check if the provided password matches the stored one
        if (!user.getPassword().equals(loginRequest.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid username or password");
        }

        // If valid, you could generate a JWT or session token here.
        // For simplicity, we just return a success message.
        return ResponseEntity.ok("Login successful!");
    }




    // log in by the Email Using getMapping
    @GetMapping("/by-email")
    public ResponseEntity<?> loginByEmail(@RequestParam("email") String email,
                                               @RequestParam("password") String password) {
        // Look up the user by email
        Optional<User> curUser = userRepository.findByEmail(email);

        // If user is not found or password doesn't match, return 401 Unauthorized
        if (curUser.isEmpty() || !curUser.get().getPassword().equals(password)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body( new JSOn_objectString("invalid email or pass word"));
        }

        // If valid, return a success message (optionally, generate a JWT or session)
        return ResponseEntity.ok(new Json_object_withID("successfully",curUser.get().getId()));
    }

    @PostMapping("/employee")
    public ResponseEntity<?> EmployeeLogin(@RequestBody Loginrequest loginrequest){

        Optional<Employee> Cur_em = employeeReposittory.findByUsername(loginrequest.getUsername());

       // Can not find the employee to log in
        if (Cur_em.isEmpty()){

            return  ResponseEntity.status(HttpStatus.NOT_FOUND).body(new JSOn_objectString(" Can not find the username:"));
        }

        Employee employee = Cur_em.get();

        if(!employee.getServicePassword().equals(loginrequest.getPassword())){


            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new JSOn_objectString(" Invalid username or password"));
        }

        return ResponseEntity.ok(new Json_object_withID(employee.getUsername(), employee.getId()));
    }

    @PostMapping("/admin")
    public ResponseEntity<?> AdminLogin(@RequestBody Loginrequest loginRequest) {
        // 1. Find the admin by username
        Optional<Admin> optionalAdmin = adminRepository.findByUsername(loginRequest.getUsername());
        if (optionalAdmin.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new JSOn_objectString("Cannot find username: " + loginRequest.getUsername()));
        }

        Admin admin = optionalAdmin.get();

        // 2. Verify password
        if (!admin.getPassword().equals(loginRequest.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new JSOn_objectString("Invalid username or password"));
        }

        // 3. Success → return their username and ID
        return ResponseEntity.ok(
                new Json_object_withID(admin.getUsername(), admin.getId())
        );
    }

    // in Authcontroller (package phong.demo.Controller;)

    @PostMapping("/admin/signup")
    public ResponseEntity<?> adminSignup(@RequestBody Admin adminRequest) {
        // 1. Basic validation
        if (adminRequest.getUsername() == null || adminRequest.getUsername().trim().isEmpty()) {
            return ResponseEntity
                    .badRequest()
                    .body(new JSOn_objectString("Username cannot be null or empty"));
        }
        if (adminRequest.getEmail() == null || adminRequest.getEmail().trim().isEmpty()) {
            return ResponseEntity
                    .badRequest()
                    .body(new JSOn_objectString("Email cannot be null or empty"));
        }

        // 2. Uniqueness checks
        if (adminRepository.existsByUsername(adminRequest.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body(new JSOn_objectString("Username already exists"));
        }
        if (adminRepository.existsByEmail(adminRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(new JSOn_objectString("Email already in use"));
        }

        // 3. Persist new Admin
        Admin savedAdmin = adminRepository.save(adminRequest);

        // 4. Return a simple DTO with their username and new ID
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new Json_object_withID(savedAdmin.getUsername(), savedAdmin.getId()));
    }




}
