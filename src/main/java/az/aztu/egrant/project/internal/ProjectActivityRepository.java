package az.aztu.egrant.project.internal;

import az.aztu.egrant.project.domain.ProjectActivity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectActivityRepository extends JpaRepository<ProjectActivity, Long> {

    List<ProjectActivity> findByProjectIdOrderByMonthAsc(Long projectId);

    List<ProjectActivity> findByProjectIdAndMonth(Long projectId, Integer month);
}
