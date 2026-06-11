package az.aztu.egrant.project.web.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

public record CreateProjectRequest(
        @NotBlank String projectName,
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
