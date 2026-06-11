package az.aztu.egrant.project.internal;

import az.aztu.egrant.project.domain.ProjectActivity;
import az.aztu.egrant.project.web.dto.ActivityResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ActivityMapper {

    ActivityResponse toResponse(ProjectActivity activity);
}
