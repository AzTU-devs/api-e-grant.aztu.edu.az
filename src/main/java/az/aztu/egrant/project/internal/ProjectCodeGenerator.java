package az.aztu.egrant.project.internal;

import java.security.SecureRandom;
import org.springframework.stereotype.Component;

/** Generates a random, unique 8-digit {@code project_code} business key (kept from the legacy app). */
@Component
public class ProjectCodeGenerator {

    private static final long MIN = 10_000_000L;
    private static final long BOUND = 90_000_000L; // [10000000, 99999999]
    private static final int MAX_ATTEMPTS = 50;

    private final SecureRandom random = new SecureRandom();
    private final ProjectRepository projectRepository;

    public ProjectCodeGenerator(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    public long next() {
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            long candidate = MIN + (long) (random.nextDouble() * BOUND);
            if (!projectRepository.existsByProjectCode(candidate)) {
                return candidate;
            }
        }
        throw new IllegalStateException("Unable to allocate a unique project code after "
                + MAX_ATTEMPTS + " attempts");
    }
}
