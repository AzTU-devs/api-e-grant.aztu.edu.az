package az.aztu.egrant.budget.api;

/** Authoritative budget totals (from {@code v_budget_totals}) plus the policy fee/fund. */
public record BudgetTotals(
        int totalFee,
        int defenseFund,
        int totalSalary,
        int totalEquipment,
        int totalServices,
        int totalRent,
        int totalOther,
        int grandTotal) {
}
