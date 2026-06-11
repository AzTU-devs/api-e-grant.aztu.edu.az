package az.aztu.egrant.notification.internal;

import az.aztu.egrant.expert.api.ExpertAssigned;
import az.aztu.egrant.iam.api.OtpRequested;
import az.aztu.egrant.iam.api.UserApproved;
import az.aztu.egrant.iam.api.UserRegistered;
import az.aztu.egrant.project.api.MembershipApproved;
import az.aztu.egrant.project.api.MembershipRejected;
import az.aztu.egrant.shared.mail.EmailRequest;
import az.aztu.egrant.shared.mail.EmailSender;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Turns domain events into transactional emails. Listeners fire after the publishing transaction
 * commits; {@link EmailSender#send} is itself {@code @Async}, so nothing blocks the request thread.
 */
@Component
public class NotificationListeners {

    private final EmailSender emailSender;

    public NotificationListeners(EmailSender emailSender) {
        this.emailSender = emailSender;
    }

    @TransactionalEventListener
    public void onUserRegistered(UserRegistered event) {
        emailSender.send(new EmailRequest(event.email(), "Welcome to AZTU E-Grant", "welcome",
                Map.of("name", nz(event.name()), "finKod", nz(event.finKod()))));
    }

    @TransactionalEventListener
    public void onUserApproved(UserApproved event) {
        emailSender.send(new EmailRequest(event.email(), "Your AZTU E-Grant account is approved",
                "account-approved", Map.of("name", nz(event.name()))));
    }

    @TransactionalEventListener
    public void onOtpRequested(OtpRequested event) {
        emailSender.send(new EmailRequest(event.email(), "Your AZTU E-Grant verification code", "otp",
                Map.of("name", nz(event.name()), "code", nz(event.code()))));
    }

    @TransactionalEventListener
    public void onMembershipApproved(MembershipApproved event) {
        emailSender.send(new EmailRequest(event.email(), "You have been added to a project",
                "membership-approved", Map.of("name", nz(event.name()), "projectCode", event.projectCode())));
    }

    @TransactionalEventListener
    public void onMembershipRejected(MembershipRejected event) {
        emailSender.send(new EmailRequest(event.email(), "Update on your project join request",
                "membership-rejected", Map.of("name", nz(event.name()), "projectCode", event.projectCode())));
    }

    @TransactionalEventListener
    public void onExpertAssigned(ExpertAssigned event) {
        emailSender.send(new EmailRequest(event.expertEmail(), "You have been assigned to review a project",
                "expert-assigned", Map.of("name", nz(event.expertName()), "projectCode", event.projectCode())));
    }

    private static String nz(String value) {
        return value == null ? "" : value;
    }
}
