package az.aztu.egrant.project.internal;

import az.aztu.egrant.institution.api.InstitutionDirectory;
import az.aztu.egrant.institution.api.InstitutionSummary;
import az.aztu.egrant.iam.api.UserDirectory;
import az.aztu.egrant.iam.api.UserSummary;
import az.aztu.egrant.priority.api.PriorityDirectory;
import az.aztu.egrant.priority.api.PrioritySummary;
import az.aztu.egrant.project.api.ProjectSubmissionContext;
import az.aztu.egrant.project.api.ProjectSubmissionGuard;
import az.aztu.egrant.project.api.ProjectSubmitted;
import az.aztu.egrant.project.domain.MemberRole;
import az.aztu.egrant.project.domain.MemberStatus;
import az.aztu.egrant.project.domain.Project;
import az.aztu.egrant.project.domain.ProjectMember;
import az.aztu.egrant.project.domain.ProjectStatus;
import az.aztu.egrant.project.web.dto.CreateProjectRequest;
import az.aztu.egrant.project.web.dto.ProjectListItem;
import az.aztu.egrant.project.web.dto.ProjectResponse;
import az.aztu.egrant.project.web.dto.UpdateProjectRequest;
import az.aztu.egrant.shared.error.BadRequestException;
import az.aztu.egrant.shared.error.ConflictException;
import az.aztu.egrant.shared.error.ForbiddenException;
import az.aztu.egrant.shared.error.NotFoundException;
import az.aztu.egrant.shared.security.AuthenticatedUser;
import java.time.Instant;
import java.util.List;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Project lifecycle: CRUD, lock/cap-gated submission (via {@link ProjectSubmissionGuard}s), approve/reject. */
@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository memberRepository;
    private final ProjectCodeGenerator codeGenerator;
    private final ProjectMapper mapper;
    private final UserDirectory userDirectory;
    private final InstitutionDirectory institutionDirectory;
    private final PriorityDirectory priorityDirectory;
    private final ApplicationEventPublisher events;
    private final List<ProjectSubmissionGuard> submissionGuards;

    public ProjectService(ProjectRepository projectRepository, ProjectMemberRepository memberRepository,
                          ProjectCodeGenerator codeGenerator, ProjectMapper mapper, UserDirectory userDirectory,
                          InstitutionDirectory institutionDirectory, PriorityDirectory priorityDirectory,
                          ApplicationEventPublisher events, List<ProjectSubmissionGuard> submissionGuards) {
        this.projectRepository = projectRepository;
        this.memberRepository = memberRepository;
        this.codeGenerator = codeGenerator;
        this.mapper = mapper;
        this.userDirectory = userDirectory;
        this.institutionDirectory = institutionDirectory;
        this.priorityDirectory = priorityDirectory;
        this.events = events;
        this.submissionGuards = submissionGuards;
    }

    @Transactional
    public ProjectResponse create(Long ownerId, CreateProjectRequest req) {
        validateInstitution(req.institutionId());
        validatePriority(req.priorityId());

        Project p = new Project();
        p.setProjectCode(codeGenerator.next());
        p.setOwnerId(ownerId);
        p.setInstitutionId(req.institutionId());
        p.setPriorityId(req.priorityId());
        applyContent(p, req.projectName(), req.projectPurpose(), req.annotation(), req.keyWords(),
                req.scientificIdea(), req.structure(), req.teamCharacterization(), req.monitoringPlan(),
                req.assessmentPlan(), req.requirements());
        p.setDeadline(req.deadline());
        p.setStatus(ProjectStatus.DRAFT);
        Project saved = projectRepository.save(p);

        // The owner is modelled as an APPROVED member with role OWNER (schema §6.2).
        Instant now = Instant.now();
        ProjectMember owner = new ProjectMember();
        owner.setProjectId(saved.getId());
        owner.setUserId(ownerId);
        owner.setRole(MemberRole.OWNER);
        owner.setStatus(MemberStatus.APPROVED);
        owner.setJoinedAt(now);
        owner.setApprovedAt(now);
        memberRepository.save(owner);

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public ProjectResponse get(Long id) {
        return toResponse(require(id));
    }

    @Transactional(readOnly = true)
    public ProjectResponse getByCode(Long projectCode) {
        Project p = projectRepository.findByProjectCodeAndDeletedAtIsNull(projectCode)
                .orElseThrow(() -> NotFoundException.of("Project", projectCode));
        return toResponse(p);
    }

    @Transactional(readOnly = true)
    public Page<ProjectListItem> list(ProjectStatus status, Long ownerId, Pageable pageable) {
        Page<Project> page;
        if (ownerId != null && status != null) {
            page = projectRepository.findByOwnerIdAndStatusAndDeletedAtIsNull(ownerId, status, pageable);
        } else if (ownerId != null) {
            page = projectRepository.findByOwnerIdAndDeletedAtIsNull(ownerId, pageable);
        } else if (status != null) {
            page = projectRepository.findByStatusAndDeletedAtIsNull(status, pageable);
        } else {
            page = projectRepository.findByDeletedAtIsNull(pageable);
        }
        return page.map(mapper::toListItem);
    }

    @Transactional
    public ProjectResponse update(Long id, AuthenticatedUser actor, UpdateProjectRequest req) {
        Project p = require(id);
        requireOwnerOrAdmin(p, actor);
        if (p.getStatus() != ProjectStatus.DRAFT && !isAdmin(actor)) {
            throw new ConflictException("Only draft projects can be edited.");
        }
        if (req.institutionId() != null) {
            validateInstitution(req.institutionId());
            p.setInstitutionId(req.institutionId());
        }
        if (req.priorityId() != null) {
            validatePriority(req.priorityId());
            p.setPriorityId(req.priorityId());
        }
        if (req.projectName() != null) p.setProjectName(req.projectName());
        if (req.projectPurpose() != null) p.setProjectPurpose(req.projectPurpose());
        if (req.annotation() != null) p.setAnnotation(req.annotation());
        if (req.keyWords() != null) p.setKeyWords(req.keyWords());
        if (req.scientificIdea() != null) p.setScientificIdea(req.scientificIdea());
        if (req.structure() != null) p.setStructure(req.structure());
        if (req.teamCharacterization() != null) p.setTeamCharacterization(req.teamCharacterization());
        if (req.monitoringPlan() != null) p.setMonitoringPlan(req.monitoringPlan());
        if (req.assessmentPlan() != null) p.setAssessmentPlan(req.assessmentPlan());
        if (req.requirements() != null) p.setRequirements(req.requirements());
        if (req.deadline() != null) p.setDeadline(req.deadline());
        return toResponse(p);
    }

    @Transactional
    public void delete(Long id, AuthenticatedUser actor) {
        Project p = require(id);
        requireOwnerOrAdmin(p, actor);
        p.setDeletedAt(Instant.now());
    }

    @Transactional
    public ProjectResponse submit(Long id, AuthenticatedUser actor) {
        Project p = require(id);
        requireOwnerOrAdmin(p, actor);
        if (p.getStatus() != ProjectStatus.DRAFT) {
            throw new ConflictException("Only draft projects can be submitted.");
        }
        if (!userDirectory.isProfileCompleted(p.getOwnerId())) {
            throw new ForbiddenException("The owner profile must be completed before submitting.");
        }
        ProjectSubmissionContext context = new ProjectSubmissionContext(
                p.getId(), p.getProjectCode(), p.getOwnerId(), p.getMaxBudgetAmount());
        submissionGuards.forEach(guard -> guard.check(context)); // system lock, budget cap, …

        p.setStatus(ProjectStatus.SUBMITTED);
        p.setSubmittedAt(Instant.now());
        events.publishEvent(new ProjectSubmitted(p.getId(), p.getProjectCode(), p.getOwnerId()));
        return toResponse(p);
    }

    @Transactional
    public ProjectResponse approve(Long id) {
        return decide(id, ProjectStatus.APPROVED);
    }

    @Transactional
    public ProjectResponse reject(Long id) {
        return decide(id, ProjectStatus.REJECTED);
    }

    private ProjectResponse decide(Long id, ProjectStatus decision) {
        Project p = require(id);
        if (p.getStatus() != ProjectStatus.SUBMITTED && p.getStatus() != ProjectStatus.UNDER_REVIEW) {
            throw new ConflictException("Project is not awaiting a decision.");
        }
        p.setStatus(decision);
        return toResponse(p);
    }

    // ---- helpers -----------------------------------------------------------

    private Project require(Long id) {
        return projectRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> NotFoundException.of("Project", id));
    }

    private void requireOwnerOrAdmin(Project p, AuthenticatedUser actor) {
        if (!p.getOwnerId().equals(actor.userId()) && !isAdmin(actor)) {
            throw new ForbiddenException("Only the project owner or an admin may perform this action.");
        }
    }

    private boolean isAdmin(AuthenticatedUser actor) {
        return "ADMIN".equals(actor.role()) || "SUPER_ADMIN".equals(actor.role());
    }

    private void validateInstitution(Long institutionId) {
        if (institutionId != null && !institutionDirectory.existsById(institutionId)) {
            throw new BadRequestException("Unknown institution: " + institutionId);
        }
    }

    private void validatePriority(Long priorityId) {
        if (priorityId != null && !priorityDirectory.existsById(priorityId)) {
            throw new BadRequestException("Unknown priority: " + priorityId);
        }
    }

    private void applyContent(Project p, String name, String purpose, String annotation, String keyWords,
                              String scientificIdea, String structure, String teamCharacterization,
                              String monitoringPlan, String assessmentPlan, String requirements) {
        p.setProjectName(name);
        p.setProjectPurpose(purpose);
        p.setAnnotation(annotation);
        p.setKeyWords(keyWords);
        p.setScientificIdea(scientificIdea);
        p.setStructure(structure);
        p.setTeamCharacterization(teamCharacterization);
        p.setMonitoringPlan(monitoringPlan);
        p.setAssessmentPlan(assessmentPlan);
        p.setRequirements(requirements);
    }

    private ProjectResponse toResponse(Project p) {
        String ownerName = userDirectory.findById(p.getOwnerId())
                .map(u -> displayName(u)).orElse(null);
        String institutionName = p.getInstitutionId() == null ? null
                : institutionDirectory.findById(p.getInstitutionId()).map(InstitutionSummary::name).orElse(null);
        String priorityName = p.getPriorityId() == null ? null
                : priorityDirectory.findById(p.getPriorityId()).map(PrioritySummary::name).orElse(null);
        return mapper.toResponse(p, ownerName, institutionName, priorityName);
    }

    private static String displayName(UserSummary u) {
        return ((u.name() == null ? "" : u.name()) + " " + (u.surname() == null ? "" : u.surname())).trim();
    }
}
