package az.aztu.egrant.expert.web.dto;

import java.time.Instant;

public record AssignmentResponse(
        Long id,
        Long projectId,
        Long expertId,
        String expertName,
        String status,
        Instant assignedAt,
        Instant respondedAt) {
}
