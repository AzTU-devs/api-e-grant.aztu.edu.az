package az.aztu.egrant.priority.web.dto;

import jakarta.validation.constraints.NotBlank;

/** {@code code} is optional (null keeps the current code); {@code name} is required. */
public record UpdatePriorityRequest(
        Integer code,
        @NotBlank String name) {
}
