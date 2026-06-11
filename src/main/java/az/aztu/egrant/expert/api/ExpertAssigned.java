package az.aztu.egrant.expert.api;

/** Published when an expert is assigned to a project. {@code notification} emails the expert. */
public record ExpertAssigned(
        Long projectId,
        Long projectCode,
        Long expertId,
        String expertEmail,
        String expertName) {
}
