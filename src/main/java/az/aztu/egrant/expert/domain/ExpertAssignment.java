package az.aztu.egrant.expert.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** Assignment of an expert to a project. {@code UNIQUE(project_id, expert_id)}. No audit columns per schema. */
@Entity
@Table(name = "expert_assignments")
@Getter
@Setter
public class ExpertAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "expert_id", nullable = false)
    private Long expertId;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status", columnDefinition = "assignment_status", nullable = false)
    private AssignmentStatus status = AssignmentStatus.ASSIGNED;

    @Column(name = "assigned_at", nullable = false)
    private Instant assignedAt;

    @Column(name = "responded_at")
    private Instant respondedAt;
}
