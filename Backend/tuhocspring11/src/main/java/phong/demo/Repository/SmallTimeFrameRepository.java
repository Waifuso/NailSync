package phong.demo.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import phong.demo.Entity.SmallTimeframe;

public interface SmallTimeFrameRepository extends JpaRepository <SmallTimeframe, Long> {

}
