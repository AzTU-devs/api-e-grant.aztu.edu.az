package az.aztu.egrant.project.web.dto;

import java.time.Instant;

public record ActivityResponse(
        Long id,
        Long projectId,
        Integer month,
        String activityName,
        Instant createdAt,
        Instant updatedAt) {
}
