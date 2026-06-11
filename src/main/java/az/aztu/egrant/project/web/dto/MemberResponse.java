package az.aztu.egrant.project.web.dto;

import java.time.Instant;

public record MemberResponse(
        Long id,
        Long projectId,
        Long userId,
        String userFinKod,
        String userName,
        String userSurname,
        String role,
        String status,
        Instant joinedAt,
        Instant approvedAt) {
}
