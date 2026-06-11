package az.aztu.egrant.project.web.dto;

import java.time.LocalDate;

/** Partial update (PATCH): every field is optional; {@code null} leaves the current value unchanged. */
public record UpdateProjectRequest(
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
        Long institutionId,
        Long priorityId) {
}
