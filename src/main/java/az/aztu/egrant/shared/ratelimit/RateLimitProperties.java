package az.aztu.egrant.shared.ratelimit;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Binds {@code egrant.ratelimit.*}. Legacy default: 200/day + 50/hour per client IP. */
@ConfigurationProperties(prefix = "egrant.ratelimit")
public record RateLimitProperties(boolean enabled, long perDay, long perHour) {
}
