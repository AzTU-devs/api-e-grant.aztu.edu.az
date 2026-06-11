package az.aztu.egrant.report.domain;

import az.aztu.egrant.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

/** A quarterly report header. {@code UNIQUE(project_id, year, quarter_number)}. */
@Entity
@Table(name = "quarterly_reports")
@Getter
@Setter
public class QuarterlyReport extends BaseEntity {

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "quarter_number", nullable = false)
    private Integer quarterNumber;

    @Column(name = "year", nullable = false)
    private Integer year;

    @Column(name = "submission_date")
    private Instant submissionDate;
}
