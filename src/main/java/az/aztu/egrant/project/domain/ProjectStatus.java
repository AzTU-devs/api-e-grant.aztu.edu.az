package az.aztu.egrant.project.domain;

/** Project lifecycle (replaces the legacy {@code approved} int + {@code submitted} bool). */
public enum ProjectStatus {
    DRAFT,
    SUBMITTED,
    UNDER_REVIEW,
    APPROVED,
    REJECTED
}
