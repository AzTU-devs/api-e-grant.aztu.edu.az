/**
 * Project lifecycle: projects, team membership ({@code project_members}) and the monthly
 * activity plan. Owns submit/approve/reject. Submission is gated by pluggable
 * {@link az.aztu.egrant.project.api.ProjectSubmissionGuard}s contributed by other modules
 * (e.g. {@code admin}'s system lock, {@code budget}'s cap check) — so this module never depends
 * on them. Publishes {@link az.aztu.egrant.project.api.ProjectSubmitted}.
 */
@org.springframework.modulith.ApplicationModule(
        displayName = "Project")
package az.aztu.egrant.project;
