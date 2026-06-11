package az.aztu.egrant.report.internal;

import az.aztu.egrant.project.api.ProjectDirectory;
import az.aztu.egrant.project.api.ProjectSummary;
import az.aztu.egrant.report.domain.QuarterlyReport;
import az.aztu.egrant.report.domain.QuarterlyReportItem;
import az.aztu.egrant.report.web.dto.ReportPointRequest;
import az.aztu.egrant.report.web.dto.ReportPointResponse;
import az.aztu.egrant.report.web.dto.ReportResponse;
import az.aztu.egrant.report.web.dto.SubmitReportRequest;
import az.aztu.egrant.shared.error.BadRequestException;
import az.aztu.egrant.shared.error.ForbiddenException;
import az.aztu.egrant.shared.error.NotFoundException;
import az.aztu.egrant.shared.security.AuthenticatedUser;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Quarterly report management: list/get and file (upsert) the 17 points as report items. */
@Service
public class ReportService {

    private final QuarterlyReportRepository reportRepository;
    private final QuarterlyReportItemRepository itemRepository;
    private final ProjectDirectory projectDirectory;

    public ReportService(QuarterlyReportRepository reportRepository,
                         QuarterlyReportItemRepository itemRepository, ProjectDirectory projectDirectory) {
        this.reportRepository = reportRepository;
        this.itemRepository = itemRepository;
        this.projectDirectory = projectDirectory;
    }

    @Transactional(readOnly = true)
    public List<ReportResponse> list(Long projectId, Integer year, Integer quarter, AuthenticatedUser actor) {
        requireOwnerOrAdmin(requireProject(projectId), actor);
        List<QuarterlyReport> reports = (year != null && quarter != null)
                ? reportRepository.findByProjectIdAndYearAndQuarterNumber(projectId, year, quarter)
                        .map(List::of).orElseGet(List::of)
                : reportRepository.findByProjectIdOrderByYearDescQuarterNumberDesc(projectId);
        return reports.stream().map(this::toResponse).toList();
    }

    @Transactional
    public ReportResponse submit(Long projectId, AuthenticatedUser actor, SubmitReportRequest req) {
        requireOwnerOrAdmin(requireProject(projectId), actor);
        requireDistinctPoints(req.points());

        QuarterlyReport report = reportRepository
                .findByProjectIdAndYearAndQuarterNumber(projectId, req.year(), req.quarterNumber())
                .orElseGet(QuarterlyReport::new);
        report.setProjectId(projectId);
        report.setYear(req.year());
        report.setQuarterNumber(req.quarterNumber());
        report.setSubmissionDate(Instant.now());
        QuarterlyReport saved = reportRepository.save(report);

        itemRepository.deleteByReportId(saved.getId()); // bulk delete; lets re-submission replace points
        List<QuarterlyReportItem> items = req.points().stream().map(p -> {
            QuarterlyReportItem item = new QuarterlyReportItem();
            item.setReportId(saved.getId());
            item.setItemNo(p.itemNo());
            item.setContent(p.content());
            return item;
        }).toList();
        return toResponse(saved, itemRepository.saveAll(items));
    }

    private ProjectSummary requireProject(Long projectId) {
        return projectDirectory.findById(projectId)
                .orElseThrow(() -> NotFoundException.of("Project", projectId));
    }

    private void requireOwnerOrAdmin(ProjectSummary project, AuthenticatedUser actor) {
        boolean admin = "ADMIN".equals(actor.role()) || "SUPER_ADMIN".equals(actor.role());
        if (!project.ownerId().equals(actor.userId()) && !admin) {
            throw new ForbiddenException("Only the project owner or an admin may access reports.");
        }
    }

    private void requireDistinctPoints(List<ReportPointRequest> points) {
        long distinct = points.stream().map(ReportPointRequest::itemNo).distinct().count();
        if (distinct != points.size()) {
            throw new BadRequestException("Duplicate point numbers are not allowed in a report.");
        }
    }

    private ReportResponse toResponse(QuarterlyReport report) {
        return toResponse(report, itemRepository.findByReportIdOrderByItemNoAsc(report.getId()));
    }

    private ReportResponse toResponse(QuarterlyReport report, List<QuarterlyReportItem> items) {
        List<ReportPointResponse> points = items.stream()
                .sorted(Comparator.comparing(QuarterlyReportItem::getItemNo))
                .map(i -> new ReportPointResponse(i.getItemNo(), i.getContent()))
                .collect(Collectors.toList());
        return new ReportResponse(report.getId(), report.getProjectId(), report.getYear(),
                report.getQuarterNumber(), report.getSubmissionDate(), points,
                report.getCreatedAt(), report.getUpdatedAt());
    }
}
