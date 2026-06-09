package az.aztu.egrant;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

/** Verifies the Spring Modulith boundaries: no module reaches into another's internals; no cycles. */
class ModularityTests {

    private static final ApplicationModules MODULES = ApplicationModules.of(EGrantApplication.class);

    @Test
    void verifiesModuleBoundaries() {
        MODULES.verify();
    }
}
