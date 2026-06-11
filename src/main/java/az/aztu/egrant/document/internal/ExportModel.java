package az.aztu.egrant.document.internal;

import az.aztu.egrant.budget.api.BudgetTotals;
import az.aztu.egrant.budget.api.CostLine;
import az.aztu.egrant.project.api.ActivityView;
import az.aztu.egrant.project.api.ProjectDetail;
import java.util.List;

/** Everything the PDF/Excel renderers need, gathered from other modules' published APIs. */
public record ExportModel(
        ProjectDetail project,
        String ownerName,
        String ownerFinKod,
        String institutionName,
        String priorityName,
        List<MemberRow> members,
        List<ActivityView> activities,
        BudgetTotals totals,
        List<SalaryRow> salaries,
        List<CostLine> lineItems) {

    public record MemberRow(String name, String role, String status) {
    }

    public record SalaryRow(String memberName, int salaryPerMonth, int months, int totalAmount) {
    }
}
