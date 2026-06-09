package az.aztu.egrant.shared.security;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Binds {@code egrant.cors.*}. {@code allowed-origins} accepts a comma-separated list or {@code *}. */
@ConfigurationProperties(prefix = "egrant.cors")
public record CorsProperties(List<String> allowedOrigins) {
}
