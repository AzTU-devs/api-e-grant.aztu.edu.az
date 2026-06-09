package az.aztu.egrant.iam.internal;

import az.aztu.egrant.iam.api.UserRegistered;
import az.aztu.egrant.iam.domain.AccountStatus;
import az.aztu.egrant.iam.domain.Credential;
import az.aztu.egrant.iam.domain.GlobalRole;
import az.aztu.egrant.iam.domain.User;
import az.aztu.egrant.iam.web.dto.LoginRequest;
import az.aztu.egrant.iam.web.dto.LoginResponse;
import az.aztu.egrant.iam.web.dto.RegisterRequest;
import az.aztu.egrant.shared.error.BadRequestException;
import az.aztu.egrant.shared.error.ConflictException;
import az.aztu.egrant.shared.error.ForbiddenException;
import az.aztu.egrant.shared.error.NotFoundException;
import az.aztu.egrant.shared.security.JwtService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Registration, login, OTP verification and password reset. */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final CredentialRepository credentialRepository;
    private final OtpService otpService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final ApplicationEventPublisher events;

    public AuthService(UserRepository userRepository, CredentialRepository credentialRepository,
                       OtpService otpService, PasswordEncoder passwordEncoder, JwtService jwtService,
                       ApplicationEventPublisher events) {
        this.userRepository = userRepository;
        this.credentialRepository = credentialRepository;
        this.otpService = otpService;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.events = events;
    }

    @Transactional
    public Long register(RegisterRequest request) {
        if (userRepository.existsByFinKod(request.finKod())) {
            throw new ConflictException("A user with this FIN already exists.");
        }
        User user = new User();
        user.setFinKod(request.finKod());
        user.setAcademicType(request.academicType());
        user.setGlobalRole(GlobalRole.APPLICANT);
        user.setName(request.name());
        user.setSurname(request.surname());
        user.setFatherName(request.fatherName());
        user.setPersonalEmail(request.personalEmail());
        user.setInstitutionId(request.institutionId());
        user.setProfileCompleted(false);
        user = userRepository.save(user);

        Credential credential = new Credential();
        credential.setUserId(user.getId());
        credential.setPasswordHash(passwordEncoder.encode(request.password()));
        credential.setStatus(AccountStatus.PENDING);
        credentialRepository.save(credential);

        events.publishEvent(new UserRegistered(
                user.getId(), user.getFinKod(), user.getPersonalEmail(), user.getName()));
        return user.getId();
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByFinKod(request.finKod())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));
        Credential credential = credentialRepository.findByUserId(user.getId())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        if (!passwordEncoder.matches(request.password(), credential.getPasswordHash())) {
            throw new BadCredentialsException("Invalid credentials");
        }
        if (credential.getStatus() == AccountStatus.BLOCKED) {
            throw new ForbiddenException("Account is blocked.");
        }
        if (credential.getStatus() != AccountStatus.APPROVED) {
            throw new ForbiddenException("Account is awaiting approval.");
        }

        String token = jwtService.createAccessToken(
                user.getId(), user.getFinKod(), user.getGlobalRole().name(), user.isProfileCompleted());
        return new LoginResponse(token, user.getId(), user.getFinKod(),
                user.getGlobalRole().name(), user.isProfileCompleted());
    }

    public void requestOtp(String finKod) {
        otpService.issue(finKod);
    }

    /** Verifies an OTP and returns a short-lived token usable for password reset. */
    @Transactional
    public String verifyOtp(String finKod, String code) {
        User user = otpService.verify(finKod, code);
        return jwtService.createOtpToken(user.getFinKod());
    }

    @Transactional
    public void resetPassword(String otpToken, String newPassword) {
        String finKod = jwtService.parseOtpToken(otpToken)
                .orElseThrow(() -> new BadRequestException("Invalid or expired reset token."));
        User user = userRepository.findByFinKod(finKod)
                .orElseThrow(() -> NotFoundException.of("User", finKod));
        Credential credential = credentialRepository.findByUserId(user.getId())
                .orElseThrow(() -> NotFoundException.of("Credential", finKod));
        credential.setPasswordHash(passwordEncoder.encode(newPassword));
    }
}
