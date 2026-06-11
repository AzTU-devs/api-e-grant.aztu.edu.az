package az.aztu.egrant.budget.internal;

import az.aztu.egrant.budget.domain.Budget;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BudgetRepository extends JpaRepository<Budget, Long> {

    Optional<Budget> findByProjectId(Long projectId);

    boolean existsByProjectId(Long projectId);
}
