/**
 * Administration: the global system lock (submission window). Exposes lock/unlock/status and
 * contributes a {@link az.aztu.egrant.project.api.ProjectSubmissionGuard} that blocks project
 * submission while locked. Depends on {@code project} (never the reverse).
 */
@org.springframework.modulith.ApplicationModule(
        displayName = "Admin")
package az.aztu.egrant.admin;
