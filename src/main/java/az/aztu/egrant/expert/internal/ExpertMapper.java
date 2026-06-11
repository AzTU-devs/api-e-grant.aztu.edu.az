package az.aztu.egrant.expert.internal;

import az.aztu.egrant.expert.domain.Assessment;
import az.aztu.egrant.expert.domain.Expert;
import az.aztu.egrant.expert.domain.ExpertAssignment;
import az.aztu.egrant.expert.web.dto.AssessmentResponse;
import az.aztu.egrant.expert.web.dto.AssignmentResponse;
import az.aztu.egrant.expert.web.dto.ExpertResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ExpertMapper {

    ExpertResponse toResponse(Expert expert);

    @Mapping(target = "expertName", source = "expertName")
    AssignmentResponse toAssignmentResponse(ExpertAssignment assignment, String expertName);

    @Mapping(target = "expertName", source = "expertName")
    AssessmentResponse toAssessmentResponse(Assessment assessment, String expertName);
}
