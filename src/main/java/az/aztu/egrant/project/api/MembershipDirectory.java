package az.aztu.egrant.project.api;

import java.util.List;
import java.util.Optional;

/** Read-only team-membership lookups for other modules ({@code budget} ties salaries to members). */
public interface MembershipDirectory {

    Optional<ProjectMemberInfo> findMember(Long memberId);

    List<ProjectMemberInfo> membersOfProject(Long projectId);

    boolean isApprovedMember(Long projectId, Long userId);
}
