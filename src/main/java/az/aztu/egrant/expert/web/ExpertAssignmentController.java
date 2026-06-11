package az.aztu.egrant.expert.web;

import az.aztu.egrant.expert.internal.ExpertAssignmentService;
import az.aztu.egrant.expert.web.dto.AssignmentResponse;
import az.aztu.egrant.expert.web.dto.CreateAssignmentRequest;
import az.aztu.egrant.expert.web.dto.UpdateAssignmentRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/projects/{projectId}/expert-assignments")
@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
@Tag(name = "Expert assignments", description = "Assign experts to a submitted project (admin)")
public class ExpertAssignmentController {

    private final ExpertAssignmentService assignmentService;

    public ExpertAssignmentController(ExpertAssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    @GetMapping
    @Operation(summary = "List a project's expert assignments")
    public List<AssignmentResponse> list(@PathVariable Long projectId) {
        return assignmentService.list(projectId);
    }

    @PostMapping
    @Operation(summary = "Assign an expert (only after the project is submitted)")
    public ResponseEntity<AssignmentResponse> assign(@PathVariable Long projectId,
                                                     @Valid @RequestBody CreateAssignmentRequest request) {
        return ResponseEntity.status(201).body(assignmentService.assign(projectId, request.expertId()));
    }

    @PatchMapping("/{assignmentId}")
    @Operation(summary = "Update an assignment's status")
    public AssignmentResponse updateStatus(@PathVariable Long projectId, @PathVariable Long assignmentId,
                                           @Valid @RequestBody UpdateAssignmentRequest request) {
        return assignmentService.updateStatus(projectId, assignmentId, request.status());
    }

    @DeleteMapping("/{assignmentId}")
    @Operation(summary = "Remove an assignment")
    public ResponseEntity<Void> remove(@PathVariable Long projectId, @PathVariable Long assignmentId) {
        assignmentService.remove(projectId, assignmentId);
        return ResponseEntity.noContent().build();
    }
}
