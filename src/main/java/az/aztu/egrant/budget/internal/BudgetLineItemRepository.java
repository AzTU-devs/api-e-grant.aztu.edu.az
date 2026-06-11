package az.aztu.egrant.budget.internal;

import az.aztu.egrant.budget.domain.BudgetCategory;
import az.aztu.egrant.budget.domain.BudgetLineItem;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BudgetLineItemRepository extends JpaRepository<BudgetLineItem, Long> {

    List<BudgetLineItem> findByBudgetIdOrderByCategoryAscIdAsc(Long budgetId);

    List<BudgetLineItem> findByBudgetIdAndCategoryOrderByIdAsc(Long budgetId, BudgetCategory category);
}
