package az.aztu.egrant.priority.internal;

import az.aztu.egrant.priority.api.PrioritySummary;
import az.aztu.egrant.priority.domain.Priority;
import az.aztu.egrant.priority.web.dto.PriorityResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PriorityMapper {

    PriorityResponse toResponse(Priority priority);

    PrioritySummary toSummary(Priority priority);
}
