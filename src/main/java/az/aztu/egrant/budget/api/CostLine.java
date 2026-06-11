package az.aztu.egrant.budget.api;

/** Cross-module view of a cost line item. */
public record CostLine(
        String category,
        String itemName,
        String unitOfMeasure,
        int unitPrice,
        int quantity,
        int duration,
        int totalAmount) {
}
