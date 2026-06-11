package az.aztu.egrant.project.api;

/**
 * Narrow write-port letting the {@code expert} module advance a project into review when an
 * expert is assigned ({@code SUBMITTED → UNDER_REVIEW}). Keeps the status machine owned by
 * {@code project} while allowing the inversion (expert depends on project, never the reverse).
 */
public interface ProjectReview {

    /** Moves a {@code SUBMITTED} project to {@code UNDER_REVIEW}; no-op if already under review or beyond. */
    void markUnderReview(Long projectId);
}
