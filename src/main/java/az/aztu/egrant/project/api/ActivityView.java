package az.aztu.egrant.project.api;

/** Cross-module view of a monthly activity (for {@code document} exports). */
public record ActivityView(Integer month, String activityName) {
}
