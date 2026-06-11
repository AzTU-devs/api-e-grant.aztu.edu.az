package az.aztu.egrant.budget.web.dto;

public record LineItemResponse(
        Long id,
        Long budgetId,
        String category,
        String itemName,
        String unitOfMeasure,
        Integer unitPrice,
        Integer quantity,
        Integer duration,
        Integer totalAmount) {
}
