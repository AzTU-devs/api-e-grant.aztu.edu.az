package az.aztu.egrant.project.api;

/**
 * Extension point letting other modules veto a project submission. The {@code project} module
 * runs every registered guard before a project moves {@code DRAFT → SUBMITTED}; a guard signals
 * rejection by throwing a domain exception (typically {@code ConflictException} → HTTP 409).
 *
 * <p>Implemented by {@code admin} (global system lock) and {@code budget} (grand-total cap).
 * This inversion keeps {@code project} free of any compile-time dependency on those modules.
 */
public interface ProjectSubmissionGuard {

    void check(ProjectSubmissionContext context);
}
