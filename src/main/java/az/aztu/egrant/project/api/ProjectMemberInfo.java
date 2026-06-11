package az.aztu.egrant.project.api;

/** Cross-module view of a team membership (e.g. {@code budget} validating a salary's member). */
public record ProjectMemberInfo(
        Long id,
        Long projectId,
        Long userId,
        String role,
        String status) {

    public boolean isApproved() {
        return "APPROVED".equals(status);
    }
}
