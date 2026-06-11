package az.aztu.egrant.expert.web.dto;

public record ExpertResponse(
        Long id,
        String email,
        String name,
        String surname,
        String fatherName,
        String personalIdSerialNumber,
        String workPlace,
        String duty,
        String scientificDegree,
        String phoneNumber,
        Long userId) {
}
