package az.aztu.egrant.expert.internal;

import az.aztu.egrant.expert.api.ExpertAssigned;
import az.aztu.egrant.expert.domain.AssignmentStatus;
import az.aztu.egrant.expert.domain.Expert;
import az.aztu.egrant.expert.domain.ExpertAssignment;
import az.aztu.egrant.expert.web.dto.AssignmentResponse;
import az.aztu.egrant.project.api.ProjectDirectory;
import az.aztu.egrant.project.api.ProjectReview;
import az.aztu.egrant.project.api.ProjectSummary;
import az.aztu.egrant.shared.error.ConflictException;
import az.aztu.egrant.shared.error.NotFoundException;
import java.time.Instant;
import java.util.List;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Project↔expert assignments (admin). Allowed only after submission; advances project to review. */
@Service
public class ExpertAssignmentService {

    private final ExpertAssignmentRepository assignmentRepository;
    private final ExpertRepository expertRepository;
    private final ExpertMapper mapper;
    private final ProjectDirectory projectDirectory;
    private final ProjectReview projectReview;
    private final ApplicationEventPublisher events;

    public ExpertAssignmentService(ExpertAssignmentRepository assignmentRepository,
                                   ExpertRepository expertRepository, ExpertMapper mapper,
                                   ProjectDirectory projectDirectory, ProjectReview projectReview,
                                   ApplicationEventPublisher events) {
        this.assignmentRepository = assignmentRepository;
        this.expertRepository = expertRepository;
        this.mapper = mapper;
        this.projectDirectory = projectDirectory;
        this.projectReview = projectReview;
        this.events = events;
    }

    @Transactional(readOnly = true)
    public List<AssignmentResponse> list(Long projectId) {
        requireProject(projectId);
        return assignmentRepository.findByProjectId(projectId).stream().map(this::toResponse).toList();
    }

    @Transactional
    public AssignmentResponse assign(Long projectId, Long expertId) {
        ProjectSummary project = requireProject(projectId);
        if (!isAssignable(project.status())) {
            throw new ConflictException("An expert can be assigned only after the project is submitted.");
        }
        Expert expert = expertRepository.findById(expertId)
                .orElseThrow(() -> NotFoundException.of("Expert", expertId));
        if (assignmentRepository.existsByProjectIdAndExpertId(projectId, expertId)) {
            throw new ConflictException("This expert is already assigned to the project.");
        }
        ExpertAssignment a = new ExpertAssignment();
        a.setProjectId(projectId);
        a.setExpertId(expertId);
        a.setStatus(AssignmentStatus.ASSIGNED);
        a.setAssignedAt(Instant.now());
        ExpertAssignment saved = assignmentRepository.save(a);

        projectReview.markUnderReview(projectId); // SUBMITTED -> UNDER_REVIEW
        events.publishEvent(new ExpertAssigned(projectId, project.projectCode(), expertId,
                expert.getEmail(), fullName(expert)));
        return toResponse(saved);
    }

    @Transactional
    public AssignmentResponse updateStatus(Long projectId, Long assignmentId, AssignmentStatus status) {
        ExpertAssignment a = requireAssignment(projectId, assignmentId);
        a.setStatus(status);
        if (status != AssignmentStatus.ASSIGNED) {
            a.setRespondedAt(Instant.now());
        }
        return toResponse(a);
    }

    @Transactional
    public void remove(Long projectId, Long assignmentId) {
        assignmentRepository.delete(requireAssignment(projectId, assignmentId));
    }

    private ProjectSummary requireProject(Long projectId) {
        return projectDirectory.findById(projectId)
                .orElseThrow(() -> NotFoundException.of("Project", projectId));
    }

    private ExpertAssignment requireAssignment(Long projectId, Long assignmentId) {
        ExpertAssignment a = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> NotFoundException.of("Assignment", assignmentId));
        if (!a.getProjectId().equals(projectId)) {
            throw new ConflictException("Assignment " + assignmentId + " does not belong to project " + projectId);
        }
        return a;
    }

    private boolean isAssignable(String status) {
        return "SUBMITTED".equals(status) || "UNDER_REVIEW".equals(status);
    }

    private AssignmentResponse toResponse(ExpertAssignment a) {
        String name = expertRepository.findById(a.getExpertId()).map(this::fullName).orElse(null);
        return mapper.toAssignmentResponse(a, name);
    }

    private String fullName(Expert e) {
        return (e.getName() + " " + e.getSurname()).trim();
    }
}
