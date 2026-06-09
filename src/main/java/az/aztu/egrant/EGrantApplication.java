package az.aztu.egrant;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.modulith.Modulithic;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Entry point for the AZTU E-Grant modular monolith.
 *
 * <p>Each direct sub-package of {@code az.aztu.egrant} is a Spring Modulith application
 * module (bounded context). The {@code shared} module is declared {@code OPEN}
 * (see its {@code package-info}) so every other module may use its types freely.
 */
@Modulithic(systemName = "AZTU E-Grant")
@SpringBootApplication
@ConfigurationPropertiesScan
@EnableAsync
public class EGrantApplication {

    public static void main(String[] args) {
        SpringApplication.run(EGrantApplication.class, args);
    }
}
