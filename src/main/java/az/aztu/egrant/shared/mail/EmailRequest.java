package az.aztu.egrant.shared.mail;

import java.util.Map;

/**
 * A transactional email to render and send.
 *
 * @param to           recipient address
 * @param subject      subject line
 * @param templateName Thymeleaf template name under {@code templates/email/} (without suffix)
 * @param model        template variables
 */
public record EmailRequest(String to, String subject, String templateName, Map<String, Object> model) {
}
