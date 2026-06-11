package az.aztu.egrant.expert.internal;

import az.aztu.egrant.expert.domain.Assessment;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssessmentRepository extends JpaRepository<Assessment, Long> {

    List<Assessment> findByProjectId(Long projectId);

    Optional<Assessment> findByProjectIdAndExpertId(Long projectId, Long expertId);
}
