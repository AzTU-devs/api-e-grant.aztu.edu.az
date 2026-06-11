package az.aztu.egrant.budget.internal;

import az.aztu.egrant.budget.domain.BudgetTotalsView;
import az.aztu.egrant.project.api.ProjectSubmissionContext;
import az.aztu.egrant.project.api.ProjectSubmissionGuard;
import az.aztu.egrant.shared.error.ConflictException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Submission guard enforcing the budget cap: a project's authoritative {@code grand_total}
 * (from {@code v_budget_totals}) must not exceed its {@code max_budget_amount}. Plugs into the
 * {@code project} module's {@link ProjectSubmissionGuard} extension point (inversion of control —
 * project never depends on budget).
 */
@Component
public class BudgetCapSubmissionGuard implements ProjectSubmissionGuard {

    private final BudgetRepository budgetRepository;
    private final BudgetTotalsViewRepository totalsViewRepository;

    public BudgetCapSubmissionGuard(BudgetRepository budgetRepository,
                                    BudgetTotalsViewRepository totalsViewRepository) {
        this.budgetRepository = budgetRepository;
        this.totalsViewRepository = totalsViewRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public void check(ProjectSubmissionContext context) {
        budgetRepository.findByProjectId(context.projectId()).ifPresent(b -> {
            int grandTotal = totalsViewRepository.findById(b.getId())
                    .map(BudgetTotalsView::getGrandTotal)
                    .orElse(0);
            if (grandTotal > context.maxBudgetAmount()) {
                throw new ConflictException("Budget grand total (" + grandTotal
                        + ") exceeds the project cap (" + context.maxBudgetAmount() + ").");
            }
        });
    }
}
