package az.aztu.egrant.budget.web.dto;

import jakarta.validation.constraints.PositiveOrZero;

/** Sets the policy values. Either field may be {@code null} to leave it unchanged. */
public record UpdateBudgetRequest(
        @PositiveOrZero Integer totalFee,
        @PositiveOrZero Integer defenseFund) {
}
