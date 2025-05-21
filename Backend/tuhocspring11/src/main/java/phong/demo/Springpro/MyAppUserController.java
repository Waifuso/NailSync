package phong.demo.Springpro;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import phong.demo.Entity.User;
import phong.demo.Repository.UserRepository;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(path = "/api/signup")
public class MyAppUserController {

    @Autowired
    private final UserRepository myAppUserRepository;
    private SendEmailService sendEmailService;
    private RandomNumberService randomNumberService;
    private String success = "{\"message\":\"sign up success\"}";
    private String failure = "{\"message\":\"sign up failure\"}";
    private String succesDel = "{\"message\":\"delete success\"}";
    private String failureDel = "{\"message\":\"delete failure\"}";

    public MyAppUserController(UserRepository myAppUserRepository, SendEmailService sendEmailService, RandomNumberService randomNumberService) {
        this.myAppUserRepository = myAppUserRepository;
        this.sendEmailService = sendEmailService;
        this.randomNumberService = randomNumberService;
    }

    @PostMapping (path = "/resetPassword")
    public ResponseEntity<ResponseMessagge> sendResetCode (@RequestBody User user ) {
        if (user == null || user.getEmail() == null) {
            return ResponseEntity.badRequest().body(new ResponseMessagge(failure));
        }
        Optional<User> existingEmail = myAppUserRepository.findByEmail(user.getEmail());
        if (existingEmail.isEmpty()) {
            return ResponseEntity.badRequest().body(new ResponseMessagge("Email not found"));
        }
        int resetCode = randomNumberService.generateRandomNumber(100);

        user = existingEmail.get();

        user.setResetNums(resetCode);

        myAppUserRepository.save(user);

        String subject = "Vertification from NAILSYNC";
        String body = "Your vertification code is: " + resetCode;

        sendEmailService.SendEmail(user.getEmail(), body, subject);

        return ResponseEntity.ok( new ResponseMessagge("Vertification code sent. Check your email."));


    }

    @PostMapping(path = "/enterCode")
    public ResponseEntity<ResponseMessagge> enterResetCode(@RequestBody User user) {
        if (user == null || user.getEmail() == null || user.getResetNums() == 0 || user.getResetNums() == null) {
            return ResponseEntity.badRequest().body( new ResponseMessagge("Email and reset code are required."));
        }

        Optional<User> existingUser = myAppUserRepository.findByEmail(user.getEmail());
        //Optional<MyAppUser> existingCode = myAppUserRepository.findByResetNums(user.getResetNums());

        if (existingUser.isPresent()) {
            User foundUser = existingUser.get();

            // Compare Integer values correctly
            if (foundUser.getResetNums().equals(user.getResetNums())) {
                return ResponseEntity.ok().body(new ResponseMessagge ("Great! Code is correct."));
            } else {
                return ResponseEntity.badRequest().body( new ResponseMessagge("Not correct"));
            }
        } else {
            return ResponseEntity.badRequest().body( new ResponseMessagge("User not found"));
        }
    }





    @GetMapping(path = "/get")
    List<User> getAllPersons(){
        return myAppUserRepository.findAll();
    }

    @PostMapping(path = "/reg")
    public ResponseEntity<ResponseMessagge> createUser(@RequestBody User user) {
        if (user == null || user.getEmail() == null) {
            return ResponseEntity.badRequest().body(new ResponseMessagge(failure));
        }

        // Check if the email already exists in the database
        Optional<User> existingEmail = myAppUserRepository.findByEmail(user.getEmail());
        Optional<User> existingName = myAppUserRepository.findByUsername(user.getUsername());
        if (existingEmail.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ResponseMessagge("{\"Email already exists. Please log in.\"}"));

        } else if (existingName.isPresent())
        {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ResponseMessagge("{\"Username already exists. Please log in or change your username.\"}"));

        }

        // Save the new user
        User user1 = myAppUserRepository.save(user);
        return ResponseEntity.ok(new ResponseMessagge(success));
    }

    @DeleteMapping(path = "/del")
    public ResponseEntity<ResponseMessagge> deleteUser(@RequestBody User user) {
        if (user == null || (user.getEmail() == null && user.getUsername() == null)) {
            return ResponseEntity.badRequest().body( new ResponseMessagge(failureDel));
        }

        Optional<User> existingUserOrEmail = Optional.empty();

        // Find user by email if provided
        if (user.getEmail() != null) {
            existingUserOrEmail = myAppUserRepository.findByEmail(user.getEmail());
        }

        // If not found by email, try finding by username
        if (existingUserOrEmail.isEmpty() && user.getUsername() != null) {
            existingUserOrEmail = myAppUserRepository.findByUsername(user.getUsername());
        }

        if (existingUserOrEmail.isPresent()) {
            myAppUserRepository.delete(existingUserOrEmail.get());
            return ResponseEntity.ok( new ResponseMessagge(succesDel));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body( new ResponseMessagge(failureDel));
        }
    }





}

