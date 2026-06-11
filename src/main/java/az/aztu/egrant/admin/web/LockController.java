package az.aztu.egrant.admin.web;

import az.aztu.egrant.admin.internal.LockService;
import az.aztu.egrant.admin.web.dto.LockResponse;
import az.aztu.egrant.admin.web.dto.UpdateLockRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/system/lock")
@Tag(name = "System lock", description = "Global submission window (read: authenticated, write: admin)")
public class LockController {

    private final LockService lockService;

    public LockController(LockService lockService) {
        this.lockService = lockService;
    }

    @GetMapping
    @Operation(summary = "Get the current lock status")
    public LockResponse get() {
        return lockService.status();
    }

    @PutMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @Operation(summary = "Lock or unlock submissions")
    public LockResponse set(@Valid @RequestBody UpdateLockRequest request) {
        return lockService.setLocked(request.locked());
    }
}
