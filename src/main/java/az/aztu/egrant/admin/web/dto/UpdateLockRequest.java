package az.aztu.egrant.admin.web.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateLockRequest(@NotNull Boolean locked) {
}
