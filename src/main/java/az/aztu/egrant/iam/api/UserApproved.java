package az.aztu.egrant.iam.api;

/** Published when an admin approves a pending user. The {@code notification} module emails them. */
public record UserApproved(Long userId, String finKod, String email, String name) {
}
