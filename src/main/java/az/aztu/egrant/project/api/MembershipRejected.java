package az.aztu.egrant.project.api;

/** Published when an owner/admin rejects a collaborator's join request. {@code notification} emails them. */
public record MembershipRejected(Long projectId, Long projectCode, Long userId, String email, String name) {
}
