package az.aztu.egrant.iam.web.dto;

import jakarta.validation.constraints.NotBlank;

public record OtpRequest(@NotBlank String finKod) {
}
