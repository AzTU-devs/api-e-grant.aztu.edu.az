package az.aztu.egrant.budget.web.dto;

/** Budget header plus authoritative computed totals (from {@code v_budget_totals}). */
public record BudgetResponse(
        Long id,
        Long projectId,
        int totalFee,
        int defenseFund,
        int totalSalary,
        int totalEquipment,
        int totalServices,
        int totalRent,
        int totalOther,
        int grandTotal) {
}
