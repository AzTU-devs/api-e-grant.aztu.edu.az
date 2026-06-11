package az.aztu.egrant.admin.web.dto;

import java.time.Instant;

public record LockResponse(boolean locked, Instant updatedAt) {
}
