package az.aztu.egrant.project.domain;

import az.aztu.egrant.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * A team membership (replaces the legacy {@code collaborators}; also models the owner).
 * {@code UNIQUE(project_id, user_id)} fixes the legacy global-unique {@code fin_kod} defect:
 * a person may join many projects and a project may have many collaborators.
 */
@Entity
@Table(name = "project_members")
@Getter
@Setter
public class ProjectMember extends BaseEntity {

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "role", columnDefinition = "member_role", nullable = false)
    private MemberRole role;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status", columnDefinition = "member_status", nullable = false)
    private MemberStatus status = MemberStatus.PENDING;

    @Column(name = "joined_at")
    private Instant joinedAt;

    @Column(name = "approved_at")
    private Instant approvedAt;
}
