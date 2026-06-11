package az.aztu.egrant.budget.api;

/** Cross-module view of a salary line (member resolved to a name by the consumer). */
public record SalaryLine(Long memberId, int salaryPerMonth, int months, int totalAmount) {
}
