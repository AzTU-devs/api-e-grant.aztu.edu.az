package az.aztu.egrant.budget.web.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateSalaryRequest(
        @NotNull Long memberId,
        @NotNull @Positive Integer salaryPerMonth,
        @NotNull @Positive Integer months) {
}
