package az.aztu.egrant.institution.web;

import az.aztu.egrant.institution.internal.InstitutionService;
import az.aztu.egrant.institution.web.dto.CreateInstitutionRequest;
import az.aztu.egrant.institution.web.dto.InstitutionResponse;
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
@RequestMapping("/api/v1/institutions")
@Tag(name = "Institutions", description = "Institution lookup (public read, admin write)")
public class InstitutionController {

    private static final String ADMIN = "hasAnyRole('ADMIN','SUPER_ADMIN')";

    private final InstitutionService service;

    public InstitutionController(InstitutionService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "List all institutions")
    public List<InstitutionResponse> list() {
        return service.list();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get an institution by id")
    public InstitutionResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    @PostMapping
    @PreAuthorize(ADMIN)
    @Operation(summary = "Create an institution")
    public ResponseEntity<InstitutionResponse> create(@Valid @RequestBody CreateInstitutionRequest request) {
        InstitutionResponse created = service.create(request);
        return ResponseEntity.created(URI.create("/api/v1/institutions/" + created.id())).body(created);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(ADMIN)
    @Operation(summary = "Delete an institution")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
