package az.aztu.egrant.project.api;

import java.util.List;
import java.util.Optional;

/**
 * Read-only project lookups for other modules ({@code budget}, {@code expert}, {@code report},
 * {@code document}, {@code publicapi}) — avoids reaching into project internals or entities.
 */
public interface ProjectDirectory {

    boolean existsById(Long projectId);

    Optional<ProjectSummary> findById(Long projectId);

    Optional<ProjectSummary> findByCode(Long projectCode);

    /** Full content view by surrogate id (for {@code document} exports). */
    Optional<ProjectDetail> findDetailById(Long projectId);

    /** Full content view by business {@code project_code} (for {@code publicapi} / {@code document}). */
    Optional<ProjectDetail> findDetailByCode(Long projectCode);

    /** Summaries of all non-deleted projects in the given status (e.g. {@code APPROVED} for public listing). */
    List<ProjectSummary> findByStatus(String status);
}
