package phong.demo.Service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import phong.demo.Entity.Profile;
import phong.demo.Entity.Ranking;
import phong.demo.Entity.User;
import phong.demo.Repository.ProfileRepository;
import phong.demo.Repository.UserRepository;
import java.util.Optional;



@Service
public class ProfileService {

    @Autowired
    public  ProfileRepository profileRepository;

    @Autowired
    public UserRepository userRepository;

    public ProfileService(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    // receive the id of the user and return the value of their ranking
    public Ranking estimateRanking(long id){

        Optional<User> curUser = userRepository.findById(id);

        if (curUser.isEmpty()){
            throw new EntityNotFoundException(" Can not find the user by their id");
        }

        User user = curUser.get();

        Profile profile = user.getProfile();

        Integer usage  = user.getTotalSpend();

         if (usage >= 300 && usage < 700) {
            return Ranking.SILVER;
            
        }else if (usage >= 700 && usage < 1500){
            return Ranking.GOLD;
            
        } else if (usage >= 1500 && usage < 2000) {
            return  Ranking.PLATINUM;

        } else if (usage >= 2000 ) {

            return Ranking.DIAMOND;

        }
        return Ranking.BRONZE;
    }

    // update the new amount and save the user to the database
    public void updateSpending(Integer amount,long id ){

        Optional<User> Cur_User = userRepository.findById(id);

        if ( Cur_User.isEmpty()){

            throw new EntityNotFoundException("Can not find the people by the id ");

        }

        User user = Cur_User.get();

        Integer Update_amount = user.getTotalSpend() + amount;

        user.setTotalSpend(Update_amount);

        User Saved_User = userRepository.save(user);
    }


    // helper method for saving the profile
    public void saveProfile(Profile Requestprofile){

        Profile profile = profileRepository.save(Requestprofile);

    }

    // the function to get the remaning point to the next ranking
    public String nextRankingestimate(Integer total, Ranking ranking ){

        if (total >= 2000){

            return "You are currently at the highest ranking of us !!!!";
        }
        else{
            return " you are currently: "  + (ranking.getUpperbound()-total+1) + " points away from the next Ranking";
        }

    }





}
