package az.aztu.egrant.expert.internal;

import az.aztu.egrant.expert.domain.Assessment;
import az.aztu.egrant.expert.domain.Expert;
import az.aztu.egrant.expert.web.dto.AssessmentResponse;
import az.aztu.egrant.expert.web.dto.CreateAssessmentRequest;
import az.aztu.egrant.project.api.ProjectDirectory;
import az.aztu.egrant.shared.error.ConflictException;
import az.aztu.egrant.shared.error.NotFoundException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Assessments recorded against a project by an assigned expert (admin-entered). */
@Service
public class AssessmentService {

    private final AssessmentRepository assessmentRepository;
    private final ExpertAssignmentRepository assignmentRepository;
    private final ExpertRepository expertRepository;
    private final ProjectDirectory projectDirectory;
    private final ExpertMapper mapper;

    public AssessmentService(AssessmentRepository assessmentRepository,
                             ExpertAssignmentRepository assignmentRepository, ExpertRepository expertRepository,
                             ProjectDirectory projectDirectory, ExpertMapper mapper) {
        this.assessmentRepository = assessmentRepository;
        this.assignmentRepository = assignmentRepository;
        this.expertRepository = expertRepository;
        this.projectDirectory = projectDirectory;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<AssessmentResponse> list(Long projectId) {
        requireProject(projectId);
        return assessmentRepository.findByProjectId(projectId).stream().map(this::toResponse).toList();
    }

    /** Records (or updates) the expert's assessment for the project. */
    @Transactional
    public AssessmentResponse submit(Long projectId, CreateAssessmentRequest req) {
        requireProject(projectId);
        Expert expert = expertRepository.findById(req.expertId())
                .orElseThrow(() -> NotFoundException.of("Expert", req.expertId()));
        if (!assignmentRepository.existsByProjectIdAndExpertId(projectId, expert.getId())) {
            throw new ConflictException("This expert is not assigned to the project.");
        }
        Assessment a = assessmentRepository.findByProjectIdAndExpertId(projectId, expert.getId())
                .orElseGet(Assessment::new);
        a.setProjectId(projectId);
        a.setExpertId(expert.getId());
        a.setScore(req.score());
        a.setNote(req.note());
        return toResponse(assessmentRepository.save(a));
    }

    private void requireProject(Long projectId) {
        if (!projectDirectory.existsById(projectId)) {
            throw NotFoundException.of("Project", projectId);
        }
    }

    private AssessmentResponse toResponse(Assessment a) {
        String name = expertRepository.findById(a.getExpertId())
                .map(e -> (e.getName() + " " + e.getSurname()).trim()).orElse(null);
        return mapper.toAssessmentResponse(a, name);
    }
}
