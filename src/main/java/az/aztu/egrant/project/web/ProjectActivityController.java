package az.aztu.egrant.project.web;

import az.aztu.egrant.project.internal.ProjectActivityService;
import az.aztu.egrant.project.web.dto.ActivityResponse;
import az.aztu.egrant.project.web.dto.CreateActivityRequest;
import az.aztu.egrant.project.web.dto.UpdateActivityRequest;
import az.aztu.egrant.shared.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/api/v1/projects/{projectId}/activities")
@Tag(name = "Project activities", description = "Monthly activity plan")
public class ProjectActivityController {

    private final ProjectActivityService activityService;

    public ProjectActivityController(ProjectActivityService activityService) {
        this.activityService = activityService;
    }

    @GetMapping
    @Operation(summary = "List the project's activities")
    public List<ActivityResponse> list(@PathVariable Long projectId) {
        return activityService.list(projectId);
    }

    @PostMapping
    @Operation(summary = "Add an activity (owner or admin)")
    public ResponseEntity<ActivityResponse> create(@AuthenticationPrincipal AuthenticatedUser principal,
                                                    @PathVariable Long projectId,
                                                    @Valid @RequestBody CreateActivityRequest request) {
        return ResponseEntity.status(201).body(activityService.create(projectId, principal, request));
    }

    @PatchMapping("/{activityId}")
    @Operation(summary = "Update an activity (owner or admin)")
    public ActivityResponse update(@AuthenticationPrincipal AuthenticatedUser principal,
                                   @PathVariable Long projectId, @PathVariable Long activityId,
                                   @Valid @RequestBody UpdateActivityRequest request) {
        return activityService.update(projectId, activityId, principal, request);
    }

    @DeleteMapping("/{activityId}")
    @Operation(summary = "Delete an activity (owner or admin)")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal AuthenticatedUser principal,
                                       @PathVariable Long projectId, @PathVariable Long activityId) {
        activityService.delete(projectId, activityId, principal);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping(params = "month")
    @Operation(summary = "Delete all activities for a given month (owner or admin)")
    public ResponseEntity<Void> deleteByMonth(@AuthenticationPrincipal AuthenticatedUser principal,
                                              @PathVariable Long projectId, @RequestParam Integer month) {
        activityService.deleteByMonth(projectId, month, principal);
        return ResponseEntity.noContent().build();
    }
}
