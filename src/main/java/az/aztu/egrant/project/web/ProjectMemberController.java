package az.aztu.egrant.project.web;

import az.aztu.egrant.project.domain.MemberRole;
import az.aztu.egrant.project.domain.MemberStatus;
import az.aztu.egrant.project.internal.ProjectMemberService;
import az.aztu.egrant.project.web.dto.MemberResponse;
import az.aztu.egrant.shared.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/projects/{projectId}/members")
@Tag(name = "Project team", description = "Team membership: join requests and owner/admin decisions")
public class ProjectMemberController {

    private final ProjectMemberService memberService;

    public ProjectMemberController(ProjectMemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping
    @Operation(summary = "List team members (filter by status/role)")
    public List<MemberResponse> list(@PathVariable Long projectId,
                                     @RequestParam(required = false) MemberStatus status,
                                     @RequestParam(required = false) MemberRole role) {
        return memberService.list(projectId, status, role);
    }

    @PostMapping
    @Operation(summary = "Request to join the project as a collaborator (requires a completed profile)")
    public ResponseEntity<MemberResponse> join(@AuthenticationPrincipal AuthenticatedUser principal,
                                               @PathVariable Long projectId) {
        return ResponseEntity.status(201).body(memberService.requestJoin(projectId, principal.userId()));
    }

    @PostMapping("/{userId}/approve")
    @Operation(summary = "Approve a join request (owner or admin)")
    public MemberResponse approve(@AuthenticationPrincipal AuthenticatedUser principal,
                                  @PathVariable Long projectId, @PathVariable Long userId) {
        return memberService.approve(projectId, userId, principal);
    }

    @PostMapping("/{userId}/reject")
    @Operation(summary = "Reject a join request (owner or admin)")
    public MemberResponse reject(@AuthenticationPrincipal AuthenticatedUser principal,
                                 @PathVariable Long projectId, @PathVariable Long userId) {
        return memberService.reject(projectId, userId, principal);
    }

    @DeleteMapping("/{userId}")
    @Operation(summary = "Remove a member (owner or admin)")
    public ResponseEntity<Void> remove(@AuthenticationPrincipal AuthenticatedUser principal,
                                       @PathVariable Long projectId, @PathVariable Long userId) {
        memberService.remove(projectId, userId, principal);
        return ResponseEntity.noContent().build();
    }
}
