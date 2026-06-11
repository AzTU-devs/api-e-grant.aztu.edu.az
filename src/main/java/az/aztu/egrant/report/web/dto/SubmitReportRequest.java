package az.aztu.egrant.report.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/** Files (or re-files) a quarterly report; the points replace any previously stored points. */
public record SubmitReportRequest(
        @NotNull @Min(1) @Max(4) Integer quarterNumber,
        @NotNull @Min(2000) @Max(2100) Integer year,
        @NotEmpty @Valid List<ReportPointRequest> points) {
}
