package az.aztu.egrant.project.internal;

import az.aztu.egrant.project.api.MembershipDirectory;
import az.aztu.egrant.project.api.ProjectMemberInfo;
import az.aztu.egrant.project.domain.MemberStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** {@link MembershipDirectory} implementation exposed to other modules (e.g. {@code budget}). */
@Service
@Transactional(readOnly = true)
public class MembershipDirectoryImpl implements MembershipDirectory {

    private final ProjectMemberRepository repository;
    private final MemberMapper mapper;

    public MembershipDirectoryImpl(ProjectMemberRepository repository, MemberMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Optional<ProjectMemberInfo> findMember(Long memberId) {
        return repository.findById(memberId).map(mapper::toInfo);
    }

    @Override
    public List<ProjectMemberInfo> membersOfProject(Long projectId) {
        return repository.findByProjectId(projectId).stream().map(mapper::toInfo).toList();
    }

    @Override
    public boolean isApprovedMember(Long projectId, Long userId) {
        return repository.existsByProjectIdAndUserIdAndStatus(projectId, userId, MemberStatus.APPROVED);
    }
}
