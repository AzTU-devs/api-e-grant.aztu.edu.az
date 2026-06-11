package az.aztu.egrant.project.internal;

import az.aztu.egrant.project.api.ProjectMemberInfo;
import az.aztu.egrant.project.domain.ProjectMember;
import az.aztu.egrant.project.web.dto.MemberResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MemberMapper {

    @Mapping(target = "userFinKod", source = "finKod")
    @Mapping(target = "userName", source = "name")
    @Mapping(target = "userSurname", source = "surname")
    MemberResponse toResponse(ProjectMember member, String finKod, String name, String surname);

    ProjectMemberInfo toInfo(ProjectMember member);
}
