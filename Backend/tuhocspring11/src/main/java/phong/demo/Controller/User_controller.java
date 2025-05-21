package phong.demo.Controller;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import phong.demo.DTO.JSOn_objectString;
import phong.demo.DTO.User_DTO;
import phong.demo.Entity.Profile;
import phong.demo.Entity.Ranking;
import phong.demo.Entity.User;
import phong.demo.Repository.EmployeeReposittory;
import phong.demo.Repository.ProfileRepository;
import phong.demo.Repository.UserRepository;
import phong.demo.Service.ProfileService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class User_controller {

    @Autowired
    private  UserRepository userRepository;

    @Autowired
    private EmployeeReposittory employeeReposittory;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private ProfileService profileService;

    @GetMapping("/Next/ranking/{id}")
    public ResponseEntity<?> nextRanking(@PathVariable long id){

        Optional<User> Cur_user = userRepository.findById(id);

        if (Cur_user.isEmpty()){

            return ResponseEntity.notFound().build();

        }

        User user = Cur_user.get();

        Integer spended = user.getTotalSpend();

        String goal = profileService.nextRankingestimate(spended,user.getProfile().getRanking());


        return ResponseEntity.ok(goal);
    }



    // Find people by Id
    @GetMapping("/{id}")
    public ResponseEntity<?> FindById(@PathVariable long id) {

        Optional<User> curUser = userRepository.findById(id);

        if(curUser.isEmpty()){

            return ResponseEntity.notFound().build();
        }
        User user = curUser.get();

        User_DTO userDto = new User_DTO(user.getUsername(),user.getEmail(),user.getJoinedDate(),user.getProfile());


        return ResponseEntity.ok(userDto);
    }

    // return the list of all people
    @GetMapping("")
    public List<User> Allpeople() {
        return userRepository.findAll();
    }


    // find the user by the user name
    @GetMapping("/username/{username}")
    public ResponseEntity<User> findByName(@PathVariable String username) {

        Optional<User> curUser = userRepository.findByUsername(username);

        if (curUser.isEmpty()) {

            return ResponseEntity.notFound().build();
        }
        User user = curUser.get();

        return ResponseEntity.ok(user);
    }

    // sign up
    @PostMapping
    public User CreateteUser(@RequestBody User user) {
        return userRepository.save(user);
    }







    // Edit the user by there user name
    @PutMapping("/{id}")
    public ResponseEntity<?>EditUser(@PathVariable long id ,@RequestBody User Updateduser) {

        Optional<User> CurUser = userRepository.findById(id);

        if (CurUser.isEmpty()) {

            return ResponseEntity.notFound().build();
        }

        User user = CurUser.get();
        //check if the Updatee's username is not null

        if(userRepository.existsByUsername(Updateduser.getUsername())){

            return ResponseEntity.badRequest().body(new JSOn_objectString("The username has existed."));

        }

//       //check if the Updatee's username is not null then set the username as the Update name
        if (Updateduser.getUsername() != null) {
            user.setUsername(Updateduser.getUsername());

        }
        // check if the Updatee's password is not null
        if (Updateduser.getPassword() != null) {
            // check if the current password is the same as the previous one
            if(user.getPassword().equals(Updateduser.getPassword())){

                return ResponseEntity.badRequest().body(new JSOn_objectString("New password cannot be the same as the previous password."));
            }

            user.setPassword(Updateduser.getPassword());

        }

        // check if the email has already been used or not

        if(userRepository.existsByEmail(Updateduser.getEmail())){

            return ResponseEntity.badRequest().body(new JSOn_objectString(" The email has already existed"));
        }


        // check if  the email is not null then set the email as request
        if (Updateduser.getEmail() != null) {

            user.setEmail(Updateduser.getEmail());
        }

        User Saveduser = userRepository.save(user);

        return ResponseEntity.ok(new JSOn_objectString("Success"));

    }

    @GetMapping("/Profile/{id}")
    public  ResponseEntity<?>GetProfile(@PathVariable long id){
        Optional<User> Cur_user = userRepository.findById(id);

        if (Cur_user.isEmpty()){

            return ResponseEntity.notFound().build();

        }

        User user = Cur_user.get();

        Profile profile = user.getProfile();

        return ResponseEntity.ok(profile);
    }


    //create the profile
    @PostMapping("/Profile/create/{id}")

    public ResponseEntity<?> createProfile(@PathVariable long id,@RequestBody Profile updateProfile){

        Optional<User> Cur_user = userRepository.findById(id);

        if (Cur_user.isEmpty()){

            return ResponseEntity.notFound().build();

        }

        User user = Cur_user.get();

        Profile profile = new Profile();

        if(updateProfile == null){

            return ResponseEntity.badRequest().body(" the profile is null");
        }

        if(profileRepository.existsByPhone(updateProfile.getPhone())){

            return ResponseEntity.badRequest().body(new JSOn_objectString(" the phone number of the profile has already exist"));
        }
        // check if the phone number is not null then set it to the phone number of the user
        if (updateProfile.getPhone()!= null ){

            profile.setPhone(updateProfile.getPhone());
        }
        // check if the first name is not null then set it to the first name  of the user
        if (updateProfile.getFirstName()!= null){

            profile.setFirstName(updateProfile.getFirstName());
        }

        if(updateProfile.getLastName()!= null){

            profile.setLastName(updateProfile.getLastName());
        }

        Ranking ranking = profileService.estimateRanking(user.getId());

        profile.setRanking(ranking);

        profile.setUser(user);

        user.setProfile(profile);

       Profile saveProfile =  profileRepository.save(profile);

        return ResponseEntity.ok(new JSOn_objectString("Successfully save the profile"));
    }







    // update the profile money amount
    // check the id then update the amount of the money straight to the Total balance
    @PutMapping("/Test/id/{id}/money/{money}")
    public ResponseEntity<?>testUpdate(@PathVariable long id,@PathVariable Integer money){

       Optional<Profile> Cur_profile = profileRepository.findById(id);

       Profile profile = Cur_profile.get();

        try {
            profileService.updateSpending(money,id);

            Ranking ranking = profileService.estimateRanking(id);

            profile.setRanking(ranking);

            Profile profile1 = profileRepository.save(profile);

            return  ResponseEntity.ok("Successfully");
        } catch (EntityNotFoundException e) {

            return  ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }

    }


    @DeleteMapping("/{id}")

    public void Deleteprofile(@PathVariable long id){

        profileRepository.deleteById(id);
    }






}
