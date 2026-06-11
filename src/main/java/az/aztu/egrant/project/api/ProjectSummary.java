package az.aztu.egrant.project.api;

/** Cross-module view of a project (no heavy content fields). */
public record ProjectSummary(
        Long id,
        Long projectCode,
        Long ownerId,
        Long institutionId,
        Long priorityId,
        String projectName,
        String status,
        int collaboratorLimit,
        int maxBudgetAmount) {
}
