package az.aztu.egrant.iam.web.dto;

import az.aztu.egrant.iam.domain.AcademicType;
import az.aztu.egrant.iam.domain.AccountStatus;
import az.aztu.egrant.iam.domain.GlobalRole;
import java.time.LocalDate;

/** Full user profile + account status. */
public record UserResponse(
        Long id,
        String finKod,
        GlobalRole globalRole,
        AccountStatus status,
        AcademicType academicType,
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
        Long institutionId,
        boolean hasAvatar,
        boolean profileCompleted) {
}
