package az.aztu.egrant.iam.api;

/** Lightweight, cross-module view of a user (no sensitive contact data). */
public record UserSummary(
        Long id,
        String finKod,
        String name,
        String surname,
        String fatherName,
        String globalRole,
        boolean profileCompleted) {
}
