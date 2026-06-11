package az.aztu.egrant.publicapi.web.dto;

/** Sanitized public project card. */
public record PublicProjectSummary(
        Long projectCode,
        String projectName,
        String status,
        String institutionName,
        String priorityName,
        String ownerName) {
}
