package az.aztu.egrant.iam.internal;

import az.aztu.egrant.iam.domain.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByFinKod(String finKod);

    boolean existsByFinKod(String finKod);
}
