package az.aztu.egrant.project.internal;

import az.aztu.egrant.iam.api.UserDirectory;
import az.aztu.egrant.iam.api.UserSummary;
import az.aztu.egrant.project.api.MembershipApproved;
import az.aztu.egrant.project.api.MembershipRejected;
import az.aztu.egrant.project.domain.MemberRole;
import az.aztu.egrant.project.domain.MemberStatus;
import az.aztu.egrant.project.domain.Project;
import az.aztu.egrant.project.domain.ProjectMember;
import az.aztu.egrant.project.web.dto.MemberResponse;
import az.aztu.egrant.shared.error.ConflictException;
import az.aztu.egrant.shared.error.ForbiddenException;
import az.aztu.egrant.shared.error.NotFoundException;
import az.aztu.egrant.shared.security.AuthenticatedUser;
import java.time.Instant;
import java.util.List;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Team membership: join requests, owner/admin approval/rejection, removal, listing. */
@Service
public class ProjectMemberService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository memberRepository;
    private final MemberMapper mapper;
    private final UserDirectory userDirectory;
    private final ApplicationEventPublisher events;

    public ProjectMemberService(ProjectRepository projectRepository, ProjectMemberRepository memberRepository,
                                MemberMapper mapper, UserDirectory userDirectory, ApplicationEventPublisher events) {
        this.projectRepository = projectRepository;
        this.memberRepository = memberRepository;
        this.mapper = mapper;
        this.userDirectory = userDirectory;
        this.events = events;
    }

    @Transactional(readOnly = true)
    public List<MemberResponse> list(Long projectId, MemberStatus status, MemberRole role) {
        requireProject(projectId);
        List<ProjectMember> members = status != null
                ? memberRepository.findByProjectIdAndStatus(projectId, status)
                : memberRepository.findByProjectId(projectId);
        return members.stream()
                .filter(m -> role == null || m.getRole() == role)
                .map(this::toResponse)
                .toList();
    }

    /** A user requests to join {@code projectId} as a collaborator. */
    @Transactional
    public MemberResponse requestJoin(Long projectId, Long userId) {
        requireProject(projectId);
        if (!userDirectory.isProfileCompleted(userId)) {
            throw new ForbiddenException("Complete your profile before joining a project.");
        }
        if (memberRepository.existsByProjectIdAndUserId(projectId, userId)) {
            throw new ConflictException("You have already joined or requested to join this project.");
        }
        ProjectMember m = new ProjectMember();
        m.setProjectId(projectId);
        m.setUserId(userId);
        m.setRole(MemberRole.COLLABORATOR);
        m.setStatus(MemberStatus.PENDING);
        m.setJoinedAt(Instant.now());
        return toResponse(memberRepository.save(m));
    }

    @Transactional
    public MemberResponse approve(Long projectId, Long targetUserId, AuthenticatedUser actor) {
        Project project = requireProject(projectId);
        requireOwnerOrAdmin(project, actor);
        ProjectMember m = requireMembership(projectId, targetUserId);
        if (m.getRole() == MemberRole.OWNER) {
            throw new ConflictException("The owner is already an approved member.");
        }
        if (m.getStatus() == MemberStatus.APPROVED) {
            return toResponse(m);
        }
        int approved = memberRepository.countByProjectIdAndRoleAndStatus(
                projectId, MemberRole.COLLABORATOR, MemberStatus.APPROVED);
        if (approved >= project.getCollaboratorLimit()) {
            throw new ConflictException(
                    "Collaborator limit (" + project.getCollaboratorLimit() + ") reached for this project.");
        }
        m.setStatus(MemberStatus.APPROVED);
        m.setApprovedAt(Instant.now());
        publishDecision(targetUserId, true, projectId, project.getProjectCode());
        return toResponse(m);
    }

    @Transactional
    public MemberResponse reject(Long projectId, Long targetUserId, AuthenticatedUser actor) {
        Project project = requireProject(projectId);
        requireOwnerOrAdmin(project, actor);
        ProjectMember m = requireMembership(projectId, targetUserId);
        if (m.getRole() == MemberRole.OWNER) {
            throw new ConflictException("The owner cannot be rejected.");
        }
        m.setStatus(MemberStatus.REJECTED);
        publishDecision(targetUserId, false, projectId, project.getProjectCode());
        return toResponse(m);
    }

    @Transactional
    public void remove(Long projectId, Long targetUserId, AuthenticatedUser actor) {
        Project project = requireProject(projectId);
        requireOwnerOrAdmin(project, actor);
        ProjectMember m = requireMembership(projectId, targetUserId);
        if (m.getRole() == MemberRole.OWNER) {
            throw new ConflictException("The owner cannot be removed from the team.");
        }
        memberRepository.delete(m);
    }

    // ---- helpers -----------------------------------------------------------

    private Project requireProject(Long projectId) {
        return projectRepository.findByIdAndDeletedAtIsNull(projectId)
                .orElseThrow(() -> NotFoundException.of("Project", projectId));
    }

    private ProjectMember requireMembership(Long projectId, Long userId) {
        return memberRepository.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> NotFoundException.of("Membership", projectId + "/" + userId));
    }

    private void requireOwnerOrAdmin(Project project, AuthenticatedUser actor) {
        boolean admin = "ADMIN".equals(actor.role()) || "SUPER_ADMIN".equals(actor.role());
        if (!project.getOwnerId().equals(actor.userId()) && !admin) {
            throw new ForbiddenException("Only the project owner or an admin may manage the team.");
        }
    }

    private void publishDecision(Long userId, boolean approved, Long projectId, Long projectCode) {
        String name = userDirectory.findById(userId).map(this::displayName).orElse(null);
        String email = userDirectory.findEmail(userId).orElse(null);
        if (email == null) {
            return; // nothing to email
        }
        if (approved) {
            events.publishEvent(new MembershipApproved(projectId, projectCode, userId, email, name));
        } else {
            events.publishEvent(new MembershipRejected(projectId, projectCode, userId, email, name));
        }
    }

    private String displayName(UserSummary u) {
        return ((u.name() == null ? "" : u.name()) + " " + (u.surname() == null ? "" : u.surname())).trim();
    }

    private MemberResponse toResponse(ProjectMember m) {
        UserSummary u = userDirectory.findById(m.getUserId()).orElse(null);
        return mapper.toResponse(m,
                u == null ? null : u.finKod(),
                u == null ? null : u.name(),
                u == null ? null : u.surname());
    }
}
