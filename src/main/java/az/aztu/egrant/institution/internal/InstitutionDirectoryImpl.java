package az.aztu.egrant.institution.internal;

import az.aztu.egrant.institution.api.InstitutionDirectory;
import az.aztu.egrant.institution.api.InstitutionSummary;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** {@link InstitutionDirectory} implementation exposed to other modules. */
@Service
@Transactional(readOnly = true)
public class InstitutionDirectoryImpl implements InstitutionDirectory {

    private final InstitutionRepository repository;
    private final InstitutionMapper mapper;

    public InstitutionDirectoryImpl(InstitutionRepository repository, InstitutionMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public boolean existsById(Long id) {
        return id != null && repository.existsById(id);
    }

    @Override
    public Optional<InstitutionSummary> findById(Long id) {
        return repository.findById(id).map(mapper::toSummary);
    }
}
