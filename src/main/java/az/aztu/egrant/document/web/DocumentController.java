package az.aztu.egrant.document.web;

import az.aztu.egrant.document.internal.GeneratedDocument;
import az.aztu.egrant.document.internal.ProjectExportService;
import az.aztu.egrant.shared.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/projects/{projectId}")
@Tag(name = "Documents", description = "Project + smeta exports (PDF, Excel)")
public class DocumentController {

    private final ProjectExportService exportService;

    public DocumentController(ProjectExportService exportService) {
        this.exportService = exportService;
    }

    @GetMapping("/pdf")
    @Operation(summary = "Export the project and budget as PDF")
    public ResponseEntity<byte[]> pdf(@AuthenticationPrincipal AuthenticatedUser principal,
                                      @PathVariable Long projectId) {
        return asFile(exportService.exportPdf(projectId, principal));
    }

    @GetMapping("/excel")
    @Operation(summary = "Export the project and budget as Excel")
    public ResponseEntity<byte[]> excel(@AuthenticationPrincipal AuthenticatedUser principal,
                                        @PathVariable Long projectId) {
        return asFile(exportService.exportExcel(projectId, principal));
    }

    private ResponseEntity<byte[]> asFile(GeneratedDocument doc) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + doc.filename() + "\"")
                .contentType(MediaType.parseMediaType(doc.contentType()))
                .body(doc.content());
    }
}
