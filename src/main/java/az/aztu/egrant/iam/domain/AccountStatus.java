package az.aztu.egrant.iam.domain;

/** Login eligibility (maps to the {@code account_status} Postgres enum). */
public enum AccountStatus {
    PENDING,
    APPROVED,
    BLOCKED
}
