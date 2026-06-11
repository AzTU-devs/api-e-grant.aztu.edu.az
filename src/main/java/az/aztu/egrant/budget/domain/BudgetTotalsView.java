package az.aztu.egrant.budget.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import org.hibernate.annotations.Immutable;

/** Read-only mapping of the authoritative {@code v_budget_totals} view (schema §6.3). */
@Entity
@Immutable
@Table(name = "v_budget_totals")
@Getter
public class BudgetTotalsView {

    @Id
    @Column(name = "budget_id")
    private Long budgetId;

    @Column(name = "total_salary")
    private Integer totalSalary;

    @Column(name = "total_equipment")
    private Integer totalEquipment;

    @Column(name = "total_services")
    private Integer totalServices;

    @Column(name = "total_rent")
    private Integer totalRent;

    @Column(name = "total_other")
    private Integer totalOther;

    @Column(name = "grand_total")
    private Integer grandTotal;
}
