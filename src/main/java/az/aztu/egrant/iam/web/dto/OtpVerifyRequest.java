package az.aztu.egrant.iam.web.dto;

import jakarta.validation.constraints.NotBlank;

public record OtpVerifyRequest(@NotBlank String finKod, @NotBlank String code) {
}
