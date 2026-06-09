package az.aztu.egrant.iam.api;

/** Published when an OTP is issued; the {@code notification} module emails the code. */
public record OtpRequested(Long userId, String finKod, String email, String name, String code) {
}
