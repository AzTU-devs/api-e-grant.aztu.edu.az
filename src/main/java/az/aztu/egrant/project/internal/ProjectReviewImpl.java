package az.aztu.egrant.project.internal;

import az.aztu.egrant.project.api.ProjectReview;
import az.aztu.egrant.project.domain.ProjectStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** {@link ProjectReview} implementation: lets {@code expert} advance a submitted project into review. */
@Service
public class ProjectReviewImpl implements ProjectReview {

    private final ProjectRepository repository;

    public ProjectReviewImpl(ProjectRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public void markUnderReview(Long projectId) {
        repository.findByIdAndDeletedAtIsNull(projectId).ifPresent(p -> {
            if (p.getStatus() == ProjectStatus.SUBMITTED) {
                p.setStatus(ProjectStatus.UNDER_REVIEW);
            }
        });
    }
}
