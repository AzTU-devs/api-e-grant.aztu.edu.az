package az.aztu.egrant.expert.domain;

import az.aztu.egrant.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/** A reviewing expert. FK-linked from assignments/assessments (fixes legacy email-string linkage). */
@Entity
@Table(name = "experts")
@Getter
@Setter
public class Expert extends BaseEntity {

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "surname", nullable = false)
    private String surname;

    @Column(name = "father_name")
    private String fatherName;

    @Column(name = "personal_id_serial_number", nullable = false, unique = true)
    private String personalIdSerialNumber;

    @Column(name = "work_place")
    private String workPlace;

    private String duty;

    @Column(name = "scientific_degree")
    private String scientificDegree;

    @Column(name = "phone_number")
    private String phoneNumber;

    /** Optional link to a platform user, if the expert also logs in. */
    @Column(name = "user_id")
    private Long userId;
}
