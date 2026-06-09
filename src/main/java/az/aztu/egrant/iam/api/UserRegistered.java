package az.aztu.egrant.iam.api;

/**
 * Published when a new user registers (account is {@code PENDING}). The {@code notification}
 * module listens to send a welcome/OTP-context email.
 */
public record UserRegistered(Long userId, String finKod, String email, String name) {
}
