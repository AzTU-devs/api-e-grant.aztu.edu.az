package az.aztu.egrant.iam.web.dto;

/** Returned after a successful OTP verification; carries the short-lived reset token. */
public record OtpTokenResponse(String otpToken) {
}
