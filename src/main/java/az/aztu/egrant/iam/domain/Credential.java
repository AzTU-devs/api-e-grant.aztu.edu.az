package az.aztu.egrant.iam.domain;

import az.aztu.egrant.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** Login credentials, 1:1 with {@link User}. Collapses the legacy {@code approved}+{@code blocked} flags into {@code status}. */
@Entity
@Table(name = "credentials")
@Getter
@Setter
public class Credential extends BaseEntity {

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status", columnDefinition = "account_status", nullable = false)
    private AccountStatus status = AccountStatus.PENDING;

    @Column(name = "otp_verified", nullable = false)
    private boolean otpVerified = false;

    @Column(name = "approved_at")
    private Instant approvedAt;

    @Column(name = "blocked_at")
    private Instant blockedAt;

    @Column(name = "unblocked_at")
    private Instant unblockedAt;
}
