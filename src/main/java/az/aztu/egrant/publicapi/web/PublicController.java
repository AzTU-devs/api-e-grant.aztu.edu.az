package az.aztu.egrant.publicapi.web;

import az.aztu.egrant.publicapi.internal.PublicQueryService;
import az.aztu.egrant.publicapi.web.dto.PriorityTreeNode;
import az.aztu.egrant.publicapi.web.dto.PublicProjectDetail;
import az.aztu.egrant.publicapi.web.dto.PublicProjectSummary;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/public")
@Tag(name = "Public", description = "Unauthenticated public views")
public class PublicController {

    private final PublicQueryService publicQueryService;

    public PublicController(PublicQueryService publicQueryService) {
        this.publicQueryService = publicQueryService;
    }

    @GetMapping("/projects")
    @Operation(summary = "List approved projects (sanitized)")
    public List<PublicProjectSummary> projects() {
        return publicQueryService.listApprovedProjects();
    }

    @GetMapping("/projects/{projectCode}")
    @Operation(summary = "Get an approved project by its public code (sanitized)")
    public PublicProjectDetail project(@PathVariable Long projectCode) {
        return publicQueryService.getApprovedProject(projectCode);
    }

    @GetMapping("/priorities-tree")
    @Operation(summary = "Priorities with their approved projects (legacy leads-tree)")
    public List<PriorityTreeNode> prioritiesTree() {
        return publicQueryService.prioritiesTree();
    }
}
