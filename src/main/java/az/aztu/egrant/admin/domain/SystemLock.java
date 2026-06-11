package az.aztu.egrant.admin.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

/** Global submission gate (single canonical row, seeded by the V1 migration). */
@Entity
@Table(name = "system_lock")
@Getter
@Setter
public class SystemLock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "is_locked", nullable = false)
    private boolean locked;

    @Column(name = "updated_at")
    private Instant updatedAt;
}
