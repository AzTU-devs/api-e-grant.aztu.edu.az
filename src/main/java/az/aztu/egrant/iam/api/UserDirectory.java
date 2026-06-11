package az.aztu.egrant.iam.api;

import java.util.Optional;

/**
 * Read-only user lookups for other modules (e.g. {@code project} resolving owners/members).
 * Lets modules avoid reaching into {@code iam}'s internals or entities.
 */
public interface UserDirectory {

    Optional<UserSummary> findById(Long userId);

    Optional<UserSummary> findByFinKod(String finKod);

    /** The user's primary (personal) email, for transactional mail. Empty if unknown. */
    Optional<String> findEmail(Long userId);

    /** Whether the user has completed their profile (gates joining/approval/submission). */
    boolean isProfileCompleted(Long userId);
}
