package az.aztu.egrant.iam.web.dto;

import az.aztu.egrant.iam.domain.GlobalRole;
import jakarta.validation.constraints.NotNull;

public record RoleUpdateRequest(@NotNull GlobalRole role) {
}
