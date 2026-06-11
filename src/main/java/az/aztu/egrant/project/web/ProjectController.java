package az.aztu.egrant.project.web;

import az.aztu.egrant.project.domain.ProjectStatus;
import az.aztu.egrant.project.internal.ProjectService;
import az.aztu.egrant.project.web.dto.CreateProjectRequest;
import az.aztu.egrant.project.web.dto.ProjectListItem;
import az.aztu.egrant.project.web.dto.ProjectResponse;
import az.aztu.egrant.project.web.dto.UpdateProjectRequest;
import az.aztu.egrant.shared.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/projects")
@Tag(name = "Projects", description = "Grant projects: lifecycle, submission and admin decisions")
public class ProjectController {

    private static final String ADMIN = "hasAnyRole('ADMIN','SUPER_ADMIN')";

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    @Operation(summary = "List projects (admins see all; others see their own). Filter by status or ?mine=true")
    public Page<ProjectListItem> list(@AuthenticationPrincipal AuthenticatedUser principal,
                                      @RequestParam(required = false) ProjectStatus status,
                                      @RequestParam(defaultValue = "false") boolean mine,
                                      Pageable pageable) {
        boolean admin = "ADMIN".equals(principal.role()) || "SUPER_ADMIN".equals(principal.role());
        Long owner = (mine || !admin) ? principal.userId() : null;
        return projectService.list(status, owner, pageable);
    }

    @PostMapping
    @Operation(summary = "Create a draft project (the caller becomes the owner)")
    public ResponseEntity<ProjectResponse> create(@AuthenticationPrincipal AuthenticatedUser principal,
                                                   @Valid @RequestBody CreateProjectRequest request) {
        ProjectResponse created = projectService.create(principal.userId(), request);
        return ResponseEntity.created(URI.create("/api/v1/projects/" + created.id())).body(created);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a project by id")
    public ProjectResponse get(@PathVariable Long id) {
        return projectService.get(id);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update a project (owner while DRAFT, or admin)")
    public ProjectResponse update(@AuthenticationPrincipal AuthenticatedUser principal,
                                  @PathVariable Long id, @Valid @RequestBody UpdateProjectRequest request) {
        return projectService.update(id, principal, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a project (owner or admin)")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal AuthenticatedUser principal, @PathVariable Long id) {
        projectService.delete(id, principal);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/submit")
    @Operation(summary = "Submit a project (gated by system lock, completed profile and budget cap)")
    public ProjectResponse submit(@AuthenticationPrincipal AuthenticatedUser principal, @PathVariable Long id) {
        return projectService.submit(id, principal);
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize(ADMIN)
    @Operation(summary = "Approve a submitted project (admin)")
    public ProjectResponse approve(@PathVariable Long id) {
        return projectService.approve(id);
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize(ADMIN)
    @Operation(summary = "Reject a submitted project (admin)")
    public ProjectResponse reject(@PathVariable Long id) {
        return projectService.reject(id);
    }
}
