package az.aztu.egrant.iam.domain;

import az.aztu.egrant.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * A platform user (researcher/admin). Natural key {@code fin_kod} stays a stable UNIQUE
 * business column; the surrogate {@code id} is the relational glue. {@code institution_id}
 * is kept as a plain id (the institution lives in another module).
 */
@Entity
@Table(name = "users")
@Getter
@Setter
public class User extends BaseEntity {

    @Column(name = "fin_kod", nullable = false, unique = true, length = 7)
    private String finKod;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "global_role", columnDefinition = "global_role", nullable = false)
    private GlobalRole globalRole = GlobalRole.APPLICANT;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "academic_type", columnDefinition = "academic_type")
    private AcademicType academicType;

    // identity
    private String name;
    private String surname;

    @Column(name = "father_name")
    private String fatherName;

    @Column(name = "born_date")
    private LocalDate bornDate;

    @Column(name = "born_place")
    private String bornPlace;

    private String sex;
    private String citizenship;

    @Column(name = "personal_id_number")
    private String personalIdNumber;

    @Column(name = "living_location")
    private String livingLocation;

    // contact
    @Column(name = "home_phone")
    private String homePhone;

    @Column(name = "personal_mobile_number")
    private String personalMobileNumber;

    @Column(name = "personal_email")
    private String personalEmail;

    // work
    @Column(name = "work_place")
    private String workPlace;

    private String department;
    private String duty;

    @Column(name = "work_location")
    private String workLocation;

    @Column(name = "work_phone")
    private String workPhone;

    @Column(name = "work_email")
    private String workEmail;

    // education
    @Column(name = "main_education")
    private String mainEducation;

    @Column(name = "additional_education")
    private String additionalEducation;

    // science
    @Column(name = "scientific_degree")
    private String scientificDegree;

    @Column(name = "scientific_degree_date")
    private LocalDate scientificDegreeDate;

    @Column(name = "scientific_title")
    private String scientificTitle;

    @Column(name = "scientific_title_date")
    private LocalDate scientificTitleDate;

    @Column(name = "institution_id")
    private Long institutionId;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "profile_completed", nullable = false)
    private boolean profileCompleted = false;

    @Column(name = "deleted_at")
    private Instant deletedAt;
}
