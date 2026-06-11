package az.aztu.egrant.report.web;

import az.aztu.egrant.report.internal.ReportService;
import az.aztu.egrant.report.web.dto.ReportResponse;
import az.aztu.egrant.report.web.dto.SubmitReportRequest;
import az.aztu.egrant.shared.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/projects/{projectId}/reports")
@Tag(name = "Quarterly reports", description = "Quarterly reports and their 17 points")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping
    @Operation(summary = "List quarterly reports (optionally filter by year and quarter)")
    public List<ReportResponse> list(@AuthenticationPrincipal AuthenticatedUser principal,
                                     @PathVariable Long projectId,
                                     @RequestParam(required = false) Integer year,
                                     @RequestParam(required = false) Integer quarter) {
        return reportService.list(projectId, year, quarter, principal);
    }

    @PostMapping
    @Operation(summary = "File (or re-file) a quarterly report with its points")
    public ResponseEntity<ReportResponse> submit(@AuthenticationPrincipal AuthenticatedUser principal,
                                                 @PathVariable Long projectId,
                                                 @Valid @RequestBody SubmitReportRequest request) {
        return ResponseEntity.status(201).body(reportService.submit(projectId, principal, request));
    }
}
