package az.aztu.egrant.project.web.dto;

import java.time.Instant;
import java.time.LocalDate;

/** Full project representation. Display names are resolved cross-module from the directories. */
public record ProjectResponse(
        Long id,
        Long projectCode,
        Long ownerId,
        String ownerName,
        Long institutionId,
        String institutionName,
        Long priorityId,
        String priorityName,
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
        int maxBudgetAmount,
        Instant createdAt,
        Instant updatedAt) {
}
