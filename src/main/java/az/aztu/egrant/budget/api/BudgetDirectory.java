package az.aztu.egrant.budget.api;

import java.util.List;
import java.util.Optional;

/** Read-only budget access for other modules (currently {@code document}, to render the smeta). */
public interface BudgetDirectory {

    Optional<BudgetTotals> totalsForProject(Long projectId);

    List<SalaryLine> salariesForProject(Long projectId);

    List<CostLine> lineItemsForProject(Long projectId);
}
