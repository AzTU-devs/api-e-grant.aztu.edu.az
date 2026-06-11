package az.aztu.egrant.report.internal;

import az.aztu.egrant.report.domain.QuarterlyReport;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuarterlyReportRepository extends JpaRepository<QuarterlyReport, Long> {

    Optional<QuarterlyReport> findByProjectIdAndYearAndQuarterNumber(Long projectId, Integer year, Integer quarterNumber);

    List<QuarterlyReport> findByProjectIdOrderByYearDescQuarterNumberDesc(Long projectId);
}
