package az.aztu.egrant.iam.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordResetRequest(
        @NotBlank String token,
        @NotBlank @Size(min = 8, max = 100) String newPassword) {
}
