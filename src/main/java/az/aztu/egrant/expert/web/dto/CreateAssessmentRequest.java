package az.aztu.egrant.expert.web.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/** An expert's assessment, recorded by an admin. Upserts on {@code (project, expert)}. */
public record CreateAssessmentRequest(
        @NotNull Long expertId,
        @Min(0) @Max(100) Integer score,
        String note) {
}
