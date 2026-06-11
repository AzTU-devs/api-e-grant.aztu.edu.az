package az.aztu.egrant.priority.internal;

import az.aztu.egrant.priority.domain.Priority;
import az.aztu.egrant.priority.web.dto.CreatePriorityRequest;
import az.aztu.egrant.priority.web.dto.PriorityResponse;
import az.aztu.egrant.priority.web.dto.UpdatePriorityRequest;
import az.aztu.egrant.shared.error.ConflictException;
import az.aztu.egrant.shared.error.NotFoundException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Research-priority lookup management: list, read, create, update and delete. */
@Service
public class PriorityService {

    private final PriorityRepository repository;
    private final PriorityMapper mapper;

    public PriorityService(PriorityRepository repository, PriorityMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<PriorityResponse> list() {
        return repository.findAllByOrderByCodeAsc().stream().map(mapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public PriorityResponse get(Long id) {
        return mapper.toResponse(require(id));
    }

    @Transactional
    public PriorityResponse create(CreatePriorityRequest request) {
        if (repository.existsByCode(request.code())) {
            throw new ConflictException("A priority with this code already exists.");
        }
        Priority priority = new Priority();
        priority.setCode(request.code());
        priority.setName(request.name());
        return mapper.toResponse(repository.save(priority));
    }

    @Transactional
    public PriorityResponse update(Long id, UpdatePriorityRequest request) {
        Priority priority = require(id);
        if (request.code() != null && !request.code().equals(priority.getCode())) {
            if (repository.existsByCode(request.code())) {
                throw new ConflictException("A priority with this code already exists.");
            }
            priority.setCode(request.code());
        }
        priority.setName(request.name());
        return mapper.toResponse(priority);
    }

    @Transactional
    public void delete(Long id) {
        repository.delete(require(id));
    }

    private Priority require(Long id) {
        return repository.findById(id).orElseThrow(() -> NotFoundException.of("Priority", id));
    }
}
