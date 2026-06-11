package az.aztu.egrant;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Base for integration tests: boots the full application against a real Postgres (Testcontainers),
 * so Flyway migrations, native enums, generated columns and the {@code v_budget_totals} view all run.
 * Requires Docker.
 *
 * <p>Uses the <em>singleton container</em> pattern — one container is started once per JVM and shared
 * by every integration-test class (its port stays stable across the cached Spring context). Do not
 * switch back to {@code @Testcontainers}/{@code @Container} static fields here: those are stopped after
 * the first test class, leaving later classes pointing at a dead port.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {

    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    static {
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }
}
