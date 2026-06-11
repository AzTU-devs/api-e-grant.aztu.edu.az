package az.aztu.egrant.expert.web.dto;

import jakarta.validation.constraints.NotNull;

public record CreateAssignmentRequest(@NotNull Long expertId) {
}
