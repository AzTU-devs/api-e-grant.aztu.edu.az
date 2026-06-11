/**
 * Notification: renders and sends transactional email (JavaMailSender + Thymeleaf) in response to
 * domain events from {@code iam}, {@code project} and {@code expert}. Sending is asynchronous and
 * fired after the publishing transaction commits, so the request path never blocks on SMTP.
 * Provides the {@code shared} {@link az.aztu.egrant.shared.mail.EmailSender} implementation.
 */
@org.springframework.modulith.ApplicationModule(
        displayName = "Notification")
package az.aztu.egrant.notification;
