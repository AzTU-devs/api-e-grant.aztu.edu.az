package az.aztu.egrant.iam.web.dto;

public record LoginResponse(
        String token,
        Long userId,
        String finKod,
        String role,
        boolean profileCompleted) {
}
