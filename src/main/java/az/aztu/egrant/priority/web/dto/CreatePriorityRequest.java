package az.aztu.egrant.priority.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreatePriorityRequest(
        @NotNull @Positive Integer code,
        @NotBlank String name) {
}
