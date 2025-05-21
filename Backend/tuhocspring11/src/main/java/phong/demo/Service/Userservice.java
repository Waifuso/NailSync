package phong.demo.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import phong.demo.Entity.User;
import phong.demo.Repository.UserRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class Userservice {


    @Autowired
    private UserRepository userRepository;


    public int findNewUser(LocalDate startDate, LocalDate EndDate){

        Integer newuser = 0;

        LocalDate now = startDate;
        while(!now.isAfter(EndDate)){


            LocalDate current = now;
            int totalNewRegister = (int) userRepository.findAll().stream().filter(h->h.getJoinedDate().equals(current)).count();


            newuser += totalNewRegister;

            now = now.plusDays(1);


        }

        return newuser;
    }


}
