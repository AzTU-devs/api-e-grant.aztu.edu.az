package az.aztu.egrant.budget.internal;

import az.aztu.egrant.budget.domain.BudgetSalary;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BudgetSalaryRepository extends JpaRepository<BudgetSalary, Long> {

    List<BudgetSalary> findByBudgetId(Long budgetId);

    boolean existsByBudgetIdAndMemberId(Long budgetId, Long memberId);
}
