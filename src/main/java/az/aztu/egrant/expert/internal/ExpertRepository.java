package az.aztu.egrant.expert.internal;

import az.aztu.egrant.expert.domain.Expert;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpertRepository extends JpaRepository<Expert, Long> {

    List<Expert> findAllByOrderBySurnameAscNameAsc();

    boolean existsByEmail(String email);

    boolean existsByPersonalIdSerialNumber(String personalIdSerialNumber);
}
