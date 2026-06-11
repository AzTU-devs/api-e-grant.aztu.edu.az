package az.aztu.egrant.project.internal;

import az.aztu.egrant.project.domain.Project;
import az.aztu.egrant.project.domain.ProjectStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    Optional<Project> findByIdAndDeletedAtIsNull(Long id);

    Optional<Project> findByProjectCodeAndDeletedAtIsNull(Long projectCode);

    boolean existsByProjectCode(Long projectCode);

    boolean existsByIdAndDeletedAtIsNull(Long id);

    Page<Project> findByDeletedAtIsNull(Pageable pageable);

    Page<Project> findByStatusAndDeletedAtIsNull(ProjectStatus status, Pageable pageable);

    Page<Project> findByOwnerIdAndDeletedAtIsNull(Long ownerId, Pageable pageable);

    Page<Project> findByOwnerIdAndStatusAndDeletedAtIsNull(Long ownerId, ProjectStatus status, Pageable pageable);

    List<Project> findByStatusAndDeletedAtIsNullOrderBySubmittedAtDesc(ProjectStatus status);
}
