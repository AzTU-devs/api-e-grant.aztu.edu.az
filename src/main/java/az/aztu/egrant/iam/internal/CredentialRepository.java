package az.aztu.egrant.iam.internal;

import az.aztu.egrant.iam.domain.AccountStatus;
import az.aztu.egrant.iam.domain.Credential;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CredentialRepository extends JpaRepository<Credential, Long> {

    Optional<Credential> findByUserId(Long userId);

    List<Credential> findByStatus(AccountStatus status);
}
