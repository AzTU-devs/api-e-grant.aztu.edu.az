package az.aztu.egrant.iam.domain;

/** System-wide authorization role (maps to the {@code global_role} Postgres enum). */
public enum GlobalRole {
    APPLICANT,
    ADMIN,
    SUPER_ADMIN
}
