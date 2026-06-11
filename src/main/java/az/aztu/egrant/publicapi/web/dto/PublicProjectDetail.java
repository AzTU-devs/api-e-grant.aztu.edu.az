package az.aztu.egrant.publicapi.web.dto;

import java.time.LocalDate;

/** Sanitized public project detail (no budget, contact or review data). */
public record PublicProjectDetail(
        Long projectCode,
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
        String institutionName,
        String priorityName,
        String ownerName) {
}
