package phong.demo.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import phong.demo.Entity.Profile;

public interface ProfileRepository extends JpaRepository<Profile,Long>{
    boolean existsByPhone(String phone);


}
