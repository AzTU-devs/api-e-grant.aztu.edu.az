package az.aztu.egrant.project.api;

/** Published when an owner/admin approves a collaborator's join request. {@code notification} emails them. */
public record MembershipApproved(Long projectId, Long projectCode, Long userId, String email, String name) {
}
