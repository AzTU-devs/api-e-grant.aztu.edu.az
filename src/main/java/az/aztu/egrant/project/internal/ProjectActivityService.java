package az.aztu.egrant.project.internal;

import az.aztu.egrant.project.domain.Project;
import az.aztu.egrant.project.domain.ProjectActivity;
import az.aztu.egrant.project.web.dto.ActivityResponse;
import az.aztu.egrant.project.web.dto.CreateActivityRequest;
import az.aztu.egrant.project.web.dto.UpdateActivityRequest;
import az.aztu.egrant.shared.error.ConflictException;
import az.aztu.egrant.shared.error.ForbiddenException;
import az.aztu.egrant.shared.error.NotFoundException;
import az.aztu.egrant.shared.security.AuthenticatedUser;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Monthly activity-plan management for a project. */
@Service
public class ProjectActivityService {

    private final ProjectRepository projectRepository;
    private final ProjectActivityRepository activityRepository;
    private final ActivityMapper mapper;

    public ProjectActivityService(ProjectRepository projectRepository,
                                  ProjectActivityRepository activityRepository, ActivityMapper mapper) {
        this.projectRepository = projectRepository;
        this.activityRepository = activityRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<ActivityResponse> list(Long projectId) {
        requireProject(projectId);
        return activityRepository.findByProjectIdOrderByMonthAsc(projectId).stream()
                .map(mapper::toResponse).toList();
    }

    @Transactional
    public ActivityResponse create(Long projectId, AuthenticatedUser actor, CreateActivityRequest req) {
        Project project = requireProject(projectId);
        requireOwnerOrAdmin(project, actor);
        ProjectActivity a = new ProjectActivity();
        a.setProjectId(projectId);
        a.setMonth(req.month());
        a.setActivityName(req.activityName());
        return mapper.toResponse(activityRepository.save(a));
    }

    @Transactional
    public ActivityResponse update(Long projectId, Long activityId, AuthenticatedUser actor,
                                   UpdateActivityRequest req) {
        Project project = requireProject(projectId);
        requireOwnerOrAdmin(project, actor);
        ProjectActivity a = requireActivity(projectId, activityId);
        if (req.month() != null) a.setMonth(req.month());
        if (req.activityName() != null) a.setActivityName(req.activityName());
        return mapper.toResponse(a);
    }

    @Transactional
    public void delete(Long projectId, Long activityId, AuthenticatedUser actor) {
        Project project = requireProject(projectId);
        requireOwnerOrAdmin(project, actor);
        activityRepository.delete(requireActivity(projectId, activityId));
    }

    @Transactional
    public void deleteByMonth(Long projectId, Integer month, AuthenticatedUser actor) {
        Project project = requireProject(projectId);
        requireOwnerOrAdmin(project, actor);
        activityRepository.deleteAll(activityRepository.findByProjectIdAndMonth(projectId, month));
    }

    private Project requireProject(Long projectId) {
        return projectRepository.findByIdAndDeletedAtIsNull(projectId)
                .orElseThrow(() -> NotFoundException.of("Project", projectId));
    }

    private ProjectActivity requireActivity(Long projectId, Long activityId) {
        ProjectActivity a = activityRepository.findById(activityId)
                .orElseThrow(() -> NotFoundException.of("Activity", activityId));
        if (!a.getProjectId().equals(projectId)) {
            throw new ConflictException("Activity " + activityId + " does not belong to project " + projectId);
        }
        return a;
    }

    private void requireOwnerOrAdmin(Project project, AuthenticatedUser actor) {
        boolean admin = "ADMIN".equals(actor.role()) || "SUPER_ADMIN".equals(actor.role());
        if (!project.getOwnerId().equals(actor.userId()) && !admin) {
            throw new ForbiddenException("Only the project owner or an admin may manage activities.");
        }
    }
}
