package az.aztu.egrant.expert.web;

import az.aztu.egrant.expert.internal.ExpertService;
import az.aztu.egrant.expert.web.dto.CreateExpertRequest;
import az.aztu.egrant.expert.web.dto.ExpertResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/experts")
@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
@Tag(name = "Experts", description = "Expert registry (admin)")
public class ExpertController {

    private final ExpertService expertService;

    public ExpertController(ExpertService expertService) {
        this.expertService = expertService;
    }

    @GetMapping
    @Operation(summary = "List experts")
    public List<ExpertResponse> list() {
        return expertService.list();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get an expert by id")
    public ExpertResponse get(@PathVariable Long id) {
        return expertService.get(id);
    }

    @PostMapping
    @Operation(summary = "Register an expert")
    public ResponseEntity<ExpertResponse> create(@Valid @RequestBody CreateExpertRequest request) {
        ExpertResponse created = expertService.create(request);
        return ResponseEntity.created(URI.create("/api/v1/experts/" + created.id())).body(created);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an expert")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        expertService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
