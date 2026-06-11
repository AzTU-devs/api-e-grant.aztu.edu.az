package az.aztu.egrant.publicapi.internal;

import az.aztu.egrant.iam.api.UserDirectory;
import az.aztu.egrant.iam.api.UserSummary;
import az.aztu.egrant.institution.api.InstitutionDirectory;
import az.aztu.egrant.institution.api.InstitutionSummary;
import az.aztu.egrant.priority.api.PriorityDirectory;
import az.aztu.egrant.priority.api.PrioritySummary;
import az.aztu.egrant.project.api.ProjectDetail;
import az.aztu.egrant.project.api.ProjectDirectory;
import az.aztu.egrant.project.api.ProjectSummary;
import az.aztu.egrant.publicapi.web.dto.PriorityTreeNode;
import az.aztu.egrant.publicapi.web.dto.PublicProjectDetail;
import az.aztu.egrant.publicapi.web.dto.PublicProjectSummary;
import az.aztu.egrant.shared.error.NotFoundException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/** Builds sanitized public views by aggregating other modules' read-only directories. */
@Service
public class PublicQueryService {

    private static final String APPROVED = "APPROVED";

    private final ProjectDirectory projectDirectory;
    private final PriorityDirectory priorityDirectory;
    private final InstitutionDirectory institutionDirectory;
    private final UserDirectory userDirectory;

    public PublicQueryService(ProjectDirectory projectDirectory, PriorityDirectory priorityDirectory,
                              InstitutionDirectory institutionDirectory, UserDirectory userDirectory) {
        this.projectDirectory = projectDirectory;
        this.priorityDirectory = priorityDirectory;
        this.institutionDirectory = institutionDirectory;
        this.userDirectory = userDirectory;
    }

    public List<PublicProjectSummary> listApprovedProjects() {
        return projectDirectory.findByStatus(APPROVED).stream().map(this::toSummary).toList();
    }

    public PublicProjectDetail getApprovedProject(Long projectCode) {
        ProjectDetail d = projectDirectory.findDetailByCode(projectCode)
                .filter(p -> APPROVED.equals(p.status()))
                .orElseThrow(() -> NotFoundException.of("Project", projectCode));
        return new PublicProjectDetail(d.projectCode(), d.projectName(), d.projectPurpose(), d.annotation(),
                d.keyWords(), d.scientificIdea(), d.structure(), d.teamCharacterization(), d.monitoringPlan(),
                d.assessmentPlan(), d.requirements(), d.deadline(), d.status(),
                institutionName(d.institutionId()), priorityName(d.priorityId()), ownerName(d.ownerId()));
    }

    public List<PriorityTreeNode> prioritiesTree() {
        Map<Long, List<PublicProjectSummary>> byPriority = projectDirectory.findByStatus(APPROVED).stream()
                .filter(p -> p.priorityId() != null)
                .collect(Collectors.groupingBy(ProjectSummary::priorityId,
                        Collectors.mapping(this::toSummary, Collectors.toList())));
        return priorityDirectory.findAll().stream()
                .map(p -> new PriorityTreeNode(p.code(), p.name(),
                        byPriority.getOrDefault(p.id(), List.of())))
                .toList();
    }

    private PublicProjectSummary toSummary(ProjectSummary s) {
        return new PublicProjectSummary(s.projectCode(), s.projectName(), s.status(),
                institutionName(s.institutionId()), priorityName(s.priorityId()), ownerName(s.ownerId()));
    }

    private String institutionName(Long institutionId) {
        return institutionId == null ? null
                : institutionDirectory.findById(institutionId).map(InstitutionSummary::name).orElse(null);
    }

    private String priorityName(Long priorityId) {
        return priorityId == null ? null
                : priorityDirectory.findById(priorityId).map(PrioritySummary::name).orElse(null);
    }

    private String ownerName(Long ownerId) {
        return userDirectory.findById(ownerId)
                .map(u -> displayName(u))
                .orElse(null);
    }

    private static String displayName(UserSummary u) {
        return ((u.name() == null ? "" : u.name()) + " " + (u.surname() == null ? "" : u.surname())).trim();
    }
}
