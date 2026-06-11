package az.aztu.egrant.priority.api;

import java.util.List;
import java.util.Optional;

/**
 * Read-only priority lookups for other modules ({@code project} validating a {@code priority_id};
 * {@code publicapi} building the public priorities tree).
 */
public interface PriorityDirectory {

    boolean existsById(Long id);

    Optional<PrioritySummary> findById(Long id);

    List<PrioritySummary> findAll();
}
