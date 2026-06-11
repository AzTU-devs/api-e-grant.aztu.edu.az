package az.aztu.egrant.report.web.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ReportPointRequest(
        @NotNull @Min(1) @Max(17) Integer itemNo,
        String content) {
}
