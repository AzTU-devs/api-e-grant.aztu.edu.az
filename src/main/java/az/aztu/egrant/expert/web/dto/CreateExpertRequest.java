package az.aztu.egrant.expert.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateExpertRequest(
        @NotBlank @Email String email,
        @NotBlank String name,
        @NotBlank String surname,
        String fatherName,
        @NotBlank String personalIdSerialNumber,
        String workPlace,
        String duty,
        String scientificDegree,
        String phoneNumber,
        Long userId) {
}
