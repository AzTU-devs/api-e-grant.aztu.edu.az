package az.aztu.egrant.iam.web.dto;

import az.aztu.egrant.iam.domain.AcademicType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Size(min = 7, max = 7) String finKod,
        @NotBlank @Size(min = 8, max = 100) String password,
        @NotNull AcademicType academicType,
        @NotBlank String name,
        @NotBlank String surname,
        String fatherName,
        @NotBlank @Email String personalEmail,
        Long institutionId) {
}
