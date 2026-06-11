package az.aztu.egrant.budget.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

/**
 * A salary line tied to a real team member ({@code member_id} → {@code project_members.id}).
 * {@code total_amount} is DB-computed ({@code salary_per_month * months}); single surrogate PK
 * fixes the legacy double-PK defect. No audit columns per schema §6.3.
 */
@Entity
@Table(name = "budget_salaries")
@Getter
@Setter
public class BudgetSalary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "budget_id", nullable = false)
    private Long budgetId;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "salary_per_month", nullable = false)
    private Integer salaryPerMonth;

    @Column(name = "months", nullable = false)
    private Integer months;

    /** {@code GENERATED ALWAYS AS (salary_per_month * months) STORED} — read back after write. */
    @Generated(event = {EventType.INSERT, EventType.UPDATE})
    @Column(name = "total_amount", insertable = false, updatable = false)
    private Integer totalAmount;
}
