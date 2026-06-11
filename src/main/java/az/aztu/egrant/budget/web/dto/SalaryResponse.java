package az.aztu.egrant.budget.web.dto;

public record SalaryResponse(
        Long id,
        Long budgetId,
        Long memberId,
        Integer salaryPerMonth,
        Integer months,
        Integer totalAmount) {
}
