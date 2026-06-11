package az.aztu.egrant.iam.internal;

import az.aztu.egrant.iam.api.UserDirectory;
import az.aztu.egrant.iam.api.UserSummary;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** {@link UserDirectory} implementation exposed to other modules. */
@Service
@Transactional(readOnly = true)
public class UserDirectoryImpl implements UserDirectory {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserDirectoryImpl(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Override
    public Optional<UserSummary> findById(Long userId) {
        return userRepository.findById(userId).map(userMapper::toSummary);
    }

    @Override
    public Optional<UserSummary> findByFinKod(String finKod) {
        return userRepository.findByFinKod(finKod).map(userMapper::toSummary);
    }

    @Override
    public Optional<String> findEmail(Long userId) {
        return userRepository.findById(userId).map(u -> u.getPersonalEmail());
    }

    @Override
    public boolean isProfileCompleted(Long userId) {
        return userRepository.findById(userId).map(u -> u.isProfileCompleted()).orElse(false);
    }
}
