package az.aztu.egrant.expert.web;

import az.aztu.egrant.expert.internal.AssessmentService;
import az.aztu.egrant.expert.web.dto.AssessmentResponse;
import az.aztu.egrant.expert.web.dto.CreateAssessmentRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/projects/{projectId}/assessments")
@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
@Tag(name = "Assessments", description = "Expert assessments of a project (admin)")
public class AssessmentController {

    private final AssessmentService assessmentService;

    public AssessmentController(AssessmentService assessmentService) {
        this.assessmentService = assessmentService;
    }

    @GetMapping
    @Operation(summary = "List a project's assessments")
    public List<AssessmentResponse> list(@PathVariable Long projectId) {
        return assessmentService.list(projectId);
    }

    @PostMapping
    @Operation(summary = "Record an expert's assessment (upserts on project+expert)")
    public ResponseEntity<AssessmentResponse> submit(@PathVariable Long projectId,
                                                     @Valid @RequestBody CreateAssessmentRequest request) {
        return ResponseEntity.status(201).body(assessmentService.submit(projectId, request));
    }
}
