package az.aztu.egrant.iam.internal;

import az.aztu.egrant.iam.api.UserSummary;
import az.aztu.egrant.iam.domain.AccountStatus;
import az.aztu.egrant.iam.domain.User;
import az.aztu.egrant.iam.web.dto.UserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "status", source = "status")
    @Mapping(target = "hasAvatar", expression = "java(user.getAvatarUrl() != null)")
    UserResponse toResponse(User user, AccountStatus status);

    @Mapping(target = "globalRole", expression = "java(user.getGlobalRole().name())")
    UserSummary toSummary(User user);
}
