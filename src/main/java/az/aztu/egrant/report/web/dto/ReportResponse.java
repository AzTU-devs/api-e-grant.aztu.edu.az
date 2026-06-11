package az.aztu.egrant.report.web.dto;

import java.time.Instant;
import java.util.List;

public record ReportResponse(
        Long id,
        Long projectId,
        Integer year,
        Integer quarterNumber,
        Instant submissionDate,
        List<ReportPointResponse> points,
        Instant createdAt,
        Instant updatedAt) {
}
