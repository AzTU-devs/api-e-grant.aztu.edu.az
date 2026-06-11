package az.aztu.egrant.budget.web.dto;

import az.aztu.egrant.budget.domain.BudgetCategory;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

/** Partial update; {@code null} fields are left unchanged. */
public record UpdateLineItemRequest(
        BudgetCategory category,
        String itemName,
        String unitOfMeasure,
        @PositiveOrZero Integer unitPrice,
        @Positive Integer quantity,
        @Positive Integer duration) {
}
