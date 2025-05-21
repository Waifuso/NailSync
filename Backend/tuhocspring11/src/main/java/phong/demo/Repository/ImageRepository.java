package phong.demo.Repository;


import org.springframework.data.jpa.repository.JpaRepository;
import phong.demo.Entity.Image;

import java.util.Optional;

public interface ImageRepository  extends JpaRepository<Image,Long> {

    Optional<Image> findByImageName(String imageName);
    boolean existsByImageName(String imageName);
}
