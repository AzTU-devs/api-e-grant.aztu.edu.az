package az.aztu.egrant.admin.internal;

import az.aztu.egrant.admin.domain.SystemLock;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SystemLockRepository extends JpaRepository<SystemLock, Long> {

    Optional<SystemLock> findTopByOrderByIdAsc();
}
