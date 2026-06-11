package az.aztu.egrant.document.internal;

import az.aztu.egrant.budget.api.BudgetDirectory;
import az.aztu.egrant.budget.api.BudgetTotals;
import az.aztu.egrant.budget.api.SalaryLine;
import az.aztu.egrant.document.internal.ExportModel.MemberRow;
import az.aztu.egrant.document.internal.ExportModel.SalaryRow;
import az.aztu.egrant.iam.api.UserDirectory;
import az.aztu.egrant.iam.api.UserSummary;
import az.aztu.egrant.institution.api.InstitutionDirectory;
import az.aztu.egrant.institution.api.InstitutionSummary;
import az.aztu.egrant.priority.api.PriorityDirectory;
import az.aztu.egrant.priority.api.PrioritySummary;
import az.aztu.egrant.project.api.MembershipDirectory;
import az.aztu.egrant.project.api.ProjectDetail;
import az.aztu.egrant.project.api.ProjectDirectory;
import az.aztu.egrant.project.api.ProjectMemberInfo;
import az.aztu.egrant.shared.error.ForbiddenException;
import az.aztu.egrant.shared.error.NotFoundException;
import az.aztu.egrant.shared.security.AuthenticatedUser;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Gathers a project's data via published APIs and renders it as PDF or Excel. */
@Service
public class ProjectExportService {

    private final ProjectDirectory projectDirectory;
    private final MembershipDirectory membershipDirectory;
    private final BudgetDirectory budgetDirectory;
    private final UserDirectory userDirectory;
    private final InstitutionDirectory institutionDirectory;
    private final PriorityDirectory priorityDirectory;
    private final ProjectPdfRenderer pdfRenderer;
    private final ProjectExcelRenderer excelRenderer;

    public ProjectExportService(ProjectDirectory projectDirectory, MembershipDirectory membershipDirectory,
                                BudgetDirectory budgetDirectory, UserDirectory userDirectory,
                                InstitutionDirectory institutionDirectory, PriorityDirectory priorityDirectory,
                                ProjectPdfRenderer pdfRenderer, ProjectExcelRenderer excelRenderer) {
        this.projectDirectory = projectDirectory;
        this.membershipDirectory = membershipDirectory;
        this.budgetDirectory = budgetDirectory;
        this.userDirectory = userDirectory;
        this.institutionDirectory = institutionDirectory;
        this.priorityDirectory = priorityDirectory;
        this.pdfRenderer = pdfRenderer;
        this.excelRenderer = excelRenderer;
    }

    @Transactional(readOnly = true)
    public GeneratedDocument exportPdf(Long projectId, AuthenticatedUser actor) {
        ExportModel model = buildModel(projectId, actor);
        byte[] content = pdfRenderer.render(model);
        return new GeneratedDocument(filename(model, ".pdf"), "application/pdf", content);
    }

    @Transactional(readOnly = true)
    public GeneratedDocument exportExcel(Long projectId, AuthenticatedUser actor) {
        ExportModel model = buildModel(projectId, actor);
        byte[] content = excelRenderer.render(model);
        return new GeneratedDocument(filename(model, ".xlsx"),
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", content);
    }

    private ExportModel buildModel(Long projectId, AuthenticatedUser actor) {
        ProjectDetail project = projectDirectory.findDetailById(projectId)
                .orElseThrow(() -> NotFoundException.of("Project", projectId));
        requireExportAccess(projectId, project.ownerId(), actor);

        List<MemberRow> members = membershipDirectory.membersOfProject(projectId).stream()
                .map(this::toMemberRow).toList();
        BudgetTotals totals = budgetDirectory.totalsForProject(projectId).orElse(null);
        List<SalaryRow> salaries = budgetDirectory.salariesForProject(projectId).stream()
                .map(this::toSalaryRow).toList();

        return new ExportModel(
                project,
                userName(project.ownerId()),
                userDirectory.findById(project.ownerId()).map(UserSummary::finKod).orElse(null),
                project.institutionId() == null ? null
                        : institutionDirectory.findById(project.institutionId())
                                .map(InstitutionSummary::name).orElse(null),
                project.priorityId() == null ? null
                        : priorityDirectory.findById(project.priorityId())
                                .map(PrioritySummary::name).orElse(null),
                members,
                projectDirectory.activitiesForProject(projectId),
                totals,
                salaries,
                budgetDirectory.lineItemsForProject(projectId));
    }

    private void requireExportAccess(Long projectId, Long ownerId, AuthenticatedUser actor) {
        boolean admin = "ADMIN".equals(actor.role()) || "SUPER_ADMIN".equals(actor.role());
        boolean owner = ownerId.equals(actor.userId());
        if (admin || owner || membershipDirectory.isApprovedMember(projectId, actor.userId())) {
            return;
        }
        throw new ForbiddenException("Only team members or an admin may export this project.");
    }

    private MemberRow toMemberRow(ProjectMemberInfo m) {
        return new MemberRow(userName(m.userId()), m.role(), m.status());
    }

    private SalaryRow toSalaryRow(SalaryLine s) {
        String memberName = membershipDirectory.findMember(s.memberId())
                .map(m -> userName(m.userId())).orElse("Member #" + s.memberId());
        return new SalaryRow(memberName, s.salaryPerMonth(), s.months(), s.totalAmount());
    }

    private String userName(Long userId) {
        return userDirectory.findById(userId)
                .map(u -> ((u.name() == null ? "" : u.name()) + " "
                        + (u.surname() == null ? "" : u.surname())).trim())
                .orElse(null);
    }

    private String filename(ExportModel model, String extension) {
        return "project-" + model.project().projectCode() + extension;
    }
}
