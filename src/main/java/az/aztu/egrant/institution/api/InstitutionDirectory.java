package az.aztu.egrant.institution.api;

import java.util.Optional;

/**
 * Read-only institution lookups for other modules (e.g. {@code project} validating an
 * {@code institution_id} before persisting, or resolving its name for a DTO/export).
 */
public interface InstitutionDirectory {

    boolean existsById(Long id);

    Optional<InstitutionSummary> findById(Long id);
}
