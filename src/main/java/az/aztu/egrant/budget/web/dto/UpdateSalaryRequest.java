package az.aztu.egrant.budget.web.dto;

import jakarta.validation.constraints.Positive;

/** Partial update; {@code null} fields are left unchanged. */
public record UpdateSalaryRequest(
        @Positive Integer salaryPerMonth,
        @Positive Integer months) {
}
