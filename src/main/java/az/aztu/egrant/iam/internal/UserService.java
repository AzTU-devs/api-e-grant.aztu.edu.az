package az.aztu.egrant.iam.internal;

import az.aztu.egrant.iam.api.UserApproved;
import az.aztu.egrant.iam.domain.AccountStatus;
import az.aztu.egrant.iam.domain.Credential;
import az.aztu.egrant.iam.domain.GlobalRole;
import az.aztu.egrant.iam.domain.User;
import az.aztu.egrant.iam.web.dto.UpdateProfileRequest;
import az.aztu.egrant.iam.web.dto.UserResponse;
import az.aztu.egrant.shared.error.NotFoundException;
import az.aztu.egrant.shared.storage.FileStorage;
import java.time.Instant;
import java.util.List;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/** User profile management, the admin approval queue, blocking and role administration. */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final CredentialRepository credentialRepository;
    private final OtpCodeRepository otpCodeRepository;
    private final UserMapper userMapper;
    private final FileStorage fileStorage;
    private final ApplicationEventPublisher events;

    public UserService(UserRepository userRepository, CredentialRepository credentialRepository,
                       OtpCodeRepository otpCodeRepository, UserMapper userMapper,
                       FileStorage fileStorage, ApplicationEventPublisher events) {
        this.userRepository = userRepository;
        this.credentialRepository = credentialRepository;
        this.otpCodeRepository = otpCodeRepository;
        this.userMapper = userMapper;
        this.fileStorage = fileStorage;
        this.events = events;
    }

    @Transactional(readOnly = true)
    public UserResponse getById(Long id) {
        User user = requireUser(id);
        return userMapper.toResponse(user, statusOf(user.getId()));
    }

    @Transactional(readOnly = true)
    public List<UserResponse> list(AccountStatus status) {
        if (status != null) {
            return credentialRepository.findByStatus(status).stream()
                    .map(c -> userRepository.findById(c.getUserId()).orElse(null))
                    .filter(u -> u != null)
                    .map(u -> userMapper.toResponse(u, status))
                    .toList();
        }
        return userRepository.findAll().stream()
                .map(u -> userMapper.toResponse(u, statusOf(u.getId())))
                .toList();
    }

    @Transactional
    public UserResponse updateProfile(Long userId, UpdateProfileRequest req) {
        User u = requireUser(userId);
        if (req.name() != null) u.setName(req.name());
        if (req.surname() != null) u.setSurname(req.surname());
        if (req.fatherName() != null) u.setFatherName(req.fatherName());
        if (req.bornDate() != null) u.setBornDate(req.bornDate());
        if (req.bornPlace() != null) u.setBornPlace(req.bornPlace());
        if (req.sex() != null) u.setSex(req.sex());
        if (req.citizenship() != null) u.setCitizenship(req.citizenship());
        if (req.personalIdNumber() != null) u.setPersonalIdNumber(req.personalIdNumber());
        if (req.livingLocation() != null) u.setLivingLocation(req.livingLocation());
        if (req.homePhone() != null) u.setHomePhone(req.homePhone());
        if (req.personalMobileNumber() != null) u.setPersonalMobileNumber(req.personalMobileNumber());
        if (req.personalEmail() != null) u.setPersonalEmail(req.personalEmail());
        if (req.workPlace() != null) u.setWorkPlace(req.workPlace());
        if (req.department() != null) u.setDepartment(req.department());
        if (req.duty() != null) u.setDuty(req.duty());
        if (req.workLocation() != null) u.setWorkLocation(req.workLocation());
        if (req.workPhone() != null) u.setWorkPhone(req.workPhone());
        if (req.workEmail() != null) u.setWorkEmail(req.workEmail());
        if (req.mainEducation() != null) u.setMainEducation(req.mainEducation());
        if (req.additionalEducation() != null) u.setAdditionalEducation(req.additionalEducation());
        if (req.scientificDegree() != null) u.setScientificDegree(req.scientificDegree());
        if (req.scientificDegreeDate() != null) u.setScientificDegreeDate(req.scientificDegreeDate());
        if (req.scientificTitle() != null) u.setScientificTitle(req.scientificTitle());
        if (req.scientificTitleDate() != null) u.setScientificTitleDate(req.scientificTitleDate());
        if (req.institutionId() != null) u.setInstitutionId(req.institutionId());

        u.setProfileCompleted(isProfileComplete(u));
        return userMapper.toResponse(u, statusOf(u.getId()));
    }

    @Transactional
    public void approve(Long userId) {
        User user = requireUser(userId);
        Credential credential = requireCredential(userId);
        credential.setStatus(AccountStatus.APPROVED);
        credential.setApprovedAt(Instant.now());
        events.publishEvent(new UserApproved(
                user.getId(), user.getFinKod(), user.getPersonalEmail(), user.getName()));
    }

    @Transactional
    public void reject(Long userId) {
        User user = requireUser(userId);
        otpCodeRepository.deleteByUserId(userId);
        credentialRepository.findByUserId(userId).ifPresent(credentialRepository::delete);
        userRepository.delete(user);
    }

    @Transactional
    public void block(Long userId) {
        Credential credential = requireCredential(userId);
        credential.setStatus(AccountStatus.BLOCKED);
        credential.setBlockedAt(Instant.now());
    }

    @Transactional
    public void unblock(Long userId) {
        Credential credential = requireCredential(userId);
        credential.setStatus(AccountStatus.APPROVED);
        credential.setUnblockedAt(Instant.now());
    }

    @Transactional
    public void setRole(Long userId, GlobalRole role) {
        requireUser(userId).setGlobalRole(role);
    }

    @Transactional
    public UserResponse setAvatar(Long userId, String filename, String contentType, byte[] content) {
        User user = requireUser(userId);
        if (StringUtils.hasText(user.getAvatarUrl())) {
            fileStorage.delete(user.getAvatarUrl());
        }
        String key = fileStorage.store("avatars", filename, contentType, content);
        user.setAvatarUrl(key);
        return userMapper.toResponse(user, statusOf(user.getId()));
    }

    @Transactional(readOnly = true)
    public FileStorage.StoredFile loadAvatar(Long userId) {
        User user = requireUser(userId);
        if (!StringUtils.hasText(user.getAvatarUrl())) {
            throw NotFoundException.of("Avatar", userId);
        }
        return fileStorage.load(user.getAvatarUrl())
                .orElseThrow(() -> NotFoundException.of("Avatar", userId));
    }

    private User requireUser(Long id) {
        return userRepository.findById(id).orElseThrow(() -> NotFoundException.of("User", id));
    }

    private Credential requireCredential(Long userId) {
        return credentialRepository.findByUserId(userId)
                .orElseThrow(() -> NotFoundException.of("Credential", userId));
    }

    private AccountStatus statusOf(Long userId) {
        return credentialRepository.findByUserId(userId)
                .map(Credential::getStatus)
                .orElse(AccountStatus.PENDING);
    }

    /** Required-field set for a complete profile (gates collaborator-join/approve/submit). */
    private boolean isProfileComplete(User u) {
        return StringUtils.hasText(u.getName())
                && StringUtils.hasText(u.getSurname())
                && StringUtils.hasText(u.getFatherName())
                && u.getBornDate() != null
                && StringUtils.hasText(u.getBornPlace())
                && StringUtils.hasText(u.getSex())
                && StringUtils.hasText(u.getCitizenship())
                && StringUtils.hasText(u.getPersonalIdNumber())
                && StringUtils.hasText(u.getPersonalMobileNumber())
                && StringUtils.hasText(u.getPersonalEmail())
                && StringUtils.hasText(u.getWorkPlace())
                && StringUtils.hasText(u.getDepartment())
                && StringUtils.hasText(u.getDuty());
    }
}
