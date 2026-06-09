package az.aztu.egrant.iam.web.dto;

import java.time.LocalDate;

/** Editable profile fields. All optional; when the required set becomes complete, {@code profile_completed} flips to true. */
public record UpdateProfileRequest(
        String name,
        String surname,
        String fatherName,
        LocalDate bornDate,
        String bornPlace,
        String sex,
        String citizenship,
        String personalIdNumber,
        String livingLocation,
        String homePhone,
        String personalMobileNumber,
        String personalEmail,
        String workPlace,
        String department,
        String duty,
        String workLocation,
        String workPhone,
        String workEmail,
        String mainEducation,
        String additionalEducation,
        String scientificDegree,
        LocalDate scientificDegreeDate,
        String scientificTitle,
        LocalDate scientificTitleDate,
        Long institutionId) {
}
