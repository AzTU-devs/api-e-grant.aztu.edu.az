package az.aztu.egrant.shared.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Binds {@code egrant.storage.*}. */
@ConfigurationProperties(prefix = "egrant.storage")
public record StorageProperties(Local local) {

    public record Local(String baseDir) {
    }
}
