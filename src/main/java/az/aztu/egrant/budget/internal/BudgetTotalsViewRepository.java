package az.aztu.egrant.budget.internal;

import az.aztu.egrant.budget.domain.BudgetTotalsView;
import org.springframework.data.jpa.repository.JpaRepository;

/** Read-only access to the authoritative {@code v_budget_totals} view (keyed by {@code budget_id}). */
public interface BudgetTotalsViewRepository extends JpaRepository<BudgetTotalsView, Long> {
}
