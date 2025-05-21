package phong.demo.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import phong.demo.Entity.Notification;

import java.util.List;
import java.util.Optional;

public interface NotificationImageRepository extends JpaRepository<Notification, Long> {

    @Query("SELECT n.imageURL FROM Notification n")
    List<String> findAllImageUrls();

    Optional<Notification> findByCaption(String caption);

}
