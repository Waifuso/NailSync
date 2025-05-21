package phong.demo.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import phong.demo.Entity.Service;

import java.util.Optional;

public interface ServiceRepository extends JpaRepository<Service,Long> {

    Optional<Service> findByServiceName(String serviceName);

    boolean existsByServiceName(String serviceName);




}
