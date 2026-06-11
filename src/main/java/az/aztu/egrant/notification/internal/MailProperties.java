package az.aztu.egrant.notification.internal;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Binds {@code egrant.mail.*}. When {@code enabled=false}, sends are logged and skipped (dev/test). */
@ConfigurationProperties(prefix = "egrant.mail")
public record MailProperties(String from, boolean enabled) {
}
