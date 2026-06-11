package az.aztu.egrant.expert.web.dto;

import az.aztu.egrant.expert.domain.AssignmentStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateAssignmentRequest(@NotNull AssignmentStatus status) {
}
