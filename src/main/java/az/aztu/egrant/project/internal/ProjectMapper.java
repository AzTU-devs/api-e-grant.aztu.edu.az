package az.aztu.egrant.project.internal;

import az.aztu.egrant.project.api.ProjectDetail;
import az.aztu.egrant.project.api.ProjectSummary;
import az.aztu.egrant.project.domain.Project;
import az.aztu.egrant.project.web.dto.ProjectListItem;
import az.aztu.egrant.project.web.dto.ProjectResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProjectMapper {

    @Mapping(target = "ownerName", source = "ownerName")
    @Mapping(target = "institutionName", source = "institutionName")
    @Mapping(target = "priorityName", source = "priorityName")
    ProjectResponse toResponse(Project project, String ownerName, String institutionName, String priorityName);

    ProjectListItem toListItem(Project project);

    ProjectSummary toSummary(Project project);

    ProjectDetail toDetail(Project project);
}
