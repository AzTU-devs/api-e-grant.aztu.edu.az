package az.aztu.egrant.expert.web.dto;

import java.time.Instant;

public record AssessmentResponse(
        Long id,
        Long projectId,
        Long expertId,
        String expertName,
        Integer score,
        String note,
        Instant createdAt,
        Instant updatedAt) {
}
