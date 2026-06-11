package az.aztu.egrant.notification.internal;

import az.aztu.egrant.shared.mail.EmailRequest;
import az.aztu.egrant.shared.mail.EmailSender;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

/**
 * {@link EmailSender} backed by {@link JavaMailSender} + Thymeleaf. Renders {@code templates/email/*.html}
 * and sends as HTML. Runs on the async executor; failures are logged, never propagated to callers.
 */
@Component
public class ThymeleafEmailSender implements EmailSender {

    private static final Logger log = LoggerFactory.getLogger(ThymeleafEmailSender.class);

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;
    private final MailProperties properties;

    public ThymeleafEmailSender(JavaMailSender mailSender, SpringTemplateEngine templateEngine,
                                MailProperties properties) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
        this.properties = properties;
    }

    @Override
    @Async
    public void send(EmailRequest request) {
        if (!properties.enabled()) {
            log.info("Mail disabled — skipping '{}' to {}", request.subject(), request.to());
            return;
        }
        if (!StringUtils.hasText(request.to())) {
            log.warn("No recipient for '{}' — skipping", request.subject());
            return;
        }
        try {
            Context context = new Context();
            if (request.model() != null) {
                request.model().forEach(context::setVariable);
            }
            String html = templateEngine.process("email/" + request.templateName(), context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
            helper.setTo(request.to());
            helper.setSubject(request.subject());
            helper.setText(html, true);
            if (StringUtils.hasText(properties.from())) {
                helper.setFrom(properties.from());
            }
            mailSender.send(message);
            log.info("Sent '{}' to {}", request.subject(), request.to());
        } catch (Exception ex) {
            log.error("Failed to send '{}' to {}: {}", request.subject(), request.to(), ex.getMessage());
        }
    }
}
