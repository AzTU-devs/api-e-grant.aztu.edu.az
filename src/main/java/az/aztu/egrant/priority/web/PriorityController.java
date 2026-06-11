package az.aztu.egrant.priority.web;

import az.aztu.egrant.priority.internal.PriorityService;
import az.aztu.egrant.priority.web.dto.CreatePriorityRequest;
import az.aztu.egrant.priority.web.dto.PriorityResponse;
import az.aztu.egrant.priority.web.dto.UpdatePriorityRequest;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/priorities")
@Tag(name = "Priorities", description = "Research-priority lookup (public read, admin write)")
public class PriorityController {

    private static final String ADMIN = "hasAnyRole('ADMIN','SUPER_ADMIN')";

    private final PriorityService service;

    public PriorityController(PriorityService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "List all priorities")
    public List<PriorityResponse> list() {
        return service.list();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a priority by id")
    public PriorityResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    @PostMapping
    @PreAuthorize(ADMIN)
    @Operation(summary = "Create a priority")
    public ResponseEntity<PriorityResponse> create(@Valid @RequestBody CreatePriorityRequest request) {
        PriorityResponse created = service.create(request);
        return ResponseEntity.created(URI.create("/api/v1/priorities/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize(ADMIN)
    @Operation(summary = "Update a priority")
    public PriorityResponse update(@PathVariable Long id, @Valid @RequestBody UpdatePriorityRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(ADMIN)
    @Operation(summary = "Delete a priority")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
