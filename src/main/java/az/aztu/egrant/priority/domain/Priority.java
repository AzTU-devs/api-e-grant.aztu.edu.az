package az.aztu.egrant.priority.domain;

import az.aztu.egrant.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/** A research priority a project is tagged with. {@code code} (int) is the stable business key. */
@Entity
@Table(name = "priorities")
@Getter
@Setter
public class Priority extends BaseEntity {

    @Column(name = "code", nullable = false, unique = true)
    private Integer code;

    @Column(name = "name", nullable = false, columnDefinition = "text")
    private String name;
}
