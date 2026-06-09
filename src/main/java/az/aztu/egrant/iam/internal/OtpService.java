package az.aztu.egrant.iam.internal;

import az.aztu.egrant.iam.api.OtpRequested;
import az.aztu.egrant.iam.domain.OtpCode;
import az.aztu.egrant.iam.domain.User;
import az.aztu.egrant.shared.error.BadRequestException;
import az.aztu.egrant.shared.error.NotFoundException;
import java.security.SecureRandom;
import java.time.Instant;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Issues and verifies one-time passwords. */
@Service
public class OtpService {

    private final OtpCodeRepository otpCodeRepository;
    private final UserRepository userRepository;
    private final CredentialRepository credentialRepository;
    private final ApplicationEventPublisher events;
    private final OtpProperties properties;
    private final SecureRandom random = new SecureRandom();

    public OtpService(OtpCodeRepository otpCodeRepository, UserRepository userRepository,
                      CredentialRepository credentialRepository, ApplicationEventPublisher events,
                      OtpProperties properties) {
        this.otpCodeRepository = otpCodeRepository;
        this.userRepository = userRepository;
        this.credentialRepository = credentialRepository;
        this.events = events;
        this.properties = properties;
    }

    /** Generates an OTP for the given FIN, stores it, and publishes {@link OtpRequested}. */
    @Transactional
    public void issue(String finKod) {
        User user = userRepository.findByFinKod(finKod)
                .orElseThrow(() -> NotFoundException.of("User", finKod));
        Instant now = Instant.now();
        String code = randomCode();

        OtpCode otp = new OtpCode();
        otp.setUserId(user.getId());
        otp.setCode(code);
        otp.setIssuedAt(now);
        otp.setExpiresAt(now.plus(properties.ttl()));
        otpCodeRepository.save(otp);

        events.publishEvent(new OtpRequested(
                user.getId(), user.getFinKod(), user.getPersonalEmail(), user.getName(), code));
    }

    /** Verifies the latest active OTP for the user; marks it consumed and flags the credential. */
    @Transactional
    public User verify(String finKod, String code) {
        User user = userRepository.findByFinKod(finKod)
                .orElseThrow(() -> NotFoundException.of("User", finKod));
        OtpCode otp = otpCodeRepository
                .findFirstByUserIdAndConsumedAtIsNullOrderByIssuedAtDesc(user.getId())
                .orElseThrow(() -> new BadRequestException("No active OTP. Request a new code."));

        Instant now = Instant.now();
        if (otp.isExpired(now)) {
            throw new BadRequestException("OTP has expired. Request a new code.");
        }
        if (!otp.getCode().equals(code)) {
            throw new BadRequestException("Invalid OTP.");
        }

        otp.setConsumedAt(now);
        credentialRepository.findByUserId(user.getId()).ifPresent(c -> c.setOtpVerified(true));
        return user;
    }

    private String randomCode() {
        int bound = (int) Math.pow(10, properties.length());
        int value = random.nextInt(bound);
        return String.format("%0" + properties.length() + "d", value);
    }
}
