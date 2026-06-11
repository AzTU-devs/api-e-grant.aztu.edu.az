package az.aztu.egrant.report.internal;

import az.aztu.egrant.report.domain.QuarterlyReportItem;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface QuarterlyReportItemRepository extends JpaRepository<QuarterlyReportItem, Long> {

    List<QuarterlyReportItem> findByReportIdOrderByItemNoAsc(Long reportId);

    /** Bulk delete (executes immediately) so re-submitting a report can replace its points cleanly. */
    @Modifying
    @Query("delete from QuarterlyReportItem i where i.reportId = :reportId")
    void deleteByReportId(@Param("reportId") Long reportId);
}
