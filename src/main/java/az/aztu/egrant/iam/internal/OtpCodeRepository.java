package az.aztu.egrant.iam.internal;

import az.aztu.egrant.iam.domain.OtpCode;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OtpCodeRepository extends JpaRepository<OtpCode, Long> {

    /** Latest issued, not-yet-consumed code for a user. */
    Optional<OtpCode> findFirstByUserIdAndConsumedAtIsNullOrderByIssuedAtDesc(Long userId);

    void deleteByUserId(Long userId);
}
