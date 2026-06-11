package az.aztu.egrant.institution.internal;

import az.aztu.egrant.institution.api.InstitutionSummary;
import az.aztu.egrant.institution.domain.Institution;
import az.aztu.egrant.institution.web.dto.InstitutionResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface InstitutionMapper {

    InstitutionResponse toResponse(Institution institution);

    InstitutionSummary toSummary(Institution institution);
}
