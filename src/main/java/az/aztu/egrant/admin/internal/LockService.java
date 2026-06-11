package az.aztu.egrant.admin.internal;

import az.aztu.egrant.admin.domain.SystemLock;
import az.aztu.egrant.admin.web.dto.LockResponse;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Reads and toggles the global submission lock. */
@Service
public class LockService {

    private final SystemLockRepository repository;

    public LockService(SystemLockRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public LockResponse status() {
        return toResponse(current());
    }

    @Transactional(readOnly = true)
    public boolean isLocked() {
        return current().isLocked();
    }

    @Transactional
    public LockResponse setLocked(boolean locked) {
        SystemLock lock = current();
        lock.setLocked(locked);
        lock.setUpdatedAt(Instant.now());
        return toResponse(lock);
    }

    /** The single canonical lock row (created lazily if the seed row is somehow absent). */
    private SystemLock current() {
        return repository.findTopByOrderByIdAsc().orElseGet(() -> {
            SystemLock lock = new SystemLock();
            lock.setLocked(false);
            return repository.save(lock);
        });
    }

    private LockResponse toResponse(SystemLock lock) {
        return new LockResponse(lock.isLocked(), lock.getUpdatedAt());
    }
}
