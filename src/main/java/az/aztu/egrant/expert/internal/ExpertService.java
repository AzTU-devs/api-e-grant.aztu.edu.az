package az.aztu.egrant.expert.internal;

import az.aztu.egrant.expert.domain.Expert;
import az.aztu.egrant.expert.web.dto.CreateExpertRequest;
import az.aztu.egrant.expert.web.dto.ExpertResponse;
import az.aztu.egrant.shared.error.ConflictException;
import az.aztu.egrant.shared.error.NotFoundException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Expert registry management (admin). */
@Service
public class ExpertService {

    private final ExpertRepository repository;
    private final ExpertMapper mapper;

    public ExpertService(ExpertRepository repository, ExpertMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<ExpertResponse> list() {
        return repository.findAllByOrderBySurnameAscNameAsc().stream().map(mapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ExpertResponse get(Long id) {
        return mapper.toResponse(require(id));
    }

    @Transactional
    public ExpertResponse create(CreateExpertRequest req) {
        if (repository.existsByEmail(req.email())) {
            throw new ConflictException("An expert with this email already exists.");
        }
        if (repository.existsByPersonalIdSerialNumber(req.personalIdSerialNumber())) {
            throw new ConflictException("An expert with this personal id already exists.");
        }
        Expert e = new Expert();
        e.setEmail(req.email());
        e.setName(req.name());
        e.setSurname(req.surname());
        e.setFatherName(req.fatherName());
        e.setPersonalIdSerialNumber(req.personalIdSerialNumber());
        e.setWorkPlace(req.workPlace());
        e.setDuty(req.duty());
        e.setScientificDegree(req.scientificDegree());
        e.setPhoneNumber(req.phoneNumber());
        e.setUserId(req.userId());
        return mapper.toResponse(repository.save(e));
    }

    @Transactional
    public void delete(Long id) {
        repository.delete(require(id));
    }

    private Expert require(Long id) {
        return repository.findById(id).orElseThrow(() -> NotFoundException.of("Expert", id));
    }
}
