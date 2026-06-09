package az.aztu.egrant.shared.security;

/**
 * Authenticated principal exposed to controllers via {@code @AuthenticationPrincipal}.
 * Published API of the {@code shared} module.
 *
 * @param userId           the authenticated user's surrogate id ({@code sub})
 * @param finKod           the user's FIN code
 * @param role             the global role (e.g. {@code APPLICANT}, {@code ADMIN}, {@code SUPER_ADMIN})
 * @param profileCompleted whether the user's profile is complete
 */
public record AuthenticatedUser(Long userId, String finKod, String role, boolean profileCompleted) {
}
