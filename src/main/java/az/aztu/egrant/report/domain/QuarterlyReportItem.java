package az.aztu.egrant.report.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/** One numbered point of a quarterly report. {@code UNIQUE(report_id, item_no)}. No audit columns. */
@Entity
@Table(name = "quarterly_report_items")
@Getter
@Setter
public class QuarterlyReportItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "report_id", nullable = false)
    private Long reportId;

    @Column(name = "item_no", nullable = false)
    private Integer itemNo;

    @Column(name = "content", columnDefinition = "text")
    private String content;
}
