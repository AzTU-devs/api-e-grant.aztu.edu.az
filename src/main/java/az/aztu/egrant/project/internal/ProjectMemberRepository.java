package az.aztu.egrant.project.internal;

import az.aztu.egrant.project.domain.MemberRole;
import az.aztu.egrant.project.domain.MemberStatus;
import az.aztu.egrant.project.domain.ProjectMember;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {

    List<ProjectMember> findByProjectId(Long projectId);

    List<ProjectMember> findByProjectIdAndStatus(Long projectId, MemberStatus status);

    Optional<ProjectMember> findByProjectIdAndUserId(Long projectId, Long userId);

    boolean existsByProjectIdAndUserId(Long projectId, Long userId);

    boolean existsByProjectIdAndUserIdAndStatus(Long projectId, Long userId, MemberStatus status);

    int countByProjectIdAndRoleAndStatus(Long projectId, MemberRole role, MemberStatus status);
}
