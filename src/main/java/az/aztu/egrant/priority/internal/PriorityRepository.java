package az.aztu.egrant.priority.internal;

import az.aztu.egrant.priority.domain.Priority;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PriorityRepository extends JpaRepository<Priority, Long> {

    List<Priority> findAllByOrderByCodeAsc();

    Optional<Priority> findByCode(Integer code);

    boolean existsByCode(Integer code);
}
