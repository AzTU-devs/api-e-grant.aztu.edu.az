package az.aztu.egrant.priority.internal;

import az.aztu.egrant.priority.api.PriorityDirectory;
import az.aztu.egrant.priority.api.PrioritySummary;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** {@link PriorityDirectory} implementation exposed to other modules. */
@Service
@Transactional(readOnly = true)
public class PriorityDirectoryImpl implements PriorityDirectory {

    private final PriorityRepository repository;
    private final PriorityMapper mapper;

    public PriorityDirectoryImpl(PriorityRepository repository, PriorityMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public boolean existsById(Long id) {
        return id != null && repository.existsById(id);
    }

    @Override
    public Optional<PrioritySummary> findById(Long id) {
        return repository.findById(id).map(mapper::toSummary);
    }

    @Override
    public List<PrioritySummary> findAll() {
        return repository.findAllByOrderByCodeAsc().stream().map(mapper::toSummary).toList();
    }
}
