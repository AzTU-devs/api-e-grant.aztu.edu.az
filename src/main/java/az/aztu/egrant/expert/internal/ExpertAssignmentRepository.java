package az.aztu.egrant.expert.internal;

import az.aztu.egrant.expert.domain.ExpertAssignment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpertAssignmentRepository extends JpaRepository<ExpertAssignment, Long> {

    List<ExpertAssignment> findByProjectId(Long projectId);

    boolean existsByProjectIdAndExpertId(Long projectId, Long expertId);
}
