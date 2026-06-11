package az.aztu.egrant.project.api;

/**
 * Published when a project moves to {@code SUBMITTED}. The {@code expert} module consumes it
 * (a project becomes eligible for expert assignment); {@code notification} may email the owner.
 */
public record ProjectSubmitted(Long projectId, Long projectCode, Long ownerId) {
}
