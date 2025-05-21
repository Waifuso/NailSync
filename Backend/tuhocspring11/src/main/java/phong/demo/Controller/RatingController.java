package phong.demo.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import phong.demo.DTO.RatingDTO;
import phong.demo.Entity.Appointment;
import phong.demo.Entity.AppointmentService;
import phong.demo.Entity.Rating;
import phong.demo.Repository.AppointmentRepository;
import phong.demo.Repository.RatingRepository;
import phong.demo.Repository.UserRepository;
import phong.demo.Springpro.ResponseMessagge;

import javax.swing.text.html.Option;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(path = "/api/rating")
public class RatingController {

    @Autowired
    private final UserRepository userRepository;
    private final AppointmentRepository appointmentRepository;
    private final RatingRepository ratingRepository;

    public RatingController(UserRepository userRepository, AppointmentRepository appointmentRepository, RatingRepository ratingRepository) {
        this.userRepository = userRepository;
        this.appointmentRepository = appointmentRepository;
        this.ratingRepository = ratingRepository;
    }

    @GetMapping(path = "/get/allReviews")
    public List<RatingDTO> gettingAllReviews(){

        List<RatingDTO> ratingDTOList = new ArrayList<>();

        List<Rating> ratingList = ratingRepository.findAll();

        for (Rating rating: ratingList) {
            RatingDTO ratingDTO = new RatingDTO();
            ratingDTO.setRating(rating);
            ratingDTO.setDate(rating.getAppointment().getDate());
            List<String> serviceName = new ArrayList<>();
            String employee = "";
            for (AppointmentService appointmentService : rating.getAppointment().getAppointmentServices()) {
                serviceName.add(appointmentService.getService().getService_name());
                employee = appointmentService.getEmployee().getUsername();
            }
            ratingDTO.setServiceName(serviceName);
            ratingDTO.setEmployee(employee);
            ratingDTOList.add(ratingDTO);
        }
        return ratingDTOList;
    }


    @PostMapping(path = "/rate/{star}/{comment}/{appointmentId}")
    public ResponseEntity<?> ratingByCustomer (@PathVariable Integer star, @PathVariable String comment, @PathVariable Long appointmentId) {
        if (comment == null) {
            return ResponseEntity.badRequest().body(new ResponseMessagge("Please fill the comment"));
        } else if (star == null) {
            return ResponseEntity.badRequest().body(new ResponseMessagge("Please fill up the star for rating"));
        }

        Optional<Appointment> findAppointment = appointmentRepository.findById(appointmentId);
        Optional<Rating> findAppointmentRating = ratingRepository.findByAppointmentId(appointmentId);

        if (appointmentId == null || findAppointment.isEmpty()) {
            return ResponseEntity.badRequest().body(new ResponseMessagge("Couldn't find the appointment"));
        } else if (findAppointmentRating.isPresent()) {
            return ResponseEntity.badRequest().body(new ResponseMessagge("You have rated this appointment before!"));
        }
        Rating rating = new Rating();
        rating.setStar(star);
        rating.setComment(comment);
        rating.setAppointment(findAppointment.get());

        ratingRepository.save(rating);

        return ResponseEntity.ok(new ResponseMessagge("Thank you for your rating"));
    }
}
