/**
 * Expert review: the expert registry, project→expert assignments and assessments. Experts are
 * FK-linked (fixing the legacy email-string linkage). Assignment is allowed only after a project
 * is SUBMITTED and advances it to UNDER_REVIEW via {@code project}'s {@code ProjectReview} port.
 * Consumes {@link az.aztu.egrant.project.api.ProjectSubmitted}; publishes
 * {@link az.aztu.egrant.expert.api.ExpertAssigned}.
 */
@org.springframework.modulith.ApplicationModule(
        displayName = "Expert")
package az.aztu.egrant.expert;
