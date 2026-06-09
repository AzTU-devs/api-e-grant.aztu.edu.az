package az.aztu.egrant.iam.internal;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Binds {@code egrant.otp.*}. */
@ConfigurationProperties(prefix = "egrant.otp")
public record OtpProperties(Duration ttl, int length) {
}
