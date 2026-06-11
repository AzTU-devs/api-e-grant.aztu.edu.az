package az.aztu.egrant.project.api;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Full cross-module view of a project's content (for {@code document} exports and {@code publicapi}).
 * Carries no budget figures or owner contact data — those live in their own modules.
 */
public record ProjectDetail(
        Long id,
        Long projectCode,
        Long ownerId,
        Long institutionId,
        Long priorityId,
        String projectName,
        String projectPurpose,
        String annotation,
        String keyWords,
        String scientificIdea,
        String structure,
        String teamCharacterization,
        String monitoringPlan,
        String assessmentPlan,
        String requirements,
        LocalDate deadline,
        String status,
        Instant submittedAt,
        int collaboratorLimit,
        int maxBudgetAmount) {
}
