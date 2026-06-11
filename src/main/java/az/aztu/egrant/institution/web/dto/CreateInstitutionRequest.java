package az.aztu.egrant.institution.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateInstitutionRequest(
        @NotBlank @Size(max = 255) String code,
        @NotBlank @Size(max = 255) String name) {
}
