package az.aztu.egrant.project.api;

/**
 * Context passed to each {@link ProjectSubmissionGuard} when a project is submitted.
 * Carries everything a guard needs without exposing the {@code Project} entity.
 */
public record ProjectSubmissionContext(
        Long projectId,
        Long projectCode,
        Long ownerId,
        int maxBudgetAmount) {
}
