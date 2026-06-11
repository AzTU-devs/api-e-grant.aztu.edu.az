package az.aztu.egrant.project.web.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/** Partial update; {@code null} fields are left unchanged. */
public record UpdateActivityRequest(
        @Min(1) @Max(12) Integer month,
        String activityName) {
}
