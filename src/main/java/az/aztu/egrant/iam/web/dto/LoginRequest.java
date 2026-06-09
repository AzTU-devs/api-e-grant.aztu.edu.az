package az.aztu.egrant.iam.web.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(@NotBlank String finKod, @NotBlank String password) {
}
