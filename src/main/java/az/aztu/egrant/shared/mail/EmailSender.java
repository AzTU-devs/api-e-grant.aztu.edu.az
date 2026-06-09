package az.aztu.egrant.shared.mail;

/**
 * Mail abstraction. The implementation (JavaMailSender + Thymeleaf, async) lives in the
 * {@code notification} module. Callers should generally prefer publishing domain events
 * that {@code notification} listens to, rather than depending on this directly.
 */
public interface EmailSender {

    void send(EmailRequest request);
}
