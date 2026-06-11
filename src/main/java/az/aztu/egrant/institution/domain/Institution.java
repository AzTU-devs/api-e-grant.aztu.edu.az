package az.aztu.egrant.institution.domain;

import az.aztu.egrant.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/** An organisation users belong to and projects are hosted at. {@code code} and {@code name} are unique. */
@Entity
@Table(name = "institutions")
@Getter
@Setter
public class Institution extends BaseEntity {

    @Column(name = "code", nullable = false, unique = true)
    private String code;

    @Column(name = "name", nullable = false, unique = true)
    private String name;
}
