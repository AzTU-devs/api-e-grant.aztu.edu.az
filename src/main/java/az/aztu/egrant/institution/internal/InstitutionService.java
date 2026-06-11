package az.aztu.egrant.institution.internal;

import az.aztu.egrant.institution.domain.Institution;
import az.aztu.egrant.institution.web.dto.CreateInstitutionRequest;
import az.aztu.egrant.institution.web.dto.InstitutionResponse;
import az.aztu.egrant.shared.error.ConflictException;
import az.aztu.egrant.shared.error.NotFoundException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Institution lookup management: list, read, create and delete. */
@Service
public class InstitutionService {

    private final InstitutionRepository repository;
    private final InstitutionMapper mapper;

    public InstitutionService(InstitutionRepository repository, InstitutionMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<InstitutionResponse> list() {
        return repository.findAllByOrderByNameAsc().stream().map(mapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public InstitutionResponse get(Long id) {
        return mapper.toResponse(require(id));
    }

    @Transactional
    public InstitutionResponse create(CreateInstitutionRequest request) {
        if (repository.existsByCode(request.code())) {
            throw new ConflictException("An institution with this code already exists.");
        }
        if (repository.existsByName(request.name())) {
            throw new ConflictException("An institution with this name already exists.");
        }
        Institution institution = new Institution();
        institution.setCode(request.code());
        institution.setName(request.name());
        return mapper.toResponse(repository.save(institution));
    }

    @Transactional
    public void delete(Long id) {
        repository.delete(require(id));
    }

    private Institution require(Long id) {
        return repository.findById(id).orElseThrow(() -> NotFoundException.of("Institution", id));
    }
}
