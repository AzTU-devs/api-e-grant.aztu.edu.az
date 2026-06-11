package az.aztu.egrant.institution.internal;

import az.aztu.egrant.institution.domain.Institution;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InstitutionRepository extends JpaRepository<Institution, Long> {

    List<Institution> findAllByOrderByNameAsc();

    boolean existsByCode(String code);

    boolean existsByName(String name);
}
