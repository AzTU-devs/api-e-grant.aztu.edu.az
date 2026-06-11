package az.aztu.egrant.project.web.dto;

import java.time.Instant;

/** Lightweight project row for paginated list endpoints. */
public record ProjectListItem(
        Long id,
        Long projectCode,
        Long ownerId,
        String projectName,
        String status,
        Instant submittedAt,
        Instant createdAt) {
}
