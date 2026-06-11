package az.aztu.egrant.expert.domain;

import az.aztu.egrant.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/** An expert's assessment of a project ({@code score} + {@code note}). {@code UNIQUE(project_id, expert_id)}. */
@Entity
@Table(name = "assessments")
@Getter
@Setter
public class Assessment extends BaseEntity {

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "expert_id", nullable = false)
    private Long expertId;

    @Column(name = "score")
    private Integer score;

    @Column(name = "note", columnDefinition = "text")
    private String note;
}
