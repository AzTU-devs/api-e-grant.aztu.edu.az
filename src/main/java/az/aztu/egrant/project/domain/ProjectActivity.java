package az.aztu.egrant.project.domain;

import az.aztu.egrant.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/** A monthly activity-plan entry for a project ({@code month} 1–12). */
@Entity
@Table(name = "project_activities")
@Getter
@Setter
public class ProjectActivity extends BaseEntity {

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "month", nullable = false)
    private Integer month;

    @Column(name = "activity_name", nullable = false, columnDefinition = "text")
    private String activityName;
}
