package az.aztu.egrant.project.domain;

import az.aztu.egrant.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * A grant project. {@code project_code} is the stable UNIQUE business key (kept for frontends);
 * {@code owner_id} is a plain FK id (cross-module reference to {@code users.id}) and is intentionally
 * <em>not</em> unique, so an owner may have multiple projects.
 */
@Entity
@Table(name = "projects")
@Getter
@Setter
public class Project extends BaseEntity {

    @Column(name = "project_code", nullable = false, unique = true)
    private Long projectCode;

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    @Column(name = "institution_id")
    private Long institutionId;

    @Column(name = "priority_id")
    private Long priorityId;

    // content
    @Column(name = "project_name", columnDefinition = "text")
    private String projectName;

    @Column(name = "project_purpose", columnDefinition = "text")
    private String projectPurpose;

    @Column(columnDefinition = "text")
    private String annotation;

    @Column(name = "key_words", columnDefinition = "text")
    private String keyWords;

    @Column(name = "scientific_idea", columnDefinition = "text")
    private String scientificIdea;

    @Column(columnDefinition = "text")
    private String structure;

    @Column(name = "team_characterization", columnDefinition = "text")
    private String teamCharacterization;

    @Column(name = "monitoring_plan", columnDefinition = "text")
    private String monitoringPlan;

    @Column(name = "assessment_plan", columnDefinition = "text")
    private String assessmentPlan;

    @Column(columnDefinition = "text")
    private String requirements;

    private LocalDate deadline;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status", columnDefinition = "project_status", nullable = false)
    private ProjectStatus status = ProjectStatus.DRAFT;

    @Column(name = "submitted_at")
    private Instant submittedAt;

    @Column(name = "collaborator_limit", nullable = false)
    private int collaboratorLimit = 7;

    @Column(name = "max_budget_amount", nullable = false)
    private int maxBudgetAmount = 30000;

    @Column(name = "deleted_at")
    private Instant deletedAt;
}
