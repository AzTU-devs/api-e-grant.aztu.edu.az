package az.aztu.egrant.shared.security;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Binds {@code egrant.jwt.*}. */
@ConfigurationProperties(prefix = "egrant.jwt")
public record JwtProperties(
        String secret,
        Duration accessTokenTtl,
        Duration otpTokenTtl) {
}
