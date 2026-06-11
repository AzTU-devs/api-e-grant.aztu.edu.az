package az.aztu.egrant.project.internal;

import az.aztu.egrant.project.api.ProjectDetail;
import az.aztu.egrant.project.api.ProjectDirectory;
import az.aztu.egrant.project.api.ProjectSummary;
import az.aztu.egrant.project.domain.ProjectStatus;
import az.aztu.egrant.shared.error.BadRequestException;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** {@link ProjectDirectory} implementation exposed to other modules (read-only, excludes soft-deleted). */
@Service
@Transactional(readOnly = true)
public class ProjectDirectoryImpl implements ProjectDirectory {

    private final ProjectRepository repository;
    private final ProjectMapper mapper;

    public ProjectDirectoryImpl(ProjectRepository repository, ProjectMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public boolean existsById(Long projectId) {
        return projectId != null && repository.existsByIdAndDeletedAtIsNull(projectId);
    }

    @Override
    public Optional<ProjectSummary> findById(Long projectId) {
        return repository.findByIdAndDeletedAtIsNull(projectId).map(mapper::toSummary);
    }

    @Override
    public Optional<ProjectSummary> findByCode(Long projectCode) {
        return repository.findByProjectCodeAndDeletedAtIsNull(projectCode).map(mapper::toSummary);
    }

    @Override
    public Optional<ProjectDetail> findDetailById(Long projectId) {
        return repository.findByIdAndDeletedAtIsNull(projectId).map(mapper::toDetail);
    }

    @Override
    public Optional<ProjectDetail> findDetailByCode(Long projectCode) {
        return repository.findByProjectCodeAndDeletedAtIsNull(projectCode).map(mapper::toDetail);
    }

    @Override
    public List<ProjectSummary> findByStatus(String status) {
        ProjectStatus parsed;
        try {
            parsed = ProjectStatus.valueOf(status);
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Unknown project status: " + status);
        }
        return repository.findByStatusAndDeletedAtIsNullOrderBySubmittedAtDesc(parsed).stream()
                .map(mapper::toSummary).toList();
    }
}
