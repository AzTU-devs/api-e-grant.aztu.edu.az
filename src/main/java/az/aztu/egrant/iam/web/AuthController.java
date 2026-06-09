package az.aztu.egrant.iam.web;

import az.aztu.egrant.iam.internal.AuthService;
import az.aztu.egrant.iam.web.dto.LoginRequest;
import az.aztu.egrant.iam.web.dto.LoginResponse;
import az.aztu.egrant.iam.web.dto.OtpRequest;
import az.aztu.egrant.iam.web.dto.OtpTokenResponse;
import az.aztu.egrant.iam.web.dto.OtpVerifyRequest;
import az.aztu.egrant.iam.web.dto.PasswordResetRequest;
import az.aztu.egrant.iam.web.dto.RegisterRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Auth", description = "Registration, login, OTP and password reset")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new applicant (account starts PENDING)")
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterRequest request) {
        Long id = authService.register(request);
        return ResponseEntity.created(URI.create("/api/v1/users/" + id)).build();
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate and receive a JWT")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/otp")
    @Operation(summary = "Request a one-time password (emailed)")
    public ResponseEntity<Void> requestOtp(@Valid @RequestBody OtpRequest request) {
        authService.requestOtp(request.finKod());
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/otp/verify")
    @Operation(summary = "Verify an OTP and receive a short-lived reset token")
    public OtpTokenResponse verifyOtp(@Valid @RequestBody OtpVerifyRequest request) {
        return new OtpTokenResponse(authService.verifyOtp(request.finKod(), request.code()));
    }

    @PostMapping("/password/reset")
    @Operation(summary = "Reset password using a reset token from OTP verification")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody PasswordResetRequest request) {
        authService.resetPassword(request.token(), request.newPassword());
        return ResponseEntity.noContent().build();
    }
}
