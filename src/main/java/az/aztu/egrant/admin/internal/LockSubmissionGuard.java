package az.aztu.egrant.admin.internal;

import az.aztu.egrant.project.api.ProjectSubmissionContext;
import az.aztu.egrant.project.api.ProjectSubmissionGuard;
import az.aztu.egrant.shared.error.ConflictException;
import org.springframework.stereotype.Component;

/**
 * Submission guard enforcing the global system lock: while {@code system_lock.is_locked} is true,
 * project submission is blocked. Plugs into {@code project}'s {@link ProjectSubmissionGuard} SPI.
 */
@Component
public class LockSubmissionGuard implements ProjectSubmissionGuard {

    private final LockService lockService;

    public LockSubmissionGuard(LockService lockService) {
        this.lockService = lockService;
    }

    @Override
    public void check(ProjectSubmissionContext context) {
        if (lockService.isLocked()) {
            throw new ConflictException("Submissions are currently closed (the system is locked).");
        }
    }
}
