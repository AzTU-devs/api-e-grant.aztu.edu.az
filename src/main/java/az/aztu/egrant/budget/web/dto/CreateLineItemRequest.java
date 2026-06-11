package az.aztu.egrant.budget.web.dto;

import az.aztu.egrant.budget.domain.BudgetCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record CreateLineItemRequest(
        @NotNull BudgetCategory category,
        @NotBlank String itemName,
        String unitOfMeasure,
        @NotNull @PositiveOrZero Integer unitPrice,
        @NotNull @Positive Integer quantity,
        @Positive Integer duration) {
}
